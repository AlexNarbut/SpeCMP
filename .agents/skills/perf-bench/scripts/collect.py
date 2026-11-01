#!/usr/bin/env python3
"""raw/ прогона → results.json. Только stdlib. Пишет исключительно id из metrics-registry.json.

results.json:
  meta     — meta.txt прогона
  env      — окружение по платформам (env.txt)
  metrics  — id → {value, unit} или для сэмплированных {value=медиана, samples, min, max, mad, n}
  details  — разбивки и списки для отчёта
  notes    — предупреждения сборщика (не вердикт)
"""

import argparse
import csv
import json
import os
import re
import statistics
import sys
import time
import zipfile
from pathlib import Path
from xml.etree import ElementTree

# Модули SpeCMP: feature/<имя>[/<подмодуль>], core/<имя>; прочие (composeApp, flasherApp, …) — по первой папке.
LAYERS = ("feature", "core")
APP_PACKAGE = "ru.alarmtrade.specmp"


def read_kv(path: Path) -> dict[str, str]:
    result: dict[str, str] = {}
    if not path.exists():
        return result
    for line in path.read_text(errors="replace").splitlines():
        if "=" in line:
            key, value = line.split("=", 1)
            result[key.strip()] = value.strip()
    return result


def to_number(text: str | None) -> float | int | None:
    if text is None or text in ("", "null", "None"):
        return None
    try:
        return int(text)
    except ValueError:
        try:
            return float(text)
        except ValueError:
            return None


def first_int(pattern: str, text: str) -> int | None:
    match = re.search(pattern, text, re.MULTILINE)
    return int(match.group(1)) if match else None


def aggregate(samples: list[float | int | None]) -> dict:
    values = [v for v in samples if v is not None]
    if not values:
        return {"value": None, "samples": samples, "n": 0}
    median = statistics.median(values)
    mad = statistics.median(abs(v - median) for v in values)
    return {
        "value": median,
        "samples": samples,
        "min": min(values),
        "max": max(values),
        "mad": mad,
        "n": len(values),
    }


# --- статика --------------------------------------------------------------------------


def apk_breakdown(apk: Path) -> dict[str, dict]:
    groups = {k: {"bytes": 0, "files": 0} for k in ("dex", "lib", "res", "assets", "fonts", "other")}
    with zipfile.ZipFile(apk) as zf:
        for info in zf.infolist():
            name = info.filename
            lower = name.lower()
            if lower.endswith((".ttf", ".otf")):
                group = "fonts"
            elif re.fullmatch(r"classes\d*\.dex", name):
                group = "dex"
            elif name.startswith("lib/"):
                group = "lib"
            elif name.startswith("res/") or name == "resources.arsc":
                group = "res"
            elif name.startswith("assets/"):
                group = "assets"
            else:
                group = "other"
            groups[group]["bytes"] += info.compress_size
            groups[group]["files"] += 1
    return groups


def dex_packages(path: Path) -> tuple[int | None, int | None, list[dict]]:
    total = app = None
    modules: list[dict] = []
    if not path.exists():
        return total, app, modules
    for line in path.read_text(errors="replace").splitlines():
        parts = line.split()
        if len(parts) < 6 or parts[0] != "P":
            continue
        defined, name = int(parts[2]), parts[5]
        if name == "<TOTAL>":
            total = defined
        elif name == APP_PACKAGE:
            app = defined
        else:
            segments = name.split(".")
            if name.startswith(APP_PACKAGE + ".") and len(segments) == 5 and segments[3] in LAYERS:
                modules.append({"module": f"{segments[3]}/{segments[4]}", "methods": defined})
    modules.sort(key=lambda m: -m["methods"])
    return total, app, modules


def kt_lines(path: Path) -> tuple[int, int, list[dict]]:
    files = lines = 0
    by_module: dict[str, dict] = {}
    if not path.exists():
        return files, lines, []
    for line in path.read_text(errors="replace").splitlines():
        parts = line.split(None, 1)
        if len(parts) != 2 or not parts[0].isdigit() or parts[1] == "total":
            continue
        count, file_path = int(parts[0]), parts[1]
        segments = file_path.split("/")
        # Модуль — путь до src/ (feature/device/firmware); файлы вне модулей — other.
        module = "/".join(segments[: segments.index("src")]) if "src" in segments[1:] else "other"
        entry = by_module.setdefault(module, {"module": module, "lines": 0, "files": 0})
        entry["lines"] += count
        entry["files"] += 1
        files += 1
        lines += count
    return files, lines, sorted(by_module.values(), key=lambda m: -m["lines"])


