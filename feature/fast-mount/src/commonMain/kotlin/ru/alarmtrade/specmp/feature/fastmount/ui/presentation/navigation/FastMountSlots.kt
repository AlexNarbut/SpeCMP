package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.fastmount.domain.model.FastMountResult
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountCar
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountChecklist

/** Подставляет composition root: у `feature/fast-mount` нет зависимостей на фичи, чьи экраны идут шагами. */
@Immutable
class FastMountSlots(
    val firmwareUpdate: @Composable (onBack: () -> Unit) -> Unit,
    val vehiclePicker: @Composable (
        request: MountVehicleRequest,
        onPick: (MountCar) -> Unit,
        onBack: () -> Unit,
    ) -> Unit,
    val configurationPicker: @Composable (
        request: MountConfigurationRequest,
        onPick: (configurationId: Int) -> Unit,
        onOpenFirmwareUpdate: () -> Unit,
        onBack: () -> Unit,
    ) -> Unit,
    val bypass: @Composable (step: MountStep, onNext: (cloneRequired: Boolean) -> Unit) -> Unit,
    val clone: @Composable (step: MountStep, onNext: () -> Unit) -> Unit,
    val systemCheck: @Composable (step: MountStep, checklist: MountChecklist, actions: MountChecklistActions) -> Unit,
    val fileOpener: MountFileOpener,
    val passport: MountPassportSlot,
)

/**
 * Паспорт монтажа с финала: [reading] — считывание блока с итогом мастера ([onDone] — паспорт создан), [editor] —
 * предотчёт ([onBack] — назад на финал), [export] — экспорт оформленного паспорта: [content] — финал с кнопками.
 */
@Immutable
class MountPassportSlot(
    val reading: @Composable (
        result: FastMountResult,
        onDone: (passportId: String) -> Unit,
        onCancel: () -> Unit,
    ) -> Unit,
    val editor: @Composable (passportId: String, onBack: () -> Unit) -> Unit,
    val export: @Composable (passportId: String, content: @Composable (MountPassportExport) -> Unit) -> Unit,
)

/** «Экспорт PDF» ([onPdf], [pdfBusy] — PDF собирается) и «Экспорт ZIP» ([onZip]) оформленного паспорта. */
@Immutable
class MountPassportExport(val pdfBusy: Boolean, val onPdf: () -> Unit, val onZip: () -> Unit)

@Immutable
data class MountVehicleRequest(val deviceName: String, val canCode: String?)

@Immutable
data class MountConfigurationRequest(val deviceName: String, val canCode: String?, val car: MountCar)

@Immutable
class MountChecklistActions(
    val onAnswer: (taskId: Int, option: Int) -> Unit,
    val onSkip: (taskId: Int) -> Unit,
    val onNext: () -> Unit,
    val onBack: () -> Unit,
)

/** Открыть файл системным приложением: `true` — открыт, `false` — нет приложения, `null` — ошибка. */
fun interface MountFileOpener {
    suspend fun open(name: String, bytes: ByteArray): Boolean?
}

/** @property key ключ VM экрана шага: меняется, когда шаг надо пройти заново (вернулись к монтажу). */
@Immutable
class MountStep(val key: String, val frame: MountStepFrame, val next: MountStepNext, val onBack: () -> Unit)

@Immutable
class MountStepFrame(
    val topBar: @Composable (locked: Boolean, onBack: () -> Unit) -> Unit,
    val dock: @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit,
)

@Immutable
class MountStepNext(val busy: Boolean, val error: String?)
