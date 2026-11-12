// «Устройства системы» (SystemDevicesScreen) и «Приживление устройств» (PairingScreen).
// Периферия сигнализации: метки, реле, замки капота, датчики. Только Bluetooth.
// Устройства системы: блок опрашивается по типам (полный проход ≈5 с, затем по кругу) — группы
// добавляются в порядке каталога снизу, список не прыгает. Живые данные — тик 2 с.
// Приживление: таблица слотов, обновление раз в 2 с; режим приживления ≈2 мин с инструкцией.
// Адаптив: <760 — одна колонка + нижние листы; ≥760 — список | панель справа.
// Строка устройства — 3 варианта (A строка · B карточка · C статус крупно), режим приживления —
// 2 варианта (card — закреплённая карточка над списком · step — отдельный шаг).

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpText, CmpIcon, CmpCircularProgressIndicator, CmpLinearProgressIndicator, CmpSnackbar, CmpTextField } = DS;
const PdTextBtn = ({ style, ...p }) => (DS.CmpTextButton ? <DS.CmpTextButton {...p} style={style} /> : <CmpOutlinedButton {...p} style={style} />);
const { useState, useEffect, useRef } = React;

(function injectStyles() {
  if (document.getElementById("pd-anim")) return;
  const s = document.createElement("style");
  s.id = "pd-anim";
  s.textContent =
    // Предупреждающий тон — в ДС нет warning-роли; предложение токена (см. changelog).
    ":root{--pd-warn:#8A5300;--pd-warn-container:#FBE4C4;--on-pd-warn-container:#2E1A00}" +
    "[data-theme=dark]{--pd-warn:#F2B455;--pd-warn-container:#4A3410;--on-pd-warn-container:#FFE2B8}" +
    ".pd-svg svg{width:100%;height:100%;display:block}" +
    ".pd-ic svg [stroke]{vector-effect:non-scaling-stroke;stroke-width:var(--pd-sw,1.8px)}" +
    ".pd-scheme svg{width:100%;height:100%;display:block}" +
    "@keyframes pdShimmer{from{background-position:200% 0}to{background-position:-200% 0}}" +
    "@keyframes pdUp{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:none}}" +
    "@keyframes pdIn{from{opacity:0;transform:translateY(-6px)}to{opacity:1;transform:none}}" +
    "@keyframes pdSheet{from{transform:translateY(40px);opacity:.4}to{transform:none;opacity:1}}" +
    "@keyframes pdPulse{0%,100%{opacity:1}50%{opacity:.35}}" +
    "@keyframes pdRing{0%{box-shadow:0 0 0 0 color-mix(in srgb,var(--primary) 45%,transparent)}100%{box-shadow:0 0 0 10px transparent}}" +
    "@keyframes pdFlash{0%{opacity:.5}100%{opacity:0}}" +
    "@media (prefers-reduced-motion: reduce){.pd-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Каталог типов ────────────────────────────────────────────────────────────
// icon — файл из icons/ (легаси, currentColor); mi — Material-символ там, где легаси-иконки нет.
const TYPES = {
  token:    { grp: "Метки", name: "Метка", icon: "token", near: true, batt: true, scheme: ["bt_760_1"], flash: true },
  keyfob:   { grp: "Брелоки", name: "Брелок", icon: "keychain_watch", near: true, scheme: ["d_030_1", "control_device_1"] },
  watch:    { grp: "Часы", name: "Часы", mi: "watch", near: true, scheme: ["watch_2_1"] },
  bracelet: { grp: "Браслеты", name: "Браслет", mi: "watch", near: true },
  relay:    { grp: "Реле", name: "Радиореле", icon: "relay", batt: true, scheme: ["btr_1"] },
  hood:     { grp: "Замки капота", name: "Замок капота", icon: "hood_lock", scheme: ["hood_lock_1", "hood_lock_2", "hood_lock_3"] },
  rhm:      { grp: "RHM / сирены", name: "Сирена RHM", icon: "rhm_siren", scheme: ["rhm_03_1"] },
  dms:      { grp: "Датчики двери", name: "Датчик двери", icon: "dms", batt: true, scheme: ["door_module_1"], flash: true },
  tpms:     { grp: "Датчики шин (TPMS)", name: "Датчик шины", icon: "tpms", batt: true, addr: true, paired: true },
  fuel:     { grp: "Датчики топлива", name: "Датчик топлива", icon: "fuel_sensor", batt: true },
  pir:      { grp: "PIR-датчики", name: "PIR-датчик", icon: "pir_sensor", batt: true, scheme: ["ps_331_1", "ps_332_1"] },
  vs:       { grp: "VS", name: "VS", icon: "vs", scheme: ["vs_1"] },
  nav:      { grp: "GPS / NAV", name: "GPS-модуль", icon: "nav_09", scheme: ["gps_1", "nav_09_1", "nav_10_1", "nav_035_1"] },
  di:       { grp: "DI-04", name: "DI-04", icon: "di", scheme: ["di_04_1"] },
  bt02:     { grp: "BT-02", name: "BT-02", icon: "bt_02", scheme: ["bt_01_1"] },
  rfm:      { grp: "RF-модуль", name: "RF-модуль", icon: "rf_module", scheme: ["rfm_1"] },
};
const ORDER = Object.keys(TYPES);

// Инструкции. Пока есть только текст BTR-101 из ТЗ; остальные — по выгрузке text_device_manual_pairing_*.
const STEPS = {
  relay: { model: "BTR-101", steps: ["Подключите провод 1 к массе автомобиля.", "Соедините провода 3 и 4 и подайте на них питание 12 В.", "После успешной записи радиореле провод 3 отключите и заизолируйте."] },
};

// ── Статусы и тона ───────────────────────────────────────────────────────────
const ST = {
  near:     { label: "В зоне", tone: "ok", icon: "check_circle" },
  far:      { label: "Вне зоны", tone: "warn", icon: "near_me_disabled" },
  online:   { label: "На связи", tone: "ok", icon: "check_circle" },
  offline:  { label: "Нет связи", tone: "err", icon: "link_off" },
  paired:   { label: "Приживлено", tone: "neutral", icon: "link" },
  s_online: { label: "На связи", tone: "ok", icon: "check_circle" },
  s_offline:{ label: "Не на связи", tone: "warn", icon: "link_off" },
  s_paired: { label: "Приживлено", tone: "ok", icon: "link" },
  free:     { label: "Свободный слот", tone: "neutral", icon: "add" },
  waiting:  { label: "Ожидает устройство…", tone: "accent", icon: "sync" },
  deleting: { label: "Удаляется…", tone: "neutral", icon: "sync" },
};
const TONE = {
  ok:      { bg: "color-mix(in srgb, var(--success) 22%, var(--surface-container-high))", fg: "var(--on-surface)", dot: "var(--success)", row: "transparent" },
  warn:    { bg: "var(--pd-warn-container)", fg: "var(--on-pd-warn-container)", dot: "var(--pd-warn)", row: "color-mix(in srgb, var(--pd-warn-container) 55%, transparent)" },
  err:     { bg: "var(--error-container)", fg: "var(--on-error-container)", dot: "var(--error)", row: "color-mix(in srgb, var(--error-container) 70%, transparent)" },
  neutral: { bg: "var(--surface-container-highest)", fg: "var(--on-surface)", dot: "var(--outline)", row: "transparent" },
  accent:  { bg: "var(--primary-container)", fg: "var(--on-primary-container)", dot: "var(--primary)", row: "color-mix(in srgb, var(--primary-container) 60%, transparent)" },
};
const F = "var(--font-sans)";
const M = "var(--font-mono)";
const num = (v, d = 2) => v.toFixed(d).replace(".", ",");
const mmss = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;
const sigClass = (r) => (r >= -60 ? 4 : r >= -75 ? 3 : r >= -85 ? 2 : 1);
const SIG_NAME = { 4: "Отличный", 3: "Хороший", 2: "Слабый", 1: "Очень слабый" };
const battLevel = (pct) => (pct >= 75 ? 4 : pct >= 50 ? 3 : pct >= 25 ? 2 : 1);
const title = (d) => `${TYPES[d.type].name} ${d.n}`;
const needsAttention = (d) => d.st === "offline" || d.st === "far" || d.low || (d.rssi != null && sigClass(d.rssi) === 1);
const H48 = { height: 48 };
const BIG = { height: 56, fontSize: 16 };

// ── Примитивы ────────────────────────────────────────────────────────────────
const SV = {};
function svgText(p) { if (!SV[p]) SV[p] = fetch((window.PD_BASE || "") + p + "?v=5").then((r) => (r.ok ? r.text() : "")).catch(() => ""); return SV[p]; }
function Svg({ src, className = "pd-svg", style }) {
  const [h, setH] = useState("");
  useEffect(() => { let on = true; svgText(src).then((t) => on && setH(t)); return () => { on = false; }; }, [src]);
  return <span className={className} aria-hidden="true" style={{ display: "block", ...style }} dangerouslySetInnerHTML={{ __html: h }} />;
}

// Иконка типа. state: on | off (нет связи — тон + бейдж) | free (свободный слот — приглушённо, пунктир).
function DevIcon({ type, size = 56, state = "on", tone }) {
  const t = TYPES[type] || {};
  const off = state === "off", free = state === "free";
  const fg = free ? "var(--on-surface-variant)" : off ? "var(--on-surface-variant)" : "var(--primary)";
  const ic = Math.round(size * 0.78);
  return (
    <span style={{ position: "relative", width: size, height: size, flex: "0 0 auto", borderRadius: size * 0.3, display: "grid", placeItems: "center", color: fg, background: free ? "transparent" : tone ? TONE[tone].bg : "var(--surface-container-high)", border: free ? "2px dashed var(--outline)" : "none", boxSizing: "border-box", opacity: free ? 0.75 : 1 }}>
      {t.icon ? <Svg src={"icons/" + t.icon + ".svg"} className="pd-svg pd-ic" style={{ width: ic, height: ic }} /> : <CmpIcon icon={t.mi || "help"} size={Math.round(size * 0.5)} color={fg} />}
      {off && <span style={{ position: "absolute", right: -4, bottom: -4, width: 24, height: 24, borderRadius: 999, background: "var(--error)", color: "var(--on-error)", display: "grid", placeItems: "center", boxShadow: "0 0 0 2px var(--surface)" }}><CmpIcon icon="link_off" size={16} color="var(--on-error)" /></span>}
    </span>
  );
}

function StatusPill({ st, big }) {
  const s = ST[st]; const t = TONE[s.tone];
  const spin = st === "waiting" || st === "deleting";
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 8, height: big ? 36 : 32, padding: "0 12px 0 10px", borderRadius: 999, background: t.bg, color: t.fg, fontFamily: F, fontSize: big ? 16 : 15, fontWeight: 600, whiteSpace: "nowrap", flex: "0 0 auto", boxSizing: "border-box" }}>
      {spin ? <span style={{ width: 16, height: 16, display: "grid" }}><CmpCircularProgressIndicator size={16} strokeWidth={2.5} progress={null} color={t.fg} /></span>
        : <span className={s.tone === "err" ? "pd-anim" : undefined} style={{ width: 10, height: 10, borderRadius: 99, background: t.dot, animation: s.tone === "err" ? "pdPulse 1.6s ease-in-out infinite" : "none" }} />}
      {s.label}
    </span>
  );
}

