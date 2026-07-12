---
status: Todo
owner: "Alex"
estimate: S/M
deps: [t09-delete-event-usecase, t11-calendar-screen-viewmodel]
---

# T14 — Delete event UI flow

**Links:** [PRD US-05, AC-06](../PRD.md#5-acceptance-criteria)

## Scope

Trigger to delete an event from the day's event list in `CalendarScreen` (T11) — e.g. swipe-to-delete or a delete icon on the list item — with a confirmation step, calling `DeleteEventUseCase` (T09) and refreshing the list.

## DoD

- Deleting an event removes it from the list immediately and it does not reappear on re-selecting the day (AC-06).
- Cancelling the confirmation leaves the event untouched.
