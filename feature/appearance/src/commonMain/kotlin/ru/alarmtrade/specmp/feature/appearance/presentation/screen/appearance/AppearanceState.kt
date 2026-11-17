package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.theme.custom.ContrastCheckKind
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeSpec

/**
 * [applied] — тема приложения, [draft] — черновик экрана; [isDark] — какая тема черновика настраивается и видна в
 * предпросмотре. [linked] — правки размеров, формы и кнопок идут в обе темы.
 */
@Immutable
internal data class AppearanceState(
    val applied: ThemeSpec,
    val isDark: Boolean,
    val draft: ThemeSpec = applied,
    val linked: Boolean = draft.light.sameShapeAs(draft.dark),
    val advancedExpanded: Boolean = false,
    val editor: ColorEditorState? = null,
) {
    val mode: ThemeModeSpec get() = draft.mode(isDark)

    val isDirty: Boolean get() = draft != applied

    val preset: ThemePreset? get() = ThemePreset.matching(draft)
}

/** Редактор цвета [role]: [input] — текст поля, [candidate] — последний верный цвет, [fix] — ближайший подходящий. */
@Immutable
internal data class ColorEditorState(
    val role: ThemeColorRole,
    val input: String,
    val candidate: ThemeColor,
    val checks: List<ContrastResult>,
    val fix: ThemeColor?,
    val paletteOpen: Boolean = false,
) {
    val inputValid: Boolean get() = ThemeColor.parse(input) != null

    val failing: Boolean get() = checks.any { !it.passes }

    val canSave: Boolean get() = inputValid && !failing
}

@Immutable
internal data class ContrastResult(val kind: ContrastCheckKind, val ratio: Double, val min: Double) {
    val passes: Boolean get() = ratio >= min
}

internal enum class RadiusElement { Buttons, Inputs, Cards, Chips, Sheets }
