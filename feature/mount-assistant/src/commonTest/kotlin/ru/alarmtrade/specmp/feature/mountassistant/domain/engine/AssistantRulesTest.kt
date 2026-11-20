package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ActionKind
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.Availability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.BlockReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.MountProgress
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.NextStep
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.NextStepReason
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport

class AssistantRulesTest {
    private val empty = MountProgress()
    private val factoryBlocked = Availability.Blocked(BlockReason.FACTORY_CAN, AssistantAction.CAN_UPDATE)
    private val offline = Availability.Blocked(BlockReason.NO_NETWORK, null)

    private fun visible(
        facts: ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts,
        progress: MountProgress = empty,
    ) = AssistantAction.entries.filter { availability(it, facts, progress) != Availability.Hidden }.toSet()

    @Test
    fun `full alarm over bluetooth shows everything but firmware update and its absence of check`() {
        val expected = AssistantAction.entries.toSet() - AssistantAction.FIRMWARE_UPDATE
        assertEquals(expected, visible(facts()))
    }

    @Test
    fun `firmware update appears only after a check found a newer version`() {
        val action = AssistantAction.FIRMWARE_UPDATE
        assertEquals(Availability.Hidden, availability(action, facts(), MountProgress(firmware = firmwareCheck(false))))
        assertEquals(
            Availability.Available,
            availability(action, facts(), MountProgress(firmware = firmwareCheck(true))),
        )
    }

    @Test
    fun `usb hides state actions only`() {
        val shown = visible(facts(transport = AssistantTransport.USB))
        assertFalse(AssistantAction.STATE in shown)
        assertFalse(AssistantAction.STATE_FULL in shown)
        assertTrue(AssistantAction.SETTINGS_FIND in shown)
        assertTrue(AssistantAction.BYPASS_RUN in shown)
    }

    @Test
    fun `unknown model shows firmware, vehicles and support only`() {
        val shown = visible(facts(capabilities = null), MountProgress(firmware = firmwareCheck(true)))
        assertEquals(AssistantAction.ForUnknownModel, shown)
    }

    @Test
    fun `missing capability hides its actions`() {
        val shown = visible(facts(capabilities = setOf(AssistantCapability.FIRMWARE)))
        assertEquals(
            setOf(
                AssistantAction.FIRMWARE_CHECK,
                AssistantAction.CAN_UPDATE,
                AssistantAction.FIRMWARE_ARCHIVE,
                AssistantAction.STATE,
                AssistantAction.STATE_FULL,
                AssistantAction.MOUNT_PASSPORT,
                AssistantAction.SYSTEM_DEVICES,
                AssistantAction.PRODUCT_INFO,
                AssistantAction.VEHICLES,
                AssistantAction.TICKET_NEW,
                AssistantAction.TICKETS,
                AssistantAction.SUPPORT,
            ),
            shown,
        )
    }

    @Test
    fun `status is shown when only clone is supported`() {
        val shown = visible(facts(capabilities = setOf(AssistantCapability.CLONE)))
        assertTrue(AssistantAction.AUTOSTART_STATUS in shown)
        assertTrue(AssistantAction.CLONE_RUN in shown)
        assertFalse(AssistantAction.BYPASS_RUN in shown)
    }

    @Test
    fun `device without can hides can update and does not lock settings`() {
        val noCan = facts(can = null)
        assertEquals(Availability.Hidden, availability(AssistantAction.CAN_UPDATE, noCan, empty))
        assertEquals(Availability.Available, availability(AssistantAction.SETTINGS_FIND, noCan, empty))
        assertEquals(Availability.Available, availability(AssistantAction.BYPASS_RUN, noCan, empty))
    }

