# Конфигуратор (configurator)

Работа с настройками без подключённого блока + загрузка / сохранение конфигураций из редактора блока. ТЗ — `uploads/configurator-design-brief.md` (заменяет C1–C4, C6, C7 из device-settings-design-brief).

## Файлы

- **cfg-data.js** — `window.CFG_DATA`: модели блоков, «Мои конфигурации» (8 + 26 сгенерированных для подгрузки), облачные конфигурации, файл .dxs. Значения — патчи к текущим настройкам демо-блока DX-9X (ключи = id из `DeviceSettings` TREE).
- **CfgKit.jsx** — новые компоненты кита (`window.CfgKit`): `ModelBadge`, `Rating`, `Plaque`, `MineCard`, `CloudCard`, `AttachGrid`, `OptRow`, `Chip`, `TextArea`, `FullDlg`, `Sheet`, `Popover`, `SysFile`, `Toast`, `Progress`, `DiffView` + `diffRows` (K9).
- **Configurator.jsx** — `ConfiguratorApp` (стек экранов K1–K7, оверлеи K6/K8/K9) + `useCfgCtl` (панель витрины) + `CFG_PRESETS` (сценарии).
- **Configurator.html** — Compact 412×840. **ConfiguratorTablet.html** — 800×1280 / 1280×800. **ConfiguratorDesktop.html** — 1280×800, 800×600, 1600×900.
- **ConfiguratorStates.html** — все сценарии сеткой (K8). **ConfiguratorParts.html** — компоненты кита, light и dark рядом.
- Использует `features/device-settings/DeviceSettings.jsx` (редактор) и `features/vehicle-select` (выбор автомобиля, точка E, проп `onPicked`).

## Редактор (DeviceSettingsScreen, новые пропсы)

`source { kind: block|mine|file|cloud|factory, title, model }` → шапка (название + плашка источника + модель + «Только просмотр»), меню «⋯», панель внизу:
block — «Записать»; mine/file — «Сохранить» (файл — с подтверждением перезаписи); cloud/factory — «Сохранить как…» → K6.
`writeAvail: ok|hide|disable` + `writeReason` — пункт «Записать в блок». `baseValues`, `initialEdits`, `toast`, `apiRef` (`load / commit / values / snack / n`), `onAction(key)`.
Меню блока: «Загрузить из…», «Сохранить в…», «Сбросить к заводским». Без `onAction` пункты ведут в конфигуратор (`?entry=block&go=load|saveTo`).

## Решения

- K1 широкое окно (≥600): список слева, справа карточка выбранной: «Открыть», «Записать в блок», «Сохранить в файл», ниже — «Изменить описание», «Копия», «Удалить». Меню ⋮: шторка (compact) / поповер (wide).
- «Записать в блок» без блока или при другой модели: оба варианта — скрыт / неактивен с пояснением (переключатель витрины).
- K9 на телефоне — полноэкранный диалог, на широком — диалог 820 px с колонками «Было / Станет». Разделы свёрнуты при > 10 изменениях (первый раскрыт), поиск — при > 20. CAN-код — первой выделенной строкой. Тап по строке — полные значения.
- Загрузка в блок только подставляет значения: подсветка, «Изменено: N · Записать», тост. Из K1/K5 — тот же K9, затем переход в редактор блока с загруженными значениями.
- Модель в облаке фильтруется чипом «Модель блока» на списке конфигураций (каталог автомобилей — общий экран E).
- Системные окна файлов не рисуются — показан момент вызова (пунктирная заглушка).

## Навигация

Ключи `res/nav.js`: `configurator`, `configurator-tablet`, `configurator-desktop`, `configurator-states`, `configurator-parts`. Главная → плитка «Конфигуратор».
