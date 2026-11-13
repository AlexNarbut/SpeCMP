// FastMountApp — мастер «Быстрый монтаж»: подготовка (поиск → проверка блока → автомобиль → конфигурация → сводка)
// и мастер (запись настроек → монтаж → [перезагрузка] → обход → Clone? → Slave → проверка системы → отзыв → готово).
// Каркас: <760 — обычный topbar + закреплённая панель с главной кнопкой, шагов нет;
//         ≥760 — вариант B: слева колонка шагов (StepRail), справа topbar + контент + панель.
// Перезагрузка блока — не шаг, а состояние перехода: «бегущая» линия между двумя шагами в колонке.
// Clone в пути — пунктиром «Если потребуется»; когда обход его требует — плавно становится обычным шагом.
// По USB нет Slave и проверки системы. Пройденные шаги не кликабельны для перехода, тап — короткая справка.
// Иллюстрации — плейсхолдеры (кроме обхода/Clone — SVG из bypass-clone/illus).
(function () {
const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpSurface, CmpText, CmpIcon, CmpCircularProgressIndicator, CmpSnackbar, CmpCheckbox, CmpDivider } = DS;
const TextBtn = ({ fullWidth, style, ...p }) => (DS.CmpTextButton ? <DS.CmpTextButton {...p} style={{ ...(fullWidth ? { width: "100%" } : null), ...style }} /> : <CmpOutlinedButton {...p} fullWidth={fullWidth} style={style} />);
const BIG = { height: 56, fontSize: 16 };
const BIGW = { height: 56, fontSize: 16, width: "100%" };

(function injectStyles() {
  if (document.getElementById("fm-anim")) return;
  const s = document.createElement("style");
  s.id = "fm-anim";
  s.textContent =
    "@keyframes fmIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}" +
    "@keyframes fmUp{from{opacity:0;transform:translateY(16px)}to{opacity:1;transform:none}}" +
    "@keyframes fmRun{from{background-position:0 0}to{background-position:0 24px}}" +
    "@keyframes fmSpin{to{transform:rotate(360deg)}}" +
    "@keyframes fmRing{0%{transform:scale(.9);opacity:.5}100%{transform:scale(1.5);opacity:0}}" +
    "@keyframes fmPulse{0%,100%{box-shadow:0 0 0 0 color-mix(in srgb,var(--primary) 45%,transparent)}50%{box-shadow:0 0 0 8px transparent}}" +
    "@keyframes fmFlash{0%{background:color-mix(in srgb,var(--primary) 30%,transparent)}100%{background:transparent}}" +
    "@keyframes fmGrow{from{opacity:.4;max-height:44px}to{opacity:1;max-height:200px}}" +
    "@keyframes fmPop{0%{transform:scale(0)}60%{transform:scale(1.15)}100%{transform:scale(1)}}" +
    "@keyframes fmIndet{0%{left:-40%;width:40%}100%{left:100%;width:40%}}" +
    "@keyframes fmBurst{0%{transform:scale(.6);opacity:.7}100%{transform:scale(1.9);opacity:0}}" +
    ".fm-hover{transition:background .15s}.fm-hover:hover{background:color-mix(in srgb,var(--on-surface) 8%,transparent)!important}" +
    ".fm-hover:focus-visible,.fm-focus:focus-visible{outline:3px solid var(--primary);outline-offset:2px}" +
    ".fm-svg svg{width:100%;height:100%}" +
    "@media (prefers-reduced-motion: reduce){.fm-anim{animation:none!important;transition:none!important}}";
  document.head.appendChild(s);
})();

// ── Данные (демо) ────────────────────────────────────────────────────────────
const DEV = { name: "Pandora DX-4GS", id: "107131", fw: "3.55", fwNew: "3.57", can: "0213" };
const OTHER_ID = "204588";
const CAR = { brand: "Volkswagen", model: "Polo", year: "2020", can: "0213" };
const CFG = {
  name: "Polo VI 2020 — автозапуск, обход по CAN", author: "Алексей Морозов", rating: 4.8, votes: 37, date: "14.09.2026", can: "0213",
  desc: "Автозапуск по CAN с бесключевым обходом. Управление штатным брелоком (Slave), закрытие окон при постановке в охрану. Питание и масса — у блока предохранителей водителя, CAN — разъём за бардачком.",
};
const PHOTOS = [
  "CAN — разъём за бардачком, зелёный/оранжевый",
  "Питание +12 В — предохранитель F44",
  "Масса — болт под рулевой колонкой",
  "Место установки блока — за панелью приборов",
  "Антенна — верх лобового стекла справа",
];
const DEVICES = [
  { id: "dx4gs", name: "Pandora DX-4GS", type: "Сигнализация", tr: "ble", meta: "−48 dBm", alarm: true },
  { id: "dxl5000", name: "Pandora DXL-5000", type: "Сигнализация", tr: "ble", meta: "−67 dBm", alarm: true, unsupported: true },
  { id: "rmd5", name: "Pandora RMD-5", type: "Модуль", tr: "usb", meta: "HID · 0483:5750" },
  { id: "d032", name: "Pandora D-032", type: "Метка", tr: "ble", meta: "−75 dBm" },
  { id: "vx4g", name: "Pandora VX-4G", type: "Сигнализация", tr: "ble", meta: "−83 dBm", alarm: true },
];
const TR = { ble: { icon: "bluetooth", label: "Bluetooth" }, usb: { icon: "usb", label: "USB" } };

const STEP = {
  prep: { t: "Подготовка", help: "Проверка блока, выбор автомобиля и конфигурации. Здесь можно свободно возвращаться назад — с блоком ещё ничего не делали." },
  write: { t: "Запись настроек", help: "Настройки конфигурации записываются в блок. Не отключайте блок, после записи он перезагрузится." },
  mount: { t: "Монтаж", help: "Установка блока по карте монтажа и фото конфигурации. Обычно самый долгий шаг." },
  bypass: { t: "Обход иммобилайзера", help: "Обучение бесключевому обходу. Если обходу нужен Clone, он появится следующим шагом." },
  clone: { t: "Clone", help: "Копирование данных штатного ключа. Нужен не всегда — только если его потребует обход." },
  slave: { t: "Обучение Slave", help: "Штатный брелок начнёт ставить и снимать сигнализацию с охраны." },
  check: { t: "Проверка системы", help: "Задания по порядку: двери, капот, зажигание, постановка в охрану." },
  review: { t: "Отзыв", help: "Оценка конфигурации и короткий отзыв для других монтажников." },
};
const PREP_SUB = [["block", "Блок"], ["vehicle", "Автомобиль"], ["config", "Конфигурация"], ["summary", "Сводка"]];
const PREP_OF = { search: "block", unsupported: "block", blockcheck: "block", vehicle: "vehicle", configs: "config", cfgcard: "config", summary: "summary" };
const STEP_OF = { starting: "write", write: "write", mount: "mount", bypass: "bypass", clone: "clone", slave: "slave", check: "check", review: "review", done: "done" };
const stepOf = (screen) => (PREP_OF[screen] ? "prep" : STEP_OF[screen] || "prep");
const pathOf = (conn) => ["prep", "write", "mount", "bypass", "clone", ...(conn === "usb" ? [] : ["slave", "check"]), "review"];
const nextAfter = (k, conn, cloneNeeded) => {
  const p = pathOf(conn).filter((x) => x !== "clone" || cloneNeeded === true);
  return p[p.indexOf(k) + 1] || "done";
};
const prevBefore = (k, conn, cloneNeeded) => {
  const p = pathOf(conn).filter((x) => x !== "clone" || cloneNeeded === true);
  return p[p.indexOf(k) - 1];
};

// Статусы шагов для колонки: done · current · ahead · optional (Clone ещё не нужен) · skipped · next (ждёт после перезагрузки)
function statusesFor({ conn, cur, cloneNeeded, reboot }) {
  const path = pathOf(conn);
  const target = reboot ? reboot.to : cur;
  const idx = target === "done" ? path.length : path.indexOf(target);
  const out = {};
  path.forEach((k, i) => {
    if (k === "clone" && cloneNeeded === false) out[k] = "skipped";
    else if (k === "clone" && cloneNeeded !== true && i >= idx) out[k] = "optional";
    else if (i < idx) out[k] = "done";
    else if (i === idx) out[k] = reboot ? "next" : "current";
    else out[k] = "ahead";
  });
  return out;
}
const counted = (conn, cloneNeeded) => pathOf(conn).filter((k) => k !== "clone" || cloneNeeded === true);

// ── Утилиты ─────────────────────────────────────────────────────────────────
function useSize() {
  const ref = React.useRef(null);
  const [sz, setSz] = React.useState({ w: 0, h: 0 });
  React.useEffect(() => {
    const el = ref.current; if (!el) return;
    const m = () => setSz({ w: el.offsetWidth, h: el.offsetHeight });
    const ro = new ResizeObserver(m); ro.observe(el); m();
    return () => ro.disconnect();
  }, []);
  return [ref, sz.w, sz.h];
}
const mss = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;
function useTicker(on, from = 0, key) {
  const [s, setS] = React.useState(from);
  React.useEffect(() => { setS(from); if (!on) return; const t = setInterval(() => setS((x) => x + 1), 1000); return () => clearInterval(t); }, [on, key]);
  return s;
}

// Плейсхолдер иллюстрации художника: штриховка + моноширинная подпись.
function Placeholder({ label, icon, w = 240, h = 165, radius = 24, style }) {
  return (
    <div style={{ width: w, maxWidth: "100%", height: h, borderRadius: radius, position: "relative", overflow: "hidden", flex: "0 0 auto", border: "1px solid var(--outline-variant)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 8, boxSizing: "border-box",
      background: "repeating-linear-gradient(135deg, var(--surface-container-high) 0 8px, var(--surface-container-low) 8px 16px)", ...style }}>
      {icon && <CmpIcon icon={icon} size={Math.round(Math.min(w === "100%" ? 200 : w, h) * 0.26)} color="var(--on-surface-variant)" />}
      {label && <span style={{ fontFamily: "var(--font-mono)", fontSize: 12, color: "var(--on-surface-variant)", background: "var(--surface-container-low)", padding: "2px 8px", borderRadius: 6, textAlign: "center", maxWidth: "90%" }}>{label}</span>}
    </div>
  );
}

const SVG_C = {};
function Illus({ file, w = 240 }) {
  const src = "../bypass-clone/illus/" + file + ".svg";
  const [html, setHtml] = React.useState("");
  const ref = React.useRef(null);
  React.useEffect(() => {
    let on = true;
    if (!SVG_C[src]) SVG_C[src] = fetch(src).then((r) => r.text()).then((t) => t.replace(/<\?xml[^>]*\?>/, "").replace(/<metadata[\s\S]*?<\/metadata>/, "")
      .replace(/(fill|stroke)(=")(#000000|#000|black)"/gi, '$1$2currentColor"').replace(/(fill|stroke)="#2672d1"/gi, '$1="currentColor" style="$1:var(--primary)"').replace(/(fill|stroke):\s*(#000000|#000|black)\b/gi, "$1:currentColor")).catch(() => "");
    SVG_C[src].then((t) => on && setHtml(t));
    return () => { on = false; };
  }, [src]);
  React.useLayoutEffect(() => { const svg = ref.current && ref.current.querySelector("svg"); if (svg) svg.setAttribute("viewBox", "40 24 720 495"); }, [html]);
  return <span ref={ref} className="fm-svg" aria-hidden="true" style={{ display: "block", width: w, maxWidth: "100%", height: Math.round(w / 1.45), color: "var(--on-surface)" }} dangerouslySetInnerHTML={{ __html: html }} />;
}

const TONES = {
  warn: { bg: "var(--tertiary-container)", fg: "var(--on-tertiary-container)", icon: "warning" },
  error: { bg: "var(--error-container)", fg: "var(--on-error-container)", icon: "error" },
  info: { bg: "var(--secondary-container)", fg: "var(--on-secondary-container)", icon: "info" },
  success: { bg: "color-mix(in srgb, var(--success) 20%, var(--surface-container-high))", fg: "var(--on-surface)", icon: "check_circle" },
};
// Плашка (в т.ч. «Ошибка шага» F17): причина + действие. Действие — крупная кнопка-контур.
function Banner({ tone = "warn", icon, title, text, action, onAction, busy }) {
  const t = TONES[tone];
  return (
    <div role={tone === "error" ? "alert" : undefined} className="fm-anim" style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "14px 14px 14px 16px", borderRadius: 18, background: t.bg, color: t.fg, boxSizing: "border-box", width: "100%", animation: "fmIn .25s ease both" }}>
      <span style={{ paddingTop: 1, display: "inline-flex" }}><CmpIcon icon={icon || t.icon} size={26} color="currentColor" /></span>
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 4 }}>
        {title && <CmpText variant="titleMedium" weight={700} color="currentColor" style={{ textWrap: "pretty" }}>{title}</CmpText>}
        {text && <CmpText variant="bodyLarge" color="currentColor" style={{ textWrap: "pretty" }}>{text}</CmpText>}
        {action && (
          <div style={{ paddingTop: 6 }}>
            <button type="button" className="fm-hover" onClick={onAction} disabled={busy} style={{ minHeight: 48, padding: "0 18px", borderRadius: 999, border: `1.5px solid ${t.fg}`, background: "transparent", color: t.fg, fontFamily: "var(--font-sans)", fontSize: 15, fontWeight: 600, cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 8 }}>
              {busy && <CmpCircularProgressIndicator size={18} strokeWidth={2.5} color={t.fg} />}{action}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function Card({ title, aside, children, pad = 16, style }) {
  return (
    <CmpSurface color="var(--surface-container-low)" radius={20} border style={{ boxSizing: "border-box", ...style }}>
      {title && (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, padding: "14px 16px 0" }}>
          <CmpText variant="titleMedium" weight={700}>{title}</CmpText>
          {aside}
        </div>
      )}
      <div style={{ padding: pad }}>{children}</div>
    </CmpSurface>
  );
}
function KV({ label, value, mono, strong, tone }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, minHeight: 44 }}>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{label}</CmpText>
      <span style={{ fontFamily: mono ? "var(--font-mono)" : "var(--font-sans)", fontSize: 16, fontWeight: strong ? 700 : 600, textAlign: "right", color: tone === "error" ? "var(--error)" : tone === "warn" ? "var(--tertiary)" : "var(--on-surface)" }}>{value}</span>
    </div>
  );
}
function Stars({ value, size = 20 }) {
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}><CmpIcon icon="star" size={size} color="var(--tertiary)" /><span style={{ fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 700, color: "var(--on-surface)" }}>{String(value).replace(".", ",")}</span></span>;
}
function Chip({ icon, text, tone }) {
  const t = tone ? TONES[tone] : { bg: "var(--surface-container-highest)", fg: "var(--on-surface)" };
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, minHeight: 32, padding: "0 12px", borderRadius: 10, background: t.bg, color: t.fg, fontFamily: "var(--font-sans)", fontSize: 14, fontWeight: 600, whiteSpace: "nowrap" }}>{icon && <CmpIcon icon={icon} size={18} color="currentColor" />}{text}</span>;
}

