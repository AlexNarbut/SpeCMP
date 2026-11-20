// PassportList.jsx — «Паспорта монтажа»: список (раздел расширенного монтажа и архив), экран «Блок»
// (паспорт без блока), рабочее место на широком окне и навигация всего сценария.
// Раскладка — по M3 Window Size Classes ширины контейнера:
//   compact <600 — одна колонка, экраны сменяют друг друга;
//   medium 600–839 — то же, контент по центру с max-width;
//   expanded ≥840 — список + выбранный паспорт; редактор = поля слева + лист справа (живой предпросмотр).
//
// PassportApp props:
//   entry      "archive" | "mount"
//   listState  "list" | "loading" | "empty" | "one" | "search" | "noresults"
//   connected  архив: подключён блок 107131 (для «Не оформлен» → «Дооформить»)
//   platform   "mobile" | "tablet" | "desktop"
//   lookup     состояние экрана «Блок»: "live" | "errEmpty" | "errDigits" | "found" | "notfound" | "offline"
//   start      стартовый экран: "list" | "lookup"

const PL_DS = window.PandoraSpecDesignSystem_b8d6e1;
const PlText = (p) => <PL_DS.CmpText {...p} style={{ display: "block", ...p.style }} />;
const { CmpIcon: PlIcon, CmpButton: PlButton, CmpOutlinedButton: PlOutlined, CmpIconButton: PlIconBtn, CmpTopAppBar: PlTopBar, CmpTextField: PlField, CmpDialog: PlDialog, CmpSnackbar: PlSnack, CmpDropdown: PlDropdown } = PL_DS;
const PlTextBtn = (p) => (PL_DS.CmpTextButton ? <PL_DS.CmpTextButton {...p} /> : <PlOutlined {...p} />);
const PL_EASE = "cubic-bezier(.23,1,.32,1)";
const plMuted = "var(--foreground-on-dark-secondary-default)";

