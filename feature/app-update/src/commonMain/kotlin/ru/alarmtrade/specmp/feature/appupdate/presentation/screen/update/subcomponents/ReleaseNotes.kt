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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.appupdate.presentation.model.AppUpdateTags

private const val COLLAPSED_LINES = 6
private const val COLLAPSE_THRESHOLD = COLLAPSED_LINES + 2
private val BulletPattern = Regex("""^\s*[-•]\s+(.*)$""")

/** «Что нового» с сервера — простой текст: «-»/«•» — пункт, пустая строка — отступ, остальное — подзаголовок. */
@Composable
internal fun ReleaseNotes(text: String, expanded: Boolean, onToggle: () -> Unit) {
    val lines = remember(text) { text.lines() }
    val collapsible = lines.size > COLLAPSE_THRESHOLD
    val shown = if (collapsible && !expanded) lines.take(COLLAPSED_LINES) else lines
    val colors = cmpThemeColors()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CmpText(text = stringResource(Strings.string.appupdate_notes_title), style = CmpTextStyle.Headline2)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            shown.forEach { line ->
                val bullet = BulletPattern.find(line)?.groupValues?.get(1)
                when {
                    line.isBlank() -> Spacer(modifier = Modifier.height(6.dp))

                    bullet != null -> Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 9.dp)
                                .size(6.dp)
                                .background(colors.primary, CircleShape),
                        )
                        CmpText(text = bullet, style = CmpTextStyle.Paragraph1)
                    }

                    else -> CmpText(
                        text = line.trim(),
                        style = CmpTextStyle.Headline3,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
        if (collapsible) {
            UpdateButton(
                text = stringResource(
                    if (expanded) {
                        Strings.string.appupdate_notes_btn_collapse
                    } else {
                        Strings.string.appupdate_notes_btn_expand
                    },
                ),
                onClick = onToggle,
                kind = ButtonKind.TEXT,
                icon = if (expanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                modifier = Modifier.testTag(AppUpdateTags.NOTES),
            )
        }
    }
}
