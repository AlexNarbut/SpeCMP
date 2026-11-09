# Навигация — androidx Navigation3 (nav3)

Библиотека: `org.jetbrains.androidx.navigation3:navigation3-ui`.

## Основные понятия

- **Back stack** — обычный `SnapshotStateList`, управляемый кодом приложения.
- **Routes** — сериализуемые объекты, реализующие `NavKey`.
- **NavDisplay** — отображает текущий destination из back stack.
- **NavEntry** — связывает ключ маршрута с Compose-контентом.

## Текущая структура навигации

Верхний уровень — `AuthStateNavGraph` из `feature/auth-state`: composable-переключатель по состоянию сессии (`WAITING` / `NO_AUTH` / `AUTHED` / `CHECK_FAILED`) с fade-анимацией. Он получает слоты зон и сам решает, какая зона приложения видна:

```kotlin
@Composable
fun AppNavigation() {
    AuthStateNavGraph(
        waitingState = { AppStartLoading() },
        noAuthState = { LoginNavGraph() },
        authedState = { AuthedNavGraph() },
        errorState = { onRetry -> SessionCheckErrorScreen(onRetry = onRetry) },
    )
}
```

Экраны не навигируют между зонами сами: успешный login и logout меняют состояние сессии, `AuthStateNavGraph` подхватывает изменение и переключает зону.

Nav3 back stack используется **внутри каждой зоны**: `LoginNavGraph` (`feature/login`) в неавторизованной, `AuthedNavGraph` (`composeApp`) в авторизованной. `AuthedNavGraph` — только хост: подключает **целые графы фич** (`MainNavGraph`, `DeviceSelectionNavGraph`; чат — entry `SupportChatEntry`) и хранит app-level переходы между их ключами (`MainRoute` / `SupportChatRoute` / `DeviceSelectionRoute`). Каждый ключ фичи объявлен в самой фиче.

| App-ключ (в `AuthedNavGraph`) | Граф фичи | Экран(ы) |
|---|---|---|
| `LoginRoute` / `RegistrationRoute` / `ForgotPasswordRoute` | `LoginNavGraph` (`feature/login`) | Login / Registration / ForgotPassword |
| `MainRoute` | `MainNavGraph` (`feature/main`) | `HomeRoute` → `MainScreen` |
| `SupportChatRoute` | `SupportChatEntry` (`feature/support-chat`, без собственного графа — экран один) | `SupportChatScreen` |
| `DeviceSelectionRoute` | `DeviceSelectionNavGraph` (`feature/device/selection`) | `ScanDeviceRoute` → экран поиска |
| `ConfiguratorRoute` | `ConfiguratorEntry` (`feature/configurator-ui`, свой nav3-граф) | «Мои конфигурации», модели, облако, редактор конфигурации |
| `VehicleSelectRoute(request)` | `VehicleSelectEntry` (`feature/vehicle-ui`, без nav3-графа: шаги каталога — в state VM, «назад» — `NavigationBackHandler`) | каталог автомобилей и карточка |
| `FirmwareArchiveRoute` | `FirmwareArchiveHome` (`composeApp/navigation/FirmwareArchiveWiring.kt`: `FirmwareSystemsEntry` → `FirmwareArchiveEntry` из `feature/device/firmware`, шаг — в state, «назад» — `NavigationBackHandler`) | архив прошивок без блока: системы и подсистемы → архив подсистемы |

Каждый граф фичи — свой `NavDisplay` со своим back stack и `entryDecorators`
(`rememberSaveableStateHolderNavEntryDecorator()` + `rememberViewModelStoreNavEntryDecorator()`),
экраны фичи и её ViewModel скрыты внутри графа (наружу — только graph-composable + app-ключ).

Из главного экрана в `SupportChatRoute` / `DeviceSelectionRoute` ведут карточки/пункты; `feature/main` только эмитит `MainNavigationTarget`, изменение app-level back stack остаётся ответственностью `composeApp` (`AuthedNavGraph`).

