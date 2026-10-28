# Практики написания кода — Response / LCEState / ViewModel / DI / State

Документ организован по областям: сначала подходы, общие для всего кода, затем работа
с `core/*`, затем feature-модули по слоям (domain → data → presentation), в конце — сборка
приложения (DI).

## Ограничения — чеклист

Каждое правило раскрыто в своём разделе; здесь — короткая памятка.

- Никогда не использовать `material3` / `foundation` напрямую в фичах — только `Cmp*` из `core/ui`.
- Предпочитать `commonMain`; `expect`/`actual` — только для по-настоящему платформо-специфичного кода.
- **Никаких `launch`/`coroutineScope` в Composable-функциях** — suspend-операции только через ViewModel или Repository; composable вызывает синхронные методы VM.
- **Бизнес-логику не запускать из `LaunchedEffect`/`DisposableEffect`/`rememberCoroutineScope`** — см. «Запуск работы на старте экрана».
- **ViewModel — только через `BaseViewModel`**, корутины — через `safeLaunch` — см. «BaseViewModel».
- **Репозитории возвращают `Response<T>` и никогда не бросают** — см. «Data — репозитории».
- **Domain — чистый Kotlin**: без Compose и других UI-фреймворков, без SDK-типов — см. «Domain».
- **Гонки: `MutableStateFlow` — только через `update { }`, запись `.value = …` запрещена (даже простая); персистентный read-modify-write и многошаговая пересборка внешнего состояния — под `Mutex`** — см. «Корутины и Flow».
- **Ошибки из ViewModel — только типизированные** (enum / sealed в state), не строковые коды — см. «Ошибки фичи».
- **Никаких строковых литералов в `@Composable`** — UI-текст (включая `contentDescription`) только через `stringResource(Res.string.*)` (см. `docs/localization.md`); строки для логики (ключи, теги) — через `const val`, не inline.
- **Импорты — только сверху файла, unused удаляем** — см. «Импорты».
- **У каждого `@Serializable` — `@SerialName("<модуль>.<Класс>")`** — см. «Сериализация».
- **Domain-модель без UI-строк, sentinel-значений и дублирующих иерархий**; отмена ≠ ошибка; запрос с асинхронным результатом возвращает результат или таймаут — см. «Domain».
- **Состояние, привязанное к аккаунту, чистится через `AuthCloseable`**; identity берётся из `AccountRepository`, а не кэшируется полем фичи — см. «Состояние сессии и смена аккаунта».
- **Сессия экрана — `init {}` / `onCleared()` per-entry VM**, без `DisposableEffect`, полей-`Job` и `var`-флагов, которые пишутся с разных диспетчеров — см. «Жизненный цикл ViewModel и сессии».
- **У каждого флага загрузки — гарантированный сброс; ошибка первой загрузки ≠ пустой экран; «нечего делать» ≠ `Success`** — см. «Корректность state».
- **Код проходит `./gradlew spotlessCheck` и `./gradlew detektCheck` без замечаний** (строка ≤ 120, без `runCatching`/`!!`/`println`, generic `catch` только на границах) — см. «Что проверяют анализаторы (Spotless + detekt)» и [`code-quality.md`](code-quality.md).

---

# Общие подходы (весь код)

## KMP: commonMain и expect/actual

Предпочитать `commonMain`. `expect`/`actual` — только для по-настоящему платформо-специфичного
кода (пример: `isMainAppUpdateSupported()` — доступность механизма обновления зависит от
платформы). Если логика выражается общим кодом с узкой платформенной точкой — выноси в
`expect`/`actual` только эту точку, а не весь класс.

Раскладка UI (выравнивание заголовка, число колонок, ширина контента) зависит от **ширины окна**,
а не от платформы: Android-планшет широкий, узкое окно Desktop — компактное. Такие решения
принимаются по вычисленному в UI layout-режиму (compact/medium/expanded), а не через
`expect fun isDesktopPlatform()`.

## Корутины и Flow

- **`MutableStateFlow` обновляем только через `update { }`** — это атомарный CAS,
  потокобезопасный при конкурентных обновлениях из нескольких корутин. Прямое присваивание
  `_state.value = _state.value.copy(...)` теряет обновления при гонке. **Запись через `.value =`
  запрещена полностью**, в том числе простая запись без чтения (`_flag.value = true` →
  `_flag.update { true }`): одно правило без исключений не даёт «простой» записи со временем
  превратиться в read-modify-write. Атомарный «проверил и записал» — `compareAndSet(expect, update)`.
  Чтение `_state.value` допустимо. Правило — для production-кода; тестовые фейки могут писать `.value`.
- **Персистентный read-modify-write не атомарен.** Key-value-хранилища
  (Settings / DataStore / SharedPreferences) не дают атомарного «прочитал → изменил → записал»:
  при конкурентных писателях одно из изменений теряется — например,
  `prefs.getStringSet(...) + x` → `prefs.putStringSet(...)` из двух корутин запишет только
  одно добавление. Либо сериализуй **всю пару read+write** (`Mutex.withLock` вокруг обеих
  операций), либо держи источник истины в `MutableStateFlow` (обновление — атомарный
  `update { }`) и пиши в хранилище уже готовое новое значение из state.
- **Многошаговая пересборка внешнего состояния — только под `Mutex`.** Идемпотентная операция
  вида «снести и построить заново» (например, `cancelAll()` + `schedule(...)` планировщика
  уведомлений), вызываемая из нескольких триггеров, при параллельном запуске переплетает шаги:
  `cancelAll` одного вызова стирает `schedule` другого, итог зависит от порядка гонки.
  Сериализуй: в доменном объекте/репозитории — собственный `Mutex.withLock`, во ViewModel —
  готовый `executeWithProceedLock(needSkipAnother = false)` из `BaseViewModel`
  (см. «BaseViewModel»).
- **Бесконечные циклы — по `isActive`** (кооперативная отмена), а не `while (true)`:
  внутри `flow { }` это `currentCoroutineContext().isActive`, внутри `launch` — `isActive` скоупа.
- `flowOn` / `stateIn` применимы **только к холодным Flow** (`combine`, `zip`, `map`, `flow {}`).
  `MutableStateFlow` / `StateFlow` — горячие, для них эти операторы бессмысленны:

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

## Диспетчеры (AppDispatchers)

`AppDispatchers` инжектится через Koin и используется **вместо** прямых обращений к `Dispatchers.*`.

| Диспетчер | Назначение |
|-----------|-----------|
| `dispatchers.io` | Сеть, БД, файловый I/O — во ViewModel вокруг вызова repository |
| `dispatchers.default` | Маппинг, комбинация, CPU-работа — в `flowOn()`, `withContext()` |
| `dispatchers.main` | UI-обновления (обычно не нужен явно — `viewModelScope` уже на Main) |

**Где переключать контекст.** Диспетчер переключаем **в месте вызова** — обычно во ViewModel:
`withContext(dispatchers.io) { repository.foo() }`. Репозитории **не** оборачивают свои методы
в `withContext` — это прячет переключение от вызывающего кода. Единственное исключение —
`AppDispatchers` внутри менеджера/репозитория допустим, когда у него есть собственная,
не зависимая от UI фоновая логика (пуллинг, периодическая синхронизация): такая работа живёт
в доменном объекте и сама владеет своим диспетчером.

