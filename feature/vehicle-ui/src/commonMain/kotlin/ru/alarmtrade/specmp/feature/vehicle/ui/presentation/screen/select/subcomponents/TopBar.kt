package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.vehicle_filter_a11y_open
import ru.alarmtrade.specmp.core.localization.resources.vehicle_filter_a11y_open_active
import ru.alarmtrade.specmp.core.localization.resources.vehicle_select_subtitle_device
import ru.alarmtrade.specmp.core.localization.resources.vehicle_select_title_pick
import ru.alarmtrade.specmp.core.localization.resources.vehicle_select_title_view
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.typeChipLabel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.typeLabel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectState

/**
 * Шапка: «назад», заголовок по режиму («Выберите автомобиль» / «Автомобили»), подзаголовок — модель блока и
 * CAN-код прошивки или выбранный тип устройства; справа — чип типа устройства (широкое окно) и фильтр функций.
 */
@Composable
internal fun TopBar(state: VehicleSelectState, wide: Boolean, actions: VehicleSelectActions) {
    val mode = state.mode
    val subtitle = subtitle(state, wide)
    CmpTopAppBar(
        title = {
            CmpText(
                text = stringResource(
                    if (mode.pick) {
                        Strings.string.vehicle_select_title_pick
                    } else {
                        Strings.string.vehicle_select_title_view
                    },
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = subtitle?.let { { CmpText(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.vehicle_common_a11y_back),
            )
        },
        actions = {
            if (!state.showTypeChoice) {
                val type = state.deviceType
                if (wide && mode.typeSelectable && type != null) TypeChip(type = type, onPick = actions.onTypePick)
                if (mode.filterAvailable) FilterButton(active = state.groups.size, onClick = actions.onFilterOpen)
            }
        },
    )
}

/** Модель блока и CAN-код прошивки или выбранный тип (на узком окне тип виден чипом под поиском — не дублируем). */
@Composable
private fun subtitle(state: VehicleSelectState, wide: Boolean): String? {
    val mode = state.mode
    return when {
        mode.deviceName != null && mode.firmwareCanCode != null ->
            stringResource(Strings.string.vehicle_select_subtitle_device, mode.deviceName, mode.firmwareCanCode)

        mode.deviceName != null -> mode.deviceName

        mode.typeSelectable && wide -> state.deviceType?.let { typeLabel(it) }

        else -> null
    }
}

@Composable
internal fun TypeChip(type: VehicleDeviceType, onPick: (VehicleDeviceType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = typeChipLabel(type)
    Box {
        CmpChip(
            text = label,
            onClick = { expanded = true },
            icon = if (type == VehicleDeviceType.ALARM) CmpIcons.Security else CmpIcons.Memory,
            modifier = Modifier.testTag(VehicleTestTags.TYPE_CHIP),
        )
        CmpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            VehicleDeviceType.entries.forEach { option ->
                CmpDropdownMenuItem(
                    text = typeLabel(option),
                    selected = option == type,
                    onClick = {
                        expanded = false
                        if (option != type) onPick(option)
                    },
                )
            }
        }
    }
}

@Composable
private fun FilterButton(active: Int, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        CmpIconButton(
            onClick = onClick,
            icon = CmpIcons.FilterList,
            contentDescription = if (active > 0) {
                stringResource(Strings.string.vehicle_filter_a11y_open_active, active)
            } else {
                stringResource(Strings.string.vehicle_filter_a11y_open)
            },
            modifier = Modifier.testTag(VehicleTestTags.FILTER),
        )
        if (active > 0) CmpDot(color = cmpThemeColors().error, modifier = Modifier.offset(x = (-8).dp, y = 8.dp))
    }
}
