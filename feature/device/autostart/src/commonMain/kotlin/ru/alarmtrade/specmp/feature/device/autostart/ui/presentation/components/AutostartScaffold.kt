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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.autostart_common_a11y_locked
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_btn_no
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_btn_yes
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_msg_dialog
import ru.alarmtrade.specmp.core.localization.resources.autostart_exit_title_dialog
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_a11y_elapsed
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step.AutostartStepChrome
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.HandsFreeVoice
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.HandsFreeVoiceButton

enum class AutostartLayout { Compact, Medium, Expanded }

private val MediumMaxWidth = 680.dp
private val ExpandedMaxWidth = 1040.dp
private val SideWidth = 360.dp
private val FooterMaxWidth = 440.dp
private val StatusHeightCompact = 64.dp
private val StatusHeight = 68.dp
private val StatusMaxWidth = 976.dp
private val StatusProgressHeight = 8.dp
private val HowIconBox = 40.dp
private val IllustrationRadius = 32.dp
private val IllustrationInset = 6.dp
private val HeadlineMaxWidth = 560.dp
internal val AccentBorder = 3.dp

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
    width < CmpBreakpoints.Medium -> AutostartLayout.Compact
    width < CmpBreakpoints.Expanded -> AutostartLayout.Medium
    else -> AutostartLayout.Expanded
}

/** Высота иллюстрации — одна на всех состояниях, чтобы картинка не прыгала между шагами (макет). */
internal fun illustrationHeight(layout: AutostartLayout): Dp = if (layout == AutostartLayout.Compact) 160.dp else 210.dp