def age_minutes(path: Path) -> int:
    return int((time.time() - path.stat().st_mtime) / 60)


def detekt_reports(list_path: Path, root: Path) -> tuple[int | None, dict]:
    paths = [root / p for p in list_path.read_text().split() if p] if list_path.exists() else []
    paths = [p for p in paths if p.exists()]
    if not paths:
        return None, {"reports": 0}
    by_rule: dict[str, int] = {}
    total = 0
    for p in paths:
        try:
            tree = ElementTree.parse(p)
        except ElementTree.ParseError:
            continue
        for error in tree.iter("error"):
            total += 1
            rule = (error.get("source") or "?").replace("detekt.", "")
            by_rule[rule] = by_rule.get(rule, 0) + 1
    rules = sorted(({"rule": r, "count": c} for r, c in by_rule.items()), key=lambda x: -x["count"])
    return total, {"reports": len(paths), "oldest_age_min": max(age_minutes(p) for p in paths), "by_rule": rules}


def test_reports(list_path: Path, root: Path) -> tuple[dict, dict]:
    paths = [root / p for p in list_path.read_text().split() if p] if list_path.exists() else []
    paths = [p for p in paths if p.exists()]
    if not paths:
        return {"total": None, "failed": None, "duration_s": None}, {"reports": 0}
    total = failed = skipped = 0
    duration = 0.0
    for p in paths:
        try:
            suite = ElementTree.parse(p).getroot()
        except ElementTree.ParseError:
            continue
        suites = [suite] if suite.tag == "testsuite" else list(suite.iter("testsuite"))
        for s in suites:
            total += int(s.get("tests", 0))
            failed += int(s.get("failures", 0)) + int(s.get("errors", 0))
            skipped += int(s.get("skipped", 0))
            duration += float(s.get("time", 0))
    values = {"total": total, "failed": failed, "duration_s": round(duration, 1)}
    return values, {"reports": len(paths), "skipped": skipped, "oldest_age_min": max(age_minutes(p) for p in paths)}


def compose_module_name(path: Path, root: Path) -> str:
    rel = path.relative_to(root).parts
    return "/".join(rel[: rel.index("build")]) if "build" in rel[1:] else rel[0]


def compose_metrics(list_path: Path, root: Path, metrics: dict, details: dict, notes: list[str]) -> None:
    paths = [root / p for p in list_path.read_text().split() if p]
    paths = [p for p in paths if p.exists()]
    if not paths:
        notes.append("static: отчётов compose-компилятора нет — сборка без init-скрипта compose-reports.init.gradle")
        return
    by_module: dict[str, dict] = {}
    for p in paths:
        entry = by_module.setdefault(compose_module_name(p, root), {"module": compose_module_name(p, root), "total": 0, "restartable": 0, "skippable": 0, "unstable_classes": 0})
        if p.name.endswith("-module.json"):
            data = json.loads(p.read_text() or "{}")
            entry["total"] += data.get("totalComposables", 0)
        elif p.name.endswith("-composables.csv"):
            # Именованные composable из CSV: module.json считает и composable-лямбды, которые при strong skipping
            # всегда выглядят нескипаемыми, — доля skippable из него вводит в заблуждение.
            for row in csv.DictReader(p.open(errors="replace")):
                if row.get("restartable") == "1":
                    entry["restartable"] += 1
                    entry["skippable"] += 1 if row.get("skippable") == "1" else 0
        else:
            entry["unstable_classes"] += sum(1 for line in p.read_text(errors="replace").splitlines() if line.startswith("unstable class"))
    total = sum(e["total"] for e in by_module.values())
    restartable = sum(e["restartable"] for e in by_module.values())
    skippable = sum(e["skippable"] for e in by_module.values())
    metrics["static.compose.composables_total"] = {"value": total}
    metrics["static.compose.restartable_total"] = {"value": restartable}
    metrics["static.compose.skippable_pct"] = {"value": round(skippable / restartable * 100, 1) if restartable else None}
    metrics["static.compose.unstable_classes"] = {"value": sum(e["unstable_classes"] for e in by_module.values())}
    for e in by_module.values():
        e["skippable_pct"] = round(e["skippable"] / e["restartable"] * 100, 1) if e["restartable"] else None
    details["static.compose.modules"] = sorted(by_module.values(), key=lambda e: (-e["unstable_classes"], -e["restartable"]))
    details["static.compose.reports"] = {"files": len(paths), "oldest_age_min": max(age_minutes(p) for p in paths)}


