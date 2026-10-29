package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
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

/**
 * CAN-код: 4 ячейки (цифровая клавиатура), «Найти автомобиль» по коду и «Выбрать из каталога». Один
 * найденный по коду автомобиль привязывается сразу, несколько — выбор, ни одного или нет связи — предупреждение (код
 * всё равно можно применить). Выбор из каталога подставляет и код, и автомобиль.
 */
@Suppress("LongMethod")
@Composable
internal fun CanCodeEditor(
    value: CanCode,
    carSearch: LCEState<List<CarSummary>, SettingsError>,
    cars: Map<Int, CarSummary>,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPick: (carId: Int) -> Unit,
    onPickFromCatalog: () -> Unit
) {
    val complete = value.code.length == CAN_CODE_LENGTH
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CmpCodeField(
                value = value.code,
                onValueChange = onCodeChange,
                label = stringResource(Strings.string.settings_can_label_code),
                clearContentDescription = stringResource(Strings.string.settings_can_a11y_clear),
                length = CAN_CODE_LENGTH,
                isError = value.code.isNotEmpty() && !complete,
                modifier = Modifier.testTag(SettingsTestTags.CAN_CODE)
            )
            if (!complete) {
                CmpText(
                    text = stringResource(Strings.string.settings_can_hint_digits),
                    style = CmpTextStyle.Body,
                    color = if (value.code.isEmpty()) cmpThemeColors().onSurfaceVariant else cmpThemeColors().error
                )
            }
        }
        CmpOutlinedButton(
            onClick = onSearch,
            enabled = complete && carSearch != LCEState.Loading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(SettingsTestTags.CAN_FIND)
        ) {
            CmpIcon(icon = CmpIcons.Search, contentDescription = null)
            CmpText(
                text = stringResource(Strings.string.settings_can_btn_find),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        CmpOutlinedButton(
            onClick = onPickFromCatalog,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(SettingsTestTags.CAN_PICK_VEHICLE)
        ) {
            CmpIcon(icon = CmpIcons.DirectionsCar, contentDescription = null)
            CmpText(
                text = stringResource(Strings.string.settings_can_btn_catalog),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        CarSearchResult(value, carSearch, cars, onPick)
    }
}

/** Результат поиска автомобиля по коду: привязанный автомобиль, загрузка, выбор из нескольких или предупреждение. */
@Composable
private fun CarSearchResult(
    value: CanCode,
    carSearch: LCEState<List<CarSummary>, SettingsError>,
    cars: Map<Int, CarSummary>,
    onPick: (carId: Int) -> Unit
) {
    val colors = cmpThemeColors()
    when (carSearch) {
        LCEState.None -> value.carId?.let(cars::get)?.let { car -> CarCard(carLabel(car)) }

        LCEState.Loading -> CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            CmpText(
                text = stringResource(Strings.string.settings_can_loading),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant
            )
        }

        is LCEState.Error -> Warning(
            when (val error = carSearch.error) {
                SettingsError.LoadFailed -> stringResource(Strings.string.settings_can_msg_error)

                is SettingsError.Common ->
                    "${error.error.message()}. ${stringResource(Strings.string.settings_can_msg_apply_without_car)}"
            }
        )

        is LCEState.Content -> when (carSearch.content.size) {
            0 -> Warning(stringResource(Strings.string.settings_can_msg_none, value.code))

            1 -> CarCard(carLabel(carSearch.content.single()))

            else -> CmpColumn {
                CmpText(
                    text = stringResource(Strings.string.settings_can_msg_many),
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant
                )
                carSearch.content.forEach { car ->
                    val selected = car.id == value.carId
                    CmpRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .background(
                                if (selected) colors.secondaryContainer else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(role = Role.RadioButton) { onPick(car.id) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CmpRadioButton(selected = selected, onClick = null)
                        CmpText(
                            text = carLabel(car),
                            style = CmpTextStyle.BodyLarge,
                            color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarCard(text: String) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpIcon(icon = CmpIcons.Check, contentDescription = null, tint = colors.onPrimaryContainer)
        CmpText(
            text = text,
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onPrimaryContainer
        )
    }
}

@Composable
private fun Warning(text: String) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.tertiaryContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.onTertiaryContainer)
        CmpText(text = text, style = CmpTextStyle.Body, color = colors.onTertiaryContainer)
    }
}
