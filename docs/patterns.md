# Практики написания кода в проекте, Паттерны — Response / LCEState / DI / State

## Ограничения
- Никогда не использовать `material3` / `foundation` напрямую в фичах — только через `Cmp*` из `core/ui`
- Предпочитать `commonMain`; `expect`/`actual` только для по-настоящему платформо-специфичного кода
- **Никаких `launch`/`coroutineScope` в Composable-функциях** — все suspend-операции (сохранение настроек, сетевые вызовы, БД) выполнять только через ViewModel (`viewModelScope.launch`) или Repository. Composable-функции вызывают только синхронные методы ViewModel.
- **Импорты — только сверху файла.** Полностью квалифицированные имена (`ru.alarmtrade.specmp.feature.*.SomeType`) в теле кода — запрещены. Сигнатуры, `when`-ветки, параметры, возвращаемые типы, аннотации — всё через `import` блок. Единственное исключение: конфликт имён (два разных `Foo` из разных пакетов в одном файле) — тогда используется `import ... as` alias, а не inline FQN.
- **Unused imports удаляем** по завершении работы с файлом. Неиспользуемый import — это шум и источник ложной связи между модулями. Проверять глазами после каждой крупной правки (или `./gradlew :moduleName:compileCommonMainKotlinMetadata` — компилятор выдаст warning).
- **`init`-блок не использовать для инициализации и загрузки данных** — особенно во ViewModel и Repository. Работа, запущенная из `init`, стартует неявно при создании объекта, не переживает пересоздание подписчиков и усложняет тестирование. Вместо этого: реактивные данные — через `stateIn(SharingStarted.WhileSubscribed)` (загрузка стартует при подписке), one-shot загрузка — через явный метод (`loadProfile()`), вызываемый из UI. Эмит эффектов из `init` запрещён отдельно (см. «One-shot события»).
- **`MutableStateFlow` обновляем только через `update { }`** — это атомарный CAS, потокобезопасный при конкурентных обновлениях из нескольких корутин. Прямое присваивание `_state.value = ...` при read-modify-write (`_state.value = _state.value.copy(...)`) теряет обновления при гонке. Чтение `_state.value` допустимо.
- **Никаких строковых литералов в `@Composable`-функциях.** Текст в UI (включая `contentDescription`) — только через `stringResource(Res.string.*)` (см. `docs/localization.md`). Строки, нужные для логики (ключи, теги, идентификаторы) — через `const val` / companion object, не inline. Исключение — нелокализуемые математические/служебные глифы (цифры `0`–`9`, операторы `+ − × ÷ =`, `.`, `⌫`): остаются литералами, т.к. не являются переводимым текстом.

```kotlin
// ❌ Плохо — статический UI-текст в composable
CmpIconButton(onClick = onBack, icon = CmpIcons.ArrowBack, contentDescription = "Назад")
CmpText(text = "Вход")

// ✅ Хорошо — через перевод
CmpIconButton(
    onClick = onBack,
    icon = CmpIcons.ArrowBack,
    contentDescription = stringResource(Res.string.login_common_a11y_back),
)
CmpText(text = stringResource(Res.string.login_login_title_screen))

// ✅ Строки для логики — через константы, а не inline
private const val ANALYTICS_SCREEN = "login"
```

- **Ошибки из ViewModel — только типизированные** (enum / sealed class в state), не строковые коды. `LCEState.Error(message)` со строкой-«кодом» (`"required_fields"`), которую UI сравнивает с собственной константой, — запрещён: контракт держится на двух независимых литералах в разных файлах и при рассинхронизации ломается молча. ViewModel кладёт в state типизированную ошибку, UI маппит её в `stringResource` (см. `docs/localization.md`, правило 4; структура иерархии — раздел «Ошибки в feature-модулях» ниже).

```kotlin
// ❌ Плохо — строковый код как контракт VM ↔ UI
// ViewModel:
_state.update { it.copy(registrationState = LCEState.Error("required_fields")) }
// UI (другой файл, своя копия константы):
if (state.error == REQUIRED_FIELDS_ERROR) { /* ... */ }

// ✅ Хорошо — типизированная ошибка в state
enum class RegistrationError { RequiredFields, RequestFailed }
_state.update { it.copy(error = RegistrationError.RequiredFields) }
// UI:
val text = when (error) {
    RegistrationError.RequiredFields -> stringResource(Res.string.login_common_error_required_fields)
    RegistrationError.RequestFailed -> stringResource(Res.string.login_common_error_registration_failed)
}
```

