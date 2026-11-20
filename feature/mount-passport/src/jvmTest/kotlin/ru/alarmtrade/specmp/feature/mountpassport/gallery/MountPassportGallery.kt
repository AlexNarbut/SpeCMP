package ru.alarmtrade.specmp.feature.mountpassport.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportPhotoActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportSection
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.SaveStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.toForm
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.ConnectedUnit
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListActions
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListContent
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListMode
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListScreen
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.PassportListState

/** Состояния списка и предотчёта; PNG — в `build/state-gallery/mount-passport` (телефон, планшет, широкое окно). */
class MountPassportGallery {

    private val outDir = File("build/state-gallery/mount-passport")
    private val listActions = PassportListActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    private val photoActions = PassportPhotoActions({}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, {})
    private val editorActions =
        PassportEditorActions({}, {}, { _, _ -> }, {}, { _, _ -> }, {}, {}, {}, photoActions, {}, {})

    private fun editorState(passport: MountPassport, connected: String? = "1234567") = PassportEditorState(
        passportId = passport.id,
        passport = LCEState.Content(passport),
        form = passport.toForm(),
        connectedSystemId = connected,
    )

    private fun list(name: String, state: PassportListState) = StateGallery.render(outDir, name) {
        PassportListScreen(state, listActions, CmpSnackbarHostState(), detail = {
            PassportEditorScreen(editorState(fastPassport()), editorActions, CmpSnackbarHostState(), embedded = true)
        })
    }

    private fun editor(
        name: String,
        state: PassportEditorState,
        open: Set<PassportSection> = setOf(PassportSection.VEHICLE),
    ) = StateGallery.render(outDir, name) {
        PassportEditorScreen(state, editorActions, CmpSnackbarHostState(), initialOpen = open)
    }

    private fun content(
        query: String = "",
        items: List<MountPassportSummary> = rows,
    ): LCEState<PassportListContent, PassportError> =
        LCEState.Content(PassportListContent(query, items.toImmutableList()))

    @Test
    fun archive() = list(
        "01-archive",
        PassportListState(
            mode = PassportListMode.Archive,
            list = content(),
            storage = PassportStorageUsage(128, 340_000_000),
            connected = ConnectedUnit("107131", "Pandora DX-4GS"),
        ),
    )

    @Test
    fun archiveOffline() = list("02-archive-offline", PassportListState(PassportListMode.Archive, list = content()))

    @Test
    fun search() = list(
        "03-search",
        PassportListState(PassportListMode.Archive, query = "Ков", list = content("Ков", rows.take(1))),
    )

    @Test
    fun nothingFound() = list(
        "04-nothing-found",
        PassportListState(PassportListMode.Archive, query = "Х 999", list = content("Х 999", emptyList())),
    )

    @Test
    fun loading() = list("05-loading", PassportListState(PassportListMode.Archive))

    @Test
    fun emptyArchive() =
        list("06-empty-archive", PassportListState(PassportListMode.Archive, list = content(items = emptyList())))

    @Test
    fun emptyUnit() = list(
        "07-empty-unit",
        PassportListState(
            PassportListMode.Unit(107131),
            list = content(items = emptyList()),
            connected = ConnectedUnit("107131", "Pandora DX-4GS"),
        ),
    )

    @Test
    fun unitList() = list(
        "08-unit",
        PassportListState(
            PassportListMode.Unit(107131),
            list = content(items = rows.filter { it.systemId == "107131" }),
            connected = ConnectedUnit("107131", "Pandora DX-4GS"),
        ),
    )

    @Test
    fun listError() =
        list("09-error", PassportListState(PassportListMode.Archive, list = LCEState.Error(PassportError.Unknown)))

    @Test
    fun deleteDialog() = list(
        "10-delete",
        PassportListState(PassportListMode.Archive, list = content(), deleteCandidate = rows.first()),
    )

    @Test
    fun editorFast() = editor("20-editor-fast", editorState(fastPassport()))

    @Test
    fun editorFastDetails() = editor(
        "20b-editor-fast-details",
        editorState(fastPassport()),
        setOf(PassportSection.EQUIPMENT, PassportSection.CONFIGURATION, PassportSection.WORKS, PassportSection.CHECK),
    )

    @Test
    fun editorPhotosAndPeople() = editor(
        "20c-editor-photos",
        editorState(fastPassport()),
        setOf(PassportSection.DEVICES, PassportSection.PHOTOS, PassportSection.CLIENT, PassportSection.INSTALLER),
    )

    @Test
    fun editorPartial() = editor(
        "21-editor-partial",
        editorState(partialPassport()).copy(save = SaveStatus.PENDING),
        setOf(PassportSection.EQUIPMENT, PassportSection.CONFIGURATION, PassportSection.WORKS),
    )

    @Test
    fun editorPartialOffline() = editor(
        "22-editor-partial-offline",
        editorState(partialPassport(), connected = null),
        setOf(PassportSection.EQUIPMENT, PassportSection.DEVICES),
    )

    @Test
    fun editorWithoutUnit() = editor(
        "23-editor-no-unit",
        editorState(noUnitPassport()),
        setOf(PassportSection.EQUIPMENT, PassportSection.CONFIGURATION),
    )

    @Test
    fun editorInvalid() = editor(
        "24-editor-invalid",
        editorState(partialPassport()).let {
            it.copy(
                form = it.form.copy(vin = "XWEPH81ADN001234O", mileage = "12к", clientPhone = "7903"),
                save = SaveStatus.FAILED,
            )
        },
    )

    @Test
    fun editorPhotoError() = editor(
        "25-editor-photo-error",
        editorState(fastPassport()).copy(photoError = PassportError.StorageFull),
        setOf(PassportSection.PHOTOS),
    )

    @Test
    fun editorLoadError() = editor(
        "26-editor-error",
        PassportEditorState(passportId = "p1", passport = LCEState.Error(PassportError.Unknown)),
    )
}
