package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/** Вызовы `println`/`print` без ресивера; по имени, без type resolution — работает во всех source set'ах. */
class ForbiddenPrintln(config: Config) :
    Rule(config, "Вывод в stdout вместо логгера: в релизе теряется, в логах не фильтруется.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name !in FORBIDDEN) return
        val qualified = expression.parent as? KtQualifiedExpression
        if (qualified?.selectorExpression == expression) return
        report(Finding(Entity.from(expression), "$name запрещён: используйте логгер (Kermit)."))
    }

    private companion object {
        val FORBIDDEN = setOf("println", "print")
    }
}
