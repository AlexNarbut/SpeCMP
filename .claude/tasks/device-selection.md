# Задача: Поиск и подключение устройства (feature/device/selection)

Статус: **реализовано** (бизнес-модули + selection-ui + интеграция). `:androidApp:assembleDebug` — успешно.

## Итог реализации (что сделано)

- `core/specialist`: дорегистрированы `bluetoothConnectorInteractor`,
  `usbConnectorInteractor`, `bluetoothAlarmDeviceInteractor`.
- **Бизнес-модуль `feature/device/selection`** (логический): `DeviceScanRepository`
  (фасад BLE/USB-скана) + домен `ScannedDevice`.
- **Бизнес-модуль `feature/device/interaction`** (логический):
  `DeviceConnectionRepository` (сессия/статус), `AlarmDeviceRepository`
  (инфо/boot-data/disconnect) + домен (`DeviceConnectionState`,
  `DeviceConnectionResult`, `AlarmDeviceInfo`, `BootData`).
- **UI-модуль `feature/device/selection-ui`**: `ScanDeviceViewModel`
  (реактивный скан gated по permission-стадии, bond-проверка, ветки BASE/PAIRING/BOOT
  и USB, one-shot эффекты), экран на `Cmp*`, роут `DeviceSelectionRoute`, DI,
  строковые ресурсы. Юнит-тесты VM (скан/коннект/bond/ошибка) — зелёные.
- **`core/permissions`** (новый): самодостаточная нативная реализация
  (`ScanPermissionController` + `rememberScanPermissionController`) — Android
  runtime-запрос через `rememberLauncherForActivityResult`, iOS/desktop — no-op.
- **composeApp**: маршрут `DeviceSelectionRoute` + entry, вход из
  `MainNavigationTarget.AdvancedMount`. Результат подключения пока заглушка (pop),
  до `feature/device/interaction-ui`.
- **Манифесты**: Android — BLE (`BLUETOOTH_SCAN neverForLocation`, `BLUETOOTH_CONNECT`,
  legacy location/bluetooth до API 30) + USB-host feature; iOS — `NSBluetoothAlwaysUsageDescription`.

## UI по макету (design/HANDOFF.md) — реализовано

- Состояния: `Rationale` (обоснование + per-платформенные строки, «Разрешить»/«Не сейчас»),
  `Denied` (подсказки «как починить» + «Открыть настройки»/«Повторить»), `AdapterOff`
  (Bluetooth выключен, best-effort детект через `isBluetoothEnabled()`), `Scanning` (радар-герой
  + спиннер), `Empty` (после таймаута без находок, поиск продолжается фоном), `Connecting`
  (заглушка + «Отмена»), список.
- Адаптив: `Compact` (телефон) · `Stacked` (планшет-портрет, центр ≤620) · `Split`
  (ландшафт/desktop — master-detail: список + карточка «Подключить»), через `BoxWithConstraints`.
- Строка устройства: фото-слот (плейсхолдер = иконка типа) · имя · чип транспорта · адрес.
- Только токены `cmpThemeColors()`, только `Cmp*`. Иконки добавлены в `core/ui` (`Bluetooth`,
  `BluetoothSearching`, `BluetoothDisabled`, `Usb`, `Sensors`, `Memory`, `VpnKey`, `SearchOff`,
  `TouchApp`, `Link`).
- **Переводы:** `values/strings.xml` (EN, default) + `values-ru/strings.xml` (RU), ключи `device_select_*`.
- **Грациозная деградация** (нет данных в SDK-скане): RSSI-столбики и реальные фото не
  показываем; анимированный радар заменён спиннером; USB-адрес = производитель. Отмечено как
  дальнейший polish при появлении данных.

## Версии SDK-либ (уточнено)

- specialist 1.1.76 **транзитивно тянет common-либы 2.0.x** (резолвится в 2.0.64).
  Пакет `ru.alarmtrade.bluetoothControl.*` (публичный BLE-API specialist) — в артефакте
  `alarmtrade-bluetooth` 2.0.x. Поэтому все non-specialist alarmtrade-либы держим на
  `alarmtrade-common = 2.0.42` (фактически поднимается до 2.0.64). USB-типы — в самом
  specialist. См. [[alarmtrade-sdk-versions]].

