@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

enum class CmpFabSize(internal val size: Dp) {
    Small(CmpSize.touchMin),
    Default(CmpSize.iconXxl),
    Large(96.dp),
}

/** Плавающая кнопка: синий акцент, круг (с подписью — пилюля), без тени; нажатие — как у всех Cmp*. */
@Composable
fun CmpFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: CmpFabSize = CmpFabSize.Default,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.round)
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = size.size, minHeight = size.size)
            .cmpPressable(onClick = onClick, enabled = enabled, shape = shape, role = Role.Button)
            .background(if (enabled) colors.brandPrimaryDefault else colors.backgroundSurfaceHighlight, shape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) colors.onBrand else colors.foregroundOnDarkPrimaryDisabled,
            LocalTextStyle provides CmpTextStyle.Headline3.toTextStyle(),
            content = content,
        )
    }
}
