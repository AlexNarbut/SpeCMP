package ru.alarmtrade.specmp.feature.mountpassport.data.mapper

import ru.alarmtrade.specialist.domain.mountPassport.MountPassportFastMountResult
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadCommand
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadPhase
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadSource
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadState
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportReadStep
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStepStatus
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportStepSummary
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportUnavailableReason
import ru.alarmtrade.specialist.domain.mountPassport.MountPassportUnitLookup
import ru.alarmtrade.specialist.domain.mountPassport.PassportBluetoothModule as SdkBluetoothModule
import ru.alarmtrade.specialist.domain.mountPassport.PassportCan as SdkCan
import ru.alarmtrade.specialist.domain.mountPassport.PassportCheckResult as SdkCheckResult
import ru.alarmtrade.specialist.domain.mountPassport.PassportCheckTask as SdkCheckTask
import ru.alarmtrade.specialist.domain.mountPassport.PassportInstaller as SdkInstaller
import ru.alarmtrade.specialist.domain.mountPassport.PassportReadStage as SdkReadStage
import ru.alarmtrade.specialist.domain.mountPassport.PassportTransport as SdkTransport
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnit as SdkUnit
import ru.alarmtrade.specialist.domain.mountPassport.PassportUnitOrigin
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportCheckResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportFastMountResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadPhase
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStage
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnavailableReason
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnit
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup

internal fun MountPassportReadState.toDomain(): PassportReadState = PassportReadState(
    steps = steps.map { PassportStepState(it.step.toDomain(), it.status.toDomain()) },
    phase = phase.toDomain(),
    transport = transport?.toDomain(),
    unitName = unit?.systemName,
)

internal fun PassportReadSource.toSdk(): MountPassportReadSource = when (this) {
    PassportReadSource.AdvancedMount -> MountPassportReadSource.AdvancedMount
    is PassportReadSource.Complete -> MountPassportReadSource.Complete(passportId)
    is PassportReadSource.FastMount -> MountPassportReadSource.FastMount(result.toSdk())
}

internal fun PassportInstaller.toSdk(): SdkInstaller = SdkInstaller(name, contact, organization, address)

internal fun PassportReadCommand.toSdk(): MountPassportReadCommand = when (this) {
    PassportReadCommand.RETRY -> MountPassportReadCommand.RETRY
    PassportReadCommand.CONTINUE_WITHOUT -> MountPassportReadCommand.CONTINUE_WITHOUT
    PassportReadCommand.SKIP -> MountPassportReadCommand.SKIP
}

internal fun PassportReadStage.toSdk(): SdkReadStage = when (this) {
    PassportReadStage.UNIT -> SdkReadStage.UNIT
    PassportReadStage.PARAMETERS -> SdkReadStage.PARAMETERS
    PassportReadStage.PERIPHERALS -> SdkReadStage.PERIPHERALS
    PassportReadStage.SETTINGS -> SdkReadStage.SETTINGS
}

internal fun MountPassportUnitLookup.toDomain(): PassportUnitLookup = when (this) {
    is MountPassportUnitLookup.Found -> PassportUnitLookup.Found(systemName, systemId)
    MountPassportUnitLookup.NotFound -> PassportUnitLookup.NotFound
    MountPassportUnitLookup.Offline -> PassportUnitLookup.Offline
}

private fun PassportFastMountResult.toSdk(): MountPassportFastMountResult = MountPassportFastMountResult(
    vehicle = vehicle.toSdk(),
    configuration = configuration?.toSdk(),
    works = works.copy(fromMaster = true).toSdk(),
    systemCheck = systemCheck.map { SdkCheckTask(it.title, it.result.toSdk()) },
    durationMinutes = durationMinutes,
    unit = unit?.toSdk(),
)

