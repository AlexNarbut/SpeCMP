package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_a11y_locked
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_btn_no
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_btn_yes
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_msg_dialog
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_title_dialog
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_a11y_elapsed
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepChrome

/** Класс окна (Material 3 Window Size Classes): раскладка экранов обхода и Clone. */
enum class AutostartLayout { Compact, Medium, Expanded }

private val MediumFrom = 600.dp
private val ExpandedFrom = 840.dp
private val MediumMaxWidth = 680.dp
private val ExpandedMaxWidth = 1040.dp
private val SideWidth = 360.dp
private val FooterMaxWidth = 440.dp
internal val AutostartButtonHeight = 52.dp

// Анимации макета (`bcRing`, `bcBreath`): пульс рамки у запроса быстрее, чем у ожидания.
private const val PULSE_REQUEST_MILLIS = 1_400
private const val PULSE_WAIT_MILLIS = 2_200
private const val PULSE_SCALE_FROM = 0.96f
private const val PULSE_SCALE_GROW = 0.22f
private const val PULSE_ALPHA = 0.55f
private const val BREATH_MIN_ALPHA = 0.55f
private const val BREATH_HALF_MILLIS = 1_100
private const val STATUS_FADE_MILLIS = 300
private const val TICK_MILLIS = 1_000L
private const val ILLUSTRATION_RATIO = 800f / 550f

internal fun autostartLayoutOf(width: Dp): AutostartLayout = when {
    width < MediumFrom -> AutostartLayout.Compact
    width < ExpandedFrom -> AutostartLayout.Medium
    else -> AutostartLayout.Expanded
}

/** Высота иллюстрации — одна на всех состояниях, чтобы картинка не прыгала между шагами (макет). */
internal fun illustrationHeight(layout: AutostartLayout): Dp = if (layout == AutostartLayout.Compact) 160.dp else 210.dp

/**
 * Каркас экрана: шапка «Название · Модель ID», полоса статуса (место зарезервировано всегда, во время процесса
 * плавно появляется), прокручиваемое тело; на Compact кнопки закреплены внизу, на Expanded — боковая колонка.
 *
 * @param status содержимое полосы статуса; `null` — полоса пустая (гаснет).
 * @param locked вместо «Назад» — замок (выход недоступен).
 */
