#!/usr/bin/env python3
"""Сводит compose-metrics-N.json повторов в compose-metrics.json: скаляры — медиана и MAD, списки — медиана по имени."""

import json
import statistics
import sys

SCALARS = ("home_frame_ms", "home_frame_offset_ms", "home_compose_ms", "home_measure_ms", "home_layout_ms", "home_draw_ms", "home_recompose_passes", "app_composable_slices")


def median_by_name(lists: list[list[dict]], key: str, value: str, extra: tuple[str, ...] = ()) -> list[dict]:
    by_name: dict[str, list[dict]] = {}
    for items in lists:
        for item in items:
            by_name.setdefault(item[key], []).append(item)
    merged = []
    for name, items in by_name.items():
        entry = {key: name, value: round(statistics.median(i[value] for i in items), 1), "repeats": len(items)}
        for field in extra:
            entry[field] = items[0].get(field)
        merged.append(entry)
    return sorted(merged, key=lambda e: -e[value])


def main() -> None:
    out, paths = sys.argv[1], sys.argv[2:]
    runs = [json.load(open(p)) for p in paths]
    result: dict = {"repeats": len(runs), "per_repeat": {k: [r.get(k) for r in runs] for k in SCALARS}}
    for key in SCALARS:
        values = [r[key] for r in runs if r.get(key) is not None]
        if values:
            median = statistics.median(values)
            result[key] = round(median, 1)
            result[f"{key}_mad"] = round(statistics.median(abs(v - median) for v in values), 1)
            result[f"{key}_samples"] = values
    result["top_composables"] = median_by_name([r.get("top_composables", []) for r in runs], "name", "ms", ("depth",))[:25]
    result["recomposed"] = median_by_name([r.get("recomposed", []) for r in runs], "name", "count", ("ms",))[:15]
    notes = [r["note"] for r in runs if r.get("note")]
    if notes:
        result["note"] = notes[0]
    with open(out, "w") as fh:
        json.dump(result, fh, ensure_ascii=False, indent=1)
    print(f"compose aggregate: {out} ({len(runs)} повторов)")


if __name__ == "__main__":
    main()
