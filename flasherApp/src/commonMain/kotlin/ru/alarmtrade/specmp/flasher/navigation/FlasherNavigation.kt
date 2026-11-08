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
import ru.alarmtrade.specmp.core.ui.navigation.popSafe
import ru.alarmtrade.specmp.core.ui.navigation.replaceCurrent
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.feature.authstate.presentation.navigation.AuthStateNavGraph
import ru.alarmtrade.specmp.feature.authstate.presentation.screen.SessionCheckErrorScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareBootGraph
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.rememberFirmwareFilePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file.FirmwareFileScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial.SerialFirmwareScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FirmwareSources
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host.DeviceInteractionHost
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation.DeviceSelectionNavGraph
import ru.alarmtrade.specmp.feature.login.presentation.navigation.LoginNavGraph

/** Поиск устройства — корень авторизованной зоны. */
@Serializable
@SerialName("flasher.ScanRoute")
private data object ScanRoute : NavKey

/** Подключённый блок (BLE / USB / COM): хост устройства — связь, обрыв, рабочий режим и загрузчик. */
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

/**
 * Навигация приложения прошивки: логин (первая версия; финальная — без авторизации) → поиск → блок.
 * Экранов два: поиск и экран блока с одной кнопкой «Обновить из файла».
 */
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

/**
 * Экран блока. BLE / USB: «Обновить из файла» переводит блок в загрузчик, прошивку ведёт граф загрузчика.
 * COM: загрузчик SDK входит сам внутри обновления — один экран прошивки по COM в обоих слотах хоста (VM у них общая).
 * Транспорт читается один раз: сессия уже создана поиском, а на выходе она закрывается (транспорт — `null`).
 */
@Composable
private fun DeviceEntry(onExit: () -> Unit) {
    val session = koinInject<DeviceSession>()
    val serial = remember(session) { session.transport.value == ConnectionTransport.SERIAL }
    val filePicker = rememberFirmwareFilePicker()
    DeviceInteractionHost(
        onExit = onExit,
        bootContent = { exit ->
            if (serial) {
                SerialFirmwareScreen(onExit = exit, filePicker = filePicker)
            } else {
                FirmwareBootGraph(
                    onExit = exit,
                    onOpenSupportChat = {},
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
