package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
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
    sessionRepository: DeviceSessionRepository,
    private val connectionRepository: DeviceConnectionRepository,
    appScope: CoroutineScope,
) {

    val state: StateFlow<DeviceSessionState> = sessionRepository.linkState()
        .distinctUntilChanged()
        .transformLatest { link ->
            when (link) {
                DeviceLinkState.CONNECTING -> emit(DeviceSessionState.Connecting)
                DeviceLinkState.DISCONNECTED -> emit(DeviceSessionState.Disconnected)
                DeviceLinkState.READY -> {
                    emit(DeviceSessionState.Connecting)
                    val device = readWithRetry(sessionRepository)
                    emit(if (device != null) DeviceSessionState.Ready(device) else DeviceSessionState.Disconnected)
                }
            }
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0),
            initialValue = DeviceSessionState.Connecting,
        )

    /** Закрыть сессию с устройством (выход из flow работы с устройством). */
    suspend fun close() = connectionRepository.closeSessions()

    private suspend fun readWithRetry(repository: DeviceSessionRepository): AlarmDevice? {
        repeat(READ_ATTEMPTS) { attempt ->
            val response = repository.readDevice()
            if (response is Response.Success) return response.value
            if (attempt < READ_ATTEMPTS - 1) delay(READ_RETRY_DELAY_MILLIS)
        }
        return null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val READ_ATTEMPTS = 3
        const val READ_RETRY_DELAY_MILLIS = 500L
    }
}
