// PassportDoc.jsx — «Паспорт монтажа»: документ PDF, A4 книжная, поля 15 мм.
// Чёрно-белая печать: чёрный текст, один акцент (seed #2C3639), тонкие линии, без заливок.
// Пустые поля и разделы не печатаются. Пагинация в макете задана явно (как её должен вести генератор).
//
// window.PassportDocPages({ variant, lang, guides }) → массив листов
//   variant "full" | "min" | "noblock" | "long";  lang "ru" | "en";  guides — показать поле логотипа и поля листа

const PD_ACC = "#2C3639";
const PD_FONT = "'IBM Plex Sans', system-ui, sans-serif";
const PD_SZ = { title: "17pt", body: "9.5pt", small: "7.5pt" };

const PD_T = {
  ru: {
    title: "Паспорт монтажа", vehicle: "Автомобиль", eq: "Оборудование", cfg: "Конфигурация", works: "Выполненные работы",
    check: "Проверка системы", checkCont: "Проверка системы — продолжение", devices: "Устройства системы", notes: "Замечания", photos: "Фото",
    master: "Исполнитель", client: "Клиент", signDone: "Работу выполнил", signAcc: "Работу принял", fio: "Ф. И. О.", sign: "подпись",
    page: (a, b) => `стр. ${a} из ${b}`, passed: (a, b) => `Пройдено ${a} из ${b}`,
    res: { ok: "Пройдено", fail: "Не пройдено", skip: "Пропущено" }, task: "Задание", result: "Итог", type: "Тип", num: "Номер", fw: "Версия ПО", addr: "Адрес",
    date: "19 ноября 2026", manual: "Данные блока внесены вручную, блок не считывался",
    car: [["Марка и модель", "Kia Sportage"], ["Годы выпуска", "2021–2024"], ["Госномер", "А 123 ВС 777"], ["VIN", "XWEPH81ADN0012345"], ["Пробег", "48 200 км"]],
    eqRows: [["Модель", "Pandora DX-91"], ["ID устройства", "1234567"], ["Серийный номер", "DX91-0042-118734"], ["ПО основное", "3.57"], ["Загрузчик", "1.12"], ["CAN", "12.05 · код 1234"], ["Bluetooth", "2.8"], ["IMEI", "861234050118734"]],
    cfgRows: [["Название", "Kia Sportage 2022 · автозапуск"], ["Автор", "Pandora"]],
    workRows: [["Настройки", "записаны"], ["Обход иммобилайзера", "потребовался Clone"], ["Clone", "выполнен"], ["Slave", "обучение выполнено"], ["Время монтажа", "1 ч 25 мин"]],
    worksMin: [["Настройки", "записаны"], ["Обход иммобилайзера", "выполнен"]],
    checks: ["Постановка на охрану с брелока", "Снятие с охраны с брелока", "Открытие дверей", "Закрытие дверей", "Багажник", "Концевик капота", "Датчик удара", "Автозапуск двигателя", "Блокировка двигателя", "Метка в зоне", "Метка вне зоны"],
    checksLong: ["Датчик наклона", "Датчик движения", "Сирена", "Аварийная сигнализация", "Поворотники", "Стояночный тормоз", "Педаль тормоза", "Зажигание", "Обороты двигателя", "Температура двигателя", "Напряжение бортсети", "Центральный замок — закрытие", "Центральный замок — открытие", "Стеклоподъёмники", "Зеркала", "Замок капота", "Реле блокировки", "GSM-связь", "GPS", "Режим «Сервис»"],
    devs: [["Метка BT-760", "№ 1", "1.24", "4C:11:AE:9F:20:01"], ["Метка BT-760", "№ 2", "1.24", "4C:11:AE:9F:20:02"], ["Брелок D-030", "№ 1", "2.03", "4C:11:AE:12:7B:44"], ["Модуль RHM-03", "№ 1", "1.8", "4C:11:AE:3A:05:90"], ["Замок капота", "№ 1", "1.05", "4C:11:AE:3A:05:91"]],
    note: "Автозапуск не прошёл проверку: штатный иммобилайзер не принимает ключ при запуске. Клиент предупреждён, повторный визит назначен на 21 ноября. Метки — в бардачке, вторая передана клиенту.",
    caps: ["Блок под панелью приборов", "Подключение к CAN", "Реле блокировки", "Сирена под капотом", "Датчик капота", "Модуль RHM-03", "Метки и брелок", "Пломба на разъёме"],
    m: [["Ф. И. О.", "Алексей Смирнов"], ["Контакт", "+7 912 345-67-89"]],
    org: "ООО «Автозвук-Север»", orgAddr: "Москва, ул. Складочная, 1, стр. 6",
    c: [["Ф. И. О.", "Ирина Ковалёва"], ["Телефон", "+7 903 555-12-40"]],
  },
  en: {
    title: "Installation certificate", vehicle: "Vehicle", eq: "Equipment", cfg: "Configuration", works: "Work performed",
    check: "System check", checkCont: "System check — continued", devices: "System devices", notes: "Remarks", photos: "Photos",
    master: "Installer", client: "Customer", signDone: "Work performed by", signAcc: "Work accepted by", fio: "Full name", sign: "signature",
    page: (a, b) => `page ${a} of ${b}`, passed: (a, b) => `Passed ${a} of ${b}`,
    res: { ok: "Passed", fail: "Failed", skip: "Skipped" }, task: "Test", result: "Result", type: "Type", num: "Number", fw: "Firmware", addr: "Address",
    date: "November 19, 2026", manual: "Device data entered manually; the device was not read",
    car: [["Make and model", "Kia Sportage"], ["Model years", "2021–2024"], ["License plate", "A 123 BC 777"], ["VIN", "XWEPH81ADN0012345"], ["Mileage", "48,200 km"]],
    eqRows: [["Model", "Pandora DX-91"], ["Device ID", "1234567"], ["Serial number", "DX91-0042-118734"], ["Main firmware", "3.57"], ["Bootloader", "1.12"], ["CAN", "12.05 · code 1234"], ["Bluetooth", "2.8"], ["IMEI", "861234050118734"]],
    cfgRows: [["Name", "Kia Sportage 2022 · remote start"], ["Author", "Pandora"]],
    workRows: [["Settings", "written"], ["Immobilizer bypass", "Clone required"], ["Clone", "completed"], ["Slave", "learning completed"], ["Installation time", "1 h 25 min"]],
    worksMin: [["Settings", "written"], ["Immobilizer bypass", "completed"]],
    checks: ["Arming from the key fob", "Disarming from the key fob", "Door unlocking", "Door locking", "Trunk", "Hood switch", "Shock sensor", "Remote engine start", "Engine immobilization", "Tag in range", "Tag out of range"],
    checksLong: ["Tilt sensor", "Motion sensor", "Siren", "Hazard lights", "Turn signals", "Parking brake", "Brake pedal", "Ignition", "Engine RPM", "Engine temperature", "On-board voltage", "Central locking — lock", "Central locking — unlock", "Power windows", "Mirrors", "Hood lock", "Immobilizer relay", "GSM connection", "GPS", "Service mode"],
    devs: [["Tag BT-760", "No. 1", "1.24", "4C:11:AE:9F:20:01"], ["Tag BT-760", "No. 2", "1.24", "4C:11:AE:9F:20:02"], ["Key fob D-030", "No. 1", "2.03", "4C:11:AE:12:7B:44"], ["Module RHM-03", "No. 1", "1.8", "4C:11:AE:3A:05:90"], ["Hood lock", "No. 1", "1.05", "4C:11:AE:3A:05:91"]],
    note: "Remote start failed the check: the stock immobilizer does not accept the key on start. The customer has been informed; a follow-up visit is scheduled for November 21. Tags are in the glove box; the second one was handed to the customer.",
    caps: ["Unit under the dashboard", "CAN connection", "Immobilizer relay", "Siren under the hood", "Hood sensor", "RHM-03 module", "Tags and key fob", "Seal on the connector"],
    m: [["Full name", "Alexey Smirnov"], ["Contact", "+7 912 345-67-89"]],
    org: "Avtozvuk-Sever LLC", orgAddr: "1 Skladochnaya St., bldg. 6, Moscow",
    c: [["Full name", "Irina Kovaleva"], ["Phone", "+7 903 555-12-40"]],
  },
};
const PD_NO = "20261119-234567-1";
const PD_RES11 = ["ok", "ok", "ok", "ok", "ok", "ok", "skip", "fail", "ok", "ok", "ok"];
const PD_ICON = { ok: "check_circle", fail: "cancel", skip: "do_not_disturb_on" };

