
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
  s.textContent = "@keyframes fwRing{0%{transform:scale(.9);opacity:.5}100%{transform:scale(1.35);opacity:0}}@keyframes fwIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}@media (prefers-reduced-motion: reduce){.fw-anim{animation:none!important}}";
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
  return <span style={{ fontFamily: "var(--font-mono)", fontSize: size, fontWeight: 600, color }}>{v}</span>;
}
function Spin({ size = 20 }) { return <CmpCircularProgressIndicator size={size} strokeWidth={2.5} />; }
const btn48 = { height: 48 };

function Banner({ icon = "warning", tone = "warn", children }) {
  const bg = tone === "error" ? "var(--error-container)" : tone === "ok" ? "var(--primary-container)" : "var(--tertiary-container)";
  const fg = tone === "error" ? "var(--on-error-container)" : tone === "ok" ? "var(--on-primary-container)" : "var(--on-tertiary-container)";
  return (
    <div style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "12px 14px", borderRadius: 14, background: bg, color: fg, boxSizing: "border-box" }}>
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
  return [base, bt, watch];
}

function ModCard({ m, onDesc, onEnter, onRefresh, i }) {
  const s = m.srv, upd = s.k === "update", busy = s.k === "search";
  const hl = upd && !m.unsupported;
  return (
    <div className="fw-anim" style={{ animation: "fwIn .26s ease both", animationDelay: `${i * 50}ms`, boxSizing: "border-box", borderRadius: 20, padding: 16, background: "var(--surface-container-low)", border: hl ? "2px solid var(--primary)" : "1px solid var(--outline-variant)", margin: hl ? 0 : 1, display: "flex", flexDirection: "column", gap: 12, minWidth: 0 }}>
      <div style={{ display: "flex", alignItems: "flex-start", gap: 14 }}>
        <div style={{ width: 48, height: 48, borderRadius: 14, flex: "0 0 auto", background: "var(--primary-container)", color: "var(--on-primary-container)", display: "grid", placeItems: "center" }}><CmpIcon icon="memory" size={28} color="currentColor" /></div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontFamily: "var(--font-sans)", fontSize: 12, fontWeight: 700, letterSpacing: ".06em", color: "var(--primary)", textTransform: "uppercase" }}>{L(m.type)}</div>
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
        <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: 14, borderRadius: 14, background: "var(--primary-container)", color: "var(--on-primary-container)" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8 }}>
            <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}><CmpIcon icon="arrow_circle_up" size={24} color="currentColor" /><CmpText variant="labelLarge" color="currentColor">{L("Доступно обновление")}</CmpText></span>
            <Ver v={s.v} size={18} color="currentColor" />
          </div>
          <div style={{ display: "flex", gap: 10 }}>
            <CmpOutlinedButton text={L("Описание")} onClick={onDesc} style={{ ...btn48, flex: 1 }} />
            <CmpButton text={L("Обновить")} icon="system_update" onClick={onEnter} style={{ ...btn48, flex: 1.4 }} />
          </div>
        </div>
      ) : s.k === "actual" ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpIcon icon="check_circle" size={24} color="var(--primary)" /><CmpText variant="bodyMedium">{L("Актуальная")}</CmpText><Ver v={s.v} /></div>
          <CmpTextButton text={L("Переустановить")} onClick={onEnter} style={{ ...btn48, alignSelf: "flex-start" }} />
        </div>
      ) : s.k === "error" ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 10, padding: 14, borderRadius: 14, background: "var(--error-container)", color: "var(--on-error-container)" }}>
          <div style={{ display: "flex", gap: 10 }}><CmpIcon icon="cloud_off" size={24} color="currentColor" /><CmpText variant="bodyMedium" color="currentColor">{L(s.err)}</CmpText></div>
          <CmpOutlinedButton text={L("Повторить")} icon="refresh" onClick={onRefresh} style={btn48} />
        </div>
      ) : null}
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
        style={{ height: 48, padding: "0 12px 0 14px", borderRadius: 999, border: test ? "none" : "1px solid var(--outline)", cursor: "pointer", display: "inline-flex", alignItems: "center", gap: 6, fontFamily: "var(--font-sans)", fontSize: 14, fontWeight: 600, whiteSpace: "nowrap",
          background: test ? "var(--tertiary-container)" : "transparent", color: test ? "var(--on-tertiary-container)" : "var(--on-surface-variant)" }}>
        {test && <CmpIcon icon="warning" size={18} color="currentColor" />}
        <span style={{ fontWeight: 500 }}>{L("Канал")}:</span> {L(cur[2])}
        <CmpIcon icon={open ? "expand_less" : "expand_more"} size={20} color="currentColor" />
      </button>
      {open && (
        <div role="listbox" aria-label={L("Тип канала обновления")} style={{ position: "absolute", top: "calc(100% + 4px)", right: 0, zIndex: 20, minWidth: 260, padding: "8px 0", borderRadius: 12, background: "var(--surface-container)", boxShadow: "var(--elevation-2)" }}>
          {FW_CHANNELS.map(([k, name]) => (
            <button key={k} type="button" role="option" aria-selected={k === channel} onClick={() => { setOpen(false); k !== channel && onChange(k); }}
              style={{ display: "flex", alignItems: "center", gap: 10, width: "100%", minHeight: 48, padding: "0 16px", border: "none", cursor: "pointer", textAlign: "left", fontFamily: "var(--font-sans)", fontSize: 15, background: k === channel ? "var(--surface-variant)" : "transparent", color: "var(--on-surface)" }}>
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
        <div role="radiogroup" aria-label={L("Тип канала обновления")} style={{ display: "flex", padding: 4, gap: 4, borderRadius: 999, background: "var(--surface-container-high)", flex: layout === "compact" ? undefined : "0 0 380px" }}>
          {FW_CHANNELS.map(([k, , short]) => {
            const on = channel === k;
            return <button key={k} type="button" role="radio" aria-checked={on} onClick={() => onChange(k)}
              style={{ flex: 1, height: 48, border: "none", borderRadius: 999, cursor: "pointer", fontFamily: "var(--font-sans)", fontSize: 15, fontWeight: 600, background: on ? "var(--primary)" : "transparent", color: on ? "var(--on-primary)" : "var(--on-surface)" }}>{L(short)}</button>;
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
          {[0, 1, 2, 3].map((i) => <span key={i} aria-hidden="true" style={{ width: 56, height: 64, boxSizing: "border-box", borderRadius: 12, display: "grid", placeItems: "center", fontSize: 30, color: "var(--on-surface)", border: err ? "2px solid var(--error)" : i === Math.min(pin.length, 3) ? "2px solid var(--primary)" : "1px solid var(--outline)" }}>{i < pin.length ? "•" : ""}</span>)}
          <input ref={inputRef} value={pin} onChange={change} onKeyDown={(e) => { if (e.key === "Enter" && pin.length === 4 && left > 0) submit(); }}
            type="password" inputMode="numeric" pattern="[0-9]*" autoComplete="one-time-code" maxLength={4} aria-label={L("PIN-код")} disabled={left === 0}
            style={{ position: "absolute", inset: 0, width: "100%", height: "100%", opacity: 0, border: "none", cursor: "text", fontSize: 16 }} />
        </span>
        <span style={{ minHeight: 22, color: "var(--error)", fontWeight: 600 }} aria-live="polite">{err ? L("Неверный PIN-код. Осталось попыток:") + " " + left : ""}</span>
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
  let body;
  if (state === "loading") body = <Center><CmpCircularProgressIndicator /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Читаем список модулей блока…")}</CmpText></Center>;
  else if (state === "error") body = <Center><Hero icon="error_outline" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Не удалось получить список модулей")}</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Проверьте соединение с блоком и повторите попытку.")}</CmpText><CmpButton text={L("Повторить")} icon="refresh" fullWidth style={{ height: 52 }} onClick={() => setState("loading")} /></Center>;
  else body = (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: 1040, margin: "0 auto", padding: pad, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 16 }}>
        <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: "4px 8px", padding: "8px 8px 8px 14px", borderRadius: 16, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, flex: layout === "compact" ? "1 1 100%" : "1 1 0", minWidth: 0 }}>
            <CmpIcon icon="sensors" size={26} color="var(--primary)" />
            <div style={{ flex: 1, minWidth: 0, overflowWrap: "break-word" }}><CmpText variant="titleMedium">{N(FW_DEV.name)}</CmpText></div>
            {layout !== "compact" && <span style={{ fontFamily: "var(--font-mono)", fontSize: 14, color: "var(--on-surface-variant)", flex: "0 0 auto" }}>ID {FW_DEV.id}</span>}
          </div>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8, flex: layout === "compact" ? "1 1 100%" : "0 0 auto" }}>
            {layout === "compact" && <span style={{ fontFamily: "var(--font-mono)", fontSize: 14, color: "var(--on-surface-variant)", paddingLeft: 38 }}>ID {FW_DEV.id}</span>}
            <ChannelChip channel={channel} onChange={changeCh} />
          </div>
        </div>
        {channel !== "release" && <Banner icon="warning">{L(FW_TEST_WARN)}</Banner>}
        <CmpText variant="titleMedium">{L("Доступные для обновления модули")}</CmpText>
        <div style={{ display: "grid", gridTemplateColumns: layout === "compact" ? "1fr" : "repeat(2, minmax(0,1fr))", gap: 14, alignItems: "start" }}>
          {mods.map((m, i) => <ModCard key={m.id} m={m} i={i} onDesc={() => setDialog("desc")} onEnter={() => { setTarget(m.id); setDialog("confirm"); }} onRefresh={() => refresh(m.id)} />)}
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

function FwUpdate({ layout, profile, phase0, channel0, reserve, live0 = false, nextVer, downgrade = false, onBack, onDone, onArchive }) {
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
    <div style={{ display: "flex", flexDirection: "column", gap: 4, padding: 16, borderRadius: 16, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)" }}>
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
    <div style={{ padding: 14, borderRadius: 14, background: "var(--surface-container-high)", textAlign: "left" }}>
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("Что нового")}</CmpText>
      <div style={{ whiteSpace: "pre-line", marginTop: 4 }}><CmpText variant="bodyMedium">{L(FW_NOTES)}</CmpText></div>
    </div>
    {downgrade && <Banner icon="warning">{L("Выбрана версия старее установленной")}</Banner>}
    {warnVer}
    <div style={{ display: "flex", gap: 12, width: "100%" }}>
      <CmpButton text={L("Обновить")} style={{ height: 52, flex: 1 }} onClick={() => { setLive(true); setPct(0); setPhase("prepare"); }} />
      <CmpOutlinedButton text={L("Отмена")} style={{ height: 52, flex: 1 }} onClick={() => setPhase("source")} />
    </div></React.Fragment>;
  else if (running) content = (
    <div style={{ display: "flex", flexDirection: "column", gap: 18, width: "100%" }}>
      <div style={{ display: "flex", gap: 6 }} aria-hidden="true">
        {STEPS.map(([k, l], i) => (
          <div key={k} style={{ flex: 1, display: "flex", flexDirection: "column", gap: 6 }}>
            <div style={{ height: 6, borderRadius: 3, background: i <= stepIdx ? "var(--primary)" : "var(--surface-variant)" }} />
            <span style={{ fontFamily: "var(--font-sans)", fontSize: 13, fontWeight: i === stepIdx ? 700 : 500, color: i === stepIdx ? "var(--on-surface)" : "var(--on-surface-variant)" }}>{L(l)}</span>
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
const FW_VERS = [
  { v: "3.61", d: "14.10.2026", desc: FW_NOTES },
  { v: "3.60", d: "02.09.2026", desc: "• Исправлена ошибка постановки на охрану при низком заряде АКБ\n• Ускорено подключение по Bluetooth" },
  { v: "3.58", d: "19.07.2026", desc: "" },
  { v: "3.57", d: "30.05.2026", desc: "• Первая версия с поддержкой профилей CAN 2.0\n• Оптимизировано энергопотребление в режиме охраны\n• Исправлены ошибки обработки датчика удара\n• Улучшена работа автозапуска при низких температурах\n• Обновлён протокол обмена с модулем часов" },
  { v: "3.55", d: "11.04.2026", desc: "• Улучшена стабильность связи по Bluetooth" },
  { v: "3.52", d: "03.02.2026", desc: "" },
];
const FW_INSTALLED = "3.57";

function VerBadge({ kind }) {
  const inst = kind === "installed";
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 4, height: 26, padding: "0 10px", borderRadius: 999, boxSizing: "border-box", fontFamily: "var(--font-sans)", fontSize: 13, fontWeight: 600, whiteSpace: "nowrap", background: inst ? "var(--primary)" : "var(--surface-variant)", color: inst ? "var(--on-primary)" : "var(--on-surface-variant)" }}>{inst && <CmpIcon icon="check" size={16} color="currentColor" />}{inst ? L("Установлена") : L("Старее установленной")}</span>;
}

function VerRow({ it, kind, wide, sel, onSelect, onPick, i }) {
  const [open, setOpen] = React.useState(false);
  const has = !!it.desc;
  const clamp = open ? {} : { display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden" };
  return (
    <div className="fw-anim" onClick={wide ? onSelect : undefined} role={wide ? "button" : undefined} tabIndex={wide ? 0 : undefined}
      style={{ animation: "fwIn .26s ease both", animationDelay: `${i * 40}ms`, boxSizing: "border-box", borderRadius: 18, padding: 16, cursor: wide ? "pointer" : "default", display: "flex", flexDirection: "column", gap: 10, minWidth: 0,
        background: sel ? "var(--primary-container)" : "var(--surface-container-low)", color: sel ? "var(--on-primary-container)" : "var(--on-surface)",
        border: sel ? "2px solid var(--primary)" : kind === "installed" ? "2px solid var(--primary)" : "1px solid var(--outline-variant)", margin: sel || kind === "installed" ? 0 : 1 }}>
      <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 8, justifyContent: "space-between" }}>
        <div style={{ display: "flex", alignItems: "center", flexWrap: "wrap", gap: 10, minWidth: 0 }}>
          <Ver v={it.v} size={24} color={sel ? "currentColor" : "var(--primary)"} />
          {kind !== "new" && <VerBadge kind={kind} />}
        </div>
        <span style={{ fontFamily: "var(--font-mono)", fontSize: 14, color: sel ? "currentColor" : "var(--on-surface-variant)" }}>{it.d}</span>
      </div>
      {has && <div style={{ whiteSpace: "pre-line", ...clamp }}><CmpText variant="bodyMedium" color={sel ? "currentColor" : "var(--on-surface-variant)"}>{L(it.desc)}</CmpText></div>}
      {(has || !wide) && (
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 8, marginTop: 2 }}>
          {has ? <CmpTextButton text={open ? L("Свернуть") : L("Показать больше")} onClick={(e) => { e && e.stopPropagation && e.stopPropagation(); setOpen(!open); }} style={btn48} /> : <span />}
          {!wide && <CmpOutlinedButton text={L("Выбрать")} onClick={onPick} style={btn48} />}
        </div>)}
    </div>
  );
}

function FwArchive({ layout, scenario, channel, dialog0, onBack, onPick }) {
  const [state, setState] = React.useState(scenario === "loading" ? "loading" : scenario);
  const [dialog, setDialog] = React.useState(dialog0 ? FW_VERS[3 + 1].v : null);
  const [selV, setSelV] = React.useState(dialog0 ? "3.55" : "3.61");
  const wide = layout === "expanded";
  React.useEffect(() => { if (state !== "loading") return; const t = setTimeout(() => setState("list"), 1500); return () => clearTimeout(t); }, [state]);
  const instIdx = FW_VERS.findIndex((x) => x.v === FW_INSTALLED);
  const kindOf = (i) => i === instIdx ? "installed" : i > instIdx ? "older" : "new";
  const pick = (it, i) => kindOf(i) === "older" ? setDialog(it.v) : onPick(it.v, false);
  const sel = FW_VERS.find((x) => x.v === selV), selI = FW_VERS.indexOf(sel);
  const list = (state === "list" || state === "more" || state === "moreerror") ? FW_VERS : [];
  const pad = layout === "compact" ? 14 : 24;
  let body;
  if (state === "loading") body = <Center><CmpCircularProgressIndicator /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Загружаем список версий…")}</CmpText></Center>;
  else if (state === "empty") body = <Center><Hero icon="inventory_2" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Для данного устройства не найдено прошивок. Попробуйте позже")}</CmpText><CmpOutlinedButton text={L("Обновить")} icon="refresh" fullWidth style={{ height: 52 }} onClick={() => setState("loading")} /></Center>;
  else if (state === "error") body = <Center><Hero icon="cloud_off" tone="neutral" size={88} /><CmpText variant="titleLarge">{L("Нет связи с сервером")}</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">{L("Не удалось загрузить список версий. Проверьте интернет и повторите.")}</CmpText><CmpButton text={L("Повторить")} icon="refresh" fullWidth style={{ height: 52 }} onClick={() => setState("loading")} /></Center>;
  else {
    const footer = state === "more" ? <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 12, minHeight: 56 }}><Spin /><CmpText variant="bodyMedium" color="var(--on-surface-variant)">{L("Загружаем ещё…")}</CmpText></div>
      : state === "moreerror" ? <Banner icon="cloud_off" tone="error"><span style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 10, flexWrap: "wrap" }}><span>{L("Не удалось загрузить ещё версии")}</span><CmpTextButton text={L("Повторить")} onClick={() => setState("more")} style={btn48} /></span></Banner> : null;
    const rows = list.map((it, i) => <VerRow key={it.v} i={i} it={it} kind={kindOf(i)} wide={wide} sel={wide && selV === it.v} onSelect={() => setSelV(it.v)} onPick={() => pick(it, i)} />);
    const warn = channel !== "release" && <Banner icon="warning">{L(FW_TEST_WARN)}</Banner>;
    body = wide ? (
      <div style={{ flex: 1, minHeight: 0, display: "flex", gap: 24, padding: 24, boxSizing: "border-box", maxWidth: 1180, width: "100%", margin: "0 auto" }}>
        <div style={{ flex: "0 0 440px", minHeight: 0, overflow: "auto", display: "flex", flexDirection: "column", gap: 12, padding: 2 }}>{warn}{rows}{footer}</div>
        <div style={{ flex: 1, minWidth: 0, minHeight: 0, overflow: "auto" }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 16, padding: 24, borderRadius: 20, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}><Ver v={sel.v} size={34} />{kindOf(selI) !== "new" && <VerBadge kind={kindOf(selI)} />}</div>
              <span style={{ fontFamily: "var(--font-mono)", fontSize: 15, color: "var(--on-surface-variant)" }}>{sel.d}</span>
            </div>
            <div><CmpText variant="labelLarge" color="var(--on-surface-variant)">{L("Полное описание")}</CmpText>
              <div style={{ whiteSpace: "pre-line", marginTop: 6 }}><CmpText variant="bodyLarge">{sel.desc ? L(sel.desc) : L("Описание отсутствует")}</CmpText></div></div>
            {kindOf(selI) === "older" && <Banner icon="warning">{L("Выбранная версия старее установленной")} <Ver v={FW_INSTALLED} color="currentColor" size={14} /></Banner>}
            {kindOf(selI) === "installed" && <Banner icon="info" tone="ok">{L("Эта версия уже установлена. Можно переустановить")}</Banner>}
            <CmpButton text={kindOf(selI) === "installed" ? L("Переустановить") : L("Установить")} icon="system_update" style={{ height: 52, alignSelf: "flex-start", minWidth: 240 }} onClick={() => pick(sel, selI)} />
          </div>
        </div>
      </div>
    ) : (
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ maxWidth: 760, margin: "0 auto", padding: pad, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 12 }}>{warn}{rows}{footer}</div>
      </div>
    );
  }
  const dlgIt = FW_VERS.find((x) => x.v === dialog);
  return (
    <React.Fragment>
      <CmpTopAppBar title={L("Архив прошивок")} subtitle={fwSub("single")} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription={L("Назад")} onClick={onBack} />}
        actions={<button type="button" aria-label={L("Обновить список")} onClick={() => setState("loading")} style={{ width: 48, height: 48, border: "none", background: "transparent", borderRadius: 999, display: "grid", placeItems: "center", cursor: "pointer", color: "var(--primary)" }}><CmpIcon icon="refresh" size={26} color="currentColor" /></button>} />
      {body}
      {dlgIt && (
        <CmpDialog open onDismissRequest={() => setDialog(null)} icon="warning" title={L("Установить более старую версию?")} style={{ maxWidth: 440 }}
          text={<span>{L("Выбранная версия")} <Ver v={dlgIt.v} /> {L("старее установленной")} <Ver v={FW_INSTALLED} />. {L("Новые функции и исправления в ней могут отсутствовать.")}</span>}
          dismissButton={<CmpTextButton text={L("Отмена")} onClick={() => setDialog(null)} style={btn48} />}
          confirmButton={<CmpButton text={L("Установить")} onClick={() => { setDialog(null); onPick(dlgIt.v, true); }} style={btn48} />} />)}
    </React.Fragment>
  );
}

