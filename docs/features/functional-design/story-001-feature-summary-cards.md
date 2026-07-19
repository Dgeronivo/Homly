# Story 001 — Картки фіч зі зведенням + підтвердження виходу

*Дата створення: 2026-07-19*

## Вимога

Джерело: [functional-design/designImprovements.md](designImprovements.md), розділ 1 (Home).

> **Рекомендація:** A (або A+D разом) — найбільший приріст функціональності Home за помірні зусилля, не чіпає бізнес-логіку фіч, лише додає читання агрегованих даних.

Реалізуємо обидва варіанти разом:
- **A** — три кнопки-рядки на Home замінюються картками з коротким зведенням стану кожної фічі (кількість подій сьогодні / активних покупок / незавершених справ).
- **D** — кнопка Log out більше не виходить миттєво, а показує діалог підтвердження.

Бізнес-логіка фіч (Calendar/Shopping/Todo) не змінюється — Home лише читає вже наявні дані через існуючі use cases.

---

## Дизайн екрана

Замість трьох `Button` одна під одною — три картки (`FeatureCard`), кожна клікабельна (веде на відповідну фічу), з назвою і summary-рядком:

| Картка | Summary (є дані) | Summary (порожньо) |
|---|---|---|
| Calendar | "Today: {n} events" | "No events today" |
| Shopping list | "{n} items left to buy" | "Shopping list is empty" |
| Todo list | "{n} tasks pending" | "All tasks done" |

Кнопка **Log out** лишається внизу; клік відкриває `AlertDialog` ("Log out?" / Log out / Cancel) замість негайного виходу.

---

## Зміни по файлах

### `home/presentation/HomeViewModel.kt`
- Конструктор отримує 3 нові залежності: `getEventsUseCase: GetEventsUseCase`, `observeShoppingItems: ObserveShoppingItemsUseCase`, `getTodoItems: GetTodoItemsUseCase`.
- Нові `StateFlow`:
  - `todayEventsCount: StateFlow<Int>` — рахує через `GetEventsUseCase(userId, YearMonth.now())` + `GetEventsUseCase.forDay(events, LocalDate.now()).size`. Suspend-джерело (як і в Calendar) → потребує ручного `refresh()`.
  - `shoppingActiveCount: StateFlow<Int>` — `observeShoppingItems(userId, ShoppingSortOrder.DATE_DESC).map { items -> items.count { !it.isBought } } .stateIn(...)`. Реактивний Flow, оновлюється сам.
  - `todoPendingCount: StateFlow<Int>` — `getTodoItems(userId).count { !it.isDone }`. Suspend-джерело → потребує `refresh()`.
- Новий метод `refresh()` — перезапускає завантаження `todayEventsCount` і `todoPendingCount` (аналогічно `CalendarViewModel.refresh()`); викликається при поверненні на Home.
- `Factory` — додати 3 нові параметри конструктора.

### `home/presentation/HomeScreen.kt`
- Додати `DisposableEffect` + `LifecycleEventObserver` на `ON_RESUME`, що викликає `viewModel.refresh()` — той самий патерн, що вже є в `CalendarScreen.kt` (рядки 98–106), потрібен тому що Home повертається з екранів фіч після змін даних.
- `HomeContent`: замінити 3 `Button` на 3 виклики нового приватного composable-компонента `FeatureCard` (заголовок + summary-текст, `Modifier.clickable`) — не окрема сутність/модель, а звичайна UI-функція в тому ж файлі, аналогічно наявним приватним composable типу `DayCell`/`EventRow` у `CalendarScreen.kt`:
  ```kotlin
  @Composable
  private fun FeatureCard(
      title: String,
      summary: String,
      onClick: () -> Unit,
  ) {
      Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
          Column(Modifier.padding(16.dp)) {
              Text(title, style = MaterialTheme.typography.titleMedium)
              Text(summary, style = MaterialTheme.typography.bodyMedium)
          }
      }
  }
  ```
  Викликається тричі в `HomeContent` з різними `title`/`summary`/`onClick`; нових файлів, класів чи доменних моделей для неї не заводиться.
- Локальний стан `showLogoutConfirm by remember { mutableStateOf(false) }`; кнопка Log out відкриває `AlertDialog` замість прямого виклику `onLogout`.
- Оновити preview-и (`HomeContentPreview` тощо) під нові параметри `uiState`.

### `MainActivity.kt`
- У `HomeViewModel.Factory(...)` додати:
  ```kotlin
  getEventsUseCase = GetEventsUseCase(container.calendarEventRepository),
  observeShoppingItems = ObserveShoppingItemsUseCase(container.shoppingRepository),
  getTodoItems = GetTodoItemsUseCase(container.todoRepository),
  ```

**Нових use case, доменних моделей чи Room-змін не потрібно** — усі три use case вже існують і використовуються іншими фічами.

---

## Тести

### Unit test — `HomeViewModelTest` (новий, у `test/`)

| Тест | Перевіряє |
|---|---|
| `todayEventsCount_countsOnlyTodayEvents` | події на інші дні місяця не враховуються |
| `todayEventsCount_zeroWhenNoEventsToday` | порожній стан |
| `shoppingActiveCount_countsOnlyNotBoughtItems` | куплені товари не враховуються |
| `shoppingActiveCount_updatesReactivelyOnItemChange` | Flow з репозиторію оновлює count без виклику `refresh()` |
| `todoPendingCount_countsOnlyNotDoneItems` | виконані задачі не враховуються |
| `refresh_reloadsEventsAndTodoCounts` | після `refresh()` нові suspend-дані підхоплюються |
| `onLogout_stillCallsLogoutUseCaseAndOnDone` | існуюча поведінка виходу не зламана |

Інструментальні тести не потрібні — змін у Room/DAO немає.

---

## Definition of done

- [ ] Home показує 3 картки з актуальним зведенням замість голих кнопок
- [ ] Дані на картках оновлюються при поверненні на Home після дій в Calendar/Shopping/Todo
- [ ] Log out вимагає підтвердження діалогом
- [ ] Навігаційні колбеки (`onOpenShoppingList`, `onOpenTodoList`, `onOpenCalendar`) не змінили сигнатуру
- [ ] `make rebuild` — зелений
- [ ] `make test` — `HomeViewModelTest` зелений
- [ ] Усі зміни сторі закомічені одним комітом
