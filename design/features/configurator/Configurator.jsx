// Конфигуратор (ТЗ uploads/configurator-design-brief.md): K1 «Мои конфигурации», K2 выбор модели, K3 файл .dxs, K4 облако,
// K5 редактор конфигурации (DeviceSettingsScreen с source), K6 «Сохранить в…», K7 «Загрузить из…» в редактор блока, K8 диалоги, K9 «Что изменится».
// Экраны лежат стеком и остаются смонтированными (правки в редакторе блока не теряются при переходе к выбору источника).
(function () {
const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpTextField, CmpCircularProgressIndicator } = DS;
const D = window.DSET, K = window.CfgKit, VK = window.VsKit, CD = window.CFG_DATA, VD = window.VS_DATA;
const { useState, useEffect, useRef } = React;

function resolve(patch, base) {
  const o = { ...base };
  Object.keys(patch || {}).forEach((k) => {
    const p = patch[k];
    if (p && p.io) {
      const b = base[k];
      const v = { ...b, logics: b.logics.map((l) => ({ ...l, on: p.add.includes(l.n) ? true : p.del.includes(l.n) ? false : l.on })) };
      if (p.type !== undefined) v.type = p.type;
      if (p.pol !== undefined) v.pol = p.pol;
      o[k] = v;
    } else o[k] = p;
  });
  return o;
}
const BLOCK_VALS = resolve(CD.BLOCK_PATCH, D.BASE0);
const valsOf = (patch) => resolve(patch, BLOCK_VALS);
const mineVals = (c) => c.values || valsOf(c.patch);
const editsOf = (base, vals) => { const e = {}; Object.keys(vals).forEach((k) => { if (D.IDX[k] && !D.IDX[k].n.disabled && !D.same(base[k], vals[k])) e[k] = vals[k]; }); return e; };
const SEED = { "3.2.2": 7, "3.2.5.1": true, "3.2.5.2": 6 };
const CLOUD = CD.CLOUD.map((c) => ({ model: "DX-9X", ...c })).concat([{ id: "c6", name: "Охрана и автозапуск для DX-4GS", short: "Автозапуск по температуре, без меток.", model: "DX-4GS", author: "Pandora Lab", login: "lab", updated: "05.10.26", created: "05.10.26", rating: 3, votes: 2, can: "2154", map: true, att: [], html: "<p>Базовая установка для DX-4GS.</p>", patch: {} }]);
const NOW = "10:42 26.10.26";
const BLK = CD.BLOCK;
const plural = K.plural;
const nChanges = (n) => `${n} ${plural(n, "изменение", "изменения", "изменений")}`;
const fname = (m, t) => `${m} ${(t || "26.10.26").replace(/[\\/:*?"<>|]/g, "").slice(0, 40)}.dxs`;
const dnum = (s) => { const m = s.match(/(\d\d)\.(\d\d)\.(\d\d)$/); return m ? +(m[3] + m[2] + m[1]) : 0; };

function useLayout() {
  const ref = useRef(null); const [w, setW] = useState(0);
  useEffect(() => { const el = ref.current; if (!el) return; const m = () => setW(el.offsetWidth); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, []);
  return [ref, w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact", w];
}
function Bar({ title, sub, onBack, actions, backIcon = "ArrowBack" }) {
  return <CmpTopAppBar title={title} subtitle={sub} navigationIcon={onBack ? <CmpIconButton icon={backIcon} contentDescription="Назад" onClick={onBack} /> : null} actions={actions} />;
}
const subStyle = { font: "500 14px var(--font-sans)", color: "var(--on-surface-variant)" };
function Skel({ n = 5, h = 104 }) {
  return <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>{Array.from({ length: n }).map((_, i) => <div key={i} style={{ height: h, borderRadius: 16, background: "var(--surface-container)", opacity: 1 - i * 0.12 }} />)}</div>;
}
function SecTitle({ children, n }) {
  return <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 4px" }}><CmpText variant="titleMedium">{children}</CmpText>{n ? <span style={subStyle}>· {n}</span> : null}</div>;
}

// ── K2 Выбор модели блока
function ModelsScreen({ purpose, st0 = "ready", wide, onPick, onBack, blockOn }) {
  const [q, setQ] = useState("");
  const [st, setSt] = useState(st0);
  useEffect(() => { if (st !== "loading" || st0 === "loading") return; const t = setTimeout(() => setSt("ready"), 900); return () => clearTimeout(t); }, [st]);
  const ql = q.trim().toLowerCase();
  const all = CD.MODELS.filter((m) => !ql || m.toLowerCase().includes(ql));
  const row = (m, sub, lead) => <VK.VsRow key={m + (sub || "")} lead={<span style={{ width: 40, height: 40, borderRadius: 12, display: "grid", placeItems: "center", background: "var(--surface-container-highest)" }}><CmpIcon icon={lead || "memory"} size={22} color="var(--on-surface-variant)" /></span>} title={<span style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}><K.Hl text={m} q={q} /></span>} sub={sub} onClick={() => onPick(m)} />;
  const recent = purpose === "file" ? CD.RECENT_MODELS : CD.RECENT_MODELS;
  return (
    <React.Fragment>
      <Bar title="Модель блока" sub={purpose === "file" ? "В файле модель не хранится — укажите её" : "Заводские настройки выбранной модели"} onBack={onBack} />
      <div style={{ padding: "4px 16px 8px", maxWidth: 720, width: "100%", margin: "0 auto", boxSizing: "border-box" }}><VK.SearchBar value={q} onChange={setQ} placeholder="Название модели" /></div>
      <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ maxWidth: 720, margin: "0 auto", padding: "0 0 24px" }}>
          {st === "loading" && <div style={{ padding: "8px 16px" }}><VK.SkelList n={8} /></div>}
          {st === "error" && <VK.StateBox kind="error" title="Не удалось загрузить список моделей" text="Сервер не ответил. Проверьте интернет и повторите." onRetry={() => setSt("loading")} />}
          {st === "ready" && (
            <React.Fragment>
              {!ql && blockOn && <React.Fragment><div style={{ padding: "8px 20px 0" }}><CmpText variant="labelLarge" color="var(--on-surface-variant)">Подключённый блок</CmpText></div>{row(BLK.model, "Серийный № " + BLK.serial, "bluetooth_connected")}</React.Fragment>}
              {!ql && <React.Fragment><div style={{ padding: "12px 20px 0" }}><CmpText variant="labelLarge" color="var(--on-surface-variant)">Недавние</CmpText></div>{recent.map((m, i) => row(m, purpose === "file" && i === 0 ? "Последняя использованная" : null, "history"))}</React.Fragment>}
              <div style={{ padding: "12px 20px 0" }}><CmpText variant="labelLarge" color="var(--on-surface-variant)">{ql ? "Найдено: " + all.length : "Все модели · " + CD.MODELS.length}</CmpText></div>
              {all.map((m) => row(m))}
              {ql && !all.length && <VK.StateBox kind="nothing" title={`Модели «${q}» нет`} text="Проверьте название на наклейке блока." />}
            </React.Fragment>
          )}
        </div>
      </div>
    </React.Fragment>
  );
}

