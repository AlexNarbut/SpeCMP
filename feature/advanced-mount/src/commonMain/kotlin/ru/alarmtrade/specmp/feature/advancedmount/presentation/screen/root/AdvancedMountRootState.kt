package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountError
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

@Immutable
data class AdvancedMountRootState(
    /** Последний прочитанный блок; `null` — ещё не прочитан (экран перекрыт обёрткой «Подключение…»). */
    val device: AlarmDevice? = null,
    val transport: ConnectionTransport? = null,
    val imei: String? = null,
    val content: LCEState<MountAccess, MountError> = LCEState.Loading,
    val unreadCount: Int = 0,
    val remoteAvailable: Boolean = false,
    val productInfo: Boolean = false,
    val dialog: MountDialog? = null,
) {
    val canLocked: Boolean
        get() = device?.can?.isFactory == true && content.content is MountAccess.Ready
}

@Immutable
sealed interface MountAccess {
    data class Denied(val message: String) : MountAccess

    /** [modelNotInDatabase] — модели блока нет в базе устройств телефона (не скачана/устарела). */
    data class Ready(
        val sections: List<MountSection>,
        val unsupported: Boolean,
        val modelNotInDatabase: Boolean = false,
    ) : MountAccess
}

enum class MountDialog {
    CAN_UPDATE,

    CAN_HINT,
}
