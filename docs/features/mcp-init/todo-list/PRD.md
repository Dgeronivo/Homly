---
status: Accepted
owner: "Alex"
reviewers: ["Tech Lead", "Security Lead"]
updated_at: "2026-06-13"
resolved_questions:
  - "max_items_per_user: 50 (confirmed 2026-06-13)"
  - "sort_order: not-done by date desc, done deferred to bottom on list open (confirmed 2026-06-13)"
feature_size: M
stage: "03"
ticket: ""
---

# PRD — todo-list (mcp-init)

> **Inputs (required):** [idea-brief](./idea-brief.md) · [CONTEXT](./CONTEXT.md)
> **Reference module:** `app/src/main/java/com/dgero/homly/shopping/` — sealed-class errors, MAX_ITEMS=50, MAX_NAME_LENGTH=100, TransactionRunner, Repository/ViewModel/UseCase.
> **External context channels used:** Reference module code (shopping-list).

## 1. Context

Сімейні todo-items — справи, які потрібно виконати — зберігаються в Telegram-чатах, закріплених повідомленнях і пам'яті координатора. Статус «виконано» ніде не фіксується, справи губляться в потоці повідомлень, і лише один координатор знає актуальний перелік. Члени сім'ї не бачать стану одне одного в реальному часі. *(idea-brief §2, §3)*

