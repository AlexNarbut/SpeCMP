#!/usr/bin/env bash
# PostToolUse-хук: после правки Kotlin-файла прогоняет Spotless и возвращает агенту нарушения
# именно этого файла (чужие изменённые файлы в дереве не мешают). Подробнее — docs/code-quality.md.
set -u
cd "${CLAUDE_PROJECT_DIR:-$(pwd)}" || exit 0

file=$(python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input", {}).get("file_path", ""))')
case "$file" in
    *.kt | *.kts) ;;
    *) exit 0 ;;
esac
rel=${file#"$PWD"/}

out=$(./gradlew -q spotlessCheck --continue 2>&1)

report=$(python3 - "$rel" "$out" <<'PY'
import sys

rel, out = sys.argv[1], sys.argv[2].split("\n")
block, taking = [], False
for line in out:
    stripped = line.strip()
    starts_file = stripped.endswith((".kt", ".kts")) and " " not in stripped
    if rel in line and (starts_file or "ktlint(" in line):
        taking = True
    elif taking and (starts_file or stripped.startswith(("Run './gradlew", "* Try", "Resolve these"))):
        taking = False
    if taking:
        block.append(line)
print("\n".join(block[:60]))
PY
)

if [ -n "$report" ]; then
    {
        echo "Spotless (ktlint + compose-rules): нарушения в $rel"
        echo "$report"
        echo
        echo "Исправь по diff выше. Если в дереве нет чужих изменений, можно ./gradlew spotlessApply."
        echo "Правила и подавления — docs/code-quality.md."
    } >&2
    exit 2
fi
exit 0
