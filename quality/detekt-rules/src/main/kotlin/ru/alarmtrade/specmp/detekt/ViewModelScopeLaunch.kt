package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression

/**
 * Запуск корутины прямо в `viewModelScope` мимо `BaseViewModel.safeLaunch`: `viewModelScope.launch { }`,
 * `viewModelScope.async { }` (в том числе `this.viewModelScope`, `this@Foo.viewModelScope`, `?.`) и
 * `flow.launchIn(viewModelScope)`. Такая корутина идёт без `CoroutineExceptionHandler` базового класса —
 * необработанное исключение роняет приложение вместо записи в лог.
 *
 * `stateIn(viewModelScope, …)` и `shareIn(viewModelScope, …)` разрешены. Проверка по именам, без type
 * resolution — работает во всех source set'ах. Сам `BaseViewModel` исключён путём в
 * detekt.yml.
 */
class ViewModelScopeLaunch(config: Config) :
    Rule(
        config,
        "Корутины ViewModel запускаются через intent/safeLaunch базы ViewModel — они логируют необработанные исключения.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        when (expression.calleeExpression?.text) {
            in BUILDERS -> {
                val qualified = expression.parent as? KtQualifiedExpression ?: return
                if (qualified.selectorExpression == expression && qualified.receiverExpression.isViewModelScope()) {
                    report(Finding(Entity.from(expression), MESSAGE))
                }
            }

            LAUNCH_IN -> {
                val scope = expression.valueArguments.singleOrNull()?.getArgumentExpression() ?: return
                if (scope.isViewModelScope()) report(Finding(Entity.from(expression), MESSAGE))
            }
        }
    }

    private fun KtExpression.isViewModelScope(): Boolean = when (this) {
        is KtNameReferenceExpression -> getReferencedName() == VIEW_MODEL_SCOPE

        is KtParenthesizedExpression -> expression?.isViewModelScope() == true

        is KtDotQualifiedExpression ->
            receiverExpression is KtThisExpression && selectorExpression?.isViewModelScope() == true

        else -> false
    }

    private companion object {
        const val VIEW_MODEL_SCOPE = "viewModelScope"
        const val LAUNCH_IN = "launchIn"
        val BUILDERS = setOf("launch", "async")
        const val MESSAGE =
            "Корутина в viewModelScope мимо обработчика ошибок: используйте intent { } / safeLaunch { } / " +
                "safeLaunch(context) { } из BaseViewModel (core/ui/.../viewmodel/BaseViewModel.kt, " +
                "docs/patterns.md, «BaseViewModel»); flow.launchIn(viewModelScope) → подписка в onStart " +
                "(repeatOnSubscription { flow.collect() }) или safeLaunch { flow.collect() }."
    }
}
