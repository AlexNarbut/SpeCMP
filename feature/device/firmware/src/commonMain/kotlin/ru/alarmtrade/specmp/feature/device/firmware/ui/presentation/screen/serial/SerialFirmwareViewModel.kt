package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toSerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceOrNull
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/**
 * Экран стоит и в рабочем слоте хоста, и в слоте загрузчика: VM одна на запись навигации, смена режима его не
 * сбрасывает. Порт при перезагрузке блока не отключается — после обновления режим перечитывается явно.
 */
class SerialFirmwareViewModel(
    private val repository: SerialFirmwareRepository,
    private val deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<SerialFirmwareState, SerialFirmwareEffect>(SerialFirmwareState()) {

    override suspend fun onStart() {
        deviceSession.state.collectIntoState { session ->
            copy(device = session.deviceOrNull, deviceInBoot = session is DeviceSessionState.FirmwareBoot)
        }
    }

    fun onFilePicked(result: FirmwareFilePickResult) {
        if (currentState.phase != SerialPhase.Ready) return
        when (result) {
            FirmwareFilePickResult.Cancelled -> Unit
            is FirmwareFilePickResult.Picked -> checkFile(result.name, result.path)
            FirmwareFilePickResult.TooLarge -> showFileError(FileError.TooLarge)
            FirmwareFilePickResult.Failed -> showFileError(FileError.Failed)
        }
    }

    private fun showFileError(error: FileError) {
        reduce { if (phase == SerialPhase.Ready) copy(fileError = error, fileProblem = null) else this }
    }

    private fun checkFile(fileName: String, path: String) {
        intent {
            val info = withContext(dispatchers.io) { repository.readFileInfo(path) }
            val current = deviceSession.state.value.deviceOrNull
            reduce {
                when {
                    phase != SerialPhase.Ready -> this

                    info !is Response.Success -> copy(fileError = null, fileProblem = SerialFileProblem.NotFrm)

                    else -> {
                        val version = info.value.version
                        val older = version != null && current != null &&
                            version.relationTo(current.firmwareVersion) == VersionRelation.Older
                        val mismatch = current?.takeUnless { info.value.isFor(it.systemName) }?.let {
                            SystemNameMismatch(info.value.systemName, it.systemName.trim())
                        }
                        copy(
                            phase = SerialPhase.Confirm(fileName, path, version, older, mismatch),
                            fileError = null,
                            fileProblem = null,
                        )
                    }
                }
            }
        }
    }

    fun onCancelConfirm() {
        reduce { if (phase is SerialPhase.Confirm) copy(phase = SerialPhase.Ready) else this }
    }

    fun onConfirmUpdate() {
        intent {
            val confirm = currentState.phase as? SerialPhase.Confirm ?: return@intent
            // Фаза уходит из Confirm до первой приостановки: двойное нажатие запустит обновление один раз.
            reduce { copy(phase = SerialPhase.WaitingBoot) }
            repository.update(confirm.fileName, confirm.path)
                .flowOn(dispatchers.io)
                .collect { step ->
                    val phase = when (step) {
                        is Response.Success -> step.value.toPhase(confirm.fileVersion)
                        is Response.Error -> SerialPhase.Error(step.exception.toSerialFirmwareFailure())
                    }
                    reduce { copy(phase = phase) }
                }
            // Блок мог перезагрузиться (в загрузчик и обратно) — пусть сессия узнает его режим.
            deviceSession.refreshMode()
        }
    }

    fun onResultOk() {
        reduce {
            if (phase is SerialPhase.Success || phase is SerialPhase.Error) {
                copy(phase = SerialPhase.Ready, fileError = null, fileProblem = null)
            } else {
                this
            }
        }
    }

    fun onBack() {
        if (currentState.locked) return
        postSideEffect(SerialFirmwareEffect.Exit)
    }

    private fun SerialFirmwareProgress.toPhase(fileVersion: String?): SerialPhase = when (this) {
        SerialFirmwareProgress.WaitingForBoot -> SerialPhase.WaitingBoot
        SerialFirmwareProgress.CheckingBoot -> SerialPhase.CheckingBoot
        is SerialFirmwareProgress.Sending -> SerialPhase.Transfer(percent)
        SerialFirmwareProgress.Verifying -> SerialPhase.Verify
        SerialFirmwareProgress.Applying -> SerialPhase.Apply
        SerialFirmwareProgress.Finished -> SerialPhase.Success(fileVersion)
    }
}
