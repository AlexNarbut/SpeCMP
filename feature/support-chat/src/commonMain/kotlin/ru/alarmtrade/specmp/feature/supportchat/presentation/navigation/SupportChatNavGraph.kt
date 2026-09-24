package ru.alarmtrade.specmp.feature.supportchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver
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
 * кухня фичи, composeApp передаёт только платформенные порты.
 */
@Composable
fun SupportChatEntry(
    onBack: () -> Unit,
    filePicker: SupportChatFilePicker,
    fileSaver: SupportChatFileSaver,
) {
    val viewModel = koinViewModel<SupportChatScreenViewModel>()
    val state = viewModel.state.collectAsState()

    ProvideSupportChatImageLoader {
        SupportChatScreen(
            state = state.value,
            effect = viewModel.effect,
            onBack = onBack,
            onDraftChange = viewModel::onDraftChange,
            onAttachFile = { filePicker.launchDocument { file -> file?.let(viewModel::onDocumentPicked) } },
            onAttachPhoto = { filePicker.launchImage { file -> file?.let(viewModel::onImagePicked) } },
            onRemoveAttachment = viewModel::onRemoveAttachment,
            onSend = viewModel::onSend,
            onRetry = viewModel::onRetry,
            onConnectionRetry = viewModel::onConnectionRetry,
            onHistoryRetry = viewModel::onHistoryRetry,
            onLoadMore = viewModel::onLoadMore,
            onMessageVisible = viewModel::onMessageVisible,
            onDownloadAttachment = { attachment ->
                viewModel.onDownloadAttachment(attachment, fileSaver)
            },
        )
    }
}
