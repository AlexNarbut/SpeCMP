# Навигация — androidx Navigation3 (nav3)

Библиотека: `org.jetbrains.androidx.navigation3:navigation3-ui`

## Основные понятия

- **Back stack** — обычный `SnapshotStateList`, управляемый вашим кодом (не библиотекой)
- **Маршруты (Routes)** — любые объекты, реализующие `NavKey`
- **NavDisplay** — отображает текущий destination из back stack
- **NavEntry** — связывает ключ маршрута с Compose-контентом

---

## Объявление маршрутов

Каждый маршрут — это `@Serializable data object` или `data class`, реализующий `NavKey`.
Для multiplatform (iOS не имеет рефлексии) требуется `SavedStateConfiguration` с явной регистрацией типов.

```kotlin
@Serializable
data object HomeRoute : NavKey

@Serializable
data object CalculatorRoute : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(CalculatorRoute::class, CalculatorRoute.serializer())
        }
    }
}
```

---

## NavDisplay + entryProvider

```kotlin
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navConfig, HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider<NavKey> {
            entry<HomeRoute> { _ ->
                HomeScreen(onNavigateToCalculator = { backStack += CalculatorRoute })
            }
            entry<CalculatorRoute> { _ ->
                CalculatorScreen(onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
```

---

## Навигационные действия

```kotlin
backStack += CalculatorRoute           // перейти вперёд
backStack.removeLastOrNull()           // назад
backStack.clear(); backStack += HomeRoute  // сброс к корню
```

---

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

---

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

---

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
