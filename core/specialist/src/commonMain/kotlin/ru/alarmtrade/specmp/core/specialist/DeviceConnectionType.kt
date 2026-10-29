package ru.alarmtrade.specmp.core.specialist

/**
 * Тип соединения с устройством, поддерживаемый Specialist SDK на платформе. [SERIAL] — COM-порт (USB-UART адаптер,
 * `SerialConnectorInteractor`): прошивка `.frm` по serial; в поиске включается только приложением прошивки.
 */
enum class DeviceConnectionType { BLUETOOTH, USB, SERIAL }
