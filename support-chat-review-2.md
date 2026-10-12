# Ревью №2 `feature/support-chat` — замечания и предложения по доработкам

**Модуль:** `feature/support-chat` + платформенные actual в `composeApp/src/*/filepicker`
**Ветка:** `feature/support-chart` (HEAD `f512e73`)
**Дата:** 2026-09-28
**Базис проверки:** обновлённые `docs/patterns.md` (разделы «Domain», «Состояние сессии», «Жизненный цикл ViewModel и сессии», «Корректность state»), `docs/compose-best-prac.md` §9, `docs/ui-kit.md`, `docs/localization.md`, `feature/support-chat/AGENTS.md`

Первое ревью (`support-chat-review.md`) в основном закрыто: per-entry VM, `AuthCloseable`/`LogoutUseCase`,
контракт истории с таймаутом, единая модель вложения, outbox, `reverseLayout`, Coil в `core/ui`,
локализация. Этот документ — **новые** находки по текущему коду; незакрытые пункты первого ревью
отмечены явно.

Нумерация: `P-*` — ViewModel/presentation, `A-*` — data, `U-*` — UI, `PL-*` — платформенные actual
(`composeApp`), `X-*` — кросс-модульные. Приоритеты — как в первом ревью.

Пути без префикса — от `feature/support-chat/src/commonMain/kotlin/ru/alarmtrade/specmp/feature/supportchat/`;
`VM` = `presentation/screen/supportchat/SupportChatScreenViewModel.kt`,
`Screen` = `.../SupportChatScreen.kt`, `Repo` = `data/repository/SpecialistSupportChatRepository.kt`.

---

# Высокий приоритет

## P-1. Ошибка первой загрузки стирает outbox — высокий, потеря данных

`VM:586-603`, `VM:525-541`, `SupportChatScreenState.kt:45-46`

Сценарий: в outbox есть неотправленные сообщения → новый вход → `restoreOutbox()` кладёт их в
`history = Content(...)` → `loadInitialHistory()` падает (таймаут 15 с на плохой сети — штатный случай).

1. Ветка ошибки безусловно пишет `history = LCEState.Error("")` (`VM:601`), хотя комментарий
   выше (`VM:587-588`) обещает не трогать `Content`. Восстановленные сообщения пропадают с экрана.
2. Геттер `messages` для `Error` возвращает `emptyList()`. Следующее любое событие —
   `updateMessageStatus` / `handleMessageReceived` — строит `Content(transform(empty))` и вызывает
   `persistOutbox()` → в хранилище пишется **пустой снимок**. Очередь потеряна безвозвратно.

Та же ветка затирает живое сообщение, пришедшее во время первой загрузки (обратный порядок к тесту
`live message replaces fullscreen error with content`).

Правило: `patterns.md` → «Корректность state»: «ошибка первой загрузки — `Error` только когда
показывать нечего; при `Content` — тост».

**Доработка:** в `loadInitialHistory` при `history is Content` — `HistoryLoadFailed` тостом, ленту
не трогать. Отдельно: `persistOutbox()` не должен работать от производного `messages` в состоянии
`Error/Loading` — outbox обновляется только из явных переходов статуса. Тест: «restore outbox →
initial load error → outbox и лента сохранены».

## P-2. Гонка пагинации с начальной загрузкой — высокий, ложная ошибка и вход в P-1

`VM:209-224`, `Screen:157-170`, `Repo:122-134`

При восстановленном outbox `history` уже `Content` на первой композиции, лента короткая,
`canScrollForward == false` → `onLoadMore()` вызывается сразу, пока `init` ещё выполняет `openChat()`.
Дальше:

- `loadNextMessages()` от `onLoadMore` уходит в SDK **до открытия чата**;
- `loadInitialHistory()` получает от репозитория `Error("History page request is already in progress")`
  → `history = Error` → срабатывает P-1.

Та же проблема у `onHistoryRetry()` (двойной тап) и `onConnectionRetry()`: все вызывающие
`loadNextMessages()` не сериализованы между собой, а репозиторий отвечает на параллельный запрос
ошибкой, которую VM трактует как сбой загрузки.

