package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.parents

/** Каждый вызов запускает новый горячий поток в scope — подписки множатся. По именам, без type resolution. */
class SharingInsideFunction(config: Config) :
    Rule(config, "shareIn/stateIn — только в инициализаторе свойства: в функции каждый вызов создаёт новый поток.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text
        if (name != SHARE_IN && name != STATE_IN) return
        val qualified = expression.parent as? KtQualifiedExpression ?: return
        if (qualified.selectorExpression != expression) return
        val boundary = expression.parents.firstOrNull {
            when (it) {
                is KtFunctionLiteral -> false
                is KtProperty -> !it.isLocal
                is KtFunction, is KtPropertyAccessor, is KtClassOrObject, is KtFile -> true
                else -> false
            }
        }
        if (boundary !is KtFunction && boundary !is KtPropertyAccessor) return
        report(
            Finding(
                Entity.from(expression),
                "`$name` внутри функции: каждый вызов запускает новый горячий поток. Объявите его один раз в " +
                    "инициализаторе свойства (docs/patterns.md, «Корутины и Flow»).",
            ),
        )
    }

    private companion object {
        const val SHARE_IN = "shareIn"
        const val STATE_IN = "stateIn"
    }
}