## Permissions — permissions-common 2.0.42 (moko), через DI

- `PermissionsProvider` (интерфейс в `core/permissions`: `getController()` + `getBlePermission()`)
  создаётся **через DI** — `permissionsModule()` (expect/actual): Android — `AndroidPermissionsProvider(get<Context>())`,
  desktop/iOS — без контекста. Android-контекст в Koin регистрирует composition root
  (`composeApp` `rememberPlatformKoinModules()` → `single<Context>`), т.к. Koin стартует в `App()`.
- **`BindEffect` — один раз в корне** (`App.kt`): `BindEffect(koinInject<PermissionsProvider>().getController())`.
  Контроллер app-scoped, биндится к корневой `FragmentActivity`.
- **`ScanDeviceViewModel` инжектит `PermissionsProvider` напрямую** и сам проверяет/запрашивает
  доступ (`checkPermission()`): по `getBlePermission()` — `getPermissionState`/`providePermission`;
  `DeniedAlwaysException` → `Denied(canOpenSettings=true)` + «Открыть настройки»; `Denied/RequestCanceled`
  → `Denied(false)` + «Повторить». Rationale-экрана нет — системный диалог сразу.
- Запуск проверки — init-источник `stateIn(Lazily)` (первая подписка на state), не `LaunchedEffect`
  (см. [[no-launchedeffect-vm-start]] и `docs/patterns.md`).
- `AppActivity` — `FragmentActivity` (нужно для `bind`); в androidApp добавлен `androidx.fragment`.
- Креды репо — в `local.properties` (`signing.GITEA_*`). См. [[alarmtrade-sdk-versions]].

Ниже — исходный план (актуален как проектное описание).

---

Статус исходного плана: согласован, реализован.

Цель: экран поиска устройств Pandora по Bluetooth (BLE) и USB, подключение к
выбранному устройству и передача управления во flow работы с устройством
(`feature/device/interaction`, вне рамок этой задачи).

---

## Часть 1. Конспект логики (как это работает в старом проекте)

Источник: `specOldKmp/.../presentation/app/auth/pandoraDevice/selection` (Decompose).

### 1.1. Структура экранов
- `DeviceSelectionComponent` — контейнер с child-stack из двух экранов:
  - `ScanDeviceScreen` (по умолчанию) — поиск + список + подключение.
  - `ConnectionManualScreen` — статичные инструкции по подключению (BLE/USB).
- Наружу контейнер отдаёт два коллбэка:
  - `onAlarmDeviceSelected(AlarmDeviceInformation)` — устройство в рабочем режиме
    подключено, дальше — flow взаимодействия.
  - `onBleBootSelected(BluetoothManufacturerData)` — устройство в режиме BOOT
    (загрузчик), дальше — flow прошивки/восстановления.

### 1.2. Жизненный цикл `ScanDeviceViewModel`
Зависимости (всё из `SpecialistApp`):
`bluetoothAlarmDeviceInteractor`, `bluetoothConnectorInteractor`,
`usbConnectorInteractor` + `PermissionsProvider`.

Состояния экрана: `DeviceScanState = { Default, PermissionCheck, PermissionError(name, needOpenAppSettings), Scan }`.
Состояние списка: `LCEState<List<ScanDeviceItemViewModel>>` (None/Loading/Content/Error).

Поток:
1. `onStart → prepareScan()`:
   - `checkPermission()` → состояние `PermissionCheck`; по каждому BLE-permission
     из `permissionsProvider.getBlePermission()` — `getPermissionState`, при
     необходимости `providePermission`. `DeniedException`/`DeniedAlwaysException`
     → `PermissionError` (для «always» кнопка ведёт в системные настройки).
   - если разрешения есть → `startScan()`.
