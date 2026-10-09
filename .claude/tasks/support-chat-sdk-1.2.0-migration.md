# Миграция чата техподдержки на Specialist SDK 1.2.0

**Статус:** реализовано 2026-10-09 (ответы и отклонения — внизу); не проверен на устройстве только логаут/вход.
**Ветка:** `feature/support-chart`.
**Основание:** миграция SDK — `alarmtrade-common3/.claude/issues/specialist-sdk-chat-fixes-migration.md`
(в репозитории SDK файл `specialist/MIGRATION-chat-1.2.0.md` удалён); наш запрос к SDK —
`specialist-sdk-chat-fixes.md`.
**Заменяет:** этап 7 из `support-chat-session-rework.md` (обход `closeChat` перед `openChat` не нужен —
P0-1 исправлен в SDK).

Статусы: [ ] не начато · [x] готово · [~] частично

---

## Уже сделано (без правок кода фичи)

- [x] `specialist` 1.2.0 опубликован в mavenLocal (`deploySpecLocal.sh`), общие модули 2.0.93 (`core`
      и др.) там уже были.
- [x] `gradle/libs.versions.toml`: `alarmtrade-common-specialist = "1.2.0"`. `core` 2.0.93 приходит
      транзитивно; `mavenLocal()` в репозиториях уже первый.
- [x] `:androidApp:assembleDebug`, `:feature:support-chat:jvmTest` — зелёные: старые use case-ы
      deprecated, но работают.

## Что меняется в поведении SDK (важно для плана)

- `ChatConnection` — единственный владелец соединения: заявки (`acquire()` / `session()`), `Ready` =
  `subscribe_success`, переподключение с backoff (1 → 30 с), `WaitingForNetwork`, `AuthFailed`
  (после повторного входа при активной заявке подключается сам), логаут → `Idle`, старые заявки мертвы.
- Use case-ы: `chatSessionFlowUseCase` (поток состояний, заявка живёт пока собирается),
  `loadChatMessagesUseCase(LoadChatMessagesRequest(cursor, pageSize))` → `ChatMessagesPage(messages,
  hasMore, total, nextCursor)`, `downloadChatFileUseCase(DownloadChatFileRequest(url))` (лимит 20 МБ).
- Команды вне `Ready` → `Response.Error(NetworkNotConnectedError)`.
- Тип чата по умолчанию `ChatType.Default` — `setChatTypeUseCase` для чата не нужен.

---

## Этап 1 — Хост: монитор сети и логирование

- [x] **Монитор сети — своя мультиплатформенная реализация в `core/network`, внедряется через Koin в корне.**
      Без монитора SDK опрашивает «сеть всегда есть» и не видит блокировку сети в фоне Android 15.
      - `core/network`: интерфейс `NetworkMonitor { val isAvailable: StateFlow<Boolean> }` и реализации
        `AndroidNetworkMonitor(context)` (`ConnectivityManager.registerDefaultNetworkCallback`, включая
        `onBlockedStatusChanged`), `IosNetworkMonitor` (`NWPathMonitor`). JVM — без реализации.
      - `composeApp` (корень DI): платформенный Koin-модуль `rememberPlatformModule()` (expect/actual):
        Android — `single<NetworkMonitor> { AndroidNetworkMonitor(applicationContext) }`, iOS —
        `IosNetworkMonitor()`, JVM — пустой. Подключается в `App.kt` рядом с остальными модулями.
      - `core/specialist`: перед созданием `SpecialistApp` — `getOrNull<NetworkMonitor>()` → адаптер к
        `ru.alarmtrade.common.core.configuration.NetworkMonitor` → `CommonConfigurator.networkMonitor`.
        Нет монитора (JVM) — дефолт SDK.
- [x] **Логирование SDK:** `SpecialistConfig.Network(logLevel = NetworkLogLevel.NONE, logWebSocketFrames = false)`
      всегда (все сборки).
- [x] Документация: `.ai/agents/core/network/AGENTS.md` (монитор сети),
      `.ai/agents/core/specialist/AGENTS.md` (монитор → `CommonConfigurator`, логирование `NONE`) →
      `.ai/generate.sh`.

