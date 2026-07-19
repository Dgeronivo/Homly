# Story 005 — Todo: фільтр "Active only" + масове видалення виконаного

*Дата створення: 2026-07-19*

## Вимога

Джерело: [designImprovements.md](designImprovements.md), розділ 5 (Список справ), Варіант B:

> Варіант B — Приховати виконані / фільтр "Active only". Toggle або чіп, що ховає задачі з `isDone = true`.
> **Складність: низька** (чистий UI-фільтр над уже завантаженим списком).

Плюс додаткова вимога від власника продукту, не описана в `designImprovements.md`: **масове видалення всіх виконаних елементів** одною кнопкою з підтвердженням.

У цій story реалізуємо для екрана Todo list рівно два пов'язані покращення:
1. Фільтр, що приховує виконані задачі зі списку.
2. Кнопку, що видаляє всі виконані задачі одразу (після підтвердження).

Варіант A (сортування Date/A–Z) і Варіант C (групування виконано/невиконано) із того ж розділу — не в цій story.

---

## Дизайн

Зараз екран Todo list (`TodoListScreen.kt`) складається з: `TopAppBar` → рядок додавання елемента (`AddItemRow`) → список елементів або порожній стан (`EmptyState`). Кожен рядок списку має чекбокс (виконано/ні), назву (закреслену, якщо виконано) і кнопку видалення (✕).

Додається новий рядок керування списком одразу під `AddItemRow`, до списку елементів:

### Фільтр "Active only"
`FilterChip` з написом "Active only".
- Вимкнений (за замовчуванням) — показує всі елементи, як зараз.
- Увімкнений — зі списку зникають елементи з `isDone = true`; чекбокс, редагування і видалення решти елементів працюють як і раніше.
- Перемикання фільтра — суто відображення, дані в БД не змінюються.

### Кнопка "Очистити виконані"
Розміщена в тому ж рядку, що й `FilterChip`.
- Неактивна (`enabled = false`), якщо серед елементів немає жодного виконаного — незалежно від того, увімкнений фільтр чи ні.
- Клік відкриває діалог підтвердження: "Delete {n} completed tasks?" з кнопками **Delete** / **Cancel**.
- Підтвердження видаляє одразу всі виконані елементи одним запитом до БД (не по одному через існуючий `DeleteTodoItemUseCase` в циклі) і одразу прибирає їх зі списку на екрані.
- Скасування (Cancel) закриває діалог без жодних змін.

---

## Зміни по файлах

### `todolist/data/local/TodoItemDao.kt`
- Новий метод:
  ```kotlin
  @Query("DELETE FROM todo_items WHERE userId = :userId AND isDone = 1")
  suspend fun deleteCompleted(userId: Long): Int
  ```
  Повертає кількість видалених рядків.

### `todolist/domain/repository/TodoRepository.kt`
- Новий метод: `suspend fun deleteCompleted(userId: Long): Result<Int>`.

### `todolist/data/repository/LocalTodoRepository.kt`
- Реалізація `deleteCompleted` — виклик `dao.deleteCompleted(userId)`, обгорнутий у `Result` (аналогічно іншим методам репозиторію).

### `todolist/domain/usecase/DeleteCompletedTodoItemsUseCase.kt` (новий файл)
```kotlin
class DeleteCompletedTodoItemsUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(userId: Long): Result<Int> = repository.deleteCompleted(userId)
}
```

### `todolist/presentation/TodoListUiState.kt`
- Новий поле: `showActiveOnly: Boolean = false`.
- `items` у стані й надалі містить повний список — фільтрація для відображення відбувається у `TodoListViewModel` при побудові `uiState`, щоб `_items` (внутрішній стан) завжди тримав повний список для коректного підрахунку "скільки виконано".

### `todolist/presentation/TodoListViewModel.kt`
- Конструктор отримує нову залежність `deleteCompletedItems: DeleteCompletedTodoItemsUseCase`.
- Новий `MutableStateFlow<Boolean>` `_showActiveOnly`.
- `uiState` combine розширюється: `items = if (showActiveOnly) currentItems.filterNot { it.isDone } else currentItems`.
- Новий метод `onToggleActiveOnly()` — інвертує `_showActiveOnly.value`.
- Новий метод `onClearCompleted()`:
  ```kotlin
  fun onClearCompleted() {
      val uid = userId ?: return
      viewModelScope.launch {
          deleteCompletedItems(uid).onSuccess {
              _items.value = _items.value.filterNot { it.isDone }
          }
      }
  }
  ```
- `Factory` — додати новий параметр конструктора.

### `todolist/presentation/TodoListScreen.kt`
- Додати `FilterChip` "Active only" (стан з `uiState.showActiveOnly`, `onClick = onToggleActiveOnly`).
- Додати кнопку "Очистити виконані" з `enabled = uiState.items.any { it.isDone }` (зверху над списком, в одному ряду з чіпом фільтра).
- Локальний стан `showClearConfirm by remember { mutableStateOf(false) }` + `AlertDialog` підтвердження на кліку кнопки.
- Оновити preview-и під нові параметри.

### `MainActivity.kt`
- У `TodoListViewModel.Factory(...)` додати `deleteCompletedItems = DeleteCompletedTodoItemsUseCase(container.todoRepository)`.

### `app/src/test/java/com/dgero/homly/todolist/fake/FakeTodoRepository.kt`
- Додати реалізацію `deleteCompleted(userId)` для юніт-тестів.

---

## Тести

### AndroidTest — доповнення `TodoItemDaoTest.kt`

| Тест | Перевіряє |
|---|---|
| `deleteCompleted_removesOnlyDoneItemsForThatUser` | видаляються лише `isDone = true`, чужі items не чіпаються |
| `deleteCompleted_noCompletedItems_returnsZeroAndDeletesNothing` | немає виконаних — 0 рядків видалено |

### AndroidTest — доповнення `LocalTodoRepositoryTest.kt`

| Тест | Перевіряє |
|---|---|
| `deleteCompleted_isScopedPerUser` | видаляє виконані лише для свого userId |

### Unit test — новий `DeleteCompletedTodoItemsUseCaseTest.kt`

| Тест | Перевіряє |
|---|---|
| `invoke_delegatesToRepository_andReturnsCount` | проксі-виклик до репозиторію |

### Unit test — доповнення `TodoListViewModelTest.kt`

| Тест | Перевіряє |
|---|---|
| `onToggleActiveOnly_hidesCompletedItemsInUiState` | фільтр приховує виконані в `uiState.items` |
| `onToggleActiveOnly_toggledOff_showsAllItemsAgain` | повернення до повного списку |
| `onClearCompleted_removesAllDoneItemsFromState` | усі виконані зникають з `_items` після успіху |
| `onClearCompleted_noCompletedItems_noOpSafely` | виклик без виконаних елементів не падає |

---

## Definition of done

- [ ] Чіп "Active only" приховує/показує виконані елементи без перезапиту з БД
- [ ] Кнопка "Очистити виконані" неактивна, коли виконаних елементів немає
- [ ] Клік по "Очистити виконані" вимагає підтвердження і видаляє всі виконані одним запитом
- [ ] `TodoRepository`/`LocalTodoRepository`/`TodoItemDao` мають новий метод `deleteCompleted`, покритий тестами
- [ ] `make rebuild` — зелений
- [ ] `make test` — зелений (`DeleteCompletedTodoItemsUseCaseTest`, `TodoListViewModelTest`)
- [ ] `make connected-test` — зелений (`TodoItemDaoTest`, `LocalTodoRepositoryTest`)
- [ ] Усі зміни сторі закомічені одним комітом
