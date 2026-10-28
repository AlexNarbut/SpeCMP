package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.LocalAppLocale
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_common_a11y_language_en
import ru.alarmtrade.specmp.core.localization.resources.login_common_a11y_language_ru
import ru.alarmtrade.specmp.core.localization.resources.login_common_btn_language_en
import ru.alarmtrade.specmp.core.localization.resources.login_common_btn_language_ru
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

@Composable
fun AuthLanguageSegment(
    onLocaleChange: (AppLocale) -> Unit,
    modifier: Modifier = Modifier,
    colors: CmpThemeColors = cmpThemeColors()
) {
    val appLocale = LocalAppLocale.current

    CmpSurface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surfaceVariant,
        contentColor = colors.onSurfaceVariant
    ) {
        CmpRow(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuthLanguageSegmentItem(
                text = stringResource(Strings.string.login_common_btn_language_en),
                contentDescription = stringResource(Strings.string.login_common_a11y_language_en),
                selected = appLocale == AppLocale.English,
                colors = colors,
                onClick = { onLocaleChange(AppLocale.English) }
            )
            AuthLanguageSegmentItem(
                text = stringResource(Strings.string.login_common_btn_language_ru),
                contentDescription = stringResource(Strings.string.login_common_a11y_language_ru),
                selected = appLocale == AppLocale.Russian,
                colors = colors,
                onClick = { onLocaleChange(AppLocale.Russian) }
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
    onClick: () -> Unit
) {
    // Размеры кнопки (38dp) заданы дизайном segmented control;
    // стандартная M3-инфляция до 48dp здесь отключена, иначе трек раздувается.
    CmpClickableSurface(
        modifier = Modifier.semantics {
            this.contentDescription = contentDescription
            this.selected = selected
        },
        onClick = {
            if (!selected) onClick()
        },
        shape = CircleShape,
        color = if (selected) colors.primary else Color.Transparent,
        contentColor = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        enforceMinimumTouchTarget = false
    ) {
        CmpBox(
            modifier = Modifier
                .heightIn(min = 38.dp)
                .widthIn(min = 46.dp)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            CmpText(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
