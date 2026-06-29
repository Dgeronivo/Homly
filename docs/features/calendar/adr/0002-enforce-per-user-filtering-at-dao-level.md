---
status: Accepted
owner: "Alex"
reviewers: ["Tech Lead", "Security Lead"]
updated_at: "2026-06-29"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0002 — Enforce per-user event filtering at DAO level

- **Status:** Accepted
- **Date:** 2026-06-29
- **Deciders:** Alex (Architect)

## Context

PRD §6.1 вимагає: «repository always filters events by userId; events created by other users are never returned». Питання — де саме має відбуватись фільтрація: у SQL-запиті Room DAO або у use-case після завантаження записів.

## Decision drivers

- Data isolation (QG-1, PRD §6.1, AC-10): жоден чужий рядок не повинен потрапляти в застосунок
- Defense in depth: security-invariant не має залежати від коректності use-case
- Встановлений паттерн: `TodoItemEntity` і `ShoppingItemEntity` вже фільтруються за `userId` на рівні DAO

## Considered options

1. **DAO-level WHERE userId = :userId** — SQL фільтр у кожному `@Query`, Room генерує запит із bind-параметром.
2. **Use-case level filter** — DAO повертає всі рядки, use-case відфільтровує за `userId` у Kotlin-коді.

## Decision outcome

**Chosen:** Option 1 — DAO-level `WHERE userId = :userId`.

Use-case є логіка, не security-boundary. Якщо use-case пропустить або некоректно застосує фільтр — дані іншого користувача потраплять у ViewModel. При DAO-рівні фільтрації — навіть із багованим use-case — БД фізично не повертає чужі рядки. Крім того, SQL-фільтр більш ефективний (індекс на `userId`), ніж post-load filtering у памʼяті.

## Consequences

**Positive**
- Security invariant виконується незалежно від коректності use-case
- SQL-рівень ефективніший (індекс): запит повертає тільки потрібні рядки
- Узгоджено з існуючим паттерном todo/shopping DAO

**Negative**
- Кожен новий DAO-метод потребує явного параметра `userId: Long` — дисципліна, яку треба підтримувати
- Складніше тестувати DAO ізольовано (треба передавати userId у кожен тест)

**Neutral**
- Use-case також має перевіряти `currentUserId` для операцій запису (create/update/delete) — це різні перевірки: читання ізолює DAO, записи перевіряє use-case

## Links

- PRD: [[../PRD.md]] §6.1
- SAD: [[../sad.md]] §4
- Related ADR: [[0001-use-proper-room-migration-for-calendar-events]]
