@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIRectFill
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import ru.alarmtrade.specmp.core.common.AppDispatchers

/** Галерея — PHPicker (без разрешения на фото), камера — `UIImagePickerController`. */
@Composable
actual fun rememberImagePicker(dispatchers: AppDispatchers): ImagePicker = remember(dispatchers) {
    object : ImagePicker {
        override val canTakePhoto: Boolean = UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
        )

        override suspend fun pick(source: ImageSource): ImagePickResult {
            val outcome = withContext(dispatchers.main) {
                when (source) {
                    ImageSource.Gallery -> presentPhotoPicker()
                    ImageSource.Camera -> if (canTakePhoto) presentCamera() else ImageOutcome.NotPresented
                }
            }
            return when (outcome) {
                ImageOutcome.NotPresented -> ImagePickResult.Failed

                ImageOutcome.Cancelled -> ImagePickResult.Cancelled

                is ImageOutcome.Picked -> {
                    val bytes = withContext(dispatchers.default) {
                        outcome.image.toJpeg(IMAGE_PREVIEW_MAX_SIDE, IMAGE_PREVIEW_JPEG_QUALITY, crop = null)
                    }
                    pickedImage(bytes, outcome.name, dispatchers)
                }
            }
        }

        override suspend fun prepare(image: PickedImage, crop: ImageCropRect?): ImagePrepareResult {
            val bytes = withContext(dispatchers.default) {
                UIImage.imageWithData(image.bytes.toNSData())?.toJpeg(IMAGE_MAX_SIDE, IMAGE_JPEG_QUALITY, crop)
            }
            return preparedImage(bytes, image)
        }
    }
}

private sealed interface ImageOutcome {
    class Picked(val image: UIImage, val name: String?) : ImageOutcome

    data object Cancelled : ImageOutcome

    data object NotPresented : ImageOutcome
}

private suspend fun presentPhotoPicker(): ImageOutcome = presentPicker { finish ->
    val configuration = PHPickerConfiguration().apply {
        filter = PHPickerFilter.imagesFilter
        selectionLimit = 1
    }
    val delegate = PhotoPickerDelegate { result ->
        if (result == null) {
            finish(ImageOutcome.Cancelled)
        } else {
            result.loadImage { image, name ->
                finish(image?.let { ImageOutcome.Picked(it, name) } ?: ImageOutcome.NotPresented)
            }
        }
    }
    PHPickerViewController(configuration).apply { this.delegate = delegate } to delegate
}

private suspend fun presentCamera(): ImageOutcome = presentPicker { finish ->
    val delegate =
        CameraDelegate { image -> finish(image?.let { ImageOutcome.Picked(it, null) } ?: ImageOutcome.Cancelled) }
    UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        this.delegate = delegate
    } to delegate
}

/** Делегат у UIKit — weak: держим его здесь до исхода. Только на main. */
private var activeDelegate: NSObject? = null

/** Вызывать на main. Пикер закрывается здесь при любом исходе: PHPicker и камера сами себя не закрывают. */
private suspend fun presentPicker(
    create: (finish: (ImageOutcome) -> Unit) -> Pair<UIViewController, NSObject>,
): ImageOutcome = suspendCancellableCoroutine { cont ->
    val presenter = topViewController()
    if (presenter == null) {
        cont.resume(ImageOutcome.NotPresented)
        return@suspendCancellableCoroutine
    }
    var controller: UIViewController? = null
    val finish: (ImageOutcome) -> Unit = { outcome ->
        dispatch_async(dispatch_get_main_queue()) {
            controller?.dismissViewControllerAnimated(true, null)
            activeDelegate = null
            if (cont.isActive) cont.resume(outcome)
        }
    }
    val (created, delegate) = create(finish)
    controller = created
    activeDelegate = delegate
    cont.invokeOnCancellation {
        dispatch_async(dispatch_get_main_queue()) {
            if (activeDelegate === delegate) activeDelegate = null
            created.dismissViewControllerAnimated(true, null)
        }
    }
    presenter.presentViewController(created, animated = true, completion = null)
    // UIKit молча отказывает в показе, пока идёт другая презентация/закрытие: без проверки корутина зависнет.
    if (created.presentingViewController == null && cont.isActive) {
        activeDelegate = null
        cont.resume(ImageOutcome.NotPresented)
    }
}

private class PhotoPickerDelegate(private val onResult: (PHPickerResult?) -> Unit) :
    NSObject(),
    PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        onResult(didFinishPicking.firstOrNull() as? PHPickerResult)
    }
}

private class CameraDelegate(private val onResult: (UIImage?) -> Unit) :
    NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        onResult(didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onResult(null)
    }
}

/** Колбэк — на фоновой очереди. */
private fun PHPickerResult.loadImage(onLoaded: (UIImage?, String?) -> Unit) {
    val provider = itemProvider
    val name = provider.suggestedName
    provider.loadDataRepresentationForTypeIdentifier(IMAGE_UTI) { data, _ ->
        onLoaded(data?.let { UIImage.imageWithData(it) }, name)
    }
}

/**
 * Рисование учитывает `imageOrientation`: результат всегда «прямой». Прозрачное — на белом. Обрезка — сдвигом и
 * масштабом рисунка на холсте размера области.
 */
private fun UIImage.toJpeg(maxSide: Int, quality: Int, crop: ImageCropRect?): ByteArray? {
    val (pixelWidth, pixelHeight) = size.useContents { (width * scale).toInt() to (height * scale).toInt() }
    if (pixelWidth <= 0 || pixelHeight <= 0) return null
    val region = cropRegion(crop, pixelWidth, pixelHeight)
    val target = scaledToFit(region.width, region.height, maxSide)
    val ratio = target.width.toDouble() / region.width
    val format = UIGraphicsImageRendererFormat().apply {
        scale = 1.0
        opaque = true
    }
    val canvasSize = CGSizeMake(target.width.toDouble(), target.height.toDouble())
    val rendered = UIGraphicsImageRenderer(size = canvasSize, format = format).imageWithActions { _ ->
        UIColor.whiteColor.setFill()
        UIRectFill(CGRectMake(0.0, 0.0, target.width.toDouble(), target.height.toDouble()))
        drawInRect(
            CGRectMake(
                -region.left * ratio,
                -region.top * ratio,
                pixelWidth * ratio,
                pixelHeight * ratio,
            ),
        )
    }
    return UIImageJPEGRepresentation(rendered, quality / PERCENT)?.toByteArray()
}

private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}

private const val IMAGE_UTI = "public.image"
private const val PERCENT = 100.0