// Главная кнопка. busy — состояние ожидания ответа: спиннер, повторный тап невозможен.
function MainBtn({ text, icon, onClick, disabled, busy, busyText, outlined }) {
  if (busy) return (
    <CmpButton disabled fullWidth style={BIG}>
      <span style={{ display: "inline-flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={20} strokeWidth={2.5} color="currentColor" />{busyText || text}</span>
    </CmpButton>
  );
  if (outlined) return <CmpOutlinedButton text={text} icon={icon} fullWidth disabled={disabled} onClick={onClick} style={BIG} />;
  return <CmpButton text={text} icon={icon} fullWidth disabled={disabled} onClick={onClick} style={BIG} />;
}
function Dock({ children, wide, note }) {
  return (
    <div style={{ padding: "12px 16px 16px", background: "var(--surface-container)", borderTop: "1px solid var(--outline-variant)", flex: "0 0 auto" }}>
      <div style={{ maxWidth: wide ? 1040 : "none", margin: "0 auto", display: "flex", flexDirection: wide ? "row-reverse" : "column", alignItems: wide ? "center" : "stretch", gap: 8 }}>
        {React.Children.toArray(children).filter(Boolean).map((c, i) => <div key={i} style={{ flex: wide ? "0 0 300px" : "none" }}>{c}</div>)}
        {note && <div style={{ flex: 1, minWidth: 0, textAlign: wide ? "left" : "center" }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{note}</CmpText></div>}
      </div>
    </div>
  );
}

// Центрированный «статусный» экран: иллюстрация, заголовок, текст, доп. блоки.
function Hero({ art, title, text, children }) {
  return (
    <div className="fm-anim" style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: 14, padding: "28px 20px 24px", maxWidth: 560, margin: "0 auto", width: "100%", boxSizing: "border-box", animation: "fmIn .3s ease both" }}>
      {art}
      <CmpText variant="headlineSmall" style={{ textWrap: "balance" }}>{title}</CmpText>
      {text && <CmpText variant="bodyLarge" color="var(--on-surface-variant)" style={{ textWrap: "pretty" }}>{text}</CmpText>}
      {children}
    </div>
  );
}
function BusyArt({ icon, size = 132, spin }) {
  return (
    <div style={{ position: "relative", width: size, height: size, display: "grid", placeItems: "center" }}>
      <span className="fm-anim" style={{ position: "absolute", inset: 0, borderRadius: 999, background: "var(--primary-container)", animation: "fmRing 1.8s ease-out infinite" }} />
      <span style={{ position: "absolute", inset: 0, borderRadius: 999, background: "var(--primary-container)" }} />
      <span style={{ position: "absolute", inset: -6 }}><CmpCircularProgressIndicator size={size + 12} strokeWidth={4} /></span>
      <span className="fm-anim" style={{ position: "relative", display: "inline-flex", animation: spin ? "fmSpin 2.4s linear infinite" : "none" }}><CmpIcon icon={icon} size={Math.round(size * 0.42)} color="var(--on-primary-container)" /></span>
    </div>
  );
}
function IndetBar() {
  return <div style={{ position: "relative", height: 8, borderRadius: 4, overflow: "hidden", background: "color-mix(in srgb, var(--primary) 18%, transparent)", width: "100%", maxWidth: 360 }}><span className="fm-anim" style={{ position: "absolute", top: 0, bottom: 0, borderRadius: 4, background: "var(--primary)", animation: "fmIndet 1.4s ease-in-out infinite" }} /></div>;
}

// ── Диалог ───────────────────────────────────────────────────────────────────
function Modal({ icon, tone = "primary", title, text, children, actions, onDismiss }) {
  const ib = tone === "error" ? "var(--error-container)" : tone === "warn" ? "var(--tertiary-container)" : "var(--primary-container)";
  const ifg = tone === "error" ? "var(--on-error-container)" : tone === "warn" ? "var(--on-tertiary-container)" : "var(--on-primary-container)";
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 20, display: "grid", placeItems: "center", padding: 20 }}>
      <div onClick={onDismiss} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim) 42%, transparent)" }} />
      <div role="dialog" aria-modal="true" className="fm-anim" style={{ position: "relative", width: "100%", maxWidth: 420, boxSizing: "border-box", background: "var(--surface-container-high)", borderRadius: 28, padding: 24, display: "flex", flexDirection: "column", gap: 16, animation: "fmUp .22s ease both" }}>
        {icon && <div style={{ alignSelf: "center", width: 56, height: 56, borderRadius: 999, background: ib, color: ifg, display: "grid", placeItems: "center" }}><CmpIcon icon={icon} size={30} /></div>}
        <CmpText variant="headlineSmall" align="center" style={{ textWrap: "balance" }}>{title}</CmpText>
        {text && <CmpText variant="bodyLarge" color="var(--on-surface)" align="center" style={{ textWrap: "pretty" }}>{text}</CmpText>}
        {children}
        <div style={{ display: "flex", flexDirection: "column", gap: 8, marginTop: 4 }}>{actions}</div>
      </div>
    </div>
  );
}
const DIALOG = {
  exit: { icon: "logout", tone: "warn", title: "Выйти из быстрого монтажа?", text: "Монтаж не будет завершён. Настройки, записанные в блок, сохранятся.", ok: "Выйти на главную", cancel: "Продолжить монтаж" },
  toMount: { icon: "undo", tone: "warn", title: "Вернуться к монтажу?", text: "Обход придётся пройти заново.", ok: "Вернуться к монтажу", cancel: "Остаться" },
  toMountClone: { icon: "undo", tone: "warn", title: "Вернуться к монтажу?", text: "Обход и Clone придётся пройти заново.", ok: "Вернуться к монтажу", cancel: "Остаться" },
  prev: { icon: "undo", title: "Вернуться к предыдущему шагу?", text: "Проверка системы будет прервана.", ok: "Вернуться", cancel: "Отмена" },
  mountDone: { icon: "build_circle", title: "Монтаж завершён?", text: "Блок будет перезагружен, и вы перейдёте к следующему шагу.", ok: "Да, монтаж завершён", cancel: "Отмена", primaryOk: true },
  slaveDone: { icon: "settings_remote", title: "Обучение Slave завершено?", text: "Вы действительно завершили обучение Slave-режиму и готовы продолжить монтаж?", ok: "Продолжить монтаж", cancel: "Отмена", primaryOk: true },
};
function StdDialog({ k, onOk, onCancel }) {
  const d = DIALOG[k];
  // Безопасное действие — заливкой; уход с шага — контуром.
  return (
    <Modal icon={d.icon} tone={d.tone} title={d.title} text={d.text} onDismiss={onCancel}
      actions={d.primaryOk ? <><CmpButton text={d.ok} fullWidth style={BIG} onClick={onOk} /><TextBtn text={d.cancel} style={BIGW} onClick={onCancel} /></>
        : <><CmpButton text={d.cancel} fullWidth style={BIG} onClick={onCancel} /><CmpOutlinedButton text={d.ok} fullWidth style={BIG} onClick={onOk} /></>} />
  );
}
function PinDialog({ onOk, onCancel }) {
  const [v, setV] = React.useState("");
  return (
    <Modal icon="lock" title="Введите PIN-код программирования" text="PIN нужен, чтобы записать настройки в блок." onDismiss={onCancel}
      actions={<><CmpButton text="Продолжить" fullWidth disabled={v.length < 4} style={BIG} onClick={onOk} /><TextBtn text="Отмена" style={BIGW} onClick={onCancel} /></>}>
      <input value={v} onChange={(e) => setV(e.target.value.replace(/\D/g, "").slice(0, 6))} inputMode="numeric" placeholder="••••" aria-label="PIN-код"
        style={{ height: 60, borderRadius: 12, border: "2px solid var(--primary)", background: "var(--surface)", color: "var(--on-surface)", fontFamily: "var(--font-mono)", fontSize: 26, letterSpacing: ".4em", textAlign: "center", outline: "none" }} />
    </Modal>
  );
}