// ════════ Контейнер: проверка ⇄ обновление ════════════════════════════════════
function FirmwareApp({ view = "check", scenario = "list", dialog = null, channel = "release", profile = "single", phase = "source", reserve = false, onBack = () => {} }) {
  const [rootRef, layout] = useFwWidth();
  const [v, setV] = React.useState(view);
  const [ch, setCh] = React.useState(channel);
  const [ph, setPh] = React.useState(phase);
  const [n, setN] = React.useState(0);
  const [nx, setNx] = React.useState({ v: null, dg: false, live: false });
  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      {v === "archive"
        ? <FwArchive key={"a" + n} layout={layout} scenario={scenario} channel={ch} dialog0={dialog === "rollback"} onBack={() => { setPh("source"); setNx({ v: null, dg: false, live: false }); setV("update"); setN(n + 1); }} onPick={(ver, dg) => { setNx({ v: ver, dg, live: true }); setPh("download"); setV("update"); setN(n + 1); }} />
        : v === "check"
        ? <FwCheck key={"c" + n} layout={layout} scenario={scenario} dialog0={dialog} channel0={ch} onBack={onBack} onEnter={(c) => { setCh(c); setPh("entering"); setV("update"); setN(n + 1); }} />
        : <FwUpdate key={"u" + n} layout={layout} profile={profile} phase0={ph} channel0={ch} reserve={reserve} live0={nx.live} nextVer={nx.v} downgrade={nx.dg} onBack={() => { setV("check"); setN(n + 1); }} onDone={onBack} onArchive={() => { setV("archive"); setN(n + 1); }} />}
    </div>
  );
}

