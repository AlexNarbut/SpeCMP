// Vehicle select — карточка автомобиля: шапка (варианты A/B), бейджи, «Важно», вкладки,
// матрица функций (варианты A — шины столбцами, B — «Чтение · Управление»), точки подключения с галереей,
// схемы / карты монтажа (PDF), народные карты. Экспорт — window.VsCard, window.VsCardSkeleton.
const VC_DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpText: VText, CmpIcon: VIcon, CmpIconButton: VIconBtn, CmpButton: VBtn, CmpTextButton: VTBtn, CmpTabRow: VTabs } = VC_DS;
const VC_K = window.VsKit;
const VC_D = window.VS_DATA;
const VC_WARN = "Заявленный функционал может отсутствовать или различаться для данной модели, в зависимости от года выпуска и комплектации";

function VcCan({ code, onCopy, big }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "6px 6px 6px 16px", borderRadius: 16, background: "var(--primary-container)", color: "var(--on-primary-container)", flex: "0 0 auto", alignSelf: "flex-start" }}>
      <div style={{ display: "flex", flexDirection: "column" }}>
        <span style={{ font: "600 13px var(--font-sans)", letterSpacing: ".04em", textTransform: "uppercase" }}>CAN-код</span>
        <span style={{ font: `700 ${big ? 36 : 32}px/1.1 var(--font-mono)`, letterSpacing: ".06em" }}>{code}</span>
      </div>
      <VIconBtn icon="content_copy" contentDescription={"Скопировать CAN-код " + code} onClick={onCopy} tint="currentColor" />
    </div>
  );
}