function SignalBars({ rssi, size = 24, label, cls }) {
  const c = sigClass(rssi);
  const col = c >= 3 ? "var(--on-surface)" : c === 2 ? "var(--pd-warn)" : "var(--error)";
  const w = Math.round(size / 5);
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 8, whiteSpace: "nowrap" }} title={SIG_NAME[c]}>
      <span aria-label={`Сигнал: ${SIG_NAME[c]}`} style={{ display: "inline-flex", alignItems: "flex-end", gap: Math.max(2, Math.round(size / 12)), height: size }}>
        {[1, 2, 3, 4].map((i) => <span key={i} style={{ width: w, height: Math.round(size * (0.25 + i * 0.1875)), borderRadius: 2, background: i <= c ? col : "var(--outline-variant)" }} />)}
      </span>
      {label !== false && <span style={{ fontFamily: F, fontSize: 15, fontWeight: 600, color: c <= 2 ? col : "var(--on-surface)", fontVariantNumeric: "tabular-nums" }}>{"\u2212" + Math.abs(rssi) + " dBm"}</span>}
      {cls && <span style={{ fontFamily: F, fontSize: 15, color: c <= 2 ? col : "var(--on-surface-variant)" }}>{SIG_NAME[c]}</span>}
    </span>
  );
}

function Battery({ v, pct, low, size = 24, label = true, cls }) {
  const lv = low ? 1 : battLevel(pct);
  const col = low ? "var(--error)" : "var(--on-surface)";
  const h = Math.round(size * 0.58), w = Math.round(size * 1.05);
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 8, whiteSpace: "nowrap" }}>
      <span aria-label={low ? "Низкий заряд" : `Заряд ${lv} из 4`} style={{ display: "inline-flex", alignItems: "center" }}>
        <span style={{ width: w, height: h, borderRadius: 4, border: `2px solid ${col}`, boxSizing: "border-box", padding: 2, display: "flex", gap: 1.5 }}>
          {[1, 2, 3, 4].map((i) => <span key={i} style={{ flex: 1, borderRadius: 1, background: i <= lv ? col : "transparent" }} />)}
        </span>
        <span style={{ width: 3, height: h * 0.45, background: col, borderRadius: "0 2px 2px 0" }} />
      </span>
      {label && <span style={{ fontFamily: F, fontSize: 15, fontWeight: 600, color: col, fontVariantNumeric: "tabular-nums" }}>{num(v)} В</span>}
      {(low || cls) && <span style={{ fontFamily: F, fontSize: 15, fontWeight: low ? 700 : 400, color: low ? "var(--error)" : "var(--on-surface-variant)" }}>{low ? "Низкий заряд" : `${pct} %`}</span>}
    </span>
  );
}

function Chip({ children, tone }) {
  const t = tone ? TONE[tone] : null;
  return <span style={{ display: "inline-flex", alignItems: "center", height: 30, padding: "0 10px", borderRadius: 8, background: t ? t.bg : "transparent", border: t ? "none" : "1px solid var(--outline)", color: t ? t.fg : "var(--on-surface)", fontFamily: F, fontSize: 14, fontWeight: 500, whiteSpace: "nowrap", boxSizing: "border-box" }}>{children}</span>;
}
// Метка этапа («Фаза 2», «Опционально», «Позже») — пунктир, чтобы не путать с данными.
function PhaseTag({ children = "Фаза 2" }) {
  return <span style={{ display: "inline-flex", alignItems: "center", height: 24, padding: "0 8px", borderRadius: 6, border: "1.5px dashed var(--tertiary)", color: "var(--tertiary)", fontFamily: F, fontSize: 12, fontWeight: 700, letterSpacing: ".02em", whiteSpace: "nowrap" }}>{children}</span>;
}
const Addr = ({ a, size = 13 }) => <span style={{ fontFamily: M, fontSize: size, letterSpacing: ".04em", color: "var(--on-surface-variant)" }}>{a}</span>;

function Skel({ h = 20, w = "100%", r = 10, style }) {
  return <div className="pd-anim" style={{ height: h, width: w, borderRadius: r, background: "linear-gradient(90deg, var(--surface-container-high) 0%, var(--surface-container-highest) 50%, var(--surface-container-high) 100%)", backgroundSize: "200% 100%", animation: "pdShimmer 1.6s linear infinite", ...style }} />;
}
function useWidth(ref) {
  const [w, setW] = useState(412);
  useEffect(() => {
    if (!ref.current) return;
    const ro = new ResizeObserver(([e]) => setW(e.contentRect.width));
    ro.observe(ref.current); return () => ro.disconnect();
  }, []);
  return w;
}
function Press({ onClick, children, style, selected, label }) {
  const [h, setH] = useState(false);
  return (
    <div role={onClick ? "button" : undefined} tabIndex={onClick ? 0 : undefined} aria-label={label} onClick={onClick}
      onKeyDown={(e) => { if (onClick && (e.key === "Enter" || e.key === " ")) { e.preventDefault(); onClick(); } }}
      onMouseEnter={() => setH(true)} onMouseLeave={() => setH(false)}
      style={{ position: "relative", cursor: onClick ? "pointer" : "default", outline: "none", ...style, boxShadow: selected ? "inset 0 0 0 2px var(--primary)" : style && style.boxShadow }}>
      {onClick && <span style={{ position: "absolute", inset: 0, borderRadius: "inherit", background: "var(--on-surface)", opacity: h ? 0.06 : 0, pointerEvents: "none", transition: "opacity .15s" }} />}
      {children}
    </div>
  );
}
function GroupHead({ children, right }) {
  return (
    <div style={{ display: "flex", alignItems: "baseline", gap: 12, padding: "18px 4px 8px" }}>
      <span style={{ flex: 1, fontFamily: F, fontSize: 15, fontWeight: 700, letterSpacing: ".02em", color: "var(--on-surface-variant)", textTransform: "uppercase" }}>{children}</span>
      {right && <span style={{ fontFamily: F, fontSize: 14, color: "var(--on-surface-variant)" }}>{right}</span>}
    </div>
  );
}

// ── Данные (демо) ────────────────────────────────────────────────────────────
function demoDevices() {
  return [
    { id: "t1", type: "token", n: 1, addr: "A1B2C3D4E5F6", st: "near", rssi: -58, v: 3.02, pct: 92, flags: [] },
    { id: "t2", type: "token", n: 2, addr: "A1B2C3D4E5F7", st: "far", rssi: -87, v: 2.41, pct: 10, low: true, flags: ["Мигает"] },
    { id: "r1", type: "relay", n: 1, addr: "0C7D19A4B2E1", st: "online", rssi: -67, v: 2.95, pct: 70, metrics: [["Состояние", "Заблокировано"]], flags: [] },
    { id: "h1", type: "hood", n: 1, addr: "5E21AA0F9C34", st: "online", rssi: -72, metrics: [["Замок", "Закрыт"], ["Капот", "Закрыт"], ["Температура", "+18 °C"], ["Срабатываний", "124"]], flags: [] },
    { id: "d1", type: "dms", n: 1, addr: "7F3302C1D8E0", st: "offline", seen: "Не отвечает 4 мин", metrics: [["Температура", "—"], ["Напряжение", "—"]] },
    { id: "f1", type: "fuel", n: 1, addr: "1A9E44B07C25", st: "online", rssi: -63, v: 3.6, pct: 81, metrics: [["Уровень", "42 л · 64 %"], ["Температура", "+19 °C"]], flags: [] },
    { id: "p1", type: "tpms", n: 1, addr: "3B0019F2", st: "paired", v: 3.0, pct: 88, metrics: [["Давление", "2,3 бар"], ["Температура", "+21 °C"]] },
    { id: "p2", type: "tpms", n: 2, addr: "3B0019F7", st: "paired", v: 2.98, pct: 85, metrics: [["Давление", "2,2 бар"], ["Температура", "+20 °C"]] },
  ];
}
const metricLine = (d) => (d.metrics || []).filter(([, v]) => v !== "—").map(([k, v]) => (k === "Состояние" || k === "Замок" ? v : `${k}: ${v}`));

