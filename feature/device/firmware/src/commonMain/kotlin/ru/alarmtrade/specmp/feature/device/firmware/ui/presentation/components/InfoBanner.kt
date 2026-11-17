@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal enum class BannerTone { Warning, Error, Ok }

private const val BORDER_ALPHA = 0.3f

@Composable
internal fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Warning,
    icon: CmpIcons = CmpIcons.Warning,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val (container, accent) = when (tone) {
        BannerTone.Warning -> colors.warningHighlight to colors.warningDefault
        BannerTone.Error -> colors.errorHighlight to colors.errorDefault
        BannerTone.Ok -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault
    }
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = container,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
        border = BorderStroke(CmpBorderWidth.tags, accent.copy(alpha = BORDER_ALPHA)),
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.Top,
        ) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconM), tint = accent)
            CmpText(
                text = text,
                modifier = Modifier.weight(1f),
                color = colors.foregroundOnDarkPrimaryDefault,
                style = CmpTextStyle.Paragraph2,
            )
            action?.invoke()
        }
    }
}
