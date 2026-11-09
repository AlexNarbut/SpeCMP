package ru.alarmtrade.specmp.flasher.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.feature.authstate.presentation.navigation.AuthStateNavGraph
import ru.alarmtrade.specmp.feature.authstate.presentation.screen.SessionCheckErrorScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareBootGraph
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.rememberFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file.FirmwareFileScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareSources
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionNavGraph
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph

@Serializable
@SerialName("flasher.ScanRoute")
private data object ScanRoute : NavKey

@Serializable
@SerialName("flasher.DeviceRoute")
private data object DeviceRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScanRoute::class, ScanRoute.serializer())
            subclass(DeviceRoute::class, DeviceRoute.serializer())
        }
    }
}

@Composable
fun FlasherNavigation() {
    AuthStateNavGraph(
        waitingState = { StartLoading() },
        noAuthState = { LoginNavGraph() },
        authedState = { AuthedNavGraph() },
        errorState = { onRetry -> SessionCheckErrorScreen(onRetry = onRetry) },
    )
}

@Composable
private fun AuthedNavGraph() {
    val backStack = rememberNavBackStack(navConfig, ScanRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.popSafe() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<ScanRoute> { _ ->
                // Поиск — корень: без «Назад»; COM-порты ищутся только здесь. Экран блока заменяет поиск, а выход из
                // блока — новый поиск: VM поиска не переживает сессию (иначе вернулась бы на шаге «Подключение»).
                DeviceSelectionNavGraph(
                    onSessionCreate = { backStack.replaceCurrent(DeviceRoute) },
                    onClose = null,
                    serialEnabled = true,
                )
            }
            entry<DeviceRoute> { _ -> DeviceEntry(onExit = { backStack.replaceCurrent(ScanRoute) }) }
        },
    )
}

/** COM: загрузчик SDK входит сам внутри обновления — один экран прошивки в обоих слотах хоста (VM у них общая). */
@Composable
private fun DeviceEntry(onExit: () -> Unit) {
    val session = koinInject<DeviceSession>()
    val serial = remember(session) { session.transport.value == ConnectionTransport.SERIAL }
    val filePicker = rememberFirmwareFilePicker(
        appScope = koinInject(qualifier = appScopeQualifier),
        ioDispatcher = koinInject<AppDispatchers>().io,
    )
    val archivePicker = koinInject<FirmwareArchivePicker>()
    DeviceInteractionHost(
        onExit = onExit,
        bootContent = { exit ->
            if (serial) {
                SerialFirmwareScreen(onExit = exit, filePicker = filePicker)
            } else {
                FirmwareBootGraph(
                    onExit = exit,
                    onOpenSupportChat = {},
                    archivePicker = archivePicker,
                    filePicker = filePicker,
                    sources = FirmwareSources.FileOnly,
                )
            }
        },
    ) { _, exit ->
        if (serial) {
            SerialFirmwareScreen(onExit = exit, filePicker = filePicker)
        } else {
            FirmwareFileScreen(onBack = exit)
        }
    }
}

@Composable
private fun StartLoading() {
    CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CmpCircularProgressIndicator(color = cmpThemeColors().primary)
    }
}
