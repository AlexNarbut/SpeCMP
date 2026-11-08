package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.TransferActions

/** Действия экрана, кроме строк меню (они — в панелях). */
@Immutable
internal class DeviceSettingsActions(
    val onBack: () -> Unit,
    val onSearch: () -> Unit,
    /** Перейти к разделу по пути от корня (крошки, дерево). */
    val onNavigateTo: (sectionPath: List<Int>) -> Unit,
    val onRetry: () -> Unit,
    /** Ширина колонки дерева изменена перетаскиванием (dp, по окончании). */
    val onTreeWidthChange: (Int) -> Unit,
    val onWrite: () -> Unit,
    val onExitWrite: () -> Unit,
    val onExitDiscard: () -> Unit,
    val onDialogDismiss: () -> Unit,
    val onResetRequest: () -> Unit,
    val onResetConfirm: () -> Unit,
    val onSingleCanUpdate: () -> Unit,
    val editor: EditorActions,
    /** Загрузка и сохранение конфигураций. */
    val transfer: TransferActions,
    /** Открыть окно настройки корневого уровня из дерева (например «Код модели»). */
    val onEditSetting: (settingId: Int) -> Unit = {},
)
