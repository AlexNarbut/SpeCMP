package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

/** `runCatching` глотает CancellationException; по имени, без type resolution — во всех source set'ах. */
class ForbiddenRunCatching(config: Config) :
    Rule(
        config,
        "runCatching перехватывает CancellationException и ломает отмену корутин — используйте safeRun.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text in expression.containingKtFile.namesWithAliases(NAMES)) {
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        if (expression.callableReference.getReferencedName() in expression.containingKtFile.namesWithAliases(NAMES)) {
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    private companion object {
        val NAMES = setOf("runCatching")
        const val MESSAGE =
            "runCatching запрещён: используйте safeRun (ru.alarmtrade.specmp.core.common.safeRun, " +
                "core/common/.../ResponseExt.kt) — он пробрасывает CancellationException."
    }
}
