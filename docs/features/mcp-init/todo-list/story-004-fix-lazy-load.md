# Plan — Lazy sort: refactor ObserveTodoItemsUseCase → GetTodoItemsUseCase

## Вимога

SAD §4 стратегічне рішення 3:

> **Lazy sort (deferred reorder on list-open)** — done items переміщуються вниз при відкритті списку, не миттєво при toggle.

## Проблема

`ObserveTodoItemsUseCase` повертає `Flow<List<TodoItem>>`. Room автоматично перевипускає Flow при кожній зміні в таблиці — toggle, edit, add, delete. ViewModel підписується на цей Flow і при кожній емісії замінює UI-стан новим відсортованим списком. В результаті — toggle item done → item миттєво йде вниз. SAD порушено.

Крім того, Flow тут архітектурно зайвий: реактивність не потрібна, але тип `Flow` і назва "Observe" обіцяють її. Це невідповідність між наміром і реалізацією.

## Рішення

Перейменувати `ObserveTodoItemsUseCase` → `GetTodoItemsUseCase` і змінити сигнатуру:

```kotlin
// було
operator fun invoke(userId: Long): Flow<List<TodoItem>>

// стане
suspend operator fun invoke(userId: Long): List<TodoItem>
```

ViewModel завантажує список **один раз** при ініціалізації через `viewModelScope.launch` і зберігає у `MutableStateFlow<List<TodoItem>>`. Усі подальші мутації (toggle, add, edit, delete) оновлюють цей локальний стан напряму після успішного запису в БД:

- **toggle** — оновити `isDone` у відповідного item на місці, без пересортування
- **add** — вставити новий item на початок списку
- **edit** — оновити `title` у відповідного item на місці
- **delete** — видалити item зі списку

При наступному відкритті екрану ViewModel створюється заново → `getItems()` завантажує список з БД вже з актуальним порядком (done внизу).

## Зміни

| Файл | Зміна |
|---|---|
| `TodoItemDao.kt` | `getByUser()` — змінити з `Flow<List<TodoItemEntity>>` на `suspend fun ... : List<TodoItemEntity>` |
| `TodoRepository.kt` | `getItems()` — змінити з `Flow<List<TodoItem>>` на `suspend fun ... : List<TodoItem>` |
| `LocalTodoRepository.kt` | `getItems()` — прибрати Flow.map, залишити тільки `List.map { it.toDomain() }` |
| `ObserveTodoItemsUseCase.kt` → `GetTodoItemsUseCase.kt` | перейменувати клас і файл; змінити return type на `suspend List<TodoItem>` |
| `ObserveTodoItemsUseCaseTest.kt` → `GetTodoItemsUseCaseTest.kt` | перейменувати тест-файл і клас; оновити тести під suspend fun |
| `TodoListViewModel.kt` | замінити Flow-підписку на `launch { getItems() }`; додати локальну мутацію стану після кожної дії |
| `MainActivity.kt` | замінити `ObserveTodoItemsUseCase(...)` на `GetTodoItemsUseCase(...)` |
