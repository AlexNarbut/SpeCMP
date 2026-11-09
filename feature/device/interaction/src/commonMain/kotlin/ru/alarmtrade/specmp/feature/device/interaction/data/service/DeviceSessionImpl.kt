package ru.alarmtrade.specmp.feature.device.interaction.data.service

import co.touchlab.kermit.Logger
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/**
 * Закрытие сессии и переподключение BLE сериализованы [sessionMutex]: закрытие не переплетается с идущим
 * `reconnect()`, а новое подключение (через [closeAndAwait]) не стартует до конца закрытия.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceSessionImpl(
    private val sessionRepository: DeviceSessionRepository,
    private val connectionRepository: DeviceConnectionRepository,
    private val scope: CoroutineScope,
) : DeviceSession {

    private val logger = Logger.withTag(LOG_TAG)

    private val sessionMutex = Mutex()

    // Начало отсчёта для таймстемпов этапов в логах: новая сессия (attach) обнуляет.
    private val stageClock = MutableStateFlow(TimeSource.Monotonic.markNow())

    // Связь по BLE уже была готова в этой сессии: дальше потеря связи остаётся «Disconnected» на всё переподключение
    // (попытки SDK и чтение режима не подменяют её на «Connecting»). Новая сессия (attach) сбрасывает.
    private val wasReady = MutableStateFlow(false)

    // Базу устройств в этой сессии уже синхронизировали из-за неизвестной модели — второй раз не пытаемся
    // (без сети синхронизация не поможет, а переподключения не должны каждый раз её запускать). attach сбрасывает.
    private val databaseSynced = MutableStateFlow(false)

    private val _transport = MutableStateFlow<ConnectionTransport?>(null)

    // Идёт передача прошивки — режим (загрузчик), в котором она идёт. На переподключении BLE режим не перечитываем:
    // блок в загрузчике между соединениями не перезагружается, а SDK занят передачей (чтение блока ждало бы протокол
    // и рвало бы связь по тайм-ауту) и сам продолжает её после переподключения. attach/close сбрасывают.
    private val transferMode = MutableStateFlow<DeviceSessionState?>(null)

    private val expectation = MutableStateFlow<RebootExpectation?>(null)

    // Последнее состояние связи — запрос [refreshMode] обслуживается только на готовой связи.
    private val lastLink = MutableStateFlow<DeviceLinkState?>(null)

    private val modeRefreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val started = MutableStateFlow(false)

    private val _reboot = MutableStateFlow<DeviceReboot?>(null)

    override val reboot: StateFlow<DeviceReboot?> = _reboot.asStateFlow()

    override val transport: StateFlow<ConnectionTransport?> = _transport.asStateFlow()

    override val state: StateFlow<DeviceSessionState> = merge(
        sessionRepository.linkState()
            .map { it.getValueIfSuccess() ?: DeviceLinkState.DISCONNECTED }
            .distinctUntilChanged()
            .map { SessionEvent.Link(it) },
        modeRefreshes.map { SessionEvent.RefreshMode },
    )
        .transformLatest { event ->
            if (event == SessionEvent.RefreshMode) {
                if (lastLink.value != DeviceLinkState.READY) return@transformLatest
                // Тихо: экран остаётся на месте; блок не ответил — прежнее состояние (связь при этом не трогаем).
                logStage { "refreshMode start" }
                val mode = detectMode()
                logStage { "refreshMode result=${mode?.let { it::class.simpleName }}" }
                if (mode != null) {
                    emit(mode)
                    onModeReached(mode)
                }
                return@transformLatest
            }
            val link = (event as SessionEvent.Link).state
            lastLink.update { link }
            logStage { "link=$link" }
            when (link) {
                DeviceLinkState.CONNECTING -> if (!isBleReconnect()) emit(DeviceSessionState.Connecting)

                DeviceLinkState.DISCONNECTED -> {
                    emit(DeviceSessionState.Disconnected)
                    reconnectWhileBle()
                }

                DeviceLinkState.READY -> {
                    val reconnect = isBleReconnect()
                    wasReady.update { true }
                    if (!reconnect) emit(DeviceSessionState.Connecting)
                    val held = transferMode.value?.takeIf { reconnect }
                    val mode = if (held != null) {
                        logStage { "detectMode skipped: firmware transfer" }
                        held
                    } else {
                        logStage { "detectMode start" }
                        detectMode()
                    }
                    logStage { "detectMode result=${mode?.let { it::class.simpleName }}" }
                    if (mode != null) {
                        emit(mode)
                        onModeReached(mode)
                    } else if (_transport.value == ConnectionTransport.SERIAL) {
                        // COM: порт открыт, а блок молчит (выключен, ещё не в загрузчике). Порт не рвём — serial-сессия
                        // SDK после disconnect сама его не откроет; ждём, пока блок ответит (перезагрузили по питанию).
                        emit(DeviceSessionState.Disconnected)
                        val answered = awaitSerialBoard()
                        emit(answered)
                        onModeReached(answered)
                    } else {
                        // Связь «готова», а блок не отвечает (сессия SDK не поднялась) — рвём соединение:
                        // связь уйдёт в DISCONNECTED, и BLE-цикл переподключения подключится заново.
                        emit(DeviceSessionState.Disconnected)
                        sessionRepository.disconnect().onError { logger.w(it) { "Failed to drop a silent link" } }
                    }
                }
            }
        }
        .onEach { logStage { "state=${it::class.simpleName}" } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0),
            initialValue = DeviceSessionState.Connecting,
        )

    override fun start() {
        if (!started.compareAndSet(expect = false, update = true)) return
        expectation.onEach { expected -> _reboot.update { expected?.kind } }.launchIn(scope)
    }

    override fun attach(transport: ConnectionTransport) {
        stageClock.update { TimeSource.Monotonic.markNow() }
        wasReady.update { false }
        databaseSynced.update { false }
        expectation.update { null }
        transferMode.update { null }
        logStage { "attach transport=$transport" }
        _transport.update { transport }
    }

    override fun close() {
        val closing = startClosing()
        scope.launch { closing.await().onError { logger.w(it) { "Failed to close device session" } } }
    }

    override suspend fun closeAndAwait(): Response<Unit> = startClosing().await()

    private fun startClosing(): Deferred<Response<Unit>> {
        logStage { "close requested" }
        _transport.update { null }
        expectation.update { null }
        transferMode.update { null }
        return scope.async {
            sessionMutex.withLock {
                connectionRepository.closeSessions().also { logStage { "close done" } }
            }
        }
    }

    override fun refreshMode() {
        modeRefreshes.tryEmit(Unit)
    }

    override suspend fun readImei(): Response<String> = sessionRepository.readImei()

    override suspend fun reconnectNow(): Response<Unit>? {
        if (_transport.value == null) return null
        return sessionMutex.withLock {
            if (_transport.value == null) {
                null
            } else {
                withTimeoutOrNull(RECONNECT_TIMEOUT_MILLIS) {
                    sessionRepository.reconnect()
                }
            }
        }
    }

    override suspend fun readBluetoothModule(): Response<BluetoothModule> = sessionRepository.readBluetoothModule()

    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = sessionRepository.exitBootMode(target)

    override suspend fun setServicePin(pin: ServicePin): Response<Unit> = sessionRepository.setServicePin(pin)

    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
        val response = sessionRepository.setBootMode(target)
        if (response is Response.Success && response.value == BootModeResult.Entered) {
            scope.launch {
                delay(BOOT_MODE_RETRY_DELAY_MILLIS)
                sessionRepository.setBootMode(target).onError {
                    logger.d(it) { "Boot mode retry did not reach the board" }
                }
            }
        }
        return response
    }

    override fun expectReboot(kind: DeviceReboot) {
        val expected = RebootExpectation(kind)
        expectation.update { expected }
        scope.launch {
            delay(REBOOT_TIMEOUT_MILLIS)
            expectation.update { current -> if (current === expected && !current.reached) null else current }
        }
    }

    override fun beginFirmwareTransfer() {
        // Уже держим — не трогаем: прогресс может прийти, когда сессия уже увидела обрыв.
        transferMode.update { it ?: state.value.takeIf { current -> current.isBoot } }
    }

    override fun endFirmwareTransfer() {
        transferMode.update { null }
    }

    override fun cancelReboot() {
        expectation.update { null }
    }

    override fun acknowledgeReboot() {
        expectation.update { current ->
            when {
                current == null || current.kind != DeviceReboot.ToMain -> current
                current.reached -> null
                else -> current.copy(acknowledged = true)
            }
        }
    }

    /** Блок вернулся в режим [mode]: снять ожидание, если оно этим исполнено. */
    private fun onModeReached(mode: DeviceSessionState) {
        expectation.update { current ->
            when {
                current == null -> null

                current.kind == DeviceReboot.ToBoot && mode.isBoot -> null

                current.kind == DeviceReboot.Restart && mode is DeviceSessionState.Ready -> null

                current.kind == DeviceReboot.ToMain && mode is DeviceSessionState.Ready ->
                    if (current.acknowledged) null else current.copy(reached = true)

                else -> current
            }
        }
    }

    /**
     * BLE: SDK после обрыва (блок выключен или далеко, выключили Bluetooth) сам не переподключается — повторяем
     * подключение, пока связь не вернётся. Цикл отменяется сменой состояния связи (`transformLatest`) и отпиской;
     * после [close] транспорта нет — повторов тоже нет. USB переподключает SDK при повторном подключении кабеля.
     * Проверка транспорта и `reconnect()` — под [sessionMutex], чтобы не пересечься с закрытием; тайм-аут не даёт
     * зависшему подключению держать закрытие.
     */
    private suspend fun reconnectWhileBle() {
        var attempt = 0
        while (_transport.value == ConnectionTransport.BLE) {
            delay(RECONNECT_INTERVAL_MILLIS)
            sessionMutex.withLock {
                if (_transport.value == ConnectionTransport.BLE) {
                    attempt++
                    logStage { "reconnect #$attempt start" }
                    val response = withTimeoutOrNull(RECONNECT_TIMEOUT_MILLIS) { sessionRepository.reconnect() }
                    logStage {
                        "reconnect #$attempt end response=${response?.let { it::class.simpleName } ?: "timeout"}"
                    }
                }
            }
        }
        logStage { "reconnect loop stopped transport=${_transport.value}" }
    }

    /**
     * Режим блока на этом подключении (как в старом приложении). BLE: сначала Bluetooth-модуль (мануфактура + режим) —
     * в его загрузчике основная программа недоступна; USB — сразу основная программа. `null` — блок не ответил.
     */
    private suspend fun detectMode(): DeviceSessionState? {
        if (_transport.value == ConnectionTransport.BLE) {
            val module = readWithRetry { sessionRepository.readBluetoothModule() } ?: return null
            if (module.mode == BluetoothModuleMode.BOOT) return DeviceSessionState.BluetoothBoot(module.manufacturer)
        }
        var device = readWithRetry { sessionRepository.readDevice() } ?: return null
        // COM (приложение прошивки): возможности из базы не нужны — прошивка `.frm` идёт без них, а синхронизация
        // держала бы «Подключение…» десятки секунд (брелока в базе может не быть вовсе).
        if (!device.modelInDatabase && !databaseSynced.value && _transport.value != ConnectionTransport.SERIAL) {
            // Модели нет в базе устройств телефона (свежая установка, база не скачана или устарела): один раз
            // синхронизируем базу и перечитываем блок — иначе он показался бы «не поддерживается».
            databaseSynced.update { true }
            logStage { "device database sync: model ${device.systemName.trim()} not found" }
            // Сеть может зависнуть: без предела экран остался бы в «Подключение…». Таймаут — неуспех синхронизации.
            val synced = withTimeoutOrNull(SYNC_TIMEOUT_MILLIS) { sessionRepository.syncDeviceDatabase() }
            logStage { "device database sync ${if (synced is Response.Success) "ok" else "failed"}" }
            if (synced is Response.Success) device = readWithRetry { sessionRepository.readDevice() } ?: device
        }
        return when (device.mode) {
            DeviceOperationMode.MAIN -> DeviceSessionState.Ready(device)
            DeviceOperationMode.BOOT -> DeviceSessionState.FirmwareBoot(device)
        }
    }

    /**
     * COM: опрашивать блок, пока он не ответит, затем определить режим. Опрос короткий (одно чтение с малым
     * тайм-аутом), чтобы экран сменился вскоре после перезагрузки блока; цикл отменяется сменой связи
     * (`transformLatest`) и отпиской.
     */
    private suspend fun awaitSerialBoard(): DeviceSessionState {
        var attempt = 0
        while (true) {
            delay(SERIAL_POLL_INTERVAL_MILLIS)
            attempt++
            val answer = withTimeoutOrNull(SERIAL_POLL_TIMEOUT_MILLIS) { sessionRepository.readDevice() }
            logStage { "serial poll #$attempt response=${answer?.let { it::class.simpleName } ?: "timeout"}" }
            if (answer is Response.Success) detectMode()?.let { return it }
        }
    }

    private suspend fun <T> readWithRetry(read: suspend () -> Response<T>): T? {
        repeat(READ_ATTEMPTS) { attempt ->
            // Без тайм-аута зависший запрос (блок не ответил) держал бы «Подключение…» бесконечно.
            val response = withTimeoutOrNull(READ_TIMEOUT_MILLIS) { read() }
            logStage {
                "read attempt=${attempt + 1}/$READ_ATTEMPTS response=${response?.let {
                    it::class.simpleName
                } ?: "timeout"}"
            }
            if (response is Response.Success) return response.value
            if (attempt < READ_ATTEMPTS - 1) delay(READ_RETRY_DELAY_MILLIS)
        }
        return null
    }

    private fun isBleReconnect(): Boolean = wasReady.value && _transport.value == ConnectionTransport.BLE

    /** Лог этапа с временем от начала сессии: по нему замеряются подключение и переподключение. */
    private inline fun logStage(message: () -> String) {
        val elapsed = stageClock.value.elapsedNow().inWholeMilliseconds
        logger.i { "+${elapsed}ms ${message()}" }
    }

    private sealed interface SessionEvent {
        data class Link(val state: DeviceLinkState) : SessionEvent

        data object RefreshMode : SessionEvent
    }

    private data class RebootExpectation(
        val kind: DeviceReboot,
        val acknowledged: Boolean = false,
        val reached: Boolean = false,
    )

    private companion object {
        const val LOG_TAG = "DeviceSession"
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val READ_ATTEMPTS = 3
        const val READ_RETRY_DELAY_MILLIS = 500L
        const val READ_TIMEOUT_MILLIS = 10_000L
        const val SYNC_TIMEOUT_MILLIS = 30_000L
        const val BOOT_MODE_RETRY_DELAY_MILLIS = 3_000L
        const val REBOOT_TIMEOUT_MILLIS = 60_000L
        const val RECONNECT_INTERVAL_MILLIS = 1_000L
        const val RECONNECT_TIMEOUT_MILLIS = 15_000L
        const val SERIAL_POLL_INTERVAL_MILLIS = 1_000L
        const val SERIAL_POLL_TIMEOUT_MILLIS = 2_000L
    }
}
