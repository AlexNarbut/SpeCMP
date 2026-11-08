package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors

@Composable
internal fun SupportChatWallpaper(colors: CmpThemeColors, modifier: Modifier = Modifier) {
    // drawWithCache: раскладка тайлов и градиент считаются один раз на размер,
    // а не на каждый проход фазы Draw (U-19).
    val drawCacheColors = colors
    Canvas(
        modifier = modifier.drawWithCache {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    drawCacheColors.primary.copy(alpha = 0.06f),
                    Color.Transparent,
                ),
                center = Offset(size.width / 2f, -size.height * 0.12f),
                radius = size.maxDimension * 0.72f,
            )

            val tile = 240.dp.toPx()
            val wrenchColor = drawCacheColors.onSurface.copy(alpha = 0.045f)
            val placements = listOf(
                WrenchPlacement(0.12f, 0.14f, 28f),
                WrenchPlacement(0.70f, 0.19f, -38f),
                WrenchPlacement(0.32f, 0.50f, 82f),
                WrenchPlacement(0.82f, 0.60f, -64f),
                WrenchPlacement(0.16f, 0.84f, 12f),
                WrenchPlacement(0.61f, 0.90f, -18f),
            )

            val areaSize = size
            val wrenches = buildList {
                var tileY = 0f
                while (tileY < areaSize.height + tile) {
                    var tileX = 0f
                    while (tileX < areaSize.width + tile) {
                        placements.forEach { placement ->
                            add(
                                WrenchDraw(
                                    center = Offset(
                                        x = tileX + tile * placement.x,
                                        y = tileY + tile * placement.y,
                                    ),
                                    degrees = placement.degrees,
                                ),
                            )
                        }
                        tileX += tile
                    }
                    tileY += tile
                }
            }

            onDrawBehind {
                drawRect(brush = gradient)
                wrenches.forEach { wrench ->
                    drawWrench(center = wrench.center, degrees = wrench.degrees, color = wrenchColor)
                }
            }
        },
    ) {
        // Весь контент рисуется через drawWithCache-обёртку выше.
    }
}

private fun DrawScope.drawWrench(center: Offset, degrees: Float, color: Color) {
    val strokeWidth = 3.dp.toPx()
    val headRadius = 6.dp.toPx()
    val handleLength = 22.dp.toPx()

    rotate(degrees = degrees, pivot = center) {
        drawLine(
            color = color,
            start = Offset(center.x - handleLength, center.y),
            end = Offset(center.x + headRadius, center.y),
            strokeWidth = strokeWidth,
        )
        drawCircle(
            color = color,
            radius = 3.5.dp.toPx(),
            center = Offset(center.x - handleLength, center.y),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawArc(
            color = color,
            startAngle = 42f,
            sweepAngle = 276f,
            useCenter = false,
            topLeft = Offset(center.x, center.y - headRadius),
            size = Size(headRadius * 2f, headRadius * 2f),
            style = Stroke(width = strokeWidth),
        )
    }
}

private data class WrenchPlacement(val x: Float, val y: Float, val degrees: Float)

private data class WrenchDraw(val center: Offset, val degrees: Float)
