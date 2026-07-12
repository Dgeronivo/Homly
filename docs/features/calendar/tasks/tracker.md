# Task tracker — calendar

| Task | Title | Layer | Deps | Estimate | Owner | Status |
|---|---|---|---|---|---|---|
| [T01](./t01-domain-foundation.md) | Domain foundation: `CalendarEvent`, `CalendarError`, `CalendarLimits` | domain | — | S | Alex | Done |
| [T02](./t02-calendar-event-validator.md) | `CalendarEventValidator` | domain | T01 | S/M | Alex | Done |
| [T03](./t03-room-entity-migration.md) | Room entity + `DateTimeConverters` + `Migration(3→4)` | data | T01 | M | Alex | Done |
| [T04](./t04-calendar-event-dao.md) | `CalendarEventDao` (per-user filtered queries) | data | T03 | S | Alex | Done |
| [T05](./t05-calendar-event-repository.md) | `CalendarEventRepository` + `LocalCalendarEventRepository` | data | T01, T04 | M | Alex | Done |
| [T06](./t06-get-events-usecase.md) | `GetEventsUseCase` | domain | T05 | S/M | Alex | Done |
| [T07](./t07-create-event-usecase.md) | `CreateEventUseCase` | domain | T02, T05 | M | Alex | Done |
| [T08](./t08-update-event-usecase.md) | `UpdateEventUseCase` | domain | T02, T05 | S/M | Alex | Done |
| [T09](./t09-delete-event-usecase.md) | `DeleteEventUseCase` | domain | T05 | S | Alex | Done |
| [T10](./t10-app-container-wiring.md) | `AppContainer` wiring for calendar module | wiring | T06, T07, T08, T09 | S | Alex | Done |
| [T11](./t11-calendar-screen-viewmodel.md) | `CalendarScreen` + `CalendarViewModel` (month grid + day list) | presentation | T10 | L | Alex | Done |
| [T12](./t12-month-year-picker.md) | Month/year picker | presentation | T11 | M | Alex | Done |
| [T13](./t13-add-edit-event-screen.md) | `AddEditEventScreen` + `AddEditEventViewModel` | presentation | T10 | L | Alex | Done |
| [T14](./t14-delete-event-ui-flow.md) | Delete event UI flow | presentation | T09, T11 | S/M | Alex | Done |
| [T15](./t15-navigation-wiring.md) | NavHost routes + FAB wiring | presentation | T11, T13 | M | Alex | Done |

**Sizing key:** S = 2-3h · S/M = 3-4h · M = 4-6h · L = 6-8h.

**Status values:** Todo → In Progress → In Review → Done.
