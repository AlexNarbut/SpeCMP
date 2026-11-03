package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/** Отношение версии в архиве к установленной на блоке: по нему метка и предупреждения. */
enum class ArchiveRowKind { New, Installed, Older }

/**
 * Ключ строки архива: номер версии **и** CAN, под который она собрана. У единой CAN-прошивки один номер (`3.61`)
 * выходит под разные CAN-коды и версии CAN-профиля — это разные файлы; версия без CAN — просто номер.
 */
internal fun archiveRowKey(version: String, canCode: String?, canVersion: String?): String =
    listOfNotNull(version, canCode, canVersion).joinToString("|")

/** Строка архива; идентификатор — [key] (номер версии + CAN). */
@Immutable
data class ArchiveRowUi(
    val version: String,
    val createdOn: LocalDate,
    val description: String,
    val kind: ArchiveRowKind,
    /** CAN-код версии (единая CAN-прошивка); `null` — версия без привязки к CAN. */
    val canCode: String? = null,
    val canVersion: String? = null,
    /** Версия собрана под CAN-код подключённого блока — подсветка «как в блоке». */
    val canMatchesDevice: Boolean = false,
    /** Под CAN-код блока, но профиль у блока другой (пояснение в панели версии). */
    val canProfileDiffers: Boolean = false
) {
    val key: String get() = archiveRowKey(version, canCode, canVersion)
}

/**
 * CAN-фильтр над списком: [input] — текст поля, [applied] — код, по которому загружен список (`null` — все версии),
 * [carTitle] — автомобиль, из которого взят код (выбор из каталога); [canPickVehicle] — каталог подключён.
 * [deviceCode] — CAN-код блока (`null` — блока нет или у него нет кода), [deviceProfile] — версия его CAN-профиля
 * для показа, [deviceFactory] — у блока заводские CAN-настройки.
 */
@Immutable
data class CanFilterUi(
    val input: String = "",
    val applied: String? = null,
    val carTitle: String? = null,
    val canPickVehicle: Boolean = false,
    val deviceCode: String? = null,
    val deviceProfile: String? = null,
    val deviceFactory: Boolean = false,
    /** Архив без блока: под пустым полем — «Выберите автомобиль…». */
    val noUnit: Boolean = false
) {
    /** В поле код блока — фильтр поставлен приложением («подставлен автоматически»). */
    val isDeviceCode: Boolean get() = deviceCode != null && input == deviceCode

    /** Поле ушло от кода блока — напоминание о нём и «Вернуть CAN блока». */
    val canResetToDevice: Boolean get() = deviceCode != null && input != deviceCode

    /** Набрано 1–3 цифры: подсказка и рамка ошибки. */
    val isPartial: Boolean get() = input.isNotEmpty() && input.length < CAN_CODE_DIGITS

    /** Список загружен под код блока (пустое состояние предлагает поискать под другим кодом). */
    val appliedIsDevice: Boolean get() = deviceCode != null && applied == deviceCode
}

/** Длина CAN-кода. */
internal const val CAN_CODE_DIGITS = 4

/** Файл версии сохранён: диалог «Прошивка скачана» — имя, где лежит, «Поделиться» / «Открыть папку». */
@Immutable
data class ArchiveSavedUi(val key: String, val fileName: String, val path: String, val folder: String?)

/** Не получилось скачать, сохранить или отдать файл (баннер над списком). */
sealed interface ArchiveExportError {
    data class Download(val error: FirmwareError) : ArchiveExportError

    data object Permission : ArchiveExportError

    data object Save : ArchiveExportError

    data object Share : ArchiveExportError
}

/** Диалог «Установить более старую версию?». */
@Immutable
data class RollbackUi(val version: String, val installedVersion: String)

@Immutable
data class FirmwareArchiveState(
    /** Пустой `Content` — сервер вернул пустой список; `isUpdating`/`updateError` — догрузка следующей страницы. */
    val content: LCEState<List<ArchiveRowUi>, FirmwareError> = LCEState.Loading,
    /** Ключ выбранной строки ([ArchiveRowUi.key], master-detail на широком окне). */
    val selected: String? = null,
    val rollback: RollbackUi? = null,
    val channel: FirmwareChannel = FirmwareChannel.RELEASE,
    val installedVersion: String? = null,
    val deviceTitle: String = "",
    val profile: UpdateProfile = UpdateProfile.Main,
    /** `null` — фильтра нет (Bluetooth-модуль). */
    val canFilter: CanFilterUi? = null,
    /** Просмотр без выбора версии для установки. */
    val viewOnly: Boolean = false,
    /** Открыт каталог автомобилей для CAN-фильтра. */
    val vehiclePicker: Boolean = false,
    /** Модель блока для каталога автомобилей (`null` — каталог с выбором типа устройства). */
    val deviceName: String? = null,
    /** Архив без блока (с главного экрана): система, подсистема и канал в шапке, «Скачать» / «Поделиться». */
    val noUnit: Boolean = false,
    /** Внутреннее (системное) имя прошивки подсистемы — мелко под её названием в шапке. */
    val moduleName: String? = null,
    /** Без блока: у подсистемы нет прошивок — «Выбрать другую подсистему». */
    val noModelFirmware: Boolean = false,
    /** Ключ версии, которая сейчас скачивается («Скачиваем…»). */
    val busy: String? = null,
    val saved: ArchiveSavedUi? = null,
    val exportError: ArchiveExportError? = null,
    val saveTarget: FirmwareSaveTarget = FirmwareSaveTarget.Downloads,
    val canShare: Boolean = false,
    val canOpenFolder: Boolean = false
)

/** Действия экрана архива (stateless-контент получает их от VM или заглушки в галерее состояний). */
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
    val onExportErrorDismiss: () -> Unit = {}
)

sealed interface FirmwareArchiveEffect {
    /** Выбрана версия для установки — результат архива. */
    data class Picked(val version: FirmwareVersion) : FirmwareArchiveEffect
}
