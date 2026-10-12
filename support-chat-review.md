# Ревью `feature/support-chat` — замечания и предложения по доработкам

**Модуль:** `feature/support-chat`
**Ветка:** `feature/support-chart`
**Дата:** 2026-09-23
**Базис проверки:** `docs/patterns.md`, `docs/architecture.md`, `docs/ui-kit.md`, `docs/localization.md`, `core/AGENTS.md`, `feature/support-chat/AGENTS.md`

Документ фиксирует замечания для передачи в работу. Нумерация сквозная и стабильная:
`D-*` — domain, `A-*` — data, `P-*` — presentation (ViewModel), `U-*` — UI и навигация,
`X-*` — кросс-модульные. Ссылаться в задачах — по этим идентификаторам.

**Приоритеты:** высокий — ломает поведение или данные пользователя; средний — нарушение
правил проекта с накопительным эффектом; низкий — чистота и поддерживаемость.

---

## Изменения в документации проекта

В рамках ревью в `docs/localization.md` добавлены недостающие правила — на них опираются
пункты U-21 … U-24:

- **правило 6** «Не склеивать UI-текст в коде» — один видимый текст = один ключ с
  плейсхолдерами; разделители, порядок слов и единицы измерения переводятся;
- **правило 7** «Технические строки — не в ресурсах, но и не inline» — URI-схемы, префиксы
  ключей списков, log-теги и `label` анимаций объявляются `private const val`;
