# Лучшие практики Compose — Рекомпозиция, Стабильность, State, Side Effects

## Compose: три фазы

Compose работает в 3 фазах: **Composition → Layout → Draw**.
Чтение состояния на более ранней фазе вынуждает все последующие фазы перезапускаться.
Лучшая практика: откладывать чтение состояния как можно позже (как можно глубже в дерево).

---

## 1. Разбиение на composable-функции

### Правило

Каждая не-inline `@Composable fun`, возвращающая `Unit`, создаёт **область рекомпозиции (recomposition scope)**.
При изменении любого состояния, прочитанного внутри этой области, **перезапускается всё тело функции**.

`Column`, `Row`, `Box`, `Layout` — **inline**, они разделяют область родителя. Разбиение на inline-контейнеры ничего не даёт.

### Когда разбивать

- Дочерний элемент читает часто меняющееся состояние (скролл, значения анимации, тики таймера)
- Родитель содержит дорогостоящих соседей, которые НЕ должны рекомпозироваться вместе
- Результатом разбиения становится значимо меньшая область рекомпозиции

### Когда НЕ разбивать

- Все дочерние элементы всё равно читают одно и то же «горячее» состояние — разбиение ничего не изменит
- Composable рекомпозируется редко — накладные расходы превысят выгоду
- Придётся добавить много параметров в извлечённую функцию

### Пример

```kotlin
// ПЛОХО: HeavyProductGrid рекомпозируется на каждый пиксель скролла
@Composable
fun SnackDetail(snack: Snack) {
    Box {
        val scroll = rememberScrollState()
        HeavyProductGrid(snack.products)          // рекомпозируется со скроллом
        TitleBar(snack, scroll.value)
    }
}

// ХОРОШО: рекомпозируется только TitleBar, HeavyProductGrid не трогается
@Composable
fun SnackDetail(snack: Snack) {
    Box {
        val scroll = rememberScrollState()
        HeavyProductGrid(snack.products)           // никогда не рекомпозируется от скролла
        TitleBar(snack) { scroll.value }           // лямбда откладывает чтение
    }
}

@Composable
private fun TitleBar(snack: Snack, scrollProvider: () -> Int) {
    // ТОЛЬКО эта область инвалидируется при изменении скролла
    Column(Modifier.offset { IntOffset(0, scrollProvider()) }) {
        Text(snack.name)
    }
}
```

---

## 2. Откладывать чтение состояния как можно глубже

Чтение значения состояния на высоком уровне вызывает более широкую рекомпозицию. Чем позже по фазе и глубже в дереве происходит чтение — тем меньше работы делает Compose.

### Три уровня отложенного чтения

**Уровень 1 — Параметр-лямбда** (откладывает чтение до Composition дочернего элемента)

```kotlin
// Передать лямбду вместо значения — родительская область не читает scroll вообще
Header { scrollState.value }
```

**Уровень 2 — Лямбда в модификаторе** (откладывает до фазы Layout, пропускает Composition полностью)

```kotlin
// offset { } выполняется в фазе Layout — рекомпозиции нет вообще
Modifier.offset { IntOffset(0, scrollProvider()) }
```

**Уровень 3 — Фаза Draw** (пропускает и Composition, и Layout)

```kotlin
// drawBehind { } выполняется в фазе Draw — ноль рекомпозиций для анимации
Modifier.drawBehind { drawRect(animatedColor) }
Modifier.graphicsLayer { alpha = alphaProvider() }
```

### Передача лямбд через промежуточные слои (State Conduit)

```kotlin
@Composable
fun Root() {
    val scroll = rememberScrollState()
    Middle { scroll.value }       // Middle никогда не читает его
}

@Composable
fun Middle(scrollProvider: () -> Int) {
    Inner(scrollProvider)         // просто передаёт дальше
}

@Composable
fun Inner(scrollProvider: () -> Int) {
    // ТОЛЬКО эта область инвалидируется
    Box(Modifier.offset { IntOffset(0, scrollProvider()) })
}
```

---

## 3. Стабильность

Compose **пропускает** рекомпозицию composable, если его параметры не изменились. «Не изменились» определяется
стабильностью типа параметра — это главный рычаг против лишних рекомпозиций.

### 3.1 Как Compose сравнивает параметры

