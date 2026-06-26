# Тестування дизайну на емуляторі через ADB

## Передумови

- Емулятор запущений в Android Studio (`emulator-5554` або інший)
- Шлях до ADB: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`
  (додано до PATH — доступний як `adb` після перезапуску терміналу)

Перевірити підключення:
```powershell
adb devices
# emulator-5554   device
```

## 1. Збірка та встановлення

```powershell
# Зібрати debug APK
.\gradlew.bat assembleDebug

# Встановити на емулятор
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 2. Запуск додатку

```powershell
adb shell monkey -p com.dgero.homly -c android.intent.category.LAUNCHER 1
```

## 3. Скріншот

```bash
# Використовувати Bash (не PowerShell) — бінарно-безпечний pipe
adb exec-out screencap -p > screenshot.png
```

Відкрити `screenshot.png` або прочитати через Claude Code для перегляду UI.

## 4. Взаємодія з UI

### Отримати координати елементів (UI dump)
```powershell
adb shell uiautomator dump /sdcard/ui.xml
adb pull /sdcard/ui.xml ui.xml
# Відкрити ui.xml, знайти bounds="[x1,y1][x2,y2]" для потрібного елементу
# Центр = ((x1+x2)/2, (y1+y2)/2)
```

### Тап по елементу
```powershell
adb shell input tap 540 989
```

### Введення тексту
```powershell
adb shell input text "yourtext"
```

### Кнопки Back / Home
```powershell
adb shell input keyevent 4    # Back
adb shell input keyevent 3    # Home
```

## 5. Типовий сценарій

```powershell
# 1. Збірка
.\gradlew.bat assembleDebug

# 2. Встановлення
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 3. Запуск
adb shell monkey -p com.dgero.homly -c android.intent.category.LAUNCHER 1

# 4. Взаємодія (приклад: заповнення форми входу)
adb shell input tap 540 989      # тап на поле Login
adb shell input text "testuser"
adb shell input tap 540 1242     # тап на поле Password
adb shell input text "Test1!"
adb shell input tap 540 1470     # тап на кнопку Login

# 5. Скріншот (виконувати в Bash, не PowerShell)
adb exec-out screencap -p > screenshot.png
```

## Важливі нотатки

- `make build` не працює в PowerShell (викликає `gradlew` без `.bat`); використовувати `.\gradlew.bat assembleDebug` напряму.
- `adb exec-out screencap -p` потрібно піпити в Bash — PowerShell через `Set-Content` пошкоджує бінарні дані PNG.
- Координати в `uiautomator dump` — оригінальні пікселі пристрою (1080×2400). Claude Code відображає скріншоти у зменшеному вигляді; множити відображені координати на 1.20 для отримання оригінальних.
