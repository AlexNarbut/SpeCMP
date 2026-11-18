package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeColor
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val SelectedBorder = 2.dp
private val SwatchSize = 40.dp

@Composable
internal fun SectionCard(title: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    CmpCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            if (title != null) {
                CmpText(
                    text = title,
                    style = CmpTextStyle.Headline2,
                    color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
                )
            }
            content()
        }
    }
}

@Composable
internal fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    CmpText(
        text = text,
        style = CmpTextStyle.Headline3,
        color = CmpTheme.colors.foregroundOnDarkPrimaryDefault,
        modifier = modifier,
    )
}

@Composable
internal fun Hint(text: String, modifier: Modifier = Modifier) {
    CmpText(
        text = text,
        style = CmpTextStyle.Paragraph3,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        modifier = modifier,
    )
}

/** Плитка выбора варианта: выбранная — рамка акцента; недоступная — приглушена и не нажимается. */
@Composable
internal fun OptionTile(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    horizontalPadding: Dp = CmpSpacing.space12,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CmpTheme.colors
    CmpClickableCard(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = CmpSize.touchMin)
            .semantics {
                this.selected = selected
                role = Role.RadioButton
            },
        shape = RoundedCornerShape(CmpRadius.cardSmall),
        border = if (selected) {
            BorderStroke(SelectedBorder, colors.brandPrimaryDefault)
        } else {
            BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding, vertical = CmpSpacing.space12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            content = content,
        )
    }
}

/** Образец цвета пользовательской темы; с [onClick] — кнопка 48 dp, выбранный — с кольцом акцента. */
@Composable
internal fun ColorSwatch(
    color: ThemeColor,
    modifier: Modifier = Modifier,
    size: Dp = SwatchSize,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val swatch = Modifier
        .size(size)
        .background(color.toColor(), CircleShape)
        .cmpBorder(CmpBorderWidth.listItem, colors.borderMediumHighlight, CircleShape)
    if (onClick == null) {
        Box(modifier = modifier.then(swatch))
        return
    }
    Box(
        modifier = modifier
            .size(CmpSize.touchMin + CmpSpacing.space4)
            .cmpPressable(onClick = onClick, shape = CircleShape, role = Role.RadioButton)
            .semantics {
                contentDescription = color.hex
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(CmpSize.touchMin + CmpSpacing.space4)
                .cmpBorder(if (selected) SelectedBorder else 0.dp, colors.brandPrimaryDefault, CircleShape),
        )
        Box(modifier = swatch)
    }
}

@Composable
internal fun ValueRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FieldLabel(label)
        CmpText(text = value, style = CmpTextStyle.Headline3, color = CmpTheme.colors.brandPrimaryDefault)
    }
}

/** Размеры отступов из макета: высота и поля по бокам. */
internal class ControlDims(val height: Int, val side: Int)
