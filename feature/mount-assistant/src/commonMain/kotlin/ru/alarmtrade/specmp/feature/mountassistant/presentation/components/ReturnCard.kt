package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_card_value_can_factory
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_bypass
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_can
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_clone
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_clone_attempts
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_from
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_label_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_msg_checking
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_msg_help
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_msg_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_msg_settings_more
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_msg_unchanged
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_title_changed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_title_checking
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_title_unchanged
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_return_value_none
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SnapshotChange
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.actionTitle

private const val SPIN_MILLIS = 1_000
private const val FULL_TURN = 360f

/** Возврат с другого экрана: сначала «Перечитываем блок», потом итог сравнения «до → после». */
@Composable
internal fun ReturnCard(entry: JournalEntry.Return, onAction: (AssistantAction) -> Unit) {
    val colors = CmpTheme.colors
    val changes = entry.changes
    val (icon, tint, strip) = when {
        changes == null -> Triple(CmpIcons.Sync, colors.brandPrimaryDefault, colors.backgroundSurfaceHighlight)
        changes.isNotEmpty() -> Triple(CmpIcons.CheckCircle, colors.successDefault, colors.successHighlight)
        else -> Triple(CmpIcons.Info, colors.foregroundOnDarkSecondaryDefault, colors.backgroundSurfaceHighlight)
    }
    CmpCard(modifier = Modifier.fillMaxWidth().testTag(resultTag(entry.id))) {
        CmpColumn {
            CmpRow(
                modifier = Modifier.fillMaxWidth()
                    .background(strip)
                    .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReturnIcon(icon, tint, spinning = changes == null)
                CmpColumn {
                    CmpText(
                        text = stringResource(
                            Strings.string.mountassistant_return_label_from,
                            actionTitle(entry.action),
                        ),
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                    CmpText(
                        text = stringResource(
                            when {
                                changes == null -> Strings.string.mountassistant_return_title_checking
                                changes.isNotEmpty() -> Strings.string.mountassistant_return_title_changed
                                else -> Strings.string.mountassistant_return_title_unchanged
                            },
                        ),
                        style = CmpTextStyle.Headline2,
                    )
                }
            }
            CmpColumn(
                modifier = Modifier.padding(CmpSpacing.space16),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                when {
                    changes == null -> {
                        CmpText(
                            text = stringResource(Strings.string.mountassistant_return_msg_checking),
                            style = CmpTextStyle.Paragraph1,
                            color = colors.foregroundOnDarkSecondaryDefault,
                        )
                        SkeletonRows(rows = 2)
                    }

                    changes.isNotEmpty() -> {
                        Diffs(changes)
                        val names = changes.filterIsInstance<SnapshotChange.Settings>().flatMap { it.names }
                        if (names.isNotEmpty()) WrittenSettings(names)
                    }

                    else -> {
                        CmpText(
                            text = stringResource(Strings.string.mountassistant_return_msg_unchanged),
                            style = CmpTextStyle.Paragraph1,
                        )
                        if (entry.action in AssistantAction.NeedsHelpOnFailure) HelpChips(onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnIcon(icon: CmpIcons, tint: Color, spinning: Boolean) {
    val angle = if (spinning) {
        val transition = rememberInfiniteTransition(label = "return-sync")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = FULL_TURN,
            animationSpec = infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing), RepeatMode.Restart),
            label = "return-sync-angle",
        )
        value
    } else {
        0f
    }
    CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconL).rotate(angle), tint = tint)
}

private const val WRITTEN_NAMES_SHOWN = 5

@Composable
private fun WrittenSettings(names: List<String>) {
    val shown = names.take(WRITTEN_NAMES_SHOWN).joinToString("; ")
    val rest = names.size - WRITTEN_NAMES_SHOWN
    CmpText(
        text = if (rest > 0) {
            stringResource(Strings.string.mountassistant_return_msg_settings_more, shown, rest)
        } else {
            stringResource(Strings.string.mountassistant_return_msg_settings, shown)
        },
        style = CmpTextStyle.Paragraph1,
    )
}

@Composable
private fun Diffs(changes: List<SnapshotChange>) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = Modifier.fillMaxWidth()
            .border(CmpBorderWidth.listItem, colors.borderHighlight, RoundedCornerShape(CmpRadius.cardSmall)),
    ) {
        changes.forEachIndexed { index, change ->
            if (index > 0) CmpDivider()
            val (label, before, after) = diffTexts(change)
            CmpFlowRow(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = CmpSize.touchMin + CmpSpacing.space4)
                    .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space8),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                CmpText(text = label, modifier = Modifier.weight(1f), style = CmpTextStyle.Paragraph1)
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(before) }
                        },
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                    CmpIcon(
                        icon = CmpIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconS),
                        tint = colors.foregroundOnDarkSecondaryDefault,
                    )
                    CmpTag(text = after, tone = CmpTagTone.Brand)
                }
            }
        }
    }
}

@Composable
private fun diffTexts(change: SnapshotChange): Triple<String, String, String> {
    val none = stringResource(Strings.string.mountassistant_return_value_none)
    return when (change) {
        is SnapshotChange.Firmware ->
            Triple(stringResource(Strings.string.mountassistant_return_label_firmware), change.before, change.after)

        is SnapshotChange.Can -> Triple(
            stringResource(Strings.string.mountassistant_return_label_can),
            canText(change.before, none),
            canText(change.after, none),
        )

        is SnapshotChange.Bypass -> Triple(
            stringResource(Strings.string.mountassistant_return_label_bypass),
            bypassText(change.before),
            bypassText(change.after),
        )

        is SnapshotChange.Clone -> Triple(
            stringResource(Strings.string.mountassistant_return_label_clone),
            cloneText(change.before),
            cloneText(change.after),
        )

        is SnapshotChange.CloneAttempts -> Triple(
            stringResource(Strings.string.mountassistant_return_label_clone_attempts),
            change.before?.toString() ?: none,
            change.after?.toString() ?: none,
        )

        is SnapshotChange.Settings -> Triple(
            stringResource(Strings.string.mountassistant_return_label_settings),
            change.before.toString(),
            change.after.toString(),
        )
    }
}

@Composable
private fun canText(can: CanFacts?, none: String): String = when {
    can == null -> none
    can.factory -> stringResource(Strings.string.mountassistant_card_value_can_factory)
    else -> listOfNotNull(can.version, can.code).joinToString(" · ").ifEmpty { none }
}

/** Помощь, если на экране не получилось: заявка на техпомощь — основной путь, чат — второй. */
@Composable
private fun HelpChips(onAction: (AssistantAction) -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpText(
            text = stringResource(Strings.string.mountassistant_return_msg_help),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        CmpFlowRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpButton(
                text = actionTitle(AssistantAction.TICKET_NEW),
                onClick = { onAction(AssistantAction.TICKET_NEW) },
                size = CmpButtonSize.S,
            )
            CmpButton(
                text = actionTitle(AssistantAction.SUPPORT),
                onClick = { onAction(AssistantAction.SUPPORT) },
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.S,
            )
        }
    }
}
