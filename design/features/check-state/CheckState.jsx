// CheckStateScreen — «Проверка состояния»: живое состояние сигнализации и автомобиля + команды на блок.
// Режимы: advanced (Расширенный монтаж, свободная проверка) | quick (шаг Быстрого монтажа, чек-лист).
// Только Bluetooth. Блок присылает состояние раз в 3 с — прототип имитирует это тиком 3 с:
// изменения (нажатие на зону в демо, команды) применяются на следующем тике и подсвечиваются.
// Зоны — два варианта подачи: list (группы строк с подписями) | scheme (схема кузова + плитки).
// Команды — удержание 1 с с полосой заполнения; опасные (запуск двигателя, паника) — лист подтверждения
// с удержанием 2 с. Адаптив: <760 — липкий статус + вкладки; ≥760 — две колонки (состояние | команды/чек-лист).

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpSurface, CmpText, CmpIcon, CmpCircularProgressIndicator, CmpLinearProgressIndicator, CmpTabRow, CmpSnackbar } = DS;
const CsTextBtn = ({ fullWidth, style, ...p }) => (DS.CmpTextButton ? <DS.CmpTextButton {...p} style={{ ...(fullWidth ? { width: "100%" } : null), ...style }} /> : <CmpOutlinedButton {...p} fullWidth={fullWidth} style={style} />);

