package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import org.jetbrains.compose.resources.Font
import ru.alarmtrade.specmp.core.ui.resources.Res
import ru.alarmtrade.specmp.core.ui.resources.ibm_plex_sans_bold
import ru.alarmtrade.specmp.core.ui.resources.ibm_plex_sans_medium
import ru.alarmtrade.specmp.core.ui.resources.ibm_plex_sans_regular
import ru.alarmtrade.specmp.core.ui.resources.roboto_mono_medium
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpColors
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpTextScale
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpColors
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpM3TypeAliases
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpTypeScale
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpTypeSpec
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/** Цвета дизайн-системы; отступы, радиусы и размеры — объекты `CmpSpacing`, `CmpRadius`, `CmpSize`. */
object CmpTheme {
    val colors: CmpColors
        @Composable @ReadOnlyComposable
        get() = LocalCmpColors.current

    internal val typography: CmpTypography
        @Composable
        get() {
            val family = MaterialTheme.typography.bodyMedium.fontFamily ?: FontFamily.Default
            val monoFamily = rememberCmpMonoFontFamily()
            val scale = LocalCmpTextScale.current
            return remember(family, monoFamily, scale) { CmpTypography(family, monoFamily, scale) }
        }
}

/** Стили шкалы; наружу не отдаются — текст в фичах только через `CmpText(style = …)`. */
@Immutable
internal class CmpTypography(
    val fontFamily: FontFamily,
    val monoFontFamily: FontFamily,
    private val scale: Float = 1f,
) {
    val accent1 = style(CmpTypeScale.accent1)
    val accent2 = style(CmpTypeScale.accent2)
    val accent3 = style(CmpTypeScale.accent3)
    val title1 = style(CmpTypeScale.title1)
    val title2 = style(CmpTypeScale.title2)
    val headline1 = style(CmpTypeScale.headline1)
    val headline2 = style(CmpTypeScale.headline2)
    val headline3 = style(CmpTypeScale.headline3)
    val paragraph1 = style(CmpTypeScale.paragraph1)
    val paragraph2 = style(CmpTypeScale.paragraph2)
    val paragraph3 = style(CmpTypeScale.paragraph3)
    val additional0 = style(CmpTypeScale.additional0)
    val additional1 = style(CmpTypeScale.additional1)
    val additional2 = style(CmpTypeScale.additional2)
    val mono1 = style(CmpTypeScale.mono1)
    val mono2 = style(CmpTypeScale.mono2)

    private val byStyle = mapOf(
        CmpTextStyle.Accent1 to accent1,
        CmpTextStyle.Accent2 to accent2,
        CmpTextStyle.Accent3 to accent3,
        CmpTextStyle.Title1 to title1,
        CmpTextStyle.Title2 to title2,
        CmpTextStyle.Headline1 to headline1,
        CmpTextStyle.Headline2 to headline2,
        CmpTextStyle.Headline3 to headline3,
        CmpTextStyle.Paragraph1 to paragraph1,
        CmpTextStyle.Paragraph2 to paragraph2,
        CmpTextStyle.Paragraph3 to paragraph3,
        CmpTextStyle.Additional0 to additional0,
        CmpTextStyle.Additional1 to additional1,
        CmpTextStyle.Additional2 to additional2,
        CmpTextStyle.Mono1 to mono1,
        CmpTextStyle.Mono2 to mono2,
    )

    fun of(style: CmpTextStyle): TextStyle = byStyle.getValue(style)

    fun material(): Typography {
        fun m3(role: String) = style(CmpM3TypeAliases.getValue(role))
        return Typography(
            displayLarge = m3("displayLarge"),
            displayMedium = m3("displayMedium"),
            displaySmall = m3("displaySmall"),
            headlineLarge = m3("headlineLarge"),
            headlineMedium = m3("headlineMedium"),
            headlineSmall = m3("headlineSmall"),
            titleLarge = m3("titleLarge"),
            titleMedium = m3("titleMedium"),
            titleSmall = m3("titleSmall"),
            bodyLarge = m3("bodyLarge"),
            bodyMedium = m3("bodyMedium"),
            bodySmall = m3("bodySmall"),
            labelLarge = m3("labelLarge"),
            labelMedium = m3("labelMedium"),
            labelSmall = m3("labelSmall"),
        )
    }

    private fun style(spec: CmpTypeSpec) = TextStyle(
        fontFamily = if (spec.mono) monoFontFamily else fontFamily,
        fontWeight = spec.weight,
        fontSize = spec.size * scale,
        lineHeight = spec.lineHeight * scale,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
    )
}

@Composable
internal fun rememberCmpTypography(scale: Float = 1f): CmpTypography {
    val regular = Font(Res.font.ibm_plex_sans_regular, FontWeight.Normal)
    val medium = Font(Res.font.ibm_plex_sans_medium, FontWeight.Medium)
    val bold = Font(Res.font.ibm_plex_sans_bold, FontWeight.Bold)
    val mono = rememberCmpMonoFontFamily()
    return remember(regular, medium, bold, mono, scale) {
        CmpTypography(FontFamily(regular, medium, bold), mono, scale)
    }
}

@Composable
private fun rememberCmpMonoFontFamily(): FontFamily {
    val medium = Font(Res.font.roboto_mono_medium, FontWeight.Medium)
    return remember(medium) { FontFamily(medium) }
}
