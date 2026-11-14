// Ассистент монтажа — общий набор блоков ленты и демо-оболочек (window.AsstKit).
// Экран «Пульт» — Assistant.jsx.

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpText, CmpIcon, CmpButton, CmpOutlinedButton, CmpTextButton, CmpIconButton, CmpSurface, CmpTopAppBar, CmpTextField, CmpSwitch, CmpRadioButton, CmpLinearProgressIndicator, CmpDivider } = DS;

(function injectStyles() {
  if (document.getElementById("asst-css")) return;
  const s = document.createElement("style");
  s.id = "asst-css";
  s.textContent = `
:root{--asst-card:var(--surface-container-lowest)}
[data-theme="dark"]{--asst-card:var(--surface-container-high)}
@keyframes asstIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
@keyframes asstSk{0%,100%{opacity:.5}50%{opacity:1}}
@keyframes asstDot{0%,80%,100%{opacity:.25;transform:translateY(0)}40%{opacity:1;transform:translateY(-3px)}}
.asst-in{animation:asstIn .28s cubic-bezier(.2,0,0,1) both}
.asst-sk{animation:asstSk 1.4s ease-in-out infinite}
.asst-dot{animation:asstDot 1.2s ease-in-out infinite}
.asst-f:focus-visible{outline:2px solid var(--primary);outline-offset:2px}
.asst-nosb{scrollbar-width:none}.asst-nosb::-webkit-scrollbar{display:none}
.asst-mark{background:var(--primary-container);color:var(--on-primary-container);border-radius:4px;padding:0 2px}
@media (prefers-reduced-motion:reduce){.asst-in,.asst-sk,.asst-dot{animation:none!important}}`;
  document.head.appendChild(s);
})();

const { useState, useEffect, useRef } = React;

// ── Каталог действий ─────────────────────────────────────────────────────────
// can — нужна обновлённая CAN-прошивка; net — нужен интернет; ble — только по Bluetooth.
const ACT = {
  fw:           { label: "Проверить прошивку", icon: "system_update", net: 1 },
  state:        { label: "Состояние блока", icon: "speed", ble: 1 },
  find:         { label: "Найти настройку", icon: "search", can: 1 },
  load:         { label: "Загрузить настройки", icon: "file_download", can: 1 },
  bypass:       { label: "Обход и Clone", icon: "key", can: 1 },
  archive:      { label: "Архив прошивок", icon: "inventory_2", net: 1 },
  cars:         { label: "Автомобили", icon: "directions_car" },
  support:      { label: "Чат поддержки", icon: "support_agent" },
  fw_update:    { label: "Обновить", icon: "system_update_alt", net: 1 },
  state_more:   { label: "Подробнее", icon: "open_in_new", ble: 1 },
  bypass_run:   { label: "Запустить обход", icon: "key", can: 1 },
  clone_run:    { label: "Запустить Clone", icon: "content_copy", can: 1, net: 1 },
  all_settings: { label: "Все настройки", icon: "tune", can: 1 },
  can_update:   { label: "Обновить CAN", icon: "system_update", net: 1 },
  find_more:    { label: "Найти другую настройку", icon: "search", can: 1 },
  open_setting: { label: "Открыть в настройках", icon: "open_in_new", can: 1 },
};
const GREET = ["fw", "state", "find", "load", "bypass", "archive", "cars", "support"];
const QUICK = ["fw", "find", "state", "load", "bypass", "support"];
const UNSUP_OK = ["fw", "archive", "fw_update", "cars", "support"];

const PROFILES = {
  ble: { model: "Pandora DX-4GS", id: "107131", transport: "ble", can: "12.05", canCode: "VWPOLO20" },
  usb: { model: "Pandora DX-4GS", id: "107131", transport: "usb", can: "12.05", canCode: "VWPOLO20" },
  unsupported: { model: "Pandora RMD-4", id: "107131320", transport: "usb", can: null, unsupported: true },
};

