package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * При CAS-конфликте лямбда выполняется повторно — эффект повторится. Метод своего класса проверяется на один уровень,
 * вложенные лямбды — только `inline`-функций. Требует type resolution.
 */
class UpdateLambdaSideEffects(config: Config) :
    Rule(config, "Лямбда update/getAndUpdate/updateAndGet — чистая функция: она повторяется при CAS-конфликте."),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text
        if (name !in expression.containingKtFile.namesWithAliases(UPDATES)) return
        val lambda = expression.lambdaArguments.singleOrNull()?.getLambdaExpression()
            ?: expression.valueArguments.singleOrNull()?.getArgumentExpression() as? KtLambdaExpression
            ?: return
        analyze(expression) {
            val callable = resolvedFunction(expression)?.callableId ?: return@analyze
            if (callable.packageName.asString() != FLOW_PACKAGE) return@analyze
            inlineCalls(lambda).filter { it != expression }.forEach { call ->
                val problem = problem(call) ?: return@forEach
                report(
                    Finding(
                        Entity.from(call),
                        "`${call.calleeExpression?.text}` в лямбде $name: $problem. Лямбда повторяется при " +
                            "CAS-конфликте — вычислите новое значение чисто, эффект выполните до или после " +
                            "update (docs/patterns.md, «Конкурентность»).",
                    ),
                )
            }
        }
    }

    private fun KaSession.problem(call: KtCallExpression): String? {
        val owner = ownerName(call)
        val enclosing = call.getStrictParentOfType<KtClassOrObject>()
        if (owner == null || owner != enclosing?.name) return directProblem(call)
        if (isSuspendCall(call)) return "suspend-вызов `${call.calleeExpression?.text}`"
        val name = call.calleeExpression?.text
        val inner = enclosing.declarations.filterIsInstance<KtNamedFunction>()
            .filter { it.name == name }
            .flatMap { inlineCalls(it) }
            .firstNotNullOfOrNull { directProblem(it) }
        return inner?.let { "свой метод с эффектом внутри ($it)" }
    }

    private fun KaSession.directProblem(call: KtCallExpression): String? {
        val callee = call.calleeExpression?.text.orEmpty()
        val owner = ownerName(call).orEmpty()
        return when {
            callee in EMITS -> "отправка события `$callee`"
            callee in LAUNCHES -> "запуск корутины `$callee`"
            isSuspendCall(call) -> "suspend-вызов `$callee`"
            isServiceCall(call) -> "вызов $owner.$callee"
            STORAGE.matches(owner) && WRITE.containsMatchIn(callee) -> "запись $owner.$callee"
            else -> null
        }
    }

    private fun KaSession.isServiceCall(call: KtCallExpression): Boolean = SERVICE.matches(ownerName(call).orEmpty())

    // Класс-владелец вызываемого метода, для расширений — тип явного ресивера.
    private fun KaSession.ownerName(call: KtCallExpression): String? {
        val receiver = (call.parent as? KtQualifiedExpression)?.takeIf { it.selectorExpression == call }
            ?.receiverExpression
        val receiverType = receiver?.expressionType as? KaClassType
        val owner = resolvedFunction(call)?.callableId?.classId ?: receiverType?.classId
        return owner?.shortClassName?.asString()
    }

    private companion object {
        const val FLOW_PACKAGE = "kotlinx.coroutines.flow"
        val UPDATES = setOf("update", "getAndUpdate", "updateAndGet")
        val EMITS = setOf("emit", "tryEmit", "send", "trySend", "postSideEffect")
        val LAUNCHES = setOf("launch", "async", "launchIn")
        val SERVICE = Regex(".*(Repository|DataSource|Coordinator|Controller|Scheduler|Service|Api|Tracker)(Impl)?")
        val STORAGE = Regex(".*(Settings|Store|Storage|Preferences|Queries|Dao|Cache)(Impl)?")
        val WRITE = Regex("^(put|set|remove|clear|persist|save|write|delete|insert|upsert)")
    }
}
