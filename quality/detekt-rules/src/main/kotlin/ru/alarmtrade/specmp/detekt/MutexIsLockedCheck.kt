package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

/** «Проверить и захватить» не атомарно: ровно-один-в-полёте — `tryLock()`, очередь — `withLock { }`. */
class MutexIsLockedCheck(config: Config) :
    Rule(config, "Mutex.isLocked — check-then-act: используйте tryLock() или withLock { }."),
    RequiresAnalysisApi {

    @OptIn(KaExperimentalApi::class)
    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        if (expression !is KtNameReferenceExpression || expression.getReferencedName() != IS_LOCKED) return
        val isMutex = analyze(expression) {
            val symbol = expression.resolveSymbol() as? KaCallableSymbol ?: return@analyze false
            (sequenceOf(symbol) + symbol.allOverriddenSymbols).any { it.callableId?.classId == MUTEX }
        }
        if (isMutex) {
            val shown = expression.getQualifiedExpressionForSelector() ?: expression
            report(
                Finding(
                    Entity.from(shown),
                    "`${shown.text}`: проверка и захват не атомарны — оба конкурентных вызова пройдут " +
                        "проверку. Используйте tryLock() (ровно один в полёте) или withLock { } " +
                        "(docs/patterns.md, «Конкурентность»).",
                ),
            )
        }
    }

    private companion object {
        const val IS_LOCKED = "isLocked"
        val MUTEX = ClassId(FqName("kotlinx.coroutines.sync"), Name.identifier("Mutex"))
    }
}
