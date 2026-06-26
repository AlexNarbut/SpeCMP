# Практики написания кода в проекте, Паттерны — Response / LCEState / DI / State

## Ограничения
- Никогда не использовать `material3` / `foundation` напрямую в фичах — только через `Cmp*` из `core/ui`
- Предпочитать `commonMain`; `expect`/`actual` только для по-настоящему платформо-специфичного кода

## Response vs LCEState

| Слой | Тип | Состояния |
|------|-----|-----------|
| Репозитории, Use Cases | `Response<T>` | `Success<T>` / `Error` |
| ViewModels, Compose | `LCEState<T>` | `None` / `Loading` / `Content<T>` / `Error` |

Конвертация: `response.asLCEState { it.message ?: "Error" }`

---

## Response — слой бизнес-логики

```kotlin
// ✅ Правильно
suspend fun fetchUser(id: String): Response<User> {
    return try {
        Response.Success(api.getUser(id))
    } catch (e: Exception) {
        Response.Error(e, "Failed to fetch user")
    }
}

// ❌ Неправильно — бросает необработанное исключение
suspend fun fetchUser(id: String): User = api.getUser(id)
```

**Правила:**
- ВСЕ suspend-функции в репозиториях/use cases ДОЛЖНЫ возвращать `Response<T>`
- НИКОГДА не бросать исключения из репозиториев/use cases
- Оборачивать каждую асинхронную операцию в `Response.Success` / `Response.Error`

---

## LCEState — ViewModel + Compose

```kotlin
// ViewModel
class UserViewModel(private val getUser: GetUserUseCase) : ViewModel() {
    private val _state = MutableStateFlow<LCEState<User>>(LCEState.None())
    val state: StateFlow<LCEState<User>> = _state.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _state.value = LCEState.Loading()
            _state.value = getUser(id).asLCEState { it.message ?: "Error" }
        }
    }
}

// Compose UI
val state by viewModel.state.collectAsState()
when (state) {
    is LCEState.None    -> { /* начальное состояние */ }
    is LCEState.Loading -> CmpCircularProgressIndicator()
    is LCEState.Content -> UserCard((state as LCEState.Content).data)
    is LCEState.Error   -> ErrorMessage((state as LCEState.Error).message)
}
```

---

## Внедрение зависимостей — Koin 4.0.0

Инициализация в `composeApp` через `startKoin { modules(...) }`.

```kotlin
// DI-модуль в feature/*/di/
val myFeatureModule = module {
    singleOf(::MyRepositoryImpl) bind MyRepository::class
    factoryOf(::MyUseCase)
    viewModelOf(::MyViewModel)
}

// В Compose-экранах
val viewModel = koinViewModel<MyViewModel>()
val dep = koinInject<MyDependency>()
```

Чеклист при добавлении фичи:
1. Добавить зависимость модуля в `composeApp/build.gradle.kts`
2. Зарегистрировать Koin-модуль в `startKoin { modules(...) }` в `App()`
3. Добавить маршруты в навигацию в `AppNavigation.kt`

---

## Управление состоянием

```kotlin
// Локальное UI-состояние
var expanded by remember { mutableStateOf(false) }

// Переключение темы
var isDark by LocalThemeIsDark.current

// Дорогостоящее вычисление на основе состояния
val filtered by remember {
    derivedStateOf { items.filter { it.isActive } }
}
```

**Аннотации стабильности:**
- `@Immutable` — все свойства `val` (неизменяемые)
- `@Stable` — мутации наблюдаемы через Compose

Всегда помечать состояние ViewModel и доменные модели аннотациями `@Immutable` или `@Stable` для оптимизации skip в Compose.

> Полные практики работы с Compose — когда разбивать на composable-функции, как правильно читать часто меняемые значения, когда использовать `derivedStateOf`, `rememberUpdatedState`, `remember` с ключами, передавать `Flow` vs `State` — см. **`docs/compose-best-prac.md`**.

---

## Множественные LCEState в State экрана

Когда экрану нужно несколько независимых асинхронных данных, каждое оборачивается в отдельный `LCEState<T>` внутри общего State:

