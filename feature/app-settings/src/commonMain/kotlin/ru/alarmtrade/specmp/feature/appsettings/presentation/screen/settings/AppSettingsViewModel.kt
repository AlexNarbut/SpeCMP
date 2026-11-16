package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.push.domain.usecase.RegisterPushTokenUseCase

internal class AppSettingsViewModel(
    private val localeStore: AppLocaleStore,
    private val configurationLanguages: ConfigurationLanguageStore,
    private val registerPushToken: RegisterPushTokenUseCase,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<AppSettingsState, Nothing>(AppSettingsState(current = localeStore.current())) {

    override suspend fun IntentScope.onStart() {
        val language = withContext(dispatchers.io) { configurationLanguages.current() }
        reduce { copy(configurationLanguage = language) }
    }

    fun onLocaleSelect(locale: AppLocale) {
        intent { reduce { copy(selected = locale) } }
    }

    fun onApply() {
        intent { reduce { if (canApply) copy(dialog = AppSettingsDialog.CONFIRM) else this } }
    }

    fun onConfirm() {
        val locale = currentState.selected
        intent {
            withContext(dispatchers.io) { localeStore.save(locale) }
            reduce { copy(current = locale, dialog = AppSettingsDialog.RESTART_MANUALLY) }
            // Тексты push сервер шлёт на языке токена: сообщаем новый язык сразу, не дожидаясь перезапуска.
            withContext(dispatchers.io) { registerPushToken() }
                .onError { logger.w(it) { "Push language is not updated" } }
        }
    }

    fun onDialogDismiss() {
        intent { reduce { copy(dialog = null) } }
    }

    /** Выбран язык конфигураций: сразу на экране, не сохранился — откат к прежнему с сообщением. */
    fun onConfigurationLanguageSelect(language: ConfigurationLanguage) {
        intent { executeWithProceedLock(needSkipAnother = false) { saveConfigurationLanguage(language) } }
    }

    private suspend fun saveConfigurationLanguage(language: ConfigurationLanguage) = subIntent {
        val previous = currentState.configurationLanguage
        if (language == previous) return@subIntent
        reduce { copy(configurationLanguage = language, configurationLanguageError = false) }
        val response = withContext(dispatchers.io) { configurationLanguages.save(language) }
        if (response is Response.Error) {
            logger.w(response.exception) { "Failed to save the configuration language" }
            reduce { copy(configurationLanguage = previous, configurationLanguageError = true) }
        }
    }
}
