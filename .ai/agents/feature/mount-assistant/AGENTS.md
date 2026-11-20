# feature/mount-assistant

- «Ассистент» (ИИ-монтаж v1, без LLM): раздел расширенного монтажа — журнал (лента) и пульт действий. Один обособленный
  модуль (domain + data + presentation, DI — `mountAssistantModule`): логика другим фичам не нужна. План и ход
  реализации — `.claude/tasks/ai-mount/ai-mount-plan.md` (раздел 11), макет — `design/features/assistant`,
  `design/changelog/ai-mount/assistant.md`. Слово «ИИ» в интерфейсе не используется.
- **Ядро — чистый Kotlin** (`domain/model`, `domain/engine`): каталог `AssistantAction` (тема, вид IN_FEED/HANDOFF,
  нужные возможности, сеть, «закрыто заводским CAN», только BLE), `availability` (порядок: модели нет в базе → нет
  возможности/не BLE → «Обновить прошивку» без найденного обновления → «Обновить CAN» без CAN → заводской CAN → нет
  сети), `topics`, `nextStep` (CAN → прошивка → статус обхода → обход → Clone → состояние → настройки / поддержка),
  `topicStatus`, `snapshotOf`/`diffTo`, `stemsOf`/`matchCatalog` (названия и ключевые слова — строки
  `mountassistant_search_value_keywords_<тема>`, ядро языка не знает). Работает от возможностей устройства
  (`DeviceFacts.capabilities`, `can`, транспорт, сеть), без допущения «это сигнализация»; «CAN не заводской» — только
  если CAN есть. Микробот и навигационный модуль — вне v1 (для них — свои адаптеры портов, ядро не меняется).
- **Порты** (`domain/repository`): `AssistantDeviceRepository` (реализация в модуле — сессия + `NetworkMonitor`,
  монитора нет → сеть доступна), `AssistantSettingsRepository` (в модуле, над `DeviceSettingsRepository`),
  `AssistantFirmwareRepository` / `AssistantAutostartRepository` / `AssistantStateRepository` /
  `AssistantProductRepository` — **адаптеры в `composeApp/assistant`** (`assistantPortsModule`): фича не зависит от
  обособленных `firmware`/`autostart`/`state`/`product-info`. Товар идёт через `AssistantDeviceRepository` (`product()`,
  флаг `DeviceFacts.productSearch` — функция аккаунта `PRODUCT_SEARCH`): конструктор VM уже на пределе Konsist (12).
  Модели портов — свои, типы других фич в домен ассистента не протекают.
- **Темы**: прошивка · обход и Clone · настройки · состояние · **устройства** (устройства системы — только BLE; приживление — `DYNAMIC_DEVICES`, закрыто заводским CAN; **информация о товаре** — `PRODUCT_INFO`, IN_FEED, нужна сеть и `productSearch`, от модели и CAN не зависит: карточка товара блока, «Подробнее» / «Искать по другому номеру» → `AssistantNavigationTarget.ProductInfo` → `ProductInfoRoute` поверх монтажа; номер из 6–19 цифр в поиске — первая строка «№…» (`DockTile.ProductNumber`, группа `PRODUCT`), Enter с номером — сразу на экран) · автомобиль · **поддержка** (заявка на техпомощь — первой, мои заявки, чат). Заявка — `AssistantNavigationTarget.TechRequestNew` → форма «Новая заявка» поверх монтажа (`composeApp`, `TechRequestsRoute(prefill)`), «Мои заявки» — раздел `TECH_REQUESTS`. Возврат без изменений после прошивки, CAN, обхода и Clone (`AssistantAction.NeedsHelpOnFailure`) показывает чипы «Заявка на техпомощь» и «Чат поддержки» (`FeedCallbacks.onAction`); «Ничего не найдено» предлагает их же. Устройства системы, приживление и заявки — «тихие» возвраты (снимок их не видит).
- **Паспорт монтажа** — `AssistantAction.MOUNT_PASSPORT` (HANDOFF, тема «Состояние», `standalone`: своя плитка
  пульта `DockTile.Passport` сразу после «Состояния», в списке темы и в совпадениях по теме его нет; без требований к
  возможностям, CAN, сети и транспорту; модели нет в базе — скрыт). Поиск — свои ключевые слова действия
  (`matchCatalog(…, actionKeywords)`, строка `mountassistant_search_value_keywords_passport`). `nextStep` — последнее
  звено перед «Найти настройку» / «Поддержка», пока сегодня нет оформленного паспорта (`MountProgress.passport` —
  `TodayPassport`, `ISSUED`); пока шаг — «Следующий шаг», плитка паспорта не показывается. Статус плитки —
  `passportStatus`. Данные — порт `AssistantPassportRepository` (адаптер `composeApp/assistant/AppAssistantPassportRepository`
  над `MountPassportRepository.lastOfUnitToday`, заготовка `BLANK` — не паспорт) через `AssistantDeviceRepository.passportToday`
  (конструктор VM — на пределе Konsist). Читается при каждом подключении блока и после возврата с паспорта.
  Карточка журнала `JournalEntry.Passport` (одна): маршрут дошёл до паспорта — «Монтаж закончен? Оформите паспорт»;
  оформлен — «Паспорт оформлен · №» + «Открыть» (и при возврате, если карточки не было). Переход —
  `AssistantNavigationTarget.MountPassport(passportId)`: `null` — `MountPassportReadRoute(AdvancedMount)`, иначе
  `MountPassportEditorRoute(id)`; возврат «тихий».
