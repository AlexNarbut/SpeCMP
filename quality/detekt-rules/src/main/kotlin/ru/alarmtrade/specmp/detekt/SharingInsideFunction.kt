package ru.alarmtrade.specmp.detekt

import com.intellij.psi.PsiElement
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.psiUtil.parents

/** Каждый вызов запускает новый горячий поток в scope — подписки множатся. По именам, без type resolution. */
class SharingInsideFunction(config: Config) :
    Rule(config, "shareIn/stateIn — только в инициализаторе свойства: в функции каждый вызов создаёт новый поток.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name !in expression.containingKtFile.namesWithAliases(SHARING)) return
        val boundary = expression.parents.firstOrNull {
            when (it) {
                is KtFunctionLiteral -> it.parent.isPropertyInitializer()
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

    // `val make = { flow.stateIn(...) }` — значение-функция: поток создаётся на каждый вызов, как в fun.
    private fun PsiElement.isPropertyInitializer(): Boolean =
        this is KtLambdaExpression && (parent as? KtProperty)?.initializer == this

    private companion object {
        val SHARING = setOf("shareIn", "stateIn")
    }
}
