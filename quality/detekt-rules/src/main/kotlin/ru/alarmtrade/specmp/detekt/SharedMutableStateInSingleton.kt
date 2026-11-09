package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

/**
 * Разделяемое изменяемое состояние в долгоживущем объекте (docs/patterns.md, «Конкурентность»): `var` и `val`
 * с изменяемой коллекцией у класса/объекта, который
 * - держит собственный `CoroutineScope` (свойство или параметр конструктора с таким типом/инициализатором), или
 * - сервисный в data-слое: пакет `..data..` и имя `*Repository`/`*Coordinator`/`*Controller`/`*Session`/`*Impl`.
 *
 * Его зовут из нескольких корутин и потоков: голый `var` не атомарен и не виден между потоками. Разрешены
 * `MutableStateFlow` + `update { }`/`compareAndSet` (это `val`). Поле, которое читают и пишут только под `Mutex`, или
 * объект с явным confinement (один поток) — `@Suppress("SharedMutableStateInSingleton")` с причиной.
 * Проверяются члены самого класса (не вложенных) и параметры его первичного конструктора. Без type resolution.
 */
class SharedMutableStateInSingleton(config: Config) :
    Rule(config, "Изменяемое поле в разделяемом сервисе: MutableStateFlow + update { } или Mutex.") {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject is KtObjectDeclaration && classOrObject.isCompanion()) return
        if (!classOrObject.isShared()) return
        classOrObject.declarations.filterIsInstance<KtProperty>().forEach { property ->
            when {
                property.isVar -> report(property, "var ${property.name}")

                property.typeReference.isMutableCollection() || property.initializer.isMutableCollectionCall() ->
                    report(property, "изменяемая коллекция ${property.name}")
            }
        }
        classOrObject.primaryConstructorParameters.filter { it.valOrVarKeyword != null }.forEach { parameter ->
            when {
                parameter.isMutable -> report(parameter, "var ${parameter.name}")

                parameter.typeReference.isMutableCollection() -> report(
                    parameter,
                    "изменяемая коллекция ${parameter.name}",
                )
            }
        }
    }

    private fun report(declaration: KtNamedDeclaration, what: String) {
        val owner = declaration.containingClassOrObject?.name
        report(
            Finding(
                Entity.from(declaration),
                "$what в $owner: объект разделяют корутины и потоки, голое поле не атомарно и не видно между " +
                    "потоками. Состояние — MutableStateFlow + update { }/compareAndSet; поле только под Mutex — " +
                    "@Suppress(\"SharedMutableStateInSingleton\") с причиной (docs/patterns.md, «Конкурентность»).",
            ),
        )
    }

    private fun KtClassOrObject.isShared(): Boolean = ownsScope() || isDataService()

    private fun KtClassOrObject.ownsScope(): Boolean {
        val properties = declarations.filterIsInstance<KtProperty>().map { it.typeReference to it.initializer }
        val parameters = primaryConstructorParameters.filter { it.valOrVarKeyword != null }
            .map { it.typeReference to null }
        return (properties + parameters).any { (type, initializer) ->
            type?.text?.removeSuffix("?") == COROUTINE_SCOPE || initializer.callName() == COROUTINE_SCOPE
        }
    }

    private fun KtClassOrObject.isDataService(): Boolean {
        val segments = containingKtFile.packageFqName.pathSegments().map { it.asString() }
        return DATA in segments && SERVICE_NAME.matches(name.orEmpty())
    }

    private companion object {
        const val COROUTINE_SCOPE = "CoroutineScope"
        const val DATA = "data"
        val SERVICE_NAME = Regex(".*(Repository|Coordinator|Controller|Session|Impl)")
    }
}
