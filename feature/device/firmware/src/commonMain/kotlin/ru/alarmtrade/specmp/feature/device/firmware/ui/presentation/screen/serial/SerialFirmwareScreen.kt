package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_update_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_applying
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_boot_timeout
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_connection
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_crc
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_error_not_in_boot
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
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_title_ready
import ru.alarmtrade.specmp.core.localization.resources.firmware_serial_title_screen
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_a11y_locked
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_file_too_large
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_error_title
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_version
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_title_success
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.NoFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.ConfirmUi
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdatePhase
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.BigButtonHeight
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ConfirmBody
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.Headline
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.Message
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.ProgressBody

private val BodyMaxWidth = 480.dp
private val HeroSize = 128.dp

internal const val SERIAL_UPDATE_FILE_TAG = "firmware_serial_update_file"
internal const val SERIAL_RESULT_OK_TAG = "firmware_serial_result_ok"

/**
 * «Прошивка по COM-порту»: публичная точка входа (приложение прошивки, `flasherApp`) — контент хоста устройства
 * для сессии [ConnectionTransport.SERIAL][ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport]
 * и в рабочем режиме блока, и в загрузчике. «Назад» заблокирован, пока идёт обновление.
 *
 * @param onExit выход из flow устройства (закрывает сессию).
 * @param filePicker выбор файла прошивки в файловом менеджере (платформа, composition root).
 */
@Composable
fun SerialFirmwareScreen(onExit: () -> Unit, filePicker: FirmwareFilePicker = NoFirmwareFilePicker) {
    val viewModel = koinViewModel<SerialFirmwareViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            SerialFirmwareEffect.Exit -> onExit()
        }
    }
    // Результаты пикера буферизуются на уровне платформы: подписка забирает и тот, что пришёл после пересоздания.
    LaunchedEffect(filePicker) { filePicker.results.collect(viewModel::onFilePicked) }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = viewModel::onBack,
    )

    val actions = remember(viewModel, filePicker) {
        SerialFirmwareActions(
            onBack = viewModel::onBack,
            onFileClick = filePicker::launch,
            onConfirmUpdate = viewModel::onConfirmUpdate,
            onCancelConfirm = viewModel::onCancelConfirm,
            onResultOk = viewModel::onResultOk,
        )
    }
    SerialFirmwareContent(state = state, actions = actions)
}

/** Stateless-контент экрана прошивки по COM: иллюстрация и тело по фазе. */
@Composable
internal fun SerialFirmwareContent(state: SerialFirmwareState, actions: SerialFirmwareActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.firmware_serial_title_screen)) },
                subtitle = state.deviceTitle?.let { name ->
                    { CmpText(text = name, style = CmpTextStyle.Caption) }
                },
                navigationIcon = {
                    if (state.locked) {
                        CmpBox(modifier = Modifier.padding(12.dp)) {
                            CmpIcon(
                                icon = CmpIcons.Lock,
                                contentDescription = stringResource(Strings.string.firmware_update_a11y_locked),
                                tint = cmpThemeColors().onSurfaceVariant,
                            )
                        }
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
        CmpColumn(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CmpBox(modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)) { PhaseHero(state.phase) }
            CmpColumn(
                modifier = Modifier
                    .widthIn(max = BodyMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PhaseBody(state, actions)
            }
        }
    }
}

@Composable
private fun PhaseHero(phase: SerialPhase) {
    when (phase) {
        is SerialPhase.Success -> FirmwareHero(CmpIcons.Check, size = HeroSize)

        is SerialPhase.Error -> FirmwareHero(CmpIcons.PriorityHigh, tone = HeroTone.Error, size = HeroSize)

        SerialPhase.WaitingBoot -> FirmwareHero(CmpIcons.PowerSettingsNew, size = HeroSize, pulse = true)

        SerialPhase.Ready, is SerialPhase.Confirm -> FirmwareHero(CmpIcons.Cable, size = HeroSize)

        SerialPhase.CheckingBoot,
        is SerialPhase.Transfer,
        SerialPhase.Verify,
        SerialPhase.Apply,
        -> FirmwareHero(CmpIcons.SystemUpdate, size = HeroSize, pulse = true)
    }
}

