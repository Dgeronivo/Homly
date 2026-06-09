# Changelog: Shopping List

Журнал реалізації фічі «Список покупок». Кожна ітерація — окремий запис.

---

## Iteration 1 — 2026-06-03 (Domain)

**Зроблено:**
- `shopping/domain/model/ShoppingItem.kt` — `id, name, isBought, createdAt`.
- `shopping/domain/model/ShoppingSortOrder.kt` — enum `DATE_DESC` (дефолт), `ALPHABETICAL`.
- `shopping/domain/model/ShoppingError.kt` — `EmptyName`, `NameTooLong`, `LimitReached`, `Unknown` (за зразком `AuthError`).
- `shopping/domain/model/ShoppingLimits.kt` — `MAX_ITEMS = 50`, `MAX_NAME_LENGTH = 100` (єдине джерело бізнес-констант).
- `shopping/domain/repository/ShoppingRepository.kt` — інтерфейс; мутації повертають `Result`.
- Use cases: `ObserveShoppingItemsUseCase` (сортування + тай-брейк за `id`), `AddShoppingItemUseCase`, `EditShoppingItemUseCase`, `ToggleShoppingItemUseCase`, `DeleteShoppingItemUseCase`.
- Unit-тести: `AddShoppingItemUseCaseTest`, `EditShoppingItemUseCaseTest`, `ObserveShoppingItemsUseCaseTest` (фейковий репозиторій).

**Відхилення від плану:**
- Ліміт 50 (`LimitReached`) забезпечується атомарно в репозиторії (Ітерація 2), а `AddShoppingItemUseCase` валідує назву й делегує `repository.add`. Інтерфейс `ShoppingRepository` не має `count`, тому окрема (неатомарна) перевірка в use case була б зайвою й суперечила б атомарності з Ітерації 2. Тест use case на `LimitReached` працює через фейковий репозиторій, що теж тримає ліміт.
- Додано `ShoppingError.Unknown` (не в списку плану) — дзеркалить `AuthError.Unknown` для catch-all мапінгу помилок у репозиторії.

**Build / review:** `gradlew build` → `BUILD SUCCESSFUL`; unit-тести зелені. Self-review проти плану → ✅.

---

## Iteration 2 — 2026-06-03 (Data + DB integration)

**Зроблено:**
- `shopping/data/local/ShoppingItemEntity.kt` — таблиця `shopping_items`, `@PrimaryKey(autoGenerate)`, `Index("userId")`, дефолти `isBought=false`, `createdAt`.
- `shopping/data/local/ShoppingItemDao.kt` — `observeByUser` (Flow, без сортування в SQL), `countByUser` (suspend), `insert`, `updateName`, `updateBought`, `deleteById`.
- `shopping/data/repository/LocalShoppingRepository.kt` — реалізує `ShoppingRepository`; мапінг Entity↔domain (`userId` лишається в data); `add` через `TransactionRunner` атомарно: `countByUser` + перевірка ліміту + `insert`; помилки мапляться в `ShoppingError.Unknown`.
- `core/data/HomlyDatabase.kt` — додано `ShoppingItemEntity`, `version = 2`, `shoppingItemDao()`.
- `HomlyApplication.kt` (`AppContainer`) — спільний `transactionRunner`, виставлено `shoppingRepository`; `fallbackToDestructiveMigration(dropAllTables = true)` у білдері.
- androidTest: `ShoppingItemDaoTest` (фільтрація за `userId`, відсутність витоку, оновлення Flow на insert/edit/toggle/delete, `countByUser`), `LocalShoppingRepositoryTest` (атомарність ліміту 50, ліміт per-user, edit не чіпає `createdAt`/`isBought`).

**Відхилення від плану:**
- Замість застарілого `fallbackToDestructiveMigration()` використано `fallbackToDestructiveMigration(dropAllTables = true)` — актуальний незадепрекейчений API Room 2.7.1 (та сама семантика).

