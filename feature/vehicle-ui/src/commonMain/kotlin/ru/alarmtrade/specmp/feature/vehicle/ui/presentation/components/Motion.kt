package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox

private const val FADE_IN_MS = 220
private const val FADE_IN_DELAY_MS = 60
private const val FADE_OUT_MS = 120

/** Уходящее содержимое дорисовывается со своим старым [targetState]. */
@Composable
internal fun <S> FadeContent(
    targetState: S,
    contentKey: (S) -> Any?,
    modifier: Modifier = Modifier,
    content: @Composable (S) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            fadeIn(tween(FADE_IN_MS, delayMillis = FADE_IN_DELAY_MS)) togetherWith
                fadeOut(tween(FADE_OUT_MS)) using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.TopStart,
        contentKey = contentKey,
        label = "vehicleFade",
    ) { state -> content(state) }
}

/** Тип слота — префикс ключа до «-» (`brand-12` → `brand`): при прокрутке строка марки переиспользует слот марки. */
internal fun LazyListScope.fadeItem(key: Any, content: @Composable () -> Unit) {
    item(key = key, contentType = contentTypeOf(key)) { Animated { content() } }
}

internal fun <T> LazyListScope.fadeItems(list: List<T>, key: (T) -> Any, content: @Composable (T) -> Unit) {
    items(
        count = list.size,
        key = { key(list[it]) },
        contentType = { contentTypeOf(key(list[it])) },
    ) { index -> Animated { content(list[index]) } }
}

private fun contentTypeOf(key: Any): Any? = (key as? String)?.substringBefore('-')

@Composable
private fun LazyItemScope.Animated(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.animateItem(
            fadeInSpec = tween(FADE_IN_MS),
            placementSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            fadeOutSpec = tween(FADE_OUT_MS),
        ),
    ) { content() }
}
