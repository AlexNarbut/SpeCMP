package ru.alarmtrade.specmp.core.ui.startup

import ru.alarmtrade.specmp.core.common.AppServicesStarter
import ru.alarmtrade.specmp.core.common.AppStartup
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel

/**
 * Запуск приложения для корня (`App`, `FlasherApp`): process-wide сервисы и настройка SDK. Оба `start()`
 * идемпотентны — пересоздание VM (новая Activity) их не повторит.
 */
class AppStartViewModel(
    private val services: AppServicesStarter,
    private val startup: AppStartup,
    private val localeStore: AppLocaleStore,
) : BaseViewModel<AppStartState, Nothing>(AppStartState(isReady = startup.ready.value)) {

    override suspend fun IntentScope.onStart() {
        services.start()
        startup.start(localeStore.current().languageTag)
        startup.ready.collect { ready -> reduce { copy(isReady = ready) } }
    }
}
