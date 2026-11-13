package ru.alarmtrade.specmp.feature.mountassistant.presentation.screen.assistant

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Density
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import org.jetbrains.skia.EncodedImageFormat
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
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
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ResultRequest
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SnapshotChange
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateZone
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StepTitle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.WriteStatus
import ru.alarmtrade.specmp.feature.mountassistant.presentation.components.FeedCallbacks
import ru.alarmtrade.specmp.feature.mountassistant.testing.switchSetting
import ru.alarmtrade.specmp.feature.mountassistant.testing.testCheck
import ru.alarmtrade.specmp.feature.mountassistant.testing.testFacts
import ru.alarmtrade.specmp.feature.mountassistant.testing.testSummary
import ru.alarmtrade.specmp.feature.mountassistant.testing.timeSetting

/**
 * Экран отрисовывается headless в опорных окнах Compact / Medium / Expanded: ошибка раскладки (например, intrinsic-
 * измерение ленивого списка) роняет измерение только на устройстве, а не при сборке. Листы — в `build/assistant-shots`.
 */
class AssistantLayoutRenderTest {
    private val windows = listOf("compact" to (412 to 915), "medium" to (800 to 1280), "expanded" to (1280 to 800))
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

    private val states = mapOf(
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

    private fun render(width: Int, height: Int, content: @Composable () -> Unit): ByteArray {
        val scene = ImageComposeScene(width, height, Density(1f)) { AppTheme(onThemeChange = {}) { content() } }
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