**Хардкод запрещён** — иначе нельзя подменить в тестах:

```kotlin
// ✅ Правильно — через инжектированный AppDispatchers
class MyViewModel(private val dispatchers: AppDispatchers) : BaseViewModel() {
    val state = coldFlow
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initial)
}

// ❌ Неправильно — хардкод
val state = coldFlow
    .flowOn(Dispatchers.Default)
    .stateIn(...)

// Тесты: подмена на UnconfinedTestDispatcher()
val testDispatchers = AppDispatchers(
    io = UnconfinedTestDispatcher(),
    default = UnconfinedTestDispatcher(),
    main = UnconfinedTestDispatcher()
)
```

## Логирование

Использовать **Kermit** на всех платформах.

- **`logger.e` — только необработанные / неожиданные исключения**, то есть сбои контракта,
  а не ожидаемые ошибки. Основные точки уже покрыты инфраструктурой и руками почти не пишутся:
  `CoroutineExceptionHandler` внутри `BaseViewModel.safeLaunch` и `safe*`-хелперы `ResponseExt`
  логируют сами.
- **Ожидаемые ошибки (`Response.Error`) НЕ логируются на каждом слое.** Они — часть контракта:
  всплывают в state → UI. Допустимо одно диагностическое логирование на границе data-слоя
  (repository, при маппинге SDK-ошибки), но не дублирование в VM: error-mapper `asLCEState { }`
  только маппит сообщение, без `logger.e`.
- `logger.d` — трассировка значимых событий (старт/завершение загрузки, результат операции).
- **Тег — от имени класса, не литералом**: `Logger.withTag(this::class.simpleName.orEmpty())`.
  Строковый литерал (`withTag("MyRepository")`) разъедется при переименовании. Во ViewModel свой
  логгер не создаётся — есть `logger` из `BaseViewModel`.

```kotlin
// ✅ ViewModel: свой logger не создаём — есть protected logger из BaseViewModel
logger.d { "Initial home data refresh completed" }

// ✅ error-mapper без логирования — ошибка уже в state
response.asLCEState { e -> e.message ?: "Error" }

// ❌ Плохо — logger.e на каждую ожидаемую ошибку в VM
response.asLCEState { e ->
    logger.e(e) { "Failed to load profile" }   // дублирует data-слой, шумит в логах
    e.message ?: "Error"
}
```

## Импорты

- **Импорты — только сверху файла.** Полностью квалифицированные имена — и свои
  (`ru.alarmtrade.specmp.feature.*.SomeType`), и библиотечные (`androidx.compose.ui.unit.Dp`,
  `androidx.compose.material3.AlertDialogDefaults`) — в теле кода запрещены: сигнатуры, `when`-ветки,
  параметры, возвращаемые типы, аннотации — всё через `import`-блок. Единственное исключение —
  конфликт имён (два разных `Foo` из разных пакетов в одном файле): тогда `import ... as` alias,
  а не inline FQN.
- **Unused imports удаляем** по завершении работы с файлом. Неиспользуемый import — это шум и
  источник ложной связи между модулями. Проверять после каждой крупной правки
  (`./gradlew :moduleName:compileCommonMainKotlinMetadata` — компилятор выдаст warning).

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

## Сериализация

- **Каждый `@Serializable`-класс, объект, интерфейс и enum — с `@SerialName`.** Имя по умолчанию — FQN класса:
  оно меняется при переносе в другой пакет или переименовании и участвует в данных — дискриминатор полиморфных
  типов (`NavKey`-маршруты в `SavedStateConfiguration`, sealed-иерархии), сохранённый back stack. С явным именем
  рефакторинг пакетов и R8 не ломают сохранённое состояние.
- **Формат:** `"<модуль>.<Класс>"`, где модуль — путь Gradle-модуля без `feature:`/`core:`, через точку
  (`login`, `device.firmware`, `configurator-ui`); у вложенных — через точку с внешним (`configurator-ui.SourceKey.Mine`).
  Имя уникально в проекте и после появления **не меняется** — это контракт сохранённых данных.
- `@SerialName` стоит рядом с `@Serializable` (порядок не важен).
- Проверка — pre-commit хук `serial-name-check` (`config/hooks/check-serial-name.sh`); весь репозиторий:
  `config/hooks/check-serial-name.sh`.

```kotlin
@Serializable
@SerialName("login.LoginRoute")
data object LoginRoute : NavKey

@Serializable
@SerialName("configurator-ui.SourceKey")
internal sealed interface SourceKey {
    @Serializable
    @SerialName("configurator-ui.SourceKey.Mine")
    data class Mine(val id: Long) : SourceKey
}
```

## Что проверяют анализаторы (Spotless + detekt)

Код должен проходить `./gradlew spotlessCheck` и `./gradlew detektCheck` — это обязательная стадия
([`code-quality.md`](code-quality.md)). Правила ниже пишутся сразу так, чтобы не получать замечаний:

- **Строка ≤ 120 символов.** Длинные вызовы переносятся по аргументам, цепочки — по точкам, `if/else` длиннее строки
  раскрывается в блоки `{ }`. Длинную строку-литерал разбивай конкатенацией `"…" + "…"` (итоговый текст тот же).
  Комментарии тоже переносятся.
- **Импорты.** Без неиспользуемых и без wildcard (`import a.b.*`); исключения — сгенерированные ресурсы
  (`...generated.resources.*`) и `androidx.compose.*`. Порядок и лишние импорты приводит в порядок `spotlessApply`.
- **Запрещённые вызовы:**

  | ❌ Нельзя | ✅ Вместо этого |
  |---|---|
  | `runCatching { }` | `safeRun { }` / `safeResponseRun { }` из `ResponseExt` (раздел «Data — репозитории») |
  | `println(...)` / `print(...)` | Kermit `Logger` (раздел «Логирование») |
  | `x!!` | `requireNotNull(x) { "что отсутствует" }`, `?:`, `?.let` |
  | `throw IllegalStateException("…")` | `error("…")` / `check(...)` |
  | `flow.value = x` у `MutableStateFlow` | `flow.update { x }` (раздел «Корутины и Flow»); читать `.value` можно, тестовые фейки — исключение |
  | комментарии `FIXME:` и `STOPSHIP:` | закрыть до коммита |

- **Исключения.** Generic `catch (e: Exception/Throwable)` допустим только на границах (`core/network`,
  safe*-хелперы `ResponseExt`): везде ловим конкретный тип или оборачиваем вызов в `safeRun`. Если исключение
  сознательно игнорируется — назови переменную `ignored`. Не глотай исключение молча: передай его как `cause` или
  залогируй.
- **Мёртвый код.** Неиспользуемые параметры, локальные переменные, приватные функции и свойства удаляй, «на будущее»
  не оставляй. Параметр конструктора, нужный только в инициализаторе, пиши без `private val`
  (`class X(args: XArgs) : …`), иначе `UnusedPrivateProperty`. Если заготовка действительно нужна —
  `@Suppress("<Правило>")` на конкретном месте.
