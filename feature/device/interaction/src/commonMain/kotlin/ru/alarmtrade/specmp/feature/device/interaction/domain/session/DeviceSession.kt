package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

/**
 * Единственный держатель состояния подключённого устройства (app-scoped). На `READY` читает блок
 * (с повтором) и отдаёт [DeviceSessionState.Ready]; обрыв — [DeviceSessionState.Disconnected].
 * Горячий, пока есть подписчики; после ухода всех подписчиков значение сбрасывается.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceSession(
    private val sessionRepository: DeviceSessionRepository,
    private val connectionRepository: DeviceConnectionRepository,
    appScope: CoroutineScope,
) {

    private val _transport = MutableStateFlow<ConnectionTransport?>(null)

    /** Транспорт текущей сессии (для текстов «связь потеряна»); `null` — сессии нет. */
    val transport: StateFlow<ConnectionTransport?> = _transport.asStateFlow()

    val state: StateFlow<DeviceSessionState> = sessionRepository.linkState()
        .distinctUntilChanged()
        .transformLatest { link ->
            when (link) {
                DeviceLinkState.CONNECTING -> emit(DeviceSessionState.Connecting)
                DeviceLinkState.DISCONNECTED -> {
                    emit(DeviceSessionState.Disconnected)
                    reconnectWhileBle()
                }
                DeviceLinkState.READY -> {
                    emit(DeviceSessionState.Connecting)
                    val device = readWithRetry(sessionRepository)
                    if (device != null) {
                        emit(DeviceSessionState.Ready(device))
                    } else {
                        // Связь «готова», а блок не отвечает (сессия SDK не поднялась) — рвём соединение:
                        // связь уйдёт в DISCONNECTED, и BLE-цикл переподключения подключится заново.
                        emit(DeviceSessionState.Disconnected)
                        sessionRepository.disconnect()
                    }
                }
            }
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0),
            initialValue = DeviceSessionState.Connecting,
        )

    /** Сессия создана поиском устройства — запомнить, по какому транспорту. */
    fun attach(transport: ConnectionTransport) {
        _transport.value = transport
    }

    /** Закрыть сессию с устройством (выход из flow работы с устройством). */
    suspend fun close() {
        _transport.value = null
        connectionRepository.closeSessions()
    }

    /** IMEI GSM-модема подключённого блока. */
    suspend fun readImei(): Response<String> = sessionRepository.readImei()

    /**
     * Перевести блок в загрузчик. Команду повторяем через паузу: если обрыв связи не пришёл,
     * повтор форсирует переподключение (как в старом приложении); после успешного перехода повтор не дойдёт до блока.
     */
    suspend fun setBootMode(): Response<Unit> {
        val response = sessionRepository.setBootMode()
        delay(BOOT_MODE_RETRY_DELAY_MILLIS)
        sessionRepository.setBootMode()
        return response
    }

    /**
     * BLE: SDK после обрыва (блок выключен или далеко, выключили Bluetooth) сам не переподключается — повторяем
     * подключение, пока связь не вернётся. Цикл отменяется сменой состояния связи (`transformLatest`) и отпиской;
     * после [close] транспорта нет — повторов тоже нет. USB переподключает SDK при повторном подключении кабеля.
     */
    private suspend fun reconnectWhileBle() {
        while (_transport.value == ConnectionTransport.BLE) {
            delay(RECONNECT_INTERVAL_MILLIS)
            if (_transport.value == ConnectionTransport.BLE) sessionRepository.reconnect()
        }
    }

    private suspend fun readWithRetry(repository: DeviceSessionRepository): AlarmDevice? {
        repeat(READ_ATTEMPTS) { attempt ->
            // Без тайм-аута зависший запрос (блок не ответил) держал бы «Подключение…» бесконечно.
            val response = withTimeoutOrNull(READ_TIMEOUT_MILLIS) { repository.readDevice() }
            if (response is Response.Success) return response.value
            if (attempt < READ_ATTEMPTS - 1) delay(READ_RETRY_DELAY_MILLIS)
        }
        return null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val READ_ATTEMPTS = 3
        const val READ_RETRY_DELAY_MILLIS = 500L
        const val READ_TIMEOUT_MILLIS = 10_000L
        const val BOOT_MODE_RETRY_DELAY_MILLIS = 3_000L
        const val RECONNECT_INTERVAL_MILLIS = 1_000L
    }
}
