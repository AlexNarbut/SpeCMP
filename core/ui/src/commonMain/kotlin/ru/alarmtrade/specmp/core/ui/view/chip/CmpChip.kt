package ru.alarmtrade.specmp.core.ui.view.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpControlStyle
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val ChipStart = 14.dp
private val ChipEnd = 16.dp
private val ClearEnd = 4.dp

/** Нажатие [onClear] не вызывает [onClick]; [dropdown] — стрелка выпадашки (чип открывает выбор). */
@Composable
fun CmpChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    selected: Boolean = false,
    onClear: (() -> Unit)? = null,
    clearContentDescription: String? = null,
    dropdown: Boolean = false,
) {
    val colors = CmpTheme.colors
    val style = LocalCmpControlStyle.current
    val shape = RoundedCornerShape(CmpRadius.chips)
    val height = CmpSize.chip + style.chipHeight
    val start = max(ChipStart + style.chipPadding, CmpSpacing.space8)
    val end = if (onClear != null) ClearEnd else max(ChipEnd + style.chipPadding, CmpSpacing.space8)
    Surface(
        modifier = modifier.heightIn(min = height),
        shape = shape,
        color = if (selected) colors.backgroundSurfacePrimary else colors.backgroundSurfaceUltraHighlight,
        contentColor = if (selected) colors.foregroundOnLightPrimaryDefault else colors.foregroundOnDarkPrimaryDefault,
        border = if (selected) null else BorderStroke(CmpBorderWidth.chips, colors.borderLight),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(shape)
                    .clickable(role = Role.Button, onClick = onClick)
                    .heightIn(min = height)
                    .padding(start = start, end = end),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let { CmpIcon(icon = it, contentDescription = null, modifier = Modifier.size(20.dp)) }
                CmpText(
                    text = text,
                    style = CmpTextStyle.Headline3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = true,
                    modifier = Modifier.widthIn(max = 280.dp),
                )
                if (dropdown) {
                    CmpIcon(
                        icon = CmpIcons.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconM),
                    )
                }
            }
            if (onClear != null) {
                CmpIconButton(onClick = onClear, icon = CmpIcons.Close, contentDescription = clearContentDescription)
            }
        }
    }
}
