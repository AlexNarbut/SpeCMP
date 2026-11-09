package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title

sealed interface SerialPhase {
    data object Ready : SerialPhase

    /** [path] — копия файла для SDK: живёт в фазе, чтобы подтверждение забрало её атомарно вместе с переходом. */
    data class Confirm(
        val fileName: String,
        val path: String,
        val fileVersion: String?,
        val older: Boolean,
        val nameMismatch: SystemNameMismatch? = null,
    ) : SerialPhase

    data object WaitingBoot : SerialPhase

    data object CheckingBoot : SerialPhase

    data class Transfer(val percent: Int?) : SerialPhase

    data object Verify : SerialPhase

    data object Apply : SerialPhase

    data class Success(val writtenVersion: String?) : SerialPhase

    data class Error(val failure: SerialFirmwareFailure) : SerialPhase
}

data class SystemNameMismatch(val fileSystemName: String, val deviceSystemName: String)

sealed interface SerialFileProblem {
    data object NotFrm : SerialFileProblem
}

@Immutable
data class SerialFirmwareState(
    val device: AlarmDevice? = null,
    /** Блок в загрузчике: его версия может быть версией загрузчика, а не прошивки. */
    val deviceInBoot: Boolean = false,
    val phase: SerialPhase = SerialPhase.Ready,
    val fileError: FileError? = null,
    val fileProblem: SerialFileProblem? = null,
) {
    val deviceTitle: String? get() = device?.title

    val deviceVersion: String? get() = device?.firmwareVersion

    val locked: Boolean
        get() = when (phase) {
            SerialPhase.WaitingBoot,
            SerialPhase.CheckingBoot,
            is SerialPhase.Transfer,
            SerialPhase.Verify,
            SerialPhase.Apply,
            -> true

            SerialPhase.Ready,
            is SerialPhase.Confirm,
            is SerialPhase.Success,
            is SerialPhase.Error,
            -> false
        }
}

class SerialFirmwareActions(
    val onBack: () -> Unit = {},
    val onFileClick: () -> Unit = {},
    val onConfirmUpdate: () -> Unit = {},
    val onCancelConfirm: () -> Unit = {},
    val onResultOk: () -> Unit = {},
)
