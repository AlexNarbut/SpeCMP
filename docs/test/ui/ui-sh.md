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
| `build` | Android: `:androidApp:assembleGoogleDebug`; iOS: xcodebuild схемы `iosApp`; Desktop: `:desktopApp:classes` |
| `install` | установка APK / .app (на desktop не нужна) |
| `launch [sec]` / `stop` / `restart` | запуск / остановка; desktop — `gradlew :desktopApp:run` в фоне, лог в `desktop.log` |
| `reset` | сброс данных приложения (разлогин). Desktop — `stop` + удаление узлов `SpeCMP_*` и ключей `supportchat.*` из `java.util.prefs` (`scripts/Prefs.java`) |
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
| `a11y` | аудит доступности и тестируемости экрана (см. ниже) |

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
| `wait-log <regex> [sec]` | весь лог процесса с отметки | `log show` процесса `SpeCMP` | `desktop.log` с отметки |

`wait-log` ждёт строку по `grep -E` (по умолчанию 10 с) и печатает до 5 совпадений; при таймауте — ❌ и exit 1,
поэтому внутри `do` работает как проверка: `do "tap Connect; wait-log 'connected' 20; expect 'Device info'"`.

## Разрешения и сеть

| Команда | Android | iOS-симулятор | Desktop |
|---|---|---|---|
| `perm` | runtime-разрешения: `granted`, флаги | — | — |
| `perm grant\|revoke <name>…` | `pm grant/revoke`; только запрошенные приложением на этом API | `simctl privacy`, только `location` | — |
| `perm reset` | revoke + сброс `user-set`/`user-fixed`: диалог покажется снова | `simctl privacy reset all` | — |
| `net [on\|off]` | `svc wifi` + `svc data`, статус и ping | — (сеть хоста) | — (сеть хоста) |

Имена: `bluetooth` (`BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT`), `location` (`ACCESS_FINE_LOCATION` +
`ACCESS_COARSE_LOCATION`), `notifications` (`POST_NOTIFICATIONS`) или полное `android.permission.X`.
Отзыв разрешения система применяет, останавливая приложение, — после `revoke`/`reset` нужен `launch`.
`net off` — не авиарежим: Bluetooth остаётся включённым. В Maestro-флоу разрешения задаются через
`launchApp: {permissions: …}` (по умолчанию `launchApp` выдаёт все).

## Аудит доступности

`a11y` проверяет видимые интерактивные элементы (`btn`, `input`, `check`, `switch`, `slider`); индексы `[N]` —
как у `tree`. Exit 0 всегда: это сбор замечаний, а не проверка кейса.

| Правило | Что | Раздел ревью |
|---|---|---|
| `no-label` | нет текста / contentDescription ни у элемента, ни у потомков (подсказка поля считается) | Тестируемость + UX |
| `no-testtag` | нет testTag; если тегов нет на всём экране — одна строка на экран | Тестируемость |
| `small-target` | зона касания меньше 48dp (Android, px → dp по `wm density`) / 44pt (iOS); desktop не проверяется | UX |
| `dup-label` | несколько интерактивных элементов с одной подписью, у этого нет testTag | Тестируемость |

Используется в необязательном этапе плана «Доступность и тестируемость».

## Производительность

`perf reset` запоминает базу, `perf` печатает метрики с базы и строку-вердикт. Exit 0 всегда: эмулятор и
симулятор шумят, это наблюдение, а не проверка.

| | Android | iOS-симулятор | Desktop |
|---|---|---|---|
| Кадры | `dumpsys gfxinfo` (сброс на `perf reset`): всего, janky %, p50/p90/p95, slow UI thread | — | — |
| Память | `dumpsys meminfo`: TOTAL PSS, Java / Native heap | RSS процесса `SpeCMP` на хосте | RSS JVM |

Вердикт ⚠️: janky > 25 % или p95 > 100 мс (только при ≥ 30 кадрах), прирост памяти > 30 МБ. Пороги — в начале
`cmd_perf`.

## Данные приложения (только чтение)

| Команда | Android (debug-сборка, `run-as`) | iOS-симулятор (контейнер `simctl get_app_container`) | Desktop |
|---|---|---|---|
| `data [files]` | файлы без `cache/`, `code_cache/` | файлы без `tmp/`, `Library/Caches/` | — |
| `data prefs [filter]` | `shared_prefs/*.xml` | `Library/Preferences/<bundle>.plist` | `java.util.prefs`: `SpeCMP_*`, `supportchat.*` |
| `data db <file> "<SQL>"` | копия `databases/<file>` (+ журналы) → `sqlite3 -readonly` на хосте | `sqlite3 -readonly` в контейнере | — |
| `data cat <path>` | первые 200 строк | первые 200 строк; бинарный plist — в XML | — |

`prefs` маскирует значения ключей с `token`, `password`, `secret`, `session`, `auth`, `cookie`, `pin`:
`*** (N символов)` / `(пусто)`. `db` и `cat` не маскируют. Записи нет намеренно: предусловия создаются через UI.

## Maestro (Android, iOS)

`flow [--out dir] <file.yaml> [-e K=V …]` — `maestro test` с `APP_ID` и всеми ключами кредов как `-e`.
Debug-артефакты сохраняются в `flows/out/debug-<ID>-<platform>` только при падении. Desktop Maestro не
поддерживает — кейсы проходятся вручную (`do` / `expect`).
