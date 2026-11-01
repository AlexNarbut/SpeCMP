#!/usr/bin/env python3
"""results.json (+ база, + история) → report.md и report.html. Вердикт — чистая функция от реестра и чисел.

Правило (metrics-registry.json → verdict_rule): Δ = current − baseline; регресс по направлению better;
регресс засчитывается только при |Δ| > шум = 2 × max(MAD базы, MAD текущего); уровень fail / warn —
когда |Δ| ≥ abs И |Δ%| ≥ pct соответствующего порога; fail_if_above — абсолютный предел без базы.
"""

import argparse
import html
import json
from pathlib import Path

STATUS = {
    "fail": ("❌", "регресс"),
    "warn": ("⚠️", "внимание"),
    "ok": ("✅", "ок"),
    "better": ("🟢", "лучше"),
    "info": ("ℹ️", "справочно"),
    "nobase": ("⚪", "нет базы"),
    "na": ("—", "нет данных"),
}
RANK = {"fail": 3, "warn": 2, "ok": 1, "better": 1, "info": 0, "nobase": 0, "na": 0}
GROUP_ORDER = ("static", "android.startup", "android.trace", "android.feature", "android.stability", "ios.app", "ios.startup", "ios.feature")


def spec_id(mid: str) -> str:
    """android.feature.<сценарий>.<метрика> → android.feature.<метрика>."""
    parts = mid.split(".")
    return f"{parts[0]}.feature.{parts[3]}" if len(parts) == 4 and parts[1] == "feature" else mid


def group_of(mid: str) -> str:
    parts = mid.split(".")
    if len(parts) == 4 and parts[1] == "feature":
        return ".".join(parts[:3])
    for g in GROUP_ORDER:
        if mid.startswith(g + "."):
            return g
    return parts[0]


def group_title(group: str, registry: dict, results: dict) -> str:
    parts = group.split(".")
    if len(parts) == 3 and parts[1] == "feature":
        platform = "Android" if parts[0] == "android" else "iOS"
        return f"{platform}: сценарий {parts[2]} — {results.get('details', {}).get('features', {}).get(parts[2], '')}"
    return registry["groups"].get(group, group)


def ordered_groups(rows: list[dict]) -> list[str]:
    present = {group_of(r["id"]) for r in rows}
    def rank(g: str) -> tuple:
        for i, prefix in enumerate(GROUP_ORDER):
            if g == prefix or g.startswith(prefix + "."):
                return (i, g)
        return (len(GROUP_ORDER), g)
    return sorted(present, key=rank)


# --- форматирование --------------------------------------------------------------------


def group_digits(value: float) -> str:
    return f"{value:,.0f}".replace(",", " ")


def fmt(value, unit: str, signed: bool = False) -> str:
    if value is None:
        return "—"
    sign = "+" if signed and value > 0 else ""
    if unit == "B":
        mib = value / 1048576
        text = f"{mib:.2f} MiB" if abs(mib) >= 1 else f"{value / 1024:.0f} KiB"
    elif unit == "kB":
        text = f"{value / 1024:.1f} MB"
    elif unit == "ms":
        text = f"{value / 1000:.2f} s" if abs(value) >= 10000 else f"{group_digits(value)} ms"
    elif unit == "s":
        text = f"{value:.0f} s"
    elif unit == "%":
        text = f"{value:.1f} %"
    else:
        text = group_digits(value) if float(value).is_integer() else f"{value:.1f}"
    return sign + text


def fmt_pct(pct) -> str:
    return "" if pct is None else f"{pct:+.1f} %"


def threshold_text(spec: dict, unit: str) -> str:
    parts = []
    for level in ("warn", "fail"):
        thr = spec.get(level)
        if not thr:
            continue
        conds = []
        if thr.get("abs"):
            conds.append(fmt(thr["abs"], unit))
        if thr.get("pct"):
            conds.append(f"{thr['pct']} %")
        parts.append(f"{level}: ≥ {' и ≥ '.join(conds)}" if conds else f"{level}: любое")
    if "warn_if_above" in spec:
        parts.append(f"warn, если > {spec['warn_if_above']} даже без базы")
    if "fail_if_above" in spec:
        parts.append(f"fail, если > {spec['fail_if_above']}")
    if "fail_if_below" in spec:
        parts.append(f"fail, если < {spec['fail_if_below']}")
    return "; ".join(parts) if parts else "без порогов"


