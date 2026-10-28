// Конфигуратор — демо-данные. Значения настроек — патчи к текущим настройкам демо-блока (ключи = id из DeviceSettings TREE).
// IO-патч: { io: true, add: [...], del: [...], type?, pol? } — разворачивается в Configurator.jsx.
window.CFG_DATA = (function () {
  var MODELS = ["DX-9X", "DX-9X LoRa", "DX-9X Mini", "DX-91", "DX-91 LoRa", "DX-90B", "DX-90BT", "DX-6X", "DX-4GS", "DX-4G", "DX-40RS", "DX-40S", "DX-40R",
    "DXL-5000", "DXL-4950", "DXL-4910", "X-1900 BT", "X-1800L", "X-1700", "X-3190L", "X-3110", "VX-4G GPS", "VX-3100", "VX-1000", "NAV-10", "NAV-08", "NAV-12",
    "SMART PRO v3", "SMART v2", "Light Pro v2", "Light Pro", "BT-760", "BT-100", "IMMO-9", "IMMO-5", "CAN-6"];
  var BLOCK = { model: "DX-9X", serial: "1234567", can: "2154" };
  var RECENT_MODELS = ["DX-9X", "DX-4GS", "X-1900 BT"];
  var io = function (add, del, o) { return Object.assign({ io: true, add: add || [], del: del || [] }, o || {}); };
  // текущие значения блока относительно заводских (заводские → блок)
  var BLOCK_PATCH = { "1.1": "Polo Иванов", "2.1": 1, "2.2": 15, "3.2.4": 45, "6.1": "2154" };

  var NIVA = { "2.1": 0, "2.2": 5, "3.2.2": 4, "3.2.3": 2, "3.3.1": { type: "Тон", num: 1 }, "4.1": io(["Капот"], ["Багажник"]), "5.2": io([], ["Автозапуск по температуре"]) };
  var BIG = { "1.2": 1, "1.4": false, "1.5": 20, "2.1": 2, "2.2": 20, "2.3": -15, "2.4.1": { am: true, amT: "06:45", pm: false, pmT: "18:00" }, "2.4.3": { am: true, amT: "07:00", pm: true, pmT: "17:30" }, "2.5": "06:45",
    "3.1.1": 3, "3.1.2": 7, "3.1.3": 4, "3.2.1": false, "3.2.2": 8, "3.2.3": 1, "3.2.4": 60, "3.2.6": 6, "3.2.5.1": true, "3.2.5.2": 6, "3.2.5.3": true,
    "3.3.1": { type: "Мелодия", num: 4 }, "3.3.4": { type: "Мелодия", num: 20 }, "4.2": io(["Ручной тормоз"], [], { type: 1 }), "5.1": io(["Люк"], ["Кодовый канал (HM-05)"], { pol: 0 }), "5.4": io(["Подогрев руля"]), "6.1": "2155", "6.2": 1 };

  var D = function (s) { return s; };
  var MINE = [
    { id: "m1", name: "Шевроле Нива — без автозапуска", desc: "Отключён автозапуск, сирена тише, концевик капота на IN1 вместо багажника.", model: "DX-9X", created: "02.10.26 09:12", updated: "14:20 21.10.26", patch: NIVA },
    { id: "m2", name: "Polo — полный комплект, автозапуск по расписанию", desc: "Расписание по будням, все датчики, люк на CH1. Проверено на трёх машинах.", model: "DX-9X", created: "28.09.26", updated: "11:05 20.10.26", patch: BIG },
    { id: "m3", name: "Базовая охрана", desc: "", model: "DX-9X", created: "25.09.26", updated: "18:40 18.10.26", patch: {} },
    { id: "m4", name: "Kia Rio 2019 — дистанционный запуск с телефона и меток", desc: "Автозапуск по температуре −15 °C, работа двигателя 20 минут, прогрев салона, управление подогревом сидений с метки.", model: "DX-4GS", created: "20.09.26", updated: "09:30 17.10.26", patch: { "2.3": -15, "2.2": 20 } },
    { id: "m5", name: "Такси — тихая охрана", desc: "Без звука сирены при постановке и снятии.", model: "X-1900 BT", created: "19.09.26", updated: "16:02 15.10.26", patch: { "1.4": false } },
    { id: "m6", name: "Конфигурация с очень длинным названием на пятьдесят", desc: "Название — 50 символов, проверка переноса и многоточия в карточке и в шапке редактора.", model: "DX-9X LoRa", created: "15.09.26", updated: "12:00 12.10.26", patch: { "3.2.2": 6 } },
    { id: "m7", name: "Hilux — прицеп", desc: "", model: "DX-9X", created: "10.09.26", updated: "10:15 08.10.26", patch: { "5.4": io(["Габариты"]) } },
    { id: "m8", name: "Офисный шаблон", desc: "Стартовая точка для всех новых установок DX-4GS.", model: "DX-4GS", created: "05.09.26", updated: "08:50 01.10.26", patch: {} },
  ];
  var CARS = ["Lada Vesta", "Kia K5", "Toyota Camry", "Haval Jolion", "Hyundai Creta", "Skoda Octavia", "Geely Coolray", "Chery Tiggo 7 Pro"];
  for (var i = 0; i < 26; i++) {
    var dd = String(30 - (i % 28)).padStart(2, "0");
    MINE.push({ id: "g" + i, name: CARS[i % CARS.length] + " — вариант " + (i + 2), desc: i % 3 === 0 ? "" : "Типовая установка, автозапуск по температуре, " + (i % 2 ? "без сирены." : "с подогревом сидений."),
      model: ["DX-9X", "DX-4GS", "X-1900 BT", "DX-91"][i % 4], created: dd + ".08.26", updated: "1" + (i % 10) + ":" + String((i * 7) % 60).padStart(2, "0") + " " + dd + ".09.26", patch: { "3.2.2": i % 10 } });
  }

  var HTML = "<p>Конфигурация для штатного ключа с кнопкой багажника. Автозапуск по <b>температуре и расписанию</b>, прогрев двигателя до 20 минут.</p><ul><li>Обходчик иммобилайзера — по CAN, отдельный модуль не нужен.</li><li>Капот подключён на IN1, концевик штатный.</li><li>Сирена — тихий режим при постановке ночью.</li></ul><p>Перед записью проверьте, что в блоке прошивка для CAN-кода <b>2154</b>.</p><p>Если после записи не работает закрытие стёкол, включите пункт 5.3 вручную.</p>";
  var ATT = [{ k: "img", t: "Точка подключения CAN под рулём" }, { k: "img", t: "Разъём BCM, вид сзади" }, { k: "pdf", t: "Схема подключения обходчика" }, { k: "img", t: "Концевик капота" }, { k: "img", t: "Место установки блока" }];
  var CLOUD = [
    { id: "c1", name: "Автозапуск по температуре и расписанию", short: "Штатный ключ, обход по CAN, капот на IN1, тихая сирена ночью.", author: "Сергей Петров", login: "spetrov", updated: "19.10.26", created: "02.06.26", rating: 5, votes: 23, can: "2154", map: true, att: ATT, html: HTML, patch: NIVA, mineOnly: false },
    { id: "c2", name: "Полный комплект с метками", short: "Метки, люк, подогрев руля и сидений, все датчики включены.", author: "Pandora Lab", login: "lab", updated: "12.10.26", created: "11.03.26", rating: 4, votes: 8, can: "2154", map: true, att: ATT.slice(0, 2), html: HTML, patch: BIG },
    { id: "c3", name: "Только охрана, без автозапуска", short: "Минимальная установка: двери, капот, багажник, блокировка двигателя.", author: "Андрей К.", login: "akuz", updated: "30.09.26", created: "30.09.26", rating: null, can: "2154", map: false, att: [], html: "<p>Минимальная установка без автозапуска.</p>", patch: { "2.1": 0 } },
    { id: "c4", name: "Для рестайлинга 2021 года", short: "Другая шина CAN — нужна прошивка под 1032.", author: "Сергей Петров", login: "spetrov", updated: "22.08.26", created: "01.08.26", rating: 4, votes: 3, can: "1032", map: true, att: ATT.slice(2, 3), html: HTML, patch: { "6.1": "1032" } },
    { id: "c5", name: "Моя тестовая", short: "Черновик, виден только мне.", author: "Иван Сидоров (вы)", login: "isidorov", updated: "20.10.26", created: "20.10.26", rating: null, can: "2154", map: false, att: [], html: "", patch: {}, mineOnly: true },
  ];
  var FILE = { name: "Нива_клиент_Иванов.dxs", patch: { "2.1": 0, "3.2.2": 6, "4.1": io(["Капот"]) } };
  return { MODELS: MODELS, BLOCK: BLOCK, RECENT_MODELS: RECENT_MODELS, BLOCK_PATCH: BLOCK_PATCH, MINE: MINE, CLOUD: CLOUD, FILE: FILE, NIVA: NIVA, BIG: BIG };
})();
