package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportItem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportProblem
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportResult
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/** Лист (compact) / диалог (шире) переноса: [sheet] — `null`, пока ничего не открыто. */
@Immutable
internal data class PassportTransferState(
    val canShare: Boolean,
    val sheet: TransferSheet? = null,
    val pdfExporting: String? = null,
)

@Immutable
internal sealed interface TransferSheet {
    /** [fullBackup] — «Экспорт всех»: успешная сборка снимает напоминание о копии. */
    data class Export(
        val ids: ImmutableList<String>,
        val fullBackup: Boolean,
        val passports: ImmutableList<ExportPassport> = persistentListOf(),
        val includeSettings: Boolean = true,
        val includePhotos: Boolean = false,
        val phase: ExportPhase = ExportPhase.Loading,
    ) : TransferSheet {
        val withSettings: Int get() = passports.count { it.hasSettings }

        val photos: Int get() = passports.sumOf { it.photos }

        val busy: Boolean get() = phase is ExportPhase.Packing || (phase as? ExportPhase.Ready)?.busy != null
    }

    data class Import(val phase: ImportPhase = ImportPhase.Pick) : TransferSheet {
        val busy: Boolean get() = phase is ImportPhase.Reading || phase is ImportPhase.Importing
    }
}

/** Паспорт в составе экспорта: для подписей листа (авто, снимок настроек, число фото). */
@Immutable
internal data class ExportPassport(
    val id: String,
    val carTitle: String?,
    val plate: String?,
    val hasSettings: Boolean,
    val photos: Int,
)

@Immutable
internal sealed interface ExportPhase {
    data object Loading : ExportPhase

    data object Form : ExportPhase

    /** [done] — готово PDF из [total]. */
    data class Packing(val done: Int, val total: Int) : ExportPhase

    /** Архив собран; [busy] — идёт «Поделиться» / «Сохранить файл». */
    data class Ready(val archive: PassportArchive, val busy: ExportAction? = null) : ExportPhase

    data class Failed(val error: PassportError) : ExportPhase
}

internal enum class ExportAction { SHARE, SAVE }

@Immutable
internal sealed interface ImportPhase {
    data object Pick : ImportPhase

    data class Reading(val fileName: String) : ImportPhase

    data class NotArchive(val fileName: String) : ImportPhase

    data class NothingToImport(val fileName: String, val problems: List<PassportImportProblem>) : ImportPhase

    /** [replace] — паспорта, которые уже есть, взять из файла; по умолчанию пропустить. */
    data class Summary(
        val fileName: String,
        val path: String,
        val items: ImmutableList<PassportImportItem>,
        val problems: ImmutableList<PassportImportProblem>,
        val replace: Boolean = false,
    ) : ImportPhase {
        val existing: Int get() = items.count { it.exists }

        val toImport: Int get() = if (replace) items.size else items.size - existing
    }

    data class Importing(val fileName: String, val count: Int) : ImportPhase

    data class Done(val result: PassportImportResult) : ImportPhase

    data class Failed(val fileName: String, val error: PassportError) : ImportPhase
}