# --- вердикт -------------------------------------------------------------------------------


def hits(level: dict | None, delta: float, pct: float | None) -> bool:
    if not level:
        return False
    abs_ok = not level.get("abs") or abs(delta) >= level["abs"]
    pct_ok = not level.get("pct") or pct is None or abs(pct) >= level["pct"]
    return abs_ok and pct_ok


def evaluate(spec: dict, cur: dict | None, base: dict | None) -> tuple[str, str, dict]:
    cv = cur.get("value") if cur else None
    bv = base.get("value") if base else None
    calc = {"delta": None, "pct": None, "noise": None}
    if cv is None:
        return "na", "значение не получено", calc
    limit = spec.get("fail_if_above")
    if limit is not None and cv > limit:
        return "fail", f"значение {fmt(cv, spec['unit'])} выше предела {limit}", calc
    floor = spec.get("fail_if_below")
    if floor is not None and cv < floor:
        return "fail", f"значение {fmt(cv, spec['unit'])} ниже предела {floor}", calc
    warn_limit = spec.get("warn_if_above")
    if bv is None:
        if warn_limit is not None and cv > warn_limit:
            return "warn", f"значение {fmt(cv, spec['unit'])} выше ожидаемого {warn_limit}", calc
        return "nobase", "", calc
    delta = cv - bv
    pct = delta / bv * 100 if bv else None
    noise = 2 * max((cur.get("mad") or 0), (base.get("mad") or 0))
    calc = {"delta": delta, "pct": pct, "noise": noise}
    if spec["better"] == "none":
        return "info", "", calc
    if delta == 0:
        return "ok", "без изменений", calc
    regress = delta > 0 if spec["better"] == "lower" else delta < 0
    if not regress:
        if warn_limit is not None and cv > warn_limit:
            return "warn", f"значение {fmt(cv, spec['unit'])} выше ожидаемого {warn_limit}, хотя лучше базы", calc
        return "better", "", calc
    if warn_limit is not None and cv > warn_limit and abs(delta) <= noise:
        return "warn", f"значение {fmt(cv, spec['unit'])} выше ожидаемого {warn_limit} (как и в базе)", calc
    if abs(delta) <= noise:
        return "ok", f"в пределах шума ±{fmt(noise, spec['unit'])}", calc
    if hits(spec.get("fail"), delta, pct):
        return "fail", f"порог fail: {threshold_text({'fail': spec['fail']}, spec['unit'])[6:]}", calc
    if hits(spec.get("warn"), delta, pct):
        return "warn", f"порог warn: {threshold_text({'warn': spec['warn']}, spec['unit'])[6:]}", calc
    return "ok", "ниже порога warn", calc


def build_rows(registry: dict, results: dict, baseline: dict | None) -> list[dict]:
    rows = []
    specs = registry["metrics"]
    template_order = {mid: i for i, mid in enumerate(specs)}
    ids = list(results["metrics"]) + [m for m in (baseline["metrics"] if baseline else {}) if m not in results["metrics"]]
    ids = [m for m in ids if spec_id(m) in specs]
    ids.sort(key=lambda m: (group_of(m), template_order[spec_id(m)]))
    for mid in ids:
        spec = specs[spec_id(mid)]
        cur = results["metrics"].get(mid)
        base = baseline["metrics"].get(mid) if baseline else None
        status, reason, calc = evaluate(spec, cur, base)
        rows.append({"id": mid, "spec": spec, "cur": cur, "base": base, "status": status, "reason": reason, **calc})
    # Группа, которую этот прогон не мерил (например, iOS-прогон против Android-базы), в отчёт не попадает.
    measured = {group_of(r["id"]) for r in rows if r["cur"] is not None}
    return [r for r in rows if group_of(r["id"]) in measured]


def overall(rows: list[dict], has_base: bool) -> str:
    # База без данных по этим метрикам (Android-база для iOS-прогона) — сравнения нет.
    if not has_base or not any(r["base"] and r["base"].get("value") is not None for r in rows):
        return "nobase"
    worst = max((RANK[r["status"]] for r in rows), default=0)
    return {3: "fail", 2: "warn"}.get(worst, "ok")


# --- Markdown ----------------------------------------------------------------------------


