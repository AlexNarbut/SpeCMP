package ru.alarmtrade.specmp.feature.device.state.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_format_on
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_format_open
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_format_triggered
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_status_all_closed
import ru.alarmtrade.specmp.core.localization.resources.devicestate_zones_status_normal
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneGroup
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue

internal const val ZONE_TAG_PREFIX = "devicestate_zone_"

@Composable
internal fun ZonesList(vehicle: VehicleState, changed: Set<Zone>, tick: Int, modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        ZoneGroup.entries.forEach { group ->
            val zones = Zone.entries.filter { it.group == group }
            SectionCard(
                title = stringResource(group.title),
                aside = groupAside(group, zones, vehicle),
                contentPadding = CmpSpacing.space8,
            ) {
                CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                    zones.forEach { zone ->
                        ZoneRow(zone = zone, value = vehicle.zone(zone), changed = zone in changed, tick = tick)
                    }
                }
            }
        }
    }
}

@Composable
private fun groupAside(group: ZoneGroup, zones: List<Zone>, vehicle: VehicleState): String? {
    val active = zones.count { vehicle.zone(it).tone.let { tone -> tone == ZoneTone.ACTIVE || tone == ZoneTone.ALARM } }
    return when (group) {
        ZoneGroup.BODY -> if (active > 0) {
            stringResource(Strings.string.devicestate_zones_format_open, active)
        } else {
            stringResource(Strings.string.devicestate_zones_status_all_closed)
        }

        ZoneGroup.SENSORS -> if (active > 0) {
            stringResource(Strings.string.devicestate_zones_format_triggered, active)
        } else {
            stringResource(Strings.string.devicestate_zones_status_normal)
        }

        ZoneGroup.CONTROLS -> if (active >
            0
        ) {
            stringResource(Strings.string.devicestate_zones_format_on, active)
        } else {
            null
        }
    }
}

@Composable
private fun ZoneRow(zone: Zone, value: ZoneValue, changed: Boolean, tick: Int) {
    val theme = CmpTheme.colors
    val tone = value.tone
    val colors = theme.tone(tone)
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpBox(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.row, shape)
            .testTag(ZONE_TAG_PREFIX + zone.name.lowercase()),
    ) {
        ChangeFlash(
            on = changed,
            key = tick,
            color = if (tone ==
                ZoneTone.ALARM
            ) {
                theme.errorDefault
            } else {
                theme.brandPrimaryDefault
            },
        )
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(
                min = ZoneRowMinHeight,
            ).padding(horizontal = CmpSpacing.space8, vertical = CmpSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpBox(
                modifier = Modifier.size(CmpSize.iconXl).background(colors.icon, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = zone.icon,
                    contentDescription = null,
                    tint = colors.iconTint,
                    modifier = Modifier.size(CmpSize.iconM),
                )
            }
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = stringResource(zone.label),
                    color = colors.text,
                    style = if (tone == ZoneTone.REST ||
                        tone == ZoneTone.MUTED
                    ) {
                        CmpTextStyle.Paragraph2
                    } else {
                        CmpTextStyle.Headline3
                    },
                )
                if (changed) ChangedTag()
            }
            ValuePill(text = zoneValueText(zone, value), tone = tone)
        }
    }
}
private val ZoneRowMinHeight = 64.dp
