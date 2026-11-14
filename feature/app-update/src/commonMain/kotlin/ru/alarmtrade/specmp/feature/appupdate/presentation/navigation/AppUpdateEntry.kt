@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.appupdate.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_export_msg_done
import ru.alarmtrade.specmp.core.localization.resources.appupdate_export_msg_failed
import ru.alarmtrade.specmp.core.localization.resources.appupdate_store_msg_failed
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateActions
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateArgs
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateEffect
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateScreen
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateViewModel

@Serializable
@SerialName("app-update.AppUpdateEntryPoint")
enum class AppUpdateEntryPoint {
    /** Меню: проверка заново, без «Позже». */
    MENU,

    /** Плашка, блок бокового меню, уведомление, значок «Нужно обновить»: то, что уже известно, и «Позже». */
    PROMPT,
}

/** [install] — сразу установить скачанное (кнопка «Установить» в боковом меню главного). */
@Serializable
@SerialName("app-update.AppUpdateRoute")
data class AppUpdateRoute(val from: AppUpdateEntryPoint = AppUpdateEntryPoint.MENU, val install: Boolean = false) :
    NavKey

@Composable
fun AppUpdateEntry(onBack: () -> Unit, from: AppUpdateEntryPoint, install: Boolean) {
    val viewModel = koinViewModel<AppUpdateViewModel> { parametersOf(AppUpdateArgs(from, install)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val currentOnBack by rememberUpdatedState(onBack)

    val actions = remember(viewModel) {
        AppUpdateActions(
            onBack = { currentOnBack() },
            onRetry = viewModel::onRetry,
            onUpdate = viewModel::onUpdate,
            onSave = viewModel::onSave,
            onLater = viewModel::onLater,
            onCancel = viewModel::onCancel,
            onInstall = viewModel::onInstall,
            onToggleNotes = viewModel::onToggleNotes,
            onSelectChannel = viewModel::onSelectChannel,
            onConfirmDialog = viewModel::onConfirmDialog,
            onDismissDialog = viewModel::onDismissDialog,
        )
    }
    val showMessage: (StringResource) -> Unit = { message ->
        scope.launch { snackbar.showSnackbar(getString(message)) }
    }
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is AppUpdateEffect.OpenStorePage -> {
                val opened = effect.urls.any { url -> safeRun { uriHandler.openUri(url) } is Response.Success }
                if (!opened) showMessage(Strings.string.appupdate_store_msg_failed)
            }

            AppUpdateEffect.Saved -> showMessage(Strings.string.appupdate_export_msg_done)

            AppUpdateEffect.SaveFailed -> showMessage(Strings.string.appupdate_export_msg_failed)

            AppUpdateEffect.Close -> currentOnBack()
        }
    }

    AppUpdateScreen(state = state, actions = actions, snackbar = snackbar)
}