def problem_title(r: dict) -> str:
    parts = r["id"].split(".")
    scenario = f" [{parts[2]}]" if len(parts) == 4 and parts[1] == "feature" else ""
    return r["spec"]["title"] + scenario


def problem_values(r: dict) -> str:
    unit = r["spec"]["unit"]
    cur = fmt(r["cur"]["value"], unit) if r["cur"] else "—"
    if r["delta"] is None:
        return cur if not r["base"] else f"{fmt(r['base']['value'], unit)} → {cur}"
    pct = f", {fmt_pct(r['pct'])}" if r["pct"] is not None else ""
    return f"{fmt(r['base']['value'], unit)} → {cur} ({fmt(r['delta'], unit, True)}{pct})"


def env_line(results: dict) -> str:
    parts = []
    a = results["env"].get("android")
    if a:
        parts.append(f"Android: {a.get('model', '?')} API {a.get('sdk', '?')} AVD {a.get('avd') or '—'} CPU {a.get('cpus', '?')} anim {a.get('anim_scale', '?')}")
    i = results["env"].get("ios")
    if i:
        parts.append(f"iOS: {i.get('name', '?')} {i.get('runtime', '')}")
    return "; ".join(parts) or "без устройства"


def md_report(results: dict, baseline: dict | None, rows: list[dict], registry: dict, html_path: Path) -> str:
    meta = results["meta"]
    verdict = overall(rows, baseline is not None)
    icon, label = STATUS[verdict]
    out = [f"# perf-bench: {meta.get('kind')}{' · ' + meta['label'] if meta.get('label') else ''} — {meta.get('sha7')} ({meta.get('branch')}) — {meta.get('created')}", ""]
    out.append(f"**Вердикт: {icon} {label.upper()}**")
    if baseline:
        bm = baseline["meta"]
        out.append(f"База: {bm.get('sha7')} ({bm.get('branch')}) {bm.get('created')}{' · ' + bm['label'] if bm.get('label') else ''}")
    else:
        out.append("База: нет — только значения, без сравнения")
    out.append(f"Стенд: {env_line(results)}; сборка androidApp debug; замеров старта {meta.get('runs')}, settle {meta.get('settle_s')} с")
    if meta.get("dirty") == "1":
        out.append("⚠️ рабочее дерево грязное — sha не описывает код полностью")
    problems = [r for r in rows if r["status"] in ("fail", "warn")]
    if problems:
        out += ["", "Причины:"]
        for r in problems:
            out.append(f"- {STATUS[r['status']][0]} {problem_title(r)}: {problem_values(r)}; {r['reason']}")
    if results.get("notes"):
        out += ["", "Заметки сборщика:"] + [f"- {n}" for n in results["notes"]]
    for g in ordered_groups(rows):
        grows = [r for r in rows if group_of(r["id"]) == g]
        out += ["", f"## {group_title(g, registry, results)}", "", "| Метрика | База | Текущее | Δ | Статус |", "|---|---|---|---|---|"]
        for r in grows:
            unit = r["spec"]["unit"]
            cur = fmt(r["cur"]["value"], unit) if r["cur"] else "—"
            if r["cur"] and r["cur"].get("mad") is not None:
                cur += f" ±{fmt(r['cur']['mad'], unit)}"
            base = fmt(r["base"]["value"], unit) if r["base"] else "—"
            delta = fmt(r["delta"], unit, True) + (f" ({fmt_pct(r['pct'])})" if r["pct"] is not None else "") if r["delta"] is not None else "—"
            si, sl = STATUS[r["status"]]
            out.append(f"| {r['spec']['title']} | {base} | {cur} | {delta} | {si} {sl}{' — ' + r['reason'] if r['reason'] and r['status'] in ('fail', 'warn', 'ok') and r['reason'] != 'без изменений' else ''} |")
    errors = results["details"].get("android.startup.error_lines")
    if errors:
        out += ["", "Ошибки в логе за старт (топ):"] + [f"- ×{e['count']} {e['line'][:160]}" for e in errors[:8]]
    slices = results["details"].get("android.trace.top_slices")
    if slices:
        out += ["", "Perfetto: самые долгие слайсы главного потока за старт:"] + [f"- {e['ms']} ms {e['name']}" for e in slices[:8]]
    comps = results["details"].get("android.trace.home_composables")
    if comps:
        out += ["", "Начальная композиция главной, composable по inclusive-времени (вложенные входят в родителя):"] + [f"- {e['ms']} ms d{e['depth']} {e['name']}" for e in comps[:8]]
    rec = results["details"].get("android.trace.recomposed")
    if rec:
        out += ["", "Самые частые рекомпозиции за 9 с на главной:"] + [f"- ×{e['count']} {e['ms']} ms {e['name']}" for e in rec[:6]]
    for name in results["details"].get("features", {}):
        ferr = results["details"].get(f"android.feature.{name}.error_lines")
        if ferr:
            out += ["", f"Ошибки в логе за сценарий {name} (топ):"] + [f"- ×{e['count']} {e['line'][:160]}" for e in ferr[:6]]
    out += ["", f"Подробности и определения метрик: {html_path.name} рядом; правила — metrics-registry.json. "
            "Вердикт: регресс по направлению «лучше», если |Δ| > 2×MAD и выше порога warn/fail (abs И %)."]
    return "\n".join(out) + "\n"


