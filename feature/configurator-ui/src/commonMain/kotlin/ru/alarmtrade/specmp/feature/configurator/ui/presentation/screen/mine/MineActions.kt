package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration

@Immutable
internal class MineActions(
    val onBack: () -> Unit,
    val onOpen: (LocalConfiguration) -> Unit,
    val onSelect: (Long) -> Unit,
    val onQueryChange: (String) -> Unit,
    val onModelFilter: (String?) -> Unit,
    val onResetFilter: () -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit,
    val onMenu: (LocalConfiguration) -> Unit,
    val onCreateRequest: () -> Unit,
    val onCreateScratch: () -> Unit,
    val onCreateCloud: () -> Unit,
    val onCreateFile: () -> Unit,
    val onRenameRequest: (LocalConfiguration) -> Unit,
    val onRenameChange: (String, String) -> Unit,
    val onRenameSubmit: () -> Unit,
    val onCopy: (LocalConfiguration, String) -> Unit,
    val onExport: (LocalConfiguration) -> Unit,
    val onWrite: (LocalConfiguration) -> Unit,
    val onDeleteRequest: (LocalConfiguration) -> Unit,
    val onDeleteConfirm: () -> Unit,
    val onDialogDismiss: () -> Unit,
)
