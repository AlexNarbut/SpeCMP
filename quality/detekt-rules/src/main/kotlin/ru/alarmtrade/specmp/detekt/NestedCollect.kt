package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Внешний поток останавливается на первом значении, пока жив внутренний. Комбинация — `flatMapLatest`/`combine` и
 * один `collect`. Ловит и сбор в функции своего класса/файла, вызванной (или переданной ссылкой) из `collect { }`.
 * По именам, без type resolution.
 */
class NestedCollect(config: Config) :
    Rule(
        config,
        "Вложенный collect: потоки комбинируются операторами (flatMapLatest/combine), а не вложенным сбором.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        val outer = expression.outerCollect()
        when {
            name in COLLECTS && outer != null -> report(
                expression,
                "`$name` внутри `${outer.calleeExpression?.text} { }`",
            )

            outer != null && expression.helperCollects(name) ->
                report(expression, "`$name()` собирает поток внутри `${outer.calleeExpression?.text} { }`")

            name in OUTER_COLLECTS ->
                expression.valueArguments
                    .mapNotNull { it.getArgumentExpression() as? KtCallableReferenceExpression }
                    .filter { expression.helperCollects(it.callableReference.getReferencedName()) }
                    .forEach { report(expression, "`${it.text}` собирает поток внутри `$name`") }
        }
    }

    private fun report(expression: KtCallExpression, what: String) {
        report(
            Finding(
                Entity.from(expression),
                "$what: внутренний сбор держит внешний поток. Скомбинируйте потоки — flatMapLatest/combine — " +
                    "и соберите один раз (docs/patterns.md, «Корутины и Flow»).",
            ),
        )
    }

    private fun KtCallExpression.outerCollect(): KtCallExpression? = parents
        .takeWhile { it !is KtNamedFunction && it !is KtClassOrObject }
        .filterIsInstance<KtLambdaExpression>()
        .mapNotNull { lambda -> lambda.getStrictParentOfType<KtCallExpression>()?.takeIf { lambda.isArgumentOf(it) } }
        .firstOrNull { call -> call.calleeExpression?.text in OUTER_COLLECTS }

    private fun KtLambdaExpression.isArgumentOf(call: KtCallExpression): Boolean =
        call.lambdaArguments.any { it.getLambdaExpression() == this } ||
            call.valueArguments.any { it.getArgumentExpression() == this }

    private fun KtCallExpression.helperCollects(name: String): Boolean {
        if (name in COLLECTS) return false
        val owner = getStrictParentOfType<KtClassOrObject>()
        val declarations = owner?.declarations ?: containingKtFile.declarations
        return declarations.filterIsInstance<KtNamedFunction>()
            .filter { it.name == name }
            .any { function ->
                function.collectDescendantsOfType<KtCallExpression>().any { it.calleeExpression?.text in COLLECTS }
            }
    }

    private companion object {
        val COLLECTS = setOf("collect", "collectLatest", "collectIndexed")
        val OUTER_COLLECTS = COLLECTS + "onEach"
    }
}
