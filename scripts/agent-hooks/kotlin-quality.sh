#!/usr/bin/env bash
# Проверка правленых .kt/.kts через Spotless ideHook (ktlint + compose-rules, ровно эти файлы, без git) и UI-аудит
# (scripts/ui-audit, только feature/composeApp/flasherApp).
# Остальные файлы пропускаются; detekt не запускается (медленно) — он на pre-push.
#   Вручную (любой агент или человек): scripts/agent-hooks/kotlin-quality.sh <файл>...  → отчёт, exit 1
#   Хук агента: JSON в stdin — Claude/Gemini (tool_input.file_path), Codex (apply_patch: tool_input.command)
#   --format claude|gemini|codex|plain — протокол ответа; по умолчанию plain для аргументов, иначе по payload.
# Регистрация хуков — .ai/hooks/* → .ai/generate.sh; подробнее — docs/code-quality.md.
set -u
root=$(cd "$(dirname "$0")/../.." && pwd)

usage() {
    sed -n '2,7p' "$0" | sed 's/^# \{0,1\}//'
}

format=""
files=()
while [ $# -gt 0 ]; do
    case "$1" in
        --format) format=${2:-}; shift 2 || shift ;;
        --format=*) format=${1#--format=}; shift ;;
        -h | --help) usage; exit 0 ;;
        --) shift; files+=("$@"); break ;;
        -*) usage >&2; exit 64 ;;
        *) files+=("$1"); shift ;;
    esac
done
case "$format" in
    "" | claude | gemini | codex | plain) ;;
    *) usage >&2; exit 64 ;;
esac

has_python=false
command -v python3 >/dev/null 2>&1 && has_python=true

