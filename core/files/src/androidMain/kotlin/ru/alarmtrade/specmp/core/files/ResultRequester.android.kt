package ru.alarmtrade.specmp.core.files

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private val logger = Logger.withTag("ResultRequester")

/**
 * Ожидание — на уровне процесса, лаунчер — текущей композиции: после поворота экрана registry доставит результат
 * новому лаунчеру, и он завершит запрос, начатый через старый.
 */
internal class ResultRequester<I : Any, O : Any>(private val name: String) {
    private var launcher: ActivityResultLauncher<I>? = null
    private var pending: CompletableDeferred<O?>? = null

    @Composable
    fun Bind(current: ActivityResultLauncher<I>) {
        DisposableEffect(current) {
            launcher = current
            onDispose { if (launcher === current) launcher = null }
        }
    }

    // Нет системного приложения (документы, камера) — ActivityNotFoundException: запрос не начат.
    @Suppress("TooGenericExceptionCaught")
    suspend fun request(input: I, main: CoroutineDispatcher): CompletableDeferred<O?>? = withContext(main) {
        val current = launcher ?: return@withContext null
        val deferred = CompletableDeferred<O?>()
        pending?.complete(null)
        pending = deferred
        try {
            current.launch(input)
            deferred
        } catch (e: Exception) {
            logger.w(e) { "$name launch failed" }
            pending = null
            null
        }
    }

    fun onResult(result: O?) {
        pending?.complete(result)
        pending = null
    }
}
