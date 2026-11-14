package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection

sealed interface AdvancedMountRootEffect {
    data class OpenSection(val section: MountSection) : AdvancedMountRootEffect

    data object OpenSupportChat : AdvancedMountRootEffect

    data object OpenRemoteSync : AdvancedMountRootEffect

    data class OpenProductInfo(val systemId: Long) : AdvancedMountRootEffect
}
