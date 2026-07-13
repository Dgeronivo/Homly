---
status: Accepted
owner: "Alex"
reviewers: ["Tech Lead"]
updated_at: "2026-07-14"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0001 — Rely on fallbackToDestructiveMigration for calendar_events schema change

- **Status:** Accepted (revised 2026-07-14 — reverses the original 2026-06-29 decision)
- **Date:** 2026-06-29 (original) / 2026-07-14 (revision)
- **Deciders:** Alex (Architect)

## Context

Додавання таблиці `calendar_events` вимагає зміни `HomlyDatabase.version` з 3 до 4. Початково (2026-06-29) було прийнято рішення написати явну `Migration(3, 4)`, щоб не втратити існуючі todo/shopping-дані при зміні схеми під час розробки. Проект залишається experimental prototype (PRD §6 — NFR deferred, немає production-даних, немає production SLA): вартість підтримки явної міграції та окремого instrumented-тесту (`CalendarMigrationTest`) перевищує цінність для одноразового прототипу, де локальна БД на пристрої розробника скидається без жодних наслідків.

## Decision drivers

- Прототип не має production-користувачів і production-даних — втрата локальної БД під час розробки не критична
- Явна Migration додавала ~15 LOC у `HomlyDatabase` + окремий instrumented-тест (`CalendarMigrationTest`), які для прототипу недоцільно підтримувати
- `fallbackToDestructiveMigration(dropAllTables = true)` вже присутній у `AppContainer` як safety net — простіше й дешевше покладатись лише на нього

## Considered options

1. **Proper Migration(3→4)** — явна SQL-міграція `CREATE TABLE calendar_events (...)` в `HomlyDatabase` (початкове рішення, 2026-06-29).
2. **Rely on fallbackToDestructiveMigration** — не писати міграцію; Room видаляє і пересоздає БД при зміні версії.

## Decision outcome

**Chosen:** Option 2 — Rely on fallbackToDestructiveMigration (реверс початкового рішення від 2026-06-29).

Для experimental prototype без production-даних вартість підтримки явної Migration (код + окремий тест) не виправдана. `fallbackToDestructiveMigration(dropAllTables = true)` вже налаштований у `AppContainer` і покриває version bump 3→4: якщо схема застаріла, Room просто перестворює БД. Явну `Migration(3, 4)` та `CalendarMigrationTest` видалено з кодової бази.

## Consequences

**Positive**
- Менше коду й тестів для підтримки в прототипі
- Одне джерело правди для схемних змін (`fallbackToDestructiveMigration`), без розбіжностей між явними міграціями окремих фіч

**Negative**
- Зміна схеми (включно з цим 3→4 бампом) видаляє всі локальні дані (`users`, `todo_items`, `shopping_items`, `calendar_events`) при першому запуску після оновлення — прийнятно лише поки немає production-користувачів
- Якщо проект вийде за межі прототипу, це рішення потрібно переглянути до першого production-релізу (див. sad.md §11 risks)

**Neutral**
- Схема-експорти (`app/schemas/.../3.json`, `4.json`) залишаються — Room генерує їх незалежно від наявності явної міграції

## Links

- PRD: [[../PRD.md]]
- SAD: [[../sad.md]] §4
