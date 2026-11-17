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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

enum class CmpStateTone { Neutral, Error }

@Composable
fun CmpStateBox(
    icon: CmpIcons,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    tone: CmpStateTone = CmpStateTone.Neutral,
    compact: Boolean = false,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    val colors = CmpTheme.colors
    val error = tone == CmpStateTone.Error
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(horizontal = if (compact) 16.dp else 20.dp, vertical = if (compact) 24.dp else 40.dp)
                .then(if (error) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 64.dp else 80.dp)
                    .background(if (error) colors.errorHighlight else colors.backgroundSurfaceHighlight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = icon,
                    contentDescription = null,
                    tint = if (error) colors.errorDefault else colors.foregroundOnDarkSecondaryDefault,
                    modifier = Modifier.size(if (compact) 32.dp else 40.dp),
                )
            }
            CmpText(
                text = title,
                style = if (compact) CmpTextStyle.Headline1 else CmpTextStyle.Title2,
                textAlign = TextAlign.Center,
            )
            text?.let {
                CmpText(
                    text = it,
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    textAlign = TextAlign.Center,
                )
            }
            actions()
        }
    }
}
