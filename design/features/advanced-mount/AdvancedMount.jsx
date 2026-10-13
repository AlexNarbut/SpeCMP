
// AdvancedMountScreen — «Расширенный монтаж»: шапка с данными блока + сетка доступных разделов.
// Состав разделов зависит от типа блока (profile) и его возможностей; плитки, требующие
// сначала обновить CAN-прошивку, визуально помечены (в старой версии не отличались).
// Состояния (state): loading | error | denied | unsupported | ready | empty | lost.
// Адаптив (M3 Window Size Classes, по ширине контейнера):
//   compact <600 — шапка сверху + один столбец плиток;
//   medium 600–839 — шапка сверху (в 2 колонки) + сетка 2 колонки;
//   expanded ≥840 — слева постоянная панель «Устройство», справа сетка разделов.

const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpSurface, CmpText, CmpIcon, CmpDivider, CmpCircularProgressIndicator } = DS;

// ── Каталог разделов (порядок = порядок в сетке) ─────────────────────────────
const SECTIONS = [
  { key: "firmware", svg: "sections/01_proverka_proshivok.svg", icon: "",    title: "Проверка прошивок",     sub: "Обновите прошивки системы до актуальных версий" },
  { key: "settings", svg: "sections/02_rasshirennye_nastroyki.svg", icon: "",             title: "Расширенные настройки", sub: "Выполните полную настройку системы", needsCan: true },
  { key: "bypass",   svg: "sections/03_beskluchevoy_obhod.svg", icon: "",              title: "Бесключевой обход",     sub: "Обучение бесключевому обходу штатного иммобилайзера", needsCan: true },
  { key: "clone",    svg: "sections/04_procedura_clone.svg", icon: "",     title: "Процедура Clone",       sub: "Вычисление ключа средствами нашего сервера", needsCan: true },
  { key: "extra",    svg: "sections/05_dop_ustroystva.svg", icon: "",    title: "Доп. устройства",       sub: "Приживляйте и удаляйте устройства Pandora", needsCan: true },
  { key: "tickets",  svg: "sections/06_zayavki_na_tehpomosch.svg", icon: "",       title: "Заявки на техпомощь",   sub: "Просмотрите историю обращений или оформите заявку" },
  { key: "chat",     svg: "sections/07_chat_tehpodderzhki.svg", icon: "",    title: "Чат техподдержки",      sub: "Напишите о вашей проблеме при монтаже нашим специалистам" },
  { key: "voice",    svg: "sections/08_golosovaya_proshivka.svg", icon: "",title: "Голосовая прошивка",    sub: "Обновите голосовую прошивку" },
  { key: "maps",     svg: "sections/09_zayavki_narodnyh_kart.svg", icon: "",           title: "Заявки народных карт",  sub: "Делитесь своим опытом монтажа" },
  { key: "cars",     svg: "sections/10_avtomobili.svg", icon: "",   title: "Автомобили",            sub: "Изучение поддерживаемых функций автомобиля" },
];

// ── Профили блоков: что у блока есть → какие разделы показать ───────────────
const PROFILES = {
  alarm: {
    label: "Сигнализация (CAN)", name: "Pandora DX-4GS", id: "107131", transport: "ble", address: "AC:23:3F:A1:0C:7D",
    fw: "3.57", can: { fw: "12.05", code: "VWPOLO20", factory: true }, imei: "351756051523999", product: true,
    sections: ["firmware", "settings", "bypass", "clone", "extra", "tickets", "chat", "voice", "maps", "cars"],
  },
  module: {
    label: "Модуль (USB)", name: "Pandora RMD-5", id: "107131313", transport: "usb", address: "HID · 0483:5750",
    fw: "3.57", can: null, imei: null, product: false,
    sections: ["firmware", "settings", "extra", "tickets", "chat", "cars"],
  },
  legacy: {
    label: "Не поддерживается", name: "Pandora RMD-4", id: "107131320", transport: "usb", address: "HID · 0483:5751",
    fw: "3.42", can: null, imei: null, product: false, unsupported: true,
    sections: ["firmware", "chat", "cars"],
  },
};

