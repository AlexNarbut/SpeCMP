package ru.alarmtrade.specmp.core.ui.view.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

private val MenuItemMinHeight = 48.dp
private val MenuMinWidth = 260.dp
private val CheckSlot = 20.dp

/** Выпадающее меню у якоря (кнопка, чип): вызывать внутри контейнера якоря. */
@Composable
fun CmpDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.defaultMinSize(minWidth = MenuMinWidth),
        content = content
    )
}

/** Пункт меню ≥ 48 dp; у выбранного слева галочка (у остальных — пустое место того же размера, текст не «пляшет»). */
@Composable
fun CmpDropdownMenuItem(text: String, onClick: () -> Unit, selected: Boolean, modifier: Modifier = Modifier) {
    DropdownMenuItem(
        text = { CmpText(text = text) },
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MenuItemMinHeight),
        leadingIcon = {
            if (selected) {
                CmpIcon(icon = CmpIcons.Check, contentDescription = null, modifier = Modifier.size(CheckSlot))
            } else {
                Spacer(Modifier.size(CheckSlot))
            }
        }
    )
}

/** Пункт меню действия ≥ 48 dp: иконка слева (необязательна), неактивный пункт — приглушён и не нажимается. */
@Composable
fun CmpDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = { CmpText(text = text) },
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = MenuItemMinHeight),
        enabled = enabled,
        leadingIcon = icon?.let { { CmpIcon(icon = it, contentDescription = null) } }
    )
}
