// AppUpdateScreen — Pandora Spec desktop app-version update (download a newer
// build, e.g. .dmg). Built ONLY from design-system Cmp* components.
//
//   • UpdateScreen — stateless BODY, rendered per `state`. Chrome (top bar) is
//     supplied by the host, so it embeds inside the main screen (route=update)
//     and inside this feature's own AppUpdate.html the same way.
//   • UpdateFlow   — stateful controller: checking → available → downloading
//     (determinate progress) → downloaded → (install) up-to-date. Renders the
//     body only. Used by the main screen's update route and by AppUpdateApp.
//   • AppUpdateApp — full standalone screen (top bar + UpdateFlow), or pinned to
//     one explicit state for the states gallery.

const AU_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpSurface, CmpDivider,
  CmpButton, CmpTextButton, CmpCircularProgressIndicator,
} = AU_DS;

const UPDATE_STATES = ["available", "checking", "downloading", "downloaded", "uptodate", "error"];
const AU_ICON = { appUpdate: "system_update", download: "download" };
const DEFAULT_NOTES = [
  "Ускорена запись прошивок и синхронизация базы",
  "Новые поддерживаемые автомобили и точки подключения",
  "Исправления стабильности при работе по CAN/LIN",
];

// ── Container-width hook → padding / max-width class ─────────────────────────
function useAuMode() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const remeasure = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => { ro.disconnect(); window.removeEventListener("resize", remeasure); };
  }, []);
  let mode = "compact";
  if (w >= 1200) mode = "large";
  else if (w >= 840) mode = "expanded";
  else if (w >= 600) mode = "medium";
  return [ref, mode];
}

function VerPill({ label, value, accent = false }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 2, padding: "10px 16px", borderRadius: 12, minWidth: 120, background: accent ? "var(--primary-container)" : "var(--surface-variant)", color: accent ? "var(--on-primary-container)" : "var(--on-surface-variant)" }}>
      <CmpText variant="labelMedium" color="currentColor">{label}</CmpText>
      <CmpText variant="titleMedium" color="currentColor">{value}</CmpText>
    </div>
  );
}

const HEAD = {
  available:   { icon: AU_ICON.appUpdate, tone: "primary", title: "Доступна новая версия",       sub: (c, l) => `Версия ${l} · для macOS (.dmg)` },
  checking:    { icon: null,              tone: "primary", title: "Проверка обновлений",          sub: () => "Ищем новую версию…" },
  downloading: { icon: AU_ICON.download,  tone: "primary", title: "Загрузка обновления",          sub: (c, l, s) => `Версия ${l} · ${s} МБ` },
  downloaded:  { icon: "CheckCircle",     tone: "primary", title: "Обновление готово к установке", sub: (c, l) => `Версия ${l} загружена` },
  uptodate:    { icon: "CheckCircle",     tone: "success", title: "Установлена последняя версия",  sub: (c) => `Версия ${c}` },
  error:       { icon: "AlertCircle",     tone: "error",   title: "Не удалось проверить обновления", sub: () => "Проверьте подключение и повторите" },
};

