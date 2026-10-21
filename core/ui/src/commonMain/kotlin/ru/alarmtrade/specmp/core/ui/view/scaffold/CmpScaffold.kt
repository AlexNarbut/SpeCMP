package ru.alarmtrade.specmp.core.ui.view.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CmpScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    content: @Composable (PaddingValues) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedContainerColor = if (containerColor != Color.Unspecified) {
        containerColor
    } else {
        colorScheme.background
    }
    val resolvedContentColor = if (contentColor != Color.Unspecified) {
        contentColor
    } else {
        colorScheme.onBackground
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Box(
                modifier = Modifier.windowInsetsPadding(
                    contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
            ) { topBar() }
        },
        bottomBar = {
            Box(
                modifier = Modifier.windowInsetsPadding(
                    contentWindowInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                )
            ) { bottomBar() }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        containerColor = resolvedContainerColor,
        contentColor = resolvedContentColor,
        contentWindowInsets = contentWindowInsets,
        content = content
    )
}
