// Vehicle select — экран выбора автомобиля из каталога (ТЗ vehicle-select-design-brief.md).
// Режимы: просмотр / выбор; каталоги: все / с конфигурациями; точки входа A–E.
// Адаптив по ширине контейнера (M3 WSC): compact <600 — по шагам; medium 600–839 — две колонки
// (марки · модели с раскрытием комплектаций) + карточка на весь экран; expanded ≥840 — колонки + карточка справа,
// третья колонка «Комплектации» — при ширине ≥1400, иначе комплектации раскрываются в колонке моделей.
// Выбор (марка/модель/комплектация/вкладка) хранится в контейнере → не теряется при смене ширины.
const VS_DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar: STop, CmpIconButton: SIconBtn, CmpButton: SBtn, CmpOutlinedButton: SOBtn, CmpTextButton: STBtn, CmpText: SText, CmpIcon: SIcon,
  CmpDialog: SDialog, CmpBottomSheet: SSheet, CmpCheckbox: SCheck, CmpSnackbar: SSnack } = VS_DS;
const VK = window.VsKit;
const VD = window.VS_DATA;

const VS_ENTRY = {
  A: { mode: "view", cat: "all", needType: true, chip: true, sub: null },
  B: { mode: "view", cat: "all", sub: `${VD.BLOCK.name} · CAN-код прошивки ${VD.BLOCK.can}`, prefill: VD.BLOCK.can },
  C: { mode: "select", cat: "all", sub: `${VD.BLOCK.name} · настройка «CAN-код автомобиля»`, mismatch: true, result: (c) => `CAN-код ${c.can} и ${c.brand} ${c.model} подставлены в настройку` },
  D: { mode: "select", cat: "all", needType: true, chip: true, sub: "Фильтр архива прошивок", result: (c) => `CAN-код ${c.can} подставлен в фильтр архива` },
  E: { mode: "select", cat: "configs", noFilter: true, noCan: true, sub: `Конфигурации для ${VD.BLOCK.name}`, result: (c) => `${c.brand} ${c.model} ${c.trim}: открываем список конфигураций` },
};
const VS_TYPES = [{ id: "alarm", t: "Базовый блок сигнализации", chip: "Сигнализация", icon: "security" }, { id: "microbot", t: "Микробот", chip: "Микробот", icon: "memory" }];
const vsLow = (s) => (s || "").toLowerCase().replace(/ё/g, "е").trim();
const vsHas = (name, q) => vsLow(name).includes(vsLow(q));

function VsTypeScreen({ onPick, wide }) {
  return (
    <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto", display: "flex", justifyContent: "center", padding: wide ? 40 : "24px 16px", boxSizing: "border-box" }}>
      <div style={{ width: "100%", maxWidth: 760, display: "flex", flexDirection: "column", gap: 20 }}>
        <div><SText variant="headlineSmall">Для какого устройства подобрать автомобиль?</SText>
          <div style={{ marginTop: 8 }}><SText variant="bodyLarge" color="var(--on-surface-variant)">Устройство не подключено. Каталог покажет автомобили, поддерживаемые выбранным типом. Тип можно сменить позже в шапке каталога.</SText></div></div>
        <div style={{ display: "grid", gridTemplateColumns: wide ? "1fr 1fr" : "1fr", gap: 14 }}>
          {VS_TYPES.map((t) => <VsTypeCard key={t.id} t={t} onClick={() => onPick(t.id)} wide={wide} />)}
        </div>
      </div>
    </div>
  );
}
function VsTypeCard({ t, onClick, wide }) {
  const [hov, setHov] = React.useState(false);
  return (
    <button type="button" onClick={onClick} onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}
      style={{ display: "flex", flexDirection: wide ? "column" : "row", alignItems: wide ? "flex-start" : "center", gap: 16, minHeight: wide ? 200 : 104, padding: 20, borderRadius: 24, border: "1px solid var(--outline-variant)", cursor: "pointer", textAlign: "left", font: "inherit", color: "var(--on-surface)",
        background: hov ? "color-mix(in srgb, var(--primary) 8%, var(--surface-container-low))" : "var(--surface-container-low)" }}>
      <div style={{ width: 72, height: 72, borderRadius: 20, display: "grid", placeItems: "center", background: "var(--primary-container)", color: "var(--on-primary-container)", flex: "0 0 auto" }}><SIcon icon={t.icon} size={40} color="currentColor" /></div>
      <span style={{ flex: 1, font: "600 20px/1.3 var(--font-sans)" }}>{t.t}</span>
      {!wide && <SIcon icon="chevron_right" size={28} color="var(--on-surface-variant)" />}
    </button>
  );
}

function VsTypeChip({ type, onPick }) {
  const [open, setOpen] = React.useState(false);
  const ref = React.useRef(null);
  React.useEffect(() => { if (!open) return; const c = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); }; document.addEventListener("mousedown", c); return () => document.removeEventListener("mousedown", c); }, [open]);
  const cur = VS_TYPES.find((t) => t.id === type);
  return (
    <div ref={ref} style={{ position: "relative" }}>
      <button type="button" aria-haspopup="listbox" aria-expanded={open} onClick={() => setOpen(!open)} aria-label={"Тип устройства: " + cur.t}
        style={{ height: 48, padding: "0 8px 0 14px", borderRadius: 12, border: "1px solid var(--outline)", background: "transparent", color: "var(--on-surface)", display: "inline-flex", alignItems: "center", gap: 4, font: "600 15px var(--font-sans)", cursor: "pointer", whiteSpace: "nowrap" }}>
        {cur.chip}<SIcon icon={open ? "arrow_drop_up" : "arrow_drop_down"} size={24} color="currentColor" />
      </button>
      {open && (
        <div role="listbox" style={{ position: "absolute", right: 0, top: "calc(100% + 4px)", zIndex: 30, minWidth: 280, padding: "8px 0", borderRadius: 12, background: "var(--surface-container)", boxShadow: "var(--elevation-2)" }}>
          {VS_TYPES.map((t) => (
            <button key={t.id} type="button" role="option" aria-selected={t.id === type} onClick={() => { setOpen(false); t.id !== type && onPick(t.id); }}
              style={{ display: "flex", alignItems: "center", gap: 12, width: "100%", minHeight: 56, padding: "0 16px", border: "none", background: t.id === type ? "var(--secondary-container)" : "transparent", color: "var(--on-surface)", font: "500 16px var(--font-sans)", cursor: "pointer", textAlign: "left" }}>
              <SIcon icon={t.icon} size={24} color="var(--primary)" /><span style={{ flex: 1 }}>{t.t}</span>{t.id === type && <SIcon icon="check" size={22} color="var(--primary)" />}
            </button>))}
        </div>)}
    </div>
  );
}

