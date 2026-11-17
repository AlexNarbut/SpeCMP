// Vehicle select — новые компоненты кита: BusChip, CapBadge, Ph (заглушка картинки), Logo, Hl (подсветка),
// VsRow (строка списка 56–72 dp, hover/focus/selected), SkelList, StateBox, Seg, CanInput, SearchBar, Crumbs,
// Html (HTML-примечание), Gallery + PhotoViewer. Экспорт — window.VsKit.
const VK_DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpText: KText, CmpIcon: KIcon, CmpIconButton: KIconBtn, CmpButton: KBtn, CmpOutlinedButton: KOBtn } = VK_DS;

(function () {
  if (document.getElementById("vs-anim")) return;
  const s = document.createElement("style"); s.id = "vs-anim";
  s.textContent = "@keyframes vsShim{0%{opacity:.55}50%{opacity:1}100%{opacity:.55}}@keyframes vsIn{from{opacity:0;transform:translateY(4px)}to{opacity:1;transform:none}}" +
    ".vs-html p{margin:0 0 8px}.vs-html p:last-child{margin-bottom:0}.vs-html ul,.vs-html ol{margin:0 0 8px;padding-left:22px}.vs-html li{margin:2px 0}" +
    ".vs-scroll::-webkit-scrollbar{width:8px;height:8px}.vs-scroll::-webkit-scrollbar-thumb{background:var(--outline-variant);border-radius:8px}" +
    "@media (prefers-reduced-motion: reduce){.vs-anim{animation:none!important}}";
  document.head.appendChild(s);
})();

function useVsWidth() {
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

// ── Шины ─────────────────────────────────────────────────────────────────
// Семейство → цвет; номер шины всегда в подписи. «Неизвестно» — пунктирный контур без заливки.
const BUS_FAM_VAR = { can: "var(--bus-can)", fd: "var(--bus-canfd)", lin: "var(--bus-lin)", bean: "var(--bus-bean)", imo: "var(--bus-imo)", unknown: "var(--bus-unknown)" };
function busFam(b) { return (window.VS_DATA && window.VS_DATA.BUS_FAMILY[b]) || "unknown"; }
function busVar(b) { return BUS_FAM_VAR[busFam(b)]; }
function BusChip({ bus, size = "m" }) {
  const fam = busFam(bus), c = BUS_FAM_VAR[fam], s = size === "s", unk = fam === "unknown";
  return (
    <span title={bus} style={{ display: "inline-flex", alignItems: "center", gap: 6, minHeight: s ? 26 : 30, padding: s ? "2px 8px" : "3px 10px", borderRadius: 8, boxSizing: "border-box", flex: "0 1 auto", maxWidth: "100%",
      border: `2px ${unk ? "dashed" : "solid"} ${c}`, background: unk ? "transparent" : `color-mix(in srgb, ${c} 14%, var(--surface))`, color: "var(--on-surface)",
      font: `${unk ? 600 : 700} ${s ? 13 : 14}px/1.2 ${unk ? "var(--font-sans)" : "var(--font-mono)"}`, letterSpacing: ".01em" }}>
      {!unk && <span aria-hidden="true" style={{ width: 8, height: 8, borderRadius: 2, background: c, flex: "0 0 auto" }} />}
      {unk && <KIcon icon="help_outline" size={s ? 15 : 16} color="var(--on-surface-variant)" />}
      <span style={{ whiteSpace: "nowrap" }}>{bus}</span>
    </span>
  );
}

// ── Бейдж возможности: поддерживается / не поддерживается / нет данных; тап — примечание
const CAP = {
  yes: { t: "поддерживается", icon: "check_circle", bg: "color-mix(in srgb, var(--success) 22%, var(--surface))", bd: "var(--success)", ic: "color-mix(in srgb, var(--success) 70%, var(--on-surface))" },
  no: { t: "не поддерживается", icon: "cancel", bg: "var(--error-container)", bd: "var(--error)", ic: "var(--error)" },
  nodata: { t: "нет данных", icon: "help", bg: "var(--background-surface-highlight)", bd: "var(--outline)", ic: "var(--on-surface-variant)" },
};
function CapBadge({ label, v, note, onClick, fill, tile }) {
  const k = CAP[v] || CAP.nodata, has = !!note;
  const [hov, setHov] = React.useState(false);
  return (
    <button type="button" onClick={has ? onClick : undefined} disabled={!has} aria-label={label + ": " + k.t + (has ? ". Открыть примечание" : "")}
      onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}
      style={{ position: "relative", display: "flex", flexDirection: tile ? "column" : "row", alignItems: tile ? "flex-start" : "center", gap: tile ? 6 : 10, minHeight: tile ? 112 : 56, padding: tile ? "10px 12px" : "8px 12px", borderRadius: "var(--r-card-small-radius)", border: `2px solid ${k.bd}`, background: k.bg, color: "var(--on-surface)", textAlign: "left", cursor: has ? "pointer" : "default", boxSizing: "border-box", flex: fill ? "1 1 0" : "0 1 auto", minWidth: 0, font: "inherit",
        boxShadow: has && hov ? "inset 0 0 0 100px color-mix(in srgb, var(--on-surface) 6%, transparent)" : "none" }}>
      <KIcon icon={k.icon} size={26} color={k.ic} />
      <span lang="ru" style={{ display: "flex", flexDirection: "column", minWidth: 0, flex: 1, alignSelf: "stretch", justifyContent: "center" }}>
        <span style={{ font: "600 15px/1.25 var(--font-sans)", overflowWrap: "break-word", hyphens: "auto", paddingRight: tile && has ? 26 : 0 }}>{label}</span>
        <span style={{ font: "500 14px/1.25 var(--font-sans)", color: "var(--on-surface)", overflowWrap: "break-word", hyphens: "auto" }}>{k.t}</span>
      </span>
      {has && <span style={tile ? { position: "absolute", top: 8, right: 8 } : { display: "inline-flex" }}><KIcon icon="info" size={22} color="var(--primary)" /></span>}
    </button>
  );
}

