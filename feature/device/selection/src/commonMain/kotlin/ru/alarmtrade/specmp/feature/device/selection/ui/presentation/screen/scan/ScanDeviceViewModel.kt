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
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.lifecycle.AppForegroundProvider
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
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
 * Разрешения запрашивает сама VM через [PermissionsProvider]; скан активен только на [ScanStep.Scanning], пока экран
 * подписан на state. COM-порт подключается как USB.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanDeviceViewModel(
    private val scanRepository: DeviceScanRepository,
    private val connectionRepository: DeviceConnectionRepository,
    private val deviceSession: DeviceSession,
    private val permissionsProvider: PermissionsProvider,
    private val foregroundProvider: AppForegroundProvider,
    private val dispatchers: AppDispatchers,
    supportedTransports: Set<DeviceTransport>,
    serialEnabled: Boolean = false,
    private val alarmsOnly: Boolean = false,
) : BaseViewModel<ScanDeviceState, ScanDeviceEffect>(
    ScanDeviceState(
        requirements = permissionsProvider.deviceAccessRequirements(),
        supportedTransports = supportedTransports
            .filter { it != DeviceTransport.SERIAL || serialEnabled }
            .toSet(),
    ),
) {

    override suspend fun IntentScope.onStart() {
        // Поиск перезапускается и без смены шага (Scanning → Scanning при возврате в приложение) — через reload().
        select { step }
            .flatMapLatest { step -> if (step is ScanStep.Scanning) scanDevices() else flowOf(LCEState.None) }
            .flowOn(dispatchers.default)
            .collectIntoState { devices -> copy(devices = devices) }
        // Подсказка «пусто» зависит только от шага: reload() поиска её не сбрасывает.
        intent {
            repeatOnSubscription {
                select { step }
                    .flatMapLatest { step -> if (step is ScanStep.Scanning) emptyHintTimer() else flowOf(false) }
                    .collect { searchedLong -> reduce { copy(searchedLongEnough = searchedLong) } }
            }
        }
        // Возврат в приложение отслеживается всю жизнь VM, а не только пока экран подписан на state.
        intent { foregroundProvider.foregroundReturns.collect { recheckOnForeground() } }
        // Прошлая сессия могла не закрыться (или ещё закрывается после выхода из flow устройства):
        // подключённое устройство не рекламируется и не появится в поиске — дожидаемся закрытия.
        closeSession()
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

    fun onRetry() {
        intent { checkPermissionsExclusively() }
    }

    fun onOpenBluetoothSettings() {
        permissionsProvider.openBluetoothSettings()
    }

    fun onOpenLocationSettings() {
        permissionsProvider.openLocationSettings()
    }

    /** Подключение и системный запрос разрешения не трогаем. */
    private suspend fun recheckOnForeground() = subIntent {
        executeWithProceedLock {
            // Подключение и идущая проверка держат lock — сюда не попадут. Остаётся шаг Connecting после
            // успешного подключения (экран уже ушёл во flow устройства): поиск не перезапускаем.
            if (currentState.step is ScanStep.Connecting) return@executeWithProceedLock
            checkPermissionAndProceed()
            reload()
        }
    }

    fun onOpenAppSettings() {
        permissionsProvider.openAppSettings()
    }

    /** Под общим lock: параллельная вторая проверка показала бы второй системный запрос — дубль пропускается. */
    private suspend fun checkPermissionsExclusively() = subIntent {
        executeWithProceedLock { checkPermissionAndProceed() }
    }

    private suspend fun checkPermissionAndProceed() = subIntent {
        if (checkPermission()) proceedAfterPermission()
    }

    private suspend fun checkPermission(): Boolean = subIntent {
        reduce { copy(step = ScanStep.PermissionCheck) }
        return@subIntent when (val result = permissionsProvider.requestBlePermissions()) {
            PermissionResult.Granted -> true

            is PermissionResult.Denied -> {
                reduce { copy(step = ScanStep.Denied(canOpenSettings = result.canOpenSettings)) }
                false
            }
        }
    }

    // Preflight после выдачи разрешений: BLE требует включённого адаптера, а на Android < 12 —
    // ещё и включённой службы геолокации (иначе скан молча ничего не находит).
    private suspend fun proceedAfterPermission() = subIntent {
        val bluetoothNeeded = currentState.requirements.contains(DeviceAccessRequirement.BLUETOOTH)
        val step = when {
            bluetoothNeeded && !permissionsProvider.isBluetoothEnabled() -> ScanStep.AdapterOff
            bluetoothNeeded && !permissionsProvider.isLocationEnabled() -> ScanStep.LocationOff
            else -> ScanStep.Scanning
        }
        reduce { copy(step = step) }
    }

    fun onDeviceSelected(item: DeviceUiItem, isSplit: Boolean) {
        if (item.restricted) return
        if (isSplit) {
            intent { reduce { copy(selectedId = item.id) } }
        } else {
            onConnect(item)
        }
    }

    fun onConnectSelected() {
        val selected = (currentState.devices as? LCEState.Content)?.content
            ?.firstOrNull { it.id == currentState.selectedId }
            ?: return
        onConnect(selected)
    }

    fun onBack() {
        if (currentState.step is ScanStep.Connecting) {
            onCancelConnecting()
        } else {
            intent { postSideEffect(ScanDeviceEffect.Close) }
        }
    }

    fun onCancelConnecting() {
        cancelLaunched(CONNECT_KEY)
        intent {
            // Дожидаемся, пока отменённое подключение отпустит lock; новое «Подключить» до конца закрытия
            // пропускается. Сначала закрыть сессию, затем вернуться к поиску (он стартует заново).
            executeWithProceedLock(needSkipAnother = false) {
                closeSession()
                reduce { copy(step = ScanStep.Scanning) }
            }
        }
    }

    fun onConnect(item: DeviceUiItem) {
        // Дубль (двойное нажатие) не отменяет идущую попытку; одновременную проверку разрешений отсекает lock.
        intent {
            if (currentState.connectAttempt) return@intent
            reduce { copy(connectAttempt = true) }
            launchReplacing(CONNECT_KEY) {
                try {
                    connect(item)
                } finally {
                    reduce { copy(connectAttempt = false) }
                }
            }
        }
    }

    private suspend fun connect(item: DeviceUiItem) = subIntent {
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
                deviceSession.attach(connection)
                postSideEffect(ScanDeviceEffect.SessionCreated(connection))
            } else {
                closeSession()
                reduce { copy(step = ScanStep.Scanning) }
            }
        }
    }

    /**
     * Сопряжение решает SDK: блок в режиме сопряжения переспряжётся сам, «забыл» телефон —
     * [DeviceConnectionState.PAIRING_REQUIRED]. До подключения отсекаем только блок, привязанный к другому телефону.
     */
    private suspend fun connectBle(item: DeviceUiItem.Ble, scanned: LCEState<List<DeviceUiItem>, ScanError>): Boolean =
        subIntent {
            if (item.mode != ScannedDeviceMode.BOOT && item.isBoundToAnotherPhone()) {
                postSideEffect(ScanDeviceEffect.BoundToAnotherPhone)
                return@subIntent false
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
                    return@subIntent false
                }
                return@subIntent isConnected(result)
            }
            return@subIntent false
        }

    private fun LCEState<List<DeviceUiItem>, ScanError>.modeOf(id: String): ScannedDeviceMode? =
        ((this as? LCEState.Content)?.content?.firstOrNull { it.id == id } as? DeviceUiItem.Ble)?.mode

    private suspend fun connectUsb(item: DeviceUiItem.Usb): Boolean =
        isConnected(connectionRepository.connectUsb(item.id).flowOn(dispatchers.io).awaitTerminal())

    // На Android в подключение входит системный запрос разрешения USB — его покрывает общий тайм-аут ожидания.
    private suspend fun connectSerial(item: DeviceUiItem.Serial): Boolean =
        isConnected(connectionRepository.connectSerial(item.id).flowOn(dispatchers.io).awaitTerminal())

    private suspend fun isConnected(response: Response<DeviceConnectionState>?): Boolean = subIntent {
        val connected = response is Response.Success && response.value == DeviceConnectionState.CONNECTED
        if (!connected) postSideEffect(ScanDeviceEffect.ConnectionFailed)
        return@subIntent connected
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

    private suspend fun closeSession() {
        deviceSession.closeAndAwait().onError { logger.w(it) { "Failed to close device session" } }
    }

    private companion object {
        const val CONNECT_KEY = "connect"
        const val CONNECT_TIMEOUT_MILLIS = 20_000L

        const val BLE_CONNECT_ATTEMPTS = 2

        // Поиск внутри connect (до 8 с) + сопряжение с подтверждением (до ~30 с) + соединение (20 с).
        const val CONNECT_FLOW_TIMEOUT_MILLIS = 60_000L
        const val EMPTY_HINT_DELAY_MILLIS = 10_000L
    }
}
