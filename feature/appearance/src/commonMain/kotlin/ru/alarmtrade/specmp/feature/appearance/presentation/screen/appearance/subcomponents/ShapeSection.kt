package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_format_dims
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_format_dp
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_format_percent
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_hint_fill_disabled
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_hint_height
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_hint_linked_off
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_hint_linked_on
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_hint_text
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_button_padding
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_chip_padding
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_chip_radius
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_fill
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_linked
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_radius
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_shape
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_label_text
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_title
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_title_dark
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_title_light
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_filled
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_max
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_outlined
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_pill
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_round
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_rounded
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_sample_button
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_sample_chip
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_sample_text
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_sharp
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_soft
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_square
import ru.alarmtrade.specmp.core.localization.resources.appearance_shape_value_tonal
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonShape
import ru.alarmtrade.specmp.core.ui.theme.custom.ControlPadding
import ru.alarmtrade.specmp.core.ui.theme.custom.TextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeRadii
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.input.CmpSlider
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceActions
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceState
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.RadiusElement
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.fillAvailable

private val ShapeSampleWidth = 56.dp
private val ShapeSampleHeight = 32.dp

/** Быстрые значения общего скругления, как в макете. */
private val QuickRadii = listOf(2, 12, 24)

@Composable
internal fun ShapeSection(state: AppearanceState, actions: AppearanceActions, modifier: Modifier = Modifier) {
    val mode = state.mode
    val title = when {
        state.linked -> Strings.string.appearance_shape_title
        state.isDark -> Strings.string.appearance_shape_title_dark
        else -> Strings.string.appearance_shape_title_light
    }
    SectionCard(title = stringResource(title), modifier = modifier) {
        CmpListItem(
            title = stringResource(Strings.string.appearance_shape_label_linked),
            subtitle = stringResource(
                if (state.linked) {
                    Strings.string.appearance_shape_hint_linked_on
                } else {
                    Strings.string.appearance_shape_hint_linked_off
                },
            ),
            flat = true,
            titleMaxLines = 2,
            onClick = actions.onLinkedToggle,
            modifier = Modifier.testTag("appearance_linked"),
            trailing = { CmpSwitch(checked = state.linked, onCheckedChange = { actions.onLinkedToggle() }) },
        )
        TextScaleBlock(mode, actions.onTextScaleSelect)
        RadiusBlock(mode, actions)
        FillBlock(state, actions.onButtonFillSelect)
        ShapeBlock(mode, actions.onButtonShapeSelect)
        PaddingBlock(
            label = stringResource(Strings.string.appearance_shape_label_button_padding),
            selected = mode.buttonPadding,
            onSelect = actions.onButtonPaddingSelect,
        ) { padding ->
            SampleButton(
                text = stringResource(Strings.string.appearance_shape_value_sample_button),
                mode = mode,
                dims = padding.buttonDims(),
            )
            padding.buttonDims()
        }
        RadiusSlider(
            label = stringResource(Strings.string.appearance_shape_label_chip_radius),
            value = mode.radii.chips,
            onChange = { actions.onElementRadiusChange(RadiusElement.Chips, it) },
            maxIsPill = true,
        )
        PaddingBlock(
            label = stringResource(Strings.string.appearance_shape_label_chip_padding),
            selected = mode.chipPadding,
            onSelect = actions.onChipPaddingSelect,
        ) { padding ->
            SampleChip(
                text = stringResource(Strings.string.appearance_shape_value_sample_chip),
                mode = mode.copy(chipPadding = padding),
            )
            padding.chipDims()
        }
    }
}

@Composable
private fun TextScaleBlock(mode: ThemeModeSpec, onSelect: (TextScale) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(stringResource(Strings.string.appearance_shape_label_text))
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            TextScale.entries.forEach { scale ->
                OptionTile(
                    selected = scale == mode.textScale,
                    onClick = { onSelect(scale) },
                    modifier = Modifier.weight(1f).testTag("appearance_text_${scale.percent}"),
                ) {
                    CmpText(
                        text = stringResource(Strings.string.appearance_shape_value_sample_text),
                        style = scale.sampleStyle(),
                        color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                        maxLines = 1,
                    )
                    CmpText(
                        text = stringResource(Strings.string.appearance_shape_format_percent, scale.percent),
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                        maxLines = 1,
                    )
                }
            }
        }
        Hint(stringResource(Strings.string.appearance_shape_hint_text))
    }
}

