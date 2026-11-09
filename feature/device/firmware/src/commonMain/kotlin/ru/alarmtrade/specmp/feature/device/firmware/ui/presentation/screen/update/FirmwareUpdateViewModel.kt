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
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * «Обновление прошивки» (блок в загрузчике): выбор источника → версия → процесс → результат.
 *
 * Фазы «блок входит в загрузчик» ([UpdatePhase.Entering]), «ждём блок после прошивки» ([UpdatePhase.Finishing])
 * и «связь прервана» ([UpdatePhase.Lost]) выводятся из связи и ожидаемой перезагрузки сессии, а не пишутся
 * вручную: перезагрузку мы ждём (`reboot`), обрыв без ожидания — потеря связи. Остальные — состояние процесса,
 * которое меняют действия пользователя и поток [FirmwareUpdateRepository.update].
 *
 * Связь при процессе: на стадии «Применение» блок перезагружается до результата — ставим ожидание возврата
 * в рабочий режим; если блок вернулся и результата не было — это успех. Передача прошивки по BLE (основная
 * программа и Bluetooth-модуль) обрыв переживает: SDK ждёт переподключения и продолжает с прерванного кадра (или
 * начинает передачу заново, если блок продолжение не принял; на iOS загрузчик модуля теряет связь каждые ~30 с),
 * экран показывает «переподключение» на той же передаче, а сессия на это время не перечитывает режим блока
 * ([DeviceSession.beginFirmwareTransfer]). Обрыв на других стадиях
 * (и после того, как SDK сдался) прерывает обновление: после возврата блока в загрузчик экран снова на выборе
 * источника; если блок после применения вернулся в загрузчике — ошибка.
 *
 * Процесс, связь и выбор в архиве живут всю жизнь VM, а не подписку на state: обновление идёт, пока экран перекрыт
 * архивом или приложение свёрнуто.
 */