// ── Заглушка картинки: фиксированное соотношение сторон → раскладка не прыгает
function Ph({ label, ratio = "16 / 9", icon = "directions_car", fail = false, radius = 14, h, w, small }) {
  return (
    <div role="img" aria-label={fail ? "Изображение не загрузилось" : label}
      style={{ position: "relative", width: w || "100%", height: h, aspectRatio: h ? undefined : ratio, borderRadius: radius, overflow: "hidden", flex: "0 0 auto", boxSizing: "border-box",
        background: fail ? "var(--background-surface-highlight)" : "repeating-linear-gradient(135deg, var(--surface-container-high) 0 10px, var(--surface-container) 10px 20px)",
        border: "1px solid var(--border-highlight)", display: "grid", placeItems: "center", color: "var(--on-surface-variant)" }}>
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4, padding: 6, textAlign: "center" }}>
        <KIcon icon={fail ? "broken_image" : icon} size={small ? 24 : 36} color="currentColor" />
        {!small && <span style={{ font: "500 12px var(--font-mono)", lineHeight: 1.3 }}>{fail ? "Фото недоступно" : label}</span>}
      </div>
    </div>
  );
}
function Logo({ name, size = 44 }) {
  const ini = name.replace(/[^A-Za-zА-Яа-я0-9 ]/g, "").split(/[\s-]+/).filter(Boolean).slice(0, 2).map((x) => x[0]).join("").toUpperCase();
  return <div aria-hidden="true" style={{ width: size, height: size, borderRadius: "var(--r-card-small-radius)", flex: "0 0 auto", display: "grid", placeItems: "center", background: "var(--background-surface-highlight)", color: "var(--on-surface-variant)", font: `700 ${Math.round(size * 0.36)}px var(--font-sans)`, border: "1px solid var(--border-highlight)", boxSizing: "border-box" }}>{ini}</div>;
}

// ── Подсветка совпадения
function Hl({ text, q }) {
  const toks = (q || "").toLowerCase().split(/\s+/).filter(Boolean);
  if (!toks.length) return text;
  const low = text.toLowerCase();
  let best = -1, len = 0;
  toks.forEach((t) => { const i = low.indexOf(t); if (i >= 0 && (best < 0 || i < best)) { best = i; len = t.length; } });
  if (best < 0) return text;
  return <React.Fragment>{text.slice(0, best)}<mark style={{ background: "var(--primary-container)", color: "var(--on-primary-container)", borderRadius: 4, padding: "0 1px", fontWeight: 700 }}>{text.slice(best, best + len)}</mark>{text.slice(best + len)}</React.Fragment>;
}

