# Перенос фикса пикера (329db6f, А. Гужавин) поверх нашей версии

**Статус:** план **одобрен** 2026-10-09 (буфер Android — object; отдельный коммит с Co-authored-by), реализовано.
**Ветка:** `feature/support-chart`. В merge `85f8979` файлы `329db6f` оставлены в нашей версии — фикс не вошёл.

## Что чинит 329db6f

1. **Android, `GetContent` теряет выбор.** Часть галерей возвращает uri в `clipData`, а не в `intent.data` →
   `GetContent` отдаёт `null` при успешном выборе → «тихая отмена». Фикс: картинки — системный Photo Picker
   (`PickVisualMedia(ImageOnly)`), документы — SAF (`OpenDocument(arrayOf("*/*"))`).
2. **Результат теряется при смерти процесса.** Пока открыт системный пикер (отдельная activity), процесс может
   быть убит. Registry доставит pending-результат в пересозданный лаунчер, но колбэк уже потерян.
   У нас — `PendingPickerCallbacks` (object): переживает пересоздание Activity, **но не смерть процесса**.
   Фикс: контракт пикера — `Flow` результатов, реализации буферизуют их в `Channel`, экран собирает поток.
3. Логирование ошибок копирования вместо «тихой» отмены.

## Что сохраняем из нашей версии

- Копирование в `filesDir/support-chat-attachments/<uuid>/<имя>` (нужно outbox'у после рестарта), а не во временный
  `cacheDir`; проверка размера до и после копирования; результаты `TooLarge` / `Failed` со снекбарами.
- Копирование на `dispatchers.io` в app-scope (переживает пересоздание композиции посреди копирования).
- iOS / JVM-реализации (UIDocumentPicker / PHPicker, AWT FileDialog) — только адаптация к новому контракту.

## План

### 1. Контракт (`feature/support-chat/.../presentation/platform/SupportChatFilePicker.kt`)
- `interface SupportChatFilePicker { val results: Flow<SupportChatPickedFileResult>; fun launchImage(); fun launchDocument() }`.
- `Picked(file, kind: SupportChatPickKind)`; `enum class SupportChatPickKind { Image, Document }` — тип вложения
  задаёт источник выбора (как сейчас, картинка, выбранная как документ, остаётся документом).
- `Cancelled` / `TooLarge` / `Failed` — без изменений. KDoc: по одному результату на `launch*`, доставка на main,
  буфер до появления подписчика, почему не колбэк.

### 2. Экран (`SupportChatNavGraph.kt`, `SupportChatEntry`)
- `LaunchedEffect(filePicker, viewModel) { filePicker.results.collect { … } }`: `Picked` → по `kind`
  `onImagePicked` / `onDocumentPicked`; `TooLarge` / `Failed` → `onAttachmentRejected(tooLarge)`; `Cancelled` — ничего.
  Это проброс платформенных результатов, а не старт процесса VM (правило «не стартовать VM из LaunchedEffect»
  не нарушается).
- `onAttachFile = { filePicker.launchDocument() }`, `onAttachPhoto = { filePicker.launchImage() }`;
  убрать `currentPicker` / `deliverPickResult`-колбэки.

### 3. Android (`composeApp/src/androidMain/.../rememberSupportChatFilePicker.kt`)
- Лаунчеры: `PickVisualMedia()` (`launch(PickVisualMediaRequest(ImageOnly))`) и `OpenDocument()` (`launch(arrayOf("*/*"))`).
- Буфер результатов — **process-level** `Channel(UNLIMITED)` (object `PickerResults` вместо
  `PendingPickerCallbacks`): переживает и пересоздание Activity посреди копирования, и (после смерти процесса)
  получает результат от registry в новом процессе. Поток — `receiveAsFlow()`.
- `uri == null` → `Cancelled`; иначе `appScope.launch { io: copyToAttachments } → trySend(result с kind)`.
- Логирование (`Logger.withTag("SupportChatFilePicker")`) в ветках `Failed` копирования.
- Известное: Photo Picker отдаёт `DISPLAY_NAME` вида `<id>.<ext>` («26.png») — поведение системы, не трогаем.

### 4. iOS (`composeApp/src/iosMain/...`) и JVM (`composeApp/src/jvmMain/...`)
- `Channel(UNLIMITED)` в реализации (в `remember`), `results = receiveAsFlow()`; делегаты/диалог шлют результат в
  канал с `kind` вместо колбэка. Логика копирования и проверок — без изменений.

### 5. Документация
- `.ai/agents/feature/support-chat/AGENTS.md`: пункт «Платформенные порты» — контракт пикера `Flow` + `kind`,
  Android Photo Picker / SAF и почему (clipData, смерть процесса) → `.ai/generate.sh`.

### 6. Проверка
- `:feature:support-chat:jvmTest`, `:composeApp:compileKotlinJvm`, `:composeApp:compileKotlinIosArm64`,
  `:androidApp:assembleDebug`.
- Эмулятор: картинка и документ выбираются и отправляются; отмена пикера — без снекбара; файл > 20 МБ — снекбар;
  **смерть процесса**: открыть пикер документов → `adb shell am kill ru.alarmtrade.specmp.androidApp` (процесс в фоне) →
  выбрать файл → приложение восстанавливается, вложение в композере.
- Десктоп: выбор картинки и документа (через исполнитель кликов).

## Открытые вопросы

1. Буфер на Android — process-level object (предлагаю) или в `remember`, как у Гужавина? В `remember` результат
   теряется, если Activity пересоздаётся во время копирования большого файла.
2. Коммит — отдельным коммитом с `Co-authored-by` Гужавина (его идея и часть кода)?

## Результат (2026-10-09)

- Сборки: `:feature:support-chat:jvmTest`, `:composeApp:compileKotlinJvm`, `:composeApp:compileKotlinIosArm64`,
  `:androidApp:assembleDebug` ✓.
- Эмулятор: Photo Picker (`photopicker.MainActivity`) → картинка в композере ✓; документ через SAF
  (`documentsui PickActivity`) ✓; отмена — без вложения и снекбара ✓; файл > 20 МБ — снекбар ✓;
  **смерть процесса**: пикер документов открыт → `am kill` (pid 9023) → выбор файла → новый процесс (9573),
  экран чата восстановлен, `qa_doc.pdf` в композере ✓.
- Десктоп: выбор документа через диалог → в композере ✓.
