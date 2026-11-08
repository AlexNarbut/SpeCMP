package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `withContext` внутри репозитория или источника данных (класс `*Repository`, `*RepositoryImpl`, `*DataSource`,
 * `*DataSourceImpl`, включая вложенные в него классы и лямбды). Диспетчер выбирает вызывающий — ViewModel или
 * точка входа; метод репозитория — голый `safeRun { }` (docs/patterns.md, «Диспетчеры»).
 *
 * Исключение — объект, который сам хранит состояние и обновляет его фоновой работой (кэш + сетевой refresh):
 * точечный `@Suppress("RepositoryWithContext")` с причиной. Проверка по именам — работает во всех source set'ах.
 */
class RepositoryWithContext(config: Config) :
    Rule(config, "Репозиторий не переключает диспетчер сам — это решает вызывающий (ViewModel/точка входа).") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != WITH_CONTEXT) return
        val owner = expression.parents.filterIsInstance<KtClassOrObject>()
            .firstOrNull { declaration -> SUFFIXES.any { declaration.name.orEmpty().endsWith(it) } }
            ?: return
        report(
            Finding(
                Entity.from(expression),
                "withContext в ${owner.name}: переключение диспетчера — в месте вызова " +
                    "(withContext(dispatchers.io) { repository.foo() } во ViewModel), метод репозитория — " +
                    "safeRun { … } без withContext (docs/patterns.md, «Диспетчеры»).",
            ),
        )
    }

    private companion object {
        const val WITH_CONTEXT = "withContext"
        val SUFFIXES = listOf("Repository", "RepositoryImpl", "DataSource", "DataSourceImpl")
    }
}
