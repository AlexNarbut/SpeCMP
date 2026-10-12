# Устройство: поиск → сессия → interaction-обёртка → расширенный монтаж

**Статус:** план, ждёт апрува (2026-10-12).
**Ветка:** `feature/device-connection`.
**Источник логики:** `specialistDesktop/app/.../mainApp/alarmDevice/{selection,session,interaction}`,
`AlarmDeviceViewModel.kt`, `advancedMount/*`.
**Предыдущая задача:** `device-selection.md` (поиск + подключение, реализовано).

Статусы: [ ] не начато · [x] готово · [~] частично

---

## Как это устроено в старом проекте

- **`DeviceSessionCreationComponentChildStack`**: стек из двух экранов, `DeviceSelection` → `Session`.
  - Выбор устройства пушит `Session`.
  - Выход из `Session` вызывает `closeUsbDeviceSessionUseCase` и закрывает весь flow.
- **`DeviceSelection`**: свой стек экранов — тип подключения, поиск, инструкции. При выборе устройства:
  1. открывает сессию нужного типа;
  2. дожидается `READY`;
  3. читает информацию об устройстве;
  4. вызывает колбэк.
- **`DeviceInteractionComponent`** оборачивает дочерний flow (расширенный монтаж).
  - `DeviceInteractorViewModel` слушает `getDeviceConnectionStateFlowUseCase`:
    - `READY` → читает информацию об устройстве: `MAIN` или `BOOT`;
    - любое другое состояние или ошибка → `DISCONNECTED`.
  - Роутер обёртки:
    - `Disconnected` пушится поверх текущего экрана;
    - `openMain` срезает стек до `Main`, так что дочерний flow остаётся там же, где был;
    - `Boot` заменяет `Main`;
    - «Назад» на `Disconnected` выходит из всего flow.
- **`AlarmDeviceViewModel`** — базовый класс для экранов, которые работают с устройством.
  - Держит `deviceState` (Connected / Disconnected) и кэш `AlarmDeviceInformation` вместе с features.
  - На `READY` перечитывает информацию с повтором.
  - `safeLaunchInDeviceScope` запускает работу с устройством в `deviceJob`; на дисконнекте `deviceJob` отменяется.
  - Хуки `doOnConnected` и `doOnDisconnected`.
- **`AdvancedMountViewModel`** (наследник `AlarmDeviceViewModel`) — корневое меню расширенного монтажа.
  - На подключении: верификация устройства, получение remote-информации, меню по features устройства.
  - Ещё: IMEI, непрочитанные сообщения чата, переход в BOOT.
- **SDK 1.2.0 / common 2.0.93**:
  - `BluetoothAlarmDeviceInteractor.getDeviceConnectionStateFlowUseCase` → `ConnectionState`: `DISCONNECTED / CONNECTING / CONNECTED / READY / UNKNOWN`;
  - `AlarmDeviceInformation.isInBootMode()`, `getAlarmDeviceAppFeaturesUseCase`;
  - один интерактор `bluetoothAlarmDevice` обслуживает и BLE, и USB.

---

## Этап 0 — прогон текущей реализации и исправления

Прогон 2026-10-12 на Pixel 8 (Android 16, BLE), рядом с реальными блоками DX4G, X-1911BT и DX95FDS_AR.

Работает:
- системный диалог разрешений;
- поиск: список обновляется, найдено от 1 до 3 устройств;
- подключение к DX4G в режиме сопряжения: системный диалог «Подключить устройство», сопряжение, `Session created`, чтение информации, выход из экрана.

Найдено:

