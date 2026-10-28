package ru.alarmtrade.specmp.feature.authstate.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.navigation.ViewModelScope
import ru.alarmtrade.specmp.feature.authstate.domain.model.AuthState
import ru.alarmtrade.specmp.feature.authstate.presentation.logic.AuthStateViewModel

@Composable
fun AuthStateNavGraph(
    waitingState: @Composable () -> Unit,
    noAuthState: @Composable () -> Unit,
    authedState: @Composable () -> Unit,
    errorState: @Composable (onRetry: () -> Unit) -> Unit
) {
    val viewModel = koinViewModel<AuthStateViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = state.value,
        transitionSpec = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION_MILLIS))
                .togetherWith(fadeOut(animationSpec = tween(TRANSITION_DURATION_MILLIS)))
        },
        label = ANIMATION_LABEL
    ) { current ->
        when (current.state) {
            AuthState.WAITING -> waitingState()

            // Зона входа — тоже на сессию: формы прошлого входа (регистрация, сброс пароля) не достаются следующему.
            AuthState.NO_AUTH -> ViewModelScope(name = NO_AUTH_SCOPE, scopeKey = current.session) { noAuthState() }

            // Сессия — из target анимации: уходящая при выходе зона дорисовывается со своим хранилищем VM.
            AuthState.AUTHED -> ViewModelScope(name = AUTHED_SCOPE, scopeKey = current.session) { authedState() }

            AuthState.CHECK_FAILED -> errorState(viewModel::retry)
        }
    }
}

private const val TRANSITION_DURATION_MILLIS = 220
private const val ANIMATION_LABEL = "AuthStateNavGraph"
private const val NO_AUTH_SCOPE = "auth.no-auth"
private const val AUTHED_SCOPE = "auth.authed"
