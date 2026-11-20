package ru.alarmtrade.specmp.feature.mountpassport.gallery

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.dp
import java.io.File
import kotlin.test.Test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import ru.alarmtrade.specmp.core.barcode.BarcodeScannerAccess
import ru.alarmtrade.specmp.core.common.device.DeviceIdQueryError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportFastMountResult
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadPhase
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportSettingsSnapshot
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnavailableReason
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportPhotoActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupScanActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.PassportLookupState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup.UnitLookupStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadingState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPdf
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPreviewPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

/**
 * Считывание блока, «Паспорт готов» (и живой лист широкого окна), экран «Блок»; PNG — в
 * `build/state-gallery/mount-passport` (телефон, планшет, широкое окно; строки — светлая и тёмная тема).
 */
class MountPassportFlowGallery {

    private val outDir = File("build/state-gallery/mount-passport")
    private val readActions = PassportReadActions({}, {}, {}, {})
    private val readyActions = PassportReadyActions({}, {}, { _, _ -> }, {}, {}, {}, {}, {})
    private val lookupActions = PassportLookupActions(
        {}, {}, {}, {}, {}, {}, {}, {},
        PassportLookupScanActions({}, {}, {}, {}, {}, {}),
    )
    private val photoActions = PassportPhotoActions({}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {}, {}, {}, {})
    private val editorActions =
        PassportEditorActions({}, {}, { _, _ -> }, {}, { _, _ -> }, {}, {}, {}, photoActions, {}, {}, {})

    private val block = PassportStepStatus.Done(PassportStepSummary.Block("Pandora DX-91", "1234567", "3.57"))
    private val params = PassportStepStatus.Done(PassportStepSummary.Params("861234050118734", "2.8", "1.12"))
    private val devices = PassportStepStatus.Done(PassportStepSummary.Devices(5, PassportTransport.BLUETOOTH))
    private val settings = PassportStepStatus.Done(PassportStepSummary.Settings(48_000))
    private val ready = PassportStepStatus.Done(PassportStepSummary.Ready("p1", "20261119-234567-1"))

    private fun read(
        name: String,
        vararg statuses: PassportStepStatus,
        phase: PassportReadPhase = PassportReadPhase.Reading,
        transport: PassportTransport = PassportTransport.BLUETOOTH,
        source: PassportReadSource = PassportReadSource.AdvancedMount,
        seconds: Int = 0,
        failure: PassportError? = null,
    ) = StateGallery.render(outDir, name) {
        val state = PassportReadingState(
            source = source,
            read = PassportReadState(
                steps = PassportReadStep.entries.mapIndexed { index, step ->
                    PassportStepState(step, statuses.getOrElse(index) { PassportStepStatus.Waiting })
                },
                phase = phase,
                transport = transport,
                unitName = "Pandora DX-91",
            ),
            failure = failure,
            settingsSeconds = seconds,
        )
        PassportReadScreen(state, readActions)
    }

    @Test
    fun readRunning() = read("30-read-running", block, params, PassportStepStatus.Reading)

    @Test
    fun readUsb() = read(
        "31-read-usb",
        block,
        params,
        PassportStepStatus.Reading,
        transport = PassportTransport.USB,
    )

    @Test
    fun readSettingsLong() = read("32-read-settings", block, params, devices, PassportStepStatus.Reading, seconds = 24)

    @Test
    fun readPartFail() = read(
        "33-read-part-fail",
        block,
        PassportStepStatus.NotRead(connectionLost = false),
        PassportStepStatus.Reading,
    )

    @Test
    fun readSkipAndUnavailable() = read(
        "34-read-skip-na",
        block,
        params,
        devices,
        PassportStepStatus.Unavailable(PassportUnavailableReason.FACTORY_CAN),
        PassportStepStatus.Reading,
    )

    @Test
    fun readSkipped() =
        read("35-read-skipped", block, params, devices, PassportStepStatus.Skipped, PassportStepStatus.Reading)

    @Test
    fun readDone() = read("36-read-done", block, params, devices, settings, ready, phase = PassportReadPhase.Done("p1"))

    @Test
    fun readUnitFailed() = read(
        "37-read-unit-failed",
        PassportStepStatus.NotRead(connectionLost = false),
        phase = PassportReadPhase.UnitFailed(connectionLost = false),
    )

    @Test
    fun readLost() = read(
        "38-read-lost",
        block,
        params,
        PassportStepStatus.NotRead(connectionLost = true),
        phase = PassportReadPhase.ConnectionLost(PassportReadStep.DEVICES),
    )

    @Test
    fun readOfflineFast() = read(
        "39-read-offline-fast",
        phase = PassportReadPhase.Offline(canContinueWithout = true),
        source = PassportReadSource.FastMount(PassportFastMountResult()),
    )

    @Test
    fun readWrongUnit() = read(
        "40-read-wrong-unit",
        phase = PassportReadPhase.WrongUnit("1234567", "7654321"),
        source = PassportReadSource.Complete("p1"),
    )

    @Test
    fun readSaveFailed() =
        read("41-read-save-failed", block, params, devices, settings, phase = PassportReadPhase.SaveFailed)

    private fun readyState(
        document: DocumentStatus = DocumentStatus.Ready,
        canShare: Boolean = true,
        refreshing: Boolean = false,
        passport: MountPassport = withSettings(),
    ): PassportReadyState {
        val pdf = PassportPdf(ByteArray(1), 3, "Паспорт.pdf", AT).takeIf { document == DocumentStatus.Ready }
        return PassportReadyState(
            passportId = passport.id,
            canShare = canShare,
            passport = passport,
            document = document,
            pdf = pdf,
            refreshing = refreshing,
            pages = if (pdf != null) persistentMapOf(0 to page(), 1 to page()) else persistentMapOf(),
            pageWidthPx = PAGE_WIDTH,
        )
    }