(function injectStyles() {
  if (document.getElementById("cs-anim")) return;
  const s = document.createElement("style");
  s.id = "cs-anim";
  s.textContent =
    "@keyframes csFlash{0%{opacity:.42}100%{opacity:0}}" +
    "@keyframes csBlink{0%,100%{opacity:0}50%{opacity:.22}}" +
    "@keyframes csBorder{0%,100%{opacity:1}50%{opacity:.25}}" +
    "@keyframes csFill{from{width:0}to{width:100%}}" +
    "@keyframes csDot{0%,100%{transform:scale(1);opacity:1}50%{transform:scale(.6);opacity:.5}}" +
    "@keyframes csShimmer{from{background-position:200% 0}to{background-position:-200% 0}}" +
    "@keyframes csIn{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:none}}" +
    ".cs-veh svg{width:100%;height:100%;display:block}.cs-veh .vb{fill:var(--surface-container-low)}" +
    "@keyframes csAlt{0%,49.9%{opacity:1}50%,100%{opacity:0}}" +
    "@keyframes csUp{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}" +
    "@media (prefers-reduced-motion: reduce){.cs-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Каталог зон ──────────────────────────────────────────────────────────────
const V_F = { off: "Закрыта", on: "Открыта", alarm: "Тревога" };
const V_M = { off: "Закрыт", on: "Открыт", alarm: "Тревога" };
const V_ON = { off: "Выкл", on: "Вкл", alarm: "Тревога" };
const V_BRAKE = { off: "Отпущен", on: "Нажат", alarm: "Тревога" };
const V_SENS = { off: "Норма", on: "Сработал", alarm: "Тревога" };
const V_SHOCK = { off: "Норма", warn: "Сработала зона предупреждения", main: "Сработала основная зона", warn_dis: "Зона предупреждения отключена", main_dis: "Основная зона отключена" };

const ZONES = [
  { k: "door_fl", grp: "body", label: "Передняя левая дверь", short: "Пер. левая", icon: "sensor_door", vals: V_F },
  { k: "door_fr", grp: "body", label: "Передняя правая дверь", short: "Пер. правая", icon: "sensor_door", vals: V_F },
  { k: "door_rl", grp: "body", label: "Задняя левая дверь", short: "Задн. левая", icon: "sensor_door", vals: V_F },
  { k: "door_rr", grp: "body", label: "Задняя правая дверь", short: "Задн. правая", icon: "sensor_door", vals: V_F },
  { k: "hood", grp: "body", label: "Капот", short: "Капот", icon: "directions_car", vals: V_M },
  { k: "trunk", grp: "body", label: "Багажник", short: "Багажник", icon: "luggage", vals: V_M },
  { k: "ign", grp: "ctl", label: "Зажигание", icon: "key", vals: V_ON },
  { k: "brake", grp: "ctl", label: "Педаль тормоза", icon: "pause_circle", vals: V_BRAKE },
  { k: "handbrake", grp: "ctl", label: "Ручник", icon: "pan_tool", vals: V_ON },
  { k: "neutral", grp: "ctl", label: "Нейтраль / «P»", icon: "local_parking", vals: V_ON },
  { k: "lights", grp: "ctl", label: "Габариты", icon: "highlight", vals: V_ON, noAlarm: true },
  { k: "shock", grp: "sens", label: "Датчик удара", icon: "vibration", vals: V_SHOCK, cycle: ["off", "warn", "main", "off"] },
  { k: "extra", grp: "sens", label: "Доп. датчик", icon: "sensors", vals: V_SHOCK, cycle: ["off", "warn", "main", "off"] },
  { k: "motion", grp: "sens", label: "Датчик движения", icon: "directions_walk", vals: V_SENS },
  { k: "tilt", grp: "sens", label: "Датчик наклона", icon: "screen_rotation", vals: V_SENS },
];
const ZMAP = Object.fromEntries(ZONES.map((z) => [z.k, z]));
const GROUPS = [
  { k: "body", title: "Двери и кузов" },
  { k: "ctl", title: "Органы управления" },
  { k: "sens", title: "Датчики" },
];

function toneOf(v) {
  if (v === "alarm" || v === "main") return "alarm";
  if (v === "on" || v === "warn") return "active";
  if (v && v.endsWith("_dis")) return "muted";
  return "rest";
}
const isAlarm = (d) => d.guard !== "disarmed" && ZONES.some((z) => toneOf(d.z[z.k]) === "alarm");
const zVal = (k, v) => ZMAP[k].vals[v] || v;
const fmt = (n) => Number(n).toLocaleString("ru-RU");
const mmss = (s) => `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;

const TONE = {
  rest:   { row: "transparent", pill: "var(--background-surface-highlight)", pillFg: "var(--on-surface)", ic: "var(--background-surface-highlight)", icFg: "var(--on-surface-variant)", fg: "var(--on-surface)", tile: "var(--background-surface-highlight)" },
  active: { row: "color-mix(in srgb, var(--primary-container) 75%, transparent)", pill: "var(--primary)", pillFg: "var(--on-primary)", ic: "var(--primary)", icFg: "var(--on-primary)", fg: "var(--on-primary-container)", tile: "var(--primary-container)" },
  alarm:  { row: "var(--error-container)", pill: "var(--error)", pillFg: "var(--on-error)", ic: "var(--error)", icFg: "var(--on-error)", fg: "var(--on-error-container)", tile: "var(--error-container)" },
  muted:  { row: "transparent", pill: "transparent", pillFg: "var(--on-surface-variant)", ic: "var(--background-surface-highlight)", icFg: "var(--on-surface-variant)", fg: "var(--on-surface)", tile: "var(--background-surface-card)" },
};

// ── Данные блока (демо) ──────────────────────────────────────────────────────
function baseData() {
  return {
    guard: "disarmed", service: false, prog: false, event: "Снятие с охраны",
    z: { door_fl: "off", door_fr: "off", door_rl: "off", door_rr: "off", hood: "off", trunk: "off", ign: "off", brake: "off", handbrake: "on", neutral: "on", lights: "off", shock: "off", extra: "off", motion: "off", tilt: "off" },
    engine: { state: "off", auto: "enabled", left: 0, total: 600, rpm: null },
    power: { volt: 12.6, tEngine: 18, tCabin: 21, tOut: 12, fuel: 64 },
    link: { gsm: 3, gps: { fix: true, speed: 0 }, sms: true },
    can: { mileage: 84210, range: 410, cons: 7.8, washerLow: false, windows: { fl: "off", fr: "off", rl: "off", rr: "off" }, tires: null },
    ev: null, heater: null, camera: null, siren: { mode: "Вкл" }, hf: null, hours: null,
  };
}
function maxData(d) {
  d.can.tires = { fl: 2.3, fr: 2.3, rl: 2.2, rr: 2.4, spare: 2.5 };
  d.can.washerLow = true; d.can.windows.fl = "on";
  d.ev = { charge: "Медленная", soc: 78, soh: 96, tBat: 24, range: 312 };
  d.heater = { on: false, t: 18, volt: 12.5, power: 0, errors: true };
  d.camera = { installed: true, rec: true, min: 42 };
  d.hf = { mode: "Авто" }; d.hours = 1284;
  return d;
}

const TASKS = [
  { id: "t1", type: "auto", text: "Откройте водительскую дверь", zone: "door_fl" },
  { id: "t2", type: "auto", text: "Откройте пассажирскую дверь", zone: "door_fr" },
  { id: "t3", type: "auto", text: "Откройте капот", zone: "hood" },
  { id: "t4", type: "auto", text: "Откройте багажник", zone: "trunk" },
  { id: "t5", type: "auto", text: "Включите зажигание", zone: "ign" },
  { id: "t6", type: "auto", text: "Нажмите педаль тормоза", zone: "brake" },
  { id: "t7", type: "auto", text: "Затяните ручник", zone: "handbrake", from: "off" },
  { id: "t8", type: "auto", text: "Поставьте на охрану брелоком", guard: true },
  { id: "t9", type: "manual", text: "Сработала ли сирена при постановке на охрану?", options: ["Да", "Нет"] },
];
const taskMet = (t, d) => (t.guard ? d.guard !== "disarmed" : toneOf(d.z[t.zone]) === "active");

function makeInit(preset, mode) {
  const d = baseData();
  const o = { data: d, changed: {}, cmd: {}, queue: [], dialog: null, sheet: null, lost: false, staleMs: 0, loading: false, tab: null, toast: null, tasks: TASKS, ts: {}, quickProg: false };
  const done = (ids) => ids.forEach((id) => (o.ts[id] = "done"));
  TASKS.forEach((t) => (o.ts[t.id] = "pending"));
  if (mode === "quick") d.z.handbrake = "off";
  switch (preset) {
    case "S1": o.loading = true; break;
    case "S3": d.z.door_fl = "on"; d.z.brake = "on"; d.event = "Передняя левая дверь: открыта"; o.changed = { door_fl: 1, brake: 1 }; break;
    case "S4": d.guard = "armed"; d.event = "Постановка в охрану"; break;
    case "S5": d.guard = "armed"; d.z.trunk = "alarm"; d.z.shock = "main"; d.event = "Тревога: багажник"; o.changed = { trunk: 1, shock: 1 }; break;
    case "S6": d.service = true; d.event = "Режим ТО включён"; break;
    case "S6p": d.prog = true; d.event = "Режим программирования включён"; break;
    case "S7": Object.assign(d.engine, { state: "on", auto: "running", left: 582, total: 600, rpm: 820 }); d.z.ign = "on"; Object.assign(d.power, { volt: 14.2, tEngine: 64 }); d.event = "Автозапуск двигателя"; break;
    case "S8": d.power = { volt: 12.4, tEngine: null, tCabin: 20, tOut: null, fuel: null }; d.link = { gsm: -1, gps: { fix: false }, sms: null }; d.can = null; d.siren = null; d.engine.auto = "off"; break;
    case "S9": maxData(d); break;
    case "S10": o.staleMs = 14000; break;
    case "S11": o.cmd = { arm: { st: "sending" } }; o.queue = [{ cmd: "arm", at: 0 }]; break;
    case "S12": o.sheet = "start"; break;
    case "S13": o.dialog = { type: "pin" }; break;
    case "S14": d.service = true; o.dialog = { type: "exit" }; break;
    case "S15": o.lost = true; break;
    case "F2": done(["t1"].slice(0)); d.z.door_fl = "on"; o.changed = { door_fl: 1 }; d.event = "Передняя левая дверь: открыта"; o.toast = { id: "t1" }; o.tab = "checks"; break;
    case "F3": done(["t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8"]); d.guard = "armed"; d.z.handbrake = "on"; d.event = "Постановка в охрану"; o.tab = "checks"; break;
    case "F4": done(["t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8"]); o.ts.t9 = "yes"; d.z.handbrake = "on"; break;
    case "F5": done(["t1", "t2", "t3", "t4", "t5"]); o.ts.t6 = "skipped"; o.dialog = { type: "finish" }; break;
    case "F6": o.tasks = []; break;
    case "FP": d.prog = true; o.quickProg = true; break;
  }
  return o;
}

const clone = (x) => JSON.parse(JSON.stringify(x));

// Эффект команды на данные блока — применяется на ближайшем тике (как ответ блока).
const EFFECT = {
  arm: (d) => { d.guard = "armed"; d.event = "Постановка в охрану"; },
  disarm: (d) => { d.guard = "disarmed"; ZONES.forEach((z) => { if (toneOf(d.z[z.k]) === "alarm") d.z[z.k] = z.cycle ? "off" : "on"; }); d.event = "Снятие с охраны"; },
  start: (d) => { Object.assign(d.engine, { state: "on", auto: "running", left: 600, total: 600, rpm: 820 }); d.z.ign = "on"; if (d.power.volt) d.power.volt = 14.1; d.event = "Запуск двигателя"; },
  extend: (d) => { d.engine.left += 300; d.engine.total += 300; d.event = "Продление автозапуска"; },
  stop: (d) => { Object.assign(d.engine, { state: "off", auto: "enabled", left: 0, rpm: null }); d.z.ign = "off"; if (d.power.volt) d.power.volt = 12.5; d.event = "Остановка двигателя"; },
  trunk: (d) => { d.z.trunk = "on"; d.event = "Открыт багажник"; },
  heater: (d) => { d.heater.on = !d.heater.on; d.event = d.heater.on ? "Подогреватель включён" : "Подогреватель выключен"; },
  heaterReset: (d) => { d.heater.errors = false; d.event = "Ошибки подогревателя сброшены"; },
  service: (d) => { d.service = !d.service; d.event = d.service ? "Режим ТО включён" : "Режим ТО выключен"; },
  prog: (d) => { d.prog = !d.prog; d.event = d.prog ? "Режим программирования включён" : "Режим программирования выключен"; },
  panic: (d) => { d.event = "Паника"; },
  light: (d) => { d.z.lights = "on"; d.event = "Световой путь"; },
};

function commandsFor(d, mode) {
  const L = [
    { k: "arm", label: "Поставить на охрану", icon: "li:active_arm" },
    { k: "disarm", label: "Снять с охраны", icon: "li:active_disarm" },
  ];
  if (d.engine.state === "off") L.push({ k: "start", label: "Запустить двигатель", icon: "li:engine_start", danger: true });
  if (d.engine.auto === "running") L.push({ k: "extend", label: "Продлить автозапуск", icon: "li:engine_prolong" });
  if (d.engine.state !== "off") L.push({ k: "stop", label: "Остановить двигатель", icon: "li:engine_stop" });
  L.push({ k: "trunk", label: "Открыть багажник", icon: "li:open_trunk" });
  if (d.heater) L.push({ k: "heater", label: d.heater.on ? "Выключить подогреватель" : "Включить подогреватель", icon: "li:heater" });
  if (d.heater && d.heater.errors) L.push({ k: "heaterReset", label: "Сбросить ошибки подогревателя", icon: "li:clear_heater_errors" });
  L.push({ k: "service", label: d.service ? "Выключить режим ТО" : "Включить режим ТО", icon: d.service ? "li:service_mode_disable" : "li:service_mode_enable" });
  if (mode === "advanced") L.push({ k: "prog", label: d.prog ? "Выключить режим программирования" : "Включить режим программирования", icon: d.prog ? "li:program_mode_disable" : "li:program_mode" });
  L.push({ k: "panic", label: "Паника", icon: "li:alarm", danger: true });
  L.push({ k: "light", label: "Световой путь", icon: "li:light_way" });
  return L;
}
const CMD_LABEL = { arm: "Поставить на охрану", disarm: "Снять с охраны", start: "Запустить двигатель", extend: "Продлить автозапуск", stop: "Остановить двигатель", trunk: "Открыть багажник", heater: "Подогреватель", heaterReset: "Сбросить ошибки подогревателя", service: "Режим ТО", prog: "Режим программирования", panic: "Паника", light: "Световой путь" };

// ── Примитивы ────────────────────────────────────────────────────────────────
function useWidth() {
  const ref = React.useRef(null);
  const [sz, setSz] = React.useState([0, 0]);
  React.useEffect(() => {
    const el = ref.current; if (!el) return;
    const m = () => setSz([el.offsetWidth, el.offsetHeight]);
    const ro = new ResizeObserver(m); ro.observe(el); m();
    return () => ro.disconnect();
  }, []);
  return [ref, sz[0], sz[1]];
}

// ── Раскладки v2 (исследование: планшет-ландшафт, раскладные, телефон-ландшафт) ──
// low   — высота < 520 (телефон-ландшафт 915×412): статус в шапке, слева машина, справа пульт / текущее задание.
// split — ширина 600–1099 (раскладной 690×829 / 829×690, планшет-портрет): две равные панели по сгибу,
//         слева «живое» (статус, машина, показатели), справа вкладки.
// desk  — ширина ≥ 1100 (планшет-ландшафт 1280×800): три колонки — машина | зоны | команды или чек-лист.
function layoutV2(w, h) {
  if (!w) return "split";
  if (h && h < 520 && w >= 600) return "low";
  if (w >= 1100) return "desk";
  if (w >= 600) return "split";
  return "compact";
}
function StatusPill({ d, loading, stale, secs }) {
  if (loading) return <span style={{ display: "inline-flex", alignItems: "center", gap: 8, height: 40, padding: "0 14px", borderRadius: 999, background: "var(--background-surface-highlight)" }}><CmpCircularProgressIndicator size={16} strokeWidth={2.5} /><CmpText variant="labelLarge">Ждём блок…</CmpText></span>;
  const alarm = isAlarm(d); const c = HERO[alarm ? "alarm" : d.guard];
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 8, height: 40, padding: "0 14px 0 10px", borderRadius: 999, background: c.bg, color: c.fg, whiteSpace: "nowrap", flex: "0 0 auto" }}>
      <CmpIcon icon={stale ? "sync_problem" : c.icon} size={22} />
      <CmpText variant="titleSmall" weight={700} color={c.fg}>{c.title}</CmpText>
      <CmpText variant="labelMedium" color={c.fg} style={{ opacity: 0.85 }}>{stale ? `нет данных ${secs} с` : secs < 2 ? "· только что" : `· ${secs} с`}</CmpText>
    </span>
  );
}
function ZonesGrid({ d, changed, tick, onZone, min = 150 }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      {GROUPS.map((g) => (
        <div key={g.k} style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", justifyContent: "space-between", gap: 12, padding: "0 2px" }}><CmpText variant="titleMedium" weight={600}>{g.title}</CmpText><CmpText variant="labelLarge" color="var(--on-surface-variant)">{groupAside(g.k, d)}</CmpText></div>
          <div style={{ display: "grid", gridTemplateColumns: `repeat(auto-fill, minmax(${min}px, 1fr))`, gap: 8 }}>
            {ZONES.filter((z) => z.grp === g.k).map((z) => <ZoneTile key={z.k} z={z} v={d.z[z.k]} changed={!!changed[z.k]} tick={tick} onClick={onZone && (() => onZone(z.k))} />)}
          </div>
        </div>
      ))}
    </div>
  );
}
function PaneTabs({ tabs, value, onChange }) {
  const idx = Math.max(0, tabs.findIndex((t) => t.k === value));
  return <CmpTabRow tabs={tabs.map((t) => ({ title: t.title }))} selectedTabIndex={idx} onTabSelected={(i) => onChange(tabs[i].k)} />;
}

function Flash({ on, k, radius = 14, color = "var(--primary)" }) {
  if (!on) return null;
  return (
    <React.Fragment>
      <span key={k} className="cs-anim" style={{ position: "absolute", inset: 0, borderRadius: radius, background: color, animation: "csFlash 1.8s ease-out both", pointerEvents: "none" }} />
      <span style={{ position: "absolute", inset: 0, borderRadius: radius, boxShadow: `inset 0 0 0 2px ${color}`, pointerEvents: "none" }} />
    </React.Fragment>
  );
}

function NewTag() {
  return <span style={{ display: "inline-flex", alignItems: "center", height: 22, padding: "0 8px", borderRadius: 999, background: "var(--primary)", color: "var(--on-primary)", fontFamily: "var(--font-sans)", fontSize: "var(--t-additional1-size)", fontWeight: 700, letterSpacing: ".02em", whiteSpace: "nowrap" }}>Изменилось</span>;
}

function ValuePill({ tone = "rest", text, big }) {
  const t = TONE[tone];
  return (
    <span style={{ flex: "0 1 auto", maxWidth: "55%", boxSizing: "border-box", display: "inline-flex", alignItems: "center", justifyContent: "center", textAlign: "center", minHeight: big ? 36 : 32, padding: "4px 12px", borderRadius: "var(--r-card-medium-radius)", background: t.pill, color: t.pillFg, border: tone === "muted" ? "1.5px dashed var(--outline)" : "none", fontFamily: "var(--font-sans)", fontSize: big ? 16 : 15, fontWeight: 700, lineHeight: 1.25, textWrap: "pretty" }}>{text}</span>
  );
}

function Skel({ h = 20, w = "100%", r = 10, style }) {
  return <div className="cs-anim" style={{ height: h, width: w, borderRadius: r, background: "linear-gradient(90deg, var(--surface-container-high) 0%, var(--surface-container-highest) 50%, var(--surface-container-high) 100%)", backgroundSize: "200% 100%", animation: "csShimmer 1.6s linear infinite", ...style }} />;
}

function Card({ title, aside, children, pad = 12, style }) {
  return (
    <CmpSurface color="var(--background-surface-card)" radius="var(--r-card-medium-radius)" border style={{ boxSizing: "border-box", ...style }}>
      {title && (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, padding: "14px 16px 4px" }}>
          <CmpText variant="titleMedium" weight={600}>{title}</CmpText>
          {aside && <CmpText variant="labelLarge" color="var(--on-surface-variant)">{aside}</CmpText>}
        </div>
      )}
      <div style={{ padding: pad }}>{children}</div>
    </CmpSurface>
  );
}

// ── Строка зоны (вариант A) ──────────────────────────────────────────────────
function ZoneRow({ z, v, changed, tick, onClick }) {
  const tone = toneOf(v); const t = TONE[tone];
  return (
    <div role="button" tabIndex={0} onClick={onClick} onKeyDown={(e) => { if (e.key === "Enter") onClick && onClick(); }}
      style={{ position: "relative", display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "8px 10px 8px 8px", borderRadius: "var(--r-card-small-radius)", background: t.row, cursor: onClick ? "pointer" : "default", boxSizing: "border-box", transition: "background .3s" }}>
      <Flash on={changed} k={tick} color={tone === "alarm" ? "var(--error)" : "var(--primary)"} />
      <div style={{ width: 44, height: 44, borderRadius: 999, background: t.ic, color: t.icFg, display: "grid", placeItems: "center", flex: "0 0 auto", position: "relative", transition: "background .3s" }}><CmpIcon icon={z.icon} size={24} /></div>
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 4, alignItems: "flex-start", position: "relative" }}>
        <CmpText variant="titleMedium" color={t.fg} weight={tone === "rest" || tone === "muted" ? 500 : 600}>{z.label}</CmpText>
        {changed && <NewTag />}
      </div>
      <ValuePill tone={tone} text={zVal(z.k, v)} />
    </div>
  );
}

function groupAside(g, d) {
  const zs = ZONES.filter((z) => z.grp === g);
  const act = zs.filter((z) => toneOf(d.z[z.k]) !== "rest" && toneOf(d.z[z.k]) !== "muted").length;
  if (g === "body") return act ? `Открыто: ${act}` : "Всё закрыто";
  if (g === "sens") return act ? `Сработало: ${act}` : "Норма";
  return act ? `Включено: ${act}` : "";
}

function ZonesList({ d, changed, tick, onZone, wide }) {
  return (
    <div style={{ display: "grid", gridTemplateColumns: wide ? "repeat(auto-fit, minmax(320px, 1fr))" : "1fr", gap: 12, alignItems: "start" }}>
      {GROUPS.map((g) => (
        <Card key={g.k} title={g.title} aside={groupAside(g.k, d)} pad={6}>
          <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
            {ZONES.filter((z) => z.grp === g.k).map((z) => (
              <ZoneRow key={z.k} z={z} v={d.z[z.k]} changed={!!changed[z.k]} tick={tick} onClick={onZone && (() => onZone(z.k))} />
            ))}
          </div>
        </Card>
      ))}
    </div>
  );
}

// ── Вариант B: схема кузова + плитки ────────────────────────────────────────
function ZoneTile({ z, v, changed, tick, onClick }) {
  const tone = toneOf(v); const t = TONE[tone];
  return (
    <div role="button" tabIndex={0} onClick={onClick}
      style={{ position: "relative", minHeight: 88, boxSizing: "border-box", padding: 12, borderRadius: "var(--r-card-medium-radius)", background: t.tile, border: tone === "rest" ? "1px solid var(--border-highlight)" : tone === "muted" ? "1.5px dashed var(--outline)" : "1px solid transparent", display: "flex", flexDirection: "column", gap: 6, cursor: onClick ? "pointer" : "default", transition: "background .3s" }}>
      <Flash on={changed} k={tick} radius="var(--r-card-medium-radius)" color={tone === "alarm" ? "var(--error)" : "var(--primary)"} />
      <div style={{ display: "flex", alignItems: "center", gap: 8, position: "relative" }}>
        <CmpIcon icon={z.icon} size={22} color={tone === "rest" || tone === "muted" ? "var(--on-surface-variant)" : t.fg} />
        <CmpText variant="titleSmall" color={t.fg} weight={500} style={{ fontSize: "var(--t-paragraph2-size)" }}>{z.label}</CmpText>
      </div>
      <CmpText variant="titleMedium" weight={700} color={tone === "alarm" ? "var(--error)" : tone === "active" ? "var(--primary)" : "var(--on-surface)"} style={{ position: "relative", textWrap: "pretty" }}>{zVal(z.k, v)}</CmpText>
      {changed && <div style={{ position: "relative" }}><NewTag /></div>}
    </div>
  );
}

function DoorChip({ k, v, side, changed, tick, onClick }) {
  const tone = toneOf(v); const t = TONE[tone];
  return (
    <div role="button" tabIndex={0} onClick={onClick} style={{ position: "relative", minHeight: 76, boxSizing: "border-box", padding: "8px 10px", borderRadius: "var(--r-card-medium-radius)", background: tone === "rest" ? "var(--background-surface-highlight)" : t.tile, border: `2px solid ${tone === "rest" ? "transparent" : tone === "alarm" ? "var(--error)" : "var(--primary)"}`, display: "flex", flexDirection: "column", justifyContent: "center", gap: 2, textAlign: side === "left" ? "right" : side === "center" ? "center" : "left", alignItems: side === "left" ? "flex-end" : side === "center" ? "center" : "flex-start", cursor: "pointer", transition: "background .3s" }}>
      <Flash on={changed} k={tick} radius={14} color={tone === "alarm" ? "var(--error)" : "var(--primary)"} />
      <CmpText variant="labelLarge" color={t.fg} style={{ fontSize: "var(--t-additional0-size)", position: "relative", whiteSpace: "nowrap" }}>{ZMAP[k].short}</CmpText>
      <CmpText variant="titleMedium" weight={700} color={tone === "alarm" ? "var(--error)" : tone === "active" ? "var(--primary)" : "var(--on-surface)"} style={{ position: "relative" }}>{zVal(k, v)}</CmpText>
    </div>
  );
}
const VEH = {};
function vehSvg(n) { if (!VEH[n]) VEH[n] = fetch((window.CS_BASE || "") + "vehicle/" + n + ".svg").then((r) => r.text()).catch(() => ""); return VEH[n]; }
function VLayer({ n, color, anim, delay }) {
  const [h, setH] = React.useState("");
  React.useEffect(() => { let on = true; vehSvg(n).then((x) => on && setH(x)); return () => { on = false; }; }, [n]);
  return <span className="cs-veh cs-anim" aria-hidden="true" style={{ position: "absolute", inset: 0, color, transition: "color .3s", animation: anim, animationDelay: delay }} dangerouslySetInnerHTML={{ __html: h }} />;
}
const VC = { rest: "var(--on-surface-variant)", active: "var(--primary)", alarm: "var(--error)", muted: "var(--outline)" };
function vehicleLayers(d) {
  const L = [["body", "var(--on-surface-variant)"]];
  const al = isAlarm(d);
  [["top_left", "door_fl"], ["top_right", "door_fr"], ["bottom_left", "door_rl"], ["bottom_right", "door_rr"]].forEach(([p, k]) => {
    const tn = toneOf(d.z[k]); L.push([p + (tn === "rest" ? "_close" : "_open"), VC[tn]]);
  });
  ["hood", "trunk"].forEach((k) => { const tn = toneOf(d.z[k]); if (tn !== "rest") L.push([k, VC[tn]]); });
  [["ign", "ignition"], ["brake", "brake"], ["neutral", "parking"], ["lights", "light"]].forEach(([k, n]) => { const tn = toneOf(d.z[k]); if (tn !== "rest") L.push([n, VC[tn]]); });
  const sh = { warn: ["sensor_alert_zone", VC.active], main: ["sensor_main_zone", VC.alarm], warn_dis: ["sensor_alert_zone_off", VC.muted], main_dis: ["sensor_main_zone_off", VC.muted] }[d.z.shock];
  if (sh) L.push(sh);
  const ex = { warn: ["ext_sensor_alert_zone", VC.active], main: ["ext_sensor_main_zone_alarm", VC.alarm], warn_dis: ["ext_sensor_alert_zone_off", VC.muted], main_dis: ["ext_sensor_main_zone_off", VC.muted] }[d.z.extra];
  if (ex) L.push(ex);
  if (toneOf(d.z.motion) !== "rest") L.push(["moving_sens", VC[toneOf(d.z.motion)]]);
  if (toneOf(d.z.tilt) !== "rest") L.push(["angle_sensor", VC[toneOf(d.z.tilt)]]);
  L.push(d.guard === "disarmed" ? ["alarm_off", VC.rest] : ["alarm_on", al ? VC.alarm : VC.active]);
  if (d.service) L.push(["service_mode", "var(--tertiary)"]);
  if (d.prog) L.push(["progm_mode", "var(--tertiary)"]);
  if (d.heater && d.heater.on) L.push(["heater", VC.active]);
  if (d.engine.auto === "running") { L.push(["autostart_rotate_1", VC.active, "csAlt 1s linear infinite"]); L.push(["autostart_rotate_2", VC.active, "csAlt 1s linear infinite", "-.5s"]); }
  return L;
}
function Vehicle({ d, width = 150 }) {
  return (
    <div style={{ position: "relative", width, height: Math.round((width * 1000) / 522), margin: "0 auto" }}>
      {vehicleLayers(d).map(([n, c, a, dl]) => <VLayer key={n} n={n} color={c} anim={a} delay={dl} />)}
    </div>
  );
}
function BodyScheme({ d, changed, tick, onZone, carW = 132 }) {
  const p = (k) => ({ k, v: d.z[k], changed: !!changed[k], tick, onClick: onZone && (() => onZone(k)) });
  const side = { display: "flex", flexDirection: "column", justifyContent: "center", gap: 28, paddingTop: 24 };
  return (
    <Card title="Двери и кузов" aside={groupAside("body", d)} pad={14}>
      <div style={{ display: "grid", gridTemplateColumns: `minmax(0,1fr) ${carW}px minmax(0,1fr)`, columnGap: 6, rowGap: 6, alignItems: "stretch", maxWidth: 520, margin: "0 auto" }}>
        <div /><DoorChip {...p("hood")} side="center" /><div />
        <div style={side}><DoorChip {...p("door_fl")} side="left" /><DoorChip {...p("door_rl")} side="left" /></div>
        <Vehicle d={d} width={carW} />
        <div style={side}><DoorChip {...p("door_fr")} side="right" /><DoorChip {...p("door_rr")} side="right" /></div>
        <div /><DoorChip {...p("trunk")} side="center" /><div />
      </div>
    </Card>
  );
}
function ZonesScheme({ d, changed, tick, onZone, wide }) {
  const tiles = (g) => (
    <Card title={GROUPS.find((x) => x.k === g).title} aside={groupAside(g, d)} pad={12}>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(150px, 1fr))", gap: 8 }}>
        {ZONES.filter((z) => z.grp === g).map((z) => <ZoneTile key={z.k} z={z} v={d.z[z.k]} changed={!!changed[z.k]} tick={tick} onClick={onZone && (() => onZone(z.k))} />)}
      </div>
    </Card>
  );
  return (
    <div style={{ display: "grid", gridTemplateColumns: wide ? "repeat(auto-fit, minmax(340px, 1fr))" : "1fr", gap: 12, alignItems: "start" }}>
      {tiles("body")}
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>{tiles("ctl")}{tiles("sens")}</div>
    </div>
  );
}

// ── Главный статус ───────────────────────────────────────────────────────────
const HERO = {
  disarmed: { bg: "var(--background-surface-highlight)", fg: "var(--on-surface)", ib: "var(--secondary-container)", ifg: "var(--on-secondary-container)", icon: "lock_open", title: "Снято с охраны" },
  armed: { bg: "var(--primary)", fg: "var(--on-primary)", ib: "color-mix(in srgb, var(--on-primary) 18%, transparent)", ifg: "var(--on-primary)", icon: "lock", title: "На охране" },
  active: { bg: "var(--primary)", fg: "var(--on-primary)", ib: "color-mix(in srgb, var(--on-primary) 18%, transparent)", ifg: "var(--on-primary)", icon: "gpp_good", title: "Активная охрана" },
  alarm: { bg: "var(--error)", fg: "var(--on-error)", ib: "color-mix(in srgb, var(--on-error) 18%, transparent)", ifg: "var(--on-error)", icon: "notifications_active", title: "Тревога" },
};
function Hero({ d, loading, stale, secs, tick, flash, note }) {
  if (loading) return (
    <div style={{ borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-highlight)", padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
      <div style={{ display: "flex", gap: 14, alignItems: "center" }}>
        <Skel h={56} w={56} r={999} />
        <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 8 }}><Skel h={26} w="60%" /><Skel h={18} w="85%" /></div>
      </div>
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <CmpCircularProgressIndicator size={18} strokeWidth={2.5} />
        <CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём первый ответ блока…</CmpText>
      </div>
    </div>
  );
  const alarm = isAlarm(d);
  const c = HERO[alarm ? "alarm" : d.guard];
  const alarmZones = ZONES.filter((z) => toneOf(d.z[z.k]) === "alarm").map((z) => z.label).join(" · ");
  const fresh = stale ? `Нет данных ${secs} с` : secs < 2 ? "На связи · только что" : `На связи · ${secs} с назад`;
  return (
    <div style={{ position: "relative", overflow: "hidden", borderRadius: "var(--r-card-medium-radius)", background: c.bg, color: c.fg, padding: "8px 12px", display: "flex", flexDirection: "column", gap: 8, transition: "background .35s" }}>
      {alarm && <span className="cs-anim" style={{ position: "absolute", inset: 0, background: c.fg, animation: "csBlink 1s ease-in-out infinite", pointerEvents: "none" }} />}
      {flash && <span key={"h" + tick} className="cs-anim" style={{ position: "absolute", inset: 0, background: d.guard === "disarmed" ? "var(--primary)" : c.fg, animation: "csFlash 1.8s ease-out both", pointerEvents: "none" }} />}
      <div style={{ display: "flex", alignItems: "center", gap: 12, position: "relative" }}>
        <div style={{ position: "relative", width: 44, height: 44, borderRadius: 999, background: c.ib, color: c.ifg, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
          <CmpIcon icon={stale ? "sync_problem" : c.icon} size={26} />
          {!stale && <span key={tick} className="cs-anim" aria-label="На связи" style={{ position: "absolute", right: 0, top: 0, width: 10, height: 10, borderRadius: 999, background: "var(--success)", boxShadow: `0 0 0 2px ${c.bg}`, animation: "csDot .6s ease-out 1" }} />}
        </div>
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 0 }}>
          <CmpText variant="titleLarge" weight={700} color={c.fg} style={{ lineHeight: 1.15 }}>{c.title}</CmpText>
          <CmpText variant="bodyLarge" color={c.fg} style={{ opacity: stale ? 0.75 : 1, lineHeight: 1.25, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{stale ? fresh : alarm ? alarmZones : d.event}</CmpText>
        </div>
      </div>
      {note && (
        <div className="cs-anim" style={{ position: "relative", display: "flex", alignItems: "center", gap: 10, minHeight: 40, padding: "6px 12px", borderRadius: "var(--r-card-small-radius)", background: `color-mix(in srgb, ${c.fg} 12%, transparent)`, animation: "csIn .25s ease both" }}>
          {note.type === "sending" ? <CmpCircularProgressIndicator size={18} strokeWidth={2.5} color={c.fg} /> : <CmpIcon icon={note.type === "ok" ? "check_circle" : "error"} size={22} color={c.fg} />}
          <CmpText variant="bodyMedium" weight={500} color={c.fg}>{note.type === "sending" ? `Ждём ответа блока: ${note.text}` : note.type === "ok" ? `Блок выполнил: ${note.text}` : `${note.msg}: ${note.text}`}</CmpText>
        </div>
      )}
    </div>
  );
}

function Banner({ icon, title, text, action, onAction, tone = "warn", busy }) {
  const s = tone === "warn" ? { bg: "var(--tertiary-container)", fg: "var(--on-tertiary-container)" } : { bg: "var(--background-surface-highlight)", fg: "var(--on-surface)" };
  return (
    <div className="cs-anim" style={{ display: "flex", alignItems: "center", gap: 12, padding: "10px 10px 10px 14px", borderRadius: "var(--r-card-medium-radius)", background: s.bg, color: s.fg, animation: "csIn .25s ease both" }}>
      {busy ? <CmpCircularProgressIndicator size={24} strokeWidth={3} color={s.fg} /> : <CmpIcon icon={icon} size={26} />}
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="titleSmall" weight={700} color={s.fg} style={{ fontSize: "var(--t-paragraph2-size)", display: "block" }}>{title}</CmpText>
        {text && <CmpText variant="bodyMedium" color={s.fg}>{text}</CmpText>}
      </div>
      {action && <button type="button" onClick={onAction} style={{ flex: "0 0 auto", minHeight: 48, padding: "0 16px", borderRadius: 999, border: `1.5px solid ${s.fg}`, background: "transparent", color: s.fg, fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, cursor: "pointer" }}>{action}</button>}
    </div>
  );
}

// ── Двигатель и питание, связь, дополнительно ───────────────────────────────
function Metric({ label, value, unit, changed, tick }) {
  return (
    <div style={{ position: "relative", padding: "10px 12px", borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)", minHeight: 72, boxSizing: "border-box", display: "flex", flexDirection: "column", justifyContent: "center", gap: 2 }}>
      <Flash on={changed} k={tick} />
      <CmpText variant="bodyMedium" color="var(--on-surface-variant)" style={{ position: "relative" }}>{label}</CmpText>
      <span style={{ position: "relative", fontFamily: "var(--font-mono)", fontSize: "var(--t-headline1-size)", fontWeight: 700, color: "var(--on-surface)" }}>{value}<span style={{ fontSize: "var(--t-paragraph2-size)", fontWeight: 500, marginLeft: 4, color: "var(--on-surface-variant)" }}>{unit}</span></span>
    </div>
  );
}
function KV({ label, value, tone, children }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, minHeight: 48, padding: "4px 4px" }}>
      <CmpText variant="bodyLarge" color="var(--on-surface)">{label}</CmpText>
      <span style={{ display: "inline-flex", alignItems: "center", gap: 8, fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, color: tone === "alarm" ? "var(--error)" : tone === "active" ? "var(--primary)" : "var(--on-surface)", textAlign: "right" }}>{children}{value}</span>
    </div>
  );
}
const ENGINE_LABEL = { off: "Заглушен", on: "Запущен", ready: "Готов (Ready)" };
function EngineCard({ d, tick, changed }) {
  const e = d.engine, p = d.power;
  const metrics = [
    e.state !== "off" && e.rpm != null && ["Обороты", fmt(e.rpm), "об/мин", "rpm"],
    p.volt != null && ["Напряжение сети", p.volt.toFixed(1), "В", "volt"],
    p.tEngine != null && ["Температура двигателя", p.tEngine, "°C"],
    p.tCabin != null && ["Температура в салоне", p.tCabin, "°C"],
    p.tOut != null && ["Температура снаружи", p.tOut, "°C"],
    p.fuel != null && ["Топливо", p.fuel, "%"],
  ].filter(Boolean);
  return (
    <Card title="Двигатель и питание">
      <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        <div style={{ position: "relative", display: "flex", alignItems: "center", gap: 12, minHeight: 56, padding: "4px 4px 4px 2px", borderRadius: "var(--r-card-small-radius)" }}>
          <Flash on={!!changed.engine} k={tick} />
          <div style={{ width: 44, height: 44, borderRadius: 999, display: "grid", placeItems: "center", background: e.state === "off" ? "var(--background-surface-highlight)" : "var(--primary)", color: e.state === "off" ? "var(--on-surface-variant)" : "var(--on-primary)", flex: "0 0 auto" }}><CmpIcon icon="settings_power" size={24} /></div>
          <CmpText variant="titleMedium" style={{ flex: 1 }}>Двигатель</CmpText>
          <ValuePill tone={e.state === "off" ? "rest" : "active"} text={ENGINE_LABEL[e.state]} />
        </div>
        {e.auto === "running" ? (
          <div style={{ borderRadius: "var(--r-card-medium-radius)", background: "var(--primary-container)", color: "var(--on-primary-container)", padding: "12px 14px", display: "flex", flexDirection: "column", gap: 8 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
              <span className="cs-anim" style={{ display: "inline-flex", animation: "csDot 1.5s ease-in-out infinite" }}><CmpIcon icon="autorenew" size={22} /></span>
              <CmpText variant="titleSmall" weight={700} style={{ fontSize: "var(--t-paragraph2-size)" }}>Автозапуск · до остановки</CmpText>
            </div>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: 40, fontWeight: 700, lineHeight: 1.1, letterSpacing: ".02em" }}>{mmss(e.left)}</span>
            <CmpLinearProgressIndicator progress={e.left / e.total} height={6} color="var(--primary)" trackColor="color-mix(in srgb, var(--primary) 20%, transparent)" />
          </div>
        ) : (
          <KV label="Автозапуск" value={e.auto === "enabled" ? "Включён в настройках" : "Выкл"} />
        )}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(150px, 1fr))", gap: 8 }}>
          {metrics.map(([l, v, u, ck]) => <Metric key={l} label={l} value={v} unit={u} changed={ck && !!changed[ck]} tick={tick} />)}
        </div>
      </div>
    </Card>
  );
}
function Bars({ n }) {
  return <span aria-hidden="true" style={{ display: "inline-flex", alignItems: "flex-end", gap: 3, height: 20 }}>{[1, 2, 3].map((i) => <span key={i} style={{ width: 6, height: 6 + i * 4, borderRadius: 2, background: i <= n ? "currentColor" : "var(--outline-variant)" }} />)}</span>;
}
function LinkCard({ d }) {
  const l = d.link;
  return (
    <Card title="Связь" pad="4px 12px 8px">
      <KV label="GSM" value={l.gsm < 0 ? "Выкл" : `Сигнал ${l.gsm} из 3`} tone={l.gsm < 0 ? "rest" : "active"}>{l.gsm >= 0 && <Bars n={l.gsm} />}</KV>
      <KV label="GPS" value={l.gps.fix ? `Есть · ${l.gps.speed} км/ч` : "Нет фиксации"} tone={l.gps.fix ? "active" : "rest"} />
      {l.sms != null && <KV label="SMS и звонки" value={l.sms ? "Разрешены" : "Запрещены"} />}
    </Card>
  );
}

function Acc({ icon, title, summary, open, onToggle, children }) {
  return (
    <CmpSurface color="var(--background-surface-card)" radius="var(--r-card-medium-radius)" border style={{ boxSizing: "border-box", overflow: "hidden" }}>
      <div role="button" tabIndex={0} aria-expanded={open} onClick={onToggle} onKeyDown={(e) => { if (e.key === "Enter") onToggle(); }}
        style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 68, padding: "8px 12px 8px 14px", cursor: "pointer" }}>
        <div style={{ width: 44, height: 44, borderRadius: "var(--r-card-small-radius)", background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={icon} size={24} /></div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CmpText variant="titleMedium" weight={600} style={{ display: "block" }}>{title}</CmpText>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)" maxLines={1}>{summary}</CmpText>
        </div>
        <span style={{ display: "inline-flex", transform: open ? "rotate(180deg)" : "none", transition: "transform .25s" }}><CmpIcon icon="expand_more" size={28} color="var(--on-surface-variant)" /></span>
      </div>
      {open && <div className="cs-anim" style={{ padding: "0 12px 10px", animation: "csUp .22s ease both" }}>{children}</div>}
    </CmpSurface>
  );
}
function ExtraGroups({ d, initialOpen }) {
  const [open, setOpen] = React.useState(initialOpen || {});
  const t = (k) => ({ open: !!open[k], onToggle: () => setOpen((o) => ({ ...o, [k]: !o[k] })) });
  const g = [];
  const c = d.can;
  if (c) {
    const winOpen = Object.values(c.windows).filter((v) => v === "on").length;
    g.push(
      <Acc key="can" icon="cable" title="Данные CAN" summary={`Пробег ${fmt(c.mileage)} км${c.washerLow ? " · мало омывателя" : ""}`} {...t("can")}>
        <KV label="Пробег" value={`${fmt(c.mileage)} км`} />
        <KV label="Запас хода" value={`${fmt(c.range)} км`} />
        <KV label="Расход" value={`${c.cons} л/100 км`} />
        <KV label="Омыватель" value={c.washerLow ? "Низкий уровень" : "Норма"} tone={c.washerLow ? "alarm" : "rest"} />
        <div style={{ padding: "8px 4px 4px" }}><CmpText variant="titleSmall" weight={600} style={{ fontSize: "var(--t-paragraph2-size)" }}>{winOpen ? `Окна · открыто: ${winOpen}` : "Окна · все закрыты"}</CmpText></div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8, padding: "4px 0 6px" }}>
          {[["fl", "Пер. левое"], ["fr", "Пер. правое"], ["rl", "Задн. левое"], ["rr", "Задн. правое"]].map(([k, l]) => {
            const on = c.windows[k] === "on";
            return <div key={k} style={{ minHeight: 56, borderRadius: "var(--r-card-small-radius)", padding: "8px 12px", boxSizing: "border-box", background: on ? "var(--primary-container)" : "var(--background-surface-highlight)", display: "flex", flexDirection: "column", justifyContent: "center" }}>
              <CmpText variant="bodyMedium" color={on ? "var(--on-primary-container)" : "var(--on-surface-variant)"}>{l}</CmpText>
              <CmpText variant="titleSmall" weight={700} color={on ? "var(--primary)" : "var(--on-surface)"} style={{ fontSize: "var(--t-paragraph2-size)" }}>{on ? "Открыто" : "Закрыто"}</CmpText>
            </div>;
          })}
        </div>
        {c.tires && (<>
          <div style={{ padding: "8px 4px 4px" }}><CmpText variant="titleSmall" weight={600} style={{ fontSize: "var(--t-paragraph2-size)" }}>Давление в шинах, бар</CmpText></div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(96px, 1fr))", gap: 8, padding: "4px 0 6px" }}>
            {[["fl", "Пер. лев."], ["fr", "Пер. прав."], ["rl", "Задн. лев."], ["rr", "Задн. прав."], ["spare", "Запасное"]].map(([k, l]) => (
              <div key={k} style={{ minHeight: 56, borderRadius: "var(--r-card-small-radius)", padding: "8px 10px", boxSizing: "border-box", background: "var(--background-surface-highlight)" }}>
                <CmpText variant="bodyMedium" color="var(--on-surface-variant)" style={{ display: "block" }}>{l}</CmpText>
                <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-headline2-size)", fontWeight: 700 }}>{c.tires[k].toFixed(1)}</span>
              </div>
            ))}
          </div>
        </>)}
      </Acc>
    );
  }
  if (d.ev) g.push(
    <Acc key="ev" icon="ev_station" title="Электромобиль" summary={`Заряд ${d.ev.soc} % · зарядка: ${d.ev.charge.toLowerCase()}`} {...t("ev")}>
      <KV label="Зарядка" value={d.ev.charge} tone={d.ev.charge === "Нет" ? "rest" : "active"} />
      <KV label="Заряд батареи (SOC)" value={`${d.ev.soc} %`} />
      <KV label="Состояние батареи (SOH)" value={`${d.ev.soh} %`} />
      <KV label="Температура батареи" value={`${d.ev.tBat} °C`} />
      <KV label="Запас хода на батарее" value={`${fmt(d.ev.range)} км`} />
    </Acc>
  );
  if (d.heater) g.push(
    <Acc key="heater" icon="local_fire_department" title="Подогреватель" summary={`${d.heater.on ? "Работает" : "Выкл"}${d.heater.errors ? " · есть ошибки" : ""}`} {...t("heater")}>
      <KV label="Статус" value={d.heater.on ? "Работает" : "Выкл"} tone={d.heater.on ? "active" : "rest"} />
      <KV label="Температура" value={`${d.heater.t} °C`} />
      <KV label="Напряжение" value={`${d.heater.volt.toFixed(1)} В`} />
      <KV label="Мощность" value={`${d.heater.power} %`} />
      <KV label="Ошибки" value={d.heater.errors ? "Есть ошибки" : "Нет"} tone={d.heater.errors ? "alarm" : "rest"} />
    </Acc>
  );
  if (d.camera) g.push(
    <Acc key="cam" icon="videocam" title="Камера" summary={d.camera.rec ? `Идёт запись · ${d.camera.min} мин` : "Установлена"} {...t("cam")}>
      <KV label="Камера" value={d.camera.installed ? "Установлена" : "Нет"} />
      <KV label="Запись" value={d.camera.rec ? "Идёт" : "Нет"} tone={d.camera.rec ? "active" : "rest"} />
      <KV label="Записано" value={`${d.camera.min} мин`} />
    </Acc>
  );
  if (d.siren || d.hf) g.push(
    <Acc key="siren" icon="volume_up" title="Сирена и hands-free" summary={[d.siren && `Сирена: ${d.siren.mode}`, d.hf && `Hands-free: ${d.hf.mode}`].filter(Boolean).join(" · ")} {...t("siren")}>
      {d.siren && <KV label="Сирена" value={d.siren.mode} />}
      {d.hf && <KV label="Hands-free" value={d.hf.mode} />}
    </Acc>
  );
  if (d.hours != null) g.push(
    <Acc key="hours" icon="schedule" title="Моточасы" summary={`${fmt(d.hours)} ч`} {...t("hours")}>
      <KV label="Моточасы" value={`${fmt(d.hours)} ч`} />
    </Acc>
  );
  if (!g.length) return null;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      <div style={{ padding: "8px 4px 0" }}><CmpText variant="titleMedium" weight={600}>Дополнительно</CmpText></div>
      {g}
    </div>
  );
}

// ── Кнопка команды ───────────────────────────────────────────────────────────
function useHold(ms, onDone, enabled = true) {
  const [p, setP] = React.useState(0);
  const [hint, setHint] = React.useState(false);
  const raf = React.useRef(null), t0 = React.useRef(0), hintT = React.useRef(null), cb = React.useRef(onDone);
  cb.current = onDone;
  React.useEffect(() => () => { cancelAnimationFrame(raf.current); clearTimeout(hintT.current); }, []);
  const step = () => {
    const k = Math.min(1, (performance.now() - t0.current) / ms);
    setP(k);
    if (k >= 1) { raf.current = null; setP(0); cb.current(); } else raf.current = requestAnimationFrame(step);
  };
  const start = (e) => {
    if (!enabled || raf.current) return;
    if (e && e.preventDefault) e.preventDefault();
    t0.current = performance.now(); setHint(false); raf.current = requestAnimationFrame(step);
  };
  const end = () => {
    if (!raf.current) return;
    cancelAnimationFrame(raf.current); raf.current = null; setP(0); setHint(true);
    clearTimeout(hintT.current); hintT.current = setTimeout(() => setHint(false), 1800);
  };
  return {
    p, hint,
    handlers: {
      onPointerDown: start, onPointerUp: end, onPointerLeave: end, onPointerCancel: end, onContextMenu: (e) => e.preventDefault(),
      onKeyDown: (e) => { if ((e.key === "Enter" || e.key === " ") && !e.repeat) start(e); },
      onKeyUp: (e) => { if (e.key === "Enter" || e.key === " ") end(); },
    },
  };
}

// Иконки старого приложения (features/check-state/icons, перекрашены через currentColor): префикс "li:".
const LI = {};
function liSvg(n) { if (!LI[n]) LI[n] = fetch((window.CS_BASE || "") + "icons/" + n + ".svg").then((r) => r.text()).catch(() => ""); return LI[n]; }
function Ico({ icon, size = 24, color }) {
  const li = typeof icon === "string" && icon.startsWith("li:");
  const [h, setH] = React.useState("");
  React.useEffect(() => { if (!li) return; let on = true; liSvg(icon.slice(3)).then((x) => on && setH(x)); return () => { on = false; }; }, [icon]);
  if (!li) return <CmpIcon icon={icon} size={size} color={color} />;
  return <span className="cs-veh" aria-hidden="true" style={{ display: "inline-block", width: size, height: size, color, flex: "0 0 auto" }} dangerouslySetInnerHTML={{ __html: h }} />;
}
// Иконка команды с кольцом прогресса: удержание — определённое кольцо, отправка — бесконечное.
function RingIcon({ icon, color, p = 0, sending, size = 48, iconSize = 26 }) {
  return (
    <span style={{ position: "relative", width: size, height: size, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
      <span style={{ position: "absolute", inset: 0, borderRadius: 999, background: p > 0 || sending ? "color-mix(in srgb, var(--primary) 14%, transparent)" : "transparent", transition: "background .2s" }} />
      {(p > 0 || sending) && <span style={{ position: "absolute", inset: 0 }}><CmpCircularProgressIndicator size={size} strokeWidth={3.5} progress={sending ? null : p} trackColor="color-mix(in srgb, var(--primary) 18%, transparent)" /></span>}
      <Ico icon={icon} size={iconSize} color={color} />
    </span>
  );
}
// Чистое представление кнопки (используется и в каталоге компонентов).
function CmdView({ c, st, p = 0, hint, disabled, handlers = {}, onClick }) {
  const s = st && st.st;
  const look = s === "success" ? { bg: "color-mix(in srgb, var(--success) 24%, var(--surface-container-high))", fg: "var(--on-surface)", ic: "check_circle", icc: "var(--on-surface)", cap: "Выполнено" }
    : s === "error" ? { bg: "var(--error-container)", fg: "var(--on-error-container)", ic: "error", icc: "var(--on-error-container)", cap: st.msg || "Блок не ответил" }
    : s === "sending" ? { bg: "var(--background-surface-highlight)", fg: "var(--on-surface)", cap: "Ждём ответа блока" }
    : { bg: "var(--background-surface-highlight)", fg: "var(--on-surface)", ic: c.icon, icc: c.danger ? "var(--error)" : "var(--primary)", cap: p > 0 ? "Держите…" : hint ? "Держите 1 с" : c.danger ? "С вопросом" : null };
  const off = disabled || s === "sending";
  return (
    <div role="button" tabIndex={off ? -1 : 0} aria-disabled={off} aria-label={c.label} onClick={off ? undefined : onClick} {...(off ? {} : handlers)}
      style={{ position: "relative", overflow: "hidden", userSelect: "none", WebkitUserSelect: "none", touchAction: "manipulation", minHeight: 80, boxSizing: "border-box", padding: "12px 10px", borderRadius: "var(--r-card-medium-radius)", background: look.bg, color: look.fg, display: "flex", alignItems: "center", gap: 12, cursor: off ? "default" : "pointer", opacity: disabled ? 0.45 : 1, transition: "background .25s", border: hint && !p ? "2px solid var(--primary)" : "2px solid transparent" }}>
      <RingIcon icon={s === "sending" ? c.icon : look.ic} color={s === "sending" ? "var(--primary)" : look.icc} p={p} sending={s === "sending"} size={40} iconSize={30} />
      <span style={{ position: "relative", flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, lineHeight: 1.25, textWrap: "pretty" }}>{c.label}</span>
        {look.cap && <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, lineHeight: 1.25, color: s === "error" ? "var(--on-error-container)" : hint || p > 0 ? "var(--primary)" : c.danger && !s ? "var(--error)" : "var(--on-surface-variant)" }}>{look.cap}</span>}
      </span>
    </div>
  );
}
function CmdButton({ c, st, disabled, onFire, onDanger }) {
  const h = useHold(1000, () => onFire(c), !c.danger && !disabled);
  if (c.danger) return <CmdView c={c} st={st} disabled={disabled} onClick={() => onDanger(c)} />;
  return <CmdView c={c} st={st} p={h.p} hint={h.hint} disabled={disabled} handlers={h.handlers} />;
}
function CommandsPanel({ cmds, cmdSt, disabled, onFire, onDanger, cols = 2, bare }) {
  const body = (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 12px", borderRadius: "var(--r-card-small-radius)", background: "var(--secondary-container)", color: "var(--on-secondary-container)" }}>
        <CmpIcon icon="touch_app" size={24} />
        <CmpText variant="bodyMedium" color="var(--on-secondary-container)">Удерживайте кнопку 1 секунду. Запуск двигателя и паника — с подтверждением.</CmpText>
      </div>
      <div style={{ display: "grid", gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`, gap: 8 }}>
        {cmds.map((c) => <CmdButton key={c.k} c={c} st={cmdSt[c.k]} disabled={disabled} onFire={onFire} onDanger={onDanger} />)}
      </div>
    </div>
  );
  return bare ? body : <Card title="Команды">{body}</Card>;
}

