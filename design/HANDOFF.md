# Хендофф: Экран поиска / выбора устройства (device-select) — 2026-07-21

Эта папка — распакованная выгрузка макетов Pandora Spec. Реализуй в коде только то, что описано ниже.
Все пути указаны относительно корня этой папки.

## Область (что реализуем)
- Экран **«Выбор устройства»** (`features/device-select/`) — поиск и выбор блока сигнализации
  перед монтажом: запрос разрешений по платформе, сканирование, список найденных устройств,
  подключение.
- Точка входа на **главном экране**: карточки **«Быстрый монтаж»** и **«Расширенный монтаж»**
  ведут на этот экран (экран одинаковый для обоих сценариев).

Остальные экраны (auth, calculator, home в остальном, support-chat, app-update) — вне области.

## Контекст и намерение
После выбора сценария монтажа монтажнику нужно подключиться к блоку сигнализации. Доступные
транспорты и запрашиваемые разрешения различаются по платформам, поэтому экран платформо-зависимый:
- **Android** — доступ к **Bluetooth (BLE)** и **USB**.
- **iOS** — доступ **только к BLE**.
- **Desktop** — без запроса разрешений: поток начинается сразу со сканирования.

Устройства в приложении — только **BLE** и **USB-HID**. По каждому выводим имя, опционально адрес
(MAC для BLE / `HID vid:pid` для USB), уровень сигнала (RSSI) для BLE и картинку типа устройства.

## Поток (state machine)
```
permission → (Разрешить) → scanning → list | empty
permission → (Не сейчас)  → denied  → (Открыть настройки) → permission
adapterOff → (Включить)   → scanning
scanning  → (авто, ~2.2 c) → list | empty
list → выбор → connecting              (compact / stacked)
list → выбор → detail → Подключить → connecting  (split / master-detail)
```
Состояния: **permission** (запрос доступа, набор зависит от платформы), **denied** (доступ отклонён
+ подсказки «как починить»), **adapterOff** (Bluetooth выключен), **scanning** (радар + спиннер),
**list** (найденные устройства), **empty** (ничего не найдено, поиск продолжается),
**connecting** (подключение — заглушка, следующий экран вне области).

## Требования заказчика (важные решения макета)
- Список — **единый плоский**, у каждого устройства иконка транспорта (Bluetooth/USB).
- Строка устройства: **плейсхолдер-слот фото** (в макете полосатый — заменить реальным снимком
  модели Pandora) + иконка типа · имя · чип транспорта · адрес моноширинно · RSSI-столбики (BLE)
  либо значок USB.
- **Нет** кнопки «Искать снова» и **нет** ручного ввода адреса — поиск идёт автоматически и
  продолжается фоном (индикатор «поиск…» в шапке списка и в пустом состоянии).
- «Подключение…» — заглушка; следующий экран (сам монтаж) — вне рамок этой задачи.

## Файлы-источники (макеты = истина по UI)
| Путь | Роль | Что взять |
|---|---|---|
| features/device-select/DeviceSelect.jsx | экран `DeviceSelectScreen` (`window.DeviceSelectScreen`) | вся разметка, состояния, адаптив, демо-данные `DEVICES` |
| features/device-select/DeviceSelect.html | хост Compact / телефон (Android-фрейм 412×840) | референс-окно; тумблеры темы, платформы (Android/iOS), состояния |
| features/device-select/DeviceSelectTablet.html | хост планшет 800×1280 (портрет) и 1280×800 (ландшафт) | Medium/Expanded раскладка |
| features/device-select/DeviceSelectDesktop.html | хост Compose Desktop 1280×800 | Desktop-поведение (без разрешений, master-detail, hover) |
| features/device-select/DeviceSelectStates.html | обзорный лист: каждое состояние × 3 размера | быстрый визуальный аудит всех состояний в mobile/планшет/desktop |
| features/device-select/README.md | описание фичи | сводка решений и потока |
| res/nav.js | навигация макетов | ключи device-select[-tablet\|-desktop\|-states] |
| features/home/Home.jsx | контроллер главного | onNav: "quick"/"extended" → device-select[-desktop] |

## Дизайн-система (обязательно соблюдать)
- Токены: `design-system/styles.css` + `design-system/tokens/*` — только `--*`, без хардкода
  цветов/размеров. Ключевые: `--primary-container`/`--on-primary-container`,
  `--secondary-container` (чип транспорта), `--surface-container-low` (строки/карточки),
  `--surface-variant` (слот фото, нейтральный герой), `--error-container` (герой «нет доступа»),
  `--outline-variant`, `--font-mono` (адрес). Темы Light/Dark через `data-theme` — обе обязательны.
