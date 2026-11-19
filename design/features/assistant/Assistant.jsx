// Ассистент монтажа — концепция «Пульт».
// Лента — журнал: что сделано и что ответил блок. Пульт — что делать: внизу на телефоне и
// планшете-портрете, слева на широком окне. В пульте: «Следующий шаг» по маршруту монтажа,
// темы с живым статусом → подуровень темы → действия; поиск сразу показывает темы, действия и
// настройки. Требует window.AsstKit (AssistantKit.jsx).

const { CmpText, CmpIcon, CmpButton, CmpOutlinedButton, CmpTextButton, CmpIconButton, CmpSurface, CmpTopAppBar, CmpTextField } = window.PandoraSpecDesignSystem_b8d6e1;
const { useState, useEffect, useRef } = React;
let K = null;

(function css() {
  if (document.getElementById("asst2-css")) return;
  const s = document.createElement("style"); s.id = "asst2-css";
  s.textContent = `
:root{--asst-card:var(--background-surface-card)}
[data-theme="dark"]{--asst-card:var(--background-surface-card)}
@keyframes a2Pulse{0%{box-shadow:0 0 0 0 color-mix(in srgb,var(--primary) 60%,transparent)}100%{box-shadow:0 0 0 12px transparent}}
@keyframes a2InR{from{opacity:0;transform:translateX(28px)}to{opacity:1;transform:none}}
@keyframes a2InL{from{opacity:0;transform:translateX(-28px)}to{opacity:1;transform:none}}
@keyframes a2Up{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}
@keyframes a2Spin{to{transform:rotate(360deg)}}
.a2-pulse{animation:a2Pulse 1.1s ease-out 2}
.a2-r{animation:a2InR .26s cubic-bezier(.2,0,0,1) both}
.a2-l{animation:a2InL .26s cubic-bezier(.2,0,0,1) both}
.a2-up{animation:a2Up .26s cubic-bezier(.2,0,0,1) both}
.a2-spin{animation:a2Spin 1s linear infinite;display:inline-grid}
.a2-f:focus-visible{outline:2px solid var(--primary);outline-offset:2px}
.a2-nosb{scrollbar-width:none}.a2-nosb::-webkit-scrollbar{display:none}
@media (prefers-reduced-motion:reduce){.a2-pulse,.a2-r,.a2-l,.a2-up,.a2-spin{animation:none!important}}`;
  document.head.appendChild(s);
})();