# --- HTML ----------------------------------------------------------------------------------

CSS = """
:root{--bg:#fcfcfb;--surface:#ffffff;--ink:#0b0b0b;--ink2:#52514e;--muted:#898781;--line:#e6e4df;--accent:#2457c5;
--ok:#0ca30c;--warn:#fab219;--fail:#d03b3b;--better:#006300;--okbg:#e9f6e9;--warnbg:#fff4d6;--failbg:#fbe4e4;--betterbg:#e4f1e4;--infobg:#f0efec}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]){--bg:#1a1a19;--surface:#222221;--ink:#f0efec;--ink2:#c3c2b7;--muted:#898781;--line:#34332f;--accent:#7ea2ea;
--better:#0ca30c;--okbg:#17301a;--warnbg:#3a2e10;--failbg:#3b1b1b;--betterbg:#142a16;--infobg:#2a2926}}
:root[data-theme="dark"]{--bg:#1a1a19;--surface:#222221;--ink:#f0efec;--ink2:#c3c2b7;--muted:#898781;--line:#34332f;--accent:#7ea2ea;
--better:#0ca30c;--okbg:#17301a;--warnbg:#3a2e10;--failbg:#3b1b1b;--betterbg:#142a16;--infobg:#2a2926}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--ink);font:14px/1.45 -apple-system,system-ui,Segoe UI,Roboto,sans-serif}
main{max-width:1180px;margin:0 auto;padding:24px 16px 64px}h1{font-size:22px;margin:0 0 4px}h2{font-size:17px;margin:32px 0 10px;padding-top:12px;border-top:1px solid var(--line)}
.sub{color:var(--ink2);margin:0 0 14px}.verdict{display:inline-flex;align-items:center;gap:10px;padding:10px 16px;border-radius:10px;font-size:18px;font-weight:600;margin:10px 0}
.v-ok{background:var(--okbg)}.v-warn{background:var(--warnbg)}.v-fail{background:var(--failbg)}.v-nobase{background:var(--infobg)}
.badge{display:inline-flex;align-items:center;gap:6px;padding:2px 8px;border-radius:999px;white-space:nowrap;font-size:12.5px}
.s-ok{background:var(--okbg)}.s-warn{background:var(--warnbg)}.s-fail{background:var(--failbg)}.s-better{background:var(--betterbg)}.s-info,.s-nobase,.s-na{background:var(--infobg);color:var(--ink2)}
table{border-collapse:collapse;width:100%;background:var(--surface);border:1px solid var(--line);border-radius:10px;overflow:hidden}th,td{padding:7px 10px;text-align:left;vertical-align:top;border-top:1px solid var(--line)}
th{color:var(--muted);font-weight:500;font-size:12px;text-transform:uppercase;letter-spacing:.03em;border-top:0}td.num,th.num{text-align:right;font-variant-numeric:tabular-nums;white-space:nowrap}
td .mad{color:var(--muted);font-size:12px}details summary{cursor:pointer;list-style:none}details summary::-webkit-details-marker{display:none}details summary::after{content:" ⓘ";color:var(--muted);font-size:12px}
details div{color:var(--ink2);font-size:12.5px;margin-top:6px;line-height:1.4}details code{font-size:12px;background:var(--infobg);padding:1px 4px;border-radius:4px}
.wrap{overflow-x:auto}.notes li,.reasons li{margin:3px 0}.reasons{padding-left:20px}.muted{color:var(--muted)}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:16px}
svg.spark{display:block}.delta-up{color:var(--fail)}.delta-down{color:var(--better)}.small{font-size:12.5px}
"""


