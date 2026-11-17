package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_filter_btn_apply
import ru.alarmtrade.specmp.core.localization.resources.vehicle_filter_btn_reset
import ru.alarmtrade.specmp.core.localization.resources.vehicle_filter_msg_hint
import ru.alarmtrade.specmp.core.localization.resources.vehicle_filter_title_screen
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.input.CmpCheckbox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.groupLabel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions

/** Черновик отдельно от применённого: закрытие без «Применить» фильтр не меняет. */
@Composable
internal fun FilterPanel(draft: Set<CarFeatureGroup>, wide: Boolean, actions: VehicleSelectActions) {
    if (wide) {
        CmpDialog(
            onDismissRequest = actions.onFilterDismiss,
            title = {
                CmpText(
                    text = stringResource(Strings.string.vehicle_filter_title_screen),
                    style = CmpTextStyle.Headline1,
                )
            },
            text = { FilterBody(draft = draft, onToggle = actions.onFilterToggle) },
            dismissButton = { ResetButton(actions) },
            confirmButton = { ApplyButton(actions) },
        )
    } else {
        CmpBottomSheet(onDismissRequest = actions.onFilterDismiss) {
            CmpColumn(
                modifier = Modifier.fillMaxWidth().padding(
                    start = CmpSpacing.screenPaddingH,
                    end = CmpSpacing.screenPaddingH,
                    bottom = CmpSpacing.screenPaddingBottom,
                ),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpText(
                    text = stringResource(Strings.string.vehicle_filter_title_screen),
                    style = CmpTextStyle.Headline1,
                )
                FilterBody(draft = draft, onToggle = actions.onFilterToggle)
                CmpRow(
                    modifier = Modifier.fillMaxWidth().padding(top = CmpSpacing.space8),
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.End),
                ) {
                    ResetButton(actions)
                    ApplyButton(actions)
                }
            }
        }
    }
}

@Composable
private fun FilterBody(draft: Set<CarFeatureGroup>, onToggle: (CarFeatureGroup) -> Unit) {
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(
            text = stringResource(Strings.string.vehicle_filter_msg_hint),
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        CarFeatureGroup.entries.forEach { group ->
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CmpSize.touchMin)
                    .cmpPressable(onClick = { onToggle(group) }, shape = RoundedCornerShape(CmpRadius.controls))
                    .testTag(VehicleTestTags.filterGroup(group.name.lowercase())),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpCheckbox(checked = group in draft, onCheckedChange = { onToggle(group) })
                CmpText(text = groupLabel(group), style = CmpTextStyle.Paragraph2)
            }
        }
    }
}

@Composable
private fun ResetButton(actions: VehicleSelectActions) {
    CmpButton(
        text = stringResource(Strings.string.vehicle_filter_btn_reset),
        onClick = actions.onFilterDraftReset,
        type = CmpButtonType.Tertiary,
        modifier = Modifier.testTag(VehicleTestTags.FILTER_RESET),
    )
}

@Composable
private fun ApplyButton(actions: VehicleSelectActions) {
    CmpButton(
        text = stringResource(Strings.string.vehicle_filter_btn_apply),
        onClick = actions.onFilterApply,
        modifier = Modifier.testTag(VehicleTestTags.FILTER_APPLY),
    )
}
