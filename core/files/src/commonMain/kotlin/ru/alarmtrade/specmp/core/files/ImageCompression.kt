package ru.alarmtrade.specmp.core.files

import kotlin.math.max
import kotlin.math.roundToInt
import ru.alarmtrade.specmp.core.common.file.safeFileName

internal const val IMAGE_MAX_SIDE = 800
internal const val IMAGE_JPEG_QUALITY = 85
internal const val IMAGE_PREVIEW_MAX_SIDE = 2048
internal const val IMAGE_PREVIEW_JPEG_QUALITY = 92
internal const val JPEG_MIME_TYPE = "image/jpeg"

internal data class ImageSize(val width: Int, val height: Int)

internal data class ImageRegion(val left: Int, val top: Int, val width: Int, val height: Int)

/** Меньшее изображение не увеличивается. */
internal fun scaledToFit(width: Int, height: Int, maxSide: Int = IMAGE_MAX_SIDE): ImageSize {
    val longest = max(width, height)
    if (longest <= maxSide) return ImageSize(width, height)
    val ratio = maxSide.toDouble() / longest
    return ImageSize(
        width = (width * ratio).roundToInt().coerceAtLeast(1),
        height = (height * ratio).roundToInt().coerceAtLeast(1),
    )
}

/** Наибольшая степень двойки, при которой декодированная сторона не меньше [maxSide] (Android `inSampleSize`). */
internal fun sampleSizeFor(width: Int, height: Int, maxSide: Int = IMAGE_MAX_SIDE): Int {
    var sample = 1
    while (max(width, height) / (sample * 2) >= maxSide) sample *= 2
    return sample
}

/** Область [crop] на изображении [width]×[height]: пропорционально и в его границах; `null` — всё изображение. */
internal fun cropRegion(crop: ImageCropRect?, width: Int, height: Int): ImageRegion {
    if (crop == null || crop.imageWidth <= 0 || crop.imageHeight <= 0) return ImageRegion(0, 0, width, height)
    val scaleX = width.toDouble() / crop.imageWidth
    val scaleY = height.toDouble() / crop.imageHeight
    val left = (crop.left * scaleX).roundToInt().coerceIn(0, width - 1)
    val top = (crop.top * scaleY).roundToInt().coerceIn(0, height - 1)
    val right = ((crop.left + crop.width) * scaleX).roundToInt().coerceIn(left + 1, width)
    val bottom = ((crop.top + crop.height) * scaleY).roundToInt().coerceIn(top + 1, height)
    return ImageRegion(left, top, right - left, bottom - top)
}

/** `IMG_1.HEIC` → `IMG_1.jpg`; без имени — `image.jpg`. */
internal fun jpegFileName(original: String?): String {
    val safe = safeFileName(original.orEmpty(), fallback = DEFAULT_IMAGE_NAME)
    val stem = safe.substringBeforeLast('.').ifBlank { DEFAULT_IMAGE_STEM }
    return "$stem.jpg"
}

private const val DEFAULT_IMAGE_STEM = "image"
private const val DEFAULT_IMAGE_NAME = "$DEFAULT_IMAGE_STEM.jpg"
