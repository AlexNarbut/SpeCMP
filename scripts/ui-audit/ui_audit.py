#!/usr/bin/env python3
"""UI-аудит Compose-кода фич и composition roots по docs/ui-kit.md: обходы дизайн-системы core/ui.

Ловит сырые цвета (hex, Color(r, g, b), Color.White/Black/…), размеры текста числами (`.sp` вместо роли
CmpTextStyle), TextStyle(...) в фиче и импорты визуальных компонентов мимо Cmp* (Material, material-icons,
foundation.Image, ui.window.Dialog, Coil). Отступы/радиусы/толщины и анимации не проверяются: в core/ui нет их токенов.

Текущие находки — в baseline.json рядом (по файлам); новая находка сверх baseline — ошибка.

  ui_audit.py                       # сводка по модулям
  ui_audit.py feature/vehicle-ui    # подробно: файлы и строки
  ui_audit.py --rule raw_sp         # только одно правило
  ui_audit.py --diff                # сравнить с baseline по модулям (рост → exit 1); это ./gradlew uiAudit
  ui_audit.py --file <path>         # один файл против его baseline (хук агентов; рост → exit 1)
  ui_audit.py --write-baseline      # зафиксировать текущее состояние (только после исправлений или осознанно)

Осознанное исключение — комментарий на той же или предыдущей строке:
  // ui-audit: ignore raw_color — причина
На весь файл: // ui-audit: ignore-file raw_color,raw_sp — причина
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASELINE = Path(__file__).resolve().parent / "baseline.json"
SCOPES = ["feature", "composeApp", "flasherApp"]

NAMED_COLORS = "Black|White|Red|Green|Blue|Gray|LightGray|DarkGray|Yellow|Cyan|Magenta"

RULES: list[tuple[str, re.Pattern[str], str]] = [
    ("hex_color", re.compile(r"\bColor\(\s*0x"),
     "Цвет в hex — роль cmpThemeColors()/cmpDomainColors(); нет роли — добавь токен в core/ui/theme"),
    ("raw_color", re.compile(rf"\bColor\.(?:{NAMED_COLORS})\b|\bColor\(\s*(?:red\s*=\s*)?\d+(?:\.\d+)?f?\s*,"),
     "Сырой цвет — роль cmpThemeColors()/cmpDomainColors() (Color.Transparent/Unspecified допустимы)"),
    ("raw_sp", re.compile(r"\b\d+(?:\.\d+)?\.sp\b"),
     "Размер текста числом — роль CmpText(style = CmpTextStyle.*); не хватает роли — дополни CmpTextStyle"),
    ("bare_text_style", re.compile(r"(?<![\w.])TextStyle\("),
     "TextStyle(...) в фиче — роль CmpTextStyle"),
    ("material_import", re.compile(r"^import androidx\.compose\.material3?\."),
     "Material/material-icons напрямую — Cmp* и CmpIcons из core/ui"),
    ("ui_component_import",
     re.compile(r"^import (?:androidx\.compose\.foundation\.Image|androidx\.compose\.ui\.window\.Dialog(?:Properties)?)$"),
     "Визуальный компонент мимо Cmp* — CmpImage/CmpDialog/CmpFullScreenDialog"),
    ("coil_import", re.compile(r"^import coil3?\."),
     "Coil в фиче — CmpAsyncImage/CmpSubcomposeAsyncImage (ImageLoader провайдит composition root)"),
]
FEATURE_ONLY = {"coil_import"}

LETTER_SPACING_RE = re.compile(r"letterSpacing\s*=\s*\(?-?[\d.]+\)?\.sp")
IGNORE_FILE_RE = re.compile(r"ui-audit:\s*ignore-file\s+([\w,\s]+)")
IGNORE_RE = re.compile(r"ui-audit:\s*ignore\s+([\w,\s]+)")


def audited(p: Path) -> bool:
    rel = p.relative_to(ROOT).parts
    return (p.suffix == ".kt" and rel[0] in SCOPES and "src" in rel and "build" not in rel and "testing" not in rel
            and not any(part == "test" or part.endswith("Test") for part in rel))


def iter_files(paths: list[str]) -> list[Path]:
    files: list[Path] = []
    for scope in paths:
        base = (ROOT / scope).resolve()
        if base.is_file():
            files.append(base)
        elif base.exists():
            files.extend(base.rglob("*.kt"))
    return sorted(f for f in set(files) if f.is_relative_to(ROOT) and audited(f))


def module_of(rel: str) -> str:
    parts = Path(rel).parts
    src = parts.index("src") if "src" in parts else len(parts)
    return "/".join(parts[:src])


def ignored(lines: list[str], idx: int, rule: str) -> bool:
    for j in (idx, idx - 1):
        if 0 <= j < len(lines):
            m = IGNORE_RE.search(lines[j])
            if m and rule in re.split(r"[\s,]+", m.group(1)):
                return True
    return False


def audit_file(path: Path) -> list[dict]:
    text = path.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines()
    rel = path.relative_to(ROOT).as_posix()
    file_ignored = {r.strip() for m in IGNORE_FILE_RE.finditer(text) for r in m.group(1).split(",")}
    findings: list[dict] = []
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith(("//", "*", "/*")):
            continue
        for rule, rx, msg in RULES:
            if rule in FEATURE_ONLY and not rel.startswith("feature/"):
                continue
            probe = LETTER_SPACING_RE.sub("", line) if rule == "raw_sp" else line
            if not rx.search(probe) or rule in file_ignored or ignored(lines, i, rule):
                continue
            findings.append({"file": rel, "line": i + 1, "rule": rule, "message": msg, "snippet": stripped[:140]})
    return findings


def run(paths: list[str]) -> list[dict]:
    return [f for p in iter_files(paths) for f in audit_file(p)]


def by_file(findings: list[dict]) -> dict[str, dict[str, int]]:
    out: dict[str, Counter] = defaultdict(Counter)
    for f in findings:
        out[f["file"]][f["rule"]] += 1
    return {k: dict(sorted(v.items())) for k, v in sorted(out.items())}


def by_module(counts: dict[str, dict[str, int]]) -> dict[str, Counter]:
    out: dict[str, Counter] = defaultdict(Counter)
    for file, rules in counts.items():
        out[module_of(file)].update(rules)
    return out


def load_baseline() -> dict[str, dict[str, int]] | None:
    return json.loads(BASELINE.read_text(encoding="utf-8")) if BASELINE.exists() else None


def print_findings(findings: list[dict]) -> None:
    for f in sorted(findings, key=lambda x: (x["file"], x["line"])):
        print(f"{f['file']}:{f['line']}  {f['rule']}  {f['message']}\n    {f['snippet']}")


def check_file(arg: str, base: dict[str, dict[str, int]]) -> int:
    path = Path(arg).resolve()
    if not path.is_file() or not path.is_relative_to(ROOT) or not audited(path):
        return 0
    findings = audit_file(path)
    allowed = base.get(path.relative_to(ROOT).as_posix(), {})
    now = Counter(f["rule"] for f in findings)
    grown = {r for r, n in now.items() if n > allowed.get(r, 0)}
    if not grown:
        return 0
    for r in sorted(grown):
        print(f"{r}: {now[r]} при baseline {allowed.get(r, 0)} — новые находки среди строк ниже")
    print_findings([f for f in findings if f["rule"] in grown])
    return 1


def diff(findings: list[dict], base: dict[str, dict[str, int]]) -> int:
    now_files = by_file(findings)
    old, new = by_module(base), by_module(now_files)
    worse = improved = False
    for m in sorted(set(old) | set(new)):
        for r in sorted(set(old.get(m, {})) | set(new.get(m, {}))):
            a, b = old.get(m, Counter())[r], new.get(m, Counter())[r]
            if a != b:
                worse |= b > a
                improved |= b < a
                print(f"{'▲' if b > a else '▼'} {m:<36} {r:<20} {a} → {b}")
    if worse:
        print("\nНовые обходы дизайн-системы (docs/ui-kit.md); исключение — // ui-audit: ignore <rule> — причина")
        print_findings([f for f in findings
                        if now_files[f["file"]][f["rule"]] > base.get(f["file"], {}).get(f["rule"], 0)])
    elif improved:
        print("находок стало меньше — зафиксируй: python3 scripts/ui-audit/ui_audit.py --write-baseline")
    print("ui-audit: регрессии есть" if worse else "ui-audit: регрессий нет")
    return 1 if worse else 0


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("paths", nargs="*", help="модули/папки/файлы (по умолчанию feature, composeApp, flasherApp)")
    ap.add_argument("--rule", action="append", help="только эти правила")
    ap.add_argument("--diff", action="store_true", help="сравнить с baseline; рост по модулю → exit 1")
    ap.add_argument("--file", help="один файл против его baseline; рост → exit 1")
    ap.add_argument("--write-baseline", action="store_true")
    args = ap.parse_args()

    if args.write_baseline:
        BASELINE.write_text(json.dumps(by_file(run(SCOPES)), ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
        print(f"baseline → {BASELINE.relative_to(ROOT)}")
        return 0

    base = load_baseline()
    if (args.diff or args.file) and base is None:
        print("baseline не найден: python3 scripts/ui-audit/ui_audit.py --write-baseline", file=sys.stderr)
        return 2
    if args.file:
        return check_file(args.file, base)

    findings = run(args.paths or SCOPES)
    if args.rule:
        findings = [f for f in findings if f["rule"] in args.rule]
    if args.diff:
        return diff(findings, base)

    if args.paths:
        print_findings(findings)
        print()
    for m, rules in sorted(by_module(by_file(findings)).items()):
        print(f"{m:<36} " + ", ".join(f"{r} {n}" for r, n in sorted(rules.items())))
    print(f"всего: {len(findings)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
