package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HardcodedUiTextBypassTest {
    private val rule = HardcodedUiText(Config.empty)

    @Test
    fun `bypass via string concatenation`() {
        val code = """
            @Composable
            fun Screen(name: String) {
                Text("Hello, " + name)
                CmpTopAppBar(title = name + " settings")
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via conditional branches`() {
        val code = """
            @Composable
            fun Screen(on: Boolean, name: String?) {
                CmpText(if (on) "Enabled" else "Disabled")
                CmpButton(onClick = {}, text = name ?: "Anonymous")
                CmpText(when { on -> "Yes"; else -> stringResource(Strings.string.no) })
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `bypass via local variable`() {
        val code = """
            @Composable
            fun Screen() {
                val greeting = "Hello"
                CmpText(greeting)
                CmpIcon(icon, contentDescription = greeting)
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via call on a literal`() {
        val code = """
            @Composable
            fun Screen() {
                CmpText("hello".uppercase())
                CmpTopAppBar(title = ("Settings"))
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via composable lambda property`() {
        val code = """
            val content: @Composable () -> Unit = { CmpText("Hello") }

            object Screens {
                val header = @Composable { Text(text = "Header") }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report resources, symbols and non-composable lambdas`() {
        val code = """
            val formatter: () -> String = { "Hello" }

            @Composable
            fun Screen(count: Int, total: Int, name: String?) {
                val label = stringResource(Strings.string.title)
                val separator = " / "
                CmpText(label)
                CmpText(count.toString() + separator + total)
                CmpText(name ?: stringResource(Strings.string.anonymous))
                CmpText(if (count > 0) "${'$'}count" else "—")
            }

            @Preview
            @Composable
            fun ScreenPreview() {
                val title = "Preview"
                CmpText(title)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
