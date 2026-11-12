package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtWhileExpression

/** Без точки приостановки отмена корутины до цикла не доходит: он крутится до конца процесса. Требует type resolution. */
class LoopWithoutCancellation(config: Config) :
    Rule(config, "Бесконечный цикл в корутине содержит точку отмены: suspend-вызов (delay, emit, …) или isActive."),
    RequiresAnalysisApi {

    override fun visitWhileExpression(expression: KtWhileExpression) {
        super.visitWhileExpression(expression)
        val condition = expression.condition as? KtConstantExpression ?: return
        if (condition.text != TRUE) return
        val body = expression.body ?: return
        val stuck = analyze(expression) {
            isInCoroutine(expression) &&
                inlineCalls(body).none { it.calleeExpression?.text == ENSURE_ACTIVE || isSuspendCall(it) }
        }
        if (stuck) {
            report(
                Finding(
                    Entity.from(expression),
                    "while (true) в корутине без suspend-вызова не отменяется: добавьте точку приостановки " +
                        "(delay/yield/emit) или крутите по isActive / ensureActive() (docs/patterns.md, «Корутины и Flow»).",
                ),
            )
        }
    }

    private companion object {
        const val TRUE = "true"
        const val ENSURE_ACTIVE = "ensureActive"
    }
}
