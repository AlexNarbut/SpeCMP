#!/bin/bash
# Изменения для брифа QA, разложенные по разделам (.agents/skills/qa-brief/sections.md).
#
#   changes.sh full
#   changes.sh branch <ветка> [база=develop]
#   changes.sh since <ГГГГ-ММ-ДД> [до ГГГГ-ММ-ДД] [ветка=develop]
#
# Печатает шапку (ветка, коммит, версии сборок, период, число коммитов) и по каждому разделу — коммиты
# («дата hash тема») и изменённые файлы. В режиме full — только шапку и дату последнего изменения раздела.

set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

# Раздел по пути файла — первое совпадение сверху вниз (как в sections.md).
section_of() {
    case "$1" in
        feature/login/* | feature/auth-state/*) echo auth ;;
        feature/main/*) echo home ;;
        feature/advanced-mount/*) echo advanced-mount ;;
        feature/device/selection/*) echo scan ;;
        feature/device/interaction/* | feature/device/interaction-ui/*) echo connection ;;
        feature/device/firmware/*) echo firmware ;;
        feature/device/settings/* | feature/device/settings-ui/*) echo settings ;;
        feature/configurator/* | feature/configurator-ui/*) echo configurator ;;
        feature/vehicle/* | feature/vehicle-ui/*) echo vehicle ;;
        feature/support-chat/*) echo support-chat ;;
        */AGENTS.md | *.md | design/* | docs/* | .ai/* | .claude/* | .agents/* | .codex/* | .gemini/*) echo other ;;
        composeApp/* | core/* | androidApp/* | iosApp/* | desktopApp/* | gradle/* | settings.gradle.kts | build.gradle.kts) echo platform ;;
        *) echo other ;;
    esac
}

SECTIONS="auth home advanced-mount scan connection firmware settings configurator vehicle support-chat platform other"

usage() {
    sed -n '4,8p' "$0" | sed 's/^# \{0,1\}//'
    exit 2
}

header() {
    local android ios desktop
    android=$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' androidApp/build.gradle.kts | head -1)
    ios=$(sed -n 's/^MARKETING_VERSION = //p' iosApp/Configuration/Config.xcconfig | head -1)
    ios_build=$(sed -n 's/^CURRENT_PROJECT_VERSION = //p' iosApp/Configuration/Config.xcconfig | head -1)
    desktop=$(sed -n 's/.*packageVersion = "\(.*\)".*/\1/p' desktopApp/build.gradle.kts | head -1)
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
        sec=$(section_of "$line")
        echo "$hash" >> "$tmp/$sec.commits"
        echo "$line" >> "$tmp/$sec.files"
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
            paths=$(sed -n "/^## $sec /,/^## /p" .agents/skills/qa-brief/sections.md 2>/dev/null |
                sed -n 's/^- Пути: //p' | tr -d '`' | tr ',' ' ')
            [ -z "$paths" ] && continue
            # shellcheck disable=SC2086
            echo "  $sec: $(git log -1 --format='%ad %h %s' --date=short -- $paths)"
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
