# Homly

A family planning app for Android that keeps routines, events, and daily tasks in one shared place.

## Features

- **Calendar** — create and view events, get reminders before they happen
- **To-do list** — add tasks with deadlines, visible to all family members
- **Shopping list** — shared grocery and purchase list across the family
- **Family mode** — each family member can see everyone else's events and lists in real time
- **Voice input** — add items hands-free using keyword phrases (e.g., *"Buy sugar 1kg"* adds to the shopping list; *"Event, dentist, Thursday 12:00"* creates a calendar event)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM, feature-based package structure
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)

## Project Structure

Each feature lives in its own directory:

```
com.dgero.homly/
├── calendar/       # Calendar and events
├── todo/           # To-do list
├── shopping/       # Shopping list
├── family/         # Family sharing and user management
├── voice/          # Voice input processing
├── core/           # Shared utilities and models
└── ui/theme/       # App-wide theme (colors, typography)
```

Every feature follows the same internal layout:

```
feature/
├── presentation/   # Composable screens and ViewModels
├── domain/         # Use cases and domain models
└── data/           # Repository, Room DAOs, data sources
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Android device or emulator running API 24+

### Build & Run

```bash
# Build the project
./gradlew build

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

Vocabulary:
PRD (Product Requirements Document)
