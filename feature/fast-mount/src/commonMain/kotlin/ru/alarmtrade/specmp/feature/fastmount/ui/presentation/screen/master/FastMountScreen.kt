package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components.BannerTone
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
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStep
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStepFrame
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountStepNext
import ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation.MountVehicleRequest

internal const val FAST_MOUNT_WRONG_DEVICE_TAG = "fastmount_wrong_device"

private const val TOAST_MILLIS = 3_200L
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
    FastMountContent(
        state = state,
        prep = prep,
        master = master,
        slotContent = { screen -> SlotScreen(screen, state, slots, viewModel, checklist) },
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        onToastHide = viewModel::onToastShown,
        onExit = viewModel::onExit,
    )
}

internal enum class SlotScreen { FIRMWARE, VEHICLE, CONFIGURATION, BYPASS, CLONE, SYSTEM_CHECK }

internal fun FastMountState.slotScreen(): SlotScreen? = when (screen) {
    WizardScreen.FIRMWARE -> SlotScreen.FIRMWARE

    WizardScreen.VEHICLE -> SlotScreen.VEHICLE

    WizardScreen.CONFIGURATION -> SlotScreen.CONFIGURATION

    WizardScreen.MASTER -> when (master.screen) {
        MasterScreen.BYPASS -> SlotScreen.BYPASS
        MasterScreen.CLONE -> SlotScreen.CLONE
        MasterScreen.SYSTEM_CHECK -> SlotScreen.SYSTEM_CHECK
        else -> null
    }

    WizardScreen.CHECK, WizardScreen.SUMMARY -> null
}

@Composable
private fun SlotScreen(
    screen: SlotScreen,
    state: FastMountState,
    slots: FastMountSlots,
    viewModel: FastMountViewModel,
    checklist: MountChecklistActions,
) {
    val fixed = state.fixed ?: return
    when (screen) {
        SlotScreen.FIRMWARE -> slots.firmwareUpdate(viewModel::onFirmwareBack)

        SlotScreen.VEHICLE -> slots.vehiclePicker(
            MountVehicleRequest(fixed.systemName, state.unit?.canCode),
            viewModel::onCarPicked,
            viewModel::onBack,
        )

        SlotScreen.CONFIGURATION -> state.car?.let { car ->
            slots.configurationPicker(
                MountConfigurationRequest(fixed.systemName, state.unit?.canCode, car),
                viewModel::onConfigurationPicked,
                viewModel::onOpenFirmwareUpdate,
                viewModel::onBack,
            )
        }

        SlotScreen.BYPASS -> slots.bypass(mountStep(state, RailKey.BYPASS, viewModel), viewModel::onBypassNext)

        SlotScreen.CLONE -> slots.clone(mountStep(state, RailKey.CLONE, viewModel), viewModel::onCloneNext)

        SlotScreen.SYSTEM_CHECK -> slots.systemCheck(
            mountStep(state, RailKey.CHECK, viewModel),
            state.master.checklist,
            checklist,
        )
    }
}

@Composable
private fun mountStep(state: FastMountState, key: RailKey, viewModel: FastMountViewModel): MountStep {
    val title = stringResource(key.title())
    val subtitle = state.fixed?.title
    val frame = remember(title, subtitle) {
        MountStepFrame(
            topBar = { locked, onBack ->
                WizardTopBar(title, subtitle, if (locked) WizardNav.LOCK else WizardNav.BACK, onBack)
            },
            dock = { content -> DockFrame(content = content) },
        )
    }
    val master = state.master
    return MountStep(
        key = "fastmount-${key.name.lowercase()}-${master.stepAttempt}",
        frame = frame,
        next = MountStepNext(busy = master.busy, error = master.error?.takeIf { !master.busy }?.text()),
        onBack = viewModel::onStepScreenBack,
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
            MasterScreen.DONE -> DoneScreen(state, master)
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
    val colors = cmpThemeColors()
    val hide by rememberUpdatedState(onHide)
    LaunchedEffect(toast) {
        delay(TOAST_MILLIS)
        hide()
    }
    val text = stringResource(
        when (toast) {
            WizardToast.SETTINGS_WRITTEN -> Strings.string.fastmount_write_msg_done
            WizardToast.FILE_NO_APP -> Strings.string.fastmount_common_msg_file_no_app
            WizardToast.FILE_FAILED -> Strings.string.fastmount_common_msg_file_failed
        },
    )
    CmpBox(modifier = modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 100.dp)) {
        CmpBox(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(colors.onSurface, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            CmpText(text = text, color = colors.surface)
        }
    }
}

@Composable
private fun WrongDeviceOverlay(expected: String, connected: String, onExit: () -> Unit) {
    val colors = cmpThemeColors()
    NavigationBackHandler(state = rememberNavigationEventState(NavigationEventInfo.None), onBackCompleted = onExit)
    CmpSurface(modifier = Modifier.fillMaxSize().testTag(FAST_MOUNT_WRONG_DEVICE_TAG), color = colors.background) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Hero(
                title = stringResource(Strings.string.fastmount_wrong_title_screen),
                text = stringResource(Strings.string.fastmount_wrong_format_msg, connected, expected),
                art = { RoundArt(icon = CmpIcons.SyncProblem, tone = BannerTone.ERROR, size = 96.dp) },
            ) {
                CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnitRow(stringResource(Strings.string.fastmount_wrong_label_connected), connected, ok = false)
                    UnitRow(stringResource(Strings.string.fastmount_wrong_label_expected), expected, ok = true)
                }
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CmpCircularProgressIndicator(modifier = Modifier.widthIn(max = 18.dp), strokeWidth = 2.5.dp)
                    CmpText(
                        text = stringResource(Strings.string.fastmount_wrong_format_wait, expected),
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
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
    val colors = cmpThemeColors()
    val fg = if (ok) colors.onSurface else colors.onErrorContainer
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(if (ok) colors.surfaceContainerHigh else colors.errorContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(
            icon = if (ok) CmpIcons.CheckCircle else CmpIcons.Cancel,
            contentDescription = null,
            tint = if (ok) colors.primary else colors.onErrorContainer,
        )
        CmpColumn(modifier = Modifier.weight(1f)) {
            CmpText(text = label, style = CmpTextStyle.Body, color = if (ok) colors.onSurfaceVariant else fg)
            CmpText(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}
