package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_msg_file_failed
import ru.alarmtrade.specmp.core.localization.resources.fastmount_common_msg_file_no_app
import ru.alarmtrade.specmp.core.localization.resources.fastmount_write_msg_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_btn_disconnect
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_format_msg
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_format_wait
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_label_connected
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_label_expected
import ru.alarmtrade.specmp.core.localization.resources.fastmount_wrong_title_screen
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.navigation.ViewModelScope
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbar
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarData
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.HandsFreeVoice
import ru.alarmtrade.specmp.feature.device.presentation.core.voice.rememberHandsFreeVoice
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.DockFrame
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.Hero
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.LocalWizardWide
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.MainButton
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.PrepSub
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailCar
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailPosition
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RailStatus
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.RoundArt
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.StepRail
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardNav
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.WizardTopBar
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.railKey
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.railModel
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.railPath
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.railWidth
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.title
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.FastMountSlots
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountChecklistActions
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountConfigurationRequest
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountPassportSlot
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStep
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStepFrame
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStepNext
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountVehicleRequest

internal const val FAST_MOUNT_WRONG_DEVICE_TAG = "fastmount_wrong_device"

private const val TOAST_MILLIS = 3_200L
private val ToastBottom = 100.dp
private val ToastMaxWidth = 520.dp
private val WrongArtSize = 96.dp
private val UnitRowHeight = 64.dp
private val SpinnerStroke = 2.5.dp
private val WideFrom = 760.dp
private val LowHeight = 860.dp

@Composable
internal fun FastMountScreen(
    onExit: () -> Unit,
    onChooseAnother: () -> Unit,
    onOpenAdvancedMount: () -> Unit,
    slots: FastMountSlots,
) {
    val viewModel = koinViewModel<FastMountViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            FastMountEffect.Exit -> onExit()

            FastMountEffect.ChooseAnother -> onChooseAnother()

            FastMountEffect.AdvancedMount -> onOpenAdvancedMount()

            is FastMountEffect.OpenFile -> scope.launch {
                viewModel.onFileOpenResult(slots.fileOpener.open(effect.name, effect.bytes))
            }
        }
    }
    val prep = remember(viewModel) {
        PrepActions(
            onBack = viewModel::onBack,
            onRetryCheck = viewModel::onRetryCheck,
            onCheckNext = viewModel::onCheckNext,
            onOpenFirmwareUpdate = viewModel::onOpenFirmwareUpdate,
            onChooseAnother = viewModel::onChooseAnother,
            onOpenAdvancedMount = viewModel::onOpenAdvancedMount,
            onRetrySummary = viewModel::onRetrySummary,
            onStart = viewModel::onStartMount,
        )
    }
    val master = remember(viewModel) {
        MasterActions(
            onBack = viewModel::onBack,
            onStartRetry = viewModel::onStartMount,
            onWriteRetry = viewModel::onWriteRetry,
            onMountDone = viewModel::onMountDone,
            onMapOpen = viewModel::onMapOpen,
            onMapRetry = viewModel::onMapRetry,
            onSlaveDoneChange = viewModel::onSlaveDoneChange,
            onSlaveNext = viewModel::onSlaveNext,
            onRatingChange = viewModel::onRatingChange,
            onFeedbackChange = viewModel::onFeedbackChange,
            onFeedbackSend = viewModel::onFeedbackSend,
            onRetryStep = viewModel::onRetryStep,
            onHome = viewModel::onExit,
            onPassportStart = viewModel::onPassportStart,
        )
    }
    val checklist = remember(viewModel) {
        MountChecklistActions(
            onAnswer = viewModel::onTaskAnswer,
            onSkip = viewModel::onTaskSkip,
            onNext = viewModel::onSystemCheckNext,
            onBack = viewModel::onSystemCheckBack,
        )
    }
    val slotActions = remember(viewModel) {
        SlotActions(
            onFirmwareBack = viewModel::onFirmwareBack,
            onCarPicked = viewModel::onCarPicked,
            onConfigurationPicked = viewModel::onConfigurationPicked,
            onOpenFirmwareUpdate = viewModel::onOpenFirmwareUpdate,
            onBypassNext = viewModel::onBypassNext,
            onCloneNext = viewModel::onCloneNext,
            onStepBack = viewModel::onStepScreenBack,
            onBack = viewModel::onBack,
            onPassportRead = viewModel::onPassportRead,
            onPassportCancel = viewModel::onPassportCancel,
            onPassportClose = viewModel::onPassportClose,
        )
    }
    val voice = rememberHandsFreeVoice()
    FastMountContent(
        state = state,
        prep = prep,
        master = master,
        slotContent = { screen -> SlotScreen(screen, state, slots, slotActions, checklist, voice) },
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        onToastHide = viewModel::onToastShown,
        onExit = viewModel::onExit,
        passportExport = slots.passport.export,
    )
}

