@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.supportchat.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatActions
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentFiles
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreen
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

@Serializable
@SerialName("support-chat.SupportChatRoute")
data object SupportChatRoute : NavKey

/**
 * Точка входа фичи: размещается как entry внутри `MainNavGraph`
 * (`composeApp/navigation/AppNavigation.kt`). Собственный nav3-граф фичи не
 * создаётся — экран один; per-entry `ViewModelStoreOwner` даёт декоратор
 * `rememberViewModelStoreNavEntryDecorator()` этого графа, поэтому VM живёт
 * ровно столько, сколько запись бэкстека (см. `docs/navigation.md`).
 *
 * VM создаётся здесь, а не в `composeApp`: экран и его интенты — внутренняя
 * кухня фичи, composeApp передаёт только платформенные порты. Сейвер и порт
 * локальных файлов — зависимости конструктора VM (`parametersOf`); они не
 * должны держать Activity-контекст — VM переживает пересоздание Activity.
 */
@Composable
fun SupportChatEntry(
    onBack: () -> Unit,
    filePicker: SupportChatFilePicker,
    fileSaver: SupportChatFileSaver,
    attachmentFiles: SupportChatAttachmentFiles,
    fileOpener: SupportChatFileOpener,
) {
    val viewModel = koinViewModel<SupportChatScreenViewModel> {
        parametersOf(fileSaver, attachmentFiles, fileOpener)
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentPicker by rememberUpdatedState(filePicker)

    // Проброс результатов платформенного пикера в VM. Поток, а не колбэк: на Android
    // результат может прийти уже в новый процесс (см. SupportChatFilePicker).
    LaunchedEffect(filePicker, viewModel) {
        filePicker.results.collect { viewModel.onPickerResult(it) }
    }

    val actions = remember(viewModel) {
        SupportChatActions(
            onBack = { currentOnBack() },
            onAttachFile = { currentPicker.launchDocument() },
            onAttachPhoto = { currentPicker.launchImage() },
            onRemoveAttachment = viewModel::onRemoveAttachment,
            onSend = viewModel::onSend,
            onRetry = viewModel::onRetry,
            onDeleteFailed = viewModel::onDeleteFailed,
            onHistoryRetry = viewModel::onHistoryRetry,
            onLoadMore = viewModel::onLoadMore,
            onMessagesVisible = viewModel::onMessagesVisible,
            onDownloadAttachment = viewModel::onDownloadAttachment,
            onOpenAttachment = viewModel::onOpenAttachment,
        )
    }

    SupportChatScreen(
        state = state,
        sideEffect = viewModel.sideEffect,
        actions = actions,
    )
}

private fun SupportChatScreenViewModel.onPickerResult(result: SupportChatPickedFileResult) {
    when (result) {
        is SupportChatPickedFileResult.Picked -> when (result.kind) {
            SupportChatPickKind.Image -> onImagePicked(result.file)
            SupportChatPickKind.Document -> onDocumentPicked(result.file)
        }

        SupportChatPickedFileResult.Cancelled -> Unit

        SupportChatPickedFileResult.TooLarge -> onAttachmentRejected(tooLarge = true)

        SupportChatPickedFileResult.Failed -> onAttachmentRejected(tooLarge = false)
    }
}
