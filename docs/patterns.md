# Практики написания кода — Response / LCEState / ViewModel / DI / State

Документ организован по областям: сначала подходы, общие для всего кода, затем работа
с `core/*`, затем feature-модули по слоям (domain → data → presentation), в конце — сборка
приложения (DI).

## Ограничения — чеклист

Каждое правило раскрыто в своём разделе; здесь — короткая памятка.

- Компоненты Material3 в фичах — только через `Cmp*` из `core/ui` (detekt `ForbiddenImport` на `androidx.compose.material3.*`); визуальные компоненты других библиотек (`foundation.Image`, `Dialog`, Coil) — тоже через `Cmp*`, layout-примитивы и модификаторы `foundation` — напрямую (`docs/ui-kit.md`).
- Предпочитать `commonMain`; `expect`/`actual` — только для по-настоящему платформо-специфичного кода.
- **Никаких `launch`/`coroutineScope` в Composable-функциях** — suspend-операции только через ViewModel или Repository; composable вызывает синхронные методы VM.
- **Бизнес-логику не запускать из `LaunchedEffect`/`DisposableEffect`/`rememberCoroutineScope`** (detekt `ViewModelCallInEffect`: прямой вызов VM из эффекта запрещён, мост результатов `results.collect { viewModel.onX(it) }` разрешён) — см. «Запуск работы на старте экрана».
- **ViewModel — только через `BaseViewModel<S, E>`** (detekt `ForbiddenImport` на `androidx.lifecycle.ViewModel`): наружу `state`, `sideEffect` и интенты `onXxx()`, state меняется только `reduce`, эффекты — `postSideEffect`, и оба доступны только внутри `intent { }` (получатель `IntentScope`, как `Syntax` в Orbit), корутины — `intent`/`launchReplacing`, стартовая работа — `IntentScope.onStart()` — см. «BaseViewModel».
- **VM, работающая с подключённым блоком, — от `BaseDeviceViewModel<S, E>`** (`feature/device/presentation-core`): работа с блоком через `launchOnDevice` / `onEachDeviceReady` / `onDeviceLost`, имя блока — `connectedDeviceName`; свои `deviceSession.state.map { … Ready … }` не писать (`feature/device/presentation-core/AGENTS.md`).
- **Файлы экрана: `XxxViewModel.kt` — только VM (одна на файл), State и все его подстейты — `XxxState.kt`, эффекты — `XxxEffect.kt`**; модель view со своим состоянием — тоже не в файле composable — см. «Файлы экрана».
- **«Сейчас» и часовой пояс — только из внедрённых `Clock`/`TimeZoneSource`, в composable — из `LocalClock`/`LocalTimeZoneSource`** (detekt `DirectClockUsage`, весь production): без `Clock.System`/`TimeZone.currentSystemDefault()` в теле и дефолтах функций — см. «Источник времени».
- **Репозитории возвращают `Response<T>` и никогда не бросают** — см. «Data — репозитории».
- **Без побочных эффектов в конструкторе** (Konsist `no-constructor-side-effects`): нет `init { }` и корутин в инициализаторах свойств; фоновая работа — идемпотентный `start()` (CAS, `start-once-cas`) доменного сервиса из `AppServicesStarter` — см. «Доменные сервисы».
- **Domain — чистый Kotlin**: без Compose и других UI-фреймворков, без SDK-типов (Konsist `domain-isolation`, `domain-model-purity`) — см. «Domain».
- **Слои и модули — только по направлению зависимостей**: presentation → domain ← data, фича ↛ data и presentation чужой фичи, типы SDK — только data/di (Konsist `module-direction`, `presentation-isolation`, `cross-feature-presentation`, `sdk-confinement`, graph-assert) — см. «Слои и границы модуля».
- **Гонки: `MutableStateFlow` — только через `update { }`, запись `.value = …` запрещена (даже простая); персистентный read-modify-write и многошаговая пересборка внешнего состояния — под `Mutex`** — см. «Корутины и Flow».
- **Ошибки из ViewModel — только типизированные** (enum / sealed в state; в `*State`/`*Effect` нет `Throwable`/`*Exception`/`Response` — Konsist `state-typed-errors`), не строковые коды — см. «Ошибки фичи».
- **Никаких строковых литералов в `@Composable`** (detekt `HardcodedUiText`) — UI-текст (включая `contentDescription`) только через `stringResource(Strings.string.*)` (см. `docs/localization.md`); строки для логики (ключи, теги) — через `const val`, не inline.
- **Импорты — только сверху файла, unused удаляем** — см. «Импорты».
- **Стабильность для Compose:** state и presentation-модели — `@Immutable` (Konsist `state-immutable`), domain — неизменяемый (стабилен через `config/compose/stability.conf`), SDK-типы и голые `List` параметрами composable не передаём, неизменённые экземпляры не пересоздаём; `stability.conf` — крайняя мера, сначала код — см. «Compose: локальное состояние и стабильность».
- **`@Serializable` — только форматы данных и маршруты, у каждого — `@SerialName("<модуль>.<Класс>")`** (Konsist `serializable-placement`) — см. «Сериализация».
- **Domain-модель без UI-строк, sentinel-значений и дублирующих иерархий**; отмена ≠ ошибка; запрос с асинхронным результатом возвращает результат или таймаут — см. «Domain».
- **Состояние, привязанное к аккаунту, чистится через `AuthCloseable`**; identity берётся из `AccountRepository`, а не кэшируется полем фичи — см. «Состояние сессии и смена аккаунта».
- **State экрана — `collectAsStateWithLifecycle()`**, не `collectAsState()` (detekt `ForbiddenImport`): в фоне сбор останавливается и `WhileSubscribed` гасит источники; то, что жить в фоне обязано, — не на `WhileSubscribed` — см. `docs/compose-best-prac.md` §4.
- **Время жизни работы VM задаёт место запуска**: `collectIntoState` — пока экран подписан, `onStart()` — один раз, `launchReplacing(key)`/`launchOnDevice(key)` — всю жизнь VM, дольше экрана — репозиторий со скоупом приложения; без `init {}`, `DisposableEffect`, полей-`Job` и `var` — см. «Жизненный цикл ViewModel и сессии».
- **У каждого флага загрузки — гарантированный сброс; ошибка первой загрузки ≠ пустой экран; «нечего делать» ≠ `Success`** — см. «Корректность state».
- **Код проходит `./gradlew spotlessCheck` и `./gradlew detektCheck` без замечаний** (строка ≤ 120, без `runCatching`/`!!`/`println`, generic `catch` только на границах) — см. «Что проверяют анализаторы (Spotless + detekt)» и [`code-quality.md`](code-quality.md).

---

# Общие подходы (весь код)

## KMP: commonMain и expect/actual

Предпочитать `commonMain`. `expect`/`actual` — только для по-настоящему платформо-специфичного
кода (пример: `isBarcodeScanAvailable` в `core/barcode` — камера для сканера штрих-кода есть только на Android и
iOS). Если логика выражается общим кодом с узкой платформенной точкой — выноси в
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
  Проверяется автоматически: detekt `StateFlowValueMutation` (`detektCheck`, [`code-quality.md`](code-quality.md)).
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
- **Бесконечный цикл в корутине — только по `isActive`** (detekt `LoopWithoutCancellation`): `while (true)`,
  `do { } while (true)` и другое условие-константа запрещены, даже с `delay`/`emit` в теле. Внутри suspend fun и
  `flow { }` — `while (currentCoroutineContext().isActive)`, внутри `launch`/`LaunchedEffect` — `while (isActive)`
  скоупа; в `awaitPointerEventScope { }` (restricted suspension) контекст берётся до входа:
  `val context = currentCoroutineContext()` в `pointerInput`, затем `while (context.isActive)`. Допустима
  конъюнкция (`while (isActive && …)`); своё свойство `isActive` не засчитывается. Выход `break`/`return` из тела —
  как прежде; конечный цикл (пагинация) — с настоящим условием (`do { … } while (page.size >= PAGE_SIZE)`).
- `flowOn` / `stateIn` применимы **только к холодным Flow** (`combine`, `zip`, `map`, `flow {}`).
  `MutableStateFlow` / `StateFlow` — горячие, для них эти операторы бессмысленны:

```kotlin
// ✅ Холодный Flow (combine) — flowOn на upstream; во VM — привязка к state базы
combine(select { systems }, select { query }, ::systemRows)
    .flowOn(dispatchers.default)
    .collectIntoState { content -> copy(content = content) }

// ✅ MutableStateFlow держателя состояния (репозиторий) — уже горячий, наружу asStateFlow()
private val _state = MutableStateFlow(MyState())
val state: StateFlow<MyState> = _state.asStateFlow()

// ❌ Неправильно — flowOn/stateIn на горячем StateFlow
val state = _state
    .flowOn(dispatchers.default)   // бессмысленно
    .stateIn(scope, ...)           // лишнее
```


### Конкурентность

Пакет правил «гонки и корутины» (detekt, набор `specmp`; production-код, тесты вне охвата):

- **Scope сервиса создаёт DI** — `serviceScope(dispatcher, logTag)` (`core.common.coroutines`: `SupervisorJob()` +
  диспетчер + обработчик, логирующий необработанное исключение) передаётся в конструктор (`DeviceSessionImpl(…, scope)`,
  `appScope` в `commonModule`); scope живёт столько же, сколько синглтон Koin, тест передаёт свой
  (`serviceScope(dispatcher, parent = backgroundScope.coroutineContext)`). `CoroutineScope(…)` вне пакета `di` — только
  короткоживущий с явным `scope.cancel()` по завершении (ресивер, мост: scope на одно действие + `cancel()` в
  `finally`); контекст всегда с `SupervisorJob()` и обработчиком — detekt `UnmanagedCoroutineScope`. `GlobalScope` —
  нет (detekt `GlobalCoroutineUsage`).
- **Нет голых `var` и изменяемых коллекций в разделяемых объектах**: у класса со своим `CoroutineScope`, у сервисов по
  имени в любом пакете (`*Repository`/`*Manager`/`*Controller`/`*Starter`/`*Initializer`/`*Coordinator`/`*Session`/`*Notifier`/`*Provider`/`*Store`/`*Source`) и у
  `*Impl` в `..data..` — только потокобезопасные поля и параметры: состояние — `MutableStateFlow`
  + `update { }`/`compareAndSet`; поле, которое читают и пишут строго под `Mutex`, — `@Suppress` с причиной (detekt
  `SharedMutableStateInSingleton`). Локальные `var` в функциях не в счёт.
- **Лямбда `update`/`getAndUpdate`/`updateAndGet` — чистая функция**: при CAS-конфликте она выполняется повторно. Без
  suspend-вызовов, вызовов чужих репозиториев/сервисов, записи в хранилище, `emit`/`tryEmit`/`postSideEffect`,
  `launch`; значение считается в `update`, эффект — до или после (detekt `UpdateLambdaSideEffects`).
- **`Mutex` нереентерабелен и проверяется только атомарно**: `withLock` того же мьютекса внутри `withLock` (в т. ч.
  через свой метод) — deadlock (detekt `NestedSameMutexLock`); «уже занято → выйти» — `tryLock()`, не `isLocked`
  (detekt `MutexIsLockedCheck`); suspend-вызов под блокирующей блокировкой потока (`synchronized`, `Lock.lock()`…
  `unlock()`) запрещён (detekt `SuspendInsideSynchronized`).
