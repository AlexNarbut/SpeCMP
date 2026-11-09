package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

enum class ArchiveRowKind { New, Installed, Older }

/** У единой CAN-прошивки один номер (`3.61`) выходит под разные CAN-коды и профили — это разные файлы. */
internal fun archiveRowKey(version: String, canCode: String?, canVersion: String?): String =
    listOfNotNull(version, canCode, canVersion).joinToString("|")

@Immutable
data class ArchiveRowUi(
    val version: String,
    val createdOn: LocalDate,
    val description: String,
    val kind: ArchiveRowKind,
    val canCode: String? = null,
    val canVersion: String? = null,
    val canMatchesDevice: Boolean = false,
    val canProfileDiffers: Boolean = false,
) {
    val key: String get() = archiveRowKey(version, canCode, canVersion)
}

@Immutable
data class CanFilterUi(
    val input: String = "",
    val applied: String? = null,
    val carTitle: String? = null,
    val canPickVehicle: Boolean = false,
    val deviceCode: String? = null,
    val deviceProfile: String? = null,
    val deviceFactory: Boolean = false,
    val noUnit: Boolean = false,
) {
    val isDeviceCode: Boolean get() = deviceCode != null && input == deviceCode

    val canResetToDevice: Boolean get() = deviceCode != null && input != deviceCode

    val isPartial: Boolean get() = input.isNotEmpty() && input.length < CAN_CODE_DIGITS

    val appliedIsDevice: Boolean get() = deviceCode != null && applied == deviceCode
}

internal const val CAN_CODE_DIGITS = 4

@Immutable
data class ArchiveSavedUi(val key: String, val fileName: String, val path: String, val folder: String?)

sealed interface ArchiveExportError {
    data class Download(val error: FirmwareError) : ArchiveExportError

    data object Permission : ArchiveExportError

    data object Save : ArchiveExportError

    data object Share : ArchiveExportError
}

@Immutable
data class RollbackUi(val version: String, val installedVersion: String)

@Immutable
data class FirmwareArchiveState(
    /** Пустой `Content` — сервер вернул пустой список; `isUpdating`/`updateError` — догрузка следующей страницы. */
    val content: LCEState<List<ArchiveRowUi>, FirmwareError> = LCEState.Loading,
    val selected: String? = null,
    val rollback: RollbackUi? = null,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val installedVersion: String? = null,
    val deviceTitle: String = "",
    val profile: UpdateProfile = UpdateProfile.Main,
    /** `null` — фильтра нет (Bluetooth-модуль). */
    val canFilter: CanFilterUi? = null,
    val viewOnly: Boolean = false,
    val vehiclePicker: Boolean = false,
    /** Модель блока для каталога автомобилей (`null` — каталог с выбором типа устройства). */
    val deviceName: String? = null,
    val noUnit: Boolean = false,
    val moduleName: String? = null,
    val noModelFirmware: Boolean = false,
    val busy: String? = null,
    val saved: ArchiveSavedUi? = null,
    val exportError: ArchiveExportError? = null,
    val saveTarget: FirmwareSaveTarget = FirmwareSaveTarget.Downloads,
    val canShare: Boolean = false,
    val canOpenFolder: Boolean = false,
    /** Исходные данные списка и фильтра; поля экрана выше считаются из них (`withSource`). */
    val source: ArchiveSource = ArchiveSource(),
)

@Immutable
data class ArchiveSource(
    /** Первая страница: `Content(Unit)` — загружена (строки — в [rows]). */
    val load: LCEState<Unit, FirmwareError> = LCEState.Loading,
    val items: List<FirmwareVersion> = emptyList(),
    /** [items] от новой к старой; пересчитываются только при новом списке версий. */
    val rows: List<ArchiveRowUi> = emptyList(),
    val page: Int = 0,
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val moreError: FirmwareError? = null,
    val selected: String? = null,
    val rollback: String? = null,
    val moduleId: Long? = null,
    val noModule: Boolean = false,
    val canInput: String = "",
    val canApplied: String? = null,
    val carTitle: String? = null,
) {
    val canLoadMore: Boolean get() = load is LCEState.Content && hasMore && !loadingMore && moreError == null
}

class FirmwareArchiveActions(
    val onBack: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onEndReached: () -> Unit = {},
    val onRetryMore: () -> Unit = {},
    val onSelectRow: (String) -> Unit = {},
    val onPick: (String) -> Unit = {},
    val onRollbackConfirm: () -> Unit = {},
    val onRollbackDismiss: () -> Unit = {},
    val onCanInput: (String) -> Unit = {},
    val onCanResetToDevice: () -> Unit = {},
    val onVehiclePickerOpen: () -> Unit = {},
    val onChangeModel: () -> Unit = {},
    val onChannelSelect: (FirmwareChannel) -> Unit = {},
    val onDownload: (String) -> Unit = {},
    val onShare: (String) -> Unit = {},
    val onSavedShare: () -> Unit = {},
    val onSavedOpenFolder: () -> Unit = {},
    val onSavedDismiss: () -> Unit = {},
    val onExportErrorDismiss: () -> Unit = {},
)
