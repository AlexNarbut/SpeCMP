package ru.alarmtrade.specmp.core.ui.view.badge

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CmpBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    Badge(
        modifier = modifier,
        containerColor = if (containerColor != Color.Unspecified) {
            containerColor
        } else {
            androidx.compose.material3.MaterialTheme.colorScheme.error
        },
        contentColor = if (contentColor != Color.Unspecified) {
            contentColor
        } else {
            androidx.compose.material3.MaterialTheme.colorScheme.onError
        },
        content = content
    )
}

@Composable
fun CmpBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    BadgedBox(
        badge = badge,
        modifier = modifier,
        content = content
    )
}
