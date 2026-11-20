package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import ru.alarmtrade.specmp.core.barcode.BarcodeScannerAccess
import ru.alarmtrade.specmp.core.common.device.DeviceIdQueryError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/** Поиск блока по ID: не нашли, нет сети или ошибка — модель вводится вручную. */
@Immutable
internal sealed interface UnitLookupStatus {
    data object Idle : UnitLookupStatus

    data object Searching : UnitLookupStatus

    data class Found(val systemName: String, val systemId: String) : UnitLookupStatus

    data object NotFound : UnitLookupStatus

    data object Offline : UnitLookupStatus

    data class Failed(val error: PassportError) : UnitLookupStatus
}

/**
 * Экран «Блок» паспорта без блока. [searchedId] — ID, по которому искали (поле могли поправить после);
 * [customModel] — выбран пункт «Другая — вписать», модель — [customText].
 */
@Immutable
internal data class PassportLookupState(
    val unitId: String = "",
    val idError: DeviceIdQueryError? = null,
    val lookup: UnitLookupStatus = UnitLookupStatus.Idle,
    val searchedId: String? = null,
    val models: LCEState<ImmutableList<String>, PassportError> = LCEState.None,
    val model: String? = null,
    val customModel: Boolean = false,
    val customText: String = "",
    val creating: Boolean = false,
    val scanner: BarcodeScannerAccess? = null,
) {
    val manual: Boolean
        get() = lookup is UnitLookupStatus.NotFound ||
            lookup is UnitLookupStatus.Offline ||
            lookup is UnitLookupStatus.Failed

    /** Модель для паспорта: с сервера или выбранная/вписанная вручную. */
    val chosenModel: String?
        get() = when (val found = lookup) {
            is UnitLookupStatus.Found -> found.systemName
            else -> (if (customModel) customText else model)?.trim()?.takeIf { manual && it.isNotEmpty() }
        }

    val canProceed: Boolean get() = !creating && searchedId != null && chosenModel != null
}
