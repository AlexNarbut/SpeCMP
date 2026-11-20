package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/** Архив — все паспорта и поиск; блок — паспорта подключённого блока [Unit.systemId]. */
@Immutable
internal sealed interface PassportListMode {
    data object Archive : PassportListMode

    data class Unit(val systemId: Long) : PassportListMode
}

/** Подключённый сейчас блок: ID — как в паспорте (строкой). */
@Immutable
internal data class ConnectedUnit(val systemId: String, val name: String)

/** [query] — запрос, по которому найдены [items] (подсветка, «Найдено», «Ничего не нашли»); пустой — весь список. */
@Immutable
internal data class PassportListContent(val query: String, val items: ImmutableList<MountPassportSummary>)

/** [selectedId] — выбранный паспорт широкого окна; [deleteCandidate] — открыт диалог удаления. */
@Immutable
internal data class PassportListState(
    val mode: PassportListMode,
    val query: String = "",
    val list: LCEState<PassportListContent, PassportError> = LCEState.Loading,
    val storage: PassportStorageUsage? = null,
    val connected: ConnectedUnit? = null,
    val selectedId: String? = null,
    val deleteCandidate: MountPassportSummary? = null,
) {
    val isArchive: Boolean get() = mode is PassportListMode.Archive

    /** «Дооформить» — только заготовка и только при подключённом том же блоке. */
    fun canFinish(item: MountPassportSummary): Boolean =
        item.status == PassportStatus.BLANK && connected?.systemId == item.systemId
}
