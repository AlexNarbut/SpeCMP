package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold

@Composable
fun AuthScreenChrome(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isLoading: Boolean = false,
    colors: CmpThemeColors = cmpThemeColors(),
    overlay: (@Composable (AuthLayoutMode) -> Unit)? = null,
    body: @Composable ColumnScope.(AuthLayoutMode) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .authBrandBackdrop(colors),
    ) {
        val layoutMode = authLayoutModeForWidth(maxWidth)

        CmpScaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = colors.onBackground,
            topBar = {
                CmpTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    containerColor = Color.Transparent,
                    contentColor = colors.onBackground,
                )
            },
        ) { paddingValues ->
            CmpBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                CmpColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CmpBox(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                        if (isLoading) {
                            CmpLinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = colors.primary,
                                trackColor = Color.Transparent,
                            )
                        }
                    }
                    CmpColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        body(layoutMode)
                    }
                }
            }
        }

        overlay?.invoke(layoutMode)
    }
}

private fun Modifier.authBrandBackdrop(colors: CmpThemeColors): Modifier = drawBehind {
    drawRect(colors.background)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.primaryContainer.copy(alpha = 0.88f),
                Color.Transparent,
            ),
            center = Offset(x = size.width * 1.02f, y = -size.height * 0.15f),
            radius = size.maxDimension * 0.72f,
        ),
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                colors.tertiaryContainer.copy(alpha = 0.78f),
                Color.Transparent,
            ),
            center = Offset(x = -size.width * 0.2f, y = size.height * 1.12f),
            radius = size.maxDimension * 0.68f,
        ),
    )
}
