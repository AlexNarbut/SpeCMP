package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/** Отсутствие значения в domain-модели — `null`: `""`/`-1`/`"unknown"` выглядят как настоящие данные. По именам. */
class DomainSentinelDefault(config: Config) :
    Rule(config, "Domain-модель не подставляет sentinel-значения (\"\", -1, \"unknown\") вместо null.") {

    override fun visitParameter(parameter: KtParameter) {
        super.visitParameter(parameter)
        if (parameter.parent?.parent !is KtPrimaryConstructor) return
        check(parameter.defaultValue)
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        if (!property.isLocal) check(property.initializer)
    }

    override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
        super.visitPropertyAccessor(accessor)
        if (!accessor.isGetter) return
        when (val body = accessor.bodyExpression) {
            is KtBlockExpression -> body.collectDescendantsOfType<KtReturnExpression>()
                .forEach { check(it.returnedExpression) }

            else -> check(body)
        }
    }

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {
        super.visitSecondaryConstructor(constructor)
        constructor.getDelegationCallOrNull()?.valueArguments?.forEach { check(it.getArgumentExpression()) }
    }

    private fun check(value: KtExpression?) {
        if (value == null || !value.containingKtFile.packageFqName.asString().contains(DOMAIN_MODEL)) return
        if (KtPsiUtil.safeDeparenthesize(
                value,
            ).isSentinel()
        ) {
            report(Finding(Entity.from(value), "${value.text} $MESSAGE"))
        }
    }

    private fun KtExpression.isSentinel(): Boolean = when (this) {
        is KtStringTemplateExpression -> !hasInterpolation() && text.trim('"').lowercase() in STRING_SENTINELS

        is KtPrefixExpression ->
            operationReference.text == "-" &&
                (baseExpression as? KtConstantExpression)?.text?.trimEnd('L', 'l') == "1"

        else -> false
    }

    private companion object {
        const val DOMAIN_MODEL = ".domain.model"
        val STRING_SENTINELS = setOf("", "unknown", "none", "n/a", "-")
        const val MESSAGE =
            "в domain-модели: отсутствие значения — null, а не sentinel (docs/patterns.md, «Domain»)."
    }
}
