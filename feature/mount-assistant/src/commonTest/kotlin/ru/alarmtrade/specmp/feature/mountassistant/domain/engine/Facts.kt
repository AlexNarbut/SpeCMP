package ru.alarmtrade.specmp.feature.mountassistant.domain.engine

import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole

internal val AllCapabilities = AssistantCapability.entries.toSet()

internal fun facts(
    transport: AssistantTransport = AssistantTransport.BLE,
    capabilities: Set<AssistantCapability>? = AllCapabilities,
    can: CanFacts? = CanFacts(factory = false, code = "1234", version = "12.05"),
    online: Boolean = true,
    firmware: String = "3.56",
) = DeviceFacts(
    systemId = 1,
    title = "DX-4G",
    firmwareVersion = firmware,
    transport = transport,
    capabilities = capabilities,
    can = can,
    online = online,
)

internal val FactoryCan = CanFacts(factory = true, code = null, version = null)

internal fun firmwareCheck(update: Boolean) = FirmwareCheck(
    listOf(
        FirmwareModuleCheck(
            id = 1,
            role = FirmwareModuleRole.MAIN,
            title = "DX-4G",
            installed = "3.56",
            latest = if (update) "3.57" else "3.56",
            updateAvailable = update,
        ),
    ),
)

internal fun autostart(learned: Boolean, clone: CloneState = CloneState.NOT_NEEDED, attempts: Int? = 3) =
    AutostartStatus(bypassLearned = learned, clone = clone, cloneAttempts = attempts)