@Composable
private fun PhaseBody(state: SerialFirmwareState, actions: SerialFirmwareActions) {
    when (val phase = state.phase) {
        SerialPhase.Ready -> ReadyBody(state, actions.onFileClick)

        is SerialPhase.Confirm -> ConfirmPhaseBody(phase, state.deviceVersion, actions)

        SerialPhase.WaitingBoot -> {
            Headline(stringResource(Strings.string.firmware_serial_title_boot))
            Message(stringResource(Strings.string.firmware_serial_msg_boot))
            CmpCircularProgressIndicator(modifier = Modifier.size(40.dp))
        }

        SerialPhase.CheckingBoot -> ProgressBody(UpdatePhase.Prepare)

        is SerialPhase.Transfer -> ProgressBody(UpdatePhase.Transfer(phase.percent))

        SerialPhase.Verify -> ProgressBody(UpdatePhase.Verify)

        SerialPhase.Apply -> ProgressBody(UpdatePhase.Apply)

        is SerialPhase.Success -> SuccessBody(phase, state, actions.onResultOk)

        is SerialPhase.Error -> {
            Headline(stringResource(Strings.string.firmware_update_error_title))
            InfoBanner(
                text = stringResource(phase.failure.messageRes()),
                tone = BannerTone.Error,
                icon = CmpIcons.AlertCircle,
            )
            BigButton(Strings.string.firmware_common_btn_retry, actions.onResultOk, SERIAL_RESULT_OK_TAG)
        }
    }
}

@Composable
private fun ConfirmPhaseBody(phase: SerialPhase.Confirm, deviceVersion: String?, actions: SerialFirmwareActions) {
    phase.nameMismatch?.let { mismatch ->
        InfoBanner(
            text = stringResource(
                Strings.string.firmware_serial_msg_other_device,
                mismatch.fileSystemName,
                mismatch.deviceSystemName,
            ),
            tone = BannerTone.Warning,
        )
    }
    ConfirmBody(
        confirm = ConfirmUi(
            currentVersion = deviceVersion,
            newVersion = phase.fileVersion ?: phase.fileName,
            description = "",
            older = phase.older,
            localFile = false,
        ),
        testChannel = false,
        onUpdate = actions.onConfirmUpdate,
        onCancel = actions.onCancelConfirm,
    )
}

@Composable
private fun SuccessBody(phase: SerialPhase.Success, state: SerialFirmwareState, onOk: () -> Unit) {
    Headline(stringResource(Strings.string.firmware_update_title_success))
    Message(stringResource(Strings.string.firmware_serial_msg_success))
    phase.writtenVersion?.let { version ->
        Message(stringResource(Strings.string.firmware_serial_label_written_version, version))
    }
    // В загрузчике блок может отдавать версию загрузчика — версию прошивки покажет только рабочий режим.
    state.deviceVersion?.let { version ->
        if (state.deviceInBoot) {
            InfoBanner(text = stringResource(Strings.string.firmware_serial_msg_still_in_boot))
        } else {
            Message(stringResource(Strings.string.firmware_serial_label_device_version, version))
        }
    }
    BigButton(Strings.string.firmware_update_btn_ok, onOk, SERIAL_RESULT_OK_TAG)
}

@Composable
private fun ReadyBody(state: SerialFirmwareState, onFileClick: () -> Unit) {
    Headline(stringResource(Strings.string.firmware_serial_title_ready))
    state.deviceVersion?.let { version ->
        CmpText(text = stringResource(Strings.string.firmware_update_label_version), style = CmpTextStyle.Body)
        VersionText(version, fontSize = 20.sp)
        if (state.deviceInBoot) {
            CmpText(text = stringResource(Strings.string.firmware_serial_label_boot_mode), style = CmpTextStyle.Caption)
        }
    }
    // Блок уже в загрузчике — GOBOOT не нужен, перезагружать по питанию не придётся.
    val readyMessage = if (state.deviceInBoot) {
        Strings.string.firmware_serial_msg_ready_boot
    } else {
        Strings.string.firmware_serial_msg_ready
    }
    Message(stringResource(readyMessage))
    state.fileError?.let { error ->
        InfoBanner(
            text = stringResource(
                when (error) {
                    FileError.TooLarge -> Strings.string.firmware_update_error_file_too_large
                    FileError.Failed -> Strings.string.firmware_update_error_file
                },
            ),
            tone = BannerTone.Error,
            icon = CmpIcons.ErrorOutline,
        )
    }
    state.fileProblem?.let { problem ->
        InfoBanner(
            text = when (problem) {
                SerialFileProblem.NotFrm -> stringResource(Strings.string.firmware_serial_error_file)
            },
            tone = BannerTone.Error,
            icon = CmpIcons.ErrorOutline,
        )
    }
    BigButton(Strings.string.firmware_common_btn_update_file, onFileClick, SERIAL_UPDATE_FILE_TAG)
}

@Composable
private fun BigButton(text: StringResource, onClick: () -> Unit, tag: String) {
    CmpButton(
        text = stringResource(text),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(tag),
    )
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
