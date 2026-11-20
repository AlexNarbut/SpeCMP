package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_a11y_locked
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpActionBar
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.HandsFreeVoice
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.HandsFreeVoiceButton

private val DockButtonWidth = 300.dp
private val DockColumnWidth = 360.dp
private val HeroMaxWidth = 560.dp
private val ProgressMaxWidth = 360.dp
private val ProgressHeight = 8.dp
private const val SPIN_MILLIS = 2_400
private const val RING_MILLIS = 1_800
private const val RING_GROW = 0.5f

// Раскладку мастера (колонка шагов слева) задаёт каркас один раз — без проброса через каждый экран.
internal val LocalWizardWide = staticCompositionLocalOf { false }

internal enum class WizardNav { BACK, LOCK, NONE }

@Composable
internal fun WizardScaffold(
    title: String,
    subtitle: String?,
    nav: WizardNav,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    dock: (@Composable () -> Unit)? = null,
    overlay: @Composable () -> Unit = {},
    body: @Composable ColumnScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    CmpScaffold(
        modifier = modifier,
        topBar = { WizardTopBar(title, subtitle, nav, onBack) },
        bottomBar = { dock?.invoke() },
    ) { padding ->
        CmpBox(
            modifier = Modifier.fillMaxSize().padding(
                start = padding.calculateStartPadding(direction),
                end = padding.calculateEndPadding(direction),
                bottom = padding.calculateBottomPadding(),
            ),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val minHeight = maxHeight
                CmpColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = if (centered) minHeight else 0.dp)
                        .padding(top = padding.calculateTopPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (centered) Arrangement.Center else Arrangement.Top,
                    content = body,
                )
            }
            overlay()
        }
    }
}

@Composable
internal fun WizardTopBar(
    title: String,
    subtitle: String?,
    nav: WizardNav,
    onBack: () -> Unit,
    voice: HandsFreeVoice? = null,
) {
    CmpTopAppBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = { NavIcon(nav, onBack) },
        actions = { voice?.let { HandsFreeVoiceButton(it, Modifier.padding(end = CmpSpacing.space8)) } },
    )
}

@Composable
private fun NavIcon(nav: WizardNav, onBack: () -> Unit) {
    when (nav) {
        WizardNav.BACK -> CmpIconButton(
            onClick = onBack,
            icon = CmpIcons.ArrowBack,
            contentDescription = stringResource(Strings.string.fastmount_common_a11y_back),
        )

        WizardNav.LOCK -> {
            val a11y = stringResource(Strings.string.fastmount_common_a11y_locked)
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
        }

        WizardNav.NONE -> Unit
    }
}

@Composable
internal fun ContentColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 760.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val side = if (LocalWizardWide.current) CmpSpacing.screenPaddingHMedium else CmpSpacing.screenPaddingH
    CmpColumn(
        modifier = modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth()
            .padding(
                start = side,
                end = side,
                top = CmpSpacing.screenPaddingTop,
                bottom = CmpSpacing.screenPaddingBottom,
            ),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        content = content,
    )
}

@Composable
internal fun Dock(buttons: List<@Composable () -> Unit>, modifier: Modifier = Modifier, note: String? = null) {
    val wide = LocalWizardWide.current
    CmpActionBar(modifier = modifier) {
        if (wide) {
            CmpBox(modifier = Modifier.weight(1f)) {
                if (note != null) DockNote(note, TextAlign.Start)
            }
            buttons.asReversed().forEach { button -> CmpBox(Modifier.width(DockButtonWidth)) { button() } }
        } else {
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                buttons.forEach { it() }
                if (note != null) DockNote(note, TextAlign.Center, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DockNote(note: String, align: TextAlign, modifier: Modifier = Modifier) {
    CmpText(
        text = note,
        style = CmpTextStyle.Paragraph3,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        textAlign = align,
        modifier = modifier,
    )
}

// Панель встроенных шагов других фич стоит внутри их контента, где системный отступ снизу уже учтён:
// CmpActionBar добавил бы его второй раз.
@Composable
internal fun DockFrame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val wide = LocalWizardWide.current
    CmpColumn(modifier = modifier.fillMaxWidth().background(CmpTheme.colors.backgroundSurfaceUltraHighlight)) {
        CmpDivider()
        CmpBox(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = CmpSpacing.screenPaddingH,
                vertical = CmpSpacing.space12,
            ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            CmpColumn(
                modifier = if (wide) Modifier.width(DockColumnWidth) else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                content = content,
            )
        }
    }
}

@Composable
internal fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    enabled: Boolean = true,
    busy: Boolean = false,
    outlined: Boolean = false,
    busyText: String? = null,
) {
    CmpButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        type = if (outlined) CmpButtonType.Secondary else CmpButtonType.Primary,
        loading = busy,
        loadingText = busyText,
        icon = icon,
        fullWidth = true,
    )
}

@Composable
internal fun InfoCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: CmpIcons? = null,
    aside: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpCard(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(
                    start = CmpSpacing.space16,
                    end = CmpSpacing.space16,
                    top = CmpSpacing.space16,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                if (icon != null) {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = CmpTheme.colors.brandPrimaryDefault,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                }
                CmpText(text = title, style = CmpTextStyle.Headline3, modifier = Modifier.weight(1f))
                aside?.invoke()
            }
        }
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            content = content,
        )
    }
}