(function plCss() {
  if (document.getElementById("pl-css")) return;
  const s = document.createElement("style");
  s.id = "pl-css";
  s.textContent =
    ".pl-tap{transition:background-color 150ms ease,transform 160ms cubic-bezier(.23,1,.32,1)}" +
    "@media (hover:hover) and (pointer:fine){.pl-tap:hover{background-color:var(--background-surface-ultra-highlight)}}" +
    ".pl-tap:active{transform:scale(.99)}.pl-tap:focus-visible{outline:2px solid var(--border-focus);outline-offset:-2px}" +
    ".pl-sel{background-color:var(--brand-primary-highlight)!important}" +
    "@keyframes plShim{from{background-position:100% 0}to{background-position:0 0}}" +
    ".pl-skel{background:linear-gradient(90deg,var(--background-surface-highlight) 25%,var(--background-surface-ultra-highlight) 37%,var(--background-surface-highlight) 63%);background-size:400% 100%;animation:plShim 1.4s ease infinite}" +
    "@keyframes plIn{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}" +
    "@keyframes plSpin{to{transform:rotate(360deg)}}" +
    "mark.pl-hl{background:var(--brand-primary-highlight);color:inherit;border-radius:3px;box-shadow:0 0 0 2px var(--brand-primary-highlight);font-weight:700}" +
    "@media (prefers-reduced-motion:reduce){.pl-skel,.pl-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

// ── Данные ────────────────────────────────────────────────────────────────────
const PL_ITEMS = [
  { no: "20261119-234567-1", car: "Kia Sportage", plate: "А 123 ВС 777", model: "Pandora DX-91", devId: "1234567", serial: "DX91-0042-118734", vin: "XWEPH81ADN0012345", client: "Ирина Ковалёва", phone: "+7 903 555-12-40", date: "Сегодня, 14:05", status: "draft", settings: true, doc: "full" },
  { no: "20261119-107131-1", car: "Volkswagen Polo", plate: "", model: "Pandora DX-4GS", devId: "107131", serial: "DX4GS-0017-551203", date: "Сегодня, 11:40", status: "pending" },
  { no: "20261118-118004-2", car: "Toyota RAV4", plate: "О 456 КМ 199", model: "Pandora DX-91", devId: "1180042", serial: "DX91-0031-220917", vin: "JTMW43FV60D123456", client: "Сергей Литвинов", phone: "+7 916 210-33-08", date: "18 ноября, 17:20", status: "done", settings: true, doc: "full" },
  { no: "20261118-107131-1", car: "", plate: "", model: "Pandora DX-4GS", devId: "107131", serial: "DX4GS-0017-551203", date: "18 ноября, 10:05", status: "done", noblock: true, doc: "noblock" },
  { no: "20261117-221045-1", car: "Hyundai Tucson", plate: "А 777 АА 77", model: "Pandora DX-91", devId: "2210457", serial: "DX91-0044-009812", date: "17 ноября, 16:45", status: "done", settings: true, doc: "full" },
  { no: "20261115-310077-1", car: "Lada Vesta", plate: "Е 205 ТО 750", model: "Pandora VX-4G", devId: "3100772", serial: "VX4G-0102-447120", client: "Дмитрий Орлов", date: "15 ноября, 12:30", status: "done", doc: "min" },
  { no: "20261114-998213-1", car: "Skoda Octavia", plate: "К 919 МР 97", model: "Pandora DX-91", devId: "9982131", serial: "DX91-0045-118000", date: "14 ноября, 18:10", status: "done", settings: true, doc: "full" },
  { no: "20261113-450981-1", car: "Haval Jolion", plate: "Т 404 ХУ 790", model: "Pandora DX-4GS", devId: "4509812", serial: "DX4GS-0021-330412", date: "13 ноября, 10:15", status: "done", doc: "min" },
  { no: "20261112-781120-1", car: "Chery Tiggo 7 Pro", plate: "", model: "Pandora DX-91", devId: "7811204", serial: "DX91-0046-771020", date: "12 ноября, 15:50", status: "done", settings: true, doc: "full" },
  { no: "20261111-660451-1", car: "Kia Rio", plate: "В 310 НЕ 77", model: "Pandora VX-4G", devId: "6604515", serial: "VX4G-0099-120044", date: "11 ноября, 09:40", status: "done", doc: "min" },
  { no: "20261110-120333-1", car: "Geely Coolray", plate: "М 088 СК 197", model: "Pandora DX-4GS", devId: "1203330", serial: "DX4GS-0019-020201", date: "10 ноября, 13:05", status: "done", doc: "min" },
  { no: "20261108-505177-1", car: "Mazda CX-5", plate: "Р 515 ОР 50", model: "Pandora DX-91", devId: "5051770", serial: "DX91-0040-200718", date: "8 ноября, 17:35", status: "done", settings: true, doc: "full" },
];
const PL_STATUS = {
  pending: { t: "Не оформлен", icon: "schedule", bg: "var(--warning-highlight)", fg: "var(--on-surface)", ic: "var(--warning-default)" },
  draft: { t: "Черновик", icon: "edit_note", bg: "var(--brand-primary-highlight)", fg: "var(--on-surface)", ic: "var(--primary)" },
  done: { t: "Оформлен", icon: "task", bg: "var(--success-highlight)", fg: "var(--on-surface)", ic: "var(--success-default)" },
};
const PL_SEARCH = [["car", ""], ["plate", "Госномер"], ["vin", "VIN"], ["devId", "ID"], ["serial", "Серийный №"], ["client", "Клиент"], ["phone", "Телефон"]];

function plNorm(s) { return (s || "").toLowerCase().replace(/[\s\-()+]/g, "").replace(/ё/g, "е"); }
// Подсветка совпадения с учётом пробелов/дефисов в номерах.
function PlHl({ text, q }) {
  if (!q || !text) return text || null;
  const nq = plNorm(q);
  const map = []; let flat = "";
  for (let i = 0; i < text.length; i++) { const c = plNorm(text[i]); if (c) { flat += c; map.push(i); } }
  const at = flat.indexOf(nq);
  if (at < 0) return text;
  const a = map[at], b = map[at + nq.length - 1] + 1;
  return <React.Fragment>{text.slice(0, a)}<mark className="pl-hl">{text.slice(a, b)}</mark>{text.slice(b)}</React.Fragment>;
}
function plMatch(it, q) {
  const nq = plNorm(q);
  if (!nq) return { ok: true };
  for (const [k, label] of PL_SEARCH) if (plNorm(it[k]).includes(nq)) return { ok: true, field: k, label };
  return { ok: false };
}

function usePlWidth(ref, withH) {
  const [w, setW] = React.useState(withH ? [0, 0] : 0);
  React.useLayoutEffect(() => {
    const el = ref.current; if (!el) return;
    const m = () => setW(withH ? [el.clientWidth, el.clientHeight] : el.clientWidth);
    m();
    const ro = new ResizeObserver(m);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);
  return w;
}

function PlSpinner({ size = 22 }) {
  return <span className="pl-anim" style={{ width: size, height: size, borderRadius: 999, border: "3px solid var(--brand-primary-highlight)", borderTopColor: "var(--primary)", animation: "plSpin .8s linear infinite", flex: "none", boxSizing: "border-box", display: "inline-block" }} />;
}

function PlTag({ icon, text, bg, ic }) {
  return (
    <span style={{ display: "inline-flex", alignItems: "center", gap: 5, minHeight: 28, padding: "0 10px 0 8px", borderRadius: 8, background: bg || "var(--background-surface-highlight)", flex: "none" }}>
      <PlIcon icon={icon} size={16} color={ic || plMuted} />
      <PL_DS.CmpText variant="additional0" style={{ whiteSpace: "nowrap" }}>{text}</PL_DS.CmpText>
    </span>
  );
}

// ── Строка списка ─────────────────────────────────────────────────────────────
function PlRow({ it, q, match, connected, selected, onOpen, onDelete, onFinish, onExport, picking, picked, onPick }) {
  const [menu, setMenu] = React.useState(false);
  const st = PL_STATUS[it.status];
  const pending = it.status === "pending";
  const canFinish = pending && connected;
  const clickable = !pending || canFinish;
  const extra = match && match.field && !["car", "plate", "devId"].includes(match.field) ? match : null;
  return (
    <div className={"pl-tap" + (selected || picked ? " pl-sel" : "")} role={picking ? "checkbox" : "button"} aria-checked={picking ? !!picked : undefined} tabIndex={clickable || picking ? 0 : -1}
      onClick={() => picking ? (it.status !== "pending" && onPick(it)) : clickable && (canFinish ? onFinish(it) : onOpen(it))}
      style={{ position: "relative", display: "flex", gap: 8, padding: picking ? "14px 16px 14px 8px" : "14px 4px 14px 16px", opacity: picking && it.status === "pending" ? 0.5 : 1, border: picked ? "2px solid var(--primary)" : undefined, borderRadius: 16, border: "1px solid var(--border-highlight)", background: "var(--background-surface-card)", cursor: clickable ? "pointer" : "default", boxSizing: "border-box" }}>
      {picking && <span style={{ flex: "none", width: 48, height: 48, display: "grid", placeItems: "center", marginTop: -8 }}>{PL_DS.CmpCheckbox ? <PL_DS.CmpCheckbox checked={!!picked} disabled={it.status === "pending"} onCheckedChange={() => onPick(it)} /> : null}</span>}
      <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 6 }}>
        <PlText variant="headline2" maxLines={1} color={it.car ? undefined : plMuted}>{it.car ? <PlHl text={it.car} q={q} /> : "Автомобиль не указан"}</PlText>
        <PlText variant="paragraph2" color={plMuted} maxLines={2}>
          {it.plate && <React.Fragment><span style={{ color: "var(--on-surface)", fontWeight: 700 }}><PlHl text={it.plate} q={q} /></span> · </React.Fragment>}
          {it.model} · ID <PlHl text={it.devId} q={q} />
        </PlText>
        {extra && <PlText variant="paragraph3" color={plMuted}>{extra.label}: <PlHl text={it[extra.field]} q={q} /></PlText>}
        <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: 6, paddingTop: 2 }}>
          <PlTag icon={st.icon} text={st.t} bg={st.bg} ic={st.ic} />
          {it.imported && <PlTag icon="download_done" text="Импортирован" bg="var(--brand-primary-highlight)" ic="var(--primary)" />}
          {it.noblock && <PlTag icon="edit_note" text="Без блока" />}
          {it.settings && <span title="Сохранён снимок настроек блока" style={{ display: "inline-flex", alignItems: "center", gap: 4, minHeight: 28, padding: "0 6px" }}><PlIcon icon="tune" size={18} color={plMuted} /><PL_DS.CmpText variant="additional0" color={plMuted}>Настройки</PL_DS.CmpText></span>}
          <PL_DS.CmpText variant="additional0" color={plMuted} style={{ marginLeft: "auto", paddingRight: 8, whiteSpace: "nowrap" }}>{it.date}</PL_DS.CmpText>
        </div>
        {pending && !picking && (canFinish
          ? <div style={{ paddingTop: 6 }} onClick={(e) => e.stopPropagation()}><PlButton text="Дооформить" icon="description" type="Primary" size="M" onClick={() => onFinish(it)} /></div>
          : <div style={{ display: "flex", gap: 8, alignItems: "center", paddingTop: 4 }}><PlIcon icon="bluetooth" size={18} color={plMuted} /><PlText variant="paragraph3" color={plMuted}>Подключитесь к блоку, чтобы оформить</PlText></div>)}
      </div>
      {!picking && <div style={{ flex: "none", alignSelf: "flex-start", marginTop: -6 }} onClick={(e) => e.stopPropagation()}>
        <PlIconBtn icon="more_vert" contentDescription="Действия с паспортом" onClick={() => setMenu(!menu)} />
      </div>}
      {menu && (
        <div className="pl-anim" onClick={(e) => e.stopPropagation()} style={{ position: "absolute", right: 8, top: 52, zIndex: 5, minWidth: 220, background: "var(--surface-container-lowest)", border: "1px solid var(--border-highlight)", borderRadius: 12, boxShadow: "0 8px 24px rgba(10,26,58,.18)", padding: 6, transformOrigin: "top right", animation: `plIn 160ms ${PL_EASE}` }}>
          {clickable && <PlMenuItem icon="open_in_new" text="Открыть" onClick={() => { setMenu(false); canFinish ? onFinish(it) : onOpen(it); }} />}
          {!pending && <PlMenuItem icon="folder_zip" text="Экспорт (ZIP)" onClick={() => { setMenu(false); onExport(it); }} />}
          <PlMenuItem icon="delete" text="Удалить" danger onClick={() => { setMenu(false); onDelete(it); }} />
        </div>
      )}
    </div>
  );
}
function PlMenuItem({ icon, text, danger, onClick }) {
  return (
    <button className="pl-tap" onClick={onClick} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 52, display: "flex", alignItems: "center", gap: 14, padding: "0 14px", borderRadius: 8, cursor: "pointer" }}>
      <PlIcon icon={icon} size={22} color={danger ? "var(--error-default)" : plMuted} />
      <PL_DS.CmpText variant="paragraph2" color={danger ? "var(--error-default)" : undefined}>{text}</PL_DS.CmpText>
    </button>
  );
}

