
// Firmware — «Проверка прошивки» + «Обновление прошивки» (загрузчик) + процесс обновления.
// Решения по ТЗ (раздел 7): без кнопки «Далее»; описание версии показывается и на шаге подтверждения;
// отмена на передаче данных запрещена (Назад скрыт с шага «Подготовка» до результата);
// канал обновления — один на экран (сегмент), а не на каждой карточке.
// Адаптив (M3 WSC по ширине контейнера): compact <600 — 1 колонка; medium 600–839 / expanded ≥840 —
// карточки в 2 колонки (max-width 1040), экран обновления — иллюстрация и данные в две равные части.

const FW = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpButton, CmpOutlinedButton, CmpTextButton, CmpText, CmpIcon, CmpCircularProgressIndicator, CmpLinearProgressIndicator, CmpDropdown, CmpDialog } = FW;

const FW_DEV = { name: "Pandora DX-4GS", id: "107131" };
const L = (s) => (window.FW_LANG === "en" && window.FW_EN && window.FW_EN[s] != null) ? window.FW_EN[s] : s;
const N = (s) => (window.FW_LONG && window.FW_LONGN && window.FW_LONGN[s]) || s;
const fwSub = (profile) => profile === "bt" ? L("Bluetooth-модуль") + " · " + N("Pandora BT (BTM2)") : N(FW_DEV.name) + "  " + FW_DEV.id;
const wrapTxt = { overflowWrap: "break-word", minWidth: 0 };
const FW_CHANNELS = [["release", "Готовая (протестированная)", "Готовая"], ["beta", "Beta-версия", "Beta"], ["alpha", "Alpha-версия", "Alpha"]];
const FW_TEST_WARN = "Внимание! Эта тестовая версия. Некоторые функции могут работать нестабильно";
const FW_POWER = "Не выключайте питание на устройстве и не разрывайте соединение с устройством";
const FW_NOTES = "• Улучшена стабильность связи по Bluetooth при слабом сигнале\n• Исправлена ошибка запуска двигателя по расписанию\n• Добавлена поддержка новых CAN-профилей";

(function () {
  if (document.getElementById("fw-anim")) return;
  const s = document.createElement("style"); s.id = "fw-anim";
  s.textContent = "@keyframes fwRing{0%{transform:scale(.9);opacity:.5}100%{transform:scale(1.35);opacity:0}}@keyframes fwIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}@media (prefers-reduced-motion: reduce){.fw-anim{animation:none!important}}.fw-hov:hover{background-color:color-mix(in srgb, var(--on-surface) 8%, transparent)!important}.fw-hov:focus-visible{outline:2px solid var(--primary);outline-offset:2px}";
  document.head.appendChild(s);
})();

function useFwWidth() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current; if (!el) return;
    const m = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(m); ro.observe(el); m();
    return () => ro.disconnect();
  }, []);
  return [ref, w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact", w];
}

function Ver({ v, size = 15, color = "var(--primary)" }) {
  return <span style={{ fontFamily: "var(--font-mono)", fontSize: size, fontWeight: 700, color }}>{v}</span>;
}
function Spin({ size = 20 }) { return <CmpCircularProgressIndicator size={size} strokeWidth={2.5} />; }
const btn48 = { height: 48 };

function Banner({ icon = "warning", tone = "warn", children }) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "ok" ? "var(--primary-container)" : "var(--tertiary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "ok" ? "var(--on-primary-container)" : "var(--on-tertiary-container)";
  return (
    <div style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "12px 14px", borderRadius: "var(--r-card-small-radius)", background: bg, color: fg, boxSizing: "border-box" }}>
      <CmpIcon icon={icon} size={24} color="currentColor" />
      <div style={{ flex: 1, minWidth: 0 }}><CmpText variant="bodyMedium" color="currentColor">{children}</CmpText></div>
    </div>
  );
}

function Hero({ icon = "system_update", tone = "primary", size = 160, pulse = false }) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "neutral" ? "var(--surface-variant)" : "var(--primary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "neutral" ? "var(--on-surface-variant)" : "var(--on-primary-container)";
  return (
    <div style={{ position: "relative", width: size, height: size, flex: "0 0 auto" }} aria-hidden="true">
      {pulse && <div className="fw-anim" style={{ position: "absolute", inset: 0, borderRadius: "50%", border: "3px solid var(--primary)", animation: "fwRing 1.8s ease-out infinite" }} />}
      <div style={{ position: "absolute", inset: 0, borderRadius: "50%", background: bg, display: "grid", placeItems: "center" }}>
        <CmpIcon icon={icon} size={Math.round(size * 0.46)} color={fg} />
      </div>
    </div>
  );
}

function Center({ children, gap = 16 }) {
  return <div style={{ flex: 1, minHeight: 240, display: "flex", alignItems: "center", justifyContent: "center", padding: "24px 16px", boxSizing: "border-box" }}>
    <div style={{ maxWidth: 440, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap, textAlign: "center" }}>{children}</div>
  </div>;
}

// ════════ Экран 1. Проверка прошивки ════════════════════════════════════════
const FW_MODS = {
  base:  { id: "base",  type: "Базовый блок сигнализации", name: "Pandora DX 4G (DX4G)", cur: "3.57" },
  bt:    { id: "bt",    type: "Bluetooth-модуль",          name: "Pandora BT (BTM2)",    cur: "1.14" },
  watch: { id: "watch", type: "Основной модуль часов",     name: "Pandora Watch (PW2)",  cur: "2.03" },
};
function fwScenario(sc, channel) {
  const v = channel === "release" ? "3.61" : channel === "beta" ? "3.62-beta.2" : "3.63-alpha.5";
  const base = { ...FW_MODS.base, srv: { k: "update", v, desc: FW_NOTES } };
  const bt = { ...FW_MODS.bt, srv: { k: "actual", v: "1.14" } };
  const watch = { ...FW_MODS.watch, srv: { k: "none" }, unsupported: true };
  if (sc === "states") return [
    { ...FW_MODS.base, cur: "reading", srv: { k: "search" } },
    { ...FW_MODS.bt, srv: { k: "search" } },
    { ...FW_MODS.watch, cur: null, srv: { k: "error", err: "Сервер не ответил (503). Проверьте интернет и повторите." } },
  ];
  if (sc === "nodesc") return [{ ...base, srv: { ...base.srv, desc: "" } }, bt, watch];
  if (sc === "one") return [base];
  return [base, bt, watch];
}

// wide — горизонтальная карточка для окон ≥600 (раскладной, планшет-портрет, один модуль на широком окне):
// слева модуль и версия, справа обновление с началом описания — без узких колонок и переносов в кнопках.
function ModCard({ m, onDesc, onEnter, onRefresh, i, wide }) {
  const s = m.srv, upd = s.k === "update", busy = s.k === "search";
  const hl = upd && !m.unsupported;
  if (wide) return <ModCardWide m={m} onDesc={onDesc} onEnter={onEnter} onRefresh={onRefresh} i={i} />;
  return (
    <div className="fw-anim" style={{ animation: "fwIn .26s ease both", animationDelay: `${i * 50}ms`, boxSizing: "border-box", borderRadius: "var(--r-card-medium-radius)", padding: 16, background: "var(--background-surface-card)", border: hl ? "2px solid var(--primary)" : "1px solid var(--border-highlight)", margin: hl ? 0 : 1, display: "flex", flexDirection: "column", gap: 12, minWidth: 0 }}>
      <div style={{ display: "flex", alignItems: "flex-start", gap: 14 }}>
        <div style={{ width: 48, height: 48, borderRadius: "var(--r-card-small-radius)", flex: "0 0 auto", background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}><CmpIcon icon="memory" size={28} color="currentColor" /></div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-additional1-size)", fontWeight: 700, letterSpacing: ".06em", color: "var(--primary)", textTransform: "uppercase" }}>{L(m.type)}</div>
          <div style={wrapTxt}><CmpText variant="titleMedium">{N(m.name)}</CmpText></div>
        </div>
        {!m.unsupported && (
          <button type="button" onClick={onRefresh} aria-label={L("Проверить версию на сервере заново")} disabled={busy}
            style={{ width: 48, height: 48, margin: "-6px -8px -6px 0", border: "none", background: "transparent", borderRadius: 999, display: "grid", placeItems: "center", cursor: busy ? "default" : "pointer", color: "var(--on-surface-variant)", opacity: busy ? .38 : 1, flex: "0 0 auto" }}>
            <CmpIcon icon="refresh" size={26} color="currentColor" />
          </button>)}
      </div>

      {m.cur !== null && (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 }}>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Версия ПО")}</CmpText>
          {m.cur === "reading"
            ? <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Считывание версии…")}</CmpText><Spin size={18} /></span>
            : <Ver v={m.cur} size={17} />}
        </div>)}

      {m.unsupported ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10, color: "var(--error)" }}><CmpIcon icon="warning" size={24} color="currentColor" /><CmpText variant="titleSmall" color="var(--error)">{L("Не поддерживается")}</CmpText></div>
          <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Можно обновить в мобильном приложении Pandora Spec. Desktop версия пока не поддерживает обновление таких прошивок")}</CmpText>
        </div>
      ) : busy ? (
        <div style={{ display: "flex", alignItems: "center", gap: 10, minHeight: 48 }}><Spin /><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Поиск обновлений…")}</CmpText></div>
      ) : upd ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: 14, borderRadius: "var(--r-card-small-radius)", background: "var(--primary-container)", color: "var(--on-primary-container)" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8 }}>
            <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpIcon icon="arrow_circle_up" size={24} color="currentColor" /><CmpText variant="labelLarge" color="currentColor">{L("Доступно обновление")}</CmpText></span>
            <Ver v={s.v} size={18} color="currentColor" />
          </div>
          <div style={{ display: "flex", gap: 10 }}>
            <CmpOutlinedButton text={L("Описание")} onClick={onDesc} style={{ ...btn48, flex: 1, whiteSpace: "nowrap", padding: "0 12px" }} />
            <CmpButton text={L("Обновить")} icon="system_update" onClick={onEnter} style={{ ...btn48, flex: 1.4, whiteSpace: "nowrap" }} />
          </div>
        </div>
      ) : s.k === "actual" ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpIcon icon="check_circle" size={24} color="var(--primary)" /><CmpText variant="bodyMedium">{L("Актуальная")}</CmpText><Ver v={s.v} /></div>
          <CmpTextButton text={L("Переустановить")} onClick={onEnter} style={{ ...btn48, alignSelf: "flex-start" }} />
        </div>
      ) : s.k === "error" ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 10, padding: 14, borderRadius: "var(--r-card-small-radius)", background: "var(--error-container)", color: "var(--on-error-container)" }}>
          <div style={{ display: "flex", gap: 10 }}><CmpIcon icon="cloud_off" size={24} color="currentColor" /><CmpText variant="bodyMedium" color="currentColor">{L(s.err)}</CmpText></div>
          <CmpOutlinedButton text={L("Повторить")} icon="refresh" onClick={onRefresh} style={btn48} />
        </div>
      ) : null}
    </div>
  );
}

