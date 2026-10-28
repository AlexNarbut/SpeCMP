# core/network

- Область: сетевая инфраструктура процесса в `core/network`: HTTP-движки Ktor и доступность сети.
- Перед изменениями читай `docs/architecture.md`; при правке зависимостей, client plugins и платформенной конфигурации — `docs/build.md`.
- DTO, mapper-ы и feature-репозитории ищи в `feature/*/data`, а не в `core/network`.
- HTTP-движки Ktor (OkHttp на Android/JVM, Darwin на iOS) — единственный источник движка в сборке: Specialist SDK и Coil (`coil-network-ktor3`) используют ktor-client, но движок не тянут. Не удалять, пока они на ktor. Своего `HttpClient` приложение не создаёт — сеть идёт через SDK.
- `monitor/NetworkMonitor` — доступность сети процесса (`isAvailable: StateFlow<Boolean>`). Реализации push-based: `AndroidNetworkMonitor(context)` (`ConnectivityManager` default callback, включая `onBlockedStatusChanged` — блокировку сети в фоне Android 15+; нужен `ACCESS_NETWORK_STATE`), `IosNetworkMonitor` (`NWPathMonitor`); на JVM реализации нет. Экземпляр — синглтон процесса, в Koin его кладёт корень приложения (`composeApp/di/rememberPlatformModule()`), а не этот модуль: Android-реализации нужен `applicationContext`.