const SETTINGS = [
  { id: "as_temp", name: "Автозапуск по температуре", path: "Автозапуск › Условия запуска", type: "switch", value: false },
  { id: "as_thr", name: "Температура для автозапуска", path: "Автозапуск › Условия запуска", type: "number", value: -15, unit: "°C", min: -40, max: 0, step: 1 },
  { id: "as_dur", name: "Длительность прогрева двигателя при автозапуске по температуре", path: "Автозапуск › Прогрев", type: "list", value: "10 мин", options: ["5 мин", "10 мин", "15 мин", "20 мин", "30 мин"] },
  { id: "sens_t", name: "Источник температуры двигателя", path: "Датчики › Температура", type: "list", value: "CAN-шина", options: ["CAN-шина", "Внешний датчик", "Не используется"] },
  { id: "cab_t", name: "Расписание прогрева по температуре салона", path: "Автозапуск › Расписание", type: "other", value: "2 правила" },
  { id: "t_off", name: "Поправка датчика температуры", path: "Датчики › Температура", type: "number", value: 0, unit: "°C", min: -10, max: 10, step: 1 },
  { id: "siren", name: "Сирена при постановке в охрану", path: "Охрана › Оповещение", type: "switch", value: true },
  { id: "siren_vol", name: "Громкость сирены", path: "Охрана › Оповещение", type: "list", value: "Высокая", options: ["Тихая", "Средняя", "Высокая"] },
  { id: "door_lock", name: "Закрывать двери при начале движения", path: "Комфорт › Замки", type: "switch", value: false },
  { id: "turbo", name: "Турботаймер", path: "Двигатель › Остановка", type: "number", value: 2, unit: "мин", min: 0, max: 10, step: 1 },
  { id: "byp_mode", name: "Режим бесключевого обхода", path: "Обход › Бесключевой обход", type: "list", value: "По метке", options: ["По метке", "Свободные руки", "Выключен"] },
  { id: "byp_as", name: "Обход иммобилайзера при автозапуске", path: "Обход › Бесключевой обход", type: "switch", value: true },
];
const SBY = Object.fromEntries(SETTINGS.map((s) => [s.id, s]));
const fmt = (s, v) => s.type === "switch" ? (v ? "Вкл" : "Выкл") : s.type === "number" ? `${v > 0 && s.min < 0 ? "+" : ""}${v} ${s.unit}`.replace("-", "−") : String(v);
const findSettings = (q) => { const st = q.toLowerCase().split(/[\s,.;:!?«»"]+/).filter((w) => w.length >= 3).map((w) => w.slice(0, Math.min(w.length, 6))); if (!st.length) return []; return SETTINGS.map((s) => [s, st.filter((x) => (s.name + " " + s.path).toLowerCase().includes(x)).length]).filter(([, n]) => n > 0).sort((x, y) => y[1] - x[1]).map(([s]) => s); };

const SOURCES = [
  { k: "file", icon: "description", title: "Файл .dxs", sub: "С телефона, из загрузок или облачного диска" },
  { k: "mine", icon: "folder_special", title: "Мои конфигурации", sub: "Сохранённые для DX-4GS" },
  { k: "cloud", icon: "cloud_download", title: "Облако по автомобилю", sub: "Проверенные конфигурации для VW Polo 2020", net: 1 },
  { k: "factory", icon: "restart_alt", title: "Заводские настройки", sub: "Значения по умолчанию для этой прошивки" },
];
const SRC_TXT = { file: "Файл «polo_2020_base.dxs». Изменено 14 настроек.", mine: "Мои конфигурации: «Polo 2020 база». Изменено 14 настроек.", cloud: "Облако: VW Polo 2020, конфигурация № 12. Изменено 22 настройки.", factory: "Заводские значения для прошивки 3.57. Изменено 31 настройка." };

function makeAvail({ profile, canF, online }) {
  return (k) => {
    const a = ACT[k];
    if (!a) return { ok: true };
    if (PROFILES[profile].unsupported && !UNSUP_OK.includes(k)) return { ok: false, hidden: true };
    if (a.ble && PROFILES[profile].transport !== "ble") return { ok: false, hidden: true };
    if (a.can && canF) return { ok: false, reason: "Нужно обновить CAN", full: "Заводской CAN. Обновите CAN, чтобы открыть настройки, обход и Clone.", fix: "can_update" };
    if (a.net && !online) return { ok: false, reason: "Нет сети", full: "Нет сети. Подключитесь к интернету, чтобы проверять прошивки и загружать облачные конфигурации." };
    return { ok: true };
  };
}
// Разделы по запросу: основы слов → действия раздела. «бесключевой обход», «обход», «clone» → Обход и Clone.
const SECT_KW = [
  [["обход", "бесключ", "иммоб", "clone", "клон", "ключ"], ["bypass", "bypass_run", "clone_run"]],
  [["прошив", "обнов", "верси", "firmware", "загрузч"], ["fw", "archive"]],
  [["состоян", "датчик", "двер", "капот", "напряж"], ["state", "state_more"]],
  [["загруз", "конфиг", "dxs"], ["load"]],
  [["автомоб", "машин", "марк"], ["cars"]],
  [["поддерж", "чат", "помощ", "специал"], ["support"]],
];
const matchSections = (q) => { const t = q.trim().toLowerCase(); if (!t) return []; const out = []; SECT_KW.forEach(([kw, ks]) => { if (kw.some((w) => t.includes(w))) ks.forEach((k) => !out.includes(k) && out.push(k)); }); GREET.forEach((k) => { if (!out.includes(k) && ACT[k].label.toLowerCase().includes(t)) out.push(k); }); return out; };
const norm = (i) => typeof i === "string" ? { k: i, ...ACT[i] } : { ...ACT[i.k], ...i };

// ── Утилиты ──────────────────────────────────────────────────────────────────
function useLayer() {
  const [h, setH] = useState(false), [p, setP] = useState(false), [f, setF] = useState(false);
  return {
    opacity: p ? 0.12 : f ? 0.1 : h ? 0.08 : 0,
    handlers: { onMouseEnter: () => setH(true), onMouseLeave: () => { setH(false); setP(false); }, onMouseDown: () => setP(true), onMouseUp: () => setP(false), onFocus: () => setF(true), onBlur: () => setF(false) },
  };
}
function useWidth(force) {
  const ref = useRef(null);
  const [w, setW] = useState(0);
  useEffect(() => {
    const el = ref.current; if (!el) return;
    const m = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(m); ro.observe(el); m();
    return () => ro.disconnect();
  }, []);
  return [ref, force || (w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact")];
}
function Hl({ text, q }) {
  if (!q) return text;
  const i = text.toLowerCase().indexOf(q.toLowerCase());
  if (i < 0) return text;
  return <>{text.slice(0, i)}<mark className="asst-mark" style={{ color: "inherit" }}>{text.slice(i, i + q.length)}</mark>{text.slice(i + q.length)}</>;
}

// ── Признак ассистента ───────────────────────────────────────────────────────
// Метка — молния в круге primary. Используется в рейке (A) и в заголовке шага (B).
function Mark({ size = 32 }) {
  return (
    <span aria-hidden="true" style={{ width: size, height: size, borderRadius: 999, background: "var(--primary)", color: "var(--on-primary)", display: "grid", placeItems: "center", flex: "0 0 auto" }}>
      <CmpIcon icon="bolt" size={Math.round(size * 0.62)} />
    </span>
  );
}
// Иконка раздела (duotone, как в сетке расширенного монтажа).
function AssistantIcon({ size = 32 }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" width={size} height={size} fill="currentColor" style={{ flex: "0 0 auto" }}>
      <rect opacity=".3" x="2" y="2" width="20" height="20" rx="6" />
      <path d="M11 21h-1l1-7H7.5c-.58 0-.57-.32-.38-.66.19-.34.05-.08.07-.12C8.48 10.94 10.42 7.54 13 3h1l-1 7h3.5c.49 0 .56.33.47.51l-.07.15C12.96 17.55 11 21 11 21z" />
    </svg>
  );
}

// ── Чип-действие ─────────────────────────────────────────────────────────────
// state: idle | selected | used | blocked. Заблокированный — пунктир, замок, причина второй строкой.
function Chip({ label, icon, primary, state = "idle", reason, onClick, nowrap }) {
  const L = useLayer();
  const blocked = state === "blocked", sel = state === "selected", used = state === "used";
  const prim = primary && state === "idle";
  const bg = prim ? "var(--primary)" : sel ? "var(--secondary-container)" : blocked ? "var(--surface-container)" : "var(--asst-card)";
  const fg = prim ? "var(--on-primary)" : sel ? "var(--on-secondary-container)" : blocked ? "var(--on-surface-variant)" : "var(--on-surface)";
  const border = prim || sel ? "1px solid transparent" : blocked ? "1px dashed var(--outline)" : "1px solid var(--outline)";
  const ic = sel ? "check" : blocked ? "lock" : icon;
  const icColor = prim || sel ? "currentColor" : blocked ? "var(--on-surface-variant)" : "var(--primary)";
  const live = (state === "idle" || blocked) && !!onClick;
  return (
    <button type="button" className="asst-f" disabled={!live} aria-disabled={blocked || undefined} onClick={live ? onClick : undefined} {...(live ? L.handlers : {})}
      style={{ position: "relative", overflow: "hidden", display: "inline-flex", alignItems: "center", gap: 8, minHeight: 48, padding: blocked ? "6px 16px 6px 12px" : "0 16px 0 12px", borderRadius: 12, background: bg, color: fg, border, boxSizing: "border-box", cursor: live ? "pointer" : "default", opacity: used ? 0.38 : 1, fontFamily: "var(--font-sans)", textAlign: "left", maxWidth: "100%", flex: "0 0 auto", whiteSpace: nowrap ? "nowrap" : "normal", transition: "background var(--motion-fast), opacity var(--motion-fast)" }}>
      <span style={{ position: "absolute", inset: 0, background: prim ? "var(--on-primary)" : "var(--on-surface)", opacity: L.opacity, pointerEvents: "none" }} />
      {ic && <CmpIcon icon={ic} size={20} color={icColor} />}
      <span style={{ display: "flex", flexDirection: "column", minWidth: 0, position: "relative" }}>
        <span style={{ fontSize: "var(--body-large-size)", lineHeight: "var(--body-large-line)", fontWeight: 500 }}>{label}</span>
        {blocked && reason && <span style={{ fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500, color: "var(--tertiary)" }}>{reason}</span>}
      </span>
    </button>
  );
}

// Группа чипов: перенос строк; после выбора — сворачивается в одну строку с выбранным чипом.
function ChipGroup({ g, ctx, blockId }) {
  const list = g.items.map(norm).filter((i) => !ctx.avail(i.k).hidden);
  const toggle = () => ctx.patchG && ctx.patchG(blockId, { collapsed: !g.collapsed });
  if (g.used && g.collapsed) {
    const u = list.find((i) => i.k === g.used) || norm(g.used);
    return (
      <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
        <Chip {...u} state="selected" />
        {list.length > 1 && <CmpTextButton text={`Ещё ${list.length - 1}`} icon="expand_more" onClick={toggle} />}
      </div>
    );
  }
  return (
    <div style={{ display: "flex", flexWrap: "wrap", gap: 8, alignItems: "center" }}>
      {list.map((i) => {
        const av = ctx.avail(i.k);
        const st = g.used ? (i.k === g.used ? "selected" : "used") : !av.ok ? "blocked" : "idle";
        return <Chip key={i.k} {...i} state={st} reason={av.reason} onClick={() => ctx.act(i.k, blockId)} />;
      })}
      {g.used && <CmpTextButton text="Свернуть" icon="expand_less" onClick={toggle} />}
    </div>
  );
}

// ── Шаг ленты: подача A (рейка) / B (карточки) ───────────────────────────────
function Step({ look, role, first, last, children, wide }) {
  if (role === "user") {
    return (
      <div className="asst-in" style={{ display: "flex", justifyContent: "flex-end", paddingLeft: look === "A" ? 40 : 32 }}>{children}</div>
    );
  }
  if (look === "A") {
    return (
      <div className="asst-in" style={{ display: "grid", gridTemplateColumns: "32px minmax(0,1fr)", columnGap: 12, position: "relative" }}>
        <div style={{ position: "relative" }}>
          {!first && <span style={{ position: "absolute", left: 15, top: -16, height: 16, width: 2, background: "var(--outline-variant)" }} />}
          {first ? <Mark size={32} /> : <span style={{ position: "absolute", left: 11, top: 14, width: 10, height: 10, borderRadius: 999, background: "var(--outline)" }} />}
          {!last && <span style={{ position: "absolute", left: 15, top: first ? 36 : 28, bottom: 0, width: 2, background: "var(--outline-variant)" }} />}
        </div>
        <div style={{ minWidth: 0, paddingTop: first ? 4 : 0 }}>{children}</div>
      </div>
    );
  }
  return (
    <div className="asst-in" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      {first && (
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <Mark size={24} />
          <CmpText variant="labelLarge" color="var(--on-surface-variant)">Ассистент</CmpText>
        </div>
      )}
      {children}
    </div>
  );
}

function QueryBlock({ b, look }) {
  const A = look === "A";
  return (
    <div style={{ display: "inline-flex", alignItems: "center", gap: 10, padding: "10px 16px", minHeight: 48, boxSizing: "border-box", borderRadius: 12, maxWidth: "100%",
      background: A ? "var(--secondary-container)" : "transparent", color: A ? "var(--on-secondary-container)" : "var(--on-surface)", border: A ? "1px solid transparent" : "1.5px solid var(--primary)" }}>
      <CmpIcon icon={b.icon || "search"} size={20} color={A ? "currentColor" : "var(--primary)"} />
      <CmpText variant="titleMedium">{b.typed ? `«${b.text}»` : b.text}</CmpText>
    </div>
  );
}

function MsgBlock({ b, ctx, look }) {
  const body = (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {(b.title || b.text) && (
        <div style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
          {b.tone === "warn" && <CmpIcon icon="lock" size={22} color="var(--tertiary)" style={{ marginTop: 1 }} />}
          <div style={{ display: "flex", flexDirection: "column", gap: 4, minWidth: 0 }}>
            {b.title && <CmpText variant="titleMedium">{b.title}</CmpText>}
            {b.text && <CmpText variant="bodyLarge">{b.text}</CmpText>}
          </div>
        </div>
      )}
      {b.g && <ChipGroup g={b.g} ctx={ctx} blockId={b.id} />}
    </div>
  );
  if (look === "B") return <CmpSurface color="var(--surface-container)" radius={16} style={{ padding: 16, boxSizing: "border-box" }}>{body}</CmpSurface>;
  return body;
}

// ── Карточка результата: оболочка ────────────────────────────────────────────
function Skeleton({ rows = 3 }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }} aria-label="Загрузка">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div className="asst-sk" style={{ flex: 1, height: 20, borderRadius: 8, background: "var(--surface-variant)", maxWidth: `${88 - i * 14}%` }} />
          <div className="asst-sk" style={{ width: 72, height: 28, borderRadius: 8, background: "var(--surface-variant)" }} />
        </div>
      ))}
    </div>
  );
}
function Pill({ tone = "neutral", icon, children }) {
  const m = { ok: ["color-mix(in srgb, var(--success) 22%, transparent)", "var(--on-surface)", "var(--success)"], warn: ["var(--tertiary-container)", "var(--on-tertiary-container)", "currentColor"], err: ["var(--error-container)", "var(--on-error-container)", "currentColor"], neutral: ["var(--surface-container-highest)", "var(--on-surface)", "currentColor"], info: ["var(--secondary-container)", "var(--on-secondary-container)", "currentColor"] }[tone];
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 6, minHeight: 32, padding: "4px 12px", boxSizing: "border-box", borderRadius: 8, background: m[0], color: m[1], fontFamily: "var(--font-sans)", fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500, flex: "0 0 auto" }}>
      {icon && <CmpIcon icon={icon} size={18} color={m[2]} />}{children}
    </span>
  );
}
function CardShell({ b, ctx, icon, title, badge, children, rows = 3, heavy }) {
  const st = b.status || "data";
  const net = st === "error" && b.errTone === "net";
  return (
    <CmpSurface color="var(--asst-card)" radius={16} border={!heavy} style={{ boxSizing: "border-box", overflow: "hidden", ...(heavy ? { border: "2px solid var(--primary)", boxShadow: "var(--elevation-2)" } : null) }}>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <span style={{ width: 40, height: 40, borderRadius: 12, background: net ? "var(--surface-container-highest)" : st === "error" ? "var(--error-container)" : "var(--primary-container)", color: net ? "var(--on-surface-variant)" : st === "error" ? "var(--on-error-container)" : "var(--on-primary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}>
            <CmpIcon icon={net ? "wifi_off" : st === "error" ? "error" : icon} size={22} />
          </span>
          <div style={{ flex: 1, minWidth: 0 }}><CmpText variant="titleMedium">{title}</CmpText></div>
          {st === "data" && badge}
        </div>
        {st === "loading" && <Skeleton rows={rows} />}
        {st === "error" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 12, alignItems: "flex-start" }}>
            <CmpText variant="bodyLarge">{b.err || "Блок не ответил. Проверьте подключение и повторите."}</CmpText>
            <CmpOutlinedButton text="Повторить" icon="refresh" onClick={() => ctx.retry && ctx.retry(b)} />
          </div>
        )}
        {st === "data" && children}
        {st === "data" && b.g && <div style={{ paddingTop: 4 }}><ChipGroup g={b.g} ctx={ctx} blockId={b.id} /></div>}
      </div>
    </CmpSurface>
  );
}

