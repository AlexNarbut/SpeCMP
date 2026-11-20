package ru.alarmtrade.specmp.feature.mountassistant.presentation.model

import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTopic

internal val AssistantTopic.icon: CmpIcons
    get() = when (this) {
        AssistantTopic.FIRMWARE -> CmpIcons.SystemUpdate
        AssistantTopic.AUTOSTART -> CmpIcons.Key
        AssistantTopic.SETTINGS -> CmpIcons.Tune
        AssistantTopic.STATE -> CmpIcons.Speed
        AssistantTopic.DEVICES -> CmpIcons.SettingsRemote
        AssistantTopic.VEHICLE -> CmpIcons.DirectionsCar
        AssistantTopic.SUPPORT -> CmpIcons.SupportAgent
    }

internal val AssistantAction.icon: CmpIcons
    get() = when (this) {
        AssistantAction.FIRMWARE_UPDATE -> CmpIcons.ArrowCircleUp
        AssistantAction.FIRMWARE_CHECK -> CmpIcons.SystemUpdate
        AssistantAction.CAN_UPDATE -> CmpIcons.Memory
        AssistantAction.FIRMWARE_ARCHIVE -> CmpIcons.Inventory2
        AssistantAction.AUTOSTART_STATUS -> CmpIcons.FactCheck
        AssistantAction.BYPASS_RUN -> CmpIcons.Key
        AssistantAction.CLONE_RUN -> CmpIcons.ContentCopy
        AssistantAction.SETTINGS_FIND -> CmpIcons.Search
        AssistantAction.SETTINGS_LOAD -> CmpIcons.Download
        AssistantAction.SETTINGS_ALL -> CmpIcons.Tune
        AssistantAction.CONFIGURATOR -> CmpIcons.FolderOpen
        AssistantAction.STATE -> CmpIcons.Speed
        AssistantAction.STATE_FULL -> CmpIcons.Sensors
        AssistantAction.MOUNT_PASSPORT -> CmpIcons.Description
        AssistantAction.SYSTEM_DEVICES -> CmpIcons.SettingsRemote
        AssistantAction.PAIRING -> CmpIcons.AddLink
        AssistantAction.PRODUCT_INFO -> CmpIcons.ManageSearch
        AssistantAction.VEHICLES -> CmpIcons.DirectionsCar
        AssistantAction.TICKET_NEW -> CmpIcons.Call
        AssistantAction.TICKETS -> CmpIcons.FactCheck
        AssistantAction.SUPPORT -> CmpIcons.SupportAgent
    }
