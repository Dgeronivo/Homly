---
status: Todo
owner: "Alex"
estimate: S/M
deps: [t05-calendar-event-repository]
---

# T06 — `GetEventsUseCase`

**Links:** [PRD AC-01, AC-02](../PRD.md#5-acceptance-criteria) · [SAD QG-3](../sad.md#10-quality-requirements) · [SAD Flow 1](../sad.md#6-runtime-view)

## Scope

`domain/usecase/GetEventsUseCase.kt` — single use case (per commit `96d2403` consolidation of month+date variants) that:

- Fetches events for a `userId` + `YearMonth` from the repository.
- Exposes filtering/sorting so the caller (ViewModel) can derive a selected day's list: all-day events first, then timed events ascending by `startTime` (AC-01, QG-3).

## DoD

- Unit test `GetEventsUseCaseTest` with a stub repository: mixed all-day/timed input → asserts all-day-first, then ascending `startTime` order (QG-3 verify method).
- Empty-day case returns an empty list (supports AC-02 empty state in T11), not null/exception.
