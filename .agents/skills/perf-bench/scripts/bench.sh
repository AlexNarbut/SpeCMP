#!/usr/bin/env bash
# perf-bench: замеры производительности утилитами, без модели. Справка: bench.sh help
# Совместим с bash 3.2 (macOS): без ассоциативных массивов и ${var,,}.
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_DIR="$(dirname "$SCRIPT_DIR")"
ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel 2>/dev/null || { cd "$SCRIPT_DIR/../../../.." && pwd; })"
OUT="$ROOT/.ai-perf/bench"
RUNS_DIR="$OUT/runs"
BASES_DIR="$OUT/baselines"
HISTORY="$OUT/history.jsonl"
REGISTRY="$SKILL_DIR/metrics-registry.json"
SCENARIOS="$SKILL_DIR/scenarios.json"
PROBE="$SCRIPT_DIR/startup_probe.py"
COLLECT="$SCRIPT_DIR/collect.py"
REPORT="$SCRIPT_DIR/report.py"
APKANALYZER="${APKANALYZER:-$HOME/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer}"
TRACE_CFG="$SCRIPT_DIR/perfetto-startup.cfg"
TRACE_METRICS="$SCRIPT_DIR/trace_metrics.py"
# Пакет perfetto (trace_processor) живёт в venv внутри вывода скила: системный python не трогаем.
TRACE_VENV="$OUT/.venv"
TRACE_PY="$TRACE_VENV/bin/python"

PLATFORMS="android" LABEL="" SCOPE="static,startup,trace" BUILD=1 BASELINE_REF=""
RUNS_N="" SETTLE_S="" ANDROID_DEVICE="" IOS_DEVICE="" APK_OVERRIDE=""
ACTIVITY="ru.alarmtrade.specmp.androidApp.AppActivity"
IOS_PRODUCT="SpeCMP"

die() { echo "ERROR: $*" >&2; exit 1; }
log() { echo "• $*"; }
cap() { echo "$(tr '[:lower:]' '[:upper:]' <<<"${1:0:1}")${1:1}"; }
have() { command -v "$1" >/dev/null 2>&1; }
json_get() { python3 -c 'import json,sys; d=json.load(open(sys.argv[1]))
for k in sys.argv[2].split("."): d=d[k]
print(d)' "$1" "$2"; }

# --- параметры ---------------------------------------------------------------------

parse_opts() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --platform) PLATFORMS="$2"; shift 2 ;;
      --label) LABEL="$2"; shift 2 ;;
      --scope) SCOPE="$2"; shift 2 ;;
      --runs) RUNS_N="$2"; shift 2 ;;
      --settle) SETTLE_S="$2"; shift 2 ;;
      --no-build) BUILD=0; shift ;;
      --baseline) BASELINE_REF="$2"; shift 2 ;;
      --android-device) ANDROID_DEVICE="$2"; shift 2 ;;
      --apk) APK_OVERRIDE="$2"; BUILD=0; shift 2 ;;
      --ios-device) IOS_DEVICE="$2"; shift 2 ;;
      *) die "неизвестный флаг $1 (bench.sh help)" ;;
    esac
  done
  [[ "$PLATFORMS" == both ]] && PLATFORMS="android,ios"
  for p in $(tr ',' ' ' <<<"$PLATFORMS"); do
    [[ "$p" == android || "$p" == ios ]] || die "--platform android|ios|both"
  done
  [[ -n "$RUNS_N" ]] || RUNS_N="$(json_get "$SCENARIOS" startup.runs)"
  [[ -n "$SETTLE_S" ]] || SETTLE_S="$(json_get "$SCENARIOS" startup.settle_s)"
}
has_platform() { [[ ",$PLATFORMS," == *",$1,"* ]]; }
has_scope() { [[ ",$SCOPE," == *",$1,"* ]]; }