| Стабильность типа | Сравнение (Strong Skipping) | Откуда берётся |
|---|---|---|
| **stable** | `equals` — новый экземпляр с теми же данными **не** рекомпозирует | примитивы, `String`, enum, функции; `@Immutable`/`@Stable`; классы из `val` стабильных типов того же модуля; типы из `config/compose/stability.conf` |
| **unstable** | `===` — **любой новый экземпляр** рекомпозирует, даже с теми же данными | `var`-поля; типы из модулей **без** Compose-компилятора (domain, SDK); `List`/`Map`/`Set`; `Any` |
| **runtime** | решается при выполнении | поле-интерфейс или дженерик (`List<T>`, `sealed interface`), реализацию которого компилятор не видит |

Strong Skipping (Kotlin 2.0+, включён по умолчанию): все restartable composable — skippable, лямбды мемоизируются,
нестабильные параметры сравниваются по `===`. Отсюда правило: **нестабильный параметр вредит, только если на
каждый эмит state приходит новый экземпляр с теми же данными.**

### 3.2 Какие типы стабильны в проекте и как

| Тип | Как обеспечена стабильность | Что делать |
|---|---|---|
| State экрана, `presentation/model` | `@Immutable` / `@Stable` | помечать сразу при создании |
| Доменные модели (`feature/**/domain/model`) | конфиг `stability.conf` (крайняя мера: domain собирается без Compose, аннотацию не поставить) | аннотации в domain **не** ставить; идентичность экземпляров — всё равно кодом (§3.4) |
| Общие ошибки `AppError` / `ApiError` / `PlatformError` | конфиг | — |
| `kotlinx.datetime.*`, `IntRange` | конфиг | — |
| SDK-типы | нестабильны | в composable не передавать: маппинг в presentation/domain-модель (`docs/patterns.md`, «Domain») |
| Коллекции параметром composable | `List`/`Set`/`Map` — нестабильны | `ImmutableList` (`kotlinx-collections-immutable`) или поле `@Immutable`-класса state |
| `Any?` (модель картинки Coil в `CmpAsyncImage`) | нестабилен по определению | допустимо: экземпляр модели меняется вместе с данными |

### 3.3 `@Immutable` vs `@Stable`

Аннотации — **обещания компилятору**. Их нарушение приводит к скрытым багам с устаревшими данными.

| Аннотация | Контракт | Когда использовать |
|---|---|---|
| `@Immutable` | Все свойства `val`, не изменяются после создания (транзитивно) | State экрана, presentation-модели |
| `@Stable` | Может мутировать, но Compose УВЕДОМЛЯЕТСЯ через snapshot state | Объекты с полями `MutableState` |

```kotlin
// @Immutable — настоящий value-объект
@Immutable
data class UserProfile(val id: String, val name: String)

// @Stable — мутируемый, но наблюдаемый
@Stable
class CartState {
    var count by mutableStateOf(0)  // Compose видит изменения через snapshot
}

// НЕПРАВИЛЬНО — обманывает компилятор, Compose не узнает о мутациях
@Stable
data class BrokenState(var count: Int)  // var без mutableStateOf
```

### 3.4 Идентичность экземпляров — не пересоздавать то, что не изменилось

Стабильный тип спасает от рекомпозиции при `equals`-равенстве, но не от лишней работы на пересборку. Для
нестабильных (и для `runtime`) решает только идентичность. Правила для VM и state:

- **Обновлять state через `copy` поля, а не пересобирать целиком.** `copy` сохраняет ссылки на неизменённые поля —
  их composable пропускаются.
- **Неизменённые элементы — из прежнего экземпляра.** Перечитанный список — через `reusing(previous) { it.id }`
  (`core/common`): элементы с тем же ключом и `==` берутся из прежнего списка, а если не изменилось ничего —
  возвращается сам прежний список (образец — «Мои конфигурации», `MineViewModel`). Для деревьев — свой обход
  (`SettingsSection.reusing(old)` в `feature/device/settings`: правка одной настройки не перерисовывает остальные строки).
- **Источник, эмитящий одинаковые значения, — через `distinctUntilChanged()`** в VM, а не фильтром в composable.
- **Не маппить, не сортировать, не фильтровать в теле composable** без `remember(узкие ключи)` — каждый вызов
  создаёт новый список и новые элементы (`docs/patterns.md`, «Корректность state»; §9 «Списки»).
