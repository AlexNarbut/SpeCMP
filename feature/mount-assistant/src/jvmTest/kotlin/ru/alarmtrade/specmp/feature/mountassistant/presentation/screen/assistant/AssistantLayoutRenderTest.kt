package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Density
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import org.jetbrains.skia.EncodedImageFormat
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.core.ui.theme.LocalThemeIsDark
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalEntry
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.JournalState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultContent
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultFailure
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultRequest
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SnapshotChange
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateZone
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StepTitle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteStatus
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.FeedCallbacks
import ru.alarmtrade.specmp.feature.mountassistant.testing.switchSetting
import ru.alarmtrade.specmp.feature.mountassistant.testing.testCheck
import ru.alarmtrade.specmp.feature.mountassistant.testing.testFacts
import ru.alarmtrade.specmp.feature.mountassistant.testing.testProduct
import ru.alarmtrade.specmp.feature.mountassistant.testing.testSummary
import ru.alarmtrade.specmp.feature.mountassistant.testing.timeSetting

/**
 * Экран отрисовывается headless в опорных окнах Compact / Medium / Expanded и низких окнах (телефон-ландшафт, светлая и
 * тёмная тема): ошибка раскладки (например, intrinsic-измерение ленивого списка) роняет измерение только на устройстве,
 * а не при сборке. Листы — в `build/assistant-shots`.
 */
class AssistantLayoutRenderTest {
    private val windows = listOf(
        "compact" to (412 to 915),
        "medium" to (800 to 1280),
        "expanded" to (1280 to 800),
        "low-iphone" to (852 to 393),
        "low-safe" to (734 to 372),
    )
    private val lowWindows = windows.filter { it.first.startsWith("low") }
    private val at = Instant.fromEpochSeconds(1_700_000_000)
    private val outDir = File("build/assistant-shots")

    private val actions = AssistantScreenActions(
        onTile = {},
        onTopicBack = {},
        onQueryChange = {},
        onSubmit = {},
        onDockToggle = {},
        onFeedScrolledUp = {},
        onHeaderToggle = {},
        onCanUpdate = {},
        feed = FeedCallbacks(
            onRetry = {},
            onModuleArchive = {},
            onModuleFlash = {},
            onEditStart = { _, _ -> },
            onDraftChange = { _, _ -> },
            onEditCancel = {},
            onEditNext = {},
            onOpenInSettings = {},
            onWrite = {},
            onWriteCancel = {},
            onSourcePick = { _, _ -> },
            onAction = {},
            onProductOpen = {},
        ),
    )

    private fun step(id: Long, action: AssistantAction) = JournalEntry.Step(id, StepTitle.Action(action), at)

    private val fullJournal = listOf(
        JournalEntry.Greeting(1),
        step(2, AssistantAction.FIRMWARE_CHECK),
        JournalEntry.Result(3, ResultRequest.Firmware, ResultContent.FirmwareData(testCheck, testFacts.can)),
        step(4, AssistantAction.STATE),
        JournalEntry.Result(
            5,
            ResultRequest.State,
            ResultContent.StateData(testSummary.copy(openZones = setOf(StateZone.HOOD))),
        ),
        step(6, AssistantAction.AUTOSTART_STATUS),
        JournalEntry.Result(
            7,
            ResultRequest.Autostart,
            ResultContent.AutostartData(AutostartStatus(true, CloneState.REQUIRED, 3), switchSetting),
        ),
        JournalEntry.Step(8, StepTitle.Search("темп"), at),
        JournalEntry.Result(
            9,
            ResultRequest.Settings("темп"),
            ResultContent.SettingsData(
                found = listOf(switchSetting, timeSetting),
                editing = switchSetting.id,
                draft = AssistantSettingValue.Switch(true),
            ),
        ),
        JournalEntry.Confirm(
            id = 10,
            setting = switchSetting,
            from = AssistantSettingValue.Switch(false),
            to = AssistantSettingValue.Switch(true),
            status = WriteStatus.PENDING,
            otherUnsaved = 2,
        ),
        step(11, AssistantAction.SETTINGS_LOAD),
        JournalEntry.SourcePicker(12),
        JournalEntry.Notice(13, BlockReason.NO_NETWORK),
        step(14, AssistantAction.SETTINGS_ALL),
        JournalEntry.Return(15, AssistantAction.SETTINGS_ALL, listOf(SnapshotChange.Settings(0, 2, listOf("A", "B")))),
        JournalEntry.Return(16, AssistantAction.BYPASS_RUN, changes = null),
        JournalEntry.Return(17, AssistantAction.CLONE_RUN, changes = emptyList()),
    )

