package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/** Дублирует компилятор (reduce/postSideEffect — члены IntentScope) на случай обхода через свои обёртки во VM. */
class ReduceOutsideIntent(config: Config) :
    Rule(
        config,
        "В ViewModel reduce и postSideEffect — только в лямбде intent/subIntent/launchReplacing/" +
            "launchOnDevice или в IntentScope.onStart(); хелперы — через subIntent, не с получателем IntentScope.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        check(expression, expression.calleeExpression?.text)
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        check(expression, expression.callableReference.getReferencedName())
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val name = function.name ?: return
        if (function.receiverTypeReference?.text?.isIntentScope() == true && !function.isOnStart() &&
            function.parents.any { it is KtClass && it.isViewModel() && !it.hasModifier(KtTokens.ABSTRACT_KEYWORD) }
        ) {
            report(Finding(Entity.atName(function), HELPER_MESSAGE))
        }
        if (name !in LAUNCHERS && name !in STATE_CALLS) return
        val owner = function.getStrictParentOfType<KtClass>() ?: return
        if (owner.hasModifier(KtTokens.ABSTRACT_KEYWORD) || !owner.isViewModel()) return
        report(
            Finding(
                Entity.atName(function),
                "Своя `$name` во ViewModel подменяет запуск интента базы: удалите её и используйте intent { } " +
                    "(docs/patterns.md, «BaseViewModel»).",
            ),
        )
    }

    private fun check(expression: KtExpression, name: String?) {
        if (name == null || name !in expression.containingKtFile.namesWithAliases(STATE_CALLS)) return
        val owner = expression.parents.filterIsInstance<KtClass>().firstOrNull { it.isViewModel() } ?: return
        if (expression.isInsideIntent(owner)) return
        report(Finding(Entity.from(expression), "$name $MESSAGE"))
    }

    private fun KtExpression.isInsideIntent(owner: KtClass): Boolean {
        val launchers = owner.intentScopeFunctions() + containingKtFile.namesWithAliases(LAUNCHERS)
        return parents.takeWhile { it != owner }.any { parent ->
            when (parent) {
                is KtNamedFunction -> parent.isOnStart()
                is KtLambdaExpression -> parent.isArgumentOf(launchers)
                else -> false
            }
        }
    }

    private fun KtNamedFunction.isOnStart(): Boolean = name == ON_START &&
        hasModifier(KtTokens.OVERRIDE_KEYWORD) &&
        receiverTypeReference?.text?.isIntentScope() == true

    private fun KtClass.intentScopeFunctions(): Set<String> = declarations.filterIsInstance<KtNamedFunction>()
        .filter { function ->
            function.valueParameters.any { it.typeReference?.text?.hasIntentScopeReceiver() == true }
        }
        .mapNotNull { it.name }
        .toSet()

    private fun KtLambdaExpression.isArgumentOf(launchers: Set<String>): Boolean {
        val call = getStrictParentOfType<KtCallExpression>() ?: return false
        return call.calleeExpression?.text in launchers &&
            (
                call.lambdaArguments.any { it.getLambdaExpression() == this } ||
                    call.valueArguments.any { it.getArgumentExpression() == this }
                )
    }

    private fun String.isIntentScope(): Boolean = replace(GENERICS, "").substringAfterLast('.') == INTENT_SCOPE

    private fun String.hasIntentScopeReceiver(): Boolean =
        removePrefix("suspend").trim().substringBefore('(').trimEnd('.').let { it.isNotEmpty() && it.isIntentScope() }

    private fun KtClass.isViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private companion object {
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        const val INTENT_SCOPE = "IntentScope"
        val GENERICS = Regex("<[^<>]*>")
        const val ON_START = "onStart"
        val LAUNCHERS = setOf(
            "intent",
            "subIntent",
            "launchReplacing",
            "launchOnDevice",
            "onDeviceLost",
            "onEachDeviceReady",
        )
        val STATE_CALLS = setOf("reduce", "postSideEffect")
        const val MESSAGE =
            "вне intent { }: перенесите вызов в intent { }, а в хелпере — в subIntent { } " +
                "(docs/patterns.md, «BaseViewModel»)."
        const val HELPER_MESSAGE =
            "Хелпер с получателем IntentScope — используйте `private suspend fun x() = subIntent { … }` " +
                "(docs/patterns.md, «BaseViewModel»)."
    }
}