// ── Лист подтверждения опасной команды ──────────────────────────────────────
function HoldBar({ text, ms = 2000, onDone, tone = "error" }) {
  const h = useHold(ms, onDone);
  const bg = tone === "error" ? "var(--error)" : "var(--primary)";
  const fg = tone === "error" ? "var(--on-error)" : "var(--on-primary)";
  return (
    <div role="button" tabIndex={0} {...h.handlers} style={{ position: "relative", overflow: "hidden", userSelect: "none", touchAction: "manipulation", minHeight: 64, borderRadius: 999, background: `color-mix(in srgb, ${bg} 22%, var(--surface-container-high))`, display: "flex", alignItems: "center", justifyContent: "center", gap: 10, cursor: "pointer", border: `2px solid ${bg}` }}>
      <span style={{ position: "absolute", left: 0, top: 0, bottom: 0, width: `${h.p * 100}%`, background: bg }} />
      <span style={{ position: "relative", display: "inline-flex", alignItems: "center", gap: 10, fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", fontWeight: 700, color: h.p > 0.5 ? fg : "var(--on-surface)" }}>
        <CmpIcon icon="touch_app" size={24} />{h.p > 0 ? "Держите…" : h.hint ? "Держите до конца полосы" : text}
      </span>
    </div>
  );
}
function DangerSheet({ k, d, onCancel, onConfirm }) {
  const start = k === "start";
  const checks = start ? [
    { l: "Нейтраль / «P»", ok: toneOf(d.z.neutral) === "active", v: zVal("neutral", d.z.neutral) },
    { l: "Ручник", ok: toneOf(d.z.handbrake) === "active", v: zVal("handbrake", d.z.handbrake) },
    { l: "Капот", ok: d.z.hood === "off", v: zVal("hood", d.z.hood) },
  ] : null;
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 8, display: "flex", flexDirection: "column", justifyContent: "flex-end" }}>
      <div onClick={onCancel} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)" }} />
      <div className="cs-anim" style={{ position: "relative", background: "var(--background-surface-card)", borderRadius: "28px 28px 0 0", padding: "10px 20px 20px", display: "flex", flexDirection: "column", gap: 16, animation: "csUp .25s ease both", maxWidth: 640, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
        <div style={{ alignSelf: "center", width: 36, height: 4, borderRadius: 2, background: "var(--outline)" }} />
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: 56, height: 56, borderRadius: 999, background: "var(--error-container)", color: "var(--on-error-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><Ico icon={start ? "li:engine_start" : "li:alarm"} size={34} /></div>
          <CmpText variant="headlineSmall">{start ? "Запустить двигатель?" : "Включить панику?"}</CmpText>
        </div>
        <CmpText variant="bodyLarge" color="var(--on-surface)">{start ? "Двигатель запустится дистанционно. Проверьте, что машина не тронется с места и рядом нет людей." : "Включатся сирена и аварийная сигнализация. Предупредите людей рядом."}</CmpText>
        {checks && (
          <div style={{ borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-highlight)", padding: "4px 12px" }}>
            {checks.map((x) => (
              <div key={x.l} style={{ display: "flex", alignItems: "center", gap: 10, minHeight: 48 }}>
                <CmpIcon icon={x.ok ? "check_circle" : "error"} size={24} color={x.ok ? "var(--primary)" : "var(--error)"} />
                <CmpText variant="bodyLarge" style={{ flex: 1 }}>{x.l}</CmpText>
                <CmpText variant="titleSmall" weight={700} color={x.ok ? "var(--on-surface)" : "var(--error)"} style={{ fontSize: "var(--t-paragraph2-size)" }}>{x.v}</CmpText>
              </div>
            ))}
          </div>
        )}
        <HoldBar text={start ? "Удерживайте 2 с — запустить" : "Удерживайте 2 с — включить"} onDone={onConfirm} />
        <CmpOutlinedButton text="Отмена" fullWidth onClick={onCancel} style={{ height: 56, fontSize: "var(--t-paragraph2-size)" }} />
      </div>
    </div>
  );
}

// ── Диалог (3 действия, крупные кнопки) ─────────────────────────────────────
function Modal({ icon, tone = "primary", title, text, children, actions, onDismiss }) {
  const ib = tone === "error" ? "var(--error-container)" : tone === "warn" ? "var(--tertiary-container)" : "var(--primary-container)";
  const ifg = tone === "error" ? "var(--on-error-container)" : tone === "warn" ? "var(--on-tertiary-container)" : "var(--on-primary-container)";
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 9, display: "grid", placeItems: "center", padding: 20 }}>
      <div onClick={onDismiss} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)" }} />
      <div className="cs-anim" style={{ position: "relative", width: "100%", maxWidth: 400, boxSizing: "border-box", background: "var(--background-surface-highlight)", borderRadius: "var(--r-card-medium-radius)", padding: 24, display: "flex", flexDirection: "column", gap: 16, animation: "csUp .22s ease both" }}>
        {icon && <div style={{ alignSelf: "center", width: 56, height: 56, borderRadius: 999, background: ib, color: ifg, display: "grid", placeItems: "center" }}><CmpIcon icon={icon} size={30} /></div>}
        <CmpText variant="headlineSmall" align="center">{title}</CmpText>
        {text && <CmpText variant="bodyLarge" color="var(--on-surface)" align="center">{text}</CmpText>}
        {children}
        <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 4 }}>{actions}</div>
      </div>
    </div>
  );
}
const BIG = { height: 56, fontSize: "var(--t-paragraph2-size)" };

