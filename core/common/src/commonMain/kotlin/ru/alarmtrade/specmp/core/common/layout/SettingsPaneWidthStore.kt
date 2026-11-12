package ru.alarmtrade.specmp.core.common.layout

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.safeRun

internal class SettingsPaneWidthStore(private val settings: Settings) : PaneWidthStore {

    private val mutex = Mutex()
    private val stored = MutableStateFlow(safeRun { settings.keys.associateWith { settings.getInt(it, 0) } })
    override val widthsDp: StateFlow<Response<Map<String, Int>>> = stored.asStateFlow()

    override suspend fun setWidthDp(key: String, widthDp: Int): Response<Unit> = safeRun {
        mutex.withLock {
            settings.putInt(key, widthDp)
            stored.update { Response.Success(it.getValueIfSuccess().orEmpty() + (key to widthDp)) }
        }
    }
}
