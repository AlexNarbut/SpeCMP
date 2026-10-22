@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/** Элемент хлебных крошек; [marked] — показать точку «есть изменения». */
data class CmpBreadcrumb(val label: String, val marked: Boolean = false)

private const val COLLAPSED_MARK = "…"

/**
 * Хлебные крошки: путь от корня к текущему месту. Последний элемент — текущий (не нажимается), остальные —
 * переход по [onItemClick] с индексом в [items]. Если элементов больше [maxVisible] — начало сворачивается в «…».
 * Строка прокручивается по горизонтали и держит конец пути в виде; высота элемента — 48 dp.
 */
@Composable
fun CmpBreadcrumbs(
    items: List<CmpBreadcrumb>,
    onItemClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3
) {
    val collapsed = items.size > maxVisible
    val firstShown = if (collapsed) items.size - (maxVisible - 1) else 0
    val scroll = rememberScrollState()
    LaunchedEffect(items.size) { scroll.scrollTo(scroll.maxValue) }

    Row(
        modifier = modifier.horizontalScroll(scroll),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (collapsed) {
            Crumb(text = COLLAPSED_MARK, current = false, marked = false, onClick = null)
        }
        for (index in firstShown until items.size) {
            if (index > 0 || collapsed) Chevron()
            val item = items[index]
            val current = index == items.lastIndex
            Crumb(
                text = item.label,
                current = current,
                marked = item.marked,
                onClick = if (current) null else ({ onItemClick(index) })
            )
        }
    }
}

@Composable
private fun Chevron() {
    CmpIcon(
        icon = CmpIcons.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun Crumb(text: String, current: Boolean, marked: Boolean, onClick: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (current) FontWeight.SemiBold else FontWeight.Medium,
            color = if (current || onClick == null) colors.onSurface else colors.primary,
            maxLines = 1
        )
        if (marked) CmpDot(size = 8.dp)
    }
}
