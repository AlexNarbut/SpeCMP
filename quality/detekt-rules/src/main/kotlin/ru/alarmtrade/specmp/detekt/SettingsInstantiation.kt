package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

/** Платформенный `Settings()` создаёт только `core/common/di` (`platformSettings`, исключение путём в detekt.yml). */
class SettingsInstantiation(config: Config) :
    Rule(config, "Файл настроек создаёт AppSettingsFactory (core/common); владелец получает Settings через DI.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val callee = expression.calleeExpression?.text ?: return
        val forbidden =
            (callee == SETTINGS && expression.valueArguments.isEmpty() && expression.lambdaArguments.isEmpty()) ||
                callee in PLATFORM_SETTINGS
        if (forbidden) report(Finding(Entity.from(expression), MESSAGE.format(callee)))
    }

    private companion object {
        const val SETTINGS = "Settings"
        val PLATFORM_SETTINGS = setOf("SharedPreferencesSettings", "NSUserDefaultsSettings", "PreferencesSettings")
        const val MESSAGE =
            "%s(…) вне core/common/di: получите файл владельца get<AppSettingsFactory>().create(<OWNER>_SETTINGS) " +
                "в DI-модуле и передайте его конструктором (docs/patterns.md)."
    }
}