def collect_static(raw: Path, root: Path, metrics: dict, details: dict, notes: list[str]) -> None:
    apk_file = raw / "apk.path"
    if not apk_file.exists():
        return
    apk = Path(apk_file.read_text().strip())
    if not apk.exists():
        notes.append(f"static: APK не найден: {apk}")
        return
    metrics["static.apk.size_bytes"] = {"value": apk.stat().st_size}
    download = to_number((raw / "apk-download-size.txt").read_text().strip() if (raw / "apk-download-size.txt").exists() else None)
    metrics["static.apk.download_bytes"] = {"value": download}
    groups = apk_breakdown(apk)
    for group, data in groups.items():
        metrics[f"static.apk.{group}_bytes"] = {"value": data["bytes"]}
    details["static.apk.breakdown"] = [{"group": g, **d} for g, d in groups.items()]
    details["static.apk.path"] = str(apk)

    total, app, modules = dex_packages(raw / "dex-packages.txt")
    metrics["static.dex.methods_defined"] = {"value": total}
    metrics["static.dex.methods_app"] = {"value": app}
    details["static.dex.modules"] = modules
    if total is None:
        notes.append("static: нет вывода apkanalyzer dex packages")

    files, lines, by_module = kt_lines(raw / "kt-lines.txt")
    metrics["static.code.kt_files"] = {"value": files}
    metrics["static.code.kt_lines"] = {"value": lines}
    details["static.code.modules"] = by_module

    findings, detekt_info = detekt_reports(raw / "detekt-reports.txt", root)
    metrics["static.detekt.findings"] = {"value": findings}
    details["static.detekt"] = detekt_info
    if findings is None:
        notes.append("static: отчётов detekt нет — ./gradlew detektCheck перед прогоном")
    elif detekt_info.get("oldest_age_min", 0) > 240:
        notes.append(f"static: отчёты detekt старше {detekt_info['oldest_age_min']} мин — могут не отражать текущий код")

    tests, tests_info = test_reports(raw / "test-reports.txt", root)
    metrics["static.tests.total"] = {"value": tests["total"]}
    metrics["static.tests.failed"] = {"value": tests["failed"]}
    metrics["static.tests.duration_s"] = {"value": tests["duration_s"]}
    details["static.tests"] = tests_info
    if tests["total"] is None:
        notes.append("static: отчётов unit-тестов нет — ./gradlew test перед прогоном")

    compose_list = raw / "compose-reports.txt"
    if compose_list.exists():
        compose_metrics(compose_list, root, metrics, details, notes)

    build_time = raw / "build-time.txt"
    if build_time.exists():
        metrics["static.build.assemble_s"] = {"value": to_number(build_time.read_text().strip())}


# --- Android: старт ------------------------------------------------------------------

GFX_PATTERNS = {
    "frames_total": r"^Total frames rendered: (\d+)",
    "frame_p50_ms": r"^50th percentile: (\d+)ms",
    "frame_p90_ms": r"^90th percentile: (\d+)ms",
    "frame_p95_ms": r"^95th percentile: (\d+)ms",
    "frame_p99_ms": r"^99th percentile: (\d+)ms",
    "slow_ui_frames": r"^Number Slow UI thread: (\d+)",
}
GC_LINE = re.compile(r"GC freed .*?paused (\S+) total")
SKIPPED = re.compile(r"Skipped (\d+) frames")


def proc_cpu_ms(path: Path, clk_tck: int) -> int | None:
    text = path.read_text(errors="replace").strip() if path.exists() else ""
    if ")" not in text:
        return None
    fields = text.rsplit(")", 1)[1].split()
    # После «pid (comm)» поле 3 — индекс 0, поэтому utime (14) и stime (15) — индексы 11 и 12.
    if len(fields) < 13:
        return None
    return int((int(fields[11]) + int(fields[12])) * 1000 / clk_tck)


def pause_ms(spec: str) -> float:
    total = 0.0
    for part in spec.split(","):
        match = re.fullmatch(r"([\d.]+)(us|ms|s)", part.strip())
        if match:
            value, unit = float(match.group(1)), match.group(2)
            total += value / 1000 if unit == "us" else value * 1000 if unit == "s" else value
    return total