function PlEmpty({ icon, title, text, children }) {
  return (
    <div className="pl-anim" style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: 12, padding: "48px 24px", animation: `plIn 220ms ${PL_EASE}` }}>
      <span style={{ width: 72, height: 72, borderRadius: 20, display: "grid", placeItems: "center", background: "var(--background-surface-highlight)", color: "var(--on-surface)" }}>
        {icon === "passport" && window.PS_SECTION_ICONS ? <span style={{ width: 40, height: 40, display: "inline-flex" }} dangerouslySetInnerHTML={{ __html: window.PS_SECTION_ICONS.passport }} /> : <PlIcon icon={icon} size={36} color="var(--on-surface)" />}
      </span>
      <PlText variant="headline1" align="center" style={{ textWrap: "balance" }}>{title}</PlText>
      {text && <PlText variant="paragraph1" color={plMuted} align="center" style={{ textWrap: "pretty", maxWidth: 420 }}>{text}</PlText>}
      {children}
    </div>
  );
}

// ── Колонка списка ────────────────────────────────────────────────────────────
function PlListPane({ entry, listState, connected, dev, q, setQ, items, selected, onOpen, onFinish, onDelete, onNew, onNoBlock, onExport, onExportAll, backup, onBackupLater, picking, picked, onPick, fresh = [], pad = 16 }) {
  const archive = entry === "archive";
  const filtered = items.map((it) => [it, plMatch(it, q)]).filter(([, m]) => m.ok);
  const top = (
    <div style={{ display: "flex", flexDirection: "column", gap: 12, padding: `8px ${pad}px 12px` }}>
      {archive ? (
        <React.Fragment>
          <PlField label="Поиск" placeholder="Госномер, авто, VIN, ID, клиент" value={q} onChange={setQ} leadingIcon={<PlIcon icon="search" size={24} />}
            trailingIcon={q ? <span onClick={() => setQ("")} style={{ cursor: "pointer", display: "inline-flex", width: 40, height: 40, alignItems: "center", justifyContent: "center", margin: -8 }}><PlIcon icon="close" size={22} /></span> : null} />
          {!q && backup && items.length > 0 && (
            <div className="pl-anim" style={{ display: "flex", flexDirection: "column", gap: 10, padding: "14px 14px 10px 16px", borderRadius: 16, background: "var(--brand-primary-highlight)", border: "1px solid var(--border-highlight)", animation: `plIn 220ms ${PL_EASE}` }}>
              <div style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
                <PlIcon icon="backup" size={26} color="var(--primary)" />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <PlText variant="headline3">Сохраните копию архива</PlText>
                  <PlText variant="paragraph3" color={plMuted} style={{ marginTop: 2, textWrap: "pretty" }}>Паспорта хранятся только на этом устройстве. Последняя копия — 12 октября.</PlText>
                </div>
              </div>
              <div style={{ display: "flex", gap: 8, flexWrap: "wrap", justifyContent: "flex-end" }}>
                <PlTextBtn text="Позже" onClick={onBackupLater} />
                <PlButton text="Экспорт всех" icon="folder_zip" type="Primary" size="M" onClick={onExportAll} />
              </div>
            </div>
          )}
          {!q && (
            <div style={{ display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" }}>
              <div style={{ display: "flex", gap: 8, alignItems: "center", flex: "1 1 200px", minWidth: 0 }}>
                <PlIcon icon="info" size={20} color={plMuted} />
                <PlText variant="paragraph3" color={plMuted}>Новый паспорт оформляется при подключённом блоке</PlText>
              </div>
              <PlTextBtn text="Новый паспорт без блока" icon="add" onClick={onNoBlock} />
            </div>
          )}
        </React.Fragment>
      ) : (
        <PlButton text="Новый паспорт" icon="add" type="Primary" size="L" fullWidth onClick={onNew} />
      )}
    </div>
  );

  let body;
  if (listState === "loading") {
    body = [0, 1, 2, 3].map((i) => <div key={i} className="pl-skel" style={{ height: 132, borderRadius: 16 }} />);
  } else if (!items.length) {
    body = archive
      ? <PlEmpty icon="passport" title="Паспортов ещё нет" text="Оформите первый в конце монтажа — он появится здесь." />
      : <PlEmpty icon="passport" title="Для этого блока паспортов ещё нет" text="Паспорт — документ для клиента: оборудование, проверка, фото." />;
  } else if (!filtered.length) {
    body = <PlEmpty icon="search_off" title="Ничего не нашли" text={`По запросу «${q}» паспортов нет. Проверьте госномер или ищите по ID блока.`}><PlOutlined text="Сбросить поиск" icon="close" onClick={() => setQ("")} /></PlEmpty>;
  } else {
    body = filtered.map(([it, m], i) => (
      <div key={it.no} className="pl-anim" style={{ animation: `plIn 220ms ${PL_EASE} both`, animationDelay: `${Math.min(i, 6) * 35}ms` }}>
        <PlRow it={it} q={q} match={m} picking={picking} picked={picked && picked.has(it.no)} onPick={onPick} onExport={onExport} connected={archive ? connected && it.devId === "107131" : true} selected={selected === it.no} onOpen={onOpen} onFinish={onFinish} onDelete={onDelete} />
      </div>
    ));
  }
  return (
    <div style={{ display: "flex", flexDirection: "column", minHeight: 0, flex: 1 }}>
      {picking ? null : (items.length > 0 || archive) && listState !== "loading" ? top : listState === "loading" ? top : null}
      <div style={{ flex: 1, minHeight: 0, overflowY: "auto", padding: `0 ${pad}px 24px`, display: "flex", flexDirection: "column", gap: 10 }}>
        {archive && q && filtered.length > 0 && <PlText variant="additional0" color={plMuted} style={{ padding: "0 4px 2px" }}>Найдено: {filtered.length}</PlText>}
        {archive && !q && !picking && items.length > 0 && listState !== "loading" && (
          <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "0 4px 2px" }}>
            <PlIcon icon="sd_storage" size={18} color={plMuted} />
            <PlText variant="additional0" color={plMuted}>{items.length === PL_ITEMS.length ? 128 : items.length} {items.length === 1 ? "паспорт" : "паспортов"} · {items.length === PL_ITEMS.length ? "340 МБ" : `${(items.length * 2.7).toFixed(1).replace(".", ",")} МБ`} на этом устройстве</PlText>
          </div>
        )}
        {body}
        {!archive && !items.length && listState !== "loading" && <div style={{ display: "flex", justifyContent: "center" }}><PlButton text="Новый паспорт" icon="add" type="Primary" size="L" onClick={onNew} /></div>}
      </div>
    </div>
  );
}