| # | Проблема | Как проявилось | Исправление |
|---|---|---|---|
| 0.1 | Сессия после выхода не закрывается, а при входе в поиск не сбрасывается | После успешного подключения экран закрылся, BLE-канал до DX4G остался открытым (`ACL LE:Y`). Повторный поиск: «Устройства не найдены» | В начале flow поиска (до первого скана) вызывать `closeSessions()`. Тот же шаг — п. 1 задачи пользователя |
| 0.2 | После «Отмены» (и после неудачного подключения) поиск не возобновляется | После отмены — «Устройства не найдены», новый `onScannerRegistered` в логе не появился. При повторном входе DX4G тоже не найден | Перед подключением явно вызывать `stopScan()`. После отмены или ошибки: `closeSessions()`, затем перезапуск скана (переподписка или явный restart). Сначала выяснить по байткоду SDK, как скан связан с connect: SDK запускает внутри connect свой скан (`Main connect 2`) |
| 0.3 | Подключение висит дольше таймаута | Сопряжение не подтверждено, bond упал через 30 с, экран ещё дольше оставался на «Подключение…» вместо 20 с | Таймаут на стороне VM (`withTimeoutOrNull`) для BLE и USB, по таймауту — `closeSessions()` и ошибка |
| 0.4 | USB: нет состояния FAILED | По коду: `UsbConnectingState.FAILED` отображается в `CONNECTING`, поэтому ожидание бесконечно | Отображать `FAILED` → `FAILED` (плюс общий таймаут из 0.3) |
| 0.5 | Ошибка одного транспорта скрывает другой | По коду: `mergeDevices` отдаёт `Error` на весь список | Ошибку транспорта показывать, только если оба пусто или ошибка; иначе — список того, что найдено |
| 0.6 | Проверка сопряжения работает и на iOS | По коду; в старом проекте проверка была только на Android | Делать проверку только там, где SDK отдаёт флаги сопряжения (Android) |
| 0.7 | Строка в VM | `DEFAULT_SCAN_ERROR` захардкожен по-русски | Перенести в ресурсы |
| 0.8 | Однократный возврат на главный экран после выдачи разрешения | Один раз, не воспроизвелось | Наблюдать; не чиним без воспроизведения |

- [x] 0.1–0.7, тесты VM на отмену, таймаут, USB FAILED и частичную ошибку; `:androidApp:assembleDebug`; повторный прогон на Pixel 8 (см. «Итог этапа 0»).

### Этап 0 — апрув и уточнения (2026-10-12)

Апрув: «отдельным коммитом; после отмены поиск возобновлять; ввести дополнительный таймаут и поправить остальные недочёты».

Факты SDK (исходники `alarmtrade-bluetooth` и `bluetooth-common` 2.0.93):
- **Сканер один на всё приложение**, у него единственный `ScanCallback`.
  - `startScan` при уже идущем поиске Android молча игнорирует: callback тот же.
  - `scanDevices` сам останавливает поиск в `onCompletion`.
- **`connect` (BLE) внутри запускает свой поиск с фильтром по manufacturer** (`scanDevices(true)`, до 8 с) и не останавливает его.
- **Цикл переподключения в `connect`.** По таймауту SDK эмитит `CONNECTING_FAIL` и пробует снова, бесконечно. Финал у нас — первый неуспешный статус, как и раньше.
  - При отмене сбора `onCompletion` делает `dispose` и `disconnect(true)`.
  - Bond: `createBond` ждёт решения пользователя; системный таймаут около 30 с, затем `BONDING_FAIL`.
- **`closeBluetoothDeviceSessionUseCase` закрывает только уже созданную сессию.**
- **iOS:** `isBonded` всегда `false`, поэтому `hasPhoneBond` неизвестен.
- **Поправка к 0.3:** зависания не было. Экран вернулся к списку после `BONDING_FAIL` (около 32 с), а скриншот был снят раньше.

Правки:
- **0.1** — при старте VM, до первого поиска: `connectionRepository.closeSessions()`.
- **0.2**
  - `SpecialistDeviceScanRepository.scanBluetooth()`: перед стартом вызывать `stopBluetoothScanUseCase`, чтобы сбросить чужой (внутренний) поиск на общем callback.
  - `SpecialistDeviceConnectionRepository.connectBle()`: в `onCompletion` вызывать `stopBluetoothScanUseCase`, чтобы остановить внутренний поиск SDK.
  - VM: счётчик перезапусков поиска. После отмены, ошибки или таймаута — `closeSessions()`, затем новый поиск.
- **0.3** — общий таймаут подключения в VM: `CONNECT_FLOW_TIMEOUT_MILLIS = 60_000` (поиск до 8 с, сопряжение до 30 с, соединение до 20 с). По таймауту: `ConnectionFailed`, `closeSessions()`, поиск заново.
- **0.4** — `UsbConnectingState.FAILED` → `DeviceConnectionState.FAILED`.
- **0.5** — `mergeDevices`: `Error` только если оба транспорта дали ошибку или ошибка плюс пусто; иначе список найденного.
- **0.6** — `ScannedDevice.Ble.hasPhoneBond: Boolean?` (`null`, когда платформа его не сообщает: `platformType() != ANDROID`). Проверка сопряжения работает только при известном значении.
- **0.7** — убрать `DEFAULT_SCAN_ERROR`. Текст ошибки в UI не выводится (Error рисуется как поиск), в state кладётся `message.orEmpty()`.

> Пункты 0.1–0.3 частично совпадают с этапом 1. Этап 0 правит текущий экран минимально, чтобы им можно было пользоваться. Этап 1 перестраивает его в flow.

