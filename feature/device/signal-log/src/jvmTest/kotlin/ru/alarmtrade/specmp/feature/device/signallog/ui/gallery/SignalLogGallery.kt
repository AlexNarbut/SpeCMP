package ru.alarmtrade.specmp.feature.device.signallog.ui.gallery

import java.io.File
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogActions
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogContent
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogDialog
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogError
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogShareAction
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogShareForm
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.SignalLogState

/**
 * Поиска здесь нет: поле поиска запрашивает фокус, а headless-сцена обрабатывает его в другом потоке и падает.
 * Состояния «Лога состояния» (макет `design/features/signal-log`); PNG — в `build/state-gallery/signal-log`.
 */
class SignalLogGallery {

    private val outDir = File("build/state-gallery/signal-log")

    private val texts = listOf(
        "GUARD ON: zones=0x1F, delay=0s, src=KEY_FOB",
        "Охрана включена брелоком, задержка 0 с",
        "TILT calib ok: x=+0.4° y=-1.2°",
        "SHOCK lvl1=12 lvl2=28, hit=9 → ниже порога",
        "CAN2 rx 0x3B5 [01 00 00 00 FF 00 00 00]",
        "Автозапуск: обороты пропали через 9.8 с → стоп, код RPM_LOST",
        "ALARM: src=TILT Δ=3.1° > 1.5°, цикл 1/8",
        "CAN_ERR: bus-off на CAN2 (BCM), tec=255 rec=0, перезапуск через 500 мс; проверьте подключение CAN-H/CAN-L",
        "BAT 12.42V, T=+4°C",
        "Тревога сброшена брелоком",
    )

    private val start = LocalDateTime(2026, 11, 17, 23, 58, 30).toInstant(TimeZone.currentSystemDefault())

    private val log = (1L..60L).map { id ->
        SignalLogLine(
            id = id,
            timeMs = start.toEpochMilliseconds() + id * 7_000L,
            text = texts[(id % texts.size).toInt()],
        )
    }

    private val ready = SignalLogState(
        deviceName = "Pandora DX-4GS",
        serial = "107131",
        load = LCEState.Content(Unit),
        lines = log.asReversed().toPersistentList(),
        hasOlder = false,
        summary = SignalLogSummary(log.size, log.first().timeMs, log.last().timeMs),
    )

    private fun render(name: String, state: SignalLogState) =
        StateGallery.render(outDir, name) { SignalLogContent(state = state, actions = SignalLogActions()) }

    @Test
    fun p01Live() = render("p01_live", ready)

    @Test
    fun p02Loading() = render("p02_loading", ready.copy(load = LCEState.Loading, lines = persistentListOf()))

    @Test
    fun p03EmptyConnected() = render("p03_empty_connected", emptyLog(connected = true))

    @Test
    fun p04EmptyOffline() = render("p04_empty_offline", emptyLog(connected = false))

    @Test
    fun p05Paused() = render("p05_paused", ready.copy(paused = true, pendingCount = 37))

    @Test
    fun p06Offline() = render("p06_offline", ready.copy(connected = false))

    @Test
    fun p07LoadingOlder() = render("p07_older", ready.copy(hasOlder = true, loadingOlder = true))

    @Test
    fun p08Error() = render("p08_error", ready.copy(load = LCEState.Error(SignalLogError.NO_SERIAL)))

    @Test
    fun p11ClearDialog() = render("p11_clear", ready.copy(dialog = SignalLogDialog.ConfirmClear(log.size)))

    @Test
    fun p12PanelFailed() = render(
        "p12_panel_failed",
        ready.copy(share = SignalLogShareForm(problem = "Сама уходит в тревогу", sendFailed = true)),
    )

    @Test
    fun p13PanelSending() = render(
        "p13_panel_sending",
        ready.copy(share = SignalLogShareForm(problem = "Сама уходит в тревогу", busy = SignalLogShareAction.SEND)),
    )

    @Test
    fun p14PanelProblemError() = render("p14_panel_error", ready.copy(share = SignalLogShareForm(problemError = true)))

    private fun emptyLog(connected: Boolean) = ready.copy(
        lines = persistentListOf(),
        summary = SignalLogSummary(0, null, null),
        connected = connected,
    )
}