function VcNotes({ notes, open, onToggle }) {
  if (!notes.length) return null;
  return (
    <section aria-label="Важно" style={{ borderRadius: 16, padding: "12px 14px", background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", display: "flex", flexDirection: "column", gap: 8 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8 }}><VIcon icon="priority_high" size={22} color="currentColor" /><VText variant="titleMedium" color="currentColor">Важно</VText></div>
      <div style={{ display: "flex", flexDirection: "column", gap: 10, color: "var(--on-surface)" }}>
        {open ? notes.map((n, i) => <div key={i} style={{ borderTop: i ? "1px solid color-mix(in srgb, var(--on-tertiary-container) 20%, transparent)" : "none", paddingTop: i ? 10 : 0 }}><VC_K.Html html={n} /></div>)
          : <VC_K.Html html={notes[0]} clamp={3} />}
      </div>
      {(notes.length > 1 || notes[0].replace(/<[^>]+>/g, "").length > 140) && <VTBtn text={open ? "Свернуть" : notes.length > 1 ? `Показать полностью · ${notes.length} примечания` : "Показать полностью"} onClick={onToggle} style={{ height: 48, alignSelf: "flex-start", marginLeft: -8 }} />}
    </section>
  );
}

function VcHead({ c, layout, v, ui, setUi, imgFail, onNote, onCopy, onArchive }) {
  const wide = layout !== "compact";
  const title = <div style={{ minWidth: 0 }}>
    <h2 style={{ margin: 0, font: `600 ${wide ? 26 : 24}px/1.2 var(--font-sans)`, color: "var(--on-surface)", overflowWrap: "anywhere" }}>{c.brand} {c.model}</h2>
    <div style={{ marginTop: 4, font: "500 17px/1.35 var(--font-sans)", color: "var(--on-surface-variant)", overflowWrap: "anywhere" }}>{c.years} · {c.trim}</div>
  </div>;
  const caps = [["Блокировка по CAN", c.canLock], ["Бесключевой обход", c.keyless]];
  const archive = onArchive && <VTBtn text={`Прошивки для CAN ${c.can}`} icon="history" onClick={onArchive} style={{ height: 48, alignSelf: "flex-start", marginLeft: -8 }} />;
  const notes = <VcNotes notes={c.notes} open={ui.notesOpen} onToggle={() => setUi({ notesOpen: !ui.notesOpen })} />;
  if (v === "B") return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      <div style={{ display: "flex", gap: 14, alignItems: "center" }}>
        <div style={{ width: wide ? 200 : 120, flex: "0 0 auto" }}><VC_K.Ph label="фото модели" ratio="4 / 3" fail={imgFail} small={!wide} /></div>
        {title}
      </div>
      {!wide ? (
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "6px 6px 6px 16px", borderRadius: 14, boxSizing: "border-box", background: "var(--primary-container)", color: "var(--on-primary-container)" }}>
            <span style={{ font: "600 13px/1.2 var(--font-sans)", letterSpacing: ".04em", textTransform: "uppercase", flex: "0 1 auto", minWidth: 0 }}>CAN-код</span>
            <span style={{ flex: 1, font: "700 30px/1 var(--font-mono)", letterSpacing: ".06em" }}>{c.can}</span>
            <VIconBtn icon="content_copy" contentDescription={"Скопировать CAN-код " + c.can} onClick={onCopy} tint="currentColor" />
          </div>
          {caps.map(([l, x], i) => <VC_K.CapBadge key={i} label={l} v={x.v} note={x.note} onClick={() => onNote(l, x)} fill />)}
        </div>
      ) : (
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, minmax(0,1fr))", gap: 8 }}>
        <div style={{ position: "relative", minHeight: 112, borderRadius: 14, padding: "10px 12px", boxSizing: "border-box", background: "var(--primary-container)", color: "var(--on-primary-container)", display: "flex", flexDirection: "column", justifyContent: "space-between" }}>
          <span style={{ font: "600 13px/1.2 var(--font-sans)", letterSpacing: ".04em", textTransform: "uppercase", paddingRight: 44 }}>CAN-код</span>
          <span style={{ font: "700 34px/1 var(--font-mono)", letterSpacing: ".04em" }}>{c.can}</span>
          <div style={{ position: "absolute", top: 2, right: 2 }}><VIconBtn icon="content_copy" contentDescription={"Скопировать CAN-код " + c.can} onClick={onCopy} tint="currentColor" /></div>
        </div>
        {caps.map(([l, x], i) => <VC_K.CapBadge key={i} tile label={l} v={x.v} note={x.note} onClick={() => onNote(l, x)} />)}
      </div>)}
      {archive}{notes}
    </div>
  );
  return (
    <div style={{ display: "flex", flexDirection: wide ? "row" : "column", gap: wide ? 20 : 14, alignItems: "flex-start" }}>
      <div style={{ width: wide ? 280 : "100%", flex: "0 0 auto" }}><VC_K.Ph label="фото модели 16:9" fail={imgFail} /></div>
      <div style={{ display: "flex", flexDirection: "column", gap: 14, minWidth: 0, flex: 1, width: "100%" }}>
        {title}
        <div style={{ display: "flex", flexWrap: "wrap", gap: 10, alignItems: "center" }}><VcCan code={c.can} onCopy={onCopy} big={wide} />{archive}</div>
        <div style={{ display: "flex", flexDirection: layout === "compact" ? "column" : "row", flexWrap: "wrap", gap: 8 }}>
          {caps.map(([l, x], i) => <VC_K.CapBadge key={i} fill={layout !== "compact"} label={l} v={x.v} note={x.note} onClick={() => onNote(l, x)} />)}
        </div>
      </div>
    </div>
  );
}