def esc(x) -> str:
    return html.escape(str(x)) if x is not None else "—"


def sparkline(points: list[float | None], better: str) -> str:
    vals = [p for p in points if p is not None]
    if len(vals) < 2:
        return ""
    w, h, pad = 120, 28, 3
    lo, hi = min(vals), max(vals)
    span = (hi - lo) or 1
    coords = []
    for i, p in enumerate(points):
        if p is None:
            continue
        x = pad + i * (w - 2 * pad) / max(len(points) - 1, 1)
        y = h - pad - (p - lo) / span * (h - 2 * pad)
        coords.append((x, y))
    path = " ".join(f"{x:.1f},{y:.1f}" for x, y in coords)
    lx, ly = coords[-1]
    return (f'<svg class="spark" width="{w}" height="{h}" viewBox="0 0 {w} {h}" aria-label="история: {len(vals)} прогонов">'
            f'<polyline fill="none" stroke="var(--muted)" stroke-width="1.5" points="{path}"/>'
            f'<circle cx="{lx:.1f}" cy="{ly:.1f}" r="3" fill="var(--accent)"/></svg>')


def html_table(headers: list[str], rows: list[list[str]], numeric: set[int] = frozenset()) -> str:
    head = "".join(f'<th class="{"num" if i in numeric else ""}">{h}</th>' for i, h in enumerate(headers))
    body = "".join("<tr>" + "".join(f'<td class="{"num" if i in numeric else ""}">{c}</td>' for i, c in enumerate(r)) + "</tr>" for r in rows)
    return f'<div class="wrap"><table><thead><tr>{head}</tr></thead><tbody>{body}</tbody></table></div>'


def metric_rows_html(grows: list[dict], history: list[dict]) -> str:
    out = []
    for r in grows:
        spec, unit = r["spec"], r["spec"]["unit"]
        cur = fmt(r["cur"]["value"], unit) if r["cur"] else "—"
        if r["cur"] and r["cur"].get("mad") is not None:
            cur += f' <span class="mad">±{fmt(r["cur"]["mad"], unit)} · n={r["cur"]["n"]}</span>'
        base = fmt(r["base"]["value"], unit) if r["base"] else "—"
        if r["delta"] is None:
            delta = "—"
        else:
            cls = "" if spec["better"] == "none" or r["delta"] == 0 else ("delta-up" if r["status"] in ("fail", "warn") else "delta-down" if r["status"] == "better" else "")
            delta = f'<span class="{cls}">{fmt(r["delta"], unit, True)}</span>' + (f' <span class="muted">{fmt_pct(r["pct"])}</span>' if r["pct"] is not None else "")
        si, sl = STATUS[r["status"]]
        reason = f'<div class="small muted">{esc(r["reason"])}</div>' if r["reason"] else ""
        definition = (f'<details><summary>{esc(spec["title"])}</summary><div><b>Источник:</b> <code>{esc(spec["source"])}</code><br>'
                      f'<b>Формула:</b> {esc(spec["formula"])}<br><b>Лучше:</b> {"меньше" if spec["better"] == "lower" else "больше" if spec["better"] == "higher" else "справочно"}; '
                      f'<b>пороги:</b> {esc(threshold_text(spec, unit))}'
                      + (f'<br><b>Шум:</b> ±{fmt(r["noise"], unit)} (2×MAD)' if r["noise"] else "") + f'<br><code>{r["id"]}</code></div></details>')
        points = [h["metrics"].get(r["id"]) for h in history]
        out.append([definition, base, cur, delta, f'<span class="badge s-{r["status"]}">{si} {sl}</span>{reason}', sparkline(points, spec["better"])])
    return html_table(["Метрика", "База", "Текущее", "Δ", "Статус", "История"], out, {1, 2, 3})


def details_delta_table(cur_list, base_list, key: str, value_key: str, unit: str, title: str) -> str:
    if not cur_list:
        return ""
    base_map = {e[key]: e[value_key] for e in (base_list or [])}
    rows = []
    for e in cur_list:
        bv = base_map.get(e[key])
        d = e[value_key] - bv if bv is not None else None
        rows.append([esc(e[key]), fmt(bv, unit), fmt(e[value_key], unit), fmt(d, unit, True) if d is not None else "—"])
    return f"<h3>{title}</h3>" + html_table(["", "База", "Текущее", "Δ"], rows, {1, 2, 3})


