#!/usr/bin/env bash
# В main/master вливается только develop (.ai/rules.md, «Ветки»). Вызывается pre-commit:
#   merge — стадия pre-merge-commit: при слиянии в main/master вливаемая ветка должна быть develop (её коммитом);
#   push  — стадия pre-push: каждый новый коммит main/master должен быть из develop, кроме merge-коммита «develop → master».
# Fast-forward-слияние коммит не создаёт и pre-merge-commit не вызывает — его ловит push.
set -uo pipefail
cd "$(dirname "$0")/../.."

ZERO=0000000000000000000000000000000000000000

# Коммит считается «из develop», если он достижим из локальной или удалённой develop.
from_develop() {
    local ref
    for ref in develop origin/develop; do
        git rev-parse -q --verify "$ref^{commit}" >/dev/null && git merge-base --is-ancestor "$1" "$ref" && return 0
    done
    return 1
}

fail() {
    echo "Заблокировано: в $1 можно вливать только develop (.ai/rules.md, «Ветки»)." >&2
    shift
    printf '%s\n' "$@" >&2
    echo "Фичи и фиксы — сначала в develop, затем develop → $branch." >&2
    exit 1
}

case "${1:-}" in
merge)
    branch="$(git rev-parse --abbrev-ref HEAD)"
    [[ "$branch" == main || "$branch" == master ]] || exit 0
    git rev-parse -q --verify MERGE_HEAD >/dev/null || exit 0
    bad=()
    while read -r head; do
        [[ -n "$head" ]] && ! from_develop "$head" && bad+=("  не из develop: $(git log -1 --format='%h %s' "$head")")
    done <"$(git rev-parse --git-path MERGE_HEAD)"
    [[ ${#bad[@]} -eq 0 ]] || fail "$branch" "${bad[@]}"
    ;;
push)
    branch="${PRE_COMMIT_REMOTE_BRANCH#refs/heads/}"
    [[ "$branch" == main || "$branch" == master ]] || exit 0
    to="${PRE_COMMIT_TO_REF:-}" from="${PRE_COMMIT_FROM_REF:-}"
    [[ -z "$to" || "$to" == "$ZERO" ]] && exit 0   # удаление ветки — не наш случай
    if [[ -z "$from" || "$from" == "$ZERO" ]] || ! git cat-file -e "$from^{commit}" 2>/dev/null; then
        range=("$to" --not --remotes)   # новая ветка на удалённом: только ещё не запушенные коммиты
    else
        range=("$from..$to")
    fi
    bad=()
    while read -r c; do
        from_develop "$c" && continue
        # merge-коммит «develop → master»: все вливаемые родители — из develop.
        parents=($(git rev-list --parents -n 1 "$c"))
        if [[ ${#parents[@]} -gt 2 ]]; then
            ok=1
            for p in "${parents[@]:2}"; do from_develop "$p" || ok=0; done
            ((ok)) && continue
        fi
        bad+=("  не из develop: $(git log -1 --format='%h %s' "$c")")
    done < <(git rev-list "${range[@]}")
    [[ ${#bad[@]} -eq 0 ]] || fail "$branch" "${bad[@]}"
    ;;
*)
    echo "usage: $0 merge|push" >&2
    exit 2
    ;;
esac
exit 0