function PinBody() {
  const [v, setV] = React.useState("");
  return (
    <label style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">Сервисный PIN</CmpText>
      <input value={v} onChange={(e) => setV(e.target.value.replace(/\D/g, "").slice(0, 6))} inputMode="numeric" autoFocus placeholder="••••"
        style={{ height: 60, borderRadius: "var(--r-card-small-radius)", border: "2px solid var(--primary)", background: "var(--surface)", color: "var(--on-surface)", fontFamily: "var(--font-mono)", fontSize: "var(--t-title2-size)", letterSpacing: ".4em", textAlign: "center", outline: "none" }} />
    </label>
  );
}

// ── Чек-лист быстрого монтажа ───────────────────────────────────────────────
// current: undefined — свободный порядок; true — текущее задание (выделено); false — «Ожидает» (строгий порядок).
function TaskRow({ t, st, d, changed, tick, n, onSkip, onAnswer, current }) {
  const done = st === "done" || st === "yes";
  const fail = st === "no";
  const skip = st === "skipped";
  const waiting = st === "pending" && current === false;
  const pending = st === "pending" && !waiting;
  const cur = pending && current === true;
  const live = t.type === "auto" && (t.guard ? { tone: d.guard === "disarmed" ? "rest" : "active", text: HERO[d.guard].title } : { tone: toneOf(d.z[t.zone]), text: zVal(t.zone, d.z[t.zone]) });
  const ch = t.type === "auto" && (t.guard ? changed.guard : changed[t.zone]);
  return (
    <div style={{ position: "relative", display: "flex", gap: 12, padding: "12px 10px", borderRadius: "var(--r-card-medium-radius)", background: done ? "color-mix(in srgb, var(--success) 14%, transparent)" : fail ? "var(--error-container)" : cur ? "var(--secondary-container)" : "transparent", boxShadow: cur ? "inset 0 0 0 2px var(--primary)" : "none", transition: "background .4s" }}>
      {ch && done && <Flash on k={tick} radius="var(--r-card-medium-radius)" color="var(--success)" />}
      <span style={{ position: "relative", width: 36, height: 36, borderRadius: 999, flex: "0 0 auto", display: "grid", placeItems: "center", background: done ? "var(--success)" : fail ? "var(--error)" : "transparent", border: cur ? "2px solid var(--primary)" : pending || waiting ? "2px solid var(--outline)" : skip ? "2px dashed var(--outline)" : "none", color: done ? "#fff" : fail ? "var(--on-error)" : cur ? "var(--primary)" : "var(--on-surface-variant)", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700 }}>
        {done ? <CmpIcon icon="check" size={22} color="#fff" /> : fail ? <CmpIcon icon="close" size={22} /> : skip ? <CmpIcon icon="redo" size={20} /> : n}
      </span>
      <div style={{ position: "relative", flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 8 }}>
        {cur && <CmpText variant="labelLarge" weight={700} color="var(--primary)" style={{ paddingTop: 2, marginBottom: -6 }}>Текущее задание</CmpText>}
        <CmpText variant="titleMedium" weight={pending ? 600 : 500} color={fail ? "var(--on-error-container)" : waiting ? "var(--on-surface-variant)" : "var(--on-surface)"} style={{ textWrap: "pretty", paddingTop: cur ? 0 : 6 }}>{t.text}</CmpText>
        {waiting && (
          <span style={{ display: "inline-flex", alignItems: "center", gap: 6, color: "var(--on-surface-variant)" }}>
            <CmpIcon icon="schedule" size={20} />
            <CmpText variant="bodyMedium" weight={600} color="var(--on-surface-variant)">Ожидает — после предыдущего</CmpText>
          </span>
        )}
        {t.type === "auto" && pending && (
          <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Сейчас:</CmpText>
            <ValuePill tone={live.tone} text={live.text} />
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)">· отметится само</CmpText>
          </div>
        )}
        {t.type === "manual" && pending && (
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 8 }}>
            {t.options.map((o) => <CmpOutlinedButton key={o} text={o} onClick={() => onAnswer(t.id, o === "Да" ? "yes" : "no")} style={BIG} />)}
          </div>
        )}
        {!pending && !waiting && (
          <CmpText variant="bodyMedium" weight={600} color={fail ? "var(--on-error-container)" : "var(--on-surface-variant)"}>
            {st === "done" ? "Проверка пройдена" : st === "yes" ? "Ответ: да" : st === "no" ? "Ответ: нет — не работает" : "Пропущено"}
          </CmpText>
        )}
      </div>
      {t.type === "auto" && pending && (
        <button type="button" onClick={() => onSkip(t.id)} style={{ position: "relative", alignSelf: "flex-start", flex: "0 0 auto", minHeight: 48, padding: "0 12px", border: "none", borderRadius: 999, background: "transparent", color: "var(--primary)", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, cursor: "pointer" }}>Пропустить</button>
      )}
    </div>
  );
}
function Checklist({ tasks, ts, d, changed, tick, onSkip, onAnswer, bare, seq }) {
  const curId = seq ? (tasks.find((t) => ts[t.id] === "pending") || {}).id : null;
  const closed = tasks.filter((t) => ts[t.id] !== "pending").length;
  const fails = tasks.filter((t) => ts[t.id] === "no").length;
  const all = closed === tasks.length;
  const order = seq ? tasks : [...tasks].sort((a, b) => (ts[a.id] === "pending" ? 0 : 1) - (ts[b.id] === "pending" ? 0 : 1));
  const body = (
    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <div style={{ padding: "10px 12px 12px", borderRadius: "var(--r-card-medium-radius)", background: all ? "color-mix(in srgb, var(--success) 20%, var(--surface-container-high))" : "var(--background-surface-highlight)", display: "flex", flexDirection: "column", gap: 10 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          {all && <CmpIcon icon="task_alt" size={28} color="var(--on-surface)" />}
          <CmpText variant="titleLarge" weight={700} style={{ flex: 1 }}>{all ? "Все проверки пройдены" : `Пройдено ${closed} из ${tasks.length}`}</CmpText>
        </div>
        <CmpLinearProgressIndicator progress={closed / tasks.length} height={8} color={all ? "var(--success)" : "var(--primary)"} />
        <CmpText variant="bodyMedium" color="var(--on-surface)">{all ? (fails ? `Не работает: ${fails}. Нажмите «Далее», чтобы завершить.` : "Нажмите «Далее», чтобы завершить шаг.") : "Выполняйте действия в машине — задания отметятся сами."}</CmpText>
      </div>
      {order.map((t) => <TaskRow key={t.id} t={t} st={ts[t.id]} d={d} changed={changed} tick={tick} n={tasks.indexOf(t) + 1} onSkip={onSkip} onAnswer={onAnswer} current={seq ? t.id === curId : undefined} />)}
    </div>
  );
  return bare ? body : <Card title="Проверки" pad={8}>{body}</Card>;
}

// ── Потеря связи (общий экран, поверх) ──────────────────────────────────────
function LostOverlay({ onBack }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 10, background: "var(--background)", display: "flex", flexDirection: "column" }}>
      <CmpTopAppBar title="" navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} />
      <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", padding: 24 }}>
        <div style={{ maxWidth: 440, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 16, textAlign: "center" }}>
          <div style={{ width: 96, height: 96, borderRadius: 999, background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}><CmpIcon icon="bluetooth_disabled" size={48} /></div>
          <CmpText variant="headlineSmall">Отсутствует соединение с устройством</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок выключен или находится далеко. Поднесите телефон ближе и проверьте питание блока.</CmpText>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={18} strokeWidth={2.5} /><CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём возвращения блока…</CmpText></div>
          <CmpOutlinedButton text="Отключиться" icon="link_off" fullWidth onClick={onBack} style={BIG} />
        </div>
      </div>
    </div>
  );
}

