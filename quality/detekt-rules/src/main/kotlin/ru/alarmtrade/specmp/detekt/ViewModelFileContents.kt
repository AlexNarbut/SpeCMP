package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtTypeAlias

/**
 * Файл ViewModel содержит только ViewModel: State, Effect, UI-модели, enum'ы и прочие типы — в своих файлах,
 * private-хелперы — вложенными private-типами внутри VM. Top-level функции и свойства не проверяются.
 *
 * ViewModel — top-level класс, имя которого оканчивается на `ViewModel`, или класс с супертипом `…ViewModel`
 * (в том числе `BaseViewModel<S, E>`). Проверка по именам, без type resolution — работает
 * во всех source set'ах.
 */
class ViewModelFileContents(config: Config) :
    Rule(
        config,
        "Файл ViewModel содержит только ViewModel: остальные типы — в своих файлах или вложенными private-типами.",
    ) {

    override fun visitKtFile(file: KtFile) {
        super.visitKtFile(file)
        val declarations = file.declarations
        if (declarations.none { it is KtClass && it.isViewModel() && !it.isLegacyViewModel() }) return
        declarations
            .filterIsInstance<KtNamedDeclaration>()
            .filter { it is KtClassOrObject || it is KtTypeAlias }
            .filterNot { it is KtClass && it.isViewModel() }
            .forEach { report(Finding(Entity.atName(it), it.message())) }
    }

    private fun KtNamedDeclaration.message(): String = if (hasModifier(KtTokens.PRIVATE_KEYWORD)) {
        "`$name` перенеси внутрь ViewModel как private вложенный тип: $SUFFIX"
    } else {
        "`$name` вынеси в свой файл $name.kt: $SUFFIX"
    }

    private fun KtClass.isViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private companion object {
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        const val SUFFIX = "файл ViewModel содержит только ViewModel"
    }
}
