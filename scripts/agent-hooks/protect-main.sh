#!/usr/bin/env bash
# PreToolUse(Bash): агентам нельзя вливать в main/master и пушить в них — интеграционная ветка develop.
# Правило: .ai/rules.md. Сообщение уходит агенту (exit 2).
input=$(cat)
command=$(printf '%s' "$input" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("tool_input",{}).get("command",""))' 2>/dev/null)
[ -z "$command" ] && exit 0

protected='(^|[[:space:]:/])(main|master)([[:space:]]|$)'
branch=$(git -C "${CLAUDE_PROJECT_DIR:-.}" rev-parse --abbrev-ref HEAD 2>/dev/null)
on_protected=0
[[ "$branch" == "main" || "$branch" == "master" ]] && on_protected=1

block() {
  echo "Заблокировано: в main/master не вливаем и не пушим (см. .ai/rules.md). Интеграция — в develop." >&2
  exit 2
}

# git push, где цель — main/master (или текущая ветка main/master без явной цели)
# Смотрим только аргументы самой команды push (до `&&`, `;`, `|`): текст коммита с «main» не считается.
if [[ "$command" =~ git[[:space:]]+(-C[[:space:]]+[^[:space:]]+[[:space:]]+)?push([^\&\;\|]*) ]]; then
  push_args="${BASH_REMATCH[2]}"
  [[ "$push_args" =~ $protected ]] && block
  (( on_protected )) && block
fi
# любые изменения истории (в т.ч. commit/tag), пока HEAD на main/master
if (( on_protected )) && [[ "$command" =~ git[[:space:]]+(-C[[:space:]]+[^[:space:]]+[[:space:]]+)?(merge|rebase|cherry-pick|reset|pull|commit|revert|tag) ]]; then
  block
fi
# переключение на main с последующим слиянием в одной команде
if [[ "$command" =~ git[[:space:]]+(checkout|switch)[[:space:]]+(-[^[:space:]]+[[:space:]]+)*(main|master)([[:space:]]|$) ]] \
   && [[ "$command" =~ git[[:space:]]+(merge|push|pull) ]]; then
  block
fi
exit 0
