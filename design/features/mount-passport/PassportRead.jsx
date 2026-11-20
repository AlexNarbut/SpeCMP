// PassportRead.jsx — «Считывание блока» (паспорт монтажа), телефон.
// Приложение по этапам читает блок; монтажник видит ход и ничего не нажимает.
// После последнего этапа предотчёт открывается сам.
//
// PassportRead props:
//   context  "fast" (внутри мастера быстрого монтажа, Bluetooth) | "ext" (расширенный монтаж)
//   conn     "bt" | "usb"
//   state    live | run1 | run3 | done | partFail | blockFail | lost | offline | usb | setRun | setSkip | setNa
//   onDone   () => void — открыть предотчёт;  onCancel

const PR_DS = window.PandoraSpecDesignSystem_b8d6e1;
const PrText = (p) => <PR_DS.CmpText {...p} style={{ display: "block", ...p.style }} />;
const { CmpIcon: PrIcon, CmpButton: PrButton, CmpOutlinedButton: PrOutlined, CmpIconButton: PrIconBtn, CmpTopAppBar: PrTopBar, CmpLinearProgressIndicator: PrLinear } = PR_DS;
const PrTextBtn = (p) => (PR_DS.CmpTextButton ? <PR_DS.CmpTextButton {...p} /> : <PrOutlined {...p} />);
const PR_EASE = "cubic-bezier(.23,1,.32,1)";
const prMuted = "var(--foreground-on-dark-secondary-default)";

(function prCss() {
  if (document.getElementById("pr-css")) return;
  const s = document.createElement("style");
  s.id = "pr-css";
  s.textContent =
    "@keyframes prSpin{to{transform:rotate(360deg)}}" +
    "@keyframes prPop{from{transform:scale(.6);opacity:0}to{transform:none;opacity:1}}" +
    "@keyframes prIn{from{opacity:0;transform:translateY(4px)}to{opacity:1;transform:none}}" +
    "@media (prefers-reduced-motion:reduce){.pr-anim{animation:none!important;transition:none!important}}";
  document.head.appendChild(s);
})();

const PR_STAGES = [
  { k: "block", t: "Блок", wait: "Модель, ID, версия ПО, CAN", ok: "Pandora DX-91 · ID 1234567 · ПО 3.57" },
  { k: "params", t: "Параметры", wait: "Серийный номер, IMEI, загрузчик, Bluetooth", ok: "DX91-0042-118734 · IMEI …8734" },
  { k: "devices", t: "Устройства системы", wait: "Метки, брелоки, модули", ok: "5 устройств" },
  { k: "settings", t: "Настройки", wait: "Снимок всех настроек блока", ok: "Снимок сохранён · в документ не попадает" },
  { k: "ready", t: "Готовим паспорт", wait: "", ok: "Готово" },
];

// Статусы этапов для статичных состояний.
function prModel(state, conn) {
  const S = (a) => a.map((st, i) => (typeof st === "string" ? { st } : st));
  switch (state) {
    case "run1": return { stages: S(["run", "wait", "wait", "wait", "wait"]), p: 0.08 };
    case "run3": return { stages: S(["ok", "ok", "run", "wait", "wait"]), p: 0.42 };
    case "usb": return { stages: S(["ok", "ok", { st: "run", sub: "Читаем по USB — только список, без адресов" }, "wait", "wait"]), p: 0.42 };
    case "done": return { stages: S(["ok", "ok", "ok", "ok", "ok"]), p: 1, done: true };
    case "partFail": return { stages: S(["ok", { st: "fail", sub: "Не прочитано — можно перечитать в паспорте" }, "run", "wait", "wait"]), p: 0.45 };
    case "blockFail": return { stages: S([{ st: "fail", sub: "Не удалось прочитать" }, "wait", "wait", "wait", "wait"]), p: 0.05, stop: "blockFail" };
    case "lost": return { stages: S(["ok", "ok", { st: "fail", sub: "Связь прервалась" }, "wait", "wait"]), p: 0.4, stop: "lost" };
    case "offline": return { stages: S(["wait", "wait", "wait", "wait", "wait"]), p: 0, stop: "offline" };
    case "setRun": return { stages: S(["ok", "ok", "ok", { st: "run", long: true }, "wait"]), p: 0.66, elapsed: "0:24" };
    case "setSkip": return { stages: S(["ok", "ok", "ok", { st: "skip", sub: "Пропущено — можно перечитать в паспорте" }, "run"]), p: 0.92 };
    case "setNa": return { stages: S(["ok", "ok", "ok", { st: "na", sub: "Недоступно: сначала обновите CAN-прошивку" }, "run"]), p: 0.92 };
    default: return { stages: S(["wait", "wait", "wait", "wait", "wait"]), p: 0 };
  }
}

