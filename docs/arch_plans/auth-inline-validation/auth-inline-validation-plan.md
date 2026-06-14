# Auth — Inline Field Validation Plan

## Проблема

Користувач вводить недозволені символи у поле логіну — символи відображаються, але помилка з'являється лише при натисканні Login. Зворотній зв'язок приходить надто пізно.

### Першопричина

Вся валідація зосереджена у use case і спрацьовує лише при сабміті. `onLoginChange` / `onPasswordChange` зберігають значення без перевірки. `isFormValid` у Screen перевіряє лише довжину.

### Наслідки

- Користувач дізнається про невалідні символи лише після натискання кнопки.
- `errorMessage` змішує два рівні помилок: field-level (невалідні символи) і auth-level (неправильний пароль).
- Частина помилок у `authErrorMessage` недосяжна через `isFormValid` — мертвий код.

---

## Вимоги

1. **Inline-валідація** — помилка про недозволені символи з'являється одразу при вводі, під відповідним полем.
2. **Підказка формату** — під кожним полем завжди видно очікуваний формат; замінюється на помилку коли є порушення.
3. **Submit-валідація** залишається, синхронізована з inline через спільні validators. Field-помилки показуються під інпутами, auth-помилки — під кнопкою.

---

## Правила валідації

### Login
- Дозволені символи: `[a-zA-Z0-9]` (тільки латиниця і цифри)
- Пробіли: заборонені, включаючи в середині (`"ab cd"` → невалідно)
- Leading/trailing пробіли: обрізаються перед перевіркою
- Мінімальна довжина: 3 символи

### Password
- Дозволені символи: латиниця, цифри, спецсимволи (`!@#$%^&*` тощо)
- Пробіли: дозволені
- Мінімальна довжина: 4 символи

---

## Пріоритети спрацювання

### Inline (на кожен введений символ)

Тільки перевірка невалідних символів — решта агресивна під час набору:

| Перевірка | Спрацьовує | Чому |
|---|---|---|
| Невалідні символи | ✅ одразу | Користувач має знати що клавіатуру треба змінити |
| Занадто коротко | ❌ | Агресивно поки людина ще друкує |
| Порожнє поле | ❌ | Поле порожнє бо ще не почали, а не через помилку |

### Submit (при натисканні кнопки)

Перевірки виконуються послідовно, перша невдала зупиняє ланцюжок:

**Login:** порожній → задовкороткий → невалідні символи  
**Password:** порожній → задовкороткий → невалідні символи  
**Auth:** невірні credentials / невідома помилка

---

## Едж-кейси

| Введення | Trim | Результат |
|---|---|---|
| `"  abc  "` | `"abc"` | валідно, кнопка активна |
| `"  к  "` | `"к"` | невалідний символ → inline помилка |
| `"ab cd"` | `"ab cd"` | пробіл у середині → невалідний символ → inline помилка |
| `"   "` | `""` | порожньо → кнопка задизейблена, помилки немає |
| `""` (очищене) | `""` | кнопка задизейблена, помилки немає (не наганяємо) |
| paste `"abc кир"` | `"abc кир"` | невалідний символ → inline помилка одразу |

Submit з уже встановленим `loginError` / `passwordError` фізично неможливий — `isFormValid` вже `false`, кнопка задизейблена.

---

## Рішення

### Принцип

Валідація залишається на **обох рівнях**:

- **Use case** — захист домену, незалежний від UI.
- **ViewModel** — UX, миттєвий зворотній зв'язок при вводі.

Щоб не дублювати логіку, правила виносяться у спільні класи domain-шару, які використовують обидва рівні.

### Архітектурні зміни

**Domain — нові validator-и:**

Два нових класи `LoginValidator` і `PasswordValidator` у `auth/domain/validation/`. Кожен інкапсулює правила для свого поля і повертає `AuthError?`. Use case-и делегують валідацію їм.

**UiState — три рівні помилок:**

