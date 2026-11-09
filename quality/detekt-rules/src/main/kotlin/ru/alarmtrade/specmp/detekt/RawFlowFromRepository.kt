package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

/** Ловит реализации с любым именем; интерфейсы проверяет Konsist `repository-contract`. Требует type resolution. */
class RawFlowFromRepository(config: Config) :
    Rule(config, "Поток репозитория — Flow<Response<T>>: значения — Response.Success, ошибка — Response.Error."),
    RequiresAnalysisApi {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (classOrObject is KtClass && classOrObject.isInterface()) return
        if (!classOrObject.implementsRepository()) return
        val members = classOrObject.declarations.filter { it is KtNamedFunction || it is KtProperty } +
            classOrObject.primaryConstructorParameters.filter { it.hasValOrVar() }
        members.filterIsInstance<KtCallableDeclaration>()
            .filter { !it.isPrivate() }
            .filter { it.returnsRawFlow() }
            .forEach { declaration ->
                report(
                    Finding(
                        Entity.atName(declaration),
                        "`${declaration.name}` в ${classOrObject.name ?: "репозитории"} отдаёт поток без Response: " +
                            "значения — Response.Success (SDK — toResponse()), " +
                            "ошибка потока — catch → Response.Error " +
                            "(docs/patterns.md, «Контракты репозиториев»).",
                    ),
                )
            }
    }

    private fun KtClassOrObject.implementsRepository(): Boolean = analyze(this) {
        val symbol = classSymbol ?: return@analyze false
        symbol.superTypes.flatMap { listOf(it) + it.allSupertypes }.any { it.isRepositoryInterface() }
    }

    private fun KaType.isRepositoryInterface(): Boolean {
        val classType = this as? KaClassType ?: return false
        val symbol = classType.symbol as? KaClassSymbol ?: return false
        return symbol.classKind == KaClassKind.INTERFACE &&
            classType.classId.shortClassName.asString().endsWith(REPOSITORY)
    }

    private fun KtCallableDeclaration.returnsRawFlow(): Boolean = analyze(this) {
        val flowArgument = flowElementType(returnType) ?: return@analyze false
        val element = flowArgument.type ?: return@analyze true
        !element.isSubtypeOf(RESPONSE)
    }

    // Аргумент Flow<T> для самого типа или его супертипа (StateFlow<T> : Flow<T>); null — не поток.
    private fun KaSession.flowElementType(type: KaType) = (sequenceOf(type) + type.allSupertypes)
        .filterIsInstance<KaClassType>()
        .firstOrNull { it.classId == FLOW }
        ?.typeArguments
        ?.firstOrNull()

    private companion object {
        const val REPOSITORY = "Repository"
        val FLOW = ClassId(FqName("kotlinx.coroutines.flow"), Name.identifier("Flow"))
        val RESPONSE = ClassId(FqName("ru.alarmtrade.specmp.core.common"), Name.identifier("Response"))
    }
}
