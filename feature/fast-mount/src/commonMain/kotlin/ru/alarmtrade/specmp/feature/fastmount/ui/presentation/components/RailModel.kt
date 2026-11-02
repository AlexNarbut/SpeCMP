package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountPlan
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountStep

/** Шаг в колонке шагов: подготовка (одна точка с подшагами) и шаги мастера. */
enum class RailKey { PREP, WRITE, MOUNT, BYPASS, CLONE, SLAVE, CHECK, REVIEW }

/** Подшаг подготовки. */
enum class PrepSub { UNIT, VEHICLE, CONFIGURATION, SUMMARY }

/**
 * Статус шага (макет `statusesFor`): пройден · текущий · впереди · по необходимости (Clone ещё не нужен) ·
 * пропущен (Clone не потребовался) · следующий после перезагрузки (пульсирует).
 */
enum class RailStatus { DONE, CURRENT, AHEAD, OPTIONAL, SKIPPED, NEXT }

/** Где мастер сейчас: шаг [current] (`null` — «Готово»), после шага [rebootFrom] идёт перезагрузка. */
@Immutable
data class RailPosition(val current: RailKey?, val prepSub: PrepSub = PrepSub.UNIT, val rebootFrom: RailKey? = null)

/** Строка колонки: [number] — номер среди считаемых шагов (`null` — Clone «по необходимости» / пропущен). */
@Immutable
data class RailRow(val key: RailKey, val status: RailStatus, val number: Int?)

/** Колонка целиком: строки и счётчик «N из M». */
@Immutable
data class RailModel(val rows: List<RailRow>, val currentNumber: Int, val total: Int, val done: Boolean)

/**
 * Путь шагов: до выбора конфигурации — полный (как в макете), после — по плану (обход/Slave — по конфигурации,
 * USB — без Slave и проверки системы).
 */
fun railPath(plan: FastMountPlan?, usb: Boolean): List<RailKey> {
    if (plan == null) {
        return buildList {
            add(RailKey.PREP)
            add(RailKey.WRITE)
            add(RailKey.MOUNT)
            add(RailKey.BYPASS)
            add(RailKey.CLONE)
            if (!usb) {
                add(RailKey.SLAVE)
                add(RailKey.CHECK)
            }
            add(RailKey.REVIEW)
        }
    }
    return listOf(RailKey.PREP) + plan.steps.mapNotNull { it.step.railKey() }
}

fun FastMountStep.railKey(): RailKey? = when (this) {
    FastMountStep.WRITING_SETTINGS -> RailKey.WRITE
    FastMountStep.MOUNT -> RailKey.MOUNT
    FastMountStep.KEY_BYPASS -> RailKey.BYPASS
    FastMountStep.CLONE -> RailKey.CLONE
    FastMountStep.SLAVE -> RailKey.SLAVE
    FastMountStep.SYSTEM_CHECK -> RailKey.CHECK
    FastMountStep.FEEDBACK -> RailKey.REVIEW
    FastMountStep.REBOOTING, FastMountStep.FINISH -> null
}

/** Clone не потребовался — пропущен; ещё не потребовался (и впереди) — «по необходимости»; иначе — как все. */
private fun cloneStatus(key: RailKey, cloneNeeded: Boolean?, ahead: Boolean): RailStatus? = when {
    key != RailKey.CLONE -> null
    cloneNeeded == false -> RailStatus.SKIPPED
    cloneNeeded != true && ahead -> RailStatus.OPTIONAL
    else -> null
}

/** Модель колонки по пути, позиции и итогу обхода ([cloneNeeded]: `null` — ещё неизвестно). */
fun railModel(path: List<RailKey>, position: RailPosition, cloneNeeded: Boolean?): RailModel {
    val counted = path.filter { it != RailKey.CLONE || cloneNeeded == true }
    val target = position.rebootFrom?.let { from -> counted.getOrNull(counted.indexOf(from) + 1) } ?: position.current
    val targetIndex = if (target == null) path.size else path.indexOf(target).coerceAtLeast(0)
    val rows = path.mapIndexed { index, key ->
        val status = cloneStatus(key, cloneNeeded, index >= targetIndex) ?: when {
            index < targetIndex -> RailStatus.DONE
            index == targetIndex -> if (position.rebootFrom != null) RailStatus.NEXT else RailStatus.CURRENT
            else -> RailStatus.AHEAD
        }
        val number = counted.indexOf(key).takeIf { it >= 0 }?.plus(1)
        RailRow(key, status, number)
    }
    val currentNumber = target?.let { counted.indexOf(it) + 1 } ?: counted.size
    return RailModel(rows = rows, currentNumber = currentNumber, total = counted.size, done = target == null)
}
