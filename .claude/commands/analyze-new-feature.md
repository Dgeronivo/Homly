---
description: analyzing a new feature request for a family planning Android app (Kotlin + Jetpack Compose).Goal is to produce a thorough feature analysis document.
---

## Input

Feature description: $ARGUMENTS

## Step 1 — Clarify ambiguities

Before analyzing, identify anything in the description that is unclear, contradictory, or missing. If you find such items, use `AskUserQuestion` to ask the user — group all questions into a single call (up to 4 questions). Only skip this step if the description is fully unambiguous.

## Step 2 — Analyze the feature

Analyze the feature across these dimensions:

- [ ] 1. **Summary** — one-paragraph plain-language description of the feature.
- [ ] 2. **User stories** — 3–5 "As a [user], I want [goal] so that [benefit]" statements.
- [ ] 3. **Scope** — what is explicitly in scope and what is explicitly out of scope.
- [ ] 4. **Architecture impact** — which layers are affected (UI, domain, data), which existing modules change, what new modules/classes are needed. Reference the project's Clean Architecture (see ARCHITECTURE.md).
- [ ] 5. **Data model** — new or changed entities, fields, relationships.
- [ ] 6. **UI/UX** — screens, navigation changes, Compose components needed.
- [ ] 7. **Edge cases & risks** — at least 3 non-obvious edge cases or technical risks.
- [ ] 8. **Dependencies** — new libraries or permissions required.
- [ ] 9. **Testing strategy** — what unit tests, instrumented tests, and manual test cases are needed.
- [ ] 10. **Open questions** — any remaining decisions that need a product or design answer.

## Step 3 — Write the output file

Derive a kebab-case slug from the feature name (e.g. "Recurring Tasks" → `recurring-tasks`).

Write the full analysis as a Markdown document to `plans/<slug>/<slug>-analysis.md` (e.g. `plans/recurring-tasks/recurring-tasks-analysis.md`). Create the `plans/<slug>/` directory if it does not already exist — this keeps each feature's analysis, plan, and changelog together under one directory, matching the existing layout (e.g. `plans/auth-init/`, `plans/shopping-list/`).

**Write the entire output document in Ukrainian language**. This applies to all section content and the summary you give the user afterwards.

The document must start with:
```
# Feature Analysis: <Feature Name>
_Analyzed: <today's date>_
```

Then include all sections from Step 2 with `##` headings.

After writing the file, tell the user the filename and give a one-sentence summary of the most important architectural decision.

## Step 4 — Maintain a changelog during implementation

When the plan for this feature is later implemented, keep a changelog at `plans/<slug>/<slug>-changelog.md` (matching the existing layout, e.g. `plans/auth-init/auth-changelog.md`). Create it on the first iteration if it does not exist.

Update the changelog **as you go**, not only at the end:

- Record **each iteration** as its own dated entry (`## Iteration N — <date>`), describing what was built and which part of the plan it covers.
- Log every **problem encountered** and how it was resolved (build/test failures, design changes, deviations from the plan, blockers).
- Note any decisions that diverge from the analysis or plan, with a short reason.

**Write the changelog in Ukrainian language**, consistent with the analysis document.

### Execution Pipeline (how to implement each iteration)

Each iteration runs through the following loop. Throughout, `<slug>` is the feature slug, the plan lives at `plans/<slug>/<slug>-plan.md`, and the changelog at `plans/<slug>/<slug>-changelog.md`.

- [ ] 1. **Iter subagent** — implements the iteration, finishes with `./gradlew build` (or `make build`), and returns a summary plus `BUILD SUCCESSFUL` or `BUILD FAILED + cause`.

- [ ] 2. **If BUILD FAILED**:
   - [ ] Record the problem in the changelog under the current iteration's block.
   - [ ] Spawn a fix subagent → repeat from step 1.

- [ ] 3. **Reviewer subagent** — reads the written files plus the iteration's requirements from `plans/<slug>/<slug>-plan.md`, and returns `✅ approved` or `❌ [specific problems]`.

- [ ] 4. **If ❌**:
   - [ ] Record the problems in the changelog as "Post-review findings".
   - [ ] Spawn a fix subagent → repeat from step 1.

- [ ] 5. **If ✅**:
   - [ ] Write the summary into the changelog.
   - [ ] Commit: `feat(<slug>): iteration N — <description>`.
   - [ ] Move on to the next iteration.

The loop continues until every iteration is approved by the reviewer.
