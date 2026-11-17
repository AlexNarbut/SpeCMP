package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RawTextSizeBypassTest {
    private val rule = RawTextSize(Config.empty)

    @Test
    fun `bypass via import alias of sp`() {
        val code = """
            import androidx.compose.ui.unit.sp as size

            val a = 14.size
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via em and toSp`() {
        val code = """
            val a = 1.2.em
            fun b(d: Dp) = d.toSp()
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via TextStyle and TextUnit constructors`() {
        val code = """
            val a = TextStyle()
            val b = TextUnit(14f, TextUnitType.Sp)
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via TextUnit and TextStyle typed parameters and properties`() {
        val code = """
            fun a(size: TextUnit, style: TextStyle) = Unit
            val b: TextUnit? = null
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `bypass via aliased TextStyle constructor`() {
        val code = """
            import androidx.compose.ui.text.TextStyle as Ts

            val a = Ts()
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via sp hidden inside letterSpacing expression`() {
        val code = """
            fun a(x: Any) = x.copy(letterSpacing = 1.sp, fontSize = (2 * 7).sp)
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }
}
