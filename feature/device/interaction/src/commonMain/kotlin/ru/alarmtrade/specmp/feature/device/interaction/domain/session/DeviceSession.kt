package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import co.touchlab.kermit.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

/**
 * Единственный держатель состояния подключённого устройства (app-scoped). На каждом `READY` определяет режим
 * блока (с повтором): рабочий — [DeviceSessionState.Ready], загрузчик BLE-модуля — [DeviceSessionState.BluetoothBoot],
 * загрузчик основной программы — [DeviceSessionState.FirmwareBoot]; обрыв — [DeviceSessionState.Disconnected].
 * Вход в загрузчик и выход из него проходят через переподключение блока — режим определяется заново.
 * Горячий, пока есть подписчики; после ухода всех подписчиков значение сбрасывается.
 * Ожидаемая перезагрузка блока ([expectReboot]) — отдельное состояние [reboot]: по нему экраны отличают
 * намеренный обрыв связи (вход в загрузчик, возврат после прошивки) от потери связи.
 *
 * Владеет собственным скоупом (фоновая логика: переподключение, закрытие сессии), он переживает экраны.
 * Закрытие сессии и переподключение BLE сериализованы [sessionMutex]: закрытие не переплетается с
 * идущим `reconnect()`, а новое подключение (через [close]`.join()`) не стартует до конца закрытия.
 *
 * @param parentContext родитель скоупа — только для тестов (привязка к `backgroundScope`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceSession(
    private val sessionRepository: DeviceSessionRepository,
    private val connectionRepository: DeviceConnectionRepository,
    dispatchers: AppDispatchers,
    parentContext: CoroutineContext = EmptyCoroutineContext
) {

    private val logger = Logger.withTag(LOG_TAG)

    private val scope = CoroutineScope(
        parentContext + SupervisorJob(parentContext[Job]) + dispatchers.io +
            CoroutineExceptionHandler { _, throwable -> logger.e(throwable) { "Unhandled session exception" } }
    )

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

    private val expectation = MutableStateFlow<RebootExpectation?>(null)

    /** Перезагрузка блока, которую приложение вызвало и ждёт; `null` — не ждём (раздел «ожидаемая перезагрузка»). */
    val reboot: StateFlow<DeviceReboot?> = expectation
        .map { it?.kind }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /** Транспорт текущей сессии (для текстов «связь потеряна»); `null` — сессии нет. */
    val transport: StateFlow<ConnectionTransport?> = _transport.asStateFlow()

    val state: StateFlow<DeviceSessionState> = sessionRepository.linkState()
        .distinctUntilChanged()
        .transformLatest { link ->
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
                    logStage { "detectMode start" }
                    val mode = detectMode()
                    logStage { "detectMode result=${mode?.let { it::class.simpleName }}" }
                    if (mode != null) {
                        emit(mode)
                        onModeReached(mode)
                    } else {
                        // Связь «готова», а блок не отвечает (сессия SDK не поднялась) — рвём соединение:
                        // связь уйдёт в DISCONNECTED, и BLE-цикл переподключения подключится заново.
                        emit(DeviceSessionState.Disconnected)
                        sessionRepository.disconnect()
                    }
                }
            }
        }
        .onEach { logStage { "state=${it::class.simpleName}" } }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0),
            initialValue = DeviceSessionState.Connecting
        )

    /** Сессия создана поиском устройства — запомнить, по какому транспорту. */
    fun attach(transport: ConnectionTransport) {
        stageClock.update { TimeSource.Monotonic.markNow() }
        wasReady.update { false }
        databaseSynced.update { false }
        expectation.update { null }
        logStage { "attach transport=$transport" }
        _transport.update { transport }
    }

    /**
     * Закрыть сессию с устройством. Не ждёт: транспорт снимается сразу (переподключений больше не будет),
     * само закрытие идёт в скоупе сессии и переживает экран. Кто открывает новую сессию — дожидается
     * возвращённого [Job] (`join`), иначе отложенное закрытие оборвёт новую сессию.
     */
    fun close(): Job {
        logStage { "close requested" }
        _transport.update { null }
        expectation.update { null }
        return scope.launch {
            sessionMutex.withLock {
                val response = connectionRepository.closeSessions()
                if (response is Response.Error) logger.w(response.exception) { "Failed to close device session" }
                logStage { "close done" }
            }
        }
    }

    /** IMEI GSM-модема подключённого блока. */
    suspend fun readImei(): Response<String> = sessionRepository.readImei()

    /**
     * Переподключиться сейчас (кнопка «Переподключиться» при обрыве): одна попытка под тем же [sessionMutex], что и
     * цикл BLE и закрытие, с тем же тайм-аутом. Для BLE цикл переподключения и так идёт сам; для USB блок обычно
     * возвращается после повторного подключения кабеля. `null` — попытка не уложилась в тайм-аут или сессия закрыта.
     */
    suspend fun reconnectNow(): Response<Unit>? {
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

    /** Bluetooth-модуль блока (мануфактура и режим) — BLE; в рабочем режиме нужен для версии BLE-прошивки. */
    suspend fun readBluetoothModule(): Response<BluetoothModule> = sessionRepository.readBluetoothModule()

    /**
     * Выйти из загрузчика командой (перезагрузка [target] в рабочий режим). Одна команда, без повтора; кто вызывает —
     * сам ставит [expectReboot] ([DeviceReboot.ToMain]) до команды: блок может перезагрузиться раньше ответа.
     */
    suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = sessionRepository.exitBootMode(target)

    /** Сохранить PIN программирования для текущего блока (после [BootModeResult.WrongPin]). */
    suspend fun setServicePin(pin: ServicePin): Response<Unit> = sessionRepository.setServicePin(pin)

    /**
     * Перевести [target] в загрузчик. Ответ возвращается сразу после первой команды. Если блок её принял, через паузу
     * в скоупе сессии уходит повтор: при потерянном обрыве связи он форсирует переподключение (как в старом
     * приложении); после успешного перехода повтор не дойдёт до блока. Неверный PIN и ошибки не повторяем: каждая
     * лишняя попытка с неверным PIN продлевает блокировку ввода на блоке.
     */
    suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
        val response = sessionRepository.setBootMode(target)
        if (response is Response.Success && response.value == BootModeResult.Entered) {
            scope.launch {
                delay(BOOT_MODE_RETRY_DELAY_MILLIS)
                sessionRepository.setBootMode(target)
            }
        }
        return response
    }

    /**
     * Ждём перезагрузку блока: до команды ([DeviceReboot.ToBoot]) или на стадии применения прошивки
     * ([DeviceReboot.ToMain]).
     * - [DeviceReboot.ToBoot] снимается, когда блок вернулся в загрузчике, по тайм-ауту, при [cancelReboot], [attach] и [close].
     * - [DeviceReboot.ToMain] держится, пока блок не вернулся в рабочий режим **и** пользователь не подтвердил
     *   ([acknowledgeReboot]); тайм-аут действует только до возврата блока.
     */
    fun expectReboot(kind: DeviceReboot) {
        val expected = RebootExpectation(kind)
        expectation.update { expected }
        scope.launch {
            delay(REBOOT_TIMEOUT_MILLIS)
            expectation.update { current -> if (current === expected && !current.reached) null else current }
        }
    }

    /** Команда не удалась, ожидаемой перезагрузки не будет. */
    fun cancelReboot() {
        expectation.update { null }
    }

    /** Пользователь увидел результат прошивки: [DeviceReboot.ToMain] снимается, как только блок в рабочем режиме. */
    fun acknowledgeReboot() {
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

                current.kind == DeviceReboot.ToBoot && mode.isBoot() -> null

                current.kind == DeviceReboot.ToMain && mode is DeviceSessionState.Ready ->
                    if (current.acknowledged) null else current.copy(reached = true)

                else -> current
            }
        }
    }

    private fun DeviceSessionState.isBoot(): Boolean =
        this is DeviceSessionState.BluetoothBoot || this is DeviceSessionState.FirmwareBoot

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
        if (!device.modelInDatabase && !databaseSynced.value) {
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

    private data class RebootExpectation(
        val kind: DeviceReboot,
        val acknowledged: Boolean = false,
        val reached: Boolean = false
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
    }
}
