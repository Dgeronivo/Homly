---
status: Todo
owner: "Alex"
estimate: L
deps: [t10-app-container-wiring]
---

# T13 — `AddEditEventScreen` + `AddEditEventViewModel`

**Links:** [PRD US-02, US-03, US-04, AC-03, AC-04, AC-05, AC-07, AC-08, AC-09](../PRD.md#5-acceptance-criteria) · [SAD Flow 2, Flow 3](../sad.md#6-runtime-view)

## Scope

- `presentation/AddEditEventViewModel.kt` — takes `eventId: Long? = null` (null = create, non-null = edit — SAD §5); reads `currentUserId` from `DataStoreSessionRepository`; calls `CreateEventUseCase` (T07) or `UpdateEventUseCase` (T08) on save.
- `presentation/AddEditEventScreen.kt` — form: title (text), date (date picker), all-day toggle, start/end time pickers (shown only when not all-day). On `CalendarError` from the use case, show inline field errors (SAD Flow 3: error under the `endTime` field for `EndNotAfterStart`; equivalent inline errors for `EmptyTitle`/`TitleTooLong`).

## DoD

- Save with valid timed input records the event and navigates back (AC-03, SAD Flow 2).
- Save with valid all-day input records the event with "весь день" label shown in the day list (AC-04) — label rendering itself lives in T11's list item, this task only needs `isAllDay` to persist correctly.
- Save with `endTime <= startTime` shows inline error, no navigation, no DB write (AC-07, SAD Flow 3).
- Empty title / title > 100 chars shows corresponding inline error (AC-08, AC-09).
- Editing an existing event pre-fills the form and updates in place (AC-05).
