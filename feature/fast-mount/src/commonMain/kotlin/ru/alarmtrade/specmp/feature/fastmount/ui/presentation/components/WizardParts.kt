// Раскладка всех экранов мастера (колонка шагов слева) задаётся каркасом один раз — без проброса через каждый экран.
@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_a11y_locked
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal val MainButtonHeight = 56.dp
private val DockButtonWidth = 300.dp
private const val SPIN_MILLIS = 2_400
private const val RING_MILLIS = 1_800
private const val RING_GROW = 0.5f

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
    CmpScaffold(
        modifier = modifier,
        topBar = { WizardTopBar(title, subtitle, nav, onBack) },
        bottomBar = { dock?.invoke() },
    ) { padding ->
        CmpBox(modifier = Modifier.fillMaxSize().padding(padding)) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val minHeight = maxHeight
                CmpColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = if (centered) minHeight else 0.dp),
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
internal fun WizardTopBar(title: String, subtitle: String?, nav: WizardNav, onBack: () -> Unit) {
    CmpTopAppBar(
        title = { CmpText(text = title, maxLines = 1) },
        subtitle = subtitle?.let { { CmpText(text = it, style = CmpTextStyle.Caption, maxLines = 1) } },
        navigationIcon = { NavIcon(nav, onBack) },
    )
}

