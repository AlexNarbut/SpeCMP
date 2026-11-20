package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.app_picker_label_images
import ru.alarmtrade.specmp.core.localization.resources.app_picker_title_image

private val logger = Logger.withTag("ImagePicker")

/** Камеры нет: только файл. EXIF-ориентация не учитывается — ImageIO её не читает. */
@Composable
actual fun rememberImagePicker(dispatchers: AppDispatchers): ImagePicker {
    val title = stringResource(Strings.string.app_picker_title_image)
    val filterLabel = stringResource(Strings.string.app_picker_label_images)
    return remember(title, filterLabel, dispatchers) {
        object : ImagePicker {
            override val canTakePhoto: Boolean = false

            override suspend fun pick(source: ImageSource): ImagePickResult {
                val file = withContext(dispatchers.main) {
                    val chooser = imageChooser(filterLabel).apply { dialogTitle = title }
                    chooser.selectedFile.takeIf { chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION }
                } ?: return ImagePickResult.Cancelled
                val bytes = withContext(dispatchers.io) {
                    guarded { file.takeIf { it.isFile && it.canRead() }?.let { ImageIO.read(it) }?.previewJpeg() }
                }
                return pickedImage(bytes, file.name, dispatchers)
            }

            override suspend fun prepare(image: PickedImage, crop: ImageCropRect?, maxSide: Int): ImagePrepareResult {
                val bytes = withContext(dispatchers.io) {
                    guarded { ImageIO.read(ByteArrayInputStream(image.bytes))?.cropToJpeg(crop, maxSide) }
                }
                return preparedImage(bytes, image)
            }
        }
    }
}

private fun imageChooser(filterLabel: String) = JFileChooser().apply {
    fileSelectionMode = JFileChooser.FILES_ONLY
    isMultiSelectionEnabled = false
    fileFilter = FileNameExtensionFilter(filterLabel, "jpg", "jpeg", "png", "gif", "bmp")
}

// Граница платформенного I/O и декодера: любой сбой — null (Failed у вызывающего).
@Suppress("TooGenericExceptionCaught")
private inline fun <T> guarded(block: () -> T?): T? = try {
    block()
} catch (e: Exception) {
    logger.w(e) { "Image read failed" }
    null
}

private fun BufferedImage.previewJpeg(): ByteArray {
    val target = scaledToFit(width, height, IMAGE_PREVIEW_MAX_SIDE)
    return scaled(ImageRegion(0, 0, width, height), target).toJpeg(IMAGE_PREVIEW_JPEG_QUALITY)
}

private fun BufferedImage.cropToJpeg(crop: ImageCropRect?, maxSide: Int): ByteArray {
    val region = cropRegion(crop, width, height)
    return scaled(region, scaledToFit(region.width, region.height, maxSide)).toJpeg(IMAGE_JPEG_QUALITY)
}

/** Прозрачное — на белом: в JPEG альфы нет. */
private fun BufferedImage.scaled(region: ImageRegion, target: ImageSize): BufferedImage {
    val image = BufferedImage(target.width, target.height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    try {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.drawImage(
            getSubimage(region.left, region.top, region.width, region.height),
            0,
            0,
            target.width,
            target.height,
            Color.WHITE,
            null,
        )
    } finally {
        graphics.dispose()
    }
    return image
}

private fun BufferedImage.toJpeg(quality: Int): ByteArray {
    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    return try {
        ByteArrayOutputStream().use { output ->
            ImageIO.createImageOutputStream(output).use { stream ->
                writer.output = stream
                val params = writer.defaultWriteParam.apply {
                    compressionMode = ImageWriteParam.MODE_EXPLICIT
                    compressionQuality = quality / PERCENT
                }
                writer.write(null, IIOImage(this, null, null), params)
            }
            output.toByteArray()
        }
    } finally {
        writer.dispose()
    }
}

private const val PERCENT = 100f
