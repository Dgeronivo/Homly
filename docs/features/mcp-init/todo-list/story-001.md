---
iteration: 1
title: "Domain + Data layer"
status: todo
ac_coverage: [AC-01, AC-02, AC-03, AC-04, AC-05, AC-06, AC-10, AC-11]
---

# Story 001 — Domain + Data layer

## Мета

Реалізувати domain-моделі, sealed-помилки, validator, інтерфейс репозиторію, Room entity + DAO, LocalTodoRepository з атомарним лімітом та Unauthorized-перевіркою. Після цієї ітерації шар зберігання покрито тестами; UI немає.

---

## Нові файли

### `todolist/domain/model/TodoItem.kt`
Data class: `id: Long`, `title: String`, `isDone: Boolean`, `createdAt: Long`.

### `todolist/domain/model/TodoLimits.kt`
Object-константи: `MAX_ITEMS = 50`, `MAX_TITLE_LENGTH = 100`. Дзеркало `ShoppingLimits`.

### `todolist/domain/error/TodoError.kt`
Sealed class із варіантами: `EmptyTitle`, `TitleTooLong`, `LimitReached`, `Unauthorized`, `Unknown(cause)`.
Відмінність від `ShoppingError`: є `Unauthorized` (потрібен для AC-10). `Unknown(cause)` — catch-all для несподіваних `SQLiteException` / `IllegalStateException` з Room.

### `todolist/domain/validation/TodoTitleValidator.kt`
Спільний validator для UseCase і ViewModel — правила не дублюються. Два методи:
- `validateMaxLength(title)` → `TodoError?` — перевіряє тільки `TitleTooLong`; викликається при кожній зміні поля для inline-показу помилки.
- `validate(title)` → `TodoError?` — повна перевірка перед збереженням: `EmptyTitle` якщо blank, `TitleTooLong` якщо перевищує ліміт, інакше null.

Патерн: аналог `LoginValidator` / `PasswordValidator` з auth-inline-validation-plan.

### `todolist/domain/repository/TodoRepository.kt`
Інтерфейс:
- `getItems(userId)` → `Flow<List<TodoItem>>` — без гарантованого порядку; сортування — відповідальність UseCase.
- `add(userId, title)` → `Result<TodoItem>`
- `editTitle(id, userId, title)` → `Result<Unit>`
- `toggleDone(id, userId, isDone)` → `Result<Unit>`
- `delete(id, userId)` → `Result<Unit>`

Мутації приймають `userId` — основа механізму Unauthorized (AC-10). Контракт `getItems` не визначає порядок — дозволяє замінити Local-реалізацію на Remote без переносу сортування.

### `todolist/data/local/TodoItemEntity.kt`
Room `@Entity(tableName = "todo_items")` з `@Index("userId")`. Поля: `id` (autoGenerate), `userId`, `title`, `isDone = false`, `createdAt = System.currentTimeMillis()`.

### `todolist/data/local/TodoItemDao.kt`
- `getByUser(userId)` → `Flow<List<TodoItemEntity>>` — `WHERE userId = :userId` без `ORDER BY`. Сортування в UseCase, а не в SQL — зберігає однакову поведінку при зміні репозиторію.
- `countByUser(userId)` → `Int`
- `insert(entity)` → `Long`
- `updateTitle(id, userId, title)` → `Int` (кількість зачеплених рядків)
- `updateDone(id, userId, isDone)` → `Int`
- `deleteById(id, userId)` → `Int`

Усі мутації фільтрують `WHERE id = :id AND userId = :userId`. Повернення `0` → item не належить цьому user → Unauthorized.

### `todolist/data/repository/LocalTodoRepository.kt`
Реалізує `TodoRepository`:
- `getItems` — повертає Flow без сортування (`dao.getByUser`), маппить entity → domain.
- `add` — `TransactionRunner`: count ≥ MAX_ITEMS → кидає `LimitReached`, інакше вставляє entity.
- `editTitle / toggleDone / delete` — викликають DAO-метод; якщо повернуто `0` рядків → `Result.failure(TodoError.Unauthorized)`; несподіваний `Exception` → `Result.failure(TodoError.Unknown(e))`.

---

## Змінювані файли

### `core/data/HomlyDatabase.kt`
- Додати `TodoItemEntity::class` до `entities`.
- Підняти `version` з `2` до `3`.
- Додати абстрактний метод `todoItemDao(): TodoItemDao`.

> `fallbackToDestructiveMigration` вже налаштований — schema drop відбудеться автоматично.

### `HomlyApplication.kt` (AppContainer)
Додати поле `todoRepository: TodoRepository = LocalTodoRepository(db.todoItemDao(), transactionRunner)`.

---

## Тести

### Unit test — `TodoTitleValidatorTest` (у `test/`)

| Тест | Метод | Перевіряє |
|---|---|---|
| `validateMaxLength_tooLong_returnsTitleTooLong` | `validateMaxLength` | inline TitleTooLong |
| `validateMaxLength_atMaxLength_returnsNull` | `validateMaxLength` | граничне значення |
| `validateMaxLength_blank_returnsNull` | `validateMaxLength` | EmptyTitle не перевіряється inline |
| `validate_blank_returnsEmptyTitle` | `validate` | AC-07, AC-09 |
| `validate_tooLong_returnsTitleTooLong` | `validate` | AC-08 |
| `validate_valid_returnsNull` | `validate` | AC-01 |

### AndroidTest — `TodoItemDaoTest`
In-memory DB (`inMemoryDatabaseBuilder`). Перевіряє:

| Тест | AC |
|---|---|
| `insertAndGet_returnsItemsForUser` | AC-03 |
| `getByUser_doesNotLeakOtherUsersItems` | AC-10 |
| `countByUser_countsOnlyThatUser` | AC-11 |
| `updateTitle_withCorrectUser_returns1` | AC-04 |
| `updateTitle_withWrongUser_returns0_noChange` | AC-10 |
| `updateDone_withCorrectUser_returns1` | AC-02 |
| `updateDone_withWrongUser_returns0` | AC-10 |
| `deleteById_withCorrectUser_returns1` | AC-05 |
| `deleteById_withWrongUser_returns0_noChange` | AC-10 |

### AndroidTest — `LocalTodoRepositoryTest`
In-memory DB + real `TransactionRunner`. Перевіряє:

| Тест | AC |
|---|---|
| `add_isScopedPerUser` | — |
| `add_atLimit_returnsLimitReachedAndDoesNotInsert` | AC-11, QG-1 |
| `add_limitIsPerUser` | AC-11 |
| `editTitle_withCorrectUser_changesTitle` — createdAt/isDone незмінні | AC-04 |
| `editTitle_withWrongUser_returnsUnauthorized` | AC-10 |
| `toggleDone_withCorrectUser_updatesFlag` | AC-02, AC-06 |
| `toggleDone_withWrongUser_returnsUnauthorized` | AC-10 |
| `delete_withCorrectUser_removesItem` | AC-05 |
| `delete_withWrongUser_returnsUnauthorized` — item залишається | AC-10 |

---

## Definition of done

- [x] Усі файли у пакеті `com.dgero.homly.todolist.*`
- [x] `HomlyDatabase` version = 3, entity зареєстрована, DAO-метод присутній
- [x] `AppContainer` має `todoRepository`
- [x] `make rebuild` — зелений
- [x] `make test` — `TodoTitleValidatorTest` зелений
- [x] `make connected-test` — всі нові AndroidTest зелені
