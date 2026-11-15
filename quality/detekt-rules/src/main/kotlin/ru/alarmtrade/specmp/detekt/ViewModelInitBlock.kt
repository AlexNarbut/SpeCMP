package ru.alarmtrade.specmp.detekt

import com.intellij.psi.PsiElement
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * `init` выполняется при создании VM, даже если экран так и не подписался. Так же ловятся `init` объект-литерала
 * в VM, тело вторичного конструктора и запуск корутины из инициализатора свойства (companion и вложенные классы —
 * нет). По именам, без type resolution.
 */
class ViewModelInitBlock(config: Config) :
    Rule(
        config,
        "init во ViewModel запрещён: стартовая работа — onStart() из BaseViewModel.",
    ) {

    override fun visitAnonymousInitializer(initializer: KtAnonymousInitializer) {
        super.visitAnonymousInitializer(initializer)
        val owner = when (val holder = (initializer.parent as? KtClassBody)?.parent) {
            is KtClass -> holder
            is KtObjectDeclaration -> holder.takeIf { it.isObjectLiteral() }?.ownerClass()
            else -> null
        }
        if (owner?.isViewModel() == true) report(Finding(Entity.from(initializer), MESSAGE))
    }

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
        super.visitSecondaryConstructor(constructor)
        if (constructor.bodyExpression?.statements.isNullOrEmpty()) return
        if (constructor.ownerClass()?.isViewModel() == true) report(Finding(Entity.from(constructor), MESSAGE))
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (property.isLocal || property.ownerClass()?.isViewModel() != true) return
        val initializer = property.initializer ?: return
        initializer.collectDescendantsOfType<KtCallExpression> { it.calleeExpression?.text in LAUNCHERS }
            .plus(listOfNotNull((initializer as? KtCallExpression)?.takeIf { it.calleeExpression?.text in LAUNCHERS }))
            .distinct()
            .filter { call -> call.runsAtConstruction(initializer) }
            .forEach { report(Finding(Entity.from(it), MESSAGE)) }
    }

    // Лямбда-значение (`val retry = { intent { } }`) не выполняется при создании, лямбды run/let/also/apply/with — да.
    private fun KtCallExpression.runsAtConstruction(root: PsiElement): Boolean = parents
        .takeWhile { it != root.parent }
        .filterIsInstance<KtLambdaExpression>()
        .all { it.getStrictParentOfType<KtCallExpression>()?.calleeExpression?.text in SCOPE_FUNCTIONS }

    private fun PsiElement.ownerClass(): KtClass? = parents.filterIsInstance<KtClass>().firstOrNull()

    private fun KtClass.isViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private companion object {
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        val LAUNCHERS = setOf(
            "intent",
            "launchReplacing",
            "launchOnDevice",
            "onEachDeviceReady",
            "onDeviceLost",
        )
        val SCOPE_FUNCTIONS = setOf("run", "let", "also", "apply", "with")
        const val MESSAGE =
            "init во ViewModel запрещён: стартовая работа — override suspend fun IntentScope.onStart() из " +
                "BaseViewModel, docs/patterns.md"
    }
}
