// Конфигуратор — новые компоненты кита: ModelBadge, Rating, MineCard, CloudCard, AttachGrid, OptRow, Chip, TextArea,
// FullDlg (полноэкранный диалог на compact / диалог на широком), Popover, SysFile (момент вызова системного окна),
// DiffView (K9 «Что изменится»), Toast. Экспорт — window.CfgKit.
(function () {
const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpText, CmpIcon, CmpIconButton, CmpLinearProgressIndicator, CmpSnackbar } = DS;
const D = window.DSET;
const { useState } = React;

function useHover() {
  const [h, setH] = useState(false), [f, setF] = useState(false);
  return [h, f, { onMouseEnter: () => setH(true), onMouseLeave: () => setH(false), onFocus: () => setF(true), onBlur: () => setF(false) }];
}
const focusRing = (f) => f ? { outline: "3px solid var(--primary)", outlineOffset: 2 } : { outline: "none" };

function ModelBadge({ m, big }) {
  return <span style={{ display: "inline-flex", alignItems: "center", height: big ? 32 : 28, padding: "0 10px", borderRadius: 8, background: "var(--surface-container-highest)", color: "var(--on-surface)", font: `600 ${big ? 15 : 14}px var(--font-mono)`, whiteSpace: "nowrap", flex: "0 0 auto", boxSizing: "border-box", border: "1px solid var(--outline-variant)" }}>{m}</span>;
}
function Rating({ r, votes }) {
  if (r == null) return null;
  return <span aria-label={"Рейтинг " + r + (votes ? ", оценок " + votes : "")} style={{ display: "inline-flex", alignItems: "center", gap: 4, font: "600 15px var(--font-sans)", color: "var(--on-surface)", whiteSpace: "nowrap" }}><CmpIcon icon="star" size={20} color="var(--rating, #E0A100)" />{String(r).replace(".", ",")}{votes ? <span style={{ fontWeight: 400, color: "var(--on-surface-variant)" }}>· {votes}</span> : null}</span>;
}
function Plaque({ icon, text, tone }) {
  const t = tone === "warn" ? { bg: "var(--error-container)", fg: "var(--on-error-container)" } : { bg: "var(--tertiary-container)", fg: "var(--on-tertiary-container)" };
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 28, padding: "0 10px 0 8px", borderRadius: 8, background: t.bg, color: t.fg, font: "500 14px var(--font-sans)", whiteSpace: "nowrap", flex: "0 0 auto" }}>{icon && <CmpIcon icon={icon} size={18} />}{text}</span>;
}