- **Не создавать объекты-параметры в теле composable** (`listOf(...)`, `Modifier`-цепочки с данными, `TextStyle(...)`,
  data class «на месте»): новый экземпляр каждую рекомпозицию. Константа, `remember` или поле state.

```kotlin
// ❌ Новый список и новые элементы на каждую рекомпозицию — MessageRow не пропускается
@Composable
fun Feed(messages: List<Message>) {
    LazyColumn { items(messages.map { it.toUi() }) { MessageRow(it) } }
}

// ✅ Элементы готовит VM (один раз на изменение данных), composable только рисует
@Immutable
data class FeedState(val items: ImmutableList<MessageUi>)
```

### 3.5 Конфиг стабильности (`config/compose/stability.conf`) — крайняя мера

**Порядок решения нестабильности:** сначала код — идентичность экземпляров (§3.4), `@Immutable` presentation-модель,
`ImmutableList`, маппинг SDK-типов до UI. Конфиг — только когда тип нельзя пометить аннотацией (он в модуле без
Compose-компилятора или в библиотеке) и кодом проблема не снимается; каждая запись — с комментарием «почему».
Конфиг не исправляет пересоздание экземпляров — он лишь включает `equals` для типа.

Модули `domain` (и `core/common`) собираются **без** Compose-компилятора, поэтому их неизменяемые data-классы
по умолчанию считаются нестабильными. `@Immutable` на них не ставим (domain без Compose). Вместо этого они объявлены
стабильными в одном конфиге, который корневой `build.gradle.kts` подключает ко всем модулям с плагином
`org.jetbrains.kotlin.plugin.compose`:

- `ru.alarmtrade.specmp.feature.**.domain.model.*` — доменные модели фич;
- `AppError` / `ApiError` / `PlatformError` из `core/common`;
- `kotlinx.datetime.*`, `kotlin.ranges.IntRange`.

**Что требуется от типа в конфиге:** все поля `val`, без мутабельных коллекций, массивов и изменяемого состояния
внутри — транзитивно. Нарушение не ломает сборку, а молча оставляет на экране старые данные: Compose пропустит
рекомпозицию при реальном изменении. Поэтому:

- в `domain/model` — только неизменяемые типы (это и так правило «Domain» в `docs/patterns.md`);
- новый пакет или библиотечный тип в конфиг — только как крайняя мера (см. порядок выше), после проверки этого условия, с комментарием «почему»;
- presentation-модели в модулях с Compose помечаются `@Immutable`, а не добавляются в конфиг.

### 3.6 Как проверить

```bash
.agents/skills/perf-audit/scripts/compose-reports.sh feature/vehicle-ui feature/device/settings-ui
```

Скрипт перекомпилирует jvm-таргет с отчётами Compose-компилятора и печатает: `unstable`/`runtime`-классы, composable
с нестабильными параметрами и restartable-но-не-skippable функции (полные отчёты — `<модуль>/build/compose-audit/`).

Как читать:

- **Composable с unstable-параметрами** — главный список. Для каждого: приходит ли новый экземпляр на эмит
  (§3.4)? Если да и composable на горячем пути (элемент списка, часто эмитящий state) — исправлять.
- **`unstable`/`runtime` ViewModel, репозитории, use case, эффекты** — норма: в composable они не передаются.
- **`runtime` presentation-классы** (поле `List`/`Set`/sealed-интерфейс) — пометить `@Immutable`, если класс
  передаётся параметром на горячем пути.
- Любая правка стабильности — с отчётом до/после (скилл `perf-audit`).

---

## 4. Flow vs State в composable-функциях

**Никогда не передавать `Flow` в дочерний composable.** Собирать один раз на верхнем уровне, передавать вниз plain-значения.

```kotlin
// ПЛОХО: filter { } создаёт новый Flow при каждой рекомпозиции → подписка перезапускается → мерцание
@Composable
fun Screen(vm: MyViewModel) {
    val state by vm.items.filter { it.visible }.collectAsState(emptyList())
}

// ХОРОШО: собрать один раз, дочерний получает plain-значение
@Composable
fun Screen(vm: MyViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    ItemList(state.items)
}
```

### `collectAsState` vs `collectAsStateWithLifecycle`

- **Android-приложение**: всегда использовать `collectAsStateWithLifecycle` — приостанавливает сбор в фоне
- **KMP / платформо-независимый код**: `collectAsState` подходит

