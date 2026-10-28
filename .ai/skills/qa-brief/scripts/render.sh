#!/bin/bash
# Собирает страницу брифа: <папка>/brief.json + templates/brief.html → <папка>/index.html.
#
#   render.sh .ai-qa/briefs/<slug>
#
# Проверяет JSON и обязательные поля (title, sections[].id/title, cases[].id/title/steps/expected),
# уникальность id кейсов. Ошибки — в stderr, код выхода 1.

set -euo pipefail
dir="${1:?usage: render.sh <папка брифа>}"
here="$(cd "$(dirname "$0")/.." && pwd)"

python3 -I - "$dir/brief.json" "$here/templates/brief.html" "$dir/index.html" <<'EOF'
import json, sys

src, template, out = sys.argv[1:4]
try:
    with open(src, encoding="utf-8") as f:
        data = json.load(f)
except (OSError, ValueError) as e:
    sys.exit(f"brief.json: {e}")

errors, ids = [], set()
if not data.get("title"):
    errors.append("нет title")
for si, s in enumerate(data.get("sections") or []):
    where = f"sections[{si}] ({s.get('id', '?')})"
    for key in ("id", "title"):
        if not s.get(key):
            errors.append(f"{where}: нет {key}")
    for ci, c in enumerate(s.get("cases") or []):
        cw = f"{where}.cases[{ci}] ({c.get('id', '?')})"
        for key in ("id", "title"):
            if not c.get(key):
                errors.append(f"{cw}: нет {key}")
        for key in ("steps", "expected"):
            if not c.get(key):
                errors.append(f"{cw}: пустой {key}")
        if c.get("priority") not in (None, "high", "normal", "low"):
            errors.append(f"{cw}: priority — high / normal / low")
        if c.get("id") in ids:
            errors.append(f"{cw}: повтор id")
        ids.add(c.get("id"))
if not data.get("sections"):
    errors.append("нет sections")
if errors:
    sys.exit("brief.json:\n  " + "\n  ".join(errors))

with open(template, encoding="utf-8") as f:
    html = f.read()
marker = "/*__BRIEF_DATA__*/"
if marker not in html:
    sys.exit("в шаблоне нет маркера данных")
# </ внутри JSON закрыл бы <script> — экранируем.
payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
with open(out, "w", encoding="utf-8") as f:
    f.write(html.replace(marker, payload))
cases = sum(len(s.get("cases") or []) for s in data["sections"])
print(f"{out}: разделов {len(data['sections'])}, кейсов {cases}")
EOF