def parse_logcat(text: str) -> dict:
    errors: list[str] = []
    skipped = gc_count = 0
    gc_pause = 0.0
    for line in text.splitlines():
        if match := SKIPPED.search(line):
            skipped += int(match.group(1))
        if match := GC_LINE.search(line):
            gc_count += 1
            gc_pause += pause_ms(match.group(1))
        parts = line.split(None, 5)
        if len(parts) >= 6 and parts[4] == "E":
            errors.append(parts[5][:200])
    return {
        "skipped_frames": skipped,
        "gc_count": gc_count,
        "gc_pause_ms": round(gc_pause, 1),
        "log_errors": len(errors),
        "error_lines": errors,
    }


def displayed_ms(text: str) -> int | None:
    """«+5s931ms», «+931ms», «+2s» из строк ActivityTaskManager → мс."""
    match = re.search(r"\+(?:(\d+)s)?(?:(\d+)ms)?\s*$", text.strip().splitlines()[0]) if text.strip() else None
    if not match or not (match.group(1) or match.group(2)):
        return None
    return int(match.group(1) or 0) * 1000 + int(match.group(2) or 0)


def idle_frames(d: Path) -> int | None:
    path = d / "gfxinfo-idle.txt"
    return first_int(r"^Total frames rendered: (\d+)", path.read_text(errors="replace")) if path.exists() else None


def android_sample(d: Path, clk_tck: int) -> dict:
    kv = read_kv(d / "sample.txt")
    gfx = (d / "gfxinfo.txt").read_text(errors="replace") if (d / "gfxinfo.txt").exists() else ""
    mem = (d / "meminfo.txt").read_text(errors="replace") if (d / "meminfo.txt").exists() else ""
    logcat = parse_logcat((d / "logcat.txt").read_text(errors="replace") if (d / "logcat.txt").exists() else "")
    crash = (d / "crash.txt").read_text(errors="replace") if (d / "crash.txt").exists() else ""
    anr = (d / "anr.txt").read_text(errors="replace") if (d / "anr.txt").exists() else ""
    sample = {
        "id": d.name.removeprefix("sample-"),
        "launch_state": kv.get("launch_state", "UNKNOWN"),
        "ttid_ms": to_number(kv.get("ttid_ms")),
        "ttfc_ms": to_number(kv.get("ttfc_ms")),
        "ttfd_ms": displayed_ms((d / "fully-drawn.txt").read_text(errors="replace")) if (d / "fully-drawn.txt").exists() else None,
        "pss_kb": first_int(r"TOTAL PSS:\s+(\d+)", mem) or first_int(r"^\s+TOTAL\s+(\d+)", mem),
        "java_heap_kb": first_int(r"Java Heap:\s+(\d+)", mem),
        "native_heap_kb": first_int(r"Native Heap:\s+(\d+)", mem),
        "cpu_ms": proc_cpu_ms(d / "proc-stat.txt", clk_tck),
        "main_thread_cpu_ms": proc_cpu_ms(d / "proc-main-stat.txt", clk_tck),
        "activities": first_int(r"Activities:\s+(\d+)", mem),
        "idle_frames": idle_frames(d),
        "crashes": crash.count("FATAL EXCEPTION"),
        "anr": len([l for l in anr.splitlines() if l.strip()]),
    }
    for key, pattern in GFX_PATTERNS.items():
        sample[key] = first_int(pattern, gfx)
    janky = re.search(r"^Janky frames: \d+ \(([\d.]+)%\)", gfx, re.MULTILINE)
    frames = sample["frames_total"] or 0
    sample["janky_pct"] = float(janky.group(1)) if janky and frames >= 10 else None
    for key in ("skipped_frames", "gc_count", "gc_pause_ms", "log_errors"):
        sample[key] = logcat[key]
    sample["error_lines"] = logcat["error_lines"]
    return sample


ANDROID_SAMPLE_METRICS = (
    "ttid_ms", "ttfc_ms", "ttfd_ms", "frames_total", "janky_pct", "frame_p50_ms", "frame_p90_ms", "frame_p95_ms",
    "frame_p99_ms", "slow_ui_frames", "idle_frames", "skipped_frames", "pss_kb", "java_heap_kb", "native_heap_kb", "activities", "cpu_ms",
    "main_thread_cpu_ms", "gc_count", "gc_pause_ms", "log_errors",
    "crashes", "anr",
)