**Доработка:** единый путь загрузки страницы под `executeWithProceedLock`; `onLoadMore` разрешён
только после завершения первичной загрузки (признак в state, а не `history is Content`);
«уже выполняется» — не ошибка для UI (skip), а не `Response.Error`.

## P-3. Композер заблокирован офлайн, а баннер обещает отправку — высокий, UX

`Screen:399` (`enabled = !current.isOffline`), `strings.xml` → `supportchat_chat_banner_offline_subtitle`

Баннер: «Сообщения будут отправлены при восстановлении связи». Но при `isOffline` выключены и поле
ввода, и вложения — написать сообщение офлайн нельзя. Весь путь `Pending` → outbox достижим только
через гонку (связь пропала между нажатием и отправкой); тест
`offline message is persisted to outbox...` вызывает `onSend` в обход UI.

Правило: `patterns.md` → «UI-текст не обещает несуществующего поведения».

**Доработка (продуктовое решение):** либо разрешить ввод офлайн (сообщение сразу `Pending` —
для этого outbox и делался), либо поменять текст баннера и удалить неиспользуемую ветку.
Рекомендую первое.

## P-4. Двойной ретрай отправляет сообщение дважды — высокий

`VM:197-207`, `SupportChatMessageBubble.kt:95-99`, `Screen:96-99`

`onRetry` ищет сообщение только по `id && isMine`, статус не проверяет и в `Sending` переводит уже
внутри `safeLaunch`. Два быстрых тапа по Failed-пузырю, тап по пузырю + action снекбара, или
`onRetry` параллельно с `retryPendingMessages` → два `sendMessage` → у поддержки дубль.

Правило: `patterns.md` → «Check-then-act на `_state.value` не атомарен».

**Доработка:** атомарный переход `Failed → Sending` внутри одного `update {}`; ретрай выполняется,
только если переход состоялся. Ретраи — через тот же proceed-lock, что и `retryPendingMessages`.

## U-1. Дубликат ключа `LazyColumn` → краш — высокий (воспроизводимость зависит от SDK)

`VM:355-379`, `Screen:349-353`

`handleMessageReceived` заменяет существующее сообщение по `serverId` **только для `isMine`**.
Входящее сообщение, которое уже есть в ленте (пришло страницей, затем повторно live-событием —
например, SDK повторно доставляет после reconnect/`resetPages`), дописывается в конец →
два элемента с одним `id` → `items(key = "msg_$id")` бросает
`IllegalArgumentException: Key ... was already used`.

**Доработка:** дедупликация по `serverId` для любых сообщений (замена, а не добавление).
Тест на повторное live-событие уже известного входящего.

## PL-1. Android: копирование выбранного файла на main thread — высокий, ANR

`composeApp/src/androidMain/.../rememberSupportChatFilePicker.kt:27-39`, `:60-93`

`uri.toPickedFile(context)` вызывается прямо в activity-result callback (main) и делает
`input.copyTo(output)` целиком. Видео/архив на сотни МБ → ANR. Лимита размера на отправку нет.

**Доработка:** копирование в фоне (callback отдаёт `Uri`, копия — в suspend-функции на
`dispatchers.io`), лимит размера с внятной ошибкой до копирования (`OpenableColumns.SIZE`).

---

# Средний приоритет

## P-5. Снимок outbox пишется с двух потоков без сериализации — средний, дубль после рестарта

`VM:389` (из коллектора на `dispatchers.io`), `VM:522` (с main), `VM:539-541`

`persistOutbox()` читает `_state.value` и пишет снимок целиком. Эхо сервера (io) удаляет запись,
параллельно `updateMessageStatus` (main) пишет **более старый** снимок, где запись ещё есть →
после рестарта сообщение отправится повторно. Плюс синхронный диск на main: `restoreOutbox()` в
`init` и запись на каждый переход статуса.

Правило: `patterns.md` → «Персистентный read-modify-write не атомарен».

**Доработка:** outbox обновлять под `Mutex` на `dispatchers.io`, источником истины держать state;
загрузку в `init` — через `safeLaunch` на io до открытия сессии.

## P-6. ViewModel зависит от data-слоя; `Settings()` создаётся в фиче — средний

`VM:28-30`, `di/SupportChatModule.kt:19`, `data/outbox/SupportChatOutboxStore.kt`

