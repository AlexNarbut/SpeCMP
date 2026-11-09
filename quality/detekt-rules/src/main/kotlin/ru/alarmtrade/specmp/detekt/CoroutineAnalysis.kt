package ru.alarmtrade.specmp.detekt

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.types.KaFunctionType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/** Общие проверки корутинного контекста для правил с type resolution (вызываются внутри `analyze { }`). */
internal fun KaSession.isSuspendCall(call: KtCallExpression): Boolean = resolvedFunction(call)?.isSuspend == true

internal fun KaSession.resolvedFunction(call: KtCallExpression): KaNamedFunctionSymbol? =
    call.resolveToCall()?.singleFunctionCallOrNull()?.symbol as? KaNamedFunctionSymbol

/** Лямбда выполняется на месте: аргумент `inline`-функции (`forEach`, `update`, `withLock`, `map`). */
internal fun KaSession.isInlineLambda(lambda: KtLambdaExpression): Boolean {
    val call = lambda.getStrictParentOfType<KtCallExpression>() ?: return false
    if (call.lambdaArguments.none { it.getLambdaExpression() == lambda } &&
        call.valueArguments.none { it.getArgumentExpression() == lambda }
    ) {
        return false
    }
    return resolvedFunction(call)?.isInline == true
}

internal fun KaSession.isSuspendLambda(lambda: KtLambdaExpression): Boolean =
    (lambda.expressionType as? KaFunctionType)?.isSuspend == true

/**
 * Вызовы, которые выполняются синхронно в теле [root]: обход не заходит во вложенные функции, классы и лямбды,
 * кроме лямбд `inline`-функций.
 */
internal fun KaSession.inlineCalls(root: KtElement): List<KtCallExpression> {
    val calls = mutableListOf<KtCallExpression>()
    root.accept(
        object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                calls += expression
                super.visitCallExpression(expression)
            }

            override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
                if (lambdaExpression == root || isInlineLambda(lambdaExpression)) {
                    super.visitLambdaExpression(lambdaExpression)
                }
            }

            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function == root) super.visitNamedFunction(function)
            }

            override fun visitClassOrObject(classOrObject: KtClassOrObject) = Unit
        },
    )
    return calls
}

/**
 * [element] выполняется в корутине: ближайшая не-`inline` граница — `suspend fun` или suspend-лямбда
 * (`launch { }`, `withContext { }`, `flow { }`).
 */
internal fun KaSession.isInCoroutine(element: KtElement): Boolean {
    var current = element.parent
    while (current != null) {
        when (current) {
            is KtLambdaExpression -> {
                if (isSuspendLambda(current)) return true
                if (!isInlineLambda(current)) return false
            }

            is KtNamedFunction -> return current.hasModifier(KtTokens.SUSPEND_KEYWORD)

            is KtClassOrObject -> return false
        }
        current = current.parent
    }
    return false
}
