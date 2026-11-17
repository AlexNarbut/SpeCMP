package ru.alarmtrade.specmp.core.ui.theme.custom.data.mapper

import kotlin.math.roundToInt
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonShape
import ru.alarmtrade.specmp.core.ui.theme.custom.ControlPadding
import ru.alarmtrade.specmp.core.ui.theme.custom.GradientDirection
import ru.alarmtrade.specmp.core.ui.theme.custom.TextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeRadii
import ru.alarmtrade.specmp.core.ui.theme.custom.data.ThemeModeDto

internal fun ThemeModeDto?.applyTo(mode: ThemeModeSpec): ThemeModeSpec {
    if (this == null) return mode
    var palette = mode.palette
    c?.forEach { (key, hex) ->
        val role = RoleKeys[key] ?: return@forEach
        val color = ThemeColor.parse(hex) ?: return@forEach
        palette = palette.with(role, color)
    }
    val base = radius.toRadius() ?: mode.radius
    return mode.copy(
        palette = palette,
        background = BackgroundKeys.parse(bgType) ?: mode.background,
        gradientDirection = DirectionKeys.parse(bgDir) ?: mode.gradientDirection,
        textScale = TextScale.entries.firstOrNull { it.percent == text } ?: mode.textScale,
        buttonFill = FillKeys.parse(fill) ?: mode.buttonFill,
        buttonShape = ShapeKeys.parse(shape) ?: mode.buttonShape,
        radius = base,
        radii = r.applyTo(mode.radii),
        buttonPadding = PaddingKeys.parse(btnPad) ?: mode.buttonPadding,
        chipPadding = PaddingKeys.parse(chipPad) ?: mode.chipPadding,
    )
}

private fun Map<String, Double>?.applyTo(radii: ThemeRadii): ThemeRadii {
    if (this == null) return radii
    fun get(key: String, current: Int) = this[key].toRadius() ?: current
    return ThemeRadii(
        buttons = get("buttons", radii.buttons),
        inputs = get("inputs", radii.inputs),
        cards = get("cards", radii.cards),
        chips = get("chips", radii.chips),
        sheets = get("sheets", radii.sheets),
    )
}

private fun Double?.toRadius(): Int? = this?.takeIf { it in 0.0..ThemeRadii.MAX.toDouble() }?.roundToInt()

internal fun ThemeModeSpec.toThemeModeDto() = ThemeModeDto(
    c = RoleKeys.entries.associate { (key, role) -> key to palette[role].hex },
    bgType = BackgroundKeys.key(background),
    bgDir = DirectionKeys.key(gradientDirection),
    text = textScale.percent,
    fill = FillKeys.key(buttonFill),
    shape = ShapeKeys.key(buttonShape),
    radius = radius.toDouble(),
    r = mapOf(
        "buttons" to radii.buttons.toDouble(),
        "inputs" to radii.inputs.toDouble(),
        "cards" to radii.cards.toDouble(),
        "chips" to radii.chips.toDouble(),
        "sheets" to radii.sheets.toDouble(),
    ),
    btnPad = PaddingKeys.key(buttonPadding),
    chipPad = PaddingKeys.key(chipPadding),
)

private class Keys<T>(vararg pairs: Pair<String, T>) {
    private val byKey = pairs.toMap()
    private val byValue = pairs.associate { (k, v) -> v to k }

    fun parse(key: String?): T? = key?.let(byKey::get)

    fun key(value: T): String = byValue.getValue(value)
}

private val RoleKeys = mapOf(
    "primary" to ThemeColorRole.Primary,
    "onPrimary" to ThemeColorRole.OnPrimary,
    "bg" to ThemeColorRole.Background,
    "bg2" to ThemeColorRole.Background2,
    "card" to ThemeColorRole.Card,
    "ink" to ThemeColorRole.Ink,
    "ink2" to ThemeColorRole.Ink2,
    "outline" to ThemeColorRole.Outline,
    "success" to ThemeColorRole.Success,
    "warning" to ThemeColorRole.Warning,
    "error" to ThemeColorRole.Error,
    "info" to ThemeColorRole.Info,
)
private val BackgroundKeys =
    Keys("glow" to ThemeBackground.Glow, "solid" to ThemeBackground.Solid, "gradient" to ThemeBackground.Gradient)
private val DirectionKeys =
    Keys("down" to GradientDirection.Down, "diag" to GradientDirection.Diagonal, "radial" to GradientDirection.Corner)
private val FillKeys =
    Keys("filled" to ButtonFill.Filled, "tonal" to ButtonFill.Tonal, "outlined" to ButtonFill.Outlined)
private val ShapeKeys =
    Keys("square" to ButtonShape.Square, "rounded" to ButtonShape.Rounded, "pill" to ButtonShape.Pill)
private val PaddingKeys =
    Keys("s" to ControlPadding.Small, "m" to ControlPadding.Medium, "l" to ControlPadding.Large)
