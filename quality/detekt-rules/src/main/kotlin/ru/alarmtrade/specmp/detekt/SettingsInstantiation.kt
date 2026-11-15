package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtQualifiedExpression

/** Платформенный `Settings()` создаёт только `core/common/di` (`platformSettings`, исключение путём в detekt.yml). */
class SettingsInstantiation(config: Config) :
    Rule(config, "Файл настроек создаёт AppSettingsFactory (core/common); владелец получает Settings через DI.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        val file = expression.containingKtFile
        val noArgs = expression.valueArguments.isEmpty() && expression.lambdaArguments.isEmpty()
        val forbidden = (noArgs && callee in file.namesWithAliases(setOf(SETTINGS))) ||
            callee in file.namesWithAliases(PLATFORM_SETTINGS) ||
            expression.isPlatformFactory()
        if (forbidden) report(expression, callee)
    }

    override fun visitCallableReferenceExpression(expression: KtCallableReferenceExpression) {
        super.visitCallableReferenceExpression(expression)
        if (expression.receiverExpression != null) return
        val name = expression.callableReference.getReferencedName()
        if (name in expression.containingKtFile.namesWithAliases(PLATFORM_SETTINGS)) report(expression, name)
    }

    private fun KtCallExpression.isPlatformFactory(): Boolean {
        if (calleeExpression?.text != FACTORY) return false
        val receiver = (parent as? KtQualifiedExpression)?.takeIf { it.selectorExpression == this }?.receiverExpression
        val owner = receiver?.text?.substringAfterLast('.') ?: return false
        return owner in containingKtFile.namesWithAliases(PLATFORM_SETTINGS)
    }

    private fun report(element: KtElement, name: String) {
        report(Finding(Entity.from(element), MESSAGE.format(name)))
    }

    private companion object {
        const val SETTINGS = "Settings"
        const val FACTORY = "Factory"
        val PLATFORM_SETTINGS = setOf("SharedPreferencesSettings", "NSUserDefaultsSettings", "PreferencesSettings")
        const val MESSAGE =
            "%s(…) вне core/common/di: получите файл владельца get<AppSettingsFactory>().create(<OWNER>_SETTINGS) " +
                "в DI-модуле и передайте его конструктором (docs/patterns.md)."
    }
}
