package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/** Вызовы и ссылки `println`/`print` (с алиасами импорта) без ресивера или на stdout/stderr; по имени, без type resolution. */
class ForbiddenPrintln(config: Config) :
    Rule(config, "Вывод в stdout вместо логгера: в релизе теряется, в логах не фильтруется.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name !in expression.containingKtFile.namesWithAliases(FORBIDDEN)) return
        val qualified = expression.parent as? KtQualifiedExpression
        if (qualified?.selectorExpression == expression && qualified.receiverExpression.text !in STD_RECEIVERS) return
        report(Finding(Entity.from(expression), "$name запрещён: используйте логгер (Kermit)."))
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        val name = expression.callableReference.getReferencedName()
        if (name !in expression.containingKtFile.namesWithAliases(FORBIDDEN)) return
        val receiver = expression.receiverExpression?.text
        if (receiver != null && receiver !in STD_RECEIVERS) return
        report(Finding(Entity.from(expression), "::$name запрещён: используйте логгер (Kermit)."))
    }

    private companion object {
        val FORBIDDEN = setOf("println", "print")
        val STD_RECEIVERS = setOf(
            "kotlin.io",
            "System.out",
            "System.err",
            "java.lang.System.out",
            "java.lang.System.err",
        )
    }
}