// ── Оверлеи: потеря связи (существующий экран) и «Подключён другой блок» ──────
function FullOverlay({ icon, tone = "primary", title, text, children, onBack }) {
  const ib = tone === "error" ? "var(--error-container)" : "var(--primary-container)";
  const ifg = tone === "error" ? "var(--on-error-container)" : "var(--on-primary-container)";
  return (
    <div className="fm-anim" style={{ position: "absolute", inset: 0, zIndex: 30, background: "var(--background)", display: "flex", flexDirection: "column", animation: "fmIn .2s ease both" }}>
      <CmpTopAppBar title="" navigationIcon={onBack ? <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} /> : null} />
      <div style={{ flex: 1, overflow: "auto", display: "flex", alignItems: "center", justifyContent: "center", padding: 24 }}>
        <div style={{ maxWidth: 460, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 16, textAlign: "center" }}>
          <div style={{ width: 96, height: 96, borderRadius: 999, background: ib, color: ifg, display: "grid", placeItems: "center" }}><CmpIcon icon={icon} size={48} /></div>
          <CmpText variant="headlineSmall" style={{ textWrap: "balance" }}>{title}</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)" style={{ textWrap: "pretty" }}>{text}</CmpText>
          {children}
        </div>
      </div>
    </div>
  );
}
function LostOverlay({ onExit }) {
  return (
    <FullOverlay icon="bluetooth_disabled" title="Отсутствует соединение с устройством" text="Блок выключен или находится далеко. Поднесите телефон ближе и проверьте питание блока." onBack={onExit}>
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={18} strokeWidth={2.5} /><CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём возвращения блока…</CmpText></div>
      <div style={{ width: "100%" }}><CmpOutlinedButton text="Отключиться" icon="link_off" fullWidth onClick={onExit} style={BIG} /></div>
    </FullOverlay>
  );
}
function OtherBlockOverlay({ onExit }) {
  const row = (lbl, id, ok) => (
    <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "8px 14px", borderRadius: 16, background: ok ? "var(--surface-container-high)" : "var(--error-container)", textAlign: "left" }}>
      <CmpIcon icon={ok ? "check_circle" : "cancel"} size={26} color={ok ? "var(--primary)" : "var(--on-error-container)"} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="bodyMedium" color={ok ? "var(--on-surface-variant)" : "var(--on-error-container)"} style={{ display: "block" }}>{lbl}</CmpText>
        <CmpText variant="titleMedium" weight={700} color={ok ? "var(--on-surface)" : "var(--on-error-container)"}>{DEV.name} · ID {id}</CmpText>
      </div>
    </div>
  );
  return (
    <FullOverlay icon="sync_problem" tone="error" title="Подключён другой блок" text={`Подключён блок ID ${OTHER_ID}. Монтаж идёт с блоком ID ${DEV.id} — подключите его.`}>
      <div style={{ width: "100%", display: "flex", flexDirection: "column", gap: 8 }}>{row("Подключён сейчас", OTHER_ID, false)}{row("Монтаж идёт с блоком", DEV.id, true)}</div>
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={18} strokeWidth={2.5} /><CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём блок ID {DEV.id}…</CmpText></div>
      <div style={{ width: "100%" }}><CmpOutlinedButton text="Отключиться" icon="link_off" fullWidth onClick={onExit} style={BIG} /></div>
    </FullOverlay>
  );
}

// ── W1 · Колонка шагов (вариант B) ───────────────────────────────────────────
function Marker({ st, n }) {
  const base = { width: 36, height: 36, borderRadius: 999, boxSizing: "border-box", display: "grid", placeItems: "center", flex: "0 0 auto", fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 700, position: "relative", zIndex: 1, transition: "all .4s" };
  if (st === "done") return <span style={{ ...base, background: "var(--primary)", color: "var(--on-primary)" }}><CmpIcon icon="check" size={22} color="currentColor" /></span>;
  if (st === "current") return <span style={{ ...base, background: "var(--primary)", color: "var(--on-primary)", boxShadow: "0 0 0 4px color-mix(in srgb, var(--primary) 25%, transparent)" }}>{n}</span>;
  if (st === "next") return <span className="fm-anim" style={{ ...base, background: "var(--background)", border: "2px dashed var(--primary)", color: "var(--primary)", animation: "fmPulse 1.6s ease-in-out infinite" }}>{n}</span>;
  if (st === "optional") return <span style={{ ...base, width: 28, height: 28, margin: "0 4px", background: "var(--background)", border: "2px dashed var(--outline)", color: "var(--on-surface-variant)" }}><CmpIcon icon="add" size={18} color="currentColor" /></span>;
  if (st === "skipped") return <span style={{ ...base, width: 28, height: 28, margin: "0 4px", background: "var(--surface-container-high)", border: "2px dashed var(--outline-variant)", color: "var(--on-surface-variant)" }}><CmpIcon icon="remove" size={18} color="currentColor" /></span>;
  return <span style={{ ...base, background: "var(--background)", border: "2px solid var(--outline)", color: "var(--on-surface-variant)" }}>{n}</span>;
}
const SUBTEXT = { done: "Готово", current: "Сейчас", next: "Следующий", optional: "Если потребуется обходу", skipped: "Не потребовался" };

function StepRail({ conn = "ble", cur = "prep", prepSub = "block", cloneNeeded = null, reboot = null, rebootSecs = 0, cfgChosen, width = 300, compact = false, dense = false, initialHelp = null }) {
  const path = pathOf(conn);
  const sts = statusesFor({ conn, cur, cloneNeeded, reboot });
  const nums = {}; counted(conn, cloneNeeded).forEach((k, i) => (nums[k] = i + 1));
  const total = counted(conn, cloneNeeded).length;
  const curNum = cur === "done" ? total : nums[reboot ? reboot.to : cur];
  const [help, setHelp] = React.useState(initialHelp ? { k: initialHelp, top: 120 } : null);
  const navRef = React.useRef(null);
  // Появление Clone: переход optional → обычный шаг проигрывает анимацию.
  const prevClone = React.useRef(cloneNeeded);
  const [cloneAnim, setCloneAnim] = React.useState(0);
  React.useEffect(() => { if (prevClone.current !== true && cloneNeeded === true) setCloneAnim((x) => x + 1); prevClone.current = cloneNeeded; }, [cloneNeeded]);
  const toggle = (k, e) => {
    if (help && help.k === k) return setHelp(null);
    const nr = navRef.current.getBoundingClientRect(), r = e.currentTarget.getBoundingClientRect();
    setHelp({ k, top: (r.top - nr.top) / (nr.height / navRef.current.offsetHeight) });
  };
  const lineColor = (b) => (sts[b] === "done" || sts[b] === "current" ? "var(--primary)" : "var(--outline-variant)");
  const gapH = dense ? 6 : 10;
  return (
    <nav ref={navRef} aria-label="Шаги быстрого монтажа" style={{ position: "relative", width: compact ? 88 : width, flex: "0 0 auto", boxSizing: "border-box", height: "100%", display: "flex", flexDirection: "column", background: "var(--surface-container-low)", borderRight: "1px solid var(--outline-variant)", zIndex: 3 }}>
      {compact ? (
        <div style={{ padding: "16px 0 10px", textAlign: "center" }}>
          <CmpText variant="titleMedium" weight={700} style={{ display: "block" }}>{cur === "done" ? "✓" : `${curNum}/${total}`}</CmpText>
          <CmpText variant="labelMedium" color="var(--on-surface-variant)">шаг</CmpText>
        </div>
      ) : (
        <div style={{ padding: dense ? "14px 20px 8px" : "18px 20px 12px", display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 8 }}>
          <CmpText variant="titleLarge" weight={700}>Быстрый монтаж</CmpText>
          <CmpText variant="titleMedium" weight={600} color="var(--on-surface-variant)" style={{ whiteSpace: "nowrap" }}>{cur === "done" ? "Готово" : `${curNum} из ${total}`}</CmpText>
        </div>
      )}
      <div style={{ flex: 1, minHeight: 0, overflow: "auto", padding: compact ? "4px 8px 12px" : "2px 12px 12px" }}>
        {path.map((k, i) => {
          const st = sts[k];
          const muted = st === "optional" || st === "skipped";
          const isCur = st === "current";
          const showSub = (st === "optional" || st === "skipped" || st === "next") && !compact;
          const animClone = k === "clone" && cloneAnim && !muted;
          const row = (
            <div key={k + (k === "clone" ? cloneAnim : "")} className="fm-anim" style={{ position: "relative", overflow: "hidden", borderRadius: 16, animation: animClone ? "fmGrow .6s ease both, fmFlash 1.6s ease-out both" : "none" }}>
              <div role="button" tabIndex={0} aria-current={isCur ? "step" : undefined} aria-label={STEP[k].t} title={compact ? STEP[k].t : undefined} className="fm-hover" onClick={(e) => toggle(k, e)} onKeyDown={(e) => { if (e.key === "Enter") toggle(k, e); }}
                style={{ display: "flex", alignItems: compact ? "center" : "flex-start", justifyContent: compact ? "center" : "flex-start", gap: 12, minHeight: 48, padding: compact ? "6px 0" : "6px 10px", borderRadius: 16, cursor: "pointer", background: isCur ? "var(--secondary-container)" : "transparent", boxSizing: "border-box" }}>
                <span style={{ width: 36, display: "flex", justifyContent: "center", flex: "0 0 auto", paddingTop: muted ? 4 : 0 }}><Marker st={st} n={nums[k]} /></span>
                {!compact && (
                  <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", paddingTop: 6 }}>
                    <CmpText variant="titleMedium" weight={isCur ? 700 : 500} color={isCur ? "var(--on-secondary-container)" : muted || st === "ahead" ? "var(--on-surface-variant)" : "var(--on-surface)"} style={{ lineHeight: 1.3 }}>{STEP[k].t}</CmpText>
                    {showSub && <CmpText variant="bodyMedium" weight={st === "next" ? 600 : 400} color={st === "next" ? "var(--primary)" : "var(--on-surface-variant)"}>{SUBTEXT[st]}</CmpText>}
                    {k === "prep" && isCur && (
                      <span style={{ display: "flex", flexDirection: "column", paddingTop: 4, paddingBottom: 2 }}>
                        {PREP_SUB.map(([sk, sl], si) => {
                          const ci = PREP_SUB.findIndex((x) => x[0] === prepSub);
                          const d = si < ci, c = si === ci;
                          return (
                            <span key={sk} style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 28 }}>
                              <span style={{ width: 20, display: "inline-flex", justifyContent: "center" }}><CmpIcon icon={d ? "check" : c ? "arrow_right" : "radio_button_unchecked"} size={d || c ? 20 : 14} color={d || c ? "var(--on-secondary-container)" : "var(--on-surface-variant)"} /></span>
                              <CmpText variant="bodyLarge" weight={c ? 700 : 400} color={c || d ? "var(--on-secondary-container)" : "var(--on-surface-variant)"} style={{ fontSize: 15 }}>{sl}</CmpText>
                            </span>
                          );
                        })}
                      </span>
                    )}
                  </span>
                )}
              </div>
            </div>
          );
          if (i === path.length - 1) return row;
          const nk = path[i + 1];
          const dotted = nk === "clone" ? sts.clone === "optional" || sts.clone === "skipped" : k === "clone" && (sts.clone === "optional" || sts.clone === "skipped");
          // Перезагрузка — «бегущая» линия под завершённым шагом (в компактной колонке — без подписи, таймер в подсказке).
          const gap = reboot && reboot.from === k ? (
            <div key={k + "-g"} title={`Перезагрузка · ${mss(rebootSecs)}`} style={{ display: "flex", alignItems: "center", justifyContent: compact ? "center" : "flex-start", gap: 12, minHeight: compact ? 40 : 48, padding: compact ? 0 : "0 10px" }}>
              <span style={{ width: 36, alignSelf: "stretch", display: "flex", justifyContent: "center" }}>
                <span className="fm-anim" style={{ width: 4, borderRadius: 2, background: "repeating-linear-gradient(180deg, var(--primary) 0 12px, color-mix(in srgb, var(--primary) 20%, transparent) 12px 24px)", animation: "fmRun .6s linear infinite" }} />
              </span>
              {!compact && (
                <span style={{ display: "flex", alignItems: "center", gap: 8, padding: "4px 12px", borderRadius: 12, background: "var(--primary-container)", color: "var(--on-primary-container)" }}>
                  <span className="fm-anim" style={{ display: "inline-flex", animation: "fmSpin 2s linear infinite" }}><CmpIcon icon="autorenew" size={20} color="currentColor" /></span>
                  <CmpText variant="labelLarge" weight={700} color="currentColor" style={{ fontSize: 15 }}>Перезагрузка · {mss(rebootSecs)}</CmpText>
                </span>
              )}
            </div>
          ) : (
            <div key={k + "-g"} style={{ height: gapH, padding: compact ? 0 : "0 10px", display: "flex", justifyContent: compact ? "center" : "flex-start" }}>
              <span style={{ width: 36, display: "flex", justifyContent: "center" }}>
                <span style={{ width: 3, borderRadius: 2, background: dotted ? "transparent" : lineColor(nk), borderLeft: dotted ? "3px dotted var(--outline-variant)" : "none", transition: "background .4s" }} />
              </span>
            </div>
          );
          return <React.Fragment key={k}>{row}{gap}</React.Fragment>;
        })}
      </div>
      {cfgChosen && !compact && !dense && (
        <div style={{ margin: "0 12px 12px", padding: "10px 14px", borderRadius: 16, background: "var(--surface-container-high)", display: "flex", alignItems: "center", gap: 10 }}>
          <CmpIcon icon={TR[conn].icon} size={22} color="var(--on-surface-variant)" />
          <div style={{ flex: 1, minWidth: 0 }}>
            <CmpText variant="titleSmall" weight={600} style={{ fontSize: 15, display: "block" }} maxLines={1}>{CAR.brand} {CAR.model} {CAR.year}</CmpText>
            <CmpText variant="bodyMedium" color="var(--on-surface-variant)" maxLines={1}>{CFG.name}</CmpText>
          </div>
        </div>
      )}
      {help && (
        <div className="fm-anim" role="tooltip" style={{ position: "absolute", left: compact ? 80 : width - 16, top: Math.max(8, help.top), zIndex: 15, width: 280, boxSizing: "border-box", padding: "12px 14px 14px", borderRadius: 16, background: "var(--inverse-surface)", color: "var(--inverse-on-surface)", boxShadow: "var(--elevation-3)", animation: "fmIn .18s ease both" }}>
          <div style={{ display: "flex", alignItems: "flex-start", gap: 8 }}>
            <CmpText variant="titleMedium" weight={700} color="var(--inverse-on-surface)" style={{ flex: 1 }}>{STEP[help.k].t}{SUBTEXT[sts[help.k]] ? ` · ${SUBTEXT[sts[help.k]].toLowerCase()}` : ""}</CmpText>
            <button type="button" aria-label="Закрыть" onClick={() => setHelp(null)} style={{ width: 48, height: 48, margin: "-12px -12px -8px 0", border: "none", background: "transparent", display: "grid", placeItems: "center", cursor: "pointer" }}><CmpIcon icon="close" size={20} color="var(--inverse-on-surface)" /></button>
          </div>
          <CmpText variant="bodyMedium" color="var(--inverse-on-surface)" style={{ display: "block", paddingTop: 4, textWrap: "pretty" }}>{STEP[help.k].help}</CmpText>
        </div>
      )}
    </nav>
  );
}