### Архитектура потока данных

```
Repository (Flow<Domain>)
    → ViewModel (.stateIn(WhileSubscribed(5000)))
    → StateFlow<UiState>
    → Screen (.collectAsStateWithLifecycle())
    → Children (plain-значения, стабильные параметры)
```

### Одноразовые события (side effects)

НЕ встраивать события в `UiState`. В проекте используется **только `MutableSharedFlow`** (`Channel` + `receiveAsFlow()` не используем — см. `docs/patterns.md`, раздел «One-shot события (Effects)»):

```kotlin
private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
val events: SharedFlow<UiEvent> = _events.asSharedFlow()

LaunchedEffect(Unit) {
    vm.events.collect { event ->
        when (event) {
            is UiEvent.Navigate -> navController.navigate(event.route)
        }
    }
}
```

---

## 5. Передача `State<T>` в дочерний composable вместо значения

### Проблема

Когда родитель читает `StateFlow` через `by collectAsState()`, он подписывается на каждое изменение состояния — даже если только глубокий дочерний composable реально использует это состояние.

```kotlin
// ПЛОХО: FormScreen рекомпозируется при каждом нажатии клавиши
@Composable
fun FormScreen() {
    val state by viewModel.state.collectAsState()  // ← родитель подписывается
    // ...
    FormDisplay(state = state)                     // ← значение читается здесь, родитель владеет областью
    StaticActions(...)                             // ← рекомпозируется без необходимости
}
```

### Решение: собрать без `by`, передать `State<T>` дочернему

```kotlin
// ХОРОШО: FormScreen никогда не читает значение — рекомпозируется только FormDisplay
@Composable
fun FormScreen() {
    val state = viewModel.state.collectAsState()   // объект State<T>, значение НЕ читается здесь
    // ...
    FormDisplay(state = state)
    StaticActions(...)                             // стабильные параметры → пропускается
}

@Composable
private fun FormDisplay(state: State<FormState>) {
    val value = state.value                        // ← чтение происходит здесь, только эта область инвалидируется
    // ...
}
```

### Сравнение подходов

| Подход | Область рекомпозиции | Примечания |
|---|---|---|
| `val state by collectAsState()` в родителе, передать значение | Родитель | Все соседи рекомпозируются |
| `val state = collectAsState()`, передать `State<T>` дочернему | Только дочерний | Корутина живёт в области родителя — стабильно |
| Передать `StateFlow`, вызвать `collectAsState()` в дочернем | Только дочерний | Тот же результат, чуть многословнее |
| `rememberUpdatedState` | ❌ не тот инструмент | Не предотвращает рекомпозицию |

### Когда использовать

- Дочерний composable читает **большинство или все** поля объекта состояния (состояние «горячее»)
- Родитель имеет дорогостоящих соседей, которые не должны рекомпозироваться при каждом изменении
- Предпочтительно держать корутинную подписку в области родителя (переживает рекомпозиции дочернего)

### Когда использовать отдельные поля `Flow<>` в ViewModel

Если разные части дерева читают **разные** поля — рассмотреть экспонирование каждого поля как собственного `StateFlow`. Каждый дочерний подписывается только на то, что ему нужно. Усложняет ViewModel, поэтому сначала профилировать.

---

## 6. `rememberUpdatedState`

**Решаемая проблема**: долго живущий эффект, который НЕ должен перезапускаться при изменении коллбэка, но всегда должен вызывать *последнюю* версию этого коллбэка.

```kotlin
// БАГ: эффект перезапускается при каждой рекомпозиции (лямбда — новый экземпляр)
LaunchedEffect(onTimeout) { delay(3000); onTimeout() }

// БАГ: устаревшее замыкание — вызывает начальный onTimeout, даже если он изменился
LaunchedEffect(Unit) { delay(3000); onTimeout() }

// ПРАВИЛЬНО
val currentOnTimeout by rememberUpdatedState(onTimeout)
LaunchedEffect(Unit) {          // стабильный ключ — запускается один раз
    delay(3000)
    currentOnTimeout()          // всегда актуальный
}
```

### Правило выбора

```
Должен ли эффект перезапускаться при изменении значения?
├── ДА → использовать значение как ключ эффекта напрямую
└── НЕТ, но эффект должен использовать последнее значение?
      └── ДА → rememberUpdatedState
```

