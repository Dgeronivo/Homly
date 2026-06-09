# Auth Feature Plan — Login / Registration

## Context

Додаємо авторизацію і реєстрацію за логіном+паролем.
- Логін — **не унікальний**; унікальна зв'язка `(login, password)`.
- Пароль зберігається хешованим (PBKDF2-HmacSHA256, per-row salt) — **підхід TBD, обговорюємо пізніше**.
- Юзер персистується в локальній Room-БД, сесія — в DataStore.
- Після авторизації: HomeScreen з `"Hello {login}"`.
- Архітектура через `UserRepository` interface — дозволяє підключити backend без зміни ViewModel/UseCase.

---

## A. Аналіз варіантів зберігання даних (для майбутнього шерингу)

| Варіант | Плюси | Мінуси | Fit для (login,pass)-unique | Fit для колаборації |
|---|---|---|---|---|
| **Room + DataStore** (обрано зараз) | Нуль інфраструктури, offline, простий | Один пристрій | Ідеальний (перевірка в коді repo) | Погано сам по собі; ОК як offline-half |
| Firebase Auth + Firestore | Realtime, offline-cache, без бекенду | Auth вимагає унікальний email — **конфліктує** з логін-не-унікальний; vendor lock-in | Погано через Firebase Auth | Відмінно (Firestore listeners) |
| Supabase (Postgres + realtime) | SQL, RLS, realtime, open-source | Supabase Auth теж email-unique; потрібен зовнішній сервіс | OK якщо обійти їх Auth, використовувати свою таблицю | Дуже добре |
| Custom Ktor backend + Room cache | Повний контроль моделі auth | Потрібно розробити і деплоїти бекенд | Ідеальний | Відмінно (WS/HTTP) |
| CRDT (Yjs/Automerge) | Conflict-free collaborative editing | Overkill для auth, складно, не вирішує identity | N/A | Найкраще для документів, але поверх одного з вищих |

**Рекомендація на зараз**: Room + DataStore.
**Рекомендація на майбутнє**: Custom Ktor backend + Room (повний контроль auth-моделі) або Supabase з власною таблицею users (без Firebase Auth).

---

## B. Структура пакетів

```
com.dgero.homly/
├── HomlyApplication.kt              # Application subclass, AppContainer
├── auth/
│   ├── presentation/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   ├── LoginViewModel.kt
│   │   │   └── LoginUiState.kt
│   │   ├── register/
│   │   │   ├── RegisterScreen.kt
│   │   │   ├── RegisterViewModel.kt
│   │   │   └── RegisterUiState.kt
│   │   └── AuthNavGraph.kt          # вкладений nav-граф
│   ├── domain/
│   │   ├── model/
│   │   │   ├── User.kt              # User(id: Long, login: String) — хеш назовні не виходить
│   │   │   └── AuthError.kt        # sealed: EmptyLogin, EmptyPassword, LoginTooShort, PasswordTooShort, InvalidLoginChars, InvalidPasswordChars, DuplicateLogin, InvalidCredentials, Unknown
│   │   ├── repository/
│   │   │   ├── UserRepository.kt
│   │   │   └── SessionRepository.kt
│   │   └── usecase/
│   │       ├── RegisterUserUseCase.kt
│   │       ├── LoginUserUseCase.kt
│   │       ├── LogoutUseCase.kt
│   │       └── ObserveSessionUseCase.kt
│   └── data/
│       ├── local/
│       │   ├── UserEntity.kt        # id, login, passwordHash, salt, createdAt
│       │   └── UserDao.kt           # insert, findByLogin(login): List, findById
│       ├── session/
│       │   └── DataStoreSessionRepository.kt   # key "current_user_id": Long?
│       ├── crypto/
│       │   └── PasswordHasher.kt    # interface + Pbkdf2PasswordHasher (javax.crypto, без external lib)
│       └── repository/
│           └── LocalUserRepository.kt
├── home/
│   └── presentation/
│       ├── HomeScreen.kt            # "Hello {login}" + Logout button
│       └── HomeViewModel.kt
├── core/
│   └── data/
│       └── HomlyDatabase.kt         # @Database(entities=[UserEntity,...], version=1) — спільна БД для всіх feature
└── ui/theme/                        # без змін
```

**hello/ видаляємо**: HomeScreen робить те саме, дві схожих screens не потрібні.

---

## C. Repository Interfaces

