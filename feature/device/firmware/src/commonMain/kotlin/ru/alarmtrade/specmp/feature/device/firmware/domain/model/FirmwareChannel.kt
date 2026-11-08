package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/** Канал обновления прошивки; [isTest] — тестовые версии, о которых UI предупреждает. */
enum class FirmwareChannel(val isTest: Boolean) {
    RELEASE(isTest = false),
    BETA(isTest = true),
    ALPHA(isTest = true),
}
