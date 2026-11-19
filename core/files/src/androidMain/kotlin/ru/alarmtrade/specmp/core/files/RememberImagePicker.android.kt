package ru.alarmtrade.specmp.core.files

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import co.touchlab.kermit.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.file.queryNameAndSize

private val logger = Logger.withTag("ImagePicker")

private val galleryRequester = ResultRequester<PickVisualMediaRequest, Uri>("Photo picker")
private val cameraRequester = ResultRequester<Uri, Boolean>("Camera")

@Composable
actual fun rememberImagePicker(dispatchers: AppDispatchers): ImagePicker {
    val appContext = LocalContext.current.applicationContext
    galleryRequester.Bind(
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), galleryRequester::onResult),
    )
    cameraRequester.Bind(
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), cameraRequester::onResult),
    )
    return remember(appContext, dispatchers) { AndroidImagePicker(appContext, dispatchers) }
}

private class AndroidImagePicker(private val context: Context, private val dispatchers: AppDispatchers) : ImagePicker {

    override val canTakePhoto: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    override suspend fun pick(source: ImageSource): ImagePickResult = when (source) {
        ImageSource.Gallery -> pickFromGallery()
        ImageSource.Camera -> takePhoto()
    }

    override suspend fun prepare(image: PickedImage, crop: ImageCropRect?): ImagePrepareResult {
        val bytes = withContext(dispatchers.io) { guarded { image.bytes.cropToJpeg(crop) } }
        return preparedImage(bytes, image)
    }

    private suspend fun pickFromGallery(): ImagePickResult {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        val pending = galleryRequester.request(request, dispatchers.main) ?: return ImagePickResult.Failed
        val uri = pending.await() ?: return ImagePickResult.Cancelled
        val bytes =
            withContext(dispatchers.io) { guarded { previewJpeg { context.contentResolver.openInputStream(uri) } } }
        val name = withContext(dispatchers.io) { guarded { context.contentResolver.queryNameAndSize(uri).name } }
        return pickedImage(bytes, name, dispatchers)
    }

    private suspend fun takePhoto(): ImagePickResult {
        val file = withContext(dispatchers.io) {
            File(context.cacheDir, CAPTURE_DIR).apply { mkdirs() }
                .let { File(it, "capture-${UUID.randomUUID()}.jpg") }
        }
        return try {
            val uri = FileProvider.getUriForFile(context, context.packageName + FILE_PROVIDER_AUTHORITY_SUFFIX, file)
            val pending = cameraRequester.request(uri, dispatchers.main) ?: return ImagePickResult.Failed
            if (pending.await() != true) return ImagePickResult.Cancelled
            val bytes = withContext(dispatchers.io) { guarded { previewJpeg { file.inputStream() } } }
            pickedImage(bytes, file.name, dispatchers)
        } finally {
            withContext(NonCancellable + dispatchers.io) { file.delete() }
        }
    }
}

// Граница платформенного I/O и декодера: любой сбой — null (Failed у вызывающего).
@Suppress("TooGenericExceptionCaught")
private inline fun <T> guarded(block: () -> T?): T? = try {
    block()
} catch (e: Exception) {
    logger.w(e) { "Image read failed" }
    null
} catch (e: OutOfMemoryError) {
    logger.w(e) { "Image is too large to decode" }
    null
}

/** Превью для обрезки: повёрнуто по EXIF, не больше [IMAGE_PREVIEW_MAX_SIDE]. */
private fun previewJpeg(open: () -> InputStream?): ByteArray? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    // С inJustDecodeBounds decodeStream всегда отдаёт null: размеры — только в bounds.
    (open() ?: return null).use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, IMAGE_PREVIEW_MAX_SIDE)
    }
    val decoded = open()?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
    val orientation = open()?.use { it.exifOrientation() } ?: ExifInterface.ORIENTATION_NORMAL
    val target = scaledToFit(decoded.width, decoded.height, IMAGE_PREVIEW_MAX_SIDE)
    val matrix = Matrix().apply {
        postScale(target.width.toFloat() / decoded.width, target.height.toFloat() / decoded.height)
        applyExifOrientation(orientation)
    }
    val transformed = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    return transformed.toJpeg(IMAGE_PREVIEW_JPEG_QUALITY, recycle = setOf(decoded))
}

/** Обрезка в пикселях превью, затем не больше [IMAGE_MAX_SIDE] и JPEG для отправки. */
private fun ByteArray.cropToJpeg(crop: ImageCropRect?): ByteArray? {
    val decoded = BitmapFactory.decodeByteArray(this, 0, size) ?: return null
    val region = cropRegion(crop, decoded.width, decoded.height)
    val target = scaledToFit(region.width, region.height)
    val matrix = Matrix().apply {
        postScale(target.width.toFloat() / region.width, target.height.toFloat() / region.height)
    }
    val cropped = Bitmap.createBitmap(decoded, region.left, region.top, region.width, region.height, matrix, true)
    return cropped.toJpeg(IMAGE_JPEG_QUALITY, recycle = setOf(decoded))
}

private fun Bitmap.toJpeg(quality: Int, recycle: Set<Bitmap>): ByteArray {
    val opaque = if (hasAlpha()) onWhite() else this
    return try {
        ByteArrayOutputStream().use { output ->
            opaque.compress(Bitmap.CompressFormat.JPEG, quality, output)
            output.toByteArray()
        }
    } finally {
        (recycle + this + opaque).forEach(Bitmap::recycle)
    }
}

// Не JPEG (PNG, WebP) — EXIF может не читаться: тогда ориентация обычная.
@Suppress("TooGenericExceptionCaught", "SwallowedException")
private fun InputStream.exifOrientation(): Int = try {
    ExifInterface(this).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
} catch (e: Exception) {
    ExifInterface.ORIENTATION_NORMAL
}

@Suppress("MagicNumber")
private fun Matrix.applyExifOrientation(orientation: Int) {
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)

        ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)

        ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)

        ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)

        ExifInterface.ORIENTATION_TRANSPOSE -> {
            postRotate(90f)
            postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            postRotate(270f)
            postScale(-1f, 1f)
        }
    }
}

/** JPEG без альфы: прозрачное становится белым, а не чёрным. */
private fun Bitmap.onWhite(): Bitmap {
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(result).apply {
        drawColor(android.graphics.Color.WHITE)
        drawBitmap(this@onWhite, 0f, 0f, null)
    }
    return result
}

private const val CAPTURE_DIR = "image-capture"