```kotlin
// domain/repository/UserRepository.kt
interface UserRepository {
    suspend fun register(login: String, password: String): Result<User>
    suspend fun login(login: String, password: String): Result<User>
}

// domain/repository/SessionRepository.kt
interface SessionRepository {
    val currentUserId: Flow<Long?>
    suspend fun setSession(userId: Long)
    suspend fun clear()
}
```

`LocalUserRepository` реалізує `UserRepository`. Майбутній `RemoteUserRepository` (або `OfflineFirstUserRepository`) підключається в `AppContainer` через `BuildConfig.AUTH_BACKEND` flag.

---

## D. Ключова логіка: (login, password) uniqueness з hashed password

Оскільки `passwordHash = pbkdf2(password, randomSalt)` — той самий пароль дає різний хеш щоразу.  
**Composite UNIQUE index у Room неможливий.** Замість нього — логіка в `LocalUserRepository`:

### Register
```
db.withTransaction {
  candidates = dao.findByLogin(login)          // індекс по login (non-unique)
  if candidates.any { hasher.verify(password, it.passwordHash, it.salt) }
      → Result.failure(AuthError.DuplicateAccount)
  else
      salt = randomSalt()
      hash = pbkdf2(password, salt)
      dao.insert(UserEntity(login, hash, salt))
      → Result.success(User(id, login))
}
```

### Login
```
candidates = dao.findByLogin(login)
match = candidates.firstOrNull { hasher.verify(password, it.passwordHash, it.salt) }
if match != null → Result.success(User(match.id, login))
else             → Result.failure(AuthError.InvalidCredentials)  // однакова помилка для "не існує" і "не вірний пароль" — щоб не давати підказки
```

---

## E. Password Hashing

**PBKDF2-HmacSHA256** через `javax.crypto.SecretKeyFactory` (є в Android JDK, нульові зовнішні залежності).

- Salt: 16 байт, `SecureRandom`
- Iterations: 120 000 (OWASP floor 2023); в тестах — параметр що знижується до ~1000
- Output: 32 байти → Base64
- Storage: окремі колонки `passwordHash` (String) і `salt` (String)

---

## F. DI — Manual Application Container

```kotlin
class HomlyApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val db = Room.databaseBuilder(context, HomlyDatabase::class.java, "homly.db").build()  // core/data/
    val passwordHasher: PasswordHasher = Pbkdf2PasswordHasher()
    val userRepository: UserRepository = LocalUserRepository(db.userDao(), passwordHasher, db)
    val sessionRepository: SessionRepository = DataStoreSessionRepository(context)
}
// HomlyDatabase живе у core/data/ і реєструє entity з усіх features:
// @Database(entities = [UserEntity::class /*, TodoEntity::class, ... */], version = 1)
```

Кожен ViewModel отримує залежності через `ViewModelProvider.Factory`. Перехід на Hilt — коли з'являться 3+ features.

---

## G. Навігація

Додаємо `androidx.navigation:navigation-compose`.

```
NavHost:
  "auth" (nested graph):
    "auth/login"     ← startDestination якщо сесії немає
    "auth/register"
  "home"             ← startDestination якщо сесія є
```

- `MainActivity` читає перший емішн `sessionRepository.currentUserId`, показує splash до готовності, потім монтує NavHost.
- Успішна реєстрація/логін → `navigate("home") { popUpTo("auth") { inclusive = true } }`.
- Logout → `navigate("auth/login") { popUpTo("home") { inclusive = true } }`.

**Поведінка після реєстрації**: авто-логін → одразу HomeScreen (менше тертя, відповідає вимозі).

---

## H. Screens

### LoginScreen
- Поля: `login` (TextField), `password` (TextField + PasswordVisualTransformation, IME Done = submit)
- Кнопки: `Login` (disabled поки поля пусті/короткі), `Register` (TextButton → `auth/register`)
- Помилка: `Text(state.errorMessage)` під формою
- Loading: `CircularProgressIndicator`, кнопки disabled

### RegisterScreen
- Та сама форма (login + password), кнопка `Register`, посилання `Login`
- Без поля підтвердження пароля (мінімальний UI)

### HomeScreen
- `Text("Hello $login")` по центру
- `Button("Log out")` → LogoutUseCase → clear session → nav to login

---

## I. Валідація

