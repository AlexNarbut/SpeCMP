@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/** Тон блока состояния: ошибка (error-container) или нейтральное (пусто, ничего не найдено). */
enum class CmpStateTone { Neutral, Error }

/**
 * Блок состояния вместо содержимого: иконка в круге, заголовок, пояснение и действия ([actions]: «Повторить»,
 * «Сбросить фильтр»…) по центру, ширина до 420 dp. [compact] — меньше отступы и иконка (колонки широкого окна).
 * Ошибка объявляется экранным диктором (live region).
 */
@Composable
fun CmpStateBox(
    icon: CmpIcons,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    tone: CmpStateTone = CmpStateTone.Neutral,
    compact: Boolean = false,
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val error = tone == CmpStateTone.Error
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(horizontal = if (compact) 16.dp else 20.dp, vertical = if (compact) 24.dp else 40.dp)
                .then(if (error) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 64.dp else 80.dp)
                    .background(if (error) colors.errorContainer else colors.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CmpIcon(
                    icon = icon,
                    contentDescription = null,
                    tint = if (error) colors.onErrorContainer else colors.onSurfaceVariant,
                    modifier = Modifier.size(if (compact) 32.dp else 40.dp)
                )
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            actions()
        }
    }
}
