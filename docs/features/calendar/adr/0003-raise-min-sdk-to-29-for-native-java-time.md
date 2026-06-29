---
status: Accepted
owner: "Alex"
reviewers: ["Tech Lead"]
updated_at: "2026-06-29"
feature_size: M
stage: "04-05"
ticket: ""
---

# 0003 — Raise minSdk to 29 for native java.time support

- **Status:** Accepted
- **Date:** 2026-06-29
- **Deciders:** Alex (Architect)

## Context

Модуль calendar потребує роботи з датами і часом (`LocalDate`, `LocalTime`). На момент проектування `minSdk = 24` (Android 7.0), але `java.time.*` нативно доступні лише з API 26 (Android 8.0). Для роботи на API 24-25 потрібен або `coreLibraryDesugaring`, або обхід через raw `Long`. Рішення: підняти `minSdk` до 29 (Android 10), що відповідає мінімальному цільовому пристрою проєкту.

Source: [Android 10 API level 29 — developer.android.com](https://developer.android.com/about/versions/10/features)

## Decision drivers

- `java.time.LocalDate` / `LocalTime` нативно доступні з API 26+ без будь-яких додаткових залежностей
- Prototype-продукт: цільова аудиторія — одна сім'я з сучасними пристроями (Android 10+)
- Спрощення: не потрібен `coreLibraryDesugaring` (зайва залежність і крок збірки)
- Android 10 (API 29) займає значну частину активних пристроїв (2025+)

## Considered options

1. **Raise minSdk to 29** — java.time доступні нативно; жодних додаткових налаштувань.
2. **Keep minSdk = 24 + coreLibraryDesugaring** — підтримка Android 7.0+; +1 залежність + `isCoreLibraryDesugaringEnabled = true`.
3. **Keep minSdk = 24 + epoch millis (Long)** — нуль налаштувань; domain-модель з raw `Long` замість typed date/time.

## Decision outcome

**Chosen:** Option 1 — Raise minSdk to 29.

Для прототипу сімейного застосунку підтримка Android 7.0 (2016 р.) не є вимогою. Android 10 (2019 р.) охоплює практично всі сучасні пристроїв цільової аудиторії. Підняття minSdk — найчистіше рішення: type-safe domain-модель без зайвих залежностей.

## Consequences

**Positive**
- `LocalDate`, `LocalTime`, `LocalDateTime` доступні нативно скрізь в проєкті
- Немає `coreLibraryDesugaring` залежності і конфігурації
- Domain-модель типобезпечна і читабельна

**Negative**
- Пристрої з Android 7.0-9.0 (API 24-28) більше не підтримуються застосунком
- Зміна глобальна для всього app-модуля, не лише calendar

**Neutral**
- Змінити рішення пізніше (знизити minSdk) — означає або додати desugaring, або переробити date/time-шар

## Links

- PRD: [[../PRD.md]]
- SAD: [[../sad.md]] §2, §5
- Source: https://developer.android.com/about/versions/10/features
