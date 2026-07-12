---
status: Todo
owner: "Alex"
estimate: S
deps: []
---

# T01 — Domain foundation: `CalendarEvent`, `CalendarError`, `CalendarLimits`

**Links:** [SAD §5](../sad.md#5-building-block-view) (package layout) · [PRD §8](../PRD.md#8-open-questions) (MAX_EVENTS=100)

## Scope

Create the pure-Kotlin domain types with no Android SDK / Room / Compose dependency:

- `domain/model/CalendarEvent.kt` — `id`, `userId`, `title`, `date: LocalDate`, `isAllDay`, `startTime: LocalTime?`, `endTime: LocalTime?` (per SAD §5).
- `domain/error/CalendarError.kt` — `sealed class`: `EmptyTitle | TitleTooLong | EndNotAfterStart | EventLimitReached` (per SAD §5, §8).
- `domain/CalendarLimits.kt` — `object` with `const val MAX_EVENTS = 100`.

No use cases, no validator, no persistence in this task — only the shapes other tasks depend on.

## Out of scope

Validation logic (→ T02), persistence (→ T03-T05).

## DoD

- Files compile as part of the `calendar` package.
- `CalendarEvent` fields match SAD §5 exactly (types, nullability).
- No import of `android.*`, `androidx.room.*`, or `androidx.compose.*` in these files.
