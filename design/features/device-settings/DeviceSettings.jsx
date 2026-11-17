
// DeviceSettingsScreen — «Расширенные настройки» (Устройство → Расширенный монтаж → Настройки).
// Меню приходит из устройства: дерево разделов (leaf = 12 типов настроек). Правки копятся в `edits`,
// в устройство пишутся только явно (панель «Изменено: N · Записать»).
// Адаптив (M3 Window Size Classes по ширине контейнера):
//   compact <600 — пошаговый переход по разделам + хлебные крошки, редактор = нижняя шторка;
//   medium 600–839 — две панели с узким деревом (232), редактор = диалог;
//   expanded ≥840 — две панели (дерево 280 + содержимое ≤720), редактор = диалог.
// demo: null | write-progress | write-error | write-ok | exit | reset | reset-progress | can | lost | menu |
//       search | search-busy | search-empty | info | edit:<id>   — готовые состояния для макетов.
(function () {
const DS = window.PandoraSpecDesignSystem_b8d6e1;
const { CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpSwitch, CmpSlider, CmpTextField, CmpLinearProgressIndicator, CmpCircularProgressIndicator, CmpSnackbar } = DS;
const { useState, useEffect, useRef } = React;

// ── Данные (имитация меню из устройства) ─────────────────────────────────────
const W = { ylw: "#E0B400", blk: "#1B1B1B", red: "#D32F2F", grn: "#2E7D32", blu: "#1565C0", wht: "#F2F2F2", brn: "#6D4C41", org: "#EF6C00" };
const S = (id, t, kids, o) => ({ id, t, type: "sec", kids, ...o });
const L = (id, t, type, o) => ({ id, t, type, ...o });
const TZ = Array.from({ length: 27 }, (_, i) => { const h = i - 12; return "UTC" + (h < 0 ? "−" : "+") + Math.abs(h) + ":00"; });
const SCH = (am, amT, pm, pmT) => ({ am, amT, pm, pmT });
const zone = (id, t) => L(id, t, "int", { min: 0, max: 10, v: 5, minLabel: "Выкл" });
const sir = (id, t, type, num) => L(id, t, "sir", { v: { type, num } });

const FN = ["Блокировка 1 NC","Блокировка 1 NO","Зажигание","Стартер","Обходчик иммобилайзера","Указатели поворотов","Открытие ЦЗ","Закрытие ЦЗ","Канал ACC","Кодовый канал (реле)","Кодовый канал (HM-05)","Кодовый канал (обходчик)","Предпусковой подогреватель","Выключение ACC после глушения двигателя","Управление капотом","Управление багажником","Сирена","Световая индикация","Подогрев сидений","Подогрев руля","Подогрев зеркал","Подогрев лобового стекла","Складывание зеркал","Закрытие стёкол","Открытие стёкол","Люк","Режим «Комфорт»","Режим «Сервис»","Режим «Валет»","Блокировка двигателя","Блокировка КПП","Дополнительный канал 1","Дополнительный канал 2","Дополнительный канал 3","Дополнительный канал 4","Ближний свет","Дальний свет","Габариты","Противотуманные фары","Аварийная сигнализация","Звуковой сигнал","Задний ход","Стоп-сигнал","Ручной тормоз","Нейтраль","Педаль тормоза","Зажигание 2","Зажигание 3","Стартер 2","Датчик удара","Датчик объёма","Датчик наклона","Доп. датчик","Тревожная кнопка","Сброс тревоги","Режим парковки","Режим эвакуации","Тихая охрана","Отключение сирены","Отключение датчика удара","Двери","Багажник","Капот","Дверь водителя","Дверь пассажира","Задние двери","Концевик капота","Концевик багажника","Автозапуск по температуре","Автозапуск по времени","Автозапуск по напряжению","Турботаймер","Прогрев салона","Прогрев двигателя","Обдув стекла","Кондиционер","Вентилятор салона","Топливный насос","Масляный насос"].map((n) => ({ n, on: false }));
const fnOn = (names) => FN.map((f) => ({ ...f, on: names.includes(f.n) }));
const TREE = [
  S("1", "Общие", [
    L("1.1", "Имя устройства", "txt", { v: "Volvo XC60", maxLen: 16, kb: "text", pattern: "^[A-Za-zА-Яа-я0-9 \\-]*$", err: "Допустимы буквы, цифры, пробел и дефис", desc: "Название, под которым устройство отображается в приложении и в журнале событий." }),
    L("1.2", "Язык голосовых сообщений", "sel", { desc: "Язык, на котором устройство произносит голосовые сообщения.", opts: ["Русский", "English", "Українська", "Polski"], v: 0 }),
    L("1.3", "Часовой пояс", "sel", { opts: TZ, v: 15, desc: "Нужен для расписания автозапуска и отметок времени в журнале событий." }),
    L("1.4", "Звук подтверждения", "sw", { v: true, on: "Включён", off: "Выключен", desc: "Короткий сигнал сирены при постановке и снятии." }),
    L("1.5", "Таймаут сервисного режима", "int", { desc: "Через сколько минут бездействия устройство выйдет из сервисного режима.", min: 1, max: 60, v: 10, unit: "мин" }),
    L("1.6", "Служебный параметр изготовителя", "sw", { v: true, disabled: true }),
    L("1.7", "Название метки при блокировке запуска двигателя с открытым капотом в режиме автозапуска по температуре", "txt", { v: "Метка клиента: Иванов Иван Иванович, тел. +7 900 000-00-00", maxLen: 64, kb: "text" }),
  ]),
  S("2", "Автозапуск", [
    L("2.1", "Режим автозапуска", "sel", { desc: "Условие автозапуска: выключен, по температуре или по времени.", opts: ["Выкл", "Темп.", "Врем."], v: 1 }),
    L("2.2", "Время работы двигателя", "int", { min: 3, max: 30, v: 10, unit: "мин", desc: "Сколько двигатель работает после автозапуска." }),
    L("2.3", "Порог температуры", "dec", { desc: "Температура, ниже которой запускается автозапуск в режиме «Темп.».", min: -40, max: 20, step: 0.5, v: -5, unit: "°C" }),
    S("2.4", "Расписание", [
      L("2.4.1", "Понедельник", "sch", { v: SCH(true, "07:30", false, "18:00") }),
      L("2.4.2", "Вторник", "sch", { v: SCH(true, "07:30", true, "18:00") }),
      L("2.4.3", "Среда", "sch", { v: SCH(false, "07:30", false, "18:00") }),
    ]),
    L("2.5", "Ежедневный запуск", "time", { v: "07:30" }),
  ]),
  S("3", "Охрана", [
    S("3.1", "Зоны", [zone("3.1.1", "Зона 1 · предупреждение"), zone("3.1.2", "Зона 1 · тревога"), zone("3.1.3", "Зона 2 · предупреждение")]),
    S("3.2", "Датчики", [
      L("3.2.1", "Датчик удара", "sw", { v: true, desc: "Реагирует на удар по кузову." }),
      L("3.2.2", "Чувствительность", "int", { min: 0, max: 10, v: 5, minLabel: "Выкл", desc: "0 — датчик выключен, 10 — максимальная чувствительность." }),
      L("3.2.3", "Режим", "sel", { desc: "Импульсный или постоянный сигнал при срабатывании датчика.", opts: ["Имп", "Пост", "Выкл"], v: 0 }),
      L("3.2.6", "Реакция на срабатывание датчика при включённой охране", "sel", { v: 3, desc: "Что делает устройство при срабатывании датчика в режиме охраны.", opts: ["Ничего не делать", "Только световая индикация", "Короткий сигнал сирены", "Сирена и световая индикация в течение 30 секунд", "Сирена, свет и блокировка двигателя до снятия с охраны", "Уведомление владельцу без сирены", "Сирена, свет и уведомление владельцу по GSM и Bluetooth", "Полная тревога с отправкой координат в сервисный центр"] }),
      L("3.2.4", "Задержка постановки", "int", { min: 0, max: 120, step: 5, v: 30, unit: "с", minLabel: "Выкл", desc: "Время от закрытия дверей до перехода в охрану." }),
      S("3.2.5", "Доп. датчики", [
        L("3.2.5.1", "Датчик наклона", "sw", { v: false }),
        L("3.2.5.2", "Датчик объёма", "int", { min: 0, max: 10, v: 3, minLabel: "Выкл" }),
        L("3.2.5.3", "Микроволновый датчик", "sw", { v: false, on: "Включён", off: "Выключен" }),
      ]),
    ]),
    S("3.3", "Сирена", [
      sir("3.3.1", "Сирена · постановка", "Тон", 2), sir("3.3.2", "Сирена · снятие", "Тон", 3),
      sir("3.3.3", "Сирена · предупреждение", "Мелодия", 3), sir("3.3.4", "Сирена · тревога", "Мелодия", 12),
    ]),
  ]),
  S("4", "Входы", [
    L("4.1", "IN1", "in", { name: "Концевик дверей", wires: [{ c: [W.ylw, W.blk], n: "Жёлто-чёрный", pin: 5 }], defPol: "−", v: { type: 1, logics: fnOn(["Двери","Багажник"]) } }),
    L("4.2", "IN2", "in", { name: "Педаль тормоза", wires: [{ c: [W.red], n: "Красный", pin: 12 }, { c: [], n: "Без цвета", pin: 13 }], defPol: "+", desc: "Вход сигнала стоп-сигнала.", v: { type: 0, logics: fnOn(["Стоп-сигнал","Педаль тормоза"]) } }),
    L("4.3", "IN3", "in", { name: "Датчик заднего хода при установленной дополнительной камере обзора", wires: [{ c: [W.grn, W.wht, W.blk], n: "Зелёно-бело-чёрный", pin: 8 }], defPol: "−", v: { type: 0, logics: fnOn([]) } }),
  ]),
  S("5", "Выходы", [
    { id: "5.0a", t: "Основные выходы", type: "div" },
    L("5.1", "CH1", "out", { name: "Центральный замок", wires: [{ c: [W.blu], n: "Синий", pin: 1 }, { c: [W.blu, W.wht], n: "Сине-белый", pin: 2 }], polEditable: true, v: { pol: 1, logics: fnOn(["Блокировка 1 NC","Блокировка 1 NO","Зажигание","Стартер","Обходчик иммобилайзера","Указатели поворотов","Открытие ЦЗ","Закрытие ЦЗ","Канал ACC","Кодовый канал (реле)","Кодовый канал (HM-05)","Кодовый канал (обходчик)","Предпусковой подогреватель","Выключение ACC после глушения двигателя"]) } }),
    L("5.2", "CH2", "out", { name: "Стартер", wires: [{ c: [W.org, W.blk], n: "Оранжево-чёрный", pin: 14 }], polEditable: false, v: { pol: 0, logics: fnOn(["Автозапуск по температуре"]) } }),
    L("5.3", "SIREN", "osw", { name: "Дополнительный канал", wires: [{ c: [W.brn], n: "Коричневый", pin: 6 }], polEditable: false, v: { pol: 0, on: true, logics: fnOn(["Подогрев сидений"]) } }),
    { id: "5.0b", t: "Дополнительные выходы", type: "div" },
    L("5.4", "R1", "out", { name: "Реле 1", wires: [], polEditable: false, v: { pol: 0, logics: fnOn([]) } }),
    L("5.5", "R2", "out", { name: "Реле 2", wires: [], polEditable: false, v: { pol: 0, logics: fnOn(["Сирена"]) } }),
  ]),
  S("6", "CAN", [
    L("6.1", "CAN-код автомобиля", "can", { can: true, v: "2154", desc: "Четыре цифры — код автомобиля в базе CAN." }),
    L("6.2", "Скорость шины", "sel", { can: true, opts: ["125", "250", "500"], v: 2 }),
    L("6.3", "Передача состояний по CAN", "sw", { can: true, v: true }),
  ]),
  S("7", "Резерв", []),
];
const CARS = { "2154": ["Volkswagen Polo, 2015–2020"], "7001": ["Kia Rio, 2017–2020", "Hyundai Solaris, 2017–2020"] };
const IDX = {};
(function walk(ns, parent) { ns.forEach((n) => { IDX[n.id] = { n, parent }; if (n.kids) walk(n.kids, n.id); }); })(TREE, "");
const LEAVES = Object.values(IDX).map((x) => x.n).filter((n) => n.type !== "sec" && n.type !== "div");
const chain = (id) => { const r = []; while (id) { r.unshift(IDX[id].n); id = IDX[id].parent; } return r; };
const BASE0 = {}; LEAVES.forEach((n) => { BASE0[n.id] = n.v; });
const SEED = { "3.2.2": 7, "3.2.5.1": true, "3.2.5.2": 6 };
const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
const countIn = (n, edits) => n.type !== "sec" ? (edits[n.id] !== undefined ? 1 : 0) : n.kids.reduce((s, k) => s + countIn(k, edits), 0);
const nf = (x, dp) => (dp ? Number(x).toFixed(dp) : String(x)).replace(".", ",").replace("-", "−");
const dpOf = (it) => it.type === "dec" ? (String(it.step || 1).split(".")[1] || "").length || 1 : 0;
function fmt(it, v) {
  switch (it.type) {
    case "sw": return v ? (it.on || "Вкл") : (it.off || "Выкл");
    case "osw": return v.on ? "Вкл" : "Выкл";
    case "sel": return it.opts[v];
    case "int": case "dec":
      if (v === it.min && it.minLabel) return it.minLabel;
      if (v === it.max && it.maxLabel) return it.maxLabel;
      return nf(v, dpOf(it)) + (it.unit ? " " + it.unit : "");
    case "txt": return v || "—";
    case "time": return v;
    case "sch": return "Утро " + (v.am ? v.amT : "—") + " · Вечер " + (v.pm ? v.pmT : "—");
    case "sir": return v.type + " " + v.num;
    case "can": return v;
    case "in": return ["НО", "НЗ"][v.type] + " · функций: " + v.logics.filter((l) => l.on).length;
    case "out": return ["ПЛЮС", "МИНУС"][v.pol] + " · функций: " + v.logics.filter((l) => l.on).length;
    default: return "";
  }
}

// ── Стили/анимации ───────────────────────────────────────────────────────────
(function () {
  if (document.getElementById("ds-anim")) return;
  const s = document.createElement("style"); s.id = "ds-anim";
  s.textContent = "@keyframes dsUp{from{transform:translateY(40px);opacity:0}to{transform:none;opacity:1}}@keyframes dsPop{from{transform:scale(.96);opacity:0}to{transform:none;opacity:1}}@keyframes dsFade{from{opacity:0}to{opacity:1}}.ds-hide::-webkit-scrollbar{display:none}@media (prefers-reduced-motion:reduce){.ds-an{animation:none!important}}";
  document.head.appendChild(s);
})();
const mono = { fontFamily: "var(--font-mono)" };
const CH_BG = "color-mix(in srgb, var(--tertiary-container) 50%, transparent)";

// ── Новые компоненты кита ────────────────────────────────────────────────────
function Segmented({ opts, value, onChange, h = 48, disabled, style }) {
  return (
    <div role="radiogroup" style={{ display: "flex", height: h, borderRadius: 999, border: "1px solid var(--border-light)", overflow: "hidden", opacity: disabled ? 0.38 : 1, boxSizing: "border-box", ...style }}>
      {opts.map((o, i) => {
        const on = i === value;
        return (
          <button key={i} role="radio" aria-checked={on} onClick={(e) => { e.stopPropagation(); if (!disabled) onChange(i); }}
            style={{ flex: 1, minWidth: 0, border: "none", borderLeft: i ? "1px solid var(--border-light)" : "none", background: on ? "var(--secondary-container)" : "transparent", color: on ? "var(--on-secondary-container)" : "var(--on-surface)", font: "500 15px var(--font-sans)", display: "flex", alignItems: "center", justifyContent: "center", gap: 6, padding: "0 8px", cursor: disabled ? "default" : "pointer" }}>
            {on && <CmpIcon icon="Check" size={18} />}<span style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{o}</span>
          </button>
        );
      })}
    </div>
  );
}

function useHold(fn) {
  const f = useRef(fn); f.current = fn;
  const t = useRef();
  const stop = () => { clearTimeout(t.current); clearInterval(t.current); };
  useEffect(() => stop, []);
  return { onPointerDown: (e) => { e.preventDefault(); stop(); f.current(); t.current = setTimeout(() => { t.current = setInterval(() => f.current(), 80); }, 400); }, onPointerUp: stop, onPointerLeave: stop, onPointerCancel: stop };
}
function StepBtn({ icon, fn, label, size = 64 }) {
  const h = useHold(fn);
  return (
    <button aria-label={label} {...h} style={{ width: size, height: size, flex: "0 0 auto", border: "none", borderRadius: "var(--r-card-medium-radius)", background: "var(--secondary-container)", color: "var(--on-secondary-container)", display: "grid", placeItems: "center", cursor: "pointer", touchAction: "none" }}>
      <CmpIcon icon={icon} size={32} />
    </button>
  );
}

function WireTag({ c = [], n, pin }) {
  const sw = c.length === 0
    ? { background: "repeating-linear-gradient(135deg, var(--outline) 0 3px, transparent 3px 6px)" }
    : { background: c.length === 1 ? c[0] : `linear-gradient(90deg, ${c.map((x, i) => `${x} ${(i * 100 / c.length).toFixed(2)}% ${((i + 1) * 100 / c.length).toFixed(2)}%`).join(", ")})` };
  return (
    <span title={n} style={{ display: "inline-flex", alignItems: "center", gap: 8, minHeight: 32, padding: "3px 12px 3px 6px", borderRadius: "var(--r-card-medium-radius)", border: "1px solid var(--border-highlight)", background: "var(--surface)", flex: "0 0 auto", maxWidth: "100%", boxSizing: "border-box" }}>
      <span style={{ width: 34, height: 18, flex: "0 0 auto", borderRadius: 9, border: "1px solid var(--border-light)", boxSizing: "border-box", ...sw }} />
      <span style={{ fontSize: "var(--t-additional0-size)", fontWeight: 700, lineHeight: 1.25, color: "var(--on-surface)" }}>{n}, <span style={{ whiteSpace: "nowrap" }}>Pin {pin}</span></span>
    </span>
  );
}
function Pol({ p, size = 28 }) {
  const plus = p === "+";
  return <span aria-label={"Полярность " + p} style={{ width: size, height: size, borderRadius: 999, flex: "0 0 auto", display: "grid", placeItems: "center", boxSizing: "border-box", font: "700 18px var(--font-sans)", lineHeight: 1, background: plus ? "var(--primary)" : "transparent", color: plus ? "var(--on-primary)" : "var(--on-surface)", border: plus ? "2px solid var(--primary)" : "2px solid var(--on-surface)" }}>{p}</span>;
}
function CountPill({ n }) {
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 28, padding: "0 10px", borderRadius: 999, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", font: "600 14px var(--font-sans)" }}><span style={{ width: 8, height: 8, borderRadius: 999, background: "var(--tertiary)" }} />{n}</span>;
}
const Dot = () => <span style={{ display: "inline-block", width: 10, height: 10, borderRadius: 999, background: "var(--tertiary)", verticalAlign: "middle", flex: "0 0 auto" }} />;

function Breadcrumbs({ cur, edits, onGo }) {
  const ch = chain(cur);
  const items = [{ id: "", t: "Все" }, ...ch.map((n) => ({ id: n.id, t: n.id + " " + n.t, n }))];
  const shown = items.length > 3 ? [{ id: null, t: "…" }, ...items.slice(-2)] : items;
  return (
    <div className="ds-hide" style={{ display: "flex", alignItems: "center", minHeight: 48, padding: "0 8px 0 12px", overflowX: "auto", borderBottom: "1px solid var(--border-highlight)", background: "var(--surface)", flex: "0 0 auto" }}>
      {shown.map((it, i) => {
        const last = i === shown.length - 1;
        const dot = it.n && countIn(it.n, edits) > 0;
        return (
          <React.Fragment key={i}>
            {i > 0 && <CmpIcon icon="chevron_right" size={20} color="var(--on-surface-variant)" />}
            <button disabled={it.id === null} onClick={() => it.id !== null && onGo(it.id)} style={{ height: 48, minWidth: 48, padding: "0 8px", border: "none", background: "transparent", whiteSpace: "nowrap", display: "inline-flex", alignItems: "center", gap: 6, font: `${last ? 600 : 500} 15px var(--font-sans)`, color: last ? "var(--on-surface)" : "var(--primary)", cursor: "pointer" }}>
              {it.t}{dot && <Dot />}
            </button>
          </React.Fragment>
        );
      })}
    </div>
  );
}

function Tree({ sel, exp, toggle, select, edits, width }) {
  const rows = [];
  const walk = (ns, d) => ns.filter((n) => n.type === "sec").forEach((n) => {
    const has = n.kids.some((k) => k.type === "sec"); const open = exp.includes(n.id); const on = sel === n.id; const c = countIn(n, edits);
    rows.push(
      <div key={n.id} style={{ display: "flex", alignItems: "center", minHeight: 48, margin: "0 8px", paddingLeft: d * 18, borderRadius: 999, background: on ? "var(--secondary-container)" : "transparent", color: on ? "var(--on-secondary-container)" : "var(--on-surface)" }}>
        <button aria-label={open ? "Свернуть" : "Развернуть"} onClick={() => has && toggle(n.id)} style={{ width: 40, height: 48, flex: "0 0 auto", border: "none", background: "transparent", color: "inherit", display: "grid", placeItems: "center", cursor: has ? "pointer" : "default" }}>
          {has && <CmpIcon icon={open ? "expand_more" : "chevron_right"} size={22} />}
        </button>
        <button onClick={() => select(n.id)} style={{ flex: 1, minWidth: 0, minHeight: 48, border: "none", background: "transparent", color: "inherit", textAlign: "left", display: "flex", alignItems: "center", gap: 8, padding: "0 12px 0 0", cursor: "pointer", font: `${on ? 600 : 400} 15px var(--font-sans)` }}>
          <span style={{ ...mono, fontSize: "var(--t-additional0-size)", opacity: 0.75 }}>{n.id}</span>
          <span style={{ flex: 1, minWidth: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{n.t}</span>
          {c > 0 && <CountPill n={c} />}
        </button>
      </div>
    );
    if (open && has) walk(n.kids, d + 1);
  });
  walk(TREE, 0);
  return <div style={{ width, flex: "0 0 auto", overflow: "auto", padding: "8px 0", boxSizing: "border-box", borderRight: "1px solid var(--border-highlight)", background: "var(--background-surface-card)" }}>{rows}</div>;
}

// ── Строка настройки ─────────────────────────────────────────────────────────
function InfoBtn({ onClick, off }) {
  return <button aria-label="Описание" disabled={off} onClick={(e) => { e.stopPropagation(); onClick(); }} style={{ position: "absolute", left: 0, top: 4, zIndex: 1, width: 48, height: 48, border: "none", background: "transparent", color: off ? "var(--outline)" : "var(--primary)", opacity: off ? 0.5 : 1, display: "grid", placeItems: "center", cursor: off ? "default" : "pointer", borderRadius: 999 }}><CmpIcon icon="info_outline" size={24} /></button>;
}
function SettingRow({ it, val, changed, wide, path, count = 0, focus, onOpen, onInfo, onChangeVal, last }) {
  const sl = DS.useStateLayer ? DS.useStateLayer() : { opacity: 0, handlers: {} };
  if (it.type === "div") return <div role="separator" style={{ minHeight: 48, display: "flex", alignItems: "center", padding: "0 16px", background: "var(--background-surface-card)", borderTop: "1px solid var(--border-light)", borderBottom: "1px solid var(--border-highlight)", font: "600 15px var(--font-sans)", color: "var(--on-surface)", letterSpacing: 0.2 }}>{it.t}</div>;
  const v = val !== undefined ? val : it.v;
  const t = it.type, dis = !!it.disabled, isSec = t === "sec", io = t === "in" || t === "out" || t === "osw";
  const seg = t === "sel" && it.opts.length <= 3 && it.opts.every((o) => o.length <= 6);
  const sw = t === "sw" || t === "osw";
  const swOn = t === "osw" ? v.on : v;
  const vtxt = (t === "txt" || (t === "sel" && !seg)) ? fmt(it, v) : "";
  const stackVal = vtxt.length > (t === "txt" ? 12 : 16);
  const title = (
    <CmpText variant="bodyLarge" maxLines={2} style={{ wordBreak: "break-word" }}>
      <span style={{ ...mono, fontSize: "var(--t-additional0-size)", color: "var(--on-surface-variant)", marginRight: 10 }}>{it.id}</span>
      {io ? (it.t + " · " + it.name) : it.t}{changed && <span style={{ marginLeft: 8 }}><Dot /></span>}
    </CmpText>
  );
  const isIn = t === "in", labs = isIn ? ["НО", "НЗ"] : ["ПЛЮС", "МИНУС"], cu = io && (isIn ? v.type : v.pol), edP = isIn || it.polEditable;
  const cyc = () => onChangeVal(isIn ? { ...v, type: (cu + 1) % 2 } : { ...v, pol: (cu + 1) % 2 });
  const ioHead = io ? (
    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <div style={{ display: "flex", alignItems: "center", columnGap: 10, rowGap: 4, minHeight: 40 }}>
        <span style={{ ...mono, fontSize: "var(--t-additional0-size)", color: "var(--on-surface-variant)" }}>{it.id}</span>
        <span style={{ font: "600 19px var(--font-sans)", color: "var(--on-surface)" }}>{it.t}</span>
        {t !== "osw" && (isIn
          ? <span style={{ display: "inline-flex", alignItems: "center", height: 28, padding: "0 10px", borderRadius: 999, background: "var(--surface-variant)", color: "var(--on-surface)", font: "700 14px var(--font-sans)" }}>{labs[cu]}</span>
          : <Pol p={["+", "−"][cu]} />)}
        {changed && <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 26, padding: "0 10px", borderRadius: 999, background: "var(--tertiary-container)", color: "var(--on-tertiary-container)", font: "600 13px var(--font-sans)" }}><Dot />Изменено</span>}
        <span style={{ flex: 1 }} />
        {t !== "osw" && (edP
          ? <button aria-label={"Сменить: " + labs[cu]} title={"Сменить: " + labs[cu]} onClick={(e) => { e.stopPropagation(); cyc(); }} style={{ width: 48, height: 48, flex: "0 0 auto", border: "1px solid var(--border-light)", borderRadius: "var(--r-card-small-radius)", background: "transparent", color: "var(--primary)", display: "grid", placeItems: "center", cursor: "pointer" }}><CmpIcon icon="swap_horiz" size={24} /></button>
          : <span aria-label="Нельзя изменить" style={{ width: 48, height: 48, display: "grid", placeItems: "center", color: "var(--on-surface-variant)" }}><CmpIcon icon="lock" size={20} /></span>)}
      </div>
      {it.wires.length > 0 && <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>{it.wires.map((w, i) => <WireTag key={i} {...w} />)}</div>}
    </div>
  ) : null;
  const sub = path ? <CmpText variant="bodySmall" color="var(--on-surface-variant)" maxLines={1}>{path}</CmpText>
    : t === "sw" && (it.on || it.off) ? <CmpText variant="bodySmall" color="var(--on-surface-variant)">{fmt(it, v)}</CmpText>
    : t === "can" && (CARS[v] || [])[0] ? <CmpText variant="bodySmall" color="var(--on-surface-variant)" maxLines={1}>{CARS[v][0]}</CmpText> : null;
  const tap = () => { if (dis) return; if (sw) onChangeVal(t === "osw" ? { ...v, on: !v.on } : !v); else if (!seg) onOpen(it); };
  const polTxt = t === "in" ? null : (t === "out" ? ["+", "−"][v.pol] : null);
  return (
    <div role="button" tabIndex={dis ? -1 : 0} aria-disabled={dis} {...sl.handlers} onClick={tap}
      onKeyDown={(e) => { if ((e.key === "Enter" || e.key === " ") && !dis) { e.preventDefault(); tap(); } }}
      style={{ position: "relative", display: "flex", flexWrap: seg && !wide ? "wrap" : "nowrap", alignItems: "center", columnGap: 8, rowGap: 4, minHeight: 56, padding: `${io ? 10 : 4}px 4px ${io ? 10 : 4}px 16px`, boxSizing: "border-box", background: changed ? CH_BG : "transparent", borderBottom: last ? "none" : "1px solid var(--border-highlight)", opacity: dis ? 0.38 : 1, cursor: dis ? "default" : "pointer", outline: focus ? "2px solid var(--primary)" : "none", outlineOffset: -2 }}>
      <div style={{ position: "absolute", inset: 0, background: "var(--on-surface)", opacity: dis ? 0 : sl.opacity, pointerEvents: "none" }} />
      {!isSec && <InfoBtn off={!it.desc || dis} onClick={() => onInfo(it)} />}
      <div style={{ flex: seg && !wide ? "1 1 100%" : "1 1 0", minWidth: 0, display: "flex", flexDirection: "column", gap: io ? 6 : 0, padding: seg && !wide ? "8px 0 0" : "4px 0", paddingLeft: isSec ? 0 : 32, position: "relative" }}>
        {io ? ioHead : title}{sub}
        {stackVal && <span style={{ font: "600 16px/1.35 var(--font-sans)", color: "var(--primary)", wordBreak: "break-word", whiteSpace: "normal", paddingTop: 2 }}>{vtxt}</span>}
        
        {io && (
          <div style={{ display: "flex", flexWrap: "wrap", gap: 6, alignItems: "center" }}>
            {v.logics.filter((l) => l.on).length === 0
              ? <span style={{ font: "600 17px var(--font-sans)", color: "var(--primary)" }}>Не назначено</span>
              : <span style={{ font: "500 16px/1.4 var(--font-sans)", color: "var(--primary)", wordBreak: "break-word" }}>{v.logics.filter((l) => l.on).map((l) => l.n).join(", ")}</span>}
          </div>
        )}
      </div>
      {seg ? (
        <div style={{ position: "relative", flex: wide ? "0 0 300px" : "1 1 100%", padding: wide ? "4px 4px 4px 0" : "0 12px 8px 0" }}>
          <Segmented opts={it.opts} value={v} disabled={dis} onChange={(i) => onChangeVal(i)} />
        </div>
      ) : (
        <div style={{ position: "relative", display: "flex", alignItems: "center", flex: "0 1 auto", minWidth: 0, maxWidth: isSec || sw ? "none" : "46%" }}>
          
          {isSec && count > 0 && <CountPill n={count} />}
          {isSec && <CmpIcon icon="chevron_right" size={24} color="var(--on-surface-variant)" style={{ margin: "0 12px 0 6px" }} />}
          {sw && <div style={{ width: 64, height: 48, display: "grid", placeItems: "center" }}><div style={{ pointerEvents: "none" }}><CmpSwitch checked={!!swOn} disabled={dis} thumbIcon /></div></div>}
          {!isSec && !sw && (
            <React.Fragment>
              {!io && !stackVal && <span style={{ minWidth: 0, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", font: "600 16px var(--font-sans)", color: "var(--primary)", textAlign: "right", paddingLeft: 4 }}>{fmt(it, v)}</span>}
              
              <CmpIcon icon="chevron_right" size={24} color="var(--on-surface-variant)" style={{ margin: "0 12px 0 4px" }} />
            </React.Fragment>
          )}
        </div>
      )}
    </div>
  );
}

// ── Окна редактирования: каркас + содержимое по типу ─────────────────────────
function DBtn({ text, onClick, kind = "text", disabled, icon, style }) {
  const k = { filled: { background: "var(--primary)", color: "var(--on-primary)", border: "none" }, tonal: { background: "var(--secondary-container)", color: "var(--on-secondary-container)", border: "none" }, outlined: { background: "transparent", color: "var(--primary)", border: "1px solid var(--border-light)" }, danger: { background: "var(--error)", color: "var(--on-error)", border: "none" }, text: { background: "transparent", color: "var(--primary)", border: "none" } }[kind];
  return <button disabled={disabled} onClick={onClick} style={{ height: 52, minWidth: 64, padding: "0 20px", borderRadius: 999, font: "600 16px var(--font-sans)", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 8, cursor: disabled ? "default" : "pointer", opacity: disabled ? 0.38 : 1, boxSizing: "border-box", ...k, ...style }}>{icon && <CmpIcon icon={icon} size={22} />}{text}</button>;
}

function NumBody({ it, v, set }) {
  const dp = dpOf(it), step = it.step || 1;
  const clamp = (x) => +Math.min(it.max, Math.max(it.min, x)).toFixed(dp + 1);
  const ref = useRef(v); ref.current = v;
  const [typing, setTyping] = useState(false), [txt, setTxt] = useState(""), [bad, setBad] = useState(false);
  const move = (d) => () => { const n = clamp(ref.current + d * step); ref.current = n; set(n); setBad(false); };
  const onType = (s) => { setTxt(s); const n = parseFloat(s.replace(",", ".").replace("−", "-")); if (isNaN(n) || n < it.min || n > it.max) setBad(true); else { setBad(false); set(n); } };
  const lbl = (x) => (x === it.min && it.minLabel) || (x === it.max && it.maxLabel) || (nf(x, dp) + (it.unit ? " " + it.unit : ""));
  const big = (v === it.min && it.minLabel) ? it.minLabel : nf(v, dp);
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 88 }}>
        <StepBtn icon="remove" label="Меньше" fn={move(-1)} />
        <div style={{ flex: 1, minWidth: 0, textAlign: "center" }}>
          {typing ? (
            <CmpTextField value={txt} onChange={onType} singleLine isError={bad} inputMode="decimal" autoFocus supportingText={bad ? `Допустимо от ${nf(it.min, dp)} до ${nf(it.max, dp)}` : undefined} />
          ) : (
            <button onClick={() => { setTxt(String(v).replace(".", ",")); setTyping(true); }} aria-label="Ввести значение" style={{ border: "none", background: "transparent", color: "var(--on-surface)", cursor: "text", padding: "4px 8px", minHeight: 64, width: "100%" }}>
              <span style={{ font: "600 48px/1.1 var(--font-sans)", fontVariantNumeric: "tabular-nums" }}>{big}</span>
              {it.unit && !(v === it.min && it.minLabel) && <span style={{ font: "500 22px var(--font-sans)", color: "var(--on-surface-variant)", marginLeft: 8 }}>{it.unit}</span>}
            </button>
          )}
        </div>
        <StepBtn icon="add" label="Больше" fn={move(1)} />
      </div>
      <CmpSlider value={v} min={it.min} max={it.max} step={step} onValueChange={(x) => { set(clamp(x)); setBad(false); }} />
      <div style={{ display: "flex", justifyContent: "space-between", padding: "0 2px" }}>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{lbl(it.min)}</CmpText>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)">{lbl(it.max)}</CmpText>
      </div>
    </div>
  );
}
function Radio({ on }) {
  return <span style={{ width: 24, height: 24, borderRadius: 999, border: `2px solid ${on ? "var(--primary)" : "var(--outline)"}`, boxSizing: "border-box", display: "grid", placeItems: "center", flex: "0 0 auto" }}>{on && <span style={{ width: 12, height: 12, borderRadius: 999, background: "var(--primary)" }} />}</span>;
}
function Check({ on, size = 24 }) {
  return <span style={{ width: size, height: size, borderRadius: 4, border: on ? "none" : "2px solid var(--outline)", background: on ? "var(--primary)" : "transparent", boxSizing: "border-box", display: "grid", placeItems: "center", flex: "0 0 auto", color: "var(--on-primary)" }}>{on && <CmpIcon icon="Check" size={size - 4} />}</span>;
}
function SelBody({ it, v, set, tall }) {
  const box = useRef(null);
  useEffect(() => { const el = box.current && box.current.children[v]; if (el && box.current) box.current.scrollTop = Math.max(0, el.offsetTop - 56); }, []);
  return (
    <div ref={box} style={{ maxHeight: tall ? 380 : 360, overflowY: "auto", margin: "0 -8px", position: "relative" }}>
      {it.opts.map((o, i) => (
        <button key={i} role="radio" aria-checked={i === v} onClick={() => set(i)} style={{ display: "flex", alignItems: "center", gap: 16, width: "100%", minHeight: 56, padding: "10px 16px", boxSizing: "border-box", border: "none", borderRadius: "var(--r-card-medium-radius)", background: i === v ? "var(--secondary-container)" : "transparent", color: i === v ? "var(--on-secondary-container)" : "var(--on-surface)", font: `${i === v ? 600 : 400} 17px/1.3 var(--font-sans)`, textAlign: "left", cursor: "pointer" }}>
          <Radio on={i === v} /><span style={{ flex: 1, minWidth: 0, wordBreak: "break-word" }}>{o}</span>
        </button>
      ))}
    </div>
  );
}
function TxtBody({ it, v, set }) {
  const bad = it.pattern && !new RegExp(it.pattern).test(v);
  return <CmpTextField label={it.t.length > 28 ? "Значение" : it.t} value={v} onChange={(s) => set(it.maxLen ? s.slice(0, it.maxLen) : s)} singleLine isError={!!bad} inputMode={it.kb === "digits" ? "numeric" : it.kb === "phone" ? "tel" : "text"} supportingText={bad ? it.err : `${v.length}/${it.maxLen || 32}`} />;
}
function HM({ v, set, label, max, fmtv }) {
  const m = (d) => () => set((cv) => (cv + d + max) % max);
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
      <StepBtn icon="keyboard_arrow_up" label={label + " больше"} fn={m(1)} size={56} />
      <div style={{ font: "600 48px/1 var(--font-sans)", fontVariantNumeric: "tabular-nums", minWidth: 84, textAlign: "center", padding: "6px 0" }}>{fmtv(v)}</div>
      <StepBtn icon="keyboard_arrow_down" label={label + " меньше"} fn={m(-1)} size={56} />
    </div>
  );
}
function TimeBody({ v, set }) {
  const [h, m] = v.split(":").map(Number);
  const pad = (x) => String(x).padStart(2, "0");
  const sh = (f) => set(pad(f(h)) + ":" + pad(m)), sm = (f) => set(pad(h) + ":" + pad(f(m)));
  return (
    <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: 12, padding: "8px 0" }}>
      <HM v={h} label="Часы" max={24} fmtv={pad} set={(f) => sh(f)} />
      <div style={{ font: "600 48px/1 var(--font-sans)", paddingBottom: 4 }}>:</div>
      <HM v={m} label="Минуты" max={60} fmtv={pad} set={(f) => sm(f)} />
    </div>
  );
}
function SchBody({ v, set }) {
  const [open, setOpen] = useState(null);
  const Line = ({ k, tk, label }) => (
    <div style={{ borderRadius: "var(--r-card-medium-radius)", background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, minHeight: 64, padding: "4px 12px 4px 16px" }}>
        <div style={{ width: 64, height: 48, display: "grid", placeItems: "center" }}><CmpSwitch checked={v[k]} onCheckedChange={(x) => set({ ...v, [k]: x })} thumbIcon /></div>
        <CmpText variant="titleMedium" style={{ flex: 1 }}>{label}</CmpText>
        <button disabled={!v[k]} onClick={() => setOpen(open === tk ? null : tk)} style={{ minWidth: 96, height: 48, borderRadius: "var(--r-card-small-radius)", border: "1px solid var(--border-light)", background: open === tk ? "var(--secondary-container)" : "transparent", color: "var(--on-surface)", font: "600 20px var(--font-sans)", opacity: v[k] ? 1 : 0.38, cursor: v[k] ? "pointer" : "default", ...mono }}>{v[tk]}</button>
      </div>
      {open === tk && v[k] && <div style={{ borderTop: "1px solid var(--border-highlight)" }}><TimeBody v={v[tk]} set={(x) => set({ ...v, [tk]: x })} /></div>}
    </div>
  );
  return <div style={{ display: "flex", flexDirection: "column", gap: 10 }}><Line k="am" tk="amT" label="Утренний запуск" /><Line k="pm" tk="pmT" label="Вечерний запуск" /></div>;
}
function SirBody({ v, set }) {
  const tone = v.type === "Тон";
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <Segmented opts={["Тон", "Мелодия"]} value={tone ? 0 : 1} onChange={(i) => set({ type: i ? "Мелодия" : "Тон", num: 1 })} />
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">Номер {tone ? "тона (1–8)" : "мелодии (1–32)"}</CmpText>
      <NumBody key={v.type} it={{ type: "int", min: 1, max: tone ? 8 : 32, step: 1 }} v={v.num} set={(n) => set({ ...v, num: n })} />
    </div>
  );
}
function CanBody({ v, set }) {
  const [found, setFound] = useState(false), [pick, setPick] = useState(0);
  const cars = CARS[v] || [];
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
      <CmpTextField label="CAN-код (4 цифры)" value={v} onChange={(s) => { set(s.replace(/\D/g, "").slice(0, 4)); setFound(false); }} singleLine inputMode="numeric" isError={v.length > 0 && v.length < 4} supportingText={v.length < 4 ? "Введите 4 цифры" : undefined} />
      <DBtn text="Найти автомобиль" icon="search" kind="outlined" disabled={v.length < 4} onClick={() => setFound(true)} />
      {found && cars.length === 1 && <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 16px", borderRadius: "var(--r-card-medium-radius)", background: "var(--primary-container)", color: "var(--on-primary-container)" }}><CmpIcon icon="directions_car" size={28} /><CmpText variant="titleMedium">{cars[0]}</CmpText></div>}
      {found && cars.length > 1 && <div><CmpText variant="labelLarge" color="var(--on-surface-variant)">Подходит несколько автомобилей</CmpText>{cars.map((c, i) => <button key={i} onClick={() => setPick(i)} style={{ display: "flex", alignItems: "center", gap: 16, width: "100%", minHeight: 56, padding: "0 8px", border: "none", background: "transparent", color: "var(--on-surface)", font: "400 17px var(--font-sans)", cursor: "pointer", textAlign: "left" }}><Radio on={pick === i} />{c}</button>)}</div>}
      {found && cars.length === 0 && <div style={{ display: "flex", alignItems: "flex-start", gap: 12, padding: "12px 16px", borderRadius: "var(--r-card-medium-radius)", background: "var(--tertiary-container)", color: "var(--on-tertiary-container)" }}><CmpIcon icon="warning" size={26} /><CmpText variant="bodyMedium">Для кода {v} автомобиль не найден. Код всё равно можно применить.</CmpText></div>}
    </div>
  );
}
function IOBody({ it, v, set }) {
  const out = it.type !== "in";
  const [q, setQ] = useState(""), [onlySel, setOnlySel] = useState(false);
  const cnt = v.logics.filter((l) => l.on).length;
  const ql = q.trim().toLowerCase();
  const rows = v.logics.map((l, i) => ({ l, i })).filter(({ l }) => (!onlySel || l.on) && (!ql || l.n.toLowerCase().includes(ql)));
  const tog = (i) => set({ ...v, logics: v.logics.map((x, j) => j === i ? { ...x, on: !x.on } : x) });
  return (
    <div>
      <div>
        <div style={{ position: "sticky", top: -8, zIndex: 2, background: "var(--background-surface-highlight)", paddingTop: 8, paddingBottom: 8, display: "flex", flexDirection: "column", gap: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <CmpText variant="labelLarge" color="var(--on-surface-variant)" style={{ flex: 1 }}>Выбрано {cnt} из {v.logics.length}</CmpText>
            <button onClick={() => setOnlySel(!onlySel)} style={{ height: 36, padding: "0 12px", borderRadius: 10, border: "1px solid var(--border-light)", background: onlySel ? "var(--secondary-container)" : "transparent", color: onlySel ? "var(--on-secondary-container)" : "var(--on-surface)", font: "500 14px var(--font-sans)", cursor: "pointer" }}>Выбранные</button>
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 8, height: 48, padding: "0 4px 0 14px", borderRadius: 999, background: "var(--surface-container-highest, var(--surface-variant))" }}>
            <CmpIcon icon="search" size={22} color="var(--on-surface-variant)" />
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Найти функцию" style={{ flex: 1, minWidth: 0, border: "none", outline: "none", background: "transparent", color: "var(--on-surface)", font: "400 16px var(--font-sans)", height: 48 }} />
            {q && <button aria-label="Очистить" onClick={() => setQ("")} style={{ width: 48, height: 48, border: "none", background: "transparent", color: "var(--on-surface-variant)", display: "grid", placeItems: "center", cursor: "pointer" }}><CmpIcon icon="close" size={22} /></button>}
          </div>
        </div>
        {rows.length === 0 && <div style={{ padding: "20px 8px", textAlign: "center" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Ничего не найдено</CmpText></div>}
        {rows.map(({ l, i }) => (
          <button key={i} role="checkbox" aria-checked={l.on} onClick={() => tog(i)} style={{ display: "flex", alignItems: "center", gap: 16, width: "100%", minHeight: 52, padding: "4px 8px", border: "none", background: l.on ? "color-mix(in srgb, var(--secondary-container) 45%, transparent)" : "transparent", borderRadius: "var(--r-card-small-radius)", color: "var(--on-surface)", textAlign: "left", cursor: "pointer" }}>
            <Check on={l.on} />
            <span style={{ display: "flex", flexDirection: "column", flex: 1, minWidth: 0 }}><span style={{ font: "400 17px/1.3 var(--font-sans)", wordBreak: "break-word" }}>{l.n}</span>{l.d && <span style={{ font: "400 14px var(--font-sans)", color: "var(--on-surface-variant)" }}>{l.d}</span>}</span>
          </button>
        ))}
      </div>
    </div>
  );
}

function EditorBody({ it, v, set, wide }) {
  switch (it.type) {
    case "int": case "dec": return <NumBody it={it} v={v} set={set} />;
    case "sel": return <SelBody it={it} v={v} set={set} tall={wide} />;
    case "txt": return <TxtBody it={it} v={v} set={set} />;
    case "time": return <TimeBody v={v} set={set} />;
    case "sch": return <SchBody v={v} set={set} />;
    case "sir": return <SirBody v={v} set={set} />;
    case "can": return <CanBody v={v} set={set} />;
    default: return <IOBody it={it} v={v} set={set} />;
  }
}
function validV(it, v) {
  if (it.type === "txt") return !(it.pattern && !new RegExp(it.pattern).test(v));
  if (it.type === "can") return v.length === 4;
  return true;
}

function Overlay({ wide, onClose, children, w = 560 }) {
  return (
    <div onClick={onClose} className="ds-an" style={{ position: "absolute", inset: 0, zIndex: 60, background: "color-mix(in srgb, var(--scrim) 42%, transparent)", display: "flex", alignItems: wide ? "center" : "flex-end", justifyContent: "center", animation: "dsFade .15s ease both" }}>
      <div onClick={(e) => e.stopPropagation()} className="ds-an" style={{ width: wide ? w : "100%", maxWidth: "calc(100% - 32px)", ...(wide ? {} : { maxWidth: "100%" }), maxHeight: wide ? "88%" : "92%", display: "flex", flexDirection: "column", background: "var(--background-surface-highlight)", color: "var(--on-surface)", borderRadius: wide ? 28 : "28px 28px 0 0", boxShadow: "var(--elevation-3)", animation: wide ? "dsPop .18s ease both" : "dsUp .22s ease both" }}>
        {!wide && <div style={{ width: 36, height: 4, borderRadius: 999, background: "var(--outline-variant)", margin: "10px auto 0", flex: "0 0 auto" }} />}
        {children}
      </div>
    </div>
  );
}

function EditorPanel({ it, base, cur, wide, onApply, onClose }) {
  const [v, setV] = useState(cur);
  const [more, setMore] = useState(false);
  const diff = !same(v, base);
  const ok = validV(it, v);
  const isIO = it.type === "in" || it.type === "out";
  const ioIn = it.type === "in", labs = ioIn ? ["НО", "НЗ"] : ["+", "−"], pv = isIO ? (ioIn ? v.type : v.pol) : 0, edP = ioIn || it.polEditable;
  return (
    <Overlay wide={wide} onClose={onClose}>
      <div style={{ padding: isIO ? "12px 20px 4px" : "16px 24px 8px", flex: "0 0 auto" }}>
        <div style={{ display: "flex", alignItems: "flex-start", gap: 8 }}>
          <div style={{ flex: 1, minWidth: 0, ...(isIO ? { display: "flex", alignItems: "baseline", gap: 10, minHeight: 48 } : {}) }}>
            <span style={{ ...mono, fontSize: "var(--t-additional0-size)", color: "var(--on-surface-variant)" }}>{it.id}</span>
            <CmpText variant="titleLarge" as="div" style={{ wordBreak: "break-word" }}>{isIO ? it.t + " (" + labs[pv] + ")" : it.t}</CmpText>
          </div>
          {isIO && edP && <Segmented opts={labs} value={pv} onChange={(i) => setV(ioIn ? { ...v, type: i } : { ...v, pol: i })} h={44} style={{ width: 132, flex: "0 0 auto" }} />}
          {it.desc && !isIO && <button aria-label={more ? "Свернуть описание" : "Показать описание"} onClick={() => setMore(!more)} style={{ width: 48, height: 48, border: "none", background: "transparent", color: "var(--on-surface-variant)", borderRadius: 999, display: "grid", placeItems: "center", cursor: "pointer", margin: "-4px -12px 0 0" }}><CmpIcon icon={more ? "expand_less" : "expand_more"} size={28} /></button>}
        </div>
        {it.desc && !isIO && <CmpText variant="bodyMedium" color="var(--on-surface-variant)" as="div" maxLines={more ? undefined : 1}>{it.desc}</CmpText>}
        {isIO && <div style={{ display: "flex", flexWrap: "wrap", alignItems: "center", gap: 8, paddingBottom: 4 }}>{it.wires.map((w, i) => <WireTag key={i} {...w} />)}<span style={{ display: "inline-flex", alignItems: "center", height: 28, padding: "0 10px", borderRadius: 999, background: diff ? "var(--tertiary-container)" : "var(--surface-variant)", color: diff ? "var(--on-tertiary-container)" : "var(--on-surface-variant)", font: "500 14px var(--font-sans)" }}>Было: {fmt(it, base)}</span></div>}
        {!isIO && <div style={{ marginTop: 8, minHeight: 28 }}><span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 28, padding: "0 10px", borderRadius: 999, background: diff ? "var(--tertiary-container)" : "var(--surface-variant)", color: diff ? "var(--on-tertiary-container)" : "var(--on-surface-variant)", font: "500 14px var(--font-sans)", maxWidth: "100%", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", boxSizing: "border-box" }}>Было: {fmt(it, base)}</span></div>}
      </div>
      <div style={{ padding: isIO ? "4px 20px 8px" : "8px 24px 12px", overflowY: "auto", flex: "1 1 auto", minHeight: 0 }}>
        <EditorBody it={it} v={v} set={setV} wide={wide} />
      </div>
      <div style={{ display: "flex", gap: 12, padding: "12px 24px 24px", flex: "0 0 auto" }}>
        <DBtn text="Сбросить" kind="outlined" disabled={!diff} onClick={() => setV(base)} style={{ flex: "1 1 0" }} />
        <DBtn text="Готово" kind="filled" disabled={!ok} onClick={() => onApply(v)} style={{ flex: "1 1 0" }} />
      </div>
    </Overlay>
  );
}

// ── Диалоги ──────────────────────────────────────────────────────────────────
function Dlg({ icon, tone = "primary", title, children, actions, stack, onClose, w = 400 }) {
  return (
    <div onClick={onClose} className="ds-an" style={{ position: "absolute", inset: 0, zIndex: 80, background: "color-mix(in srgb, var(--scrim) 42%, transparent)", display: "grid", placeItems: "center", padding: 24, boxSizing: "border-box", animation: "dsFade .15s ease both" }}>
      <div onClick={(e) => e.stopPropagation()} role="dialog" aria-label={title} className="ds-an" style={{ width: "100%", maxWidth: w, background: "var(--background-surface-highlight)", color: "var(--on-surface)", borderRadius: "var(--r-card-medium-radius)", padding: 24, boxSizing: "border-box", boxShadow: "var(--elevation-3)", display: "flex", flexDirection: "column", gap: 16, animation: "dsPop .18s ease both" }}>
        {icon && <div style={{ color: tone === "error" ? "var(--error)" : "var(--primary)", display: "grid", placeItems: "center" }}><CmpIcon icon={icon} size={36} /></div>}
        <CmpText variant="headlineSmall" align={icon ? "center" : undefined} as="div">{title}</CmpText>
        <div style={{ color: "var(--on-surface-variant)" }}>{children}</div>
        {actions && <div style={{ display: "flex", flexDirection: stack ? "column" : "row", justifyContent: "flex-end", gap: 8, flexWrap: "wrap" }}>{actions}</div>}
      </div>
    </div>
  );
}
function Block({ icon, tone, title, text, progress, hint }) {
  return (
    <Dlg icon={icon} tone={tone} title={title} w={360}>
      <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
        {progress === undefined ? <CmpLinearProgressIndicator /> : <CmpLinearProgressIndicator progress={progress} />}
        <CmpText variant="bodyMedium">{text}</CmpText>
        {hint && <CmpText variant="bodySmall">{hint}</CmpText>}
      </div>
    </Dlg>
  );
}
function Hero({ icon, tone }) {
  const bg = tone === "error" ? "var(--error-container)" : "var(--surface-variant)";
  const fg = tone === "error" ? "var(--on-error-container)" : "var(--on-surface-variant)";
  return <div style={{ width: 88, height: 88, borderRadius: "50%", background: bg, display: "grid", placeItems: "center" }}><CmpIcon icon={icon} size={42} color={fg} /></div>;
}
function Center({ children }) {
  return <div style={{ flex: 1, minHeight: 240, display: "flex", alignItems: "center", justifyContent: "center", padding: 24, boxSizing: "border-box" }}><div style={{ maxWidth: 420, width: "100%", display: "flex", flexDirection: "column", alignItems: "center", gap: 16, textAlign: "center" }}>{children}</div></div>;
}

function useWidth() {
  const ref = useRef(null); const [w, setW] = useState(0);
  useEffect(() => { const el = ref.current; if (!el) return; const m = () => setW(el.offsetWidth); const ro = new ResizeObserver(m); ro.observe(el); m(); return () => ro.disconnect(); }, []);
  return [ref, w >= 840 ? "expanded" : w >= 600 ? "medium" : "compact"];
}

function CanBadge() {
  return <span style={{ display: "inline-flex", alignItems: "center", height: 24, padding: "0 10px", borderRadius: 999, background: "var(--secondary-container)", color: "var(--on-secondary-container)", font: "600 13px var(--font-sans)", whiteSpace: "nowrap" }}>Единая CAN 2154</span>;
}

// ── Главный экран ────────────────────────────────────────────────────────────
const SRC_META = { mine: { i: "inventory_2", t: "Мои конфигурации" }, file: { i: "description", t: "Файл" }, cloud: { i: "cloud", t: "Облако" }, factory: { i: "factory", t: "Заводские" } };
function SrcTag({ kind }) {
  const m = SRC_META[kind];
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 28, padding: "0 10px 0 8px", borderRadius: 8, background: "var(--secondary-container)", color: "var(--on-secondary-container)", font: "500 14px var(--font-sans)", whiteSpace: "nowrap", flex: "0 0 auto" }}><CmpIcon icon={m.i} size={18} />{m.t}</span>;
}
function RoPill() {
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 6, height: 28, padding: "0 10px 0 8px", borderRadius: 999, border: "1px solid var(--border-light)", color: "var(--on-surface-variant)", font: "500 14px var(--font-sans)", whiteSpace: "nowrap", flex: "0 0 auto", boxSizing: "border-box" }}><CmpIcon icon="visibility" size={18} />Только просмотр</span>;
}

