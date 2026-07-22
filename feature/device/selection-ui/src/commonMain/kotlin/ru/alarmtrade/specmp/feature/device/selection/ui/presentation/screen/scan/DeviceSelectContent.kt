package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import specmp.feature.device.selection_ui.generated.resources.Res
import specmp.feature.device.selection_ui.generated.resources.device_select_transport_ble
import specmp.feature.device.selection_ui.generated.resources.device_select_transport_usb

// M3-подобные размеры типографики (CmpText не принимает variant).
internal object DsType {
    val HeadlineSmall = 24.sp
    val TitleMedium = 16.sp
    val TitleSmall = 14.sp
    val BodyLarge = 16.sp
    val BodyMedium = 14.sp
    val LabelLarge = 14.sp
}

internal enum class HeroTone { Primary, Error, Neutral }

@Composable
internal fun transportLabel(transport: DeviceTransport): String = when (transport) {
    DeviceTransport.BLE -> stringResource(Res.string.device_select_transport_ble)
    DeviceTransport.USB -> stringResource(Res.string.device_select_transport_usb)
}

internal fun DeviceTransport.icon(): CmpIcons = when (this) {
    DeviceTransport.BLE -> CmpIcons.Bluetooth
    DeviceTransport.USB -> CmpIcons.Usb
}

/** Плейсхолдер-слот фото устройства: нейтральный фон + иконка типа (реального фото пока нет). */
@Composable
internal fun DeviceTile(icon: CmpIcons, sizeDp: Int) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.size(sizeDp.dp),
        shape = RoundedCornerShape(if (sizeDp >= 96) 20.dp else 12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/** Чип транспорта (Bluetooth / USB). */
@Composable
internal fun TransportChip(transport: DeviceTransport) {
    val colors = cmpThemeColors()
    CmpSurface(
        shape = CircleShape,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(icon = transport.icon(), contentDescription = null, tint = colors.onSecondaryContainer)
            CmpText(text = transportLabel(transport), fontSize = DsType.BodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

/** Строка устройства в списке. */
@Composable
internal fun DeviceRow(
    item: DeviceUiItem,
    selected: Boolean,
    onClick: () -> Unit,
    tileSize: Int,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DeviceTile(icon = item.typeIcon, sizeDp = tileSize)
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(text = item.name, fontSize = DsType.TitleMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                CmpRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TransportChip(transport = item.transport)
                    CmpText(
                        text = item.address,
                        fontSize = DsType.BodyMedium,
                        color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
        }
    }
}

/** Круглый герой-значок состояний. */
@Composable
internal fun Hero(icon: CmpIcons, tone: HeroTone = HeroTone.Primary) {
    val colors = cmpThemeColors()
    val bg = when (tone) {
        HeroTone.Primary -> colors.primaryContainer
        HeroTone.Error -> colors.errorContainer
        HeroTone.Neutral -> colors.surfaceVariant
    }
    val fg = when (tone) {
        HeroTone.Primary -> colors.onPrimaryContainer
        HeroTone.Error -> colors.onErrorContainer
        HeroTone.Neutral -> colors.onSurfaceVariant
    }
    CmpSurface(modifier = Modifier.size(96.dp), shape = CircleShape, color = bg) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, tint = fg)
        }
    }
}

/** Обёртка центрированных состояний (прокручиваемая). */
@Composable
internal fun CenterState(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) { content() }
    }
}

/** Строка разрешения в экране-обосновании. */
@Composable
internal fun PermissionRow(icon: CmpIcons, label: String, desc: String) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpSurface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
            ) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpIcon(icon = icon, contentDescription = null, tint = colors.onSecondaryContainer)
                }
            }
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(text = label, fontSize = DsType.TitleSmall, fontWeight = FontWeight.Medium)
                CmpText(text = desc, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** Список подсказок «как починить» / «почему пусто». */
@Composable
internal fun FixSteps(items: List<String>, done: Boolean) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items.forEach { text ->
                CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CmpIcon(
                        icon = if (done) CmpIcons.CheckCircle else CmpIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.primary,
                    )
                    CmpText(text = text, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

/** Кнопка с иконкой (CmpButton не принимает icon — используем content-overload). */
@Composable
internal fun IconedButton(text: String, icon: CmpIcons, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(onClick = onClick, modifier = modifier) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(icon = icon, contentDescription = null)
            CmpText(text = text, fontSize = DsType.LabelLarge, fontWeight = FontWeight.Medium)
        }
    }
}

/** Заголовок + подзаголовок для центрированных состояний. */
@Composable
internal fun StateHeadline(title: String, subtitle: String) {
    val colors = cmpThemeColors()
    CmpColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpText(text = title, fontSize = DsType.HeadlineSmall, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        CmpText(text = subtitle, fontSize = DsType.BodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
