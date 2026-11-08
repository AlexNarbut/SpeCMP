package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import kotlinx.coroutines.flow.update
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
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title

/*
 * Машина состояний экрана обновления — данные процесса ([Core]) и чистые функции «состояние процесса + сессия блока →
 * фаза и state экрана». Без VM и корутин: тестируются напрямую. VM меняет [Core] интентами и событиями сессии/SDK.
 */

internal class Profile(
    val kind: UpdateProfile,
    /** Что обновляем и чем выходим из загрузчика — основная программа или Bluetooth-модуль. */
    val target: BootModeTarget,
    /** Модель блока (основная программа) или прошивка Bluetooth-модуля — для базы устройств. */
    val deviceName: String,
    val deviceTitle: String,
    val currentVersion: String,
    val modules: List<FirmwareModule>,
    /** Выход из загрузчика командой доступен (BLE). */
    val canExit: Boolean,
    /** Связь по BLE: передача прошивки переживает обрыв (SDK переподключается и продолжает). */
    val overBle: Boolean,
    /** CAN блока (единая CAN-прошивка, только основная программа): фильтр «Скачать» и архива. */
    val can: FirmwareCan? = null,
    /** Заводские CAN-настройки: кода нет — в архиве подсказка выбрать автомобиль. */
    val factoryCan: Boolean = false,
)

internal data class Core(
    val stage: UpdatePhase = UpdatePhase.Loading,
    val profile: Profile? = null,
    val selectedModuleId: Long? = null,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val candidate: FirmwareVersion? = null,
    /** Прошивка из файла пользователя (вместо [candidate]). */
    val localFile: FirmwareFilePickResult.Picked? = null,
    val fileError: FileError? = null,
    val sourceError: FirmwareError? = null,
    val exitDialog: Boolean = false,
    val exitError: Boolean = false,
    /** Пользователь подтвердил результат («OK»). */
    val acknowledged: Boolean = false,
    /** Связь пропадала, пока шёл процесс обновления. */
    val linkDropped: Boolean = false,
    /** Поток обновления SDK ещё идёт (не закончился и не сдался). */
    val running: Boolean = false,
) {
    /** Выбран модуль «основная программа сигнализации»: только у него прошивки под CAN-код. */
    val isAlarmMain: Boolean
        get() = profile?.modules?.firstOrNull { it.id == selectedModuleId }?.kind == FirmwareModuleKind.AlarmMain

    /** Передача по BLE, которую SDK продолжит после переподключения. */
    val resumable: Boolean
        get() = running && stage is UpdatePhase.Transfer && profile?.overBle == true
}

/** Откуда берём файл прошивки: версия с сервера или файл пользователя. */
internal sealed interface UpdateSource {
    data class Server(val version: FirmwareVersion) : UpdateSource

    data class File(val path: String) : UpdateSource
}

internal fun toState(core: Core, session: DeviceSessionState, reboot: DeviceReboot?): FirmwareUpdateState {
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
        phase = phaseOf(core, session, reboot),
        profile = profile?.kind ?: UpdateProfile.Main,
        deviceTitle = title,
        moduleTitles = modules.map { it.displayTitle },
        selectedModule = selected,
        currentVersion = current,
        channel = core.channel,
        confirm = confirmOf(core, current),
        sourceError = core.sourceError,
        exitDialog = core.exitDialog,
        inBoot = session.isBoot,
        canExitBoot = profile?.canExit == true,
        exitError = core.exitError,
        fileError = core.fileError,
        blockCan = blockCanOf(profile),
    )
}

private fun blockCanOf(profile: Profile?): BlockCanUi? {
    if (profile?.kind != UpdateProfile.Main) return null
    val can = profile.can
    return when {
        can != null -> BlockCanUi(can.code, can.profileName)
        profile.factoryCan -> BlockCanUi(code = null, profile = null)
        else -> null
    }
}

private fun confirmOf(core: Core, current: String?): ConfirmUi? {
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

internal fun phaseOf(core: Core, session: DeviceSessionState, reboot: DeviceReboot?): UpdatePhase {
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
private fun linkDownPhase(core: Core): UpdatePhase {
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
