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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.input.hideKeyboardOnScroll
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface

@Composable
fun AuthBody(
    layoutMode: AuthLayoutMode,
    modifier: Modifier = Modifier,
    showCompactBrand: Boolean = true,
    showExpandedBrandRail: Boolean = true,
    error: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    // Плашка ошибки висит над формой внизу экрана: область формы уменьшается на её высоту, чтобы кнопку
    // можно было прокрутить выше плашки, а не прятать под ней.
    val density = LocalDensity.current
    var bannerHeight by remember { mutableStateOf(0.dp) }
    val formInset = Modifier.padding(bottom = if (error != null) bannerHeight + ErrorBannerGap else 0.dp)
    CmpBox(modifier = modifier.fillMaxSize()) {
        when (layoutMode) {
            AuthLayoutMode.Compact -> AuthCompactBody(
                modifier = formInset,
                showBrand = showCompactBrand,
                content = content,
            )

            AuthLayoutMode.Medium -> AuthMediumBody(
                modifier = formInset,
                showBrand = showCompactBrand,
                content = content,
            )

            AuthLayoutMode.Expanded -> AuthExpandedBody(
                modifier = formInset,
                showBrandRail = showExpandedBrandRail,
                content = content,
            )
        }

        AuthErrorOverlay(
            error = error,
            modifier = Modifier.align(Alignment.BottomCenter),
            onBannerHeight = { bannerHeight = with(density) { it.toDp() } },
        )
    }
}

/**
 * Ошибка отправки формы — поверх контента внизу экрана, вне потока формы: карточка центрирована,
 * и баннер внутри неё сдвигал бы поля и кнопку.
 */
@Composable
private fun AuthErrorOverlay(error: String?, onBannerHeight: (Int) -> Unit, modifier: Modifier = Modifier) {
    // AnimatedContent держит прежний текст, пока баннер уезжает.
    AnimatedContent(
        targetState = error,
        modifier = modifier
            .imePadding()
            .navigationBarsPadding()
            .padding(ErrorBannerGap)
            .widthIn(max = 460.dp)
            .onSizeChanged { onBannerHeight(it.height) },
        transitionSpec = {
            (fadeIn() + slideInVertically { it / 2 }) togetherWith (fadeOut() + slideOutVertically { it / 2 })
        },
    ) { text ->
        if (text != null) AuthErrorBanner(text = text)
    }
}

@Composable
private fun AuthCompactBody(modifier: Modifier, showBrand: Boolean, content: @Composable BoxScope.() -> Unit) {
    CmpColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .hideKeyboardOnScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showBrand) {
            AuthBrandLockup(
                layoutMode = AuthLayoutMode.Compact,
                modifier = Modifier.padding(bottom = CmpSpacing.space24),
            )
        }

        AuthFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            content = content,
        )
    }
}

@Composable
private fun AuthMediumBody(modifier: Modifier, showBrand: Boolean, content: @Composable BoxScope.() -> Unit) {
    CmpColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .hideKeyboardOnScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CmpSpacing.screenPaddingHMedium, vertical = CmpSpacing.space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showBrand) {
            AuthBrandLockup(
                layoutMode = AuthLayoutMode.Medium,
                modifier = Modifier.padding(bottom = CmpSpacing.space32),
            )
        }

        AuthFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            content = content,
        )
    }
}

@Composable
private fun AuthExpandedBody(modifier: Modifier, showBrandRail: Boolean, content: @Composable BoxScope.() -> Unit) {
    CmpRow(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .hideKeyboardOnScroll()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CmpSpacing.space56, vertical = CmpSpacing.space32),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(64.dp, Alignment.CenterHorizontally),
    ) {
        if (showBrandRail) {
            AuthBrandLockup(
                layoutMode = AuthLayoutMode.Expanded,
                modifier = Modifier.widthIn(max = 420.dp).weight(1f),
            )
        }

        AuthFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .weight(1f),
            content = content,
        )
    }
}

@Composable
private fun AuthFormCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = CmpTheme.colors
    CmpSurface(
        modifier = modifier,
        shape = RoundedCornerShape(CmpRadius.cardLarge),
        color = colors.backgroundSurfaceCard,
        contentColor = colors.foregroundOnDarkPrimaryDefault,
        border = BorderStroke(CmpBorderWidth.inputs, colors.borderHighlight),
    ) {
        CmpBox(
            modifier = Modifier.padding(CmpSpacing.space24),
            content = content,
        )
    }
}

private val ErrorBannerGap = CmpSpacing.space24