- **Отмену не глотать**: широкий `catch (Throwable/Exception)` в корутине вокруг suspend-вызова — только с пробросом
  `CancellationException` (отдельный `catch` выше, `if (e is CancellationException) throw e`, `ensureActive()`);
  обычно — `safeRun { }` (detekt `CatchSwallowsCancellation`, стандартный `SuspendFunSwallowedCancellation`).
- **Горячий поток — один на объект**: `shareIn`/`stateIn` только в инициализаторе свойства, не в функции/геттере
  (detekt `SharingInsideFunction`); `async { }` всегда дожидаются (`await`/`awaitAll`), «огонь и забудь» — `launch`
  (detekt `UnawaitedAsync`); потоки комбинируются `flatMapLatest`/`combine`, без `collect` внутри `collect` (detekt
  `NestedCollect`).
- **Стресс-тест на гонку**: у каждого start-once сервиса и места с `Mutex`/`tryLock` — тест «32 параллельных вызова
  на `Dispatchers.Default` → эффект ровно один раз / без потерь / без пересечения» (`AppServicesStarterTest`,
  `DeviceSessionImplTest` — `start()` и `sessionMutex`, `BaseViewModelTest` — `executeWithProceedLock`,
  `RecentListTest`).

## Диспетчеры (AppDispatchers)

`AppDispatchers` инжектится через Koin и используется **вместо** прямых обращений к `Dispatchers.*`.

| Диспетчер | Назначение |
|-----------|-----------|
| `dispatchers.io` | SDK, сеть, `Settings`, файловый I/O — **в месте вызова** (обычно VM) вокруг вызова репозитория |
| `dispatchers.default` | Маппинг, комбинация, CPU-работа — в `flowOn()`, `withContext()` |
| `dispatchers.main` | UI-обновления (обычно не нужен явно — `viewModelScope` уже на Main) |

**Где переключать контекст.** Диспетчер выбирает **вызывающая сторона** — ViewModel или точка входа
(platform-делегат, обработчик файла, `*Entry`): `withContext(dispatchers.io) { repository.foo() }`, у потока —
`.flowOn(dispatchers.io)` после `repository.foo()`. Репозитории и `*DataSource` **не** оборачивают свои методы
в `withContext` — это прячет переключение от вызывающего; suspend-метод — голый `safeRun { }`/`safeResponseRun { }`
(detekt `RepositoryWithContext`: классы `*Repository`, в т.ч. `Specialist*Repository`, `*RepositoryImpl`,
`*DataSource`). Вызов репозитория из VM без `withContext`/`flowOn` — ошибка, даже если метод «быстрый»: внутри SDK,
`Settings` (`commit()` на Android) или файл.

Исключение — объект, который **сам хранит состояние и сам запускает фоновую работу в собственном `CoroutineScope`**
(сессии, контроллеры с app-scope: `DeviceSessionImpl`, `DeviceSettingsSessionControllerImpl`; scope приходит из DI —
«Конкурентность»): он переключает диспетчер внутри, вызывающий его вызов **не** оборачивает. Правило распознаёт такой класс структурно — свойство, параметр
конструктора или супертип `CoroutineScope`; `@Suppress` не нужен. `withContext(NonCancellable)` (suspend-уборка
в `finally` после отмены) — не выбор диспетчера и разрешён везде.

**`withContext` внутри `flow {}` vs `flowOn`.** Внутри `flow {}` `withContext` можно — для вычисления
значения (`val recent = withContext(dispatchers.io) { models.recent() }`, затем `emit(recent)`).
Нельзя вызывать `emit` **внутри** `withContext` — это нарушает инвариант контекста Flow
(`IllegalStateException: Flow invariant is violated`). Если на другом диспетчере должен работать
весь upstream (сбор SDK-flow, скан, `combine` с маппингом) — `.flowOn(dispatchers.io/default)` после
цепочки; он влияет только на операторы **выше** себя.

**Что не должно работать на Main:** вызовы SDK/сети/BLE/БД (включая `suspend`-методы SDK, которые
«выглядят» асинхронными — внутри может быть блокирующий вызов), обход больших деревьев и сортировка/
фильтрация больших списков. Производные списки экрана (фильтр по поиску, сортировка) кладутся в state готовыми
полями, а не `get()`-свойствами, которые пересчитываются на Main при каждом чтении из Compose: небольшие — в том же
`reduce`, что и их входы (`MineState.withLists()`), тяжёлые — `combine(select { … }).flowOn(dispatchers.default)
.collectIntoState { … }` (`FirmwareSystemsViewModel`).

**Хардкод запрещён** — иначе нельзя подменить в тестах:

```kotlin
// ✅ Правильно — через инжектированный AppDispatchers
class MyViewModel(private val dispatchers: AppDispatchers) : BaseViewModel<MyState, Nothing>(MyState()) {
    override suspend fun IntentScope.onStart() {
        coldFlow.flowOn(dispatchers.default).collectIntoState { value -> copy(value = value) }
    }
}

// ❌ Неправильно — хардкод
coldFlow.flowOn(Dispatchers.Default).collectIntoState { … }

// Тесты: один тестовый диспетчер на все роли (core:testing)
val dispatchers = testDispatchers(StandardTestDispatcher())
```

## Источник времени

«Сейчас» — внедряемый `kotlinx.datetime.Clock`, часовой пояс — `TimeZoneSource` (`core/common/time`). Оба привязаны в
`commonModule`: `single<Clock> { Clock.System }`, `single<TimeZoneSource> { TimeZoneSource.System }`.
`kotlin.time.Clock` не используем: SDK закрепляет `kotlinx-datetime` 0.6.2, где модели (`Instant`) —
`kotlinx.datetime.Instant`, а мостов к `kotlin.time.Instant` нет.

- **Охват detekt `DirectClockUsage` — весь production-код** (domain, data, ViewModel, мапперы/форматтеры
  presentation, composable, инфраструктура `core/*`, androidMain/iosMain). Запрещены `Clock.System`,
  `TimeZoneSource.System`, `TimeZone.currentSystemDefault()`, `System.currentTimeMillis()`/`nanoTime()`, `NSDate()`.
  Вне охвата: тесты, `di`-модули и сами источники времени — `core/common/time` и `core/ui/time/LocalClock.kt`.
- **Классы** (domain, data, ViewModel, инфраструктура) получают их параметром первичного конструктора —
  `clock: Clock = Clock.System`, `timeZones: TimeZoneSource = TimeZoneSource.System`; DI передаёт `get()`, тест —
  фиксированные часы (`FixedClock`) и `TimeZoneSource.fixed(TimeZone.UTC)`. Дефолт допустим только у первичного
  конструктора; в теле, свойствах и дефолтах обычных функций — `clock.now()`/`timeZones.current()`, чистой функции —
  параметром (`timeZone: TimeZone`, без дефолта).
- **Пояс читается на вызов, не внедряется значением**: синглтон с `TimeZone` «замёрз» бы при смене пояса на
  устройстве.
- **Один `now` на операцию**: `val now = clock.now()` один раз и дальше — значение (`SupportChatScreenViewModel.onSend`).
- **Composable** — `LocalClock`/`LocalTimeZoneSource` (`core/ui/time`): `App()`/`FlasherApp()` провайдят их из Koin
  (`LocalClock provides koinInject<Clock>()`), UI-тест — `CompositionLocalProvider(LocalClock provides fixedClock)`;
  системный дефолт — только для превью и корней без провайдера. Хелперы: `currentTimeZone()` (пояс на каждую
  композицию), `currentToday()` («сегодня» на каждую рекомпозицию — `rememberCmpDateLabeler`). Значение пояса,
  от которого зависит `remember`, — его ключ. Дефолт параметра composable допустим только из `CompositionLocal`.
- Замер длительностей и таймауты — `TimeSource.Monotonic`, правило его не трогает.

## Тестирование репозиториев

У каждой реализации репозитория (класс `..data..` с супертипом `*Repository`: `Specialist*Repository`,
`Settings*Repository`) есть тест в тестовом source set своего модуля, который использует класс по имени в коде — не
только в `import`, KDoc или строке (Konsist `repository-impl-tested`, без исключений). Тест — не заглушка ради
правила; минимум:

- **успешный путь**: ответ SDK → доменная модель (`Response.Success` с ожидаемым значением, для потоков — ожидаемая
  последовательность);
- **ошибка SDK → `Response.Error`** с нужным типом: общая ошибка (`SpecialistResponse.Error.Network(IOException)`,
  `NetworkIsNotAvailableException`) — `exception.appError` (`ApiError.NoConnection`…), предметная (`AuthFailure`,
  `UserIsNotConfirmed`) — исключение SDK как есть или доменный результат, если репозиторий его так трактует;
- **ключевые операции**: что уходит в SDK (нормализованный запрос), порядок шагов (синхронизация клиента до входа),
  особые ветки (`settingsVersion not found` → `null`), поток, бросивший исключение, → последний `Response.Error`.

Источники в тесте:

- **SDK AlarmTrade — через `*DataSource`.** Интеракторы SDK — `final` с `internal`-конструктором от DI SDK, их не
  создать и не подменить; поэтому репозиторий получает не интерактор, а `internal interface <Фича>DataSource` из
  `data/datasource`. Его реализация `Specialist<Фича>DataSource` в том же файле — только делегирование в
  интерактор (`authInteractor.loginUseCase.execute(request)`), без ветвлений и маппинга: принимает запросы SDK и
  возвращает `SpecialistResponse<SdkType>`/`Flow<SpecialistResponse<…>>`. Вся логика — сборка запроса, маппинг SDK →
  domain, `toResponse()`, особые ветки — остаётся в репозитории и тестируется с локальным фейком источника
  (`FakeLoginDataSource`). Модели и ответы SDK (`User`, `SettingsUpdatingStatus`, `SpecialistResponse.Error.*`)
  создаются в тесте напрямую. Привязка — в Koin-модуле фичи: `singleOf(::SpecialistLoginDataSource) bind
  LoginDataSource::class`. Эталоны — `SpecialistLoginRepositoryTest`, `SpecialistMainRepositoryTest`,
  `SpecialistAccountRepositoryTest`.
- **Ленивый интерактор** (`() -> Interactor`, недоступный на платформе не должен ронять создание) уходит в источник
  вместе с провайдером: метод источника — `interactor.resolveOrNull()?.useCase?.execute(request)`, ответ `null` —
  интерактор недоступен; репозиторий переводит его `toResponseOrUnavailable()` (`core/specialist`: `null` →
  `interactorUnavailable()`). Тест проверяет и эту ветку (фейк отвечает `null`). Поток над ленивым интерактором —
  не-`suspend` `Flow<…>?` (`resolveOrNull()?.let { flow { emitAll(…) } }`), репозиторий разбирает `null` внутри своего
  `flow { }`: `?: flowOf(interactorUnavailable())` или, у процессов (прошивка, обход, Clone), доменное исключение
  процесса (`FirmwareUpdateException`…). Процессный поток тестируется на успех, ошибку посреди процесса (поток
  обрывается на ней) и недоступный интерактор. Провайдер создаёт Koin-модуль:
  `single<VehicleCatalogDataSource> { SpecialistVehicleCatalogDataSource(cars = { get() }, …) }`.
