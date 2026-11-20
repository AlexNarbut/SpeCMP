package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.barcode.BarcodeScanner
import ru.alarmtrade.specmp.core.barcode.digitsOf
import ru.alarmtrade.specmp.core.common.device.DeviceIdQueryError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_format_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_a11y_scan
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_btn_find
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_btn_next
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_error_empty
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_error_models
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_error_not_digits
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_error_too_long
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_item_other
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_label_id
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_label_model
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_msg_intro
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_msg_manual
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_placeholder_custom
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_placeholder_model
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_subtitle_step
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_title_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_title_not_found
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_title_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_title_question
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_title_screen
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpDropdown
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags

private val ContentMaxWidth = 560.dp
private val ResultIconBox = 48.dp

/** «Новый паспорт без блока · Шаг 1 из 2»: ID блока (ввод или штрих-код) → модель → «Далее». */
@Composable
internal fun PassportLookupScreen(
    state: PassportLookupState,
    actions: PassportLookupActions,
    snackbar: CmpSnackbarHostState,
    scanAvailable: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    // Клавиатура поля ID иначе остаётся поверх камеры: первое «Назад» и первый тап уходят ей, а не сканеру.
    val onScan = if (scanAvailable) {
        {
            focusManager.clearFocus()
            keyboard?.hide()
            actions.scanner.onScan()
        }
    } else {
        null
    }
    Box(modifier = modifier.fillMaxSize().testTag(MountPassportTags.LOOKUP)) {
        val hidden = if (state.scanner != null) Modifier.clearAndSetSemantics { } else Modifier
        Box(modifier = hidden) { Content(state, actions, snackbar, onScan, focusRequester) }
        state.scanner?.let { access ->
            NavigationBackHandler(
                state = rememberNavigationEventState(NavigationEventInfo.None),
                onBackCompleted = actions.scanner.onClose,
            )
            BarcodeScanner(
                access = access,
                accept = ::digitsOf,
                onResult = actions.scanner.onScanned,
                onRequestAccess = actions.scanner.onAccessRetry,
                onOpenSettings = actions.scanner.onOpenSettings,
                onManualInput = actions.scanner.onManual,
                onClose = actions.scanner.onClose,
            )
        }
    }
}

@Composable
private fun Content(
    state: PassportLookupState,
    actions: PassportLookupActions,
    snackbar: CmpSnackbarHostState,
    onScan: (() -> Unit)?,
    focusRequester: FocusRequester,
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.mountpassport_lookup_title_screen),
                subtitle = stringResource(Strings.string.mountpassport_lookup_subtitle_step),
                navigationIcon = {
                    CmpIconButton(
                        onClick = actions.onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Strings.string.mountpassport_common_a11y_back),
                    )
                },
            )
        },
        bottomBar = { NextBar(state, actions.onNext) },
        snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().padding(
                    horizontal = CmpSpacing.space20,
                    vertical = CmpSpacing.space16,
                ),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            ) {
                Intro()
                IdRow(state, actions, onScan, focusRequester)
                when (val lookup = state.lookup) {
                    is UnitLookupStatus.Found -> FoundCard(lookup)

                    UnitLookupStatus.NotFound, UnitLookupStatus.Offline, is UnitLookupStatus.Failed ->
                        ManualModel(state, actions)

                    UnitLookupStatus.Idle, UnitLookupStatus.Searching -> Unit
                }
            }
        }
    }
}