| Поле | Правило | AuthError |
|---|---|---|
| login | непустий після `trim()` | `EmptyLogin` |
| login | `length >= 3` | `LoginTooShort` |
| login | тільки латиниця та цифри: `^[a-zA-Z0-9]+$` | `InvalidLoginChars` |
| password | непустий (без trim — пробіли є частиною пароля) | `EmptyPassword` |
| password | `length >= 4` | `PasswordTooShort` |
| password | тільки латиниця, цифри та спецзнаки; кирилиця заборонена: `^[a-zA-Z0-9!@#$%^&*()\-_=+\[\]{};:'",.<>?/\\| ]+$` | `InvalidPasswordChars` |

Логін тримується і приводиться до lowercase перед збереженням і пошуком. Пароль — ніколи не тримується.  
Валідація — у ViewModel (швидкий feedback); UseCase/Repository дублює (defensive).

---

## J. Файли до зміни / створення

### Build files
- `gradle/libs.versions.toml` — додати: `room`, `room-compiler` (KSP), `datastore-preferences`, `navigation-compose`, `kotlinx-coroutines-test`, `room-testing`; плагін `ksp`
- `build.gradle.kts` (root) — оголосити KSP plugin `apply false`
- `app/build.gradle.kts` — apply KSP; додати всі нові deps; `buildFeatures { buildConfig = true }`; `buildConfigField("String", "AUTH_BACKEND", "\"LOCAL\"")`

### Manifest
- `AndroidManifest.xml` — `android:name=".HomlyApplication"`; **`android:allowBackup="false"`** (паролі не потрапляють у Google Backup)

### Нові файли (усі шляхи від `app/src/main/java/com/dgero/homly/`)
- `HomlyApplication.kt`
- `auth/domain/model/User.kt`, `AuthError.kt`
- `auth/domain/repository/UserRepository.kt`, `SessionRepository.kt`
- `auth/domain/usecase/RegisterUserUseCase.kt`, `LoginUserUseCase.kt`, `LogoutUseCase.kt`, `ObserveSessionUseCase.kt`
- `auth/data/local/UserEntity.kt`, `UserDao.kt`
- `core/data/HomlyDatabase.kt`
- `auth/data/session/DataStoreSessionRepository.kt`
- `auth/data/crypto/PasswordHasher.kt`
- `auth/data/repository/LocalUserRepository.kt`
- `auth/presentation/login/LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt`
- `auth/presentation/register/RegisterUiState.kt`, `RegisterViewModel.kt`, `RegisterScreen.kt`
- `auth/presentation/AuthNavGraph.kt`
- `home/presentation/HomeViewModel.kt`, `HomeScreen.kt`

### Зміни
- `MainActivity.kt` — замінити `HelloScreen` на `AuthGate` + `NavHost`
- `hello/` — **видалити** весь пакет

---

## K. Тести

### Unit (JVM)
- `Pbkdf2PasswordHasherTest` — hash/verify roundtrip, wrong-password false, different-hash-same-password
- `LocalUserRepositoryTest` (fake DAO) — register happy path, duplicate (login+pass) rejected, same login different pass accepted, login correct, login wrong password, login unknown user
- `LoginViewModelTest`, `RegisterViewModelTest` — валідація помилок, навігаційні ефекти

### Instrumented
- `UserDaoTest` (in-memory Room) — insert + findByLogin, findById

### Manual checklist
1. Холодний старт → LoginScreen
2. Register `alex / passA` → HomeScreen `Hello alex`
3. Logout → LoginScreen
4. Register `alex / passB` → успіх (той самий логін, інший пароль)
5. Login `alex / passA` → успіх; Login `alex / passB` → успіх
6. Login `alex / wrong` → помилка `"Wrong login or password"`
7. Register `alex / passA` вдруге → помилка `"Account already exists"`
8. Вбити застосунок залогіненим → перезапуск → одразу HomeScreen (DataStore session)
9. Logout → перезапуск → LoginScreen
10. Register `"  bob  " / "pw"` → login з `"bob" / "pw"` → успіх (trim login)

---

## M. Ітерації реалізації

### M.0 — Execution Pipeline

Кожна ітерація виконується за циклом:

1. **Iter subagent** — реалізує ітерацію, завершує `./gradlew build`,
   повертає summary + `BUILD SUCCESSFUL` або `BUILD FAILED + причина`.

2. **Якщо BUILD FAILED**:
   - Записати проблему в `auth-changelog` у блок поточної ітерації.
   - Spawn fix subagent → повторити з кроку 1.

