package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.localization.AppLocale

/**
 * [current] — язык этого запуска, [selected] — выбранный в списке (применяется перезапуском).
 * [configurationLanguage] `null` — ещё читается или не прочитался.
 */
@Immutable
internal data class AppSettingsState(
    val current: AppLocale,
    val selected: AppLocale = current,
    val dialog: AppSettingsDialog? = null,
    val configurationLanguage: ConfigurationLanguage? = null,
    val configurationLanguageError: Boolean = false,
) {
    val canApply: Boolean get() = selected != current
}

internal enum class AppSettingsDialog {
    CONFIRM,

    /** Язык сохранён и применится, когда пользователь сам перезапустит приложение. */
    RESTART_MANUALLY,
}
