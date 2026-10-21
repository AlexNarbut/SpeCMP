#!/usr/bin/env bash
# Единый CLI UI-тестирования: Android (adb), iOS-симулятор (simctl + AXe) и desktop на macOS (ax.swift).
# Справка: ui.sh help
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel 2>/dev/null || { cd "$SCRIPT_DIR/../../../.." && pwd; })"
TREE="$SCRIPT_DIR/tree.py"
# Состояние — своё на проект: иначе ui.sh разных репозиториев делят устройство и app id.
STATE_DIR="${UI_TEST_STATE_DIR:-${TMPDIR:-/tmp}/ui-test-$(basename "$ROOT")}"
STATE="$STATE_DIR/state"
# Креды — в runtime-копии скилла (.ai/generate.sh её не перетирает), откуда бы ни запускали скрипт.
CREDS="$ROOT/.agents/skills/ui-test/credentials.properties"
mkdir -p "$STATE_DIR"

export MAESTRO_CLI_NO_ANALYTICS=1
export MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true

PLATFORM="" DEVICE="" MARK="" APP_ID="" PERF_MEM=""
[[ -f "$STATE" ]] && source "$STATE"

die() { echo "ERROR: $*" >&2; exit 1; }
save_state() {
  printf 'PLATFORM=%q\nDEVICE=%q\nMARK=%q\nAPP_ID=%q\nPERF_MEM=%q\n' \
    "$PLATFORM" "$DEVICE" "$MARK" "$APP_ID" "$PERF_MEM" >"$STATE"
}
need_platform() {
  [[ -n "$PLATFORM" ]] || die "платформа не выбрана: ui.sh use android|ios|desktop"
  # pid desktop-приложения меняется при каждом перезапуске — берём актуальный.
  [[ "$PLATFORM" == desktop ]] && DEVICE="$(desktop_pid)"
  return 0
}

# --- идентификаторы сборки -------------------------------------------------------

# Имя процесса и .app на iOS — PRODUCT_NAME из iosApp/Configuration/Config.xcconfig.
IOS_PRODUCT="SpeCMP"

app_id() {
  if [[ -n "$APP_ID" ]]; then
    echo "$APP_ID"
  elif [[ "$PLATFORM" == android ]]; then
    echo "ru.alarmtrade.specmp.androidApp"
  elif [[ "$PLATFORM" == desktop ]]; then
    echo "ru.alarmtrade.specmp.desktopApp"
  else
    echo "ru.alarmtrade.specmp.iosApp"
  fi
}
ios_scheme() { echo "iosApp"; }
ios_config() { echo "Debug"; }
ios_derived() { echo "$ROOT/iosApp/build/ui-test"; }
ios_app_path() { echo "$(ios_derived)/Build/Products/$(ios_config)-iphonesimulator/$IOS_PRODUCT.app"; }
apk_path() { ls "$ROOT"/androidApp/build/outputs/apk/debug/*.apk 2>/dev/null | head -1; }

adb_() { adb -s "$DEVICE" "$@"; }

# Без app accessibility Compose на iOS не строит дерево доступности (AXe видит пустое Application).
# Maestro по завершении теста сбрасывает флаг — включаем перед каждым запуском приложения.
ios_ax_on() { xcrun simctl spawn "$DEVICE" defaults write com.apple.Accessibility ApplicationAccessibilityEnabled -bool true; }

# Desktop (macOS): DEVICE — pid JVM-процесса приложения, экран и ввод — через ax.swift (Accessibility + CGEvent).
DESKTOP_MAIN="ru.alarmtrade.specmp.desktopApp.MainKt"
DESKTOP_LOG="$STATE_DIR/desktop.log"
desktop_pid() { pgrep -f "java.*$DESKTOP_MAIN" | head -1; }

# Хелпер собирается при первом вызове и после правки исходника.
ax_bin() {
  local bin="$STATE_DIR/ax"
  if [[ ! -x "$bin" || "$SCRIPT_DIR/ax.swift" -nt "$bin" ]]; then
    swiftc -O "$SCRIPT_DIR/ax.swift" -o "$bin" 2>"$STATE_DIR/ax-build.log" || die "сборка ax.swift упала: $STATE_DIR/ax-build.log"
  fi
  echo "$bin"
}
ax_() {
  [[ -n "$DEVICE" ]] || die "desktop-приложение не запущено — ui.sh launch"
  local cmd="$1"; shift
  "$(ax_bin)" "$cmd" "$DEVICE" "$@"
}

# --- выбор платформы и окружение -------------------------------------------------

cmd_use() {
  PLATFORM="${1:-}"; shift || true
  [[ "$PLATFORM" == android || "$PLATFORM" == ios || "$PLATFORM" == desktop ]] || die "use android|ios|desktop [--device id] [--app-id id]"
  DEVICE="" APP_ID=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --device) DEVICE="$2"; shift 2 ;;
      --app-id) APP_ID="$2"; shift 2 ;;
      *) die "неизвестный флаг $1" ;;
    esac
  done
  if [[ "$PLATFORM" == desktop ]]; then
    DEVICE="$(desktop_pid)"
    save_state
    [[ -n "$DEVICE" ]] && cmd_mark >/dev/null
    echo "platform=desktop pid=${DEVICE:-не запущено — ui.sh launch} app=$(app_id)"
    return 0
  fi
  if [[ -z "$DEVICE" ]]; then
    if [[ "$PLATFORM" == android ]]; then
      DEVICE="$(adb devices | awk 'NR>1 && $2=="device"{print $1; exit}')"
    else
      DEVICE="$(xcrun simctl list devices booted -j | python3 -c 'import json,sys
d=json.load(sys.stdin)["devices"]
print(next((x["udid"] for v in d.values() for x in v if x["state"]=="Booted"),""))')"
    fi
  fi
  [[ -n "$DEVICE" ]] || { save_state; die "нет запущенного устройства — см. ui.sh doctor"; }
  # Настройки симулятора действуют на процессы, запущенные после записи.
  # Английский — как на Android-эмуляторе: одни ожидаемые тексты в плане и Maestro-флоу.
  if [[ "$PLATFORM" == ios ]]; then
    ios_ax_on
    xcrun simctl spawn "$DEVICE" defaults write -g AppleLanguages -array en
    xcrun simctl spawn "$DEVICE" defaults write -g AppleLocale -string en_US
  fi
  cmd_mark >/dev/null
  echo "platform=$PLATFORM device=$DEVICE app=$(app_id)"
}

cmd_doctor() {
  local p="${1:-$PLATFORM}" fail=0
  [[ -n "$p" ]] || die "doctor android|ios|desktop"
  ok() { echo "✅ $1"; }
  bad() { echo "❌ $1 — $2"; fail=1; }
  have() { command -v "$1" >/dev/null 2>&1; }

  have python3 && ok "python3" || bad "python3" "brew install python"
  have java && ok "java ($(java -version 2>&1 | head -1))" || bad "java" "brew install openjdk@17 (нужен Gradle и Maestro)"
  if [[ "$p" != desktop ]]; then
    have maestro && ok "maestro $(maestro --version 2>/dev/null | tail -1)" || bad "maestro" "brew tap mobile-dev-inc/tap && brew install mobile-dev-inc/tap/maestro"
  fi
  if [[ "$p" == desktop ]]; then
    if have swiftc; then
      ok "swiftc"
      "$(ax_bin)" trusted >/dev/null 2>&1 && ok "Accessibility: разрешено" \
        || bad "нет разрешения Accessibility" "Системные настройки → Конфиденциальность и безопасность → Универсальный доступ → включи приложение, из которого запущен агент (Терминал / IDE), и перезапусти его"
    else
      bad "swiftc" "xcode-select --install"
    fi
    local pid; pid="$(desktop_pid)"
    [[ -n "$pid" ]] && ok "desktop-приложение запущено (pid $pid)" || ok "desktop-приложение не запущено — запустит ui.sh launch"
  elif [[ "$p" == android ]]; then
    have adb && ok "adb" || bad "adb" "Android SDK platform-tools в PATH (~/Library/Android/sdk/platform-tools)"
    if have adb; then
      local devs; devs="$(adb devices | awk 'NR>1 && $2=="device"{print $1}' | tr '\n' ' ')"
      [[ -n "$devs" ]] && ok "устройства: $devs" || bad "нет устройства" "запусти эмулятор: emulator -list-avds; emulator -avd <name> &  (или подключи телефон с USB-отладкой)"
    fi
  else
    have xcrun && ok "xcrun" || bad "xcrun" "xcode-select --install"
    have xcodebuild && ok "$(xcodebuild -version 2>/dev/null | head -1)" || bad "xcodebuild" "установи Xcode"
    have axe && ok "axe $(axe --version 2>/dev/null)" || bad "axe" "brew install cameroncooke/axe/axe"
    local booted; booted="$(xcrun simctl list devices booted 2>/dev/null | grep -E '\(Booted\)' | sed 's/^ *//')"
    [[ -n "$booted" ]] && ok "симулятор: $booted" || bad "нет запущенного симулятора" "xcrun simctl list devices available | grep iPhone; xcrun simctl boot <udid>; open -a Simulator"
    pgrep -x Simulator >/dev/null && ok "Simulator.app открыт" || bad "Simulator.app закрыт" "open -a Simulator (без окна AXe не читает дерево)"
  fi
  [[ -f "$CREDS" ]] && ok "креды: .agents/skills/ui-test/credentials.properties" || bad "нет кредов" "cp .agents/skills/ui-test/credentials.properties.example .agents/skills/ui-test/credentials.properties и заполнить"
  [[ $fail -eq 0 ]] && echo "OK: окружение готово" || { echo "FAIL: исправь пункты ❌ и повтори ui.sh doctor $p"; exit 1; }
}

