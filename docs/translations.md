# Переклади UI текстів (англійська → українська)

Це документ містить усі UI тексти з проекту, які потребують перекладу на українську мову.

## Authentication (Реєстрація та вхід)

### LoginScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 78 | "Sign In" | Заголовок екрану входу |
| 83 | "Login" | Label для поля логіну |
| 87 | "Only English letters and digits, min 3 characters" | Підтримуючий текст для поля логіну |
| 97 | "Password" | Label для поля пароля |
| 101 | "Min 4 characters, letters, digits and special characters" | Підтримуючий текст для поля пароля |
| 131 | "Login" | Текст кнопки входу |
| 135 | "Register" | Текст кнопки переходу до реєстрації |
| 162 | "Login can only contain letters and digits" | Повідомлення про помилку валідації |

### RegisterScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 78 | "Create Account" | Заголовок екрану реєстрації |
| 83 | "Login" | Label для поля логіну |
| 87 | "Only English letters and digits, min 3 characters" | Підтримуючий текст для поля логіну |
| 97 | "Password" | Label для поля пароля |
| 101 | "Min 4 characters, letters, digits and special characters" | Підтримуючий текст для поля пароля |
| 131 | "Register" | Текст кнопки реєстрації |
| 135 | "Login" | Текст кнопки переходу до входу |
| 162 | "Login can only contain letters and digits" | Повідомлення про помилку валідації |

## Home Screen (Домашня сторінка)

### HomeScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 97 | "Home" | Заголовок в TopAppBar |
| 100 | "Log out" | ContentDescription для кнопки виходу |
| 114 | "Calendar" | Заголовок картки календаря |
| 115 | "Today: $todayEventsCount events" | Підсумок для календаря (вставляється номер подій) |
| 115 | "No events today" | Підсумок для календаря, коли немає подій |
| 125 | "Shopping list" | Заголовок картки списку покупок |
| 126 | "$shoppingActiveCount left" | Підсумок для списку покупок (вставляється кількість) |
| 126 | "Empty" | Підсумок для списку покупок, коли він порожній |
| 132 | "Todo list" | Заголовок картки списку завдань |
| 134 | "$todoPendingCount pending" | Підсумок для списку завдань (вставляється кількість) |
| 135 | "All done" | Підсумок для списку завдань, коли всі завдання виконані |
| 136 | "Empty" | Підсумок для списку завдань, коли він порожній |
| 148 | "Log out?" | Заголовок діалогу підтвердження виходу |
| 156 | "Log out" | Текст кнопки підтвердження виходу |
| 161 | "Cancel" | Текст кнопки скасування в діалозі |

## Todo List Screen (Список завдань)

### TodoListScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 84 | "Todo list" | Заголовок в TopAppBar |
| 158 | "Active only" | Label для фільтра активних завдань |
| 164 | "Clear completed" | Текст кнопки очищення виконаних завдань |
| 190 | "Delete $completedCount completed tasks?" | Заголовок діалогу підтвердження видалення (вставляється кількість) |
| 192 | "Delete" | Текст кнопки видалення |
| 195 | "Cancel" | Текст кнопки скасування |
| 231 | "Add item" | Label для поля додавання завдання |
| 239 | "List is full (max ${TodoLimits.MAX_ITEMS} items)" | Повідомлення про досягнення ліміту (вставляється максимум) |
| 251 | "Add" | Текст кнопки додавання |
| 316 | "Your todo list is empty" | Текст коли список пустий |

## Shopping List Screen (Список покупок)

### ShoppingListScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 78 | "Shopping list" | Заголовок в TopAppBar |
| 139 | "Add item" | Label для поля додавання товару |
| 147 | "List is full (max ${ShoppingLimits.MAX_ITEMS} items)" | Повідомлення про досягнення ліміту (вставляється максимум) |
| 159 | "Add" | Текст кнопки додавання |
| 179 | "Date" | Label для фільтра сортування за датою |
| 184 | "A–Z" | Label для фільтра алфавітного сортування |
| 249 | "Your shopping list is empty" | Текст коли список пустий |

## Calendar Screen (Календар)

### CalendarScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 75 | "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" | Дні тижня у calendario (у масиві WEEKDAY_LABELS) |
| 100 | "Event limit reached (max ${CalendarLimits.MAX_EVENTS} events)" | Snackbar при досягненні ліміту подій (вставляється максимум) |
| 170 | "Add event" | ContentDescription для FAB кнопки |
| 240 | "Pick month and year" | ContentDescription для кнопки вибору місяця |
| 255 | "Сьогодні" | ContentDescription для кнопки "Сьогодні" (вже українська) |
| 301 | "Select month & year" | Заголовок діалогу вибору місяця/року |
| 317 | "OK" | Текст кнопки підтвердження |
| 322 | "Cancel" | Текст кнопки скасування |
| 503 | "No events for this day" | Текст коли у обраний день немає подій |
| 524 | "Delete this event?" | Заголовок діалогу видалення подій |
| 532 | "Delete" | Текст кнопки видалення |
| 537 | "Cancel" | Текст кнопки скасування |
| 570 | "All day" | Текст для позначення цілодневної подій |

## Add/Edit Event Screen (Додавання/редагування подій)

### AddEditEventScreen.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 97 | "Edit event" | Заголовок при редагуванні |
| 97 | "Add event" | Заголовок при додаванні |
| 115 | "Title" | Label для поля заголовка |
| 123 | "Date: ${uiState.date.format(DATE_FORMATTER)}" | Текст кнопки вибору дати (вставляється дата) |
| 132 | "All day" | Label для перемикача цілодневної подій |
| 139 | "Start: ${uiState.startTime?.format(TIME_FORMATTER) ?: "--:--"}" | Текст кнопки вибору часу початку (вставляється час) |
| 143 | "End: ${uiState.endTime?.format(TIME_FORMATTER) ?: "--:--"}" | Текст кнопки вибору часу завершення (вставляється час) |
| 166 | "Save" | Текст кнопки збереження |
| 219 | "OK" | Текст кнопки підтвердження у Date Picker |
| 223 | "Cancel" | Текст кнопки скасування у Date Picker |
| 251 | "OK" | Текст кнопки підтвердження у Time Picker |
| 251 | "Cancel" | Текст кнопки скасування у Time Picker |
| 288 | "End time must be after start time" | Текст помилки валідації часу |

## Error Messages (Повідомлення про помилки)

### AuthError.kt (Domain тексти помилок)
| Дефініція | Тип помилки |
|-----------|-----------|
| EmptyLogin | Поле логіну пусте |
| EmptyPassword | Поле пароля пусте |
| LoginTooShort | Логін менший за 3 символи |
| PasswordTooShort | Пароль менший за 4 символи |
| InvalidLoginChars | Логін містить недопустимі символи |
| InvalidPasswordChars | Пароль містить недопустимі символи |
| DuplicateAccount | Акаунт з таким логіном вже існує |
| InvalidCredentials | Невірні облікові дані |

### ShoppingError.kt
| Дефініція | Тип помилки |
|-----------|-----------|
| EmptyName | Назва товару пуста |
| NameTooLong | Назва товару занадто довга |
| LimitReached | Досягнуто максимум товарів у списку |

### TodoError.kt
| Дефініція | Тип помилки |
|-----------|-----------|
| EmptyTitle | Заголовок завдання пустий |
| TitleTooLong | Заголовок завдання занадто довгий |
| LimitReached | Досягнуто максимум завдань у списку |
| Unauthorized | Користувач не авторизований |

## Additional UI Elements (Дизайнерські варіанти)

### Components.kt (Загальні компоненти)
| Компонент | Текст | Контекст |
|-----------|-------|---------|
| HomlyHeroCard | "Calendar" | Приклад у Preview |
| HomlyHeroCard | "Today: 2 events" | Приклад у Preview |

### HomeDesignTerracottaHoney.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 113 | "Home" | Заголовок TopAppBar (дублюється) |

### Design Variations (Варіанти дизайну)