## Этап 2 — Domain-контракт фичи

- [x] `SupportChatConnectionState` — прямое отображение `ChatConnectionState` SDK, без финала сессии:
      `Connecting`, `Ready`, `Reconnecting`, `WaitingForNetwork`, `AuthFailed`
      (`Idle` в фиче не наблюдается — пока собираем сессию, заявка есть). Убрать `Connected(profile)`,
      `Disconnected(reason)`.
- [x] Профиль — по-прежнему часть сессии, но отдельным событием: `SupportChatEvent.Profile(profile)`
      **или** поле в первом событии (см. вопрос 3). Нужен VM для `isMine` и маппингу событий.
- [x] `SupportChatEvent`: `Connection(state)`, `MessageReceived`, `MessagesRead`, `TypingChanged`.
      Удалить `HistoryPage`.
- [x] `SupportChatRepository`:
      - `fun session(): Flow<SupportChatEvent>` — заявка на соединение живёт, пока поток собирается;
        поток **не завершается** сам (обрывы и переподключения — внутри SDK, приходят как `Connection`);
      - `suspend fun loadHistoryPage(cursor: SupportChatHistoryCursor?): Response<SupportChatHistoryPage>`
        (`messages`, `nextCursor`; `null` — истории больше нет). Курсор — непрозрачная обёртка над
        `ChatCursor.token`;
      - `sendMessage` / `sendImage` / `sendDocument` / `markMessagesAsRead` / `downloadFile` — сигнатуры те
        же; ошибка «нет соединения» — `Response.Error` с доменным признаком
        `SupportChatNotConnectedException` (VM кладёт сообщение в очередь, а не в `Failed`);
      - `AuthCloseable` остаётся; `release()` — без вызовов SDK: логаут SDK сам переводит соединение
        в `Idle` и гасит заявки (вопрос 4).
- [x] Удалить `requestHistoryPage()`.

## Этап 3 — Репозиторий (`SpecialistSupportChatRepository`, пишется заново, тонкий)

- [x] Поля: только `logger`, маппер. Убрать `connectionMutex`, `closeChat`, `CompletableDeferred`,
      `UNDISPATCHED`, таймаут подписки 10 с, `safeFlowRun` вокруг событий, `setChatType`/`openChat`.
- [x] `session()` = `channelFlow`:
      1. профиль `accountRepository.currentProfile()` (ошибка — см. вопрос 3);
      2. `launch { chatEventsFlow.collect { map(profile)?.let(::send) } }` — `getChatEventsFlowUseCase`;
      3. `chatSessionFlowUseCase.execute().collect { send(Connection(it.toDomain())) }` — держит заявку.
      Отмена сбора → заявка освобождается в SDK.
- [x] `loadHistoryPage` → `loadChatMessagesUseCase(LoadChatMessagesRequest(cursor?.token, PAGE_SIZE))`,
      маппинг `ChatMessagesPage` → `SupportChatHistoryPage` (`nextCursor`), `PAGE_SIZE` константа (не
      менять между вызовами).
- [x] `downloadFile` → `downloadChatFileUseCase`; `NetworkFileTooLargeError` → прежняя доменная ошибка
      «слишком большой файл».
- [x] Ошибка `NetworkNotConnectedError` команд → `SupportChatNotConnectedException`.
- [x] Маппер событий: `Chat.Message`, `MarkMessageAsRead`, `Writing.*` — как сейчас; `MessagesPage` и
      `Connection.*`/`ChatRoom.*` больше не маппятся (состояние — из `chatSessionFlowUseCase`).

## Этап 4 — ViewModel: соединение

- [x] Убрать цикл сессий: `runSessions`, `collectSession`, `SessionOutcome`, backoff
      (`RECONNECT_DELAYS`), `reconnectNow`, `screenStarted`, `onScreenStarted` / `onScreenStopped`;
      в `SupportChatNavGraph` — `LifecycleEventEffect(ON_START/ON_STOP)`.