# --- сборка и жизненный цикл -----------------------------------------------------

cmd_build() {
  need_platform
  cd "$ROOT" || exit 1
  if [[ "$PLATFORM" == desktop ]]; then
    ./gradlew :desktopApp:classes -q >"$STATE_DIR/build.log" 2>&1 \
      || { grep -E "^e: |What went wrong" -A3 "$STATE_DIR/build.log" | head -40; die "сборка упала, лог: $STATE_DIR/build.log"; }
    echo "OK: desktopApp скомпилирован (запуск — ui.sh launch)"
  elif [[ "$PLATFORM" == android ]]; then
    ./gradlew :androidApp:assembleDebug -q >"$STATE_DIR/build.log" 2>&1 \
      || { grep -E "^e: |What went wrong" -A3 "$STATE_DIR/build.log" | head -40; die "сборка упала, лог: $STATE_DIR/build.log"; }
    echo "OK: $(apk_path)"
  else
    xcodebuild -project iosApp/iosApp.xcodeproj -scheme "$(ios_scheme)" -configuration "$(ios_config)" \
      -sdk iphonesimulator -destination "id=$DEVICE" ARCHS=arm64 -derivedDataPath "$(ios_derived)" build \
      -quiet >"$STATE_DIR/build.log" 2>&1 \
      || { grep -E "^e: |: error: |BUILD FAILED" "$STATE_DIR/build.log" | sort -u | head -40; die "сборка упала, лог: $STATE_DIR/build.log"; }
    echo "OK: $(ios_app_path)"
  fi
}

cmd_install() {
  need_platform
  if [[ "$PLATFORM" == desktop ]]; then
    echo "desktop: установка не нужна"
  elif [[ "$PLATFORM" == android ]]; then
    adb_ install -r "$(apk_path)" | tail -1
  else
    xcrun simctl install "$DEVICE" "$(ios_app_path)" && echo "Success"
  fi
}

# Desktop запускается через Gradle в фоне. Язык — из настроек приложения: Gradle передаёт JVM локаль своего
# демона (-Duser.language перекрывает JAVA_TOOL_OPTIONS), а сохранённый в приложении язык важнее системного.
desktop_launch() {
  if [[ -n "$DEVICE" ]]; then
    cmd_mark >/dev/null
    echo "already running pid $DEVICE"
    return 0
  fi
  (cd "$ROOT" && nohup ./gradlew :desktopApp:run -q >"$DESKTOP_LOG" 2>&1 &)
  local start=$SECONDS
  until DEVICE="$(desktop_pid)" && [[ -n "$DEVICE" ]] && ax_ window >/dev/null 2>&1; do
    ((SECONDS - start < 300)) || die "окно не появилось за 300 с, лог: $DESKTOP_LOG"
    sleep 1
  done
  MARK=0
  save_state
  sleep "${1:-3}"
  echo "launched desktop pid $DEVICE ($((SECONDS - start))s)"
}

cmd_launch() {
  need_platform
  [[ "$PLATFORM" == desktop ]] && { desktop_launch "$@"; return; }
  cmd_mark >/dev/null
  if [[ "$PLATFORM" == android ]]; then
    adb_ shell monkey -p "$(app_id)" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  else
    ios_ax_on
    xcrun simctl launch "$DEVICE" "$(app_id)" >/dev/null
  fi
  sleep "${1:-4}"
  echo "launched $(app_id)"
}

cmd_stop() {
  need_platform
  if [[ "$PLATFORM" == desktop ]]; then
    if [[ -n "$DEVICE" ]]; then
      kill "$DEVICE" 2>/dev/null
      local i
      for ((i = 0; i < 20; i++)); do kill -0 "$DEVICE" 2>/dev/null || break; sleep 0.5; done
      kill -0 "$DEVICE" 2>/dev/null && kill -9 "$DEVICE"
    fi
  elif [[ "$PLATFORM" == android ]]; then adb_ shell am force-stop "$(app_id)"
  else xcrun simctl terminate "$DEVICE" "$(app_id)" 2>/dev/null; fi
  echo "stopped"
}

# Данные desktop-приложения в java.util.prefs: узлы SDK (appName "SpeCMP") и ключи корня из AppSettingsFactory.
DESKTOP_PREFS=(SpeCMP_ supportchat.)
desktop_prefs() { java "$SCRIPT_DIR/Prefs.java" "$1" "${DESKTOP_PREFS[@]}"; }

cmd_reset() {
  need_platform
  # Останавливаем до удаления: иначе JVM при выходе запишет prefs обратно.
  cmd_stop >/dev/null
  if [[ "$PLATFORM" == desktop ]]; then
    desktop_prefs remove || die "не удалось очистить java.util.prefs"
    echo "app data cleared (запуск — ui.sh launch)"
    return 0
  fi
  if [[ "$PLATFORM" == android ]]; then
    adb_ shell pm clear "$(app_id)" >/dev/null
  else
    # Данные приложения на симуляторе сбрасываются только переустановкой.
    xcrun simctl uninstall "$DEVICE" "$(app_id)"
    cmd_install >/dev/null
  fi
  echo "app data cleared"
}

