package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_a11y_locked
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_exit_exit
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_exit_stay
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_msg_exit
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_exit
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_screen
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLayout
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.deviceSubtitle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.firmwareLayoutOf
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.NoFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ConfirmBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.DataErrorBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.DownloadBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.EnteringBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ErrorBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.FinishingBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.LoadingBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.LostBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ProgressBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.SourceBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.SuccessBody

private val BodyMaxWidth = 480.dp
private val CompactHero = 128.dp
private val WideHero = 220.dp

internal const val EXIT_DIALOG_CONFIRM_TAG = "firmware_exit_dialog_confirm"

/** Системная «Назад» перехвачена: на процессе заблокирована, на выборе спрашивает подтверждение. */
@Composable
fun FirmwareUpdateScreen(
    onExit: () -> Unit,
    onOpenSupportChat: () -> Unit,
    onOpenArchive: (FirmwareArchiveRequest) -> Unit,
    filePicker: FirmwareFilePicker = NoFirmwareFilePicker,
    sources: FirmwareSources = FirmwareSources.All,
) {
    val viewModel = koinViewModel<FirmwareUpdateViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            FirmwareUpdateEffect.OpenSupportChat -> onOpenSupportChat()
            is FirmwareUpdateEffect.OpenArchive -> onOpenArchive(effect.args)
            FirmwareUpdateEffect.Exit -> onExit()
        }
    }
    // Результаты пикера буферизуются на уровне платформы: подписка забирает и тот, что пришёл после пересоздания.
    LaunchedEffect(filePicker) { filePicker.results.collect(viewModel::onFilePicked) }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = viewModel::onBackClick,
    )

    // Один экземпляр на экран: дочерние composable пропускают рекомпозицию по равенству действий.
    val actions = remember(viewModel, filePicker) {
        FirmwareUpdateActions(
            onBack = viewModel::onBackClick,
            onModuleSelected = viewModel::onModuleSelected,
            onChannelSelected = viewModel::onChannelSelected,
            onDownloadClick = viewModel::onDownloadClick,
            onArchiveClick = viewModel::onArchiveClick,
            onFileClick = filePicker::launch,
            onConfirmUpdate = viewModel::onConfirmUpdate,
            onCancelConfirm = viewModel::onCancelConfirm,
            onResultOk = viewModel::onResultOk,
            onRetry = viewModel::onRetry,
            onExitBootClick = viewModel::onExitBootClick,
            onSupportClick = viewModel::onSupportClick,
            onReconnect = viewModel::onReconnect,
            onDisconnect = viewModel::onDisconnect,
            onExitDialogDismiss = viewModel::onExitDialogDismiss,
            onExitConfirm = viewModel::onExitConfirm,
        )
    }
    FirmwareUpdateContent(
        state = state,
        actions = actions,
        sources = sources,
    )
}

@Composable
internal fun FirmwareUpdateContent(
    state: FirmwareUpdateState,
    actions: FirmwareUpdateActions,
    sources: FirmwareSources = FirmwareSources.All,
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.firmware_update_title_screen)) },
                subtitle = state.deviceTitle.takeIf { it.isNotBlank() }?.let { title ->
                    { CmpText(text = subtitleOf(state, title), style = CmpTextStyle.Caption) }
                },
                navigationIcon = {
                    if (state.locked) {
                        LockedBackIcon()
                    } else {
                        CmpIconButton(
                            onClick = actions.onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.firmware_common_a11y_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = firmwareLayoutOf(maxWidth)
            val wide = layout != FirmwareLayout.Compact
            if (wide) {
                CmpRow(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    CmpBox(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        PhaseHero(state.phase, WideHero)
                    }
                    CmpBox(
                        modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center,
                    ) {
                        PhaseBody(
                            state,
                            actions,
                            sources,
                            Modifier.padding(start = 8.dp, end = 32.dp, top = 24.dp, bottom = 24.dp),
                        )
                    }
                }
            } else {
                CmpColumn(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CmpBox(modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)) {
                        PhaseHero(state.phase, CompactHero)
                    }
                    PhaseBody(state, actions, sources, Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }
            }
        }
    }
    if (state.exitDialog) ExitDialog(actions)
}

