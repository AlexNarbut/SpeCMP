package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Диспетчер выбирает вызывающий. Исключения структурные: класс владеет своим `CoroutineScope` или
 * `withContext(NonCancellable)` (уборка в `finally`). По именам — во всех source set'ах.
 */
class RepositoryWithContext(config: Config) :
    Rule(config, "Репозиторий не переключает диспетчер сам — это решает вызывающий (ViewModel/точка входа).") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        if (callee !in expression.containingKtFile.namesWithAliases(setOf(WITH_CONTEXT))) return
        if (expression.valueArguments.firstOrNull()?.getArgumentExpression()?.text == NON_CANCELLABLE) return
        val owner = expression.parents.filterIsInstance<KtClassOrObject>()
            .firstOrNull { declaration -> declaration.name.isDataLayerName() }
        val ownerName = owner?.name ?: expression.extendedDataLayerType() ?: return
        if (owner?.ownsCoroutineScope() == true) return
        report(
            Finding(
                Entity.from(expression),
                "withContext в $ownerName: переключение диспетчера — в месте вызова " +
                    "(withContext(dispatchers.io) { repository.foo() } во ViewModel), метод репозитория — " +
                    "safeRun { … } без withContext (docs/patterns.md, «Диспетчеры (AppDispatchers)»).",
            ),
        )
    }

    private fun KtCallExpression.extendedDataLayerType(): String? = parents.filterIsInstance<KtNamedFunction>()
        .mapNotNull { it.receiverTypeReference?.text?.substringBefore('<')?.substringAfterLast('.') }
        .firstOrNull { it.isDataLayerName() }

    private fun String?.isDataLayerName(): Boolean = SUFFIXES.any { this.orEmpty().endsWith(it) }

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
