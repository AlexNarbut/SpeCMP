// BypassCloneApp — «Бесключевой обход» и «Процедура Clone» (раздел «Расширенный монтаж»).
// Оба экрана: старт → процесс → итог. Обход может закончиться переходом в Clone; «Назад» из Clone — обратно в обход.
// Экран шага обхода — карточка-команда под иллюстрацией. Истории шагов нет.
// Адаптив по ширине контейнера: compact <600 (одна колонка, кнопки внизу) · medium 600–839 (колонка до 680) ·
// expanded ≥840 (основная колонка + боковая 360, max-width 1040).
// Иллюстрации — плейсхолдеры с ключом файла; реальные SVG подключаются через BC_ILLUS (ключ → путь).

const BC = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpTextButton, CmpText, CmpIcon, CmpCircularProgressIndicator, CmpLinearProgressIndicator, CmpDialog } = BC;

const BC_DEV = { name: "Pandora DX-4GS", id: "107131", can: "VWPOLO20" };
const BC_SUB = `${BC_DEV.name} · ${BC_DEV.id}`;
const H52 = { height: 52 };
// Один размер иллюстрации на всех состояниях (высота; ширина ×1.45) — чтобы картинка не прыгала между шагами.
const illSize = (layout) => layout === "compact" ? 160 : 210;
const H48 = { height: 48 };

// Ключ иллюстрации (`id` или `id_mode`) → SVG из illus/ (линейная графика 800×550, красится currentColor). Нет файла → плейсхолдер.
const I = (f) => "illus/" + f + ".svg";
const BC_ILLUS = Object.assign({
  bypass_start: I("bypass_learning_turn_off_ignition_request"), prep: I("bypass_learning_turn_off_ignition_request"),
  learn: I("bypass_learning_process"),
  ign_on_req: I("bypass_learning_turn_on_ignition_request"), ign_on_done: I("bypass_learning_turn_on_ignition_success"),
  ign_off_req: I("bypass_learning_turn_off_ignition_request"), ign_off_done: I("bypass_learning_turn_off_ignition_success"),
  engine_stop_req: I("bypass_engine_stop_request"), engine_stop_done: I("bypass_engine_stop_success"),
  key_out: I("bypass_key_out"), toyota: I("bypass_toyota_v2"),
  engine_start_req: I("bypass_learning_turn_on_engine_request"), engine_start_done: I("bypass_learning_turn_on_engine_success"),
  hood_open_req: I("bypass_need_open_hood"), hood_open_done: I("bypass_open_hood_successfully"),
  hood_close_req: I("bypass_need_close_hood"), hood_close_done: I("bypass_close_hood_successfully"),
  ecu: I("bypass_engine_exchange_v2"), alarm_ex: I("bypass_engine_exchange_v2"), lada: I("bypass_learning_lada"),
  res_ok: I("bypass_result_success"), res_fail: I("bypass_result_fail"),
  clone_start: I("alarm_clone"), clone_run: I("alarm_cloning"),
}, window.BC_ILLUS || {});
const ILL = {
  prep: { icon: "hourglass_top", label: "ожидание" },
  ign_on: { icon: "vpn_key", label: "зажигание вкл" },
  ign_off: { icon: "vpn_key_off", label: "зажигание выкл" },
  engine_start: { icon: "play_circle", label: "запуск двигателя" },
  engine_stop: { icon: "stop_circle", label: "остановка двигателя" },
  key_out: { icon: "logout", label: "вынуть ключ" },
  hood_open: { icon: "garage", label: "открыть капот" },
  hood_close: { icon: "garage", label: "закрыть капот" },
  ecu: { icon: "swap_horiz", label: "обмен с ЭБУ" },
  lada: { icon: "format_list_numbered", label: "LADA APS" },
  bypass_start: { icon: "vpn_key", label: "обход · старт" },
  clone_start: { icon: "content_copy", label: "clone · старт" },
  clone_run: { icon: "cloud_sync", label: "clone · процесс" },
  learn: { icon: "hourglass_top", label: "обучение" }, alarm_ex: { icon: "swap_horiz", label: "обмен с блоком" },
};

(function () {
  if (document.getElementById("bc-anim")) return;
  const s = document.createElement("style"); s.id = "bc-anim";
  s.textContent = "@keyframes bcRing{0%{transform:scale(.96);opacity:.55}100%{transform:scale(1.18);opacity:0}}" +
    "@keyframes bcBreath{0%,100%{opacity:1}50%{opacity:.55}}@keyframes bcIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}" +
    ".bc-svg svg{width:100%;height:100%}@media (prefers-reduced-motion: reduce){.bc-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Утилиты ─────────────────────────────────────────────────────────────────
function useBcWidth() {
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
function useElapsed(key, from = 0) {
  const [s, setS] = React.useState(from);
  React.useEffect(() => { setS(from); const t = setInterval(() => setS((x) => x + 1), 1000); return () => clearInterval(t); }, [key]);
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;
}
const goChat = () => window.psGo && window.psGo("support-chat");
// Заявка на техпомощь — в приоритете перед чатом: текст ошибки сразу попадает в описание заявки.
const goTicket = (from, text) => window.psTicket && window.psTicket(from, text);
function HelpBtns({ from, text, primary }) {
  return <React.Fragment>
    {primary
      ? <CmpButton text="Заявка на техпомощь" icon="phone_callback" fullWidth style={H52} onClick={() => goTicket(from, text)} />
      : <CmpOutlinedButton text="Заявка на техпомощь" icon="phone_callback" fullWidth style={H52} onClick={() => goTicket(from, text)} />}
    <CmpTextButton text="Написать в чат поддержки" icon="support_agent" style={{ height: 48, alignSelf: "center" }} onClick={goChat} />
  </React.Fragment>;
}

function Banner({ icon = "warning", tone = "warn", title, children, action }) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "info" ? "var(--secondary-container)" : "var(--tertiary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "info" ? "var(--on-secondary-container)" : "var(--on-tertiary-container)";
  return (
    <div role={tone === "error" ? "alert" : undefined} style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "14px 16px", borderRadius: "var(--r-card-medium-radius)", background: bg, color: fg, boxSizing: "border-box", width: "100%" }}>
      <CmpIcon icon={icon} size={26} color="currentColor" />
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 4 }}>
        {title && <CmpText variant="titleMedium" color="currentColor">{title}</CmpText>}
        {children && <CmpText variant="bodyLarge" color="currentColor">{children}</CmpText>}
        {action}
      </div>
    </div>
  );
}