```kotlin
// ❌ Плохо — inline FQN
fun submit(credentials: ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials) { }

// ✅ Хорошо — через import сверху файла
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials

fun submit(credentials: LoginCredentials) { }

// ✅ Хорошо — alias при конфликте имён
import androidx.compose.ui.graphics.Color as ComposeColor
import ru.alarmtrade.specmp.core.ui.theme.Color as ThemeColor
```

## Ошибки в feature-модулях — sealed interface

Ошибки фичи организуются как иерархия `sealed interface` внутри модуля, на двух уровнях:

- **Domain-ошибки** — `feature/<name>/domain/model`. Базовый `sealed interface` на предметную
  область; наследники-`sealed interface` на каждый flow/операцию; конкретные случаи —
  `data object` (или `data class`, когда ошибке нужен payload: имя поля, лимит и т.п.).
- **Presentation-ошибки** — `feature/<name>/presentation/model`. Компактный тип под то, что
  реально различают экраны: `sealed interface` с наследниками, либо `enum`, если ни одному
  случаю не нужен payload. ViewModel маппит domain → presentation, UI маппит
  presentation → `stringResource`.

```kotlin
// domain/model — полная детализация для бизнес-правил
sealed interface AuthValidationError

sealed interface SignUpValidationError : AuthValidationError {
    data object EmptyFullName : SignUpValidationError
    data object EmptyEmail : SignUpValidationError
    data object EmptyPassword : SignUpValidationError
}

sealed interface ResetPasswordValidationError : AuthValidationError {
    data object EmptyEmail : ResetPasswordValidationError
}

// presentation/model — только то, что различает UI
enum class AuthFormError {
    RequiredFields,
    RequestFailed,
}

// пример sealed-варианта с payload на UI-уровне
sealed interface ProfileFormError {
    data object RequestFailed : ProfileFormError
    data class FieldTooLong(val maxLength: Int) : ProfileFormError
}
```

**Правила:**

- `when` по sealed-иерархии — исчерпывающий, **без ветки `else`**: новый наследник должен
  ломать компиляцию во всех точках обработки, а не молча проваливаться в default.
- Данные об ошибке (поле, лимит, оставшиеся попытки) — свойства `data class`-наследника,
  а не парсинг `message`-строки.
- Уровень детализации presentation-типа диктует UI: если экран показывает одно сообщение
  на группу domain-случаев — группа маппится в одно значение (`SignUpValidationError.*` →
  `AuthFormError.RequiredFields`), полная domain-иерархия в state экрана не протаскивается.
- Ошибки фичи живут в самой фиче. В `core/*` иерархия выносится только когда одни и те же
  ошибки реально нужны нескольким модулям, не «на будущее».
- Исключения из domain/data наружу не выходят: репозиторий конвертирует их в
  `Response.Error` (см. «Response — слой бизнес-логики»), а типизированная ошибка
  появляется на границе ViewModel при обработке `Response`.

## Response vs LCEState

| Слой | Тип | Состояния |
|------|-----|-----------|
| Репозитории | `Response<T>` | `Success<T>` / `Error` |
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
- ВСЕ suspend-функции в репозиториях ДОЛЖНЫ возвращать `Response<T>`
- НИКОГДА не бросать исключения из репозиториев
- Оборачивать каждую асинхронную операцию в `Response.Success` / `Response.Error`

---

## LCEState — ViewModel + Compose

```kotlin
// ViewModel
class UserViewModel(
    private val repository: UserRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow<LCEState<User>>(LCEState.None())
    val state: StateFlow<LCEState<User>> = _state.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _state.update { LCEState.Loading() }
            val response = withContext(dispatchers.io) {
                repository.fetchUser(id)
            }
            _state.update { response.asLCEState { it.message ?: "Error" } }
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

## Внедрение зависимостей — Koin 4.2.2

Инициализация в `composeApp` через `KoinApplication { modules(...) }` в `App()` (не `startKoin`):

```kotlin
// composeApp/.../App.kt
KoinApplication(application = {
    modules(commonModule, specialistModule, loginModule)
}) {
    AppTheme(onThemeChanged) { AppNavigation() }
}

// DI-модуль в feature/*/di/
val myFeatureModule = module {
    singleOf(::MyRepositoryImpl) bind MyRepository::class
    viewModelOf(::MyViewModel)
}

