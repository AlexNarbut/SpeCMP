package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_presets_title
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val PresetWidth = 148.dp
private val SampleHeight = 56.dp
private val SampleBar = 8.dp

@Composable
internal fun PresetSection(selected: ThemePreset?, onSelect: (ThemePreset) -> Unit, modifier: Modifier = Modifier) {
    SectionCard(title = stringResource(Strings.string.appearance_presets_title), modifier = modifier) {
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            ThemePreset.entries.forEach { preset ->
                OptionTile(
                    selected = preset == selected,
                    onClick = { onSelect(preset) },
                    modifier = Modifier.width(PresetWidth).testTag("appearance_preset_${preset.name.lowercase()}"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(SampleHeight).clip(RoundedCornerShape(SampleBar)),
                    ) {
                        PresetHalf(preset.spec.light, Modifier.weight(1f))
                        PresetHalf(preset.spec.dark, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                    ) {
                        CmpText(
                            text = preset.title(),
                            style = CmpTextStyle.Headline3,
                            color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                            modifier = Modifier.weight(1f),
                        )
                        if (preset == selected) {
                            CmpIcon(
                                icon = CmpIcons.CheckCircle,
                                contentDescription = null,
                                tint = CmpTheme.colors.brandPrimaryDefault,
                                modifier = Modifier.size(CmpSize.iconM),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetHalf(mode: ThemeModeSpec, modifier: Modifier) {
    val bar = RoundedCornerShape(SampleBar / 2)
    Box(modifier = modifier.height(SampleHeight).background(mode.palette.background.toColor())) {
        Column(
            modifier = Modifier.padding(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            Box(Modifier.fillMaxWidth().height(SampleBar).background(mode.palette.ink.toColor(), bar))
            Box(
                Modifier.fillMaxWidth(
                    fraction = 0.6f,
                ).height(SampleBar * 2).background(mode.palette.primary.toColor(), bar),
            )
        }
    }
}
