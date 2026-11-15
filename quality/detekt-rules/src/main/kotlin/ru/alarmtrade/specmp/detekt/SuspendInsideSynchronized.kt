package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/** Корутина засыпает с удержанным монитором/локом: блокирует поток, а unlock может прийти с чужого потока. */
class SuspendInsideSynchronized(config: Config) :
    Rule(config, "Suspend-вызов под блокирующей блокировкой потока — используйте Mutex.withLock { }."),
    RequiresAnalysisApi {

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        val names = function.containingKtFile.namesWithAliases(setOf(SYNCHRONIZED_ANNOTATION))
        val synchronized = function.annotationEntries.any { it.shortName?.asString() in names }
        if (synchronized && function.hasModifier(KtTokens.SUSPEND_KEYWORD)) {
            report(Finding(Entity.atName(function), "@Synchronized на suspend-функции: $MESSAGE"))
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name !in expression.containingKtFile.namesWithAliases(LOCK_FUNCTIONS)) return
        val lambda = expression.lambdaArguments.singleOrNull()?.getLambdaExpression()
            ?: expression.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression
            ?: return
        analyze(expression) {
            if (!isThreadLock(expression)) return@analyze
            reportSuspendCalls(lambda, "$name { }")
        }
    }

    override fun visitBlockExpression(expression: KtBlockExpression) {
        super.visitBlockExpression(expression)
        val statements = expression.statements
        statements.forEachIndexed { index, statement ->
            val lock =
                statement.lockReceiver(LOCK) ?: statement.lockReceiver(LOCK_INTERRUPTIBLY) ?: return@forEachIndexed
            analyze(expression) {
                if (!isLockType(statement)) return@analyze
                for (next in statements.drop(index + 1)) {
                    if (next.lockReceiver(UNLOCK) == lock) break
                    reportSuspendCalls(next, "$lock.lock()")
                    if (next.unlocks(lock)) break
                }
            }
        }
    }

    override fun visitIfExpression(expression: KtIfExpression) {
        super.visitIfExpression(expression)
        val condition = expression.condition ?: return
        val then = expression.then ?: return
        val lock = condition.lockReceiver(TRY_LOCK) ?: return
        analyze(expression) {
            if (isLockType(condition)) reportSuspendCalls(then, "$lock.tryLock()")
        }
    }

    private fun KaSession.reportSuspendCalls(root: KtElement, section: String) {
        inlineCalls(root).filter { isSuspendCall(it) }.forEach { call ->
            report(
                Finding(Entity.from(call), "`${call.calleeExpression?.text}` — suspend-вызов под $section: $MESSAGE"),
            )
        }
    }

    private fun KaSession.isThreadLock(call: KtCallExpression): Boolean {
        val callable = resolvedFunction(call)?.callableId ?: return false
        return callable.packageName.asString() in LOCK_PACKAGES && callable.callableName.asString() in LOCK_FUNCTIONS
    }

    private fun KaSession.isLockType(statement: KtExpression): Boolean {
        val receiver = (statement as KtQualifiedExpression).receiverExpression
        return receiver.expressionType?.isSubtypeOf(JAVA_LOCK) == true
    }

    private fun KtExpression.lockReceiver(method: String): String? {
        val qualified = this as? KtQualifiedExpression ?: return null
        val call = qualified.selectorExpression as? KtCallExpression ?: return null
        if (call.calleeExpression?.text != method || call.valueArguments.isNotEmpty()) return null
        return qualified.receiverExpression.text
    }

    // `try { … } finally { lock.unlock() }` — секция заканчивается на этом операторе.
    private fun KtExpression.unlocks(lock: String): Boolean =
        collectDescendantsOfType<KtQualifiedExpression>().any { it.lockReceiver(UNLOCK) == lock }

    private companion object {
        const val SYNCHRONIZED_ANNOTATION = "Synchronized"
        val LOCK_FUNCTIONS = setOf("synchronized", "withLock", "read", "write")
        const val LOCK = "lock"
        const val LOCK_INTERRUPTIBLY = "lockInterruptibly"
        const val TRY_LOCK = "tryLock"
        const val UNLOCK = "unlock"
        val LOCK_PACKAGES = setOf("kotlin", "kotlin.concurrent", "kotlinx.atomicfu.locks")
        val JAVA_LOCK = ClassId(FqName("java.util.concurrent.locks"), Name.identifier("Lock"))
        const val MESSAGE =
            "поток держит блокировку, пока корутина спит, а продолжение может прийти на другом потоке. " +
                "Критическая секция вокруг suspend-работы — Mutex.withLock { } (docs/patterns.md, «Конкурентность»)."
    }
}
