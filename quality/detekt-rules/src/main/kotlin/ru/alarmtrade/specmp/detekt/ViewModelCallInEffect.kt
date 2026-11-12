package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
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
        val qualified = expression.parent as? KtDotQualifiedExpression ?: return
        if (qualified.selectorExpression != expression || !qualified.receiverExpression.text.isViewModelName()) return
        val lambdas = expression.parents
            .takeWhile { it !is KtNamedFunction }
            .filterIsInstance<KtLambdaExpression>()
            .mapNotNull { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text }
            .toList()
        val effect = lambdas.indexOfFirst { it in EFFECTS }
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
        val EFFECTS = setOf("LaunchedEffect", "DisposableEffect")
        val COLLECTORS = setOf("collect", "collectLatest", "onEach")
    }
}
