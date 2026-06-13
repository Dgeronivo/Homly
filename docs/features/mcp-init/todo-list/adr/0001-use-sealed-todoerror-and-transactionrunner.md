---
status: Accepted
owner: "Alex"
reviewers: []
updated_at: "2026-06-13"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0001 — Use sealed TodoError and TransactionRunner for domain validation

- **Status:** Accepted
- **Date:** 2026-06-13
- **Deciders:** Alex

## Context

The todo-list feature must enforce a ≤50-item-per-user limit atomically and communicate validation failures (empty name, too-long name, limit reached, cross-user access) to the presentation layer. The shopping module already uses a sealed error class and `TransactionRunner` for the same problem. This decision locks the error contract for the domain layer of the todo module.

## Decision drivers

- QG-1 (data integrity): the ≤50-item limit must be enforced atomically — no partial write can leave the list inconsistent (PRD §6 NFR).
- QG-3 (architectural conformance): new modules must follow the project's layered MVVM + Clean Architecture convention (CLAUDE.md).
- Existing shopping module pattern: `ShoppingError` sealed class + `TransactionRunner` is already proven in production.

## Considered options

1. **Sealed `TodoError` class + `TransactionRunner`** — explicit domain error type per variant; limit check + insert in one Room transaction.
2. **Exception-based errors** — throw typed exceptions from use cases; catch in ViewModel.
3. **Nullable return / Boolean flags** — use cases return `null` or `false` on failure; no explicit error type.

## Decision outcome

**Chosen:** Option 1. Sealed error class provides exhaustive-when at compile time, making it impossible to silently ignore an error variant in the ViewModel. `TransactionRunner` wraps `db.withTransaction {}` — the only correct way to atomically check the limit and insert without TOCTOU gaps. Exceptions (option 2) are harder to handle exhaustively in Compose; nullables (option 3) lose the error variant information.

## Consequences

**Positive**
- Compile-time exhaustive handling of all error variants in ViewModel `when` expressions.
- Atomic limit enforcement prevents race conditions even in future multi-coroutine scenarios.
- Consistent with `ShoppingError` — same mental model for developers.

**Negative**
- Every new error variant requires updating all `when` expressions across the module.
- `TransactionRunner` abstraction adds ~30 LOC of boilerplate to the data layer.

**Neutral**
- Migration to exception-based errors in a future iteration is possible but requires updating all ViewModel `when` blocks.

## Links

- PRD: [[../PRD.md]]
- SAD: [[../sad.md]] §4
