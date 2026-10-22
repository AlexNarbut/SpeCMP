package ru.alarmtrade.specmp.feature.device.settings.ui.gallery

import androidx.compose.runtime.remember
import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartSchedule
import ru.alarmtrade.specmp.feature.device.settings.domain.model.AutoStartScheduleSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Car
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.ChannelLogic
import ru.alarmtrade.specmp.feature.device.settings.domain.model.DecimalSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OptionSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.OutputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenEvent
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenTone
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SirenToneType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextInputType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TextSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeOfDay
import ru.alarmtrade.specmp.feature.device.settings.domain.model.TimeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Weekday
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Wire
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.search
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.SectionEntry
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.CarSearch
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsContent
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.DeviceSettingsState
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.EditingSetting
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.ExitTarget
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsDialog
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsHeader
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsLocation
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.SettingsMenu
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.EditorActions
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SearchPane
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor.subcomponents.SettingRowActions

/** Состояния редактора настроек; PNG — в `build/state-gallery/device-settings`. */
class DeviceSettingsGallery {

    private val outDir = File("build/state-gallery/device-settings")

    private fun sw(id: Int, number: String, name: String, on: Boolean, desc: String? = null) =
        SwitchSetting(id, number, name, desc, SettingValue.Switch(on), onTitle = null, offTitle = null)

    private fun int(id: Int, number: String, name: String, value: Int, range: IntRange, units: String?, min: String?) =
        IntegerSetting(
            id, number, name, "Описание настройки $number",
            SettingValue.Integer(
                value
            ),
            range, units, min, null
        )

    private fun opt(id: Int, number: String, name: String, value: Int, options: List<String>) =
        OptionSetting(id, number, name, "Описание", SettingValue.Option(value), options)

    private fun section(id: Int, number: String, name: String, vararg children: SettingNode) =
        SettingsSection(id, number, name, null, children.toList())

    private val sensors = section(
        320, "3.2", "Датчики",
        sw(321, "3.2.1", "Датчик удара", on = true, desc = "Реагирует на удар по кузову."),
        int(322, "3.2.2", "Чувствительность", 7, 0..10, null, "Выкл"),
        opt(323, "3.2.3", "Режим", 0, listOf("Имп", "Пост", "Выкл")),
        opt(
            326,
            "3.2.6",
            "Реакция на срабатывание датчика при включённой охране",
            3,
            listOf(
                "Ничего не делать",
                "Только световая индикация",
                "Короткий сигнал сирены",
                "Сирена и световая индикация в течение 30 секунд",
                "Уведомление владельцу без сирены"
            )
        ),
        int(324, "3.2.4", "Задержка постановки", 30, 0..120, "с", "Выкл"),
        section(325, "3.2.5", "Доп. датчики", sw(3251, "3.2.5.1", "Датчик наклона", on = true))
    )
    private val general = section(
        100,
        "1",
        "Общие",
        TextSetting(
            101, "1.1", "Имя устройства", "Название в приложении", SettingValue.Text("Volvo XC60"), 16,
            TextInputType.TEXT, null, null
        ),
        opt(102, "1.2", "Язык голосовых сообщений", 0, listOf("Русский", "English", "Українська", "Polski")),
        sw(104, "1.4", "Звук подтверждения", on = true),
        DecimalSetting(
            105, "1.5", "Порог температуры", null, SettingValue.Decimal(-5f), -40f, 20f, 0.5f, "°C", null, null
        ),
        TimeSetting(106, "1.6", "Ежедневный запуск", null, SettingValue.Time(TimeOfDay(7, 30)))
    )
    private val inputs = section(
        400,
        "4",
        "Входы",
        InputSetting(
            401, "4.1", "Концевик дверей", null,
            SettingValue.Input(InputValue(listOf(true, true, false), InputContactType.NORMALLY_CLOSED)),
            Channel(1, "IN1", listOf(Wire("Жёлто-чёрный", listOf(0xE0B400, 0x1B1B1B), 5))), Polarity.MINUS,
            listOf(ChannelLogic(0, "Двери", null), ChannelLogic(1, "Багажник", null), ChannelLogic(2, "Капот", null)),
            listOf(ChannelLogic(0, "Двери", null), ChannelLogic(1, "Багажник", null))
        )
    )
    private fun logic(id: Int, name: String) = ChannelLogic(id, name, null)

