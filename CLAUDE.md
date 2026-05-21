# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A family planning app (Kotlin + Jetpack Compose) for storing routines and plans in one place. Planned features:
- Calendar with events and reminders
- To-do list (shared between family members)
- Shopping list (shared between family members)
- Family mode — members can see each other's events and lists
- Voice input — keyword phrases trigger actions without a keyboard (e.g., "Buy sugar 1kg" adds to shopping list)

## Architecture: Feature-Based Package Structure

Every feature lives in its own directory under `app/src/main/kotlin/com/example/scheduler/`. Do not mix features together.

Each feature follows MVVM with these sub-packages:

```
feature/
├── presentation/   # Composable screens and ViewModels
├── domain/         # Business logic: use cases and domain models
└── data/           # Repository implementations, Room DAOs, data sources
```

Example layout:
```
com.example.scheduler/
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

## Build & SDK

- **Min SDK**: 24 (Android 7.0) — do not use APIs below this without a compatibility check.
- **Target/Compile SDK**: 36 (Android 15).
- **Build**: `./gradlew build`
- **Unit tests**: `./gradlew test`
- **Instrumented tests**: `./gradlew connectedAndroidTest` (requires a connected device or emulator)
- **Install on device**: `./gradlew installDebug`

## Dependencies

Managed via `gradle/libs.versions.toml` (Version Catalog). Always add new dependencies there, not directly in `build.gradle.kts`.

## Code Style

- Kotlin with `kotlin.code.style=official` (4-space indentation, standard Kotlin conventions).
- Compose: one Composable per file when the composable is a full screen; small reusable components can share a file.
- No XML layouts — Compose only.
