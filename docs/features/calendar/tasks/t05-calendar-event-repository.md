---
status: Todo
owner: "Alex"
estimate: M
deps: [t01-domain-foundation, t04-calendar-event-dao]
---

# T05 — `CalendarEventRepository` + `LocalCalendarEventRepository`

**Links:** [SAD §5](../sad.md#5-building-block-view) (building block view) · [SAD §4 SC-1](../sad.md#4-solution-strategy)

## Scope

- `domain/repository/CalendarEventRepository.kt` — interface: `getEventsForMonth`, `getEventCount`, `create`, `update`, `delete` (domain-typed, `CalendarEvent`, not entity).
- `data/repository/LocalCalendarEventRepository.kt` — implements the interface over `CalendarEventDao` (T04), maps `CalendarEventEntity` ↔ `CalendarEvent`.

## DoD

- Unit tests for the mapping layer (entity ↔ domain model round-trip, especially `LocalDate`/`LocalTime` fields).
- Repository interface has no Room import; only the `Local*` implementation does (Clean Architecture boundary per SAD §4 SC-1).