---

## 7. `derivedStateOf`

`derivedStateOf` = `distinctUntilChanged` для Compose state.

**Использовать только когда входное состояние меняется ЧАЩЕ, чем нужно обновлять выходное.**
Если входное и выходное меняются с одинаковой частотой — `derivedStateOf` лишь добавляет накладные расходы.

```kotlin
// ХОРОШО: скролл меняется на каждый пиксель, кнопка переключается лишь дважды за жест
val showButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}

// ХОРОШО: поля формы меняются при каждом нажатии, валидность меняется редко
val isValid by remember {
    derivedStateOf { email.contains("@") && password.length >= 8 }
}

// ПЛОХО: fullName должен обновляться при каждом изменении — никакой фильтрации нет
val fullName by remember { derivedStateOf { "$first $last" } }
// ПРАВИЛЬНО: использовать remember(key) или вычислять напрямую
val fullName = remember(first, last) { "$first $last" }
```

### Ловушка захваченных переменных

Не-State переменные захватываются в момент создания — их изменения не инвалидируют derived state.

```kotlin
// БАГ: threshold устарел внутри derivedStateOf
val filtered by remember {
    derivedStateOf { items.value.filter { it.score > threshold } }
}

// ИСПРАВЛЕНИЕ: добавить threshold как ключ remember
val filtered by remember(threshold) {
    derivedStateOf { items.value.filter { it.score > threshold } }
}
```

### Правило выбора

```
Должен ли UI реагировать на каждое изменение входных данных?
├── ДА → не использовать derivedStateOf
└── НЕТ, входные меняются чаще, чем значимо меняются выходные?
      ├── ДА → derivedStateOf (порог скролла, переключение валидности формы)
      └── НЕТ → remember(key) для дорогостоящего преобразования
```

---

## 8. `remember` — Ключи и время жизни

`remember` хранит значения **позиционно**. Блок инициализации запускается при первой композиции и при изменении ключа.

| Событие | Результат |
|---|---|
| Рекомпозиция, те же ключи | Возвращается кэшированное значение |
| Рекомпозиция, другие ключи | Блок инициализации перезапускается |
| Composable покидает композицию | Значение забывается |
| Изменение конфигурации (поворот) | Значение забывается |

```kotlin
// Без ключа — вычисляется один раз, никогда не обновляется, даже если items изменится
val sorted = remember { items.sortedBy { it.name } }

// С ключом — пересортируется при изменении items, пропускается при несвязанных рекомпозициях
val sorted = remember(items) { items.sortedBy { it.name } }
```

**Правило**: всегда указывать ключи, когда блок инициализации читает внешние переменные, которые могут измениться.

### `remember` vs `rememberSaveable`

| | `remember` | `rememberSaveable` |
|---|---|---|
| Переживает рекомпозицию | ✅ | ✅ |
| Переживает поворот экрана | ❌ | ✅ |
| Переживает смерть процесса | ❌ | ✅ |
| Требует сериализуемое значение | Нет | Да (примитивы, Parcelable, custom Saver) |

**Использовать `rememberSaveable`** для: пользовательского ввода, позиции скролла, выбранной вкладки, состояния формы.
**Использовать обычный `remember`** для: производных отображаемых значений, состояния анимации, легко восстанавливаемых данных.

### Антипаттерны

```kotlin
// ПЛОХО: дорогостоящие вычисления при каждой рекомпозиции
val processed = items.filter { it.active }.sortedBy { it.name }

// ХОРОШО: мемоизировано
val processed = remember(items) { items.filter { it.active }.sortedBy { it.name } }

// ПЛОХО: излишний remember для дешёвой операции
val label = remember(count) { "Count: $count" }

// ХОРОШО: просто вычислить
val label = "Count: $count"
```

---

## 9. Списки, ввод, эффекты — типовые ловушки

### Построение элементов списка

- **Не сортировать и не пересобирать весь список в композиции.** `remember(messages) {
  messages.sortedBy { … } }` — O(n log n) на каждое изменение любого элемента. Порядок
  поддерживает state при вставке (`docs/patterns.md`, «Корректность state»).
- **Редко меняющиеся и часто меняющиеся данные — в разных ключах.** Индикатор «печатает…» в ключе
  `remember(messages, typingAuthor)` пересобирает всю ленту ради одного элемента — вынеси его в
  отдельный `item {}`.
