package ru.alarmtrade.specmp.core.ui.view.image

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.decode.DataSource

/** Без провайдера Cmp*-обёртки построят дефолтный загрузчик на текущей композиции. */
val LocalCmpImageLoader = staticCompositionLocalOf<ImageLoader?> { null }

@Composable
fun rememberCmpImageLoader(): ImageLoader {
    val context = LocalPlatformContext.current
    return remember(context) { ImageLoader.Builder(context).build() }
}

@Composable
fun ProvideCmpImageLoader(imageLoader: ImageLoader, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCmpImageLoader provides imageLoader, content = content)
}

@Composable
private fun cmpImageLoader(): ImageLoader {
    LocalCmpImageLoader.current?.let { return it }
    return rememberCmpImageLoader()
}

@Composable
fun CmpAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    imageLoader: ImageLoader = cmpImageLoader(),
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
    )
}

@Immutable
class CmpSubcomposeAsyncImageScope internal constructor(internal val asyncPainter: AsyncImagePainter) {
    val painter: Painter get() = asyncPainter
    val intrinsicSize: Size get() = asyncPainter.intrinsicSize
}

@Composable
fun CmpSubcomposeAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = cmpImageLoader(),
    content: @Composable CmpSubcomposeAsyncImageScope.() -> Unit,
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
    ) {
        content(CmpSubcomposeAsyncImageScope(painter))
    }
}

private const val IMAGE_FADE_MS = 220

private enum class ImagePhase { LOADING, SUCCESS, ERROR }

/** Без субкомпозиции — дёшево в строках списков; проявление только не из memory-кэша, чтобы при скролле не мигали. */
@Composable
fun CmpAsyncImageWithPlaceholder(
    model: Any?,
    contentDescription: String?,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    imageLoader: ImageLoader = cmpImageLoader(),
) {
    var phase by remember(model) { mutableStateOf(ImagePhase.LOADING) }
    var fade by remember(model) { mutableStateOf(true) }
    val alpha = animateFloatAsState(
        targetValue = if (phase == ImagePhase.SUCCESS) 1f else 0f,
        animationSpec = if (fade) tween(IMAGE_FADE_MS) else snap(),
        label = "imageFade",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (phase != ImagePhase.SUCCESS) placeholder()
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            imageLoader = imageLoader,
            // Альфа читается в фазе draw — проявление не рекомпозирует строку.
            modifier = Modifier.matchParentSize().graphicsLayer { this.alpha = alpha.value },
            contentScale = contentScale,
            onState = { state ->
                phase = when (state) {
                    is AsyncImagePainter.State.Success -> ImagePhase.SUCCESS
                    is AsyncImagePainter.State.Error -> ImagePhase.ERROR
                    else -> ImagePhase.LOADING
                }
                if (state is AsyncImagePainter.State.Success) {
                    fade = state.result.dataSource != DataSource.MEMORY_CACHE
                }
            },
        )
    }
}
