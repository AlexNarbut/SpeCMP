package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/** Функция VM с корутиной меняет state и шлёт эффекты только из неё, иначе часть перехода идёт синхронно до запуска. */
class ReduceOutsideIntent(config: Config) :
    Rule(
        config,
        "Если не-suspend функция ViewModel запускает intent/safeLaunch, reduce и postSideEffect в ней — только внутри них.",
    ) {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (function.hasModifier(KtTokens.SUSPEND_KEYWORD)) return
        val body = function.bodyExpression ?: return
        if (function.getStrictParentOfType<KtClassOrObject>().let { it !is KtClass || !it.isViewModel() }) return
        val calls = callsOf(body)
        if (calls.none { it.calleeExpression?.text in TRIGGERS }) return
        calls
            .filter { it.calleeExpression?.text in STATE_CALLS }
            .filterNot { it.isInsideLauncher(function) }
            .forEach { call -> report(Finding(Entity.from(call), "${call.calleeExpression?.text} $MESSAGE")) }
    }

    private fun callsOf(body: KtElement): List<KtCallExpression> {
        val calls = mutableListOf<KtCallExpression>()
        body.accept(
            object : KtTreeVisitorVoid() {
                override fun visitCallExpression(expression: KtCallExpression) {
                    calls += expression
                    super.visitCallExpression(expression)
                }

                override fun visitNamedFunction(function: KtNamedFunction) = Unit

                override fun visitClassOrObject(classOrObject: KtClassOrObject) = Unit
            },
        )
        return calls
    }

    private fun KtCallExpression.isInsideLauncher(function: KtNamedFunction): Boolean = parents
        .takeWhile { it != function }
        .filterIsInstance<KtLambdaExpression>()
        .any { lambda ->
            val call = lambda.getStrictParentOfType<KtCallExpression>() ?: return@any false
            call.calleeExpression?.text in LAUNCHERS &&
                (
                    call.lambdaArguments.any { it.getLambdaExpression() == lambda } ||
                        call.valueArguments.any { it.getArgumentExpression() == lambda }
                    )
        }

    private fun KtClass.isViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private companion object {
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        val TRIGGERS = setOf("intent", "safeLaunch")
        val LAUNCHERS = TRIGGERS + setOf("launchReplacing", "launchOnDevice")
        val STATE_CALLS = setOf("reduce", "postSideEffect")
        const val MESSAGE =
            "вне intent { } / safeLaunch { } в функции, которая их запускает: перенесите вызов внутрь корутины, " +
                "чтобы переход state и эффекты шли одной последовательностью (docs/patterns.md, «BaseViewModel»)."
    }
}
