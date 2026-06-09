---
status: Confirmed
owner: "Alex"
reviewers: []
updated_at: "2026-06-09"
feature_size: M
stage: "01"
ticket: ""
value_score:
  rice: 2
  state: confirmed
  confirmed_at: "2026-06-09"
feasibility_state: confirmed
---

<!-- Stage 01 → see SDLC/plugin/skills/interview/SKILL.md -->
<!-- Why: capture the idea before it's forgotten or retold incorrectly -->

# Idea Brief — mcp-init: Сімейний планувальник у одному місці

## 1. Raw idea

«Сімейний планувальник у одному місці.»

Застосунок для збирання повторюваних сімейних справ: календар подій, список покупок, список справ. Ці сутності поділяються між користувачами, об'єднаними у сім'ю. Сім'я перестає вести ці справи в Telegram-чатах і пам'яті — все в одному місці зі спільним доступом і синхронізацією між пристроями.

## 2. Problem

Сімейні справи (покупки, події, todo) розкидані по Telegram-чатах, закріплених повідомленнях та пам'яті — структурованого спільного простору немає. Члени сім'ї не бачать актуальний стан одне одного в реальному часі, а дані губляться в потоці повідомлень. Координатор сім'ї несе весь mental load, бо тільки він знає де що «записано».

## 3. Users

**Сегмент:** всі члени сім'ї, що живуть разом — батьки, можливо підлітки та старше покоління.

**Хто страждає найбільше:** головний координатор (зазвичай один з батьків), який ввижає Telegram-чат, нагадує іншим і тримає актуальний список покупок у голові.

**Частота болю:** щоденна — покупки, тижнева — координація подій і справ.

**Розмір сім'ї:** 2–6 осіб на household.

## 4. Why now

Прототип потрібен для валідації ядра продукту на реальній сім'ї (одна family-група). Тригер: поточний workflow (Telegram + пам'ять) не структурований — події губляться в чаті, список покупок не зберігається після походу в магазин, todo-items не мають стану «виконано». **Критерій успіху:** сім'я перестала використовувати Telegram для покупок, подій та справ.

## 5. Out of scope

- Push-нотифікації (заплановано на наступну фазу)
- Голосовий ввід

## 6. Competitive analysis