- Presentation импортирует `data.outbox.SupportChatOutboxStore/Entry/toOutboxEntries`. Контракт
  хранилища — в `domain`, DTO и маппинг `SupportChatMessage ↔ Entry` — целиком в data
  (сейчас половина маппинга — `toMessage()` в VM, `VM:543-573`).
- `SettingsSupportChatOutboxStore(Settings())` — фича создаёт дефолтный app-wide `Settings`.
  Общие settings — app-level инфраструктура (`patterns.md` → «Core»): инстанс/фабрика должны
  приходить из `core/*`, фича получает узкую зависимость.

## P-7. Guard скачивания пропускает повторный тап по тому же вложению — средний

`VM:132-139`

После `update` проверка `downloadingAttachmentId != attachment.id` для второго тапа по **тому же**
вложению ложна (id совпадает) → стартует второе скачивание. Итог: два файла в Downloads (Android)
или перезапись (Desktop), двойной снекбар. Комментарий утверждает обратное.

**Доработка:** флаг «этот вызов захватил слот» из самого `update` (например, сравнить `previous` и
`next`) или `executeWithProceedLock`.

## A-1. Лимит скачивания не защищает от OOM; пустые байты = «файл сохранён» — средний

`Repo:178-196`; сейверы `androidMain`/`jvmMain` `rememberSupportChatFileSaver.kt`

- Размер проверяется **после** того, как SDK уже положил весь файл в `ByteArray` — от OOM лимит не
  спасает. Проверять до загрузки (`sizeBytes` из метаданных вложения, когда SDK их отдаст) либо
  честно убрать комментарий-обещание.
- Известный баг SDK (404 → `Success` с пустыми байтами) обрабатывает только iOS-сейвер. Android и
  Desktop сохраняют 0-байтовый файл и показывают «Файл сохранён». `AGENTS.md` утверждает, что
  «сейвер пустые байты отклоняет» — это верно только для iOS.

**Доработка:** пустой ответ → `Response.Error` в репозитории (граница с SDK), сейверы об этом не знают.

## A-2. Поздняя страница истории выбрасывается — средний, дыра в истории

`Repo:147-157`, `Repo:232-237`

По таймауту awaiter снимается, а пришедшая позже `MessagesPage` логируется как «dropped». Курсор
пагинации SDK при этом уже сдвинут: следующий `loadNextMessages()` вернёт более старую страницу,
пропущенная так и не появится.

**Доработка:** при таймауте сбрасывать пагинацию (`resetPages`) или мерджить позднюю страницу
отдельным событием. Минимум — зафиксировать риск в KDoc контракта.

## P-8. Коллектор событий не перезапускается при ручном реконнекте — средний (P-18 первого ревью не закрыт)

`VM:230-248`, `VM:295-305`; `core/common/.../ResponseExt.kt:196-206`

`safeFlowRun { … }.catch` завершает поток при исключении SDK-flow. `onConnectionRetry()` повторяет
только `openChat()` — баннер снимается, событий больше нет. Правило добавлено в
`patterns.md` → «Flow-цепочки — без ручных обёрток».

**Доработка:** перезапуск `startEventCollection()` при реконнекте (с отменой старого Job) либо
`retryWhen` в репозитории.

## P-9. Failed-сообщение нельзя удалить — средний, UX

`VM:197-207`, `VM:466-469`, `data/outbox/SupportChatOutboxStore.kt:67-98`

Failed живёт в outbox бесконечно, переживает рестарты. Если локальный файл пропал (см. PL-4),
ретрай даёт `missingLocalFile` → снова Failed → снова снекбар «Повторить», и так без конца.
Удаления нет ни в UI, ни в VM.

**Доработка:** действие «Удалить» для Failed (long-press или меню пузыря); для «файл отсутствует» —
не предлагать Retry.

## P-10. Отправка до открытия сессии — средний

`SupportChatScreenState.kt:31` (`isOffline = false` по умолчанию), `VM:82-94`

До завершения `openChat()` композер активен, а `sendMessage` уходит в незакрытое соединение →
Failed + снекбар ошибки на первом же сообщении при медленном старте.

**Доработка:** состояние соединения тремя значениями (`Connecting / Online / Offline`) вместо
`Boolean`; в `Connecting` сообщение становится `Pending` и уходит после открытия.

