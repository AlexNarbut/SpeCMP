package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_cap_a11y_note
import specmp.feature.vehicle_ui.generated.resources.vehicle_cap_label_no_data
import specmp.feature.vehicle_ui.generated.resources.vehicle_cap_label_not_supported
import specmp.feature.vehicle_ui.generated.resources.vehicle_cap_label_supported

private const val SUCCESS_TINT = 0.22f
private const val SUCCESS_ICON_MIX = 0.3f

/** Примечание возможности (HTML) или `null`. */
internal val CarCapability.note: String?
    get() = when (this) {
        CarCapability.NoData -> null
        is CarCapability.Supported -> note
        is CarCapability.NotSupported -> note
    }

/**
 * Бейдж возможности автомобиля: цвет + иконка + текст («поддерживается» / «не поддерживается» / «нет данных»).
 * Есть примечание — иконка ⓘ, нажатие открывает его. [tile] — плитка (широкое окно), иначе строка во всю ширину.
 */
@Composable
internal fun CapBadge(
    label: String,
    capability: CarCapability,
    onNote: () -> Unit,
    modifier: Modifier = Modifier,
    tile: Boolean = false
) {
    val colors = cmpThemeColors()
    val success = cmpDomainColors().success
    val (icon, text) = when (capability) {
        is CarCapability.Supported -> CmpIcons.CheckCircle to stringResource(Res.string.vehicle_cap_label_supported)
        is CarCapability.NotSupported -> CmpIcons.Cancel to stringResource(Res.string.vehicle_cap_label_not_supported)
        CarCapability.NoData -> CmpIcons.HelpOutline to stringResource(Res.string.vehicle_cap_label_no_data)
    }
    val (background, border, tint) = when (capability) {
        is CarCapability.Supported -> Triple(
            lerp(colors.surface, success, SUCCESS_TINT),
            success,
            lerp(success, colors.onSurface, SUCCESS_ICON_MIX)
        )

        is CarCapability.NotSupported -> Triple(colors.errorContainer, colors.error, colors.error)

        CarCapability.NoData -> Triple(colors.surfaceContainerHigh, colors.outline, colors.onSurfaceVariant)
    }
    val hasNote = capability.note != null
    val shape = RoundedCornerShape(14.dp)
    val clickable = if (hasNote) {
        Modifier.clickable(
            role = Role.Button,
            onClickLabel = stringResource(Res.string.vehicle_cap_a11y_note),
            onClick = onNote
        )
    } else {
        Modifier
    }
    CmpBox(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .then(clickable)
            .heightIn(min = if (tile) 112.dp else 56.dp)
    ) {
        val labels: @Composable () -> Unit = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = label,
                    style = CmpTextStyle.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                CmpText(text = text, style = CmpTextStyle.Body, color = colors.onSurface)
            }
        }
        val info: @Composable () -> Unit = {
            if (hasNote) {
                CmpIcon(
                    icon = CmpIcons.Info,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (tile) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                    info()
                }
                labels()
            }
        } else {
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                CmpBox(modifier = Modifier.weight(1f)) { labels() }
                info()
            }
        }
    }
}
