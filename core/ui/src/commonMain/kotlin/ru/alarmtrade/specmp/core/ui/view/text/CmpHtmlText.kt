package ru.alarmtrade.specmp.core.ui.view.text

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

/** Простой HTML с сервера: абзацы, `<br>`, b/i/u, списки, ссылки; прочие теги отбрасываются, текст остаётся. */
@Composable
fun CmpHtmlText(
    html: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = LocalContentColor.current,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val text = remember(html, linkColor) { parseSimpleHtml(html, linkColor) }
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

private val tagRegex = Regex("<(/?)([a-zA-Z0-9]+)([^>]*)>")
private val hrefRegex = Regex("href\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE)
private val spacesRegex = Regex("[ \\t\\r\\n]+")
private val entities = mapOf(
    "&nbsp;" to "\u00A0",
    "&amp;" to "&",
    "&lt;" to "<",
    "&gt;" to ">",
    "&quot;" to "\"",
    "&#39;" to "'",
    "&laquo;" to "«",
    "&raquo;" to "»",
    "&mdash;" to "—",
    "&ndash;" to "–",
)

internal fun parseSimpleHtml(html: String, linkColor: Color = Color.Unspecified): AnnotatedString =
    buildAnnotatedString { SimpleHtmlWriter(this, linkColor).write(html) }.trimEnd()

private const val CLOSING_GROUP = 1
private const val TAG_GROUP = 2
private const val ATTRIBUTES_GROUP = 3

private class SimpleHtmlWriter(private val builder: AnnotatedString.Builder, private val linkColor: Color) {
    /** Последний добавленный символ: Builder не отдаёт свой текст, а переносы зависят от него. */
    private var last: Char? = null
    private val opened = ArrayDeque<String>()

    fun write(html: String) {
        var cursor = 0
        for (match in tagRegex.findAll(html)) {
            text(html.substring(cursor, match.range.first))
            cursor = match.range.last + 1
            tag(
                name = match.groupValues[TAG_GROUP].lowercase(),
                closing = match.groupValues[CLOSING_GROUP] == "/",
                attributes = match.groupValues[ATTRIBUTES_GROUP],
            )
        }
        text(html.substring(cursor))
        while (opened.isNotEmpty()) {
            opened.removeLast()
            builder.pop()
        }
    }

    private fun tag(name: String, closing: Boolean, attributes: String) {
        when (name) {
            "br" -> put("\n")

            "p", "div", "ul", "ol" -> newLine()

            "li" -> {
                newLine()
                if (!closing) put("• ")
            }

            in inlineTags -> if (closing) close(name) else open(name, attributes)

            else -> Unit
        }
    }

    private fun open(name: String, attributes: String) {
        opened.addLast(name)
        builder.pushStyleFor(name, attributes, linkColor)
    }

    private fun close(name: String) {
        if (opened.lastOrNull() == name) {
            opened.removeLast()
            builder.pop()
        }
    }

    private fun text(raw: String) {
        val collapsed = raw.replace(spacesRegex, " ")
        val decoded = entities.entries.fold(collapsed) { acc, (entity, value) -> acc.replace(entity, value) }
        put(if (last == null || last == '\n') decoded.trimStart() else decoded)
    }

    private fun newLine() {
        if (last != null && last != '\n') put("\n")
    }

    private fun put(text: String) {
        if (text.isEmpty()) return
        builder.append(text)
        last = text.last()
    }

    private companion object {
        val inlineTags = setOf("b", "strong", "i", "em", "u", "a")
    }
}

private fun AnnotatedString.Builder.pushStyleFor(tag: String, attributes: String, linkColor: Color) {
    when (tag) {
        "b", "strong" -> pushStyle(SpanStyle(fontWeight = FontWeight.Bold))

        "i", "em" -> pushStyle(SpanStyle(fontStyle = FontStyle.Italic))

        "u" -> pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))

        else -> {
            val href = hrefRegex.find(attributes)?.groupValues?.get(1)
            if (href.isNullOrBlank()) {
                pushStyle(SpanStyle())
            } else {
                val style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                pushLink(LinkAnnotation.Url(href, TextLinkStyles(style = style)))
            }
        }
    }
}

private fun AnnotatedString.trimEnd(): AnnotatedString {
    val end = text.trimEnd().length
    return if (end == text.length) this else subSequence(0, end)
}
