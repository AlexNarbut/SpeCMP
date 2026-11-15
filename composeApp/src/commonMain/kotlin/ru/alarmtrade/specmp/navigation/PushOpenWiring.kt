package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.filterNotNull
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.push.PushOpenRequests
import ru.alarmtrade.specmp.core.push.PushTarget
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatRoute

private val logger = Logger.withTag("PushOpenWiring")

/**
 * Тап по уведомлению — только в авторизованной зоне: без входа тап ждёт в [PushOpenRequests] и выполнится после
 * него. Чат открывается поверх текущего экрана — работа с блоком не прерывается.
 */
@Composable
internal fun PushOpenEffect(backStack: NavBackStack<NavKey>) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(backStack) {
        PushOpenRequests.pending.filterNotNull().collect { target ->
            if (!PushOpenRequests.consume(target)) return@collect
            when (target) {
                PushTarget.Chat -> backStack.navigateSafe(SupportChatRoute)

                is PushTarget.Url -> safeRun { uriHandler.openUri(target.url) }
                    .onError { logger.w(it) { "Push link is not opened" } }

                PushTarget.App -> Unit
            }
        }
    }
}