// ── Строка списка
function VsRow({ lead, title, sub, trail, action, onClick, selected, chevron = true, dense, children }) {
  const [hov, setHov] = React.useState(false);
  const [foc, setFoc] = React.useState(false);
  const bg = selected ? "var(--secondary-container)" : hov ? "color-mix(in srgb, var(--on-surface) 6%, transparent)" : "transparent";
  return (
    <div className="vs-anim" style={{ animation: "vsIn .2s ease both", borderRadius: "var(--r-card-small-radius)", background: bg, outline: foc ? "3px solid var(--primary)" : "none", outlineOffset: -3, position: "relative" }}
      onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}>
      <div role="button" tabIndex={0} aria-current={selected ? "true" : undefined} onClick={onClick} onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); onClick && onClick(); } }}
        onFocus={(e) => setFoc(e.target.matches(":focus-visible"))} onBlur={() => setFoc(false)}
        style={{ display: "flex", alignItems: "center", gap: 14, minHeight: dense ? 56 : 64, padding: "8px 12px", cursor: "pointer", outline: "none", color: selected ? "var(--on-secondary-container)" : "var(--on-surface)" }}>
        {lead}
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 2 }}>
          <span style={{ font: `${selected ? 600 : 500} 17px/1.3 var(--font-sans)`, overflowWrap: "anywhere" }}>{title}</span>
          {sub && <span style={{ font: "400 15px/1.3 var(--font-sans)", color: selected ? "currentColor" : "var(--on-surface-variant)" }}>{sub}</span>}
          {action && <span onClick={(e) => e.stopPropagation()} style={{ display: "flex", marginTop: 6 }}>{action}</span>}
        </div>
        {trail}
        {chevron && <KIcon icon="chevron_right" size={26} color={selected ? "currentColor" : "var(--on-surface-variant)"} />}
      </div>
      {children}
    </div>
  );
}

function SkelList({ n = 6, lead = 44, thumb }) {
  const bar = (w, h = 14) => <div style={{ width: w, height: h, borderRadius: 6, background: "var(--background-surface-highlight)" }} />;
  return (
    <div aria-busy="true" aria-label="Загрузка" style={{ display: "flex", flexDirection: "column" }}>
      {Array.from({ length: n }).map((_, i) => (
        <div key={i} className="vs-anim" style={{ display: "flex", alignItems: "center", gap: 14, minHeight: 64, padding: "8px 12px", animation: `vsShim 1.4s ease-in-out ${i * 0.08}s infinite` }}>
          <div style={{ width: thumb ? 88 : lead, height: lead, borderRadius: "var(--r-card-small-radius)", background: "var(--background-surface-highlight)", flex: "0 0 auto" }} />
          <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 8 }}>{bar(`${50 + ((i * 37) % 35)}%`, 16)}{thumb && bar("30%", 12)}</div>
        </div>))}
    </div>
  );
}