---

## Этап 1 — flow поиска (`feature/device/selection-ui`)

- [ ] Граф `DeviceSelectionNavGraph` — flow со своими подэкранами: поиск, а позже инструкции подключения (USB / BLE), как `DeviceManuals` в старом проекте.
- [ ] На входе в flow:
  1. сбросить старую сессию (`closeSessions`);
  2. запросить разрешения;
  3. проверить адаптер и геолокацию;
  4. запустить все доступные на платформе сканы (BLE и/или USB).
- [ ] При выборе устройства: остановить скан → по транспорту открыть сессию нужного типа (`connectBle` / `connectUsb`) → дождаться `CONNECTED` / `READY`.
- [ ] Результат flow — колбэк `onSessionCreated(device: ConnectedDevice)` (транспорт, id, имя). Информацию об устройстве selection больше **не читает**: это делает interaction, как в старом проекте, где её на `READY` читает `DeviceInteractorViewModel`. `DeviceConnectionResult` (Working / Boot) уходит из selection: режим определяет interaction.
- [ ] Куда вести после `onSessionCreated`, решает вызывающий: `composeApp` передаёт цель (расширенный или быстрый монтаж).

## Этап 2 — сессия и устройство (`feature/device/interaction`, логика)

- [ ] `DeviceSessionRepository` (singleton):
  - `state: Flow<DeviceLinkState>` поверх `getDeviceConnectionStateFlowUseCase`: `Connecting` | `Ready` | `Disconnected`;
  - `suspend getDeviceInfo()` — информация вместе с features и режимом `Main` / `Boot` (`isInBootMode()`);
  - `suspend closeSession()` — BLE и USB.
- [ ] Доменные модели: `DeviceLinkState`, `ConnectedDevice`, расширенный `AlarmDeviceInfo` (systemId, systemName, firmwareVersion, operationMode, features, canInformation).
- [ ] Device scope для экранов, работающих с устройством (замена `AlarmDeviceViewModel`): **композиция вместо наследования** — `DeviceScope`, который VM получает через DI.
  - `state: StateFlow<DeviceSessionState>` (Connected(info) / Disconnected);
  - `launchInDevice { }` — работа, которая отменяется на дисконнекте;
  - `onConnected { }`.
  - Альтернатива — наследник `BaseViewModel`. **Вопрос 2.**

## Этап 3 — interaction-обёртка (`feature/device/interaction-ui`, новый модуль)

- [ ] `DeviceInteractionHost(onExit, content)` — composable-обёртка вокруг дочернего flow и `DeviceInteractionViewModel` в той же nav-записи.
  - VM слушает `DeviceSessionRepository.state`: на `Ready` читает информацию → `Main(info)` или `Boot(info)`; иначе — `Disconnected`.
  - `Main` → `content()`, то есть собственный `NavDisplay` дочернего flow.
  - `Disconnected` (и начальный `Connecting`) → экран дисконнекта **поверх** content. Content остаётся в композиции: его back stack и VM сохраняются, на `Ready` экран убирается, и пользователь оказывается там же, где был.
  - «Назад» на экране дисконнекта перехватывается обёрткой (back-обработчик поверх дочернего `NavDisplay`) → `closeSession()` → `onExit()`: выход из **всего** flow, а не из одного подэкрана.
  - `Boot` → экран-заглушка режима загрузчика (**вопрос 3**).
- [ ] Закрытие сессии при выходе из flow (`onExit`) и при уничтожении записи, если выход был не через `onExit`.
- [ ] Проверить на устройстве, переподключается ли SDK сам после обрыва BLE (выключить и включить блок). От этого зависит, нужен ли на экране дисконнекта «Повторить».

## Этап 4 — расширенный монтаж (`feature/advanced-mount`, `-ui`, новые модули)

- [ ] Свой граф навигации, корневой экран — меню разделов, как в `AdvancedMountViewModel`:
  - заголовок с информацией об устройстве;
  - меню по features устройства;
  - сами разделы пока заглушки, без переходов.
- [ ] Обёрнут в `DeviceInteractionHost`.
- [ ] В объём **не входят** (отдельные задачи): верификация устройства, remote-информация, IMEI, счётчик непрочитанных сообщений чата, переход в BOOT, сами подразделы (настройки, прошивки, автозапуск, динамические устройства, пульт). **Вопрос 4.**

## Этап 5 — интеграция в `composeApp`

