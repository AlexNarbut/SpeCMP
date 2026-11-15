package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `Mutex` нереентерабелен — вложенный захват (`withLock`/`lock`) ждёт сам себя; ловит и цепочку вызовов методов своего
 * класса. Мьютекс сравнивается по тексту ресивера (без `this.`/`this@X.`, локальный алиас `val m = mutex` раскрывается).
 */
class NestedSameMutexLock(config: Config) :
    Rule(config, "Mutex нереентерабелен: withLock того же мьютекса внутри withLock — deadlock.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val mutex = expression.lockedMutex(OUTER_LOCKS) ?: return
        val body = expression.lambdaArguments.singleOrNull()?.getLambdaExpression()?.bodyExpression ?: return
        body.collectDescendantsOfType<KtCallExpression>().filterNot { it.isInOtherCoroutine(body) }.forEach { inner ->
            when {
                inner.lockedMutex(INNER_LOCKS) == mutex ->
                    report(inner, "вложенный `$mutex.${inner.calleeExpression?.text}`")

                inner.locksViaMemberFunction(mutex, mutableSetOf()) ->
                    report(inner, "`${inner.calleeExpression?.text}()` сам захватывает `$mutex`")
            }
        }
    }

    private fun report(call: KtCallExpression, what: String) {
        report(
            Finding(
                Entity.from(call),
                "$what внутри `withLock` того же мьютекса: Mutex нереентерабелен — deadlock. Вынесите тело " +
                    "в функцию без захвата (`…Locked`) и зовите её из одной критической секции " +
                    "(docs/patterns.md, «Конкурентность»).",
            ),
        )
    }

    private fun KtCallExpression.lockedMutex(names: Set<String>): String? {
        if (calleeExpression?.text !in names) return null
        val qualified = parent as? KtQualifiedExpression ?: return null
        if (qualified.selectorExpression != this) return null
        return qualified.receiverExpression.mutexKey()
    }

    private fun KtExpression.mutexKey(): String {
        val key = text.replace(THIS_PREFIX, "")
        if (this !is KtNameReferenceExpression) return key
        val function = getStrictParentOfType<KtNamedFunction>() ?: return key
        val alias = function.collectDescendantsOfType<KtProperty>()
            .lastOrNull { it.isLocal && !it.isVar && it.name == key && it.textOffset < textOffset }
            ?.initializer
        return if (alias is KtNameReferenceExpression || alias is KtQualifiedExpression) alias.mutexKey() else key
    }

    private fun KtCallExpression.locksViaMemberFunction(mutex: String, visited: MutableSet<String>): Boolean {
        val qualified = parent as? KtQualifiedExpression
        if (qualified != null && qualified.selectorExpression == this &&
            qualified.receiverExpression !is KtThisExpression
        ) {
            return false
        }
        val name = calleeExpression?.text ?: return false
        if (!visited.add(name)) return false
        val owner = getStrictParentOfType<KtClassOrObject>() ?: return false
        return owner.declarations.filterIsInstance<KtNamedFunction>()
            .filter { it.name == name }
            .any { function ->
                function.collectDescendantsOfType<KtCallExpression>().any { call ->
                    call.lockedMutex(INNER_LOCKS) == mutex || call.locksViaMemberFunction(mutex, visited)
                }
            }
    }

    // `launch { m.withLock { } }` внутри секции — другая корутина: она ждёт освобождения, а не держит захват.
    private fun KtCallExpression.isInOtherCoroutine(body: KtElement): Boolean = parents
        .takeWhile { it != body }
        .filterIsInstance<KtLambdaExpression>()
        .any { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text in COROUTINE_BUILDERS }

    private companion object {
        val OUTER_LOCKS = setOf("withLock")
        val INNER_LOCKS = setOf("withLock", "lock")
        val THIS_PREFIX = Regex("""^this(@\w+)?\.""")
        val COROUTINE_BUILDERS = setOf("launch", "async")
    }
}