function KV({ label, value, tone, icon, sub }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 48 }}>
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
        <CmpText variant="bodyLarge">{label}</CmpText>
        {sub && <div style={{ fontFamily: "var(--font-mono)", fontSize: "var(--body-medium-size)", lineHeight: "var(--body-medium-line)", color: "var(--on-surface-variant)" }}>{sub}</div>}
      </div>
      <Pill tone={tone} icon={icon}>{value}</Pill>
    </div>
  );
}

function FirmwareBody({ d }) {
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {d.mods.map((m, i) => (
        <React.Fragment key={m.name}>
          {i > 0 && <CmpDivider />}
          <div style={{ padding: "8px 0" }}>
            <KV label={m.name} sub={m.cur === m.last ? m.cur : `${m.cur} → ${m.last}`} value={m.cur === m.last ? "Актуальна" : "Есть обновление"} tone={m.cur === m.last ? "ok" : "warn"} icon={m.cur === m.last ? "check_circle" : "arrow_upward"} />
          </div>
        </React.Fragment>
      ))}
    </div>
  );
}
function StateBody({ d }) {
  return (
    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(min(100%, 150px), 1fr))", gap: 8 }}>
      {d.items.map((it) => (
        <div key={it.l} style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 12px", minHeight: 56, boxSizing: "border-box", borderRadius: 12, background: it.warn ? "var(--tertiary-container)" : "var(--surface-container)", color: it.warn ? "var(--on-tertiary-container)" : "var(--on-surface)" }}>
          <CmpIcon icon={it.i} size={22} color={it.warn ? "currentColor" : "var(--primary)"} />
          <div style={{ minWidth: 0, display: "flex", flexDirection: "column" }}>
            <CmpText variant="labelLarge" color={it.warn ? "currentColor" : "var(--on-surface-variant)"}>{it.l}</CmpText>
            <CmpText variant="titleMedium">{it.v}</CmpText>
          </div>
        </div>
      ))}
    </div>
  );
}
function BypassBody({ d }) {
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      <KV label="Бесключевой обход" value={d.bypass ? "Обучен" : "Не обучен"} tone={d.bypass ? "ok" : "neutral"} icon={d.bypass ? "check_circle" : undefined} />
      <CmpDivider />
      <KV label="Clone" value={d.clone ? "Выполнен" : "Не выполнен"} tone={d.clone ? "ok" : "neutral"} icon={d.clone ? "check_circle" : undefined} />
      <CmpDivider />
      <KV label="Осталось попыток Clone" value={`${d.cloneLeft} из 3`} tone={d.cloneLeft > 1 ? "neutral" : "warn"} />
    </div>
  );
}

