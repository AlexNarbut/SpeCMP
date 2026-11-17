package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Текстовая кнопка — CmpButton(type = Tertiary, size = S); новый код — сразу CmpButton. */
@Composable
fun CmpTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CmpButtonSize = CmpButtonSize.S,
    loading: Boolean = false,
) {
    CmpButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        type = CmpButtonType.Tertiary,
        size = size,
        loading = loading,
    )
}

@Composable
fun CmpTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: CmpButtonSize = CmpButtonSize.S,
    content: @Composable RowScope.() -> Unit,
) {
    CmpButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        type = CmpButtonType.Tertiary,
        size = size,
        content = content,
    )
}
