package ru.alarmtrade.specmp.core.files

import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import ru.alarmtrade.specmp.core.common.AppDispatchers

private val logger = Logger.withTag("ImagePicker")

/** Превью — общим декодером Compose из уже подготовленных байтов (≤ [IMAGE_PREVIEW_MAX_SIDE] px). */
// Граница декодера: битые байты — Failed.
@Suppress("TooGenericExceptionCaught")
internal suspend fun pickedImage(bytes: ByteArray?, name: String?, dispatchers: AppDispatchers): ImagePickResult {
    if (bytes == null) return ImagePickResult.Failed
    return withContext(dispatchers.default) {
        try {
            ImagePickResult.Picked(PickedImage(bytes, jpegFileName(name), bytes.decodeToImageBitmap()))
        } catch (e: Exception) {
            logger.w(e) { "Image preview decode failed" }
            ImagePickResult.Failed
        }
    }
}

internal fun preparedImage(bytes: ByteArray?, image: PickedImage): ImagePrepareResult =
    bytes?.let { ImagePrepareResult.Prepared(it, image.name, JPEG_MIME_TYPE) } ?: ImagePrepareResult.Failed
