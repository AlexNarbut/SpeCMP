package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.FileError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.deviceOrNull
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * Прошивка блока по COM-порту: «Обновить из файла» → разбор заголовка `.frm` → подтверждение с версиями блока и
 * файла (имя блока в файле другое — предупреждение, прошить можно) → GOBOOT (пользователь перезагружает блок по
 * питанию) → передача → CRC → применение → результат.
 *
 * Сессию порта держит [deviceSession] (её открыл поиск), связь и обрыв показывает хост устройства. Экран стоит и в
 * рабочем слоте хоста, и в слоте загрузчика: VM одна на запись навигации, поэтому смена режима блока экран не
 * сбрасывает. Порт при перезагрузке блока не отключается — после обновления режим перечитывается явно.
 *
 * Блок VM не копирует: [state] собирается из собственных фаз и `DeviceSession.state`, проверка файла читает блок
 * из сессии в момент проверки. Выбранный файл живёт в фазе [SerialPhase.Confirm] — отдельных полей нет.
 */
class SerialFirmwareViewModel(
    private val repository: SerialFirmwareRepository,
    private val deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : LegacyBaseViewModel() {

    // Фазы экрана и баннеры выбора файла; блок добавляет state из сессии.
    private val screen = MutableStateFlow(SerialFirmwareState())

    val state: StateFlow<SerialFirmwareState> = combine(screen, deviceSession.state) { own, session ->
        own.copy(device = session.deviceOrNull, deviceInBoot = session is DeviceSessionState.FirmwareBoot)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SerialFirmwareState())

    private val _effect = sideEffectFlow<SerialFirmwareEffect>()
    val effect: SharedFlow<SerialFirmwareEffect> = _effect.asSharedFlow()

    /** Результат выбора файла: `.frm` для этого блока — на подтверждение, иначе — баннер на шаге выбора. */
    fun onFilePicked(result: FirmwareFilePickResult) {
        if (screen.value.phase != SerialPhase.Ready) return
        when (result) {
            FirmwareFilePickResult.Cancelled -> Unit

            is FirmwareFilePickResult.Picked -> checkFile(result.name, result.path)

            FirmwareFilePickResult.TooLarge -> screen.updateIf({ it.phase == SerialPhase.Ready }) {
                it.copy(fileError = FileError.TooLarge, fileProblem = null)
            }

            FirmwareFilePickResult.Failed -> screen.updateIf({ it.phase == SerialPhase.Ready }) {
                it.copy(fileError = FileError.Failed, fileProblem = null)
            }
        }
    }

    private fun checkFile(fileName: String, path: String) {
        safeLaunch {
            val info = withContext(dispatchers.io) { repository.readFileInfo(path) }
            val current = deviceSession.state.value.deviceOrNull
            screen.updateIf({ it.phase == SerialPhase.Ready }) { state ->
                when {
                    info !is Response.Success -> state.copy(fileError = null, fileProblem = SerialFileProblem.NotFrm)

                    else -> {
                        val version = info.value.version
                        val older = version != null && current != null &&
                            version.relationTo(current.firmwareVersion) == VersionRelation.Older
                        // Блок ещё не прочитан — сверять не с чем.
                        val mismatch = current?.takeUnless { info.value.isFor(it.systemName) }?.let {
                            SystemNameMismatch(info.value.systemName, it.systemName.trim())
                        }
                        state.copy(
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
        screen.updateIf({ it.phase is SerialPhase.Confirm }) { it.copy(phase = SerialPhase.Ready) }
    }

    fun onConfirmUpdate() {
        val confirm = screen.value.phase as? SerialPhase.Confirm ?: return
        // Переход атомарный: двойное нажатие запустит обновление один раз.
        if (!screen.updateIf({ it.phase == confirm }) { it.copy(phase = SerialPhase.WaitingBoot) }) return
        safeLaunch {
            repository.update(confirm.fileName, confirm.path)
                .flowOn(dispatchers.io)
                .collect { progress -> screen.update { it.copy(phase = progress.toPhase(confirm.fileVersion)) } }
            // Блок мог перезагрузиться (в загрузчик и обратно) — пусть сессия узнает его режим.
            deviceSession.refreshMode()
        }
    }

    /** «OK» на результате — снова «Обновить из файла». */
    fun onResultOk() {
        screen.updateIf({ it.phase is SerialPhase.Success || it.phase is SerialPhase.Error }) {
            it.copy(phase = SerialPhase.Ready, fileError = null, fileProblem = null)
        }
    }

    fun onBack() {
        if (screen.value.locked) return
        _effect.tryEmit(SerialFirmwareEffect.Exit)
    }

    private fun SerialFirmwareProgress.toPhase(fileVersion: String?): SerialPhase = when (this) {
        SerialFirmwareProgress.WaitingForBoot -> SerialPhase.WaitingBoot
        SerialFirmwareProgress.CheckingBoot -> SerialPhase.CheckingBoot
        is SerialFirmwareProgress.Sending -> SerialPhase.Transfer(percent)
        SerialFirmwareProgress.Verifying -> SerialPhase.Verify
        SerialFirmwareProgress.Applying -> SerialPhase.Apply
        SerialFirmwareProgress.Finished -> SerialPhase.Success(fileVersion)
        is SerialFirmwareProgress.Failed -> SerialPhase.Error(failure)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
