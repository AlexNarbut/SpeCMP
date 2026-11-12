package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/** Литерал тега отстаёт от переименования класса; у верхнеуровневого логгера файла класса нет — там литерал допустим. */
class LoggerTagLiteral(config: Config) :
    Rule(config, "Тег логгера класса — this::class.simpleName, не строковый литерал.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != WITH_TAG) return
        val tag = expression.valueArguments.singleOrNull()?.getArgumentExpression() as? KtStringTemplateExpression
        if (tag == null || expression.getStrictParentOfType<KtClassOrObject>() == null) return
        report(
            Finding(
                Entity.from(tag),
                "Тег логгера литералом: Logger.withTag(this::class.simpleName.orEmpty()) (docs/patterns.md, " +
                    "«Логирование»); во ViewModel — готовый logger из BaseViewModel.",
            ),
        )
    }

    private companion object {
        const val WITH_TAG = "withTag"
    }
}
