package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_boot_failed
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_update_file
import ru.alarmtrade.specmp.core.localization.resources.firmware_file_msg_hint
import ru.alarmtrade.specmp.core.localization.resources.firmware_file_msg_not_supported
import ru.alarmtrade.specmp.core.localization.resources.firmware_file_title_screen
import ru.alarmtrade.specmp.core.localization.resources.firmware_update_label_version
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareHero
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.HeroTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ConfirmBootDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModulesError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModulesLoading
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.PinDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.BigButtonHeight
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.subcomponents.Message
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle

private val BodyMaxWidth = 480.dp
private val HeroSize = 128.dp

internal const val FILE_UPDATE_TAG = "firmware_file_update"

@Composable
fun FirmwareFileScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<FirmwareFileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) {
        FirmwareFileActions(
            onBack = onBack,
            onRetry = viewModel::onRetry,
            onUpdateClick = viewModel::onUpdateClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onConfirmBoot = viewModel::onConfirmBoot,
            onPinSubmit = viewModel::onPinSubmit,
        )
    }
    FirmwareFileContent(state = state, actions = actions)
}

@Composable
internal fun FirmwareFileContent(state: FirmwareFileState, actions: FirmwareFileActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.firmware_file_title_screen)) },
                subtitle = state.deviceId?.let { id ->
                    { CmpText(text = deviceTitle(state.deviceName, id), style = CmpTextStyle.Caption) }
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.firmware_common_a11y_back),
                    )
                },
            )
        },
    ) { padding ->
        CmpBox(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val target = state.target) {
                LCEState.None, LCEState.Loading -> ModulesLoading()
                is LCEState.Error -> ModulesError(error = target.error, onRetry = actions.onRetry)
                is LCEState.Content -> ReadyBody(state, target.content, actions.onUpdateClick)
            }
        }
    }
    when (val dialog = state.dialog) {
        is CheckDialog.ConfirmBoot -> ConfirmBootDialog(
            dialog = dialog,
            onDismiss = actions.onDialogDismiss,
            onConfirm = actions.onConfirmBoot,
        )

        is CheckDialog.Pin -> PinDialog(
            dialog = dialog,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit,
        )

        is CheckDialog.Description, null -> Unit
    }
}

@Composable
private fun ReadyBody(state: FirmwareFileState, target: FileTarget, onUpdateClick: () -> Unit) {
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = BodyMaxWidth).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (target) {
                is FileTarget.Supported -> FirmwareHero(CmpIcons.SystemUpdate, size = HeroSize)
                FileTarget.NotSupported -> FirmwareHero(CmpIcons.Memory, tone = HeroTone.Neutral, size = HeroSize)
            }
            CmpText(text = state.deviceName, fontSize = 22.sp, style = CmpTextStyle.Title)
            state.firmwareVersion?.let { version ->
                CmpText(text = stringResource(Strings.string.firmware_update_label_version), style = CmpTextStyle.Body)
                VersionText(version, fontSize = 20.sp)
            }
            if (state.bootFailed) {
                InfoBanner(
                    text = stringResource(Strings.string.firmware_check_msg_boot_failed),
                    tone = BannerTone.Error,
                    icon = CmpIcons.CloudOff,
                )
            }
            when (target) {
                is FileTarget.Supported -> {
                    Message(stringResource(Strings.string.firmware_file_msg_hint))
                    CmpButton(
                        text = stringResource(Strings.string.firmware_common_btn_update_file),
                        onClick = onUpdateClick,
                        modifier = Modifier.fillMaxWidth().height(BigButtonHeight).testTag(FILE_UPDATE_TAG),
                    )
                }

                FileTarget.NotSupported -> InfoBanner(
                    text = stringResource(Strings.string.firmware_file_msg_not_supported),
                )
            }
        }
    }
}
