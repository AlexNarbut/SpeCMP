package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import specmp.feature.device.interaction_ui.generated.resources.Res
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_a11y_back
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_boot_subtitle
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_boot_title
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_btn_exit
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_connecting_title
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_disconnected_subtitle
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_disconnected_title

/**
 * Обёртка flow работы с подключённым устройством (аналог `DeviceInteractionComponent` старого приложения).
 *
 * - Блок на связи в рабочем режиме — [content] (граф дочернего flow).
 * - Связь потеряна или ещё устанавливается — экран состояния **поверх** [content]: дочерний flow
 *   остаётся в композиции (его back stack и VM живы), после переподключения пользователь там же.
 * - «Назад» на экране состояния — выход из всего flow (сессия закрывается), а не из подэкрана.
 * - Блок в загрузчике — экран режима загрузчика вместо [content].
 *
 * @param content дочерний flow; получает блок и выход из flow (закрывает сессию).
 */
@Composable
fun DeviceInteractionHost(
    onExit: () -> Unit,
    content: @Composable (device: AlarmDevice, onExit: () -> Unit) -> Unit,
) {
    val viewModel = koinViewModel<DeviceInteractionViewModel>()
    val state by viewModel.state.collectAsState()

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            DeviceInteractionEffect.Exit -> onExit()
        }
    }

    val device = state.device
    CmpBox(modifier = Modifier.fillMaxSize()) {
        if (device != null && device.mode == DeviceOperationMode.MAIN) {
            content(device, viewModel::onExit)
        }
        when {
            state.session !is DeviceSessionState.Ready -> StatusScreen(
                status = if (state.session is DeviceSessionState.Disconnected) Status.Disconnected else Status.Connecting,
                deviceName = device?.systemName,
                onExit = viewModel::onExit,
            )

            device?.mode == DeviceOperationMode.BOOT -> StatusScreen(
                status = Status.Boot,
                deviceName = device.systemName,
                onExit = viewModel::onExit,
            )
        }
    }
}

private enum class Status { Connecting, Disconnected, Boot }

/** Полноэкранное состояние поверх дочернего flow; перехватывает «Назад» (регистрируется позже content). */
@Composable
private fun StatusScreen(status: Status, deviceName: String?, onExit: () -> Unit) {
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = onExit,
    )
    val colors = cmpThemeColors()
    // Surface поверх content перехватывает касания.
    CmpSurface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = { CmpText(text = deviceName.orEmpty()) },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = onExit,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.device_interaction_a11y_back),
                        )
                    },
                )
            },
        ) { padding ->
            CmpBox(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CmpColumn(
                    modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    val isError = status != Status.Connecting
                    CmpSurface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = if (isError) colors.errorContainer else colors.primaryContainer,
                    ) {
                        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CmpIcon(
                                icon = when (status) {
                                    Status.Connecting -> CmpIcons.Link
                                    Status.Disconnected -> CmpIcons.BluetoothDisabled
                                    Status.Boot -> CmpIcons.Memory
                                },
                                contentDescription = null,
                                tint = if (isError) colors.onErrorContainer else colors.onPrimaryContainer,
                            )
                        }
                    }
                    when (status) {
                        Status.Connecting -> {
                            CmpText(
                                text = stringResource(Res.string.device_interaction_connecting_title),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                            CmpCircularProgressIndicator()
                        }

                        Status.Disconnected -> Headline(
                            title = stringResource(Res.string.device_interaction_disconnected_title),
                            subtitle = stringResource(Res.string.device_interaction_disconnected_subtitle),
                        )

                        Status.Boot -> Headline(
                            title = stringResource(Res.string.device_interaction_boot_title),
                            subtitle = stringResource(Res.string.device_interaction_boot_subtitle),
                        )
                    }
                    CmpButton(
                        text = stringResource(Res.string.device_interaction_btn_exit),
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Headline(title: String, subtitle: String) {
    val colors = cmpThemeColors()
    CmpColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpText(text = title, fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        CmpText(text = subtitle, fontSize = 16.sp, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
