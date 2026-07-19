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
