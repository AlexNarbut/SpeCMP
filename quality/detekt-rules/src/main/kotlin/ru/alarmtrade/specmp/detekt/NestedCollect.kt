package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Внешний поток останавливается на первом значении, пока жив внутренний. Комбинация — `flatMapLatest`/`combine` и
 * один `collect`. По именам, без type resolution.
 */
class NestedCollect(config: Config) :
    Rule(
        config,
        "Вложенный collect: потоки комбинируются операторами (flatMapLatest/combine), а не вложенным сбором.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text
        if (name !in COLLECTS) return
        val outer = expression.parents
            .takeWhile { it !is KtNamedFunction && it !is KtClassOrObject }
            .filterIsInstance<KtLambdaExpression>()
            .mapNotNull { it.getStrictParentOfType<KtCallExpression>() }
            .firstOrNull { call -> call.calleeExpression?.text in COLLECTS && call.lambdaArguments.isNotEmpty() }
            ?: return
        report(
            Finding(
                Entity.from(expression),
                "`$name` внутри `${outer.calleeExpression?.text} { }`: внутренний сбор держит внешний поток. " +
                    "Скомбинируйте потоки — flatMapLatest/combine — и соберите один раз " +
                    "(docs/patterns.md, «Корутины и Flow»).",
            ),
        )
    }

    private companion object {
        val COLLECTS = setOf("collect", "collectLatest")
    }
}
