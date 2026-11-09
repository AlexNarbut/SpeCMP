package ru.alarmtrade.specmp.feature.fastmount.domain.model

sealed interface FastMountFailure {
    data object SessionLost : FastMountFailure

    data object BypassNotFinished : FastMountFailure

    data object CloneNotFinished : FastMountFailure

    data object ChecksNotDone : FastMountFailure

    data object FeedbackRequired : FastMountFailure

    data object TransportNotSupported : FastMountFailure
}

class FastMountException(val failure: FastMountFailure, cause: Throwable? = null) :
    Exception(failure.toString(), cause)

enum class MountDeviceClass {
    ALARM,

    MICROBOT,
    OTHER,
}
