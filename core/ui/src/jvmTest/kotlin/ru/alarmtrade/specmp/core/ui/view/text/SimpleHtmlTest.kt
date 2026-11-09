package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleHtmlTest {

    @Test
    fun `paragraphs and breaks become line breaks without empty edges`() {
        assertEquals("Первый\nВторой\nТретий", parseSimpleHtml("<p>Первый</p>\n<p>Второй<br/>Третий</p>").text)
    }

    @Test
    fun `list items become bullet lines`() {
        assertEquals(
            "Шаги:\n• Снять\n• Подключить",
            parseSimpleHtml("Шаги:<ul><li>Снять</li><li>Подключить</li></ul>").text,
        )
    }

    @Test
    fun `whitespace collapses and entities decode`() {
        assertEquals("A & B «C»", parseSimpleHtml("A  &amp;\n B &laquo;C&raquo;").text)
    }

    @Test
    fun `bold span covers its text`() {
        val text = parseSimpleHtml("Блок <b>по CAN</b>")
        val bold = text.spanStyles.single()
        assertEquals(FontWeight.Bold, bold.item.fontWeight)
        assertEquals("по CAN", text.text.substring(bold.start, bold.end))
    }

    @Test
    fun `links keep url and unknown tags keep text`() {
        val text = parseSimpleHtml("<span>См. <a href=\"https://x.ru/a.pdf\">схему</a></span>")
        assertEquals("См. схему", text.text)
        val link = text.getLinkAnnotations(0, text.length).single()
        assertEquals("https://x.ru/a.pdf", (link.item as LinkAnnotation.Url).url)
    }
}
