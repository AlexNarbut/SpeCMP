package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Эффект перезапускается при рекомпозиции с новым ключом и при возврате на экран — работа на старте живёт в
 * `onStart()` VM. Мост платформенных результатов (`picker.results.collect { vm.onPicked(it) }`) разрешён. По именам.
 */
class ViewModelCallInEffect(config: Config) :
    Rule(config, "LaunchedEffect/DisposableEffect не вызывает ViewModel напрямую: старт работы — onStart() VM.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val qualified = expression.parent as? KtQualifiedExpression
        val viewModelCall = if (qualified != null && qualified.selectorExpression == expression) {
            qualified.receiverExpression.text.isViewModelName()
        } else {
            expression.calleeExpression?.text == WITH &&
                expression.valueArguments.firstOrNull()?.getArgumentExpression()?.text?.isViewModelName() == true
        }
        if (viewModelCall) check(expression)
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        if (expression.receiverExpression?.text?.isViewModelName() != true) return
        val bridge = expression.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text in COLLECTORS
        if (!bridge) check(expression)
    }

    private fun check(expression: KtExpression) {
        val effects = expression.containingKtFile.namesWithAliases(EFFECTS)
        val lambdas = expression.parents
            .takeWhile { it !is KtNamedFunction }
            .filterIsInstance<KtLambdaExpression>()
            .mapNotNull { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text }
            .toList()
        val effect = lambdas.indexOfFirst { it in effects }
        if (effect < 0 || lambdas.take(effect).any { it in COLLECTORS }) return
        report(
            Finding(
                Entity.from(expression),
                "Вызов ViewModel из ${lambdas[effect]}: работа на старте экрана — onStart() VM, реакция на " +
                    "действие — интент из обработчика UI; из эффекта — только мост результатов " +
                    "(results.collect { viewModel.onX(it) }). docs/patterns.md, «Запуск работы на старте экрана».",
            ),
        )
    }

    private fun String.isViewModelName(): Boolean = this == VM || substringAfterLast('.').endsWith(VIEW_MODEL, true)

    private companion object {
        const val VM = "vm"
        const val VIEW_MODEL = "ViewModel"
        const val WITH = "with"
        val EFFECTS = setOf("LaunchedEffect", "DisposableEffect")
        val COLLECTORS = setOf("collect", "collectLatest", "onEach")
    }
}
