package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtWhenConditionIsPattern
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor

/**
 * `catch (Throwable/Exception/…)` вокруг suspend-вызова в корутине глотает отмену. Не срабатывает, если отмена
 * пробрасывается: `catch (e: CancellationException)` выше, `if (e is CancellationException) throw e`, `ensureActive()`.
 */
class CatchSwallowsCancellation(config: Config) :
    Rule(config, "Широкий catch в корутине пробрасывает CancellationException."),
    RequiresAnalysisApi {

    override fun visitTryExpression(expression: KtTryExpression) {
        super.visitTryExpression(expression)
        val clauses = expression.catchClauses
        clauses.forEachIndexed { index, clause ->
            val earlierCatchesCancellation = analyze(expression) {
                clauses.take(index).any { isCancellation(it.catchParameter?.typeReference) }
            }
            if (earlierCatchesCancellation) return
            val swallows = analyze(expression) {
                val type = clause.catchParameter?.typeReference?.type as? KaClassType ?: return@analyze false
                type.classId.asSingleFqName().asString() in BROAD &&
                    !rethrowsCancellation(clause) &&
                    isInCoroutine(expression) &&
                    inlineCalls(expression.tryBlock).any { isSuspendCall(it) }
            }
            if (swallows) {
                report(
                    Finding(
                        Entity.from(clause),
                        "catch (${clause.catchParameter?.typeReference?.text}) в корутине глотает " +
                            "CancellationException: используйте safeRun { } или пробросьте отмену — " +
                            "`catch (e: CancellationException) { throw e }` выше или " +
                            "`if (e is CancellationException) throw e` (docs/patterns.md, «Корутины и Flow»).",
                    ),
                )
            }
        }
    }

    private fun KaSession.isCancellation(type: KtTypeReference?): Boolean =
        (type?.type as? KaClassType)?.classId?.asSingleFqName()?.asString() in CANCELLATIONS

    private fun KaSession.rethrowsCancellation(clause: KtCatchClause): Boolean {
        val body = clause.catchBody ?: return false
        val parameter = clause.catchParameter?.name
        val checksCancellation = body.collectDescendantsOfType<KtIsExpression>().any {
            !it.isNegated && isCancellation(it.typeReference) && it.guardsThrow()
        } || body.collectDescendantsOfType<KtWhenConditionIsPattern>().any {
            !it.isNegated && isCancellation(it.typeReference) &&
                it.getStrictParentOfType<KtWhenEntry>()?.expression.containsThrow()
        }
        val ensuresActive = body.collectDescendantsOfType<KtCallExpression>().any {
            it.calleeExpression?.text == ENSURE_ACTIVE
        }
        val statements = (body as? KtBlockExpression)?.statements ?: listOf(body)
        val rethrows = statements.any { statement ->
            ((statement as? KtThrowExpression)?.thrownExpression as? KtNameReferenceExpression)
                ?.getReferencedName() == parameter
        }
        return checksCancellation || ensuresActive || rethrows
    }

    private fun KtIsExpression.guardsThrow(): Boolean {
        val ifExpression = getStrictParentOfType<KtIfExpression>() ?: return false
        return ifExpression.condition?.isAncestor(this) == true && ifExpression.then.containsThrow()
    }

    private fun KtExpression?.containsThrow(): Boolean =
        this is KtThrowExpression || this?.collectDescendantsOfType<KtThrowExpression>()?.isNotEmpty() == true

    private companion object {
        val CANCELLATIONS = setOf(
            "kotlin.coroutines.cancellation.CancellationException",
            "java.util.concurrent.CancellationException",
        )
        const val ENSURE_ACTIVE = "ensureActive"
        val BROAD = setOf(
            "kotlin.Throwable",
            "kotlin.Exception",
            "kotlin.RuntimeException",
            "kotlin.IllegalStateException",
            "java.lang.Throwable",
            "java.lang.Exception",
            "java.lang.RuntimeException",
            "java.lang.IllegalStateException",
        )
    }
}
