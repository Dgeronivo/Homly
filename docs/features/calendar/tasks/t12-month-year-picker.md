---
status: Todo
owner: "Alex"
estimate: M
deps: [t11-calendar-screen-viewmodel]
---

# T12 — Month/year picker

**Links:** [PRD US-06, AC-11](../PRD.md#5-acceptance-criteria)

## Scope

A picker (dialog or dropdown) reachable from `CalendarScreen`'s header (per `reference.png` top-right icon area) letting the user pick a month + year directly; on confirm, calls `CalendarViewModel.onMonthChanged(yearMonth)` (already defined in T11 for month navigation) to jump straight to that month.

## DoD

- Selecting a month/year 6+ months away from the current one navigates directly to it without stepping through intermediate months (AC-11).