`LoginUiState` і `RegisterUiState` отримують окремі поля:
- `loginError: String?` — field-level помилка логіну, показується під полем
- `passwordError: String?` — field-level помилка пароля, показується під полем
- `authError: String?` — form-level помилка, показується під кнопкою

`errorMessage` видаляється.

**ViewModel — inline-валідація:**

`onLoginChange` і `onPasswordChange` викликають відповідний validator і одразу оновлюють `loginError` / `passwordError`. Кнопка залишається задизейбленою поки є field-помилки.

**Screen — підказка і помилка через `supportingText`:**

`supportingText` під кожним полем: показує підказку формату коли помилок немає, замінює на помилку коли є. `isError` підсвічує поле червоним.

```
Login field:    "Letters and digits only, min 3 characters"  ← підказка
                "Login can only contain letters and digits"   ← помилка (замість підказки)

Password field: "Min 4 characters, letters, digits and special characters"
                "Password contains invalid characters"
```

`authError` відображається під кнопкою окремо.

---

## Зачеплені файли

| Файл | Зміна |
|---|---|
| `auth/domain/validation/LoginValidator.kt` | новий — інкапсулює правила логіну: empty, length, chars; повертає `AuthError?` |
| `auth/domain/validation/PasswordValidator.kt` | новий — інкапсулює правила пароля: empty, length, chars; повертає `AuthError?` |
| `auth/domain/usecase/LoginUserUseCase.kt` | видалити inline `validate()`; прийняти `LoginValidator` і `PasswordValidator` через конструктор; делегувати їм |
| `auth/domain/usecase/RegisterUserUseCase.kt` | видалити inline `validate()`; прийняти `LoginValidator` і `PasswordValidator` через конструктор; делегувати їм |
| `auth/presentation/login/LoginUiState.kt` | замінити `errorMessage` на три поля: `loginError`, `passwordError`, `authError` |
| `auth/presentation/register/RegisterUiState.kt` | замінити `errorMessage` на три поля: `loginError`, `passwordError`, `authError` |
| `auth/presentation/login/LoginViewModel.kt` | прийняти validators через конструктор; `onLoginChange` → викликати `LoginValidator`, оновлювати `loginError`; `onPasswordChange` → викликати `PasswordValidator`, оновлювати `passwordError`; `onLoginClick` → помилки use case писати в `authError`; оновити `Factory` |
| `auth/presentation/register/RegisterViewModel.kt` | прийняти validators через конструктор; `onLoginChange` → викликати `LoginValidator`, оновлювати `loginError`; `onPasswordChange` → викликати `PasswordValidator`, оновлювати `passwordError`; `onRegisterClick` → помилки use case писати в `authError`; оновити `Factory` |
| `auth/presentation/login/LoginScreen.kt` | login `OutlinedTextField`: додати `isError`, `supportingText` (підказка або `loginError`); password `OutlinedTextField`: додати `isError`, `supportingText` (підказка або `passwordError`); `authError` — показувати під кнопкою замість `errorMessage`; `isFormValid` — додати умови `loginError == null && passwordError == null` |
| `auth/presentation/register/RegisterScreen.kt` | login `OutlinedTextField`: додати `isError`, `supportingText` (підказка або `loginError`); password `OutlinedTextField`: додати `isError`, `supportingText` (підказка або `passwordError`); `authError` — показувати під кнопкою замість `errorMessage`; `isFormValid` — додати умови `loginError == null && passwordError == null` |
| `MainActivity.kt` / DI | створювати `LoginValidator` і `PasswordValidator`; передавати у `LoginViewModel.Factory` і `RegisterViewModel.Factory` |

---

## UX після фіксу

```
Вводить "кириліця"        → поле червоне, помилка під ним одразу (підказка замінюється)
Виправляє на "abc"        → поле нормальне, підказка повертається
Вводить "ab cd"           → поле червоне (пробіл у середині заборонений)
Натискає Login з 2 chars  → loginError = "Login must be at least 3 characters"
Невірний пароль           → authError під кнопкою
```