- [x] В `init`: `safeLaunch { repository.session().collect(::handleEvent) }` на `dispatchers.io`; уход с
      экрана = отмена `viewModelScope` = освобождение заявки. Фон: заявка держится, SDK ждёт сеть
      (`WaitingForNetwork`) — решение «ON_STOP держит сокет» сохраняется без нашего кода.
- [x] `handleConnection(state)`:
      - `Ready` → `connection = Online`, сброс `hasConnectionIssue`, перечитать первую страницу истории
        (этап 5), после неё — `retryPendingMessages()`;
      - уход из `Ready` (`Reconnecting` / `WaitingForNetwork` / `AuthFailed`) → `Offline`,
        `hasConnectionIssue = true`, `requeueUnconfirmed()`;
      - `AuthFailed` → `isChatAuthFailed = true` (плашка без спиннера); при следующем `Ready` — сброс.
- [x] Плашка: `Connecting` — до первого `Ready`; `Reconnecting` — `Reconnecting` и `WaitingForNetwork`
      (одна плашка «нет соединения · переподключение», без мигания); `AuthFailed` — как сейчас.
- [x] `onHistoryRetry`: при `Online` — перечитать историю; иначе ничего (переподключение — забота SDK).
- [x] Отправка: `SupportChatNotConnectedException` → `park` (как сейчас при потере связи), прочие
      ошибки → `fail`.

## Этап 5 — ViewModel: история

- [x] State: `historyCursor: SupportChatHistoryCursor?`, `hasMoreHistory`, `isLoadingMore`,
      `isInitialHistoryLoaded`. Удалить `isAwaitingHistoryPage`, `historyRequestId`, таймаут 15 с,
      `failHistoryPage`, `handleHistoryPage(event)`.
- [x] Первая страница на каждом `Ready`: отменить текущую загрузку (`historyJob?.cancel()`),
      `loadHistoryPage(null)`, слить с лентой (`withHistoryPage` + подтверждение оптимистичных),
      `historyCursor = page.nextCursor`, `hasMoreHistory = nextCursor != null`.
- [x] `onLoadMore`: если `Online`, `hasMoreHistory`, нет загрузки → `loadHistoryPage(historyCursor)`.
      Одна загрузка за раз — `historyJob`.
- [x] Ошибка загрузки: есть контент → тост; нет → `LCEState.Error` (полноэкранная с Retry), как сейчас.
      Финал гарантирует сам suspend-вызов (HTTP-таймаут), отдельный таймаут не нужен.
- [x] Дедупликация по id при слиянии — остаётся (новые сообщения сдвигают страницы, курсор SDK — номер
      страницы).

## Этап 6 — Чистка

- [x] `core/network`: удалить `FileDownloader` / `KtorFileDownloader` и биндинг в `networkModule`;
      зависимость `feature/support-chat` → `core:network` убрать (вопрос 5). `HttpClientFactory` оставить.
- [x] Фича: удалить SDK-обходы и комментарии про 1.1.76 (`hasMore` по непустой странице, 404 как
      пустой `Success`, `prepareUrl`).
- [x] `support-chat-session-rework.md`: этап 7 → «не нужен, закрыт SDK 1.2.0 (P0-1)».
- [x] `specialist-sdk-chat-fixes.md`: отметка «реализовано в 1.2.0».

## Этап 7 — Тесты

- [x] `FakeSupportChatRepository`: управляемый `session()` (эмит `Connection(state)` вручную, поток не
      завершается), `loadHistoryPage` с очередью ответов/ошибок и курсором, флаг «нет соединения» для
      отправки, счётчик активных сессий (заявок).
- [x] VM, соединение: первый `Ready` → `Online` + загрузка первой страницы; `Reconnecting` →
      `Offline` + плашка + `requeueUnconfirmed`; повторный `Ready` → перечитать первую страницу и
      отправить очередь; `WaitingForNetwork` = та же плашка; `AuthFailed` → плашка без попыток со
      стороны VM, следующий `Ready` её снимает; отмена `viewModelScope` завершает сбор `session()`.
- [x] VM, история: курсор передаётся дальше; `nextCursor == null` выключает пагинацию; одна загрузка
      за раз; `Ready` во время загрузки перезапускает первую страницу; ошибка → тост / `LCEState.Error`.