// ── Кирпичи листа ─────────────────────────────────────────────────────────────
const pdTxt = (size, weight, extra) => ({ fontFamily: PD_FONT, fontSize: PD_SZ[size], fontWeight: weight || 400, lineHeight: 1.35, color: "#000", ...extra });

function PdH({ children, aside }) {
  return (
    <div style={{ display: "flex", alignItems: "baseline", gap: "4mm", borderBottom: `0.6pt solid ${PD_ACC}`, paddingBottom: "1.2mm", marginBottom: "1.5mm", breakAfter: "avoid" }}>
      <div style={pdTxt("body", 700, { color: PD_ACC, textTransform: "uppercase", letterSpacing: "0.06em", flex: 1 })}>{children}</div>
      {aside && <div style={pdTxt("body", 700)}>{aside}</div>}
    </div>
  );
}

function PdRows({ rows, labelW = "38mm", mono = [] }) {
  return (
    <div>
      {rows.map(([l, v], i) => (
        <div key={l} style={{ display: "flex", gap: "3mm", padding: "0.9mm 0", borderBottom: i === rows.length - 1 ? "none" : "0.3pt solid #bbb" }}>
          <div style={pdTxt("small", 400, { flex: `0 0 ${labelW}`, paddingTop: "0.4mm", color: "#333" })}>{l}</div>
          <div style={pdTxt("body", 500, { flex: 1, minWidth: 0, overflowWrap: "anywhere", ...(mono.includes(l) ? { fontVariantNumeric: "tabular-nums", letterSpacing: "0.02em" } : {}) })}>{v}</div>
        </div>
      ))}
    </div>
  );
}

