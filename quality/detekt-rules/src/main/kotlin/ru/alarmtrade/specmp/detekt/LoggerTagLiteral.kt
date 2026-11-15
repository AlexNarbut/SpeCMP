package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/** Литерал тега отстаёт от переименования класса; у верхнеуровневого логгера файла класса нет — там литерал допустим. */
class LoggerTagLiteral(config: Config) :
    Rule(config, "Тег логгера класса — this::class.simpleName, не строковый литерал.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val tag = expression.tagArgument() ?: return
        if (!tag.isLiteralTag() || expression.getStrictParentOfType<KtClassOrObject>() == null) return
        report(
            Finding(
                Entity.from(tag),
                "Тег логгера литералом: Logger.withTag(this::class.simpleName.orEmpty()) (docs/patterns.md, " +
                    "«Логирование»); во ViewModel — готовый logger из BaseViewModel.",
            ),
        )
    }

    private fun KtCallExpression.tagArgument(): KtExpression? {
        val file = containingKtFile
        return when (calleeExpression?.text) {
            in file.namesWithAliases(WITH_TAG) -> valueArguments.singleOrNull()?.getArgumentExpression()

            in file.namesWithAliases(LOGGER) ->
                valueArguments
                    .firstOrNull { it.getArgumentName()?.asName?.asString() == TAG }
                    ?.getArgumentExpression()
                    ?: valueArguments.getOrNull(TAG_POSITION)?.takeIf { !it.isNamed() }?.getArgumentExpression()

            else -> null
        }
    }

    private fun KtExpression.isLiteralTag(): Boolean = when (this) {
        is KtStringTemplateExpression -> true

        is KtParenthesizedExpression -> expression?.isLiteralTag() == true

        is KtBinaryExpression ->
            operationToken == KtTokens.PLUS &&
                (left?.isLiteralTag() == true || right?.isLiteralTag() == true)

        is KtNameReferenceExpression -> containingKtFile.collectDescendantsOfType<KtProperty>()
            .filter { !it.isLocal && !it.isVar && it.name == getReferencedName() }
            .any { it.initializer?.isLiteralTag() == true }

        else -> false
    }

    private companion object {
        val WITH_TAG = setOf("withTag")
        val LOGGER = setOf("Logger")
        const val TAG = "tag"
        const val TAG_POSITION = 1
    }
}
