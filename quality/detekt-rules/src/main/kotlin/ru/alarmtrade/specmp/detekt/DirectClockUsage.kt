package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor

/**
 * «Сейчас» и пояс — из внедрённых `Clock`/`TimeZoneSource` (composable — `LocalClock`). Разрешён только дефолт
 * параметра первичного конструктора (`clock: Clock = Clock.System`). По именам — во всех source set'ах.
 */
class DirectClockUsage(config: Config) :
    Rule(config, "Время и часовой пояс — из внедрённых Clock/TimeZoneSource, не из системных часов напрямую.") {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val receiver = expression.receiverExpression.text.removeSuffix(COMPANION).substringAfterLast('.')
            .let { name -> expression.containingKtFile.aliasedNames()[name] ?: name }
        val selector = expression.selectorExpression ?: return
        val forbidden = when (selector) {
            is KtNameReferenceExpression -> selector.getReferencedName() == SYSTEM && receiver in SYSTEM_HOLDERS

            is KtCallExpression -> when (selector.calleeExpression?.text) {
                CURRENT_SYSTEM_DEFAULT -> receiver == TIME_ZONE
                in SYSTEM_TIME_CALLS -> receiver == SYSTEM
                else -> false
            }

            else -> false
        }
        if (forbidden) reportIfNotAllowed(expression, "$receiver.${selector.text}")
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != NS_DATE || expression.valueArguments.isNotEmpty()) return
        val qualified = expression.parent as? KtDotQualifiedExpression
        if (qualified != null && qualified.selectorExpression == expression) return
        reportIfNotAllowed(expression, "$NS_DATE()")
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)
        val path = importDirective.importedFqName?.asString()?.replace(COMPANION, "") ?: return
        if (FORBIDDEN_IMPORTS.any { path.endsWith(it) }) {
            report(Finding(Entity.from(importDirective), MESSAGE.format(path)))
        }
    }

    private fun KtFile.aliasedNames(): Map<String, String> = importDirectives.mapNotNull { directive ->
        val alias = directive.aliasName ?: return@mapNotNull null
        val original = directive.importedFqName?.shortName()?.asString() ?: return@mapNotNull null
        alias to original
    }.toMap()

    private fun reportIfNotAllowed(element: KtElement, source: String) {
        if (element.getParentOfType<KtImportDirective>(true) != null) return
        if (element.getParentOfType<KtPackageDirective>(true) != null) return
        if (isPrimaryConstructorDefault(element)) return
        report(Finding(Entity.from(element), MESSAGE.format(source)))
    }

    private fun isPrimaryConstructorDefault(element: KtElement): Boolean {
        var parameter = element.getParentOfType<KtParameter>(true)
        while (parameter != null) {
            val default = parameter.defaultValue
            if (default != null && default.isAncestor(element, strict = false)) {
                return parameter.ownerFunction is KtPrimaryConstructor
            }
            parameter = parameter.getParentOfType<KtParameter>(true)
        }
        return false
    }

    private companion object {
        const val SYSTEM = "System"
        const val TIME_ZONE = "TimeZone"
        const val CURRENT_SYSTEM_DEFAULT = "currentSystemDefault"
        const val NS_DATE = "NSDate"
        val SYSTEM_HOLDERS = setOf("Clock", "TimeZoneSource")
        val SYSTEM_TIME_CALLS = setOf("currentTimeMillis", "nanoTime")
        const val COMPANION = ".Companion"
        val FORBIDDEN_IMPORTS = setOf(
            "Clock.System",
            "TimeZoneSource.System",
            "TimeZone.currentSystemDefault",
            "System.currentTimeMillis",
            "System.nanoTime",
        )
        const val MESSAGE =
            "%s: время и часовой пояс берутся из внедрённых Clock/TimeZoneSource (параметр конструктора из DI, " +
                "в composable — LocalClock/LocalTimeZoneSource; дефолт допустим только у первичного конструктора) — " +
                "docs/patterns.md, «Источник времени»."
    }
}