// ── Предпросмотр документа справа (expanded) ─────────────────────────────────
function PlPreview({ pages, actions, title, note, dense }) {
  const ref = React.useRef(null);
  const w = usePlWidth(ref);
  const sheetW = Math.max(240, Math.min(w - 48, 620));
  return (
    <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", background: "var(--surface-container-high)", minHeight: 0 }}>
      <div style={{ flex: "none", display: "flex", alignItems: "center", gap: 10, padding: dense ? "4px 8px 4px 16px" : "10px 16px", borderBottom: "1px solid var(--border-highlight)", background: "var(--background)", flexWrap: dense ? "nowrap" : "wrap" }}>
        <div style={{ flex: "1 1 160px", minWidth: 0 }}>
          <PlText variant="headline3" maxLines={1}>{title}</PlText>
          {!dense && <PlText variant="additional0" color={plMuted}>{note || `${pages.length} ${pages.length === 1 ? "страница" : pages.length < 5 ? "страницы" : "страниц"} · A4`}</PlText>}
        </div>
        <div style={{ display: "flex", gap: 8, alignItems: "center", flexWrap: dense ? "nowrap" : "wrap", flex: "none" }}>{actions}</div>
      </div>
      <div ref={ref} style={{ flex: 1, minHeight: 0, overflowY: "auto", display: "flex", flexDirection: "column", alignItems: "center", gap: 20, padding: "24px 0 32px" }}>
        {w > 0 && pages.map((p, i) => <window.PassportSheet key={i} page={p} width={sheetW} />)}
      </div>
    </div>
  );
}

// dense — узкое/низкое окно: главная кнопка + «⋯» (остальные действия — в меню).
function PlDocActions({ platform, onEdit, onSave, onMenu, editLabel = "Изменить", dense }) {
  const desk = platform === "desktop";
  if (dense) return (
    <React.Fragment>
      {desk ? <PlButton text="Сохранить файл" icon="download" type="Primary" size="S" onClick={onSave} /> : <PlButton text="Поделиться" icon="share" type="Primary" size="S" />}
      <PlIconBtn icon="more_vert" contentDescription="Ещё действия" onClick={onMenu} />
    </React.Fragment>
  );
  return (
    <React.Fragment>
      {onEdit && <PlTextBtn text={editLabel} icon="edit" onClick={onEdit} />}
      <PlOutlined text="Открыть" icon="open_in_new" />
      {desk ? <PlButton text="Сохранить файл" icon="download" type="Primary" size="M" onClick={onSave} />
        : <React.Fragment><PlOutlined text="Сохранить файл" icon="download" onClick={onSave} /><PlButton text="Поделиться" icon="share" type="Primary" size="M" /></React.Fragment>}
      <PlIconBtn icon="more_vert" contentDescription="Настройки блока" onClick={onMenu} />
    </React.Fragment>
  );
}

