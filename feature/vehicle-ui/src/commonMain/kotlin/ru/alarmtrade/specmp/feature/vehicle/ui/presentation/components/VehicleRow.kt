package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal val RowMinHeight = 64.dp

/**
 * Строка каталога в виде `CmpListItem`, но с подсветкой поиска в заголовке и картинкой слева;
 * [action] — кнопка под текстом, чтобы название не переносилось.
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
    content: (@Composable () -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.listItem)
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (dense) RowMinHeight else CmpSize.listItem)
                .cmpPressable(onClick = onClick, shape = shape)
                .background(
                    if (selected) colors.backgroundSurfaceHighlight else colors.backgroundSurfaceUltraHighlight,
                    shape,
                )
                .border(
                    CmpBorderWidth.listItem,
                    if (selected) colors.borderLight else colors.borderHighlight,
                    shape,
                )
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            lead?.invoke()
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = title,
                    style = CmpTextStyle.Headline2,
                    color = colors.foregroundOnDarkPrimaryDefault,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    CmpText(
                        text = it,
                        style = CmpTextStyle.Paragraph3,
                        color = colors.foregroundOnDarkSecondaryDefault,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                action?.invoke()
            }
            when {
                trailing != null -> trailing()

                chevron && action == null -> CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(CmpSize.iconL),
                )
            }
        }
        content?.invoke()
    }
}
