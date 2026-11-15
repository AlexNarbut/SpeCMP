package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Корутина без `CoroutineExceptionHandler` базы роняет приложение; `stateIn`/`shareIn(viewModelScope)` разрешены.
 * Ловит и запуск в `with(viewModelScope)`/`viewModelScope.run { }`, `viewModelScope + context`,
 * `CoroutineScope(viewModelScope.coroutineContext)` и алиас импорта. По именам, без type resolution.
 */
class ViewModelScopeLaunch(config: Config) :
    Rule(
        config,
        "Корутины ViewModel запускаются через intent базы ViewModel — они логируют необработанные исключения.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val names = expression.containingKtFile.namesWithAliases(setOf(VIEW_MODEL_SCOPE))
        val violation = when (expression.calleeExpression?.text) {
            in BUILDERS -> {
                val receiver = (expression.parent as? KtQualifiedExpression)
                    ?.takeIf { it.selectorExpression == expression }
                    ?.receiverExpression
                if (receiver == null || receiver.text in IMPLICIT_RECEIVERS) {
                    expression.isInViewModelScopeLambda(names)
                } else {
                    receiver.isViewModelScope(names)
                }
            }

            LAUNCH_IN -> {
                val scope = expression.valueArguments.singleOrNull()?.getArgumentExpression()
                when {
                    scope == null -> false
                    scope.isViewModelScope(names) -> true
                    scope.text in IMPLICIT_RECEIVERS -> expression.isInViewModelScopeLambda(names)
                    else -> false
                }
            }

            else -> false
        }
        if (violation) report(Finding(Entity.from(expression), MESSAGE))
    }

    private fun KtCallExpression.isInViewModelScopeLambda(names: Set<String>): Boolean = parents
        .takeWhile { it !is KtNamedFunction }
        .filterIsInstance<KtLambdaExpression>()
        .mapNotNull { it.getStrictParentOfType<KtCallExpression>() }
        .takeWhile { it.calleeExpression?.text !in SCOPE_CHANGERS }
        .any { owner ->
            val callee = owner.calleeExpression?.text
            val qualified = (owner.parent as? KtQualifiedExpression)?.takeIf { it.selectorExpression == owner }
            when {
                callee == WITH ->
                    owner.valueArguments.firstOrNull()?.getArgumentExpression()?.isViewModelScope(names) == true

                callee in SCOPE_FUNCTIONS -> qualified?.receiverExpression?.isViewModelScope(names) == true

                else -> false
            }
        }

    private fun KtExpression.isViewModelScope(names: Set<String>): Boolean = when (this) {
        is KtNameReferenceExpression -> getReferencedName() in names

        is KtParenthesizedExpression -> expression?.isViewModelScope(names) == true

        is KtBinaryExpression -> operationToken == KtTokens.PLUS && left?.isViewModelScope(names) == true

        is KtDotQualifiedExpression -> when {
            receiverExpression is KtThisExpression -> selectorExpression?.isViewModelScope(names) == true

            receiverExpression.isViewModelScope(names) ->
                (selectorExpression as? KtCallExpression)?.calleeExpression?.text == PLUS

            else -> false
        }

        is KtCallExpression -> calleeExpression?.text == COROUTINE_SCOPE && valueArguments.any { argument ->
            argument.getArgumentExpression()?.collectDescendantsOfType<KtNameReferenceExpression>()
                ?.any { it.getReferencedName() in names } == true
        }

        else -> false
    }

    private companion object {
        const val VIEW_MODEL_SCOPE = "viewModelScope"
        const val LAUNCH_IN = "launchIn"
        const val WITH = "with"
        const val PLUS = "plus"
        const val COROUTINE_SCOPE = "CoroutineScope"
        val BUILDERS = setOf("launch", "async")
        val IMPLICIT_RECEIVERS = setOf("this", "it")
        val SCOPE_FUNCTIONS = setOf("run", "apply", "let", "also")
        val SCOPE_CHANGERS = BUILDERS + setOf("coroutineScope", "supervisorScope", "withContext")
        const val MESSAGE =
            "Корутина в viewModelScope мимо обработчика ошибок: используйте intent { } / " +
                "intent(context) { } из BaseViewModel (core/ui/.../viewmodel/BaseViewModel.kt, " +
                "docs/patterns.md, «BaseViewModel»); flow.launchIn(viewModelScope) → подписка в onStart " +
                "(repeatOnSubscription { flow.collect() }) или intent { flow.collect() }."
    }
}
