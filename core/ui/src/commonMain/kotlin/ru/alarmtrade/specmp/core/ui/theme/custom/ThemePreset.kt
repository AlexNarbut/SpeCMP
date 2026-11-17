package ru.alarmtrade.specmp.core.ui.theme.custom

import androidx.compose.material3.ColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColors

/** Готовые темы; `Pandora` — тема по умолчанию, ровно CSS-токены дизайн-системы. */
enum class ThemePreset(val spec: ThemeSpec) {
    Pandora(
        ThemeSpec(
            light = defaultMode(LightCmpColors, LightCmpColorScheme),
            dark = defaultMode(DarkCmpColors, DarkCmpColorScheme),
        ),
    ),
    Graphite(
        preset(
            light = palette("#2C3639", "#FFFFFF", "#F3F4F2", "#FFFFFF", "#1A1F1E", "#505957", "#848D8B", LightStatus),
            dark = palette("#A9CCD2", "#102024", "#111514", "#1C2221", "#E9EDEB", "#A7B1AF", "#6A7573", DarkStatus),
            shape = ShapePreset(ButtonFill.Tonal, ButtonShape.Rounded, radius = 6, chips = 6),
        ),
    ),
    Contrast(
        preset(
            light = palette(
                "#0038D6",
                "#FFFFFF",
                "#FFFFFF",
                "#FFFFFF",
                "#000000",
                "#2B2B2B",
                "#000000",
                listOf("#106B2E", "#8F3D00", "#B00F05", "#00508F"),
            ),
            dark = palette(
                "#FFD60A",
                "#000000",
                "#000000",
                "#121212",
                "#FFFFFF",
                "#E0E0E0",
                "#FFFFFF",
                listOf("#5EE38A", "#FFB04D", "#FF8A80", "#7CC8FF"),
            ),
            shape = ShapePreset(ButtonFill.Filled, ButtonShape.Square, radius = 4, chips = 4, text = TextScale.Large),
        ),
    ),
    Workshop(
        preset(
            light = palette("#C2410C", "#FFFFFF", "#F8F5F0", "#FFFFFF", "#24180E", "#5E5046", "#93857A", LightStatus),
            dark = palette("#FF8A3D", "#1F0D00", "#16120E", "#221C17", "#F7EFE7", "#C2B3A5", "#7D6F62", DarkStatus),
            shape = ShapePreset(ButtonFill.Filled, ButtonShape.Pill, radius = 20, chips = ThemeRadii.MAX),
        ),
    ),
    NightShift(
        preset(
            light = palette("#1F7A55", "#FFFFFF", "#F1F5F2", "#FFFFFF", "#0F1F18", "#46564E", "#7D8D85", LightStatus),
            dark = palette("#7AD3A6", "#05231A", "#0A100D", "#141C18", "#E3EEE8", "#9DB0A6", "#627670", DarkStatus),
            shape = ShapePreset(
                ButtonFill.Outlined,
                ButtonShape.Rounded,
                radius = 14,
                chips = ThemeRadii.MAX,
                text = TextScale.Large,
            ),
        ),
    ),
    ;

    companion object {
        val Default: ThemeSpec get() = Pandora.spec

        fun matching(spec: ThemeSpec): ThemePreset? = entries.firstOrNull { it.spec == spec }
    }
}

private val LightStatus = listOf("#2E7D46", "#A35200", "#C62A1E", "#2A6F97")
private val DarkStatus = listOf("#6CCB8A", "#FFB062", "#FF7B70", "#7CC0E8")

private class ShapePreset(
    val fill: ButtonFill,
    val shape: ButtonShape,
    val radius: Int,
    val chips: Int,
    val text: TextScale = TextScale.Normal,
)

private fun preset(light: ThemePalette, dark: ThemePalette, shape: ShapePreset): ThemeSpec {
    fun mode(palette: ThemePalette) = ThemeModeSpec(
        palette = palette,
        background = ThemeBackground.Solid,
        textScale = shape.text,
        buttonFill = shape.fill,
        buttonShape = shape.shape,
        radius = shape.radius,
        radii = ThemeRadii.fromBase(shape.radius, shape.chips),
    )
    return ThemeSpec(light = mode(light), dark = mode(dark))
}

@Suppress("LongParameterList")
private fun palette(
    primary: String,
    onPrimary: String,
    background: String,
    card: String,
    ink: String,
    ink2: String,
    outline: String,
    status: List<String>,
): ThemePalette {
    fun c(hex: String) = requireNotNull(ThemeColor.parse(hex))
    val (success, warning) = status.take(2).map(::c)
    val (error, info) = status.drop(2).map(::c)
    return ThemePalette(
        primary = c(primary),
        onPrimary = c(onPrimary),
        background = c(background),
        background2 = ThemePalette.defaultBackground2(c(background), c(primary)),
        card = c(card),
        ink = c(ink),
        ink2 = c(ink2),
        outline = c(outline),
        success = success,
        warning = warning,
        error = error,
        info = info,
    )
}

/** Палитра из CSS-токенов; полупрозрачные (карточка в тёмной, обводка) сведены на подложку. */
private fun defaultMode(colors: CmpColors, scheme: ColorScheme): ThemeModeSpec {
    val page = ThemeColor.flatten(colors.backgroundPagePrimary, ThemeColor.White)
    val primary = ThemeColor.flatten(colors.brandPrimaryDefault, page)
    val card = ThemeColor.flatten(colors.backgroundSurfaceCard, page)
    return ThemeModeSpec(
        palette = ThemePalette(
            primary = primary,
            onPrimary = ThemeColor.flatten(colors.onBrand, primary),
            background = page,
            background2 = ThemePalette.defaultBackground2(page, primary),
            card = card,
            ink = ThemeColor.flatten(colors.foregroundOnDarkPrimaryDefault, page),
            ink2 = ThemeColor.flatten(scheme.onSurfaceVariant, card),
            outline = ThemeColor.flatten(colors.borderTertiary, card),
            success = ThemeColor.flatten(colors.successDefault, card),
            warning = ThemeColor.flatten(colors.warningDefault, card),
            error = ThemeColor.flatten(colors.errorDefault, card),
            info = ThemeColor.flatten(colors.infoDefault, card),
        ),
    )
}
