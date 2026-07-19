# Story 003 — Кнопка "Сьогодні" в календарі

*Дата створення: 2026-07-19*

## Вимога

Джерело: [designImprovements.md](designImprovements.md), розділ 3 (Календар), Варіант B.

> Наприклад, невеликий чіп "Today" для швидкого повернення до поточної дати, коли переглядаєш інший місяць.
> **Складність: низька** (кнопка + `onDateSelected(LocalDate.now())` + `onMonthChanged(YearMonth.now())`).

Реалізуємо тільки цей варіант зараз. Варіант A (badge з кількістю подій у клітинці дня) та Варіант C (категорії подій) — не в цій story. Ідея "список подій на сьогодні завжди видимий, окремо від обраного дня" (з того ж розділу) свідомо НЕ реалізується тут

---

## Дизайн

У `MonthHeader` (поруч із заголовком місяця та іконкою вибору місяця/року) з'являється невеликий чіп/кнопка **"Сьогодні"**. Напис — українською (на відміну від решти UI, де тексти англійською), за прямим рішенням власника продукту. Клік:
1. Перемикає видимий місяць на поточний (`YearMonth.now()`), навіть якщо користувач переглядає інший місяць.
2. Обирає сьогоднішній день (`LocalDate.now()`) і оновлює список подій знизу.

Чіп видимий завжди (не тільки коли переглядається інший місяць) — це простіше і без додаткової умовної логіки видимості.

---

## Зміни по файлах

### `calendar/presentation/CalendarViewModel.kt`
- Новий метод `onTodayClick()`:
  ```kotlin
  fun onTodayClick() {
      val today = LocalDate.now()
      _currentYearMonth.value = YearMonth.from(today)
      _selectedDate.value = today
      viewModelScope.launch { loadMonth(_currentYearMonth.value) }
  }
  ```
  Не перевикористовує `onMonthChanged` напряму, бо той через `clampToMonth` лишає день місяця з попереднього вибору — тут потрібна саме сьогоднішня дата, а не clamp.

### `calendar/presentation/CalendarScreen.kt`
- `MonthHeader`: додати параметр `onTodayClick: () -> Unit`, розмістити чіп/`TextButton("Сьогодні")` в `Row` поруч з іконкою `DateRange`.
- `CalendarContent` і `CalendarScreen`: прокинути `onTodayClick = viewModel::onTodayClick`.
- Оновити preview-и `MonthHeader`/`CalendarContent` під новий параметр.

**`CalendarUiState` не змінюється** — нових полів не потрібно, чіп завжди активний і не залежить від стану.

---

## Тести

### Unit test — доповнення `CalendarViewModelTest` (якщо існує) або новий тест-клас

| Тест | Перевіряє |
|---|---|
| `onTodayClick_setsCurrentYearMonthToNow` | зміна місяця на поточний з будь-якого іншого |
| `onTodayClick_setsSelectedDateToToday` | обраний день = сьогодні, а не clamp попереднього дня |
| `onTodayClick_reloadsEventsForCurrentMonth` | `selectedDayEvents`/`daysWithEvents` оновлюються під новий місяць |

Інструментальних тестів не потрібно — змін у Room/DAO немає.

---

## Definition of done

- [x] Кнопка/чіп "Сьогодні" видима на екрані календаря незалежно від переглянутого місяця
- [x] Клік повертає на поточний місяць і обирає сьогоднішній день одним переходом
- [x] `CalendarUiState` без нових полів
- [x] `make rebuild` — зелений
- [x] `make test` — новий/оновлений `CalendarViewModelTest` зелений
- [x] Усі зміни сторі закомічені одним комітом