const MODE_LABEL = { work: "Рабочий", bootloader: "Загрузчик" };

(function injectStyles() {
  if (document.getElementById("am-anim")) return;
  const s = document.createElement("style");
  s.id = "am-anim";
  s.textContent =
    ".am-svg svg *[stroke]:not([stroke=\"none\"]){stroke:currentColor}.am-svg svg *[fill]:not([fill=\"none\"]){fill:currentColor}" +
    "@keyframes amPulse{0%,100%{opacity:1}50%{opacity:.45}}" +
    "@keyframes amIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}" +
    "@media (prefers-reduced-motion: reduce){.am-pulse,.am-in{animation:none!important}}";
  document.head.appendChild(s);
})();

// SVG из legacy-набора: грузим один раз, чистим c2pa-метаданные, вставляем inline и красим через currentColor.
// Единый набор иконок разделов: сплошная заливка, сетка 24, одинаковая плотность. Пути — по ключу раздела.
const SECTION_PATHS = {
 "firmware": "M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM16 13h-3v4h-2v-4H8l4-5 4 5z",
 "settings": "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z",
 "bypass": "M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z",
 "clone": "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2z",
 "extra": "M15 9H9c-.55 0-1 .45-1 1v12c0 .55.45 1 1 1h6c.55 0 1-.45 1-1V10c0-.55-.45-1-1-1zm-3 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zM7.05 6.05l1.41 1.41C9.37 6.56 10.62 6 12 6s2.63.56 3.54 1.46l1.41-1.41C15.68 4.78 13.93 4 12 4s-3.68.78-4.95 2.05zM12 0C8.96 0 6.21 1.23 4.22 3.22l1.41 1.41C7.26 3.01 9.51 2 12 2s4.74 1.01 6.36 2.64l1.41-1.41C17.79 1.23 15.04 0 12 0z",
 "tickets": "M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z",
 "chat": "M21 12.22C21 6.73 16.74 3 12 3c-4.69 0-9 3.65-9 9.28-.6.34-1 .98-1 1.72v2c0 1.1.9 2 2 2h1v-6.1c0-3.87 3.13-7 7-7s7 3.13 7 7V19h-8v2h8c1.1 0 2-.9 2-2v-1.22c.59-.31 1-.92 1-1.64v-2.3c0-.7-.41-1.31-1-1.62zM9 13c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1zm6 0c-.55 0-1 .45-1 1s.45 1 1 1 1-.45 1-1-.45-1-1-1z",
 "voice": "M9 13c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4zM9 5a4 4 0 1 0 0 8 4 4 0 0 0 0-8zm7.76-1.64l-1.68 1.69c.84 1.18.84 2.71 0 3.89l1.68 1.69c2.02-2.02 2.02-5.07 0-7.27zM20.07 0l-1.63 1.63c2.77 3.02 2.77 7.56 0 10.74L20.07 14c3.9-3.89 3.91-9.95 0-14z",
 "maps": "M12 12.75c1.63 0 3.07.39 4.24.9 1.08.48 1.76 1.56 1.76 2.73V18H6v-1.61c0-1.18.68-2.26 1.76-2.73 1.17-.52 2.61-.91 4.24-.91zM4 13c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm1.13 1.1c-.37-.06-.74-.1-1.13-.1-.99 0-1.93.21-2.78.58C.48 14.9 0 15.62 0 16.43V18h4.5v-1.61c0-.83.23-1.61.63-2.29zM20 13c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm4 3.43c0-.81-.48-1.53-1.22-1.85-.85-.37-1.79-.58-2.78-.58-.39 0-.76.04-1.13.1.4.68.63 1.46.63 2.29V18H24v-1.57zM12 6c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3z",
 "cars": "M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"
};
// Стиль как на главной: часть иконок — те же main_* (duotone), остальные дорисованы в той же манере
// (базовый слой opacity .3 + деталь полным тоном).
const MAIN_MAP = { firmware: "firmware_archive", settings: "settings_configurator", tickets: "orders", cars: "vehicle_support", maps: "articles" };
const DUO = {
  bypass: <><path opacity=".3" d={SECTION_PATHS.bypass} /><path d="M7 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zm0 2a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM17 14h4v4h-4zM12 10h11v2H12z" /></>,
  clone: <><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1z" /><path opacity=".3" d="M19 5H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2z" /><path d="M10 10h7v2h-7zM10 14h7v2h-7z" /></>,
  extra: <><path opacity=".3" d="M15 9H9c-.55 0-1 .45-1 1v12c0 .55.45 1 1 1h6c.55 0 1-.45 1-1V10c0-.55-.45-1-1-1z" /><circle cx="12" cy="13" r="2" /><path d="M7.05 6.05l1.41 1.41C9.37 6.56 10.62 6 12 6s2.63.56 3.54 1.46l1.41-1.41C15.68 4.78 13.93 4 12 4s-3.68.78-4.95 2.05zM12 0C8.96 0 6.21 1.23 4.22 3.22l1.41 1.41C7.26 3.01 9.51 2 12 2s4.74 1.01 6.36 2.64l1.41-1.41C17.79 1.23 15.04 0 12 0z" /></>,
  chat: <><path opacity=".3" d="M12 6a6 6 0 0 0-6 6v4h12v-4a6 6 0 0 0-6-6z" /><path d={SECTION_PATHS.chat} /></>,
  voice: <><path opacity=".3" d="M9 13c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4zM9 5a4 4 0 1 0 0 8 4 4 0 0 0 0-8z" /><path d="M16.76 3.36l-1.68 1.69c.84 1.18.84 2.71 0 3.89l1.68 1.69c2.02-2.02 2.02-5.07 0-7.27zM20.07 0l-1.63 1.63c2.77 3.02 2.77 7.56 0 10.74L20.07 14c3.9-3.89 3.91-9.95 0-14z" /></>,
};
function SectionIcon({ k, size = 32 }) {
  if (MAIN_MAP[k] && window.PS_MAIN_ICONS && window.PS_MAIN_ICONS[MAIN_MAP[k]])
    return <span aria-hidden="true" style={{ width: size, height: size, display: "inline-flex", flex: "0 0 auto" }} dangerouslySetInnerHTML={{ __html: window.PS_MAIN_ICONS[MAIN_MAP[k]] }} />;
  return <svg aria-hidden="true" viewBox="0 0 24 24" width={size} height={size} fill="currentColor" style={{ flex: "0 0 auto" }}>{DUO[k]}</svg>;
}
const SVG_CACHE = {};
function loadSvg(src) {
  if (!SVG_CACHE[src]) SVG_CACHE[src] = fetch("icons/" + src).then((r) => r.text()).then((t) =>
    t.replace(/<\?xml[^>]*\?>/, "").replace(/<metadata[\s\S]*?<\/metadata>/, "")
     .replace(/<svg([^>]*)>/, (m, at) => "<svg" + at.replace(/\s(width|height|x|y)="[^"]*"/g, "").replace(/style="[^"]*"/, "") + ' width="100%" height="100%" fill="currentColor">'));
  return SVG_CACHE[src];
}
function SvgIcon({ src, size = 28, w, h }) {
  const [html, setHtml] = React.useState("");
  React.useEffect(() => { let on = true; loadSvg(src).then((t) => on && setHtml(t)); return () => { on = false; }; }, [src]);
  return <span aria-hidden="true" className="am-svg" style={{ display: "inline-flex", flex: "0 0 auto", width: w || size, height: h || size }} dangerouslySetInnerHTML={{ __html: html }} />;
}

