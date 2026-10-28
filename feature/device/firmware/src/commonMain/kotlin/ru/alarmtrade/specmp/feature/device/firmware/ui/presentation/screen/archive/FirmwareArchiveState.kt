package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/** Отношение версии в архиве к установленной на блоке: по нему метка и предупреждения. */
enum class ArchiveRowKind { New, Installed, Older }

/** Строка архива; ключ — номер версии (он уникален в пределах модуля и канала). */
@Immutable
data class ArchiveRowUi(
    val version: String,
    val createdOn: LocalDate,
    val description: String,
    val kind: ArchiveRowKind,
    /** CAN-код версии (единая CAN-прошивка); `null` — версия без привязки к CAN. */
    val canCode: String? = null,
    val canVersion: String? = null
)

sealed interface ArchiveContent {
    data object Loading : ArchiveContent

    /** Первая страница не загрузилась: сбой связи или отказ сервера. */
    data class Error(val error: RequestError) : ArchiveContent

    /** Сервер вернул пустой список. */
    data object Empty : ArchiveContent

    /** [moreError] — догрузка не удалась: список остаётся, внизу баннер с повтором. */
    @Immutable
    data class Ready(
        val rows: List<ArchiveRowUi>,
        val loadingMore: Boolean = false,
        val moreError: RequestError? = null
    ) : ArchiveContent
}

/**
 * CAN-фильтр над списком: [input] — текст поля, [applied] — код, по которому загружен список (`null` — все версии),
 * [carTitle] — автомобиль, из которого взят код (выбор из каталога); [canPickVehicle] — каталог подключён.
 */
@Immutable
data class CanFilterUi(
    val input: String = "",
    val applied: String? = null,
    val carTitle: String? = null,
    val canPickVehicle: Boolean = false
)

/** Диалог «Установить более старую версию?». */
@Immutable
data class RollbackUi(val version: String, val installedVersion: String)

@Immutable
data class FirmwareArchiveState(
    val content: ArchiveContent = ArchiveContent.Loading,
    /** Выбранная строка (master-detail на широком окне). */
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
    val deviceName: String? = null
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
    val onVehiclePickerOpen: () -> Unit = {}
)

sealed interface FirmwareArchiveEffect {
    /** Выбрана версия для установки — результат архива. */
    data class Picked(val version: FirmwareVersion) : FirmwareArchiveEffect
}
