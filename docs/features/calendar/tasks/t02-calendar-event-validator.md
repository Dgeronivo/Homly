---
status: Todo
owner: "Alex"
estimate: S/M
deps: [t01-domain-foundation]
---

# T02 — `CalendarEventValidator`

**Links:** [PRD AC-07, AC-07b, AC-08, AC-09](../PRD.md#5-acceptance-criteria) · [SAD QG-2](../sad.md#10-quality-requirements) · [SAD §5](../sad.md#5-building-block-view)

## Scope

`domain/validation/CalendarEventValidator.kt` — validates a draft event and returns the matching `CalendarError` (from T01) or success:

- Empty title → `CalendarError.EmptyTitle` (AC-08)
- Title > 100 chars → `CalendarError.TitleTooLong` (AC-09)
- `endTime <= startTime` for timed events → `CalendarError.EndNotAfterStart` (AC-07)
- Event count check (`EventLimitReached`, AC-07b) is NOT this validator's job — it requires a repository count, which belongs to `CreateEventUseCase` (→ T07). Keep this validator pure (no repository dependency).

## Out of scope

Event-limit check (repository-dependent, → T07).

## DoD

- Unit test per `CalendarError` variant this validator can produce (QG-2 verify method: `CalendarEventValidatorTest`).
- Validator has zero dependency on Room/repository — pure function of a draft + existing state it's given.