// ── K4 Облачные конфигурации автомобиля: список + карточка
function CloudCardBody({ c, car, ctx, layout, blockOn, writeMode, act }) {
  const wide = layout !== "compact";
  const [full, setFull] = useState(false);
  const canBad = blockOn && c.can !== BLK.can;
  const modelOk = blockOn && c.model === BLK.model;
  const imgs = c.att.filter((a) => a.k === "img");
  const meta = (icon, text) => <div style={{ display: "flex", alignItems: "center", gap: 10, minHeight: 32 }}><CmpIcon icon={icon} size={22} color="var(--on-surface-variant)" /><span style={{ font: "400 16px/1.35 var(--font-sans)" }}>{text}</span></div>;
  let primary;
  if (ctx === "block") primary = <D.DBtn kind="filled" icon="download" text="Загрузить в редактор" disabled={canBad || !modelOk} onClick={act.load} style={{ flex: "1 1 auto" }} />;
  else if (modelOk) primary = <D.DBtn kind="filled" icon="upload" text="Записать в блок" disabled={canBad} onClick={act.write} style={{ flex: "1 1 auto" }} />;
  else if (writeMode === "disable") primary = <D.DBtn kind="filled" icon="upload" text="Записать в блок" disabled style={{ flex: "1 1 auto" }} />;
  const why = ctx !== "block" && !modelOk && writeMode === "disable" ? (blockOn ? `Подключён блок ${BLK.model}, конфигурация — для ${c.model}` : "Блок не подключён") : null;
  const actions = (
    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      {why && <span style={{ ...subStyle, fontWeight: 400 }}>{why}</span>}
      <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
        {primary}
        <D.DBtn kind="outlined" icon="visibility" text="Открыть настройки" onClick={act.open} style={{ flex: "1 1 auto" }} />
        <D.DBtn kind="outlined" icon="inventory_2" text="Сохранить в мои" onClick={act.save} style={{ flex: "1 1 auto" }} />
      </div>
    </div>
  );
  return (
    <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
        <div style={{ maxWidth: 760, margin: "0 auto", padding: wide ? "16px 24px 24px" : "8px 16px 24px", display: "flex", flexDirection: "column", gap: 16, boxSizing: "border-box" }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}><K.ModelBadge m={c.model} big /><K.Rating r={c.rating} votes={c.votes} />{c.mineOnly && <K.Plaque icon="lock" text="Видна только вам" />}</div>
            <CmpText variant="headlineSmall" as="div" style={{ wordBreak: "break-word" }}>{c.name}</CmpText>
            {c.short && <CmpText variant="bodyLarge" color="var(--on-surface-variant)" as="div">{c.short}</CmpText>}
          </div>
          <div style={{ display: "grid", gridTemplateColumns: wide ? "1fr 1fr" : "1fr", gap: 4 }}>
            {meta("directions_car", `${car.brand} ${car.model} ${car.trim}`)}
            {meta("person", `${c.author} · @${c.login}`)}
            {meta("update", `Обновлена ${c.updated}`)}
            {meta("memory", <span>CAN-код конфигурации <b style={{ fontFamily: "var(--font-mono)" }}>{c.can}</b></span>)}
          </div>
          {canBad && (
            <div role="alert" style={{ display: "flex", flexDirection: "column", gap: 12, padding: 16, borderRadius: 16, background: "var(--error-container)", color: "var(--on-error-container)" }}>
              <div style={{ display: "flex", gap: 12 }}><CmpIcon icon="warning" size={26} /><CmpText variant="bodyLarge">В блоке прошивка для CAN-кода {BLK.can}, конфигурация рассчитана на {c.can}. Обновите прошивку под {c.can}.</CmpText></div>
              <D.DBtn kind="filled" icon="system_update" text="Перейти к обновлению прошивки" onClick={act.firmware} style={{ alignSelf: wide ? "flex-start" : "stretch", background: "var(--error)", color: "var(--on-error)" }} />
            </div>
          )}
          {wide && <div>{actions}</div>}
          {c.html && (
            <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
              <SecTitle>Описание</SecTitle>
              <VK.Html html={c.html} clamp={full ? undefined : 4} />
              {c.html.length > 260 && <div><D.DBtn text={full ? "Свернуть" : "Показать полностью"} icon={full ? "expand_less" : "expand_more"} onClick={() => setFull(!full)} style={{ paddingLeft: 8 }} /></div>}
            </div>
          )}
          {c.map && <K.OptRow icon="picture_as_pdf" title="Монтажная карта" sub="PDF · откроется в просмотрщике" onClick={act.pdf} trail={<CmpIcon icon="open_in_new" size={22} color="var(--on-surface-variant)" />} />}
          {c.att.length > 0 && (
            <div>
              <SecTitle n={c.att.length}>Вложения</SecTitle>
              <K.AttachGrid items={c.att} cols={layout === "compact" ? 2 : 3} onOpen={(a) => a.k === "pdf" ? act.pdf(a) : act.viewer(imgs.indexOf(a), imgs.length, a.t)} />
            </div>
          )}
        </div>
      </div>
      {!wide && <div style={{ flex: "0 0 auto", padding: "12px 16px 14px", borderTop: "1px solid var(--outline-variant)", background: "var(--surface-container)" }}>{actions}</div>}
    </div>
  );
}
function CloudScreen({ car, ctx, net0, empty, initSel, layout, blockOn, writeMode, onBack, mkAct }) {
  const [sort, setSort] = useState("best");
  const [mf, setMf] = useState(ctx === "block" ? BLK.model : null);
  const [mfOpen, setMfOpen] = useState(false);
  const [sel, setSel] = useState(initSel || (layout === "expanded" ? null : null));
  const [net, setNet] = useState(net0);
  useEffect(() => { if (net !== "loading" || net0 === "loading") return; const t = setTimeout(() => setNet("ok"), 900); return () => clearTimeout(t); }, [net]);
  const items = empty ? [] : CLOUD.filter((c) => !mf || c.model === mf).sort((a, b) => sort === "best" ? ((b.rating || 0) - (a.rating || 0)) || dnum(b.updated) - dnum(a.updated) : dnum(b.updated) - dnum(a.updated));
  const exp = layout === "expanded";
  const selC = sel ? CLOUD.find((c) => c.id === sel) : exp ? items[0] : null;
  const carT = `${car.brand} ${car.model} ${car.trim}`;
  const card = selC && <CloudCardBody key={selC.id} c={selC} car={car} ctx={ctx} layout={layout} blockOn={blockOn} writeMode={writeMode} act={mkAct(selC, car)} />;
  if (!exp && sel) return <React.Fragment><Bar title="Конфигурация" sub={carT} onBack={() => setSel(null)} />{card}</React.Fragment>;
  const filters = (
    <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap", padding: "0 16px 8px" }}>
      <VK.Seg opts={[["best", "Лучшие"], ["new", "Новые"]]} value={sort} onChange={setSort} style={{ width: 220 }} />
      {ctx === "block" ? <K.Plaque icon="memory" text={"Под " + BLK.model} /> : <K.Chip label={"Модель блока: " + (mf || "все")} active={!!mf} onClick={() => setMfOpen(true)} onClear={mf ? () => setMf(null) : null} />}
    </div>
  );
  let list;
  if (net === "loading") list = <div style={{ padding: "0 16px" }}><Skel n={4} h={150} /></div>;
  else if (net === "error") list = <VK.StateBox kind="error" title="Не удалось загрузить конфигурации" text="Сервер не ответил. Повторите попытку." onRetry={() => setNet("loading")} />;
  else if (net === "offline") list = <VK.StateBox kind="offline" title="Облако недоступно без интернета" text="«Мои конфигурации», файлы и заводские настройки работают без сети." onRetry={() => setNet("loading")} />;
  else if (!items.length) list = <VK.StateBox kind="empty" title={`Для этого автомобиля нет конфигураций под ${mf || "эту модель"}`} text="Выберите другой автомобиль или создайте конфигурацию с нуля." action={<D.DBtn kind="outlined" icon="directions_car" text="Другой автомобиль" onClick={onBack} />} />;
  else list = <div style={{ display: "flex", flexDirection: "column", gap: 10, padding: "0 16px 24px" }}>{items.map((c) => <K.CloudCard key={c.id} c={c} selected={exp && selC && selC.id === c.id} canBad={blockOn && c.can !== BLK.can} onOpen={() => setSel(c.id)} />)}</div>;
  const mfSheet = mfOpen && (
    <K.Sheet wide={layout !== "compact"} title="Модель блока" onClose={() => setMfOpen(false)}>
      {[null, ...Array.from(new Set(CLOUD.map((c) => c.model)))].map((m) => <K.OptRow key={m || "all"} icon={m ? "memory" : "select_all"} title={m || "Все модели"} onClick={() => { setMf(m); setMfOpen(false); }} trail={mf === m ? <CmpIcon icon="check" size={24} color="var(--primary)" /> : null} />)}
    </K.Sheet>
  );
  const head = <Bar title="Конфигурации" sub={carT} onBack={onBack} />;
  if (!exp) return <React.Fragment>{head}<div style={{ maxWidth: 760, width: "100%", margin: "0 auto", flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>{filters}<div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{list}</div></div>{mfSheet}</React.Fragment>;
  return (
    <React.Fragment>
      {head}
      <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
        <div style={{ width: 420, flex: "0 0 auto", display: "flex", flexDirection: "column", borderRight: "1px solid var(--outline-variant)" }}>{filters}<div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{list}</div></div>
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", background: "var(--surface-container-lowest, var(--background))" }}>{card || (net === "ok" && items.length ? null : <div />)}</div>
      </div>
      {mfSheet}
    </React.Fragment>
  );
}

// ── K6 форма «Мои конфигурации» / «Переименовать»
function SaveForm({ wide, mode, init, model, onCancel, onSubmit }) {
  const [name, setName] = useState(init.name || "");
  const [desc, setDesc] = useState(init.desc || "");
  const [touched, setTouched] = useState(false);
  const bad = !name.trim();
  const submit = () => { setTouched(true); if (!bad) onSubmit({ name: name.trim(), desc: desc.trim() }); };
  const btns = <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}><D.DBtn text="Отмена" onClick={onCancel} /><D.DBtn kind="filled" text="Сохранить" onClick={submit} style={{ minWidth: 140 }} /></div>;
  return (
    <K.FullDlg wide={wide} w={560} title={mode === "rename" ? "Название и описание" : "Сохранить в «Мои конфигурации»"} onClose={onCancel}
      action={<D.DBtn kind="filled" text="Сохранить" onClick={submit} style={{ height: 48 }} />} footer={wide ? btns : null}>
      <div style={{ display: "flex", flexDirection: "column", gap: 20, padding: wide ? "8px 24px 8px" : "12px 16px 24px" }}>
        <CmpTextField label="Название" value={name} onChange={(s) => setName(s.slice(0, 50))} singleLine isError={touched && bad}
          supportingText={<span style={{ display: "flex", justifyContent: "space-between", gap: 8 }}><span>{touched && bad ? "Введите название" : "Обязательно"}</span><span>{name.length} / 50</span></span>} />
        <K.TextArea label="Описание" value={desc} onChange={setDesc} max={500} rows={wide ? 4 : 5} placeholder="Что настроено, для какой машины" />
        {mode !== "rename" && (
          <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 56, padding: "0 16px", borderRadius: 12, background: "var(--surface-container)" }}>
            <CmpIcon icon="memory" size={24} color="var(--on-surface-variant)" />
            <span style={{ flex: 1, font: "400 16px var(--font-sans)", color: "var(--on-surface-variant)" }}>Модель блока</span>
            <K.ModelBadge m={model} big />
          </div>
        )}
      </div>
    </K.FullDlg>
  );
}

