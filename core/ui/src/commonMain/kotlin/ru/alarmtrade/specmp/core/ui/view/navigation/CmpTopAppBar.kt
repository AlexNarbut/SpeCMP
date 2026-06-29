package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class CmpTopAppBarStyle { Small, CenterAligned }

private val EdgeInset = 8.dp
private val SlotGap = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmpTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.background,
        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
    ),
    style: CmpTopAppBarStyle = CmpTopAppBarStyle.Small,
) {
    val titleSlot: @Composable () -> Unit = if (subtitle != null) {
        {
            Column(modifier = Modifier.padding(start = SlotGap)) {
                title()
                subtitle()
            }
        }
    } else {
        {
            Box(modifier = Modifier.padding(start = SlotGap)) {
                title()
            }
        }
    }

    val navigationIconSlot: @Composable () -> Unit = {
        Box(modifier = Modifier.padding(start = EdgeInset)) {
            navigationIcon()
        }
    }

    val actionsSlot: @Composable RowScope.() -> Unit = {
        Row(
            modifier = Modifier.padding(start = SlotGap, end = EdgeInset),
            horizontalArrangement = Arrangement.spacedBy(SlotGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }

    // Insets'ами (status bar) владеет CmpScaffold через topBar-слот — обнуляем здесь,
    // чтобы не было двойного верхнего отступа.
    val zeroInsets = WindowInsets(0, 0, 0, 0)

    when (style) {
        CmpTopAppBarStyle.Small -> TopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navigationIconSlot,
            actions = actionsSlot,
            windowInsets = zeroInsets,
            colors = colors,
        )

        CmpTopAppBarStyle.CenterAligned -> CenterAlignedTopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navigationIconSlot,
            actions = actionsSlot,
            windowInsets = zeroInsets,
            colors = colors,
        )
    }
}

