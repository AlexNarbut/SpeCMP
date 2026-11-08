package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/**
 * Иконочная кнопка. Принимает **только иконку** — из каталога [CmpIcons]
 * (предпочтительно) или произвольный [ImageVector]. Произвольный
 * `@Composable`-контент сознательно не поддерживается: содержимое
 * иконочной кнопки должно быть иконкой, а не любым UI.
 */
@Composable
fun CmpIconButton(
    onClick: () -> Unit,
    icon: CmpIcons,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        CmpIcon(icon = icon, contentDescription = contentDescription, tint = tint)
    }
}

@Composable
fun CmpIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        CmpIcon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}