function VsFilterBody({ draft, setDraft }) {
  return (
    <div style={{ display: "flex", flexDirection: "column" }}>
      {VD.GROUPS.map((g) => {
        const on = draft.includes(g.id);
        const tog = () => setDraft(on ? draft.filter((x) => x !== g.id) : [...draft, g.id]);
        return (
          <div key={g.id} onClick={tog} role="checkbox" aria-checked={on} tabIndex={0} onKeyDown={(e) => { if (e.key === " " || e.key === "Enter") { e.preventDefault(); tog(); } }}
            style={{ display: "flex", alignItems: "center", gap: 8, minHeight: 56, padding: "0 8px", borderRadius: 12, cursor: "pointer" }}>
            <span onClick={(e) => e.stopPropagation()}><SCheck checked={on} onCheckedChange={tog} /></span>
            <span style={{ font: "500 17px/1.3 var(--font-sans)", color: "var(--on-surface)" }}>{g.t}</span>
          </div>);
      })}
    </div>
  );
}

function VsCol({ title, w, children, border = true, grow }) {
  return (
    <section aria-label={title} style={{ flex: grow ? "1 1 0" : `0 0 ${w}px`, minWidth: 0, display: "flex", flexDirection: "column", borderRight: border ? "1px solid var(--outline-variant)" : "none" }}>
      <div style={{ padding: "12px 16px 4px", font: "600 14px var(--font-sans)", letterSpacing: ".05em", textTransform: "uppercase", color: "var(--on-surface-variant)", flex: "0 0 auto" }}>{title}</div>
      <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto", padding: "0 8px 16px" }}>{children}</div>
    </section>
  );
}