## U-2. Пагинация «застревает» — средний

`Screen:157-170`

Триггер — `snapshotFlow { canScrollForward }.distinctUntilChanged()`: срабатывает только на
**смене** значения. Если подгруженная страница не заполнила экран (планшет, Desktop, короткие
сообщения) или загрузка упала — значение остаётся `false`, нового триггера нет, пока пользователь не
поскроллит. Плюс `state.value` читается вне снапшота: изменения `hasMoreHistory`/`isLoadingMore`
триггер не перезапускают.

**Доработка:** снапшот из `(canScrollForward, hasMoreHistory, isLoadingMore, messages.size)`;
запуск при `!canScrollForward && hasMore && !isLoadingMore`.

## PL-2. Имя файла с сервера не санитизируется — средний, безопасность

`jvmMain/.../rememberSupportChatFileSaver.kt:15-18`, `androidMain/...FileSaver.kt:59-64`,
`iosMain/...FileSaver.kt:101-102`

`attachment.name` приходит с сервера. `File(dir, fileName)` на Desktop и на Android API < 29
допускает `../` → запись вне `Downloads/Pandora Spec`; одноимённый файл молча перезаписывается.
На MediaStore система переименует дубль (`name (1).pdf`), а в снекбар уходит исходное имя. При сбое
записи в MediaStore остаётся пустая запись (нет `IS_PENDING` и удаления `uri`).

**Доработка:** санитизация имени (basename, запрещённые символы) и уникализация в общем хелпере;
MediaStore — `IS_PENDING` + `delete(uri)` при ошибке, путь брать из фактической записи.

## PL-3. Android: утечка Activity через сейвер; потеря результата пикера — средний

`androidMain/...FileSaver.kt:17-18`, `navigation/SupportChatNavGraph.kt:37`,
`androidMain/...FilePicker.kt:24-25`

- Сейвер захватывает `LocalContext` (Activity) и передаётся в конструктор VM через `parametersOf`.
  Per-entry VM переживает пересоздание Activity (смена системной локали/шрифта) → держит старую
  Activity. Нужен `applicationContext`.
- Колбэк пикера хранится в обычном `remember`. Если Activity/процесс пересоздались, пока открыта
  галерея (частый случай на слабых устройствах), колбэк `null` — выбранный файл молча теряется.

## PL-4. Временные копии вложений: утечка кэша и хрупкий outbox — средний

`androidMain/...FilePicker.kt:66-71`

Копии в `cacheDir` не удаляются ни после отправки, ни при удалении чипа — кэш растёт. При этом
outbox ссылается на эти пути, а система вправе вычистить `cacheDir` до ретрая → восстановленное
`Pending` уходит в Failed «локальный файл отсутствует» (P-9).

**Доработка:** копия для outbox — в `filesDir/outbox`; удаление по `Sent`, по удалению чипа и в
`release()` outbox при логауте.

## X-1. `collectSideEffect` теряет эффекты во время снекбара — средний, `core/ui`

`core/ui/.../effect/SideEffect.kt:57-70`, `Screen:94-104`

Обработчик `suspend` и выполняется внутри `collect`: `showSnackbar` держит коллектор несколько
секунд, буфер `sideEffectFlow` — 1, `tryEmit` остальных возвращает `false`. Серия ошибок отправки
даст один-два снекбара; «Файл сохранён» теряется, если в этот момент висит ошибка.

**Доработка (core):** в обработчике снекбаров запускать показ в отдельной корутине или
буферизовать эффекты; зафиксировать поведение в `patterns.md` → «One-shot события».

---

# Низкий приоритет

## A-3. Маппер молча теряет неизвестный контент — низкий

`data/mapper/SupportChatMessageMapper.kt:28-44`

`(message as? Default)?.messageContent` и `else -> null` превращают неизвестный вариант SDK в пустое
сообщение без лога — ровно случай из `patterns.md` → «Маппинг не теряет данные молча». Также
`Document.mimeType/sizeBytes` не маппятся — проверить, отдаёт ли их SDK (нужны для A-1).

## A-4. Часовой пояс зафиксирован при создании синглтона — низкий

`Repo:45` против `VM:565`, `VM:627-629`, `SupportChatOutboxStore.kt:100-102`

