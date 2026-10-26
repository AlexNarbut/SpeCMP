// Vehicle select — демо-данные каталога (марки → модели → комплектации → карточка).
// Структура повторяет то, что реально отдаёт сервер (ТЗ, раздел 7). Значения — демонстрационные.
window.VS_DATA = (function () {
  var GROUPS = [
    { id: "basic", t: "Основные параметры" },
    { id: "ext", t: "Расширенные параметры" },
    { id: "ev", t: "Параметры электромобилей и гибридов" },
    { id: "trailer", t: "Параметры модуля прицепа" },
  ];
  // CarBusGroup: [код сервера, подпись в чипе, семейство]. Подписи не переводятся, кроме «Неизвестно».
  var BUSES = [
    [1, "CAN1", "can"], [2, "CAN2", "can"], [3, "CAN3", "can"], [5, "CAN4", "can"],
    [6, "CAN1-FD", "fd"], [7, "CAN2-FD", "fd"], [8, "CAN3-FD", "fd"], [9, "CAN4-FD", "fd"],
    [100, "CAN-FD1 (CAN-FD3)", "fd"], [101, "CAN-FD2 (CAN-FD4)", "fd"],
    [4, "LIN", "lin"], [10, "LIN (IN-OUT)", "lin"], [11, "BEAN", "bean"],
    [12, "IMO-KEY1", "imo"], [13, "IMO-KEY2", "imo"], [102, "IMO-KEY1 + IMO-KEY2", "imo"],
  ];
  var UNKNOWN = "Неизвестно";
  var BUS_ORDER = BUSES.map(function (b) { return b[1]; }).concat([UNKNOWN]);
  var BUS_FAMILY = {}; BUSES.forEach(function (b) { BUS_FAMILY[b[1]] = b[2]; }); BUS_FAMILY[UNKNOWN] = "unknown";
  var BUS_BY_CODE = {}; BUSES.forEach(function (b) { BUS_BY_CODE[b[0]] = b[1]; });
  // Любой новый код сервера → «Неизвестно», функция всё равно показывается.
  var busOf = function (x) { return typeof x === "number" ? (BUS_BY_CODE[x] || UNKNOWN) : x; };
  // CarFeatureGroup: код сервера → группа; неизвестный код → «Основные».
  var GROUP_BY_CODE = { 1: "basic", 2: "ext", 3: "ev", 4: "trailer" };
  var BLOCK = { name: "Pandora DX-4GS", can: "2154" };

  var slug = function (s) { return s.toLowerCase().replace(/[^a-zа-я0-9]+/gi, "-").replace(/^-|-$/g, ""); };
  var hash = function (s) { var h = 7; for (var i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0; return h; };

  // ── Модели. [название, годы, комплектации[], группы функций]
  var STD = ["basic", "ext"];
  var M = {
    Toyota: [
      ["Camry", "2017–2023", ["2.5 AT", "3.5 AT", "XV70 Hybrid"], ["basic", "ext", "ev"]],
      ["Camry", "2023–", ["XV80 2.5 Hybrid"], ["basic", "ext", "ev"]],
      ["Corolla", "2018–2022", ["1.6 CVT"], STD],
      ["Highlander", "2020–", ["3.5 AT AWD"], STD],
      ["Hilux", "2015–", ["2.4 D MT", "2.8 D AT"], ["basic", "ext", "trailer"]],
      ["Land Cruiser 300", "2021–", ["3.3 D AT", "3.5 AT"], ["basic", "ext", "trailer"]],
      ["Land Cruiser Prado 150", "2009–2023", ["2.8 D AT", "4.0 AT"], ["basic", "ext", "trailer"]],
      ["RAV4", "2019–", ["2.0 CVT", "2.5 AT", "2.5 Hybrid AWD-i с электромеханическим приводом задней оси"], ["basic", "ext", "ev"]],
      ["bZ4X", "2022–", [], ["ev"]],
    ],
    Kia: [
      ["EV6", "2021–", ["77 kWh AWD"], ["basic", "ev"]],
      ["K5", "2020–", ["2.0 AT", "2.5 AT"], STD],
      ["Rio", "2017–2023", ["1.4 MT", "1.6 AT"], STD],
      ["Seltos", "2019–", ["1.6 AT", "2.0 CVT"], STD],
      ["Sorento", "2020–", ["2.5 AT", "2.2 D AT"], STD],
      ["Sportage", "2021–", ["2.0 AT", "2.5 AT"], STD],
    ],
    Hyundai: [
      ["Creta", "2021–", ["1.6 AT", "2.0 AT"], STD],
      ["Elantra", "2020–", ["1.6 AT", "2.0 AT"], STD],
      ["Santa Fe", "2018–2021", ["2.4 AT", "2.2 D AT"], ["basic", "ext", "trailer"]],
      ["Solaris", "2017–2022", ["1.4 MT", "1.6 AT"], ["basic"]],
      ["Tucson", "2021–", ["2.0 AT", "2.5 AT"], STD],
    ],
    Lada: [
      ["Granta", "2018–", ["1.6 MT"], ["basic"]],
      ["Largus", "2021–", ["1.6 MT"], ["basic"]],
      ["Niva Travel", "2021–", ["1.7 MT"], ["basic"]],
      ["Vesta", "2015–2022", ["1.6 MT", "1.8 AMT"], ["basic"]],
      ["Vesta NG", "2023–", ["1.6 MT", "1.8 CVT"], STD],
    ],
    BMW: [["3 Series G20", "2019–", ["320i AT", "330i xDrive AT"], STD], ["5 Series G30", "2016–2023", ["520d AT", "530i AT"], STD], ["X3 G01", "2017–", ["xDrive20d AT"], STD], ["X5 G05", "2018–", ["xDrive30d AT", "xDrive40i AT"], ["basic", "ext", "trailer"]]],
    Haval: [["Dargo", "2022–", ["2.0 DCT"], STD], ["F7", "2019–", ["1.5 DCT", "2.0 DCT"], STD], ["H6", "2020–", ["1.5 DCT"], STD], ["Jolion", "2021–", ["1.5 MT", "1.5 DCT"], STD]],
    Geely: [["Atlas", "2023–", ["2.0 AT"], STD], ["Coolray", "2019–", ["1.5 DCT"], STD], ["Monjaro", "2022–", ["2.0 AT"], STD]],
    "Mercedes-Benz Commercial Vehicles": [["Sprinter", "2018–", ["2.1 D MT", "3.0 D AT"], ["basic", "trailer"]], ["Vito", "2014–", ["2.1 D AT"], ["basic"]]],
  };
  var SHORT = {
    Audi: ["A4", "A6", "Q5", "Q7"], Changan: ["CS35 Plus", "CS55 Plus", "UNI-K"], Chery: ["Tiggo 4 Pro", "Tiggo 7 Pro Max", "Tiggo 8 Pro"], Chevrolet: ["Cruze", "Tahoe"],
    "Citroën": ["C4", "C5 Aircross"], Exeed: ["TXL", "VX"], Ford: ["Focus", "Kuga", "Transit"], Honda: ["Accord", "CR-V"], Jaecoo: ["J7"], Jeep: ["Grand Cherokee", "Wrangler"],
    "Land Rover": ["Defender", "Discovery", "Range Rover"], Lexus: ["LX", "NX", "RX"], Mazda: ["6", "CX-5", "CX-9"], "Mercedes-Benz": ["C-Class W206", "E-Class W214", "GLE V167"],
    Mitsubishi: ["L200", "Outlander", "Pajero Sport"], Nissan: ["Patrol", "Qashqai", "X-Trail"], Omoda: ["C5"], Opel: ["Astra", "Zafira Life"], Peugeot: ["3008", "408"],
    Porsche: ["Cayenne", "Macan"], Renault: ["Arkana", "Duster", "Logan"], Skoda: ["Karoq", "Kodiaq", "Octavia"], Subaru: ["Forester", "Outback"], Suzuki: ["Jimny", "Vitara"],
    Tank: ["300", "500"], Volkswagen: ["Polo", "Tiguan", "Touareg"], Volvo: ["XC60", "XC90"],
  };
  var YEARS = ["2016–2021", "2018–", "2019–2024", "2020–", "2021–", "2017–2022"];
  Object.keys(SHORT).forEach(function (b) {
    M[b] = SHORT[b].map(function (n, i) { var h = hash(b + n); return [n, YEARS[h % YEARS.length], h % 3 === 0 ? ["2.0 AT"] : ["2.0 AT", "2.5 AT"], STD]; });
  });

  var BRANDS = Object.keys(M).sort(function (a, b) { return a.localeCompare(b, "ru"); }).map(function (n) { return { id: slug(n), name: n }; });
  var MODELS = {}, TRIMS = {}, MODEL_BY_ID = {}, TRIM_BY_ID = {}, BRAND_BY_ID = {};
  BRANDS.forEach(function (b) {
    BRAND_BY_ID[b.id] = b;
    MODELS[b.id] = M[b.name].map(function (m, i) {
      var id = b.id + "~" + slug(m[0]) + "~" + i;
      var md = { id: id, brand: b.id, name: m[0], years: m[1], groups: m[3] };
      MODEL_BY_ID[id] = md;
      TRIMS[id] = m[2].map(function (t, j) { var tr = { id: id + "~" + j, model: id, name: t }; TRIM_BY_ID[tr.id] = tr; return tr; });
      return md;
    });
  });
  // Модель без комплектаций каталог не показывает (п.3.2) — кроме bZ4X: демо пустого уровня «нет под этот тип устройства».
  var CONFIG_BRANDS = ["toyota", "kia", "hyundai", "lada", "haval", "geely"];
  var MICROBOT_SKIP = ["porsche", "land-rover", "jeep", "tank", "mercedes-benz-commercial-vehicles"];

  // ── Карточки
  var ord = 0;
  var F = function (n, g, r, c, d) {
    var uniq = function (a) { return (a || []).map(busOf).filter(function (x, i, s) { return s.indexOf(x) === i; }); };
    return { n: n, g: typeof g === "number" ? (GROUP_BY_CODE[g] || "basic") : g, r: uniq(r), c: uniq(c), d: d, o: ++ord };
  };
  var CAMRY_FUNCS = [
    F("Зажигание", "basic", ["CAN1"]),
    F("Двигатель запущен", "basic", ["CAN1"]),
    F("Обороты двигателя", "basic", ["CAN1"]),
    F("Скорость автомобиля", "basic", ["CAN1"]),
    F("Дверь водителя", "basic", ["CAN2"]),
    F("Двери пассажиров", "basic", ["CAN2"]),
    F("Багажник", "basic", ["CAN2"], ["CAN2"], "Открытие — только при снятой охране"),
    F("Капот", "basic", ["CAN2"]),
    F("Ручной тормоз", "basic", ["CAN1"]),
    F("Педаль тормоза", "basic", ["CAN1"]),
    F("Положение АКПП «P»", "basic", ["CAN1"]),
    F("Центральный замок", "basic", ["CAN1", "CAN2", "BEAN", "LIN"], ["CAN2", "CAN3", "BEAN", "LIN (IN-OUT)", "CAN-FD2 (CAN-FD4)"], "Пример максимальной длины: 4 и 5 шин"),
    F("Аварийная сигнализация", "basic", [], ["CAN2", "BEAN"]),
    F("Габаритные огни", "basic", ["CAN2"], ["CAN2"]),
    F("Температура двигателя", "basic", ["CAN1"]),
    F("Уровень топлива", "basic", ["CAN1"]),
    F("Пробег", "basic", ["CAN1"]),
    F("Блокировка двигателя", "basic", [], ["CAN2"], "Блокировка через блок кузова, при выключенном зажигании"),
    F("Стеклоподъёмники: закрытие", "ext", [], ["LIN"]),
    F("Складывание зеркал", "ext", [10], [10]),
    F("Подогрев руля", "ext", ["CAN2"], ["CAN2", "CAN4"]),
    F("Подогрев сидений", "ext", ["CAN2"], ["CAN2", "LIN"]),
    F("Подогрев заднего стекла", "ext", ["CAN2"], ["CAN2"]),
    F("Климат-контроль: запуск", "ext", [], ["CAN3"]),
    F("Штатный дистанционный запуск", "ext", [], [1, 102], "Требуется обход иммобилайзера"),
    F("Ошибки двигателя (Check Engine)", "ext", ["CAN1"]),
    F("Давление в шинах", "ext", ["CAN3"]),
    F("Напряжение бортовой сети", "ext", ["CAN1"]),
    F("Ремень водителя", "ext", ["CAN2"]),
    F("Датчик дождя", 2, [57], [], "Код шины 57 приложению неизвестен"),
    F("Обход штатного иммобилайзера", "ext", [], [12, 13]),
    F("Уровень заряда тяговой АКБ", 3, [100]),
    F("Режим EV", 3, [100, 7], [100]),
    F("Готовность к движению (READY)", "ev", ["CAN1"]),
    F("Запас хода", 3, [101]),
  ];
  var CAMRY = {
    can: "2154",
    canLock: { v: "yes", note: "<p>Блокировка выполняется по <b>CAN2</b> через блок кузова.</p><ul><li>Только при выключенном зажигании.</li><li>После снятия блокировки двигатель запускается со второй попытки.</li></ul>" },
    keyless: { v: "no", note: "<p>Бесключевой обход на этой комплектации не поддерживается. Для автозапуска нужен <b>модуль обхода иммобилайзера</b> с подключением к IMO-KEY1.</p>" },
    notes: [
      "<p><b>Перед монтажом</b> отключите клемму АКБ минимум на 5 минут — иначе блок кузова может записать ошибку SRS.</p><p>На автомобилях после 2020 г. разъём CAN2 перенесён из-под рулевой колонки в блок кузова за вещевым ящиком.</p><ul><li>Не используйте контакты разъёма OBD для питания блока.</li><li>Для CAN3 нужен адаптер Pandora CAN-Y.</li></ul>",
      "<p>В комплектациях с проекционным дисплеем статус дверей по CAN2 приходит с задержкой до 1 с.</p>",
    ],
    funcs: CAMRY_FUNCS,
    points: [
      { type: "Зажигание", icon: "key", name: "Замок зажигания, разъём E4, контакт 6 (IG1)", photos: 7, desc: "Чёрно-красный провод, толщина 0,85 мм². Разъём находится за нижней панелью под рулевой колонкой, слева от педали тормоза." },
      { type: "CAN", icon: "settings_ethernet", name: "CAN1 — разъём OBD-II, контакты 6 / 14", photos: 3, desc: "Витая пара: CAN-H — 6, CAN-L — 14." },
      { type: "CAN", icon: "settings_ethernet", name: "CAN2 — блок кузова, разъём 4D, контакты 17 / 18", photos: 4, desc: "Блок кузова за вещевым ящиком, белый разъём на 40 контактов." },
      { type: "LIN", icon: "linear_scale", name: "LIN — блок двери водителя", photos: 2, desc: "Жёлтый провод в гофре двери." },
      { type: "Питание", icon: "bolt", name: "+12 В — монтажный блок под рулём", photos: 2, desc: "" },
      { type: "Масса", icon: "vertical_align_bottom", name: "Масса — болт у левой стойки", photos: 1, desc: "" },
      { type: "Концевик капота", icon: "sensors", name: "Капот — разъём штатного датчика", photos: 0, desc: "Фото пока нет." },
    ],
    schemes: [
      { t: "Схема подключения DX-4GS / DXL-4970", d: "Основная схема: CAN1 + CAN2, обход иммобилайзера. 2 листа" },
      { t: "Схема подключения микробота", d: "Только CAN1, без блокировки" },
    ],
    maps: [{ t: "Карта монтажа Camry XV70", d: "Расположение точек подключения, фото салона. 12 страниц" }],
    folk: [
      { t: "Camry 70 — монтаж без снятия торпедо", d: "Подключение через блок кузова за бардачком, CAN2 и замки. Время работы около 2,5 ч, нужен пластиковый съёмник." },
      { t: "Где взять зажигание на Camry 2.5 2021", d: "На рестайлинге IG1 перенесли: берём с разъёма E4 под рулём, а не с замка." },
      { t: "Автозапуск на гибриде: READY по CAN", d: "Как настроить статус готовности к движению вместо оборотов двигателя для XV70 Hybrid." },
      { t: "Обход иммобилайзера Camry 3.5", d: "Подключение к IMO-KEY1 и запись ключа через Pandora Spec, частые ошибки." },
      { t: "Складывание зеркал по LIN", d: "Какой провод в гофре водительской двери и какие настройки выставить в блоке." },
    ],
  };

  var BUSES_POOL = [["CAN1"], ["CAN1"], ["CAN2"], ["CAN1", "CAN2"], ["LIN"], ["CAN3"], []];
  function genCard(trim) {
    var md = MODEL_BY_ID[trim.model], b = BRAND_BY_ID[md.brand], h = hash(trim.id);
    var can = String(1000 + (h % 8900));
    if (can === "2154" || can === "1007") can = "3312";
    var funcs = CAMRY_FUNCS.filter(function (f, i) { return md.groups.indexOf(f.g) >= 0 && (h >> (i % 12)) % 3 !== 0; })
      .map(function (f, i) { var r = BUSES_POOL[(h + i) % BUSES_POOL.length]; var x = F(f.n, f.g, r, (h + i) % 4 === 0 ? r : [], f.d); x.o = f.o; return x; });
    if (md.groups.indexOf("trailer") >= 0) funcs = funcs.concat([F("Прицеп подключён", "trailer", ["CAN3"]), F("Габариты прицепа", "trailer", ["CAN3"], ["CAN3"]), F("Стоп-сигналы прицепа", "trailer", ["CAN3"])]);
    var caps = ["yes", "no", "nodata"];
    return {
      can: can,
      canLock: { v: caps[h % 3], note: h % 2 ? "<p>Блокировка по CAN1. Работает только с прошивкой блока от 3.50.</p>" : "" },
      keyless: { v: caps[(h >> 3) % 3], note: "" },
      notes: h % 2 ? ["<p>Шина CAN2 доступна только в комплектациях с электрическим стояночным тормозом.</p>"] : [],
      funcs: funcs,
      points: [
        { type: "Зажигание", icon: "key", name: "Замок зажигания, контакт IG1", photos: 3, desc: "" },
        { type: "CAN", icon: "settings_ethernet", name: "CAN1 — разъём OBD-II, контакты 6 / 14", photos: 2, desc: "" },
      ],
      schemes: [{ t: "Схема подключения " + b.name + " " + md.name, d: "Основная схема, 1 лист" }],
      maps: [], folk: h % 2 ? [] : [{ t: b.name + " " + md.name + " — монтаж за 2 часа", d: "Точки подключения и порядок разборки панели." }],
    };
  }
  var EMPTY_CARD = { can: "6021", canLock: { v: "nodata", note: "" }, keyless: { v: "nodata", note: "" }, notes: [], funcs: [], points: [], schemes: [], maps: [], folk: [] };

  var SPECIAL = {};
  var camryId = MODELS.toyota[0].id;
  SPECIAL[camryId + "~0"] = CAMRY;
  SPECIAL[camryId + "~1"] = Object.assign({}, CAMRY, { notes: [CAMRY.notes[1]], canLock: { v: "yes", note: "" } });
  SPECIAL[camryId + "~2"] = Object.assign({}, CAMRY, { keyless: { v: "yes", note: "<p>Обход по <b>IMO-KEY1</b>, нужна запись ключа.</p>" } });
  var rav = MODELS.toyota.filter(function (m) { return m.name === "RAV4"; })[0].id;
  var k5 = MODELS.kia.filter(function (m) { return m.name === "K5"; })[0].id;
  var granta = MODELS.lada.filter(function (m) { return m.name === "Granta"; })[0].id;
  SPECIAL[granta + "~0"] = EMPTY_CARD;

  var cache = {};
  function card(trimId) {
    if (cache[trimId]) return cache[trimId];
    var tr = TRIM_BY_ID[trimId], md = MODEL_BY_ID[tr.model], b = BRAND_BY_ID[md.brand];
    var base = SPECIAL[trimId] || genCard(tr);
    if (trimId === rav + "~1") base = Object.assign({}, genCard(tr), { can: "2154" });
    if (trimId === k5 + "~1") base = Object.assign({}, genCard(tr), { can: "1007" });
    if (trimId === camryId + "~1") base = Object.assign({}, base, { can: "2154" });
    var c = Object.assign({ id: trimId, brand: b.name, brandId: b.id, model: md.name, modelId: md.id, years: md.years, trim: tr.name }, base);
    cache[trimId] = c; return c;
  }
  // Индекс CAN-кодов (сервер ищет по коду)
  function byCan(code) {
    var out = [];
    Object.keys(TRIM_BY_ID).forEach(function (id) { if (card(id).can === code) out.push(card(id)); });
    return out;
  }
  var RECENT = [k5 + "~1", rav + "~0", MODELS.lada.filter(function (m) { return m.name === "Vesta"; })[0].id + "~0"];

  function brands(o) {
    o = o || {};
    return BRANDS.filter(function (b) {
      if (o.catalog === "configs" && CONFIG_BRANDS.indexOf(b.id) < 0) return false;
      if (o.type === "microbot" && MICROBOT_SKIP.indexOf(b.id) >= 0) return false;
      return true;
    });
  }

  return { GROUPS: GROUPS, BUS_ORDER: BUS_ORDER, BUSES: BUSES, BUS_FAMILY: BUS_FAMILY, busOf: busOf, UNKNOWN: UNKNOWN, BLOCK: BLOCK, BRANDS: BRANDS, MODELS: MODELS, TRIMS: TRIMS, MODEL_BY_ID: MODEL_BY_ID, TRIM_BY_ID: TRIM_BY_ID, BRAND_BY_ID: BRAND_BY_ID,
    card: card, byCan: byCan, brands: brands, RECENT: RECENT, CAMRY_TRIM: camryId + "~0", CAMRY_MODEL: camryId, K5_MODEL: k5, GRANTA_TRIM: granta + "~0" };
})();
