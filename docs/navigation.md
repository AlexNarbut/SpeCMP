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
fun AppNavigation(
    onLocaleChanged: (AppLocale) -> Unit,
) {
    AuthStateNavGraph(
        waitingState = { AppStartLoading() },
        noAuthState = { LoginNavGraph(onLocaleChanged = onLocaleChanged) },
        authedState = { MainNavGraph() },
        errorState = { onRetry -> SessionCheckErrorScreen(onRetry = onRetry) },
    )
}
```

Экраны не навигируют между зонами сами: успешный login и logout меняют состояние сессии, `AuthStateNavGraph` подхватывает изменение и переключает зону.

Nav3 back stack используется **внутри каждой зоны**: `LoginNavGraph` (`feature/login`) в неавторизованной, `MainNavGraph` (`composeApp`) в авторизованной.

| Route | Экран | Граф |
|---|---|---|
| `LoginRoute` | `LoginScreen` (`feature/login`) | `LoginNavGraph` |
| `RegistrationRoute` | `RegistrationScreen` (`feature/login`) | `LoginNavGraph` |
| `ForgotPasswordRoute` | `ForgotPasswordScreen` (`feature/login`) | `LoginNavGraph` |
| `MainRoute` | `MainScreen` (`feature/main`) | `MainNavGraph` |

## Навигация внутри feature

Фича с несколькими экранами обязана иметь **собственный nav3-граф** внутри модуля:
`feature/<name>/.../presentation/navigation/<Name>NavGraph.kt` — свои `NavKey`-роуты,
свой `SavedStateConfiguration`, `rememberNavBackStack` + `NavDisplay`.

- `composeApp` подключает граф фичи целиком (один composable-вызов в слоте/entry),
  а не отдельные экраны фичи.
- **Самодельный роутинг через enum / `mutableStateOf` / boolean-флаги запрещён.**
  Он ломает системную кнопку «назад» (и predictive back), теряет позицию во flow
  при пересоздании (plain `remember` не saveable) и не масштабируется на новые экраны.
- Системный back внутри фичи обрабатывает `NavDisplay.onBack` её графа. Выход за пределы
  фичи (смена зоны) — по-прежнему только через состояние сессии (`feature/auth-state`),
  экраны между зонами сами не навигируют.
- Общий каркас экранов фичи (chrome: title, back-кнопка, actions) может жить снаружи
  `NavDisplay`; title и наличие back-кнопки выводятся из последнего ключа back stack.

## Объявление маршрутов

Каждый маршрут — это `@Serializable data object` или `data class`, реализующий `NavKey`.
Для multiplatform требуется `SavedStateConfiguration` с явной регистрацией типов.

```kotlin
@Serializable
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
        entryProvider = entryProvider<NavKey> {
            entry<MainRoute> { _ ->
                MainScreen()
            }
        },
    )
}
```

## Навигационные действия

```kotlin
backStack += MainRoute
if (backStack.size > 1) backStack.removeLastOrNull()
```

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
@Serializable data object NewFeatureRoute : NavKey

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
