package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource

/** Считывание со своей VM; «Назад» — как «Отмена»: считывание прерывается, паспорт не создаётся. */
@Composable
internal fun PassportReadPane(source: PassportReadSource, onDone: (passportId: String) -> Unit, onCancel: () -> Unit) {
    val viewModel = koinViewModel<PassportReadViewModel> { parametersOf(PassportReadArgs(source)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentDone by rememberUpdatedState(onDone)
    val currentCancel by rememberUpdatedState(onCancel)
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is PassportReadEffect.Done -> currentDone(effect.passportId)
            PassportReadEffect.Cancelled -> currentCancel()
        }
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = viewModel::onCancel,
    )
    val actions = remember(viewModel) {
        PassportReadActions(
            onCancel = viewModel::onCancel,
            onRetry = viewModel::onRetry,
            onContinueWithout = viewModel::onContinueWithout,
            onSkipSettings = viewModel::onSkipSettings,
        )
    }
    PassportReadScreen(state = state, actions = actions)
}
