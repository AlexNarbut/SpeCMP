package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_format_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_label_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_status_settings_factory
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_btn_skip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_format_block
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_format_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_format_devices_usb
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_format_imei
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_format_join
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_hint_block
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_hint_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_hint_params
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_hint_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_item_block
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_item_devices
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_item_params
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_item_ready
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_item_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_block_failed
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_devices_none
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_devices_usb
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_lost
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_not_read
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_params_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_reading
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_ready_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_settings_done
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_settings_long
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_skipped
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_read_status_unavailable
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportReadStep
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepStatus
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStepSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportTransport
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportUnavailableReason
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read.PassportReadingState

private val GlyphSize = 40.dp
private val GlyphBorder = 2.dp
private val GlyphRing = 3.dp
private val ConnectorWidth = 2.dp

/** Этапы считывания: значок статуса, название, итог или подсказка; у «Настроек» — таймер и «Пропустить». */
@Composable
internal fun ReadSteps(state: PassportReadingState, onSkipSettings: () -> Unit) {
    val steps = state.read.steps
    Column {
        steps.forEachIndexed { index, step ->
            StepRow(
                number = index + 1,
                step = step.step,
                status = step.status,
                last = index == steps.lastIndex,
                transport = state.read.transport,
                seconds = state.settingsSeconds,
                onSkip = onSkipSettings.takeIf { state.stop == null },
            )
        }
    }
}

@Composable
private fun StepRow(
    number: Int,
    step: PassportReadStep,
    status: PassportStepStatus,
    last: Boolean,
    transport: PassportTransport?,
    seconds: Int,
    onSkip: (() -> Unit)?,
) {
    val colors = CmpTheme.colors
    val muted = colors.foregroundOnDarkSecondaryDefault
    val longRead = step == PassportReadStep.SETTINGS && status == PassportStepStatus.Reading
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).testTag(MountPassportTags.readStep(step.name)),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Glyph(number, status)
            if (!last) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(ConnectorWidth)
                        .background(
                            if (status is PassportStepStatus.Done) colors.successDefault else colors.borderHighlight,
                        ),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = CmpSpacing.space8, bottom = if (last) 0.dp else CmpSpacing.space20)
                .height(IntrinsicSize.Min),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CmpText(
                    text = stringResource(step.title()),
                    style = CmpTextStyle.Headline2,
                    color = if (status == PassportStepStatus.Waiting) muted else Color.Unspecified,
                    modifier = Modifier.weight(1f),
                )
                if (longRead) CmpText(text = elapsed(seconds), style = CmpTextStyle.Additional0, color = muted)
            }
            stepLine(step, status, transport)?.let { line ->
                CmpText(
                    text = line,
                    style = CmpTextStyle.Paragraph2,
                    color = when (status) {
                        is PassportStepStatus.NotRead -> colors.warningDefault
                        is PassportStepStatus.Done -> Color.Unspecified
                        else -> muted
                    },
                )
            }
            if (longRead && onSkip != null) {
                CmpButton(
                    text = stringResource(Strings.string.mountpassport_read_btn_skip),
                    onClick = onSkip,
                    type = CmpButtonType.Tertiary,
                    size = CmpButtonSize.S,
                    icon = CmpIcons.Redo,
                    uppercase = false,
                    modifier = Modifier.testTag(MountPassportTags.READ_SKIP),
                )
            }
        }
    }
}

