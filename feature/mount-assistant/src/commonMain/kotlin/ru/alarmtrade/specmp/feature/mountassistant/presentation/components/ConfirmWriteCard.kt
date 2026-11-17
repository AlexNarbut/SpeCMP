package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_btn_write
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_error_failed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_error_link_lost
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_label_after
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_label_after_done
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_label_before
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_msg_cancelled
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_msg_other_unsaved
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_msg_pin
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_msg_pin_skipped
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_msg_writing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_title_cancelled
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_title_done
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_title_error
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_title_pending
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_title_pin
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_confirm_title_writing
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteStatus
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.settingValueText

internal const val CONFIRM_WRITE_TAG = "mountassistant_confirm_write"
private const val CANCELLED_ALPHA = 0.72f

private class ConfirmHead(val icon: CmpIcons, val title: StringResource, val background: Color, val content: Color)

@Composable
private fun WriteStatus.head(): ConfirmHead {
    val colors = cmpThemeColors()
    return when (this) {
        WriteStatus.PENDING ->
            ConfirmHead(
                CmpIcons.Edit,
                Strings.string.mountassistant_confirm_title_pending,
                colors.primary,
                colors.onPrimary,
            )

        WriteStatus.PIN ->
            ConfirmHead(CmpIcons.Pin, Strings.string.mountassistant_confirm_title_pin, colors.primary, colors.onPrimary)

        WriteStatus.WRITING ->
            ConfirmHead(
                CmpIcons.Sync,
                Strings.string.mountassistant_confirm_title_writing,
                colors.primary,
                colors.onPrimary,
            )

        WriteStatus.DONE -> ConfirmHead(
            CmpIcons.Check,
            Strings.string.mountassistant_confirm_title_done,
            cmpDomainColors().success,
            colors.onPrimary,
        )

        WriteStatus.ERROR ->
            ConfirmHead(
                CmpIcons.ErrorOutline,
                Strings.string.mountassistant_confirm_title_error,
                colors.error,
                colors.onError,
            )

        WriteStatus.CANCELLED -> ConfirmHead(
            CmpIcons.Block,
            Strings.string.mountassistant_confirm_title_cancelled,
            colors.surfaceContainerHighest,
            colors.onSurfaceVariant,
        )
    }
}

/** Подтверждение записи «было → станет»: ожидание, PIN, запись, записано, ошибка, отменено. */
@Composable
internal fun ConfirmWriteCard(entry: JournalEntry.Confirm, callbacks: FeedCallbacks) {
    val colors = cmpThemeColors()
    val status = entry.status
    val heavy = status == WriteStatus.PENDING || status == WriteStatus.PIN ||
        status == WriteStatus.WRITING || status == WriteStatus.ERROR
    val head = status.head()
    CardShell(
        modifier = Modifier.testTag(resultTag(entry.id)).alpha(
            if (status ==
                WriteStatus.CANCELLED
            ) {
                CANCELLED_ALPHA
            } else {
                1f
            },
        ),
        heavy = heavy,
        borderColor = if (status == WriteStatus.ERROR) colors.error else colors.primary,
    ) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpSurface(modifier = Modifier.size(40.dp), shape = CircleShape, color = head.background) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpIcon(icon = head.icon, contentDescription = null, tint = head.content)
                }
            }
            CmpText(text = stringResource(head.title), style = CmpTextStyle.Headline1)
        }
        CmpColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = entry.setting.name, style = CmpTextStyle.Paragraph1)
            CmpText(
                text = entry.setting.path.joinToString(" › "),
                style = CmpTextStyle.Paragraph2,
                color = colors.onSurfaceVariant,
            )
        }
        BeforeAfter(entry)
        ConfirmFooter(entry, callbacks)
    }
}

