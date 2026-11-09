package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog

/** Что обновляем из файла: основная программа блока ([moduleId]) или обновление не поддерживается. */
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
    /** Диалоги входа в загрузчик — те же, что на экране проверки ([CheckDialog.ConfirmBoot], [CheckDialog.Pin]). */
    val dialog: CheckDialog? = null,
    /** Команда входа в загрузчик не удалась — баннер. */
    val bootFailed: Boolean = false,
    /** Цель входа в загрузчик (основная программа блока); экрану нужен только [FileTarget.Supported.moduleId]. */
    val module: FirmwareModule? = null,
)

/** Действия экрана «Обновить из файла» (stateless-контент получает их от VM или заглушки в галерее состояний). */
class FirmwareFileActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onUpdateClick: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onConfirmBoot: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
)
