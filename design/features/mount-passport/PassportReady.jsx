// PassportReady.jsx — «Паспорт готов» (телефон): предпросмотр документа постранично + действия.
// PassportReady props: state "forming" | "ready" | "error";  variant — вариант документа (full | min | long);
//   platform "mobile" | "desktop" (на desktop главная — «Сохранить файл», «Поделиться» нет);  saveFail — демо ошибки сохранения
//   onEdit, onDone

const RD_DS = window.PandoraSpecDesignSystem_b8d6e1;
const RdText = (p) => <RD_DS.CmpText {...p} style={{ display: "block", ...p.style }} />;
const { CmpIcon: RdIcon, CmpButton: RdButton, CmpOutlinedButton: RdOutlined, CmpIconButton: RdIconBtn, CmpTopAppBar: RdTopBar, CmpSnackbar: RdSnack } = RD_DS;
const RdTextBtn = (p) => (RD_DS.CmpTextButton ? <RD_DS.CmpTextButton {...p} /> : <RdOutlined {...p} />);
const RD_EASE = "cubic-bezier(.23,1,.32,1)";
const rdMuted = "var(--foreground-on-dark-secondary-default)";
const RD_PAGE_W = 793.7, RD_PAGE_H = 1122.5;

(function rdCss() {
  if (document.getElementById("rd-css")) return;
  const s = document.createElement("style");
  s.id = "rd-css";
  s.textContent =
    "@keyframes rdSpin{to{transform:rotate(360deg)}}" +
    "@keyframes rdUp{from{opacity:0;transform:translateY(12px)}to{opacity:1;transform:none}}" +
    "@keyframes rdPulse{0%,100%{opacity:.55}50%{opacity:1}}" +
    ".rd-tile{transition:background-color 150ms ease,transform 160ms cubic-bezier(.23,1,.32,1)}" +
    "@media (hover:hover) and (pointer:fine){.rd-tile:hover{background-color:var(--background-surface-highlight)}}" +
    ".rd-tile:active{transform:scale(.97)}.rd-tile:focus-visible{outline:2px solid var(--border-focus);outline-offset:2px}" +
    "@media (prefers-reduced-motion:reduce){.rd-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

// Лист документа в масштабе — переиспользуется в desktop-предпросмотре.
function PassportSheet({ page, width }) {
  const k = width / RD_PAGE_W;
  return (
    <div style={{ width, height: RD_PAGE_H * k, flex: "none", position: "relative", boxShadow: "0 1px 2px rgba(10,26,58,.12), 0 6px 20px rgba(10,26,58,.14)", background: "#fff" }}>
      <div style={{ position: "absolute", left: 0, top: 0, transform: `scale(${k})`, transformOrigin: "top left" }}>{page}</div>
    </div>
  );
}

function RdTile({ icon, text, onClick }) {
  return (
    <button className="rd-tile" onClick={onClick} style={{ all: "unset", boxSizing: "border-box", flex: "1 1 0", minWidth: 0, minHeight: 72, borderRadius: 16, border: "1px solid var(--border-light)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 6, cursor: "pointer", padding: "8px 4px" }}>
      <RdIcon icon={icon} size={26} color="var(--on-surface)" />
      <RdText variant="additional0" align="center">{text}</RdText>
    </button>
  );
}

function PassportReady({ state = "ready", variant = "full", platform = "mobile", saveFail = false, onEdit, onDone }) {
  const [st, setSt] = React.useState(state);
  const [page, setPage] = React.useState(1);
  const [snack, setSnack] = React.useState(null);
  const [menu, setMenu] = React.useState(false);
  const [xp, setXp] = React.useState(false);
  const [boxW, setBoxW] = React.useState(360);
  const scRef = React.useRef(null), snT = React.useRef(null);
  React.useEffect(() => setSt(state), [state]);
  React.useEffect(() => {
    if (st !== "forming" || state !== "live") return;
  }, [st]);
  React.useLayoutEffect(() => { const el = scRef.current; if (el) setBoxW(el.clientWidth); });

  const pages = window.PassportDocPages({ variant, lang: "ru" });
  const total = pages.length;
  const sheetW = Math.min(boxW - 40, 560);
  const show = (s) => { setSnack(s); clearTimeout(snT.current); snT.current = setTimeout(() => setSnack(null), 3200); };
  const save = () => show(saveFail ? { t: "Не удалось сохранить", tone: "error", a: "Повторить" } : { t: "Файл сохранён · Загрузки", tone: "success", a: "Открыть" });
  const onScroll = () => {
    const el = scRef.current; if (!el) return;
    const h = RD_PAGE_H * (sheetW / RD_PAGE_W) + 20;
    setPage(Math.min(total, Math.max(1, Math.round((el.scrollTop + el.clientHeight * 0.35) / h + 0.5))));
  };
  const desk = platform === "desktop";

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)", fontFamily: "var(--font-sans)", position: "relative" }}>
      <RdTopBar title={st === "ready" ? "Паспорт готов" : "Паспорт монтажа"} subtitle="№ 20261119-234567-1"
        navigationIcon={<RdIconBtn icon="arrow_back" contentDescription="Изменить" onClick={onEdit} />}
        actions={st === "ready" ? <RdIconBtn icon="more_vert" contentDescription="Ещё" onClick={() => setMenu(!menu)} /> : null} />

      {menu && (
        <div className="rd-anim" style={{ position: "absolute", right: 8, top: 60, zIndex: 20, width: 300, background: "var(--surface-container-lowest)", border: "1px solid var(--border-highlight)", borderRadius: 12, boxShadow: "0 8px 24px rgba(10,26,58,.18)", padding: "6px", transformOrigin: "top right", animation: `rdUp 160ms ${RD_EASE}` }}>
          <button className="rd-tile" onClick={() => { setMenu(false); setXp(true); }} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 52, display: "flex", alignItems: "center", gap: 14, padding: "0 14px", borderRadius: 8, cursor: "pointer" }}>
            <RdIcon icon="folder_zip" size={22} color={rdMuted} /><RdText variant="paragraph2">Экспорт паспорта (ZIP)</RdText>
          </button>
          <div style={{ height: 1, background: "var(--border-highlight)", margin: "6px 8px" }} />
          <RdText variant="additional0" color={rdMuted} style={{ padding: "10px 14px 6px" }}>Настройки блока · не в документе</RdText>
          {[["save", "Сохранить в файл", "Файл настроек сохранён"], ["folder", "Сохранить в мои конфигурации", "Сохранено в мои конфигурации"]].map(([ic, t, msg]) => (
            <button key={t} className="rd-tile" onClick={() => { setMenu(false); show({ t: msg, tone: "success", a: "Открыть" }); }} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 52, display: "flex", alignItems: "center", gap: 14, padding: "0 14px", borderRadius: 8, cursor: "pointer" }}>
              <RdIcon icon={ic} size={22} color={rdMuted} /><RdText variant="paragraph2">{t}</RdText>
            </button>
          ))}
        </div>
      )}

      <div ref={scRef} onScroll={onScroll} style={{ flex: 1, minHeight: 0, overflowY: "auto", background: "var(--surface-container-high)", position: "relative" }}>
        {st === "ready" && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 20, padding: "20px 0 28px" }}>
            {pages.map((p, i) => <PassportSheet key={i} page={p} width={sheetW} />)}
          </div>
        )}
        {st === "forming" && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", padding: "20px 0" }}>
            <div className="rd-anim" style={{ width: sheetW, height: RD_PAGE_H * (sheetW / RD_PAGE_W), background: "#fff", boxShadow: "0 6px 20px rgba(10,26,58,.14)", padding: "8%", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 10, animation: "rdPulse 1.6s ease-in-out infinite" }}>
              {[46, 30, 0, 100, 100, 100, 82, 0, 100, 100, 64].map((w, i) => <span key={i} style={{ height: w ? 8 : 6, width: `${w}%`, background: "#E4E8EF", borderRadius: 2 }} />)}
            </div>
          </div>
        )}
        {st === "forming" && (
          <div style={{ position: "absolute", inset: 0, display: "grid", placeItems: "center", pointerEvents: "none" }}>
            <div style={{ display: "flex", alignItems: "center", gap: 14, padding: "14px 20px", borderRadius: 16, background: "var(--surface-container-lowest)", boxShadow: "0 6px 24px rgba(10,26,58,.16)" }}>
              <span className="rd-anim" style={{ width: 24, height: 24, borderRadius: 999, border: "3px solid var(--brand-primary-highlight)", borderTopColor: "var(--primary)", animation: "rdSpin .8s linear infinite" }} />
              <div>
                <RdText variant="headline3">Формируем документ…</RdText>
                <RdText variant="paragraph3" color={rdMuted}>С фото — до нескольких секунд</RdText>
              </div>
            </div>
          </div>
        )}
        {st === "error" && (
          <div style={{ height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 12, padding: 32, textAlign: "center", background: "var(--background)" }}>
            <span style={{ width: 64, height: 64, borderRadius: 18, display: "grid", placeItems: "center", background: "var(--error-highlight)" }}><RdIcon icon="error" size={36} color="var(--error-default)" /></span>
            <RdText variant="title2" align="center">Не удалось сформировать документ</RdText>
            <RdText variant="paragraph1" color={rdMuted} align="center" style={{ textWrap: "pretty" }}>Паспорт сохранён как черновик. Повторите — обычно помогает.</RdText>
          </div>
        )}
      </div>

      {st === "ready" && total > 1 && (
        <div aria-live="polite" style={{ position: "absolute", left: "50%", transform: "translateX(-50%)", bottom: desk ? 96 : 184, zIndex: 5, padding: "8px 14px", borderRadius: 999, background: "var(--inverse-surface)", color: "var(--inverse-on-surface)", pointerEvents: "none" }}>
          <RdText variant="additional0" color="var(--inverse-on-surface)" style={{ fontVariantNumeric: "tabular-nums" }}>Стр. {page} из {total}</RdText>
        </div>
      )}

      {xp && window.PtExport && <window.PtExport items={[{ no: "20261119-234567-1", car: "Kia Sportage", plate: "А 123 ВС 777", settings: true, photos: 4 }]} platform={platform} wide={false} onClose={() => setXp(false)} onDone={({ how, name }) => { setXp(false); show(how === "share" ? { t: "Архив готов — выберите, куда отправить", tone: "success" } : { t: `Сохранено: Загрузки/${name}`, tone: "success", a: "Открыть" }); }} />}
      {snack && (
        <div className="rd-anim" style={{ position: "absolute", left: 12, right: 12, bottom: desk ? 92 : 180, zIndex: 10, animation: `rdUp 260ms ${RD_EASE}` }}>
          <RdSnack message={snack.t} tone={snack.tone} actionLabel={snack.a} onActionClick={() => (snack.a === "Повторить" ? save() : setSnack(null))} style={{ width: "100%", minHeight: 56, borderRadius: 12 }} />
        </div>
      )}

      <div style={{ flex: "none", padding: "12px 20px 16px", borderTop: "1px solid var(--border-highlight)", display: "flex", flexDirection: "column", gap: 10, background: "var(--background)", ...(desk ? {} : { width: "100%", maxWidth: 600, margin: "0 auto", boxSizing: "border-box", borderTop: "none" }) }}>
        {st === "ready" && !desk && (
          <React.Fragment>
            <div style={{ display: "flex", gap: 8 }}>
              <RdTile icon="download" text="Сохранить файл" onClick={save} />
              <RdTile icon="open_in_new" text="Открыть" />
              <RdTile icon="edit" text="Изменить" onClick={onEdit} />
            </div>
            <RdButton text="Поделиться" icon="share" type="Primary" size="XL" fullWidth onClick={onDone} />
          </React.Fragment>
        )}
        {st === "ready" && desk && (
          <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
            <RdTextBtn text="Изменить" icon="edit" onClick={onEdit} />
            <RdOutlined text="Открыть" icon="open_in_new" />
            <RdButton text="Сохранить файл" icon="download" type="Primary" size="L" onClick={save} />
          </div>
        )}
        {st === "forming" && <RdOutlined text="Изменить" icon="edit" fullWidth onClick={onEdit} />}
        {st === "error" && (
          <React.Fragment>
            <RdButton text="Повторить" icon="refresh" type="Primary" size="XL" fullWidth onClick={() => { setSt("forming"); setTimeout(() => setSt("ready"), 1600); }} />
            <RdOutlined text="Изменить" icon="edit" fullWidth onClick={onEdit} />
          </React.Fragment>
        )}
      </div>
    </div>
  );
}

Object.assign(window, { PassportReady, PassportSheet });
