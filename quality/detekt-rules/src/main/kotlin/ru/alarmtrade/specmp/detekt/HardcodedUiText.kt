package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotatedExpression
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/** UI-текст — из `core/localization`; литерал без букв (`"$a / $b"`, `"•"`) — не текст. По именам. */
class HardcodedUiText(config: Config) :
    Rule(config, "Текст в composable — только stringResource(Strings.string.*), не строковый литерал.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (!expression.isInComposable()) return
        val callee = expression.calleeExpression?.text
        expression.valueArguments.forEachIndexed { index, argument ->
            val name = argument.getArgumentName()?.asName?.asString()
            val isText = when {
                name == LABEL && callee.isAnimation() -> false
                name != null -> name in UI_PARAMS
                else -> index == 0 && callee in TEXT_CALLS
            }
            val value = argument.getArgumentExpression()
            if (isText && value != null && value.isTextLiteral()) {
                report(Finding(Entity.from(value), MESSAGE))
            }
        }
    }

    // `label` анимаций Compose — имя для инспектора, не текст на экране.
    private fun String?.isAnimation(): Boolean =
        this != null && (startsWith(ANIMATE) || contains(TRANSITION) || this in ANIMATED_CONTAINERS)

    private fun KtAnnotated.hasAnnotation(name: String): Boolean =
        annotationEntries.any { it.shortName?.asString() == name }

    private fun KtElement.isInComposable(): Boolean {
        for (parent in parents) {
            when (parent) {
                is KtNamedFunction -> return parent.hasAnnotation(COMPOSABLE) && !parent.hasAnnotation(PREVIEW)
                is KtLambdaExpression -> if (parent.isComposableLambda()) return true
            }
        }
        return false
    }

    private fun KtLambdaExpression.isComposableLambda(): Boolean {
        val annotated = parent as? KtAnnotatedExpression
        if (annotated != null && annotated.annotationEntries.any { it.shortName?.asString() == COMPOSABLE }) {
            return true
        }
        val property = parent as? KtProperty ?: return false
        return property.typeReference?.text?.contains("@$COMPOSABLE") == true
    }

    private fun KtExpression.isTextLiteral(): Boolean = when (this) {
        is KtStringTemplateExpression ->
            entries.filterIsInstance<KtLiteralStringTemplateEntry>().any { entry -> entry.text.any(Char::isLetter) }

        is KtParenthesizedExpression -> expression?.isTextLiteral() == true

        is KtBinaryExpression ->
            operationToken in TEXT_OPERATIONS &&
                (left?.isTextLiteral() == true || right?.isTextLiteral() == true)

        is KtIfExpression -> then?.isTextLiteral() == true || `else`?.isTextLiteral() == true

        is KtWhenExpression -> entries.any { it.expression?.isTextLiteral() == true }

        is KtBlockExpression -> statements.lastOrNull()?.isTextLiteral() == true

        is KtDotQualifiedExpression -> receiverExpression.isTextLiteral()

        is KtNameReferenceExpression -> localTextValue(this)

        else -> false
    }

    private fun localTextValue(reference: KtNameReferenceExpression): Boolean {
        val function = reference.getStrictParentOfType<KtNamedFunction>() ?: return false
        return function.collectDescendantsOfType<KtProperty>()
            .filter { it.isLocal && it.name == reference.getReferencedName() && it.textOffset < reference.textOffset }
            .lastOrNull()
            ?.initializer
            ?.isTextLiteral() == true
    }

    private companion object {
        const val COMPOSABLE = "Composable"
        const val PREVIEW = "Preview"
        val TEXT_OPERATIONS = setOf(KtTokens.PLUS, KtTokens.ELVIS)
        const val LABEL = "label"
        const val ANIMATE = "animate"
        const val TRANSITION = "Transition"
        val ANIMATED_CONTAINERS = setOf("AnimatedContent", "AnimatedVisibility", "Crossfade")
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
