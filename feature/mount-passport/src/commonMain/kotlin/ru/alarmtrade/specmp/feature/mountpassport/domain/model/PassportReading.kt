package ru.alarmtrade.specmp.feature.mountpassport.domain.model

/** Откуда считывание: расширенный монтаж, итог мастера быстрого монтажа или дооформление паспорта [passportId]. */
sealed interface PassportReadSource {
    data object AdvancedMount : PassportReadSource

    data class FastMount(val result: PassportFastMountResult) : PassportReadSource

    data class Complete(val passportId: String) : PassportReadSource
}

/** Итог мастера: [unit] — блок, как его знает мастер (оформить без считывания, если блок не на связи). */
data class PassportFastMountResult(
    val vehicle: PassportVehicle = PassportVehicle(),
    val configuration: PassportConfiguration? = null,
    val works: PassportWorks = PassportWorks(fromMaster = true),
    val systemCheck: List<PassportCheckTask> = emptyList(),
    val durationMinutes: Int? = null,
    val unit: PassportUnit? = null,
)

enum class PassportReadStep { BLOCK, PARAMS, DEVICES, SETTINGS, READY }

enum class PassportReadCommand { RETRY, CONTINUE_WITHOUT, SKIP }

/** `NOT_SUPPORTED` — блок этого не умеет, `FACTORY_CAN` — заводская CAN-прошивка. */
enum class PassportUnavailableReason { NOT_SUPPORTED, FACTORY_CAN }

sealed interface PassportStepSummary {
    data class Block(val systemName: String, val systemId: String, val firmwareVersion: String?) : PassportStepSummary

    data class Params(val imei: String?, val bluetoothFirmware: String?, val bluetoothBootloader: String?) :
        PassportStepSummary

    data class Devices(val count: Int, val transport: PassportTransport) : PassportStepSummary

    data class Settings(val sizeBytes: Long) : PassportStepSummary

    data class Ready(val passportId: String, val number: String) : PassportStepSummary
}

sealed interface PassportStepStatus {
    data object Waiting : PassportStepStatus

    data object Reading : PassportStepStatus

    data class Done(val summary: PassportStepSummary) : PassportStepStatus

    data class NotRead(val connectionLost: Boolean) : PassportStepStatus

    data class Unavailable(val reason: PassportUnavailableReason) : PassportStepStatus

    data object Skipped : PassportStepStatus
}

data class PassportStepState(val step: PassportReadStep, val status: PassportStepStatus)

/** Фаза считывания: остановки ждут команды ([PassportReadCommand]), [Done] — паспорт сохранён. */
sealed interface PassportReadPhase {
    data object Reading : PassportReadPhase

    data class Offline(val canContinueWithout: Boolean) : PassportReadPhase

    data class UnitFailed(val connectionLost: Boolean) : PassportReadPhase

    data class ConnectionLost(val step: PassportReadStep) : PassportReadPhase

    data class WrongUnit(val expectedSystemId: String, val actualSystemId: String) : PassportReadPhase

    data object SaveFailed : PassportReadPhase

    data class Done(val passportId: String) : PassportReadPhase
}

/** [unitName] — модель читаемого блока для подзаголовка экрана. */
data class PassportReadState(
    val steps: List<PassportStepState>,
    val phase: PassportReadPhase = PassportReadPhase.Reading,
    val transport: PassportTransport? = null,
    val unitName: String? = null,
) {
    fun status(step: PassportReadStep): PassportStepStatus =
        steps.firstOrNull { it.step == step }?.status ?: PassportStepStatus.Waiting

    companion object {
        val Initial = PassportReadState(
            steps = PassportReadStep.entries.map { PassportStepState(it, PassportStepStatus.Waiting) },
        )
    }
}

/** Блок по ID с сервера для паспорта без блока. */
sealed interface PassportUnitLookup {
    data class Found(val systemName: String, val systemId: String) : PassportUnitLookup

    data object NotFound : PassportUnitLookup

    data object Offline : PassportUnitLookup
}

/** «Перечитать»: подключён другой блок. */
class PassportUnitMismatchException(
    val expectedSystemId: String,
    val actualSystemId: String,
    cause: Throwable? = null,
) : Exception("Connected unit $actualSystemId is not the passport unit $expectedSystemId", cause)

/** «Перечитать»: этап не прочитан; [connectionLost] — связь с блоком прервалась. */
class PassportStageNotReadException(val connectionLost: Boolean, cause: Throwable? = null) :
    Exception("Mount passport stage was not read", cause)