## Навигация внутри feature

Фича с несколькими экранами обязана иметь **собственный nav3-граф** внутри модуля:
`feature/<name>/.../presentation/navigation/<Name>NavGraph.kt` — свои `NavKey`-роуты,
свой `SavedStateConfiguration`, `rememberNavBackStack` + `NavDisplay`.

Фича с **одним экраном** граф не создаёт: её точка входа — публичный composable
(`<Name>Entry`), который `composeApp` размещает как entry в корневом `NavDisplay`
(как `SupportChatEntry`). При появлении второго экрана — вводить собственный граф.

- `composeApp` подключает граф фичи целиком (один composable-вызов в слоте/entry),
  а не отдельные экраны фичи.
- **Самодельный роутинг через enum / `mutableStateOf` / boolean-флаги запрещён.**
  Он ломает системную кнопку «назад» (и predictive back), теряет позицию во flow
  при пересоздании (plain `remember` не saveable) и не масштабируется на новые экраны.
- Системный back внутри фичи обрабатывает `NavDisplay.onBack` её графа. Выход за пределы
  фичи (смена зоны) — по-прежнему только через состояние сессии (`feature/auth-state`),
  экраны между зонами сами не навигируют.
- Каркас экрана (chrome: фон, top bar с title и back-кнопкой, actions) живёт **внутри entry**:
  каждая запись непрозрачна и рисует свой фон и свою панель (общий composable переиспользуется
  в каждой записи, как `AuthScreenChrome` в `LoginNavGraph`). На iOS `NavDisplay` анимирует
  push/pop сдвигом с затемнением уходящего экрана: прозрачные записи под общим chrome снаружи
  просвечивают друг сквозь друга, затемнение обрезано по контенту, а title/back в общей панели
  переключаются мгновенно, не вместе со страницей.

## Объявление маршрутов

Каждый маршрут — это `@Serializable data object` или `data class`, реализующий `NavKey`, с явным
`@SerialName("<модуль>.<Маршрут>")` (дискриминатор в сохранённом back stack; правило — `docs/patterns.md`,
«Сериализация»). Для multiplatform требуется `SavedStateConfiguration` с явной регистрацией типов.

```kotlin
@Serializable
@SerialName("main.MainRoute")
data object MainRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
        }
    }
}
```

## NavDisplay + entryProvider

```kotlin
@Composable
private fun MainNavGraph() {
    val backStack = rememberNavBackStack(navConfig, MainRoute)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            // Дефолтный декоратор: без него не работает SavedStateHandle.
            rememberSaveableStateHolderNavEntryDecorator(),
            // Per-entry ViewModelStoreOwner: VM entry живёт ровно столько,
            // сколько запись бэкстека (onStart/onCleared вместо DisposableEffect
            // и ручного сброса state).
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainScreen()
            }
        },
    )
}
```

**Оба декоратора обязательны в каждом `NavDisplay`** — корневом и во вложенных графах фич.
Без `rememberViewModelStoreNavEntryDecorator()` `koinViewModel()` отдаёт app-level инстанс,
который переживает уход с экрана и смену аккаунта, — отсюда соблазн чинить жизненный цикл
`DisposableEffect`'ами и ручным сбросом state (см. `docs/patterns.md`, «Жизненный цикл ViewModel
и сессии»). Без `rememberSaveableStateHolderNavEntryDecorator()` не работает `SavedStateHandle`.
Зависимость — `libs.androidx.lifecycle.viewmodel.nav3`.

## Жизненный цикл ViewModel: `entryDecorators`

По умолчанию `NavDisplay.entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator())`
— **без** `ViewModelStore`-декоратора. Тогда у entry нет своего `ViewModelStore`, и
`koinViewModel()` цепляется к `ViewModelStore` уровня Activity: **ViewModel НЕ очищается**
(`onCleared` не вызывается) при уходе с экрана (pop) и продолжает жить до смерти Activity.

