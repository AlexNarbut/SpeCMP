package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.WriteFailureReason
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.displayTitle
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title

// Без VM и корутин: VM меняет [UpdateCore], поля экрана пересчитываются в том же `reduce` ([toState]).

@Immutable
class BootBlock(
    val kind: UpdateProfile,
    val target: BootModeTarget,
    val deviceName: String,
    val deviceTitle: String,
    val currentVersion: String,
    val modules: List<FirmwareModule>,
    val canExit: Boolean,
    /** Связь по BLE: передача прошивки переживает обрыв (SDK переподключается и продолжает). */
    val overBle: Boolean,
    val can: FirmwareCan? = null,
    val factoryCan: Boolean = false,
)

@Immutable
data class UpdateCore(
    val stage: UpdatePhase = UpdatePhase.Loading,
    val profile: BootBlock? = null,
    val selectedModuleId: Long? = null,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val candidate: FirmwareVersion? = null,
    val localFile: FirmwareFilePickResult.Picked? = null,
    val fileError: FileError? = null,
    val sourceError: FirmwareError? = null,
    val exitDialog: Boolean = false,
    val exitError: Boolean = false,
    val acknowledged: Boolean = false,
    val linkDropped: Boolean = false,
    val running: Boolean = false,
    val session: DeviceSessionState = DeviceSessionState.Disconnected,
    val reboot: DeviceReboot? = null,
) {
    val isAlarmMain: Boolean
        get() = profile?.modules?.firstOrNull { it.id == selectedModuleId }?.kind == FirmwareModuleKind.AlarmMain

    /** Передача по BLE, которую SDK продолжит после переподключения. */
    val resumable: Boolean
        get() = running && stage is UpdatePhase.Transfer && profile?.overBle == true
}

internal sealed interface UpdateSource {
    data class Server(val version: FirmwareVersion) : UpdateSource

    data class File(val path: String) : UpdateSource
}

internal data class BootSource(
    val kind: UpdateProfile,
    val target: BootModeTarget,
    val deviceName: String,
    val title: String,
    val current: String,
    val can: FirmwareCan? = null,
    val factoryCan: Boolean = false,
)

internal fun DeviceSessionState.bootSource(): BootSource? = when (this) {
    is DeviceSessionState.FirmwareBoot -> BootSource(
        UpdateProfile.Main,
        BootModeTarget.MainProgram,
        device.systemName,
        device.title,
        device.displayFirmwareVersion,
        // CAN — как блок сообщает его в загрузчике; не сообщил — ищем по всем версиям.
        can = device.can.toFirmwareCan(),
        factoryCan = device.can?.isFactory == true,
    )

    is DeviceSessionState.BluetoothBoot -> BootSource(
        UpdateProfile.Bluetooth,
        BootModeTarget.BluetoothModule,
        manufacturer.firmwareName,
        manufacturer.deviceName,
        manufacturer.mainFirmwareVersion,
    )

    else -> null
}

/** Выбор модуля сохраняется, если он среди подсистем. */
internal fun UpdateCore.withBlock(
    source: BootSource,
    modules: List<FirmwareModule>,
    transport: ConnectionTransport,
): UpdateCore = copy(
    profile = BootBlock(
        source.kind,
        source.target,
        source.deviceName,
        source.title,
        source.current,
        modules,
        canExit = transport == ConnectionTransport.BLE,
        overBle = transport == ConnectionTransport.BLE,
        can = source.can,
        factoryCan = source.factoryCan,
    ),
    selectedModuleId = selectedModuleId?.takeIf { id -> modules.any { it.id == id } } ?: modules.firstOrNull()?.id,
    stage = if (stage == UpdatePhase.Loading) {
        if (modules.isEmpty()) UpdatePhase.DataError else UpdatePhase.Source
    } else {
        stage
    },
)

internal fun FirmwareBootRequest?.initialUpdateState(
    session: DeviceSessionState,
    reboot: DeviceReboot?,
): FirmwareUpdateState = UpdateCore(
    selectedModuleId = this?.moduleId,
    channel = this?.channel ?: FirmwareChannel.RELEASE,
    session = session,
    reboot = reboot,
).toState()

