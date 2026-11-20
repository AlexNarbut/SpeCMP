package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadCommand
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadPhase
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportInstallerRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportReadingRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/**
 * Считывание блока по этапам: ход и остановки — из SDK, команды остановок ([PassportReadCommand]) уходят в SDK.
 * «Отмена» — отмена сбора, паспорт не создаётся. После сохранения — пауза [OPEN_DELAY_MS], чтобы было видно «Всё
 * прочитано», затем предотчёт.
 */
internal class PassportReadViewModel(
    args: PassportReadArgs,
    private val reading: PassportReadingRepository,
    private val installers: PassportInstallerRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<PassportReadingState, PassportReadEffect>(PassportReadingState(args.source)) {

    override suspend fun IntentScope.onStart() {
        start()
    }

    fun onRetry() {
        if (currentState.failure != null) start() else send(PassportReadCommand.RETRY)
    }

    fun onContinueWithout() {
        send(PassportReadCommand.CONTINUE_WITHOUT)
    }

    fun onSkipSettings() {
        send(PassportReadCommand.SKIP)
    }

    fun onCancel() {
        intent {
            cancelLaunched(READ)
            cancelLaunched(TIMER)
            cancelLaunched(OPEN)
            postSideEffect(PassportReadEffect.Cancelled)
        }
    }

    private fun start() {
        launchReplacing(READ) {
            cancelLaunched(TIMER)
            reduce { copy(read = PassportReadState.Initial, failure = null, settingsSeconds = 0) }
            val installer = when (val profile = withContext(dispatchers.io) { installers.installer() }) {
                is Response.Success -> profile.value
                is Response.Error -> PassportInstaller()
            }
            reading.read(currentState.source, installer).collect { response ->
                when (response) {
                    is Response.Success -> {
                        val previous = currentState.read
                        reduce { copy(read = response.value) }
                        onProgress(previous, response.value)
                    }

                    is Response.Error -> {
                        cancelLaunched(TIMER)
                        reduce { copy(failure = PassportError.from(response.exception)) }
                    }
                }
            }
        }
    }

    private fun onProgress(previous: PassportReadState, read: PassportReadState) {
        val settingsBefore = previous.status(PassportReadStep.SETTINGS) == PassportStepStatus.Reading
        val settingsNow = read.status(PassportReadStep.SETTINGS) == PassportStepStatus.Reading
        when {
            settingsNow && !settingsBefore -> startTimer()
            !settingsNow && settingsBefore -> cancelLaunched(TIMER)
        }
        val done = read.phase as? PassportReadPhase.Done
        if (done != null && previous.phase !is PassportReadPhase.Done) {
            launchReplacing(OPEN) {
                delay(OPEN_DELAY_MS)
                postSideEffect(PassportReadEffect.Done(done.passportId))
            }
        }
    }

    private fun startTimer() {
        launchReplacing(TIMER) {
            reduce { copy(settingsSeconds = 0) }
            repeat(MAX_TIMER_SECONDS) {
                delay(SECOND_MS)
                reduce { copy(settingsSeconds = settingsSeconds + 1) }
            }
        }
    }

    private fun send(command: PassportReadCommand) {
        intent {
            withContext(dispatchers.io) { reading.send(command) }
                .onError { logger.w(it) { "Read command $command failed" } }
        }
    }

    private companion object {
        const val READ = "read"
        const val TIMER = "timer"
        const val OPEN = "open"
        const val OPEN_DELAY_MS = 900L
        const val SECOND_MS = 1_000L
        const val MAX_TIMER_SECONDS = 3_600
    }
}