function useWidth() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current; if (!el) return;
    const m = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(m); ro.observe(el); m();
    return () => ro.disconnect();
  }, []);
  return [ref, w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact"];
}

function Chip({ icon, text, tone = "secondary" }) {
  const bg = tone === "warn" ? "var(--tertiary-container)" : tone === "ok" ? "var(--primary-container)" : "var(--secondary-container)";
  const fg = tone === "warn" ? "var(--on-tertiary-container)" : tone === "ok" ? "var(--on-primary-container)" : "var(--on-secondary-container)";
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 28, padding: "0 12px", borderRadius: 999, background: bg, color: fg, fontFamily: "var(--font-sans)", fontSize: 13, fontWeight: 500, whiteSpace: "nowrap" }}>
      {icon && <CmpIcon icon={icon} size={16} />}{text}
    </span>
  );
}

function Row({ label, value, mono = true, accent = true, children }) {
  return (
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12, padding: "10px 0", minHeight: 24 }}>
      <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{label}</CmpText>
      <span style={{ display: "inline-flex", alignItems: "center", gap: 8, fontFamily: mono ? "var(--font-mono)" : "var(--font-sans)", fontSize: 15, fontWeight: 600, color: accent ? "var(--primary)" : "var(--on-surface)", textAlign: "right" }}>{value}{children}</span>
    </div>
  );
}

