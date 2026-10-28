package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.login_authroot_subtitle_specialist
import ru.alarmtrade.specmp.core.localization.resources.login_authroot_title_brand
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.image.CmpImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.app_icon

@Composable
fun AuthBrandHeader(colors: CmpThemeColors, modifier: Modifier = Modifier) {
    CmpColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CmpImage(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )

        CmpText(
            text = stringResource(Strings.string.login_authroot_title_brand),
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onBackground,
            textAlign = TextAlign.Center
        )

        CmpText(
            text = stringResource(Strings.string.login_authroot_subtitle_specialist),
            fontSize = 16.sp,
            color = colors.onBackground.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
    }
}