paths=()
if [ ${#files[@]} -gt 0 ]; then
    [ -n "$format" ] || format=plain
    for f in "${files[@]}"; do
        case "$f" in
            /*) paths+=("$f") ;;
            *) paths+=("$PWD/$f") ;;
        esac
    done
elif [ -t 0 ]; then
    usage >&2
    exit 64
else
    input=$(cat)
    if $has_python; then
        parsed=$(printf '%s' "$input" | python3 -I -c '
import json, os, re, sys
try:
    d = json.loads(sys.stdin.read())
except ValueError:
    d = {}
if not isinstance(d, dict):
    d = {}
ti = d.get("tool_input") or {}
fmt = sys.argv[1]
if not fmt:
    if d.get("hook_event_name") == "AfterTool":
        fmt = "gemini"
    elif d.get("tool_name") == "apply_patch" or "turn_id" in d:
        fmt = "codex"
    else:
        fmt = "claude"
cwd = d.get("cwd") or os.getcwd()
found = []
texts = [ti] if isinstance(ti, str) else []
if isinstance(ti, dict):
    for k in ("file_path", "absolute_path"):
        if isinstance(ti.get(k), str) and ti[k]:
            found.append(ti[k])
    for k in ("command", "patch", "input"):
        v = ti.get(k)
        texts.append("\n".join(map(str, v)) if isinstance(v, list) else v)
for t in texts:
    if isinstance(t, str) and "*** Begin Patch" in t:
        found += re.findall(r"^\*\*\* (?:Update File|Add File|Move to): (.+?)\s*$", t, re.M)
print(fmt)
for p in dict.fromkeys(os.path.join(cwd, p) for p in found):
    print(p)
' "$format")
        format=$(printf '%s\n' "$parsed" | head -1)
        while IFS= read -r p; do
            [ -n "$p" ] && paths+=("$p")
        done < <(printf '%s\n' "$parsed" | tail -n +2)
    else
        [ -n "$format" ] || format=claude
        p=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
        [ -n "$p" ] && paths+=("$p")
    fi
fi

cd "$root" || exit 0
rels=()
for f in ${paths[@]+"${paths[@]}"}; do
    case "$f" in
        *.kt | *.kts) ;;
        *) continue ;;
    esac
    [ -f "$f" ] || continue
    case "$f" in
        "$root"/*) rels+=("${f#"$root"/}") ;;
    esac
done
if [ ${#rels[@]} -eq 0 ]; then
    [ "$format" = plain ] && echo "kotlin-quality: нет .kt/.kts внутри проекта — проверять нечего."
    exit 0
fi

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
report="$tmp/report"
: > "$report"

check_file() {
    local rel=$1 section="$tmp/section" code
    : > "$section"
    # ideHook проверяет ровно этот файл, не зависит от git и чужих правок; с UseStdOut файл не переписывается.
    # Неавтофиксимое нарушение — exit 1 и "found problem" (только первое); только формат — "IS DIRTY" и
    # отформатированный текст в stdout.
    ./gradlew -q --console=plain spotlessApply -PspotlessIdeHook="$root/$rel" -PspotlessIdeHookUseStdOut \
        > "$tmp/out" 2> "$tmp/err"
    code=$?
    if grep -q "found problem in" "$tmp/err"; then
        {
            echo "Spotless (ktlint + compose-rules), $rel — показано первое нарушение, после исправления всплывёт следующее:"
            sed -n '/found problem in/,/^com\.diffplug\./p' "$tmp/err" | grep -v '^com\.diffplug\.'
            echo
        } >> "$section"
    elif grep -q "IS DIRTY" "$tmp/err"; then
        perl -pe 's/\e\][^\a]*\a//g' "$tmp/out" > "$tmp/formatted"
        {
            echo "Spotless, $rel — форматирование расходится (что должно быть):"
            diff -u "$rel" "$tmp/formatted" | tail -n +3
            echo "Автофикс только этого файла: ./gradlew spotlessApply -PspotlessIdeHook=$root/$rel"
            echo
        } >> "$section"
    elif [ "$code" -ne 0 ]; then
        {
            echo "Spotless не запустился ($rel) — вероятно, сломана конфигурация сборки:"
            grep -v '^[[:space:]]*at \|^[[:space:]]*\.\.\. ' "$tmp/err" | tail -20
            echo
        } >> "$section"
    fi

    case "$rel" in
        feature/*.kt | composeApp/*.kt | flasherApp/*.kt)
            if $has_python && ! python3 -I scripts/ui-audit/ui_audit.py --file "$rel" > "$tmp/ui" 2>&1; then
                {
                    echo "UI-аудит, $rel — обход дизайн-системы сверх baseline:"
                    cat "$tmp/ui"
                    echo "Чини через core/ui (docs/ui-kit.md); осознанное исключение —"
                    echo "  // ui-audit: ignore <rule> — причина   (на той же или предыдущей строке)"
                    echo
                } >> "$section"
            fi
            ;;
    esac

    if [ -s "$section" ]; then
        head -60 "$section" >> "$report"
        [ "$(wc -l < "$section")" -gt 60 ] && echo "… (обрезано)" >> "$report"
    fi
}

for rel in "${rels[@]}"; do
    check_file "$rel"
done

if [ ! -s "$report" ]; then
    [ "$format" = plain ] && echo "kotlin-quality: OK (${#rels[@]} файл(ов))."
    exit 0
fi
echo "Правила и подавления — docs/code-quality.md." >> "$report"

case "$format" in
    plain)
        cat "$report"
        exit 1
        ;;
    gemini | codex)
        if $has_python; then
            python3 -I -c '
import json, sys
fmt, text = sys.argv[1], open(sys.argv[2], encoding="utf-8").read()
if fmt == "gemini":
    out = {"hookSpecificOutput": {"hookEventName": "AfterTool", "additionalContext": text},
           "systemMessage": "kotlin-quality: есть замечания по Kotlin-файлам"}
else:
    out = {"decision": "block",
           "reason": "Правка применена, но проверка качества нашла замечания — исправь их:\n\n" + text}
print(json.dumps(out, ensure_ascii=False))
' "$format" "$report"
            exit 0
        fi
        cat "$report" >&2
        exit 2
        ;;
    *)
        cat "$report" >&2
        exit 2
        ;;
esac
