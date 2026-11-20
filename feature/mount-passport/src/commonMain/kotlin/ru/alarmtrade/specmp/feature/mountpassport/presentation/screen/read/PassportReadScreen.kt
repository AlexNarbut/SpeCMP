package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_btn_continue_without
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_btn_without_reading
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_keep_connected
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_lost
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_offline_master
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_save_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_unit_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_unit_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_unit_usb
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_msg_wrong_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_subtitle_advanced
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_subtitle_complete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_subtitle_fast
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_lost
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_offline
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_reading
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_save_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_screen
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_unit_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_title_wrong_unit
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadSource
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.text
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.subcomponents.ReadSteps

private val ContentMaxWidth = 640.dp
private val StopIconBox = 56.dp

/** «Считывание блока»: 5 этапов по порядку, остановки — с причиной и кнопками вместо «Отмена». */
@Composable
internal fun PassportReadScreen(
    state: PassportReadingState,
    actions: PassportReadActions,
    modifier: Modifier = Modifier,
) {
    CmpScaffold(
        modifier = modifier.testTag(MountPassportTags.READ),
        topBar = {
            Column {
                CmpTopAppBar(
                    title = stringResource(Strings.string.mountpassport_read_title_screen),
                    subtitle = stringResource(state.source.subtitle()),
                    navigationIcon = {
                        CmpIconButton(
                            onClick = actions.onCancel,
                            icon = CmpIcons.Close,
                            contentDescription = stringResource(Strings.string.mountpassport_common_btn_cancel),
                        )
                    },
                )
                CmpLinearProgressIndicator(progress = state.progress, modifier = Modifier.fillMaxWidth())
            }
        },
        bottomBar = { BottomButtons(state, actions) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().padding(CmpSpacing.space24),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
            ) {
                val stop = state.stop
                if (stop != null) StopHead(stop, state.source) else Head(state)
                ReadSteps(state, actions.onSkipSettings)
            }
        }
    }
}

@Composable
private fun Head(state: PassportReadingState) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(
                if (state.done) {
                    Strings.string.mountpassport_read_title_done
                } else {
                    Strings.string.mountpassport_read_title_reading
                },
            ),
            style = CmpTextStyle.Title2,
        )
        val unit = state.read.unitName
        val subtitle = when {
            state.done -> stringResource(Strings.string.mountpassport_read_msg_done)

            unit == null -> stringResource(Strings.string.mountpassport_read_msg_keep_connected)

            state.read.transport == PassportTransport.USB ->
                stringResource(Strings.string.mountpassport_read_msg_unit_usb, unit)

            else -> stringResource(Strings.string.mountpassport_read_msg_unit_bluetooth, unit)
        }
        CmpText(text = subtitle, style = CmpTextStyle.Paragraph2, color = muted)
    }
}

@Composable
private fun StopHead(stop: ReadStop, source: PassportReadSource) {
    val colors = CmpTheme.colors
    val error = stop is ReadStop.UnitFailed || stop is ReadStop.Failed || stop is ReadStop.WrongUnit
    val tone = if (error) colors.errorDefault else colors.warningDefault
    val background = if (error) colors.errorHighlight else colors.warningHighlight
    val (icon, title) = stop.iconAndTitle()
    val message = stop.message(source)
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        Box(
            modifier = Modifier.size(StopIconBox).background(background, RoundedCornerShape(CmpRadius.cardMedium)),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(icon = icon, contentDescription = null, tint = tone, modifier = Modifier.size(CmpSize.iconL))
        }
        CmpText(text = stringResource(title), style = CmpTextStyle.Title2)
        CmpText(
            text = message,
            style = CmpTextStyle.Paragraph1,
            color = colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
private fun BottomButtons(state: PassportReadingState, actions: PassportReadActions) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CmpDivider()
        Column(
            modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().padding(
                horizontal = CmpSpacing.space20,
                vertical = CmpSpacing.space12,
            ),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            when (val stop = state.stop) {
                null -> if (state.done) {
                    CmpButton(
                        text = stringResource(Strings.string.mountpassport_read_msg_done),
                        onClick = {},
                        loading = true,
                        size = CmpButtonSize.XL,
                        fullWidth = true,
                        uppercase = false,
                    )
                } else {
                    Secondary(
                        Strings.string.mountpassport_common_btn_cancel,
                        MountPassportTags.READ_CANCEL,
                        actions.onCancel,
                    )
                }

                is ReadStop.ConnectionLost -> {
                    Retry(actions.onRetry)
                    Secondary(
                        Strings.string.mountpassport_read_btn_continue_without,
                        MountPassportTags.READ_CONTINUE,
                        actions.onContinueWithout,
                    )
                }

                is ReadStop.Offline -> {
                    Retry(actions.onRetry)
                    if (stop.canContinueWithout) {
                        Secondary(
                            Strings.string.mountpassport_read_btn_without_reading,
                            MountPassportTags.READ_CONTINUE,
                            actions.onContinueWithout,
                        )
                    } else {
                        Secondary(
                            Strings.string.mountpassport_common_btn_cancel,
                            MountPassportTags.READ_CANCEL,
                            actions.onCancel,
                        )
                    }
                }

                is ReadStop.WrongUnit -> Secondary(
                    Strings.string.mountpassport_common_btn_cancel,
                    MountPassportTags.READ_CANCEL,
                    actions.onCancel,
                )

                is ReadStop.UnitFailed, ReadStop.SaveFailed, is ReadStop.Failed -> {
                    Retry(actions.onRetry)
                    Secondary(
                        Strings.string.mountpassport_common_btn_cancel,
                        MountPassportTags.READ_CANCEL,
                        actions.onCancel,
                    )
                }
            }
        }
    }
}

