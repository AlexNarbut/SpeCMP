@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class CmpNavigationItem(
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: @Composable () -> Unit,
    val label: (@Composable () -> Unit)? = null,
    val enabled: Boolean = true,
    val alwaysShowLabel: Boolean = true,
)

@Composable
fun CmpNavigationBar(items: List<CmpNavigationItem>, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = item.icon,
                label = item.label,
                enabled = item.enabled,
                alwaysShowLabel = item.alwaysShowLabel,
            )
        }
    }
}
