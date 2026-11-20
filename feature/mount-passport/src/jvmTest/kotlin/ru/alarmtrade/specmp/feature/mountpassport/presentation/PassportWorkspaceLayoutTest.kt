package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_update
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_screen
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_hint_preview
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_label_pages
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_issued
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.gallery.fastPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportWorkspace
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPdf
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPreviewPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.IssuedDialog

@OptIn(ExperimentalTestApi::class)
class PassportWorkspaceLayoutTest {

    private val passport = fastPassport()

    private val editorState = PassportEditorState(
        passportId = passport.id,
        passport = LCEState.Content(passport),
        form = passport.toForm(),
    )

    private val readyState = PassportReadyState(
        passportId = passport.id,
        canShare = false,
        passport = passport,
        document = DocumentStatus.Ready,
        pdf = PassportPdf(ByteArray(1), 1, "passport.pdf", Instant.fromEpochMilliseconds(0)),
    )

    private val editorActions = editorActions()

    private val readyActions = PassportReadyActions(onBack = {}, onRetry = {}, onPageVisible = { _, _ -> })

    private fun workspace(
        width: Float = WIDTH,
        height: Float = HEIGHT,
        ready: PassportReadyState = readyState,
        editor: PassportEditorState = editorState,
        actions: PassportEditorActions = editorActions,
        block: SkikoComposeUiTest.() -> Unit,
    ) = runSkikoComposeUiTest(size = Size(width, height), density = Density(1f)) {
        setContent {
            AppTheme(onThemeChange = {}) {
                PassportWorkspace(editor, actions, CmpSnackbarHostState()) { modifier ->
                    PassportPreviewPane(ready, readyActions, CmpSnackbarHostState(), modifier)
                }
            }
        }
        block()
    }

    @Test
    fun `one top bar spans both panes`() {
        val title = runBlocking { getString(Strings.string.mountpassport_editor_title_screen) }
        workspace {
            val bar = onNodeWithTag(MountPassportTags.WORKSPACE_TOP_BAR).getBoundsInRoot()
            assertEquals(WIDTH.dp, bar.right - bar.left)
            assertEquals(1, onAllNodesWithText(title).fetchSemanticsNodes().size)
        }
    }

    @Test
    fun `top bar shows save status as text`() {
        val saved = runBlocking { getString(Strings.string.mountpassport_editor_status_saved) }
        workspace { onNodeWithText(saved).assertExists() }
    }

    @Test
    fun `top bar subtitle has date without time`() {
        workspace {
            val subtitle = onNodeWithText(passport.number, substring = true).fetchSemanticsNode()
                .config.getOrNull(SemanticsProperties.Text)?.joinToString().orEmpty()
            assertFalse(Regex("""\d{1,2}:\d{2}""").containsMatchIn(subtitle), subtitle)
        }
    }

    @Test
    fun `preview header notes live update instead of page count`() {
        val note = runBlocking { getString(Strings.string.mountpassport_ready_hint_preview) }
        val pages = runBlocking { getPluralString(Strings.plurals.mountpassport_ready_label_pages, 1, 1) }
        workspace {
            onNodeWithText(note).assertExists()
            assertEquals(0, onAllNodesWithText(pages).fetchSemanticsNodes().size)
        }
    }

    @Test
    fun `draft workspace has generate in top bar and no export`() {
        val generate = runBlocking { getString(Strings.string.mountpassport_editor_btn_pdf) }
        workspace {
            val bar = onNodeWithTag(MountPassportTags.WORKSPACE_TOP_BAR).getBoundsInRoot()
            val main = onNodeWithTag(MountPassportTags.PDF).getBoundsInRoot()
            assertTrue(main.top >= bar.top && main.bottom <= bar.bottom, "main $main bar $bar")
            assertTrue(label(MountPassportTags.PDF).equals(generate, ignoreCase = true))
            onNodeWithTag(MountPassportTags.EXPORT_PDF).assertDoesNotExist()
            onNodeWithTag(MountPassportTags.EXPORT_ZIP).assertDoesNotExist()
        }
    }

