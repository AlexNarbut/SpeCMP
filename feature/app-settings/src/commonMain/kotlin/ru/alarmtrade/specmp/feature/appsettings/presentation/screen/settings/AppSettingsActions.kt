package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.localization.AppLocale

internal class AppSettingsActions(
    val onBack: () -> Unit,
    val onLocaleSelect: (AppLocale) -> Unit,
    val onApply: () -> Unit,
    val onConfirm: () -> Unit,
    val onDialogDismiss: () -> Unit,
    val onConfigurationLanguageSelect: (ConfigurationLanguage) -> Unit,
)
