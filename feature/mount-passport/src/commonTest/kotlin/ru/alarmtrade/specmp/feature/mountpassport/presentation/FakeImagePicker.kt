package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import ru.alarmtrade.specmp.core.files.ImageCropRect
import ru.alarmtrade.specmp.core.files.ImagePickResult
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.core.files.ImagePrepareResult
import ru.alarmtrade.specmp.core.files.ImageSource
import ru.alarmtrade.specmp.core.files.PickedImage

internal val PICKED_BYTES = byteArrayOf(1, 2, 3)
internal val THUMB_BYTES = byteArrayOf(9)

internal class FakeImagePicker(override val canTakePhoto: Boolean = true) : ImagePicker {
    var result: ImagePickResult = ImagePickResult.Picked(PickedImage(PICKED_BYTES, "photo.jpg", FakeImageBitmap()))
    val requests = mutableListOf<ImageSource>()
    val preparedSides = mutableListOf<Int>()

    override suspend fun pick(source: ImageSource): ImagePickResult {
        requests += source
        return result
    }

    override suspend fun prepare(image: PickedImage, crop: ImageCropRect?, maxSide: Int): ImagePrepareResult {
        preparedSides += maxSide
        return ImagePrepareResult.Prepared(THUMB_BYTES, "thumb.jpg", "image/jpeg")
    }
}

private class FakeImageBitmap : ImageBitmap {
    override val width: Int = 4
    override val height: Int = 3
    override val colorSpace: ColorSpace = ColorSpaces.Srgb
    override val hasAlpha: Boolean = false
    override val config: ImageBitmapConfig = ImageBitmapConfig.Argb8888

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) = Unit

    override fun prepareToDraw() = Unit
}