// Широкая карточка: шапка модуля во всю ширину (название не зажато), ниже — одна зона состояния.
// Версия показывается один раз; «Проверить заново» — иконка в шапке, как в compact-карточке.
function ModCardWide({ m, onDesc, onEnter, onRefresh, i }) {
  const s = m.srv, upd = s.k === "update", busy = s.k === "search";
  const hl = upd && !m.unsupported;
  const curVer = m.cur === null ? null : m.cur === "reading"
    ? <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Считывание версии…")}</CmpText><Spin size={18} /></span>
    : <span style={{ display: "inline-flex", alignItems: "baseline", gap: 8 }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Версия ПО")}</CmpText><Ver v={m.cur} size={18} /></span>;
  const row = (left, right) => <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 16, flexWrap: "wrap", minHeight: 48 }}>{left}{right}</div>;
  const status = m.unsupported ? (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      {row(<div style={{ display: "flex", alignItems: "center", gap: 10, color: "var(--error)" }}><CmpIcon icon="warning" size={26} color="currentColor" /><CmpText variant="titleSmall" color="var(--error)">{L("Не поддерживается")}</CmpText></div>, curVer)}
      <div style={{ maxWidth: 640 }}><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Можно обновить в мобильном приложении Pandora Spec. Desktop версия пока не поддерживает обновление таких прошивок")}</CmpText></div>
    </div>
  ) : busy ? row(
    <div style={{ display: "flex", alignItems: "center", gap: 10 }}><Spin /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Поиск обновлений…")}</CmpText></div>, curVer)
  : upd ? (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: 16, borderRadius: "var(--r-card-medium-radius)", background: "var(--primary-container)", color: "var(--on-primary-container)" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8, flexWrap: "wrap" }}>
        <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpIcon icon="arrow_circle_up" size={26} color="currentColor" /><CmpText variant="titleMedium" color="currentColor">{L("Доступно обновление")}</CmpText></span>
        <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><Ver v={m.cur} size={16} color="currentColor" /><CmpIcon icon="arrow_forward" size={18} color="currentColor" /><Ver v={s.v} size={20} color="currentColor" /></span>
      </div>
      {s.desc
        ? <div style={{ whiteSpace: "pre-line", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden", maxWidth: 720 }}><CmpText variant="bodyMedium" color="currentColor">{L(s.desc)}</CmpText></div>
        : <CmpText variant="bodyMedium" color="currentColor">{L("Описание отсутствует")}</CmpText>}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 10, flexWrap: "wrap" }}>
        {s.desc && <CmpOutlinedButton text={L("Полное описание")} onClick={onDesc} style={{ ...btn48, whiteSpace: "nowrap" }} />}
        <CmpButton text={L("Обновить")} icon="system_update" onClick={onEnter} style={{ height: 52, minWidth: 200, whiteSpace: "nowrap" }} />
      </div>
    </div>
  ) : s.k === "actual" ? row(
    <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpIcon icon="check_circle" size={26} color="var(--primary)" /><CmpText variant="titleSmall">{L("Актуальная")}</CmpText><Ver v={s.v} size={18} /></div>,
    <CmpOutlinedButton text={L("Переустановить")} onClick={onEnter} style={{ ...btn48, whiteSpace: "nowrap" }} />)
  : s.k === "error" ? (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      {curVer}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, flexWrap: "wrap", padding: 14, borderRadius: "var(--r-card-small-radius)", background: "var(--error-container)", color: "var(--on-error-container)" }}>
        <div style={{ display: "flex", gap: 10, flex: "1 1 280px" }}><CmpIcon icon="cloud_off" size={24} color="currentColor" /><CmpText variant="bodyMedium" color="currentColor">{L(s.err)}</CmpText></div>
        <CmpOutlinedButton text={L("Повторить")} icon="refresh" onClick={onRefresh} style={{ ...btn48, whiteSpace: "nowrap" }} />
      </div>
    </div>
  ) : null;
  return (
    <div className="fw-anim" style={{ animation: "fwIn .26s ease both", animationDelay: `${i * 50}ms`, boxSizing: "border-box", borderRadius: "var(--r-card-medium-radius)", padding: 20, background: "var(--background-surface-card)", border: hl ? "2px solid var(--primary)" : "1px solid var(--border-highlight)", margin: hl ? 0 : 1, display: "flex", flexDirection: "column", gap: 16, minWidth: 0 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
        <div style={{ width: 56, height: 56, borderRadius: "var(--r-card-medium-radius)", flex: "0 0 auto", background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}><CmpIcon icon="memory" size={32} color="currentColor" /></div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-additional1-size)", fontWeight: 700, letterSpacing: ".06em", color: "var(--primary)", textTransform: "uppercase" }}>{L(m.type)}</div>
          <div style={wrapTxt}><CmpText variant="titleLarge">{N(m.name)}</CmpText></div>
        </div>
        {!m.unsupported && (
          <button type="button" onClick={onRefresh} aria-label={L("Проверить версию на сервере заново")} title={L("Проверить заново")} disabled={busy}
            style={{ width: 48, height: 48, border: "none", background: "transparent", borderRadius: 999, display: "grid", placeItems: "center", cursor: busy ? "default" : "pointer", color: "var(--on-surface-variant)", opacity: busy ? .38 : 1, flex: "0 0 auto" }}>
            <CmpIcon icon="refresh" size={26} color="currentColor" />
          </button>)}
      </div>
      <div style={{ borderTop: "1px solid var(--border-highlight)", paddingTop: 16 }}>{status}</div>
    </div>
  );
}

function ChannelChip({ channel, onChange }) {
  const [open, setOpen] = React.useState(false);
  const ref = React.useRef(null);
  React.useEffect(() => {
    if (!open) return;
    const close = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, [open]);
  const test = channel !== "release";
  const cur = FW_CHANNELS.find((c) => c[0] === channel);
  return (
    <div ref={ref} style={{ position: "relative", flex: "0 0 auto" }}>
      <button type="button" aria-haspopup="listbox" aria-expanded={open} onClick={() => setOpen(!open)}
        style={{ height: 48, padding: "0 12px 0 14px", borderRadius: 999, border: test ? "none" : "1px solid var(--border-light)", cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 6, fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, whiteSpace: "nowrap",
          background: test ? "var(--tertiary-container)" : "transparent", color: test ? "var(--on-tertiary-container)" : "var(--on-surface-variant)" }}>
        {test && <CmpIcon icon="warning" size={18} color="currentColor" />}
        <span style={{ fontWeight: 500 }}>{L("Канал")}:</span> {L(cur[2])}
        <CmpIcon icon={open ? "expand_less" : "expand_more"} size={20} color="currentColor" />
      </button>
      {open && (
        <div role="listbox" aria-label={L("Тип канала обновления")} style={{ position: "absolute", top: "calc(100% + 4px)", right: 0, zIndex: 20, minWidth: 260, padding: "8px 0", borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-ultra-highlight)", boxShadow: "var(--elevation-2)" }}>
          {FW_CHANNELS.map(([k, name]) => (
            <button key={k} type="button" role="option" aria-selected={k === channel} onClick={() => { setOpen(false); k !== channel && onChange(k); }}
              style={{ display: "flex", alignItems: "center", gap: 10, width: "100%", minHeight: 48, padding: "0 16px", border: "none", cursor: "pointer", textAlign: "left", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", background: k === channel ? "var(--surface-variant)" : "transparent", color: "var(--on-surface)" }}>
              <span style={{ width: 20, display: "inline-flex" }}>{k === channel && <CmpIcon icon="check" size={20} color="var(--primary)" />}</span>{L(name)}
            </button>))}
        </div>)}
    </div>
  );
}

function ChannelPanel({ channel, onChange, layout }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      <div style={{ display: "flex", flexDirection: layout === "compact" ? "column" : "row", alignItems: layout === "compact" ? "stretch" : "center", justifyContent: "space-between", gap: 10 }}>
        <CmpText variant="titleSmall" color="var(--on-surface-variant)">{L("Тип канала обновления")}</CmpText>
        <div role="radiogroup" aria-label={L("Тип канала обновления")} style={{ display: "flex", padding: 4, gap: 4, borderRadius: 999, background: "var(--background-surface-highlight)", flex: layout === "compact" ? undefined : "0 0 380px" }}>
          {FW_CHANNELS.map(([k, , short]) => {
            const on = channel === k;
            return <button key={k} type="button" role="radio" aria-checked={on} onClick={() => onChange(k)}
              style={{ flex: 1, height: 48, border: "none", borderRadius: 999, cursor: "pointer", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, background: on ? "var(--primary)" : "transparent", color: on ? "var(--on-primary)" : "var(--on-surface)" }}>{L(short)}</button>;
          })}
        </div>
      </div>
      {channel !== "release" && <Banner icon="warning">{L(FW_TEST_WARN)}</Banner>}
    </div>
  );
}


// PIN программирования: нужен перед входом в загрузчик Bluetooth-модуля (и USB-сигнализаций). Демо-PIN: 1234.
function FwPin({ err0, onCancel, onOk }) {
  const [pin, setPin] = React.useState(err0 ? "0000" : "");
  const [left, setLeft] = React.useState(err0 ? 2 : 3);
  const [err, setErr] = React.useState(!!err0);
  const inputRef = React.useRef(null);
  React.useEffect(() => { inputRef.current && inputRef.current.focus(); }, []);
  const change = (e) => { setErr(false); setPin(e.target.value.replace(/\D/g, "").slice(0, 4)); };
  const submit = () => { if (pin === "1234") onOk(); else { setErr(true); setLeft((l) => Math.max(0, l - 1)); setPin(""); inputRef.current && inputRef.current.focus(); } };
  return (
    <CmpDialog open onDismissRequest={onCancel} icon="lock" title={L("Введите PIN-код программирования")} style={{ maxWidth: 400 }}
      text={<span style={{ display: "flex", flexDirection: "column", gap: 14, alignItems: "center" }}>
        <span>{L("PIN нужен для перехода блока в режим обновления")}</span>
        <span style={{ position: "relative", display: "flex", gap: 10 }}>
          {[0, 1, 2, 3].map((i) => <span key={i} aria-hidden="true" style={{ width: 56, height: 64, boxSizing: "border-box", borderRadius: "var(--r-card-small-radius)", display: "grid", placeItems: "center", fontSize: 30, color: "var(--on-surface)", border: err ? "2px solid var(--error)" : i === Math.min(pin.length, 3) ? "2px solid var(--primary)" : "1px solid var(--border-light)" }}>{i < pin.length ? "•" : ""}</span>)}
          <input ref={inputRef} value={pin} onChange={change} onKeyDown={(e) => { if (e.key === "Enter" && pin.length === 4 && left > 0) submit(); }}
            type="password" inputMode="numeric" pattern="[0-9]*" autoComplete="one-time-code" maxLength={4} aria-label={L("PIN-код")} disabled={left === 0}
            style={{ position: "absolute", inset: 0, width: "100%", height: "100%", opacity: 0, border: "none", cursor: "text", fontSize: "var(--t-paragraph2-size)" }} />
        </span>
        <span style={{ minHeight: 22, color: "var(--error)", fontWeight: 700 }} aria-live="polite">{err ? L("Неверный PIN-код. Осталось попыток:") + " " + left : ""}</span>
      </span>}
      dismissButton={<CmpTextButton text={L("Отмена")} onClick={onCancel} style={btn48} />}
      confirmButton={<CmpButton text={L("Продолжить")} disabled={pin.length < 4 || left === 0} onClick={submit} style={btn48} />} />
  );
}

function FwCheck({ layout, scenario, dialog0, channel0, onBack, onEnter }) {
  const [channel, setChannel] = React.useState(channel0);
  const [state, setState] = React.useState(scenario === "loading" ? "loading" : scenario === "error" ? "error" : "ready");
  const [dialog, setDialog] = React.useState(dialog0);
  const [target, setTarget] = React.useState(dialog0 === "pin" || dialog0 === "pinerr" ? "bt" : null);
  React.useEffect(() => { if (state !== "loading") return; const t = setTimeout(() => setState("ready"), 1600); return () => clearTimeout(t); }, [state]);
  const [mods, setMods] = React.useState(() => fwScenario(scenario, channel0));
  const changeCh = (c) => { setChannel(c); setMods(fwScenario("list", c).map((m) => m.unsupported ? m : { ...m, srv: { k: "search" } })); setTimeout(() => setMods(fwScenario("list", c)), 1200); };
  const refresh = (id) => { setMods((ms) => ms.map((m) => m.id === id ? { ...m, srv: { k: "search" } } : m)); setTimeout(() => setMods(fwScenario("list", channel)), 1200); };
  const upd = mods.find((m) => m.srv.k === "update");
  const pad = layout === "compact" ? 14 : 24;
  // Раскладка карточек: compact — столбик обычных; medium (раскладной 690, планшет-портрет) — столбик
  // широких горизонтальных; expanded — 2 колонки обычных, а если модуль один — одна широкая на всю строку.
  const wideCards = layout === "medium" || (layout === "expanded" && mods.length === 1);
  const grid2 = layout === "expanded" && mods.length > 1;
  let body;
  if (state === "loading") body = <Center><CmpCircularProgressIndicator /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Читаем список модулей блока…")}</CmpText></Center>;
  else if (state === "error") body = <Center><Hero icon="error_outline" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Не удалось получить список модулей")}</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Проверьте соединение с блоком и повторите попытку.")}</CmpText><CmpButton text={L("Повторить")} icon="refresh" fullWidth style={{ height: 52 }} onClick={() => setState("loading")} /></Center>;
  else body = (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: 1040, margin: "0 auto", padding: pad, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: "4px 8px", padding: "8px 8px 8px 14px", borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, flex: layout === "compact" ? "1 1 100%" : "1 1 0", minWidth: 0 }}>
            <CmpIcon icon="sensors" size={26} color="var(--primary)" />
            <div style={{ flex: 1, minWidth: 0, overflowWrap: "break-word" }}><CmpText variant="titleMedium">{N(FW_DEV.name)}</CmpText></div>
            {layout !== "compact" && <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", color: "var(--on-surface-variant)", flex: "0 0 auto" }}>ID {FW_DEV.id}</span>}
          </div>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8, flex: layout === "compact" ? "1 1 100%" : "0 0 auto" }}>
            {layout === "compact" && <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", color: "var(--on-surface-variant)", paddingLeft: 38 }}>ID {FW_DEV.id}</span>}
            <ChannelChip channel={channel} onChange={changeCh} />
          </div>
        </div>
        {channel !== "release" && <Banner icon="warning">{L(FW_TEST_WARN)}</Banner>}
        <CmpText variant="titleMedium">{L("Доступные для обновления модули")}</CmpText>
        <div style={{ display: "grid", gridTemplateColumns: grid2 ? "repeat(2, minmax(0,1fr))" : "minmax(0, 1fr)", gap: 14, alignItems: "start" }}>
          {mods.map((m, i) => <ModCard key={m.id} m={m} i={i} wide={wideCards} onDesc={() => setDialog("desc")} onEnter={() => { setTarget(m.id); setDialog("confirm"); }} onRefresh={() => refresh(m.id)} />)}
        </div>
      </div>
    </div>
  );
  const desc = upd && upd.srv.desc;
  return (
    <React.Fragment>
      <CmpTopAppBar title={L("Проверка прошивки")} subtitle={fwSub("single")} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={L("Назад")} onClick={onBack} />} />
      {body}
      {dialog === "desc" && upd && (
        <CmpDialog open onDismissRequest={() => setDialog(null)} title={L("Описание ПО")} style={{ maxWidth: 440 }}
          text={<span style={{ display: "flex", flexDirection: "column", gap: 10, maxHeight: 320, overflow: "auto" }}>
            <span>{L("Версия ПО")} <Ver v={upd.srv.v} /></span>
            <b style={{ color: "var(--on-surface)" }}>{L("Полное описание")}</b>
            <span style={{ whiteSpace: "pre-line" }}>{desc ? L(desc) : L("Описание отсутствует")}</span></span>}
          dismissButton={<CmpTextButton text={L("Отмена")} onClick={() => setDialog(null)} style={btn48} />}
          confirmButton={<CmpTextButton text={L("Обновить")} onClick={() => { setTarget(upd.id); setDialog("confirm"); }} style={btn48} />} />)}
      {dialog === "confirm" && (
        <CmpDialog open onDismissRequest={() => setDialog(null)} icon="power_settings_new" title={L("Перейти в режим обновления?")} style={{ maxWidth: 440 }}
          text={L("Блок перезагрузится, связь с ним пропадёт на несколько секунд. Обновление нужно довести до конца: пока блок в режиме обновления, он не работает как сигнализация. Не выключайте питание и не отключайте кабель.")}
          dismissButton={<CmpTextButton text={L("Отмена")} onClick={() => setDialog(null)} style={btn48} />}
          confirmButton={<CmpButton text={L("Перейти")} onClick={() => { if (target === "bt") setDialog("pin"); else { setDialog(null); onEnter(channel); } }} style={btn48} />} />)}
      {(dialog === "pin" || dialog === "pinerr") && <FwPin err0={dialog === "pinerr"} onCancel={() => setDialog(null)} onOk={() => { setDialog(null); onEnter(channel); }} />}
    </React.Fragment>
  );
}

