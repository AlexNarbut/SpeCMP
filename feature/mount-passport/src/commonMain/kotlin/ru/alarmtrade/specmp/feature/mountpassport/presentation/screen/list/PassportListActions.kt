package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary

/**
 * [onOpen] — открыть паспорт отдельным экраном (узкое окно), [onSelect] — показать справа (широкое). Перенос:
 * [onExport] — один паспорт, [onExportPicked] — отмеченные в режиме выбора, [onExportAll] — все (копия архива).
 */
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
    val onExport: (MountPassportSummary) -> Unit = {},
    val onExportPdf: (MountPassportSummary) -> Unit = {},
    val onExportAll: () -> Unit = {},
    val onExportPicked: () -> Unit = {},
    val onImport: () -> Unit = {},
    val onPickStart: () -> Unit = {},
    val onPickCancel: () -> Unit = {},
    val onPickToggle: (MountPassportSummary) -> Unit = {},
    val onPickAll: () -> Unit = {},
    val onBackupLater: () -> Unit = {},
)
