#!/usr/bin/env bash
# Каждый @Serializable-класс обязан иметь @SerialName("<модуль>.<Класс>") — см. docs/patterns.md, «Сериализация».
# Без аргументов проверяет все *.kt репозитория, иначе — переданные файлы.
set -euo pipefail
cd "$(dirname "$0")/../.."
check() {
    awk '
        FNR == 1 { pending = 0; prev = "" }
        pending && /^[[:space:]]*@/ { if ($0 ~ /@SerialName\(/) pending = 0; prev = $0; next }
        pending { printf "%s:%d: @Serializable без @SerialName\n", FILENAME, at; bad = 1; pending = 0 }
        /^[[:space:]]*@Serializable[[:space:]]*$/ && prev !~ /@SerialName\(/ { pending = 1; at = FNR }
        { prev = $0 }
        END { if (bad) { print "Правило: docs/patterns.md, раздел «Сериализация»" > "/dev/stderr"; exit 1 } }
    ' "$@"
}
if (($#)); then check "$@"; else git ls-files -z '*.kt' | xargs -0 bash -c "$(declare -f check); check \"\$@\"" _; fi
