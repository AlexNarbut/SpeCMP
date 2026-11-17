package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

@Composable
fun AuthSubmitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    CmpButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = isLoading,
        fullWidth = true,
    )
}

@Composable
fun AuthErrorBanner(text: String, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        color = colors.errorHighlight,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
        border = BorderStroke(CmpBorderWidth.inputs, colors.errorDefault),
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            verticalAlignment = Alignment.Top,
        ) {
            CmpIcon(
                icon = CmpIcons.AlertCircle,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconM),
                tint = colors.errorDefault,
            )
            CmpText(
                text = text,
                style = CmpTextStyle.Paragraph2,
                modifier = Modifier.weight(1f),
                color = colors.foregroundOnDarkPrimaryDefault,
            )
        }
    }
}

@Composable
fun AuthSuccessPanel(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    icon: CmpIcons = CmpIcons.CheckCircle,
    primaryAction: (@Composable () -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CmpSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        CmpBox(
            modifier = Modifier
                .size(SuccessIconCircle)
                .background(colors.brandPrimaryHighlight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.size(CmpSize.iconXl),
                tint = colors.brandPrimaryDefault,
            )
        }
        CmpText(
            text = title,
            style = CmpTextStyle.Headline1,
            color = colors.foregroundOnDarkPrimaryDefault,
            textAlign = TextAlign.Center,
        )
        CmpText(
            text = text,
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnDarkSecondaryDefault,
            textAlign = TextAlign.Center,
        )
        if (primaryAction != null) {
            CmpBox(modifier = Modifier.fillMaxWidth().padding(top = CmpSpacing.space8)) {
                primaryAction()
            }
        }
    }
}

private val SuccessIconCircle = 72.dp
