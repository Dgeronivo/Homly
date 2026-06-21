---
iteration: 3
title: "Presentation + Navigation"
status: todo
depends_on: story-002
ac_coverage: [AC-01, AC-02, AC-03, AC-04, AC-05, AC-06, AC-07, AC-08, AC-09, AC-10, AC-11, AC-12]
---

# Story 003 — Presentation + Navigation

## Мета

Реалізувати Compose-екран, під'єднати його до NavGraph та HomeScreen. Після цієї ітерації todo-list повністю функціональний у застосунку.

---

## Нові файли

### `todolist/presentation/TodoListScreen.kt`

Структура аналогічна `ShoppingListScreen`. Дві composable-функції — `TodoListScreen(viewModel, onBack)` та приватна `TodoListContent(uiState, callbacks…)`.

**Компоненти всередині файлу:**

- `TodoListContent` — `Scaffold` з `TopAppBar` ("Todo list", кнопка назад) + `Column` з `AddItemRow` та `LazyColumn` / `EmptyState`.
- `AddItemRow` — `OutlinedTextField` + кнопка "Add". Поле вимкнено при `isLimitReached`; `supportingText` показує `errorMessage` або "List is full (max 50 items)".
- `TodoItemRow` — `Checkbox` (toggle isDone) + назва item + кнопка видалення. **Назва відображається з `TextDecoration.LineThrough` при `isDone = true`** (аналогічно до shopping). Тап на назву переводить рядок в режим редагування (`OutlinedTextField`); стан `isEditing` і `editText` — `rememberSaveable(item.id)`.
- `EmptyState` — центрований текст "Your todo list is empty".

**Відмінності від `ShoppingListScreen`:**
- Немає `SortSelector` — сортування фіксоване на рівні DAO.
- `Checkbox` використовує `isDone` (замість `isBought`).

**Previews:** `TodoListContentPreview` (зі списком) + `TodoListEmptyPreview`.

---

## Змінювані файли

### `MainActivity.kt`
Додати маршрут `"todo"` у `NavHost`:
- Створити `TodoListViewModel` через `Factory` з use cases на основі `container.todoRepository` та `container.sessionRepository`.
- Відрендерити `TodoListScreen(vm, onBack = { navController.popBackStack() })`.
- Передати `onOpenTodoList = { navController.navigate("todo") }` у `HomeScreen`.

### `home/presentation/HomeScreen.kt`
- Додати параметр `onOpenTodoList: () -> Unit` до `HomeScreen` і `HomeContent`.
- Додати кнопку "Todo list" аналогічно до кнопки "Shopping list".
- Оновити `@Preview` — передати `onOpenTodoList = {}`.

---

## Тести

Нових unit/androidTest у цій ітерації немає — логіка покрита в story-001 та story-002.

Ручна перевірка перед закриттям ітерації:

| Сценарій | Очікуваний результат | AC |
|---|---|---|
| Відкрити Todo list з HomeScreen | Список відкривається, порожній стан | AC-12 |
| Додати item — з'являється у списку зверху | Item видно, checkbox порожній | AC-01, AC-03 |
| Позначити item done | Checkbox відмічено, **назва закреслена** | AC-02 |
| Знову відкрити список | Done items внизу, не-done вгорі | AC-03 |
| Зняти позначку done | Item повертається вгору, закреслення зникає | AC-06 |
| Тапнути на назву → відредагувати → Enter | Назва оновлюється | AC-04 |
| Зберегти порожню назву | Помилка "Name cannot be empty" | AC-09 |
| Натиснути ✕ | Item видалено зі списку | AC-05 |
| Спробувати додати порожню назву | Помилка "Name cannot be empty" | AC-07 |
| Поле вимкнено при 50 items | Повідомлення "List is full (max 50 items)" | AC-11 |

---

## Definition of done

- [ ] `TodoListScreen` має `@Preview` (дві preview-функції без параметрів)
- [ ] HomeScreen має кнопку "Todo list"
- [ ] Маршрут `"todo"` зареєстровано в `MainActivity`
- [ ] `make rebuild` — зелений
- [ ] Ручна перевірка всіх сценаріїв пройдена
