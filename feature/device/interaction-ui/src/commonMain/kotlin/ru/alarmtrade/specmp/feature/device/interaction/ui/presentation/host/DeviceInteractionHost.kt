// *Body-функции эмитят несколько узлов в Column родителя: обёртка добавила бы лишний layout-узел
@file:Suppress("ktlint:compose:multiple-emitters-check")

package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_common_btn_disconnect
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_common_btn_exit
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_common_msg_waiting
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_connecting_title
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_disconnected_msg_ble
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_disconnected_msg_serial
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_disconnected_msg_usb
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_disconnected_subtitle
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_disconnected_title
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import specmp.feature.device.interaction_ui.generated.resources.Res
import specmp.feature.device.interaction_ui.generated.resources.device_link_lost

/**
 * Связь потеряна — экран состояния поверх [content]: дочерний flow остаётся в композиции (back stack и VM живы).
 * Блок в загрузчике или его ждём ([DeviceInteractionState.showBootContent]) — [bootContent] вместо оверлеев связи.
 */
@Composable
fun DeviceInteractionHost(
    onExit: () -> Unit,
    bootContent: @Composable (onExit: () -> Unit) -> Unit,
    content: @Composable (device: AlarmDevice, onExit: () -> Unit) -> Unit,
) {
    val viewModel = koinViewModel<DeviceInteractionViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            DeviceInteractionEffect.Exit -> onExit()
        }
    }

    val device = state.device
    val linkLossHolds = remember { LinkLossHolds() }
    CmpBox(modifier = Modifier.fillMaxSize()) {
        if (device != null) {
            CompositionLocalProvider(LocalLinkLossHolds provides linkLossHolds) {
                content(device, viewModel::onExit)
            }
        }
        if (state.showBootContent) {
            bootContent(viewModel::onExit)
        } else {
            // Ожидаемая перезагрузка в рабочем режиме: ожидание рисует сам flow (мастер быстрого монтажа);
            // не дождались за тайм-аут — ожидание снимается, и обрыв снова показывается как потеря связи.
            val status = if (
                state.reboot == DeviceReboot.Restart || linkLossHolds.count > 0 || state.linkOverlaySuppressed
            ) {
                null
            } else {
                when (state.session) {
                    is DeviceSessionState.Ready -> null

                    DeviceSessionState.Connecting -> DeviceStatus.Connecting

                    DeviceSessionState.Disconnected -> DeviceStatus.Disconnected

                    // Загрузчик всегда обслуживает bootContent (showBootContent); сюда не попадаем.
                    is DeviceSessionState.BluetoothBoot, is DeviceSessionState.FirmwareBoot -> null
                }
            }
            status?.let {
                NavigationBackHandler(
                    state = rememberNavigationEventState(NavigationEventInfo.None),
                    onBackCompleted = viewModel::onExit,
                )
                DeviceStatusContent(
                    status = it,
                    transport = state.transport,
                    deviceName = device?.systemName,
                    onExit = viewModel::onExit,
                )
            }
        }
    }
}

/**
 * Экран внутри [DeviceInteractionHost], который сам показывает «нет связи» и остаётся рабочим без блока (лог
 * состояния), — оверлеи «Потеря связи»/«Подключение» его не перекрывают, пока он в композиции.
 */
@Composable
fun KeepContentOnLinkLoss() {
    val holds = LocalLinkLossHolds.current ?: return
    DisposableEffect(holds) {
        holds.count++
        onDispose { holds.count-- }
    }
}

@Stable
private class LinkLossHolds {
    var count by mutableIntStateOf(0)
}

private val LocalLinkLossHolds = staticCompositionLocalOf<LinkLossHolds?> { null }

internal const val DEVICE_STATUS_EXIT_TAG = "device_status_exit"

internal const val DEVICE_STATUS_DISCONNECT_TAG = "device_status_disconnect"

internal enum class DeviceStatus { Connecting, Disconnected }

private val ContentMaxWidth = 460.dp
private val HeroSize = 96.dp
private val HeroIconSize = 48.dp
private val LinkLostWidth = 176.dp
private val LinkLostHeight = 121.dp

@Composable
internal fun DeviceStatusContent(
    status: DeviceStatus,
    transport: ConnectionTransport?,
    deviceName: String?,
    onExit: () -> Unit,
) {
    // Surface поверх content перехватывает касания.
    CmpSurface(modifier = Modifier.fillMaxSize(), color = CmpTheme.colors.backgroundPagePrimary) {
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = if (status == DeviceStatus.Disconnected) "" else deviceName.orEmpty(),
                    navigationIcon = {
                        CmpIconButton(
                            onClick = onExit,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.deviceinteraction_common_a11y_back),
                        )
                    },
                )
            },
        ) { padding ->
            CmpBox(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center,
            ) {
                CmpColumn(
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
                ) {
                    when (status) {
                        DeviceStatus.Disconnected -> DisconnectedBody(transport, onExit)

                        DeviceStatus.Connecting -> {
                            StatusHero(CmpIcons.Link)
                            CmpText(
                                text = stringResource(Strings.string.deviceinteraction_connecting_title),
                                style = CmpTextStyle.Headline1,
                                textAlign = TextAlign.Center,
                            )
                            CmpCircularProgressIndicator()
                            CmpButton(
                                text = stringResource(Strings.string.deviceinteraction_common_btn_exit),
                                onClick = onExit,
                                fullWidth = true,
                                modifier = Modifier.testTag(DEVICE_STATUS_EXIT_TAG),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisconnectedBody(transport: ConnectionTransport?, onExit: () -> Unit) {
    val colors = CmpTheme.colors
    CmpIcon(
        painter = painterResource(Res.drawable.device_link_lost),
        contentDescription = null,
        modifier = Modifier.size(width = LinkLostWidth, height = LinkLostHeight),
        tint = colors.brandPrimaryDefault,
    )
    Headline(
        title = stringResource(Strings.string.deviceinteraction_disconnected_title),
        subtitle = stringResource(
            when (transport) {
                ConnectionTransport.USB -> Strings.string.deviceinteraction_disconnected_msg_usb
                ConnectionTransport.SERIAL -> Strings.string.deviceinteraction_disconnected_msg_serial
                ConnectionTransport.BLE -> Strings.string.deviceinteraction_disconnected_msg_ble
                null -> Strings.string.deviceinteraction_disconnected_subtitle
            },
        ),
    )
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconM))
        CmpText(
            text = stringResource(Strings.string.deviceinteraction_common_msg_waiting),
            style = CmpTextStyle.Headline3,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
    CmpButton(
        text = stringResource(Strings.string.deviceinteraction_common_btn_disconnect),
        onClick = onExit,
        type = CmpButtonType.Secondary,
        icon = CmpIcons.LinkOff,
        fullWidth = true,
        modifier = Modifier.testTag(DEVICE_STATUS_DISCONNECT_TAG),
    )
}

@Composable
private fun StatusHero(icon: CmpIcons) {
    val colors = CmpTheme.colors
    CmpSurface(
        modifier = Modifier.size(HeroSize),
        shape = CircleShape,
        color = colors.brandPrimaryHighlight,
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(HeroIconSize),
                tint = colors.brandPrimaryDefault,
            )
        }
    }
}

@Composable
private fun Headline(title: String, subtitle: String) {
    CmpColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(text = title, style = CmpTextStyle.Headline1, textAlign = TextAlign.Center)
        CmpText(
            text = subtitle,
            style = CmpTextStyle.Paragraph1,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            textAlign = TextAlign.Center,
        )
    }
}