def html_report(results: dict, baseline: dict | None, rows: list[dict], registry: dict, history: list[dict]) -> str:
    meta, det = results["meta"], results["details"]
    bdet = baseline["details"] if baseline else {}
    verdict = overall(rows, baseline is not None)
    vi, vl = STATUS[verdict]
    title = f"perf-bench {meta.get('sha7')}"
    parts = [f"<!doctype html><html lang=\"ru\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
             f"<title>{esc(title)}</title><style>{CSS}</style></head><body><main>"]
    parts.append(f"<h1>perf-bench: {esc(meta.get('kind'))}{' · ' + esc(meta['label']) if meta.get('label') else ''} — {esc(meta.get('sha7'))} ({esc(meta.get('branch'))})</h1>")
    parts.append(f'<p class="sub">{esc(meta.get("created"))} · сборка androidApp debug · {esc(env_line(results))}'
                 f'{" · ⚠️ грязное дерево" if meta.get("dirty") == "1" else ""}</p>')
    parts.append(f'<div class="verdict v-{verdict if verdict != "ok" else "ok"}">{vi} {esc(vl.upper())}</div>')
    if baseline:
        bm = baseline["meta"]
        parts.append(f'<p class="sub">База: {esc(bm.get("sha7"))} ({esc(bm.get("branch"))}) {esc(bm.get("created"))}{" · " + esc(bm["label"]) if bm.get("label") else ""}</p>')
    else:
        parts.append('<p class="sub">Базы нет — значения без сравнения. Снять базу: <code>bench.sh baseline</code>.</p>')
    problems = [r for r in rows if r["status"] in ("fail", "warn")]
    if problems:
        parts.append('<ul class="reasons">' + "".join(
            f'<li>{STATUS[r["status"]][0]} <b>{esc(problem_title(r))}</b>: {esc(problem_values(r))} — {esc(r["reason"])}</li>' for r in problems) + "</ul>")
    if results.get("notes"):
        parts.append('<ul class="notes small">' + "".join(f"<li>⚠️ {esc(n)}</li>" for n in results["notes"]) + "</ul>")

    for g in ordered_groups(rows):
        grows = [r for r in rows if group_of(r["id"]) == g]
        parts.append(f"<h2>{esc(group_title(g, registry, results))}</h2>" + metric_rows_html(grows, history))

    parts.append("<h2>Детали</h2><div class=\"grid\">")
    parts.append("<div>" + details_delta_table(det.get("static.apk.breakdown"), bdet.get("static.apk.breakdown"), "group", "bytes", "B", "APK по типам содержимого") + "</div>")
    parts.append("<div>" + details_delta_table(det.get("static.dex.modules", [])[:20], bdet.get("static.dex.modules"), "module", "methods", "count", "DEX: методов по модулям (топ-20)") + "</div>")
    parts.append("<div>" + details_delta_table(det.get("static.code.modules", [])[:20], bdet.get("static.code.modules"), "module", "lines", "count", "Строк Kotlin по модулям (топ-20)") + "</div>")
    compose = det.get("static.compose.modules")
    if compose:
        parts.append("<div>" + details_delta_table(compose[:20], bdet.get("static.compose.modules"), "module", "unstable_classes", "count", "Compose: unstable-классов по модулям (топ-20)") + "</div>")
        parts.append("<div>" + details_delta_table([e for e in compose if e["skippable_pct"] is not None][:20], bdet.get("static.compose.modules"), "module", "skippable_pct", "%", "Compose: доля skippable по модулям") + "</div>")
    detekt = det.get("static.detekt", {})
    if detekt.get("by_rule"):
        parts.append("<div>" + details_delta_table(detekt["by_rule"][:20], bdet.get("static.detekt", {}).get("by_rule"), "rule", "count", "count", f"detekt по правилам (отчётов {detekt['reports']}, возраст {detekt.get('oldest_age_min', '?')} мин)") + "</div>")
    parts.append("</div>")

    samples = det.get("android.startup.samples")
    if samples:
        keys = ["id", "launch_state", "ttid_ms", "ttfc_ms", "ttfd_ms", "frames_total", "janky_pct", "frame_p95_ms", "idle_frames", "skipped_frames", "pss_kb", "java_heap_kb", "activities", "cpu_ms", "main_thread_cpu_ms", "gc_count", "log_errors"]
        parts.append("<h3>Android: замеры старта по одному</h3>" + html_table(keys, [[esc(s.get(k)) for k in keys] for s in samples], set(range(2, len(keys)))))
        warm = det.get("android.startup.warmups")
        if warm:
            parts.append(f'<p class="small muted">Прогрев (не учитывается): {esc(", ".join(f"{w["id"]}: {w["launch_state"]} TTID {w["ttid_ms"]} TTFC {w["ttfc_ms"]}" for w in warm))}</p>')
    ios_samples = det.get("ios.startup.samples")
    if ios_samples:
        keys = ["id", "ttfc_ms", "rss_kb", "cpu_ms", "log_errors", "crashes"]
        parts.append("<h3>iOS: замеры старта по одному</h3>" + html_table(keys, [[esc(s.get(k)) for k in keys] for s in ios_samples], set(range(1, len(keys)))))
    errors = det.get("android.startup.error_lines")
    if errors:
        parts.append("<h3>Ошибки в логе за старт</h3>" + html_table(["×", "Строка"], [[str(e["count"]), f"<code>{esc(e['line'])}</code>"] for e in errors], {0}))
    for name, title in det.get("features", {}).items():
        for platform in ("android", "ios"):
            reps = det.get(f"{platform}.feature.{name}.repeats")
            if not reps:
                continue
            keys = [k for k in ("id", "flow_ms", "flow_rc", "process_restarted", "frames_total", "janky_pct", "frame_p95_ms", "idle_frames", "pss_after_kb", "activities", "rss_after_kb", "cpu_ms", "main_thread_cpu_ms", "gc_count", "log_errors", "crashes") if k in reps[0]]
            parts.append(f"<h3>{esc(platform)}: сценарий {esc(name)} — {esc(title)}, повторы</h3>" + html_table(keys, [[esc(r.get(k)) for k in keys] for r in reps], set(range(1, len(keys)))))
        ferr = det.get(f"android.feature.{name}.error_lines")
        if ferr:
            parts.append(f"<h4>Ошибки в логе за сценарий {esc(name)}</h4>" + html_table(["×", "Строка"], [[str(e["count"]), f"<code>{esc(e['line'])}</code>"] for e in ferr], {0}))
    slices = det.get("android.trace.top_slices")
    if slices:
        bsl = {e["name"]: e["ms"] for e in bdet.get("android.trace.top_slices", [])}
        parts.append("<h3>Perfetto: самые долгие слайсы главного потока</h3>" + html_table(["Слайс", "База", "Текущее"],
            [[f"<code>{esc(e['name'])}</code>", fmt(bsl.get(e["name"]), "ms"), fmt(e["ms"], "ms")] for e in slices], {1, 2})
            + '<p class="small muted">Трейс: raw/android/trace/startup.pftrace — открыть в ui.perfetto.dev для разбора.</p>')
    comps = det.get("android.trace.home_composables")
    if comps:
        bc = {e["name"]: e["ms"] for e in bdet.get("android.trace.home_composables", [])}
        parts.append("<h3>Начальная композиция главной: composable по inclusive-времени в самом долгом кадре</h3>" + html_table(["Composable", "Глубина", "База", "Текущее"],
            [[f"<code>{esc(e['name'])}</code>", str(e["depth"]), fmt(bc.get(e["name"]), "ms"), fmt(e["ms"], "ms")] for e in comps], {1, 2, 3})
            + '<p class="small muted">Вложенные composable входят во время родителя. Трейс: raw/android/trace/compose.pftrace.</p>')
    rec = det.get("android.trace.recomposed")
    if rec:
        parts.append("<h4>Самые частые рекомпозиции за 9 с после открытия главной</h4>" + html_table(["Composable", "Вызовов", "Суммарно"],
            [[f"<code>{esc(e['name'])}</code>", str(e["count"]), fmt(e["ms"], "ms")] for e in rec], {1, 2}))
    monkey = det.get("android.stability.monkey")
    if monkey:
        parts.append(f'<h3>Стабильность</h3><p class="small">monkey: seed {esc(monkey.get("seed"))}, событий {esc(monkey.get("events"))}, throttle {esc(monkey.get("throttle_ms"))} ms'
                     + ("<br>" + "<br>".join(f"<code>{esc(l)}</code>" for l in monkey.get("crash_excerpt", [])) if monkey.get("crash_excerpt") else "") + "</p>")
    offline = det.get("android.stability.offline")
    if offline:
        parts.append(f'<p class="small">старт без сети: показан «{esc(offline.get("offline_shown"))}» за {esc(offline.get("offline_screen_ms"))} ms; восстановление после включения сети: {esc(offline.get("recover_ms"))} ms</p>')

    rule = registry.get("verdict_rule", {})
    parts.append("<h2>Как выносится вердикт</h2><ul class=\"small\">" + "".join(f"<li><b>{esc(k)}:</b> {esc(v)}</li>" for k, v in rule.items()) + "</ul>")
    parts.append(f'<p class="small muted">Определения метрик — в колонке «Метрика» (ⓘ) и в metrics-registry.json. Сырые дампы — в raw/ рядом с отчётом. results.json — машиночитаемый итог.</p>')
    parts.append("</main></body></html>")
    return "".join(parts)


