# Auth Feature Changelog

## Ітерація 1 — Build & Config (2026-05-26)

### Зроблено
- `gradle/libs.versions.toml` — додано версії та бібліотеки: KSP `2.2.10-2.0.2`, Room `2.7.1`, DataStore `1.1.4`, Navigation Compose `2.9.0`, `kotlinx-coroutines-test 1.9.0`; плагін `ksp`
- `build.gradle.kts` (root) — оголошено KSP плагін `apply false`
- `app/build.gradle.kts` — застосовано KSP; додано всі нові deps (`room-runtime`, `room-ktx`, `room-compiler` як `ksp(...)`, `datastore-preferences`, `navigation-compose`, `coroutines-test`, `room-testing`); `buildConfig = true`; `buildConfigField AUTH_BACKEND = "LOCAL"`
- `AndroidManifest.xml` — `android:name=".HomlyApplication"`; `android:allowBackup="false"`
- `HomlyApplication.kt` — stub `class HomlyApplication : Application()` (AppContainer додається в Ітерації 3)
- `gradle.properties` — `android.disallowKotlinSourceSets=false`

### Проблеми та рішення

**Проблема 1**: KSP версія `2.2.10-1.0.29` не знайдена в репозиторіях.
- **Причина**: KSP2 (default з початку 2025) змінив схему версіонування з `{kotlin}-1.0.x` на `{kotlin}-2.0.x`. KSP1 не підтримує AGP 9.x і Kotlin 2.2+.
- **Рішення**: Змінено на `2.2.10-2.0.2` — актуальна KSP2 версія для Kotlin 2.2.10.

**Проблема 2**: `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` (AGP 9.2.1).
- **Причина**: AGP 9.x заборонив реєстрацію джерел через `kotlin.sourceSets`; KSP 2.0.x ще використовує цей механізм.
- **Рішення**: `android.disallowKotlinSourceSets=false` у `gradle.properties` (офіційний workaround від AGP).

**Проблема 3**: Lint помилка `MissingClass` для `com.dgero.homly.HomlyApplication`.
- **Причина**: Manifest посилається на клас який ще не існував; Lint блокує білд.
- **Рішення**: Створено stub `HomlyApplication.kt` з порожньою реалізацією (розширюється в Ітерації 3).

### Результат
`./gradlew build` — **BUILD SUCCESSFUL** (98 tasks, 0 errors, 0 test failures).
