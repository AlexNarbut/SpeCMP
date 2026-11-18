package ru.alarmtrade.specmp.di

import com.russhwolf.settings.Settings
import kotlin.reflect.KClass
import kotlin.test.Test
import org.koin.dsl.module
import org.koin.test.verify.ParameterTypeInjection
import org.koin.test.verify.definition
import org.koin.test.verify.verify
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.core.files.TextFilePicker
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateInstaller
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveViewModel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareAutoBoot
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckViewModel
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan.ScanDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen.CheckStateViewModel
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenViewModel
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatNotifier
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.notifications.SupportChatNotificationsViewModel
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest

class AppKoinModulesTest {
    @Test
    fun `every constructor dependency of the app graph is defined`() {
        module { includes(appKoinModules(appVersionName = "1.0", appVersionCode = 1)) }.verify(
            // Settings, Set транспортов, Pair сервисов — значения в лямбдах; установщик обновления — платформенный модуль.
            extraTypes = listOf(Settings::class, Set::class, Pair::class, AppUpdateInstaller::class),
            // Параметры ViewModel из parametersOf экрана.
            injections = listOf(
                definition<SupportChatNotificationsViewModel>(SupportChatNotifier::class),
                definition<SupportChatScreenViewModel>(
                    SupportChatFileSaver::class,
                    SupportChatAttachmentFiles::class,
                    SupportChatFileOpener::class,
                ),
                definition<ScanDeviceViewModel>(Boolean::class),
                definition<CloneViewModel>(Boolean::class),
                definition<CheckStateViewModel>(Boolean::class),
                definition<FirmwareArchiveViewModel>(FirmwareArchiveRequest::class),
                definition<FirmwareCheckViewModel>(FirmwareAutoBoot::class),
                definition<MainScreenViewModel>(ImagePicker::class),
                internalDefinition(
                    "feature.vehicle.ui.presentation.screen.select.VehicleSelectViewModel",
                    VehicleSelectRequest::class,
                ),
                internalDefinition(
                    "feature.configurator.ui.presentation.screen.models.ModelsViewModel",
                    internalType("feature.configurator.ui.presentation.navigation.SourceKey\$File"),
                ),
                internalDefinition(
                    "feature.appupdate.presentation.screen.update.AppUpdateViewModel",
                    internalType("feature.appupdate.presentation.screen.update.AppUpdateArgs"),
                ),
                internalDefinition(
                    "feature.productinfo.presentation.screen.product.ProductInfoViewModel",
                    internalType("feature.productinfo.presentation.screen.product.ProductInfoArgs"),
                ),
                internalDefinition(
                    "feature.appearance.presentation.screen.appearance.AppearanceViewModel",
                    TextFilePicker::class,
                    TextFileSaver::class,
                    Boolean::class,
                ),
                internalDefinition(
                    "feature.device.signallog.ui.presentation.screen.SignalLogViewModel",
                    TextFileSaver::class,
                ),
                internalDefinition(
                    "feature.device.settings.ui.presentation.screen.editor.DeviceSettingsViewModel",
                    SettingsSource::class,
                    internalType("feature.device.settings.ui.presentation.screen.editor.SettingsEditorOptions"),
                ),
            ),
        )
    }
}

// internal-классы фич недоступны из composeApp по имени — берутся рефлексией.
private fun internalType(name: String): KClass<*> = Class.forName("ru.alarmtrade.specmp.$name").kotlin

private fun internalDefinition(target: String, vararg parameters: KClass<*>) =
    ParameterTypeInjection(internalType(target), parameters.toList())