2. `startScan()` (состояние `Scan`, список `Loading`): параллельно два сбора:
   - `usbConnectorInteractor.scanUsbDevicesUseCase` → `List<AttachedUsbDeviceInformation>`.
   - `bluetoothConnectorInteractor.scanBluetoothDevicesUseCase` → `List<BleDeviceScanResult>`.
   - обе коллекции мержатся в единый список item-моделей (`updateDevicesListView`),
     пока пусто — держим `Loading`.
3. `connectDevice(item)` — по типу item:
   - **BLE** (`connectBle`): на Android (не iOS) и если устройство не в BOOT —
     проверка bond-состояния (`hasDeviceBond`/`hasPhoneBond`); при рассинхроне
     bond — тост «удалите сопряжение на телефоне/устройстве», иначе `innerConnectBle`.
     `innerConnectBle`: `openBluetoothAlarmDeviceSessionFlowableUseCase(ConnectToBluetoothDeviceRequest(uuid, ConnectionParameters(timeout=20s)))`
     → collect `ConnectingStatus`:
       - `CONNECTED` + `ScannedDeviceState.BASE|PAIRING` → `getAlarmDeviceInformationUseCase` → `onAlarmDeviceSelected`.
       - `CONNECTED` + `ScannedDeviceState.BOOT` → `getBluetoothManufacturerDataUseCase` → `onBleBootSelected`.
       - `BONDING/BONDED/CONNECTING` → прогресс-тост; `*_FAIL/FAIL` → ошибка-тост.
   - **USB** (`connectUsb`): `openUsbDeviceSessionFlowableUseCase(ConnectToUsbDeviceRequest(id))`
     → collect `UsbConnectingState`: `READY` → `getAlarmDeviceInformationUseCase` → `onAlarmDeviceSelected`; иначе — ошибка-тост.
   - после завершения выбора (`invokeOnCompletion`) — закрытие временных сессий
     сканера (`closeUsbDeviceSessionUseCase`, `closeBluetoothDeviceSessionUseCase`).
4. `onStop → stopScan()`: отменить job'ы сканирования + `stopBluetoothScanUseCase`.

### 1.3. Типы данных
- `BleDeviceScanResult` (`specialist.domain.bluetooth.scan`): `uuid`, `name`,
  `deviceType`, `state: ScannedDeviceState`, `hasDeviceBond`, `hasPhoneBond`.
- `AttachedUsbDeviceInformation` (`specialist.domain.usb`): `id`, `productName`,
  `manufacturerName`, `productId`.
- `ConnectingStatus`, `ConnectionParameters`, `ScannedDeviceState`,
  `ConnectToBluetoothDeviceRequest` — из библиотеки `bluetooth-common`
  (пакет `ru.alarmtrade.bluetoothControl.*`).
- Permissions: `PermissionsController`, `Permission`, `PermissionState`,
  `DeniedException`, `DeniedAlwaysException` — из `permissions-common`.

### 1.4. Разрешения по платформам (старый проект)
- **Android**: до API 31 — `COARSE_LOCATION` + `LOCATION`; с API 31 (S) —
  дополнительно `BLUETOOTH_CONNECT` + `BLUETOOTH_SCAN`.
- **iOS**: `getBlePermission()` → пустой список (запрос CoreBluetooth показывает
  система при старте сканирования; в Info.plist нужен `NSBluetoothAlwaysUsageDescription`).
- **Desktop (jvm)**: свой `PermissionsController`; USB — без runtime-permission.

---

## Часть 2. Как это ложится на архитектуру нового проекта

Отличия, которые меняют реализацию (не копируем старый код 1:1):

| Аспект | Старый проект | Новый проект |
|---|---|---|
| Навигация | Decompose child-stack | **Nav3** граф внутри фичи (`docs/navigation.md`) |
| ViewModel | кастомный `ViewModel` | **`BaseViewModel`** из `core/ui` (`safeLaunch`) |
| UI | `material3` напрямую | только **`Cmp*`** из `core/ui` |
| Ошибки/тосты | `Toaster` + строки | типизированные ошибки в state + эффекты (см. support-chat) |
| Строки | `SharedRes` | compose-resources фичи (`docs/localization.md`) |
| Permissions | app-модуль `compose-permissions` | **`core/*`** модуль (app-level инфра нельзя в feature) |
| Repository | ViewModel дергает SDK напрямую | ViewModel → узкий **Repository** фичи, `Response<T>` |