// ── Stateless body ────────────────────────────────────────────────────────────
function UpdateScreen({
  mode = "expanded", state = "available",
  current = "2.4.1", latest = "2.5.0", sizeMB = 84, progress = 0, notes = DEFAULT_NOTES,
  onDownload = () => {}, onCancel = () => {}, onInstall = () => {}, onLater = () => {}, onCheck = () => {},
}) {
  const pad = mode === "compact" ? 16 : mode === "medium" ? 24 : 32;
  const h = HEAD[state] || HEAD.available;
  const tileBg = h.tone === "error" ? "var(--error-container)" : h.tone === "success" ? "color-mix(in srgb, var(--success) 20%, transparent)" : "var(--primary-container)";
  const tileFg = h.tone === "error" ? "var(--on-error-container)" : h.tone === "success" ? "var(--success)" : "var(--on-primary-container)";

  return (
    <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
      <div style={{ maxWidth: 640, margin: "0 auto", padding: `${pad}px`, boxSizing: "border-box" }}>
        <CmpSurface border radius={16} color="var(--surface-container-low)" style={{ padding: 24, display: "flex", flexDirection: "column", gap: 20 }}>
          {/* Header */}
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <div style={{ width: 56, height: 56, borderRadius: "var(--radius-m)", background: tileBg, color: tileFg, display: "grid", placeItems: "center", flex: "0 0 auto" }}>
              {state === "checking"
                ? <CmpCircularProgressIndicator size={28} strokeWidth={3} />
                : <CmpIcon icon={h.icon} size={30} color={tileFg} />}
            </div>
            <div style={{ minWidth: 0 }}>
              <CmpText variant="headlineSmall">{h.title}</CmpText>
              <div style={{ marginTop: 2 }}>
                <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{h.sub(current, latest, sizeMB)}</CmpText>
              </div>
            </div>
          </div>

          {/* Version pills (available / downloaded) */}
          {(state === "available" || state === "downloaded") && (
            <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
              <VerPill label="Установлено" value={current} />
              <CmpIcon icon="ArrowForward" size={20} color="var(--on-surface-variant)" />
              <VerPill label="Доступно" value={latest} accent />
            </div>
          )}

          {/* Determinate progress (downloading) */}
          {state === "downloading" && (
            <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
              <div style={{ height: 6, borderRadius: 999, background: "var(--surface-variant)", overflow: "hidden" }}>
                <div style={{ height: "100%", width: `${progress}%`, background: "var(--primary)", borderRadius: 999, transition: "width .2s linear" }} />
              </div>
              <CmpText variant="labelMedium" color="var(--on-surface-variant)">{`Загрузка… ${Math.round(progress)}% · ${Math.round(sizeMB * progress / 100)} из ${sizeMB} МБ`}</CmpText>
            </div>
          )}

          {/* What's new (available) */}
          {state === "available" && (
            <React.Fragment>
              <CmpDivider />
              <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                <CmpText variant="titleMedium">Что нового</CmpText>
                <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                  {notes.map((n, i) => (
                    <div key={i} style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
                      <CmpIcon icon="Check" size={18} color="var(--success)" style={{ marginTop: 2 }} />
                      <CmpText variant="bodyMedium" color="var(--on-surface-variant)" style={{ textWrap: "pretty" }}>{n}</CmpText>
                    </div>
                  ))}
                </div>
              </div>
            </React.Fragment>
          )}

          {/* Actions */}
          <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap", paddingTop: 4 }}>
            {state === "available" && <React.Fragment>
              <CmpButton text="Скачать обновление (.dmg)" icon={AU_ICON.download} onClick={onDownload} />
              <CmpTextButton text="Позже" onClick={onLater} />
            </React.Fragment>}
            {state === "checking" && <CmpButton text="Проверка…" disabled onClick={() => {}} />}
            {state === "downloading" && <CmpTextButton text="Отменить" onClick={onCancel} />}
            {state === "downloaded" && <React.Fragment>
              <CmpButton text="Установить и перезапустить" icon="Refresh" onClick={onInstall} />
              <CmpTextButton text="Позже" onClick={onLater} />
            </React.Fragment>}
            {state === "uptodate" && <React.Fragment>
              <CmpButton text="Проверить снова" icon="Refresh" onClick={onCheck} />
              <CmpTextButton text="Готово" onClick={onLater} />
            </React.Fragment>}
            {state === "error" && <React.Fragment>
              <CmpButton text="Повторить" icon="Refresh" onClick={onCheck} />
              <CmpTextButton text="Позже" onClick={onLater} />
            </React.Fragment>}
          </div>
        </CmpSurface>
      </div>
    </div>
  );
}

// ── Stateful body: full download flow ────────────────────────────────────────
function UpdateFlow({ mode = "expanded", initialState = "available", current = "2.4.1", latest = "2.5.0", sizeMB = 84, onLater = () => {} }) {
  const { useState, useRef, useEffect } = React;
  const [state, setState] = useState(initialState);
  const [progress, setProgress] = useState(0);
  const t = useRef(null);
  const iv = useRef(null);
  useEffect(() => () => { clearTimeout(t.current); clearInterval(iv.current); }, []);

  const check = () => {
    setState("checking");
    clearTimeout(t.current);
    t.current = setTimeout(() => setState("available"), 1300);
  };
  const download = () => {
    setState("downloading");
    setProgress(0);
    clearInterval(iv.current);
    iv.current = setInterval(() => {
      setProgress((p) => {
        const next = p + 100 / 22; // ~2.2s
        if (next >= 100) { clearInterval(iv.current); setState("downloaded"); return 100; }
        return next;
      });
    }, 100);
  };
  const cancel = () => { clearInterval(iv.current); setProgress(0); setState("available"); };
  const install = () => {
    setState("checking");
    clearTimeout(t.current);
    t.current = setTimeout(() => setState("uptodate"), 1200);
  };

  return (
    <UpdateScreen
      mode={mode} state={state} current={current} latest={latest} sizeMB={sizeMB} progress={progress}
      onDownload={download} onCancel={cancel} onInstall={install} onCheck={check} onLater={onLater}
    />
  );
}

// ── Full standalone screen (top bar + flow), or pinned to one state ──────────
function AppUpdateApp({ pinnedState, current = "2.4.1", latest = "2.5.0", sizeMB = 84, onBack, showBack = false }) {
  const [rootRef, mode] = useAuMode();
  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", width: "100%", display: "flex", flexDirection: "column", overflow: "hidden", background: "var(--background)", color: "var(--on-background)" }}>
      <style>{`.au-focusable:focus-visible{outline:2px solid var(--primary);outline-offset:2px}`}</style>
      <CmpTopAppBar
        centered
        title="Обновление приложения"
        style={{ flex: "0 0 auto" }}
        navigationIcon={showBack ? <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={onBack} /> : undefined}
      />
      {pinnedState
        ? <UpdateScreen mode={mode} state={pinnedState} current={current} latest={latest} sizeMB={sizeMB} progress={pinnedState === "downloading" ? 46 : 0} />
        : <UpdateFlow mode={mode} current={current} latest={latest} sizeMB={sizeMB} onLater={() => {}} />}
    </div>
  );
}

window.AppUpdate = { UpdateScreen, UpdateFlow, AppUpdateApp, VerPill, useAuMode, UPDATE_STATES };