- в разделе «Экранирование» уточнено поведение слешей: прямой `/` пишется как есть,
  экранируется только обратный `\` и только в четырёх последовательностях CMP-парсера.

Правки в коде фичи по этим правилам **не вносились** — они зафиксированы как задачи
U-21 … U-26 и выполняются исполнителем вместе с остальным списком.

---

# Часть 1. Domain

## D-1. `displayTime: String` в доменной модели — высокий

`domain/model/SupportChatMessage.kt:11`

Модель хранит готовую UI-строку `"HH:mm"` рядом с уже имеющимся `time: LocalDateTime`.
Форматирование продублировано: `data/repository/SpecialistSupportChatRepository.kt:233`
и `presentation/screen/supportchat/SupportChatScreenViewModel.kt:561` — две приватные копии
одной функции.

Почему плохо: `patterns.md` → «Domain — чистый Kotlin, модели описываются в терминах фичи»;
формат времени — презентационная логика. Строка вычисляется в момент маппинга, а в проекте
есть live-смена языка через `LocalAppLocale` — уже отрисованные сообщения формат не перечитают.

**Доработка:** удалить `displayTime`, форматировать в UI из `time`; обе копии `formatTime` убрать.

## D-2. `Image.url` non-null против `Document.url: String?` — высокий

`domain/model/SupportChatAttachment.kt:10`, `:17`

Асимметрия вынуждает ViewModel подставлять пустую строку как sentinel для оптимистичной
картинки (`SupportChatScreenViewModel.kt:471`, `url = ""`). Пустая строка — не «нет url»,
её приходится отличать отдельной проверкой.

**Доработка:** `Image.url: String?`, sentinel убрать.

## D-3. Две почти одинаковые иерархии вложения — высокий

`domain/model/SupportChatAttachment.kt`, `domain/model/SupportChatPendingAttachment.kt`

Обе — `Image`/`Document` с `id`/`name`/`localPath`/`mimeType`/`sizeBytes`, различаются только
nullability. Из-за этого в VM живут два конвертера: `toAttachment()` (`:466`) и `toPending()`
(`:497`), причём второй возвращает `null`, если `localPath == null` — см. P-13, где это
приводит к ложному статусу «отправлено».

Побочный эффект: `SupportChatAttachment.Image` не имеет `mimeType`/`sizeBytes`, а
`SupportChatPendingAttachment.Image` имеет — при конверте (`:468-473`) эти поля теряются.

**Доработка:** одна иерархия с nullable `url`/`localPath`, либо явное разделение
`Incoming`/`Outgoing` с общим интерфейсом. Конвертеры и потерю полей убрать.

## D-4. Двойная идентичность сообщения — средний

`domain/model/SupportChatMessage.kt:6-7`

`id: String` + `serverId: Long?`. Для серверных сообщений `id = serverId.toString()`
(`repository:221`), для локальных — `Random.nextLong().toString()` (`VM:545`). Контракт нигде
не зафиксирован, размазан между репозиторием и VM, и на нём держится вся логика сопоставления
эха и дедупликации.

**Доработка:** минимум — явный контракт в KDoc модели; лучше — `sealed interface MessageId`
с `Local`/`Remote`.

## D-5. `TypingChanged` не отличает «я печатаю» — средний, спящий баг

`domain/model/SupportChatEvent.kt:5`, маппинг `repository:156-163`

В `mapChatEvent` уже вычислен `myEmail`, но для Writing-событий он не используется — своё же
событие набора текста придёт в VM и отрисуется как «собеседник печатает». Сейчас не
воспроизводится только потому, что `startWriting()` никто не вызывает (D-7); включат typing —
проявится сразу.

**Доработка:** фильтровать своё событие в data-слое либо добавить в `TypingChanged` признак `isMine`.

## D-6. Platform-порты лежат в `domain/` — средний

`domain/picker/SupportChatFilePicker.kt`, `domain/picker/SupportChatFileSaver.kt`

Это платформенные UI-порты (activity-result, `UIDocumentPickerViewController`, `JFileChooser`),
а не доменные контракты: `FilePicker` — callback-API без `suspend` и без `Response`. Доменом
они не используются вообще — обращения только из nav-графа и `composeApp`.
`patterns.md` → `domain/` = модели и контракты репозиториев.

**Доработка:** перенести в `presentation/platform` (там уже лежит `isSupportChatDesktopPlatform`)
или выделить `domain/port`. Заодно `SupportChatPickedFile` вынести из файла интерфейса
(`SupportChatFilePicker.kt:9`) в `domain/model/`.

## D-7. Мёртвые методы контракта — средний

`domain/repository/SupportChatRepository.kt:18-19`

`startWriting()` / `stopWriting()` реализованы в репозитории и в тестовом фейке, но не
вызываются ниоткуда. Индикатор набора работает только в одну сторону.

**Доработка:** либо подключить (дебаунс на `onDraftChange`, стоп по таймауту и по отправке),
либо удалить из контракта вместе с реализациями.

## D-8. `null` как единственный результат отказа — средний, UX-дефект

`domain/picker/SupportChatFileSaver.kt:15`, `SupportChatFilePicker.kt:18-19`

`saveToDownloads(...): String?` схлопывает в `null` три разных исхода: ошибка записи,
отсутствие прав на Android < 29 и **отмена пользователем** системного пикера на iOS. В итоге
пользователь, сам нажавший «Отмена», получает снекбар «Не удалось скачать файл». То же у `FilePicker`.

Правило: `patterns.md` → «Ошибки из ViewModel — только типизированные».

**Доработка:** `Response<String>` либо sealed-результат `Saved / Cancelled / Failed`;
отмену не показывать как ошибку.

## D-9. Контракт истории не даёт финала — высокий

`domain/repository/SupportChatRepository.kt:12`

`loadNextMessages(): Response<Unit>` — успех означает «запрос ушёл», сама страница приезжает
асинхронно отдельным событием в `observeEvents()`. Гарантии, что событие придёт, нет. Отсюда
растёт залипание `isLoadingHistory` (P-4).

Правило: `patterns.md` → «процессы со стадиями описываются собственной sealed-иерархией
**с гарантированным финалом**».

**Доработка:** `loadNextMessages(): Response<List<SupportChatMessage>>` (страница возвращается
вызывающему) либо отдельный `Flow` со стадиями и финалом. Ключевая правка — снимает и P-4.

---

# Часть 2. Data

## A-1. Восемь копий одного `when` — средний

`data/repository/SpecialistSupportChatRepository.kt:86-149`

`closeChat`, `loadNextMessages`, `sendMessage`, `sendImage`, `sendDocument`,
`markMessagesAsRead`, `startWriting`, `stopWriting` — идентичное тело
`when (response) { Success -> Response.Success(Unit); Error -> toProjectError() }`.

**Доработка:** хелпер `private fun <T> SpecialistResponse<T>.toProject(): Response<T>`
(+ вариант для `Unit`): ~60 строк сжимаются до ~15. Заодно `openChat` (`:74-82`) с вложенным
`when` в `when` разворачивается в цепочку через `ResponseExt.mapIfSuccess` — `patterns.md`
прямо требует расширения вместо ручного `when`.

## A-2. `observeEvents()` — лишняя обёртка и двойной `catch` — средний

`:51-67`

Внешний `flow { … .collect { emit(it) } }` избыточен: цепочка `map → filterNotNull` уже
холодная и возвращается напрямую. `catch` навешан дважды, внешний недостижим. Правило
проекта — не писать ручные обёртки, а использовать `safeFlowRun` / `safeFlowResponseRun`
из `core/common`: они гарантированно сохраняют проброс `CancellationException`.

Заодно: `map` возвращает `Response<SupportChatEvent>?` и тут же `filterNotNull()` — просится `mapNotNull`.

Связано с P-17: `catch` завершает поток, и коллектор в VM после этого не перезапускается.

## A-3. Кэш `currentUserEmail` в полях репозитория чата — высокий

`:49`, `:69-84`, `:177-191`

Репозиторий чата держит `private var currentUserEmail: String?` и использует его обнуление
в `openChat()` как побочный признак «сессия сменилась». Проблемы:

1. Чат-репозиторий не владеет identity пользователя и не должен её кэшировать.
2. Обнуление выполняется под `connectionMutex`, а чтение и запись в `resolveCurrentUserEmail()` —
   вне мьютекса, из потока событий. Событие, обработанное параллельно с открытием новой сессии,
   вернёт в кэш email прошлого аккаунта — ровно то, от чего обнуление защищает
   (`feature/support-chat/AGENTS.md:10`).
3. Жизненный цикл сессии выражен неявно: «поле обнулили» вместо явной операции.

**Доработка (решение принято):**

- Email профиля получать **единожды** из account-репозитория (account info), а не хранить полем
  в чат-репозитории. `AccountRepository` в проекте отсутствует, при этом `feature/main` и
  `feature/support-chat` уже дублируют обращение к `getUserUseCase` (см. A-11) — завести
  профильный источник account info в `core/*` (или отдельным модулем уровня account) и подключить
  к нему обе фичи. Чат получает узкую зависимость на чтение профиля, а не собственный кэш.
- Принудительную очистку состояния оформить **явным контрактом**: интерфейс вида `AuthCloseable`
  (по смыслу `Closeable`) с методом `release()`. Им наследуются все контракты репозиториев,
  обязанных чиститься при смене аккаунта.
- Ввести кастомный `LogoutUseCase`, который выполняет logout и проходит по всем зарегистрированным
  `AuthCloseable`, вызывая `release()`. Сейчас такого use case нет (см. X-1), поэтому точки
  очистки не существует в принципе.

**Эффект:** уходит поле-кэш, уходит гонка, «смерть сессии» перестаёт определяться по побочному
признаку, появляется единое место для будущих репозиториев, требующих сброса.

## A-4. Провал определения автора делает все сообщения чужими — высокий

`:227`, `:181-190`

`isMine = myEmail != null && sender.login == myEmail`. Если `getUserUseCase` вернул ошибку,
`myEmail == null` → **все** сообщения, включая собственные, помечаются входящими: чужое
выравнивание, чужой цвет пузыря, кнопка «Скачать» на своих вложениях, статус доставки не
рисуется. Ошибка проглочена в `logger.d` и наружу не выходит. Дополнительно: неуспех не
кэшируется, поэтому при устойчивой ошибке запрос уходит на каждое событие.

**Доработка:** после перехода на account-репозиторий (A-3) email резолвится один раз при
открытии сессии, и его отсутствие становится ошибкой открытия чата (offline-баннер), а не
молчаливой деградацией на каждом сообщении.

## A-5. Потеря содержимого при маппинге сообщения — средний

`:193-216`

- Расширение объявлено на `ChatEvent.Chat.Message`, но `messageContent` читается только у
  `Default` (`:194-197`); для любого другого наследника вернётся сообщение без текста и вложения —
  либо мёртвая ветка, либо тихая потеря данных.
- `text` заполняется только для `ChatMessageContent.Text`. Если SDK умеет присылать вложение
  **с подписью**, текст будет потерян.

**Доработка:** проверить по SDK, умеет ли сообщение нести текст и вложение одновременно; если да —
маппить оба поля; ветку `else -> null` либо убрать, либо сделать явной ошибкой маппинга.

## A-6. Часовой пояс зашит без обоснования — средний

`:218`

`time.toInstant(TimeZone.UTC)` — предположение, что сервер отдаёт UTC, нигде не зафиксировано.
Если формат изменится или окажется локальным, получим сдвиг на часы без единой ошибки в логах.

**Доработка:** комментарий с обоснованием + тест на маппинг времени.

## A-7. Статус входящего сообщения врёт — низкий

`:229`

`status = if (wasRead) Read else Sent` выставляется всем сообщениям, хотя `Sent`/`Read` — статус
доставки **моего** сообщения. Визуально не видно (UI рисует иконку только при `isMine`), но на
это поле завязан `onMessageVisible` (`VM:247` проверяет `status == Read`).

## A-8. `downloadFile` грузит файл целиком в память — средний

`:123-128`

`Response<ByteArray>` без ограничения размера: документ на несколько десятков МБ на слабом
Android-устройстве — риск OOM.

**Доработка:** минимум — лимит размера и внятная ошибка при превышении; в перспективе —
потоковая запись в файл вместо `ByteArray`.

## A-9. Тег логгера строковым литералом — низкий

`:43`

`Logger.withTag("SpecialistSupportChatRepository")` — литерал, который разъедется при
переименовании класса. В `BaseViewModel` это сделано через `this::class.simpleName`.

## A-10. `loadNextMessages` вне `connectionMutex` — низкий

`:95`

Мьютекс сериализует только `openChat`/`closeChat`. Загрузка страницы может стартовать в момент
закрытия соединения. Проверить по поведению SDK, нужно ли расширить область сериализации.

## A-11. Дублирование доступа к профилю между фичами — средний

`SpecialistMainRepository.kt:49` и `SpecialistSupportChatRepository.kt:181`

Два feature-модуля независимо вызывают `userInteractor.getUserUseCase` и строят свои модели
поверх одного источника. `patterns.md` → «Понадобилась глобальная возможность приложения —
сначала добавь/расширь профильный `core/*` API». Потребность подтверждена двумя потребителями,
то есть выносить пора — это не «на будущее».

**Доработка:** закрывается тем же account-репозиторием из A-3.

---

# Часть 3. Presentation — ViewModel

Файл: `presentation/screen/supportchat/SupportChatScreenViewModel.kt` (570 строк),
`presentation/screen/supportchat/SupportChatScreenState.kt`,
`presentation/navigation/SupportChatNavGraph.kt`.

## S-0. Предварительное условие: per-entry ViewModel scope в nav3

Почти все замечания по жизненному циклу (P-1, P-2, P-3) существуют из-за одного факта,
зафиксированного в `feature/support-chat/AGENTS.md:9`: «в nav3 1.1.x нет per-entry
`ViewModelStoreOwner`». **Этот факт устарел** — проверено по исходникам в Gradle-кэше.

| Что | Результат проверки |
|---|---|
| `androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0` | существует, `commonMain` |
| JB-порт `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0` | существует; таргеты **android, desktop, iosArm64, iosSimulatorArm64, uikit\*** — все платформы проекта покрыты |
| API | `rememberViewModelStoreNavEntryDecorator()` → per-entry `ViewModelStoreOwner` через `LocalViewModelStoreOwner` |
| nav3 1.1.1 (текущая) | `NavEntryDecorator<T>(onPop, decorate)` и `NavDisplay(entryDecorators = …)` **уже есть** |
| Обновление nav3 | **не требуется**; у JetBrains последняя — `1.2.0-alpha02`, тащить alpha незачем |

### Что обновить

`gradle/libs.versions.toml`:

```toml
[versions]
androidx-lifecycle = "2.11.0"   # было 2.10.0
androidx-nav3 = "1.1.1"         # НЕ трогаем

[libraries]
androidx-lifecycle-viewmodel-nav3 = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "androidx-lifecycle" }
```

`feature/support-chat/build.gradle.kts` (и другие модули со своим `NavDisplay`):

```kotlin
commonMain.dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
}
```

### Как подключить

Декоратор добавляется в каждый `NavDisplay` — и в корневой (`composeApp/.../AppNavigation.kt`),
и во вложенные графы фич. Дефолтный `SaveableStateHolder`-декоратор **обязательно сохранить**:
без него не работает `SavedStateHandle` (требование указано в KDoc самого класса).

```kotlin
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

NavDisplay(
    backStack = backStack,
    modifier = Modifier.fillMaxSize(),
    onBack = onBack,
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),   // дефолт NavDisplay, не потерять
        rememberViewModelStoreNavEntryDecorator(),        // per-entry ViewModelStoreOwner
    ),
    entryProvider = entryProvider<NavKey> { … },
)
```

### Что это меняет

| Было | Стало |
|---|---|
| VM живёт в app-level `ViewModelStore`, переиспользуется между аккаунтами и открытиями | VM живёт ровно столько, сколько запись бэкстека |
| Сессия открывается/закрывается из `DisposableEffect` в nav-графе | `init {}` открывает, `onCleared()` закрывает |
| Пять полей-`Job` + ручной сброс состояния в `onScreenOpened()` | не нужны: новый вход = новый инстанс VM = чистое состояние |
| `koinViewModel()` отдаёт один и тот же инстанс | резолвит owner через `LocalViewModelStoreOwner`, отдаёт per-entry инстанс |

### Проверить при внедрении

- `koinViewModel()` берёт owner из `LocalViewModelStoreOwner` — должен подхватить декоратор без правок; убедиться на всех трёх платформах.
- `onCleared()` не `suspend`: закрытие чата выполнять в скоупе, переживающем VM (смыкается с A-3 / X-1 — `AuthCloseable` и app-scoped очистка).
- После внедрения переписать `feature/support-chat/AGENTS.md:9` — обоснование там станет неверным.

---

## S-1. Форма state экрана — принято

Требования: подписка на WebSocket, при неудаче — висячий баннер сверху с кнопкой
переподключения; параллельно пагинация истории; ошибка **первой** загрузки — во весь экран;
ошибка **последующей** пагинации — тост.

Ключевое: различие «первая / последующая» **не нужно хранить флагом** — полноэкранная ошибка
имеет смысл ровно тогда, когда показывать нечего:

```
Ошибка истории →
    history is Content  →  тост (лента остаётся на экране)
    иначе               →  LCEState.Error, ошибка во весь экран
```

```kotlin
@Immutable
data class SupportChatScreenState(
    // История: None/Loading → скелетон, Content → лента, Error → полный экран
    val history: LCEState<List<SupportChatMessage>> = LCEState.None(),
    val isLoadingMore: Boolean = false,      // тонкая полоска при подгрузке
    val hasMoreHistory: Boolean = true,

    // Соединение — измерение, независимое от истории
    val isOffline: Boolean = false,

    val typingAuthor: String? = null,
    val draftText: String = "",
    val pendingAttachments: List<SupportChatPendingAttachment> = emptyList(),
    val downloadingAttachmentId: String? = null,
)
```

`isOffline` и `history` не сливаются в один LCE сознательно: можно быть offline с полной лентой
(баннер сверху, лента под ним) и online с ошибкой истории (полный экран, баннера нет).

Типизированную ошибку рядом заводить пока не нужно — случай один, UI рисует свой
`stringResource` в ветке `Error`. Появятся разные причины — добавится поле по образцу
`MainScreenState.logoutError`.

UI-маппинг:

```kotlin
if (state.isOffline) SupportChatOfflineBanner(onRetry = onConnectionRetry)
if (state.isLoadingMore) CmpLinearProgressIndicator()

when (val h = state.history) {
    is LCEState.None, is LCEState.Loading -> SupportChatLoadingSkeleton()
    is LCEState.Error   -> SupportChatErrorState(onRetry = onHistoryRetry)   // новый компонент
    is LCEState.Content -> if (h.content.isEmpty()) SupportChatEmptyState() else LazyColumn(…)
}
```

Живое сообщение, пришедшее во время первой загрузки: буфер не нужен, достаточно правила
«первое сообщение снимает скелетон» — и оно же защищает от затирания ленты полноэкранной
ошибкой, потому что `history` уже станет `Content`:

```kotlin
is LCEState.Content -> LCEState.Content(h.content.merge(message))
else                -> LCEState.Content(listOf(message))
```

Доделать: компонент `SupportChatErrorState` (во весь экран, кнопка «Повторить»), интент
`onHistoryRetry()` — сейчас повтора для истории нет вообще, и две строки в ресурсы.

---

## S-2. Сборка state во ViewModel — принят вариант B

`_state: MutableStateFlow` как источник истины + сессия отдельным холодным источником
со `stateIn(SharingStarted.Lazily)`; жизненный цикл инстанса даёт per-entry VM из S-0.

**`WhileSubscribed` на этом экране не применять.** Причины:

- на `MutableStateFlow` `stateIn`/`flowOn` бессмысленны в принципе — `patterns.md` говорит, что
  они применимы только к холодным Flow;
- проверено: список сообщений при блокировке экрана **не теряется** — `stateIn` сохраняет
  последнее значение (сброс к `initialValue` включается только параметром `replayExpiration`,
  дефолт — `Long.MAX_VALUE`), а в проекте везде обычный `collectAsState()`
  (`SupportChatNavGraph.kt:42` и ещё пять мест), который подписан, пока композабл в композиции,
  независимо от lifecycle — при блокировке экрана отписки не происходит вовсе;
- риск появился бы при переходе на `collectAsStateWithLifecycle`: тогда через 5 секунд после
  блокировки экрана погас бы **сокет**, а не список. Для чата это отдельное продуктовое решение.

`WhileSubscribed` остаётся правильным выбором там, где есть что гасить, — например поллинг
непрочитанных в `MainScreenViewModel`.

---

## S-3. Рассмотрено: open/close на `onStart` / `onCompletion` — отклонено

Идея: привязать открытие и закрытие чата к жизненному циклу подписки на `observeEvents()`.
Направление верное, но в прямом виде даёт три дефекта:

1. **`onStart { openChat() }` воспроизводит гонку подписки.** `Flow.onStart` выполняет лямбду
   **до** подписки на upstream. А `AGENTS.md:8` требует обратного порядка: сначала подписка на
   hot flow, потом открытие, иначе теряется `MessagesPage`, публикуемый без replay. Сейчас это
   обходится `yield()` в `VM:69`. Правильная семантика «подписался → открывай» — `onSubscription`,
   но он существует только для `SharedFlow`.
2. **`onCompletion { closeChat() }` при отмене не выполнится.** Лямбда вызывается в контексте уже
   отменённой корутины, а `closeChat()` — `suspend` с `connectionMutex.withLock` и сетевым
   вызовом: он немедленно бросит `CancellationException`, сокет останется открытым. Нужен
   `withContext(NonCancellable)`, и даже тогда завершения никто не ждёт.
3. **Ручной реконнект исчезает.** При `Lazily` поток не перезапускается; кнопка «Повторить»
   потребует внешнего триггера через `retryTrigger.flatMapLatest { … }`.

Если всё же делать — только целиком, все три условия обязательны:

```kotlin
// репозиторий владеет соединением; emitAll убирает гонку порядка
override fun observeSession(): Flow<Response<SupportChatEvent>> = flow {
    val opened = openChat()
    if (opened is Response.Error) { emit(opened); return@flow }
    emitAll(rawEvents())
}.onCompletion {
    withContext(NonCancellable) { closeChat() }
}

// VM: триггер даёт ручной реконнект и закрывает P-18 по построению
private val events = retryTrigger.flatMapLatest { repository.observeSession() }
```

**Решение:** отклонено в пользу S-0 + S-2. С per-entry VM симметрия достигается через
`init {}` / `onCleared()` без гонки `onStart`, без `NonCancellable` и без `flatMapLatest`,
который приходится держать в голове при отладке. Вариант зафиксирован здесь, чтобы не
возвращаться к нему повторно.

---

## Шаги доработки presentation

1. **S-0** — поднять `androidx-lifecycle` до 2.11.0, добавить `lifecycle-viewmodel-navigation3`,
   подключить `rememberViewModelStoreNavEntryDecorator()` в корневой и вложенные `NavDisplay`.
   Проверить на всех трёх платформах, что `koinViewModel()` отдаёт per-entry инстанс.
2. **P-1, P-2, P-3** — перенести открытие сессии в `init {}`, закрытие в `onCleared()`
   (через app-scoped скоуп, см. A-3 / X-1). Удалить `onScreenOpened()`/`onScreenDisposed()`,
   `DisposableEffect` в nav-графе и все пять полей-`Job`. Переписать `AGENTS.md:9`.
3. **D-9 + A-1** — контракт истории с гарантированным финалом и дедупликация репозитория.
   Без этого шага P-4 не лечится.
4. **S-1** — заменить `isLoadingHistory` на `history: LCEState<List<…>>` + `isLoadingMore`.
   Добавить `SupportChatErrorState`, интент `onHistoryRetry()`, строки в ресурсы.
   Закрывает P-4 (в части блокировки ввода), P-5, P-20.
5. **P-7, P-9** — убрать ложный `Sent` (ветка «нечего отправлять» не должна возвращать
   `Success`), привести обе ветки `indexOfEcho` к FIFO.
6. **P-10** — персистить офлайн-очередь либо убрать обещание из текста баннера.
7. **P-11, P-12, P-13** — батч `markMessagesAsRead`, `executeWithProceedLock` вместо самодельного
   барьера, `SupportChatFileSaver` в конструктор VM.
8. **P-14 … P-17** — чистка: хелпер обновления статуса, единый источник `canSend`, порядок
   сообщений в state, устойчивый клиентский id.

---

## P-1. Жизненный цикл экрана на `DisposableEffect` и пяти ручных `Job` — высокий

`SupportChatScreenViewModel.kt:44-49`, `SupportChatNavGraph.kt:45-48`

Сессия чата запускается и гасится из `DisposableEffect` в nav-графе, а VM держит
`typingTimeoutJob`, `eventCollectionJob`, `reconnectJob`, `pendingRetryJob`, `sessionJob`
и флаг `pendingRetryRequested`.

`patterns.md` запрещает и то, и другое: «Бизнес-логику не запускать из
`LaunchedEffect`/`DisposableEffect`» и «Поллинг не держать ручными `Job` во ViewModel».
Канонический механизм — `stateIn(WhileSubscribed(5.seconds))` + `onStart` на общей цепочке:
старт и остановка сессии, а также сброс transient-state при возврате на экран выражаются
временем жизни шаринга, без полей-джобов.

В `feature/support-chat/AGENTS.md:9` это зафиксировано как вынужденная уступка nav3
(VM живёт в app-level `ViewModelStore`, per-entry owner в nav3 1.1.x нет). **Уступка больше
не действует** — per-entry `ViewModelStoreOwner` доступен, см. S-0. Из этого пункта прямо
следуют P-2 и P-3.

**Доработка:** S-0 (подключить `rememberViewModelStoreNavEntryDecorator()`), затем перенести
открытие сессии в `init {}`, закрытие — в `onCleared()` через app-scoped скоуп (A-3 / X-1).
`DisposableEffect`, `onScreenOpened()`/`onScreenDisposed()` и все пять полей-`Job` удалить.
Затрагивает nav-граф и тесты VM.

## P-2. Гонки на полях жизненного цикла — высокий

`:260` (коллектор на `dispatchers.io`), `:294-297`, `:305`, `:435-443`

Коллектор событий запущен через `safeLaunch(dispatchers.io)`, и **из io-потока** пишутся
`reconnectJob` (`handleConnectionChanged`), `typingTimeoutJob` (`handleTypingChanged`),
`pendingRetryJob` и `pendingRetryRequested` (`retryPendingMessages`). Те же поля пишутся
с main из `onScreenOpened()`, `onScreenDisposed()`, `onConnectionRetry()`. Поля — обычные
`var`, без `@Volatile` и без мьютекса.

Правило: `patterns.md` → «Гонки: источник истины в `MutableStateFlow` (атомарный `update`)
либо сериализация под `Mutex`».

Последствия на практике: потерянная отмена джоба, два параллельных цикла ретрая, «повисший»
таймаут индикатора набора.

`markedAsReadIds` (`:50`) сейчас main-confined — отдельного риска не несёт, но при рефакторинге
его тоже нужно учесть.

**Доработка:** снимается вместе с P-1; до этого — минимум перевести флаги в состояние и
сериализовать через `executeWithProceedLock`.

## P-3. Поля `reconnectJob` и `sessionJob` перегружены двумя ролями — средний

`:227` и `:286`, `:295`; `:66` и `:81`

`reconnectJob` хранит то попытку переподключения, то закрытие сокета. `sessionJob` — то
загрузку истории, то `closeChat`. Из-за совмещения следующая операция молча отменяет
предыдущую другого смысла: ручной retry отменит незавершённый `closeChat`, и наоборот.

**Доработка:** разделить поля по смыслу либо (предпочтительно) убрать вместе с P-1.

## P-4. `isLoadingHistory` может залипнуть и заблокировать чат — высокий

`:212-223`, `:528-537`; следствие в `SupportChatScreen.kt:444`

Флаг ставится в `true` перед `loadNextMessages()` и снимается **только** двумя путями:
`Response.Error` от вызова либо приход события `HistoryPageReceived`. Если вызов вернул
`Success`, а событие не придёт — например, соединение оборвалось между вызовом и ответом,
либо сокет стал «зомби» (описано в `AGENTS.md:14` для iOS) — флаг остаётся `true` навсегда.

Последствия: бесконечный `CmpLinearProgressIndicator` и, что важнее, **заблокированный
композер** (`enabled = !state.isLoadingHistory`) — отправить сообщение нельзя до перезахода
на экран. Таймаута нет, сброса при переходе в offline нет.

Штатный сценарий пустой истории работает: SDK присылает пустую страницу событием (покрыто
тестом `empty history page sets hasMoreHistory to false`). Проблема именно в отсутствии
гарантированного финала.

**Доработка:** корень — D-9 (контракт с гарантированным финалом). S-1 снимает худшее
следствие: композер перестаёт гейтиться загрузкой, и залипание вырождается в вечный скелетон
вместо заблокированного ввода. До исправления D-9 — сбрасывать состояние загрузки при переходе
в offline и при закрытии сессии.

## P-5. Композер блокируется на любой подгрузке истории — средний, UX

`SupportChatScreen.kt:444`

`enabled = !state.isLoadingHistory` выключает ввод не только на начальной загрузке, но и при
каждой подгрузке предыдущей страницы: пользователь скроллит вверх — поле ввода и кнопки
вложений гаснут.

**Доработка:** не блокировать ввод загрузкой вовсе — отправка сообщения не зависит от
истории. Закрывается в рамках S-1: отправку гейтит только реальный offline.

## P-6. `hasMoreHistory` выводится эвристикой «нет новых — значит конец» — средний

`:353`

`hasMoreHistory = newMessages.isNotEmpty()` после дедупликации. Если источник повторно отдаст
уже известную страницу (сбой пагинации, переоткрытие сессии), пагинация выключится навсегда.

Поведение **закреплено тестом** `duplicate history messages are ignored` (`:458`), то есть
сейчас это сознательный контракт, а не недосмотр. Отмечаем как риск, а не как дефект.

**Доработка:** признак конца истории должен приходить от источника (см. D-9), а не выводиться
из результата дедупликации.

## P-7. Ложный статус «отправлено» для неотправленного сообщения — высокий

`:194-209`, `:388-392`, `:497-512`

В `onRetry` вложение конвертируется через `attachment?.toPending()`, который возвращает `null`,
если `localPath == null` (D-3). Дальше `sendOutgoingMessage` получает `text = ""` и
`attachment = null`, попадает в ветку

```kotlin
else -> Response.Success(Unit)
```

и сообщение получает статус **`Sent`**. Пользователь видит галочку доставки для сообщения,
которое не отправлялось и никогда не дойдёт.

**Доработка:** ветка «нечего отправлять» не должна возвращать `Success` — это ошибка
контракта; логировать и переводить сообщение в `Failed`. Первопричину закрывает D-3.

## P-8. Сообщение с текстом и вложением: вложение теряется — средний

`:388-392`

`when` отдаёт приоритет тексту: при непустом тексте вложение не отправляется вовсе. Сейчас
не воспроизводится, потому что `onSend()` (`:166-173`) разводит текст и вложения по отдельным
сообщениям. Но `onRetry` работает с одним сообщением, и если серверное эхо несёт и текст,
и вложение (см. A-5), ретрай отправит только текст.

**Доработка:** отправлять оба либо явно запретить комбинацию на уровне модели.

## P-9. Несогласованный порядок сопоставления эха — средний

`:340-344`

```kotlin
indexOfLast { … contentMatches(message) }   // LIFO
indexOfFirst { … shapeMatches(message) }    // FIFO
```

Две ветки одной функции используют противоположный порядок. Для двух одинаковых сообщений
подряд эхо связывается с **последним** оптимистичным, а не с первым, из-за чего статус
доставки может оказаться не на том сообщении. FIFO покрыт тестом только для ветки
`shapeMatches` (`server echoes for two pending images merge in FIFO order`).

**Доработка:** привести обе ветки к FIFO — порядок подтверждений соответствует порядку
отправки. Радикальное решение — клиентский идентификатор в эхе (D-4).

## P-10. Офлайн-очередь и черновик теряются при уходе с экрана — высокий, UX

`:62`

`onScreenOpened()` делает `_state.update { SupportChatScreenState() }` — полный сброс,
включая сообщения в статусе `Pending` и набранный черновик. Очередь живёт только в памяти
state и нигде не персистится. При этом UI обещает обратное:
`support_chat_offline_subtitle` — «Сообщения будут отправлены при восстановлении связи».

Поворот экрана безопасен (`androidApp/src/main/AndroidManifest.xml:14`,
`configChanges="orientation|screenSize|screenLayout|keyboardHidden"`), но конфигурационные
изменения вне этого списка — смена системной локали, размера шрифта — пересоздают Activity
и дают тот же сброс. Обычный уход с экрана и возврат — тоже.

**Доработка:** либо персистить очередь неотправленных сообщений, либо убрать обещание из
текста баннера. Первое предпочтительно — это заявленное поведение фичи.

## P-11. `markMessagesAsRead` вызывается по одному сообщению — средний

`:244-256`

На каждое ставшее видимым сообщение уходит отдельный сетевой вызов `listOf(serverId)`, хотя
контракт принимает `List<Long>` (`SupportChatRepository.kt:17`). При быстрой прокрутке истории —
десятки одиночных запросов подряд.

**Доработка:** буферизовать идентификаторы и отправлять пачкой с небольшим debounce.

## P-12. Самодельный барьер вместо готового хелпера — средний

`:434-444`

`pendingRetryRequested` + цикл `while` реализуют «перезапусти, если попросили во время
выполнения». В `BaseViewModel` для сериализации уже есть
`executeWithProceedLock(needSkipAnother = false)`. Плюс сам флаг участвует в гонке P-2.

## P-13. Зависимость приходит параметром метода, guard неатомарен — средний

`:131`, `:136`; вызов из `SupportChatNavGraph.kt:70-72`

`onDownloadAttachment(attachment, saver)` получает `SupportChatFileSaver` из UI вместо
конструктора — `patterns.md` требует передавать узкие зависимости в конструктор feature-класса.

Там же `if (_state.value.downloadingAttachmentId != null) return` — чтение и последующая
запись не атомарны: два быстрых нажатия могут пройти оба. И второе нажатие молча игнорируется
без обратной связи пользователю.

**Доработка:** saver — в конструктор VM; guard — через атомарный `update` либо
`executeWithProceedLock`.

## P-14. Пятикратное дублирование «обновить статус по id» — низкий

`:199`, `:381`, `:398`, `:408`, `:454`

Пять копий `messages.map { if (it.id == X) it.copy(status = Y) else it }`.

**Доработка:** приватный хелпер `updateMessageStatus(id, status)`.

## P-15. `isComposerEnabled` в state не используется — низкий

`SupportChatScreenState.kt:18`, `SupportChatComposer.kt:54`

Правило «можно ли отправлять» посчитано в state и **независимо** пересчитано в композере
(`canSend`). Две копии одного условия разойдутся молча.

**Доработка:** оставить один источник — поле state.

## P-16. Порядок сообщений не поддерживается в state — низкий

`:333` и `SupportChatScreen.kt:471`

`handleMessageReceived` добавляет сообщение в конец списка, а сортировка по времени делается
в UI при каждом построении списка. Инвариант «список упорядочен» должен держать state,
а не пересортировка на каждом рендере.

## P-17. `Random.nextLong()` как клиентский идентификатор — низкий

`:545`

Нет гарантии уникальности и монотонности. Связано с D-4.

## P-18. Коллектор событий не перезапускается при ручном реконнекте — средний

`:225-242`, `:258-269`; связано с A-2

`observeEvents()` завершает поток, если upstream бросит исключение: `catch` эмитит `Error`
и закрывает flow. `onConnectionRetry()` повторяет только `openChat()` и **не** вызывает
`startEventCollection()` — баннер снимется, а события больше приходить не будут: чат выглядит
живым, фактически мёртв.

Основной сценарий обрыва на iOS приходит как `Response.Error` внутри потока и его не завершает
(`AGENTS.md:14`), поэтому воспроизводится не всегда — но путь с исключением не закрыт.

**Доработка:** при ручном реконнекте перезапускать коллектор, либо сделать поток
самовосстанавливающимся (`retryWhen`) в репозитории.

## P-19. `handleEventError` глушит все последующие ошибки — принятое решение

`:281-287`

`if (_state.value.isOffline) return` — сознательная защита от кГц-шторма ошибок на iOS,
обоснование в `AGENTS.md:14`. Замечаний нет; стоит продублировать обоснование комментарием
в самом методе, чтобы его не «починили» при рефакторинге.

---

## P-20. Ошибка первой загрузки показывается как пустой чат — высокий

`:528-537`, следствие в `SupportChatScreen.kt:306`

Если `openChat()` прошёл, а `loadNextMessages()` упал, `loadHistoryPage()` снимает флаг загрузки
и эмитит снекбар `HistoryLoadFailed`; `isOffline` при этом не выставляется. В результате
`isEmpty` становится `true`, и экран рисует empty-state **«Чем можем помочь?»** — приглашение
написать вместо сообщения об ошибке. Снекбар уезжает через пару секунд, и пользователь остаётся
с ложной картиной «переписки нет».

Состояния «первая загрузка провалилась» в текущей модели не существует в принципе, как и
интента для повтора.

**Доработка:** закрывается S-1 — ветка `LCEState.Error` и полноэкранный `SupportChatErrorState`
с кнопкой «Повторить».

---

# Часть 4. Кросс-модульное

## X-1. Нет точки очистки состояния при логауте — высокий

`feature/main/.../MainScreenViewModel.kt:168-199`, `feature/auth-state/.../AuthStateRepository.kt:8`

Логаут — один вызов `authStateRepository.logout()` из ViewModel экрана. Ни use case, ни хука
очистки нет, поэтому app-scoped синглтоны (чат-репозиторий зарегистрирован как `single`,
`SupportChatModule.kt:12`) переживают смену аккаунта со своим состоянием: открытым соединением,
кэшем identity, незакрытой сессией.

**Доработка:** `LogoutUseCase` + контракт `AuthCloseable` (см. A-3). Это app-level
инфраструктура — живёт в `core/*` или composition root, не в фиче.

---

# Часть 5. UI и навигация

Файлы: `presentation/screen/supportchat/SupportChatScreen.kt`,
`presentation/screen/supportchat/subcomponents/*` (10 компонентов),
`presentation/screen/supportchat/SupportChatImageLoaderProvider.kt`,
`presentation/navigation/SupportChatNavGraph.kt`,
`presentation/platform/SupportChatPlatform.*`.

Базис: `docs/ui-kit.md`, `docs/compose-best-prac.md`, `docs/navigation.md`.

## Дизайн-система и зависимости

### U-1. Coil и собственный `ImageLoader` живут внутри фичи — высокий

`SupportChatImageLoaderProvider.kt`, `build.gradle.kts:56-57`

Фича объявляет зависимости `coil` и `coil-network-ktor` и сама строит
`ImageLoader.Builder(context).build()`. Это app-level инфраструктура: у `ImageLoader` свой
сетевой стек, memory- и disk-кэш. `patterns.md`, раздел «Core»: «App-level инфраструктура живёт
в `core/*` или `composeApp`, не в фиче — инициализация сторонних SDK, network/client factories».

Следствия:

- кэш изображений не переиспользуется другими экранами — следующая фича с картинками заведёт
  второй `ImageLoader` со своим кэшем;
- `remember(context)` пересоздаёт загрузчик при входе в композицию — memory-кэш теряется;
- загрузчик не настроен и не связан с `core/network`: нет crossfade, лимитов кэша, общего
  Ktor-движка.

**Доработка:** один `ImageLoader` в `core/ui` (или `composeApp`), провайдится над всем деревом;
зависимости Coil переезжают туда же.

### U-2. Coil-composable вызываются из фичи напрямую — высокий

`SupportChatMessageBubble.kt:344` (`SubcomposeAsyncImage`), `SupportChatImageViewer.kt:45`
(`AsyncImage`)

`ui-kit.md`: «Feature-модули и `composeApp` **НИКОГДА не используют** компоненты
Material3/Foundation напрямую — только через `Cmp*`-функции». Coil — та же категория сторонней
UI-библиотеки, и в карте компонентов `core/ui` её нет. Замена или обновление Coil сейчас
означает правки во всех фичах, которые его вызовут.

**Доработка:** `CmpAsyncImage` в `core/ui/view/image/` (с поддержкой placeholder и
intrinsic-размера, которые нужны пузырю), фича переходит на него.

### U-3. `foundation.Image` при существующем `CmpImage` — средний

`SupportChatMessageBubble.kt:374`

`CmpImage` есть в `core/ui/view/image/CmpImage.kt` и делает ровно то же (`painter`,
`contentDescription`, `contentScale`, `alignment`). Прямое нарушение ui-kit без причины.

### U-4. Полноэкранный диалог в обход `CmpDialog` — средний, пробел дизайн-системы

`SupportChatImageViewer.kt:32` использует `androidx.compose.ui.window.Dialog` с
`usePlatformDefaultWidth = false`.

Это **не вина фичи**: `CmpDialog` — обёртка над `AlertDialog` и полноэкранный режим не
поддерживает в принципе. То есть дизайн-система не покрывает сценарий «просмотр изображения».

**Доработка:** добавить в `core/ui` полноэкранный вариант (`CmpFullScreenDialog` или параметр
у `CmpDialog`), обновить таблицу компонентов в `ui-kit.md`.

### U-5. В `core/ui` нет обёрток для списков и Canvas — средний, пробел дизайн-системы

`SupportChatScreen.kt:361` (`LazyColumn`), `SupportChatComposer.kt:137` (`LazyRow`),
`SupportChatWallpaper.kt:21` (`Canvas`)

В карте компонентов `ui-kit.md` списков нет вообще — чат стал первым экраном со скроллируемым
списком. Сейчас правило «только `Cmp*`» нарушается молча, без осознанного решения.

**Доработка:** выбрать одно из двух и зафиксировать в `ui-kit.md` — либо добавить
`CmpLazyColumn`/`CmpLazyRow`, либо явно записать исключение для lazy-контейнеров и `Canvas`.
Второе честнее: обёртка над lazy-списком неизбежно протечёт `LazyListScope`.

### U-6. У `CmpText` нет типографических ролей — низкий, пробел дизайн-системы

`SupportChatEmptyState.kt:56` — `fontSize = 22.sp` числом.

`CmpText` принимает только `fontSize: TextUnit`, ролей типа `Title`/`Body`/`Caption` нет,
поэтому задать размер иначе невозможно. Результат — магические числа в фичах и расхождение
шрифтовой шкалы между экранами.

**Доработка:** добавить в `CmpText` параметр роли (собственный enum, маппинг на
`MaterialTheme.typography` внутри обёртки — по правилу сигнатур из `ui-kit.md`).

## Рекомпозиция и производительность

### U-7. Полная пересборка и сортировка списка на каждое сообщение и на каждый тик typing — средний

`SupportChatScreen.kt:147-155`, `:471`

`listItems` мемоизируется ключами `(state.messages, state.typingAuthor, todayLabel,
yesterdayLabel, monthLabels)`. Внутри `buildListItems` делает `messages.sortedBy { it.time }` —
O(n log n) по всей истории.

Два триггера лишней работы:

- любое изменение сообщения (в том числе смена статуса на `Sent`) меняет `state.messages` →
  полная пересборка и пересортировка;
- `typingAuthor` в ключе: появление и исчезновение индикатора набора пересобирает **весь**
  список, хотя это один элемент в конце.

**Доработка:** порядок сообщений поддерживать в state (P-16), а не сортировать на каждом
построении; typing вынести из `listItems` в отдельный `item {}` в конце `LazyColumn` — тогда
он перестанет инвалидировать список.

### U-8. `onMessageVisible` вызывается на каждый кадр прокрутки, коллектор перезапускается — средний

`SupportChatScreen.kt:309-318`

```kotlin
LaunchedEffect(listState, listItems) {
    snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
```

- `listItems` в ключе эффекта: каждое новое сообщение пересоздаёт подписку `snapshotFlow`.
- `layoutInfo` меняется на каждый пиксель скролла, `map { it.key }` аллоцирует новый список на
  каждый кадр — `distinctUntilChanged` отсекает дубликаты уже после аллокации.
- Дальше `forEach` дёргает VM на каждый видимый ключ, а VM делает `find` по всему списку
  (`VM:245`) — O(видимых × всех) за кадр.

**Доработка:** ключ эффекта — только `listState`; отдавать во ViewModel пачку id одним вызовом
(смыкается с P-11); в VM держать индекс по id вместо линейного поиска.

### U-9. Автоскролл выбрасывает пользователя из истории — средний, UX

`SupportChatScreen.kt:210-214`

```kotlin
LaunchedEffect(state.messages.lastOrNull()?.id, state.typingAuthor) {
    listState.animateScrollToItem(listItems.size - 1)
}
```

Скролл в конец выполняется **безусловно** при каждом новом сообщении. Если пользователь в этот
момент читает историю выше, его насильно уносит вниз. Стандартное поведение чатов —
доскроллить только если пользователь уже находится у последнего сообщения, иначе показать
индикатор «есть новые».

Подгрузка предыдущей страницы сама по себе скролл не сбивает (последний id не меняется) — но
если страница и новое сообщение придут одновременно, эффект сработает уже после вставки.

**Доработка:** скроллить только при `!listState.canScrollForward` (или в пределах порога от
конца).

### U-10. Список не `reverseLayout` — средний

`SupportChatScreen.kt:361`

Для чата канонична схема `reverseLayout = true` + список от новых к старым: «низ» становится
естественной стартовой позицией, автоскролл-эффект (U-9) не нужен вовсе, а пагинация вверх
выражается через `canScrollForward`. Сейчас при открытии экрана пользователь на мгновение видит
верх истории, после чего происходит рывок вниз.

Переделка заметная, но снимает U-9 и упрощает U-8.

### U-11. Черновик в state ViewModel рекомпозирует весь экран на каждый символ — средний

`SupportChatScreenState.kt:14`, `VM:86`, `SupportChatNavGraph.kt:42`

`draftText` живёт в state VM. Каждое нажатие клавиши → `_state.update` → новое значение
`StateFlow` → рекомпозиция `SupportChatScreen` → `SupportChatScaffold` → `SupportChatBody`,
внутри которого `LazyColumn` с сообщениями и `BoxWithConstraints`.

`compose-best-prac.md` относит такое к локальному UI-состоянию. ViewModel черновик нужен только
в момент отправки.

**Доработка:** держать черновик в `rememberSaveable` внутри композера, во ViewModel передавать
по `onSend`. Оговорка: если решим персистить офлайн-черновик (P-10), нужна явная синхронизация —
либо VM остаётся владельцем, но тогда экран надо разбить так, чтобы список не попадал в область
рекомпозиции композера.

### U-12. Sealed domain-типы передаются параметрами composable — средний

`SupportChatMessageBubble.kt:226` (`attachment: SupportChatAttachment`),
`SupportChatPendingAttachmentChip.kt:26`, `SupportChatComposer.kt:48`
(`List<SupportChatPendingAttachment>`)

`patterns.md`: «Если domain-тип с интерфейсной статикой (`sealed interface`) передаётся напрямую
параметром composable — компилятор считает его нестабильным. Решение: маппить в аннотированную
presentation-модель». Плюс `compose-best-prac.md` §3: `List<T>` нестабилен всегда.

`presentation/model` в фиче содержит только `SupportChatEffect` — presentation-моделей нет.

**Доработка:** presentation-модели вложений; списки — `ImmutableList` либо `@Immutable`-обёртка.

### U-13. Сигнатуры на 13 и 15 параметров — низкий

`SupportChatScreen.kt:86-101` (13 параметров), `:188-206` (15), `:277-295` (15)

Каждый новый интент добавляется в три сигнатуры подряд: `Screen` → `Scaffold` → `Body`.
`compose-best-prac.md`, «Когда НЕ разбивать»: «придётся добавить много параметров в извлечённую
функцию».

**Доработка:** собрать колбэки в один `@Immutable data class SupportChatActions(...)`.

### U-14. Значение state читается в nav-графе — низкий

`SupportChatNavGraph.kt:42`, `:58`

`collectAsState()` вызван без `by` — это правильно, но дальше передаётся `state.value`, то есть
чтение всё равно происходит в области nav-графа, и она рекомпозируется на каждое изменение
состояния. `compose-best-prac.md` §5 описывает ровно этот случай: передавать дочернему
`State<T>`.

Эффект сейчас мал (в графе нет дорогих соседей), отсюда низкий приоритет — но в паре с U-11 это
и есть причина, по которой ввод символа рекомпозирует всё поддерево.

## Платформа и навигация

### U-15. `expect`/`actual` ради выравнивания заголовка — низкий

`presentation/platform/SupportChatPlatform.*` (4 файла), использование —
`SupportChatScreen.kt:103`, `:246-250`

`isSupportChatDesktopPlatform()` нужен ровно для выбора `CenterAligned` / `Small` у топбара.
`patterns.md`: «`expect`/`actual` — только для по-настоящему платформо-специфичного кода».
Выравнивание заголовка зависит от ширины окна, а не от платформы: рядом уже вычислен
`layoutMode` (`:141-145`), который выражает это точнее — на Android-планшете экран тоже широкий.

**Доработка:** заменить на `layoutMode`, четыре `expect`/`actual`-файла удалить.

### U-16. `NavDisplay` ради одного экрана — средний, требует решения

`SupportChatNavGraph.kt:24-76`

Граф фичи состоит из одного маршрута: back stack из одного элемента, `entryProvider` с одной
записью, отдельный `SavedStateConfiguration`. `navigation.md` требует собственный граф от фичи
**с несколькими экранами**; здесь он избыточен.

Но решение связано с **S-0**: именно `NavEntry` даёт per-entry `ViewModelStoreOwner`. Два
варианта:

1. Оставить граф фичи и подключить декоратор в нём — фича самодостаточна, но остаётся вложенный
   `NavDisplay` ради одного экрана.
2. Убрать граф фичи, оставить экран entry внутри `MainNavGraph` (`composeApp`) и подключить
   декоратор там — проще, per-entry scope работает так же; при появлении второго экрана в чате
   граф вернуть.

Рекомендую второй вариант, но решение принимать вместе с S-0.

## Локализация и строковые ресурсы

Базис: `docs/localization.md`. Правила 6 («Не склеивать UI-текст в коде») и 7 («Технические
строки — не в ресурсах, но и не inline») добавлены в документ в рамках этого ревью — раньше
их не было, поэтому пункты ниже фиксируются как доработки, а не как нарушение существовавших
договорённостей.

### U-21. Склейка UI-текста в индикаторе набора — средний

`SupportChatTypingIndicator.kt:45`

```kotlin
text = "$author · ${stringResource(Res.string.support_chat_typing)}"
```

Разделитель и порядок слов зашиты в коде — в другом языке они другие или не нужны.
`localization.md`, правило 6: один видимый текст = один ключ с плейсхолдером.

**Доработка:** ключ `support_chat_typing_author` = `%s is typing…` / `%s печатает…`,
вызов `stringResource(Res.string.support_chat_typing_author, author)`; ключ
`support_chat_typing` удалить как осиротевший.

### U-22. Единицы размера файла — литералами в коде — средний

`SupportChatMessageBubble.kt:456-459`, `:465`

```kotlin
sizeBytes < kilobyte -> "$sizeBytes B"
sizeBytes < megabyte -> "${sizeBytes / kilobyte} KB"
… formatOneDecimal(sizeBytes, megabyte, "MB")
```

`B`/`KB`/`MB`/`GB` — видимый пользователю текст, он переводится (`Б`/`КБ`/`МБ`/`ГБ`).
Правило 6 распространяется и на единицы измерения.

Там же `formatOneDecimal` (`:465`) собирает дробь через точку — в русской локали десятичный
разделитель запятая.

**Доработка:** ресурсы `support_chat_size_b` / `_kb` / `_mb` / `_gb` вида `%s KB`;
`formatFileSize` становится `@Composable`, в Kotlin остаётся только арифметика. Разделитель
дроби — либо в тот же ресурс, либо через платформенное форматирование чисел.

### U-23. Разделитель метаданных документа литералом — низкий

`SupportChatMessageBubble.kt:448`

`joinToString(separator = " · ")` для пары «размер · тип».

**Доработка:** ресурс `support_chat_label_document_meta` = `%1$s · %2$s`; когда известна только
одна часть, разделитель не должен появляться — это разные ветки, а не `joinToString`.

### U-24. Технические строки написаны inline — низкий

`SupportChatMessageBubble.kt:417` — `localPath?.let { "file://$it" }`
`SupportChatScreen.kt:389` — `"typing_${item.author}"`

Правило 7: URI-схемы и префиксы ключей `LazyColumn` в ресурсы не выносятся, но и не пишутся
литералом в месте вызова. Рядом в том же файле `DATE_KEY_PREFIX` и `MESSAGE_KEY_PREFIX`
(`SupportChatScreen.kt:517-518`) уже вынесены в `private const val` — `typing_` просто забыли.

**Доработка:** `private const val FILE_URI_SCHEME = "file://"`, `TYPING_KEY_PREFIX = "typing_"`.

### U-25. Ключи не соответствуют конвенции именования — средний

`composeResources/values/strings.xml`, `values-ru/strings.xml` — все 46 ключей

Конвенция `localization.md`: `{feature}_{screen}_{type}_{name}`. В ключах чата сегмент
`{screen}` пропущен: `support_chat_title_screen`, `support_chat_a11y_back`. Остальные модули
конвенцию соблюдают — `login_login_title_screen`, `main_drawer_error_logout_failed`,
`authstate_root_title_error`.

**Доработка:** переименовать по образцу `supportchat_chat_title_screen`,
`supportchat_common_a11y_back`. Правка механическая: два XML плюс импорты в восьми
Kotlin-файлах, но затягивать не стоит — чем больше ключей, тем дороже.

### U-26. Дата собирается конкатенацией из двенадцати ключей — средний

`SupportChatScreen.kt:124-137` (12 `stringResource` месяцев), `:492-509` (`formatDate`)

Название месяца выбирается по индексу из списка, дальше `buildString` вручную клеит день,
месяц и год через пробелы. Это и правило 6 (порядок слов и разделители — часть перевода:
в английском это «14 March», а не «14 марта»), и дублирование — следующий экран с датами
заведёт свои 12 ключей.

**Доработка:** форматирование дат вынести в `core/*` общим хелпером; в фиче оставить вызов.
Ключи месяцев переедут туда же.

---

## Чистота кода

### U-17. Полностью квалифицированные имена в сигнатуре — низкий

`SupportChatLoadingSkeleton.kt:65`, `:67`

```kotlin
height: androidx.compose.ui.unit.Dp,
color: androidx.compose.ui.graphics.Color,
```

`patterns.md`: «Импорты — только сверху файла. Полностью квалифицированные имена в теле кода
запрещены». Конфликта имён здесь нет.

### U-18. Пустые тела `{}` у `CmpBox` в четырёх местах — низкий

`SupportChatEmptyState.kt:181-182`, `SupportChatLoadingSkeleton.kt:53`, `:86`,
`SupportChatTypingIndicator.kt:101-102`

Нужен просто закрашенный прямоугольник, но `CmpBox` требует content-лямбду. Признак нехватки в
`core/ui` варианта «box без контента» (или `CmpSpacer` с фоном).

### U-19. Wallpaper пересчитывает раскладку тайлов при каждой перерисовке — низкий

`SupportChatWallpaper.kt:21-62`

Двойной `while` по тайлам 240dp × 6 фигур на тайл выполняется внутри `Canvas` на каждый проход
фазы Draw. Геометрия зависит только от размера и цветов.

**Доработка:** `Modifier.drawWithCache` — раскладка считается один раз на размер.

### U-20. Ретрай по клику на пузырь без доступной семантики — низкий

`SupportChatMessageBubble.kt:89`

`Modifier.clickable { onRetry(message.id) }` — без `onClickLabel` и без роли. Для скринридера
это безымянная кликабельная область; понять, что тап повторит отправку, невозможно. Строка
`support_chat_a11y_retry` в ресурсах уже есть.

---

# Итоговая сводка

Всего 57 пунктов + 4 принятых решения.

| Приоритет | Пункты |
|---|---|
| Высокий | D-1, D-2, D-3, D-9, A-3, A-4, P-1, P-2, P-4, P-7, P-10, P-20, U-1, U-2, X-1 |
| Средний | D-4, D-5, D-6, D-7, D-8, A-1, A-2, A-5, A-6, A-8, A-11, P-3, P-5, P-6, P-8, P-9, P-11, P-12, P-13, P-18, U-3, U-4, U-5, U-7, U-8, U-9, U-10, U-11, U-12, U-16, U-21, U-22, U-25, U-26 |
| Низкий | A-7, A-9, A-10, P-14, P-15, P-16, P-17, U-6, U-13, U-14, U-15, U-17, U-18, U-19, U-20, U-23, U-24 |

Принятые решения: **S-0** (per-entry ViewModel scope в nav3), **S-1** (форма state на
`LCEState`), **S-2** (сборка state — вариант B, без `WhileSubscribed`), **S-3** (open/close на
`onStart`/`onCompletion` — отклонено).

**Затрагивают `core/*`, а не фичу:** U-1, U-2 (Coil и `CmpAsyncImage`), U-4 (полноэкранный
диалог), U-5 (обёртки списков или явное исключение в `ui-kit.md`), U-6 (типографические роли в
`CmpText`), A-3/A-11/X-1 (account-репозиторий, `AuthCloseable`, `LogoutUseCase`). Их стоит
вынести в отдельные задачи по `core/*` — они пригодятся следующим фичам.

**Рекомендуемый порядок работ:**

1. **A-3 + A-11 + X-1** — account-репозиторий, `AuthCloseable`, `LogoutUseCase`. Снимает гонку и
   дублирование, закрывает A-4, даёт app-scoped точку очистки для шага 2.
2. **S-0 + P-1 + P-2 + P-3 + U-16** — per-entry ViewModel scope, перенос сессии в
   `init`/`onCleared`, удаление полей-`Job` и `DisposableEffect`; заодно решить судьбу
   вложенного `NavDisplay`.
3. **D-9 + A-1** — контракт истории с гарантированным финалом и дедупликация репозитория.
4. **S-1 + P-4 + P-5 + P-20** — форма state на `LCEState`, полноэкранная ошибка первой загрузки,
   разблокировка композера. Самая заметная для пользователя группа.
5. **D-3 + P-7 + P-9** — единая модель вложения, устранение ложного статуса «отправлено»,
   FIFO в сопоставлении эха.
6. **U-1 + U-2 + U-3** — вынос Coil в `core/ui`, `CmpAsyncImage`. Отдельная задача по
   дизайн-системе.
7. **U-7 + U-8 + U-9 + U-10** — производительность и поведение списка: порядок в state, typing
   отдельным item, условный автоскролл, `reverseLayout`.
8. **U-21 … U-26** — локализация: фразы целиком вместо склейки, единицы размера в ресурсы,
   технические строки в `const val`, переименование ключей по конвенции, вынос форматирования
   дат в `core/*`. Делать одной задачей — все пункты трогают одни и те же файлы.
9. **D-1 + D-2 + U-11 + U-12**, затем остальные средние и низкие.

**Что в порядке и правок не требует:** репозиторий не бросает исключения и возвращает
`Response`; `withContext` внутрь репозитория не затащен; SDK-типы за пределы data-слоя не
текут; DI-модуль регистрирует только зависимости своей фичи; все ViewModel-корутины идут
через `safeLaunch`, `AppDispatchers` инжектится, хардкода `Dispatchers.*` нет; `_state`
обновляется только через `update {}`; one-shot события — типизированный `sealed interface`
через `sideEffectFlow`/`collectSideEffect`; ожидаемые ошибки логируются `logger.d`, не
`logger.e`; ViewModel покрыт 32 тестами; каркас экрана собран через `CmpScaffold` +
`CmpTopAppBar` по `ui-kit.md`; `contentDescription` проставлены осмысленно (декоративные
иконки — `null`); строковые ресурсы в паритете EN/RU, неиспользуемых ключей нет.

**Замечание за пределами модуля:** `core/ui/.../dialog/CmpDialog.kt:29` содержит
полностью квалифицированное имя `androidx.compose.material3.AlertDialogDefaults.shape` в теле —
то же нарушение правила импортов, что U-17.
