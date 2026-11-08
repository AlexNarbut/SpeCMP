package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

/**
 * Любой вызов `runCatching` (с ресивером и без) по имени, без type resolution: правило работает во всех
 * source set'ах, включая iosMain, jvmMain и тесты. `runCatching` глотает CancellationException.
 */
class ForbiddenRunCatching(config: Config) :
    Rule(
        config,
        "runCatching перехватывает CancellationException и ломает отмену корутин — используйте safeRun.",
    ) {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text == RUN_CATCHING) {
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        if (expression.callableReference.getReferencedName() == RUN_CATCHING) {
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    private companion object {
        const val RUN_CATCHING = "runCatching"
        const val MESSAGE =
            "runCatching запрещён: используйте safeRun (ru.alarmtrade.specmp.core.common.safeRun, " +
                "core/common/.../ResponseExt.kt) — он пробрасывает CancellationException."
    }
}