// ── Строка устройства: 3 варианта ────────────────────────────────────────────
function DeviceRow({ d, variant = "A", onClick, selected, flash }) {
  const t = ST[d.st]; const tone = TONE[t.tone];
  const off = d.st === "offline";
  const bad = needsAttention(d);
  const flags = [...(d.flags || [])];
  const ml = metricLine(d);
  const indicators = (
    <>
      {d.rssi != null && !off && <SignalBars rssi={d.rssi} size={20} />}
      {TYPES[d.type].batt && d.v != null && !off && <Battery v={d.v} pct={d.pct} low={d.low} size={22} />}
      {off && d.seen && <span style={{ fontFamily: F, fontSize: 15, fontWeight: 600, color: "var(--on-error-container)" }}>{d.seen}</span>}
    </>
  );
  const fl = flash ? <span key={flash} className="pd-anim" style={{ position: "absolute", inset: 0, borderRadius: "inherit", background: "var(--primary)", animation: "pdFlash 1.2s ease-out both", pointerEvents: "none" }} /> : null;

  if (variant === "B") {
    const tiles = [];
    if (d.rssi != null && !off) tiles.push(["Сигнал", <SignalBars rssi={d.rssi} size={20} />]);
    if (TYPES[d.type].batt && d.v != null && !off) tiles.push(["Батарея", <Battery v={d.v} pct={d.pct} low={d.low} size={22} />]);
    if (!off) (d.metrics || []).forEach(([k, v]) => tiles.push([k, <span style={{ fontFamily: F, fontSize: 17, fontWeight: 600, color: "var(--on-surface)" }}>{v}</span>]));
    return (
      <Press onClick={onClick} selected={selected} label={title(d)} style={{ borderRadius: 20, background: bad ? tone.row : "var(--surface-container-low)", border: "1px solid " + (bad ? "transparent" : "var(--outline-variant)"), padding: 14, display: "flex", flexDirection: "column", gap: 12 }}>
        {fl}
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <DevIcon type={d.type} size={52} state={off ? "off" : "on"} />
          <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
            <span style={{ fontFamily: F, fontSize: 18, fontWeight: 600, color: "var(--on-surface)" }}>{title(d)}</span>
            <Addr a={d.addr} />
          </div>
          <StatusPill st={d.st} />
        </div>
        {(tiles.length > 0 || off) && (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(150px, 1fr))", gap: 8 }}>
            {off ? <div style={{ gridColumn: "1 / -1", fontFamily: F, fontSize: 16, color: "var(--on-error-container)", fontWeight: 600 }}>{d.seen} · проверьте питание и расстояние до блока</div>
              : tiles.map(([k, v], i) => (
                <div key={i} style={{ borderRadius: 12, background: "var(--surface-container-high)", padding: "8px 12px", display: "flex", flexDirection: "column", gap: 4, minHeight: 56, justifyContent: "center" }}>
                  <span style={{ fontFamily: F, fontSize: 13, color: "var(--on-surface-variant)" }}>{k}</span>{v}
                </div>))}
          </div>
        )}
        {flags.length > 0 && <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>{flags.map((f) => <Chip key={f} tone="accent">{f}</Chip>)}</div>}
      </Press>
    );
  }

  if (variant === "C") {
    const sub = [title(d), ...ml].join(" · ");
    return (
      <Press onClick={onClick} selected={selected} label={title(d)} style={{ borderRadius: 18, background: bad ? tone.row : "transparent", padding: "12px 10px", display: "flex", alignItems: "center", gap: 14, minHeight: 80, boxSizing: "border-box" }}>
        {fl}
        <DevIcon type={d.type} size={56} state={off ? "off" : "on"} tone={t.tone === "neutral" ? undefined : t.tone} />
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 4 }}>
          <span style={{ display: "flex", alignItems: "center", gap: 8, fontFamily: F, fontSize: 20, fontWeight: 700, color: t.tone === "err" ? "var(--on-error-container)" : t.tone === "warn" ? "var(--on-pd-warn-container)" : "var(--on-surface)" }}>
            <span style={{ width: 10, height: 10, borderRadius: 99, background: tone.dot, flex: "0 0 auto" }} />{t.label}
          </span>
          <span style={{ fontFamily: F, fontSize: 15, color: "var(--on-surface)", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{sub}</span>
          <span style={{ display: "flex", alignItems: "center", gap: 14, flexWrap: "wrap" }}>{indicators}{flags.map((f) => <Chip key={f} tone="accent">{f}</Chip>)}</span>
        </div>
        {onClick && <CmpIcon icon="chevron_right" size={24} color="var(--on-surface-variant)" />}
      </Press>
    );
  }

  // A — строка: иконка, заголовок + адрес, статус справа; индикаторы и показатели второй строкой.
  return (
    <Press onClick={onClick} selected={selected} label={title(d)} style={{ borderRadius: 18, background: bad ? tone.row : "transparent", padding: "12px 10px", display: "flex", gap: 14, minHeight: 72, boxSizing: "border-box" }}>
      {fl}
      <DevIcon type={d.type} size={52} state={off ? "off" : "on"} />
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 6 }}>
        <div style={{ display: "flex", alignItems: "flex-start", gap: 8 }}>
          <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
            <span style={{ fontFamily: F, fontSize: 18, fontWeight: 600, color: "var(--on-surface)" }}>{title(d)}</span>
            <Addr a={d.addr} />
          </div>
          <StatusPill st={d.st} />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 14, flexWrap: "wrap", rowGap: 6 }}>
          {indicators}
          {!off && ml.map((m) => <span key={m} style={{ fontFamily: F, fontSize: 15, color: "var(--on-surface)" }}>{m}</span>)}
          {flags.map((f) => <Chip key={f} tone="accent">{f}</Chip>)}
        </div>
      </div>
    </Press>
  );
}

// ── Подробнее (лист / панель) ────────────────────────────────────────────────
function DetailBody({ d, phase2, onFlash, onFw, flashing, live }) {
  const t = TYPES[d.type]; const off = d.st === "offline";
  const Row = ({ k, children }) => (
    <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 52, borderBottom: "1px solid var(--outline-variant)" }}>
      <span style={{ flex: "0 0 40%", fontFamily: F, fontSize: 16, color: "var(--on-surface-variant)" }}>{k}</span>
      <span style={{ flex: 1, minWidth: 0, fontFamily: F, fontSize: 17, fontWeight: 600, color: "var(--on-surface)", display: "flex", justifyContent: "flex-end", textAlign: "right" }}>{children}</span>
    </div>
  );
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
        <DevIcon type={d.type} size={72} state={off ? "off" : "on"} />
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 6, alignItems: "flex-start" }}>
          <CmpText variant="headlineSmall">{title(d)}</CmpText>
          <StatusPill st={d.st} big />
        </div>
      </div>
      {off && <div style={{ borderRadius: 16, background: "var(--error-container)", color: "var(--on-error-container)", padding: 14, display: "flex", gap: 12, alignItems: "flex-start" }}>
        <CmpIcon icon="link_off" size={24} color="var(--on-error-container)" />
        <span style={{ fontFamily: F, fontSize: 16, lineHeight: 1.4 }}><b>{d.seen}.</b> Устройство приживлено, но не отвечает. Проверьте питание устройства и расстояние до блока.</span>
      </div>}
      <div>
        <Row k="Тип">{t.name}</Row>
        <Row k="Номер">{d.n}</Row>
        <Row k="Адрес"><span style={{ fontFamily: M, fontSize: 16, letterSpacing: ".04em" }}>{d.addr}</span></Row>
        {d.rssi != null && !off && <Row k="Сигнал"><SignalBars rssi={d.rssi} size={22} cls /></Row>}
        {t.batt && d.v != null && !off && <Row k="Батарея"><Battery v={d.v} pct={d.pct} low={d.low} size={24} cls /></Row>}
        {(d.metrics || []).map(([k, v]) => <Row key={k} k={k}>{v}</Row>)}
        {(d.flags || []).map((f) => <Row key={f} k="Событие">{f}</Row>)}
      </div>
      <span style={{ display: "flex", alignItems: "center", gap: 8, fontFamily: F, fontSize: 14, color: "var(--on-surface-variant)" }}>
        <span key={live} className="pd-anim" style={{ width: 8, height: 8, borderRadius: 9, background: "var(--success)", animation: "pdPulse .8s ease-out 1" }} />Обновляется каждые 2 с
      </span>
      {!t.paired && (
        <div style={{ display: "flex", flexDirection: "column", gap: 8, paddingTop: 4 }}>
          {t.flash && <CmpOutlinedButton text={flashing ? "Моргает…" : "Поморгать"} icon="flare" fullWidth disabled={off || flashing} style={BIG} onClick={onFlash} />}
          <CmpButton text="Обновить" icon="system_update" fullWidth disabled={off} style={BIG} onClick={onFw} />
          <span style={{ fontFamily: F, fontSize: 14, lineHeight: 1.4, color: "var(--on-surface-variant)" }}>{off ? "Недоступно: устройство не на связи." : (t.flash ? "«Поморгать» — устройство моргнёт светодиодом, чтобы его найти. " : "") + "«Обновить» — устройство перейдёт в режим прошивки, сессия с блоком закроется."}</span>
        </div>
      )}
    </div>
  );
}

// ── Оверлеи ──────────────────────────────────────────────────────────────────
function Sheet({ children, onClose, title: ttl, maxH = "88%" }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 8, display: "flex", flexDirection: "column", justifyContent: "flex-end" }}>
      <div onClick={onClose} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)" }} />
      <div className="pd-anim" style={{ position: "relative", maxHeight: maxH, display: "flex", flexDirection: "column", background: "var(--surface-container-low)", borderRadius: "28px 28px 0 0", animation: "pdSheet .22s ease both" }}>
        <div style={{ display: "flex", justifyContent: "center", padding: "10px 0 4px" }}><span style={{ width: 36, height: 4, borderRadius: 4, background: "var(--outline)" }} /></div>
        <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "0 8px 0 20px", minHeight: 48 }}>
          <span style={{ flex: 1, fontFamily: F, fontSize: 15, fontWeight: 700, color: "var(--on-surface-variant)" }}>{ttl}</span>
          <CmpIconButton icon="close" contentDescription="Закрыть" onClick={onClose} />
        </div>
        <div style={{ overflowY: "auto", padding: "0 20px 24px" }}>{children}</div>
      </div>
    </div>
  );
}
function Modal({ icon, tone = "primary", title: ttl, text, children, actions, onDismiss, busy }) {
  const ib = tone === "error" ? "var(--error-container)" : tone === "warn" ? "var(--pd-warn-container)" : tone === "ok" ? "color-mix(in srgb, var(--success) 28%, var(--surface-container-high))" : "var(--primary-container)";
  const ifg = tone === "error" ? "var(--on-error-container)" : tone === "warn" ? "var(--on-pd-warn-container)" : tone === "ok" ? "var(--on-surface)" : "var(--on-primary-container)";
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 9, display: "grid", placeItems: "center", padding: 20 }}>
      <div onClick={busy ? undefined : onDismiss} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)" }} />
      <div className="pd-anim" style={{ position: "relative", width: "100%", maxWidth: 420, boxSizing: "border-box", background: "var(--surface-container-high)", borderRadius: 28, padding: 24, display: "flex", flexDirection: "column", gap: 16, animation: "pdUp .22s ease both" }}>
        {busy ? <div style={{ alignSelf: "center" }}><CmpCircularProgressIndicator size={56} strokeWidth={5} progress={null} /></div>
          : icon && <div style={{ alignSelf: "center", width: 56, height: 56, borderRadius: 999, background: ib, color: ifg, display: "grid", placeItems: "center" }}><CmpIcon icon={icon} size={30} color={ifg} /></div>}
        <CmpText variant="headlineSmall" align="center">{ttl}</CmpText>
        {text && <CmpText variant="bodyLarge" color="var(--on-surface)" align="center">{text}</CmpText>}
        {children}
        {actions && <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 4 }}>{actions}</div>}
      </div>
    </div>
  );
}
const DangerBtn = (p) => <CmpButton {...p} fullWidth style={{ ...BIG, background: "var(--error)", color: "var(--on-error)" }} />;
function Snack({ s, bottom = 16, onAction }) {
  if (!s) return null;
  return (
    <div key={s.id} className="pd-anim" style={{ position: "absolute", left: 12, right: 12, bottom, zIndex: 7, display: "flex", justifyContent: "center", animation: "pdUp .2s ease both" }}>
      <CmpSnackbar message={s.text} actionLabel={s.action} onActionClick={onAction} style={{ maxWidth: 560, width: "100%" }} />
    </div>
  );
}
function useSnack() {
  const [s, set] = useState(null);
  const tm = useRef();
  const show = (text, action) => { clearTimeout(tm.current); set({ text, action, id: Date.now() }); tm.current = setTimeout(() => set(null), 3500); };
  useEffect(() => () => clearTimeout(tm.current), []);
  return [s, show, () => set(null)];
}
function AppBar({ ttl, sub, onBack, actions }) {
  return <CmpTopAppBar title={ttl} subtitle={sub} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} actions={actions} />;
}