// ── Правка настройки (switch / list / number) ────────────────────────────────
function SettingEditor({ s, draft, onDraft, onNext, onCancel, cur }) {
  const changed = draft !== cur;
  let ctl;
  if (s.type === "switch") {
    ctl = (
      <label style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, minHeight: 56, padding: "0 4px", cursor: "pointer" }}>
        <CmpText variant="titleMedium">{draft ? "Включено" : "Выключено"}</CmpText>
        <CmpSwitch checked={!!draft} onCheckedChange={onDraft} thumbIcon />
      </label>
    );
  } else if (s.type === "list") {
    ctl = (
      <div role="radiogroup" style={{ display: "flex", flexDirection: "column" }}>
        {s.options.map((o) => (
          <div key={o} role="radio" aria-checked={draft === o} tabIndex={0} className="asst-f" onClick={() => onDraft(o)}
            onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); onDraft(o); } }}
            style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 48, cursor: "pointer", borderRadius: 12 }}>
            <CmpRadioButton selected={draft === o} onClick={() => onDraft(o)} />
            <CmpText variant="bodyLarge">{o}</CmpText>
          </div>
        ))}
      </div>
    );
  } else {
    const dec = () => onDraft(Math.max(s.min, draft - s.step)), inc = () => onDraft(Math.min(s.max, draft + s.step));
    const btn = (icon, fn, dis, lbl) => (
      <button type="button" className="asst-f" aria-label={lbl} disabled={dis} onClick={fn}
        style={{ width: 56, height: 56, borderRadius: 16, border: "1px solid var(--outline)", background: "var(--asst-card)", color: "var(--primary)", display: "grid", placeItems: "center", cursor: dis ? "default" : "pointer", opacity: dis ? 0.38 : 1 }}>
        <CmpIcon icon={icon} size={28} />
      </button>
    );
    ctl = (
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          {btn("remove", dec, draft <= s.min, "Уменьшить")}
          <div style={{ flex: 1, height: 56, borderRadius: 16, background: "var(--surface-container)", display: "grid", placeItems: "center", fontFamily: "var(--font-mono)", fontSize: "var(--headline-small-size)", lineHeight: "var(--headline-small-line)", fontWeight: 500, color: "var(--on-surface)" }}>{fmt(s, draft)}</div>
          {btn("add", inc, draft >= s.max, "Увеличить")}
        </div>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{`От ${fmt(s, s.min)} до ${fmt(s, s.max)}`}</CmpText>
      </div>
    );
  }
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: 12, borderRadius: 12, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)" }}>
      {ctl}
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap", justifyContent: "flex-end" }}>
        <CmpTextButton text="Отмена" onClick={onCancel} />
        <CmpButton text="Далее" icon="arrow_forward" disabled={!changed} onClick={onNext} />
      </div>
    </div>
  );
}

function SettingsBody({ b, ctx }) {
  const all = b.data.found;
  const list = b.showAll ? all : all.slice(0, 5);
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {list.map((s, i) => {
        const cur = ctx.vals[s.id];
        const editable = s.type !== "other";
        const editing = b.editing === s.id;
        return (
          <React.Fragment key={s.id}>
            {i > 0 && <CmpDivider />}
            <div style={{ display: "flex", flexDirection: "column", gap: 8, padding: "12px 0" }}>
              <div style={{ display: "flex", gap: 12, alignItems: "flex-start", flexWrap: "wrap" }}>
                <div style={{ flex: "1 1 180px", minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
                  <CmpText variant="titleMedium"><Hl text={s.name} q={b.q} /></CmpText>
                  <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{s.path}</CmpText>
                </div>
                <Pill tone="neutral">{fmt(s, cur)}</Pill>
              </div>
              {editing
                ? <SettingEditor s={s} cur={cur} draft={b.draft} onDraft={(v) => ctx.patch(b.id, { draft: v })} onCancel={() => ctx.patch(b.id, { editing: null })} onNext={() => ctx.toConfirm(b, s)} />
                : (
                  <div style={{ display: "flex", gap: 4, flexWrap: "wrap", marginLeft: -12 }}>
                    {editable && <CmpTextButton text="Изменить" icon="edit" onClick={() => ctx.patch(b.id, { editing: s.id, draft: cur })} />}
                    <CmpTextButton text="Открыть в настройках" icon="open_in_new" onClick={() => ctx.openSetting(s)} />
                  </div>
                )}
              {!editable && !editing && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Этот тип меняется только в настройках</CmpText>}
            </div>
          </React.Fragment>
        );
      })}
      {all.length > 5 && !b.showAll && (
        <div style={{ paddingTop: 4 }}><CmpOutlinedButton text={`Показать все ${all.length}`} icon="expand_more" fullWidth onClick={() => ctx.patch(b.id, { showAll: true })} /></div>
      )}
    </div>
  );
}

const compsLabel = (n) => { const a = n % 10, h = n % 100; return `${n} ${a === 1 && h !== 11 ? "компонент" : a >= 2 && a <= 4 && (h < 12 || h > 14) ? "компонента" : "компонентов"}`; };
// Информация о товаре — коротко про подключённый блок; полный экран — «Подробнее».
function ProductBody({ d, ctx }) {
  if (d.notFound) return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, alignItems: "flex-start" }}>
      <CmpText variant="titleMedium">{`Товар с ID ${d.id} не найден`}</CmpText>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок мог быть продан не через систему — это не ошибка. Можно поискать по штрих-коду с коробки.</CmpText>
      <CmpOutlinedButton text="Искать по другому номеру" icon="search" onClick={() => ctx.openProduct && ctx.openProduct("")} style={{ height: 48, marginTop: 4 }} />
    </div>
  );
  const rows = [["ID", d.id], ["Серийный номер", d.serial], ["Заводская дата", d.date]].filter(([, v]) => v);
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CmpText variant="titleLarge">{d.name}</CmpText>
      <div style={{ display: "flex", flexDirection: "column" }}>
        {rows.map(([l, v], i) => (
          <div key={l} style={{ display: "flex", alignItems: "center", flexWrap: "wrap", columnGap: 12, minHeight: 48, borderTop: i ? "1px solid var(--outline-variant)" : "none" }}>
            <span style={{ flex: "1 1 120px" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{l}</CmpText></span>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: 16, fontWeight: 500, color: "var(--on-surface)", overflowWrap: "anywhere" }}>{v}</span>
          </div>
        ))}
      </div>
      {d.kit && (
        <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 56, padding: "10px 12px", borderRadius: 12, background: "var(--surface-container)", flexWrap: "wrap" }}>
          <CmpIcon icon="inventory_2" size={24} color="var(--primary)" />
          <div style={{ flex: "1 1 160px", minWidth: 0, display: "flex", flexDirection: "column" }}>
            <CmpText variant="labelLarge" color="var(--on-surface-variant)">Комплект</CmpText>
            <CmpText variant="titleMedium">{d.kit.name}</CmpText>
          </div>
          <Pill tone="info">{compsLabel(d.kit.n)}</Pill>
        </div>
      )}
      <CmpButton text="Подробнее" icon="open_in_new" onClick={() => ctx.openProduct && ctx.openProduct(d.id)} style={{ height: 48, alignSelf: "flex-start" }} />
    </div>
  );
}

