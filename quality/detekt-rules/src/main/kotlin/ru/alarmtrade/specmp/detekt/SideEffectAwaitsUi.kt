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
 * `collectSideEffect` обрабатывает эффекты по очереди: снекбар, ожидающий закрытия, держит следующие эффекты
 * (навигацию, выход). По именам, без type resolution; `launch { }` внутри обработчика снимает запрет.
 */
class SideEffectAwaitsUi(config: Config) :
    Rule(
        config,
        "Обработчик collectSideEffect не ждёт UI: снекбар — в rememberCoroutineScope().launch { }.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text
        if (name !in AWAITING_UI) return
        val enclosing = expression.parents
            .takeWhile { it !is KtNamedFunction && it !is KtClassOrObject }
            .filterIsInstance<KtLambdaExpression>()
            .mapNotNull { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text }
            .firstOrNull { it in LAUNCHERS || it == COLLECT_SIDE_EFFECT }
        if (enclosing != COLLECT_SIDE_EFFECT) return
        report(
            Finding(
                Entity.from(expression),
                "`$name` прямо в `collectSideEffect { }`: обработчики идут по очереди, и пока снекбар открыт, " +
                    "следующие эффекты (навигация, «Назад») ждут. Запускайте его в " +
                    "`rememberCoroutineScope().launch { }` (docs/patterns.md, «One-shot события (Effects)»).",
            ),
        )
    }

    private companion object {
        const val COLLECT_SIDE_EFFECT = "collectSideEffect"
        val AWAITING_UI = setOf("showSnackbar", "showResult")
        val LAUNCHERS = setOf("launch", "async")
    }
}