    private fun osw(id: Int, number: String, ch: String, wire: Wire, logics: List<ChannelLogic>, on: Boolean = false) =
        SwitchOutputSetting(
            id,
            number,
            ch,
            null,
            SettingValue.SwitchOutput(on),
            Channel(id, ch, listOf(wire)),
            Polarity.MINUS,
            logics
        )

    private val channelChoice = section(
        411,
        "4.1.1",
        "Выбор канала",
        osw(4111, "4.1.1.1", "CH1", Wire("Серый", listOf(0x777777), 5), emptyList()),
        osw(4112, "4.1.1.2", "CH2", Wire("Синий", listOf(0x0033AA), 9), emptyList(), on = true),
        osw(
            4113,
            "4.1.1.3",
            "CH3",
            Wire("Сине/Черный", listOf(0x0033AA, 0x000000), 13),
            listOf(logic(1, "Блокировка 1 NC"))
        ),
        osw(4114, "4.1.1.4", "CH4", Wire("Зеленый", listOf(0x1E8F5A), 6), listOf(logic(2, "Указатели поворотов"))),
        osw(
            4115,
            "4.1.1.5",
            "CH5",
            Wire("Красно/черный", listOf(0xEE2222, 0x000000), 2),
            listOf(logic(3, "Выход Бипер"))
        )
    )
    private val outputs = section(
        500,
        "5",
        "Выходы",
        OutputSetting(
            501, "5.1", "Центральный замок", null,
            SettingValue.Output(OutputValue(listOf(true, true, false), Polarity.PLUS)),
            Channel(
                1,
                "CH1",
                listOf(Wire("Синий", listOf(0x1565C0), 1), Wire("Сине-белый", listOf(0x1565C0, 0xF2F2F2), 2))
            ),
            canEditPolarity = true, defaultPolarity = Polarity.MINUS,
            availableLogics = listOf(logic(0, "Открытие ЦЗ"), logic(1, "Закрытие ЦЗ"), logic(2, "Канал ACC")),
            enabledLogics = emptyList()
        ),
        OutputSetting(
            502, "5.4", "Реле 1", null, SettingValue.Output(OutputValue(listOf(false), Polarity.MINUS)),
            Channel(4, "R1", emptyList()), canEditPolarity = false, defaultPolarity = Polarity.MINUS,
            availableLogics = listOf(logic(0, "Сирена")), enabledLogics = emptyList()
        ),
        osw(503, "5.5", "SIREN", Wire("", emptyList(), 6), listOf(logic(4, "Подогрев сидений")), on = true)
    )

    private val extras = section(
        800,
        "8",
        "Прочее",
        AutoStartScheduleSetting(
            801,
            "8.1",
            "Понедельник",
            null,
            SettingValue.AutoStart(AutoStartSchedule(true, TimeOfDay(7, 30), false, TimeOfDay(18, 0))),
            Weekday.MONDAY
        ),
        SirenSetting(
            802,
            "8.2",
            "Сирена · тревога",
            null,
            SettingValue.Siren(SirenTone(SirenToneType.MELODY, 3)),
            1..12,
            null,
            SirenEvent.ALARM
        ),
        CanCodeSetting(803, "8.3", "CAN-код автомобиля", null, SettingValue.Can(CanCode("7002", null)))
    )
    private val cars = listOf(Car(21, "Kia", "Rio", "IV", 2017..2020), Car(22, "Hyundai", "Solaris", "II", 2017..2020))

    private val root = section(
        0, "", "", general,
        section(
            300,
            "3",
            "Охрана",
            sensors
        ),
        inputs, channelChoice, outputs, extras, section(700, "7", "Резерв")
    )

    private val base = DeviceSettingsState(
        header = SettingsHeader("DX-9X", 1234567, singleCanCode = "2154"),
        menu = SettingsMenu.Ready(root),
        changedIds = setOf(322, 3251),
        sectionChanges = mapOf(300 to 2, 320 to 2, 325 to 1)
    )

    private val noRowActions = SettingRowActions({}, {}, { _, _ -> }, {})
    private val noActions = DeviceSettingsActions(
        onBack = {}, onSearch = {}, onNavigateTo = {}, onRetry = {},
        onTreeWidthChange = {}, onWrite = {}, onExitWrite = {}, onExitDiscard = {},
        onDialogDismiss = {
        }, onResetRequest = {
        }, onResetConfirm = {}, onSingleCanUpdate = {}, editor = EditorActions({}, {}, {}, {}, {}, {})
    )

