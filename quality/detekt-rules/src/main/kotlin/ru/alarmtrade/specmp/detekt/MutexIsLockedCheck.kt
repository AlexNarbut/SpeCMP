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
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/**
 * Чтение `Mutex.isLocked` (docs/patterns.md, «Конкурентность»): «проверить и захватить» не атомарно — два вызова
 * проходят проверку до захвата. Ровно-один-в-полёте — `tryLock()`, очередь — `withLock { }`.
 *
 * Требует type resolution: в задачах detekt без классов правило молчит.
 */
class MutexIsLockedCheck(config: Config) :
    Rule(config, "Mutex.isLocked — check-then-act: используйте tryLock() или withLock { }."),
    RequiresAnalysisApi {

    override fun visitQualifiedExpression(expression: KtQualifiedExpression) {
        super.visitQualifiedExpression(expression)
        val selector = expression.selectorExpression as? KtNameReferenceExpression ?: return
        if (selector.getReferencedName() != IS_LOCKED) return
        val receiver = expression.receiverExpression
        val isMutex = analyze(receiver) { receiver.expressionType?.isSubtypeOf(MUTEX) == true }
        if (isMutex) {
            report(
                Finding(
                    Entity.from(expression),
                    "`${expression.text}`: проверка и захват не атомарны — оба конкурентных вызова пройдут " +
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
