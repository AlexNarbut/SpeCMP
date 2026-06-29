package ru.alarmtrade.specmp.core.ui.view.icon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CmpIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = if (tint != Color.Unspecified) tint
        else androidx.compose.material3.LocalContentColor.current,
    )
}

@Composable
fun CmpIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = if (tint != Color.Unspecified) tint
        else androidx.compose.material3.LocalContentColor.current,
    )
}

@Composable
fun CmpIcon(
    icon: CmpIcons,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    CmpIcon(
        imageVector = icon.imageVector(),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}
