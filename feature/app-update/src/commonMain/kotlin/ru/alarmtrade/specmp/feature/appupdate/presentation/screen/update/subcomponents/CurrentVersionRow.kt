package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.background
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateState

private val ChipShape = RoundedCornerShape(10.dp)

/** «Установлена версия 2.1.0 (249) · база настроек s1234» и, если каналов несколько, «Канал: …». */
@Composable
internal fun CurrentVersionRow(state: AppUpdateState, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpFlowRow(
        modifier = modifier.heightIn(min = 48.dp).testTag(AppUpdateTags.CURRENT),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            CmpText(
                text = stringResource(Strings.string.appupdate_current_label_installed),
                style = CmpTextStyle.Headline3,
                color = colors.onSurfaceVariant,
            )
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
        val channel = state.selectedChannel
        if (state.showChannels && channel != null) {
            Row(
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .background(colors.secondaryContainer, ChipShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(
                    icon = CmpIcons.Layers,
                    contentDescription = null,
                    tint = colors.onSecondaryContainer,
                    modifier = Modifier.size(20.dp),
                )
                CmpText(
                    text = stringResource(Strings.string.appupdate_current_label_channel, channel.name),
                    style = CmpTextStyle.Headline3,
                    color = colors.onSecondaryContainer,
                )
            }
        }
    }
}