// ── Матрица функций
const VC_R = "visibility", VC_C = "settings_remote";
function VcLegend({ v }) {
  const it = (icon, t) => <span style={{ display: "inline-flex", alignItems: "center", gap: 6, font: "500 15px var(--font-sans)", color: "var(--on-surface)" }}><VIcon icon={icon} size={22} color="var(--on-surface)" />{t}</span>;
  return v === "A"
    ? <div style={{ display: "flex", flexWrap: "wrap", gap: "6px 18px", alignItems: "center" }}>{it(VC_R, "чтение (статус)")}{it(VC_C, "управление (команда)")}</div>
    : <div style={{ font: "400 15px var(--font-sans)", color: "var(--on-surface-variant)" }}>Шины, по которым блок читает статус и отправляет команду</div>;
}
function VcGroupHead({ g, n, open, onToggle, span }) {
  return (
    <button type="button" onClick={onToggle} aria-expanded={open} style={{ gridColumn: span, display: "flex", alignItems: "center", gap: 10, minHeight: 52, padding: "0 8px 0 12px", border: "none", borderRadius: 12, marginTop: 10, background: "var(--surface-container-high)", color: "var(--on-surface)", cursor: "pointer", textAlign: "left", font: "inherit" }}>
      <span style={{ flex: 1, font: "600 16px/1.25 var(--font-sans)" }}>{g.t}</span>
      <span style={{ font: "600 14px var(--font-mono)", color: "var(--on-surface-variant)" }}>{n}</span>
      <VIcon icon={open ? "expand_less" : "expand_more"} size={26} color="var(--on-surface-variant)" />
    </button>
  );
}
function VcFuncName({ f }) {
  return <div style={{ display: "flex", flexDirection: "column", gap: 2, padding: "10px 8px 10px 12px", minWidth: 0 }}>
    <span style={{ font: "500 16px/1.3 var(--font-sans)", color: "var(--on-surface)", overflowWrap: "anywhere" }}>{f.n}</span>
    {f.d && <span style={{ font: "400 14px/1.3 var(--font-sans)", color: "var(--on-surface-variant)" }}>{f.d}</span>}
  </div>;
}
function VcMatrix({ funcs, v, layout, closed, onToggle }) {
  const groups = VC_D.GROUPS.map((g) => ({ g, fs: funcs.filter((f) => f.g === g.id).sort((a, b) => (a.o || 0) - (b.o || 0)) })).filter((x) => x.fs.length);
  const cellBd = { borderBottom: "1px solid var(--outline-variant)" };
  if (v === "A") {
    const used = VC_D.BUS_ORDER.filter((b) => funcs.some((f) => f.r.includes(b) || f.c.includes(b)));
    const cwOf = (b) => Math.max(layout === "compact" ? 58 : 76, Math.round(b.length * 8.2 + 40));
    const nameW = layout === "compact" ? 150 : 220;
    const cols = `minmax(${nameW}px, 1fr) ${used.map((b) => cwOf(b) + "px").join(" ")}`;
    const minW = nameW + used.reduce((s, b) => s + cwOf(b), 0);
    return (
      <div className="vs-scroll" style={{ overflowX: "auto", margin: "0 -4px", padding: "0 4px" }}>
        <div role="table" aria-label="Функции по шинам" style={{ display: "grid", gridTemplateColumns: cols, minWidth: minW }}>
          <div role="row" style={{ display: "contents" }}>
            <div role="columnheader" style={{ position: "sticky", left: 0, background: "var(--background)", zIndex: 1, padding: "8px 12px", font: "600 14px var(--font-sans)", color: "var(--on-surface-variant)", alignSelf: "end", ...cellBd }}>Функция</div>
            {used.map((b) => <div key={b} role="columnheader" style={{ display: "flex", justifyContent: "center", alignItems: "flex-end", padding: "8px 2px", ...cellBd }}><VC_K.BusChip bus={b} size="s" /></div>)}
          </div>
          {groups.map(({ g, fs }) => (
            <React.Fragment key={g.id}>
              <VcGroupHead g={g} n={fs.length} open={!closed[g.id]} onToggle={() => onToggle(g.id)} span="1 / -1" />
              {!closed[g.id] && fs.map((f, i) => (
                <div key={i} role="row" style={{ display: "contents" }}>
                  <div role="cell" style={{ position: "sticky", left: 0, background: "var(--background)", zIndex: 1, ...cellBd }}><VcFuncName f={f} /></div>
                  {used.map((b) => {
                    const r = f.r.includes(b), c = f.c.includes(b), col = VC_K.busVar(b);
                    return <div key={b} role="cell" aria-label={`${b}: ${r && c ? "чтение и управление" : r ? "чтение" : c ? "управление" : "нет"}`}
                      style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 2, background: r || c ? `color-mix(in srgb, ${col} 10%, transparent)` : "transparent", ...cellBd }}>
                      {r && <VIcon icon={VC_R} size={22} color={col} />}{c && <VIcon icon={VC_C} size={22} color={col} />}
                      {!r && !c && <span aria-hidden="true" style={{ width: 4, height: 4, borderRadius: 2, background: "var(--outline)" }} />}
                    </div>;
                  })}
                </div>))}
            </React.Fragment>))}
        </div>
      </div>
    );
  }
  const chips = (arr, pad = "10px 4px") => arr.length ? <div style={{ display: "flex", flexWrap: "wrap", gap: 4, padding: pad, minWidth: 0 }}>{arr.map((b) => <VC_K.BusChip key={b} bus={b} size="s" />)}</div>
    : <span aria-label="нет" style={{ padding: pad, color: "var(--on-surface-variant)", font: "500 16px var(--font-sans)" }}>—</span>;
  if (layout === "compact") {
    const line = (icon, t, arr) => (
      <div style={{ display: "grid", gridTemplateColumns: "128px minmax(0,1fr)", alignItems: "start", gap: 4 }}>
        <span style={{ display: "inline-flex", alignItems: "center", gap: 6, minHeight: 30, font: "500 14px var(--font-sans)", color: "var(--on-surface-variant)" }}><VIcon icon={icon} size={18} color="currentColor" />{t}</span>
        {chips(arr, "0")}
      </div>);
    return (
      <div role="list" aria-label="Функции: чтение и управление" style={{ display: "flex", flexDirection: "column" }}>
        {groups.map(({ g, fs }) => (
          <React.Fragment key={g.id}>
            <VcGroupHead g={g} n={fs.length} open={!closed[g.id]} onToggle={() => onToggle(g.id)} />
            {!closed[g.id] && fs.map((f, i) => (
              <div key={i} role="listitem" style={{ ...cellBd, padding: "0 0 12px", display: "flex", flexDirection: "column", gap: 6 }}>
                <VcFuncName f={f} />
                <div style={{ display: "flex", flexDirection: "column", gap: 6, padding: "0 4px 0 12px" }}>
                  {line(VC_R, "Чтение", f.r)}{line(VC_C, "Управление", f.c)}
                </div>
              </div>))}
          </React.Fragment>))}
      </div>);
  }
  const cols = "minmax(0,1.3fr) minmax(0,1fr) minmax(0,1fr)";
  return (
    <div role="table" aria-label="Функции: чтение и управление" style={{ display: "grid", gridTemplateColumns: cols }}>
      <div role="row" style={{ display: "contents" }}>
        {[["Функция", ""], ["Чтение", VC_R], ["Управление", VC_C]].map(([t, ic]) => <div key={t} role="columnheader" style={{ display: "flex", alignItems: "center", gap: 4, padding: "8px 4px 8px " + (ic ? "4px" : "12px"), font: "600 14px var(--font-sans)", color: "var(--on-surface-variant)", ...cellBd }}>{ic && <VIcon icon={ic} size={18} color="currentColor" />}{t}</div>)}
      </div>
      {groups.map(({ g, fs }) => (
        <React.Fragment key={g.id}>
          <VcGroupHead g={g} n={fs.length} open={!closed[g.id]} onToggle={() => onToggle(g.id)} span="1 / -1" />
          {!closed[g.id] && fs.map((f, i) => (
            <div key={i} role="row" style={{ display: "contents" }}>
              <div role="cell" style={cellBd}><VcFuncName f={f} /></div>
              <div role="cell" style={{ ...cellBd, display: "flex", alignItems: "center" }}>{chips(f.r)}</div>
              <div role="cell" style={{ ...cellBd, display: "flex", alignItems: "center" }}>{chips(f.c)}</div>
            </div>))}
        </React.Fragment>))}
    </div>
  );
}