// ════════ Экран 2. Обновление прошивки (загрузчик) ═══════════════════════════
const FW_PROFILES = {
  single: { title: "Обновление прошивки", sub: `${FW_DEV.name}  ${FW_DEV.id}`, subs: ["Pandora DX 4G (DX4G)"], ver: "3.57", next: "3.61" },
  multi:  { title: "Обновление прошивки", sub: `${FW_DEV.name}  ${FW_DEV.id}`, subs: ["Pandora DX 4G (DX4G)", "Основной процессор (MAIN)", "CAN-модуль (CAN2)"], ver: "3.57", next: "3.61" },
  bt:     { title: "Обновление прошивки", sub: "Bluetooth-модуль · Pandora BT (BTM2)", subs: ["Pandora BT (BTM2)"], ver: "1.14", next: "1.16" },
};
const STEPS = [["prepare", "Подготовка"], ["transfer", "Передача"], ["verify", "Проверка"], ["apply", "Применение"]];
const STATUS = {
  prepare: "Идёт процесс подготовки прошивки",
  transfer: "Идёт процесс обновления",
  verify: "Идёт проверка загруженной прошивки",
  apply: "Идёт применение прошивки",
};
const NEXT = { entering: ["source", 2200], download: ["confirm", 1800], prepare: ["transfer", 1600], verify: ["apply", 1500], apply: ["success", 1600] };

function Field({ label, children }) {
  return <div style={{ display: "flex", flexDirection: "column", gap: 6 }}><CmpText variant="labelLarge" color="var(--on-surface-variant)">{label}</CmpText>{children}</div>;
}