@Composable
internal fun KeyValue(label: String, value: String, modifier: Modifier = Modifier, tone: Color? = null) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpText(text = label, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkSecondaryDefault)
        CmpText(
            text = value,
            style = CmpTextStyle.Headline3,
            color = tone ?: colors.foregroundOnDarkPrimaryDefault,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun RatingLabel(rating: Int, modifier: Modifier = Modifier) {
    CmpRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        CmpIcon(
            icon = CmpIcons.Star,
            contentDescription = null,
            tint = cmpDomainColors().rating,
            modifier = Modifier.size(CmpSize.iconM),
        )
        CmpText(text = rating.toString(), style = CmpTextStyle.Headline3)
    }
}

@Composable
internal fun Hero(
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    art: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    CmpColumn(
        modifier = modifier
            .widthIn(max = HeroMaxWidth)
            .fillMaxWidth()
            .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        art()
        CmpText(text = title, style = CmpTextStyle.Headline1, textAlign = TextAlign.Center)
        if (text != null) {
            CmpText(
                text = text,
                style = CmpTextStyle.Paragraph1,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
        content()
    }
}

@Composable
internal fun RoundArt(
    icon: CmpIcons,
    modifier: Modifier = Modifier,
    tone: CmpBannerTone = CmpBannerTone.Neutral,
    size: Dp = 112.dp,
) {
    val (fill, tint) = tone.artColors()
    CmpBox(modifier = modifier.size(size).background(fill, CircleShape), contentAlignment = Alignment.Center) {
        CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * ICON_SHARE))
    }
}

@Composable
private fun CmpBannerTone.artColors(): Pair<Color, Color> {
    val colors = CmpTheme.colors
    return when (this) {
        CmpBannerTone.Neutral -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkSecondaryDefault
        CmpBannerTone.Info -> colors.infoHighlight to colors.infoDefault
        CmpBannerTone.Success -> colors.successHighlight to colors.successDefault
        CmpBannerTone.Warning -> colors.warningHighlight to colors.warningDefault
        CmpBannerTone.Error -> colors.errorHighlight to colors.errorDefault
    }
}

private const val ICON_SHARE = 0.46f

@Composable
internal fun BusyArt(icon: CmpIcons, modifier: Modifier = Modifier, spin: Boolean = false, size: Dp = 132.dp) {
    val colors = CmpTheme.colors
    val transition = rememberInfiniteTransition(label = "busyArt")
    val ring by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(RING_MILLIS), RepeatMode.Restart),
        label = "ring",
    )
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (spin) FULL_TURN else 0f,
        animationSpec = infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing)),
        label = "spin",
    )
    val outer = size + CmpSpacing.space12
    CmpBox(modifier = modifier.size(outer), contentAlignment = Alignment.Center) {
        CmpBox(
            modifier = Modifier
                .size(size)
                .scale(1f + RING_GROW * ring)
                .alpha(RING_ALPHA * (1f - ring))
                .background(colors.brandPrimaryHighlight, CircleShape),
        )
        CmpBox(modifier = Modifier.size(size).background(colors.brandPrimaryHighlight, CircleShape))
        CmpCircularProgressIndicator(modifier = Modifier.size(outer))
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.brandPrimaryDefault,
            modifier = Modifier.size(size * BUSY_ICON_SHARE).rotate(angle),
        )
    }
}

private const val FULL_TURN = 360f
private const val RING_ALPHA = 0.5f
private const val BUSY_ICON_SHARE = 0.42f

@Composable
internal fun IndeterminateBar(modifier: Modifier = Modifier) {
    CmpLinearProgressIndicator(
        modifier = modifier.widthIn(max = ProgressMaxWidth).fillMaxWidth().height(ProgressHeight),
    )
}
