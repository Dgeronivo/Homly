# Story 002 — Показати/приховати пароль (Login/Register)

*Дата створення: 2026-07-19*

## Вимога

Джерело: [designImprovements.md](designImprovements.md), розділ 2 (Аутентифікація), Варіант B.

> Зараз пароль завжди прихований (`PasswordVisualTransformation` без перемикача). Додати `IconButton` toggle видимості.
> **Складність: низька** (лише UI-стан у Composable, без змін ViewModel/domain).

Реалізуємо тільки цей варіант. Решту екрана Auth залишаємо без змін (правила валідації, hint-патерн — вважаємо достатньо функціональними).

---

## Дизайн

У полі **Password** з'являється trailing-іконка (кнопка) всередині `OutlinedTextField`, що перемикає видимість введеного тексту:
- прихований стан (за замовчуванням) — `PasswordVisualTransformation()`, іконка показує "показати"
- видимий стан — `VisualTransformation.None`, іконка показує "приховати"

**Технічне обмеження:** у проєкті підключено лише `material-icons-core` (`gradle/libs.versions.toml`), а `Icons.Filled.Visibility` / `VisibilityOff` доступні тільки в `material-icons-extended`. Додавати нову залежність заради однієї іконки — над-інженерія для цієї задачі. Замість цього — текстовий гліф у `IconButton`, як уже робиться в проєкті для інших кнопок (`Text("‹")` для back, `Text("✕")` для delete): наприклад `Text("👁")` / `Text("🙈")`, або лаконічний `TextButton` з написом "Show"/"Hide".

Стан видимості — суто UI, локальний до Composable (`var isPasswordVisible by remember { mutableStateOf(false) }`), без жодних змін у `LoginViewModel` / `RegisterViewModel` / `LoginUiState` / `RegisterUiState`.

---

## Зміни по файлах

### `auth/presentation/login/LoginScreen.kt`
- У `LoginContent`: локальний стан `isPasswordVisible`.
- `OutlinedTextField` для пароля: `visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()`.
- `trailingIcon = { IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) { Text(if (isPasswordVisible) "🙈" else "👁") } }`.
- Оновити preview-и (`LoginContentPreview`, `LoginContentWithErrorPreview`) — без змін сигнатури, стан локальний.

### `auth/presentation/register/RegisterScreen.kt`
- Той самий патерн, застосований до `RegisterContent` (поле пароля ідентичне Login).

**ViewModel/domain-шар не змінюється.**

---

## Тести

Змін у ViewModel/domain немає — юніт-тестів не додаємо. Візуальна поведінка (toggle) перевіряється вручну через Compose Preview / manual QA, оскільки увесь стан локальний до Composable і не впливає на бізнес-логіку.

---

## Definition of done

- [ ] На екранах Login і Register є кнопка перемикання видимості пароля
- [ ] Дефолтний стан — пароль прихований (без регресії існуючої поведінки)
- [ ] Нових залежностей (material-icons-extended) не додано
- [ ] `LoginViewModel`, `RegisterViewModel`, `LoginUiState`, `RegisterUiState` не змінені
- [ ] `make rebuild` — зелений
- [ ] Усі зміни сторі закомічені одним комітом