function Hl({ text, q }) {
  const s = (q || "").trim().toLowerCase();
  if (!s) return text;
  const i = text.toLowerCase().indexOf(s);
  if (i < 0) return text;
  return <React.Fragment>{text.slice(0, i)}<mark style={{ background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", borderRadius: 3 }}>{text.slice(i, i + s.length)}</mark>{text.slice(i + s.length)}</React.Fragment>;
}

const clamp = (n) => ({ display: "-webkit-box", WebkitLineClamp: n, WebkitBoxOrient: "vertical", overflow: "hidden" });

// Карточка «Моя конфигурация». Тап — открыть; ⋮ — меню (48 dp).
function MineCard({ c, q, selected, onOpen, onMenu, pick }) {
  const [h, f, hv] = useHover();
  return (
    <div style={{ position: "relative", borderRadius: 16, background: selected ? "var(--secondary-container)" : "var(--surface-container-low)", border: "1px solid " + (selected ? "transparent" : "var(--outline-variant)"), color: selected ? "var(--on-secondary-container)" : "var(--on-surface)", overflow: "hidden" }}>
      <button type="button" onClick={onOpen} {...hv} aria-current={selected || undefined}
        style={{ display: "flex", flexDirection: "column", gap: 8, width: "100%", minHeight: 88, padding: "14px " + (onMenu ? 60 : 16) + "px 14px 16px", border: "none", background: h ? "color-mix(in srgb, var(--on-surface) 6%, transparent)" : "transparent", color: "inherit", textAlign: "left", cursor: "pointer", boxSizing: "border-box", borderRadius: 16, ...focusRing(f) }}>
        <span style={{ font: "500 18px/1.3 var(--font-sans)", ...clamp(2), wordBreak: "break-word" }}><Hl text={c.name} q={q} /></span>
        <span style={{ display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" }}>
          <ModelBadge m={c.model} />
          <span style={{ flex: 1 }} />
          <span style={{ font: "400 14px var(--font-sans)", color: "var(--on-surface-variant)", whiteSpace: "nowrap" }}>{c.updated}</span>
        </span>
        {c.desc ? <span style={{ font: "400 16px/1.4 var(--font-sans)", color: "var(--on-surface-variant)", ...clamp(2) }}><Hl text={c.desc} q={q} /></span> : null}
        {pick && <span style={{ display: "inline-flex", alignItems: "center", gap: 6, font: "600 15px var(--font-sans)", color: "var(--primary)" }}><CmpIcon icon="download" size={20} />Загрузить в редактор</span>}
      </button>
      {onMenu && <div style={{ position: "absolute", top: 6, right: 6 }}><CmpIconButton icon="MoreVert" contentDescription={"Действия: " + c.name} onClick={(e) => onMenu(e)} /></div>}
    </div>
  );
}

// Карточка облачной конфигурации в списке.
function CloudCard({ c, selected, onOpen, canBad }) {
  const [h, f, hv] = useHover();
  return (
    <button type="button" onClick={onOpen} {...hv} aria-current={selected || undefined}
      style={{ display: "flex", flexDirection: "column", gap: 8, width: "100%", padding: "14px 16px", borderRadius: 16, border: "1px solid " + (selected ? "transparent" : "var(--outline-variant)"), background: selected ? "var(--secondary-container)" : h ? "var(--surface-container)" : "var(--surface-container-low)", color: selected ? "var(--on-secondary-container)" : "var(--on-surface)", textAlign: "left", cursor: "pointer", boxSizing: "border-box", ...focusRing(f) }}>
      <span style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
        <span style={{ flex: 1, minWidth: 0, font: "500 18px/1.3 var(--font-sans)", ...clamp(2) }}>{c.name}</span>
        <Rating r={c.rating} votes={c.votes} />
      </span>
      {c.short && <span style={{ font: "400 16px/1.4 var(--font-sans)", color: "var(--on-surface-variant)", ...clamp(2) }}>{c.short}</span>}
      <span style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
        <ModelBadge m={c.model} />
        {c.mineOnly && <Plaque icon="lock" text="Видна только вам" />}
        {canBad && <Plaque icon="warning" tone="warn" text={"Нужна прошивка " + c.can} />}
      </span>
      <span style={{ font: "400 14px var(--font-sans)", color: "var(--on-surface-variant)" }}>{c.author} · {c.updated}</span>
    </button>
  );
}

// Сетка вложений: превью картинок и PDF с подписями. Тап — просмотр.
function AttachGrid({ items, onOpen, cols = 3 }) {
  return (
    <div style={{ display: "grid", gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`, gap: 12 }}>
      {items.map((a, i) => <AttachTile key={i} a={a} onClick={() => onOpen(a, i)} />)}
    </div>
  );
}
function AttachTile({ a, onClick }) {
  const [h, f, hv] = useHover();
  const pdf = a.k === "pdf";
  return (
    <button type="button" onClick={onClick} {...hv} aria-label={(pdf ? "PDF: " : "Фото: ") + a.t}
      style={{ display: "flex", flexDirection: "column", gap: 6, padding: 0, border: "none", background: "transparent", color: "var(--on-surface)", textAlign: "left", cursor: "pointer", borderRadius: 12, ...focusRing(f) }}>
      <span style={{ position: "relative", width: "100%", aspectRatio: "4 / 3", borderRadius: 12, overflow: "hidden", display: "grid", placeItems: "center", boxSizing: "border-box",
        background: pdf ? "var(--surface-container-high)" : "repeating-linear-gradient(135deg, var(--surface-container-high) 0 8px, var(--surface-container-highest) 8px 16px)", border: "1px solid var(--outline-variant)" }}>
        <CmpIcon icon={pdf ? "picture_as_pdf" : "image"} size={32} color={pdf ? "var(--error)" : "var(--on-surface-variant)"} />
        {h && <span style={{ position: "absolute", inset: 0, background: "color-mix(in srgb, var(--on-surface) 8%, transparent)" }} />}
      </span>
      <span style={{ font: "400 14px/1.3 var(--font-sans)", ...clamp(2) }}>{a.t}</span>
    </button>
  );
}

// Строка выбора (источник / назначение / пункт меню). 64+ dp.
function OptRow({ icon, title, sub, onClick, disabled, danger, trail }) {
  const [h, f, hv] = useHover();
  return (
    <button type="button" onClick={onClick} disabled={disabled} {...hv}
      style={{ display: "flex", alignItems: "center", gap: 16, width: "100%", minHeight: sub ? 72 : 56, padding: "8px 20px", border: "none", background: h && !disabled ? "color-mix(in srgb, var(--on-surface) 6%, transparent)" : "transparent", color: danger ? "var(--error)" : "var(--on-surface)", textAlign: "left", cursor: disabled ? "default" : "pointer", boxSizing: "border-box", ...focusRing(f), outlineOffset: -3 }}>
      <span style={{ width: 40, height: 40, borderRadius: 12, flex: "0 0 auto", display: "grid", placeItems: "center", background: danger ? "transparent" : "var(--secondary-container)", color: danger ? "var(--error)" : "var(--on-secondary-container)", opacity: disabled ? 0.38 : 1 }}><CmpIcon icon={icon} size={24} /></span>
      <span style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
        <span style={{ font: "500 17px/1.3 var(--font-sans)", opacity: disabled ? 0.38 : 1 }}>{title}</span>
        {sub && <span style={{ font: "400 14px/1.35 var(--font-sans)", color: "var(--on-surface-variant)" }}>{sub}</span>}
      </span>
      {trail}
    </button>
  );
}

function Chip({ label, icon, onClick, active, trailing = "arrow_drop_down", onClear }) {
  const [h, f, hv] = useHover();
  return (
    <span style={{ display: "inline-flex", alignItems: "center", height: 48, flex: "0 0 auto" }}>
      <button type="button" onClick={onClick} {...hv}
        style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 40, padding: onClear ? "0 4px 0 12px" : "0 8px 0 12px", borderRadius: 8, border: active ? "none" : "1px solid var(--outline)", background: active ? "var(--secondary-container)" : h ? "color-mix(in srgb, var(--on-surface) 6%, transparent)" : "transparent", color: active ? "var(--on-secondary-container)" : "var(--on-surface)", font: "500 15px var(--font-sans)", cursor: "pointer", boxSizing: "border-box", whiteSpace: "nowrap", ...focusRing(f) }}>
        {icon && <CmpIcon icon={icon} size={18} />}{label}
        {onClear ? <span role="button" aria-label="Сбросить фильтр" onClick={(e) => { e.stopPropagation(); onClear(); }} style={{ width: 32, height: 32, display: "grid", placeItems: "center", borderRadius: 999 }}><CmpIcon icon="close" size={18} /></span> : <CmpIcon icon={trailing} size={20} />}
      </button>
    </span>
  );
}

function TextArea({ label, value, onChange, max, rows = 4, placeholder }) {
  const [f, setF] = useState(false);
  return (
    <label style={{ display: "flex", flexDirection: "column", gap: 4 }}>
      <span style={{ position: "relative", display: "block", borderRadius: 4, border: f ? "2px solid var(--primary)" : "1px solid var(--outline)", padding: f ? "21px 15px 11px" : "22px 16px 12px", boxSizing: "border-box" }}>
        <span style={{ position: "absolute", left: 12, top: -9, padding: "0 4px", background: "var(--surface-container-high)", font: "400 13px var(--font-sans)", color: f ? "var(--primary)" : "var(--on-surface-variant)" }}>{label}</span>
        <textarea value={value} rows={rows} placeholder={placeholder} maxLength={max} onFocus={() => setF(true)} onBlur={() => setF(false)} onChange={(e) => onChange(e.target.value.slice(0, max))}
          style={{ width: "100%", border: "none", outline: "none", resize: "vertical", background: "transparent", color: "var(--on-surface)", font: "400 16px/1.45 var(--font-sans)", padding: 0, boxSizing: "border-box", display: "block" }} />
      </span>
      <span style={{ display: "flex", justifyContent: "space-between", gap: 8, padding: "0 16px", font: "400 13px var(--font-sans)", color: "var(--on-surface-variant)" }}><span>Необязательно</span><span>{value.length} / {max}</span></span>
    </label>
  );
}

// Полноэкранный диалог (compact) / обычный диалог (wide).
function FullDlg({ wide, title, sub, onClose, action, children, footer, w = 600, closeIcon = "close" }) {
  const head = (
    <div style={{ display: "flex", alignItems: "center", gap: 4, minHeight: 64, padding: wide ? "12px 12px 4px 24px" : "4px 8px 4px 4px", flex: "0 0 auto" }}>
      {!wide && <CmpIconButton icon={closeIcon === "close" ? "Close" : "ArrowBack"} contentDescription="Закрыть" onClick={onClose} />}
      <div style={{ flex: 1, minWidth: 0, paddingLeft: wide ? 0 : 4 }}>
        <CmpText variant={wide ? "headlineSmall" : "titleLarge"} as="div" maxLines={2}>{title}</CmpText>
        {sub && <div style={{ font: "400 15px/1.35 var(--font-sans)", color: "var(--on-surface-variant)", marginTop: 2 }}>{sub}</div>}
      </div>
      {!wide && action}
      {wide && <CmpIconButton icon="Close" contentDescription="Закрыть" onClick={onClose} />}
    </div>
  );
  if (!wide) return (
    <div className="ds-an" style={{ position: "absolute", inset: 0, zIndex: 75, background: "var(--surface-container-high)", color: "var(--on-surface)", display: "flex", flexDirection: "column", animation: "dsUp .2s ease both" }}>
      {head}
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{children}</div>
      {footer && <div style={{ flex: "0 0 auto", borderTop: "1px solid var(--outline-variant)", padding: "12px 16px 16px" }}>{footer}</div>}
    </div>
  );
  return (
    <div onClick={onClose} className="ds-an" style={{ position: "absolute", inset: 0, zIndex: 75, background: "color-mix(in srgb, var(--scrim) 42%, transparent)", display: "grid", placeItems: "center", padding: 24, boxSizing: "border-box", animation: "dsFade .15s ease both" }}>
      <div onClick={(e) => e.stopPropagation()} role="dialog" aria-label={typeof title === "string" ? title : undefined} className="ds-an" style={{ width: "100%", maxWidth: w, maxHeight: "100%", display: "flex", flexDirection: "column", background: "var(--surface-container-high)", color: "var(--on-surface)", borderRadius: 28, boxShadow: "var(--elevation-3)", overflow: "hidden", animation: "dsPop .18s ease both" }}>
        {head}
        <div style={{ flex: "1 1 auto", minHeight: 0, overflow: "auto" }}>{children}</div>
        {footer && <div style={{ flex: "0 0 auto", padding: "12px 24px 24px" }}>{footer}</div>}
      </div>
    </div>
  );
}

// Шторка (compact) / диалог (wide) со списком вариантов.
function Sheet({ wide, title, sub, onClose, children, w = 480 }) {
  return (
    <D.Overlay wide={wide} onClose={onClose} w={w}>
      <div style={{ padding: wide ? "24px 24px 8px" : "12px 20px 8px", flex: "0 0 auto" }}>
        <CmpText variant="headlineSmall" as="div">{title}</CmpText>
        {sub && <div style={{ font: "400 15px/1.4 var(--font-sans)", color: "var(--on-surface-variant)", marginTop: 4 }}>{sub}</div>}
      </div>
      <div style={{ overflow: "auto", padding: "4px 0 " + (wide ? 16 : 24) + "px" }}>{children}</div>
    </D.Overlay>
  );
}

// Меню у кнопки (wide). Compact — используется Sheet.
function Popover({ at, onClose, children, w = 300 }) {
  return (
    <div onClick={onClose} style={{ position: "absolute", inset: 0, zIndex: 65 }}>
      <div onClick={(e) => e.stopPropagation()} className="ds-an" style={{ position: "absolute", left: Math.max(8, at.x - w), top: at.y, width: w, padding: "8px 0", background: "var(--surface-container)", borderRadius: 16, boxShadow: "var(--elevation-3)", animation: "dsPop .15s ease both", transformOrigin: "top right" }}>{children}</div>
    </div>
  );
}

// Момент вызова системного окна выбора / сохранения файла (само окно рисует платформа).
function SysFile({ mode, name, wide, onPick, onCancel }) {
  const open = mode === "open";
  return (
    <div className="ds-an" style={{ position: "absolute", inset: 0, zIndex: 85, background: "color-mix(in srgb, var(--scrim) 55%, transparent)", display: "flex", alignItems: wide ? "center" : "flex-end", justifyContent: "center", padding: wide ? 24 : 0, animation: "dsFade .15s ease both" }}>
      <div style={{ width: wide ? 520 : "100%", maxWidth: "100%", borderRadius: wide ? 16 : "16px 16px 0 0", border: "2px dashed color-mix(in srgb, #fff 70%, transparent)", background: "color-mix(in srgb, var(--surface) 92%, transparent)", color: "var(--on-surface)", padding: 20, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 14 }}>
        <span style={{ font: "500 12px var(--font-mono)", letterSpacing: ".06em", textTransform: "uppercase", color: "var(--on-surface-variant)" }}>Системное окно платформы · не рисуем</span>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <CmpIcon icon={open ? "folder_open" : "save"} size={28} color="var(--primary)" />
          <CmpText variant="titleMedium" as="div">{open ? "Выбор файла настроек (.dxs)" : "Сохранение файла"}</CmpText>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10, height: 52, padding: "0 14px", borderRadius: 10, background: "var(--surface-container-high)", font: "400 16px var(--font-mono)", overflow: "hidden" }}>
          <CmpIcon icon="description" size={22} color="var(--on-surface-variant)" /><span style={{ whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{name}</span>
        </div>
        <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
          <D.DBtn text="Отмена" onClick={onCancel} />
          <D.DBtn kind="filled" text={open ? "Открыть" : "Сохранить"} onClick={onPick} />
        </div>
      </div>
    </div>
  );
}

function Toast({ msg, action, onAction, onClose, bottom = 16 }) {
  return <div className="ds-an" style={{ position: "absolute", left: 16, right: 16, bottom, display: "flex", justifyContent: "center", zIndex: 95, animation: "dsUp .2s ease both" }}><CmpSnackbar message={msg} actionLabel={action || "OK"} onActionClick={onAction || onClose} /></div>;
}

function Progress({ title, text, p }) {
  return (
    <D.Dlg title={title} w={360}>
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        {p === undefined ? <CmpLinearProgressIndicator /> : <CmpLinearProgressIndicator progress={p} />}
        {text && <CmpText variant="bodyMedium">{text}</CmpText>}
      </div>
    </D.Dlg>
  );
}

// ── K9 «Что изменится»
const plural = (n, a, b, c) => { const m = n % 10, h = n % 100; return m === 1 && h !== 11 ? a : m >= 2 && m <= 4 && (h < 12 || h > 14) ? b : c; };
function ioDiff(it, a, b) {
  const on = (v) => v.logics.filter((l) => l.on).map((l) => l.n);
  const A = on(a), B = on(b);
  const plus = B.filter((x) => !A.includes(x)), minus = A.filter((x) => !B.includes(x));
  const parts = [];
  if (it.type === "in" && a.type !== b.type) parts.push(["НО", "НЗ"][a.type] + " → " + ["НО", "НЗ"][b.type]);
  if (it.type !== "in" && a.pol !== b.pol) parts.push(["+", "−"][a.pol] + " → " + ["+", "−"][b.pol]);
  plus.forEach((x) => parts.push("+ " + x)); minus.forEach((x) => parts.push("− " + x));
  return parts;
}
function diffRows(cur, next) {
  const secs = {}, order = [];
  let can = null, total = 0;
  D.LEAVES.forEach((it) => {
    if (it.disabled || next[it.id] === undefined || D.same(cur[it.id], next[it.id])) return;
    const io = it.type === "in" || it.type === "out" || it.type === "osw";
    const row = { id: it.id, t: io ? it.t + " · " + it.name : it.t, io, from: io ? null : D.fmt(it, cur[it.id]), to: io ? null : D.fmt(it, next[it.id]), parts: io ? ioDiff(it, cur[it.id], next[it.id]) : null, can: it.type === "can" };
    total++;
    if (row.can) { can = row; return; }
    const top = D.chain(it.id)[0];
    if (!secs[top.id]) { secs[top.id] = { id: top.id, t: top.t, rows: [] }; order.push(top.id); }
    secs[top.id].rows.push(row);
  });
  return { total, can, secs: order.map((k) => secs[k]) };
}
function DiffRow({ r, wide, open, onToggle }) {
  const [h, f, hv] = useHover();
  const val = (s, strong) => <span style={{ font: `${strong ? 600 : 400} 16px/1.35 var(--font-sans)`, color: strong ? "var(--on-surface)" : "var(--on-surface-variant)", ...(open ? { wordBreak: "break-word" } : { whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }) }}>{s}</span>;
  const ioText = r.parts && r.parts.join(", ");
  const bg = r.can ? "var(--tertiary-container)" : h ? "color-mix(in srgb, var(--on-surface) 5%, transparent)" : "transparent";
  return (
    <button type="button" onClick={onToggle} aria-expanded={open} {...hv}
      style={{ display: "grid", gridTemplateColumns: wide ? "64px minmax(0, 1.3fr) minmax(0, 1fr) minmax(0, 1fr)" : "minmax(0, 1fr) auto", alignItems: "center", columnGap: 12, rowGap: 2, width: "100%", minHeight: 52, padding: "8px 16px", border: "none", borderRadius: r.can ? 12 : 0, background: bg, color: r.can ? "var(--on-tertiary-container)" : "var(--on-surface)", textAlign: "left", cursor: "pointer", boxSizing: "border-box", ...focusRing(f), outlineOffset: -3 }}>
      {wide && <span style={{ font: "400 14px var(--font-mono)", color: "var(--on-surface-variant)" }}>{r.id}</span>}
      <span style={{ display: "flex", alignItems: "baseline", gap: 8, minWidth: 0 }}>
        {!wide && <span style={{ font: "400 14px var(--font-mono)", color: "var(--on-surface-variant)", flex: "0 0 auto" }}>{r.id}</span>}
        <span style={{ font: "400 16px/1.35 var(--font-sans)", ...(open ? { wordBreak: "break-word" } : { whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }) }}>{r.t}</span>
      </span>
      {r.io ? (
        wide ? <span style={{ gridColumn: "3 / 5", font: "500 16px/1.35 var(--font-sans)", ...(open ? {} : { whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }) }}>{ioText}</span>
          : <span style={{ font: "500 16px/1.35 var(--font-sans)", maxWidth: 170, textAlign: "right", ...(open ? {} : { whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }) }}>{open ? "" : ioText}</span>
      ) : wide ? (
        <React.Fragment>{val(r.from)}<span style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 0 }}><CmpIcon icon="arrow_forward" size={18} color="var(--on-surface-variant)" />{val(r.to, true)}</span></React.Fragment>
      ) : (
        <span style={{ display: "flex", alignItems: "center", gap: 6, maxWidth: 190, minWidth: 0, justifyContent: "flex-end" }}>{!open && <React.Fragment>{val(r.from)}<span style={{ color: "var(--on-surface-variant)", flex: "0 0 auto" }}>→</span>{val(r.to, true)}</React.Fragment>}</span>
      )}
      {open && !wide && (
        <span style={{ gridColumn: "1 / -1", display: "flex", flexDirection: "column", gap: 4, paddingTop: 6 }}>
          {r.io ? r.parts.map((p, i) => <span key={i} style={{ font: "500 16px/1.35 var(--font-sans)" }}>{p}</span>) : (
            <React.Fragment>
              <span style={{ font: "400 15px/1.35 var(--font-sans)", color: "var(--on-surface-variant)" }}>Было: <span style={{ color: "var(--on-surface)" }}>{r.from}</span></span>
              <span style={{ font: "400 15px/1.35 var(--font-sans)", color: "var(--on-surface-variant)" }}>Станет: <b style={{ color: "var(--on-surface)" }}>{r.to}</b></span>
            </React.Fragment>
          )}
        </span>
      )}
    </button>
  );
}
function DiffView({ d, wide, initOpen }) {
  const many = d.total > 10;
  const [exp, setExp] = useState(() => initOpen || (d.secs.length ? (many ? [d.secs[0].id] : d.secs.map((s) => s.id)) : []));
  const [rowOpen, setRowOpen] = useState(null);
  const [q, setQ] = useState("");
  const ql = q.trim().toLowerCase();
  const secs = ql ? d.secs.map((s) => ({ ...s, rows: s.rows.filter((r) => r.t.toLowerCase().includes(ql) || r.id.startsWith(ql)) })).filter((s) => s.rows.length) : d.secs;
  const VK = window.VsKit;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8, padding: wide ? "0 16px 8px" : "0 0 8px" }}>
      {d.total > 20 && <div style={{ padding: wide ? "4px 8px 4px" : "4px 16px 4px" }}><VK.SearchBar value={q} onChange={setQ} placeholder="Найти в изменениях" /></div>}
      {wide && <div style={{ display: "grid", gridTemplateColumns: "64px minmax(0, 1.3fr) minmax(0, 1fr) minmax(0, 1fr)", columnGap: 12, padding: "4px 16px", font: "500 13px var(--font-sans)", letterSpacing: ".03em", color: "var(--on-surface-variant)", textTransform: "uppercase" }}><span>№</span><span>Настройка</span><span>Было</span><span>Станет</span></div>}
      {d.can && (!ql || d.can.t.toLowerCase().includes(ql)) && (
        <div style={{ padding: wide ? 0 : "0 8px" }}>
          <DiffRow r={d.can} wide={wide} open={rowOpen === d.can.id} onToggle={() => setRowOpen(rowOpen === d.can.id ? null : d.can.id)} />
          <div style={{ display: "flex", gap: 8, alignItems: "center", padding: "6px 16px 2px", font: "400 14px/1.35 var(--font-sans)", color: "var(--on-surface-variant)" }}><CmpIcon icon="memory" size={18} />Проверьте, что прошивка блока подходит под новый CAN-код.</div>
        </div>
      )}
      {secs.map((s) => {
        const on = ql || exp.includes(s.id);
        return (
          <div key={s.id} style={{ borderTop: "1px solid var(--outline-variant)" }}>
            <button type="button" onClick={() => setExp(on ? exp.filter((x) => x !== s.id) : [...exp, s.id])} aria-expanded={!!on}
              style={{ display: "flex", alignItems: "center", gap: 10, width: "100%", minHeight: 56, padding: "0 16px 0 10px", border: "none", background: "transparent", color: "var(--on-surface)", cursor: "pointer", textAlign: "left" }}>
              <CmpIcon icon={on ? "expand_more" : "chevron_right"} size={26} color="var(--on-surface-variant)" />
              <span style={{ font: "400 15px var(--font-mono)", color: "var(--on-surface-variant)" }}>{s.id}</span>
              <span style={{ flex: 1, font: "500 17px var(--font-sans)" }}>{s.t}</span>
              <D.CountPill n={s.rows.length} />
            </button>
            {on && <div style={{ paddingBottom: 8 }}>{s.rows.map((r) => <DiffRow key={r.id} r={r} wide={wide} open={rowOpen === r.id} onToggle={() => setRowOpen(rowOpen === r.id ? null : r.id)} />)}</div>}
          </div>
        );
      })}
      {ql && !secs.length && !(d.can && d.can.t.toLowerCase().includes(ql)) && <div style={{ padding: 24, textAlign: "center" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">В изменениях нет «{q}»</CmpText></div>}
    </div>
  );
}
const diffSummary = (d) => `Изменится ${d.total} ${plural(d.total, "настройка", "настройки", "настроек")} в ${d.secs.length + (d.can && !d.secs.some((s) => s.id === "6") ? 1 : 0)} ${plural(d.secs.length + (d.can && !d.secs.some((s) => s.id === "6") ? 1 : 0), "разделе", "разделах", "разделах")}`;

window.CfgKit = { ModelBadge, Rating, Plaque, Hl, MineCard, CloudCard, AttachGrid, OptRow, Chip, TextArea, FullDlg, Sheet, Popover, SysFile, Toast, Progress, DiffView, DiffRow, diffRows, diffSummary, plural };
})();
