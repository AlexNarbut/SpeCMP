package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/**
 * `response.getOrThrow()` в прод-коде (docs/patterns.md, «Контракт результата»): ошибка `Response` снова становится
 * исключением мимо контракта. Ветвиться по `Response` — `when`/`onError`/`map*`, значение с запасным —
 * `getValueIfSuccess()`. Тесты исключены в detekt.yml.
 *
 * Ресивер сверяется по типу (`Response` и подтипы), `kotlin.Result.getOrThrow()` не трогается. Требует type
 * resolution: в задачах detekt без классов правило молчит (как RedundantSafeRun).
 */
class ResponseGetOrThrow(config: Config) :
    Rule(config, "getOrThrow() на Response в прод-коде запрещён: ветвиться по Response, не бросать."),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != GET_OR_THROW || expression.valueArguments.isNotEmpty()) return
        val qualified = expression.parent as? KtQualifiedExpression ?: return
        if (qualified.selectorExpression != expression) return
        val receiver = qualified.receiverExpression
        val isResponse = analyze(receiver) { receiver.expressionType?.isSubtypeOf(RESPONSE) == true }
        if (!isResponse) return
        report(
            Finding(
                Entity.from(expression),
                "getOrThrow() на Response превращает ошибку в исключение: ветвитесь по Response " +
                    "(when/onError/map*) или берите getValueIfSuccess() " +
                    "(docs/patterns.md, «Контракт результата»).",
            ),
        )
    }

    private companion object {
        const val GET_OR_THROW = "getOrThrow"
        val RESPONSE = ClassId(FqName("ru.alarmtrade.specmp.core.common"), Name.identifier("Response"))
    }
}
