# План реалізації: Shopping List (список покупок)
_Складено: 2026-05-31 · Оновлено: 2026-06-03 (узгоджено з пайплайном `analyze-new-feature`)_

Узгоджені рішення:
- приватний список на користувача (фільтрація за `userId`);
- куплені позиції лишаються перекресленими, видаляються вручну;
- є редагування назви існуючої позиції;
- ліміт **50** рахує всі позиції (куплені + активні); при досягненні додавання блокується з повідомленням.

**Послідовність:** domain → data → presentation. Domain першим, бо `LocalShoppingRepository` реалізує інтерфейс `ShoppingRepository` з domain — кожна ітерація збирається самостійно (`make build`). Це повторює auth (iter2 domain → iter3 data → iter4 presentation).

**Залежності:** нічого не додаємо — Room, Navigation Compose, Lifecycle, Material3 і тестові бібліотеки вже є в `gradle/libs.versions.toml`.

**Субагенти:** кожна ітерація проходить цикл пайплайну. Реалізацію коду виконує окремий **Iter subagent** (пише код ітерації, завершує `make build`, повертає підсумок + `BUILD SUCCESSFUL`/`BUILD FAILED + причина`). Далі окремий **Reviewer subagent** перевіряє написане проти плану й повертає `✅`/`❌`. На `BUILD FAILED` або `❌` — окремий **Fix subagent**, потім цикл повторюється з кроку build. Головний агент лише оркеструє цикл, веде changelog і робить commit — сам код пишуть субагенти.

**Changelog:** вести `plans/shopping-list/shopping-list-changelog.md` (українською), створити на Ітерації 1. На кожну ітерацію — запис `## Iteration N — <дата>`: що зроблено, проблеми build/review, відхилення від плану.

---

## Ітерація 1 — Domain (`shopping/domain`)
Реалізація:
- [ ] `model/ShoppingItem.kt` — `id: Long, name: String, isBought: Boolean, createdAt: Long`.
- [ ] `model/ShoppingSortOrder.kt` — enum `DATE_DESC` (дефолт), `ALPHABETICAL`.
- [ ] `model/ShoppingError.kt` — `EmptyName`, `NameTooLong`, `LimitReached` (за зразком `AuthError`).
- [ ] `repository/ShoppingRepository.kt` — `observeItems(userId): Flow<List<ShoppingItem>>`, `add(userId, name)`, `editName(id, name)`, `toggleBought(id, isBought)`, `delete(id)` (мутації повертають `Result`/`ShoppingError`).
- [ ] `usecase/ObserveShoppingItemsUseCase` — приймає `userId` + `ShoppingSortOrder`, повертає відсортований `Flow`; тай-брейк за `id`; `ALPHABETICAL` — case-insensitive.
- [ ] `usecase/AddShoppingItemUseCase` — `trim` → валідація (не порожнє, ≤100) → перевірка ліміту 50 → `Result`.
- [ ] `usecase/EditShoppingItemUseCase` — `trim` + валідація; не змінює `createdAt`/`isBought`.
- [ ] `usecase/ToggleShoppingItemUseCase`, `usecase/DeleteShoppingItemUseCase`.

Тести (`src/test`, фейковий репозиторій):
- [ ] `AddShoppingItemUseCase` — порожнє/пробіли → `EmptyName`; >100 → `NameTooLong`; 50 позицій → `LimitReached`; валідний кейс → success.
- [ ] `EditShoppingItemUseCase` — валідація; `createdAt`/`isBought` незмінні.
- [ ] Сортування — `DATE_DESC` з тай-брейком за `id`; `ALPHABETICAL` case-insensitive.

Завершення:
- [ ] `make build` — `BUILD SUCCESSFUL`.
- [ ] Reviewer subagent → `✅`.
- [ ] Commit `feat(shopping-list): iteration 1 — domain layer`.

---

