package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_btn_pause
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_btn_resume
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_label_lines
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_subtitle_loading
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_subtitle_offline
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_subtitle_offline_empty
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_subtitle_paused
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_subtitle_waiting
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_title_offline
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_title_paused
import ru.alarmtrade.specmp.core.localization.resources.signallog_status_title_recording
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogLayout
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogState

internal const val SIGNAL_LOG_PAUSE_TAG = "signallog_pause"

private enum class RecordStatus { RECORDING, PAUSED, OFFLINE }

private val BarMinHeight = 64.dp
private val DotSize = 12.dp
private val PulseSpread = 10.dp
private const val PULSE_MS = 1800
private const val PULSE_ALPHA = 0.6f

@Composable
internal fun SignalLogStatusBar(state: SignalLogState, onPauseToggle: () -> Unit, layout: SignalLogLayout) {
    val colors = CmpTheme.colors
    val status = when {
        !state.connected -> RecordStatus.OFFLINE
        state.paused -> RecordStatus.PAUSED
        else -> RecordStatus.RECORDING
    }
    val background = when (status) {
        RecordStatus.RECORDING -> colors.backgroundSurfaceUltraHighlight
        RecordStatus.PAUSED -> colors.warningHighlight
        RecordStatus.OFFLINE -> colors.errorHighlight
    }
    CmpColumn(modifier = Modifier.fillMaxWidth().background(background)) {
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BarMinHeight)
                .padding(start = layout.paddingH, end = CmpSpacing.space8)
                .padding(vertical = CmpSpacing.space8),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusIcon(status)
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(text = statusTitle(status, state), style = CmpTextStyle.Headline3)
                CmpText(
                    text = statusSubtitle(status, state),
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
            }
            if (state.hasLines && status != RecordStatus.OFFLINE) PauseButton(state.paused, onPauseToggle)
        }
        CmpDivider(color = colors.borderHighlight)
    }
}

@Composable
private fun StatusIcon(status: RecordStatus) {
    val colors = CmpTheme.colors
    CmpBox(modifier = Modifier.size(CmpSize.iconM), contentAlignment = Alignment.Center) {
        when (status) {
            RecordStatus.RECORDING -> PulseDot(colors.successDefault)

            RecordStatus.PAUSED -> CmpIcon(
                CmpIcons.PauseCircle,
                contentDescription = null,
                tint = colors.warningDefault,
            )

            RecordStatus.OFFLINE -> CmpIcon(
                CmpIcons.BluetoothDisabled,
                contentDescription = null,
                tint = colors.errorDefault,
            )
        }
    }
}

@Composable
private fun PauseButton(paused: Boolean, onClick: () -> Unit) {
    CmpButton(
        text = stringResource(if (paused) PauseLabels.second else PauseLabels.first),
        onClick = onClick,
        type = if (paused) CmpButtonType.Primary else CmpButtonType.Secondary,
        size = CmpButtonSize.S,
        icon = if (paused) CmpIcons.PlayArrow else CmpIcons.Pause,
        uppercase = false,
        modifier = Modifier.testTag(SIGNAL_LOG_PAUSE_TAG),
    )
}

@Composable
private fun statusTitle(status: RecordStatus, state: SignalLogState): String = when (status) {
    RecordStatus.RECORDING -> stringResource(Strings.string.signallog_status_title_recording)
    RecordStatus.PAUSED -> stringResource(Strings.string.signallog_status_title_paused, state.pendingCount)
    RecordStatus.OFFLINE -> stringResource(Strings.string.signallog_status_title_offline)
}

@Composable
private fun statusSubtitle(status: RecordStatus, state: SignalLogState): String {
    val count = state.summary.count
    return when (status) {
        RecordStatus.RECORDING -> when {
            !state.loaded -> stringResource(Strings.string.signallog_status_subtitle_loading)
            count > 0 -> pluralStringResource(Strings.plurals.signallog_status_label_lines, count, count)
            else -> stringResource(Strings.string.signallog_status_subtitle_waiting)
        }

        RecordStatus.PAUSED -> pluralStringResource(Strings.plurals.signallog_status_subtitle_paused, count, count)

        RecordStatus.OFFLINE -> if (count > 0) {
            pluralStringResource(Strings.plurals.signallog_status_subtitle_offline, count, count)
        } else {
            stringResource(Strings.string.signallog_status_subtitle_offline_empty)
        }
    }
}

@Composable
private fun PulseDot(color: Color) {
    val transition = rememberInfiniteTransition(label = PULSE_LABEL)
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = LinearEasing), RepeatMode.Restart),
        label = PULSE_LABEL,
    )
    CmpBox(
        modifier = Modifier
            .size(DotSize)
            .drawBehind {
                val p = progress.value
                drawCircle(
                    color = color.copy(alpha = PULSE_ALPHA * (1f - p)),
                    radius = size.minDimension / 2 + PulseSpread.toPx() * p,
                )
            }
            .background(color, CircleShape),
    )
}

private val PauseLabels = Strings.string.signallog_status_btn_pause to Strings.string.signallog_status_btn_resume

private const val PULSE_LABEL = "signalLogPulse"
