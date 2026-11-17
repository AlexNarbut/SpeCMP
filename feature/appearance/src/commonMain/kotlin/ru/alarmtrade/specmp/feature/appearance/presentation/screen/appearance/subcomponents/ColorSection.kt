package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_label_background
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_title_dark
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_title_light
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_value_corner
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_value_diagonal
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_value_down
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_value_glow
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_value_gradient
import ru.alarmtrade.specmp.core.localization.resources.appearance_colors_value_solid
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.GradientDirection
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.AppearanceActions

private val DirectionSample = 40.dp

@Composable
internal fun ColorSection(
    mode: ThemeModeSpec,
    isDark: Boolean,
    actions: AppearanceActions,
    modifier: Modifier = Modifier,
) {
    val title = if (isDark) {
        Strings.string.appearance_colors_title_dark
    } else {
        Strings.string.appearance_colors_title_light
    }
    SectionCard(title = stringResource(title), modifier = modifier) {
        FieldLabel(stringResource(Strings.string.appearance_colors_label_background))
        val types = ThemeBackground.entries
        CmpSegmentedButton(
            options = types.map { it.title() },
            selectedIndex = types.indexOf(mode.background),
            onSelect = { actions.onBackgroundSelect(types[it]) },
            modifier = Modifier.fillMaxWidth().testTag("appearance_background"),
        )
        val isGradient = mode.background == ThemeBackground.Gradient
        if (isGradient) {
            GradientDirections(mode, actions.onGradientDirectionSelect)
        }
        ColorRows(
            roles = BasicRoles.filter { it != ThemeColorRole.Background2 || isGradient },
            mode = mode,
            onEdit = actions.onColorEdit,
        )
    }
}

@Composable
internal fun ColorRows(roles: List<ThemeColorRole>, mode: ThemeModeSpec, onEdit: (ThemeColorRole) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        roles.forEach { role ->
            val color = mode.palette[role]
            CmpListItem(
                title = role.title(),
                subtitle = role.hint(),
                flat = true,
                leading = { ColorSwatch(color) },
                onClick = { onEdit(role) },
                modifier = Modifier.fillMaxWidth().testTag("appearance_color_${role.name.lowercase()}"),
                trailing = {
                    CmpText(
                        text = color.hex,
                        style = CmpTextStyle.Additional0,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                    CmpIcon(
                        icon = CmpIcons.ChevronRight,
                        contentDescription = null,
                        tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                },
            )
        }
    }
}

@Composable
private fun GradientDirections(mode: ThemeModeSpec, onSelect: (GradientDirection) -> Unit) {
    val start = mode.palette.background.toColor()
    val end = mode.palette.background2.toColor()
    Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        GradientDirection.entries.forEach { direction ->
            OptionTile(
                selected = direction == mode.gradientDirection,
                onClick = { onSelect(direction) },
                modifier = Modifier.weight(1f),
            ) {
                val brush = when (direction) {
                    GradientDirection.Down -> Brush.verticalGradient(listOf(start, end))
                    GradientDirection.Diagonal -> Brush.linearGradient(listOf(start, end))
                    GradientDirection.Corner -> Brush.radialGradient(listOf(end, start))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DirectionSample)
                        .background(brush, RoundedCornerShape(CmpRadius.cardSmall)),
                )
                CmpText(
                    text = direction.title(),
                    style = CmpTextStyle.Additional0,
                    color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun ThemeBackground.title(): String = stringResource(
    when (this) {
        ThemeBackground.Glow -> Strings.string.appearance_colors_value_glow
        ThemeBackground.Solid -> Strings.string.appearance_colors_value_solid
        ThemeBackground.Gradient -> Strings.string.appearance_colors_value_gradient
    },
)

@Composable
private fun GradientDirection.title(): String = stringResource(
    when (this) {
        GradientDirection.Down -> Strings.string.appearance_colors_value_down
        GradientDirection.Diagonal -> Strings.string.appearance_colors_value_diagonal
        GradientDirection.Corner -> Strings.string.appearance_colors_value_corner
    },
)
