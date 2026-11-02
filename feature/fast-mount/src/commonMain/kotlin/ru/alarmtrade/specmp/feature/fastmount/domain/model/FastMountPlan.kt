package ru.alarmtrade.specmp.feature.fastmount.domain.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

/**
 * Ожидаемый путь мастера — для сводки перед стартом и шапки с шагами. Считается до старта по конфигурации и
 * транспорту — так же, как маршрутизирует SDK (`BluetoothFastMountRouter` / `UsbFastMountRouter`).
 * [FastMountStep.REBOOTING] — состояние перехода, в план не входит.
 */
data class FastMountPlan(val steps: List<PlannedStep>) {
    val stepCount: Int get() = steps.size

    /** Позиция шага в плане (0-based); `null` — шага в плане нет (например, переход). */
    fun indexOf(step: FastMountStep): Int? = steps.indexOfFirst { it.step == step }.takeIf { it >= 0 }
}

/** Шаг плана. [conditional] — появится, только если потребуется (Clone — по итогу обхода). */
data class PlannedStep(val step: FastMountStep, val conditional: Boolean = false)

/** План по конфигурации и транспорту; транспорт без поддержки быстрого монтажа — `null`. */
fun fastMountPlan(configuration: FastMountConfiguration, transport: ConnectionTransport): FastMountPlan? {
    val bluetooth = when (transport) {
        ConnectionTransport.BLE -> true
        ConnectionTransport.USB -> false
        ConnectionTransport.SERIAL -> return null
    }
    val steps = buildList {
        add(PlannedStep(FastMountStep.WRITING_SETTINGS))
        add(PlannedStep(FastMountStep.MOUNT))
        if (configuration.keyBypass) {
            add(PlannedStep(FastMountStep.KEY_BYPASS))
            add(PlannedStep(FastMountStep.CLONE, conditional = true))
        }
        // По USB SDK не поддерживает Slave и проверку системы (телеметрии по USB нет).
        if (bluetooth) {
            if (configuration.slave) add(PlannedStep(FastMountStep.SLAVE))
            add(PlannedStep(FastMountStep.SYSTEM_CHECK))
        }
        add(PlannedStep(FastMountStep.FEEDBACK))
    }
    return FastMountPlan(steps)
}
