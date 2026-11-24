package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.fileNameOf
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.isFrmFileName
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toSerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceOrNull
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/**
 * Экран стоит и в рабочем слоте хоста, и в слоте загрузчика: VM одна на запись навигации, смена режима его не
 * сбрасывает (поэтому и архив открывается из state, а не вложенной навигацией — у неё в каждом слоте была бы своя
 * VM). Порт при перезагрузке блока не отключается — после обновления режим перечитывается явно.
 */
class SerialFirmwareViewModel(
    private val repository: SerialFirmwareRepository,
    private val firmwareRepository: FirmwareRepository,
    private val deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<SerialFirmwareState, SerialFirmwareEffect>(SerialFirmwareState()) {

    override suspend fun IntentScope.onStart() {
        deviceSession.state.collectIntoState { session ->
            copy(device = session.deviceOrNull, deviceInBoot = session is DeviceSessionState.FirmwareBoot)
        }
    }

    fun onChannelSelected(channel: FirmwareChannel) {
        intent {
            reduce { if (phase == SerialPhase.Ready) copy(channel = channel).clearedProblems() else this }
        }
    }

    fun onFilePicked(result: FirmwareFilePickResult) {
        intent {
            if (currentState.phase != SerialPhase.Ready) return@intent
            when (result) {
                FirmwareFilePickResult.Cancelled -> Unit

                is FirmwareFilePickResult.Picked -> if (isFrmFileName(result.name)) {
                    inspect(result.name, result.path, server = null)
                } else {
                    showFileProblem(SerialFileProblem.NotFrm, from = SerialPhase.Ready)
                }

                FirmwareFilePickResult.TooLarge -> showFileError(FileError.TooLarge)

                FirmwareFilePickResult.Failed -> showFileError(FileError.Failed)
            }
        }
    }

    private suspend fun showFileProblem(problem: SerialFileProblem, from: SerialPhase) = subIntent {
        reduce { if (phase == from) clearedProblems().copy(phase = SerialPhase.Ready, fileProblem = problem) else this }
    }

    private suspend fun showFileError(error: FileError) = subIntent {
        reduce { if (phase == SerialPhase.Ready) clearedProblems().copy(fileError = error) else this }
    }

    fun onDownloadClick() {
        intent {
            if (currentState.phase != SerialPhase.Ready) return@intent
            reduce { clearedProblems().copy(phase = SerialPhase.Download) }
            launchReplacing(DOWNLOAD) { downloadLatest() }
        }
    }

    private suspend fun downloadLatest() = subIntent {
        val channel = currentState.channel
        val moduleId = serverModule() ?: return@subIntent
        val can = deviceSession.state.value.deviceOrNull?.can.toFirmwareCan()
        when (val latest = withContext(dispatchers.io) { firmwareRepository.latest(moduleId, channel, can) }) {
            is Response.Success -> fetch(latest.value)
            is Response.Error -> failSource(SerialSourceError.Server(FirmwareError.from(latest.exception)))
        }
    }

    fun onArchiveClick() {
        intent {
            if (currentState.phase != SerialPhase.Ready) return@intent
            executeWithProceedLock {
                reduce { clearedProblems() }
                val moduleId = serverModule() ?: return@executeWithProceedLock
                val device = deviceSession.state.value.deviceOrNull ?: return@executeWithProceedLock
                reduce {
                    if (phase == SerialPhase.Ready) copy(archive = archiveRequest(moduleId, device)) else this
                }
            }
        }
    }

    // Версия выбрана в архиве: описание и ссылка уже известны — сразу скачивание.
    fun onArchivePicked(version: FirmwareVersion) {
        intent {
            if (currentState.archive == null || currentState.phase != SerialPhase.Ready) return@intent
            reduce { clearedProblems().copy(archive = null, phase = SerialPhase.Download) }
            launchReplacing(DOWNLOAD) { fetch(version) }
        }
    }

    fun onArchiveClosed() {
        intent {
            reduce { copy(archive = null) }
        }
    }

    private fun SerialFirmwareState.archiveRequest(moduleId: Long, device: AlarmDevice): FirmwareArchiveRequest {
        val can = device.can
        val firmwareCan = can.toFirmwareCan()
        return FirmwareArchiveRequest(
            moduleId = moduleId,
            channel = channel,
            installedVersion = device.firmwareVersion,
            deviceTitle = device.title,
            profile = UpdateProfile.Main,
            deviceName = device.systemName.trim(),
            deviceCanCode = firmwareCan?.code,
            deviceCanVersion = firmwareCan?.version,
            deviceCanProfile = firmwareCan?.profileName,
            deviceCanFactory = can?.isFactory == true,
        )
    }

    /** Модуль модели блока на сервере ([FirmwareRepository.serialModuleId]); не нашёлся — ошибка источника. */
    private suspend fun serverModule(): Long? = subIntent {
        currentState.moduleId?.let { return@subIntent it }
        val deviceName = deviceSession.state.value.deviceOrNull?.systemName?.trim()
        if (deviceName.isNullOrEmpty()) {
            failSource(SerialSourceError.NoModule)
            return@subIntent null
        }
        val response = withContext(dispatchers.io) { firmwareRepository.serialModuleId(deviceName) }
        when (response) {
            is Response.Success -> {
                val id = response.value
                if (id == null) failSource(SerialSourceError.NoModule) else reduce { copy(moduleId = id) }
                id
            }

            is Response.Error -> {
                failSource(SerialSourceError.Server(FirmwareError.from(response.exception)))
                null
            }
        }
    }

    // Сначала расширение по ссылке: не `.frm` не скачиваем; заголовок файла — второй рубеж после скачивания.
    private suspend fun fetch(version: FirmwareVersion) = subIntent {
        if (!isFrmFileName(version.url)) {
            showFileProblem(SerialFileProblem.ServerNotFrm, from = SerialPhase.Download)
            return@subIntent
        }
        when (val file = withContext(dispatchers.io) { repository.download(version.url) }) {
            is Response.Success -> inspect(fileNameOf(version.url), file.value, server = version)
            is Response.Error -> failSource(SerialSourceError.Server(FirmwareError.from(file.exception)))
        }
    }

    private suspend fun failSource(error: SerialSourceError) = subIntent {
        reduce {
            if (phase == SerialPhase.Ready || phase == SerialPhase.Download) {
                copy(phase = SerialPhase.Ready, sourceError = error)
            } else {
                this
            }
        }
    }

    /** Файл — с диска ([server] = `null`, ждём на выборе) или скачанная версия (ждём на скачивании). */
    private suspend fun inspect(fileName: String, path: String, server: FirmwareVersion?) = subIntent {
        val expected = if (server == null) SerialPhase.Ready else SerialPhase.Download
        val info = withContext(dispatchers.io) { repository.readFileInfo(path) }
        val current = deviceSession.state.value.deviceOrNull
        reduce {
            when {
                phase != expected -> this

                info !is Response.Success -> clearedProblems().copy(
                    phase = SerialPhase.Ready,
                    fileProblem = if (server == null) SerialFileProblem.NotFrm else SerialFileProblem.ServerNotFrm,
                )

                else -> {
                    val version = info.value.version ?: server?.version
                    val older = version != null && current != null &&
                        version.relationTo(current.firmwareVersion) == VersionRelation.Older
                    val mismatch = current?.takeUnless { info.value.isFor(it.systemName) }?.let {
                        SystemNameMismatch(info.value.systemName, it.systemName.trim())
                    }
                    clearedProblems().copy(
                        phase = SerialPhase.Confirm(
                            fileName = fileName,
                            path = path,
                            fileVersion = version,
                            older = older,
                            nameMismatch = mismatch,
                            description = server?.description.orEmpty(),
                            fromServer = server != null,
                        ),
                    )
                }
            }
        }
    }

    fun onCancelConfirm() {
        intent {
            reduce { if (phase is SerialPhase.Confirm) copy(phase = SerialPhase.Ready) else this }
        }
    }

    fun onConfirmUpdate() {
        intent {
            val confirm = currentState.phase as? SerialPhase.Confirm ?: return@intent
            // Фаза уходит из Confirm до первой приостановки: двойное нажатие запустит обновление один раз.
            reduce { copy(phase = SerialPhase.WaitingBoot) }
            // На всё обновление: сессия не опрашивает молчащий блок (его перезагружают в загрузчик), хост не рисует
            // потерю связи — обрыв покажет ошибка обновления.
            deviceSession.beginFirmwareTransfer()
            try {
                repository.update(confirm.fileName, confirm.path)
                    .flowOn(dispatchers.io)
                    .collect { step ->
                        val phase = when (step) {
                            is Response.Success -> step.value.toPhase(confirm.fileVersion)
                            is Response.Error -> SerialPhase.Error(step.exception.toSerialFirmwareFailure())
                        }
                        reduce { copy(phase = phase) }
                    }
            } finally {
                deviceSession.endFirmwareTransfer()
            }
            // Блок мог перезагрузиться (в загрузчик и обратно) — пусть сессия узнает его режим.
            deviceSession.refreshMode()
        }
    }

    fun onResultOk() {
        intent {
            reduce {
                if (phase is SerialPhase.Success || phase is SerialPhase.Error) {
                    clearedProblems().copy(phase = SerialPhase.Ready)
                } else {
                    this
                }
            }
        }
    }

    fun onBack() {
        intent {
            when {
                currentState.phase == SerialPhase.Download -> {
                    cancelLaunched(DOWNLOAD)
                    reduce { if (phase == SerialPhase.Download) copy(phase = SerialPhase.Ready) else this }
                }

                currentState.locked -> Unit

                else -> postSideEffect(SerialFirmwareEffect.Exit)
            }
        }
    }

    private fun SerialFirmwareState.clearedProblems(): SerialFirmwareState =
        copy(fileError = null, fileProblem = null, sourceError = null)

    private fun SerialFirmwareProgress.toPhase(fileVersion: String?): SerialPhase = when (this) {
        SerialFirmwareProgress.WaitingForBoot -> SerialPhase.WaitingBoot
        SerialFirmwareProgress.CheckingBoot -> SerialPhase.CheckingBoot
        is SerialFirmwareProgress.Sending -> SerialPhase.Transfer(percent)
        SerialFirmwareProgress.Verifying -> SerialPhase.Verify
        SerialFirmwareProgress.Applying -> SerialPhase.Apply
        SerialFirmwareProgress.Finished -> SerialPhase.Success(fileVersion)
    }

    private companion object {
        const val DOWNLOAD = "download"
    }
}
