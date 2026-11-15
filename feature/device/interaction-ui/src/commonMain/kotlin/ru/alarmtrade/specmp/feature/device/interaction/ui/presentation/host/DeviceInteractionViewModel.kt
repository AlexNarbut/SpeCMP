package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

class DeviceInteractionViewModel(private val deviceSession: DeviceSession) :
    BaseViewModel<DeviceInteractionState, DeviceInteractionEffect>(
        DeviceInteractionState()
            .withSession(deviceSession.state.value)
            .copy(transport = deviceSession.transport.value, reboot = deviceSession.reboot.value),
    ) {

    // Подписки живут всю жизнь VM, а не подписки экрана: «последний блок» и «был загрузчик» копятся по всем
    // состояниям сессии, пропуск состояния в фоне сбил бы их.
    override suspend fun IntentScope.onStart() {
        intent { deviceSession.state.collect { session -> reduce { withSession(session) } } }
        intent { deviceSession.transport.collect { transport -> reduce { copy(transport = transport) } } }
        intent { deviceSession.reboot.collect { reboot -> reduce { copy(reboot = reboot) } } }
    }

    /** Закрытие идёт в скоупе сессии и не ждётся: следующее подключение (экран поиска) само его дождётся. */
    fun onExit() {
        intent {
            if (currentState.exitRequested) return@intent
            reduce { copy(exitRequested = true) }
            deviceSession.close()
            postSideEffect(DeviceInteractionEffect.Exit)
        }
    }

    override fun onCleared() {
        // Запись могла уйти не через onExit (например, сброс навигации при выходе из аккаунта) — закрываем
        // сессию всегда; повторное закрытие безопасно и сериализовано внутри сессии.
        deviceSession.close()
        super.onCleared()
    }
}
