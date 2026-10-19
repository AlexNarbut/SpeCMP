package ru.alarmtrade.specmp.core.specialist.account

/**
 * Профиль текущего аккаунта — общий источник identity для фич.
 *
 * Данные приходят из Specialist SDK (`userInteractor.getUserUseCase`); маппинг
 * SDK-типов выполняется в `core:specialist`, чтобы фичи не зависели от них.
 */
data class AccountProfile(val name: String, val email: String)
