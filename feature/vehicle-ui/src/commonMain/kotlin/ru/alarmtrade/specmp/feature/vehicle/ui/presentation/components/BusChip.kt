package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_bean
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can1
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can1_fd
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can2
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can2_fd
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can3
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can3_fd
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can4
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can4_fd
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can_fd1_or_fd3
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_can_fd2_or_fd4
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_imo_key1
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_imo_key1_key2
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_imo_key2
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_lin
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_lin_in_out
import ru.alarmtrade.specmp.core.localization.resources.vehicle_bus_label_unknown
import ru.alarmtrade.specmp.core.ui.theme.CmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.BusFamily
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBus

private const val TINT_ALPHA = 0.14f

@Composable
internal fun BusChip(bus: CarBus, modifier: Modifier = Modifier) {
    val color = busColor(bus.family, cmpDomainColors())
    val shape = RoundedCornerShape(8.dp)
    val frame = if (bus.family == BusFamily.UNKNOWN) {
        Modifier.drawBehind {
            drawRoundRect(
                color = color,
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))),
            )
        }
    } else {
        Modifier.drawBehind {
            drawRoundRect(color = color.copy(alpha = TINT_ALPHA), cornerRadius = CornerRadius(8.dp.toPx()))
        }
            .border(2.dp, color, shape)
    }
    CmpBox(
        modifier = modifier.heightIn(min = 32.dp).then(frame).padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(
            text = stringResource(busLabel(bus)),
            style = CmpTextStyle.Headline3,
            color = cmpThemeColors().onSurface,
        )
    }
}

internal fun busColor(family: BusFamily, colors: CmpDomainColors): Color = when (family) {
    BusFamily.CAN -> colors.busCan
    BusFamily.CAN_FD -> colors.busCanFd
    BusFamily.LIN -> colors.busLin
    BusFamily.BEAN -> colors.busBean
    BusFamily.IMO -> colors.busImmobilizer
    BusFamily.UNKNOWN -> colors.busUnknown
}

@Suppress("CyclomaticComplexMethod") // плоская таблица подписей
internal fun busLabel(bus: CarBus): StringResource = when (bus) {
    CarBus.CAN1 -> Strings.string.vehicle_bus_label_can1
    CarBus.CAN2 -> Strings.string.vehicle_bus_label_can2
    CarBus.CAN3 -> Strings.string.vehicle_bus_label_can3
    CarBus.CAN4 -> Strings.string.vehicle_bus_label_can4
    CarBus.CAN1_FD -> Strings.string.vehicle_bus_label_can1_fd
    CarBus.CAN2_FD -> Strings.string.vehicle_bus_label_can2_fd
    CarBus.CAN3_FD -> Strings.string.vehicle_bus_label_can3_fd
    CarBus.CAN4_FD -> Strings.string.vehicle_bus_label_can4_fd
    CarBus.CAN_FD1_OR_CAN_FD3 -> Strings.string.vehicle_bus_label_can_fd1_or_fd3
    CarBus.CAN_FD2_OR_CAN_FD4 -> Strings.string.vehicle_bus_label_can_fd2_or_fd4
    CarBus.LIN -> Strings.string.vehicle_bus_label_lin
    CarBus.LIN_IN_OUT -> Strings.string.vehicle_bus_label_lin_in_out
    CarBus.BEAN -> Strings.string.vehicle_bus_label_bean
    CarBus.IMO_KEY1 -> Strings.string.vehicle_bus_label_imo_key1
    CarBus.IMO_KEY2 -> Strings.string.vehicle_bus_label_imo_key2
    CarBus.IMO_KEY1_PLUS_IMO_KEY2 -> Strings.string.vehicle_bus_label_imo_key1_key2
    CarBus.UNKNOWN -> Strings.string.vehicle_bus_label_unknown
}