// ── Шапка «Базовый блок / BLE-модуль» (вариант, под вопросом) ───────────────
function BlockHeader({ wide }) {
  const Cell = ({ k, v, sub }) => (
    <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
      <span style={{ fontFamily: F, fontSize: 13, color: "var(--on-surface-variant)" }}>{k}</span>
      <span style={{ fontFamily: F, fontSize: 17, fontWeight: 600, color: "var(--on-surface)" }}>{v}</span>
      <span style={{ fontFamily: F, fontSize: 14, color: "var(--on-surface-variant)" }}>{sub}</span>
    </div>
  );
  return (
    <div style={{ borderRadius: 20, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", padding: 14, display: "flex", flexDirection: wide ? "row" : "column", gap: 12, alignItems: wide ? "center" : "stretch" }}>
      <div style={{ display: "flex", gap: 16, flex: 1 }}>
        <Cell k="Базовый блок" v="Pandora DX-4GS" sub="Прошивка 3.57" />
        <Cell k="BLE-модуль" v="Версия 1.14" sub="Доступна 1.16" />
      </div>
    </div>
  );
}

// ═════════════════════════════════════════════════════════════════════════════
// Экран «Устройства системы»
// ═════════════════════════════════════════════════════════════════════════════
const POLL_MS = 330; // шаг опроса одного типа; 16 типов ≈ 5,3 с

function SystemDevicesScreen({ preset = "U3", variant = "A", phase2 = false, header = true, response = "ok", freeze = false, onBack, onOpenPairing, hasPairing = true, splitAt, panelWidth }) {
  const all = React.useMemo(() => {
    const d = demoDevices();
    if (preset === "U5") return [];
    if (preset === "U6") { const x = d.find((z) => z.id === "r1"); x.st = "offline"; x.seen = "Не отвечает 40 с"; delete x.rssi; }
    return d;
  }, [preset]);
  const initPoll = preset === "U1" ? 0 : preset === "U2" ? 6 : ORDER.length;
  const [poll, setPoll] = useState(initPoll);
  const [passes, setPasses] = useState(initPoll >= ORDER.length ? 1 : 0);
  const [devs, setDevs] = useState(all);
  const [sel, setSel] = useState(preset === "U4" || preset === "U4t" || preset === "U7" ? (preset === "U4t" ? "p1" : "t2") : preset === "U6" ? "r1" : null);
  const [sheet, setSheet] = useState(preset === "U4" || preset === "U4t" || preset === "U7");
  const [fw, setFw] = useState(preset === "U7" ? { st: "confirm" } : null);
  const [flashing, setFlashing] = useState(false);
  const [hl, setHl] = useState(preset === "U6" ? { id: "r1", k: 1 } : null);
  const [live, setLive] = useState(0);
  const [snack, showSnack] = useSnack();
  const root = useRef(); const scroller = useRef(); const rowRefs = useRef({});
  const w = useWidth(root);
  const wide = w >= (splitAt || 760);

  useEffect(() => { setDevs(all); }, [all]);
  // Опрос по типам → затем по кругу.
  useEffect(() => {
    if (freeze) return;
    const id = setInterval(() => setPoll((p) => { if (p >= ORDER.length) return p; if (p + 1 >= ORDER.length) setPasses((n) => n + 1); return p + 1; }), POLL_MS);
    return () => clearInterval(id);
  }, [freeze]);
  // Живые данные: тик 2 с.
  useEffect(() => {
    if (freeze) return;
    const id = setInterval(() => {
      setLive((n) => n + 1);
      setDevs((ds) => ds.map((d) => (d.rssi == null ? d : { ...d, rssi: Math.max(-95, Math.min(-50, d.rssi + Math.round((Math.random() - 0.5) * 4))) })));
    }, 2000);
    return () => clearInterval(id);
  }, [freeze]);

  const polledTypes = new Set(ORDER.slice(0, poll));
  const visible = devs.filter((d) => polledTypes.has(d.type));
  const groups = ORDER.filter((k) => visible.some((d) => d.type === k)).map((k) => ({ k, items: visible.filter((d) => d.type === k) }));
  const done = passes > 0;
  const online = visible.filter((d) => d.st !== "offline").length;
  const attn = visible.filter(needsAttention);
  const cur = devs.find((d) => d.id === sel);
  const selDev = wide ? cur || (done ? attn[0] || visible[0] : null) : cur;

  const open = (d) => { setSel(d.id); if (!wide) setSheet(true); };
  const goProblem = () => {
    const d = attn.find((x) => x.st === "offline") || attn[0]; if (!d) return;
    const el = rowRefs.current[d.id];
    if (el && scroller.current) scroller.current.scrollTo({ top: el.offsetTop - 80, behavior: "smooth" });
    setHl({ id: d.id, k: Date.now() }); if (wide) setSel(d.id);
  };
  const doFlash = () => { setFlashing(true); showSnack(`${title(selDev)} моргает светодиодом`); setTimeout(() => setFlashing(false), 3000); };
  // «Обновить»: подтверждение → устройство в режим прошивки → сессия закрывается → экран поиска устройств.
  const fwConfirm = () => { setFw({ st: "wait" }); if (freeze) return; setTimeout(() => { if (response !== "ok") { setFw({ st: "fail" }); return; } setFw({ st: "closing" }); setTimeout(() => window.psGo && window.psGo("device-select"), 1400); }, 2000); };
  useEffect(() => { if (preset === "U7w") setFw({ st: "wait" }); }, []);

  const pollName = poll < ORDER.length ? TYPES[ORDER[poll]].name : null;
  const status = (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      {header && <BlockHeader wide={wide && !panelWidth} />}
      {!done ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 8, padding: "4px 2px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <CmpCircularProgressIndicator size={20} strokeWidth={2.5} progress={null} />
            <span style={{ flex: 1, fontFamily: F, fontSize: 17, fontWeight: 600, color: "var(--on-surface)" }}>Опрос блока…</span>
            <span style={{ fontFamily: F, fontSize: 15, color: "var(--on-surface-variant)", fontVariantNumeric: "tabular-nums", whiteSpace: "nowrap" }}>{poll} из {ORDER.length}</span>
          </div>
          <CmpLinearProgressIndicator progress={poll / ORDER.length} height={6} />
        </div>
      ) : visible.length > 0 && (
        <Press onClick={attn.length ? goProblem : undefined} label="Сводка" style={{ borderRadius: 18, padding: "12px 14px", background: attn.length ? (attn.some((d) => d.st === "offline") ? "var(--error-container)" : "var(--pd-warn-container)") : "var(--surface-container-low)", display: "flex", alignItems: "center", gap: 12, minHeight: 64, boxSizing: "border-box" }}>
          <CmpIcon icon={attn.length ? "warning" : "check_circle"} size={28} color={attn.length ? (attn.some((d) => d.st === "offline") ? "var(--on-error-container)" : "var(--on-pd-warn-container)") : "var(--success)"} />
          <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
            <span style={{ fontFamily: F, fontSize: 17, fontWeight: 700, color: attn.length ? (attn.some((d) => d.st === "offline") ? "var(--on-error-container)" : "var(--on-pd-warn-container)") : "var(--on-surface)" }}>
              {attn.length ? `${attn.length} ${attn.length === 1 ? "требует" : "требуют"} внимания` : "Все устройства на связи"}
            </span>
            <span style={{ fontFamily: F, fontSize: 15, color: attn.length ? "inherit" : "var(--on-surface-variant)" }}>{visible.length} устройств · {online} на связи</span>
          </div>
          {attn.length > 0 && <span style={{ fontFamily: F, fontSize: 15, fontWeight: 700, color: "inherit", display: "flex", alignItems: "center", gap: 2 }}>Показать<CmpIcon icon="chevron_right" size={22} /></span>}
          <span key={live} className="pd-anim" title="Обновлено" style={{ position: "absolute", top: 10, right: 10, width: 6, height: 6, borderRadius: 9, background: "var(--success)", animation: "pdPulse .8s ease-out 1" }} />
        </Press>
      )}
    </div>
  );

  const list = (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {groups.map((g) => (
        <div key={g.k} className="pd-anim" style={{ animation: "pdIn .3s ease both" }}>
          <GroupHead right={g.items.length > 1 ? g.items.length : null}>{TYPES[g.k].grp}</GroupHead>
          <div style={{ display: "flex", flexDirection: "column", gap: variant === "B" ? 10 : 4 }}>
            {g.items.map((d) => (
              <div key={d.id} ref={(el) => (rowRefs.current[d.id] = el)}>
                <DeviceRow d={d} variant={variant} onClick={() => open(d)} selected={wide && selDev && selDev.id === d.id} flash={hl && hl.id === d.id ? hl.k : null} />
              </div>
            ))}
          </div>
        </div>
      ))}
      {!done && (
        <div style={{ display: "flex", flexDirection: "column", gap: 10, paddingTop: 18 }} aria-label="Опрашиваются остальные типы">
          {(groups.length ? [0] : [0, 1, 2]).map((i) => (
            <div key={i} style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              <Skel h={14} w={120} />
              <div style={{ display: "flex", gap: 14, alignItems: "center" }}><Skel h={52} w={52} r={16} /><div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 8 }}><Skel h={18} w="60%" /><Skel h={14} w="40%" /></div></div>
            </div>
          ))}
          {pollName && <span style={{ fontFamily: F, fontSize: 15, color: "var(--on-surface-variant)" }}>Опрашиваем: {pollName}…</span>}
        </div>
      )}
      {done && visible.length === 0 && (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: 16, padding: "56px 16px" }}>
          <span style={{ width: 112, height: 112, borderRadius: 32, background: "var(--surface-container-high)", color: "var(--on-surface-variant)", display: "grid", placeItems: "center" }}><Svg src="icons/menu_peripheral_devices.svg" className="pd-svg pd-ic" style={{ width: 72, height: 72, "--pd-sw": "2.4px" }} /></span>
          <CmpText variant="titleLarge">К блоку не приживлено ни одного устройства</CmpText>
          {hasPairing && <CmpButton text="Приживить устройство" icon="add_link" style={BIG} onClick={onOpenPairing} />}
        </div>
      )}
    </div>
  );

  const detail = selDev && <DetailBody d={selDev} phase2={phase2} onFlash={doFlash} onFw={() => setFw({ st: "confirm" })} flashing={flashing} live={live} />;
  const barActions = hasPairing && visible.length + (done ? 0 : 1) > 0 ? (wide
    ? <CmpOutlinedButton text="Приживление" icon="add_link" style={{ ...H48, marginRight: 8 }} onClick={onOpenPairing} />
    : <CmpIconButton icon="add_link" contentDescription="Приживление устройств" onClick={onOpenPairing} />) : null;

  return (
    <div ref={root} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", overflow: "hidden" }}>
      <AppBar ttl="Устройства системы" sub="Pandora DX-4GS · Bluetooth" onBack={onBack} actions={barActions} />
      {wide ? (
        <div style={{ flex: 1, minHeight: 0, display: "flex", gap: 0 }}>
          <div ref={scroller} style={{ flex: "1 1 0", minWidth: 0, overflowY: "auto", position: "relative" }}>
            <div style={{ maxWidth: 640, margin: "0 auto", padding: "8px 24px 32px", display: "flex", flexDirection: "column", gap: 4 }}>{status}{list}</div>
          </div>
          {(done || selDev) && visible.length > 0 && (
            <div style={{ flex: "0 0 auto", width: panelWidth || (w >= 1100 ? 460 : 380), borderLeft: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", overflowY: "auto", padding: "20px 24px 32px", boxSizing: "border-box" }}>
              {detail || <span style={{ fontFamily: F, fontSize: 16, color: "var(--on-surface-variant)" }}>Выберите устройство</span>}
            </div>
          )}
        </div>
      ) : (
        <div ref={scroller} style={{ flex: 1, minHeight: 0, overflowY: "auto", position: "relative" }}>
          <div style={{ padding: "4px 16px 32px", display: "flex", flexDirection: "column", gap: 4, maxWidth: 720, margin: "0 auto" }}>{status}{list}</div>
        </div>
      )}
      {!wide && sheet && cur && <Sheet title="Подробнее" onClose={() => setSheet(false)}>{detail}</Sheet>}
      {fw && fw.st === "confirm" && (
        <Modal icon="system_update" title={`Обновить ${title(selDev)}?`} text="Устройство перейдёт в режим прошивки. Сессия с блоком закроется — откроется поиск устройств, где нужно выбрать это устройство для прошивки." onDismiss={() => setFw(null)}
          actions={<><CmpButton text="Обновить" fullWidth style={BIG} onClick={fwConfirm} /><PdTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={() => setFw(null)} /></>} />
      )}
      {fw && fw.st === "wait" && <Modal busy title="Переводим в режим прошивки…" text={`${title(selDev || devs[1])}: ждём ответа устройства. Не отходите от машины.`} />}
      {fw && fw.st === "closing" && <Modal busy title="Закрываем сессию…" text="Устройство в режиме прошивки. Переходим к поиску устройств." />}
      {fw && fw.st === "fail" && (
        <Modal icon="error" tone="error" title="Не удалось перевести в режим прошивки" text="Устройство не ответило. Поднесите его ближе к блоку и повторите." onDismiss={() => setFw(null)}
          actions={<><CmpButton text="Повторить" fullWidth style={BIG} onClick={fwConfirm} /><PdTextBtn text="Закрыть" style={{ ...BIG, width: "100%" }} onClick={() => setFw(null)} /></>} />
      )}
      <Snack s={snack} />
    </div>
  );
}

// ═════════════════════════════════════════════════════════════════════════════
// Экран «Приживление устройств»
// ═════════════════════════════════════════════════════════════════════════════
const SLOT_GROUPS = [
  { title: "Метки", types: ["token"] },
  { title: "Реле", types: ["relay"] },
  { title: "Датчики", types: ["dms", "pir"] },
  { title: "Датчики шин (TPMS)", types: ["tpms"] },
];
function demoSlots(allFree) {
  const s = [
    { id: "t1", type: "token", n: 1, st: "s_online", v: 3.02 }, { id: "t2", type: "token", n: 2, st: "free" }, { id: "t3", type: "token", n: 3, st: "free" },
    { id: "r1", type: "relay", n: 1, st: "s_online", v: 2.95 }, { id: "r2", type: "relay", n: 2, st: "free" },
    { id: "d1", type: "dms", n: 1, st: "s_offline", v: 2.71 }, { id: "d2", type: "dms", n: 2, st: "free" },
    { id: "pir1", type: "pir", n: 1, st: "s_online", addr: "4A21", v: 3.1 },
    { id: "p1", type: "tpms", n: 1, st: "s_paired", v: 3.0 }, { id: "p2", type: "tpms", n: 2, st: "s_paired", v: 2.98 }, { id: "p3", type: "tpms", n: 3, st: "free" }, { id: "p4", type: "tpms", n: 4, st: "free" },
  ];
  return allFree ? s.map((x) => ({ id: x.id, type: x.type, n: x.n, st: "free" })) : s;
}

function SlotRow({ s, onClick, onPair, selected, flash, wide }) {
  const free = s.st === "free";
  const busy = s.st === "waiting" || s.st === "deleting";
  const t = ST[s.st];
  const meta = s.type === "pir" && s.addr ? `Адрес ${s.addr}` : s.v != null && !free ? `${num(s.v)} В` : null;
  return (
    <Press onClick={busy ? undefined : onClick} selected={selected} label={title(s)} style={{ borderRadius: 18, padding: "10px 10px", display: "flex", alignItems: "center", gap: 14, minHeight: 76, boxSizing: "border-box", background: s.st === "waiting" ? TONE.accent.row : s.st === "s_offline" ? TONE.warn.row : "transparent", boxShadow: s.st === "waiting" ? "inset 0 0 0 2px var(--primary)" : "none" }}>
      {flash && <span key={flash} className="pd-anim" style={{ position: "absolute", inset: 0, borderRadius: "inherit", background: "var(--success)", animation: "pdFlash 1.4s ease-out both", pointerEvents: "none" }} />}
      <span className={s.st === "waiting" ? "pd-anim" : undefined} style={{ borderRadius: 16, animation: s.st === "waiting" ? "pdRing 1.4s ease-out infinite" : "none" }}>
        <DevIcon type={s.type} size={52} state={free || s.st === "waiting" ? "free" : s.st === "s_offline" ? "off" : "on"} />
      </span>
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 6, alignItems: "flex-start" }}>
        <span style={{ fontFamily: F, fontSize: 18, fontWeight: 600, color: free ? "var(--on-surface-variant)" : "var(--on-surface)" }}>{title(s)}</span>
        <span style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
          <StatusPill st={s.st} />
          {meta && <span style={{ fontFamily: s.type === "pir" ? M : F, fontSize: 15, fontWeight: 600, color: "var(--on-surface)", fontVariantNumeric: "tabular-nums" }}>{meta}</span>}
        </span>
      </div>
      {free && onPair ? <CmpButton text="Приживить" icon={wide ? "add_link" : undefined} style={{ ...H48, flex: "0 0 auto", position: "relative" }} onClick={(e) => { e && e.stopPropagation && e.stopPropagation(); onPair(); }} />
        : !busy && <CmpIcon icon={wide ? "chevron_right" : "more_vert"} size={24} color="var(--on-surface-variant)" />}
    </Press>
  );
}