private fun PassportUnit.toSdk(): SdkUnit = SdkUnit(
    systemName = systemName,
    systemId = systemId,
    firmwareVersion = firmwareVersion,
    can = can?.let { SdkCan(it.version, it.code, it.factory) },
    imei = imei,
    bluetoothModule = bluetoothModule?.let { SdkBluetoothModule(it.firmwareVersion, it.bootloaderVersion) },
    transport = transport?.let {
        when (it) {
            PassportTransport.BLUETOOTH -> SdkTransport.BLUETOOTH
            PassportTransport.USB -> SdkTransport.USB
        }
    },
    origin = if (manual) PassportUnitOrigin.MANUAL else PassportUnitOrigin.READ,
)

private fun PassportCheckResult.toSdk(): SdkCheckResult = when (this) {
    PassportCheckResult.PASSED -> SdkCheckResult.PASSED
    PassportCheckResult.FAILED -> SdkCheckResult.FAILED
    PassportCheckResult.SKIPPED -> SdkCheckResult.SKIPPED
}

private fun MountPassportReadStep.toDomain(): PassportReadStep = when (this) {
    MountPassportReadStep.BLOCK -> PassportReadStep.BLOCK
    MountPassportReadStep.PARAMS -> PassportReadStep.PARAMS
    MountPassportReadStep.DEVICES -> PassportReadStep.DEVICES
    MountPassportReadStep.SETTINGS -> PassportReadStep.SETTINGS
    MountPassportReadStep.READY -> PassportReadStep.READY
}

private fun MountPassportStepStatus.toDomain(): PassportStepStatus = when (this) {
    MountPassportStepStatus.Waiting -> PassportStepStatus.Waiting

    MountPassportStepStatus.Reading -> PassportStepStatus.Reading

    is MountPassportStepStatus.Done -> PassportStepStatus.Done(summary.toDomain())

    is MountPassportStepStatus.NotRead -> PassportStepStatus.NotRead(connectionLost)

    is MountPassportStepStatus.Unavailable -> PassportStepStatus.Unavailable(
        when (reason) {
            MountPassportUnavailableReason.NOT_SUPPORTED -> PassportUnavailableReason.NOT_SUPPORTED
            MountPassportUnavailableReason.FACTORY_CAN -> PassportUnavailableReason.FACTORY_CAN
        },
    )

    MountPassportStepStatus.Skipped -> PassportStepStatus.Skipped
}

private fun MountPassportStepSummary.toDomain(): PassportStepSummary = when (this) {
    is MountPassportStepSummary.Block -> PassportStepSummary.Block(systemName, systemId, firmwareVersion)
    is MountPassportStepSummary.Params -> PassportStepSummary.Params(imei, bluetoothFirmware, bluetoothBootloader)
    is MountPassportStepSummary.Devices -> PassportStepSummary.Devices(count, transport.toDomain())
    is MountPassportStepSummary.Settings -> PassportStepSummary.Settings(sizeBytes)
    is MountPassportStepSummary.Ready -> PassportStepSummary.Ready(passportId, number)
}

private fun MountPassportReadPhase.toDomain(): PassportReadPhase = when (this) {
    MountPassportReadPhase.Reading -> PassportReadPhase.Reading
    is MountPassportReadPhase.Offline -> PassportReadPhase.Offline(canContinueWithout)
    is MountPassportReadPhase.UnitFailed -> PassportReadPhase.UnitFailed(connectionLost)
    is MountPassportReadPhase.ConnectionLost -> PassportReadPhase.ConnectionLost(step.toDomain())
    is MountPassportReadPhase.WrongUnit -> PassportReadPhase.WrongUnit(expectedSystemId, actualSystemId)
    MountPassportReadPhase.SaveFailed -> PassportReadPhase.SaveFailed
    is MountPassportReadPhase.Done -> PassportReadPhase.Done(passport.id)
}

private fun SdkTransport.toDomain(): PassportTransport = when (this) {
    SdkTransport.BLUETOOTH -> PassportTransport.BLUETOOTH
    SdkTransport.USB -> PassportTransport.USB
}