- [ ] `MainNavigationTarget.AdvancedMount` → `DeviceSelectionRoute(target = Advanced)` → `onSessionCreated` → заменить запись поиска на `AdvancedMountRoute` (обёрнутый в interaction), чтобы «Назад» из монтажа не возвращал в поиск.
- [ ] `QuickMount` — та же схема позже, с тем же `DeviceInteractionHost`.

## Этап 6 — проверка и документация

- [ ] Тесты:
  - VM поиска: сброс сессии, остановка скана, таймаут;
  - `DeviceInteractionViewModel`: Ready → Main / Boot, дисконнект, повторное подключение;
  - `DeviceScope`: отмена работы на дисконнекте.
- [ ] `:androidApp:assembleDebug`, compile JVM и iOS.
- [ ] Прогон на Pixel 8: вход → поиск → подключение → меню → выключить блок (дисконнект поверх) → включить (возврат на место) → снова выключить и нажать «Назад» (выход из flow, сессия закрыта) → повторный вход находит блок.
- [ ] `.ai/agents/feature/device/**/AGENTS.md`, `docs/navigation.md` (паттерн обёртки) → `.ai/generate.sh`.

---

## Открытые вопросы

1. Этап 0 делаем отдельным коммитом до перестройки (этапы 1–6) или сразу в рамках этапа 1?
2. Device scope: композиция (`DeviceScope` через DI) или базовый класс-наследник `BaseViewModel`, как `AlarmDeviceViewModel`?
3. BOOT-режим в этой итерации: заглушка «устройство в режиме загрузчика» или переносим `BootModeScreen` (обновление прошивки)?
4. Объём корневого экрана расширенного монтажа: только меню по features или сразу верификация, remote-информация и IMEI?
5. Инструкции подключения в flow поиска — сейчас или позже?

### Итог этапа 0 (2026-10-12)

- [x] Код 0.1–0.7.
- [x] Тесты `selection-ui`: 10 из 10, добавлены 6 — сброс сессии, отмена с перезапуском поиска, таймаут, USB FAILED, частичная ошибка, неизвестное сопряжение.
- [x] `:androidApp:assembleDebug`, `:composeApp:compileKotlinIosArm64`, `:composeApp:compileKotlinJvm`.
- Pixel 8:
  - [x] **0.1.** Подключение → выход (сессия открыта) → повторный вход: при старте сессия закрывается, канал рвётся (`CONNECTION_TERMINATED_BY_LOCAL_HOST`), DX4G снова в списке.
  - [x] **0.2, поиск.** После «Отмены» поиск действительно стартует заново: новый `RegisterScanner` сразу после отмены. До правки его не было.
  - [ ] **0.2, отменённое устройство.** Само устройство после отмены не находится. **Ошибка SDK** (`bluetooth-common` 2.0.93, `BluetoothDeviceRepositoryImpl.connect`): отмена подписки после `connectGatt` не закрывает GATT-клиент.
    - Колбэки `BluetoothManager` продолжают работу (`onConfigureMTU` → `discoverServices` → `setCharacteristicNotification`).
    - Блок остаётся подключённым (`ACL LE:Y` больше 80 с) и не рекламируется.
    - Сессии нет, поэтому `closeBluetoothDeviceSessionUseCase` ничего не делает.
    - Отпускает только завершение процесса.

### Решение по отмене (апрув 2026-10-12)

Обход ошибки SDK: подключение не обрывается пользователем.
1. **Кнопку «Отмена» убрать:**
   - `ConnectingBody`;
   - `onCancelConnecting` в VM и экране;
   - строку `device_select_btn_cancel`.
2. **«Назад» запрещён во время подключения.** Системное «Назад» (`NavigationBackHandler` из `navigationevent-compose`, всегда включён на экране) и стрелка в заголовке вызывают `viewModel.onBack()`.
   - VM при `Connecting` игнорирует, иначе эмитит `ScanDeviceEffect.Close`;
   - экран по эффекту вызывает `onClose`;
   - стрелка во время подключения скрыта.
3. **Таймаут 60 с остаётся.** Тест «отмена → перезапуск поиска» заменён на «неудача → закрытие сессии → новый поиск», плюс тест на `onBack`.

**Проверка (2026-10-12):**
- `selection-ui` jvmTest: 12 из 12.
- `:androidApp:assembleDebug`, compile iOS и JVM — проходят.
- Pixel 8:
  - кнопки «Отмена» нет;
  - системное «Назад» во время подключения игнорируется, подключение доходит до `Session created`;
  - повторный вход находит DX4G (старая сессия закрыта);
  - «Назад» на поиске (системное и стрелка) закрывает экран.