function FwUpdate({ layout, profile, phase0, channel0, reserve, live0 = false, nextVer, nextCan = null, blkCan = null, downgrade = false, onBack, onDone, onArchive }) {
  const P = FW_PROFILES[profile];
  const [phase, setPhase] = React.useState(phase0);
  const [live, setLive] = React.useState(live0);
  const [pct, setPct] = React.useState(64);
  const [sub, setSub] = React.useState(0);
  const [channel, setChannel] = React.useState(channel0);
  const [exit, setExit] = React.useState(false);
  const go = (p) => { if (p === "transfer" && live) setPct(0); setPhase(p); };
  const start = () => { setLive(true); setPhase("download"); };

  React.useEffect(() => {
    if (!live) return;
    const n = NEXT[phase]; if (!n) return;
    const t = setTimeout(() => go(n[0]), n[1]);
    return () => clearTimeout(t);
  }, [phase, live]);
  React.useEffect(() => {
    if (!live || phase !== "transfer") return;
    const t = setInterval(() => setPct((p) => Math.min(100, p + 4)), 140);
    return () => clearInterval(t);
  }, [phase, live]);
  React.useEffect(() => { if (live && phase === "transfer" && pct >= 100) setPhase("verify"); }, [pct]);
  React.useEffect(() => { if (phase !== "finishing") return; const t = setTimeout(onDone, 5000); return () => clearTimeout(t); }, [phase]);

  const running = STEPS.some(([k]) => k === phase);
  const locked = running || phase === "finishing" || phase === "download" || phase === "success" || phase === "entering";
  const stepIdx = STEPS.findIndex(([k]) => k === phase);
  const wide = layout !== "compact";
  const bt = profile === "bt";

  let hero = <Hero pulse={running || phase === "download" || phase === "entering" || phase === "finishing"} size={wide ? 220 : 128} />, content;
  if (phase === "success") hero = <Hero icon="check" size={wide ? 220 : 128} />;
  if (phase === "error" || phase === "lost") hero = <Hero icon={phase === "lost" ? "link_off" : "priority_high"} tone="error" size={wide ? 220 : 128} />;
  if (phase === "dataerror") hero = <Hero icon="memory" tone="neutral" size={wide ? 220 : 128} />;

  const verBlock = (cur, nxt) => (
    <div style={{ display: "flex", flexDirection: "column", gap: 4, padding: 16, borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", minHeight: 40 }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Текущая версия")}</CmpText><Ver v={cur} size={20} /></div>
      <div style={{ height: 1, background: "var(--outline-variant)" }} />
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", minHeight: 40 }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Найденная версия ПО")}</CmpText><Ver v={nxt} size={20} /></div>
    </div>);

  const warnVer = channel !== "release" && <Banner icon="warning">{L(FW_TEST_WARN)}</Banner>;
  const chName = L(FW_CHANNELS.find((c) => c[0] === channel)[1]);

  if (phase === "entering") content = <React.Fragment>
    <CmpText variant="headlineSmall">{L("Блок перезагружается в режим обновления")}</CmpText>
    <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Связь пропадёт на несколько секунд и вернётся сама. Ничего нажимать не нужно.")}</CmpText>
    <div style={{ display: "flex", alignItems: "center", gap: 12 }}><Spin size={24} /><CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("Ждём возвращения блока…")}</CmpText></div>
    <Banner icon="power_settings_new">{L(FW_POWER)}</Banner></React.Fragment>;
  else if (phase === "dataerror") content = <React.Fragment>
    <CmpText variant="bodyLarge" color="var(--on-surface-variant)" style={{ textAlign: "center" }}>{L("Ошибка. Не найдена информация о подсистемах устройства")}</CmpText>
    <CmpButton text={L("Повторить")} icon="refresh" fullWidth style={{ height: 52 }} onClick={() => setPhase("source")} /></React.Fragment>;
  else if (phase === "source") content = <React.Fragment>
    <Field label={L("Подсистема:")}>
      {P.subs.length === 1
        ? <div style={wrapTxt}><CmpText variant="titleMedium" color="var(--primary)">{N(P.subs[0])}</CmpText></div>
        : <CmpDropdown selectedValue={N(P.subs[sub])} options={P.subs.map(N)} onOptionSelected={(i) => setSub(i)} />}
    </Field>
    <Field label={L("Версия ПО:")}><Ver v={P.ver} size={20} /></Field>
    {!bt && <Field label={L("CAN-прошивка:")}>
      {blkCan
        ? <span style={{ display: "inline-flex", alignItems: "baseline", flexWrap: "wrap", gap: "4px 12px" }}><Ver v={"CAN " + blkCan.can} size={20} /><span style={{ font: "400 16px var(--font-sans)", color: "var(--on-surface-variant)" }}>{L("профиль")} <b style={{ fontFamily: "var(--font-mono)", color: "var(--on-surface)" }}>{blkCan.prof}</b></span></span>
        : <CmpText variant="bodyLarge">{L("Заводские CAN-настройки")}</CmpText>}
    </Field>}
    <Field label={L("Тип канала обновления:")}>
      <CmpDropdown selectedValue={chName} options={FW_CHANNELS.map((c) => L(c[1]))} onOptionSelected={(i) => setChannel(FW_CHANNELS[i][0])} />
    </Field>
    {warnVer}
    <CmpButton text={L("Скачать прошивку")} icon="download" fullWidth style={{ height: 52, marginTop: 4 }} onClick={start} />
    <CmpOutlinedButton text={L("Архив прошивок")} icon="history" fullWidth style={{ height: 52 }} onClick={onArchive} />
    <div aria-hidden={!reserve} style={{ display: "flex", flexDirection: "column", gap: 10, visibility: reserve ? "visible" : "hidden" }}>
      <CmpOutlinedButton text={L("Файловый менеджер")} icon="folder_open" fullWidth style={{ height: 52 }} disabled={!reserve} />
    </div></React.Fragment>;
  else if (phase === "download") content = <React.Fragment>
    <CmpText variant="headlineSmall">{L("Скачивание прошивки")}</CmpText>
    <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Ожидайте")}</CmpText><Spin size={40} /></React.Fragment>;
  else if (phase === "confirm") content = <React.Fragment>
    <CmpText variant="headlineSmall" style={{ textAlign: "center" }}>{L("Подтвердите версию")}</CmpText>
    {verBlock(P.ver, nextVer || P.next)}
    {nextCan && (() => {
      const same = !!blkCan && nextCan.can === blkCan.can;
      return (
        <div style={{ width: "100%", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 6, padding: 16, borderRadius: "var(--r-card-medium-radius)", textAlign: "left",
          background: same ? "var(--background-surface-card)" : "var(--secondary-container)", color: same ? "var(--on-surface)" : "var(--on-secondary-container)", border: same ? "1px solid var(--border-highlight)" : "2px solid var(--primary)" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 12, flexWrap: "wrap", minHeight: 40 }}>
            <CmpText variant="bodyLarge" color="currentColor">{L("CAN блока")}</CmpText>
            <span style={{ display: "inline-flex", alignItems: "center", flexWrap: "wrap", gap: 8, fontFamily: "var(--font-mono)", fontSize: "var(--t-headline2-size)", fontWeight: 700 }}>
              <span>{blkCan ? blkCan.can : L("заводские")}</span>
              {!same && <React.Fragment><CmpIcon icon="arrow_forward" size={20} color="currentColor" /><span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 500 }}>{L("будет")}</span><span>{nextCan.can}</span></React.Fragment>}
            </span>
          </div>
          <CmpText variant="bodyMedium" color="currentColor">{same ? L("Версия собрана под CAN-код блока") : L("Блок будет прошит под другой автомобиль. Это допустимо — например, при переносе блока на другую машину.")} · {L("профиль")} {nextCan.prof}</CmpText>
        </div>);
    })()}
    <div style={{ padding: 14, borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)", textAlign: "left", width: "100%", boxSizing: "border-box", display: "flex", flexDirection: "column", minHeight: 0 }}>
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("Что нового")}</CmpText>
      <div style={{ whiteSpace: "pre-line", marginTop: 4, maxHeight: "min(220px, 30vh)", overflowY: "auto" }} tabIndex={0}><CmpText variant="bodyMedium">{L(FW_NOTES)}</CmpText></div>
    </div>
    {downgrade && <Banner icon="warning">{L("Выбрана версия старее установленной")}</Banner>}
    {warnVer}
    <div style={{ display: "flex", gap: 12, width: "100%", position: "sticky", bottom: 0, padding: "12px 0 4px", background: "var(--background)", zIndex: 2 }}>
      <CmpButton text={L("Обновить")} style={{ height: 52, flex: 1 }} onClick={() => { setLive(true); setPct(0); setPhase("prepare"); }} />
      <CmpOutlinedButton text={L("Отмена")} style={{ height: 52, flex: 1 }} onClick={() => setPhase("source")} />
    </div></React.Fragment>;
  else if (running) content = (
    <div style={{ display: "flex", flexDirection: "column", gap: 18, width: "100%" }}>
      <div style={{ display: "flex", gap: 6 }} aria-hidden="true">
        {STEPS.map(([k, l], i) => (
          <div key={k} style={{ flex: 1, display: "flex", flexDirection: "column", gap: 6 }}>
            <div style={{ height: 6, borderRadius: 3, background: i <= stepIdx ? "var(--primary)" : "var(--surface-variant)" }} />
            <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: i === stepIdx ? 700 : 500, color: i === stepIdx ? "var(--on-surface)" : "var(--on-surface-variant)" }}>{L(l)}</span>
          </div>))}
      </div>
      <div aria-live="polite" style={{ textAlign: "center" }}>
        <CmpText variant="titleLarge">{L(STATUS[phase])}</CmpText>
        <div style={{ height: 56, display: "grid", placeItems: "center" }}>
          {phase === "transfer" && pct != null
            ? <span style={{ fontFamily: "var(--font-mono)", fontSize: 40, fontWeight: 700, color: "var(--primary)" }}>{pct}%</span>
            : <span style={{ opacity: 0 }}>.</span>}
        </div>
      </div>
      <CmpLinearProgressIndicator progress={phase === "transfer" ? pct / 100 : null} height={12} />
      <Banner icon="power_settings_new">{L(FW_POWER)}</Banner>
    </div>);
  else if (phase === "success") content = <React.Fragment>
    <CmpText variant="headlineSmall">{L("Обновление устройства завершено")}</CmpText>
    <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Блок перезагрузится, связь пропадёт на несколько секунд. Приложение само вернётся в расширенный монтаж.")}</CmpText>
    <CmpButton text="OK" fullWidth style={{ height: 52 }} onClick={() => setPhase("finishing")} /></React.Fragment>;
  else if (phase === "finishing") content = <React.Fragment>
    <CmpText variant="headlineSmall">{L("Завершаем обновление")}</CmpText>
    <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Ожидаем блок. Это займёт около 5 секунд, затем откроется расширенный монтаж.")}</CmpText><Spin size={40} /></React.Fragment>;
  else if (phase === "error") content = <React.Fragment>
    <CmpText variant="headlineSmall">{L("Обновление не выполнено")}</CmpText>
    <Banner icon="error" tone="error">{L("Ошибка записи: блок не подтвердил приём данных (код 0x1A40)")}</Banner>
    <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Блок остался в режиме обновления. Повторите попытку: прошивка скачается заново.")}</CmpText>
    <CmpButton text={L("Повторить")} icon="refresh" fullWidth style={{ height: 52 }} onClick={() => setPhase("source")} />
    <CmpOutlinedButton text={L("Написать в поддержку")} icon="support_agent" fullWidth style={{ height: 52 }} /></React.Fragment>;
  else if (phase === "lost") content = <React.Fragment>
    <CmpText variant="headlineSmall">{L("Связь с блоком прервана")}</CmpText>
    <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Обновление остановлено, блок остался в режиме обновления. Проверьте питание и соединение, переподключитесь и повторите обновление.")}</CmpText>
    <CmpButton text={L("Переподключиться")} icon="sync" fullWidth style={{ height: 52 }} onClick={() => setPhase("entering")} />
    <CmpOutlinedButton text={L("Отключиться")} icon="link_off" fullWidth style={{ height: 52 }} onClick={onBack} /></React.Fragment>;

  const voice = (
    <div aria-hidden={!reserve} style={{ width: 48, height: 48, display: "grid", placeItems: "center", color: "var(--primary)", visibility: reserve && phase === "source" ? "visible" : "hidden" }}>
      <CmpIcon icon="record_voice_over" size={28} color="currentColor" /></div>);
  const nav = locked ? <div style={{ width: 48, height: 48, display: "grid", placeItems: "center", color: "var(--on-surface-variant)", opacity: .6 }} title={L("Выход заблокирован до завершения")}><CmpIcon icon="lock" size={22} color="currentColor" /></div>
    : <CmpIconButton icon="ArrowBack" contentDescription={L("Назад")} onClick={() => (phase === "source" || phase === "dataerror" || phase === "confirm") ? setExit(true) : onBack()} />;

  return (
    <React.Fragment>
      <CmpTopAppBar title={P.title} subtitle={fwSub(profile)} navigationIcon={nav} actions={voice} />
      <div style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex", flexDirection: wide ? "row" : "column", alignItems: "stretch" }}>
        <div style={{ flex: wide ? "1 1 0" : "0 0 auto", display: "grid", placeItems: "center", padding: wide ? 24 : "20px 16px 4px" }}>{hero}</div>
        <div style={{ flex: wide ? "1 1 0" : "1 0 auto", display: "flex", alignItems: wide ? "center" : "flex-start", justifyContent: "center", padding: wide ? "24px 32px 24px 8px" : "12px 16px 24px", boxSizing: "border-box", minWidth: 0 }}>
          <div style={{ width: "100%", maxWidth: 480, display: "flex", flexDirection: "column", gap: 16, alignItems: phase === "source" ? "stretch" : "center", textAlign: phase === "source" ? "left" : "center" }}>{content}</div>
        </div>
      </div>
      {exit && (
        <CmpDialog open onDismissRequest={() => setExit(false)} icon="warning" title={L("Выйти из режима обновления?")} style={{ maxWidth: 440 }}
          text={L("Блок останется в режиме обновления и не будет работать как сигнализация, пока прошивка не установлена или режим не отключён.")}
          dismissButton={<CmpTextButton text={L("Остаться")} onClick={() => setExit(false)} style={btn48} />}
          confirmButton={<CmpButton text={L("Выйти")} onClick={() => { setExit(false); onBack(); }} style={btn48} />} />)}
    </React.Fragment>
  );
}


