package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Composable не берёт сервисы (репозитории, менеджеры, use case…) из DI и не держит их ключом эффекта: подписки,
 * запуск и чтение — во ViewModel (`koinViewModel`), UI получает state. Без type resolution — по именам типа,
 * переменной и параметра.
 */
class ServiceInComposable(config: Config) :
    Rule(config, "Сервис в composable: koinInject/эффект с репозиторием, менеджером, use case — работа во ViewModel.") {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (!expression.isInsideComposable()) return
        val file = expression.containingKtFile
        val callee = expression.calleeExpression?.text ?: return
        when (callee) {
            in file.namesWithAliases(INJECTORS) -> expression.injectedType()?.takeIf { it.isService() }?.let {
                report(expression, "$callee<$it>()")
            }

            in file.namesWithAliases(EFFECTS) ->
                expression.valueArguments
                    .mapNotNull { it.getArgumentExpression() }
                    .filter { it.text.substringAfterLast('.').isService() }
                    .forEach { report(it, "ключ $callee(${it.text})") }
        }
    }

    // Тип — аргумент дженерика, иначе явный тип свойства, которому присваивается вызов.
    private fun KtCallExpression.injectedType(): String? =
        (typeArguments.firstOrNull()?.typeReference ?: (parent as? KtProperty)?.typeReference)?.simpleName()

    private fun KtTypeReference.simpleName(): String =
        text.substringBefore('<').removeSuffix("?").substringAfterLast('.')

    private fun String.isService(): Boolean {
        val name = replaceFirstChar { it.uppercaseChar() }
        return name.isServiceName() || name.endsWith(USE_CASE) || name.endsWith(INTERACTOR)
    }

    private fun KtCallExpression.isInsideComposable(): Boolean = parents.filterIsInstance<KtNamedFunction>()
        .any { function -> function.annotationEntries.any { it.shortName?.asString() == COMPOSABLE } }

    private fun report(element: KtExpression, what: String) {
        report(
            Finding(
                Entity.from(element),
                "$what в composable: подписки, запуск и чтение сервисов — во ViewModel (koinViewModel, onStart), " +
                    "UI получает state и вызывает интенты; платформенное (пикеры, уведомления ОС) — порт " +
                    "presentation/platform, его передают во VM. docs/patterns.md, «Сервисы — только во ViewModel».",
            ),
        )
    }

    private companion object {
        const val COMPOSABLE = "Composable"
        const val USE_CASE = "UseCase"
        const val INTERACTOR = "Interactor"
        val INJECTORS = setOf("koinInject")
        val EFFECTS = setOf("LaunchedEffect", "DisposableEffect", "produceState")
    }
}
