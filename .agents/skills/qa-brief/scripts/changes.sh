#!/bin/bash
# Изменения для брифа QA, разложенные по разделам по строкам «- Пути:» из ../sections.md.
#
#   changes.sh full
#   changes.sh section <раздел> [<раздел>…]
#   changes.sh branch <ветка> [база=develop]
#   changes.sh since <ГГГГ-ММ-ДД> [до ГГГГ-ММ-ДД] [ветка=develop]
#
# Печатает шапку (ветка, коммит, версии сборок, период, число коммитов) и по каждому разделу — коммиты
# («дата hash тема») и изменённые файлы. В режимах full / section — шапку, дату последнего изменения раздела
# и (section) последние коммиты раздела — для «Что нового».

set -euo pipefail
here="$(cd "$(dirname "$0")/.." && pwd)"
sections_md="$here/sections.md"
cd "$(git rev-parse --show-toplevel)"
[ -f "$sections_md" ] || { echo "Нет $sections_md" >&2; exit 1; }

# Раскладка из sections.md: строки «раздел<TAB>путь» в порядке файла (раздел — из «## <id> — …»,
# пути — из «- Пути: `a`, `b`»). Единственный источник — sections.md, здесь таблицы нет.
SECTION_PATHS=$(awk '
    /^## / { sec = $2; next }
    /^- Пути:/ && sec != "" {
        line = $0
        while (match(line, /`[^`]+`/)) {
            print sec "\t" substr(line, RSTART + 1, RLENGTH - 2)
            line = substr(line, RSTART + RLENGTH)
        }
    }' "$sections_md")
[ -n "$SECTION_PATHS" ] || { echo "В $sections_md не найдено ни одной строки «- Пути:»" >&2; exit 1; }
SECTIONS="$(printf '%s\n' "$SECTION_PATHS" | cut -f1 | awk '!seen[$0]++' | tr '\n' ' ')other"

# Раздел по пути файла — первое совпадение сверху вниз. Путь раздела — сам файл, его папка или маска с `*`.
# Тот же путь у нескольких разделов (autostart — bypass и clone) — печатаются все, через пробел.
# Документация и конфиги агентов — всегда «Прочее» (в бриф не попадают).
section_of() {
    local f="$1" sec pat
    case "$f" in
        *.md | design/* | docs/* | .ai/* | .claude/* | .agents/* | .codex/* | .gemini/*) echo other; return ;;
    esac
    while IFS=$'\t' read -r sec pat; do
        # shellcheck disable=SC2053 # $pat — маска намеренно
        if [[ "$f" == $pat || "$f" == "$pat"/* ]]; then
            printf '%s\n' "$SECTION_PATHS" | awk -F'\t' -v p="$pat" '$2 == p { print $1 }' | tr '\n' ' '
            echo
            return
        fi
    done <<< "$SECTION_PATHS"
    echo other
}

# Пути раздела через пробел (для git log -- …).
paths_of() {
    printf '%s\n' "$SECTION_PATHS" | awk -F'\t' -v s="$1" '$1 == s { print $2 }' | tr '\n' ' '
}

usage() {
    sed -n '4,8p' "$0" | sed 's/^# \{0,1\}//'
    exit 2
}

header() {
    local android ios ios_build desktop
    # Android и desktop — одна версия из каталога (gradle/libs.versions.toml, app-versionName).
    android=$(sed -n 's/^app-versionName = "\(.*\)".*/\1/p' gradle/libs.versions.toml | head -1)
    ios=$(sed -n 's/^MARKETING_VERSION = //p' iosApp/Configuration/Config.xcconfig | head -1)
    ios_build=$(sed -n 's/^CURRENT_PROJECT_VERSION = //p' iosApp/Configuration/Config.xcconfig | head -1)
    desktop=$android
    echo "Ветка: $(git rev-parse --abbrev-ref HEAD)   коммит: $(git rev-parse --short HEAD)   сегодня: $(date +%F)"
    echo "Версии: Android $android · iOS $ios ($ios_build) · Desktop $desktop"
}

# Коммиты диапазона ($@ — аргументы git log) с файлами → по разделам.
by_sections() {
    local tmp
    tmp=$(mktemp -d)
    trap 'rm -rf "$tmp"' RETURN
    local count first last
    count=$(git log --no-merges --format=%h "$@" | wc -l | tr -d ' ')
    if [ "$count" = "0" ]; then
        echo "Коммитов нет."
        return 3
    fi
    # Без `| head`: с pipefail оборванный git log завершил бы скрипт.
    first=$(git log --no-merges --format=%ad --date=short "$@" | tail -1)
    last=$(git log --no-merges --format=%ad --date=short "$@" | sed -n 1p)
    echo "Коммитов: $count   период: $first … $last"

    local hash="" line sec
    while IFS= read -r line; do
        if [[ "$line" == @@* ]]; then
            hash="${line#@@}"
            continue
        fi
        [ -z "$line" ] && continue
        for sec in $(section_of "$line"); do
            echo "$hash" >> "$tmp/$sec.commits"
            echo "$line" >> "$tmp/$sec.files"
        done
    done < <(git log --no-merges --format='@@%h' --name-only "$@")

    for sec in $SECTIONS; do
        [ -f "$tmp/$sec.commits" ] || continue
        echo
        echo "## $sec"
        sort -u "$tmp/$sec.commits" | while read -r h; do
            git log -1 --format='  %ad %h %s' --date=short "$h"
        done | sort
        echo "  файлы:"
        sort -u "$tmp/$sec.files" | sed 's/^/    /'
    done
}

mode="${1:-}"
case "$mode" in
    full)
        header
        echo
        echo "Последние изменения по разделам:"
        for sec in $SECTIONS; do
            [ "$sec" = other ] && continue
            read -r -a paths <<< "$(paths_of "$sec")"
            [ "${#paths[@]}" -eq 0 ] && continue
            echo "  $sec: $(git log -1 --format='%ad %h %s' --date=short -- "${paths[@]}")"
        done
        ;;
    section)
        shift
        [ "$#" -eq 0 ] && usage
        header
        for sec in "$@"; do
            read -r -a paths <<< "$(paths_of "$sec")"
            [ "${#paths[@]}" -eq 0 ] && { echo "Нет раздела $sec в $sections_md (есть: $SECTIONS)" >&2; exit 1; }
            echo
            echo "## $sec — последние коммиты (до 15)"
            git log --no-merges -15 --format='  %ad %h %s' --date=short -- "${paths[@]}"
        done
        ;;
    branch)
        branch="${2:-}"
        base="${3:-develop}"
        [ -z "$branch" ] && usage
        git rev-parse --verify -q "$branch" >/dev/null || { echo "Нет ветки $branch" >&2; exit 1; }
        git rev-parse --verify -q "$base" >/dev/null || { echo "Нет базы $base" >&2; exit 1; }
        header
        echo "Охват: $base...$branch"
        by_sections "$base..$branch"
        ;;
    since)
        from="${2:-}"
        [ -z "$from" ] && usage
        to="${3:-$(date +%F)}"
        ref="${4:-develop}"
        git rev-parse --verify -q "$ref" >/dev/null || { echo "Нет ветки $ref" >&2; exit 1; }
        header
        echo "Охват: $ref с $from по $to"
        by_sections "$ref" --since="$from 00:00" --until="$to 23:59"
        ;;
    *) usage ;;
esac
