package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_btn_reflash
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_label_open_in_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_label_query
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_feed_label_search
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_format_number
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultRequest
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StepTitle
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.actionTitle
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.blockLabel
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.blockMessage
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.icon
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.topicTitle

private val StepNode = CmpSize.iconL
private val RailWidth = StepNode
private val RailGap = CmpSpacing.space12
private val RailX = CmpSpacing.space16
private val RailLine = 2.dp

@Composable
internal fun AssistantFeed(
    entries: List<JournalEntry>,
    facts: DeviceFacts,
    callbacks: FeedCallbacks,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) listState.animateScrollToItem(entries.lastIndex)
    }
    val line = CmpTheme.colors.borderHighlight
    LazyColumn(modifier = modifier.hideKeyboardOnScroll(), state = listState, contentPadding = contentPadding) {
        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
            val last = index == entries.lastIndex
            val node = when {
                index == 0 -> RailNode.MARK
                entry is JournalEntry.Step -> RailNode.STEP
                else -> RailNode.NONE
            }
            val top = if (node == RailNode.STEP && index > 0) CmpSpacing.space8 else 0.dp
            CmpRow(
                modifier = Modifier.fillMaxWidth()
                    .drawBehind {
                        val width = RailLine.toPx()
                        val x = RailX.toPx() - width / 2
                        if (node == RailNode.STEP && index > 0) {
                            drawLine(line, Offset(x, 0f), Offset(x, top.toPx()), width)
                        }
                        if (!last) {
                            val start = when (node) {
                                RailNode.MARK -> StepNode.toPx()
                                RailNode.STEP -> top.toPx() + StepNode.toPx()
                                RailNode.NONE -> 0f
                            }
                            drawLine(line, Offset(x, start), Offset(x, size.height), width)
                        }
                    }
                    .padding(top = top, bottom = if (last) 0.dp else CmpSpacing.space16),
                horizontalArrangement = Arrangement.spacedBy(RailGap),
            ) {
                CmpBox(modifier = Modifier.width(RailWidth)) { RailMark(node, entry) }
                CmpColumn(modifier = Modifier.weight(1f)) { FeedEntry(entry, facts, callbacks) }
            }
        }
    }
}

private enum class RailNode { MARK, STEP, NONE }

@Composable
private fun RailMark(node: RailNode, entry: JournalEntry) {
    val colors = CmpTheme.colors
    when (node) {
        RailNode.MARK -> AssistantMark(StepNode)

        RailNode.STEP -> IconBox(
            icon = (entry as? JournalEntry.Step)?.title?.icon() ?: CmpIcons.Bolt,
            background = colors.backgroundSurfaceHighlight,
            tint = colors.foregroundOnDarkPrimaryDefault,
            size = StepNode,
            iconSize = CmpSize.iconS,
        )

        RailNode.NONE -> Unit
    }
}

private fun StepTitle.icon(): CmpIcons = when (this) {
    is StepTitle.Action -> action.icon
    is StepTitle.Search -> CmpIcons.Search
    is StepTitle.Setting -> CmpIcons.Tune
    StepTitle.OpenInSettings -> CmpIcons.OpenInNew
    StepTitle.Reflash -> AssistantAction.FIRMWARE_UPDATE.icon
    is StepTitle.ProductNumber -> AssistantAction.PRODUCT_INFO.icon
}

@Composable
private fun FeedEntry(entry: JournalEntry, facts: DeviceFacts, callbacks: FeedCallbacks) {
    when (entry) {
        is JournalEntry.Greeting -> Greeting(facts)

        is JournalEntry.Step -> StepLabel(entry)

        is JournalEntry.Notice -> Message(
            title = blockLabel(entry.reason),
            text = blockMessage(entry.reason),
            lock = true,
        )

        is JournalEntry.Result -> when (entry.request) {
            ResultRequest.Firmware ->
                DeviceResultCard(
                    entryId = entry.id,
                    kind = DeviceResultKind.FIRMWARE,
                    content = entry.content,
                    onModuleArchive = callbacks.onModuleArchive,
                    onModuleFlash = callbacks.onModuleFlash,
                    onRetry = { callbacks.onRetry(entry.id) },
                )

            ResultRequest.State ->
                DeviceResultCard(entry.id, DeviceResultKind.STATE, entry.content) { callbacks.onRetry(entry.id) }

            ResultRequest.Autostart ->
                DeviceResultCard(entry.id, DeviceResultKind.AUTOSTART, entry.content) { callbacks.onRetry(entry.id) }

            ResultRequest.Product -> ProductResultCard(
                entryId = entry.id,
                content = entry.content,
                onOpen = callbacks.onProductOpen,
                onRetry = { callbacks.onRetry(entry.id) },
            )

            is ResultRequest.Settings -> {
                val request = entry.request as ResultRequest.Settings
                SettingsCard(entry.id, request.query, entry.content, callbacks)
            }
        }

        is JournalEntry.Return -> ReturnCard(entry, callbacks.onAction)

        is JournalEntry.Passport -> PassportCard(entry, callbacks.onAction)

        is JournalEntry.Confirm -> ConfirmWriteCard(entry, callbacks)

        is JournalEntry.SourcePicker -> SourceCard(entry, facts.online) { kind ->
            callbacks.onSourcePick(entry.id, kind)
        }
    }
}

@Composable
internal fun Message(title: String?, text: String?, lock: Boolean = false) {
    CmpRow(
        modifier = Modifier.padding(top = CmpSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        if (lock) {
            CmpIcon(
                icon = CmpIcons.Lock,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconM),
                tint = CmpTheme.colors.warningDefault,
            )
        }
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            if (title != null) CmpText(text = title, style = CmpTextStyle.Headline2)
            if (text != null) CmpText(text = text, style = CmpTextStyle.Paragraph1)
        }
    }
}

@Composable
private fun StepLabel(step: JournalEntry.Step) {
    val (topic, action) = when (val title = step.title) {
        is StepTitle.Action -> topicTitle(title.action.topic) to actionTitle(title.action)

        is StepTitle.Search -> stringResource(Strings.string.mountassistant_feed_label_search) to
            stringResource(Strings.string.mountassistant_feed_label_query, title.query)

        is StepTitle.Setting -> topicTitle(title.topic) to title.name

        StepTitle.OpenInSettings -> topicTitle(StepTitle.OpenInSettings.topic) to
            stringResource(Strings.string.mountassistant_feed_label_open_in_settings)

        StepTitle.Reflash -> topicTitle(StepTitle.Reflash.topic) to
            stringResource(Strings.string.mountassistant_card_btn_reflash)

        is StepTitle.ProductNumber -> topicTitle(title.topic) to
            stringResource(Strings.string.mountassistant_product_format_number, title.number)
    }
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = StepNode),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpFlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(text = topic, style = CmpTextStyle.Paragraph2, color = muted)
            CmpIcon(
                icon = CmpIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconS),
                tint = muted,
            )
            CmpText(text = action, style = CmpTextStyle.Headline3)
        }
        CmpText(
            text = step.at.clockText(),
            modifier = Modifier.padding(start = CmpSpacing.space8),
            style = CmpTextStyle.Paragraph3,
            color = muted,
        )
    }
}

@Composable
private fun Instant.clockText(): String {
    val time = toLocalDateTime(currentTimeZone())
    return "${time.hour}:${time.minute.toString().padStart(2, '0')}"
}
