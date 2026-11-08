package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.toFirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.displayFirmwareVersion
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isBoot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.isLinkDown
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.title
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
 */
class FirmwareUpdateViewModel(
    private val deviceSession: DeviceSession,
    private val repository: FirmwareRepository,
    private val updateRepository: FirmwareUpdateRepository,
    bootRequester: FirmwareBootRequester,
    archivePicker: FirmwareArchivePicker,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

    // Начальное состояние — из запроса входа в загрузчик (модуль и канал), при создании VM.
    private val core: MutableStateFlow<Core> = bootRequester.take().let { request ->
        MutableStateFlow(
            Core(
                selectedModuleId = request?.moduleId,
                channel = request?.channel ?: FirmwareChannel.RELEASE,
            ),
        )
    }

    // Запуск (и отмена, `null`) обновления: collectLatest отменяет предыдущий процесс сам, без поля-Job.
    private val updateRequests = MutableSharedFlow<UpdateSource?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val state: StateFlow<FirmwareUpdateState> = combine(
        core,
        deviceSession.state,
        deviceSession.reboot,
    ) { core, session, reboot -> toState(core, session, reboot) }
        // Всё живёт всю жизнь VM (обновление идёт и пока экран перекрыт архивом). Подписки на запросы — до первого
        // запроса (UNDISPATCHED): у SharedFlow без replay запрос без подписчика потерялся бы.
        .launchOnFirstStart {
            launch(start = CoroutineStart.UNDISPATCHED) {
                updateRequests.collectLatest { source -> if (source != null) runUpdate(source) }
            }
            // Версия из архива: экран архива лежит поверх, эта ViewModel под ним жива и ждёт выбор.
            launch(start = CoroutineStart.UNDISPATCHED) { archivePicker.picks.collect(::onArchivePicked) }
            deviceSession.state.collectLatest { onSession(it) }
        }
        .flowOn(dispatchers.default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            toState(core.value, deviceSession.state.value, deviceSession.reboot.value),
        )

    private val _effect = sideEffectFlow<FirmwareUpdateEffect>()
    val effect: SharedFlow<FirmwareUpdateEffect> = _effect.asSharedFlow()

    // ───── связь ─────

    private suspend fun onSession(session: DeviceSessionState) {
        when (session) {
            is DeviceSessionState.FirmwareBoot, is DeviceSessionState.BluetoothBoot -> {
                onBootReturned()
                // Под тем же замком, что и «Повторить»: чтение профиля не идёт дважды параллельно.
                if (core.value.profile == null) executeWithProceedLock { loadProfile(session) }
            }

            DeviceSessionState.Disconnected, DeviceSessionState.Connecting ->
                core.update { if (it.stage.isUpdating()) it.copy(linkDropped = true) else it }

            is DeviceSessionState.Ready -> Unit
        }
    }

    // Блок снова в загрузчике. Если связь пропадала в процессе, обновление не дошло до конца.
    private fun onBootReturned() {
        val current = core.value
        if (!current.linkDropped) return
        if (current.resumable) {
            // Передача продолжается сама: SDK дождался связи и шлёт неподтверждённый кадр.
            core.update { it.copy(linkDropped = false) }
        } else if (current.stage == UpdatePhase.Apply) {
            // После применения блок должен был вернуться в рабочий режим, а он снова в загрузчике: не применилась.
            updateRequests.tryEmit(null)
            deviceSession.cancelReboot()
            core.update { it.copy(stage = UpdatePhase.Error(FailureKind.ApplyingError), linkDropped = false) }
        } else if (current.stage.isUpdating()) {
            updateRequests.tryEmit(null)
            core.update { it.copy(stage = UpdatePhase.Source, candidate = null, localFile = null, linkDropped = false) }
        } else {
            core.update { it.copy(linkDropped = false) }
        }
    }

    private suspend fun loadProfile(session: DeviceSessionState) {
        val transport = deviceSession.transport.value ?: return
        core.update { if (it.stage == UpdatePhase.DataError) it.copy(stage = UpdatePhase.Loading) else it }
        val source = when (session) {
            is DeviceSessionState.FirmwareBoot -> {
                val device = session.device
                BootSource(
                    UpdateProfile.Main,
                    BootModeTarget.MainProgram,
                    device.systemName,
                    device.title,
                    device.displayFirmwareVersion,
                    // CAN — как блок сообщает его в загрузчике; не сообщил — ищем по всем версиям.
                    can = device.can.toFirmwareCan(),
                    factoryCan = device.can?.isFactory == true,
                )
            }

            is DeviceSessionState.BluetoothBoot -> {
                val manufacturer = session.manufacturer
                BootSource(
                    UpdateProfile.Bluetooth,
                    BootModeTarget.BluetoothModule,
                    manufacturer.firmwareName,
                    manufacturer.deviceName,
                    manufacturer.mainFirmwareVersion,
                )
            }

            else -> return
        }
        val response = withContext(dispatchers.io) { repository.modules(source.deviceName, transport) }
        val modules = (response as? Response.Success)?.value.orEmpty().filter { it.bootTarget == source.target }
        core.update { state ->
            state.copy(
                profile = Profile(
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
                selectedModuleId = state.selectedModuleId?.takeIf { id -> modules.any { it.id == id } }
                    ?: modules.firstOrNull()?.id,
                stage = if (state.stage == UpdatePhase.Loading) {
                    if (modules.isEmpty()) UpdatePhase.DataError else UpdatePhase.Source
                } else {
                    state.stage
                },
            )
        }
    }

    private data class BootSource(
        val kind: UpdateProfile,
        val target: BootModeTarget,
        val deviceName: String,
        val title: String,
        val current: String,
        val can: FirmwareCan? = null,
        val factoryCan: Boolean = false,
    )

    // ───── интенты ─────

    fun onModuleSelected(index: Int) {
        core.update { state ->
            val module = state.profile?.modules?.getOrNull(index) ?: return@update state
            state.copy(selectedModuleId = module.id)
        }
    }

    fun onChannelSelected(channel: FirmwareChannel) {
        core.update { it.copy(channel = channel, sourceError = null, exitError = false, fileError = null) }
    }

    /** «Скачать прошивку»: запросить последнюю версию выбранного канала и перейти к подтверждению. */
    fun onDownloadClick() {
        safeLaunch {
            executeWithProceedLock {
                val start = core.value
                val moduleId = start.selectedModuleId ?: return@executeWithProceedLock
                if (!core.updateIf({ it.stage == UpdatePhase.Source }) {
                        it.copy(stage = UpdatePhase.Download, sourceError = null, fileError = null)
                    }
                ) {
                    return@executeWithProceedLock
                }
                val can = start.profile?.can?.takeIf { start.isAlarmMain }
                val response = withContext(dispatchers.io) { repository.latest(moduleId, start.channel, can) }
                core.updateIf({ it.stage == UpdatePhase.Download }) { state ->
                    when (response) {
                        is Response.Success -> state.copy(
                            stage = UpdatePhase.Confirm,
                            candidate = response.value,
                            localFile = null,
                        )

                        is Response.Error -> state.copy(
                            stage = UpdatePhase.Source,
                            sourceError = FirmwareError.from(response.exception),
                        )
                    }
                }
            }
        }
    }

    fun onArchiveClick() {
        val current = core.value
        val profile = current.profile
        val moduleId = current.selectedModuleId
        if (current.stage != UpdatePhase.Source || profile == null || moduleId == null) return
        _effect.tryEmit(
            FirmwareUpdateEffect.OpenArchive(
                FirmwareArchiveRequest(
                    moduleId = moduleId,
                    channel = current.channel,
                    installedVersion = profile.currentVersion,
                    deviceTitle = state.value.deviceTitle,
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
        core.updateIf({ it.stage == UpdatePhase.Source }) { state ->
            when (result) {
                FirmwareFilePickResult.Cancelled -> state

                is FirmwareFilePickResult.Picked -> state.copy(
                    stage = UpdatePhase.Confirm,
                    candidate = null,
                    localFile = result,
                    sourceError = null,
                    fileError = null,
                )

                FirmwareFilePickResult.TooLarge -> state.copy(fileError = FileError.TooLarge)

                FirmwareFilePickResult.Failed -> state.copy(fileError = FileError.Failed)
            }
        }
    }

    // Версия выбрана в архиве: сразу на подтверждение (версия, описание и ссылка уже известны — запрашивать нечего).
    private fun onArchivePicked(version: FirmwareVersion) {
        core.updateIf({ it.stage == UpdatePhase.Source }) {
            it.copy(stage = UpdatePhase.Confirm, candidate = version, localFile = null, sourceError = null)
        }
    }

    fun onCancelConfirm() {
        core.updateIf({
            it.stage == UpdatePhase.Confirm
        }) { it.copy(stage = UpdatePhase.Source, candidate = null, localFile = null) }
    }

    /** «Обновить» на подтверждении: запуск процесса с выбранной версией. */
    fun onConfirmUpdate() {
        val current = core.value
        val source = current.localFile?.let { UpdateSource.File(it.path) }
            ?: current.candidate?.let { UpdateSource.Server(it) }
            ?: return
        if (core.updateIf({
                it.stage == UpdatePhase.Confirm
            }) { it.copy(stage = UpdatePhase.Prepare, linkDropped = false) }
        ) {
            updateRequests.tryEmit(source)
        }
    }

    /**
     * «OK» на успехе. Блок ещё в загрузчике — обычное состояние выбора источника (можно выбрать другую прошивку или
     * выйти командой); блок уже вернулся в рабочий режим — подтверждение, хост уйдёт со слота сам.
     */
    fun onResultOk() {
        if (state.value.phase != UpdatePhase.Success) return
        if (state.value.inBoot) {
            // Блок остался в загрузчике: ожидание возврата, поставленное на «Применении», больше не нужно.
            if (deviceSession.reboot.value == DeviceReboot.ToMain) deviceSession.cancelReboot()
            core.update {
                it.copy(
                    stage = UpdatePhase.Source,
                    candidate = null,
                    localFile = null,
                    sourceError = null,
                    exitError = false,
                )
            }
        } else {
            core.update { it.copy(acknowledged = true) }
            deviceSession.acknowledgeReboot()
        }
    }

    /**
     * «Выйти из обновления» (BLE): команда перезагрузки блока в рабочий режим. Ожидание возврата ставится до команды —
     * блок может перезагрузиться раньше ответа. Не прошла и блок на связи — баннер; связь оборвалась — это и есть
     * перезагрузка, ждём возврата.
     */
    fun onExitBootClick() {
        val current = core.value
        val profile = current.profile ?: return
        if (current.stage != UpdatePhase.Source || !profile.canExit) return
        val target = profile.target
        safeLaunch {
            executeWithProceedLock {
                deviceSession.expectReboot(DeviceReboot.ToMain)
                deviceSession.acknowledgeReboot()
                core.update { it.copy(acknowledged = true, exitError = false) }
                val response = withContext(dispatchers.io) { deviceSession.exitBootMode(target) }
                val stillInBoot = deviceSession.state.value.isBoot
                if (response is Response.Error && stillInBoot) {
                    logger.d { "Exit boot mode failed: ${response.exception::class.simpleName}" }
                    deviceSession.cancelReboot()
                    core.update { it.copy(acknowledged = false, exitError = true) }
                }
            }
        }
    }

    /** «Повторить»: после ошибки — снова к выбору источника; без данных блока — перечитать подсистемы. */
    fun onRetry() {
        val stage = core.value.stage
        when {
            stage is UpdatePhase.Error -> core.update {
                it.copy(stage = UpdatePhase.Source, candidate = null, localFile = null, sourceError = null)
            }

            stage == UpdatePhase.DataError -> {
                val session = deviceSession.state.value
                safeLaunch { executeWithProceedLock { loadProfile(session) } }
            }
        }
    }

    fun onSupportClick() {
        _effect.tryEmit(FirmwareUpdateEffect.OpenSupportChat)
    }

    fun onReconnect() {
        safeLaunch { withContext(dispatchers.io) { deviceSession.reconnectNow() } }
    }

    fun onDisconnect() {
        _effect.tryEmit(FirmwareUpdateEffect.Exit)
    }

    /** «Назад»: на процессе заблокирован; на выборе — предупреждение; после ошибки и обрыва — сразу выход. */
    fun onBackClick() {
        val current = state.value
        when {
            current.locked -> Unit

            current.phase is UpdatePhase.Error || current.phase is UpdatePhase.Lost ->
                _effect.tryEmit(FirmwareUpdateEffect.Exit)

            else -> core.update { it.copy(exitDialog = true) }
        }
    }

    fun onExitDialogDismiss() {
        core.update { it.copy(exitDialog = false) }
    }

    fun onExitConfirm() {
        core.update { it.copy(exitDialog = false) }
        _effect.tryEmit(FirmwareUpdateEffect.Exit)
    }

    // ───── процесс обновления ─────

    private suspend fun runUpdate(source: UpdateSource) {
        val target = core.value.profile?.target ?: return
        val progress = when (source) {
            is UpdateSource.Server -> updateRepository.update(target, source.version)
            is UpdateSource.File -> updateRepository.updateFromFile(target, source.path)
        }
        // SDK шлёт событие на каждый пакет передачи: маппинг и отсев повторов — в фоне (io), на Main приходит
        // только смена стадии или процента; переходы состояния (onProgress) — в коллекторе, на Main.
        core.update { it.copy(running = true) }
        try {
            progress
                .distinctUntilChanged()
                .flowOn(dispatchers.io)
                .collect { onProgress(it) }
        } finally {
            deviceSession.endFirmwareTransfer()
            core.update { it.copy(running = false) }
        }
    }

    private suspend fun onProgress(progress: FirmwareUpdateProgress) {
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

            is FirmwareUpdateProgress.Failed -> onFailed(progress.failure)
        }
    }

    private suspend fun onFailed(failure: FirmwareFailure) {
        // SDK сдался: продолжения передачи после переподключения больше не будет.
        core.update { it.copy(running = false) }
        // Неизвестный сбой при потере связи — это обрыв: экран покажет «Связь прервана», а после возврата блока
        // вернётся к выбору источника. Связь оценивается с запасом: поток SDK иногда падает раньше статуса связи.
        if (failure == FirmwareFailure.Unknown && linkWentDown()) {
            core.update { it.copy(linkDropped = true) }
            return
        }
        if (deviceSession.reboot.value == DeviceReboot.ToMain) deviceSession.cancelReboot()
        setStage(UpdatePhase.Error(failure.toKind()))
    }

    private suspend fun linkWentDown(): Boolean = withTimeoutOrNull(LINK_GRACE) {
        deviceSession.state.first { it.isLinkDown }
    } != null

    // Стадия меняется, только пока процесс идёт: запоздавшее событие отменённого процесса экран не трогает.
    private fun setStage(stage: UpdatePhase) {
        core.updateIf({ it.stage.isUpdating() || it.stage == UpdatePhase.Success }) { it.copy(stage = stage) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val LINK_GRACE = 3.seconds
    }
}
