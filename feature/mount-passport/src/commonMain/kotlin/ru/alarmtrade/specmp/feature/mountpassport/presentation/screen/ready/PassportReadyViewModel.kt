package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.files.BinaryFileExporter
import ru.alarmtrade.specmp.core.files.HtmlPdfRenderer
import ru.alarmtrade.specmp.core.files.PdfPageRasterizer
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportDocumentRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportDocumentLocalizer
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportPdfBuilder
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportPdfResult
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PdfExportResult
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.exportPdf

/**
 * Документ: HTML из SDK → PDF на платформе → листы картинками по мере прокрутки. PDF — в state до правки паспорта:
 * правка (новый `updatedAt` в индексе) пересобирает его после паузы [REBUILD_DEBOUNCE_MS]. Экран «Паспорт оформлен»
 * ставит `ISSUED` первым успешным формированием; живой лист — только по [onIssue].
 */
@Suppress("LongParameterList")
internal class PassportReadyViewModel(
    args: PassportReadyArgs,
    private val repository: MountPassportRepository,
    private val documents: PassportDocumentRepository,
    localizer: PassportDocumentLocalizer,
    renderer: HtmlPdfRenderer,
    private val rasterizer: PdfPageRasterizer,
    private val exporter: BinaryFileExporter,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<PassportReadyState, PassportReadyEffect>(
    PassportReadyState(passportId = args.passportId, canShare = exporter.canShare),
) {
    private val passportId = args.passportId
    private val preview = args.preview
    private val pdfBuilder = PassportPdfBuilder(documents, localizer, renderer, dispatchers.io)

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

    fun onExportPdf() {
        intent {
            fileAction(if (exporter.canShare) DocumentAction.SHARE else DocumentAction.SAVE) { pdf ->
                when (val result = exporter.exportPdf(pdf.fileName, pdf.bytes)) {
                    PdfExportResult.Done -> null
                    is PdfExportResult.Saved -> PassportReadyEffect.Saved(result.name)
                    PdfExportResult.SaveFailed -> PassportReadyEffect.SaveFailed
                    PdfExportResult.ShareFailed -> PassportReadyEffect.ShareFailed
                }
            }
        }
    }

    /** «Сформировать» / «Обновить» широкого окна: пересобрать документ, поставить «Оформлен», показать диалог. */
    fun onIssue() {
        intent {
            if (currentState.issue == DocumentStatus.Forming) return@intent
            reduce { copy(issue = DocumentStatus.Forming) }
            build(debounce = false)
        }
    }

    fun onIssueDismiss() {
        intent { if (currentState.issue != DocumentStatus.Forming) reduce { copy(issue = null) } }
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

    private fun build(debounce: Boolean) {
        launchReplacing(BUILD) {
            if (debounce) delay(REBUILD_DEBOUNCE_MS)
            reduce { if (pdf == null) copy(document = DocumentStatus.Forming) else copy(refreshing = true) }
            val issuing = currentState.issue == DocumentStatus.Forming
            when (val result = render(issuing)) {
                is BuildResult.Built -> {
                    reduce {
                        copy(
                            passport = result.passport,
                            pdf = result.pdf,
                            document = DocumentStatus.Ready,
                            refreshing = false,
                            pages = persistentMapOf(),
                            issue = if (issuing) DocumentStatus.Ready else issue,
                        )
                    }
                }

                is BuildResult.Unchanged -> reduce { copy(passport = result.passport, refreshing = false) }

                is BuildResult.Failed -> {
                    val shown = currentState.pdf != null
                    reduce {
                        val failed = if (shown) {
                            copy(refreshing = false)
                        } else {
                            copy(document = DocumentStatus.Failed(result.error))
                        }
                        if (issuing) failed.copy(issue = DocumentStatus.Failed(result.error)) else failed
                    }
                    if (shown && !issuing) postSideEffect(PassportReadyEffect.RefreshFailed(result.error))
                }
            }
        }
    }

    private suspend fun render(issuing: Boolean): BuildResult {
        val loaded = withContext(dispatchers.io) { repository.get(passportId) }
        val passport = (loaded as? Response.Success)?.value
            ?: return BuildResult.Failed(PassportError.from((loaded as Response.Error).exception))
        if (!issuing && passport.updatedAt == currentState.pdf?.builtFor) return BuildResult.Unchanged(passport)
        val built = when (val result = pdfBuilder.build(passport)) {
            is PassportPdfResult.Built -> result
            is PassportPdfResult.Failed -> return BuildResult.Failed(result.error)
        }
        val current = if ((preview && !issuing) || passport.status == PassportStatus.ISSUED) {
            passport
        } else {
            (withContext(dispatchers.io) { documents.markIssued(passportId) } as? Response.Success)?.value ?: passport
        }
        return BuildResult.Built(current, PassportPdf(built.bytes, built.pageCount, built.fileName, current.updatedAt))
    }

    private suspend fun fileAction(action: DocumentAction, block: suspend (PassportPdf) -> PassportReadyEffect?) =
        subIntent {
            val pdf = currentState.pdf ?: return@subIntent
            if (currentState.busy != null) return@subIntent
            reduce { copy(busy = action) }
            val effect = block(pdf)
            reduce { copy(busy = null) }
            effect?.let(::postSideEffect)
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
        const val REBUILD_DEBOUNCE_MS = 800L
    }
}
