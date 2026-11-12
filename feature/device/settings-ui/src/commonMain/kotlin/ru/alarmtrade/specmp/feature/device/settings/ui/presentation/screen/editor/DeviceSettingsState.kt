package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.presentation.core.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsDiff
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.accepts
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

@Immutable
internal data class DeviceSettingsState(
    val header: SettingsHeader? = null,
    val menu: LCEState<SettingsSection, SettingsError> = LCEState.Loading,
    val changedIds: Set<Int> = emptySet(),
    val sectionChanges: Map<Int, Int> = emptyMap(),
    val searchQuery: String = "",
    val searchHits: List<SettingsSearchHit> = emptyList(),
    val editing: EditingSetting? = null,
    val dialog: SettingsDialog? = null,
    val isWriting: Boolean = false,
    val isResetting: Boolean = false,
    val canResetToFactory: Boolean = false,
    val treeWidthDp: Int? = null,
    val cars: Map<Int, CarSummary> = emptyMap(),
    val vehiclePicker: Boolean = false,
    val source: SourceHeader = SourceHeader.Device,
    val connectedDeviceName: String? = null,
    val canWriteToDevice: Boolean = false,
    val configurationPicker: ConfigurationPickRequest? = null,
    val diff: DiffState? = null,
    val saveForm: SaveForm? = null,
    val isApplying: Boolean = false,
    val isSaving: Boolean = false,
    /** Набираемый запрос: в [searchQuery] попадает вместе с результатом (после debounce), а не на каждый символ. */
    val searchInput: String = "",
    val changes: SettingsChangeTracker = SettingsChangeTracker(),
    val pendingLoad: SettingsSource? = null,
    val pendingFocus: Int? = null,
    val pendingLoadMenu: Boolean = false,
    /** Снимок для «Что изменится» (временный файл); после загрузки или отмены — удаляется. */
    val previewSnapshot: SettingsSource.File? = null,
    /** Снимок, отданный системному сохранению файла (до ответа `onFileExported`). */
    val pendingExport: PendingExport? = null,
    /**
     * `carId`, подпись к которым уже запрашивалась в этом чтении меню (в том числе неудачно — без сети запрос не
     * повторяется на каждую правку).
     */
    val requestedCarIds: Set<Int> = emptySet(),
) {
    val changedCount: Int get() = changedIds.size

    val barAction: BarAction
        get() = when {
            source.kind == SourceKind.DEVICE -> BarAction.WRITE
            source.canSave -> BarAction.SAVE
            else -> BarAction.SAVE_AS
        }

    val showWriteToDevice: Boolean
        get() = source.kind != SourceKind.DEVICE && canWriteToDevice && connectedDeviceName != null
}

internal enum class SourceKind { DEVICE, MINE, FILE, CLOUD, DEFAULTS }

@Immutable
internal data class SourceHeader(
    val kind: SourceKind,
    val title: String,
    val deviceName: String,
    val isReadOnly: Boolean,
    val canSave: Boolean,
) {
    companion object {
        val Device = SourceHeader(SourceKind.DEVICE, "", "", isReadOnly = false, canSave = true)
    }
}

internal enum class BarAction { WRITE, SAVE, SAVE_AS }

internal enum class LoadSourceKind { MINE, CLOUD, FILE, DEFAULTS }

internal enum class SaveTarget { MINE, FILE }

internal sealed interface DiffState {
    val title: String

    data class Busy(override val title: String) : DiffState

    data class Ready(override val title: String, val diff: SettingsDiff, val pendingChanges: Int) : DiffState

    /** «Повторить» — то же сравнение: [from] с тем же [closeAfter] (сессию чужого редактора не закрываем). */
    data class Error(override val title: String, val from: SettingsSource, val closeAfter: Boolean) : DiffState
}

@Immutable
internal data class SaveForm(
    val name: String,
    val description: String,
    val deviceName: String,
    val exitAfter: Boolean = false,
)

internal data class PendingExport(val snapshot: SettingsSource.File, val exitAfter: Boolean)

internal data class SettingsHeader(val deviceName: String, val deviceId: Long, val singleCanCode: String?)

internal data class EditingSetting(
    val setting: Setting,
    val original: SettingValue,
    val draft: SettingValue,
    // Поиск автомобиля по CAN-коду в окне CAN-кода; пустой Content — для кода автомобиля нет.
    val carSearch: LCEState<List<CarSummary>, SettingsError> = LCEState.None,
) {
    /** Черновик можно применить — считается один раз на изменение черновика, не на каждую рекомпозицию. */
    val canApply: Boolean = setting.accepts(draft)
}

internal sealed interface SettingsDialog {
    data class Description(val node: SettingNode) : SettingsDialog

    data class ExitConfirm(val target: ExitTarget) : SettingsDialog

    data object SingleCanFirmware : SettingsDialog
    data object WriteError : SettingsDialog
    data object ResetConfirm : SettingsDialog
    data object ResetError : SettingsDialog

    data object LoadSources : SettingsDialog

    data class SaveTargets(val exitAfter: Boolean) : SettingsDialog

    data class ModelMismatch(
        val source: SettingsSource,
        val sourceDeviceName: String,
        val deviceName: String,
        val forWrite: Boolean,
        val closeAfter: Boolean = true,
    ) : SettingsDialog

    data class LoadError(val kind: LoadErrorKind) : SettingsDialog

    data class SaveError(val retry: SaveRetry) : SettingsDialog

    data class OverwriteConfirm(val exitAfter: Boolean) : SettingsDialog

    data object ExitSave : SettingsDialog
}

internal enum class LoadErrorKind {
    MISMATCH,

    FILE,

    OTHER,
}

internal sealed interface SaveRetry {
    data class Mine(val form: SaveForm) : SaveRetry
    data object File : SaveRetry
    data class Source(val exitAfter: Boolean) : SaveRetry
}

internal enum class ExitTarget { BACK, FIRMWARE_UPDATE }

@Immutable
internal data class SettingsLocation(val sectionPath: List<Int>, val isSearch: Boolean) {
    val sectionId: Int? get() = sectionPath.lastOrNull()
}

/** Отдельно от state экрана: строки не перерисовываются от ввода, диалогов и прочих полей, которые их не касаются. */
@Immutable
internal data class RowMarks(
    val changedIds: Set<Int>,
    val sectionChanges: Map<Int, Int>,
    val cars: Map<Int, CarSummary>,
)
