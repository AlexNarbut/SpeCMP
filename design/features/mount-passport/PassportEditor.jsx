// PassportEditor.jsx — «Паспорт монтажа»: предотчёт (редактор паспорта), телефон.
// Монтажник не заполняет, а проверяет и дополняет: данные блока и мастера — готовые,
// ручные поля — необязательные и не выглядят упрёком, если пусты.
//
// PassportEditor props:
//   scenario  "fast" (быстрый монтаж, всё само) | "ext" (расширенный, работы галочками, USB)
//             | "partial" (часть не прочитана → «Перечитать») | "noblock" (паспорт без блока)
//   nav       "accordion" (секции свёрнуты, итог в строку) | "chips" (липкие чипы-якоря)
//   fill      "quiet" (само — текст; дополнить — тихие строки «+ Добавить») | "fields" (само — карточки; дополнить — поля)
//   onPdf     () => void

const PE_DS = window.PandoraSpecDesignSystem_b8d6e1;
const CmpText = (p) => <PE_DS.CmpText {...p} style={{ display: "block", ...p.style }} />;
const { CmpIcon, CmpButton, CmpOutlinedButton, CmpTextField, CmpCheckbox, CmpChip, CmpIconButton, CmpTopAppBar, CmpCircularProgressIndicator } = PE_DS;
const PeTextBtn = (p) => (PE_DS.CmpTextButton ? <PE_DS.CmpTextButton {...p} /> : <CmpOutlinedButton {...p} />);

(function peCss() {
  if (document.getElementById("pe-css")) return;
  const s = document.createElement("style");
  s.id = "pe-css";
  s.textContent =
    ".pe-tap{transition:background-color 150ms ease,transform 160ms cubic-bezier(.23,1,.32,1)}" +
    "@media (hover:hover) and (pointer:fine){.pe-tap:hover{background-color:var(--background-surface-ultra-highlight)}}" +
    ".pe-tap:active{transform:scale(.985)}" +
    ".pe-tap:focus-visible{outline:2px solid var(--border-focus);outline-offset:-2px}" +
    ".pe-scroll::-webkit-scrollbar{display:none}" +
    ".pe-area::placeholder{color:var(--foreground-on-dark-secondary-default);opacity:1}" +
    "@keyframes peSpin{to{transform:rotate(360deg)}}" +
    "@keyframes peIn{from{opacity:0;transform:translateY(-4px)}to{opacity:1;transform:none}}" +
    "@keyframes peFade{from{opacity:0}to{opacity:1}}" +
    "@keyframes peSheet{from{transform:translateY(100%)}to{transform:none}}" +
    "@media (prefers-reduced-motion:reduce){.pe-tap,.pe-anim{transition:none!important;animation:none!important}}";
  document.head.appendChild(s);
})();

const PE_EASE = "cubic-bezier(.23,1,.32,1)";
const PE_ICONS = "../peripheral-devices/icons/";

// ── Демо-данные ───────────────────────────────────────────────────────────────
const PE = {
  no: "20261119-234567-1", date: "19 ноября 2026, 14:05",
  vehicle: { name: "Kia Sportage", years: "2021–2024" },
  eqSummary: "Pandora DX-91 · ID 1234567 · ПО 3.57",
  eq: [
    ["Модель", "Pandora DX-91"], ["ID устройства", "1234567"], ["Серийный номер", "DX91-0042-118734"],
    ["ПО основное", "3.57"], ["Загрузчик", "1.12"], ["CAN", "12.05 · код 1234"], ["Bluetooth", "2.8"], ["IMEI", "861234050118734"],
  ],
  eqParams: ["Серийный номер", "Загрузчик", "Bluetooth", "IMEI"],
  eqManual: [["Модель", "Pandora DX-91"], ["ID устройства", "1234567"], ["Серийный номер", "DX91-0042-118734"]],
  cfg: { name: "Kia Sportage 2022 · автозапуск", author: "Pandora" },
  settingsAt: "19.11.2026 14:05",
  worksFast: [["Настройки записаны", ""], ["Обход иммобилайзера", "потребовался Clone"], ["Clone", "выполнен"], ["Slave", "обучение выполнено"], ["Время монтажа", "1 ч 25 мин"]],
  worksExt: ["Настройки записаны", "Обход иммобилайзера", "Clone", "Slave"],
  checks: [
    ["Постановка на охрану с брелока", "ok"], ["Снятие с охраны с брелока", "ok"], ["Открытие дверей", "ok"],
    ["Закрытие дверей", "ok"], ["Багажник", "ok"], ["Концевик капота", "ok"], ["Датчик удара", "skip"],
    ["Автозапуск двигателя", "fail"], ["Блокировка двигателя", "ok"], ["Метка в зоне", "ok"], ["Метка вне зоны", "ok"],
  ],
  devices: [
    ["token.svg", "Метка BT-760", "№ 1", "4C:11:AE:9F:20:01", "1.24"], ["token.svg", "Метка BT-760", "№ 2", "4C:11:AE:9F:20:02", "1.24"],
    ["keychain_watch.svg", "Брелок D-030", "№ 1", "4C:11:AE:12:7B:44", "2.03"], ["rhm.svg", "Модуль RHM-03", "№ 1", "4C:11:AE:3A:05:90", "1.8"],
    ["hood_lock.svg", "Замок капота", "№ 1", "4C:11:AE:3A:05:91", "1.05"],
  ],
  master: { name: "Алексей Смирнов", contact: "+7 912 345-67-89" },
};
const PE_CHECK = {
  ok: { icon: "check_circle", word: "Пройдено", color: "var(--success-default)" },
  fail: { icon: "cancel", word: "Не пройдено", color: "var(--error-default)" },
  skip: { icon: "do_not_disturb_on", word: "Пропущено", color: "var(--foreground-on-dark-secondary-default)" },
};
const PE_SECTIONS = [
  ["vehicle", "Автомобиль", "directions_car"], ["eq", "Оборудование", "memory"], ["cfg", "Конфигурация", "tune"],
  ["works", "Выполненные работы", "build"], ["check", "Проверка системы", "fact_check"], ["devices", "Устройства системы", "sensors"],
  ["photos", "Фото", "photo_camera"], ["notes", "Замечания", "notes"], ["client", "Клиент", "person"], ["master", "Исполнитель", "badge"],
];

