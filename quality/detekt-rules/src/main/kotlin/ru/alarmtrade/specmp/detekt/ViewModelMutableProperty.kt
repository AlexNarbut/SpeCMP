package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * Изменяемое свойство в наследнике `BaseViewModel` (супертип `…ViewModel`), включая вложенные классы и конструктор:
 * `var` (в т.ч. `lateinit`, `by`) и `val` с изменяемой коллекцией. Проверка по именам, без type resolution.
 */
class ViewModelMutableProperty(config: Config) :
    Rule(
        config,
        "У ViewModel нет var-свойств и изменяемых коллекций: состояние — в state, перезапуск корутины — launchReplacing.",
    ) {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (!klass.extendsViewModel() || klass.isLegacyViewModel()) return
        klass.collectDescendantsOfType<KtProperty> { !it.isLocal }.forEach { property ->
            when {
                property.isVar -> reportVar(property)

                property.typeReference.isMutableCollection() || property.initializer.isMutableCollectionCall() ->
                    reportCollection(property)
            }
        }
        klass.collectDescendantsOfType<KtParameter> { it.valOrVarKeyword != null }.forEach { parameter ->
            when {
                parameter.valOrVarKeyword?.text == VAR -> reportVar(parameter)
                parameter.typeReference.isMutableCollection() -> reportCollection(parameter)
            }
        }
    }

    private fun reportVar(declaration: KtNamedDeclaration) {
        report(Finding(Entity.from(declaration), "var ${declaration.name} во ViewModel: $MESSAGE"))
    }

    private fun reportCollection(declaration: KtNamedDeclaration) {
        report(
            Finding(
                Entity.from(declaration),
                "изменяемая коллекция ${declaration.name} во ViewModel: неизменяемая коллекция в state через reduce; " +
                    MESSAGE,
            ),
        )
    }

    private fun KtTypeReference?.isMutableCollection(): Boolean =
        this?.text?.substringBefore('<')?.removeSuffix("?")?.substringAfterLast('.') in MUTABLE_TYPES

    private fun KtExpression?.isMutableCollectionCall(): Boolean {
        val call = when (this) {
            is KtCallExpression -> this
            is KtQualifiedExpression -> selectorExpression as? KtCallExpression
            else -> null
        }
        return call?.calleeExpression?.text in MUTABLE_FACTORIES
    }

    private fun KtClass.extendsViewModel(): Boolean = superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.')?.endsWith(VIEW_MODEL_SUFFIX) == true
    }

    private companion object {
        const val VAR = "var"
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        val MUTABLE_TYPES = setOf(
            "MutableList", "MutableSet", "MutableMap", "MutableCollection",
            "ArrayList", "HashMap", "HashSet", "LinkedHashMap", "LinkedHashSet",
        )
        val MUTABLE_FACTORIES = setOf(
            "mutableListOf", "mutableSetOf", "mutableMapOf", "arrayListOf", "hashMapOf", "hashSetOf",
            "linkedMapOf", "linkedSetOf", "ArrayList", "HashMap", "HashSet", "LinkedHashMap", "LinkedHashSet",
        )
        const val MESSAGE =
            "служебное значение — поле state («VM bookkeeping»), отменяемая/перезапускаемая корутина — " +
                "launchReplacing(key)/cancelLaunched(key) из BaseViewModel (docs/patterns.md, «BaseViewModel»)"
    }
}
