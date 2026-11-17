package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_notes_btn_collapse
import ru.alarmtrade.specmp.core.localization.resources.appupdate_notes_btn_expand
import ru.alarmtrade.specmp.core.localization.resources.appupdate_notes_title
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags

private const val COLLAPSED_LINES = 6
private const val COLLAPSE_THRESHOLD = COLLAPSED_LINES + 2
private val BulletSize = 6.dp
private val BulletTop = 9.dp
private val BlankLine = 6.dp
private val LineGap = 6.dp
private val BulletPattern = Regex("""^\s*[-•]\s+(.*)$""")

/** «Что нового» с сервера — простой текст: «-»/«•» — пункт, пустая строка — отступ, остальное — подзаголовок. */
@Composable
internal fun ReleaseNotes(text: String, expanded: Boolean, onToggle: () -> Unit) {
    val lines = remember(text) { text.lines() }
    val collapsible = lines.size > COLLAPSE_THRESHOLD
    val shown = if (collapsible && !expanded) lines.take(COLLAPSED_LINES) else lines
    val colors = CmpTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpText(text = stringResource(Strings.string.appupdate_notes_title), style = CmpTextStyle.Headline2)
        Column(verticalArrangement = Arrangement.spacedBy(LineGap)) {
            shown.forEach { line ->
                val bullet = BulletPattern.find(line)?.groupValues?.get(1)
                when {
                    line.isBlank() -> Spacer(modifier = Modifier.height(BlankLine))

                    bullet != null -> Row(
                        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = BulletTop)
                                .size(BulletSize)
                                .background(colors.brandPrimaryDefault, CircleShape),
                        )
                        CmpText(text = bullet, style = CmpTextStyle.Paragraph1)
                    }

                    else -> CmpText(
                        text = line.trim(),
                        style = CmpTextStyle.Headline3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
        }
        if (collapsible) {
            CmpButton(
                text = stringResource(
                    if (expanded) {
                        Strings.string.appupdate_notes_btn_collapse
                    } else {
                        Strings.string.appupdate_notes_btn_expand
                    },
                ),
                onClick = onToggle,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                icon = if (expanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                modifier = Modifier.testTag(AppUpdateTags.NOTES),
            )
        }
    }
}
