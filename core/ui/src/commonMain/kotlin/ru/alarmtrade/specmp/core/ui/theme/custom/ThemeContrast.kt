package ru.alarmtrade.specmp.core.ui.theme.custom

import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Background
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColorRole.Background2
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

/** Что проверяется; подписи — на стороне экрана. */
enum class ContrastCheckKind(val foreground: ThemeColorRole, val background: ThemeColorRole) {
    InkOnBackground(Ink, Background),
    InkOnCard(Ink, Card),
    Ink2OnCard(Ink2, Card),
    LabelOnButton(OnPrimary, Primary),
    AccentOnBackground(Primary, Background),
    AccentOnCard(Primary, Card),
    OutlineOnCard(Outline, Card),
    SuccessOnCard(Success, Card),
    WarningOnCard(Warning, Card),
    ErrorOnCard(Error, Card),
    InfoOnCard(Info, Card),
    InkOnGradientEnd(Ink, Background2),
    AccentOnGradientEnd(Primary, Background2),
    ;

    fun involves(role: ThemeColorRole): Boolean = foreground == role || background == role
}

data class ContrastCheck(val kind: ContrastCheckKind, val min: Double) {
    fun ratio(palette: ThemePalette): Double = palette[kind.foreground].contrast(palette[kind.background])

    fun passes(palette: ThemePalette): Boolean = ratio(palette) >= min
}

/**
 * Пороги макета: текст 4.5:1, акцент/обводки/статусы 3:1, акцент при контурных кнопках 4.5:1, при градиенте —
 * проверка на обоих концах. `base` — тема по умолчанию для того же режима: часть её CSS-токенов порогов не
 * проходит, поэтому пара, где оба цвета не менялись, не проверяется — кроме проверок, которые ужесточила
 * настройка (контурные кнопки) или добавил градиент.
 */
object ThemeContrast {
    const val TEXT_MIN = 4.5
    const val GRAPHIC_MIN = 3.0

    fun checks(mode: ThemeModeSpec, base: ThemeModeSpec): List<ContrastCheck> =
        allChecks(mode).filterNot { it.isGrandfathered(mode.palette, base) }

    fun failing(mode: ThemeModeSpec, base: ThemeModeSpec): List<ContrastCheck> =
        checks(mode, base).filterNot { it.passes(mode.palette) }

    fun passes(mode: ThemeModeSpec, base: ThemeModeSpec): Boolean = failing(mode, base).isEmpty()

    /** Проверки, затронутые цветом [role], если поставить ему [color]. */
    fun checksFor(
        mode: ThemeModeSpec,
        base: ThemeModeSpec,
        role: ThemeColorRole,
        color: ThemeColor,
    ): List<ContrastCheck> {
        val candidate = mode.withColor(role, color)
        return checks(candidate, base).filter { it.kind.involves(role) }
    }

    fun passes(mode: ThemeModeSpec, base: ThemeModeSpec, role: ThemeColorRole, color: ThemeColor): Boolean {
        val candidate = mode.withColor(role, color)
        return checksFor(mode, base, role, color).all { it.passes(candidate.palette) }
    }

    /** Ближайший подходящий: смешивание с чёрным или белым шагом 2%; `null` — мешают связанные цвета. */
    fun nearest(mode: ThemeModeSpec, base: ThemeModeSpec, role: ThemeColorRole, color: ThemeColor): ThemeColor? {
        if (passes(mode, base, role, color)) return color
        for (step in 1..NEAREST_STEPS) {
            for (target in listOf(ThemeColor.Black, ThemeColor.White)) {
                val mixed = color.mix(target, step / NEAREST_STEPS.toFloat())
                if (passes(mode, base, role, mixed)) return mixed
            }
        }
        return null
    }

    fun canUseFill(mode: ThemeModeSpec, base: ThemeModeSpec, fill: ButtonFill): Boolean =
        passes(mode.copy(buttonFill = fill), base)

    private fun allChecks(mode: ThemeModeSpec): List<ContrastCheck> {
        val accentMin = if (mode.buttonFill == ButtonFill.Outlined) TEXT_MIN else GRAPHIC_MIN
        return ContrastCheckKind.entries.mapNotNull { kind ->
            when (kind) {
                ContrastCheckKind.InkOnBackground,
                ContrastCheckKind.InkOnCard,
                ContrastCheckKind.Ink2OnCard,
                ContrastCheckKind.LabelOnButton,
                -> ContrastCheck(kind, TEXT_MIN)

                ContrastCheckKind.AccentOnBackground, ContrastCheckKind.AccentOnCard -> ContrastCheck(kind, accentMin)

                ContrastCheckKind.InkOnGradientEnd ->
                    ContrastCheck(kind, TEXT_MIN).takeIf { mode.background == ThemeBackground.Gradient }

                ContrastCheckKind.AccentOnGradientEnd ->
                    ContrastCheck(kind, accentMin).takeIf { mode.background == ThemeBackground.Gradient }

                else -> ContrastCheck(kind, GRAPHIC_MIN)
            }
        }
    }

    private fun ContrastCheck.isGrandfathered(palette: ThemePalette, base: ThemeModeSpec): Boolean {
        if (kind.involves(Background2)) return false
        val unchanged = palette[kind.foreground] == base.palette[kind.foreground] &&
            palette[kind.background] == base.palette[kind.background]
        val baseCheck = allChecks(base).firstOrNull { it.kind == kind }
        return unchanged && baseCheck != null && min <= baseCheck.min
    }

    private fun ThemeModeSpec.withColor(role: ThemeColorRole, color: ThemeColor) =
        copy(palette = palette.with(role, color))

    private const val NEAREST_STEPS = 50
}
