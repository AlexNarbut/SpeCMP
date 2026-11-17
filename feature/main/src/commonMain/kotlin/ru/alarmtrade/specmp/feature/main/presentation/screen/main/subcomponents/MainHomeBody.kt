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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.CmpBackdrop
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBackdrop
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainAppUpdateActions
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenState

private const val DRAWER_ANIMATION_DURATION_MILLIS = 220
private const val DRAWER_SCRIM_ANIMATION_DURATION_MILLIS = 180

@Suppress("LongMethod")
@Composable
fun MainHomeBody(
    state: State<MainScreenState>,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onUserDataRefreshClick: () -> Unit,
    onDatabaseSyncClick: () -> Unit,
    appUpdateActions: MainAppUpdateActions,
    onHomeActionClick: (MainHomeAction) -> Unit,
    onSettingsClick: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = state.value
    val colors = CmpTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .cmpBackdrop(CmpBackdrop.Home),
    ) {
        val mode = mainHomeMode(maxWidth.value)
        val layoutDirection = LocalLayoutDirection.current
        val isSidePanelMode = mode == MainHomeMode.Expanded || mode == MainHomeMode.Large
        val showSidePanel = !value.isInitialLoading && isSidePanelMode && value.isMenuOpen
        val showModalDrawer = !value.isInitialLoading && !isSidePanelMode && value.isMenuOpen

        CmpScaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = colors.foregroundOnDarkPrimaryDefault,
            topBar = {
                Column {
                    MainHomeTopBar(
                        isCompact = mode == MainHomeMode.Compact,
                        isLoading = value.isInitialLoading,
                        databaseSyncBadge = value.databaseSyncBadge,
                        onMenuClick = onMenuClick,
                        onRequiredUpdateClick = appUpdateActions.onOpenPrompt,
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
                top = scaffoldTopPadding,
                bottom = scaffoldBottomPadding,
            )
            val modalDrawerPadding = PaddingValues(
                start = scaffoldStartPadding,
                top = scaffoldTopPadding,
                bottom = scaffoldBottomPadding,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                // Контент уходит под прозрачный топбар (блюр CmpScaffold): верхний отступ — внутри скролла
                Row(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showSidePanel,
                        enter = sidePanelEnterTransition(),
                        exit = sidePanelExitTransition(),
                    ) {
                        MainHomeDrawer(
                            state = value,
                            isDark = isDarkTheme,
                            onThemeChange = onThemeChange,
                            onDatabaseSyncClick = onDatabaseSyncClick,
                            appUpdateActions = appUpdateActions,
                            onSettingsClick = onSettingsClick,
                            onLogout = onLogout,
                            onUserDataRefreshClick = onUserDataRefreshClick,
                            contentPadding = sidePanelDrawerPadding,
                            modifier = Modifier.width(MainHomeTokens.DrawerPanelWidth),
                        )
                    }

                    if (value.isInitialLoading) {
                        MainHomeSkeleton(
                            mode = mode,
                            startContentPadding = contentStartPadding,
                            endContentPadding = scaffoldEndPadding,
                            bottomContentPadding = scaffoldBottomPadding,
                            topContentPadding = scaffoldTopPadding,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        MainHomeContent(
                            state = value,
                            mode = mode,
                            onHomeActionClick = onHomeActionClick,
                            appUpdateActions = appUpdateActions,
                            startContentPadding = contentStartPadding,
                            endContentPadding = scaffoldEndPadding,
                            bottomContentPadding = scaffoldBottomPadding,
                            topContentPadding = scaffoldTopPadding,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showModalDrawer,
                        enter = fadeIn(animationSpec = tween(DRAWER_SCRIM_ANIMATION_DURATION_MILLIS)),
                        exit = fadeOut(animationSpec = tween(DRAWER_SCRIM_ANIMATION_DURATION_MILLIS)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.backgroundOverlay)
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
                            onThemeChange = onThemeChange,
                            onDatabaseSyncClick = onDatabaseSyncClick,
                            appUpdateActions = appUpdateActions,
                            onSettingsClick = onSettingsClick,
                            onLogout = onLogout,
                            onUserDataRefreshClick = onUserDataRefreshClick,
                            contentPadding = modalDrawerPadding,
                            modifier = Modifier.width(MainHomeTokens.DrawerModalWidth),
                        )
                    }
                }
            }
        }

        value.updateToastVersion?.let { version ->
            MainUpdateToast(
                version = version,
                actions = appUpdateActions,
                modifier = Modifier.align(Alignment.BottomCenter).padding(CmpSpacing.space16),
            )
        }
    }
}

private fun drawerEnterTransition() = slideInHorizontally(
    animationSpec = tween(DRAWER_ANIMATION_DURATION_MILLIS),
    initialOffsetX = { -it },
) + fadeIn(animationSpec = tween(DRAWER_ANIMATION_DURATION_MILLIS))

private fun drawerExitTransition() = slideOutHorizontally(
    animationSpec = tween(DRAWER_ANIMATION_DURATION_MILLIS),
    targetOffsetX = { -it },
) + fadeOut(animationSpec = tween(DRAWER_ANIMATION_DURATION_MILLIS))

private fun sidePanelEnterTransition() = drawerEnterTransition() + expandHorizontally(
    animationSpec = tween(DRAWER_ANIMATION_DURATION_MILLIS),
    expandFrom = Alignment.Start,
)

private fun sidePanelExitTransition() = drawerExitTransition() + shrinkHorizontally(
    animationSpec = tween(DRAWER_ANIMATION_DURATION_MILLIS),
    shrinkTowards = Alignment.Start,
)

@Composable
private fun MainHomeContent(
    state: MainScreenState,
    mode: MainHomeMode,
    onHomeActionClick: (MainHomeAction) -> Unit,
    appUpdateActions: MainAppUpdateActions,
    startContentPadding: Dp,
    endContentPadding: Dp,
    bottomContentPadding: Dp,
    topContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    MainHomeDashboard(
        mode = mode,
        onHomeActionClick = onHomeActionClick,
        startContentPadding = startContentPadding,
        endContentPadding = endContentPadding,
        bottomContentPadding = bottomContentPadding,
        topContentPadding = topContentPadding,
        actions = state.visibleActions,
        modifier = modifier,
        header = {
            val bannerVersion = state.availableUpdateVersion
            if (state.showUpdateBanner && bannerVersion != null) {
                MainUpdateBanner(version = bannerVersion, actions = appUpdateActions)
            }
        },
    )
}
