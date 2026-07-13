package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.feature.main.presentation.model.MainCardVariant
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenState

private const val DrawerAnimationDurationMillis = 220
private const val DrawerScrimAnimationDurationMillis = 180

@Composable
fun MainHomeBody(
    state: State<MainScreenState>,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onDatabaseSyncClick: () -> Unit,
    onAppUpdateCheckClick: () -> Unit,
    onAppUpdateOpenClick: () -> Unit,
    onHomeActionClick: (MainHomeAction) -> Unit,
    onSettingsClick: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = state.value
    val colors = cmpThemeColors()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .mainHomeBrandBackdrop(colors),
    ) {
        val mode = mainHomeMode(maxWidth.value)
        val layoutDirection = LocalLayoutDirection.current
        val isSidePanelMode = mode == MainHomeMode.Expanded || mode == MainHomeMode.Large
        val showSidePanel = !value.isInitialLoading && isSidePanelMode && value.isMenuOpen
        val showModalDrawer = !value.isInitialLoading && !isSidePanelMode && value.isMenuOpen

        CmpScaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = colors.onBackground,
            topBar = {
                Column {
                    MainHomeTopBar(
                        isCompact = mode == MainHomeMode.Compact,
                        isLoading = value.isInitialLoading,
                        databaseSyncBadge = value.databaseSyncBadge,
                        onMenuClick = onMenuClick,
                    )
                    if (value.isInitialLoading) {
                        CmpLinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                        )
                    }
                }
            },
        ) { paddingValues ->
            val scaffoldStartPadding = paddingValues.calculateStartPadding(layoutDirection)
            val scaffoldEndPadding = paddingValues.calculateEndPadding(layoutDirection)
            val scaffoldTopPadding = paddingValues.calculateTopPadding()
            val scaffoldBottomPadding = paddingValues.calculateBottomPadding()
            val contentStartPadding = if (showSidePanel) 0.dp else scaffoldStartPadding
            val sidePanelDrawerPadding = PaddingValues(
                start = scaffoldStartPadding,
                bottom = scaffoldBottomPadding,
            )
            val modalDrawerPadding = PaddingValues(
                start = scaffoldStartPadding,
                top = scaffoldTopPadding,
                bottom = scaffoldBottomPadding,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = scaffoldTopPadding),
                ) {
                    AnimatedVisibility(
                        visible = showSidePanel,
                        enter = sidePanelEnterTransition(),
                        exit = sidePanelExitTransition(),
                    ) {
                        MainHomeDrawer(
                            state = value,
                            isDark = isDarkTheme,
                            onThemeChanged = onThemeChanged,
                            onDatabaseSyncClick = onDatabaseSyncClick,
                            onAppUpdateCheckClick = onAppUpdateCheckClick,
                            onAppUpdateOpenClick = onAppUpdateOpenClick,
                            onSettingsClick = onSettingsClick,
                            onLogout = onLogout,
                            contentPadding = sidePanelDrawerPadding,
                            modifier = Modifier.width(312.dp),
                        )
                    }

                    if (value.isInitialLoading) {
                        MainHomeSkeleton(
                            mode = mode,
                            cardVariant = value.cardVariant,
                            startContentPadding = contentStartPadding,
                            endContentPadding = scaffoldEndPadding,
                            bottomContentPadding = scaffoldBottomPadding,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        MainHomeContent(
                            state = value,
                            mode = mode,
                            cardVariant = value.cardVariant,
                            onHomeActionClick = onHomeActionClick,
                            startContentPadding = contentStartPadding,
                            endContentPadding = scaffoldEndPadding,
                            bottomContentPadding = scaffoldBottomPadding,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showModalDrawer,
                        enter = fadeIn(animationSpec = tween(DrawerScrimAnimationDurationMillis)),
                        exit = fadeOut(animationSpec = tween(DrawerScrimAnimationDurationMillis)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.scrim.copy(alpha = 0.42f))
                                .clickable(onClick = onMenuDismiss),
                        )
                    }

                    AnimatedVisibility(
                        visible = showModalDrawer,
                        enter = drawerEnterTransition(),
                        exit = drawerExitTransition(),
                    ) {
                        MainHomeDrawer(
                            state = value,
                            isDark = isDarkTheme,
                            onThemeChanged = onThemeChanged,
                            onDatabaseSyncClick = onDatabaseSyncClick,
                            onAppUpdateCheckClick = onAppUpdateCheckClick,
                            onAppUpdateOpenClick = onAppUpdateOpenClick,
                            onSettingsClick = onSettingsClick,
                            onLogout = onLogout,
                            contentPadding = modalDrawerPadding,
                            modifier = Modifier.width(320.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun drawerEnterTransition() = slideInHorizontally(
    animationSpec = tween(DrawerAnimationDurationMillis),
    initialOffsetX = { -it },
) + fadeIn(animationSpec = tween(DrawerAnimationDurationMillis))

private fun drawerExitTransition() = slideOutHorizontally(
    animationSpec = tween(DrawerAnimationDurationMillis),
    targetOffsetX = { -it },
) + fadeOut(animationSpec = tween(DrawerAnimationDurationMillis))

private fun sidePanelEnterTransition() = drawerEnterTransition() + expandHorizontally(
    animationSpec = tween(DrawerAnimationDurationMillis),
    expandFrom = Alignment.Start,
)

private fun sidePanelExitTransition() = drawerExitTransition() + shrinkHorizontally(
    animationSpec = tween(DrawerAnimationDurationMillis),
    shrinkTowards = Alignment.Start,
)

@Composable
private fun MainHomeContent(
    state: MainScreenState,
    mode: MainHomeMode,
    cardVariant: MainCardVariant,
    onHomeActionClick: (MainHomeAction) -> Unit,
    startContentPadding: Dp,
    endContentPadding: Dp,
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val actions = MainHomeAction.visibleActions(
        features = state.appFeatures,
        commercial = state.userCommercial,
        showChat = state.chatAvailability,
    )

    MainHomeDashboard(
        mode = mode,
        cardVariant = cardVariant,
        onHomeActionClick = onHomeActionClick,
        startContentPadding = startContentPadding,
        endContentPadding = endContentPadding,
        bottomContentPadding = bottomContentPadding,
        actions = actions,
        modifier = modifier,
    )
}

private fun Modifier.mainHomeBrandBackdrop(colors: CmpThemeColors): Modifier {
    return drawBehind {
        drawRect(colors.background)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.primaryContainer.copy(alpha = 0.82f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 1.04f, -size.height * 0.18f),
                radius = size.maxDimension * 0.76f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.tertiaryContainer.copy(alpha = 0.72f),
                    Color.Transparent,
                ),
                center = Offset(-size.width * 0.18f, size.height * 1.1f),
                radius = size.maxDimension * 0.7f,
            ),
        )
    }
}
