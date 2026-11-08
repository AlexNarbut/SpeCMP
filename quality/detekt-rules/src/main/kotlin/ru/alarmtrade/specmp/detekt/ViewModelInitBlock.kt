package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtAnonymousInitializer
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody

/**
 * `init { }` в теле ViewModel. Стартовая работа экрана — `override suspend fun onStart()` из `BaseViewModel`:
 * запускается при первой подписке UI и один раз за жизнь VM. `init` выполняется при создании VM, даже если экран так и не подписался.
 *
 * ViewModel — класс, имя которого оканчивается на `ViewModel`, или класс с супертипом `…ViewModel` (в том числе
 * `BaseViewModel<S, E>`). Проверка по именам, без type resolution — работает во всех source set'ах. `companion object` и вложенные классы не трогаем.
 */
class ViewModelInitBlock(config: Config) :
    Rule(
        config,
        "init во ViewModel запрещён: стартовая работа — onStart() из BaseViewModel."
    ) {

    override fun visitAnonymousInitializer(initializer: KtAnonymousInitializer) {
        super.visitAnonymousInitializer(initializer)
        val klass = (initializer.parent as? KtClassBody)?.parent as? KtClass ?: return
        if (klass.isViewModel()) report(Finding(Entity.from(initializer), MESSAGE))
    }

    private fun KtClass.isViewModel(): Boolean = name.isViewModelName() || superTypeListEntries.any { entry ->
        entry.typeReference?.text?.substringBefore('<')?.substringAfterLast('.').isViewModelName()
    }

    private fun String?.isViewModelName(): Boolean = this?.endsWith(VIEW_MODEL_SUFFIX) == true

    private companion object {
        const val VIEW_MODEL_SUFFIX = "ViewModel"
        const val MESSAGE =
            "init во ViewModel запрещён: стартовая работа — override suspend fun onStart() из BaseViewModel, " +
                "docs/patterns.md"
    }
}
