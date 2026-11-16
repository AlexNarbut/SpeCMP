package ru.alarmtrade.specmp.flasher.navigation

import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

/** Транспорт выбран до входа в устройство и за сессию не меняется — читается один раз. */
internal class FlasherDeviceViewModel(session: DeviceSession) :
    BaseViewModel<FlasherDeviceState, Nothing>(
        FlasherDeviceState(isSerial = session.transport.value == ConnectionTransport.SERIAL),
    )
