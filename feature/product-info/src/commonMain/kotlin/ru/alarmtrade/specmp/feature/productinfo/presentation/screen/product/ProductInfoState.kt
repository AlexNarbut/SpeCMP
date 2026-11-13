package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductInfo
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQueryError
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoError

/** [initialQuery] — номер из входа (монтаж, ассистент): ищется сразу; [connectedId] — ID подключённого блока. */
@Immutable
internal data class ProductInfoArgs(val initialQuery: String?, val connectedId: Long?)

/**
 * [searchedId] — номер последнего запроса (для «Повторить» и текста «не найден»); [expanded] — раскрытые строки
 * компонентов по индексу, сбрасываются новым поиском.
 */
@Immutable
internal data class ProductInfoState(
    val query: String = "",
    val queryError: ProductQueryError? = null,
    val result: LCEState<ProductInfo, ProductInfoError> = LCEState.None,
    val searchedId: Long? = null,
    val connectedId: Long? = null,
    val expanded: PersistentSet<Int> = persistentSetOf(),
) {
    /** Пометка «Подключённый блок» — по ID найденного товара, а не по тексту поля. */
    val isConnectedUnit: Boolean
        get() = connectedId != null && (result as? LCEState.Content)?.content?.product?.id == connectedId

    val componentsCount: Int
        get() = (result as? LCEState.Content)?.content?.kit?.components?.size ?: 0

    val allExpanded: Boolean get() = componentsCount > 0 && expanded.size == componentsCount
}

internal fun ProductInfoArgs.initialState(): ProductInfoState =
    ProductInfoState(query = initialQuery.orEmpty(), connectedId = connectedId)