// ── Состояния уровня: error / offline / empty / filter / nothing / cannone
function StateBox({ kind, title, text, onRetry, onReset, action, compact }) {
  const P = {
    error: { icon: "cloud_off", t: "Не удалось загрузить", x: "Сервер не ответил. Выбранные марка и модель сохранены." },
    offline: { icon: "wifi_off", t: "Нет подключения к интернету", x: "Каталог загружается с сервера. Подключитесь к сети и повторите." },
    empty: { icon: "inventory_2", t: "Список пуст", x: "" },
    filter: { icon: "filter_alt_off", t: "Под фильтр ничего не подходит", x: "Выбранные группы функций отсеяли все автомобили на этом уровне." },
    nothing: { icon: "search_off", t: "Ничего не найдено", x: "Проверьте написание или найдите по CAN-коду." },
  }[kind] || {};
  return (
    <div role={kind === "error" || kind === "offline" ? "alert" : "status"} style={{ display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", gap: 12, padding: compact ? "24px 16px" : "40px 20px", maxWidth: 420, margin: "0 auto", boxSizing: "border-box" }}>
      <div style={{ width: compact ? 64 : 80, height: compact ? 64 : 80, borderRadius: "50%", display: "grid", placeItems: "center", background: kind === "error" || kind === "offline" ? "var(--error-container)" : "var(--background-surface-highlight)", color: kind === "error" || kind === "offline" ? "var(--on-error-container)" : "var(--on-surface-variant)" }}>
        <KIcon icon={P.icon} size={compact ? 32 : 40} color="currentColor" />
      </div>
      <KText variant="titleLarge">{title || P.t}</KText>
      {(text || P.x) && <KText variant="bodyLarge" color="var(--on-surface-variant)">{text || P.x}</KText>}
      {onRetry && <KBtn text="Повторить" icon="refresh" onClick={onRetry} style={{ height: 52, minWidth: 200 }} />}
      {onReset && <KOBtn text="Сбросить фильтр" icon="filter_alt_off" onClick={onReset} style={{ height: 52, minWidth: 200 }} />}
      {action}
    </div>
  );
}

function Seg({ opts, value, onChange, style }) {
  return (
    <div role="radiogroup" style={{ display: "flex", height: 48, borderRadius: 999, border: "1px solid var(--border-light)", overflow: "hidden", boxSizing: "border-box", flex: "0 0 auto", ...style }}>
      {opts.map(([k, l], i) => {
        const on = k === value;
        return (
          <button key={k} type="button" role="radio" aria-checked={on} onClick={() => onChange(k)}
            style={{ flex: 1, minWidth: 0, border: "none", borderLeft: i ? "1px solid var(--border-light)" : "none", background: on ? "var(--secondary-container)" : "transparent", color: on ? "var(--on-secondary-container)" : "var(--on-surface)", font: "500 15px var(--font-sans)", display: "flex", alignItems: "center", justifyContent: "center", gap: 6, padding: "0 10px", cursor: "pointer" }}>
            {on && <KIcon icon="check" size={18} color="currentColor" />}<span style={{ whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{l}</span>
          </button>);
      })}
    </div>
  );
}

// ── Поле CAN-кода: 4 ячейки, числовая клавиатура, поиск уходит сам после 4-й цифры
function CanInput({ value, onChange, hint, autoFocus, state }) {
  const ref = React.useRef(null);
  React.useEffect(() => { autoFocus && ref.current && ref.current.focus(); }, []);
  const [foc, setFoc] = React.useState(false);
  const err = state === "none";
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <div style={{ position: "relative", display: "flex", gap: 10, alignItems: "center" }}>
        {[0, 1, 2, 3].map((i) => (
          <span key={i} aria-hidden="true" style={{ width: 58, height: 60, boxSizing: "border-box", borderRadius: "var(--r-card-small-radius)", display: "grid", placeItems: "center", font: "700 28px var(--font-mono)", color: "var(--on-surface)", background: "var(--background-surface-card)",
            border: err ? "2px solid var(--error)" : foc && i === Math.min(value.length, 3) ? "2px solid var(--primary)" : "1px solid var(--border-light)" }}>{value[i] || ""}</span>))}
        {value && <KIconBtn icon="close" contentDescription="Очистить код" onClick={() => { onChange(""); ref.current && ref.current.focus(); }} />}
        <input ref={ref} value={value} onChange={(e) => onChange(e.target.value.replace(/\D/g, "").slice(0, 4))} onFocus={() => setFoc(true)} onBlur={() => setFoc(false)}
          inputMode="numeric" pattern="[0-9]*" maxLength={4} aria-label="CAN-код, 4 цифры"
          style={{ position: "absolute", left: 0, top: 0, width: 262, height: "100%", opacity: 0, border: "none", cursor: "text", fontSize: "var(--t-paragraph2-size)" }} />
      </div>
      <span style={{ font: "500 14px var(--font-sans)", color: hint ? "var(--primary)" : "var(--on-surface-variant)", minHeight: 18, display: "inline-flex", alignItems: "center", gap: 6 }}>
        {hint ? <React.Fragment><KIcon icon="memory" size={16} color="currentColor" />{hint}</React.Fragment> : value.length < 4 ? "Поиск начнётся после 4-й цифры" : ""}
      </span>
    </div>
  );
}

function SearchBar({ value, onChange, placeholder }) {
  const ref = React.useRef(null);
  const [foc, setFoc] = React.useState(false);
  return (
    <label style={{ display: "flex", alignItems: "center", gap: 8, height: 56, padding: "0 4px 0 16px", borderRadius: "var(--r-card-medium-radius)", boxSizing: "border-box", background: "var(--background-surface-highlight)", border: foc ? "2px solid var(--primary)" : "2px solid transparent", cursor: "text", flex: "1 1 auto", minWidth: 0 }}>
      <KIcon icon="search" size={26} color="var(--on-surface-variant)" />
      <input ref={ref} value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} onFocus={() => setFoc(true)} onBlur={() => setFoc(false)} aria-label={placeholder}
        style={{ flex: 1, minWidth: 0, border: "none", outline: "none", background: "transparent", color: "var(--on-surface)", font: "400 17px var(--font-sans)", height: "100%" }} />
      {value ? <KIconBtn icon="close" contentDescription="Очистить" onClick={() => { onChange(""); ref.current && ref.current.focus(); }} /> : <span style={{ width: 12 }} />}
    </label>
  );
}

function Crumbs({ items }) {
  return (
    <nav aria-label="Где вы в каталоге" className="vs-scroll" style={{ display: "flex", alignItems: "center", minHeight: 48, overflowX: "auto", flex: "0 0 auto" }}>
      {items.map((it, i) => {
        const last = i === items.length - 1;
        return (
          <React.Fragment key={i}>
            {i > 0 && <KIcon icon="chevron_right" size={20} color="var(--on-surface-variant)" />}
            <button type="button" disabled={last} onClick={it.on} aria-current={last ? "page" : undefined}
              style={{ height: 48, minWidth: 48, padding: "0 8px", border: "none", background: "transparent", whiteSpace: "nowrap", font: `${last ? 600 : 500} 16px var(--font-sans)`, color: last ? "var(--on-surface)" : "var(--primary)", cursor: last ? "default" : "pointer", textDecoration: last ? "none" : "underline", textUnderlineOffset: 4, textDecorationThickness: 1 }}>{it.t}</button>
          </React.Fragment>);
      })}
    </nav>
  );
}

function Html({ html, clamp }) {
  return <div className="vs-html" style={{ font: "400 16px/1.45 var(--font-sans)", color: "var(--on-surface)", ...(clamp ? { display: "-webkit-box", WebkitLineClamp: clamp, WebkitBoxOrient: "vertical", overflow: "hidden" } : {}) }} dangerouslySetInnerHTML={{ __html: html }} />;
}

// ── Галерея фото точки подключения: свайп, «3 из 7», миниатюры, полноэкранный просмотр
function useSwipe(onPrev, onNext) {
  const st = React.useRef(null);
  return {
    onPointerDown: (e) => { st.current = e.clientX; },
    onPointerUp: (e) => { if (st.current == null) return; const d = e.clientX - st.current; st.current = null; if (d > 40) onPrev(); else if (d < -40) onNext(); },
  };
}
function NavBtn({ icon, label, onClick, disabled, dark }) {
  return <button type="button" aria-label={label} onClick={(e) => { e.stopPropagation(); onClick(); }} disabled={disabled}
    style={{ width: 52, height: 52, borderRadius: "50%", border: "none", display: "grid", placeItems: "center", cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.35 : 1,
      background: dark ? "rgba(255,255,255,.16)" : "color-mix(in srgb, var(--surface) 88%, transparent)", color: dark ? "#fff" : "var(--on-surface)", boxShadow: dark ? "none" : "var(--elevation-1)" }}><KIcon icon={icon} size={30} color="currentColor" /></button>;
}
function Counter({ i, n, dark }) {
  return <span aria-live="polite" style={{ height: 32, padding: "0 12px", borderRadius: "var(--r-card-medium-radius)", display: "inline-flex", alignItems: "center", background: dark ? "rgba(255,255,255,.16)" : "var(--inverse-surface)", color: dark ? "#fff" : "var(--inverse-on-surface)", font: "600 15px var(--font-sans)" }}>{i + 1} из {n}</span>;
}
function Gallery({ count, idx, onIdx, onFull, label }) {
  const sw = useSwipe(() => onIdx(Math.max(0, idx - 1)), () => onIdx(Math.min(count - 1, idx + 1)));
  if (!count) return <Ph label="Фото нет" icon="no_photography" ratio="4 / 3" />;
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      <div {...sw} onClick={onFull} role="button" tabIndex={0} aria-label="Открыть фото на весь экран" style={{ position: "relative", touchAction: "pan-y", cursor: "zoom-in" }}>
        <Ph label={`${label} · фото ${idx + 1}`} icon="photo_camera" ratio="4 / 3" />
        <div style={{ position: "absolute", top: 10, left: 10 }}><Counter i={idx} n={count} /></div>
        <div style={{ position: "absolute", top: 4, right: 4 }}><NavBtn icon="fullscreen" label="На весь экран" onClick={onFull} /></div>
        {count > 1 && <React.Fragment>
          <div style={{ position: "absolute", left: 8, top: "50%", transform: "translateY(-50%)" }}><NavBtn icon="chevron_left" label="Предыдущее фото" disabled={idx === 0} onClick={() => onIdx(idx - 1)} /></div>
          <div style={{ position: "absolute", right: 8, top: "50%", transform: "translateY(-50%)" }}><NavBtn icon="chevron_right" label="Следующее фото" disabled={idx === count - 1} onClick={() => onIdx(idx + 1)} /></div>
        </React.Fragment>}
      </div>
      {count > 1 && (
        <div className="vs-scroll" style={{ display: "flex", gap: 8, overflowX: "auto", paddingBottom: 4 }}>
          {Array.from({ length: count }).map((_, i) => (
            <button key={i} type="button" aria-label={`Фото ${i + 1}`} aria-pressed={i === idx} onClick={() => onIdx(i)}
              style={{ width: 72, height: 56, padding: 0, flex: "0 0 auto", borderRadius: 10, border: i === idx ? "3px solid var(--primary)" : "1px solid var(--border-highlight)", background: "repeating-linear-gradient(135deg, var(--surface-container-high) 0 6px, var(--surface-container) 6px 12px)", cursor: "pointer", font: "600 13px var(--font-mono)", color: "var(--on-surface-variant)" }}>{i + 1}</button>))}
        </div>)}
    </div>
  );
}
function PhotoViewer({ count, idx, onIdx, onClose, label }) {
  const [z, setZ] = React.useState(1);
  React.useEffect(() => setZ(1), [idx]);
  React.useEffect(() => { const k = (e) => { if (e.key === "Escape") onClose(); if (e.key === "ArrowLeft" && idx > 0) onIdx(idx - 1); if (e.key === "ArrowRight" && idx < count - 1) onIdx(idx + 1); }; window.addEventListener("keydown", k); return () => window.removeEventListener("keydown", k); }, [idx]);
  const sw = useSwipe(() => z === 1 && onIdx(Math.max(0, idx - 1)), () => z === 1 && onIdx(Math.min(count - 1, idx + 1)));
  return (
    <div role="dialog" aria-label="Просмотр фото" style={{ position: "absolute", inset: 0, zIndex: 60, background: "#0b0d10", display: "flex", flexDirection: "column", color: "#fff" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 8px 8px 12px", flex: "0 0 auto" }}>
        <NavBtn icon="close" label="Закрыть" onClick={onClose} dark />
        <div style={{ flex: 1, minWidth: 0, font: "500 16px var(--font-sans)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{label}</div>
        <Counter i={idx} n={count} dark />
      </div>
      <div {...sw} onDoubleClick={() => setZ(z > 1 ? 1 : 2)} style={{ flex: 1, minHeight: 0, overflow: "hidden", display: "grid", placeItems: "center", touchAction: "none", position: "relative" }}>
        <div style={{ width: "min(100%, 960px)", transform: `scale(${z})`, transition: "transform .2s", padding: 12, boxSizing: "border-box" }}>
          <div style={{ aspectRatio: "4 / 3", borderRadius: 8, background: "repeating-linear-gradient(135deg, #22262c 0 12px, #1a1d22 12px 24px)", display: "grid", placeItems: "center", color: "#9aa4b8", font: "500 14px var(--font-mono)" }}>{label} · фото {idx + 1}</div>
        </div>
        {count > 1 && <div style={{ position: "absolute", left: 8, top: "50%", transform: "translateY(-50%)" }}><NavBtn icon="chevron_left" label="Предыдущее фото" dark disabled={idx === 0} onClick={() => onIdx(idx - 1)} /></div>}
        {count > 1 && <div style={{ position: "absolute", right: 8, top: "50%", transform: "translateY(-50%)" }}><NavBtn icon="chevron_right" label="Следующее фото" dark disabled={idx === count - 1} onClick={() => onIdx(idx + 1)} /></div>}
      </div>
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: 16, padding: 12, flex: "0 0 auto" }}>
        <NavBtn icon="zoom_out" label="Уменьшить" dark disabled={z <= 1} onClick={() => setZ(Math.max(1, z - 0.5))} />
        <span style={{ font: "600 15px var(--font-mono)", minWidth: 56, textAlign: "center" }}>{Math.round(z * 100)}%</span>
        <NavBtn icon="zoom_in" label="Увеличить" dark disabled={z >= 3} onClick={() => setZ(Math.min(3, z + 0.5))} />
      </div>
    </div>
  );
}

window.VsKit = { useVsWidth, busVar, BusChip, CapBadge, CAP, Ph, Logo, Hl, VsRow, SkelList, StateBox, Seg, CanInput, SearchBar, Crumbs, Html, Gallery, PhotoViewer, Counter };
