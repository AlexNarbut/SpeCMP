package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_current_label_channel
import ru.alarmtrade.specmp.core.localization.resources.appupdate_current_label_installed
import ru.alarmtrade.specmp.core.localization.resources.appupdate_current_label_settings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_current_value_version
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateState

private val ChipShape = RoundedCornerShape(CmpRadius.tags)
private val ChipMinHeight = 36.dp

@Composable
internal fun CurrentVersionRow(state: AppUpdateState, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpFlowRow(
        modifier = modifier.heightIn(min = CmpSize.touchMin).testTag(AppUpdateTags.CURRENT),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            CmpText(
                text = stringResource(Strings.string.appupdate_current_label_installed),
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                CmpText(
                    text = stringResource(
                        Strings.string.appupdate_current_value_version,
                        state.installedVersion,
                        state.installedCode,
                    ),
                    style = CmpTextStyle.Headline1,
                )
                state.process.overview?.settingsVersion?.let { version ->
                    CmpText(
                        text = stringResource(Strings.string.appupdate_current_label_settings, version.toString()),
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
        }
        val channel = state.selectedChannel
        if (state.showChannels && channel != null) {
            Row(
                modifier = Modifier
                    .heightIn(min = ChipMinHeight)
                    .background(colors.backgroundSurfaceHighlight, ChipShape)
                    .border(CmpBorderWidth.tags, colors.borderHighlight, ChipShape)
                    .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space4),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(
                    icon = CmpIcons.Layers,
                    contentDescription = null,
                    tint = colors.foregroundOnDarkPrimaryDefault,
                    modifier = Modifier.size(CmpSize.iconM),
                )
                CmpText(
                    text = stringResource(Strings.string.appupdate_current_label_channel, channel.name),
                    style = CmpTextStyle.Headline3,
                    color = colors.foregroundOnDarkPrimaryDefault,
                )
            }
        }
    }
}