function VehicleSelectApp(props) {
  const { entry = "A", net = "ok", netLevel = "all", catVar = "A", headVar = "B", mtxVar = "B", init = {}, onBack = () => {} } = props;
  const E = props.sub ? { ...VS_ENTRY[entry], sub: props.sub } : VS_ENTRY[entry];
  const [rootRef, layout, w] = VK.useVsWidth();
  const [type, setType] = React.useState(init.type !== undefined ? init.type : E.needType ? null : "alarm");
  const [sel, setSel] = React.useState(() => {
    if (init.trim) { const md = VD.MODEL_BY_ID[VD.TRIM_BY_ID[init.trim].model]; return { brand: md.brand, model: md.id, trim: init.trim, src: init.src || "cat" }; }
    return { brand: init.brand || null, model: init.model || null, trim: null, src: init.src || "cat" };
  });
  const [q, setQ] = React.useState(init.q || "");
  const [smode, setSmode] = React.useState(init.smode || "name");
  const [can, setCan] = React.useState(init.can != null ? init.can : E.prefill || "");
  const [fg, setFg] = React.useState(init.fgroups || []);
  const [draft, setDraft] = React.useState(init.fgroups || []);
  const [sheet, setSheet] = React.useState(init.sheet || null);
  const [note, setNote] = React.useState(null);
  const [dialog, setDialog] = React.useState(null);
  const [snack, setSnack] = React.useState(null);
  const [viewer, setViewer] = React.useState(null);
  const [ui, setUiRaw] = React.useState({ tab: init.tab || "funcs", point: init.point != null ? init.point : null, photo: 0, notesOpen: false, closed: {} });
  const setUi = (p) => setUiRaw((u) => ({ ...u, ...p }));
  const [recent, setRecent] = React.useState(VD.RECENT);
  const [loads, setLoads] = React.useState({});
  const mounted = React.useRef(false);
  const autoOpened = React.useRef(null);
  const timers = React.useRef([]);
  React.useEffect(() => () => timers.current.forEach(clearTimeout), []);
  React.useEffect(() => { if (!snack) return; const t = setTimeout(() => setSnack(null), 3800); return () => clearTimeout(t); }, [snack]);

  const wide = layout !== "compact";
  const canAvail = !E.noCan;
  const fActive = fg.length > 0;
  const passM = (m) => !fActive || m.groups.some((g) => fg.includes(g));
  const brands = type ? VD.brands({ catalog: E.cat, type }).filter((b) => !fActive || VD.MODELS[b.id].some(passM)) : [];
  const models = sel.brand ? VD.MODELS[sel.brand].filter(passM) : [];
  const trims = sel.model ? VD.TRIMS[sel.model] : [];
  const B = sel.brand && VD.BRAND_BY_ID[sel.brand], M = sel.model && VD.MODEL_BY_ID[sel.model];
  const card = sel.trim ? VD.card(sel.trim) : null;

  // Поиск «марка модель» на уровне марок
  const combo = React.useMemo(() => {
    const lq = vsLow(q);
    if (!lq || sel.brand) return null;
    const all = type ? VD.brands({ catalog: E.cat, type }) : [];
    const hit = all.filter((b) => lq.startsWith(vsLow(b.name) + " ")).sort((a, b) => b.name.length - a.name.length)[0];
    if (hit) return { b: hit, rest: lq.slice(vsLow(hit.name).length + 1).trim() };
    const toks = lq.split(/\s+/);
    if (toks.length < 2) return null;
    const pre = all.filter((b) => vsLow(b.name).startsWith(toks[0]));
    return pre.length === 1 ? { b: pre[0], rest: toks.slice(1).join(" ") } : null;
  }, [q, sel.brand, type, E.cat]);

  // ── Загрузка уровней (сервер отдаёт уровни целиком)
  const lvKey = {
    brands: type && `b:${type}:${E.cat}`, models: sel.brand && `m:${sel.brand}`, trims: sel.model && `t:${sel.model}`, card: sel.trim && `c:${sel.trim}`,
    can: smode === "can" && can.length === 4 && `can:${can}`, combo: combo && `m:${combo.b.id}`,
  };
  const levelOf = (k) => k.startsWith("b:") ? "brands" : k.startsWith("m:") ? "models" : k.startsWith("t:") ? "trims" : k.startsWith("c:") ? "card" : "can";
  const finish = (k, ms) => { timers.current.push(setTimeout(() => setLoads((l) => ({ ...l, [k]: "ok" })), ms)); };
  React.useEffect(() => {
    const first = !mounted.current; mounted.current = true;
    const add = {};
    Object.values(lvKey).forEach((k) => {
      if (!k || loads[k] || add[k]) return;
      const targeted = net !== "ok" && (netLevel === "all" || netLevel === levelOf(k));
      if (targeted) add[k] = net;
      else if (first) add[k] = "ok";
      else { add[k] = "loading"; finish(k, levelOf(k) === "can" ? 900 : 650); }
    });
    if (Object.keys(add).length) setLoads((l) => ({ ...l, ...add }));
  });
  const st = (k) => (k ? loads[k] || "loading" : "ok");
  const retry = (k) => { setLoads((l) => ({ ...l, [k]: "loading" })); finish(k, 800); };

  const canRes = lvKey.can && st(lvKey.can) === "ok" ? VD.byCan(can) : [];
  React.useEffect(() => {
    if (lvKey.can && st(lvKey.can) === "ok" && canRes.length === 1 && autoOpened.current !== can) { autoOpened.current = can; openCard(canRes[0].id, "can"); }
  }, [lvKey.can, loads[lvKey.can]]);

  // ── Навигация
  const pushRecent = (id) => setRecent((r) => [id, ...r.filter((x) => x !== id)].slice(0, 5));
  const openCard = (trimId, src) => {
    const tr = VD.TRIM_BY_ID[trimId], md = VD.MODEL_BY_ID[tr.model];
    setSel({ brand: md.brand, model: md.id, trim: trimId, src });
    setUiRaw({ tab: "funcs", point: null, photo: 0, notesOpen: false, closed: {} });
    pushRecent(trimId);
  };
  const pickBrand = (id) => { setSel({ brand: id, model: null, trim: null, src: "cat" }); setQ(""); };
  const pickModel = (id, brandId) => {
    const ts = VD.TRIMS[id], b = brandId || sel.brand;
    if (ts.length === 1) { openCard(ts[0].id, "cat"); setQ(""); return; }
    setSel({ brand: b, model: sel.model === id && wide && !sel.trim ? null : id, trim: null, src: "cat" }); setQ("");
  };
  const toTop = () => { setSel({ brand: null, model: null, trim: null, src: "cat" }); setQ(""); };
  const back = () => {
    if (viewer) return setViewer(null);
    if (sel.trim && ui.point != null && ui.tab === "points") return setUi({ point: null });
    if (sel.trim && (layout !== "expanded")) {
      if (sel.src === "can") return setSel({ brand: null, model: null, trim: null, src: "cat" });
      if (sel.src === "recent") return toTop();
      const single = VD.TRIMS[sel.model].length === 1;
      return setSel({ ...sel, trim: null, model: single && !wide ? null : sel.model });
    }
    if (layout === "compact" && smode === "name") {
      if (sel.model) return setSel({ ...sel, model: null });
      if (sel.brand) return setSel({ ...sel, brand: null });
    }
    if (E.needType && type && !init.lockType) return setType(null);
    onBack();
  };
  const doSelect = (c) => {
    if (E.mismatch && c.can !== VD.BLOCK.can) return setDialog({ c });
    finishSelect(c);
  };
  const finishSelect = (c) => { setDialog(null); pushRecent(c.id); if (props.onPicked) return props.onPicked(c); setSnack(E.result(c)); };
  const setMode = (m) => { setSmode(m); setSel({ brand: null, model: null, trim: null, src: "cat" }); autoOpened.current = null; };
  const changeType = (t) => { setType(t); toTop(); };
  const copy = (c) => { try { navigator.clipboard && navigator.clipboard.writeText(c.can); } catch (e) {} setSnack(`CAN-код ${c.can} скопирован`); };

  const title = E.mode === "select" ? "Выберите автомобиль" : "Автомобили";

  // ── Куски UI
  const stateFor = (k, retryable = true, n = 6, thumb) => {
    const s = st(k);
    if (s === "loading") return <VK.SkelList n={n} thumb={thumb} />;
    if (s === "error") return <VK.StateBox kind="error" compact={wide} onRetry={() => retry(k)} />;
    if (s === "offline") return <VK.StateBox kind="offline" compact={wide} onRetry={() => retry(k)} />;
    return null;
  };
  const letter = (t) => <div key={"L" + t} style={{ padding: "14px 12px 4px", font: "700 15px var(--font-sans)", color: "var(--primary)" }}>{t}</div>;
  const withLetters = (list, render) => {
    const out = []; let last = null;
    list.forEach((b) => { const L = b.name[0].toUpperCase(); if (L !== last && !q) { out.push(letter(L)); last = L; } out.push(render(b)); });
    return out;
  };
  const brandList = (selId) => {
    const s = stateFor(lvKey.brands); if (s) return s;
    if (combo) {
      const ms = VD.MODELS[combo.b.id].filter(passM).filter((m) => !combo.rest || vsHas(m.name, combo.rest));
      const sm = stateFor(lvKey.combo, true, 3, true);
      return (
        <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <VK.VsRow lead={<VK.Logo name={combo.b.name} />} title={<VK.Hl text={combo.b.name} q={q} />} sub="Все модели марки" onClick={() => pickBrand(combo.b.id)} />
          <div style={{ padding: "10px 12px 2px", font: "600 14px var(--font-sans)", color: "var(--on-surface-variant)" }}>Модели {combo.b.name}{combo.rest ? ` по запросу «${combo.rest}»` : ""}</div>
          {sm || (ms.length ? ms.map((m) => modelRow(m, combo.rest, false, combo.b.id)) : <VK.StateBox kind="nothing" compact text={`У ${combo.b.name} нет моделей «${combo.rest}». Проверьте написание или найдите по CAN-коду.`} />)}
        </div>);
    }
    const list = brands.filter((b) => !q || vsHas(b.name, q));
    if (!list.length) {
      if (fActive && !q) return <VK.StateBox kind="filter" compact={wide} onReset={() => { setFg([]); setDraft([]); }} />;
      if (/^\d+$/.test(q.trim()) && canAvail) return <VK.StateBox kind="nothing" compact={wide} title={`«${q.trim()}» похоже на CAN-код`} text="CAN-код ищется в отдельном режиме." action={<SBtn text="Искать по CAN-коду" icon="pin" onClick={() => { setMode("can"); setCan(q.trim().slice(0, 4)); setQ(""); }} style={{ height: 52 }} />} />;
      return <VK.StateBox kind="nothing" compact={wide} text={canAvail ? "Проверьте написание или найдите по CAN-коду." : "Проверьте написание."} action={canAvail && <SOBtn text="Искать по CAN-коду" icon="pin" onClick={() => { setMode("can"); setQ(""); }} style={{ height: 52 }} />} />;
    }
    if (catVar === "B" && !wide) {
      const groups = []; list.forEach((b) => { const L = q ? "" : b.name[0].toUpperCase(); const g = groups[groups.length - 1]; if (g && g.L === L) g.items.push(b); else groups.push({ L, items: [b] }); });
      return groups.map((g, gi) => (
        <React.Fragment key={gi}>{g.L && letter(g.L)}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, minmax(0,1fr))", gap: 8, padding: "0 4px" }}>
            {g.items.map((b) => <VsBrandTile key={b.id} b={b} q={q} onClick={() => pickBrand(b.id)} />)}
          </div></React.Fragment>));
    }
    return withLetters(list, (b) => <VK.VsRow key={b.id} dense lead={<VK.Logo name={b.name} size={40} />} title={<VK.Hl text={b.name} q={q} />} selected={selId === b.id} onClick={() => pickBrand(b.id)} />);
  };
  const trimRows = (m, inline) => {
    const ts = VD.TRIMS[m.id];
    const k = `t:${m.id}`;
    const s = stateFor(k, true, Math.max(1, Math.min(3, ts.length))); if (s) return s;
    if (!ts.length) return <VK.StateBox kind="empty" compact title="Нет комплектаций" text={`Для ${m.name} нет комплектаций под ${type === "microbot" ? "микробот" : "базовый блок сигнализации"}.`} />;
    const list = ts.filter((t) => inline || !sel.model || !q || vsHas(t.name, q));
    if (!list.length) return <VK.StateBox kind="nothing" compact text="Проверьте написание комплектации." />;
    return list.map((t) => (
      <VK.VsRow key={t.id} dense title={t.name} selected={sel.trim === t.id} onClick={() => openCard(t.id, "cat")} lead={inline ? <span style={{ width: 4 }} /> : null}
        action={E.mode === "select" && <SOBtn text="Выбрать" icon="check" onClick={() => doSelect(VD.card(t.id))} style={{ height: 48 }} />} />));
  };
  const modelRow = (m, hq, accordion, brandId) => {
    const ts = VD.TRIMS[m.id], open = accordion && sel.model === m.id;
    return (
      <VK.VsRow key={m.id} lead={<VK.Ph label="" small h={56} w={84} radius={10} />} title={<VK.Hl text={m.name} q={hq} />}
        sub={`${m.years}${ts.length > 1 ? ` · ${ts.length} комплектации` : ""}`} selected={sel.model === m.id && (!accordion || !open)} chevron={!accordion || ts.length <= 1}
        trail={accordion && ts.length > 1 && <SIcon icon={open ? "expand_less" : "expand_more"} size={26} color="var(--on-surface-variant)" />}
        onClick={() => pickModel(m.id, brandId)}>
        {open && <div style={{ padding: "0 0 8px 8px", borderLeft: "3px solid var(--primary)", marginLeft: 16 }}>{trimRows(m, true)}</div>}
      </VK.VsRow>);
  };
  const modelList = (accordion) => {
    const s = stateFor(lvKey.models, true, 5, true); if (s) return s;
    if (!models.length) return fActive ? <VK.StateBox kind="filter" compact={wide} onReset={() => { setFg([]); setDraft([]); }} /> : <VK.StateBox kind="empty" compact title="Нет моделей" />;
    const hq = wide || !sel.model ? q : "";
    const list = models.filter((m) => !hq || vsHas(m.name, hq));
    if (!list.length) return <VK.StateBox kind="nothing" compact={wide} text={`У ${B.name} нет моделей по запросу «${q}».`} />;
    return list.map((m) => modelRow(m, hq, accordion));
  };
  const canList = () => {
    if (can.length < 4) return (
      <div style={{ padding: "8px 4px", display: "flex", flexDirection: "column", gap: 8 }}>
        <SText variant="bodyLarge" color="var(--on-surface-variant)">Введите 4 цифры CAN-кода — покажем все комплектации с этим кодом.</SText>
      </div>);
    const s = stateFor(lvKey.can, true, 3, true); if (s) return s;
    if (!canRes.length) return <VK.StateBox kind="nothing" compact={wide} title={`Автомобили с CAN-кодом ${can} не найдены`} text="Проверьте код или найдите автомобиль по названию." action={<SOBtn text="Искать по названию" icon="search" onClick={() => setMode("name")} style={{ height: 52 }} />} />;
    return (
      <div style={{ display: "flex", flexDirection: "column", gap: 2 }}>
        <div style={{ padding: "6px 12px", font: "600 15px var(--font-sans)", color: "var(--on-surface-variant)" }}>Найдено: {canRes.length}</div>
        {canRes.map((c) => (
          <VK.VsRow key={c.id} selected={sel.trim === c.id} lead={<VK.Ph label="" small h={48} w={64} radius={10} />} title={`${c.brand} ${c.model}`} sub={`${c.years} · ${c.trim}`} onClick={() => openCard(c.id, "can")}
            action={E.mode === "select" && <SOBtn text="Выбрать" icon="check" onClick={() => doSelect(c)} style={{ height: 48 }} />} />))}
      </div>);
  };
  const recents = (asChips) => {
    if (q || !recent.length || smode !== "name") return null;
    const items = recent.map((id) => VD.card(id));
    if (asChips) return (
      <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
        <span style={{ font: "600 15px var(--font-sans)", color: "var(--on-surface-variant)", marginRight: 4, display: "inline-flex", alignItems: "center", gap: 6 }}><SIcon icon="history" size={20} color="currentColor" />Недавние</span>
        {items.map((c) => <VsChip key={c.id} icon="directions_car" text={`${c.brand} ${c.model} ${c.trim}`} onClick={() => openCard(c.id, "recent")} />)}
      </div>);
    return (
      <div style={{ marginBottom: 6 }}>
        <div style={{ padding: "12px 12px 4px", font: "600 15px var(--font-sans)", color: "var(--on-surface-variant)" }}>Недавние</div>
        {items.map((c) => <VK.VsRow key={c.id} dense lead={<div style={{ width: 40, height: 40, borderRadius: 12, display: "grid", placeItems: "center", background: "var(--secondary-container)", color: "var(--on-secondary-container)", flex: "0 0 auto" }}><SIcon icon="history" size={24} color="currentColor" /></div>}
          title={`${c.brand} ${c.model} ${c.trim}`} sub={`${c.years} · CAN ${c.can}`} onClick={() => openCard(c.id, "recent")} />)}
        <div style={{ height: 1, background: "var(--outline-variant)", margin: "8px 12px 0" }} />
      </div>);
  };
  const crumbsItems = () => {
    const it = [{ t: smode === "can" ? `CAN ${can}` : "Марки", on: () => (smode === "can" ? setSel({ brand: null, model: null, trim: null, src: "cat" }) : toTop()) }];
    if (smode === "name" || sel.trim) {
      if (B && smode === "name") it.push({ t: B.name, on: () => setSel({ brand: B.id, model: null, trim: null, src: "cat" }) });
      if (M && smode === "name") it.push({ t: `${M.name} (${M.years})`, on: () => setSel({ ...sel, trim: null, model: VD.TRIMS[M.id].length === 1 && !wide ? null : M.id }) });
      if (card) it.push({ t: smode === "can" ? `${card.brand} ${card.model} ${card.trim}` : card.trim });
    }
    return it;
  };
  const fCount = fg.length;
  const filterChip = fActive && <VsChip icon="filter_alt" active text={`Фильтр: ${fCount} ${fCount === 1 ? "группа" : "группы"}`} onClick={() => { setDraft(fg); setSheet("filter"); }} onClear={() => { setFg([]); setDraft([]); }} />;

  const searchArea = (
    <div style={{ display: "flex", flexDirection: wide ? "row" : "column", gap: wide ? 12 : 10, alignItems: wide ? "center" : "stretch" }}>
      {canAvail && <VK.Seg opts={[["name", "По названию"], ["can", "По CAN-коду"]]} value={smode} onChange={setMode} style={wide ? { width: 340 } : {}} />}
      {smode === "name"
        ? <div style={{ flex: 1, minWidth: 0, maxWidth: wide ? 560 : undefined }}><VK.SearchBar value={q} onChange={setQ} placeholder={sel.model && !wide ? `Комплектация ${M.name}` : sel.brand ? `Модель ${B.name}` : canAvail ? "Марка, модель или CAN-код" : "Марка или модель"} /></div>
        : <VK.CanInput value={can} onChange={(v) => { setCan(v); autoOpened.current = null; setSel({ brand: null, model: null, trim: null, src: "cat" }); }} hint={E.prefill && can === E.prefill ? "код прошивки блока" : null} autoFocus={!can} />}
    </div>
  );

  const cardArea = (lay, pad) => {
    const k = lvKey.card, s = st(k);
    if (s === "loading") return <VsCardSkeleton layout={lay} />;
    if (s === "error" || s === "offline") return <VK.StateBox kind={s} onRetry={() => retry(k)} />;
    return <VsCard c={card} layout={lay} ui={ui} setUi={setUi} headVar={headVar} mtxVar={mtxVar} fgroups={fg} imgFail={init.imgFail} pad={pad}
      selectable={E.mode === "select"} onSelect={() => doSelect(card)} onNote={(l, x) => setNote({ l, x })} onCopy={() => copy(card)}
      onArchive={E.mode === "view" && canAvail ? () => setSnack(`Архив прошивок: фильтр CAN ${card.can}`) : null}
      onViewer={(p) => setViewer({ p, i: ui.photo })} onPdf={(d) => setSnack(`«${d.t}» откроется в системном просмотрщике PDF`)} onFolk={(d) => setSnack("Экран инструкции — вне этого ТЗ")} />;
  };

  // ── Тело по классу окна
  let body;
  const showType = E.needType && !type;
  if (showType) body = <VsTypeScreen wide={wide} onPick={(t) => setType(t)} />;
  else if (layout === "compact") {
    const step = smode === "can" ? (sel.trim ? "card" : "can") : sel.trim ? "card" : sel.model ? "trims" : sel.brand ? "models" : "brands";
    const showSearch = step !== "card";
    body = (
      <React.Fragment>
        {showSearch && <div style={{ padding: "4px 16px 8px", flex: "0 0 auto" }}>{searchArea}</div>}
        {(sel.brand || sel.trim || fActive) && (
          <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "0 8px", borderBottom: "1px solid var(--outline-variant)", flex: "0 0 auto", flexWrap: "wrap" }}>
            {(sel.brand || sel.trim) && <div style={{ flex: "1 1 200px", minWidth: 0 }}><VK.Crumbs items={crumbsItems()} /></div>}
            {filterChip && <div style={{ padding: "4px 8px" }}>{filterChip}</div>}
          </div>)}
        {step === "card" ? cardArea("compact", 16) : (
          <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto", padding: "4px 8px 24px" }}>
            {step === "brands" && <React.Fragment>{recents(false)}{brandList()}</React.Fragment>}
            {step === "models" && modelList(false)}
            {step === "trims" && trimRows(M, false)}
            {step === "can" && canList()}
          </div>)}
      </React.Fragment>);
  } else {
    const third = layout === "expanded" && w >= 1400;
    const top = (
      <div style={{ padding: "8px 24px 12px", display: "flex", flexDirection: "column", gap: 10, flex: "0 0 auto", borderBottom: "1px solid var(--outline-variant)" }}>
        {searchArea}
        {(recents(true) || filterChip) && <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>{filterChip}{recents(true)}</div>}
      </div>);
    if (layout === "medium" && sel.trim) body = (
      <React.Fragment>
        <div style={{ padding: "0 16px", borderBottom: "1px solid var(--outline-variant)", flex: "0 0 auto", display: "flex", alignItems: "center", gap: 8 }}><div style={{ flex: 1, minWidth: 0 }}><VK.Crumbs items={crumbsItems()} /></div>{filterChip}</div>
        <div style={{ flex: 1, minHeight: 0, display: "flex", justifyContent: "center" }}><div style={{ width: "100%", maxWidth: 840, display: "flex", flexDirection: "column" }}>{cardArea("medium", 24)}</div></div>
      </React.Fragment>);
    else {
      const cols = [];
      if (smode === "can") cols.push(<VsCol key="can" title={`Результаты · CAN ${can || "—"}`} w={layout === "expanded" ? 460 : 0} grow={layout === "medium"} border={layout === "expanded"}>{canList()}</VsCol>);
      else {
        cols.push(<VsCol key="b" title={combo ? "Результаты поиска" : `Марки · ${brands.length}`} w={layout === "expanded" ? (combo ? 340 : 260) : 300}>{brandList(sel.brand)}</VsCol>);
        cols.push(<VsCol key="m" title={B ? `Модели ${B.name}` : "Модели"} w={third ? 300 : 340} grow={layout === "medium"} border={layout === "expanded"}>
          {B ? modelList(!third) : <div style={{ padding: "8px 12px" }}><SText variant="bodyLarge" color="var(--on-surface-variant)">Выберите марку слева или начните вводить название в поиске.</SText></div>}</VsCol>);
        if (third) cols.push(<VsCol key="t" title="Комплектации" w={260}>{M && VD.TRIMS[M.id].length > 1 ? trimRows(M, false) : <div style={{ padding: "8px 12px" }}><SText variant="bodyLarge" color="var(--on-surface-variant)">{M ? "Одна комплектация — открыта сразу." : "Выберите модель."}</SText></div>}</VsCol>);
      }
      if (layout === "expanded") cols.push(
        <section key="card" aria-label="Карточка автомобиля" style={{ flex: "1 1 0", minWidth: 0, display: "flex", flexDirection: "column", background: "var(--surface-container-lowest)" }}>
          {card ? <React.Fragment>
            <div style={{ padding: "0 16px", borderBottom: "1px solid var(--outline-variant)", flex: "0 0 auto" }}><VK.Crumbs items={crumbsItems()} /></div>
            {cardArea(w - (third ? 820 : smode === "can" ? 460 : 600) >= 640 ? "medium" : "compact", 20)}
          </React.Fragment> : (
            <div style={{ flex: 1, display: "grid", placeItems: "center", padding: 24 }}>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 12, textAlign: "center", maxWidth: 340 }}>
                <SIcon icon="directions_car" size={56} color="var(--outline)" />
                <SText variant="bodyLarge" color="var(--on-surface-variant)">{smode === "can" ? "Выберите автомобиль из результатов — карточка откроется здесь." : "Выберите марку, модель и комплектацию — карточка откроется здесь."}</SText>
              </div>
            </div>)}
        </section>);
      body = <React.Fragment>{top}<div style={{ flex: 1, minHeight: 0, display: "flex" }}>{cols}</div></React.Fragment>;
    }
  }

  const navIcon = <SIconBtn icon="ArrowBack" contentDescription="Назад" onClick={back} />;
  const actions = !showType && (
    <div style={{ display: "flex", alignItems: "center", gap: 4 }}>
      {E.chip && type && <VsTypeChip type={type} onPick={changeType} />}
      {!E.noFilter && (
        <div style={{ position: "relative" }}>
          <SIconBtn icon="filter_list" contentDescription={fActive ? `Фильтр функций, активен: ${fCount}` : "Фильтр функций"} selected={fActive} onClick={() => { setDraft(fg); setSheet("filter"); }} />
          {fActive && <span aria-hidden="true" style={{ position: "absolute", top: 8, right: 8, width: 10, height: 10, borderRadius: 5, background: "var(--error)", border: "2px solid var(--background)" }} />}
        </div>)}
    </div>);
  const selectBarVisible = E.mode === "select" && sel.trim && (layout !== "expanded" || true) && !showType;

  const filterActions = [
    <STBtn key="r" text="Сбросить" onClick={() => setDraft([])} style={{ height: 48 }} />,
    <SBtn key="a" text="Применить" onClick={() => { setFg(draft); setSheet(null); }} style={{ height: 48, minWidth: 140 }} />,
  ];
  const noteBody = note && <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
    <div style={{ display: "flex", alignItems: "center", gap: 8 }}><SIcon icon={VK.CAP[note.x.v].icon} size={24} color={VK.CAP[note.x.v].ic} /><SText variant="titleMedium">{VK.CAP[note.x.v].t}</SText></div>
    <VK.Html html={note.x.note} /></div>;

  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      <STop title={title} subtitle={E.sub || (type && E.needType ? VS_TYPES.find((t) => t.id === type).t : null)} navigationIcon={navIcon} actions={actions} />
      {body}

      {sheet === "filter" && (wide
        ? <SDialog open onDismissRequest={() => setSheet(null)} title="Фильтр функций" style={{ maxWidth: 480, width: "100%" }}
            text={<div style={{ display: "flex", flexDirection: "column", gap: 8 }}><span>Показывать автомобили, у которых есть функции выбранных групп. Фильтр применяется и к вкладке «Функции».</span><VsFilterBody draft={draft} setDraft={setDraft} /></div>}
            dismissButton={filterActions[0]} confirmButton={filterActions[1]} />
        : <SSheet open onDismissRequest={() => setSheet(null)}>
            <div style={{ padding: "0 16px 16px", display: "flex", flexDirection: "column", gap: 8 }}>
              <SText variant="titleLarge">Фильтр функций</SText>
              <SText variant="bodyMedium" color="var(--on-surface-variant)">Показывать автомобили, у которых есть функции выбранных групп.</SText>
              <VsFilterBody draft={draft} setDraft={setDraft} />
              <div style={{ display: "flex", gap: 12, justifyContent: "flex-end", paddingTop: 8 }}>{filterActions}</div>
            </div>
          </SSheet>)}
      {note && (wide
        ? <SDialog open onDismissRequest={() => setNote(null)} title={note.l} style={{ maxWidth: 480 }} text={noteBody} confirmButton={<STBtn text="Закрыть" onClick={() => setNote(null)} style={{ height: 48 }} />} />
        : <SSheet open onDismissRequest={() => setNote(null)}><div style={{ padding: "0 16px 20px", display: "flex", flexDirection: "column", gap: 12 }}><SText variant="titleLarge">{note.l}</SText>{noteBody}<SBtn text="Закрыть" fullWidth onClick={() => setNote(null)} style={{ height: 52 }} /></div></SSheet>)}
      {dialog && (
        <SDialog open onDismissRequest={() => setDialog(null)} icon="warning" title="CAN-код не совпадает с прошивкой блока" style={{ maxWidth: 460 }}
          text={<span>У выбранного автомобиля CAN-код <b style={{ fontFamily: "var(--font-mono)" }}>{dialog.c.can}</b>, а в блоке {VD.BLOCK.name} единая CAN-прошивка с кодом <b style={{ fontFamily: "var(--font-mono)" }}>{VD.BLOCK.can}</b>. Блок может не работать с шинами этого автомобиля.</span>}
          dismissButton={<STBtn text="Отмена" onClick={() => setDialog(null)} style={{ height: 48 }} />}
          confirmButton={<SBtn text="Выбрать всё равно" onClick={() => finishSelect(dialog.c)} style={{ height: 48 }} />} />)}
      {viewer && <VK.PhotoViewer count={viewer.p.photos} idx={ui.photo} onIdx={(i) => setUi({ photo: i })} onClose={() => setViewer(null)} label={viewer.p.name} />}
      {snack && <div style={{ position: "absolute", left: 16, right: 16, bottom: selectBarVisible ? 96 : 16, zIndex: 50, display: "flex", justifyContent: "center", pointerEvents: "none" }}>
        <div style={{ pointerEvents: "auto", maxWidth: 560, width: "100%" }}><SSnack message={snack} withDismissAction onDismiss={() => setSnack(null)} /></div></div>}
    </div>
  );
}