function CardBlock({ b, ctx }) {
  if (b.kind === "product") return <CardShell b={b} ctx={ctx} icon="manage_search" title="Информация о товаре" rows={4} badge={b.data && b.data.notFound ? <Pill tone="neutral">Нет в базе</Pill> : null}>{b.data && <ProductBody d={b.data} ctx={ctx} />}</CardShell>;
  if (b.kind === "fw") return <CardShell b={b} ctx={ctx} icon="system_update" title="Прошивка" badge={b.data && (b.data.mods.some((m) => m.cur !== m.last) ? <Pill tone="warn">Есть обновление</Pill> : <Pill tone="ok" icon="check_circle">Актуальна</Pill>)}>{b.data && <FirmwareBody d={b.data} />}</CardShell>;
  if (b.kind === "state") return <CardShell b={b} ctx={ctx} icon="speed" title="Состояние блока" rows={4} badge={<Pill tone="warn" icon="warning">1 внимание</Pill>}>{b.data && <StateBody d={b.data} />}</CardShell>;
  if (b.kind === "bypass") return <CardShell b={b} ctx={ctx} icon="key" title="Обход и Clone">{b.data && <BypassBody d={b.data} />}</CardShell>;
  if (b.kind === "settings") return <CardShell b={b} ctx={ctx} icon="tune" title="Найденные настройки" badge={b.data && <Pill tone="info">{b.data.found.length}</Pill>}>{b.data && <SettingsBody b={b} ctx={ctx} />}</CardShell>;
  if (b.kind === "none") return (
    <CardShell b={b} ctx={ctx} icon="search_off" title="Ничего не найдено">
      <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
        <CmpText variant="bodyLarge">{`По запросу «${b.q}» настроек нет.`}</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Попробуйте часть слова: «темп», «сирен», «двер».</CmpText>
      </div>
    </CardShell>
  );
  return null;
}

// ── Подтверждение записи ─────────────────────────────────────────────────────
// status: pending | writing | done | error | cancelled
function ConfirmBlock({ b, ctx }) {
  const s = SBY[b.sid];
  const st = b.status;
  const head = {
    pending: ["edit_note", "Записать в блок?", "var(--primary)", "var(--on-primary)"],
    writing: ["sync", "Записываем в блок", "var(--primary)", "var(--on-primary)"],
    done: ["check", "Записано в блок", "var(--success)", "var(--on-primary)"],
    error: ["error", "Не удалось записать", "var(--error)", "var(--on-error)"],
    cancelled: ["block", "Запись отменена", "var(--surface-container-highest)", "var(--on-surface-variant)"],
  }[st];
  const heavy = st === "pending" || st === "writing" || st === "error";
  return (
    <CmpSurface color="var(--asst-card)" radius={20} border={!heavy}
      style={{ boxSizing: "border-box", overflow: "hidden", opacity: st === "cancelled" ? 0.72 : 1, ...(heavy ? { border: `2px solid ${st === "error" ? "var(--error)" : "var(--primary)"}`, boxShadow: "var(--elevation-3)" } : null) }}>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <span style={{ width: 40, height: 40, borderRadius: 999, background: head[2], color: head[3], display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={head[0]} size={24} /></span>
          <CmpText variant="titleLarge">{head[1]}</CmpText>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <CmpText variant="titleMedium">{s.name}</CmpText>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{s.path}</CmpText>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) 32px minmax(0,1fr)", alignItems: "stretch", gap: 4 }}>
          <div style={{ padding: "10px 12px", borderRadius: 12, background: "var(--surface-container)", display: "flex", flexDirection: "column" }}>
            <CmpText variant="labelLarge" color="var(--on-surface-variant)">Было</CmpText>
            <CmpText variant="titleLarge">{fmt(s, b.from)}</CmpText>
          </div>
          <div style={{ display: "grid", placeItems: "center", color: "var(--on-surface-variant)" }}><CmpIcon icon="arrow_forward" size={24} /></div>
          <div style={{ padding: "10px 12px", borderRadius: 12, background: st === "cancelled" ? "var(--surface-container)" : "var(--primary-container)", color: st === "cancelled" ? "var(--on-surface)" : "var(--on-primary-container)", display: "flex", flexDirection: "column" }}>
            <CmpText variant="labelLarge">{st === "done" ? "Стало" : "Станет"}</CmpText>
            <CmpText variant="titleLarge">{fmt(s, b.to)}</CmpText>
          </div>
        </div>
        {st === "pending" && (
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
            <CmpButton text="Записать" icon="save" style={{ flex: "1 1 140px", height: 48 }} onClick={() => ctx.write(b)} />
            <CmpOutlinedButton text="Отмена" style={{ flex: "1 1 120px", height: 48 }} onClick={() => ctx.patch(b.id, { status: "cancelled" })} />
          </div>
        )}
        {st === "writing" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <CmpLinearProgressIndicator />
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Не отключайте блок до окончания записи</CmpText>
          </div>
        )}
        {st === "error" && (
          <>
            <CmpText variant="bodyLarge">{b.err || "Блок не ответил. Значение в блоке не изменилось."}</CmpText>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              <CmpButton text="Повторить" icon="refresh" style={{ flex: "1 1 140px", height: 48 }} onClick={() => ctx.write(b)} />
              <CmpOutlinedButton text="Отмена" style={{ flex: "1 1 120px", height: 48 }} onClick={() => ctx.patch(b.id, { status: "cancelled" })} />
            </div>
          </>
        )}
        {st === "cancelled" && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок не изменён.</CmpText>}
        {st === "done" && b.g && <ChipGroup g={b.g} ctx={ctx} blockId={b.id} />}
      </div>
    </CmpSurface>
  );
}

// ── Возврат с другого экрана ─────────────────────────────────────────────────
function ReturnBlock({ b, ctx }) {
  const t = { ok: ["check_circle", "var(--success)", "color-mix(in srgb, var(--success) 22%, var(--asst-card))"], error: ["error", "var(--on-error-container)", "var(--error-container)"], stop: ["pause_circle", "var(--on-tertiary-container)", "var(--tertiary-container)"] }[b.tone];
  return (
    <CmpSurface color="var(--asst-card)" radius={16} border style={{ boxSizing: "border-box", overflow: "hidden" }}>
      <div style={{ display: "flex", gap: 12, alignItems: "center", padding: "12px 16px", background: t[2] }}>
        <CmpIcon icon={t[0]} size={28} color={t[1]} />
        <div style={{ minWidth: 0, display: "flex", flexDirection: "column" }}>
          <CmpText variant="labelLarge" color={b.tone === "ok" ? "var(--on-surface-variant)" : "currentColor"}>{`Возврат из «${b.from}»`}</CmpText>
          <CmpText variant="titleMedium" color={b.tone === "ok" ? "var(--on-surface)" : t[1]}>{b.title}</CmpText>
        </div>
      </div>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 16 }}>
        {b.text && <CmpText variant="bodyLarge">{b.text}</CmpText>}
        {b.g && <ChipGroup g={b.g} ctx={ctx} blockId={b.id} />}
      </div>
    </CmpSurface>
  );
}