// ════════ Экран 3. Архив прошивок ════════════════════════════════════════════
// Вход А — из «Обновление прошивки» (блок подключён): CAN-поле заполнено кодом блока, фильтр по коду и профилю.
// Вход Б — с главного экрана без блока: модель (экран K2 конфигуратора) → архив с моделью и каналом в шапке,
// «Скачать» / «Поделиться» вместо «Установить». У Bluetooth-модуля CAN-фильтра нет.
const FW_VERS = [
  { v: "3.61", d: "14.10.2026", can: "2154", prof: "1.03", desc: FW_NOTES },
  { v: "3.61", d: "14.10.2026", can: "3312", prof: "1.01", desc: FW_NOTES },
  { v: "3.60", d: "02.09.2026", can: "2154", prof: "1.03", desc: "• Исправлена ошибка постановки на охрану при низком заряде АКБ\n• Ускорено подключение по Bluetooth" },
  { v: "3.60", d: "02.09.2026", can: "2154", prof: "1.02", desc: "• Исправлена ошибка постановки на охрану при низком заряде АКБ\n• Ускорено подключение по Bluetooth" },
  { v: "3.58", d: "19.07.2026", desc: "" },
  { v: "3.57", d: "30.05.2026", can: "2154", prof: "1.03", desc: "• Первая версия с поддержкой профилей CAN 2.0\n• Оптимизировано энергопотребление в режиме охраны\n• Исправлены ошибки обработки датчика удара\n• Улучшена работа автозапуска при низких температурах\n• Обновлён протокол обмена с модулем часов" },
  { v: "3.57", d: "30.05.2026", can: "0188", prof: "1.00", desc: "• Первая версия с поддержкой профилей CAN 2.0" },
  { v: "3.55", d: "11.04.2026", can: "3312", prof: "1.01", desc: "• Улучшена стабильность связи по Bluetooth" },
  { v: "3.52", d: "03.02.2026", can: "2154", prof: "1.01", desc: "" },
];
const FW_BT_VERS = [
  { v: "1.16", d: "01.10.2026", desc: "• Улучшена стабильность связи по Bluetooth" },
  { v: "1.14", d: "12.06.2026", desc: "" },
  { v: "1.12", d: "20.03.2026", desc: "" },
];
const FW_INSTALLED = "3.57", FW_BT_INSTALLED = "1.14";
const FW_BLOCK_CAN = { can: "2154", prof: "1.03" };
const FW_CARS = [{ name: "Kia Rio 2021", can: "0188" }, { name: "Hyundai Solaris 2020", can: "3312" }, { name: "Toyota Camry 2019", can: "0342" }];
// Список моделей — тот же, что у K2 «Модель блока» в конфигураторе (cfg-data.js).
const FW_MODELS = ["DX-9X", "DX-9X LoRa", "DX-9X Mini", "DX-91", "DX-91 LoRa", "DX-90B", "DX-90BT", "DX-6X", "DX-4GS", "DX-4G", "DX-40RS", "DX-40S", "DX-40R",
  "DXL-5000", "DXL-4950", "DXL-4910", "X-1900 BT", "X-1800L", "X-1700", "X-3190L", "X-3110", "VX-4G GPS", "VX-3100", "VX-1000", "NAV-10", "NAV-08", "NAV-12",
  "SMART PRO v3", "SMART v2", "Light Pro v2", "Light Pro", "BT-760", "BT-100", "IMMO-9", "IMMO-5", "CAN-6"];
const FW_RECENT = ["DX-9X", "DX-4GS", "X-1900 BT"];
const vid = (it) => it.v + "|" + (it.can || "-") + "|" + (it.prof || "-");
const vnum = (v) => parseFloat(v);

function fwCanInit(sc, entry) {
  if (entry === "b") {
    const f = { none: { can: "", src: "none" }, car: { can: "0188", src: "car", car: FW_CARS[0].name }, other: { can: "3312", src: "manual" }, partial: { can: "33", src: "manual" }, emptyother: { can: "0342", src: "car", car: FW_CARS[2].name } }[sc];
    return { blk: null, f: f || { can: "", src: "none" } };
  }
  const blk = sc === "factory" ? null : sc === "emptyblock" ? { can: "4410", prof: "1.00" } : FW_BLOCK_CAN;
  const f = { block: { can: "2154", src: "block" }, other: { can: "3312", src: "manual" }, car: { can: "0188", src: "car", car: FW_CARS[0].name }, cleared: { can: "", src: "manual" }, partial: { can: "21", src: "manual" }, factory: { can: "", src: "none" }, emptyblock: { can: "4410", src: "block" }, emptyother: { can: "9999", src: "manual" } }[sc];
  return { blk, f: f || { can: "2154", src: "block" } };
}
function fwFilter(list, f, blk) {
  if (f.can.length !== 4) return list;
  if (f.src === "block" && blk) return list.filter((x) => x.can === blk.can && x.prof === blk.prof);
  return list.filter((x) => x.can === f.can);
}

function VerBadge({ kind }) {
  const inst = kind === "installed";
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 4, height: 26, padding: "0 10px", borderRadius: 999, boxSizing: "border-box", fontFamily: "var(--font-sans)", fontSize: "var(--t-additional0-size)", fontWeight: 700, whiteSpace: "nowrap", background: inst ? "var(--primary)" : "var(--surface-variant)", color: inst ? "var(--on-primary)" : "var(--on-surface-variant)" }}>{inst && <CmpIcon icon="check" size={16} color="currentColor" />}{inst ? L("Установлена") : L("Старее установленной")}</span>;
}

// Метка CAN в строке: под код блока — акцент + «как в блоке»; другой код — приглушённая; без CAN — нет метки.
function CanTag({ it, blk }) {
  if (!it.can) return null;
  const mine = !!blk && it.can === blk.can;
  return (
    <span style={{ display: "inline-flex", alignItems: "center", flexWrap: "wrap", gap: 6, minHeight: 30, padding: "2px 10px", borderRadius: 8, boxSizing: "border-box", fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", fontWeight: 700,
      background: mine ? "var(--secondary-container)" : "transparent", color: mine ? "var(--on-secondary-container)" : "var(--on-surface-variant)", border: mine ? "2px solid var(--primary)" : "1px solid var(--border-highlight)" }}>
      {mine && <CmpIcon icon="link" size={18} color="currentColor" />}
      <span>CAN {it.can} · {it.prof}</span>
      {mine && <span style={{ fontFamily: "var(--font-sans)", fontWeight: 700 }}>· {L("как в блоке")}</span>}
    </span>
  );
}

function FwIconBtn({ icon, label, onClick, color = "var(--on-surface-variant)" }) {
  return <button type="button" className="fw-hov" aria-label={label} title={label} onClick={onClick}
    style={{ width: 48, height: 48, flex: "0 0 auto", border: "none", background: "transparent", borderRadius: 999, display: "grid", placeItems: "center", cursor: "pointer", color }}><CmpIcon icon={icon} size={24} color="currentColor" /></button>;
}

function CanField({ f, setF, blk, entry, onCatalog, onReset }) {
  const [focus, setFocus] = React.useState(false);
  const partial = f.can.length > 0 && f.can.length < 4;
  const auto = entry === "a" && !!blk && f.src === "block" && f.can === blk.can;
  const differs = entry === "a" && !!blk && !auto;
  const border = partial ? "var(--error)" : focus ? "var(--primary)" : "var(--outline)";
  const line = (icon, text, color = "var(--on-surface-variant)") => (
    <div style={{ display: "flex", alignItems: "flex-start", gap: 10, padding: "0 4px" }}>
      <CmpIcon icon={icon} size={20} color={color} />
      <div style={{ flex: 1, minWidth: 0 }}><CmpText variant="bodyMedium" color={color}>{text}</CmpText></div>
    </div>);
  const carLine = f.src === "car" && f.car && f.can.length === 4 && line("directions_car", <b style={{ color: "var(--on-surface)" }}>{f.car}</b>, "var(--primary)");
  let help = null;
  if (partial) help = line("error", L("Введите 4 цифры CAN-кода"), "var(--error)");
  else if (entry === "b") help = carLine || (!f.can && line("info", L("Выберите автомобиль, чтобы увидеть прошивки под его CAN")));
  else if (!blk) help = <React.Fragment>{carLine}{line("info", f.can ? L("У блока заводские CAN-настройки") : L("У блока заводские CAN-настройки — выберите автомобиль или введите CAN-код"))}</React.Fragment>;
  else if (auto) help = line("memory", <span><b>{L("CAN-код блока")}</b> · {L("профиль")} {blk.prof} — {L("подставлен автоматически")}</span>, "var(--primary)");
  else help = <React.Fragment>{carLine}{!f.can && line("filter_alt_off", L("Фильтр снят — показаны все версии"))}</React.Fragment>;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      <label htmlFor="fw-can" style={{ padding: "0 4px" }}><CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("CAN-код автомобиля")}</CmpText></label>
      <div style={{ display: "flex", alignItems: "center", gap: 2, minHeight: 60, padding: "0 4px 0 14px", borderRadius: "var(--r-card-small-radius)", boxSizing: "border-box", border: `${focus || partial ? 2 : 1}px solid ${border}`, margin: focus || partial ? 0 : 1, background: auto ? "var(--background-surface-highlight)" : "var(--surface)" }}>
        <CmpIcon icon={auto ? "memory" : "tag"} size={22} color={auto ? "var(--primary)" : "var(--on-surface-variant)"} />
        <input id="fw-can" value={f.can} inputMode="numeric" pattern="[0-9]*" maxLength={4} placeholder={L("4 цифры")} autoComplete="off"
          onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
          onChange={(e) => setF({ can: e.target.value.replace(/\D/g, "").slice(0, 4), src: "manual" })}
          style={{ flex: 1, minWidth: 0, height: 56, padding: "0 8px", border: "none", outline: "none", background: "transparent", fontFamily: "var(--font-mono)", fontSize: "var(--t-headline1-size)", fontWeight: 700, letterSpacing: ".12em", color: "var(--on-surface)" }} />
        {f.can && <FwIconBtn icon="close" label={L("Очистить")} onClick={() => setF({ can: "", src: entry === "b" ? "none" : "manual" })} />}
        <FwIconBtn icon="directions_car" label={L("Выбрать автомобиль из каталога")} onClick={onCatalog} color="var(--primary)" />
      </div>
      {help}
      {differs && (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: "0 8px", padding: "2px 4px 2px 12px", borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)" }}>
          <span style={{ display: "inline-flex", alignItems: "center", gap: 8, minHeight: 48 }}><CmpIcon icon="memory" size={20} color="var(--on-surface-variant)" /><CmpText variant="bodyMedium">{L("CAN-код блока —")}</CmpText><Ver v={blk.can} size={16} /></span>
          <CmpTextButton text={L("Вернуть CAN блока")} onClick={onReset} style={btn48} />
        </div>)}
    </div>
  );
}

