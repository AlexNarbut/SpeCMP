package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.time.currentTimeZone
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.icon
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.label
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.RequestAction

private val CardShape = RoundedCornerShape(20.dp)
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
    val colors = cmpThemeColors()
    val active = request.status.isActive
    val background by animateColorAsState(
        when {
            highlighted -> colors.primaryContainer
            active -> colors.surfaceContainerLow
            else -> Color.Transparent
        },
    )
    val border = when {
        highlighted -> BorderStroke(2.dp, colors.primary)
        else -> BorderStroke(1.dp, colors.outlineVariant)
    }
    val textColor = if (active) colors.onSurface else colors.onSurfaceVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(background, CardShape)
            .border(border, CardShape)
            .testTag("techrequests_item_${request.id}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
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

@Composable
private fun CardHeader(request: TechRequest, textColor: Color) {
    val colors = cmpThemeColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(
                text = stringResource(Strings.string.techrequests_common_label_number, request.id.toString()),
                style = CmpTextStyle.Headline2,
                color = textColor,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpIcon(
                    icon = CmpIcons.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colors.onSurfaceVariant,
                )
                CmpText(
                    text = rememberRequestDate(request.createdAt),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        StatusChip(request.status)
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
            CmpTextButton(
                text = stringResource(
                    if (expanded) {
                        Strings.string.techrequests_list_btn_show_less
                    } else {
                        Strings.string.techrequests_list_btn_show_more
                    },
                ),
                onClick = { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun Contact(request: TechRequest) {
    val colors = cmpThemeColors()
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        ContactItem(CmpIcons.Person, request.userName, colors.onSurfaceVariant)
        ContactItem(CmpIcons.Call, request.userPhone, colors.onSurfaceVariant)
    }
}

@Composable
private fun ContactItem(icon: CmpIcons, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
        CmpText(
            text = text,
            style = CmpTextStyle.Paragraph2,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CardActions(id: Long, busyAction: RequestAction?, onAction: (RequestAction) -> Unit) {
    val colors = cmpThemeColors()
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant))
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpOutlinedButton(
                onClick = { onAction(RequestAction.Close) },
                enabled = busyAction == null,
                modifier = Modifier.heightIn(min = 48.dp).testTag("techrequests_item_close_$id"),
            ) {
                if (busyAction != RequestAction.Close) {
                    CmpIcon(icon = CmpIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                CmpText(
                    text = stringResource(
                        if (busyAction == RequestAction.Close) {
                            Strings.string.techrequests_list_loading_closing
                        } else {
                            Strings.string.techrequests_list_btn_resolved
                        },
                    ),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 1,
                )
            }
            CmpTextButton(
                onClick = { onAction(RequestAction.Cancel) },
                enabled = busyAction == null,
                modifier = Modifier.heightIn(min = 48.dp).testTag("techrequests_item_cancel_$id"),
            ) {
                val tint = if (busyAction == null) colors.error else colors.onSurfaceVariant
                if (busyAction != RequestAction.Cancel) {
                    CmpIcon(
                        icon = CmpIcons.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = tint,
                    )
                }
                CmpText(
                    text = stringResource(
                        if (busyAction == RequestAction.Cancel) {
                            Strings.string.techrequests_list_loading_canceling
                        } else {
                            Strings.string.techrequests_list_btn_cancel_request
                        },
                    ),
                    style = CmpTextStyle.Paragraph2,
                    color = tint,
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun StatusChip(status: TechRequestStatus, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    val domain = cmpDomainColors()
    val (container, content) = when (status) {
        TechRequestStatus.OPEN -> colors.primaryContainer to colors.onPrimaryContainer

        TechRequestStatus.IN_PROCESS -> domain.success.copy(alpha = SUCCESS_TINT) to colors.onSurface

        TechRequestStatus.SUSPENDED -> domain.warningContainer to domain.onWarningContainer

        TechRequestStatus.FINISHED,
        TechRequestStatus.FINISHED_BY_USER,
        -> colors.surfaceContainerHighest to colors.onSurfaceVariant

        TechRequestStatus.CANCELED_BY_USER -> Color.Transparent to colors.onSurfaceVariant
    }
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .height(32.dp)
            .background(container, shape)
            .then(
                if (status ==
                    TechRequestStatus.CANCELED_BY_USER
                ) {
                    Modifier.border(1.dp, colors.outlineVariant, shape)
                } else {
                    Modifier
                },
            )
            .padding(start = 8.dp, end = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CmpIcon(icon = status.icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = content)
        CmpText(text = stringResource(status.label), style = CmpTextStyle.Headline3, color = content, maxLines = 1)
    }
}

@Composable
internal fun TechRequestCardSkeleton(modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.outlineVariant, CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CmpSkeleton(Modifier.width(150.dp).height(22.dp))
            CmpSkeleton(Modifier.width(96.dp).height(32.dp))
        }
        CmpSkeleton(Modifier.width(120.dp).height(16.dp))
        CmpSkeleton(Modifier.fillMaxWidth(SKELETON_LINE).height(16.dp))
        CmpSkeleton(Modifier.fillMaxWidth(SKELETON_SHORT_LINE).height(16.dp))
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

private const val SUCCESS_TINT = 0.24f
private const val SKELETON_LINE = 0.92f
private const val SKELETON_SHORT_LINE = 0.7f