function VcTile({ icon, tone }) {
  return <div style={{ width: 48, height: 48, borderRadius: 12, flex: "0 0 auto", display: "grid", placeItems: "center", background: tone === "pdf" ? "var(--error-container)" : "var(--secondary-container)", color: tone === "pdf" ? "var(--on-error-container)" : "var(--on-secondary-container)" }}><VIcon icon={icon} size={26} color="currentColor" /></div>;
}

function VcPoint({ p, ui, setUi, onViewer }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      <VTBtn text="Все точки подключения" icon="arrow_back" onClick={() => setUi({ point: null, photo: 0 })} style={{ height: 48, alignSelf: "flex-start", marginLeft: -8 }} />
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}><VcTile icon={p.icon} /><div style={{ minWidth: 0 }}>
        <div style={{ font: "600 13px var(--font-sans)", letterSpacing: ".05em", textTransform: "uppercase", color: "var(--primary)" }}>{p.type}</div>
        <div style={{ font: "600 19px/1.3 var(--font-sans)", color: "var(--on-surface)", overflowWrap: "anywhere" }}>{p.name}</div></div></div>
      <VC_K.Gallery count={p.photos} idx={ui.photo} onIdx={(i) => setUi({ photo: i })} onFull={() => onViewer(p)} label={p.type} />
      {p.desc && <VText variant="bodyLarge">{p.desc}</VText>}
    </div>
  );
}

