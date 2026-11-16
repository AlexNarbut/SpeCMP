package ru.alarmtrade.specmp.flasher.navigation

import androidx.compose.runtime.Immutable

/** [isSerial] — блок подключён по COM: прошивка своим экраном, загрузчик SDK входит сам. */
@Immutable
internal data class FlasherDeviceState(val isSerial: Boolean)
