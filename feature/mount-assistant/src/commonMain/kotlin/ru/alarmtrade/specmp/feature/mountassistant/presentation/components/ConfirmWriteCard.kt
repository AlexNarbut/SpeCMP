package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBanner
import ru.alarmtrade.specmp.core.ui.view.banner.CmpBannerTone
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpInfoRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteStatus
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.settingValueText

internal const val CONFIRM_WRITE_TAG = "mountassistant_confirm_write"
private const val CANCELLED_ALPHA = 0.72f
private val ButtonMinWidth = 140.dp

private class ConfirmHead(val icon: CmpIcons, val title: StringResource, val background: Color, val content: Color)

@Composable
private fun WriteStatus.head(): ConfirmHead {
    val colors = CmpTheme.colors
    return when (this) {
        WriteStatus.PENDING -> ConfirmHead(
            CmpIcons.Edit,
            Strings.string.mountassistant_confirm_title_pending,
            colors.brandPrimaryHighlight,
            colors.brandPrimaryDefault,
        )

        WriteStatus.PIN -> ConfirmHead(
            CmpIcons.Pin,
            Strings.string.mountassistant_confirm_title_pin,
            colors.brandPrimaryHighlight,
            colors.brandPrimaryDefault,
        )

        WriteStatus.WRITING -> ConfirmHead(
            CmpIcons.Sync,
            Strings.string.mountassistant_confirm_title_writing,
            colors.brandPrimaryHighlight,
            colors.brandPrimaryDefault,
        )

        WriteStatus.DONE -> ConfirmHead(
            CmpIcons.Check,
            Strings.string.mountassistant_confirm_title_done,
            colors.successHighlight,
            colors.successDefault,
        )

        WriteStatus.ERROR -> ConfirmHead(
            CmpIcons.ErrorOutline,
            Strings.string.mountassistant_confirm_title_error,
            colors.errorHighlight,
            colors.errorDefault,
        )

        WriteStatus.CANCELLED -> ConfirmHead(
            CmpIcons.Block,
            Strings.string.mountassistant_confirm_title_cancelled,
            colors.backgroundSurfaceHighlight,
            colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

/** Подтверждение записи «было → станет»: ожидание, PIN, запись, записано, ошибка, отменено. */
@Composable
internal fun ConfirmWriteCard(entry: JournalEntry.Confirm, callbacks: FeedCallbacks) {
    val colors = CmpTheme.colors
    val status = entry.status
    val heavy = status == WriteStatus.PENDING || status == WriteStatus.PIN ||
        status == WriteStatus.WRITING || status == WriteStatus.ERROR
    val head = status.head()
    CardShell(
        modifier = Modifier.testTag(resultTag(entry.id))
            .alpha(if (status == WriteStatus.CANCELLED) CANCELLED_ALPHA else 1f),
        heavy = heavy,
        borderColor = if (status == WriteStatus.ERROR) colors.errorDefault else colors.brandPrimaryDefault,
    ) {
        CmpRow(
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBox(icon = head.icon, background = head.background, tint = head.content)
            CmpText(text = stringResource(head.title), style = CmpTextStyle.Headline1)
        }
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = entry.setting.name, style = CmpTextStyle.Headline2)
            CmpText(
                text = entry.setting.path.joinToString(" › "),
                style = CmpTextStyle.Paragraph3,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }
        BeforeAfter(entry)
        ConfirmFooter(entry, callbacks)
    }
}

@Composable
private fun BeforeAfter(entry: JournalEntry.Confirm) {
    val colors = CmpTheme.colors
    val cancelled = entry.status == WriteStatus.CANCELLED
    CmpRow(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ValueBox(
            label = stringResource(Strings.string.mountassistant_confirm_label_before),
            value = settingValueText(entry.setting, entry.from),
            background = colors.backgroundSurfaceHighlight,
            accent = false,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        CmpIcon(
            icon = CmpIcons.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(CmpSize.iconM),
            tint = colors.foregroundOnDarkSecondaryDefault,
        )
        ValueBox(
            label = stringResource(
                if (entry.status == WriteStatus.DONE) {
                    Strings.string.mountassistant_confirm_label_after_done
                } else {
                    Strings.string.mountassistant_confirm_label_after
                },
            ),
            value = settingValueText(entry.setting, entry.to),
            background = if (cancelled) colors.backgroundSurfaceHighlight else colors.brandPrimaryHighlight,
            accent = !cancelled,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun ValueBox(label: String, value: String, background: Color, accent: Boolean, modifier: Modifier) {
    CmpInfoRow(
        label = label,
        value = value,
        modifier = modifier.background(background, RoundedCornerShape(CmpRadius.cardSmall))
            .padding(horizontal = CmpSpacing.space12),
        accent = accent,
        last = true,
        compact = true,
    )
}

@Composable
private fun ConfirmFooter(entry: JournalEntry.Confirm, callbacks: FeedCallbacks) {
    val colors = CmpTheme.colors
    val muted = colors.foregroundOnDarkSecondaryDefault
    when (entry.status) {
        WriteStatus.PENDING -> {
            if (entry.otherUnsaved > 0) {
                CmpBanner(
                    text = stringResource(Strings.string.mountassistant_confirm_msg_other_unsaved, entry.otherUnsaved),
                    tone = CmpBannerTone.Warning,
                    icon = CmpIcons.Warning,
                )
            }
            if (entry.pinSkipped) Note(stringResource(Strings.string.mountassistant_confirm_msg_pin_skipped), muted)
            Buttons(
                primary = Strings.string.mountassistant_confirm_btn_write,
                onPrimary = { callbacks.onWrite(entry.id) },
                onCancel = { callbacks.onWriteCancel(entry.id) },
            )
        }

        WriteStatus.PIN -> Note(stringResource(Strings.string.mountassistant_confirm_msg_pin), muted)

        WriteStatus.WRITING -> CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Note(stringResource(Strings.string.mountassistant_confirm_msg_writing), muted)
        }

        WriteStatus.ERROR -> {
            Note(writeFailureText(entry.failure), colors.foregroundOnDarkPrimaryDefault)
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
    CmpText(text = text, style = CmpTextStyle.Paragraph2, color = color)
}

/** Две кнопки в строку, а на узкой карточке — друг под другом: капс не помещается в половину ширины. */
@Composable
private fun Buttons(primary: StringResource, onPrimary: () -> Unit, onCancel: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= ButtonMinWidth * 2 + CmpSpacing.space8) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                WriteButton(primary, onPrimary, Modifier.weight(1f))
                CancelButton(onCancel, Modifier.weight(1f))
            }
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                WriteButton(primary, onPrimary, Modifier.fillMaxWidth())
                CancelButton(onCancel, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun WriteButton(text: StringResource, onClick: () -> Unit, modifier: Modifier) {
    CmpButton(text = stringResource(text), onClick = onClick, modifier = modifier.testTag(CONFIRM_WRITE_TAG))
}

@Composable
private fun CancelButton(onClick: () -> Unit, modifier: Modifier) {
    CmpButton(
        text = stringResource(Strings.string.mountassistant_confirm_btn_cancel),
        onClick = onClick,
        modifier = modifier,
        type = CmpButtonType.Secondary,
    )
}
