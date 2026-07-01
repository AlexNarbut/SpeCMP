# Core Modules

Базовые модули приложения, организованные по принципу разделения ответственности.

Все core-модули поддерживают Android, iOS и JVM/Desktop.

## Структура

### core/common
**Чистый Kotlin модуль** без зависимостей от Android или UI фреймворков.

**Содержит:**
- `Response<T>` - обертка для результатов операций (Success/Error)
- `ResponseExt.kt` - расширения для работы с Response
- `AppDispatchers` - централизованное управление корутин диспетчерами

**Зависимости:**
- Kotlinx Coroutines
- Kermit (логирование)

**Использование:**
```kotlin
suspend fun fetchUser(id: String): Response<User> {
    return try {
        val user = api.getUser(id)
        Response.Success(user)
    } catch (e: Exception) {
        Response.Error(e, "Failed to fetch user")
    }
}
```

### core/network
**Сетевой слой** с HTTP клиентом и конфигурацией.

**Содержит:**
- `HttpClientFactory` - фабрика для создания Ktor HttpClient
- Конфигурация сериализации, логирования, и других плагинов

**Зависимости:**
- `:core:common`
- Ktor Client (OkHttp для Android/JVM, Darwin для iOS)
- Kotlinx Serialization

**Использование:**
```kotlin
class UserRemoteDataSource(private val httpClient: HttpClient) {
    suspend fun getUser(id: String): UserDto {
        return httpClient.get("https://api.example.com/users/$id").body()
    }
}
```

### core/localization
**Общая локализация приложения** без зависимости от UI-фич.

**Содержит:**
- `AppLocale` - поддерживаемые языки приложения
- `currentAppLocale()` - получение текущей локали приложения
- `applyAppLocale()` - применение выбранной локали на платформе

**Использование:**
```kotlin
val locale = currentAppLocale()
applyAppLocale(AppLocale.Russian)
```

### core/specialist
**Интеграционный слой Specialist SDK** и единая точка его DI-инициализации.

**Содержит:**
- `specialistModule` - Koin-модуль, который создаёт `SpecialistApp`
- Регистрацию конкретных SDK interactor-ов для feature-модулей
- `SpecialistDeviceInformationProvider` - узкий provider device information для SDK
- Platform-specific реализацию `AppDeviceInformation` для Android, iOS и JVM/Desktop

**Правило:**
Feature-модули не создают `SpecialistApp` и не принимают его целиком в конструкторах. Вместо
этого они получают конкретные SDK interactor-ы, use case-обёртки или узкие provider-ы, которые им нужны.
Если SDK доступен в common-коде, feature-репозиторий и DI должны оставаться в `commonMain`;
в platform source set выносится только действительно platform-specific код.

### core/ui
**UI компоненты и утилиты** для Compose Multiplatform.

**Содержит:**
- `LCEState<T>` - состояния UI (None/Loading/Content/Error)
- `LCEStateExt.kt` - расширения для работы с LCEState
- `theme/` - цветовая палитра и Material3 тема

**Зависимости:**
- `:core:common`
- Compose Multiplatform
- Material3
- Lifecycle ViewModel
- Material Kolor

**Использование:**
```kotlin
class UserViewModel : ViewModel() {
    private val _userState = MutableStateFlow<LCEState<User>>(LCEState.None())
    val userState: StateFlow<LCEState<User>> = _userState.asStateFlow()

    fun loadUser(id: String) {
        viewModelScope.launch {
            _userState.value = LCEState.Loading()
            val response = getUserUseCase(id)
            _userState.value = response.asLCEState { it.message ?: "Error" }
        }
    }
}
```

## Граф зависимостей

```
┌─────────────┐
│core/common  │ ◄─── Базовый модуль (чистый Kotlin)
└─────────────┘
       ▲
       │
       ├─────────────┬──────────────┬──────────────┐
       │             │              │              │
┌─────────────┐ ┌─────────────┐ ┌────────────┐ ┌───────────────────┐
│core/network │ │  core/ui    │ │core/local- │ │  core/specialist  │
│             │ │             │ │ ization    │ │                   │
└─────────────┘ └─────────────┘ └────────────┘ └───────────────────┘
       ▲             ▲              ▲              ▲
       │             │              │              │
       └─────────────┴──────────────┴──────────────┘
                │
         ┌─────────────┐
         │  feature/*  │ ◄─── Feature модули
         └─────────────┘
                ▲
                │
         ┌─────────────┐
         │ composeApp  │ ◄─── Главное приложение
         └─────────────┘
```

## Принципы

1. **Разделение ответственности**: каждый модуль имеет четкую зону ответственности
2. **Минимальные зависимости**: core/common не зависит ни от чего, кроме базовых библиотек
3. **Переиспользование**: core модули используются всеми feature модулями
4. **Тестируемость**: чистые модули легко тестировать

## Добавление нового core модуля

1. Создать директорию `core/module_name/`
2. Добавить `build.gradle.kts`
3. Добавить в `settings.gradle.kts`: `include(":core:module_name")`
4. Определить зависимости от других core модулей
