# Журнал змін функціонального дизайну

## Story 001 — Картки фіч зі зведенням + підтвердження виходу

**Дата:** 2026-07-19

### Проблеми під час реалізації
- без відхилень від плану

### Рішення
- Реалізовано точно за описом у `story-001-feature-summary-cards.md`: три `FeatureCard` замість кнопок на Home, підтвердження виходу через `AlertDialog`, оновлення лічильників при поверненні на Home через `ON_RESUME`-спостерігач (той самий патерн, що в `CalendarScreen.kt`).

### Зміни
- `HomeViewModel.kt` — додано 3 залежності (`GetEventsUseCase`, `ObserveShoppingItemsUseCase`, `GetTodoItemsUseCase`), `StateFlow` для `todayEventsCount`/`shoppingActiveCount`/`todoPendingCount`, метод `refresh()` для suspend-джерел; `shoppingActiveCount` реактивний через `flatMapLatest` на `sessionRepository.currentUserId`.
- `HomeScreen.kt` — три `Button` замінено на приватний composable `FeatureCard` (картка з заголовком і summary), `DisposableEffect` + `LifecycleEventObserver` на `ON_RESUME` викликає `viewModel.refresh()`, кнопка Log out тепер відкриває `AlertDialog` підтвердження замість негайного виходу; додано preview для порожнього стану.
- `MainActivity.kt` — `HomeViewModel.Factory` отримав 3 нові параметри-залежності (use case вже існували, нових use case/Room-змін не знадобилося).
- Новий unit-тест `HomeViewModelTest.kt` (7 тестів): підрахунок подій/покупок/справ, реактивність shopping-лічильника, робота `refresh()`, незмінена поведінка `onLogout`.
- `make rebuild` і `make test` зелені; усі 7 тестів `HomeViewModelTest` проходять.

## Story 002 — Показати/приховати пароль (Login/Register)

**Дата:** 2026-07-19

### Проблеми під час реалізації
- без відхилень від плану

### Рішення
- Реалізовано точно за описом у `story-002-login-show-password.md`: локальний UI-стан `isPasswordVisible` у `LoginContent`/`RegisterContent`, перемикання `visualTransformation` між `PasswordVisualTransformation()` та `VisualTransformation.None`, `IconButton` з текстовим гліфом (`👁`/`🙈`) як trailing-іконка поля пароля — без нової залежності `material-icons-extended`.

### Зміни
- `LoginScreen.kt` — у `LoginContent` додано `var isPasswordVisible by remember { mutableStateOf(false) }`, `trailingIcon` з `IconButton` для поля пароля; сигнатури composable-функцій і previews не змінені.
- `RegisterScreen.kt` — той самий патерн застосовано до `RegisterContent`.
- `LoginViewModel`, `RegisterViewModel`, `LoginUiState`, `RegisterUiState` — без змін.
- Юніт-тестів не додано (зміни суто UI-локальні, без впливу на бізнес-логіку — відповідно до плану сторі).
- `make rebuild` і `make test` зелені.

## Story 003 — Кнопка "Сьогодні" в календарі

**Дата:** 2026-07-19

