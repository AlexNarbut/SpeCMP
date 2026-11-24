package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_applying
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_boot_timeout
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_connection
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_crc
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_no_module
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_not_in_boot
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_server_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_transfer
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_unknown
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_label_boot_mode
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_label_device_version
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_label_written_version
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_msg_boot
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_msg_other_device
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_msg_ready
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_msg_ready_boot
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_msg_still_in_boot
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_msg_success
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_title_boot
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_title_screen
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_source
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareScreenFrame
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.text
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.NoFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveVehiclePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveEntry
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareSources
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdatePhase
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ConfirmBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.DownloadBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ErrorContent
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.FileErrorBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.Headline
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.Message
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ProgressBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.SourceErrorBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.SourcePanel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.SourcePanelActions
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.SuccessContent

internal const val SERIAL_UPDATE_FILE_TAG = "firmware_serial_update_file"
internal const val SERIAL_RESULT_OK_TAG = "firmware_serial_result_ok"

/**
 * Контент хоста и в рабочем режиме блока, и в загрузчике; «Назад» заблокирован, пока идёт обновление.
 * [sources] — откуда брать прошивку: [FirmwareSources.All] — ещё сервер и архив (`.frm` основного модуля модели).
 */
@Composable
fun SerialFirmwareScreen(
    onExit: () -> Unit,
    filePicker: FirmwareFilePicker = NoFirmwareFilePicker,
    sources: FirmwareSources = FirmwareSources.FileOnly,
    vehiclePicker: ArchiveVehiclePicker? = null,
) {
    val viewModel = koinViewModel<SerialFirmwareViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            SerialFirmwareEffect.Exit -> onExit()
        }
    }
    // Результаты пикера буферизуются на уровне платформы: подписка забирает и тот, что пришёл после пересоздания.
    LaunchedEffect(filePicker) { filePicker.results.collect(viewModel::onFilePicked) }

    val archive = state.archive
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = if (archive != null) viewModel::onArchiveClosed else viewModel::onBack,
    )
    if (archive != null) {
        FirmwareArchiveEntry(
            request = archive,
            onPick = viewModel::onArchivePicked,
            onBack = viewModel::onArchiveClosed,
            vehiclePicker = vehiclePicker,
        )
        return
    }
    val actions = remember(viewModel, filePicker) {
        SerialFirmwareActions(
            onBack = viewModel::onBack,
            onChannelSelected = viewModel::onChannelSelected,
            onDownloadClick = viewModel::onDownloadClick,
            onArchiveClick = viewModel::onArchiveClick,
            onFileClick = filePicker::launch,
            onConfirmUpdate = viewModel::onConfirmUpdate,
            onCancelConfirm = viewModel::onCancelConfirm,
            onResultOk = viewModel::onResultOk,
        )
    }
    SerialFirmwareContent(state = state, actions = actions, sources = sources)
}

@Composable
internal fun SerialFirmwareContent(
    state: SerialFirmwareState,
    actions: SerialFirmwareActions,
    sources: FirmwareSources = FirmwareSources.FileOnly,
) {
    FirmwareScreenFrame(
        title = stringResource(Strings.string.firmware_serial_title_screen),
        subtitle = state.deviceTitle,
        locked = state.locked,
        onBack = actions.onBack,
        bodyAlignment = if (state.phase == SerialPhase.Ready) Alignment.Start else Alignment.CenterHorizontally,
        hero = { size -> PhaseHero(state.phase, size) },
    ) {
        PhaseBody(state, actions, sources)
    }
}

@Composable
private fun PhaseHero(phase: SerialPhase, size: Dp) {
    when (phase) {
        is SerialPhase.Success -> FirmwareHero(CmpIcons.Check, size = size)

        is SerialPhase.Error -> FirmwareHero(CmpIcons.PriorityHigh, tone = HeroTone.Error, size = size)

        SerialPhase.WaitingBoot -> FirmwareHero(CmpIcons.PowerSettingsNew, size = size, pulse = true)

        SerialPhase.Ready, is SerialPhase.Confirm -> FirmwareHero(CmpIcons.SystemUpdate, size = size)

        SerialPhase.Download,
        SerialPhase.CheckingBoot,
        is SerialPhase.Transfer,
        SerialPhase.Verify,
        SerialPhase.Apply,
        -> FirmwareHero(CmpIcons.SystemUpdate, size = size, pulse = true)
    }
}

@Composable
private fun PhaseBody(state: SerialFirmwareState, actions: SerialFirmwareActions, sources: FirmwareSources) {
    when (val phase = state.phase) {
        SerialPhase.Ready -> ReadyBody(state, actions, fileOnly = sources == FirmwareSources.FileOnly)

        SerialPhase.Download -> DownloadBody()

        is SerialPhase.Confirm -> ConfirmPhaseBody(phase, state, actions)

        SerialPhase.WaitingBoot -> {
            Headline(stringResource(Strings.string.firmware_serial_title_boot))
            Message(stringResource(Strings.string.firmware_serial_msg_boot))
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconXl))
        }

        SerialPhase.CheckingBoot -> ProgressBody(UpdatePhase.Prepare)

        is SerialPhase.Transfer -> ProgressBody(UpdatePhase.Transfer(phase.percent))

        SerialPhase.Verify -> ProgressBody(UpdatePhase.Verify)

        SerialPhase.Apply -> ProgressBody(UpdatePhase.Apply)

        is SerialPhase.Success -> SuccessBody(phase, state, actions.onResultOk)

        is SerialPhase.Error -> ErrorContent(
            message = stringResource(phase.failure.messageRes()),
            onRetry = actions.onResultOk,
            retryTag = SERIAL_RESULT_OK_TAG,
        )
    }
}