const SVG_C = {};
function SvgInline({ src }) {
  const [html, setHtml] = React.useState("");
  React.useEffect(() => {
    let on = true;
    if (!SVG_C[src]) SVG_C[src] = fetch(src).then((r) => r.text()).then((t) => t.replace(/<\?xml[^>]*\?>/, "").replace(/<metadata[\s\S]*?<\/metadata>/, "")
      // Жёсткий чёрный в исходниках → currentColor, иначе в тёмной теме не виден.
      .replace(/(fill|stroke)(=")(#000000|#000|black)"/gi, '$1$2currentColor"').replace(/(fill|stroke)="#2672d1"/gi, '$1="currentColor" style="$1:var(--primary)"').replace(/(fill|stroke):\s*(#000000|#000|black)\b/gi, "$1:currentColor"));
    SVG_C[src].then((t) => on && setHtml(t));
    return () => { on = false; };
  }, [src]);
  // Все иллюстрации — на одном артборде 800×550 в общем масштабе: одна рамка для всех сохраняет одинаковую толщину линий.
  const ref = React.useRef(null);
  React.useLayoutEffect(() => {
    const svg = ref.current && ref.current.querySelector("svg"); if (!svg || !html) return;
    svg.setAttribute("viewBox", "40 24 720 495");
  }, [html]);
  return <span ref={ref} className="bc-svg" aria-hidden="true" style={{ display: "block", width: "100%", height: "100%" }} dangerouslySetInnerHTML={{ __html: html }} />;
}

// Иллюстрация шага. mode: req (запрос действия) · done (выполнено) · wait (ожидание) · idle.
function Illus({ id, mode = "idle", size = 200, onPrimary = false }) {
  const m = ILL[id] || ILL.prep;
  const src = BC_ILLUS[`${id}_${mode}`] || BC_ILLUS[id];
  const req = mode === "req", done = mode === "done", wait = mode === "wait";
  const fg = onPrimary ? "var(--primary)" : req ? "var(--on-primary-container)" : "var(--on-surface-variant)";
  if (src) {
    const w = Math.round(size * 1.45), ink = onPrimary ? "var(--on-surface)" : req ? "var(--on-primary-container)" : done ? "var(--on-surface)" : "var(--on-surface-variant)";
    return (
      <div aria-hidden="true" style={{ position: "relative", width: w, maxWidth: "100%", aspectRatio: "800 / 550", flex: "0 0 auto" }}>
        {(req || wait) && <div className="bc-anim" style={{ position: "absolute", inset: 0, borderRadius: 32, border: `3px solid ${onPrimary ? "var(--on-primary)" : "var(--primary)"}`, animation: `bcRing ${req ? 1.4 : 2.2}s ease-out infinite` }} />}
        <div className={wait ? "bc-anim" : undefined} style={{ position: "absolute", inset: 0, borderRadius: 32, boxSizing: "border-box", color: ink,
          background: onPrimary ? "var(--surface)" : req ? "var(--primary-container)" : "var(--background-surface-card)",
          border: req && !onPrimary ? "3px solid var(--primary)" : "1px solid var(--border-highlight)", animation: wait ? "bcBreath 2.2s ease-in-out infinite" : undefined }}>
          <div style={{ position: "absolute", inset: 0, borderRadius: 32, overflow: "hidden" }}><div style={{ position: "absolute", inset: 0 }}><SvgInline src={src} /></div></div>
        </div>
      </div>);
  }
  return (
    <div aria-hidden="true" style={{ position: "relative", width: Math.round(size * 1.45), maxWidth: "100%", aspectRatio: "800 / 550", flex: "0 0 auto" }}>
      {(req || wait) && <div className="bc-anim" style={{ position: "absolute", inset: 0, borderRadius: 32, border: `3px solid ${onPrimary ? "var(--on-primary)" : "var(--primary)"}`, animation: `bcRing ${req ? 1.4 : 2.2}s ease-out infinite` }} />}
      <div className={wait ? "bc-anim" : undefined} style={{ position: "absolute", inset: 0, borderRadius: 32, overflow: "hidden", boxSizing: "border-box",
        border: req && !onPrimary ? "3px solid var(--primary)" : "1px solid var(--border-highlight)",
        background: onPrimary ? "var(--surface)" : req ? "repeating-linear-gradient(135deg, var(--primary-container) 0 8px, color-mix(in srgb, var(--primary-container) 70%, var(--surface)) 8px 16px)"
          : "repeating-linear-gradient(135deg, var(--surface-variant) 0 8px, color-mix(in srgb, var(--surface-variant) 55%, var(--surface)) 8px 16px)",
        display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 8, animation: wait ? "bcBreath 2.2s ease-in-out infinite" : undefined }}>
        {src ? <div style={{ position: "absolute", inset: 12 }}><SvgInline src={src} /></div> : <>
          <CmpIcon icon={m.icon} size={Math.round(size * 0.34)} color={fg} />
          <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional1-size)", color: fg, opacity: .85, textAlign: "center", padding: "0 8px" }}>{id}{mode !== "idle" ? "_" + mode : ""}</span>
        </>}
      </div>
      {done && <span style={{ position: "absolute", right: -8, bottom: -8, width: 48, height: 48, borderRadius: 999, background: "var(--primary)", color: "var(--on-primary)", display: "grid", placeItems: "center", border: "4px solid var(--background)" }}><CmpIcon icon="check" size={28} color="currentColor" /></span>}
    </div>
  );
}

function ResultHero({ icon, tone = "primary", size = 120 }) {
  const map = { primary: ["var(--primary-container)", "var(--on-primary-container)"], tertiary: ["var(--tertiary-container)", "var(--on-tertiary-container)"], error: ["var(--error-container)", "var(--on-error-container)"], neutral: ["var(--surface-variant)", "var(--on-surface-variant)"] };
  const [bg, fg] = map[tone];
  return <div aria-hidden="true" style={{ width: size, height: size, borderRadius: "50%", background: bg, display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={icon} size={Math.round(size * 0.46)} color={fg} /></div>;
}

function HowItWorks({ items }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 2, padding: "8px 16px", borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
      {items.map(([ic, t], i) => (
        <div key={i} style={{ display: "flex", alignItems: "center", gap: 14, minHeight: 56, padding: "6px 0", borderTop: i ? "1px solid var(--border-highlight)" : "none" }}>
          <span style={{ width: 40, height: 40, borderRadius: "var(--r-card-small-radius)", background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon={ic} size={22} color="currentColor" /></span>
          <CmpText variant="bodyLarge">{t}</CmpText>
        </div>))}
    </div>
  );
}

// Каркас экрана: шапка, прокручиваемое тело, на compact — закреплённые кнопки внизу.
function Shell({ layout, title, nav, main, side, footer, flush, status }) {
  const compact = layout === "compact";
  const lastStatus = React.useRef(null);
  if (status) lastStatus.current = status;
  const foot = footer && <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", maxWidth: compact ? "none" : 440 }}>{footer}</div>;
  let body;
  if (layout === "expanded") body = (
    <div style={{ maxWidth: 1040, margin: "0 auto", padding: "28px 32px", boxSizing: "border-box", display: "grid", gridTemplateColumns: side ? "minmax(0,1fr) 360px" : "minmax(0,1fr)", gap: 32, alignItems: "start", minHeight: "100%" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 20, alignItems: "center", minWidth: 0 }}>{main}{foot}</div>
      {side && <aside style={{ position: "sticky", top: 0, display: "flex", flexDirection: "column", gap: 16 }}>{side}</aside>}
    </div>);
  else body = (
    <div style={{ maxWidth: compact ? "none" : 680, margin: "0 auto", padding: compact ? (flush ? 0 : "16px 16px 24px") : "28px 24px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: compact ? 16 : 20, alignItems: "center", minHeight: flush ? "100%" : undefined }}>
      {main}{!compact && foot}{side && <div style={{ width: "100%", padding: flush ? "0 16px 16px" : 0, boxSizing: "border-box" }}>{side}</div>}
    </div>);
  return (
    <React.Fragment>
      <CmpTopAppBar title={title} subtitle={BC_SUB} navigationIcon={nav} />
      {/* Место под статус зарезервировано всегда: во время процесса он плавно появляется, после — плавно гаснет, контент не сдвигается. */}
      <div aria-hidden={!status} style={{ flex: "0 0 auto", height: compact ? 64 : 68, boxSizing: "border-box", padding: compact ? "12px 16px" : "14px 32px", transition: "background-color .3s ease, border-color .3s ease",
        background: status ? "var(--background-surface-card)" : "transparent", borderBottom: `1px solid ${status ? "var(--outline-variant)" : "transparent"}` }}>
        <div style={{ maxWidth: layout === "expanded" ? 976 : 632, margin: "0 auto", opacity: status ? 1 : 0, transition: "opacity .3s ease" }}>{status || lastStatus.current}</div></div>
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{body}</div>
      {compact && footer && <div style={{ padding: "12px 16px 16px", borderTop: "1px solid var(--border-highlight)", background: "var(--background-surface-card)" }}>{foot}</div>}
    </React.Fragment>
  );
}

function BackBtn({ onClick }) { return <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onClick} />; }
function LockedNav() {
  return <div role="img" aria-label="Выход недоступен до завершения записи" title="Выход недоступен до завершения записи" style={{ width: 48, height: 48, display: "grid", placeItems: "center", color: "var(--on-surface-variant)" }}><CmpIcon icon="lock" size={24} color="currentColor" /></div>;
}

function ExitDialog({ onStay, onExit }) {
  return <CmpDialog open onDismissRequest={onStay} icon="warning" title="Внимание" style={{ maxWidth: 440 }}
    text="При активном процессе настройки закрытие окна может привести к некорректной работе сигнализации. Вы действительно хотите выйти?"
    dismissButton={<CmpTextButton text="Да" onClick={onExit} style={H48} />}
    confirmButton={<CmpButton text="Нет" onClick={onStay} style={H48} />} />;
}

function BcPin({ onCancel, onOk }) {
  const [pin, setPin] = React.useState("");
  const [err, setErr] = React.useState(false);
  const ref = React.useRef(null);
  React.useEffect(() => { ref.current && ref.current.focus(); }, []);
  const submit = () => { if (pin === "1234") onOk(); else { setErr(true); setPin(""); ref.current && ref.current.focus(); } };
  return (
    <CmpDialog open onDismissRequest={onCancel} icon="lock" title="Введите PIN-код программирования" style={{ maxWidth: 400 }}
      text={<span style={{ display: "flex", flexDirection: "column", gap: 14, alignItems: "center" }}>
        <span>PIN нужен для обучения блока. Демо: 1234</span>
        {React.createElement(window.PandoraSpecDesignSystem_b8d6e1.CmpInputDigit, { value: pin, onChange: (v) => { setErr(false); setPin(v); }, length: 4, secure: true, error: err, autoFocus: true })}
        <span style={{ minHeight: 22, color: "var(--error-default)", fontWeight: 700, fontSize: "var(--t-headline3-size)" }} aria-live="polite">{err ? "Неверный PIN-код" : ""}</span>
      </span>}
      dismissButton={<CmpTextButton text="Отмена" onClick={onCancel} style={H48} />}
      confirmButton={<CmpButton text="Продолжить" disabled={pin.length < 4} onClick={submit} style={H48} />} />
  );
}

function LostOverlay({ transport, onDisconnect }) {
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 6, background: "var(--background)", display: "flex", flexDirection: "column" }}>
      <CmpTopAppBar title="" navigationIcon={<BackBtn onClick={onDisconnect} />} />
      <div style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", padding: 24 }}>
        <div style={{ maxWidth: 440, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 16, textAlign: "center" }}>
          <ResultHero icon="link_off" tone="neutral" />
          <CmpText variant="headlineSmall">Отсутствует соединение с устройством</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{transport === "usb" ? "Проверьте USB соединение с устройством." : "Блок выключен или находится далеко. Поднесите телефон ближе и проверьте питание блока."}</CmpText>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={20} strokeWidth={2.5} /><CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём возвращения блока…</CmpText></div>
          <CmpOutlinedButton text="Отключиться" icon="link_off" fullWidth style={H52} onClick={onDisconnect} />
        </div>
      </div>
    </div>
  );
}

// ════════ Бесключевой обход ══════════════════════════════════════════════════
const BP_SEQ = [
  { k: "wait", il: "prep", t: "Подготовка сигнализации к обучению ключа. Ожидайте", h: "Сигнализация подготовлена" },
  { k: "act", il: "hood_open", t: "Откройте капот", done: "Капот открыт, ожидайте", h: "Капот открыт" },
  { k: "act", il: "ign_on", t: "Включите зажигание", done: "Зажигание включено. Ожидайте", h: "Зажигание включено" },
  { k: "act", il: "engine_start", t: "Запустите двигатель", done: "Двигатель запущен. Ожидайте", h: "Двигатель запущен" },
  { k: "wait", il: "ecu", t: "Идёт обмен данными с блоком управления двигателем. Ожидайте", done: "Обмен данными с блоком управления двигателем завершён. Ожидайте", h: "Обмен данными с ЭБУ завершён" },
  { k: "act", il: "engine_stop", t: "Заглушите двигатель", done: "Двигатель заглушен. Ожидайте", h: "Двигатель заглушен" },
  { k: "act", il: "ign_off", t: "Выключите зажигание", done: "Зажигание выключено. Ожидайте", h: "Зажигание выключено" },
  { k: "act", il: "hood_close", t: "Закройте капот", done: "Капот закрыт, ожидайте", h: "Капот закрыт" },
  { k: "act", il: "key_out", t: "Выключите зажигание и вытащите ключ", h: "Ключ извлечён" },
];
const LADA_STEP = { k: "instr", il: "lada", h: "Инструкция LADA APS выполнена" };
const TOYOTA_STEP = { k: "instr", il: "toyota", h: "Инструкция Toyota/Daihatsu выполнена" };
const LEARN_STEP = { k: "wait", il: "learn", t: "Идет процесс обучения, ожидайте", h: "Обучение" };
const ALARM_EX_STEP = { k: "wait", il: "alarm_ex", t: "Идет обмен данных с блоком сигнализации. Ожидайте", h: "Обмен с блоком сигнализации" };
// Критичные ожидания «не прерывайте»: head — что происходит, крупно — запрет.
const LADA_CAN_STEP = { k: "crit", il: "lada", head: "Обход иммобилайзера LADA CAN. Обмен данными с приложением.", t: "Не прерывайте соединение", h: "LADA CAN" };
const ALARM_CRIT_STEP = { k: "crit", il: "alarm_ex", head: "Обмен данными с блоком сигнализации.", t: "Не прерывайте соединение", h: "Обмен с блоком сигнализации" };
const BP_SPECIAL = { lada: LADA_STEP, toyota: TOYOTA_STEP, learn: LEARN_STEP, alarm_ex: ALARM_EX_STEP, lada_can: LADA_CAN_STEP, alarm_crit: ALARM_CRIT_STEP };

// Инструкции, переписанные под структуру «действие / запрет / отклик / если не получилось» (смысл и цифры — из strings_ru.xml,
// CRAWLER_STATE_LADA_APS_*, LEARN_TOYOTA_DAIHATSU_*). ТРЕБУЕТ СВЕРКИ С ТЕХПОДДЕРЖКОЙ. key: red — обучающий, black — рабочий.
const INSTR = {
  lada: { il: "lada", title: "Обход иммобилайзера LADA APS", lead: "Внимательно прочитайте инструкцию ниже и выполните все шаги", items: [
    { key: "red", a: "Закройте все двери автомобиля. Включите зажигание обучающим (красным) ключом и подождите во включенном состоянии не менее 6 секунд." },
    { key: "red", a: "Выключите зажигание и выньте обучающий (красный) ключ из замка зажигания.", r: "Лампа быстро мигает всё время, пока процедура обучения выполняется правильно." },
    { key: "black", a: "Пока мигает индикатор (до 6 секунд), вставьте рабочий (черный) ключ в замок и включите зажигание.", r: "Иммобилайзер выдаёт три звуковых сигнала при включении зажигания." },
    { key: "black", a: "Ждите с включенным зажиганием 6 секунд, затем выключите зажигание.", r: "Через 6 секунд иммобилайзер выдаёт два звуковых сигнала." },
    { key: "red", a: "Пока мигает индикатор (до 6 секунд), выньте ключ, вставьте снова обучающий (красный) ключ и включите зажигание.", r: "Три звуковых сигнала сразу при включении зажигания и ещё два — через 6 секунд." },
    { key: "red", a: "Выключите зажигание.", no: "Не вынимайте обучающий (красный) ключ из замка зажигания.", r: "Через 6 секунд — одиночный сигнал зуммера, индикатор мигает в два раза быстрее.", f: "Сигнала нет, а быстрое мигание прекратилось — выключите зажигание и начните процедуру снова." },
    { key: "red", a: "Не позднее 6 секунд после сигнала (пока быстро мигает индикатор) включите зажигание этим же обучающим (красным) ключом. Через 2–3 секунды выключите зажигание.", r: "Не позднее 5 секунд после выключения — три сигнала зуммера, индикатор перестаёт быстро мигать.", then: "Выключите зажигание и выньте обучающий (красный) ключ минимум на 10 секунд." },
  ] },
  toyota: { il: "toyota", title: "Обход иммобилайзера Toyota/Daihatsu TX RX", lead: "Внимательно прочитайте инструкцию ниже и выполните все шаги.", items: [
    { a: "Откройте дверь водителя и сядьте в машину." },
    { a: "Замкните 13-й пин разъёма OBDII на «массу»." },
    { a: "Включите и выключите зажигание", n: "4 раза" },
    { a: "После выключения зажигания закройте и откройте дверь водителя", n: "3 раза", then: "В конце закройте дверь." },
    { wait: true, r: "Дождитесь светозвукового сигнала об успешном обучении." },
  ] },
};
const BP_ERR = {
  short: "Ошибка при обучении: Нет VIN номера в CAN шине",
  long: "Ошибка при обучении: Не открыта водительская дверь, нет ответа от компонентов авто. Проверьте подключение CAN2, автомобиль соответствует списку для обхода, при обучении каждым ключом запускайте ДВС",
  timeout: "Ошибка при обучении: Выход из режима обучения по таймауту (нет зажигания или зажигание в \"разрыв\", или используется LIN вместо Immo_Key порта)",
  repeat: "Ошибка при обучении: Повторите обучение. При выключении зажигания обязательно извлеките ключ из замка зажигания, после каждого включения зажигания",
  immo: "Ошибка при обучении: Выключен иммобилайзер. Для проверки отключите считывающую рамку иммобилайзера и произведите автозапуск",
  general: "Ошибка при обучении. Проверьте корректность настроек, особенно \"Вариант бесключевого обхода\" и попробуйте еще раз",
};
// «Что случилось» — первое предложение после «Ошибка при обучении»; «Что проверить» — остальное (или всё, если начинается с призыва).
function splitErr(s) {
  const body = s.replace(/^Ошибка при обучении[:.]\s*/, "");
  const m = body.match(/^(.+?[.)])\s+(.+)$/);
  let what = m ? m[1] : body, check = m ? m[2] : "";
  if (/^(Проверьте|Повторите|Убедитесь)/.test(what)) { check = (what + " " + check).trim(); what = ""; }
  return { what, check };
}
function ErrBlock({ text }) {
  const { what, check } = splitErr(text);
  const box = (bg, fg, ic, title, t) => (
    <div style={{ width: "100%", boxSizing: "border-box", padding: "14px 18px", borderRadius: "var(--r-card-medium-radius)", background: bg, color: fg, display: "flex", gap: 12, alignItems: "flex-start" }}>
      <CmpIcon icon={ic} size={26} color="currentColor" />
      <div style={{ display: "flex", flexDirection: "column", gap: 4, minWidth: 0 }}>
        <CmpText variant="titleMedium" color="currentColor">{title}</CmpText>
        <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", lineHeight: 1.45, textWrap: "pretty" }}>{t}</span>
      </div>
    </div>);
  return (
    <div role="alert" style={{ width: "100%", display: "flex", flexDirection: "column", gap: 10 }}>
      {what && box("var(--error-container)", "var(--on-error-container)", "error", "Что случилось", what)}
      {check && box("var(--background-surface-highlight)", "var(--on-surface)", "fact_check", "Что проверить", check)}
    </div>);
}

const kindOf = (st, sub) => st.k === "act" ? (sub === "done" ? "done" : "act") : st.k;
const textOf = (st, sub) => (sub === "done" && st.done) ? st.done : st.t;
const modeOf = (kind) => kind === "act" ? "req" : kind === "done" ? "done" : "wait";
const CRIT_BG = "var(--tertiary-container)", CRIT_FG = "var(--on-tertiary-container)";

function KeyTag({ k }) {
  const red = k === "red";
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 30, padding: "0 12px", borderRadius: 999, boxSizing: "border-box", fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, flex: "0 0 auto",
    background: red ? "#C62828" : "#1F2324", color: "#fff", border: red ? "none" : "1px solid var(--border-light)" }}><CmpIcon icon="vpn_key" size={16} color="currentColor" />{red ? "Красный · обучающий" : "Чёрный · рабочий"}</span>;
}

function InstrView({ id, layout }) {
  const D = INSTR[id];
  const row = (ic, label, t, fg) => (
    <div style={{ display: "flex", gap: 8, alignItems: "flex-start", color: fg }}>
      <CmpIcon icon={ic} size={20} color="currentColor" />
      <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", lineHeight: 1.45, textWrap: "pretty" }}><b style={{ fontWeight: 700 }}>{label}</b> {t}</span>
    </div>);
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16, width: "100%" }}>
      {D.il && <Illus id={D.il} mode="req" size={illSize(layout)} />}
      <Banner icon="menu_book" tone="info" title={D.title}>{D.lead}</Banner>
      <ol style={{ listStyle: "none", margin: 0, padding: 0, display: "flex", flexDirection: "column", gap: 10, width: "100%" }}>
        {D.items.map((it, i) => (
          <li key={i} style={{ display: "flex", gap: 14, padding: "14px 16px", borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
            <span style={{ width: 40, height: 40, borderRadius: 999, flex: "0 0 auto", display: "grid", placeItems: "center", background: it.wait ? "var(--background-surface-highlight)" : "var(--primary)", color: it.wait ? "var(--on-surface)" : "var(--on-primary)", fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", fontWeight: 700 }}>{i + 1}</span>
            <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 8, paddingTop: 6 }}>
              {it.a && <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", lineHeight: 1.45, fontWeight: 700, color: "var(--on-surface)", textWrap: "pretty" }}>{it.a}{it.n && <React.Fragment> <span style={{ display: "inline-block", padding: "0 10px", borderRadius: 8, background: "var(--secondary-container)", color: "var(--on-secondary-container)", fontWeight: 800 }}>{it.n}</span></React.Fragment>}</span>}
              {it.no && row("block", "Нельзя:", it.no, "var(--error)")}
              {it.r && row(it.wait ? "hourglass_top" : "hearing", it.wait ? "Ожидание:" : "Отклик:", it.r, "var(--on-surface-variant)")}
              {it.then && <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", lineHeight: 1.45, fontWeight: 700, color: "var(--on-surface)", textWrap: "pretty" }}>{it.then}</span>}
              {it.f && <div style={{ padding: "10px 12px", borderRadius: "var(--r-card-small-radius)", background: "var(--error-container)", color: "var(--on-error-container)" }}>{row("replay", "Если не получилось:", it.f, "currentColor")}</div>}
              {it.key && <div style={{ display: "flex" }}><KeyTag k={it.key} /></div>}
            </div>
          </li>))}
      </ol>
    </div>
  );
}

function WaitRow({ text = "Ожидайте", elapsed, onPrimary }) {
  const c = onPrimary ? "var(--on-primary)" : "var(--on-surface-variant)";
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%" }}>
      <CmpLinearProgressIndicator progress={null} height={8} />
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12 }}>
        <CmpText variant="labelLarge" color={c}>{text}</CmpText>
        <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, color: c }} aria-label={`Прошло ${elapsed}`}>{elapsed}</span>
      </div>
    </div>
  );
}

