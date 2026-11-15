package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/**
 * `kotlin.Result.getOrThrow()` не трогается: функция сверяется по пакету `Response`, ресивер — по типу. Требует type
 * resolution.
 */
class ResponseGetOrThrow(config: Config) :
    Rule(config, "getOrThrow() на Response в прод-коде запрещён: ветвиться по Response, не бросать."),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (!expression.isGetOrThrowName(expression.calleeExpression?.text) || expression.valueArguments.isNotEmpty()) {
            return
        }
        val forbidden = analyze(expression) {
            resolvedFunction(expression)?.callableId?.isResponseGetOrThrow() == true || hasResponseReceiver(expression)
        }
        if (forbidden) report(expression)
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        val reference = expression.callableReference
        if (!expression.isGetOrThrowName(reference.getReferencedName())) return
        val forbidden = analyze(expression) {
            (reference.mainReference.resolveToSymbol() as? KaCallableSymbol)?.callableId?.isResponseGetOrThrow() == true
        }
        if (forbidden) report(expression)
    }

    private fun KaSession.hasResponseReceiver(call: KtCallExpression): Boolean {
        val qualified = call.parent as? KtQualifiedExpression ?: return false
        if (qualified.selectorExpression != call) return false
        return qualified.receiverExpression.expressionType?.isSubtypeOf(RESPONSE) == true
    }

    private fun KtElement.isGetOrThrowName(name: String?): Boolean =
        name != null && name in containingKtFile.namesWithAliases(setOf(GET_OR_THROW))

    private fun CallableId.isResponseGetOrThrow(): Boolean =
        packageName == RESPONSE.packageFqName && callableName.asString() == GET_OR_THROW

    private fun report(element: KtElement) {
        report(
            Finding(
                Entity.from(element),
                "getOrThrow() на Response превращает ошибку в исключение: ветвитесь по Response " +
                    "(when/onError/map*) или берите getValueIfSuccess() " +
                    "(docs/patterns.md, «Data — репозитории»).",
            ),
        )
    }

    private companion object {
        const val GET_OR_THROW = "getOrThrow"
        val RESPONSE = ClassId(FqName("ru.alarmtrade.specmp.core.common"), Name.identifier("Response"))
    }
}
