package ru.alarmtrade.specmp.feature.device.settings.data.files

import java.io.File

internal actual fun settingsScratchDirectory(): String =
    File(System.getProperty("java.io.tmpdir"), SETTINGS_SCRATCH_DIR).apply { mkdirs() }.absolutePath

internal actual fun deleteSettingsScratchFile(path: String) {
    File(path).delete()
}

private const val SETTINGS_SCRATCH_DIR = "settings-files"