function KindLabel({ kind, onPrimary, center }) {
  const act = kind === "act";
  const crit = kind === "crit";
  const [ic, t] = act ? ["touch_app", "Выполните действие"] : kind === "done" ? ["check_circle", "Блок принял действие"] : crit ? ["sync_lock", "Критичный обмен данными"] : ["hourglass_top", "Блок работает"];
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 8, height: 36, padding: "0 14px", borderRadius: 999, alignSelf: center ? "center" : "flex-start", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, letterSpacing: .2,
    background: onPrimary ? "var(--on-primary)" : act ? "var(--primary)" : crit ? "var(--tertiary)" : "var(--background-surface-highlight)", color: onPrimary ? "var(--primary)" : act ? "var(--on-primary)" : crit ? "var(--on-tertiary)" : "var(--on-surface)" }}>
    <CmpIcon icon={ic} size={20} color="currentColor" />{t}</span>;
}

// Шаг — карточка-команда под иллюстрацией.
function StepCard({ st, sub, layout, elapsed }) {
  const kind = kindOf(st, sub), act = kind === "act", crit = kind === "crit";
  return (
    <div key={st.il + sub} className="bc-anim" style={{ width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 20, animation: "bcIn .25s ease both" }}>
      <Illus id={st.il} mode={modeOf(kind)} size={illSize(layout)} />
      <div aria-live="assertive" style={{ width: "100%", boxSizing: "border-box", borderRadius: "var(--r-card-medium-radius)", padding: 20, display: "flex", flexDirection: "column", gap: 14,
        background: act ? "var(--primary-container)" : crit ? CRIT_BG : "var(--background-surface-highlight)", border: act ? "3px solid var(--primary)" : crit ? "3px solid var(--tertiary)" : "1px solid var(--border-highlight)" }}>
        <KindLabel kind={kind} />
        {crit && <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-headline2-size)", lineHeight: 1.4, color: CRIT_FG, textWrap: "pretty" }}>{st.head}</span>}
        <span style={{ fontFamily: "var(--font-sans)", fontSize: act || crit ? 30 : 22, lineHeight: 1.25, fontWeight: act || crit ? 700 : 600, color: act ? "var(--on-primary-container)" : crit ? CRIT_FG : "var(--on-surface)", textWrap: "balance" }}>{textOf(st, sub)}</span>
        {crit ? <CmpText variant="bodyLarge" color={CRIT_FG}>Не сворачивайте приложение, не уносите телефон от блока и не отключайте USB.</CmpText> : act ? <CmpText variant="bodyLarge" color="var(--on-primary-container)">Блок увидит действие сам. Нажимать ничего не нужно.</CmpText>
          : <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Нажимать ничего не нужно</CmpText>}
      </div>
    </div>
  );
}

