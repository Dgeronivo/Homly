---
iteration: 2
title: "Use Cases + ViewModel"
status: todo
depends_on: story-001
ac_coverage: [AC-01, AC-02, AC-03, AC-04, AC-05, AC-06, AC-07, AC-08, AC-09, AC-10, AC-11]
---

# Story 002 — Use Cases + ViewModel

## Мета

Реалізувати всі use cases і ViewModel із `StateFlow<TodoListUiState>`. Після цієї ітерації вся бізнес-логіка покрита unit-тестами; UI немає.

---

## Нові файли

### `todolist/domain/usecase/ObserveTodoItemsUseCase.kt`
Викликає `TodoRepository.getItems(userId)` і застосовує фіксоване сортування в пам'яті:
- Не-виконані (`isDone = false`) першими.
- Всередині кожної групи — новіші за `createdAt` першими.

Компаратор: `compareBy<TodoItem> { it.isDone }.thenByDescending { it.createdAt }`.

Сортування в UseCase (не в DAO/SQL) — щоб при заміні `LocalTodoRepository` на Remote-реалізацію поведінка залишалась незмінною.

### `todolist/domain/usecase/AddTodoItemUseCase.kt`
Приймає `TodoTitleValidator` через конструктор. Викликає `validator.validate(title)` перед збереженням: повертає помилку якщо є. При успішній валідації делегує `TodoRepository.add(userId, title.trim())`. Патерн: аналог `AddShoppingItemUseCase`.

### `todolist/domain/usecase/EditTodoItemUseCase.kt`
Приймає `TodoTitleValidator` через конструктор. Ті самі кроки: `validator.validate(title)` → якщо ОК → `TodoRepository.editTitle(id, userId, title.trim())`. Відмінність від shopping: передає `userId` (потрібно для Unauthorized).

### `todolist/domain/usecase/ToggleTodoItemUseCase.kt`
Делегує `TodoRepository.toggleDone(id, userId, isDone)` без додаткової логіки.

### `todolist/domain/usecase/DeleteTodoItemUseCase.kt`
Делегує `TodoRepository.delete(id, userId)` без додаткової логіки.

### `todolist/presentation/TodoListUiState.kt`
Data class з двома рівнями помилок — патерн з auth-inline-validation-plan:
- `items: List<TodoItem> = emptyList()`
- `newItemTitle: String = ""`
- `isLimitReached: Boolean = false`
- `titleError: String?` — field-level помилка, показується під полем введення: TitleTooLong (inline при вводі) або EmptyTitle (при спробі зберегти).
- `formError: String?` — form-level помилка, показується окремо від поля: LimitReached або Unknown.

Немає поля `sortOrder` — сортування фіксоване в UseCase. Немає єдиного `errorMessage` — замінено двома окремими полями.

### `todolist/presentation/TodoListViewModel.kt`
Приймає `TodoTitleValidator` через конструктор (поруч з use cases і `SessionRepository`).

Аналог `ShoppingListViewModel` з відмінностями:
- Немає `sortOrder`.
- `currentUserId: StateFlow<Long?>` з `SessionRepository`, `stateIn(WhileSubscribed(5_000))`.
- `items: Flow` — `flatMapLatest` по `currentUserId` → `ObserveTodoItemsUseCase(userId)`.
- `uiState` — `combine(items, newItemTitle, titleError, formError)` → `TodoListUiState`.

Публічні методи:

**`onNewItemTitleChange(value)`** — оновлює `newItemTitle`; викликає `validator.validateMaxLength(value)` → якщо не null → `titleError = "Name is too long (max 100 characters)"`; якщо null і `titleError` був TitleTooLong → очищає `titleError`. `formError` не чіпає.

**`onAdd()`** — читає `currentUserId.value`; запускає `AddTodoItemUseCase`:
- success → очищає `newItemTitle` і `titleError`.
- failure `EmptyTitle` → `titleError = "Name cannot be empty"`.
- failure `TitleTooLong` → `titleError = "Name is too long (max 100 characters)"`.
- failure `LimitReached` → `formError = "List is full (max 50 items)"` (хоча UI вже блокує поле через `isLimitReached`).
- failure `Unknown` → `formError = "Something went wrong"`.