    @Test
    fun `factory can blocks settings and autostart with can update as fix`() {
        val factory = facts(can = FactoryCan)
        listOf(
            AssistantAction.SETTINGS_FIND,
            AssistantAction.SETTINGS_LOAD,
            AssistantAction.SETTINGS_ALL,
            AssistantAction.AUTOSTART_STATUS,
            AssistantAction.BYPASS_RUN,
            AssistantAction.CLONE_RUN,
        ).forEach { assertEquals(factoryBlocked, availability(it, factory, empty), it.name) }
        assertEquals(Availability.Available, availability(AssistantAction.CONFIGURATOR, factory, empty))
        assertEquals(Availability.Available, availability(AssistantAction.CAN_UPDATE, factory, empty))
        assertEquals(Availability.Available, availability(AssistantAction.FIRMWARE_CHECK, factory, empty))
    }

    @Test
    fun `factory can wins over missing network`() {
        val both = facts(can = FactoryCan, online = false)
        assertEquals(factoryBlocked, availability(AssistantAction.CLONE_RUN, both, empty))
        assertEquals(offline, availability(AssistantAction.CAN_UPDATE, both, empty))
    }

    @Test
    fun `no network blocks server actions only`() {
        val off = facts(online = false)
        listOf(
            AssistantAction.FIRMWARE_CHECK,
            AssistantAction.CAN_UPDATE,
            AssistantAction.FIRMWARE_ARCHIVE,
            AssistantAction.CLONE_RUN,
        ).forEach { assertEquals(offline, availability(it, off, empty), it.name) }
        assertEquals(Availability.Available, availability(AssistantAction.BYPASS_RUN, off, empty))
        assertEquals(Availability.Available, availability(AssistantAction.STATE, off, empty))
        assertEquals(Availability.Available, availability(AssistantAction.SUPPORT, off, empty))
    }

    @Test
    fun `topics keep order, drop empty and mark fully blocked`() {
        // Без «Информации о товаре»: она не зависит от CAN и сняла бы блокировку темы устройств.
        val list = topics(facts(transport = AssistantTransport.USB, can = FactoryCan, productSearch = false), empty)
        assertEquals(
            listOf(
                AssistantTopic.FIRMWARE,
                AssistantTopic.AUTOSTART,
                AssistantTopic.SETTINGS,
                AssistantTopic.DEVICES,
                AssistantTopic.VEHICLE,
                AssistantTopic.SUPPORT,
            ),
            list.map { it.topic },
        )
        assertTrue(list.first { it.topic == AssistantTopic.DEVICES }.blocked)
        assertTrue(list.first { it.topic == AssistantTopic.AUTOSTART }.blocked)
        assertFalse(list.first { it.topic == AssistantTopic.SETTINGS }.blocked)
        assertFalse(list.first { it.topic == AssistantTopic.FIRMWARE }.blocked)
    }

    @Test
    fun `route goes can, firmware, bypass, clone, state, settings`() {
        val full = facts()
        assertEquals(
            NextStep(AssistantAction.CAN_UPDATE, NextStepReason.FACTORY_CAN),
            nextStep(facts(can = FactoryCan), empty),
        )
        assertEquals(NextStep(AssistantAction.FIRMWARE_CHECK, NextStepReason.FIRMWARE_UNCHECKED), nextStep(full, empty))
        var progress = MountProgress(firmware = firmwareCheck(true))
        assertEquals(
            NextStep(AssistantAction.FIRMWARE_UPDATE, NextStepReason.FIRMWARE_OUTDATED),
            nextStep(full, progress),
        )
        progress = progress.copy(firmware = firmwareCheck(false))
        assertEquals(
            NextStep(AssistantAction.AUTOSTART_STATUS, NextStepReason.AUTOSTART_UNKNOWN),
            nextStep(full, progress),
        )
        progress = progress.copy(autostart = autostart(learned = false, clone = CloneState.REQUIRED))
        assertEquals(NextStep(AssistantAction.BYPASS_RUN, NextStepReason.BYPASS_NOT_LEARNED), nextStep(full, progress))
        progress = progress.copy(autostart = autostart(learned = true, clone = CloneState.REQUIRED))
        assertEquals(NextStep(AssistantAction.CLONE_RUN, NextStepReason.CLONE_REQUIRED), nextStep(full, progress))
        progress = progress.copy(autostart = autostart(learned = true, clone = CloneState.DONE))
        assertEquals(NextStep(AssistantAction.STATE, NextStepReason.STATE_UNCHECKED), nextStep(full, progress))
        progress = progress.copy(state = StateSummary(true, emptySet(), false, false, 12.6f, 20))
        assertEquals(
            NextStep(AssistantAction.MOUNT_PASSPORT, NextStepReason.PASSPORT_NOT_ISSUED),
            nextStep(full, progress),
        )
        progress = progress.copy(passport = TodayPassport("p1", "20261119-1", issued = false))
        assertEquals(
            NextStep(AssistantAction.MOUNT_PASSPORT, NextStepReason.PASSPORT_NOT_ISSUED),
            nextStep(full, progress),
        )
        progress = progress.copy(passport = TodayPassport("p1", "20261119-1", issued = true))
        assertEquals(NextStep(AssistantAction.SETTINGS_FIND, NextStepReason.ROUTE_DONE), nextStep(full, progress))
    }