const BLOCK_DEMOS = ["blk", "load-sheet", "pick", "pick-cloud", "diff-few", "diff-many", "diff-none", "diff-busy", "diff-error", "diff-pending", "loading", "loaded", "load-can", "load-bad", "save-to-blk"];
function initState(p) {
  const dm = p.demo || "";
  const S = (s, o) => ({ key: s + Math.random(), s, api: { current: null }, ...o });
  const m1 = CD.MINE[0], car = VD.card(VD.CAMRY_TRIM);
  const blockStart = p.entry === "block" || BLOCK_DEMOS.includes(dm);
  const blk = S("block", { initialEdits: p.blockEdits || dm === "diff-pending" ? SEED : undefined });
  const st = { stack: [blockStart ? blk : S("list")], ov: null, busy: null, snack: null, mine: CD.MINE, listSt: "ready", q: "", mf: null, sel: "m1" };
  const push = (s, o) => st.stack.push(S(s, o));
  const ed = (src, values, o) => push("editor", { src, values, ...o });
  const edMine = (o) => ed({ kind: "mine", id: "m1", title: m1.name, model: m1.model }, valsOf(m1.patch), o);
  const edFile = (o) => ed({ kind: "file", title: CD.FILE.name, model: "DX-9X" }, valsOf(CD.FILE.patch), o);
  const edCloud = (o) => ed({ kind: "cloud", title: CLOUD[0].name, model: "DX-9X" }, valsOf(CLOUD[0].patch), o);
  const cloud = (o) => { push("vehicles", { ctx: blockStart ? "block" : "home" }); push("cloud", { car, ctx: blockStart ? "block" : "home", ...o }); };
  const diff = (patch, title, o) => ({ k: "diff", title, vals: valsOf(patch), cur: BLOCK_VALS, ctx: blockStart ? "block" : "home", phase: "ready", ...o });
  switch (dm) {
    case "list-empty": st.mine = []; break;
    case "list-loading": st.listSt = "loading"; break;
    case "list-error": st.listSt = "error"; break;
    case "list-search": st.q = "нива"; break;
    case "list-nothing": st.q = "вертолёт"; break;
    case "list-filter": st.mf = "DX-4GS"; st.sel = "m4"; break;
    case "card-menu": st.ov = { k: "cardMenu", c: m1 }; break;
    case "create": st.ov = { k: "create" }; break;
    case "delete": st.ov = { k: "delete", c: m1 }; break;
    case "rename": st.ov = { k: "form", mode: "rename", item: m1 }; break;
    case "models": push("models", { purpose: "new" }); break;
    case "models-loading": push("models", { purpose: "new", st0: "loading" }); break;
    case "models-error": push("models", { purpose: "new", st0: "error" }); break;
    case "file-sys": st.ov = { k: "sys", mode: "open", name: CD.FILE.name, then: "file" }; break;
    case "file-model": push("models", { purpose: "file" }); break;
    case "file-corrupt": push("models", { purpose: "file" }); st.ov = { k: "err", kind: "corrupt" }; break;
    case "file-mismatch": push("models", { purpose: "file" }); st.ov = { k: "err", kind: "mismatch", m: "DX-4GS" }; break;
    case "vehicles": push("vehicles", { ctx: "home" }); break;
    case "cloud-list": cloud(); break;
    case "cloud-empty": cloud({ empty: true }); break;
    case "cloud-offline": cloud({ net0: "offline" }); break;
    case "cloud-loading": cloud({ net0: "loading" }); break;
    case "cloud-card": cloud({ initSel: "c1" }); break;
    case "cloud-can": cloud({ initSel: "c4" }); break;
    case "cloud-bare": cloud({ initSel: "c3" }); break;
    case "viewer": cloud({ initSel: "c1" }); st.ov = { k: "viewer", i: 0, n: 4, label: "Точка подключения CAN под рулём" }; break;
    case "ed-mine": edMine(); break;
    case "ed-file": edFile(); break;
    case "ed-cloud": edCloud(); break;
    case "ed-factory": ed({ kind: "factory", title: "Заводские настройки", model: "DX-9X" }, D.BASE0); break;
    case "ed-mine-menu": edMine({ demo: "menu" }); break;
    case "ed-file-menu": edFile({ demo: "menu" }); break;
    case "ed-cloud-menu": edCloud({ demo: "menu" }); break;
    case "ed-unsaved": edMine({ initialEdits: { "3.2.2": 9, "2.2": 12 }, demo: "exit" }); break;
    case "ed-ro-unsaved": edCloud({ initialEdits: { "3.2.2": 9, "2.2": 12 }, demo: "exit" }); break;
    case "ed-ro-edits": edCloud({ initialEdits: { "3.2.2": 9, "2.2": 12 } }); break;
    case "save-to": edCloud({ initialEdits: { "3.2.2": 9 } }); st.ov = { k: "saveTo", values: valsOf(CLOUD[0].patch), model: "DX-9X", name: CLOUD[0].name }; break;
    case "save-to-blk": st.ov = { k: "saveTo", values: BLOCK_VALS, model: BLK.model, name: "" }; break;
    case "save-form": st.ov = { k: "form", mode: "new", values: BLOCK_VALS, model: "DX-9X", name: CLOUD[0].name, desc: CLOUD[0].short }; break;
    case "saving": st.busy = { title: "Сохраняем…", hold: true }; break;
    case "save-error": st.ov = { k: "err", kind: "save" }; break;
    case "saved": st.snack = { m: "Сохранено в «Мои конфигурации»", a: "Открыть" }; break;
    case "overwrite": edFile({ initialEdits: { "3.2.2": 9 } }); st.ov = { k: "overwrite" }; break;
    case "load-sheet": st.ov = { k: "load" }; break;
    case "pick": push("pick"); break;
    case "pick-cloud": cloud({ initSel: "c1" }); break;
    case "diff-few": st.ov = diff(CD.NIVA, m1.name); break;
    case "diff-many": st.ov = diff(CD.BIG, CD.MINE[1].name); break;
    case "diff-none": st.ov = diff({}, CD.MINE[2].name); break;
    case "diff-busy": st.ov = diff(CD.NIVA, m1.name, { phase: "busy", hold: true }); break;
    case "diff-error": st.ov = diff(CD.NIVA, m1.name, { phase: "error" }); break;
    case "diff-pending": st.ov = diff(CD.NIVA, m1.name, { pending: 3 }); break;
    case "write-home": st.ov = diff(CD.NIVA, m1.name); break;
    case "loading": st.busy = { title: "Загрузка настроек…", text: "Подставляем значения в редактор. В блок пока ничего не записывается.", hold: true }; break;
    case "loaded": blk.initialEdits = editsOf(BLOCK_VALS, valsOf(CD.NIVA)); blk.toast = `Загружено: ${nChanges(Object.keys(blk.initialEdits).length)}. Нажмите «Записать»`; break;
    case "load-can": st.ov = { k: "err", kind: "can", can: "1032" }; break;
    case "load-bad": st.ov = { k: "err", kind: "badBlock" }; break;
  }
  return st;
}

