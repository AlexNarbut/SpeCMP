package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.productinfo.domain.model.ProductQuery
import ru.alarmtrade.specmp.feature.productinfo.domain.repository.ProductInfoRepository
import ru.alarmtrade.specmp.feature.productinfo.presentation.model.ProductInfoError

/** Поиск по явной команде: сервер ищет только по точному номеру, запрос на каждый символ бессмыслен. */
internal class ProductInfoViewModel(
    args: ProductInfoArgs,
    private val repository: ProductInfoRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<ProductInfoState, Nothing>(args.initialState()) {

    override suspend fun onStart() {
        val initial = ProductQuery.parse(currentState.query)
        if (initial is ProductQuery.Valid) search(initial.id)
    }

    fun onQueryChange(text: String) {
        reduce { copy(query = text.take(MAX_QUERY_LENGTH), queryError = null) }
    }

    fun onClear() {
        reduce { copy(query = "", queryError = null) }
    }

    fun onSearch() {
        when (val query = ProductQuery.parse(currentState.query)) {
            is ProductQuery.Invalid -> reduce { copy(queryError = query.error) }

            is ProductQuery.Valid -> {
                reduce { copy(query = query.id.toString(), queryError = null) }
                search(query.id)
            }
        }
    }

    fun onRetry() {
        currentState.searchedId?.let(::search)
    }

    fun onToggleComponent(index: Int) {
        reduce { copy(expanded = if (index in expanded) expanded.remove(index) else expanded.add(index)) }
    }

    fun onToggleAll() {
        reduce {
            copy(expanded = if (allExpanded) persistentSetOf() else (0 until componentsCount).toPersistentSet())
        }
    }

    private fun search(id: Long) {
        launchReplacing(SEARCH) {
            reduce { copy(result = LCEState.Loading, searchedId = id, expanded = persistentSetOf()) }
            val response = withContext(dispatchers.io) { repository.find(id) }
            reduce { copy(result = response.asLCEState(ProductInfoError::from)) }
        }
    }

    private companion object {
        const val SEARCH = "search"
        const val MAX_QUERY_LENGTH = 40
    }
}