@Composable
private fun Glyph(number: Int, status: PassportStepStatus) {
    val colors = CmpTheme.colors
    val muted = colors.foregroundOnDarkSecondaryDefault
    val base = Modifier.size(GlyphSize).padding(0.dp)
    when (status) {
        PassportStepStatus.Reading -> Box(base, contentAlignment = Alignment.Center) {
            CmpCircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = GlyphRing,
                color = colors.brandPrimaryDefault,
                trackColor = colors.brandPrimaryHighlight,
            )
            CmpText(text = number.toString(), style = CmpTextStyle.Headline3, color = colors.brandPrimaryDefault)
        }

        is PassportStepStatus.Done -> Box(
            base.background(colors.successDefault, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(icon = CmpIcons.Check, contentDescription = null, tint = colors.staticWhite)
        }

        is PassportStepStatus.NotRead -> Box(
            base.background(
                colors.warningHighlight,
                CircleShape,
            ).border(GlyphBorder, colors.warningDefault, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(icon = CmpIcons.PriorityHigh, contentDescription = null, tint = colors.warningDefault)
        }

        is PassportStepStatus.Unavailable, PassportStepStatus.Skipped -> Box(
            base.border(GlyphBorder, colors.borderTertiary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = if (status == PassportStepStatus.Skipped) CmpIcons.Redo else CmpIcons.Remove,
                contentDescription = null,
                tint = muted,
                modifier = Modifier.size(CmpSize.iconM),
            )
        }

        PassportStepStatus.Waiting -> Box(
            base.border(GlyphBorder, colors.borderLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpText(text = number.toString(), style = CmpTextStyle.Headline3, color = muted)
        }
    }
}

@Composable
private fun stepLine(step: PassportReadStep, status: PassportStepStatus, transport: PassportTransport?): String? =
    when (status) {
        PassportStepStatus.Waiting -> step.waitHint()?.let { stringResource(it) }

        PassportStepStatus.Reading -> stringResource(readingLine(step, transport))

        is PassportStepStatus.Done -> summaryLine(status.summary)

        is PassportStepStatus.NotRead -> stringResource(notReadLine(step, status.connectionLost))

        is PassportStepStatus.Unavailable -> stringResource(
            when (status.reason) {
                PassportUnavailableReason.FACTORY_CAN -> Strings.string.mountpassport_editor_status_settings_factory
                PassportUnavailableReason.NOT_SUPPORTED -> Strings.string.mountpassport_read_status_unavailable
            },
        )

        PassportStepStatus.Skipped -> stringResource(Strings.string.mountpassport_read_status_skipped)
    }

private fun readingLine(step: PassportReadStep, transport: PassportTransport?): StringResource = when {
    step == PassportReadStep.SETTINGS -> Strings.string.mountpassport_read_status_settings_long

    step == PassportReadStep.DEVICES && transport == PassportTransport.USB ->
        Strings.string.mountpassport_read_status_devices_usb

    else -> Strings.string.mountpassport_read_status_reading
}

private fun notReadLine(step: PassportReadStep, connectionLost: Boolean): StringResource = when {
    connectionLost -> Strings.string.mountpassport_read_status_lost
    step == PassportReadStep.BLOCK -> Strings.string.mountpassport_read_status_block_failed
    else -> Strings.string.mountpassport_read_status_not_read
}

@Composable
private fun summaryLine(summary: PassportStepSummary): String = when (summary) {
    is PassportStepSummary.Block -> summary.firmwareVersion?.let {
        stringResource(Strings.string.mountpassport_read_format_block, summary.systemName, summary.systemId, it)
    } ?: stringResource(Strings.string.mountpassport_common_format_unit, summary.systemName, summary.systemId)

    is PassportStepSummary.Params -> paramsLine(summary)

    is PassportStepSummary.Devices -> devicesLine(summary)

    is PassportStepSummary.Settings -> stringResource(Strings.string.mountpassport_read_status_settings_done)

    is PassportStepSummary.Ready -> stringResource(Strings.string.mountpassport_read_status_ready_done)
}

@Composable
private fun paramsLine(summary: PassportStepSummary.Params): String {
    val parts = listOfNotNull(
        summary.imei?.takeIf {
            it.isNotBlank()
        }?.let { stringResource(Strings.string.mountpassport_read_format_imei, it) },
        summary.bluetoothFirmware?.takeIf { it.isNotBlank() }?.let {
            stringResource(Strings.string.mountpassport_read_format_bluetooth, it)
        },
    )
    return when (parts.size) {
        0 -> stringResource(Strings.string.mountpassport_read_status_params_done)
        1 -> parts.single()
        else -> stringResource(Strings.string.mountpassport_read_format_join, parts[0], parts[1])
    }
}

@Composable
private fun devicesLine(summary: PassportStepSummary.Devices): String {
    if (summary.count == 0) return stringResource(Strings.string.mountpassport_read_status_devices_none)
    val count = pluralStringResource(Strings.plurals.mountpassport_editor_label_devices, summary.count, summary.count)
    return if (summary.transport == PassportTransport.USB) {
        stringResource(Strings.string.mountpassport_read_format_devices_usb, count)
    } else {
        count
    }
}

private fun PassportReadStep.title(): StringResource = when (this) {
    PassportReadStep.BLOCK -> Strings.string.mountpassport_read_item_block
    PassportReadStep.PARAMS -> Strings.string.mountpassport_read_item_params
    PassportReadStep.DEVICES -> Strings.string.mountpassport_read_item_devices
    PassportReadStep.SETTINGS -> Strings.string.mountpassport_read_item_settings
    PassportReadStep.READY -> Strings.string.mountpassport_read_item_ready
}

private fun PassportReadStep.waitHint(): StringResource? = when (this) {
    PassportReadStep.BLOCK -> Strings.string.mountpassport_read_hint_block
    PassportReadStep.PARAMS -> Strings.string.mountpassport_read_hint_params
    PassportReadStep.DEVICES -> Strings.string.mountpassport_read_hint_devices
    PassportReadStep.SETTINGS -> Strings.string.mountpassport_read_hint_settings
    PassportReadStep.READY -> null
}

/** «0:24» — минуты и секунды одинаково на всех языках. */
private fun elapsed(seconds: Int): String =
    "${seconds / SECONDS_IN_MINUTE}$TIME_SEPARATOR${(seconds % SECONDS_IN_MINUTE).toString().padStart(2, '0')}"

private const val SECONDS_IN_MINUTE = 60
private const val TIME_SEPARATOR = ":"
