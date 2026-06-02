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