@Composable
private fun Retry(onClick: () -> Unit) {
    CmpButton(
        text = stringResource(Strings.string.mountpassport_common_btn_retry),
        onClick = onClick,
        icon = CmpIcons.Refresh,
        size = CmpButtonSize.XL,
        fullWidth = true,
        modifier = Modifier.testTag(MountPassportTags.READ_RETRY),
    )
}

@Composable
private fun Secondary(text: StringResource, tag: String, onClick: () -> Unit) {
    CmpButton(
        text = stringResource(text),
        onClick = onClick,
        type = CmpButtonType.Secondary,
        fullWidth = true,
        uppercase = false,
        modifier = Modifier.testTag(tag),
    )
}

private fun PassportReadSource.subtitle(): StringResource = when (this) {
    PassportReadSource.AdvancedMount -> Strings.string.mountpassport_read_subtitle_advanced
    is PassportReadSource.FastMount -> Strings.string.mountpassport_read_subtitle_fast
    is PassportReadSource.Complete -> Strings.string.mountpassport_read_subtitle_complete
}

private fun ReadStop.iconAndTitle(): Pair<CmpIcons, StringResource> = when (this) {
    is ReadStop.UnitFailed -> CmpIcons.ErrorOutline to Strings.string.mountpassport_read_title_unit_failed
    is ReadStop.ConnectionLost -> CmpIcons.LinkOff to Strings.string.mountpassport_read_title_lost
    is ReadStop.Offline -> CmpIcons.BluetoothDisabled to Strings.string.mountpassport_read_title_offline
    is ReadStop.WrongUnit -> CmpIcons.SyncProblem to Strings.string.mountpassport_read_title_wrong_unit
    ReadStop.SaveFailed -> CmpIcons.ErrorOutline to Strings.string.mountpassport_read_title_save_failed
    is ReadStop.Failed -> CmpIcons.ErrorOutline to Strings.string.mountpassport_read_title_failed
}

@Composable
private fun ReadStop.message(source: PassportReadSource): String = when (this) {
    is ReadStop.UnitFailed -> stringResource(Strings.string.mountpassport_read_msg_unit_failed)

    is ReadStop.ConnectionLost -> stringResource(Strings.string.mountpassport_read_msg_lost)

    is ReadStop.Offline -> stringResource(
        if (source is PassportReadSource.FastMount && canContinueWithout) {
            Strings.string.mountpassport_read_msg_offline_master
        } else {
            Strings.string.mountpassport_read_msg_offline
        },
    )

    is ReadStop.WrongUnit ->
        stringResource(Strings.string.mountpassport_read_msg_wrong_unit, expectedSystemId, actualSystemId)

    ReadStop.SaveFailed -> stringResource(Strings.string.mountpassport_read_msg_save_failed)

    is ReadStop.Failed -> error.text()
}
