# Навигация — androidx Navigation3 (nav3)

Библиотека: `org.jetbrains.androidx.navigation3:navigation3-ui`.

## Основные понятия

- **Back stack** — обычный `SnapshotStateList`, управляемый кодом приложения.
- **Routes** — сериализуемые объекты, реализующие `NavKey`.
- **NavDisplay** — отображает текущий destination из back stack.
- **NavEntry** — связывает ключ маршрута с Compose-контентом.

## Текущий route graph

Auth-flow является стартовым экраном приложения. Демо-экран и демонстрационные маршруты удалены из composition root.

| Route | Экран |
|---|---|
| `LoginRoute` | `LoginScreen` / auth-root |

## Объявление маршрутов

Каждый маршрут — это `@Serializable data object` или `data class`, реализующий `NavKey`.
Для multiplatform требуется `SavedStateConfiguration` с явной регистрацией типов.

```kotlin
@Serializable
data object LoginRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginRoute::class, LoginRoute.serializer())
        }
    }
}
```

## NavDisplay + entryProvider

```kotlin
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navConfig, LoginRoute)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = entryProvider<NavKey> {
            entry<LoginRoute> { _ ->
                LoginScreen(
                    onLoginSuccess = {},
                )
            }
        },
    )
}
```

## Навигационные действия

```kotlin
backStack += LoginRoute
if (backStack.size > 1) backStack.removeLastOrNull()
```

Успешный login не сбрасывает stack на home-экран: auth-root сам переключается в logged-in placeholder state.

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