// ── Пульт: машина + показатели + основные команды (первая вкладка) ─────────
const SHORT = { arm: "Охрана", disarm: "Снять", start: "Запуск", stop: "Стоп", extend: "Продлить", trunk: "Багажник", panic: "Паника" };
function CmdSquare({ c, st, disabled, onFire, onDanger }) {
  const h = useHold(1000, () => onFire(c), !c.danger && !disabled);
  const s = st && st.st; const off = disabled || s === "sending";
  const p = c.danger ? 0 : h.p;
  const bg = s === "success" ? "color-mix(in srgb, var(--success) 26%, var(--surface-container-high))" : s === "error" ? "var(--error-container)" : "var(--background-surface-highlight)";
  const icon = s === "success" ? "check_circle" : s === "error" ? "error" : c.icon;
  const cap = p > 0 ? "Держите…" : h.hint && !c.danger ? "Держите 1 с" : s === "error" ? "Ошибка" : s === "success" ? "Готово" : SHORT[c.k] || c.label;
  const fg = s === "error" ? "var(--on-error-container)" : h.hint || p > 0 ? "var(--primary)" : "var(--on-surface)";
  return (
    <div role="button" aria-label={c.label} tabIndex={off ? -1 : 0} onClick={c.danger && !off ? () => onDanger(c) : undefined} {...(!c.danger && !off ? h.handlers : {})}
      style={{ position: "relative", overflow: "hidden", userSelect: "none", WebkitUserSelect: "none", touchAction: "manipulation", height: 72, boxSizing: "border-box", borderRadius: "var(--r-card-medium-radius)", background: bg, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2, cursor: off ? "default" : "pointer", opacity: disabled ? 0.45 : 1, border: h.hint && !p ? "2px solid var(--primary)" : "2px solid transparent", transition: "background .25s" }}>
      <span style={{ position: "relative", height: 40, display: "grid", placeItems: "center" }}>{(p > 0 || s === "sending") ? <RingIcon icon={c.icon} color="var(--primary)" p={p} sending={s === "sending"} size={40} iconSize={30} /> : <Ico icon={icon} size={34} color={c.danger && !s ? "var(--error)" : s === "error" ? "var(--on-error-container)" : "var(--primary)"} />}</span>
      <span style={{ position: "relative", fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, lineHeight: 1.1, color: fg }}>{cap}</span>
    </div>
  );
}
function stripItems(d) {
  const e = d.engine, p = d.power, l = d.link;
  return [
    e.auto === "running" && { icon: "li:autostart", v: mmss(e.left), l: "Автозапуск", on: true },
    e.state !== "off" && e.rpm != null && { icon: "li:speed", v: fmt(e.rpm), l: "об/мин", ck: "rpm" },
    p.volt != null && { icon: "li:voltage", v: p.volt.toFixed(1) + " В", l: "Сеть", ck: "volt" },
    p.tEngine != null && { icon: "li:engine", v: p.tEngine + " °C", l: "Двигатель" },
    p.tCabin != null && { icon: "li:cabin", v: p.tCabin + " °C", l: "Салон" },
    p.tOut != null && { icon: "li:out_temp", v: p.tOut + " °C", l: "Снаружи" },
    p.fuel != null && { icon: "li:fuel", v: p.fuel + " %", l: "Топливо" },
    { icon: l.gsm < 0 ? "li:network_off" : "li:network_" + l.gsm, v: l.gsm < 0 ? "Выкл" : `${l.gsm} из 3`, l: "GSM" },
    { icon: "li:gps", v: l.gps.fix ? `${l.gps.speed} км/ч` : "Нет", l: "GPS" },
  ].filter(Boolean);
}
function MetricStrip({ d, changed, tick, wrap }) {
  return (
    <div style={{ display: "flex", flexWrap: wrap ? "wrap" : "nowrap", gap: 8, overflowX: wrap ? "visible" : "auto", paddingBottom: 2, scrollbarWidth: "none" }}>
      {stripItems(d).map((m) => (
        <div key={m.l} style={{ position: "relative", flex: "0 0 auto", minWidth: 88, boxSizing: "border-box", padding: "8px 12px", borderRadius: "var(--r-card-small-radius)", background: m.on ? "var(--primary-container)" : "var(--background-surface-highlight)", color: m.on ? "var(--on-primary-container)" : "var(--on-surface)", display: "flex", flexDirection: "column", gap: 2 }}>
          <Flash on={m.ck && !!changed[m.ck]} k={tick} />
          <span style={{ position: "relative", display: "flex", alignItems: "center", gap: 6 }}>
            <Ico icon={m.icon} size={24} color={m.on ? "var(--on-primary-container)" : "var(--primary)"} />
            <span style={{ fontFamily: "var(--font-sans)", fontVariantNumeric: "tabular-nums", fontSize: "var(--t-headline2-size)", fontWeight: 700, whiteSpace: "nowrap" }}>{m.v}</span>
          </span>
          <span style={{ position: "relative", fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 500, color: m.on ? "var(--on-primary-container)" : "var(--on-surface-variant)", whiteSpace: "nowrap" }}>{m.l}</span>
        </div>
      ))}
    </div>
  );
}
function ActiveChips({ d, changed, tick }) {
  const act = ZONES.filter((z) => toneOf(d.z[z.k]) !== "rest" || changed[z.k]);
  return (
    <div style={{ height: 40, display: "flex", alignItems: "center", gap: 6, overflowX: "auto", overflowY: "hidden", scrollbarWidth: "none" }}>
      {!act.length && <React.Fragment><CmpIcon icon="check_circle" size={22} color="var(--primary)" /><CmpText variant="bodyLarge" style={{ whiteSpace: "nowrap" }}>Всё закрыто, датчики в норме</CmpText></React.Fragment>}
      {act.map((z) => {
        const tn = toneOf(d.z[z.k]); const t = TONE[tn]; const ch = !!changed[z.k];
        return (
          <span key={z.k} style={{ position: "relative", flex: "0 0 auto", display: "inline-flex", alignItems: "center", gap: 6, height: 36, padding: "0 12px 0 8px", borderRadius: "var(--r-card-small-radius)", boxSizing: "border-box", background: tn === "rest" || tn === "muted" ? "var(--background-surface-highlight)" : t.tile, color: t.fg, border: `2px solid ${ch ? (tn === "alarm" ? "var(--error)" : "var(--primary)") : "transparent"}`, whiteSpace: "nowrap" }}>
            <Flash on={ch} k={tick} radius={10} color={tn === "alarm" ? "var(--error)" : "var(--primary)"} />
            <CmpIcon icon={z.icon} size={18} color={tn === "alarm" ? "var(--error)" : tn === "active" ? "var(--primary)" : "var(--on-surface-variant)"} />
            <span style={{ position: "relative", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 500 }}>{z.label}: <b style={{ fontWeight: 700 }}>{String(zVal(z.k, d.z[z.k])).toLowerCase()}</b></span>
          </span>
        );
      })}
    </div>
  );
}
function AllCommandsSheet({ children, onClose, title = "Все команды" }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 8, display: "flex", flexDirection: "column", justifyContent: "flex-end" }}>
      <div onClick={onClose} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)" }} />
      <div className="cs-anim" style={{ position: "relative", maxHeight: "86%", display: "flex", flexDirection: "column", background: "var(--background-surface-card)", borderRadius: "28px 28px 0 0", animation: "csUp .25s ease both", maxWidth: 720, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
        <div style={{ alignSelf: "center", width: 36, height: 4, borderRadius: 2, background: "var(--outline)", marginTop: 10 }} />
        <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 8px 4px 20px" }}>
          <CmpText variant="headlineSmall" style={{ flex: 1 }}>{title}</CmpText>
          <CmpIconButton icon="Close" contentDescription="Закрыть" onClick={onClose} />
        </div>
        <div style={{ overflow: "auto", padding: "4px 16px 20px" }}>{children}</div>
      </div>
    </div>
  );
}
// Широкое окно: машина развёрнута горизонтально (носом вправо) — низкий пульт, команды строкой под ней.
function VehicleH({ d, height = 180 }) {
  const w = Math.round((height * 1000) / 522);
  return (
    <div style={{ position: "relative", width: w, maxWidth: "100%", height, margin: "0 auto" }}>
      <div style={{ position: "absolute", left: (w - height) / 2, top: (height - w) / 2, width: height, height: w, transform: "rotate(90deg)" }}><Vehicle d={d} width={height} /></div>
    </div>
  );
}
function PultWide({ d, changed, tick, cmds, cmdSt, loading, onFire, onDanger, onAll, carH = 170 }) {
  const keys = ["arm", "disarm", d.engine.state === "off" ? "start" : "stop", "trunk", "panic"];
  const pc = keys.map((k) => cmds.find((c) => c.k === k)).filter(Boolean);
  return (
    <Card pad={12}>
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        <div style={{ display: "grid", gridTemplateColumns: "auto minmax(0, 1fr)", gap: 16, alignItems: "center" }}>
          <div style={{ opacity: loading ? 0.3 : 1, transition: "opacity .3s", padding: "4px 0" }}><VehicleH d={loading ? baseData() : d} height={carH} /></div>
          {loading ? <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>{[0, 1, 2, 3].map((i) => <Skel key={i} h={56} w={88} r={14} />)}</div> : <MetricStrip d={d} changed={changed} tick={tick} wrap />}
        </div>
        {loading ? <Skel h={36} w="70%" r={12} /> : <ActiveChips d={d} changed={changed} tick={tick} />}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(6, minmax(0, 1fr))", gap: 8 }}>
          {pc.map((c) => <CmdSquare key={c.k} c={c} st={cmdSt[c.k]} disabled={loading} onFire={onFire} onDanger={onDanger} />)}
          <button type="button" onClick={onAll} style={{ height: 72, border: "1.5px solid var(--outline-variant)", borderRadius: "var(--r-card-medium-radius)", background: "transparent", color: "var(--primary)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2, fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, cursor: "pointer" }}>
            <CmpIcon icon="apps" size={30} color="var(--primary)" />Все команды
          </button>
        </div>
      </div>
    </Card>
  );
}
// «Подробно» в широком окне — одна карточка с вкладками вместо длинной ленты карточек.
function DetailsWide({ d, changed, tick, onZone, loading }) {
  const [t, setT] = React.useState("zones");
  const tabs = [["zones", "Зоны"], ["engine", "Двигатель"], ["link", "Связь"], ["extra", "Дополнительно"]];
  const idx = tabs.findIndex((x) => x[0] === t);
  let body = null;
  if (loading) body = <div style={{ display: "flex", flexDirection: "column", gap: 8 }}><Skel h={22} w="40%" /><Skel h={88} /><Skel h={88} /></div>;
  else if (t === "zones") body = (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      {GROUPS.map((g) => (
        <div key={g.k} style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", justifyContent: "space-between", gap: 12, padding: "0 2px" }}><CmpText variant="titleMedium" weight={600}>{g.title}</CmpText><CmpText variant="labelLarge" color="var(--on-surface-variant)">{groupAside(g.k, d)}</CmpText></div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(150px, 1fr))", gap: 8 }}>
            {ZONES.filter((z) => z.grp === g.k).map((z) => <ZoneTile key={z.k} z={z} v={d.z[z.k]} changed={!!changed[z.k]} tick={tick} onClick={onZone && (() => onZone(z.k))} />)}
          </div>
        </div>
      ))}
    </div>
  );
  else if (t === "engine") body = <EngineCard d={d} tick={tick} changed={changed} />;
  else if (t === "link") body = <LinkCard d={d} />;
  else body = (d.can || d.ev || d.heater || d.camera || d.siren || d.hf || d.hours != null) ? <ExtraGroups d={d} /> : <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Нет дополнительных данных</CmpText>;
  return (
    <CmpSurface color="var(--background-surface-card)" radius="var(--r-card-medium-radius)" border style={{ boxSizing: "border-box", overflow: "hidden" }}>
      <div style={{ padding: "14px 16px 0" }}><CmpText variant="titleMedium" weight={700}>Подробно</CmpText></div>
      <CmpTabRow tabs={tabs.map((x) => ({ title: x[1] }))} selectedTabIndex={idx} onTabSelected={(i) => setT(tabs[i][0])} />
      <div style={{ padding: 12 }}>{body}</div>
    </CmpSurface>
  );
}
function Pult({ d, changed, tick, cmds, cmdSt, loading, onFire, onDanger, onAll, carW = 196 }) {
  const keys = ["arm", "disarm", d.engine.state === "off" ? "start" : "stop", "trunk", "panic"];
  const pc = keys.map((k) => cmds.find((c) => c.k === k)).filter(Boolean);
  return (
    <Card pad={12}>
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {loading ? <div style={{ display: "flex", gap: 8 }}>{[0, 1, 2, 3].map((i) => <Skel key={i} h={56} w={88} r={14} />)}</div> : <MetricStrip d={d} changed={changed} tick={tick} />}
        <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) 108px", gap: 10, alignItems: "start" }}>
          <div style={{ opacity: loading ? 0.3 : 1, transition: "opacity .3s" }}><Vehicle d={loading ? baseData() : d} width={carW} /></div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {pc.map((c) => <CmdSquare key={c.k} c={c} st={cmdSt[c.k]} disabled={loading} onFire={onFire} onDanger={onDanger} />)}
            <button type="button" onClick={onAll} style={{ minHeight: 48, border: "none", borderRadius: 999, background: "transparent", color: "var(--primary)", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, cursor: "pointer" }}>Все команды</button>
          </div>
        </div>
        {loading ? <Skel h={36} w="70%" r={12} /> : <ActiveChips d={d} changed={changed} tick={tick} />}
      </div>
    </Card>
  );
}

