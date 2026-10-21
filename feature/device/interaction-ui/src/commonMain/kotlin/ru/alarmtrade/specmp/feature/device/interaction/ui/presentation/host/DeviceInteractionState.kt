package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState

@Immutable
data class DeviceInteractionState(
    val session: DeviceSessionState = DeviceSessionState.Connecting,
    /** Последний блок в рабочем режиме — дочерний flow; null — связи ещё не было или блок в загрузчике. */
    val device: AlarmDevice? = null,
    /** Транспорт сессии — тексты экрана «связь потеряна»; null — неизвестен. */
    val transport: ConnectionTransport? = null,
    /** Последний режим блока на связи — загрузчик: обрыв и переподключение остаются на экране загрузчика. */
    val wasBoot: Boolean = false,
    /** Перезагрузка блока, которую приложение вызвало и ждёт (вход в загрузчик, возврат после прошивки). */
    val reboot: DeviceReboot? = null
) {
    /**
     * Показывать экран режима загрузчика (слот хоста), а не рабочий flow и не оверлей «потеря связи»:
     * блок в загрузчике; вход в загрузчик ждём (связь рвётся намеренно); после прошивки ждём возврат блока
     * и подтверждение результата; обрыв связи, пока блок был в загрузчике (обрыв при передаче — экран решает сам).
     */
    val showBootContent: Boolean
        get() = when {
            session is DeviceSessionState.BluetoothBoot || session is DeviceSessionState.FirmwareBoot -> true
            reboot == DeviceReboot.ToMain -> true
            reboot == DeviceReboot.ToBoot -> true
            session is DeviceSessionState.Ready -> false
            else -> wasBoot
        }
}

sealed interface DeviceInteractionEffect {
    /** Сессия закрыта — выйти из flow. */
    data object Exit : DeviceInteractionEffect
}
