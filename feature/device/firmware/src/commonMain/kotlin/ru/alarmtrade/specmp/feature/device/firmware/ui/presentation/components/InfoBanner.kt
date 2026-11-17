@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal enum class BannerTone { Warning, Error, Ok }

@Composable
internal fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Warning,
    icon: CmpIcons = CmpIcons.Warning,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = cmpThemeColors()
    val container = when (tone) {
        BannerTone.Warning -> colors.tertiaryContainer
        BannerTone.Error -> colors.errorContainer
        BannerTone.Ok -> colors.primaryContainer
    }
    val content = when (tone) {
        BannerTone.Warning -> colors.onTertiaryContainer
        BannerTone.Error -> colors.onErrorContainer
        BannerTone.Ok -> colors.onPrimaryContainer
    }
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = content,
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = content)
            CmpText(text = text, modifier = Modifier.weight(1f), color = content, style = CmpTextStyle.Paragraph2)
            action?.invoke()
        }
    }
}
