package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.files.ImageCropGeometry
import ru.alarmtrade.specmp.core.files.ImageCropRect
import ru.alarmtrade.specmp.core.files.ImageCropTransform
import ru.alarmtrade.specmp.core.files.PickedImage
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.main_crop_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.main_crop_btn_done
import ru.alarmtrade.specmp.core.localization.resources.main_crop_label_hint
import ru.alarmtrade.specmp.core.localization.resources.main_crop_title_screen
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpFullScreenDialog
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private const val CROP_AREA_FRACTION = 0.86f
private const val WHEEL_ZOOM_STEP = 0.1f
private const val MIN_WHEEL_ZOOM = 0.5f

/** Обрезка под круглый аватар: «Отмена» и системный «Назад» закрывают без загрузки. */
@Composable
internal fun MainAvatarCropScreen(image: PickedImage, onConfirm: (ImageCropRect) -> Unit, onCancel: () -> Unit) {
    CmpFullScreenDialog(onDismissRequest = onCancel) {
        val colors = CmpTheme.colors
        val cropState = remember(image) { MainAvatarCropState() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPagePrimary)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .testTag(MAIN_AVATAR_CROP_TAG),
        ) {
            CmpText(
                text = stringResource(Strings.string.main_crop_title_screen),
                style = CmpTextStyle.Headline1,
                color = colors.foregroundOnDarkPrimaryDefault,
                modifier = Modifier.padding(
                    start = CmpSpacing.screenPaddingH,
                    end = CmpSpacing.screenPaddingH,
                    top = CmpSpacing.space16,
                ),
            )
            CmpText(
                text = stringResource(Strings.string.main_crop_label_hint),
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space8),
            )
            MainAvatarCropArea(
                image = image,
                state = cropState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(CmpSpacing.screenPaddingH),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpButton(
                    text = stringResource(Strings.string.main_crop_btn_cancel),
                    onClick = onCancel,
                    type = CmpButtonType.Secondary,
                    modifier = Modifier.weight(1f).testTag("main_avatar_crop_cancel"),
                )
                CmpButton(
                    text = stringResource(Strings.string.main_crop_btn_done),
                    onClick = { cropState.cropRect()?.let(onConfirm) },
                    enabled = cropState.geometry != null,
                    modifier = Modifier.weight(1f).testTag("main_avatar_crop_done"),
                )
            }
        }
    }
}

/** Геометрия зависит от размера области (известен при раскладке), положение — от жестов; «Готово» читает оба. */
@Stable
private class MainAvatarCropState {
    var geometry: ImageCropGeometry? by mutableStateOf(null)
    var transform: ImageCropTransform? by mutableStateOf(null)

    fun cropRect(): ImageCropRect? = geometry?.let { it.sourceRect(transform ?: it.initial()) }
}

@Composable
private fun MainAvatarCropArea(image: PickedImage, state: MainAvatarCropState, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val strokeWidth = with(LocalDensity.current) { CmpBorderWidth.inputs.toPx() }
    BoxWithConstraints(modifier = modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        val cropSize = with(LocalDensity.current) { min(maxWidth.toPx(), maxHeight.toPx()) * CROP_AREA_FRACTION }
        val bitmap = image.preview
        val geometry = remember(bitmap, cropSize) { ImageCropGeometry(bitmap.width, bitmap.height, cropSize) }
        SideEffect { state.geometry = geometry }
        val clamped = geometry.clamp(state.transform ?: geometry.initial())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(geometry) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        state.transform = geometry.transform(
                            current = geometry.clamp(state.transform ?: geometry.initial()),
                            panX = pan.x,
                            panY = pan.y,
                            zoom = zoom,
                            centroidX = centroid.x - center.x,
                            centroidY = centroid.y - center.y,
                        )
                    }
                }
                .pointerInput(geometry) {
                    // Колесо мыши (desktop): масштаб вокруг курсора.
                    val context = currentCoroutineContext()
                    awaitPointerEventScope {
                        while (context.isActive) {
                            val change = awaitPointerEvent().wheelChange() ?: continue
                            val delta = change.scrollDelta.y
                            val center = Offset(size.width / 2f, size.height / 2f)
                            state.transform = geometry.transform(
                                current = geometry.clamp(state.transform ?: geometry.initial()),
                                zoom = (1f - delta * WHEEL_ZOOM_STEP).coerceAtLeast(MIN_WHEEL_ZOOM),
                                centroidX = change.position.x - center.x,
                                centroidY = change.position.y - center.y,
                            )
                            change.consume()
                        }
                    }
                },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = bitmap.width * clamped.scale
                val height = bitmap.height * clamped.scale
                drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(
                        (size.width / 2 + clamped.offsetX - width / 2).roundToInt(),
                        (size.height / 2 + clamped.offsetY - height / 2).roundToInt(),
                    ),
                    dstSize = IntSize(width.roundToInt(), height.roundToInt()),
                )
                val cropRect = Rect(center = center, radius = cropSize / 2)
                val mask = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(Offset.Zero, size))
                    addOval(cropRect)
                }
                drawPath(mask, color = colors.backgroundOverlay)
                drawCircle(
                    color = colors.foregroundOnDarkPrimaryDefault,
                    radius = cropSize / 2,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}

private fun PointerEvent.wheelChange(): PointerInputChange? =
    changes.firstOrNull()?.takeIf { type == PointerEventType.Scroll && it.scrollDelta.y != 0f }

internal const val MAIN_AVATAR_CROP_TAG = "main_avatar_crop"