function ConfiguratorApp(props) {
  const { blockOn = true, writeMode = "hide", net = "ok", fileState = "ok", saveFail = false, onBack = () => {} } = props;
  const [ref, layout] = useLayout();
  const wide = layout !== "compact";
  const [init] = useState(() => initState(props));
  const [stack, setStack] = useState(init.stack);
  const [ov, setOv] = useState(init.ov);
  const [busy, setBusy] = useState(init.busy);
  const [snack, setSnack] = useState(init.snack);
  const [mine, setMine] = useState(init.mine);
  const [listSt, setListSt] = useState(init.listSt);
  const [q, setQ] = useState(init.q);
  const [mf, setMf] = useState(init.mf);
  const [sel, setSel] = useState(init.sel);
  const [limit, setLimit] = useState(12);
  const [more, setMore] = useState(false);

  useEffect(() => { if (!snack || snack.hold) return; const t = setTimeout(() => setSnack(null), 4500); return () => clearTimeout(t); }, [snack]);
  useEffect(() => { if (listSt !== "loading" || init.listSt === "loading") return; const t = setTimeout(() => setListSt("ready"), 900); return () => clearTimeout(t); }, [listSt]);
  useEffect(() => {
    if (!ov || ov.k !== "diff" || ov.phase !== "busy" || ov.hold) return;
    const t = setTimeout(() => setOv((o) => o && o.k === "diff" ? { ...o, phase: "ready" } : o), 900);
    return () => clearTimeout(t);
  }, [ov && ov.k, ov && ov.phase]);

  const top = stack[stack.length - 1];
  const mk = (s, o) => ({ key: s + Math.random(), s, api: { current: null }, ...o });
  const push = (s, o) => setStack((st) => [...st, mk(s, o)]);
  const pop = () => { if (stack.length > 1) setStack((st) => st.slice(0, -1)); else onBack(); };
  const blockEntry = stack.find((e) => e.s === "block");
  const popTo = (s) => setStack((st) => { const i = st.map((x) => x.s).lastIndexOf(s); return i >= 0 ? st.slice(0, i + 1) : st; });
  const run = (title, text, ms, then) => { setBusy({ title, text }); setTimeout(() => { setBusy(null); then && then(); }, ms); };
  const wa = (m) => blockOn && m === BLK.model ? "ok" : writeMode;
  const wr = (m) => !blockOn ? "Блок не подключён" : `Подключён ${BLK.model}, а конфигурация для ${m}`;

  // ── сравнение и загрузка (K9 → K7)
  const askDiff = (title, vals) => {
    const inBlock = !!blockEntry;
    const api = inBlock && blockEntry.api.current;
    setOv({ k: "diff", title, vals, ctx: inBlock ? "block" : "home", cur: api ? api.values() : BLOCK_VALS, pending: api ? api.n : 0, phase: "busy" });
  };
  const confirmDiff = () => {
    const d = ov; setOv(null);
    run("Загрузка настроек…", "Подставляем значения в редактор. В блок пока ничего не записывается.", 1100, () => {
      if (d.ctx === "block") {
        popTo("block");
        setTimeout(() => { const e = stack.find((x) => x.s === "block"); const api = e && e.api.current; if (!api) return; const n = api.load(d.vals); api.snack(n ? `Загружено: ${nChanges(n)}. Нажмите «Записать»` : "Настройки уже совпадают"); }, 30);
      } else {
        const ie = editsOf(BLOCK_VALS, d.vals);
        push("block", { initialEdits: ie, toast: `Загружено: ${nChanges(Object.keys(ie).length)}. Нажмите «Записать»` });
      }
    });
  };
  const openFile = (then) => setOv({ k: "sys", mode: "open", name: CD.FILE.name, then });
  const afterFile = (then) => {
    if (then === "file") return push("models", { purpose: "file" });
    if (then === "blockLoad") {
      if (fileState === "corrupt") return setOv({ k: "err", kind: "corrupt", inBlock: true });
      if (fileState === "other") return setOv({ k: "err", kind: "badBlock" });
      askDiff(CD.FILE.name, valsOf(CD.FILE.patch));
    }
  };
  const pickModel = (e, m) => {
    if (e.purpose === "new") return push("editor", { src: { kind: "factory", title: "Заводские настройки", model: m }, values: D.BASE0 });
    if (fileState === "corrupt") return setOv({ k: "err", kind: "corrupt" });
    if (fileState === "other" || m !== "DX-9X") return setOv({ k: "err", kind: "mismatch", m });
    push("editor", { src: { kind: "file", title: CD.FILE.name, model: m }, values: valsOf(CD.FILE.patch) });
  };

  // ── мои конфигурации
  const openMine = (c) => push("editor", { src: { kind: "mine", id: c.id, title: c.name, model: c.model }, values: mineVals(c) });
  const addMine = (c) => { setMine((l) => [c, ...l]); setSel(c.id); return c; };
  const copyMine = (c, vals) => { const n = addMine({ ...c, id: "n" + Date.now(), name: ("Копия — " + c.name).slice(0, 50), updated: NOW, created: NOW, values: vals || mineVals(c) }); setSnack({ m: "Копия создана", a: "Открыть", f: () => openMine(n) }); };
  const saveFileFlow = (m, title) => setOv({ k: "sys", mode: "save", name: fname(m, title) });
  const submitForm = (o, data) => {
    setOv(null);
    run("Сохраняем…", null, 800, () => {
      if (saveFail) return setOv({ k: "err", kind: "save", retry: () => submitForm(o, data) });
      if (o.mode === "rename") {
        setMine((l) => { const it = { ...l.find((x) => x.id === o.item.id), ...data, updated: NOW }; return [it, ...l.filter((x) => x.id !== it.id)]; });
        setStack((st) => st.map((e) => e.s === "editor" && e.src.id === o.item.id ? { ...e, src: { ...e.src, title: data.name } } : e));
        return setSnack({ m: "Название и описание сохранены" });
      }
      const n = addMine({ id: "n" + Date.now(), ...data, model: o.model, updated: NOW, created: NOW, values: o.values });
      if (o.fromEd && o.fromEd.api.current) o.fromEd.api.current.commit();
      if (o.exit) pop();
      setSnack({ m: "Сохранено в «Мои конфигурации»", a: "Открыть", f: () => openMine(n) });
    });
  };
  const edAction = (e) => (k, o) => {
    const api = e.api.current, vals = api ? api.values() : e.values, m = e.src.model;
    if (k === "save" && e.src.kind === "mine") {
      run("Сохраняем…", null, 600, () => { api.commit(); setMine((l) => { const it = { ...l.find((x) => x.id === e.src.id), values: vals, updated: NOW }; return [it, ...l.filter((x) => x.id !== it.id)]; }); setSnack({ m: "Сохранено" }); if (o.exit) pop(); });
    } else if (k === "save") setOv({ k: "overwrite", e, exit: o.exit });
    else if (k === "saveAs") setOv({ k: "saveTo", values: vals, model: m, name: e.src.kind === "cloud" ? e.src.title : "", exit: o.exit, fromEd: e });
    else if (k === "saveMine") setOv({ k: "form", mode: "new", values: vals, model: m, name: e.src.kind === "file" ? e.src.title.replace(/\.dxs$/, "").replace(/_/g, " ") : e.src.title === "Заводские настройки" ? "" : e.src.title, fromEd: e });
    else if (k === "saveFile") saveFileFlow(m, e.src.kind === "file" ? null : e.src.title);
    else if (k === "copy") copyMine(mine.find((x) => x.id === e.src.id), vals);
    else if (k === "rename") setOv({ k: "form", mode: "rename", item: mine.find((x) => x.id === e.src.id) });
    else if (k === "write") askDiff(e.src.title, vals);
  };
  const blockAction = (k) => {
    const api = blockEntry && blockEntry.api.current;
    if (k === "load") setOv({ k: "load" });
    if (k === "saveTo") setOv({ k: "saveTo", values: api ? api.values() : BLOCK_VALS, model: BLK.model, name: "" });
  };
  const cloudAct = (c, car) => ({
    load: () => askDiff(c.name, valsOf(c.patch)),
    write: () => askDiff(c.name, valsOf(c.patch)),
    open: () => push("editor", { src: { kind: "cloud", title: c.name, model: c.model }, values: valsOf(c.patch) }),
    save: () => setOv({ k: "form", mode: "new", values: valsOf(c.patch), model: c.model, name: c.name, desc: c.short }),
    pdf: (a) => setSnack({ m: `«${a && a.t ? a.t : "Монтажная карта"}» откроется в просмотрщике PDF` }),
    viewer: (i, n, label) => setOv({ k: "viewer", i, n, label }),
    firmware: () => window.psGo && window.psGo("firmware"),
  });

  // ── K1 список
  const ql = q.trim().toLowerCase();
  const pickMode = top.s === "pick";
  const all = mine.filter((c) => (pickMode ? c.model === BLK.model : !mf || c.model === mf) && (!ql || c.name.toLowerCase().includes(ql) || c.desc.toLowerCase().includes(ql)));
  const paged = !ql && !mf && !pickMode;
  const shown = paged ? all.slice(0, limit) : all;
  const hasMore = paged && limit < all.length;
  const onScroll = (ev) => { const el = ev.currentTarget; if (hasMore && !more && el.scrollTop + el.clientHeight > el.scrollHeight - 120) { setMore(true); setTimeout(() => { setLimit((l) => l + 12); setMore(false); }, 700); } };
  const models = Array.from(new Set(mine.map((c) => c.model)));
  const cardMenuItems = (c) => [
    { i: "edit", t: "Переименовать / изменить описание", f: () => setOv({ k: "form", mode: "rename", item: c }) },
    { i: "content_copy", t: "Создать копию", f: () => { setOv(null); copyMine(c); } },
    { i: "description", t: "Сохранить в файл", f: () => saveFileFlow(c.model, c.name) },
    ...(wa(c.model) === "hide" ? [] : [{ i: "upload", t: "Записать в блок", sub: wa(c.model) === "disable" ? wr(c.model) : null, off: wa(c.model) === "disable", f: () => askDiff(c.name, mineVals(c)) }]),
    { i: "delete", t: "Удалить", danger: true, f: () => setOv({ k: "delete", c }) },
  ];
  const createItems = [
    { i: "factory", t: "С нуля", sub: "Заводские настройки выбранной модели", f: () => { setOv(null); push("models", { purpose: "new" }); } },
    { i: "cloud", t: "Из облака", sub: "Готовые конфигурации специалистов", f: () => { setOv(null); push("vehicles", { ctx: "home" }); } },
    { i: "description", t: "Из файла", sub: "Файл .dxs на телефоне или компьютере", f: () => openFile("file") },
    ...(blockOn ? [{ i: "bluetooth_connected", t: "Из подключённого блока", sub: `${BLK.model} · ${BLK.serial}`, f: () => { setOv(null); run("Читаем настройки из блока…", "По Bluetooth это может занять до минуты.", 1200, () => setOv({ k: "form", mode: "new", values: BLOCK_VALS, model: BLK.model, name: "" })); } }] : []),
  ];
  const selC = mine.find((c) => c.id === sel && (!mf || c.model === mf)) || all[0];
  const md = wide && !pickMode && mine.length > 0 && listSt === "ready";

  const listScreen = () => {
    const header = (
      <div style={{ display: "flex", flexDirection: "column", gap: 4, padding: "4px 16px 8px" }}>
        <VK.SearchBar value={q} onChange={setQ} placeholder="Название или описание" />
        {!pickMode && <div style={{ display: "flex", gap: 8 }}><K.Chip icon="memory" label={mf || "Все модели"} active={!!mf} onClick={() => setOv({ k: "mf" })} onClear={mf ? () => setMf(null) : null} /></div>}
        {pickMode && <div style={{ display: "flex", gap: 8, paddingTop: 4 }}><K.Plaque icon="memory" text={"Только для " + BLK.model} /></div>}
      </div>
    );
    let body;
    if (listSt === "loading") body = <div style={{ padding: "0 16px" }}><Skel /></div>;
    else if (listSt === "error") body = <VK.StateBox kind="error" title="Не удалось прочитать конфигурации" text="Хранилище приложения недоступно. Повторите попытку — сохранённые конфигурации не удалены." onRetry={() => setListSt("loading")} />;
    else if (!all.length && (ql || mf)) body = <VK.StateBox kind="nothing" title={ql ? `Ничего не найдено по «${q}»` : `Нет конфигураций для ${mf}`} text={ql ? "Поиск идёт по названию и описанию." : ""} action={<D.DBtn kind="outlined" icon="filter_alt_off" text="Сбросить поиск и фильтр" onClick={() => { setQ(""); setMf(null); }} />} />;
    else if (!all.length && pickMode) body = <VK.StateBox kind="empty" title={`Нет сохранённых конфигураций для ${BLK.model}`} text="Выберите другой источник: облако, файл или заводские настройки." action={<D.DBtn kind="outlined" text="Другой источник" onClick={() => { pop(); setOv({ k: "load" }); }} />} />;
    else body = (
      <div style={{ display: "flex", flexDirection: "column", gap: 10, padding: "0 16px " + (wide ? 24 : 96) + "px" }}>
        {shown.map((c) => <K.MineCard key={c.id} c={c} q={q} selected={md && selC && selC.id === c.id}
          onOpen={() => pickMode ? askDiff(c.name, mineVals(c)) : md ? setSel(c.id) : openMine(c)}
          onMenu={pickMode ? null : (ev) => { const r = ev.currentTarget.getBoundingClientRect(), R = ref.current.getBoundingClientRect(); setOv({ k: "cardMenu", c, at: wide ? { x: r.right - R.left, y: r.bottom - R.top + 4 } : null }); }} />)}
        {(more || hasMore) && <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 12, minHeight: 56 }}>{more ? <React.Fragment><CmpCircularProgressIndicator size={22} strokeWidth={3} /><span style={subStyle}>Загружаем ещё…</span></React.Fragment> : <span style={subStyle}>Показано {shown.length} из {all.length}</span>}</div>}
      </div>
    );
    const emptyFirst = !pickMode && listSt === "ready" && mine.length === 0;
    const createBtn = <D.DBtn kind="filled" icon="add" text="Создать" onClick={() => setOv({ k: "create" })} style={{ minWidth: 140 }} />;
    const bar = pickMode
      ? <Bar title="Мои конфигурации" sub={"Выберите, что загрузить в " + BLK.model} onBack={pop} />
      : <Bar title="Конфигуратор" sub={wide ? "Настройки без подключённого блока" : null} onBack={pop} actions={wide && !emptyFirst ? createBtn : null} />;
    if (emptyFirst) return (
      <React.Fragment>{bar}
        <div className="vs-scroll" style={{ flex: 1, minHeight: 0, overflow: "auto" }}>
          <div style={{ maxWidth: 520, margin: "0 auto", padding: "32px 16px", display: "flex", flexDirection: "column", alignItems: "stretch", gap: 16 }}>
            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 12, textAlign: "center" }}><D.Hero icon="inventory_2" /><CmpText variant="titleLarge">Здесь будут ваши конфигурации</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Создайте первую: с нуля, из облака или из файла.</CmpText></div>
            <div style={{ borderRadius: 16, background: "var(--surface-container-low)", border: "1px solid var(--outline-variant)", overflow: "hidden" }}>{createItems.map((m) => <K.OptRow key={m.t} icon={m.i} title={m.t} sub={m.sub} onClick={m.f} />)}</div>
          </div>
        </div>
      </React.Fragment>
    );
    const listCol = <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>{header}<div className="vs-scroll" onScroll={onScroll} style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{body}</div></div>;
    return (
      <React.Fragment>
        {bar}
        {md ? (
          <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
            <div style={{ width: layout === "expanded" ? 420 : 340, flex: "0 0 auto", display: "flex", borderRight: "1px solid var(--outline-variant)" }}>{listCol}</div>
            <div style={{ flex: 1, minWidth: 0, overflow: "auto", background: "var(--surface-container-lowest, var(--background))" }}>{selC ? detail(selC) : null}</div>
          </div>
        ) : <div style={{ flex: 1, minHeight: 0, display: "flex", maxWidth: 760, width: "100%", margin: "0 auto" }}>{listCol}</div>}
        {!wide && !pickMode && listSt === "ready" && (
          <button type="button" onClick={() => setOv({ k: "create" })} style={{ position: "absolute", right: 16, bottom: 20, height: 56, padding: "0 22px 0 18px", borderRadius: 16, border: "none", background: "var(--primary-container)", color: "var(--on-primary-container)", font: "600 17px var(--font-sans)", display: "flex", alignItems: "center", gap: 10, boxShadow: "var(--elevation-3)", cursor: "pointer", zIndex: 5 }}><CmpIcon icon="add" size={26} />Создать</button>
        )}
      </React.Fragment>
    );
  };
  const detail = (c) => {
    const w = wa(c.model);
    return (
      <div style={{ maxWidth: 680, padding: layout === "expanded" ? "24px 32px" : "20px 24px", display: "flex", flexDirection: "column", gap: 16, boxSizing: "border-box" }}>
        <div style={{ display: "flex", gap: 8, alignItems: "center" }}><K.ModelBadge m={c.model} big /></div>
        <CmpText variant="headlineSmall" as="div" style={{ wordBreak: "break-word" }}>{c.name}</CmpText>
        <div style={{ display: "flex", flexWrap: "wrap", gap: "4px 16px", ...subStyle, fontWeight: 400, fontSize: 15 }}><span>Изменена {c.updated}</span><span>Создана {c.created}</span></div>
        {c.desc ? <CmpText variant="bodyLarge" as="div" style={{ whiteSpace: "pre-wrap" }}>{c.desc}</CmpText> : <CmpText variant="bodyLarge" color="var(--on-surface-variant)" as="div">Без описания</CmpText>}
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", paddingTop: 8 }}>
          <D.DBtn kind="filled" icon="tune" text="Открыть" onClick={() => openMine(c)} style={{ minWidth: 150 }} />
          {w !== "hide" && <D.DBtn kind="tonal" icon="upload" text="Записать в блок" disabled={w === "disable"} onClick={() => askDiff(c.name, mineVals(c))} />}
          <D.DBtn kind="outlined" icon="description" text="Сохранить в файл" onClick={() => saveFileFlow(c.model, c.name)} />
        </div>
        {w === "disable" && <span style={{ ...subStyle, fontWeight: 400 }}>Записать в блок: {wr(c.model).toLowerCase()}</span>}
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", borderTop: "1px solid var(--outline-variant)", paddingTop: 16 }}>
          <D.DBtn icon="edit" text="Изменить описание" onClick={() => setOv({ k: "form", mode: "rename", item: c })} />
          <D.DBtn icon="content_copy" text="Копия" onClick={() => copyMine(c)} />
          <D.DBtn icon="delete" text="Удалить" onClick={() => setOv({ k: "delete", c })} style={{ color: "var(--error)" }} />
        </div>
      </div>
    );
  };

  const screen = (e) => {
    switch (e.s) {
      case "list": case "pick": return listScreen();
      case "models": return <ModelsScreen purpose={e.purpose} st0={e.st0} wide={wide} blockOn={blockOn} onBack={pop} onPick={(m) => pickModel(e, m)} />;
      case "vehicles": return <window.VehicleSelectApp entry="E" sub={e.ctx === "block" ? `Конфигурации под ${BLK.model} · CAN ${BLK.can}` : "Автомобили с конфигурациями"} onPicked={(car) => push("cloud", { car, ctx: e.ctx })} onBack={pop} />;
      case "cloud": return <CloudScreen car={e.car} ctx={e.ctx} net0={e.net0 || net} empty={e.empty} initSel={e.initSel} layout={layout} blockOn={blockOn} writeMode={writeMode} onBack={pop} mkAct={cloudAct} />;
      case "editor": return <window.DeviceSettingsScreen source={e.src} baseValues={e.values} initialEdits={e.initialEdits} seedEdits={false} demo={e.demo} writeAvail={wa(e.src.model)} writeReason={wr(e.src.model)} onAction={edAction(e)} apiRef={e.api} onBack={pop} />;
      case "block": return <window.DeviceSettingsScreen baseValues={BLOCK_VALS} initialEdits={e.initialEdits} seedEdits={false} toast={e.toast} onAction={blockAction} apiRef={e.api} onBack={pop} />;
      default: return null;
    }
  };

  // ── оверлеи
  let over = null;
  const close = () => setOv(null);
  if (ov) {
    if (ov.k === "create") over = <K.Sheet wide={wide} title="Создать конфигурацию" onClose={close}>{createItems.map((m) => <K.OptRow key={m.t} icon={m.i} title={m.t} sub={m.sub} onClick={m.f} />)}</K.Sheet>;
    else if (ov.k === "load") over = (
      <K.Sheet wide={wide} title="Загрузить из…" sub={`Значения подставятся в редактор ${BLK.model}. В блок они запишутся только после «Записать».`} onClose={close}>
        <K.OptRow icon="inventory_2" title="Мои конфигурации" sub={`Сохранённые для ${BLK.model}`} onClick={() => { close(); push("pick"); }} />
        <K.OptRow icon="cloud" title="Облако" sub="Готовые конфигурации для автомобиля" onClick={() => { close(); push("vehicles", { ctx: "block" }); }} />
        <K.OptRow icon="description" title="Файл" sub=".dxs с телефона или компьютера" onClick={() => openFile("blockLoad")} />
        <K.OptRow icon="factory" title="Заводские настройки модели" sub={`Настройки ${BLK.model} «из коробки»`} onClick={() => askDiff(`Заводские настройки ${BLK.model}`, D.BASE0)} />
      </K.Sheet>
    );
    else if (ov.k === "saveTo") over = (
      <K.Sheet wide={wide} title="Сохранить в…" sub={"Модель блока " + ov.model} onClose={close}>
        <K.OptRow icon="inventory_2" title="Мои конфигурации" sub="В приложении на этом устройстве" onClick={() => setOv({ ...ov, k: "form", mode: "new" })} />
        <K.OptRow icon="description" title="Файл" sub="Файл .dxs — передать коллеге или на компьютер" onClick={() => saveFileFlow(ov.model, ov.name)} />
      </K.Sheet>
    );
    else if (ov.k === "form") over = <SaveForm wide={wide} mode={ov.mode} init={ov.mode === "rename" ? ov.item : { name: ov.name, desc: ov.desc }} model={ov.model} onCancel={close} onSubmit={(data) => submitForm(ov, data)} />;
    else if (ov.k === "mf") over = (
      <K.Sheet wide={wide} title="Модель блока" onClose={close}>
        {[null, ...models].map((m) => <K.OptRow key={m || "all"} icon={m ? "memory" : "select_all"} title={m || "Все модели"} sub={m ? `${mine.filter((c) => c.model === m).length} шт.` : `${mine.length} шт.`} onClick={() => { setMf(m); close(); }} trail={mf === m ? <CmpIcon icon="check" size={24} color="var(--primary)" /> : null} />)}
      </K.Sheet>
    );
    else if (ov.k === "cardMenu") {
      const items = cardMenuItems(ov.c).map((m) => <K.OptRow key={m.t} icon={m.i} title={m.t} sub={m.sub} disabled={m.off} danger={m.danger} onClick={m.f} />);
      over = ov.at ? <K.Popover at={ov.at} onClose={close} w={340}>{items}</K.Popover> : <K.Sheet wide={false} title={ov.c.name} onClose={close}>{items}</K.Sheet>;
    }
    else if (ov.k === "delete") over = (
      <D.Dlg icon="delete" tone="error" title={`Удалить «${ov.c.name}»?`} onClose={close}
        actions={<React.Fragment><D.DBtn text="Отмена" onClick={close} /><D.DBtn kind="danger" text="Удалить" onClick={() => { setMine((l) => l.filter((x) => x.id !== ov.c.id)); close(); setSnack({ m: "Конфигурация удалена" }); }} /></React.Fragment>}>
        <CmpText variant="bodyLarge">Отменить удаление нельзя.</CmpText>
      </D.Dlg>
    );
    else if (ov.k === "overwrite") {
      const e = ov.e || stack.find((x) => x.s === "editor" && x.src.kind === "file");
      over = (
        <D.Dlg icon="save" title={`Перезаписать файл «${e ? e.src.title : CD.FILE.name}»?`} onClose={close}
          actions={<React.Fragment><D.DBtn text="Отмена" onClick={close} /><D.DBtn kind="filled" text="Перезаписать" onClick={() => { close(); run("Сохраняем файл…", null, 700, () => { if (saveFail) return setOv({ k: "err", kind: "fileWrite" }); e && e.api.current && e.api.current.commit(); setSnack({ m: "Файл перезаписан" }); if (ov.exit) pop(); }); }} /></React.Fragment>}>
          <CmpText variant="bodyLarge">Старые значения в файле будут заменены. Отменить это нельзя.</CmpText>
        </D.Dlg>
      );
    }
    else if (ov.k === "sys") over = <K.SysFile mode={ov.mode} name={ov.name} wide={wide} onCancel={close} onPick={() => {
      const o = ov; close();
      if (o.mode === "open") return afterFile(o.then);
      run("Сохраняем файл…", null, 700, () => saveFail ? setOv({ k: "err", kind: "fileWrite", name: o.name }) : setSnack({ m: `Сохранено в файл «${o.name}»` }));
    }} />;
    else if (ov.k === "viewer") over = <VK.PhotoViewer count={ov.n} idx={ov.i} onIdx={(i) => setOv({ ...ov, i })} onClose={close} label={ov.label} />;
    else if (ov.k === "err") {
      const E = {
        corrupt: { i: "broken_image", t: "Файл повреждён или это не файл настроек", x: "Выберите файл .dxs, сохранённый в приложении «Специалист» или в старой версии.", a: [<D.DBtn key="c" text="Закрыть" onClick={close} />, <D.DBtn key="o" kind="filled" text="Выбрать другой файл" onClick={() => openFile(ov.inBlock ? "blockLoad" : "file")} />] },
        mismatch: { i: "memory", t: `Настройки не подходят для модели ${ov.m}`, x: "Файл сохранён для другой модели блока — выберите другую модель.", a: [<D.DBtn key="c" text="Отмена" onClick={() => { close(); popTo("list"); }} />, <D.DBtn key="o" kind="filled" text="Выбрать модель" onClick={close} />] },
        badBlock: { i: "memory", t: "Настройки не подходят для этого блока", x: `Файл сохранён для другой модели или с другим набором настроек. В редакторе ${BLK.model} ничего не изменилось.`, a: [<D.DBtn key="c" text="Закрыть" onClick={close} />, <D.DBtn key="o" kind="filled" text="Другой источник" onClick={() => setOv({ k: "load" })} />] },
        can: { i: "warning", t: "Не подходит прошивка CAN", x: `В блоке прошивка для CAN-кода ${BLK.can}, конфигурация рассчитана на ${ov.can}. Обновите прошивку под ${ov.can}.`, a: [<D.DBtn key="c" text="Закрыть" onClick={close} />, <D.DBtn key="o" kind="filled" icon="system_update" text="Обновить прошивку" onClick={() => window.psGo && window.psGo("firmware")} />] },
        save: { i: "error_outline", t: "Не удалось сохранить", x: "Недостаточно места или хранилище приложения занято. Ничего не потеряно — повторите.", a: [<D.DBtn key="c" text="Отмена" onClick={close} />, <D.DBtn key="o" kind="filled" icon="refresh" text="Повторить" onClick={() => ov.retry ? ov.retry() : close()} />] },
        fileWrite: { i: "error_outline", t: "Не удалось записать файл", x: "Нет доступа к папке или закончилось место. Выберите другое место и повторите.", a: [<D.DBtn key="c" text="Отмена" onClick={close} />, <D.DBtn key="o" kind="filled" icon="refresh" text="Повторить" onClick={() => setOv({ k: "sys", mode: "save", name: ov.name || fname(BLK.model) })} />] },
      }[ov.kind];
      over = <D.Dlg icon={E.i} tone="error" title={E.t} onClose={close} actions={<React.Fragment>{E.a}</React.Fragment>}><CmpText variant="bodyLarge">{E.x}</CmpText></D.Dlg>;
    }
    else if (ov.k === "diff") {
      const d = ov.phase === "ready" ? K.diffRows(ov.cur, ov.vals) : null;
      const none = d && d.total === 0;
      const dst = ov.ctx === "block" ? "редактор " + BLK.model : "блок " + BLK.model;
      const footer = (
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {d && !none && <span style={{ ...subStyle, fontWeight: 400 }}>Значения подставятся в редактор. В блок они запишутся только после «Записать».</span>}
          <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
            {none || !d ? <D.DBtn kind={none ? "filled" : "text"} text={none ? "Закрыть" : "Отмена"} onClick={close} style={wide ? {} : { flex: 1 }} /> : (
              <React.Fragment><D.DBtn kind="outlined" text="Отмена" onClick={close} style={wide ? {} : { flex: 1 }} /><D.DBtn kind="filled" icon="download" text="Загрузить" onClick={confirmDiff} style={wide ? { minWidth: 160 } : { flex: 1 }} /></React.Fragment>
            )}
          </div>
        </div>
      );
      over = (
        <K.FullDlg wide={wide} w={820} title="Что изменится" sub={`«${ov.title}» → ${dst}`} onClose={close} footer={footer}>
          {ov.phase === "busy" && <D.Center><CmpCircularProgressIndicator /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Сравниваем с настройками блока…</CmpText></D.Center>}
          {ov.phase === "error" && <D.Center><D.Hero icon="error_outline" tone="error" /><CmpText variant="titleLarge">Не удалось прочитать конфигурацию</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Данные повреждены или не загрузились до конца.</CmpText><D.DBtn kind="filled" icon="refresh" text="Повторить" onClick={() => setOv({ ...ov, phase: "busy", hold: false })} /></D.Center>}
          {none && <D.Center><D.Hero icon="check_circle" /><CmpText variant="titleLarge">Настройки совпадают</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">В редакторе уже такие значения — загружать нечего.</CmpText></D.Center>}
          {d && !none && (
            <React.Fragment>
              <div style={{ padding: wide ? "4px 24px 8px" : "4px 16px 8px", display: "flex", flexDirection: "column", gap: 10 }}>
                <CmpText variant="titleMedium" as="div">{K.diffSummary(d)}</CmpText>
                {ov.pending > 0 && <div style={{ display: "flex", gap: 12, padding: "12px 16px", borderRadius: 16, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)" }}><CmpIcon icon="warning" size={24} /><CmpText variant="bodyMedium">В редакторе есть незаписанные правки ({ov.pending}). После загрузки они пропадут.</CmpText></div>}
              </div>
              <K.DiffView d={d} wide={wide} />
            </React.Fragment>
          )}
        </K.FullDlg>
      );
    }
  }

  return (
    <div ref={ref} style={{ position: "relative", height: "100%", overflow: "hidden", background: "var(--background)", color: "var(--on-background)" }}>
      {stack.map((e, i) => <div key={e.key} style={{ position: "absolute", inset: 0, display: i === stack.length - 1 ? "flex" : "none", flexDirection: "column", background: "var(--background)" }}>{screen(e)}</div>)}
      {over}
      {busy && <K.Progress title={busy.title} text={busy.text} />}
      {snack && <K.Toast msg={snack.m} action={snack.a} onAction={() => { setSnack(null); snack.f && snack.f(); }} bottom={!wide && (top.s === "list") ? 92 : 16} />}
    </div>
  );
}

// ── Панель витрины
const CFG_PRESETS = [
  ["K1", [["", "Список"], ["list-search", "Поиск"], ["list-filter", "Фильтр"], ["list-nothing", "Не найдено"], ["list-empty", "Пусто"], ["list-loading", "Загрузка"], ["list-error", "Ошибка"], ["card-menu", "Меню ⋮"], ["create", "+ Создать"], ["delete", "Удалить"], ["rename", "Переименовать"]]],
  ["K2–K4", [["models", "K2 Модель"], ["models-loading", "K2 Загрузка"], ["models-error", "K2 Ошибка"], ["file-sys", "K3 Сист. окно"], ["file-model", "K3 Модель"], ["file-corrupt", "K3 Повреждён"], ["file-mismatch", "K3 Не та модель"], ["vehicles", "K4 Автомобиль"], ["cloud-list", "K4 Список"], ["cloud-card", "K4 Карточка"], ["cloud-can", "K4 CAN ≠"], ["cloud-bare", "K4 Без вложений"], ["viewer", "K4 Фото"], ["cloud-empty", "K4 Пусто"], ["cloud-offline", "K4 Нет сети"]]],
  ["K5–K6", [["ed-mine", "K5 Мои"], ["ed-file", "K5 Файл"], ["ed-cloud", "K5 Облако"], ["ed-factory", "K5 Заводские"], ["ed-mine-menu", "⋯ Мои"], ["ed-file-menu", "⋯ Файл"], ["ed-cloud-menu", "⋯ Облако"], ["ed-ro-edits", "Сохранить как…"], ["ed-unsaved", "Выход (мои)"], ["ed-ro-unsaved", "Выход (облако)"], ["overwrite", "Перезапись"], ["save-to", "K6 Сохранить в…"], ["save-form", "K6 Форма"], ["saving", "K6 Прогресс"], ["saved", "K6 Успех"], ["save-error", "K6 Ошибка"]]],
  ["K7/K9", [["blk", "Редактор блока"], ["load-sheet", "Загрузить из…"], ["pick", "Мои (выбор)"], ["pick-cloud", "Облако → блок"], ["diff-few", "K9 Мало"], ["diff-many", "K9 Много"], ["diff-none", "K9 Совпадают"], ["diff-pending", "K9 Есть правки"], ["diff-busy", "K9 Сравнение"], ["diff-error", "K9 Ошибка"], ["loading", "Загрузка"], ["loaded", "Результат"], ["load-can", "CAN ≠"], ["load-bad", "Не подходит"], ["save-to-blk", "Сохранить в…"], ["write-home", "Записать из K1"]]],
];
function useCfgCtl() {
  const [dark, setDarkRaw] = useState(() => { try { const v = localStorage.getItem("ps-theme"); return v ? v === "dark" : matchMedia("(prefers-color-scheme: dark)").matches; } catch (e) { return false; } });
  const setDark = (v) => { setDarkRaw(v); try { localStorage.setItem("ps-theme", v ? "dark" : "light"); } catch (e) {} };
  useEffect(() => { document.body.classList.toggle("page-dark", dark); }, [dark]);
  const qs = new URLSearchParams(location.search);
  const go = qs.get("go");
  const [demo, setDemo] = useState(go === "load" ? "load-sheet" : go === "saveTo" ? "save-to-blk" : qs.get("s") || "");
  const [entry, setEntry] = useState(qs.get("entry") || "home");
  const [blockOn, setBlockOn] = useState(true);
  const [writeMode, setWriteMode] = useState("hide");
  const [net, setNet] = useState("ok");
  const [fileState, setFileState] = useState("ok");
  const [saveFail, setSaveFail] = useState(false);
  const [blockEdits, setBlockEdits] = useState(false);
  const [grp, setGrp] = useState(() => (CFG_PRESETS.find((g) => g[1].some((x) => x[0] === demo)) || CFG_PRESETS[0])[0]);
  const Seg = window.VsSeg;
  const bar = (
    <React.Fragment>
      <button type="button" className="theme-fab" onClick={() => setDark(!dark)} aria-label={dark ? "Светлая тема" : "Тёмная тема"}>{dark ? "☀" : "☾"}</button>
      <div className="toolbar">
        <Seg items={[["l", "Светлая"], ["d", "Тёмная"]]} value={dark ? "d" : "l"} onChange={(x) => setDark(x === "d")} />
        <Seg items={[["home", "A · Главная → Конфигуратор"], ["block", "B/C · Редактор блока"]]} value={entry} onChange={(x) => { setEntry(x); setDemo(x === "block" ? "blk" : ""); }} />
        <Seg items={[["on", "Блок DX-9X подключён"], ["off", "Блок не подключён"]]} value={blockOn ? "on" : "off"} onChange={(x) => setBlockOn(x === "on")} />
        <Seg items={[["hide", "Нет блока: скрыть «Записать»"], ["disable", "неактивен + пояснение"]]} value={writeMode} onChange={setWriteMode} />
      </div>
      <div className="toolbar">
        <Seg items={[["ok", "Облако: ОК"], ["loading", "Загрузка"], ["error", "Ошибка"], ["offline", "Нет интернета"]]} value={net} onChange={setNet} />
        <Seg items={[["ok", "Файл: исправен"], ["corrupt", "повреждён"], ["other", "другой модели"]]} value={fileState} onChange={setFileState} />
        <Seg items={[["n", "Сохранение: успех"], ["y", "ошибка"]]} value={saveFail ? "y" : "n"} onChange={(x) => setSaveFail(x === "y")} />
        <Seg items={[["n", "Правок в блоке нет"], ["y", "есть"]]} value={blockEdits ? "y" : "n"} onChange={(x) => setBlockEdits(x === "y")} />
      </div>
      <div className="toolbar">
        <Seg items={CFG_PRESETS.map((g) => [g[0], g[0]])} value={grp} onChange={setGrp} />
        <div className="seg wrap">{CFG_PRESETS.find((g) => g[0] === grp)[1].map(([k, l]) => <button key={k} className={demo === k ? "on" : ""} onClick={() => setDemo(k)}>{l}</button>)}</div>
      </div>
    </React.Fragment>
  );
  const props = { entry, demo, blockOn, writeMode, net, fileState, saveFail, blockEdits };
  return { dark, props, bar, key: [entry, demo, blockOn, writeMode, net, fileState, saveFail, blockEdits].join() };
}

Object.assign(window, { ConfiguratorApp, useCfgCtl, CFG_PRESETS, CFG: { resolve, BLOCK_VALS, valsOf, CLOUD } });
})();