Чтобы ViewModel экрана очищалась при снятии entry со стека (и создавалась заново при
повторном входе), в граф, где живут экраны с VM, добавляй `ViewModelStore`-декоратор:

```kotlin
NavDisplay(
    backStack = backStack,
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(), // сохранение saveable-состояния (дефолт)
        rememberViewModelStoreNavEntryDecorator(),      // свой ViewModelStore на entry → onCleared при pop
    ),
    onBack = { ... },
    entryProvider = entryProvider<NavKey> { ... },
)
```

- `rememberViewModelStoreNavEntryDecorator` — из `androidx.lifecycle.viewmodel.navigation3`
  (артефакт `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3`), подключается отдельно.
- Передавая `entryDecorators` вручную, **не забывай** оставить `rememberSaveableStateHolderNavEntryDecorator()`
  — это дефолтный декоратор, иначе потеряешь сохранение состояния.
- При навигации **вперёд** (push нового ключа поверх) entry остаётся в стеке и его VM живёт
  по семантике Nav3 (при возврате состояние восстанавливается); сканирование/подписки при этом
  приостанавливаются сами: `state` `BaseViewModel` — `WhileSubscribed`, привязки `collectIntoState` живут с подпиской.
- **Граф, уходящий из композиции без pop, свои VM не очищает.** Декоратор держит хранилища записей в ближайшем
  `LocalViewModelStoreOwner` (Activity): авторизованная зона после выхода из аккаунта просто покидает композицию, и
  без отдельной границы следующий вход получил бы VM прежнего аккаунта. Граница — `ViewModelScope(name, scopeKey)`
  (`core/ui/navigation`): VM поддерева живут, пока не сменится ключ; поворот экрана их сохраняет. `name` — отдельное
  хранилище на зону (во время перехода видны обе, и общее хранилище очищало бы VM соседа). `AuthStateNavGraph`
  оборачивает обе зоны — `ViewModelScope("auth.authed", session)` и `ViewModelScope("auth.no-auth", session)`, где
  `session` растёт при каждой смене состояния авторизации: после выхода формы входа/регистрации снова пустые.

## Навигационные действия

Операции над back stack — **только через безопасные хелперы** из
`core/ui` (`ru.alarmtrade.specmp.core.ui.navigation`, файл `NavBackStackSafe.kt`), а не
через сырые `+=` / `removeLastOrNull()` / `size`-проверки. Хелперы убирают дубли и
защищают от снятия последнего экрана.

```kotlin
import ru.alarmtrade.specmp.core.ui.navigation.navigateSafe
import ru.alarmtrade.specmp.core.ui.navigation.popSafe

backStack.navigateSafe(SupportChatRoute) // push без дубля (если уже наверху — no-op)
onBack = { backStack.popSafe() }          // pop только если в стеке >1
onBack = { if (!backStack.popSafe()) onClose() } // на корне графа — выйти из графа
```

Полный набор: `navigateSafe`, `replaceCurrent`, `popSafe()`, свойство `currentRoute`. Новый хелпер
добавляется под реальный вызов, «про запас» не держим.

## Настройка Gradle

В `composeApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlinx.serialization)
}

commonMain.dependencies {
    implementation(libs.compose.nav3)
    implementation(libs.kotlinx.serialization.json)
}
```

## Добавление нового маршрута

В `AppNavigation.kt`:

```kotlin
// 1. Объявить маршрут
@Serializable @SerialName("new-feature.NewFeatureRoute") data object NewFeatureRoute : NavKey

// 2. Зарегистрировать в navConfig
subclass(NewFeatureRoute::class, NewFeatureRoute.serializer())

// 3. Добавить entry в entryProvider
entry<NewFeatureRoute> { _ -> NewFeatureScreen(...) }
```

## Необходимые импорты

```kotlin
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
```
