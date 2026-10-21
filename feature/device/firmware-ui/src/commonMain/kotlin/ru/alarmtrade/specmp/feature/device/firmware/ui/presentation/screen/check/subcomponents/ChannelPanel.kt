package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.ChannelSegments
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLayout
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_label_channel
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_msg_test_channel

private val SegmentsWidth = 380.dp

/** Канал обновления один на экран: подпись, переключатель и предупреждение для тестовых каналов. */
@Composable
internal fun ChannelPanel(
    channel: FirmwareChannel,
    layout: FirmwareLayout,
    onSelect: (FirmwareChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    val label: @Composable () -> Unit = {
        CmpText(
            text = stringResource(Res.string.firmware_common_label_channel),
            color = cmpThemeColors().onSurfaceVariant,
            style = CmpTextStyle.Body
        )
    }
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (layout == FirmwareLayout.Compact) {
            label()
            ChannelSegments(selected = channel, onSelect = onSelect, modifier = Modifier)
        } else {
            CmpRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {
                label()
                ChannelSegments(selected = channel, onSelect = onSelect, modifier = Modifier.width(SegmentsWidth))
            }
        }
        if (channel.isTest) {
            InfoBanner(text = stringResource(Res.string.firmware_common_msg_test_channel))
        }
    }
}
