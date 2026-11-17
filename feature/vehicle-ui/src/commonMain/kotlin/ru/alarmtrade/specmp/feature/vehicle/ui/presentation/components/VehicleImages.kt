package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImageWithPlaceholder
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal val ThumbSize = CmpSize.touchMin

internal val PhotoThumbSize = 72.dp

internal val ThumbRadius = CmpRadius.cardSmall

internal const val PHOTO_RATIO = 4f / 3f

@Composable
internal fun CarPhoto(
    url: String?,
    modifier: Modifier = Modifier,
    radius: Dp = CmpRadius.cardSmall,
    contentDescription: String? = null,
) {
    val colors = CmpTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(colors.backgroundSurfaceHighlight)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center,
    ) {
        val placeholder: @Composable () -> Unit = {
            CmpIcon(
                icon = CmpIcons.DirectionsCar,
                contentDescription = null,
                tint = colors.foregroundOnDarkTertiary,
                modifier = Modifier.size(CmpSize.iconL),
            )
        }
        if (url == null) {
            placeholder()
        } else {
            CmpAsyncImageWithPlaceholder(
                model = url,
                contentDescription = contentDescription,
                placeholder = placeholder,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun BrandLogo(name: String, url: String?, modifier: Modifier = Modifier, size: Dp = ThumbSize) {
    val colors = CmpTheme.colors
    val letter: @Composable () -> Unit = {
        CmpText(
            text = name.take(1).uppercase(),
            style = CmpTextStyle.Headline2,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(ThumbRadius))
            .background(colors.backgroundSurfaceHighlight)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, RoundedCornerShape(ThumbRadius)),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null) {
            letter()
        } else {
            CmpAsyncImageWithPlaceholder(
                model = url,
                contentDescription = null,
                placeholder = letter,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