- **Ключи `items(key = …)` — стабильные**, префиксы ключей — `private const val`
  (`docs/localization.md`, правило 7).

### Чат-подобные ленты

`LazyColumn(reverseLayout = true)` + элементы **от новых к старым**: «низ» — естественная стартовая
позиция (нет рывка «верх → низ» при открытии), эффект автоскролла не нужен, пагинация вверх —
`!listState.canScrollForward`. Если автоскролл всё же нужен — только когда пользователь уже у
последнего сообщения; безусловный `animateScrollToItem(last)` выдёргивает его из чтения истории.

### `snapshotFlow` по `layoutInfo`

```kotlin
// ❌ Плохо: listItems в ключе перезапускает подписку на каждое сообщение;
// map аллоцирует список на каждый кадр скролла; VM дёргается поштучно
LaunchedEffect(listState, listItems) {
    snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
        .distinctUntilChanged()
        .collect { keys -> keys.forEach { vm.onVisible(it) } }
}

// ✅ Хорошо: ключ — только listState; дешёвый снимок (два Int) вместо списка на кадр;
// во VM — одна пачка на изменение видимого диапазона
val currentOnVisible by rememberUpdatedState(onItemsVisible)
LaunchedEffect(listState) {
    snapshotFlow {
        val visible = listState.layoutInfo.visibleItemsInfo
        (visible.firstOrNull()?.index ?: -1) to (visible.lastOrNull()?.index ?: -1)
    }
        .distinctUntilChanged()
        .collect { currentOnVisible(listState.layoutInfo.visibleItemsInfo.map { it.key }) }
}
```

### Текстовый ввод — локальное состояние

Черновик поля ввода держится в `rememberSaveable` внутри composable, а не в state ViewModel:
иначе каждый символ → `_state.update` → рекомпозиция всего экрана со списком. ViewModel получает
текст в момент действия (`onSend(text)`). Если VM обязана владеть черновиком (персист),
экран разбивается так, чтобы список не попадал в область рекомпозиции поля.

### Много колбэков — один `Actions`

Когда один и тот же набор интентов протаскивается через несколько уровней
(`Screen → Scaffold → Body`, 10+ параметров), колбэки собираются в
`@Immutable data class XxxActions(val onSend: () -> Unit, …)`, создаваемый один раз
(`remember(viewModel)`) в entry-точке.

### Canvas и отрисовка

Геометрия, зависящая только от размера и цветов (узоры, тайлы, пути), считается в
`Modifier.drawWithCache { … onDrawBehind { … } }` — один раз на размер, а не на каждый проход
фазы Draw внутри `Canvas {}`.

### Доступность кликабельных областей

Кликабельная область, которая не является `Cmp*`-кнопкой, получает `onClickLabel` (строка из
ресурсов) и `role`: `Modifier.clickable(onClickLabel = stringResource(Res.string.…_a11y_retry),
role = Role.Button) { … }`. Иначе скринридер озвучит безымянную область.

---

## 10. Требования анализаторов (compose-rules)

Compose-код проверяется `./gradlew spotlessCheck` (ktlint + compose-rules), это обязательная стадия
([`code-quality.md`](code-quality.md)). Чтобы не получать замечаний, пиши сразу так:

