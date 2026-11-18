package ru.alarmtrade.specmp.core.files

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Порт выбора изображения (аватар и т.п.) в два шага: [pick] — системный пикер или камера, изображение уже
 * повёрнуто по EXIF и уменьшено до [IMAGE_PREVIEW_MAX_SIDE] px (превью для обрезки); [prepare] — обрезка, уменьшение
 * до [IMAGE_MAX_SIDE] px и JPEG для отправки. Всё — вне main. Создаётся `rememberImagePicker` в composable и
 * передаётся во ViewModel.
 */
interface ImagePicker {
    /** `false` — источник [ImageSource.Camera] недоступен (desktop, устройство без камеры). */
    val canTakePhoto: Boolean

    suspend fun pick(source: ImageSource): ImagePickResult

    /** [crop] — `null`: всё изображение. */
    suspend fun prepare(image: PickedImage, crop: ImageCropRect?): ImagePrepareResult
}

enum class ImageSource {
    Camera,
    Gallery,
}

/** [preview] декодирован из [bytes]: размеры совпадают, обрезка задаётся в его пикселях. */
class PickedImage(val bytes: ByteArray, val name: String, val preview: ImageBitmap)

/** Отмена — не ошибка и ничего не показывает. */
sealed interface ImagePickResult {
    class Picked(val image: PickedImage) : ImagePickResult

    data object Cancelled : ImagePickResult

    data object Failed : ImagePickResult
}

sealed interface ImagePrepareResult {
    class Prepared(val bytes: ByteArray, val name: String, val mimeType: String) : ImagePrepareResult

    data object Failed : ImagePrepareResult
}

/** Обрезка в пикселях изображения [imageWidth]×[imageHeight] (по превью); при другом размере — пропорционально. */
data class ImageCropRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
    val imageWidth: Int,
    val imageHeight: Int,
)
