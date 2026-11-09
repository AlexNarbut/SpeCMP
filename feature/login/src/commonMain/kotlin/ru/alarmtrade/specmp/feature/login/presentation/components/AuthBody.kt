package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface

@Composable
fun AuthBody(
    layoutMode: AuthLayoutMode,
    modifier: Modifier = Modifier,
    colors: CmpThemeColors = cmpThemeColors(),
    showCompactBrand: Boolean = true,
    showExpandedBrandRail: Boolean = true,
    error: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    CmpBox(modifier = modifier.fillMaxSize()) {
        when (layoutMode) {
            AuthLayoutMode.Compact -> AuthCompactBody(
                modifier = Modifier,
                colors = colors,
                showBrand = showCompactBrand,
                content = content,
            )

            AuthLayoutMode.Medium -> AuthMediumBody(
                modifier = Modifier,
                colors = colors,
                showBrand = showCompactBrand,
                content = content,
            )

            AuthLayoutMode.Expanded -> AuthExpandedBody(
                modifier = Modifier,
                colors = colors,
                showBrandRail = showExpandedBrandRail,
                content = content,
            )
        }

        AuthErrorOverlay(
            error = error,
            colors = colors,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Ошибка отправки формы — поверх контента внизу экрана, вне потока формы: карточка центрирована,
 * и баннер внутри неё сдвигал бы поля и кнопку.
 */
@Composable
private fun AuthErrorOverlay(error: String?, colors: CmpThemeColors, modifier: Modifier = Modifier) {
    // AnimatedContent держит прежний текст, пока баннер уезжает.
    AnimatedContent(
        targetState = error,
        modifier = modifier
            .imePadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .widthIn(max = 460.dp),
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 2 }) togetherWith (fadeOut() + slideOutVertically { it / 2 })
        },
    ) { text ->
        if (text != null) AuthErrorBanner(text = text, colors = colors)
    }
}

@Composable
private fun AuthCompactBody(
    modifier: Modifier,
    colors: CmpThemeColors,
    showBrand: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    CmpColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .hideKeyboardOnScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showBrand) {
            AuthBrandLockup(
                layoutMode = AuthLayoutMode.Compact,
                colors = colors,
                modifier = Modifier.padding(bottom = 28.dp),
            )
        }

        AuthFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            colors = colors,
            content = content,
        )
    }
}

@Composable
private fun AuthMediumBody(
    modifier: Modifier,
    colors: CmpThemeColors,
    showBrand: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    CmpColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .hideKeyboardOnScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showBrand) {
            AuthBrandLockup(
                layoutMode = AuthLayoutMode.Medium,
                colors = colors,
                modifier = Modifier.padding(bottom = 32.dp),
            )
        }

        AuthFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            colors = colors,
            content = content,
        )
    }
}

@Composable
private fun AuthExpandedBody(
    modifier: Modifier,
    colors: CmpThemeColors,
    showBrandRail: Boolean,
    content: @Composable BoxScope.() -> Unit,
) {
    CmpRow(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .hideKeyboardOnScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 56.dp, vertical = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(64.dp, Alignment.CenterHorizontally),
    ) {
        if (showBrandRail) {
            AuthBrandLockup(
                layoutMode = AuthLayoutMode.Expanded,
                colors = colors,
                modifier = Modifier.widthIn(max = 420.dp).weight(1f),
            )
        }

        AuthFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .weight(1f),
            colors = colors,
            content = content,
        )
    }
}

@Composable
private fun AuthFormCard(
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    CmpSurface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        contentColor = colors.onSurface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpBox(
            modifier = Modifier.padding(AuthCardPadding),
            content = content,
        )
    }
}

private val AuthCardPadding = PaddingValues(28.dp)
