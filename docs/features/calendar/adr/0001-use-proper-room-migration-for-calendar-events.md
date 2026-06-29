---
status: Accepted
owner: "Alex"
reviewers: ["Tech Lead"]
updated_at: "2026-06-29"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0001 — Use proper Room Migration for calendar_events schema change

- **Status:** Accepted
- **Date:** 2026-06-29
- **Deciders:** Alex (Architect)

## Context

Додавання таблиці `calendar_events` вимагає зміни `HomlyDatabase.version` з 3 до 4. Наразі в базі активний `fallbackToDestructiveMigration`, який знищує всі дані при незапланованій зміні схеми. Бізнес-рішення: для цього проекту потрібна явна Migration.

## Decision drivers

- Збереження даних користувача (todo, shopping, session) при оновленні застосунку
- `HomlyDatabase` спільна для всіх модулів — будь-яка деструктивна операція зачіпає всі фічі
- Простота: Migration(3→4) — лише `CREATE TABLE calendar_events (...)`, не потребує перезапису даних

## Considered options

1. **Proper Migration(3→4)** — явна SQL-міграція `CREATE TABLE calendar_events (...)` в `HomlyDatabase`.
2. **Rely on fallbackToDestructiveMigration** — не писати міграцію; Room видалить і пересоздасть БД при зміні версії.

## Decision outcome

**Chosen:** Option 1 — Proper Migration(3→4).

Деструктивна міграція означає втрату todo-items і shopping-items кожного разу, коли змінюється схема під час розробки. Оскільки Migration(3→4) є простою (`CREATE TABLE` без data backfill), зусилля мінімальні, а збереження існуючих даних критично навіть для прототипу.

## Consequences

**Positive**
- Дані todo/shopping/session зберігаються при оновленні
- Встановлюємо precedent явних міграцій для наступних фіч (family, shopping v2)

**Negative**
- ~10-20 LOC додаткового коду у `HomlyDatabase`
- Кожна наступна зміна схеми потребує явної migration

**Neutral**
- `fallbackToDestructiveMigration` залишається як fallback на edge-cases (corrupted DB) — не видаляємо

## Links

- PRD: [[../PRD.md]]
- SAD: [[../sad.md]] §4
