package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_channel_alpha_short
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_channel_beta_short
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_channel_release_short

/** testTag сегмента канала: `firmware_channel_<release|beta|alpha>`. */
internal fun channelTag(channel: FirmwareChannel): String = "firmware_channel_${channel.name.lowercase()}"

private fun FirmwareChannel.shortLabel(): StringResource = when (this) {
    FirmwareChannel.RELEASE -> Res.string.firmware_common_channel_release_short
    FirmwareChannel.BETA -> Res.string.firmware_common_channel_beta_short
    FirmwareChannel.ALPHA -> Res.string.firmware_common_channel_alpha_short
}

/** Переключатель канала обновления: три равные кнопки ≥ 48 dp в «таблетке». */
@Composable
internal fun ChannelSegments(
    selected: FirmwareChannel,
    onSelect: (FirmwareChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    CmpSurface(modifier = modifier, shape = CircleShape, color = colors.surfaceContainerHigh) {
        CmpRow(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FirmwareChannel.entries.forEach { channel ->
                val isSelected = channel == selected
                CmpClickableSurface(
                    onClick = { onSelect(channel) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag(channelTag(channel))
                        .semantics {
                            role = Role.RadioButton
                            this.selected = isSelected
                        },
                    shape = CircleShape,
                    color = if (isSelected) colors.primary else Color.Transparent,
                    contentColor = if (isSelected) colors.onPrimary else colors.onSurface,
                    enforceMinimumTouchTarget = false
                ) {
                    CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CmpText(
                            text = stringResource(channel.shortLabel()),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
