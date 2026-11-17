package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_a11y_note
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_no_data
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_not_supported
import ru.alarmtrade.specmp.core.localization.resources.vehicle_cap_label_supported
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability

internal val CapTileMinHeight = 112.dp
private val CapRowMinHeight = 64.dp

internal val CarCapability.note: String?
    get() = when (this) {
        CarCapability.NoData -> null
        is CarCapability.Supported -> note
        is CarCapability.NotSupported -> note
    }

@Composable
internal fun CapBadge(
    label: String,
    capability: CarCapability,
    onNote: () -> Unit,
    modifier: Modifier = Modifier,
    tile: Boolean = false,
) {
    val colors = CmpTheme.colors
    val (icon, text) = when (capability) {
        is CarCapability.Supported -> CmpIcons.CheckCircle to stringResource(Strings.string.vehicle_cap_label_supported)

        is CarCapability.NotSupported ->
            CmpIcons.Cancel to
                stringResource(Strings.string.vehicle_cap_label_not_supported)

        CarCapability.NoData -> CmpIcons.HelpOutline to stringResource(Strings.string.vehicle_cap_label_no_data)
    }
    val (tone, tint) = when (capability) {
        is CarCapability.Supported -> CmpTagTone.Success to colors.successDefault
        is CarCapability.NotSupported -> CmpTagTone.Error to colors.errorDefault
        CarCapability.NoData -> CmpTagTone.Neutral to colors.foregroundOnDarkSecondaryDefault
    }
    val hasNote = capability.note != null
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    val noteLabel = stringResource(Strings.string.vehicle_cap_a11y_note)
    val clickable = if (hasNote) {
        Modifier
            .cmpPressable(onClick = onNote, shape = shape)
            .semantics {
                onClick(label = noteLabel) {
                    onNote()
                    true
                }
            }
    } else {
        Modifier
    }
    CmpBox(
        modifier = modifier
            .then(clickable)
            .background(colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .heightIn(min = if (tile) CapTileMinHeight else CapRowMinHeight),
    ) {
        val labels: @Composable () -> Unit = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(text = label, style = CmpTextStyle.Headline3, color = colors.foregroundOnDarkPrimaryDefault)
                CmpTag(text = text, tone = tone, maxLines = 2)
            }
        }
        if (tile) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusIcon(icon = icon, tint = tint)
                    if (hasNote) NoteIcon()
                }
                labels()
            }
        } else {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusIcon(icon = icon, tint = tint)
                CmpBox(modifier = Modifier.weight(1f)) { labels() }
                if (hasNote) NoteIcon()
            }
        }
    }
}

@Composable
private fun StatusIcon(icon: CmpIcons, tint: Color) {
    CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(CmpSize.iconL))
}

@Composable
private fun NoteIcon() {
    CmpIcon(
        icon = CmpIcons.Info,
        contentDescription = null,
        tint = CmpTheme.colors.brandPrimaryDefault,
        modifier = Modifier.size(CmpSize.iconM),
    )
}