function VsChip({ text, icon, onClick, onClear, active }) {
  const [hov, setHov] = React.useState(false);
  return (
    <span onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)} style={{ display: "inline-flex", alignItems: "center", height: 48, borderRadius: 12, boxSizing: "border-box", maxWidth: "100%",
      border: active ? "none" : "1px solid var(--outline)", background: active ? "var(--secondary-container)" : hov ? "color-mix(in srgb, var(--on-surface) 6%, transparent)" : "transparent", color: active ? "var(--on-secondary-container)" : "var(--on-surface)" }}>
      <button type="button" onClick={onClick} style={{ height: "100%", display: "inline-flex", alignItems: "center", gap: 8, padding: onClear ? "0 4px 0 12px" : "0 14px 0 12px", border: "none", background: "transparent", color: "inherit", font: "500 15px var(--font-sans)", cursor: "pointer", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", minWidth: 0 }}>
        {icon && <SIcon icon={icon} size={20} color="currentColor" />}<span style={{ overflow: "hidden", textOverflow: "ellipsis" }}>{text}</span>
      </button>
      {onClear && <button type="button" aria-label="Сбросить фильтр" onClick={onClear} style={{ width: 44, height: 48, border: "none", background: "transparent", color: "inherit", display: "grid", placeItems: "center", cursor: "pointer" }}><SIcon icon="close" size={20} color="currentColor" /></button>}
    </span>
  );
}