**Build / review:** `gradlew build` → `BUILD SUCCESSFUL`; unit-тести зелені, androidTest компілюється (виконання — на емуляторі, гейт Ітерації 3). Self-review проти плану → ✅.

---

## Iteration 3 — 2026-06-03 (Presentation + navigation)

**Зроблено:**
- `shopping/presentation/ShoppingListUiState.kt` — `items`, `sortOrder`, `newItemText`, `isLimitReached`, `errorMessage`.
- `shopping/presentation/ShoppingListViewModel.kt` — `combine(currentUserId, sortOrder)` + `flatMapLatest` (без stale `userId` при зміні користувача); `sortOrder`/`newItemText`/`errorMessage` як `MutableStateFlow`; `StateFlow<ShoppingListUiState>`; методи `onAdd/onToggle/onEdit/onDelete/onSortChange/onNewItemTextChange`; `Factory`; мапер `shoppingErrorMessage`.
- `shopping/presentation/ShoppingListScreen.kt` — Material3: рядок додавання (вимкнений на 50 + helper text), перемикач сортування `FilterChip` (Date ⇄ A–Z), `LazyColumn` (чекбокс, `LineThrough` для куплених, tap-to-edit inline, кнопка `✕`), порожній стан, `TopAppBar` із back. Приватний stateless `ShoppingListContent` + два `@Preview`.
- `MainActivity.kt` — `composable("shopping")` з `ShoppingListViewModel.Factory`; back через `popBackStack`.
- `HomeScreen.kt` — кнопка «Shopping list» → `navigate("shopping")`.
- Unit-тести: `ShoppingListViewModelTest` — дефолт `DATE_DESC`, перемикання сортування, `isLimitReached`, реакція на зміну `currentUserId` без витоку, порожній список.

**Відхилення від плану:**
- Замість векторної іконки видалення використано текст `✕` — у проєкті немає залежності `material-icons`, а план забороняє додавати нові залежності.
- `make connected-test` не виконано — підключеного емулятора/пристрою немає (`adb devices` порожній). androidTest-джерела компілюються (`compileDebugAndroidTestKotlin` → `BUILD SUCCESSFUL`).

**Build / review:** `gradlew clean build` (= `make rebuild`) → `BUILD SUCCESSFUL`; усі unit-тести зелені; lint чистий. Self-review проти плану → ✅.

---

## Iteration 4 — 2026-06-03 (Наскрізне ревью)

Наскрізне ревью всієї фічі (commits Ітерацій 1–3) проти `ARCHITECTURE.md` і `code-quality.md`.

**Перевірено (✅):**
- Структура пакетів `shopping/{domain,data,presentation}` + `core/data`; MVVM зі `StateFlow` + `collectAsStateWithLifecycle`; Navigation Compose; один `Activity`.
- Відсутність витоку `userId`: `flatMapLatest` перепідписується на зміну користувача; DAO/репозиторій скоупляться за `userId`; покрито VM- та DAO-тестами.
- Ліміт/валідація на стиках: валідація назви — в use case, ліміт — атомарно в репозиторії, UI блокує поле на 50 і мапить `LimitReached`.
- SOLID / одна відповідальність; залежності лише всередину; спільний `transactionRunner` (DRY).

**Post-review findings:**
1. Helper text у `ShoppingListScreen` хардкодив «max 50 items», дублюючи `ShoppingLimits.MAX_ITEMS` (єдине джерело правди) — ризик розсинхрону при зміні ліміту.

**Виправлення:**
1. Helper text тепер посилається на `ShoppingLimits.MAX_ITEMS` через інтерполяцію.

**Build / review:** `gradlew clean build` → `BUILD SUCCESSFUL`; усі unit-тести зелені; lint чистий. Final review → ✅.

> Примітка: `make connected-test` по всій фічі не виконано — немає підключеного емулятора/пристрою (`adb devices` порожній). Усі androidTest-джерела компілюються; запустити на емуляторі при можливості.
