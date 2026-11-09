package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog

sealed interface FileTarget {
    data class Supported(val moduleId: Long) : FileTarget

    data object NotSupported : FileTarget
}

@Immutable
data class FirmwareFileState(
    val deviceName: String = "",
    val deviceId: Long? = null,
    val firmwareVersion: String? = null,
    val target: LCEState<FileTarget, FirmwareError> = LCEState.Loading,
    val dialog: CheckDialog? = null,
    val bootFailed: Boolean = false,
    val module: FirmwareModule? = null,
)

class FirmwareFileActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onUpdateClick: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onConfirmBoot: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
)