function PrGlyph({ st, n }) {
  const base = { width: 40, height: 40, borderRadius: 999, flex: "none", display: "grid", placeItems: "center", boxSizing: "border-box", position: "relative", zIndex: 1 };
  if (st === "run") return (
    <span style={{ ...base, background: "var(--background)" }}>
      <span className="pr-anim" style={{ position: "absolute", inset: 0, borderRadius: 999, border: "3px solid var(--brand-primary-highlight)", borderTopColor: "var(--primary)", animation: "prSpin .8s linear infinite" }} />
      <PrText variant="headline3" color="var(--primary)">{n}</PrText>
    </span>
  );
  if (st === "ok") return <span className="pr-anim" style={{ ...base, background: "var(--success-default)", animation: `prPop 260ms ${PR_EASE}` }}><PrIcon icon="check" size={24} color="#fff" /></span>;
  if (st === "fail") return <span className="pr-anim" style={{ ...base, background: "var(--warning-highlight)", border: "2px solid var(--warning-default)", animation: `prPop 260ms ${PR_EASE}` }}><PrIcon icon="priority_high" size={22} color="var(--warning-default)" /></span>;
  if (st === "skip" || st === "na") return <span style={{ ...base, background: "var(--background)", border: "2px solid var(--border-tertiary)" }}><PrIcon icon={st === "skip" ? "redo" : "remove"} size={22} color={prMuted} /></span>;
  return <span style={{ ...base, background: "var(--background)", border: "2px solid var(--border-light)" }}><PrText variant="headline3" color={prMuted}>{n}</PrText></span>;
}

