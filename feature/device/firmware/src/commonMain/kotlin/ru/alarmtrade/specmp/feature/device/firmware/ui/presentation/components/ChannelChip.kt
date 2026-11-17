package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_channel
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_label_channel
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_alpha
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_alpha_short
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_beta
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_beta_short
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_release
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_channel_release_short
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel

private const val TEST_BORDER_ALPHA = 0.3f

internal const val CHANNEL_CHIP_TAG = "firmware_channel_chip"

internal fun channelTag(channel: FirmwareChannel): String = "firmware_channel_${channel.name.lowercase()}"

private fun FirmwareChannel.shortLabel(): StringResource = when (this) {
    FirmwareChannel.RELEASE -> Strings.string.firmware_common_value_channel_release_short
    FirmwareChannel.BETA -> Strings.string.firmware_common_value_channel_beta_short
    FirmwareChannel.ALPHA -> Strings.string.firmware_common_value_channel_alpha_short
}

private fun FirmwareChannel.longLabel(): StringResource = when (this) {
    FirmwareChannel.RELEASE -> Strings.string.firmware_common_value_channel_release
    FirmwareChannel.BETA -> Strings.string.firmware_common_value_channel_beta
    FirmwareChannel.ALPHA -> Strings.string.firmware_common_value_channel_alpha
}

@Composable
internal fun ChannelChip(
    selected: FirmwareChannel,
    onSelect: (FirmwareChannel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    var open by remember { mutableStateOf(false) }
    val test = selected.isTest
    val a11y = stringResource(Strings.string.firmware_common_label_channel)
    CmpBox(modifier = modifier) {
        CmpClickableSurface(
            onClick = { open = true },
            modifier = Modifier
                .height(CmpSize.chip)
                .testTag(CHANNEL_CHIP_TAG)
                .semantics {
                    role = Role.DropdownList
                    contentDescription = a11y
                },
            shape = CircleShape,
            color = if (test) colors.warningHighlight else Color.Transparent,
            contentColor = if (test) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkSecondaryDefault,
            border = BorderStroke(
                CmpBorderWidth.chips,
                if (test) colors.warningDefault.copy(alpha = TEST_BORDER_ALPHA) else colors.borderLight,
            ),
            enforceMinimumTouchTarget = false,
        ) {
            CmpRow(
                modifier = Modifier.padding(start = CmpSpacing.space16, end = CmpSpacing.space12),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (test) {
                    CmpIcon(
                        icon = CmpIcons.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconS),
                        tint = colors.warningDefault,
                    )
                }
                CmpText(
                    text = stringResource(
                        Strings.string.firmware_check_label_channel,
                        stringResource(selected.shortLabel()),
                    ),
                    style = CmpTextStyle.Additional0,
                    maxLines = 1,
                )
                CmpIcon(
                    icon = if (open) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(CmpSize.iconM),
                )
            }
        }
        CmpDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            FirmwareChannel.entries.forEach { channel ->
                CmpDropdownMenuItem(
                    text = stringResource(channel.longLabel()),
                    selected = channel == selected,
                    onClick = {
                        open = false
                        if (channel != selected) onSelect(channel)
                    },
                    modifier = Modifier.testTag(channelTag(channel)),
                )
            }
        }
    }
}
