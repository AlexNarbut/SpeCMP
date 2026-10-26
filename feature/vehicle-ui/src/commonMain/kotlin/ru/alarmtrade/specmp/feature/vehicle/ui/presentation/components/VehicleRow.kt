package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

/**
 * Строка каталога: [lead] (логотип, фото, иконка), название и подпись, справа шеврон или [trailing].
 * Нажимается вся строка. [action] — кнопка под текстом («Выбрать»), чтобы название не переносилось.
 * [content] — содержимое под строкой (раскрытые комплектации модели).
 */
@Composable
internal fun VehicleRow(
    title: AnnotatedString,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    selected: Boolean = false,
    chevron: Boolean = true,
    dense: Boolean = false,
    lead: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = modifier.fillMaxWidth()) {
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) colors.secondaryContainer else Color.Transparent)
                .clickable(onClick = onClick)
                .heightIn(min = if (dense) 56.dp else 64.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            lead?.invoke()
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = title,
                    style = CmpTextStyle.BodyLarge,
                    color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    CmpText(
                        text = it,
                        style = CmpTextStyle.Body,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                action?.invoke()
            }
            when {
                trailing != null -> trailing()

                chevron -> CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant
                )
            }
        }
        content?.invoke()
    }
}