// ── Шапка с данными блока (видна всегда) ─────────────────────────────────────
function DeviceHeader({ p, mode = "work", layout, onInfo, onCanHint, hideRows = false, unsupportedTone }) {
  const t = p.transport === "ble" ? { icon: "bluetooth_connected", text: "Bluetooth" } : { icon: "usb", text: "USB" };
  const wide = layout === "expanded";
  return (
    <CmpSurface color="var(--surface-container-low)" radius={20} border style={{ boxSizing: "border-box", width: "100%" }}>
      <div style={{ padding: wide ? 24 : 16, display: "flex", flexDirection: "column", gap: 12 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: wide ? 72 : 56, height: wide ? 72 : 56, borderRadius: 16, flex: "0 0 auto", display: "grid", placeItems: "center", border: "1px solid var(--outline-variant)", background: "repeating-linear-gradient(135deg, var(--surface-variant) 0 6px, color-mix(in srgb, var(--surface-variant) 55%, var(--surface)) 6px 12px)" }}>
            <CmpIcon icon={p.transport === "ble" ? "sensors" : "memory"} size={wide ? 32 : 26} color="var(--on-surface-variant)" />
          </div>
          <div style={{ minWidth: 0, flex: 1 }}>
            <CmpText variant={wide ? "headlineSmall" : "titleLarge"} color="var(--primary)" maxLines={1}>{p.name}</CmpText>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: 14, color: "var(--on-surface-variant)" }}>ID {p.id}</span>
          </div>
        </div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
          <Chip icon={t.icon} text={t.text} />
          <Chip icon={mode === "bootloader" ? "system_update" : "check_circle"} text={`Режим: ${MODE_LABEL[mode]}`} tone={mode === "bootloader" ? "warn" : "ok"} />
        </div>
        {!hideRows && (
          <div style={{ display: "flex", flexDirection: "column" }}>
            <CmpDivider />
            <Row label="Версия ПО" value={p.fw} />
            {p.can && (<>
              <CmpDivider />
              <Row label="Версия ПО CAN" value={p.can.fw} />
              <CmpDivider />
              <Row label="CAN код" value={p.can.code}>
                <span onClick={onCanHint} role="button" aria-label="Подсказка про CAN" style={{ width: 32, height: 32, display: "grid", placeItems: "center", cursor: "pointer", margin: "-6px -4px -6px 0", borderRadius: 999 }}>
                  <span className="am-pulse" style={{ display: "grid", animation: "amPulse 1.6s ease-in-out infinite" }}><CmpIcon icon="warning" size={22} color="var(--error)" /></span>
                </span>
              </Row>
            </>)}
            {p.imei && (<><CmpDivider /><Row label="IMEI GSM модема" value={p.imei} /></>)}
          </div>
        )}
        {p.product && !hideRows && (
          <CmpOutlinedButton text="Информация о товаре" icon="inventory_2" fullWidth onClick={onInfo} />
        )}
      </div>
    </CmpSurface>
  );
}

