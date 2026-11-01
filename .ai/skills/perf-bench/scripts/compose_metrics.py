#!/usr/bin/env python3
"""Композиционный трейс (новый экземпляр MainActivity при включённом runtime-tracing) → compose-metrics.json.

home_frame_ms      — самый долгий кадр после activityStart (начальная композиция + measure/layout + draw);
home_compose_ms    — Compose:recompose внутри этого кадра; home_measure_ms / home_layout_ms / home_draw_ms — view-фазы;
top_composables    — composable приложения по inclusive-времени в этом кадре (дерево: вложенные входят в родителя);
recomposed         — самые часто перекомпонуемые composable приложения за весь трейс (с числом вызовов).
"""

import json
import sys

from perfetto.trace_processor import TraceProcessor


def ms(value) -> float | None:
    return None if value is None else round(value / 1e6, 1)


def main() -> None:
    trace, pkg, out = sys.argv[1:4]
    tp = TraceProcessor(trace=trace)
    q = lambda sql: list(tp.query(sql))  # noqa: E731
    main_join = (f"join thread_track tt on s.track_id = tt.id join thread t using(utid) join process p using(upid) "
                 f"where p.name = '{pkg}' and t.tid = p.pid")
    app = "s.name like 'ru.alarmtrade.specmp.%' and s.name like '%.kt:%'"
    result: dict = {}
    starts = q(f"select min(s.ts) ts from slice s {main_join} and s.name like 'activityStart%'")
    act = starts[0].ts if starts and starts[0].ts is not None else None
    if act is None:
        result["note"] = "activityStart не найден — композиционный трейс без привязки к новому экземпляру"
        act = q("select min(ts) ts from slice")[0].ts
    frames = q(f"select s.id, s.ts, s.dur, s.track_id from slice s {main_join} and s.name like 'Choreographer#doFrame%' "
               f"and s.ts > {act} order by s.dur desc limit 1")
    if frames:
        f = frames[0]
        window = f"s.ts >= {f.ts} and s.ts < {f.ts + f.dur}"
        result["home_frame_ms"] = ms(f.dur)
        result["home_frame_offset_ms"] = ms(f.ts - act)
        # Первый кадр нового экземпляра пишет draw как «Record View#draw()», последующие — «draw».
        for key, names in (("home_compose_ms", ("Compose:recompose",)), ("home_measure_ms", ("measure",)), ("home_layout_ms", ("layout",)), ("home_draw_ms", ("draw", "Record View#draw()"))):
            quoted = ", ".join(f"'{n}'" for n in names)
            r = q(f"select coalesce(sum(s.dur), 0) d from slice s where {window} and s.track_id = {f.track_id} and s.name in ({quoted})")[0]
            result[key] = ms(r.d)
        # Проходы рекомпозиции внутри кадра: сколько раз раскладка возвращала кадр в композицию.
        result["home_recompose_passes"] = q(f"select count(*) c from slice s where {window} and s.track_id = {f.track_id} and s.name = 'Compose:recompose'")[0].c
        result["top_composables"] = [
            {"name": r.name.replace("ru.alarmtrade.specmp.", ""), "ms": ms(r.dur), "depth": r.depth}
            for r in q(f"select s.name, s.dur, s.depth from slice s where {window} and {app} order by s.dur desc limit 25")
        ]
    result["recomposed"] = [
        {"name": r.name.replace("ru.alarmtrade.specmp.", ""), "count": r.n, "ms": ms(r.dur)}
        for r in q(f"select s.name, count(*) n, sum(s.dur) dur from slice s where {app} group by s.name order by n desc limit 15")
    ]
    result["app_composable_slices"] = q(f"select count(*) c from slice s where {app}")[0].c
    tp.close()
    with open(out, "w") as fh:
        json.dump(result, fh, ensure_ascii=False, indent=1)
    print(f"compose metrics: {out}")


if __name__ == "__main__":
    main()
