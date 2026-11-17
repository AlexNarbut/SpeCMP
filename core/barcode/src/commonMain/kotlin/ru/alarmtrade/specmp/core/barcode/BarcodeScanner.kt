package ru.alarmtrade.specmp.core.barcode

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_a11y_torch_off
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_a11y_torch_on
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_btn_allow
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_btn_manual
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_btn_settings
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_error_blocked
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_error_denied
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_error_unavailable
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_hint_slow
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_hint_where
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_msg_found
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_title
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_title_no_access
import ru.alarmtrade.specmp.core.localization.resources.barcode_scanner_title_unavailable
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val CompactFrame = DpSize(320.dp, 170.dp)
private val WideFrame = DpSize(460.dp, 220.dp)
private val FrameCorner = 18.dp
private val CornerLength = 40.dp
private val CornerStroke = 5.dp
private val ControlSize = 56.dp
private val SlowHintAfter = 8.seconds
private val FoundShownFor = 1_300.milliseconds
private const val LASER_MILLIS = 1_200
private const val LASER_INSET_FRACTION = 0.06f
private const val EAN13_LENGTH = 13
private const val EAN13_HALF = 7
private const val FOCUS_RING_MILLIS = 700
private val FocusRingRadius = 28.dp

/**
 * Полноэкранный сканер штрих-кода: камера, рамка, лазер, фонарик, «Ввести вручную», подсказка через 8 с, отклик
 * «распознано» и состояния без камеры. Разрешение запрашивает вызывающий ([access]). [accept] — номер из текста кода
 * или `null` (не тот код — пропускается молча, камера работает дальше). Только коды внутри рамки.
 */
