package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_apply
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_change
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_label_language
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_msg_confirm
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_msg_restart
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_title
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_title_confirm
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_title_restart
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_value_language_en
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_value_language_ru
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
internal fun AppSettingsScreen(state: AppSettingsState, actions: AppSettingsActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.appsettings_main_title)) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.appsettings_main_a11y_back)
                    )
                }
            )
        }
    ) { padding ->
        CmpColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CmpText(
                text = stringResource(Strings.string.appsettings_main_label_language),
                style = CmpTextStyle.Title,
                color = cmpThemeColors().onSurfaceVariant
            )
            AppLocale.entries.forEach { locale ->
                LocaleRow(locale = locale, selected = locale == state.selected, onClick = {
                    actions.onLocaleSelect(locale)
                })
            }
            CmpButton(
                text = stringResource(Strings.string.appsettings_main_btn_apply),
                onClick = actions.onApply,
                enabled = state.canApply,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
    AppSettingsDialogs(state.dialog, actions)
}

@Composable
private fun LocaleRow(locale: AppLocale, selected: Boolean, onClick: () -> Unit) {
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpRadioButton(selected = selected, onClick = null)
        CmpText(text = localeName(locale), style = CmpTextStyle.BodyLarge)
    }
}

/** Название языка — на нём самом (одинаково в любой локали). */
@Composable
private fun localeName(locale: AppLocale): String = stringResource(
    when (locale) {
        AppLocale.English -> Strings.string.appsettings_main_value_language_en
        AppLocale.Russian -> Strings.string.appsettings_main_value_language_ru
    }
)

@Composable
private fun AppSettingsDialogs(dialog: AppSettingsDialog?, actions: AppSettingsActions) {
    when (dialog) {
        null -> Unit

        AppSettingsDialog.CONFIRM -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            title = { CmpText(text = stringResource(Strings.string.appsettings_main_title_confirm)) },
            text = { CmpText(text = stringResource(Strings.string.appsettings_main_msg_confirm)) },
            confirmButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.appsettings_main_btn_change),
                    onClick = actions.onConfirm
                )
            },
            dismissButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.appsettings_main_btn_cancel),
                    onClick = actions.onDialogDismiss
                )
            }
        )

        AppSettingsDialog.RESTART_MANUALLY -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            title = { CmpText(text = stringResource(Strings.string.appsettings_main_title_restart)) },
            text = { CmpText(text = stringResource(Strings.string.appsettings_main_msg_restart)) },
            confirmButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.appsettings_main_btn_ok),
                    onClick = actions.onDialogDismiss
                )
            }
        )
    }
}
