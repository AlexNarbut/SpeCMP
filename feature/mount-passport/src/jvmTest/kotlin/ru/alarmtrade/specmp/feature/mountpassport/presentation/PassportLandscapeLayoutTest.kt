package ru.alarmtrade.specmp.feature.mountpassport.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.gallery.fastPassport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportSection
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.DocumentStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPdf
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPreviewPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportReadyState

@OptIn(ExperimentalTestApi::class)
class PassportLandscapeLayoutTest {

    private val passport = fastPassport()

    private val readyState = PassportReadyState(
        passportId = passport.id,
        canShare = true,
        passport = passport,
        document = DocumentStatus.Ready,
        pdf = PassportPdf(ByteArray(1), 1, "passport.pdf", Instant.fromEpochMilliseconds(0)),
    )

    private fun readyActions(onBack: () -> Unit = {}) =
        PassportReadyActions(onBack = onBack, onRetry = {}, onPageVisible = { _, _ -> })

    private fun landscape(content: @Composable () -> Unit, block: SkikoComposeUiTest.() -> Unit) =
        runSkikoComposeUiTest(size = Size(LANDSCAPE_WIDTH, LANDSCAPE_HEIGHT), density = Density(1f)) {
            setContent { AppTheme(onThemeChange = {}) { content() } }
            block()
        }

    @Test
    fun `low ready screen keeps both exports and back in top bar`() {
        var backs = 0
        landscape({ PassportReadyScreen(readyState, readyActions { backs++ }, CmpSnackbarHostState()) }) {
            val back = onNodeWithTag(MountPassportTags.READY_BACK).getBoundsInRoot()
            listOf(MountPassportTags.READY_EXPORT_PDF, MountPassportTags.READY_EXPORT_ZIP).forEach { tag ->
                val button = onNodeWithTag(tag).getBoundsInRoot()
                assertTrue(button.bottom <= back.bottom + 8.dp, "$tag $button back $back")
            }
            onNodeWithTag(MountPassportTags.READY_BACK).performClick()

            assertEquals(1, backs)
        }
    }

    @Test
    fun `ready back button has own tag and back description`() {
        val back = runBlocking { getString(Strings.string.mountpassport_common_a11y_back) }
        landscape({ PassportReadyScreen(readyState, readyActions(), CmpSnackbarHostState()) }) {
            onNodeWithTag(MountPassportTags.READY_BACK).assertContentDescriptionEquals(back)
        }
    }

    @Test
    fun `low preview pane has no document actions`() {
        landscape({ PassportPreviewPane(readyState, readyActions(), CmpSnackbarHostState()) }) {
            val buttons = onAllNodes(hasAnyAncestor(hasTestTag(MountPassportTags.PREVIEW_HEADER)) and hasClickAction())
            assertEquals(0, buttons.fetchSemanticsNodes().size)
        }
    }

    @Test
    fun `low editor screen keeps create pdf and shows sections open in state`() {
        val state = PassportEditorState(
            passportId = passport.id,
            passport = LCEState.Content(passport),
            form = passport.toForm(),
            openSections = persistentSetOf(PassportSection.VEHICLE, PassportSection.CLIENT),
        )
        landscape({ PassportEditorScreen(state, editorActions, CmpSnackbarHostState()) }) {
            onNodeWithTag(MountPassportTags.PDF).assertExists()
            onNodeWithTag(MountPassportTags.FIELD_PLATE, useUnmergedTree = true).assertExists()
            onNodeWithTag(MountPassportTags.FIELD_VIN, useUnmergedTree = true).assertExists()
            onNodeWithTag(MountPassportTags.FIELD_MILEAGE, useUnmergedTree = true).assertExists()
            onNodeWithTag(MountPassportTags.FIELD_CUSTOMER_PHONE, useUnmergedTree = true).assertExists()
        }
    }

    private val editorActions = editorActions()

    private companion object {
        const val LANDSCAPE_WIDTH = 780f
        const val LANDSCAPE_HEIGHT = 360f
    }
}