internal enum class SlotScreen { FIRMWARE, VEHICLE, CONFIGURATION, BYPASS, CLONE, SYSTEM_CHECK, PASSPORT }

internal class SlotActions(
    val onFirmwareBack: () -> Unit,
    val onCarPicked: (MountCar) -> Unit,
    val onConfigurationPicked: (configurationId: Int) -> Unit,
    val onOpenFirmwareUpdate: () -> Unit,
    val onBypassNext: (cloneRequired: Boolean) -> Unit,
    val onCloneNext: () -> Unit,
    val onStepBack: () -> Unit,
    val onBack: () -> Unit,
    val onPassportRead: (passportId: String) -> Unit,
    val onPassportCancel: () -> Unit,
    val onPassportClose: () -> Unit,
)

internal fun FastMountState.slotScreen(): SlotScreen? = when (screen) {
    WizardScreen.FIRMWARE -> SlotScreen.FIRMWARE

    WizardScreen.VEHICLE -> SlotScreen.VEHICLE

    WizardScreen.CONFIGURATION -> SlotScreen.CONFIGURATION

    WizardScreen.MASTER -> when (master.screen) {
        MasterScreen.BYPASS -> SlotScreen.BYPASS
        MasterScreen.CLONE -> SlotScreen.CLONE
        MasterScreen.SYSTEM_CHECK -> SlotScreen.SYSTEM_CHECK
        MasterScreen.DONE -> SlotScreen.PASSPORT.takeIf { master.passport.screen != null }
        else -> null
    }

    WizardScreen.CHECK, WizardScreen.SUMMARY -> null
}

@Composable
private fun SlotScreen(
    screen: SlotScreen,
    state: FastMountState,
    slots: FastMountSlots,
    actions: SlotActions,
    checklist: MountChecklistActions,
    voice: HandsFreeVoice,
) {
    val fixed = state.fixed ?: return
    when (screen) {
        SlotScreen.FIRMWARE -> slots.firmwareUpdate(actions.onFirmwareBack)

        SlotScreen.VEHICLE -> slots.vehiclePicker(
            MountVehicleRequest(fixed.systemName, state.unit?.canCode),
            actions.onCarPicked,
            actions.onBack,
        )

        SlotScreen.CONFIGURATION -> state.car?.let { car ->
            slots.configurationPicker(
                MountConfigurationRequest(fixed.systemName, state.unit?.canCode, car),
                actions.onConfigurationPicked,
                actions.onOpenFirmwareUpdate,
                actions.onBack,
            )
        }

        SlotScreen.BYPASS -> slots.bypass(mountStep(state, RailKey.BYPASS, actions, voice), actions.onBypassNext)

        SlotScreen.CLONE -> slots.clone(mountStep(state, RailKey.CLONE, actions, voice), actions.onCloneNext)

        SlotScreen.SYSTEM_CHECK -> slots.systemCheck(
            mountStep(state, RailKey.CHECK, actions, voice),
            state.master.checklist,
            checklist,
        )

        SlotScreen.PASSPORT -> PassportSlot(state.master, slots.passport, actions)
    }
}

/** Экраны паспорта в панели мастера; новое считывание ([PassportFlow.attempt]) — новые VM паспорта. */
@Composable
private fun PassportSlot(master: MasterState, slot: MountPassportSlot, actions: SlotActions) {
    val flow = master.passport
    ViewModelScope(name = PASSPORT_SCOPE, scopeKey = flow.attempt) {
        val passportId = flow.passportId
        when (flow.screen) {
            PassportScreen.READING -> master.result?.let { result ->
                slot.reading(result, actions.onPassportRead, actions.onPassportCancel)
            }

            PassportScreen.EDITOR -> if (passportId != null) slot.editor(passportId, actions.onPassportClose)

            null -> Unit
        }
    }
}

private const val PASSPORT_SCOPE = "fastmount-passport"