def collect_android(raw: Path, env: dict, metrics: dict, details: dict, notes: list[str]) -> None:
    if not raw.exists():
        return
    env["android"] = read_kv(raw / "env.txt")
    clk_tck = int(env["android"].get("clk_tck") or 100)
    dirs = sorted((d for d in raw.glob("sample-*") if not d.name.startswith("sample-w")), key=lambda d: int(d.name.split("-")[1]))
    warmups = [android_sample(d, clk_tck) for d in sorted(raw.glob("sample-w*"))]
    samples = [android_sample(d, clk_tck) for d in dirs]
    if not samples:
        return
    for key in ANDROID_SAMPLE_METRICS:
        values = [s[key] for s in samples]
        if key == "ttid_ms":
            values = [s[key] if s["launch_state"] == "COLD" else None for s in samples]
        metrics[f"android.startup.{key}"] = aggregate(values)
    not_cold = [s["id"] for s in samples if s["launch_state"] != "COLD"]
    if not_cold:
        notes.append(f"android: замеры {', '.join(not_cold)} не COLD — исключены из TTID")
    no_content = [s["id"] for s in samples if s["ttfc_ms"] is None]
    if no_content:
        notes.append(f"android: в замерах {', '.join(no_content)} контент (content_tag {read_kv(dirs[0] / 'sample.txt').get('content_tag')}) не появился за таймаут")
    details["android.startup.samples"] = [{k: v for k, v in s.items() if k != "error_lines"} for s in samples]
    details["android.startup.warmups"] = [{k: s[k] for k in ("id", "launch_state", "ttid_ms", "ttfc_ms")} for s in warmups]
    seen: dict[str, int] = {}
    for s in samples:
        for line in s["error_lines"]:
            seen[line] = seen.get(line, 0) + 1
    details["android.startup.error_lines"] = [{"line": l, "count": c} for l, c in sorted(seen.items(), key=lambda x: -x[1])[:30]]


# --- Android: сценарии фич и стабильность ----------------------------------------------

FEATURE_SAMPLE_METRICS = (
    "flow_ms", "frames_total", "janky_pct", "frame_p50_ms", "frame_p90_ms", "frame_p95_ms", "frame_p99_ms",
    "slow_ui_frames", "skipped_frames", "idle_frames", "pss_after_kb", "activities", "cpu_ms", "main_thread_cpu_ms",
    "gc_count", "gc_pause_ms", "log_errors", "crashes", "anr",
)


def cpu_delta(before: Path, after: Path, clk_tck: int, same_process: bool) -> int | None:
    after_ms = proc_cpu_ms(after, clk_tck)
    if after_ms is None:
        return None
    before_ms = proc_cpu_ms(before, clk_tck) if same_process else 0
    return None if before_ms is None else after_ms - before_ms


def android_feature_repeat(d: Path, clk_tck: int) -> dict:
    kv = read_kv(d / "sample.txt")
    same = bool(kv.get("pid")) and kv.get("pid") == kv.get("pid_after")
    gfx = (d / "gfxinfo.txt").read_text(errors="replace") if (d / "gfxinfo.txt").exists() else ""
    mem = (d / "meminfo.txt").read_text(errors="replace") if (d / "meminfo.txt").exists() else ""
    raw_log = (d / "logcat.txt").read_text(errors="replace") if (d / "logcat.txt").exists() else ""
    # Принудительный GC в конце повтора — наш, в метрики GC не входит.
    logcat = parse_logcat("\n".join(l for l in raw_log.splitlines() if "Explicit concurrent" not in l and "SIGUSR1" not in l))
    crash = (d / "crash.txt").read_text(errors="replace") if (d / "crash.txt").exists() else ""
    anr = (d / "anr.txt").read_text(errors="replace") if (d / "anr.txt").exists() else ""
    sample = {
        "id": d.name.removeprefix("repeat-"),
        "flow_ms": to_number(kv.get("flow_ms")),
        "flow_rc": to_number(kv.get("flow_rc")),
        "process_restarted": not same,
        "pss_after_kb": first_int(r"TOTAL PSS:\s+(\d+)", mem) or first_int(r"^\s+TOTAL\s+(\d+)", mem),
        "activities": first_int(r"Activities:\s+(\d+)", mem),
        "idle_frames": idle_frames(d),
        "cpu_ms": cpu_delta(d / "proc-stat-before.txt", d / "proc-stat-after.txt", clk_tck, same),
        "main_thread_cpu_ms": cpu_delta(d / "proc-main-stat-before.txt", d / "proc-main-stat-after.txt", clk_tck, same),
        "crashes": crash.count("FATAL EXCEPTION"),
        "anr": len([l for l in anr.splitlines() if l.strip()]),
    }
    for key, pattern in GFX_PATTERNS.items():
        sample[key] = first_int(pattern, gfx)
    janky = re.search(r"^Janky frames: \d+ \(([\d.]+)%\)", gfx, re.MULTILINE)
    sample["janky_pct"] = float(janky.group(1)) if janky and (sample["frames_total"] or 0) >= 10 else None
    for key in ("skipped_frames", "gc_count", "gc_pause_ms", "log_errors"):
        sample[key] = logcat[key]
    sample["error_lines"] = logcat["error_lines"]
    return sample


