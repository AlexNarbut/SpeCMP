package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.lifecycle.viewModelScope
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
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.permissions.deviceAccessRequirements
import ru.alarmtrade.specmp.core.permissions.isBluetoothEnabled
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.AlarmDeviceRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.domain.repository.DeviceScanRepository
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.toUiItem
import kotlin.coroutines.cancellation.CancellationException

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
    private val scanRepository: DeviceScanRepository,
    private val connectionRepository: DeviceConnectionRepository,
    private val alarmDeviceRepository: AlarmDeviceRepository,
    private val permissionsProvider: PermissionsProvider,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

    private val requirements = deviceAccessRequirements()
    private val supportedTransports = scanRepository.supportedTransports()

    private val _step = MutableStateFlow<ScanStep>(ScanStep.PermissionCheck)
    private val _selectedId = MutableStateFlow<String?>(null)

    private val _effect = sideEffectFlow<ScanDeviceEffect>()
    val effect: SharedFlow<ScanDeviceEffect> = _effect.asSharedFlow()

    private var connectJob: Job? = null

    // Init-источник: один раз за жизнь VM (Lazily) запрашивает разрешения при первой
    // подписке на state и переводит на скан/adapterOff/denied. Не «работа из UI».
    private val permissionGate: StateFlow<Boolean> = flow {
        // Прошлая сессия могла закрыться неудачно: подключённое устройство не рекламируется
        // и не появится в поиске.
        connectionRepository.closeSessions()
        checkPermissionAndProceed()
        emit(true)
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val devicesState: Flow<LCEState<List<DeviceUiItem>>> = _step
        .flatMapLatest { step ->
            if (step !is ScanStep.Scanning) {
                flowOf(LCEState.None())
            } else {
                combine(
                    scanRepository.scanBluetooth().onStart { emit(Response.Success(emptyList())) },
                    scanRepository.scanUsb().onStart { emit(Response.Success(emptyList())) },
                ) { bleResponse, usbResponse -> mergeDevices(bleResponse, usbResponse) }
            }
        }

    private val searchedLongEnough: Flow<Boolean> = _step.flatMapLatest { step ->
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
        _step,
        devicesState,
        _selectedId,
        searchedLongEnough,
        permissionGate,
    ) { step, devices, selectedId, searchedLong, _ ->
        ScanDeviceState(
            step = step,
            requirements = requirements,
            supportedTransports = supportedTransports,
            devices = devices,
            selectedId = selectedId,
            searchedLongEnough = searchedLong,
        )
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ScanDeviceState())

    // --- Интенты: разрешения ---

    /** Повтор: заново запросить доступ / перепроверить адаптер и геолокацию. */
    fun onRetry() {
        safeLaunch { checkPermissionAndProceed() }
    }

    /** Открыть системные настройки Bluetooth (случай выключенного адаптера). */
    fun onOpenBluetoothSettings() {
        permissionsProvider.openBluetoothSettings()
    }

    /** Открыть системные настройки геолокации (случай выключенной службы). */
    fun onOpenLocationSettings() {
        permissionsProvider.openLocationSettings()
    }

    /** Открыть системные настройки приложения (случай «отклонено навсегда»). */
    fun openAppSettings() {
        permissionsProvider.getController().openAppSettings()
    }

    private suspend fun checkPermissionAndProceed() {
        if (checkPermission()) proceedAfterPermission()
    }

    private suspend fun checkPermission(): Boolean {
        _step.update { ScanStep.PermissionCheck }
        val controller = permissionsProvider.getController()
        permissionsProvider.getBlePermission().forEach { permission ->
            try {
                if (controller.getPermissionState(permission) != PermissionState.Granted) {
                    controller.providePermission(permission)
                }
            }catch (e : CancellationException){
                throw e
            }
            catch (e: DeniedAlwaysException) {
                _step.update { ScanStep.Denied(canOpenSettings = true) }
                return false
            } catch (e: DeniedException) {
                _step.update { ScanStep.Denied(canOpenSettings = false) }
                return false
            } catch (e: RequestCanceledException) {
                _step.update { ScanStep.Denied(canOpenSettings = false) }
                return false
            }
        }
        return true
    }

    // Preflight после выдачи разрешений: BLE требует включённого адаптера, а на Android < 12 —
    // ещё и включённой службы геолокации (иначе скан молча ничего не находит).
    private fun proceedAfterPermission() {
        val bluetoothNeeded = requirements.contains(DeviceAccessRequirement.BLUETOOTH)
        _step.update {
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
            _selectedId.update { item.id }
        } else {
            connect(item)
        }
    }

    /** «Подключить» в split-раскладке для выбранного устройства. */
    fun onConnectSelected() {
        val selected = (state.value.devices as? LCEState.Content)?.content
            ?.firstOrNull { it.id == _selectedId.value }
            ?: return
        connect(selected)
    }

    /**
     * «Назад» (системное и в заголовке). Во время подключения игнорируется: отмена подписки
     * на connect SDK после установки GATT оставляет блок подключённым (не закрывает GATT),
     * и он пропадает из поиска до перезапуска приложения. Попытка завершается сама (или по таймауту).
     */
    fun onBack() {
        if (_step.value is ScanStep.Connecting) return
        _effect.tryEmit(ScanDeviceEffect.Close)
    }

    fun connect(item: DeviceUiItem) {
        connectJob?.cancel()
        connectJob = safeLaunch {
            executeWithProceedLock {
                _step.update { ScanStep.Connecting(item) }
                val result = when (item) {
                    is DeviceUiItem.Ble -> connectBle(item)
                    is DeviceUiItem.Usb -> connectUsb(item)
                }
                if (result != null) {
                    _effect.tryEmit(ScanDeviceEffect.Connected(result))
                } else {
                    connectionRepository.closeSessions()
                    _step.update { ScanStep.Scanning }
                }
            }
        }
    }

    private suspend fun connectBle(item: DeviceUiItem.Ble): DeviceConnectionResult? {
        if (item.mode != ScannedDeviceMode.BOOT) {
            val bondConflict = bondConflictOf(item)
            if (bondConflict != null) {
                _effect.tryEmit(ScanDeviceEffect.BondConflict(bondConflict))
                return null
            }
        }
        val terminal = connectionRepository.connectBle(item.id, CONNECT_TIMEOUT_MILLIS).awaitTerminal()
        return terminal.toResultOrNull(item.mode)
    }

    private suspend fun connectUsb(item: DeviceUiItem.Usb): DeviceConnectionResult? {
        val terminal = connectionRepository.connectUsb(item.id).awaitTerminal()
        return terminal.toResultOrNull(ScannedDeviceMode.BASE)
    }

    private suspend fun Response<DeviceConnectionState>?.toResultOrNull(
        mode: ScannedDeviceMode,
    ): DeviceConnectionResult? {
        val connected = this is Response.Success && value == DeviceConnectionState.CONNECTED
        if (!connected) {
            _effect.tryEmit(ScanDeviceEffect.ConnectionFailed)
            return null
        }
        return readConnectionResult(mode)
    }

    private suspend fun readConnectionResult(mode: ScannedDeviceMode): DeviceConnectionResult? {
        val response: Response<DeviceConnectionResult> = if (mode == ScannedDeviceMode.BOOT) {
            alarmDeviceRepository.getBootData().map { DeviceConnectionResult.Boot(it) }
        } else {
            alarmDeviceRepository.getAlarmDeviceInformation().map { DeviceConnectionResult.Working(it) }
        }
        return when (response) {
            is Response.Success -> response.value
            is Response.Error -> {
                _effect.tryEmit(ScanDeviceEffect.ConnectionFailed)
                null
            }
        }
    }

    // Общий предел ожидания: SDK переподключается по кругу, а USB может не дать финального статуса.
    // null (таймаут или поток завершился без финала) — неудача.
    private suspend fun Flow<Response<DeviceConnectionState>>.awaitTerminal(): Response<DeviceConnectionState>? =
        withTimeoutOrNull(CONNECT_FLOW_TIMEOUT_MILLIS) { firstOrNull { it.isTerminal() } }

    private fun Response<DeviceConnectionState>.isTerminal(): Boolean =
        this is Response.Error ||
            (this is Response.Success && value != DeviceConnectionState.CONNECTING)

    private fun bondConflictOf(item: DeviceUiItem.Ble): BondConflictKind? {
        // Платформа не сообщает о сопряжении с телефоном (iOS) — конфликт не определить.
        val hasPhoneBond = item.hasPhoneBond ?: return null
        return when {
            item.hasDeviceBond == hasPhoneBond -> null
            hasPhoneBond -> BondConflictKind.REMOVE_FROM_PHONE
            else -> BondConflictKind.REMOVE_FROM_DEVICE
        }
    }

    private fun mergeDevices(
        bleResponse: Response<List<ScannedDevice.Ble>>,
        usbResponse: Response<List<ScannedDevice.Usb>>,
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
        // Поиск внутри connect (до 8 с) + сопряжение с подтверждением (до ~30 с) + соединение (20 с).
        const val CONNECT_FLOW_TIMEOUT_MILLIS = 60_000L
        const val EMPTY_HINT_DELAY_MILLIS = 10_000L
    }
}
