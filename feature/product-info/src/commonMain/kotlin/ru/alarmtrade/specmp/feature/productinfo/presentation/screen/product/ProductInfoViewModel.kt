package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.barcode.BarcodeScannerAccess
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
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
    private val permissions: PermissionsProvider,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<ProductInfoState, ProductInfoEffect>(args.initialState()) {

    override suspend fun IntentScope.onStart() {
        val initial = ProductQuery.parse(currentState.query)
        if (initial is ProductQuery.Valid) search(initial.id)
    }

    fun onQueryChange(text: String) {
        intent {
            reduce { copy(query = text.take(MAX_QUERY_LENGTH), queryError = null) }
        }
    }

    fun onClear() {
        intent {
            reduce { copy(query = "", queryError = null) }
        }
    }

    fun onSearch() {
        intent {
            when (val query = ProductQuery.parse(currentState.query)) {
                is ProductQuery.Invalid -> reduce { copy(queryError = query.error) }

                is ProductQuery.Valid -> {
                    reduce { copy(query = query.id.toString(), queryError = null) }
                    search(query.id)
                }
            }
        }
    }

    fun onRetry() {
        currentState.searchedId?.let(::search)
    }

    fun onToggleComponent(index: Int) {
        intent {
            reduce { copy(expanded = if (index in expanded) expanded.remove(index) else expanded.add(index)) }
        }
    }

    fun onToggleAll() {
        intent {
            reduce {
                copy(expanded = if (allExpanded) persistentSetOf() else (0 until componentsCount).toPersistentSet())
            }
        }
    }

    fun onScanClick() {
        intent {
            reduce { copy(scanner = BarcodeScannerAccess.REQUESTING) }
            requestCamera()
        }
    }

    fun onScanAccessRetry() {
        requestCamera()
    }

    fun onScanOpenSettings() {
        intent {
            permissions.openAppSettings()
            reduce { copy(scanner = null) }
        }
    }

    /** Номер из штрих-кода: в поле и сразу поиск, как в старом Android-приложении (без диалога подтверждения). */
    fun onScanned(number: String) {
        intent {
            reduce { copy(scanner = null, query = number, queryError = null) }
            onSearch()
        }
    }

    fun onScanClose() {
        intent {
            reduce { copy(scanner = null) }
        }
    }

    fun onScanManual() {
        intent {
            reduce { copy(scanner = null) }
            postSideEffect(ProductInfoEffect.FocusQuery)
        }
    }

    private fun requestCamera() {
        launchReplacing(CAMERA) {
            val access = when (val result = permissions.requestCameraPermission()) {
                PermissionResult.Granted -> BarcodeScannerAccess.GRANTED

                is PermissionResult.Denied ->
                    if (result.canOpenSettings) BarcodeScannerAccess.BLOCKED else BarcodeScannerAccess.DENIED
            }
            reduce { if (scanner != null) copy(scanner = access) else this }
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
        const val CAMERA = "camera"
        const val MAX_QUERY_LENGTH = 40
    }
}