internal fun UpdateCore.toState(): FirmwareUpdateState {
    val core = this
    val profile = core.profile
    val modules = profile?.modules.orEmpty()
    val selected = modules.indexOfFirst { it.id == core.selectedModuleId }.coerceAtLeast(0)
    val current = profile?.currentVersion
    val title = when (profile?.kind) {
        UpdateProfile.Bluetooth -> modules.getOrNull(selected)?.displayTitle
            ?: profile.deviceTitle

        UpdateProfile.Main, null -> profile?.deviceTitle.orEmpty()
    }
    return FirmwareUpdateState(
        phase = phaseOf(core),
        profile = profile?.kind ?: UpdateProfile.Main,
        deviceTitle = title,
        moduleTitles = modules.map { it.displayTitle },
        selectedModule = selected,
        currentVersion = current,
        channel = core.channel,
        confirm = confirmOf(core, current),
        sourceError = core.sourceError,
        exitDialog = core.exitDialog,
        inBoot = core.session.isBoot,
        canExitBoot = profile?.canExit == true,
        exitError = core.exitError,
        fileError = core.fileError,
        blockCan = blockCanOf(profile),
        core = core,
    )
}

private fun blockCanOf(profile: BootBlock?): BlockCanUi? {
    if (profile?.kind != UpdateProfile.Main) return null
    val can = profile.can
    return when {
        can != null -> BlockCanUi(can.code, can.profileName)
        profile.factoryCan -> BlockCanUi(code = null, profile = null)
        else -> null
    }
}

private fun confirmOf(core: UpdateCore, current: String?): ConfirmUi? {
    val blockCan = blockCanOf(core.profile)
    core.localFile?.let { file ->
        return ConfirmUi(current, file.name, description = "", older = false, localFile = true)
    }
    return core.candidate?.let { version ->
        ConfirmUi(
            currentVersion = current,
            newVersion = version.version,
            description = version.description,
            older = current != null && version.version.relationTo(current) == VersionRelation.Older,
            can = version.canCode?.takeIf { blockCan != null }?.let { code ->
                ConfirmCanUi(blockCode = blockCan?.code, newCode = code, newProfile = version.canVersion)
            },
        )
    }
}

internal fun phaseOf(core: UpdateCore): UpdatePhase {
    val session = core.session
    val reboot = core.reboot
    val inBoot = session.isBoot
    val linkDown = session.isLinkDown
    return when {
        reboot == DeviceReboot.ToBoot && !inBoot -> UpdatePhase.Entering

        reboot == DeviceReboot.ToMain && core.acknowledged -> UpdatePhase.Finishing

        // Блок перезагрузился после применения и вернулся в рабочий режим, а результата SDK не было — успех.
        core.stage == UpdatePhase.Apply && session is DeviceSessionState.Ready -> UpdatePhase.Success

        linkDown && reboot == null -> linkDownPhase(core)

        else -> core.stage
    }
}

// Передачу Bluetooth-модуля SDK продолжит после переподключения — это не потеря связи.
private fun linkDownPhase(core: UpdateCore): UpdatePhase {
    val stage = core.stage
    return if (core.resumable && stage is UpdatePhase.Transfer) {
        stage.copy(reconnecting = true)
    } else {
        UpdatePhase.Lost(updating = stage.isUpdating())
    }
}

internal fun UpdatePhase.isUpdating(): Boolean = this == UpdatePhase.Prepare ||
    this is UpdatePhase.Transfer ||
    this == UpdatePhase.Verify ||
    this == UpdatePhase.Apply

internal fun FirmwareFailure.toKind(): FailureKind = when (this) {
    is FirmwareFailure.Write -> when (reason) {
        WriteFailureReason.Crc -> FailureKind.Crc
        WriteFailureReason.Timeout -> FailureKind.Timeout
        WriteFailureReason.LostPacket -> FailureKind.LostPacket
        WriteFailureReason.WrongPacketLength -> FailureKind.WrongPacketLength
        WriteFailureReason.DeviceNotReady -> FailureKind.DeviceNotReady
        WriteFailureReason.ApplyingError -> FailureKind.ApplyingError
        WriteFailureReason.Unknown -> FailureKind.Unknown
    }

    FirmwareFailure.Preparation -> FailureKind.Preparation

    FirmwareFailure.Unknown -> FailureKind.Unknown
}