// ── Экран ────────────────────────────────────────────────────────────────────
function CheckStateScreen({ mode = "advanced", preset, zoneView = "list", response = "ok", device = { name: "Pandora DX-4GS", id: "107131" }, onBack = () => {}, onDone, layoutV = "v2", hingeAt }) {
  const { useState, useEffect, useRef } = React;
  const init = React.useMemo(() => makeInit(preset || (mode === "quick" ? "F1" : "S2"), mode), []);
  const [rootRef, width, height] = useWidth();
  const L2 = layoutV === "v2" ? layoutV2(width, height) : null;
  const wide = L2 ? L2 !== "compact" : width >= 760;
  const [pane, setPane] = React.useState(null);
  const [detailsOpen, setDetailsOpen] = React.useState(false);
  const [checksOpen, setChecksOpen] = React.useState(false);
  const [d, setD] = useState(init.data);
  const dRef = useRef(init.data); dRef.current = d;
  const queue = useRef(init.queue);
  const [changed, setChanged] = useState(init.changed);
  const [tick, setTick] = useState(0);
  const [lastAt, setLastAt] = useState(() => Date.now() - init.staleMs);
  const [now, setNow] = useState(Date.now());
  const [loading, setLoading] = useState(init.loading);
  const [stale] = useState(init.staleMs > 0);
  const [lost, setLost] = useState(init.lost);
  const [cmdSt, setCmdSt] = useState(init.cmd);
  const [note, setNote] = useState(init.cmd.arm ? { type: "sending", text: CMD_LABEL.arm } : null);
  const [sheet, setSheet] = useState(init.sheet);
  const [dialog, setDialog] = useState(init.dialog);
  const [snack, setSnack] = useState(null);
  const [toast, setToast] = useState(init.toast);
  const [quickProg, setQuickProg] = useState(init.quickProg);
  const tasks = init.tasks;
  const [ts, setTs] = useState(init.ts);
  const tsRef = useRef(init.ts); tsRef.current = ts;
  const hasChecks = mode === "quick" && tasks.length > 0;
  const [tab, setTab] = useState(init.tab || (hasChecks ? "checks" : "pult"));
  const timers = useRef([]);
  const cmdRef = useRef(null);
  const [allOpen, setAllOpen] = useState(false);
  const later = (fn, ms) => { const t = setTimeout(fn, ms); timers.current.push(t); return t; };
  useEffect(() => () => timers.current.forEach(clearTimeout), []);

  const resolve = (k, ok, msg) => {
    const label = CMD_LABEL[k];
    setCmdSt((c) => ({ ...c, [k]: { st: ok ? "success" : "error", msg } }));
    setNote(ok ? { type: "ok", text: label } : { type: "err", text: label, msg });
    setSnack(ok ? { text: `Блок выполнил: ${label}` } : { text: `${msg}. Команда «${label}» не выполнена`, k });
    later(() => { setCmdSt((c) => { const n = { ...c }; delete n[k]; return n; }); setNote(null); }, ok ? 2600 : 4500);
    later(() => setSnack(null), 4500);
  };

  const doTick = () => {
    const prev = dRef.current;
    const q = queue.current; queue.current = [];
    const next = clone(prev);
    const fired = [];
    q.forEach((it) => {
      if (it.cmd && Date.now() - (it.at || 0) < 700) { queue.current.push(it); return; }
      if (it.cmd) { EFFECT[it.cmd](next); fired.push(it.cmd); }
      if (it.fn) it.fn(next);
    });
    if (next.engine.state !== "off" && next.engine.rpm) next.engine.rpm = 790 + Math.round(Math.random() * 50);
    const ch = {};
    ZONES.forEach((z) => { if (next.z[z.k] !== prev.z[z.k]) ch[z.k] = 1; });
    if (next.guard !== prev.guard) ch.guard = 1;
    if (next.engine.state !== prev.engine.state) ch.engine = 1;
    if (next.power.volt !== prev.power.volt) ch.volt = 1;
    dRef.current = next;
    setD(next); setChanged(ch); setLastAt(Date.now()); setNow(Date.now()); setTick((t) => t + 1);
    fired.forEach((k) => resolve(k, true));
    if (mode === "quick") {
      // Строгий порядок: отмечается только текущее (первое незакрытое) задание.
      const curT = tasks.find((t) => tsRef.current[t.id] === "pending");
      const met = curT && curT.type === "auto" && taskMet(curT, next) ? [curT] : [];
      if (met.length) {
        const upd = { ...tsRef.current }; met.forEach((t) => (upd[t.id] = "done"));
        tsRef.current = upd; setTs(upd); setToast({ id: met[met.length - 1].id, at: Date.now() });
      }
    }
  };

  useEffect(() => { const t = setInterval(() => setNow(Date.now()), 1000); return () => clearInterval(t); }, []);
  useEffect(() => {
    const t = setInterval(() => setD((x) => (x.engine.auto === "running" && x.engine.left > 0 ? { ...x, engine: { ...x.engine, left: x.engine.left - 1 } } : x)), 1000);
    return () => clearInterval(t);
  }, []);
  useEffect(() => {
    if (!loading) return;
    const t = setTimeout(() => { setLoading(false); setLastAt(Date.now()); }, 2800);
    return () => clearTimeout(t);
  }, [loading]);
  useEffect(() => {
    if (loading || stale || lost) return;
    const t = setInterval(doTick, 3000);
    return () => clearInterval(t);
  }, [loading, stale, lost]);
  useEffect(() => { if (!toast) return; const t = setTimeout(() => setToast(null), 3600); return () => clearTimeout(t); }, [toast]);
  useEffect(() => {
    if (!quickProg) return;
    const t = setTimeout(() => { queue.current.push({ fn: (x) => { x.prog = false; x.event = "Выход из режима программирования"; } }); setQuickProg(false); }, 4200);
    return () => clearTimeout(t);
  }, [quickProg]);

  const send = (c) => {
    if (cmdSt[c.k] && cmdSt[c.k].st === "sending") return;
    setCmdSt((s) => ({ ...s, [c.k]: { st: "sending" } }));
    setNote({ type: "sending", text: CMD_LABEL[c.k] });
    if (response === "ok") queue.current.push({ cmd: c.k, at: Date.now() });
    else if (response === "timeout") later(() => resolve(c.k, false, "Блок не ответил"), 5000);
    else later(() => resolve(c.k, false, "Нет связи с блоком"), 1500);
  };
  const fire = (c) => { if (c.k === "prog" && !dRef.current.prog) { setDialog({ type: "pin" }); return; } send(c); };
  const onZone = (k) => {
    if (loading) return;
    queue.current.push({ fn: (x) => {
      const z = ZMAP[k]; const v = x.z[k];
      let nv = z.cycle ? z.cycle[(z.cycle.indexOf(v) + 1) % z.cycle.length] : (v === "off" ? "on" : "off");
      if (x.guard !== "disarmed" && nv === "on" && !z.noAlarm) { nv = "alarm"; x.event = `Тревога: ${z.label.toLowerCase()}`; }
      else x.event = `${z.label}: ${String(z.vals[nv]).toLowerCase()}`;
      x.z[k] = nv;
    } });
  };

  const onZoneRef = useRef(null); onZoneRef.current = onZone;
  useEffect(() => { const h = (e) => onZoneRef.current && onZoneRef.current(e.detail); window.addEventListener("cs-demo", h); return () => window.removeEventListener("cs-demo", h); }, []);
  const back = () => {
    if (mode === "quick") return setDialog({ type: "back" });
    if (d.service || d.prog) return setDialog({ type: "exit" });
    onBack();
  };
  const next = () => {
    const left = tasks.filter((t) => ts[t.id] === "pending").length;
    if (left) setDialog({ type: "finish" }); else if (onDone) onDone(); else setSnack({ text: "Шаг «Проверка состояния» завершён" });
  };
  const skip = (id) => setTs((s) => ({ ...s, [id]: "skipped" }));
  const answer = (id, v) => setTs((s) => ({ ...s, [id]: v }));

  const secs = Math.max(0, Math.round((now - lastAt) / 1000));
  const cmds = commandsFor(d, mode);
  const alarm = !loading && isAlarm(d);
  const closed = tasks.filter((t) => ts[t.id] !== "pending").length;

  const banners = !loading && (
    <React.Fragment>
      {stale && <Banner tone="neutral" icon="sync_problem" title={`Данные не обновляются ${secs} с`} text="Связь с блоком есть, но он не присылает состояние. Подождите или поднесите телефон ближе." />}
      {quickProg && <Banner busy title="Необходимо выйти из режима программирования" text="Перезагрузка системы" />}
      {d.service && <Banner icon="build" title="Режим ТО включён" text="Блок не охраняет" action="Выключить" onAction={() => send({ k: "service" })} />}
      {d.prog && !quickProg && <Banner icon="developer_board" title="Режим программирования" text="Блок не охраняет" action="Выключить" onAction={() => send({ k: "prog" })} />}
    </React.Fragment>
  );
  const hero = <Hero d={d} loading={loading} stale={stale} secs={secs} tick={tick} flash={!!changed.guard} />;

  const zones = loading ? (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {[6, 5, 4].map((n, i) => (
        <Card key={i}><div style={{ display: "flex", flexDirection: "column", gap: 10 }}><Skel h={22} w="45%" />{Array.from({ length: n }).map((_, j) => <div key={j} style={{ display: "flex", alignItems: "center", gap: 12 }}><Skel h={44} w={44} r={999} /><Skel h={20} style={{ flex: 1 }} /><Skel h={32} w={84} r={16} /></div>)}</div></Card>
      ))}
    </div>
  ) : zoneView === "scheme"
    ? <ZonesScheme d={d} changed={changed} tick={tick} onZone={onZone} wide={wide && width > 1100} />
    : <ZonesList d={d} changed={changed} tick={tick} onZone={onZone} wide={wide && width > 1100} />;

  const stateCol = (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {zones}
      {!loading && <EngineCard d={d} tick={tick} changed={changed} />}
      {!loading && <LinkCard d={d} />}
      {!loading && <ExtraGroups d={d} initialOpen={preset === "S9" ? { can: true, ev: true, heater: true } : undefined} />}
    </div>
  );
  const commands = <div ref={cmdRef}><CommandsPanel cmds={cmds} cmdSt={cmdSt} disabled={loading} onFire={fire} onDanger={(c) => setSheet(c.k)} /></div>;
  const scrollRef = useRef(null);
  const goAll = () => setAllOpen(true);
  const pult = <Pult d={d} changed={changed} tick={tick} cmds={cmds} cmdSt={cmdSt} loading={loading} onFire={fire} onDanger={(c) => setSheet(c.k)} onAll={goAll} carW={wide ? 220 : 196} />;
  const checklist = hasChecks && <Checklist tasks={tasks} ts={ts} d={d} changed={changed} tick={tick} onSkip={skip} onAnswer={answer} bare={!wide || L2 === "split" || L2 === "low"} seq={mode === "quick"} />;
  const dock = mode === "quick" && (
    <div style={{ padding: "12px 14px 14px", background: "var(--background-surface-ultra-highlight)", borderTop: "1px solid var(--border-highlight)" }}>
      <CmpButton text={hasChecks && closed < tasks.length ? `Далее · пройдено ${closed} из ${tasks.length}` : "Далее"} icon="arrow_forward" fullWidth onClick={next} style={BIG} />
    </div>
  );

  const toastTask = toast && tasks.find((t) => t.id === toast.id);
  const toastEl = toastTask && (
    <div key={toast.id + (toast.at || "")} className="cs-anim" style={{ position: "absolute", left: 12, right: 12, top: 8, zIndex: 6, display: "flex", alignItems: "center", gap: 12, padding: "12px 14px", borderRadius: "var(--r-card-medium-radius)", background: "var(--inverse-surface)", color: "var(--inverse-on-surface)", boxShadow: "var(--elevation-3)", animation: "csIn .3s ease both", maxWidth: 520, margin: "0 auto" }}>
      <span style={{ width: 40, height: 40, borderRadius: 999, background: "var(--success)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon="check" size={26} color="#fff" /></span>
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="titleMedium" weight={700} color="var(--inverse-on-surface)" style={{ display: "block" }}>Проверка пройдена</CmpText>
        <CmpText variant="bodyMedium" color="var(--inverse-on-surface)">{toastTask.text}</CmpText>
      </div>
      {!wide && tab !== "checks" && <button type="button" onClick={() => { setTab("checks"); setToast(null); }} style={{ minHeight: 48, padding: "0 12px", border: "none", background: "transparent", color: "var(--inverse-primary)", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, cursor: "pointer" }}>К списку</button>}
    </div>
  );

  let body;
  const onDanger = (c) => setSheet(c.k);
  const pultKeys = ["arm", "disarm", d.engine.state === "off" ? "start" : "stop", "trunk", "panic"].map((k) => cmds.find((c) => c.k === k)).filter(Boolean);
  const squares = (cols) => (
    <div style={{ display: "grid", gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`, gap: 8 }}>
      {pultKeys.map((c) => <CmdSquare key={c.k} c={c} st={cmdSt[c.k]} disabled={loading} onFire={fire} onDanger={onDanger} />)}
      <button type="button" onClick={goAll} style={{ height: 72, border: "1.5px solid var(--outline-variant)", borderRadius: "var(--r-card-medium-radius)", background: "transparent", color: "var(--primary)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2, fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, cursor: "pointer" }}>
        <CmpIcon icon="apps" size={30} color="var(--primary)" />Все команды
      </button>
    </div>
  );
  const curTask = hasChecks && tasks.find((t) => ts[t.id] === "pending");
  const nextBtn = (full) => <CmpButton text={hasChecks && closed < tasks.length ? `Далее · ${closed} из ${tasks.length}` : "Далее"} icon="arrow_forward" fullWidth={full} onClick={next} style={BIG} />;
  const details = (min) => loading ? <Skel h={160} /> : (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <ZonesGrid d={d} changed={changed} tick={tick} onZone={onZone} min={min} />
      <EngineCard d={d} tick={tick} changed={changed} />
      <LinkCard d={d} />
      <ExtraGroups d={d} />
    </div>
  );
  const scrollCol = (children, extra) => <div style={{ minHeight: 0, overflow: "auto", display: "flex", flexDirection: "column", gap: 12, ...(extra && extra.padding ? null : { paddingBottom: 16 }), ...extra }}>{children}</div>;

  if (L2 === "low") {
    // Телефон-ландшафт: высоты мало — без карточки статуса и вкладок; всё главное без прокрутки.
    const rightW = Math.min(420, Math.round(width * 0.44));
    body = (
      <div style={{ flex: 1, minHeight: 0, display: "grid", gridTemplateColumns: `minmax(0, 1fr) ${rightW}px`, gap: 12, padding: "8px 12px 12px" }}>
        {scrollCol(<React.Fragment>
          {banners}
          <div style={{ opacity: loading ? 0.3 : 1 }}><VehicleH d={loading ? baseData() : d} height={Math.max(110, Math.min(150, height - 250))} /></div>
          {loading ? <Skel h={36} w="70%" r={12} /> : <ActiveChips d={d} changed={changed} tick={tick} />}
          {!loading && <MetricStrip d={d} changed={changed} tick={tick} />}
        </React.Fragment>, { paddingBottom: 4 })}
        <div style={{ position: "relative", minHeight: 0, display: "flex", flexDirection: "column", gap: 8 }}>
          {toastEl}
          {hasChecks ? (
            <React.Fragment>
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <CmpText variant="titleMedium" weight={700} style={{ flex: 1 }}>{closed === tasks.length ? "Все проверки пройдены" : `Проверка ${closed + 1} из ${tasks.length}`}</CmpText>
                <CsTextBtn text="Весь список" icon="checklist" onClick={() => setChecksOpen(true)} style={{ height: 48 }} />
              </div>
              <CmpLinearProgressIndicator progress={closed / tasks.length} height={6} />
              <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
                {curTask ? <TaskRow t={curTask} st={ts[curTask.id]} d={d} changed={changed} tick={tick} n={tasks.indexOf(curTask) + 1} onSkip={skip} onAnswer={answer} current /> : <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Нажмите «Далее», чтобы завершить шаг.</CmpText>}
              </div>
              {nextBtn(true)}
            </React.Fragment>
          ) : (
            <React.Fragment>
              <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{squares(3)}</div>
              <CmpOutlinedButton text="Подробно: зоны, двигатель, связь" icon="list" fullWidth onClick={() => setDetailsOpen(true)} style={{ height: 48 }} />
            </React.Fragment>
          )}
        </div>
      </div>
    );
  } else if (L2 === "split") {
    // Раскладной (и планшет-портрет): две равные панели, граница — по сгибу.
    const tabsR = hasChecks
      ? [{ k: "checks", title: `Проверки ${closed}/${tasks.length}` }, { k: "cmds", title: "Команды" }, { k: "details", title: "Подробно" }]
      : [{ k: "cmds", title: "Команды" }, { k: "details", title: "Подробно" }];
    const pv = pane && tabsR.some((t) => t.k === pane) ? pane : tabsR[0].k;
    const half = hingeAt || width / 2;
    body = (
      <React.Fragment>
        <div style={{ flex: 1, minHeight: 0, display: "grid", gridTemplateColumns: `${half}px minmax(0, 1fr)` }}>
          {scrollCol(<React.Fragment>
            {hero}{banners}
            <Card pad={12}>
              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                <div style={{ opacity: loading ? 0.3 : 1 }}><Vehicle d={loading ? baseData() : d} width={Math.min(200, half * 0.46)} /></div>
                {loading ? <Skel h={36} w="70%" r={12} /> : <ActiveChips d={d} changed={changed} tick={tick} />}
                {!loading && <MetricStrip d={d} changed={changed} tick={tick} wrap />}
              </div>
            </Card>
          </React.Fragment>, { padding: "12px 16px 16px", borderRight: "1px solid var(--border-highlight)" })}
          <div style={{ position: "relative", minHeight: 0, display: "flex", flexDirection: "column" }}>
            <PaneTabs tabs={tabsR} value={pv} onChange={setPane} />
            {toastEl}
            {scrollCol(pv === "checks" ? checklist : pv === "cmds" ? <CommandsPanel bare cols={half >= 400 ? 2 : 1} cmds={cmds} cmdSt={cmdSt} disabled={loading} onFire={fire} onDanger={onDanger} /> : details(130), { padding: "12px 16px 16px" })}
          </div>
        </div>
        {dock && <div style={{ padding: "12px 16px 16px", background: "var(--background-surface-ultra-highlight)", borderTop: "1px solid var(--border-highlight)", display: "grid", gridTemplateColumns: `${half}px minmax(0, 1fr)` }}><span /><div style={{ paddingLeft: 16 }}>{nextBtn(true)}</div></div>}
      </React.Fragment>
    );
  } else if (L2 === "desk") {
    // Планшет-ландшафт: машина всегда видна слева, зоны — в центре без вкладок, справа команды или чек-лист.
    const leftW = width >= 1240 ? 340 : 300, rightW = width >= 1240 ? 400 : 340;
    body = (
      <React.Fragment>
        <div style={{ flex: 1, minHeight: 0, display: "grid", gridTemplateColumns: `${leftW}px minmax(0, 1fr) ${rightW}px`, gap: 16, padding: "12px 16px 0", maxWidth: 1600, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
          {scrollCol(<React.Fragment>
            {hero}{banners}
            <Card pad={12}>
              <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
                <div style={{ opacity: loading ? 0.3 : 1 }}><Vehicle d={loading ? baseData() : d} width={170} /></div>
                {!loading && <MetricStrip d={d} changed={changed} tick={tick} wrap />}
              </div>
            </Card>
            {hasChecks && <CmpOutlinedButton text="Команды блоку" icon="apps" fullWidth onClick={goAll} style={{ height: 56 }} />}
          </React.Fragment>)}
          {scrollCol(<React.Fragment>
            {loading ? <Skel h={36} w="70%" r={12} /> : <Card pad={12}><ActiveChips d={d} changed={changed} tick={tick} /></Card>}
            {details(150)}
          </React.Fragment>)}
          <div style={{ position: "relative", minHeight: 0, display: "flex", flexDirection: "column" }}>
            {toastEl}
            {scrollCol(checklist || <Card title="Команды"><CommandsPanel bare cols={1} cmds={cmds} cmdSt={cmdSt} disabled={loading} onFire={fire} onDanger={onDanger} /></Card>)}
          </div>
        </div>
        {dock && <div style={{ padding: "12px 16px 16px", background: "var(--background-surface-ultra-highlight)", borderTop: "1px solid var(--border-highlight)", display: "flex", justifyContent: "flex-end" }}><div style={{ width: rightW }}>{nextBtn(true)}</div></div>}
      </React.Fragment>
    );
  } else if (wide) {
    // Широкое окно: слева — статус, горизонтальный пульт и «Подробно» вкладками; справа — чек-лист (быстрый монтаж) или все команды.
    const rightW = width >= 1200 ? 440 : width >= 960 ? 380 : 340;
    const pultW = <PultWide d={d} changed={changed} tick={tick} cmds={cmds} cmdSt={cmdSt} loading={loading} onFire={fire} onDanger={(c) => setSheet(c.k)} onAll={goAll} carH={width >= 1100 ? 180 : 150} />;
    body = (
      <React.Fragment>
        <div style={{ flex: 1, minHeight: 0, display: "grid", gridTemplateColumns: `minmax(0, 1fr) ${rightW}px`, gap: 16, padding: "12px 16px 0", maxWidth: 1400, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
          <div ref={scrollRef} style={{ minHeight: 0, overflow: "auto", display: "grid", alignContent: "start", gridAutoRows: "max-content", gridTemplateColumns: "minmax(0, 1fr)", gap: 12, paddingBottom: 16 }}>{hero}{banners}{pultW}<DetailsWide d={d} changed={changed} tick={tick} onZone={onZone} loading={loading} /></div>
          <div style={{ position: "relative", minHeight: 0, display: "flex", flexDirection: "column" }}>
            {toastEl}
            <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "grid", alignContent: "start", gridAutoRows: "max-content", gridTemplateColumns: "minmax(0, 1fr)", gap: 12, paddingBottom: 16 }}>
              {checklist || <div ref={cmdRef}><CommandsPanel cmds={cmds} cmdSt={cmdSt} disabled={loading} onFire={fire} onDanger={(c) => setSheet(c.k)} cols={rightW >= 400 ? 2 : 1} /></div>}
            </div>
          </div>
        </div>
        {dock && (
          <div style={{ padding: "12px 16px 16px", background: "var(--background-surface-ultra-highlight)", borderTop: "1px solid var(--border-highlight)", display: "flex", justifyContent: "flex-end" }}>
            <div style={{ width: "100%", maxWidth: 400 }}><CmpButton text={hasChecks && closed < tasks.length ? `Далее · пройдено ${closed} из ${tasks.length}` : "Далее"} icon="arrow_forward" fullWidth onClick={next} style={BIG} /></div>
          </div>
        )}
      </React.Fragment>
    );
  } else {
    const tabs = [...(hasChecks ? [{ k: "checks", title: `Проверки ${closed}/${tasks.length}` }] : []), { k: "pult", title: "Пульт" }, { k: "details", title: "Подробно" }];
    const idx = Math.max(0, tabs.findIndex((t) => t.k === tab));
    body = (
      <React.Fragment>
        <div style={{ padding: "2px 14px 4px", display: "flex", flexDirection: "column", gap: 6, background: "var(--background)" }}>{hero}{banners}</div>
        <CmpTabRow tabs={tabs.map((t) => ({ title: t.title }))} selectedTabIndex={idx} onTabSelected={(i) => setTab(tabs[i].k)} />
        <div style={{ position: "relative", flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
          {toastEl}
          <div ref={scrollRef} style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
            <div style={{ padding: 14, display: "flex", flexDirection: "column", gap: 12 }}>
              {tabs[idx].k === "checks" ? checklist : tabs[idx].k === "pult" ? pult : <React.Fragment>{stateCol}{commands}</React.Fragment>}
            </div>
          </div>
        </div>
        {dock}
      </React.Fragment>
    );
  }

  const both = d.service && d.prog;
  const modeName = both ? "режиме ТО и программирования" : d.service ? "режиме ТО" : "режиме программирования";

  return (
    <div ref={rootRef} data-screen-label={mode === "quick" ? "Проверка состояния · быстрый монтаж" : "Проверка состояния"} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      {L2 === "low" ? (
        <div style={{ flex: "0 0 auto", height: 56, display: "flex", alignItems: "center", gap: 8, padding: "0 12px 0 4px", borderBottom: "1px solid var(--border-highlight)" }}>
          <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={back} />
          <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
            <CmpText variant="titleMedium" weight={600} maxLines={1}>Проверка состояния</CmpText>
            <CmpText variant="bodySmall" color="var(--on-surface-variant)" maxLines={1}>{device.name} · ID {device.id}</CmpText>
          </div>
          <StatusPill d={d} loading={loading} stale={stale} secs={secs} />
        </div>
      ) : <CmpTopAppBar title="Проверка состояния" subtitle={`${device.name} · ID ${device.id}`} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={back} />} />}
      {body}
      {detailsOpen && <AllCommandsSheet title="Подробно" onClose={() => setDetailsOpen(false)}>{details(140)}</AllCommandsSheet>}
      {checksOpen && <AllCommandsSheet title="Проверки" onClose={() => setChecksOpen(false)}>{checklist}</AllCommandsSheet>}
      {alarm && <span className="cs-anim" style={{ position: "absolute", inset: 0, zIndex: 7, border: "6px solid var(--error)", pointerEvents: "none", animation: "csBorder 1s ease-in-out infinite" }} />}
      {snack && (
        <div style={{ position: "absolute", left: 12, right: 12, bottom: mode === "quick" && L2 !== "low" ? 96 : 16, zIndex: 7, display: "flex", justifyContent: "center" }}>
          <CmpSnackbar message={snack.text} actionLabel={snack.k ? "Повторить" : undefined} onActionClick={() => { const k = snack.k; setSnack(null); send({ k }); }} style={{ width: "100%", borderRadius: "var(--r-card-small-radius)", minHeight: 56 }} />
        </div>
      )}
      {allOpen && <AllCommandsSheet onClose={() => setAllOpen(false)}><CommandsPanel bare cols={wide ? 3 : 2} cmds={cmds} cmdSt={cmdSt} disabled={loading} onFire={fire} onDanger={(c) => setSheet(c.k)} /></AllCommandsSheet>}
      {sheet && <DangerSheet k={sheet} d={d} onCancel={() => setSheet(null)} onConfirm={() => { const k = sheet; setSheet(null); send({ k }); }} />}
      {dialog && dialog.type === "exit" && (
        <Modal icon="warning" tone="warn" title="Выйти из проверки?" text={`Блок в ${modeName} — он не будет охранять. Выключить?`} onDismiss={() => setDialog(null)}
          actions={<>
            <CmpButton text="Выключить и выйти" fullWidth style={BIG} onClick={() => { setDialog(null); if (d.service) send({ k: "service" }); if (d.prog) send({ k: "prog" }); later(onBack, 600); }} />
            <CmpOutlinedButton text={both ? "Оставить включёнными" : "Оставить включённым"} fullWidth style={BIG} onClick={() => { setDialog(null); onBack(); }} />
            <CsTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={() => setDialog(null)} />
          </>} />
      )}
      {dialog && dialog.type === "pin" && (
        <Modal icon="pin" title="Режим программирования" text="Введите сервисный PIN блока." onDismiss={() => setDialog(null)}
          actions={<>
            <CmpButton text="Включить" fullWidth style={BIG} onClick={() => { setDialog(null); send({ k: "prog" }); }} />
            <CsTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={() => setDialog(null)} />
          </>}><PinBody /></Modal>
      )}
      {dialog && dialog.type === "finish" && (
        <Modal icon="playlist_add_check" tone="warn" title="Не все проверки пройдены. Завершить?" text={`Не пройдено: ${tasks.filter((t) => ts[t.id] === "pending").length} из ${tasks.length}.`} onDismiss={() => setDialog(null)}
          actions={<>
            <CmpButton text="Продолжить проверку" fullWidth style={BIG} onClick={() => { setDialog(null); setTab("checks"); }} />
            <CmpOutlinedButton text="Завершить" fullWidth style={BIG} onClick={() => { setDialog(null); if (onDone) onDone(); else setSnack({ text: "Шаг «Проверка состояния» завершён" }); }} />
          </>} />
      )}
      {dialog && dialog.type === "back" && (
        <Modal icon="undo" title="Вернуться на предыдущий шаг?" text="Проверка состояния будет прервана." onDismiss={() => setDialog(null)}
          actions={<>
            <CmpButton text="Вернуться" fullWidth style={BIG} onClick={() => { setDialog(null); onBack(); }} />
            <CsTextBtn text="Отмена" style={{ ...BIG, width: "100%" }} onClick={() => setDialog(null)} />
          </>} />
      )}
      {lost && <LostOverlay onBack={onBack} />}
    </div>
  );
}

// ── Демо-панель (общая для mobile/tablet) ────────────────────────────────────
const CS_ADV = [["S1", "S1 Загрузка"], ["S2", "S2 Норма"], ["S3", "S3 Изменения"], ["S4", "S4 На охране"], ["S5", "S5 Тревога"], ["S6", "S6 Режим ТО"], ["S6p", "S6 Программирование"], ["S7", "S7 Автозапуск"], ["S8", "S8 Минимум данных"], ["S9", "S9 Максимум данных"], ["S10", "S10 Данные устарели"], ["S11", "S11 Команда"], ["S12", "S12 Опасная команда"], ["S13", "S13 PIN"], ["S14", "S14 Выход в режиме ТО"], ["S15", "S15 Потеря связи"]];
const CS_QUICK = [["F1", "F1 Начало"], ["F2", "F2 Авто-проверка прошла"], ["F3", "F3 Ручной вопрос"], ["F4", "F4 Всё пройдено"], ["F5", "F5 «Далее» не всё"], ["F6", "F6 Заданий нет"], ["FP", "Выход из программирования"]];
function CsSeg({ items, value, onChange, wrap }) {
  return <div className={"seg" + (wrap ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
function useCsCtl() {
  const q = new URLSearchParams(location.search);
  const [dark, setDark] = React.useState(q.get("theme") === "dark");
  const [mode, setModeRaw] = React.useState(q.get("mode") || "advanced");
  const [preset, setPreset] = React.useState(q.get("state") || ((q.get("mode") || "advanced") === "quick" ? "F1" : "S2"));
  const [view, setView] = React.useState(q.get("zones") || "list");
  const [resp, setResp] = React.useState("ok");
  React.useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const setMode = (m) => { setModeRaw(m); setPreset(m === "quick" ? "F1" : "S2"); };
  const bar = (
    <React.Fragment>
      <div className="toolbar">
        <CsSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(v) => setDark(v === "d")} />
        <CsSeg items={[["advanced", "Расширенный монтаж"], ["quick", "Быстрый монтаж"]]} value={mode} onChange={setMode} />
        <CsSeg items={[["list", "Подробно: A · список"], ["scheme", "Подробно: B · плитки"]]} value={view} onChange={setView} />
        <CsSeg items={[["ok", "Блок: отвечает"], ["timeout", "Не отвечает"], ["nolink", "Нет связи"]]} value={resp} onChange={setResp} />
      </div>
      <div className="toolbar"><CsSeg wrap items={mode === "quick" ? CS_QUICK : CS_ADV} value={preset} onChange={setPreset} /></div>
      <div className="toolbar"><span className="hint">Демо — действие в машине (блок увидит его на следующем обновлении, до 3 с):</span><div className="seg">{[["door_fl","Водит. дверь"],["hood","Капот"],["trunk","Багажник"],["ign","Зажигание"],["brake","Тормоз"],["shock","Удар"]].map(([k,l]) => <button key={k} onClick={() => window.dispatchEvent(new CustomEvent("cs-demo", { detail: k }))}>{l}</button>)}</div></div>
    </React.Fragment>
  );
  return { dark, props: { mode, preset, zoneView: view, response: resp }, bar, key: [mode, preset].join() };
}

Object.assign(window, {
  CheckStateScreen, useCsCtl, CsSeg,
  CS_ZONES: ZONES, CS_ZoneRow: ZoneRow, CS_ZoneTile: ZoneTile, CS_DoorChip: DoorChip, CS_CmdView: CmdView, CS_Hero: Hero, CS_TaskRow: TaskRow, CS_TASKS: TASKS, CS_baseData: baseData, CS_ValuePill: ValuePill, CS_BodyScheme: BodyScheme, CS_Vehicle: Vehicle, CS_Pult: Pult, CS_CmdSquare: CmdSquare, CS_ExtraGroups: ExtraGroups, CS_maxData: maxData,
});
