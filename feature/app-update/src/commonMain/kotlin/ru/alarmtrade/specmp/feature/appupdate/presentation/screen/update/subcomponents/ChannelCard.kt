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
import androidx.compose.ui.graphics.Color
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.domain.model.AppUpdateChannel
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateState

private val RowShape = RoundedCornerShape(CmpRadius.listItem)
private val RowMinHeight = 64.dp
private const val LOCKED_ALPHA = 0.5f

@Composable
internal fun ChannelCard(state: AppUpdateState, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    UpdateCard(
        modifier = modifier.testTag(AppUpdateTags.CHANNELS),
        padding = PaddingValues(
            start = CmpSpacing.space16,
            top = CmpSpacing.space20,
            end = CmpSpacing.space16,
            bottom = CmpSpacing.space16,
        ),
        spacing = CmpSpacing.space12,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            CmpText(text = stringResource(Strings.string.appupdate_channels_title), style = CmpTextStyle.Headline1)
            CmpText(
                text = stringResource(Strings.string.appupdate_channels_msg),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
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
    val colors = CmpTheme.colors
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
            .heightIn(min = RowMinHeight)
            .clip(RowShape)
            .background(if (selected) colors.brandPrimaryHighlight else Color.Transparent)
            .selectable(selected = selected, enabled = !locked, role = Role.RadioButton, onClick = onSelect)
            .alpha(if (locked && !selected) LOCKED_ALPHA else 1f)
            .testTag(AppUpdateTags.channel(channel.id))
            .padding(top = CmpSpacing.space8, end = CmpSpacing.space12, bottom = CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        CmpRadioButton(selected = selected, onClick = null, enabled = !locked)
        Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = channel.name, style = CmpTextStyle.Headline2)
            description?.let {
                CmpText(text = it, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkSecondaryDefault)
            }
        }
    }
}
