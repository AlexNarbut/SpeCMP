package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

/** Часть фаз выводится из связи, а не из действий пользователя. */
sealed interface UpdatePhase {
    data object Loading : UpdatePhase

    /** Блок перезагружается в загрузчик (связь рвётся намеренно). */
    data object Entering : UpdatePhase

    data object Source : UpdatePhase

    data object DataError : UpdatePhase

    data object Download : UpdatePhase

    data object Confirm : UpdatePhase

    data object Prepare : UpdatePhase

    /** Передача; [reconnecting] — связь прервалась, передача продолжится с того же места после переподключения. */
    data class Transfer(val percent: Int?, val reconnecting: Boolean = false) : UpdatePhase

    data object Verify : UpdatePhase

    data object Apply : UpdatePhase

    data object Success : UpdatePhase

    data object Finishing : UpdatePhase

    data class Error(val failure: FailureKind) : UpdatePhase

    data class Lost(val updating: Boolean) : UpdatePhase
}

enum class FailureKind {
    Crc,
    Timeout,
    LostPacket,
    WrongPacketLength,
    DeviceNotReady,
    ApplyingError,
    Preparation,
    Unknown,
}

enum class UpdateProfile { Main, Bluetooth }

@Immutable
data class FirmwareUpdateState(
    val phase: UpdatePhase = UpdatePhase.Loading,
    val profile: UpdateProfile = UpdateProfile.Main,
    val deviceTitle: String = "",
    val moduleTitles: List<String> = emptyList(),
    val selectedModule: Int = 0,
    val currentVersion: String? = null,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val confirm: ConfirmUi? = null,
    val sourceError: FirmwareError? = null,
    val exitDialog: Boolean = false,
    /** Блок сейчас в загрузчике (на экране успеха это значит «после прошивки блок остался в загрузчике»). */
    val inBoot: Boolean = false,
    val canExitBoot: Boolean = false,
    val exitError: Boolean = false,
    val fileError: FileError? = null,
    val blockCan: BlockCanUi? = null,
    /** Данные процесса и связи; поля экрана выше считаются из них ([toState]). */
    val core: UpdateCore = UpdateCore(),
) {
    val locked: Boolean
        get() = when (phase) {
            UpdatePhase.Entering,
            UpdatePhase.Download,
            UpdatePhase.Prepare,
            is UpdatePhase.Transfer,
            UpdatePhase.Verify,
            UpdatePhase.Apply,
            UpdatePhase.Success,
            UpdatePhase.Finishing,
            -> true

            UpdatePhase.Loading,
            UpdatePhase.Source,
            UpdatePhase.DataError,
            UpdatePhase.Confirm,
            is UpdatePhase.Error,
            is UpdatePhase.Lost,
            -> false
        }
}

/** CAN-прошивка блока: код и профиль; `code == null` — заводские CAN-настройки. */
@Immutable
data class BlockCanUi(val code: String?, val profile: String?)

/** Другой CAN-код — информация, а не запрет. */
@Immutable
data class ConfirmCanUi(val blockCode: String?, val newCode: String, val newProfile: String?) {
    val same: Boolean get() = blockCode == newCode
}

enum class FileError { TooLarge, Failed }

/** [localFile] — [newVersion] тогда имя файла, описания нет; версию и совместимость проверит только блок. */
@Immutable
data class ConfirmUi(
    val currentVersion: String?,
    val newVersion: String,
    val description: String,
    val older: Boolean,
    val localFile: Boolean = false,
    val can: ConfirmCanUi? = null,
)

class FirmwareUpdateActions(
    val onBack: () -> Unit = {},
    val onModuleSelected: (Int) -> Unit = {},
    val onChannelSelected: (FirmwareChannel) -> Unit = {},
    val onDownloadClick: () -> Unit = {},
    val onArchiveClick: () -> Unit = {},
    val onFileClick: () -> Unit = {},
    val onConfirmUpdate: () -> Unit = {},
    val onCancelConfirm: () -> Unit = {},
    val onResultOk: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSupportClick: () -> Unit = {},
    val onReconnect: () -> Unit = {},
    val onDisconnect: () -> Unit = {},
    val onExitBootClick: () -> Unit = {},
    val onExitDialogDismiss: () -> Unit = {},
    val onExitConfirm: () -> Unit = {},
)
