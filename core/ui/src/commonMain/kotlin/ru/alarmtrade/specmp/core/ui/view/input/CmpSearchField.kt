package ru.alarmtrade.specmp.core.ui.view.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/**
 * Поле поиска: лупа слева, «очистить» справа, когда есть текст. Одна строка.
 * [clearContentDescription] — подпись кнопки очистки для доступности.
 */
@Composable
fun CmpSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
) {
    CmpTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        singleLine = true,
        leadingIcon = { CmpIcon(icon = CmpIcons.Search, contentDescription = null) },
        trailingIcon = if (value.isNotEmpty()) {
            {
                CmpIconButton(
                    onClick = { onValueChange("") },
                    icon = CmpIcons.Close,
                    contentDescription = clearContentDescription,
                )
            }
        } else {
            null
        },
        modifier = modifier,
    )
}
