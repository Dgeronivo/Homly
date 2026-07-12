---
status: Todo
owner: "Alex"
estimate: L
deps: [t10-app-container-wiring]
---

# T11 — `CalendarScreen` + `CalendarViewModel` (month grid + day list)

**Links:** [PRD US-00, US-01, AC-01, AC-02, AC-10](../PRD.md#5-acceptance-criteria) · [SAD Flow 1](../sad.md#6-runtime-view) · **UI reference:** [reference.png](../reference.png)

## Scope

- `presentation/CalendarViewModel.kt` — `StateFlow` of `uiState(daysWithEvents, selectedDate, selectedDayEvents)`; calls `GetEventsUseCase` (T06) on month change and on day selection (SAD Flow 1).
- `presentation/CalendarScreen.kt` — two-part Compose layout matching `reference.png`:
  - **Top half:** month grid (week rows, Mon-Sun header, current month days highlighted, days with events get a visual indicator/dot as in the reference screenshot), selected day gets a highlight ring.
  - **Bottom half:** selected day's event list — all-day events first, then timed (order from T06); empty state message when the day has no events (AC-02).
  - FAB ("+") — wired in T15, not this task; leave a callback slot.

`userId` comes from `DataStoreSessionRepository.currentUserId` — the ViewModel reads it once per session, never accepts it from the UI layer (AC-10 data isolation; SAD Flow 1 note — no user-facing userId selection).

## Out of scope

Month/year picker button next to the header (→ T12), FAB action (→ T15), delete swipe/long-press on list items (→ T14).

## DoD

- Compose preview or manual run shows month grid + day list resembling `reference.png` layout (grid on top, list below, no picker/FAB logic yet).
- Selecting a day with events shows all-day-first/timed-ascending ordering (AC-01).
- Selecting an empty day shows the empty state (AC-02).
- No event from another `userId` is ever rendered (AC-10 — verified via ViewModel unit test with a fake repository seeded with two users).