function BypassScreen({ layout, scenario, variant, transport, onBack, onClone, active }) {
  const S = scenario;
  const init = {
    phase: ["act", "done", "wait", "exit", "lost"].includes(S) || BP_SPECIAL[S] ? "run" : ["ok", "ok_noclone", "needclone", "unsupported"].includes(S) || S.startsWith("err_") ? "result" : "start",
    idx: BP_SPECIAL[S] ? 1 : S === "act" || S === "done" ? 2 : S === "wait" ? 4 : S === "exit" || S === "lost" ? 3 : 0,
    sub: S === "done" ? "done" : "act",
    result: S === "ok_noclone" ? "ok" : S.startsWith("err_") ? "error" : S,
  };
  const [phase, setPhase] = React.useState(init.phase);
  const [idx, setIdx] = React.useState(init.idx);
  const [sub, setSub] = React.useState(init.sub);
  const [seq, setSeq] = React.useState(BP_SPECIAL[S] ? [BP_SEQ[0], BP_SPECIAL[S]] : BP_SEQ);
  const [result, setResult] = React.useState(init.result);
  const [live, setLive] = React.useState(false);
  const [dialog, setDialog] = React.useState(S === "pin" ? "pin" : S === "exit" ? "exit" : null);
  const [lost, setLost] = React.useState(S === "lost");
  const [lostMsg, setLostMsg] = React.useState(S === "start_lost");
  const can = S === "start_can";
  const cloneAvail = S !== "ok_noclone";
  const errText = BP_ERR[S.replace("err_", "")] || BP_ERR.short;
  const st = seq[idx];
  const elapsed = useElapsed(phase + idx + sub, live ? 0 : 17);

  React.useEffect(() => {
    if (!live || phase !== "run" || dialog) return;
    const next = () => { if (idx + 1 >= seq.length) { setPhase("result"); setResult("ok"); setLive(false); } else { setIdx(idx + 1); setSub("act"); } };
    const t = setTimeout(() => { if (sub !== "done" && st.done) setSub("done"); else next(); }, st.k === "act" && sub !== "done" ? 2600 : 1900);
    return () => clearTimeout(t);
  }, [live, phase, idx, sub, dialog]);
  React.useEffect(() => { if (!lost) return; const t = setTimeout(() => { setLost(false); setPhase("start"); setLostMsg(true); setLive(false); }, 4000); return () => clearTimeout(t); }, [lost]);

  const begin = () => { setLostMsg(false); setSeq(BP_SEQ); setIdx(0); setSub("act"); setPhase("run"); setLive(true); };
  const start = () => transport === "ble" ? setDialog("pin") : begin();
  const back = () => phase === "run" ? setDialog("exit") : onBack();
  const compact = layout === "compact";
  let main, side, footer, status, flush = false;

  if (phase === "start") {
    main = <React.Fragment>
      <Illus id="bypass_start" size={illSize(layout)} />
      {lostMsg && <Banner icon="link_off" tone="error" title="Процедура прервана: связь с блоком потеряна">Начните обучение заново.</Banner>}
      <div style={{ textAlign: "center", display: "flex", flexDirection: "column", gap: 8, maxWidth: 560 }}>
        <CmpText variant="headlineSmall">Обучение бесключевому обходу</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок научится обходить штатный иммобилайзер, чтобы автозапуск работал без ключа в салоне.</CmpText>
      </div>
      {can && <Banner icon="memory" tone="info" title={`Устройство с прошивкой для CAN кода ${BC_DEV.can}`}>Данный функционал теперь зависит не только от настроек системы, но и от версии прошивки. Теперь необходимо обновлять прошивку под конкретный CAN код автомобиля.</Banner>}
      <Banner icon="power_settings_new" title="Сначала перезапустите систему по питанию">
        {"Функция бесключевого обхода доступна только, если после изменения настроек был выполнен перезапуск системы по питанию."}{transport === "usb" ? " Для корректной перезагрузки необходимо полностью обесточить сигнализацию и запитать её обратно." : ""}
      </Banner>
      {layout !== "expanded" && <div style={{ width: "100%" }}><HowItWorks items={BP_HOW} /></div>}
    </React.Fragment>;
    side = layout === "expanded" ? <HowItWorks items={BP_HOW} /> : null;
    footer = <CmpButton text="Начать обучение" icon="play_arrow" fullWidth style={H52} onClick={start} />;
  } else if (phase === "run") {
    const elap = elapsed;
    const k = kindOf(st, sub);
    status = <WaitRow text={st.k === "instr" ? "Блок ждёт выполнения инструкции" : k === "crit" ? "Не прерывайте соединение" : k === "act" ? "Блок ждёт ваше действие" : "Блок работает. Ожидайте"} elapsed={elap} />;
    if (st.k === "instr") main = <InstrView id={st === TOYOTA_STEP ? "toyota" : "lada"} layout={layout} />;
    else main = <StepCard st={st} sub={sub} layout={layout} elapsed={elap} />;
  } else {
    const R = {
      ok: ["res_ok", "primary", "Обучение прошло успешно", cloneAvail ? "Если автозапуск работает нестабильно, выполните процедуру Clone." : "Бесключевой обход настроен. Проверьте автозапуск."],
      needclone: ["res_fail", "tertiary", "Необходим CLONE для корректной работы автозапуска", "Обучение завершено. Чтобы автозапуск работал, выполните процедуру Clone."],
      unsupported: ["res_fail", "neutral", "Бесключевой обход не поддерживается", "Для этого автомобиля или прошивки блока обход недоступен."],
      error: ["res_fail", "error", "Обучение не выполнено", null],
    }[result];
    main = <React.Fragment>
      <Illus id={R[0]} size={illSize(layout)} />
      <div style={{ textAlign: "center", display: "flex", flexDirection: "column", gap: 10, maxWidth: 560 }}>
        <CmpText variant="headlineSmall">{R[2]}</CmpText>
        {R[3] && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{R[3]}</CmpText>}
      </div>
      {result === "error" && <ErrBlock text={errText} />}
    </React.Fragment>;
    side = null;
    footer = result === "ok" ? <React.Fragment><CmpButton text="Готово" fullWidth style={H52} onClick={onBack} />{cloneAvail && <CmpOutlinedButton text="Перейти к процедуре Clone" icon="content_copy" fullWidth style={H52} onClick={onClone} />}</React.Fragment>
      : result === "needclone" ? <React.Fragment><CmpButton text="Перейти к процедуре Clone" icon="content_copy" fullWidth style={H52} onClick={onClone} /><CmpOutlinedButton text="Закрыть" fullWidth style={H52} onClick={onBack} /></React.Fragment>
      : result === "unsupported" ? <CmpButton text="Закрыть" fullWidth style={H52} onClick={onBack} />
      : <React.Fragment><CmpButton text="Повторить" icon="refresh" fullWidth style={H52} onClick={begin} /><HelpBtns from="Бесключевой обход" text={`Бесключевой обход: обучение не выполнено. ${errText}`} /></React.Fragment>;
  }

  return (
    <div style={{ display: active ? "flex" : "none", flexDirection: "column", height: "100%", position: "relative" }}>
      <Shell layout={layout} title="Бесключевой обход" nav={<BackBtn onClick={back} />} main={main} side={side} footer={footer} flush={flush} status={status} />
      {dialog === "pin" && <BcPin onCancel={() => setDialog(null)} onOk={() => { setDialog(null); begin(); }} />}
      {dialog === "exit" && <ExitDialog onStay={() => setDialog(null)} onExit={() => { setDialog(null); setLive(false); onBack(); }} />}
      {lost && <LostOverlay transport={transport} onDisconnect={onBack} />}
    </div>
  );
}
const BP_HOW = [
  ["vpn_key", "Понадобится штатный ключ автомобиля"],
  ["directions_car", "Блок по очереди попросит включить и выключить зажигание, запустить и заглушить двигатель"],
  ["visibility", "Следите за экраном: команда для вас выделена цветом, нажимать ничего не нужно"],
];

