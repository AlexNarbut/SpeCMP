// Pandora Spec — иконки разделов (duotone в манере main_*: базовый слой opacity .3 + деталь полным тоном).
// Сетка 24, fill=currentColor. Используются в «Расширенном монтаже», на главной и в «Заявках на техпомощь».
// Рендерить inline (не <img>), чтобы работал currentColor.
(function () {
  var KEY = "M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z";
  var CALL = "M20.01 15.38c-1.23 0-2.42-.2-3.53-.56a.977.977 0 0 0-1.01.24l-1.57 1.97c-2.83-1.35-5.48-3.9-6.89-6.83l1.95-1.66c.27-.28.35-.67.24-1.02-.37-1.11-.56-2.3-.56-3.53 0-.54-.45-.99-.99-.99H4.19C3.65 3 3 3.24 3 3.99 3 13.28 10.73 21 20.01 21c.71 0 .99-.63.99-1.18v-3.45c0-.54-.45-.99-.99-.99z";
  var FOB = '<path opacity=".3" d="M6 8h5a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-8a3 3 0 0 1 3-3z"/><circle cx="8.5" cy="12.5" r="2"/><rect x="6.5" y="17" width="4" height="1.8" rx=".9"/>';
  var S = function (body) { return '<svg viewBox="0 0 24 24" width="100%" height="100%" fill="currentColor" aria-hidden="true">' + body + "</svg>"; };
  window.PS_SECTION_ICONS = {
    // Расширенные настройки — панель с ползунками (не путать с «Конфигуратором»: шестерня + база).
    adv_settings: S('<rect opacity=".3" x="2" y="3" width="20" height="18" rx="4"/><path d="M5 7h14v1.6H5zM5 11.2h14v1.6H5zM5 15.4h14v1.6H5z"/><circle cx="9" cy="7.8" r="2.4"/><circle cx="15.5" cy="12" r="2.4"/><circle cx="7.5" cy="16.2" r="2.4"/>'),
    // Заявки на техпомощь — заявка + обратный звонок.
    tech_requests: S('<defs><mask id="psTrCall"><rect width="24" height="24" fill="#fff"/><path transform="translate(13.6 13.6) scale(.34)" d="' + CALL + '" fill="#000"/></mask></defs><rect opacity=".3" x="2" y="2" width="14" height="19" rx="2.5"/><path d="M5 6h8v1.7H5zM5 9.6h8v1.7H5zM5 13.2h4.5v1.7H5z"/><circle cx="17.5" cy="17.5" r="5.5" mask="url(#psTrCall)"/>'),
    // Процедура Clone — два ключа, в манере «Бесключевого обхода».
    clone: S('<path opacity=".3" transform="translate(5.6 -2.4) scale(.78)" d="' + KEY + '"/><path transform="translate(0 6.8) scale(.78)" d="' + KEY + '"/>'),
    // Устройства системы — брелок/метка на связи.
    sys_devices: S('<g transform="translate(1.2 0)">' + FOB + '<path d="M15 9.2a3.2 3.2 0 0 0-2.4-3.1l.45-1.75A5 5 0 0 1 16.8 9.2zM18.6 9.2a6.8 6.8 0 0 0-5.1-6.6L13.95.85A8.6 8.6 0 0 1 20.4 9.2z"/></g>'),
    // Приживление устройств — брелок/метка + добавить.
    pairing: S('<g transform="translate(.8 0)">' + FOB + '<path d="M17.1 2h1.8v3.1H22v1.8h-3.1V10h-1.8V6.9H14V5.1h3.1z"/></g>'),
  };
})();