/** Шаги обхода, Clone и проверки системы — с кнопкой «Озвучка» в шапке мастера. */
@Composable
private fun mountStep(state: FastMountState, key: RailKey, actions: SlotActions, voice: HandsFreeVoice): MountStep {
    val title = stringResource(key.title())
    val subtitle = state.fixed?.title
    val frame = remember(title, subtitle, voice) {
        MountStepFrame(
            topBar = { locked, onBack ->
                WizardTopBar(title, subtitle, if (locked) WizardNav.LOCK else WizardNav.BACK, onBack, voice)
            },
            dock = { content -> DockFrame(content = content) },
        )
    }
    val master = state.master
    return MountStep(
        key = "fastmount-${key.name.lowercase()}-${master.stepAttempt}",
        frame = frame,
        next = MountStepNext(busy = master.busy, error = master.error?.takeIf { !master.busy }?.text()),
        onBack = actions.onStepBack,
    )
}

@Composable
internal fun FastMountContent(
    state: FastMountState,
    prep: PrepActions,
    master: MasterActions,
    slotContent: @Composable (SlotScreen) -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    onToastHide: () -> Unit,
    onExit: () -> Unit,
    passportExport: PassportExportSlot,
    rebootSecondsOverride: Int? = null,
) {
    val rebooting = state.screen == WizardScreen.MASTER && state.master.screen == MasterScreen.REBOOTING
    val rebootSeconds = rebootSecondsOverride ?: rememberRebootSeconds(rebooting)
    val position = state.railPosition()
    val model = railModel(railPath(state.plan, state.usb), position, state.master.cloneNeeded)
    val slot = state.slotScreen()
    if (slot == null && state.wrongDevice == null) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = if (state.screen == WizardScreen.CHECK) prep.onChooseAnother else prep.onBack,
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= WideFrom
        val railWidth = railWidth(maxWidth)
        val dense = maxHeight < LowHeight
        CompositionLocalProvider(LocalWizardWide provides wide) {
            CmpRow(modifier = Modifier.fillMaxSize()) {
                if (wide) {
                    StepRail(
                        model = model,
                        position = position,
                        width = railWidth,
                        rebootSeconds = rebootSeconds,
                        dense = dense,
                        car = state.railCar(),
                    )
                }
                CmpBox(modifier = Modifier.weight(1f).fillMaxSize()) {
                    if (slot != null) {
                        slotContent(slot)
                    } else {
                        OwnScreen(
                            state,
                            prep,
                            master,
                            model.rows.firstOrNull { it.status == RailStatus.NEXT }?.key,
                            rebootSeconds,
                            passportExport,
                        )
                    }
                    Toast(state.master.toast, onToastHide, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
        state.dialog?.let { WizardDialogView(it, onConfirm = onDialogConfirm, onDismiss = onDialogDismiss) }
        state.wrongDevice?.let { connected -> WrongDeviceOverlay(state.fixed?.title.orEmpty(), connected, onExit) }
    }
}

@Composable
private fun OwnScreen(
    state: FastMountState,
    prep: PrepActions,
    master: MasterActions,
    rebootNext: RailKey?,
    rebootSeconds: Int,
    passportExport: PassportExportSlot,
) {
    when (state.screen) {
        WizardScreen.CHECK -> UnitCheckScreen(state, prep)

        WizardScreen.SUMMARY -> SummaryScreen(state, prep)

        WizardScreen.MASTER -> when (state.master.screen) {
            MasterScreen.STARTING -> StartingScreen(state, master)
            MasterScreen.WRITING -> WritingScreen(state, master)
            MasterScreen.REBOOTING -> RebootScreen(state, rebootNext, rebootSeconds)
            MasterScreen.MOUNT -> MountScreen(state, master)
            MasterScreen.SLAVE -> SlaveScreen(state, master)
            MasterScreen.FEEDBACK -> FeedbackScreen(state, master)
            MasterScreen.DONE -> DoneScreen(state, master, passportExport)
            MasterScreen.BYPASS, MasterScreen.CLONE, MasterScreen.SYSTEM_CHECK -> Unit
        }

        WizardScreen.FIRMWARE, WizardScreen.VEHICLE, WizardScreen.CONFIGURATION -> Unit
    }
}

internal fun FastMountState.railPosition(): RailPosition = when (screen) {
    WizardScreen.CHECK, WizardScreen.FIRMWARE -> RailPosition(RailKey.PREP, PrepSub.UNIT)
    WizardScreen.VEHICLE -> RailPosition(RailKey.PREP, PrepSub.VEHICLE)
    WizardScreen.CONFIGURATION -> RailPosition(RailKey.PREP, PrepSub.CONFIGURATION)
    WizardScreen.SUMMARY -> RailPosition(RailKey.PREP, PrepSub.SUMMARY)
    WizardScreen.MASTER -> master.railPosition()
}

private fun MasterState.railPosition(): RailPosition = when (screen) {
    MasterScreen.STARTING, MasterScreen.WRITING -> RailPosition(RailKey.WRITE)

    MasterScreen.REBOOTING -> {
        val from = reboot?.from?.railKey() ?: step?.railKey() ?: RailKey.WRITE
        RailPosition(from, rebootFrom = from)
    }

    MasterScreen.MOUNT -> RailPosition(RailKey.MOUNT)

    MasterScreen.BYPASS -> RailPosition(RailKey.BYPASS)

    MasterScreen.CLONE -> RailPosition(RailKey.CLONE)

    MasterScreen.SLAVE -> RailPosition(RailKey.SLAVE)

    MasterScreen.SYSTEM_CHECK -> RailPosition(RailKey.CHECK)

    MasterScreen.FEEDBACK -> RailPosition(RailKey.REVIEW)

    MasterScreen.DONE -> RailPosition(null)
}

private fun FastMountState.railCar(): RailCar? {
    val car = car ?: return null
    val configuration = configuration ?: return null
    return RailCar(title = car.title, configuration = configuration.name, usb = usb)
}

@Composable
private fun Toast(toast: WizardToast?, onHide: () -> Unit, modifier: Modifier = Modifier) {
    toast ?: return
    val hide by rememberUpdatedState(onHide)
    LaunchedEffect(toast) {
        delay(TOAST_MILLIS)
        hide()
    }
    val (text, tone) = when (toast) {
        WizardToast.SETTINGS_WRITTEN -> Strings.string.fastmount_write_msg_done to CmpSnackbarTone.Success
        WizardToast.FILE_NO_APP -> Strings.string.fastmount_common_msg_file_no_app to CmpSnackbarTone.Error
        WizardToast.FILE_FAILED -> Strings.string.fastmount_common_msg_file_failed to CmpSnackbarTone.Error
    }
    CmpBox(modifier = modifier.fillMaxWidth().padding(bottom = ToastBottom), contentAlignment = Alignment.Center) {
        CmpSnackbar(
            data = CmpSnackbarData(message = stringResource(text), tone = tone),
            modifier = Modifier.widthIn(max = ToastMaxWidth),
        )
    }
}

@Composable
private fun WrongDeviceOverlay(expected: String, connected: String, onExit: () -> Unit) {
    NavigationBackHandler(state = rememberNavigationEventState(NavigationEventInfo.None), onBackCompleted = onExit)
    CmpSurface(
        modifier = Modifier.fillMaxSize().testTag(FAST_MOUNT_WRONG_DEVICE_TAG),
        color = CmpTheme.colors.backgroundPagePrimary,
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Hero(
                title = stringResource(Strings.string.fastmount_wrong_title_screen),
                text = stringResource(Strings.string.fastmount_wrong_format_msg, connected, expected),
                art = { RoundArt(icon = CmpIcons.SyncProblem, tone = CmpBannerTone.Error, size = WrongArtSize) },
            ) {
                CmpColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    UnitRow(stringResource(Strings.string.fastmount_wrong_label_connected), connected, ok = false)
                    UnitRow(stringResource(Strings.string.fastmount_wrong_label_expected), expected, ok = true)
                }
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                ) {
                    CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconS), strokeWidth = SpinnerStroke)
                    CmpText(
                        text = stringResource(Strings.string.fastmount_wrong_format_wait, expected),
                        style = CmpTextStyle.Paragraph2,
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                }
                MainButton(
                    text = stringResource(Strings.string.fastmount_wrong_btn_disconnect),
                    icon = CmpIcons.LinkOff,
                    outlined = true,
                    onClick = onExit,
                )
            }
        }
    }
}

@Composable
private fun UnitRow(label: String, title: String, ok: Boolean) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = UnitRowHeight)
            .background(
                if (ok) colors.backgroundSurfaceHighlight else colors.errorHighlight,
                RoundedCornerShape(CmpRadius.cardMedium),
            )
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(
            icon = if (ok) CmpIcons.CheckCircle else CmpIcons.Cancel,
            contentDescription = null,
            tint = if (ok) colors.successDefault else colors.errorDefault,
        )
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(text = label, style = CmpTextStyle.Paragraph3, color = colors.foregroundOnDarkSecondaryDefault)
            CmpText(text = title, style = CmpTextStyle.Headline3)
        }
    }
}