## Ітерація 2 — Data + інтеграція з БД (`shopping/data`, `core/data`)
Реалізація:
- [ ] `ShoppingItemEntity.kt` — таблиця `shopping_items`: `id: Long @PrimaryKey(autoGenerate = true)`, `userId: Long` з `Index("userId")`, `name: String`, `isBought: Boolean = false`, `createdAt: Long = System.currentTimeMillis()`.
- [ ] `ShoppingItemDao.kt` — `observeByUser(userId): Flow<List<ShoppingItemEntity>>` (без сортування в SQL), `countByUser(userId): Int` (suspend), `insert(...)`, `updateName(id, name)`, `updateBought(id, isBought)`, `deleteById(id)`.
- [ ] `LocalShoppingRepository.kt` — реалізує `ShoppingRepository`, мапить Entity↔domain (`userId` лишається в data); `add` через `TransactionRunner` (`auth.data.repository`): `countByUser` + `insert` атомарно з перевіркою ліміту.
- [ ] `HomlyDatabase.kt` (`core/data`) — додати `ShoppingItemEntity` в `entities`, `version = 2`, `abstract fun shoppingItemDao()`, `fallbackToDestructiveMigration()` у білдері.
- [ ] `AppContainer` (`HomlyApplication.kt`) — створити й виставити `shoppingRepository` (передати `shoppingItemDao()` + наявний `runTransaction`).

Тести (`src/androidTest`, in-memory Room, за зразком `UserDaoTest`):
- [ ] фільтрація за `userId` без витоку між користувачами;
- [ ] `Flow` оновлюється на insert/toggle/edit/delete;
- [ ] атомарність ліміту в транзакції.

Завершення:
- [ ] `make build` — `BUILD SUCCESSFUL`.
- [ ] Reviewer subagent → `✅`.
- [ ] Commit `feat(shopping-list): iteration 2 — data layer + DB integration`.

---

## Ітерація 3 — Presentation + навігація (`shopping/presentation`)
Реалізація:
- [ ] `ShoppingListUiState.kt` — `items: List<ShoppingItem>`, `sortOrder`, `newItemText`, `isLimitReached`, `errorMessage`.
- [ ] `ShoppingListViewModel.kt` — `flatMapLatest` від `sessionRepository.currentUserId` (без stale userId при зміні користувача), тримає вибраний `sortOrder`, експонує `StateFlow<ShoppingListUiState>`; методи `onAdd/onToggle/onEdit/onDelete/onSortChange`; має `Factory`.
- [ ] `ShoppingListScreen.kt` — Material3: рядок додавання (`OutlinedTextField` + кнопка, блокується на 50 з helper text), перемикач сортування (Date ⇄ A–Z), `LazyColumn` (чекбокс bought, `LineThrough` при `isBought`, tap-to-edit inline, іконка видалення), порожній стан. Приватний stateless content-composable + `@Preview`.
- [ ] `MainActivity.kt` — `composable("shopping")` з `ShoppingListViewModel.Factory`.
- [ ] `HomeScreen.kt` — кнопка «Список покупок» → `navController.navigate("shopping")`.

Тести (`src/test`):
- [ ] `ShoppingListViewModel` — дефолт `DATE_DESC`; перемикання сортування; прапор `isLimitReached`; реакція на зміну `currentUserId`.

Завершення:
- [ ] `make rebuild` — компіляція + усі unit-тести; `make connected-test` за наявності емулятора.
- [ ] Reviewer subagent → `✅`.
- [ ] Commit `feat(shopping-list): iteration 3 — presentation + navigation`.

---

## Ітерація 4 — Фінальне ревью та виправлення
Наскрізне ревью всієї фічі (а не однієї ітерації) після того, як 1–3 змерджені.

Ревью:
- [ ] Запустити ревью по всьому diff фічі (`/code-review` або Reviewer subagent над сукупним diff Ітерацій 1–3).
- [ ] Перевірити наскрізні аспекти: відповідність ARCHITECTURE.md і правилам `code-quality.md` (SOLID, одна відповідальність), узгодженість шарів, відсутність витоку `userId` між користувачами, коректність ліміту/валідації на стиках шарів, повнота тестів.
- [ ] Зафіксувати всі знахідки в changelog як «Post-review findings».

Виправлення:
- [ ] Fix subagent усуває знахідки; повторити ревью до `✅`.
- [ ] Описати виправлення в changelog.

Завершення:
- [ ] `make rebuild` + `make connected-test` (за наявності емулятора) — усе зелене.
- [ ] Commit `feat(shopping-list): iteration 4 — review fixes`.

---

## Відкриті дрібниці (дефолти, не блокують)
- Міграція БД — destructive для прототипу (узгоджено: лишаємо простоту).
- UX ліміту — вимкнене поле + helper text.
- Редагування — inline-поле.
- Сортування не персиститься між сесіями (скидається до `DATE_DESC`).
