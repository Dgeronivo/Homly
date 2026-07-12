---
status: Draft
owner: "Alex"
reviewers: ["Tech Lead"]
updated_at: "2026-07-06"
feature_size: M
stage: "13"
ticket: ""
---

# Epic — calendar (Events)

**Links:** [PRD](../PRD.md) · [SAD](../sad.md) · [ADR-0001](../adr/0001-use-proper-room-migration-for-calendar-events.md) · [ADR-0002](../adr/0002-enforce-per-user-filtering-at-dao-level.md) · [ADR-0003](../adr/0003-raise-min-sdk-to-29-for-native-java-time.md)

**UI reference:** [reference.png](../reference.png) — month grid (top) + selected-day list (bottom) + FAB, used as the visual reference for T11 (`CalendarScreen`).

## Summary

Break down of the calendar module (SAD §5 building blocks) into 15 atomic tasks across domain, data, and presentation layers. Solo effort (~1 person-week, PRD/ADR estimate).

## Layers

- **Domain** (T01, T02, T06-T09): `CalendarEvent`, `CalendarError`, `CalendarLimits`, `CalendarEventValidator`, use cases.
- **Data** (T03-T05): Room entity + `DateTimeConverters` + `Migration(3→4)` (ADR-0001), `CalendarEventDao` with per-user filtering (ADR-0002), repository.
- **Wiring** (T10): `AppContainer` manual DI.
- **Presentation** (T11-T14): `CalendarScreen`/`CalendarViewModel`, month/year picker, `AddEditEventScreen`/`AddEditEventViewModel`, delete flow.
- **Integration** (T15): NavHost routes.

## Dependency graph

```
T01 ──┬─► T02 ──────────────────────────┐
      └─► T03 ─► T04 ─► T05 ─┬─► T06 ──►┼─► T10 ─┬─► T11 ─┬─► T12
                              ├─► T07 ──►┤        │        └─► T15
                              ├─► T08 ──►┤        └─► T13 ─┬─► T15
                              └─► T09 ──►┘                 │
                                                  T11 ─► T14 (also needs T09)
```

Parallel branches once T05 lands: T06/T07/T08/T09 (use cases) are independent of each other. T11 and T13 (the two screens) can proceed in parallel once T10 wiring is in place; both converge at T15 (navigation).

## Tracker

→ [tracker.md](./tracker.md)

## Next owner

Tech Lead → stage 14 (claude-context) once tickets are created from this breakdown.