private fun TextScale.sampleStyle(): CmpTextStyle = when (this) {
    TextScale.Normal -> CmpTextStyle.Paragraph1
    TextScale.Large -> CmpTextStyle.Headline1
    TextScale.Larger -> CmpTextStyle.Title2
    TextScale.Largest -> CmpTextStyle.Title1
}

@Composable
private fun RadiusBlock(mode: ThemeModeSpec, actions: AppearanceActions) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        RadiusSlider(
            label = stringResource(Strings.string.appearance_shape_label_radius),
            value = mode.radius,
            onChange = actions.onRadiusChange,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            val labels = listOf(
                Strings.string.appearance_shape_value_sharp,
                Strings.string.appearance_shape_value_soft,
                Strings.string.appearance_shape_value_round,
            )
            QuickRadii.zip(labels).forEach { (radius, label) ->
                CmpChip(
                    text = stringResource(label),
                    onClick = { actions.onRadiusChange(radius) },
                    selected = mode.radius == radius,
                )
            }
        }
    }
}

@Composable
internal fun RadiusSlider(label: String, value: Int, onChange: (Int) -> Unit, maxIsPill: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        val shown = if (maxIsPill && value >= ThemeRadii.MAX) {
            stringResource(Strings.string.appearance_shape_value_max)
        } else {
            stringResource(Strings.string.appearance_shape_format_dp, value)
        }
        ValueRow(label = label, value = shown)
        CmpSlider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = 0f..ThemeRadii.MAX.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FillBlock(state: AppearanceState, onSelect: (ButtonFill) -> Unit) {
    val mode = state.mode
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(stringResource(Strings.string.appearance_shape_label_fill))
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            ButtonFill.entries.forEach { fill ->
                val available = state.fillAvailable(fill)
                OptionTile(
                    selected = fill == mode.buttonFill,
                    onClick = { onSelect(fill) },
                    enabled = available,
                    modifier = Modifier.weight(1f).testTag("appearance_fill_${fill.name.lowercase()}"),
                ) {
                    SampleButton(
                        text = stringResource(Strings.string.appearance_shape_value_sample_button),
                        mode = mode,
                        fill = fill,
                        dims = ControlPadding.Small.buttonDims(),
                    )
                    CmpText(
                        text = stringResource(fill.label()),
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                        maxLines = 1,
                    )
                    if (!available) {
                        CmpText(
                            text = stringResource(Strings.string.appearance_shape_hint_fill_disabled),
                            style = CmpTextStyle.Additional1,
                            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                        )
                    }
                }
            }
        }
    }
}

private fun ButtonFill.label() = when (this) {
    ButtonFill.Filled -> Strings.string.appearance_shape_value_filled
    ButtonFill.Tonal -> Strings.string.appearance_shape_value_tonal
    ButtonFill.Outlined -> Strings.string.appearance_shape_value_outlined
}

@Composable
private fun ShapeBlock(mode: ThemeModeSpec, onSelect: (ButtonShape) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(stringResource(Strings.string.appearance_shape_label_shape))
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            ButtonShape.entries.forEach { shape ->
                OptionTile(
                    selected = shape == mode.buttonShape,
                    onClick = { onSelect(shape) },
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .width(ShapeSampleWidth)
                            .height(ShapeSampleHeight)
                            .background(mode.palette.primary.toColor(), buttonShape(shape, mode.radii.buttons)),
                    )
                    CmpText(
                        text = stringResource(shape.label()),
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                        maxLines = 2,
                    )
                }
            }
        }
        Hint(stringResource(Strings.string.appearance_shape_hint_height))
    }
}

private fun ButtonShape.label() = when (this) {
    ButtonShape.Square -> Strings.string.appearance_shape_value_square
    ButtonShape.Rounded -> Strings.string.appearance_shape_value_rounded
    ButtonShape.Pill -> Strings.string.appearance_shape_value_pill
}

/** [sample] рисует образец варианта и отдаёт его размеры для подписи. */
@Composable
private fun PaddingBlock(
    label: String,
    selected: ControlPadding,
    onSelect: (ControlPadding) -> Unit,
    sample: @Composable (ControlPadding) -> ControlDims,
) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(label)
        Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            ControlPadding.entries.forEach { padding ->
                OptionTile(
                    selected = padding == selected,
                    onClick = { onSelect(padding) },
                    modifier = Modifier.weight(1f),
                ) {
                    val dims = sample(padding)
                    CmpText(
                        text = padding.title(),
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                        maxLines = 1,
                    )
                    CmpText(
                        text = stringResource(Strings.string.appearance_shape_format_dims, dims.height, dims.side),
                        style = CmpTextStyle.Additional1,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
