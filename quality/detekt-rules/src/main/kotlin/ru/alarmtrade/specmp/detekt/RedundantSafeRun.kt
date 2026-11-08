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
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Обёртка того, что уже возвращает `Response` (docs/patterns.md, «Data — репозитории»):
 * - `safeRun { … }`, последнее выражение которого — `Response`: результат `Response<Response<T>>`, ошибка
 *   внутреннего `Response` не всплывает;
 * - `safeResponseRun { repository.foo(id) }` — блок из одного вызова, возвращающего `Response`, с простыми
 *   ресивером и аргументами (имена, `this`, литералы): бросать там нечему, контракт `Response` уже это гарантирует.
 *
 * Требует type resolution: в задачах detekt без классов правило молчит (как StateFlowValueMutation).
 */
class RedundantSafeRun(config: Config) :
    Rule(config, "Не оборачивать в safeRun/safeResponseRun то, что уже возвращает Response."),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text
        if (callee != SAFE_RUN && callee != SAFE_RESPONSE_RUN) return
        val statements = expression.lambdaArguments.singleOrNull()
            ?.getLambdaExpression()?.bodyExpression?.statements
            ?: return
        val last = statements.lastOrNull() ?: return
        when {
            callee == SAFE_RUN && last.isResponse() -> report(
                Finding(
                    Entity.from(expression),
                    "safeRun вокруг Response даёт Response<Response<T>>: вызов уже не бросает — уберите обёртку " +
                        "или используйте map*/mapIfSuccess (docs/patterns.md, «Data — репозитории»).",
                ),
            )

            callee == SAFE_RESPONSE_RUN && statements.size == 1 && last.isSimpleCall() && last.isResponse() -> report(
                Finding(
                    Entity.from(expression),
                    "safeResponseRun вокруг одного вызова, который уже возвращает Response: обёртка избыточна " +
                        "(docs/patterns.md, «Data — репозитории»).",
                ),
            )
        }
    }

    private fun KtExpression.isResponse(): Boolean = analyze(this) {
        expressionType?.isSubtypeOf(RESPONSE) == true
    }

    private fun KtExpression.isSimpleCall(): Boolean = when (this) {
        is KtCallExpression -> !invokesFunctionValue() &&
            valueArguments.all { it.getArgumentExpression()?.isSimpleValue() == true } &&
            lambdaArguments.isEmpty()

        is KtDotQualifiedExpression -> receiverExpression.isSimpleValue() &&
            selectorExpression?.isSimpleCall() == true

        else -> false
    }

    // Вызов значения функционального типа (`block()`, `it(value)`, `x.invoke()`) — произвольный код, он может бросать.
    private fun KtCallExpression.invokesFunctionValue(): Boolean {
        val name = (calleeExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return true
        if (name == INVOKE || name == IT) return true
        return parents.any { scope ->
            when (scope) {
                is KtFunction -> scope.valueParameters.any { it.name == name }
                is KtClass -> scope.primaryConstructorParameters.any { it.name == name }
                is KtBlockExpression -> scope.statements.any { it is KtProperty && it.name == name }
                else -> false
            }
        }
    }

    private fun KtExpression.isSimpleValue(): Boolean = when (this) {
        is KtNameReferenceExpression, is KtThisExpression, is KtConstantExpression -> true

        is KtStringTemplateExpression -> !hasInterpolation()

        is KtDotQualifiedExpression -> receiverExpression.isSimpleValue() &&
            selectorExpression is KtNameReferenceExpression

        else -> false
    }

    private companion object {
        const val SAFE_RUN = "safeRun"
        const val SAFE_RESPONSE_RUN = "safeResponseRun"
        const val INVOKE = "invoke"
        const val IT = "it"
        val RESPONSE = ClassId(FqName("ru.alarmtrade.specmp.core.common"), Name.identifier("Response"))
    }
}
