package ru.alarmtrade.specmp.feature.main.presentation.screen.main

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

internal class FakeImagePicker(
    override val canTakePhoto: Boolean = true,
    var result: ImagePickResult = ImagePickResult.Picked(testPickedImage()),
    var prepareResult: ImagePrepareResult = ImagePrepareResult.Prepared(ByteArray(4), "avatar.jpg", "image/jpeg"),
) : ImagePicker {
    val requests = mutableListOf<ImageSource>()
    val prepared = mutableListOf<ImageCropRect?>()

    override suspend fun pick(source: ImageSource): ImagePickResult {
        requests += source
        return result
    }

    override suspend fun prepare(image: PickedImage, crop: ImageCropRect?, maxSide: Int): ImagePrepareResult {
        prepared += crop
        return prepareResult
    }
}

internal fun testPickedImage(width: Int = 400, height: Int = 300): PickedImage =
    PickedImage(ByteArray(8), "avatar.jpg", FakeImageBitmap(width, height))

private class FakeImageBitmap(override val width: Int, override val height: Int) : ImageBitmap {
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
