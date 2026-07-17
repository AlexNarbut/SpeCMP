package ru.alarmtrade.specmp.feature.supportchat.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.ProvideSupportChatImageLoader
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreen
import ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.SupportChatScreenViewModel

@Serializable
data object SupportChatRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SupportChatRoute::class, SupportChatRoute.serializer())
        }
    }
}

@Composable
fun SupportChatNavGraph(
    onBack: () -> Unit,
    filePicker: SupportChatFilePicker,
    fileSaver: SupportChatFileSaver,
) {
    val viewModel = koinViewModel<SupportChatScreenViewModel>()
    val state = viewModel.state.collectAsState()
    val backStack = rememberNavBackStack(navConfig, SupportChatRoute)

    DisposableEffect(viewModel) {
        viewModel.onScreenOpened()
        onDispose(viewModel::onScreenDisposed)
    }

    ProvideSupportChatImageLoader {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = onBack,
            entryProvider = entryProvider<NavKey> {
                entry<SupportChatRoute> { _ ->
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
                        onLoadMore = viewModel::onLoadMore,
                        onMessageVisible = viewModel::onMessageVisible,
                        onDownloadAttachment = { attachment ->
                            viewModel.onDownloadAttachment(attachment, fileSaver)
                        },
                    )
                }
            },
        )
    }
}