@Suppress("LongParameterList") // колбэки экрана сканера: результат, доступ, настройки, ручной ввод, закрытие
@Composable
fun BarcodeScanner(
    access: BarcodeScannerAccess,
    accept: (String) -> String?,
    onResult: (String) -> Unit,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onManualInput: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val camera = cmpDomainColors()
    var torch by rememberSaveable { mutableStateOf(false) }
    var unavailable by remember { mutableStateOf(false) }
    var found by remember { mutableStateOf<String?>(null) }
    var slow by remember { mutableStateOf(false) }
    var focus by remember { mutableStateOf<FocusRequest?>(null) }
    val stabilizer = remember { ScanStabilizer() }
    val started = remember { TimeSource.Monotonic.markNow() }
    val haptic = LocalHapticFeedback.current
    val currentAccept by rememberUpdatedState(accept)
    val currentOnResult by rememberUpdatedState(onResult)
    val scanning = access == BarcodeScannerAccess.GRANTED && !unavailable

    LaunchedEffect(scanning, found) {
        slow = false
        if (scanning && found == null) {
            delay(SlowHintAfter)
            slow = true
        }
    }
    LaunchedEffect(found) {
        val number = found ?: return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(FoundShownFor)
        currentOnResult(number)
    }

    // Слой поверх экрана: нажатия мимо кнопок не уходят под него.
    val blockTouches = remember { MutableInteractionSource() }
    CmpAdaptiveBox(
        modifier = modifier
            .fillMaxSize()
            .background(camera.camera)
            .clickable(interactionSource = blockTouches, indication = null, onClick = {})
            .testTag(BarcodeScannerTags.ROOT),
    ) { window ->
        val frame = if (window.atLeast(CmpWidthClass.Medium)) WideFrame else CompactFrame
        ScannerBody(
            access = access,
            unavailable = unavailable,
            frame = frame,
            area = DpSize(window.width, window.height),
            torch = torch,
            found = found,
            focus = focus,
            onFocus = { x, y -> focus = FocusRequest(x, y, (focus?.seq ?: 0) + 1) },
            onDetect = { texts ->
                if (found == null) {
                    val now = started.elapsedNow().inWholeMilliseconds
                    val number = texts.firstNotNullOfOrNull { text -> currentAccept(text) }
                    if (number != null && stabilizer.offer(number, now)) found = number
                }
            },
            onUnavailable = { unavailable = true },
            onRequestAccess = onRequestAccess,
            onOpenSettings = onOpenSettings,
        )
        TopControls(
            showTorch = scanning,
            torch = torch,
            onTorch = { torch = !torch },
            onClose = onClose,
        )
        BottomControls(
            hint = when {
                !scanning || found != null -> null
                slow -> BottomHint.Slow
                else -> BottomHint.Where
            },
            showManual = found == null,
            onManualInput = onManualInput,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private enum class BottomHint { Where, Slow }

@Suppress("LongParameterList") // состояние сканера и колбэки камеры/доступа
@Composable
private fun ScannerBody(
    access: BarcodeScannerAccess,
    unavailable: Boolean,
    frame: DpSize,
    area: DpSize,
    torch: Boolean,
    found: String?,
    focus: FocusRequest?,
    onFocus: (Dp, Dp) -> Unit,
    onDetect: (List<String>) -> Unit,
    onUnavailable: () -> Unit,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    when {
        access == BarcodeScannerAccess.REQUESTING -> Unit

        access == BarcodeScannerAccess.DENIED || access == BarcodeScannerAccess.BLOCKED -> {
            val blocked = access == BarcodeScannerAccess.BLOCKED
            NoCamera(
                icon = CmpIcons.NoPhotography,
                title = Strings.string.barcode_scanner_title_no_access,
                text = if (blocked) {
                    Strings.string.barcode_scanner_error_blocked
                } else {
                    Strings.string.barcode_scanner_error_denied
                },
                action = if (blocked) {
                    Triple(Strings.string.barcode_scanner_btn_settings, CmpIcons.Settings, onOpenSettings)
                } else {
                    Triple(Strings.string.barcode_scanner_btn_allow, CmpIcons.PhotoCamera, onRequestAccess)
                },
            )
        }

        unavailable -> NoCamera(
            icon = CmpIcons.VideocamOff,
            title = Strings.string.barcode_scanner_title_unavailable,
            text = Strings.string.barcode_scanner_error_unavailable,
            action = null,
        )

        else -> {
            CameraPreview(
                frame = frame,
                torch = torch,
                focus = focus,
                onDetect = onDetect,
                onUnavailable = onUnavailable,
                modifier = Modifier.fillMaxSize(),
            )
            Viewfinder(frame = frame, found = found != null)
            if (found == null) FocusArea(frame = frame, area = area, focus = focus, onFocus = onFocus)
            FoundChip(number = found, frame = frame)
        }
    }
}

@Composable
private fun TopControls(showTorch: Boolean, torch: Boolean, onTorch: () -> Unit, onClose: () -> Unit) {
    val camera = cmpDomainColors()
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundControl(
            icon = CmpIcons.Close,
            label = stringResource(Strings.string.barcode_scanner_a11y_close),
            active = false,
            onClick = onClose,
            tag = BarcodeScannerTags.CLOSE,
        )
        CmpText(
            text = stringResource(Strings.string.barcode_scanner_title),
            modifier = Modifier.weight(1f),
            color = camera.onCamera,
            style = CmpTextStyle.Headline1,
        )
        if (showTorch) {
            RoundControl(
                icon = if (torch) CmpIcons.FlashOn else CmpIcons.FlashOff,
                label = stringResource(
                    if (torch) {
                        Strings.string.barcode_scanner_a11y_torch_off
                    } else {
                        Strings.string.barcode_scanner_a11y_torch_on
                    },
                ),
                active = torch,
                onClick = onTorch,
                tag = BarcodeScannerTags.TORCH,
            )
        }
    }
}

@Composable
private fun RoundControl(icon: CmpIcons, label: String, active: Boolean, onClick: () -> Unit, tag: String) {
    val colors = CmpTheme.colors
    val camera = cmpDomainColors()
    Box(
        modifier = Modifier
            .size(ControlSize)
            .background(if (active) colors.brandPrimaryDefault else camera.cameraControl, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpIconButton(
            onClick = onClick,
            icon = icon,
            contentDescription = label,
            modifier = Modifier.testTag(tag),
            tint = if (active) colors.onBrand else camera.onCamera,
        )
    }
}

@Composable
private fun BottomControls(hint: BottomHint?, showManual: Boolean, onManualInput: () -> Unit, modifier: Modifier) {
    val colors = CmpTheme.colors
    val camera = cmpDomainColors()
    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (hint) {
            BottomHint.Slow -> CmpSurface(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.warningHighlight,
                contentColor = colors.foregroundOnDarkPrimaryDefault,
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CmpIcon(
                        icon = CmpIcons.Lightbulb,
                        contentDescription = null,
                        tint = colors.foregroundOnDarkPrimaryDefault,
                    )
                    CmpText(
                        text = stringResource(Strings.string.barcode_scanner_hint_slow),
                        color = colors.foregroundOnDarkPrimaryDefault,
                        style = CmpTextStyle.Paragraph1,
                    )
                }
            }

            BottomHint.Where -> CmpText(
                text = stringResource(Strings.string.barcode_scanner_hint_where),
                modifier = Modifier.widthIn(max = 480.dp),
                color = camera.onCameraVariant,
                textAlign = TextAlign.Center,
                style = CmpTextStyle.Paragraph1,
            )

            null -> Unit
        }
        if (showManual) {
            CmpButton(
                onClick = onManualInput,
                type = CmpButtonType.Secondary,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    .testTag(BarcodeScannerTags.MANUAL),
            ) {
                CmpIcon(icon = CmpIcons.Keyboard, contentDescription = null, tint = camera.onCamera)
                CmpText(
                    text = stringResource(Strings.string.barcode_scanner_btn_manual),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.padding(start = 8.dp),
                    color = camera.onCamera,
                )
            }
        }
    }
}

/** Затемнение вокруг рамки, уголки (зелёные — распознано) и бегущая «лазерная» линия. */
@Composable
private fun Viewfinder(frame: DpSize, found: Boolean) {
    val colors = CmpTheme.colors
    val domain = cmpDomainColors()
    val laser by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(LASER_MILLIS, easing = LinearEasing), RepeatMode.Reverse),
    )
    val cornerColor = if (found) domain.success else domain.onCamera
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = frame.width.toPx()
        val height = frame.height.toPx()
        val left = (size.width - width) / 2
        val top = (size.height - height) / 2
        val radius = FrameCorner.toPx()
        val hole = Path().apply {
            addRoundRect(RoundRect(left, top, left + width, top + height, CornerRadius(radius)))
        }
        clipPath(hole, ClipOp.Difference) { drawRect(domain.cameraScrim) }
        val stroke = Stroke(width = CornerStroke.toPx())
        val length = CornerLength.toPx()
        listOf(
            Offset(left, top) to Offset(1f, 1f),
            Offset(left + width, top) to Offset(-1f, 1f),
            Offset(left, top + height) to Offset(1f, -1f),
            Offset(left + width, top + height) to Offset(-1f, -1f),
        ).forEach { (corner, dir) ->
            val path = Path().apply {
                moveTo(corner.x, corner.y + dir.y * length)
                lineTo(corner.x, corner.y + dir.y * radius)
                quadraticTo(corner.x, corner.y, corner.x + dir.x * radius, corner.y)
                lineTo(corner.x + dir.x * length, corner.y)
            }
            drawPath(path, cornerColor, style = stroke)
        }
        if (!found) {
            val inset = height * LASER_INSET_FRACTION
            val y = top + inset + (height - 2 * inset) * laser
            drawRoundRect(
                color = colors.errorDefault,
                topLeft = Offset(left + radius, y - 1.5.dp.toPx()),
                size = Size(width - 2 * radius, 3.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
        }
    }
}

