package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.BinaryFileExporter
import ru.alarmtrade.specmp.core.files.BinaryFilePickResult
import ru.alarmtrade.specmp.core.files.BinaryFilePicker
import ru.alarmtrade.specmp.core.files.BinaryFileSaveResult
import ru.alarmtrade.specmp.core.files.BinaryFileType
import ru.alarmtrade.specmp.core.files.HtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.core.files.shrinkJpeg
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportArchive
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportExportOptions
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportDecision
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportImportInspection
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportNotArchiveException
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportPdfSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportThumbnailer
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportTransferRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportDocumentLocalizer
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportPdfBuilder
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportPdfResult
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.vehicleTitle

/**
 * Перенос архивом: экспорт (один / выбранные / все) — состав, сборка ZIP в SDK с PDF каждого паспорта (как в
 * «Паспорт готов»), затем «Поделиться» / «Сохранить файл»; импорт — файл → сводка → решение о дубликатах → итог.
 * Собранный архив живёт до закрытия листа: отменённое сохранение не собирает его заново.
 */
@Suppress("LongParameterList", "TooManyFunctions")
internal class PassportTransferViewModel(
    private val repository: MountPassportRepository,
    private val transfer: PassportTransferRepository,
    documents: PassportDocumentRepository,
    localizer: PassportDocumentLocalizer,
    renderer: HtmlPdfRenderer,
    private val exporter: BinaryFileExporter,
    private val filePicker: BinaryFilePicker,
    imagePicker: ImagePicker,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<PassportTransferState, PassportTransferEffect>(PassportTransferState(canShare = exporter.canShare)) {

    private val pdfBuilder = PassportPdfBuilder(documents, localizer, renderer, dispatchers.io)
    private val thumbnailer = PassportThumbnailer { jpeg -> imagePicker.shrinkJpeg(jpeg, THUMBNAIL_SIDE) ?: jpeg }

    fun onExport(ids: List<String>) {
        if (ids.isEmpty()) return
        launchReplacing(SHEET) { openExport(ids.distinct(), fullBackup = false) }
    }

    fun onExportAll() {
        launchReplacing(SHEET) {
            reduce { copy(sheet = TransferSheet.Export(persistentListOf(), fullBackup = true)) }
            val all = withContext(dispatchers.io) { repository.observeAll().first() }
            val ids = (all as? Response.Success)?.value.orEmpty()
                .filter { it.status != PassportStatus.BLANK }
                .map { it.id }
            if (ids.isEmpty()) reduce { copy(sheet = null) } else openExport(ids, fullBackup = true)
        }
    }

    fun onIncludeSettingsChange(include: Boolean) {
        intent { reduce { export { if (phase == ExportPhase.Form) copy(includeSettings = include) else this } } }
    }

    fun onIncludePhotosChange(include: Boolean) {
        intent { reduce { export { if (phase == ExportPhase.Form) copy(includePhotos = include) else this } } }
    }

    fun onShare() {
        if (exporter.canShare) deliver(ExportAction.SHARE)
    }

    fun onSave() {
        deliver(ExportAction.SAVE)
    }

    fun onRetry() {
        intent { reduce { export { if (phase is ExportPhase.Failed) copy(phase = ExportPhase.Form) else this } } }
    }

    fun onImport() {
        launchReplacing(SHEET) { reduce { copy(sheet = TransferSheet.Import()) } }
    }

    fun onPickFile() {
        intent {
            executeWithProceedLock {
                if ((currentState.sheet as? TransferSheet.Import)?.phase !=
                    ImportPhase.Pick
                ) {
                    return@executeWithProceedLock
                }
                when (val picked = filePicker.pick(BinaryFileType.Zip)) {
                    is BinaryFilePickResult.Picked -> launchReplacing(SHEET) { inspect(picked.name, picked.path) }
                    BinaryFilePickResult.Failed -> postSideEffect(PassportTransferEffect.PickFailed)
                    BinaryFilePickResult.Cancelled -> Unit
                }
            }
        }
    }

    fun onPickAnother() {
        intent {
            reduce {
                import {
                    when (phase) {
                        is ImportPhase.NotArchive, is ImportPhase.NothingToImport, is ImportPhase.Failed ->
                            copy(phase = ImportPhase.Pick)

                        else -> this
                    }
                }
            }
        }
    }

    fun onReplaceChange(replace: Boolean) {
        intent {
            reduce {
                import { (phase as? ImportPhase.Summary)?.let { copy(phase = it.copy(replace = replace)) } ?: this }
            }
        }
    }

    fun onImportConfirm() {
        intent {
            val summary = (currentState.sheet as? TransferSheet.Import)?.phase as? ImportPhase.Summary ?: return@intent
            if (summary.toImport == 0) return@intent
            launchReplacing(SHEET) { import(summary) }
        }
    }

    fun onClose() {
        intent {
            val busy = when (val sheet = currentState.sheet) {
                is TransferSheet.Export -> sheet.busy
                is TransferSheet.Import -> sheet.busy
                null -> return@intent
            }
            if (busy) return@intent
            cancelLaunched(SHEET)
            reduce { copy(sheet = null) }
        }
    }

    private suspend fun openExport(ids: List<String>, fullBackup: Boolean) = subIntent {
        reduce { copy(sheet = TransferSheet.Export(ids.toImmutableList(), fullBackup)) }
        val passports = withContext(dispatchers.io) { ids.map { id -> loadExportPassport(id) } }
        reduce { export { copy(passports = passports.toImmutableList(), phase = ExportPhase.Form) } }
    }

    private suspend fun loadExportPassport(id: String): ExportPassport = when (val loaded = repository.get(id)) {
        is Response.Success -> ExportPassport(
            id = id,
            carTitle = loaded.value.vehicleTitle(),
            plate = loaded.value.vehicle.plate?.takeIf { it.isNotBlank() },
            hasSettings = loaded.value.settingsSnapshot != null,
            photos = loaded.value.photos.size,
        )

        is Response.Error -> ExportPassport(id, carTitle = null, plate = null, hasSettings = false, photos = 0)
    }

    private fun deliver(action: ExportAction) {
        intent {
            val sheet = currentState.sheet as? TransferSheet.Export ?: return@intent
            val archive = when (val phase = sheet.phase) {
                ExportPhase.Form -> build(sheet) ?: return@intent
                is ExportPhase.Ready -> phase.archive.takeIf { phase.busy == null } ?: return@intent
                else -> return@intent
            }
            reduce { export { copy(phase = ExportPhase.Ready(archive, busy = action)) } }
            val effect = send(action, archive)
            val delivered = effect is PassportTransferEffect.Shared || effect is PassportTransferEffect.Saved
            reduce { if (delivered) copy(sheet = null) else export { copy(phase = ExportPhase.Ready(archive)) } }
            effect?.let(::postSideEffect)
        }
    }

    private suspend fun send(action: ExportAction, archive: PassportArchive): PassportTransferEffect? = when (action) {
        ExportAction.SHARE -> if (exporter.shareFile(archive.fileName, BinaryFileType.Zip, archive.path)) {
            PassportTransferEffect.Shared
        } else {
            PassportTransferEffect.ShareFailed
        }

        ExportAction.SAVE -> when (val saved = exporter.saveFile(archive.fileName, BinaryFileType.Zip, archive.path)) {
            is BinaryFileSaveResult.Saved -> PassportTransferEffect.Saved(saved.name)
            BinaryFileSaveResult.Failed -> PassportTransferEffect.SaveFailed
            BinaryFileSaveResult.Cancelled -> null
        }
    }

    private suspend fun build(sheet: TransferSheet.Export): PassportArchive? = subIntent {
        val total = sheet.ids.size
        var done = 0
        reduce { export { copy(phase = ExportPhase.Packing(done = 0, total = total)) } }
        val pdf = PassportPdfSource { id ->
            pdfOf(id).also {
                done++
                reduce { export { copy(phase = ExportPhase.Packing(done, total)) } }
            }
        }
        val options = PassportExportOptions(sheet.includeSettings, sheet.includePhotos)
        when (val result = transfer.export(sheet.ids, options, sheet.fullBackup, pdf)) {
            is Response.Success -> result.value.also {
                postSideEffect(PassportTransferEffect.ArchiveBuilt(sheet.fullBackup))
            }

            is Response.Error -> {
                logger.w(result.exception) { "Passport export failed" }
                reduce { export { copy(phase = ExportPhase.Failed(PassportError.from(result.exception))) } }
                null
            }
        }
    }

    private suspend fun pdfOf(id: String): ByteArray {
        val passport = when (val loaded = withContext(dispatchers.io) { repository.get(id) }) {
            is Response.Success -> loaded.value
            is Response.Error -> throw loaded.exception
        }
        return when (val built = pdfBuilder.build(passport)) {
            is PassportPdfResult.Built -> built.bytes
            is PassportPdfResult.Failed -> throw PassportPdfException()
        }
    }

    private suspend fun inspect(fileName: String, path: String) = subIntent {
        reduce { import { copy(phase = ImportPhase.Reading(fileName)) } }
        val phase = when (val response = withContext(dispatchers.io) { transfer.inspect(path) }) {
            is Response.Error -> ImportPhase.Failed(fileName, PassportError.from(response.exception))

            is Response.Success -> when (val inspection = response.value) {
                PassportImportInspection.NotArchive -> ImportPhase.NotArchive(fileName)

                is PassportImportInspection.NothingToImport ->
                    ImportPhase.NothingToImport(fileName, inspection.problems)

                is PassportImportInspection.Ready -> ImportPhase.Summary(
                    fileName = fileName,
                    path = path,
                    items = inspection.items.toImmutableList(),
                    problems = inspection.problems.toImmutableList(),
                )
            }
        }
        reduce { import { copy(phase = phase) } }
    }

    private suspend fun import(summary: ImportPhase.Summary) = subIntent {
        reduce { import { copy(phase = ImportPhase.Importing(summary.fileName, summary.toImport)) } }
        val decision = if (summary.replace) PassportImportDecision.REPLACE else PassportImportDecision.SKIP
        val decisions = summary.items.filter { it.exists }.associate { it.key to decision }
        val phase = when (val result = transfer.import(summary.path, decisions, thumbnailer)) {
            is Response.Success -> ImportPhase.Done(result.value)

            is Response.Error -> if (result.exception is PassportNotArchiveException) {
                ImportPhase.NotArchive(summary.fileName)
            } else {
                ImportPhase.Failed(summary.fileName, PassportError.from(result.exception))
            }
        }
        reduce { import { copy(phase = phase) } }
    }

    private fun PassportTransferState.export(transform: TransferSheet.Export.() -> TransferSheet.Export) =
        (sheet as? TransferSheet.Export)?.let { copy(sheet = it.transform()) } ?: this

    private fun PassportTransferState.import(transform: TransferSheet.Import.() -> TransferSheet.Import) =
        (sheet as? TransferSheet.Import)?.let { copy(sheet = it.transform()) } ?: this

    private class PassportPdfException : Exception("Mount passport PDF was not rendered")

    private companion object {
        const val SHEET = "sheet"
        const val THUMBNAIL_SIDE = 320
    }
}
