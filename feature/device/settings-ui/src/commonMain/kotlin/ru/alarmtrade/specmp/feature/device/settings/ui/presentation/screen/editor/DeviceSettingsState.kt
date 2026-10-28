package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsDiff
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

/** Состояние редактора настроек: меню, изменения, окно редактирования, диалоги, запись. */
@Immutable
internal data class DeviceSettingsState(
    val header: SettingsHeader? = null,
    val menu: LCEState<SettingsSection, SettingsError> = LCEState.Loading,
    /** Изменённые и не записанные настройки (id) — подсветка строк и панель «Изменено: N». */
    val changedIds: Set<Int> = emptySet(),
    /** Сколько настроек изменено внутри раздела (id раздела → число) — счётчики в строках, дереве, крошках. */
    val sectionChanges: Map<Int, Int> = emptyMap(),
    val searchQuery: String = "",
    val searchHits: List<SettingsSearchHit> = emptyList(),
    val editing: EditingSetting? = null,
    val dialog: SettingsDialog? = null,
    /** Идёт запись в устройство — блокирующий прогресс. */
    val isWriting: Boolean = false,
    /** Идёт сброс блока к заводским настройкам — блокирующий прогресс. */
    val isResetting: Boolean = false,
    /** Источник поддерживает сброс к заводским (пункт меню «⋯»). */
    val canResetToFactory: Boolean = false,
    /** Ширина колонки дерева, заданная пользователем (dp); `null` — по умолчанию раскладки. */
    val treeWidthDp: Int? = null,
    /** Автомобили по id (подпись к CAN-коду в строке и в окне). */
    val cars: Map<Int, CarSummary> = emptyMap(),
    /** Открыт выбор автомобиля из каталога для CAN-кода (поверх настроек; окно редактирования скрыто). */
    val vehiclePicker: Boolean = false,
    /** Что открыто: блок или конфигурация (шапка, меню «⋯», действие панели внизу). */
    val source: SourceHeader = SourceHeader.Device,
    /** Модель подключённого блока (`null` — не подключён): «Записать в блок» из конфигурации. */
    val connectedDeviceName: String? = null,
    /** Хост умеет открыть редактор блока с загрузкой конфигурации («Записать в блок»). */
    val canWriteToDevice: Boolean = false,
    /** Открыт выбор конфигурации для загрузки («Мои» / облако) — поверх настроек. */
    val configurationPicker: ConfigurationPickRequest? = null,
    /** «Что изменится» перед загрузкой конфигурации. */
    val diff: DiffState? = null,
    /** Форма «Сохранить в мои конфигурации» (название и описание). */
    val saveForm: SaveForm? = null,
    /** Подставляем значения конфигурации в редактор — блокирующий прогресс. */
    val isApplying: Boolean = false,
    /** Сохраняем (в конфигурацию, файл, «Мои») — блокирующий прогресс. */
    val isSaving: Boolean = false
) {
    val changedCount: Int get() = changedIds.size

    /** Действие панели «Изменено: N · …»: записать в блок / сохранить в источник / сохранить как новую. */
    val barAction: BarAction
        get() = when {
            source.kind == SourceKind.DEVICE -> BarAction.WRITE
            source.canSave -> BarAction.SAVE
            else -> BarAction.SAVE_AS
        }

    /** «Записать в блок» в меню конфигурации — только при подключённом блоке. */
    val showWriteToDevice: Boolean
        get() = source.kind != SourceKind.DEVICE && canWriteToDevice && connectedDeviceName != null
}

internal enum class SourceKind { DEVICE, MINE, FILE, CLOUD, DEFAULTS }

/** Шапка источника: вид (иконка и подпись плашки), название, модель блока; «только просмотр» и можно ли «Сохранить». */
@Immutable
internal data class SourceHeader(
    val kind: SourceKind,
    val title: String,
    val deviceName: String,
    val isReadOnly: Boolean,
    val canSave: Boolean
) {
    companion object {
        val Device = SourceHeader(SourceKind.DEVICE, "", "", isReadOnly = false, canSave = true)
    }
}

internal enum class BarAction { WRITE, SAVE, SAVE_AS }

/** Источник загрузки в «Загрузить из…». */
internal enum class LoadSourceKind { MINE, CLOUD, FILE, DEFAULTS }

/** Куда сохранить в «Сохранить в…». */
internal enum class SaveTarget { MINE, FILE }

/** K9 «Что изменится»: сравнение идёт / готово / не удалось. [title] — что загружаем. */
internal sealed interface DiffState {
    val title: String

    data class Busy(override val title: String) : DiffState

    /** [pendingChanges] — незаписанные правки в редакторе (загрузка их заменит). */
    data class Ready(override val title: String, val diff: SettingsDiff, val pendingChanges: Int) : DiffState

    /** «Повторить» — то же сравнение: [from] с тем же [closeAfter] (сессию чужого редактора не закрываем). */
    data class Error(override val title: String, val from: SettingsSource, val closeAfter: Boolean) : DiffState
}

/**
 * Форма «Сохранить в мои конфигурации»: название 1–50 символов (обязательно), описание до 500 (необязательно), модель —
 * только для показа. [exitAfter] — выйти из редактора после сохранения.
 */
@Immutable
internal data class SaveForm(
    val name: String,
    val description: String,
    val deviceName: String,
    val exitAfter: Boolean = false
)

