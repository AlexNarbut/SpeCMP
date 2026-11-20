package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.barcode.BarcodeScannerAccess
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.device.DeviceIdQuery
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportInstaller
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnitLookup
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportInstallerRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportReadingRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/**
 * Паспорт без блока, шаг «Блок»: ID (правила — `DeviceIdQuery`, как у информации о товаре) → модель с сервера;
 * не нашли, нет сети или ошибка — модель из списка или «Другая — вписать». Список моделей грузится один раз.
 */
internal class PassportLookupViewModel(
    private val reading: PassportReadingRepository,
    private val installers: PassportInstallerRepository,
    private val permissions: PermissionsProvider,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<PassportLookupState, PassportLookupEffect>(PassportLookupState()) {

    fun onIdChange(text: String) {
        intent {
            reduce {
                copy(
                    unitId = text.take(MAX_ID_LENGTH),
                    idError = null,
                    lookup = if (lookup == UnitLookupStatus.Searching) lookup else UnitLookupStatus.Idle,
                    searchedId = null,
                )
            }
        }
    }

    fun onFind() {
        intent {
            when (val query = DeviceIdQuery.parse(currentState.unitId)) {
                is DeviceIdQuery.Invalid -> reduce { copy(idError = query.error, lookup = UnitLookupStatus.Idle) }
                is DeviceIdQuery.Valid -> find(query.id.toString())
            }
        }
    }

    fun onModelSelect(model: String) {
        intent { reduce { copy(model = model, customModel = false) } }
    }

    fun onOtherModel() {
        intent { reduce { copy(model = null, customModel = true) } }
    }

    fun onCustomModelChange(text: String) {
        intent { reduce { copy(customText = text.take(MAX_MODEL_LENGTH)) } }
    }

    fun onModelsRetry() {
        loadModels()
    }

    fun onNext() {
        intent {
            val state = currentState
            val model = state.chosenModel ?: return@intent
            val id = state.searchedId ?: return@intent
            if (state.creating) return@intent
            reduce { copy(creating = true) }
            val response = withContext(dispatchers.io) {
                val installer = (installers.installer() as? Response.Success)?.value ?: PassportInstaller()
                reading.createWithoutUnit(model, id, installer)
            }
            reduce { copy(creating = false) }
            when (response) {
                is Response.Success -> postSideEffect(PassportLookupEffect.Created(response.value.id))

                is Response.Error ->
                    postSideEffect(PassportLookupEffect.CreateFailed(PassportError.from(response.exception)))
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

    fun onScanned(number: String) {
        intent {
            reduce { copy(scanner = null, unitId = number, idError = null) }
            onFind()
        }
    }

    fun onScanClose() {
        intent { reduce { copy(scanner = null) } }
    }

    fun onScanManual() {
        intent {
            reduce { copy(scanner = null) }
            postSideEffect(PassportLookupEffect.FocusId)
        }
    }

    private fun find(id: String) {
        launchReplacing(FIND) {
            reduce { copy(unitId = id, idError = null, lookup = UnitLookupStatus.Searching, searchedId = id) }
            val lookup = when (val response = withContext(dispatchers.io) { reading.findUnit(id) }) {
                is Response.Success -> when (val found = response.value) {
                    is PassportUnitLookup.Found -> UnitLookupStatus.Found(found.systemName, found.systemId)
                    PassportUnitLookup.NotFound -> UnitLookupStatus.NotFound
                    PassportUnitLookup.Offline -> UnitLookupStatus.Offline
                }

                is Response.Error -> UnitLookupStatus.Failed(PassportError.from(response.exception))
            }
            reduce { copy(lookup = lookup) }
            if (currentState.manual && currentState.models !is LCEState.Content) loadModels()
        }
    }

    private fun loadModels() {
        launchReplacing(MODELS) {
            reduce { copy(models = LCEState.Loading) }
            val response = withContext(dispatchers.io) { reading.unitModels() }
            reduce { copy(models = response.map { it.toImmutableList() }.asLCEState(PassportError::from)) }
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

    private companion object {
        const val FIND = "find"
        const val MODELS = "models"
        const val CAMERA = "camera"
        const val MAX_ID_LENGTH = 40
        const val MAX_MODEL_LENGTH = 60
    }
}
