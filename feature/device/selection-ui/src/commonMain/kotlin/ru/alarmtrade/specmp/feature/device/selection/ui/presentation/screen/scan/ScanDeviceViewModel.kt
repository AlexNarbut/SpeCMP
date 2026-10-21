package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.lifecycle.viewModelScope
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.permissions.common.DeniedAlwaysException
import ru.alarmtrade.permissions.common.DeniedException
import ru.alarmtrade.permissions.common.PermissionState
import ru.alarmtrade.permissions.common.RequestCanceledException
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.permissions.deviceAccessRequirements
import ru.alarmtrade.specmp.core.permissions.isBluetoothEnabled
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectedDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.toUiItem

/**
 * ViewModel экрана поиска устройства.
 *
 * Разрешения запрашиваются **самой VM** через инжектированный [PermissionsProvider]
 * (контроллер app-scoped, забинжен в корне приложения) — без промежуточного
 * rationale-экрана. Проверка запускается один раз при первой подписке на state
 * (init-источник `stateIn(Lazily)`, см. docs/patterns.md — не через `LaunchedEffect`).
 * Сканирование — реактивный источник (BLE + USB `Flow`), активный только на шаге
 * [ScanStep.Scanning].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanDeviceViewModel(
    scanRepository: DeviceScanRepository,
    private val connectionRepository: DeviceConnectionRepository,
    private val deviceSession: DeviceSession,
    private val permissionsProvider: PermissionsProvider,
    dispatchers: AppDispatchers
) : BaseViewModel() {

    private val requirements = deviceAccessRequirements()
    private val supportedTransports = scanRepository.supportedTransports()

    private val stepFlow = MutableStateFlow<ScanStep>(ScanStep.PermissionCheck)
    private val selectedIdFlow = MutableStateFlow<String?>(null)

    // Перезапуск поиска без смены шага (Scanning → Scanning при возврате в приложение):
    // StateFlow шага одинаковые значения склеивает, поэтому нужен отдельный счётчик.
    private val scanRestartsFlow = MutableStateFlow(0)

    private val _effect = sideEffectFlow<ScanDeviceEffect>()
    val effect: SharedFlow<ScanDeviceEffect> = _effect.asSharedFlow()

    // Только подключение (не отмена): по нему «Отмена» прерывает попытку. Пишется и читается на main.
    private var connectJob: Job? = null

    // Init-источник: один раз за жизнь VM (Lazily) запрашивает разрешения при первой
    // подписке на state и переводит на скан/adapterOff/denied. Не «работа из UI».
    private val permissionGate: StateFlow<Boolean> = flow {
        // Прошлая сессия могла не закрыться (или ещё закрывается после выхода из flow устройства):
        // подключённое устройство не рекламируется и не появится в поиске — дожидаемся закрытия.
        deviceSession.close().join()
        checkPermissionsExclusively()
        emit(true)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val devicesState: Flow<LCEState<List<DeviceUiItem>>> = combine(stepFlow, scanRestartsFlow) { step, _ ->
        step
    }
        .flatMapLatest { step ->
            if (step !is ScanStep.Scanning) {
                flowOf(LCEState.None())
            } else {
                combine(
                    scanRepository.scanBluetooth().onStart { emit(Response.Success(emptyList())) },
                    scanRepository.scanUsb().onStart { emit(Response.Success(emptyList())) }
                ) { bleResponse, usbResponse -> mergeDevices(bleResponse, usbResponse) }
            }
        }

    private val searchedLongEnough: Flow<Boolean> = stepFlow.flatMapLatest { step ->
        if (step is ScanStep.Scanning) {
            flow {
                emit(false)
                delay(EMPTY_HINT_DELAY_MILLIS)
                emit(true)
            }
        } else {
            flowOf(false)
        }
    }

    val state: StateFlow<ScanDeviceState> = combine(
        stepFlow,
        devicesState,
        selectedIdFlow,
        searchedLongEnough,
        permissionGate
    ) { step, devices, selectedId, searchedLong, _ ->
        ScanDeviceState(
            step = step,
            requirements = requirements,
            supportedTransports = supportedTransports,
            devices = devices,
            selectedId = selectedId,
            searchedLongEnough = searchedLong
        )
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ScanDeviceState())

    // --- Интенты: разрешения ---

    /** Повтор: заново запросить доступ / перепроверить адаптер и геолокацию. */
    fun onRetry() {
        safeLaunch { checkPermissionsExclusively() }
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
    override fun onAppForeground() {
        safeLaunch {
            executeWithProceedLock {
                // Подключение и идущая проверка держат lock — сюда не попадут. Остаётся шаг Connecting после
                // успешного подключения (экран уже ушёл во flow устройства): поиск не перезапускаем.
                if (stepFlow.value is ScanStep.Connecting) return@executeWithProceedLock
                checkPermissionAndProceed()
                scanRestartsFlow.update { it + 1 }
            }
        }
    }

    /** Открыть системные настройки приложения (случай «отклонено навсегда»). */
    fun openAppSettings() {
        permissionsProvider.getController().openAppSettings()
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
        stepFlow.update { ScanStep.PermissionCheck }
        val controller = permissionsProvider.getController()
        permissionsProvider.getBlePermission().forEach { permission ->
            try {
                if (controller.getPermissionState(permission) != PermissionState.Granted) {
                    controller.providePermission(permission)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (ignored: DeniedAlwaysException) {
                stepFlow.update { ScanStep.Denied(canOpenSettings = true) }
                return false
            } catch (ignored: DeniedException) {
                stepFlow.update { ScanStep.Denied(canOpenSettings = false) }
                return false
            } catch (ignored: RequestCanceledException) {
                stepFlow.update { ScanStep.Denied(canOpenSettings = false) }
                return false
            }
        }
        return true
    }

    // Preflight после выдачи разрешений: BLE требует включённого адаптера, а на Android < 12 —
    // ещё и включённой службы геолокации (иначе скан молча ничего не находит).
    private fun proceedAfterPermission() {
        val bluetoothNeeded = requirements.contains(DeviceAccessRequirement.BLUETOOTH)
        stepFlow.update {
            when {
                bluetoothNeeded && !isBluetoothEnabled() -> ScanStep.AdapterOff
                bluetoothNeeded && !permissionsProvider.isLocationEnabled() -> ScanStep.LocationOff
                else -> ScanStep.Scanning
            }
        }
    }

    // --- Интенты: выбор и подключение ---

    /** Выбор устройства: в split — выделяем (карточка справа), иначе — подключаемся сразу. */
    fun onDeviceSelected(item: DeviceUiItem, isSplit: Boolean) {
        if (isSplit) {
            selectedIdFlow.update { item.id }
        } else {
            connect(item)
        }
    }

    /** «Подключить» в split-раскладке для выбранного устройства. */
    fun onConnectSelected() {
        val selected = (state.value.devices as? LCEState.Content)?.content
            ?.firstOrNull { it.id == selectedIdFlow.value }
            ?: return
        connect(selected)
    }

    /** «Назад» (системное и в заголовке): во время подключения — отмена, иначе — выход. */
    fun onBack() {
        if (stepFlow.value is ScanStep.Connecting) {
            onCancelConnecting()
        } else {
            _effect.tryEmit(ScanDeviceEffect.Close)
        }
    }

    /** Отмена подключения — назад к списку. */
    fun onCancelConnecting() {
        connectJob?.cancel()
        safeLaunch {
            // Дожидаемся, пока отменённое подключение отпустит lock; новое «Подключить» до конца закрытия
            // пропускается. Сначала закрыть сессию, затем вернуться к поиску (он стартует заново).
            executeWithProceedLock(needSkipAnother = false) {
                deviceSession.close().join()
                stepFlow.update { ScanStep.Scanning }
            }
        }
    }

    fun connect(item: DeviceUiItem) {
        // Дубль (двойное нажатие) не отменяет идущую попытку; одновременную проверку разрешений отсекает lock.
        if (connectJob?.isActive == true) return
        connectJob = safeLaunch {
            executeWithProceedLock {
                stepFlow.update { ScanStep.Connecting(item) }
                val connected = when (item) {
                    is DeviceUiItem.Ble -> connectBle(item)
                    is DeviceUiItem.Usb -> connectUsb(item)
                }
                if (connected) {
                    // Сессия создана: дальше устройством владеет flow работы с ним (interaction).
                    val device = item.toConnectedDevice()
                    deviceSession.attach(device.transport)
                    _effect.tryEmit(ScanDeviceEffect.SessionCreated(device))
                } else {
                    deviceSession.close().join()
                    stepFlow.update { ScanStep.Scanning }
                }
            }
        }
    }

    /**
     * Сопряжение решает SDK: блок в режиме сопряжения — сам снимет старое сопряжение на телефоне и сопряжётся заново;
     * блок «забыл» телефон — снимет старое сопряжение и вернёт [DeviceConnectionState.PAIRING_REQUIRED].
     * До подключения отсекаем только блок, привязанный к другому телефону: с этого телефона это не исправить.
     */
    private suspend fun connectBle(item: DeviceUiItem.Ble): Boolean {
        if (item.mode != ScannedDeviceMode.BOOT && item.isBoundToAnotherPhone()) {
            _effect.tryEmit(ScanDeviceEffect.BoundToAnotherPhone)
            return false
        }
        val startedAt = TimeSource.Monotonic.markNow()
        repeat(BLE_CONNECT_ATTEMPTS) { attempt ->
            logger.i { "connectBle id=${item.id} attempt=${attempt + 1} start" }
            val result = connectionRepository.connectBle(item.id, CONNECT_TIMEOUT_MILLIS).awaitTerminal()
            logger.i {
                "connectBle id=${item.id} attempt=${attempt + 1} result=$result " +
                    "total=${startedAt.elapsedNow().inWholeMilliseconds}ms"
            }
            if (result is Response.Success && result.value == DeviceConnectionState.PAIRING_REQUIRED) {
                // Старое сопряжение снято; если блок уже в режиме сопряжения — повторяем, SDK сопряжётся заново.
                if (attempt < BLE_CONNECT_ATTEMPTS - 1 &&
                    scannedMode(item.id) == ScannedDeviceMode.PAIRING
                ) {
                    return@repeat
                }
                _effect.tryEmit(ScanDeviceEffect.PairingRequired)
                return false
            }
            return result.isConnected()
        }
        return false
    }

    /** Режим блока по последним данным поиска. */
    private fun scannedMode(id: String): ScannedDeviceMode? =
        ((state.value.devices as? LCEState.Content)?.content?.firstOrNull { it.id == id } as? DeviceUiItem.Ble)?.mode

    private suspend fun connectUsb(item: DeviceUiItem.Usb): Boolean =
        connectionRepository.connectUsb(item.id).awaitTerminal().isConnected()

    private fun Response<DeviceConnectionState>?.isConnected(): Boolean {
        val connected = this is Response.Success && value == DeviceConnectionState.CONNECTED
        if (!connected) _effect.tryEmit(ScanDeviceEffect.ConnectionFailed)
        return connected
    }

    private fun DeviceUiItem.toConnectedDevice() = ConnectedDevice(
        id = id,
        name = name,
        transport = when (transport) {
            DeviceTransport.BLE -> ConnectionTransport.BLE
            DeviceTransport.USB -> ConnectionTransport.USB
        }
    )

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
        usbResponse: Response<List<ScannedDevice.Usb>>
    ): LCEState<List<DeviceUiItem>> {
        // Ошибка одного транспорта не прячет устройства, найденные другим.
        val ble = (bleResponse as? Response.Success)?.value.orEmpty()
        val usb = (usbResponse as? Response.Success)?.value.orEmpty()
        val items = ble.map(ScannedDevice::toUiItem) + usb.map(ScannedDevice::toUiItem)
        if (items.isNotEmpty()) return LCEState.Content(items)
        val error = (bleResponse as? Response.Error) ?: (usbResponse as? Response.Error)
        // Текст ошибки экран не показывает: Error рисуется как продолжающийся поиск.
        return if (error != null) LCEState.Error(error.message.orEmpty()) else LCEState.Loading()
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val CONNECT_TIMEOUT_MILLIS = 20_000L

        /** Повтор BLE-подключения после снятия старого сопряжения, если блок в режиме сопряжения. */
        const val BLE_CONNECT_ATTEMPTS = 2

        // Поиск внутри connect (до 8 с) + сопряжение с подтверждением (до ~30 с) + соединение (20 с).
        const val CONNECT_FLOW_TIMEOUT_MILLIS = 60_000L
        const val EMPTY_HINT_DELAY_MILLIS = 10_000L
    }
}
