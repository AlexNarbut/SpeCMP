package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.parents

/** VM ветвится по `Response`, граница с бросающим кодом — data-слой. По именам — во всех source set'ах. */
class SafeRunInViewModel(config: Config) :
    Rule(config, "safeRun/safeResponseRun во ViewModel запрещены: бросающий код оборачивает репозиторий.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        if (callee !in expression.containingKtFile.namesWithAliases(SAFE_RUNS)) return
        val inViewModel = expression.parents.any {
            (it is KtClass && it.isViewModel()) || (it is KtNamedFunction && it.extendsViewModel())
        }
        if (inViewModel) {
            report(Finding(Entity.from(expression), "$callee $MESSAGE"))
        }
    }

    private fun KtNamedFunction.extendsViewModel(): Boolean =
        receiverTypeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()

    private fun KtClass.isViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private companion object {
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        val SAFE_RUNS = setOf("safeRun", "safeResponseRun")
        const val MESSAGE =
            "во ViewModel: бросающий вызов оборачивается в data-слое (репозиторий возвращает Response), " +
                "VM ветвится по Response через ResponseExt (docs/patterns.md, «BaseViewModel»)."
    }
}
