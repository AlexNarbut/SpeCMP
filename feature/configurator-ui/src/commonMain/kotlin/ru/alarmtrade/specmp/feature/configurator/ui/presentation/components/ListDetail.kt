package ru.alarmtrade.specmp.feature.configurator.ui.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator

internal val ListDetailWideFrom = 600.dp

private const val LIST_WEIGHT = 0.45f

@Composable
internal fun ListDetailPane(
    wide: Boolean,
    detail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    list: @Composable ColumnScope.() -> Unit,
) {
    CmpRow(modifier = modifier.fillMaxSize()) {
        val listModifier = if (wide) Modifier.weight(LIST_WEIGHT).fillMaxHeight() else Modifier.fillMaxSize()
        CmpColumn(modifier = listModifier, content = list)
        if (wide) {
            CmpVerticalDivider()
            CmpBox(modifier = Modifier.weight(1f - LIST_WEIGHT).fillMaxHeight()) { detail() }
        }
    }
}

@Composable
internal fun BusyOverlay() {
    CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CmpCircularProgressIndicator()
    }
}