// ── Выбор источника настроек (карточка в ленте) ──────────────────────────────
function SourceRow({ src, state, reason, onClick }) {
  const L = useLayer();
  const live = state === "idle" && onClick;
  const blocked = state === "blocked", sel = state === "selected", used = state === "used";
  return (
    <button type="button" className="asst-f" disabled={!live} onClick={live ? onClick : undefined} {...(live ? L.handlers : {})}
      style={{ position: "relative", overflow: "hidden", display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "10px 12px", width: "100%", boxSizing: "border-box", borderRadius: 12, textAlign: "left", fontFamily: "var(--font-sans)", cursor: live ? "pointer" : "default",
        border: sel ? "1.5px solid var(--primary)" : blocked ? "1px dashed var(--outline)" : "1px solid var(--outline-variant)", background: sel ? "var(--secondary-container)" : blocked ? "var(--surface-container)" : "transparent", color: "var(--on-surface)", opacity: used ? 0.38 : 1 }}>
      <span style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity: L.opacity, pointerEvents: "none" }} />
      <span style={{ width: 40, height: 40, borderRadius: 12, background: blocked ? "var(--surface-container-highest)" : "var(--primary-container)", color: blocked ? "var(--on-surface-variant)" : "var(--on-primary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={blocked ? "cloud_off" : src.icon} size={22} /></span>
      <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
        <CmpText variant="titleMedium" color={blocked ? "var(--on-surface-variant)" : undefined}>{src.title}</CmpText>
        {blocked ? <CmpText variant="labelLarge" color="var(--tertiary)">{reason}</CmpText> : <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{src.sub}</CmpText>}
      </span>
      <CmpIcon icon={sel ? "check" : blocked ? "lock" : "chevron_right"} size={24} color={sel ? "var(--primary)" : "var(--on-surface-variant)"} />
    </button>
  );
}
function SourceBlock({ b, ctx }) {
  return (
    <CmpSurface color="var(--asst-card)" radius={16} border style={{ boxSizing: "border-box" }}>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <span style={{ width: 40, height: 40, borderRadius: 12, background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}><CmpIcon icon="file_download" size={22} /></span>
          <CmpText variant="titleMedium">Откуда загрузить настройки</CmpText>
        </div>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Перед записью откроется редактор со списком изменений</CmpText>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {SOURCES.map((s) => {
            const blocked = s.net && !ctx.online;
            const st = b.used ? (b.used === s.k ? "selected" : "used") : blocked ? "blocked" : "idle";
            return <SourceRow key={s.k} src={s} state={st} reason="Нет сети. Подключитесь к интернету" onClick={() => ctx.pickSource(b, s)} />;
          })}
        </div>
      </div>
    </CmpSurface>
  );
}

// ── Задел v2: «думает» и текстовый ответ ─────────────────────────────────────
function ThinkBlock() {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 48 }}>
      <span style={{ display: "inline-flex", gap: 6 }}>
        {[0, 1, 2].map((i) => <span key={i} className="asst-dot" style={{ width: 8, height: 8, borderRadius: 999, background: "var(--primary)", animationDelay: `${i * 0.16}s` }} />)}
      </span>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Ищем ответ по данным блока и базе монтажа</CmpText>
    </div>
  );
}
function AnswerBlock({ b, ctx, look }) {
  const body = (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {b.paras.map((p, i) => <CmpText key={i} variant="bodyLarge" style={{ textWrap: "pretty" }}>{p}</CmpText>)}
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">Связанные настройки блока</CmpText>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        {b.refs.map((sid) => { const s = SBY[sid]; return (
          <div key={sid} style={{ display: "flex", alignItems: "center", gap: 12, padding: "8px 12px", minHeight: 56, boxSizing: "border-box", borderRadius: 12, border: "1px solid var(--outline-variant)", flexWrap: "wrap" }}>
            <div style={{ flex: "1 1 160px", minWidth: 0, display: "flex", flexDirection: "column" }}><CmpText variant="titleSmall">{s.name}</CmpText><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{s.path}</CmpText></div>
            <Pill>{fmt(s, ctx.vals[sid])}</Pill>
          </div>
        ); })}
      </div>
      {b.g && <ChipGroup g={b.g} ctx={ctx} blockId={b.id} />}
    </div>
  );
  if (look === "B") return <CmpSurface color="var(--surface-container)" radius={16} style={{ padding: 16, boxSizing: "border-box" }}>{body}</CmpSurface>;
  return body;
}

function FeedBlock({ b, ctx, look }) {
  switch (b.t) {
    case "msg": return <MsgBlock b={b} ctx={ctx} look={look} />;
    case "query": return <QueryBlock b={b} look={look} />;
    case "card": return <CardBlock b={b} ctx={ctx} />;
    case "confirm": return <ConfirmBlock b={b} ctx={ctx} />;
    case "ret": return <ReturnBlock b={b} ctx={ctx} />;
    case "source": return <SourceBlock b={b} ctx={ctx} />;
    case "think": return <ThinkBlock />;
    case "answer": return <AnswerBlock b={b} ctx={ctx} look={look} />;
    default: return null;
  }
}

function Feed({ feed, ctx, look }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      {feed.map((b, i) => {
        const role = b.t === "query" ? "user" : "asst";
        const prev = feed[i - 1], next = feed[i + 1];
        const first = role === "asst" && (!prev || prev.t === "query");
        const last = role === "asst" && (!next || next.t === "query");
        return <Step key={b.id} look={look} role={role} first={first} last={last}><FeedBlock b={b} ctx={ctx} look={look} /></Step>;
      })}
    </div>
  );
}

