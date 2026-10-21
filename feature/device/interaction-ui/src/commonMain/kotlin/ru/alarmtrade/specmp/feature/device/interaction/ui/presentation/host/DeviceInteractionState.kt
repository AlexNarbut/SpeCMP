package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState

@Immutable
data class DeviceInteractionState(
    val session: DeviceSessionState = DeviceSessionState.Connecting,
    /** Последний блок в рабочем режиме — дочерний flow; null — связи ещё не было или блок в загрузчике. */
    val device: AlarmDevice? = null,
    /** Транспорт сессии — тексты экрана «связь потеряна»; null — неизвестен. */
    val transport: ConnectionTransport? = null
)

sealed interface DeviceInteractionEffect {
    /** Сессия закрыта — выйти из flow. */
    data object Exit : DeviceInteractionEffect
}
