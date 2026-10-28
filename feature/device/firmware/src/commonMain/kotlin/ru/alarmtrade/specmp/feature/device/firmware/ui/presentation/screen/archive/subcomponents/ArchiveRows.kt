package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_less
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_more
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_btn_select
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_installed
import ru.alarmtrade.specmp.core.localization.resources.firmware_archive_status_older
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowKind
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.ArchiveRowUi

private val ButtonHeight = 48.dp
private val ExpandedDescriptionMaxHeight = 200.dp
private const val COLLAPSED_LINES = 2
private const val DAY_MONTH_DIGITS = 2
private const val YEAR_DIGITS = 4

/** testTag строки версии в архиве. */
internal fun archiveItemTag(version: String): String = "firmware_archive_item_$version"

/** testTag кнопки «Выбрать» в строке. */
internal fun archiveSelectTag(version: String): String = "firmware_archive_select_$version"

/** Дата как её отдаёт сервер, в виде `дд.мм.гггг` (не зависит от языка приложения). */
internal fun formatArchiveDate(date: LocalDate): String =
    "${date.dayOfMonth.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
        "${date.monthNumber.toString().padStart(DAY_MONTH_DIGITS, '0')}." +
        date.year.toString().padStart(YEAR_DIGITS, '0')

/** Метка у версии: «Установлена» или «Старее установленной» (у новых — нет). */
@Composable
internal fun VersionBadge(kind: ArchiveRowKind, modifier: Modifier = Modifier) {
    if (kind == ArchiveRowKind.New) return
    val colors = cmpThemeColors()
    val installed = kind == ArchiveRowKind.Installed
    CmpSurface(
        modifier = modifier.height(26.dp),
        shape = CircleShape,
        color = if (installed) colors.primary else colors.surfaceVariant,
        contentColor = if (installed) colors.onPrimary else colors.onSurfaceVariant
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (installed) CmpIcon(icon = CmpIcons.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            CmpText(
                text = stringResource(
                    if (installed) {
                        Strings.string.firmware_archive_status_installed
                    } else {
                        Strings.string.firmware_archive_status_older
                    }
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Строка версии: номер, метка, дата, CAN и описание (раскрывается отдельной кнопкой — нажатие по ней версию не
 * выбирает). На широком окне ([master]) вся строка выбирает версию для панели справа; на узком — кнопка «Выбрать»
 * ([onPick] = `null` — просмотр без выбора, кнопки нет).
 */
@Composable
internal fun ArchiveRow(
    row: ArchiveRowUi,
    master: Boolean,
    selected: Boolean,
    onSelect: () -> Unit,
    onPick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = cmpThemeColors()
    var expanded by remember(row.version) { mutableStateOf(false) }
    val highlighted = selected || row.kind == ArchiveRowKind.Installed
    CmpClickableSurface(
        onClick = onSelect,
        enabled = master,
        modifier = modifier.fillMaxWidth().testTag(archiveItemTag(row.version)),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = if (highlighted) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CmpRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CmpRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VersionText(
                        row.version,
                        fontSize = 24.sp,
                        color = if (selected) colors.onPrimaryContainer else colors.primary
                    )
                    VersionBadge(row.kind)
                }
                CmpText(
                    text = formatArchiveDate(row.createdOn),
                    fontSize = 14.sp,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
            }
            ArchiveRowCan(row)
            if (row.description.isNotBlank()) {
                CmpText(
                    text = row.description,
                    style = CmpTextStyle.Body,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (expanded) {
                        Modifier.heightIn(max = ExpandedDescriptionMaxHeight).verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    }
                )
            }
            RowActions(row, master, expanded, onToggle = { expanded = !expanded }, onPick = onPick)
        }
    }
}

/** Нижняя строка карточки: «Показать больше / Свернуть» (если есть описание) и «Выбрать» на узком окне. */
@Composable
private fun RowActions(
    row: ArchiveRowUi,
    master: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPick: (() -> Unit)?
) {
    val hasDescription = row.description.isNotBlank()
    val showPick = !master && onPick != null
    if (!hasDescription && !showPick) return
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasDescription) {
            CmpTextButton(
                text = stringResource(
                    if (expanded) Strings.string.firmware_archive_btn_less else Strings.string.firmware_archive_btn_more
                ),
                onClick = onToggle,
                modifier = Modifier.height(ButtonHeight)
            )
        } else {
            CmpColumn {}
        }
        if (showPick && onPick != null) {
            CmpOutlinedButton(
                text = stringResource(Strings.string.firmware_archive_btn_select),
                onClick = onPick,
                modifier = Modifier.height(ButtonHeight).testTag(archiveSelectTag(row.version))
            )
        }
    }
}