- **Имена.** Файл называется по основной декларации; если в файле один класс и рядом функции — класс выносится в
  свой файл (или `@file:Suppress("MatchingDeclarationName")`). Пакет совпадает с каталогом
  (`InvalidPackageDeclaration`). Верхнеуровневые `const val` — `SCREAMING_SNAKE`. Переменная в лямбде не должна
  затенять внешнюю (`it` внутри `it`, одноимённый параметр) — переименуй.
- **Числа.** Литералы кроме `-1`, `0`, `1`, `2` выносятся в именованные константы (`MagicNumber`); не касается
  тестов и `*.kts`. Литерал в именованном аргументе (`Offset(x = 4f, y = 8f)`, `pulseAlpha(periodMillis = 1_800)`)
  правилом не считается магическим — имя параметра уже объясняет число.
- **`suspend` и `Flow`.** Функция, возвращающая `Flow`/`StateFlow`, не помечается `suspend`, если не вызывает
  suspend-функции внутри.
- **Размер функций.** Функция длиннее 120 строк или со сложностью > 15 разбивается на части. Для Compose-функций и
  таблиц-мапперов допустим `@Suppress("LongMethod", "CyclomaticComplexMethod")`.
- **Пустое.** Класс без тела — `class X` (не `class X {}`), конструктор без параметров — без `()`,
  файл заканчивается переводом строки.
