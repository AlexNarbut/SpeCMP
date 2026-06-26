package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

data class CmpTabItem(
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
)

@Composable
fun CmpTabRow(
    selectedTabIndex: Int,
    tabs: List<CmpTabItem>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
) {
    val tabContent: @Composable () -> Unit = {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { CmpText(text = tab.title) },
                icon = tab.icon,
            )
        }
    }

    if (scrollable) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier,
            tabs = tabContent,
        )
    } else {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = modifier,
            tabs = tabContent,
        )
    }
}