function VerRow({ it, kind, wide, sel, onSelect, onPick, onShare, busy, blk, entry, i }) {
  const [open, setOpen] = React.useState(false);
  const has = !!it.desc;
  const clamp = open ? { maxHeight: 200, overflowY: "auto" } : { display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden" };
  // Узкие окна: действие — отдельной строкой во всю ширину карточки (одинаково с описанием и без), не прыгает при переносе.
  const actions = !wide && (entry === "b"
    ? <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <CmpOutlinedButton text={busy ? L("Скачиваем…") : L("Скачать")} icon="download" disabled={busy} onClick={onPick} style={{ ...btn48, flex: 1, minWidth: 0, whiteSpace: "nowrap" }} />
        <FwIconBtn icon="share" label={L("Поделиться")} onClick={onShare} />
      </div>
    : <CmpOutlinedButton text={L("Выбрать")} onClick={onPick} fullWidth style={{ ...btn48, width: "100%" }} />);
  return (
    <div className="fw-anim" onClick={wide ? onSelect : undefined} role={wide ? "button" : undefined} tabIndex={wide ? 0 : undefined}
      style={{ animation: "fwIn .26s ease both", animationDelay: `${i * 40}ms`, boxSizing: "border-box", borderRadius: "var(--r-card-medium-radius)", padding: 16, cursor: wide ? "pointer" : "default", display: "flex", flexDirection: "column", gap: 10, minWidth: 0,
        background: sel ? "var(--primary-container)" : "var(--background-surface-card)", color: sel ? "var(--on-primary-container)" : "var(--on-surface)",
        border: sel || kind === "installed" ? "2px solid var(--primary)" : "1px solid var(--border-highlight)", margin: sel || kind === "installed" ? 0 : 1 }}>
      <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 8, justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 10, minWidth: 0 }}>
          <Ver v={it.v} size={24} color={sel ? "currentColor" : "var(--primary)"} />
          {kind !== "new" && <VerBadge kind={kind} />}
        </div>
        <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", color: sel ? "currentColor" : "var(--on-surface-variant)" }}>{it.d}</span>
      </div>
      {it.can && <div style={{ display: "flex" }}><CanTag it={it} blk={blk} /></div>}
      {has && (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
          <div style={{ whiteSpace: "pre-line", ...clamp }}><CmpText variant="bodyMedium" color={sel ? "currentColor" : "var(--on-surface-variant)"}>{L(it.desc)}</CmpText></div>
          <CmpTextButton text={open ? L("Свернуть") : L("Показать больше")} onClick={(e) => { e && e.stopPropagation && e.stopPropagation(); setOpen(!open); }} style={{ ...btn48, marginLeft: -12, whiteSpace: "nowrap" }} />
        </div>)}
      {actions}
    </div>
  );
}

// CAN выбранной версии в правой панели широкого окна.
function CanInfo({ it, blk, entry }) {
  const mine = !!blk && it.can === blk.can;
  const pill = (on, text) => <span style={{ display: "inline-flex", alignItems: "center", gap: 4, height: 28, padding: "0 12px", borderRadius: 999, font: "600 13px var(--font-sans)", whiteSpace: "nowrap", background: on ? "var(--primary)" : "transparent", color: on ? "var(--on-primary)" : "var(--on-surface)", border: on ? "none" : "1px solid var(--border-light)" }}>{on && <CmpIcon icon="link" size={16} color="currentColor" />}{text}</span>;
  let note = null;
  if (entry === "a" && it.can) {
    if (!blk) note = L("У блока заводские CAN-настройки. После установки блок будет прошит под") + ` CAN ${it.can}.`;
    else if (!mine) note = `${L("Блок сейчас прошит под")} CAN ${blk.can} · ${blk.prof}. ${L("После установки будет")} CAN ${it.can} · ${it.prof}.`;
    else if (it.prof !== blk.prof) note = `${L("Код как в блоке, профиль в блоке —")} ${blk.prof}.`;
  }
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, padding: 16, borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)" }}>
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("CAN-прошивка")}</CmpText>
      {it.can ? (
        <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: "6px 14px" }}>
          <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-headline1-size)", fontWeight: 700 }}>CAN {it.can}</span>
          <span style={{ font: "400 16px var(--font-sans)", color: "var(--on-surface-variant)" }}>{L("профиль")} <b style={{ fontFamily: "var(--font-mono)", color: "var(--on-surface)" }}>{it.prof}</b></span>
          {entry === "a" && blk && pill(mine, mine ? L("как в блоке") : L("другой CAN"))}
        </div>
      ) : <CmpText variant="bodyLarge">{L("Без привязки к CAN")}</CmpText>}
      {note && <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{note}</CmpText>}
    </div>
  );
}

// Шапка архива без блока: модель (нажимается — смена модели), канал, модуль.
function FwContext({ model, onModel, channel, setChannel, mod, setMod }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 8, padding: 6, borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
        <button type="button" className="fw-hov" onClick={onModel} aria-label={L("Сменить модель блока")} title={L("Сменить модель блока")}
          style={{ flex: "1 1 260px", minWidth: 0, minHeight: 56, display: "flex", alignItems: "center", gap: 12, padding: "0 10px", border: "none", borderRadius: "var(--r-card-small-radius)", background: "transparent", cursor: "pointer", textAlign: "left", color: "var(--on-surface)" }}>
          <CmpIcon icon="memory" size={26} color="var(--primary)" />
          <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
            <span style={{ font: "500 13px var(--font-sans)", color: "var(--on-surface-variant)" }}>{L("Модель блока")}</span>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-headline2-size)", fontWeight: 700, overflowWrap: "break-word", textWrap: "pretty" }}>{model}</span>
          </span>
          <CmpIcon icon="chevron_right" size={28} color="var(--primary)" />
        </button>
        <ChannelChip channel={channel} onChange={setChannel} />
      </div>
      <div role="radiogroup" aria-label={L("Модуль")} style={{ display: "flex", padding: 4, gap: 4, borderRadius: 999, background: "var(--background-surface-highlight)" }}>
        {[["single", "Основная программа"], ["bt", "Bluetooth-модуль"]].map(([k, l]) => {
          const on = mod === k;
          return <button key={k} type="button" role="radio" aria-checked={on} onClick={() => !on && setMod(k)}
            style={{ flex: 1, minHeight: 48, padding: "0 10px", border: "none", borderRadius: 999, cursor: "pointer", fontFamily: "var(--font-sans)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, background: on ? "var(--primary)" : "transparent", color: on ? "var(--on-primary)" : "var(--on-surface)" }}>{L(l)}</button>;
        })}
      </div>
    </div>
  );
}

