package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.TransferActions

@Immutable
internal class DeviceSettingsActions(
    val onBack: () -> Unit,
    val onSearch: () -> Unit,
    val onNavigateTo: (sectionPath: List<Int>) -> Unit,
    val onRetry: () -> Unit,
    val onTreeWidthChange: (Int) -> Unit,
    val onWrite: () -> Unit,
    val onExitWrite: () -> Unit,
    val onExitDiscard: () -> Unit,
    val onDialogDismiss: () -> Unit,
    val onResetRequest: () -> Unit,
    val onResetConfirm: () -> Unit,
    val onSingleCanUpdate: () -> Unit,
    val editor: EditorActions,
    val transfer: TransferActions,
    val onEditSetting: (settingId: Int) -> Unit = {},
)
