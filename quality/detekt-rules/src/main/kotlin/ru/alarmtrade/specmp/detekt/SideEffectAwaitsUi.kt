package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `collectSideEffect` обрабатывает эффекты по очереди: снекбар, ожидающий закрытия, держит следующие эффекты
 * (навигацию, выход). По именам, без type resolution; `launch { }` внутри обработчика снимает запрет. Функция того же
 * файла, которая сама ждёт снекбар, считается ожидающей.
 */
class SideEffectAwaitsUi(config: Config) :
    Rule(
        config,
        "Обработчик collectSideEffect не ждёт UI: снекбар — в rememberCoroutineScope().launch { }.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        check(expression, expression.calleeExpression?.text)
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        check(expression, expression.callableReference.getReferencedName())
    }

    private fun check(expression: KtExpression, name: String?) {
        if (name == null) return
        val file = expression.containingKtFile
        if (name !in file.namesWithAliases(AWAITING_UI) && name !in file.awaitingFunctions()) return
        if (expression.enclosingHandler(file) != COLLECT_SIDE_EFFECT) return
        report(
            Finding(
                Entity.from(expression),
                "`$name` прямо в `collectSideEffect { }`: обработчики идут по очереди, и пока снекбар открыт, " +
                    "следующие эффекты (навигация, «Назад») ждут. Запускайте его в " +
                    "`rememberCoroutineScope().launch { }` (docs/patterns.md, «One-shot события (Effects)»).",
            ),
        )
    }

    private fun KtExpression.enclosingHandler(file: KtFile): String? {
        val handlers = file.namesWithAliases(setOf(COLLECT_SIDE_EFFECT))
        return parents
            .takeWhile { it !is KtNamedFunction && it !is KtClassOrObject }
            .filterIsInstance<KtLambdaExpression>()
            .mapNotNull { it.getStrictParentOfType<KtCallExpression>() }
            .mapNotNull { call ->
                val callee = call.calleeExpression?.text
                when {
                    callee in handlers -> COLLECT_SIDE_EFFECT
                    callee in LAUNCHERS && !call.isAwaited() -> callee
                    else -> null
                }
            }
            .firstOrNull()
    }

    // `scope.async { showSnackbar() }.await()` ждёт так же, как прямой вызов.
    private fun KtCallExpression.isAwaited(): Boolean {
        val qualified = parent as? KtQualifiedExpression ?: return false
        if (qualified.selectorExpression != this) return false
        val next = qualified.parent as? KtQualifiedExpression ?: return false
        return next.receiverExpression == qualified && (next.selectorExpression as? KtCallExpression)
            ?.calleeExpression?.text == AWAIT
    }

    private fun KtFile.awaitingFunctions(): Set<String> = collectDescendantsOfType<KtNamedFunction>()
        .filter { function ->
            function.collectDescendantsOfType<KtCallExpression>().any { call ->
                call.calleeExpression?.text in AWAITING_UI && call.parents
                    .takeWhile { it != function }
                    .filterIsInstance<KtLambdaExpression>()
                    .none { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text in LAUNCHERS }
            }
        }
        .mapNotNull { it.name }
        .toSet()

    private companion object {
        const val COLLECT_SIDE_EFFECT = "collectSideEffect"
        const val AWAIT = "await"
        val AWAITING_UI = setOf("showSnackbar", "showResult")
        val LAUNCHERS = setOf("launch", "async")
    }
}
