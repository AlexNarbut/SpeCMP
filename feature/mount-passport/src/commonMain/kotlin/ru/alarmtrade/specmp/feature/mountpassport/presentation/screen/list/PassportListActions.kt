package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary

/** [onOpen] — открыть паспорт отдельным экраном (узкое окно), [onSelect] — показать справа (широкое). */
@Immutable
internal class PassportListActions(
    val onBack: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onRetry: () -> Unit,
    val onOpen: (MountPassportSummary) -> Unit,
    val onSelect: (MountPassportSummary) -> Unit,
    val onFinish: (MountPassportSummary) -> Unit,
    val onNew: () -> Unit,
    val onNewWithoutUnit: () -> Unit,
    val onDeleteRequest: (MountPassportSummary) -> Unit,
    val onDeleteDismiss: () -> Unit,
    val onDeleteConfirm: () -> Unit,
)