// ── Экран «Блок» (паспорт без блока) ──────────────────────────────────────────
function PlLookup({ state = "live", platform, onBack, onNext }) {
  const pre = { errEmpty: "", errDigits: "12A4567", found: "1234567", notfound: "7654321", offline: "1234567" }[state];
  const [id, setId] = React.useState(pre !== undefined ? pre : "");
  const [phase, setPhase] = React.useState(state === "found" ? "found" : state === "notfound" ? "notfound" : state === "offline" ? "offline" : "input");
  const [err, setErr] = React.useState(state === "errEmpty" ? "Введите номер" : state === "errDigits" ? "Только цифры" : null);
  const [model, setModel] = React.useState("");
  const [serial, setSerial] = React.useState("");
  const find = () => {
    if (!id.trim()) return setErr("Введите номер");
    if (!/^\d+$/.test(id.trim())) return setErr("Только цифры");
    setErr(null); setPhase("search");
    setTimeout(() => setPhase(id.trim() === "1234567" ? "found" : "notfound"), 1100);
  };
  const manual = phase === "notfound" || phase === "offline";
  const models = ["Pandora DX-91", "Pandora DX-4GS", "Pandora VX-4G", "Pandora DXL-5000"];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)" }}>
      <PlTopBar title="Новый паспорт без блока" subtitle="Шаг 1 из 2 · блок" navigationIcon={<PlIconBtn icon="arrow_back" contentDescription="Назад" onClick={onBack} />} />
      <div style={{ flex: 1, minHeight: 0, overflowY: "auto" }}>
        <div style={{ maxWidth: 560, margin: "0 auto", padding: "16px 20px 24px", display: "flex", flexDirection: "column", gap: 18 }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <PlText variant="title2">Какой блок ставили?</PlText>
            <PlText variant="paragraph1" color={plMuted} style={{ textWrap: "pretty" }}>Введите ID устройства — найдём модель и серийный номер на сервере.</PlText>
          </div>
          <div style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
            <div style={{ flex: 1, minWidth: 0 }}>
              <PlField label="ID устройства" value={id} onChange={(x) => { setId(x); setErr(null); if (phase !== "input" && phase !== "search") setPhase("input"); }} isError={!!err} supportingText={err} inputMode="numeric"
                trailingIcon={platform !== "desktop" ? <span title="Сканировать штрих-код" style={{ display: "inline-flex", width: 48, height: 48, alignItems: "center", justifyContent: "center", margin: "0 -12px", cursor: "pointer" }}><PlIcon icon="qr_code_scanner" size={26} color="var(--on-surface)" /></span> : null} />
            </div>
            <PlButton text="Найти" type="Primary" size="XL" loading={phase === "search"} onClick={find} style={{ minHeight: 72 }} />
          </div>

          {phase === "found" && (
            <div className="pl-anim" style={{ display: "flex", gap: 14, alignItems: "center", padding: 16, borderRadius: 16, border: "1px solid var(--border-highlight)", background: "var(--background-surface-card)", animation: `plIn 220ms ${PL_EASE}` }}>
              <span style={{ width: 48, height: 48, borderRadius: 14, display: "grid", placeItems: "center", background: "var(--success-highlight)", flex: "none" }}><PlIcon icon="check_circle" size={28} color="var(--success-default)" /></span>
              <div style={{ flex: 1, minWidth: 0 }}>
                <PlText variant="headline2">Pandora DX-91</PlText>
                <PlText variant="paragraph2" color={plMuted}>ID 1234567 · DX91-0042-118734</PlText>
              </div>
            </div>
          )}
          {manual && (
            <div className="pl-anim" style={{ display: "flex", flexDirection: "column", gap: 14, animation: `plIn 220ms ${PL_EASE}` }}>
              <div style={{ display: "flex", gap: 12, alignItems: "flex-start", padding: "12px 14px", borderRadius: 12, background: "var(--warning-highlight)" }}>
                <PlIcon icon={phase === "offline" ? "cloud_off" : "help"} size={22} color="var(--warning-default)" />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <PlText variant="headline3">{phase === "offline" ? "Нет интернета" : "Не нашли блок по этому ID"}</PlText>
                  <PlText variant="paragraph3" color={plMuted} style={{ marginTop: 2 }}>Укажите модель вручную — в паспорте будет пометка, что блок не считывался.</PlText>
                </div>
              </div>
              {PlDropdown && <PlDropdown selectedValue={models.includes(model) ? model : model ? "Другая — вписать" : null} placeholder="Модель блока" options={[...models, "Другая — вписать"]} onOptionSelected={(i, v) => setModel(v === "Другая — вписать" ? " " : v)} />}
              {(!PlDropdown || (model && !models.includes(model))) && <PlField label="Модель" value={model.trim()} onChange={(x) => setModel(x || " ")} placeholder="Например, Pandora DX-90" />}
              <PlField label="Серийный номер" value={serial} onChange={setSerial} supportingText="Необязательно" />
            </div>
          )}
        </div>
      </div>
      <div style={{ flex: "none", borderTop: "1px solid var(--border-highlight)" }}>
        <div style={{ maxWidth: 560, margin: "0 auto", padding: "12px 20px 16px" }}>
          <PlButton text="Далее" icon="arrow_forward" type="Primary" size="XL" fullWidth disabled={!(phase === "found" || (manual && model))} onClick={onNext} />
        </div>
      </div>
    </div>
  );
}