    private fun render(name: String, state: DeviceSettingsState, path: List<Int>, search: Boolean = false) =
        StateGallery.render(outDir, name) {
            DeviceSettingsContent(
                state = state,
                location = SettingsLocation(path, search),
                actions = noActions,
                snackbarHostState = remember { CmpSnackbarHostState() }
            ) { layout ->
                if (search) {
                    SearchPane(state, layout.isWide, {}, noRowActions)
                } else {
                    SectionEntry(state, path.lastOrNull(), layout, noRowActions)
                }
            }
        }

    private fun editing(id: Int): EditingSetting {
        val setting = root.findNode(id) as Setting
        return EditingSetting(setting, original = setting.value, draft = setting.value)
    }

    @Test
    fun loading() = render("01_loading", base.copy(menu = SettingsMenu.Loading, changedIds = emptySet()), emptyList())

    @Test
    fun error() = render("02_error", base.copy(menu = SettingsMenu.Error, changedIds = emptySet()), emptyList())

    @Test
    fun root() = render("03_root", base, emptyList())

    @Test
    fun sensors() = render("04_sensors", base, listOf(300, 320))

    @Test
    fun general() = render("05_general", base, listOf(100))

    @Test
    fun inputs() = render("06_inputs_stage4", base, listOf(400))

    @Test
    fun channelChoice() = render("19_channel_choice", base.copy(changedIds = setOf(4112)), listOf(411))

    @Test
    fun outputs() = render("20_outputs", base, listOf(500))

    @Test
    fun editOutput() {
        val e = editing(501)
        val draft = SettingValue.Output(OutputValue(listOf(true, false, true), Polarity.MINUS))
        render("21_edit_output", base.copy(editing = e.copy(draft = draft)), listOf(500))
    }

    @Test
    fun editInput() = render("22_edit_input", base.copy(editing = editing(401)), listOf(400))

    @Test
    fun editSchedule() = render("23_edit_schedule", base.copy(editing = editing(801)), listOf(800))

    @Test
    fun editSiren() = render("24_edit_siren", base.copy(editing = editing(802)), listOf(800))

    @Test
    fun editCanMany() = render(
        "25_edit_can_many",
        base.copy(editing = editing(803).copy(carSearch = CarSearch.Found(cars))),
        listOf(800)
    )

    @Test
    fun editCanNone() = render(
        "26_edit_can_none",
        base.copy(editing = editing(803).copy(carSearch = CarSearch.Found(emptyList()))),
        listOf(800)
    )

    @Test
    fun resetConfirm() = render("27_reset_confirm", base.copy(dialog = SettingsDialog.ResetConfirm), listOf(300, 320))

    @Test
    fun resetting() = render("28_resetting", base.copy(isResetting = true), listOf(300, 320))

    @Test
    fun singleCan() = render("29_single_can", base.copy(dialog = SettingsDialog.SingleCanFirmware), listOf(800))

    @Test
    fun emptySection() = render("07_empty", base, listOf(700))

    @Test
    fun search() =
        render("08_search", base.copy(searchQuery = "дат", searchHits = root.search("дат")), emptyList(), true)

    @Test
    fun searchEmpty() = render("09_search_empty", base.copy(searchQuery = "вертолёт"), emptyList(), true)

    @Test
    fun editInteger() {
        val e = editing(324)
        render("10_edit_integer", base.copy(editing = e.copy(draft = SettingValue.Integer(45))), listOf(300, 320))
    }

    @Test
    fun editOption() = render("11_edit_option", base.copy(editing = editing(326)), listOf(300, 320))

    @Test
    fun editText() = render("12_edit_text", base.copy(editing = editing(101)), listOf(100))

    @Test
    fun editDecimal() = render("13_edit_decimal", base.copy(editing = editing(105)), listOf(100))

    @Test
    fun editTime() = render("14_edit_time", base.copy(editing = editing(106)), listOf(100))

    @Test
    fun exitDialog() = render("15_exit", base.copy(dialog = SettingsDialog.ExitConfirm(ExitTarget.BACK)), emptyList())

    @Test
    fun writing() = render("16_writing", base.copy(isWriting = true), listOf(300, 320))

    @Test
    fun writeError() = render("17_write_error", base.copy(dialog = SettingsDialog.WriteError), listOf(300, 320))

    @Test
    fun description() = render(
        "18_description",
        base.copy(dialog = SettingsDialog.Description(root.findNode(321) as Setting)),
        listOf(300, 320)
    )
}