@Composable
private fun BeforeAfter(entry: JournalEntry.Confirm) {
    val colors = cmpThemeColors()
    val cancelled = entry.status == WriteStatus.CANCELLED
    CmpRow(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ValueBox(
            label = stringResource(Strings.string.mountassistant_confirm_label_before),
            value = settingValueText(entry.setting, entry.from),
            background = colors.surfaceContainer,
            content = colors.onSurface,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        CmpIcon(icon = CmpIcons.ArrowForward, contentDescription = null, tint = colors.onSurfaceVariant)
        ValueBox(
            label = stringResource(
                if (entry.status == WriteStatus.DONE) {
                    Strings.string.mountassistant_confirm_label_after_done
                } else {
                    Strings.string.mountassistant_confirm_label_after
                },
            ),
            value = settingValueText(entry.setting, entry.to),
            background = if (cancelled) colors.surfaceContainer else colors.primaryContainer,
            content = if (cancelled) colors.onSurface else colors.onPrimaryContainer,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun ValueBox(label: String, value: String, background: Color, content: Color, modifier: Modifier) {
    CmpColumn(
        modifier = modifier.background(
            background,
            RoundedCornerShape(12.dp),
        ).padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        CmpText(text = label, style = CmpTextStyle.Paragraph2, color = content)
        CmpText(text = value, style = CmpTextStyle.Headline1, color = content)
    }
}

@Composable
private fun ConfirmFooter(entry: JournalEntry.Confirm, callbacks: FeedCallbacks) {
    val colors = cmpThemeColors()
    val muted = colors.onSurfaceVariant
    when (entry.status) {
        WriteStatus.PENDING -> {
            if (entry.otherUnsaved > 0) OtherUnsavedWarning(entry.otherUnsaved)
            if (entry.pinSkipped) Note(stringResource(Strings.string.mountassistant_confirm_msg_pin_skipped), muted)
            Buttons(
                primary = Strings.string.mountassistant_confirm_btn_write,
                onPrimary = { callbacks.onWrite(entry.id) },
                onCancel = { callbacks.onWriteCancel(entry.id) },
            )
        }

        WriteStatus.PIN -> Note(stringResource(Strings.string.mountassistant_confirm_msg_pin), muted)

        WriteStatus.WRITING -> CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Note(stringResource(Strings.string.mountassistant_confirm_msg_writing), muted)
        }

        WriteStatus.ERROR -> {
            Note(writeFailureText(entry.failure), colors.onSurface)
            Buttons(
                primary = Strings.string.mountassistant_confirm_btn_retry,
                onPrimary = { callbacks.onWrite(entry.id) },
                onCancel = { callbacks.onWriteCancel(entry.id) },
            )
        }

        WriteStatus.CANCELLED -> Note(stringResource(Strings.string.mountassistant_confirm_msg_cancelled), muted)

        WriteStatus.DONE -> Unit
    }
}

@Composable
private fun writeFailureText(failure: ResultFailure?): String = when (failure) {
    ResultFailure.LinkLost -> stringResource(Strings.string.mountassistant_confirm_error_link_lost)

    is ResultFailure.Common -> failure.error.message()

    ResultFailure.WrongPin, ResultFailure.Failed, null -> stringResource(
        Strings.string.mountassistant_confirm_error_failed,
    )
}

@Composable
private fun Note(text: String, color: Color) {
    CmpText(text = text, style = CmpTextStyle.Paragraph1, color = color)
}

@Composable
private fun OtherUnsavedWarning(count: Int) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().background(
            colors.tertiaryContainer,
            RoundedCornerShape(12.dp),
        ).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.onTertiaryContainer)
        CmpText(
            text = stringResource(Strings.string.mountassistant_confirm_msg_other_unsaved, count),
            style = CmpTextStyle.Paragraph1,
            color = colors.onTertiaryContainer,
        )
    }
}

@Composable
private fun Buttons(primary: StringResource, onPrimary: () -> Unit, onCancel: () -> Unit) {
    CmpRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CmpButton(
            text = stringResource(primary),
            onClick = onPrimary,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag(CONFIRM_WRITE_TAG),
        )
        CmpOutlinedButton(
            text = stringResource(Strings.string.mountassistant_confirm_btn_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        )
    }
}
