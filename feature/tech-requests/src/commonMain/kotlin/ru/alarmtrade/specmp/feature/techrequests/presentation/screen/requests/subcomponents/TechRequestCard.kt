package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_label_number
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_cancel_request
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_resolved
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_show_less
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_show_more
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_format_date_time
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_loading_canceling
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_loading_closing
import ru.alarmtrade.specmp.core.ui.format.rememberCmpDateLabeler
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsTags
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.icon
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.label
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.RequestAction

private val CardShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardMedium)
private val HighlightBorder = 2.dp
private val ContactIconSize = 18.dp
private const val ACTIVE_LINES = 4
private const val FINISHED_LINES = 2
private const val ACTIVE_LONG_TEXT = 170
private const val FINISHED_LONG_TEXT = 110

/** [busyAction] — по заявке идёт отмена/закрытие: кнопки заблокированы, вверху индикатор. */
@Composable
internal fun TechRequestCard(
    request: TechRequest,
    busyAction: RequestAction?,
    highlighted: Boolean,
    onAction: (RequestAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    val active = request.status.isActive
    val background by animateColorAsState(
        when {
            highlighted -> colors.brandPrimaryHighlight
            active -> colors.backgroundSurfaceCard
            else -> Color.Transparent
        },
    )
    val border = when {
        highlighted -> BorderStroke(HighlightBorder, colors.brandPrimaryDefault)
        else -> BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight)
    }
    val textColor = if (active) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkSecondaryDefault
    CmpCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(TechRequestsTags.item(request.id)),
        shape = CardShape,
        border = border,
        containerColor = background,
    ) {
        Box {
            Column(
                modifier = Modifier.padding(CmpSpacing.space16).animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CardHeader(request, textColor)
                IssueText(request, textColor)
                if (active) {
                    Contact(request)
                    CardActions(request.id, busyAction, onAction)
                }
            }
            if (busyAction != null) {
                CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun CardHeader(request: TechRequest, textColor: Color) {
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(
                text = stringResource(Strings.string.techrequests_common_label_number, request.id.toString()),
                style = if (request.status.isActive) CmpTextStyle.Headline2 else CmpTextStyle.Headline3,
                color = textColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpIcon(
                    icon = CmpIcons.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(CmpSize.iconS),
                    tint = secondary,
                )
                CmpText(
                    text = rememberRequestDate(request.createdAt),
                    style = CmpTextStyle.Paragraph2,
                    color = secondary,
                )
            }
        }
        StatusTag(request.status)
    }
}

@Composable
private fun IssueText(request: TechRequest, textColor: Color) {
    val active = request.status.isActive
    var expanded by rememberSaveable(request.id) { mutableStateOf(false) }
    val long = request.issue.length > if (active) ACTIVE_LONG_TEXT else FINISHED_LONG_TEXT
    Column {
        CmpText(
            text = request.issue,
            style = if (active) CmpTextStyle.Paragraph1 else CmpTextStyle.Paragraph2,
            color = textColor,
            maxLines = if (expanded) {
                Int.MAX_VALUE
            } else if (active) {
                ACTIVE_LINES
            } else {
                FINISHED_LINES
            },
            overflow = TextOverflow.Ellipsis,
        )
        if (long) {
            CmpButton(
                text = stringResource(
                    if (expanded) {
                        Strings.string.techrequests_list_btn_show_less
                    } else {
                        Strings.string.techrequests_list_btn_show_more
                    },
                ),
                onClick = { expanded = !expanded },
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        }
    }
}

@Composable
private fun Contact(request: TechRequest) {
    val color = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        ContactItem(CmpIcons.Person, request.userName, color)
        ContactItem(CmpIcons.Call, request.userPhone, color)
    }
}

@Composable
private fun ContactItem(icon: CmpIcons, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(ContactIconSize), tint = color)
        CmpText(
            text = text,
            style = CmpTextStyle.Paragraph2,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Капс-подписи S-кнопок на Compact в строку не помещаются — вторая переносится. */
@Composable
private fun CardActions(id: Long, busyAction: RequestAction?, onAction: (RequestAction) -> Unit) {
    val idle = busyAction == null
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpDivider()
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpButton(
                text = stringResource(
                    if (busyAction == RequestAction.Close) {
                        Strings.string.techrequests_list_loading_closing
                    } else {
                        Strings.string.techrequests_list_btn_resolved
                    },
                ),
                onClick = { onAction(RequestAction.Close) },
                enabled = idle,
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.S,
                icon = CmpIcons.Check.takeIf { busyAction != RequestAction.Close },
                modifier = Modifier.testTag(TechRequestsTags.itemClose(id)),
            )
            CmpButton(
                text = stringResource(
                    if (busyAction == RequestAction.Cancel) {
                        Strings.string.techrequests_list_loading_canceling
                    } else {
                        Strings.string.techrequests_list_btn_cancel_request
                    },
                ),
                onClick = { onAction(RequestAction.Cancel) },
                enabled = idle,
                type = CmpButtonType.TertiaryDanger,
                size = CmpButtonSize.S,
                icon = CmpIcons.Close.takeIf { busyAction != RequestAction.Cancel },
                modifier = Modifier.testTag(TechRequestsTags.itemCancel(id)),
            )
        }
    }
}

@Composable
private fun StatusTag(status: TechRequestStatus, modifier: Modifier = Modifier) {
    val tone = when (status) {
        TechRequestStatus.OPEN -> CmpTagTone.Brand

        TechRequestStatus.IN_PROCESS -> CmpTagTone.Success

        TechRequestStatus.SUSPENDED -> CmpTagTone.Warning

        TechRequestStatus.FINISHED,
        TechRequestStatus.FINISHED_BY_USER,
        -> CmpTagTone.Neutral

        TechRequestStatus.CANCELED_BY_USER -> CmpTagTone.Outline
    }
    CmpTag(
        text = stringResource(status.label),
        tone = tone,
        icon = status.icon,
        modifier = modifier.animateContentSize(),
    )
}

@Composable
internal fun TechRequestCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(CmpBorderWidth.listItem, CmpTheme.colors.borderHighlight, CardShape)
            .padding(CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CmpSkeleton(Modifier.width(150.dp).height(22.dp))
            CmpSkeleton(Modifier.width(96.dp).height(32.dp))
        }
        CmpSkeleton(Modifier.width(120.dp).height(CmpSpacing.space16))
        CmpSkeleton(Modifier.fillMaxWidth(SKELETON_LINE).height(CmpSpacing.space16))
        CmpSkeleton(Modifier.fillMaxWidth(SKELETON_SHORT_LINE).height(CmpSpacing.space16))
    }
}

/** «13 ноября, 14:05»; год — если не текущий (лейблер `core/ui`). */
@Composable
private fun rememberRequestDate(createdAt: Instant): String {
    val local = createdAt.toLocalDateTime(currentTimeZone())
    val day = rememberCmpDateLabeler()(local.date)
    val time = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    return stringResource(Strings.string.techrequests_list_format_date_time, day, time)
}

private const val SKELETON_LINE = 0.92f
private const val SKELETON_SHORT_LINE = 0.7f
