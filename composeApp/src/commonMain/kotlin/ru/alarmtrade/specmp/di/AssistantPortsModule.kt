package ru.alarmtrade.specmp.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.assistant.AppAssistantAutostartRepository
import ru.alarmtrade.specmp.assistant.AppAssistantFirmwareRepository
import ru.alarmtrade.specmp.assistant.AppAssistantStateRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantAutostartRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantFirmwareRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantStateRepository

/** Ассистент не зависит от обособленных фич прошивки, автозапуска и состояния: их связывает корень приложения. */
val assistantPortsModule = module {
    singleOf(::AppAssistantFirmwareRepository) bind AssistantFirmwareRepository::class
    singleOf(::AppAssistantAutostartRepository) bind AssistantAutostartRepository::class
    singleOf(::AppAssistantStateRepository) bind AssistantStateRepository::class
}
