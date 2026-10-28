package ru.alarmtrade.specmp.feature.account.domain.model

/**
 * Профиль текущего аккаунта — общий источник identity для фич.
 *
 * Данные приходят из Specialist SDK (`userInteractor.getUserUseCase`); маппинг
 * SDK-типов выполняется в data-слое `feature/account`, чтобы фичи не зависели от них.
 */
data class AccountProfile(val name: String, val email: String)
