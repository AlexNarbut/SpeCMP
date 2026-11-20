package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.core.files.BinaryFileExporter
import ru.alarmtrade.specmp.core.files.BinaryFileOpenResult
import ru.alarmtrade.specmp.core.files.BinaryFileSaveResult
import ru.alarmtrade.specmp.core.files.BinaryFileType
import ru.alarmtrade.specmp.core.files.HtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.PdfPageRasterizer
import ru.alarmtrade.specmp.core.files.PdfPageSetup
import ru.alarmtrade.specmp.core.files.PdfRenderResult
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportDocumentLocalizer
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.SettingsExport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.saveSettingsFile
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.saveSettingsToConfigurations

/**
 * Документ: HTML из SDK → PDF на платформе → листы картинками по мере прокрутки. PDF — в state до правки паспорта:
 * правка (новый `updatedAt` в индексе) пересобирает его после паузы [REBUILD_DEBOUNCE_MS]. Первое успешное
 * формирование ставит статус `ISSUED`.
 */
@Suppress("LongParameterList", "TooManyFunctions")
internal class PassportReadyViewModel(
    args: PassportReadyArgs,
    private val repository: MountPassportRepository,
    private val documents: PassportDocumentRepository,
    private val configurations: PassportConfigurationsRepository,
    private val localizer: PassportDocumentLocalizer,
    private val renderer: HtmlPdfRenderer,
    private val rasterizer: PdfPageRasterizer,
    private val exporter: BinaryFileExporter,
    private val settingsFileSaver: SettingsFileSaver,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<PassportReadyState, PassportReadyEffect>(
    PassportReadyState(passportId = args.passportId, canShare = exporter.canShare, shareOnReady = args.shareOnReady),
) {
    private val passportId = args.passportId

    override suspend fun IntentScope.onStart() {
        build(debounce = false)
        launchReplacing(CHANGES) {
            repository.observeAll()
                .map { response -> (response as? Response.Success)?.value?.firstOrNull { it.id == passportId } }
                .filterNotNull()
                .map { it.updatedAt }
                .distinctUntilChanged()
                .collect { updatedAt ->
                    val pdf = currentState.pdf ?: return@collect
                    if (updatedAt != pdf.builtFor) build(debounce = true)
                }
        }
    }

    fun onRetry() {
        build(debounce = false)
    }

    fun onPageVisible(index: Int, widthPx: Int) {
        intent {
            val pdf = currentState.pdf ?: return@intent
            if (widthPx <= 0 || index !in 0 until pdf.pageCount) return@intent
            if (currentState.pageWidthPx != widthPx) {
                reduce { copy(pageWidthPx = widthPx, pages = persistentMapOf()) }
            }
            if (index in currentState.pages) return@intent
            launchReplacing(PAGE + index) {
                val bitmap = rasterizer.render(pdf.bytes, index, widthPx) ?: return@launchReplacing
                reduce {
                    val same = this.pdf === pdf && pageWidthPx == widthPx
                    if (same) copy(pages = pages.put(index, bitmap)) else this
                }
            }
        }
    }

    fun onShare() {
        fileAction(DocumentAction.SHARE) { pdf ->
            if (!exporter.canShare) return@fileAction null
            if (exporter.share(pdf.fileName, BinaryFileType.Pdf, pdf.bytes)) null else PassportReadyEffect.ShareFailed
        }
    }

    fun onSave() {
        fileAction(DocumentAction.SAVE) { pdf ->
            when (val result = exporter.save(pdf.fileName, BinaryFileType.Pdf, pdf.bytes)) {
                is BinaryFileSaveResult.Saved -> PassportReadyEffect.Saved(result.name)
                BinaryFileSaveResult.Failed -> PassportReadyEffect.SaveFailed
                BinaryFileSaveResult.Cancelled -> null
            }
        }
    }

    fun onOpen() {
        fileAction(DocumentAction.OPEN) { pdf ->
            when (exporter.open(pdf.fileName, BinaryFileType.Pdf, pdf.bytes)) {
                BinaryFileOpenResult.Opened -> null
                BinaryFileOpenResult.NoApp -> PassportReadyEffect.NoPdfApp
                BinaryFileOpenResult.Failed -> PassportReadyEffect.OpenFailed
            }
        }
    }

    fun onEdit() {
        intent { postSideEffect(PassportReadyEffect.Edit) }
    }

    fun onSaveSettingsFile() {
        intent {
            val passport = currentState.passport ?: return@intent
            when (val result = passport.saveSettingsFile(repository, settingsFileSaver, dispatchers.io)) {
                is SettingsExport.Saved -> postSideEffect(PassportReadyEffect.SettingsFileSaved(result.name))
                is SettingsExport.Failed -> postSideEffect(PassportReadyEffect.SettingsFileFailed)
                SettingsExport.Cancelled -> Unit
            }
        }
    }

    fun onSaveSettingsToConfigurations() {
        intent {
            val passport = currentState.passport ?: return@intent
            when (val result = passport.saveSettingsToConfigurations(repository, configurations, dispatchers.io)) {
                is SettingsExport.Saved -> postSideEffect(PassportReadyEffect.ConfigurationSaved)

                is SettingsExport.Failed ->
                    postSideEffect(PassportReadyEffect.ConfigurationFailed(result.error ?: PassportError.Unknown))

                SettingsExport.Cancelled -> Unit
            }
        }
    }

    private fun build(debounce: Boolean) {
        launchReplacing(BUILD) {
            if (debounce) delay(REBUILD_DEBOUNCE_MS)
            reduce { if (pdf == null) copy(document = DocumentStatus.Forming) else copy(refreshing = true) }
            when (val result = render()) {
                is BuildResult.Built -> {
                    val share = currentState.shareOnReady
                    reduce {
                        copy(
                            passport = result.passport,
                            pdf = result.pdf,
                            document = DocumentStatus.Ready,
                            refreshing = false,
                            pages = persistentMapOf(),
                            shareOnReady = false,
                        )
                    }
                    if (share) onShare()
                }

                is BuildResult.Unchanged -> reduce { copy(passport = result.passport, refreshing = false) }

                is BuildResult.Failed -> {
                    val shown = currentState.pdf != null
                    reduce {
                        if (shown) copy(refreshing = false) else copy(document = DocumentStatus.Failed(result.error))
                    }
                    if (shown) postSideEffect(PassportReadyEffect.RefreshFailed(result.error))
                }
            }
        }
    }

    private suspend fun render(): BuildResult {
        val loaded = withContext(dispatchers.io) { repository.get(passportId) }
        val passport = (loaded as? Response.Success)?.value
            ?: return BuildResult.Failed(PassportError.from((loaded as Response.Error).exception))
        if (passport.updatedAt == currentState.pdf?.builtFor) return BuildResult.Unchanged(passport)
        val html = withContext(dispatchers.io) {
            documents.buildHtml(passportId, localizer.strings(), localizer.format())
        }
        if (html !is Response.Success) return BuildResult.Failed(PassportError.from((html as Response.Error).exception))
        val fileName = safeFileName(localizer.fileName(passport.number, passport.fileSubject()))
        val setup = PdfPageSetup(footer = localizer.footer(passport.number))
        val rendered = renderer.render(html.value, fileName.removeSuffix(PDF_SUFFIX), setup)
        if (rendered !is PdfRenderResult.Rendered) return BuildResult.Failed(PassportError.Unknown)
        val current = if (passport.status == PassportStatus.ISSUED) {
            passport
        } else {
            (withContext(dispatchers.io) { documents.markIssued(passportId) } as? Response.Success)?.value ?: passport
        }
        return BuildResult.Built(current, PassportPdf(rendered.bytes, rendered.pageCount, fileName, current.updatedAt))
    }

    private fun fileAction(action: DocumentAction, block: suspend (PassportPdf) -> PassportReadyEffect?) {
        intent {
            val pdf = currentState.pdf ?: return@intent
            if (currentState.busy != null) return@intent
            reduce { copy(busy = action) }
            val effect = block(pdf)
            reduce { copy(busy = null) }
            effect?.let(::postSideEffect)
        }
    }

    private sealed interface BuildResult {
        class Built(val passport: MountPassport, val pdf: PassportPdf) : BuildResult

        class Unchanged(val passport: MountPassport) : BuildResult

        class Failed(val error: PassportError) : BuildResult
    }

    private companion object {
        const val BUILD = "build"
        const val CHANGES = "changes"
        const val PAGE = "page-"
        const val PDF_SUFFIX = ".pdf"
        const val REBUILD_DEBOUNCE_MS = 800L
    }
}
