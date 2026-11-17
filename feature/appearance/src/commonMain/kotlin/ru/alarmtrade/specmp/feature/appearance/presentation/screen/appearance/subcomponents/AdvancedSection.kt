package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_format_type_scale
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_format_type_size
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_buttons
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_cards
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_inputs
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_radii
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_sheets
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_type_body
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_type_caption
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_type_card
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_type_controls
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_label_type_title
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_subtitle
import ru.alarmtrade.specmp.core.localization.resources.appearance_advanced_title
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_title_dark
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_title_light
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceActions
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.RadiusElement

/** Шкала текста макета: базовые размеры стилей. */
private val TypeRows = listOf(
    Strings.string.appearance_advanced_label_type_title to 32,
    Strings.string.appearance_advanced_label_type_card to 24,
    Strings.string.appearance_advanced_label_type_controls to 19,
    Strings.string.appearance_advanced_label_type_body to 16,
    Strings.string.appearance_advanced_label_type_caption to 14,
)

@Composable
internal fun AdvancedSection(
    mode: ThemeModeSpec,
    isDark: Boolean,
    expanded: Boolean,
    actions: AppearanceActions,
    modifier: Modifier = Modifier,
) {
    SectionCard(title = null, modifier = modifier) {
        CmpListItem(
            title = stringResource(Strings.string.appearance_advanced_title),
            subtitle = stringResource(Strings.string.appearance_advanced_subtitle),
            icon = CmpIcons.Tune,
            flat = true,
            onClick = actions.onAdvancedToggle,
            modifier = Modifier.testTag("appearance_advanced"),
            trailing = {
                CmpIcon(
                    icon = if (expanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(CmpSize.iconL),
                )
            },
        )
        if (expanded) {
            val colorsTitle = if (isDark) {
                Strings.string.appearance_colors_title_dark
            } else {
                Strings.string.appearance_colors_title_light
            }
            FieldLabel(stringResource(colorsTitle))
            ColorRows(roles = MoreRoles, mode = mode, onEdit = actions.onColorEdit)
            CmpDivider()
            ElementRadii(mode, actions.onElementRadiusChange)
            CmpDivider()
            TypeScale(mode)
        }
    }
}

@Composable
private fun ElementRadii(mode: ThemeModeSpec, onChange: (RadiusElement, Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(stringResource(Strings.string.appearance_advanced_label_radii))
        listOf(
            Triple(RadiusElement.Buttons, Strings.string.appearance_advanced_label_buttons, mode.radii.buttons),
            Triple(RadiusElement.Inputs, Strings.string.appearance_advanced_label_inputs, mode.radii.inputs),
            Triple(RadiusElement.Cards, Strings.string.appearance_advanced_label_cards, mode.radii.cards),
            Triple(RadiusElement.Sheets, Strings.string.appearance_advanced_label_sheets, mode.radii.sheets),
        ).forEach { (element, label, value) ->
            RadiusSlider(label = stringResource(label), value = value, onChange = { onChange(element, it) })
        }
    }
}

@Composable
private fun TypeScale(mode: ThemeModeSpec) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        FieldLabel(stringResource(Strings.string.appearance_advanced_format_type_scale, mode.textScale.percent))
        TypeRows.forEach { (label, size) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Hint(stringResource(label))
                CmpText(
                    text = stringResource(
                        Strings.string.appearance_advanced_format_type_size,
                        size,
                        (size * mode.textScale.factor).roundToInt(),
                    ),
                    style = CmpTextStyle.Paragraph3,
                    color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                )
            }
        }
    }
}
