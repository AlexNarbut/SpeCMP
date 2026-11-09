package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.lifecycle.AppForegroundProvider
import ru.alarmtrade.specmp.core.permissions.BlePermissionResult
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.domain.model.connectableAdapters
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.ScanError
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.restricted
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.toUiItem

/**
 * ViewModel экрана поиска устройства.
 *
 * Разрешения запрашиваются **самой VM** через инжектированный [PermissionsProvider]
 * (контроллер app-scoped, забинжен в корне приложения) — без промежуточного
 * rationale-экрана. Проверка запускается один раз при первой подписке на state
 * ([onStart], см. docs/patterns.md — не через `LaunchedEffect`), при возврате в приложение
 * ([AppForegroundProvider.foregroundReturns]) — заново.
 * Сканирование — реактивный источник (BLE + USB `Flow`, при `serialEnabled` — и COM-порты), активный только на шаге
 * [ScanStep.Scanning], пока экран подписан на state. COM-порт подключается как USB: сессия SDK по порту, дальше —
 * `DeviceSession` ([ConnectionTransport.SERIAL]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanDeviceViewModel(
    private val scanRepository: DeviceScanRepository,
    private val connectionRepository: DeviceConnectionRepository,
    private val deviceSession: DeviceSession,
    private val permissionsProvider: PermissionsProvider,
    private val foregroundProvider: AppForegroundProvider,
    private val dispatchers: AppDispatchers,
    serialEnabled: Boolean = false,
    /** Поиск для быстрого монтажа: BLE-устройства, которые не сигнализации, видны, но недоступны. */
    private val alarmsOnly: Boolean = false,
) : BaseViewModel<ScanDeviceState, ScanDeviceEffect>(
    ScanDeviceState(
        requirements = permissionsProvider.deviceAccessRequirements(),
        supportedTransports = scanRepository.supportedTransports()
            .filter { it != DeviceTransport.SERIAL || serialEnabled }
            .toSet(),
    ),
) {

    override suspend fun onStart() {
        // Поиск перезапускается и без смены шага (Scanning → Scanning при возврате в приложение) — через reload().
        select { step }
            .flatMapLatest { step -> if (step is ScanStep.Scanning) scanDevices() else flowOf(LCEState.None) }
            .flowOn(dispatchers.default)
            .collectIntoState { devices -> copy(devices = devices) }
        // Подсказка «пусто» зависит только от шага: reload() поиска её не сбрасывает.
        safeLaunch {
            repeatOnSubscription {
                select { step }
                    .flatMapLatest { step -> if (step is ScanStep.Scanning) emptyHintTimer() else flowOf(false) }
                    .collect { searchedLong -> reduce { copy(searchedLongEnough = searchedLong) } }
            }
        }
        // Возврат в приложение отслеживается всю жизнь VM, а не только пока экран подписан на state.
        safeLaunch { foregroundProvider.foregroundReturns.collect { recheckOnForeground() } }
        // Прошлая сессия могла не закрыться (или ещё закрывается после выхода из flow устройства):
        // подключённое устройство не рекламируется и не появится в поиске — дожидаемся закрытия.
        deviceSession.close().join()
        checkPermissionsExclusively()
    }

    private fun scanDevices(): Flow<LCEState<List<DeviceUiItem>, ScanError>> {
        val supportedTransports = currentState.supportedTransports
        val serial = if (DeviceTransport.SERIAL in supportedTransports) {
            scanRepository.scanSerial().flowOn(dispatchers.io).onStart { emit(Response.Success(emptyList())) }
        } else {
            flowOf(Response.Success(emptyList()))
        }
        return combine(
            scanRepository.scanBluetooth().flowOn(dispatchers.io).onStart { emit(Response.Success(emptyList())) },
            scanRepository.scanUsb().flowOn(dispatchers.io).onStart { emit(Response.Success(emptyList())) },
            serial,
        ) { bleResponse, usbResponse, serialResponse -> mergeDevices(bleResponse, usbResponse, serialResponse) }
    }

    private fun emptyHintTimer(): Flow<Boolean> = flow {
        emit(false)
        delay(EMPTY_HINT_DELAY_MILLIS)
        emit(true)
    }

    // --- Интенты: разрешения ---

    /** Повтор: заново запросить доступ / перепроверить адаптер и геолокацию. */
    fun onRetry() {
        intent { checkPermissionsExclusively() }
    }

    /** Открыть системные настройки Bluetooth (случай выключенного адаптера). */
    fun onOpenBluetoothSettings() {
        permissionsProvider.openBluetoothSettings()
    }

    /** Открыть системные настройки геолокации (случай выключенной службы). */
    fun onOpenLocationSettings() {
        permissionsProvider.openLocationSettings()
    }

    /**
     * Вернулись в приложение (из системных настроек, шторки на iOS, другого приложения): заново проверяем
     * разрешения, Bluetooth и геолокацию и запускаем поиск. Подключение и системный запрос разрешения не трогаем.
     */
    private suspend fun recheckOnForeground() {
        executeWithProceedLock {
            // Подключение и идущая проверка держат lock — сюда не попадут. Остаётся шаг Connecting после
            // успешного подключения (экран уже ушёл во flow устройства): поиск не перезапускаем.
            if (currentState.step is ScanStep.Connecting) return@executeWithProceedLock
            checkPermissionAndProceed()
            reload()
        }
    }

    /** Открыть системные настройки приложения (случай «отклонено навсегда»). */
    fun onOpenAppSettings() {
        permissionsProvider.openAppSettings()
    }

    /**
     * Проверка разрешений из любого триггера (старт, «Повторить», возврат в приложение) — под общим lock:
     * параллельная вторая проверка показала бы второй системный запрос, дубль пропускается.
     */
    private suspend fun checkPermissionsExclusively() {
        executeWithProceedLock { checkPermissionAndProceed() }
    }

    private suspend fun checkPermissionAndProceed() {
        if (checkPermission()) proceedAfterPermission()
    }

    private suspend fun checkPermission(): Boolean {
        reduce { copy(step = ScanStep.PermissionCheck) }
        return when (val result = permissionsProvider.requestBlePermissions()) {
            BlePermissionResult.Granted -> true

            is BlePermissionResult.Denied -> {
                reduce { copy(step = ScanStep.Denied(canOpenSettings = result.canOpenSettings)) }
                false
            }
        }
    }

    // Preflight после выдачи разрешений: BLE требует включённого адаптера, а на Android < 12 —
    // ещё и включённой службы геолокации (иначе скан молча ничего не находит).
    private fun proceedAfterPermission() {
        val bluetoothNeeded = currentState.requirements.contains(DeviceAccessRequirement.BLUETOOTH)
        val step = when {
            bluetoothNeeded && !permissionsProvider.isBluetoothEnabled() -> ScanStep.AdapterOff
            bluetoothNeeded && !permissionsProvider.isLocationEnabled() -> ScanStep.LocationOff
            else -> ScanStep.Scanning
        }
        reduce { copy(step = step) }
    }

    // --- Интенты: выбор и подключение ---

    /** Выбор устройства: в split — выделяем (карточка справа), иначе — подключаемся сразу. */
    fun onDeviceSelected(item: DeviceUiItem, isSplit: Boolean) {
        if (item.restricted) return
        if (isSplit) {
            reduce { copy(selectedId = item.id) }
        } else {
            onConnect(item)
        }
    }

    /** «Подключить» в split-раскладке для выбранного устройства. */
    fun onConnectSelected() {
        val selected = (currentState.devices as? LCEState.Content)?.content
            ?.firstOrNull { it.id == currentState.selectedId }
            ?: return
        onConnect(selected)
    }

    /** «Назад» (системное и в заголовке): во время подключения — отмена, иначе — выход. */
    fun onBack() {
        if (currentState.step is ScanStep.Connecting) {
            onCancelConnecting()
        } else {
            postSideEffect(ScanDeviceEffect.Close)
        }
    }

    /** Отмена подключения — назад к списку. */
    fun onCancelConnecting() {
        cancelLaunched(CONNECT_KEY)
        intent {
            // Дожидаемся, пока отменённое подключение отпустит lock; новое «Подключить» до конца закрытия
            // пропускается. Сначала закрыть сессию, затем вернуться к поиску (он стартует заново).
            executeWithProceedLock(needSkipAnother = false) {
                deviceSession.close().join()
                reduce { copy(step = ScanStep.Scanning) }
            }
        }
    }

    fun onConnect(item: DeviceUiItem) {
        // Дубль (двойное нажатие) не отменяет идущую попытку; одновременную проверку разрешений отсекает lock.
        if (currentState.connectAttempt) return
        reduce { copy(connectAttempt = true) }
        launchReplacing(CONNECT_KEY) {
            try {
                connect(item)
            } finally {
                reduce { copy(connectAttempt = false) }
            }
        }
    }

    private suspend fun connect(item: DeviceUiItem) {
        executeWithProceedLock {
            // На шаге Connecting поиск остановлен и список пуст — режим блока берём из последнего найденного.
            val scanned = currentState.devices
            reduce { copy(step = ScanStep.Connecting(item)) }
            val connection = when (item) {
                is DeviceUiItem.Ble -> ConnectionTransport.BLE.takeIf { connectBle(item, scanned) }
                is DeviceUiItem.Usb -> ConnectionTransport.USB.takeIf { connectUsb(item) }
                is DeviceUiItem.Serial -> ConnectionTransport.SERIAL.takeIf { connectSerial(item) }
            }
            if (connection != null) {
                // Сессия создана: дальше устройством владеет flow работы с ним (interaction).
                deviceSession.attach(connection)
                postSideEffect(ScanDeviceEffect.SessionCreated)
            } else {
                deviceSession.close().join()
                reduce { copy(step = ScanStep.Scanning) }
            }
        }
    }

    /**
     * Сопряжение решает SDK: блок в режиме сопряжения — сам снимет старое сопряжение на телефоне и сопряжётся заново;
     * блок «забыл» телефон — снимет старое сопряжение и вернёт [DeviceConnectionState.PAIRING_REQUIRED].
     * До подключения отсекаем только блок, привязанный к другому телефону: с этого телефона это не исправить.
     */
    private suspend fun connectBle(item: DeviceUiItem.Ble, scanned: LCEState<List<DeviceUiItem>, ScanError>): Boolean {
        if (item.mode != ScannedDeviceMode.BOOT && item.isBoundToAnotherPhone()) {
            postSideEffect(ScanDeviceEffect.BoundToAnotherPhone)
            return false
        }
        val startedAt = TimeSource.Monotonic.markNow()
        repeat(BLE_CONNECT_ATTEMPTS) { attempt ->
            logger.i { "connectBle id=${item.id} attempt=${attempt + 1} start" }
            val result = connectionRepository.connectBle(
                item.id,
                CONNECT_TIMEOUT_MILLIS,
            ).flowOn(dispatchers.io).awaitTerminal()
            logger.i {
                "connectBle id=${item.id} attempt=${attempt + 1} result=$result " +
                    "total=${startedAt.elapsedNow().inWholeMilliseconds}ms"
            }
            if (result is Response.Success && result.value == DeviceConnectionState.PAIRING_REQUIRED) {
                // Старое сопряжение снято; если блок уже в режиме сопряжения — повторяем, SDK сопряжётся заново.
                if (attempt < BLE_CONNECT_ATTEMPTS - 1 &&
                    scanned.modeOf(item.id) == ScannedDeviceMode.PAIRING
                ) {
                    return@repeat
                }
                postSideEffect(ScanDeviceEffect.PairingRequired)
                return false
            }
            return result.isConnected()
        }
        return false
    }

    private fun LCEState<List<DeviceUiItem>, ScanError>.modeOf(id: String): ScannedDeviceMode? =
        ((this as? LCEState.Content)?.content?.firstOrNull { it.id == id } as? DeviceUiItem.Ble)?.mode

    private suspend fun connectUsb(item: DeviceUiItem.Usb): Boolean =
        connectionRepository.connectUsb(item.id).flowOn(dispatchers.io).awaitTerminal().isConnected()

    // На Android в подключение входит системный запрос разрешения USB — его покрывает общий тайм-аут ожидания.
    private suspend fun connectSerial(item: DeviceUiItem.Serial): Boolean =
        connectionRepository.connectSerial(item.id).flowOn(dispatchers.io).awaitTerminal().isConnected()

    private fun Response<DeviceConnectionState>?.isConnected(): Boolean {
        val connected = this is Response.Success && value == DeviceConnectionState.CONNECTED
        if (!connected) postSideEffect(ScanDeviceEffect.ConnectionFailed)
        return connected
    }

    // Общий предел ожидания: SDK переподключается по кругу, а USB может не дать финального статуса.
    // null (таймаут или поток завершился без финала) — неудача.
    private suspend fun Flow<Response<DeviceConnectionState>>.awaitTerminal(): Response<DeviceConnectionState>? =
        withTimeoutOrNull(CONNECT_FLOW_TIMEOUT_MILLIS) { firstOrNull { it.isTerminal() } }

    private fun Response<DeviceConnectionState>.isTerminal(): Boolean = this is Response.Error ||
        (this is Response.Success && value != DeviceConnectionState.CONNECTING)

    /** У блока есть контекст (привязка), а телефон с ним не сопряжён. iOS не сообщает о сопряжении — не определить. */
    private fun DeviceUiItem.Ble.isBoundToAnotherPhone(): Boolean = hasDeviceBond && hasPhoneBond == false

    private fun mergeDevices(
        bleResponse: Response<List<ScannedDevice.Ble>>,
        usbResponse: Response<List<ScannedDevice.Usb>>,
        serialResponse: Response<List<ScannedDevice.Serial>>,
    ): LCEState<List<DeviceUiItem>, ScanError> {
        // Ошибка одного транспорта не прячет устройства, найденные другим.
        val ble = (bleResponse as? Response.Success)?.value.orEmpty()
        val usb = (usbResponse as? Response.Success)?.value.orEmpty()
        val serial = (serialResponse as? Response.Success)?.value.orEmpty().connectableAdapters()
        val bleItems = ble.map { device ->
            device.toUiItem(restricted = alarmsOnly && device.kind != DeviceKind.ALARM)
        }
        val items = bleItems + usb.map(ScannedDevice::toUiItem) + serial.map { it.toUiItem() }
        if (items.isNotEmpty()) return LCEState.Content(items)
        val error = (bleResponse as? Response.Error) ?: (usbResponse as? Response.Error)
            ?: (serialResponse as? Response.Error)
        // Экран рисует Error как продолжающийся поиск.
        return if (error != null) LCEState.Error(ScanError.from(error.exception)) else LCEState.Loading
    }

    private companion object {
        const val CONNECT_KEY = "connect"
        const val CONNECT_TIMEOUT_MILLIS = 20_000L

        /** Повтор BLE-подключения после снятия старого сопряжения, если блок в режиме сопряжения. */
        const val BLE_CONNECT_ATTEMPTS = 2

        // Поиск внутри connect (до 8 с) + сопряжение с подтверждением (до ~30 с) + соединение (20 с).
        const val CONNECT_FLOW_TIMEOUT_MILLIS = 60_000L
        const val EMPTY_HINT_DELAY_MILLIS = 10_000L
    }
}
