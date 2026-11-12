package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_bracelet
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_bt02
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_di
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_door_sensor
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_fuel_sensor
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_hood_lock
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_keyfob
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_nav
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_other
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_pir_sensor
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_relay
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_rf_module
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_rhm
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_tag
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_tpms
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_vs
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_title_watch
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_alt420
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_base
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_bracelet
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_bt02
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_di
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_door_sensor
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_fuel_sensor
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_function_button
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_hood_lock
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_immobilizer
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_keyfob
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_nav
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_nfc_reader
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_phone
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_pir_sensor
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_relay
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_rf_module
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_rhm
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_tag
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_tpms
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_unknown
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_vs
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_value_watch
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import specmp.feature.device.peripherals.generated.resources.Res
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_bt_02
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_di
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_dms
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_fb01
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_fuel_sensor
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_hood_lock
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_keychain_watch
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_nav_09
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_pir_sensor
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_relay
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_rf_module
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_rhm_siren
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_token
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_tpms
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_vs

/** Иконка типа: легаси-рисунок из макета или, где его нет, значок каталога. */
internal sealed interface TypeIcon {
    data class Drawable(val resource: DrawableResource) : TypeIcon

    data class Vector(val icon: CmpIcons) : TypeIcon
}

internal class TypeUi(val name: StringResource, val group: StringResource, val icon: TypeIcon)

private fun drawable(resource: DrawableResource) = TypeIcon.Drawable(resource)

private val Other = Strings.string.peripherals_type_title_other

internal val PeripheralType.ui: TypeUi
    get() = when (this) {
        PeripheralType.TAG -> TypeUi(
            Strings.string.peripherals_type_value_tag,
            Strings.string.peripherals_type_title_tag,
            drawable(Res.drawable.peripherals_ic_token),
        )

        PeripheralType.KEYFOB -> TypeUi(
            Strings.string.peripherals_type_value_keyfob,
            Strings.string.peripherals_type_title_keyfob,
            drawable(Res.drawable.peripherals_ic_keychain_watch),
        )

        PeripheralType.WATCH -> TypeUi(
            Strings.string.peripherals_type_value_watch,
            Strings.string.peripherals_type_title_watch,
            TypeIcon.Vector(CmpIcons.Watch),
        )

        PeripheralType.BRACELET -> TypeUi(
            Strings.string.peripherals_type_value_bracelet,
            Strings.string.peripherals_type_title_bracelet,
            TypeIcon.Vector(CmpIcons.Watch),
        )

        PeripheralType.RELAY -> TypeUi(
            Strings.string.peripherals_type_value_relay,
            Strings.string.peripherals_type_title_relay,
            drawable(Res.drawable.peripherals_ic_relay),
        )

        PeripheralType.HOOD_LOCK -> TypeUi(
            Strings.string.peripherals_type_value_hood_lock,
            Strings.string.peripherals_type_title_hood_lock,
            drawable(Res.drawable.peripherals_ic_hood_lock),
        )

        PeripheralType.RHM -> TypeUi(
            Strings.string.peripherals_type_value_rhm,
            Strings.string.peripherals_type_title_rhm,
            drawable(Res.drawable.peripherals_ic_rhm_siren),
        )

        PeripheralType.DOOR_SENSOR -> TypeUi(
            Strings.string.peripherals_type_value_door_sensor,
            Strings.string.peripherals_type_title_door_sensor,
            drawable(Res.drawable.peripherals_ic_dms),
        )

        PeripheralType.TPMS -> TypeUi(
            Strings.string.peripherals_type_value_tpms,
            Strings.string.peripherals_type_title_tpms,
            drawable(Res.drawable.peripherals_ic_tpms),
        )

        PeripheralType.FUEL_SENSOR -> TypeUi(
            Strings.string.peripherals_type_value_fuel_sensor,
            Strings.string.peripherals_type_title_fuel_sensor,
            drawable(Res.drawable.peripherals_ic_fuel_sensor),
        )

        PeripheralType.PIR_SENSOR -> TypeUi(
            Strings.string.peripherals_type_value_pir_sensor,
            Strings.string.peripherals_type_title_pir_sensor,
            drawable(Res.drawable.peripherals_ic_pir_sensor),
        )

        PeripheralType.VS -> TypeUi(
            Strings.string.peripherals_type_value_vs,
            Strings.string.peripherals_type_title_vs,
            drawable(Res.drawable.peripherals_ic_vs),
        )

        PeripheralType.NAV -> TypeUi(
            Strings.string.peripherals_type_value_nav,
            Strings.string.peripherals_type_title_nav,
            drawable(Res.drawable.peripherals_ic_nav_09),
        )

        PeripheralType.DI -> TypeUi(
            Strings.string.peripherals_type_value_di,
            Strings.string.peripherals_type_title_di,
            drawable(Res.drawable.peripherals_ic_di),
        )

        PeripheralType.BT02 -> TypeUi(
            Strings.string.peripherals_type_value_bt02,
            Strings.string.peripherals_type_title_bt02,
            drawable(Res.drawable.peripherals_ic_bt_02),
        )

        PeripheralType.RF_MODULE -> TypeUi(
            Strings.string.peripherals_type_value_rf_module,
            Strings.string.peripherals_type_title_rf_module,
            drawable(Res.drawable.peripherals_ic_rf_module),
        )

        PeripheralType.IMMOBILIZER ->
            TypeUi(Strings.string.peripherals_type_value_immobilizer, Other, TypeIcon.Vector(CmpIcons.Lock))

        PeripheralType.NFC_READER ->
            TypeUi(Strings.string.peripherals_type_value_nfc_reader, Other, TypeIcon.Vector(CmpIcons.Sensors))

        PeripheralType.FUNCTION_BUTTON ->
            TypeUi(
                Strings.string.peripherals_type_value_function_button,
                Other,
                drawable(Res.drawable.peripherals_ic_fb01),
            )

        PeripheralType.ALT_420 ->
            TypeUi(Strings.string.peripherals_type_value_alt420, Other, TypeIcon.Vector(CmpIcons.Sensors))

        PeripheralType.PHONE ->
            TypeUi(Strings.string.peripherals_type_value_phone, Other, TypeIcon.Vector(CmpIcons.Phone))

        PeripheralType.BASE ->
            TypeUi(Strings.string.peripherals_type_value_base, Other, TypeIcon.Vector(CmpIcons.DeveloperBoard))

        PeripheralType.UNKNOWN ->
            TypeUi(Strings.string.peripherals_type_value_unknown, Other, TypeIcon.Vector(CmpIcons.HelpOutline))
    }
