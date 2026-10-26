package ru.alarmtrade.specmp.core.ui.view.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage

/**
 * Единый `ImageLoader` приложения (U-1): memory/disk-кэш и сетевой стек
 * Coil переиспользуются всеми экранами. Провайдится из composition root
 * (`composeApp/App.kt`) через [ProvideCmpImageLoader]; без провайдера
 * Cmp*-обёртки построят дефолтный загрузчик на текущей композиции.
 */
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

/**
 * Обёртка над Coil `AsyncImage` — единая точка загрузки изображений для фич.
 * Замена/обновление Coil затрагивает только `core/ui`, не фичи (U-2).
 */
@Composable
fun CmpAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    imageLoader: ImageLoader = cmpImageLoader()
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment
    )
}

/**
 * Scope для [CmpSubcomposeAsyncImage]: отдаёт painter общего супертипа
 * [Painter] и intrinsic-размер, чтобы потребители не зависели от типов Coil.
 */
@Immutable
class CmpSubcomposeAsyncImageScope internal constructor(internal val asyncPainter: AsyncImagePainter) {
    val painter: Painter get() = asyncPainter
    val intrinsicSize: Size get() = asyncPainter.intrinsicSize
}

/**
 * Обёртка над Coil `SubcomposeAsyncImage` для сценариев, где UI зависит от
 * состояния загрузки (placeholder по пропорциям картинки и т.п.).
 */
@Composable
fun CmpSubcomposeAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = cmpImageLoader(),
    content: @Composable CmpSubcomposeAsyncImageScope.() -> Unit
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier
    ) {
        content(CmpSubcomposeAsyncImageScope(painter))
    }
}

/**
 * Картинка по url с заглушкой: [placeholder] показывается, пока картинка грузится, и вместо неё, если загрузка
 * не удалась. Размер задаёт [modifier] — раскладка не прыгает.
 */
// Слот рисуется в одной из двух взаимоисключающих фаз (загрузка / ошибка) — своего состояния у заглушки нет.
@Suppress("ktlint:compose:content-slot-reused")
@Composable
fun CmpAsyncImageWithPlaceholder(
    model: Any?,
    contentDescription: String?,
    placeholder: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    imageLoader: ImageLoader = cmpImageLoader()
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
        // Слот получает жёсткие ограничения картинки — заглушку центрируем, иначе она растянется.
        loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { placeholder() } },
        error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { placeholder() } }
    )
}
