package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_type_title_screen
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.typeLabel

private val WideCardMaxWidth = 320.dp
private val WideCardMinHeight = 200.dp
private val WideIconCircle = 96.dp

@Composable
internal fun TypeChoice(wide: Boolean, onPick: (VehicleDeviceType) -> Unit, modifier: Modifier = Modifier) {
    CmpColumn(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = if (wide) CmpSpacing.screenPaddingHMedium else CmpSpacing.screenPaddingH,
            vertical = CmpSpacing.space16,
        ),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpText(text = stringResource(Strings.string.vehicle_type_title_screen), style = CmpTextStyle.Title2)
        if (wide) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
                VehicleDeviceType.entries.forEach { type ->
                    TypeCard(type = type, wide = true, onClick = {
                        onPick(type)
                    }, modifier = Modifier.widthIn(max = WideCardMaxWidth).weight(1f))
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
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.heightIn(
            min = if (wide) WideCardMinHeight else CmpSize.rowSection,
        ).testTag(VehicleTestTags.type(type.name.lowercase())),
    ) {
        if (wide) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space24),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                TypeIcon(icon = icon, circle = WideIconCircle, size = CmpSize.iconXxl)
                CmpText(text = typeLabel(type), style = CmpTextStyle.Headline2, textAlign = TextAlign.Center)
            }
        } else {
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space16),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TypeIcon(icon = icon, circle = CmpSize.iconXxl, size = CmpSize.iconL)
                CmpText(text = typeLabel(type), style = CmpTextStyle.Headline2, modifier = Modifier.weight(1f))
                CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(CmpSize.iconL),
                )
            }
        }
    }
}

@Composable
private fun TypeIcon(icon: CmpIcons, circle: Dp, size: Dp) {
    val colors = CmpTheme.colors
    CmpBox(
        modifier = Modifier.size(circle).background(colors.brandPrimaryHighlight, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.brandPrimaryDefault,
            modifier = Modifier.size(size),
        )
    }
}