function VsCard({ c, layout, ui, setUi, headVar = "A", mtxVar = "A", fgroups = [], imgFail, selectable, onSelect, onNote, onCopy, onArchive, onViewer, onPdf, onFolk, pad = 16 }) {
  const funcs = fgroups.length ? c.funcs.filter((f) => fgroups.includes(f.g)) : c.funcs;
  const tabs = [
    c.funcs.length && { k: "funcs", t: "Функции" },
    c.points.length && { k: "points", t: "Точки подключения" },
    c.schemes.length && { k: "schemes", t: "Схемы" },
    c.maps.length && { k: "maps", t: "Карты монтажа" },
    c.folk.length && { k: "folk", t: "Народные карты" },
  ].filter(Boolean);
  const ti = Math.max(0, tabs.findIndex((t) => t.k === ui.tab));
  const tab = tabs[ti] && tabs[ti].k;
  const closed = ui.closed || {};
  let body = null;
  if (tab === "funcs") body = (
    <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
      <VcLegend v={mtxVar} />
      {fgroups.length > 0 && <div style={{ font: "500 15px var(--font-sans)", color: "var(--primary)", display: "flex", alignItems: "center", gap: 6 }}><VIcon icon="filter_alt" size={18} color="currentColor" />Показаны группы из фильтра: {funcs.length} из {c.funcs.length}</div>}
      {funcs.length ? <VcMatrix funcs={funcs} v={mtxVar} layout={layout} closed={closed} onToggle={(g) => setUi({ closed: { ...closed, [g]: !closed[g] } })} />
        : <VC_K.StateBox kind="filter" compact text="В этой комплектации нет функций из выбранных групп." />}
      <div role="note" style={{ display: "flex", gap: 10, alignItems: "flex-start", marginTop: 8, padding: "12px 14px", borderRadius: 14, background: "var(--surface-container-high)", color: "var(--on-surface)" }}>
        <VIcon icon="warning" size={22} color="var(--on-surface-variant)" /><VText variant="bodyMedium">{VC_WARN}</VText>
      </div>
    </div>);
  else if (tab === "points") body = ui.point != null && c.points[ui.point]
    ? <VcPoint p={c.points[ui.point]} ui={ui} setUi={setUi} onViewer={onViewer} />
    : <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>{c.points.map((p, i) => <VC_K.VsRow key={i} lead={<VcTile icon={p.icon} />} title={p.name} sub={`${p.type} · ${p.photos ? p.photos + " фото" : "без фото"}`} onClick={() => setUi({ point: i, photo: 0 })} />)}</div>;
  else if (tab === "schemes" || tab === "maps") body = <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>{c[tab].map((d, i) => <VC_K.VsRow key={i} chevron={false} lead={<VcTile icon="picture_as_pdf" tone="pdf" />} title={d.t} sub={d.d} trail={<VIcon icon="open_in_new" size={24} color="var(--primary)" />} onClick={() => onPdf(d)} />)}</div>;
  else if (tab === "folk") body = <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>{c.folk.map((d, i) => <VC_K.VsRow key={i} lead={<VcTile icon="groups" />} title={d.t} sub={<span style={{ display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden" }}>{d.d}</span>} onClick={() => onFolk(d)} />)}</div>;

  return (
    <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ padding: `${pad}px ${pad}px 0` }}>
          <VcHead c={c} layout={layout} v={headVar} ui={ui} setUi={setUi} imgFail={imgFail} onNote={onNote} onCopy={onCopy} onArchive={onArchive} />
        </div>
        {tabs.length ? (
          <React.Fragment>
            <div style={{ position: "sticky", top: 0, zIndex: 3, background: "var(--background)", marginTop: 12, borderBottom: "1px solid var(--outline-variant)" }}>
              <VTabs scrollable tabs={tabs.map((t) => ({ title: t.t }))} selectedTabIndex={ti} onTabSelected={(i) => setUi({ tab: tabs[i].k, point: null, photo: 0 })} />
            </div>
            <div style={{ padding: `14px ${pad}px ${pad + 8}px` }}>{body}</div>
          </React.Fragment>
        ) : (
          <div style={{ padding: pad }}>
            <div role="status" style={{ display: "flex", gap: 12, padding: "14px 16px", borderRadius: 14, background: "var(--surface-container-high)" }}>
              <VIcon icon="info" size={24} color="var(--on-surface-variant)" />
              <VText variant="bodyLarge">Подробных данных по этой комплектации пока нет: функции, точки подключения и схемы не добавлены.</VText>
            </div>
          </div>
        )}
      </div>
      {selectable && (
        <div style={{ flex: "0 0 auto", padding: `12px ${pad}px`, borderTop: "1px solid var(--outline-variant)", background: "var(--surface-container-low)" }}>
          <VBtn text={`Выбрать · ${c.brand} ${c.model} ${c.trim}`} icon="check" fullWidth onClick={onSelect} style={{ height: 56, fontSize: 17 }} />
        </div>)}
    </div>
  );
}

function VsCardSkeleton({ layout }) {
  const bar = (w, h) => <div style={{ width: w, height: h, borderRadius: 8, background: "var(--surface-container-highest)" }} />;
  const wide = layout !== "compact";
  return (
    <div aria-busy="true" aria-label="Загрузка карточки" className="vs-anim" style={{ padding: 16, display: "flex", flexDirection: wide ? "row" : "column", gap: 16, animation: "vsShim 1.4s ease-in-out infinite" }}>
      <div style={{ width: wide ? 280 : "100%", aspectRatio: "16 / 9", borderRadius: 14, background: "var(--surface-container-highest)", flex: "0 0 auto" }} />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: 12 }}>{bar("70%", 26)}{bar("45%", 18)}{bar(150, 64)}{bar("100%", 56)}{bar("100%", 56)}</div>
    </div>
  );
}

Object.assign(window, { VsCard, VsCardSkeleton });