### 2.1. Модульная раскладка (паттерн `<name>` + `<name>-ui`)
Репозитории-фасады SDK выносим в **логические (бизнес) модули**, экраны — в
отдельные `-ui` модули. `docs/architecture.md`: логический модуль = `domain`+`data`+DI
без UI, переиспользуется другими фичами как узкий API; presentation → логика.

**Бизнес-модули (без UI, без Compose):**
- `feature/device/selection` — домен обнаружения + `DeviceScanRepository`
  (BLE-скан + USB-скан + stop). Фасадит `bluetoothConnectorInteractor` /
  `usbConnectorInteractor` (только скан-часть).
- `feature/device/interaction` — домен сессии/устройства +
  `DeviceConnectionRepository` (открыть/закрыть сессию, статус подключения BLE/USB)
  и `AlarmDeviceRepository` (`getAlarmDeviceInformation`,
  `getBluetoothManufacturerData`, `disconnect`). Фасадит
  `bluetoothConnectorInteractor` / `usbConnectorInteractor` (session-часть) и
  `bluetoothAlarmDeviceInteractor`.

**UI-модули (presentation):**
- `feature/device/selection-ui` — **эта задача**: экран поиска+подключения. VM
  компонует **оба** логических модуля (скан из `selection`, connect+info из
  `interaction`). Разрешено: переиспользование — только через логические модули.
- `feature/device/interaction-ui` — flow работы с устройством (следующая задача).

**Инфраструктура:**
- `feature/device` — пустой группирующий контейнер (уже в `settings.gradle.kts`).
- **Новый `core/permissions`** — expect/actual обёртка над `permissions-common`
  (`PermissionsProvider`: `getController()`, `getBlePermission()`). App-level инфра —
  в `core`, не в feature. Использует `selection-ui` VM (permission-gate перед сканом).

Зависимости бизнес-модулей: `core:common` (Response), `core:specialist`,
`libs.alarmtrade.specialist`, `libs.alarmtrade.bluetooth`, `libs.alarmtrade.usb`,
coroutines, `koin.core`. **Без `core:ui` и Compose.**
Зависимости `selection-ui`: `core:common`, `core:ui`, `core:localization`,
`core:permissions`, `project(":feature:device:selection")`,
`project(":feature:device:interaction")`, nav3, koin-compose, lifecycle-viewmodel,
Compose-плагины.

### 2.2. Граница фичи (что отдаёт наружу)
- Бизнес-модули отдают только **доменные** типы и `Response<T>` — SDK-типы
  (`BleDeviceScanResult`, `AttachedUsbDeviceInformation`, `AlarmDeviceInformation`,
  `ConnectingStatus`, `BluetoothManufacturerData`) наружу не выходят.
- `selection-ui` через свой Nav3-граф отдаёт в `composeApp` доменный
  `DeviceConnectionResult` (Working / Boot). Куда вести дальше (interaction-ui /
  firmware) — решает `composeApp`; сейчас — заглушки/лог.

---

## Часть 3. План реализации (по шагам)

### Шаг 0. `core/specialist` — дорегистрировать интеракторы
В `core/specialist/SpecialistModule.kt` добавить `single`-провайдеры:
`bluetoothConnectorInteractor`, `usbConnectorInteractor`,
`bluetoothAlarmDeviceInteractor` (сейчас их нет). Обновить
`.ai/agents/**/AGENTS.md` для core/specialist при необходимости.

### Шаг 1. `core/permissions` (новый модуль)
- commonMain: expect-класс/интерфейс `PermissionsProvider`
  (`getController(): PermissionsController`, `getBlePermission(): List<Permission>`).
- actual: android (location + BLUETOOTH_SCAN/CONNECT по API), jvm (no-op контроллер,
  пустой список), iosArm64 (пустой список, CoreBluetooth сам покажет запрос).
- DI `permissionsModule` (Koin) + подключение в composition root (`App.kt`).
- Зависимость `libs.alarmtrade.permissions`. Добавить `:core:permissions` в
  `settings.gradle.kts`. Завести `core/permissions/.ai`-описание/AGENTS.

