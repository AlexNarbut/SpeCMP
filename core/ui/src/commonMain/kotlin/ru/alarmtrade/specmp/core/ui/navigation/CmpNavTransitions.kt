package ru.alarmtrade.specmp.core.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.scene.Scene
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpMotion

private const val BEHIND_SHIFT = 4
private const val BEHIND_ALPHA = 0.7f

private fun <V> spec(easing: Easing): FiniteAnimationSpec<V> = tween(CmpMotion.STANDARD_MS, easing = easing)

/** Вперёд: новый экран въезжает справа, прежний уходит влево на четверть ширины и чуть гаснет. */
fun <T : Any> cmpForwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(SlideDirection.Left, spec<IntOffset>(CmpMotion.standardEasing)),
        slideOutOfContainer(SlideDirection.Left, spec(CmpMotion.standardEasing), targetOffset = { it / BEHIND_SHIFT }) +
            fadeOut(spec(CmpMotion.standardEasing), targetAlpha = BEHIND_ALPHA),
    )
}

/** Назад (кнопка, системный «назад»): зеркально — текущий экран уезжает вправо, предыдущий возвращается слева. */
fun <T : Any> cmpBackTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    backTransform(CmpMotion.standardEasing)
}

/** Жест «назад» с превью: тот же сдвиг вбок, но линейно — прогресс ведёт палец. */
fun <T : Any> cmpPredictiveBackTransition(): AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = {
    backTransform(LinearEasing)
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.backTransform(easing: Easing): ContentTransform =
    ContentTransform(
        slideIntoContainer(SlideDirection.Right, spec(easing), initialOffset = { it / BEHIND_SHIFT }) +
            fadeIn(spec(easing), initialAlpha = BEHIND_ALPHA),
        slideOutOfContainer(SlideDirection.Right, spec(easing)),
    )