cmd_deeplink() {
  need_platform
  [[ "$PLATFORM" == desktop ]] && die "deeplink на desktop не поддерживается"
  if [[ "$PLATFORM" == android ]]; then adb_ shell am start -a android.intent.action.VIEW -d "$1" >/dev/null
  else xcrun simctl openurl "$DEVICE" "$1"; fi
  echo "opened $1"
}

# --- экран -----------------------------------------------------------------------

dump_ext() { [[ "$PLATFORM" == android ]] && echo xml || echo json; }

# <QA_KEY> → значение из кредов (в любом месте строки): значения не попадают в команды агента и транскрипт.
resolve() {
  local v="$1" key line val
  while [[ "$v" =~ \<([A-Z][A-Z0-9_]*)\> ]]; do
    key="${BASH_REMATCH[1]}"
    line="$(grep -E "^$key=" "$CREDS" 2>/dev/null | head -1)"
    [[ -n "$line" ]] || die "нет ключа $key в credentials.properties"
    val="${line#*=}"
    val="${val//\{n\}/$(date +%s)}"
    v="${v//<$key>/$val}"
  done
  printf %s "$v"
}

# id:foo — то же, что #foo, но без кавычек в shell (# начинает комментарий).
selector() {
  local q; q="$(resolve "$1")" || return 1
  [[ "$q" == id:* ]] && q="#${q#id:}"
  printf %s "$q"
}

dump() {
  local out="$STATE_DIR/dump.$(dump_ext)"
  if [[ "$PLATFORM" == desktop ]]; then
    ax_ dump >"$out" 2>"$STATE_DIR/ax.err" || die "ax dump: $(cat "$STATE_DIR/ax.err")"
  elif [[ "$PLATFORM" == android ]]; then
    adb_ exec-out uiautomator dump /dev/tty >"$out" 2>/dev/null
    grep -q "</hierarchy>" "$out" || { sleep 1; adb_ exec-out uiautomator dump /dev/tty >"$out" 2>/dev/null; }
  else
    axe describe-ui --udid "$DEVICE" >"$out" 2>"$STATE_DIR/axe.err" || die "axe describe-ui: $(cat "$STATE_DIR/axe.err")"
  fi
  echo "$out"
}

# Размер экрана Android берётся из wm size: по дереву диалога он определяется неверно.
screen_arg() {
  [[ "$PLATFORM" == android ]] || return 0
  local s; s="$(adb_ shell wm size | awk -F': ' '{print $2}' | tail -1 | tr -d '\r')"
  echo "--screen $s"
}

# Клавиатура — отдельное окно: в дамп приложения не попадает, но перехватывает тапы.
keyboard_shown() {
  if [[ "$PLATFORM" == android ]]; then
    adb_ shell dumpsys input_method | grep -q 'mInputShown=true'
  else
    return 1 # на iOS tree.py определяет клавиатуру по видимым клавишам сам
  fi
}

# Ждёт, пока два снимка подряд совпадут (анимации, загрузка), но не дольше ~4 с.
settle() {
  local prev="" cur i
  for ((i = 0; i < 6; i++)); do
    cur="$(python3 "$TREE" render "$PLATFORM" "$(dump)" "$STATE_DIR/settle.json" $(screen_arg) | md5)"
    [[ "$cur" == "$prev" ]] && return 0
    prev="$cur"
    sleep 0.3
  done
}

cmd_tree() {
  need_platform
  settle
  python3 "$TREE" render "$PLATFORM" "$STATE_DIR/dump.$(dump_ext)" "$STATE_DIR/tree.json" $(screen_arg) $(keyboard_shown && echo --keyboard) "$@"
}

# Проверка элемента с поллингом: одна строка ✅/❌, при ❌ — дерево экрана.
cmd_expect() {
  need_platform
  local q="${1:?expect <q> [visible|gone|disabled|enabled|checked|focused|value=<текст>] [сек]}"
  local cond="${2:-visible}" timeout="${3:-5}" start=$SECONDS st found ok flags label
  [[ "$cond" =~ ^[0-9]+$ ]] && { timeout="$cond"; cond=visible; }
  while :; do
    st="$(python3 "$TREE" state "$PLATFORM" "$(dump)" "$(selector "$q")" $(screen_arg))"
    found=$?
    label="$(cut -f2 <<<"$st")" flags=",$(cut -f3 <<<"$st"),"
    ok=0
    case "$cond" in
      visible) ((found == 0)) && ok=1 ;;
      gone) ((found != 0)) && ok=1 ;;
      disabled) ((found == 0)) && [[ "$flags" == *",disabled,"* ]] && ok=1 ;;
      enabled) ((found == 0)) && [[ "$flags" != *",disabled,"* ]] && ok=1 ;;
      checked | focused | selected) ((found == 0)) && [[ "$flags" == *",$cond,"* ]] && ok=1 ;;
      value=*)
        local want; want="$(resolve "${cond#value=}")" || exit 1
        ((found == 0)) && [[ "$(tr '[:upper:]' '[:lower:]' <<<"$label")" == *"$(tr '[:upper:]' '[:lower:]' <<<"$want")"* ]] && ok=1
        cond="value=${cond#value=}" ;;
      *) die "expect: неизвестное условие $cond" ;;
    esac
    ((ok)) && { echo "✅ $q $cond"; return 0; }
    ((SECONDS - start >= timeout)) && break
    sleep 0.5
  done
  if ((found == 0)); then echo "❌ $q $cond за ${timeout}s — факт: $(tr '\t' ' ' <<<"$st")"
  else echo "❌ $q $cond за ${timeout}s — элемента нет"; fi
  python3 "$TREE" render "$PLATFORM" "$STATE_DIR/dump.$(dump_ext)" "$STATE_DIR/tree.json" $(screen_arg)
  return 1
}

# Пакет шагов одной командой, стоп на первой ошибке.
cmd_do() {
  local step steps
  IFS=';' read -ra steps <<<"$*"
  for step in "${steps[@]}"; do
    step="$(sed -E 's/^ +| +$//g' <<<"$step")"
    [[ -z "$step" ]] && continue
    # shlex, а не eval: в eval `#tag` стал бы комментарием.
    local args=()
    while IFS= read -r a; do args+=("$a"); done < <(python3 -c 'import shlex, sys; print("\n".join(shlex.split(sys.argv[1])))' "$step")
    "$0" "${args[@]}" || { echo "STOP: шаг «${step}» не выполнен"; return 1; }
  done
}

# Печатает "x y" элемента по @N / #id / тексту / координатам "x,y".
locate() {
  local q; q="$(selector "$1")" || return 1
  if [[ "$q" =~ ^[0-9]+,[0-9]+$ ]]; then echo "${q/,/ }"; return 0; fi
  python3 "$TREE" find "$PLATFORM" "$(dump)" "$q" "$STATE_DIR/tree.json" $(screen_arg)
}

