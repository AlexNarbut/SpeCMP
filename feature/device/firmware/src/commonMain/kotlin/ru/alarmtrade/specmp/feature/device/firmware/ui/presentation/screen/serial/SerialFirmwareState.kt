package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.BlockCanUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title

sealed interface SerialPhase {
    data object Ready : SerialPhase

    /** Версия с сервера скачивается во временный файл; «Назад» отменяет. */
    data object Download : SerialPhase

    /**
     * [path] — копия файла для SDK: живёт в фазе, чтобы подтверждение забрало её атомарно вместе с переходом.
     * [fromServer] — версия с сервера или из архива: [description] — её описание.
     */
    data class Confirm(
        val fileName: String,
        val path: String,
        val fileVersion: String?,
        val older: Boolean,
        val nameMismatch: SystemNameMismatch? = null,
        val description: String = "",
        val fromServer: Boolean = false,
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

    /** Версия с сервера или из архива — не `.frm` (по расширению в ссылке или по заголовку скачанного). */
    data object ServerNotFrm : SerialFileProblem
}

sealed interface SerialSourceError {
    data class Server(val error: FirmwareError) : SerialSourceError

    /** Модели блока нет в базе устройств сервера или у неё нет подсистем. */
    data object NoModule : SerialSourceError
}

/** [moduleId] — модуль модели блока на сервере, найденный при первом обращении к серверу или архиву. */
@Immutable
data class SerialFirmwareState(
    val device: AlarmDevice? = null,
    /** Блок в загрузчике: его версия может быть версией загрузчика, а не прошивки. */
    val deviceInBoot: Boolean = false,
    val phase: SerialPhase = SerialPhase.Ready,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val fileError: FileError? = null,
    val fileProblem: SerialFileProblem? = null,
    val sourceError: SerialSourceError? = null,
    val archive: FirmwareArchiveRequest? = null,
    val moduleId: Long? = null,
) {
    val deviceTitle: String? get() = device?.title

    val deviceVersion: String? get() = device?.firmwareVersion

    val blockCan: BlockCanUi?
        get() {
            val can = device?.can ?: return null
            return when {
                can.isFactory -> BlockCanUi(code = null, profile = null)
                can.code.isNotBlank() -> BlockCanUi(can.code, can.firmwareVersion.takeIf { it.isNotBlank() })
                else -> null
            }
        }

    val locked: Boolean
        get() = when (phase) {
            SerialPhase.WaitingBoot,
            SerialPhase.CheckingBoot,
            is SerialPhase.Transfer,
            SerialPhase.Verify,
            SerialPhase.Apply,
            -> true

            SerialPhase.Ready,
            SerialPhase.Download,
            is SerialPhase.Confirm,
            is SerialPhase.Success,
            is SerialPhase.Error,
            -> false
        }
}

class SerialFirmwareActions(
    val onBack: () -> Unit = {},
    val onChannelSelected: (FirmwareChannel) -> Unit = {},
    val onDownloadClick: () -> Unit = {},
    val onArchiveClick: () -> Unit = {},
    val onFileClick: () -> Unit = {},
    val onConfirmUpdate: () -> Unit = {},
    val onCancelConfirm: () -> Unit = {},
    val onResultOk: () -> Unit = {},
)