    private val productJournal = listOf(
        JournalEntry.Greeting(1),
        step(2, AssistantAction.PRODUCT_INFO),
        JournalEntry.Result(3, ResultRequest.Product, ResultContent.ProductData(testProduct)),
        step(4, AssistantAction.PRODUCT_INFO),
        JournalEntry.Result(5, ResultRequest.Product, ResultContent.ProductNotFound(77)),
        step(6, AssistantAction.PRODUCT_INFO),
        JournalEntry.Result(7, ResultRequest.Product, ResultContent.Error(ResultFailure.Common(ApiError.NoConnection))),
        JournalEntry.Step(8, StepTitle.ProductNumber("4123700107131"), at),
    )

    private val issuedPassport = TodayPassport("p1", "20261119-234567-1", issued = true)

    private val routeDone = MountProgress(
        firmware = testCheck.copy(modules = testCheck.modules.map { it.copy(updateAvailable = false) }),
        autostart = AutostartStatus(true, CloneState.DONE, 2),
        state = testSummary,
    )

    private val states = mapOf(
        "product" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(entries = productJournal),
            level = DockLevel.Topic(AssistantTopic.DEVICES),
        ),
        "product-search" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(entries = productJournal.take(3)),
            query = "4123700107131",
        ),
        "first" to
            MountAssistantState(facts = testFacts, journal = JournalState(entries = listOf(JournalEntry.Greeting(1)))),
        "topic" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(entries = fullJournal.take(3), progress = MountProgress(firmware = testCheck)),
            level = DockLevel.Topic(AssistantTopic.FIRMWARE),
        ),
        "search" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(entries = fullJournal.take(3)),
            query = "настройки прошивка",
            liveSettings = listOf(switchSetting, timeSetting),
        ),
        "collapsed" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(entries = fullJournal.take(5)),
            dockCollapsed = true,
        ),
        "feed" to MountAssistantState(facts = testFacts, journal = JournalState(entries = fullJournal)),
        "passport-offer" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(
                entries = fullJournal.take(5) + JournalEntry.Passport(18, passport = null),
                progress = routeDone,
            ),
        ),
        "passport-issued" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(
                entries = fullJournal.take(5) + listOf(
                    step(18, AssistantAction.MOUNT_PASSPORT),
                    JournalEntry.Passport(19, issuedPassport),
                ),
                progress = routeDone.copy(passport = issuedPassport),
            ),
        ),
        "passport-search" to MountAssistantState(
            facts = testFacts,
            journal = JournalState(entries = fullJournal.take(3)),
            query = "акт",
        ),
        "limited" to MountAssistantState(
            facts = testFacts.copy(capabilities = null, can = null, online = false),
            journal = JournalState(entries = listOf(JournalEntry.Greeting(1))),
        ),
    )

    @Test
    fun `every layout renders every state`() {
        outDir.mkdirs()
        var shots = 0
        states.forEach { (name, state) ->
            windows.forEach { (window, size) ->
                val png = render(size.first, size.second) {
                    AssistantBody(state, state.facts ?: testFacts, actions, FocusRequester())
                }
                File(outDir, "$name-$window.png").writeBytes(png)
                shots++
            }
        }
        assertTrue(shots == states.size * windows.size)
    }

    @Test
    fun `low layouts render in dark theme`() {
        outDir.mkdirs()
        states.forEach { (name, state) ->
            lowWindows.forEach { (window, size) ->
                val png = render(size.first, size.second, dark = true) {
                    AssistantBody(state, state.facts ?: testFacts, actions, FocusRequester())
                }
                File(outDir, "$name-$window-dark.png").writeBytes(png)
            }
        }
        assertTrue(outDir.listFiles().orEmpty().count { it.name.endsWith("-dark.png") } >= states.size)
    }

    private fun render(width: Int, height: Int, dark: Boolean = false, content: @Composable () -> Unit): ByteArray {
        val scene = ImageComposeScene(width, height, Density(1f)) {
            AppTheme(onThemeChange = {}) {
                val isDark = LocalThemeIsDark.current
                SideEffect { isDark.value = dark }
                content()
            }
        }
        return try {
            var image = scene.render(0)
            for (frame in 1..FRAMES) image = scene.render(frame * FRAME_NANOS)
            checkNotNull(image.encodeToData(EncodedImageFormat.PNG)).bytes
        } finally {
            scene.close()
        }
    }

    private companion object {
        const val FRAMES = 12
        const val FRAME_NANOS = 100_000_000L
    }
}
