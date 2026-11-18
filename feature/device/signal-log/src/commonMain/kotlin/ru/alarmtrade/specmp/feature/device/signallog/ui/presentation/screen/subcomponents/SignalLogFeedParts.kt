package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.signallog_empty_hint_background
import ru.alarmtrade.specmp.core.localization.resources.signallog_empty_hint_firmware
import ru.alarmtrade.specmp.core.localization.resources.signallog_empty_msg_connected
import ru.alarmtrade.specmp.core.localization.resources.signallog_empty_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.signallog_empty_title_connected
import ru.alarmtrade.specmp.core.localization.resources.signallog_empty_title_offline
import ru.alarmtrade.specmp.core.localization.resources.signallog_feed_btn_copy
import ru.alarmtrade.specmp.core.localization.resources.signallog_feed_format_copy
import ru.alarmtrade.specmp.core.localization.resources.signallog_feed_loading_older
import ru.alarmtrade.specmp.core.localization.resources.signallog_feed_msg_start
import ru.alarmtrade.specmp.core.localization.resources.signallog_load_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.signallog_load_error_failed
import ru.alarmtrade.specmp.core.localization.resources.signallog_load_error_no_serial
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogError
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogLayout
import specmp.feature.device.signal_log.generated.resources.Res
import specmp.feature.device.signal_log.generated.resources.signallog_receipt_long

internal const val SIGNAL_LOG_COPY_TAG = "signallog_copy"
internal const val SIGNAL_LOG_RETRY_TAG = "signallog_retry"

private val RowVerticalPadding = 7.dp
private val CopyButtonHeight = 56.dp
private val HeroSize = 96.dp
private val HeroIconSize = 48.dp
private val ContentMaxWidth = 440.dp
private val TopRowHeight = 56.dp
private val SpinnerSize = 20.dp
private val SpinnerStroke = 3.dp
private val SkeletonTimeWidth = 64.dp
private val SkeletonTimeHeight = 14.dp
private val SkeletonTextHeight = 16.dp
private val SkeletonWidths = listOf(0.72f, 0.48f, 0.9f, 0.64f, 0.38f, 0.82f, 0.56f, 0.7f, 0.44f, 0.88f, 0.6f, 0.52f)
private const val MARK_ALPHA = 0.24f
private val EmptyTitles = Strings.string.signallog_empty_title_connected to Strings.string.signallog_empty_title_offline
private val EmptyMessages = Strings.string.signallog_empty_msg_connected to Strings.string.signallog_empty_msg_offline

private val TimeFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
    char(':')
    second()
}

/** Долгий тап (на планшете с мышью — и правый клик) выделяет строку и показывает над ней «Копировать». */
@Composable
internal fun SignalLogLineRow(
    line: SignalLogLine,
    dayLabel: String?,
    query: String,
    current: Boolean,
    selected: Boolean,
    layout: SignalLogLayout,
    onSelect: () -> Unit,
    onUnselect: () -> Unit,
    onCopy: (String) -> Unit,
) {
    val colors = CmpTheme.colors
    val timeZone = currentTimeZone()
    val time = remember(line.timeMs, timeZone) {
        TimeFormat.format(Instant.fromEpochMilliseconds(line.timeMs).toLocalDateTime(timeZone).time)
    }
    val accent = colors.brandPrimaryDefault
    val onAccent = colors.onBrand
    val text = remember(line.text, query, current, accent, onAccent) {
        highlighted(line.text, query, current, accent, onAccent)
    }
    val background = when {
        selected -> colors.backgroundSurfaceMediumHighlight
        current -> colors.brandPrimaryHighlight
        else -> Color.Transparent
    }
    CmpColumn(modifier = Modifier.fillMaxWidth()) {
        if (dayLabel != null) DaySeparator(label = dayLabel, layout = layout)
        CmpBox(modifier = Modifier.fillMaxWidth()) {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background)
                    .combinedClickable(onClick = { if (selected) onUnselect() }, onLongClick = onSelect)
                    .secondaryClick(onSelect)
                    .padding(horizontal = layout.paddingH, vertical = RowVerticalPadding),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpText(
                    text = time,
                    style = CmpTextStyle.Mono2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 1,
                )
                CmpText(text = text, style = CmpTextStyle.Mono1, modifier = Modifier.weight(1f))
            }
            CmpDivider(modifier = Modifier.align(Alignment.BottomCenter), color = colors.borderUltraHighlight)
            if (selected) {
                val copyText = stringResource(Strings.string.signallog_feed_format_copy, time, line.text)
                CopyPopup(layout = layout, onDismiss = onUnselect) {
                    onCopy(copyText)
                    onUnselect()
                }
            }
        }
    }
}

@Composable
private fun CopyPopup(layout: SignalLogLayout, onDismiss: () -> Unit, onCopy: () -> Unit) {
    val density = LocalDensity.current
    val offset = with(density) { IntOffset(-layout.paddingH.roundToPx(), -CopyButtonHeight.roundToPx()) }
    Popup(alignment = Alignment.TopEnd, offset = offset, onDismissRequest = onDismiss) {
        CmpButton(
            text = stringResource(Strings.string.signallog_feed_btn_copy),
            onClick = onCopy,
            size = CmpButtonSize.XL,
            icon = CmpIcons.ContentCopy,
            uppercase = false,
            modifier = Modifier.testTag(SIGNAL_LOG_COPY_TAG),
        )
    }
}

private fun Modifier.secondaryClick(onClick: () -> Unit): Modifier = pointerInput(onClick) {
    val context = currentCoroutineContext()
    awaitPointerEventScope {
        while (context.isActive) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) onClick()
        }
    }
}

