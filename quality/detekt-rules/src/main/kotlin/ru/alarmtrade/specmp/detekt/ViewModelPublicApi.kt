package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.psi.psiUtil.isProtected

/**
 * Публичный API наследника `BaseViewModel` (docs/patterns.md, «BaseViewModel»): снаружи видны только `state` и
 * `sideEffect` из базы и функции-интенты — не `suspend`, результат `Unit`. Запрещены собственные публичные свойства
 * (в том числе `val` в первичном конструкторе), `suspend fun` и функции с результатом — включая выведенный `Job`
 * у `fun x() = safeLaunch { }`/`intent { }` и `Unit?` у `fun x() = y?.z()`. `internal` считается публичным,
 * `override` не проверяется (контракт задаёт супертип).
 *
 * Требует type resolution (наследование от `BaseViewModel` и выведенный тип результата): в задачах detekt без
 * классов правило молчит (как StateFlowValueMutation).
 */
class ViewModelPublicApi(config: Config) :
    Rule(config, "Публичный API ViewModel — state, sideEffect и интенты, возвращающие Unit."),
    RequiresAnalysisApi {

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        if (!classOrObject.extendsBaseViewModel()) return
        val members = classOrObject.declarations.filter { it is KtNamedFunction || it is KtProperty } +
            classOrObject.primaryConstructorParameters.filter { it.hasValOrVar() }
        members.filterIsInstance<KtCallableDeclaration>()
            .filter { !it.isPrivate() && !it.isProtected() && !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
            .forEach { member ->
                val problem = member.problem() ?: return@forEach
                report(
                    Finding(
                        Entity.atName(member),
                        "`${member.name}` в ${classOrObject.name}: $problem. Наружу — только state, sideEffect и " +
                            "интенты с результатом Unit; данные экрана — через state (docs/patterns.md, «BaseViewModel»)."
                    )
                )
            }
    }

    private fun KtCallableDeclaration.problem(): String? = when {
        this !is KtNamedFunction -> "публичное свойство"
        hasModifier(KtTokens.SUSPEND_KEYWORD) -> "публичная suspend-функция (интент запускает корутину сам)"
        !returnsUnit() -> "публичная функция с результатом (блочное тело или `: Unit`; `= safeLaunch {}` даёт Job)"
        else -> null
    }

    private fun KtNamedFunction.returnsUnit(): Boolean = analyze(this) { isPlainUnit(returnType) }

    private fun KaSession.isPlainUnit(type: KaType): Boolean =
        type is KaClassType && type.classId == StandardClassIds.Unit && !type.isMarkedNullable

    private fun KtClassOrObject.extendsBaseViewModel(): Boolean = analyze(this) {
        val symbol = classSymbol ?: return@analyze false
        symbol.superTypes.flatMap { listOf(it) + it.allSupertypes }
            .any { (it as? KaClassType)?.classId == BASE_VIEW_MODEL }
    }

    private companion object {
        val BASE_VIEW_MODEL =
            ClassId(FqName("ru.alarmtrade.specmp.core.ui.viewmodel"), Name.identifier("BaseViewModel"))
    }
}
