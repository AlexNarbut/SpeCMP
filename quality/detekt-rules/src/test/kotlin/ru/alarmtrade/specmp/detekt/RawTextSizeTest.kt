package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RawTextSizeTest {
    private val rule = RawTextSize(Config.empty)

    @Test
    fun `reports sp literals and variables`() {
        val code = """
            import androidx.compose.ui.unit.sp

            val a = 14.sp
            val b = 14.5.sp
            fun c(size: Int) = size.sp
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `reports named fontSize and lineHeight arguments`() {
        val code = """
            fun a(style: Any, s: Any) {
                copy(fontSize = s)
                SpanStyle(lineHeight = s)
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report scale styles, letterSpacing and font weight spans`() {
        val code = """
            import androidx.compose.ui.unit.sp

            fun a(x: Any) {
                CmpText(text = "", style = CmpTextStyle.Paragraph2)
                x.copy(letterSpacing = 0.5.sp)
                SpanStyle(fontWeight = FontWeight.Bold)
                val spacing = 4
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