- [x] VM, отправка: `NotConnected` → `Pending`, прочая ошибка → `Failed`; очередь уходит после первой
      страницы на `Ready`.
- [x] Убрать тесты цикла сессий/backoff/`ON_STOP`/таймаута страницы; `runChatTest`-обёртка — оставить,
      если ещё нужна (бесконечного цикла больше нет).
- [x] Остальные тесты (timeline, outbox, маппер, UI-модели) — адаптировать без изменения смысла.

## Этап 8 — Проверка и документация

- [x] `:feature:support-chat:jvmTest`, `:composeApp:compileKotlinJvm`,
      `:composeApp:compileKotlinIosArm64`, финально `:androidApp:assembleDebug`.
- [~] Эмулятор (ручные проверки раздела 5 миграции, применимые к нам):
      1. вход в чат → `Ready`, история;
      2. выход/вход → сразу `Ready`, лишнего `wss` через ~2 с нет;
      3. Home / пикер > 5 с → `WaitingForNetwork` (нужен монитор этапа 1) → возврат → `Ready` сам;
      4. сеть off/on → плашка, ввод заблокирован, очередь уходит после `Ready`;
      5. прокрутка истории до конца → пагинация выключается без лишнего пустого запроса;
      6. скачивание/открытие старого файла;
      7. логаут/вход → чат подключается;
      8. logcat: нет `Authorization` и тел ответов (release-логирование), в debug — замаскирован.
- [x] `.ai/agents/feature/support-chat/AGENTS.md`: сессия = заявка SDK, состояния, история по курсору,
      переподключение — SDK; `docs/patterns.md` — правило про «ответ событием внутри сессии» больше не
      нужно для чата (оставить общее, убрать ссылку на чат) → `.ai/generate.sh`.

---

## Ответы (2026-10-09)

1. Монитор сети — отдельная мультиплатформенная реализация (Android и iOS), внедряется через Koin в
   корне приложения (`composeApp`), не в `Application`.
2. Логирование SDK — `NONE` всегда.
3. Профиль — событие в начале `session()`; ошибка → `LCEState.Error` с Retry (перезапуск сессии).
4. `AuthCloseable` у репозитория **не убирать** — будет использоваться и в других местах.
5. `FileDownloader` удалить.
6. mavenLocal — до публикации 1.2.0 в `git.pandora.pri`.

## Открытые вопросы (исходные)

1. **Где ставить монитор сети.** Предлагаю `Application` в `androidApp` (раз на процесс, до SDK).
   Альтернатива — `AppActivity.onCreate` с проверкой `networkMonitor == null` (без нового класса, но
   привязано к Activity).
2. **Признак debug/release для логирования SDK.** Своего флага сборки в проекте нет. Предлагаю
   передавать `isDebugBuild` хостом в `App(...)` (Android — `BuildConfig.DEBUG`, iOS — `#if DEBUG`,
   desktop — системное свойство) и класть в `specialistModule`. Альтернатива — `NONE` всегда.
3. **Профиль в сессии.** Ошибка `currentProfile()` раньше давала `Disconnected(OpenFailed)` и повтор.
   Теперь цикла нет. Предлагаю: профиль — событие `SupportChatEvent.Profile` в начале `session()`;
   при ошибке — `LCEState.Error` с Retry, Retry перезапускает сбор `session()` во VM.
4. **`release()` у репозитория.** Логаут SDK (`LogoutUseCaseWrap`) сам гасит соединение и заявки.
   Предлагаю убрать `AuthCloseable` у `SupportChatRepository` (outbox остаётся `AuthCloseable`).
5. **`FileDownloader` в `core/network`.** После миграции не используется. Удалить (предлагаю) или
   оставить как общую инфраструктуру?
6. **mavenLocal.** 1.2.0 пока только локально. Перед мержем — публикация в `git.pandora.pri` и проверка
   сборки без mavenLocal (CI/другие машины).

## Отклонения по ходу реализации