def collect_android_features(raw: Path, env: dict, metrics: dict, details: dict, notes: list[str]) -> None:
    clk_tck = int(env.get("android", {}).get("clk_tck") or 100)
    for fdir in sorted(raw.glob("feature-*")):
        name = fdir.name.removeprefix("feature-")
        scenario = read_kv(fdir / "scenario.txt")
        details.setdefault("features", {})[name] = scenario.get("title", name)
        repeats = [android_feature_repeat(d, clk_tck) for d in sorted(fdir.glob("repeat-*"), key=lambda d: int(d.name.split("-")[1]))]
        if not repeats:
            notes.append(f"feature {name}: повторов нет")
            continue
        prefix = f"android.feature.{name}."
        failed = [r for r in repeats if r["flow_rc"] != 0]
        ok = [r for r in repeats if r["flow_rc"] == 0]
        # Упали все повторы — числа описывают оборванный флоу, в отчёт идёт только flow_failed.
        for key in FEATURE_SAMPLE_METRICS:
            metrics[prefix + key] = aggregate([r[key] for r in ok]) if ok else {"value": None}
        metrics[prefix + "flow_failed"] = {"value": len(failed)}
        pss = [r["pss_after_kb"] for r in ok if r["pss_after_kb"] is not None]
        growth = pss[-1] - pss[0] if len(pss) >= 2 and scenario.get("reset_each", "False") != "True" else None
        metrics[prefix + "pss_growth_kb"] = {"value": growth}
        if failed:
            notes.append(f"feature {name}: упали повторы {', '.join(r['id'] for r in failed)} — см. raw/android/feature-{name}/repeat-*/maestro.log; их метрики в агрегат не входят")
        if any(r["process_restarted"] for r in ok) and scenario.get("reset_each", "False") != "True":
            notes.append(f"feature {name}: процесс приложения сменился во время повтора — CPU и память считаются от нового процесса")
        details[prefix + "repeats"] = [{k: v for k, v in r.items() if k != "error_lines"} for r in repeats]
        seen: dict[str, int] = {}
        for r in repeats:
            for line in r["error_lines"]:
                seen[line] = seen.get(line, 0) + 1
        details[prefix + "error_lines"] = [{"line": l, "count": c} for l, c in sorted(seen.items(), key=lambda x: -x[1])[:30]]


TRACE_KEYS = ("ttid_ms", "bind_application_ms", "activity_start_ms", "activity_resume_ms", "choreographer_ms",
              "main_running_ms", "main_runnable_ms", "main_io_ms", "main_sleep_ms")


def collect_android_trace(raw: Path, metrics: dict, details: dict, notes: list[str]) -> None:
    path = raw / "metrics.json"
    if not path.exists():
        if raw.exists():
            notes.append("trace: metrics.json не создан — см. raw/android/trace/perfetto.log")
        return
    data = json.loads(path.read_text())
    for key in TRACE_KEYS:
        if key in data:
            metrics[f"android.trace.{key}"] = {"value": data[key]}
    details["android.trace.top_slices"] = data.get("top_slices", [])
    details["android.trace.sample"] = read_kv(raw / "sample.txt")
    compose_path = raw / "compose-metrics.json"
    if compose_path.exists():
        compose = json.loads(compose_path.read_text())
        for key in ("home_frame_ms", "home_compose_ms", "home_draw_ms", "home_recompose_passes"):
            samples = compose.get(f"{key}_samples") or ([compose[key]] if compose.get(key) is not None else [])
            if samples:
                metrics[f"android.trace.{key}"] = aggregate(samples)
        measure = compose.get("home_measure_ms_samples") or ([compose["home_measure_ms"]] if compose.get("home_measure_ms") is not None else [])
        layout = compose.get("home_layout_ms_samples") or ([compose.get("home_layout_ms") or 0] * len(measure))
        if measure:
            metrics["android.trace.home_measure_ms"] = aggregate([round(m + (l or 0), 1) for m, l in zip(measure, layout)])
        details["android.trace.home_composables"] = compose.get("top_composables", [])
        details["android.trace.recomposed"] = compose.get("recomposed", [])
        if compose.get("note"):
            notes.append(f"trace: {compose['note']}")
    elif raw.exists():
        notes.append("trace: композиционного трейса нет — runtime-tracing в сборке отсутствует или SDK не включился (perfetto.log)")
    if data.get("error"):
        notes.append(f"trace: {data['error']}")
    if data.get("note"):
        notes.append(f"trace: {data['note']}")


