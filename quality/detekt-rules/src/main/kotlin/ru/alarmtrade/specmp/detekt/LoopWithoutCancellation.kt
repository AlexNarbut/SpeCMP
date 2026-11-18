package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.base.KaConstantValue
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDoWhileExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtWhileExpression
import org.jetbrains.kotlin.psi.KtWhileExpressionBase

/** Бесконечный цикл в корутине крутится по `isActive` корутинного контекста, а не по константе. Требует type resolution. */
class LoopWithoutCancellation(config: Config) :
    Rule(config, "Бесконечный цикл в корутине — только по isActive корутинного контекста, while (true) запрещён."),
    RequiresAnalysisApi {

    override fun visitWhileExpression(expression: KtWhileExpression) {
        super.visitWhileExpression(expression)
        check(expression)
    }

    override fun visitDoWhileExpression(expression: KtDoWhileExpression) {
        super.visitDoWhileExpression(expression)
        check(expression)
    }

    private fun check(expression: KtWhileExpressionBase) {
        val condition = expression.condition ?: return
        val problem = analyze(expression) {
            when {
                !isInCoroutine(expression) -> null
                (condition.evaluate() as? KaConstantValue.BooleanValue)?.value == true -> CONSTANT
                conjuncts(condition).any { isForeignIsActive(it) } -> FOREIGN
                else -> null
            }
        } ?: return
        report(
            Finding(
                Entity.from(expression),
                "$problem Крутите цикл по isActive: внутри suspend fun и flow { } — " +
                    "`while (currentCoroutineContext().isActive)`, внутри launch/LaunchedEffect — `while (isActive)` " +
                    "(docs/patterns.md, «Корутины и Flow»).",
            ),
        )
    }

    private fun conjuncts(expression: KtExpression): List<KtExpression> = when {
        expression is KtParenthesizedExpression -> expression.expression?.let(::conjuncts).orEmpty()

        expression is KtBinaryExpression && expression.operationToken == KtTokens.ANDAND ->
            listOfNotNull(expression.left, expression.right).flatMap(::conjuncts)

        else -> listOf(expression)
    }

    private fun KaSession.isForeignIsActive(expression: KtExpression): Boolean {
        val reference = when (expression) {
            is KtNameReferenceExpression -> expression
            is KtQualifiedExpression -> expression.selectorExpression as? KtNameReferenceExpression
            else -> null
        } ?: return false
        return reference.getReferencedName() == IS_ACTIVE && !isCoroutineIsActive(reference)
    }

    @OptIn(KaExperimentalApi::class)
    private fun KaSession.isCoroutineIsActive(reference: KtNameReferenceExpression): Boolean {
        val symbol = reference.resolveSymbol() as? KaCallableSymbol ?: return false
        return (sequenceOf(symbol) + symbol.allOverriddenSymbols).any {
            val id = it.callableId ?: return@any false
            val owner = id.classId
            id.callableName.asString() == IS_ACTIVE && (owner == JOB || (owner == null && id.packageName == COROUTINES))
        }
    }

    private companion object {
        const val IS_ACTIVE = "isActive"
        const val CONSTANT = "Цикл с условием-константой в корутине не реагирует на отмену явно."
        const val FOREIGN = "Условие цикла — не корутинный isActive (своё свойство с тем же именем)."
        val COROUTINES = FqName("kotlinx.coroutines")
        val JOB = ClassId(COROUTINES, Name.identifier("Job"))
    }
}
