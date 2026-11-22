package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwarePreselect
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/**
 * Фазы [UpdatePhase.Entering], [UpdatePhase.Finishing] и [UpdatePhase.Lost] выводятся из связи и ожидаемой
 * перезагрузки: её мы ждём (`reboot`), обрыв без ожидания — потеря связи. Передача по BLE обрыв переживает: SDK
 * переподключается и продолжает (на iOS загрузчик модуля теряет связь каждые ~30 с).
 */
class FirmwareUpdateViewModel(
    private val deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    private val updateRepository: FirmwareUpdateRepository,
    bootRequester: FirmwareBootRequester,
    private val archivePicker: FirmwareArchivePicker,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<FirmwareUpdateState, FirmwareUpdateEffect>(
    bootRequester.take().initialUpdateState(deviceSession.state.value, deviceSession.reboot.value),
) {

    private val core: UpdateCore get() = currentState.core

    override suspend fun IntentScope.onStart() {
        intent {
            combine(deviceSession.state, deviceSession.reboot, ::Pair).collect { (session, reboot) ->
                // Ожидание снято (дождались, тайм-аут, отмена) — подтверждение относилось к нему.
                reduceCore {
                    copy(
                        session = session,
                        reboot = reboot,
                        acknowledged = acknowledged && (reboot != null || this.reboot == null),
                    )
                }
            }
        }
        // Версия из архива: экран архива лежит поверх, эта ViewModel под ним жива и ждёт выбор.
        intent { archivePicker.picks.collect { onArchivePicked(it) } }
        deviceSession.state.collectLatest { onSession(it) }
    }

    private suspend fun reduceCore(transform: UpdateCore.() -> UpdateCore) = subIntent {
        reduce { core.transform().toState() }
    }

    private suspend fun onSession(session: DeviceSessionState) = subIntent {
        when (session) {
            is DeviceSessionState.FirmwareBoot, is DeviceSessionState.BluetoothBoot -> {
                onBootReturned()
                // Под тем же замком, что и «Повторить»: чтение профиля не идёт дважды параллельно.
                if (core.profile == null) executeWithProceedLock { loadProfile(session) }
            }

            DeviceSessionState.Disconnected, DeviceSessionState.Connecting -> onLinkDown()

            is DeviceSessionState.Ready -> Unit
        }
    }

    private suspend fun onLinkDown() = subIntent {
        val current = core
        when {
            current.finishing -> reduceCore { copy(linkDropped = true) }

            // В блок ещё ничего не записано: продолжать нечего — «Связь прервана», после возврата блока — выбор
            // источника.
            current.stage.notStarted() -> {
                cancelLaunched(UPDATE_PROCESS)
                reduceCore { copy(linkDropped = true, running = false) }
            }

            current.stage.isUpdating() -> reduceCore { copy(linkDropped = true) }
        }
    }

    // Блок снова в загрузчике. Если связь пропадала в процессе, обновление не дошло до конца.
    private suspend fun onBootReturned() = subIntent {
        val current = core
        if (!current.linkDropped) return@subIntent
        if (current.finishing) {
            // Блок перезагрузился, но снова в загрузчике: выход не удался.
            deviceSession.cancelReboot()
            reduceCore {
                copy(
                    stage = UpdatePhase.Source,
                    candidate = null,
                    localFile = null,
                    acknowledged = false,
                    exitError = true,
                    linkDropped = false,
                )
            }
        } else if (current.resumable) {
            // Передача продолжается сама: SDK дождался связи и шлёт неподтверждённый кадр.
            reduceCore { copy(linkDropped = false) }
        } else if (current.stage == UpdatePhase.Apply && current.running) {
            // Блок после применения перезагружается обратно в загрузчик, и SDK только теперь спрашивает у него итог.
            reduceCore { copy(linkDropped = false) }
        } else if (current.stage == UpdatePhase.Apply) {
            // SDK итога не дал, а блок вместо рабочего режима снова в загрузчике: не применилась.
            cancelLaunched(UPDATE_PROCESS)
            deviceSession.cancelReboot()
            reduceCore { copy(stage = UpdatePhase.Error(FailureKind.ApplyingError), linkDropped = false) }
        } else if (current.stage.isUpdating()) {
            cancelLaunched(UPDATE_PROCESS)
            reduceCore { copy(stage = UpdatePhase.Source, candidate = null, localFile = null, linkDropped = false) }
        } else {
            reduceCore { copy(linkDropped = false) }
        }
    }

    private suspend fun loadProfile(session: DeviceSessionState) = subIntent {
        val transport = deviceSession.transport.value ?: return@subIntent
        reduceCore { if (stage == UpdatePhase.DataError) copy(stage = UpdatePhase.Loading) else this }
        val source = session.bootSource() ?: return@subIntent
        val response = withContext(dispatchers.io) { repository.modules(source.deviceName, transport) }
        val modules = (response as? Response.Success)?.value.orEmpty().filter { it.bootTarget == source.target }
        reduceCore { withBlock(source, modules, transport) }
        applyPreselect()
    }

    // Версия выбрана ещё до загрузчика (архив, ассистент): сразу на подтверждение, без выбора источника.
    private suspend fun applyPreselect() = subIntent {
        val preselect = core.preselect ?: return@subIntent
        if (core.stage != UpdatePhase.Source) return@subIntent
        reduceCore { copy(preselect = null) }
        when (preselect) {
            FirmwarePreselect.Latest -> downloadLatest()
            is FirmwarePreselect.Version -> onArchivePicked(preselect.version)
        }
    }

    fun onModuleSelected(index: Int) {
        intent {
            val module = core.profile?.modules?.getOrNull(index) ?: return@intent
            reduceCore { copy(selectedModuleId = module.id) }
        }
    }

    fun onChannelSelected(channel: FirmwareChannel) {
        intent {
            reduceCore { copy(channel = channel, sourceError = null, exitError = false, fileError = null) }
        }
    }

    fun onDownloadClick() {
        intent { executeWithProceedLock { downloadLatest() } }
    }

    private suspend fun downloadLatest() = subIntent {
        val start = core
        val moduleId = start.selectedModuleId ?: return@subIntent
        if (start.stage != UpdatePhase.Source) return@subIntent
        reduceCore { copy(stage = UpdatePhase.Download, sourceError = null, fileError = null) }
        val can = start.profile?.can?.takeIf { start.isAlarmMain }
        val response = withContext(dispatchers.io) { repository.latest(moduleId, start.channel, can) }
        reduceCore {
            if (stage != UpdatePhase.Download) return@reduceCore this
            when (response) {
                is Response.Success -> copy(stage = UpdatePhase.Confirm, candidate = response.value, localFile = null)

                is Response.Error -> copy(
                    stage = UpdatePhase.Source,
                    sourceError = FirmwareError.from(response.exception),
                )
            }
        }
    }

    fun onArchiveClick() {
        intent {
            val current = core
            val profile = current.profile
            val moduleId = current.selectedModuleId
            if (current.stage != UpdatePhase.Source || profile == null || moduleId == null) return@intent
            postSideEffect(
                FirmwareUpdateEffect.OpenArchive(
                    FirmwareArchiveRequest(
                        moduleId = moduleId,
                        channel = current.channel,
                        installedVersion = profile.currentVersion,
                        deviceTitle = currentState.deviceTitle,
                        profile = profile.kind,
                        // Каталог автомобилей CAN-фильтра — под модель блока (у Bluetooth-модуля фильтра нет).
                        deviceName = profile.deviceName.takeIf { profile.kind == UpdateProfile.Main },
                        deviceCanCode = profile.can?.code,
                        deviceCanVersion = profile.can?.version,
                        deviceCanProfile = profile.can?.profileName,
                        deviceCanFactory = profile.factoryCan,
                        canFilterAllowed = current.isAlarmMain,
                    ),
                ),
            )
        }
    }

    fun onFilePicked(result: FirmwareFilePickResult) {
        intent {
            reduceCore {
                if (stage != UpdatePhase.Source) return@reduceCore this
                when (result) {
                    FirmwareFilePickResult.Cancelled -> this

                    is FirmwareFilePickResult.Picked -> copy(
                        stage = UpdatePhase.Confirm,
                        candidate = null,
                        localFile = result,
                        sourceError = null,
                        fileError = null,
                    )

                    FirmwareFilePickResult.TooLarge -> copy(fileError = FileError.TooLarge)

                    FirmwareFilePickResult.Failed -> copy(fileError = FileError.Failed)
                }
            }
        }
    }

    // Версия выбрана в архиве: сразу на подтверждение (версия, описание и ссылка уже известны — запрашивать нечего).
    private suspend fun onArchivePicked(version: FirmwareVersion) = subIntent {
        reduceCore {
            if (stage != UpdatePhase.Source) return@reduceCore this
            copy(stage = UpdatePhase.Confirm, candidate = version, localFile = null, sourceError = null)
        }
    }

    fun onCancelConfirm() {
        intent {
            reduceCore {
                if (stage != UpdatePhase.Confirm) return@reduceCore this
                copy(stage = UpdatePhase.Source, candidate = null, localFile = null)
            }
        }
    }

    fun onConfirmUpdate() {
        intent {
            val current = core
            val source = current.localFile?.let { UpdateSource.File(it.path) }
                ?: current.candidate?.let { UpdateSource.Server(it) }
                ?: return@intent
            if (current.stage != UpdatePhase.Confirm) return@intent
            reduceCore { copy(stage = UpdatePhase.Prepare, linkDropped = false) }
            launchReplacing(UPDATE_PROCESS) { runUpdate(source) }
        }
    }

    /** Блок ещё в загрузчике — снова выбор источника; уже вернулся в рабочий режим — хост уйдёт со слота сам. */
    fun onResultOk() {
        intent {
            if (currentState.phase != UpdatePhase.Success) return@intent
            if (currentState.inBoot) {
                // Блок остался в загрузчике: ожидание возврата, поставленное на «Применении», больше не нужно.
                if (deviceSession.reboot.value == DeviceReboot.ToMain) deviceSession.cancelReboot()
                reduceCore {
                    copy(
                        stage = UpdatePhase.Source,
                        candidate = null,
                        localFile = null,
                        sourceError = null,
                        exitError = false,
                    )
                }
            } else {
                reduceCore { copy(acknowledged = true) }
                deviceSession.acknowledgeReboot()
            }
        }
    }

    /** Ожидание возврата — до команды: блок может перезагрузиться раньше ответа; обрыв связи и есть перезагрузка. */
    fun onExitBootClick() {
        val current = core
        val profile = current.profile ?: return
        if (current.stage != UpdatePhase.Source || !profile.canExit) return
        val target = profile.target
        intent {
            executeWithProceedLock {
                deviceSession.expectReboot(DeviceReboot.ToMain)
                deviceSession.acknowledgeReboot()
                reduceCore { copy(acknowledged = true, exitError = false) }
                val response = withContext(dispatchers.io) { deviceSession.exitBootMode(target) }
                val stillInBoot = deviceSession.state.value.isBoot
                if (response is Response.Error && stillInBoot) {
                    logger.d { "Exit boot mode failed: ${response.exception::class.simpleName}" }
                    deviceSession.cancelReboot()
                    reduceCore { copy(acknowledged = false, exitError = true) }
                }
            }
        }
    }

    fun onRetry() {
        intent {
            val stage = core.stage
            when {
                stage is UpdatePhase.Error -> reduceCore {
                    copy(stage = UpdatePhase.Source, candidate = null, localFile = null, sourceError = null)
                }

                stage == UpdatePhase.DataError -> {
                    val session = deviceSession.state.value
                    intent { executeWithProceedLock { loadProfile(session) } }
                }
            }
        }
    }

    fun onSupportClick() {
        intent {
            postSideEffect(FirmwareUpdateEffect.OpenSupportChat)
        }
    }

    fun onReconnect() {
        intent {
            // Экран остаётся на «Связь потеряна», пока сессия не вернётся, — неудачу попытки достаточно залогировать.
            withContext(dispatchers.io) { deviceSession.reconnectNow() }
                ?.onError { logger.w(it) { "Reconnect attempt failed" } }
        }
    }

    fun onDisconnect() {
        intent {
            postSideEffect(FirmwareUpdateEffect.Exit)
        }
    }

    fun onBackClick() {
        intent {
            val current = currentState
            when {
                current.locked -> Unit

                current.phase is UpdatePhase.Error || current.phase is UpdatePhase.Lost ->
                    postSideEffect(FirmwareUpdateEffect.Exit)

                else -> reduceCore { copy(exitDialog = true) }
            }
        }
    }

    fun onExitDialogDismiss() {
        intent {
            reduceCore { copy(exitDialog = false) }
        }
    }

    fun onExitConfirm() {
        intent {
            reduceCore { copy(exitDialog = false) }
            postSideEffect(FirmwareUpdateEffect.Exit)
        }
    }

    private suspend fun runUpdate(source: UpdateSource) = subIntent {
        val target = core.profile?.target ?: return@subIntent
        val progress = when (source) {
            is UpdateSource.Server -> updateRepository.update(target, source.version)
            is UpdateSource.File -> updateRepository.updateFromFile(target, source.path)
        }
        // SDK шлёт событие на каждый пакет передачи: маппинг и отсев повторов — в фоне (io), на Main приходит
        // только смена стадии или процента; переходы состояния (onProgress) — в коллекторе, на Main.
        reduceCore { copy(running = true) }
        try {
            progress
                .distinctUntilChanged()
                .flowOn(dispatchers.io)
                .collect { step ->
                    when (step) {
                        is Response.Success -> onProgress(step.value)
                        is Response.Error -> onFailed(step.exception.toFirmwareFailure())
                    }
                }
        } finally {
            deviceSession.endFirmwareTransfer()
            reduceCore { copy(running = false) }
        }
    }

    private suspend fun onProgress(progress: FirmwareUpdateProgress) = subIntent {
        // После передачи блок снова может сменить режим (применение — перезагрузка): режим опять перечитываем.
        if (progress !is FirmwareUpdateProgress.Sending) deviceSession.endFirmwareTransfer()
        when (progress) {
            FirmwareUpdateProgress.Preparing -> setStage(UpdatePhase.Prepare)

            is FirmwareUpdateProgress.Sending -> {
                deviceSession.beginFirmwareTransfer()
                setStage(UpdatePhase.Transfer(progress.percent))
            }

            FirmwareUpdateProgress.Verifying -> setStage(UpdatePhase.Verify)

            FirmwareUpdateProgress.Applying -> {
                // Блок перезагрузится до результата — обрыв связи дальше ожидаем, а не считаем потерей.
                deviceSession.expectReboot(DeviceReboot.ToMain)
                setStage(UpdatePhase.Apply)
            }

            // После успеха блок остаётся в загрузчике: выход — отдельной командой (или блок перезагрузится сам).
            FirmwareUpdateProgress.Finished -> setStage(UpdatePhase.Success)
        }
    }

    private suspend fun onFailed(failure: FirmwareFailure) = subIntent {
        deviceSession.endFirmwareTransfer()
        // SDK сдался: продолжения передачи после переподключения больше не будет.
        reduceCore { copy(running = false) }
        // Неизвестный сбой при потере связи — это обрыв: экран покажет «Связь прервана», а после возврата блока
        // вернётся к выбору источника. Связь оценивается с запасом: поток SDK иногда падает раньше статуса связи.
        if (failure == FirmwareFailure.Unknown && linkWentDown()) {
            reduceCore { copy(linkDropped = true) }
            return@subIntent
        }
        if (deviceSession.reboot.value == DeviceReboot.ToMain) deviceSession.cancelReboot()
        setStage(UpdatePhase.Error(failure.toKind()))
    }

    private suspend fun linkWentDown(): Boolean = withTimeoutOrNull(LINK_GRACE) {
        deviceSession.state.first { it.isLinkDown }
    } != null

    // Стадия меняется, только пока процесс идёт: запоздавшее событие отменённого процесса экран не трогает.
    private suspend fun setStage(next: UpdatePhase) = subIntent {
        reduceCore { if (stage.isUpdating() || stage == UpdatePhase.Success) copy(stage = next) else this }
    }

    private companion object {
        const val UPDATE_PROCESS = "update"
        val LINK_GRACE = 3.seconds
    }
}