@Composable
private fun ReadyBody(state: SerialFirmwareState, actions: SerialFirmwareActions, fileOnly: Boolean) {
    val panelActions = remember(actions) {
        SourcePanelActions(
            onChannelSelected = actions.onChannelSelected,
            onDownloadClick = actions.onDownloadClick,
            onArchiveClick = actions.onArchiveClick,
            onFileClick = actions.onFileClick,
        )
    }
    SourcePanel(
        moduleTitles = emptyList(),
        selectedModule = 0,
        currentVersion = state.deviceVersion,
        channel = state.channel,
        fileOnly = fileOnly,
        actions = panelActions,
        blockCan = state.blockCan,
        fileTag = SERIAL_UPDATE_FILE_TAG,
        versionBadge = {
            if (state.deviceInBoot) {
                CmpTag(text = stringResource(Strings.string.firmware_serial_label_boot_mode), tone = CmpTagTone.Warning)
            }
        },
        messages = { ReadyMessages(state) },
    )
}

@Composable
private fun ReadyMessages(state: SerialFirmwareState) {
    // Блок уже в загрузчике — GOBOOT не нужен, перезагружать по питанию не придётся.
    CmpBanner(
        text = stringResource(
            if (state.deviceInBoot) {
                Strings.string.firmware_serial_msg_ready_boot
            } else {
                Strings.string.firmware_serial_msg_ready
            },
        ),
        icon = CmpIcons.PowerSettingsNew,
    )
    state.sourceError?.let { error ->
        SourceErrorBanner(
            when (error) {
                is SerialSourceError.Server -> error.error.text(Strings.string.firmware_update_error_source)
                SerialSourceError.NoModule -> stringResource(Strings.string.firmware_serial_error_no_module)
            },
        )
    }
    state.fileError?.let { FileErrorBanner(it) }
    state.fileProblem?.let { problem ->
        CmpBanner(
            text = stringResource(
                when (problem) {
                    SerialFileProblem.NotFrm -> Strings.string.firmware_serial_error_file
                    SerialFileProblem.ServerNotFrm -> Strings.string.firmware_serial_error_server_file
                },
            ),
            tone = CmpBannerTone.Error,
            icon = CmpIcons.ErrorOutline,
        )
    }
}

@Composable
private fun ConfirmPhaseBody(phase: SerialPhase.Confirm, state: SerialFirmwareState, actions: SerialFirmwareActions) {
    phase.nameMismatch?.let { mismatch ->
        CmpBanner(
            text = stringResource(
                Strings.string.firmware_serial_msg_other_device,
                mismatch.fileSystemName,
                mismatch.deviceSystemName,
            ),
            tone = CmpBannerTone.Warning,
        )
    }
    ConfirmBody(
        confirm = ConfirmUi(
            currentVersion = state.deviceVersion,
            newVersion = phase.fileVersion ?: phase.fileName,
            description = phase.description,
            older = phase.older,
            localFile = false,
        ),
        testChannel = phase.fromServer && state.channel.isTest,
        onUpdate = actions.onConfirmUpdate,
        onCancel = actions.onCancelConfirm,
    )
}

@Composable
private fun SuccessBody(phase: SerialPhase.Success, state: SerialFirmwareState, onOk: () -> Unit) {
    SuccessContent(
        message = stringResource(Strings.string.firmware_serial_msg_success),
        onOk = onOk,
        okTag = SERIAL_RESULT_OK_TAG,
    ) {
        phase.writtenVersion?.let { version ->
            Message(stringResource(Strings.string.firmware_serial_label_written_version, version))
        }
        // В загрузчике блок может отдавать версию загрузчика — версию прошивки покажет только рабочий режим.
        state.deviceVersion?.let { version ->
            if (state.deviceInBoot) {
                CmpBanner(text = stringResource(Strings.string.firmware_serial_msg_still_in_boot))
            } else {
                Message(stringResource(Strings.string.firmware_serial_label_device_version, version))
            }
        }
    }
}

private fun SerialFirmwareFailure.messageRes(): StringResource = when (this) {
    SerialFirmwareFailure.File -> Strings.string.firmware_serial_error_file
    SerialFirmwareFailure.BootTimeout -> Strings.string.firmware_serial_error_boot_timeout
    SerialFirmwareFailure.NotInBoot -> Strings.string.firmware_serial_error_not_in_boot
    SerialFirmwareFailure.Transfer -> Strings.string.firmware_serial_error_transfer
    SerialFirmwareFailure.Crc -> Strings.string.firmware_serial_error_crc
    SerialFirmwareFailure.Applying -> Strings.string.firmware_serial_error_applying
    SerialFirmwareFailure.ConnectionLost -> Strings.string.firmware_serial_error_connection
    SerialFirmwareFailure.Unknown -> Strings.string.firmware_serial_error_unknown
}
