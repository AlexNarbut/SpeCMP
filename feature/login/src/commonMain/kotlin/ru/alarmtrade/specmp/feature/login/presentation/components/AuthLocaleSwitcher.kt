package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_btn_language_en
import specmp.feature.login.generated.resources.login_common_btn_language_ru

@Composable
fun AuthLocaleSwitcher(
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
) {
    CmpTextButton(
        text = stringResource(Res.string.login_common_btn_language_en),
        onClick = { onLocaleChanged(AppLocale.English) },
        enabled = appLocale != AppLocale.English,
    )
    CmpTextButton(
        text = stringResource(Res.string.login_common_btn_language_ru),
        onClick = { onLocaleChanged(AppLocale.Russian) },
        enabled = appLocale != AppLocale.Russian,
    )
}