// ── Приложение ────────────────────────────────────────────────────────────────
function PassportApp({ entry = "archive", listState = "list", connected = false, platform = "mobile", lookup = "live", start = "list", dev, readState = "live", readCtx, scenario = "fast", ready = "live", photosInit = "none", cameraDenied = false, transfer = null, importCase = "ok", backupDue = true, noSpace = false }) {
  const ref = React.useRef(null);
  const [w, hgt] = usePlWidth(ref, true);
  const cls = w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact";
  const short = hgt > 0 && hgt < 520;
  const wide = cls === "expanded";
  const device = dev || { name: "Pandora DX-4GS", id: "107131", transport: "bt" };
  const base = entry === "mount" ? PL_ITEMS.filter((x) => x.devId === device.id) : PL_ITEMS;
  const [items, setItems] = React.useState(() => listState === "empty" || listState === "loading" ? (listState === "loading" ? base : []) : listState === "one" ? base.slice(0, 1) : base);
  const [loading, setLoading] = React.useState(listState === "loading");
  const [q, setQ] = React.useState(entry !== "archive" ? "" : listState === "search" ? "Ковал" : listState === "noresults" ? "Х 999" : "");
  const [route, setRoute] = React.useState(() => start === "read" ? { s: "read", readState } : start === "editor" ? { s: "editor", scenario } : start === "ready" ? { s: "ready", state: ready, forming: ready === "live" } : { s: start });
  const [sel, setSel] = React.useState(null);
  const [del, setDel] = React.useState(null);
  const [snack, setSnack] = React.useState(null);
  const [menu, setMenu] = React.useState(false);
  const [live, setLive] = React.useState(null);
  const [picking, setPicking] = React.useState(transfer === "pick");
  const [picked, setPicked] = React.useState(() => new Set(transfer === "pick" || transfer === "exportMany" ? PL_ITEMS.filter((x) => x.status === "done").slice(0, 3).map((x) => x.no) : []));
  const [xfer, setXfer] = React.useState(() => transfer === "export" ? { k: "export", items: [PL_ITEMS[2]] } : transfer === "exportMany" ? { k: "export", items: PL_ITEMS.filter((x) => x.status === "done").slice(0, 3) } : transfer === "import" ? { k: "import" } : null);
  const [topMenu, setTopMenu] = React.useState(false);
  const [backup, setBackup] = React.useState(backupDue);
  const togglePick = (it) => setPicked((s0) => { const n = new Set(s0); n.has(it.no) ? n.delete(it.no) : n.add(it.no); return n; });
  const exportable = items.filter((x) => x.status !== "pending");
  React.useEffect(() => { if (loading) { const t = setTimeout(() => setLoading(false), 1600); return () => clearTimeout(t); } }, []);
  const show = (s) => { setSnack(s); setTimeout(() => setSnack(null), 3000); };
  const selected = sel || (wide && items[0] ? items[0].no : null);
  const selItem = items.find((x) => x.no === selected);
  const archive = entry === "archive";

  const go = (s, extra) => setRoute({ s, ...extra });
  const open = (it) => { if (wide) { setSel(it.no); return; } it.status === "draft" ? go("editor", { scenario: "fast", it }) : go("ready", { it }); };
  const finish = (it) => go("read", { it });
  const doDelete = () => { setItems((xs) => xs.filter((x) => x.no !== del.no)); if (sel === del.no) setSel(null); setDel(null); show({ t: "Паспорт удалён", tone: "info" }); };
  const settingsMenu = menu && (
    <div className="pl-anim" style={{ position: "absolute", right: 16, top: short ? 104 : 120, zIndex: 30, width: 320, maxHeight: "calc(100% - 120px)", overflowY: "auto", background: "var(--surface-container-lowest)", border: "1px solid var(--border-highlight)", borderRadius: 12, boxShadow: "0 8px 24px rgba(10,26,58,.18)", padding: 6, transformOrigin: "top right", animation: `plIn 160ms ${PL_EASE}` }}>
      {short && <React.Fragment>
        {route.s === "list" && selItem && <PlMenuItem icon="edit" text={selItem.status === "draft" ? "Продолжить" : "Изменить"} onClick={() => { setMenu(false); go("editor", { scenario: "fast", it: selItem }); }} />}
        <PlMenuItem icon="open_in_new" text="Открыть" onClick={() => setMenu(false)} />
        {platform !== "desktop" && <PlMenuItem icon="download" text="Сохранить файл" onClick={() => { setMenu(false); save(); }} />}
        <div style={{ height: 1, background: "var(--border-highlight)", margin: "6px 8px" }} />
      </React.Fragment>}
      {selItem && <PlMenuItem icon="folder_zip" text="Экспорт паспорта (ZIP)" onClick={() => { setMenu(false); setXfer({ k: "export", items: [selItem] }); }} />}
      <PlText variant="additional0" color={plMuted} style={{ padding: "10px 14px 6px" }}>Настройки блока · в документ не попадают</PlText>
      <PlMenuItem icon="save" text="Сохранить в файл" onClick={() => { setMenu(false); show({ t: "Файл настроек сохранён", tone: "success" }); }} />
      <PlMenuItem icon="folder" text="Сохранить в мои конфигурации" onClick={() => { setMenu(false); show({ t: "Сохранено в мои конфигурации", tone: "success", a: "Открыть" }); }} />
    </div>
  );
  const save = () => show({ t: "Файл сохранён · Загрузки", tone: "success", a: "Открыть" });

  const title = archive ? "Паспорта монтажа" : "Паспорт монтажа";
  const subtitle = archive ? (connected ? "Подключён Pandora DX-4GS · ID 107131" : "Без подключения к блоку") : `${device.name} · ID ${device.id}`;
  const backTo = archive ? (platform === "desktop" ? "home-desktop" : "home") : (platform === "mobile" ? "advanced-mount" : "advanced-mount-tablet");
  const listPane = (pad) => (
    <PlListPane entry={entry} listState={loading ? "loading" : listState} connected={connected} dev={device} q={q} setQ={setQ} items={items} selected={wide ? selected : null}
      onOpen={open} onFinish={finish} onDelete={setDel} onNew={() => go("read", {})} onNoBlock={() => go("lookup")} pad={pad}
      onExport={(it) => setXfer({ k: "export", items: [it] })} onExportAll={() => setXfer({ k: "export", items: exportable, all: true })} backup={backup} onBackupLater={() => { setBackup(false); show({ t: "Напомним через месяц", tone: "info" }); }} picking={picking} picked={picked} onPick={togglePick} />
  );

  let screen;
  const r = route;
  if (r.s === "read") {
    screen = <window.PassportRead key={r.readState} state={r.readState || "live"} context={readCtx || (entry === "fast" ? "fast" : "ext")} conn={device.transport === "usb" ? "usb" : "bt"} onDone={() => go("editor", { scenario: "ext", it: r.it })} onCancel={() => go("list")} />;
  } else if (r.s === "lookup") {
    screen = <PlLookup state={lookup} platform={platform} onBack={() => go("list")} onNext={() => go("editor", { scenario: "noblock" })} />;
  } else if (r.s === "editor" || (wide && r.s === "ready")) {
    // linked: в архиве блок может быть не подключён — тогда правятся только ручные поля.
    const sc = r.scenario || "fast";
    const linked = !archive || connected;
    const ep = { scenario: sc, linked, platform, photosInit: r.photos || photosInit, cameraDenied, noSpace, onReadBlock: sc === "noblock" && connected ? () => go("read", { it: r.it }) : undefined };
    screen = wide
      ? <PassportWorkspace key={sc + (r.photos || "")} editorProps={ep} platform={platform} short={short} onBack={() => go("list")} />
      : <window.PassportEditor key={sc} {...ep} initialOpen={sc === "partial" ? ["eq"] : r.photos || photosInit !== "none" ? ["photos"] : ["vehicle"]} onBack={() => go("list")} onPdf={() => go("ready", { forming: true, it: r.it })} />;
  } else if (r.s === "ready") {
    screen = <PlReadyLive forming={r.forming} state={r.state} variant={(r.it && r.it.doc) || (r.state === "ready1" ? "min" : "full")} saveFail={r.state === "saveFail"} platform={platform} onEdit={() => go("editor", { scenario: "fast", it: r.it })} onDone={() => go("list")} />;
  } else {
    // Список
    const bar = picking
      ? <PlTopBar title={`Выбрано: ${picked.size}`} subtitle="Отметьте паспорта для экспорта" navigationIcon={<PlIconBtn icon="close" contentDescription="Отменить выбор" onClick={() => { setPicking(false); setPicked(new Set()); }} />}
          actions={<PlTextBtn text={picked.size === exportable.length ? "Снять все" : "Выбрать все"} onClick={() => setPicked(picked.size === exportable.length ? new Set() : new Set(exportable.map((x) => x.no)))} />} />
      : <PlTopBar title={title} subtitle={subtitle} navigationIcon={<PlIconBtn icon="arrow_back" contentDescription="Назад" onClick={() => window.psGo && window.psGo(backTo)} />}
          actions={cls !== "compact"
            ? <div style={{ display: "flex", gap: 8, paddingRight: 12 }}><PlOutlined text="Импорт" icon="file_open" onClick={() => setXfer({ k: "import" })} />{exportable.length > 0 && <PlOutlined text="Экспорт" icon="folder_zip" onClick={() => setPicking(true)} />}</div>
            : <PlIconBtn icon="more_vert" contentDescription="Импорт и экспорт" onClick={() => setTopMenu(!topMenu)} />} />;
    const topMenuEl = topMenu && (
      <div className="pl-anim" style={{ position: "absolute", right: 8, top: 60, zIndex: 30, width: 290, background: "var(--surface-container-lowest)", border: "1px solid var(--border-highlight)", borderRadius: 12, boxShadow: "0 8px 24px rgba(10,26,58,.18)", padding: 6, transformOrigin: "top right", animation: `plIn 160ms ${PL_EASE}` }}>
        <PlMenuItem icon="file_open" text="Импорт из файла" onClick={() => { setTopMenu(false); setXfer({ k: "import" }); }} />
        {exportable.length > 0 && <PlMenuItem icon="checklist" text="Выбрать для экспорта" onClick={() => { setTopMenu(false); setPicking(true); }} />}
        {exportable.length > 0 && <PlMenuItem icon="folder_zip" text={`Экспорт всех (${exportable.length})`} onClick={() => { setTopMenu(false); setXfer({ k: "export", items: exportable }); }} />}
      </div>
    );
    const pickBar = picking && (
      <div style={{ flex: "none", borderTop: "1px solid var(--border-highlight)", background: "var(--background)" }}>
        <div style={{ maxWidth: 560, margin: "0 auto", padding: "12px 16px 16px" }}>
          <PlButton text={picked.size ? `Экспорт: ${picked.size}` : "Отметьте паспорта"} icon="folder_zip" type="Primary" size="XL" fullWidth disabled={!picked.size} onClick={() => setXfer({ k: "export", items: items.filter((x) => picked.has(x.no)) })} />
        </div>
      </div>
    );
    if (wide) {
      let right;
      if (!selItem) right = <div style={{ flex: 1, display: "grid", placeItems: "center", background: "var(--surface-container-high)" }}><PlEmpty icon="description" title="Выберите паспорт" text="Документ откроется здесь — можно отправить его снова." /></div>;
      else if (selItem.status === "pending") {
        const can = !archive || (connected && selItem.devId === "107131");
        right = (
          <div style={{ flex: 1, display: "grid", placeItems: "center", background: "var(--surface-container-high)", padding: 24 }}>
            <div style={{ maxWidth: 460, padding: 28, borderRadius: 20, background: "var(--background)", border: "1px solid var(--border-highlight)", display: "flex", flexDirection: "column", gap: 12 }}>
              <PlTag icon="schedule" text="Не оформлен" bg="var(--warning-highlight)" ic="var(--warning-default)" />
              <PlText variant="headline1">{selItem.car || "Автомобиль не указан"} · ID {selItem.devId}</PlText>
              <PlText variant="paragraph1" color={plMuted} style={{ textWrap: "pretty" }}>Быстрый монтаж завершён, паспорт не оформляли. Данные мастера сохранены — осталось считать блок и проверить.</PlText>
              {can ? <PlButton text="Дооформить" icon="description" type="Primary" size="L" onClick={() => finish(selItem)} />
                : <div style={{ display: "flex", gap: 8, alignItems: "center" }}><PlIcon icon="bluetooth" size={20} color={plMuted} /><PlText variant="paragraph2" color={plMuted}>Подключитесь к блоку {selItem.model} · ID {selItem.devId}, чтобы оформить</PlText></div>}
            </div>
          </div>
        );
      } else {
        right = <PlPreview pages={window.PassportDocPages({ variant: selItem.doc || "full" })} title={`${selItem.car || "Автомобиль не указан"}${selItem.plate ? " · " + selItem.plate : ""}`}
          note={`№ ${selItem.no} · ${PL_STATUS[selItem.status].t}`}
          dense={short} actions={<PlDocActions dense={short} platform={platform} editLabel={selItem.status === "draft" ? "Продолжить" : "Изменить"} onEdit={() => go("editor", { scenario: "fast", it: selItem })} onSave={save} onMenu={() => setMenu(!menu)} />} />;
      }
      screen = (
        <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)", position: "relative" }}>
          {bar}
          <div style={{ flex: 1, minHeight: 0, display: "flex", borderTop: "1px solid var(--border-highlight)" }}>
            <div style={{ flex: "0 0 400px", display: "flex", flexDirection: "column", minHeight: 0, borderRight: "1px solid var(--border-highlight)", paddingTop: 8 }}>{listPane(16)}{pickBar}</div>
            {right}
          </div>
          {settingsMenu}{topMenuEl}
        </div>
      );
    } else {
      screen = (
        <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)", position: "relative" }}>
          {bar}
          <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", width: "100%", maxWidth: cls === "medium" ? 720 : "none", margin: "0 auto" }}>{listPane(cls === "medium" ? 24 : 16)}</div>
          {pickBar}{topMenuEl}
        </div>
      );
    }
  }

  return (
    <div ref={ref} data-screen-label={`Паспорта · ${r.s} · ${cls}`} style={{ height: "100%", position: "relative", fontFamily: "var(--font-sans)", overflow: "hidden" }}>
      {w > 0 && screen}
      {xfer && xfer.k === "export" && window.PtExport && (
        <window.PtExport items={xfer.items} platform={platform} wide={cls !== "compact"} onClose={() => setXfer(null)}
          onDone={({ how, name }) => { if (xfer.all) setBackup(false); setXfer(null); setPicking(false); setPicked(new Set()); show(how === "share" ? { t: "Архив готов — выберите, куда отправить", tone: "success" } : { t: `Сохранено: Загрузки/${name}`, tone: "success", a: "Открыть" }); }} />
      )}
      {xfer && xfer.k === "import" && window.PtImport && (
        <window.PtImport platform={platform} wide={cls !== "compact"} scenario={importCase} existing={items.map((x) => x.no)} onClose={() => setXfer(null)}
          onDone={({ added, replaced }) => { setXfer(null); setItems((xs) => [...added.map((x) => ({ ...x, imported: true })), ...xs.map((x) => (replaced.some((r0) => r0.no === x.no) ? { ...x, imported: true } : x))]); setQ(""); show({ t: `Импортировано: ${added.length + replaced.length}${replaced.length ? ` (заменено ${replaced.length})` : ""}`, tone: "success" }); }} />
      )}
      {del && PlDialog && (
        <PlDialog open icon="delete" title="Удалить паспорт?" text={`Паспорт и его фото будут удалены.${del.car ? " " + del.car + (del.plate ? ", " + del.plate : "") + "." : ""}`}
          onDismissRequest={() => setDel(null)}
          confirmButton={<PlTextBtn text="Удалить" onClick={doDelete} />} dismissButton={<PlTextBtn text="Отмена" onClick={() => setDel(null)} />} />
      )}
      {snack && (
        <div className="pl-anim" style={{ position: "absolute", left: 16, right: 16, bottom: 16, zIndex: 40, display: "flex", justifyContent: "center", animation: `plIn 240ms ${PL_EASE}` }}>
          <div style={{ width: "100%", maxWidth: 520 }}><PlSnack message={snack.t} tone={snack.tone} actionLabel={snack.a} onActionClick={() => setSnack(null)} style={{ width: "100%", minHeight: 56, borderRadius: 12 }} /></div>
        </div>
      )}
    </div>
  );
}