// ── Каталог ──────────────────────────────────────────────────────────────────
const LAST = "3.57";
const TOPICS = {
  fw: { label: "Прошивка", icon: "system_update" },
  bypass: { label: "Обход и Clone", icon: "key" },
  settings: { label: "Настройки", icon: "tune" },
  state: { label: "Состояние", icon: "speed" },
  devices: { label: "Устройства", icon: "settings_remote" },
  cars: { label: "Автомобиль", icon: "directions_car" },
  support: { label: "Поддержка", icon: "support_agent" },
};
const TOPIC_ORDER = ["fw", "bypass", "settings", "state", "devices", "cars", "support"];
// t — тема; net — нужна сеть; can — нужен обновлённый CAN; ble — только Bluetooth.
const A2 = {
  fw_check: { t: "fw", label: "Проверить прошивку", icon: "system_update", net: 1 },
  fw_update: { t: "fw", label: "Обновить прошивку", icon: "system_update_alt", net: 1 },
  can_update: { t: "fw", label: "Обновить CAN", icon: "memory", net: 1 },
  archive: { t: "fw", label: "Архив прошивок", icon: "inventory_2", net: 1 },
  bypass_status: { t: "bypass", label: "Статус обхода и Clone", icon: "fact_check", can: 1 },
  bypass_run: { t: "bypass", label: "Обучить обход", icon: "key", can: 1 },
  clone_run: { t: "bypass", label: "Запустить Clone", icon: "content_copy", can: 1, net: 1 },
  find: { t: "settings", label: "Найти настройку", icon: "search", can: 1 },
  load: { t: "settings", label: "Загрузить настройки", icon: "file_download", can: 1 },
  all_settings: { t: "settings", label: "Все настройки", icon: "tune", can: 1 },
  configurator: { t: "settings", label: "Конфигуратор", icon: "folder_special" },
  state: { t: "state", label: "Состояние блока", icon: "speed", ble: 1 },
  state_more: { t: "state", label: "Проверка состояния", icon: "sensors", ble: 1 },
  cars: { t: "cars", label: "Выбрать автомобиль", icon: "directions_car" },
  devices: { t: "devices", label: "Устройства системы", icon: "settings_remote", ble: 1 },
  pairing: { t: "devices", label: "Приживить устройство", icon: "add_link", can: 1 },
  product: { t: "devices", label: "Информация о товаре", icon: "manage_search", net: 1, kw: ["товар", "комплект", "серийн", "штрих", "упаков"] },
  ticket_new: { t: "support", label: "Заявка на техпомощь", icon: "phone_callback", net: 1 },
  tickets: { t: "support", label: "Мои заявки", icon: "assignment", net: 1 },
  support: { t: "support", label: "Чат поддержки", icon: "support_agent" },
};
// Помощь после ошибки обхода / Clone / прошивки: заявка на техпомощь — первой, чат — вторым.
const HELP_G = { items: [{ k: "ticket_new", label: "Заявка на техпомощь", icon: "phone_callback", primary: true }, { k: "support", label: "Чат поддержки", icon: "support_agent" }] };
const TOPIC_ACTS = { fw: ["fw_update", "fw_check", "can_update", "archive"], bypass: ["bypass_status", "bypass_run", "clone_run"], settings: ["find", "load", "all_settings", "configurator"], state: ["state", "state_more"], devices: ["devices", "pairing", "product"], cars: ["cars"], support: ["ticket_new", "tickets", "support"] };
const INFEED = ["fw_check", "state", "bypass_status", "find", "load", "product"];
const QUICK2 = ["state", "load", "all_settings"];
const UNSUP2 = ["fw_check", "fw_update", "archive", "cars", "ticket_new", "tickets", "support"];
const KW = {
  fw: ["прошив", "обнов", "верси", "firmware", "загрузч", "can", "кан "],
  bypass: ["обход", "бесключ", "иммоб", "clone", "клон", "ключ"],
  settings: ["настро", "конфиг", "dxs", "парамет"],
  state: ["состоян", "датчик", "двер", "капот", "напряж", "питан", "багаж"],
  cars: ["автомоб", "машин", "марк"],
  devices: ["устройств", "метк", "брелок", "реле", "прижив", "привяз", "датчик движ"],
  support: ["поддерж", "чат", "помощ", "специал", "заявк", "звон", "перезвон"],
};
let SETS = null, SBY2 = null;
const initData = () => { if (SETS) return; SETS = K.SETTINGS; SBY2 = Object.fromEntries(SETS.map((s) => [s.id, s])); };
const stemsOf = (q) => q.toLowerCase().split(/[\s,.;:!?«»"]+/).filter((w) => w.length >= 3).map((w) => w.slice(0, Math.min(w.length, 6)));
const matchSettings = (q) => { const st = stemsOf(q); if (!st.length) return []; return SETS.map((s) => [s, st.filter((x) => (s.name + " " + s.path).toLowerCase().includes(x)).length]).filter(([, n]) => n > 0).sort((a, b) => b[1] - a[1]).map(([s]) => s); };
const matchTopics = (q) => { const t = q.toLowerCase(), st = stemsOf(q); return TOPIC_ORDER.filter((k) => KW[k].some((w) => t.includes(w)) || st.some((x) => TOPICS[k].label.toLowerCase().includes(x))); };
const matchActions = (q, tops) => { const st = stemsOf(q), t = q.toLowerCase(); return Object.keys(A2).filter((k) => tops.includes(A2[k].t) || st.some((x) => A2[k].label.toLowerCase().includes(x)) || (A2[k].kw || []).some((w) => t.includes(w))); };
// Номер в строке поиска (6+ цифр, пробелы и дефисы убираем) → «Информация о товаре №…».
const numOf = (q) => { const d = q.replace(/[\s\-]/g, ""); return /^\d{6,20}$/.test(d) ? d : null; };
// Короткая сводка для карточки журнала (данные — из ProductInfo.jsx).
const productData = (id, mode) => {
  if (mode === "notfound") return { notFound: true, id };
  const r = window.piLookup ? window.piLookup(id, "kit7") : null;
  if (!r) return { id, name: "Pandora DX-4GS" };
  return { id, name: r.product.name, serial: r.product.serial, date: r.product.date, kit: r.kit && { name: r.kit.name, n: r.kit.comps.length } };
};
const fwData2 = (dev, canF) => ({ mods: [{ name: "Основной блок", cur: dev.fw, last: LAST }, { name: "Bluetooth", cur: "1.14", last: "1.14" }, { name: "CAN", cur: canF ? "заводской" : "12.05", last: "12.05" }] });

function mkAvail(S) {
  return (k) => {
    const a = A2[k]; if (!a) return { ok: true };
    if (S.p.unsupported && !UNSUP2.includes(k)) return { ok: false, hidden: true };
    if (a.ble && S.p.transport !== "ble") return { ok: false, hidden: true };
    if (k === "fw_update" && !(S.fwChecked && S.dev.fw !== LAST)) return { ok: false, hidden: true };
    if (k === "can_update" && !S.p.can) return { ok: false, hidden: true };
    if (a.can && S.canF) return { ok: false, reason: "Нужно обновить CAN", full: "Заводской CAN. Обновите CAN, чтобы открыть настройки, обход и Clone.", fix: "can_update" };
    if (a.net && !S.online) return { ok: false, reason: "Нет сети", full: "Нет сети. Прошивка, Clone и облачные конфигурации работают через сервер — подключитесь к интернету." };
    return { ok: true };
  };
}
const actLabel = (k) => k === "fw_update" ? `Обновить до ${LAST}` : A2[k].label;
function topicStatus(t, S) {
  const { p, dev, canF, online, fwChecked, stateRead, changed } = S;
  if (t === "fw") return !online ? ["off", "Нет сети"] : canF ? ["warn", "CAN заводской"] : !fwChecked ? ["neutral", `ПО ${dev.fw} · не проверено`] : dev.fw !== LAST ? ["warn", `Есть ${LAST}`] : ["ok", "Актуальна"];
  if (t === "bypass") return canF ? ["lock", "Нужно обновить CAN"] : dev.clone ? ["ok", "Обход и Clone готовы"] : dev.bypass ? ["warn", "Обучен · нужен Clone"] : ["neutral", "Не обучен"];
  if (t === "settings") return canF ? ["lock", "Нужно обновить CAN"] : changed ? ["ok", `Изменено: ${changed}`] : ["neutral", "Без изменений"];
  if (t === "state") return stateRead ? ["warn", "Капот открыт"] : ["neutral", "Не проверено"];
  if (t === "cars") return ["neutral", p.unsupported ? "Список марок" : "VW Polo 2020"];
  if (t === "devices") return canF ? ["lock", "Нужно обновить CAN"] : p.transport === "ble" ? ["warn", "4 устройства · 1 вне зоны"] : ["neutral", "Метки, реле, датчики"];
  return S.ticketNo ? ["ok", `Заявка №${S.ticketNo} · ждите звонка`] : ["neutral", "Заявка или чат"];
}
// Маршрут монтажа: CAN → прошивка → обход → Clone → состояние.
function nextStep(S) {
  const { p, dev, canF, online, fwChecked, stateRead } = S;
  if (p.unsupported) return online ? { k: "fw_check", why: "Для этой модели доступна прошивка" } : { k: "support", why: "Модели нет в базе" };
  if (canF) return { k: "can_update", why: "Заводской CAN закрывает настройки, обход и Clone" };
  if (online && !fwChecked) return { k: "fw_check", why: `Сейчас ${dev.fw}. Начните с версии` };
  if (online && dev.fw !== LAST) return { k: "fw_update", why: `Сейчас ${dev.fw}, настройки сохранятся` };
  if (!dev.bypass) return { k: "bypass_run", why: "Бесключевой обход не обучен" };
  if (!dev.clone && online) return { k: "clone_run", why: "Обход обучен, для авто нужен Clone" };
  if (p.transport === "ble" && !stateRead) return { k: "state", why: "Проверьте датчики перед сдачей" };
  return { k: "find", why: "Маршрут пройден. Донастройте блок" };
}

// ── Утилиты ──────────────────────────────────────────────────────────────────
function useLayer() {
  const [h, setH] = useState(false), [pr, setP] = useState(false), [f, setF] = useState(false);
  return { o: pr ? 0.12 : f ? 0.1 : h ? 0.08 : 0, h: { onMouseEnter: () => setH(true), onMouseLeave: () => { setH(false); setP(false); }, onMouseDown: () => setP(true), onMouseUp: () => setP(false), onFocus: () => setF(true), onBlur: () => setF(false) } };
}
function useWidth2() {
  const ref = useRef(null); const [sz, setSz] = useState([0, 0]);
  useEffect(() => { const el = ref.current; if (!el) return; const m = () => setSz([el.offsetWidth, el.offsetHeight]); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, []);
  const [w, h] = sz;
  // Низкое окно (телефон-ландшафт, высота < 480 dp): отдельная раскладка — пульт слева узкой колонкой, лента справа.
  const low = w >= 560 && h > 0 && h < 480;
  return [ref, low ? "low" : w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact"];
}
const MONO = { fontFamily: "var(--font-mono)", fontSize: "var(--body-medium-size)", lineHeight: "var(--body-medium-line)", color: "var(--on-surface-variant)" };
const TONE = { ok: "var(--success)", warn: "var(--tertiary)", lock: "var(--tertiary)", err: "var(--error)", off: "var(--outline)", neutral: "var(--outline)" };

// ── Плитка пульта ────────────────────────────────────────────────────────────
// v: hero (следующий шаг) | topic (есть подуровень) | action | setting
function Tile({ v, icon, title, kicker, sub, tone = "neutral", value, primary, blocked, reason, more, nav, onClick, pulse, side }) {
  const L = useLayer();
  const hero = v === "hero", setting = v === "setting";
  const filled = (hero || primary) && !blocked;
  const corner = blocked ? "lock" : more ? "chevron_right" : nav ? "open_in_new" : null;
  const subColor = filled ? "currentColor" : blocked || tone === "warn" || tone === "lock" ? "var(--tertiary)" : "var(--on-surface-variant)";
  return (
    <button type="button" className={"a2-f" + (pulse ? " a2-pulse" : "")} onClick={onClick} {...L.h}
      style={{ position: "relative", overflow: "hidden", flex: side ? undefined : "0 0 auto", width: side ? "100%" : hero ? 264 : 160, gridColumn: side && hero ? "1 / -1" : undefined, minHeight: side && hero ? 112 : 124, padding: 14, borderRadius: 20, boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "stretch", gap: 8, textAlign: "left", cursor: "pointer", scrollSnapAlign: "start", fontFamily: "var(--font-sans)",
        background: filled ? "var(--primary)" : blocked ? "var(--surface-container)" : "var(--asst-card)", color: filled ? "var(--on-primary)" : "var(--on-surface)", border: filled ? "1px solid transparent" : blocked ? "1.5px dashed var(--outline)" : "1px solid var(--outline-variant)", boxShadow: filled ? "var(--elevation-1)" : "none", transition: "background var(--motion-fast)" }}>
      <span style={{ position: "absolute", inset: 0, background: filled ? "var(--on-primary)" : "var(--on-surface)", opacity: L.o, pointerEvents: "none" }} />
      <span style={{ position: "relative", display: "flex", alignItems: "center", gap: 10, minHeight: setting ? 24 : 40 }}>
        {setting
          ? <><CmpIcon icon="tune" size={20} color="var(--primary)" /><span style={{ fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500, color: "var(--on-surface-variant)" }}>Настройка</span></>
          : <span style={{ width: 40, height: 40, borderRadius: 12, flex: "0 0 auto", display: "grid", placeItems: "center", background: filled ? "color-mix(in srgb, var(--on-primary) 18%, transparent)" : blocked ? "var(--surface-container-highest)" : "var(--primary-container)", color: filled ? "var(--on-primary)" : blocked ? "var(--on-surface-variant)" : "var(--on-primary-container)" }}><CmpIcon icon={icon} size={22} /></span>}
        {hero && <span style={{ display: "inline-flex", alignItems: "center", gap: 4, fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500 }}><CmpIcon icon="bolt" size={18} />{kicker}</span>}
        <span style={{ flex: 1 }} />
        {corner && <CmpIcon icon={corner} size={20} color={filled ? "currentColor" : "var(--on-surface-variant)"} />}
        {hero && !blocked && <CmpIcon icon="arrow_forward" size={24} />}
      </span>
      <span style={{ position: "relative" }}><CmpText variant={hero ? "titleLarge" : "titleMedium"} color="currentColor" maxLines={2}>{title}</CmpText></span>
      <span style={{ position: "relative", marginTop: "auto", display: "flex", alignItems: "center", gap: 6, minWidth: 0 }}>
        {value != null ? <K.Pill>{value}</K.Pill> : <>
          {v === "topic" && !blocked && <span style={{ width: 8, height: 8, borderRadius: 999, flex: "0 0 auto", background: TONE[tone] }} />}
          <span style={{ fontSize: "var(--label-large-size)", lineHeight: "var(--label-large-line)", fontWeight: 500, color: subColor, overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>{blocked ? reason : sub}</span>
        </>}
      </span>
    </button>
  );
}
// Строка результата поиска (боковая панель): название целиком, значение или признак перехода справа.
function Row({ v, icon, title, sub, tone = "neutral", value, primary, blocked, reason, more, nav, onClick, q }) {
  const L = useLayer();
  const filled = primary && !blocked;
  const setting = v === "setting";
  const corner = blocked ? "lock" : more ? "chevron_right" : nav ? "open_in_new" : null;
  const hl = (t) => { const st = stemsOf(q || ""); const low = t.toLowerCase(); const hit = st.map((x) => [low.indexOf(x), x.length]).filter(([i]) => i >= 0).sort((x, y) => x[0] - y[0])[0]; if (!hit) return t; const [i, n] = hit; return <>{t.slice(0, i)}<mark style={{ background: "var(--primary-container)", color: "var(--on-primary-container)", borderRadius: 3 }}>{t.slice(i, i + n)}</mark>{t.slice(i + n)}</>; };
  return (
    <button type="button" className="a2-f" onClick={onClick} {...L.h}
      style={{ position: "relative", overflow: "hidden", display: "flex", alignItems: "center", gap: 12, width: "100%", minHeight: 64, padding: "10px 12px", borderRadius: 16, boxSizing: "border-box", textAlign: "left", cursor: "pointer", fontFamily: "var(--font-sans)",
        background: filled ? "var(--primary)" : blocked ? "var(--surface-container)" : "var(--asst-card)", color: filled ? "var(--on-primary)" : "var(--on-surface)", border: filled ? "1px solid transparent" : blocked ? "1.5px dashed var(--outline)" : "1px solid var(--outline-variant)" }}>
      <span style={{ position: "absolute", inset: 0, background: filled ? "var(--on-primary)" : "var(--on-surface)", opacity: L.o, pointerEvents: "none" }} />
      <span style={{ position: "relative", width: 40, height: 40, borderRadius: 12, flex: "0 0 auto", display: "grid", placeItems: "center", background: filled ? "color-mix(in srgb, var(--on-primary) 18%, transparent)" : blocked ? "var(--surface-container-highest)" : setting ? "var(--surface-container-high)" : "var(--primary-container)", color: filled ? "var(--on-primary)" : blocked ? "var(--on-surface-variant)" : setting ? "var(--primary)" : "var(--on-primary-container)" }}>
        <CmpIcon icon={setting ? "tune" : icon} size={22} />
      </span>
      <span style={{ position: "relative", flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <CmpText variant="titleMedium" color="currentColor">{hl(title)}</CmpText>
        <span style={{ display: "flex", alignItems: "center", gap: 6, fontSize: "var(--body-medium-size)", lineHeight: "var(--body-medium-line)", color: filled ? "currentColor" : blocked || tone === "warn" || tone === "lock" ? "var(--tertiary)" : "var(--on-surface-variant)" }}>
          {v === "topic" && !blocked && <span style={{ width: 8, height: 8, borderRadius: 999, flex: "0 0 auto", background: TONE[tone] }} />}
          {blocked ? reason : sub}
        </span>
      </span>
      {value != null && <span style={{ position: "relative" }}><K.Pill>{value}</K.Pill></span>}
      {value == null && corner && <span style={{ position: "relative", display: "grid" }}><CmpIcon icon={corner} size={22} color={filled ? "currentColor" : "var(--on-surface-variant)"} /></span>}
    </button>
  );
}
function Carousel({ tiles }) {
  const ref = useRef(null);
  const onWheel = (e) => { const el = ref.current; if (el && Math.abs(e.deltaY) > Math.abs(e.deltaX)) el.scrollLeft += e.deltaY; };
  return (
    <div ref={ref} onWheel={onWheel} className="a2-nosb" style={{ display: "flex", gap: 8, overflowX: "auto", padding: "2px 16px 4px", scrollSnapType: "x proximity", scrollPaddingLeft: 16 }}>
      {tiles.map((t) => <Tile key={t.id} {...t} />)}
    </div>
  );
}
function LevelHead({ head, onBack, onMini, pad = "0 8px 0 16px" }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 4, minHeight: 48, padding: pad }}>
      {head.kind === "topic" && <span style={{ marginLeft: -12 }}><CmpIconButton icon="ArrowBack" contentDescription="К темам" onClick={onBack} /></span>}
      <div style={{ flex: 1, minWidth: 0, display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" }}>
        <CmpText variant="titleMedium">{head.title}</CmpText>
        {head.status && <K.Pill tone={head.status[0] === "ok" ? "ok" : head.status[0] === "warn" || head.status[0] === "lock" ? "warn" : "neutral"}>{head.status[1]}</K.Pill>}
      </div>
      {onMini && <CmpIconButton icon="expand_more" contentDescription="Свернуть пульт" onClick={onMini} />}
    </div>
  );
}
function SearchRow({ q, setQ, onSubmit, onFocus, formRef, pad = 16 }) {
  const has = !!q.trim();
  return (
    <form ref={formRef} onSubmit={(e) => { e.preventDefault(); onSubmit(); }} onFocusCapture={onFocus} style={{ padding: `0 ${pad}px`, display: "flex", gap: 8, alignItems: "center" }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpTextField value={q} onChange={setQ} placeholder="Найти действие или настройку" singleLine leadingIcon={<CmpIcon icon="search" size={24} color="var(--on-surface-variant)" />}
          trailingIcon={q ? <CmpIconButton icon="close" contentDescription="Очистить" onClick={() => setQ("")} /> : undefined} rootStyle={{ width: "100%" }} />
      </div>
      <button type="submit" className="a2-f" aria-label="Показать в ленте" disabled={!has}
        style={{ width: 56, height: 56, borderRadius: 16, border: "none", flex: "0 0 auto", display: "grid", placeItems: "center", cursor: has ? "pointer" : "default", background: has ? "var(--primary)" : "var(--surface-container-highest)", color: has ? "var(--on-primary)" : "var(--on-surface-variant)", transition: "background var(--motion-fast)" }}>
        <CmpIcon icon="arrow_forward" size={26} />
      </button>
    </form>
  );
}

// ── Блоки ленты ──────────────────────────────────────────────────────────────
function StepLabel({ b }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 6, minHeight: 32, flexWrap: "wrap" }}>
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">{b.path[0]}</CmpText>
      <CmpIcon icon="chevron_right" size={18} color="var(--on-surface-variant)" />
      <CmpText variant="titleMedium">{b.path[1]}</CmpText>
      <span style={{ ...MONO, marginLeft: "auto", paddingLeft: 8 }}>{b.time}</span>
    </div>
  );
}
function Msg2({ b }) {
  return (
    <div style={{ display: "flex", gap: 10, alignItems: "flex-start", paddingTop: 4 }}>
      {b.tone === "warn" && <CmpIcon icon="lock" size={22} color="var(--tertiary)" />}
      <div style={{ display: "flex", flexDirection: "column", gap: 4, minWidth: 0 }}>
        {b.title && <CmpText variant="titleMedium">{b.title}</CmpText>}
        {b.text && <CmpText variant="bodyLarge" style={{ textWrap: "pretty" }}>{b.text}</CmpText>}
      </div>
    </div>
  );
}
function Diffs({ rows }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", borderRadius: 12, border: "1px solid var(--outline-variant)", overflow: "hidden" }}>
      {rows.map(([l, from, to], i) => (
        <div key={l} style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 52, padding: "6px 12px", borderTop: i ? "1px solid var(--outline-variant)" : "none", flexWrap: "wrap" }}>
          <div style={{ flex: "1 1 120px", minWidth: 0 }}><CmpText variant="bodyLarge">{l}</CmpText></div>
          <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
            <span style={{ ...MONO, textDecoration: "line-through" }}>{from}</span>
            <CmpIcon icon="arrow_forward" size={18} color="var(--on-surface-variant)" />
            <K.Pill tone="info">{to}</K.Pill>
          </div>
        </div>
      ))}
    </div>
  );
}
// Возврат с другого экрана: сначала перечитываем блок, потом итог со сравнением «до → после».
function ReturnV2({ b }) {
  const checking = b.status === "checking";
  const T = checking ? ["sync", "var(--primary)", "var(--surface-container-high)", "var(--on-surface)"]
    : { ok: ["check_circle", "var(--success)", "var(--success-highlight)", "var(--on-surface)"], error: ["error", "var(--error-default)", "var(--error-highlight)", "var(--on-error-container)"], stop: ["pause_circle", "var(--on-tertiary-container)", "var(--tertiary-container)", "var(--on-tertiary-container)"], none: ["info", "var(--on-surface-variant)", "var(--surface-container-high)", "var(--on-surface)"] }[b.tone];
  return (
    <CmpSurface color="var(--asst-card)" radius="var(--r-card-small-radius)" border style={{ boxSizing: "border-box", overflow: "hidden" }}>
      <div style={{ display: "flex", gap: 12, alignItems: "center", padding: "12px 16px", background: T[2], color: T[3] }}>
        <span className={checking ? "a2-spin" : undefined} style={{ display: "inline-grid" }}><CmpIcon icon={T[0]} size={28} color={T[1]} /></span>
        <div style={{ minWidth: 0, display: "flex", flexDirection: "column" }}>
          <CmpText variant="labelLarge" color="currentColor">{`Возврат из «${b.from}»`}</CmpText>
          <CmpText variant="titleMedium" color="currentColor">{checking ? "Перечитываем блок" : b.title}</CmpText>
        </div>
      </div>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
        {checking ? <><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Сравниваем с состоянием до перехода</CmpText><K.Skeleton rows={2} /></> : <>
          {b.diffs && <Diffs rows={b.diffs} />}
          {b.text && <CmpText variant="bodyLarge" style={{ textWrap: "pretty" }}>{b.text}</CmpText>}
        </>}
      </div>
    </CmpSurface>
  );
}
// Подтверждение записи: pending | pin | writing | done | error | cancelled. b.unsaved — правки в редакторе.
function ConfirmV2({ b, ctx }) {
  const s = SBY2[b.sid], st = b.status, fmt = K.fmt;
  const H = { pending: ["edit_note", "Записать в блок?", "var(--primary)", "var(--on-primary)"], pin: ["pin", "Нужен PIN блока", "var(--primary)", "var(--on-primary)"], writing: ["sync", "Записываем в блок", "var(--primary)", "var(--on-primary)"], done: ["check", "Записано в блок", "var(--success)", "var(--on-primary)"], error: ["error", "Не удалось записать", "var(--error)", "var(--on-error)"], cancelled: ["block", "Запись отменена", "var(--background-surface-highlight)", "var(--foreground-on-dark-secondary-default)"] }[st];
  const heavy = ["pending", "pin", "writing", "error"].includes(st);
  const btn = { flex: "1 1 140px", height: 48 };
  return (
    <CmpSurface color="var(--asst-card)" radius="var(--r-card-medium-radius)" border={!heavy} style={{ boxSizing: "border-box", overflow: "hidden", opacity: st === "cancelled" ? 0.72 : 1, ...(heavy ? { border: `2px solid ${st === "error" ? "var(--error)" : "var(--primary)"}`, boxShadow: "var(--elevation-3)" } : null) }}>
      <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <span className={st === "writing" ? "a2-spin" : undefined} style={{ width: 40, height: 40, borderRadius: 999, background: H[2], color: H[3], display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={H[0]} size={24} /></span>
          <CmpText variant="titleLarge">{H[1]}</CmpText>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <CmpText variant="titleMedium">{s.name}</CmpText>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{s.path}</CmpText>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: "minmax(0,1fr) 32px minmax(0,1fr)", gap: 4 }}>
          <div style={{ padding: "10px 12px", borderRadius: 12, background: "var(--surface-container)", display: "flex", flexDirection: "column" }}>
            <CmpText variant="labelLarge" color="var(--on-surface-variant)">Было</CmpText><CmpText variant="titleLarge">{fmt(s, b.from)}</CmpText>
          </div>
          <div style={{ display: "grid", placeItems: "center", color: "var(--on-surface-variant)" }}><CmpIcon icon="arrow_forward" size={24} /></div>
          <div style={{ padding: "10px 12px", borderRadius: 12, background: st === "cancelled" ? "var(--surface-container)" : "var(--primary-container)", color: st === "cancelled" ? "var(--on-surface)" : "var(--on-primary-container)", display: "flex", flexDirection: "column" }}>
            <CmpText variant="labelLarge" color="currentColor">{st === "done" ? "Стало" : "Станет"}</CmpText><CmpText variant="titleLarge" color="currentColor">{fmt(s, b.to)}</CmpText>
          </div>
        </div>
        {b.unsaved > 0 && (st === "pending" || st === "pin") && (
          <div style={{ display: "flex", gap: 12, padding: 12, borderRadius: 12, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)" }}>
            <CmpIcon icon="warning" size={22} />
            <div style={{ display: "flex", flexDirection: "column", gap: 2, minWidth: 0 }}>
              <CmpText variant="titleSmall" color="currentColor">{`В редакторе настроек ${b.unsaved} несохранённые правки`}</CmpText>
              <CmpText variant="bodyMedium" color="currentColor">Запишется только эта настройка. Правки в редакторе останутся несохранёнными.</CmpText>
            </div>
          </div>
        )}
        {b.note && st === "pending" && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{b.note}</CmpText>}
        {st === "pending" && (
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
            <CmpButton text={b.unsaved ? "Записать только эту" : "Записать"} icon="save" style={btn} onClick={() => ctx.write(b)} />
            {b.unsaved > 0 && <CmpOutlinedButton text="Открыть редактор" icon="open_in_new" style={btn} onClick={() => ctx.openSetting(s)} />}
            <CmpOutlinedButton text="Отмена" style={btn} onClick={() => ctx.patch(b.id, { status: "cancelled" })} />
          </div>
        )}
        {st === "pin" && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Запись требует режима программирования. Введите PIN в диалоге блока.</CmpText>}
        {st === "writing" && <div style={{ display: "flex", flexDirection: "column", gap: 8 }}><K.Skeleton rows={1} /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Не отключайте блок до окончания записи</CmpText></div>}
        {st === "error" && <>
          <CmpText variant="bodyLarge">{b.err || "Блок не ответил. Значение в блоке не изменилось."}</CmpText>
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
            <CmpButton text="Повторить" icon="refresh" style={btn} onClick={() => ctx.write(b)} />
            <CmpOutlinedButton text="Отмена" style={btn} onClick={() => ctx.patch(b.id, { status: "cancelled" })} />
          </div>
        </>}
        {st === "done" && b.unsaved > 0 && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{`Правки в редакторе не тронуты: ${b.unsaved}.`}</CmpText>}
        {st === "cancelled" && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок не изменён.</CmpText>}
      </div>
    </CmpSurface>
  );
}
function Block2({ b, ctx }) {
  if (b.t === "msg") return <Msg2 b={b} />;
  if (b.t === "step") return <StepLabel b={b} />;
  if (b.t === "card") return <K.CardBlock b={b} ctx={ctx} />;
  if (b.t === "confirm") return <ConfirmV2 b={b} ctx={ctx} />;
  if (b.t === "ret") return <ReturnV2 b={b} />;
  if (b.t === "source") return <K.SourceBlock b={b} ctx={ctx} />;
  return null;
}
// Журнал: рейка слева; узлы — метка ассистента и шаги монтажника.
function Feed2({ feed, ctx }) {
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {feed.map((b, i) => {
        const last = i === feed.length - 1;
        const node = i === 0 ? "mark" : b.t === "step" ? "step" : null;
        return (
          <div key={b.id} className="a2-up" style={{ position: "relative", display: "grid", gridTemplateColumns: "32px minmax(0,1fr)", columnGap: 12, paddingBottom: last ? 0 : 16, paddingTop: node === "step" && i > 0 ? 8 : 0 }}>
            {!last && <span style={{ position: "absolute", left: 15, top: node ? (node === "step" ? 40 : 32) : 0, bottom: 0, width: 2, background: "var(--outline-variant)" }} />}
            {node === "step" && <span style={{ position: "absolute", left: 15, top: 0, height: 8, width: 2, background: "var(--outline-variant)" }} />}
            <div style={{ position: "relative" }}>
              {node === "mark" && <K.Mark size={32} />}
              {node === "step" && <span style={{ width: 32, height: 32, borderRadius: 999, background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center" }}><CmpIcon icon={b.icon} size={18} /></span>}
            </div>
            <div style={{ minWidth: 0 }}><Block2 b={b} ctx={ctx} /></div>
          </div>
        );
      })}
    </div>
  );
}

// ── Оверлеи существующих экранов ─────────────────────────────────────────────
function BootOverlay({ boot }) {
  const { pct, fail } = boot;
  const title = fail ? "Связь с загрузчиком потеряна" : pct < 15 ? "Блок перезагружается в загрузчик" : pct < 92 ? `Передаём прошивку ${LAST}` : "Блок перезапускается";
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 8, background: "var(--background)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 20, padding: 32, textAlign: "center" }}>
      <div style={MONO}>существующий экран обновления · поверх всего графа навигации</div>
      <CmpIcon icon={fail ? "usb_off" : "system_update_alt"} size={64} color={fail ? "var(--error)" : "var(--primary)"} />
      <CmpText variant="headlineSmall">{title}</CmpText>
      <div style={{ width: "100%", maxWidth: 360, height: 8, borderRadius: 4, background: "var(--surface-container-highest)", overflow: "hidden" }}>
        <div style={{ width: pct + "%", height: "100%", background: fail ? "var(--error)" : "var(--primary)", transition: "width .14s linear" }} />
      </div>
      <CmpText variant="titleMedium">{`${pct}%`}</CmpText>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{fail ? "Возвращаемся в ассистент" : "Не отключайте питание блока"}</CmpText>
    </div>
  );
}
function PinDialog({ onOk, onCancel }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 9, background: "rgba(0,0,0,.45)", display: "grid", placeItems: "center", padding: 24 }}>
      <div role="dialog" aria-label="PIN блока" style={{ width: "100%", maxWidth: 360, borderRadius: 28, background: "var(--surface-container-high)", color: "var(--on-surface)", padding: 24, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={MONO}>существующий диалог PIN · не перерисовывается</div>
        <CmpIcon icon="pin" size={28} color="var(--secondary)" />
        <CmpText variant="headlineSmall">Введите PIN блока</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Нужен режим программирования для записи настройки</CmpText>
        <div style={{ display: "flex", gap: 12, justifyContent: "center" }}>{[0, 1, 2, 3].map((i) => <span key={i} style={{ width: 52, height: 60, borderRadius: 12, border: `1.5px solid ${i === 0 ? "var(--primary)" : "var(--outline)"}` }} />)}</div>
        <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", flexWrap: "wrap" }}>
          <CmpTextButton text="Отмена" onClick={onCancel} />
          <CmpButton text="PIN введён" onClick={onOk} />
        </div>
      </div>
    </div>
  );
}

// ── Экран ────────────────────────────────────────────────────────────────────
function AssistantScreen({ profile = "ble", canFactory = false, online = true, errMode = false, pinRequired = false, unsaved = 0, preset = "first", keyboardDemo = false, productMode = "found", platform = "mobile", onBack, apiRef }) {
  initData();
  const p = K.PROFILES[profile] || K.PROFILES.ble;
  const long = preset === "long";
  const [rootRef, layout] = useWidth2();
  const compact = layout === "compact", side = layout === "expanded", low = layout === "low";
  const [lowHdr, setLowHdr] = useState(false);
  const [canF, setCanF] = useState(!!canFactory && !!p.can);
  const [dev, setDev] = useState({ fw: long ? LAST : "3.56", bypass: long, clone: false, cloneLeft: 3 });
  const [fwChecked, setFwChecked] = useState(long);
  const [stateRead, setStateRead] = useState(long && p.transport === "ble");
  const [changed, setChanged] = useState(long ? 1 : 0);
  const [ticketNo, setTicketNo] = useState(null);
  const [vals, setVals] = useState(() => { const v = Object.fromEntries(SETS.map((s) => [s.id, s.value])); if (long) v.as_temp = true; return v; });
  const [pinOk, setPinOk] = useState(false);
  const clock = useRef(30), nid = useRef(1);
  const tick = () => `9:${String(clock.current++).padStart(2, "0")}`;
  const mk = (b) => ({ id: nid.current++, ...b });
  const [feed, setFeed] = useState(() => {
    const cf = !!canFactory && !!p.can;
    const g = greet2(p, cf, online);
    if (!long) return [mk(g)];
    const st = (icon, a, b2) => mk({ t: "step", icon, path: [a, b2], time: tick() });
    const out = [mk(g),
      st("system_update", "Прошивка", "Проверить прошивку"), mk({ t: "card", kind: "fw", status: "data", data: fwData2({ fw: "3.56" }) }),
      st("system_update_alt", "Прошивка", `Обновить до ${LAST}`), mk({ t: "ret", from: "Прошивка", status: "done", tone: "ok", title: `Прошивка обновлена до ${LAST}`, diffs: [["Основной блок", "3.56", LAST]], text: "Блок перезагружался в загрузчик. Сессия и лента восстановлены." }),
      st("key", "Обход и Clone", "Обучить обход"), mk({ t: "ret", from: "Бесключевой обход", status: "done", tone: "ok", title: "Обход обучен", diffs: [["Бесключевой обход", "не обучен", "обучен"]], text: "Для этого автомобиля нужен Clone." }),
      st("search", "Поиск", "«темп»"), mk({ t: "card", kind: "settings", status: "data", q: "темп", data: { found: matchSettings("темп") } }),
      mk({ t: "confirm", sid: "as_temp", from: false, to: true, status: "done" })];
    if (p.transport === "ble") out.push(st("speed", "Состояние", "Состояние блока"), mk({ t: "card", kind: "state", status: "data", data: K.stateData() }));
    return out;
  });
  const [q, setQ] = useState(keyboardDemo ? "бесключевой обход" : "");
  const [kb, setKb] = useState(keyboardDemo);
  const [away, setAway] = useState(null), [lost, setLost] = useState(false), [boot, setBoot] = useState(null), [pinAsk, setPinAsk] = useState(null);
  const [dock, setDock] = useState("open");
  const [lvl, setLvl] = useState(long ? { kind: "topic", t: "bypass" } : { kind: "home" });
  const [dir, setDir] = useState("a2-r");
  const [pulse, setPulse] = useState(null);
  const [pi, setPi] = useState(null);
  const [hdr, setHdr] = useState("auto"), [scrolled, setScrolled] = useState(false);
  const feedRef = useRef(null), formRef = useRef(null), anchorY = useRef(0);
  const st = useRef({}); st.current = { lost, errMode, dev, vals, feed, canF, changed, pinOk, productMode };
  const S = { p, dev, canF, online, fwChecked, stateRead, changed, ticketNo };
  const avail = mkAvail(S);
  const ns = nextStep(S);

  const push = (...bs) => { setFeed((f) => [...f, ...bs]); setHdr("auto"); };
  const patch = (id, pt) => setFeed((f) => f.map((b) => b.id === id ? { ...b, ...(typeof pt === "function" ? pt(b) : pt) } : b));
  const openTopic = (t) => { setQ(""); setDir("a2-r"); setLvl({ kind: "topic", t }); setDock("open"); };
  const goHome = () => { setDir("a2-l"); setLvl({ kind: "home" }); };
  const pulseT = (t) => { setPulse(t); setTimeout(() => setPulse((x) => x === t ? null : x), 2400); };

  const lenRef = useRef(feed.length);
  useEffect(() => { const el = feedRef.current; if (!el || feed.length === lenRef.current) return; lenRef.current = feed.length; requestAnimationFrame(() => el.scrollTo({ top: el.scrollHeight, behavior: "smooth" })); }, [feed.length]);
  useEffect(() => { if (long) setTimeout(() => { const el = feedRef.current; if (el) el.scrollTop = el.scrollHeight; }, 350); }, []);

  const AFTER = { product: () => openTopic("devices"), fw: () => { setFwChecked(true); openTopic("fw"); pulseT("fw"); }, state: () => { setStateRead(true); openTopic("state"); pulseT("state"); }, bypass: () => openTopic("bypass"), settings: () => openTopic("settings"), none: () => openTopic("settings") };
  const resolve = (id, kind, attempt, extra = {}) => setTimeout(() => {
    const s = st.current;
    if (s.lost) return patch(id, { status: "error", err: "Связь с блоком прервалась во время запроса. Повторите, когда блок снова на связи." });
    if (s.errMode && attempt === 0) return patch(id, { status: "error", err: "Блок не ответил за 10 секунд. Проверьте подключение и повторите." });
    if (kind === "product" && s.productMode === "offline" && attempt === 0) return patch(id, { status: "error", errTone: "net", err: "Нет подключения к интернету. Информация о товаре загружается с сервера." });
    let data, k = kind;
    if (kind === "product") data = productData(p.id, s.productMode);
    if (kind === "fw") data = fwData2(s.dev, s.canF);
    if (kind === "state") data = K.stateData();
    if (kind === "bypass") data = { ...s.dev };
    if (kind === "settings") { const found = matchSettings(extra.q || ""); if (!found.length) k = "none"; data = { found }; }
    patch(id, { status: "data", kind: k, data });
    AFTER[k] && AFTER[k]();
  }, 1300);
  const loadCard = (kind, extra = {}) => { const c = mk({ t: "card", kind, status: "loading", attempt: 0, ...extra }); push(c); resolve(c.id, kind, 0, extra); };
  const retry = (b) => { const a = (b.attempt || 0) + 1, kind = b.kind === "none" ? "settings" : b.kind; patch(b.id, { status: "loading", attempt: a }); resolve(b.id, kind, a, { q: b.q }); };

  const leave = (spec) => { setKb(false); setAway({ ...spec, outcomes: spec.outcomes.map((o) => ({ ...o, ret: o.res && o.res.tone !== "none" ? o.res : null })) }); };
  const finishReturn = (spec, o) => {
    const r = mk({ t: "ret", from: spec.title, status: "checking" }); push(r);
    setTimeout(() => {
      o.eff && o.eff();
      patch(r.id, { status: "done", ...(o.res || { tone: "none", title: "Блок не изменился", text: "Состояние до и после перехода совпадает." }) });
      if (o.topic) { openTopic(o.topic); pulseT(o.topic); } else goHome();
    }, 1400);
  };
  const startBoot = (spec, o) => {
    const fail = o.boot === "fail"; let pct = 0; setBoot({ pct, fail: false });
    const iv = setInterval(() => {
      pct = Math.min(100, pct + 4);
      if (fail && pct >= 64) { clearInterval(iv); setBoot({ pct, fail: true }); setTimeout(() => { setBoot(null); finishReturn(spec, o); }, 1500); return; }
      setBoot({ pct, fail: false });
      if (pct >= 100) { clearInterval(iv); setTimeout(() => { setBoot(null); finishReturn(spec, o); }, 700); }
    }, 120);
  };
  const onReturn = (o) => { const spec = away; setAway(null); if (o.boot) startBoot(spec, o); else finishReturn(spec, o); };

  const simple = (title) => ({ title, outcomes: [{ label: "Назад без изменений" }] });
  const navSpec = (k) => {
    const s = st.current, fw = s.dev.fw, n = s.dev.cloneLeft;
    switch (k) {
      case "fw_update": return { title: "Прошивка", note: `Обновление ${fw} → ${LAST}`, outcomes: [
        { label: "Прошивка обновлена", boot: true, eff: () => setDev((d) => ({ ...d, fw: LAST })), res: { tone: "ok", title: `Прошивка обновлена до ${LAST}`, diffs: [["Основной блок", fw, LAST]], text: "Блок перезагружался в загрузчик. Сессия и лента восстановлены." }, topic: "fw" },
        { label: "Связь пропала в загрузчике", boot: "fail", res: { tone: "error", title: "Прошивка не завершена", diffs: [["Основной блок", fw, "загрузчик"]], text: "Блок остался в режиме загрузчика. Подключите его снова и повторите обновление: экран прошивки продолжит с места остановки.", g: HELP_G }, topic: "fw" },
        { label: "Прервано монтажником", res: { tone: "stop", title: "Обновление прервано", text: `Версия осталась ${fw}.` }, topic: "fw" }] };
      case "can_update": return { title: "Прошивка", note: "Обновление CAN под код автомобиля", outcomes: [
        { label: "CAN обновлён", eff: () => setCanF(false), res: { tone: "ok", title: "CAN обновлён", diffs: [["CAN", "заводской", `${p.can} · ${p.canCode}`]], text: "Настройки, обход и Clone открыты." } },
        { label: "Прервано", res: { tone: "stop", title: "CAN не обновлён", text: "Блок остался с заводским CAN." }, topic: "fw" }] };
      case "bypass_run": return { title: "Бесключевой обход", outcomes: [
        { label: "Обход обучен", eff: () => setDev((d) => ({ ...d, bypass: true })), res: { tone: "ok", title: "Обход обучен", diffs: [["Бесключевой обход", "не обучен", "обучен"]], text: "Для этого автомобиля нужен Clone — он первым в теме." }, topic: "bypass" },
        { label: "Ошибка обучения", res: { tone: "error", title: "Обход не обучен", text: "Ключ не распознан. Положите ключ рядом с кнопкой запуска и повторите. Не получается — оставьте заявку, специалист перезвонит.", g: HELP_G }, topic: "bypass" },
        { label: "Прервано монтажником", res: { tone: "stop", title: "Обход не завершён", text: "Обучение остановлено на шаге 3 из 5. Блок не изменён." }, topic: "bypass" }] };
      case "clone_run": return { title: "Процедура Clone", outcomes: [
        { label: "Clone выполнен", eff: () => setDev((d) => ({ ...d, clone: true, cloneLeft: d.cloneLeft - 1 })), res: { tone: "ok", title: "Clone выполнен", diffs: [["Clone", "не выполнен", "выполнен"], ["Попытки Clone", `${n} из 3`, `${n - 1} из 3`]] } },
        { label: "Ошибка сервера", res: { tone: "error", title: "Clone не выполнен", text: "Сервер не вычислил ключ. Попытка не списана. Оставьте заявку — специалист перезвонит и разберёт ошибку.", g: HELP_G }, topic: "bypass" },
        { label: "Прервано монтажником", res: { tone: "stop", title: "Clone прерван", text: "Попытка не списана." }, topic: "bypass" }] };
      case "all_settings": return { title: "Настройки блока", outcomes: [
        { label: "Изменено и записано", eff: () => setChanged((c) => c + 2), res: { tone: "ok", title: "Настройки записаны", diffs: [["Изменено в сессии", String(s.changed), String(s.changed + 2)]] }, topic: "settings" },
        { label: "Назад без изменений", topic: "settings" }] };
      case "archive": return simple("Архив прошивок");
      case "state_more": return simple("Проверка состояния");
      case "configurator": return simple("Конфигуратор");
      case "cars": return simple("Автомобили");
      case "support": return simple("Чат техподдержки");
      case "tickets": return simple("Заявки на техпомощь");
      case "ticket_new": return { title: "Заявки на техпомощь", note: "Форма «Новая заявка»: имя и телефон из профиля, данные блока приложатся сами", outcomes: [
        { label: "Заявка отправлена", eff: () => setTicketNo(10483), res: { tone: "ok", title: "Заявка №10483 отправлена", text: "Специалист перезвонит по номеру +7 912 345-67-89. Статус — в теме «Поддержка»." }, topic: "support" },
        { label: "Много незакрытых заявок", res: { tone: "stop", title: "Заявка не отправлена", text: "У вас много незакрытых заявок. Дождитесь звонка или закройте лишние в «Мои заявки»." }, topic: "support" },
        { label: "Закрыто без отправки", topic: "support" }] };
      case "devices": return { title: "Устройства системы", outcomes: [
        { label: "Посмотрел, всё на связи", res: { tone: "ok", title: "Устройства на связи", text: "4 устройства, проблем нет." }, topic: "devices" },
        { label: "Метка вне зоны", res: { tone: "stop", title: "1 устройство требует внимания", text: "Метка 2 вне зоны. Поднесите её к блоку или приживите заново." }, topic: "devices" },
        { label: "Назад без изменений", topic: "devices" }] };
      case "pairing": return { title: "Приживление устройств", outcomes: [
        { label: "Устройство приживлено", res: { tone: "ok", title: "Метка 3 приживлена", diffs: [["Метки", "2 из 4", "3 из 4"]] }, topic: "devices" },
        { label: "Время вышло", res: { tone: "error", title: "Устройство не приживлено", text: "Блок не дождался устройства за 2 минуты. Проверьте батарею и повторите.", g: HELP_G }, topic: "devices" },
        { label: "Назад без изменений", topic: "devices" }] };
      default: return null;
    }
  };
  const step = (k, label) => mk({ t: "step", icon: A2[k].icon, path: [TOPICS[A2[k].t].label, label || actLabel(k)], time: tick() });
  const focusInput = () => { setDock("open"); const i = formRef.current && formRef.current.querySelector("input"); i && i.focus(); if (compact) setKb(true); };
  const act = (k) => {
    const av = avail(k);
    if (!av.ok) { push(mk({ t: "msg", tone: "warn", title: av.reason, text: av.full })); if (av.fix) goHome(); return; }
    if (k === "find") { focusInput(); return; }
    setQ(""); setKb(false);
    if (k === "fw_check") { push(step(k)); loadCard("fw"); return; }
    if (k === "state") { push(step(k)); loadCard("state"); return; }
    if (k === "bypass_status") { push(step(k)); loadCard("bypass"); return; }
    if (k === "load") { push(step(k), mk({ t: "source" })); openTopic("settings"); return; }
    if (k === "product") { push(step(k)); loadCard("product"); return; }
    const spec = navSpec(k); if (spec) { push(step(k)); leave(spec); }
  };
  const openSetting = (s) => {
    push(mk({ t: "step", icon: "open_in_new", path: ["Настройки", "Открыть в настройках"], time: tick() }));
    leave({ title: "Настройки блока", note: `Редактор откроется на «${s.name}»`, outcomes: [
      { label: "Изменено и записано", eff: () => setChanged((c) => c + 1), res: { tone: "ok", title: "Записано в редакторе", diffs: [[s.name, K.fmt(s, st.current.vals[s.id]), "изменено"]] }, topic: "settings" },
      { label: "Назад без изменений", topic: "settings" }] });
  };
  const settingToFeed = (s) => {
    if (!avail("find").ok) return act("find");
    setQ(""); setKb(false);
    push(mk({ t: "step", icon: "tune", path: ["Настройки", s.name], time: tick() }),
      mk({ t: "card", kind: "settings", status: "data", q: "", data: { found: [s] }, editing: s.type !== "other" ? s.id : null, draft: st.current.vals[s.id] }));
    openTopic("settings");
  };
  const openProduct = (n) => { setKb(false); setPi({ n }); };
  const productByNum = (n) => {
    const av = avail("product");
    if (!av.ok) { push(mk({ t: "msg", tone: "warn", title: av.reason, text: av.full })); return; }
    setQ(""); push(mk({ t: "step", icon: "manage_search", path: ["Устройства", `Информация о товаре №${n}`], time: tick() })); openProduct(n);
  };
  const submit = () => {
    const t = q.trim(); if (!t) return;
    const num = numOf(t); if (num && !avail("product").hidden) return productByNum(num);
    const sets = matchSettings(t), tops = matchTopics(t);
    if (!sets.length && tops.length) { setQ(""); setKb(false); openTopic(tops[0]); return; }
    if (!avail("find").ok) return act("find");
    setQ(""); setKb(false);
    push(mk({ t: "step", icon: "search", path: ["Поиск", `«${t}»`], time: tick() }));
    loadCard("settings", { q: t });
  };
  const toConfirm = (b, s) => { patch(b.id, { editing: null }); push(mk({ t: "confirm", sid: s.id, from: st.current.vals[s.id], to: b.draft, status: "pending", unsaved })); };
  const write = (b, pinPass) => {
    if (pinRequired && !st.current.pinOk && !pinPass) { patch(b.id, { status: "pin" }); setPinAsk(b.id); return; }
    patch(b.id, { status: "writing", note: null });
    setTimeout(() => {
      const s = st.current;
      if (s.lost) return patch(b.id, { status: "error", err: "Связь с блоком прервалась. Значение в блоке не изменилось." });
      if (s.errMode && !b.tries) return patch(b.id, { status: "error", tries: 1 });
      setVals((v) => ({ ...v, [b.sid]: b.to })); setChanged((c) => c + 1);
      patch(b.id, { status: "done" }); openTopic("settings"); pulseT("settings");
    }, 1500);
  };
  const pinOkFn = () => { const b = st.current.feed.find((x) => x.id === pinAsk); setPinOk(true); setPinAsk(null); b && write(b, true); };
  const pinCancel = () => { patch(pinAsk, { status: "pending", note: "PIN не введён. Блок не изменён." }); setPinAsk(null); };
  const pickSource = (b, src) => {
    patch(b.id, { used: src.k });
    leave({ title: "Конфигуратор", note: `Загрузка: ${src.title}. Редактор со списком изменений`, outcomes: [
      { label: "Записано в блок", eff: () => setChanged((c) => c + 14), res: { tone: "ok", title: "Настройки загружены", diffs: [["Изменено настроек", "0", "14"]], text: `Источник: ${src.title}.` }, topic: "settings" },
      { label: "Ошибка записи", res: { tone: "error", title: "Настройки не загружены", text: `Конфигурация не подходит для CAN-кода ${p.canCode}.` }, topic: "settings" },
      { label: "Отменено в редакторе", res: { tone: "stop", title: "Загрузка отменена", text: "Блок не изменён." }, topic: "settings" }] });
  };
  if (apiRef) apiRef.current = { drop: () => { act(p.transport === "ble" ? "state" : "fw_check"); setTimeout(() => setLost(true), 450); } };

  // ── Содержимое пульта ──
  const tileFor = (k) => { const av = avail(k), a = A2[k]; return { id: k, v: "action", icon: a.icon, title: actLabel(k), sub: INFEED.includes(k) ? "Ответ в ленте" : "Откроет экран", nav: !INFEED.includes(k), blocked: !av.ok, reason: av.reason, primary: av.ok && ns.k === k, onClick: () => act(k) }; };
  const topicTile = (t, sideMode) => {
    const acts = TOPIC_ACTS[t].filter((k) => !avail(k).hidden); if (!acts.length) return null;
    const blocked = acts.every((k) => !avail(k).ok), single = acts.length === 1, [tone, text] = topicStatus(t, S);
    return { id: "t-" + t, v: "topic", icon: TOPICS[t].icon, title: TOPICS[t].label, sub: text, tone, blocked, reason: blocked ? text : null, more: !single, nav: single && !INFEED.includes(acts[0]), pulse: pulse === t, side: sideMode, onClick: () => single ? act(acts[0]) : openTopic(t) };
  };
  const level = (sideMode, noSearch) => {
    const add = (arr) => arr.filter(Boolean).map((x) => ({ ...x, side: sideMode }));
    const tq = noSearch ? "" : q.trim();
    if (tq) {
      const tops = matchTopics(tq), singleTop = (k) => TOPIC_ACTS[A2[k].t].filter((x) => !avail(x).hidden).length === 1, acts = matchActions(tq, tops).filter((k) => !avail(k).hidden && !(tops.includes(A2[k].t) && singleTop(k))), sets = matchSettings(tq).slice(0, 6), fa = avail("find");
      const num = numOf(tq), pa = avail("product");
      const numTiles = num && !pa.hidden ? [{ id: "pn", v: "action", grp: "Товар", icon: "manage_search", title: `Информация о товаре №${num}`, sub: "Откроет экран", nav: true, primary: pa.ok, blocked: !pa.ok, reason: pa.reason, onClick: () => productByNum(num) }] : [];
      const tiles = [...numTiles, ...tops.map((t) => topicTile(t, sideMode)).filter(Boolean).map((x) => ({ ...x, grp: "Разделы" })), ...acts.map(tileFor).map((x) => ({ ...x, grp: "Действия" })), ...sets.map((s) => ({ id: "s-" + s.id, v: "setting", grp: "Настройки", title: s.name, sub: s.path, value: K.fmt(s, vals[s.id]), blocked: !fa.ok, reason: fa.reason, onClick: () => settingToFeed(s) }))];
      if (sets.length > 1) tiles.push({ id: "all", v: "action", grp: "Настройки", icon: "format_list_bulleted", title: `Все ${matchSettings(tq).length} настроек в ленту`, sub: "Список с правкой", onClick: submit });
      if (!tiles.length) return { key: "s0", head: { kind: "search", title: "Ничего не найдено" }, tiles: add([tileFor("ticket_new"), tileFor("support")].map((x) => ({ ...x, primary: false, grp: "Попробуйте" }))) };
      return { key: "s", head: { kind: "search", title: `Найдено: ${tiles.filter((x) => x.id !== "all").length}` }, tiles: add(tiles) };
    }
    if (lvl.kind === "topic") {
      const t = lvl.t;
      return { key: "t-" + t, head: { kind: "topic", title: TOPICS[t].label, status: topicStatus(t, S) }, tiles: add(TOPIC_ACTS[t].filter((k) => !avail(k).hidden).map(tileFor).sort((a, b) => (b.primary ? 1 : 0) - (a.primary ? 1 : 0))) };
    }
    const hav = avail(ns.k);
    const hero = { id: "hero", v: "hero", icon: A2[ns.k].icon, kicker: "Следующий шаг", title: actLabel(ns.k), sub: ns.why, blocked: !hav.ok, reason: hav.reason, pulse: pulse === "hero", onClick: () => act(ns.k) };
    return { key: "home", head: { kind: "home", title: "Пульт" }, tiles: add([hero, ...TOPIC_ORDER.map((t) => topicTile(t, sideMode))]) };
  };

  // Чипы в карточках ленты (помощь после ошибки) — живые: отмечают выбор и запускают действие.
  const chipAct = (k, blockId) => { if (blockId) patch(blockId, (b) => ({ g: { ...b.g, used: k } })); act(k); };
  const ctx = { look: "A", avail, act: chipAct, patch, openProduct, patchG: (id, pt) => patch(id, (b) => ({ g: { ...b.g, ...pt } })), retry, vals, online, toConfirm, write, openSetting, pickSource };
  const onScroll = (e) => {
    const el = e.currentTarget, y = el.scrollTop, atBottom = el.scrollHeight - y - el.clientHeight < 32;
    setScrolled(y > 24);
    if (y > anchorY.current || atBottom) anchorY.current = y;
    else if (anchorY.current - y > 60 && !side && !q && dock === "open") { setDock("mini"); anchorY.current = y; }
  };
  const collapsed = hdr === "closed" || (hdr === "auto" && (feed.length > 1 || kb || scrolled));
  const onFocusSearch = () => { setDock("open"); if (compact) setKb(true); };
  const search = <SearchRow q={q} setQ={setQ} onSubmit={submit} onFocus={onFocusSearch} formRef={formRef} pad={side || low ? 0 : 16} />;
  const feedEl = <Feed2 feed={feed} ctx={ctx} />;

  let body;
  if (low) {
    const L = level(true, true);
    const R = q.trim() ? level(true) : null;
    body = (
      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <aside style={{ flex: "0 0 min(340px, 44%)", minHeight: 0, overflow: "auto", borderRight: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", boxSizing: "border-box", display: "flex", flexDirection: "column" }}>
          <div style={{ position: "sticky", top: 0, zIndex: 2, background: "var(--surface-container-low)" }}>
            <K.DeviceHeader p={p} dev={dev} canF={canF} online={online} collapsed={!lowHdr} onToggle={() => setLowHdr(!lowHdr)} onCan={() => act("can_update")} />
            <LevelHead head={L.head} onBack={goHome} pad="0 8px 0 16px" />
          </div>
          <div key={L.key} className={dir} style={{ display: "flex", flexDirection: "column", gap: 8, padding: "4px 12px 16px" }}>{L.tiles.map((t) => <Row key={t.id} {...t} />)}</div>
        </aside>
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
          <div style={{ flex: 1, minHeight: 0, position: "relative" }}>
            <div ref={feedRef} onScroll={onScroll} style={{ position: "absolute", inset: 0, overflow: "auto" }}>
              <div style={{ maxWidth: 720, margin: "0 auto", padding: "16px 16px 24px", boxSizing: "border-box" }}>{feedEl}</div>
            </div>
            {R && (
              <div className="a2-up" style={{ position: "absolute", inset: "8px 8px 0", overflow: "auto", boxSizing: "border-box", background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", borderBottom: "none", borderRadius: "20px 20px 0 0", boxShadow: "var(--elevation-3)", padding: "4px 12px 12px", display: "flex", flexDirection: "column", gap: 8 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 48 }}>
                  <div style={{ flex: 1, minWidth: 0 }}><CmpText variant="titleMedium">{R.head.title}</CmpText></div>
                  <CmpIconButton icon="close" contentDescription="Закрыть результаты" onClick={() => setQ("")} />
                </div>
                {R.tiles.map((t) => <Row key={t.id} {...t} q={q.trim()} />)}
              </div>
            )}
          </div>
          <div style={{ borderTop: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", padding: "8px 0" }}>
            <div style={{ maxWidth: 720, margin: "0 auto", padding: "0 16px", boxSizing: "border-box" }}>{search}</div>
          </div>
        </div>
      </div>
    );
  } else if (side) {
    const L = level(true, true);
    const R = q.trim() ? level(true) : null;
    body = (
      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <aside style={{ flex: "0 0 400px", minHeight: 0, overflow: "auto", borderRight: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", padding: 24, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 20 }}>
          <K.DeviceHeader p={p} dev={dev} canF={canF} online={online} wide onCan={() => act("can_update")} />
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <LevelHead head={L.head} onBack={goHome} pad="0" />
            <div key={L.key} className={dir} style={{ display: "grid", gridTemplateColumns: "repeat(2, minmax(0,1fr))", gap: 8 }}>{L.tiles.map((t) => <Tile key={t.id} {...t} />)}</div>
          </div>
        </aside>
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
          <div style={{ flex: 1, minHeight: 0, position: "relative" }}>
            <div ref={feedRef} onScroll={onScroll} style={{ position: "absolute", inset: 0, overflow: "auto" }}>
              <div style={{ maxWidth: 760, margin: "0 auto", padding: "24px 24px 40px", boxSizing: "border-box" }}>{feedEl}</div>
            </div>
            {R && (
              <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, maxHeight: "78%", display: "flex", justifyContent: "center", padding: "0 24px", boxSizing: "border-box", pointerEvents: "none" }}>
                <div className="a2-up" style={{ pointerEvents: "auto", width: "100%", maxWidth: 712, maxHeight: "100%", overflow: "auto", boxSizing: "border-box", background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", borderBottom: "none", borderRadius: "24px 24px 0 0", boxShadow: "var(--elevation-3)", padding: "8px 16px 16px", display: "flex", flexDirection: "column", gap: 12 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 48 }}>
                    <div style={{ flex: 1, minWidth: 0 }}><CmpText variant="titleMedium">{R.head.title}</CmpText></div>
                    <CmpIconButton icon="close" contentDescription="Закрыть результаты" onClick={() => setQ("")} />
                  </div>
                  {[...new Set(R.tiles.map((t) => t.grp))].map((g) => (
                    <div key={g} style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                      <CmpText variant="labelLarge" color="var(--on-surface-variant)">{g}</CmpText>
                      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(min(100%, 300px), 1fr))", gap: 6 }}>
                        {R.tiles.filter((t) => t.grp === g).map((t) => <Row key={t.id} {...t} q={q.trim()} />)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
          <div style={{ borderTop: "1px solid var(--outline-variant)", background: "var(--surface-container-low)", padding: "12px 0" }}>
            <div style={{ maxWidth: 760, margin: "0 auto", padding: "0 24px", boxSizing: "border-box" }}>{search}</div>
          </div>
        </div>
      </div>
    );
  } else {
    const L = level(false);
    const colMax = compact ? "none" : 840;
    const quick = QUICK2.filter((k) => !avail(k).hidden);
    body = (
      <>
        <div style={{ maxWidth: colMax, width: "100%", margin: "0 auto" }}>
          <K.DeviceHeader p={p} dev={dev} canF={canF} online={online} collapsed={collapsed} onToggle={() => setHdr(collapsed ? "open" : "closed")} onCan={() => act("can_update")} />
        </div>
        <div ref={feedRef} onScroll={onScroll} style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
          <div style={{ maxWidth: colMax, margin: "0 auto", padding: compact ? "16px 16px 24px" : "24px 24px 32px", boxSizing: "border-box" }}>{feedEl}</div>
        </div>
        <div style={{ width: "100%", maxWidth: compact ? "none" : 888, margin: "0 auto", background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", borderBottom: "none", borderRadius: "24px 24px 0 0", boxShadow: "var(--elevation-2)", padding: "8px 0 12px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 10 }}>
          {dock === "mini" ? (
            <div style={{ display: "flex", alignItems: "center", gap: 8, paddingRight: 12 }}>
              <div className="a2-nosb" style={{ flex: 1, minWidth: 0, display: "flex", gap: 8, overflowX: "auto", paddingLeft: 16 }}>
                {quick.map((k) => { const av = avail(k); return <K.Chip key={k} label={A2[k].label} icon={A2[k].icon} nowrap state={av.ok ? "idle" : "blocked"} reason={av.reason} onClick={() => act(k)} />; })}
              </div>
              <button type="button" className="a2-f" onClick={() => setDock("open")} aria-label="Развернуть пульт"
                style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 48, padding: "0 14px 0 10px", borderRadius: 12, border: "none", background: "var(--secondary-container)", color: "var(--on-secondary-container)", fontFamily: "var(--font-sans)", fontSize: "var(--label-large-size)", fontWeight: 500, cursor: "pointer", flex: "0 0 auto" }}>
                <CmpIcon icon="expand_less" size={22} />Пульт
              </button>
            </div>
          ) : <>
            <LevelHead head={L.head} onBack={goHome} onMini={() => setDock("mini")} />
            <div key={L.key} className={dir}><Carousel tiles={L.tiles} /></div>
          </>}
          {search}
        </div>
        {compact && kb && <K.Keyboard />}
      </>
    );
  }
  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden", fontFamily: "var(--font-sans)" }}>
      <CmpTopAppBar title="Ассистент" navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack || (() => window.psGo && window.psGo("advanced-mount"))} />}
        actions={kb ? <CmpIconButton icon="keyboard_hide" contentDescription="Скрыть клавиатуру" onClick={() => setKb(false)} /> : undefined} />
      {body}
      {away && <K.AwayStub spec={away} onReturn={onReturn} />}
      {lost && <K.LostStub transport={p.transport} onRestore={() => { setLost(false); setPinOk(false); }} />}
      {boot && <BootOverlay boot={boot} />}
      {pinAsk != null && <PinDialog onOk={pinOkFn} onCancel={pinCancel} />}
      {pi && window.ProductInfoApp && <div className="a2-r" style={{ position: "absolute", inset: 0, zIndex: 8 }}><window.ProductInfoApp entry="assistant" number={pi.n} platform={platform} onBack={() => setPi(null)} /></div>}
    </div>
  );
}
function greet2(p, canF, online) {
  if (p.unsupported) return { t: "msg", title: `${p.model}: модели нет в базе`, text: "Доступны прошивка, автомобили и чат поддержки. Остальное появится, когда модель добавят в базу." };
  const parts = [`Подключён по ${p.transport === "ble" ? "Bluetooth" : "USB"}, прошивка 3.56, ${canF ? "CAN заводской" : `CAN ${p.can}`}.`];
  if (p.transport === "usb") parts.push("Состояние датчиков читается только по Bluetooth, всё остальное доступно.");
  if (!online) parts.push("Нет сети: прошивка, Clone и облако недоступны.");
  parts.push("Рекомендуемый шаг — первым в пульте.");
  return { t: "msg", title: `${p.model} готов к работе`, text: parts.join(" ") };
}

// ── Демо ─────────────────────────────────────────────────────────────────────
const DF = "var(--font-sans)";
function useKit() {
  const [ok, setOk] = useState(!!window.AsstKit);
  useEffect(() => { if (ok) return; const t = setInterval(() => { if (window.AsstKit) { setOk(true); clearInterval(t); } }, 80); return () => clearInterval(t); }, [ok]);
  if (ok) K = window.AsstKit;
  return ok;
}
function useCtl2(init) {
  const pr = new URLSearchParams(location.search).get("profile");
  const prof = ({ alarm: "ble", module: "usb", legacy: "unsupported" })[pr] || (["ble", "usb", "unsupported"].includes(pr) ? pr : "ble");
  const [c, setC] = useState({ dark: false, profile: prof, can: "ok", net: "on", err: "off", pin: "off", uns: "off", preset: "first", kbd: "off", pim: "found", ...init });
  return [c, (k) => (v) => setC((s) => ({ ...s, [k]: v }))];
}
function Toolbar2({ c, set, extra, onDrop }) {
  const Seg = K.Seg, row = { display: "flex", gap: 10, flexWrap: "wrap", justifyContent: "center", maxWidth: 1200 };
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10, alignItems: "center" }}>
      <div style={row}>
        <Seg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={c.dark ? "d" : "l"} onChange={(v) => set("dark")(v === "d")} />
        <Seg items={[["first", "Первый вход"], ["long", "Длинная лента"]]} value={c.preset} onChange={set("preset")} />
        <Seg items={[["off", "Клавиатура скрыта"], ["on", "Ввод «бесключевой обход»"]]} value={c.kbd} onChange={set("kbd")} />
        {extra}
      </div>
      <div style={row}>
        <Seg items={[["ble", "Bluetooth"], ["usb", "USB"], ["unsupported", "Нет в базе"]]} value={c.profile} onChange={set("profile")} />
        <Seg items={[["ok", "CAN обновлён"], ["factory", "CAN заводской"]]} value={c.can} onChange={set("can")} />
        <Seg items={[["on", "Сеть есть"], ["off", "Нет сети"]]} value={c.net} onChange={set("net")} />
      </div>
      <div style={row}>
        <Seg items={[["off", "Без ошибок"], ["on", "Ошибка с 1-й попытки"]]} value={c.err} onChange={set("err")} />
        <Seg items={[["off", "Запись без PIN"], ["on", "Запись по PIN"]]} value={c.pin} onChange={set("pin")} />
        <Seg items={[["off", "Редактор чистый"], ["on", "В редакторе 3 правки"]]} value={c.uns} onChange={set("uns")} />
        <Seg items={[["found", "Товар: найден"], ["notfound", "Товар: не найден"], ["offline", "Товар: обрыв сети"]]} value={c.pim} onChange={set("pim")} />
        <button onClick={onDrop} style={{ border: "none", background: "#fff", borderRadius: 999, padding: "10px 16px", fontFamily: DF, fontSize: "var(--t-additional0-size)", fontWeight: 500, cursor: "pointer", boxShadow: "0 1px 3px rgba(0,0,0,.18)", color: "#93000A" }}>Обрыв связи во время загрузки</button>
      </div>
    </div>
  );
}
const GUIDE2 = [
  ["Следующий шаг", "Первая плитка пульта ведёт по маршруту: CAN → прошивка → обход → Clone → состояние."],
  ["Подуровни", "«Прошивка» открывает свои действия. После результата пульт сам открывает тему результата."],
  ["Поиск", "Введите «бесключевой обход»: тема «Обход и Clone», её действия и связанные настройки. Стрелка — все совпадения в ленту."],
  ["Возврат", "«Обучить обход» → итог → «Перечитываем блок» → сравнение «до → после»."],
  ["Загрузчик", "«Проверить прошивку» → «Обновить до 3.57» → «Прошивка обновлена»: экран загрузчика поверх всего, лента на месте."],
  ["PIN", "Включите «Запись по PIN», найдите «темп» → «Изменить» → «Далее» → «Записать»."],
  ["Правки в редакторе", "Включите «В редакторе 3 правки» и измените настройку: карточка предупредит и запишет только её."],
  ["USB", "Переключите на USB: без «Состояния», остальные темы и следующий шаг на месте."],
  ["Информация о товаре", "«Устройства» → «Информация о товаре»: короткая карточка в ленте, «Подробнее» — полный экран. Введите в поиск 107131 — первой строкой «Информация о товаре №107131»."],
  ["Пульт свернуть", "Прокрутите ленту вверх: пульт сворачивается до быстрых действий."],
];
function Guide2() {
  return (
    <div style={{ width: 320, display: "flex", flexDirection: "column", gap: 10, fontFamily: DF, color: "#262626" }}>
      <div style={{ fontSize: "var(--t-paragraph2-size)", fontWeight: 700 }}>Концепция «Пульт»</div>
      <div style={{ fontSize: "var(--t-additional0-size)", lineHeight: "19px", color: "#444" }}>Лента — журнал: что сделано и что ответил блок. Пульт под большим пальцем — что делать дальше. Кнопок в ленте нет, кроме действий над самой карточкой: правка, запись, повтор.</div>
      {GUIDE2.map(([t, d], i) => (
        <div key={t} style={{ display: "grid", gridTemplateColumns: "28px 1fr", gap: 8, background: "rgba(255,255,255,.7)", borderRadius: 12, padding: "10px 12px" }}>
          <span style={{ width: 24, height: 24, borderRadius: 999, background: "#1A3E7A", color: "#fff", fontSize: "var(--t-additional1-size)", fontWeight: 700, display: "grid", placeItems: "center" }}>{i + 1}</span>
          <div><div style={{ fontSize: "var(--t-additional0-size)", fontWeight: 600 }}>{t}</div><div style={{ fontSize: "var(--t-additional0-size)", lineHeight: "18px", color: "#555" }}>{d}</div></div>
        </div>
      ))}
    </div>
  );
}
const props2 = (c) => ({ profile: c.profile, canFactory: c.can === "factory", online: c.net === "on", errMode: c.err === "on", pinRequired: c.pin === "on", unsaved: c.uns === "on" ? 3 : 0, preset: c.preset, keyboardDemo: c.kbd === "on", productMode: c.pim });
const key2 = (c, x = "") => [c.profile, c.can, c.net, c.preset, c.kbd, x].join("|");

function PhoneDemo2() {
  const [c, set] = useCtl2();
  const api = useRef(null);
  return (
    <div style={{ minHeight: "100vh", background: "#c9ccce", padding: "20px 16px 32px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 20 }}>
      <Toolbar2 c={c} set={set} onDrop={() => api.current && api.current.drop()} />
      <div style={{ display: "flex", gap: 32, flexWrap: "wrap", justifyContent: "center", alignItems: "flex-start" }}>
        <K.Phone dark={c.dark}><AssistantScreen key={key2(c)} {...props2(c)} apiRef={api} /></K.Phone>
        <Guide2 />
      </div>
    </div>
  );
}
function WideDemo2() {
  const [c, set] = useCtl2({ win: "tl" });
  const api = useRef(null);
  const W = K.WINDOWS, win = W[c.win];
  return (
    <div style={{ minHeight: "100vh", background: "#c9ccce", padding: "20px 24px 32px", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", gap: 20 }}>
      <Toolbar2 c={c} set={set} onDrop={() => api.current && api.current.drop()} extra={<K.Seg items={Object.entries(W).map(([k, v]) => [k, v.l])} value={c.win} onChange={set("win")} />} />
      <div style={{ fontFamily: DF, fontSize: "var(--t-additional0-size)", color: "#444", maxWidth: 900, textAlign: "center" }}>С 840 dp пульт стоит слева постоянно: шапка блока, следующий шаг и темы; найденное при вводе появляется справа, над строкой поиска. На Medium (800 dp) пульт внизу, лента по центру.</div>
      <K.WindowFrame win={win} dark={c.dark}><AssistantScreen key={key2(c, c.win)} {...props2(c)} keyboardDemo={false} platform={win.kind} apiRef={api} /></K.WindowFrame>
    </div>
  );
}
function AssistantDemo() { return useKit() ? <PhoneDemo2 /> : null; }
function AssistantWideDemo() { return useKit() ? <WideDemo2 /> : null; }
Object.assign(window, { AssistantDemo, AssistantWideDemo, AssistantScreen });