- **Журнал** (`AssistantJournal`, `data/service/AssistantJournalImpl`, Koin `single`) хранит записи, прогресс и
  ожидаемый возврат (`PendingHandoff` = действие + снимок «до») на время сессии блока: прошивка уводит граф
  расширенного монтажа из композиции. `bind(systemId)` — другой блок сбрасывает журнал, тот же — нет. Что писать,
  решает VM; экран всегда строится из журнала (VM после загрузчика бывает и новой, и прежней).
- **Возврат с другого экрана**: HANDOFF → шаг + `beginHandoff` + эффект `Navigate(AssistantNavigationTarget)` →
  `composeApp/navigation/AssistantWiring.kt` переводит цель в разделы `AdvancedMountNavGraph` (архив — `FirmwareArchiveEntry`
  модуля блока поверх ассистента; один модуль — сразу, несколько — выбор в карточке прошивки; «Установить» в архиве и
  кнопки карточки «Обновить до X» / «Перепрошить» (`FirmwareFlash`) открывают экран прошивок с `FirmwareAutoBoot` —
  сразу подтверждение загрузчика, версия уже выбрана; чат —
  `onOpenSupportChat`). Возврат ловит `LifecycleResumeEffect` → `onScreenResumed()`
  (событие, не стартовая загрузка), новая VM после загрузчика — `onEachDeviceReady`; граф после загрузчика
  стартует со стеком `[Root, ASSISTANT]` (`resumeAssistantTitle`). Итог — только «изменилось (до → после)» / «не
  изменилось»: экраны обхода, Clone и прошивки итог наружу не сообщают.
- **Настройки** — общая сессия `SettingsSource.Device`: `open(reload = false)`, ассистент её не закрывает во время
  работы, `discardSession(Device)` — в `onCleared` VM. Запись — `setValue` → `save`; неверный PIN приходит как
  `SettingsWrongPinException` (`device:settings`) → `AssistantWriteException(WRONG_PIN)` → `ServicePinDialog` →
  `setServicePin` → `save`; отмена после попытки — `revert` старого значения. SDK пишет сессию целиком: чужие
  несохранённые правки — предупреждением. «Открыть в настройках» — редактор на разделе настройки
  (`focusSettingId`, без прокрутки и подсветки строки), загрузка — редактор с меню «Загрузить из…» (`openLoadMenu`).
- UI (`presentation`): `MountAssistantEntry(onBack, onNavigate)` — каркас внутри записи графа; раскладки Compact
  (< 600) / Medium (600–839) — шапка блока, лента, пульт внизу каруселью; Expanded (≥ 840) — колонка 400 dp (шапка,
  плитки 2 колонки; при вводе не меняется) и лента до 760 dp со строкой поиска под ней; найденное — панель
  `SearchResultsPanel` над строкой поиска (строки по группам `resultGroups`, `testTag` `mountassistant_result_<ключ>`).
  Низкое окно (`CmpWindowSize.isLowHeight` из `core/ui`: высота < 480 при ширине ≥ 600, телефон-ландшафт) — раскладка
  `Low` поверх любой ширины: слева колонка пульта `min(340 dp, 44 %)` (шапка блока свёрнута в строку, раскрытие —
  UI-состояние `rememberSaveable`; «Следующий шаг» и темы — строки `TileForm.ROW` от 64 dp со скроллом), справа лента
  на всю высоту до 720 dp, строка поиска закреплена под лентой, найденное — `SearchResultsPanel` на всю ленту. Прокрутка ленты вверх > 60 dp сворачивает пульт до быстрых действий.
  Пульт — `dockOf` (`presentation/model/DockUi.kt`, чистая функция: дом / тема / поиск). Строки —
  `core/localization` `mountassistant.xml`. `testTag`: плитки `mountassistant_tile_<ключ>`, поле поиска
  `mountassistant_search`, стрелка `mountassistant_search_submit`, пульт `mountassistant_dock`, шапка
  `mountassistant_header`, карточки `mountassistant_card_<id>`, «Записать» `mountassistant_confirm_write`, «Далее»
  `mountassistant_editor_next`, источники `mountassistant_source_<file|mine|cloud|defaults>`.
- **Не проверено на железе**: возвращает ли `getCloneBypassState` итог обхода после переподключения; время первого
  `open(Device)`; автоматический возврат и «до → после» после прошивки; запись настройки при неверном сохранённом PIN.