// ════════ Процедура Clone ════════════════════════════════════════════════════
const CL_STAGES = [
  { t: "Подготовка", s: "Подготовка к процедуре клонирования" },
  { t: "Чтение сигнализации", s: "Чтение сигнализации" },
  { t: "Вычисление на сервере", s: "Ваша задача в очереди", s2: "Ваша задача обрабатывается", long: true },
  { t: "Загрузка данных в блок", s: "Загрузка посчитанных данных", lock: true },
  { t: "Проверка данных", s: "Проверка данных", lock: true },
];
const CL_ERR = {
  error: "Ошибка процедуры Clone. Невозможно вычислить ключ",
  error_access: "У данного устройства нет доступа для процедуры Clone",
  error_device: "Ошибка процедуры Clone. Неопознанное устройство",
  error_unknown: "Неизвестная ошибка процедуры Clone. Попробуйте еще раз",
  error_code: "Процедура Clone завершилась с ошибкой. Код ошибки: 4",
};
// Советы сервера — произвольные строки 1–5 шт., без перевода (демо).
const CL_TIPS = ["Проверьте, что выбран правильный CAN код автомобиля", "Выполните бесключевой обход повторно и повторите Clone", "Убедитесь, что во время чтения зажигание было включено"];
const CL_HOW = [
  ["cloud", "Ключ вычисляется на нашем сервере — нужен интернет"],
  ["schedule", "Задача может стоять в очереди: процедура займёт несколько минут"],
  ["lock", "На этапах записи в блок выйти будет нельзя"],
];