### Шаг 2. Бизнес-модуль `feature/device/selection` (логический)
- `build.gradle.kts`: таргеты jvm/android/iosArm64; deps по разделу 2.1 (без UI).
- `domain/model`: `ScannedDevice` (sealed `Ble`(uuid,name,type,scannedState,bond-флаги)/
  `Usb`(id,productName,manufacturer,productId)); `DeviceTransport` enum.
- `domain/repository/DeviceScanRepository`:
  `scanBluetooth(): Flow<Response<List<ScannedDevice.Ble>>>`,
  `scanUsb(): Flow<Response<List<ScannedDevice.Usb>>>`, `suspend stopScan()`.
- `data/repository/SpecialistDeviceScanRepository` — над
  `bluetoothConnectorInteractor`/`usbConnectorInteractor` (только скан), маппинг
  SDK→домен, обёртка в `Response`, **не бросает**.
- `di/DeviceSelectionModule` (Koin).

### Шаг 3. Бизнес-модуль `feature/device/interaction` (логический)
- `build.gradle.kts`: как Шаг 2.
- `domain/model`: `DeviceConnectionState` (доменный аналог `ConnectingStatus`/
  `UsbConnectingState`), `DeviceConnectionResult` (`Working(AlarmDeviceInfo)` /
  `Boot(BootData)`), `BondConflict` (none/removeOnPhone/removeOnDevice),
  доменные `AlarmDeviceInfo`, `BootData` (обёртки, без SDK-типов).
- `domain/repository`:
  - `DeviceConnectionRepository`: `connectBle(uuid, timeout): Flow<Response<DeviceConnectionState>>`,
    `connectUsb(id): Flow<Response<DeviceConnectionState>>`,
    `suspend closeSessions()`.
  - `AlarmDeviceRepository`: `suspend getAlarmDeviceInformation(): Response<AlarmDeviceInfo>`,
    `suspend getBootData(): Response<BootData>`, `suspend disconnect()`.
- `data/repository`: реализации над `bluetoothConnectorInteractor`/
  `usbConnectorInteractor`/`bluetoothAlarmDeviceInteractor`. Использует
  `ConnectToBluetoothDeviceRequest`/`ConnectionParameters` из `bluetooth-common`,
  `ConnectToUsbDeviceRequest` из SDK. Маппинг SDK→домен, `Response`, не бросает.
- `di/DeviceInteractionModule` (Koin).

### Шаг 4. UI-модуль `feature/device/selection-ui` (presentation) — эта задача
- Новый модуль в `settings.gradle.kts` (`:feature:device:selection-ui`),
  `build.gradle.kts` по образцу `feature/support-chat`; deps по разделу 2.1.
- `presentation/screen/scan/ScanDeviceViewModel : BaseViewModel`:
  - зависимости: `DeviceScanRepository`, `DeviceConnectionRepository`,
    `AlarmDeviceRepository`, `PermissionsProvider`.
  - state: `ScanDeviceState(stage: ScanStage, devices: LCEState<List<DeviceItem>>, isConnecting)`;
    `ScanStage = { PermissionCheck, PermissionError(name, needSettings), Scanning }`.
  - эффекты (one-shot) для ошибок подключения — по образцу
    `SupportChatScreenViewModel` (без `Toaster`).
  - методы: `prepareScan()`, `retry()`, `connect(item)`, `openAppSettings()`.
    Параллельный сбор скан-флоу под `scanJob`; остановка в `onCleared`/уходе.
    `MutableStateFlow` только `update { }`; выбор устройства сериализуем.
  - логика connect (bond-проверка, ветки BASE/PAIRING vs BOOT, USB READY) —
    из конспекта 1.2, но на доменных типах репозиториев.
- `presentation/model`: `DeviceItem` (sealed Ble/Usb) + маппер из домена.
- UI на `Cmp*`: `ScanDeviceScreen` (PermissionCheck / PermissionError / Loading /
  Content(список) / Error(повтор) + диалог прогресса подключения). Строки — в
  `composeResources` модуля (`docs/localization.md`).
