package ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ConfigurationLanguage
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_apply
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_change
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_btn_push_rules
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_error_conf_language
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_hint_conf_language
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_label_conf_language
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_label_language
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_label_push
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_msg_confirm
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_msg_restart
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_title
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_title_confirm
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_title_restart
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_value_language_en
import ru.alarmtrade.specmp.core.localization.resources.appsettings_main_value_language_ru
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val ContentMaxWidth = 720.dp

@Composable
internal fun AppSettingsScreen(state: AppSettingsState, actions: AppSettingsActions) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.appsettings_main_title),
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.appsettings_main_a11y_back),
                    )
                },
            )
        },
    ) { padding ->
        val direction = LocalLayoutDirection.current
        CmpAdaptiveBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(direction),
                    end = padding.calculateEndPadding(direction),
                ),
        ) { window ->
            val horizontal = if (window.isCompact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                SettingsContent(
                    state = state,
                    actions = actions,
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .padding(
                            start = horizontal,
                            end = horizontal,
                            top = padding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
                            bottom = padding.calculateBottomPadding() + CmpSpacing.screenPaddingBottom,
                        ),
                )
            }
        }
    }
    AppSettingsDialogs(state.dialog, actions)
}

@Composable
private fun SettingsContent(state: AppSettingsState, actions: AppSettingsActions, modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        SettingsItems(state, actions)
    }
}

@Composable
private fun ColumnScope.SettingsItems(state: AppSettingsState, actions: AppSettingsActions) {
    SectionTitle(stringResource(Strings.string.appsettings_main_label_language))
    AppLocale.entries.forEach { locale ->
        LanguageRow(
            name = localeName(locale),
            selected = locale == state.selected,
            onClick = { actions.onLocaleSelect(locale) },
        )
    }
    CmpButton(
        text = stringResource(Strings.string.appsettings_main_btn_apply),
        onClick = actions.onApply,
        enabled = state.canApply,
        modifier = Modifier.padding(top = CmpSpacing.space8),
        fullWidth = true,
    )
    CmpDivider(modifier = Modifier.padding(vertical = CmpSpacing.space16))
    ConfigurationLanguageSection(state, actions.onConfigurationLanguageSelect)
    CmpDivider(modifier = Modifier.padding(vertical = CmpSpacing.space16))
    SectionTitle(stringResource(Strings.string.appsettings_main_label_push))
    CmpButton(
        text = stringResource(Strings.string.appsettings_main_btn_push_rules),
        onClick = actions.onPushRules,
        modifier = Modifier.testTag("appsettings_push_rules"),
        type = CmpButtonType.Secondary,
        fullWidth = true,
    )
}

@Composable
private fun SectionTitle(text: String) {
    CmpText(text = text, style = CmpTextStyle.Headline1, color = CmpTheme.colors.foregroundOnDarkPrimaryDefault)
}

@Composable
private fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CmpSize.buttonL)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpRadioButton(selected = selected, onClick = null)
        CmpText(text = name, style = CmpTextStyle.Paragraph1)
    }
}

@Composable
private fun ConfigurationLanguageSection(state: AppSettingsState, onSelect: (ConfigurationLanguage) -> Unit) {
    SectionTitle(stringResource(Strings.string.appsettings_main_label_conf_language))
    CmpText(
        text = stringResource(Strings.string.appsettings_main_hint_conf_language),
        style = CmpTextStyle.Paragraph2,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
    )
    ConfigurationLanguage.entries.forEach { language ->
        LanguageRow(
            name = languageName(language),
            selected = language == state.configurationLanguage,
            onClick = { onSelect(language) },
            modifier = Modifier.testTag("appsettings_conf_language_${language.name.lowercase()}"),
        )
    }
    if (state.configurationLanguageError) {
        CmpText(
            text = stringResource(Strings.string.appsettings_main_error_conf_language),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.errorDefault,
        )
    }
}

/** Название языка — на нём самом (одинаково в любой локали). */
@Composable
private fun localeName(locale: AppLocale): String = stringResource(
    when (locale) {
        AppLocale.English -> Strings.string.appsettings_main_value_language_en
        AppLocale.Russian -> Strings.string.appsettings_main_value_language_ru
    },
)

@Composable
private fun languageName(language: ConfigurationLanguage): String = stringResource(
    when (language) {
        ConfigurationLanguage.EN -> Strings.string.appsettings_main_value_language_en
        ConfigurationLanguage.RU -> Strings.string.appsettings_main_value_language_ru
    },
)

@Composable
private fun AppSettingsDialogs(dialog: AppSettingsDialog?, actions: AppSettingsActions) {
    when (dialog) {
        null -> Unit

        AppSettingsDialog.CONFIRM -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            title = {
                CmpText(
                    text = stringResource(Strings.string.appsettings_main_title_confirm),
                    style = CmpTextStyle.Headline1,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.appsettings_main_msg_confirm),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.appsettings_main_btn_change),
                    onClick = actions.onConfirm,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
            },
            dismissButton = {
                CmpButton(
                    text = stringResource(Strings.string.appsettings_main_btn_cancel),
                    onClick = actions.onDialogDismiss,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
            },
        )

        AppSettingsDialog.RESTART_MANUALLY -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            title = {
                CmpText(
                    text = stringResource(Strings.string.appsettings_main_title_restart),
                    style = CmpTextStyle.Headline1,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.appsettings_main_msg_restart),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.appsettings_main_btn_ok),
                    onClick = actions.onDialogDismiss,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                )
            },
        )
    }
}
