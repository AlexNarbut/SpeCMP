package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

/**
 * Откуда экран загрузчика предлагает взять прошивку. [All] — сервер (последняя версия канала), архив и файл;
 * [FileOnly] — только файл из файлового менеджера: без канала, сервера и чата поддержки (приложение прошивки).
 */
enum class FirmwareSources { All, FileOnly }
