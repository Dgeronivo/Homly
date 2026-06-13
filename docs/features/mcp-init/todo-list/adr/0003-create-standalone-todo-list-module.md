---
status: Accepted
owner: "Alex"
reviewers: []
updated_at: "2026-06-13"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0003 — Create standalone todo-list module

- **Status:** Accepted
- **Date:** 2026-06-13
- **Deciders:** Alex

## Context

The todo-list feature needs a home in the Homly monolith. The project already contains `auth/`, `shopping/`, `home/`, and `core/` modules. Adding todo-list functionality requires choosing whether to create a new standalone module or extend an existing one. This decision shapes the HomlyDatabase entity list, the navigation graph, and all future import paths.

## Decision drivers

- QG-3 (architectural conformance): follow the existing feature-based module layout (CLAUDE.md + shopping module precedent).
- Module isolation: each feature has exactly one module boundary, preventing cross-feature coupling.
- Future iteration: the family module will add shared access to todo-items; a standalone module makes this boundary explicit.

## Considered options

1. **New standalone `todo-list` module** (`com.dgero.homly.todolist`) — a dedicated vertical slice mirroring the shopping module layout.
2. **Extend `shopping` module** — add todo-item functionality as a secondary feature inside the existing shopping package.
3. **Generic `items` module** — unify shopping and todo into a shared items module with a type discriminator.

## Decision outcome

**Chosen:** Option 1. A standalone module is the only option consistent with the project's feature-based layout. Option 2 would couple unrelated features and violate single-responsibility. Option 3 introduces premature abstraction — the two features have different sorting logic, different error variants, and may diverge further when family-sharing is added.

## Consequences

**Positive**
- Clean module boundary — future family-sharing extension touches only `todo-list` module internals.
- Consistent with `shopping/` module — zero learning curve for the developer.
- `HomlyDatabase` entity list grows by one entry (`TodoItemEntity`), with no impact on other modules.

**Negative**
- New module = new boilerplate (entity, DAO, repository, 5 use cases, ViewModel, Screen) — ~200–300 LOC of structure before business logic.

**Neutral**
- Kotlin package name is `com.dgero.homly.todolist` (hyphens not valid in package names).

## Links

- PRD: [[../PRD.md]]
- SAD: [[../sad.md]] §5
