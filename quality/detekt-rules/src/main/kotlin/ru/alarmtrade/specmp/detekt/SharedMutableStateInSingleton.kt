package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

/**
 * Класс со своим `CoroutineScope`, сервис по имени (репозиторий, менеджер, контроллер, запускатель… — в любом пакете)
 * или `*Impl` data-слоя зовут из нескольких потоков. Поле под `Mutex` или с
 * confinement — `@Suppress("SharedMutableStateInSingleton")` с причиной. Без type resolution.
 */
class SharedMutableStateInSingleton(config: Config) :
    Rule(config, "Изменяемое поле в разделяемом сервисе: MutableStateFlow + update { } или Mutex.") {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject is KtObjectDeclaration && classOrObject.isNestedInShared()) return
        if (!classOrObject.isShared()) return
        // Вложенные object (и companion) разделяемого класса — такие же синглтоны: проверяются вместе с ним.
        val nestedObjects = classOrObject.declarations.filterIsInstance<KtObjectDeclaration>()
        val properties = (classOrObject.declarations + nestedObjects.flatMap { it.declarations })
            .filterIsInstance<KtProperty>()
        properties.forEach { property ->
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

    private fun KtObjectDeclaration.isNestedInShared(): Boolean = containingClassOrObject?.isShared() == true

    private fun KtClassOrObject.isShared(): Boolean = ownsScope() || isServiceByName()

    private fun KtClassOrObject.ownsScope(): Boolean {
        val properties = declarations.filterIsInstance<KtProperty>().map { it.typeReference to it.initializer }
        val parameters = primaryConstructorParameters.filter { it.valOrVarKeyword != null }
            .map { it.typeReference to null }
        return (properties + parameters).any { (type, initializer) ->
            type?.text?.removeSuffix("?")?.substringAfterLast('.') == COROUTINE_SCOPE ||
                initializer.callName() in SCOPE_FACTORIES
        }
    }

    // Интерфейс — контракт без состояния: его абстрактные var реализует класс, который проверяется сам.
    private fun KtClassOrObject.isServiceByName(): Boolean {
        if (this is KtClass && isInterface()) return false
        val name = name.orEmpty()
        if (name.isServiceName()) return true
        val segments = containingKtFile.packageFqName.pathSegments().map { it.asString() }
        return DATA in segments && DATA_IMPL_NAME.matches(name)
    }

    private companion object {
        const val COROUTINE_SCOPE = "CoroutineScope"
        val SCOPE_FACTORIES = setOf(COROUTINE_SCOPE, "MainScope")
        const val DATA = "data"
        val DATA_IMPL_NAME = Regex(".*Impl")
    }
}
