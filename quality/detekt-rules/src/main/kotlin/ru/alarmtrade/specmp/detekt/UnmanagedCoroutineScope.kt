package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `CoroutineScope(…)` без управляемого времени жизни (docs/patterns.md, «Конкурентность»):
 * - создаётся только в DI (пакет `..di..` — scope живёт столько же, сколько синглтон Koin) или в классе, который сам
 *   его отменяет: в классе есть `<scope>.cancel()` для свойства/переменной, куда scope положен;
 * - контекст содержит `SupervisorJob()` (сбой одной корутины не убивает соседей) и обработчик исключений
 *   (`CoroutineExceptionHandler` или значение с `Handler`/`handler` в имени) — иначе необработанное исключение
 *   роняет процесс.
 * Проверка по именам, без type resolution.
 */
class UnmanagedCoroutineScope(config: Config) :
    Rule(config, "CoroutineScope — в DI или с явным cancel(), контекст с SupervisorJob() и обработчиком исключений.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != COROUTINE_SCOPE || expression.valueArguments.isEmpty()) return
        val context = expression.valueArguments.joinToString { it.text }
        val problems = buildList {
            if (!expression.isManaged()) add("создан вне DI и не отменяется (`scope.cancel()`)")
            if (SUPERVISOR_JOB !in context) add("в контексте нет SupervisorJob()")
            if (!HANDLER.containsMatchIn(context)) add("в контексте нет обработчика исключений")
        }
        if (problems.isEmpty()) return
        report(
            Finding(
                Entity.from(expression),
                "`${expression.text}`: ${problems.joinToString("; ")}. Scope сервиса создаёт DI " +
                    "(`serviceScope(…)` из core.common.coroutines) и передаёт в конструктор; короткоживущий — " +
                    "SupervisorJob() + обработчик и `cancel()` по завершении (docs/patterns.md, «Конкурентность»).",
            ),
        )
    }

    private fun KtCallExpression.isManaged(): Boolean {
        val segments = containingKtFile.packageFqName.pathSegments().map { it.asString() }
        if (DI in segments) return true
        val holder = holderName() ?: return false
        val owner = getStrictParentOfType<KtClassOrObject>() ?: getStrictParentOfType<KtDeclaration>() ?: return false
        return owner.collectDescendantsOfType<KtQualifiedExpression>().any { qualified ->
            val call = qualified.selectorExpression as? KtCallExpression
            call?.calleeExpression?.text == CANCEL && qualified.receiverExpression.text.removePrefix(THIS) == holder
        }
    }

    // Имя свойства/переменной/параметра по умолчанию, куда положен scope (`val scope = CoroutineScope(…)`).
    private fun KtCallExpression.holderName(): String? = parents
        .takeWhile { it !is KtClassOrObject }
        .firstOrNull { it is KtProperty || it is KtParameter }
        ?.let { (it as KtNamedDeclaration).name }

    private companion object {
        const val COROUTINE_SCOPE = "CoroutineScope"
        const val SUPERVISOR_JOB = "SupervisorJob("
        const val CANCEL = "cancel"
        const val THIS = "this."
        const val DI = "di"
        val HANDLER = Regex("CoroutineExceptionHandler|[hH]andler\\b")
    }
}
