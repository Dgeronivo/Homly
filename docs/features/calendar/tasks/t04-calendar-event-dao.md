---
status: Todo
owner: "Alex"
estimate: S
deps: [t03-room-entity-migration]
---

# T04 — `CalendarEventDao` (per-user filtered queries)

**Links:** [ADR-0002](../adr/0002-enforce-per-user-filtering-at-dao-level.md) · [SAD QG-1](../sad.md#10-quality-requirements) · [SAD §5](../sad.md#5-building-block-view)

## Scope

`data/local/CalendarEventDao.kt` — every read query takes `userId: Long` and filters `WHERE userId = :userId` at the SQL level (ADR-0002, not post-load filtering):

- `getEventsForMonth(userId, yearMonth)` / equivalent range query
- `getEventCount(userId): Int`
- `insert`, `update`, `delete` (write methods scoped by entity, not requiring a separate userId filter since they operate on the row's own id — use-case layer is responsible for confirming ownership before calling update/delete, per ADR-0002 "Neutral" note)

## DoD

- Unit test `CalendarEventDaoTest` (QG-1 verify method): insert events for two different `userId`s, assert a query for `userId=1` never returns rows with `userId=2`.
