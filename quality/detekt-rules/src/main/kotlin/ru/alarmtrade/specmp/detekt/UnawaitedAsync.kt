package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/** Исключение такой корутины не дойдёт до вызывающего; «огонь и забудь» — `launch { }`. Требует type resolution. */
class UnawaitedAsync(config: Config) :
    Rule(config, "Deferred из async { } нужно дождаться (await/awaitAll); без результата — launch { }."),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text !in expression.containingKtFile.namesWithAliases(setOf(ASYNC))) return
        val outer: KtExpression = (expression.parent as? KtQualifiedExpression)
            ?.takeIf { it.selectorExpression == expression } ?: expression
        val unawaited = analyze(outer) {
            if (outer.expressionType?.isSubtypeOf(DEFERRED) != true) return@analyze false
            !outer.isUsedAsExpression || outer.isUnusedLocal()
        }
        if (unawaited) {
            report(
                Finding(
                    Entity.from(expression),
                    "Результат async { } не используется: исключение потеряется до await(). Дождитесь его " +
                        "(await/awaitAll) или используйте launch { } (docs/patterns.md, «Корутины и Flow»).",
                ),
            )
        }
    }

    private fun KtExpression.isUnusedLocal(): Boolean {
        val property = parent as? KtProperty ?: return false
        if (!property.isLocal || property.initializer != this) return false
        val block = property.parent as? KtBlockExpression ?: return false
        return block.collectDescendantsOfType<KtNameReferenceExpression>()
            .none { it.getReferencedName() == property.name }
    }

    private companion object {
        const val ASYNC = "async"
        val DEFERRED = ClassId(FqName("kotlinx.coroutines"), Name.identifier("Deferred"))
    }
}
