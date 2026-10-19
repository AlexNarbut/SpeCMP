# ui.sh — CLI управления устройством

`U=.agents/skills/ui-test/scripts/ui.sh` (`$U help` — полная справка). Один интерфейс для трёх платформ:

| Платформа | Бэкенд |
|---|---|
| Android | `adb` + `uiautomator dump` |
| iOS-симулятор | `xcrun simctl` + [AXe](https://github.com/cameroncooke/AXe) |
| Desktop (macOS) | `scripts/ax.swift` — macOS Accessibility + CGEvent |

Компактное дерево экрана для всех платформ строит `scripts/tree.py`.

## Окружение и жизненный цикл

| Команда | Что делает |
|---|---|
| `doctor android\|ios\|desktop` | инструменты, устройство, разрешения, креды; ❌ — с командой исправления |
| `use android\|ios\|desktop [--device id] [--app-id id]` | выбрать платформу (состояние — в `$TMPDIR/ui-test-SpeCMP/`) |
| `build` | Android: `:androidApp:assembleDebug`; iOS: xcodebuild схемы `iosApp`; Desktop: `:desktopApp:classes` |
| `install` | установка APK / .app (на desktop не нужна) |
| `launch [sec]` / `stop` / `restart` | запуск / остановка; desktop — `gradlew :desktopApp:run` в фоне, лог в `desktop.log` |
| `reset` | сброс данных приложения (разлогин). Desktop — не поддерживается |
| `deeplink <uri>` | Android / iOS |

## Экран

| Команда | Что делает |
|---|---|
| `tree [--all]` | `[N] тип "текст" #testTag @x,y (флаги)`; ждёт, пока экран перестанет меняться |
| `tap <q> [sleep]`, `longtap <q>` | q: `#testTag` (glob: `#*back_button`) / `id:tag` / текст / `@N` из последнего `tree` / `x,y` |
| `expect <q> [cond] [sec]` | поллинг до sec (5): `visible`, `gone`, `disabled`, `enabled`, `checked`, `focused`, `value=<текст>` |
| `do "<cmd>; <cmd>; …"` | пакет шагов, стоп на первой ошибке |
| `type <text>` / `clear` | ввод в поле в фокусе / очистка поля; `<QA_KEY>` → значение из кредов, `{n}` → timestamp |
| `key back\|enter\|home\|hide-keyboard` | системные действия |
| `swipe up\|down\|left\|right`, `scroll-to <q>` | прокрутка; `up` — контент едет вверх |
| `wait <q> [sec]`, `gone <q> [sec]` | дождаться появления / исчезновения |
| `shot <path.png>` | скриншот (уменьшается до 1000 px) |

Текст ищется без учёта регистра: сначала точное совпадение, затем частичное. `#id` вне кавычек shell считает
комментарием — `"#id"` или `id:foo`; внутри `do "…"` — как есть.

Пример:

```bash
$U do "tap Email; type <QA_EMAIL>; tap Password; type <QA_PASSWORD>; key hide-keyboard; tap 'Log in'; gone 'Specialist authorization' 15"
```

## Логи и крэши

| Команда | Android | iOS | Desktop |
|---|---|---|---|
| `mark` | время устройства | время хоста | номер строки `desktop.log` |
| `logs [lines]` | crash-буфер + W/E процесса | `log show` процесса `SpeCMP` | ошибки из `desktop.log` с отметки |
| `crash` | crash-буфер с отметки | `.ips` в DiagnosticReports | процесс умер / `Exception in thread` |

## Maestro (Android, iOS)

`flow [--out dir] <file.yaml> [-e K=V …]` — `maestro test` с `APP_ID` и всеми ключами кредов как `-e`.
Debug-артефакты сохраняются в `flows/out/debug-<ID>-<platform>` только при падении. Desktop Maestro не
поддерживает — кейсы проходятся вручную (`do` / `expect`).
