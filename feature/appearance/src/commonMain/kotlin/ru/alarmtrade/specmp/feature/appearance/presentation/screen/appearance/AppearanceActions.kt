package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonShape
import ru.alarmtrade.specmp.core.ui.theme.custom.ControlPadding
import ru.alarmtrade.specmp.core.ui.theme.custom.GradientDirection
import ru.alarmtrade.specmp.core.ui.theme.custom.TextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset

internal class AppearanceActions(
    val onBack: () -> Unit,
    val onModeSelect: (isDark: Boolean) -> Unit,
    val onPresetSelect: (ThemePreset) -> Unit,
    val onBackgroundSelect: (ThemeBackground) -> Unit,
    val onGradientDirectionSelect: (GradientDirection) -> Unit,
    val onColorEdit: (ThemeColorRole) -> Unit,
    val onLinkedToggle: () -> Unit,
    val onTextScaleSelect: (TextScale) -> Unit,
    val onRadiusChange: (Int) -> Unit,
    val onElementRadiusChange: (RadiusElement, Int) -> Unit,
    val onButtonFillSelect: (ButtonFill) -> Unit,
    val onButtonShapeSelect: (ButtonShape) -> Unit,
    val onButtonPaddingSelect: (ControlPadding) -> Unit,
    val onChipPaddingSelect: (ControlPadding) -> Unit,
    val onAdvancedToggle: () -> Unit,
    val onApply: () -> Unit,
    val onRevert: () -> Unit,
    val onExport: () -> Unit,
    val onImport: () -> Unit,
    val editor: ColorEditorActions,
)

internal class ColorEditorActions(
    val onInput: (String) -> Unit,
    val onColorPick: (ThemeColor) -> Unit,
    val onFix: () -> Unit,
    val onPaletteToggle: () -> Unit,
    val onDone: () -> Unit,
    val onCancel: () -> Unit,
)