// В Compose-экранах
val viewModel = koinViewModel<MyViewModel>()
val dep = koinInject<MyDependency>()
```

**Границы DI и app-level инфраструктуры:**
- `composeApp` подключает все Koin-модули приложения и является composition root.
- `core/*` модули владеют переиспользуемой инфраструктурой и app-level integration DI: локаль, SDK initialization, общие settings, network/client factories, UI theme primitives.
- `feature/*/di` регистрирует только зависимости конкретной фичи: repositories, mappers, ViewModel.
- Feature-модули не создают singleton внешнего SDK/application container (`SpecialistApp` и аналоги). Такой singleton создаётся в профильном `core/*` модуле или в `composeApp`.
- В конструкторы feature-классов передаются узкие зависимости (`AuthInteractor`, repository, data source, provider), а не цельный SDK/application container. Так по сигнатуре видно, что реально использует класс, и фича не становится владельцем всего SDK.
- Не добавляй use case, если он только прокидывает один вызов в repository. В таком случае ViewModel может зависеть от repository напрямую; отдельный use case нужен только когда он инкапсулирует самостоятельное бизнес-правило или оркестрацию нескольких зависимостей.
- Если при разработке feature понадобилась глобальная возможность приложения, сначала добавь/расширь профильный `core/*` API и документацию, затем используй этот API из feature.

Чеклист при добавлении фичи:
1. Добавить зависимость модуля в `composeApp/build.gradle.kts`
2. Зарегистрировать Koin-модуль в `KoinApplication { modules(...) }` в `App()`
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

## One-shot события (Effects)

Навигация, snackbar'ы и прочие одноразовые события из ViewModel в UI — **только через `MutableSharedFlow`**. `Channel` + `receiveAsFlow()` в проекте не используем.

Механизм изолирован в `core/ui` — `ru.alarmtrade.specmp.core.ui.effect`: фабрика `sideEffectFlow()` для emit-стороны и composable `collectSideEffect { }` для lifecycle-aware сбора. Руками `LaunchedEffect` + `repeatOnLifecycle` в фичах не писать.

ViewModel — emit через `sideEffectFlow()`:

```kotlin
class LoginViewModel(...) : ViewModel() {

    private val _effect = sideEffectFlow<LoginEffect>()   // extraBufferCapacity = 1, replay = 0
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    private fun emitEffect(effect: LoginEffect) {
        _effect.tryEmit(effect)
    }
}
```

UI — сбор через `collectSideEffect`:

```kotlin
viewModel.effect.collectSideEffect { effect ->
    when (effect) {
        is LoginEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
    }
}
```

Правила:

- `sideEffectFlow()` фиксирует стандартную конфигурацию (`extraBufferCapacity = 1`, `replay = 0`); эмит — через `tryEmit`. `replay` не задаём (повтор события после пересоздания экрана приведёт к двойной навигации).
- `collectSideEffect` собирает с `repeatOnLifecycle(STARTED)` и `rememberUpdatedState` — подписка приостанавливается в фоне, пересоздание лямбды не перезапускает сбор.
- Эффекты эмитим только в ответ на действия пользователя, когда экран на переднем плане — коллектор гарантированно активен. Эмит «в пустоту» (до старта коллектора) будет потерян — не эмитить эффекты из `init`.
- Несколько подписчиков получат одно и то же событие каждый — на эффект подписывается ровно один экран.

---

## Правила работы с диспетчерами (AppDispatchers)

`AppDispatchers` инжектится через Koin и используется **вместо** прямых обращений к `Dispatchers.*`.

| Диспетчер | Назначение |
|-----------|-----------|
| `dispatchers.io` | Сеть, БД, файловый I/O — во ViewModel вокруг вызова repository |
| `dispatchers.default` | Маппинг, комбинация, CPU-работа — в `flowOn()`, `withContext()` |
| `dispatchers.main` | UI-обновления (обычно не нужен явно — `viewModelScope` уже на Main) |

### Где переключать контекст

Диспетчер переключаем **в месте вызова** — обычно во ViewModel: `withContext(dispatchers.io) { repository.foo() }`. Репозитории **не** оборачивают свои методы в `withContext` — это прячет переключение от вызывающего кода и делает поведение менее явным.

**Единственное исключение** — `AppDispatchers` внутри менеджера/репозитория допустим, когда у него есть собственная, не зависимая от UI логика: обычно наблюдение за стейтом с фоновой работой (пуллинг баланса, периодическая синхронизация). Такая работа живёт в доменном объекте и сама владеет своим диспетчером.

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
