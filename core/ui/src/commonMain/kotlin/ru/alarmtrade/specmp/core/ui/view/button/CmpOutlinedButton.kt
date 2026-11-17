package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Обводная кнопка — CmpButton(type = Secondary); новый код — сразу CmpButton. */
@Composable
fun CmpOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CmpButtonSize = CmpButtonSize.L,
    loading: Boolean = false,
) {
    CmpButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        type = CmpButtonType.Secondary,
        size = size,
        loading = loading,
    )
}

@Composable
fun CmpOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CmpButtonSize = CmpButtonSize.L,
    content: @Composable RowScope.() -> Unit,
) {
    CmpButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        type = CmpButtonType.Secondary,
        size = size,
        content = content,
    )
}
