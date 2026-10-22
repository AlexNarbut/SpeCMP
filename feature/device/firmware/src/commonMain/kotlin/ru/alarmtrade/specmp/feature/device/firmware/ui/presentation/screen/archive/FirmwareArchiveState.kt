package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/** Отношение версии в архиве к установленной на блоке: по нему метка и предупреждения. */
enum class ArchiveRowKind { New, Installed, Older }

/** Строка архива; ключ — номер версии (он уникален в пределах модуля и канала). */
@Immutable
data class ArchiveRowUi(
    val version: String,
    val createdOn: LocalDate,
    val description: String,
    val kind: ArchiveRowKind
)

sealed interface ArchiveContent {
    data object Loading : ArchiveContent

    /** Первая страница не загрузилась (нет связи с сервером). */
    data object Error : ArchiveContent

    /** Сервер вернул пустой список. */
    data object Empty : ArchiveContent

    /** [moreError] — догрузка не удалась: список остаётся, внизу баннер с повтором. */
    @Immutable
    data class Ready(val rows: List<ArchiveRowUi>, val loadingMore: Boolean = false, val moreError: Boolean = false) :
        ArchiveContent
}

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
    val profile: UpdateProfile = UpdateProfile.Main
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
    val onRollbackDismiss: () -> Unit = {}
)

sealed interface FirmwareArchiveEffect {
    /** Версия выбрана (результат передан экрану обновления) — закрыть архив. */
    data object Close : FirmwareArchiveEffect
}
