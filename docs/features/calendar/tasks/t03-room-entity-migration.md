---
status: Todo
owner: "Alex"
estimate: M
deps: [t01-domain-foundation]
---

# T03 — Room entity + `DateTimeConverters` + `Migration(3→4)`

**Links:** [ADR-0001](../adr/0001-use-proper-room-migration-for-calendar-events.md) · [ADR-0003](../adr/0003-raise-min-sdk-to-29-for-native-java-time.md) · [SAD §5](../sad.md#5-building-block-view) · [SAD Risks §11](../sad.md#11-risks-and-technical-debt)

## Scope

- `data/local/CalendarEventEntity.kt` — Room entity for table `calendar_events`, fields mirroring `CalendarEvent` (T01).
- `data/local/DateTimeConverters.kt` — `@TypeConverter`: `LocalDate` ↔ `Long` (epochDay), `LocalTime` ↔ `Int` (secondOfDay).
- `HomlyDatabase`: bump `version` 3 → 4, register `CalendarEventEntity` + `DateTimeConverters`, add explicit `Migration(3, 4)` with `CREATE TABLE calendar_events (...)` (per ADR-0001 — do not rely on `fallbackToDestructiveMigration`).

## Out of scope

DAO queries (→ T04).

## DoD

- Instrumented `MigrationTest` (per SAD §11 risk mitigation) verifies `Migration(3, 4)` applies cleanly against a v3 schema and the resulting schema matches Room's expected schema.
- Existing `todo_items` / `shopping_items` data survives the migration in the test (no destructive fallback triggered).