const peVinErr = (v) => (v && !/^[A-HJ-NPR-Z0-9]{17}$/i.test(v) ? "VIN — 17 символов: латинские буквы и цифры" : null);
const peDigErr = (v) => (v && !/^\d+$/.test(v.replace(/\s/g, "")) ? "Пробег — только цифры" : null);
const peMuted = "var(--foreground-on-dark-secondary-default)";

// ── Мелкие кирпичи ────────────────────────────────────────────────────────────
function PeDevIcon({ file, size = 28 }) {
  const url = `url(${PE_ICONS}${file})`;
  return <span aria-hidden style={{ width: size, height: size, flex: "none", background: "var(--on-surface)", WebkitMask: `${url} center/contain no-repeat`, mask: `${url} center/contain no-repeat` }} />;
}

function PeSpinner({ size = 20 }) {
  return <span className="pe-anim" style={{ width: size, height: size, borderRadius: "50%", border: "2.5px solid var(--border-light)", borderTopColor: "var(--primary)", animation: "peSpin .7s linear infinite", flex: "none", boxSizing: "border-box" }} />;
}

// Готовые данные — «ключ — значение».
function PeKV({ label, value, mono, last, dense }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 4, padding: dense ? "10px 0" : "12px 0", borderBottom: last ? "none" : "1px solid var(--border-ultra-highlight)" }}>
      <CmpText variant="additional0" color={peMuted} weight={500}>{label}</CmpText>
      <CmpText variant="headline3" style={mono ? { fontFamily: "var(--font-mono)", fontWeight: 500 } : undefined}>{value}</CmpText>
    </div>
  );
}

// Компактная строка для длинных однотипных списков (оборудование): метка слева, значение справа.
function PeLine({ label, value, last, warn }) {
  return (
    <div style={{ minHeight: 44, display: "flex", alignItems: "center", gap: 12, borderBottom: last ? "none" : "1px solid var(--border-ultra-highlight)" }}>
      <CmpText variant="paragraph2" color={peMuted} style={{ flex: "0 0 132px" }}>{label}</CmpText>
      <CmpText variant="headline3" color={warn ? peMuted : undefined} style={{ flex: 1, minWidth: 0, textAlign: "right", fontWeight: warn ? 500 : 700 }}>{value}</CmpText>
    </div>
  );
}

// Карточка «заполнено само» (вариант fields).
function PeAutoCard({ children, source }) {
  return (
    <div style={{ background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)", borderRadius: 16, padding: "4px 16px 4px" }}>
      {source && (
        <div style={{ display: "flex", alignItems: "center", gap: 6, padding: "10px 0 2px" }}>
          <CmpIcon icon="lock" size={16} color={peMuted} />
          <CmpText variant="additional1" color={peMuted}>{source}</CmpText>
        </div>
      )}
      {children}
    </div>
  );
}

// Ручное поле в двух подачах: quiet — тихая строка «+ Добавить …», по тапу становится полем;
// fields — всегда видимое поле с подписью «необязательно».
function PeField({ fill, label, addLabel, value, onChange, err, hint, inputMode, placeholder, trailing }) {
  const [edit, setEdit] = React.useState(false);
  const [touched, setTouched] = React.useState(false);
  const shownErr = touched && err;
  const field = (
    <div onBlur={() => { setTouched(true); if (!value) setEdit(false); }} onFocus={() => setTouched(false)}>
      <CmpTextField label={label} value={value} onChange={onChange} isError={!!shownErr} placeholder={placeholder}
        supportingText={shownErr || hint || (fill === "fields" ? "Необязательно" : undefined)} inputMode={inputMode} autoFocus={fill === "quiet" && edit && !value}
        trailingIcon={trailing} />
    </div>
  );
  if (fill === "fields" || edit || value) return field;
  return (
    <button className="pe-tap" onClick={() => setEdit(true)} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 52, display: "flex", alignItems: "center", gap: 12, padding: "0 12px", margin: "0 -12px", borderRadius: 12, cursor: "pointer", color: "var(--primary)" }}>
      <CmpIcon icon="add" size={24} color="var(--primary)" />
      <CmpText variant="headline3" color="var(--primary)" style={{ flex: 1 }}>{addLabel || label}</CmpText>
    </button>
  );
}

function PeNote({ icon, children, tone }) {
  const c = tone === "warn" ? "var(--warning-default)" : peMuted;
  return (
    <div style={{ display: "flex", gap: 10, alignItems: "flex-start", padding: "4px 0" }}>
      <CmpIcon icon={icon} size={20} color={c} style={{ marginTop: 1 }} />
      <CmpText variant="paragraph3" color={peMuted} style={{ flex: 1 }}>{children}</CmpText>
    </div>
  );
}

// Строка-проблема с «Перечитать».
function PeReread({ text, state, onRetry, linked = true }) {
  if (state === "ok") return null;
  if (!linked) return (
    <div style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "12px 14px", borderRadius: 12, background: "var(--background-surface-highlight)" }}>
      <CmpIcon icon="bluetooth_disabled" size={22} color={peMuted} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="paragraph2">{text}</CmpText>
        <CmpText variant="paragraph3" color={peMuted} style={{ marginTop: 2 }}>Перечитать можно при подключённом блоке</CmpText>
      </div>
    </div>
  );
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "8px 8px 8px 14px", borderRadius: 12, background: "var(--warning-highlight)" }}>
      <CmpIcon icon="warning" size={22} color="var(--warning-default)" />
      <CmpText variant="paragraph2" style={{ flex: 1 }}>{state === "loading" ? "Читаем…" : text}</CmpText>
      {state === "loading" ? <span style={{ padding: "0 14px" }}><PeSpinner /></span> : <PeTextBtn text="Перечитать" icon="refresh" onClick={onRetry} />}
    </div>
  );
}

