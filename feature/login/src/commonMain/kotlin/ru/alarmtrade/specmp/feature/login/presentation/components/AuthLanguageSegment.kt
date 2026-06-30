package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.login.presentation.locale.AppLocale
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.login_common_a11y_language_en
import specmp.feature.login.generated.resources.login_common_a11y_language_ru
import specmp.feature.login.generated.resources.login_common_btn_language_en
import specmp.feature.login.generated.resources.login_common_btn_language_ru

@Composable
fun AuthLanguageSegment(
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
    modifier: Modifier = Modifier,
    colors: CmpThemeColors = cmpThemeColors(),
) {
    CmpSurface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surfaceVariant,
        contentColor = colors.onSurfaceVariant,
    ) {
        CmpRow(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthLanguageSegmentItem(
                text = stringResource(Res.string.login_common_btn_language_en),
                contentDescription = stringResource(Res.string.login_common_a11y_language_en),
                selected = appLocale == AppLocale.English,
                colors = colors,
                onClick = { onLocaleChanged(AppLocale.English) },
            )
            AuthLanguageSegmentItem(
                text = stringResource(Res.string.login_common_btn_language_ru),
                contentDescription = stringResource(Res.string.login_common_a11y_language_ru),
                selected = appLocale == AppLocale.Russian,
                colors = colors,
                onClick = { onLocaleChanged(AppLocale.Russian) },
            )
        }
    }
}

@Composable
private fun AuthLanguageSegmentItem(
    text: String,
    contentDescription: String,
    selected: Boolean,
    colors: CmpThemeColors,
    onClick: () -> Unit,
) {
    CmpClickableSurface(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            .semantics {
                this.contentDescription = contentDescription
                this.selected = selected
            },
        onClick = {
            if (!selected) onClick()
        },
        shape = CircleShape,
        color = if (selected) colors.primary else colors.surfaceVariant,
        contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
    ) {
        CmpText(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