@Composable
private fun Intro() {
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(text = stringResource(Strings.string.mountpassport_lookup_title_question), style = CmpTextStyle.Title2)
        CmpText(
            text = stringResource(Strings.string.mountpassport_lookup_msg_intro),
            style = CmpTextStyle.Paragraph1,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun IdRow(
    state: PassportLookupState,
    actions: PassportLookupActions,
    onScan: (() -> Unit)?,
    focusRequester: FocusRequester,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8), verticalAlignment = Alignment.Top) {
        CmpTextField(
            value = state.unitId,
            onValueChange = actions.onIdChange,
            label = stringResource(Strings.string.mountpassport_lookup_label_id),
            singleLine = true,
            isError = state.idError != null,
            supportingText = state.idError?.let { error ->
                { CmpText(text = error.text(), style = CmpTextStyle.Paragraph3) }
            },
            trailingIcon = onScan?.let { scan ->
                {
                    CmpIconButton(
                        onClick = scan,
                        icon = CmpIcons.QrCodeScanner,
                        contentDescription = stringResource(Strings.string.mountpassport_lookup_a11y_scan),
                        tint = CmpTheme.colors.brandPrimaryDefault,
                        modifier = Modifier.testTag(MountPassportTags.LOOKUP_SCAN),
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { actions.onFind() }),
            modifier = Modifier.weight(1f).focusRequester(focusRequester).testTag(MountPassportTags.LOOKUP_ID),
        )
        CmpButton(
            text = stringResource(Strings.string.mountpassport_lookup_btn_find),
            onClick = actions.onFind,
            loading = state.lookup == UnitLookupStatus.Searching,
            size = CmpButtonSize.XL,
            uppercase = false,
            modifier = Modifier.testTag(MountPassportTags.LOOKUP_FIND),
        )
    }
}

@Composable
private fun FoundCard(found: UnitLookupStatus.Found) {
    val colors = CmpTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceCard, RoundedCornerShape(CmpRadius.cardMedium))
            .padding(CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        Box(
            modifier = Modifier.size(
                ResultIconBox,
            ).background(colors.successHighlight, RoundedCornerShape(CmpRadius.cardSmall)),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(icon = CmpIcons.CheckCircle, contentDescription = null, tint = colors.successDefault)
        }
        Column(modifier = Modifier.weight(1f)) {
            CmpText(text = found.systemName, style = CmpTextStyle.Headline2)
            CmpText(
                text = stringResource(
                    Strings.string.mountpassport_common_format_unit,
                    found.systemName,
                    found.systemId,
                ),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun ManualModel(state: PassportLookupState, actions: PassportLookupActions) {
    val colors = CmpTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.warningHighlight, RoundedCornerShape(CmpRadius.cardSmall))
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpIcon(
                icon = if (state.lookup == UnitLookupStatus.Offline) CmpIcons.CloudOff else CmpIcons.HelpOutline,
                contentDescription = null,
                tint = colors.warningDefault,
                modifier = Modifier.size(CmpSize.iconM),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = stringResource(
                        when (state.lookup) {
                            UnitLookupStatus.Offline -> Strings.string.mountpassport_lookup_title_offline
                            is UnitLookupStatus.Failed -> Strings.string.mountpassport_lookup_title_failed
                            else -> Strings.string.mountpassport_lookup_title_not_found
                        },
                    ),
                    style = CmpTextStyle.Headline3,
                )
                CmpText(
                    text = stringResource(Strings.string.mountpassport_lookup_msg_manual),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
        }
        ModelChoice(state, actions)
    }
}

@Composable
private fun ModelChoice(state: PassportLookupState, actions: PassportLookupActions) {
    val other = stringResource(Strings.string.mountpassport_lookup_item_other)
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        when (val models = state.models) {
            LCEState.None, LCEState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CmpCircularProgressIndicator()
            }

            is LCEState.Error -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpText(
                    text = stringResource(Strings.string.mountpassport_lookup_error_models),
                    style = CmpTextStyle.Paragraph2,
                    color = CmpTheme.colors.errorDefault,
                    modifier = Modifier.weight(1f),
                )
                CmpButton(
                    text = stringResource(Strings.string.mountpassport_common_btn_retry),
                    onClick = actions.onModelsRetry,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                    uppercase = false,
                )
            }

            is LCEState.Content -> CmpDropdown(
                selectedValue = if (state.customModel) other else state.model,
                placeholder = stringResource(Strings.string.mountpassport_lookup_placeholder_model),
                options = models.content + other,
                onOptionSelect = { index, value ->
                    if (index == models.content.size) actions.onOtherModel() else actions.onModelSelect(value)
                },
                modifier = Modifier.fillMaxWidth().testTag(MountPassportTags.LOOKUP_MODEL),
            )
        }
        if (state.customModel || state.models is LCEState.Error) {
            CmpTextField(
                value = state.customText,
                onValueChange = { text ->
                    if (!state.customModel) actions.onOtherModel()
                    actions.onCustomModelChange(text)
                },
                label = stringResource(Strings.string.mountpassport_lookup_label_model),
                placeholder = stringResource(Strings.string.mountpassport_lookup_placeholder_custom),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(MountPassportTags.LOOKUP_CUSTOM),
            )
        }
    }
}

@Composable
private fun NextBar(state: PassportLookupState, onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CmpDivider()
        CmpButton(
            text = stringResource(Strings.string.mountpassport_lookup_btn_next),
            onClick = onNext,
            enabled = state.canProceed,
            loading = state.creating,
            icon = CmpIcons.ArrowForward,
            size = CmpButtonSize.XL,
            fullWidth = true,
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .padding(horizontal = CmpSpacing.space20, vertical = CmpSpacing.space12)
                .testTag(MountPassportTags.LOOKUP_NEXT),
        )
    }
}

@Composable
private fun DeviceIdQueryError.text(): String = stringResource(
    when (this) {
        DeviceIdQueryError.EMPTY -> Strings.string.mountpassport_lookup_error_empty
        DeviceIdQueryError.NOT_DIGITS -> Strings.string.mountpassport_lookup_error_not_digits
        DeviceIdQueryError.TOO_LONG -> Strings.string.mountpassport_lookup_error_too_long
    },
)
