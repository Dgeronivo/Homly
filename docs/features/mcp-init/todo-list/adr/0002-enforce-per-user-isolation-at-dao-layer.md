---
status: Accepted
owner: "Alex"
reviewers: []
updated_at: "2026-06-13"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0002 — Enforce per-user isolation at the DAO layer

- **Status:** Accepted
- **Date:** 2026-06-13
- **Deciders:** Alex

## Context

Every todo-item belongs to exactly one user. The system must guarantee that a user can never read, toggle, edit, or delete a todo-item that belongs to another user, and must not reveal whether such an item exists (AC-10). The question is where in the layered architecture this userId filter is applied: at the database query level (DAO) or in the domain service (use case).

## Decision drivers

- QG-2 (authorization correctness): cross-user access must be structurally impossible, not just conventionally avoided (PRD AC-10, §6.1).
- QG-3 (architectural conformance): follow the established pattern from the shopping module, which applies `userId` filters at the DAO level.
- Defence-in-depth: even if a use case accidentally omits a userId check, the DAO must not return foreign rows.

## Considered options

1. **userId parameter in every DAO query (DAO layer)** — all SELECT, UPDATE, DELETE queries include `WHERE userId = :userId`; the DAO structurally cannot return a foreign row.
2. **Domain service (use case) filtering** — DAO returns all rows; use case filters by `userId` in-memory before returning to ViewModel.

## Decision outcome

**Chosen:** Option 1. Applying the filter at the DAO layer makes cross-user data leakage structurally impossible: no matter how a use case is written, the DB will never surface another user's items. Option 2 creates a window where a bug in a use case exposes all rows to any caller; it also loads all users' items into memory for filtering, which is wasteful and would worsen significantly once a remote backend is added.

## Consequences

**Positive**
- Cross-user access is structurally impossible — DAO enforces the boundary at the SQL level.
- Consistent with shopping module pattern — zero additional cognitive load for developers.
- Future-proof: when a remote backend is added, the same DAO-level filter pattern migrates cleanly.

**Negative**
- Every DAO method signature carries an extra `userId: Long` parameter.
- Integration tests must verify the filter is present (not purely a unit-test concern).

**Neutral**
- All DAO queries get an indexed `WHERE userId = :userId` clause; SQLite performance is unaffected at prototype scale.

## Links

- PRD: [[../PRD.md]]
- SAD: [[../sad.md]] §4
