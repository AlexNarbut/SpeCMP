package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_channels_hint_beta
import ru.alarmtrade.specmp.core.localization.resources.appupdate_channels_label_empty
import ru.alarmtrade.specmp.core.localization.resources.appupdate_channels_label_installed
import ru.alarmtrade.specmp.core.localization.resources.appupdate_channels_label_version
import ru.alarmtrade.specmp.core.localization.resources.appupdate_channels_msg
import ru.alarmtrade.specmp.core.localization.resources.appupdate_channels_title
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateChannel
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateState

private val RowShape = RoundedCornerShape(12.dp)
private const val SELECTED_TINT_ALPHA = 0.08f
private const val LOCKED_ALPHA = 0.5f

@Composable
internal fun ChannelCard(state: AppUpdateState, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    UpdateCard(
        modifier = modifier.testTag(AppUpdateTags.CHANNELS),
        padding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp),
        spacing = 12.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = stringResource(Strings.string.appupdate_channels_title), style = CmpTextStyle.Title)
            CmpText(
                text = stringResource(Strings.string.appupdate_channels_msg),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.selectableGroup()) {
            state.channels.forEach { channel ->
                ChannelRow(
                    channel = channel,
                    selected = channel.id == state.selectedChannel?.id,
                    locked = state.channelsLocked,
                    installedCode = state.installedCode,
                    onSelect = { onSelect(channel.id) },
                )
            }
        }
        InfoNote(text = stringResource(Strings.string.appupdate_channels_hint_beta), icon = CmpIcons.Science)
    }
}

@Composable
private fun ChannelRow(
    channel: AppUpdateChannel,
    selected: Boolean,
    locked: Boolean,
    installedCode: Int,
    onSelect: () -> Unit,
) {
    val colors = cmpThemeColors()
    val latest = channel.latest
    val description = when {
        latest != null && latest.versionCode == installedCode -> stringResource(
            Strings.string.appupdate_channels_label_installed,
            latest.version,
            latest.publishedAt.formatted(),
        )

        latest != null -> stringResource(
            Strings.string.appupdate_channels_label_version,
            latest.version,
            latest.publishedAt.formatted(),
        )

        channel.isEmpty -> stringResource(Strings.string.appupdate_channels_label_empty)

        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RowShape)
            .background(if (selected) colors.primary.copy(alpha = SELECTED_TINT_ALPHA) else colors.surfaceContainerLow)
            .selectable(selected = selected, enabled = !locked, role = Role.RadioButton, onClick = onSelect)
            .alpha(if (locked && !selected) LOCKED_ALPHA else 1f)
            .testTag(AppUpdateTags.channel(channel.id))
            .padding(top = 6.dp, end = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CmpRadioButton(selected = selected, onClick = null, enabled = !locked)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = channel.name, style = CmpTextStyle.TitleMedium)
            description?.let { CmpText(text = it, style = CmpTextStyle.Body, color = colors.onSurfaceVariant) }
        }
    }
}