- Профиль в VM не нужен (маппинг «моё/чужое» — в репозитории), поэтому события `Profile` нет: ошибка
  профиля — состояние `SupportChatConnectionState.Unavailable` (финал потока `session()`), VM показывает
  полноэкранную ошибку, «Повторить» перезапускает сбор сессии.
- Ошибка `NetworkNotConnectedError` → `SupportChatNotConnectedException` (`domain/model/SupportChatHistoryPage.kt`).
- Размер страницы истории — 20 (`HISTORY_PAGE_SIZE`); повторная первая страница на `Ready` не сбрасывает курсор.
- События сессии собираются на main (`flowOn(io)` у репозитория) — `historyJob`/`sessionJob` main-confined.

## Результат проверки (2026-10-09)

- `:feature:support-chat:jvmTest` — 84 ✓; `:composeApp:compileKotlinJvm`, `:composeApp:compileKotlinIosArm64`,
  `:androidApp:assembleDebug` ✓.
- Эмулятор (Pixel, Android 16), SDK 1.2.0 из mavenLocal:
  1. вход в чат → онлайн, история ✓;
  2. выход → через ~10 с у процесса нет соединений (нет «воскресшего» сокета); повторный вход — онлайн < 2 с ✓;
  3. Home → через ~6 с `WaitingForNetwork` (монитор сети), сокетов в фоне нет; возврат → онлайн < 3 с ✓;
  4. сеть off → плашка, ввод заблокирован; on → онлайн через ~3 с ✓;
  5. прокрутка истории — старые страницы подгружаются ✓ (до конца 940 сообщений не листали);
  6. открытие старого PDF — скачан через SDK, открыт во внешнем viewer ✓;
  7. логаут/вход — **не проверено** (нужны учётные данные);
  8. logcat — нет `Authorization`/`Bearer`/HTTP-логов SDK ✓.
- Отправка сообщений на проде не проверялась (покрыто тестами VM), чтобы не засорять чат поддержки.

## Этап 9 — Баг (эмулятор 2026-10-09): текст из очереди «подтверждается» чужим сообщением и теряется — исправлено (апрув 2026-10-09)

**Воспроизведение.** Отправить картинку + текст «QA offline …» и сразу выключить сеть → оба `Pending` →
выйти из чата → включить сеть → войти. Картинка отправилась, текст исчез из ленты и из outbox, на
сервер не ушёл.

**Причина.** `SupportChatTimeline.indexOfEcho` (и для страницы истории, и для живого эха) после
сопоставления по содержимому сопоставляет **по форме** (`shapeMatches`). Для двух чисто текстовых
сообщений форма совпадает всегда. Серверное «QA doc caption» (14:17:39) попало в окно `CLOCK_SKEW` (2 мин)
восстановленного «QA offline» (14:19:14) и заменило его: оптимистичное считается подтверждённым,
VM удаляет запись outbox — сообщение не отправляется никогда. Баг до миграции (этапы 1–13).

**Фикс (`feature/support-chat`, только `domain/timeline/SupportChatTimeline.kt` + тест).**
- Сопоставление по форме — **только для вложений** (сервер может переименовать файл): текстовое
  оптимистичное подтверждается только серверным сообщением с тем же текстом.
- Тесты `SupportChatTimelineTest`: восстановленный текст не подтверждается другим моим текстом из
  истории в окне 2 мин; живое эхо другого текста не съедает ожидающий текст; переименованное вложение
  по-прежнему подтверждается по форме.
- Проверка: `:feature:support-chat:jvmTest`, `:androidApp:assembleDebug`, эмулятор — сценарий выше.

**Сделано:** `shapeMatches` — только вложения; 3 теста в `SupportChatTimelineTest` (история, живое эхо,
переименованное вложение); `jvmTest` 87 ✓; AGENTS обновлён; `:androidApp:assembleDebug` ✓. Эмулятор: «QA9 first» + (картинка + «QA9 queued» в очереди) → выход → сеть → вход: оба из очереди ушли, текст не потерян ✓.

**Не делаем (риск остаётся, зафиксировать в AGENTS):** вложения в пачке одного типа в окне 2 мин
сопоставляются по форме FIFO — при переименовании сервером это единственный способ.
