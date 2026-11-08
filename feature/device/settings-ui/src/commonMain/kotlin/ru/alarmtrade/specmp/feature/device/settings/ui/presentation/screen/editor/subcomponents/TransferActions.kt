package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.LoadSourceKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SaveTarget

/** Действия загрузки и сохранения конфигураций (меню «⋯», диалоги K6/K7/K9, панель внизу). */
@Immutable
internal class TransferActions(
    val onLoadRequest: () -> Unit,
    val onLoadSourcePick: (LoadSourceKind) -> Unit,
    val onMismatchContinue: () -> Unit,
    val onDiffConfirm: () -> Unit,
    val onDiffDismiss: () -> Unit,
    val onDiffRetry: () -> Unit,
    val onSaveToRequest: () -> Unit,
    val onSaveTargetPick: (SaveTarget) -> Unit,
    val onSaveToMine: () -> Unit,
    val onExport: () -> Unit,
    val onSaveFormChange: (name: String, description: String) -> Unit,
    val onSaveFormSubmit: () -> Unit,
    val onSaveFormDismiss: () -> Unit,
    val onSaveRetry: () -> Unit,
    val onOverwriteConfirm: () -> Unit,
    val onBarAction: () -> Unit,
    val onExitSave: () -> Unit,
    val onCopy: (name: String) -> Unit,
    val onWriteToDevice: () -> Unit,
)
