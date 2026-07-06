# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A family planning app (Kotlin + Jetpack Compose) for storing routines and plans in one place.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full architecture description.

## Task Execution Flow

When given a new requirement:

**Never write any code without explicit user confirmation of the plan.**

1. **Analyze & Plan** — study the existing code, identify ambiguities, state the approach and patterns to be used, and produce a step-by-step plan. Wait for confirmation before writing any code.
2. **Implement & Test** — write the feature following the agreed plan, cover it with unit tests (and instrumented tests where needed).
3. **Review** — review the written code for correctness, edge cases, and consistency with the architecture; refactor if needed.
4. **Rebuild** — run `make rebuild` to verify the project compiles and all tests pass.

## Build & SDK

- **Min SDK**: 29 (Android 10) — do not use APIs below this without a compatibility check. Raised from 24 for native `java.time` support (see [ADR-0003](docs/features/calendar/adr/0003-raise-min-sdk-to-29-for-native-java-time.md)).
- **Target/Compile SDK**: 36 (Android 15).
- **Build**: `make build`
- **Unit tests**: `make test`
- **Instrumented tests**: `make connected-test` (requires a connected device or emulator)
- **Clean**: `make clean` — fixes IDE "Conflicting overloads" and stale cache errors; run before rebuilding when the IDE shows phantom errors.
- **Rebuild**: `make rebuild` — clean then build.

## Dependencies

Managed via `gradle/libs.versions.toml` (Version Catalog). Always add new dependencies there, not directly in `build.gradle.kts`.

## Code Style

- Kotlin with `kotlin.code.style=official` (4-space indentation, standard Kotlin conventions).
- Compose: one Composable per file when the composable is a full screen; small reusable components can share a file.
- No XML layouts — Compose only.
