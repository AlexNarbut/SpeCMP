package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.push_rules_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.push_rules_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.push_rules_btn_save
import ru.alarmtrade.specmp.core.localization.resources.push_rules_error_load
import ru.alarmtrade.specmp.core.localization.resources.push_rules_msg_hint
import ru.alarmtrade.specmp.core.localization.resources.push_rules_title
import ru.alarmtrade.specmp.core.localization.resources.push_rules_value_type_chat
import ru.alarmtrade.specmp.core.localization.resources.push_rules_value_type_notification
import ru.alarmtrade.specmp.core.localization.resources.push_rules_value_type_warning
import ru.alarmtrade.specmp.core.push.PushType
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules
import ru.alarmtrade.specmp.feature.push.ui.presentation.model.message

private val ContentMaxWidth = 640.dp

@Composable
internal fun PushRulesScreen(
    state: PushRulesState,
    actions: PushRulesActions,
    snackbarHostState: CmpSnackbarHostState,
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.push_rules_title),
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.push_rules_a11y_back),
                    )
                },
            )
        },
        snackbarHost = { CmpSnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.fillMaxSize().padding(padding)
        when (val saved = state.saved) {
            LCEState.None, LCEState.Loading -> CmpBox(modifier, contentAlignment = Alignment.Center) {
                CmpCircularProgressIndicator()
            }

            is LCEState.Error -> CmpStateBox(
                icon = CmpIcons.CloudOff,
                title = saved.error.message(Strings.string.push_rules_error_load),
                tone = CmpStateTone.Error,
                modifier = modifier,
            ) {
                CmpButton(text = stringResource(Strings.string.push_rules_btn_retry), onClick = actions.onRetry)
            }

            is LCEState.Content -> state.selected?.let { selected ->
                RulesContent(selected, state, actions, padding)
            }
        }
    }
}

@Composable
private fun RulesContent(
    selected: PushRules,
    state: PushRulesState,
    actions: PushRulesActions,
    padding: PaddingValues,
) {
    // Отступ топбара — внутри прокрутки: контент уезжает под прозрачный топбар (блюр CmpScaffold).
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        CmpColumn(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(
                    start = CmpSpacing.screenPaddingH,
                    end = CmpSpacing.screenPaddingH,
                    top = padding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
                    bottom = padding.calculateBottomPadding() + CmpSpacing.screenPaddingBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpText(
                text = stringResource(Strings.string.push_rules_msg_hint),
                style = CmpTextStyle.Paragraph2,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.padding(bottom = CmpSpacing.space8),
            )
            PushType.entries.forEach { type ->
                TypeRow(
                    type = type,
                    checked = selected.isEnabled(type),
                    enabled = !state.isSaving,
                    onToggle = { actions.onToggle(type, it) },
                )
            }
            CmpButton(
                text = stringResource(Strings.string.push_rules_btn_save),
                onClick = actions.onSave,
                enabled = state.canSave,
                loading = state.isSaving,
                fullWidth = true,
                modifier = Modifier.padding(top = CmpSpacing.space16).testTag("push_rules_save"),
            )
        }
    }
}

@Composable
private fun TypeRow(type: PushType, checked: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    CmpListItem(
        title = type.title(),
        icon = type.icon(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CmpRadius.listItem))
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onToggle)
            .testTag("push_rules_type_${type.name.lowercase()}"),
        trailing = { CmpCheckbox(checked = checked, onCheckedChange = null, enabled = enabled) },
    )
}

private fun PushType.icon(): CmpIcons = when (this) {
    PushType.NOTIFICATION -> CmpIcons.Notifications
    PushType.WARNING -> CmpIcons.Warning
    PushType.CHAT -> CmpIcons.SupportAgent
}

@Composable
private fun PushType.title(): String = stringResource(
    when (this) {
        PushType.NOTIFICATION -> Strings.string.push_rules_value_type_notification
        PushType.WARNING -> Strings.string.push_rules_value_type_warning
        PushType.CHAT -> Strings.string.push_rules_value_type_chat
    },
)