function CloneScreen({ layout, scenario, transport, onBack, active, fromBypass }) {
  const S = scenario;
  const stage0 = { s1: 0, s2: 1, s3q: 2, s3p: 2, s4: 3, s5: 4, exit: 1, lost: 1 }[S];
  const [phase, setPhase] = React.useState(stage0 != null ? "run" : ["ok", "already", "unsupported"].includes(S) || S.startsWith("error") ? "result" : "start");
  const [stage, setStage] = React.useState(stage0 || 0);
  const [sub, setSub] = React.useState(S === "s3p" ? "proc" : "queue");
  const [result, setResult] = React.useState(S.startsWith("error") ? "error" : S);
  const [att, setAtt] = React.useState(S === "zero" ? 0 : S === "unknown" ? null : S.startsWith("error") ? 2 : 3);
  const [live, setLive] = React.useState(false);
  const [dialog, setDialog] = React.useState(S === "confirm" ? "confirm" : S === "pin" ? "pin" : S === "exit" ? "exit" : null);
  const [lost, setLost] = React.useState(S === "lost");
  const [lostMsg, setLostMsg] = React.useState(false);
  const elapsed = useElapsed(phase + stage + sub, live ? 0 : (stage === 2 ? 74 : 6));
  const compact = layout === "compact";

  React.useEffect(() => {
    if (!live || phase !== "run" || dialog) return;
    const t = setTimeout(() => {
      if (stage === 2 && sub === "queue") return setSub("proc");
      if (stage + 1 >= CL_STAGES.length) { setPhase("result"); setResult("ok"); setLive(false); setAtt((a) => a == null ? a : Math.max(0, a - 1)); }
      else { setStage(stage + 1); setSub("queue"); }
    }, stage === 2 ? 2600 : 1900);
    return () => clearTimeout(t);
  }, [live, phase, stage, sub, dialog]);
  React.useEffect(() => { if (!lost) return; const t = setTimeout(() => { setLost(false); setPhase("start"); setLostMsg(true); setLive(false); }, 4000); return () => clearTimeout(t); }, [lost]);

  const begin = () => { setLostMsg(false); setStage(0); setSub("queue"); setPhase("run"); setLive(true); };
  const locked = phase === "run" && CL_STAGES[stage].lock;
  const back = () => phase === "run" ? (!locked && setDialog("exit")) : onBack();
  const errMsg = CL_ERR[S] || CL_ERR.error;
  const closeTxt = fromBypass ? "Вернуться к обходу" : "Завершить";
  let main, side, footer, status;

  if (phase === "start") {
    main = <React.Fragment>
      <Illus id="clone_start" size={illSize(layout)} />
      {lostMsg && <Banner icon="link_off" tone="error" title="Процедура прервана: связь с блоком потеряна">Попытка не потрачена. Начните процедуру заново.</Banner>}
      <div style={{ textAlign: "center", display: "flex", flexDirection: "column", gap: 8, maxWidth: 560 }}>
        <CmpText variant="headlineSmall">Процедура Clone</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок передаст данные иммобилайзера, наш сервер вычислит ключ, приложение запишет его в блок. От вас — только ждать.</CmpText>
      </div>
      {att === 0 ? <Banner icon="block" tone="error">Исчерпано число попыток CLONE. Обратитесь в тех. поддержку</Banner>
        : att == null ? <Banner icon="cloud_off" tone="info">Не удалось узнать число оставшихся попыток. Начать процедуру можно.</Banner>
        : <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, width: "100%", boxSizing: "border-box", padding: "14px 18px", borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Оставшееся число попыток CLONE</CmpText>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-title1-size)", fontWeight: 700, color: att === 1 ? "var(--error)" : "var(--primary)" }}>{att}</span>
          </div>}
      {layout !== "expanded" && <div style={{ width: "100%" }}><HowItWorks items={CL_HOW} /></div>}
    </React.Fragment>;
    side = layout === "expanded" ? <HowItWorks items={CL_HOW} /> : null;
    footer = att === 0
      ? <HelpBtns primary from="Процедура Clone" text="Процедура Clone: исчерпано число попыток. Нужны дополнительные попытки." />
      : <CmpButton text="Начать Clone" icon="play_arrow" fullWidth style={H52} onClick={() => setDialog("confirm")} />;
  } else if (phase === "run") {
    const g = CL_STAGES[stage];
    const title = g.long ? (sub === "proc" ? g.s2 : g.s) : g.s;
    status = <WaitRow text={`Этап ${stage + 1} из 5 · нажимать ничего не нужно`} elapsed={elapsed} />;
    main = <React.Fragment>
      <Illus id="clone_run" mode="wait" size={illSize(layout)} />
      <div aria-live="polite" style={{ width: "100%", maxWidth: 560, display: "flex", flexDirection: "column", gap: 14, textAlign: "center" }}>
        <CmpText variant="headlineSmall">{title}</CmpText>
        {g.long && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{sub === "queue" ? "Сервер обрабатывает задачи по очереди. Это может занять несколько минут." : "Сервер вычисляет ключ. Это может занять несколько минут."}</CmpText>}
      </div>
      {locked && <Banner icon="lock" title="Выход недоступен до конца проверки">Идёт запись в блок. Не закрывайте приложение и не отключайте питание — иначе попытка будет потеряна.</Banner>}
    </React.Fragment>;
  } else {
    const R = {
      ok: ["res_ok", "primary", "Процедура Clone выполнена", "Ключ записан в блок. Проверьте автозапуск."],
      already: ["res_ok", "primary", "Процедура Clone уже успешно выполнена", "Попытка не потрачена."],
      unsupported: ["res_fail", "neutral", "Clone не поддерживается", "Для этого блока или автомобиля процедура недоступна."],
      error: ["res_fail", "error", "Процедура Clone не выполнена", null],
    }[result];
    main = <React.Fragment>
      <Illus id={R[0]} size={illSize(layout)} />
      <div style={{ textAlign: "center", display: "flex", flexDirection: "column", gap: 10, maxWidth: 560 }}>
        <CmpText variant="headlineSmall">{R[2]}</CmpText>
        {R[3] && <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{R[3]}</CmpText>}
      </div>
      {result === "error" && <React.Fragment>
        <Banner icon="error" tone="error">{errMsg}</Banner>
        {S === "error" && <div style={{ width: "100%", boxSizing: "border-box", padding: "14px 18px", borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)", display: "flex", flexDirection: "column", gap: 8 }}>
          <CmpText variant="titleMedium">Что можно сделать</CmpText>
          <ul style={{ margin: 0, paddingLeft: 22, display: "flex", flexDirection: "column", gap: 6 }}>
            {CL_TIPS.map((t, i) => <li key={i}><CmpText variant="bodyLarge">{t}</CmpText></li>)}
          </ul>
        </div>}
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", width: "100%", padding: "0 4px", boxSizing: "border-box" }}>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Оставшееся число попыток CLONE</CmpText>
          <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-headline1-size)", fontWeight: 700, color: "var(--primary)" }}>{att}</span>
        </div>
      </React.Fragment>}
    </React.Fragment>;
    side = null;
    footer = result === "error"
      ? <React.Fragment><CmpButton text="Повторить" icon="refresh" fullWidth style={H52} onClick={() => setDialog("confirm")} /><HelpBtns from="Процедура Clone" text={`Процедура Clone: ${errMsg}.`} /></React.Fragment>
      : <CmpButton text={result === "unsupported" ? "Закрыть" : closeTxt} fullWidth style={H52} onClick={onBack} />;
  }

  return (
    <div style={{ display: active ? "flex" : "none", flexDirection: "column", height: "100%", position: "relative" }}>
      <Shell layout={layout} title="Процедура Clone" nav={locked ? <LockedNav /> : <BackBtn onClick={back} />} main={main} side={side} footer={footer} status={status} />
      {dialog === "confirm" && <CmpDialog open onDismissRequest={() => setDialog(null)} icon="content_copy" title="Начать Clone?" style={{ maxWidth: 420 }}
        text={att == null ? "Число оставшихся попыток неизвестно. Не расходуйте их зря." : `Доступно попыток: ${att}. Не расходуйте их зря.`}
        dismissButton={<CmpTextButton text="Отмена" onClick={() => setDialog(null)} style={H48} />}
        confirmButton={<CmpButton text="Начать" onClick={() => { if (transport === "ble") setDialog("pin"); else { setDialog(null); begin(); } }} style={H48} />} />}
      {dialog === "pin" && <BcPin onCancel={() => setDialog(null)} onOk={() => { setDialog(null); begin(); }} />}
      {dialog === "exit" && <ExitDialog onStay={() => setDialog(null)} onExit={() => { setDialog(null); setLive(false); onBack(); }} />}
      {lost && <LostOverlay transport={transport} onDisconnect={onBack} />}
    </div>
  );
}