    @Test
    fun `route skips links that do not apply`() {
        val module = facts(
            transport = AssistantTransport.USB,
            capabilities = setOf(AssistantCapability.SETTINGS),
            can = null,
        )
        val issued = MountProgress(passport = TodayPassport("p1", "1", issued = true))
        assertEquals(
            NextStep(AssistantAction.MOUNT_PASSPORT, NextStepReason.PASSPORT_NOT_ISSUED),
            nextStep(module, empty),
        )
        assertEquals(NextStep(AssistantAction.SETTINGS_FIND, NextStepReason.ROUTE_DONE), nextStep(module, issued))
        val bare = facts(transport = AssistantTransport.USB, capabilities = emptySet(), can = null)
        assertEquals(NextStep(AssistantAction.SUPPORT, NextStepReason.NOTHING_LEFT), nextStep(bare, issued))
    }

    @Test
    fun `mount passport needs nothing from the unit, network or transport`() {
        val action = AssistantAction.MOUNT_PASSPORT
        assertEquals(AssistantTopic.STATE, action.topic)
        assertEquals(ActionKind.HANDOFF, action.kind)
        val bare = facts(transport = AssistantTransport.USB, capabilities = emptySet(), can = null, online = false)
        assertEquals(Availability.Available, availability(action, bare, empty))
        assertEquals(Availability.Available, availability(action, facts(can = FactoryCan, online = false), empty))
        assertEquals(Availability.Hidden, availability(action, facts(capabilities = null), empty))
    }

    @Test
    fun `mount passport has its own dock tile and is not listed in the state topic`() {
        assertFalse(AssistantAction.MOUNT_PASSPORT in AssistantAction.of(AssistantTopic.STATE))
        val usbState = topics(facts(transport = AssistantTransport.USB), empty).firstOrNull {
            it.topic == AssistantTopic.STATE
        }
        assertEquals(null, usbState)
    }

    @Test
    fun `offline skips firmware and clone`() {
        val off = facts(online = false)
        val progress = MountProgress(autostart = autostart(learned = true, clone = CloneState.REQUIRED))
        assertEquals(NextStep(AssistantAction.STATE, NextStepReason.STATE_UNCHECKED), nextStep(off, progress))
    }

    @Test
    fun `unknown model leads to firmware or support`() {
        assertEquals(
            NextStep(AssistantAction.FIRMWARE_CHECK, NextStepReason.UNKNOWN_MODEL_FIRMWARE),
            nextStep(facts(capabilities = null), empty),
        )
        assertEquals(
            NextStep(AssistantAction.SUPPORT, NextStepReason.UNKNOWN_MODEL_SUPPORT),
            nextStep(facts(capabilities = null, online = false), empty),
        )
    }

    @Test
    fun `product info needs the account feature and the network, not the model`() {
        val action = AssistantAction.PRODUCT_INFO
        assertEquals(Availability.Hidden, availability(action, facts(productSearch = false), empty))
        assertEquals(offline, availability(action, facts(online = false), empty))
        assertEquals(Availability.Available, availability(action, facts(capabilities = null), empty))
        assertEquals(Availability.Available, availability(action, facts(transport = AssistantTransport.USB), empty))
    }
}