#### ShoppingDesignChecklistHero.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 51 | "Shopping list" | Заголовок (дублюється) |
| 68 | "In cart" | Заголовок картки |
| 69 | "$boughtCount of ${items.size} bought" | Підсумок (вставляються числа) |
| 82 | "Add item" | Label для поля |
| 98 | "Clear bought" | Текст кнопки |
| 141 | "Your shopping list is empty" | Текст, коли список пустий |

#### ShoppingDesignGroupedSections.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 50 | "Shopping list" | Заголовок (дублюється) |
| 74 | "Add item" | Label для поля |
| 80 | "Add" | Текст кнопки |
| 91 | "To buy" | Заголовок секції |
| 104 | "Bought" | Заголовок секції |
| 107 | "Clear" | Текст кнопки |

#### ShoppingDesignClassicCards.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 58 | "Shopping list" | Заголовок (дублюється) |
| 88 | "Date" | Label для фільтра (дублюється) |
| 93 | "A–Z" | Label для фільтра (дублюється) |
| 105 | "Add item" | Label для поля (як placeholder) |
| 125 | "Add" | Текст кнопки |

#### TodoDesignClassicCards.kt
| Строка | Текст | Контекст |
|--------|-------|---------|
| 57 | "Todo list" | Заголовок (дублюється) |
| 87 | "Active only" | Label для фільтра (дублюється) |
| 90 | "Clear completed" | Текст кнопки (дублюється) |
| 102 | "Add item" | Label для поля (як placeholder) |
| 122 | "Add" | Текст кнопки |
| 160 | "Progress: $completedCount of $totalCount done" | Текст прогреса (вставляються числа) |

## Resources

### strings.xml
| Дефініція | Текст |
|-----------|-------|
| app_name | "Homly" |

## Примітки для перекладача

1. **Полягаючи слова** (variables в текстах):
   - `$todayEventsCount` - кількість подій на сьогодні
   - `$shoppingActiveCount` - кількість активних товарів
   - `$todoPendingCount` - кількість незавершених завдань
   - `$completedCount` - кількість виконаних завдань
   - `${TodoLimits.MAX_ITEMS}` - максимум завдань (константа)
   - `${ShoppingLimits.MAX_ITEMS}` - максимум товарів (константа)
   - `${CalendarLimits.MAX_EVENTS}` - максимум подій (константа)
   - `${uiState.date.format(DATE_FORMATTER)}` - відформатована дата

2. **ContentDescription**: Тексти для `contentDescription` повинні бути описовими для екрану читання (accessibility).

3. **День тижня**: Скорочення дня тижня (Mon-Sun) повинні залишатися 3-символьні у скороченому форматі (Пн, Вт, Ср, Чт, Пт, Сб, Нд) або подібні залежно від локалізації.

4. **Динамічні тексти**: Багато текстів містять динамічну інформацію (цифри, дати, часи) - це повинно залишатися на місцях для вставки змінних.

5. **Дублювання**: Деякі тексти повторюються в кількох місцях (напр. "Home", "Shopping list", "Add item"). Це нормально - рекомендується перекладати їх однаково у всіх місцях для консистентності.

## Реалізація локалізації

Щоб імплементувати переклади, потрібно:

1. **Переместить текст в strings.xml**: Замінити всі жорстко закодовані тексти на посилання на ресурси:
   ```kotlin
   Text(stringResource(R.string.sign_in))
   ```

2. **Додати локалізовані ресурси**:
   - `app/src/main/res/values-uk/strings.xml` — Українські переклади
   - `app/src/main/res/values-uk/strings.xml` (uk = Ukrainian locale)

3. **Формат у strings.xml**:
   ```xml
   <string name="sign_in">Вхід</string>
   <string name="login">Логін</string>
   <string name="event_limit_reached">Досягнуто максимум подій (макс %d подій)</string>
   ```
   Для текстів з параметрами використовувати `%d`, `%s` тощо.

## Статус перекладу

- [ ] Аутентифікація (Login/Register)
- [ ] Домашня сторінка (Home)
- [ ] Список завдань (Todo)
- [ ] Список покупок (Shopping)
- [ ] Календар (Calendar)
- [ ] Повідомлення про помилки (Error Messages)