3. **Reviewer subagent** — читає написані файли + вимоги ітерації з `auth-plan.md`,
   повертає `✅ approved` або `❌ [конкретні проблеми]`.

4. **Якщо ❌**:
   - Записати проблеми в `auth-changelog` як "Post-review findings".
   - Spawn fix subagent → повторити з кроку 1.

5. **Якщо ✅**:
   - Записати підсумок в `auth-changelog`.
   - Зробити коміт: `feat(auth): iteration N — <опис>`.
   - Перейти до наступної ітерації.

Цикл продовжується поки всі ітерації не апрувнені ревьювером.

---

### Ітерація 1 — Build & Config
**Мета**: проект компілюється з новими залежностями, нічого не зламано.

Файли:
- `gradle/libs.versions.toml` — room, room-compiler (KSP), datastore-preferences, navigation-compose, kotlinx-coroutines-test, room-testing; плагін ksp
- `build.gradle.kts` (root) — KSP plugin `apply false`
- `app/build.gradle.kts` — apply KSP; всі нові deps; `buildConfig = true`; `AUTH_BACKEND = "LOCAL"`
- `AndroidManifest.xml` — `android:name=".HomlyApplication"`; `android:allowBackup="false"`

Перевірка: `./gradlew build` без помилок.

---

### Ітерація 2 — Domain Layer
**Мета**: бізнес-логіка без жодної залежності від Android/Room/DataStore.

Файли:
- `auth/domain/model/User.kt`, `AuthError.kt`
- `auth/domain/repository/UserRepository.kt`, `SessionRepository.kt`
- `auth/domain/usecase/RegisterUserUseCase.kt`, `LoginUserUseCase.kt`, `LogoutUseCase.kt`, `ObserveSessionUseCase.kt`

Перевірка: `./gradlew test` (поки що порожньо, але компілюється).

---

### Ітерація 3 — Data Layer
**Мета**: персистентність і крипто; UseCase-и тепер реально працюють.

Файли:
- `auth/data/crypto/PasswordHasher.kt` (interface + `Pbkdf2PasswordHasher`)
- `auth/data/local/UserEntity.kt`, `UserDao.kt`
- `core/data/HomlyDatabase.kt`
- `auth/data/session/DataStoreSessionRepository.kt`
- `auth/data/repository/LocalUserRepository.kt`
- `HomlyApplication.kt` + `AppContainer`

Перевірка: unit-тести `Pbkdf2PasswordHasherTest`, `LocalUserRepositoryTest` (fake DAO); instrumented `UserDaoTest`.

---

### Ітерація 4 — Presentation & Navigation
**Мета**: повний UI-флоу LoginScreen → RegisterScreen → HomeScreen.

Файли:
- `auth/presentation/login/LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt`
- `auth/presentation/register/RegisterUiState.kt`, `RegisterViewModel.kt`, `RegisterScreen.kt`
- `auth/presentation/AuthNavGraph.kt`
- `home/presentation/HomeViewModel.kt`, `HomeScreen.kt`
- `MainActivity.kt` — замінити `HelloScreen` на `AuthGate` + `NavHost`
- Видалити пакет `hello/`

Перевірка: `LoginViewModelTest`, `RegisterViewModelTest`; ручний чеклист K.

---

### Ітерація 5 — Review & Hardening
**Мета**: код відповідає архітектурі, тести зелені, build чистий.

Дії:
- `./gradlew clean && ./gradlew build`
- Всі unit-тести зелені: `./gradlew test`
- Ручний чеклист K (пункти 1–10)
- Code review: перевірити транзакції, валідацію, nav-стек, security (allowBackup, trim login)

---

## L. Відкриті питання / ризики

| # | Питання | Рішення |
|---|---|---|
| 1 | Хешування чи plaintext? | ✅ **PBKDF2-HmacSHA256** — рішення прийнято. |
| 2 | Backup паролів на Google | ✅ `android:allowBackup="false"` у `AndroidManifest.xml` |
| 3 | Race condition при Register | ✅ `db.withTransaction { check; insert }` у `LocalUserRepository.register()` |
| 4 | `kotlin.Result` у suspend | ✅ Залишаємо `kotlin.Result` |
| 5 | HomlyDatabase у auth/ | ✅ Одразу у `core/data/HomlyDatabase.kt`; entity-класи лишаються у своїх feature-пакетах |
| 6 | "Same login" UX | ✅ Залишаємо як є; ідентифікатор — `id` у БД |
