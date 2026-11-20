package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_pdf
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_zip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_update
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_a11y_actions
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_item_open
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_issued
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.gallery.fastPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents.PassportRow
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPdf
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

/** Единая схема кнопок паспорта на телефоне: предотчёт, «Паспорт оформлен», «⋯» строки архива. */
@OptIn(ExperimentalTestApi::class)
class PassportDocumentButtonsTest {

    private val passport = fastPassport()

    private fun editor(status: PassportStatus) = PassportEditorState(
        passportId = passport.id,
        passport = LCEState.Content(passport.copy(status = status)),
        form = passport.toForm(),
    )

    private val readyState = PassportReadyState(
        passportId = passport.id,
        canShare = true,
        passport = passport.copy(status = PassportStatus.ISSUED),
        document = DocumentStatus.Ready,
        pdf = PassportPdf(ByteArray(1), 1, "passport.pdf", Instant.fromEpochMilliseconds(0)),
    )

    private fun phone(content: @Composable () -> Unit, block: SkikoComposeUiTest.() -> Unit) =
        runSkikoComposeUiTest(size = Size(PHONE_WIDTH, PHONE_HEIGHT), density = Density(1f)) {
            setContent { AppTheme(onThemeChange = {}) { content() } }
            block()
        }

    private fun SkikoComposeUiTest.label(tag: String): String = onNodeWithTag(tag).fetchSemanticsNode()
        .config.getOrNull(SemanticsProperties.Text)?.joinToString().orEmpty()

    private fun text(resource: org.jetbrains.compose.resources.StringResource) = runBlocking { getString(resource) }

    @Test
    fun `draft editor has only generate pinned at bottom`() {
        phone({ PassportEditorScreen(editor(PassportStatus.DRAFT), editorActions(), CmpSnackbarHostState()) }) {
            assertTrue(label(MountPassportTags.PDF).equals(text(Strings.string.mountpassport_editor_btn_pdf), true))
            assertTrue(onNodeWithTag(MountPassportTags.PDF).getBoundsInRoot().bottom > (PHONE_HEIGHT / 2).dp)
            onNodeWithTag(MountPassportTags.EXPORT_PDF).assertDoesNotExist()
            onNodeWithTag(MountPassportTags.EXPORT_ZIP).assertDoesNotExist()
        }
    }

    @Test
    fun `issued editor has update and both exports at bottom`() {
        var created = 0
        var pdf = 0
        var zip = 0
        val actions = editorActions(onCreatePdf = { created++ }, onExportPdf = { pdf++ }, onExportZip = { zip++ })
        phone({ PassportEditorScreen(editor(PassportStatus.ISSUED), actions, CmpSnackbarHostState()) }) {
            assertTrue(label(MountPassportTags.PDF).equals(text(Strings.string.mountpassport_editor_btn_update), true))
            listOf(MountPassportTags.PDF, MountPassportTags.EXPORT_PDF, MountPassportTags.EXPORT_ZIP).forEach {
                assertTrue(onNodeWithTag(it).getBoundsInRoot().top > (PHONE_HEIGHT / 2).dp, it)
                onNodeWithTag(it).performClick()
            }
            assertEquals(listOf(1, 1, 1), listOf(created, pdf, zip))
        }
    }

    @Test
    fun `issued screen has both exports and back`() {
        var back = 0
        var pdf = 0
        var zip = 0
        val actions = PassportReadyActions(
            onBack = { back++ },
            onRetry = {},
            onPageVisible = { _, _ -> },
            onExportPdf = { pdf++ },
            onExportZip = { zip++ },
        )
        phone({ PassportReadyScreen(readyState, actions, CmpSnackbarHostState()) }) {
            onNodeWithText(text(Strings.string.mountpassport_ready_title_issued)).assertExists()
            onNodeWithTag(MountPassportTags.READY_EXPORT_PDF).performClick()
            onNodeWithTag(MountPassportTags.READY_EXPORT_ZIP).performClick()
            onNodeWithContentDescription(text(Strings.string.mountpassport_common_a11y_back)).performClick()
            assertEquals(listOf(1, 1, 1), listOf(pdf, zip, back))
        }
    }

