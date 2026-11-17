package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_btn_start
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_error_vin
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_label_car
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_label_device
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_label_vin
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_msg_firmware
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_status_online
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_title
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_value_can
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_value_lin
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_value_scheme
import ru.alarmtrade.specmp.core.localization.resources.appearance_preview_value_vin
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeProvider
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPageBackground
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Образцы компонентов в черновике темы: рамка — в теме приложения, содержимое — в [mode]. */
@Composable
internal fun ThemePreview(mode: ThemeModeSpec, isDark: Boolean, modifier: Modifier = Modifier) {
    val frame = RoundedCornerShape(CmpRadius.cardLarge)
    val frameBorder = CmpTheme.colors.borderHighlight
    CmpThemeProvider(mode, isDark) {
        val colors = CmpTheme.colors
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(frame)
                .cmpPageBackground()
                .cmpBorder(CmpBorderWidth.listItem, frameBorder, frame)
                .padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpText(
                text = stringResource(Strings.string.appearance_preview_title),
                style = CmpTextStyle.Title2,
                color = colors.foregroundOnDarkPrimaryDefault,
            )
            PreviewDeviceCard()
            CmpTextField(
                value = stringResource(Strings.string.appearance_preview_value_vin),
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                isError = true,
                label = stringResource(Strings.string.appearance_preview_label_vin),
                supportingText = {
                    CmpText(
                        text = stringResource(Strings.string.appearance_preview_error_vin),
                        style = CmpTextStyle.Paragraph3,
                        color = colors.errorDefault,
                    )
                },
            )
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpChip(text = stringResource(Strings.string.appearance_preview_value_can), onClick = {
                }, selected = true)
                CmpChip(text = stringResource(Strings.string.appearance_preview_value_lin), onClick = {})
                CmpChip(
                    text = stringResource(Strings.string.appearance_preview_value_scheme),
                    onClick = {},
                    icon = CmpIcons.Info,
                )
            }
            CmpButton(
                text = stringResource(Strings.string.appearance_preview_btn_start),
                onClick = {},
                type = CmpButtonType.Accent,
                icon = CmpIcons.PlayArrow,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun PreviewDeviceCard() {
    val colors = CmpTheme.colors
    CmpCard {
        Column(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                    CmpText(
                        text = stringResource(Strings.string.appearance_preview_label_device),
                        style = CmpTextStyle.Headline2,
                        color = colors.foregroundOnDarkPrimaryDefault,
                    )
                    CmpText(
                        text = stringResource(Strings.string.appearance_preview_label_car),
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                CmpTag(
                    text = stringResource(Strings.string.appearance_preview_status_online),
                    tone = CmpTagTone.Success,
                    icon = CmpIcons.CheckCircle,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpIcon(
                    icon = CmpIcons.SystemUpdate,
                    contentDescription = null,
                    tint = colors.infoDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
                CmpText(
                    text = stringResource(Strings.string.appearance_preview_msg_firmware),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.infoDefault,
                )
            }
        }
    }
}
