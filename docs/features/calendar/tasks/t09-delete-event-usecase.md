---
status: Todo
owner: "Alex"
estimate: S
deps: [t05-calendar-event-repository]
---

# T09 — `DeleteEventUseCase`

**Links:** [PRD US-05, AC-06](../PRD.md#5-acceptance-criteria)

## Scope

`domain/usecase/DeleteEventUseCase.kt` — deletes an event by id via the repository. No validator involved.

## DoD

- Unit test: deleting an existing event removes it (repository stub/in-memory fake asserts it's gone from subsequent `getEventsForMonth`).