function FwArchive({ layout, scenario, channel: ch0, dialog0, entry = "a", canSc = "block", module0 = "single", model = "DX-4GS", platform = "mobile", onBack, onPick, onModel, onChannel, onModule }) {
  const init = React.useMemo(() => fwCanInit(canSc, entry), []);
  const blk = entry === "a" ? init.blk : null;
  const [f, setF] = React.useState(init.f);
  const [mod, setModRaw] = React.useState(module0);
  const [channel, setChRaw] = React.useState(ch0);
  const noFw = scenario === "nofw" || (entry === "b" && model === "IMMO-5");
  const [state, setState] = React.useState(noFw ? "nofw" : scenario === "loading" ? "loading" : (scenario === "rollback" || scenario === "dl") ? "list" : scenario);
  const fast = React.useRef(false);
  const [dialog, setDialog] = React.useState(dialog0 ? FW_VERS.find((x) => x.v === "3.55") : null);
  const [cat, setCat] = React.useState(false);
  const [dl, setDl] = React.useState(scenario === "dl" ? { id: vid(FW_VERS[0]), it: FW_VERS[0], done: true } : null);
  const [toast, setToast] = React.useState(null);
  const [selId, setSelId] = React.useState(dialog0 ? null : null);
  const wide = layout === "expanded";
  const bt = mod === "bt";
  React.useEffect(() => { if (state !== "loading") return; const t = setTimeout(() => { fast.current = false; setState(noFw ? "nofw" : "list"); }, fast.current ? 700 : 1500); return () => clearTimeout(t); }, [state]);
  React.useEffect(() => { if (!toast) return; const t = setTimeout(() => setToast(null), 3500); return () => clearTimeout(t); }, [toast]);
  const reload = () => { fast.current = true; setState("loading"); };
  const setFilter = (nf) => {
    const f2 = { car: null, ...nf };
    if (entry === "a" && blk && f2.can === blk.can && f2.src !== "car") f2.src = "block";
    const changed = f2.can !== f.can || f2.src !== f.src;
    setF(f2);
    if (changed && (f2.can.length === 4 || f2.can.length === 0) && state !== "nofw") reload();
  };
  const setMod = (m) => { setModRaw(m); onModule && onModule(m); if (state !== "nofw") reload(); };
  const setChannel = (c) => { setChRaw(c); onChannel && onChannel(c); if (state !== "nofw") reload(); };

  const base = bt ? FW_BT_VERS : FW_VERS;
  const inst = bt ? FW_BT_INSTALLED : FW_INSTALLED;
  const list0 = bt ? base : fwFilter(base, f, blk);
  const isBlockBuild = (it) => !!blk && it.can === blk.can && it.prof === blk.prof;
  const kindOf = (it) => entry === "b" ? "new" : (it.v === inst && (bt || isBlockBuild(it))) ? "installed" : vnum(it.v) < vnum(inst) ? "older" : "new";
  const listOn = state === "list" || state === "more" || state === "moreerror";
  const list = listOn ? list0 : [];
  const sel = list.find((x) => vid(x) === selId) || list[0];
  const fname = (it) => `${model}${bt ? "_BT" : ""}_${it.v}${it.can ? "_CAN" + it.can + "-" + it.prof : ""}.bin`;
  const download = (it) => { const id = vid(it); setDl({ id, it, done: false }); setTimeout(() => setDl((d) => d && d.id === id ? { ...d, done: true } : d), 1200); };
  const share = (it) => setToast(L("Откроется системное меню «Поделиться»") + ": " + fname(it));
  const pick = (it) => { if (entry === "b") return download(it); kindOf(it) === "older" ? setDialog(it) : onPick(it, false, blk); };
  const resetBlk = () => blk && setFilter({ can: blk.can, src: "block" });
  const pad = layout === "compact" ? 14 : 24;
  const btn52 = { height: 52 };

  let stateEl = null;
  if (state === "loading") stateEl = <Center><CmpCircularProgressIndicator /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Загружаем список версий…")}</CmpText></Center>;
  else if (state === "empty") stateEl = <Center><Hero icon="inventory_2" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Для данного устройства не найдено прошивок. Попробуйте позже")}</CmpText><CmpOutlinedButton text={L("Обновить")} icon="refresh" fullWidth style={btn52} onClick={() => setState("loading")} /></Center>;
  else if (state === "nofw") stateEl = <Center><Hero icon="inventory_2" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Для модели")} {model} {L("прошивок нет")}</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Проверьте модель на наклейке блока или выберите другую.")}</CmpText><CmpOutlinedButton text={L("Выбрать другую модель")} icon="swap_horiz" fullWidth style={btn52} onClick={onModel} /></Center>;
  else if (state === "error") stateEl = <Center><Hero icon="cloud_off" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Нет связи с сервером")}</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Не удалось загрузить список версий. Проверьте интернет и повторите.")}</CmpText><CmpButton text={L("Повторить")} icon="refresh" fullWidth style={btn52} onClick={() => setState("loading")} /></Center>;
  else if (!list0.length) {
    const isBlk = entry === "a" && !!blk && f.src === "block";
    stateEl = (
      <Center>
        <Hero icon="search_off" tone="neutral" size={88} />
        <CmpText variant="titleLarge">{L("Для CAN")} {f.can} {L("прошивок не найдено")}</CmpText>
        <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{isBlk ? L("Под CAN-код блока сборок нет. Можно поискать под другим кодом: введите его в поле выше или выберите автомобиль.") : f.src === "car" && f.car ? `${f.car}: ${L("сборок под этот CAN-код пока нет.")}` : L("Проверьте код или выберите автомобиль из каталога.")}</CmpText>
        <CmpButton text={L("Показать все версии")} icon="filter_alt_off" fullWidth style={btn52} onClick={() => setFilter({ can: "", src: entry === "b" ? "none" : "manual" })} />
        {isBlk && <CmpOutlinedButton text={L("Выбрать автомобиль")} icon="directions_car" fullWidth style={btn52} onClick={() => setCat(true)} />}
        {!isBlk && entry === "a" && blk && <CmpOutlinedButton text={L("Вернуть CAN блока")} icon="undo" fullWidth style={btn52} onClick={resetBlk} />}
      </Center>);
  }
  const footer = state === "more" ? <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 12, minHeight: 56 }}><Spin /><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Загружаем ещё…")}</CmpText></div>
    : state === "moreerror" ? <Banner icon="cloud_off" tone="error"><span style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 10, flexWrap: "wrap" }}><span>{L("Не удалось загрузить ещё версии")}</span><CmpTextButton text={L("Повторить")} onClick={() => setState("more")} style={btn48} /></span></Banner> : null;
  const rows = list.map((it, i) => <VerRow key={vid(it)} i={i} it={it} kind={kindOf(it)} wide={wide} blk={bt ? null : blk} entry={entry} sel={wide && sel && vid(sel) === vid(it)} busy={!!dl && !dl.done && dl.id === vid(it)}
    onSelect={() => setSelId(vid(it))} onPick={() => pick(it)} onShare={() => share(it)} />);
  const warn = channel !== "release" && <Banner icon="warning">{L(FW_TEST_WARN)}</Banner>;
  const canBox = !bt && <CanField f={f} setF={setFilter} blk={blk} entry={entry} onCatalog={() => setCat(true)} onReset={resetBlk} />;
  const strip = entry === "b" && <FwContext model={model} onModel={onModel} channel={channel} setChannel={setChannel} mod={mod} setMod={setMod} />;
  const main = stateEl || <React.Fragment>{rows}{footer}</React.Fragment>;

  let detail = null;
  if (wide && sel && !stateEl) {
    const k = kindOf(sel), busy = !!dl && !dl.done && dl.id === vid(sel);
    detail = (
      <div style={{ display: "flex", flexDirection: "column", gap: 16, padding: 24, borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}><Ver v={sel.v} size={34} />{k !== "new" && <VerBadge kind={k} />}</div>
          <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-paragraph2-size)", color: "var(--on-surface-variant)" }}>{sel.d}</span>
        </div>
        {!bt && <CanInfo it={sel} blk={blk} entry={entry} />}
        <div><CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("Полное описание")}</CmpText>
          <div style={{ whiteSpace: "pre-line", marginTop: 6, maxHeight: 260, overflowY: "auto" }} tabIndex={0}><CmpText variant="bodyLarge">{sel.desc ? L(sel.desc) : L("Описание отсутствует")}</CmpText></div></div>
        {k === "older" && <Banner icon="warning">{L("Выбранная версия старее установленной")} <Ver v={inst} color="currentColor" size={14} /></Banner>}
        {k === "installed" && <Banner icon="info" tone="ok">{L("Эта версия уже установлена. Можно переустановить")}</Banner>}
        {entry === "b"
          ? <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              <CmpButton text={busy ? L("Скачиваем…") : L("Скачать")} icon="download" disabled={busy} style={{ height: 52, minWidth: 200 }} onClick={() => download(sel)} />
              <CmpOutlinedButton text={L("Поделиться")} icon="share" style={{ height: 52, minWidth: 180 }} onClick={() => share(sel)} />
            </div>
          : <CmpButton text={k === "installed" ? L("Переустановить") : L("Установить")} icon="system_update" style={{ height: 52, alignSelf: "flex-start", minWidth: 240 }} onClick={() => pick(sel)} />}
      </div>);
  } else if (wide) detail = (
    <div style={{ height: "100%", minHeight: 240, display: "grid", placeItems: "center", borderRadius: "var(--r-card-medium-radius)", border: "1px dashed var(--outline-variant)", padding: 24, boxSizing: "border-box", textAlign: "center" }}>
      <CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Описание версии появится здесь")}</CmpText>
    </div>);

  const body = wide ? (
    <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", gap: 16, padding: 24, boxSizing: "border-box", maxWidth: 1180, width: "100%", margin: "0 auto" }}>
      {strip}
      <div style={{ flex: 1, minHeight: 0, display: "flex", gap: 24 }}>
        <div style={{ flex: "0 0 440px", minHeight: 0, overflow: "auto", display: "flex", flexDirection: "column", gap: 12, padding: 2 }}>{warn}{canBox}{main}</div>
        <div style={{ flex: 1, minWidth: 0, minHeight: 0, overflow: "auto" }}>{detail}</div>
      </div>
    </div>
  ) : (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: 760, margin: "0 auto", padding: pad, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 12 }}>{strip}{warn}{canBox}{main}</div>
    </div>
  );
  const sub = entry === "b" ? model + (bt ? " · " + L("Bluetooth-модуль") : "") : fwSub(bt ? "bt" : "single");
  return (
    <React.Fragment>
      <CmpTopAppBar title={L("Архив прошивок")} subtitle={sub} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={L("Назад")} onClick={onBack} />}
        actions={<FwIconBtn icon="refresh" label={L("Обновить список")} onClick={() => setState("loading")} color="var(--primary)" />} />
      {body}
      {dialog && (
        <CmpDialog open onDismissRequest={() => setDialog(null)} icon="warning" title={L("Установить более старую версию?")} style={{ maxWidth: 440 }}
          text={<span>{L("Выбранная версия")} <Ver v={dialog.v} /> {L("старее установленной")} <Ver v={inst} />. {L("Новые функции и исправления в ней могут отсутствовать.")}</span>}
          dismissButton={<CmpTextButton text={L("Отмена")} onClick={() => setDialog(null)} style={btn48} />}
          confirmButton={<CmpButton text={L("Установить")} onClick={() => { const it = dialog; setDialog(null); onPick(it, true, blk); }} style={btn48} />} />)}
      {cat && (
        <CmpDialog open onDismissRequest={() => setCat(false)} icon="directions_car" title={L("Каталог автомобилей")} style={{ maxWidth: 460 }}
          text={<span style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <span>{L("Общий экран каталога. Для примера — недавние:")}</span>
            {FW_CARS.map((c) => (
              <button key={c.can} type="button" className="fw-hov" onClick={() => { setCat(false); setFilter({ can: c.can, src: "car", car: c.name }); }}
                style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 56, padding: "0 14px", border: "none", borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)", cursor: "pointer", textAlign: "left", color: "var(--on-surface)", font: "500 16px var(--font-sans)" }}>
                <CmpIcon icon="directions_car" size={24} color="var(--primary)" /><span style={{ flex: 1 }}>{c.name}</span><span style={{ fontFamily: "var(--font-mono)", color: "var(--on-surface-variant)" }}>CAN {c.can}</span>
              </button>))}
          </span>}
          dismissButton={<CmpTextButton text={L("Отмена")} onClick={() => setCat(false)} style={btn48} />} />)}
      {dl && dl.done && (
        <CmpDialog open onDismissRequest={() => setDl(null)} icon="download_done" title={L("Прошивка скачана")} style={{ maxWidth: 460 }}
          text={<span style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <span>{platform === "desktop" ? L("Файл сохранён в папку:") : L("Файл сохранён в «Файлы» → «Загрузки»:")}</span>
            {platform === "desktop" && <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-additional0-size)", overflowWrap: "anywhere" }}>C:\Users\Мастер\Downloads\Pandora Spec</span>}
            <span style={{ fontFamily: "var(--font-mono)", fontSize: "var(--t-paragraph2-size)", fontWeight: 700, color: "var(--on-surface)", padding: "10px 12px", borderRadius: 10, background: "var(--background-surface-highlight)", overflowWrap: "anywhere" }}>{fname(dl.it)}</span>
            <span>{L("Установить файл можно позже: «Обновление прошивки» → «Выбрать файл…».")}</span>
          </span>}
          dismissButton={<CmpTextButton text={L("Готово")} onClick={() => setDl(null)} style={btn48} />}
          confirmButton={platform === "desktop"
            ? <CmpButton text={L("Открыть папку")} onClick={() => { setDl(null); setToast(L("Папка откроется в проводнике")); }} style={btn48} />
            : <CmpButton text={L("Поделиться")} onClick={() => { const it = dl.it; setDl(null); share(it); }} style={btn48} />} />)}
      {toast && <div role="status" style={{ position: "absolute", left: 16, right: 16, bottom: 16, margin: "0 auto", maxWidth: 560, padding: "14px 16px", borderRadius: "var(--r-card-small-radius)", background: "var(--inverse-surface)", color: "var(--inverse-on-surface)", boxShadow: "var(--elevation-2)", zIndex: 30, font: "500 15px/1.35 var(--font-sans)", overflowWrap: "anywhere" }}>{toast}</div>}
    </React.Fragment>
  );
}

// Вход Б, шаг 1: выбор модели блока. В приложении — тот же экран K2 конфигуратора (ModelsScreen),
// здесь повторён его вид без зависимостей конфигуратора.
function FwModels({ current, onPick, onBack }) {
  const [q, setQ] = React.useState("");
  const ql = q.trim().toLowerCase();
  const all = FW_MODELS.filter((m) => !ql || m.toLowerCase().includes(ql));
  const sec = (t) => <div style={{ padding: "14px 20px 4px" }}><CmpText variant="labelLarge" color="var(--on-surface-variant)">{t}</CmpText></div>;
  const row = (m, icon) => (
    <button key={icon + m} type="button" className="fw-hov" onClick={() => onPick(m)}
      style={{ display: "flex", alignItems: "center", gap: 14, width: "100%", minHeight: 64, padding: "0 16px 0 20px", border: "none", background: m === current ? "var(--secondary-container)" : "transparent", cursor: "pointer", textAlign: "left", color: m === current ? "var(--on-secondary-container)" : "var(--on-surface)", boxSizing: "border-box" }}>
      <span style={{ width: 40, height: 40, borderRadius: "var(--r-card-small-radius)", display: "grid", placeItems: "center", background: "var(--background-surface-highlight)", flex: "0 0 auto" }}><CmpIcon icon={icon} size={22} color="var(--on-surface-variant)" /></span>
      <span style={{ flex: 1, minWidth: 0, fontFamily: "var(--font-mono)", fontSize: "var(--t-headline2-size)", fontWeight: 700 }}>{m}</span>
      {m === current && <CmpIcon icon="check" size={22} color="var(--primary)" />}
      <CmpIcon icon="chevron_right" size={24} color="var(--on-surface-variant)" />
    </button>);
  return (
    <React.Fragment>
      <CmpTopAppBar title={L("Модель блока")} subtitle={L("Архив прошивок без подключения")} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={L("Назад")} onClick={onBack} />} />
      <div style={{ padding: "4px 16px 8px", maxWidth: 720, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, minHeight: 56, padding: "0 4px 0 16px", borderRadius: 999, background: "var(--background-surface-highlight)" }}>
          <CmpIcon icon="search" size={24} color="var(--on-surface-variant)" />
          <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={L("Название модели")} aria-label={L("Название модели")}
            style={{ flex: 1, minWidth: 0, height: 52, border: "none", outline: "none", background: "transparent", font: "400 17px var(--font-sans)", color: "var(--on-surface)" }} />
          {q && <FwIconBtn icon="close" label={L("Очистить")} onClick={() => setQ("")} />}
        </div>
      </div>
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ maxWidth: 720, margin: "0 auto", paddingBottom: 24 }}>
          {!ql && <React.Fragment>{sec(L("Недавние"))}{FW_RECENT.map((m) => row(m, "history"))}</React.Fragment>}
          {sec(ql ? L("Найдено:") + " " + all.length : L("Все модели") + " · " + FW_MODELS.length)}
          {all.map((m) => row(m, "memory"))}
          {ql && !all.length && <Center><Hero icon="search_off" tone="neutral" size={72} /><CmpText variant="titleMedium">{L("Модели нет:")} «{q}»</CmpText><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Проверьте название на наклейке блока.")}</CmpText></Center>}
        </div>
      </div>
    </React.Fragment>
  );
}

