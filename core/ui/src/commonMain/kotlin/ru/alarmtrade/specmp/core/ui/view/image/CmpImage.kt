package ru.alarmtrade.specmp.core.ui.view.image

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

@Composable
fun CmpImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment
    )
}
