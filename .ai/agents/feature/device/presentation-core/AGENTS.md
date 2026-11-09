# feature/device/presentation-core

- Общий presentation фич устройства, без экранов и DI: его подключают фичи устройства (`device:{autostart,firmware,state,settings-ui}`, `advanced-mount`, `fast-mount`, `configurator-ui`) вместо чужих `-ui` модулей. Сам не зависит ни от одного `*-ui` (graph-assert). Пакет — `ru.alarmtrade.specmp.feature.device.presentation.core`.
- `api`: `core:ui` (супертип `BaseViewModel`) и `feature:device:interaction` (`DeviceSession`, модели блока).
- `viewmodel/BaseDeviceViewModel<S, E>(deviceSession, initialState)` — база VM, работающих с подключённым блоком, поверх `BaseViewModel<S, E>` (правила VM действуют):
  - `connectedDevice` / `connectedDeviceName` — блок на связи (`Ready`) как поток для `collectIntoState`; `currentDevice` — снимок для интентов.
  - `launchOnDevice { }` — работа с блоком: без связи не стартует (`null`), при потере связи отменяется вся запущенная так работа (параллельные запуски не отменяют друг друга); `launchOnDevice(key) { }` — то же с заменой прежней работы по ключу (процесс экрана вместо поля `Job?`), отмена — `cancelLaunched(key)`. Слежение за связью стартует с первого вызова — `super.onStart()` звать не нужно.
  - `onEachDeviceReady { }` (на каждое (пере)подключение, отменяется при потере связи) и `onDeviceLost { }` (на каждый выход сессии из `Ready`, в т.ч. если VM создана до подключения) — вызывать из `onStart()`; живут, пока жива VM (не только пока экран подписан на state).
  - Необработанные исключения логируются, как у `safeLaunch`.
- `pin/ServicePinDialog(message, ServicePinDialogState, …, testTagPrefix)` — диалог PIN программирования (BLE), общий для прошивки и режима программирования; строки — `deviceinteraction_pin_*` в `core/localization`.
