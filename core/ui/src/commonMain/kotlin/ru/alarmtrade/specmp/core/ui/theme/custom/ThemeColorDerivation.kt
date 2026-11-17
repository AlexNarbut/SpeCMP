package ru.alarmtrade.specmp.core.ui.theme.custom

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Background
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Card
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Error
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Info
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Ink
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Ink2
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.OnPrimary
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Outline
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Primary
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Success
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Warning
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColors

/**
 * Токены из палитры. Цвет, который пользователь не менял, отдаёт CSS-токен как есть — тема по умолчанию
 * совпадает с дизайн-системой точь-в-точь; изменённый перекрашивает свои токены, сохраняя их прозрачность.
 */
internal class ThemeColorDerivation(private val palette: ThemePalette, private val isDark: Boolean) {
    private val defaults = ThemePreset.Default.mode(isDark).palette
    private val css: CmpColors = if (isDark) DarkCmpColors else LightCmpColors
    private val cssScheme: ColorScheme = if (isDark) DarkCmpColorScheme else LightCmpColorScheme

    private fun changed(vararg roles: ThemeColorRole) = roles.any { palette[it] != defaults[it] }

    private fun tint(token: Color, role: ThemeColorRole): Color =
        if (changed(role)) palette[role].toColor().copy(alpha = token.alpha) else token

    private fun solid(token: Color, role: ThemeColorRole): Color = if (changed(role)) palette[role].toColor() else token

    private fun shade(token: Color, role: ThemeColorRole, fraction: Float, toward: ThemeColor): Color =
        if (changed(role)) palette[role].mix(toward, fraction).toColor() else token

    private val lighter = if (isDark) ThemeColor.White else ThemeColor.Black
    private val onInkFill = if (isDark) Background else Card

    @Suppress("LongMethod")
    fun colors(): CmpColors = CmpColors(
        brandPrimaryDefault = solid(css.brandPrimaryDefault, Primary),
        brandPrimaryHover = shade(css.brandPrimaryHover, Primary, HOVER, lighter),
        brandPrimaryPressed = shade(css.brandPrimaryPressed, Primary, PRESSED, ThemeColor.Black),
        brandPrimaryDisabled = tint(css.brandPrimaryDisabled, Primary),
        brandPrimaryHighlight = tint(css.brandPrimaryHighlight, Primary),
        brandSecondaryDefault = solid(css.brandSecondaryDefault, Primary),
        onBrand = solid(css.onBrand, OnPrimary),
        foregroundOnDarkPrimaryDefault = tint(css.foregroundOnDarkPrimaryDefault, Ink),
        foregroundOnDarkPrimaryPressed = tint(css.foregroundOnDarkPrimaryPressed, Ink),
        foregroundOnDarkPrimaryDisabled = tint(css.foregroundOnDarkPrimaryDisabled, Ink),
        foregroundOnDarkSecondaryDefault = if (changed(Ink2)) {
            solid(css.foregroundOnDarkSecondaryDefault, Ink2)
        } else {
            tint(css.foregroundOnDarkSecondaryDefault, Ink)
        },
        foregroundOnDarkTertiary = tint(css.foregroundOnDarkTertiary, Ink),
        foregroundOnLightPrimaryDefault = tint(css.foregroundOnLightPrimaryDefault, onInkFill),
        foregroundOnLightSecondaryDefault = tint(css.foregroundOnLightSecondaryDefault, onInkFill),
        backgroundPagePrimary = solid(css.backgroundPagePrimary, Background),
        backgroundSurfacePrimary = tint(css.backgroundSurfacePrimary, Ink),
        backgroundSurfaceSemi = tint(css.backgroundSurfaceSemi, Ink),
        backgroundSurfaceSecondary = tint(css.backgroundSurfaceSecondary, Ink),
        backgroundSurfaceTertiary = tint(css.backgroundSurfaceTertiary, Ink),
        backgroundSurfaceLight = tint(css.backgroundSurfaceLight, Ink),
        backgroundSurfaceMediumHighlight = tint(css.backgroundSurfaceMediumHighlight, Ink),
        backgroundSurfaceHighlight = tint(css.backgroundSurfaceHighlight, Ink),
        backgroundSurfaceUltraHighlight = tint(css.backgroundSurfaceUltraHighlight, Ink),
        backgroundSurfaceCard = solid(css.backgroundSurfaceCard, Card),
        backgroundOverlay = tint(css.backgroundOverlay, if (isDark) Background else Ink),
        backgroundGlass = tint(css.backgroundGlass, if (isDark) Ink else Card),
        borderPrimary = tint(css.borderPrimary, Ink),
        borderSecondary = tint(css.borderSecondary, Ink),
        borderTertiary = if (changed(Outline)) solid(css.borderTertiary, Outline) else tint(css.borderTertiary, Ink),
        borderLight = tint(css.borderLight, Ink),
        borderMediumHighlight = tint(css.borderMediumHighlight, Ink),
        borderHighlight = tint(css.borderHighlight, Ink),
        borderUltraHighlight = tint(css.borderUltraHighlight, Ink),
        borderFocus = solid(css.borderFocus, Primary),
        successDefault = solid(css.successDefault, Success),
        successHighlight = tint(css.successHighlight, Success),
        warningDefault = solid(css.warningDefault, Warning),
        warningHighlight = tint(css.warningHighlight, Warning),
        errorDefault = solid(css.errorDefault, Error),
        errorPressed = shade(css.errorPressed, Error, PRESSED, ThemeColor.Black),
        errorHighlight = tint(css.errorHighlight, Error),
        infoDefault = solid(css.infoDefault, Info),
        infoHighlight = tint(css.infoHighlight, Info),
        staticWhite = css.staticWhite,
        staticBlack = css.staticBlack,
        busCan = css.busCan,
        busCanfd = css.busCanfd,
        busLin = css.busLin,
        busBean = css.busBean,
        busImo = css.busImo,
        busUnknown = css.busUnknown,
    )

