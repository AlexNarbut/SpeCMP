package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.appupdate_main_title_screen
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.AppUpdateDialogs
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.ChannelCard
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.CurrentVersionRow
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents.StageCard

private val OneColumnMaxWidth = 720.dp
private val TwoColumnMaxWidth = 1040.dp
private val ChannelColumnWidth = 360.dp

/** Compact — отступ 16; Medium — 24 и 720; Expanded — 32, при нескольких каналах — каналы справа, до 1040. */
private enum class UpdateLayout(val gutter: Dp) { Compact(16.dp), Medium(24.dp), Expanded(32.dp) }

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
                title = {
                    CmpText(
                        text = stringResource(Strings.string.appupdate_main_title_screen),
                        style = CmpTextStyle.Paragraph2,
                    )
                },
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
        CmpAdaptiveBox(modifier = Modifier.fillMaxSize().padding(padding)) { window ->
            val layout = when {
                window.atLeast(CmpWidthClass.Expanded) -> UpdateLayout.Expanded
                window.atLeast(CmpWidthClass.Medium) -> UpdateLayout.Medium
                else -> UpdateLayout.Compact
            }
            val twoColumns = layout == UpdateLayout.Expanded && state.showChannels
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = if (twoColumns) TwoColumnMaxWidth else OneColumnMaxWidth)
                        .fillMaxWidth()
                        .padding(
                            start = layout.gutter,
                            top = layout.gutter,
                            end = layout.gutter,
                            bottom = layout.gutter + 16.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CurrentVersionRow(state)
                    if (twoColumns) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Top) {
                            StageCard(state, actions, Modifier.weight(1f))
                            ChannelCard(state, actions.onSelectChannel, Modifier.widthIn(max = ChannelColumnWidth))
                        }
                    } else {
                        StageCard(state, actions)
                        if (state.showChannels) ChannelCard(state, actions.onSelectChannel)
                    }
                }
            }
        }
    }
    AppUpdateDialogs(dialog = state.dialog, onConfirm = actions.onConfirmDialog, onDismiss = actions.onDismissDialog)
}