/** Тап внутри рамки — фокус камеры в эту точку; короткое кольцо показывает, куда навели. */
@Composable
private fun FocusArea(frame: DpSize, area: DpSize, focus: FocusRequest?, onFocus: (Dp, Dp) -> Unit) {
    val density = LocalDensity.current
    val left = (area.width - frame.width) / 2
    val top = (area.height - frame.height) / 2
    val currentOnFocus by rememberUpdatedState(onFocus)
    val ring = remember { Animatable(0f) }
    LaunchedEffect(focus) {
        if (focus == null) return@LaunchedEffect
        ring.snapTo(1f)
        ring.animateTo(0f, tween(FOCUS_RING_MILLIS))
    }
    val ringColor = cmpDomainColors().onCamera
    Box(
        modifier = Modifier
            .offset(x = left, y = top)
            .size(frame)
            .testTag(BarcodeScannerTags.FOCUS)
            .pointerInput(left, top) {
                detectTapGestures { tap ->
                    with(density) { currentOnFocus(left + tap.x.toDp(), top + tap.y.toDp()) }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val target = focus ?: return@Canvas
            val alpha = ring.value
            if (alpha <= 0f) return@Canvas
            val center = Offset((target.x - left).toPx(), (target.y - top).toPx())
            drawCircle(
                color = ringColor.copy(alpha = alpha),
                radius = FocusRingRadius.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

@Composable
private fun FoundChip(number: String?, frame: DpSize) {
    if (number == null) return
    val colors = CmpTheme.colors
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CmpSurface(
            modifier = Modifier.offset(y = frame.height / 2 + 52.dp).testTag(BarcodeScannerTags.FOUND),
            shape = RoundedCornerShape(16.dp),
            color = colors.backgroundSurfaceHighlight,
            contentColor = colors.foregroundOnDarkPrimaryDefault,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(
                    icon = CmpIcons.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = cmpDomainColors().success,
                )
                Column {
                    CmpText(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)) {
                                append(grouped(number))
                            }
                        },
                        style = CmpTextStyle.Headline1,
                    )
                    CmpText(
                        text = stringResource(Strings.string.barcode_scanner_msg_found),
                        color = colors.foregroundOnDarkSecondaryDefault,
                        style = CmpTextStyle.Paragraph2,
                    )
                }
            }
        }
    }
}

