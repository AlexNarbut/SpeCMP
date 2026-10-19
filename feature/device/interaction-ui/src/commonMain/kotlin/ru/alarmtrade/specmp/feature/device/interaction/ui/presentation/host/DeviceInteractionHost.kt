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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import specmp.feature.device.interaction_ui.generated.resources.Res
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_a11y_back
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_bluetooth_boot_subtitle
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_bluetooth_boot_title
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_btn_disconnect
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_btn_exit
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_connecting_title
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_disconnected_ble
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_disconnected_subtitle
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_disconnected_title
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_disconnected_usb
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_firmware_boot_subtitle
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_firmware_boot_title
import specmp.feature.device.interaction_ui.generated.resources.device_interaction_waiting
import specmp.feature.device.interaction_ui.generated.resources.device_link_lost

/**
 * Обёртка flow работы с подключённым устройством (аналог `DeviceInteractionComponent` старого приложения).
 *
 * - Блок на связи в рабочем режиме — [content] (граф дочернего flow).
 * - Связь потеряна или ещё устанавливается — экран состояния **поверх** [content]: дочерний flow
 *   остаётся в композиции (его back stack и VM живы), после переподключения пользователь там же.
 * - «Назад» на экране состояния — выход из всего flow (сессия закрывается), а не из подэкрана.
 * - Блок в загрузчике (BLE-модуля или основной программы) — экран режима загрузчика вместо [content];
 *   режим определяется на каждом подключении, после возврата в рабочий режим [content] стартует заново.
 *
 * @param content дочерний flow; получает блок и выход из flow (закрывает сессию).
 */
@Composable
fun DeviceInteractionHost(onExit: () -> Unit, content: @Composable (device: AlarmDevice, onExit: () -> Unit) -> Unit) {
    val viewModel = koinViewModel<DeviceInteractionViewModel>()
    val state by viewModel.state.collectAsState()

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            DeviceInteractionEffect.Exit -> onExit()
        }
    }

    val device = state.device
    CmpBox(modifier = Modifier.fillMaxSize()) {
        if (device != null) {
            content(device, viewModel::onExit)
        }
        val session = state.session
        val status = when (session) {
            is DeviceSessionState.Ready -> null
            DeviceSessionState.Connecting -> DeviceStatus.Connecting
            DeviceSessionState.Disconnected -> DeviceStatus.Disconnected
            is DeviceSessionState.BluetoothBoot -> DeviceStatus.BluetoothBoot
            is DeviceSessionState.FirmwareBoot -> DeviceStatus.FirmwareBoot
        }
        val deviceName = when (session) {
            is DeviceSessionState.BluetoothBoot -> session.manufacturer.deviceName
            is DeviceSessionState.FirmwareBoot -> session.device.systemName
            else -> device?.systemName
        }
        status?.let {
            NavigationBackHandler(
                state = rememberNavigationEventState(NavigationEventInfo.None),
                onBackCompleted = viewModel::onExit
            )
            DeviceStatusContent(
                status = it,
                transport = state.transport,
                deviceName = deviceName,
                onExit = viewModel::onExit
            )
        }
    }
}

/** Состояние связи, при котором поверх дочернего flow показывается экран обёртки. */
internal enum class DeviceStatus { Connecting, Disconnected, BluetoothBoot, FirmwareBoot }

/**
 * Stateless-экран состояния поверх дочернего flow («Назад» перехватывает [DeviceInteractionHost]).
 * Потеря связи — по макету: иллюстрация, текст по транспорту, ожидание возврата блока, «Отключиться».
 */
@Composable
internal fun DeviceStatusContent(
    status: DeviceStatus,
    transport: ConnectionTransport?,
    deviceName: String?,
    onExit: () -> Unit
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
                            contentDescription = stringResource(Res.string.device_interaction_a11y_back)
                        )
                    }
                )
            }
        ) { padding ->
            CmpBox(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                CmpColumn(
                    modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (status) {
                        DeviceStatus.Disconnected -> DisconnectedBody(transport, onExit)

                        DeviceStatus.Connecting -> {
                            StatusHero(CmpIcons.Link, error = false)
                            CmpText(
                                text = stringResource(Res.string.device_interaction_connecting_title),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            CmpCircularProgressIndicator()
                            CmpButton(
                                text = stringResource(Res.string.device_interaction_btn_exit),
                                onClick = onExit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        DeviceStatus.BluetoothBoot -> BootBody(
                            icon = CmpIcons.Bluetooth,
                            title = stringResource(Res.string.device_interaction_bluetooth_boot_title),
                            subtitle = stringResource(Res.string.device_interaction_bluetooth_boot_subtitle),
                            onExit = onExit
                        )

                        DeviceStatus.FirmwareBoot -> BootBody(
                            icon = CmpIcons.Memory,
                            title = stringResource(Res.string.device_interaction_firmware_boot_title),
                            subtitle = stringResource(Res.string.device_interaction_firmware_boot_subtitle),
                            onExit = onExit
                        )
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
        tint = colors.primary
    )
    Headline(
        title = stringResource(Res.string.device_interaction_disconnected_title),
        subtitle = stringResource(
            when (transport) {
                ConnectionTransport.USB -> Res.string.device_interaction_disconnected_usb
                ConnectionTransport.BLE -> Res.string.device_interaction_disconnected_ble
                null -> Res.string.device_interaction_disconnected_subtitle
            }
        )
    )
    CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        CmpCircularProgressIndicator(modifier = Modifier.size(18.dp))
        CmpText(
            text = stringResource(Res.string.device_interaction_waiting),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant
        )
    }
    CmpOutlinedButton(
        text = stringResource(Res.string.device_interaction_btn_disconnect),
        onClick = onExit,
        modifier = Modifier.fillMaxWidth()
    )
}

/** Режим загрузчика (сам загрузчик пока не реализован): что с блоком и выход. */
@Composable
private fun BootBody(icon: CmpIcons, title: String, subtitle: String, onExit: () -> Unit) {
    StatusHero(icon, error = true)
    Headline(title = title, subtitle = subtitle)
    CmpButton(
        text = stringResource(Res.string.device_interaction_btn_exit),
        onClick = onExit,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StatusHero(icon: CmpIcons, error: Boolean) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.size(96.dp),
        shape = CircleShape,
        color = if (error) colors.errorContainer else colors.primaryContainer
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (error) colors.onErrorContainer else colors.onPrimaryContainer
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
