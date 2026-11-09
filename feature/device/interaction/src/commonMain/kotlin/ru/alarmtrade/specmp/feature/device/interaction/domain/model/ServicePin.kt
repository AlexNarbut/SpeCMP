package ru.alarmtrade.specmp.feature.device.interaction.domain.model

import kotlin.jvm.JvmInline

/** Значение не попадает в логи: [toString] его скрывает. */
@JvmInline
value class ServicePin private constructor(val value: String) {

    override fun toString(): String = "ServicePin(****)"

    companion object {
        const val LENGTH = 4

        fun parseOrNull(raw: String): ServicePin? =
            if (raw.length == LENGTH && raw.all { it in '0'..'9' }) ServicePin(raw) else null
    }
}
