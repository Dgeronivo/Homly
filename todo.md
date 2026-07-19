# TODO / Issues / Plans / Debt

Tech. debt:
- проблеми з вводом кирилиці з клавіатури з емулятора
- додати привітання для першого показу
- часометр на івентах

## На обговорення

- [ ] **Атомарні бізнес-інваріанти в data-шарі (ліміт shopping-списку).** `LocalShoppingRepository.add` застосовує ліміт `MAX_ITEMS = 50` всередині транзакції (`countByUser` + `insert` атомарно), а не в `AddShoppingItemUseCase`. Значення/помилка/контракт лежать у domain (`ShoppingLimits`, `ShoppingError.LimitReached`, kdoc інтерфейсу), але саме *застосування* — у data.
  - **Чому так:** це check-then-act інваріант. Перевірка в use case (`count()` → `add()`) = два окремі виклики → TOCTOU-гонка (два паралельні `add` бачать 49 → вставляють 51). Атомарність вимагає транзакції Room, а транзакція — інфраструктурна межа, доступна лише в data-шарі. Той самий патерн уже в `LocalUserRepository.register` (унікальність акаунта в транзакції).
  - **Питання на обговорення:** (1) чи лишаємо це усталеним патерном для всіх майбутніх атомарних інваріантів (todo, calendar); (2) чи варто підсилити kdoc `ShoppingRepository.add` явним «transactional invariant», щоб не сприймалось як хардкод; (3) чи треба domain-сервіс/абстракція над транзакцією для «чистоти» — поки виглядає як over-engineering для Room; (4) реальне застосування ліміту тестується лише в androidTest (фейк-репо в unit-тесті дублює правило) — чи влаштовує.

## Pending

- [ ] Browse official plugins — `/plugin` shows available bundles of skills, hooks, and MCP servers you can add to Claude Code.
- [ ] Add ktlint or detekt — once the app has meaningful code, a linter will help Claude catch style issues on its own edits.
- [ ] Set up GitHub CLI — `winget install GitHub.cli` on Windows; enables PR and issue management from the terminal.
- [ ] Add a ViewModel + Room skeleton — scaffold one feature end-to-end (e.g., `todo/`) with ViewModel + Room to establish the pattern for all other features.
- [ ] Consider Firebase for the family-sharing feature — Firestore gives real-time sync across family members' devices without building a backend.

Planned features:
- Calendar with events and reminders
- To-do list (shared between family members)
- Shopping list (shared between family members) - +
- Family mode — members can see each other's events and lists
- Push - Notification
- Voice input — keyword phrases trigger actions without a keyboard (e.g., "Buy sugar 1kg" adds to shopping list)
- translations
- ліцензія, попередження про збереження данних
