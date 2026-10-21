package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.image.CmpImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import specmp.feature.login.generated.resources.Res
import specmp.feature.login.generated.resources.app_icon
import specmp.feature.login.generated.resources.login_authroot_subtitle_specialist
import specmp.feature.login.generated.resources.login_authroot_title_brand

@Composable
fun AuthBrandLockup(
    layoutMode: AuthLayoutMode,
    modifier: Modifier = Modifier,
    colors: CmpThemeColors = cmpThemeColors()
) {
    val isExpanded = layoutMode == AuthLayoutMode.Expanded
    val iconSize = if (isExpanded) 84.dp else 60.dp
    val titleSize = if (isExpanded) 34.sp else 28.sp
    val gap = if (isExpanded) 18.dp else 12.dp

    CmpColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        CmpImage(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = stringResource(Res.string.login_authroot_title_brand),
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = 10.dp,
                    ambientColor = colors.primary.copy(alpha = 0.25f),
                    spotColor = colors.primary.copy(alpha = 0.35f)
                )
        )

        CmpColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CmpText(
                text = stringResource(Res.string.login_authroot_title_brand),
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )

            CmpText(
                text = stringResource(Res.string.login_authroot_subtitle_specialist),
                fontSize = 16.sp,
                color = colors.onBackground.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )
        }
    }
}