tap_xy() {
  if [[ "$PLATFORM" == desktop ]]; then ax_ tap "$1" "$2"
  elif [[ "$PLATFORM" == android ]]; then adb_ shell input tap "$1" "$2"
  else axe tap -x "$1" -y "$2" --udid "$DEVICE" >/dev/null; fi
}

cmd_tap() {
  need_platform
  local xy; xy="$(locate "$1")" || die "не найдено: $1 (проверь ui.sh tree)"
  tap_xy $xy
  [[ -n "${2:-}" ]] && sleep "$2"
  echo "tapped $1 @${xy/ /,}"
}

cmd_longtap() {
  need_platform
  local xy; xy="$(locate "$1")" || die "не найдено: $1"
  set -- $xy
  if [[ "$PLATFORM" == desktop ]]; then ax_ longtap "$1" "$2"
  elif [[ "$PLATFORM" == android ]]; then adb_ shell input swipe "$1" "$2" "$1" "$2" 1000
  else axe touch -x "$1" -y "$2" --down --up --delay 1 --udid "$DEVICE" >/dev/null; fi
  echo "long-tapped @$1,$2"
}

cmd_type() {
  need_platform
  local text; text="$(resolve "$*")" || exit 1
  if [[ "$PLATFORM" == desktop ]]; then
    # Через буфер обмена: не зависит от раскладки, кириллица вводится.
    printf %s "$text" | ax_ type
  elif [[ "$PLATFORM" == android ]]; then
    LC_ALL=C grep -q '[^ -~]' <<<"$text" && die "adb input вводит только ASCII — используй латиницу в тестовых данных"
    adb_ shell input text "$(sed -e 's/ /%s/g' -e "s/[&|<>;()\$\`\\\\\"']/\\\\&/g" <<<"$text")" 2>&1 | grep -q Exception && die "adb input text упал"
  else
    # axe type шлёт HID-коды через раскладку симулятора (на ru печатает кириллицу) — вставка не зависит от раскладки.
    printf %s "$text" | xcrun simctl pbcopy "$DEVICE"
    axe key-combo --modifiers 227 --key 25 --udid "$DEVICE" >/dev/null
  fi
  echo "typed"
}

cmd_clear() {
  need_platform
  local n="${1:-40}"
  if [[ "$PLATFORM" == desktop ]]; then
    ax_ key select-all
    ax_ key backspace
  elif [[ "$PLATFORM" == android ]]; then
    adb_ shell input keyevent KEYCODE_MOVE_END
    adb_ shell input keyevent $(printf 'KEYCODE_DEL %.0s' $(seq "$n"))
  else
    # Курсор после тапа может встать в начало поля — выделяем всё (Cmd+A) и стираем (42 — Backspace).
    axe key-combo --modifiers 227 --key 4 --udid "$DEVICE" >/dev/null
    axe key 42 --udid "$DEVICE" >/dev/null
  fi
  echo "cleared"
}

cmd_key() {
  need_platform
  case "$1" in
    back)
      if [[ "$PLATFORM" == android ]]; then adb_ shell input keyevent KEYCODE_BACK
      # На desktop системной «назад» нет: кнопка back в топбаре, иначе Escape.
      elif [[ "$PLATFORM" == desktop ]]; then
        if xy="$(locate '#*back_button')"; then tap_xy $xy; else ax_ key escape; fi
      # На iOS системной «назад» нет: кнопка back в топбаре, иначе свайп от левого края.
      elif xy="$(locate '#*back_button')"; then tap_xy $xy
      else size; axe swipe --start-x 2 --start-y $((H / 2)) --end-x $((W * 2 / 3)) --end-y $((H / 2)) --duration 0.3 --udid "$DEVICE" >/dev/null; fi ;;
    enter)
      if [[ "$PLATFORM" == desktop ]]; then ax_ key enter
      elif [[ "$PLATFORM" == android ]]; then adb_ shell input keyevent KEYCODE_ENTER
      else axe key 40 --udid "$DEVICE" >/dev/null; fi ;;
    home)
      [[ "$PLATFORM" == desktop ]] && die "key home на desktop не поддерживается"
      if [[ "$PLATFORM" == android ]]; then adb_ shell input keyevent KEYCODE_HOME
      else axe button home --udid "$DEVICE" >/dev/null; fi ;;
    hide-keyboard)
      # BACK закрывает только IME; без открытой клавиатуры он ушёл бы на экран назад.
      # На desktop экранной клавиатуры нет.
      if [[ "$PLATFORM" == desktop ]]; then :
      elif [[ "$PLATFORM" == android ]]; then keyboard_shown && adb_ shell input keyevent KEYCODE_BACK
      else axe key 41 --udid "$DEVICE" >/dev/null; fi ;;
    *) die "key back|enter|home|hide-keyboard" ;;
  esac
  echo "key $1"
}

size() {
  local s
  s="$(python3 "$TREE" render "$PLATFORM" "$(dump)" "$STATE_DIR/size.json" $(screen_arg) | head -1 | awk '{print $2}')"
  W="${s%x*}" H="${s#*x}"
}

swipe_dir() {
  if [[ "$PLATFORM" == desktop ]]; then
    ax_ scroll "$1" || exit 1
    sleep 0.5 # инерция прокрутки
    return
  fi
  size
  local cx=$((W / 2)) cy=$((H / 2)) dy=$((H * 3 / 10)) dx=$((W * 4 / 10)) x1 y1 x2 y2
  case "$1" in
    up) x1=$cx y1=$((cy + dy)) x2=$cx y2=$((cy - dy)) ;;
    down) x1=$cx y1=$((cy - dy)) x2=$cx y2=$((cy + dy)) ;;
    left) x1=$((cx + dx)) y1=$cy x2=$((cx - dx)) y2=$cy ;;
    right) x1=$((cx - dx)) y1=$cy x2=$((cx + dx)) y2=$cy ;;
    *) die "swipe up|down|left|right (up — контент едет вверх, т.е. прокрутка вниз)" ;;
  esac
  if [[ "$PLATFORM" == android ]]; then adb_ shell input swipe $x1 $y1 $x2 $y2 400
  else axe swipe --start-x $x1 --start-y $y1 --end-x $x2 --end-y $y2 --duration 0.4 --udid "$DEVICE" >/dev/null; fi
  sleep 0.5 # инерция прокрутки
}

cmd_swipe() { need_platform; swipe_dir "$1"; echo "swiped $1"; }

cmd_scroll_to() {
  need_platform
  local q="$1" max="${2:-8}" dir="${3:-up}" i
  for ((i = 0; i <= max; i++)); do
    if xy="$(locate "$q")"; then echo "found $q @${xy/ /,} after $i swipes"; return 0; fi
    swipe_dir "$dir"
  done
  die "не найдено за $max свайпов: $q"
}

cmd_wait() {
  need_platform
  local q="$1" timeout="${2:-10}" start=$SECONDS
  while ((SECONDS - start < timeout)); do
    if xy="$(locate "$q")"; then echo "visible $q @${xy/ /,} ($((SECONDS - start))s)"; return 0; fi
    sleep 0.5
  done
  die "не появилось за ${timeout}s: $q"
}

cmd_gone() {
  need_platform
  local q="$1" timeout="${2:-10}" start=$SECONDS
  while ((SECONDS - start < timeout)); do
    locate "$q" >/dev/null || { echo "gone $q ($((SECONDS - start))s)"; return 0; }
    sleep 0.5
  done
  die "не исчезло за ${timeout}s: $q"
}

cmd_shot() {
  need_platform
  local out="${1:?shot <путь.png>}"
  mkdir -p "$(dirname "$out")"
  if [[ "$PLATFORM" == desktop ]]; then screencapture -o -x -l "$(ax_ window)" "$out"
  elif [[ "$PLATFORM" == android ]]; then adb_ exec-out screencap -p >"$out"
  else xcrun simctl io "$DEVICE" screenshot "$out" >/dev/null 2>&1; fi
  # Уменьшенный скриншот экономит токены при просмотре моделью.
  sips -Z "${2:-1000}" "$out" >/dev/null 2>&1
  echo "$out"
}

# --- логи ------------------------------------------------------------------------

cmd_mark() {
  need_platform
  # Desktop: отметка — число строк в логе приложения.
  if [[ "$PLATFORM" == desktop ]]; then MARK=0; [[ -f "$DESKTOP_LOG" ]] && MARK="$(wc -l <"$DESKTOP_LOG" | tr -d ' ')"
  elif [[ "$PLATFORM" == android ]]; then MARK="$(adb_ shell date +'%m-%d\ %H:%M:%S.000')"
  else MARK="$(date +'%Y-%m-%d %H:%M:%S')"; fi
  save_state
  echo "mark $MARK"
}

cmd_logs() {
  need_platform
  local lines="${1:-60}"
  if [[ "$PLATFORM" == desktop ]]; then
    [[ -f "$DESKTOP_LOG" ]] || { echo "лога нет: приложение запущено не через ui.sh launch"; return 0; }
    echo "--- app (desktop.log с отметки)"
    tail -n +"$((MARK + 1))" "$DESKTOP_LOG" | grep -E "🟡|🔴|⚠️|❌|[Ee]rror|[Ee]xception|[Ff]ail|WARN|^\s+at " | tail -n "$lines"
    cmd_crash quiet
  elif [[ "$PLATFORM" == android ]]; then
    local pid; pid="$(adb_ shell pidof "$(app_id)" | tr -d '\r')"
    echo "--- crash buffer"
    adb_ logcat -d -b crash -T "$MARK" 2>/dev/null | tail -n "$lines"
    echo "--- app W/E (pid ${pid:-не запущено})"
    if [[ -n "$pid" ]]; then adb_ logcat -d -T "$MARK" --pid="$pid" '*:W' 2>/dev/null | tail -n "$lines"
    else adb_ logcat -d -T "$MARK" '*:E' 2>/dev/null | grep -E "$(app_id)|AndroidRuntime" | tail -n "$lines"; fi
  else
    # Системные фреймворки в процессе шумят ошибками (haptics, клавиатура) — берём только образ приложения.
    xcrun simctl spawn "$DEVICE" log show --start "$MARK" --style compact \
      --predicate "process == \"$IOS_PRODUCT\" AND (senderImagePath CONTAINS \"$IOS_PRODUCT.app\" OR messageType == fault)" 2>/dev/null \
      | grep -E "🟡|🔴|⚠️|❌|[Ee]rror|[Ee]xception|[Ff]ail| E | F |[Ff]ault" | tail -n "$lines"
    cmd_crash quiet
  fi
}

cmd_crash() {
  need_platform
  if [[ "$PLATFORM" == desktop ]]; then
    local since; since="$(tail -n +"$((MARK + 1))" "$DESKTOP_LOG" 2>/dev/null)"
    if [[ -z "$DEVICE" ]]; then
      echo "CRASH: процесс desktop-приложения завершился"
      grep -E -A25 "Exception in thread|FATAL" <<<"$since" | head -60
    elif grep -q "Exception in thread" <<<"$since"; then
      echo "CRASH:"
      grep -A25 "Exception in thread" <<<"$since" | head -60
    elif [[ "${1:-}" != quiet ]]; then
      echo "no crash since line $MARK"
    fi
  elif [[ "$PLATFORM" == android ]]; then
    local c; c="$(adb_ logcat -d -b crash -T "$MARK" 2>/dev/null | grep -v '^-----')"
    [[ -n "$c" ]] && { echo "CRASH:"; echo "$c" | head -60; } || echo "no crash since $MARK"
  else
    local f; f="$(find ~/Library/Logs/DiagnosticReports -name "$IOS_PRODUCT*.ips" -newermt "$MARK" 2>/dev/null | head -1)"
    if [[ -n "$f" ]]; then
      echo "CRASH: $f"
      python3 - "$f" <<'PY'
import json, sys
raw = open(sys.argv[1]).read().split("\n", 1)[1]
d = json.loads(raw)
ex = d.get("exception", {})
print("exception:", ex.get("type"), ex.get("signal"), ex.get("codes", ""))
print("reason:", d.get("asi") or d.get("crashReason") or "")
th = d.get("threads", [])[d.get("faultingThread", 0)] if d.get("threads") else {}
imgs = d.get("usedImages", [])
for fr in th.get("frames", [])[:25]:
    img = imgs[fr["imageIndex"]]["name"] if fr.get("imageIndex", -1) < len(imgs) else "?"
    print(" ", img, fr.get("symbol", hex(fr.get("imageOffset", 0))))
PY
    elif [[ "${1:-}" != quiet ]]; then
      echo "no crash since $MARK"
    fi
  fi
}

# Все строки лога приложения с отметки (для wait-log).
app_log_since_mark() {
  if [[ "$PLATFORM" == desktop ]]; then
    tail -n +"$((MARK + 1))" "$DESKTOP_LOG" 2>/dev/null
  elif [[ "$PLATFORM" == android ]]; then
    local pid; pid="$(adb_ shell pidof "$(app_id)" | tr -d '\r')"
    if [[ -n "$pid" ]]; then adb_ logcat -d -T "$MARK" --pid="$pid" 2>/dev/null
    else adb_ logcat -d -T "$MARK" 2>/dev/null; fi | grep -v '^--------- beginning of'
  else
    # Первая строка log show — заголовок колонок, не запись.
    xcrun simctl spawn "$DEVICE" log show --start "$MARK" --style compact --predicate "process == \"$IOS_PRODUCT\"" 2>/dev/null \
      | grep -v '^Timestamp '
  fi
}

cmd_wait_log() {
  need_platform
  local re="${1:?wait-log <regex> [сек]}" timeout="${2:-10}" start=$SECONDS hits
  while :; do
    hits="$(app_log_since_mark | grep -E -- "$re" | head -5 | cut -c1-200)"
    [[ -n "$hits" ]] && { echo "✅ лог: $re ($((SECONDS - start))s)"; echo "$hits"; return 0; }
    ((SECONDS - start >= timeout)) && break
    sleep 1
  done
  echo "❌ нет в логе за ${timeout}s: $re"
  return 1
}

# --- разрешения и сеть -----------------------------------------------------------

# Runtime-разрешения приложения: «android.permission.X: granted=…, flags=[…]».
android_runtime_perms() {
  adb_ shell dumpsys package "$(app_id)" | tr -d '\r' | awk '
    /runtime permissions:/ { rt = 1; next }
    rt && /^ *[A-Za-z0-9_.]+: granted=/ { sub(/^ */, ""); print; next }
    rt { rt = 0 }' | sort -u
}

android_perm_names() {
  local name
  for name in "$@"; do
    case "$name" in
      bluetooth) echo android.permission.BLUETOOTH_SCAN android.permission.BLUETOOTH_CONNECT ;;
      location) echo android.permission.ACCESS_FINE_LOCATION android.permission.ACCESS_COARSE_LOCATION ;;
      notifications) echo android.permission.POST_NOTIFICATIONS ;;
      *.*) echo "$name" ;;
      *) echo "android.permission.$name" ;;
    esac
  done | tr ' ' '\n'
}

cmd_perm() {
  need_platform
  local action="${1:-}"; shift || true
  [[ "$PLATFORM" == desktop ]] && die "perm на desktop не поддерживается: разрешения выдаёт macOS"
  if [[ "$PLATFORM" == ios ]]; then
    [[ -n "$action" ]] || die "perm без аргументов на iOS не поддерживается: simctl не показывает состояние разрешений"
    [[ "$action" =~ ^(grant|revoke|reset)$ ]] || die "perm grant|revoke|reset <name>"
    if [[ "$action" == reset && $# -eq 0 ]]; then
      xcrun simctl privacy "$DEVICE" reset all "$(app_id)" || exit 1
      echo "reset all"; return 0
    fi
    local name
    for name in "$@"; do
      # Bluetooth в simctl privacy нет, да и BLE на симуляторе нет.
      [[ "$name" == location ]] || die "perm $name на iOS-симуляторе не поддерживается (simctl privacy: только location)"
      xcrun simctl privacy "$DEVICE" "$action" location "$(app_id)" || exit 1
      echo "$action location"
    done
    return 0
  fi
  local runtime p applied=0 was_running
  runtime="$(android_runtime_perms)"
  was_running="$(adb_ shell pidof "$(app_id)" | tr -d '\r')"
  case "$action" in
    "")
      [[ -n "$runtime" ]] && echo "$runtime" | sed -E 's/^android\.permission\.//' || echo "runtime-разрешений нет"
      return 0 ;;
    grant | revoke)
      [[ $# -gt 0 ]] || die "perm $action <bluetooth|location|notifications|android.permission.X> ..."
      # for, а не while read: adb shell внутри цикла съел бы его stdin.
      for p in $(android_perm_names "$@"); do
        if ! grep -q "^$p:" <<<"$runtime"; then
          echo "skip ${p#android.permission.}: приложение не запрашивает его на этом API"
          continue
        fi
        adb_ shell pm "$action" "$(app_id)" "$p" || exit 1
        echo "$action ${p#android.permission.}"
        applied=1
      done
      ((applied)) || die "ни одно разрешение не изменено"
      ;;
    reset)
      # Отзыв + сброс «пользователь отказал / больше не спрашивать»: системный диалог покажется снова.
      for p in $(cut -d: -f1 <<<"$runtime"); do
        adb_ shell pm revoke "$(app_id)" "$p" 2>/dev/null
        adb_ shell pm clear-permission-flags "$(app_id)" "$p" user-set user-fixed
      done
      echo "permissions reset"
      ;;
    *) die "perm [grant|revoke <name>... | reset]" ;;
  esac
  # Отзыв выданного разрешения система применяет, убивая процесс приложения.
  [[ -n "$was_running" && -z "$(adb_ shell pidof "$(app_id)" | tr -d '\r')" ]] \
    && echo "приложение остановлено системой — ui.sh launch"
  return 0
}

android_online() { adb_ shell ping -c1 -W2 8.8.8.8 >/dev/null 2>&1; }

cmd_net() {
  need_platform
  [[ "$PLATFORM" == android ]] || die "net на $PLATFORM не поддерживается: сеть хоста не отключается из ui.sh — кейс ⏭ на этой платформе"
  local want="" i
  case "${1:-status}" in
    # Не авиарежим: он выключает и Bluetooth, а нужно «нет интернета, BLE работает».
    off) adb_ shell svc wifi disable; adb_ shell svc data disable; want=offline ;;
    on) adb_ shell svc wifi enable; adb_ shell svc data enable; want=online ;;
    status) ;;
    *) die "net [on|off]" ;;
  esac
  # Переключение сети занимает несколько секунд.
  for ((i = 0; i < 15 && ${#want}; i++)); do
    if android_online; then [[ "$want" == online ]] && break
    else [[ "$want" == offline ]] && break; fi
    sleep 1
  done
  local wifi data
  wifi="$(adb_ shell settings get global wifi_on | tr -d '\r')"
  data="$(adb_ shell settings get global mobile_data | tr -d '\r')"
  echo "wifi=$([[ "$wifi" == 0 ]] && echo off || echo on) data=$([[ "$data" == 0 ]] && echo off || echo on) internet=$(android_online && echo yes || echo no)"
}

# --- доступность -----------------------------------------------------------------

cmd_a11y() {
  need_platform
  settle
  local density=""
  # uiautomator отдаёт px — пороги в dp пересчитываются по плотности экрана.
  [[ "$PLATFORM" == android ]] && density="--density $(adb_ shell wm density | awk -F': ' '{print $2}' | tail -1 | tr -d '\r')"
  python3 "$TREE" audit "$PLATFORM" "$STATE_DIR/dump.$(dump_ext)" "$STATE_DIR/tree.json" $(screen_arg) $density
}

# --- производительность ----------------------------------------------------------

# Память приложения в КБ: Android — TOTAL PSS, iOS-симулятор и desktop — RSS процесса на хосте.
app_mem_kb() {
  if [[ "$PLATFORM" == android ]]; then
    adb_ shell dumpsys meminfo "$(app_id)" | tr -d '\r' | awk '/TOTAL PSS:/ { print $3; exit }'
  else
    local pid="$DEVICE"
    [[ "$PLATFORM" == ios ]] && pid="$(pgrep -x "$IOS_PRODUCT" | head -1)"
    [[ -n "$pid" ]] && ps -o rss= -p "$pid" | tr -d ' '
  fi
}

cmd_perf() {
  need_platform
  # Пороги подобраны под эмулятор/симулятор: они шумят, поэтому это наблюдение, а не проверка (exit 0).
  local min_frames=30 max_janky=25 max_p95=100 max_mem_growth_mb=30
  local mem; mem="$(app_mem_kb)"
  [[ -n "$mem" ]] || die "процесс приложения не найден — ui.sh launch"
  if [[ "${1:-}" == reset ]]; then
    [[ "$PLATFORM" == android ]] && adb_ shell dumpsys gfxinfo "$(app_id)" reset >/dev/null
    PERF_MEM="$mem"
    save_state
    echo "perf reset: память $((mem / 1024)) МБ"
    return 0
  fi
  local warn=()
  if [[ "$PLATFORM" == android ]]; then
    local gfx frames janky p50 p90 p95 slow
    gfx="$(adb_ shell dumpsys gfxinfo "$(app_id)" | tr -d '\r')"
    frames="$(awk -F': ' '/^Total frames rendered/ { print $2; exit }' <<<"$gfx")"
    janky="$(sed -nE 's/^Janky frames: [0-9]+ \(([0-9]+)[.,]?[0-9]*%\).*/\1/p' <<<"$gfx" | head -1)"
    p50="$(sed -nE 's/^50th percentile: ([0-9]+)ms/\1/p' <<<"$gfx" | head -1)"
    p90="$(sed -nE 's/^90th percentile: ([0-9]+)ms/\1/p' <<<"$gfx" | head -1)"
    p95="$(sed -nE 's/^95th percentile: ([0-9]+)ms/\1/p' <<<"$gfx" | head -1)"
    slow="$(awk -F': ' '/^Number Slow UI thread/ { print $2; exit }' <<<"$gfx")"
    echo "кадры: ${frames:-0}, janky ${janky:-0}%, p50 ${p50:-?}ms p90 ${p90:-?}ms p95 ${p95:-?}ms, slow UI thread ${slow:-0}"
    if ((${frames:-0} >= min_frames)); then
      ((${janky:-0} > max_janky)) && warn+=("janky ${janky}% > ${max_janky}%")
      ((${p95:-0} > max_p95)) && warn+=("p95 ${p95}ms > ${max_p95}ms")
    else
      echo "кадров меньше $min_frames — плавность не оценивается"
    fi
    adb_ shell dumpsys meminfo "$(app_id)" | tr -d '\r' \
      | awk '/^ *Java Heap:/ { j = $3 } /^ *Native Heap:/ { n = $3 } END { printf "heap: Java %d МБ, Native %d МБ\n", j / 1024, n / 1024 }'
  else
    echo "кадры: на $PLATFORM не поддерживается"
  fi
  local line="память: $((mem / 1024)) МБ"
  if [[ -n "$PERF_MEM" ]]; then
    local growth=$(((mem - PERF_MEM) / 1024))
    line+=", прирост с reset ${growth} МБ"
    ((growth > max_mem_growth_mb)) && warn+=("память +${growth} МБ > ${max_mem_growth_mb} МБ")
  else
    line+=" (базы нет — ui.sh perf reset)"
  fi
  echo "$line"
  if ((${#warn[@]})); then
    local IFS=';'
    echo "perf: ⚠️ ${warn[*]}"
  else
    echo "perf: ✅"
  fi
}

# --- данные приложения (только чтение) -------------------------------------------

# «key<TAB>value» → «key = value»: фильтр по подстроке ключа, секреты маскируются.
mask_kv() {
  python3 -c '
import re, sys
flt = sys.argv[1].casefold()
secret = re.compile(r"token|password|passwd|secret|session|auth|cookie|(?<![a-z])pin(?![a-z])", re.I)
n = 0
for line in sys.stdin:
    key, _, value = line.rstrip("\n").partition("\t")
    if flt and flt not in key.casefold():
        continue
    if secret.search(key):
        value = f"*** ({len(value)} символов)" if value else "(пусто)"
    elif len(value) > 120:
        value = value[:120] + "…"
    print(f"{key} = {value}")
    n += 1
if not n:
    print("ничего не найдено" + (f" по фильтру {sys.argv[1]!r}" if flt else ""))
' "${1:-}"
}

run_as() {
  local out
  out="$(adb_ shell run-as "$(app_id)" "$@" 2>&1)"
  [[ "$out" == *"not debuggable"* ]] && die "сборка не debuggable — run-as недоступен"
  printf '%s\n' "$out" | LC_ALL=C tr -d '\r'
}

ios_container() {
  xcrun simctl get_app_container "$DEVICE" "$(app_id)" data 2>/dev/null || die "приложение не установлено на симуляторе"
}

cmd_data() {
  need_platform
  local sub="${1:-files}"; shift || true
  if [[ "$PLATFORM" == desktop ]]; then
    [[ "$sub" == prefs ]] || die "data $sub на desktop не поддерживается: путь хранения SDK неизвестен — есть только data prefs"
    desktop_prefs dump | mask_kv "${1:-}"
    return 0
  fi
  local root=""
  [[ "$PLATFORM" == ios ]] && root="$(ios_container)"
  case "$sub" in
    files)
      if [[ "$PLATFORM" == android ]]; then
        run_as sh -c "'find . -type f | grep -v -e ^./cache/ -e ^./code_cache/ | while read f; do echo \"\$(stat -c %s \"\$f\") \$f\"; done'"
      else
        (cd "$root" && find . -type f ! -path './tmp/*' ! -path './Library/Caches/*' -exec stat -f '%z %N' {} +)
      fi ;;
    prefs)
      if [[ "$PLATFORM" == android ]]; then
        local f
        for f in $(run_as ls shared_prefs 2>/dev/null | grep '\.xml$'); do
          run_as cat "shared_prefs/$f" | python3 -c '
import sys, xml.etree.ElementTree as ET
name = sys.argv[1][:-4]
for el in ET.fromstring(sys.stdin.read()):
    value = el.get("value") if el.get("value") is not None else (el.text or "")
    if el.tag == "set":
        value = ",".join(c.text or "" for c in el)
    key = el.get("name")
    print(f"{name}/{key}\t{value}")
' "$f"
        done | mask_kv "${1:-}"
      else
        local plist="$root/Library/Preferences/$(app_id).plist"
        [[ -f "$plist" ]] || { echo "настроек нет"; return 0; }
        plutil -convert json -o - "$plist" | python3 -c '
import json, sys
def walk(prefix, v):
    if isinstance(v, dict):
        for k, x in v.items():
            walk(f"{prefix}{k}/" if isinstance(x, dict) else f"{prefix}{k}", x)
    else:
        print(f"{prefix.rstrip(chr(47))}\t{v}")
walk("", json.load(sys.stdin))
' | mask_kv "${1:-}"
      fi ;;
    db)
      local file="${1:?data db <file> \"<SQL>\"}" sql="${2:?data db <file> \"<SQL>\"}" db
      if [[ "$PLATFORM" == android ]]; then
        [[ "$file" == */* ]] || file="databases/$file"
        db="$STATE_DIR/db/$(basename "$file")"
        rm -rf "$STATE_DIR/db"; mkdir -p "$STATE_DIR/db"
        # sqlite3 на устройстве нет: копия базы с журналами — и чтение на хосте.
        local existing ext
        existing="$(run_as ls "$(dirname "$file")")"
        grep -qx "$(basename "$file")" <<<"$existing" || die "нет базы $file (список — ui.sh data)"
        for ext in "" -wal -journal; do
          grep -qx "$(basename "$file$ext")" <<<"$existing" || continue
          adb_ exec-out run-as "$(app_id)" cat "$file$ext" >"$db$ext"
          [[ -s "$db$ext" ]] || rm -f "$db$ext"
        done
        [[ "$(head -c 15 "$db")" == "SQLite format 3" ]] || die "$file — не sqlite-база"
      else
        db="$root/$file"
        [[ -f "$db" ]] || db="$(find "$root" -name "$(basename "$file")" -type f | head -1)"
        [[ -f "$db" ]] || die "нет базы $file (список — ui.sh data)"
      fi
      sqlite3 -readonly -header -column "$db" "$sql" | head -100 ;;
    cat)
      local path="${1:?data cat <path>}"
      if [[ "$PLATFORM" == android ]]; then run_as cat "$path" | head -200
      # Бинарный plist (системные и NSUserDefaults) — в читаемый XML.
      elif [[ "$(head -c 6 "$root/$path")" == bplist ]]; then plutil -convert xml1 -o - "$root/$path" | head -200
      else head -200 "$root/$path"; fi ;;
    *) die "data [files] | prefs [filter] | db <file> \"<SQL>\" | cat <path>" ;;
  esac
}

# --- Maestro ---------------------------------------------------------------------

cmd_flow() {
  need_platform
  [[ "$PLATFORM" == desktop ]] && die "Maestro не поддерживает desktop — кейс проходится вручную (do / expect)"
  local out=""
  if [[ "${1:-}" == --out ]]; then out="$2"; shift 2; fi
  local yaml="${1:?flow <file.yaml>}"
  [[ -n "$out" ]] || out="$(dirname "$yaml")/out"
  local debug="$STATE_DIR/maestro-debug"
  rm -rf "$debug"; mkdir -p "$out"
  # takeScreenshot пишет только внутрь --test-output-dir.
  local args=(-e "APP_ID=$(app_id)" --no-ansi --test-output-dir "$out" --debug-output "$debug" --flatten-debug-output)
  if [[ -f "$CREDS" ]]; then
    while IFS='=' read -r k v; do
      [[ -z "$k" || "$k" == \#* ]] && continue
      args+=(-e "$k=$v")
    done <"$CREDS"
  fi
  maestro --device "$DEVICE" test "${args[@]}" "$@" 2>&1 | grep -vE "^\s*$|Analytics|analyze|[│╭╰]" | tail -40
  local rc="${PIPESTATUS[0]}"
  [[ "$PLATFORM" == ios ]] && ios_ax_on
  # Debug-артефакты (иерархия, скриншоты шагов, логи) нужны только для разбора падения.
  local keep; keep="$out/debug-$(basename "$yaml" .yaml)-$PLATFORM"
  rm -rf "$keep"
  if [[ "$rc" -ne 0 ]]; then
    mv "$debug" "$keep"
    echo "FAILED: debug — $keep"
  fi
  return "$rc"
}

cmd_hierarchy() {
  need_platform
  [[ "$PLATFORM" == desktop ]] && die "hierarchy на desktop не поддерживается — ui.sh tree --all"
  maestro --device "$DEVICE" hierarchy
}

cmd_help() {
  cat <<'EOF'
ui.sh — UI-тестирование Android (adb) / iOS-симулятор (simctl + AXe) / desktop macOS (ax.swift)

Окружение:
  doctor [android|ios|desktop]      проверка инструментов, устройства, кредов
  use android|ios|desktop [--device id] [--app-id id]
                                    --app-id — тестировать уже установленную сборку с другим id
  build | install | launch [sec] | stop | reset (очистить данные) | deeplink <uri>
                                    desktop: build — компиляция, install — не нужен, launch — gradle run в фоне
                                    (язык — из настроек приложения), reset — удаляет данные SDK и приложения из
                                    java.util.prefs (разлогин); deeplink / flow / key home — не поддерживаются

Экран:
  tree [--all]                      компактное дерево (ждёт стабильного экрана): [N] тип "текст" #testTag @x,y (флаги)
  tap <q> [sleep]                   q = @N | #id или id:foo (glob: #*back_button) | текст (без учёта регистра, частичное) | x,y
                                    #id в одиночной команде — в кавычках ("#id") или id:foo
  expect <q> [cond] [sec]           cond: visible (по умолч.) | gone | disabled | enabled | checked | focused | value=<текст>
                                    поллинг до sec (5); одна строка ✅/❌, при ❌ — дерево экрана
  do "<cmd>; <cmd>; ..."            пакет шагов, стоп на первой ошибке, напр. do "tap #a; type <QA_EMAIL>; expect #b"
  longtap <q> | type <text|<QA_KEY>> | clear [n] | key back|enter|home|hide-keyboard
                                    <QA_KEY> — значение из credentials.properties ({n} → timestamp)
                                    iOS: type — через буфер обмена (любая раскладка, кириллица), clear — всё поле
  swipe up|down|left|right          up — прокрутка контента вниз
  scroll-to <q> [max] [dir] | wait <q> [sec] | gone <q> [sec]
  shot <path.png> [maxpx]           скриншот (уменьшен до 1000px)
  a11y                              аудит экрана: no-label | no-testtag | small-target (48dp / 44pt) | dup-label

Логи:
  mark                              отметка времени (launch ставит сам)
  logs [lines] | crash              ошибки/крэши приложения с отметки
  wait-log <regex> [sec]            ждать строку в логе приложения с отметки (10 с); ✅/❌, работает в do

Разрешения и сеть:
  perm                              runtime-разрешения приложения (Android)
  perm grant|revoke <name>...       name: bluetooth | location | notifications | android.permission.X
                                    Android: revoke останавливает приложение; iOS: только location; desktop — нет
  perm reset                        как «ещё не спрашивали» — системный диалог покажется снова
  net [on|off]                      Android: Wi-Fi + мобильные данные (Bluetooth не трогает); без аргумента — статус

Производительность и данные:
  perf reset | perf                 база / кадры (Android: janky, p50–p95) и память с базы; вердикт ✅/⚠️, exit 0
  data [files]                      файлы приложения с размерами (Android — run-as, debug-сборка; iOS — контейнер)
  data prefs [filter]               настройки key = value, секреты (token, password, …) маскируются; desktop — только это
  data db <file> "<SQL>"            sqlite только на чтение (Android: databases/<file>)
  data cat <path>                   файл, первые 200 строк (iOS: бинарный plist → XML); db и cat не маскируют —
                                    секреты не выводить

Maestro:
  flow [--out dir] <file.yaml> [-e K=V ...]
                                    maestro test с APP_ID и кредами как -e; скриншоты в --out (по умолчанию
                                    <папка yaml>/out), debug-артефакты — только при падении
  hierarchy                         полное дерево Maestro (дорого, только для отладки)
EOF
}

cmd="${1:-help}"; shift || true
case "$cmd" in
  use) cmd_use "$@" ;;
  doctor) cmd_doctor "$@" ;;
  build) cmd_build ;;
  install) cmd_install ;;
  launch) cmd_launch "$@" ;;
  stop) cmd_stop ;;
  restart) cmd_stop >/dev/null; cmd_launch "$@" ;;
  reset) cmd_reset ;;
  deeplink) cmd_deeplink "$@" ;;
  tree) cmd_tree "$@" ;;
  tap) cmd_tap "$@" ;;
  expect) cmd_expect "$@" ;;
  do) cmd_do "$@" ;;
  longtap) cmd_longtap "$@" ;;
  type) cmd_type "$@" ;;
  clear) cmd_clear "$@" ;;
  key) cmd_key "$@" ;;
  swipe) cmd_swipe "$@" ;;
  scroll-to) cmd_scroll_to "$@" ;;
  wait) cmd_wait "$@" ;;
  gone) cmd_gone "$@" ;;
  shot) cmd_shot "$@" ;;
  mark) cmd_mark ;;
  logs) cmd_logs "$@" ;;
  crash) cmd_crash "$@" ;;
  perf) cmd_perf "$@" ;;
  data) cmd_data "$@" ;;
  wait-log) cmd_wait_log "$@" ;;
  perm) cmd_perm "$@" ;;
  net) cmd_net "$@" ;;
  a11y) cmd_a11y ;;
  flow) cmd_flow "$@" ;;
  hierarchy) cmd_hierarchy ;;
  help | -h | --help) cmd_help ;;
  *) die "неизвестная команда $cmd — ui.sh help" ;;
esac
