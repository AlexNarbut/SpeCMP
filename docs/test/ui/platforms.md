# Платформы: требования и особенности

Общее: `python3`, JDK 17, креды в `.agents/skills/ui-test/credentials.properties`.

## Android

- `adb` в `PATH`, запущенный эмулятор или телефон с USB-отладкой; Maestro
  (`brew tap mobile-dev-inc/tap && brew install mobile-dev-inc/tap/maestro`).
- app id `ru.alarmtrade.pandoraspecialist`, APK — `androidApp/build/outputs/apk/google/debug/`.
- `adb input` вводит только ASCII — тестовые данные латиницей.
- Тексты в планах — английские: эмулятор на en.
- `perm` и `net` работают полностью; `perm revoke`/`reset` останавливают приложение.

## iOS-симулятор

- Xcode, AXe (`brew install cameroncooke/axe/axe`), Maestro, запущенный симулятор и открытый Simulator.app.
- `ui.sh use ios` переключает симулятор на en и включает app accessibility (без неё Compose не строит дерево).
- `perm` — только `location` (Bluetooth в `simctl privacy` нет, BLE на симуляторе тоже); `net` не поддерживается —
  сеть хоста, офлайн-кейсы ⏭.
- **Сейчас не собирается под симулятор.** Таргет `iosSimulatorArm64` убран (коммит `820800a`): Specialist SDK
  (`ru.alarmtrade.common:*`) публикует только `iosarm64` (устройство) и `iosx64` (Intel-симулятор).
  `ui.sh build` на iOS падает с `ios_simulator_arm64 … not configured in Gradle`. Варианты: сборка SDK под
  `iossimulatorarm64`, таргет `iosX64` + x86_64-симулятор, или реальное устройство (в `ui.sh` не поддержано).

## Desktop (macOS)

- `swiftc` (Xcode Command Line Tools) — `ax.swift` собирается автоматически в `$TMPDIR/ui-test-SpeCMP/ax`
  при первом вызове и после правки исходника.
- **Разрешение Accessibility** для приложения, из которого запущен агент (Терминал / IDE):
  Системные настройки → Конфиденциальность и безопасность → Универсальный доступ. `doctor desktop` проверяет.
- `launch` — `./gradlew :desktopApp:run` в фоне; уже запущенное приложение переиспользуется
  (поиск по процессу `ru.alarmtrade.specmp.desktopApp.MainKt`). Перед действием окно поднимается наверх —
  во время прогона мышь и клавиатуру не трогать.

Особенности Compose Desktop под Accessibility:

| Что | Как ведёт себя | Как с этим работать |
|---|---|---|
| Язык | Gradle передаёт JVM локаль своего демона, приложение берёт язык из своих настроек | runner переключает язык на English в «Настройках» (меню главной) и перезапускает приложение перед этапом |
| Поле ввода | неотличимо от кликабельного текста (`AXStaticText` + `AXPress`) | в `tree` — `btn` с плейсхолдером или введённым текстом; `tap` → `type` |
| Кликабельность | `AXPress` есть только у кликабельных узлов | такие узлы показываются как `btn` (строки списков тоже) |
| Назад | системной «назад» нет | `tap` по кнопке «Back»; `key back` без testTag жмёт Escape |
| Сброс данных | SDK и приложение хранят данные в `java.util.prefs` (общий plist JVM-приложений) | `reset` удаляет узлы `SpeCMP_*` и ключи `supportchat.*`; язык после него — системный |
| `perf`, `data` | кадров нет, путь файлов SDK неизвестен | `perf` — только память, `data` — только `prefs` |
| Maestro | не поддерживает desktop | кейсы вручную через `do` / `expect` |
| Разрешения, сеть | выдаёт macOS / сеть хоста | `perm` и `net` не поддерживаются — такие кейсы ⏭ |
| `a11y` | мышь вместо касаний | `small-target` не проверяется |
| Координаты | от левого верхнего угла окна | `@x,y` из `tree` подходят для `tap x,y` |
