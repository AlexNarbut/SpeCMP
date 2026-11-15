package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.push.domain.model.PushRules
import ru.alarmtrade.specmp.feature.push.ui.presentation.model.message

@Composable
internal fun PushRulesScreen(
    state: PushRulesState,
    actions: PushRulesActions,
    snackbarHostState: CmpSnackbarHostState,
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.push_rules_title)) },
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
                RulesContent(selected, state, actions, modifier)
            }
        }
    }
}

@Composable
private fun RulesContent(
    selected: PushRules,
    state: PushRulesState,
    actions: PushRulesActions,
    modifier: Modifier = Modifier,
) {
    CmpColumn(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpText(
            text = stringResource(Strings.string.push_rules_msg_hint),
            style = CmpTextStyle.Body,
            color = cmpThemeColors().onSurfaceVariant,
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
            onClick = actions.onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("push_rules_save"),
        ) {
            if (state.isSaving) {
                CmpCircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                CmpText(text = stringResource(Strings.string.push_rules_btn_save))
            }
        }
    }
}

@Composable
private fun TypeRow(type: PushType, checked: Boolean, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onToggle)
            .testTag("push_rules_type_${type.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CmpIcon(icon = type.icon(), contentDescription = null, tint = cmpThemeColors().onSurfaceVariant)
        CmpText(text = type.title(), style = CmpTextStyle.BodyLarge, modifier = Modifier.weight(1f))
        CmpCheckbox(checked = checked, onCheckedChange = null, enabled = enabled)
    }
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
