package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/** Включая вложенные классы, конструктор, массивы и snapshot-коллекции Compose; по именам, без type resolution. */
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

                property.typeReference.isMutable() || property.initializer.isMutableFactory() ->
                    reportCollection(property)
            }
        }
        klass.collectDescendantsOfType<KtParameter> { it.valOrVarKeyword != null }.forEach { parameter ->
            when {
                parameter.valOrVarKeyword?.text == VAR -> reportVar(parameter)
                parameter.typeReference.isMutable() -> reportCollection(parameter)
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

    private fun KtClass.extendsViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private fun KtTypeReference?.isMutable(): Boolean = isMutableCollection() ||
        this?.text?.substringBefore('<')?.removeSuffix("?")?.substringAfterLast('.') in MUTABLE_EXTRA_TYPES

    private fun KtExpression?.isMutableFactory(): Boolean = when {
        this == null -> false
        isMutableCollectionCall() || callName() in MUTABLE_EXTRA_FACTORIES -> true
        this is KtQualifiedExpression && callName() in PASS_THROUGH -> receiverExpression.isMutableFactory()
        else -> false
    }

    private companion object {
        const val VAR = "var"
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        val PASS_THROUGH = setOf("also", "apply")
        val MUTABLE_EXTRA_TYPES = setOf(
            "Array",
            "IntArray",
            "LongArray",
            "ByteArray",
            "BooleanArray",
            "SnapshotStateList",
            "SnapshotStateMap",
        )
        val MUTABLE_EXTRA_FACTORIES = setOf(
            "arrayOf",
            "arrayOfNulls",
            "intArrayOf",
            "longArrayOf",
            "byteArrayOf",
            "booleanArrayOf",
            "IntArray",
            "LongArray",
            "ByteArray",
            "BooleanArray",
            "mutableStateListOf",
            "mutableStateMapOf",
        )
        const val MESSAGE =
            "служебное значение — поле state («VM bookkeeping»), отменяемая/перезапускаемая корутина — " +
                "launchReplacing(key)/cancelLaunched(key) из BaseViewModel (docs/patterns.md, «BaseViewModel»)"
    }
}
