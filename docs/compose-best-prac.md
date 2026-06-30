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

## 3. `@Immutable` vs `@Stable`

Compose может **пропустить рекомпозицию** composable только если все его параметры **стабильны**.
Эти аннотации — **обещания компилятору**. Их нарушение приводит к скрытым багам с устаревшими данными.

| Аннотация | Контракт | Когда использовать |
|---|---|---|
| `@Immutable` | Все свойства `val`, не изменяются после создания | Чистые value-объекты, доменные модели |
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

### Strong Skipping Mode (Kotlin 2.0+, включён по умолчанию)

- Все restartable composable становятся skippable даже с нестабильными параметрами
- Нестабильные параметры сравниваются по `===` (по ссылке)
- Лямбды в composable-функциях автоматически мемоизируются

**Что NOT исправляет**: создание нового экземпляра `List` из тех же данных всё равно вызывает рекомпозицию (ссылка изменилась). Решение: `ImmutableList` из kotlinx-collections-immutable или `distinctUntilChanged()` в ViewModel.

### Проблема с коллекциями

`List<T>` всегда **нестабилен** — даже если элементы помечены `@Immutable`.

```kotlin
// Всё ещё нестабилен — skip невозможен
@Composable fun List(items: List<Item>) { }

// Стабилен — skip возможен
@Composable fun List(items: ImmutableList<Item>) { }

// Или обернуть
@Immutable data class Items(val list: List<Item>)
```

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

## Быстрый справочник

| Паттерн | Использовать когда | Пропустить когда |
|---|---|---|
| **Разбить composable** | Дочерний читает горячее состояние, соседи дорогие | Все дочерние всё равно читают то же горячее состояние |
| **Параметр-лямбда** | Частое значение управляет модификатором/layout дочернего | Значение меняется с той же частотой, что и рекомпозиции родителя |
| **Лямбда в модификаторе** (`offset{}`, `graphicsLayer{}`) | Анимация/скролл управляет трансформацией | Статические значения layout |
| **`drawBehind{}`** | Анимация цвета/кисти, без зависимости от layout | Нужно изменять форму или размер |
| **`@Immutable`** | Все свойства `val`, никогда не мутируют | Есть `var`, или нельзя гарантировать транзитивную неизменяемость |
| **`@Stable`** | Мутируемое состояние через поля `MutableState` | Мутируемые поля не обёрнуты в snapshot state |
| **`ImmutableList`** | List передаётся как параметр composable | Внутренняя обработка, не пересекающая границу composable |
| **`collectAsStateWithLifecycle`** | Android-приложение | KMP общий код |
| **Передать `State<T>` дочернему** | Дочерний читает всё состояние, у родителя есть дорогие соседи | Только один дочерний читает состояние — просто собрать в нём |
| **`rememberUpdatedState`** | Долгий эффект + коллбэк может меняться + эффект НЕ должен перезапускаться | Эффект ДОЛЖЕН перезапускаться при изменении коллбэка |
| **`derivedStateOf`** | Вход меняется быстрее, чем значимый выход | Вход и выход меняются с одинаковой частотой |
| **`remember(key)`** | Дорогостоящее вычисление из внешних переменных | Дешёвое вычисление; нет внешних зависимостей |
| **`rememberSaveable`** | Пользовательский ввод, состояние формы, позиция скролла | Легко восстанавливаемое состояние |
