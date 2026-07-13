---
status: Draft
owner: "Alex"
reviewers: ["Tech Lead", "Security Lead"]
updated_at: "2026-07-14"
feature_size: M
stage: "03"
ticket: ""
resolved_questions:
  - "MAX_EVENTS per user: 100 (confirmed 2026-06-28)"
---

# PRD — calendar

> **Inputs (required):** [idea-brief](./idea-brief.md) · [CONTEXT](./CONTEXT.md)
> **Reference module:** N/A — green-field mode.
> **External context channels used:** None — only CONTEXT + idea-brief.

## 1. Context

Члени сім'ї координують події (візити до лікаря, виїзди, дні народження) через Telegram-повідомлення та пам'ять координатора. Немає структурованого спільного простору: що відбудеться, коли, хто має бути. Координатор тримає весь розклад у голові та нагадує решті членів сім'ї про заплановані події. *(idea-brief §2, §3)*

Тригер: події губляться в потоці Telegram; немає стану «підтверджено / скасовано»; члени сім'ї дізнаються про події пізно або не дізнаються взагалі. Прототип — для однієї family-групи (2–6 осіб). *(idea-brief §4)*

Обрано Approach C — «One Home, Always in Sync»: calendar/events є одним із чотирьох рівноцінних модулів (events, todo, shopping, family). У поточній ітерації events прив'язані до user (per-user); shared-доступ — в ітерації family-модуля. *(idea-brief §7, §13)*

*Reference: N/A — green-field mode.*

## 2. Goals

- User бачить усі події на вибраний день у calendar-view без пошуку в Telegram-чатах.
- User створює timed або all-day event одним сценарієм, без перемикань між застосунками.
- Координатор більше не тримає розклад у голові — застосунок є єдиним джерелом правди для сімейних подій *(per-user only in this iteration; shared access in family-module phase)*.

## 3. Non-goals

- Push-нотифікації — наступна фаза. *(idea-brief §5)*
- Recurring / повторювані події — out of scope прототипу. *(idea-brief §5)*
- Синхронізація з Google Calendar / Apple Calendar — out of scope прототипу. *(idea-brief §5)*
- Нагадування (reminders) — out of scope прототипу. *(idea-brief §5)*
- Week view — out of scope прототипу.
- Multi-day events — single-day only in this iteration.

## 4. User stories

### US-00: Переглянути події місяця

**As a** user
**I want** to open the calendar and see the current month's grid with visual indicators on days that have events
**So that** I can get an at-a-glance overview of which days are planned this month

### US-01: Переглянути події дня

**As a** user
**I want** to see which days in the current month have events and select a specific day to view its events listed in the lower half of the screen
**So that** I can quickly navigate to any day and view all events scheduled for it

### US-02: Додати timed event

**As a** user
**I want** to create an event with a title, date, start time, and end time
**So that** I can see the event in my calendar with its exact start and end time

### US-03: Додати all-day event

**As a** user
**I want** to create an event with a title and date, marked as all-day
**So that** day-long events are visible in the calendar without specifying exact times

### US-04: Редагувати event

**As a** user
**I want** to edit the title, date, or time of an existing event
**So that** I can correct mistakes or reflect schedule changes

### US-05: Видалити event

**As a** user
**I want** to delete an event I created
**So that** cancelled events are removed from the calendar

### US-06: Навігація по місяцях

**As a** user
**I want** to select a month and year directly from a picker
**So that** I can quickly jump to any date without scrolling month by month

## 5. Acceptance criteria

### AC-01 (US-01) — happy: select day, view events

**Given** a user is logged in and has events on a specific date
**When** the user taps on that date in the calendar grid
**Then** the system shows events for that date in the lower half of the screen: all-day events first, then timed events sorted by start time

### AC-02 (US-01) — happy: empty day

**Given** a user is logged in and the selected date has no events
**When** the user taps on that date in the calendar grid
**Then** the system shows an empty state in the lower half of the screen indicating no events for that day

### AC-03 (US-02) — happy: create timed event

**Given** a user is logged in and views the calendar
**When** the user taps the "+" button, fills in a non-empty title, selects a date, sets a start time and an end time where end is after start, and saves
**Then** the system records the event and it appears in the calendar on the selected date with start and end time shown

### AC-04 (US-03) — happy: create all-day event

**Given** a user is logged in and views the calendar
**When** the user taps the "+" button, fills in a non-empty title, selects a date, marks the event as all-day, and saves
**Then** the system records the event and it appears at the top of that date's event list with a "весь день" label instead of time

### AC-05 (US-04) — happy: edit event

**Given** a user is logged in and has an existing event
**When** the user changes the title, date, or time and saves
**Then** the system updates the event and the calendar immediately reflects the changes

### AC-06 (US-05) — happy: delete event

**Given** a user is logged in and has an existing event
**When** the user deletes the event
**Then** the system removes the event and it no longer appears in the calendar

### AC-07b (US-02, US-03) — domain invariant: event limit reached

**Given** a user is logged in and has already created 100 events
**When** the user taps the "+" button to create a new event
**Then** the system blocks the action and tells the user that no more than 100 events can be created

### AC-07 (US-02) — domain invariant: end time ≤ start time

**Given** a user is creating a timed event
**When** the user sets the end time equal to or before the start time
**Then** the system blocks the action and tells the user that the end time must be after the start time

### AC-08 (US-02, US-03) — error: empty title

**Given** a user is creating or editing an event
**When** the user submits with an empty title
**Then** the system blocks the action and tells the user that the title cannot be empty

### AC-09 (US-02, US-03) — error: title too long

**Given** a user is creating or editing an event
**When** the user submits a title longer than 100 characters
**Then** the system blocks the action and tells the user that the title must be at most 100 characters

### AC-10 (US-01) — authorization: data isolation

**Given** a user is logged in
**When** the user opens the calendar or views any event list
**Then** the system shows only events created by that user — events belonging to other users are never displayed

### AC-11 (US-06) — happy: month/year selector

**Given** a user is viewing any month in the calendar
**When** the user opens the month/year selector and picks a specific month and year
**Then** the system navigates directly to the selected month and shows it in the calendar grid

### AC-12 (US-01) — cross-context: unauthenticated access

**Given** a user is not logged in
**When** the user attempts to open the calendar screen
**Then** the system redirects the user to the login screen and no event data is shown

### AC-13 (US-02, US-03) — happy: add-event date defaults to the selected calendar day

**Given** a user is viewing the calendar with a specific day selected (either by default or because the user tapped a date in the grid)
**When** the user taps the "+" button to create a new event
**Then** the add-event form opens with the date field pre-filled to that selected day, which the user can still change before saving

## 6. Non-functional requirements

N/A — experimental prototype, NFRs deferred.

## 6.1 Security / privacy

- **Data classification:** internal — personal calendar data (event titles may contain personal info such as medical appointments)
- **Personal data touched:** event title (free text, internal sensitivity)
- **AuthZ/AuthN impact:** user must be authenticated; repository always filters events by userId; events created by other users are never returned
- **Abuse cases:**
  - Cross-user access: impossible — repository filters by current userId at DB level
  - Spam create: limited by MAX_EVENTS_PER_USER (see §8 OQ-1)
  - Injection via title: title rendered as plain text; parameterized DB queries prevent injection
- **Security review:** N/A — no new auth boundaries, no new PII beyond existing auth-scoped data, local DB only

## 7. Metrics / KPIs

N/A — experimental prototype.

## 8. Open questions

*Немає відкритих питань.*

---

**Resolved decisions:**
- MAX_EVENTS per user = 100 (confirmed 2026-06-28)
