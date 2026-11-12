package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel

internal class AppSettingsViewModel(
    private val localeStore: AppLocaleStore,
    private val configurationLanguages: ConfigurationLanguageStore,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<AppSettingsState, Nothing>(AppSettingsState(current = localeStore.current())) {

    override suspend fun onStart() {
        val language = withContext(dispatchers.io) { configurationLanguages.current() }
        reduce { copy(configurationLanguage = language) }
    }

    fun onLocaleSelect(locale: AppLocale) {
        reduce { copy(selected = locale) }
    }

    fun onApply() {
        reduce { if (canApply) copy(dialog = AppSettingsDialog.CONFIRM) else this }
    }

    fun onConfirm() {
        val locale = currentState.selected
        intent {
            withContext(dispatchers.io) { localeStore.save(locale) }
            reduce { copy(current = locale, dialog = AppSettingsDialog.RESTART_MANUALLY) }
        }
    }

    fun onDialogDismiss() {
        reduce { copy(dialog = null) }
    }

    /** Выбран язык конфигураций: сразу на экране, не сохранился — откат к прежнему с сообщением. */
    fun onConfigurationLanguageSelect(language: ConfigurationLanguage) {
        intent { executeWithProceedLock(needSkipAnother = false) { saveConfigurationLanguage(language) } }
    }

    private suspend fun saveConfigurationLanguage(language: ConfigurationLanguage) {
        val previous = currentState.configurationLanguage
        if (language == previous) return
        reduce { copy(configurationLanguage = language, configurationLanguageError = false) }
        val response = withContext(dispatchers.io) { configurationLanguages.save(language) }
        if (response is Response.Error) {
            logger.w(response.exception) { "Failed to save the configuration language" }
            reduce { copy(configurationLanguage = previous, configurationLanguageError = true) }
        }
    }
}