// ── Шапка блока: развёрнутая / строка ────────────────────────────────────────
function InfoChip({ icon, children, tone }) {
  return <Pill tone={tone || "neutral"} icon={icon}>{children}</Pill>;
}
function DeviceHeader({ p, dev, canF, online, collapsed, onToggle, wide, onCan }) {
  const ble = p.transport === "ble";
  const L = useLayer();
  if (collapsed) {
    return (
      <button type="button" className="asst-f" onClick={onToggle} {...L.handlers} aria-expanded="false" aria-label="Развернуть шапку блока"
        style={{ position: "relative", overflow: "hidden", display: "flex", alignItems: "center", gap: 12, width: "100%", minHeight: 56, padding: "0 8px 0 16px", boxSizing: "border-box", border: "none", borderBottom: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", color: "var(--on-surface)", cursor: "pointer", fontFamily: "var(--font-sans)", textAlign: "left" }}>
        <span style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity: L.opacity, pointerEvents: "none" }} />
        <CmpIcon icon={ble ? "bluetooth_connected" : "usb"} size={22} color="var(--primary)" />
        <span style={{ flex: 1, minWidth: 0, display: "flex", alignItems: "baseline", gap: 8, overflow: "hidden" }}>
          <CmpText variant="titleMedium" maxLines={1}>{p.model}</CmpText>
          <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--body-medium-size)", color: "var(--on-surface-variant)", whiteSpace: "nowrap" }}>{dev.fw}{p.can ? ` · CAN ${p.can}` : ""}</span>
        </span>
        {canF && p.can && <CmpIcon icon="warning" size={22} color="var(--tertiary)" title="Заводской CAN" />}
        {!online && <CmpIcon icon="cloud_off" size={22} color="var(--on-surface-variant)" title="Нет сети" />}
        <CmpIcon icon="expand_more" size={24} color="var(--on-surface-variant)" />
      </button>
    );
  }
  return (
    <div style={{ background: wide ? "transparent" : "var(--surface-container-low)", borderBottom: wide ? "none" : "1px solid var(--outline-variant)", padding: wide ? 0 : "12px 8px 12px 16px", display: "flex", flexDirection: "column", gap: 12 }}>
      <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
          <CmpText variant={wide ? "headlineSmall" : "titleLarge"} color="var(--on-surface)">{p.model}</CmpText>
          <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--body-medium-size)", color: "var(--on-surface-variant)" }}>ID {p.id}</span>
        </div>
        {onToggle && <CmpIconButton icon="expand_less" contentDescription="Свернуть шапку" onClick={onToggle} />}
      </div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
        <InfoChip icon={ble ? "bluetooth_connected" : "usb"}>{ble ? "Bluetooth" : "USB"}</InfoChip>
        <InfoChip icon="memory">{`ПО ${dev.fw}`}</InfoChip>
        {p.can && <InfoChip icon={canF ? "warning" : "check_circle"} tone={canF ? "warn" : "ok"}>{canF ? "CAN заводской" : `CAN ${p.can} · ${p.canCode}`}</InfoChip>}
        {!p.can && <InfoChip icon="help" tone="warn">Модели нет в базе</InfoChip>}
        {!online && <InfoChip icon="cloud_off">Нет сети</InfoChip>}
      </div>
      {canF && p.can && (
        <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap", padding: "8px 8px 8px 12px", borderRadius: 12, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", marginRight: wide ? 0 : 8 }}>
          <CmpIcon icon="lock" size={22} />
          <div style={{ flex: "1 1 180px", minWidth: 0 }}><CmpText variant="bodyLarge">Заводской CAN. Обновите CAN, чтобы открыть настройки, обход и Clone.</CmpText></div>
          <CmpButton text="Обновить CAN" icon="system_update" onClick={onCan} disabled={!online} />
        </div>
      )}
    </div>
  );
}

// ── Нижняя панель: быстрые действия + поиск ──────────────────────────────────
function QuickRow({ items, ctx }) {
  return (
    <div className="asst-nosb" style={{ display: "flex", gap: 8, overflowX: "auto", padding: "0 16px", scrollPaddingLeft: 16 }}>
      {items.map((i) => {
        const it = norm(i), av = ctx.avail(it.k);
        if (av.hidden) return null;
        return <Chip key={it.k} {...it} nowrap state={av.ok || it.k === "__search" ? "idle" : "blocked"} reason={av.reason} onClick={() => it.k === "__search" ? ctx.search() : ctx.act(it.k, null, true)} />;
      })}
    </div>
  );
}
function Composer({ q, setQ, onSubmit, onFocus, placeholder, quick, ctx, showQuick = true, inputRef }) {
  const sugg = q.trim()
    ? [...matchSections(q), { k: "__search", label: `Найти «${q.trim()}» в настройках`, icon: "search", primary: true }]
    : quick;
  return (
    <div style={{ borderTop: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", paddingTop: 12, paddingBottom: 12, display: "flex", flexDirection: "column", gap: 12 }}>
      {(showQuick || q.trim()) && <QuickRow items={sugg} ctx={ctx} />}
      <form ref={inputRef} onSubmit={(e) => { e.preventDefault(); onSubmit(); }} onFocusCapture={onFocus} style={{ padding: "0 16px", display: "flex", gap: 8, alignItems: "center" }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <CmpTextField value={q} onChange={setQ} placeholder={placeholder} singleLine leadingIcon={<CmpIcon icon="search" size={24} color="var(--on-surface-variant)" />}
            trailingIcon={q ? <CmpIconButton icon="close" contentDescription="Очистить" onClick={() => setQ("")} /> : undefined} rootStyle={{ width: "100%" }} />
        </div>
        <button type="submit" className="asst-f" aria-label="Найти" disabled={!q.trim()}
          style={{ width: 56, height: 56, borderRadius: 16, border: "none", background: q.trim() ? "var(--primary)" : "var(--surface-container-highest)", color: q.trim() ? "var(--on-primary)" : "var(--on-surface-variant)", display: "grid", placeItems: "center", cursor: q.trim() ? "pointer" : "default", flex: "0 0 auto", transition: "background var(--motion-fast)" }}>
          <CmpIcon icon="arrow_forward" size={26} />
        </button>
      </form>
    </div>
  );
}

function Keyboard() {
  const rows = ["йцукенгшщзх", "фывапролджэ", "ячсмитьбю"];
  const key = (l, flex = 1, bg = "var(--surface-bright)") => <div key={l + flex} style={{ flex, height: 44, borderRadius: 6, background: bg, display: "grid", placeItems: "center", fontFamily: "var(--font-sans)", fontSize: 20, color: "var(--on-surface)" }}>{l}</div>;
  return (
    <div aria-hidden="true" style={{ background: "var(--surface-container-highest)", padding: "8px 4px 8px", display: "flex", flexDirection: "column", gap: 8 }}>
      {rows.map((r, i) => <div key={i} style={{ display: "flex", gap: 4, padding: i === 2 ? "0 28px" : 0 }}>{r.split("").map((c) => key(c))}</div>)}
      <div style={{ display: "flex", gap: 4 }}>{key("?123", 1.5, "var(--surface-container)")}{key(",")}{key("пробел", 5)}{key(".")}{key("⏎", 1.5, "var(--primary-container)")}</div>
    </div>
  );
}

// ── «Уход» на существующий экран (не рисуется — заглушка с исходами) ─────────
function AwayStub({ spec, onReturn }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 6, background: "var(--background)", display: "flex", flexDirection: "column" }}>
      <CmpTopAppBar title={spec.title} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={() => onReturn(spec.outcomes[spec.outcomes.length - 1])} />} />
      <div style={{ flex: 1, overflow: "auto", padding: 16, display: "flex", flexDirection: "column", gap: 16, maxWidth: 560, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
        <div style={{ borderRadius: 16, border: "2px dashed var(--outline)", padding: 24, minHeight: 160, display: "grid", placeItems: "center", textAlign: "center", background: "repeating-linear-gradient(135deg, transparent 0 10px, color-mix(in srgb, var(--surface-variant) 50%, transparent) 10px 20px)" }}>
          <div>
            <div style={{ fontFamily: "var(--font-mono)", fontSize: "var(--body-medium-size)", color: "var(--on-surface-variant)" }}>существующий экран · не входит в ТЗ</div>
            <CmpText variant="titleMedium">{spec.note || spec.title}</CmpText>
          </div>
        </div>
        <CmpText variant="labelLarge" color="var(--on-surface-variant)">Вернуться в ассистент с итогом</CmpText>
        {spec.outcomes.map((o) => <CmpOutlinedButton key={o.label} text={o.label} icon={o.ret ? (o.ret.tone === "ok" ? "check_circle" : o.ret.tone === "error" ? "error" : "pause_circle") : "arrow_back"} fullWidth style={{ height: 48 }} onClick={() => onReturn(o)} />)}
      </div>
    </div>
  );
}
function LostStub({ transport, onRestore }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 7, background: "var(--background)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 16, padding: 24, textAlign: "center" }}>
      <div style={{ fontFamily: "var(--font-mono)", fontSize: "var(--body-medium-size)", color: "var(--on-surface-variant)" }}>существующий экран «Потеря связи» · не перерисовывается</div>
      <CmpIcon icon={transport === "ble" ? "bluetooth_disabled" : "usb_off"} size={64} color="var(--primary)" />
      <CmpText variant="headlineSmall">Отсутствует соединение с устройством</CmpText>
      <CmpOutlinedButton text="Связь восстановлена" icon="link" onClick={onRestore} style={{ height: 48 }} />
    </div>
  );
}