class FirmwareUpdateViewModel(
    private val deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    private val updateRepository: FirmwareUpdateRepository,
    bootRequester: FirmwareBootRequester,
    private val archivePicker: FirmwareArchivePicker,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<FirmwareUpdateState, FirmwareUpdateEffect>(
    // Модуль и канал — из запроса входа в загрузчик, забираются при создании VM.
    bootRequester.take().initialUpdateState(deviceSession.state.value, deviceSession.reboot.value),
) {

    private val core: UpdateCore get() = currentState.core

    override suspend fun onStart() {
        safeLaunch {
            combine(deviceSession.state, deviceSession.reboot, ::Pair).collect { (session, reboot) ->
                reduceCore { copy(session = session, reboot = reboot) }
            }
        }
        // Версия из архива: экран архива лежит поверх, эта ViewModel под ним жива и ждёт выбор.
        safeLaunch { archivePicker.picks.collect(::onArchivePicked) }
        deviceSession.state.collectLatest(::onSession)
    }

    private fun reduceCore(transform: UpdateCore.() -> UpdateCore) {
        reduce { core.transform().toState() }
    }

    // ───── связь ─────

    private suspend fun onSession(session: DeviceSessionState) {
        when (session) {
            is DeviceSessionState.FirmwareBoot, is DeviceSessionState.BluetoothBoot -> {
                onBootReturned()
                // Под тем же замком, что и «Повторить»: чтение профиля не идёт дважды параллельно.
                if (core.profile == null) executeWithProceedLock { loadProfile(session) }
            }

            DeviceSessionState.Disconnected, DeviceSessionState.Connecting ->
                reduceCore { if (stage.isUpdating()) copy(linkDropped = true) else this }

            is DeviceSessionState.Ready -> Unit
        }
    }

    // Блок снова в загрузчике. Если связь пропадала в процессе, обновление не дошло до конца.
    private fun onBootReturned() {
        val current = core
        if (!current.linkDropped) return
        if (current.resumable) {
            // Передача продолжается сама: SDK дождался связи и шлёт неподтверждённый кадр.
            reduceCore { copy(linkDropped = false) }
        } else if (current.stage == UpdatePhase.Apply) {
            // После применения блок должен был вернуться в рабочий режим, а он снова в загрузчике: не применилась.
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

    private suspend fun loadProfile(session: DeviceSessionState) {
        val transport = deviceSession.transport.value ?: return
        reduceCore { if (stage == UpdatePhase.DataError) copy(stage = UpdatePhase.Loading) else this }
        val source = session.bootSource() ?: return
        val response = withContext(dispatchers.io) { repository.modules(source.deviceName, transport) }
        val modules = (response as? Response.Success)?.value.orEmpty().filter { it.bootTarget == source.target }
        reduceCore { withBlock(source, modules, transport) }
    }

    // ───── интенты ─────

    fun onModuleSelected(index: Int) {
        val module = core.profile?.modules?.getOrNull(index) ?: return
        reduceCore { copy(selectedModuleId = module.id) }
    }

    fun onChannelSelected(channel: FirmwareChannel) {
        reduceCore { copy(channel = channel, sourceError = null, exitError = false, fileError = null) }
    }

    /** «Скачать прошивку»: запросить последнюю версию выбранного канала и перейти к подтверждению. */
    fun onDownloadClick() {
        intent {
            executeWithProceedLock {
                val start = core
                val moduleId = start.selectedModuleId ?: return@executeWithProceedLock
                if (start.stage != UpdatePhase.Source) return@executeWithProceedLock
                reduceCore { copy(stage = UpdatePhase.Download, sourceError = null, fileError = null) }
                val can = start.profile?.can?.takeIf { start.isAlarmMain }
                val response = withContext(dispatchers.io) { repository.latest(moduleId, start.channel, can) }
                reduceCore {
                    if (stage != UpdatePhase.Download) return@reduceCore this
                    when (response) {
                        is Response.Success ->
                            copy(stage = UpdatePhase.Confirm, candidate = response.value, localFile = null)

                        is Response.Error ->
                            copy(stage = UpdatePhase.Source, sourceError = FirmwareError.from(response.exception))
                    }
                }
            }
        }
    }

    fun onArchiveClick() {
        val current = core
        val profile = current.profile
        val moduleId = current.selectedModuleId
        if (current.stage != UpdatePhase.Source || profile == null || moduleId == null) return
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

    /** Результат выбора файла прошивки в файловом менеджере: годный файл — сразу на подтверждение. */
    fun onFilePicked(result: FirmwareFilePickResult) {
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

    // Версия выбрана в архиве: сразу на подтверждение (версия, описание и ссылка уже известны — запрашивать нечего).
    private fun onArchivePicked(version: FirmwareVersion) {
        reduceCore {
            if (stage != UpdatePhase.Source) return@reduceCore this
            copy(stage = UpdatePhase.Confirm, candidate = version, localFile = null, sourceError = null)
        }
    }

    fun onCancelConfirm() {
        reduceCore {
            if (stage != UpdatePhase.Confirm) return@reduceCore this
            copy(stage = UpdatePhase.Source, candidate = null, localFile = null)
        }
    }

    /** «Обновить» на подтверждении: запуск процесса с выбранной версией. */
    fun onConfirmUpdate() {
        val current = core
        val source = current.localFile?.let { UpdateSource.File(it.path) }
            ?: current.candidate?.let { UpdateSource.Server(it) }
            ?: return
        if (current.stage != UpdatePhase.Confirm) return
        reduceCore { copy(stage = UpdatePhase.Prepare, linkDropped = false) }
        launchReplacing(UPDATE_PROCESS) { runUpdate(source) }
    }

    /**
     * «OK» на успехе. Блок ещё в загрузчике — обычное состояние выбора источника (можно выбрать другую прошивку или
     * выйти командой); блок уже вернулся в рабочий режим — подтверждение, хост уйдёт со слота сам.
     */
    fun onResultOk() {
        if (currentState.phase != UpdatePhase.Success) return
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

    /**
     * «Выйти из обновления» (BLE): команда перезагрузки блока в рабочий режим. Ожидание возврата ставится до команды —
     * блок может перезагрузиться раньше ответа. Не прошла и блок на связи — баннер; связь оборвалась — это и есть
     * перезагрузка, ждём возврата.
     */
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

    /** «Повторить»: после ошибки — снова к выбору источника; без данных блока — перечитать подсистемы. */
    fun onRetry() {
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

    fun onSupportClick() {
        postSideEffect(FirmwareUpdateEffect.OpenSupportChat)
    }

    fun onReconnect() {
        intent { withContext(dispatchers.io) { deviceSession.reconnectNow() } }
    }

    fun onDisconnect() {
        postSideEffect(FirmwareUpdateEffect.Exit)
    }

    /** «Назад»: на процессе заблокирован; на выборе — предупреждение; после ошибки и обрыва — сразу выход. */
    fun onBackClick() {
        val current = currentState
        when {
            current.locked -> Unit

            current.phase is UpdatePhase.Error || current.phase is UpdatePhase.Lost ->
                postSideEffect(FirmwareUpdateEffect.Exit)

            else -> reduceCore { copy(exitDialog = true) }
        }
    }

    fun onExitDialogDismiss() {
        reduceCore { copy(exitDialog = false) }
    }

    fun onExitConfirm() {
        reduceCore { copy(exitDialog = false) }
        postSideEffect(FirmwareUpdateEffect.Exit)
    }

    // ───── процесс обновления ─────

    private suspend fun runUpdate(source: UpdateSource) {
        val target = core.profile?.target ?: return
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

    private fun onProgress(progress: FirmwareUpdateProgress) {
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

    private suspend fun onFailed(failure: FirmwareFailure) {
        deviceSession.endFirmwareTransfer()
        // SDK сдался: продолжения передачи после переподключения больше не будет.
        reduceCore { copy(running = false) }
        // Неизвестный сбой при потере связи — это обрыв: экран покажет «Связь прервана», а после возврата блока
        // вернётся к выбору источника. Связь оценивается с запасом: поток SDK иногда падает раньше статуса связи.
        if (failure == FirmwareFailure.Unknown && linkWentDown()) {
            reduceCore { copy(linkDropped = true) }
            return
        }
        if (deviceSession.reboot.value == DeviceReboot.ToMain) deviceSession.cancelReboot()
        setStage(UpdatePhase.Error(failure.toKind()))
    }

    private suspend fun linkWentDown(): Boolean = withTimeoutOrNull(LINK_GRACE) {
        deviceSession.state.first { it.isLinkDown }
    } != null

    // Стадия меняется, только пока процесс идёт: запоздавшее событие отменённого процесса экран не трогает.
    private fun setStage(next: UpdatePhase) {
        reduceCore { if (stage.isUpdating() || stage == UpdatePhase.Success) copy(stage = next) else this }
    }

    private companion object {
        const val UPDATE_PROCESS = "update"
        val LINK_GRACE = 3.seconds
    }
}
