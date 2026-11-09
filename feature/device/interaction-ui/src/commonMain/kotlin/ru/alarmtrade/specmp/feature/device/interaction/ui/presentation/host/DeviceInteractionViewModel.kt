package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * VM обёртки flow работы с устройством: состояние сессии + последний блок в рабочем режиме
 * (по нему остаётся в композиции дочерний flow, пока связь потеряна; загрузчик его сбрасывает), признак
 * «был загрузчик», ожидаемая перезагрузка и выход из flow.
 */
class DeviceInteractionViewModel(private val deviceSession: DeviceSession) :
    BaseViewModel<DeviceInteractionState, DeviceInteractionEffect>(
        DeviceInteractionState()
            .withSession(deviceSession.state.value)
            .copy(transport = deviceSession.transport.value, reboot = deviceSession.reboot.value),
    ) {

    // Подписки живут всю жизнь VM, а не подписки экрана: «последний блок» и «был загрузчик» копятся по всем
    // состояниям сессии, пропуск состояния в фоне сбил бы их.
    override suspend fun onStart() {
        safeLaunch { deviceSession.state.collect { session -> reduce { withSession(session) } } }
        safeLaunch { deviceSession.transport.collect { transport -> reduce { copy(transport = transport) } } }
        safeLaunch { deviceSession.reboot.collect { reboot -> reduce { copy(reboot = reboot) } } }
    }

    /**
     * Выход из всего flow: закрыть сессию и сразу уйти. Закрытие идёт в скоупе сессии и не ждётся здесь —
     * следующее подключение (экран поиска) само дождётся его. Повторное нажатие не даёт второго Exit.
     */
    fun onExit() {
        if (currentState.exitRequested) return
        reduce { copy(exitRequested = true) }
        deviceSession.close()
        postSideEffect(DeviceInteractionEffect.Exit)
    }

    override fun onCleared() {
        // Запись могла уйти не через onExit (например, сброс навигации при выходе из аккаунта) — закрываем
        // сессию всегда; повторное закрытие безопасно и сериализовано внутри сессии.
        deviceSession.close()
        super.onCleared()
    }
}
