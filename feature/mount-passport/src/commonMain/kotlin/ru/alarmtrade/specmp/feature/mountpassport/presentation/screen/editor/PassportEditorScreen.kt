package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_pdf
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_btn_read_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_load
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_intro_fast
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_intro_manual
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_intro_no_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_intro_tail
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_read_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_saving
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_subtitle_number
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_read_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_title_screen
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpActionBar
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportOrigin
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.numericDateTime
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.text
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.CheckBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.ClientBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.ConfigurationBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.DevicesBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.EditorSection
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.EquipmentBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.InstallerBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.Note
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.PhotoSourceSheet
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.PhotoViewer
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.PhotosBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.RemarksBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.VehicleBody
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.subcomponents.WorksBody

private val ContentMaxWidth = 760.dp
private val ActionMaxWidth = 560.dp
private val SpinnerSize = 20.dp

/**
 * Предотчёт: аккордеон секций в порядке документа, закреплённая «Сформировать PDF». [embedded] — правая часть
 * широкого окна списка: без своей шапки, номер и признак «Сохранено» — строкой над секциями.
 */
@Composable
internal fun PassportEditorScreen(
    state: PassportEditorState,
    actions: PassportEditorActions,
    snackbar: CmpSnackbarHostState,
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    initialOpen: Set<PassportSection> = setOf(PassportSection.VEHICLE),
) {
    val passport = (state.passport as? LCEState.Content)?.content
    if (embedded) {
        Box(modifier = modifier.fillMaxSize().testTag(MountPassportTags.EDITOR)) {
            Column(modifier = Modifier.fillMaxSize()) {
                passport?.let { EmbeddedHeader(it, state.save) }
                Box(modifier = Modifier.weight(1f)) { Body(state, actions, initialOpen) }
                if (passport != null) PdfBar(actions.onCreatePdf)
            }
            CmpSnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
        }
    } else {
        CmpScaffold(
            modifier = modifier.testTag(MountPassportTags.EDITOR),
            topBar = {
                CmpTopAppBar(
                    title = stringResource(Strings.string.mountpassport_editor_title_screen),
                    subtitle = passport?.let {
                        stringResource(
                            Strings.string.mountpassport_editor_subtitle_number,
                            it.number,
                            it.createdAt.numericDateTime(),
                        )
                    },
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.mountpassport_common_a11y_back),
                        )
                    },
                    actions = { if (passport != null) SaveIndicator(state.save) },
                )
            },
            bottomBar = { if (passport != null) PdfBar(actions.onCreatePdf) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) { Body(state, actions, initialOpen) }
        }
    }
    if (passport != null) Overlays(passport, state, actions)
}

@Composable
private fun Body(state: PassportEditorState, actions: PassportEditorActions, initialOpen: Set<PassportSection>) {
    when (val loaded = state.passport) {
        LCEState.None, LCEState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpCircularProgressIndicator()
        }

        is LCEState.Error -> CmpStateBox(
            icon = CmpIcons.ErrorOutline,
            title = stringResource(Strings.string.mountpassport_editor_error_load),
            text = loaded.error.text(),
            tone = CmpStateTone.Error,
        ) {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_common_btn_retry),
                onClick = actions.onRetry,
                icon = CmpIcons.Refresh,
                size = CmpButtonSize.M,
                modifier = Modifier.testTag(MountPassportTags.RETRY),
            )
        }

        is LCEState.Content -> Sections(loaded.content, state, actions, initialOpen)
    }
}

@Composable
private fun Sections(
    passport: MountPassport,
    state: PassportEditorState,
    actions: PassportEditorActions,
    initialOpen: Set<PassportSection>,
) {
    var open by rememberSaveable { mutableStateOf(initialOpen.map { it.name }.toSet()) }
    Box(
        modifier = Modifier.fillMaxSize().hideKeyboardOnScroll().verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().padding(
                start = CmpSpacing.space12,
                end = CmpSpacing.space12,
                bottom = CmpSpacing.screenPaddingBottom,
            ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            Intro(passport, state, actions.onReadUnit)
            passport.sections().forEach { section ->
                EditorSection(
                    section = section,
                    passport = passport,
                    state = state,
                    open = section.name in open,
                    onToggle = { open = if (section.name in open) open - section.name else open + section.name },
                ) { SectionBody(section, passport, state, actions) }
            }
        }
    }
}

