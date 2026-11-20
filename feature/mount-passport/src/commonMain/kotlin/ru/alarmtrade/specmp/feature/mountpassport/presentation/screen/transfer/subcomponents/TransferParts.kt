package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_a11y_locked
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSwitch
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val PartMinHeight = 64.dp
private val ProgressMaxWidth = 360.dp
private val StatusIconSize = 56.dp

/** Строка состава архива: всегда в архиве ([onToggle] = `null`, замок) или переключатель. */
@Composable
internal fun ArchivePart(
    icon: CmpIcons,
    title: String,
    subtitle: String,
    on: Boolean,
    onToggle: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    file: String? = null,
) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    val primary = CmpTheme.colors.foregroundOnDarkPrimaryDefault
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = PartMinHeight).padding(vertical = CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = if (on) primary else muted)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = title, style = CmpTextStyle.Headline3, color = if (on) primary else muted)
            CmpText(text = subtitle, style = CmpTextStyle.Paragraph3, color = muted)
            file?.let { CmpText(text = it, style = CmpTextStyle.Additional1, color = muted) }
        }
        if (onToggle == null) {
            CmpIcon(
                icon = CmpIcons.Lock,
                contentDescription = stringResource(Strings.string.mountpassport_transfer_a11y_locked),
                tint = muted,
                modifier = Modifier.size(CmpSize.iconM),
            )
        } else {
            CmpSwitch(checked = on, onCheckedChange = onToggle)
        }
    }
}

internal enum class StatusMark { PROGRESS, DONE, ERROR }

/** Крупный статус по центру: спиннер / галочка / ошибка, заголовок, подпись и (если есть) полоса прогресса. */
@Composable
internal fun TransferStatus(
    mark: StatusMark,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    progress: Float? = null,
) {
    val colors = CmpTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = CmpSpacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        when (mark) {
            StatusMark.PROGRESS -> CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconXl))

            StatusMark.DONE, StatusMark.ERROR -> Box(
                modifier = Modifier
                    .size(StatusIconSize)
                    .clip(CircleShape)
                    .background(if (mark == StatusMark.DONE) colors.successDefault else colors.errorDefault),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = if (mark == StatusMark.DONE) CmpIcons.Check else CmpIcons.PriorityHigh,
                    contentDescription = null,
                    tint = colors.staticWhite,
                    modifier = Modifier.size(CmpSize.iconL),
                )
            }
        }
        CmpText(
            text = title,
            style = CmpTextStyle.Headline2,
            color = colors.foregroundOnDarkPrimaryDefault,
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            CmpText(
                text = it,
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
        if (progress != null) {
            CmpLinearProgressIndicator(
                progress = progress,
                modifier = Modifier.widthIn(max = ProgressMaxWidth).fillMaxWidth(),
            )
        }
    }
}

/** Плашка-ошибка: иконка, заголовок, пояснение. */
@Composable
internal fun ErrorNote(title: String, text: String, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CmpRadius.cardSmall))
            .background(colors.errorHighlight)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.ErrorOutline, contentDescription = null, tint = colors.errorDefault)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = title, style = CmpTextStyle.Headline3, color = colors.foregroundOnDarkPrimaryDefault)
            CmpText(text = text, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkSecondaryDefault)
        }
    }
}
