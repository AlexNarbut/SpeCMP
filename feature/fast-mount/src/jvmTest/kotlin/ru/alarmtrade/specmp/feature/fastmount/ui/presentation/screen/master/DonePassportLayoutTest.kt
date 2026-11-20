package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_pdf
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_zip
import ru.alarmtrade.specmp.core.localization.resources.fastmount_done_btn_passport
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPassport
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountPassportExport

/** Блок «Паспорт монтажа» финала: оформленный — «Экспорт PDF» / «Экспорт ZIP», как в предотчёте и архиве. */
@OptIn(ExperimentalTestApi::class)
class DonePassportLayoutTest {

    private val issued = FastMountPassport(id = "p1", number = "20261119-234567-1", issued = true)

    private fun screen(width: Float, content: @Composable () -> Unit, block: SkikoComposeUiTest.() -> Unit) =
        runSkikoComposeUiTest(size = Size(width, HEIGHT), density = Density(1f)) {
            setContent { AppTheme(onThemeChange = {}) { content() } }
            block()
        }

    private fun text(resource: StringResource) = runBlocking { getString(resource) }

    @Test
    fun issuedPassportOffersPdfAndZipExport() {
        var pdf = 0
        var zip = 0
        val export = MountPassportExport(pdfBusy = false, onPdf = { pdf++ }, onZip = { zip++ })
        screen(PHONE_WIDTH, { DonePassportBlock(issued, export, MasterActions()) }) {
            onNodeWithText(
                text(Strings.string.common_btn_export_pdf),
                ignoreCase = true,
                useUnmergedTree = true,
            ).assertExists()
            onNodeWithText(
                text(Strings.string.common_btn_export_zip),
                ignoreCase = true,
                useUnmergedTree = true,
            ).assertExists()
            onNodeWithTag(FAST_MOUNT_PASSPORT_EXPORT_PDF_TAG).performClick()
            onNodeWithTag(FAST_MOUNT_PASSPORT_EXPORT_ZIP_TAG).performClick()
            assertEquals(1, pdf)
            assertEquals(1, zip)
        }
    }

    @Test
    fun issuedPassportHasNoShareOrOpen() {
        val export = MountPassportExport(pdfBusy = false, onPdf = {}, onZip = {})
        screen(TABLET_WIDTH, { DonePassportBlock(issued, export, MasterActions()) }) {
            onAllNodesWithText("Поделиться", ignoreCase = true, useUnmergedTree = true).assertCountEquals(0)
            onAllNodesWithText("Открыть", ignoreCase = true, useUnmergedTree = true).assertCountEquals(0)
            onAllNodesWithText("Share", ignoreCase = true, useUnmergedTree = true).assertCountEquals(0)
            onAllNodesWithText("Open", ignoreCase = true, useUnmergedTree = true).assertCountEquals(0)
        }
    }

    @Test
    fun draftPassportOffersOnlyIssue() {
        var started = 0
        val draft = issued.copy(issued = false)
        screen(PHONE_WIDTH, { DonePassportBlock(draft, null, MasterActions(onPassportStart = { started++ })) }) {
            onAllNodesWithTag(FAST_MOUNT_PASSPORT_EXPORT_PDF_TAG).assertCountEquals(0)
            onAllNodesWithTag(FAST_MOUNT_PASSPORT_EXPORT_ZIP_TAG).assertCountEquals(0)
            onNodeWithText(
                text(Strings.string.fastmount_done_btn_passport),
                ignoreCase = true,
                useUnmergedTree = true,
            ).performClick()
            assertEquals(1, started)
        }
    }

    private companion object {
        const val PHONE_WIDTH = 360f
        const val TABLET_WIDTH = 900f
        const val HEIGHT = 640f
    }
}
