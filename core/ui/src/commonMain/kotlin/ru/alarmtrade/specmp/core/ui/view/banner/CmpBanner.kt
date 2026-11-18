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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_banner_a11y_close
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

enum class CmpBannerTone { Neutral, Info, Success, Warning, Error }

private const val BANNER_BORDER_ALPHA = 0.3f
private val BusyStroke = 2.dp

@Composable
private fun BannerAction(label: String, onAction: () -> Unit, enabled: Boolean) {
    CmpButton(
        text = label,
        onClick = onAction,
        enabled = enabled,
        type = CmpButtonType.Tertiary,
        size = CmpButtonSize.S,
    )
}

/**
 * Плашка-предупреждение: фон `*-highlight` тона, рамка, иконка, заголовок Headline3 и текст Paragraph3,
 * действие — текстовая кнопка ([actionLabel] + [onAction], [actionEnabled]); [busy] — спиннер вместо иконки,
 * [compact] — одна строка с действием справа; [onDismiss] — крестик закрытия справа (`null` — без него).
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
    actionEnabled: Boolean = true,
    busy: Boolean = false,
    compact: Boolean = false,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val (fill, accent) = tone.colors()
    val action: (@Composable () -> Unit)? = if (actionLabel != null && onAction != null) {
        { BannerAction(actionLabel, onAction, actionEnabled) }
    } else {
        null
    }
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(fill, shape)
            .border(CmpBorderWidth.inputs, accent.copy(alpha = BANNER_BORDER_ALPHA), shape)
            .padding(if (compact) CmpSpacing.space12 else CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top,
    ) {
        BannerIcon(busy = busy, icon = icon ?: tone.defaultIcon(), tint = accent)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            if (title != null) CmpText(text = title, style = CmpTextStyle.Headline3)
            CmpText(text = text, style = CmpTextStyle.Paragraph3, color = colors.foregroundOnDarkSecondaryDefault)
            if (!compact) action?.invoke()
        }
        if (compact) action?.invoke()
        if (onDismiss != null) {
            CmpIconButton(
                onClick = onDismiss,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.common_banner_a11y_close),
                tint = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun CmpBannerTone.colors(): Pair<Color, Color> {
    val c = CmpTheme.colors
    return when (this) {
        CmpBannerTone.Neutral -> c.backgroundSurfaceHighlight to c.foregroundOnDarkSecondaryDefault
        CmpBannerTone.Info -> c.infoHighlight to c.infoDefault
        CmpBannerTone.Success -> c.successHighlight to c.successDefault
        CmpBannerTone.Warning -> c.warningHighlight to c.warningDefault
        CmpBannerTone.Error -> c.errorHighlight to c.errorDefault
    }
}

@Composable
private fun BannerIcon(busy: Boolean, icon: CmpIcons, tint: Color) {
    if (busy) {
        CircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM), color = tint, strokeWidth = BusyStroke)
    } else {
        CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(CmpSize.iconM))
    }
}

private fun CmpBannerTone.defaultIcon(): CmpIcons = when (this) {
    CmpBannerTone.Neutral, CmpBannerTone.Info -> CmpIcons.Info
    CmpBannerTone.Success -> CmpIcons.CheckCircle
    CmpBannerTone.Warning -> CmpIcons.Warning
    CmpBannerTone.Error -> CmpIcons.ErrorOutline
}

/**
 * Закреплённая нижняя панель действий: ступень чернил 5%, линия сверху, отступ над навигационной панелью системы
 * ([navigationBarPadding] = false — внутри CmpScaffold, где отступ уже есть).
 */
@Composable
fun CmpActionBar(
    modifier: Modifier = Modifier,
    navigationBarPadding: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().background(CmpTheme.colors.backgroundSurfaceUltraHighlight)) {
        CmpDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (navigationBarPadding) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier)
                .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