Тригер: поточний workflow (Telegram + пам'ять) не дає structured shared-простору, де todo-items мають стан «виконано». Прототип — для однієї family-групи (2–6 осіб), перший крок валідації ядра. *(idea-brief §4)*

Обрано Approach C — «One Home, Always in Sync»: todo-list є одним із чотирьох рівноцінних модулів (events, todo, shopping, family). *(idea-brief §13)*

*Reference patterns (shopping module):* sealed-class помилки (`EmptyName`, `NameTooLong`, `LimitReached`), `MAX_ITEMS=50`, `MAX_NAME_LENGTH=100`, `TransactionRunner` для атомарного ліміту, Repository/ViewModel/UseCase. У поточній ітерації todo-list прив'язаний до `userId` (per-user, аналогічно до shopping-list). Shared-доступ для family буде додано в ітерації family-модуля.

*Resolved decisions:* max items per user = 50; sort order — невиконані за датою створення (новіші першими), виконані переміщуються в кінець при наступному відкритті списку (не одразу після позначення).

## 2. Goals

- Будь-який user веде структурований список своїх todo-items зі статусом «виконано» — без Telegram.
- User може позначити todo-item як виконаний і бачить повну картину своїх справ у будь-який момент.
- Coordinator знімає mental load: перелік справ більше не зберігається лише в пам'яті або в Telegram.

## 3. Non-goals

- Family-shared todo-list (спільний список для всіх членів family) — деферовано до ітерації family-модуля.
- Push-нотифікації при зміні todo-item — наступна фаза *(idea-brief §6)*.
- Голосовий ввід — поза межами прототипу *(idea-brief §6)*.
- Recurring todo-items — занадто складна механіка для v1.
- Assignee per todo-item — family у v1 є плоскою моделлю без ролей.

## 4. User stories

### US-01: Додати todo-item

**As a** user
**I want** to add a new todo-item to my list
**So that** I have a structured record of what needs to be done

### US-02: Позначити todo-item виконаним

**As a** user
**I want** to mark a todo-item as done
**So that** I know which tasks are complete without rereading Telegram history

### US-03: Переглянути список справ

**As a** user
**I want** to see my full todo-list with item statuses
**So that** I know at a glance what is pending and what is already done

### US-04: Редагувати назву todo-item

**As a** user
**I want** to edit the title of an existing todo-item
**So that** I can correct a typo or clarify what needs to be done

### US-05: Видалити todo-item

**As a** user
**I want** to delete a todo-item
**So that** my list stays relevant and free of outdated items

### US-06: Відновити виконаний todo-item

**As a** user
**I want** to unmark a done todo-item (set it back to not done)
**So that** I can reopen a task that needs to be redone

## 5. Acceptance criteria

### AC-01 (US-01) — happy: create todo-item

**Дано:** авторизований user має менше ніж максимально дозволену кількість todo-items
**Коли:** user надсилає непорожню назву в межах допустимої довжини
**Тоді:** система записує новий todo-item зі статусом «не виконано», фіксує час створення та відображає його у списку user

### AC-02 (US-02) — happy: mark as done

**Дано:** авторизований user має todo-item зі статусом «не виконано»
**Коли:** user позначає todo-item як виконаний
**Тоді:** система записує статус «виконано» та одразу відображає оновлений статус у списку

### AC-03 (US-03) — happy: view list

**Дано:** авторизований user має хоча б один todo-item
**Коли:** user відкриває todo-list
**Тоді:** система показує всі todo-items user: спочатку невиконані — впорядковані за датою створення (новіші першими), потім виконані — в кінці списку; переміщення виконаних items до кінця відбувається при відкритті списку, а не одразу під час поточного перегляду

### AC-04 (US-04) — happy: edit title

**Дано:** авторизований user має todo-item у своєму списку
**Коли:** user надсилає непорожню оновлену назву в межах допустимої довжини
**Тоді:** система записує нову назву та відображає оновлену назву у списку

### AC-05 (US-05) — happy: delete

**Дано:** авторизований user має todo-item у своєму списку
**Коли:** user видаляє todo-item
**Тоді:** система видаляє його назавжди і він більше не з'являється у списку

### AC-06 (US-06) — happy: unmark done

**Дано:** авторизований user має todo-item зі статусом «виконано»
**Коли:** user знімає позначку «виконано»
**Тоді:** система записує статус «не виконано» та одразу відображає оновлений статус у списку

### AC-07 (US-01) — error: blank title on add

**Дано:** авторизований user
**Коли:** user намагається додати todo-item з порожньою назвою (пусте поле або лише пробіли)
**Тоді:** система блокує дію та повідомляє user, що назва не може бути порожньою

### AC-08 (US-01) — error: title too long on add

**Дано:** авторизований user
**Коли:** user намагається додати todo-item з назвою, що перевищує максимально допустиму довжину
**Тоді:** система блокує дію та повідомляє user, що назва занадто довга

### AC-09 (US-04) — error: blank title on edit

**Дано:** авторизований user редагує назву todo-item
**Коли:** user надсилає порожню назву (пусте поле або лише пробіли)
**Тоді:** система блокує збереження та повідомляє user, що назва не може бути порожньою

### AC-10 (US-02) — authorization: another user's todo-item

**Дано:** авторизований user
**Коли:** user намагається змінити (toggle done, редагувати або видалити) todo-item, що належить іншому user
**Тоді:** система відхиляє дію і не розкриває, що такий todo-item існує

### AC-11 (US-01) — domain invariant: list at limit

**Дано:** авторизований user, чий todo-list досяг максимально дозволеної кількості items
**Коли:** user намагається додати ще один todo-item
**Тоді:** система блокує дію та повідомляє user, що список заповнений і не може прийняти більше items

### AC-12 (US-03) — cross-context: no active session

**Дано:** user не завершив вхід в застосунок (немає активної сесії)
**Коли:** user намагається відкрити todo-list
**Тоді:** система вимагає входу перед тим, як показати будь-які todo-items

## 6. Non-functional requirements

| Aspect | Target | Measurement |
|---|---|---|
| Ліміт items per user | ≤ 50 items | AndroidTest: 51-й item → LimitReached |

## 6.1 Security / privacy

- **Data classification:** internal — особисті справи user, не публічні дані.
- **Personal data touched:** назва todo-item (вільний текст від user).
- **AuthZ/AuthN impact:** всі операції (read, create, update, delete) фільтруються за `userId`; user бачить лише власні todo-items; DAO завжди фільтрує за `userId`.
- **Abuse cases:**
  - Cross-user доступ: система не розкриває існування чужого todo-item (AC-10).
  - Spam create: природне обмеження через ліміт items (AC-11, §6 NFR).
  - SQLite injection через назву: Room використовує параметризовані запити — прямий SQL не виконується.
- **Security review:** N/A — S-size фіча, internal дані, без нових authz-меж, без нових PII полів (аналогічно до shopping-list).

## 7. Metrics / KPIs

*(Немає — прототип для однієї сім'ї; вимірювання на поточному масштабі надлишкові.)*

## 8. Open questions

- [x] Яка максимальна кількість todo-items per user? **Вирішено: 50** *(2026-06-13)*
- [x] Чи потрібне сортування todo-list? **Вирішено: невиконані за датою створення (новіші першими); виконані — в кінець при відкритті списку, не одразу** *(2026-06-13)*
- [ ] Чи гарантує Room DB durability (writes survive process death) без додаткових заходів для прототипу? *(F3 — idea-brief §10 edge-case 4: offline divergence як trust risk)* — owner: Alex, due: перед architecture-design