| ✅ Делай | ❌ Не делай | Правило |
|---|---|---|
| Параметры: данные → обязательные лямбды → `modifier` → необязательные → контентный слот | `modifier` в начале или после параметров с дефолтами | `param-order-check` |
| Лямбды событий в настоящем времени: `onClick`, `onTabSelect` | `onClicked`, `onTabSelected` | `parameter-naming` |
| Событийная лямбда не последняя, последним — `content` | Событийная лямбда последним параметром composable с контентом | `lambda-param-event-trailing` |
| Один корневой узел; `modifier` применён на корне один раз | Несколько соседних эмиттеров; один `modifier` у нескольких узлов | `multiple-emitters-check`, `modifier-reused-check`, `modifier-not-used-at-root` |
| ViewModel только в Screen, вниз — значения и колбэки | `viewModel` параметром в дочерних composable | `vm-forwarding-check` |
| Лямбда-параметр в эффекте — через `rememberUpdatedState` | Вызов лямбды напрямую внутри `LaunchedEffect(Unit)` | `lambda-param-in-effect` |
| `mutableIntStateOf` / `mutableFloatStateOf` / `mutableLongStateOf` для примитивов | `mutableStateOf(0)` / `mutableStateOf(0f)` | `mutable-state-autoboxing` |
| `State<T>` / `() -> T` в параметре — можно | `MutableState<T>` в параметре | `mutable-state-param-check` |
| `@Preview`-функции `private` | публичное превью | `preview-public-check` |
| Composable, возвращающий значение, — `camelCase` (`rememberX`); эмитящий UI — `PascalCase`-существительное | Composable-фабрика в `PascalCase`, эмитящий composable в `camelCase` | `naming-check` |
| Один вызов `content()` в теле | `content()` в нескольких ветках `if/when` | `content-slot-reused` |
| Новый `CompositionLocal` — сначала подумай о параметре; если нужен, добавь имя в `compose_allowed_composition_locals` в [`.editorconfig`](../.editorconfig) (сейчас: `LocalAppLocale`, `LocalCmpImageLoader`, `LocalThemeIsDark`) | Создавать `compositionLocalOf` без регистрации | `compositionlocal-allowlist` |

Исключения (осознанно выключены, не нужно добавлять `modifier` ради правила): `modifier-missing-check`,
`modifier-without-default-check`, `state-param-check`.

Подавление конкретного правила: `@Suppress("ktlint:compose:<правило>")` на функции. Для `multiple-emitters-check` и
`lambda-param-in-effect` аннотация на функции не работает — только `@file:Suppress(...)` перед `package`. Подробнее —
[`code-quality.md`](code-quality.md#подавления).

---

## Быстрый справочник

| Паттерн | Использовать когда | Пропустить когда |
|---|---|---|
| **Разбить composable** | Дочерний читает горячее состояние, соседи дорогие | Все дочерние всё равно читают то же горячее состояние |
| **Параметр-лямбда** | Частое значение управляет модификатором/layout дочернего | Значение меняется с той же частотой, что и рекомпозиции родителя |
| **Лямбда в модификаторе** (`offset{}`, `graphicsLayer{}`) | Анимация/скролл управляет трансформацией | Статические значения layout |
| **`drawBehind{}`** | Анимация цвета/кисти, без зависимости от layout | Нужно изменять форму или размер |
| **`@Immutable`** | State экрана / presentation-модель: все свойства `val`, никогда не мутируют | Есть `var`, или нельзя гарантировать транзитивную неизменяемость; доменная модель (её покрывает `stability.conf`) |
| **`@Stable`** | Мутируемое состояние через поля `MutableState` | Мутируемые поля не обёрнуты в snapshot state |
| **`ImmutableList`** | List передаётся как параметр composable | Внутренняя обработка, не пересекающая границу composable |
| **`stability.conf`** | Крайняя мера: неизменяемый тип из модуля без Compose, кодом (§3.4, `@Immutable`, `ImmutableList`) не решается | Есть решение кодом; тип с `var`/мутабельными коллекциями; presentation-модель (её — `@Immutable`) |
| **Переиспользовать экземпляр** | Частичное обновление дерева/списка в VM | Данные действительно изменились |
| **`collectAsStateWithLifecycle`** | Android-приложение | KMP общий код |
| **Передать `State<T>` дочернему** | Дочерний читает всё состояние, у родителя есть дорогие соседи | Только один дочерний читает состояние — просто собрать в нём |
| **`rememberUpdatedState`** | Долгий эффект + коллбэк может меняться + эффект НЕ должен перезапускаться | Эффект ДОЛЖЕН перезапускаться при изменении коллбэка |
| **`derivedStateOf`** | Вход меняется быстрее, чем значимый выход | Вход и выход меняются с одинаковой частотой |
| **`remember(key)`** | Дорогостоящее вычисление из внешних переменных | Дешёвое вычисление; нет внешних зависимостей |
| **`rememberSaveable`** | Пользовательский ввод, состояние формы, позиция скролла | Легко восстанавливаемое состояние |
| **`reverseLayout = true`** | Чат/лента, где «низ» — стартовая позиция | Обычные списки сверху вниз |
| **`drawWithCache`** | Геометрия зависит только от размера/цветов | Рисунок меняется каждый кадр (анимация) |
| **`XxxActions`-bundle** | Один набор колбэков через 2+ уровня composable | 2–3 колбэка в одной функции |
