package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.BannerTone
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.FirmwareLayout
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.InfoBanner
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.firmwareLayoutOf
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ChannelPanel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ConfirmBootDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.DescriptionDialog
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModuleCard
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModulesError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.ModulesLoading
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents.PinDialog
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_msg_boot_failed
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_title_modules
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_title_screen
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_a11y_back
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_label_device_id

private val ContentMaxWidth = 1040.dp

/**
 * «Проверка прошивки»: слот графа расширенного монтажа (вход и выход — через [onBack]); переход в режим обновления
 * запускает ViewModel, дальше экран загрузчика показывает хост устройства.
 */
@Composable
fun FirmwareCheckScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<FirmwareCheckViewModel>()
    val state by viewModel.state.collectAsState()
    FirmwareCheckContent(
        state = state,
        actions = FirmwareCheckActions(
            onBack = onBack,
            onRetry = viewModel::onRetry,
            onChannelSelected = viewModel::onChannelSelected,
            onRefreshModule = viewModel::onRefreshModule,
            onDescriptionClick = viewModel::onDescriptionClick,
            onUpdateClick = viewModel::onUpdateClick,
            onDialogDismiss = viewModel::onDialogDismiss,
            onConfirmBoot = viewModel::onConfirmBoot,
            onPinSubmit = viewModel::onPinSubmit
        )
    )
}

/** Stateless-контент экрана проверки: раскладка по ширине окна и диалоги. */
@Composable
internal fun FirmwareCheckContent(state: FirmwareCheckState, actions: FirmwareCheckActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Res.string.firmware_check_title_screen)) },
                subtitle = state.deviceId?.let { id ->
                    { CmpText(text = "${state.deviceName}  $id", style = CmpTextStyle.Caption) }
                },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.firmware_common_a11y_back)
                    )
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = firmwareLayoutOf(maxWidth)
            when (val content = state.content) {
                CheckContent.Loading -> ModulesLoading()
                CheckContent.Error -> ModulesError(onRetry = actions.onRetry)
                is CheckContent.Ready -> ReadyBody(state, content, layout, actions)
            }
        }
    }
    CheckDialogHost(state.dialog, actions)
}

@Composable
private fun ReadyBody(
    state: FirmwareCheckState,
    content: CheckContent.Ready,
    layout: FirmwareLayout,
    actions: FirmwareCheckActions
) {
    val columns = if (layout == FirmwareLayout.Compact) 1 else 2
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CmpColumn(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(if (layout == FirmwareLayout.Compact) 14.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.bootFailed) {
                InfoBanner(
                    text = stringResource(Res.string.firmware_check_msg_boot_failed),
                    tone = BannerTone.Error,
                    icon = CmpIcons.CloudOff
                )
            }
            state.deviceId?.let { DeviceRow(state.deviceName, it) }
            ChannelPanel(channel = state.channel, layout = layout, onSelect = actions.onChannelSelected)
            CmpText(text = stringResource(Res.string.firmware_check_title_modules), style = CmpTextStyle.Title)
            content.modules.chunked(columns).forEach { rowCards ->
                CmpRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                    rowCards.forEach { card ->
                        ModuleCard(
                            module = card,
                            onRefresh = { actions.onRefreshModule(card.id) },
                            onDescription = { actions.onDescriptionClick(card.id) },
                            onUpdate = { actions.onUpdateClick(card.id) },
                            modifier = Modifier.weight(1f)
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
private fun DeviceRow(name: String, id: Long) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLow
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpIcon(
                icon = CmpIcons.Sensors,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = colors.primary
            )
            CmpText(text = name, modifier = Modifier.weight(1f))
            CmpText(
                text = stringResource(Res.string.firmware_common_label_device_id, id.toString()),
                color = colors.onSurfaceVariant,
                style = CmpTextStyle.Body
            )
        }
    }
}

@Composable
private fun CheckDialogHost(dialog: CheckDialog?, actions: FirmwareCheckActions) {
    when (dialog) {
        null -> Unit

        is CheckDialog.Description -> DescriptionDialog(
            dialog = dialog,
            onDismiss = actions.onDialogDismiss,
            onUpdate = { actions.onUpdateClick(dialog.moduleId) }
        )

        is CheckDialog.ConfirmBoot -> ConfirmBootDialog(
            dialog = dialog,
            onDismiss = actions.onDialogDismiss,
            onConfirm = actions.onConfirmBoot
        )

        is CheckDialog.Pin -> PinDialog(
            dialog = dialog,
            onDismiss = actions.onDialogDismiss,
            onSubmit = actions.onPinSubmit
        )
    }
}
