@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.view.effect.cmpGlassSurface
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/** Standard — 48 без подложки; Glass — 56, стекло (кнопки топбара, плавающие над контентом). */
enum class CmpIconButtonVariant(internal val size: Dp) {
    Standard(CmpSize.touchMin),
    Glass(CmpSize.iconXxl),
}

@Composable
fun CmpIconButton(
    onClick: () -> Unit,
    icon: CmpIcons,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    variant: CmpIconButtonVariant = CmpIconButtonVariant.Standard,
    selected: Boolean = false,
) {
    IconButtonFrame(onClick, modifier, enabled, variant, selected) { color ->
        CmpIcon(icon = icon, contentDescription = contentDescription, tint = tint.takeOrElse(color))
    }
}

@Composable
fun CmpIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    variant: CmpIconButtonVariant = CmpIconButtonVariant.Standard,
    selected: Boolean = false,
) {
    IconButtonFrame(onClick, modifier, enabled, variant, selected) { color ->
        CmpIcon(imageVector = icon, contentDescription = contentDescription, tint = tint.takeOrElse(color))
    }
}

@Composable
private fun IconButtonFrame(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    variant: CmpIconButtonVariant,
    selected: Boolean,
    icon: @Composable (Color) -> Unit,
) {
    val colors = CmpTheme.colors
    val surface = when {
        selected -> Modifier.background(colors.backgroundSurfacePrimary, CircleShape)
        variant == CmpIconButtonVariant.Glass -> Modifier.cmpGlassSurface(state = null, shape = CircleShape)
        else -> Modifier
    }
    val color = when {
        !enabled -> colors.foregroundOnDarkPrimaryDisabled
        selected -> colors.foregroundOnLightPrimaryDefault
        else -> Color.Unspecified
    }
    Box(
        modifier = modifier
            .size(variant.size)
            .cmpPressable(onClick = onClick, enabled = enabled, shape = CircleShape, role = Role.Button)
            .then(surface),
        contentAlignment = Alignment.Center,
    ) {
        icon(color)
    }
}

private fun Color.takeOrElse(fallback: Color): Color = if (this != Color.Unspecified) this else fallback
