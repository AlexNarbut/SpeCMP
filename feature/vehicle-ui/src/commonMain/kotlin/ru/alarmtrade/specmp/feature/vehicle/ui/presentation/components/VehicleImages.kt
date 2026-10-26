package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpAsyncImageWithPlaceholder
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Пропорции фото автомобиля и точек подключения. */
internal const val PHOTO_RATIO = 4f / 3f

/**
 * Фото автомобиля/модели фиксированного размера (раскладка не прыгает): пока грузится, не загрузилось или URL нет —
 * заглушка с иконкой автомобиля.
 */
@Composable
internal fun CarPhoto(
    url: String?,
    modifier: Modifier = Modifier,
    radius: Dp = 10.dp,
    contentDescription: String? = null
) {
    val colors = cmpThemeColors()
    Box(
        modifier = modifier.clip(RoundedCornerShape(radius)).background(colors.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        val placeholder: @Composable () -> Unit = {
            CmpIcon(
                icon = CmpIcons.DirectionsCar,
                contentDescription = null,
                tint = colors.outline,
                modifier = Modifier.size(28.dp)
            )
        }
        if (url == null) {
            placeholder()
        } else {
            CmpAsyncImageWithPlaceholder(
                model = url,
                contentDescription = contentDescription,
                placeholder = placeholder,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Логотип марки; нет логотипа — первая буква марки в круге. */
@Composable
internal fun BrandLogo(name: String, url: String?, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val colors = cmpThemeColors()
    val letter: @Composable () -> Unit = {
        CmpText(
            text = name.take(1).uppercase(),
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSecondaryContainer
        )
    }
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(12.dp)).background(colors.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (url == null) {
            letter()
        } else {
            CmpAsyncImageWithPlaceholder(
                model = url,
                contentDescription = null,
                placeholder = letter,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
