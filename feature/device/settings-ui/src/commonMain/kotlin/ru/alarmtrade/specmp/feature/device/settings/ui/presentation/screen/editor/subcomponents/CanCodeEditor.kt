package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpRadioButton
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.SettingsTestTags
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.CAN_CODE_LENGTH
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.carLabel
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.CarSearch
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import specmp.feature.device.settings_ui.generated.resources.Res
import specmp.feature.device.settings_ui.generated.resources.settings_can_btn_find
import specmp.feature.device.settings_ui.generated.resources.settings_can_hint_digits
import specmp.feature.device.settings_ui.generated.resources.settings_can_label_code
import specmp.feature.device.settings_ui.generated.resources.settings_can_loading
import specmp.feature.device.settings_ui.generated.resources.settings_can_msg_error
import specmp.feature.device.settings_ui.generated.resources.settings_can_msg_many
import specmp.feature.device.settings_ui.generated.resources.settings_can_msg_none

/**
 * CAN-код: поле на 4 цифры (цифровая клавиатура) и «Найти автомобиль». Один найденный автомобиль привязывается
 * к коду сразу, несколько — выбор, ни одного или нет связи — предупреждение (код всё равно можно применить).
 */
@Suppress("LongMethod")
@Composable
internal fun CanCodeEditor(
    value: CanCode,
    carSearch: CarSearch,
    cars: Map<Int, CarSummary>,
    onCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPick: (carId: Int) -> Unit
) {
    val complete = value.code.length == CAN_CODE_LENGTH
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpTextField(
            value = value.code,
            onValueChange = { text -> onCodeChange(text.filter(Char::isDigit).take(CAN_CODE_LENGTH)) },
            label = stringResource(Res.string.settings_can_label_code),
            singleLine = true,
            isError = value.code.isNotEmpty() && !complete,
            supportingText = if (complete) {
                null
            } else {
                (
                    {
                        CmpText(text = stringResource(Res.string.settings_can_hint_digits))
                    }
                    )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.CAN_CODE)
        )
        CmpOutlinedButton(
            onClick = onSearch,
            enabled = complete && carSearch != CarSearch.Loading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag(SettingsTestTags.CAN_FIND)
        ) {
            CmpIcon(icon = CmpIcons.Search, contentDescription = null)
            CmpText(text = stringResource(Res.string.settings_can_btn_find), modifier = Modifier.padding(start = 8.dp))
        }
        CarSearchResult(value, carSearch, cars, onPick)
    }
}

/** Результат поиска автомобиля по коду: привязанный автомобиль, загрузка, выбор из нескольких или предупреждение. */
@Composable
private fun CarSearchResult(
    value: CanCode,
    carSearch: CarSearch,
    cars: Map<Int, CarSummary>,
    onPick: (carId: Int) -> Unit
) {
    val colors = cmpThemeColors()
    when (carSearch) {
        CarSearch.Idle -> value.carId?.let(cars::get)?.let { car -> CarCard(carLabel(car)) }

        CarSearch.Loading -> CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            CmpText(
                text = stringResource(Res.string.settings_can_loading),
                style = CmpTextStyle.Body,
                color = colors.onSurfaceVariant
            )
        }

        CarSearch.Failed -> Warning(stringResource(Res.string.settings_can_msg_error))

        is CarSearch.Found -> when (carSearch.cars.size) {
            0 -> Warning(stringResource(Res.string.settings_can_msg_none, value.code))

            1 -> CarCard(carLabel(carSearch.cars.single()))

            else -> CmpColumn {
                CmpText(
                    text = stringResource(Res.string.settings_can_msg_many),
                    style = CmpTextStyle.Body,
                    color = colors.onSurfaceVariant
                )
                carSearch.cars.forEach { car ->
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