// ════════ Контейнер ══════════════════════════════════════════════════════════
function BypassCloneApp({ view = "bypass", bypassScenario = "start", cloneScenario = "start", variant = "card", transport = "ble", onBack = () => {} }) {
  const [rootRef, layout] = useBcWidth();
  const [v, setV] = React.useState(view);
  const [fromBypass, setFromBypass] = React.useState(false);
  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      <BypassScreen active={v === "bypass"} layout={layout} scenario={bypassScenario} variant={variant} transport={transport} onBack={onBack} onClone={() => { setFromBypass(true); setV("clone"); }} />
      {(v === "clone" || view === "clone") && <CloneScreen active={v === "clone"} layout={layout} scenario={view === "clone" ? cloneScenario : "start"} transport={transport} fromBypass={fromBypass}
        onBack={fromBypass ? () => setV("bypass") : onBack} />}
    </div>
  );
}

// ════════ Панель управления витриной ══════════════════════════════════════════
function BcSeg({ items, value, onChange }) {
  return <div className={"seg" + (items.length > 4 ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
const BP_OPTS = [["start", "B1 Старт"], ["start_can", "B1a CAN"], ["start_lost", "X3 После обрыва"], ["pin", "PIN"], ["act", "B2 Действие"], ["done", "B3 Принято"], ["wait", "B3 Ожидание"], ["learn", "B3 Процесс обучения"], ["alarm_ex", "B3 Обмен с блоком"], ["lada_can", "B3 LADA CAN · не прерывать"], ["alarm_crit", "B3 Блок · не прерывать"], ["lada", "B4 LADA APS"], ["toyota", "B4 Toyota/Daihatsu"],
  ["ok", "B5 Успех"], ["ok_noclone", "B5 без Clone"], ["needclone", "B6 Нужен Clone"], ["unsupported", "B7 Не поддерж."], ["err_short", "B8 Ошибка"], ["err_long", "B8 Длинная"], ["err_timeout", "B8 Таймаут"], ["err_repeat", "B8 Повторите"], ["err_immo", "B8 Иммобилайзер"], ["err_general", "B8 Общая"], ["exit", "X1 Прервать?"], ["lost", "Потеря связи"]];
const CL_OPTS = [["start", "C1 Старт"], ["zero", "C1a 0 попыток"], ["unknown", "C1b Неизвестно"], ["confirm", "C2 Подтверждение"], ["pin", "PIN"], ["s1", "C3 Этап 1"], ["s2", "Этап 2"], ["s3q", "Этап 3 очередь"], ["s3p", "Этап 3 обработка"],
  ["s4", "X2 Этап 4"], ["s5", "X2 Этап 5"], ["ok", "C4 Успех"], ["already", "C4 Уже выполнено"], ["unsupported", "C5 Не поддерж."], ["error", "C6 Ошибка + советы"], ["error_access", "C6 Нет доступа"], ["error_device", "C6 Неопознанное"], ["error_unknown", "C6 Неизвестная"], ["error_code", "C6 Код ошибки"], ["exit", "X1 Прервать?"], ["lost", "Потеря связи"]];
function useBcCtl() {
  const [dark, setDarkRaw] = React.useState(() => { try { const v = localStorage.getItem("ps-theme"); return v ? v === "dark" : matchMedia("(prefers-color-scheme: dark)").matches; } catch (e) { return false; } });
  const setDark = (v) => { setDarkRaw(v); try { localStorage.setItem("ps-theme", v ? "dark" : "light"); } catch (e) {} };
  React.useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const q = new URLSearchParams(location.search);
  const [view, setView] = React.useState(q.get("view") || "bypass");
  const [bs, setBs] = React.useState("start");
  const [cs, setCs] = React.useState("start");
  const [variant, setVariant] = React.useState("card");
  const [transport, setTransport] = React.useState("ble");
  const props = { view, bypassScenario: bs, cloneScenario: cs, variant, transport };
  const bar = (
    <React.Fragment>
      <button type="button" className="theme-fab" onClick={() => setDark(!dark)} aria-label={dark ? "Светлая тема" : "Тёмная тема"} title={dark ? "Светлая тема" : "Тёмная тема"}>{dark ? "☀" : "☾"}</button>
      <div className="toolbar">
        <BcSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(x) => setDark(x === "d")} />
        <BcSeg items={[["bypass", "Бесключевой обход"], ["clone", "Процедура Clone"]]} value={view} onChange={setView} />
        <BcSeg items={[["ble", "Bluetooth"], ["usb", "USB"]]} value={transport} onChange={setTransport} />
      </div>
      <div className="toolbar">{view === "bypass" ? <BcSeg items={BP_OPTS} value={bs} onChange={setBs} /> : <BcSeg items={CL_OPTS} value={cs} onChange={setCs} />}</div>
    </React.Fragment>
  );
  return { dark, props, bar, key: [view, bs, cs, variant, transport].join() };
}

Object.assign(window, { BypassCloneApp, useBcCtl, BcSeg });
