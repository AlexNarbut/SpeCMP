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
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/**
 * Результат `Response` молча выброшен (docs/patterns.md, «Контракт результата»): вызов, возвращающий `Response`
 * (или `Response?` после `?.`), стоит оператором — в блоке, последней строкой `Unit`-лямбды (`safeLaunch { repo.x() }`,
 * `intent { }`), веткой `if` без `else`, `withContext(io) { repo.x() }` оператором, `x.also { … }`.
 *
 * Обработкой считается: значение используется (присваивание, `return`, аргумент, условие, `when`, expression-body,
 * результат лямбды не-`Unit`); цепочка оканчивается `onError` — решение видно
 * на месте (в том числе последней строкой лямбды обёртки: `withContext(io) { x.onError { } }`); оператор
 * `safeRun { }`/`safeResponseRun { }` — сознательный «поймать и проглотить» на границе (логирует сам).
 * «Огонь и забудь» — `.onError { logger.w(it) { … } }`.
 *
 * Требует type resolution: в задачах detekt без классов правило молчит (как RedundantSafeRun).
 */
class ResponseIgnored(config: Config) :
    Rule(config, "Результат Response нельзя молча отбрасывать: обработайте ошибку или используйте значение."),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val outer = expression.outermostOfChain() ?: return
        if (outer.isHandled()) return
        val ignored = analyze(outer) {
            val type = outer.expressionType ?: return@analyze false
            type.isSubtypeOf(RESPONSE) && !outer.isUsedAsExpression
        }
        if (!ignored) return
        report(
            Finding(
                Entity.from(outer),
                "Результат Response отброшен: ошибка теряется молча. Обработайте её — " +
                    "`.onError { logger.w(it) { … } }`, when, ошибка в state/эффект — или используйте " +
                    "значение (docs/patterns.md, «Контракт результата»).",
            ),
        )
    }

    /**
     * Внешнее выражение цепочки, которой [this] — последний вызов (`a.b().c()` для `c()`); `null`, если вызов —
     * ресивер следующего звена (его проверит внешний вызов).
     */
    private fun KtCallExpression.outermostOfChain(): KtExpression? {
        val parent = parent
        val outer: KtExpression =
            if (parent is KtQualifiedExpression && parent.selectorExpression == this) parent else this
        val grand = outer.parent
        if (grand is KtQualifiedExpression && grand.receiverExpression == outer) return null
        return outer
    }

    // `x.onError { }` или обёртка, чья лямбда оканчивается такой цепочкой: `withContext(io) { x.onError { } }`.
    private fun KtExpression.isHandled(): Boolean {
        val call =
            (if (this is KtQualifiedExpression) selectorExpression else this) as? KtCallExpression ?: return false
        if (call.calleeExpression?.text in HANDLED) return true
        val last = call.lambdaArguments.lastOrNull()?.getLambdaExpression()?.bodyExpression?.statements?.lastOrNull()
        return last?.isHandled() == true
    }

    private companion object {
        val HANDLED = setOf("onError", "safeRun", "safeResponseRun")
        val RESPONSE = ClassId(FqName("ru.alarmtrade.specmp.core.common"), Name.identifier("Response"))
    }
}
