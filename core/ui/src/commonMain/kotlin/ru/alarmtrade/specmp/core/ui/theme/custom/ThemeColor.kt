package ru.alarmtrade.specmp.core.ui.theme.custom

import androidx.compose.ui.graphics.Color
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/** Непрозрачный цвет темы `0xRRGGBB`: всё, что вводит пользователь и хранится в файле темы. */
@JvmInline
value class ThemeColor(val rgb: Int) {
    val hex: String get() = "#" + (rgb and RGB_MASK).toString(HEX_RADIX).uppercase().padStart(HEX_DIGITS, '0')

    fun toColor(): Color = Color(OPAQUE or rgb)

    fun mix(other: ThemeColor, fraction: Float): ThemeColor {
        fun channel(shift: Int): Int {
            val a = (rgb shr shift) and BYTE
            val b = (other.rgb shr shift) and BYTE
            return (a + (b - a) * fraction).roundToInt().coerceIn(0, BYTE) shl shift
        }
        return ThemeColor(channel(RED) or channel(GREEN) or channel(BLUE))
    }

    /** Контраст WCAG 2.x: 1…21. */
    fun contrast(other: ThemeColor): Double {
        val a = luminance()
        val b = other.luminance()
        return (max(a, b) + LUMINANCE_OFFSET) / (min(a, b) + LUMINANCE_OFFSET)
    }

    private fun luminance(): Double {
        fun linear(shift: Int): Double {
            val v = ((rgb shr shift) and BYTE) / BYTE.toDouble()
            return if (v <= SRGB_KNEE) v / SRGB_SLOPE else ((v + SRGB_A) / (1 + SRGB_A)).pow(SRGB_GAMMA)
        }
        return LUM_R * linear(RED) + LUM_G * linear(GREEN) + LUM_B * linear(BLUE)
    }

    companion object {
        val Black = ThemeColor(0x000000)
        val White = ThemeColor(0xFFFFFF)

        private val HexPattern = Regex("^#?[0-9A-Fa-f]{6}$")

        /** `#RRGGBB` или `RRGGBB`; иначе `null`. */
        fun parse(hex: String): ThemeColor? {
            val value = hex.trim()
            if (!HexPattern.matches(value)) return null
            return ThemeColor(value.removePrefix("#").toInt(HEX_RADIX))
        }

        /** Полупрозрачный токен, сведённый на подложку: так его видит глаз и так его проверяет контраст. */
        fun flatten(color: Color, over: ThemeColor): ThemeColor {
            val top = ThemeColor(
                ((color.red * BYTE).roundToInt() shl RED) or
                    ((color.green * BYTE).roundToInt() shl GREEN) or
                    (color.blue * BYTE).roundToInt(),
            )
            return over.mix(top, color.alpha)
        }
    }
}

private const val OPAQUE = -0x1000000
private const val RGB_MASK = 0xFFFFFF
private const val HEX_RADIX = 16
private const val HEX_DIGITS = 6
private const val BYTE = 0xFF
private const val RED = 16
private const val GREEN = 8
private const val BLUE = 0
private const val LUMINANCE_OFFSET = 0.05
private const val SRGB_KNEE = 0.03928
private const val SRGB_SLOPE = 12.92
private const val SRGB_A = 0.055
private const val SRGB_GAMMA = 2.4
private const val LUM_R = 0.2126
private const val LUM_G = 0.7152
private const val LUM_B = 0.0722
