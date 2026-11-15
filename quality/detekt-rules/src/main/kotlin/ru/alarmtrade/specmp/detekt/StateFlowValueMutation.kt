package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtUnaryExpression

/**
 * Ресивер — по типу из Analysis API, Compose `MutableState.value` не попадает. Без классов (jvmMain, iosMain, тесты)
 * правило молчит; commonMain и androidMain проверяются Android-задачей с типами.
 */
class StateFlowValueMutation(config: Config) :
    Rule(config, "Запись в MutableStateFlow.value не атомарна — используйте update { }."),
    RequiresAnalysisApi {

    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)
        if (expression.operationToken !in ASSIGNMENTS) return
        check(expression, expression.left, expression.operationReference.text)
    }

    override fun visitUnaryExpression(expression: KtUnaryExpression) {
        super.visitUnaryExpression(expression)
        if (expression.operationToken !in INCREMENTS) return
        check(expression, expression.baseExpression, expression.operationReference.text)
    }

    private fun check(expression: KtExpression, target: KtExpression?, operation: String) {
        val unwrapped = KtPsiUtil.safeDeparenthesize(target ?: return)
        if (unwrapped is KtNameReferenceExpression) return checkImplicit(expression, unwrapped, operation)
        val qualified = unwrapped as? KtQualifiedExpression ?: return
        val selector = qualified.selectorExpression as? KtNameReferenceExpression ?: return
        if (selector.getReferencedName() != VALUE) return
        val receiver = qualified.receiverExpression
        val isStateFlow = analyze(receiver) {
            receiver.expressionType?.isSubtypeOf(MUTABLE_STATE_FLOW) == true
        }
        if (isStateFlow) {
            report(
                Finding(
                    Entity.from(expression),
                    "`${qualified.text} $operation` пишет в MutableStateFlow.value в обход атомарного " +
                        "update { } (docs/patterns.md).",
                ),
            )
        }
    }

    // `with(state) { value = 1 }`, `fun MutableStateFlow<Int>.bump() { value++ }` — ресивер неявный.
    private fun checkImplicit(expression: KtExpression, target: KtNameReferenceExpression, operation: String) {
        if (target.getReferencedName() != VALUE) return
        val isStateFlow = analyze(target) {
            val symbol = target.mainReference.resolveToSymbol() as? KaCallableSymbol ?: return@analyze false
            (sequenceOf(symbol) + symbol.allOverriddenSymbols).any { it.callableId?.classId == MUTABLE_STATE_FLOW }
        }
        if (isStateFlow) {
            report(
                Finding(
                    Entity.from(expression),
                    "`${target.text} $operation` пишет в MutableStateFlow.value в обход атомарного " +
                        "update { } (docs/patterns.md).",
                ),
            )
        }
    }

    private companion object {
        const val VALUE = "value"
        val MUTABLE_STATE_FLOW = ClassId(FqName("kotlinx.coroutines.flow"), Name.identifier("MutableStateFlow"))
        val ASSIGNMENTS = setOf(
            KtTokens.EQ,
            KtTokens.PLUSEQ,
            KtTokens.MINUSEQ,
            KtTokens.MULTEQ,
            KtTokens.DIVEQ,
            KtTokens.PERCEQ,
        )
        val INCREMENTS = setOf(KtTokens.PLUSPLUS, KtTokens.MINUSMINUS)
    }
}