function PassportRead({ context = "fast", conn = "bt", state = "live", onDone, onCancel, onSkipSettings }) {
  const [live, setLive] = React.useState({ i: 0, stages: ["run", "wait", "wait", "wait", "wait"], skipped: false });
  const [secs, setSecs] = React.useState(0);
  const timers = React.useRef([]);
  const isLive = state === "live";

  React.useEffect(() => {
    if (!isLive) return;
    const dur = [1100, 1000, 1300, 3800, 900];
    let t = 0;
    const step = (i) => {
      timers.current.push(setTimeout(() => {
        setLive((s) => {
          if (s.skipped && i === 3) return s;
          const st = [...s.stages]; st[i] = "ok"; if (i + 1 < 5) st[i + 1] = "run";
          return { ...s, i: i + 1, stages: st };
        });
      }, t += dur[i]));
    };
    for (let i = 0; i < 5; i++) step(i);
    const tick = setInterval(() => setSecs((x) => x + 1), 1000);
    timers.current.push(tick);
    return () => { timers.current.forEach((x) => { clearTimeout(x); clearInterval(x); }); timers.current = []; };
  }, [isLive]);

  const allOk = isLive && live.i >= 5;
  React.useEffect(() => { if (allOk && onDone) { const x = setTimeout(onDone, 900); return () => clearTimeout(x); } }, [allOk]);

  const skipLive = () => setLive((s) => { const st = [...s.stages]; st[3] = "skip"; st[4] = "run"; timers.current.push(setTimeout(() => setLive((q) => ({ ...q, i: 5, stages: q.stages.map((x, j) => (j === 4 ? "ok" : x)) })), 900)); return { ...s, i: 4, stages: st, skipped: true }; });

  const m = isLive
    ? { stages: live.stages.map((st, i) => (i === 3 && st === "skip" ? { st, sub: "Пропущено — можно перечитать в паспорте" } : { st, long: i === 3 && st === "run" })), p: Math.min(1, live.i / 5 + (live.i < 5 ? 0.06 : 0)), done: allOk, elapsed: `0:${String(secs).padStart(2, "0")}` }
    : prModel(state, conn);
  const usb = conn === "usb" || state === "usb";

  const head = {
    blockFail: { icon: "error", tone: "var(--error-default)", t: "Не удалось прочитать блок", s: "Без модели и ID паспорт не оформить. Проверьте подключение и повторите." },
    lost: { icon: "link_off", tone: "var(--warning-default)", t: "Связь с блоком потеряна", s: "Прочитанное сохранено. Можно повторить или оформить паспорт без остальных данных." },
    offline: { icon: "bluetooth_disabled", tone: "var(--warning-default)", t: "Блок не на связи", s: "Блок не отвечает. В паспорт попадёт то, что знает мастер: автомобиль, конфигурация, выполненные работы." },
  }[m.stop];

  const title = m.done ? "Всё прочитано" : head ? null : "Читаем блок";
  const sub = m.done ? "Открываем паспорт…" : `Pandora DX-91 · ${usb ? "USB" : "Bluetooth"} · не отключайте блок`;

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)", fontFamily: "var(--font-sans)" }}>
      <PrTopBar title="Считывание блока" subtitle={context === "fast" ? "Быстрый монтаж · паспорт" : "Расширенный монтаж · паспорт"}
        navigationIcon={<PrIconBtn icon="close" contentDescription="Отмена" onClick={onCancel} />} />
      <PrLinear progress={m.p} style={{ flex: "none" }} />
      <div style={{ flex: 1, minHeight: 0, overflowY: "auto", padding: "24px 24px 16px", display: "flex", flexDirection: "column", gap: 24, width: "100%", maxWidth: 640, margin: "0 auto", boxSizing: "border-box" }}>
        {head ? (
          <div className="pr-anim" style={{ display: "flex", flexDirection: "column", gap: 10, animation: `prIn 220ms ${PR_EASE}` }}>
            <span style={{ width: 56, height: 56, borderRadius: 16, display: "grid", placeItems: "center", background: m.stop === "blockFail" ? "var(--error-highlight)" : "var(--warning-highlight)" }}><PrIcon icon={head.icon} size={32} color={head.tone} /></span>
            <PrText variant="title2">{head.t}</PrText>
            <PrText variant="paragraph1" color={prMuted} style={{ textWrap: "pretty" }}>{head.s}</PrText>
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <PrText variant="title2">{title}</PrText>
            <PrText variant="paragraph2" color={prMuted}>{sub}</PrText>
          </div>
        )}

        <ol style={{ listStyle: "none", margin: 0, padding: 0, display: "flex", flexDirection: "column", position: "relative" }}>
          {PR_STAGES.map((s, i) => {
            const x = m.stages[i];
            const st = x.st;
            let line = st === "ok" ? s.ok : st === "wait" ? s.wait : st === "run" ? (x.long ? "Самый долгий этап — может занять до минуты" : "Читаем…") : "";
            if (s.k === "devices" && st === "ok" && usb) line = "5 устройств · по USB, без адресов";
            if (x.sub) line = x.sub;
            const color = st === "fail" ? "var(--warning-default)" : st === "ok" ? "var(--on-surface)" : prMuted;
            const last = i === PR_STAGES.length - 1;
            return (
              <li key={s.k} style={{ display: "flex", gap: 16, position: "relative", paddingBottom: last ? 0 : 20, minHeight: 56 }}>
                {!last && <span aria-hidden style={{ position: "absolute", left: 19, top: 40, bottom: 0, width: 2, background: st === "ok" ? "var(--success-default)" : "var(--border-highlight)", transition: "background-color 300ms ease" }} />}
                <PrGlyph st={st} n={i + 1} />
                <div style={{ flex: 1, minWidth: 0, paddingTop: 8, display: "flex", flexDirection: "column", gap: 4 }}>
                  <div style={{ display: "flex", alignItems: "baseline", gap: 8 }}>
                    <PrText variant="headline2" color={st === "wait" ? prMuted : undefined} style={{ flex: 1 }}>{s.t}</PrText>
                    {x.long && m.elapsed && <PrText variant="additional0" color={prMuted} style={{ fontVariantNumeric: "tabular-nums" }}>{m.elapsed}</PrText>}
                  </div>
                  {line && <PrText variant="paragraph2" color={color} weight={st === "ok" ? 700 : 500} style={{ textWrap: "pretty" }}>{line}</PrText>}
                  {x.long && (
                    <div style={{ paddingTop: 4, marginLeft: -12 }}>
                      <PrTextBtn text="Пропустить" icon="redo" onClick={isLive ? skipLive : onSkipSettings} />
                    </div>
                  )}
                </div>
              </li>
            );
          })}
        </ol>
      </div>

      <div style={{ flex: "none", borderTop: "1px solid var(--border-highlight)" }}><div style={{ padding: "12px 20px 16px", display: "flex", flexDirection: "column", gap: 8, maxWidth: 640, margin: "0 auto", boxSizing: "border-box" }}>
        {m.stop === "blockFail" && <React.Fragment><PrButton text="Повторить" icon="refresh" size="XL" fullWidth /><PrOutlined text="Отмена" fullWidth onClick={onCancel} /></React.Fragment>}
        {m.stop === "lost" && <React.Fragment><PrButton text="Повторить" icon="refresh" size="XL" fullWidth /><PrOutlined text="Продолжить без этих данных" fullWidth onClick={onDone} /></React.Fragment>}
        {m.stop === "offline" && <React.Fragment><PrButton text="Повторить" icon="refresh" size="XL" fullWidth /><PrOutlined text="Оформить без считывания" fullWidth onClick={onDone} /></React.Fragment>}
        {!m.stop && (m.done
          ? <PrButton text="Открываем паспорт…" size="XL" fullWidth loading />
          : <PrOutlined text="Отмена" fullWidth onClick={onCancel} />)}
      </div></div>
    </div>
  );
}

Object.assign(window, { PassportRead });
