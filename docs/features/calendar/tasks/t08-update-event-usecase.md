---
status: Todo
owner: "Alex"
estimate: S/M
deps: [t02-calendar-event-validator, t05-calendar-event-repository]
---

# T08 — `UpdateEventUseCase`

**Links:** [PRD US-04, AC-05](../PRD.md#5-acceptance-criteria)

## Scope

`domain/usecase/UpdateEventUseCase.kt` — same validation path as `CreateEventUseCase` (T07) minus the event-limit check (editing doesn't add a row), then `repository.update(event)`.

## DoD

- Unit tests: happy-path edit (title/date/time change), one per validation-failure `CalendarError` variant (`EmptyTitle`, `TitleTooLong`, `EndNotAfterStart`).
- Confirms no limit check is performed on update (distinguishing from T07).
