# Сессия чата: новый репозиторий и ViewModel

**Статус:** план **одобрен** 2026-09-30, реализация.
**Заменяет:** реализационную часть «Этапа 14» из `support-chat-rework.md`. Первая реализация
получилась переусложнённой: поколения сессии, `@Volatile`, флаги пагинации в репозитории.
**Контекст:** ветка `feature/support-chart`; сейчас код фичи в переходном состоянии и не собирается.
Domain-модели `SupportChatConnectionState` и `SupportChatEvent.Connection` уже заведены.

Статусы: [ ] не начато · [x] готово · [~] частично

---

## Принципы (замечания пользователя)

1. **Репозиторий — тонкий.** Только обёртка над SDK: подписка, вызов, маппинг. Никаких поколений
   сессии, `@Volatile`, флагов «что делать дальше». В приложении одна сессия; полную очистку при смене
   пользователя гарантирует `release()` (`LogoutUseCase`).
2. **Процессом управляет ViewModel.** Когда открыть сессию, когда и сколько раз переподключиться,
   когда читать историю, когда сбросить пагинацию — решает VM.
3. **Профиль пользователя — часть сессии.** Сессия получает профиль один раз при открытии и держит
   его как неизменяемое значение своей области. Поля-кэша в репозитории нет.
4. **Страница истории — событие сессии.** В рамках сессии слушаем события сокета. Страница истории
   приходит тем же потоком как `SupportChatEvent.HistoryPage`, и её не нужно ждать отдельным
   suspend-вызовом с ожиданием внутри репозитория.

---

## Этап 1 — Domain-контракт

- [x] `SupportChatConnectionState` (уже есть): `Connecting` → `Connected` → `Disconnected(OpenFailed | Lost | AuthFailed)`.
- [x] `SupportChatEvent`:
      `Connection(state)`, `HistoryPage(messages, hasMore)` (новое), `MessageReceived`, `MessagesRead`,
      `TypingChanged`.
- [x] `SupportChatRepository`:
      - `fun session(): Flow<SupportChatEvent>` — одна подписка = одна сессия: подписка открывает
        чат, завершение или отмена закрывает. `Disconnected` — финал, после него поток завершается.
      - `suspend fun requestHistoryPage(): Response<Unit>` — только запрос следующей страницы; сама
        страница приходит событием `HistoryPage` в активной сессии (см. вопрос 1).
      - `sendMessage` / `sendImage` / `sendDocument` / `markMessagesAsRead` / `downloadFile` —
        без изменений.
      - `release()` — закрыть текущую сессию (логаут).
- [x] Удалить: `observeEvents`, `openChat`, `closeChat`, `loadNextMessages`, `SupportChatHistoryPage`
      (если станет не нужен), `SupportChatSessionId`, `appScopeQualifier` из VM.

## Этап 2 — Репозиторий (пишется заново)

Поля: `mutex` (сериализует открытие и закрытие) — и всё.

- [x] `session()` = `channelFlow`:
      1. `Connection(Connecting)`.
      2. Профиль: `accountRepository.currentProfile()`. Ошибка → `Disconnected(OpenFailed)`, конец.
         Профиль — локальный `val` сессии, его захватывает маппер событий.
      3. Подписка на события SDK **до** `openChat` (`launch(start = UNDISPATCHED)`) — события без
         replay не теряются.
      4. `setChatType` + `openChat(resetPages = true)` под `mutex`. Ошибка → `Disconnected(OpenFailed)`.
      5. Маппинг событий SDK:
         - `ChatRoom.SubscribeSuccess` → `Connection(Connected)` (один раз);
         - `Chat.MessagesPage` → `HistoryPage`;
         - `Chat.Message` → `MessageReceived`;
         - `MarkMessageAsRead` → `MessagesRead`;
         - `Writing.*` → `TypingChanged` (`isMine` — по профилю сессии);
         - `Connection.Disconnected` и `Response.Error` → `Disconnected(Lost)`;
         - `Error.Auth` → `Disconnected(AuthFailed)`.
         Финал эмитится один раз (шторм ошибок iOS → один `Disconnected`), после него поток завершается.
      6. Нет `SubscribeSuccess` за 10 с после `openChat` → `Disconnected(OpenFailed)`.
      7. `finally { withContext(NonCancellable) { closeChat } }` под `mutex`.
