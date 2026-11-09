package ru.alarmtrade.specmp.feature.fastmount.ui

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountConfiguration
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountFile
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountQuestion
import ru.alarmtrade.specmp.feature.fastmount.domain.model.QuestionAnswer

internal fun testDevice(
    systemId: Long = 77,
    modelInDatabase: Boolean = true,
    firmware: String = "VER3.57",
    can: CanInfo? = CanInfo(firmwareVersion = "1.2", code = "0213", isFactory = false),
) = AlarmDevice(
    systemId = systemId,
    systemName = "DX-9X",
    firmwareVersion = firmware,
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = can,
    modelInDatabase = modelInDatabase,
)

internal fun question(id: Int, automatic: Boolean = false) = MountQuestion(
    id = id,
    group = "",
    task = "Задание $id",
    expected = "",
    automatic = automatic,
    answers = if (automatic) emptyList() else listOf(QuestionAnswer("Да", 1), QuestionAnswer("Нет", 2)),
)

internal fun testConfiguration(
    keyBypass: Boolean = true,
    slave: Boolean = true,
    mountingMap: MountFile? = null,
    questions: List<MountQuestion> = listOf(question(5, automatic = true), question(100), question(101)),
) = FastMountConfiguration(
    id = 42,
    name = "Polo VI",
    shortDescription = "",
    fullDescription = "<p>Описание</p>",
    deviceName = "DX-9X",
    author = "Автор",
    rating = 5,
    mountingMap = mountingMap,
    images = emptyList(),
    keyBypass = keyBypass,
    slave = slave,
    questions = questions,
)