/** Шапка: модель и номер блока; [singleCanCode] — код единой CAN-прошивки, если она есть. */
internal data class SettingsHeader(val deviceName: String, val deviceId: Long, val singleCanCode: String?)

/** Открытое окно редактирования: [original] — значение «как было» (до правок), [draft] — набираемое. */
internal data class EditingSetting(
    val setting: Setting,
    val original: SettingValue,
    val draft: SettingValue,
    // Поиск автомобиля по CAN-коду в окне CAN-кода; пустой Content — для кода автомобиля нет.
    val carSearch: LCEState<List<CarSummary>, SettingsError> = LCEState.None
)

internal sealed interface SettingsDialog {
    data class Description(val node: SettingNode) : SettingsDialog

    /** Выход с незаписанными изменениями — подтверждение; [target] — куда после выхода. */
    data class ExitConfirm(val target: ExitTarget) : SettingsDialog

    /** CAN-код на блоке с единой CAN-прошивкой меняется прошивкой CAN — предложение перейти к обновлению. */
    data object SingleCanFirmware : SettingsDialog
    data object WriteError : SettingsDialog
    data object ResetConfirm : SettingsDialog
    data object ResetError : SettingsDialog

    /** «Загрузить из…» — выбор источника. */
    data object LoadSources : SettingsDialog

    /** «Сохранить в…» — «Мои конфигурации» / файл; [exitAfter] — выйти после сохранения. */
    data class SaveTargets(val exitAfter: Boolean) : SettingsDialog

    /**
     * Модель конфигурации не совпадает с моделью блока: предупреждение перед сравнением ([forWrite] — перед «Записать в
     * блок» из конфигурации, иначе — перед загрузкой в редактор блока).
     */
    data class ModelMismatch(
        val source: SettingsSource,
        val sourceDeviceName: String,
        val deviceName: String,
        val forWrite: Boolean,
        /** Для загрузки: закрыть ли сессию источника после сравнения (см. [DiffState.Error.closeAfter]). */
        val closeAfter: Boolean = true
    ) : SettingsDialog

    data class LoadError(val kind: LoadErrorKind) : SettingsDialog

    /** Не удалось сохранить; [retry] — что повторить. */
    data class SaveError(val retry: SaveRetry) : SettingsDialog

    /** «Сохранить» в файл перезапишет его. */
    data class OverwriteConfirm(val exitAfter: Boolean) : SettingsDialog

    /** Выход из конфигурации с несохранёнными правками: сохранить / не сохранять / отмена. */
    data object ExitSave : SettingsDialog
}

internal enum class LoadErrorKind {
    /** Другой набор настроек — не подходит для этого блока. */
    MISMATCH,

    /** Файл не читается или это не файл настроек. */
    FILE,

    /** Не удалось получить конфигурацию (сеть, сервер). */
    OTHER
}

internal sealed interface SaveRetry {
    data class Mine(val form: SaveForm) : SaveRetry
    data object File : SaveRetry
    data class Source(val exitAfter: Boolean) : SaveRetry
}

/** One-shot события экрана. */
internal sealed interface DeviceSettingsEffect {
    data class Written(val count: Int) : DeviceSettingsEffect
    data object SetValueFailed : DeviceSettingsEffect
    data object ResetDone : DeviceSettingsEffect
    data class Exit(val target: ExitTarget) : DeviceSettingsEffect

    /** Конфигурация подставлена в редактор: [count] изменений (0 — настройки уже совпадали). */
    data class Loaded(val count: Int) : DeviceSettingsEffect

    /** Открыть системный выбор файла настроек. */
    data object PickFile : DeviceSettingsEffect

    /** Отдать готовый файл [path] пользователю через системное сохранение под именем [fileName]. */
    data class ExportFile(val path: String, val fileName: String) : DeviceSettingsEffect

    data class FileSaved(val name: String) : DeviceSettingsEffect

    /** Сохранено в «Мои конфигурации» — тост с «Открыть». */
    data class SavedToMine(val configuration: SettingsSource.LocalConfiguration) : DeviceSettingsEffect

    /** Правки сохранены в источник (конфигурацию, файл). */
    data object Saved : DeviceSettingsEffect

    /** «Записать в блок»: открыть редактор блока с загрузкой [source]. */
    data class WriteToDevice(val source: SettingsSource) : DeviceSettingsEffect
}

/** Куда уходим из настроек: назад (корень монтажа) или в «Проверку прошивок» (единая CAN-прошивка). */
internal enum class ExitTarget { BACK, FIRMWARE_UPDATE }

/** Где пользователь в меню: путь разделов от корня (пусто — корень) или поиск. */
@Immutable
internal data class SettingsLocation(val sectionPath: List<Int>, val isSearch: Boolean) {
    val sectionId: Int? get() = sectionPath.lastOrNull()
}

/**
 * Пометки строк: изменённые настройки, счётчики разделов, автомобили CAN-кодов. Отдельно от state экрана — строки
 * не перерисовываются от ввода, диалогов и прочих полей, которые их не касаются.
 */
@Immutable
internal data class RowMarks(
    val changedIds: Set<Int>,
    val sectionChanges: Map<Int, Int>,
    val cars: Map<Int, CarSummary>
)
