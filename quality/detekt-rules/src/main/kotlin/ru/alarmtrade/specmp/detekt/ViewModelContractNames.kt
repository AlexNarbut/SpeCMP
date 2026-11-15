package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

/**
 * Имя — последним сегментом, без пакета, `?` и алиаса импорта; собственные параметры типа класса не проверяются.
 * Промежуточная база `*ViewModel<A, B>` проверяется как BaseViewModel. По PSI, без типов.
 */
class ViewModelContractNames(config: Config) :
    Rule(
        config,
        "Дженерики BaseViewModel<S, E>: S оканчивается на State, E — на Effect (или Nothing).",
    ) {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        val ownTypeParameters = classOrObject.typeParameters.mapNotNull { it.name }.toSet()
        val bases = classOrObject.containingKtFile.namesWithAliases(VIEW_MODEL_BASES)
        classOrObject.superTypeListEntries
            .mapNotNull { it.typeReference?.typeElement as? KtUserType }
            .filter { it.isViewModelBase(bases) }
            .forEach { baseType ->
                val arguments = baseType.typeArgumentsAsTypes
                arguments.getOrNull(0)?.let { state ->
                    val name = state.simpleName()
                    if (name !in ownTypeParameters && !name.endsWith(STATE_SUFFIX)) {
                        report(state, "$STATE_MESSAGE`$name`")
                    }
                }
                arguments.getOrNull(1)?.let { effect ->
                    val name = effect.simpleName()
                    if (name !in ownTypeParameters && name != NOTHING && !name.endsWith(EFFECT_SUFFIX)) {
                        report(effect, "$EFFECT_MESSAGE`$name`")
                    }
                }
            }
    }

    private fun KtUserType.isViewModelBase(bases: Set<String>): Boolean {
        val name = referencedName ?: return false
        return name in bases || (name.endsWith(VIEW_MODEL_SUFFIX) && typeArgumentsAsTypes.size == CONTRACT_ARITY)
    }

    private fun report(type: KtTypeReference, message: String) {
        report(Finding(Entity.from(type), message))
    }

    private fun KtTypeReference.simpleName(): String {
        val name = text.removeSuffix("?").substringBefore('<').substringAfterLast('.').trim()
        return containingKtFile.importDirectives.firstOrNull { it.aliasName == name }
            ?.importedFqName?.shortName()?.asString()
            ?: name
    }

    private companion object {
        val VIEW_MODEL_BASES = setOf("BaseViewModel", "BaseDeviceViewModel")
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        const val CONTRACT_ARITY = 2
        const val STATE_SUFFIX = "State"
        const val EFFECT_SUFFIX = "Effect"
        const val NOTHING = "Nothing"
        const val STATE_MESSAGE =
            "первый дженерик BaseViewModel — тип состояния, имя должно оканчиваться на State (docs/patterns.md): "
        const val EFFECT_MESSAGE =
            "второй дженерик BaseViewModel — тип эффекта, имя должно оканчиваться на Effect или быть Nothing " +
                "(docs/patterns.md): "
    }
}