@Composable
private fun subtitleOf(state: FirmwareUpdateState, title: String): String = deviceSubtitle(state.profile, title)

@Composable
private fun LockedBackIcon() {
    CmpBox(modifier = Modifier.padding(12.dp)) {
        CmpIcon(
            icon = CmpIcons.Lock,
            contentDescription = stringResource(Strings.string.firmware_update_a11y_locked),
            tint = cmpThemeColors().onSurfaceVariant,
        )
    }
}

@Composable
private fun PhaseHero(phase: UpdatePhase, size: Dp) {
    when (phase) {
        UpdatePhase.Success -> FirmwareHero(CmpIcons.Check, size = size)

        is UpdatePhase.Error -> FirmwareHero(CmpIcons.PriorityHigh, tone = HeroTone.Error, size = size)

        is UpdatePhase.Lost -> FirmwareHero(CmpIcons.LinkOff, tone = HeroTone.Error, size = size)

        UpdatePhase.DataError -> FirmwareHero(CmpIcons.Memory, tone = HeroTone.Neutral, size = size)

        UpdatePhase.Source, UpdatePhase.Confirm -> FirmwareHero(CmpIcons.SystemUpdate, size = size)

        UpdatePhase.Loading,
        UpdatePhase.Entering,
        UpdatePhase.Download,
        UpdatePhase.Prepare,
        is UpdatePhase.Transfer,
        UpdatePhase.Verify,
        UpdatePhase.Apply,
        UpdatePhase.Finishing,
        -> FirmwareHero(CmpIcons.SystemUpdate, size = size, pulse = true)
    }
}

@Composable
private fun PhaseBody(
    state: FirmwareUpdateState,
    actions: FirmwareUpdateActions,
    sources: FirmwareSources,
    modifier: Modifier,
) {
    val fileOnly = sources == FirmwareSources.FileOnly
    val source = state.phase == UpdatePhase.Source
    CmpColumn(
        modifier = modifier.widthIn(max = BodyMaxWidth).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = if (source) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        when (val phase = state.phase) {
            UpdatePhase.Loading -> LoadingBody()

            UpdatePhase.Entering -> EnteringBody()

            UpdatePhase.Source -> SourceBody(state, actions, fileOnly)

            UpdatePhase.DataError -> DataErrorBody(actions.onRetry)

            UpdatePhase.Download -> DownloadBody()

            UpdatePhase.Confirm -> state.confirm?.let {
                ConfirmBody(it, state.channel.isTest, actions.onConfirmUpdate, actions.onCancelConfirm)
            }

            UpdatePhase.Prepare, is UpdatePhase.Transfer, UpdatePhase.Verify, UpdatePhase.Apply -> ProgressBody(phase)

            UpdatePhase.Success -> SuccessBody(state.inBoot, state.canExitBoot, actions.onResultOk)

            UpdatePhase.Finishing -> FinishingBody()

            is UpdatePhase.Error -> ErrorBody(
                phase.failure,
                actions.onRetry,
                actions.onSupportClick.takeUnless { fileOnly },
            )

            is UpdatePhase.Lost -> LostBody(phase.updating, actions.onReconnect, actions.onDisconnect)
        }
    }
}

@Composable
private fun ExitDialog(actions: FirmwareUpdateActions) {
    CmpDialog(
        onDismissRequest = actions.onExitDialogDismiss,
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.Warning.imageVector(),
        title = { CmpText(text = stringResource(Strings.string.firmware_update_title_exit)) },
        text = { CmpText(text = stringResource(Strings.string.firmware_update_msg_exit), style = CmpTextStyle.Body) },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_update_btn_exit_stay),
                onClick = actions.onExitDialogDismiss,
                modifier = Modifier.height(48.dp),
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_update_btn_exit_exit),
                onClick = actions.onExitConfirm,
                modifier = Modifier.height(48.dp).testTag(EXIT_DIALOG_CONFIRM_TAG),
            )
        },
    )
}