@Composable
internal fun AutostartScaffold(
    title: String,
    subtitle: String?,
    locked: Boolean,
    onBack: () -> Unit,
    status: (@Composable () -> Unit)?,
    main: @Composable ColumnScope.(AutostartLayout) -> Unit,
    modifier: Modifier = Modifier,
    side: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    chrome: AutostartStepChrome? = null
) {
    val colors = cmpThemeColors()
    CmpScaffold(
        modifier = modifier,
        topBar = {
            if (chrome != null) {
                chrome.topBar(locked, onBack)
            } else {
                CmpTopAppBar(
                    title = { CmpText(text = title) },
                    subtitle = subtitle?.let { { CmpText(text = it, style = CmpTextStyle.Caption) } },
                    navigationIcon = {
                        if (locked) {
                            val a11y = stringResource(Strings.string.autostart_common_a11y_locked)
                            CmpBox(
                                modifier = Modifier.size(48.dp).semantics { contentDescription = a11y },
                                contentAlignment = Alignment.Center
                            ) {
                                CmpIcon(icon = CmpIcons.Lock, contentDescription = null, tint = colors.onSurfaceVariant)
                            }
                        } else {
                            CmpIconButton(
                                onClick = onBack,
                                icon = CmpIcons.ArrowBack,
                                contentDescription = stringResource(Strings.string.autostart_common_a11y_back)
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = autostartLayoutOf(maxWidth)
            // Шаг мастера: кнопки всегда в панели мастера внизу (как на остальных его шагах).
            val compact = layout == AutostartLayout.Compact || chrome != null
            CmpColumn(modifier = Modifier.fillMaxSize()) {
                StatusStrip(layout = layout, content = status)
                CmpColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Body(layout = layout, main = main, side = side, footer = footer.takeUnless { compact })
                }
                if (chrome != null && footer != null) {
                    chrome.dock(footer)
                } else if (compact && footer != null) {
                    CmpSurface(color = colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                        CmpColumn(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            content = footer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Body(
    layout: AutostartLayout,
    main: @Composable ColumnScope.(AutostartLayout) -> Unit,
    side: (@Composable ColumnScope.() -> Unit)?,
    footer: (@Composable ColumnScope.() -> Unit)?
) {
    val gap = if (layout == AutostartLayout.Compact) 16.dp else 20.dp
    val column: @Composable (Modifier) -> Unit = { columnModifier ->
        CmpColumn(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            main(layout)
            if (footer != null) {
                CmpColumn(
                    modifier = Modifier.widthIn(max = FooterMaxWidth).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = footer
                )
            }
            if (side != null && layout != AutostartLayout.Expanded) side()
        }
    }
    when (layout) {
        AutostartLayout.Compact -> column(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        )

        AutostartLayout.Medium -> column(Modifier.widthIn(max = MediumMaxWidth).fillMaxWidth().padding(24.dp))

        AutostartLayout.Expanded -> CmpRow(
            modifier = Modifier.widthIn(
                max = ExpandedMaxWidth
            ).fillMaxWidth().padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            column(Modifier.weight(1f))
            if (side != null) {
                CmpColumn(
                    modifier = Modifier.width(SideWidth),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = side
                )
            }
        }
    }
}

/** Полоса статуса под шапкой: высота зарезервирована, содержимое плавно появляется и гаснет — контент не прыгает. */
@Composable
private fun StatusStrip(layout: AutostartLayout, content: (@Composable () -> Unit)?) {
    val colors = cmpThemeColors()
    val shown = content != null
    val fade = tween<Float>(STATUS_FADE_MILLIS, easing = EaseInOut)
    val alpha by animateFloatAsState(if (shown) 1f else 0f, fade, label = "statusAlpha")
    val background by animateColorAsState(
        // К прозрачному тому же цвету, а не к Color.Transparent (прозрачный чёрный): иначе полоса темнеет, пока гаснет.
        if (shown) colors.surfaceContainerLow else colors.surfaceContainerLow.copy(alpha = 0f),
        tween(STATUS_FADE_MILLIS, easing = EaseInOut),
        label = "statusBackground"
    )
    val divider by animateColorAsState(
        if (shown) colors.outlineVariant else colors.outlineVariant.copy(alpha = 0f),
        tween(STATUS_FADE_MILLIS, easing = EaseInOut),
        label = "statusDivider"
    )
    // Пока полоса гаснет, в ней остаётся последнее содержимое (макет: `lastStatus`), а не пустота.
    var last by remember { mutableStateOf(content) }
    if (content != null) last = content
    CmpColumn(modifier = Modifier.fillMaxWidth().background(background)) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (layout == AutostartLayout.Compact) 63.dp else 67.dp)
                .padding(horizontal = if (layout == AutostartLayout.Compact) 16.dp else 32.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            CmpBox(modifier = Modifier.widthIn(max = 976.dp).fillMaxWidth().alpha(alpha)) { last?.invoke() }
        }
        CmpBox(modifier = Modifier.fillMaxWidth().height(1.dp).background(divider))
    }
}

/** Индикатор, фраза и таймер «m:ss» с начала текущего шага ([key] — смена шага сбрасывает таймер). */
@Composable
internal fun StatusRow(text: String, key: Any, modifier: Modifier = Modifier, startSeconds: Int = 0) {
    val colors = cmpThemeColors()
    var seconds by remember(key) { mutableIntStateOf(startSeconds) }
    LaunchedEffect(key) {
        while (true) {
            delay(TICK_MILLIS)
            seconds += 1
        }
    }
    val elapsed = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    val a11y = stringResource(Strings.string.autostart_status_a11y_elapsed, elapsed)
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(8.dp))
        CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CmpText(
                text = text,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            CmpText(
                text = elapsed,
                color = colors.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { contentDescription = a11y }
            )
        }
    }
}

/** Тон плашки. */
internal enum class BannerTone { Warn, Info, Error }

/** Плашка: иконка, заголовок, текст (макет `Banner`). */
@Composable
internal fun InfoBanner(
    icon: CmpIcons,
    tone: BannerTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null
) {
    val colors = cmpThemeColors()
    val (bg, fg) = when (tone) {
        BannerTone.Warn -> colors.tertiaryContainer to colors.onTertiaryContainer
        BannerTone.Info -> colors.secondaryContainer to colors.onSecondaryContainer
        BannerTone.Error -> colors.errorContainer to colors.onErrorContainer
    }
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = fg, modifier = Modifier.size(26.dp))
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (title != null) CmpText(text = title, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (text != null) CmpText(text = text, color = fg, style = CmpTextStyle.Body)
        }
    }
}

/** «Как это будет»: пункты с иконкой. */
@Composable
internal fun HowItWorks(items: List<Pair<CmpIcons, String>>, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, RoundedCornerShape(20.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items.forEachIndexed { index, (icon, text) ->
            if (index > 0) CmpBox(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant))
            CmpRow(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpBox(
                    modifier = Modifier.size(40.dp).background(colors.secondaryContainer, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = colors.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                CmpText(text = text, style = CmpTextStyle.Body, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Режим рамки иллюстрации: запрос действия (рамка + пульс), выполнено, ожидание («дыхание»), покой. */
internal enum class IllustrationMode { Request, Done, Wait, Idle }

/** Иллюстрация в рамке одного размера на всех состояниях (макет `Illus`). */
@Composable
internal fun IllustrationFrame(
    illustration: AutostartIllustration,
    layout: AutostartLayout,
    modifier: Modifier = Modifier,
    mode: IllustrationMode = IllustrationMode.Idle
) {
    val colors = cmpThemeColors()
    val shape = RoundedCornerShape(32.dp)
    val transition = rememberInfiniteTransition(label = "illustration")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(
                if (mode ==
                    IllustrationMode.Request
                ) {
                    PULSE_REQUEST_MILLIS
                } else {
                    PULSE_WAIT_MILLIS
                },
                easing = EaseOut
            ),
            RepeatMode.Restart
        ),
        label = "pulse"
    )
    val breath by transition.animateFloat(
        initialValue = 1f,
        targetValue = BREATH_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(BREATH_HALF_MILLIS, easing = EaseInOut), RepeatMode.Reverse),
        label = "breath"
    )
    val request = mode == IllustrationMode.Request
    val animated = request || mode == IllustrationMode.Wait
    val height = illustrationHeight(layout)
    CmpBox(modifier = modifier.height(height).aspectRatio(ILLUSTRATION_RATIO), contentAlignment = Alignment.Center) {
        if (animated) {
            CmpBox(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(PULSE_SCALE_FROM + PULSE_SCALE_GROW * pulse)
                    .alpha(PULSE_ALPHA * (1f - pulse))
                    .border(BorderStroke(3.dp, colors.primary), shape)
            )
        }
        AutostartIllustrationLayers(
            illustration = illustration,
            ink = when (mode) {
                IllustrationMode.Request -> colors.onPrimaryContainer
                IllustrationMode.Done -> colors.onSurface
                else -> colors.onSurfaceVariant
            },
            accent = colors.primary,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (mode == IllustrationMode.Wait) breath else 1f)
                .background(if (request) colors.primaryContainer else colors.surfaceContainerLow, shape)
                .border(if (request) 3.dp else 1.dp, if (request) colors.primary else colors.outlineVariant, shape)
                .padding(6.dp)
        )
    }
}

/** Заголовок и подзаголовок по центру (старт, итог, этап Clone). */
@Composable
internal fun Headline(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = modifier.widthIn(max = 560.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CmpText(text = title, fontSize = 24.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
        if (subtitle != null) {
            CmpText(
                text = subtitle,
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** «Прервать?» — выход во время процесса (текст макета). */
@Composable
internal fun ExitDialog(onStay: () -> Unit, onExit: () -> Unit) {
    CmpDialog(
        onDismissRequest = onStay,
        modifier = Modifier.widthIn(max = 440.dp),
        icon = CmpIcons.Warning.imageVector(),
        title = { CmpText(text = stringResource(Strings.string.autostart_exit_title_dialog)) },
        text = { CmpText(text = stringResource(Strings.string.autostart_exit_msg_dialog), style = CmpTextStyle.Body) },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.autostart_exit_btn_yes),
                onClick = onExit,
                modifier = Modifier.height(48.dp)
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.autostart_exit_btn_no),
                onClick = onStay,
                modifier = Modifier.height(48.dp)
            )
        }
    )
}

private const val STEP_IN_MILLIS = 250
private val StepInOffset = 8.dp

/**
 * Появление нового шага (макет `bcIn`: прозрачность 0 → 1 и подъём на 8dp за 250 мс). Прежний шаг уходит сразу —
 * как в макете, где карточка пересоздаётся по ключу шага.
 */
@Composable
internal fun <K> StepAppear(stepKey: K, modifier: Modifier = Modifier, content: @Composable ColumnScope.(K) -> Unit) {
    val density = LocalDensity.current
    val offset = with(density) { StepInOffset.roundToPx() }
    AnimatedContent(
        targetState = stepKey,
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            (
                fadeIn(tween(STEP_IN_MILLIS, easing = EaseInOut)) + slideInVertically(tween(STEP_IN_MILLIS)) {
                    offset
                }
                ) togetherWith
                ExitTransition.None using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.TopCenter,
        label = "stepAppear"
    ) { key ->
        CmpColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content(key) }
    }
}