```kotlin
@Immutable
data class DashboardState(
    val profile: LCEState<ProfileContent> = LCEState.None(),
    val transactions: LCEState<List<Transaction>> = LCEState.None(),
    val settings: LCEState<UserSettings> = LCEState.None(),
)
```

### Императивный подход — `applyFromExecuting`

Подходит когда данные грузятся **по запросу** (one-shot), а не реактивно обновляются. Каждый `MutableStateFlow<LCEState<T>>` загружается отдельно.

```kotlin
class DashboardViewModel(
    private val profileRepo: ProfileRepository,
    private val transactionsRepo: TransactionsRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _profile = MutableStateFlow<LCEState<ProfileContent>>(LCEState.None())
    private val _transactions = MutableStateFlow<LCEState<List<Transaction>>>(LCEState.None())

    // Составной State собирается через combine
    val state: StateFlow<DashboardState> = combine(
        _profile,
        _transactions,
    ) { profile, transactions ->
        DashboardState(profile = profile, transactions = transactions)
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), DashboardState())

    fun loadData() {
        // Параллельная загрузка — каждый launch независим
        viewModelScope.launch {
            _profile.applyFromExecuting(
                execute = { profileRepo.getProfile() },
                errorMapper = { it.message ?: "Ошибка профиля" },
                dispatcher = dispatchers.default,
                map = { it.toContent() }
            )
        }
        viewModelScope.launch {
            _transactions.applyFromExecuting(
                execute = { transactionsRepo.getTransactions() },
                errorMapper = { it.message ?: "Ошибка транзакций" },
                dispatcher = dispatchers.default,
                map = { it }
            )
        }
    }
}
```

**Важно**: запускайте загрузки в **отдельных `launch`-блоках** для параллельности. Если запустить последовательно в одном `launch`, каждый `applyFromExecuting` заблокирует следующий.

### Последовательная загрузка (если есть зависимость)

Когда второй запрос зависит от результата первого:

```kotlin
fun loadData() {
    viewModelScope.launch {
        _profile.applyFromExecuting(
            execute = { profileRepo.getProfile() },
            errorMapper = { it.message ?: "Ошибка" },
            dispatcher = dispatchers.default,
            map = { it.toContent() }
        )
        // Загружаем транзакции только после успешной загрузки профиля
        _profile.value.content?.let { profile ->
            _transactions.applyFromExecuting(
                execute = { transactionsRepo.getByUserId(profile.userId) },
                errorMapper = { it.message ?: "Ошибка" },
                dispatcher = dispatchers.default,
                map = { it }
            )
        }
    }
}
```

---

## Реактивный подход — combine / zip + stateIn

Когда репозитории возвращают `Flow` (реактивные источники, подписки на БД, WebSocket и т.д.), вместо ручного управления `MutableStateFlow<LCEState<T>>` лучше **комбинировать Flow-ы** и преобразовывать в `StateFlow`.

### Когда использовать какой оператор

| Оператор | Когда использовать |
|----------|-------------------|
| `combine` | Источники эмитят **независимо**, нужно последнее значение от каждого |
| `zip` | Источники эмитят **попарно**, нужно дождаться значения от каждого |

### Паттерн: combine + stateIn

```kotlin
class DashboardViewModel(
    profileRepo: ProfileRepository,        // Flow<Response<Profile>>
    transactionsRepo: TransactionsRepository, // Flow<Response<List<Transaction>>>
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        profileRepo.observeProfile(),
        transactionsRepo.observeTransactions(),
    ) { profileResponse, transactionsResponse ->
        DashboardState(
            profile = profileResponse.asLCEState { it.message ?: "Ошибка" },
            transactions = transactionsResponse.asLCEState { it.message ?: "Ошибка" },
        )
    }
        .flowOn(dispatchers.default)  // маппинг НЕ на Main потоке
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
            initialValue = DashboardState()
        )
}
```

### Выбор SharingStarted-стратегии