private fun highlighted(
    text: String,
    query: String,
    current: Boolean,
    accent: Color,
    onAccent: Color,
): AnnotatedString {
    if (query.isEmpty()) return AnnotatedString(text)
    val style = if (current) {
        SpanStyle(background = accent, color = onAccent)
    } else {
        SpanStyle(background = accent.copy(alpha = MARK_ALPHA))
    }
    return buildAnnotatedString {
        var from = 0
        var match = text.indexOf(query, from, ignoreCase = true)
        while (match >= 0) {
            append(text.substring(from, match))
            withStyle(style) { append(text.substring(match, match + query.length)) }
            from = match + query.length
            match = text.indexOf(query, from, ignoreCase = true)
        }
        append(text.substring(from))
    }
}

@Composable
private fun DaySeparator(label: String, layout: SignalLogLayout) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = layout.paddingH, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpDivider(modifier = Modifier.weight(1f), color = colors.borderHighlight)
        CmpText(
            text = label,
            style = CmpTextStyle.Additional0,
            color = colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier
                .background(colors.backgroundSurfaceHighlight, RoundedCornerShape(CmpRadius.round))
                .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space4),
        )
        CmpDivider(modifier = Modifier.weight(1f), color = colors.borderHighlight)
    }
}

@Composable
internal fun FeedTop(loadingOlder: Boolean, atStart: Boolean, layout: SignalLogLayout) {
    val colors = CmpTheme.colors
    when {
        loadingOlder -> CmpRow(
            modifier = Modifier.fillMaxWidth().height(TopRowHeight),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(SpinnerSize), strokeWidth = SpinnerStroke)
            CmpText(
                text = stringResource(Strings.string.signallog_feed_loading_older),
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }

        atStart -> CmpText(
            text = stringResource(Strings.string.signallog_feed_msg_start),
            style = CmpTextStyle.Paragraph3,
            color = colors.foregroundOnDarkSecondaryDefault,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = layout.paddingH, end = layout.paddingH, top = CmpSpacing.space16)
                .padding(bottom = CmpSpacing.space8),
        )

        else -> CmpBox(modifier = Modifier.height(CmpSpacing.space8))
    }
}

@Composable
internal fun FeedSkeleton(layout: SignalLogLayout, modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier.fillMaxWidth().padding(top = CmpSpacing.space8)) {
        SkeletonWidths.forEach { fraction ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = layout.paddingH, vertical = CmpSpacing.space12),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpSkeleton(modifier = Modifier.width(SkeletonTimeWidth).height(SkeletonTimeHeight))
                CmpSkeleton(modifier = Modifier.weight(1f).height(SkeletonTextHeight).fillMaxWidth(fraction))
            }
        }
    }
}

@Composable
internal fun FeedError(error: SignalLogError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = modifier.fillMaxSize().padding(CmpSpacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16, Alignment.CenterVertically),
    ) {
        CmpIcon(CmpIcons.AlertCircle, contentDescription = null, tint = colors.errorDefault)
        CmpText(
            text = stringResource(
                when (error) {
                    SignalLogError.LOAD_FAILED -> Strings.string.signallog_load_error_failed
                    SignalLogError.NO_SERIAL -> Strings.string.signallog_load_error_no_serial
                },
            ),
            style = CmpTextStyle.Headline3,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = ContentMaxWidth),
        )
        CmpButton(
            text = stringResource(Strings.string.signallog_load_btn_retry),
            onClick = onRetry,
            icon = CmpIcons.Refresh,
            modifier = Modifier.testTag(SIGNAL_LOG_RETRY_TAG),
        )
    }
}

@Composable
internal fun FeedEmpty(connected: Boolean, compact: Boolean, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (compact) CmpSpacing.space24 else CmpSpacing.space56)
            .padding(vertical = CmpSpacing.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16, Alignment.CenterVertically),
    ) {
        CmpBox(
            modifier = Modifier.size(HeroSize).background(colors.brandPrimaryHighlight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                painter = painterResource(Res.drawable.signallog_receipt_long),
                contentDescription = null,
                modifier = Modifier.size(HeroIconSize),
                tint = colors.brandPrimaryDefault,
            )
        }
        CmpText(
            text = stringResource(if (connected) EmptyTitles.first else EmptyTitles.second),
            style = CmpTextStyle.Headline1,
            textAlign = TextAlign.Center,
        )
        CmpText(
            text = stringResource(if (connected) EmptyMessages.first else EmptyMessages.second),
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnDarkSecondaryDefault,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = ContentMaxWidth),
        )
        CmpColumn(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .background(colors.backgroundSurfaceCard, RoundedCornerShape(CmpRadius.cardMedium))
                .border(1.dp, colors.borderHighlight, RoundedCornerShape(CmpRadius.cardMedium))
                .padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            EmptyHint(
                icon = CmpIcons.Bluetooth,
                text = stringResource(Strings.string.signallog_empty_hint_background),
                tint = colors.brandPrimaryDefault,
                textColor = colors.foregroundOnDarkPrimaryDefault,
            )
            EmptyHint(
                icon = CmpIcons.SystemUpdate,
                text = stringResource(Strings.string.signallog_empty_hint_firmware),
                tint = colors.foregroundOnDarkSecondaryDefault,
                textColor = colors.foregroundOnDarkSecondaryDefault,
            )
        }
    }
}

@Composable
private fun EmptyHint(icon: CmpIcons, text: String, tint: Color, textColor: Color) {
    CmpRow(
        modifier = Modifier.heightIn(min = CmpSize.iconM),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon, contentDescription = null, tint = tint)
        CmpText(text = text, style = CmpTextStyle.Paragraph3, color = textColor, modifier = Modifier.weight(1f))
    }
}
