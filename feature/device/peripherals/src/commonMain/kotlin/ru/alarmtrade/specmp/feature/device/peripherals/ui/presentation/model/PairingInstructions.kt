package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_band_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_band_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_bt01_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_bt01_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_bt01_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_bt760_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_btr_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_btr_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_btr_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_d030_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_d030_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_di04_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_di04_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_dms_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_fuel_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_fuel_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_fuel_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_hood_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_hood_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_hood_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_hood_4
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav035_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav09_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav09_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav09_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav10_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav10_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nav10_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_nfc_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_ps331_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_ps331_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_ps332_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_rfm_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_rfm_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_rhm_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_rhm_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_vs_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_vs_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_vs_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_watch2_1
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_watch2_2
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_watch2_3
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_watch2_4
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_item_watch2_5
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_band
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_bt01
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_d030
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_di04
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_nav035
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_nav09
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_nav10
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_ps331
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_ps332
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_rhm
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_title_watch2
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import specmp.feature.device.peripherals.generated.resources.Res
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_bt_01_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_bt_01_1_color
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_bt_760_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_btr_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_d_030_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_di_04_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_door_module_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_hood_lock_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_hood_lock_2
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_hood_lock_3
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_nav_035_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_nav_09_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_nav_10_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_ps_331_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_ps_331_1_color
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_ps_332_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_rfm_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_rhm_03_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_rhm_03_1_color
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_vs_1
import specmp.feature.device.peripherals.generated.resources.peripherals_scheme_watch_2_1

/** Схема из макета: [ink] тонируется цветом текста темы, [color] (цветные провода) — без тонировки. */
internal class Scheme(val ink: DrawableResource, val color: DrawableResource? = null)

/** Раздел инструкции приживления; [title] — модель, если у типа их несколько. Тексты — из старого приложения. */
internal class InstructionSection(
    val title: StringResource?,
    val schemes: List<Scheme>,
    val steps: List<StringResource>,
)

private val BT760 = InstructionSection(
    title = null,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_bt_760_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_bt760_1,
    ),
)

private val BTR = InstructionSection(
    title = null,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_btr_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_btr_1,
        Strings.string.peripherals_instruction_item_btr_2,
        Strings.string.peripherals_instruction_item_btr_3,
    ),
)

private val DI04 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_di04,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_di_04_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_di04_1,
        Strings.string.peripherals_instruction_item_di04_2,
    ),
)

private val BT01 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_bt01,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_bt_01_1, Res.drawable.peripherals_scheme_bt_01_1_color)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_bt01_1,
        Strings.string.peripherals_instruction_item_bt01_2,
        Strings.string.peripherals_instruction_item_bt01_3,
    ),
)

private val D030 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_d030,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_d_030_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_d030_1,
        Strings.string.peripherals_instruction_item_d030_2,
    ),
)

private val WATCH2 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_watch2,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_watch_2_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_watch2_1,
        Strings.string.peripherals_instruction_item_watch2_2,
        Strings.string.peripherals_instruction_item_watch2_3,
        Strings.string.peripherals_instruction_item_watch2_4,
        Strings.string.peripherals_instruction_item_watch2_5,
    ),
)

private val BAND = InstructionSection(
    title = Strings.string.peripherals_instruction_title_band,
    schemes = listOf(),
    steps = listOf(
        Strings.string.peripherals_instruction_item_band_1,
        Strings.string.peripherals_instruction_item_band_2,
    ),
)

private val NAV035 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_nav035,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_nav_035_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_nav035_1,
    ),
)

private val NAV09 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_nav09,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_nav_09_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_nav09_1,
        Strings.string.peripherals_instruction_item_nav09_2,
        Strings.string.peripherals_instruction_item_nav09_3,
    ),
)

private val NAV10 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_nav10,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_nav_10_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_nav10_1,
        Strings.string.peripherals_instruction_item_nav10_2,
        Strings.string.peripherals_instruction_item_nav10_3,
    ),
)

private val RFM = InstructionSection(
    title = null,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_rfm_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_rfm_1,
        Strings.string.peripherals_instruction_item_rfm_2,
    ),
)

private val RHM = InstructionSection(
    title = Strings.string.peripherals_instruction_title_rhm,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_rhm_03_1, Res.drawable.peripherals_scheme_rhm_03_1_color)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_rhm_1,
        Strings.string.peripherals_instruction_item_rhm_2,
    ),
)

private val PS331 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_ps331,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_ps_331_1, Res.drawable.peripherals_scheme_ps_331_1_color)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_ps331_1,
        Strings.string.peripherals_instruction_item_ps331_2,
    ),
)

private val PS332 = InstructionSection(
    title = Strings.string.peripherals_instruction_title_ps332,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_ps_332_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_ps332_1,
    ),
)

private val DMS = InstructionSection(
    title = null,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_door_module_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_dms_1,
    ),
)

private val HOOD = InstructionSection(
    title = null,
    schemes = listOf(
        Scheme(Res.drawable.peripherals_scheme_hood_lock_1),
        Scheme(Res.drawable.peripherals_scheme_hood_lock_2),
        Scheme(Res.drawable.peripherals_scheme_hood_lock_3),
    ),
    steps = listOf(
        Strings.string.peripherals_instruction_item_hood_1,
        Strings.string.peripherals_instruction_item_hood_2,
        Strings.string.peripherals_instruction_item_hood_3,
        Strings.string.peripherals_instruction_item_hood_4,
    ),
)

private val VS = InstructionSection(
    title = null,
    schemes = listOf(Scheme(Res.drawable.peripherals_scheme_vs_1)),
    steps = listOf(
        Strings.string.peripherals_instruction_item_vs_1,
        Strings.string.peripherals_instruction_item_vs_2,
        Strings.string.peripherals_instruction_item_vs_3,
    ),
)

private val NFC = InstructionSection(
    title = null,
    schemes = listOf(),
    steps = listOf(
        Strings.string.peripherals_instruction_item_nfc_1,
    ),
)

private val FUEL = InstructionSection(
    title = null,
    schemes = listOf(),
    steps = listOf(
        Strings.string.peripherals_instruction_item_fuel_1,
        Strings.string.peripherals_instruction_item_fuel_2,
        Strings.string.peripherals_instruction_item_fuel_3,
    ),
)

internal val PeripheralType.instruction: List<InstructionSection>
    get() = when (this) {
        PeripheralType.TAG -> listOf(BT760)
        PeripheralType.KEYFOB -> listOf(D030, WATCH2, BAND)
        PeripheralType.WATCH -> listOf(WATCH2)
        PeripheralType.BRACELET -> listOf(BAND)
        PeripheralType.RELAY -> listOf(BTR)
        PeripheralType.HOOD_LOCK -> listOf(HOOD)
        PeripheralType.RHM -> listOf(RHM, PS331, PS332)
        PeripheralType.DOOR_SENSOR -> listOf(DMS)
        PeripheralType.NAV -> listOf(NAV035, NAV09, NAV10)
        PeripheralType.DI -> listOf(DI04, BT01)
        PeripheralType.RF_MODULE -> listOf(RFM)
        PeripheralType.VS -> listOf(VS)
        PeripheralType.NFC_READER -> listOf(NFC)
        PeripheralType.FUEL_SENSOR -> listOf(FUEL)
        else -> emptyList()
    }
