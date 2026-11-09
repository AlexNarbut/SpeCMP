package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `Mutex` нереентерабелен — вложенный захват ждёт сам себя; ловит и вызов метода своего класса на один уровень.
 * Мьютекс сравнивается по тексту ресивера.
 */
class NestedSameMutexLock(config: Config) :
    Rule(config, "Mutex нереентерабелен: withLock того же мьютекса внутри withLock — deadlock.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val mutex = expression.lockedMutex() ?: return
        val body = expression.lambdaArguments.singleOrNull()?.getLambdaExpression()?.bodyExpression ?: return
        body.collectDescendantsOfType<KtCallExpression>().filterNot { it.isInOtherCoroutine(body) }.forEach { inner ->
            when {
                inner.lockedMutex() == mutex -> report(inner, "вложенный `$mutex.withLock`")

                inner.locksViaMemberFunction(mutex) ->
                    report(inner, "`${inner.calleeExpression?.text}()` сам захватывает `$mutex.withLock`")
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

    private fun KtCallExpression.lockedMutex(): String? {
        if (calleeExpression?.text != WITH_LOCK) return null
        val qualified = parent as? KtQualifiedExpression ?: return null
        if (qualified.selectorExpression != this) return null
        return qualified.receiverExpression.text.removePrefix(THIS_PREFIX)
    }

    private fun KtCallExpression.locksViaMemberFunction(mutex: String): Boolean {
        val qualified = parent as? KtQualifiedExpression
        if (qualified != null && qualified.selectorExpression == this &&
            qualified.receiverExpression !is KtThisExpression
        ) {
            return false
        }
        val name = calleeExpression?.text ?: return false
        val owner = getStrictParentOfType<KtClassOrObject>() ?: return false
        return owner.declarations.filterIsInstance<KtNamedFunction>()
            .filter { it.name == name }
            .any { function -> function.locks(mutex) }
    }

    // `launch { m.withLock { } }` внутри секции — другая корутина: она ждёт освобождения, а не держит захват.
    private fun KtCallExpression.isInOtherCoroutine(body: KtElement): Boolean = parents
        .takeWhile { it != body }
        .filterIsInstance<KtLambdaExpression>()
        .any { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text in COROUTINE_BUILDERS }

    private fun KtElement.locks(mutex: String): Boolean =
        collectDescendantsOfType<KtCallExpression>().any { it.lockedMutex() == mutex }

    private companion object {
        const val WITH_LOCK = "withLock"
        const val THIS_PREFIX = "this."
        val COROUTINE_BUILDERS = setOf("launch", "async")
    }
}
