package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.runtime.Composable
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_accent_background
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_accent_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_accent_gradient
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_error
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_info
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_ink2_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_ink_background
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_ink_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_ink_gradient
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_on_primary
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_outline
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_success
import ru.alarmtrade.specmp.core.localization.resources.appearance_check_label_warning
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_background
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_background2
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_error
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_info
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_ink
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_ink2
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_onprimary
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_outline
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_primary
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_success
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_hint_warning
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_background
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_background2
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_error
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_info
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_ink
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_ink2
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_onprimary
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_outline
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_primary
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_success
import ru.alarmtrade.specmp.core.localization.resources.appearance_color_label_warning
import ru.alarmtrade.specmp.core.localization.resources.appearance_presets_value_contrast
import ru.alarmtrade.specmp.core.localization.resources.appearance_presets_value_graphite
import ru.alarmtrade.specmp.core.localization.resources.appearance_presets_value_nightshift
import ru.alarmtrade.specmp.core.localization.resources.appearance_presets_value_pandora
import ru.alarmtrade.specmp.core.localization.resources.appearance_presets_value_workshop
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_large
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_medium
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_small
import ru.alarmtrade.specmp.core.ui.theme.custom.ContrastCheckKind
import ru.alarmtrade.specmp.core.ui.theme.custom.ControlPadding
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset

/** Цвета основного блока; остальные — в «Все параметры». */
internal val BasicRoles = listOf(
    ThemeColorRole.Primary,
    ThemeColorRole.Background,
    ThemeColorRole.Background2,
    ThemeColorRole.Card,
    ThemeColorRole.Ink,
)

internal val MoreRoles = listOf(
    ThemeColorRole.OnPrimary,
    ThemeColorRole.Ink2,
    ThemeColorRole.Outline,
    ThemeColorRole.Success,
    ThemeColorRole.Warning,
    ThemeColorRole.Error,
    ThemeColorRole.Info,
)

@Composable
internal fun ThemePreset.title(): String = stringResource(
    when (this) {
        ThemePreset.Pandora -> Strings.string.appearance_presets_value_pandora
        ThemePreset.Graphite -> Strings.string.appearance_presets_value_graphite
        ThemePreset.Contrast -> Strings.string.appearance_presets_value_contrast
        ThemePreset.Workshop -> Strings.string.appearance_presets_value_workshop
        ThemePreset.NightShift -> Strings.string.appearance_presets_value_nightshift
    },
)

@Composable
internal fun ThemeColorRole.title(): String = stringResource(roleText(this).first)

@Composable
internal fun ThemeColorRole.hint(): String = stringResource(roleText(this).second)

private fun roleText(role: ThemeColorRole): Pair<StringResource, StringResource> = when (role) {
    ThemeColorRole.Primary ->
        Strings.string.appearance_color_label_primary to
            Strings.string.appearance_color_hint_primary

    ThemeColorRole.OnPrimary ->
        Strings.string.appearance_color_label_onprimary to Strings.string.appearance_color_hint_onprimary

    ThemeColorRole.Background ->
        Strings.string.appearance_color_label_background to Strings.string.appearance_color_hint_background

    ThemeColorRole.Background2 ->
        Strings.string.appearance_color_label_background2 to Strings.string.appearance_color_hint_background2

    ThemeColorRole.Card -> Strings.string.appearance_color_label_card to Strings.string.appearance_color_hint_card

    ThemeColorRole.Ink -> Strings.string.appearance_color_label_ink to Strings.string.appearance_color_hint_ink

    ThemeColorRole.Ink2 -> Strings.string.appearance_color_label_ink2 to Strings.string.appearance_color_hint_ink2

    ThemeColorRole.Outline ->
        Strings.string.appearance_color_label_outline to
            Strings.string.appearance_color_hint_outline

    ThemeColorRole.Success ->
        Strings.string.appearance_color_label_success to
            Strings.string.appearance_color_hint_success

    ThemeColorRole.Warning ->
        Strings.string.appearance_color_label_warning to
            Strings.string.appearance_color_hint_warning

    ThemeColorRole.Error -> Strings.string.appearance_color_label_error to Strings.string.appearance_color_hint_error

    ThemeColorRole.Info -> Strings.string.appearance_color_label_info to Strings.string.appearance_color_hint_info
}

@Composable
internal fun ContrastCheckKind.title(): String = stringResource(
    when (this) {
        ContrastCheckKind.InkOnBackground -> Strings.string.appearance_check_label_ink_background
        ContrastCheckKind.InkOnCard -> Strings.string.appearance_check_label_ink_card
        ContrastCheckKind.Ink2OnCard -> Strings.string.appearance_check_label_ink2_card
        ContrastCheckKind.LabelOnButton -> Strings.string.appearance_check_label_on_primary
        ContrastCheckKind.AccentOnBackground -> Strings.string.appearance_check_label_accent_background
        ContrastCheckKind.AccentOnCard -> Strings.string.appearance_check_label_accent_card
        ContrastCheckKind.OutlineOnCard -> Strings.string.appearance_check_label_outline
        ContrastCheckKind.SuccessOnCard -> Strings.string.appearance_check_label_success
        ContrastCheckKind.WarningOnCard -> Strings.string.appearance_check_label_warning
        ContrastCheckKind.ErrorOnCard -> Strings.string.appearance_check_label_error
        ContrastCheckKind.InfoOnCard -> Strings.string.appearance_check_label_info
        ContrastCheckKind.InkOnGradientEnd -> Strings.string.appearance_check_label_ink_gradient
        ContrastCheckKind.AccentOnGradientEnd -> Strings.string.appearance_check_label_accent_gradient
    },
)

@Composable
internal fun ControlPadding.title(): String = stringResource(
    when (this) {
        ControlPadding.Small -> Strings.string.appearance_shape_value_small
        ControlPadding.Medium -> Strings.string.appearance_shape_value_medium
        ControlPadding.Large -> Strings.string.appearance_shape_value_large
    },
)

/** Одна цифра после точки, как в макете: `4.5`, `21.0`. */
internal fun formatRatio(value: Double): String {
    val tenths = (value * TENTHS).roundToInt()
    val whole = tenths / TENTHS
    val fraction = tenths % TENTHS
    return "$whole.$fraction"
}

internal fun formatMin(value: Double): String {
    val tenths = (value * TENTHS).roundToInt()
    return if (tenths % TENTHS == 0) "${tenths / TENTHS}" else formatRatio(value)
}

private const val TENTHS = 10
