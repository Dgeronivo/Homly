# WheelPickerCompose

Бібліотека для колесо-подібних (wheel/scroll) пікерів дати й часу в Jetpack Compose.
Використовується в `AddEditEventScreen.kt` для вибору часу події замість стандартного
`androidx.compose.material3.TimePicker`.

- Репозиторій: [github.com/commandiron/WheelPickerCompose](https://github.com/commandiron/WheelPickerCompose)
- Дистрибуція: JitPack (`com.github.commandiron:WheelPickerCompose`)
- Версія в проєкті: `1.1.11` (`gradle/libs.versions.toml` → `wheelPickerCompose`)
- Мін. API бібліотеки: 21 (наш `minSdk = 29`, тож обмежень немає)

> **Як здобуто цей опис:** офіційний README документує лише `WheelDateTimePicker`,
> `WheelDatePicker`, `WheelTimePicker` на рівні "one-liner" прикладів. Повний список
> параметрів (і сам факт існування нижчих рівнів `WheelTextPicker`/`WheelPicker`) тут
> відновлено декомпіляцією реально завантаженого Gradle'ом `.aar`
> (`~/.gradle/caches/modules-2/files-2.1/com.github.commandiron/WheelPickerCompose/1.1.11/.../WheelPickerCompose-1.1.11.aar` →
> `classes.jar`) — без `javap` (недоступний у PATH) методом читання рядкових констант
> байткоду (сигнатури методів, назви параметрів із `LocalVariableTable`). Точні числові
> дефолти (`size`, `rowCount`, alpha/border у `WheelPickerDefaults`) з цього способу
> не видобуваються (це `LDC`-константи, не рядки) — де дефолт невідомий, це позначено
> нижче явно замість вигаданого числа.

## Підключення

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // обов'язково для цієї бібліотеки
    }
}
```

```toml
# gradle/libs.versions.toml
[versions]
wheelPickerCompose = "1.1.11"

[libraries]
wheel-picker-compose = { group = "com.github.commandiron", name = "WheelPickerCompose", version.ref = "wheelPickerCompose" }
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.wheel.picker.compose)
}
```

## Три готові composable (публічний API з README)

Усі три — `@Composable fun ...(modifier, start..., min..., max..., ..., onSnapped... : (T) -> Unit)`,
колбек викликається з готовим значенням `java.time.*` (без обгортки), кожного разу коли
колесо "доскролило" (snap) до нового значення.

### `WheelTimePicker`

```kotlin
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.commandiron.wheel_picker_compose.core.TimeFormat

WheelTimePicker(
    modifier = Modifier,
    startTime = LocalTime.now(),      // дефолт: LocalTime.now()
    minTime = LocalTime.MIN,          // межі скролу (дефолт не підтверджено декомпіляцією,
    maxTime = LocalTime.MAX,          // але це стандартна поведінка бібліотеки — за потреби обмежте самі)
    timeFormat = TimeFormat.HOUR_24,  // дефолт: TimeFormat.HOUR_24; альтернатива — TimeFormat.AM_PM
    size = DpSize(128.dp, 128.dp),    // розмір усього пікера; дефолт точно не відомий (не рядок у байткоді)
    rowCount = 3,                     // скільки рядків видно одночасно; дефолт не відомий, типово непарне число
    textStyle = MaterialTheme.typography.titleMedium, // це саме дефолт — підтверджено
    textColor = LocalContentColor.current,             // дефолт — підтверджено, тобто вже підхоплює тему M3
    selectorProperties = WheelPickerDefaults.selectorProperties(),
) { snappedTime: LocalTime ->
    // виклик на кожен snap
}
```

Так це реально використано в проєкті — `AddEditEventScreen.kt` → `EventTimePickerDialog`:

```kotlin
WheelTimePicker(
    startTime = initialTime,
    timeFormat = TimeFormat.HOUR_24,
) { snappedTime -> selectedTime = snappedTime }
```

### `WheelDatePicker`

```kotlin
import com.commandiron.wheel_picker_compose.WheelDatePicker

