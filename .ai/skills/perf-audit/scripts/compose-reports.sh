#!/bin/bash
# Отчёты Compose-компилятора для аудита производительности.
#
#   compose-reports.sh <module>...        модуль: `feature/vehicle-ui` или `:feature:vehicle-ui`
#
# Перекомпилирует jvm-таргет модуля с включёнными reports/metrics (init-скрипт рядом, build-файлы
# не трогает) и печатает выжимку: unstable/runtime-классы, composable с unstable-параметрами,
# restartable-но-не-skippable composable. Полные отчёты: <module>/build/compose-audit/.
# Модуль без плагина Compose — отчётов не будет (это не ошибка).

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(git -C "$SCRIPT_DIR" rev-parse --show-toplevel)"
INIT="$SCRIPT_DIR/compose-reports.init.gradle"

[ $# -gt 0 ] || { sed -n '2,9p' "$0" | sed 's/^# \{0,1\}//'; exit 1; }

modules=()
for m in "$@"; do
    m="${m%/}"
    m="${m#:}"
    modules+=("${m//://}")
done

tasks=()
for m in "${modules[@]}"; do
    [ -d "$ROOT/$m" ] || { echo "❌ нет модуля $m"; exit 1; }
    tasks+=(":${m//\//:}:compileKotlinJvm" --rerun)
done

# --rerun (опция задачи — ставится после каждой): иначе UP-TO-DATE и отчёты не пишутся;
# без configuration cache, иначе init-скрипт игнорируется.
(cd "$ROOT" && ./gradlew "${tasks[@]}" --no-configuration-cache --init-script "$INIT" -q) \
    || { echo "❌ компиляция упала — см. вывод выше"; exit 1; }

or_dash() { if [ -n "$1" ]; then echo "$1"; else echo "—"; fi; }

for m in "${modules[@]}"; do
    dir="$ROOT/$m/build/compose-audit"
    echo "## $m"
    classes=$(ls "$dir"/*-classes.txt 2>/dev/null | head -1)
    composables=$(ls "$dir"/*-composables.txt 2>/dev/null | head -1)
    if [ -z "$composables" ]; then
        echo "нет отчётов (модуль без Compose)"
        echo
        continue
    fi

    echo "### Классы: unstable / runtime"
    or_dash "$(grep -E '^(unstable|runtime) class' "$classes" | sed -E 's/ \{$//; s/class [a-z0-9_.]*\.([A-Z])/class \1/')"

    echo "### Composable с unstable-параметрами"
    or_dash "$(awk '
        /(^| )fun / {
            fn = $0
            sub(/\($/, "", fn); sub(/.* fun /, "", fn)
            n = split(fn, parts, "."); fn = parts[n]
            skip = ($0 !~ /^restartable/) ? "не restartable" : ($0 ~ /skippable/) ? "skippable" : "NOT skippable"
            next
        }
        /^  unstable / { p = $0; sub(/^  unstable /, "", p); print "- " fn " (" skip "): " p }
    ' "$composables" | sort -u)"
    echo "### Restartable, но не skippable"
    or_dash "$(grep -E '^restartable ' "$composables" | grep -v ' skippable ' \
        | sed -E 's/\($//; s/.* fun [a-z0-9_.]*\.([A-Za-z])/- \1/')"
    echo
done
