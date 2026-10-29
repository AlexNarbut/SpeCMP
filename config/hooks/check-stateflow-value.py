#!/usr/bin/env python3
"""MutableStateFlow меняется только через update { } — запись `.value = …` запрещена (docs/patterns.md, «Корутины и Flow»).

Ищет в main-исходниках переменные, объявленные как MutableStateFlow, и запись в их `.value`. Compose-состояния
(`mutableStateOf`) не трогает. Тестовые фейки — исключение (как в правиле). Без аргументов — все *.kt репозитория.
"""
import re
import subprocess
import sys
from pathlib import Path

DECL = re.compile(r"\b(?:val|var)\s+(\w+)\s*(?::\s*MutableStateFlow<[^=\n]*>\s*)?=\s*MutableStateFlow\b"
                  r"|\b(?:val|var)\s+(\w+)\s*:\s*MutableStateFlow<")
TEST_DIRS = ("/commonTest/", "/jvmTest/", "/androidUnitTest/", "/iosTest/", "/test/")


def check(path: Path) -> list:
    text = path.read_text()
    names = {a or b for a, b in DECL.findall(text)}
    if not names:
        return []
    write = re.compile(r"(?<![\w.])(?:this\.)?(" + "|".join(map(re.escape, names)) + r")\.value\s*=(?!=)")
    return [f"{path}:{n}: {m.group(1)}.value = … — пиши через {m.group(1)}.update {{ … }}"
            for n, line in enumerate(text.splitlines(), 1) for m in [write.search(line)] if m]


def main() -> int:
    files = sys.argv[1:] or subprocess.check_output(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "*.kt"], text=True
    ).split()
    errors = [e for f in files if f.endswith(".kt") and not any(d in f for d in TEST_DIRS) and Path(f).exists()
              for e in check(Path(f))]
    for e in errors:
        print(e)
    if errors:
        print("Правило: docs/patterns.md, «Корутины и Flow» — MutableStateFlow только через update { }", file=sys.stderr)
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
