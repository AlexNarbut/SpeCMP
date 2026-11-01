#!/usr/bin/env python3
"""Perfetto-трейс старта → metrics.json. Запускается python'ом из venv с пакетом perfetto (см. bench.sh doctor).

Выход (ключи — хвосты id android.trace.* из реестра):
  ttid_ms, bind_application_ms, activity_start_ms, activity_resume_ms, choreographer_ms — метрика android_startup;
  main_running_ms, main_runnable_ms, main_io_ms, main_sleep_ms — состояния главного потока в окне старта;
  top_slices — 15 самых долгих atrace-слайсов главного потока за весь трейс (детали, не метрика).
"""

import json
import sys

from perfetto.trace_processor import TraceProcessor


def ns_to_ms(value) -> float | None:
    return None if value is None else round(value / 1e6, 1)


def startup_metric(tp: TraceProcessor, pkg: str) -> dict:
    result: dict = {}
    try:
        metric = tp.metric(["android_startup"])
    except Exception as error:  # noqa: BLE001 — метрика может отсутствовать в старом trace_processor
        result["error"] = f"android_startup: {error}"
        return result
    for startup in metric.android_startup.startup:
        if startup.package_name != pkg:
            continue
        first_frame = startup.to_first_frame
        result["ttid_ms"] = ns_to_ms(first_frame.dur_ns)
        result["bind_application_ms"] = ns_to_ms(first_frame.time_bind_application.dur_ns) if first_frame.HasField("time_bind_application") else None
        result["activity_start_ms"] = ns_to_ms(first_frame.time_activity_start.dur_ns) if first_frame.HasField("time_activity_start") else None
        result["activity_resume_ms"] = ns_to_ms(first_frame.time_activity_resume.dur_ns) if first_frame.HasField("time_activity_resume") else None
        result["choreographer_ms"] = ns_to_ms(first_frame.time_choreographer.dur_ns) if first_frame.HasField("time_choreographer") else None
        states = first_frame.main_thread_by_task_state
        result["main_running_ms"] = ns_to_ms(states.running_dur_ns)
        result["main_runnable_ms"] = ns_to_ms(states.runnable_dur_ns)
        result["main_io_ms"] = ns_to_ms(states.uninterruptible_sleep_dur_ns)
        result["main_sleep_ms"] = ns_to_ms(states.interruptible_sleep_dur_ns)
        break
    return result


def main_thread_states(tp: TraceProcessor, pkg: str) -> dict:
    """Запасной расчёт по thread_state за весь трейс, если android_startup не нашёл запуск."""
    rows = tp.query(f"""
        select ts.state, sum(ts.dur) dur from thread_state ts
        join thread t using(utid) join process p using(upid)
        where p.name = '{pkg}' and t.tid = p.pid group by ts.state""")
    by_state = {r.state: r.dur for r in rows}
    return {
        "main_running_ms": ns_to_ms(by_state.get("Running")),
        "main_runnable_ms": ns_to_ms((by_state.get("R") or 0) + (by_state.get("R+") or 0)),
        "main_io_ms": ns_to_ms(by_state.get("D")),
        "main_sleep_ms": ns_to_ms(by_state.get("S")),
    }


def top_slices(tp: TraceProcessor, pkg: str, limit: int = 15) -> list[dict]:
    rows = tp.query(f"""
        select s.name, s.dur, s.ts from slice s
        join thread_track tt on s.track_id = tt.id
        join thread t using(utid) join process p using(upid)
        where p.name = '{pkg}' and t.tid = p.pid and s.depth = 0
        order by s.dur desc limit {limit}""")
    return [{"name": r.name, "ms": ns_to_ms(r.dur)} for r in rows]


def compose_slices(tp: TraceProcessor, pkg: str) -> dict:
    """Слайсы композиции из runtime-tracing (track_event): до первого «Fully drawn» / конца трейса.

    compose_total_ms — суммарное время слайсов верхнего уровня композиции на главном потоке;
    top_composables — composable по суммарному inclusive-времени (имя слайса = функция)."""
    rows = tp.query(f"""
        select s.name, sum(s.dur) dur, count(*) n from slice s
        join thread_track tt on s.track_id = tt.id join thread t using(utid) join process p using(upid)
        where p.name = '{pkg}' and t.tid = p.pid and s.category is not null
          and (s.name like 'Compose:%' or s.name like '%(%' or s.name like '%.%')
          and s.name not like 'Choreographer%' and s.name not like '/data/%'
        group by s.name order by dur desc limit 25""")
    top = [{"name": r.name, "ms": ns_to_ms(r.dur), "n": r.n} for r in rows]
    total = tp.query(f"""
        select coalesce(sum(s.dur), 0) dur from slice s
        join thread_track tt on s.track_id = tt.id join thread t using(utid) join process p using(upid)
        where p.name = '{pkg}' and t.tid = p.pid and s.name like 'Compose:%' """)
    return {"compose_total_ms": ns_to_ms([r.dur for r in total][0]), "top_composables": top}


def main() -> None:
    trace, pkg, out = sys.argv[1:4]
    tp = TraceProcessor(trace=trace)
    metrics = startup_metric(tp, pkg)
    if metrics.get("ttid_ms") is None:
        metrics.update(main_thread_states(tp, pkg))
        metrics.setdefault("note", "android_startup не нашёл запуск — состояния главного потока за весь трейс")
    metrics["top_slices"] = top_slices(tp, pkg)
    metrics.update(compose_slices(tp, pkg))
    tp.close()
    with open(out, "w") as f:
        json.dump(metrics, f, ensure_ascii=False, indent=1)
    print(f"trace metrics: {out}")


if __name__ == "__main__":
    main()
