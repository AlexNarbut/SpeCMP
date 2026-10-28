package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSelectableCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.typeLabel
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_type_title_screen

/**
 * Выбор типа устройства, когда блок не подключён: каталог фильтруется под сигнализацию или микробот.
 * Узкое окно — карточки строками, широкое — две крупные карточки рядом.
 */
@Composable
internal fun TypeChoice(wide: Boolean, onPick: (VehicleDeviceType) -> Unit, modifier: Modifier = Modifier) {
    CmpColumn(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CmpText(text = stringResource(Res.string.vehicle_type_title_screen), style = CmpTextStyle.Title)
        if (wide) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VehicleDeviceType.entries.forEach { type ->
                    TypeCard(type = type, wide = true, onClick = {
                        onPick(type)
                    }, modifier = Modifier.widthIn(max = 320.dp).weight(1f))
                }
            }
        } else {
            VehicleDeviceType.entries.forEach { type ->
                TypeCard(type = type, wide = false, onClick = { onPick(type) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TypeCard(type: VehicleDeviceType, wide: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val icon = if (type == VehicleDeviceType.ALARM) CmpIcons.Security else CmpIcons.Memory
    val tint = cmpThemeColors().primary
    CmpSelectableCard(
        selected = false,
        onClick = onClick,
        modifier = modifier.heightIn(
            min = if (wide) 200.dp else 96.dp
        ).testTag(VehicleTestTags.type(type.name.lowercase()))
    ) {
        if (wide) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(72.dp))
                CmpText(text = typeLabel(type), style = CmpTextStyle.BodyLarge, textAlign = TextAlign.Center)
            }
        } else {
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(48.dp))
                CmpText(text = typeLabel(type), style = CmpTextStyle.BodyLarge)
            }
        }
    }
}
