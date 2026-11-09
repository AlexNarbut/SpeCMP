package ru.alarmtrade.specmp.core.ui.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource

// Корни композиции (App, FlasherApp) провайдят часы и пояс из Koin;
// системный дефолт — для превью и тестов без провайдера.
val LocalClock = staticCompositionLocalOf<Clock> { Clock.System }

val LocalTimeZoneSource = staticCompositionLocalOf { TimeZoneSource.System }

@Composable
@ReadOnlyComposable
fun currentTimeZone(): TimeZone = LocalTimeZoneSource.current.current()

/** «Сегодня» на момент композиции: перечитывается при каждой рекомпозиции (метки «Сегодня/Вчера»). */
@Composable
@ReadOnlyComposable
fun currentToday(): LocalDate = LocalClock.current.todayIn(currentTimeZone())
