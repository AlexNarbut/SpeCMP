package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveEntry
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareAutoBoot
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.FirmwareCheckScreen
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole
import ru.alarmtrade.specmp.feature.mountassistant.domain.service.AssistantJournal
import ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation.AssistantNavigationTarget
import ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation.MountAssistantEntry

/** Ассистент ждёт возврата с экрана, ради которого блок ушёл в загрузчик: граф монтажа начнётся с него. */
@Composable
internal fun rememberAssistantResume(device: AlarmDevice): String? {
    val journal = koinInject<AssistantJournal>()
    return remember { device.title.takeIf { journal.state.value.pendingHandoff != null } }
}

private val ArchiveTargetSaver = listSaver<AssistantNavigationTarget.FirmwareArchive?, Any?>(
    save = { target -> target?.let { listOf(it.moduleId, it.role.name, it.installed) }.orEmpty() },
    restore = { saved ->
        if (saved.isEmpty()) {
            null
        } else {
            AssistantNavigationTarget.FirmwareArchive(
                moduleId = saved[0] as Long,
                role = FirmwareModuleRole.valueOf(saved[1] as String),
                installed = saved[2] as String?,
            )
        }
    },
)

@Composable
internal fun AdvancedMountAssistant(
    device: AlarmDevice,
    firmwareBoot: PendingFirmwareBoot,
    onBack: () -> Unit,
    onOpenSection: (MountSection) -> Unit,
    onOpenSettings: (focusSettingId: Int?, openLoad: Boolean) -> Unit,
    onOpenNewTechRequest: () -> Unit,
) {
    val onFlash = { boot: FirmwareAutoBoot ->
        firmwareBoot.put(boot)
        onOpenSection(MountSection.FIRMWARE_UPDATE)
    }
    var archive by rememberSaveable(stateSaver = ArchiveTargetSaver) { mutableStateOf(null) }
    val module = archive
    if (module != null) {
        // Архив поверх ассистента: «назад» возвращает в ленту; выбранная версия ведёт в загрузчик, после него
        // ассистент открывается сам и сравнивает блок «до → после».
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = { archive = null },
        )
        FirmwareArchiveEntry(
            request = device.archiveRequest(module),
            onPick = { version -> onFlash(FirmwareAutoBoot(module.moduleId, version)) },
            onBack = { archive = null },
            vehiclePicker = { deviceName, onPick, onPickerBack ->
                ArchiveVehiclePicker(deviceName, onPick, onPickerBack)
            },
        )
        return
    }
    MountAssistantEntry(
        onBack = onBack,
        onNavigate = { target ->
            val section = target.section()
            when {
                section != null -> onOpenSection(section)

                target is AssistantNavigationTarget.FirmwareFlash ->
                    onFlash(FirmwareAutoBoot(target.moduleId, version = null))

                target is AssistantNavigationTarget.FirmwareArchive -> archive = target

                target is AssistantNavigationTarget.SettingsEditor ->
                    onOpenSettings(target.focusSettingId, target.openLoad)

                target == AssistantNavigationTarget.TechRequestNew -> onOpenNewTechRequest()
            }
        },
    )
}

/** Архив модуля подключённого блока — как из загрузчика: версию можно выбрать и прошить. */
private fun AlarmDevice.archiveRequest(module: AssistantNavigationTarget.FirmwareArchive): FirmwareArchiveRequest {
    val main = module.role == FirmwareModuleRole.MAIN
    val profile = if (module.role == FirmwareModuleRole.BLUETOOTH) UpdateProfile.Bluetooth else UpdateProfile.Main
    val deviceCan = can?.takeUnless { it.isFactory }
    return FirmwareArchiveRequest(
        moduleId = module.moduleId,
        channel = FirmwareChannel.RELEASE,
        installedVersion = module.installed,
        deviceTitle = title,
        profile = profile,
        deviceName = systemName.takeIf { profile == UpdateProfile.Main },
        deviceCanCode = deviceCan?.code?.takeIf { main && it.isNotBlank() },
        deviceCanVersion = deviceCan?.version.takeIf { main },
        deviceCanProfile = deviceCan?.firmwareVersion.takeIf { main },
        deviceCanFactory = main && can?.isFactory == true,
        canFilterAllowed = main,
    )
}

/** Экран проверки прошивок расширенного монтажа: открытый ассистентом — сразу предлагает загрузчик. */
@Composable
internal fun AssistantFirmwareCheck(firmwareBoot: PendingFirmwareBoot, onBack: () -> Unit) {
    FirmwareCheckScreen(onBack = onBack, autoBoot = remember { firmwareBoot.take() })
}

/** Запрос «сразу в загрузчик» от ассистента экрану проверки прошивок: забирается один раз, при его открытии. */
internal class PendingFirmwareBoot {
    private var boot: FirmwareAutoBoot? = null

    fun put(boot: FirmwareAutoBoot) {
        this.boot = boot
    }

    fun take(): FirmwareAutoBoot? = boot.also { boot = null }
}

/** Цели ассистента, которые просто открывают раздел расширенного монтажа. */
private fun AssistantNavigationTarget.section(): MountSection? = when (this) {
    AssistantNavigationTarget.FirmwareUpdate -> MountSection.FIRMWARE_UPDATE

    AssistantNavigationTarget.BypassLearning -> MountSection.BYPASS_LEARNING

    AssistantNavigationTarget.Clone -> MountSection.CLONE

    AssistantNavigationTarget.Configurator -> MountSection.CONFIGURATOR

    AssistantNavigationTarget.CheckState -> MountSection.CHECK_STATE

    AssistantNavigationTarget.Vehicles -> MountSection.VEHICLES

    AssistantNavigationTarget.SupportChat -> MountSection.SUPPORT_CHAT

    AssistantNavigationTarget.SystemDevices -> MountSection.SYSTEM_DEVICES

    AssistantNavigationTarget.Pairing -> MountSection.DYNAMIC_DEVICES

    AssistantNavigationTarget.TechRequests -> MountSection.TECH_REQUESTS

    is AssistantNavigationTarget.FirmwareFlash,
    is AssistantNavigationTarget.FirmwareArchive,
    is AssistantNavigationTarget.SettingsEditor,
    AssistantNavigationTarget.TechRequestNew,
    -> null
}
