package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType

/**
 * Дженерики `BaseViewModel<S, E>` (и `BaseDeviceViewModel<S, E>`) называют контракт экрана: имя типа состояния
 * оканчивается на `State`, имя типа эффекта — на `Effect` (экран без эффектов — `Nothing`). Имя берётся последним
 * сегментом, без пакета и `?`. Собственные параметры типа класса (промежуточная абстрактная база) не проверяются.
 *
 * Проверка по PSI, без type resolution — работает во всех source set'ах.
 */
class ViewModelContractNames(config: Config) :
    Rule(
        config,
        "Дженерики BaseViewModel<S, E>: S оканчивается на State, E — на Effect (или Nothing).",
    ) {

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)
        val ownTypeParameters = klass.typeParameters.mapNotNull { it.name }.toSet()
        klass.superTypeListEntries
            .mapNotNull { it.typeReference?.typeElement as? KtUserType }
            .filter { it.referencedName in VIEW_MODEL_BASES }
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

    private fun report(type: KtTypeReference, message: String) {
        report(Finding(Entity.from(type), message))
    }

    private fun KtTypeReference.simpleName(): String =
        text.removeSuffix("?").substringBefore('<').substringAfterLast('.').trim()

    private companion object {
        val VIEW_MODEL_BASES = setOf("BaseViewModel", "BaseDeviceViewModel")
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
