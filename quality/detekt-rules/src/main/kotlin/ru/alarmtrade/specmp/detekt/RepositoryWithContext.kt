package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `withContext` внутри репозитория или источника данных (класс `*Repository` — в том числе `Specialist*Repository`,
 * `*RepositoryImpl`, `*DataSource`, `*DataSourceImpl`, включая вложенные в него классы и лямбды). Диспетчер выбирает
 * вызывающий — ViewModel или точка входа; метод репозитория — голый `safeRun { }` (docs/patterns.md, «Диспетчеры»).
 *
 * Исключения структурные, без `@Suppress`:
 * - класс владеет собственным `CoroutineScope` (свойство или параметр конструктора этого типа, либо сам реализует
 *   `CoroutineScope`) — он сам хранит состояние и запускает фоновую работу, переключение внутри;
 * - `withContext(NonCancellable)` — не выбор диспетчера, а suspend-уборка в `finally` после отмены.
 *
 * Проверка по именам, без типов — работает во всех source set'ах.
 */
class RepositoryWithContext(config: Config) :
    Rule(config, "Репозиторий не переключает диспетчер сам — это решает вызывающий (ViewModel/точка входа).") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != WITH_CONTEXT) return
        if (expression.valueArguments.firstOrNull()?.getArgumentExpression()?.text == NON_CANCELLABLE) return
        val owner = expression.parents.filterIsInstance<KtClassOrObject>()
            .firstOrNull { declaration -> SUFFIXES.any { declaration.name.orEmpty().endsWith(it) } }
            ?: return
        if (owner.ownsCoroutineScope()) return
        report(
            Finding(
                Entity.from(expression),
                "withContext в ${owner.name}: переключение диспетчера — в месте вызова " +
                    "(withContext(dispatchers.io) { repository.foo() } во ViewModel), метод репозитория — " +
                    "safeRun { … } без withContext (docs/patterns.md, «Диспетчеры»).",
            ),
        )
    }

    private fun KtClassOrObject.ownsCoroutineScope(): Boolean {
        val constructorTypes = (this as? KtClass)?.primaryConstructorParameters.orEmpty().map { it.typeReference }
        val propertyTypes = declarations.filterIsInstance<KtProperty>().map { it.typeReference }
        val superTypes = superTypeListEntries.map { it.typeReference }
        return (constructorTypes + propertyTypes + superTypes).any { it.isCoroutineScope() }
    }

    private fun KtTypeReference?.isCoroutineScope(): Boolean =
        this?.typeElement?.text?.removeSuffix("?")?.substringAfterLast('.') == COROUTINE_SCOPE

    private companion object {
        const val WITH_CONTEXT = "withContext"
        const val NON_CANCELLABLE = "NonCancellable"
        const val COROUTINE_SCOPE = "CoroutineScope"
        val SUFFIXES = listOf("Repository", "RepositoryImpl", "DataSource", "DataSourceImpl")
    }
}