- [x] `requestHistoryPage()` — вызов SDK-запроса страницы (вопрос 1), `Response<Unit>`.
- [x] `release()` — `closeChat` под `mutex`.
- [x] Маппер сообщений, `FileDownloader`, отправка — переиспользуются как есть.

## Этап 3 — ViewModel: сессия

- [x] Цикл сессий: `runSessions()` собирает `session()` на `dispatchers.io`. Поток завершился — пауза
      backoff 1 → 2 → 4 → 8 → 15 с (сброс после `Connected`), затем новая подписка. `AuthFailed`
      останавливает цикл до повторного входа в чат.
- [x] Жизненный цикл: `ON_STOP` — сокет держим, новые попытки на паузе; `ON_START` — если нет
      соединения, попытка сразу. Уход с экрана (отмена `viewModelScope`) закрывает сессию через
      репозиторий. `onCleared`, `appScope` и `yield()` в `init` не нужны.
- [x] UI-состояние соединения — прямое отображение `Connection(state)`: `Connected` → `Online`,
      `Disconnected` → `Offline` (+ `hasConnectionIssue`, `isChatAuthFailed`). Плашка — как сейчас.
- [x] По `Disconnected`: `requeueUnconfirmed` (неподтверждённые `Sent` → `Pending`).

## Этап 4 — ViewModel: история и пагинация

- [x] Пагинация принадлежит сессии и живёт в VM: на каждом `Connected` сбрасывается
      (`isInitialHistoryLoaded = false`, `hasMoreHistory = true`) — SDK открывает чат с
      `resetPages = true`, и первая страница — снова самая новая.
- [x] Первая страница — по `Connected`: VM вызывает `requestHistoryPage()`. Подгрузка — по скроллу,
      как сейчас (`onLoadMore`).
- [x] Страница приходит событием `HistoryPage` → слияние с дедупликацией и подтверждением
      оптимистичных сообщений (`SupportChatTimeline.withHistoryPage`), `isLoadingMore = false`,
      `hasMoreHistory = hasMore`. Опоздавшая страница просто сливается — дыр нет, отдельный сброс
      пагинации не нужен.
- [x] Гарантированный финал запроса — в VM: `isLoadingMore` снимается по `HistoryPage`, по
      `Disconnected` или по таймауту 15 с (тост / полноэкранная ошибка — как сейчас). Один запрос
      за раз: пока ждём страницу, новый не шлём.
- [x] Очередь неотправленных уходит после первой страницы сессии: сопоставление с историей не даст
      повторить уже дошедшие сообщения.

## Этап 5 — Тесты

- [x] Фейк репозитория: управляемый `session()` (Connecting / Connected / Disconnected(reason)),
      `HistoryPage` как событие, счётчики сессий и закрытий.
- [x] VM, соединение: `Connected` только по подписке комнаты; обрыв → backoff → новая сессия;
      повторный `Disconnected` не множит попытки; пауза на `ON_STOP`, немедленная попытка на
      `ON_START`; `AuthFailed` останавливает цикл; отмена `viewModelScope` закрывает сессию.
- [x] VM, история: первая страница по `Connected`; сброс пагинации на новой сессии; опоздавшая
      страница сливается; таймаут страницы снимает загрузку; страница в `Disconnected` не зависает.
- [x] Остальные тесты VM (отправка, outbox, эхо, скачивание, открытие) — адаптировать без изменения
      смысла.

## Этап 6 — Проверка и документация

