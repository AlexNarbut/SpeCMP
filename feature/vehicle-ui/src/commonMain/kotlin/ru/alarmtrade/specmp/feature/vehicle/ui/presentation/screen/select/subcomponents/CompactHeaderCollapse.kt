package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val TopZone = 24.dp
private val Step = 6.dp

@Stable
internal class CompactHeaderCollapse(private val topZonePx: Float, private val stepPx: Float) : NestedScrollConnection {
    var collapsed by mutableStateOf(false)
        private set

    private var offset = 0f
    private var lastOffset = 0f

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        offset = if (available.y > 0f) 0f else (offset - consumed.y).coerceAtLeast(0f)
        val delta = offset - lastOffset
        when {
            offset < topZonePx -> collapsed = false
            delta > stepPx -> collapsed = true
            delta < -stepPx -> collapsed = false
        }
        if (abs(delta) > stepPx || offset < topZonePx) lastOffset = offset
        return Offset.Zero
    }
}

@Composable
internal fun rememberCompactHeaderCollapse(vararg resetKeys: Any?): CompactHeaderCollapse {
    val density = LocalDensity.current
    return remember(density, *resetKeys) {
        with(density) { CompactHeaderCollapse(topZonePx = TopZone.toPx(), stepPx = Step.toPx()) }
    }
}