// Рабочее место на expanded: поля слева + живой лист справа, действия над листом. Используется и в мастере быстрого монтажа.
function PassportWorkspace({ editorProps = {}, platform = "desktop", short, onBack, title = "Паспорт монтажа" }) {
  const [live, setLive] = React.useState(null);
  const [menu, setMenu] = React.useState(false);
  const [snack, setSnack] = React.useState(null);
  const [xp, setXp] = React.useState(false);
  const show = (x) => { setSnack(x); setTimeout(() => setSnack(null), 3000); };
  const pages = window.PassportDocPages({ variant: "live", data: live || { scenario: editorProps.scenario || "fast" } });
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)", position: "relative", fontFamily: "var(--font-sans)" }}>
      <PlTopBar title={title} subtitle="№ 20261119-234567-1 · 19.11.2026" navigationIcon={onBack ? <PlIconBtn icon="arrow_back" contentDescription="Назад" onClick={onBack} /> : null}
        actions={<div title="Черновик сохраняется сам" style={{ display: "flex", alignItems: "center", gap: 6, padding: "0 16px", minHeight: 48 }}><PlIcon icon="cloud_done" size={22} color={plMuted} /><PL_DS.CmpText variant="additional0" color={plMuted}>Сохранено</PL_DS.CmpText></div>} />
      <div style={{ flex: 1, minHeight: 0, display: "flex", borderTop: "1px solid var(--border-highlight)" }}>
        <div style={{ flex: short ? "0 1 460px" : "0 1 520px", minWidth: 380, borderRight: "1px solid var(--border-highlight)", display: "flex", flexDirection: "column", minHeight: 0, position: "relative" }}>
          <window.PassportEditor embedded initialOpen={editorProps.photosInit && editorProps.photosInit !== "none" ? ["photos"] : editorProps.scenario === "partial" ? ["eq"] : ["vehicle"]} {...editorProps} onChange={setLive} />
        </div>
        <PlPreview dense={short} pages={pages} title="Предпросмотр" note="Обновляется по мере ввода · пустые поля не печатаются"
          actions={<PlDocActions dense={short} platform={platform} onSave={() => show({ t: "Файл сохранён · Загрузки", tone: "success", a: "Открыть" })} onMenu={() => setMenu(!menu)} />} />
      </div>
      {menu && (
        <div className="pl-anim" style={{ position: "absolute", right: 16, top: short ? 104 : 120, zIndex: 30, width: 320, background: "var(--surface-container-lowest)", border: "1px solid var(--border-highlight)", borderRadius: 12, boxShadow: "0 8px 24px rgba(10,26,58,.18)", padding: 6, transformOrigin: "top right", animation: `plIn 160ms ${PL_EASE}` }}>
          {short && <React.Fragment>
            <PlMenuItem icon="open_in_new" text="Открыть" onClick={() => setMenu(false)} />
            {platform !== "desktop" && <PlMenuItem icon="download" text="Сохранить файл" onClick={() => { setMenu(false); show({ t: "Файл сохранён · Загрузки", tone: "success" }); }} />}
            <div style={{ height: 1, background: "var(--border-highlight)", margin: "6px 8px" }} />
          </React.Fragment>}
          <PlMenuItem icon="folder_zip" text="Экспорт паспорта (ZIP)" onClick={() => { setMenu(false); setXp(true); }} />
          <PlText variant="additional0" color={plMuted} style={{ padding: "10px 14px 6px" }}>Настройки блока · в документ не попадают</PlText>
          <PlMenuItem icon="save" text="Сохранить в файл" onClick={() => { setMenu(false); show({ t: "Файл настроек сохранён", tone: "success" }); }} />
          <PlMenuItem icon="folder" text="Сохранить в мои конфигурации" onClick={() => { setMenu(false); show({ t: "Сохранено в мои конфигурации", tone: "success", a: "Открыть" }); }} />
        </div>
      )}
      {xp && window.PtExport && <window.PtExport items={[{ ...PL_ITEMS[0], photos: live ? live.photos : 0, settings: true }]} platform={platform} wide onClose={() => setXp(false)} onDone={({ name }) => { setXp(false); show({ t: `Сохранено: Загрузки/${name}`, tone: "success", a: "Открыть" }); }} />}
      {snack && (
        <div className="pl-anim" style={{ position: "absolute", left: 16, right: 16, bottom: 16, zIndex: 40, display: "flex", justifyContent: "center", animation: `plIn 240ms ${PL_EASE}` }}>
          <div style={{ width: "100%", maxWidth: 520 }}><PlSnack message={snack.t} tone={snack.tone} actionLabel={snack.a} onActionClick={() => setSnack(null)} style={{ width: "100%", minHeight: 56, borderRadius: 12 }} /></div>
        </div>
      )}
    </div>
  );
}

function PlReadyLive({ forming, state, ...p }) {
  const [st, setSt] = React.useState(forming ? "forming" : state === "forming" || state === "error" ? state : "ready");
  React.useEffect(() => { if (forming) { const t = setTimeout(() => setSt("ready"), 1600); return () => clearTimeout(t); } }, []);
  return <window.PassportReady state={st} {...p} />;
}

Object.assign(window, { PassportApp, PassportWorkspace, PL_ITEMS });
