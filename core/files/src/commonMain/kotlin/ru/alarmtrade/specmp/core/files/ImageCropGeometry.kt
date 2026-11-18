package ru.alarmtrade.specmp.core.files

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Масштаб — экранных px на px изображения; смещение — центра изображения от центра области обрезки, экранные px. */
data class ImageCropTransform(val scale: Float, val offsetX: Float, val offsetY: Float)

/**
 * Обрезка под круглый аватар: изображение [imageWidth]×[imageHeight] под квадратной областью [cropSize] (экранные px)
 * в центре окна. Минимальный масштаб — изображение закрывает область, максимальный — [MAX_ZOOM] от него; сдвинуть так,
 * чтобы в области появилась пустота, нельзя.
 */
class ImageCropGeometry(val imageWidth: Int, val imageHeight: Int, val cropSize: Float) {

    val minScale: Float = if (imageWidth > 0 && imageHeight > 0) cropSize / min(imageWidth, imageHeight) else 1f

    val maxScale: Float = minScale * MAX_ZOOM

    fun initial(): ImageCropTransform = ImageCropTransform(minScale, 0f, 0f)

    fun clamp(transform: ImageCropTransform): ImageCropTransform {
        val scale = transform.scale.coerceIn(minScale, maxScale)
        val maxX = max(0f, imageWidth * scale / 2 - cropSize / 2)
        val maxY = max(0f, imageHeight * scale / 2 - cropSize / 2)
        return ImageCropTransform(
            scale,
            transform.offsetX.coerceIn(-maxX, maxX),
            transform.offsetY.coerceIn(-maxY, maxY),
        )
    }

    /** Жест: сдвиг [panX]/[panY] и масштаб [zoom] вокруг точки [centroidX]/[centroidY] (от центра области). */
    fun transform(
        current: ImageCropTransform,
        panX: Float = 0f,
        panY: Float = 0f,
        zoom: Float = 1f,
        centroidX: Float = 0f,
        centroidY: Float = 0f,
    ): ImageCropTransform {
        val scale = (current.scale * zoom).coerceIn(minScale, maxScale)
        val applied = scale / current.scale
        return clamp(
            ImageCropTransform(
                scale = scale,
                offsetX = (current.offsetX - centroidX) * applied + centroidX + panX,
                offsetY = (current.offsetY - centroidY) * applied + centroidY + panY,
            ),
        )
    }

    /** Область обрезки в пикселях изображения. */
    fun sourceRect(transform: ImageCropTransform): ImageCropRect {
        val clamped = clamp(transform)
        val side = (cropSize / clamped.scale).roundToInt().coerceIn(1, max(1, min(imageWidth, imageHeight)))
        val left = (imageWidth / 2f - (cropSize / 2 + clamped.offsetX) / clamped.scale).roundToInt()
        val top = (imageHeight / 2f - (cropSize / 2 + clamped.offsetY) / clamped.scale).roundToInt()
        return ImageCropRect(
            left = left.coerceIn(0, max(0, imageWidth - side)),
            top = top.coerceIn(0, max(0, imageHeight - side)),
            width = side,
            height = side,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
        )
    }

    companion object {
        const val MAX_ZOOM = 4f
    }
}
