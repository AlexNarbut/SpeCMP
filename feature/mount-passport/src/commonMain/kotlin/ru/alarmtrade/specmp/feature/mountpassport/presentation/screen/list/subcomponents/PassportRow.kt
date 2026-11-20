package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_pdf
import ru.alarmtrade.specmp.core.localization.resources.common_btn_export_zip
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_label_no_car
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_a11y_actions
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_finish
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_hint_connect
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_item_open
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_label_client
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_label_phone
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_label_settings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_label_vin
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_status_without_unit
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenuItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStatus
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportMatchField
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.dateTimeLabel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.extraMatch
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.highlighted
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.label

/**
 * Строка паспорта: автомобиль, госномер · модель · ID, найденное поле (если не в заголовке), статус и метки, дата.
 * Заготовка нажимается, только если её можно дооформить; без блока под ней — подсказка. В режиме выбора ([pick])
 * заготовку отметить нельзя — её не экспортируют.
 */
@Composable
internal fun PassportRow(
    item: MountPassportSummary,
    query: String,
    canFinish: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onFinish: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onExport: () -> Unit = {},
    pick: PassportRowPick? = null,
    onExportPdf: () -> Unit = {},
    wide: Boolean = false,
) {
    val blank = item.status == PassportStatus.BLANK
    val clickable = if (pick != null) item.status == PassportStatus.ISSUED else !blank || canFinish
    val open = when {
        pick != null -> pick.onToggle
        blank -> onFinish
        else -> onOpen
    }
    val picked = pick?.picked == true
    val border = if (selected ||
        picked
    ) {
        BorderStroke(CmpBorderWidth.focus, CmpTheme.colors.brandPrimaryDefault)
    } else {
        null
    }
    val content: @Composable () -> Unit = {
        RowContent(
            item,
            query,
            canFinish && pick == null,
            RowTrailing(clickable, open, onDelete, onExport, onExportPdf, pick, menuOpen = clickable && !wide),
        )
    }
    val tag = if (pick != null) MountPassportTags.pick(item.id) else MountPassportTags.row(item.id)
    if (clickable) {
        CmpClickableCard(onClick = open, modifier = modifier.testTag(tag), border = border) { content() }
    } else {
        CmpCard(modifier = modifier.testTag(tag), border = border) { content() }
    }
}

@Immutable
private class RowTrailing(
    val clickable: Boolean,
    val open: () -> Unit,
    val onDelete: () -> Unit,
    val onExport: () -> Unit,
    val onExportPdf: () -> Unit,
    val pick: PassportRowPick?,
    val menuOpen: Boolean,
)

@Composable
private fun RowContent(item: MountPassportSummary, query: String, canFinish: Boolean, trailing: RowTrailing) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = CmpSpacing.space16, top = CmpSpacing.space12),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(bottom = CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            Title(item, query)
            Subtitle(item, query)
            ExtraMatch(item, query)
            Marks(item)
            if (item.status == PassportStatus.BLANK &&
                trailing.pick == null
            ) {
                FinishLine(item.id, canFinish, trailing.open)
            }
        }
        val pick = trailing.pick
        when {
            pick == null -> RowMenu(item, trailing)

            trailing.clickable -> CmpCheckbox(
                checked = pick.picked,
                onCheckedChange = { pick.onToggle() },
                modifier = Modifier.padding(end = CmpSpacing.space4),
            )
        }
    }
}