| Стратегия | Поведение | Когда использовать |
|-----------|-----------|-------------------|
| `WhileSubscribed(stopTimeout)` | Активен пока есть подписчики + таймаут после отписки | **По умолчанию для экранов.** Экономит ресурсы при уходе с экрана, `stopTimeout = 5.seconds` — стандартное значение (переживает поворот экрана) |
| `Lazily` | Стартует при первом подписчике, **никогда не останавливается** | Данные нужны всегда пока жива ViewModel (фоновые задачи, кэш) |
| `Eagerly` | Стартует немедленно при создании | Критичные данные, нужные сразу (редко используется) |

```kotlin
// ✅ Экран — отписываемся при уходе
.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initial)

// ✅ Фоновый кэш — живёт пока жива ViewModel
.stateIn(viewModelScope, SharingStarted.Lazily, initial)
```

### Смешанный паттерн: one-shot + reactive

Когда часть данных грузится однократно, а часть — реактивно:

```kotlin
class DashboardViewModel(
    private val profileRepo: ProfileRepository,
    settingsRepo: SettingsRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    // Реактивный источник
    private val settingsFlow: Flow<LCEState<UserSettings>> = settingsRepo
        .observeSettings()
        .map { response -> response.asLCEState { it.message ?: "Ошибка" } }

    // One-shot источник
    private val _profile = MutableStateFlow<LCEState<ProfileContent>>(LCEState.None())

    val state: StateFlow<DashboardState> = combine(
        _profile,
        settingsFlow,
    ) { profile, settings ->
        DashboardState(profile = profile, settings = settings)
    }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), DashboardState())

    fun loadProfile() {
        viewModelScope.launch {
            _profile.applyFromExecuting(
                execute = { profileRepo.getProfile() },
                errorMapper = { it.message ?: "Ошибка" },
                dispatcher = dispatchers.default,
                map = { it.toContent() }
            )
        }
    }
}
```

---

## Правила работы с диспетчерами (AppDispatchers)

`AppDispatchers` инжектится через Koin и используется **вместо** прямых обращений к `Dispatchers.*`.

| Диспетчер | Назначение |
|-----------|-----------|
| `dispatchers.io` | Сеть, БД, файловый I/O — в репозиториях |
| `dispatchers.default` | Маппинг, комбинация, CPU-работа — в `flowOn()`, `withContext()` |
| `dispatchers.main` | UI-обновления (обычно не нужен явно — `viewModelScope` уже на Main) |

### Когда использовать `flowOn` / `stateIn`

`flowOn` и `stateIn` нужны **только для холодных Flow** — результатов `combine`, `zip`, `map`, `flow {}` и т.д.

`MutableStateFlow` / `StateFlow` — **горячие** потоки. Для них `flowOn` и `stateIn` бессмысленны.

```kotlin
// ✅ Холодный Flow (combine) — нужен flowOn + stateIn
val state: StateFlow<DashboardState> = combine(
    _profile, _transactions
) { profile, transactions ->
    DashboardState(profile, transactions)
}
    .flowOn(dispatchers.default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), DashboardState())

// ✅ MutableStateFlow — уже горячий, просто asStateFlow()
private val _state = MutableStateFlow(MyState())
val state: StateFlow<MyState> = _state.asStateFlow()

// ❌ Неправильно — flowOn/stateIn на горячем StateFlow
val state = _state
    .flowOn(dispatchers.default)   // бессмысленно
    .stateIn(viewModelScope, ...)  // лишнее
```

### Хардкод диспетчеров

```kotlin
// ✅ Правильно — через инжектированный AppDispatchers
class MyViewModel(private val dispatchers: AppDispatchers) : ViewModel() {
    val state = coldFlow
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initial)
}

// ❌ Неправильно — хардкод (нельзя подменить в тестах)
val state = coldFlow
    .flowOn(Dispatchers.Default)
    .stateIn(...)
```

**Тестирование**: в тестах подменяем на `UnconfinedTestDispatcher()`:

```kotlin
val testDispatchers = AppDispatchers(
    io = UnconfinedTestDispatcher(),
    default = UnconfinedTestDispatcher(),
    main = UnconfinedTestDispatcher()
)
```

---

## Логирование

Использовать **Kermit** на всех платформах:

```kotlin
import co.touchlab.kermit.Logger

Logger.d { "Debug message" }
Logger.e(e) { "Error occurred" }
```