    @Test
    fun `issued workspace has update and both exports in top bar`() {
        val update = runBlocking { getString(Strings.string.mountpassport_editor_btn_update) }
        var created = 0
        var pdf = 0
        var zip = 0
        val actions = editorActions(onCreatePdf = { created++ }, onExportPdf = { pdf++ }, onExportZip = { zip++ })
        workspace(editor = issuedEditor, actions = actions) {
            val bar = onNodeWithTag(MountPassportTags.WORKSPACE_TOP_BAR).getBoundsInRoot()
            listOf(MountPassportTags.PDF, MountPassportTags.EXPORT_PDF, MountPassportTags.EXPORT_ZIP).forEach { tag ->
                val button = onNodeWithTag(tag).getBoundsInRoot()
                assertTrue(button.top >= bar.top && button.bottom <= bar.bottom, "$tag $button bar $bar")
            }
            assertTrue(label(MountPassportTags.PDF).equals(update, ignoreCase = true))
            onNodeWithTag(MountPassportTags.PDF).performClick()
            onNodeWithTag(MountPassportTags.EXPORT_PDF).performClick()
            onNodeWithTag(MountPassportTags.EXPORT_ZIP).performClick()
            assertEquals(listOf(1, 1, 1), listOf(created, pdf, zip))
        }
    }

    @Test
    fun `preview header has no document actions`() {
        workspace {
            val buttons = onAllNodes(hasAnyAncestor(hasTestTag(MountPassportTags.PREVIEW_HEADER)) and hasClickAction())
            assertEquals(0, buttons.fetchSemanticsNodes().size)
        }
    }

    @Test
    fun `issued dialog offers both exports`() {
        val title = runBlocking { getString(Strings.string.mountpassport_ready_title_issued) }
        var pdf = 0
        var zip = 0
        val actions = PassportReadyActions(
            onBack = {},
            onRetry = {},
            onPageVisible = { _, _ -> },
            onExportPdf = { pdf++ },
            onExportZip = { zip++ },
        )
        runSkikoComposeUiTest(size = Size(WIDTH, HEIGHT), density = Density(1f)) {
            setContent {
                AppTheme(onThemeChange = {}) { IssuedDialog(readyState.copy(issue = DocumentStatus.Ready), actions) }
            }
            onNodeWithText(title, substring = true).assertExists()
            onNodeWithText(passport.number, substring = true).assertExists()
            onNodeWithTag(MountPassportTags.READY_EXPORT_PDF).performClick()
            onNodeWithTag(MountPassportTags.READY_EXPORT_ZIP).performClick()
            assertEquals(1 to 1, pdf to zip)
        }
    }

    @Test
    fun `forming issued dialog keeps pdf export disabled`() {
        runSkikoComposeUiTest(size = Size(WIDTH, HEIGHT), density = Density(1f)) {
            setContent {
                AppTheme(onThemeChange = {}) {
                    IssuedDialog(readyState.copy(issue = DocumentStatus.Forming), readyActions)
                }
            }
            onNodeWithTag(MountPassportTags.ISSUED_DIALOG).assertExists()
            onNodeWithTag(MountPassportTags.READY_EXPORT_PDF).assertIsNotEnabled()
        }
    }

    private val issuedEditor = editorState.copy(
        passport = LCEState.Content(passport.copy(status = PassportStatus.ISSUED)),
    )

    private fun SkikoComposeUiTest.label(tag: String): String = onNodeWithTag(tag).fetchSemanticsNode()
        .config.getOrNull(SemanticsProperties.Text)?.joinToString().orEmpty()

    @Test
    fun `preview note is shown whole on wide desktop`() {
        val note = runBlocking { getString(Strings.string.mountpassport_ready_hint_preview) }
        workspace(width = WIDE_WIDTH, height = WIDE_HEIGHT) {
            val layouts = mutableListOf<TextLayoutResult>()
            onNodeWithText(note).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertFalse(layouts.single().multiParagraph.didExceedMaxLines)
        }
    }

    @Test
    fun `forming sheet stays below preview header`() {
        workspace(ready = readyState.copy(document = DocumentStatus.Forming, pdf = null)) {
            val header = onNodeWithTag(MountPassportTags.PREVIEW_HEADER).getBoundsInRoot()
            val sheet = onNodeWithTag(MountPassportTags.FORMING_SHEET).getBoundsInRoot()
            assertTrue(sheet.top >= header.bottom, "sheet ${sheet.top} header ${header.bottom}")
        }
    }

    private companion object {
        const val WIDTH = 1280f
        const val HEIGHT = 800f
        const val WIDE_WIDTH = 1440f
        const val WIDE_HEIGHT = 900f
    }
}