@Composable
private fun SectionBody(
    section: PassportSection,
    passport: MountPassport,
    state: PassportEditorState,
    actions: PassportEditorActions,
) {
    when (section) {
        PassportSection.VEHICLE -> VehicleBody(passport, state, actions.onFieldChange)

        PassportSection.EQUIPMENT -> EquipmentBody(passport, state.rereadVisible, actions.onReread)

        PassportSection.CONFIGURATION -> ConfigurationBody(
            passport = passport,
            state = state,
            onFieldChange = actions.onFieldChange,
            onReread = actions.onReread,
            onSaveFile = actions.onSaveSettingsFile,
            onSaveConfigurations = actions.onSaveSettingsToConfigurations,
        )

        PassportSection.WORKS -> WorksBody(passport, state, actions.onWorkToggle)

        PassportSection.CHECK -> CheckBody(passport)

        PassportSection.DEVICES -> DevicesBody(passport, state.rereadVisible, actions.onReread)

        PassportSection.PHOTOS -> PhotosBody(passport, state, actions.photos)

        PassportSection.REMARKS -> RemarksBody(state.form.remarks, actions.onFieldChange)

        PassportSection.CLIENT -> ClientBody(state, actions.onFieldChange)

        PassportSection.INSTALLER -> InstallerBody(passport, state, actions.onFieldChange)
    }
}

@Composable
private fun Intro(passport: MountPassport, state: PassportEditorState, onReadUnit: () -> Unit) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    val intro = when {
        passport.withoutUnit -> Strings.string.mountpassport_editor_msg_intro_no_unit
        passport.origin == PassportOrigin.FAST_MOUNT -> Strings.string.mountpassport_editor_msg_intro_fast
        else -> Strings.string.mountpassport_editor_msg_intro_manual
    }
    Column(
        modifier = Modifier.padding(horizontal = CmpSpacing.space8, vertical = CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpText(
            text = stringResource(intro) + " " + stringResource(Strings.string.mountpassport_editor_msg_intro_tail),
            style = CmpTextStyle.Paragraph2,
            color = muted,
        )
        if (state.readUnitVisible) {
            CmpBanner(
                title = stringResource(Strings.string.mountpassport_editor_title_read_unit),
                text = stringResource(Strings.string.mountpassport_editor_msg_read_unit),
                tone = CmpBannerTone.Info,
                icon = CmpIcons.Bluetooth,
                actionLabel = stringResource(Strings.string.mountpassport_editor_btn_read_unit),
                onAction = onReadUnit,
                modifier = Modifier.testTag(MountPassportTags.READ_UNIT),
            )
        }
        if (!passport.withoutUnit && state.connectedSystemId != passport.unit.systemId) {
            Note(CmpIcons.Lock, stringResource(Strings.string.mountpassport_editor_msg_offline))
        }
    }
}

@Composable
private fun SaveIndicator(save: SaveStatus) {
    val colors = CmpTheme.colors
    Box(
        modifier = Modifier.padding(horizontal = CmpSpacing.space12).testTag(MountPassportTags.SAVE_STATUS),
        contentAlignment = Alignment.Center,
    ) {
        when (save) {
            SaveStatus.PENDING -> CmpCircularProgressIndicator(
                modifier = Modifier.size(SpinnerSize),
                strokeWidth = 2.dp,
            )

            SaveStatus.SAVED -> CmpIcon(
                icon = CmpIcons.CloudDone,
                contentDescription = stringResource(Strings.string.mountpassport_editor_status_saved),
                tint = colors.foregroundOnDarkSecondaryDefault,
            )

            SaveStatus.FAILED -> CmpIcon(
                icon = CmpIcons.CloudOff,
                contentDescription = stringResource(Strings.string.mountpassport_editor_status_failed),
                tint = colors.errorDefault,
            )
        }
    }
}

@Composable
private fun EmbeddedHeader(passport: MountPassport, save: SaveStatus) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = CmpSpacing.space20, top = CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(
                Strings.string.mountpassport_editor_subtitle_number,
                passport.number,
                passport.createdAt.numericDateTime(),
            ),
            style = CmpTextStyle.Headline3,
            modifier = Modifier.weight(1f),
        )
        CmpText(
            text = stringResource(
                when (save) {
                    SaveStatus.SAVED -> Strings.string.mountpassport_editor_status_saved
                    SaveStatus.PENDING -> Strings.string.mountpassport_editor_status_saving
                    SaveStatus.FAILED -> Strings.string.mountpassport_editor_status_failed
                },
            ),
            style = CmpTextStyle.Additional0,
            color = if (save == SaveStatus.FAILED) CmpTheme.colors.errorDefault else muted,
        )
        SaveIndicator(save)
    }
}

@Composable
private fun PdfBar(onCreatePdf: () -> Unit) {
    CmpActionBar {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            CmpButton(
                text = stringResource(Strings.string.mountpassport_editor_btn_pdf),
                onClick = onCreatePdf,
                icon = CmpIcons.PictureAsPdf,
                fullWidth = true,
                modifier = Modifier.widthIn(max = ActionMaxWidth).testTag(MountPassportTags.PDF),
            )
        }
    }
}

@Composable
private fun Overlays(passport: MountPassport, state: PassportEditorState, actions: PassportEditorActions) {
    state.photoSheet?.let { PhotoSourceSheet(it, actions.photos) }
    state.viewer?.let { index ->
        if (index in passport.photos.indices) {
            PhotoViewer(passport.photos, index, confirmDelete = state.photoToDelete != null, actions = actions.photos)
        }
    }
}
