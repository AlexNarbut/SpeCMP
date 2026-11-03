package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.step

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

/**
 * Обход и Clone как шаг чужого мастера (быстрый монтаж): шапку и нижнюю панель рисует мастер, автозапуск — тело,
 * статус, кнопки и диалоги со своей VM.
 *
 * @property topBar шапка мастера; `locked` — вместо «Назад» замок (идёт критичный этап).
 * @property dock нижняя панель мастера с кнопками шага.
 */
@Immutable
class AutostartStepChrome(
    val topBar: @Composable (locked: Boolean, onBack: () -> Unit) -> Unit,
    val dock: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit
)

/**
 * Переход мастера к следующему шагу после успешного итога.
 *
 * @property busy мастер ждёт ответа — кнопка «Далее» со спиннером.
 * @property error причина, по которой мастер не пустил дальше (плашка над кнопкой); `null` — нет.
 */
@Immutable
class AutostartStepNext(val busy: Boolean, val error: String?)
