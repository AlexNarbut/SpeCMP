package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.localization.AppLocale

/**
 * Настройки приложения. [current] — язык этого запуска, [selected] — выбранный в списке (применяется кнопкой
 * «Применить» после подтверждения: язык меняется только перезапуском).
 *
 * @property configurationLanguage язык конфигураций (применяется сразу); `null` — ещё читается или не прочитался.
 * @property configurationLanguageError смена языка конфигураций не сохранилась — прежний язык остался.
 */
@Immutable
internal data class AppSettingsState(
    val current: AppLocale,
    val selected: AppLocale = current,
    val dialog: AppSettingsDialog? = null,
    val configurationLanguage: ConfigurationLanguage? = null,
    val configurationLanguageError: Boolean = false
) {
    val canApply: Boolean get() = selected != current
}

internal enum class AppSettingsDialog {
    /** «Сменить язык? Приложение перезапустится». */
    CONFIRM,

    /** Платформа не перезапускается сама (iOS, desktop): язык применится после ручного перезапуска. */
    RESTART_MANUALLY
}