// ════════ Контейнер: проверка ⇄ обновление ⇄ архив; вход Б: модель → архив ════════
function FirmwareApp({ view = "check", entry = "a", scenario = "list", dialog = null, channel = "release", profile = "single", phase = "source", reserve = false, canSc = "block", module = "single", nextCan = null, nextVer = null, blkFactory = false, platform = "mobile", onBack = () => {}, onHome }) {
  const [rootRef, layout] = useFwWidth();
  const [v, setV] = React.useState(view);
  const [ch, setCh] = React.useState(channel);
  const [mod, setMod] = React.useState(module);
  const [ph, setPh] = React.useState(phase);
  const [n, setN] = React.useState(0);
  const [model, setModel] = React.useState("DX-4GS");
  const [fromArch, setFromArch] = React.useState(false);
  const [nx, setNx] = React.useState({ v: nextVer, dg: false, live: false, can: nextCan, blk: blkFactory ? null : FW_BLOCK_CAN });
  const home = onHome || onBack;
  const go = (x) => { setV(x); setN((k) => k + 1); };
  let screen;
  if (entry === "b") {
    screen = v === "models"
      ? <FwModels key={"m" + n} current={fromArch ? model : null} onBack={() => fromArch ? go("archive") : home()} onPick={(m) => { setModel(m); go("archive"); }} />
      : <FwArchive key={"b" + n} entry="b" layout={layout} platform={platform} scenario={n ? "list" : scenario} canSc={n ? "none" : canSc} channel={ch} module0={mod} model={model}
          onChannel={setCh} onModule={setMod} onBack={() => { setFromArch(false); go("models"); }} onModel={() => { setFromArch(true); go("models"); }} />;
  } else if (v === "archive") {
    screen = <FwArchive key={"a" + n} layout={layout} platform={platform} scenario={scenario} channel={ch} dialog0={dialog === "rollback"} canSc={canSc} module0={profile === "bt" ? "bt" : module}
      onBack={() => { setPh("source"); setNx({ v: null, dg: false, live: false, can: null, blk: FW_BLOCK_CAN }); go("update"); }}
      onPick={(it, dg, blk) => { setNx({ v: it.v, dg, live: true, can: it.can ? { can: it.can, prof: it.prof } : null, blk }); setPh("download"); go("update"); }} />;
  } else if (v === "check") {
    screen = <FwCheck key={"c" + n} layout={layout} scenario={scenario} dialog0={dialog} channel0={ch} onBack={onBack} onEnter={(c) => { setCh(c); setPh("entering"); go("update"); }} />;
  } else {
    screen = <FwUpdate key={"u" + n} layout={layout} profile={profile} phase0={ph} channel0={ch} reserve={reserve} live0={nx.live} nextVer={nx.v} nextCan={profile === "bt" ? null : nx.can} blkCan={nx.blk} downgrade={nx.dg} onBack={() => go("check")} onDone={onBack} onArchive={() => go("archive")} />;
  }
  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>{screen}</div>
  );
}

// ════════ Панель управления для витрины ═══════════════════════════════════════
function FwSeg({ items, value, onChange }) {
  return <div className={"seg" + (items.length > 4 ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
const FW_CHECK_OPTS = [["list", "Список"], ["one", "Один модуль"], ["states", "Состояния карточки"], ["loading", "Загрузка"], ["error", "Ошибка списка"], ["beta", "Beta-канал"], ["desc", "Диалог описания"], ["nodesc", "Описание пустое"], ["confirm", "Подтверждение"], ["pin", "PIN-диалог"], ["pinerr", "PIN: ошибка"]];
const FW_ARCH_OPTS = [["list", "Список"], ["loading", "Загрузка"], ["more", "Догрузка"], ["moreerror", "Ошибка догрузки"], ["empty", "Пусто"], ["error", "Нет связи"], ["rollback", "Откат (диалог)"]];
const FW_ARCH_OPTS_B = [["list", "Список"], ["loading", "Загрузка"], ["more", "Догрузка"], ["moreerror", "Ошибка догрузки"], ["nofw", "У модели нет прошивок"], ["error", "Нет связи"], ["dl", "Скачано (диалог)"]];
const FW_CAN_A = [["block", "CAN: код блока"], ["other", "Другой код"], ["car", "Из автомобиля"], ["cleared", "Поле очищено"], ["partial", "1–3 цифры"], ["factory", "Заводские CAN"], ["emptyblock", "Пусто: код блока"], ["emptyother", "Пусто: другой код"]];
const FW_CAN_B = [["none", "CAN: пусто"], ["car", "Из автомобиля"], ["other", "Введён код"], ["partial", "1–3 цифры"], ["emptyother", "Пусто под фильтром"]];
const FW_UPD_OPTS = [["entering", "Вход в загрузчик"], ["source", "Выбор источника"], ["dataerror", "Ошибка данных"], ["download", "1 Скачивание"], ["confirm", "2 Подтверждение"], ["confirmcan", "2 Подтверждение · другой CAN"], ["prepare", "3 Подготовка"], ["transfer", "4 Передача"], ["verify", "5 Проверка"], ["apply", "6 Применение"], ["success", "7 Успех"], ["finishing", "Ожидание блока"], ["error", "8 Ошибка"], ["lost", "Обрыв связи"]];
function useFwCtl() {
  const [dark, setDarkRaw] = React.useState(() => { try { const v = localStorage.getItem("ps-theme"); return v ? v === "dark" : matchMedia("(prefers-color-scheme: dark)").matches; } catch (e) { return false; } });
  const setDark = (v) => { setDarkRaw(v); try { localStorage.setItem("ps-theme", v ? "dark" : "light"); } catch (e) {} };
  React.useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const [entry, setEntry] = React.useState(() => { try { return new URLSearchParams(location.search).get("profile") === "nounit" ? "b" : "a"; } catch (e) { return "a"; } });
  const [view, setView] = React.useState("check");
  const [bview, setBview] = React.useState("models");
  const [sc, setSc] = React.useState("list");
  const [ph, setPh] = React.useState("source");
  const [asc, setAsc] = React.useState("list");
  const [bsc, setBsc] = React.useState("list");
  const [acan, setAcan] = React.useState("block");
  const [bcan, setBcan] = React.useState("none");
  const [amod, setAmod] = React.useState("single");
  const [ach, setAch] = React.useState("release");
  const [profile, setProfile] = React.useState("single");
  const [reserve, setReserve] = React.useState(false);
  const [ufac, setUfac] = React.useState(false);
  const [lang, setLang] = React.useState("ru");
  const [long, setLong] = React.useState(false);
  window.FW_LANG = lang; window.FW_LONG = long;
  let props;
  if (entry === "b") props = { view: bview, entry: "b", scenario: bsc, channel: ach, canSc: bcan, module: amod };
  else if (view === "check") props = { view, scenario: sc === "one" ? "one" : sc === "beta" || sc === "desc" || sc === "confirm" || sc === "pin" || sc === "pinerr" ? "list" : sc, dialog: sc === "desc" || sc === "nodesc" ? "desc" : sc === "confirm" ? "confirm" : sc === "pin" || sc === "pinerr" ? sc : null, channel: sc === "beta" ? "beta" : "release" };
  else if (view === "archive") props = { view, scenario: asc === "rollback" ? "list" : asc, dialog: asc === "rollback" ? "rollback" : null, channel: ach, canSc: acan, module: amod };
  else props = { view, phase: ph === "confirmcan" ? "confirm" : ph, profile, reserve, nextCan: ph === "confirmcan" ? { can: "3312", prof: "1.01" } : ph === "confirm" ? { can: "2154", prof: "1.03" } : null, nextVer: ph === "confirmcan" || ph === "confirm" ? "3.61" : null, blkFactory: ufac };
  const chSeg = <FwSeg items={[["release", "Готовая"], ["beta", "Beta"], ["alpha", "Alpha"]]} value={ach} onChange={setAch} />;
  const modSeg = <FwSeg items={[["single", "Основная"], ["bt", "Bluetooth-модуль"]]} value={amod} onChange={setAmod} />;
  const bar = (
    <React.Fragment>
      <button type="button" className="theme-fab" onClick={() => setDark(!dark)} aria-label={dark ? "Светлая тема" : "Тёмная тема"} title={dark ? "Светлая тема" : "Тёмная тема"}>{dark ? "☀" : "☾"}</button>
      <div className="toolbar">
        <FwSeg items={[["ru", "RU"], ["en", "EN"]]} value={lang} onChange={setLang} />
        <FwSeg items={[["0", "Имена: короткие"], ["1", "Имена: 40 символов"]]} value={long ? "1" : "0"} onChange={(x) => setLong(x === "1")} />
        <FwSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(x) => setDark(x === "d")} />
        <FwSeg items={[["a", "Вход А: блок подключён"], ["b", "Вход Б: без блока"]]} value={entry} onChange={setEntry} />
      </div>
      <div className="toolbar">
        {entry === "a"
          ? <FwSeg items={[["check", "Проверка прошивки"], ["update", "Обновление прошивки"], ["archive", "Архив прошивок"]]} value={view} onChange={setView} />
          : <FwSeg items={[["models", "1 Выбор модели"], ["archive", "2 Архив без блока"]]} value={bview} onChange={setBview} />}
      </div>
      <div className="toolbar">
        {entry === "b"
          ? (bview === "archive" ? <React.Fragment><FwSeg items={FW_ARCH_OPTS_B} value={bsc} onChange={setBsc} /><FwSeg items={FW_CAN_B} value={bcan} onChange={setBcan} />{chSeg}{modSeg}</React.Fragment> : null)
          : view === "check"
          ? <FwSeg items={FW_CHECK_OPTS} value={sc} onChange={setSc} />
          : view === "archive"
          ? <React.Fragment><FwSeg items={FW_ARCH_OPTS} value={asc} onChange={setAsc} /><FwSeg items={FW_CAN_A} value={acan} onChange={setAcan} />{chSeg}{modSeg}</React.Fragment>
          : <React.Fragment>
              <FwSeg items={FW_UPD_OPTS} value={ph} onChange={setPh} />
              <FwSeg items={[["single", "1 подсистема"], ["multi", "Несколько"], ["bt", "Bluetooth-модуль"]]} value={profile} onChange={setProfile} />
              <FwSeg items={[["0", "Резерв скрыт"], ["1", "Резерв показан"]]} value={reserve ? "1" : "0"} onChange={(x) => setReserve(x === "1")} />
              <FwSeg items={[["0", "CAN блока 2154"], ["1", "Заводские CAN"]]} value={ufac ? "1" : "0"} onChange={(x) => setUfac(x === "1")} />
            </React.Fragment>}
      </div>
    </React.Fragment>
  );
  return { dark, props, bar, key: [entry, view, bview, sc, asc, bsc, acan, bcan, amod, ach, ph, profile, reserve, ufac, lang, long].join() };
}

Object.assign(window, { FirmwareApp, useFwCtl, FwSeg });