// ── Плитка раздела ───────────────────────────────────────────────────────────
function Tile({ s, locked, unread, onClick, big }) {
  const { opacity, handlers } = DS.useStateLayer ? DS.useStateLayer() : { opacity: 0, handlers: {} };
  const title = s.key === "chat" && unread ? "Чат тех. поддержки" : s.title;
  const sub = s.key === "chat" && unread ? "Напишите в чат нашему специалисту о вашей проблеме" : s.sub;
  const iconBg = locked ? "var(--tertiary-container)" : "var(--primary-container)";
  const iconFg = locked ? "var(--on-tertiary-container)" : "var(--on-primary-container)";
  return (
    <CmpSurface onClick={onClick} color="var(--surface-container-low)" radius={18} border style={{ boxSizing: "border-box", position: "relative", overflow: "hidden", cursor: "pointer", height: "100%" }}>
      <div {...handlers} role="button" tabIndex={0}
        onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); onClick && onClick(); } }}
        style={{ display: "flex", alignItems: "center", gap: 16, padding: "14px 14px 14px 16px", minHeight: big ? 96 : 84, boxSizing: "border-box", height: "100%" }}>
        <div style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity, pointerEvents: "none" }} />
        <div style={{ position: "relative", width: 52, height: 52, borderRadius: 14, background: iconBg, color: iconFg, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
          <SectionIcon k={s.key} size={32} />
          {locked && (
            <span style={{ position: "absolute", right: -7, bottom: -7, width: 26, height: 26, borderRadius: 999, boxSizing: "border-box", background: "var(--tertiary)", color: "var(--on-tertiary)", display: "grid", placeItems: "center", border: "2px solid var(--surface-container-low)" }}>
              <svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor" aria-hidden="true"><path d="M18 8h-1V6A5 5 0 0 0 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6a3 3 0 0 1 6 0v2H9V6zm3 11.5a1.75 1.75 0 1 1 0-3.5 1.75 1.75 0 0 1 0 3.5z" /></svg>
            </span>
          )}
          {s.key === "chat" && unread > 0 && (
            <span style={{ position: "absolute", top: -6, left: -6, minWidth: 24, height: 24, padding: "0 6px", boxSizing: "border-box", borderRadius: 999, background: "var(--error)", color: "var(--on-error)", fontSize: 13, fontWeight: 700, display: "grid", placeItems: "center", border: "2px solid var(--surface-container-low)" }}>{unread}</span>
          )}
        </div>
        <div style={{ flex: 1, minWidth: 0, position: "relative" }}>
          <CmpText variant="titleMedium" maxLines={1}>{title}</CmpText>
          {locked
            ? <div style={{ marginTop: 2 }}><CmpText variant="bodyMedium" color="var(--on-tertiary-container)" maxLines={2}><b>Сначала обновите CAN-прошивку</b></CmpText></div>
            : <div style={{ marginTop: 2 }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)" maxLines={2}>{sub}</CmpText></div>}
        </div>
        <CmpIcon icon="chevron_right" size={24} color="var(--on-surface-variant)" />
      </div>
    </CmpSurface>
  );
}

function Grid({ items, layout, canLocked, unread, onOpen }) {
  const cols = layout === "compact" ? "1fr" : "repeat(2, minmax(0, 1fr))";
  return (
    <div style={{ display: "grid", gridTemplateColumns: cols, gap: 12, alignContent: "start" }}>
      {items.map((s, i) => (
        <div key={s.key} className="am-in" style={{ animation: "amIn .26s ease both", animationDelay: `${i * 35}ms` }}>
          <Tile s={s} big={layout !== "compact"} locked={canLocked && s.needsCan} unread={unread} onClick={() => onOpen(s)} />
        </div>
      ))}
    </div>
  );
}

function Hero({ icon, tone = "primary" }) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "neutral" ? "var(--surface-variant)" : "var(--primary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "neutral" ? "var(--on-surface-variant)" : "var(--on-primary-container)";
  return <div style={{ width: 88, height: 88, borderRadius: "50%", background: bg, display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={icon} size={42} color={fg} /></div>;
}

function Center({ children }) {
  return (
    <div style={{ flex: 1, minHeight: 240, display: "flex", alignItems: "center", justifyContent: "center", padding: "24px 16px", boxSizing: "border-box" }}>
      <div style={{ maxWidth: 440, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 16, textAlign: "center" }}>{children}</div>
    </div>
  );
}

// ── Содержимое области сетки по состоянию ────────────────────────────────────
function GridArea({ p, state, layout, items, canLocked, unread, onOpen, setState }) {
  if (state === "loading") return (
    <Center>
      <CmpCircularProgressIndicator />
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Читаем возможности блока и проверяем его на сервере…</CmpText>
    </Center>
  );
  if (state === "error") return (
    <Center>
      <Hero icon="cloud_off" tone="neutral" />
      <CmpText variant="titleLarge">Не удалось проверить блок на сервере</CmpText>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Проверьте интернет-соединение и повторите попытку.</CmpText>
      <CmpButton text="Исправить" icon="refresh" fullWidth onClick={() => setState("loading")} />
    </Center>
  );
  if (state === "denied") return (
    <Center>
      <Hero icon="block" tone="error" />
      <CmpText variant="titleLarge">Доступ ограничен данному устройству</CmpText>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Причина от сервера: блок заблокирован для данной учётной записи монтажника.</CmpText>
      <CmpButton text="Повторить" icon="refresh" fullWidth onClick={() => setState("loading")} />
    </Center>
  );
  if (state === "empty") return (
    <Center>
      <Hero icon="grid_off" tone="neutral" />
      <CmpText variant="titleLarge">Для этого блока нет доступных разделов</CmpText>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок не сообщил поддерживаемых возможностей. Переподключите устройство.</CmpText>
      <CmpOutlinedButton text="Перечитать возможности" icon="refresh" fullWidth onClick={() => setState("loading")} />
    </Center>
  );
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {state === "unsupported" && (
        <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 16px", borderRadius: 16, background: "var(--error-container)", color: "var(--on-error-container)" }}>
          <CmpIcon icon="warning" size={28} />
          <div><CmpText variant="titleMedium">Устройство не поддерживается</CmpText>
            <CmpText variant="bodyMedium">Доступны только базовые разделы.</CmpText></div>
        </div>
      )}
      <Grid items={items} layout={layout} canLocked={canLocked} unread={unread} onOpen={onOpen} />
    </div>
  );
}

// ── Потеря связи (поверх любого экрана монтажа), текст зависит от канала ─────
function LostScreen({ transport, onBack }) {
  const usb = transport === "usb";
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 5, background: "var(--background)", display: "flex", flexDirection: "column" }}>
      <CmpTopAppBar title="" navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} />
      <Center>
        <div style={{ color: "var(--primary)" }}><SvgIcon src="other/svyaz_poteryana.svg" w={176} h={121} /></div>
        <CmpText variant="headlineSmall">Отсутствует соединение с устройством</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">
          {usb ? "Проверьте USB соединение с устройством." : "Блок выключен или находится далеко. Поднесите телефон ближе и проверьте питание блока."}
        </CmpText>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <CmpCircularProgressIndicator size={18} strokeWidth={2.5} />
          <CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём возвращения блока…</CmpText>
        </div>
        <CmpOutlinedButton text="Отключиться" icon="link_off" fullWidth onClick={onBack} />
      </Center>
    </div>
  );
}