- **Хранилище настроек** (`Settings*Repository`, «недавние» в `Specialist*Repository`) — `InMemorySettings` из
  `core:testing`; повторное открытие — новый репозиторий над тем же `Settings`, битые данные — `putString` мусора.
- **Другие репозитории фичи** — общий фейк из `*:testing`, если он есть («Тестовые дублёры»), иначе локальная
  реализация интерфейса в тестовом source set (`FakeAccountRepository` в `feature/main`).
- **Время, поллинг** — `runTest` с виртуальным временем: интервал опроса проверяется по `currentTime`.

## Тестовые дублёры

Сначала — общий фейк из модуля тестовых дублёров, своя реализация интерфейса — только под особое поведение теста.
Модули `core/testing` и `feature/<x>/testing` держат код в `commonMain` (KMP не даёт `testFixtures`) и подключаются
**только** в тестовых source set'ах: `commonTest.dependencies { implementation(project(":core:testing")) }`
(Konsist `testing-modules-test-only`). `Fake*`/`Stub*` в production-коде запрещены (`no-fakes-in-production`), рабочая
заглушка — явный `NoOp*`.

| Контракт | Дублёр | Модуль |
|---|---|---|
| `AppDispatchers` | `testDispatchers(dispatcher)` — один диспетчер на `io`/`default`/`main` | `:core:testing` |
| `kotlinx.datetime.Clock` | `FixedClock(instant)` (`instant` — `var`) | `:core:testing` |
| `com.russhwolf.settings.Settings` | `InMemorySettings` — map в памяти | `:core:testing` |
| `AppForegroundProvider` | `FakeAppForegroundProvider` (`foregroundReturns` — `MutableSharedFlow`, тест делает `emit(Unit)`) | `:core:testing` |
| `Speaker` | `FakeSpeaker` — `phrases`/`spoken`, `stops`, `voiceInstalls`, `availability` — `MutableStateFlow` | `:core:testing` |
| `HandsFreeSettings` | `FakeHandsFreeSettings(enabled)` | `:core:testing` |
| `DeviceSessionRepository` | `FakeDeviceSessionRepository(device, link)` — связь `link`, сценарные ответы списками, счётчики | `:feature:device:interaction:testing` |
| `DeviceConnectionRepository` | `FakeDeviceConnectionRepository(states, …)` — статусы подключения, счётчики подключений/закрытий | `:feature:device:interaction:testing` |
| `ProgramModeRepository` | `FakeProgramModeRepository` (`results` по порядку, `calls`/`exits`) | `:feature:device:interaction:testing` |
| `DeviceSession` | `testDeviceSession(repo, conn, dispatcher, transport)` — настоящая сессия, `start()` и `attach` уже сделаны | `:feature:device:interaction:testing` |
| `LocalConfigurationsRepository` | `FakeLocalConfigurationsRepository` — `items`/`changes`, `createResult` + `createGate`, записи `createCalls`/`deleted` | `:feature:configurator:testing` |
| `RegisterPushTokenUseCase` | `TestPushRegistration` — настоящий use case поверх `FakePushTokenSource`, `FakePushTokenRepository` (`sends`, `result`), `FakeSentPushTokenRepository`, `FakeAppLocaleStore` | `:feature:push:testing` |

- Пояс фейка не требует: `TimeZoneSource.fixed(tz)` (`core/common`).
- Расширять общий фейк параметром/состоянием, а не копировать в тест; поведение одного теста (запись аргументов,
  сценарные ответы) — локальный фейк; неиспользуемые члены локального фейка — `error("not used")`.
- Локальные фейки — по файлу на класс (`Fake<Контракт>.kt`), фикстуры — `<Фича>Fixtures.kt`; без сборных `TestFakes.kt`.
- Фейки контрактов фичи — в `feature/<x>/testing` рядом с владельцем. Модули, от которых зависит модуль дублёров
  (`core/common`, `core/lifecycle` для `core/testing`; сама `feature/device/interaction` для своего `testing`), его не
  подключают — цикл через тестовую конфигурацию; у них локальные дублёры.
- Модули `*/testing`: production-правила Konsist к ним не применяются, тестовые исключения detekt —
  `**/testing/src/**`, CPD — проверяет.

## Логирование

Использовать **Kermit** на всех платформах.

- **`logger.e` — только необработанные / неожиданные исключения**, то есть сбои контракта,
  а не ожидаемые ошибки. Основные точки уже покрыты инфраструктурой и руками почти не пишутся:
  `CoroutineExceptionHandler` внутри `BaseViewModel.intent` и `safe*`-хелперы `ResponseExt`
  логируют сами.
- **Ожидаемые ошибки (`Response.Error`) НЕ логируются на каждом слое.** Они — часть контракта:
  всплывают в state → UI. Допустимо одно диагностическое логирование на границе data-слоя
  (repository, при маппинге SDK-ошибки), но не дублирование в VM: `asLCEState(XxxError::from)`
  только маппит ошибку, без `logger.e`.
- `logger.d` — трассировка значимых событий (старт/завершение загрузки, результат операции).
- **Тег — от имени класса, не литералом** (detekt `LoggerTagLiteral`; верхнеуровневый логгер файла — литералом): `Logger.withTag(this::class.simpleName.orEmpty())`.
  Строковый литерал (`withTag("MyRepository")`) разъедется при переименовании. Во ViewModel свой
  логгер не создаётся — есть `logger` из `BaseViewModel`.

