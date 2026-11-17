@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

enum class CmpBannerTone { Neutral, Info, Success, Warning, Error }

private const val BANNER_BORDER_ALPHA = 0.3f

/**
 * Плашка-предупреждение: фон `*-highlight` тона, рамка, иконка, заголовок Headline3 и текст Paragraph3,
 * действие — текстовая кнопка ([actionLabel] + [onAction]).
 */
@Composable
fun CmpBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: CmpBannerTone = CmpBannerTone.Warning,
    title: String? = null,
    icon: CmpIcons? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val (fill, accent) = when (tone) {
        CmpBannerTone.Neutral -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkSecondaryDefault
        CmpBannerTone.Info -> colors.infoHighlight to colors.infoDefault
        CmpBannerTone.Success -> colors.successHighlight to colors.successDefault
        CmpBannerTone.Warning -> colors.warningHighlight to colors.warningDefault
        CmpBannerTone.Error -> colors.errorHighlight to colors.errorDefault
    }
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(fill, shape)
            .border(CmpBorderWidth.inputs, accent.copy(alpha = BANNER_BORDER_ALPHA), shape)
            .padding(CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.Top,
    ) {
        CmpIcon(
            icon = icon ?: tone.defaultIcon(),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(CmpSize.iconM),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            if (title != null) CmpText(text = title, style = CmpTextStyle.Headline3)
            CmpText(text = text, style = CmpTextStyle.Paragraph3, color = colors.foregroundOnDarkSecondaryDefault)
            if (actionLabel != null && onAction != null) {
                CmpButton(
                    text = actionLabel,
                    onClick = onAction,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
            }
        }
    }
}

private fun CmpBannerTone.defaultIcon(): CmpIcons = when (this) {
    CmpBannerTone.Neutral, CmpBannerTone.Info -> CmpIcons.Info
    CmpBannerTone.Success -> CmpIcons.CheckCircle
    CmpBannerTone.Warning -> CmpIcons.Warning
    CmpBannerTone.Error -> CmpIcons.ErrorOutline
}

/** Закреплённая нижняя панель действий: ступень чернил 5%, линия сверху, отступ над навигационной панелью системы. */
@Composable
fun CmpActionBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Column(modifier = modifier.fillMaxWidth().background(CmpTheme.colors.backgroundSurfaceUltraHighlight)) {
        CmpDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
