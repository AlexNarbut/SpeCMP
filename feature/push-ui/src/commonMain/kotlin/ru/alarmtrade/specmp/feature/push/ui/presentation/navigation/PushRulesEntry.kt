package ru.alarmtrade.specmp.feature.push.ui.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.push_rules_error_save
import ru.alarmtrade.specmp.core.localization.resources.push_rules_msg_saved
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.push.ui.presentation.model.messageText
import ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules.PushRulesActions
import ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules.PushRulesEffect
import ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules.PushRulesScreen
import ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules.PushRulesViewModel

@Composable
fun PushRulesEntry(onBack: () -> Unit) {
    val viewModel = koinViewModel<PushRulesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        scope.launch {
            snackbarHostState.showSnackbar(
                when (effect) {
                    PushRulesEffect.Saved -> getString(Strings.string.push_rules_msg_saved)
                    is PushRulesEffect.SaveFailed -> effect.error.messageText(Strings.string.push_rules_error_save)
                },
            )
        }
    }
    val actions = remember(viewModel, onBack) {
        PushRulesActions(
            onBack = onBack,
            onToggle = viewModel::onToggle,
            onSave = viewModel::onSave,
            onRetry = viewModel::onRetry,
        )
    }
    PushRulesScreen(state, actions, snackbarHostState)
}