### Проблеми під час реалізації
- без відхилень від плану (єдина технічна заминка — не пов'язана з кодом сторі: `gradlew clean` спершу впав через заблокований файл від завислого Gradle-демона; вирішено через `gradlew --stop` перед повторним запуском).

### Рішення
- Реалізовано точно за описом у `story-003-calendar-today-chip.md`: новий метод `onTodayClick()` у `CalendarViewModel` напряму встановлює поточний місяць і сьогоднішню дату (без `clampToMonth`, на відміну від `onMonthChanged`), чіп `TextButton("Сьогодні")` додано в `MonthHeader` поруч з іконкою вибору місяця/року, видимий завжди.

### Зміни
- `CalendarViewModel.kt` — доданий метод `onTodayClick()`.
- `CalendarScreen.kt` — `MonthHeader` отримав параметр `onTodayClick`, у ньому додано `TextButton("Сьогодні")`; `CalendarContent`/`CalendarScreen` прокидають `viewModel::onTodayClick`; оновлено preview-и `CalendarContent`, додано новий preview `MonthHeaderPreview`.
- `CalendarUiState.kt` — без змін (нових полів не додано).
- Новий unit-тести в `CalendarViewModelTest.kt` (3 тести): зміна місяця на поточний з будь-якого іншого, обраний день = сьогодні (а не clamp попереднього дня), перезавантаження подій під новий місяць.
- `make rebuild` і `make test` зелені; усі 13 тестів `CalendarViewModelTest` (включно з 3 новими) проходять.

## Story 005 — Todo: фільтр "Active only" + масове видалення виконаного

**Дата:** 2026-07-19

### Проблеми під час реалізації
- У самій сторі виявлено внутрішню суперечність: розділ "Дизайн" вимагає, щоб кнопка "Очистити виконані" була неактивна "незалежно від того, увімкнений фільтр чи ні", а розділ "Зміни по файлах" одночасно приписує `enabled = uiState.items.any { it.isDone }` і filtered `uiState.items` (фільтрація виконується саме в полі `items`, яке віддає ViewModel). За літеральної реалізації цього пункту кнопка завжди показувалась би неактивною, поки увімкнено фільтр "Active only", навіть якщо виконані елементи існують — і суперечило б явній вимозі з розділу "Дизайн".
- Технічна заминка, не пов'язана з кодом сторі: `gradlew clean` впав через заблокований файл від застряглого Gradle-демона (як і в story-003) — вирішено через `gradlew --stop` перед повторним запуском.

### Рішення
- Додано обчислюване поле `completedCount: Int` у `TodoListUiState`, яке рахується з повного (нефільтрованого) списку `currentItems` у `TodoListViewModel.uiState` — за тим самим патерном, що вже використовується для `isLimitReached`. Кнопка "Очистити виконані" й текст діалогу підтвердження використовують `uiState.completedCount`, тому їхня поведінка коректно не залежить від стану фільтра "Active only", як і вимагав розділ "Дизайн".
- Ревью підтвердило рішення коректним і таким, що не порушує жодного пункту Definition of Done.

### Зміни
- `TodoItemDao.kt` — новий метод `deleteCompleted(userId): Int` (`DELETE ... WHERE userId = :userId AND isDone = 1`).
- `TodoRepository.kt` / `LocalTodoRepository.kt` — новий метод `deleteCompleted(userId): Result<Int>`; реалізація в репозиторії не використовує спільний `runMutation` (він трактує 0 змінених рядків як `Unauthorized`, що некоректно для "0 виконаних елементів — не помилка").
- Новий `DeleteCompletedTodoItemsUseCase.kt` — проксі-виклик до репозиторію.
- `TodoListUiState.kt` — нові поля `showActiveOnly: Boolean` і `completedCount: Int` (останнє — рішення, описане вище, не було в оригінальному переліку полів сторі).
- `TodoListViewModel.kt` — новий `_showActiveOnly` стан, `uiState` фільтрує `items` при увімкненому фільтрі (внутрішній `_items` залишається повним), нові методи `onToggleActiveOnly()`/`onClearCompleted()`, нова залежність `deleteCompletedItems` у конструкторі й `Factory`.
- `TodoListScreen.kt` — новий рядок `TodoListControls` (`FilterChip` "Active only" + кнопка "Clear completed") під `AddItemRow`, `ClearCompletedConfirmDialog` (`AlertDialog` з Delete/Cancel) на локальному стані `showClearConfirm`; додано `@Preview` для обох нових composable (`TodoListControls`, `ClearCompletedConfirmDialog`) та оновлено існуючі preview-и.
- `MainActivity.kt` — `TodoListViewModel.Factory` отримав `deleteCompletedItems = DeleteCompletedTodoItemsUseCase(container.todoRepository)`.
- `FakeTodoRepository.kt` — реалізація `deleteCompleted` для юніт-тестів.
- Нові/доповнені тести: `DeleteCompletedTodoItemsUseCaseTest.kt` (1 тест), `TodoListViewModelTest.kt` (+4 тести), `TodoItemDaoTest.kt` (+2 android-тести), `LocalTodoRepositoryTest.kt` (+1 android-тест).
- `make rebuild` і `make test` зелені (14/14 тестів `TodoListViewModelTest`, 1/1 `DeleteCompletedTodoItemsUseCaseTest`); `make connected-test` пропущено — на машині не було підключеного пристрою/емулятора (`adb devices` — порожньо), android-тести написані, але не виконані.
