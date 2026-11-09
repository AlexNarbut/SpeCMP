package ru.alarmtrade.specmp.detekt

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Репозиторий и use case не бросают (docs/patterns.md, «Контракт результата»): в файле, объявляющем класс
 * (не интерфейс) `*Repository` (реализации `Specialist*Repository`)/`*RepositoryImpl`/`*UseCase`, `throw`, `error()`, `check`/`checkNotNull`/`require`/`requireNotNull` и `!!` допустимы
 * только внутри границы, превращающей исключение в `Response`: лямбда `safeRun { }`/`safeResponseRun { }`/
 * `safeFlowRun { }` (в том числе тело `= safeRun { }` и лямбды операторов потока внутри `safeFlowRun`). Private-хелпер,
 * вызываемый из `safeRun`, правило не видит — он возвращает `null`/`Response`, а бросает уже лямбда.
 *
 * Проброс отмены разрешён везде: `catch (ce: CancellationException) { throw ce }` и
 * `if (e is CancellationException) throw e`. Проверка по именам, без типов — работает во всех source set'ах.
 */
class ThrowInRepository(config: Config) :
    Rule(config, "Репозиторий/use case не бросает: ошибка возвращается как Response.Error.") {

    override fun visitThrowExpression(expression: KtThrowExpression) {
        super.visitThrowExpression(expression)
        if (expression.isCancellationRethrow()) return
        check(expression, "throw")
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name !in THROWING_CALLS) return
        val parent = expression.parent
        if (parent is KtQualifiedExpression && parent.selectorExpression == expression) return
        check(expression, "$name()")
    }

    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        if (expression.operationToken == KtTokens.EXCLEXCL) check(expression, "!!")
    }

    private fun check(element: KtExpression, what: String) {
        val owner = element.containingKtFile.collectDescendantsOfType<KtClassOrObject>()
            .firstOrNull { declaration ->
                !(declaration is KtClass && declaration.isInterface()) &&
                    SUFFIXES.any { declaration.name.orEmpty().endsWith(it) }
            }
            ?: return
        if (element.isInsideBoundary()) return
        report(
            Finding(
                Entity.from(element),
                "$what в ${owner.name} вне safeRun/safeResponseRun/safeFlowRun: репозиторий и use case " +
                    "не бросают. Верните Response.Error (return@safeResponseRun Response.Error(…)) или null " +
                    "из хелпера и бросайте внутри лямбды safeRun { } (docs/patterns.md, «Контракт результата»).",
            ),
        )
    }

    private fun PsiElement.isInsideBoundary(): Boolean = parents
        .takeWhile { it !is KtNamedFunction }
        .filterIsInstance<KtLambdaExpression>()
        .any { lambda -> lambda.ownerCall()?.isBoundary() == true }

    private fun KtLambdaExpression.ownerCall(): KtCallExpression? = when (val parent = parent) {
        is KtLambdaArgument -> parent.parent as? KtCallExpression
        is KtValueArgument -> (parent.parent as? KtValueArgumentList)?.parent as? KtCallExpression
        else -> null
    }

    private fun KtCallExpression.isBoundary(): Boolean = calleeExpression?.text in BOUNDARY_CALLS

    private fun KtThrowExpression.isCancellationRethrow(): Boolean {
        val name = (thrownExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return false
        val catchClause = parents.filterIsInstance<KtCatchClause>().firstOrNull()
        val parameter = catchClause?.catchParameter
        if (parameter?.name == name && parameter.typeReference?.text.isCancellation()) return true
        return parents.filterIsInstance<KtIfExpression>().any { ifExpression ->
            val condition = ifExpression.condition as? KtIsExpression ?: return@any false
            !condition.isNegated &&
                condition.leftHandSide.text == name &&
                condition.typeReference?.text.isCancellation() &&
                PsiTreeUtil.isAncestor(ifExpression.then, this, false)
        }
    }

    private fun String?.isCancellation(): Boolean = this?.endsWith(CANCELLATION_EXCEPTION) == true

    private companion object {
        val SUFFIXES = listOf("Repository", "RepositoryImpl", "UseCase")
        val THROWING_CALLS = setOf("error", "check", "checkNotNull", "require", "requireNotNull")
        val BOUNDARY_CALLS = setOf("safeRun", "safeResponseRun", "safeFlowRun")
        const val CANCELLATION_EXCEPTION = "CancellationException"
    }
}
