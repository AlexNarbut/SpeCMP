package ru.alarmtrade.specmp.feature.mountpassport.presentation

import ru.alarmtrade.specmp.core.files.BinaryFilePickResult
import ru.alarmtrade.specmp.core.files.BinaryFilePicker
import ru.alarmtrade.specmp.core.files.BinaryFileType

internal class FakeBinaryFilePicker : BinaryFilePicker {
    var result: BinaryFilePickResult = BinaryFilePickResult.Picked("Паспорта.zip", "/cache/file-import/Паспорта.zip")
    val requests = mutableListOf<BinaryFileType>()

    override suspend fun pick(type: BinaryFileType): BinaryFilePickResult {
        requests += type
        return result
    }
}