// Сворачиваемый блок внутри секции (оборудование и проверка в режиме чипов).
function PeFold({ summary, children, defaultOpen }) {
  const [open, setOpen] = React.useState(!!defaultOpen);
  return (
    <div style={{ background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)", borderRadius: 16, overflow: "hidden" }}>
      <button className="pe-tap" onClick={() => setOpen(!open)} aria-expanded={open} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 56, padding: "8px 8px 8px 16px", display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }}>
        <div style={{ flex: 1, minWidth: 0 }}>{summary}</div>
        <span style={{ width: 48, height: 48, display: "grid", placeItems: "center", transform: open ? "rotate(180deg)" : "none", transition: `transform 220ms ${PE_EASE}` }}><CmpIcon icon="expand_more" size={24} color={peMuted} /></span>
      </button>
      {open && <div className="pe-anim" style={{ padding: "0 16px 8px", animation: `peIn 200ms ${PE_EASE}` }}>{children}</div>}
    </div>
  );
}

// ── Экран ─────────────────────────────────────────────────────────────────────
// linked — подключён тот же блок: «Перечитать» доступно. Без блока (архив) правятся только ручные поля.
// onReadBlock — паспорт без блока, а блок с этим ID подключён: «Считать блок».
// platform — mobile | tablet | desktop (фото: камера/галерея или файл);  photosInit — none | three | eight | loading;  cameraDenied — демо.
function PassportEditor({ scenario = "fast", nav = "accordion", fill = "fields", onPdf, initialOpen, embedded = false, onChange, onBack, linked = true, onReadBlock, platform = "mobile", photosInit = "none", cameraDenied = false, noSpace = false }) {
  const fast = scenario === "fast" || scenario === "partial";
  const noblock = scenario === "noblock";
  const ext = scenario === "ext" || noblock;
  const [v, setV] = React.useState({ vin: "", plate: "", mileage: "", notes: "", client: "", phone: "", org: "", addr: "", cfgName: "", manualCar: "" });
  const [works, setWorks] = React.useState({ "Настройки записаны": true });
  const PH_CAPS = ["Блок под панелью приборов", "", "Реле блокировки", "Сирена под капотом", "", "Модуль RHM-03", "Метки и брелок", "Пломба на разъёме"];
  const [photos, setPhotos] = React.useState(() => {
    const n = { three: 3, eight: 8, loading: 3 }[photosInit] || 0;
    return Array.from({ length: n }, (_, i) => ({ id: i + 1, cap: PH_CAPS[i], loading: photosInit === "loading" && i === n - 1 }));
  });
  const [sheet, setSheet] = React.useState(null); // null | "pick" | "denied"
  const [viewer, setViewer] = React.useState(null); // индекс фото
  const desk = platform === "desktop";
  const [rr, setRr] = React.useState(() => scenario === "partial" ? { params: "fail", devices: "fail", settings: "fail" } : { params: "ok", devices: "ok", settings: "ok" });
  const [saved, setSaved] = React.useState("saved");
  const [open, setOpen] = React.useState(() => new Set(initialOpen || ["vehicle"]));
  const [menu, setMenu] = React.useState(false);
  const [active, setActive] = React.useState("vehicle");
  const scrollRef = React.useRef(null), chipsRef = React.useRef(null), secRefs = React.useRef({});
  const saveT = React.useRef(null);

  const touch = () => { setSaved("saving"); clearTimeout(saveT.current); saveT.current = setTimeout(() => setSaved("saved"), 700); };
  const set = (k) => (val) => { setV((x) => ({ ...x, [k]: val })); touch(); };
  const reread = (k) => { setRr((x) => ({ ...x, [k]: "loading" })); setTimeout(() => setRr((x) => ({ ...x, [k]: "ok" })), 1400); };
  const addPhoto = () => { if (photos.length >= 8) return; desk ? pushPhoto() : setSheet("pick"); };
  const [photoErr, setPhotoErr] = React.useState(null);
  const pushPhoto = () => {
    setSheet(null);
    if (noSpace) { setPhotoErr("Недостаточно места, фото не сохранено. Освободите память устройства и повторите."); return; }
    setPhotoErr(null);
    const id = Date.now();
    setPhotos((p) => [...p, { id, loading: true, cap: "" }]);
    setTimeout(() => setPhotos((p) => p.map((x) => (x.id === id ? { ...x, loading: false } : x))), 900);
    touch();
  };
  React.useEffect(() => { onChange && onChange({ scenario, v, photos: photos.filter((p) => !p.loading).length, caps: photos.filter((p) => !p.loading).map((p) => p.cap), works }); }, [v, photos, works]);
  const toggle = (id) => setOpen((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });

  const sections = PE_SECTIONS.filter(([id]) => !(id === "check" && !fast) && !(id === "devices" && noblock));

  // Итог секции в одну строку (заголовок аккордеона и чипы).
  const filledWorks = Object.values(works).filter(Boolean).length;
  const checkOk = PE.checks.filter((c) => c[1] === "ok").length;
  const sum = {
    vehicle: ext && !v.manualCar ? null : [fast ? `${PE.vehicle.name} · ${PE.vehicle.years}` : v.manualCar, v.plate].filter(Boolean).join(" · "),
    eq: noblock ? "Pandora DX-91 · ID 1234567 · внесено вручную" : PE.eqSummary,
    cfg: fast ? PE.cfg.name : (v.cfgName || "Настройки блока сохранены"),
    works: fast ? "Clone, Slave · 1 ч 25 мин" : filledWorks ? `Отмечено: ${filledWorks}` : null,
    check: `Пройдено ${checkOk} из ${PE.checks.length}`,
    devices: rr.devices === "ok" ? `${PE.devices.length} устройств` : "Не удалось прочитать",
    photos: photos.length ? `${photos.length} из 8` : null,
    notes: v.notes ? v.notes : null,
    client: [v.client, v.phone].filter(Boolean).join(" · ") || null,
    master: [PE.master.name, v.org].filter(Boolean).join(" · "),
  };
  const warn = { eq: rr.params === "fail", devices: rr.devices === "fail", cfg: rr.settings === "fail" };

  // ── Тела секций ──
  const autoWrap = (children, source) => (fill === "fields" ? <PeAutoCard source={source}>{children}</PeAutoCard> : <div>{children}</div>);

  const body = {
    vehicle: (
      <div style={{ display: "flex", flexDirection: "column", gap: fill === "fields" ? 16 : 4 }}>
        {fast && autoWrap(<React.Fragment><PeKV label="Марка и модель" value={PE.vehicle.name} /><PeKV label="Годы выпуска" value={PE.vehicle.years} last /></React.Fragment>, "Из мастера монтажа")}
        {ext && !v.manualCar && (
          <div style={{ display: "flex", flexDirection: "column", gap: 4, paddingBottom: 4 }}>
            <CmpOutlinedButton text="Выбрать автомобиль" icon="directions_car" fullWidth onClick={() => { set("manualCar")("Toyota RAV4 · 2019–2024"); }} />
          </div>
        )}
        {ext && v.manualCar && <PeField fill={fill} label="Автомобиль" value={v.manualCar} onChange={set("manualCar")} />}
        <PeField fill={fill} label="Госномер" addLabel="Госномер" value={v.plate} onChange={set("plate")} />
        <PeField fill={fill} label="VIN" value={v.vin} onChange={(x) => set("vin")(x.toUpperCase())} err={peVinErr(v.vin)} />
        <PeField fill={fill} label="Пробег, км" addLabel="Пробег" value={v.mileage} onChange={set("mileage")} err={peDigErr(v.mileage)} inputMode="numeric" />
      </div>
    ),
    eq: noblock ? (
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {autoWrap(PE.eqManual.map(([l, val], i, a) => <PeLine key={l} label={l} value={val} last={i === a.length - 1} />))}
        <PeNote icon="edit_note">Данные внесены вручную, блок не считывался</PeNote>
      </div>
    ) : (
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {autoWrap(PE.eq.map(([l, val], i, a) => {
          const miss = rr.params !== "ok" && PE.eqParams.includes(l);
          return <PeLine key={l} label={l} value={miss ? "—" : val} warn={miss} last={i === a.length - 1} />;
        }), "Считано с блока · 14:02")}
        <PeReread text="Не удалось прочитать параметры" state={rr.params} onRetry={() => reread("params")} linked={linked} />
      </div>
    ),
    cfg: (
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {fast ? autoWrap(<React.Fragment><PeKV label="Название" value={PE.cfg.name} /><PeKV label="Автор" value={PE.cfg.author} last /></React.Fragment>, "Из мастера монтажа")
          : <PeField fill={fill} label="Название конфигурации" addLabel="Название конфигурации" value={v.cfgName} onChange={set("cfgName")} />}
        <div style={{ position: "relative", border: "1px dashed var(--border-light)", borderRadius: 16, padding: "8px 4px 10px 16px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 56 }}>
            <CmpIcon icon="tune" size={24} color={peMuted} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <CmpText variant="headline3">Настройки блока</CmpText>
              <CmpText variant="paragraph3" color={rr.settings === "fail" ? "var(--warning-default)" : peMuted}>
                {noblock ? "Недоступны: блок не считывался" : !linked && rr.settings === "fail" ? "Не прочитаны · перечитать можно при подключённом блоке" : rr.settings === "ok" ? `Сохранены · ${PE.settingsAt}` : rr.settings === "loading" ? "Читаем…" : "Не прочитаны"}
              </CmpText>
            </div>
            {!noblock && rr.settings === "ok" && <CmpIconButton icon="more_vert" contentDescription="Действия с настройками" onClick={() => setMenu(!menu)} />}
            {!noblock && linked && rr.settings === "fail" && <PeTextBtn text="Перечитать" icon="refresh" onClick={() => reread("settings")} />}
            {rr.settings === "loading" && <span style={{ padding: "0 14px" }}><PeSpinner /></span>}
          </div>
          <div style={{ display: "flex", gap: 8, alignItems: "center", paddingTop: 2 }}>
            <CmpIcon icon="visibility_off" size={16} color={peMuted} />
            <CmpText variant="additional1" color={peMuted}>В документ не попадают — рабочая копия для мастера</CmpText>
          </div>
          {menu && (
            <div className="pe-anim" style={{ position: "absolute", right: 8, top: 60, zIndex: 5, minWidth: 280, background: "var(--surface-container-lowest)", border: "1px solid var(--border-highlight)", borderRadius: 12, boxShadow: "0 8px 24px rgba(10,26,58,.18)", padding: 6, transformOrigin: "top right", animation: `peIn 160ms ${PE_EASE}` }}>
              {[["save", "Сохранить в файл"], ["folder", "Сохранить в мои конфигурации"]].map(([ic, t]) => (
                <button key={t} className="pe-tap" onClick={() => setMenu(false)} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 52, display: "flex", alignItems: "center", gap: 14, padding: "0 14px", borderRadius: 8, cursor: "pointer" }}>
                  <CmpIcon icon={ic} size={22} color={peMuted} /><CmpText variant="paragraph2">{t}</CmpText>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    ),
    works: fast ? autoWrap(PE.worksFast.map(([l, r], i, a) => (
      <div key={l} style={{ minHeight: 52, display: "flex", alignItems: "center", gap: 12, borderBottom: i === a.length - 1 ? "none" : "1px solid var(--border-ultra-highlight)" }}>
        <CmpIcon icon={l === "Время монтажа" ? "schedule" : "check"} size={22} color={l === "Время монтажа" ? peMuted : "var(--success-default)"} />
        <CmpText variant="headline3" style={{ flex: 1 }}>{l}</CmpText>
        {r && <CmpText variant="paragraph2" color={peMuted} style={{ textAlign: "right" }}>{r}</CmpText>}
      </div>
    )), "Из мастера монтажа") : (
      <div style={{ display: "flex", flexDirection: "column" }}>
        {PE.worksExt.map((l) => (
          <label key={l} className="pe-tap" style={{ minHeight: 56, display: "flex", alignItems: "center", gap: 12, padding: "0 12px", margin: "0 -12px", borderRadius: 12, cursor: "pointer" }}>
            <CmpCheckbox checked={!!works[l]} onCheckedChange={(c) => { setWorks((w) => ({ ...w, [l]: c })); touch(); }} />
            <CmpText variant="headline3" style={{ flex: 1 }}>{l}</CmpText>
          </label>
        ))}
      </div>
    ),
    check: (
      <div>
        {PE.checks.map(([t, r], i, a) => {
          const s = PE_CHECK[r];
          return (
            <div key={t} style={{ minHeight: 52, display: "flex", alignItems: "center", gap: 12, borderBottom: i === a.length - 1 ? "none" : "1px solid var(--border-ultra-highlight)" }}>
              <CmpIcon icon={s.icon} size={22} color={s.color} />
              <CmpText variant="paragraph2" style={{ flex: 1, minWidth: 0 }}>{t}</CmpText>
              <CmpText variant="additional0" color={r === "fail" ? "var(--error-default)" : peMuted}>{s.word}</CmpText>
            </div>
          );
        })}
      </div>
    ),
    devices: rr.devices !== "ok" ? (
      <PeReread text="Не удалось прочитать устройства" state={rr.devices} onRetry={() => reread("devices")} linked={linked} />
    ) : autoWrap(PE.devices.map(([f, n, no, addr, fw], i, a) => (
      <div key={addr} style={{ minHeight: 60, display: "flex", alignItems: "center", gap: 14, borderBottom: i === a.length - 1 ? "none" : "1px solid var(--border-ultra-highlight)" }}>
        <PeDevIcon file={f} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <CmpText variant="headline3">{n} <span style={{ color: peMuted, fontWeight: 500 }}>{no}</span></CmpText>
          <CmpText variant="additional1" color={peMuted}>ПО {fw}{scenario !== "ext" && <span style={{ fontFamily: "var(--font-mono)" }}> · {addr}</span>}</CmpText>
        </div>
      </div>
    )), scenario === "ext" ? "Считано по USB · 14:03" : "Считано с блока · 14:03"),
    photos: (
      <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, minmax(0,1fr))", gap: 8 }}>
          {photos.map((p, i) => (
            <button key={p.id} className="pe-tap" disabled={p.loading} onClick={() => setViewer(i)} aria-label={`Фото ${i + 1}${p.cap ? ": " + p.cap : ""}`} style={{ all: "unset", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 4, cursor: p.loading ? "default" : "pointer", borderRadius: 12, minWidth: 0 }}>
              <div style={{ aspectRatio: "1", borderRadius: 12, overflow: "hidden", position: "relative", background: "repeating-linear-gradient(135deg, var(--background-surface-highlight) 0 8px, var(--background-surface-ultra-highlight) 8px 16px)", display: "grid", placeItems: "center" }}>
                {p.loading
                  ? <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 6 }}><PeSpinner size={24} /><CmpText variant="additional1" color={peMuted}>Загружаем</CmpText></div>
                  : <CmpText variant="additional1" color={peMuted} style={{ fontFamily: "var(--font-mono)" }}>фото {i + 1}</CmpText>}
              </div>
              {p.cap && !p.loading && <CmpText variant="additional1" maxLines={1} color={peMuted} style={{ padding: "0 2px" }}>{p.cap}</CmpText>}
            </button>
          ))}
          {photos.length < 8 && (
            <button className="pe-tap" onClick={addPhoto} style={{ all: "unset", boxSizing: "border-box", aspectRatio: "1", borderRadius: 12, border: "1.5px dashed var(--border-tertiary)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 6, cursor: "pointer", color: "var(--primary)", padding: 6, textAlign: "center" }}>
              <CmpIcon icon="add_a_photo" size={28} color="var(--primary)" />
              <CmpText variant="additional0" color="var(--primary)">{desk ? "Добавить файл" : "Добавить фото"}</CmpText>
            </button>
          )}
        </div>
        {photoErr && (
          <div className="pe-anim" role="alert" style={{ display: "flex", gap: 10, alignItems: "flex-start", padding: "10px 12px", borderRadius: 12, background: "var(--error-highlight)", animation: `peIn 200ms ${PE_EASE}` }}>
            <CmpIcon icon="sd_card_alert" size={22} color="var(--error-default)" />
            <CmpText variant="paragraph3" style={{ flex: 1 }}>{photoErr}</CmpText>
          </div>
        )}
        <CmpText variant="additional0" color={peMuted} weight={500}>{photos.length >= 8 ? "8 из 8 — больше добавить нельзя. Нажмите на фото, чтобы подписать или удалить" : photos.length ? `${photos.length} из 8 · нажмите на фото, чтобы подписать или удалить` : desk ? "До 8 фото: JPG или PNG с компьютера" : "До 8 фото: камера или галерея"}</CmpText>
      </div>
    ),
    notes: (() => {
      const n = v.notes.length;
      const box = (
        <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
          <textarea className="pe-area" value={v.notes} maxLength={1000} onChange={(e) => set("notes")(e.target.value)} placeholder="Что важно знать клиенту или следующему мастеру" rows={3}
            style={{ boxSizing: "border-box", width: "100%", minHeight: 112, resize: "vertical", padding: "14px 18px", borderRadius: 12, border: "1px solid var(--border-light)", background: "var(--background-surface-ultra-highlight)", color: "var(--on-surface)", font: "500 var(--t-paragraph1-size)/var(--t-paragraph1-line) var(--font-sans)", outline: "none" }} />
          {n >= 800 && <CmpText variant="additional0" color={n >= 1000 ? "var(--error-default)" : peMuted} style={{ textAlign: "right" }}>{n} / 1000</CmpText>}
        </div>
      );
      return fill === "fields" || v.notes ? box : <PeQuietArea onOpen={() => {}} render={box} />;
    })(),
    client: (
      <div style={{ display: "flex", flexDirection: "column", gap: fill === "fields" ? 16 : 4 }}>
        <PeField fill={fill} label="Имя клиента" value={v.client} onChange={set("client")} />
        <PeField fill={fill} label="Телефон клиента" value={v.phone} onChange={set("phone")} placeholder="+7 ___ ___-__-__" inputMode="tel" />
      </div>
    ),
    master: (
      <div style={{ display: "flex", flexDirection: "column", gap: fill === "fields" ? 16 : 4 }}>
        {autoWrap(<React.Fragment><PeKV label="Исполнитель" value={PE.master.name} /><PeKV label="Контакт" value={PE.master.contact} last /></React.Fragment>, "Из профиля")}
        <PeField fill={fill} label="Организация" value={v.org} onChange={set("org")} hint="Запомним для следующих паспортов" />
        <PeField fill={fill} label="Адрес" value={v.addr} onChange={set("addr")} hint="Запомним для следующих паспортов" />
        {fill === "quiet" && !v.org && !v.addr && <PeNote icon="history">Организацию и адрес вводят один раз — дальше подставятся сами</PeNote>}
      </div>
    ),
  };

  // Чипы: следим за прокруткой.
  const onScroll = () => {
    if (nav !== "chips") return;
    const sc = scrollRef.current; if (!sc) return;
    const y = sc.scrollTop + 72;
    let cur = sections[0][0];
    for (const [id] of sections) { const el = secRefs.current[id]; if (el && el.offsetTop <= y) cur = id; }
    if (sc.scrollTop + sc.clientHeight >= sc.scrollHeight - 4) cur = sections[sections.length - 1][0];
    if (cur !== active) setActive(cur);
  };
  React.useEffect(() => {
    const row = chipsRef.current; if (!row) return;
    const chip = row.querySelector(`[data-chip="${active}"]`);
    if (chip) row.scrollTo({ left: chip.offsetLeft - 16, behavior: "smooth" });
  }, [active]);
  const jump = (id) => { const sc = scrollRef.current, el = secRefs.current[id]; if (sc && el) sc.scrollTo({ top: el.offsetTop - 64, behavior: "smooth" }); setActive(id); };

  const icoTile = (icon, w) => (
    <span style={{ width: 44, height: 44, borderRadius: 12, flex: "none", display: "grid", placeItems: "center", background: w ? "var(--warning-highlight)" : "var(--background-surface-highlight)" }}>
      <CmpIcon icon={w ? "warning" : icon} size={24} color={w ? "var(--warning-default)" : "var(--on-surface)"} />
    </span>
  );

  const intro = ext
    ? (noblock ? "Блок не считывался: оборудование — со слов сервера или вручную. Остальное — по желанию." : "Отметьте выполненные работы. Остальное — по желанию.")
    : "Почти всё заполнено само. Проверьте и при желании дополните.";

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-surface)", fontFamily: "var(--font-sans)", position: "relative" }}>
      {!embedded && <CmpTopAppBar
        title="Паспорт монтажа"
        subtitle={`№ ${PE.no} · 19.11.26`}
        navigationIcon={<CmpIconButton icon="arrow_back" contentDescription="Назад" onClick={onBack} />}
        actions={
          <div aria-live="polite" title={saved === "saving" ? "Сохраняем" : "Сохранено"} style={{ width: 48, height: 48, display: "grid", placeItems: "center" }}>
            {saved === "saving" ? <PeSpinner size={18} /> : <CmpIcon icon="cloud_done" size={24} color={peMuted} />}
          </div>
        }
      />}
      {nav === "chips" && (
        <div ref={chipsRef} className="pe-scroll" style={{ display: "flex", gap: 8, overflowX: "auto", padding: "4px 16px 12px", borderBottom: "1px solid var(--border-highlight)", flex: "none", scrollbarWidth: "none" }}>
          {sections.map(([id, t]) => (
            <span key={id} data-chip={id} style={{ flex: "none" }}>
              <CmpChip label={t} selected={active === id} icon={warn[id] ? "warning" : undefined} onClick={() => jump(id)} />
            </span>
          ))}
        </div>
      )}
      <div ref={scrollRef} onScroll={onScroll} className="pe-scroll" style={{ flex: 1, minHeight: 0, overflowY: "auto", position: "relative", scrollbarWidth: "none" }}>
        <div style={{ maxWidth: 760, margin: "0 auto" }}>
        <div style={{ padding: embedded ? "16px 20px 12px" : "4px 20px 12px", display: "flex", flexDirection: "column", gap: 12 }}>
          <CmpText variant="paragraph2" color={peMuted}>{intro} Пустые поля в документ не попадут. Черновик сохраняется сам.</CmpText>
          {noblock && onReadBlock && (
            <div className="pe-anim" style={{ display: "flex", flexDirection: "column", gap: 12, padding: 16, borderRadius: 16, border: "2px solid var(--primary)", background: "var(--brand-primary-highlight)", animation: `peIn 220ms ${PE_EASE}` }}>
              <div style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
                <CmpIcon icon="bluetooth_connected" size={26} color="var(--primary)" />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <CmpText variant="headline3">Подключён этот блок</CmpText>
                  <CmpText variant="paragraph3" color={peMuted} style={{ marginTop: 2 }}>Считайте его — оборудование, устройства и настройки заполнятся сами. Ручные поля останутся.</CmpText>
                </div>
              </div>
              <CmpButton text="Считать блок" icon="sync" type="Primary" size="L" fullWidth onClick={onReadBlock} />
            </div>
          )}
          {!linked && !noblock && (
            <div style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
              <CmpIcon icon="lock" size={20} color={peMuted} />
              <CmpText variant="paragraph3" color={peMuted}>Блок не подключён: данные блока — снимок на момент считывания, правятся только ручные поля.</CmpText>
            </div>
          )}
        </div>

        {nav === "accordion" ? (
          <div style={{ display: "flex", flexDirection: "column", padding: "0 12px 24px", gap: 8 }}>
            {sections.map(([id, t, icon]) => {
              const isOpen = open.has(id);
              const s = sum[id];
              return (
                <div key={id} style={{ background: isOpen ? "var(--background-surface-card)" : "transparent", border: `1px solid ${isOpen ? "var(--border-highlight)" : "transparent"}`, borderRadius: 16, transition: `background-color 200ms ease, border-color 200ms ease` }}>
                  <button className="pe-tap" onClick={() => toggle(id)} aria-expanded={isOpen} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 72, padding: "10px 4px 10px 8px", display: "flex", alignItems: "center", gap: 14, borderRadius: 16, cursor: "pointer" }}>
                    {icoTile(icon, warn[id])}
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <CmpText variant="headline2">{t}</CmpText>
                      <CmpText variant="paragraph3" maxLines={1} color={warn[id] ? "var(--warning-default)" : peMuted} style={{ marginTop: 3 }}>
                        {warn[id] ? (id === "eq" ? "Не удалось прочитать параметры" : id === "cfg" ? "Настройки блока не прочитаны" : "Не удалось прочитать") : s || "Можно дополнить"}
                      </CmpText>
                    </div>
                    <span style={{ width: 48, height: 48, display: "grid", placeItems: "center", transform: isOpen ? "rotate(180deg)" : "none", transition: `transform 220ms ${PE_EASE}` }}><CmpIcon icon="expand_more" size={26} color={peMuted} /></span>
                  </button>
                  {isOpen && <div className="pe-anim" style={{ padding: "0 16px 16px", animation: `peIn 200ms ${PE_EASE}` }}>{body[id]}</div>}
                </div>
              );
            })}
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", padding: "0 20px 32px" }}>
            {sections.map(([id, t, icon], idx) => (
              <section key={id} ref={(el) => (secRefs.current[id] = el)} style={{ padding: "20px 0", borderTop: idx ? "1px solid var(--border-highlight)" : "none", display: "flex", flexDirection: "column", gap: 12 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                  <CmpIcon icon={icon} size={24} color={peMuted} />
                  <CmpText variant="headline2" style={{ flex: 1 }}>{t}</CmpText>
                  {id === "photos" && photos.length > 0 && <CmpText variant="additional0" color={peMuted}>{photos.length} из 8</CmpText>}
                </div>
                {id === "eq" && !noblock ? (
                  <React.Fragment>
                    <PeFold summary={<CmpText variant="headline3">{PE.eqSummary}</CmpText>}>{PE.eq.map(([l, val], i, a) => {
                      const miss = rr.params !== "ok" && PE.eqParams.includes(l);
                      return <PeLine key={l} label={l} value={miss ? "—" : val} warn={miss} last={i === a.length - 1} />;
                    })}</PeFold>
                    <PeReread text="Не удалось прочитать параметры" state={rr.params} onRetry={() => reread("params")} linked={linked} />
                  </React.Fragment>
                ) : id === "check" ? (
                  <PeFold summary={
                    <div style={{ display: "flex", alignItems: "center", gap: 10, flexWrap: "wrap" }}>
                      <CmpText variant="headline3">{sum.check}</CmpText>
                      <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}><CmpIcon icon="cancel" size={18} color="var(--error-default)" /><CmpText variant="additional0" color={peMuted}>1</CmpText></span>
                      <span style={{ display: "inline-flex", alignItems: "center", gap: 4 }}><CmpIcon icon="do_not_disturb_on" size={18} color={peMuted} /><CmpText variant="additional0" color={peMuted}>1</CmpText></span>
                    </div>
                  }>{body.check}</PeFold>
                ) : body[id]}
              </section>
            ))}
          </div>
        )}
        </div>
      </div>

      {!embedded && <div style={{ flex: "none", borderTop: "1px solid var(--border-highlight)", background: "var(--background)" }}>
        <div style={{ padding: "12px 20px 16px", maxWidth: 560, margin: "0 auto" }}>
          <CmpButton text="Сформировать PDF" icon="picture_as_pdf" type="Primary" size="XL" fullWidth onClick={onPdf} />
        </div>
      </div>}
      {sheet && <PePhotoSheet mode={sheet} denied={cameraDenied} onCamera={() => (cameraDenied ? setSheet("denied") : pushPhoto())} onGallery={pushPhoto} onClose={() => setSheet(null)} />}
      {viewer !== null && photos[viewer] && (
        <PePhotoViewer photos={photos} index={viewer} onIndex={setViewer} onClose={() => setViewer(null)}
          onCaption={(cap) => { setPhotos((ps) => ps.map((x, i) => (i === viewer ? { ...x, cap } : x))); touch(); }}
          onDelete={() => { setPhotos((ps) => ps.filter((_, i) => i !== viewer)); setViewer(null); touch(); }} />
      )}
    </div>
  );
}

// Выбор источника фото (телефон/планшет) и «нет доступа к камере».
function PePhotoSheet({ mode, onCamera, onGallery, onClose }) {
  const denied = mode === "denied";
  const item = (icon, text, onClick) => (
    <button className="pe-tap" onClick={onClick} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 64, display: "flex", alignItems: "center", gap: 16, padding: "0 20px", borderRadius: 12, cursor: "pointer" }}>
      <CmpIcon icon={icon} size={26} color="var(--on-surface)" /><CmpText variant="headline3">{text}</CmpText>
    </button>
  );
  return (
    <div style={{ position: "absolute", inset: 0, zIndex: 40, display: "flex", flexDirection: "column", justifyContent: "flex-end" }}>
      <div onClick={onClose} className="pe-anim" style={{ position: "absolute", inset: 0, background: "var(--background-overlay)", animation: "peFade 200ms ease" }} />
      <div className="pe-anim" role="dialog" aria-label="Добавить фото" style={{ position: "relative", background: "var(--surface-container-lowest)", borderRadius: "20px 20px 0 0", padding: "10px 12px 20px", animation: `peSheet 300ms ${PE_EASE}`, maxWidth: 640, width: "100%", margin: "0 auto", boxSizing: "border-box" }}>
        <div style={{ width: 36, height: 4, borderRadius: 2, background: "var(--border-tertiary)", margin: "0 auto 12px" }} />
        {denied ? (
          <div style={{ display: "flex", flexDirection: "column", gap: 14, padding: "4px 12px 0" }}>
            <div style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
              <CmpIcon icon="no_photography" size={28} color="var(--warning-default)" />
              <div style={{ flex: 1 }}>
                <CmpText variant="headline2">Нет доступа к камере</CmpText>
                <CmpText variant="paragraph2" color={peMuted} style={{ marginTop: 4, textWrap: "pretty" }}>Разрешите приложению камеру в настройках телефона или выберите фото из галереи.</CmpText>
              </div>
            </div>
            <CmpButton text="Открыть настройки" icon="settings" type="Primary" size="L" fullWidth onClick={onClose} />
            <CmpOutlinedButton text="Выбрать из галереи" icon="photo_library" fullWidth onClick={onGallery} />
          </div>
        ) : (
          <React.Fragment>
            <CmpText variant="headline2" style={{ padding: "4px 20px 8px" }}>Добавить фото</CmpText>
            {item("photo_camera", "Снять камерой", onCamera)}
            {item("photo_library", "Выбрать из галереи", onGallery)}
          </React.Fragment>
        )}
      </div>
    </div>
  );
}

