package ru.alarmtrade.specmp.core.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.layout.PaneWidthStore

class FakePaneWidthStore(initial: Map<String, Int> = emptyMap()) : PaneWidthStore {
    override val widthsDp = MutableStateFlow<Response<Map<String, Int>>>(Response.Success(initial))

    fun widthDp(key: String): Int? = widthsDp.value.getValueIfSuccess()?.get(key)

    override suspend fun setWidthDp(key: String, widthDp: Int): Response<Unit> {
        widthsDp.update { Response.Success(it.getValueIfSuccess().orEmpty() + (key to widthDp)) }
        return Response.Success(Unit)
    }
}
