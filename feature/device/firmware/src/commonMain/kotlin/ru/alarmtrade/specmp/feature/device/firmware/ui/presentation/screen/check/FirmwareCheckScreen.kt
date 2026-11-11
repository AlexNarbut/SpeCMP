package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_boot_failed
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_title_modules
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_title_screen
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_label_device_id
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_msg_test_channel
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.ChannelChip
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLayout
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.firmwareLayoutOf
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ConfirmBootDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.DescriptionDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModuleCard
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModulesError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModulesLoading
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.PinDialog
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceTitle

private val ContentMaxWidth = 1040.dp

@Composable
fun FirmwareCheckScreen(onBack: () -> Unit, autoBoot: FirmwareAutoBoot? = null) {
    val viewModel = koinViewModel<FirmwareCheckViewModel>(
        parameters = { if (autoBoot != null) parametersOf(autoBoot) else parametersOf() },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Один экземпляр на экран: дочерние composable пропускают рекомпозицию по равенству действий.
    val actions = remember(viewModel, onBack) {
        FirmwareCheckActions(
            onBack = onBack,
            onRetry = viewModel::onRetry,
            onChannelSelected = viewModel::onChannelSelected,
            onRefreshModule = viewModel::onRefreshModule,
            onDescriptionClick = viewModel::onDescriptionClick,
            onUpdateClick = viewModel::onUpdateClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onConfirmBoot = viewModel::onConfirmBoot,
            onPinSubmit = viewModel::onPinSubmit,
        )
    }
    FirmwareCheckContent(
        state = state,
        actions = actions,
    )
}

@Composable
internal fun FirmwareCheckContent(state: FirmwareCheckState, actions: FirmwareCheckActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.firmware_check_title_screen)) },
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = firmwareLayoutOf(maxWidth)
            when (val content = state.content) {
                LCEState.None, LCEState.Loading -> ModulesLoading()
                is LCEState.Error -> ModulesError(error = content.error, onRetry = actions.onRetry)
                is LCEState.Content -> ReadyBody(state, content.content, layout, actions)
            }
        }
    }
    CheckDialogHost(state.dialog, actions)
}

@Composable
private fun ReadyBody(
    state: FirmwareCheckState,
    modules: List<ModuleCardUi>,
    layout: FirmwareLayout,
    actions: FirmwareCheckActions,
) {
    val columns = if (layout == FirmwareLayout.Compact) 1 else 2
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpColumn(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(if (layout == FirmwareLayout.Compact) 14.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.bootFailed) {
                InfoBanner(
                    text = stringResource(Strings.string.firmware_check_msg_boot_failed),
                    tone = BannerTone.Error,
                    icon = CmpIcons.CloudOff,
                )
            }
            state.deviceId?.let { DeviceRow(state.deviceName, it, state.channel, layout, actions.onChannelSelected) }
            if (state.channel.isTest) InfoBanner(text = stringResource(Strings.string.firmware_common_msg_test_channel))
            CmpText(text = stringResource(Strings.string.firmware_check_title_modules), style = CmpTextStyle.Title)
            modules.chunked(columns).forEach { rowCards ->
                CmpRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    rowCards.forEach { card ->
                        ModuleCard(
                            module = card,
                            onRefresh = { actions.onRefreshModule(card.id) },
                            onDescription = { actions.onDescriptionClick(card.id) },
                            onUpdate = { actions.onUpdateClick(card.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Нечётная последняя строка: пустое место, чтобы карточка не растягивалась на всю ширину.
                    if (rowCards.size <
                        columns
                    ) {
                        CmpColumn(modifier = Modifier.weight((columns - rowCards.size).toFloat())) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    name: String,
    id: Long,
    channel: FirmwareChannel,
    layout: FirmwareLayout,
    onChannelSelect: (FirmwareChannel) -> Unit,
) {
    val colors = cmpThemeColors()
    val idText = stringResource(Strings.string.firmware_common_label_device_id, id.toString())
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        if (layout == FirmwareLayout.Compact) {
            CmpColumn(
                modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DeviceNameRow(name, idText = null)
                CmpRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 38.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(text = idText, color = colors.onSurfaceVariant, style = CmpTextStyle.Body)
                    ChannelChip(channel, onChannelSelect)
                }
            }
        } else {
            CmpRow(
                modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DeviceNameRow(name, idText, modifier = Modifier.weight(1f))
                ChannelChip(channel, onChannelSelect)
            }
        }
    }
}

@Composable
private fun DeviceNameRow(name: String, idText: String?, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpIcon(
            icon = CmpIcons.Sensors,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = colors.primary,
        )
        CmpText(text = name, modifier = Modifier.weight(1f))
        idText?.let { CmpText(text = it, color = colors.onSurfaceVariant, style = CmpTextStyle.Body) }
    }
}

@Composable
private fun CheckDialogHost(dialog: CheckDialog?, actions: FirmwareCheckActions) {
    when (dialog) {
        null -> Unit

        is CheckDialog.Description -> DescriptionDialog(
            dialog = dialog,
            onDismiss = actions.onDialogDismiss,
            onUpdate = { actions.onUpdateClick(dialog.moduleId) },
        )

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
    }
}