| # | Product · URL | Features | Value (1–5) | Gap |
|---|---|---|---|---|
| 1 | Cozi · [cozi.com](https://www.cozi.com/) | Shared calendar; real-time shopping lists; to-do lists; meal planning | Cal: 4 · Shop: 4 · Todo: 3 | Ключові функції за paywall (Cozi Gold); meal planning відволікає від ядра; немає household як концепції |
| 2 | OurHome · [ourhomeapp.com](http://ourhomeapp.com/) | Shared calendar; grocery lists; chore assignment; gamified rewards | Cal: 3 · Shop: 3 · Todo: 4 | Орієнтований на дітей та гейміфікацію; ядро за Premium; погана підтримка дорослих координаторів |
| 3 | FamilyWall · [familywall.com](https://www.familywall.com/en/index.html) | Calendar; task lists; location sharing; in-app messaging; expense tracking | Cal: 3 · Shop: 2 · Todo: 3 | Слабкий shopping list; feature sprawl (location, expenses, docs) без глибини; Calendar sync за платно |
| 4 | Any.do Family · [any.do/en/family](https://www.any.do/en/family) | Shared task lists; auto-categorized grocery; calendar + sync; up to 5 members | Cal: 4 · Shop: 4 · Todo: 4 | Немає household-first UX — ті самі screens що для individual; обмеження 5 членів; немає поняття «дім» |
| 5 | Todoist + Google Calendar · [todoist.com](https://www.todoist.com/) | Shared projects; assignable tasks; calendar integration | Cal: 5 · Shop: 1 · Todo: 4 | Два окремих застосунки; shopping list відсутній; немає концепції household group |

**Ключовий gap:** жоден з п'яти конкурентів не моделює household (сім'ю, що живе разом) як першокласну сутність. Shopping list — найслабша фіча у всіх крім Cozi/Any.do, і жоден не інтегрує її з calendar events.

*Дата пошуку: 2026-06-09. Query: «family planner app android 2026», «family organizer app shopping list calendar todo».*

## 7. Strategic approaches

### Approach A — One Screen, Three Lists
- **Thesis**: Єдиний спільний екран з нульовою кривою навчання — звичка формується раніше, ніж з'являються складнощі.
- **For whom**: Батько/мати, що координує через Telegram — отримує миттєве полегшення.
- **Outcome metric**: % щотижневих координаційних задач у застосунку vs Telegram — 0% → 70% за 4 тижні.
- **Key trade-off**: Без персоналізації та household-моделі — coordinator не отримає повної заміни Telegram.
- **Effort signal**: S
- **Recommended?**: ◯

### Approach B — Telegram Replacement Done Right
- **Thesis**: Такий же миттєвий, як груп-чат, але організований — нічого не губиться й не забувається.
- **For whom**: Household coordinators (батьки, що ведуть чат).
- **Outcome metric**: Telegram-повідомлення для логістики на тиждень — ~30 → ≤5 за 4 тижні.
- **Key trade-off**: Framing «Telegram replacement» — positioning trap; Telegram виграє на швидкості та звичці без нотифікацій.
- **Effort signal**: M
- **Recommended?**: ◯

### Approach C — One Home, Always in Sync
- **Thesis**: Єдиний спільний простір де вся сім'я бачить що відбувається, що потрібно купити і що ще треба зробити — без пошуку в чат-стрічці, з синхронізацією між пристроями.
- **For whom**: Головний організатор домашнього господарства, що несе весь mental load координації.
- **Outcome metric**: Активні Telegram-треди для логістики — 5+ → 0 за 4 тижні після adoption.
- **Key trade-off**: Покриває всі 3 use-case, але жоден не на рівні спеціалізованого додатку (наприклад, немає recurring events, smart shopping suggestions).
- **Effort signal**: M
- **Recommended?**: ●

## 8. Multi-perspective feedback

### Engineer
**Approach A:**
- Найменша integration surface — sync loop найпростіший для reasoning і тестування.
- Єдиний shared-простір потребує чіткої data model boundary від початку або migration cost зростає.
- Без offline-стратегії (Effort S) — якщо sync layer падає, продукт відразу програє Telegram.

**Approach B:**
- «Миттєвість як чат» вимагає двох конкуруючих consistency models в одному шарі даних — найвищий архітектурний ризик.
- Onboarding friction прихована: UX має бути суворо кращим за першу сесію, інакше abandonment.
- Effort M може бути недооцінено, якщо real-time conflict resolution для всіх трьох domains в scope.

**Approach C:**
- Три concurrent shared state machines (calendar + shopping + todo) множать conflict surface — потрібна ізольована стратегія per domain.
- «Always in sync» встановлює high correctness bar: будь-яка видима inconsistency руйнує довіру швидше, ніж у простішому продукті.
- Offline поведінка різна для calendar і shopping — одна unified offline strategy не покриває обидва.

### Executive
**Approach A:**
- Найшвидший шлях до success metric (сім'я відкрила застосунок замість Telegram).
- Не займає household-first niche — delivers a list app, not a household OS — стратегічний moat втрачено.
- Цінний як v1 wedge, але не як standalone product strategy.

**Approach B:**
- Framing «Telegram replacement» — positioning trap; Telegram виграє на breadth і ubiquity.
- Any.do Family вже займає суміжний простір — без чіткого wedge ризик потрапити у crowded mid-tier.

**Approach C:**
- Займає незайняту niche: household як першокласна сутність — жоден конкурент не має цього (§6).
- «Functional depth that beats Telegram» — правильна планка: не feature parity з calendar app, а корисніший за груп-чат.
- Найсильніший retention фундамент: fully migrated household = highly sticky unit з природним word-of-mouth.

### UX-researcher
**Approach A:**
- Нульовий onboarding cost — бабуся і підліток можуть використовувати без туторіалу.
- Single screen з трьома списками ризикує стати візуально перевантаженим при зростанні даних.
- Пасивні члени сім'ї не мають причини відкривати застосунок без push і без ownership.

**Approach B:**
- Double-edged bet: знижує концептуальний стрибок від Telegram, але invite comparison на speed, де Telegram виграє.
- Highest adoption risk: будь-який gap у порівнянні з Telegram сприймається як regression.

**Approach C:**
- Wide surface area для нових користувачів — потрібні guided onboarding або contextual empty states.
- Discoverability є core design challenge: три модалності в одному додатку потребують clear navigation model.
- «Beats Telegram» — strong success criterion, що мотивує thorough implementation, але кожен missing feature відчувається як gap.

### Synthesis matrix

|          | Engineer | Executive | UX |
|----------|:--------:|:---------:|:--:|
| Approach A | + Найпростіший sync | 0 Wedge без moat | + Zero friction |
| Approach B | − Dual consistency; highest risk | − Positioning trap | − Chat-not-chat confusion |
| Approach C | 0 Три domain-и; manageable | + Незайнята household-first niche | 0 Wide surface; потрібен onboarding |

## 9. Trade-offs and edge cases

### Trade-offs per approach

| Approach | Pros | Cons |
|---|---|---|
| A | Найшвидший до звички; S effort; safe sync | Не витісняє Telegram для координаторів; пасивні члени не залучаються |
| B | Знайомий перехід від Telegram; low conceptual jump | Positioning trap; найскладніший sync; успадковує хаос чату |
| C | Займає незайняту household-first niche; 3 pillar coverage; найкращий retention | Найширша onboarding surface; потребує guided onboarding при UX |

### Edge cases

1. **Часткове adoption**: один член сім'ї відмовляється переходити — coordinator підтримує дві системи одночасно.
2. **Silent edit collision**: двоє редагують один і той самий shopping item одночасно — один запис зникає без попередження.
3. **Destructive undo**: дитина або новий user видаляє всі events без підтвердження — даних немає у всіх членів сім'ї.
4. **Offline divergence**: один член редагує список offline, синхронізація мовчки обирає переможця.
5. **Account recovery lockout**: власник household-групи втрачає доступ — решта членів заблоковані на manage-рівні.
6. **Motivation decay**: на 21–28 день вторинні users перестають відкривати застосунок — coordinator залишається один.
7. **Invite friction**: deep link для приєднання до family не спрацьовує на Android у Telegram in-app browser — invite acceptance rate падає нижче 40%.

## 10. Risks

**Топ ризик (devil's advocate):** Часткове прийняття — якщо хоч один член сім'ї не переходить, coordinator змушений підтримувати обидва канали одночасно. Застосунок стає додатковою роботою замість заміни Telegram. Виявляється через retention drop у primary user на тиждень 4–5.

**Інші ризики:**
- Shopping list desync at checkout: два члени одночасно редагують список — один не бачить змін іншого в магазині.
- Offline conflict resolution без visible feedback руйнує довіру до sync після першого ж непорозуміння.
- Invite flow failure блокує onboarding технічно несхильних членів сім'ї (старше покоління).

## 11. RICE — Claude proposed

- **Reach (R)**: 4 — одна сім'я (приватний прототип, 4 члени). Цитата §3 Users.
- **Impact (I)**: 2 — замінює щоденну координацію: покупки + події + справи (significant daily habit change). Цитата Executive perspective §8 + §2 Problem.
- **Confidence (C)**: 0.7 — біль підтверджений (Telegram usage); auth + shopping вже збудовані; TBD: чи перейдуть всі члени сім'ї, cross-device sync підхід. Цитата §15 Open questions.
- **Effort (E)**: 3 person-weeks — events ~0.5 тиж + todo ~0.5 тиж + family/household model ~2 тиж. Цитата Effort signal Approach C §7.
- **RICE = 4 × 2 × 0.7 / 3 ≈ 2**
- **State**: confirmed

*Примітка: низький RICE відображає приватний прототип (R=4). Product-scale RICE (R=400+, E=9) ≈ 62.*

## 12. Feasibility — Claude proposed

- [☑] **Tech**: auth і shopping вже збудовані з Compose/ViewModel/Repository патерном — events і todo структурно ідентичні. Family/household потребує нового shared-доступу, але архітектурний патерн той самий.
- [☑] **Skills**: команда задеплоїла shopping (4 ітерації) і auth за 2 дні — Kotlin + Compose + ViewModel + Repository доведені. Ті самі скіли потрібні для events, todo, family.
- [☑] **Time**: 2–3 тижні для залишку, підтверджено темпом (shopping + auth за 2 дні). Cross-device sync — eventual consistency, не real-time — реалістично в рамках Effort M.
- **State**: confirmed

## 13. Recommendation

**Selected: Approach C — One Home, Always in Sync** ●

Approach C обрано на основі чотирьох upstream секцій:

1. **RICE = 2** (§11) — прототип для одної сім'ї; feasibility підтверджена (§12, 3/3 ☑).
2. **Competitive gap** (§6): жоден з 5 конкурентів не моделює household як першокласну сутність — Any.do найближчий, але обмежений 5 членами і не має household-first UX. Цю niche займає Approach C.
3. **Executive ++ для C** (§8 synthesis matrix): «займає незайняту household-first niche; найсильніший retention foundation; functional depth that beats Telegram — правильна планка». Approach A отримав Executive 0 (wedge без moat); Approach B — Executive − (positioning trap).
4. **Feasibility 3/3 ☑** (§12): tech + skills + time підтверджені реальним темпом доставки.

Доповнення від user: **family** стає окремою фічею (не просто auth scope) — 4 фічі прототипу: events, todo, shopping, family. Всі розробляються паралельно, а не послідовно.

**Locked-in pointer**: PRD розробляє всі 4 фічі (events, todo, shopping, family) як рівноцінні модулі; family/household — першокласна сутність зі спільним доступом і invite flow; cross-device sync — eventual consistency (не real-time).

## 14. Parked & rejected approaches

| # | Approach | Status | Reason | Revisit trigger |
|---|---|:---:|---|---|
| A | One Screen, Three Lists | parked | Wedge без household moat; не витісняє Telegram для coordinators; пасивні члени не залучаться | Якщо cross-device sync виявиться надто складним для прототипу |
| B | Telegram Replacement Done Right | rejected | Positioning trap (chat-not-chat); highest UX abandonment risk; Any.do вже займає суміжний простір | Якщо команда вирішить додати real-time collaboration як окрему фічу |

## 15. Open questions

- [ ] Яка архітектура cross-device sync? (local DB + periodical export vs. cloud-based) — owner: Dev, due: до початку family feature
- [ ] Скільки членів у family максимум? — owner: Alex, due: до write-prd
- [ ] Invite flow: QR-код чи посилання? — owner: Alex, due: до write-prd
- [ ] Чи потрібен дитячий режим (child view з обмеженим доступом)? — owner: Alex, due: до write-prd

## Related

- Глосарій: [docs/features/mcp-init/CONTEXT.md](CONTEXT.md)
- Реалізовані фічі: `app/src/main/java/com/dgero/homly/shopping/` (shopping-list), `app/src/main/java/com/dgero/homly/auth/` (auth)
- Наступний крок: `sdlc:write-prd mcp-init`

## DoD self-check

- [x] 15 sections present
- [x] No anti-pattern terms (Postgres/Redis/Kafka/Room/Firebase)
- [x] Length ≤ 5 pages (~2200 words)
- [x] Frontmatter status: Confirmed
- [x] RICE confirmed (state: confirmed)
- [x] Feasibility confirmed (state: confirmed)
- [x] Recommendation present with rationale citing 4 upstream sections (§6, §8, §11, §12)
