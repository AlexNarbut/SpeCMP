package ru.alarmtrade.specmp.feature.vehicle.domain.model

/** Код сервера, которого SDK не знает, — [UNKNOWN] (функция всё равно показывается). */
enum class CarBus(val family: BusFamily) {
    CAN1(BusFamily.CAN),
    CAN2(BusFamily.CAN),
    CAN3(BusFamily.CAN),
    CAN4(BusFamily.CAN),
    CAN1_FD(BusFamily.CAN_FD),
    CAN2_FD(BusFamily.CAN_FD),
    CAN3_FD(BusFamily.CAN_FD),
    CAN4_FD(BusFamily.CAN_FD),
    CAN_FD1_OR_CAN_FD3(BusFamily.CAN_FD),
    CAN_FD2_OR_CAN_FD4(BusFamily.CAN_FD),
    LIN(BusFamily.LIN),
    LIN_IN_OUT(BusFamily.LIN),
    BEAN(BusFamily.BEAN),
    IMO_KEY1(BusFamily.IMO),
    IMO_KEY2(BusFamily.IMO),
    IMO_KEY1_PLUS_IMO_KEY2(BusFamily.IMO),
    UNKNOWN(BusFamily.UNKNOWN),
}

enum class BusFamily { CAN, CAN_FD, LIN, BEAN, IMO, UNKNOWN }