- [x] `:feature:support-chat:jvmTest` (84 ✓), сборка Android/JVM/iOS ✓.
- [~] Эмулятор (2026-10-08): прокрутка истории ✓ (страницы по одной, без дублей); обрыв сети → плашка,
      ввод заблокирован, после возврата сети переподключение ✓; Home / пикер > 5 с → сокет рвёт ОС,
      возврат на экран → переподключение сразу ✓. **Выход из чата → повторный вход: 10 с «Подключение…»**
      (см. этап 7).
- [x] `docs/patterns.md`: правило «запрос с асинхронным ответом возвращает результат» уточнить —
      допускается ответ событием внутри долгоживущей сессии, если финал гарантирует владелец
      процесса (VM: событие / `Disconnected` / таймаут).
- [x] `.ai/agents/feature/support-chat/AGENTS.md` → `.ai/generate.sh`.

---

## Ответы на вопросы (2026-09-30)

1. Следующую страницу запрашиваем SDK `getNextMessagesUseCase` как триггером; страница приходит
   событием `HistoryPage` активной сессии.
2. Профиль отдаём наружу: `SupportChatConnectionState.Connected(profile)`.

## Отклонения по ходу реализации

- `SupportChatEvent.HistoryPage` — сообщения и `hasMore` прямо в событии; `SupportChatHistoryPage` удалён.
- Таймаут ожидания страницы — во VM (15 с), номер запроса `historyRequestId` в state: старый таймаут
  не снимает новый запрос. Очередь неотправленных уходит после первой страницы сессии — успешной
  или упавшей по таймауту/ошибке.
- Тесты VM обёрнуты в `runChatTest` (отменяет скоупы VM в конце): цикл сессий бесконечен, и упавшая
  проверка иначе зацикливала завершение `runTest` в виртуальном времени.
- Доп. на ревью: `SpecialistSupportChatRepository` покрыт только косвенно (тестов с SDK-фейками нет).

## Этап 7 — Находка эмулятора: «залипший» сокет SDK — не нужен, исправлено в SDK 1.2.0 (P0-1), см. `support-chat-sdk-1.2.0-migration.md`

**Симптом.** Выйти из чата → снова войти: 10 с «Подключение…» → `OpenFailed` → со второй попытки
подключение за ~150 мс. Воспроизводится стабильно.

**Причина (SDK 1.1.76, байткод).** На обрывы сети SDK не переподключается. Переподключение
(`disconnect` → `delay(2000)` → `connect`) запускается, только если сокет закрылся без ошибки при
`isOpened == true`. Но `SpecialistWsApiImpl.close()` сначала делает `disconnect()` и лишь потом ставит
`isOpened = false` → SDK принимает наше же закрытие за обрыв и через ~2 с поднимает сокет с подпиской
(лог: выход 11:33:36 → `wss` + `subscribe_success` 11:33:38). Следующий `openChat`:
`WebSocketImpl.connect()` пропускает подключение при `state == Connected` → `subscribe` не уходит →
`SubscribeSuccess` нет → наш таймаут 10 с. Подробно — `specialist-sdk-chat-fixes.md`, P0-1.

**Фикс (наш код, минимальный).** `SpecialistSupportChatRepository.session()`: под тем же `connectionMutex`
перед `setChatType` + `openChat` вызвать `closeChatUseCase` — каждая сессия начинается с чистого сокета,
`connect` гарантированно выполняет подключение и `subscribe`.
- Файлы: только `SpecialistSupportChatRepository.kt` (+ комментарий-причина), строка в
  `.ai/agents/feature/support-chat/AGENTS.md` (SDK-факт) → `.ai/generate.sh`.
- Фейк/тесты VM не меняются (контракт `session()` тот же).
- Проверка: `:androidApp:assembleDebug`, эмулятор — выход/вход в чат подключается сразу.

**Не лечится у нас:** «воскресший» сокет после выхода из чата (живёт до следующего `closeChat`/обрыва).
Нужен фикс в SDK: `isOpened = false` до `disconnect()` в `close()` (см. `specialist-sdk-chat-fixes.md`).
