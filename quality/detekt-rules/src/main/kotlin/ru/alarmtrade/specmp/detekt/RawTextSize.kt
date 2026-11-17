package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * Свой размер текста мимо шкалы `CmpTextStyle`: `.sp`/`.em`/`.toSp()`, именованные `fontSize`/`lineHeight`, `TextStyle(...)`,
 * `TextUnit` (тип и конструктор), в т.ч. через алиасы импорта; по имени, без type resolution. Исключение —
 * `letterSpacing = x.sp`. Шкала и её реализация живут в `core/ui` (исключён в detekt.yml).
 */
class RawTextSize(config: Config) :
    Rule(config, "Размер текста задаётся только стилем шкалы CmpTextStyle (docs/ui-kit.md, «Типографика»).") {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val name = when (val selector = expression.selectorExpression) {
            is KtNameReferenceExpression -> selector.getReferencedName()
            is KtCallExpression -> selector.calleeExpression?.text
            else -> null
        } ?: return
        if (expression.getParentOfType<KtImportDirective>(strict = true) != null) return
        if (name !in expression.containingKtFile.namesWithAliases(SP)) return
        val argument = expression.getParentOfType<KtValueArgument>(strict = true)
        if (argument?.getArgumentName()?.asName?.asString() == LETTER_SPACING &&
            argument.getArgumentExpression() == expression
        ) {
            return
        }
        report(Finding(Entity.from(expression), "${expression.text}: размер текста — только стилем CmpTextStyle."))
    }

    override fun visitArgument(argument: KtValueArgument) {
        super.visitArgument(argument)
        val name = argument.getArgumentName()?.asName?.asString() ?: return
        if (name !in SIZE_ARGUMENTS) return
        report(Finding(Entity.from(argument), "$name = …: свой размер текста запрещён — стиль CmpTextStyle."))
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val name = expression.calleeExpression?.text ?: return
        if (name !in expression.containingKtFile.namesWithAliases(CONSTRUCTORS)) return
        report(Finding(Entity.from(expression), "$name(…) мимо дизайн-системы — стиль CmpTextStyle."))
    }

    override fun visitTypeReference(typeReference: KtTypeReference) {
        super.visitTypeReference(typeReference)
        val element = typeReference.typeElement
        val type = (element as? KtNullableType)?.innerType ?: element
        val name = (type as? KtUserType)?.referencedName ?: return
        if (name !in typeReference.containingKtFile.namesWithAliases(TYPES)) return
        report(Finding(Entity.from(typeReference), "$name: размер текста передаётся стилем CmpTextStyle, не числом."))
    }

    private companion object {
        const val LETTER_SPACING = "letterSpacing"
        val SP = setOf("sp", "em", "toSp")
        val SIZE_ARGUMENTS = setOf("fontSize", "lineHeight")
        val CONSTRUCTORS = setOf("TextStyle", "TextUnit")
        val TYPES = setOf("TextUnit", "TextStyle")
    }
}
