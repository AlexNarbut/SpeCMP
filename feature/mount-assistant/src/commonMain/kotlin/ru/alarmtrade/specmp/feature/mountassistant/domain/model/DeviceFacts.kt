package ru.alarmtrade.specmp.feature.mountassistant.domain.model

/** [capabilities] `null` — модели нет в базе; [can] `null` — CAN у устройства нет. */
data class DeviceFacts(
    val systemId: Long,
    val title: String,
    val firmwareVersion: String,
    val transport: AssistantTransport,
    val capabilities: Set<AssistantCapability>?,
    val can: CanFacts?,
    val online: Boolean,
) {
    val inDatabase: Boolean get() = capabilities != null

    val factoryCan: Boolean get() = can?.factory == true

    fun has(capability: AssistantCapability): Boolean = capabilities?.contains(capability) == true
}

enum class AssistantTransport { BLE, USB, SERIAL }

enum class AssistantCapability { FIRMWARE, SETTINGS, BYPASS, CLONE }

data class CanFacts(val factory: Boolean, val code: String?, val version: String?)
