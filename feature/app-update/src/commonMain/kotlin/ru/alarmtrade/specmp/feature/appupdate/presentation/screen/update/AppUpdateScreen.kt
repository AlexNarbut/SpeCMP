package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.appupdate_main_title_screen
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.AppUpdateDialogs
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.ChannelCard
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.CurrentVersionRow
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.StageCard

private val OneColumnMaxWidth = 720.dp
private val TwoColumnMaxWidth = 1040.dp
private val ChannelColumnWidth = 360.dp

@Composable
internal fun AppUpdateScreen(
    state: AppUpdateState,
    actions: AppUpdateActions,
    snackbar: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    CmpScaffold(
        modifier = modifier,
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.appupdate_main_title_screen),
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.appupdate_common_a11y_back),
                    )
                },
            )
        },
        snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
    ) { padding ->
        val direction = LocalLayoutDirection.current
        CmpAdaptiveBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(direction),
                    end = padding.calculateEndPadding(direction),
                ),
        ) { window ->
            val gutter = if (window.isCompact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium
            val twoColumns = window.atLeast(CmpWidthClass.Expanded) && state.showChannels
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = if (twoColumns) TwoColumnMaxWidth else OneColumnMaxWidth)
                        .fillMaxWidth()
                        .padding(
                            start = gutter,
                            top = padding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
                            end = gutter,
                            bottom = padding.calculateBottomPadding() + CmpSpacing.screenPaddingBottom,
                        ),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                ) {
                    CurrentVersionRow(state)
                    if (twoColumns) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
                            verticalAlignment = Alignment.Top,
                        ) {
                            StageCard(state, actions, Modifier.weight(1f))
                            ChannelCard(state, actions.onSelectChannel, Modifier.widthIn(max = ChannelColumnWidth))
                        }
                    } else {
                        StageCard(state, actions, compact = window.isCompact)
                        if (state.showChannels) ChannelCard(state, actions.onSelectChannel)
                    }
                }
            }
        }
    }
    AppUpdateDialogs(dialog = state.dialog, onConfirm = actions.onConfirmDialog, onDismiss = actions.onDismissDialog)
}
