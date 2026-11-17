package ru.alarmtrade.specmp.core.ui.view.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

private val MenuItemMinHeight = 48.dp
private val MenuMinWidth = 260.dp
private val CheckSlot = 20.dp

/** Вызывать внутри контейнера якоря. */
@Composable
fun CmpDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.defaultMinSize(minWidth = MenuMinWidth),
        content = content,
    )
}

@Composable
fun CmpDropdownMenuItem(text: String, onClick: () -> Unit, selected: Boolean, modifier: Modifier = Modifier) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MenuItemMinHeight),
        leadingIcon = {
            if (selected) {
                CmpIcon(icon = CmpIcons.Check, contentDescription = null, modifier = Modifier.size(CheckSlot))
            } else {
                Spacer(Modifier.size(CheckSlot))
            }
        },
    )
}

@Composable
fun CmpDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MenuItemMinHeight),
        enabled = enabled,
        leadingIcon = icon?.let { { CmpIcon(icon = it, contentDescription = null) } },
    )
}