def collect_android_stability(raw: Path, metrics: dict, details: dict, notes: list[str]) -> None:
    if not raw.exists():
        return
    meta = read_kv(raw / "monkey-meta.txt")
    monkey = (raw / "monkey.txt").read_text(errors="replace") if (raw / "monkey.txt").exists() else ""
    if meta:
        metrics["android.stability.monkey_events"] = {"value": to_number(meta.get("events"))}
        metrics["android.stability.monkey_duration_s"] = {"value": round((to_number(meta.get("duration_ms")) or 0) / 1000, 1)}
        metrics["android.stability.monkey_crashes"] = {"value": monkey.count("// CRASH")}
        metrics["android.stability.monkey_anr"] = {"value": monkey.count("// NOT RESPONDING")}
        details["android.stability.monkey"] = {**meta, "crash_excerpt": [l[:200] for l in monkey.splitlines() if l.startswith("//") and "Monkey" not in l][:20]}
    offline = read_kv(raw / "offline.txt")
    if offline:
        crash = (raw / "offline-crash.txt").read_text(errors="replace") if (raw / "offline-crash.txt").exists() else ""
        metrics["android.stability.offline_start_crashes"] = {"value": crash.count("FATAL EXCEPTION")}
        metrics["android.stability.offline_screen_ms"] = {"value": to_number(offline.get("offline_screen_ms"))}
        metrics["android.stability.offline_recovered"] = {"value": to_number(offline.get("recovered"))}
        metrics["android.stability.offline_recover_ms"] = {"value": to_number(offline.get("recover_ms"))}
        details["android.stability.offline"] = offline
        if offline.get("offline_shown", "none") == "none":
            notes.append("stability: при старте без сети ни один из ожидаемых экранов не появился (scenarios.json → stability.offline_tags)")


def collect_ios_features(raw: Path, metrics: dict, details: dict, notes: list[str]) -> None:
    for fdir in sorted(raw.glob("feature-*")):
        name = fdir.name.removeprefix("feature-")
        scenario = read_kv(fdir / "scenario.txt")
        details.setdefault("features", {})[name] = scenario.get("title", name)
        repeats = []
        for d in sorted(fdir.glob("repeat-*"), key=lambda d: int(d.name.split("-")[1])):
            kv = read_kv(d / "sample.txt")
            errors = (d / "log-errors.txt").read_text(errors="replace").splitlines() if (d / "log-errors.txt").exists() else []
            crashes = (d / "crashes.txt").read_text().split() if (d / "crashes.txt").exists() else []
            after, before = cputime_ms(kv.get("cputime_after")), cputime_ms(kv.get("cputime_before"))
            repeats.append({
                "id": d.name.removeprefix("repeat-"), "flow_ms": to_number(kv.get("flow_ms")), "flow_rc": to_number(kv.get("flow_rc")),
                "rss_after_kb": to_number(kv.get("rss_kb")),
                "cpu_ms": (after - before) if after is not None and before is not None else after,
                "log_errors": len([l for l in errors if l.strip()]), "crashes": len(crashes),
            })
        if not repeats:
            continue
        prefix = f"ios.feature.{name}."
        ok = [r for r in repeats if r["flow_rc"] == 0]
        for key in ("flow_ms", "rss_after_kb", "cpu_ms", "log_errors", "crashes"):
            metrics[prefix + key] = aggregate([r[key] for r in ok]) if ok else {"value": None}
        metrics[prefix + "flow_failed"] = {"value": len(repeats) - len(ok)}
        rss = [r["rss_after_kb"] for r in ok if r["rss_after_kb"] is not None]
        metrics[prefix + "rss_growth_kb"] = {"value": rss[-1] - rss[0] if len(rss) >= 2 and scenario.get("reset_each", "False") != "True" else None}
        details[prefix + "repeats"] = repeats
        if len(ok) < len(repeats):
            notes.append(f"ios feature {name}: упали повторы {', '.join(r['id'] for r in repeats if r['flow_rc'] != 0)}")


