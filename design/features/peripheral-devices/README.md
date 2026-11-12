# Устройства системы · Приживление устройств

Периферия сигнализации (метки, реле, замки капота, датчики). Два экрана и две плитки расширенного монтажа; «Устройства системы» — только Bluetooth.

- `PeripheralDevices.jsx` — `SystemDevicesScreen`, `PairingScreen`, компоненты (`PD_*`) и демо-панель `usePdCtl`.
- `PeripheralDevices.html` — телефон 412×840; `PeripheralDevicesTablet.html` — 800×1280 / 1280×800 / 800×600 / 915×412; `PeripheralDevicesStates.html` — все состояния U1–U7, D1–D13 кадрами; `PeripheralDevicesParts.html` — компоненты, иконки, схемы.
- URL-параметры: `screen=devices|pairing`, `state=U3|D6…`, `row=A|B|C`, `pair=card|step`, `phase2=1`, `theme=dark`, `size=portrait|landscape|min|phone` (tablet).
- `icons/` — легаси-иконки типов (как есть, currentColor). `pairing/` — схемы приживления 800×550 (currentColor, акцент — primary).
- Источник требований: `uploads/peripheral-devices-design-brief.md`.
