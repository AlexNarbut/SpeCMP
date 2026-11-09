package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

@Composable
fun AuthSubmitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    colors: CmpThemeColors = cmpThemeColors(),
) {
    CmpButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
    ) {
        if (isLoading) {
            CmpCircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = colors.onPrimary,
            )
        } else {
            CmpText(text = text)
        }
    }
}

@Composable
fun AuthErrorBanner(text: String, modifier: Modifier = Modifier, colors: CmpThemeColors = cmpThemeColors()) {
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.errorContainer,
        contentColor = colors.onErrorContainer,
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.22f)),
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CmpIcon(
                icon = CmpIcons.AlertCircle,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = colors.onErrorContainer,
            )
            CmpText(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = colors.onErrorContainer,
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
    colors: CmpThemeColors = cmpThemeColors(),
    primaryAction: (@Composable () -> Unit)? = null,
) {
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        CmpSurface(
            shape = CircleShape,
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        ) {
            CmpIcon(
                icon = icon,
                contentDescription = null,
                modifier = Modifier.padding(16.dp).size(40.dp),
                tint = colors.onPrimaryContainer,
            )
        }
        CmpText(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        CmpText(
            text = text,
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        primaryAction?.invoke()
    }
}