- **Подавления** — исключение, ставятся на конкретное место и объединяются в одну аннотацию `@Suppress(...)`;
  правила про файловый и функциональный уровень — в [`code-quality.md`](code-quality.md#подавления).

---

# Core

Границы модулей и полная карта архитектуры — `docs/architecture.md`; документация конкретного
модуля — его `AGENTS.md`. Здесь — что core даёт фичам и когда его расширять.

**Ключевые API core-модулей:**

| Модуль | Что даёт фичам |
|--------|----------------|
| `core/common` | `Response<T>` + `ResponseExt` (`safeRun`, `map*`, `getValueIfSuccess`, …), `AppDispatchers` — обязательная зависимость каждой фичи; `AuthCloseable`, `appScopeQualifier` (app-scoped `CoroutineScope`), `AppSettingsFactory` (именованные key-value хранилища) |
| `core/ui` | `LCEState<T>` + ext, `BaseViewModel`, `sideEffectFlow`/`collectSideEffect`, тема (`AppTheme`, `cmpThemeColors`), `Cmp*` UI Kit, `CmpIcons` |
| `core/specialist` | интеграция Specialist SDK: инициализация, провайдеры (`SpecialistAppVersionProvider`, device info) |
| `core/network` | `HttpClientFactory`, Ktor-клиент |
| `core/localization` | `AppLocale`, чтение/применение локали |

**Правила:**

- **App-level инфраструктура живёт в `core/*` или `composeApp`, не в фиче**: глобальная локаль,
  инициализация сторонних SDK, application-контейнеры, общие settings, network/client factories,
  примитивы темы.
- **Key-value хранилище фичи — только через `AppSettingsFactory.create("<feature>")`** (`core/common`,
  Koin single), а не `Settings()` в фиче: именованное хранилище изолирует ключи фичи префиксом, и
  `clear()` одной фичи не трогает чужие.
- **Feature-модули не создают singleton внешнего SDK/application container** (`SpecialistApp`
  и аналоги) — такой singleton создаётся в профильном `core/*` модуле или в `composeApp`.
- **В конструкторы feature-классов передаются узкие зависимости** (`AuthInteractor`, repository,
  data source, provider), а не цельный SDK/application container: по сигнатуре видно, что реально
  использует класс, и фича не становится владельцем всего SDK.
- **Понадобилась глобальная возможность приложения** — сначала добавь/расширь профильный
  `core/*` API и документацию, затем используй этот API из feature. Не выноси в `core/*`
  «на будущее»: выносится то, что реально нужно нескольким модулям.

---

# Feature-модули

Перед созданием фичи классифицируй её тип — логическая / обособленная / экранная /
пара `<name>` + `<name>-ui`. Таксономия и правила зависимостей — в **`docs/architecture.md`,
раздел «Типы feature-модулей»**.

### Планирование фичи: один модуль или пара `<name>` + `<name>-ui`

Решение принимается **при планировании фичи, до создания модулей**, и согласуется с владельцем проекта:

1. Оцени, будет ли логика фичи (`domain` + `data`: репозитории, модели, сессии) нужна **другим фичам** —
   их ViewModel или логике (а не только `composeApp` для регистрации DI).
2. **Не уверен или «вроде нет» — спроси** владельца явно, перечислив возможных потребителей
   («логика настроек понадобится экрану конфигураций в главном меню?»). Догадку не выдавай за решение.
3. **Логика не переиспользуется** → один **обособленный** модуль `feature/<name>`: `domain` + `data` +
   `presentation` вместе (пакеты внутри модуля сохраняют слои). Пару «на будущее» не создавай.
4. **Логика переиспользуется** (подтверждено или уже есть потребитель) → пара: `feature/<name>` (логика,
   без Compose) + `feature/<name>-ui`.
5. Решение фиксируется в плане фичи и в `AGENTS.md` модуля (кто потребитель логики). Появился потребитель
   у обособленной фичи — тогда выделяй логический модуль (триггеры — `docs/architecture.md`,
   «Когда разделять»).

**Контракт слоёв внутри фичи:**

| Слой | Тип результата | Состояния |
|------|----------------|-----------|
| `data` (репозитории) | `Response<T>` / `Flow<Response<T>>` | `Success<T>` / `Error` |
| `presentation` (VM, Compose) | `LCEState<T>` | `None` / `Loading` / `Content<T>` / `Error` |

Конвертация на границе VM: `response.asLCEState { it.message ?: "Error" }`

**Сбой связи с сервером** не маппится в фиче. SDK-ответы проходят `toResponse()` (`core/specialist`),
он приводит «нет сети / таймаут / ошибку сервера» к `NetworkException(failure: NetworkFailure)`
(`core/common`). VM кладёт в состояние `RequestError.from(response.exception)` (или берёт
`LCEState.Error.networkFailure` — `asLCEState` заполняет его сам), экран показывает
`error.text(Res.string.<фраза экрана>)`: при сбое связи — общий текст `core/ui`
(«Нет подключения к интернету», «Сервер не отвечает», «Ошибка сервера»), иначе — фразу экрана.
Свои «Проверьте интернет» на любую ошибку и проверка сети через `NetworkMonitor` во VM не нужны.

## Domain

`feature/<name>/domain` — модели (`domain/model`) и контракты репозиториев (`domain/repository`).

- **Чистый Kotlin.** Никаких зависимостей на Compose и другие UI-фреймворки:
  `@Immutable`/`@Stable` в `domain/model` не попадают (стабильность обеспечивает
  presentation — см. «Compose: локальное состояние и стабильность»).
- **SDK-типы не протекают в domain.** Модели описываются в терминах фичи, маппинг из SDK-типов —
  в data-слое. Если потребителям нужны только производные значения — модель хранит их,
  а не сырую SDK-структуру:

```kotlin
// ❌ Плохо — SDK-enum в domain-модели
data class MainUserAppFeatures(val features: Set<UserAppFeature>)

// ✅ Хорошо — только то, что нужно потребителям; маппинг из SDK — в data
data class MainUserAppFeatures(
    val hasCanScanner: Boolean = false,
    val hasOrdersRequest: Boolean = false,
    val hasProductSearch: Boolean = false,
)
```

- **Контракты репозиториев:** one-shot операции — `suspend fun ...(): Response<T>`;
  процессы со стадиями и периодические источники — `fun ...(): Flow<Response<T>>` (не suspend).
  Процесс описывается собственной sealed-иерархией под-состояний с гарантированным финалом:

```kotlin
// Процесс синхронизации: стадии InProgress, всегда завершается одним из Finished
sealed interface MainDatabaseSyncStatus {
    sealed interface InProgress : MainDatabaseSyncStatus {
        data object CheckingVersion : InProgress
        data object Downloading : InProgress
        data object Applying : InProgress
    }
    sealed interface Finished : MainDatabaseSyncStatus {
        data class Success(val newVersion: Long) : Finished
        data object NoUpdates : Finished
        data class NotCompatible(val minRequiredAppVersion: Int) : Finished
    }
}
```

- **Гарантированный финал — и для запросов с асинхронным ответом.** Если результат приходит
  отдельным событием, кто-то обязан гарантировать финал ожидания: иначе при обрыве («зомби»-сокет)
  вызывающий навсегда застрянет в загрузке. Допустимо одно из двух: метод возвращает сам результат
  (`Response<Page>`) либо ошибку/таймаут; или ответ — событие **внутри долгоживущей сессии**
  (`Flow` сессии), а финал гарантирует владелец процесса (VM): событие / завершение сессии /
  таймаут. Репозиторий при этом остаётся тонким — без ожидателей и флагов.
- **Признак «данных больше нет» приходит от источника**, а не выводится из побочного результата
  («после дедупликации новых нет — значит конец»): повтор уже известной страницы навсегда
  выключит пагинацию. Если источник такого признака не даёт — эвристика фиксируется в KDoc модели
  как известный риск.
- **Никаких UI-представлений в модели.** Отформатированные строки (`displayTime = "14:05"`,
  `sizeLabel = "2 MB"`) в domain не хранятся — модель держит исходное значение (`LocalDateTime`,
  `Long`), форматирует UI. Иначе формат дублируется по слоям, а уже построенные модели не
  перечитают live-смену языка (`LocalAppLocale`).
- **Отсутствие значения — `null`, а не sentinel.** `url = ""`, `id = -1`, `"unknown"` вместо `null`
  требуют отдельной проверки в каждом потребителе и молча проходят там, где её забыли.
- **Одна иерархия на одну сущность.** Две почти одинаковые sealed-иерархии, различающиеся только
  nullability полей (`Attachment` и `PendingAttachment`), порождают конвертеры туда-обратно,
  которые теряют поля и возвращают `null` на «неудобных» данных. Нужна одна модель с nullable
  полями — либо явное разделение `Incoming`/`Outgoing` с общим интерфейсом.
- **Идентичность — явным контрактом.** Если у модели несколько идентификаторов (клиентский
  `id` + серверный `serverId`), их связь и правила сопоставления описываются в KDoc модели
  (лучше — типом: `sealed interface MessageId { Local; Remote }`), а не размазываются между
  репозиторием и VM. Клиентские id генерируются `Uuid.random()`, не `Random.nextLong()`.
- **Отмена — не ошибка.** Результат, у которого несколько исходов, не схлопывается в `null`
  («не удалось» = ошибка записи + нет прав + пользователь сам нажал «Отмена»). Возвращай
  `Response<T>` или sealed-результат (`Saved` / `Cancelled` / `Failed`); `Cancelled` не показывается
  пользователю как ошибка.
- **Контракт без мёртвых методов.** Метод, который реализован в репозитории и фейке, но нигде не
  вызывается, — либо подключается, либо удаляется вместе с реализациями.
- **Платформенные UI-порты — не domain.** Пикеры файлов, сохранение в «Загрузки», шаринг
  (activity-result, `UIDocumentPickerViewController`, `JFileChooser`) — это presentation-порты:
  интерфейс в `presentation/platform` фичи, actual — в `composeApp`. В `domain/` остаются модели и
  контракты репозиториев; модели, которые порт отдаёт (`PickedFile`), — в `domain/model`, а не в
  файле интерфейса.

## Data — репозитории

`feature/<name>/data/repository` — реализации контрактов; единственное место фичи, где живут
прямые вызовы SDK и маппинг SDK-типов в domain-модели.

```kotlin
// ✅ Правильно — граница с бросающим кодом оборачивается хелпером из ResponseExt
suspend fun fetchUser(id: String): Response<User> {
    return safeRun { api.getUser(id) }
}

// ❌ Неправильно — бросает необработанное исключение
suspend fun fetchUser(id: String): User = api.getUser(id)

// ❌ Неправильно — самописный try/catch вместо хелпера ResponseExt
// (теряет проброс CancellationException и логирование)
suspend fun fetchUser(id: String): Response<User> {
    return try {
        Response.Success(api.getUser(id))
    } catch (e: Exception) {
        Response.Error(e, "Failed to fetch user")
    }
}

// ❌ Неправильно — вызываемый метод уже возвращает Response, обёртка избыточна
suspend fun refreshUser(id: String): Response<User> {
    return safeResponseRun { userRepository.fetchUser(id) } // fetchUser уже Response<User>
}

// ✅ Правильно — Response-цепочка без обёртки, композиция через ResponseExt
suspend fun refreshUser(id: String): Response<UserProfile> {
    return userRepository.fetchUser(id).mapValueIfSuccess { it.toProfile() }
}
```

**Правила:**

- ВСЕ suspend-функции в репозиториях ДОЛЖНЫ возвращать `Response<T>`.
- НИКОГДА не бросать исключения из репозиториев.
- **Безопасные конструкции — только готовые хелперы из `core/common` `ResponseExt`**,
  не самописные try/catch: `safeRun` (бросающий код → `Response`), `safeResponseRun` (блок сам
  возвращает `Response`, но содержит бросающий код), `safeFlowRun` / `safeFlowResponseRun`
  (для `Flow`). Хелперы уже пробрасывают `CancellationException` и логируют ошибку — ручная
  обёртка обычно теряет одно из двух.
- **Не оборачивать то, что уже возвращает `Response`.** Если вызываемый метод (или цепочка)
  возвращает `Response<T>` — в том числе SDK-Response с той же семантикой `Success`/`Error` —
  его контракт уже гарантирует, что исключение не вылетит; повторная обёртка — шум. Обёртка
  нужна только на границе с кодом, который реально может бросить: HTTP/DB-клиент, парсинг,
  файловый I/O, коллекция «сырого» Flow, SDK-вызов без Response-контракта.
- Ветвление и трансформации `Response` — через расширения `ResponseExt` (`map`,
  `mapValueIfSuccess`, `mapIfSuccess`, `mapIfError`, `onSuccess`/`onError`, `getValueIfSuccess`),
  а не ручной `when`, когда расширение выражает то же самое. Одинаковая конвертация
  SDK-Response → `Response`, повторённая в нескольких методах, выносится в одно приватное
  расширение репозитория (`private fun <T> SdkResponse<T>.toResponse(): Response<T>`),
  а не копируется `when`-блоком в каждый метод.
- **Flow-цепочки — без ручных обёрток.** `flow { upstream.collect { emit(it) } }` над уже
  холодной цепочкой избыточен — верни цепочку напрямую; `map { … }.filterNotNull()` → `mapNotNull`;
  `catch` — один раз и через `safeFlowRun`/`safeFlowResponseRun`. Помни, что `catch` **завершает**
  поток: для долгоживущей подписки (WebSocket, события SDK) это значит «событий больше не будет».
  Либо поток самовосстанавливается (`retryWhen`) в репозитории, либо VM явно пересоздаёт подписку
  при ручном реконнекте — «баннер снят, а события не приходят» недопустимо.
- **Маппинг не теряет данные молча.** `else -> null` / ветка, читающая поле только у одного
  наследника, превращают неизвестный вариант SDK в «пустое» сообщение без единой ошибки. Ветки
  исчерпывающие; неподдерживаемый вариант — явная ошибка маппинга (с `logger.d`), а не тихий
  пропуск. Если SDK может отдавать несколько полей одновременно (текст + вложение) — маппятся оба.
- **Неявные допущения — комментарием и тестом.** Часовой пояс сервера (`toInstant(TimeZone.UTC)`),
  единицы измерения, формат идентификаторов: предположение фиксируется комментарием с
  обоснованием и тестом маппинга — иначе смена формата даст сдвиг без ошибок в логах.
- **Большие данные — с лимитом.** `Response<ByteArray>` для файла произвольного размера — риск OOM
  на слабом Android. Минимум — проверка размера и внятная ошибка; лучше — потоковая запись в файл.
- **Периодический источник (поллинг)** оформляется в репозитории как `Flow<Response<T>>`;
  интервал — константа репозитория; цикл — по `isActive`:

```kotlin
override fun observeUnreadMessagesCount(): Flow<Response<Int>> {
    return flow {
        while (currentCoroutineContext().isActive) {
            emit(unreadMessagesCount())
            delay(CHAT_UNREAD_POLL_INTERVAL)
        }
    }
}
```

## Состояние сессии и смена аккаунта

Репозитории фич регистрируются как Koin `single` и **переживают логаут**. Всё, что они держат
про текущий аккаунт (открытое соединение, кэш, незакрытая сессия, офлайн-очередь), после смены
аккаунта протечёт в новую сессию, если его не сбросить явно.

- **Identity пользователя — из `AccountRepository` (`core:specialist`)**, а не полем
  `private var currentUserEmail` в репозитории фичи и не собственным вызовом
  `userInteractor.getUserUseCase`. Профиль резолвится один раз при открытии сессии фичи.
- **Неуспех резолва зависимости — ошибка операции, а не деградация.** Нельзя молча считать
  `myEmail = null` и дальше помечать все сообщения чужими: ошибка всплывает как ошибка открытия
  (баннер/экран ошибки). И не повторять неуспешный запрос на каждое событие.
- **Жизненный цикл сессии — явной операцией, не побочным признаком.** «Обнулили поле — значит
  сессия сменилась» ломается гонкой: чтение/запись поля вне мьютекса из потока событий вернёт
  значение прошлого аккаунта.
- **Состояние, привязанное к аккаунту, чистится через `AuthCloseable` (`core:common`).** Компонент
  реализует `suspend fun release()` и регистрируется в DI с `bind AuthCloseable::class`;
  `LogoutUseCase` (`feature/auth-state`) после успешного logout вызывает `release()` у всех.
  ViewModel не вызывают `AuthStateRepository.logout()` напрямую.

```kotlin
// feature/*/di
single { MyChatRepository(get(), get()) } bind ChatRepository::class bind AuthCloseable::class
```

## Presentation

### BaseViewModel

Все ViewModel наследуются от `BaseViewModel` (`core/ui`, пакет
`ru.alarmtrade.specmp.core.ui.viewmodel`), а не от androidx `ViewModel` напрямую. Что даёт:

- **`safeLaunch { }`** (есть перегрузка с `CoroutineContext`) — вместо `viewModelScope.launch`:
  в контекст добавлен `CoroutineExceptionHandler`, логирующий необработанные исключения.
  `viewModelScope` напрямую используется только для `stateIn`.
- **`logger`** (protected, тег = имя класса) — свой `Logger.withTag(...)` во VM не создавать.
- **`executeWithProceedLock { }`** — взаимоисключение операций под общим для VM mutex'ом:
  по умолчанию пропускает вызов, пока выполняется предыдущий (защита от дублей);
  с `needSkipAnother = false` — сериализует (дожидается и выполняет следом).
  От отмены он НЕ защищает — block живёт в корутине вызывающего.

**Время жизни эффекта = место, где он живёт.** Куда положить запуск работы — определяется
требуемым временем жизни эффекта:

- **Эффект живёт ровно столько, сколько screen/VM** → `init {}` во VM честен. Примеры: лог
  «открыт экран», старт подписки, которую надо убить вместе с VM (`viewModelScope` сделает сам).
  Повторный запуск при пересоздании VM — не баг, а согласованность: новый инстанс = новая жизнь.
- **Эффект привязан к подписке UI на state (init-стадия экрана)** → отдельный холодный flow +
  `stateIn(SharingStarted.Lazily)` в общем `combine` — см. «Запуск работы на старте экрана».
- **Эффект должен пережить экран / выполниться один раз за сессию процесса** → выносим из VM:
  VM не может это гарантировать (пересоздание при навигации, process death). Место — app-scoped
  singleton (репозиторий/контроллер/use-case) с идемпотентным `start()` (guard «уже запущен»:
  `if (job != null) return`), владеющий собственным `CoroutineScope`. Пример в проекте:
  наблюдение сессии в `feature/auth-state` — `AuthStateRepository` зарегистрирован как Koin
  single, его `observeIsLoggedIn()` собирает app-level `AuthStateViewModel` в composition root,
  а не VM отдельного экрана.
- Эмит one-shot эффектов (навигация/снекбар) из `init` запрещён отдельно
  (см. «One-shot события»).

### Жизненный цикл ViewModel и сессии

Каждый `NavDisplay` подключает `rememberViewModelStoreNavEntryDecorator()` (см.
`docs/navigation.md`), поэтому `koinViewModel()` отдаёт **per-entry** инстанс: VM живёт ровно
столько, сколько запись бэкстека. Новый вход на экран = новый инстанс = чистое состояние.

- **Сессия экрана** (подписка на сокет, открытие чата) открывается в `init {}` и закрывается в
  `onCleared()`. Не нужны: `DisposableEffect` в nav-графе, методы `onScreenOpened()`/
  `onScreenDisposed()`, ручной сброс `_state` при входе, поля-`Job` для «текущей сессии».
- **Suspend-работа из `onCleared()`** — через application-scoped скоуп
  (`get(appScopeQualifier)` из `core:common`, передаётся в конструктор VM): `viewModelScope` к
  этому моменту уже отменён.
- **`var`-поля VM не пишутся с разных диспетчеров.** Коллектор на `dispatchers.io`, который
  пишет `var job`/`var flag`, а main — те же поля из интентов, — гонка (потерянная отмена, два
  параллельных цикла). Изменяемое состояние — в `MutableStateFlow` (`update {}`) или сериализация
  через `executeWithProceedLock`; остальные поля main-confined.
- **Одно поле `Job` — одна роль.** Поле, в котором по очереди живут «переподключение» и
  «закрытие сокета», молча отменяет одну операцию другой. Лучше, чтобы поля-`Job` не было вовсе.
- **Самодельные барьеры** («флаг `rerunRequested` + `while`», «`if (busy) return`») не пишутся —
  есть `executeWithProceedLock` (`needSkipAnother = true` — пропустить дубль, `false` —
  выполнить следом).
- **Зависимости — в конструктор, не параметром интента.** `onDownload(item, saver)`, где `saver`
  приходит из UI, прячет зависимость VM от DI и тестов; платформенный порт передаётся через
  конструктор (`parametersOf` из `composeApp`).
- **`WhileSubscribed` — только когда останавливать безопасно.** Для поллинга — да. Для сессии
  (сокет) — нет: `collectAsStateWithLifecycle` + `WhileSubscribed(5.seconds)` погасит соединение
  через 5 секунд после блокировки экрана. Время жизни сессии = время жизни VM.
- **Не открывать/закрывать сессию в `onStart`/`onCompletion` потока.** `Flow.onStart` выполняется
  **до** подписки на upstream — hot-событие без replay, опубликованное во время открытия,
  потеряется. `onCompletion` при отмене вызывается в отменённом контексте — suspend-`close()`
  сразу бросит `CancellationException` (нужен `withContext(NonCancellable)`). Порядок «сначала
  подписка, потом открытие» явно задаётся в `init {}`.

### LCEState — ViewModel + Compose

```kotlin
// ViewModel
class UserViewModel(
    private val repository: UserRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {
    private val _state = MutableStateFlow<LCEState<User>>(LCEState.None())
    val state: StateFlow<LCEState<User>> = _state.asStateFlow()

    fun load(id: String) {
        safeLaunch {
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

### State экрана: множественные LCEState

Когда экрану нужно несколько независимых асинхронных данных, каждое оборачивается в отдельный `LCEState<T>` внутри общего State:

```kotlin
@Immutable
data class DashboardState(
    val profile: LCEState<ProfileContent> = LCEState.None(),
    val transactions: LCEState<List<Transaction>> = LCEState.None(),
    val settings: LCEState<UserSettings> = LCEState.None(),
)
```

#### Императивный подход — `applyFromExecuting`

Подходит когда данные грузятся **по запросу** (one-shot), а не реактивно обновляются. Каждый `MutableStateFlow<LCEState<T>>` загружается отдельно.

```kotlin
class DashboardViewModel(
    private val profileRepo: ProfileRepository,
    private val transactionsRepo: TransactionsRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

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
        // Параллельная загрузка — каждый запуск независим
        safeLaunch {
            _profile.applyFromExecuting(
                execute = { profileRepo.getProfile() },
                errorMapper = { it.message ?: "Ошибка профиля" },
                dispatcher = dispatchers.default,
                map = { it.toContent() }
            )
        }
        safeLaunch {
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

**Важно**: запускайте загрузки в **отдельных `safeLaunch`-блоках** для параллельности. Если запустить последовательно в одном блоке, каждый `applyFromExecuting` заблокирует следующий.

Когда второй запрос зависит от результата первого — последовательно в одном `safeLaunch`:

```kotlin
fun loadData() {
    safeLaunch {
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

#### Реактивный подход — combine / zip + stateIn

Когда репозитории возвращают `Flow` (реактивные источники, подписки на БД, поллинг, WebSocket), вместо ручного управления `MutableStateFlow<LCEState<T>>` лучше **комбинировать Flow-ы** и преобразовывать в `StateFlow`.

| Оператор | Когда использовать |
|----------|-------------------|
| `combine` | Источники эмитят **независимо**, нужно последнее значение от каждого |
| `zip` | Источники эмитят **попарно**, нужно дождаться значения от каждого |

```kotlin
class DashboardViewModel(
    profileRepo: ProfileRepository,        // Flow<Response<Profile>>
    transactionsRepo: TransactionsRepository, // Flow<Response<List<Transaction>>>
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

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

Смешанный вариант (часть данных one-shot, часть реактивно) — см. пример в
«Запуск работы на старте экрана»: one-shot источники пишут в `_state`, реактивные
комбинируются с ним в общий `StateFlow`.

#### Выбор SharingStarted-стратегии

| Стратегия | Поведение | Когда использовать |
|-----------|-----------|-------------------|
| `WhileSubscribed(stopTimeout)` | Активен пока есть подписчики + таймаут после отписки | **По умолчанию для экранов.** Экономит ресурсы при уходе с экрана, `stopTimeout = 5.seconds` — стандартное значение (переживает поворот экрана) |
| `Lazily` | Стартует при первом подписчике, **никогда не останавливается** | Работа «один раз за жизнь VM»: init-загрузка экрана, фоновый кэш |
| `Eagerly` | Стартует немедленно при создании | Критичные данные, нужные сразу (редко используется) |

```kotlin
// ✅ Экран — отписываемся при уходе
.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), initial)

// ✅ Однократная работа / фоновый кэш — живёт пока жива ViewModel
.stateIn(viewModelScope, SharingStarted.Lazily, initial)
```

#### Запуск работы на старте экрана — `onStart`, а не `LaunchedEffect`

Первичную загрузку данных экрана НЕ запускать из UI (`LaunchedEffect(Unit) { vm.load() }` — запрещено):
такой запуск завязан на композицию, а не на подписку на state, требует метода-интента «для старта»
во VM и guard-флагов от повторных вызовов при каждом входе в композицию. Composable side-effect'ы —
только для чисто UI-нужд и проброса события в метод VM (например
`LaunchedEffect(isDark) { vm.onSystemDarkChanged(isDark) }`), но не для самой бизнес-логики;
реакции на действия пользователя — явный метод-интент VM (`onRetryClicked()`).

Триггер «экран реально начал слушать state» уже существует — это подписка на `StateFlow`.
Init-стадия оформляется как **отдельный холодный flow + `stateIn(SharingStarted.Lazily)`**,
участвующий в общем `combine`: `Lazily` стартует при первой подписке и никогда не перезапускается —
«один раз за жизнь VM» выражен временем жизни источника, без `var`-флагов и методов «startIfNeeded»:

```kotlin
class MainScreenViewModel(
    private val mainRepository: MainRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

    private val _state = MutableStateFlow(MainScreenState())

    // Init-стадия: стартует при первой подписке на state (через combine),
    // выполняется ровно один раз за жизнь VM — однократность гарантирует Lazily
    private val initialLoadingState: StateFlow<Boolean> = flow {
        coroutineScope {
            awaitAll(
                async { loadUserProfile() },     // пишут в _state
                async { loadUserAppFeatures() },
            )
        }
        emit(false)  // загрузка завершена — скелетон убирается
    }
        .catch { e ->
            logger.e(e) { "Initial refresh failed" }  // сбой контракта, не Response.Error
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    // Периодический источник: репозиторий отдаёт Flow<Response<T>>,
    // ручные Job + onCleared во VM не нужны
    private val chatUnread: Flow<LCEState<Int>> = mainRepository
        .observeUnreadMessagesCount()
        .map { response -> response.asLCEState { it.message ?: "Error" } }
        .flowOn(dispatchers.io)
        .onStart { emit(LCEState.None()) }  // combine не ждёт первый ответ сети

    val state: StateFlow<MainScreenState> = combine(
        _state,
        initialLoadingState,
        chatUnread,
    ) { state, isInitialLoading, unread ->
        state.copy(isInitialLoading = isInitialLoading, chatUnreadState = unread)
    }
        .onStart { resetTransientState() }  // на каждый (пере)запуск шаринга
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), _state.value)
}
```

**Порядок объявлений важен:** свойства инициализируются сверху вниз, поэтому источники
(`initialLoadingState`, `chatUnread`) объявляются **до** `state` — иначе `combine` захватит
ещё не инициализированное (null) поле и упадёт в runtime.

**`onStart` vs `onSubscription`:**

| Оператор | Куда ставится | Когда срабатывает | Для чего |
|----------|---------------|-------------------|----------|
| `onStart` | на холодную цепочку **до** `stateIn` | при (пере)запуске шаринга: первый подписчик после простоя (`WhileSubscribed`) либо единожды (`Lazily`) | триггер init-загрузки, сброс transient-state при возврате на экран, эмит начального значения в источник |
| `onSubscription` | на **горячий** `SharedFlow`/`StateFlow` (после `stateIn` / на `MutableSharedFlow`) | на **каждого** нового подписчика | редкие случаи «действие на каждую подписку»; для init не годится — несколько коллекторов (второй `collectAsState`, пересоздание composition) повторят действие |

**Правила:**

- **Один раз за жизнь VM** → отдельный источник со `stateIn(Lazily)` (пример выше), не guard-флаг.
  **На каждый (пере)запуск шаринга** (сброс transient-state, обновление «свежести» при возврате
  на экран) → `onStart` на общей цепочке перед `stateIn(WhileSubscribed)` — он сработает повторно,
  когда подписчики вернулись после stopTimeout. `onSubscription` — только когда действие осознанно
  нужно на каждую подписку.
- Лямбда `onStart` не должна блокировать цепочку долгой suspend-работой: долгие загрузки живут в
  собственном источнике (как `initialLoadingState`) или запускаются через `safeLaunch`.
- **Поллинг не держать ручными `Job` во VM.** Репозиторий отдаёт `Flow<Response<T>>`
  (см. «Data — репозитории»), VM комбинирует его в state. `WhileSubscribed` сам останавливает
  поллинг, когда экран никто не слушает, и перезапускает при возврате — `Job`-поля и `onCleared`
  не нужны.
- Источник для `combine` без мгновенного первого значения (сеть, поллинг) — дополнять
  `.onStart { emit(<initial>) }`, иначе `combine` не эмитит, пока не ответят все источники.

**Запрещено, без исключений (частые «оправдания», которые не работают):**

- `LaunchedEffect(Unit) { viewModel.start() }` / `init()` / `load()` / `prepareX()` — **запрещено**,
  даже если метод назван не «load». Переименование init-хука не делает его легальным: любой
  запуск работы VM из composition side-effect нарушает правило. Триггер — подписка на `state`.
- **«Это платформенно-зависимо, поэтому можно через `LaunchedEffect`»** — нет. Composition
  side-effects (`LaunchedEffect`/`DisposableEffect`) допустимы **только** для (а) привязки
  платформенных объектов, живущих в композиции (напр. `BindEffect` для `PermissionsController`),
  и (б) проброса UI-события/сигнала в метод-интент VM (`LaunchedEffect(isDark) { vm.onSystemDarkChanged(isDark) }`).
  Запуск собственного жизненного цикла/загрузки VM сюда не входит — привязка контроллера и старт
  работы VM это разные вещи.
- **Начальный шаг/режим экрана как чистая функция от входа/платформы — это НЕ «работа», а начальное
  значение состояния.** Вычисляй его в конструкторе VM (инициализация `MutableStateFlow`), а не
  через `start()`-интент. Реактивные источники, завязанные на этот шаг (скан, подписки), стартуют
  сами при первой подписке на `state` (`flatMapLatest`/`combine` + `stateIn(WhileSubscribed/Lazily)`).

```kotlin
// ❌ Плохо — init-хук через LaunchedEffect, даже под именем start()
// VM:
fun start() { if (started) return; started = true; _step.value = if (…) Scanning else Rationale }
// UI:
LaunchedEffect(Unit) { viewModel.start() }

// ✅ Хорошо — начальный шаг = начальное состояние; скан реактивен и стартует по подписке
private val _step = MutableStateFlow(if (requirements.isEmpty()) Scanning else Rationale)
private val devices = _step.flatMapLatest { if (it is Scanning) scan() else flowOf(None) }
val state = combine(_step, devices, …) { … }.stateIn(scope, WhileSubscribed(5.seconds), initial)
// реакция на действие пользователя — явный интент (это не init-загрузка):
fun onAllowClicked(controller: PermissionsController) { … }
```

### Корректность state

Типовые дефекты, которые проходят компиляцию и тесты «зелёного пути», но ломают UX.

- **У каждого флага загрузки — гарантированный сброс.** Если `isLoading = true` снимается только
  «по приходу события» — он залипнет, когда событие не придёт. Сброс — в `finally`/по результату
  вызова с таймаутом (см. «Domain»: гарантированный финал), плюс при переходе в offline и при
  закрытии сессии.
- **Ошибка первой загрузки ≠ пустой экран.** Если загрузка упала, а state выглядит как «данных
  нет», пользователь увидит empty-state («Напишите нам») вместо ошибки. Данные — `LCEState<T>`;
  отличать «первую» ошибку от ошибки пагинации флагом не нужно — это следует из контента:

```kotlin
// Ошибка истории
if (_state.value.history is LCEState.Content) {
    emitEffect(Effect.PageLoadFailed)                            // лента остаётся, тост
} else {
    _state.update { it.copy(history = LCEState.Error(message)) } // полноэкранная ошибка + Retry
}
// Живое сообщение во время первой загрузки само переводит history в Content —
// поэтому полноэкранная ошибка уже не затрёт ленту.
```

- **У полноэкранной ошибки есть интент повтора** (`onHistoryRetry()`), иначе единственный выход —
  перезайти на экран.
- **Не гейтить независимые действия чужим состоянием.** Отправка сообщения не зависит от подгрузки
  истории: `enabled = !isLoadingHistory` гасит ввод на каждом скролле вверх. Гейтит только то, что
  реально мешает действию (например, offline).
- **Ветка «нечего делать» не возвращает `Success`.** `else -> Response.Success(Unit)` в
  `when (text / attachment)` превратит сломанный ретрай в галочку «отправлено». Это ошибка
  контракта: логировать и переводить в `Failed`.
- **Сопоставление — в одном порядке.** Если оптимистичные элементы сопоставляются с ответами
  сервера (эхо), все ветки поиска используют один порядок (FIFO = порядок отправки); `indexOfLast`
  в одной ветке и `indexOfFirst` в другой привяжут статус не к тому элементу.
- **UI-текст не обещает несуществующего поведения.** «Сообщения будут отправлены при
  восстановлении связи» допустимо только если очередь персистится и переживает уход с экрана и
  пересоздание процесса. Иначе — меняется текст либо реализуется обещание.
- **Один источник для производного условия.** «Можно ли отправлять» считается в одном месте
  (state или composable), а не дублируется в двух — копии разойдутся молча.
- **Инварианты держит state, а не UI.** Порядок списка, дедупликация поддерживаются при вставке в
  state; сортировка в composable на каждом рендере — лишняя работа и второй источник истины.
- **Check-then-act на `_state.value` не атомарен.** `if (_state.value.busy) return` + отдельный
  `update` пропускают два быстрых нажатия. Проверка и запись — внутри одного `update {}` либо через
  `executeWithProceedLock`.
- **Сетевые вызовы из частых UI-событий — пачкой.** Видимость элементов, ввод текста, скролл — не
  повод для запроса на каждое событие: UI отдаёт пачку (`onMessagesVisible(ids)`), VM батчит с
  debounce, если контракт принимает список.
- **Одинаковые трансформации state — приватным хелпером** (`updateMessageStatus(id, status)`),
  а не пятью копиями `map { if (it.id == x) it.copy(...) else it }`.
- **Неочевидный guard — с комментарием-обоснованием** («игнорируем повторные ошибки: на iOS они
  приходят потоком ~кГц»), иначе его «починят» при рефакторинге.

### One-shot события (Effects)

Навигация, snackbar'ы и прочие одноразовые события из ViewModel в UI — **только через `MutableSharedFlow`**. `Channel` + `receiveAsFlow()` в проекте не используем.

Механизм изолирован в `core/ui` — `ru.alarmtrade.specmp.core.ui.effect`: фабрика `sideEffectFlow()` для emit-стороны и composable `collectSideEffect { }` для lifecycle-aware сбора. Руками `LaunchedEffect` + `repeatOnLifecycle` в фичах не писать.

```kotlin
// ViewModel — emit через sideEffectFlow()
class LoginViewModel(...) : BaseViewModel() {

    private val _effect = sideEffectFlow<LoginEffect>()   // extraBufferCapacity = 1, replay = 0
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    private fun emitEffect(effect: LoginEffect) {
        _effect.tryEmit(effect)
    }
}

// UI — сбор через collectSideEffect
viewModel.effect.collectSideEffect { effect ->
    when (effect) {
        is LoginEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
    }
}
```

**Правила:**

- `sideEffectFlow()` фиксирует стандартную конфигурацию (`replay = 0`, буфер 16, `DROP_OLDEST`); эмит — через `tryEmit`, он никогда не возвращает `false`. `replay` не задаём (повтор события после пересоздания экрана приведёт к двойной навигации).
- Обработчик в `collectSideEffect` выполняется последовательно: пока он suspend'ится (`showSnackbar` висит секунды), следующие эффекты ждут в буфере. Буфер это переживает, но обработчик не должен блокироваться бесконечно (ожидание результата другого экрана, `awaitCancellation`).
- `collectSideEffect` собирает с `repeatOnLifecycle(STARTED)` и `rememberUpdatedState` — подписка приостанавливается в фоне, пересоздание лямбды не перезапускает сбор.
- Эффекты эмитим только в ответ на действия пользователя, когда экран на переднем плане — коллектор гарантированно активен. Эмит «в пустоту» (до старта коллектора) будет потерян — не эмитить эффекты из `init`.
- Несколько подписчиков получат одно и то же событие каждый — на эффект подписывается ровно один экран.

### Compose: локальное состояние и стабильность

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

Правила:

- Аннотации из `androidx.compose.runtime` — **только presentation-слой**: state экрана
  (`XxxScreenState`) и `presentation/model`. **Domain — чистый Kotlin** (см. «Domain»).
- Domain-типы, вложенные в state, в аннотациях не нуждаются: `@Immutable` на самом state-классе
  покрывает композицию, а стабильность data class'ов из `val`-примитивов того же модуля
  Compose-компилятор выводит сам.
- Если domain-тип с интерфейсной статикой (`sealed interface`) передаётся **напрямую** параметром
  composable — компилятор считает его нестабильным. Решение: маппить в аннотированную
  presentation-модель, а не тащить аннотацию в domain.

> Полные практики работы с Compose — когда разбивать на composable-функции, как правильно читать часто меняемые значения, когда использовать `derivedStateOf`, `rememberUpdatedState`, `remember` с ключами, передавать `Flow` vs `State` — см. **`docs/compose-best-prac.md`**.

## Ошибки фичи: domain ↔ presentation

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

**Ошибки из ViewModel — только типизированные**, не строковые коды. `LCEState.Error(message)`
со строкой-«кодом» (`"required_fields"`), которую UI сравнивает с собственной константой, —
запрещён: контракт держится на двух независимых литералах в разных файлах и при рассинхронизации
ломается молча (маппинг в текст — `docs/localization.md`, правило 4).

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
  `Response.Error` (см. «Data — репозитории»), а типизированная ошибка появляется на границе
  ViewModel при обработке `Response`.

---

# Сборка приложения

## Внедрение зависимостей — Koin 4.2.2

Инициализация в `composeApp` через `KoinApplication { modules(...) }` в `App()` (не `startKoin`);
`composeApp` подключает все Koin-модули приложения и является composition root.

```kotlin
// composeApp/.../App.kt
KoinApplication(application = {
    modules(commonModule, specialistModule, loginModule)
}) {
    AppTheme(onThemeChange) { AppNavigation() }
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

**Правила:**

- `feature/*/di` регистрирует только зависимости конкретной фичи: repositories, mappers, ViewModel.
- App-level инфраструктура и SDK-контейнеры регистрируются в `core/*`/`composeApp`,
  не в фиче — см. раздел «Core».
- Не добавляй use case, если он только прокидывает один вызов в repository. В таком случае
  ViewModel может зависеть от repository напрямую; отдельный use case нужен только когда он
  инкапсулирует самостоятельное бизнес-правило или оркестрацию нескольких зависимостей.

Чеклист при добавлении фичи:

1. Добавить зависимость модуля в `composeApp/build.gradle.kts`
2. Зарегистрировать Koin-модуль в `KoinApplication { modules(...) }` в `App()`
3. Добавить маршруты в навигацию в `AppNavigation.kt`
