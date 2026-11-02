package ru.alarmtrade.specmp.feature.device.autostart.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.autostart.data.repository.SpecialistBypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.data.repository.SpecialistCloneRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass.BypassLearningViewModel
import ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone.CloneViewModel

/**
 * Koin-модуль фичи `feature/device/autostart`: бесключевой обход и процедура Clone (два отдельных экрана
 * расширенного монтажа). SDK-интеракторы — из `core/specialist`; сессия, кулдаун PIN и вход в режим
 * программирования (`ProgramModeEntry`) — из `deviceInteractionModule`.
 */
val deviceAutostartModule = module {
    // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание репозитория.
    single<BypassLearningRepository> { SpecialistBypassLearningRepository(bluetoothAlarmDeviceInteractor = { get() }) }
    single<CloneRepository> { SpecialistCloneRepository(bluetoothAlarmDeviceInteractor = { get() }) }

    viewModelOf(::BypassLearningViewModel)
    viewModel { (fromBypass: Boolean) -> CloneViewModel(get(), get(), get(), get(), fromBypass) }
}
