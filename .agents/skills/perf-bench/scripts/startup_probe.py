#!/usr/bin/env python3
"""Один замер холодного старта: запуск приложения и ожидание контента с точным хост-таймером.

Вывод — строки key=value (читает bench.sh):
  Android: launch_state, ttid_ms (TotalTime из am start -W), ttfc_ms, content_tag
  iOS:     ttfc_ms, content_tag

ttfc_ms — миллисекунды от команды запуска до первого дампа дерева, содержащего content_tag;
null, если за timeout_s тег не появился.

content_tag — один или несколько вариантов через «|»: testTag (resource-id / accessibility identifier) или точный
текст элемента. В SpeCMP на главной нет testTag, поэтому экран узнаётся по тексту на обоих языках.
"""

import subprocess
import sys
import time

POLL_PAUSE_S = 0.3


def now_ms() -> int:
    return int(time.monotonic() * 1000)


def run(cmd: list[str], timeout: float = 30) -> str:
    try:
        return subprocess.run(cmd, capture_output=True, text=True, timeout=timeout, errors="replace").stdout
    except subprocess.TimeoutExpired:
        return ""


def android_needles(tag: str) -> list[str]:
    return [f'{attr}="{t}"' for t in tag.split("|") for attr in ("resource-id", "text", "content-desc")]


def ios_needles(tag: str) -> list[str]:
    return [f'"{t}"' for t in tag.split("|")]


def wait_content(dump: list[str], needle: str | list[str], t0: int, timeout_s: float, stop: list[str] | None = None) -> int | None:
    """Ждёт needle (любой из списка) в выводе dump. stop — команда, непустой вывод которой завершает ожидание без
    результата (release-сборка не отдаёт testTag как resource-id, окно замера ограничивает системный «Fully drawn»)."""
    needles = [needle] if isinstance(needle, str) else needle
    deadline = t0 + int(timeout_s * 1000)
    while now_ms() < deadline:
        out = run(dump, timeout=15)
        if any(n in out for n in needles):
            return now_ms() - t0
        if stop and run(stop, timeout=15).strip():
            return None
        time.sleep(POLL_PAUSE_S)
    return None


def android(serial: str, pkg: str, activity: str, tag: str, timeout_s: float) -> dict:
    adb = ["adb", "-s", serial, "shell"]
    t0 = now_ms()
    out = run(adb + ["am", "start", "-W", "-S", "-n", f"{pkg}/{activity}"], timeout=60)
    fields = dict(line.split(":", 1) for line in out.splitlines() if ":" in line)
    ttid = fields.get("TotalTime", "").strip()
    # Дамп в файл, а не в /dev/tty: на новых API вывод в tty теряется. Во время старта дамп может
    # вернуть ошибку вместо XML — опрос продолжается.
    fully_drawn = adb + [f"logcat -d -t 2000 | grep 'Fully drawn {pkg}'"]
    ttfc = wait_content(android_dump_cmd(serial), android_needles(tag), t0, timeout_s)
    # Тег не виден (release без testTagsAsResourceId) — ждём «Fully drawn» ещё до таймаута, но не дольше 10 с после него.
    if ttfc is None and not run(fully_drawn, timeout=15).strip():
        wait_content(adb + ["true"], "\0", now_ms(), 10, stop=fully_drawn)
    return {
        "launch_state": fields.get("LaunchState", "").strip() or "UNKNOWN",
        "ttid_ms": ttid if ttid.isdigit() else "null",
        "ttfc_ms": ttfc if ttfc is not None else "null",
    }


def ios(udid: str, bundle_id: str, tag: str, timeout_s: float) -> dict:
    t0 = now_ms()
    run(["xcrun", "simctl", "launch", udid, bundle_id], timeout=60)
    ttfc = wait_content(["axe", "describe-ui", "--udid", udid], ios_needles(tag), t0, timeout_s)
    return {"ttfc_ms": ttfc if ttfc is not None else "null"}


def android_dump_cmd(serial: str) -> list[str]:
    return ["adb", "-s", serial, "shell", "uiautomator dump /data/local/tmp/perf-bench.xml >/dev/null 2>&1 && cat /data/local/tmp/perf-bench.xml"]


def android_wait(serial: str, tag: str, timeout_s: float) -> dict:
    """Ждёт тег на уже запущенном приложении (restore после стабильности, предусловие сценария)."""
    waited = wait_content(android_dump_cmd(serial), android_needles(tag), now_ms(), timeout_s)
    return {"recovered": 1 if waited is not None else 0, "recover_ms": waited if waited is not None else "null"}


def android_offline(serial: str, pkg: str, activity: str, tags: str, timeout_s: float) -> dict:
    """Старт без сети: любой из тегов/текстов (через |) считается показанным экраном."""
    adb = ["adb", "-s", serial, "shell"]
    t0 = now_ms()
    run(adb + ["am", "start", "-W", "-S", "-n", f"{pkg}/{activity}"], timeout=60)
    needles = android_needles(tags)
    deadline = t0 + int(timeout_s * 1000)
    shown = None
    while now_ms() < deadline and shown is None:
        xml = run(android_dump_cmd(serial), timeout=15)
        shown = next((n for n in needles if n in xml), None)
        if shown is None:
            time.sleep(POLL_PAUSE_S)
    return {"offline_screen_ms": now_ms() - t0 if shown else "null", "offline_shown": shown or "none"}


def main() -> None:
    platform = sys.argv[1]
    if platform == "android":
        serial, pkg, activity, tag, timeout_s = sys.argv[2:7]
        result = android(serial, pkg, activity, tag, float(timeout_s))
    elif platform == "ios":
        udid, bundle_id, tag, timeout_s = sys.argv[2:6]
        result = ios(udid, bundle_id, tag, float(timeout_s))
    elif platform == "android-wait":
        serial, tag, timeout_s = sys.argv[2:5]
        result = android_wait(serial, tag, float(timeout_s))
    elif platform == "android-offline":
        serial, pkg, activity, tag, timeout_s = sys.argv[2:7]
        result = android_offline(serial, pkg, activity, tag, float(timeout_s))
    else:
        sys.exit(f"startup_probe: неизвестный режим {platform}")
    result["content_tag"] = tag
    for key, value in result.items():
        print(f"{key}={value}")


if __name__ == "__main__":
    main()