// Инструкция приживления: схема + шаги. collapsed — свёрнута в одну кнопку.
function Instruction({ type, collapsed, onToggle, compact, schemeIdx = 0 }) {
  const t = TYPES[type]; const st = STEPS[type];
  const scheme = t.scheme && t.scheme[schemeIdx];
  if (collapsed) return (
    <Press onClick={onToggle} label="Показать инструкцию" style={{ borderRadius: 14, minHeight: 52, padding: "0 12px", display: "flex", alignItems: "center", gap: 10, background: "var(--surface-container-high)" }}>
      <CmpIcon icon="menu_book" size={24} color="var(--primary)" />
      <span style={{ flex: 1, fontFamily: F, fontSize: 16, fontWeight: 600, color: "var(--on-surface)" }}>Инструкция{st ? ` · ${st.model}` : ""}</span>
      <CmpIcon icon="expand_more" size={24} color="var(--on-surface-variant)" />
    </Press>
  );
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {onToggle && (
        <Press onClick={onToggle} label="Свернуть инструкцию" style={{ borderRadius: 14, minHeight: 48, padding: "0 12px", display: "flex", alignItems: "center", gap: 10 }}>
          <CmpIcon icon="menu_book" size={24} color="var(--primary)" />
          <span style={{ flex: 1, fontFamily: F, fontSize: 16, fontWeight: 600, color: "var(--on-surface)" }}>Инструкция{st ? ` · ${st.model}` : ""}</span>
          <CmpIcon icon="expand_less" size={24} color="var(--on-surface-variant)" />
        </Press>
      )}
      {scheme ? (
        <div style={{ borderRadius: 16, background: "var(--surface-container-lowest)", border: "1px solid var(--outline-variant)", color: "var(--on-surface)", aspectRatio: "800 / 550", overflow: "hidden" }}>
          <Svg src={"pairing/" + scheme + ".svg"} className="pd-scheme" style={{ width: "100%", height: "100%" }} />
        </div>
      ) : (
        <div style={{ borderRadius: 16, border: "1.5px dashed var(--outline)", aspectRatio: "800 / 550", display: "grid", placeItems: "center", color: "var(--on-surface-variant)", fontFamily: M, fontSize: 13 }}>схема приживления · {t.name}</div>
      )}
      {st ? (
        <ol style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: 10 }}>
          {st.steps.map((x, i) => (
            <li key={i} style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
              <span style={{ width: 32, height: 32, borderRadius: 99, flex: "0 0 auto", background: "var(--primary)", color: "var(--on-primary)", display: "grid", placeItems: "center", fontFamily: F, fontSize: 16, fontWeight: 700 }}>{i + 1}</span>
              <span style={{ fontFamily: F, fontSize: compact ? 16 : 17, lineHeight: 1.4, color: "var(--on-surface)", paddingTop: 4, textWrap: "pretty" }}>{x}</span>
            </li>
          ))}
        </ol>
      ) : (
        <span style={{ fontFamily: F, fontSize: 15, color: "var(--on-surface-variant)" }}>Текст шагов для этого типа — из выгрузки строк старого приложения.</span>
      )}
    </div>
  );
}