function PdSection({ title, aside, children, mt = "6mm" }) {
  return <section style={{ marginTop: mt, breakInside: "avoid" }}><PdH aside={aside}>{title}</PdH>{children}</section>;
}

function PdCheckTable({ t, items, start = 0 }) {
  const th = pdTxt("small", 700, { textAlign: "left", padding: "1mm 0", borderBottom: `0.6pt solid #000` });
  const td = (extra) => pdTxt("body", 400, { padding: "0.9mm 0", borderBottom: "0.3pt solid #bbb", verticalAlign: "middle", ...extra });
  return (
    <table style={{ width: "100%", borderCollapse: "collapse", tableLayout: "fixed" }}>
      <colgroup><col style={{ width: "8mm" }} /><col /><col style={{ width: "34mm" }} /></colgroup>
      <thead><tr><th style={th}>№</th><th style={th}>{t.task}</th><th style={th}>{t.result}</th></tr></thead>
      <tbody>
        {items.map(([name, r], i) => (
          <tr key={i}>
            <td style={td({ color: "#333" })}>{start + i + 1}</td>
            <td style={td({ paddingRight: "3mm" })}>{name}</td>
            <td style={td({ fontWeight: r === "fail" ? 700 : 400 })}>
              <span style={{ display: "inline-flex", alignItems: "center", gap: "1.5mm" }}>
                <span className="material-icons" style={{ fontSize: "11pt", color: r === "skip" ? "#666" : "#000" }}>{PD_ICON[r]}</span>
                {t.res[r]}
              </span>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

function PdPhotos({ caps, from = 0 }) {
  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "4mm 6mm" }}>
      {caps.map((c, i) => (
        <figure key={i} style={{ margin: 0, breakInside: "avoid" }}>
          <div style={{ aspectRatio: "4 / 3", border: "0.3pt solid #999", background: "repeating-linear-gradient(135deg, #f1f1f1 0 2mm, #fff 2mm 4mm)", display: "grid", placeItems: "center" }}>
            <span style={{ fontFamily: "var(--font-mono)", fontSize: "7pt", color: "#777" }}>фото {from + i + 1}</span>
          </div>
          <figcaption style={pdTxt("small", 400, { marginTop: "1.2mm", color: "#222" })}>{c}</figcaption>
        </figure>
      ))}
    </div>
  );
}

function PdSign({ t }) {
  const line = (label) => (
    <div style={{ flex: 1, minWidth: 0 }}>
      <div style={pdTxt("small", 700, { marginBottom: "9mm" })}>{label}</div>
      <div style={{ display: "flex", gap: "4mm", alignItems: "flex-end" }}>
        <div style={{ flex: "0 0 30mm" }}><div style={{ borderBottom: "0.6pt solid #000", height: "1px" }} /><div style={pdTxt("small", 400, { color: "#555", marginTop: "1mm" })}>{t.sign}</div></div>
        <div style={pdTxt("body", 400, { paddingBottom: "0.5mm" })}>/</div>
        <div style={{ flex: 1 }}><div style={{ borderBottom: "0.6pt solid #000", height: "1px" }} /><div style={pdTxt("small", 400, { color: "#555", marginTop: "1mm" })}>{t.fio}</div></div>
      </div>
    </div>
  );
  return <section style={{ marginTop: "10mm", display: "flex", gap: "12mm", breakInside: "avoid" }}>{line(t.signDone)}{line(t.signAcc)}</section>;
}

function PdHeader({ t, guides, org }) {
  return (
    <header style={{ display: "flex", gap: "8mm", alignItems: "flex-start", paddingBottom: "4mm", borderBottom: "1.2pt solid #000" }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={pdTxt("title", 700, { lineHeight: 1.15, letterSpacing: "-0.01em" })}>{t.title}</div>
        <div style={pdTxt("body", 500, { marginTop: "1.5mm" })}>№ {PD_NO} · {t.date}</div>
      </div>
      {org && (
        <div style={{ textAlign: "right", maxWidth: "70mm" }}>
          <div style={pdTxt("body", 700)}>{t.org}</div>
          <div style={pdTxt("small", 400, { color: "#333", marginTop: "0.5mm" })}>{t.orgAddr}</div>
        </div>
      )}
      <div style={{ width: "30mm", height: "15mm", flex: "none", outline: guides ? "0.6pt dashed #2F6BFF" : "none", display: "grid", placeItems: "center" }}>
        {guides && <span style={{ fontFamily: "var(--font-mono)", fontSize: "6.5pt", color: "#2F6BFF" }}>логотип · позже</span>}
      </div>
    </header>
  );
}

function PdPage({ n, total, t, guides, children, label }) {
  const ref = React.useRef(null);
  const [over, setOver] = React.useState(false);
  React.useLayoutEffect(() => {
    const el = ref.current; if (!el) return;
    const check = () => { const o = el.scrollHeight > el.clientHeight + 1; setOver(o); if (o) console.error(`PassportDoc: лист «${label}» переполнен на ${el.scrollHeight - el.clientHeight}px`); };
    check(); document.fonts && document.fonts.ready.then(check);
  });
  return (
    <div className="pd-page" data-screen-label={label} style={{ width: "210mm", height: "297mm", boxSizing: "border-box", padding: "15mm", background: "#fff", color: "#000", position: "relative", overflow: "hidden", boxShadow: "0 2px 12px rgba(0,0,0,.18)", outline: guides ? "none" : undefined, flex: "none" }}>
      {guides && <div style={{ position: "absolute", inset: "15mm", outline: "0.5pt dashed rgba(47,107,255,.5)", pointerEvents: "none" }} />}
      {over && <div style={{ position: "absolute", inset: 0, outline: "3px solid red", pointerEvents: "none" }} />}
      <div ref={ref} style={{ height: "calc(297mm - 30mm - 8mm)", overflow: "hidden" }}>{children}</div>
      <footer style={{ position: "absolute", left: "15mm", right: "15mm", bottom: "10mm", display: "flex", justifyContent: "space-between", borderTop: "0.3pt solid #999", paddingTop: "1.5mm" }}>
        <span style={pdTxt("small", 400, { color: "#333" })}>{t.title} № {PD_NO}</span>
        <span style={pdTxt("small", 400, { color: "#333" })}>{t.page(n, total)}</span>
      </footer>
    </div>
  );
}

// ── Сборка вариантов ──────────────────────────────────────────────────────────
function PassportDocPages({ variant = "full", lang = "ru", guides = false, data }) {
  const t = PD_T[lang];
  const two = (a, b) => <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "8mm", marginTop: "6mm" }}>{a}{b}</div>;
  const people = (withClient) => two(
    <section><PdH>{t.master}</PdH><PdRows labelW="22mm" rows={[...t.m, [lang === "ru" ? "Организация" : "Company", t.org], [lang === "ru" ? "Адрес" : "Address", t.orgAddr]]} /></section>,
    withClient ? <section><PdH>{t.client}</PdH><PdRows labelW="22mm" rows={t.c} /></section> : <div />
  );
  const car = <section><PdH>{t.vehicle}</PdH><PdRows labelW="30mm" rows={t.car} mono={["VIN"]} /></section>;
  const eq = <section><PdH>{t.eq}</PdH><PdRows labelW="30mm" rows={t.eqRows} mono={["IMEI", "ID устройства", "Device ID", "Серийный номер", "Serial number"]} /></section>;
  const cfg = <PdSection title={t.cfg}><PdRows rows={t.cfgRows} /></PdSection>;
  const works = <PdSection title={t.works}><PdRows rows={t.workRows} /></PdSection>;
  const checks11 = t.checks.map((c, i) => [c, PD_RES11[i]]);
  const devs = (
    <PdSection title={t.devices}>
      <table style={{ width: "100%", borderCollapse: "collapse", tableLayout: "fixed" }}>
        <colgroup><col /><col style={{ width: "20mm" }} /><col style={{ width: "24mm" }} /><col style={{ width: "46mm" }} /></colgroup>
        <thead><tr>{[t.type, t.num, t.fw, t.addr].map((h) => <th key={h} style={pdTxt("small", 700, { textAlign: "left", padding: "1mm 0", borderBottom: "0.6pt solid #000" })}>{h}</th>)}</tr></thead>
        <tbody>{t.devs.map(([a, b, f, c]) => (
          <tr key={c}>{[a, b, f, c].map((x, i) => <td key={i} style={pdTxt("body", 400, { padding: "0.9mm 0", borderBottom: "0.3pt solid #bbb", ...(i === 3 ? { fontVariantNumeric: "tabular-nums", letterSpacing: "0.02em" } : {}) })}>{x}</td>)}</tr>
        ))}</tbody>
      </table>
    </PdSection>
  );
  const notes = <PdSection title={t.notes}><p style={pdTxt("body", 400, { margin: 0, textWrap: "pretty" })}>{t.note}</p></PdSection>;
  const P = (n, total, label, kids) => <PdPage key={n} n={n} total={total} t={t} guides={guides} label={label}>{kids}</PdPage>;

  if (variant === "live") return pdLive(t, lang, guides, data || {}, { P, two, eq, cfg, devs, people });
  if (variant === "full") {
    const ok = PD_RES11.filter((r) => r === "ok").length;
    return [
      P(1, 3, "Полный · 1", <React.Fragment>
        <PdHeader t={t} guides={guides} org />
        {two(car, eq)}
        {cfg}{works}
        <PdSection title={t.check} aside={t.passed(ok, 11)}><PdCheckTable t={t} items={checks11} /></PdSection>
      </React.Fragment>),
      P(2, 3, "Полный · 2", <React.Fragment>
        {devs}
        {notes}
        <PdSection title={t.photos}><PdPhotos caps={t.caps.slice(0, 2)} /></PdSection>
      </React.Fragment>),
      P(3, 3, "Полный · 3 (подписи не отрываются)", <React.Fragment>
        <PdSection title={t.photos + (lang === "ru" ? " — продолжение" : " — continued")} mt="0"><PdPhotos caps={t.caps.slice(2, 4)} from={2} /></PdSection>
        {people(true)}
        <PdSign t={t} />
      </React.Fragment>),
    ];
  }
  if (variant === "min" || variant === "noblock") {
    const rows = variant === "noblock" ? t.eqRows.slice(0, 3) : t.eqRows;
    return [P(1, 1, variant === "min" ? "Минимальный" : "Без блока", <React.Fragment>
      <PdHeader t={t} guides={guides} />
      <PdSection title={t.eq}>
        <div style={{ display: "grid", gridTemplateColumns: variant === "noblock" ? "1fr" : "1fr 1fr", gap: "0 8mm" }}>
          {variant === "noblock" ? <PdRows rows={rows} /> : <React.Fragment><PdRows rows={rows.slice(0, 4)} labelW="30mm" /><PdRows rows={rows.slice(4)} labelW="30mm" /></React.Fragment>}
        </div>
        {variant === "noblock" && (
          <div style={{ display: "flex", alignItems: "center", gap: "2mm", marginTop: "2mm" }}>
            <span className="material-icons" style={{ fontSize: "10pt" }}>edit_note</span>
            <span style={pdTxt("small", 500, { fontStyle: "italic" })}>{t.manual}</span>
          </div>
        )}
      </PdSection>
      <PdSection title={t.master}><PdRows rows={t.m} /></PdSection>
      <PdSign t={t} />
    </React.Fragment>)];
  }
  // long: 30 заданий, 8 фото, перенос таблицы с повтором заголовка
  const res30 = [...PD_RES11, ...t.checksLong.map((_, i) => (i === 7 ? "fail" : i === 13 ? "skip" : "ok"))];
  const checks30 = [...t.checks, ...t.checksLong.slice(0, 19)].map((c, i) => [c, res30[i]]);
  const ok30 = checks30.filter((c) => c[1] === "ok").length;
  const SPLIT = 11;
  return [
    P(1, 4, "Длинный · 1", <React.Fragment>
      <PdHeader t={t} guides={guides} org />
      {two(car, eq)}
      {cfg}{works}
      <PdSection title={t.check} aside={t.passed(ok30, 30)}><PdCheckTable t={t} items={checks30.slice(0, SPLIT)} /></PdSection>
    </React.Fragment>),
    P(2, 4, "Длинный · 2", <React.Fragment>
      <PdSection title={t.checkCont} mt="0"><PdCheckTable t={t} items={checks30.slice(SPLIT)} start={SPLIT} /></PdSection>
      {devs}
    </React.Fragment>),
    P(3, 4, "Длинный · 3", <React.Fragment>
      {notes}
      <PdSection title={t.photos}><PdPhotos caps={t.caps.slice(0, 6)} /></PdSection>
    </React.Fragment>),
    P(4, 4, "Длинный · 4", <React.Fragment>
      <PdSection title={t.photos + (lang === "ru" ? " — продолжение" : " — continued")} mt="0"><PdPhotos caps={t.caps.slice(6)} from={6} /></PdSection>
      {people(true)}
      <PdSign t={t} />
    </React.Fragment>),
  ];
}

// Живой документ для предпросмотра рядом с редактором: только заполненное; фото — 2 на стр. 2, далее по 4.
// data: { scenario, v: {vin, plate, mileage, notes, client, phone, org, addr, cfgName, manualCar}, photos: n, works: {name: bool} }
function pdLive(t, lang, guides, d, k) {
  const v = d.v || {};
  const sc = d.scenario || "fast";
  const fast = sc === "fast" || sc === "partial";
  const noblock = sc === "noblock";
  const carRows = [
    ...(fast ? t.car.slice(0, 2) : v.manualCar ? [[t.car[0][0], v.manualCar]] : []),
    ...(v.plate ? [[t.car[2][0], v.plate]] : []), ...(v.vin ? [["VIN", v.vin]] : []), ...(v.mileage ? [[t.car[4][0], `${v.mileage} км`]] : []),
  ];
  const eqRows = noblock ? t.eqRows.slice(0, 3) : sc === "partial" ? t.eqRows.filter((r) => !["Серийный номер", "Загрузчик", "Bluetooth", "IMEI"].includes(r[0])) : t.eqRows;
  const eqBlock = (
    <section><PdH>{t.eq}</PdH><PdRows labelW="30mm" rows={eqRows} />
      {noblock && <div style={pdTxt("small", 500, { fontStyle: "italic", marginTop: "1.5mm" })}>{t.manual}</div>}
    </section>
  );
  const top = carRows.length
    ? k.two(<section><PdH>{t.vehicle}</PdH><PdRows labelW="30mm" rows={carRows} /></section>, eqBlock)
    : <div style={{ marginTop: "6mm" }}>{eqBlock}</div>;
  const cfgRows = fast ? t.cfgRows : v.cfgName ? [[t.cfgRows[0][0], v.cfgName]] : [];
  const workRows = fast ? t.workRows : Object.entries(d.works || {}).filter(([, on]) => on).map(([n]) => [n, "выполнено"]);
  const ok = PD_RES11.filter((r) => r === "ok").length;
  const check = fast ? <PdSection title={t.check} aside={t.passed(ok, 11)}><PdCheckTable t={t} items={t.checks.map((c, i) => [c, PD_RES11[i]])} /></PdSection> : null;
  const devs = !noblock && sc !== "partial" ? k.devs : null;
  const notes = v.notes ? <PdSection title={t.notes}><p style={pdTxt("body", 400, { margin: 0, whiteSpace: "pre-wrap", textWrap: "pretty" })}>{v.notes}</p></PdSection> : null;
  const mRows = [...t.m, ...(v.org ? [["Организация", v.org]] : []), ...(v.addr ? [["Адрес", v.addr]] : [])];
  const cRows = [...(v.client ? [["Ф. И. О.", v.client]] : []), ...(v.phone ? [["Телефон", v.phone]] : [])];
  const people = cRows.length
    ? k.two(<section><PdH>{t.master}</PdH><PdRows labelW="22mm" rows={mRows} /></section>, <section><PdH>{t.client}</PdH><PdRows labelW="22mm" rows={cRows} /></section>)
    : <PdSection title={t.master}><PdRows rows={mRows} /></PdSection>;
  const n = d.photos || 0;
  const caps = d.caps ? d.caps.slice(0, n) : t.caps.slice(0, n);
  const header = (
    <header style={{ display: "flex", gap: "8mm", alignItems: "flex-start", paddingBottom: "4mm", borderBottom: "1.2pt solid #000" }}>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={pdTxt("title", 700, { lineHeight: 1.15 })}>{t.title}</div>
        <div style={pdTxt("body", 500, { marginTop: "1.5mm" })}>№ {PD_NO} · {t.date}</div>
      </div>
      {(v.org || v.addr) && <div style={{ textAlign: "right", maxWidth: "70mm" }}>{v.org && <div style={pdTxt("body", 700)}>{v.org}</div>}{v.addr && <div style={pdTxt("small", 400, { color: "#333", marginTop: "0.5mm" })}>{v.addr}</div>}</div>}
      <div style={{ width: "30mm", height: "15mm", flex: "none" }} />
    </header>
  );
  const first = <React.Fragment>{header}{top}{cfgRows.length ? <PdSection title={t.cfg}><PdRows rows={cfgRows} /></PdSection> : null}{workRows.length ? <PdSection title={t.works}><PdRows rows={workRows} /></PdSection> : null}{check}</React.Fragment>;
  // Короткий паспорт (без проверки и фото) — одна страница.
  if (!fast && n === 0) {
    return [k.P(1, 1, "Предпросмотр · 1", <React.Fragment>{first}{devs}{notes}{people}<PdSign t={t} /></React.Fragment>)];
  }
  const rest = n > 2 ? Math.ceil((n - 2) / 4) : 0;
  const total = 2 + rest;
  const pages = [k.P(1, total, "Предпросмотр · 1", <React.Fragment>{first}{!fast && devs}</React.Fragment>)];
  const photos2 = n ? <PdSection title={t.photos}><PdPhotos caps={caps.slice(0, 2)} /></PdSection> : null;
  pages.push(k.P(2, total, "Предпросмотр · 2", <React.Fragment>{fast && devs}{notes}{photos2}{rest === 0 && people}{rest === 0 && <PdSign t={t} />}</React.Fragment>));
  for (let i = 0; i < rest; i++) {
    const last = i === rest - 1;
    pages.push(k.P(3 + i, total, `Предпросмотр · ${3 + i}`, <React.Fragment>
      <PdSection title={t.photos + " — продолжение"} mt="0"><PdPhotos caps={caps.slice(2 + i * 4, 6 + i * 4)} from={2 + i * 4} /></PdSection>
      {last && people}{last && <PdSign t={t} />}
    </React.Fragment>));
  }
  return pages;
}

Object.assign(window, { PassportDocPages });