    private fun readyScreen(
        name: String,
        state: PassportReadyState,
        windows: List<Pair<Int, Int>> = StateGallery.standardWindows,
    ) = StateGallery.render(outDir, name, windows) {
        PassportReadyScreen(state, readyActions, CmpSnackbarHostState())
    }

    @Test
    fun readyPhone() = readyScreen("50-ready", readyState())

    @Test
    fun readyDesktop() = readyScreen("51-ready-desktop", readyState(canShare = false))

    @Test
    fun readyForming() = readyScreen("52-ready-forming", readyState(document = DocumentStatus.Forming))

    @Test
    fun readyError() =
        readyScreen("53-ready-error", readyState(document = DocumentStatus.Failed(PassportError.Unknown)))

    @Test
    fun readyLowWindow() = readyScreen("54-ready-low", readyState(), windows = listOf(915 to 412, 1280 to 480))

    @Test
    fun workspace() = StateGallery.render(outDir, "55-workspace", listOf(1280 to 800, 1440 to 900, 960 to 480)) {
        val passport = withSettings()
        Row(modifier = Modifier.fillMaxSize()) {
            PassportEditorScreen(
                state = PassportEditorState(
                    passportId = passport.id,
                    passport = LCEState.Content(passport),
                    form = passport.toForm(),
                    connectedSystemId = "1234567",
                ),
                actions = editorActions,
                snackbar = CmpSnackbarHostState(),
                showPdf = false,
                modifier = Modifier.width(500.dp),
            )
            CmpVerticalDivider()
            PassportPreviewPane(readyState(refreshing = true), readyActions, CmpSnackbarHostState())
        }
    }

    @Test
    fun workspaceDesktopForming() = StateGallery.render(outDir, "56-workspace-desktop", listOf(1280 to 800)) {
        PassportPreviewPane(
            readyState(document = DocumentStatus.Forming, canShare = false),
            readyActions,
            CmpSnackbarHostState(),
        )
    }

    private fun lookup(name: String, state: PassportLookupState) = StateGallery.render(outDir, name) {
        PassportLookupScreen(state, lookupActions, CmpSnackbarHostState(), scanAvailable = true)
    }

    private val models: LCEState<ImmutableList<String>, PassportError> =
        LCEState.Content(persistentListOf("Pandora DX-4GS", "Pandora DX-91", "Pandora VX-4G"))

    @Test
    fun lookupInput() = lookup("60-lookup", PassportLookupState())

    @Test
    fun lookupError() =
        lookup("61-lookup-error", PassportLookupState(unitId = "12A4567", idError = DeviceIdQueryError.NOT_DIGITS))

    @Test
    fun lookupSearching() = lookup(
        "62-lookup-searching",
        PassportLookupState(unitId = "1234567", lookup = UnitLookupStatus.Searching, searchedId = "1234567"),
    )

    @Test
    fun lookupFound() = lookup(
        "63-lookup-found",
        PassportLookupState(
            unitId = "1234567",
            lookup = UnitLookupStatus.Found("Pandora DX-91", "1234567"),
            searchedId = "1234567",
        ),
    )

    @Test
    fun lookupNotFound() = lookup(
        "64-lookup-not-found",
        PassportLookupState(
            unitId = "7654321",
            lookup = UnitLookupStatus.NotFound,
            searchedId = "7654321",
            models = models,
            model = "Pandora DX-4GS",
        ),
    )

    @Test
    fun lookupOfflineCustom() = lookup(
        "65-lookup-offline-custom",
        PassportLookupState(
            unitId = "1234567",
            lookup = UnitLookupStatus.Offline,
            searchedId = "1234567",
            models = models,
            customModel = true,
            customText = "Pandora DX-90",
        ),
    )

    @Test
    fun lookupModelsError() = lookup(
        "66-lookup-models-error",
        PassportLookupState(
            unitId = "1234567",
            lookup = UnitLookupStatus.Offline,
            searchedId = "1234567",
            models = LCEState.Error(PassportError.Unknown),
        ),
    )

    @Test
    fun lookupScannerDenied() = lookup(
        "67-lookup-scanner-denied",
        PassportLookupState(scanner = BarcodeScannerAccess.BLOCKED),
    )

    private fun withSettings(): MountPassport =
        fastPassport().copy(settingsSnapshot = PassportSettingsSnapshot("Pandora DX-91", AT, canFactory = false))

    /** Лист-заглушка «как документ»: заголовок и строки — галерее PDF не нужен. */
    private fun page(): ImageBitmap {
        val height = (PAGE_WIDTH * A4_HEIGHT / A4_WIDTH).toInt()
        val bitmap = ImageBitmap(PAGE_WIDTH, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.White
        canvas.drawRect(Rect(0f, 0f, PAGE_WIDTH.toFloat(), height.toFloat()), paint)
        paint.color = Color(0xFF2C3639)
        canvas.drawRect(Rect(40f, 40f, 300f, 64f), paint)
        paint.color = Color(0xFFBBBBBB)
        var y = 110f
        repeat(LINES) { line ->
            val width = if (line % 6 == 0) 220f else PAGE_WIDTH - 80f
            canvas.drawRect(Rect(40f, y, 40f + width, y + 8f), paint)
            y += if (line % 6 == 5) 36f else 22f
        }
        canvas.drawLine(Offset(40f, height - 50f), Offset(PAGE_WIDTH - 40f, height - 50f), paint)
        return bitmap
    }

    private companion object {
        const val PAGE_WIDTH = 560
        const val A4_WIDTH = 210f
        const val A4_HEIGHT = 297f
        const val LINES = 30
    }
}