function CountRing({ left, total, size = 88, tone = "primary" }) {
  return (
    <span style={{ position: "relative", width: size, height: size, flex: "0 0 auto", display: "grid", placeItems: "center" }}>
      <span style={{ position: "absolute", inset: 0 }}><CmpCircularProgressIndicator size={size} strokeWidth={size > 100 ? 8 : 6} progress={left / total} color={left <= 20 ? "var(--pd-warn)" : `var(--${tone})`} trackColor="var(--surface-container-highest)" /></span>
      <span style={{ fontFamily: F, fontSize: size > 100 ? 34 : 22, fontWeight: 700, color: "var(--on-surface)", fontVariantNumeric: "tabular-nums" }}>{mmss(left)}</span>
    </span>
  );
}
const PAIR_WARN = "Сигнализация недоступна, пока вы не приживите устройство. Режим завершится сам примерно через 2 минуты";

// Режим приживления: wait | ok | fail. layout: card | step | panel.
function PairingMode({ p, slot, layout, instrOpen, setInstrOpen, onCancel, onDone, onRetry, onInstr }) {
  const name = title(slot);
  if (p.phase === "ok" || p.phase === "fail") {
    const ok = p.phase === "ok";
    return (
      <div className="pd-anim" style={{ borderRadius: layout === "step" ? 0 : 24, background: ok ? "color-mix(in srgb, var(--success) 20%, var(--surface-container-low))" : "var(--error-container)", padding: 18, display: "flex", flexDirection: "column", gap: 14, animation: "pdUp .25s ease both", flex: layout === "step" ? 1 : "0 0 auto", justifyContent: layout === "step" ? "center" : "flex-start", alignItems: layout === "step" ? "center" : "stretch", textAlign: layout === "step" ? "center" : "left" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 14, flexDirection: layout === "step" ? "column" : "row" }}>
          <span style={{ width: layout === "step" ? 96 : 56, height: layout === "step" ? 96 : 56, borderRadius: 999, background: ok ? "var(--success)" : "var(--error)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={ok ? "check" : "timer_off"} size={layout === "step" ? 56 : 32} color={ok ? "#fff" : "var(--on-error)"} /></span>
          <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
            <span style={{ fontFamily: F, fontSize: 22, fontWeight: 700, color: ok ? "var(--on-surface)" : "var(--on-error-container)" }}>{ok ? `${name} приживлено` : `Не удалось приживить ${name}`}</span>
            <span style={{ fontFamily: F, fontSize: 16, lineHeight: 1.4, color: ok ? "var(--on-surface)" : "var(--on-error-container)" }}>{ok ? "Устройство на связи с блоком. Сигнализация снова работает." : "Время вышло: блок не увидел устройство и вышел из режима приживления."}</span>
          </div>
        </div>
        <div style={{ display: "flex", gap: 8, width: "100%", maxWidth: 480, flexDirection: "column" }}>
          {ok ? <CmpButton text="Готово" fullWidth style={BIG} onClick={onDone} />
            : <>
              <CmpButton text="Повторить" icon="refresh" fullWidth style={BIG} onClick={onRetry} />
              <CmpOutlinedButton text="Инструкция" icon="menu_book" fullWidth style={BIG} onClick={onInstr} />
              <PdTextBtn text="Закрыть" style={{ ...BIG, width: "100%" }} onClick={onDone} />
            </>}
        </div>
      </div>
    );
  }
  const warn = (
    <div style={{ borderRadius: 14, background: "var(--pd-warn-container)", color: "var(--on-pd-warn-container)", padding: "10px 12px", display: "flex", gap: 10, alignItems: "flex-start" }}>
      <CmpIcon icon="warning" size={22} color="var(--on-pd-warn-container)" />
      <span style={{ fontFamily: F, fontSize: 15, lineHeight: 1.4 }}>{PAIR_WARN}</span>
    </div>
  );
  if (layout === "step") {
    return (
      <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div style={{ flex: 1, overflowY: "auto", padding: "8px 20px 20px", display: "flex", flexDirection: "column", gap: 16, maxWidth: 640, margin: "0 auto", width: "100%", boxSizing: "border-box" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 18 }}>
            <CountRing left={p.left} total={p.total} size={112} />
            <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
              <span style={{ fontFamily: F, fontSize: 15, color: "var(--on-surface-variant)" }}>Ожидание устройства</span>
              <span style={{ fontFamily: F, fontSize: 24, fontWeight: 700, color: "var(--on-surface)" }}>{name}</span>
              <span style={{ display: "flex", alignItems: "center", gap: 8, fontFamily: F, fontSize: 15, color: "var(--primary)", fontWeight: 600 }}><CmpCircularProgressIndicator size={16} strokeWidth={2.5} progress={null} />Блок в режиме приживления</span>
            </div>
          </div>
          {warn}
          <Instruction type={slot.type} />
        </div>
        <div style={{ padding: "12px 20px 20px", borderTop: "1px solid var(--outline-variant)", display: "flex", justifyContent: "center" }}><CmpOutlinedButton text="Отмена" fullWidth style={{ ...BIG, maxWidth: 600 }} onClick={onCancel} /></div>
      </div>
    );
  }
  const panel = layout === "panel";
  return (
    <div className="pd-anim" style={{ borderRadius: panel ? 0 : 24, background: panel ? "transparent" : "var(--surface-container-low)", boxShadow: panel ? "none" : "inset 0 0 0 2px var(--primary), 0 6px 18px color-mix(in srgb, var(--shadow) 14%, transparent)", padding: panel ? 0 : 14, display: "flex", flexDirection: "column", gap: 12, animation: panel ? "none" : "pdIn .25s ease both" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
        <CountRing left={p.left} total={p.total} size={panel ? 96 : 72} />
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 4 }}>
          <span style={{ display: "flex", alignItems: "center", gap: 8, fontFamily: F, fontSize: 14, color: "var(--primary)", fontWeight: 700 }}><CmpCircularProgressIndicator size={14} strokeWidth={2.5} progress={null} />РЕЖИМ ПРИЖИВЛЕНИЯ</span>
          <span style={{ fontFamily: F, fontSize: panel ? 24 : 20, fontWeight: 700, color: "var(--on-surface)" }}>Ожидание: {name}</span>
        </div>
      </div>
      {warn}
      {panel ? <Instruction type={slot.type} /> : <Instruction type={slot.type} collapsed={!instrOpen} onToggle={() => setInstrOpen(!instrOpen)} compact />}
      <CmpOutlinedButton text="Отмена" fullWidth style={BIG} onClick={onCancel} />
    </div>
  );
}

function SlotActions({ s, onPair, onInstr, onDelete, onUpdate }) {
  const free = s.st === "free";
  const Item = ({ icon, label, sub, onClick, danger, tag }) => (
    <Press onClick={onClick} label={label} style={{ borderRadius: 16, minHeight: 64, padding: "8px 12px", display: "flex", alignItems: "center", gap: 14 }}>
      <CmpIcon icon={icon} size={26} color={danger ? "var(--error)" : "var(--primary)"} />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 2 }}>
        <span style={{ display: "flex", alignItems: "center", gap: 8, fontFamily: F, fontSize: 17, fontWeight: 600, color: danger ? "var(--error)" : "var(--on-surface)" }}>{label}{tag && <PhaseTag>{tag}</PhaseTag>}</span>
        {sub && <span style={{ fontFamily: F, fontSize: 14, color: "var(--on-surface-variant)" }}>{sub}</span>}
      </div>
    </Press>
  );
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
        <DevIcon type={s.type} size={60} state={free ? "free" : s.st === "s_offline" ? "off" : "on"} />
        <div style={{ display: "flex", flexDirection: "column", gap: 6, alignItems: "flex-start" }}>
          <CmpText variant="titleLarge">{title(s)}</CmpText>
          <StatusPill st={s.st} />
        </div>
      </div>
      <div style={{ display: "flex", flexDirection: "column" }}>
        {free ? <>
          <Item icon="add_link" label="Приживить" sub={TYPES[s.type].addr ? "Понадобится адрес датчика" : "Блок перейдёт в режим приживления на 2 минуты"} onClick={onPair} />
          <Item icon="menu_book" label="Инструкция" onClick={onInstr} />
        </> : <>
          <Item icon="menu_book" label="Инструкция" onClick={onInstr} />
          <Item icon="system_update" label="Обновить" sub="Переведёт устройство в режим прошивки" tag="Опционально" onClick={onUpdate} />
          <Item icon="delete" label="Удалить" danger onClick={onDelete} />
        </>}
      </div>
    </div>
  );
}

function AddressForm({ value, onChange, err, onSubmit, onCancel }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      <CmpText variant="bodyLarge" color="var(--on-surface)">Адрес напечатан на корпусе датчика шины.</CmpText>
      <label style={{ display: "flex", flexDirection: "column", gap: 6 }}>
        <span style={{ fontFamily: F, fontSize: 15, fontWeight: 600, color: err ? "var(--error)" : "var(--on-surface-variant)" }}>Адрес датчика</span>
        <input value={value} onChange={(e) => onChange(e.target.value.toUpperCase().replace(/[^0-9A-FА-ЯЁG-Z]/gi, "").slice(0, 8))} placeholder="3B0019F2" spellCheck={false} autoCapitalize="characters"
          style={{ height: 60, borderRadius: 12, border: `2px solid ${err ? "var(--error)" : "var(--primary)"}`, background: "var(--surface)", color: "var(--on-surface)", fontFamily: M, fontSize: 24, letterSpacing: ".18em", padding: "0 16px", outline: "none", boxSizing: "border-box", width: "100%" }} />
        <span style={{ fontFamily: F, fontSize: 14, color: err ? "var(--error)" : "var(--on-surface-variant)", fontWeight: err ? 600 : 400 }}>{err || "8 символов: цифры 0–9 и буквы A–F"}</span>
      </label>
      <Press label="Сканировать QR" style={{ borderRadius: 14, minHeight: 56, padding: "0 14px", display: "flex", alignItems: "center", gap: 12, border: "1.5px dashed var(--outline)", opacity: 0.8 }}>
        <CmpIcon icon="qr_code_scanner" size={26} color="var(--on-surface-variant)" />
        <span style={{ flex: 1, fontFamily: F, fontSize: 16, fontWeight: 600, color: "var(--on-surface-variant)" }}>Сканировать QR</span>
        <PhaseTag>Позже</PhaseTag>
      </Press>
      <CmpButton text="Приживить" icon="add_link" fullWidth disabled={!value} style={BIG} onClick={onSubmit} />
      {onCancel && <PdTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={onCancel} />}
    </div>
  );
}
const validAddr = (v) => /^[0-9A-F]{8}$/.test(v);

