# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A family planning app (Kotlin + Jetpack Compose) for storing routines and plans in one place.

## Architecture: Feature-Based Package Structure

Every feature lives in its own directory under `app/src/main/java/com/dgero/homly/`. Do not mix features together.

Each feature follows MVVM with these sub-packages:

```
feature/
├── presentation/   # Composable screens and ViewModels
├── domain/         # Business logic: use cases and domain models
└── data/           # Repository implementations, Room DAOs, data sources
```

Example layout:
```
com.dgero.homly/
├── calendar/
│   ├── presentation/
│   ├── domain/
│   └── data/
├── todo/
│   ├── presentation/
│   ├── domain/
│   └── data/
├── shopping/
├── family/
├── voice/
└── ui/theme/       # Shared theme (already exists)
```

Shared utilities or cross-feature models go in `core/` at the same level.

## Key Decisions

- **UI**: Jetpack Compose + Material3. Do not use XML layouts.
- **State**: Use `StateFlow` in ViewModels exposed as Compose `State` via `collectAsStateWithLifecycle()`.
- **Navigation**: Use Jetpack Navigation for Compose when adding multi-screen navigation.
- **One Activity**: Keep `MainActivity` as the single entry point; all screens are Composables.

## Task Execution Flow

When given a new requirement:

**Never write any code without explicit user confirmation of the plan.**

1. **Analyze & Plan** — study the existing code, identify ambiguities, state the approach and patterns to be used, and produce a step-by-step plan. Wait for confirmation before writing any code.
2. **Implement & Test** — write the feature following the agreed plan, cover it with unit tests (and instrumented tests where needed).
3. **Review** — review the written code for correctness, edge cases, and consistency with the architecture; refactor if needed.
4. **Rebuild** — run `./gradlew clean` then `./gradlew build` to verify the project compiles and all tests pass.

## Build & SDK

- **Min SDK**: 24 (Android 7.0) — do not use APIs below this without a compatibility check.
- **Target/Compile SDK**: 36 (Android 15).
- **Build**: `./gradlew build`
- **Unit tests**: `./gradlew test`
- **Instrumented tests**: `./gradlew connectedAndroidTest` (requires a connected device or emulator)
- **Clean**: `./gradlew clean` — fixes IDE "Conflicting overloads" and stale cache errors; run before rebuilding when the IDE shows phantom errors.

## Dependencies

Managed via `gradle/libs.versions.toml` (Version Catalog). Always add new dependencies there, not directly in `build.gradle.kts`.

## Code Style

- Kotlin with `kotlin.code.style=official` (4-space indentation, standard Kotlin conventions).
- Compose: one Composable per file when the composable is a full screen; small reusable components can share a file.
- No XML layouts — Compose only.