// ════════ Панель управления для витрины ═══════════════════════════════════════
function FwSeg({ items, value, onChange }) {
  return <div className={"seg" + (items.length > 4 ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
const FW_CHECK_OPTS = [["list", "Список"], ["states", "Состояния карточки"], ["loading", "Загрузка"], ["error", "Ошибка списка"], ["beta", "Beta-канал"], ["desc", "Диалог описания"], ["nodesc", "Описание пустое"], ["confirm", "Подтверждение"], ["pin", "PIN-диалог"], ["pinerr", "PIN: ошибка"]];
const FW_ARCH_OPTS = [["list", "Список"], ["loading", "Загрузка"], ["more", "Догрузка"], ["moreerror", "Ошибка догрузки"], ["empty", "Пусто"], ["error", "Нет связи"], ["rollback", "Откат (диалог)"]];
const FW_UPD_OPTS = [["entering", "Вход в загрузчик"], ["source", "Выбор источника"], ["dataerror", "Ошибка данных"], ["download", "1 Скачивание"], ["confirm", "2 Подтверждение"], ["prepare", "3 Подготовка"], ["transfer", "4 Передача"], ["verify", "5 Проверка"], ["apply", "6 Применение"], ["success", "7 Успех"], ["finishing", "Ожидание блока"], ["error", "8 Ошибка"], ["lost", "Обрыв связи"]];
function useFwCtl() {
  const [dark, setDarkRaw] = React.useState(() => { try { const v = localStorage.getItem("ps-theme"); return v ? v === "dark" : matchMedia("(prefers-color-scheme: dark)").matches; } catch (e) { return false; } });
  const setDark = (v) => { setDarkRaw(v); try { localStorage.setItem("ps-theme", v ? "dark" : "light"); } catch (e) {} };
  React.useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const [view, setView] = React.useState("check");
  const [sc, setSc] = React.useState("list");
  const [ph, setPh] = React.useState("source");
  const [asc, setAsc] = React.useState("list");
  const [ach, setAch] = React.useState("release");
  const [profile, setProfile] = React.useState("single");
  const [reserve, setReserve] = React.useState(false);
  const [lang, setLang] = React.useState("ru");
  const [long, setLong] = React.useState(false);
  window.FW_LANG = lang; window.FW_LONG = long;
  const props = view === "check"
    ? { view, scenario: sc === "beta" || sc === "desc" || sc === "confirm" || sc === "pin" || sc === "pinerr" ? "list" : sc, dialog: sc === "desc" || sc === "nodesc" ? "desc" : sc === "confirm" ? "confirm" : sc === "pin" || sc === "pinerr" ? sc : null, channel: sc === "beta" ? "beta" : "release" }
    : view === "archive" ? { view, scenario: asc === "rollback" ? "list" : asc, dialog: asc === "rollback" ? "rollback" : null, channel: ach }
    : { view, phase: ph, profile, reserve };
  const bar = (
    <React.Fragment>
      <button type="button" className="theme-fab" onClick={() => setDark(!dark)} aria-label={dark ? "Светлая тема" : "Тёмная тема"} title={dark ? "Светлая тема" : "Тёмная тема"}>{dark ? "☀" : "☾"}</button>
      <div className="toolbar">
        <FwSeg items={[["ru", "RU"], ["en", "EN"]]} value={lang} onChange={setLang} />
        <FwSeg items={[["0", "Имена: короткие"], ["1", "Имена: 40 символов"]]} value={long ? "1" : "0"} onChange={(x) => setLong(x === "1")} />
        <FwSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(x) => setDark(x === "d")} />
        <FwSeg items={[["check", "Проверка прошивки"], ["update", "Обновление прошивки"], ["archive", "Архив прошивок"]]} value={view} onChange={setView} />
      </div>
      <div className="toolbar">
        {view === "check"
          ? <FwSeg items={FW_CHECK_OPTS} value={sc} onChange={setSc} />
          : view === "archive"
          ? <React.Fragment><FwSeg items={FW_ARCH_OPTS} value={asc} onChange={setAsc} /><FwSeg items={[["release", "Готовая"], ["beta", "Beta"]]} value={ach} onChange={setAch} /></React.Fragment>
          : <React.Fragment>
              <FwSeg items={FW_UPD_OPTS} value={ph} onChange={setPh} />
              <FwSeg items={[["single", "1 подсистема"], ["multi", "Несколько"], ["bt", "Bluetooth-модуль"]]} value={profile} onChange={setProfile} />
              <FwSeg items={[["0", "Резерв скрыт"], ["1", "Резерв показан"]]} value={reserve ? "1" : "0"} onChange={(x) => setReserve(x === "1")} />
            </React.Fragment>}
      </div>
    </React.Fragment>
  );
  return { dark, props, bar, key: [view, sc, asc, ach, ph, profile, reserve, lang, long].join() };
}

Object.assign(window, { FirmwareApp, useFwCtl });