**`onToggle(item)`** — запускає `ToggleTodoItemUseCase(item.id, userId, !item.isDone)`; `Unauthorized` мовчки ігнорується (AC-10).

**`onEdit(id, newTitle)`** — запускає `EditTodoItemUseCase`; `Unauthorized` мовчки ігнорується (AC-10); `EmptyTitle` / `TitleTooLong` → `titleError`; `Unknown` → `formError`.

**`onDelete(id)`** — запускає `DeleteTodoItemUseCase`; `Unauthorized` мовчки ігнорується.

Вкладений `class Factory` аналогічний `ShoppingListViewModel.Factory`; приймає `TodoTitleValidator` і передає у конструктор ViewModel.

---

## Тести

### Unit test — `FakeTodoRepository.kt` (у `test/`)
In-memory реалізація `TodoRepository`. Зберігає items у `MutableStateFlow<Map<Long, TodoItem>>`. Відстежує `owners: Map<itemId, userId>`. Методи:
- `getItems(userId)` — фільтрує по `owners`, повертає Flow без сортування (сортування тестується окремо в `ObserveTodoItemsUseCaseTest`).
- `add` — перевіряє ліміт по `owners`; ≥ MAX_ITEMS → `LimitReached`.
- `editTitle / toggleDone / delete` — перевіряють `owners[id] == userId`; якщо ні → `Unauthorized`.

### Unit tests — UseCases

**`ObserveTodoItemsUseCaseTest`**

| Тест | AC |
|---|---|
| `notDoneItems_comeBeforeDoneItems` | AC-03 |
| `withinNotDone_newerCreatedAtFirst` | AC-03 |
| `withinDone_newerCreatedAtFirst` | AC-03 |

**`AddTodoItemUseCaseTest`**

| Тест | AC |
|---|---|
| `blankTitle_returnsEmptyTitle` | AC-07 |
| `titleTooLong_returnsTitleTooLong` | AC-08 |
| `titleAtMaxLength_succeeds` | AC-01 |
| `atLimit_returnsLimitReached` | AC-11 |
| `validTitle_isTrimmedAndStored` | AC-01 |

**`EditTodoItemUseCaseTest`**

| Тест | AC |
|---|---|
| `blankTitle_returnsEmptyTitle` | AC-09 |
| `titleTooLong_returnsTitleTooLong` | — |
| `validTitle_updatesItem` | AC-04 |
| `wrongUser_returnsUnauthorized` | AC-10 |

**`ToggleTodoItemUseCaseTest`**

| Тест | AC |
|---|---|
| `correctUser_togglesDone` | AC-02, AC-06 |
| `wrongUser_returnsUnauthorized` | AC-10 |

**`DeleteTodoItemUseCaseTest`**

| Тест | AC |
|---|---|
| `correctUser_deletesItem` | AC-05 |
| `wrongUser_returnsUnauthorized` | AC-10 |

### Unit test — `TodoListViewModelTest`
Патерн: `StandardTestDispatcher`, `Dispatchers.setMain/resetMain`, `backgroundScope.launch { uiState.collect {} }`, `advanceUntilIdle()`.

| Тест | AC |
|---|---|
| `noUser_itemsAreEmpty` | AC-12 |
| `items_areScopedToCurrentUser` | AC-03 |
| `userSwitch_doesNotLeakPreviousUsersItems` | AC-10 |
| `isLimitReached_true_whenAt50Items` | AC-11 |
| `onNewItemTitleChange_tooLong_setsTitleError` | AC-08 |
| `onNewItemTitleChange_withinLimit_clearsTitleError` | — |
| `onAdd_blankTitle_setsTitleError` | AC-07 |
| `onAdd_atLimit_setsFormError` | AC-11 |
| `onAdd_success_clearsInputAndErrors` | AC-01 |
| `onEdit_wrongUser_noErrorSet` | AC-10 |
| `onToggle_wrongUser_noErrorSet` | AC-10 |

---

## Definition of done

- [ ] Усі файли у пакеті `com.dgero.homly.todolist.*`
- [ ] `make rebuild` — зелений
- [ ] `make test` — всі нові unit-тести зелені