```kotlin
// ✅ ViewModel: свой logger не создаём — есть protected logger из BaseViewModel
logger.d { "Initial home data refresh completed" }

// ✅ маппинг без логирования — ошибка уже в state
response.asLCEState(ProfileError::from)

// ❌ Плохо — logger.e на каждую ожидаемую ошибку в VM
response.asLCEState { e ->
    logger.e(e) { "Failed to load profile" }   // дублирует data-слой, шумит в логах
    ProfileError.from(e)
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

- **Где:** `@Serializable` — форматы передачи и хранения (пакеты `..data..`), маршруты навигации (`..navigation..`, `NavKey`
  и типы аргументов маршрутов); в `domain/model` — нет (Konsist `serializable-placement`, `domain-model-purity`).
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
  | `runCatching { }` (detekt `ForbiddenRunCatching`) | `safeRun { }` / `safeResponseRun { }` из `ResponseExt` (раздел «Data — репозитории») |
  | `println(...)` / `print(...)` (detekt `ForbiddenPrintln`) | Kermit `Logger` (раздел «Логирование») |
  | `x!!` | `requireNotNull(x) { "что отсутствует" }`, `?:`, `?.let` |
  | `throw IllegalStateException("…")` | `error("…")` / `check(...)` |
  | `flow.value = x` у `MutableStateFlow` (detekt `StateFlowValueMutation`) | `flow.update { x }` (раздел «Корутины и Flow»); читать `.value` можно, тестовые фейки — исключение |
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
- **Порядок верхнеуровневых свойств.** Свойство верхнего уровня не читает свойство, объявленное ниже в том же файле:
  инициализаторы идут сверху вниз, и чтение даст `null` (на iOS — падение Koin в `includes`) (Konsist `top-level-init-order`).
- **Подавления** — исключение, ставятся на конкретное место и объединяются в одну аннотацию `@Suppress(...)`;
  правила про файловый и функциональный уровень — в [`code-quality.md`](code-quality.md#подавления).

---

# Core

Границы модулей и полная карта архитектуры — `docs/architecture.md`; документация конкретного
модуля — его `AGENTS.md`. Здесь — что core даёт фичам и когда его расширять.

**Ключевые API core-модулей:**

| Модуль | Что даёт фичам |
|--------|----------------|
| `core/common` | `Response<T>` + `ResponseExt` (`safeRun`, `map*`, `getValueIfSuccess`, …), `AppDispatchers` — обязательная зависимость каждой фичи; `AuthCloseable`, `appScopeQualifier` (app-scoped `CoroutineScope`), `AppSettingsFactory` (именованные key-value хранилища), `Clock`/`TimeZoneSource` (`time/`, «Источник времени») |
| `core/ui` | `LocalClock`/`LocalTimeZoneSource` (`time/`), `LCEState<T, E>` + ext, `AppError.message()`, `BaseViewModel<S, E>`, `collectSideEffect`, тема (`AppTheme`, `CmpTheme.colors`, токены `CmpSpacing`/`CmpRadius`/`CmpSize`), `Cmp*` UI Kit, `CmpIcons` |
| `core/specialist` | интеграция Specialist SDK: инициализация, провайдеры (`AppVersionProvider` из `core/common`, device info) |
| `core/network` | HTTP-движки Ktor (для SDK и Coil), `NetworkMonitor` |
| `core/localization` | `AppLocale`, чтение/применение локали |

**Правила:**

- **App-level инфраструктура живёт в `core/*` или `composeApp`, не в фиче**: глобальная локаль,
  инициализация сторонних SDK, application-контейнеры, общие settings, network/client factories,
  примитивы темы.
- **Key-value хранилище фичи — только через `AppSettingsFactory.create("<feature>")`** (`core/common`,
  Koin single), а не `Settings()` в фиче: именованное хранилище изолирует ключи фичи префиксом, и
  `clear()` одной фичи не трогает чужие. Подробнее — «Хранение настроек».
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
| `presentation` (VM, Compose) | `LCEState<T, E>` | `None` / `Loading` / `Content<T>` / `Error<E>` (E — ошибка фичи) |

Конвертация на границе VM: `response.asLCEState(XxxError::from)`

**Общие ошибки (связь с сервером, Bluetooth, разрешения, связь с блоком)** не маппятся в фиче.
SDK-ответы проходят `toResponse()` (`core/specialist`), он приводит их к `AppException(error: AppError)`
(`core/common`: `ApiError` — нет сети / таймаут / ошибка сервера, `PlatformError` — Bluetooth выключен,
нет разрешения, блок не найден / связь прервана…). Ошибка фичи оборачивает их подтипом
`Common(AppError)`, экран показывает общий текст `AppError.message()` (`core/ui`). Свои «Проверьте интернет»
на любую ошибку и проверка сети через `NetworkMonitor` во VM не нужны — см. «Ошибки фичи».

### Слои и границы модуля

Раскладка и направления держатся проверками (Konsist, graph-assert; таблица — [`code-quality.md`](code-quality.md),
«Правило → проверка»):

- **Пакет = модуль**: пакет файла начинается с базового пакета модуля, дефисы схлопываются, суффиксы `-ui`/`-core` —
  подпакет (`feature/device/presentation-core` → `…feature.device.presentation.core`) (Konsist `module-package`).
- **Направления модулей**: `core/*` импортирует только `core/*`, фичи — `core/*` и фичи, пакеты composition roots
  (`composeApp`, `flasherApp`) не импортирует никто (Konsist `module-direction`); на уровне Gradle — graph-assert
  (`core` ↛ `feature`, от `*-ui` зависят только composition roots, `docs/architecture.md`).
- **Слои внутри фичи**: domain не видит data/presentation/di, Compose, ресурсы, хранилища, сериализацию и SDK (Konsist
  `domain-isolation`); пакеты `..data..` импортируются только из data и di фичи, в `core` — не из presentation, в
  приложениях — только из di (Konsist `presentation-isolation`); data не видит presentation и di (Konsist `data-isolation`).
- **Чужая фича — только через domain**: data другой фичи не импортируется (Konsist `feature-data-isolation`), presentation
  другого модуля — тоже (Konsist `cross-feature-presentation`); общий presentation фич устройства —
  `feature/device/presentation-core`, экран другой фичи — слот (`@Composable`-колбэк), который заполняет composition root.
- **Роли — в своих пакетах** (Konsist `layer-placement`): реализация репозитория — `data/repository`, `*DataSource` —
  `data/datasource`, реализация доменного сервиса — `data/service` (или `platform`), интерфейс `*Coordinator`/`*Controller` —
  `domain/service`; интерфейс `*Repository` фичи — `domain/repository` (Konsist `repository-in-domain`).
- **SDK AlarmTrade** (`ru.alarmtrade.*` вне `ru.alarmtrade.specmp`) — только data/di фич, `core/specialist`,
  `core/permissions`, `core/network` и приложения (Konsist `sdk-confinement`).
- **`@Composable` фичи — только в `presentation`**, включая форматтеры текста (Konsist `composable-in-presentation`).

## Domain

`feature/<name>/domain` — модели (`domain/model`) и контракты репозиториев (`domain/repository`).

- **Чистый Kotlin.** Никаких зависимостей на Compose и другие UI-фреймворки:
  `@Immutable`/`@Stable` в `domain/model` не попадают. Для Compose модели стабильны через
  `config/compose/stability.conf` — поэтому **модели domain неизменяемы транзитивно**: только `val`,
  без `var`, мутабельных коллекций, массивов и изменяемого состояния внутри (см. «Compose: локальное
  состояние и стабильность»).
- **SDK-типы не протекают в domain.** Модели описываются в терминах фичи, маппинг из SDK-типов —
  в data-слое. Если потребителям нужны только производные значения — модель хранит их,
  а не сырую SDK-структуру:

```kotlin
// ❌ Плохо — SDK-enum в domain-модели
data class MainUserAppFeatures(val features: Set<UserAppFeature>)

// ✅ Хорошо — только то, что нужно потребителям; маппинг из SDK — в data
data class MainUserAppFeatures(
    val hasOrdersRequest: Boolean = false,
    val hasProductSearch: Boolean = false,
)
```

- **Модель — данные, не UI-текст** (Konsist `domain-model-ui-text`): у классов `domain/model` нет полей
  `display*`/`formatted*` и строковых `*Text` — подпись собирает presentation из `stringResource`. Текст, который
  присылает SDK (подписи краёв и сообщение валидации в `SettingNode`), — `@Suppress` с причиной.
- **Контракты репозиториев** (интерфейс — в `domain/repository`, Konsist `repository-in-domain`; Konsist
  `repository-contract` — интерфейсы `*Repository` и всё в `domain/repository`,
  detekt `RawFlowFromRepository` — публичные потоки реализаций): one-shot операции — `suspend fun ...(): Response<T>`;
  процессы со стадиями и периодические источники — `fun ...(): Flow<Response<T>>` (не suspend); других членов нет.
  Синхронный снимок (начальный state VM) — `StateFlow<Response<T>>.value` (`PaneWidthStore.widthsDp`);
  сигнал «изменилось» — тоже `Flow<Response<Unit>>`. Константа платформы или сборки — не член репозитория: значение
  из DI (`supportedDeviceTransports()` в фабрике VM, `AppVersionProvider`). Синхронная команда «в фоне приложения»,
  которую зовут из `onCleared`/`finally`, — доменный сервис (`domain/service`, `DeviceSettingsSessionController`),
  а не репозиторий.
  Процесс описывается собственной sealed-иерархией под-состояний с гарантированным финалом; **неудача процесса —
  последний `Response.Error`, а не стадия `Failed`**: типизированная причина — доменное исключение
  (`FirmwareUpdateException(failure)`), VM достаёт её расширением (`toFirmwareFailure()`, чужое исключение — `Unknown`).
  Процесс, который должен идти без подписчика экрана, VM собирает в `launchReplacing` с `when (step)` по `Response`;
  данные экрана — `collectIntoState(onError)`:

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
  `Long`), форматирует UI. Иначе формат дублируется по слоям и зависит от языка того слоя, где его собрали.
- **Отсутствие значения — `null`, а не sentinel** (detekt `DomainSentinelDefault`). `url = ""`, `id = -1`, `"unknown"` вместо `null`
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
- **Контракт без мёртвых методов** (Konsist `contract-member-usage`). Метод, который реализован в репозитории и фейке,
  но нигде не вызывается, — либо подключается, либо удаляется вместе с реализациями.
- **Платформенные UI-порты — не domain.** Пикеры файлов, сохранение в «Загрузки», шаринг
  (activity-result, `UIDocumentPickerViewController`, `JFileChooser`) — это presentation-порты:
  интерфейс в `presentation/platform` фичи, actual — в `composeApp`. В `domain/` остаются модели и
  контракты репозиториев; модели, которые порт отдаёт (`PickedFile`), — в `domain/model`, а не в
  файле интерфейса. Импорт/экспорт текстового файла (`.json` и т.п.) своего порта не заводит — общий
  `TextFilePicker`/`TextFileSaver` из `core/files` (`core/AGENTS.md`); выбор изображения (камера/галерея,
  JPEG до 800 px) — общий `ImagePicker` оттуда же.

### Use case

Слой use case массово не вводится: ViewModel зовёт репозиторий (в том числе чужой фичи через её `domain/repository`).
`*UseCase` нужен только для оркестрации (Konsist `use-case-package`, `use-case-contract`):

- лежит в `feature/<name>/domain/usecase` (в `core` — пакет `usecase` модуля);
- в первичном конструкторе ≥ 2 зависимостей-ролей (`*Repository`/`*UseCase`/`*Coordinator`/`*Controller`/`*Service`/
  `*Session`/`*Closeable`, в том числе `List<…>` из `getAll()`); диспетчеры, часы, логгер не считаются;
- единственный публичный метод — `operator fun invoke`: `suspend` → `Response<T>`, иначе `Flow`/`StateFlow<Response<T>>`.
  Пример — `ReleaseAccountStateUseCase` (`SessionMarkRepository` + все `AuthCloseable`), вызов `releaseAccountState()`.

Одна зависимость — это не use case (VM зовёт репозиторий); чистое правило без зависимостей — класс с ролью
(`*Validator`/`*Resolver`/`*Calculator`), не `*UseCase`.

## Data — репозитории

`feature/<name>/data/repository` — реализации контрактов; единственное место фичи, где живёт маппинг SDK-типов в
domain-модели. Прямые вызовы SDK — в `data/datasource` (`Specialist*DataSource`, только делегирование).

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
    return userRepository.fetchUser(id).map { it.toProfile() }
}
```

**Правила:**

- **Видимость и имя реализации.** Всё в пакетах `..data..` — `internal`/`private` (Konsist `data-visibility`): наружу
  модуля видны только контракты domain. Реализация репозитория называется по источнику — `Specialist<Интерфейс>`
  (`SpecialistLoginRepository : LoginRepository`), реализует свой интерфейс (Konsist `repository-impl`) и `internal`
  (`impl-visibility`, как и любой `*Impl`). Создаёт её только Koin-модуль фичи (`koin-wiring`), см. «Внедрение
  зависимостей».
- **SDK — через `*DataSource`.** Репозиторий не держит интерактор SDK: вызовы идут через `internal interface
  <Фича>DataSource` (`data/datasource`) с реализацией-делегатом `Specialist<Фича>DataSource`, иначе репозиторий не
  протестировать (см. «Тестирование репозиториев»). `*DataSource` в конструкторе — только у реализации репозитория и
  составного `*DataSource`: VM, use case и доменные сервисы ходят через репозиторий (Konsist `data-source-consumers`).
- ВСЕ suspend-функции в репозиториях ДОЛЖНЫ возвращать `Response<T>`.
- НИКОГДА не бросать исключения из репозиториев (detekt `ThrowInRepository`: в файлах `*Repository`/`*UseCase`
  `throw`, `error()`, `check`/`require*`, `!!` — только внутри `safeRun { }`/`safeResponseRun { }`/`safeFlowRun { }` (включая
  операторы потока внутри `safeFlowRun`); проброс `CancellationException` разрешён).
- **Контракт результата: `Response` не теряется молча** (detekt `ResponseIgnored`, `ResponseGetOrThrow`). Результат
  вызова либо используется (присваивание, `return`, `when`, условие), либо обрабатывается на месте: во ViewModel
  ошибка — в state или эффект (`when`/`collectIntoState(onError)`); в фоне и при уборке (`finally`, закрытие сессии,
  «огонь и забудь», повтор, итог которого показывает сам экран) — `.onError { logger.w(it) { "…" } }`. Пустой
  `onError { }` — не обработка. Ошибку не превращают обратно в исключение (`getOrThrow()`): ветвление — `when`/`map*`/
  `onError`, значение с запасным — `getValueIfSuccess()`.
- **Безопасные конструкции — только готовые хелперы из `core/common` `ResponseExt`**,
  не самописные try/catch: `safeRun` (бросающий код → `Response`), `safeResponseRun` (блок сам
  возвращает `Response`, но содержит бросающий код), `safeFlowRun` (для `Flow<Response<T>>`). Хелперы уже пробрасывают `CancellationException` и логируют ошибку — ручная
  обёртка обычно теряет одно из двух.
- **Не оборачивать то, что уже возвращает `Response`.** Если вызываемый метод (или цепочка)
  возвращает `Response<T>` — в том числе SDK-Response с той же семантикой `Success`/`Error` —
  его контракт уже гарантирует, что исключение не вылетит; повторная обёртка — шум (detekt `RedundantSafeRun`). Обёртка
  нужна только на границе с кодом, который реально может бросить: HTTP/DB-клиент, парсинг,
  файловый I/O, коллекция «сырого» Flow, SDK-вызов без Response-контракта.
- Ветвление и трансформации `Response` — через расширения `ResponseExt` (по одному имени на операцию:
  `map`, `mapIfSuccess`, `mapIfError`, `onError`, `getValueIfSuccess`; новое — только под реальный вызов, Konsist
  `core-helper-usage`),
  а не ручной `when`, когда расширение выражает то же самое. Одинаковая конвертация
  SDK-Response → `Response`, повторённая в нескольких методах, выносится в одно приватное
  расширение репозитория (`private fun <T> SdkResponse<T>.toResponse(): Response<T>`),
  а не копируется `when`-блоком в каждый метод.
- **Flow-цепочки — без ручных обёрток.** `flow { upstream.collect { emit(it) } }` над уже
  холодной цепочкой избыточен — верни цепочку напрямую; `map { … }.filterNotNull()` → `mapNotNull`;
  `catch` — один раз и через `safeFlowRun`. Помни, что `catch` **завершает**
  поток: для долгоживущей подписки (WebSocket, события SDK) это значит «событий больше не будет».
  Либо поток самовосстанавливается (`retryWhen`) в репозитории, либо VM явно пересоздаёт подписку
  при ручном реконнекте — «баннер снят, а события не приходят» недопустимо.
- **Мапперы — в `data/mapper`** (Konsist `mapper-placement`): `toDomain*`/`to*Dto` — top-level `internal`/`private`
  функции в пакете `data.mapper` своего модуля (`data/mapper/*Mappers.kt`; в `core` — пакет `mapper` модуля), не в файле
  репозитория. Приватный маппер-член класса репозитория допустим. Обратный маппинг presentation → domain — не
  `toDomain`, а `to<Модель>()` в presentation (`SupportChatUiAttachment.toAttachment()`).
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
override fun observeChatAvailability(): Flow<Response<Boolean>> = flow {
    while (currentCoroutineContext().isActive) {
        emit(chatAvailability())
        delay(CHAT_AVAILABILITY_POLL_INTERVAL)
    }
}
```

### Хранение настроек

- Платформенный `Settings()` создаёт только `core/common/di` (`platformSettings`, detekt `SettingsInstantiation`): поверх
  него — `AppSettingsFactory` (Koin single), фича получает своё хранилище `get<AppSettingsFactory>().create("<feature>")`
  в Koin-модуле и передаёт конструктором. Напрямую `platformSettings` читают только язык и тема (`core/localization`,
  `core/ui`) — до старта Koin, ключи без префикса.
- В фиче `com.russhwolf.settings.*` и `core.common.settings.*` — только `data` и `di` (Konsist `storage-usage`): domain и
  presentation получают данные через репозиторий.
- Хранилище фичи — это репозиторий: контракт `*Repository` в `domain/repository`, реализация
  `Settings<Интерфейс>` в `data/repository` (`SettingsRecentVehiclesRepository`); `*Store`/`*Storage` в фичах запрещены
  (Konsist `forbidden-suffix`).

### Платформенные возможности

Konsist `capability-usage`:

- API `core/permissions`, которое обращается к ОС (`PermissionsProvider`, `BindEffect`, `permissionsModule`,
  `blePermissions`), и moko-permissions — в фиче только `data`, `di` и файл ViewModel (`ScanDeviceViewModel`); значения
  (`DeviceAccessRequirement`, `PermissionResult`) — всем. Composition roots — свободно.
- Порты `presentation/platform` фичи (пикеры, сохранение, открытие файлов) — только presentation своего модуля, `di` и
  composition roots. Общие порты фич устройства (файлы настроек `.dxs`: `SettingsFilePicker`/`SettingsFileSaver`) —
  в `feature/device/presentation-core` (`platform`), их импортирует presentation любой фичи.

## Доменные сервисы

Объект, который держит состояние процесса приложения или работает дольше экрана (сессия устройства, уборка сессий
настроек), — доменный сервис: интерфейс в `domain/service`, реализация `*Impl` — `internal` в `data/service`,
`single` в Koin-модуле фичи (`DeviceSession` → `DeviceSessionImpl`, `DeviceSettingsSessionController` →
`DeviceSettingsSessionControllerImpl`). Чистая логика без своего скоупа и I/O (`ServicePinCooldown`,
`ProgramModeEntry` в `interaction/domain/session`) сервисом не оформляется.

- **Контракт** (Konsist `service-contract`): `suspend` → `Response<T>`, потоки → `…<Response<T>>`, синхронная
  функция — только команда с результатом `Unit`/`Boolean` (`start()`, `close()`, «закрыть в фоне» из `onCleared`).
  Состояние сервиса в памяти, которое не может «упасть» (`DeviceSession.state` — ошибка связи уже свёрнута в
  `Disconnected`, `reboot`, `transport`), — `StateFlow<T>` с `@Suppress("konsist.service-contract")` и причиной.
  «Дождаться» — отдельный `suspend`-член с `Response` (`close()` + `closeAndAwait()`), не `Job` наружу.
- **Реализацию для тестов других модулей** собирает модуль тестовых дублёров через Koin-модуль фичи: реализация
  `internal`, а VM-тестам нужна настоящая сессия поверх фейковых репозиториев. Скоуп сервиса — отдельная Koin-запись
  (`deviceSessionScopeQualifier`), тест подменяет её, репозитории и скоуп (`testDeviceSession(…)` из
  `:feature:device:interaction:testing`, уже после `start()`). Публичных фабрик «только для тестов» в `di` нет.

### Без побочных эффектов в конструкторе

Создание объекта (Koin-резолв — на любом потоке, в непредсказуемый момент) ничего не запускает (Konsist
`no-constructor-side-effects`, весь production): нет `init { }`, инициализаторы свойств не запускают корутин —
`launch`/`async`/`launchIn` (кроме `CoroutineStart.LAZY`), `shareIn`/`stateIn` с `SharingStarted.Eagerly`. Создать
`CoroutineScope`, `MutableStateFlow`, `stateIn(WhileSubscribed)` — можно; геттер и `by lazy` исполняются не в
конструкторе. Белый список `init` — платформенные мониторы сети (`AndroidNetworkMonitor`, `IosNetworkMonitor`):
синхронная регистрация колбэка в ОС.

- **Состояние, производное от потока**, — `stateIn(WhileSubscribed)` (`DeviceSession.state`: горячий, пока есть
  подписчики).
- **Реакция без подписчиков** — `start()` доменного сервиса со своим скоупом (`DeviceSessionImpl.start()` публикует
  `reboot`). Сервисы запускает `AppServicesStarter` (`core/common`; слоты — `appServicesModule` в `composeApp`,
  `flasherServicesModule` во `flasherApp`, `start()` — из корня композиции): новый process-wide сервис — новый слот.
- **`start()` идемпотентен через CAS** (Konsist `start-once-cas`): guard — `MutableStateFlow(false).compareAndSet(false,
  true)`, не `var started`/`var job` (check-then-set при параллельном `start()` запустит работу дважды).

## Состояние сессии и смена аккаунта

Репозитории фич регистрируются как Koin `single` и **переживают логаут**. Всё, что они держат
про текущий аккаунт (открытое соединение, кэш, незакрытая сессия, офлайн-очередь), после смены
аккаунта протечёт в новую сессию, если его не сбросить явно.

- **ViewModel зон авторизации живут одну сессию**: `AuthStateNavGraph` оборачивает и авторизованную,
  и неавторизованную зону в `ViewModelScope(name, session)` (`core/ui`), поэтому каждый вход/выход
  получает новые VM — их загрузка при старте (`onStart()`) выполняется заново, формы входа и
  регистрации пустые. Сбрасывать state VM «на логаут» вручную (`reset()` из `DisposableEffect`) не
  нужно; своё хранилище VM для зоны, переживающей смену аккаунта, не заводить (`docs/navigation.md`).

- **Identity пользователя — из `AccountRepository` (`feature/account`)**, а не полем
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
  `ReleaseAccountStateUseCase` (`feature/auth-state`) вызывает `release()` у всех при любом выходе из аккаунта:
  после успешного `LogoutUseCase` и когда сессия кончилась сама (`AuthSessionController` из `AppServicesStarter`
  наблюдает `observeIsLoggedIn()`). Один раз на сессию — по сохранённой отметке сессии; на старте без неё не вызывается.
  ViewModel не вызывают `AuthStateRepository.logout()` напрямую.

```kotlin
// feature/*/di
single { MyChatRepository(get(), get()) } bind ChatRepository::class bind AuthCloseable::class
```

## Presentation

### Файлы экрана: ViewModel, State, Effect

Экран (или view со своим состоянием) раскладывается по файлам **с самого создания**:

```
presentation/screen/<screen>/
├── XxxScreen.kt          ← composable экрана
├── XxxActions.kt         ← набор колбэков экрана (если есть)
├── XxxViewModel.kt       ← только ViewModel: один класс на файл (+ его private-константы/хелперы)
├── XxxState.kt           ← State экрана и ВСЕ его подстейты
├── XxxEffect.kt          ← one-shot эффекты экрана (если есть)
└── subcomponents/        ← composable-части экрана
```

- **В `XxxViewModel.kt` — только ViewModel.** State, его подстейты, эффекты, опции/параметры VM не объявляются
  в файле VM: открыв VM, читатель видит логику, а не модель; модель экрана читается одним файлом.
- **`XxxState.kt` — State и все подстейты**, которые в нём лежат или из него выводятся: диалоги (`XxxDialog`),
  режимы и шаги (`CompactStep`, `MineMessage`), формы (`CloudSaveForm`), элементы списков, которые строит экран
  (`SupportChatListItem`), «местоположение» и пометки строк (`SettingsLocation`, `RowMarks`), производные функции
  от State (`VehicleSelectState.compactStep()`). Ошибка фичи — в `presentation/model` (общая для экранов фичи).
- **Одна ViewModel — один файл.** Несколько VM в одном файле (`CloudViewModels.kt`) не держим.
- **View со своим состоянием** (компонент, которому передаётся собственная модель) — та же схема: модель — в
  `XxxState.kt` экрана или в `presentation/model`, а не в файле composable.
- **В файле composable допустимы** только private-детали отрисовки, не являющиеся состоянием: режим раскладки по
  ширине окна (`DeviceSelectLayout`), промежуточные структуры обхода/рисования (`TreeRow`, `WrenchDraw`),
  стилевые enum-параметры компонента (`HeroTone`).

### BaseViewModel

Все ViewModel фич наследуются от `BaseViewModel<S, E>` (`core/ui`, пакет `ru.alarmtrade.specmp.core.ui.viewmodel`), а
не от androidx `ViewModel` напрямую — импорт `androidx.lifecycle.ViewModel` вне `core/ui` запрещён detekt
(`ForbiddenImport`); имя — `*ViewModel`, в фиче — пакет `presentation` (Konsist `view-model`). VM, работающая с подключённым блоком, — от `BaseDeviceViewModel<S, E>` (`feature/device/presentation-core`,
см. ниже). `S` — `XxxState`, `E` — `XxxEffect`/`XxxSideEffect` или `Nothing` (detekt `ViewModelContractNames`).

| Член | Что делает |
|---|---|
| `val state: StateFlow<S>` | единственный state экрана: `stateIn(WhileSubscribed(5 s))` поверх приватного `MutableStateFlow`; первая подписка запускает `onStart()` |
| `val sideEffect: Flow<E>` | one-shot эффекты: `Channel(BUFFERED)` + `receiveAsFlow()` (как `FAN_OUT` в Orbit) — без подписчика копятся, каждый получает один сборщик; экран без эффектов — `E = Nothing` |
| `protected open suspend fun IntentScope.onStart()` | стартовая работа вместо `init`: один раз за жизнь VM, при первой подписке UI на `state`, отдельной корутиной (цепочку state не держит) |
| `protected val currentState: S` | текущее значение state для чтения в интентах |
| `protected inner class IntentScope : CoroutineScope` | получатель лямбд `intent`/`launchReplacing`/`launchOnDevice` и `onStart()` (как `Syntax` в Orbit); вне него `reduce`/`postSideEffect` не компилируются |
| `IntentScope.reduce(transform: S.() -> S)` | единственный способ изменить state (атомарный `update`) |
| `IntentScope.postSideEffect(effect: E)` | эффект в `sideEffect` (`trySend`, не блокирует; переполнение буфера — в лог) |
| `protected fun intent(block: suspend IntentScope.() -> Unit): Job` / `intent(context, block)` | запуск корутины с `CoroutineExceptionHandler` базы (необработанное исключение — в лог, не падение) — единственный способ запустить корутину VM, кроме `launchReplacing` |
| `protected suspend fun <T> subIntent(block: suspend IntentScope.() -> T): T` | хелпер интента (как `subIntent` в Orbit): `IntentScope` в текущей корутине, без новой; `launch` внутри — ребёнок вызывающей корутины, не ждётся; возвращает результат блока |
| `protected fun <T> Flow<T>.collectIntoState(reducer: S.(T) -> S): Job` | привязка потока к state: каждое значение — через `reduce`; собирается, только пока экран подписан на `state` (с тем же запасом 5 с), и перезапускается при возврате |
| `protected fun <T> Flow<Response<T>>.collectIntoState(onError, reducer): Job` | то же для `Flow<Response<T>>`; `onError` обязателен (без него вызов не компилируется — перегрузка `@Deprecated(ERROR)`) |
| `protected fun <T> select(selector: S.() -> T): Flow<T>` | часть state как вход подписки (`distinctUntilChanged`): `select { query }.flatMapLatest { … }.collectIntoState { … }` |
| `protected fun reload()` | переподписывает все привязки `collectIntoState` («Повторить» для реактивных источников) |
| `protected fun launchReplacing(key, context, block): Job` / `cancelLaunched(key)` | одна корутина на ключ, живёт с VM (не с подпиской): новый запуск отменяет прежний с тем же ключом; замена полю `var job: Job?` |
| `protected suspend fun repeatOnSubscription(block)` | работа без state на время подписки экрана (сброс разового состояния при возврате, поток событий → `postSideEffect`) |
| `protected suspend fun executeWithProceedLock(needSkipAnother = true, block)` | взаимоисключение под общим mutex VM: `true` — дубль пропускается (двойной клик), `false` — выполняется следом; от отмены не защищает |
| `protected val logger` | Kermit-логгер с тегом по имени класса; свой `Logger.withTag` не создавать |

Пример — `AppSettingsViewModel` (`feature/app-settings`):

```kotlin
internal class AppSettingsViewModel(
    private val localeStore: AppLocaleStore,
    private val configurationLanguages: ConfigurationLanguageStore,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<AppSettingsState, Nothing>(AppSettingsState(current = localeStore.current())) {

    override suspend fun IntentScope.onStart() {
        val language = withContext(dispatchers.io) { configurationLanguages.current() }
        reduce { copy(configurationLanguage = language) }
    }

    fun onLocaleSelect(locale: AppLocale) {
        intent { reduce { copy(selected = locale) } }      // UI-флаги — поля state, не отдельные MutableStateFlow
    }

    fun onConfigurationLanguageSelect(language: ConfigurationLanguage) {
        intent { executeWithProceedLock(needSkipAnother = false) { saveConfigurationLanguage(language) } }
    }
}
```

Правила (все с проверкой, [`code-quality.md`](code-quality.md), «Правило → проверка»):

- **Публичный API — только `state`, `sideEffect` и интенты** (detekt `ViewModelPublicApi`). Интент — `fun onXxx(...)`
  с блочным телом и результатом `Unit`, не `suspend`: у `fun onSave() = intent { }` выведен `Job`, поэтому
  `fun onSave() { intent { … } }`. Нельзя: публичные `val`/`var` (включая параметры конструктора), функции-запросы
  (`fun onBackRequest(): Boolean`) — решение VM отдаёт полем state или эффектом (`onBack()` ставит диалог или шлёт
  `Close`). `internal` считается публичным, `override` не проверяются.
- **State — один, и всё состояние VM — в нём.** Своих `MutableStateFlow`/`MutableSharedFlow`/`Channel`/`mutableStateOf`
  у VM нет (Konsist `view-model-owns-no-flows`): вход подписки (запрос поиска, фильтр, выбранный элемент) — поле state +
  `select { }`; «Повторить» подписки — `reload()`, загрузки — перезапуск того же `launchReplacing(key)`.
- **Нет `var`-свойств и изменяемых коллекций** (detekt `ViewModelMutableProperty`, включая `private`/`lateinit`,
  вложенные классы и конструктор): служебное между интентами (фаза процесса, номер попытки, снимок для сравнения) —
  поле state; корутина «перезапустить/отменить» — `launchReplacing(key)`/`cancelLaunched(key)`.
- **Без `init { }`** (detekt `ViewModelInitBlock`) — стартовая работа в `onStart()`, синхронный посев — в начальном
  state (`BaseViewModel(initialState)`, функция от параметров конструктора: `VehicleSelectRequest.initialState()`).
- **Корутины — `intent`/`launchReplacing`**, не `viewModelScope.launch`/`async`/`flow.launchIn(viewModelScope)`
  (detekt `ViewModelScopeLaunch`). Контекст — параметром: `intent(dispatchers.io) { }`.
- **`reduce`/`postSideEffect` — только внутри `intent { }`** (компилятор: это члены `IntentScope`; detekt
  `ReduceOutsideIntent` — на обёртки в обход): даже синхронный интент — `fun onInput(text: String) { intent { reduce
  { copy(text = text) } } }`. Хелпер, меняющий state, — `private suspend fun load() = subIntent { … }` (не
  расширение `IntentScope.`, кроме `override suspend fun IntentScope.onStart()`), вызывается из интента; параметр-лямбда
  с получателем `IntentScope` (`work(block: suspend IntentScope.() -> Unit)`) допустим. Проверки и ранние выходы — тоже внутри
  (`return@intent`): на `Main.immediate` тело до первой приостановки выполняется синхронно, guard
  `reduce { copy(busy = true) }` по-прежнему ставится до следующего тапа. В тестах с `StandardTestDispatcher` в Main
  после интента нужен `runCurrent()`.
- **`safeRun`/`safeResponseRun` во VM нет** (detekt `SafeRunInViewModel`): бросающий код оборачивает репозиторий, VM
  ветвится по `Response`.
- **Производные поля считаются в том же `reduce`, что и их входы** (`copy(order = order).withLists()`), иначе на кадр
  state расходится сам с собой. Чистые переходы state больших VM — в файлах рядом (`*Transitions.kt`, `*Machine.kt`),
  они тестируются без VM.
- **Файл VM содержит только VM** (detekt `ViewModelFileContents`, см. «Файлы экрана»); private-хелперы — вложенными
  private-типами или top-level функциями в отдельном файле.
- **Конструктор — не больше 12 параметров** (Konsist `view-model-constructor`): разросшуюся VM делим по поведению.

#### Зависимости ViewModel

Параметры конструктора VM (и аргументы их дженериков) — только из разрешающего списка (Konsist
`view-model-dependencies`): domain фич (`*Repository`, `*UseCase`, доменные сервисы и модели), presentation
(аргументы маршрута, порты `presentation/platform`), `core`: `AppDispatchers` (диспетчер выбирает VM, «Диспетчеры»),
`AppForegroundProvider`, `PermissionsProvider`, `AppLocaleStore`, `ConfigurationLanguageStore`, `PaneWidthStore`,
`TextFilePicker`/`TextFileSaver`/`ImagePicker`/`HtmlPdfRenderer`/`PdfPageRasterizer`/`BinaryFileExporter` (`core/files`), `CustomThemeStore` (`core/ui`, экран «Оформление»), `Clock`/`TimeZoneSource` («Источник времени»); Kotlin: примитивы, коллекции, `TimeSource`; `Logger`. Нельзя: data,
типы SDK, `Flow`, `CoroutineScope`, функциональные типы. Новый тип в списке — правкой `ArchitectureTest` вместе с этим разделом.

#### `BaseDeviceViewModel<S, E>`

`BaseDeviceViewModel(deviceSession, initialState)` (`feature/device/presentation-core`) добавляет работу с блоком:
`connectedDevice`/`connectedDeviceName` (потоки для `collectIntoState`), `currentDevice` (снимок), `launchOnDevice { }`
(без связи не стартует, при обрыве отменяется) и `launchOnDevice(key) { }` (то же с заменой по ключу, отмена —
`cancelLaunched(key)`), `onEachDeviceReady { }`/`onDeviceLost { }` — из `onStart()`, живут с VM. Свои
`deviceSession.state.map { … Ready … }` не писать. Пример — `CloneViewModel` (`feature/device/autostart`):

```kotlin
override suspend fun IntentScope.onStart() {
    onEachDeviceReady(::onDeviceReady)                      // на каждое (пере)подключение
    onDeviceLost(::onConnectionLost)                        // процесс уже отменён — вернуть экран на старт
    pinCooldown.lockedFor(select { deviceId.takeIf { dialog is CloneDialog.Pin } })
        .collectIntoState { lock -> withPinLock(lock) }
}

fun onExitConfirm() {
    intent {
        if (currentState.locked) return@intent
        cancelLaunched(PROCEDURE)
        reduce { copy(phase = ClonePhase.Start(), dialog = null) }
    }
}

private fun run(pinTries: Int = 0) {
    launchOnDevice(PROCEDURE) {                             // процесс живёт с VM, а не с подпиской экрана
        reduce { copy(phase = ClonePhase.Running(CloneStage.PREPARING)) }
        repository.run().transformWhile { … }.collect { event -> reduce { … } }
    }
}
```

### Жизненный цикл ViewModel и сессии

Каждый `NavDisplay` подключает `rememberViewModelStoreNavEntryDecorator()` (см. `docs/navigation.md`), поэтому
`koinViewModel()` отдаёт **per-entry** инстанс: VM живёт ровно столько, сколько запись бэкстека. Новый вход на экран =
новый инстанс = чистое состояние.

**Время жизни работы = место, где она живёт:**

| Сколько должна жить работа | Где | Пример |
|---|---|---|
| Пока экран подписан на state (поллинг, подписка на источник, «свежесть» при возврате) | `collectIntoState` / `intent { repeatOnSubscription { … } }` в `onStart` | поллинг чата в `MainScreenViewModel`, отсчёт PIN `lockedFor(...)` |
| Один раз за жизнь VM (первичная загрузка) | тело `onStart()` | `MainScreenViewModel.initialRefresh()` |
| Всю жизнь VM, с повтором/заменой (загрузка с «Повторить», процесс, сессия) | `launchReplacing(key)` из `onStart`/интента | `CloudListViewModel.load()`, сессия `SupportChatScreenViewModel` |
| Работа с блоком, отменяемая обрывом связи | `launchOnDevice(key)` | процесс `CloneViewModel`, `BypassLearningViewModel` |
| Дольше экрана (в т.ч. из `onCleared()`) | доменный сервис со скоупом приложения (`get(appScopeQualifier)`), VM зовёт его обычный (не `suspend`) метод | `DeviceSettingsSessionController.discardSession(source)` |

- **Процесс не зависит от подписки на state.** Прошивка, клонирование, обучение, сессия чата идут в `launchReplacing`/
  `launchOnDevice`, а не в `collectIntoState`: иначе через 5 с после ухода приложения в фон (`WhileSubscribed`)
  процесс отменится. VM-тест на это — «экран без подписчика 10 s — прогресс продолжается».
- **`CoroutineScope` в конструктор VM не передаётся** (`view-model-dependencies`): работа, переживающая экран, — в
  доменном сервисе со своим скоупом («Доменные сервисы»).
- **Не нужны:** `DisposableEffect` в nav-графе, методы `onScreenOpened()`/`onScreenDisposed()`, ручной сброс state при
  входе, поля-`Job`, самодельные барьеры («флаг + `while`», «`if (busy) return`» вне state) — есть `launchReplacing` и
  `executeWithProceedLock`. «Процесс уже идёт» — по фазе в state (`if (phase is Running) return`).
- **Возврат приложения на передний план** — порт `AppForegroundProvider` (`core/lifecycle`) в конструкторе (Konsist
  `foreground-usage`: `core.lifecycle` — только data, di и ViewModel, Koin-модуль `core.lifecycle.di` — где угодно):
  `foregroundProvider.foregroundReturns` собирается из `onStart` (`ScanDeviceViewModel`). Compose-эффекты
  (`LifecycleResumeEffect`) для решений VM не используются.
- **Зависимости — в конструктор, не параметром интента.** Платформенный порт передаётся через конструктор
  (`parametersOf` из `composeApp`).
- **Не открывать/закрывать сессию в `onStart`/`onCompletion` потока.** `Flow.onStart` выполняется **до** подписки на
  upstream — hot-событие без replay, опубликованное во время открытия, потеряется; `onCompletion` при отмене вызывается
  в отменённом контексте — suspend-`close()` сразу бросит `CancellationException`. Закрытие при уходе — `finally` в
  корутине сессии или метод репозитория со скоупом приложения.

### LCEState — ViewModel + Compose

`LCEState<T, E>` (`core/ui/lce`): `None` / `Loading` / `Content(content, isUpdating, updateError)` / `Error(error)`.
`T` — контент, `E` — **ошибка фичи** (см. «Ошибки фичи»). `None`/`Loading` — `data object`.

- `Content.isUpdating` — поверх показанного контента идёт фоновая операция (догрузка страницы, обновление);
  `Content.updateError` — она не удалась, контент остаётся (баннер с «Повторить» вместо экрана ошибки).
  Пустой `Content` — «данных нет» (empty-state), а не отдельный подтип.
- `Response` → `LCEState`: `response.asLCEState(XxxError::from)`; ошибка формы сбрасывается при вводе —
  `state.dismissError()`. Своих `Loading/Ready/Failed`-иерархий в фичах не заводим.

```kotlin
// CloudListViewModel (feature/configurator-ui): загрузка живёт с VM, «Повторить» перезапускает тот же ключ
override suspend fun IntentScope.onStart() {
    load()
}

fun onRetry() {
    load()
}

private fun load() {
    launchReplacing(LOAD_KEY) {
        reduce { copy(list = LCEState.Loading).withLists() }
        val response = withContext(dispatchers.io) { cloud.byCar(carId, deviceName) }
        reduce { copy(list = response.asLCEState(ConfiguratorError::from)).withLists() }
    }
}

// Compose UI (схема) — исчерпывающий when, ошибка → локализованная строка, «Повторить» → интент
when (val list = state.list) {
    LCEState.None, LCEState.Loading -> CmpCircularProgressIndicator()
    is LCEState.Content -> CloudList(list.content)
    is LCEState.Error -> ErrorBox(list.error, onRetry = viewModel::onRetry)
}
```

### State экрана: несколько источников

Когда экрану нужно несколько независимых асинхронных данных, каждое — отдельный `LCEState` внутри общего State
(тип ошибки — один на фичу), а загрузки идут параллельно:

- **Загрузка по запросу** — каждое поле своей корутиной (`intent`/`launchReplacing`) или `async` внутри одной
  (`MainScreenViewModel.initialRefresh()`: профиль, функции и коммерческие данные — `awaitAll`, скелетон снимается в
  `finally`). Зависимый запрос — последовательно в одной корутине (`currentState.profile.content?.let { … }`).
- **Реактивные источники** — привязка в `onStart`; входы из state — `select`, маппинг — до `flowOn(dispatchers.default)`:

```kotlin
// FirmwareSystemsViewModel (feature/device/firmware): строки списка пересчитываются в фоне из полей state
override suspend fun IntentScope.onStart() {
    combine(select { systems }, select { query }, select { expanded }, select { order }, ::systemRows)
        .flowOn(dispatchers.default)
        .collectIntoState { content -> copy(content = content) }
    executeWithProceedLock { load() }
}
```

- Независимые источники, которые пишут в разные поля, — отдельные привязки, а не один `combine`.
- Источник для `combine` без мгновенного первого значения (сеть, поллинг) — дополнять `.onStart { emit(<initial>) }`,
  иначе `combine` не эмитит, пока не ответят все источники.
- **Поток, отдающий state целиком (`{ mapped -> mapped }`), — единственный источник state.** Иначе привязка
  копирует только свои поля (`copy(content = content)`), чтобы следующее значение не затёрло UI-флаги и свежий ввод.
- **Поллинг не держать ручными `Job` во VM.** Репозиторий отдаёт `Flow<Response<T>>` (см. «Data — репозитории»), VM
  привязывает его к state — база сама остановит сбор без подписчика и запустит заново при возврате.
- Свой `stateIn(…)` во VM не нужен: state даёт база. `SharingStarted.Lazily` — только для app-scoped держателей
  состояния (доменных сервисов, репозиториев), не для экранов; `Eagerly` в инициализаторе свойства запрещён
  (`no-constructor-side-effects`) — работа без подписчиков идёт из `start()` сервиса.

### Сервисы — только во ViewModel

Composable не берёт сервисы из DI и не держит их ключом эффекта: репозитории, менеджеры, контроллеры, запускатели,
сессии, провайдеры, хранилища, источники данных, use case (`koinInject<SupportChatRepository>()`,
`LaunchedEffect(repository) { repository.session().collect { … } }` — запрещено). Подписки, запуск и чтение — во
ViewModel (`koinViewModel`, `onStart()`), UI получает state и вызывает интенты. Платформенное (уведомления ОС,
пикеры) — порт `presentation/platform`, его создаёт composable и передаёт во VM через `parametersOf`; работу с ним
ведёт VM. Так сделаны запуск приложения (`AppStartViewModel`, `core/ui`) и уведомления чата на desktop
(`SupportChatNotificationsViewModel` + порт `SupportChatNotifier`). Проверка — detekt `ServiceInComposable`.

### Запуск работы на старте экрана — `onStart`, а не `LaunchedEffect`

Первичную загрузку данных экрана НЕ запускать из UI (`LaunchedEffect(Unit) { vm.load() }` — запрещено): такой запуск
завязан на композицию, а не на подписку на state, требует метода-интента «для старта» во VM и guard-флагов от
повторных вызовов. Триггер «экран начал слушать state» уже есть — подписка на `StateFlow`; база вешает на него
`onStart()` (один раз за жизнь VM).

```kotlin
// MainScreenViewModel (feature/main)
override suspend fun IntentScope.onStart() {
    // Поллинг чата живёт, пока экран подписан на state: при возврате перезапускается с None.
    mainRepository.observeChatAvailability()
        .map { response -> response.asLCEState(MainError::from) }
        .flowOn(dispatchers.io)
        .onStart { emit(LCEState.None) }
        .collectIntoState { chatAvailability -> copy(chatAvailabilityState = chatAvailability).withActions() }
    intent { repeatOnSubscription { resetLogoutTransientState() } }       // на каждый возврат на экран
    initialRefresh()                                                       // один раз за жизнь VM
}
```

- **Не `init {}`** — он работает при создании VM, а не при показе экрана: VM, созданная без подписчика (быстрая
  навигация, превью), уже грузит данные.
- **Не голый `onStart { load() }` на цепочке потока** — он блокирует цепочку до конца загрузки (`Loading` экран не
  увидит) и повторяется на каждый перезапуск `WhileSubscribed`.
- **`onStart()` стартует на Main, dispatcher переключает вызываемый код**: IO — `withContext(dispatchers.io)`, маппинг —
  `dispatchers.default`, подписка — `flowOn` на холодном upstream. Привязки `collectIntoState` не подвешивают `onStart` —
  у каждой своя корутина; долгую разовую работу — после привязок или в `intent { }`.
- **Начальный шаг/режим экрана как чистая функция от входа/платформы — не «работа», а начальное state**
  (`BaseViewModel(initialState)`); реактивные источники, завязанные на шаг, строятся через `select { step }`.
- Composition side-effects (`LaunchedEffect`/`DisposableEffect`) допустимы **только** для привязки платформенных
  объектов, живущих в композиции (`BindEffect` для `PermissionsController`), и проброса UI-события в интент VM
  (`LaunchedEffect(isDark) { vm.onSystemDarkChanged(isDark) }`). Переименование init-хука (`start()`, `prepareX()`) не
  делает его легальным.
- **Тесты VM подписываются на state, как экран:** `backgroundScope.launch { vm.state.collect {} }`
  (`Dispatchers.setMain(UnconfinedTestDispatcher())`), иначе `onStart` не запустится и `state.value` останется
  начальным. Процесс, который должен жить без подписчика, проверяется отпиской и `advanceTimeBy` дольше 5 с.

### Корректность state

Типовые дефекты, которые проходят компиляцию и тесты «зелёного пути», но ломают UX.

- **У каждого флага загрузки — гарантированный сброс.** Если `isLoading = true` снимается только
  «по приходу события» — он залипнет, когда событие не придёт. Сброс — в `finally`/по результату
  вызова с таймаутом (см. «Domain»: гарантированный финал), плюс при переходе в offline и при
  закрытии сессии.
- **Ошибка первой загрузки ≠ пустой экран.** Если загрузка упала, а state выглядит как «данных
  нет», пользователь увидит empty-state («Напишите нам») вместо ошибки. Данные — `LCEState<T, E>`;
  отличать «первую» ошибку от ошибки пагинации флагом не нужно — это следует из контента:

```kotlin
// Ошибка истории
if (currentState.history is LCEState.Content) {
    postSideEffect(SupportChatEffect.HistoryLoadFailed)           // лента остаётся, тост
} else {
    reduce { copy(history = LCEState.Error(ChatError.from(e))) }  // полноэкранная ошибка + Retry
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
- **Check-then-act на `currentState` не атомарен между корутинами.** `if (currentState.busy) return` + отдельный
  `reduce` после `suspend`-вызова пропускают два быстрых нажатия. Проверка и запись — внутри одного `reduce {}`
  (синхронно в интенте, до запуска корутины) либо через `executeWithProceedLock`.
- **Сетевые вызовы из частых UI-событий — пачкой.** Видимость элементов, ввод текста, скролл — не
  повод для запроса на каждое событие: UI отдаёт пачку (`onMessagesVisible(ids)`), VM батчит с
  debounce, если контракт принимает список.
- **Одинаковые трансформации state — приватным хелпером** (`updateMessageStatus(id, status)`),
  а не пятью копиями `map { if (it.id == x) it.copy(...) else it }`.
- **Неочевидный guard — с комментарием-обоснованием** («игнорируем повторные ошибки: на iOS они
  приходят потоком ~кГц»), иначе его «починят» при рефакторинге.

### One-shot события (Effects)

Навигация, snackbar'ы и прочие одноразовые события из ViewModel в UI — поток `sideEffect` базы: VM шлёт событие
`postSideEffect(...)`, экран собирает его `collectSideEffect { }` (`core/ui`, `ru.alarmtrade.specmp.core.ui.effect`).
Своих `MutableSharedFlow`/`Channel` для эффектов во VM нет (Konsist `view-model-owns-no-flows`); руками
`LaunchedEffect` + `repeatOnLifecycle` в фичах не пишем.

```kotlin
// MainScreenEffect.kt
sealed interface MainScreenEffect {
    data class Navigate(val target: MainNavigationTarget) : MainScreenEffect
}

// MainScreenViewModel.kt
fun onSettingsClicked() {
    intent {
        reduce { copy(isMenuOpen = false) }
        postSideEffect(MainScreenEffect.Navigate(MainNavigationTarget.Settings))
    }
}

// MainScreen.kt
viewModel.sideEffect.collectSideEffect { effect ->
    when (effect) {
        is MainScreenEffect.Navigate -> onNavigateToFeature(effect.target)
    }
}

// CheckStateScreen.kt — ожидание пользователя (снекбар) не держит обработчик
val scope = rememberCoroutineScope()
viewModel.sideEffect.collectSideEffect { effect ->
    when (effect) {
        is CheckStateEffect.CommandFinished -> scope.launch { showResult(snackbar, effect, viewModel::onRetry) }
        CheckStateEffect.Close -> leave()
    }
}
```

**Правила:**

- Тип события — `sealed interface XxxEffect` (или `XxxSideEffect`) в своём файле; даже одно событие — `data object`
  (`Close`), а не голый `Unit`/`Boolean`. Экран без эффектов — `E = Nothing`.
- Доставка — как `FAN_OUT` в Orbit MVI: `Channel(BUFFERED)` + `receiveAsFlow()`. Эффект, отправленный без
  подписчика (из `onStart` до отрисовки, пока экран ниже `STARTED`), ждёт в буфере и доставляется при подписке;
  каждый эффект получает ровно один сборщик, повторов после пересоздания экрана нет. `postSideEffect` — `trySend`:
  не блокирует, при переполнении буфера (64) эффект отбрасывается с записью в лог.
- Обработчик в `collectSideEffect` выполняется последовательно (как `collectSideEffect` Orbit): пока он висит,
  следующие эффекты ждут. Поэтому обработчик короткий: навигация, закрытие, вызов интента — сразу, а всё, что ждёт UI
  или пользователя (`showSnackbar`, `showResult`, системный диалог сохранения/открытия файла), — в
  `rememberCoroutineScope().launch { }` экрана. Логика «по действию снекбара — навигация/повтор» остаётся внутри этой
  корутины. Прямой `showSnackbar`/`showResult` в лямбде `collectSideEffect` ловит detekt `SideEffectAwaitsUi`. Тосты
  при этом всё равно идут по очереди — их упорядочивает `CmpSnackbarHostState`.
- `collectSideEffect` собирает с `repeatOnLifecycle(STARTED)` и `rememberUpdatedState` — подписка приостанавливается в
  фоне, пересоздание лямбды не перезапускает сбор.
- Эффект, полученный, но не обработанный до ухода экрана в фон (отмена посреди обработчика), теряется; результат,
  который нельзя потерять, — поле state.
- На эффект подписывается ровно один экран.

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
- **Domain-типы стабильны через конфиг компилятора** (`config/compose/stability.conf`, пакеты
  `feature.**.domain.model.*`): модули domain собираются без Compose, и без конфига их классы
  считались бы нестабильными. Поэтому domain-модель можно передавать параметром composable как есть;
  условие — её транзитивная неизменяемость (см. «Domain»).
- **Нестабильны и требуют решения:** SDK-типы (маппинг в domain/presentation-модель), коллекции
  параметром composable (`ImmutableList` или поле `@Immutable`-state), `var`-поля.
- **Идентичность экземпляров важнее аннотаций:** state обновляется через `copy` поля, неизменённые
  элементы переиспользуются (образец — `SettingsSection.reusing(old)`), маппинг/сортировка — во VM, а не
  в теле composable. Подробно — `docs/compose-best-prac.md`, §3 «Стабильность».
- **Сначала код, конфиг — крайняя мера.** Нестабильность решается идентичностью экземпляров,
  `@Immutable` presentation-моделью, `ImmutableList`; запись в `stability.conf` — только для неизменяемого
  типа из модуля без Compose, когда кодом не решается (`docs/compose-best-prac.md`, §3.5).
- Проверка — `.agents/skills/perf-audit/scripts/compose-reports.sh <модули…>`, правка стабильности —
  с отчётом до/после.

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

**Общие ошибки — `AppError` (`core/common`)**, одинаковые для всех фич и с общим текстом:

- `ApiError` — `NoConnection` / `Timeout` / `ServerError`;
- `PlatformError` — `BluetoothOff` / `BluetoothUnsupported` / `LocationOff` / `PermissionDenied` /
  `DeviceNotFound` / `DeviceDisconnected`.

Их проставляет граница data-слоя: `toResponse()` (`core/specialist`) кладёт в `Response.Error.exception`
`AppException(error, cause)`; исключение, пойманное не из `Response`, приводится через `Throwable.sdkAppError()`.
Наследоваться от `AppError` ошибка фичи не может (sealed — только в своём модуле), поэтому **оборачивает** её:

```kotlin
// feature/<name>/presentation/model
internal sealed interface SettingsError {
    data class Common(val error: AppError) : SettingsError   // общий текст: error.message()
    data object LoadFailed : SettingsError                   // свои случаи фичи
    data object Mismatch : SettingsError

    companion object {
        fun from(error: Throwable): SettingsError = error.appError?.let(::Common) ?: LoadFailed
    }
}

// UI — исчерпывающий when, общий случай — в AppError.message() из core/ui
@Composable
internal fun SettingsError.text(loadFailed: StringResource): String = when (this) {
    is SettingsError.Common -> error.message()
    SettingsError.LoadFailed -> stringResource(loadFailed)
    SettingsError.Mismatch -> stringResource(Strings.string.settings_common_error_mismatch)
}

// state: LCEState<SettingsSection, SettingsError>
```

Строк «нет интернета», «Bluetooth выключен» и т.п. фича не заводит — они общие (`common_error_*` в `core/localization`).

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

**Ошибки из ViewModel — только типизированные**, не строковые коды. Ошибка-строка
(`"required_fields"`), которую UI сравнивает с собственной константой, —
запрещена (поэтому `LCEState.Error` и держит ошибку фичи, а не текст): контракт держится на двух независимых литералах в разных файлах и при рассинхронизации
ломается молча (маппинг в текст — `docs/localization.md`, правило 4).

```kotlin
// ❌ Плохо — строковый код как контракт VM ↔ UI
// ViewModel:
reduce { copy(registrationError = "required_fields") }
// UI (другой файл, своя копия константы):
if (state.error == REQUIRED_FIELDS_ERROR) { /* ... */ }

// ✅ Хорошо — типизированная ошибка в state
enum class RegistrationError { RequiredFields, RequestFailed }
reduce { copy(error = RegistrationError.RequiredFields) }
// UI:
val text = when (error) {
    RegistrationError.RequiredFields -> stringResource(Strings.string.login_common_error_required_fields)
    RegistrationError.RequestFailed -> stringResource(Strings.string.login_common_error_registration_failed)
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

Инициализация в composition root через `KoinApplication { modules(...) }` (не `startKoin`): `App()` в `composeApp`,
`FlasherApp()` во `flasherApp`; список модулей корня — функция в его пакете `di`.

```kotlin
// composeApp/.../App.kt
KoinApplication(application = {
    modules(platformModules + appKoinModules(versionName, versionCode)) // di/AppKoinModules.kt
}) {
    AppTheme(onThemeChange) { AppNavigation() }
}

// DI-модуль в feature/*/di/
val myFeatureModule = module {
    singleOf(::SpecialistMyRepository) bind MyRepository::class
    viewModelOf(::MyViewModel)
}

// В Compose-экранах фичи
val viewModel = koinViewModel<MyViewModel>()

// В корне композиции (composeApp/flasherApp) — и дальше параметром
FirmwareBootGraph(onExit = onExit, onOpenSupportChat = {}, archivePicker = koinInject())
```

**Правила:**

- **`koinInject` — только в корнях композиции `composeApp`/`flasherApp`** (Konsist `composable-injection`): composable
  фичи получает зависимость параметром из корня, через ViewModel (`koinViewModel` разрешён) или `CompositionLocal`,
  заданный в корне. Платформенному `remember*` фичи нужное для работы (app-scope, диспетчер) передаёт корень
  (`rememberFirmwareFilePicker(appScope, ioDispatcher)`).
- **Список модулей корня — функция в `di`** (`appKoinModules(versionName, versionCode)` в `composeApp`,
  `flasherKoinModules(…)` во `flasherApp`; платформенные — `rememberPlatformKoinModules()` отдельно). Граф проверяет
  JVM-тест корня (`AppKoinModulesTest`, `FlasherKoinModulesTest`: koin-test `Module.verify`): у каждого параметра
  конструктора определения есть определение. Платформенные типы и значения, собранные в лямбдах модулей, —
  `extraTypes`; параметры ViewModel из `parametersOf` — `injections` (`definition<Vm>(Param::class)`, internal-классы
  фич — по имени рефлексией). Новый модуль/параметр `parametersOf` — правка списка или теста в том же изменении.
- `feature/*/di` регистрирует только зависимости конкретной фичи: репозитории, `*DataSource`, доменные сервисы,
  ViewModel (мапперы — top-level функции, в DI не регистрируются).
- **Koin-модуль — `val <name>Module` в пакете `di`** (Konsist `koin-module`), включая `core/*`
  (`core/lifecycle/di/AppLifecycleModule.kt`, `core/specialist/di/SpecialistModule.kt`).
- **Реализацию (`*Impl`, `Specialist*Repository`) создаёт только Koin-модуль** (Konsist `koin-wiring`): `Foo(…)` или
  `::Foo` вне пакета `di` — ручная сборка графа в обход DI; остальной код получает абстракцию конструктором.
- **Один Koin-модуль на gradle-модуль, в его корневом пакете `di/`** (`feature/<name>/…/<name>/di/<Name>Module.kt`). Не заводить `ui/di`, `data/di` внутри модуля: ViewModel и репозитории одного gradle-модуля регистрируются вместе. Отдельный UI-модуль Koin бывает только у отдельного gradle-модуля `<name>-ui`.
- App-level инфраструктура и SDK-контейнеры регистрируются в `core/*`/`composeApp`,
  не в фиче — см. раздел «Core».
- **Koin как сервис-локатор** (`KoinComponent`/`inject`/`get`, `GlobalContext`, `KoinPlatformTools`…) — только пакеты `di`,
  composition roots, платформенные обёртки и Android-компоненты, которые создаёт система (Konsist `no-service-locator`);
  остальной код получает зависимости конструктором.
- Use case — только для оркестрации ≥ 2 зависимостей (см. «Use case»); один вызов репозитория VM делает напрямую.

Чеклист при добавлении фичи:

1. Добавить зависимость модуля в `composeApp/build.gradle.kts` (фича устройства, нужная прошивальщику, — и во
   `flasherApp/build.gradle.kts`).
2. Добавить Koin-модуль в список корня — `appKoinModules(…)` (`composeApp/.../di/AppKoinModules.kt`; во `flasherApp` —
   `flasherKoinModules(…)`) и прогнать `AppKoinModulesTest`/`FlasherKoinModulesTest` (`jvmTest` корня).
3. Доменный сервис со `start()` — слот в `di/AppServicesModule.kt` (и `di/FlasherServicesModule.kt`), см. «Доменные сервисы».
4. Добавить маршруты в навигацию в `AppNavigation.kt`.
