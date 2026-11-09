package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/** Включая вложенные классы и конструктор; по именам, без type resolution. */
class ViewModelMutableProperty(config: Config) :
    Rule(
        config,
        "У ViewModel нет var-свойств и изменяемых коллекций: состояние — в state, перезапуск корутины — launchReplacing.",
    ) {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        if (!klass.extendsViewModel()) return
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

    private fun KtClass.extendsViewModel(): Boolean = superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.')?.endsWith(VIEW_MODEL_SUFFIX) == true
    }

    private companion object {
        const val VAR = "var"
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        const val MESSAGE =
            "служебное значение — поле state («VM bookkeeping»), отменяемая/перезапускаемая корутина — " +
                "launchReplacing(key)/cancelLaunched(key) из BaseViewModel (docs/patterns.md, «BaseViewModel»)"
    }
}
