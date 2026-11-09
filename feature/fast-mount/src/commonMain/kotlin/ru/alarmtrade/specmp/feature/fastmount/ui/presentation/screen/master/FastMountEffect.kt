package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

sealed interface FastMountEffect {
    data object Exit : FastMountEffect

    data object ChooseAnother : FastMountEffect

    data object AdvancedMount : FastMountEffect

    class OpenFile(val name: String, val bytes: ByteArray) : FastMountEffect
}