- Иконки: Material Icons (Filled) через каталог CmpIcons (`res/icons/cmp-icons.js`):
  bluetooth, bluetooth_searching, bluetooth_disabled, usb, sensors, memory, vpn_key, lock,
  settings, search_off, touch_app, link, chevron_right, check_circle, ArrowBack.
- Зона касания ≥ 48 dp (строки устройств ≥72, кнопки «Разрешить»/«Открыть настройки»/«Подключить»).
- Компоненты — обёртки Cmp* над Material 3 из `components/**`. Использованы: `CmpTopAppBar`,
  `CmpIconButton`, `CmpButton`, `CmpOutlinedButton`, `CmpTextButton` (с фолбэком), `CmpSurface`,
  `CmpText`, `CmpIcon`, `CmpDivider`, `CmpCircularProgressIndicator` (см. `.d.ts`/`.prompt.md`).

## Адаптив (Window Size Classes по ширине контейнера)
- **compact** (<600) — телефон: один столбец, строки на всю ширину; выбор → сразу `connecting`.
- **stacked** (600–839, портрет) — планшет-портрет: центрированный столбец `max-width` 620, крупнее.
- **split** (Expanded / ландшафт / Desktop) — master-detail: слева список, справа карточка
  выбранного устройства (крупный слот фото, тип/связь/адрес/сигнал) и кнопка «Подключить».

Класс выбирается по реальной ширине контейнера (хук `useLayout` в DeviceSelect.jsx),
а не по типу устройства.

## Зависимости / общие части
- `components/navigation/CmpTopAppBar.*`, `components/buttons/CmpButton.*`,
  `components/buttons/CmpOutlinedButton.*`, `components/buttons/CmpIconButton.*`,
  `components/surfaces/CmpSurface.*`, `components/surfaces/CmpDivider.*`,
  `components/app-kit/CmpText.*`, `components/app-kit/CmpIcon.*`,
  `components/progress/CmpCircularProgressIndicator.*`, `components/app-kit/useStateLayer` (hover/press).
- `components/app-kit/PhoneFrame.jsx` — только рамка демо-хоста, в приложение не переносится.
- `res/nav.js` — навигация макетов; в приложении роутинг единый (после quick/extended → этот экран).

## Как смотреть макеты
Открыть HTML-файлы экранов в браузере (автономные React+Babel прототипы во фрейме устройства/окне).
В тулбарах хостов: тема, платформа (Android/iOS), состояние экрана. `DeviceSelectStates.html`
показывает все состояния сразу в трёх размерах. Общая навигация — `index.html`.

## Чек-лист реализации
- [ ] UI совпадает с макетом в светлой и тёмной теме (все состояния потока).
- [ ] Только токены `--*`, без инлайновых цветов.
- [ ] Хит-таргеты ≥ 48 dp, тексты читаемы.
- [ ] Разрешения по платформам: Android BLE+USB, iOS только BLE, Desktop — без запроса.
- [ ] Состояния denied / adapterOff с подсказками «как починить»; путь в системные настройки.
- [ ] Сканирование с авто-переходом в список/«пусто»; поиск продолжается фоном (индикатор «поиск…»).
- [ ] Строка устройства: слот фото + иконка типа, имя, чип транспорта, адрес (mono), RSSI/USB.
- [ ] Compact/Stacked → выбор ведёт сразу в connecting; Split → master-detail + «Подключить».
- [ ] Вход: главный → «Быстрый монтаж» / «Расширенный монтаж».
- [ ] Заметки/ограничения ниже учтены.

## Не входит в задачу
- Остальные экраны (auth, calculator, home в остальном, support-chat, app-update) — не менять.
- Реальные BLE/USB-стеки, системные диалоги разрешений, реальное подключение — интеграция
  по API приложения (в макете — моки и заглушка «Подключение…»).
- Следующий экран после подключения (сам монтаж) — вне области.

## Заметки / открытые вопросы
- Демо-устройства зашиты в `DEVICES` (5 шт.: BLE-блоки/метка + USB-HID модуль обхода) —
  в приложении заменить реальным результатом сканирования.
- Фото устройств — полосатые плейсхолдер-слоты; заменить реальными снимками моделей Pandora
  (по типу устройства).
- RSSI в макете статичный; в приложении обновляется в реальном времени.
- «Подключение…» — заглушка с кнопкой «Отмена» (→ back в список); реальный переход добавится
  вместе со следующим экраном.
