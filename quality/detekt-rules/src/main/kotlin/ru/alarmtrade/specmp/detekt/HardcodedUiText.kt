package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/** UI-текст — из `core/localization`; литерал без букв (`"$a / $b"`, `"•"`) — не текст. По именам. */
class HardcodedUiText(config: Config) :
    Rule(config, "Текст в composable — только stringResource(Strings.string.*), не строковый литерал.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val function = expression.getStrictParentOfType<KtNamedFunction>() ?: return
        if (!function.hasAnnotation(COMPOSABLE) || function.hasAnnotation(PREVIEW)) return
        val callee = expression.calleeExpression?.text
        expression.valueArguments.forEachIndexed { index, argument ->
            val name = argument.getArgumentName()?.asName?.asString()
            val isText = if (name != null) name in UI_PARAMS else index == 0 && callee in TEXT_CALLS
            val value = argument.getArgumentExpression()
            if (isText && value != null && value.isTextLiteral()) {
                report(Finding(Entity.from(value), MESSAGE))
            }
        }
    }

    private fun KtNamedFunction.hasAnnotation(name: String): Boolean =
        annotationEntries.any { it.shortName?.asString() == name }

    private fun KtExpression.isTextLiteral(): Boolean = this is KtStringTemplateExpression &&
        entries.filterIsInstance<KtLiteralStringTemplateEntry>().any { entry -> entry.text.any(Char::isLetter) }

    private companion object {
        const val COMPOSABLE = "Composable"
        const val PREVIEW = "Preview"
        val TEXT_CALLS = setOf("Text", "CmpText", "CmpHtmlText")
        val UI_PARAMS = setOf(
            "text",
            "title",
            "subtitle",
            "label",
            "placeholder",
            "contentDescription",
            "message",
            "actionLabel",
        )
        const val MESSAGE =
            "Строковый литерал в UI: текст и contentDescription — через stringResource(Strings.string.*) из " +
                "core/localization (docs/localization.md); строки логики — const val."
    }
}
