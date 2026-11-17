package ru.alarmtrade.specmp.feature.configurator.ui.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpResizeHandle
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.rememberPaneWidthState
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton

internal val ListDetailWideFrom = CmpBreakpoints.Medium

/** Ключ ширины списка: общий для «Моих» и облака — один и тот же список слева. */
internal const val LIST_PANE_KEY = "configurator.list"

private const val LIST_DEFAULT_FRACTION = 0.45f
private const val LIST_MAX_FRACTION = 0.65f
private val ListMinWidth = 280.dp

/** [listWidthDp] — ширина списка, перетащенная пользователем; `null` — по умолчанию. */
@Composable
internal fun ListDetailPane(
    wide: Boolean,
    listWidthDp: Int?,
    onListWidthChange: (Int) -> Unit,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    list: @Composable ColumnScope.() -> Unit,
) {
    if (!wide) {
        CmpColumn(modifier = modifier.fillMaxSize(), content = list)
        return
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val pane = rememberPaneWidthState(
            saved = listWidthDp?.dp,
            default = maxWidth * LIST_DEFAULT_FRACTION,
            min = ListMinWidth,
            max = maxWidth * LIST_MAX_FRACTION,
        )
        CmpRow(modifier = Modifier.fillMaxSize()) {
            CmpColumn(modifier = Modifier.width(pane.width).fillMaxHeight(), content = list)
            CmpResizeHandle(state = pane, onDragStop = { onListWidthChange(it.value.roundToInt()) })
            CmpBox(modifier = Modifier.weight(1f).fillMaxHeight()) { detail() }
        }
    }
}

@Composable
internal fun BusyOverlay() {
    CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CmpCircularProgressIndicator()
    }
}

@Composable
internal fun SkeletonCards(rows: Int, cardHeight: Dp = CmpSize.rowSection) {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.screenPaddingH),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        repeat(rows) {
            CmpSkeleton(
                modifier = Modifier.fillMaxWidth().height(cardHeight),
                shape = RoundedCornerShape(CmpRadius.cardMedium),
            )
        }
    }
}
