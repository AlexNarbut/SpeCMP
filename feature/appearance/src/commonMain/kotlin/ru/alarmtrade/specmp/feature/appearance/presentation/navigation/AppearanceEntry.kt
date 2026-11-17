package ru.alarmtrade.specmp.feature.appearance.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.files.TextFilePicker
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceActions
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceEffects
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceScreen
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceViewModel
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.ColorEditorActions

/** Порты файлов не должны держать Activity-контекст: VM переживает пересоздание Activity. */
@Composable
fun AppearanceEntry(onBack: () -> Unit, filePicker: TextFilePicker, fileSaver: TextFileSaver) {
    val isDark = LocalThemeIsDark.current.value
    val viewModel = koinViewModel<AppearanceViewModel> { parametersOf(filePicker, fileSaver, isDark) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) {
        AppearanceActions(
            onBack = onBack,
            onModeSelect = viewModel::onModeSelect,
            onPresetSelect = viewModel::onPresetSelect,
            onBackgroundSelect = viewModel::onBackgroundSelect,
            onGradientDirectionSelect = viewModel::onGradientDirectionSelect,
            onColorEdit = viewModel::onColorEdit,
            onLinkedToggle = viewModel::onLinkedToggle,
            onTextScaleSelect = viewModel::onTextScaleSelect,
            onRadiusChange = viewModel::onRadiusChange,
            onElementRadiusChange = viewModel::onElementRadiusChange,
            onButtonFillSelect = viewModel::onButtonFillSelect,
            onButtonShapeSelect = viewModel::onButtonShapeSelect,
            onButtonPaddingSelect = viewModel::onButtonPaddingSelect,
            onChipPaddingSelect = viewModel::onChipPaddingSelect,
            onAdvancedToggle = viewModel::onAdvancedToggle,
            onApply = viewModel::onApply,
            onRevert = viewModel::onRevert,
            onExport = viewModel::onExport,
            onImport = viewModel::onImport,
            editor = ColorEditorActions(
                onInput = viewModel::onEditorInput,
                onColorPick = viewModel::onEditorColorPick,
                onFix = viewModel::onEditorFix,
                onPaletteToggle = viewModel::onEditorPaletteToggle,
                onDone = viewModel::onEditorDone,
                onCancel = viewModel::onEditorCancel,
            ),
        )
    }
    val snackbar = rememberCmpSnackbarHostState()
    AppearanceEffects(viewModel.sideEffect, snackbar)
    AppearanceScreen(state, actions, snackbar)
}