const PAIR_SEC = 120;
function PairingScreen({ preset = "D2", pairVariant = "card", response = "ok", freeze = false, transport = "ble", onBack, onOpenDevices, splitAt, panelWidth }) {
  const ble = transport === "ble";
  const [slots, setSlots] = useState(() => demoSlots(preset === "D3"));
  const [phase, setPhase] = useState(preset === "D1" ? "loading" : preset === "D11" ? "denied" : preset === "D12" ? "readErr" : "ready");
  const [loadStep, setLoadStep] = useState(0);
  const initPair = { D6: { slotId: "r2", left: 104, total: PAIR_SEC, phase: "wait" }, D6o: { slotId: "r2", left: 104, total: PAIR_SEC, phase: "wait" }, D7: { slotId: "r2", left: 0, total: PAIR_SEC, phase: "ok" }, D8: { slotId: "r2", left: 0, total: PAIR_SEC, phase: "fail" } }[preset] || null;
  const [pair, setPair] = useState(initPair);
  const [instrOpen, setInstrOpen] = useState(preset === "D6o");
  const [sel, setSel] = useState(preset === "D4" ? "t2" : preset === "D4o" ? "t1" : preset === "D9" ? "t1" : null);
  const [sheet, setSheet] = useState(preset === "D4" || preset === "D4o" ? "actions" : preset === "D5" || preset === "D5e" || preset === "D5f" ? "addr" : null);
  const [addr, setAddr] = useState(preset === "D5e" ? "3B0Z19" : preset === "D5f" ? "3B0019F5" : "");
  const [addrErr, setAddrErr] = useState(preset === "D5e" ? "Адрес — 8 символов: 0–9 и A–F" : null);
  const [dialog, setDialog] = useState(preset === "D9" ? { type: "delete", id: "t1" } : preset === "D10" ? { type: "pin" } : preset === "D10b" ? { type: "pinBlocked" } : preset === "D13" ? { type: "unsupported" } : null);
  const [flash, setFlash] = useState(null);
  const [snack, showSnack] = useSnack();
  const root = useRef(); const w = useWidth(root); const wide = w >= (splitAt || 760);
  const pending = useRef({});
  useEffect(() => { if (preset === "D5" || preset === "D5e" || preset === "D5f") setSel("p3"); if (preset === "D9s") { setSlots((s) => s.map((x) => (x.id === "t1" ? { ...x, st: "deleting" } : x))); } }, []);
  useEffect(() => {
    if (initPair && initPair.phase === "wait") setSlots((s) => s.map((x) => (x.id === initPair.slotId ? { ...x, st: "waiting" } : x)));
    if (initPair && initPair.phase === "ok") setSlots((s) => s.map((x) => (x.id === initPair.slotId ? { ...x, st: "s_online", v: 2.97 } : x)));
  }, []);
  // D1: вход в режим программирования → чтение таблицы.
  useEffect(() => {
    if (phase !== "loading" || freeze) return;
    const a = setTimeout(() => setLoadStep(1), 1200); const b = setTimeout(() => setPhase("ready"), 2600);
    return () => { clearTimeout(a); clearTimeout(b); };
  }, [phase, freeze]);
  // Отсчёт режима приживления; ответ устройства (демо) — через ≈8 с или по кнопке демо-панели.
  useEffect(() => {
    if (!pair || pair.phase !== "wait" || freeze) return;
    const id = setInterval(() => setPair((p) => {
      if (!p || p.phase !== "wait") return p;
      const left = p.left - 1;
      if (response === "ok" && p.total - left >= 8) return finish(p, "ok");
      if (left <= 0) return finish(p, "fail");
      return { ...p, left };
    }), 1000);
    return () => clearInterval(id);
  }, [pair && pair.phase, freeze, response]);
  useEffect(() => {
    const h = (e) => setPair((p) => (p && p.phase === "wait" ? finish(p, e.detail) : p));
    window.addEventListener("pd-demo", h); return () => window.removeEventListener("pd-demo", h);
  });
  function finish(p, res) {
    setSlots((s) => s.map((x) => (x.id === p.slotId ? (res === "ok" ? { ...x, st: x.type === "tpms" ? "s_paired" : "s_online", v: 2.97 } : { ...x, st: "free" }) : x)));
    if (res === "ok") setFlash({ id: p.slotId, k: Date.now() });
    return { ...p, left: 0, phase: res };
  }

  const cur = slots.find((s) => s.id === sel);
  const pairSlot = pair && slots.find((s) => s.id === pair.slotId);
  const occupied = slots.filter((s) => s.st !== "free" && s.st !== "waiting").length;

  const startPair = (s, address) => {
    if (TYPES[s.type].addr && !address) { setSel(s.id); setAddr(""); setAddrErr(null); setSheet("addr"); return; }
    setSheet(null); setSel(s.id); setInstrOpen(false);
    setSlots((all) => all.map((x) => (x.id === s.id ? { ...x, st: "waiting" } : x)));
    setPair({ slotId: s.id, left: PAIR_SEC, total: PAIR_SEC, phase: "wait" });
  };
  const cancelPair = () => { setSlots((all) => all.map((x) => (x.id === pair.slotId ? { ...x, st: "free" } : x))); setPair(null); showSnack("Режим приживления отменён"); };
  const closePair = () => setPair(null);
  const submitAddr = () => { if (!validAddr(addr)) { setAddrErr("Адрес — 8 символов: 0–9 и A–F"); return; } startPair(cur, addr); };
  const doDelete = (id) => {
    setDialog(null); setSheet(null);
    setSlots((all) => all.map((x) => (x.id === id ? { ...x, st: "deleting" } : x)));
    pending.current[id] = setTimeout(() => { setSlots((all) => all.map((x) => (x.id === id ? { id: x.id, type: x.type, n: x.n, st: "free" } : x))); showSnack("Устройство удалено"); }, freeze ? 0 : 1600);
  };
  const tapSlot = (s) => { if (pair && pair.phase === "wait") return; setSel(s.id); if (!wide) setSheet("actions"); };
  const pairing = pair && pairSlot;
  const pairWait = pairing && pair.phase === "wait";

  const groupsEl = (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {SLOT_GROUPS.map((g) => {
        const items = slots.filter((s) => g.types.includes(s.type)); if (!items.length) return null;
        const busy = items.filter((s) => s.st !== "free" && s.st !== "waiting").length;
        return (
          <div key={g.title}>
            <GroupHead right={`${busy} из ${items.length}`}>{g.title}</GroupHead>
            <div style={{ display: "flex", flexDirection: "column", gap: 4, opacity: pairWait && !wide && pairVariant === "card" ? 1 : 1 }}>
              {items.map((s) => <SlotRow key={s.id} s={s} wide={wide && !panelWidth} selected={wide && sel === s.id} flash={flash && flash.id === s.id ? flash.k : null} onClick={() => tapSlot(s)} onPair={pairWait ? null : () => startPair(s)} />)}
            </div>
          </div>
        );
      })}
    </div>
  );

  const summary = (
    <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "8px 4px 0" }}>
      <span style={{ flex: 1, fontFamily: F, fontSize: 18, fontWeight: 700, color: "var(--on-surface)" }}>Занято {occupied} из {slots.length}</span>
      <span style={{ display: "flex", alignItems: "center", gap: 6, fontFamily: F, fontSize: 14, color: "var(--on-surface-variant)" }}><span style={{ width: 8, height: 8, borderRadius: 9, background: "var(--success)" }} />Обновляется каждые 2 с</span>
    </div>
  );
  const emptyHint = occupied === 0 && !pairing && (
    <div style={{ borderRadius: 18, background: "var(--primary-container)", color: "var(--on-primary-container)", padding: 14, display: "flex", gap: 12, alignItems: "flex-start", marginTop: 8 }}>
      <CmpIcon icon="info" size={24} color="var(--on-primary-container)" />
      <span style={{ fontFamily: F, fontSize: 16, lineHeight: 1.4 }}>К блоку ещё ничего не приживлено. Выберите слот и нажмите «Приживить».</span>
    </div>
  );

  let body;
  if (phase === "loading") {
    body = (
      <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: 14, maxWidth: 720, margin: "0 auto", width: "100%", boxSizing: "border-box" }}>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={20} strokeWidth={2.5} progress={null} /><span style={{ fontFamily: F, fontSize: 17, fontWeight: 600, color: "var(--on-surface)" }}>{loadStep ? "Читаем таблицу слотов…" : "Вход в режим программирования…"}</span></div>
          <CmpLinearProgressIndicator progress={null} height={6} />
        </div>
        {[0, 1, 2, 3, 4].map((i) => <div key={i} style={{ display: "flex", gap: 14, alignItems: "center", padding: "10px 10px" }}><Skel h={52} w={52} r={16} /><div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 8 }}><Skel h={18} w="50%" /><Skel h={28} w={130} r={99} /></div></div>)}
      </div>
    );
  } else if (phase === "denied" || phase === "readErr") {
    const d = phase === "denied";
    body = (
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", gap: 14, padding: 28, maxWidth: 520, margin: "0 auto" }}>
        <span style={{ width: 88, height: 88, borderRadius: 999, background: "var(--error-container)", display: "grid", placeItems: "center" }}><CmpIcon icon={d ? "lock" : "sync_problem"} size={44} color="var(--on-error-container)" /></span>
        <CmpText variant="headlineSmall" align="center">{d ? "Блок не переходит в режим программирования" : "Не удалось прочитать таблицу слотов"}</CmpText>
        <CmpText variant="bodyLarge" align="center" color="var(--on-surface)">{d ? "Снимите с охраны и выключите зажигание" : "Блок не ответил. Проверьте, что вы рядом с машиной."}</CmpText>
        <CmpButton text="Повторить" icon="refresh" style={{ ...BIG, minWidth: 220 }} onClick={() => { setPhase("loading"); setLoadStep(0); }} />
      </div>
    );
  } else if (pairing && pairVariant === "step" && !wide) {
    body = <PairingMode p={pair} slot={pairSlot} layout="step" onCancel={cancelPair} onDone={closePair} onRetry={() => startPair(pairSlot, "x")} onInstr={() => setSheet("instr")} />;
  } else {
    const card = pairing && !wide ? (
      <div style={{ position: "sticky", top: 0, zIndex: 2, paddingTop: 8, paddingBottom: 4, background: "var(--background)" }}>
        <PairingMode p={pair} slot={pairSlot} layout="card" instrOpen={instrOpen} setInstrOpen={setInstrOpen} onCancel={cancelPair} onDone={closePair} onRetry={() => startPair(pairSlot, "x")} onInstr={() => setSheet("instr")} />
      </div>
    ) : null;
    const listCol = <>{card}{summary}{emptyHint}{groupsEl}</>;
    body = wide ? (
      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <div style={{ flex: "1 1 0", minWidth: 0, overflowY: "auto" }}><div style={{ maxWidth: 640, margin: "0 auto", padding: "4px 24px 32px" }}>{listCol}</div></div>
        <div style={{ flex: "0 0 auto", width: panelWidth || (w >= 1100 ? 480 : 380), borderLeft: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", overflowY: "auto", padding: "20px 24px 32px", boxSizing: "border-box" }}>
          {pairing ? <PairingMode p={pair} slot={pairSlot} layout="panel" onCancel={cancelPair} onDone={closePair} onRetry={() => startPair(pairSlot, "x")} onInstr={() => setSheet("instr")} />
            : sheet === "addr" && cur ? <div style={{ display: "flex", flexDirection: "column", gap: 12 }}><CmpText variant="titleLarge">{title(cur)} · адрес</CmpText><AddressForm value={addr} onChange={(v) => { setAddr(v); setAddrErr(null); }} err={addrErr} onSubmit={submitAddr} onCancel={() => setSheet(null)} /></div>
            : cur ? <div style={{ display: "flex", flexDirection: "column", gap: 20 }}><SlotActions s={cur} onPair={() => startPair(cur)} onInstr={() => setSheet("instrPanel")} onDelete={() => setDialog({ type: "delete", id: cur.id })} onUpdate={() => setDialog({ type: "unsupported" })} />{(sheet === "instrPanel" || cur.st === "free") && <Instruction type={cur.type} />}</div>
            : <span style={{ fontFamily: F, fontSize: 16, color: "var(--on-surface-variant)" }}>Выберите слот, чтобы приживить или удалить устройство</span>}
        </div>
      </div>
    ) : (
      <div style={{ flex: 1, minHeight: 0, overflowY: "auto" }}><div style={{ padding: "0 16px 32px", maxWidth: 720, margin: "0 auto" }}>{listCol}</div></div>
    );
  }

  const delSlot = dialog && dialog.type === "delete" && slots.find((s) => s.id === dialog.id);
  const stepMode = pairing && pairVariant === "step" && !wide;
  return (
    <div ref={root} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", overflow: "hidden" }}>
      {stepMode ? <CmpTopAppBar title="Приживление" subtitle={title(pairSlot)} navigationIcon={<CmpIconButton icon="close" contentDescription="Отмена" onClick={pair.phase === "wait" ? cancelPair : closePair} />} />
        : <AppBar ttl="Приживление устройств" sub={`Pandora DX-4GS · ${ble ? "Bluetooth" : "USB"}`} onBack={onBack}
          actions={!ble ? null : wide ? <CmpOutlinedButton text="Устройства системы" icon="sensors" style={{ ...H48, marginRight: 8 }} onClick={onOpenDevices} /> : <CmpIconButton icon="sensors" contentDescription="Устройства системы" onClick={onOpenDevices} />} />}
      {body}
      {!wide && sheet === "actions" && cur && <Sheet title="Слот" onClose={() => setSheet(null)}><SlotActions s={cur} onPair={() => startPair(cur)} onInstr={() => setSheet("instr")} onDelete={() => setDialog({ type: "delete", id: cur.id })} onUpdate={() => setDialog({ type: "unsupported" })} /></Sheet>}
      {!wide && sheet === "addr" && cur && <Sheet title={`${title(cur)} · адрес`} onClose={() => setSheet(null)}><AddressForm value={addr} onChange={(v) => { setAddr(v); setAddrErr(null); }} err={addrErr} onSubmit={submitAddr} /></Sheet>}
      {sheet === "instr" && (cur || pairSlot) && <Sheet title={`Инструкция · ${TYPES[(pairSlot || cur).type].name}`} onClose={() => setSheet(null)}><Instruction type={(pairSlot || cur).type} /></Sheet>}
      {delSlot && (
        <Modal icon="delete" tone="error" title={`Удалить ${title(delSlot)}?`} text="Устройство перестанет работать с сигнализацией" onDismiss={() => setDialog(null)}
          actions={<><DangerBtn text="Удалить" onClick={() => doDelete(delSlot.id)} /><PdTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={() => setDialog(null)} /></>} />
      )}
      {dialog && dialog.type === "pin" && (
        <Modal icon="pin" title="Режим программирования" text="Сохранённый сервисный PIN не подошёл. Введите PIN блока." onDismiss={() => setDialog(null)}
          actions={<><CmpButton text="Войти" fullWidth style={BIG} onClick={() => setDialog(null)} /><PdTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={() => { setDialog(null); onBack && onBack(); }} /></>}><PinBody /></Modal>
      )}
      {dialog && dialog.type === "pinBlocked" && (
        <Modal icon="lock_clock" tone="error" title="PIN заблокирован" text="Слишком много неверных попыток. Повторите через 5 минут." onDismiss={() => setDialog(null)}
          actions={<CmpButton text="Понятно" fullWidth style={BIG} onClick={() => setDialog(null)} />} />
      )}
      {dialog && dialog.type === "unsupported" && (
        <Modal icon="block" tone="warn" title="Команда не поддерживается" text="Блок не поддерживает эту команду. Обновите прошивку блока и повторите." onDismiss={() => setDialog(null)}
          actions={<CmpButton text="Понятно" fullWidth style={BIG} onClick={() => setDialog(null)} />} />
      )}
      <Snack s={snack} />
    </div>
  );
}
function PinBody() {
  const [v, setV] = useState("");
  return (
    <label style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <span style={{ fontFamily: F, fontSize: 15, fontWeight: 600, color: "var(--on-surface-variant)" }}>Сервисный PIN</span>
      <input value={v} onChange={(e) => setV(e.target.value.replace(/\D/g, "").slice(0, 6))} inputMode="numeric" placeholder="••••"
        style={{ height: 60, borderRadius: 12, border: "2px solid var(--primary)", background: "var(--surface)", color: "var(--on-surface)", fontFamily: M, fontSize: 26, letterSpacing: ".4em", textAlign: "center", outline: "none" }} />
    </label>
  );
}

// ── Демо-панель (общая для страниц) ──────────────────────────────────────────
const U_PRESETS = [["U1", "U1 Первый опрос"], ["U2", "U2 Наполняется"], ["U3", "U3 Полный список"], ["U4", "U4 Подробнее: метка"], ["U4t", "U4 Подробнее: TPMS"], ["U5", "U5 Пусто"], ["U6", "U6 Нет связи"], ["U7", "U7 Обновить"]];
const D_PRESETS = [["D1", "D1 Загрузка"], ["D2", "D2 Список"], ["D3", "D3 Все свободны"], ["D4", "D4 Свободный слот"], ["D4o", "D4 Занятый слот"], ["D5", "D5 Адрес"], ["D5e", "D5 Ошибка адреса"], ["D5f", "D5 Заполнено"], ["D6", "D6 Приживление"], ["D6o", "D6 Инструкция открыта"], ["D7", "D7 Успех"], ["D8", "D8 Не удалось"], ["D9", "D9 Удалить?"], ["D9s", "D9 Удаляется"], ["D10", "D10 PIN"], ["D10b", "D10 PIN заблокирован"], ["D11", "D11 Не пускает"], ["D12", "D12 Ошибка чтения"], ["D13", "D13 Не поддерживается"]];
function PdSeg({ items, value, onChange, wrap }) {
  return <div className={"seg" + (wrap ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
function usePdCtl() {
  const q = new URLSearchParams(location.search);
  const [dark, setDark] = useState(q.get("theme") === "dark");
  const [screen, setScreenRaw] = useState(q.get("screen") || "devices");
  const [preset, setPreset] = useState(q.get("state") || (q.get("screen") === "pairing" ? "D2" : "U1"));
  const [variant, setVariant] = useState(q.get("row") || "A");
  const [pairVariant, setPairVariant] = useState(q.get("pair") || "card");
  const [phase2, setPhase2] = useState(q.get("phase2") === "1");
  const [resp, setResp] = useState("ok");
  const [transport, setTransport] = useState(q.get("transport") || "ble");
  const [dyn, setDyn] = useState(q.get("dynamic") !== "0");
  useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const setScreen = (s) => { if (s === "devices" && transport !== "ble") setTransport("ble"); setScreenRaw(s); setPreset(s === "pairing" ? "D2" : "U1"); };
  const dev = screen === "devices";
  const bar = (
    <>
      <div className="toolbar">
        <PdSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(v) => setDark(v === "d")} />
        <PdSeg items={[["devices", "Устройства системы"], ["pairing", "Приживление устройств"]]} value={screen} onChange={setScreen} />
        {!dev && <PdSeg items={[["card", "Приживление: карточка"], ["step", "Отдельный шаг"]]} value={pairVariant} onChange={setPairVariant} />}
      </div>
      <div className="toolbar">
        {dev && <PdSeg items={[["0", "Фаза 1"], ["1", "Фаза 2: действия"]]} value={phase2 ? "1" : "0"} onChange={(v) => setPhase2(v === "1")} />}
        {dev ? <PdSeg items={[["1", "Блок с динамической таблицей"], ["0", "Без приживления"]]} value={dyn ? "1" : "0"} onChange={(v) => setDyn(v === "1")} />
          : <PdSeg items={[["ble", "Bluetooth"], ["usb", "USB"]]} value={transport} onChange={setTransport} />}
        <PdSeg items={[["ok", "Устройство отвечает"], ["fail", "Не отвечает"]]} value={resp} onChange={setResp} />
        {!dev && <div className="seg"><button onClick={() => window.dispatchEvent(new CustomEvent("pd-demo", { detail: "ok" }))}>Демо: устройство найдено</button><button onClick={() => window.dispatchEvent(new CustomEvent("pd-demo", { detail: "fail" }))}>Демо: время вышло</button></div>}
      </div>
      <div className="toolbar"><PdSeg wrap items={dev ? U_PRESETS : D_PRESETS} value={preset} onChange={setPreset} /></div>
    </>
  );
  const common = { preset, response: resp };
  const props = dev ? { ...common, variant: "A", phase2: phase2 || preset === "U7", hasPairing: dyn } : { ...common, pairVariant, transport };
  return { dark, screen, setScreen, props, bar, key: [screen, preset, pairVariant, phase2, dyn, transport].join() };
}

Object.assign(window, {
  SystemDevicesScreen, PairingScreen, usePdCtl, PdSeg,
  PD_TYPES: TYPES, PD_DevIcon: DevIcon, PD_StatusPill: StatusPill, PD_SignalBars: SignalBars, PD_Battery: Battery, PD_DeviceRow: DeviceRow,
  PD_SlotRow: SlotRow, PD_Instruction: Instruction, PD_PairingMode: PairingMode, PD_demoDevices: demoDevices, PD_demoSlots: demoSlots, PD_Svg: Svg, PD_PhaseTag: PhaseTag, PD_DetailBody: DetailBody,
  PD_U_PRESETS: U_PRESETS, PD_D_PRESETS: D_PRESETS,
});
