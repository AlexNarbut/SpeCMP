package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HardcodedUiTextTest {
    private val rule = HardcodedUiText(Config.empty)

    @Test
    fun `reports literal text and ui parameters in composables`() {
        val code = """
            @Composable
            fun Screen() {
                CmpText("Привет")
                Text(text = "Hello ${'$'}name")
                CmpIcon(icon, contentDescription = "Close")
                CmpTextField(value, onChange, label = "Email", placeholder = "you@mail")
                CmpTopAppBar(title = "Settings")
            }
        """.trimIndent()

        assertEquals(6, rule.lint(code).size)
    }

    @Test
    fun `does not report resources, symbols, logic strings, previews and non-composables`() {
        val code = """
            @Composable
            fun Screen(count: Int) {
                CmpText(stringResource(Strings.string.title))
                CmpText("${'$'}count / ${'$'}total")
                CmpText("•")
                CmpIcon(icon, contentDescription = null)
                Modifier.testTag("submit")
                key("row") { }
            }

            @Preview
            @Composable
            fun ScreenPreview() {
                CmpText("Preview")
            }

            fun format(): String = Text("plain")
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}