// ── Данные карточек ──────────────────────────────────────────────────────────
const fwData = (dev) => ({ mods: [{ name: "Основной блок", cur: dev.fw, last: "3.57" }, { name: "Bluetooth", cur: "1.14", last: "1.14" }, { name: "CAN", cur: "12.05", last: "12.05" }] });
const stateData = () => ({ items: [{ l: "Питание", v: "12,6 В", i: "battery_full" }, { l: "Двигатель", v: "Заглушён", i: "settings_power" }, { l: "Двери", v: "Закрыты", i: "sensor_door" }, { l: "Капот", v: "Открыт", i: "warning", warn: true }, { l: "Багажник", v: "Закрыт", i: "inventory" }, { l: "Охрана", v: "Снята", i: "lock_open" }, { l: "Связь", v: "Хорошая", i: "bluetooth_connected" }, { l: "CAN", v: "Активна", i: "cable" }] });
const nextFor = (kind, data) => kind === "fw" ? { items: data.mods.some((m) => m.cur !== m.last) ? [{ k: "fw_update", primary: true }, "archive"] : ["archive", "find"] }
  : kind === "state" ? { items: ["state_more"] }
  : kind === "bypass" ? { items: data.bypass ? [{ k: "clone_run", primary: !data.clone }, "bypass_run"] : [{ k: "bypass_run", primary: true }, "clone_run"] }
  : kind === "none" ? { items: ["all_settings", "support"] } : null;

function SideAction({ it, av, onClick }) {
  const L = useLayer();
  const blocked = !av.ok;
  return (
    <button type="button" className="asst-f" onClick={onClick} {...L.handlers}
      style={{ position: "relative", overflow: "hidden", display: "flex", alignItems: "center", gap: 12, minHeight: 56, padding: "8px 12px", borderRadius: 12, width: "100%", boxSizing: "border-box", textAlign: "left", fontFamily: "var(--font-sans)", cursor: "pointer", background: blocked ? "transparent" : "var(--asst-card)", border: blocked ? "1px dashed var(--outline)" : "1px solid var(--outline-variant)", color: blocked ? "var(--on-surface-variant)" : "var(--on-surface)" }}>
      <span style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity: L.opacity, pointerEvents: "none" }} />
      <CmpIcon icon={blocked ? "lock" : it.icon} size={22} color={blocked ? "currentColor" : "var(--primary)"} />
      <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
        <span style={{ fontSize: "var(--body-large-size)", lineHeight: "var(--body-large-line)", fontWeight: 500 }}>{it.label}</span>
        {blocked && <span style={{ fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500, color: "var(--tertiary)" }}>{av.reason}</span>}
      </span>
    </button>
  );
}

// ── Демо-оболочки ────────────────────────────────────────────────────────────
const DEMO_FONT = "Roboto, system-ui, sans-serif";
function Seg({ items, value, onChange }) {
  return (
    <div style={{ display: "flex", background: "#fff", borderRadius: 999, padding: 4, boxShadow: "0 1px 3px rgba(0,0,0,.18)" }}>
      {items.map(([k, l]) => (
        <button key={k} onClick={() => onChange(k)} style={{ border: "none", background: value === k ? "#1A3E7A" : "transparent", color: value === k ? "#fff" : "#444", padding: "8px 14px", borderRadius: 999, fontFamily: DEMO_FONT, fontSize: 13, fontWeight: 500, cursor: "pointer", whiteSpace: "nowrap" }}>{l}</button>
      ))}
    </div>
  );
}
function Phone({ dark, children, width = 412, height = 840 }) {
  const c = dark ? "#fff" : "#1A1C1C";
  return (
    <div style={{ width, height, borderRadius: 22, overflow: "hidden", border: "8px solid rgba(116,119,117,.5)", boxShadow: "0 30px 80px rgba(0,0,0,.25)", display: "flex", flexDirection: "column", boxSizing: "border-box", flex: "0 0 auto" }}>
      <div data-theme={dark ? "dark" : undefined} style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", background: "var(--background)" }}>
        <div style={{ height: 32, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 18px", fontFamily: DEMO_FONT, fontSize: 14, color: c, flex: "0 0 auto" }}><span>9:30</span><span style={{ display: "flex", gap: 4 }}><CmpIcon icon="signal_cellular_alt" size={16} /><CmpIcon icon="battery_full" size={16} /></span></div>
        <div style={{ flex: 1, minHeight: 0 }}>{children}</div>
        <div style={{ height: 20, display: "grid", placeItems: "center", flex: "0 0 auto" }}><div style={{ width: 108, height: 4, borderRadius: 2, background: c, opacity: 0.4 }} /></div>
      </div>
    </div>
  );
}
const WINDOWS = { tl: { w: 1280, h: 800, l: "Планшет · ландшафт 1280×800", kind: "tablet" }, tp: { w: 800, h: 1280, l: "Планшет · портрет 800×1280", kind: "tablet" }, dk: { w: 1280, h: 800, l: "Desktop 1280×800", kind: "desktop" }, dmin: { w: 800, h: 600, l: "Desktop · мин. 800×600", kind: "desktop" } };
function Fit({ w, h, children }) {
  const ref = useRef(null);
  const [s, setS] = useState(1);
  useEffect(() => { const el = ref.current; const m = () => setS(Math.min(1, (el.offsetWidth) / w)); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, [w]);
  return <div ref={ref} style={{ width: "100%", display: "flex", justifyContent: "center" }}><div style={{ width: w * s, height: h * s }}><div style={{ width: w, height: h, transform: `scale(${s})`, transformOrigin: "top left" }}>{children}</div></div></div>;
}
function WindowFrame({ win, dark, children }) {
  const desk = win.kind === "desktop";
  const bw = desk ? 1 : 12;
  const W = win.w + bw * 2, H = win.h + bw * 2 + (desk ? 36 : 0);
  return (
    <Fit w={W} h={H}>
      <div style={{ width: W, height: H, boxSizing: "border-box", borderRadius: desk ? 10 : 28, overflow: "hidden", border: desk ? "1px solid rgba(0,0,0,.25)" : "12px solid #2b2d30", background: "#2b2d30", boxShadow: "0 30px 80px rgba(0,0,0,.25)", display: "flex", flexDirection: "column" }}>
        {desk && <div style={{ height: 36, background: dark ? "#2a3142" : "#dcdcdc", display: "flex", alignItems: "center", gap: 8, padding: "0 12px", fontFamily: DEMO_FONT, fontSize: 13, color: dark ? "#ECEEF2" : "#262626" }}><span style={{ fontWeight: 600 }}>Pandora Spec</span><span style={{ opacity: 0.6 }}>— Ассистент</span></div>}
        <div data-theme={dark ? "dark" : undefined} style={{ width: win.w, height: win.h, background: "var(--background)" }}>{children}</div>
      </div>
    </Fit>
  );
}
window.AsstKit = { ACT, GREET, QUICK, SETTINGS, SBY, SOURCES, PROFILES, makeAvail, norm, fmt, findSettings, fwData, stateData, nextFor, Mark, AssistantIcon, Chip, ChipGroup, Step, Feed, FeedBlock, QueryBlock, MsgBlock, CardBlock, CardShell, Skeleton, Pill, SettingEditor, ConfirmBlock, ReturnBlock, SourceBlock, ThinkBlock, AnswerBlock, DeviceHeader, QuickRow, Composer, Keyboard, SideAction, Phone, Seg, AwayStub, LostStub, KV, Fit, WindowFrame, WINDOWS };
Object.assign(window, { AsstLoaded: () => null, AssistantIcon });