@Composable
private fun Title(item: MountPassportSummary, query: String) {
    val car = item.carTitle
    if (car.isNullOrBlank()) {
        CmpText(
            text = stringResource(Strings.string.mountpassport_common_label_no_car),
            style = CmpTextStyle.Headline2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        CmpText(
            text = highlighted(car, query),
            style = CmpTextStyle.Headline2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Subtitle(item: MountPassportSummary, query: String) {
    val colors = CmpTheme.colors
    val plate = item.plate?.takeIf { it.isNotBlank() }?.let { highlighted(it, query) }
    val id = highlighted(item.systemId, query)
    val text = buildAnnotatedString {
        if (plate != null) {
            withStyle(SpanStyle(color = colors.foregroundOnDarkPrimaryDefault, fontWeight = FontWeight.Bold)) {
                append(plate)
            }
            append(SEPARATOR)
        }
        append(item.unitModel)
        append(SEPARATOR)
        append(ID_PREFIX)
        append(id)
    }
    CmpText(text = text, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkSecondaryDefault, maxLines = 2)
}

@Composable
private fun ExtraMatch(item: MountPassportSummary, query: String) {
    val field = extraMatch(item, query) ?: return
    val (label, value) = when (field) {
        PassportMatchField.VIN -> stringResource(Strings.string.mountpassport_list_label_vin) to item.vin.orEmpty()

        PassportMatchField.CLIENT ->
            stringResource(Strings.string.mountpassport_list_label_client) to item.clientName.orEmpty()

        PassportMatchField.PHONE -> stringResource(Strings.string.mountpassport_list_label_phone) to
            item.clientPhone.orEmpty().let { PhoneNumbers.format(PhoneNumbers.fromRaw(it)) }
    }
    val text = buildAnnotatedString {
        append(label)
        append(LABEL_SEPARATOR)
        append(highlighted(value, query))
    }
    CmpText(text = text, style = CmpTextStyle.Paragraph3, color = CmpTheme.colors.foregroundOnDarkSecondaryDefault)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Marks(item: MountPassportSummary) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = CmpSpacing.space4, end = CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        StatusTag(item.status)
        if (item.withoutUnit) {
            CmpTag(
                text = stringResource(Strings.string.mountpassport_list_status_without_unit),
                icon = CmpIcons.Edit,
                uppercase = false,
            )
        }
        if (item.hasSettings) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CmpIcon(
                    icon = CmpIcons.Tune,
                    contentDescription = null,
                    tint = muted,
                    modifier = Modifier.size(CmpSize.iconS),
                )
                CmpText(
                    text = stringResource(Strings.string.mountpassport_list_label_settings),
                    style = CmpTextStyle.Additional0,
                    color = muted,
                    modifier = Modifier.padding(start = CmpSpacing.space4),
                )
            }
        }
        CmpText(text = item.createdAt.dateTimeLabel(), style = CmpTextStyle.Additional0, color = muted)
    }
}

@Composable
internal fun StatusTag(status: PassportStatus) {
    val (tone, icon) = when (status) {
        PassportStatus.BLANK -> CmpTagTone.Warning to CmpIcons.Schedule
        PassportStatus.DRAFT -> CmpTagTone.Brand to CmpIcons.Edit
        PassportStatus.ISSUED -> CmpTagTone.Success to CmpIcons.TaskAlt
    }
    CmpTag(text = status.label(), tone = tone, icon = icon, uppercase = false)
}

@Composable
private fun FinishLine(id: String, canFinish: Boolean, onFinish: () -> Unit) {
    if (canFinish) {
        CmpButton(
            text = stringResource(Strings.string.mountpassport_list_btn_finish),
            onClick = onFinish,
            icon = CmpIcons.Description,
            size = CmpButtonSize.M,
            modifier = Modifier.padding(top = CmpSpacing.space8).testTag(MountPassportTags.finish(id)),
        )
    } else {
        val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
        Row(modifier = Modifier.padding(top = CmpSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(
                icon = CmpIcons.Bluetooth,
                contentDescription = null,
                tint = muted,
                modifier = Modifier.size(CmpSize.iconS),
            )
            CmpText(
                text = stringResource(Strings.string.mountpassport_list_hint_connect),
                style = CmpTextStyle.Paragraph3,
                color = muted,
                modifier = Modifier.padding(start = CmpSpacing.space8),
            )
        }
    }
}

/**
 * «⋯» строки: черновик — «Открыть», «Удалить»; «Оформлен» — ещё «Экспорт PDF» и «Экспорт ZIP». В широком окне
 * «Открыть» нет: выбор — тапом по строке.
 */
@Composable
private fun RowMenu(item: MountPassportSummary, trailing: RowTrailing) {
    var expanded by remember { mutableStateOf(false) }
    val issued = item.status == PassportStatus.ISSUED
    val pick: (() -> Unit) -> () -> Unit = { action ->
        {
            expanded = false
            action()
        }
    }
    Box {
        CmpIconButton(
            onClick = { expanded = true },
            icon = CmpIcons.MoreVert,
            contentDescription = stringResource(Strings.string.mountpassport_list_a11y_actions),
        )
        CmpDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (trailing.menuOpen) {
                CmpDropdownMenuItem(
                    text = stringResource(Strings.string.mountpassport_list_item_open),
                    icon = CmpIcons.OpenInNew,
                    onClick = pick(trailing.open),
                )
            }
            if (issued) {
                CmpDropdownMenuItem(
                    text = stringResource(Strings.string.common_btn_export_pdf),
                    icon = CmpIcons.PictureAsPdf,
                    onClick = pick(trailing.onExportPdf),
                    modifier = Modifier.testTag(MountPassportTags.exportPdf(item.id)),
                )
                CmpDropdownMenuItem(
                    text = stringResource(Strings.string.common_btn_export_zip),
                    icon = CmpIcons.Inventory2,
                    onClick = pick(trailing.onExport),
                    modifier = Modifier.testTag(MountPassportTags.export(item.id)),
                )
            }
            CmpDropdownMenuItem(
                text = stringResource(Strings.string.mountpassport_common_btn_delete),
                icon = CmpIcons.Delete,
                onClick = pick(trailing.onDelete),
            )
        }
    }
}

private const val SEPARATOR = " · "
private const val LABEL_SEPARATOR = ": "
private const val ID_PREFIX = "ID "