Маппер получает `TimeZone` один раз на жизнь процесса, VM и outbox берут `currentSystemDefault()`
в момент вызова. Смена пояса во время работы → серверные и локальные сообщения сортируются в разных
поясах. Надёжнее хранить в domain `Instant`, а `LocalDateTime` вычислять в UI.

## A-5. Ручной `Response.Error` рядом с готовым хелпером — низкий

`Repo:78-81` дублирует `toProjectError()` (`Repo:262-267`).

## P-11. Закрытие старой сессии может закрыть новую — низкий

`VM:98-102`, `Repo:89-120`

`connectionMutex` сериализует, но не упорядочивает: при быстром повторном входе `openChat()` нового
VM может взять мьютекс раньше `closeChat()` старого (он идёт через `appScope`) — и новое соединение
будет закрыто без баннера. Лечится токеном/поколением сессии в репозитории.

## P-12. Отправка восстановленной очереди держится на событии `Connected` — низкий

`VM:575-584`, `VM:331-335`

После успешного `openChat()` в `init` ретрай очереди не вызывается — он случится, только если SDK
пришлёт `ConnectionChanged(true)`. Неявная зависимость; вызвать `retryPendingMessages()` явно.

## P-13. Sentinel в ошибке — низкий

`VM:601` — `LCEState.Error(error = "")`. Раз payload не читается, стоит завести осмысленное значение
или типизированную причину (`patterns.md` → «Отсутствие значения — `null`, а не sentinel»).

## U-3. State-conduit не работает — низкий, производительность

`Screen:251` — `SupportChatBody` читает `state.value` целиком в самом начале, поэтому typing,
`isLoadingMore`, `downloadingAttachmentId` рекомпозируют весь Body вместе с композером. Передача
`State<T>` смысла не имеет, пока чтение не опущено в потребителей (`compose-best-prac.md` §2, §5).

## U-4. Видимые id: аллокация на каждый кадр и «чужие» ключи — низкий

`Screen:174-184`

- `visibleItemsInfo.mapNotNull { … }` в `snapshotFlow` — аллокация списка на каждый кадр скролла
  (❌-пример из `compose-best-prac.md` §9).
- `removePrefix(MESSAGE_KEY_PREFIX)` не фильтрует `date_*`/`typing_*`: они уходят в VM как id
  сообщений. Нужен `startsWith`-фильтр.

## U-5. `SupportChatActions` без `remember`, лишний параметр `onSend` — низкий

`navigation/SupportChatNavGraph.kt:43-67` — actions собираются на каждую рекомпозицию entry с
инлайн-лямбдами (`onAttachFile`/`onAttachPhoto`) → `equals` всегда `false`; §9 требует
`remember(viewModel)`. `Screen:125`, `:148`, `:236` — `onSend: (String) -> Unit`, аргумент которого
игнорируется: колбэк сам берёт `draftText`.

## U-6. Пузырь с картинкой — низкий

`SupportChatMessageBubble.kt:377`, `:424-426`

- У Failed-картинки вложенный `clickable`: тап по картинке открывает просмотр, а не повторяет отправку.
- `imageSource()` при `url == null && localPath == null` отдаёт `""` — sentinel уходит в Coil и в
  просмотрщик.
- `FILE_URI_SCHEME + path` для Windows-пути (`C:\…`) — невалидный URI (нужен `file:///C:/…`),
  если Desktop под Windows в планах.

## U-7. Мелочи UI — низкий

- `SupportChatComposer.kt:48`, `:135` — `pendingAttachments: List<…>` вместо `ImmutableList`.
- `presentation/model/SupportChatUiMessage.kt:44-48` — время всегда `HH:mm`, EN-пользователь
  с 12-часовым форматом его не получит.
- `SupportChatMessageBubble.kt:79-85` — над входящим показывается email сотрудника: сверить
  с дизайном (приватность, читаемость).
- `SupportChatEmptyState.kt:68-82` — чипы «Документы» / «Фото монтажа» выглядят интерактивными, но
  ничего не делают: либо открыть соответствующий пикер, либо убрать chip-аффорданс.

## U-8. Ключи строк вне таблицы типов — низкий