# Сценарии фич из --scope: feature:<name>, feature:all, feature:auto (по модулям, изменённым относительно базы).
feature_names() {
  local s names=""
  for s in $(tr ',' ' ' <<<"$SCOPE"); do
    case "$s" in
      feature:all) names="$names $(python3 -c 'import json,sys; print(" ".join(json.load(open(sys.argv[1]))["features"]))' "$SCENARIOS")" ;;
      feature:auto) names="$names $(auto_features)" ;;
      feature:*) names="$names ${s#feature:}" ;;
    esac
  done
  tr ' ' '\n' <<<"$names" | grep -v '^$' | sort -u
}
# Модуль — путь до src/: feature/device/firmware, core/ui, composeApp. Файлы вне src/ (gradle, ресурсы сборки) не считаются.
changed_modules() {
  local mb; mb="$(git -C "$ROOT" merge-base HEAD develop 2>/dev/null || true)"
  { [[ -n "$mb" ]] && git -C "$ROOT" diff --name-only "$mb" HEAD; git -C "$ROOT" diff --name-only HEAD; git -C "$ROOT" ls-files --others --exclude-standard; } 2>/dev/null \
    | sed -n 's#^\(.*\)/src/.*#\1#p' | sort -u
}
# Модуль сценария покрывает и вложенные: feature/device — это и feature/device/firmware.
auto_features() {
  changed_modules | python3 -c 'import json,sys
features = json.load(open(sys.argv[1]))["features"]
changed = sys.stdin.read().split()
hit = lambda mods: any(c == m or c.startswith(m + "/") for c in changed for m in mods)
print(" ".join(n for n, f in features.items() if hit(f["modules"])))' "$SCENARIOS"
}
feature_field() { python3 -c 'import json,sys; f=json.load(open(sys.argv[1]))["features"].get(sys.argv[2]); print("" if f is None else f.get(sys.argv[3], sys.argv[4] if len(sys.argv) > 4 else ""))' "$SCENARIOS" "$@"; }
maestro_args() {
  local creds="$SKILL_DIR/credentials.properties" k v
  [[ -f "$creds" ]] || creds="$ROOT/.agents/skills/perf-bench/credentials.properties"
  # Тот же QA-аккаунт, что у ui-test: отдельный файл не обязателен.
  [[ -f "$creds" ]] || creds="$ROOT/.agents/skills/ui-test/credentials.properties"
  MAESTRO_ARGS=(-e "APP_ID=$1" --no-ansi)
  [[ -f "$creds" ]] || return 0
  while IFS='=' read -r k v; do
    [[ -z "$k" || "$k" == \#* ]] && continue
    MAESTRO_ARGS+=(-e "$k=$v")
  done <"$creds"
}
now_ms() { python3 -c 'import time; print(int(time.monotonic() * 1000))'; }

# --- идентификаторы сборки -------------------------------------------------------

android_app_id() { echo "ru.alarmtrade.pandoraspecialist"; }
ios_app_id() { echo "ru.alarmtrade.specialist"; }
apk_path() {
  [[ -n "$APK_OVERRIDE" ]] && { echo "$APK_OVERRIDE"; return; }
  ls "$ROOT"/androidApp/build/outputs/apk/google/debug/*.apk 2>/dev/null | head -1
}
ios_scheme() { echo "iosApp"; }
ios_config() { echo "Debug"; }
ios_derived() { echo "$ROOT/iosApp/build/perf-bench"; }
ios_app_path() { echo "$(ios_derived)/Build/Products/$(ios_config)-iphonesimulator/$IOS_PRODUCT.app"; }

adb_() { adb -s "$ANDROID_DEVICE" "$@"; }

pick_android_device() {
  [[ -n "$ANDROID_DEVICE" ]] || ANDROID_DEVICE="$(adb devices | awk 'NR>1 && $2=="device"{print $1; exit}')"
  [[ -n "$ANDROID_DEVICE" ]] || die "нет Android-устройства (adb devices) — запусти эмулятор"
}
pick_ios_device() {
  [[ -n "$IOS_DEVICE" ]] || IOS_DEVICE="$(xcrun simctl list devices booted -j | python3 -c 'import json,sys
d=json.load(sys.stdin)["devices"]
print(next((x["udid"] for v in d.values() for x in v if x["state"]=="Booted"),""))')"
  [[ -n "$IOS_DEVICE" ]] || die "нет запущенного iOS-симулятора: xcrun simctl list devices available; xcrun simctl boot <udid>; open -a Simulator"
}

# --- doctor ------------------------------------------------------------------------

cmd_doctor() {
  local fail=0
  ok() { echo "✅ $1"; }
  bad() { echo "❌ $1 — $2"; fail=1; }
  warn() { echo "⚠️  $1 — $2"; }
  parse_opts "$@"
  have python3 && ok "python3 $(python3 --version 2>&1 | awk '{print $2}')" || bad "python3" "brew install python"
  have java && ok "java" || bad "java" "нужен Gradle: brew install openjdk@17"
  have git && ok "git" || bad "git" "xcode-select --install"
  if has_platform android; then
    have adb && ok "adb" || bad "adb" "Android SDK platform-tools в PATH"
    [[ -x "$APKANALYZER" ]] && ok "apkanalyzer" || bad "apkanalyzer" "sdkmanager 'cmdline-tools;latest' или APKANALYZER=<путь>"
    if have adb; then
      local devs; devs="$(adb devices | awk 'NR>1 && $2=="device"{print $1}' | tr '\n' ' ')"
      [[ -n "$devs" ]] && ok "Android-устройства: $devs" || bad "нет Android-устройства" "emulator -list-avds; emulator -avd <name> &"
    fi
  fi
  if has_platform ios; then
    have xcrun && ok "xcrun" || bad "xcrun" "xcode-select --install"
    have xcodebuild && ok "$(xcodebuild -version 2>/dev/null | head -1)" || bad "xcodebuild" "установи Xcode"
    have axe && ok "axe" || bad "axe" "brew install cameroncooke/axe/axe"
    local booted; booted="$(xcrun simctl list devices booted 2>/dev/null | grep -E '\(Booted\)' | sed 's/^ *//')"
    [[ -n "$booted" ]] && ok "симулятор: $booted" || bad "нет запущенного симулятора" "xcrun simctl boot <udid>; open -a Simulator"
    pgrep -x Simulator >/dev/null && ok "Simulator.app открыт" || bad "Simulator.app закрыт" "open -a Simulator (без окна axe не читает дерево)"
  fi
  have maestro && ok "maestro (сценарии фич)" || warn "maestro не найден" "нужен только для сценариев фич: brew tap mobile-dev-inc/tap && brew install maestro"
  if [[ -x "$TRACE_PY" ]] && "$TRACE_PY" -c 'import perfetto' 2>/dev/null; then ok "perfetto trace_processor ($TRACE_VENV)"
  else warn "нет trace_processor" "scope trace будет пропущен; установка: python3 -m venv $TRACE_VENV && $TRACE_VENV/bin/pip install perfetto"; fi
  [[ -d "$OUT" ]] || mkdir -p "$RUNS_DIR" "$BASES_DIR"
  ok "вывод: $OUT"
  [[ $fail -eq 0 ]] && echo "OK: окружение готово" || { echo "FAIL: исправь пункты ❌ и повтори"; exit 1; }
}

# --- прогон --------------------------------------------------------------------------

new_run_dir() {
  local kind="$1" sha7 ts name
  sha7="$(git -C "$ROOT" rev-parse --short HEAD)"
  ts="$(date +%Y-%m-%d_%H%M%S)"
  name="$ts-$sha7-$kind"
  [[ -n "$LABEL" ]] && name="$name-$(tr -c 'A-Za-z0-9_.\n-' '-' <<<"$LABEL")"
  RUN_DIR="$RUNS_DIR/$name"
  mkdir -p "$RUN_DIR/raw"
}

write_meta() {
  local kind="$1" dirty merge_base
  dirty=0; [[ -n "$(git -C "$ROOT" status --porcelain --untracked-files=no)" ]] && dirty=1
  merge_base="$(git -C "$ROOT" merge-base HEAD develop 2>/dev/null || true)"
  cat >"$RUN_DIR/meta.txt" <<EOF
run_id=$(basename "$RUN_DIR")
kind=$kind
label=$LABEL
created=$(date +%Y-%m-%dT%H:%M:%S)
sha=$(git -C "$ROOT" rev-parse HEAD)
sha7=$(git -C "$ROOT" rev-parse --short HEAD)
branch=$(git -C "$ROOT" rev-parse --abbrev-ref HEAD)
dirty=$dirty
merge_base=$merge_base
platforms=$PLATFORMS
scope=$SCOPE
runs=$RUNS_N
settle_s=$SETTLE_S
host=$(uname -m) macOS $(sw_vers -productVersion 2>/dev/null)
EOF
}

# Сборка APK: обязательна, иначе меряется старый код. Время сборки — справочная метрика.
# scope compose подключает init-скрипт compose-reports.init.gradle: компилятор пишет отчёты стабильности
# в <модуль>/build/compose-reports (полная перекомпиляция Compose-модулей).
android_build() {
  local t0 t1 props=()
  has_scope compose && props=(--init-script "$SCRIPT_DIR/compose-reports.init.gradle")
  log "Android: сборка :androidApp:assembleGoogleDebug ${props[*]:-}"
  t0=$(date +%s)
  (cd "$ROOT" && ./gradlew ":androidApp:assembleGoogleDebug" -q "${props[@]+"${props[@]}"}") >"$RUN_DIR/raw/build-android.log" 2>&1 \
    || { grep -E "^e: |What went wrong" -A3 "$RUN_DIR/raw/build-android.log" | head -40; die "сборка упала, лог: $RUN_DIR/raw/build-android.log"; }
  t1=$(date +%s)
  echo $((t1 - t0)) >"$RUN_DIR/raw/static/build-time.txt"
}

static_collect() {
  local apk; mkdir -p "$RUN_DIR/raw/static"
  apk="$(apk_path)"
  [[ -n "$apk" ]] || die "APK не найден: $ROOT/androidApp/build/outputs/apk/google/debug/"
  log "static: APK $(basename "$apk")"
  echo "$apk" >"$RUN_DIR/raw/static/apk.path"
  "$APKANALYZER" apk download-size "$apk" >"$RUN_DIR/raw/static/apk-download-size.txt" 2>/dev/null || echo "" >"$RUN_DIR/raw/static/apk-download-size.txt"
  log "static: dex packages (apkanalyzer, ~10 с)"
  "$APKANALYZER" dex packages --defined-only "$apk" >"$RUN_DIR/raw/static/dex-packages.txt" 2>/dev/null || true
  log "static: строки Kotlin, отчёты detekt и тестов"
  (cd "$ROOT" && git ls-files -z '*.kt' | xargs -0 wc -l) >"$RUN_DIR/raw/static/kt-lines.txt" 2>/dev/null
  (cd "$ROOT" && find . -path '*/build/reports/detekt/*.xml' -not -path './build-logic/*' 2>/dev/null) >"$RUN_DIR/raw/static/detekt-reports.txt"
  (cd "$ROOT" && find . -path '*/build/test-results/*' -name 'TEST-*.xml' -not -path './build-logic/*' 2>/dev/null) >"$RUN_DIR/raw/static/test-reports.txt"
  if has_scope compose; then
    (cd "$ROOT" && find . -path '*/build/compose-reports/*' \( -name '*-module.json' -o -name '*-classes.txt' -o -name '*-composables.csv' \) -not -path './build-logic/*' 2>/dev/null) >"$RUN_DIR/raw/static/compose-reports.txt"
    [[ -s "$RUN_DIR/raw/static/compose-reports.txt" ]] || log "static: отчётов compose-компилятора нет — нужна сборка с init-скриптом (без --no-build)"
  fi
}

android_env() {
  local d="$RUN_DIR/raw/android"; mkdir -p "$d"
  {
    echo "device=$ANDROID_DEVICE"
    echo "model=$(adb_ shell getprop ro.product.model | tr -d '\r')"
    echo "sdk=$(adb_ shell getprop ro.build.version.sdk | tr -d '\r')"
    echo "avd=$(adb_ emu avd name 2>/dev/null | head -1 | tr -d '\r')"
    echo "cpus=$(adb_ shell grep -c processor /proc/cpuinfo | tr -d '\r')"
    echo "anim_scale=$(adb_ shell settings get global window_animation_scale | tr -d '\r')/$(adb_ shell settings get global transition_animation_scale | tr -d '\r')/$(adb_ shell settings get global animator_duration_scale | tr -d '\r')"
    echo "app_id=$(android_app_id)"
    echo "clk_tck=$(adb_ shell getconf CLK_TCK | tr -d '\r')"
  } >"$d/env.txt"
}

# Окно простоя: после всех дампов, без действий — кадры здесь означают зацикленную анимацию, видео или вечный лоадер.
android_idle_frames() {
  local d="$1" idle; idle="$(json_get "$SCENARIOS" startup.idle_s)"
  adb_ shell dumpsys gfxinfo "$(android_app_id)" reset >/dev/null
  sleep "$idle"
  adb_ shell dumpsys gfxinfo "$(android_app_id)" | tr -d '\r' >"$d/gfxinfo-idle.txt"
  echo "idle_s=$idle" >>"$d/sample.txt"
}

# Один замер старта: force-stop → метка logcat → gfxinfo reset → probe (am start + ожидание тега) → settle → дампы.
android_startup_sample() {
  local i="$1" d="$RUN_DIR/raw/android/sample-$i" pkg mark pid tag timeout
  pkg="$(android_app_id)"
  tag="$(json_get "$SCENARIOS" startup.content_tag)"
  timeout="$(json_get "$SCENARIOS" startup.content_timeout_s)"
  mkdir -p "$d"
  adb_ shell am force-stop "$pkg"
  sleep 2
  mark="$(adb_ shell date +'%m-%d\ %H:%M:%S.000' | tr -d '\r')"
  adb_ shell dumpsys gfxinfo "$pkg" reset >/dev/null
  python3 "$PROBE" android "$ANDROID_DEVICE" "$pkg" "$ACTIVITY" "$tag" "$timeout" >"$d/sample.txt"
  sleep "$SETTLE_S"
  pid="$(adb_ shell pidof "$pkg" | tr -d '\r')"
  {
    echo "settle_s=$SETTLE_S"
    echo "mark=$mark"
    echo "pid=${pid:-}"
  } >>"$d/sample.txt"
  adb_ shell dumpsys gfxinfo "$pkg" | tr -d '\r' >"$d/gfxinfo.txt"
  adb_ shell dumpsys meminfo "$pkg" | tr -d '\r' >"$d/meminfo.txt"
  if [[ -n "$pid" ]]; then
    adb_ shell cat "/proc/$pid/stat" | tr -d '\r' >"$d/proc-stat.txt"
    adb_ shell cat "/proc/$pid/task/$pid/stat" | tr -d '\r' >"$d/proc-main-stat.txt"
    adb_ logcat -d -T "$mark" --pid="$pid" 2>/dev/null | tr -d '\r' >"$d/logcat.txt"
  else
    : >"$d/proc-stat.txt"; : >"$d/proc-main-stat.txt"; : >"$d/logcat.txt"
  fi
  adb_ logcat -d -b crash -T "$mark" 2>/dev/null | tr -d '\r' | grep -v '^-----' >"$d/crash.txt"
  adb_ logcat -d -T "$mark" 2>/dev/null | tr -d '\r' | grep -E "ANR in $pkg" >"$d/anr.txt"
  # «Fully drawn» пишет system_server, не процесс приложения — берём без фильтра по pid.
  adb_ logcat -d -T "$mark" 2>/dev/null | tr -d '\r' | grep -E "Fully drawn $pkg" >"$d/fully-drawn.txt"
  android_idle_frames "$d"
  echo "  #$i $(grep -E '^(launch_state|ttid_ms|ttfc_ms)=' "$d/sample.txt" | tr '\n' ' ')ttfd=$(sed -nE 's/.*Fully drawn .*: \+(.*)$/\1/p' "$d/fully-drawn.txt" | head -1) idle=$(sed -nE 's/^Total frames rendered: //p' "$d/gfxinfo-idle.txt")"
}

# Первый экран зависит от состояния (вход / главная): без предусловия замеры старта несравнимы.
# Предусловие — Maestro-флоу из scenarios.json → startup.ensure_flow (залогиниться QA-аккаунтом, если нужно).
android_ensure_first_screen() {
  local flow pkg; flow="$(json_get "$SCENARIOS" startup.ensure_flow 2>/dev/null)"; pkg="$(android_app_id)"
  [[ -n "$flow" && "$flow" != None ]] || return 0
  if ! have maestro; then log "startup: maestro не найден — предусловие первого экрана ($flow) пропущено"; return 0; fi
  maestro_args "$pkg"
  if maestro --device "$ANDROID_DEVICE" test "${MAESTRO_ARGS[@]}" "$SKILL_DIR/$flow" >"$RUN_DIR/raw/android/ensure-first-screen.log" 2>&1; then
    log "startup: первый экран зафиксирован ($flow)"
  else
    log "startup: предусловие первого экрана не выполнилось — см. raw/android/ensure-first-screen.log"
  fi
  adb_ shell am force-stop "$pkg"
}

android_startup() {
  local n="$RUNS_N" warm i
  warm="$(json_get "$SCENARIOS" startup.warmup)"
  pick_android_device
  android_env
  log "Android: установка $(basename "$(apk_path)") на $ANDROID_DEVICE"
  adb_ install -r "$(apk_path)" >/dev/null || die "adb install упал"
  adb_ shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1
  android_ensure_first_screen
  log "Android: старт — прогрев $warm, замеров $n, settle ${SETTLE_S}s, простой $(json_get "$SCENARIOS" startup.idle_s)s"
  for ((i = 1; i <= warm; i++)); do android_startup_sample "w$i"; done
  for ((i = 1; i <= n; i++)); do android_startup_sample "$i"; done
  adb_ shell am force-stop "$(android_app_id)"
}

# --- Android: Perfetto-трейс старта ---------------------------------------------------

# Один запуск под трейсом после обычных замеров: трейс даёт оверхед, в TTID-статистику не входит.
android_trace() {
  local d="$RUN_DIR/raw/android/trace" pkg duration tag timeout trace_dev="/data/misc/perfetto-traces/perf-bench.pftrace"
  if ! { [[ -x "$TRACE_PY" ]] && "$TRACE_PY" -c 'import perfetto' 2>/dev/null; }; then
    log "trace: нет trace_processor — пропущено (bench.sh doctor покажет установку)"
    return 0
  fi
  pkg="$(android_app_id)"; duration="$(json_get "$SCENARIOS" trace.duration_s)"
  tag="$(json_get "$SCENARIOS" startup.content_tag)"; timeout="$(json_get "$SCENARIOS" startup.content_timeout_s)"
  pick_android_device
  [[ -f "$RUN_DIR/raw/android/env.txt" ]] || android_env
  has_scope startup || { adb_ install -r "$(apk_path)" >/dev/null 2>&1; android_ensure_first_screen; }
  mkdir -p "$d"
  log "Android: Perfetto-трейс старта (${duration}s)"
  adb_ shell am force-stop "$pkg"; sleep 2
  sed -e "s/APP_ID_PLACEHOLDER/$pkg/" -e "s/DURATION_PLACEHOLDER/$((duration * 1000))/" "$TRACE_CFG" >"$d/perfetto.cfg"
  adb_ shell perfetto -c - --txt -o "$trace_dev" <"$d/perfetto.cfg" >"$d/perfetto.log" 2>&1 &
  local perfetto_pid=$!
  sleep 2
  python3 "$PROBE" android "$ANDROID_DEVICE" "$pkg" "$ACTIVITY" "$tag" "$timeout" >"$d/sample.txt"
  wait "$perfetto_pid"
  adb_ pull "$trace_dev" "$d/startup.pftrace" >/dev/null 2>&1 || { log "trace: не удалось забрать трейс ($d/perfetto.log)"; return 0; }
  adb_ shell rm -f "$trace_dev"
  "$TRACE_PY" "$TRACE_METRICS" "$d/startup.pftrace" "$pkg" "$d/metrics.json" >>"$d/perfetto.log" 2>&1 \
    || log "trace: анализ упал, см. $d/perfetto.log"
  adb_ shell am force-stop "$pkg"
  echo "  trace: $(grep -E '^(ttid_ms|ttfc_ms)=' "$d/sample.txt" | tr '\n' ' ')→ $d/startup.pftrace ($(du -k "$d/startup.pftrace" | cut -f1) КБ)"
  android_compose_trace "$d"
}

# Трейс первой композиции главной с именами composable (androidx.compose.runtime:runtime-tracing в debug-сборке).
# Холодный путь включения SDK (ENABLE_TRACING_COLD_START) в tracing-perfetto 1.0 на API 36 падает внутри
# библиотеки, поэтому SDK включается в живом процессе, а первая композиция воспроизводится новым экземпляром
# Activity. В SpeCMP runtime-tracing не подключён, а AppActivity — singleTask (новый экземпляр не создаётся):
# шаг пропускается с заметкой, пока в debug-сборку не добавят runtime-tracing и отдельный способ пересоздать экран.
android_compose_trace() {
  local d="$1" pkg trace_dev="/data/misc/perfetto-traces/perf-bench-compose.pftrace" rc
  pkg="$(android_app_id)"
  adb_ shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n "$pkg/$ACTIVITY" >/dev/null 2>&1
  android_wait_content; sleep 4
  rc="$(adb_ shell am broadcast -a androidx.tracing.perfetto.action.ENABLE_TRACING -n "$pkg/androidx.tracing.perfetto.TracingReceiver" 2>/dev/null | grep -o '"exitCode":[0-9]*' | head -1)"
  if [[ "$rc" != '"exitCode":1' && "$rc" != '"exitCode":2' ]]; then
    log "trace: Compose runtime-tracing недоступен (${rc:-нет получателя}) — композиционный трейс пропущен"
    adb_ shell am force-stop "$pkg"
    return 0
  fi
  sed -e "s/APP_ID_PLACEHOLDER/$pkg/" -e "s/DURATION_PLACEHOLDER/9000/" "$SCRIPT_DIR/perfetto-compose.cfg" >"$d/perfetto-compose.cfg"
  local repeats i perfetto_pid traces=()
  repeats="$(json_get "$SCENARIOS" trace.compose_repeats 2>/dev/null)"; [[ "$repeats" =~ ^[0-9]+$ ]] || repeats=1
  # Каждый повтор — новый экземпляр MainActivity поверх предыдущих: та же начальная композиция главной.
  for ((i = 1; i <= repeats; i++)); do
    adb_ shell perfetto -c - --txt -o "$trace_dev" <"$d/perfetto-compose.cfg" >>"$d/perfetto.log" 2>&1 &
    perfetto_pid=$!
    sleep 2
    adb_ shell am start -W -n "$pkg/$ACTIVITY" >>"$d/compose-start.txt" 2>&1
    wait "$perfetto_pid"
    adb_ pull "$trace_dev" "$d/compose-$i.pftrace" >/dev/null 2>&1 || { log "trace: не удалось забрать композиционный трейс #$i"; continue; }
    adb_ shell rm -f "$trace_dev"
    "$TRACE_PY" "$SCRIPT_DIR/compose_metrics.py" "$d/compose-$i.pftrace" "$pkg" "$d/compose-metrics-$i.json" >>"$d/perfetto.log" 2>&1 \
      && traces+=("$d/compose-metrics-$i.json") || log "trace: анализ композиционного трейса #$i упал, см. $d/perfetto.log"
  done
  adb_ shell am force-stop "$pkg"
  [[ ${#traces[@]} -gt 0 ]] && python3 "$SCRIPT_DIR/compose_aggregate.py" "$d/compose-metrics.json" "${traces[@]}" >>"$d/perfetto.log" 2>&1
  echo "  compose trace: $(python3 -c 'import json,sys; d=json.load(open(sys.argv[1])); print("кадр", d.get("home_frame_ms"), "ms, композиция", d.get("home_compose_ms"), "ms, draw", d.get("home_draw_ms"), "ms, measure", d.get("home_measure_ms"), "ms")' "$d/compose-metrics.json" 2>/dev/null)"
}

# --- Android: сценарии фич -----------------------------------------------------------

android_wait_content() {
  local tag timeout; tag="$(json_get "$SCENARIOS" startup.content_tag)"; timeout="$(json_get "$SCENARIOS" startup.content_timeout_s)"
  python3 "$PROBE" android-wait "$ANDROID_DEVICE" "$tag" "$timeout" >/dev/null
}
android_ensure_running() {
  local pkg; pkg="$(android_app_id)"
  if [[ -z "$(adb_ shell pidof "$pkg" | tr -d '\r')" ]]; then
    # Только launcher-интент: после «am start -n» каждый launchApp Maestro кладёт в задачу новую Activity
    # (корень задачи не совпадает с launcher-интентом) — память росла на экземпляр за повтор.
    adb_ shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n "$pkg/$ACTIVITY" >/dev/null 2>&1
    android_wait_content
    # Главная догружает ленту ещё несколько секунд: тап по переключателю раньше теряется.
    sleep 5
  fi
}
# Один повтор сценария: метка → gfxinfo reset → /proc до → maestro → /proc после → gfxinfo → принудительный GC → meminfo → логи.
android_feature_repeat() {
  local name="$1" i="$2" d="$RUN_DIR/raw/android/feature-$1/repeat-$2" pkg flow pid pid_after mark t0 t1 rc
  pkg="$(android_app_id)"; flow="$SKILL_DIR/$(feature_field "$name" flow)"
  mkdir -p "$d"
  [[ "$(feature_field "$name" reset_each False)" == True ]] && { adb_ shell am force-stop "$pkg"; adb_ shell pm clear "$pkg" >/dev/null; } || android_ensure_running
  pid="$(adb_ shell pidof "$pkg" | tr -d '\r')"
  mark="$(adb_ shell date +'%m-%d\ %H:%M:%S.000' | tr -d '\r')"
  adb_ shell dumpsys gfxinfo "$pkg" reset >/dev/null
  [[ -n "$pid" ]] && adb_ shell cat "/proc/$pid/stat" | tr -d '\r' >"$d/proc-stat-before.txt"
  [[ -n "$pid" ]] && adb_ shell cat "/proc/$pid/task/$pid/stat" | tr -d '\r' >"$d/proc-main-stat-before.txt"
  maestro_args "$pkg"
  t0=$(now_ms)
  maestro --device "$ANDROID_DEVICE" test "${MAESTRO_ARGS[@]}" --test-output-dir "$d/maestro" --debug-output "$d/maestro-debug" --flatten-debug-output "$flow" \
    >"$d/maestro.log" 2>&1
  rc=$?
  t1=$(now_ms)
  [[ $rc -eq 0 ]] && rm -rf "$d/maestro-debug"
  pid_after="$(adb_ shell pidof "$pkg" | tr -d '\r')"
  {
    echo "flow_ms=$((t1 - t0))"; echo "flow_rc=$rc"; echo "mark=$mark"; echo "pid=${pid:-}"; echo "pid_after=${pid_after:-}"
  } >"$d/sample.txt"
  if [[ -n "$pid_after" ]]; then
    adb_ shell cat "/proc/$pid_after/stat" | tr -d '\r' >"$d/proc-stat-after.txt"
    adb_ shell cat "/proc/$pid_after/task/$pid_after/stat" | tr -d '\r' >"$d/proc-main-stat-after.txt"
    adb_ shell dumpsys gfxinfo "$pkg" | tr -d '\r' >"$d/gfxinfo.txt"
    # SIGUSR1 — ART делает полный GC: память сравнивается без мусора, накопленного за сценарий.
    adb_ shell run-as "$pkg" kill -10 "$pid_after" 2>/dev/null
    sleep 2
    adb_ shell dumpsys meminfo "$pkg" | tr -d '\r' >"$d/meminfo.txt"
    adb_ logcat -d -T "$mark" --pid="$pid_after" 2>/dev/null | tr -d '\r' >"$d/logcat.txt"
  fi
  adb_ logcat -d -b crash -T "$mark" 2>/dev/null | tr -d '\r' | grep -v '^-----' >"$d/crash.txt"
  adb_ logcat -d -T "$mark" 2>/dev/null | tr -d '\r' | grep -E "ANR in $pkg" >"$d/anr.txt"
  [[ -n "$pid_after" ]] && android_idle_frames "$d"
  echo "  $name #$i: $((t1 - t0)) ms, rc=$rc, idle $(sed -nE 's/^Total frames rendered: //p' "$d/gfxinfo-idle.txt" 2>/dev/null)$([[ "$pid" != "$pid_after" ]] && echo ', процесс сменился')"
}

android_features() {
  local names name repeats i
  names="$(feature_names)"
  [[ -n "$names" ]] || { log "feature: сценариев для прогона нет (feature:auto — изменённые модули не входят ни в один сценарий)"; return 0; }
  pick_android_device
  [[ -f "$RUN_DIR/raw/android/env.txt" ]] || android_env
  if ! has_scope startup; then
    log "Android: установка $(basename "$(apk_path)")"
    adb_ install -r "$(apk_path)" >/dev/null || die "adb install упал"
  fi
  for name in $names; do
    [[ -n "$(feature_field "$name" flow)" ]] || die "сценария $name нет в scenarios.json"
    repeats="$(feature_field "$name" repeats 3)"
    mkdir -p "$RUN_DIR/raw/android/feature-$name"
    { echo "name=$name"; echo "title=$(feature_field "$name" title)"; echo "repeats=$repeats"; echo "flow=$(feature_field "$name" flow)"; echo "reset_each=$(feature_field "$name" reset_each False)"; } >"$RUN_DIR/raw/android/feature-$name/scenario.txt"
    log "Android: сценарий $name — $repeats повторов"
    for ((i = 1; i <= repeats; i++)); do android_feature_repeat "$name" "$i"; done
  done
  adb_ shell am force-stop "$(android_app_id)"
}

# --- Android: стабильность --------------------------------------------------------------

android_net() { adb_ shell svc wifi "$1" >/dev/null 2>&1; adb_ shell svc data "$1" >/dev/null 2>&1; }

android_stability() {
  local d="$RUN_DIR/raw/android/stability" pkg seed events throttle mark t0 t1
  pkg="$(android_app_id)"
  pick_android_device
  [[ -f "$RUN_DIR/raw/android/env.txt" ]] || android_env
  mkdir -p "$d"
  seed="$(json_get "$SCENARIOS" stability.monkey.seed)"; events="$(json_get "$SCENARIOS" stability.monkey.events)"; throttle="$(json_get "$SCENARIOS" stability.monkey.throttle_ms)"
  log "Android: monkey seed $seed, $events событий"
  adb_ shell am force-stop "$pkg"; sleep 1
  adb_ shell am start -W -n "$pkg/$ACTIVITY" >/dev/null 2>&1; android_wait_content
  mark="$(adb_ shell date +'%m-%d\ %H:%M:%S.000' | tr -d '\r')"
  t0=$(now_ms)
  adb_ shell monkey -p "$pkg" -s "$seed" --throttle "$throttle" --pct-syskeys 0 --ignore-security-exceptions -v "$events" >"$d/monkey.txt" 2>&1
  t1=$(now_ms)
  adb_ logcat -d -b crash -T "$mark" 2>/dev/null | tr -d '\r' | grep -v '^-----' >"$d/monkey-crash.txt"
  { echo "seed=$seed"; echo "events=$events"; echo "throttle_ms=$throttle"; echo "duration_ms=$((t1 - t0))"; echo "mark=$mark"; } >"$d/monkey-meta.txt"
  echo "  monkey: $((t1 - t0)) ms, crash $(grep -c '// CRASH' "$d/monkey.txt"), anr $(grep -c '// NOT RESPONDING' "$d/monkey.txt")"
  adb_ shell am force-stop "$pkg"

  log "Android: старт без сети"
  local tags timeout recover_timeout content_tag
  tags="$(python3 -c 'import json,sys; print("|".join(json.load(open(sys.argv[1]))["stability"]["offline_tags"]))' "$SCENARIOS")"
  timeout="$(json_get "$SCENARIOS" stability.offline_timeout_s)"; recover_timeout="$(json_get "$SCENARIOS" stability.recover_timeout_s)"
  content_tag="$(json_get "$SCENARIOS" startup.content_tag)"
  trap 'android_net enable' EXIT
  android_net disable; sleep 2
  mark="$(adb_ shell date +'%m-%d\ %H:%M:%S.000' | tr -d '\r')"
  python3 "$PROBE" android-offline "$ANDROID_DEVICE" "$pkg" "$ACTIVITY" "$tags" "$timeout" >"$d/offline.txt"
  adb_ logcat -d -b crash -T "$mark" 2>/dev/null | tr -d '\r' | grep -v '^-----' >"$d/offline-crash.txt"
  android_net enable
  trap - EXIT
  python3 "$PROBE" android-wait "$ANDROID_DEVICE" "$content_tag" "$recover_timeout" >>"$d/offline.txt"
  echo "  offline: $(tr '\n' ' ' <"$d/offline.txt")"
  adb_ shell am force-stop "$pkg"
}

# --- iOS: сценарии фич ------------------------------------------------------------------

ios_feature_repeat() {
  local name="$1" i="$2" d="$RUN_DIR/raw/ios/feature-$1/repeat-$2" app flow pid pid_after mark t0 t1 rc
  app="$(ios_app_id)"; flow="$SKILL_DIR/$(feature_field "$name" flow)"
  mkdir -p "$d"
  xcrun simctl spawn "$IOS_DEVICE" defaults write com.apple.Accessibility ApplicationAccessibilityEnabled -bool true
  if [[ "$(feature_field "$name" reset_each False)" == True ]]; then
    xcrun simctl terminate "$IOS_DEVICE" "$app" 2>/dev/null; xcrun simctl uninstall "$IOS_DEVICE" "$app"; xcrun simctl install "$IOS_DEVICE" "$(ios_app_path)"
  elif ! pgrep -x "$IOS_PRODUCT" >/dev/null; then
    xcrun simctl launch "$IOS_DEVICE" "$app" >/dev/null; sleep 6
  fi
  pid="$(pgrep -x "$IOS_PRODUCT" | head -1)"
  mark="$(date +'%Y-%m-%d %H:%M:%S')"
  maestro_args "$app"
  t0=$(now_ms)
  maestro --device "$IOS_DEVICE" test "${MAESTRO_ARGS[@]}" --test-output-dir "$d/maestro" --debug-output "$d/maestro-debug" --flatten-debug-output "$flow" >"$d/maestro.log" 2>&1
  rc=$?
  t1=$(now_ms)
  [[ $rc -eq 0 ]] && rm -rf "$d/maestro-debug"
  xcrun simctl spawn "$IOS_DEVICE" defaults write com.apple.Accessibility ApplicationAccessibilityEnabled -bool true
  pid_after="$(pgrep -x "$IOS_PRODUCT" | head -1)"
  {
    echo "flow_ms=$((t1 - t0))"; echo "flow_rc=$rc"; echo "mark=$mark"; echo "pid=${pid:-}"; echo "pid_after=${pid_after:-}"
    [[ -n "$pid_after" ]] && { echo "rss_kb=$(ps -o rss= -p "$pid_after" | tr -d ' ')"; echo "cputime_after=$(ps -o cputime= -p "$pid_after" | tr -d ' ')"; }
    [[ -n "$pid" && "$pid" == "$pid_after" ]] && echo "cputime_before=$CPU_BEFORE"
  } >"$d/sample.txt"
  xcrun simctl spawn "$IOS_DEVICE" log show --start "$mark" --style compact \
    --predicate "process == \"$IOS_PRODUCT\" AND senderImagePath CONTAINS \"$IOS_PRODUCT.app\" AND (messageType == error OR messageType == fault)" \
    2>/dev/null | grep -v '^Timestamp ' >"$d/log-errors.txt"
  find ~/Library/Logs/DiagnosticReports -name "$IOS_PRODUCT*.ips" -newermt "$mark" 2>/dev/null >"$d/crashes.txt"
  echo "  $name #$i: $((t1 - t0)) ms, rc=$rc"
}

ios_features() {
  local names name repeats i
  names="$(feature_names)"
  [[ -n "$names" ]] || return 0
  pick_ios_device
  [[ -f "$RUN_DIR/raw/ios/env.txt" ]] || ios_env
  has_scope startup || xcrun simctl install "$IOS_DEVICE" "$(ios_app_path)" || die "simctl install упал"
  for name in $names; do
    # Флоу с back — только Android (у Maestro на iOS нет системного «назад»): scenarios.json → platforms.
    [[ "$(feature_field "$name" platforms)" == *ios* ]] || { log "iOS: сценарий $name — только Android, пропущен"; continue; }
    repeats="$(feature_field "$name" repeats 3)"
    mkdir -p "$RUN_DIR/raw/ios/feature-$name"
    { echo "name=$name"; echo "title=$(feature_field "$name" title)"; echo "repeats=$repeats"; echo "flow=$(feature_field "$name" flow)"; echo "reset_each=$(feature_field "$name" reset_each False)"; } >"$RUN_DIR/raw/ios/feature-$name/scenario.txt"
    log "iOS: сценарий $name — $repeats повторов"
    for ((i = 1; i <= repeats; i++)); do
      CPU_BEFORE="$(pgrep -x "$IOS_PRODUCT" >/dev/null && ps -o cputime= -p "$(pgrep -x "$IOS_PRODUCT" | head -1)" | tr -d ' ')"
      ios_feature_repeat "$name" "$i"
    done
  done
  xcrun simctl terminate "$IOS_DEVICE" "$(ios_app_id)" 2>/dev/null
}

ios_build() {
  pick_ios_device
  log "iOS: сборка $(ios_scheme) / $(ios_config) (xcodebuild, долго)"
  xcodebuild -project "$ROOT/iosApp/iosApp.xcodeproj" -scheme "$(ios_scheme)" -configuration "$(ios_config)" \
    -sdk iphonesimulator -destination "id=$IOS_DEVICE" ARCHS=arm64 -derivedDataPath "$(ios_derived)" build -quiet \
    >"$RUN_DIR/raw/build-ios.log" 2>&1 \
    || { grep -E "^e: |: error: |BUILD FAILED" "$RUN_DIR/raw/build-ios.log" | sort -u | head -40; die "сборка iOS упала, лог: $RUN_DIR/raw/build-ios.log"; }
}

ios_env() {
  local d="$RUN_DIR/raw/ios"; mkdir -p "$d"
  {
    echo "device=$IOS_DEVICE"
    echo "name=$(xcrun simctl list devices | grep "$IOS_DEVICE" | sed -E 's/^ *([^(]*[^ (]) *\(.*/\1/' | head -1)"
    echo "runtime=$(xcrun simctl list devices -j | python3 -c 'import json,sys
u=sys.argv[1]; d=json.load(sys.stdin)["devices"]
print(next((k.rsplit(".",1)[-1] for k,v in d.items() for x in v if x["udid"]==u),""))' "$IOS_DEVICE")"
    echo "app_id=$(ios_app_id)"
    echo "app_path=$(ios_app_path)"
  } >"$d/env.txt"
  du -sk "$(ios_app_path)" 2>/dev/null | awk '{print $1 * 1024}' >"$d/app-size.txt"
}

ios_startup_sample() {
  local i="$1" d="$RUN_DIR/raw/ios/sample-$i" app mark pid tag timeout
  app="$(ios_app_id)"
  tag="$(json_get "$SCENARIOS" startup.content_tag)"
  timeout="$(json_get "$SCENARIOS" startup.content_timeout_s)"
  mkdir -p "$d"
  xcrun simctl terminate "$IOS_DEVICE" "$app" 2>/dev/null
  sleep 2
  # Без флага Compose на iOS не строит дерево доступности — axe видит пустой экран.
  xcrun simctl spawn "$IOS_DEVICE" defaults write com.apple.Accessibility ApplicationAccessibilityEnabled -bool true
  mark="$(date +'%Y-%m-%d %H:%M:%S')"
  python3 "$PROBE" ios "$IOS_DEVICE" "$app" "$tag" "$timeout" >"$d/sample.txt"
  sleep "$SETTLE_S"
  pid="$(pgrep -x "$IOS_PRODUCT" | head -1)"
  {
    echo "settle_s=$SETTLE_S"
    echo "mark=$mark"
    echo "pid=${pid:-}"
    if [[ -n "$pid" ]]; then
      echo "rss_kb=$(ps -o rss= -p "$pid" | tr -d ' ')"
      echo "cputime=$(ps -o cputime= -p "$pid" | tr -d ' ')"
    fi
  } >>"$d/sample.txt"
  xcrun simctl spawn "$IOS_DEVICE" log show --start "$mark" --style compact \
    --predicate "process == \"$IOS_PRODUCT\" AND senderImagePath CONTAINS \"$IOS_PRODUCT.app\" AND (messageType == error OR messageType == fault)" \
    2>/dev/null | grep -v '^Timestamp ' >"$d/log-errors.txt"
  find ~/Library/Logs/DiagnosticReports -name "$IOS_PRODUCT*.ips" -newermt "$mark" 2>/dev/null >"$d/crashes.txt"
  echo "  #$i $(grep -E '^(ttfc_ms|rss_kb)=' "$d/sample.txt" | tr '\n' ' ')"
}

ios_startup() {
  local n="$RUNS_N" warm i
  warm="$(json_get "$SCENARIOS" startup.warmup)"
  pick_ios_device
  [[ -d "$(ios_app_path)" ]] || die "нет сборки iOS: $(ios_app_path) — убери --no-build"
  ios_env
  log "iOS: установка на $IOS_DEVICE"
  xcrun simctl install "$IOS_DEVICE" "$(ios_app_path)" || die "simctl install упал"
  log "iOS: старт — прогрев $warm, замеров $n, settle ${SETTLE_S}s"
  for ((i = 1; i <= warm; i++)); do ios_startup_sample "w$i"; done
  for ((i = 1; i <= n; i++)); do ios_startup_sample "$i"; done
  xcrun simctl terminate "$IOS_DEVICE" "$(ios_app_id)" 2>/dev/null
}

# База для сравнения: --baseline <путь|sha>, иначе baselines/<merge-base>.json, иначе самая свежая база.
resolve_baseline() {
  local ref="$BASELINE_REF" mb
  if [[ -n "$ref" ]]; then
    [[ -f "$ref" ]] && { echo "$ref"; return; }
    [[ -f "$BASES_DIR/$ref.json" ]] && { echo "$BASES_DIR/$ref.json"; return; }
    ls "$BASES_DIR"/"$ref"*.json 2>/dev/null | head -1
    return
  fi
  mb="$(git -C "$ROOT" merge-base HEAD develop 2>/dev/null || true)"
  [[ -n "$mb" && -f "$BASES_DIR/$mb.json" ]] && { echo "$BASES_DIR/$mb.json"; return; }
  ls -t "$BASES_DIR"/*.json 2>/dev/null | head -1
}

do_measure() {
  local kind="$1"
  new_run_dir "$kind"
  write_meta "$kind"
  log "прогон: $RUN_DIR"
  mkdir -p "$RUN_DIR/raw/static"
  if has_platform android; then
    ((BUILD)) && android_build
    has_scope static && static_collect
    has_scope startup && android_startup
    has_scope trace && android_trace
    [[ "$SCOPE" == *feature:* ]] && android_features
    has_scope stability && android_stability
  elif has_scope static; then
    [[ -n "$(apk_path)" ]] && static_collect || log "static: нет APK и Android не выбран — статика пропущена"
  fi
  if has_platform ios; then
    ((BUILD)) && ios_build
    has_scope startup && ios_startup
    [[ "$SCOPE" == *feature:* ]] && ios_features
  fi
  log "сбор результатов (collect.py)"
  python3 "$COLLECT" "$RUN_DIR" --root "$ROOT" --registry "$REGISTRY" || die "collect.py упал"
}

cmd_baseline() {
  parse_opts "$@"
  mkdir -p "$RUNS_DIR" "$BASES_DIR"
  do_measure baseline
  local sha; sha="$(git -C "$ROOT" rev-parse HEAD)"
  cp "$RUN_DIR/results.json" "$BASES_DIR/$sha.json"
  python3 "$REPORT" "$RUN_DIR" --registry "$REGISTRY" --history "$HISTORY" || die "report.py упал"
  [[ "$(grep '^dirty=' "$RUN_DIR/meta.txt")" == "dirty=1" ]] && echo "⚠️  рабочее дерево грязное: база снята не с чистого коммита $sha"
  echo "BASELINE: $BASES_DIR/$sha.json"
  echo "REPORT: $RUN_DIR/report.md"
}

cmd_run() {
  parse_opts "$@"
  mkdir -p "$RUNS_DIR" "$BASES_DIR"
  local base; base="$(resolve_baseline)"
  do_measure run
  if [[ -n "$base" ]]; then
    python3 "$REPORT" "$RUN_DIR" --baseline "$base" --registry "$REGISTRY" --history "$HISTORY" || die "report.py упал"
  else
    echo "⚠️  базы нет (bench.sh baseline) — отчёт без сравнения"
    python3 "$REPORT" "$RUN_DIR" --registry "$REGISTRY" --history "$HISTORY" || die "report.py упал"
  fi
  echo "REPORT: $RUN_DIR/report.md"
  echo "HTML: $RUN_DIR/report.html"
}

cmd_compare() {
  local a="${1:?compare <база: папка прогона|results.json> <прогон: папка>}" b="${2:?compare <база> <прогон>}"
  [[ -d "$a" ]] && a="$a/results.json"
  [[ -f "$a" ]] || die "нет $a"
  [[ -f "$b/results.json" ]] || die "нет $b/results.json"
  python3 "$REPORT" "$b" --baseline "$a" --registry "$REGISTRY" --history "$HISTORY" --no-history || die "report.py упал"
  echo "REPORT: $b/report.md"
}

# Без --baseline база ищется как при run: по merge-base прогона, иначе самая свежая.
cmd_report() {
  local d="${1:?report <папка прогона> [--baseline <путь|sha>]}"; shift
  [[ -f "$d/results.json" ]] || die "нет $d/results.json"
  local base=""
  [[ "${1:-}" == --baseline ]] && { BASELINE_REF="$2"; shift 2; }
  if [[ -z "$BASELINE_REF" ]]; then
    local mb; mb="$(sed -n 's/^merge_base=//p' "$d/meta.txt")"
    [[ -n "$mb" && -f "$BASES_DIR/$mb.json" ]] && base="$BASES_DIR/$mb.json" || base="$(ls -t "$BASES_DIR"/*.json 2>/dev/null | head -1)"
  else
    base="$(resolve_baseline)"
  fi
  if [[ -n "$base" && "$(grep '^kind=' "$d/meta.txt")" != "kind=baseline" ]]; then
    python3 "$REPORT" "$d" --baseline "$base" --registry "$REGISTRY" --history "$HISTORY" --no-history || die "report.py упал"
  else
    python3 "$REPORT" "$d" --registry "$REGISTRY" --history "$HISTORY" --no-history || die "report.py упал"
  fi
  echo "REPORT: $d/report.md"
}

cmd_list() {
  echo "Базы ($BASES_DIR):"
  ls -t "$BASES_DIR"/*.json 2>/dev/null | while read -r f; do
    echo "  $(basename "$f" .json | cut -c1-12)  $(python3 -c 'import json,sys; m=json.load(open(sys.argv[1]))["meta"]; print(m["created"], m["branch"], m.get("label",""))' "$f")"
  done
  echo "Прогоны ($RUNS_DIR):"
  ls -t "$RUNS_DIR" 2>/dev/null | head -20 | sed 's/^/  /'
}

cmd_metrics() { python3 "$REPORT" --registry "$REGISTRY" --registry-md; }

cmd_help() {
  cat <<'EOF'
bench.sh — замеры производительности SpeCMP (без модели). Все пути — от корня репозитория.

  doctor   [--platform android|ios|both]        проверка окружения
  baseline [опции]                              замер «до» → baselines/<sha>.json + отчёт
  run      [опции] [--baseline <путь|sha>]      замер «после» → сравнение с базой, report.html + report.md
  compare  <база> <прогон>                      пересобрать отчёт прогона относительно другой базы
  report   <прогон> [--baseline <путь>]         пересобрать отчёт
  list                                          базы и прогоны
  metrics                                       реестр метрик таблицей (Markdown)

Опции baseline / run:
  --platform android|ios|both   по умолчанию android
  --scope <список>              по умолчанию static,startup,trace (trace — Perfetto-трейс одного старта,
                                нужен venv с пакетом perfetto, см. doctor); compose — сборка с
                                init-скриптом отчётов Compose-компилятора и метрики стабильности; ещё: feature:<name> | feature:all |
                                feature:auto (по изменённым модулям, карта — scenarios.json), stability
                                (monkey с фиксированным seed + старт без сети). Пример:
                                --scope static,startup,feature:auto,stability
  --runs N                      число замеров старта (scenarios.json → startup.runs)
  --settle S                    пауза после контента до дампов (scenarios.json → startup.settle_s)
  --label <текст>               метка прогона в имени папки и отчёте
  --no-build                    не собирать (меряется установленная ранее сборка)
  --android-device <serial>, --ios-device <udid>
  --apk <путь>                  мерить этот APK (например, release-сборку того же applicationId); подразумевает --no-build

Вывод: .ai-perf/bench/{runs/<дата>-<sha7>-<kind>[-label]/, baselines/, history.jsonl}
EOF
}

case "${1:-help}" in
  doctor) shift; cmd_doctor "$@" ;;
  baseline) shift; cmd_baseline "$@" ;;
  run) shift; cmd_run "$@" ;;
  compare) shift; cmd_compare "$@" ;;
  report) shift; cmd_report "$@" ;;
  list) cmd_list ;;
  metrics) cmd_metrics ;;
  help | -h | --help) cmd_help ;;
  *) die "неизвестная команда $1 (bench.sh help)" ;;
esac
