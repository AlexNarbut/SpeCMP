#!/usr/bin/env python3
"""Проверка строковых ресурсов (docs/localization.md).

Все строки — в core/localization; префикс ключа = имя файла; формат {feature}_{screen}_{type}[_{name}]; у всех языков
тот же набор ключей (кроме translatable="false" — одинаковых на всех языках); без \' и \". Проверяет весь репозиторий.
"""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(subprocess.check_output(["git", "rev-parse", "--show-toplevel"], text=True).strip())
STRINGS = Path("core/localization/src/commonMain/composeResources")
TYPES = {"title", "subtitle", "btn", "label", "placeholder", "hint", "link", "error", "msg", "loading", "a11y",
         "value", "item", "status", "format"}
ENTRY = re.compile(r'<(string|plurals|string-array) name="([^"]+)"(?![^>]*translatable="false")')


def main() -> int:
    errors = []
    tracked = subprocess.check_output(["git", "ls-files", "*/composeResources/values*/*.xml"], cwd=ROOT, text=True)
    for path in tracked.split():
        if not path.startswith(str(STRINGS) + "/"):
            errors.append(f"{path}: строки — только в {STRINGS} (docs/localization.md)")

    base = ROOT / STRINGS / "values"
    langs = sorted(p for p in (ROOT / STRINGS).iterdir() if p.is_dir() and p.name.startswith("values-"))
    for file in sorted(base.glob("*.xml")):
        prefix = file.stem
        reference = entries(file)
        for kind, key in sorted(reference):
            parts = key.split("_")
            if parts[0] != prefix:
                errors.append(f"{rel(file)}: {key} — префикс должен быть «{prefix}_» (имя файла)")
            # common.xml: экран необязателен — common_{type}_{name} (общие строки без раздела)
            elif not (len(parts) >= 3 and parts[2] in TYPES) and not (prefix == "common" and parts[1] in TYPES):
                errors.append(f"{rel(file)}: {key} — формат {{feature}}_{{screen}}_{{type}}[_{{name}}], тип из таблицы")
        for lang in langs:
            other = lang / file.name
            if not other.exists():
                errors.append(f"{rel(other)}: нет файла — у каждого языка тот же набор файлов")
                continue
            for kind, key in sorted(reference - entries(other)):
                errors.append(f"{rel(other)}: нет {kind} {key}")
            for kind, key in sorted(entries(other) - reference):
                errors.append(f"{rel(other)}: лишний {kind} {key} (нет в values/)")
    for file in sorted((ROOT / STRINGS).rglob("*.xml")):
        for n, line in enumerate(file.read_text().splitlines(), 1):
            if "\\'" in line or '\\"' in line:
                errors.append(f"{rel(file)}:{n}: \\' и \\\" CMP не распознаёт — пиши кавычки сырыми")

    for e in errors:
        print(e)
    if errors:
        print("Правила — docs/localization.md", file=sys.stderr)
    return 1 if errors else 0


def entries(file: Path) -> set:
    return set(ENTRY.findall(file.read_text()))


def rel(path: Path) -> str:
    return str(path.relative_to(ROOT))


if __name__ == "__main__":
    sys.exit(main())
