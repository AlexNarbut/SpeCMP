package ru.alarmtrade.specmp.core.barcode

import android.content.pm.PackageManager
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

actual val isBarcodeScanAvailable: Boolean = true

/** Линейные коды товаров + QR и DataMatrix; остальные форматы ML Kit не ищет — меньше ложных чтений. */
private val Formats = intArrayOf(
    Barcode.FORMAT_EAN_8,
    Barcode.FORMAT_UPC_A,
    Barcode.FORMAT_UPC_E,
    Barcode.FORMAT_CODE_128,
    Barcode.FORMAT_CODE_39,
    Barcode.FORMAT_CODE_93,
    Barcode.FORMAT_ITF,
    Barcode.FORMAT_CODABAR,
    Barcode.FORMAT_QR_CODE,
    Barcode.FORMAT_DATA_MATRIX,
)

@Composable
internal actual fun CameraPreview(
    frame: DpSize,
    torch: Boolean,
    onDetect: (List<String>) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val currentOnDetected by rememberUpdatedState(onDetect)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    val frameWidth = with(density) { frame.width.toPx() }
    val frameHeight = with(density) { frame.height.toPx() }
    val controller = remember { LifecycleCameraController(context) }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    // Привязка камеры к жизненному циклу экрана: уход в фон и закрытие сканера освобождают камеру.
    DisposableEffect(lifecycleOwner, frameWidth, frameHeight) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            currentOnUnavailable()
            return@DisposableEffect onDispose { }
        }
        val executor = Executors.newSingleThreadExecutor()
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Formats.fold(Barcode.FORMAT_EAN_13, Int::or))
            .setZoomSuggestionOptions(
                ZoomSuggestionOptions.Builder { ratio ->
                    controller.setZoomRatio(ratio)
                    true
                }
                    .setMaxSupportedZoomRatio(controller.zoomState.value?.maxZoomRatio ?: DEFAULT_MAX_ZOOM)
                    .build(),
            )
            .build()
        val scanner = BarcodeScanning.getClient(options)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        controller.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        controller.setImageAnalysisAnalyzer(
            executor,
            MlKitAnalyzer(listOf(scanner), COORDINATE_SYSTEM_VIEW_REFERENCED, mainExecutor) { result ->
                val area = frameRect(previewView.width, previewView.height, frameWidth, frameHeight)
                val texts = result.getValue(scanner).orEmpty()
                    .filter { code ->
                        code.boundingBox?.let { area.contains(it.exactCenterX(), it.exactCenterY()) } ==
                            true
                    }
                    .mapNotNull { it.rawValue }
                if (texts.isNotEmpty()) currentOnDetected(texts)
            },
        )
        controller.initializationFuture.addListener(
            {
                // CameraX сообщает отсутствие или занятость камеры исключением из future инициализации.
                @Suppress("TooGenericExceptionCaught", "SwallowedException", "IgnoredReturnValue")
                try {
                    controller.initializationFuture.get()
                    if (!controller.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) currentOnUnavailable()
                } catch (e: Exception) {
                    currentOnUnavailable()
                }
            },
            mainExecutor,
        )
        previewView.controller = controller
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
            controller.clearImageAnalysisAnalyzer()
            scanner.close()
            executor.shutdown()
        }
    }
    LaunchedEffect(torch) { controller.enableTorch(torch) }

    AndroidView(factory = { previewView }, modifier = modifier)
}

private const val DEFAULT_MAX_ZOOM = 4f

private fun frameRect(viewWidth: Int, viewHeight: Int, frameWidth: Float, frameHeight: Float): RectF {
    val left = (viewWidth - frameWidth) / 2
    val top = (viewHeight - frameHeight) / 2
    return RectF(left, top, left + frameWidth, top + frameHeight)
}