def registry_spec_id(metric_id: str) -> str:
    """android.feature.<сценарий>.<метрика> → android.feature.<метрика>; остальные id — как есть."""
    parts = metric_id.split(".")
    if len(parts) == 4 and parts[1] == "feature":
        return f"{parts[0]}.feature.{parts[3]}"
    return metric_id


# --- iOS: старт ------------------------------------------------------------------------


def cputime_ms(text: str | None) -> int | None:
    if not text:
        return None
    parts = text.split(":")
    try:
        seconds = float(parts[-1]) + int(parts[-2]) * 60 + (int(parts[-3]) * 3600 if len(parts) > 2 else 0)
    except (ValueError, IndexError):
        return None
    return int(seconds * 1000)


def collect_ios(raw: Path, env: dict, metrics: dict, details: dict, notes: list[str]) -> None:
    if not raw.exists():
        return
    env["ios"] = read_kv(raw / "env.txt")
    size_file = raw / "app-size.txt"
    if size_file.exists() and size_file.read_text().strip():
        metrics["ios.app.size_bytes"] = {"value": to_number(size_file.read_text().strip())}
    dirs = sorted((d for d in raw.glob("sample-*") if not d.name.startswith("sample-w")), key=lambda d: int(d.name.split("-")[1]))
    samples = []
    for d in dirs:
        kv = read_kv(d / "sample.txt")
        errors = (d / "log-errors.txt").read_text(errors="replace").splitlines() if (d / "log-errors.txt").exists() else []
        crashes = (d / "crashes.txt").read_text().split() if (d / "crashes.txt").exists() else []
        samples.append({
            "id": d.name.removeprefix("sample-"),
            "ttfc_ms": to_number(kv.get("ttfc_ms")),
            "rss_kb": to_number(kv.get("rss_kb")),
            "cpu_ms": cputime_ms(kv.get("cputime")),
            "log_errors": len([l for l in errors if l.strip()]),
            "crashes": len(crashes),
        })
    if not samples:
        notes.append("ios: замеров старта нет")
        return
    for key in ("ttfc_ms", "rss_kb", "cpu_ms", "log_errors", "crashes"):
        metrics[f"ios.startup.{key}"] = aggregate([s[key] for s in samples])
    details["ios.startup.samples"] = samples
    if any(s["ttfc_ms"] is None for s in samples):
        notes.append("ios: в части замеров контент не появился за таймаут (проверь ApplicationAccessibilityEnabled и Simulator.app)")


# --- main --------------------------------------------------------------------------------


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("run_dir")
    parser.add_argument("--root", required=True)
    parser.add_argument("--registry", required=True)
    args = parser.parse_args()
    run_dir, root = Path(args.run_dir), Path(args.root)
    registry = json.loads(Path(args.registry).read_text())["metrics"]

    metrics: dict = {}
    details: dict = {}
    notes: list[str] = []
    env: dict = {}
    collect_static(run_dir / "raw" / "static", root, metrics, details, notes)
    collect_android(run_dir / "raw" / "android", env, metrics, details, notes)
    if (run_dir / "raw" / "android").exists():
        env.setdefault("android", read_kv(run_dir / "raw" / "android" / "env.txt"))
        collect_android_features(run_dir / "raw" / "android", env, metrics, details, notes)
        collect_android_stability(run_dir / "raw" / "android" / "stability", metrics, details, notes)
        collect_android_trace(run_dir / "raw" / "android" / "trace", metrics, details, notes)
    collect_ios(run_dir / "raw" / "ios", env, metrics, details, notes)
    if (run_dir / "raw" / "ios").exists():
        collect_ios_features(run_dir / "raw" / "ios", metrics, details, notes)

    unknown = sorted(k for k in metrics if registry_spec_id(k) not in registry)
    if unknown:
        sys.exit(f"collect.py: метрики вне реестра: {unknown}")
    for key, entry in metrics.items():
        entry["unit"] = registry[registry_spec_id(key)]["unit"]

    results = {
        "schema": 1,
        "meta": read_kv(run_dir / "meta.txt"),
        "env": env,
        "metrics": metrics,
        "details": details,
        "notes": notes,
    }
    (run_dir / "results.json").write_text(json.dumps(results, ensure_ascii=False, indent=1))
    print(f"results: {run_dir / 'results.json'} ({len(metrics)} метрик, {len(notes)} заметок)")
    for note in notes:
        print(f"  ⚠️  {note}")


if __name__ == "__main__":
    main()
