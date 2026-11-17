package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_label_installed
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_label_new
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val PillShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardSmall)
private val PillMinWidth = 120.dp
private val SpinnerStroke = 3.dp
private const val DAY_MONTH_DIGITS = 2

internal enum class HeadTone { PRIMARY, NEUTRAL, SUCCESS, ERROR }

@Composable
internal fun UpdateCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(CmpSpacing.space24),
    spacing: Dp = CmpSpacing.space20,
    content: @Composable ColumnScope.() -> Unit,
) {
    CmpCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(spacing)) {
            content()
        }
    }
}

@Composable
internal fun StageHead(title: String, icon: CmpIcons?, tone: HeadTone, subtitle: String? = null) {
    val colors = CmpTheme.colors
    val (background, content) = when (tone) {
        HeadTone.PRIMARY -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault
        HeadTone.NEUTRAL -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkSecondaryDefault
        HeadTone.SUCCESS -> colors.successHighlight to colors.successDefault
        HeadTone.ERROR -> colors.errorHighlight to colors.errorDefault
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(CmpSize.iconXxl).background(background, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (icon == null) {
                CmpCircularProgressIndicator(
                    modifier = Modifier.size(CmpSize.iconL),
                    strokeWidth = SpinnerStroke,
                    color = content,
                )
            } else {
                CmpIcon(icon = icon, contentDescription = null, tint = content, modifier = Modifier.size(CmpSize.iconL))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(text = title, style = CmpTextStyle.Headline1)
            subtitle?.let {
                CmpText(text = it, style = CmpTextStyle.Paragraph1, color = colors.foregroundOnDarkSecondaryDefault)
            }
        }
    }
}

@Composable
internal fun VersionPills(installed: String, new: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VersionPill(stringResource(Strings.string.appupdate_release_label_installed), installed, accent = false)
        CmpIcon(
            icon = CmpIcons.ArrowForward,
            contentDescription = null,
            tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconM),
        )
        VersionPill(stringResource(Strings.string.appupdate_release_label_new), new, accent = true)
    }
}

@Composable
private fun VersionPill(label: String, value: String, accent: Boolean) {
    val colors = CmpTheme.colors
    val background = if (accent) colors.brandPrimaryHighlight else colors.backgroundSurfaceUltraHighlight
    val content = if (accent) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkSecondaryDefault
    Column(
        modifier = Modifier
            .widthIn(min = PillMinWidth)
            .background(background, PillShape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, PillShape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        CmpText(text = label, style = CmpTextStyle.Additional0, color = content)
        CmpText(text = value, style = CmpTextStyle.Headline2, color = content)
    }
}

@Composable
internal fun InfoNote(text: String, icon: CmpIcons = CmpIcons.Info) {
    val colors = CmpTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceUltraHighlight, PillShape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, PillShape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            tint = colors.foregroundOnDarkSecondaryDefault,
            modifier = Modifier.size(CmpSize.iconM),
        )
        CmpText(text = text, style = CmpTextStyle.Paragraph1, color = colors.foregroundOnDarkPrimaryDefault)
    }
}

internal fun LocalDate.formatted(): String = "${dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
    "${monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}.$year"
