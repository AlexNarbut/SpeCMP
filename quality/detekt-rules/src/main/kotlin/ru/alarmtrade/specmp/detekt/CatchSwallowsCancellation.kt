package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

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
            if (clauses.take(index).any { it.catchesCancellationExplicitly() }) return
            if (clause.rethrowsCancellation()) return@forEachIndexed
            val swallows = analyze(expression) {
                val type = clause.catchParameter?.typeReference?.type as? KaClassType ?: return@analyze false
                type.classId.asSingleFqName().asString() in BROAD &&
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

    private fun KtCatchClause.catchesCancellationExplicitly(): Boolean =
        catchParameter?.typeReference?.text?.endsWith(CANCELLATION) == true

    private fun KtCatchClause.rethrowsCancellation(): Boolean {
        val body = catchBody ?: return false
        val parameter = catchParameter?.name
        val checksCancellation = body.collectDescendantsOfType<KtIsExpression>().any {
            !it.isNegated && it.typeReference?.text?.endsWith(CANCELLATION) == true
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

    private companion object {
        const val CANCELLATION = "CancellationException"
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