WheelDatePicker(
    modifier = Modifier,
    startDate = LocalDate.now(),   // дефолт: LocalDate.now()
    minDate = ...,
    maxDate = ...,
    yearsRange = IntRange(...),    // діапазон років у колесі "рік"
    size = DpSize(...),
    rowCount = ...,
    textStyle = MaterialTheme.typography.titleMedium,
    textColor = LocalContentColor.current,
    selectorProperties = WheelPickerDefaults.selectorProperties(),
) { snappedDate: LocalDate -> }
```

### `WheelDateTimePicker`

Об'єднання обох — три колеса дати (день/місяць/рік) + колеса часу, ті самі параметри
що і в `WheelDatePicker`, плюс `timeFormat: TimeFormat`, колбек віддає `LocalDateTime`.

```kotlin
WheelDateTimePicker(
    startDateTime = LocalDateTime.now(),
    minDateTime = ...,
    maxDateTime = ...,
    yearsRange = IntRange(...),
    timeFormat = TimeFormat.HOUR_24,
    size = DpSize(...),
    rowCount = ...,
    textStyle = MaterialTheme.typography.titleMedium,
    textColor = LocalContentColor.current,
    selectorProperties = WheelPickerDefaults.selectorProperties(),
) { snappedDateTime: LocalDateTime -> }
```

## Нижчі рівні (не задокументовані в README, але публічні)

Бібліотека побудована шарами. `WheelTimePicker`/`WheelDatePicker`/`WheelDateTimePicker` —
це готові обгортки над двома нижчими примітивами, які теж технічно доступні для імпорту:

- **`com.commandiron.wheel_picker_compose.core.WheelTextPicker`** — колесо довільного
  списку рядків (`texts: List<String>`), а не тільки дат/часу. Придатне, якщо колись
  знадобиться колесо для чогось нестандартного (наприклад, вибір довільної одиниці
  виміру чи категорії) без обгортки LocalTime/LocalDate.
  Параметри: `modifier, startIndex, texts: List<String>, rowCount, style: TextStyle,
  color: Color, selectorProperties, onScrollFinished: (Int) -> Int?`.
- **`com.commandiron.wheel_picker_compose.core.WheelPicker`** — найнижчий рівень:
  повністю кастомний `content: @Composable LazyItemScope.(index: Int) -> Unit` на кожен
  індекс замість готового тексту. Саме тут реалізована 3D-анімація (нахил/прозорість
  сусідніх рядків) через `calculateAnimatedAlpha`/`calculateAnimatedRotationX` — вона
  жорстко зашита і не виноситься параметром жодного з трьох рівнів вище.

Обидва — у пакеті `core`, у README не згадані (тобто офіційно не "підтримуваний" API,
може змінитись без анонсу в майбутніх версіях бібліотеки).

## Чи можна змінити колір/дизайн компонента

**Так, частково — є три незалежні точки кастомізації:**

1. **Колір і шрифт самих цифр** — параметри `textStyle: TextStyle` і `textColor: Color`
   напряму на `WheelTimePicker`/`WheelDatePicker`/`WheelDateTimePicker`. За замовчуванням
   вже беруть `MaterialTheme.typography.titleMedium` і `LocalContentColor.current` —
   тобто без жодних параметрів колесо вже підлаштовується під поточну M3-тему проєкту.

2. **"Селектор" — прямокутник-підсвітка навколо обраного (центрального) значення** —
   через `selectorProperties`, який будується фабрикою `WheelPickerDefaults.selectorProperties(...)`:

   ```kotlin
   import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
   import androidx.compose.foundation.BorderStroke
   import androidx.compose.foundation.shape.RoundedCornerShape

   WheelTimePicker(
       selectorProperties = WheelPickerDefaults.selectorProperties(
           enabled = true,                                   // false = взагалі прибрати підсвітку
           shape = RoundedCornerShape(8.dp),                  // форма підсвітки
           color = MaterialTheme.colorScheme.primaryContainer, // фон підсвітки
           border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
       ),
       ...
   )
   ```

   За замовчуванням `color` бере `MaterialTheme.colorScheme.primary` (з альфа-каналом),
   тобто теж вже прив'язаний до теми — точні дефолтні значення alpha/border-width з
   декомпіляції не видобуті (константи, не рядки), але сам факт "колір primary з теми"
   підтверджений байткодом.

3. **Розмір і кількість видимих рядків** — `size: DpSize` (ширина/висота всього колеса)
   і `rowCount: Int` (скільки рядків одночасно видно — впливає на те, наскільки "щільне"
   колесо).

**Що змінити не можна** без форку бібліотеки чи переходу на найнижчий рівень
(`core.WheelPicker` з власним `content`):
- 3D-ефект нахилу/затемнення сусідніх рядків при скролі (`calculateAnimatedRotationX`,
  `calculateAnimatedAlpha`) — жорстко вшитий у `WheelPickerKt.WheelPicker`, немає
  параметра для вимкнення чи налаштування амплітуди.
- Формат самих текстових лейблів колеса (наприклад "09" замість "9", чи локалізовані
  назви місяців) — не виноситься параметром на рівні `WheelTimePicker`/`WheelDatePicker`;
  керується внутрішніми `Hour`/`Minute`/`Month`/`DayOfMonth`/`Year`/`AmPm` класами
  пакету `core`, не документованими й не призначеними для зовнішнього використання.

Загальний дизайн діалогу навколо пікера (заголовок, кнопки OK/Скасувати, фон) —
це вже наш власний `AlertDialog` у `EventTimePickerDialog`, там колір/стиль повністю
під контролем M3-теми проєкту як завжди.

## Як діставали ці дані (метод, для повторного використання)

Корисно, якщо знадобиться дослідити ще якусь Maven/JitPack-залежність без офіційної
документації параметрів:

```powershell
# 1. Форсувати Gradle завантажити реальний артефакт (не тільки метадані)
./gradlew :app:compileDebugKotlin -q

# 2. Знайти .aar/.jar у локальному кеші
find ~/.gradle/caches -iname "*WheelPickerCompose*.aar"

# 3. Розпакувати .aar → classes.jar → .class файли
unzip -o -q WheelPickerCompose-1.1.11.aar
unzip -o -q classes.jar -d extracted

# 4. javap не було в PATH — рядкові константи (у т.ч. LocalVariableTable з іменами
#    параметрів композебл-функцій) читали напряму з .class-файлу regexp'ом:
perl -ne 'print "$1\n" while /([ -~]{4,})/g' extracted/.../WheelTimePickerKt.class
```

Це дає сигнатури методів, назви параметрів і рядкові константи, але **не** числові
дефолти (`LDC float/int` у самому байткоді) — для тих потрібен повний дизасемблер
(`javap -c` або аналог), якого на машині не було.