// ── Строки списков подготовки ────────────────────────────────────────────────
function ListRow({ icon, title, sub, right, onClick, disabled, chip, busy }) {
  return (
    <div role="button" tabIndex={disabled ? -1 : 0} aria-disabled={disabled} className={disabled ? undefined : "fm-hover"} onClick={disabled ? undefined : onClick} onKeyDown={(e) => { if (!disabled && e.key === "Enter") onClick && onClick(); }}
      style={{ display: "flex", alignItems: "center", gap: 14, minHeight: 76, padding: "10px 12px 10px 10px", borderRadius: 18, cursor: disabled ? "default" : "pointer", background: disabled ? "transparent" : "var(--surface-container-low)", border: disabled ? "1.5px dashed var(--outline-variant)" : "1px solid var(--outline-variant)", boxSizing: "border-box" }}>
      <span style={{ width: 52, height: 52, borderRadius: 14, display: "grid", placeItems: "center", flex: "0 0 auto", background: disabled ? "var(--surface-container)" : "var(--secondary-container)", color: disabled ? "var(--on-surface-variant)" : "var(--on-secondary-container)" }}><CmpIcon icon={icon} size={28} color="currentColor" /></span>
      <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2, alignItems: "flex-start" }}>
        <CmpText variant="titleMedium" weight={600} color={disabled ? "var(--on-surface-variant)" : "var(--on-surface)"}>{title}</CmpText>
        {sub && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{sub}</CmpText>}
        {chip && <span style={{ paddingTop: 4 }}>{chip}</span>}
      </span>
      {busy ? <CmpCircularProgressIndicator size={28} strokeWidth={3} /> : right !== undefined ? right : !disabled && <CmpIcon icon="chevron_right" size={28} color="var(--on-surface-variant)" />}
    </div>
  );
}