// Просмотр фото на весь экран: листание, подпись (одна строка), удаление с подтверждением.
function PePhotoViewer({ photos, index, onIndex, onClose, onCaption, onDelete }) {
  const p = photos[index];
  const [ask, setAsk] = React.useState(false);
  const [cap, setCap] = React.useState(p.cap || "");
  React.useEffect(() => { setCap(photos[index].cap || ""); setAsk(false); }, [index]);
  React.useEffect(() => {
    const k = (e) => { if (e.key === "Escape") onClose(); if (e.key === "ArrowLeft" && index > 0) onIndex(index - 1); if (e.key === "ArrowRight" && index < photos.length - 1) onIndex(index + 1); };
    window.addEventListener("keydown", k); return () => window.removeEventListener("keydown", k);
  }, [index]);
  const W = "#F5F8FF";
  const nav = (dir, icon, disabled) => (
    <button onClick={() => !disabled && onIndex(index + dir)} aria-label={dir < 0 ? "Предыдущее" : "Следующее"} disabled={disabled}
      style={{ all: "unset", width: 56, height: 56, borderRadius: 999, display: "grid", placeItems: "center", cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.3 : 1, background: "rgba(255,255,255,.12)", flex: "none" }}>
      <CmpIcon icon={icon} size={30} color={W} />
    </button>
  );
  return (
    <div className="pe-anim" role="dialog" aria-label="Просмотр фото" style={{ position: "absolute", inset: 0, zIndex: 50, background: "#05080F", display: "flex", flexDirection: "column", animation: "peFade 200ms ease" }}>
      <div style={{ flex: "none", display: "flex", alignItems: "center", gap: 4, padding: "8px 8px 8px 4px" }}>
        <CmpIconButton icon="close" contentDescription="Закрыть" tint={W} onClick={onClose} />
        <PE_DS.CmpText variant="headline3" color={W} style={{ flex: 1 }}>{index + 1} из {photos.length}</PE_DS.CmpText>
        <CmpIconButton icon="delete" contentDescription="Удалить фото" tint={W} onClick={() => setAsk(true)} />
      </div>
      <div style={{ flex: 1, minHeight: 0, display: "flex", alignItems: "center", gap: 8, padding: "0 8px" }}>
        {nav(-1, "chevron_left", index === 0)}
        <div style={{ flex: 1, minWidth: 0, height: "100%", maxHeight: 520, borderRadius: 8, background: "repeating-linear-gradient(135deg, #1A2232 0 10px, #121929 10px 20px)", display: "grid", placeItems: "center" }}>
          <span style={{ fontFamily: "var(--font-mono)", fontSize: 13, color: "#8B97AD" }}>фото {index + 1}</span>
        </div>
        {nav(1, "chevron_right", index === photos.length - 1)}
      </div>
      <div style={{ flex: "none", padding: "12px 16px 16px", background: "var(--background)" }} data-theme="dark">
        <CmpTextField label="Подпись" value={cap} onChange={(x) => { const t = x.slice(0, 60); setCap(t); onCaption(t); }} placeholder="Например, реле блокировки" supportingText="Необязательно · одна строка, попадёт в документ под фото" />
      </div>
      {ask && (
        <div style={{ position: "absolute", inset: 0, background: "rgba(5,8,15,.6)", display: "grid", placeItems: "center", padding: 24 }}>
          <div className="pe-anim" style={{ width: "100%", maxWidth: 360, background: "var(--surface-container-lowest)", borderRadius: 16, padding: 24, display: "flex", flexDirection: "column", gap: 12, animation: `peIn 200ms ${PE_EASE}` }}>
            <CmpText variant="headline2">Удалить фото {index + 1}?</CmpText>
            <CmpText variant="paragraph2" color={peMuted}>Фото и подпись пропадут из паспорта.</CmpText>
            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end", paddingTop: 4 }}>
              <PeTextBtn text="Отмена" onClick={() => setAsk(false)} />
              <PeTextBtn text="Удалить" onClick={onDelete} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// «Замечания» в тихой подаче: строка «+ Замечания», по тапу — поле.
function PeQuietArea({ render }) {
  const [open, setOpen] = React.useState(false);
  if (open) return render;
  return (
    <button className="pe-tap" onClick={() => setOpen(true)} style={{ all: "unset", boxSizing: "border-box", width: "100%", minHeight: 52, display: "flex", alignItems: "center", gap: 12, padding: "0 12px", margin: "0 -12px", borderRadius: 12, cursor: "pointer" }}>
      <CmpIcon icon="add" size={24} color="var(--primary)" />
      <CmpText variant="headline3" color="var(--primary)">Добавить замечание</CmpText>
    </button>
  );
}

Object.assign(window, { PassportEditor, PE_DATA: PE, PE_CHECK, PeDevIcon });
