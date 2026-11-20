package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_new
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_new_without_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_btn_reset_search
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_error_load
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_hint_new
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_label_found
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_label_storage
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_empty_archive
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_empty_archive_title
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_empty_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_empty_unit_title
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_not_found
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_not_found_title
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_placeholder_search
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.PassportStorageUsage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.MountPassportTags
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.megabytesLabel
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.text

private val SkeletonHeight = 132.dp
private const val SKELETON_ROWS = 4

/** Шапка архива: поиск и (без запроса) подсказка с «Новый паспорт без блока». */
@Composable
internal fun ArchiveHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onNewWithoutUnit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(Strings.string.mountpassport_list_placeholder_search),
            clearContentDescription = stringResource(Strings.string.mountpassport_list_a11y_clear),
            modifier = Modifier.fillMaxWidth().testTag(MountPassportTags.SEARCH),
        )
        if (query.isEmpty()) {
            val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
            Row(verticalAlignment = Alignment.CenterVertically) {
                CmpIcon(
                    icon = CmpIcons.Info,
                    contentDescription = null,
                    tint = muted,
                    modifier = Modifier.size(CmpSize.iconS),
                )
                CmpText(
                    text = stringResource(Strings.string.mountpassport_list_hint_new),
                    style = CmpTextStyle.Paragraph3,
                    color = muted,
                    modifier = Modifier.weight(1f).padding(horizontal = CmpSpacing.space8),
                )
            }
            CmpButton(
                text = stringResource(Strings.string.mountpassport_list_btn_new_without_unit),
                onClick = onNewWithoutUnit,
                type = CmpButtonType.Secondary,
                size = CmpButtonSize.M,
                icon = CmpIcons.Add,
                uppercase = false,
                modifier = Modifier.testTag(MountPassportTags.NEW_WITHOUT_UNIT),
            )
        }
    }
}

@Composable
internal fun NewPassportButton(onNew: () -> Unit, modifier: Modifier = Modifier, fullWidth: Boolean = true) {
    CmpButton(
        text = stringResource(Strings.string.mountpassport_list_btn_new),
        onClick = onNew,
        icon = CmpIcons.Add,
        fullWidth = fullWidth,
        modifier = modifier.testTag(MountPassportTags.NEW),
    )
}

@Composable
internal fun FoundLine(count: Int, modifier: Modifier = Modifier) {
    CmpText(
        text = stringResource(Strings.string.mountpassport_list_label_found, count),
        style = CmpTextStyle.Additional0,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        modifier = modifier,
    )
}

@Composable
internal fun StorageLine(usage: PassportStorageUsage, modifier: Modifier = Modifier) {
    val muted = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        CmpIcon(
            icon = CmpIcons.Inventory2,
            contentDescription = null,
            tint = muted,
            modifier = Modifier.size(CmpSize.iconS),
        )
        CmpText(
            text = pluralStringResource(
                Strings.plurals.mountpassport_list_label_storage,
                usage.passports,
                usage.passports,
                megabytesLabel(usage.bytes),
            ),
            style = CmpTextStyle.Additional0,
            color = muted,
            modifier = Modifier.padding(start = CmpSpacing.space8),
        )
    }
}

@Composable
internal fun ListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        repeat(SKELETON_ROWS) {
            CmpSkeleton(
                modifier = Modifier.fillMaxWidth().height(SkeletonHeight),
                shape = RoundedCornerShape(CmpRadius.cardMedium),
            )
        }
    }
}

@Composable
internal fun EmptyArchive(modifier: Modifier = Modifier) {
    CmpStateBox(
        icon = CmpIcons.Description,
        title = stringResource(Strings.string.mountpassport_list_msg_empty_archive_title),
        text = stringResource(Strings.string.mountpassport_list_msg_empty_archive),
        modifier = modifier,
    )
}

@Composable
internal fun EmptyUnit(onNew: () -> Unit, modifier: Modifier = Modifier) {
    CmpStateBox(
        icon = CmpIcons.Description,
        title = stringResource(Strings.string.mountpassport_list_msg_empty_unit_title),
        text = stringResource(Strings.string.mountpassport_list_msg_empty_unit),
        modifier = modifier,
    ) {
        NewPassportButton(onNew = onNew, fullWidth = false)
    }
}

@Composable
internal fun NothingFound(query: String, onReset: () -> Unit, modifier: Modifier = Modifier) {
    CmpStateBox(
        icon = CmpIcons.SearchOff,
        title = stringResource(Strings.string.mountpassport_list_msg_not_found_title),
        text = stringResource(Strings.string.mountpassport_list_msg_not_found, query),
        modifier = modifier,
    ) {
        CmpButton(
            text = stringResource(Strings.string.mountpassport_list_btn_reset_search),
            onClick = onReset,
            type = CmpButtonType.Secondary,
            icon = CmpIcons.Close,
            size = CmpButtonSize.M,
        )
    }
}

@Composable
internal fun ListError(error: PassportError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    CmpStateBox(
        icon = CmpIcons.ErrorOutline,
        title = stringResource(Strings.string.mountpassport_list_error_load),
        text = error.text(),
        tone = CmpStateTone.Error,
        modifier = modifier,
    ) {
        CmpButton(
            text = stringResource(Strings.string.mountpassport_common_btn_retry),
            onClick = onRetry,
            icon = CmpIcons.Refresh,
            size = CmpButtonSize.M,
            modifier = Modifier.testTag(MountPassportTags.RETRY),
        )
    }
}