// ── Карта монтажа (PDF) ─────────────────────────────────────────────────────
function PdfCard({ state, onOpen, onRetry }) {
  const err = state === "error", dl = state === "downloading";
  return (
    <div role="button" tabIndex={state === "ready" ? 0 : -1} className={state === "ready" ? "fm-hover" : undefined} onClick={state === "ready" ? onOpen : undefined}
      style={{ display: "flex", alignItems: "center", gap: 14, minHeight: 92, padding: "12px 14px 12px 12px", borderRadius: 20, boxSizing: "border-box", cursor: state === "ready" ? "pointer" : "default", background: err ? "var(--error-container)" : "var(--surface-container-low)", border: err ? "1px solid transparent" : "1px solid var(--outline-variant)" }}>
      <span style={{ position: "relative", width: 56, height: 68, borderRadius: 10, flex: "0 0 auto", display: "grid", placeItems: "center", background: err ? "color-mix(in srgb, var(--on-error-container) 12%, transparent)" : "var(--primary-container)", color: err ? "var(--on-error-container)" : "var(--on-primary-container)" }}>
        {dl ? <CmpCircularProgressIndicator size={40} strokeWidth={4} progress={0.36} /> : <CmpIcon icon={err ? "error" : "picture_as_pdf"} size={32} color="currentColor" />}
      </span>
      <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <CmpText variant="titleMedium" weight={700} color={err ? "var(--on-error-container)" : "var(--on-surface)"}>{err ? "Карта монтажа не скачана" : "Карта монтажа (PDF)"}</CmpText>
        <CmpText variant="bodyMedium" color={err ? "var(--on-error-container)" : "var(--on-surface-variant)"}>{dl ? "Скачиваем… 1,2 из 3,4 МБ" : err ? "Нет связи с сервером" : "3,4 МБ · откроется в приложении для PDF"}</CmpText>
      </span>
      {state === "ready" && <CmpIcon icon="open_in_new" size={28} color="var(--primary)" />}
      {err && <button type="button" className="fm-hover" onClick={onRetry} style={{ flex: "0 0 auto", minHeight: 48, padding: "0 16px", borderRadius: 999, border: "1.5px solid var(--on-error-container)", background: "transparent", color: "var(--on-error-container)", fontFamily: "var(--font-sans)", fontSize: 15, fontWeight: 600, cursor: "pointer" }}>Повторить</button>}
    </div>
  );
}
// Широкое окно: крупное превью карты монтажа с кнопкой открытия (те же состояния, что у PdfCard).
function PdfPreview({ state, onOpen, onRetry }) {
  const err = state === "error", dl = state === "downloading";
  return (
    <CmpSurface color="var(--surface-container-low)" radius={24} border style={{ overflow: "hidden", boxSizing: "border-box" }}>
      <div style={{ position: "relative" }}>
        <Placeholder w="100%" h={260} radius={0} label="превью первой страницы карты монтажа" icon="schema" style={{ border: "none", borderBottom: "1px solid var(--outline-variant)", opacity: dl || err ? 0.45 : 1 }} />
        {dl && <div style={{ position: "absolute", inset: 0, display: "grid", placeItems: "center" }}><div style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 18px", borderRadius: 16, background: "var(--surface-container-high)", boxShadow: "var(--elevation-2)" }}><CmpCircularProgressIndicator size={32} strokeWidth={4} progress={0.36} /><CmpText variant="titleMedium" weight={600}>Скачиваем… 1,2 из 3,4 МБ</CmpText></div></div>}
      </div>
      <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: 14, padding: "14px 16px", background: err ? "var(--error-container)" : "transparent" }}>
        <span style={{ width: 48, height: 48, borderRadius: 12, flex: "0 0 auto", display: "grid", placeItems: "center", background: err ? "color-mix(in srgb, var(--on-error-container) 12%, transparent)" : "var(--primary-container)", color: err ? "var(--on-error-container)" : "var(--on-primary-container)" }}><CmpIcon icon={err ? "error" : "picture_as_pdf"} size={28} color="currentColor" /></span>
        <span style={{ flex: "1 1 200px", minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
          <CmpText variant="titleMedium" weight={700} color={err ? "var(--on-error-container)" : "var(--on-surface)"}>{err ? "Карта монтажа не скачана" : "Карта монтажа (PDF)"}</CmpText>
          <CmpText variant="bodyMedium" color={err ? "var(--on-error-container)" : "var(--on-surface-variant)"}>{err ? "Нет связи с сервером" : dl ? "Откроется, когда скачается" : "3,4 МБ · 6 страниц"}</CmpText>
        </span>
        <span style={{ flex: "1 1 220px", maxWidth: 320 }}>
          {err ? <CmpOutlinedButton text="Повторить" icon="refresh" fullWidth onClick={onRetry} style={BIG} /> : dl ? <MainBtn busy text="Открыть карту" busyText="Скачиваем…" /> : <CmpButton text="Открыть карту" icon="open_in_new" fullWidth onClick={onOpen} style={BIG} />}
        </span>
      </div>
    </CmpSurface>
  );
}
function PhotoMosaic({ onOpen }) {
  const tile = (i, h) => (
    <div key={i} role="button" tabIndex={0} className="fm-focus" onClick={() => onOpen(i)} onKeyDown={(e) => { if (e.key === "Enter") onOpen(i); }} style={{ display: "flex", flexDirection: "column", gap: 6, cursor: "pointer", minWidth: 0 }}>
      <Placeholder w="100%" h={h} radius={16} label={`фото ${i + 1}`} icon={i === 0 ? "photo_camera" : undefined} />
      <CmpText variant="bodyMedium" color="var(--on-surface)" style={{ textWrap: "pretty" }}>{PHOTOS[i]}</CmpText>
    </div>
  );
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      {tile(0, 220)}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(2, minmax(0, 1fr))", gap: 12 }}>{PHOTOS.slice(1).map((_, j) => tile(j + 1, 116))}</div>
    </div>
  );
}
function PhotoGrid({ cols = 2, onOpen, h = 120 }) {
  return (
    <div style={{ display: "grid", gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`, gap: 10 }}>
      {PHOTOS.map((p, i) => (
        <div key={i} role="button" tabIndex={0} className="fm-focus" onClick={() => onOpen(i)} onKeyDown={(e) => { if (e.key === "Enter") onOpen(i); }} style={{ display: "flex", flexDirection: "column", gap: 6, cursor: "pointer", minWidth: 0 }}>
          <Placeholder w="100%" h={h} radius={16} label={`фото ${i + 1}`} />
          <CmpText variant="bodyMedium" color="var(--on-surface)" style={{ textWrap: "pretty" }}>{p}</CmpText>
        </div>
      ))}
    </div>
  );
}
function PhotoViewer({ index, onClose, onNav }) {
  const navBtn = (icon, dir, label) => (
    <button type="button" aria-label={label} onClick={() => onNav(dir)} disabled={(dir < 0 && index === 0) || (dir > 0 && index === PHOTOS.length - 1)}
      style={{ width: 56, height: 56, borderRadius: 999, border: "none", background: "rgba(255,255,255,.16)", color: "#fff", display: "grid", placeItems: "center", cursor: "pointer", opacity: (dir < 0 && index === 0) || (dir > 0 && index === PHOTOS.length - 1) ? 0.35 : 1 }}>
      <CmpIcon icon={icon} size={32} color="#fff" />
    </button>
  );
  return (
    <div className="fm-anim" style={{ position: "absolute", inset: 0, zIndex: 25, background: "#0c0e0f", display: "flex", flexDirection: "column", animation: "fmIn .2s ease both" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 8px 8px 4px", minHeight: 64 }}>
        <button type="button" aria-label="Закрыть" onClick={onClose} style={{ width: 56, height: 56, border: "none", background: "transparent", display: "grid", placeItems: "center", cursor: "pointer" }}><CmpIcon icon="close" size={28} color="#fff" /></button>
        <span style={{ flex: 1, fontFamily: "var(--font-sans)", fontSize: 18, fontWeight: 600, color: "#fff" }}>Особенности монтажа</span>
        <span style={{ fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 600, color: "#fff", padding: "0 12px" }}>{index + 1} / {PHOTOS.length}</span>
      </div>
      <div style={{ flex: 1, minHeight: 0, display: "grid", placeItems: "center", padding: 12 }} data-theme="dark">
        <Placeholder w="100%" h="100%" radius={8} label={`фото ${index + 1} · на весь экран, листание свайпом`} style={{ maxWidth: 960, maxHeight: 640, background: "repeating-linear-gradient(135deg, #1c2022 0 8px, #15181a 8px 16px)", borderColor: "#2a2f32" }} />
      </div>
      <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 16px 20px" }}>
        {navBtn("chevron_left", -1, "Предыдущее фото")}
        <span style={{ flex: 1, textAlign: "center", fontFamily: "var(--font-sans)", fontSize: 16, color: "#fff", textWrap: "pretty" }}>{PHOTOS[index]}</span>
        {navBtn("chevron_right", 1, "Следующее фото")}
      </div>
    </div>
  );
}

// ── Пресеты состояний (доска и демо-панель) ──────────────────────────────────
const PRESETS = {
  F1: { screen: "search" }, F1c: { screen: "search", v: "connecting" },
  F2: { screen: "unsupported" },
  F3a: { screen: "blockcheck", v: "ok" }, F3b: { screen: "blockcheck", v: "outdated" }, F3c: { screen: "blockcheck", v: "factory" }, F3d: { screen: "blockcheck", v: "noaccess" },
  FV: { screen: "vehicle" }, FC: { screen: "configs" },
  F4a: { screen: "cfgcard", v: "ok" }, F4b: { screen: "cfgcard", v: "mismatch" },
  F5a: { screen: "summary" }, F5b: { screen: "summary", conn: "usb" },
  F6a: { screen: "starting", v: "run" }, F6b: { screen: "starting", v: "error" },
  F7a: { screen: "write", v: "run" }, F7b: { screen: "write", v: "error" }, F7c: { screen: "write", v: "run", dialog: "pin" },
  F8a: { screen: "mount", pdf: "downloading" }, F8b: { screen: "mount", pdf: "ready", toast: "Настройки записаны" }, F8c: { screen: "mount", pdf: "error" }, F8d: { screen: "mount", pdf: "ready", photo: 1 },
  F9: { screen: "reboot", reboot: { from: "mount", to: "bypass" }, t: 12 }, F9b: { screen: "reboot", reboot: { from: "mount", to: "bypass" }, t: 54 }, F9c: { screen: "reboot", reboot: { from: "bypass", to: "clone" }, cloneNeeded: true, t: 7 },
  F10s: { screen: "bypass", v: "start" }, F10r: { screen: "bypass", v: "run" },
  F10a: { screen: "bypass", v: "ok", cloneNeeded: false }, F10b: { screen: "bypass", v: "clone", cloneNeeded: true }, F10c: { screen: "bypass", v: "error" },
  F10d: { screen: "clone", v: "ok", cloneNeeded: true },
  F11: { screen: "slave", cloneNeeded: false }, F11b: { screen: "slave", cloneNeeded: false, slaveOk: true, dialog: "slaveDone" },
  F12: { screen: "check", cloneNeeded: false },
  F13a: { screen: "review", cloneNeeded: false }, F13b: { screen: "review", cloneNeeded: false, stars: 5, text: "Всё по карте, обход встал с первого раза. Фото разъёма CAN очень помогло." }, F13c: { screen: "review", cloneNeeded: false, stars: 5, text: "Всё по карте, обход встал с первого раза.", busy: true },
  F14: { screen: "done", cloneNeeded: false },
  F15: { screen: "mount", pdf: "ready", overlay: "other" }, FL: { screen: "mount", pdf: "ready", overlay: "lost" },
  F16a: { screen: "mount", pdf: "ready", dialog: "exit" }, F16b: { screen: "bypass", v: "ok", cloneNeeded: false, dialog: "toMount" }, F16c: { screen: "check", cloneNeeded: false, dialog: "prev" }, F16d: { screen: "mount", pdf: "ready", dialog: "mountDone" },
  F17: { screen: "review", cloneNeeded: false, stars: 4, text: "Обход встал со второго раза.", err: "send" },
};
const SCREEN_BEFORE_CFG = ["search", "unsupported", "blockcheck", "vehicle", "configs", "cfgcard"];

// ── Приложение ───────────────────────────────────────────────────────────────
function FastMountApp({ preset = "F1", conn: connProp = "ble", bypassResult = "clone", server = "ok", frozen = false, forceLayout, onHome = () => window.psGo && window.psGo("home") }) {
  const P = PRESETS[preset] || PRESETS.F1;
  const [rootRef, width, height] = useSize();
  const wide = forceLayout ? forceLayout === "wide" : width >= 760;
  // Колонка шагов: ≥1100 — 300, 900–1099 — 264, 760–899 — компактная 88 (только номера, подсказка по тапу).
  const railCompact = wide && width < 900;
  const railW = !wide ? 0 : railCompact ? 88 : width >= 1100 ? 300 : 264;
  const paneW = width - railW;
  const two = paneW >= 860;
  const conn = P.conn || connProp;
  const [screen, setScreen] = React.useState(P.screen);
  const [v, setV] = React.useState(P.v || null);
  const [cloneNeeded, setCloneNeeded] = React.useState(P.cloneNeeded === undefined ? null : P.cloneNeeded);
  const [reboot, setReboot] = React.useState(P.reboot || null);
  const [dialog, setDialog] = React.useState(P.dialog || null);
  const [overlay, setOverlay] = React.useState(P.overlay || null);
  const [pdf, setPdf] = React.useState(P.pdf || "downloading");
  const [photo, setPhoto] = React.useState(P.photo === undefined ? null : P.photo);
  const [toast, setToast] = React.useState(P.toast || null);
  const [busy, setBusy] = React.useState(!!P.busy);
  const [err, setErr] = React.useState(P.err || null);
  const [stars, setStars] = React.useState(P.stars || 0);
  const [text, setText] = React.useState(P.text || "");
  const [slaveOk, setSlaveOk] = React.useState(!!P.slaveOk);
  const [descOpen, setDescOpen] = React.useState(false);
  const [connecting, setConnecting] = React.useState(P.v === "connecting" ? "dx4gs" : null);
  const [unsupDev, setUnsupDev] = React.useState(DEVICES[1]);
  const timers = React.useRef([]);
  const later = (fn, ms) => { if (frozen) return; const t = setTimeout(fn, ms); timers.current.push(t); };
  React.useEffect(() => () => timers.current.forEach(clearTimeout), []);
  const rebootSecs = useTicker(screen === "reboot" && !frozen, P.t || 0, reboot && reboot.from + reboot.to);

  const go = (s, variant = null) => { setScreen(s); setV(variant); setErr(null); setBusy(false); setDialog(null); };
  const startReboot = (from, to, toastText) => { setReboot({ from, to, toast: toastText }); go("reboot"); };
  const enterStep = (k) => {
    if (k === "mount") { go("mount"); setPdf("downloading"); }
    else if (k === "bypass" || k === "clone") go(k, "start");
    else go(k);
  };
  const toPrevStep = (k) => {
    const p = prevBefore(k, conn, cloneNeeded);
    if (p === "bypass") go("bypass", cloneNeeded ? "clone" : "ok");
    else if (p === "clone") go("clone", "ok");
    else enterStep(p);
  };

  // Симуляции переходов (в замороженных превью доски не запускаются).
  React.useEffect(() => {
    if (frozen) return;
    if (screen === "starting" && v === "run") later(() => (server === "ok" ? go("write", "run") : go("starting", "error")), 2200);
    if (screen === "write" && v === "run" && !dialog) later(() => (server === "ok" ? startReboot("write", "mount", "Настройки записаны") : go("write", "error")), 3600);
    if (screen === "reboot" && (P.t || 0) < 40) later(() => { const r = reboot; setReboot(null); enterStep(r.to); if (r.toast) setToast(r.toast); }, 6000);
    if (screen === "mount" && pdf === "downloading") later(() => setPdf("ready"), 2600);
    if (screen === "bypass" && v === "run") later(() => {
      if (bypassResult === "error") go("bypass", "error");
      else { setCloneNeeded(bypassResult === "clone"); go("bypass", bypassResult === "clone" ? "clone" : "ok"); }
    }, 3000);
    if (screen === "clone" && v === "run") later(() => go("clone", "ok"), 3000);
  }, [screen, v, dialog, pdf]);
  React.useEffect(() => { if (screen === "reboot" && rebootSecs >= 60) setOverlay("lost"); }, [rebootSecs]);
  React.useEffect(() => { if (!toast || frozen) return; const t = setTimeout(() => setToast(null), 3200); return () => clearTimeout(t); }, [toast]);

  const cur = screen === "reboot" ? (reboot ? reboot.from : "write") : stepOf(screen);
  const sub = SCREEN_BEFORE_CFG.includes(screen) && screen !== "blockcheck" && screen !== "cfgcard" ? null : `${DEV.name} · ID ${DEV.id}`;

  // ── Экраны ──
  let title = STEP[cur] ? STEP[cur].t : "Быстрый монтаж";
  let nav = "back", onBack = null, body = null, dock = null, subtitle = `${DEV.name} · ID ${DEV.id}`;
  const col = (children, max = 760) => <div style={{ maxWidth: max, width: "100%", margin: "0 auto", padding: wide ? "20px 24px 28px" : "16px 16px 24px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 14 }}>{children}</div>;

  switch (screen) {
    case "search": {
      title = "Быстрый монтаж"; subtitle = "Выберите сигнализацию"; onBack = onHome;
      const pick = (d) => { if (connecting) return; setConnecting(d.id); later(() => { setConnecting(null); if (d.unsupported) { setUnsupDev(d); go("unsupported"); } else go("blockcheck", "ok"); }, 1300); };
      body = col(<>
        <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 48 }}>
          <CmpCircularProgressIndicator size={22} strokeWidth={3} />
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Ищем устройства рядом…</CmpText>
        </div>
        {DEVICES.map((d) => (
          <ListRow key={d.id} icon={d.alarm ? "sensors" : d.type === "Метка" ? "vpn_key" : "memory"} title={d.name} disabled={!d.alarm}
            sub={`${d.type} · ${TR[d.tr].label} · ${d.meta}`} busy={connecting === d.id}
            chip={!d.alarm && <Chip icon="block" text="Только для сигнализаций" />} onClick={() => pick(d)} />
        ))}
      </>, 720);
      break;
    }
    case "unsupported": {
      title = "Быстрый монтаж"; subtitle = `${unsupDev.name} · ID 118402`; onBack = () => go("search");
      body = <Hero art={<Placeholder label="иллюстрация: блок не поддерживается" icon="report" />} title="Блок не поддерживает быстрый монтаж"
        text={`Модели ${unsupDev.name} нет в базе быстрого монтажа. Выберите другой блок или настройте этот вручную в расширенном монтаже.`} />;
      dock = <Dock wide={wide}><MainBtn text="Выбрать другой блок" icon="search" onClick={() => go("search")} /><MainBtn outlined text="Расширенный монтаж" onClick={() => window.psGo && window.psGo("advanced-mount")} /></Dock>;
      break;
    }
    case "blockcheck": {
      title = "Проверка блока"; onBack = () => go("search");
      const vv = v || "ok";
      const block = vv === "factory" || vv === "noaccess";
      const fw = vv === "outdated" ? DEV.fw : DEV.fwNew;
      body = col(<>
        {vv === "ok" && <Banner tone="success" title="Блок готов к быстрому монтажу" text="Прошивка актуальна, CAN-настройки подходят." />}
        {vv === "outdated" && <Banner tone="warn" icon="system_update" title={`Доступна новая прошивка ${DEV.fwNew} — рекомендуем обновить перед монтажом`} text="Можно продолжить и без обновления." action="Обновить прошивку" onAction={() => window.psGo && window.psGo("firmware")} />}
        {vv === "factory" && <Banner tone="error" title="Сначала обновите прошивку под CAN автомобиля" text="В блоке заводские CAN-настройки (0000). Быстрый монтаж с ними невозможен." action="Обновить прошивку" onAction={() => window.psGo && window.psGo("firmware")} />}
        {vv === "noaccess" && <Banner tone="error" icon="lock" title="Нет доступа к блоку" text="Блок привязан к другой учётной записи. Обратитесь к владельцу блока или в поддержку." />}
        <Card title="Блок">
          <KV label="Модель" value={DEV.name} />
          <KV label="ID блока" value={DEV.id} mono />
          <KV label="Прошивка" value={fw} mono tone={vv === "outdated" ? "warn" : undefined} />
          <KV label="CAN-прошивка" value={vv === "factory" ? "0000 · заводская" : `${DEV.can} · Volkswagen`} mono tone={vv === "factory" ? "error" : undefined} />
          <KV label="Связь" value={TR[conn].label} />
        </Card>
      </>);
      dock = <Dock wide={wide} note={block ? "«Далее» станет доступна, когда проблема будет решена" : null}>
        <MainBtn text="Далее" icon="arrow_forward" disabled={block} onClick={() => go("vehicle")} />
        {vv === "noaccess" && <MainBtn outlined text="Выбрать другой блок" onClick={() => go("search")} />}
      </Dock>;
      break;
    }
    case "vehicle": {
      title = "Автомобиль"; onBack = () => go("blockcheck", "ok");
      body = col(<>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Каталог автомобилей — существующий экран. Для примера — недавние:</CmpText>
        {[[`${CAR.brand} ${CAR.model}`, `${CAR.year} · CAN ${CAR.can}`], ["Kia Rio", "2021 · CAN 0188"], ["Toyota Camry", "2019 · CAN 0342"]].map(([t, s], i) => (
          <ListRow key={t} icon="directions_car" title={t} sub={s} onClick={() => i === 0 && go("configs")} />
        ))}
      </>, 720);
      break;
    }
    case "configs": {
      title = "Конфигурации"; subtitle = `${CAR.brand} ${CAR.model} ${CAR.year}`; onBack = () => go("vehicle");
      body = col(<>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Облачные конфигурации — существующий экран.</CmpText>
        <ListRow icon="cloud_done" title={CFG.name} sub={`${CFG.author} · ${CFG.date}`} right={<Stars value={CFG.rating} />} onClick={() => go("cfgcard", "ok")} />
        <ListRow icon="cloud_done" title="Polo 2020 — базовая охрана" sub="Игорь Сафонов · 02.08.2026 · CAN 0215" right={<Stars value={4.5} />} onClick={() => go("cfgcard", "mismatch")} />
      </>, 720);
      break;
    }
    case "cfgcard": {
      title = "Конфигурация"; onBack = () => go("configs");
      const mm = v === "mismatch";
      const mmBanner = mm && <Banner tone="error" icon="sync_problem" title={`Конфигурация для CAN 0215, в блоке ${DEV.can}`} text="Обновите CAN-прошивку блока или выберите другую конфигурацию." action="Обновить прошивку" onAction={() => window.psGo && window.psGo("firmware")} />;
      const info = <>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <CmpText variant="headlineSmall" style={{ textWrap: "balance" }}>{mm ? "Polo 2020 — базовая охрана" : CFG.name}</CmpText>
          <div style={{ display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" }}>
            <span style={{ width: 40, height: 40, borderRadius: 999, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", display: "grid", placeItems: "center", fontFamily: "var(--font-sans)", fontWeight: 700, fontSize: 16 }}>{mm ? "ИС" : "АМ"}</span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <CmpText variant="titleMedium" weight={600} style={{ display: "block" }}>{mm ? "Игорь Сафонов" : CFG.author}</CmpText>
              <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{mm ? "02.08.2026" : CFG.date}</CmpText>
            </div>
            <Stars value={mm ? 4.5 : CFG.rating} size={24} /><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{mm ? "12 оценок" : `${CFG.votes} оценок`}</CmpText>
          </div>
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}><Chip icon="directions_car" text={`${CAR.brand} ${CAR.model} ${CAR.year}`} /><Chip icon="cable" text={mm ? "CAN 0215" : `CAN ${CFG.can}`} tone={mm ? "error" : undefined} /></div>
        </div>
        <CmpText variant="bodyLarge" style={{ textWrap: "pretty" }}>{CFG.desc}</CmpText>
      </>;
      const media = <>
        <PdfCard state="ready" onOpen={() => setToast("Карта монтажа откроется в приложении для PDF")} />
        <CmpText variant="titleMedium" weight={700}>Фото · {PHOTOS.length}</CmpText>
        <div style={{ display: "grid", gridTemplateColumns: wide ? "repeat(auto-fill, minmax(140px, 1fr))" : "repeat(2, minmax(0, 1fr))", gap: 10 }}>{PHOTOS.map((p, i) => <Placeholder key={i} w="100%" h={wide ? 110 : 100} radius={14} label={`фото ${i + 1}`} />)}</div>
      </>;
      // Все размеры — одной колонкой: баннер → описание → карта монтажа → фото (на широком окне фото сеткой).
      body = col(<>{mmBanner}{info}{media}</>, 840);
      dock = <Dock wide={wide} note={mm ? "«Выбрать» недоступна: CAN конфигурации не совпадает с блоком" : null}><MainBtn text="Выбрать" icon="check" disabled={mm} onClick={() => go("summary")} /></Dock>;
      break;
    }
    case "summary": {
      title = "Сводка"; onBack = () => go("cfgcard", "ok");
      const plan = counted(conn, true).filter((k) => k !== "prep");
      const card = (icon, t, rows) => (
        <Card title={<span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpIcon icon={icon} size={24} color="var(--primary)" />{t}</span>} pad="4px 16px 12px">
          {rows.map(([l, val, m]) => <KV key={l} label={l} value={val} mono={m} />)}
        </Card>
      );
      let n = 0;
      body = col(<>
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <CmpText variant="headlineSmall">Всё готово к монтажу</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">После «Начать монтаж» настройки запишутся в блок, дальше мастер поведёт по шагам.</CmpText>
        </div>
        <div style={{ display: "grid", gridTemplateColumns: paneW >= 900 ? "repeat(3, minmax(0,1fr))" : "1fr", gap: 12 }}>
          {card("sensors", "Блок", [["Модель", DEV.name], ["ID", DEV.id, true], ["Прошивка", DEV.fwNew, true], ["Связь", TR[conn].label]])}
          {card("directions_car", "Автомобиль", [["Марка", CAR.brand], ["Модель", CAR.model], ["Год", CAR.year], ["CAN", CAR.can, true]])}
          <Card title={<span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpIcon icon="cloud_done" size={24} color="var(--primary)" />Конфигурация</span>} pad="8px 16px 14px">
            <CmpText variant="titleMedium" weight={600} style={{ display: "block", textWrap: "pretty" }}>{CFG.name}</CmpText>
            <div style={{ display: "flex", alignItems: "center", gap: 10, paddingTop: 8 }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)" style={{ flex: 1 }}>{CFG.author}</CmpText><Stars value={CFG.rating} /></div>
          </Card>
        </div>
        <Card title="План монтажа" aside={<CmpText variant="labelLarge" color="var(--on-surface-variant)" style={{ fontSize: 15 }}>{plan.length - 1} шагов + Clone, если потребуется</CmpText>} pad="8px 12px 12px">
          <div style={{ display: "grid", gridTemplateColumns: paneW >= 640 ? "repeat(2, minmax(0,1fr))" : "1fr", gap: 4 }}>
            {plan.map((k) => {
              const opt = k === "clone"; if (!opt) n++;
              return (
                <div key={k} style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 52, padding: "4px 6px" }}>
                  <span style={{ width: 36, height: 36, borderRadius: 999, display: "grid", placeItems: "center", flex: "0 0 auto", border: opt ? "2px dashed var(--outline)" : "2px solid var(--outline)", color: "var(--on-surface-variant)", fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 700 }}>{opt ? <CmpIcon icon="add" size={18} color="currentColor" /> : n}</span>
                  <span style={{ display: "flex", flexDirection: "column" }}>
                    <CmpText variant="titleMedium" weight={500} color={opt ? "var(--on-surface-variant)" : "var(--on-surface)"}>{STEP[k].t}</CmpText>
                    {opt && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Если потребуется обходу</CmpText>}
                  </span>
                </div>
              );
            })}
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "8px 6px 0" }}><CmpIcon icon="autorenew" size={22} color="var(--on-surface-variant)" /><CmpText variant="bodyMedium" color="var(--on-surface-variant)">После записи, монтажа, обхода и Clone блок перезагружается — обычно 5–30 с.</CmpText></div>
        </Card>
        {conn === "usb" && <Banner tone="info" icon="usb" title="По USB проверка системы недоступна" text="Проверьте систему после монтажа по Bluetooth." />}
        <Banner tone="warn" title="Настройки блока будут заменены настройками конфигурации" text="Настройки GSM и GPRS блока сохранятся." />
      </>, 1040);
      dock = <Dock wide={wide}><MainBtn text="Начать монтаж" icon="play_arrow" onClick={() => go("starting", "run")} /></Dock>;
      break;
    }
    case "starting": {
      title = "Запись настроек";
      if (v === "error") {
        onBack = () => go("summary");
        body = <Hero art={<div style={{ width: 112, height: 112, borderRadius: 999, background: "var(--error-container)", color: "var(--on-error-container)", display: "grid", placeItems: "center" }}><CmpIcon icon="cloud_off" size={52} color="currentColor" /></div>}
          title="Не удалось начать монтаж" text="Нет связи с сервером. Проверьте интернет и повторите. С блоком ещё ничего не делали." />;
        dock = <Dock wide={wide}><MainBtn text="Повторить" icon="refresh" onClick={() => go("starting", "run")} /><MainBtn outlined text="Назад" onClick={() => go("summary")} /></Dock>;
      } else {
        nav = "lock";
        body = <Hero art={<BusyArt icon="cloud_sync" />} title="Готовим монтаж…" text="Получаем конфигурацию с сервера. Обычно 1–5 секунд." />;
      }
      break;
    }
    case "write": {
      if (v === "error") {
        onBack = () => setDialog("exit");
        body = col(<>
          <Banner tone="error" title="Настройки не записаны" text="Блок не подтвердил запись. Проверьте, что блок подключён, и повторите." />
          <Hero art={<div style={{ width: 112, height: 112, borderRadius: 999, background: "var(--surface-container-high)", color: "var(--on-surface-variant)", display: "grid", placeItems: "center" }}><CmpIcon icon="memory" size={52} color="currentColor" /></div>}
            title="Запись прервана" text="Настройки в блоке не изменились — можно повторить запись." />
        </>, 640);
        dock = <Dock wide={wide}><MainBtn text="Повторить" icon="refresh" onClick={() => go("write", "run")} /></Dock>;
      } else {
        nav = "lock";
        body = <Hero art={<BusyArt icon="memory" />} title="Записываем настройки в блок" text="Не отключайте блок и не закрывайте приложение.">
          <IndetBar />
        </Hero>;
      }
      break;
    }
    case "reboot": {
      title = "Перезагрузка блока"; nav = "lock";
      const to = reboot ? reboot.to : "bypass";
      body = <Hero art={<BusyArt icon="restart_alt" spin />} title="Перезагрузка блока…" text="Не отключайте питание.">
        <span style={{ fontFamily: "var(--font-mono)", fontSize: 40, fontWeight: 600, color: "var(--on-surface)", letterSpacing: ".02em" }}>{mss(rebootSecs)}</span>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">Обычно 5–30 секунд</CmpText>
        <Chip icon="arrow_forward" text={`Далее: ${STEP[to] ? STEP[to].t : "Готово"}`} />
      </Hero>;
      break;
    }
    case "mount": {
      onBack = () => setDialog("exit");
      const left = <>
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <CmpText variant="headlineSmall" style={{ textWrap: "balance" }}>Установите сигнализацию по карте монтажа</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Когда закончите — нажмите «Монтаж завершён».</CmpText>
        </div>
        <PdfCard state={pdf} onOpen={() => setToast("Карта монтажа откроется в приложении для PDF")} onRetry={() => setPdf("downloading")} />
        <CmpSurface color="var(--surface-container-low)" radius={20} border style={{ overflow: "hidden" }}>
          <div role="button" tabIndex={0} aria-expanded={descOpen} className="fm-hover" onClick={() => setDescOpen(!descOpen)} onKeyDown={(e) => { if (e.key === "Enter") setDescOpen(!descOpen); }} style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "8px 12px 8px 16px", cursor: "pointer" }}>
            <CmpIcon icon="description" size={26} color="var(--primary)" />
            <CmpText variant="titleMedium" weight={600} style={{ flex: 1 }}>Описание конфигурации</CmpText>
            <span style={{ display: "inline-flex", transform: descOpen ? "rotate(180deg)" : "none", transition: "transform .25s" }}><CmpIcon icon="expand_more" size={28} color="var(--on-surface-variant)" /></span>
          </div>
          {descOpen && <div className="fm-anim" style={{ padding: "0 16px 16px", animation: "fmIn .2s ease both" }}><CmpText variant="bodyLarge" style={{ textWrap: "pretty" }}>{CFG.desc}</CmpText></div>}
        </CmpSurface>
      </>;
      const right = <>
        <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 12 }}><CmpText variant="titleLarge" weight={700}>Особенности монтажа</CmpText><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{PHOTOS.length} фото</CmpText></div>
        <PhotoGrid cols={2} h={wide ? 150 : 120} onOpen={setPhoto} />
      </>;
      // Широкое окно: задача на всю ширину; слева — крупная карта монтажа и описание (раскрыто), справа — фото: первое крупно, остальные сеткой.
      const wideBody = (
        <div style={{ maxWidth: 1120, width: "100%", margin: "0 auto", padding: "20px 24px 28px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 18 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
            <span style={{ width: 48, height: 48, borderRadius: 14, background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", flex: "0 0 auto" }}><CmpIcon icon="build" size={26} color="currentColor" /></span>
            <div style={{ flex: 1, minWidth: 0 }}>
              <CmpText variant="headlineSmall" style={{ display: "block", textWrap: "balance" }}>Установите сигнализацию по карте монтажа</CmpText>
              <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Когда закончите — нажмите «Монтаж завершён».</CmpText>
            </div>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: !two ? "1fr" : width >= 1100 ? "minmax(0,1.25fr) minmax(0,1fr)" : "minmax(0,1fr) minmax(0,1fr)", gap: 24, alignItems: "start" }}>
            <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
              <PdfPreview state={pdf} onOpen={() => setToast("Карта монтажа откроется в приложении для PDF")} onRetry={() => setPdf("downloading")} />
              <Card title={<span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpIcon icon="description" size={24} color="var(--primary)" />Описание конфигурации</span>} pad="8px 16px 16px">
                <CmpText variant="bodyLarge" style={{ textWrap: "pretty" }}>{CFG.desc}</CmpText>
              </Card>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
              <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 12 }}><CmpText variant="titleLarge" weight={700}>Особенности монтажа</CmpText><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{PHOTOS.length} фото</CmpText></div>
              <PhotoMosaic onOpen={setPhoto} />
            </div>
          </div>
        </div>
      );
      body = wide ? wideBody : col(<>{left}{right}</>);
      dock = <Dock wide={wide}><MainBtn text="Монтаж завершён" icon="check" onClick={() => setDialog("mountDone")} /></Dock>;
      break;
    }
    case "bypass":
    case "clone": {
      const isC = screen === "clone";
      const vv = v || "start";
      const toMount = () => setDialog(cloneNeeded || isC ? "toMountClone" : "toMount");
      if (vv === "run") nav = "lock"; else onBack = toMount;
      const W = wide ? 320 : 250;
      const art = { start: isC ? "alarm_clone" : "bypass_learning_turn_off_ignition_request", run: isC ? "alarm_cloning" : "bypass_learning_process", ok: "bypass_result_success", clone: "bypass_result_success", error: "bypass_result_fail" }[vv];
      const T = {
        start: isC ? ["Процедура Clone", "Блок скопирует данные штатного ключа. Держите ключ рядом с замком зажигания."] : ["Обучение обходу", "Выключите зажигание и держите штатный ключ рядом. Обучение займёт 1–3 минуты."],
        run: isC ? ["Идёт Clone…", "Не выключайте зажигание и не отключайте блок."] : ["Идёт обучение…", "Следуйте подсказкам на экране. Не отключайте блок."],
        ok: isC ? ["Clone выполнен", "Данные ключа сохранены в блоке."] : ["Обучение завершено", "Обход иммобилайзера настроен."],
        clone: ["Обучение завершено", null],
        error: ["Обход не завершён", "Блок не получил данные от иммобилайзера. Проверьте подключение CAN и повторите."],
      }[vv];
      body = <Hero art={<Illus file={art} w={W} />} title={T[0]} text={T[1]}>
        {vv === "run" && <IndetBar />}
        {vv === "clone" && <Banner tone="info" icon="content_copy" title="Для обхода нужен Clone" text="Он будет следующим шагом — отдельно открывать ничего не нужно." />}
      </Hero>;
      if (vv === "start") dock = <Dock wide={wide}><MainBtn text={isC ? "Начать Clone" : "Начать обучение"} icon="play_arrow" onClick={() => go(screen, "run")} /></Dock>;
      if (vv === "ok" || vv === "clone") dock = <Dock wide={wide}><MainBtn text="Далее" icon="arrow_forward" onClick={() => startReboot(screen, nextAfter(screen, conn, cloneNeeded))} /></Dock>;
      if (vv === "error") dock = <Dock wide={wide} note="Шаг нельзя пропустить"><MainBtn text="Повторить" icon="refresh" onClick={() => go(screen, "start")} /><MainBtn outlined text="Заявка на техпомощь" icon="phone_callback" onClick={() => window.psTicket && window.psTicket(isC ? "Быстрый монтаж · Clone" : "Быстрый монтаж · обход", `${T[0]}. ${T[1]}`)} /><TextBtn text="Написать в чат поддержки" icon="support_agent" fullWidth style={{ height: 48 }} onClick={() => window.psGo && window.psGo("support-chat")} /></Dock>;
      break;
    }
    case "slave": {
      onBack = () => toPrevStep("slave");
      const steps = ["Выключите зажигание, закройте все двери.", "На штатном брелоке нажмите «Закрыть», затем «Открыть».", "Дождитесь короткого сигнала сирены — брелок обучен."];
      const art = <Placeholder w="100%" h={two ? 280 : 180} label="анимация: обучение штатного брелока (slave_learn)" icon="settings_remote" />;
      const ins = <>
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <CmpText variant="headlineSmall">Обучите штатный брелок</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Сигнализация будет ставиться и сниматься с охраны штатным брелоком.</CmpText>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          {steps.map((s, i) => (
            <div key={i} style={{ display: "flex", gap: 12, alignItems: "flex-start", padding: "6px 0" }}>
              <span style={{ width: 36, height: 36, borderRadius: 999, background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", flex: "0 0 auto", fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 700 }}>{i + 1}</span>
              <CmpText variant="bodyLarge" style={{ paddingTop: 6, textWrap: "pretty" }}>{s}</CmpText>
            </div>
          ))}
        </div>
      </>;
      // «Обучение выполнено» — в панели рядом с «Далее»: всегда на виду, не уезжает под край.
      const okRow = (
        <label className="fm-hover" style={{ display: "flex", alignItems: "center", gap: 10, minHeight: 56, padding: "0 16px 0 6px", borderRadius: 999, cursor: "pointer", boxSizing: "border-box", background: slaveOk ? "var(--secondary-container)" : "var(--surface-container-low)", border: "1.5px solid " + (slaveOk ? "var(--primary)" : "var(--outline-variant)") }}>
          <CmpCheckbox checked={slaveOk} onCheckedChange={setSlaveOk} />
          <CmpText variant="titleMedium" weight={600}>Обучение выполнено</CmpText>
        </label>
      );
      body = two
        ? <div style={{ maxWidth: 1040, width: "100%", margin: "0 auto", padding: "20px 24px 28px", display: "grid", gridTemplateColumns: "minmax(0,1fr) minmax(0,1.15fr)", gap: 28, alignItems: "center", boxSizing: "border-box", minHeight: "100%" }}>{art}<div style={{ display: "flex", flexDirection: "column", gap: 14 }}>{ins}</div></div>
        : col(<>{art}{ins}</>);
      const nextBtn = <MainBtn text="Далее" icon="arrow_forward" disabled={!slaveOk} onClick={() => setDialog("slaveDone")} />;
      dock = wide ? <Dock wide>{nextBtn}{okRow}</Dock> : <Dock>{okRow}{nextBtn}</Dock>;
      break;
    }
    case "review": {
      onBack = () => toPrevStep("review");
      const can = stars > 0 && text.trim().length > 0;
      const send = () => { setBusy(true); setErr(null); later(() => { setBusy(false); if (server === "ok") go("done"); else setErr("send"); }, 1800); };
      const form = <>
        {err && <Banner tone="error" title="Отзыв не отправлен" text="Нет связи с сервером. Проверьте интернет — введённое сохранится." action="Повторить" onAction={send} />}
        <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
          <CmpText variant="headlineSmall">Оцените конфигурацию</CmpText>
          <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Это поможет другим монтажникам. Оценка и отзыв обязательны.</CmpText>
        </div>
        <Card pad="12px 16px 16px">
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)" style={{ display: "block" }}>{CFG.author}</CmpText>
          <CmpText variant="titleMedium" weight={600} style={{ display: "block", textWrap: "pretty" }}>{CFG.name}</CmpText>
          <div role="radiogroup" aria-label="Оценка" style={{ display: "flex", gap: 4, paddingTop: 12 }}>
            {[1, 2, 3, 4, 5].map((i) => (
              <button key={i} type="button" role="radio" aria-checked={stars === i} aria-label={`${i} из 5`} className="fm-hover" onClick={() => !busy && setStars(i)}
                style={{ width: 56, height: 56, borderRadius: 999, border: "none", background: "transparent", display: "grid", placeItems: "center", cursor: "pointer" }}>
                <CmpIcon icon={i <= stars ? "star" : "star_border"} size={42} color={i <= stars ? "var(--tertiary)" : "var(--on-surface-variant)"} />
              </button>
            ))}
          </div>
        </Card>
        <label style={{ display: "flex", flexDirection: "column", gap: 6 }}>
          <CmpText variant="labelLarge" color="var(--on-surface-variant)" style={{ fontSize: 15 }}>Отзыв</CmpText>
          <textarea value={text} disabled={busy} onChange={(e) => setText(e.target.value)} placeholder="Что получилось, что пришлось сделать иначе"
            style={{ minHeight: 132, resize: "vertical", borderRadius: 12, border: "1.5px solid var(--outline)", background: "var(--surface)", color: "var(--on-surface)", padding: "14px 16px", fontFamily: "var(--font-sans)", fontSize: 17, lineHeight: 1.45, outlineColor: "var(--primary)", boxSizing: "border-box" }} />
        </label>
      </>;
      const done = counted(conn, cloneNeeded).filter((k) => k !== "review");
      const summ = (
        <Card title="Итоги монтажа" pad="4px 16px 14px">
          <KV label="Время монтажа" value="1 ч 24 мин" />
          {conn !== "usb" && <KV label="Проверки" value="8 из 9 · пропущена 1" />}
          <div style={{ display: "flex", flexWrap: "wrap", gap: 6, paddingTop: 10 }}>{done.map((k) => <Chip key={k} icon="check" text={STEP[k].t} />)}</div>
        </Card>
      );
      body = two
        ? <div style={{ maxWidth: 1040, width: "100%", margin: "0 auto", padding: "20px 24px 28px", display: "grid", gridTemplateColumns: "minmax(0,1.3fr) minmax(0,1fr)", gap: 24, alignItems: "start", boxSizing: "border-box" }}><div style={{ display: "flex", flexDirection: "column", gap: 14 }}>{form}</div>{summ}</div>
        : col(<>{form}{summ}</>);
      dock = <Dock wide={wide} note={!can && !busy ? "Поставьте оценку и напишите отзыв" : null}><MainBtn text="Завершить монтаж" icon="done_all" disabled={!can} busy={busy} busyText="Отправляем…" onClick={send} /></Dock>;
      break;
    }
    case "done": {
      title = "Готово"; nav = "none";
      body = <Hero art={<SuccessArt />} title="Быстрый монтаж завершён" text="Можно передавать автомобиль владельцу." />;
      dock = <Dock wide={wide}><MainBtn text="На главную" icon="home" onClick={onHome} /></Dock>;
      break;
    }
  }

  const navIcon = nav === "none" ? null : nav === "lock"
    ? <div title="Выход недоступен, пока идёт операция" aria-label="Назад недоступно" style={{ width: 48, height: 48, display: "grid", placeItems: "center", color: "var(--on-surface-variant)" }}><CmpIcon icon="lock" size={24} color="currentColor" /></div>
    : <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack || (() => {})} />;

  const dlgOk = {
    exit: () => { setDialog(null); onHome(); },
    toMount: () => { setCloneNeeded(null); enterStep("mount"); },
    toMountClone: () => { setCloneNeeded(null); enterStep("mount"); },
    prev: () => { setDialog(null); toPrevStep("check"); },
    mountDone: () => startReboot("mount", "bypass"),
    slaveDone: () => { setDialog(null); go("check"); },
  };

  const exitAll = () => onHome();
  let pane;
  if (screen === "check") {
    pane = <div style={{ position: "relative", flex: 1, minWidth: 0, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <window.CheckStateScreen key={preset} mode="quick" preset={preset === "F12" || preset === "F16c" ? "F2" : "F1"} device={DEV} onBack={() => toPrevStep("check")} onDone={() => go("review")} />
    </div>;
  } else {
    pane = (
      <div style={{ position: "relative", flex: 1, minWidth: 0, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <CmpTopAppBar title={title} subtitle={subtitle} navigationIcon={navIcon} />
        <div style={{ flex: 1, minHeight: 0, minWidth: 0, overflowY: "auto", overflowX: "hidden", ...(body && body.type === Hero ? { display: "flex", flexDirection: "column", justifyContent: "center" } : { display: "block" }) }}>{body}</div>
        {toast && (
          <div style={{ position: "absolute", left: 12, right: 12, bottom: dock ? 100 : 16, zIndex: 6, display: "flex", justifyContent: "center" }}>
            <div className="fm-anim" style={{ width: "100%", maxWidth: 520, animation: "fmUp .25s ease both" }}><CmpSnackbar message={toast} style={{ width: "100%", borderRadius: 12, minHeight: 56 }} /></div>
          </div>
        )}
        {dock}
        {photo !== null && <PhotoViewer index={photo} onClose={() => setPhoto(null)} onNav={(d) => setPhoto((i) => Math.max(0, Math.min(PHOTOS.length - 1, i + d)))} />}
      </div>
    );
  }

  return (
    <div ref={rootRef} data-screen-label={`Быстрый монтаж · ${preset}`} style={{ position: "relative", height: "100%", display: "flex", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      {wide && <StepRail conn={conn} cur={cur} prepSub={PREP_OF[screen] || "summary"} cloneNeeded={cloneNeeded} reboot={screen === "reboot" ? reboot : null} rebootSecs={rebootSecs} cfgChosen={!SCREEN_BEFORE_CFG.includes(screen)} width={railW} compact={railCompact} dense={height < 860} />}
      {pane}
      {dialog && DIALOG[dialog] && <StdDialog k={dialog} onOk={dlgOk[dialog]} onCancel={() => setDialog(null)} />}
      {dialog === "pin" && <PinDialog onOk={() => setDialog(null)} onCancel={() => { setDialog(null); go("write", "error"); }} />}
      {overlay === "lost" && <LostOverlay onExit={exitAll} />}
      {overlay === "other" && <OtherBlockOverlay onExit={exitAll} />}
    </div>
  );
}

// Успех на «Готово»: плейсхолдер иллюстрации + анимированный значок (pop + расходящиеся кольца).
function SuccessArt() {
  return (
    <div style={{ position: "relative" }}>
      <Placeholder label="иллюстрация: монтаж завершён" icon="directions_car" w={260} h={180} />
      <span style={{ position: "absolute", right: -14, bottom: -14, width: 76, height: 76 }}>
        <span className="fm-anim" style={{ position: "absolute", inset: 0, borderRadius: 999, background: "var(--success)", animation: "fmBurst 1.2s ease-out .35s both" }} />
        <span className="fm-anim" style={{ position: "absolute", inset: 0, borderRadius: 999, background: "var(--success)", animation: "fmBurst 1.2s ease-out .6s both" }} />
        <span className="fm-anim" style={{ position: "absolute", inset: 0, borderRadius: 999, background: "var(--success)", display: "grid", placeItems: "center", boxShadow: "0 0 0 5px var(--background)", animation: "fmPop .55s cubic-bezier(.2,.9,.3,1.3) .15s both" }}><CmpIcon icon="check" size={46} color="#fff" /></span>
      </span>
    </div>
  );
}

// ── Демо-панель ──────────────────────────────────────────────────────────────
const FM_FLOW = [["F1", "Поиск"], ["F3a", "Проверка блока"], ["F4a", "Конфигурация"], ["F5a", "Сводка"], ["F7a", "Запись"], ["F8a", "Монтаж"], ["F9", "Перезагрузка"], ["F10s", "Обход"], ["F11", "Slave"], ["F12", "Проверка"], ["F13a", "Отзыв"], ["F14", "Готово"]];
const FM_STATES = [["F2", "F2 Не поддерживает"], ["F3b", "F3 Обновить"], ["F3c", "F3 Заводской CAN"], ["F3d", "F3 Нет доступа"], ["F4b", "F4 CAN ≠"], ["F6a", "F6 Готовим"], ["F6b", "F6 Ошибка"], ["F7b", "F7 Ошибка"], ["F7c", "F7 PIN"], ["F8c", "F8 PDF ошибка"], ["F8d", "F8 Фото"], ["F9b", "F9 → потеря связи"], ["F10b", "F10 Нужен Clone"], ["F10c", "F10 Ошибка"], ["F15", "F15 Другой блок"], ["FL", "Потеря связи"], ["F16a", "F16 Выйти?"], ["F16b", "F16 К монтажу?"], ["F16c", "F16 Пред. шаг?"], ["F17", "F17 Ошибка шага"]];
function FmSeg({ items, value, onChange, wrap }) {
  return <div className={"seg" + (wrap ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
function useFmCtl() {
  const q = new URLSearchParams(location.search);
  const [dark, setDark] = React.useState(q.get("theme") === "dark");
  const [preset, setPreset] = React.useState(q.get("state") || "F1");
  const [conn, setConn] = React.useState("ble");
  const [bypass, setBypass] = React.useState("clone");
  const [server, setServer] = React.useState("ok");
  React.useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const bar = (
    <React.Fragment>
      <div className="toolbar">
        <FmSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(x) => setDark(x === "d")} />
        <FmSeg items={[["ble", "Bluetooth"], ["usb", "USB"]]} value={conn} onChange={setConn} />
        <FmSeg items={[["ok", "Обход: успех"], ["clone", "Нужен Clone"], ["error", "Ошибка"]]} value={bypass} onChange={setBypass} />
        <FmSeg items={[["ok", "Сервер: ок"], ["error", "Нет связи"]]} value={server} onChange={setServer} />
      </div>
      <div className="toolbar"><span className="hint">Сценарий:</span><FmSeg wrap items={FM_FLOW} value={preset} onChange={setPreset} /></div>
      <div className="toolbar"><span className="hint">Состояния:</span><FmSeg wrap items={FM_STATES} value={preset} onChange={setPreset} /></div>
    </React.Fragment>
  );
  return { dark, bar, props: { preset, conn, bypassResult: bypass, server }, key: [preset, conn, bypass, server].join() };
}

Object.assign(window, { FastMountApp, useFmCtl, FmSeg, FM_StepRail: StepRail, FM_Modal: StdDialog, FM_Banner: Banner, FM_PRESETS: PRESETS, FM_SuccessArt: SuccessArt, FM_ListRow: ListRow, FM_Chip: Chip, FM_PdfCard: PdfCard });
})();
