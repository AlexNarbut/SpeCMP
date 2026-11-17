package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance

import ru.alarmtrade.specmp.core.files.TextFilePickResult
import ru.alarmtrade.specmp.core.files.TextFilePicker
import ru.alarmtrade.specmp.core.files.TextFileType

internal class FakeTextFilePicker(var result: TextFilePickResult = TextFilePickResult.Cancelled) : TextFilePicker {
    val types = mutableListOf<TextFileType>()

    override suspend fun pick(type: TextFileType): TextFilePickResult {
        types += type
        return result
    }
}