/** EAN-13 — как на коробке: «4 607001 234567». */
internal fun grouped(number: String): String = if (number.length == EAN13_LENGTH) {
    "${number[0]} ${number.substring(1, EAN13_HALF)} ${number.substring(EAN13_HALF)}"
} else {
    number
}

@Composable
private fun NoCamera(
    icon: CmpIcons,
    title: StringResource,
    text: StringResource,
    action: Triple<StringResource, CmpIcons, () -> Unit>?,
) {
    val camera = cmpDomainColors()
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(96.dp).background(camera.cameraControl, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = camera.onCamera)
            }
            CmpText(text = stringResource(title), color = camera.onCamera, style = CmpTextStyle.Headline1)
            CmpText(
                text = stringResource(text),
                color = camera.onCameraVariant,
                textAlign = TextAlign.Center,
                style = CmpTextStyle.Paragraph1,
            )
            action?.let { (label, actionIcon, onClick) ->
                CmpButton(
                    onClick = onClick,
                    modifier = Modifier.padding(top = 8.dp).heightIn(min = 56.dp).testTag(
                        if (actionIcon == CmpIcons.Settings) BarcodeScannerTags.SETTINGS else BarcodeScannerTags.ALLOW,
                    ),
                ) {
                    CmpIcon(icon = actionIcon, contentDescription = null)
                    CmpText(
                        text = stringResource(label),
                        style = CmpTextStyle.Paragraph2,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
