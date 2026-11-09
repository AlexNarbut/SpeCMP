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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import specmp.feature.device.interaction_ui.generated.resources.Res
import specmp.feature.device.interaction_ui.generated.resources.device_link_lost

/**
 * Обёртка flow работы с подключённым устройством (аналог `DeviceInteractionComponent` старого приложения).
 *
 * - Блок на связи в рабочем режиме — [content] (граф дочернего flow).
 * - Связь потеряна или ещё устанавливается — экран состояния **поверх** [content]: дочерний flow
 *   остаётся в композиции (его back stack и VM живы), после переподключения пользователь там же.
 * - «Назад» на экране состояния — выход из всего flow (сессия закрывается), а не из подэкрана.
 * - Блок в загрузчике (BLE-модуля или основной программы), либо вход в загрузчик / возврат после прошивки
 *   ожидается ([DeviceInteractionState.showBootContent]) — [bootContent] вместо оверлеев связи: он сам рисует и
 *   обрыв связи в процессе обновления. Рабочий [content] уходит из композиции, когда блок вернулся в загрузчике,
 *   и стартует заново после возврата в рабочий режим. Режим определяется на каждом подключении.
 *
 * @param bootContent экран режима загрузчика (обновление прошивки); получает выход из всего flow (закрывает сессию).
 * @param content дочерний flow; получает блок и выход из flow (закрывает сессию).
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
    CmpBox(modifier = Modifier.fillMaxSize()) {
        if (device != null) {
            content(device, viewModel::onExit)
        }
        if (state.showBootContent) {
            bootContent(viewModel::onExit)
        } else {
            // Ожидаемая перезагрузка в рабочем режиме: ожидание рисует сам flow (мастер быстрого монтажа);
            // не дождались за тайм-аут — ожидание снимается, и обрыв снова показывается как потеря связи.
            val status = if (state.reboot == DeviceReboot.Restart) {
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

/** testTag кнопки «Выйти» на экране «Подключение к блоку…». */
internal const val DEVICE_STATUS_EXIT_TAG = "device_status_exit"

/** testTag кнопки «Отключиться» на экране потери связи. */
internal const val DEVICE_STATUS_DISCONNECT_TAG = "device_status_disconnect"

/** Состояние связи, при котором поверх дочернего flow показывается экран обёртки. */
internal enum class DeviceStatus { Connecting, Disconnected }

/**
 * Stateless-экран состояния поверх дочернего flow («Назад» перехватывает [DeviceInteractionHost]).
 * Потеря связи — по макету: иллюстрация, текст по транспорту, ожидание возврата блока, «Отключиться».
 */
@Composable
internal fun DeviceStatusContent(
    status: DeviceStatus,
    transport: ConnectionTransport?,
    deviceName: String?,
    onExit: () -> Unit,
) {
    val colors = cmpThemeColors()
    // Surface поверх content перехватывает касания.
    CmpSurface(modifier = Modifier.fillMaxSize(), color = colors.background) {
        CmpScaffold(
            topBar = {
                CmpTopAppBar(
                    title = { CmpText(text = if (status == DeviceStatus.Disconnected) "" else deviceName.orEmpty()) },
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
                    modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    when (status) {
                        DeviceStatus.Disconnected -> DisconnectedBody(transport, onExit)

                        DeviceStatus.Connecting -> {
                            StatusHero(CmpIcons.Link)
                            CmpText(
                                text = stringResource(Strings.string.deviceinteraction_connecting_title),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                            CmpCircularProgressIndicator()
                            CmpButton(
                                text = stringResource(Strings.string.deviceinteraction_common_btn_exit),
                                onClick = onExit,
                                modifier = Modifier.fillMaxWidth().testTag(DEVICE_STATUS_EXIT_TAG),
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
    val colors = cmpThemeColors()
    CmpIcon(
        painter = painterResource(Res.drawable.device_link_lost),
        contentDescription = null,
        modifier = Modifier.size(width = 176.dp, height = 121.dp),
        tint = colors.primary,
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
    CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        CmpCircularProgressIndicator(modifier = Modifier.size(18.dp))
        CmpText(
            text = stringResource(Strings.string.deviceinteraction_common_msg_waiting),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
        )
    }
    CmpOutlinedButton(
        text = stringResource(Strings.string.deviceinteraction_common_btn_disconnect),
        onClick = onExit,
        modifier = Modifier.fillMaxWidth().testTag(DEVICE_STATUS_DISCONNECT_TAG),
    )
}

@Composable
private fun StatusHero(icon: CmpIcons) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.size(96.dp),
        shape = CircleShape,
        color = colors.primaryContainer,
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colors.onPrimaryContainer,
            )
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
