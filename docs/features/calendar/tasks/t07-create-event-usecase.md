---
status: Todo
owner: "Alex"
estimate: M
deps: [t02-calendar-event-validator, t05-calendar-event-repository]
---

# T07 — `CreateEventUseCase`

**Links:** [PRD AC-03, AC-04, AC-07, AC-07b, AC-08, AC-09](../PRD.md#5-acceptance-criteria) · [SAD Flow 2, Flow 3, Flow 4](../sad.md#6-runtime-view)

## Scope

`domain/usecase/CreateEventUseCase.kt`:

1. Run `CalendarEventValidator` (T02) against the draft → return `Result.Failure(error)` on `EmptyTitle` / `TitleTooLong` / `EndNotAfterStart`.
2. Check `repository.getEventCount(userId) < CalendarLimits.MAX_EVENTS` → `Result.Failure(CalendarError.EventLimitReached)` if at limit (AC-07b).
3. Otherwise `repository.create(event)` → `Result.Success`.

Also expose the standalone limit check used by the FAB press (SAD Flow 4: `checkCanCreate(userId)`), so `CalendarViewModel` (T11) can pre-empt opening the form when at the limit.

## DoD

- Unit tests: one happy-path (timed + all-day), one per `CalendarError` variant including `EventLimitReached` at count=100 (SAD Flow 2-4).
- No DB write occurs on any failure path (QG-2).