# --- реестр таблицей -----------------------------------------------------------------------


def registry_md(registry: dict) -> str:
    out = ["| id | Метрика | Ед. | Лучше | Источник | Формула | Пороги |", "|---|---|---|---|---|---|---|"]
    for mid, spec in registry["metrics"].items():
        better = {"lower": "меньше", "higher": "больше", "none": "—"}[spec["better"]]
        out.append(f"| `{mid}` | {spec['title']}{' (N замеров)' if spec.get('sampled') else ''} | {spec['unit']} | {better} | `{spec['source']}` | {spec['formula']} | {threshold_text(spec, spec['unit'])} |")
    return "\n".join(out) + "\n"


# --- main ------------------------------------------------------------------------------------


def load_history(path: Path) -> list[dict]:
    if not path.exists():
        return []
    # Повторный отчёт одного прогона дописывает строку заново — берём последнюю запись на run_id.
    by_run: dict[str, dict] = {}
    for line in path.read_text().splitlines():
        if line.strip():
            try:
                entry = json.loads(line)
            except json.JSONDecodeError:
                continue
            by_run.pop(entry.get("run_id"), None)
            by_run[entry.get("run_id")] = entry
    return list(by_run.values())


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("run_dir", nargs="?")
    parser.add_argument("--baseline")
    parser.add_argument("--registry", required=True)
    parser.add_argument("--history")
    parser.add_argument("--no-history", action="store_true")
    parser.add_argument("--registry-md", action="store_true")
    args = parser.parse_args()
    registry = json.loads(Path(args.registry).read_text())
    if args.registry_md:
        print(registry_md(registry), end="")
        return
    run_dir = Path(args.run_dir)
    results = json.loads((run_dir / "results.json").read_text())
    baseline = json.loads(Path(args.baseline).read_text()) if args.baseline else None
    if baseline and baseline["meta"].get("run_id") == results["meta"].get("run_id"):
        baseline = None
    rows = build_rows(registry, results, baseline)

    history = load_history(Path(args.history)) if args.history else []
    entry = {
        "run_id": results["meta"].get("run_id"), "created": results["meta"].get("created"), "sha7": results["meta"].get("sha7"),
        "branch": results["meta"].get("branch"), "kind": results["meta"].get("kind"), "label": results["meta"].get("label", ""),
        "metrics": {k: v.get("value") for k, v in results["metrics"].items()},
    }
    history = [h for h in history if h.get("run_id") != entry["run_id"]] + [entry]
    if args.history and not args.no_history:
        Path(args.history).parent.mkdir(parents=True, exist_ok=True)
        with Path(args.history).open("a") as f:
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
    history = history[-12:]

    html_path = run_dir / "report.html"
    html_path.write_text(html_report(results, baseline, rows, registry, history))
    (run_dir / "report.md").write_text(md_report(results, baseline, rows, registry, html_path))
    verdict = overall(rows, baseline is not None)
    print(f"verdict: {STATUS[verdict][0]} {STATUS[verdict][1]} — {run_dir / 'report.md'}")


if __name__ == "__main__":
    main()
