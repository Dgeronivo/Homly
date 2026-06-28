---
status: Confirmed
owner: "Alex"
reviewers: []
updated_at: "2026-06-28"
feature_size: M
stage: "01"
ticket: ""
value_score:
  rice: 2
  state: confirmed
  confirmed_at: "2026-06-28"
feasibility_state: confirmed
---

<!-- Derived from docs/features/mcp-init/idea-brief.md — scoped to calendar/events feature -->

# Idea Brief — calendar: Events у сімейному планувальнику

## 1. Raw idea

«Спільний сімейний календар подій у одному місці.»

Члени сім'ї створюють події (візит до лікаря, виїзд, день народження) і бачать їх у спільному calendar-view. Події не губляться в Telegram-чатах — вони зберігаються структуровано зі статусом, датою і часом.

## 2. Problem

Сімейні події (візити, виїзди, зустрічі) координуються через Telegram-повідомлення або пам'ять координатора. Немає структурованого спільного простору: хто/що/коли. Члени сім'ї дізнаються про події пізно або не дізнаються взагалі. Координатор несе весь mental load.

## 3. Users

**Сегмент:** всі члени сім'ї — батьки, можливо підлітки та старше покоління.

**Хто страждає найбільше:** головний координатор (зазвичай один з батьків), що нагадує решті про заплановані події.

**Частота болю:** тижнева — координація подій та виїздів.

**Розмір сім'ї:** 2–6 осіб на household.

## 4. Why now

Прототип для валідації: сім'я перестала координувати події через Telegram. Тригер: події губляться в чаті, немає структури «хто/що/коли». **Критерій успіху:** сім'я перестала писати про події в Telegram.

## 5. Out of scope

- Push-нотифікації
- Recurring / повторювані події
- Синхронізація з Google Calendar / Apple Calendar
- Голосовий ввід
- Нагадування (reminders)

## 6. Competitive analysis

*(Спадковано з mcp-init idea-brief §6 — events-релевантна частина)*

| # | Product | Events strength | Gap |
|---|---|---|---|
| Cozi | cozi.com | Shared calendar | Ключові функції за paywall |
| Any.do Family | any.do/en/family | Calendar + sync | Немає household-first UX |
| Todoist + Google Calendar | — | Calendar integration | Два окремих застосунки |

**Gap:** жоден не моделює household як першокласну сутність з events як центральним об'єктом.

## 7. Strategic approach

**Обрано Approach C — One Home, Always in Sync** (з mcp-init idea-brief §13).

Calendar/events є одним із чотирьох рівноцінних модулів: events, todo, shopping, family. Спільна модель даних — per-user зараз, shared via family-модуль у наступній ітерації.

## 8. Feasibility

- [☑] **Tech**: shopping і todo вже збудовані з Compose/ViewModel/Repository — events структурно ідентичні плюс datetime-поля.
- [☑] **Skills**: Kotlin + Compose + ViewModel + Repository доведені попередніми фічами.
- [☑] **Time**: ~0.5 тиж для domain + data шарів; ~0.5 тиж для calendar UI (місячна сітка + список подій дня).
- **State**: confirmed

## 9. RICE

- **Reach (R)**: 4 — одна сім'я (4 члени).
- **Impact (I)**: 2 — замінює щотижневу координацію подій.
- **Confidence (C)**: 0.7 — біль підтверджений; auth + shopping + todo вже збудовані.
- **Effort (E)**: 1 person-week.
- **RICE = 4 × 2 × 0.7 / 1 ≈ 5.6**
- **State**: confirmed

## 10. Open questions

- [ ] Скільки подій максимум на день / на місяць? — owner: Alex, due: до write-prd
- [ ] Чи потрібен перегляд по тижнях (week view) або тільки місяць + день-список? — owner: Alex, due: до write-prd
- [ ] Чи може подія тривати кілька днів (multi-day)? — owner: Alex, due: до write-prd

## Related

- Глосарій: [docs/features/calendar/CONTEXT.md](CONTEXT.md)
- Батьківський idea-brief: [docs/features/mcp-init/idea-brief.md](../mcp-init/idea-brief.md)
- Реалізовані фічі: `app/src/main/java/com/dgero/homly/shopping/`, `app/src/main/java/com/dgero/homly/todolist/`
- Наступний крок: `sdlc:write-prd calendar`
