package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

@Immutable
data class FirmwareCheckState(
    val deviceName: String = "",
    val deviceId: Long? = null,
    val content: LCEState<List<ModuleCardUi>, FirmwareError> = LCEState.Loading,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val dialog: CheckDialog? = null,
    val bootFailed: Boolean = false,
    /** Модули блока по id — цель входа в загрузчик; карточкам [content] нужны только примитивы. */
    val modules: Map<Long, FirmwareModule> = emptyMap(),
    val deviceCan: FirmwareCan? = null,
)

/** Карточка модуля: только примитивы и enum — устойчива для Compose. */
@Immutable
data class ModuleCardUi(
    val id: Long,
    val kind: FirmwareModuleKind,
    val title: String,
    val currentVersion: CurrentVersionUi,
    val status: CardStatusUi,
)

sealed interface CurrentVersionUi {
    data object Reading : CurrentVersionUi

    data class Known(val version: String) : CurrentVersionUi

    data object Absent : CurrentVersionUi
}

sealed interface CardStatusUi {
    data object Searching : CardStatusUi

    data class UpdateAvailable(val version: String, val description: String) : CardStatusUi

    data class Actual(val version: String) : CardStatusUi

    data class Failed(val error: FirmwareError) : CardStatusUi

    data object NotSupported : CardStatusUi
}

sealed interface CheckDialog {
    data class Description(val moduleId: Long, val version: String, val description: String) : CheckDialog

    data class ConfirmBoot(val moduleId: Long, val sending: Boolean = false) : CheckDialog

    /**
     * [wrong] — подсвечивать ошибку (молчаливая первая попытка PIN у пользователя не спрашивала); [lockedFor] —
     * блок не принимает ввод ещё столько времени.
     */
    data class Pin(
        val moduleId: Long,
        val sending: Boolean = false,
        val wrong: Boolean = false,
        val wrongCount: Int = 0,
        val lockedFor: Duration? = null,
    ) : CheckDialog
}

class FirmwareCheckActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onChannelSelected: (FirmwareChannel) -> Unit = {},
    val onRefreshModule: (Long) -> Unit = {},
    val onDescriptionClick: (Long) -> Unit = {},
    val onUpdateClick: (Long) -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onConfirmBoot: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
)
