package ru.alarmtrade.specmp.core.ui.view.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CmpBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content,
    )
}

/**
 * Вариант «только прямоугольник»: нужен закрашенный бокс без контента —
 * пустая content-лямбда у полной обёртки выглядит как недочёт API (U-18).
 */
@Composable
fun CmpBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}
