package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_can_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.settings_can_btn_catalog
import ru.alarmtrade.specmp.core.localization.resources.settings_can_btn_find
import ru.alarmtrade.specmp.core.localization.resources.settings_can_hint_digits
import ru.alarmtrade.specmp.core.localization.resources.settings_can_label_code
import ru.alarmtrade.specmp.core.localization.resources.settings_can_loading
import ru.alarmtrade.specmp.core.localization.resources.settings_can_msg_apply_without_car
import ru.alarmtrade.specmp.core.localization.resources.settings_can_msg_error
import ru.alarmtrade.specmp.core.localization.resources.settings_can_msg_many
import ru.alarmtrade.specmp.core.localization.resources.settings_can_msg_none
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCodeField
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CAN_CODE_LENGTH
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.carLabel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

private val CardShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardSmall)

@Suppress("LongMethod")
@Composable
internal fun CanCodeEditor(
    value: CanCode,
    carSearch: LCEState<List<CarSummary>, SettingsError>,
    cars: Map<Int, CarSummary>,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPick: (carId: Int) -> Unit,
    onPickFromCatalog: () -> Unit,
) {
    val complete = value.code.length == CAN_CODE_LENGTH
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpCodeField(
                value = value.code,
                onValueChange = onCodeChange,
                label = stringResource(Strings.string.settings_can_label_code),
                clearContentDescription = stringResource(Strings.string.settings_can_a11y_clear),
                length = CAN_CODE_LENGTH,
                isError = value.code.isNotEmpty() && !complete,
                modifier = Modifier.testTag(SettingsTestTags.CAN_CODE),
            )
            if (!complete) {
                CmpText(
                    text = stringResource(Strings.string.settings_can_hint_digits),
                    style = CmpTextStyle.Paragraph3,
                    color = if (value.code.isEmpty()) {
                        CmpTheme.colors.foregroundOnDarkSecondaryDefault
                    } else {
                        CmpTheme.colors.errorDefault
                    },
                )
            }
        }
        CmpButton(
            text = stringResource(Strings.string.settings_can_btn_find),
            onClick = onSearch,
            enabled = complete && carSearch != LCEState.Loading,
            type = CmpButtonType.Secondary,
            icon = CmpIcons.Search,
            fullWidth = true,
            modifier = Modifier.testTag(SettingsTestTags.CAN_FIND),
        )
        CmpButton(
            text = stringResource(Strings.string.settings_can_btn_catalog),
            onClick = onPickFromCatalog,
            type = CmpButtonType.Secondary,
            icon = CmpIcons.DirectionsCar,
            fullWidth = true,
            modifier = Modifier.testTag(SettingsTestTags.CAN_PICK_VEHICLE),
        )
        CarSearchResult(value, carSearch, cars, onPick)
    }
}

@Composable
private fun CarSearchResult(
    value: CanCode,
    carSearch: LCEState<List<CarSummary>, SettingsError>,
    cars: Map<Int, CarSummary>,
    onPick: (carId: Int) -> Unit,
) {
    val colors = CmpTheme.colors
    when (carSearch) {
        LCEState.None -> value.carId?.let(cars::get)?.let { car -> CarCard(carLabel(car)) }

        LCEState.Loading -> CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            CmpText(
                text = stringResource(Strings.string.settings_can_loading),
                style = CmpTextStyle.Paragraph2,
                color = colors.foregroundOnDarkSecondaryDefault,
            )
        }

        is LCEState.Error -> Warning(
            when (val error = carSearch.error) {
                SettingsError.LoadFailed -> stringResource(Strings.string.settings_can_msg_error)

                is SettingsError.Common ->
                    "${error.error.message()}. ${stringResource(Strings.string.settings_can_msg_apply_without_car)}"
            },
        )

        is LCEState.Content -> when (carSearch.content.size) {
            0 -> Warning(stringResource(Strings.string.settings_can_msg_none, value.code))

            1 -> CarCard(carLabel(carSearch.content.single()))

            else -> CmpColumn {
                CmpText(
                    text = stringResource(Strings.string.settings_can_msg_many),
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                )
                carSearch.content.forEach { car ->
                    val selected = car.id == value.carId
                    CmpRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = CmpSize.input)
                            .background(
                                if (selected) colors.backgroundSurfaceHighlight else Color.Transparent,
                                RoundedCornerShape(CmpRadius.cardSmall),
                            )
                            .clickable(role = Role.RadioButton) { onPick(car.id) }
                            .padding(horizontal = CmpSpacing.space8),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    ) {
                        CmpRadioButton(selected = selected, onClick = null)
                        CmpText(
                            text = carLabel(car),
                            style = CmpTextStyle.Paragraph1,
                            color = colors.foregroundOnDarkPrimaryDefault,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarCard(text: String) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.brandPrimaryHighlight, CardShape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, CardShape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.Check, contentDescription = null, tint = colors.brandPrimaryDefault)
        CmpText(
            text = text,
            style = CmpTextStyle.Headline2,
            color = colors.foregroundOnDarkPrimaryDefault,
        )
    }
}

@Composable
private fun Warning(text: String) {
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.warningHighlight, CardShape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, CardShape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.warningDefault)
        CmpText(text = text, style = CmpTextStyle.Paragraph2, color = colors.foregroundOnDarkPrimaryDefault)
    }
}
