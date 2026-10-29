package ru.alarmtrade.specmp.core.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_months
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_today
import ru.alarmtrade.specmp.core.localization.resources.common_date_label_yesterday

/**
 * Общий лейблер дат (U-26): «Сегодня»/«Вчера»/«14 марта», год добавляется,
 * если он не текущий. Месяцы и примитивные даты — общие строки core/ui
 * (правило 2 docs/localization.md); язык — языка запуска (смена языка перезапускает приложение).
 *
 * Возвращает запомненную лямбду, чтобы фичи вызывали её из некомпозабельных
 * контекстов (например, из remember-блоков построения списков).
 */
@Composable
fun rememberCmpDateLabeler(): (LocalDate) -> String {
    val todayLabel = stringResource(Strings.string.common_date_label_today)
    val yesterdayLabel = stringResource(Strings.string.common_date_label_yesterday)
    val monthLabels = stringArrayResource(Strings.array.common_date_label_months)
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = now.minus(DatePeriod(days = 1))

    return remember(todayLabel, yesterdayLabel, monthLabels, now) {
        { date ->
            when (date) {
                now -> todayLabel
                yesterday -> yesterdayLabel
                else -> formatLocalizedDate(date, monthLabels, now.year)
            }
        }
    }
}

private fun formatLocalizedDate(date: LocalDate, monthLabels: List<String>, currentYear: Int): String {
    val month = monthLabels.getOrNull(date.monthNumber - 1).orEmpty()
    return buildString {
        append(date.dayOfMonth)
        if (month.isNotBlank()) {
            append(' ')
            append(month)
        }
        if (date.year != currentYear) {
            append(' ')
            append(date.year)
        }
    }
}
