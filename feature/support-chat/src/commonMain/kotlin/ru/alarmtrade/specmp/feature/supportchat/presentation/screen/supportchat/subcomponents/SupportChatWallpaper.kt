package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme

/** Узор ключей поверх фона экрана; градиент даёт `cmpBackdrop` на корне экрана. */
@Composable
internal fun SupportChatWallpaper(modifier: Modifier = Modifier) {
    val wrenchColor = CmpTheme.colors.foregroundOnDarkPrimaryDefault.copy(alpha = WRENCH_ALPHA)
    // drawWithCache: раскладка тайлов — один раз на размер, а не на каждый проход Draw.
    Canvas(
        modifier = modifier.drawWithCache {
            val tile = 240.dp.toPx()
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
                wrenches.forEach { wrench ->
                    drawWrench(center = wrench.center, degrees = wrench.degrees, color = wrenchColor)
                }
            }
        },
    ) {
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

private const val WRENCH_ALPHA = 0.05f

private data class WrenchPlacement(val x: Float, val y: Float, val degrees: Float)

private data class WrenchDraw(val center: Offset, val degrees: Float)