    @Test
    fun `forming screen keeps exports disabled`() {
        val forming = readyState.copy(document = DocumentStatus.Forming, pdf = null)
        val actions = PassportReadyActions(onBack = {}, onRetry = {}, onPageVisible = { _, _ -> })
        phone({ PassportReadyScreen(forming, actions, CmpSnackbarHostState()) }) {
            onNodeWithTag(MountPassportTags.READY_EXPORT_PDF).assertIsNotEnabled()
            onNodeWithTag(MountPassportTags.READY_EXPORT_ZIP).assertIsNotEnabled()
        }
    }

    @Test
    fun `issued row menu opens and exports pdf and zip`() {
        var pdf = 0
        var zip = 0
        val item = summary(status = PassportStatus.ISSUED)
        phone({
            PassportRow(
                item = item,
                query = "",
                canFinish = false,
                selected = false,
                onOpen = {},
                onFinish = {},
                onDelete = {},
                onExport = { zip++ },
                onExportPdf = { pdf++ },
            )
        }) {
            onNodeWithContentDescription(text(Strings.string.mountpassport_list_a11y_actions)).performClick()
            onNodeWithText(text(Strings.string.mountpassport_list_item_open)).assertExists()
            onNodeWithText(text(Strings.string.mountpassport_common_btn_delete)).assertExists()
            onNodeWithText(text(Strings.string.common_btn_export_pdf)).performClick()
            onNodeWithContentDescription(text(Strings.string.mountpassport_list_a11y_actions)).performClick()
            onNodeWithText(text(Strings.string.common_btn_export_zip)).performClick()
            assertEquals(1 to 1, pdf to zip)
        }
    }

    @Test
    fun `draft row menu has only open and delete`() {
        phone({
            PassportRow(
                item = summary(status = PassportStatus.DRAFT),
                query = "",
                canFinish = false,
                selected = false,
                onOpen = {},
                onFinish = {},
                onDelete = {},
            )
        }) {
            onNodeWithContentDescription(text(Strings.string.mountpassport_list_a11y_actions)).performClick()
            onNodeWithText(text(Strings.string.mountpassport_list_item_open)).assertExists()
            onNodeWithText(text(Strings.string.mountpassport_common_btn_delete)).assertExists()
            onNodeWithText(text(Strings.string.common_btn_export_pdf)).assertDoesNotExist()
            onNodeWithText(text(Strings.string.common_btn_export_zip)).assertDoesNotExist()
        }
    }

    @Test
    fun `wide row menu has no open since tap selects`() {
        listOf(PassportStatus.ISSUED, PassportStatus.DRAFT, PassportStatus.BLANK).forEach { status ->
            phone({ menuRow(status, wide = true) }) {
                onNodeWithContentDescription(text(Strings.string.mountpassport_list_a11y_actions)).performClick()
                onNodeWithText(text(Strings.string.mountpassport_list_item_open)).assertDoesNotExist()
                onNodeWithText(text(Strings.string.mountpassport_common_btn_delete)).assertExists()
            }
        }
    }

    @Test
    fun `narrow row menu keeps open`() {
        listOf(PassportStatus.ISSUED, PassportStatus.DRAFT, PassportStatus.BLANK).forEach { status ->
            phone({ menuRow(status, wide = false) }) {
                onNodeWithContentDescription(text(Strings.string.mountpassport_list_a11y_actions)).performClick()
                onNodeWithText(text(Strings.string.mountpassport_list_item_open)).assertExists()
            }
        }
    }

    @Composable
    private fun menuRow(status: PassportStatus, wide: Boolean) = PassportRow(
        item = summary(status = status),
        query = "",
        canFinish = true,
        selected = false,
        onOpen = {},
        onFinish = {},
        onDelete = {},
        wide = wide,
    )

    private companion object {
        const val PHONE_WIDTH = 360f
        const val PHONE_HEIGHT = 740f
    }
}
