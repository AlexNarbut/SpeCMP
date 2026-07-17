package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext

internal val LocalSupportChatImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("SupportChat ImageLoader not provided")
}

@Composable
internal fun ProvideSupportChatImageLoader(content: @Composable () -> Unit) {
    val context = LocalPlatformContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context).build()
    }
    CompositionLocalProvider(
        LocalSupportChatImageLoader provides imageLoader,
        content = content,
    )
}