function SectionStub({ s, p, onBack }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 4, background: "var(--background)", display: "flex", flexDirection: "column" }}>
      <CmpTopAppBar title={s.title} subtitle={`${p.name}  ${p.id}`} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} />
      <Center><div style={{ width: 88, height: 88, borderRadius: "50%", background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}><SectionIcon k={s.key} size={44} /></div><CmpText variant="titleMedium" color="var(--on-surface-variant)">Экран раздела — отдельная фича</CmpText></Center>
    </div>
  );
}

// ── Главный экран ────────────────────────────────────────────────────────────
function AdvancedMountScreen({ profile = "alarm", initialState = "ready", mode = "work", canFactory, unread = 0, remote = true, lostTransport, showCanDialog = false, onBack = () => {} }) {
  const { useState, useEffect } = React;
  const p = { ...PROFILES[profile] };
  if (p.can && canFactory !== undefined) p.can = { ...p.can, factory: canFactory };
  const [rootRef, layout] = useWidth();
  const [state, setState] = useState(initialState);
  const [dialog, setDialog] = useState(showCanDialog ? "can" : null);
  const [section, setSection] = useState(null);

  useEffect(() => {
    if (state !== "loading") return;
    const t = setTimeout(() => setState(p.unsupported ? "unsupported" : "ready"), 1600);
    return () => clearTimeout(t);
  }, [state]);

  const items = SECTIONS.filter((s) => p.sections.includes(s.key));
  const canLocked = !!(p.can && p.can.factory) && state === "ready";
  const open = (s) => (canLocked && s.needsCan) ? setDialog("can") : setSection(s);
  const effItems = state === "empty" ? [] : items;

  const actions = remote ? (
    <div role="button" aria-label="Удалённая синхронизация" tabIndex={0} style={{ width: 48, height: 48, display: "grid", placeItems: "center", borderRadius: 999, cursor: "pointer", color: "var(--primary)" }}><span className="am-pulse" style={{ display: "grid", animation: "amPulse 1.8s ease-in-out infinite" }}><SvgIcon src="other/udalennaya_sinhronizaciya_topbar.svg" size={30} /></span></div>
  ) : undefined;

  const header = <DeviceHeader p={p} mode={mode} layout={layout} onCanHint={() => setDialog("hint")} onInfo={() => {}} />;
  const area = <GridArea p={p} state={state} layout={layout} items={effItems} canLocked={canLocked} unread={unread} onOpen={open} setState={setState} />;
  const pad = layout === "compact" ? 14 : 24;

  let body;
  if (layout === "expanded") {
    body = (
      <div style={{ flex: 1, minHeight: 0, display: "flex", gap: 24, padding: 24, boxSizing: "border-box", maxWidth: 1180, width: "100%", margin: "0 auto" }}>
        <div style={{ flex: "0 0 340px", minHeight: 0, overflow: "auto" }}>{header}</div>
        <div style={{ flex: 1, minWidth: 0, minHeight: 0, overflow: "auto", display: "flex", flexDirection: "column" }}>{area}</div>
      </div>
    );
  } else {
    body = (
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ maxWidth: 760, margin: "0 auto", padding: pad, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 14 }}>
          {header}
          {area}
        </div>
      </div>
    );
  }

  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      <CmpTopAppBar title="Расширенный монтаж" navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} />} actions={actions} />
      {body}
      {section && <SectionStub s={section} p={p} onBack={() => setSection(null)} />}
      {dialog === "can" && (
        <DS.CmpDialog open onDismissRequest={() => setDialog(null)} icon={<span style={{ color: "var(--error)", display: "inline-flex" }}><SvgIcon src="other/dialog_preduprezhdenie.svg" size={36} /></span>} title="Внимание"
          text="Устройство с заводскими CAN настройками. Нужно обновить прошивку под необходимый CAN код автомобиля"
          dismissButton={<CmpTextButtonSafe text="Отмена" onClick={() => setDialog(null)} />}
          confirmButton={<CmpTextButtonSafe text="Перейти к обновлению" onClick={() => setDialog(null)} />} />
      )}
      {dialog === "hint" && (
        <DS.CmpDialog open onDismissRequest={() => setDialog(null)} icon={<span style={{ color: "var(--error)", display: "inline-flex" }}><SvgIcon src="other/dialog_preduprezhdenie.svg" size={36} /></span>} title="Прошивка CAN"
          text="Функционал данного устройства зависит от прошивки CAN. Теперь в первую очередь при настройке устройства необходимо определиться с CAN кодом автомобиля, обновить прошивку устройства под этот CAN код, а потом донастраивать систему и проверять ее функции"
          confirmButton={<CmpTextButtonSafe text="Понятно" onClick={() => setDialog(null)} />} />
      )}
      {lostTransport && <LostScreen transport={lostTransport} onBack={onBack} />}
    </div>
  );
}

function CmpTextButtonSafe(props) {
  const Btn = DS.CmpTextButton;
  return Btn ? <Btn {...props} /> : <CmpOutlinedButton {...props} />;
}

Object.assign(window, { AdvancedMountScreen, AM_PROFILES: PROFILES });
