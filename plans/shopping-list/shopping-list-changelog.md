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
