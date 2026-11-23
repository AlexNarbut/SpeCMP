// Режим «Руки заняты» — кнопка «Озвучка» в шапке (вариант 1a). Общий компонент для bypass-clone, check-state, fast-mount.
// Подключение: <script type="text/babel" src="../hands-free/HandsFree.jsx"> ДО скрипта экрана; экран ставит <window.HfVoiceIcon /> в actions шапки.
// Состояние общее и запоминается (localStorage): включил один раз — включено на всех трёх экранах и при следующих монтажах.
// Демо-параметры страницы: ?voice=none — нет голоса, ?voice=unsupported — кнопки нет; ?hfos=ios|android|desktop.
// HfVoiceProvider — только для доски состояний: фиксирует state / tip / sheet / repeat без записи в localStorage.

(function () {
const HF = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpIcon, CmpButton, CmpTextButton } = HF;

if (!document.getElementById("hf-css")) {
  const s = document.createElement("style");
  s.id = "hf-css";
  s.textContent = "@keyframes hfPulse{0%{box-shadow:0 0 0 0 color-mix(in srgb,var(--primary) 50%,transparent)}70%{box-shadow:0 0 0 10px transparent}100%{box-shadow:0 0 0 0 transparent}}"
    + "@keyframes hfIn{from{opacity:0;transform:translateY(-4px)}to{opacity:1;transform:none}}@keyframes hfUp{from{transform:translateY(24px);opacity:0}to{transform:none;opacity:1}}"
    + ".hf-btn{transition:background-color .15s ease,filter .15s ease}.hf-btn:hover{filter:brightness(1.08)}.hf-btn.hf-off:hover{background:color-mix(in srgb,var(--on-surface) 8%,transparent)!important}"
    + ".hf-btn:focus-visible{outline:3px solid var(--primary);outline-offset:3px}"
    + "@media (prefers-reduced-motion:reduce){.hf-btn{animation:none!important}}";
  document.head.appendChild(s);
}

// ── Тексты ru / en ──────────────────────────────────────────────────────
const T = {
  ru: {
    a11y: { off: "Озвучка шагов выключена. Включить", on: "Озвучка шагов включена. Выключить", speaking: "Озвучка шагов включена, звучит фраза. Выключить", novoice: "Озвучка недоступна: нет голоса. Подробнее" },
    tipTitle: "Озвучка включена", tipText: "Шаги будут звучать вслух. Настройка запомнится для следующих монтажей.", tipOk: "Понятно",
    repeat: "Повторить фразу",
    nvTitle: "Нет голоса для русского языка",
    nvText: { android: "Чтобы шаги звучали вслух, в телефоне нужен голос для русского языка. Его можно скачать в настройках синтеза речи — интернет нужен только для загрузки.",
      ios: "Чтобы шаги звучали вслух, добавьте русский голос в настройках iPhone. Интернет нужен только для загрузки.",
      desktop: "Чтобы шаги звучали вслух, добавьте русский голос в настройках системы. Интернет нужен только для загрузки." },
    nvPath: { ios: [["", "Настройки → Универсальный доступ → Устной контент → Голоса → Русский"]],
      desktop: [["Windows", "Параметры → Время и язык → Речь → Добавить голоса"], ["macOS", "Системные настройки → Универсальный доступ → Устной контент → Системный голос → Управлять голосами"]] },
    nvInstall: "Установить голос", nvLater: "Позже", nvOk: "Понятно",
  },
  en: {
    a11y: { off: "Voice guidance off. Turn on", on: "Voice guidance on. Turn off", speaking: "Voice guidance on, speaking. Turn off", novoice: "Voice guidance unavailable: no voice. Details" },
    tipTitle: "Voice guidance on", tipText: "Steps will be read aloud. This setting is kept for your next installs.", tipOk: "Got it",
    repeat: "Repeat phrase",
    nvTitle: "No voice for English",
    nvText: { android: "To hear the steps, your phone needs an English voice. Download it in text-to-speech settings — internet is only needed for the download.",
      ios: "To hear the steps, add an English voice in iPhone Settings. Internet is only needed for the download.",
      desktop: "To hear the steps, add an English voice in system settings. Internet is only needed for the download." },
    nvPath: { ios: [["", "Settings → Accessibility → Spoken Content → Voices → English"]],
      desktop: [["Windows", "Settings → Time & language → Speech → Add voices"], ["macOS", "System Settings → Accessibility → Spoken Content → System voice → Manage Voices"]] },
    nvInstall: "Install voice", nvLater: "Later", nvOk: "Got it",
  },
};

// ── Глобальное хранилище (прототипы) ───────────────────────────────────────
const KEY = "ps-hf-voice", SEEN = "ps-hf-voice-tip";
const q = new URLSearchParams(location.search);
const ls = (k) => { try { return localStorage.getItem(k); } catch (e) { return null; } };
const lsSet = (k, v) => { try { localStorage.setItem(k, v); } catch (e) {} };
const store = {
  on: ls(KEY) === "1", speaking: false, tip: false, sheet: false,
  voice: q.get("voice") || "ok",
  os: q.get("hfos") || (/Desktop/i.test(location.pathname) ? "desktop" : "android"),
  subs: new Set(), timer: 0,
  set(p) { Object.assign(store, p); store.snap = null; store.subs.forEach((f) => f()); },
  snapshot() { return store.snap || (store.snap = { on: store.on, speaking: store.speaking, tip: store.tip, sheet: store.sheet, voice: store.voice, os: store.os }); },
  subscribe(f) { store.subs.add(f); return () => store.subs.delete(f); },
  speak(ms = 2600) { clearTimeout(store.timer); store.set({ speaking: true }); store.timer = setTimeout(() => store.set({ speaking: false }), ms); },
  toggle() {
    if (store.voice === "none") return store.set({ sheet: true });
    const on = !store.on; lsSet(KEY, on ? "1" : "0");
    const first = on && ls(SEEN) !== "1"; if (first) lsSet(SEEN, "1");
    store.set({ on, tip: first, speaking: false });
    if (on) store.speak(); else clearTimeout(store.timer);
  },
};
window.HF_STORE = store;

const HfVoiceCtx = React.createContext(null);

// Провайдер доски: значения фиксированы, нажатия переключают локально.
function HfVoiceProvider({ state = "off", tip = false, sheet = false, os = "android", repeat = false, lang = "ru", children }) {
  const [s, setS] = React.useState({ state, tip, sheet });
  React.useEffect(() => setS({ state, tip, sheet }), [state, tip, sheet]);
  const v = {
    state: s.state, tip: s.tip, sheet: s.sheet, os, repeat, lang,
    toggle: () => setS((x) => x.state === "novoice" ? { ...x, sheet: true } : { ...x, state: x.state === "off" ? "on" : "off", tip: false }),
    closeTip: () => setS((x) => ({ ...x, tip: false })),
    closeSheet: () => setS((x) => ({ ...x, sheet: false })),
    replay: () => setS((x) => ({ ...x, state: "speaking" })),
  };
  return <HfVoiceCtx.Provider value={v}>{children}</HfVoiceCtx.Provider>;
}

function useHf() {
  const ctx = React.useContext(HfVoiceCtx);
  const g = React.useSyncExternalStore(store.subscribe, store.snapshot);
  if (ctx) return ctx.state === "unsupported" ? null : ctx;
  if (g.voice === "unsupported") return null;
  return {
    state: g.voice === "none" ? "novoice" : !g.on ? "off" : g.speaking ? "speaking" : "on",
    tip: g.tip, sheet: g.sheet, os: g.os, auto: true, repeat: q.get("hfrepeat") === "1", lang: q.get("lang") === "en" ? "en" : "ru",
    toggle: store.toggle, closeTip: () => store.set({ tip: false }), closeSheet: () => store.set({ sheet: false }), replay: () => store.speak(),
  };
}

// ── Кнопка «Озвучка» (48×48, в actions шапки) ───────────────────────────────
function HfVoiceIcon() {
  const c = useHf();
  const ref = React.useRef(null);
  const [host, setHost] = React.useState(null);
  React.useLayoutEffect(() => { if (ref.current) setHost(ref.current.closest("[data-screen-label]") || ref.current.closest("[data-hf-host]")); }, []);
  React.useEffect(() => {
    if (!c || !c.tip || !c.auto) return;
    const t = setTimeout(c.closeTip, 7000); return () => clearTimeout(t);
  }, [c && c.tip]);
  if (!c) return null;
  const L = T[c.lang] || T.ru;
  const st = c.state, on = st === "on" || st === "speaking";
  const sheet = c.sheet && host ? ReactDOM.createPortal(<HfNoVoice os={c.os} lang={c.lang} host={host} onClose={c.closeSheet} />, host) : null;
  return (
    <span ref={ref} style={{ position: "relative", display: "inline-flex", alignItems: "center", gap: 8 }}>
      {c.repeat && on && (
        <button type="button" className="hf-btn hf-off" aria-label={L.repeat} title={L.repeat} onClick={c.replay}
          style={{ width: 48, height: 48, borderRadius: "50%", border: "none", padding: 0, cursor: "pointer", display: "grid", placeItems: "center", background: "transparent", color: "var(--on-surface)" }}>
          <CmpIcon icon="replay" size={26} color="currentColor" />
        </button>
      )}
      <button type="button" className={"hf-btn" + (on ? "" : " hf-off")} aria-pressed={on} aria-label={L.a11y[st]} title={L.a11y[st]} onClick={c.toggle}
        style={{ position: "relative", width: 48, height: 48, flexShrink: 0, borderRadius: "50%", padding: 0, cursor: "pointer", display: "grid", placeItems: "center", boxSizing: "border-box",
          background: on ? "var(--primary)" : "transparent", color: on ? "var(--on-primary)" : st === "novoice" ? "var(--on-surface-variant)" : "var(--on-surface)",
          border: on ? "none" : "2px solid var(--outline)", animation: st === "speaking" ? "hfPulse 1.6s ease-out infinite" : "none" }}>
        <CmpIcon icon={on ? (st === "speaking" ? "graphic_eq" : "volume_up") : "volume_off"} size={26} color="currentColor" />
        {st === "novoice" && <span aria-hidden="true" style={{ position: "absolute", top: 4, right: 4, width: 12, height: 12, borderRadius: "50%", background: "var(--error)", boxShadow: "0 0 0 2px var(--background)" }} />}
      </button>
      {c.tip && <HfTip L={L} onClose={c.closeTip} />}
      {sheet}
    </span>
  );
}

// ── Подсказка при первом включении (у кнопки, один раз) ───────────────────────
function HfTip({ L, onClose }) {
  return (
    <div role="status" style={{ position: "absolute", top: "calc(100% + 10px)", right: 0, zIndex: 20, width: 288, boxSizing: "border-box", padding: "14px 14px 8px 16px", borderRadius: 16,
      background: "var(--inverse-surface, #2C3639)", color: "var(--inverse-on-surface, #fff)", boxShadow: "0 6px 20px rgba(0,0,0,.28)", animation: "hfIn .2s ease both", textAlign: "left" }}>
      <span aria-hidden="true" style={{ position: "absolute", top: -6, right: 18, width: 12, height: 12, background: "inherit", transform: "rotate(45deg)", borderRadius: 2 }} />
      <div style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
        <CmpIcon icon="volume_up" size={22} color="currentColor" />
        <div style={{ display: "flex", flexDirection: "column", gap: 4, minWidth: 0 }}>
          <span style={{ fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 700, lineHeight: 1.3 }}>{L.tipTitle}</span>
          <span style={{ fontFamily: "var(--font-sans)", fontSize: 15, lineHeight: 1.4, textWrap: "pretty" }}>{L.tipText}</span>
        </div>
      </div>
      <div style={{ display: "flex", justifyContent: "flex-end" }}>
        <button type="button" className="hf-btn" onClick={onClose} style={{ minHeight: 48, padding: "0 16px", border: "none", borderRadius: 999, background: "transparent", cursor: "pointer",
          color: "var(--inverse-primary, #9fc2ff)", fontFamily: "var(--font-sans)", fontSize: 16, fontWeight: 700 }}>{L.tipOk}</button>
      </div>
    </div>
  );
}

// ── «Нет голоса»: телефон — нижний лист, широкое окно — диалог ────────────────
function HfNoVoice({ os = "android", lang = "ru", host, onClose }) {
  const L = T[lang] || T.ru;
  const wide = host ? host.clientWidth >= 600 : false;
  const paths = L.nvPath[os];
  const actions = os === "android"
    ? [<CmpButton key="i" text={L.nvInstall} icon="settings" fullWidth onClick={onClose} style={{ height: 56, fontSize: 17 }} />, <CmpTextButton key="l" text={L.nvLater} onClick={onClose} style={{ height: 56, width: "100%", fontSize: 17 }} />]
    : [<CmpButton key="o" text={L.nvOk} fullWidth onClick={onClose} style={{ height: 56, fontSize: 17 }} />];
  const body = (
    <React.Fragment>
      <span style={{ width: 56, height: 56, borderRadius: "50%", display: "grid", placeItems: "center", background: "var(--tertiary-container)", color: "var(--on-tertiary-container)" }}><CmpIcon icon="voice_over_off" size={30} color="currentColor" /></span>
      <h2 style={{ margin: 0, fontFamily: "var(--font-sans)", fontSize: 22, lineHeight: 1.25, fontWeight: 700, color: "var(--on-surface)" }}>{L.nvTitle}</h2>
      <p style={{ margin: 0, fontFamily: "var(--font-sans)", fontSize: 17, lineHeight: 1.45, color: "var(--on-surface-variant)", textWrap: "pretty" }}>{L.nvText[os]}</p>
      {paths && <div style={{ display: "flex", flexDirection: "column", gap: 8, width: "100%" }}>{paths.map(([k, p]) => (
        <div key={k || p} style={{ padding: "12px 14px", borderRadius: 12, background: "var(--background-surface-highlight)", display: "flex", flexDirection: "column", gap: 2 }}>
          {k && <span style={{ fontFamily: "var(--font-sans)", fontSize: 14, fontWeight: 700, color: "var(--on-surface-variant)" }}>{k}</span>}
          <span style={{ fontFamily: "var(--font-sans)", fontSize: 16, lineHeight: 1.4, fontWeight: 600, color: "var(--on-surface)" }}>{p}</span>
        </div>))}</div>}
      <div style={{ display: "flex", flexDirection: "column", gap: 8, width: "100%", marginTop: 4 }}>{actions}</div>
    </React.Fragment>
  );
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 40, display: "flex", alignItems: wide ? "center" : "flex-end", justifyContent: "center", padding: wide ? 24 : 0, boxSizing: "border-box" }}>
      <div onClick={onClose} style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--scrim, #000) 42%, transparent)" }} />
      <div role="dialog" aria-modal="true" aria-label={L.nvTitle} style={{ position: "relative", width: "100%", maxWidth: wide ? 480 : "none", boxSizing: "border-box",
        padding: wide ? "28px 24px 20px" : "12px 20px 20px", borderRadius: wide ? 28 : "28px 28px 0 0", background: "var(--surface-container-high)",
        display: "flex", flexDirection: "column", alignItems: "flex-start", gap: 14, animation: wide ? "hfIn .2s ease both" : "hfUp .25s ease both" }}>
        {!wide && <span aria-hidden="true" style={{ alignSelf: "center", width: 36, height: 4, borderRadius: 2, background: "var(--outline)", marginBottom: 6 }} />}
        {body}
      </div>
    </div>
  );
}

Object.assign(window, { HfVoiceCtx, HfVoiceProvider, HfVoiceIcon, HfNoVoice, HF_TEXT: T });
})();
