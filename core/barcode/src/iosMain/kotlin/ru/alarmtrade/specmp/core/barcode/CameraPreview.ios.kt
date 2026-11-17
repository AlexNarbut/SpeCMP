package ru.alarmtrade.specmp.core.barcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureExposureModeContinuousAutoExposure
import platform.AVFoundation.AVCaptureFocusModeContinuousAutoFocus
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeCodabarCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeITF14Code
import platform.AVFoundation.AVMetadataObjectTypeInterleaved2of5Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.exposurePointOfInterestSupported
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.focusPointOfInterestSupported
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposureModeSupported
import platform.AVFoundation.isFocusModeSupported
import platform.AVFoundation.minimumFocusDistance
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

actual val isBarcodeScanAvailable: Boolean = true

private val Types = listOf(
    AVMetadataObjectTypeEAN13Code,
    AVMetadataObjectTypeEAN8Code,
    AVMetadataObjectTypeUPCECode,
    AVMetadataObjectTypeCode128Code,
    AVMetadataObjectTypeCode39Code,
    AVMetadataObjectTypeCode93Code,
    AVMetadataObjectTypeITF14Code,
    AVMetadataObjectTypeInterleaved2of5Code,
    AVMetadataObjectTypeCodabarCode,
    AVMetadataObjectTypeQRCode,
    AVMetadataObjectTypeDataMatrixCode,
)

// Сессия запускается и останавливается на своей очереди: startRunning блокирует поток (требование Apple).
private val SessionQueue = dispatch_queue_create("ru.alarmtrade.specmp.barcode.session", null)

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun CameraPreview(
    frame: DpSize,
    torch: Boolean,
    focus: FocusRequest?,
    onDetect: (List<String>) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val currentOnDetected by rememberUpdatedState(onDetect)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    val camera = remember { BarcodeCamera { texts -> currentOnDetected(texts) } }

    DisposableEffect(camera) {
        if (!camera.start()) currentOnUnavailable()
        onDispose { camera.stop() }
    }
    LaunchedEffect(torch) { camera.setTorch(torch) }
    LaunchedEffect(focus) {
        focus?.let { camera.focus(it.x.value.toDouble(), it.y.value.toDouble()) }
    }

    UIKitView(
        factory = { camera.view },
        modifier = modifier,
        update = { camera.frame = frame.width.value.toDouble() to frame.height.value.toDouble() },
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class BarcodeCamera(private val onDetect: (List<String>) -> Unit) {
    private val session = AVCaptureSession()
    private val output = AVCaptureMetadataOutput()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }
    private val delegate = MetadataDelegate { texts -> onDetect(texts) }
    private var device: AVCaptureDevice? = null
    private var running = false

    /** Рамка сканирования в pt; меняется — область распознавания пересчитывается в [layout]. */
    var frame: Pair<Double, Double> = 0.0 to 0.0
        set(value) {
            field = value
            view.setNeedsLayout()
        }

    val view: UIView = PreviewHost { bounds -> layout(bounds) }.also { it.layer.addSublayer(previewLayer) }

    fun start(): Boolean {
        val camera = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return false
        val input = AVCaptureDeviceInput.deviceInputWithDevice(camera, error = null) ?: return false
        if (!session.canAddInput(input) || !session.canAddOutput(output)) return false
        session.addInput(input)
        session.addOutput(output)
        output.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
        output.metadataObjectTypes = Types.filter { it in output.availableMetadataObjectTypes }
        device = camera
        configure(camera)
        // Область распознавания пересчитывается только у запущенной сессии: до startRunning
        // metadataOutputRectOfInterestForRect возвращает некорректный (нулевой) прямоугольник — коды не находятся
        dispatch_async(SessionQueue) {
            session.startRunning()
            dispatch_async(dispatch_get_main_queue()) {
                running = true
                view.setNeedsLayout()
            }
        }
        return true
    }

    fun stop() {
        setTorch(false)
        dispatch_async(SessionQueue) { session.stopRunning() }
    }

    fun setTorch(on: Boolean) {
        val camera = device ?: return
        if (!camera.hasTorch) return
        if (camera.lockForConfiguration(null)) {
            camera.torchMode = if (on) AVCaptureTorchModeOn else AVCaptureTorchModeOff
            camera.unlockForConfiguration()
        }
    }

    /** Фокус и экспозиция в точку превью (pt); потом камера сама следит за фокусом дальше. */
    fun focus(x: Double, y: Double) {
        val camera = device ?: return
        val point = previewLayer.captureDevicePointOfInterestForPoint(CGPointMake(x, y))
        if (!camera.lockForConfiguration(null)) return
        if (camera.focusPointOfInterestSupported) {
            camera.focusPointOfInterest = point
            if (camera.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
                camera.focusMode = AVCaptureFocusModeContinuousAutoFocus
            }
        }
        if (camera.exposurePointOfInterestSupported) {
            camera.exposurePointOfInterest = point
            if (camera.isExposureModeSupported(AVCaptureExposureModeContinuousAutoExposure)) {
                camera.exposureMode = AVCaptureExposureModeContinuousAutoExposure
            }
        }
        camera.unlockForConfiguration()
    }

    // Непрерывный автофокус и стартовый зум: у камер с большой минимальной дистанцией фокуса (iPhone 13 Pro+) мелкий
    // штрих-код вблизи иначе не в фокусе — так делает пример Apple AVCamBarcode.
    private fun configure(camera: AVCaptureDevice) {
        if (!camera.lockForConfiguration(null)) return
        if (camera.isFocusModeSupported(AVCaptureFocusModeContinuousAutoFocus)) {
            camera.focusMode = AVCaptureFocusModeContinuousAutoFocus
        }
        if (camera.minimumFocusDistance.toDouble() >= FAR_FOCUS_MM) camera.videoZoomFactor = FAR_FOCUS_ZOOM
        camera.unlockForConfiguration()
    }

    private fun layout(bounds: CValue<CGRect>) {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
        val (width, height) = frame
        if (!running || width <= 0 || height <= 0) return
        bounds.useContents {
            val rect = CGRectMake((size.width - width) / 2, (size.height - height) / 2, width, height)
            output.rectOfInterest = previewLayer.metadataOutputRectOfInterestForRect(rect)
        }
    }

    private companion object {
        const val FAR_FOCUS_MM = 150.0
        const val FAR_FOCUS_ZOOM = 2.0
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PreviewHost(private val onLayout: (CValue<CGRect>) -> Unit) :
    UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    override fun layoutSubviews() {
        super.layoutSubviews()
        onLayout(bounds)
    }
}

private class MetadataDelegate(private val onDetect: (List<String>) -> Unit) :
    NSObject(),
    AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        val texts = didOutputMetadataObjects.mapNotNull { (it as? AVMetadataMachineReadableCodeObject)?.stringValue }
        if (texts.isNotEmpty()) onDetect(texts)
    }
}