// source: { kind: block|mine|file|cloud|factory, title, model } — шапка, меню «⋯», панель внизу (ТЗ конфигуратора, K5/K7).
// writeAvail: ok | hide | disable — пункт «Записать в блок» в конфигурациях. onAction(key) — пункты меню и «Сохранить».
function DeviceSettingsScreen({ initialState = "ready", demo, startId = "", seedEdits = true, canFactory = true, failWrite = false, onBack = () => {},
  source, baseValues, initialEdits, writeAvail = "hide", writeReason, onAction, apiRef, toast, cfgLink = "configurator" }) {
  const src = source || { kind: "block" };
  const isBlock = src.kind === "block";
  const ro = src.kind === "cloud" || src.kind === "factory";
  canFactory = canFactory && isBlock;
  const [rootRef, layout] = useWidth();
  const wide = layout !== "compact";
  const dm = demo || "";
  const [st, setSt] = useState(initialState);
  const [cur, setCur] = useState(dm.startsWith("edit:") && !startId ? IDX[dm.slice(5)].parent : (startId || (dm === "empty" ? "7" : "")));
  const [exp, setExp] = useState(() => { const s = startId || (dm.startsWith("edit:") ? IDX[dm.slice(5)].parent : "3"); return chain(s).map((n) => n.id).concat(["3", "3.2"]); });
  const [edits, setEdits] = useState(initialEdits || (seedEdits && !dm.startsWith("noedits") ? SEED : {}));
  const [base, setBase] = useState(() => baseValues ? { ...BASE0, ...baseValues } : BASE0);
  const [ov, setOv] = useState(() => {
    if (dm.startsWith("edit:")) return { k: "edit", id: dm.slice(5) };
    if (dm === "info") return { k: "info", id: "3.2.4" };
    if (dm === "exit" && !isBlock) return { k: "exit-cfg" };
    if (["exit", "reset", "can", "lost", "menu", "write-error"].includes(dm)) return { k: dm };
    return null;
  });
  const [q, setQ] = useState(dm === "search" ? "дат" : dm === "search-busy" ? "задер" : dm === "search-empty" ? "вертолёт" : null);
  const [busy, setBusy] = useState(dm === "search-busy");
  const [run, setRun] = useState(dm === "write-progress" ? { kind: "write", p: 0.45, hold: true } : dm === "reset-progress" ? { kind: "reset", p: 0.6, hold: true } : null);
  const [snack, setSnack] = useState(toast || (dm === "write-ok" ? "Записано изменений: 3" : null));
  const [bad, setBad] = useState(null);
  const n = LEAVES.reduce((s, l) => s + (edits[l.id] !== undefined ? 1 : 0), 0);
  const val = (it) => edits[it.id] !== undefined ? edits[it.id] : base[it.id];
  const frozenN = useRef(n); if (!run) frozenN.current = n;
  const editsRef = useRef(edits); editsRef.current = edits;
  const baseRef = useRef(base); baseRef.current = base;
  if (apiRef) apiRef.current = {
    n,
    load: (vals) => { const b = baseRef.current, d = {}; Object.keys(vals).forEach((k) => { if (IDX[k] && !same(vals[k], b[k])) d[k] = vals[k]; }); setEdits(d); return Object.keys(d).length; },
    commit: () => { setBase((b) => ({ ...b, ...editsRef.current })); setEdits({}); },
    values: () => ({ ...baseRef.current, ...editsRef.current }),
    snack: setSnack,
  };
  const act = (k, o) => {
    setOv(null);
    if (onAction) return onAction(k, o || {});
    if (window.PS_LINKS && window.PS_LINKS[cfgLink]) location.href = window.PS_ROOT + window.PS_LINKS[cfgLink] + "?entry=block&go=" + k;
  };

  useEffect(() => { if (st !== "loading") return; const t = setTimeout(() => setSt("ready"), 1800); return () => clearTimeout(t); }, [st]);
  useEffect(() => { if (!snack) return; const t = setTimeout(() => setSnack(null), 3500); return () => clearTimeout(t); }, [snack]);
  useEffect(() => { if (!busy) return; const t = setTimeout(() => setBusy(false), dm === "search-busy" ? 3600000 : 450); return () => clearTimeout(t); }, [busy]);
  useEffect(() => {
    if (!run || run.hold) return;
    const t = setInterval(() => setRun((r) => r && { ...r, p: Math.min(1, r.p + 0.05) }), 110);
    return () => clearInterval(t);
  }, [!!run, run && run.hold]);
  useEffect(() => {
    if (!run || run.p < 1) return;
    const t = setTimeout(() => {
      if (run.kind === "reset") { setEdits({}); setBase(BASE0); setSnack("Устройство сброшено к заводским настройкам"); }
      else if (failWrite) { setBad("Нет ответа от устройства (таймаут записи). Проверьте связь и питание блока."); setOv({ k: "write-error" }); }
      else { setBase((b) => ({ ...b, ...edits })); setEdits({}); setSnack("Записано изменений: " + frozenN.current); }
      setRun(null);
    }, 250);
    return () => clearTimeout(t);
  }, [run && run.p >= 1]);

  const secId = cur || (wide ? "1" : "");
  const sec = secId ? IDX[secId].n : { kids: TREE, t: "Все", id: "" };
  const doEdit = (it) => { if (it.can && canFactory) setOv({ k: "can" }); else setOv({ k: "edit", id: it.id }); };
  const apply = (it, v) => { setEdits((e) => { const c = { ...e }; if (same(v, base[it.id])) delete c[it.id]; else c[it.id] = v; return c; }); setOv(null); };
  const inline = (it, v) => { if (it.can && canFactory) return setOv({ k: "can" }); apply2(it, v); };
  const apply2 = (it, v) => setEdits((e) => { const c = { ...e }; if (same(v, base[it.id])) delete c[it.id]; else c[it.id] = v; return c; });
  const goBack = () => { if (q !== null) { setQ(null); return; } if (!wide && cur) return setCur(IDX[cur].parent); if (n > 0) setOv({ k: isBlock ? "exit" : "exit-cfg" }); else onBack(); };
  const select = (id) => { setCur(id); setExp((e) => e.includes(id) ? e : [...e, id]); };
  const toggle = (id) => setExp((e) => e.includes(id) ? e.filter((x) => x !== id) : [...e, id]);
  const startWrite = () => setRun({ kind: "write", p: 0 });
  const rowFor = (it, i, arr, path) => (
    <SettingRow key={it.id} it={it} val={it.type === "sec" ? undefined : val(it)} changed={it.type !== "sec" && edits[it.id] !== undefined} wide={wide} path={path} count={it.type === "sec" ? countIn(it, edits) : 0} last={i === arr.length - 1}
      onOpen={(x) => x.type === "sec" ? select(x.id) : doEdit(x)} onInfo={(x) => setOv({ k: "info", id: x.id })} onChangeVal={(v) => inline(it, v)} />
  );

  // поиск
  const ql = (q || "").trim().toLowerCase();
  const results = ql ? Object.values(IDX).map((x) => x.n).filter((x) => x.type !== "div" && x.t.toLowerCase().includes(ql) || x.id.startsWith(ql) || (x.name || "").toLowerCase().includes(ql)) : [];
  const pathOf = (it) => chain(IDX[it.id].parent).map((x) => x.id + " " + x.t).join(" › ");

  const list = (items) => items.length === 0 ? (
    <Center><Hero icon="folder_open" /><CmpText variant="titleLarge">В разделе нет настроек</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Устройство не передало пунктов для этого раздела.</CmpText></Center>
  ) : <div style={wide ? { background: "var(--surface)", border: "1px solid var(--border-highlight)", borderRadius: "var(--r-card-medium-radius)", overflow: "hidden" } : { background: "var(--surface)" }}>{items.map((it, i) => rowFor(it, i, items))}</div>;

  const searchView = busy ? (
    <div><CmpLinearProgressIndicator /><div style={{ padding: 24, textAlign: "center" }}><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Ищем «{q}»…</CmpText></div></div>
  ) : results.length === 0 ? (
    <Center><Hero icon="search_off" /><CmpText variant="titleLarge">Ничего не найдено по «{q}»</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Проверьте написание или введите номер настройки, например 3.2.4.</CmpText></Center>
  ) : <div style={wide ? { background: "var(--surface)", border: "1px solid var(--border-highlight)", borderRadius: "var(--r-card-medium-radius)", overflow: "hidden" } : { background: "var(--surface)" }}>{results.map((it, i) => rowFor(it, i, results, pathOf(it)))}</div>;

  const searchField = (
    <div style={{ display: "flex", alignItems: "center", gap: 8, height: 48, padding: "0 4px 0 16px", borderRadius: 999, background: "var(--background-surface-highlight)", flex: wide ? "0 1 360px" : 1, minWidth: 0 }}>
      <CmpIcon icon="search" size={24} color="var(--on-surface-variant)" />
      <input autoFocus={q !== null && !wide} value={q || ""} placeholder="Название или номер" onFocus={() => q === null && setQ("")} onChange={(e) => { setQ(e.target.value); setBusy(true); }} style={{ flex: 1, minWidth: 0, border: "none", outline: "none", background: "transparent", color: "var(--on-surface)", font: "400 17px var(--font-sans)", height: 48 }} />
      {q ? <button aria-label="Очистить" onClick={() => { setQ(wide ? "" : null); }} style={{ width: 48, height: 48, border: "none", background: "transparent", color: "var(--on-surface-variant)", display: "grid", placeItems: "center", cursor: "pointer" }}><CmpIcon icon="close" size={24} /></button> : <span style={{ width: 12 }} />}
    </div>
  );

  const title = isBlock ? "Настройки" : src.title;
  const subInner = isBlock
    ? <React.Fragment><span style={{ flex: "0 0 auto", whiteSpace: "nowrap", font: "500 14px var(--font-sans)", color: "var(--on-surface-variant)" }}>DX-9X · 1234567</span><CanBadge /></React.Fragment>
    : <React.Fragment><SrcTag kind={src.kind} /><span style={{ flex: "0 0 auto", whiteSpace: "nowrap", font: "500 14px var(--font-sans)", color: "var(--on-surface-variant)" }}>{src.model}</span>{ro && <RoPill />}</React.Fragment>;
  const sub = <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>{subInner}</div>;
  const bar = wide ? (
    <CmpTopAppBar title={title} subtitle={sub} navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={goBack} />}
      actions={<React.Fragment>{searchField}<CmpIconButton icon="MoreVert" contentDescription="Ещё" onClick={() => setOv({ k: "menu" })} /></React.Fragment>} />
  ) : q !== null ? (
    <div style={{ height: 64, display: "flex", alignItems: "center", gap: 4, padding: "0 12px 0 8px", flex: "0 0 auto" }}><CmpIconButton icon="ArrowBack" contentDescription="Закрыть поиск" onClick={() => setQ(null)} />{searchField}</div>
  ) : (
    <div style={{ flex: "0 0 auto", background: "var(--surface)" }}>
      <div style={{ height: 64, display: "flex", alignItems: "center", gap: 4, padding: "0 4px 0 4px" }}>
        <CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={goBack} />
        <CmpText variant="titleLarge" style={{ flex: 1, minWidth: 0, paddingLeft: 4 }} maxLines={1}>{title}</CmpText>
        <CmpIconButton icon="Search" contentDescription="Поиск" onClick={() => setQ("")} />
        <CmpIconButton icon="MoreVert" contentDescription="Ещё" onClick={() => setOv({ k: "menu" })} />
      </div>
      <div style={{ display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap", padding: "0 16px 10px 16px" }}>{subInner}</div>
    </div>
  );

  let body;
  if (st === "loading") body = <Center><CmpCircularProgressIndicator /><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Читаем настройки из устройства…</CmpText><CmpText variant="bodyMedium" color="var(--on-surface-variant)">По Bluetooth это может занять до минуты</CmpText></Center>;
  else if (st === "error") body = <Center><Hero icon="error_outline" tone="error" /><CmpText variant="titleLarge">Не удалось прочитать настройки</CmpText><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Устройство не ответило на запрос. Проверьте связь и питание блока.</CmpText><DBtn text="Повторить" icon="refresh" kind="filled" onClick={() => setSt("loading")} style={{ width: "100%" }} /></Center>;
  else if (q !== null && !wide) body = <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{ql || busy ? searchView : <Center><CmpText variant="bodyLarge" color="var(--on-surface-variant)">Введите название или номер настройки</CmpText></Center>}</div>;
  else if (!wide) body = (
    <React.Fragment>
      <Breadcrumbs cur={cur} edits={edits} onGo={setCur} />
      <div style={{ flex: 1, minHeight: 0, overflow: "auto" }}>{list(sec.kids)}</div>
    </React.Fragment>
  );
  else body = (
    <div style={{ flex: 1, minHeight: 0, display: "flex" }}>
      <Tree sel={secId} exp={exp} toggle={toggle} select={(id) => { setQ(null); select(id); }} edits={edits} width={layout === "expanded" ? 280 : 232} />
      <div style={{ flex: 1, minWidth: 0, overflow: "auto", background: "var(--background)" }}>
        <div style={{ maxWidth: 720, padding: layout === "expanded" ? "16px 24px 24px" : "12px 16px 20px", boxSizing: "border-box" }}>
          {q !== null && (ql || busy) ? (
            <React.Fragment><CmpText variant="titleLarge" as="div" style={{ padding: "4px 4px 12px" }}>Поиск: «{q}»</CmpText>{searchView}</React.Fragment>
          ) : (
            <React.Fragment>
              <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "4px 4px 12px" }}>
                <CmpText variant="titleLarge" style={{ flex: 1, minWidth: 0 }} maxLines={1}><span style={{ ...mono, fontSize: "var(--t-paragraph2-size)", color: "var(--on-surface-variant)", marginRight: 10 }}>{sec.id}</span>{sec.t}</CmpText>
                {countIn(sec, edits) > 0 && <CountPill n={countIn(sec, edits)} />}
              </div>
              {list(sec.kids)}
            </React.Fragment>
          )}
        </div>
      </div>
    </div>
  );

  const showBar = n > 0 && st === "ready";
  const eit = ov && ov.k === "edit" ? IDX[ov.id].n : null;
  const iit = ov && ov.k === "info" ? IDX[ov.id].n : null;
  const wItem = writeAvail === "hide" ? [] : [{ i: "upload", t: "Записать в блок", on: () => act("write"), off: writeAvail === "disable", sub: writeAvail === "disable" ? (writeReason || "Подключите блок той же модели") : null }];
  const toMine = { i: "inventory_2", t: "Сохранить в мои", on: () => act("saveMine") };
  const menuItems = {
    block: [{ i: "download", t: "Загрузить из…", on: () => act("load") }, { i: "save_alt", t: "Сохранить в…", on: () => act("saveTo") }, { div: true }, { i: "restart_alt", t: "Сбросить к заводским настройкам", on: () => setOv({ k: "reset" }) }],
    mine: [{ i: "description", t: "Сохранить в файл", on: () => act("saveFile") }, { i: "content_copy", t: "Создать копию", on: () => act("copy") }, ...wItem, { i: "edit", t: "Переименовать", on: () => act("rename") }],
    file: [toMine, { i: "save_as", t: "Сохранить как файл…", on: () => act("saveFile") }, ...wItem],
    cloud: [toMine, { i: "description", t: "Сохранить в файл", on: () => act("saveFile") }, ...wItem],
  }[src.kind === "factory" ? "cloud" : src.kind];
  const barBtn = isBlock ? { t: "Записать", i: "upload", f: startWrite } : ro ? { t: "Сохранить как…", i: "save_as", f: () => act("saveAs") } : { t: "Сохранить", i: "save", f: () => act("save") };

  return (
    <div ref={rootRef} style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column", background: "var(--background)", color: "var(--on-background)", overflow: "hidden" }}>
      {bar}
      <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>{body}</div>
      {showBar && (
        <div style={{ flex: "0 0 auto", display: "flex", alignItems: "center", gap: 12, padding: "10px 16px 12px", background: "var(--background-surface-ultra-highlight)", borderTop: "1px solid var(--border-highlight)" }}>
          <div style={{ flex: 1, display: "flex", alignItems: "center", gap: 10 }}><Dot /><CmpText variant="titleMedium">Изменено: {n}</CmpText></div>
          <DBtn text={barBtn.t} icon={barBtn.i} kind="filled" onClick={barBtn.f} style={{ minWidth: 160 }} />
        </div>
      )}
      {snack && <div className="ds-an" style={{ position: "absolute", left: 16, right: 16, bottom: showBar ? 92 : 16, display: "flex", justifyContent: "center", zIndex: 70, animation: "dsUp .2s ease both" }}><CmpSnackbar message={snack} actionLabel="OK" onActionClick={() => setSnack(null)} /></div>}

      {eit && <EditorPanel key={eit.id} it={eit} base={base[eit.id]} cur={val(eit)} wide={wide} onApply={(v) => apply(eit, v)} onClose={() => setOv(null)} />}
      {iit && <Dlg title={iit.id + " " + iit.t} onClose={() => setOv(null)} actions={<DBtn text="Понятно" onClick={() => setOv(null)} />}><CmpText variant="bodyLarge">{iit.desc}</CmpText></Dlg>}
      {ov && ov.k === "menu" && (
        <div onClick={() => setOv(null)} style={{ position: "absolute", inset: 0, zIndex: 60 }}>
          <div onClick={(e) => e.stopPropagation()} className="ds-an" style={{ position: "absolute", right: 8, top: 60, width: 320, maxWidth: "calc(100% - 16px)", padding: "8px 0", background: "var(--background-surface-ultra-highlight)", borderRadius: "var(--r-card-medium-radius)", boxShadow: "var(--elevation-3)", animation: "dsPop .15s ease both", transformOrigin: "top right" }}>
            {menuItems.map((m, i) => m.div ? <div key={i} role="separator" style={{ borderTop: "1px solid var(--border-highlight)", margin: "6px 0" }} /> : (
              <button key={i} disabled={m.off} onClick={() => m.on && m.on()} style={{ display: "flex", alignItems: "center", gap: 16, width: "100%", minHeight: 56, padding: "6px 16px", border: "none", background: "transparent", color: "var(--on-surface)", font: "400 16px var(--font-sans)", textAlign: "left", cursor: m.off ? "default" : "pointer" }}>
                <CmpIcon icon={m.i} size={24} color="var(--on-surface-variant)" style={{ opacity: m.off ? 0.38 : 1 }} />
                <span style={{ display: "flex", flexDirection: "column", gap: 2, minWidth: 0 }}><span style={{ opacity: m.off ? 0.38 : 1 }}>{m.t}</span>{m.sub && <span style={{ font: "400 14px/1.3 var(--font-sans)", color: "var(--on-surface-variant)" }}>{m.sub}</span>}</span>
              </button>
            ))}
          </div>
        </div>
      )}
      {ov && ov.k === "exit" && (
        <Dlg icon="warning" title={`Есть незаписанные изменения (${n})`} stack onClose={() => setOv(null)}
          actions={<React.Fragment><DBtn kind="filled" text="Записать и выйти" onClick={() => { setOv(null); startWrite(); }} /><DBtn kind="outlined" text="Выйти без записи" onClick={() => { setOv(null); onBack(); }} /><DBtn text="Отмена" onClick={() => setOv(null)} /></React.Fragment>}>
          <CmpText variant="bodyLarge">Изменения ещё не записаны в устройство. Если выйти без записи, они будут потеряны.</CmpText>
        </Dlg>
      )}
      {ov && ov.k === "exit-cfg" && (
        <Dlg icon="warning" title="Сохранить изменения?" stack onClose={() => setOv(null)}
          actions={<React.Fragment><DBtn kind="filled" text={ro ? "Сохранить как…" : "Сохранить"} onClick={() => act(ro ? "saveAs" : "save", { exit: true })} /><DBtn kind="outlined" text="Не сохранять" onClick={() => { setOv(null); onBack(); }} /><DBtn text="Отмена" onClick={() => setOv(null)} /></React.Fragment>}>
          <CmpText variant="bodyLarge">Изменено настроек: {n}. {ro ? "Исходную конфигурацию изменить нельзя — сохраните результат как новую." : "Если не сохранить, изменения пропадут."}</CmpText>
        </Dlg>
      )}
      {ov && ov.k === "reset" && (
        <Dlg icon="restart_alt" tone="error" title="Сбросить к заводским настройкам?" onClose={() => setOv(null)}
          actions={<div style={{ display: "flex", flexDirection: "column", gap: 8, width: "100%" }}>{React.createElement(window.PandoraSpecDesignSystem_b8d6e1.CmpHoldToConfirmButton, { text: "Удерживайте для сброса", doneText: "Сбрасываем…", onConfirm: () => { setTimeout(() => { setOv(null); setRun({ kind: "reset", p: 0 }); }, 300); } })}<DBtn text="Отмена" onClick={() => setOv(null)} /></div>}>
          <CmpText variant="bodyLarge">Все настройки устройства будут заменены заводскими, незаписанные изменения ({n}) пропадут. Отменить это действие нельзя.</CmpText>
        </Dlg>
      )}
      {ov && ov.k === "can" && (
        <Dlg icon="warning" tone="error" title="Нужен режим загрузки базы" onClose={() => setOv(null)}
          actions={<React.Fragment><DBtn text="Отмена" onClick={() => setOv(null)} /><DBtn kind="filled" text="Перевести" onClick={() => setOv(null)} /></React.Fragment>}>
          <CmpText variant="bodyLarge">Для изменения CAN-настроек нужно перевести устройство в режим загрузки базы. Единая CAN-прошивка 2154.</CmpText>
        </Dlg>
      )}
      {ov && ov.k === "write-error" && (
        <Dlg icon="error_outline" tone="error" title="Не удалось записать" onClose={() => setOv(null)}
          actions={<React.Fragment><DBtn text="Закрыть" onClick={() => setOv(null)} /><DBtn kind="filled" icon="refresh" text="Повторить" onClick={() => { setOv(null); startWrite(); }} /></React.Fragment>}>
          <CmpText variant="bodyLarge">{bad || "Нет ответа от устройства (таймаут записи). Проверьте связь и питание блока."} Изменения ({n}) сохранены, их можно записать ещё раз.</CmpText>
        </Dlg>
      )}
      {run && run.kind === "write" && <Block title={`Записываем ${frozenN.current} ${frozenN.current === 1 ? "изменение" : frozenN.current < 5 ? "изменения" : "изменений"}…`} progress={run.p} text="Не отключайте питание и не закрывайте приложение." hint={`${Math.round(run.p * 100)}%`} />}
      {run && run.kind === "reset" && <Block icon="restart_alt" title="Сбрасываем настройки…" progress={run.p} text="Устройство возвращается к заводским настройкам. Не отключайте питание." hint={`${Math.round(run.p * 100)}%`} />}
      {ov && ov.k === "lost" && (
        <div style={{ position: "absolute", inset: 0, zIndex: 90, background: "var(--background)", display: "flex", flexDirection: "column" }}>
          <CmpTopAppBar title="" navigationIcon={<CmpIconButton icon="ArrowBack" contentDescription="Назад" onClick={() => setOv(null)} />} />
          <Center>
            <Hero icon="bluetooth_disabled" tone="neutral" />
            <CmpText variant="headlineSmall">Отсутствует соединение с устройством</CmpText>
            <CmpText variant="bodyLarge" color="var(--on-surface-variant)">Блок выключен или находится далеко. Поднесите телефон ближе и проверьте питание блока.</CmpText>
            {n > 0 && <div style={{ display: "flex", gap: 12, textAlign: "left", padding: "12px 16px", borderRadius: "var(--r-card-medium-radius)", background: "var(--tertiary-container)", color: "var(--on-tertiary-container)" }}><CmpIcon icon="info" size={24} /><CmpText variant="bodyMedium">Незаписанные изменения ({n}) сохранены в приложении. После переподключения их можно будет записать в устройство.</CmpText></div>}
            <div style={{ display: "flex", alignItems: "center", gap: 10 }}><CmpCircularProgressIndicator size={18} strokeWidth={2.5} /><CmpText variant="labelLarge" color="var(--on-surface-variant)">Ждём возвращения блока…</CmpText></div>
            <DBtn text="Отключиться" icon="link_off" kind="outlined" style={{ width: "100%" }} onClick={() => setOv(null)} />
          </Center>
        </div>
      )}
    </div>
  );
}

Object.assign(window, { DeviceSettingsScreen, DSET: { TREE, IDX, LEAVES, BASE0, chain, same, Overlay, Center, Hero, SrcTag, RoPill, SRC_META, fmt, Segmented, SettingRow, WireTag, Pol, CountPill, Breadcrumbs, Tree, NumBody, TimeBody, SchBody, SirBody, CanBody, IOBody, SelBody, TxtBody, EditorPanel, Dlg, DBtn, Dot, Check, Radio, CH_BG, EditorBody } });
})();
