package ru.alarmtrade.specmp.feature.supportchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.ProvideSupportChatImageLoader
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreen
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

@Serializable
data object SupportChatRoute : NavKey

/**
 * Точка входа фичи: размещается как entry внутри корневого `NavDisplay`
 * (`composeApp/AppNavigation`). Собственный nav3-граф фичи не создаётся —
 * экран один; per-entry `ViewModelStoreOwner` даёт декоратор
 * `rememberViewModelStoreNavEntryDecorator()` корневого графа, поэтому VM
 * живёт ровно столько, сколько запись бэкстека (см. `docs/navigation.md`).
 *
 * VM создаётся здесь, а не в `composeApp`: экран и его интенты — внутренняя
 * кухня фичи, composeApp передаёт только платформенные порты. Сейвер —
 * зависимость конструктора VM (parametersOf): ему нужен composition context
 * на Android, поэтому провайдится из remember-фабрики, а не из Koin.
 */
@Composable
fun SupportChatEntry(
    onBack: () -> Unit,
    filePicker: SupportChatFilePicker,
    fileSaver: SupportChatFileSaver,
) {
    val viewModel = koinViewModel<SupportChatScreenViewModel> { parametersOf(fileSaver) }
    val state = viewModel.state.collectAsState()

    ProvideSupportChatImageLoader {
        SupportChatScreen(
            state = state.value,
            effect = viewModel.effect,
            onBack = onBack,
            onDraftChange = viewModel::onDraftChange,
            onAttachFile = {
                filePicker.launchDocument { result ->
                    if (result is SupportChatPickedFileResult.Picked) {
                        viewModel.onDocumentPicked(result.file)
                    }
                }
            },
            onAttachPhoto = {
                filePicker.launchImage { result ->
                    if (result is SupportChatPickedFileResult.Picked) {
                        viewModel.onImagePicked(result.file)
                    }
                }
            },
            onRemoveAttachment = viewModel::onRemoveAttachment,
            onSend = viewModel::onSend,
            onRetry = viewModel::onRetry,
            onConnectionRetry = viewModel::onConnectionRetry,
            onHistoryRetry = viewModel::onHistoryRetry,
            onLoadMore = viewModel::onLoadMore,
            onMessageVisible = viewModel::onMessageVisible,
            onDownloadAttachment = viewModel::onDownloadAttachment,
        )
    }
}
