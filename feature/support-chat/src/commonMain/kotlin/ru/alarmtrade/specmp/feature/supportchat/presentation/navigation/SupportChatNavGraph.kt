package ru.alarmtrade.specmp.feature.supportchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatActions
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
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

    // Результаты пикера — через Flow, а не колбэк: на Android лямбда не
    // переживает смерть процесса, пока открыт системный пикер, и выбранный
    // файл терялся молча. Поток буферизует результат до коллектора.
    LaunchedEffect(filePicker, viewModel) {
        filePicker.results.collect { result ->
            if (result is SupportChatPickedFileResult.Picked) {
                when (result.kind) {
                    SupportChatPickKind.Image -> viewModel.onImagePicked(result.file)
                    SupportChatPickKind.Document -> viewModel.onDocumentPicked(result.file)
                }
            }
        }
    }

    SupportChatScreen(
        state = state,
        effect = viewModel.effect,
        actions = SupportChatActions(
            onBack = onBack,
            onAttachFile = { filePicker.launchDocument() },
            onAttachPhoto = { filePicker.launchImage() },
            onRemoveAttachment = viewModel::onRemoveAttachment,
            onSend = viewModel::onSend,
            onRetry = viewModel::onRetry,
            onConnectionRetry = viewModel::onConnectionRetry,
            onHistoryRetry = viewModel::onHistoryRetry,
            onLoadMore = viewModel::onLoadMore,
            onMessagesVisible = viewModel::onMessagesVisible,
            onDownloadAttachment = viewModel::onDownloadAttachment,
        ),
    )
}
