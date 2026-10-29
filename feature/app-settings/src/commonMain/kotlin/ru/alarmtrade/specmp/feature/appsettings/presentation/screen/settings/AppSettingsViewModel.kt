package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AppRestarter
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel

/**
 * Настройки приложения: язык. Новый язык сохраняется и применяется перезапуском (docs/localization.md, «Язык
 * приложения»); где платформа сама не перезапускается — просим перезапустить вручную.
 */
internal class AppSettingsViewModel(private val localeStore: AppLocaleStore, private val restarter: AppRestarter) :
    BaseViewModel() {

    private val _state = MutableStateFlow(AppSettingsState(current = localeStore.current()))
    val state: StateFlow<AppSettingsState> = _state.asStateFlow()

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
}