function VsBrandTile({ b, q, onClick }) {
  const [hov, setHov] = React.useState(false);
  return (
    <button type="button" onClick={onClick} onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}
      style={{ minHeight: 108, padding: "12px 6px", borderRadius: 16, border: "1px solid var(--outline-variant)", background: hov ? "color-mix(in srgb, var(--on-surface) 6%, var(--surface-container-low))" : "var(--surface-container-low)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 8, cursor: "pointer", color: "var(--on-surface)", font: "inherit", minWidth: 0 }}>
      <VK.Logo name={b.name} size={48} />
      <span style={{ font: "500 15px/1.25 var(--font-sans)", textAlign: "center", overflowWrap: "anywhere", display: "-webkit-box", WebkitLineClamp: 3, WebkitBoxOrient: "vertical", overflow: "hidden" }}><VK.Hl text={b.name} q={q} /></span>
    </button>
  );
}

// ════════ Витрина: панель управления ═════════════════════════════════════════
function VsSeg({ items, value, onChange }) {
  return <div className={"seg" + (items.length > 4 ? " wrap" : "")}>{items.map(([k, l]) => <button key={k} className={value === k ? "on" : ""} onClick={() => onChange(k)}>{l}</button>)}</div>;
}
const VS_PRESETS = [
  ["brands", "Марки", {}],
  ["type", "Тип устройства", { entry: "A", init: { type: null } }],
  ["combo", "Поиск «toyota camry»", { init: { q: "toyota camry" } }],
  ["nothing", "Ничего не найдено", { init: { q: "тайота" } }],
  ["models", "Модели Toyota", { init: { brand: "toyota" } }],
  ["trims", "Комплектации Camry", { init: { brand: "toyota", model: () => VD.CAMRY_MODEL } }],
  ["card", "Карточка Camry", { init: { trim: () => VD.CAMRY_TRIM } }],
  ["points", "Точка + галерея", { init: { trim: () => VD.CAMRY_TRIM, tab: "points", point: 0 } }],
  ["notabs", "Карточка без вкладок", { init: { trim: () => VD.GRANTA_TRIM } }],
  ["imgfail", "Фото не загрузилось", { init: { trim: () => VD.CAMRY_TRIM, imgFail: true } }],
  ["longb", "Длинная марка", { init: { brand: "mercedes-benz-commercial-vehicles" } }],
  ["longt", "Длинная комплектация", { init: { brand: "toyota", model: () => VD.MODELS.toyota.find((m) => m.name === "RAV4").id } }],
  ["empty", "Нет комплектаций", { init: { brand: "toyota", model: () => VD.MODELS.toyota.find((m) => m.name === "bZ4X").id } }],
  ["can4", "CAN 2154: несколько", { init: { smode: "can", can: "2154" } }],
  ["can1", "CAN 1007: один → карточка", { init: { smode: "can", can: "1007" } }],
  ["can0", "CAN 0000: ноль", { init: { smode: "can", can: "0000" } }],
  ["filter", "Фильтр (шторка)", { init: { sheet: "filter", fgroups: ["basic", "ev"] } }],
  ["filtered", "Фильтр отсеял всё", { init: { brand: "kia", fgroups: ["trailer"] } }],
];
function vsResolve(init) { const o = {}; Object.keys(init || {}).forEach((k) => { o[k] = typeof init[k] === "function" ? init[k]() : init[k]; }); return o; }
function useVsCtl() {
  const [dark, setDarkRaw] = React.useState(() => { try { const v = localStorage.getItem("ps-theme"); return v ? v === "dark" : matchMedia("(prefers-color-scheme: dark)").matches; } catch (e) { return false; } });
  const setDark = (v) => { setDarkRaw(v); try { localStorage.setItem("ps-theme", v ? "dark" : "light"); } catch (e) {} };
  React.useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const [entry, setEntry] = React.useState("C");
  const [preset, setPreset] = React.useState("brands");
  const [net, setNet] = React.useState("ok");
  const [lvl, setLvl] = React.useState("all");
  const catVar = "A", headVar = "B", mtxVar = "B"; // утверждено: марки списком, шапка B, матрица B
  const P = VS_PRESETS.find((p) => p[0] === preset)[2];
  const ent = P.entry || entry;
  const init = vsResolve(P.init);
  if (VS_ENTRY[ent].needType && init.type === undefined) init.type = "alarm";
  const props = { entry: ent, net, netLevel: lvl, catVar, headVar, mtxVar, init };
  const bar = (
    <React.Fragment>
      <button type="button" className="theme-fab" onClick={() => setDark(!dark)} aria-label={dark ? "Светлая тема" : "Тёмная тема"}>{dark ? "☀" : "☾"}</button>
      <div className="toolbar">
        <VsSeg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(x) => setDark(x === "d")} />
        <VsSeg items={[["A", "A · Главная"], ["B", "B · Расш. монтаж"], ["C", "C · CAN-код (выбор)"], ["D", "D · Архив (выбор)"], ["E", "E · Конфигуратор"]]} value={entry} onChange={setEntry} />
      </div>
      <div className="toolbar"><VsSeg items={VS_PRESETS.map((p) => [p[0], p[1]])} value={preset} onChange={setPreset} /></div>
      <div className="toolbar">
        <VsSeg items={[["ok", "Сеть: ОК"], ["loading", "Загрузка"], ["error", "Ошибка"], ["offline", "Нет интернета"]]} value={net} onChange={setNet} />
        <VsSeg items={[["all", "Все уровни"], ["brands", "Марки"], ["models", "Модели"], ["trims", "Компл."], ["card", "Карточка"], ["can", "CAN"]]} value={lvl} onChange={setLvl} />
      </div>
    </React.Fragment>
  );
  return { dark, props, bar, key: [entry, preset, net, lvl].join() };
}

Object.assign(window, { VehicleSelectApp, useVsCtl, VsSeg, VS_PRESETS, vsResolve });