- `presentation/navigation/DeviceSelectionNavGraph.kt` — Nav3-граф по образцу
  `LoginNavGraph`: роут `ScanDeviceRoute` (граф расширяемый под `ManualsRoute`
  позже), `SavedStateConfiguration` polymorphic, `NavDisplay`. Наружу — коллбэки
  `onDeviceConnected(DeviceConnectionResult)`, `onClose`.

### Шаг 5. Интеграция в composeApp
- `AppNavigation.kt`: добавить `DeviceSelectionRoute` в `navConfig` и `entry<>`;
  в `navigateFromMain` — `MainNavigationTarget.AdvancedMount → backStack += DeviceSelectionRoute`.
- Результат `onDeviceConnected` — пока заглушка/лог (до `interaction-ui`).

### Шаг 6. Платформенные разрешения и манифесты
- **Android** (`androidApp`): `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (API 31+,
  `neverForLocation` при отсутствии геологики), `ACCESS_FINE/COARSE_LOCATION`
  (до API 31), USB-host feature + `device_filter`; сверить с `docs/build.md`.
- **iOS** (`iosApp`): `NSBluetoothAlwaysUsageDescription` (+ `...WhenInUse`).
- **Desktop**: USB-транспорт; permissions — no-op.

### Шаг 7. Тесты и верификация
- Юнит-тесты репозиториев (fake-интеракторы) и `ScanDeviceViewModel`
  (fake-репозитории): стадии permission → scan → connect, эффекты ошибок.
- Финальная сборка: `./gradlew :androidApp:assembleDebug`.

---

## Часть 4. Решения по развилкам (согласовано)

- **A. Платформы/транспорт (1-я итерация):** Android — BLE + USB; iOS — только BLE;
  **Desktop (jvm) — USB** (донгл специалиста). BLE на desktop не делаем.
  → В `build.gradle.kts` фичи цели jvm/android/iosArm64; USB-ветка активна на
  android+jvm, BLE-ветка — на android+ios. Недоступный транспорт на платформе
  просто не сканируется (пустой поток), UI это переживает.
- **B. Точка входа:** `MainNavigationTarget.AdvancedMount` → открывает граф поиска.
  Остальные пункты монтажа пока не трогаем.
- **C. Manuals:** **отложить.** 1-я итерация — только поиск + подключение
  (`ScanDeviceRoute`). Граф проектируем расширяемым под `ManualsRoute` позже.
- **D. Формат результата:** отдаём **доменную обёртку** `DeviceConnectionResult`
  (Working/Boot), не «сырой» SDK-тип наружу — чтобы `composeApp` и будущий
  `interaction` не зависели от SDK-типов напрямую. Внутри обёртки при необходимости
  держим ссылку/идентификатор для `interaction`.
- **E. Permissions:** создаём **новый `core/permissions`** (expect/actual поверх
  `permissions-common`), подключаем в composition root, фиче даём узкий интерфейс.

- **F. Раскладка модулей:** паттерн `<name>` + `<name>-ui`. Бизнес-логику фасадим
  в логических модулях `feature/device/selection` (`DeviceScanRepository`) и
  `feature/device/interaction` (`DeviceConnectionRepository` + `AlarmDeviceRepository`);
  UI — в отдельных `-ui` модулях. Репозитории — 3 по областям.
- **G. Объём задачи:** бизнес-модули (`selection` + `interaction`, только нужные
  репозитории) **+ `selection-ui`** (рабочий экран поиска, интегрирован в app).
  `interaction-ui` — следующая задача.

### Уточнения объёма по итогам решений
- Desktop USB: проверить в `desktopApp`/`docs/build.md`, что USB-транспорт SDK
  работает на jvm (донгл); permissions на desktop — no-op контроллер.
- Пункт `AdvancedMount` в `AppNavigation.navigateFromMain` перестаёт быть `Unit` —
  добавляет `DeviceSelectionRoute` в back stack.
- `interaction` создаём как бизнес-модуль уже сейчас (его репозитории нужны
  экрану `selection-ui`); его `-ui` — позже.
