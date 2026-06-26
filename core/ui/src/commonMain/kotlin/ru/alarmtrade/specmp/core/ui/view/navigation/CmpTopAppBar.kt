package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class CmpTopAppBarStyle { Small, CenterAligned }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmpTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    style: CmpTopAppBarStyle = CmpTopAppBarStyle.Small,
) {
    when (style) {
        CmpTopAppBarStyle.Small -> TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
        )
        CmpTopAppBarStyle.CenterAligned -> CenterAlignedTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
        )
    }
}