`composeResources/values*/strings.xml`: сегменты `typing`, `chip`, `banner`, `snack`, `status`,
`size` отсутствуют в таблице типов `localization.md` (`title/btn/label/…/msg/a11y`). Либо
расширить таблицу, либо переименовать. В `composeApp` ключи пикера — `support_chat_picker_*`
без сегмента `{screen}`.

## U-9. Чистота кода — низкий

- Неиспользуемые импорты: `VM:3` (`ImmutableList`); `Screen:30-35` (`Clock`, `DatePeriod`,
  `TimeZone`, `minus`, `toLocalDateTime`).
- `SupportChatMessageBubble.kt:4-5` — импорты вразнобой; `:349-350` — пустые строки в начале тела.
- `jvmMain/.../rememberSupportChatFilePicker.kt:71-75` — сломанные отступы.

## PL-5. iOS-сейвер — низкий

`iosMain/.../rememberSupportChatFileSaver.kt`

Нет `invokeOnCancellation`: при отмене корутины `finally` удалит временный файл, пока пикер
экспорта ещё открыт. `activeExportDelegate` — глобальная `var`. Имя файла в temp-пути
без санитизации (PL-2).

## X-2. Расхождения в документации — низкий

- `feature/support-chat/AGENTS.md`: «вход через пункт drawer» против `docs/navigation.md`:
  «в drawer перехода в чат нет»; «размещается в корневом `NavDisplay` (`composeApp/AppNavigation`)»
  против таблицы маршрутов (`MainNavGraph`). Проверить и привести к одному.
- `AGENTS.md`: «сейвер пустые байты отклоняет» — только iOS (A-1).

---

# Тесты

В фиче 46 тестов (41 — на ViewModel), но найденное ими не покрыто (тесты в этом ревью не запускались). Добавить:

| Сценарий | Пункт |
|---|---|
| restore outbox → ошибка первой загрузки → лента и outbox сохранены | P-1 |
| `onLoadMore` во время первичной загрузки не даёт `Error` | P-2 |
| двойной `onRetry` → один `sendMessage` | P-4 |
| повторное live-событие известного входящего не дублирует элемент | U-1 |
| двойной `onDownloadAttachment` того же вложения → одно скачивание | P-7 |
| пустые байты скачивания → `AttachmentDownloadFailed` | A-1 |
| эхо (io) + смена статуса (main) → outbox без удалённой записи | P-5 |

---

# Итоговая сводка

| Приоритет | Пункты |
|---|---|
| Высокий | P-1, P-2, P-3, P-4, U-1, PL-1 |
| Средний | P-5, P-6, P-7, P-8, P-9, P-10, A-1, A-2, U-2, PL-2, PL-3, PL-4, X-1 |
| Низкий | A-3, A-4, A-5, P-11, P-12, P-13, U-3 … U-9, PL-5, X-2 |

**Рекомендуемый порядок:**

1. **P-1 + P-2 + P-5** — целостность outbox и единый сериализованный путь загрузки истории.
   Это одна группа: все три пункта про то, кто и когда пишет ленту и снимок очереди.
2. **P-4 + U-1 + P-7** — атомарные guard'ы и дедупликация (дубли сообщений, краш списка).
3. **P-3 + P-10 + P-9** — продуктовое решение по офлайн-вводу, состояние `Connecting`, удаление Failed.
4. **PL-1 + PL-3 + PL-4 + PL-2** — Android-пикер в фоне, applicationContext, жизненный цикл копий,
   санитизация имён.
5. **A-1 + A-2 + P-8** — граница с SDK: пустые байты, поздняя страница, перезапуск коллектора.
6. **X-1** — отдельной задачей в `core/ui`.
7. Остальные низкие.

**Что в порядке:** закрыты ключевые пункты первого ревью — per-entry VM с `init`/`onCleared` через
`appScope`, `AuthCloseable` и `AccountRepository` вместо кэша identity, контракт истории с
гарантированным финалом, единая модель вложения, типизированный результат сейвера и пикера
(`Cancelled` ≠ ошибка), FIFO-сопоставление эха, батчинг read-отметок, `reverseLayout`,
`drawWithCache`, `CmpAsyncImage`, `SupportChatActions`, фразы целиком в ресурсах, паритет EN/RU.
