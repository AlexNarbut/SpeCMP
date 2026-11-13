package ru.alarmtrade.specmp.feature.mountassistant.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_common_title
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_msg_pin_dialog
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.FeedCallbacks
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockTile
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.AssistantBody
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.AssistantScreenActions
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.MountAssistantEffect
import ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant.MountAssistantViewModel

/** Каркас (фон, top bar) — внутри записи навигации: ассистент — раздел графа расширенного монтажа. */
@Composable
fun MountAssistantEntry(onBack: () -> Unit, onNavigate: (AssistantNavigationTarget) -> Unit) {
    val viewModel = koinViewModel<MountAssistantViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is MountAssistantEffect.Navigate -> onNavigate(effect.target)
            MountAssistantEffect.FocusSearch -> focusRequester.requestFocus()
        }
    }
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenResumed()
        onPauseOrDispose {}
    }
    val actions = remember(viewModel) {
        AssistantScreenActions(
            onTile = { tile ->
                when (tile) {
                    is DockTile.Hero -> viewModel.onActionClick(tile.step.action)
                    is DockTile.Topic -> viewModel.onTopicClick(tile.topic)
                    is DockTile.Action -> viewModel.onActionClick(tile.action)
                    is DockTile.Setting -> viewModel.onSettingPick(tile.setting.id)
                    is DockTile.SearchAll -> viewModel.onSearchSubmit(topic = null)
                }
            },
            onTopicBack = viewModel::onTopicBack,
            onQueryChange = viewModel::onQueryChange,
            onSubmit = viewModel::onSearchSubmit,
            onDockToggle = viewModel::onDockToggle,
            onFeedScrolledUp = viewModel::onFeedScrolledUp,
            onHeaderToggle = viewModel::onHeaderToggle,
            onCanUpdate = viewModel::onCanUpdateClick,
            feed = FeedCallbacks(
                onRetry = viewModel::onRetry,
                onModuleArchive = viewModel::onModuleArchiveClick,
                onModuleFlash = viewModel::onModuleFlashClick,
                onEditStart = viewModel::onEditStart,
                onDraftChange = viewModel::onDraftChange,
                onEditCancel = viewModel::onEditCancel,
                onEditNext = viewModel::onEditNext,
                onOpenInSettings = viewModel::onOpenInSettings,
                onWrite = viewModel::onWriteConfirm,
                onWriteCancel = viewModel::onWriteCancel,
                onSourcePick = viewModel::onSourcePick,
                onAction = viewModel::onActionClick,
            ),
        )
    }
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Strings.string.mountassistant_common_title)) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.mountassistant_common_a11y_back),
                    )
                },
            )
        },
    ) { padding ->
        CmpBox(modifier = Modifier.fillMaxSize().padding(padding)) {
            val facts = state.facts
            if (facts != null) {
                AssistantBody(
                    state = state,
                    facts = facts,
                    actions = actions,
                    focusRequester = focusRequester,
                )
            }
        }
    }
    state.pin?.let { prompt ->
        ServicePinDialog(
            message = stringResource(Strings.string.mountassistant_confirm_msg_pin_dialog),
            state = ServicePinDialogState(
                sending = prompt.sending,
                wrong = prompt.wrongCount > 0,
                wrongCount = prompt.wrongCount,
                lockedFor = prompt.lockedFor,
            ),
            onDismiss = viewModel::onPinDismiss,
            onSubmit = viewModel::onPinSubmit,
        )
    }
}
