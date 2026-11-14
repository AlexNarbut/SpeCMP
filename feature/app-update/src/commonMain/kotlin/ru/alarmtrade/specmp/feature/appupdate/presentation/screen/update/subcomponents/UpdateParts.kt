package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_label_installed
import ru.alarmtrade.specmp.core.localization.resources.appupdate_release_label_new
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val CardShape = RoundedCornerShape(20.dp)
private val TileShape = RoundedCornerShape(16.dp)
private val PillShape = RoundedCornerShape(12.dp)
private const val SUCCESS_TILE_ALPHA = 0.18f
private const val DAY_MONTH_DIGITS = 2

internal val ButtonHeight = 48.dp

internal enum class HeadTone { PRIMARY, NEUTRAL, SUCCESS, ERROR }

@Composable
internal fun UpdateCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(24.dp),
    spacing: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(padding), verticalArrangement = Arrangement.spacedBy(spacing)) {
            content()
        }
    }
}

/** Плитка 56 dp с иконкой (или спиннером) и заголовок с подписью — шапка главной карточки. */
@Composable
internal fun StageHead(title: String, icon: CmpIcons?, tone: HeadTone, subtitle: String? = null) {
    val colors = cmpThemeColors()
    val success = cmpDomainColors().success
    val (background, content) = when (tone) {
        HeadTone.PRIMARY -> colors.primaryContainer to colors.onPrimaryContainer
        HeadTone.NEUTRAL -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        HeadTone.SUCCESS -> success.copy(alpha = SUCCESS_TILE_ALPHA) to success
        HeadTone.ERROR -> colors.errorContainer to colors.onErrorContainer
    }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(56.dp).background(background, TileShape),
            contentAlignment = Alignment.Center,
        ) {
            if (icon == null) {
                CmpCircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = content)
            } else {
                CmpIcon(icon = icon, contentDescription = null, tint = content, modifier = Modifier.size(30.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = title, style = CmpTextStyle.Headline)
            subtitle?.let { CmpText(text = it, style = CmpTextStyle.BodyLarge, color = colors.onSurfaceVariant) }
        }
    }
}

@Composable
internal fun VersionPills(installed: String, new: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        VersionPill(stringResource(Strings.string.appupdate_release_label_installed), installed, accent = false)
        CmpIcon(
            icon = CmpIcons.ArrowForward,
            contentDescription = null,
            tint = cmpThemeColors().onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        VersionPill(stringResource(Strings.string.appupdate_release_label_new), new, accent = true)
    }
}

@Composable
private fun VersionPill(label: String, value: String, accent: Boolean) {
    val colors = cmpThemeColors()
    val background = if (accent) colors.primaryContainer else colors.surfaceVariant
    val content = if (accent) colors.onPrimaryContainer else colors.onSurfaceVariant
    Column(
        modifier = Modifier
            .widthIn(min = 120.dp)
            .background(background, PillShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CmpText(text = label, style = CmpTextStyle.LabelMedium, color = content)
        CmpText(text = value, style = CmpTextStyle.TitleMedium, color = content)
    }
}

/** Пояснение на подложке: подсказка по ОС, «версия в канале старше», бета. */
@Composable
internal fun InfoNote(text: String, icon: CmpIcons = CmpIcons.Info) {
    val colors = cmpThemeColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerHigh, PillShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
        CmpText(text = text, style = CmpTextStyle.BodyLarge, color = colors.onSurface)
    }
}

internal enum class ButtonKind { FILLED, OUTLINED, TEXT }

@Composable
internal fun UpdateButton(
    text: String,
    onClick: () -> Unit,
    kind: ButtonKind,
    modifier: Modifier = Modifier,
    icon: CmpIcons? = null,
) {
    val sized = modifier.heightIn(min = ButtonHeight)
    val content: @Composable () -> Unit = {
        icon?.let { CmpIcon(icon = it, contentDescription = null, modifier = Modifier.size(20.dp)) }
        CmpText(text = text, modifier = if (icon != null) Modifier.padding(start = 8.dp) else Modifier)
    }
    when (kind) {
        ButtonKind.FILLED -> CmpButton(onClick = onClick, modifier = sized) { content() }
        ButtonKind.OUTLINED -> CmpOutlinedButton(onClick = onClick, modifier = sized) { content() }
        ButtonKind.TEXT -> CmpTextButton(onClick = onClick, modifier = sized) { content() }
    }
}

internal fun LocalDate.formatted(): String = "${dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
    "${monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}.$year"