    fun scheme(colors: CmpColors): ColorScheme {
        val page = palette.background
        val ink = palette.ink
        val containers = changed(Background, Ink)
        fun container(token: Color, fraction: Float) = if (containers) page.mix(ink, fraction).toColor() else token
        val steps = if (isDark) DarkContainerSteps else LightContainerSteps
        return cssScheme.copy(
            primary = solid(cssScheme.primary, Primary),
            onPrimary = solid(cssScheme.onPrimary, OnPrimary),
            primaryContainer = colors.brandPrimaryHighlight,
            surfaceTint = colors.brandPrimaryDefault,
            background = colors.backgroundPagePrimary,
            onBackground = colors.foregroundOnDarkPrimaryDefault,
            surface = colors.backgroundPagePrimary,
            onSurface = colors.foregroundOnDarkPrimaryDefault,
            surfaceVariant = colors.backgroundSurfaceHighlight,
            onSurfaceVariant = solid(cssScheme.onSurfaceVariant, Ink2),
            secondaryContainer = colors.backgroundSurfaceHighlight,
            onSecondaryContainer = colors.foregroundOnDarkPrimaryDefault,
            onPrimaryContainer = colors.foregroundOnDarkPrimaryDefault,
            onTertiaryContainer = colors.foregroundOnDarkPrimaryDefault,
            tertiaryContainer = colors.warningHighlight,
            error = colors.errorDefault,
            errorContainer = colors.errorHighlight,
            onErrorContainer = colors.foregroundOnDarkPrimaryDefault,
            outline = if (changed(Outline)) solid(cssScheme.outline, Outline) else tint(cssScheme.outline, Ink),
            outlineVariant = tint(cssScheme.outlineVariant, Ink),
            inverseSurface = solid(cssScheme.inverseSurface, Ink),
            inverseOnSurface = solid(cssScheme.inverseOnSurface, Background),
            surfaceBright = if (isDark) {
                container(
                    cssScheme.surfaceBright,
                    steps.bright,
                )
            } else {
                colors.backgroundPagePrimary
            },
            surfaceDim = if (isDark) colors.backgroundPagePrimary else container(cssScheme.surfaceDim, steps.dim),
            surfaceContainerLowest = if (isDark) {
                if (containers) page.mix(ThemeColor.Black, DARK_LOWEST).toColor() else cssScheme.surfaceContainerLowest
            } else {
                solid(cssScheme.surfaceContainerLowest, Card)
            },
            surfaceContainerLow = container(cssScheme.surfaceContainerLow, steps.low),
            surfaceContainer = container(cssScheme.surfaceContainer, steps.mid),
            surfaceContainerHigh = container(cssScheme.surfaceContainerHigh, steps.high),
            surfaceContainerHighest = container(cssScheme.surfaceContainerHighest, steps.highest),
        )
    }

    /** Доли чернил в контейнерах M3 — как `color-mix` в colors.css. */
    private class ContainerSteps(
        val low: Float,
        val mid: Float,
        val high: Float,
        val highest: Float,
        val dim: Float,
        val bright: Float,
    )

    private companion object {
        const val HOVER = 0.1f
        const val PRESSED = 0.2f
        const val DARK_LOWEST = 0.04f
        val LightContainerSteps = ContainerSteps(0.03f, 0.05f, 0.07f, 0.09f, 0.11f, 0f)
        val DarkContainerSteps = ContainerSteps(0.05f, 0.08f, 0.11f, 0.14f, 0f, 0.2f)
    }
}
