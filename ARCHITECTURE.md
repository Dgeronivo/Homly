# Architecture

## Feature-Based Package Structure

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
