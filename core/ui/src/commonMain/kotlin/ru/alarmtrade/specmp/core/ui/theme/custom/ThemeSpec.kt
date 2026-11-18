package ru.alarmtrade.specmp.core.ui.theme.custom

import androidx.compose.runtime.Immutable

/** Пользовательская тема: светлая и тёмная настраиваются независимо. */
@Immutable
data class ThemeSpec(val light: ThemeModeSpec, val dark: ThemeModeSpec) {
    fun mode(isDark: Boolean): ThemeModeSpec = if (isDark) dark else light

    fun withMode(isDark: Boolean, spec: ThemeModeSpec): ThemeSpec =
        if (isDark) copy(dark = spec) else copy(light = spec)
}

@Immutable
data class ThemeModeSpec(
    val palette: ThemePalette,
    val background: ThemeBackground = ThemeBackground.Glow,
    val gradientDirection: GradientDirection = GradientDirection.Down,
    val textScale: TextScale = TextScale.Normal,
    val buttonFill: ButtonFill = ButtonFill.Filled,
    val buttonShape: ButtonShape = ButtonShape.Rounded,
    val radius: Int = ThemeRadii.DEFAULT_BASE,
    val radii: ThemeRadii = ThemeRadii(),
    val buttonPadding: ControlPadding = ControlPadding.Medium,
    val chipPadding: ControlPadding = ControlPadding.Medium,
) {
    /** Общий ползунок скругления: кнопки, поля, карточки и шторки — от него, чипы — свои. */
    fun withRadius(base: Int): ThemeModeSpec {
        val value = base.coerceIn(0, ThemeRadii.MAX)
        return copy(radius = value, radii = ThemeRadii.fromBase(value, radii.chips))
    }

    /** Размеры, форма и кнопки — то, что переключатель «Одинаково для обеих тем» копирует в другую тему. */
    fun withShapeOf(other: ThemeModeSpec): ThemeModeSpec = copy(
        textScale = other.textScale,
        buttonShape = other.buttonShape,
        radius = other.radius,
        radii = other.radii,
        buttonPadding = other.buttonPadding,
        chipPadding = other.chipPadding,
    )

    fun sameShapeAs(other: ThemeModeSpec): Boolean = withShapeOf(other) == this
}

enum class ThemeColorRole {
    Primary,
    OnPrimary,
    Background,
    Background2,
    Card,
    Ink,
    Ink2,
    Outline,
    Success,
    Warning,
    Error,
    Info,
}

@Immutable
data class ThemePalette(
    val primary: ThemeColor,
    val onPrimary: ThemeColor,
    val background: ThemeColor,
    val background2: ThemeColor,
    val card: ThemeColor,
    val ink: ThemeColor,
    val ink2: ThemeColor,
    val outline: ThemeColor,
    val success: ThemeColor,
    val warning: ThemeColor,
    val error: ThemeColor,
    val info: ThemeColor,
) {
    operator fun get(role: ThemeColorRole): ThemeColor = when (role) {
        ThemeColorRole.Primary -> primary
        ThemeColorRole.OnPrimary -> onPrimary
        ThemeColorRole.Background -> background
        ThemeColorRole.Background2 -> background2
        ThemeColorRole.Card -> card
        ThemeColorRole.Ink -> ink
        ThemeColorRole.Ink2 -> ink2
        ThemeColorRole.Outline -> outline
        ThemeColorRole.Success -> success
        ThemeColorRole.Warning -> warning
        ThemeColorRole.Error -> error
        ThemeColorRole.Info -> info
    }

    fun with(role: ThemeColorRole, color: ThemeColor): ThemePalette = when (role) {
        ThemeColorRole.Primary -> copy(primary = color)
        ThemeColorRole.OnPrimary -> copy(onPrimary = color)
        ThemeColorRole.Background -> copy(background = color)
        ThemeColorRole.Background2 -> copy(background2 = color)
        ThemeColorRole.Card -> copy(card = color)
        ThemeColorRole.Ink -> copy(ink = color)
        ThemeColorRole.Ink2 -> copy(ink2 = color)
        ThemeColorRole.Outline -> copy(outline = color)
        ThemeColorRole.Success -> copy(success = color)
        ThemeColorRole.Warning -> copy(warning = color)
        ThemeColorRole.Error -> copy(error = color)
        ThemeColorRole.Info -> copy(info = color)
    }

    companion object {
        /** Второй цвет градиента по умолчанию — фон с 15% акцента (как в макете). */
        fun defaultBackground2(background: ThemeColor, primary: ThemeColor): ThemeColor =
            background.mix(primary, BACKGROUND2_PRIMARY_SHARE)

        private const val BACKGROUND2_PRIMARY_SHARE = 0.15f
    }
}

/** `Glow` — живое свечение поверх фона страницы (по умолчанию), `Solid` — заливка, `Gradient` — фон → второй цвет. */
enum class ThemeBackground { Glow, Solid, Gradient }

/** `Corner` — радиальный из верхнего угла. */
enum class GradientDirection { Down, Diagonal, Corner }

enum class TextScale(val percent: Int) {
    Small(PERCENT_SMALL),
    Normal(PERCENT_NORMAL),
    Large(PERCENT_LARGE),
    Larger(PERCENT_LARGER),
    Largest(PERCENT_LARGEST),
    ;

    val factor: Float get() = percent / PERCENT

    private companion object {
        const val PERCENT = 100f
    }
}

enum class ButtonFill { Filled, Tonal, Outlined }

enum class ButtonShape { Square, Rounded, Pill }

/** Сдвиг размеров кнопок и чипов; высота не опускается ниже 48 dp. */
enum class ControlPadding { Small, Medium, Large }

/** Скругления по элементам, 0…[MAX] dp; у чипов [MAX] — пилюля. */
@Immutable
data class ThemeRadii(
    val buttons: Int = DEFAULT_BASE,
    val inputs: Int = DEFAULT_BASE,
    val cards: Int = DEFAULT_CARDS,
    val chips: Int = MAX,
    val sheets: Int = DEFAULT_CARDS,
) {
    val chipsArePill: Boolean get() = chips >= MAX

    companion object {
        const val MAX = 28
        const val DEFAULT_BASE = 12
        private const val DEFAULT_CARDS = 16
        private const val CARD_EXTRA = 4

        fun fromBase(base: Int, chips: Int): ThemeRadii = ThemeRadii(
            buttons = base,
            inputs = base,
            cards = (base + CARD_EXTRA).coerceAtMost(MAX),
            chips = chips,
            sheets = (base + CARD_EXTRA).coerceAtMost(MAX),
        )
    }
}

private const val PERCENT_SMALL = 80
private const val PERCENT_NORMAL = 100
private const val PERCENT_LARGE = 115
private const val PERCENT_LARGER = 130
private const val PERCENT_LARGEST = 150
