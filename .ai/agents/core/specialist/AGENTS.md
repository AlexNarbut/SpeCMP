# core/specialist

- Граница приложения с внешним Specialist SDK (`ru.alarmtrade.common:specialist`): `SpecialistModule` создаёт `SpecialistApp` (конфиг с диспетчерами) и регистрирует SDK-интеракторы как Koin-синглтоны (`authInteractor`, `userInteractor`, `chatInteractor`, `filesInteractor` и др.).
- Фичи НЕ обращаются к SDK-интеракторам напрямую — только через профильные API этого модуля.
- `AccountRepository` (`account/`) — общий источник identity аккаунта (`currentProfile(): Response<AccountProfile>`, маппинг SDK-типов внутри модуля). Две и более фич с потребностью в профиле пользователя подключают его, а не дублируют вызов `userInteractor.getUserUseCase`.