/**
 * @param status `null` — полоса гаснет (место под неё зарезервировано всегда).
 * @param locked вместо «Назад» — замок (выход недоступен).
 * @param voice кнопка «Озвучка» в своей шапке; в мастере ([chrome]) она в шапке мастера.
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
    chrome: AutostartStepChrome? = null,
    voice: HandsFreeVoice? = null,
) {
    val colors = CmpTheme.colors
    CmpScaffold(
        modifier = modifier,
        topBar = {
            if (chrome != null) {
                chrome.topBar(locked, onBack)
            } else {
                CmpTopAppBar(
                    title = title,
                    subtitle = subtitle,
                    navigationIcon = { NavigationIcon(locked = locked, onBack = onBack) },
                    actions = { voice?.let { HandsFreeVoiceButton(it, Modifier.padding(end = CmpSpacing.space8)) } },
                )
            }
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = autostartLayoutOf(maxWidth)
            val compact = layout == AutostartLayout.Compact || chrome != null
            CmpColumn(modifier = Modifier.fillMaxSize()) {
                StatusStrip(layout = layout, content = status)
                CmpColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Body(layout = layout, main = main, side = side, footer = footer.takeUnless { compact })
                }
                if (chrome != null && footer != null) {
                    chrome.dock(footer)
                } else if (compact && footer != null) {
                    CmpColumn(modifier = Modifier.fillMaxWidth().background(colors.backgroundSurfaceCard)) {
                        CmpDivider()
                        CmpColumn(
                            modifier = Modifier.padding(
                                start = CmpSpacing.screenPaddingH,
                                end = CmpSpacing.screenPaddingH,
                                top = CmpSpacing.space12,
                                bottom = CmpSpacing.space16,
                            ),
                            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                            content = footer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationIcon(locked: Boolean, onBack: () -> Unit) {
    if (locked) {
        val a11y = stringResource(Strings.string.autostart_common_a11y_locked)
        CmpBox(
            modifier = Modifier.size(CmpSize.touchMin).semantics { contentDescription = a11y },
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Lock,
                contentDescription = null,
                tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
    } else {
        CmpIconButton(
            onClick = onBack,
            icon = CmpIcons.ArrowBack,
            contentDescription = stringResource(Strings.string.autostart_common_a11y_back),
        )
    }
}

@Composable
private fun Body(
    layout: AutostartLayout,
    main: @Composable ColumnScope.(AutostartLayout) -> Unit,
    side: (@Composable ColumnScope.() -> Unit)?,
    footer: (@Composable ColumnScope.() -> Unit)?,
) {
    val gap = if (layout == AutostartLayout.Compact) CmpSpacing.space16 else CmpSpacing.space20
    val column: @Composable (Modifier) -> Unit = { columnModifier ->
        CmpColumn(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            main(layout)
            if (footer != null) {
                CmpColumn(
                    modifier = Modifier.widthIn(max = FooterMaxWidth).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                    content = footer,
                )
            }
            if (side != null && layout != AutostartLayout.Expanded) side()
        }
    }
    when (layout) {
        AutostartLayout.Compact -> column(
            Modifier.fillMaxWidth().padding(
                start = CmpSpacing.screenPaddingH,
                end = CmpSpacing.screenPaddingH,
                top = CmpSpacing.screenPaddingTop,
                bottom = CmpSpacing.screenPaddingBottom,
            ),
        )

        AutostartLayout.Medium -> column(
            Modifier
                .widthIn(max = MediumMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.screenPaddingHMedium, vertical = CmpSpacing.space24),
        )

        AutostartLayout.Expanded -> CmpRow(
            modifier = Modifier
                .widthIn(max = ExpandedMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.screenPaddingHMedium, vertical = CmpSpacing.space24),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space32),
        ) {
            column(Modifier.weight(1f))
            if (side != null) {
                CmpColumn(
                    modifier = Modifier.width(SideWidth),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                    content = side,
                )
            }
        }
    }
}

@Composable
private fun StatusStrip(layout: AutostartLayout, content: (@Composable () -> Unit)?) {
    val colors = CmpTheme.colors
    val shown = content != null
    val fade = tween<Float>(STATUS_FADE_MILLIS, easing = EaseInOut)
    val alpha by animateFloatAsState(if (shown) 1f else 0f, fade, label = "statusAlpha")
    val background by animateColorAsState(
        // К прозрачному тому же цвету, а не к Color.Transparent (прозрачный чёрный): иначе полоса темнеет, пока гаснет.
        if (shown) colors.backgroundSurfaceCard else colors.backgroundSurfaceCard.copy(alpha = 0f),
        tween(STATUS_FADE_MILLIS, easing = EaseInOut),
        label = "statusBackground",
    )
    val divider by animateColorAsState(
        if (shown) colors.borderHighlight else colors.borderHighlight.copy(alpha = 0f),
        tween(STATUS_FADE_MILLIS, easing = EaseInOut),
        label = "statusDivider",
    )
    // Пока полоса гаснет, в ней остаётся последнее содержимое (макет: `lastStatus`), а не пустота.
    var last by remember { mutableStateOf(content) }
    if (content != null) last = content
    val compact = layout == AutostartLayout.Compact
    CmpColumn(modifier = Modifier.fillMaxWidth().background(background)) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) StatusHeightCompact else StatusHeight)
                .padding(
                    horizontal = if (compact) CmpSpacing.screenPaddingH else CmpSpacing.screenPaddingHMedium,
                    vertical = CmpSpacing.space12,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CmpBox(modifier = Modifier.widthIn(max = StatusMaxWidth).fillMaxWidth().alpha(alpha)) { last?.invoke() }
        }
        CmpDivider(color = divider)
    }
}

@Composable
internal fun StatusRow(text: String, key: Any, modifier: Modifier = Modifier, startSeconds: Int = 0) {
    val colors = CmpTheme.colors
    var seconds by remember(key) { mutableIntStateOf(startSeconds) }
    LaunchedEffect(key) {
        while (isActive) {
            delay(TICK_MILLIS)
            seconds += 1
        }
    }
    val elapsed = "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
    val a11y = stringResource(Strings.string.autostart_status_a11y_elapsed, elapsed)
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(StatusProgressHeight))
        CmpRow(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CmpText(
                text = text,
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            CmpText(
                text = elapsed,
                style = CmpTextStyle.Headline3,
                color = colors.foregroundOnDarkSecondaryDefault,
                modifier = Modifier.semantics { contentDescription = a11y },
            )
        }
    }
}

@Composable
internal fun HowItWorks(items: List<Pair<CmpIcons, String>>, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
    ) {
        items.forEachIndexed { index, (icon, text) ->
            if (index > 0) CmpDivider()
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CmpSize.iconXxl)
                    .padding(vertical = CmpSpacing.space8),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpBox(
                    modifier = Modifier
                        .size(HowIconBox)
                        .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.cardSmall)),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = colors.foregroundOnDarkPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                }
                CmpText(text = text, style = CmpTextStyle.Paragraph2, modifier = Modifier.weight(1f))
            }
        }
    }
}

internal enum class IllustrationMode { Request, Done, Wait, Idle }

@Composable
internal fun IllustrationFrame(
    illustration: AutostartIllustration,
    layout: AutostartLayout,
    modifier: Modifier = Modifier,
    mode: IllustrationMode = IllustrationMode.Idle,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(IllustrationRadius)
    val transition = rememberInfiniteTransition(label = "illustration")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(
                if (mode == IllustrationMode.Request) PULSE_REQUEST_MILLIS else PULSE_WAIT_MILLIS,
                easing = EaseOut,
            ),
            RepeatMode.Restart,
        ),
        label = "pulse",
    )
    val breath by transition.animateFloat(
        initialValue = 1f,
        targetValue = BREATH_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(BREATH_HALF_MILLIS, easing = EaseInOut), RepeatMode.Reverse),
        label = "breath",
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
                    .border(BorderStroke(AccentBorder, colors.brandPrimaryDefault), shape),
            )
        }
        AutostartIllustrationLayers(
            illustration = illustration,
            ink = when (mode) {
                IllustrationMode.Request, IllustrationMode.Done -> colors.foregroundOnDarkPrimaryDefault
                else -> colors.foregroundOnDarkSecondaryDefault
            },
            accent = colors.brandPrimaryDefault,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (mode == IllustrationMode.Wait) breath else 1f)
                .background(if (request) colors.brandPrimaryHighlight else colors.backgroundSurfaceCard, shape)
                .border(
                    if (request) AccentBorder else CmpBorderWidth.listItem,
                    if (request) colors.brandPrimaryDefault else colors.borderHighlight,
                    shape,
                )
                .padding(IllustrationInset),
        )
    }
}

@Composable
internal fun Headline(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    CmpColumn(
        modifier = modifier.widthIn(max = HeadlineMaxWidth).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpText(text = title, style = CmpTextStyle.Headline1, textAlign = TextAlign.Center)
        if (subtitle != null) {
            CmpText(
                text = subtitle,
                style = CmpTextStyle.Paragraph2,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun AutostartDialog(
    icon: CmpIcons,
    title: String,
    text: String,
    dismissText: String,
    onDismissClick: () -> Unit,
    confirmText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmModifier: Modifier = Modifier,
) {
    CmpDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(max = FooterMaxWidth),
        icon = icon.imageVector(),
        title = { CmpText(text = title, style = CmpTextStyle.Headline1) },
        text = { CmpText(text = text, style = CmpTextStyle.Paragraph2) },
        dismissButton = {
            CmpButton(
                text = dismissText,
                onClick = onDismissClick,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            CmpButton(text = confirmText, onClick = onConfirmClick, size = CmpButtonSize.S, modifier = confirmModifier)
        },
    )
}

@Composable
internal fun ExitDialog(onStay: () -> Unit, onExit: () -> Unit) {
    AutostartDialog(
        icon = CmpIcons.Warning,
        title = stringResource(Strings.string.autostart_exit_title_dialog),
        text = stringResource(Strings.string.autostart_exit_msg_dialog),
        dismissText = stringResource(Strings.string.autostart_exit_btn_yes),
        onDismissClick = onExit,
        confirmText = stringResource(Strings.string.autostart_exit_btn_no),
        onConfirmClick = onStay,
        onDismissRequest = onStay,
    )
}

private const val STEP_IN_MILLIS = 250
private val StepInOffset = 8.dp

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
        label = "stepAppear",
    ) { key ->
        CmpColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { content(key) }
    }
}
