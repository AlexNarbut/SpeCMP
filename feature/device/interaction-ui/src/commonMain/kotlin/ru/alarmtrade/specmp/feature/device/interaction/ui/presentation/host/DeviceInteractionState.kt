package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot

@Immutable
data class DeviceInteractionState(
    val session: DeviceSessionState = DeviceSessionState.Connecting,
    /** Последний блок в рабочем режиме — дочерний flow; null — связи ещё не было или блок в загрузчике. */
    val device: AlarmDevice? = null,
    val transport: ConnectionTransport? = null,
    /** Последний режим блока на связи — загрузчик: обрыв и переподключение остаются на экране загрузчика. */
    val wasBoot: Boolean = false,
    val reboot: DeviceReboot? = null,
    val exitRequested: Boolean = false,
) {
    /** Обрыв, пока блок был в загрузчике, тоже остаётся на экране загрузчика (обрыв при передаче экран решает сам). */
    val showBootContent: Boolean
        get() = when {
            session.isBoot -> true
            reboot == DeviceReboot.ToMain -> true
            reboot == DeviceReboot.ToBoot -> true
            session is DeviceSessionState.Ready -> false
            else -> wasBoot
        }

    fun withSession(session: DeviceSessionState): DeviceInteractionState = copy(
        session = session,
        // Рабочий режим — блок для дочернего flow; загрузчик сбрасывает его: дочерний flow уходит из композиции
        // и после возврата в рабочий режим стартует заново. Обрыв/подключение блок не трогают.
        device = when (session) {
            is DeviceSessionState.Ready -> session.device
            is DeviceSessionState.BluetoothBoot, is DeviceSessionState.FirmwareBoot -> null
            DeviceSessionState.Connecting, DeviceSessionState.Disconnected -> device
        },
        wasBoot = when (session) {
            is DeviceSessionState.Ready -> false
            is DeviceSessionState.BluetoothBoot, is DeviceSessionState.FirmwareBoot -> true
            DeviceSessionState.Connecting, DeviceSessionState.Disconnected -> wasBoot
        },
    )
}
