package ru.alarmtrade.specmp.core.ui.view.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors

@Stable
class PaneWidthState internal constructor(initial: Dp, private val min: Dp, private val max: Dp) {
    var width: Dp by mutableStateOf(initial.coerceIn(min, max))
        private set

    fun dragBy(delta: Dp) {
        width = (width + delta).coerceIn(min, max)
    }
}

/** [saved] — сохранённая пользователем ширина; меняется окно ([max]) — ширина снова ужимается в границы. */
@Composable
fun rememberPaneWidthState(saved: Dp?, default: Dp, min: Dp, max: Dp): PaneWidthState {
    val upper = maxOf(min, max)
    return remember(saved, default, min, upper) { PaneWidthState(saved ?: default, min, upper) }
}

/** Ручка между панелями: тянуть — менять ширину панели слева, [onDragStop] — сохранить её. */
@Composable
fun CmpResizeHandle(state: PaneWidthState, onDragStop: (Dp) -> Unit, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    val density = LocalDensity.current
    CmpBox(
        modifier = modifier
            .width(ResizeHandleWidth)
            .fillMaxHeight()
            .background(colors.surfaceContainerLow)
            .pointerHoverIcon(PointerIcon.Hand)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { deltaPx -> state.dragBy(with(density) { deltaPx.toDp() }) },
                onDragStopped = { onDragStop(state.width) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        CmpBox(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.outlineVariant))
        CmpBox(
            modifier = Modifier
                .size(width = 4.dp, height = 32.dp)
                .background(colors.outline, CircleShape),
        )
    }
}

private val ResizeHandleWidth = 12.dp
