---
status: Todo
owner: "Alex"
estimate: S
deps: [t06-get-events-usecase, t07-create-event-usecase, t08-update-event-usecase, t09-delete-event-usecase]
---

# T10 — `AppContainer` wiring for calendar module

**Links:** [ARCHITECTURE.md](../../../../ARCHITECTURE.md) (manual DI, no framework) · [SAD §2](../sad.md#2-constraints)

## Scope

Wire `CalendarEventDao` → `LocalCalendarEventRepository` → the four use cases (T06-T09) into `AppContainer` (`HomlyApplication.kt`), following the existing pattern used for `todo`/`shopping` modules. No DI framework (Hilt/Koin) — manual constructor wiring per CLAUDE.md/ARCHITECTURE.md convention.

## DoD

- `AppContainer` exposes the calendar use cases the same way it exposes existing todo/shopping use cases (naming/style consistency).
- App compiles and existing todo/shopping wiring is untouched.
