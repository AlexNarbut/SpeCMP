package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.AppRestarter
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.common.ConfigurationLanguageStore
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel

/**
 * Настройки приложения.
 *
 * - Язык приложения сохраняется и применяется перезапуском (docs/localization.md, «Язык приложения»); где платформа
 *   сама не перезапускается — просим перезапустить вручную.
 * - Язык конфигураций — сразу: SDK подставляет сохранённый язык в каждый запрос каталога и конфигураций, поэтому
 *   экраны конфигуратора и быстрого монтажа, открытые после смены, получают конфигурации на новом языке.
 */
internal class AppSettingsViewModel(
    private val localeStore: AppLocaleStore,
    private val restarter: AppRestarter,
    private val configurationLanguages: ConfigurationLanguageStore,
    private val dispatchers: AppDispatchers,
) : LegacyBaseViewModel() {

    private val _state = MutableStateFlow(AppSettingsState(current = localeStore.current()))
    val state: StateFlow<AppSettingsState> = _state
        .launchOnFirstStart { loadConfigurationLanguage() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), _state.value)

    private suspend fun loadConfigurationLanguage() {
        val language = withContext(dispatchers.io) { configurationLanguages.current() }
        _state.update { it.copy(configurationLanguage = language) }
    }

    fun onLocaleSelect(locale: AppLocale) = _state.update { it.copy(selected = locale) }

    fun onApply() = _state.update { if (it.canApply) it.copy(dialog = AppSettingsDialog.CONFIRM) else it }

    fun onConfirm() {
        val locale = _state.value.selected
        localeStore.save(locale)
        // Android перезапускает процесс (сюда не вернёмся); iOS/desktop — no-op, язык применится со следующего запуска.
        if (!restarter.restart()) {
            _state.update { it.copy(current = locale, dialog = AppSettingsDialog.RESTART_MANUALLY) }
        }
    }

    fun onDialogDismiss() = _state.update { it.copy(dialog = null) }

    /** Выбран язык конфигураций: сразу на экране, не сохранился — откат к прежнему с сообщением. */
    fun onConfigurationLanguageSelect(language: ConfigurationLanguage) {
        safeLaunch { executeWithProceedLock(needSkipAnother = false) { saveConfigurationLanguage(language) } }
    }

    private suspend fun saveConfigurationLanguage(language: ConfigurationLanguage) {
        val previous = _state.value.configurationLanguage
        if (language == previous) return
        _state.update { it.copy(configurationLanguage = language, configurationLanguageError = false) }
        val response = withContext(dispatchers.io) { configurationLanguages.save(language) }
        if (response is Response.Error) {
            logger.w(response.exception) { "Failed to save the configuration language" }
            _state.update { it.copy(configurationLanguage = previous, configurationLanguageError = true) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
