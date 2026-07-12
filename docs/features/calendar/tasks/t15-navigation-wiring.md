---
status: Todo
owner: "Alex"
estimate: M
deps: [t11-calendar-screen-viewmodel, t13-add-edit-event-screen]
---

# T15 — NavHost routes + FAB wiring

**Links:** [SAD §5 C4 Container](../sad.md#5-building-block-view) · [SAD §6 Flow 2, Flow 4](../sad.md#6-runtime-view) · [PRD AC-12](../PRD.md#5-acceptance-criteria)

## Scope

- Register routes in the existing NavHost (`MainActivity`): `calendar`, `calendar/add`, `calendar/edit/{id}` (per SAD §5 C4 Container).
- Wire `CalendarScreen`'s FAB ("+") to `navigate("calendar/add")`, or to a limit-reached Snackbar when `CreateEventUseCase.checkCanCreate` (T07) reports the limit hit (SAD Flow 4, AC-07b).
- Wire day-list item tap to `navigate("calendar/edit/{id}")`.

AC-12 (unauthenticated redirect) is already handled by the existing auth NavHost guard per SAD §1 decision-override F5-2 — no new code needed here, just confirm the `calendar` route sits behind that existing guard.

## DoD

- Tapping FAB at count < 100 opens `AddEditEventScreen` in create mode; at count = 100 shows the limit Snackbar instead (SAD Flow 4).
- Tapping an event in the day list opens `AddEditEventScreen` pre-filled in edit mode.
- Manual check: navigating to the `calendar` route while logged out redirects to login (confirms the existing guard covers the new route — AC-12).
