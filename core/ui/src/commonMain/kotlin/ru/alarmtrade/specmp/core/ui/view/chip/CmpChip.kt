package ru.alarmtrade.specmp.core.ui.view.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

/** Нажатие [onClear] не вызывает [onClick]. */
@Composable
fun CmpChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
    selected: Boolean = false,
    onClear: (() -> Unit)? = null,
    clearContentDescription: String? = null,
) {
    val colors = CmpTheme.colors
    Surface(
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        color = if (selected) colors.backgroundSurfacePrimary else colors.backgroundSurfaceUltraHighlight,
        contentColor = if (selected) colors.foregroundOnLightPrimaryDefault else colors.foregroundOnDarkPrimaryDefault,
        border = if (selected) null else BorderStroke(CmpBorderWidth.chips, colors.borderLight),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onClick)
                    .heightIn(min = 48.dp)
                    .padding(start = 14.dp, end = if (onClear != null) 4.dp else 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let { CmpIcon(icon = it, contentDescription = null, modifier = Modifier.size(20.dp)) }
                Text(
                    text = text,
                    style = CmpTextStyle.Headline3.toTextStyle(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 280.dp),
                )
            }
            if (onClear != null) {
                CmpIconButton(onClick = onClear, icon = CmpIcons.Close, contentDescription = clearContentDescription)
            }
        }
    }
}
