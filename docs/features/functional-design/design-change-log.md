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