@Composable
private fun NavIcon(nav: WizardNav, onBack: () -> Unit) {
    val colors = cmpThemeColors()
    when (nav) {
        WizardNav.BACK -> CmpIconButton(
            onClick = onBack,
            icon = CmpIcons.ArrowBack,
            contentDescription = stringResource(Strings.string.fastmount_common_a11y_back),
        )

        WizardNav.LOCK -> {
            val a11y = stringResource(Strings.string.fastmount_common_a11y_locked)
            CmpBox(
                modifier = Modifier.size(48.dp).semantics { contentDescription = a11y },
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(icon = CmpIcons.Lock, contentDescription = null, tint = colors.onSurfaceVariant)
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
    val wide = LocalWizardWide.current
    CmpColumn(
        modifier = modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth()
            .padding(
                start = if (wide) 24.dp else 16.dp,
                end = if (wide) 24.dp else 16.dp,
                top = if (wide) 20.dp else 16.dp,
                bottom = if (wide) 28.dp else 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
internal fun Dock(buttons: List<@Composable () -> Unit>, modifier: Modifier = Modifier, note: String? = null) {
    val colors = cmpThemeColors()
    val wide = LocalWizardWide.current
    CmpSurface(modifier = modifier.fillMaxWidth(), color = colors.surfaceContainer) {
        CmpColumn(modifier = Modifier.fillMaxWidth()) {
            CmpBox(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant))
            if (wide) {
                CmpRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CmpBox(modifier = Modifier.weight(1f)) {
                        if (note != null) {
                            CmpText(text = note, style = CmpTextStyle.Body, color = colors.onSurfaceVariant)
                        }
                    }
                    buttons.asReversed().forEach { button -> CmpBox(Modifier.width(DockButtonWidth)) { button() } }
                }
            } else {
                CmpColumn(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    buttons.forEach { it() }
                    if (note != null) {
                        CmpText(
                            text = note,
                            style = CmpTextStyle.Body,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DockFrame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = cmpThemeColors()
    val wide = LocalWizardWide.current
    CmpSurface(modifier = modifier.fillMaxWidth(), color = colors.surfaceContainer) {
        CmpColumn(modifier = Modifier.fillMaxWidth()) {
            CmpBox(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant))
            CmpBox(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                CmpColumn(
                    modifier = if (wide) Modifier.width(DockColumnWidth) else Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content,
                )
            }
        }
    }
}

private val DockColumnWidth = 360.dp

@Composable
internal fun MainButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    enabled: Boolean = true,
    busy: Boolean = false,
    busyText: String? = null,
    outlined: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (busy) {
                CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
            } else if (icon != null) {
                CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            CmpText(text = if (busy) busyText ?: text else text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
    val buttonModifier = modifier.fillMaxWidth().height(MainButtonHeight)
    if (outlined) {
        CmpOutlinedButton(onClick = onClick, modifier = buttonModifier, enabled = enabled && !busy) { content() }
    } else {
        CmpButton(onClick = onClick, modifier = buttonModifier, enabled = enabled && !busy) { content() }
    }
}

internal enum class BannerTone { WARN, ERROR, INFO, SUCCESS }

@Composable
internal fun Banner(
    tone: BannerTone,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    title: String? = null,
    text: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = cmpThemeColors()
    val (bg, fg, defaultIcon) = when (tone) {
        BannerTone.WARN -> Triple(colors.tertiaryContainer, colors.onTertiaryContainer, CmpIcons.Warning)
        BannerTone.ERROR -> Triple(colors.errorContainer, colors.onErrorContainer, CmpIcons.ErrorOutline)
        BannerTone.INFO -> Triple(colors.secondaryContainer, colors.onSecondaryContainer, CmpIcons.Info)
        BannerTone.SUCCESS -> Triple(successContainer(), colors.onSurface, CmpIcons.CheckCircle)
    }
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(18.dp))
            .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(icon = icon ?: defaultIcon, contentDescription = null, tint = fg, modifier = Modifier.size(26.dp))
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (title != null) CmpText(text = title, color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (text != null) CmpText(text = text, color = fg, style = CmpTextStyle.BodyLarge)
            if (action != null) {
                CmpOutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 6.dp).heightIn(min = 48.dp),
                ) {
                    CmpText(text = action, color = fg, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun InfoCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: CmpIcons? = null,
    aside: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = cmpThemeColors()
    val shape = RoundedCornerShape(20.dp)
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, shape)
            .border(BorderStroke(1.dp, colors.outlineVariant), shape),
    ) {
        if (title != null) {
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (icon != null) {
                    CmpIcon(
                        icon = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                CmpText(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                aside?.invoke()
            }
        }
        CmpColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), content = content)
    }
}

@Composable
internal fun KeyValue(label: String, value: String, modifier: Modifier = Modifier, tone: Color? = null) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = modifier.fillMaxWidth().heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpText(text = label, style = CmpTextStyle.BodyLarge, color = colors.onSurfaceVariant)
        CmpText(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = tone ?: colors.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun Tag(text: String, modifier: Modifier = Modifier, icon: CmpIcons? = null, tone: BannerTone? = null) {
    val colors = cmpThemeColors()
    val (bg, fg) = when (tone) {
        BannerTone.ERROR -> colors.errorContainer to colors.onErrorContainer
        BannerTone.WARN -> colors.tertiaryContainer to colors.onTertiaryContainer
        BannerTone.INFO -> colors.secondaryContainer to colors.onSecondaryContainer
        BannerTone.SUCCESS -> successContainer() to colors.onSurface
        null -> colors.surfaceContainerHighest to colors.onSurface
    }
    CmpRow(
        modifier = modifier
            .heightIn(min = 32.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) CmpIcon(icon = icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        CmpText(text = text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun RatingLabel(rating: Int, modifier: Modifier = Modifier) {
    CmpRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CmpIcon(
            icon = CmpIcons.Star,
            contentDescription = null,
            tint = cmpDomainColors().rating,
            modifier = Modifier.size(20.dp),
        )
        CmpText(text = rating.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    val colors = cmpThemeColors()
    CmpColumn(
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        art()
        CmpText(text = title, fontSize = 24.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        if (text != null) {
            CmpText(
                text = text,
                style = CmpTextStyle.BodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        content()
    }
}

@Composable
internal fun RoundArt(icon: CmpIcons, modifier: Modifier = Modifier, tone: BannerTone? = null, size: Dp = 112.dp) {
    val colors = cmpThemeColors()
    val (bg, fg) = when (tone) {
        BannerTone.ERROR -> colors.errorContainer to colors.onErrorContainer
        BannerTone.WARN -> colors.tertiaryContainer to colors.onTertiaryContainer
        BannerTone.SUCCESS -> successContainer() to colors.onSurface
        BannerTone.INFO, null -> colors.surfaceContainerHigh to colors.onSurfaceVariant
    }
    CmpBox(modifier = modifier.size(size).background(bg, CircleShape), contentAlignment = Alignment.Center) {
        CmpIcon(icon = icon, contentDescription = null, tint = fg, modifier = Modifier.size(size * ICON_SHARE))
    }
}

private const val ICON_SHARE = 0.46f

@Composable
internal fun BusyArt(icon: CmpIcons, modifier: Modifier = Modifier, spin: Boolean = false, size: Dp = 132.dp) {
    val colors = cmpThemeColors()
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
    CmpBox(modifier = modifier.size(size + 12.dp), contentAlignment = Alignment.Center) {
        CmpBox(
            modifier = Modifier
                .size(size)
                .scale(1f + RING_GROW * ring)
                .alpha(RING_ALPHA * (1f - ring))
                .background(colors.primaryContainer, CircleShape),
        )
        CmpBox(modifier = Modifier.size(size).background(colors.primaryContainer, CircleShape))
        CmpCircularProgressIndicator(modifier = Modifier.size(size + 12.dp), strokeWidth = 4.dp)
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(size * BUSY_ICON_SHARE).rotate(angle),
        )
    }
}

private const val FULL_TURN = 360f
private const val RING_ALPHA = 0.5f
private const val BUSY_ICON_SHARE = 0.42f

@Composable
internal fun successContainer(): Color =
    lerp(cmpThemeColors().surfaceContainerHigh, cmpDomainColors().success, SUCCESS_TINT)

private const val SUCCESS_TINT = 0.2f

@Composable
internal fun IndeterminateBar(modifier: Modifier = Modifier) {
    CmpLinearProgressIndicator(modifier = modifier.widthIn(max = 360.dp).fillMaxWidth().height(8.dp))
}
