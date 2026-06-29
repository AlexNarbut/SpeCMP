# Сборка — Команды / Зависимости / Ресурсы / Настройка платформ

## Возможности по платформам

| Возможность | macOS | Windows | Ubuntu |
|---|---|---|---|
| Сборка Android APK | ✅ | ✅ | ✅ |
| Сборка iOS framework | ✅ | ❌ | ❌ |
| Запуск Desktop приложения | ✅ | ✅ | ✅ |
| Нативный Desktop пакет | DMG | MSI | DEB |
| Android Emulator (аппаратная) | ✅ Hypervisor | ✅ WHPX/HAXM | ✅ KVM |

---

## Команды сборки

| Задача | Команда |
|---|---|
| Собрать всё | `./gradlew build` |
| Android debug APK | `./gradlew :androidApp:assembleDebug` |
| iOS framework | `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` |
| Запустить Desktop | `./gradlew :desktopApp:run` |
| Собрать Desktop пакет | `./gradlew :desktopApp:packageDistributionForCurrentOS` |
| Тесты | `./gradlew test` |
| Очистить | `./gradlew clean` |
| Очистить Konan-кеш | `rm -rf ~/.konan/cache` |

APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

Desktop-пакет: `desktopApp/build/compose/binaries/main/`

---

## Добавление зависимостей

Все зависимости добавляются в `gradle/libs.versions.toml`:

```toml
[versions]
my-lib = "1.0.0"

[libraries]
my-lib = { module = "com.example:my-lib", version.ref = "my-lib" }
```

Использование в `build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation(libs.my.lib)   // все платформы
}
androidMain.dependencies {
    implementation(libs.my.lib)   // только Android
}
jvmMain.dependencies {
    implementation(libs.my.lib)   // Desktop/JVM
}
iosMain.dependencies {
    implementation(libs.my.lib)   // только iOS
}
```

---

## Ресурсы

Расположение: `composeApp/src/commonMain/composeResources/`

```
composeResources/
├── drawable/   # XML vector drawables
├── values/     # strings.xml
└── font/       # TTF / OTF шрифты
```

```kotlin
import specmp.composeapp.generated.resources.Res
import specmp.composeapp.generated.resources.app_name

stringResource(Res.string.app_name)
painterResource(Res.drawable.ic_logo)
```

### Ресурсы в других модулях (`core/*`, `feature/*`)

Сейчас `composeResources/` есть только в `composeApp`. При выносе ресурсов в
library-модуль учитывать:

- Каждый KMP-library-модуль с `composeResources/` **обязан** включить
  `androidResources.enable = true` в блоке `android { … }` — иначе ресурсы не
  попадут в Android APK (в `composeApp/build.gradle.kts` это уже стоит).
- Настроить блок `compose.resources`:

  ```kotlin
  compose.resources {
      publicResClass = true          // true для общих модулей (core/ui); false для feature-модулей
      generateResClass = always
  }
  ```

- Пакет `Res`-класса формируется из `rootProject.name` + путь модуля:
  `composeApp` → `specmp.composeapp.generated.resources`,
  `core/ui` → `specmp.core.ui.generated.resources`,
  `feature/calculator` → `specmp.feature.calculator.generated.resources`.

---

## Платформо-специфичный код (expect/actual)

Предпочитать `commonMain`. Использовать `expect`/`actual` только для по-настоящему платформо-специфичного API.

```kotlin
// commonMain
expect fun platformName(): String

// androidMain
actual fun platformName(): String = "Android ${Build.VERSION.SDK_INT}"

// iosMain
actual fun platformName(): String = UIDevice.currentDevice.systemName()
```

Наборы источников: `commonMain`, `androidMain`, `jvmMain`, `iosMain`

---

## Версии ключевых библиотек

| Библиотека | Версия | Назначение |
|---|---|---|
| Compose Multiplatform | 1.10.0-rc02 | UI-фреймворк |
| Material3 | 1.10.0-alpha05 | Дизайн-компоненты |
| Ktor | 3.3.3 | HTTP-клиент |
| AndroidX Lifecycle | 2.10.0-alpha07 | ViewModel + Compose |
| AndroidX Navigation3 | 1.1.0-alpha01 | Навигация |
| Koin | 4.0.0 | Внедрение зависимостей |
| Kermit | 2.0.8 | Мультиплатформенное логирование |
| Kotlinx Serialization | 1.9.0 | JSON |
| Kotlinx Coroutines | 1.10.2 | Async |
| Coil | 3.3.0 | Загрузка изображений |

---

## Настройка платформ

### Общие требования (все платформы)

- **JDK 17** — Eclipse Temurin 17 LTS или Azul Zulu 17 (JDK 21+ работает, 23+ не тестировался)
- Всегда использовать **Gradle wrapper**: `./gradlew` (Unix/macOS) или `gradlew.bat` (Windows)
- Первая сборка скачивает Kotlin/Native toolchain (~300–500 MB в `~/.konan/`), занимает 10–20 мин

### Android SDK levels

| Параметр | Значение |
|---|---|
| compileSdk | 36 |
| minSdk | 24 |
| targetSdk | 36 |

`minSdk = 24` требуется Android-артефактом `ru.alarmtrade.common:specialist-android`.
Android wrapper также объявляет `ACCESS_COARSE_LOCATION`, потому что транзитивный
`bluetooth-common` manifest добавляет `ACCESS_FINE_LOCATION`, а Android 12+ lint
требует запрашивать оба location-разрешения парой.

---

### macOS

Единственная платформа для обоих целей: Android + iOS.

**1. Xcode**
```bash
xcode-select --install
sudo xcodebuild -license accept
# Минимум Xcode 15.x для iosArm64 / iosX64 / iosSimulatorArm64
```

**2. JDK 17**
```bash
brew install --cask temurin@17
# или: curl -s "https://get.sdkman.io" | bash && sdk install java 17.0.12-tem
```

**3. Android SDK** — через Android Studio (SDK Manager → Platform 36, Build-Tools 36) или:
```bash
brew install --cask android-commandlinetools
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

**4. `~/.zshrc`**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

**5. `local.properties`** (не коммитить)
```properties
sdk.dir=/Users/<username>/Library/Android/sdk
```

**6. iOS в Xcode**
```bash
open iosApp/iosApp.xcodeproj
# Build Phase автоматически вызывает:
# ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Основные настройки iOS находятся в `iosApp/Configuration/Config.xcconfig`:

```xcconfig
PRODUCT_NAME = SpeCMP
PRODUCT_BUNDLE_IDENTIFIER = ru.alarmtrade.specmp.iosApp
IPHONEOS_DEPLOYMENT_TARGET = 17.2
TEAM_ID =
```

Для симулятора `TEAM_ID` можно оставить пустым. Для запуска на физическом
устройстве укажите Apple Developer Team ID или выберите Team в Signing &
Capabilities внутри Xcode.

**Известные проблемы macOS**

| Проблема | Решение |
|---|---|
| iOS Release build OOM | Уже `-Xmx6G`; если мало — поднять до `-Xmx8G` |
| Xcode не находит gradlew | Build Phase должна выполняться из `$SRCROOT/..` |
| Konan-кеш несовместим с новым Xcode/SDK (undefined symbols из `*-cache.a`) | `rm -rf ~/.konan/cache`, затем пересобрать framework |

---

### Windows

Цель: Android APK. iOS — недоступен.

**1. JDK 17** — скачать `.msi` с https://adoptium.net/temurin/releases/?version=17

System Properties → Advanced → Environment Variables:
```
JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot
PATH     += %JAVA_HOME%\bin
```

**2. Android SDK** — через Android Studio (`%LOCALAPPDATA%\Android\Sdk`) или вручную:
```cmd
sdkmanager.bat "platform-tools" "platforms;android-36" "build-tools;36.0.0"
```

**3. Environment Variables** (PowerShell, Admin)
```powershell
[System.Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot", "User")
$p = [System.Environment]::GetEnvironmentVariable("PATH", "User")
[System.Environment]::SetEnvironmentVariable("PATH", "$p;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin", "User")
```

**4. `local.properties`**
```properties
sdk.dir=C\:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
```
Обратные слеши экранируются как `\\`.

**Известные проблемы Windows**

| Проблема | Решение |
|---|---|
| ⚠️ MAX_PATH = 260 символов (критично) | `Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1`; держать проект в `C:\dev\` |
| | `git config --system core.longpaths true` |
| Зависший Gradle daemon | `.\gradlew --stop` |
| Медленная сборка | Добавить `%USERPROFILE%\.gradle\` и `%USERPROFILE%\.konan\` в исключения Windows Defender |
| `'\r': command not found` в Git Bash | `git config core.autocrlf false && git checkout -- gradlew && chmod +x gradlew` |
| Android Emulator (Intel) | HAXM: https://github.com/intel/haxm/releases |
| Android Emulator (AMD) | Включить Windows Hypervisor Platform в "Windows Features" |

---

### Ubuntu Linux

Цель: Android APK. iOS — недоступен.

**1. JDK 17**
```bash
sudo apt update && sudo apt install -y openjdk-17-jdk
# или SDKMAN: curl -s "https://get.sdkman.io" | bash && sdk install java 17.0.12-tem
```

**2. Android SDK**
```bash
mkdir -p "$HOME/Android/sdk"
cd /tmp
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip -d "$HOME/Android/sdk/cmdline-tools"
mv "$HOME/Android/sdk/cmdline-tools/cmdline-tools" "$HOME/Android/sdk/cmdline-tools/latest"

"$HOME/Android/sdk/cmdline-tools/latest/bin/sdkmanager" \
    "platform-tools" "platforms;android-36" "build-tools;36.0.0"
yes | "$HOME/Android/sdk/cmdline-tools/latest/bin/sdkmanager" --licenses
```

**3. `~/.bashrc`**
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME="$HOME/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/emulator:$PATH"
```

**4. `local.properties`**
```properties
sdk.dir=/home/<username>/Android/sdk
```

**5. Android Emulator — KVM**
```bash
egrep -c '(vmx|svm)' /proc/cpuinfo   # > 0 — виртуализация есть
sudo apt install -y qemu-kvm libvirt-daemon-system cpu-checker
sudo kvm-ok
sudo usermod -aG kvm $USER && newgrp kvm
```

KVM в GitHub Actions:
```yaml
- name: Enable KVM
  run: |
    echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | \
      sudo tee /etc/udev/rules.d/99-kvm4all.rules
    sudo udevadm control --reload-rules && sudo udevadm trigger --name-match=kvm
```

**Известные проблемы Ubuntu**

| Проблема | Решение |
|---|---|
| `gradlew: Permission denied` | `chmod +x gradlew` |
| Gradle daemon OOM (8 GB RAM) | В `gradle.properties`: `-Xmx4G` вместо `-Xmx6G` |
| KVM недоступен в Docker | `docker run --device=/dev/kvm ...` |

---

## Heap-настройки

`gradle.properties` уже содержит оптимальные значения для 16 GB. При необходимости:

| RAM | `org.gradle.jvmargs` | `kotlin.daemon.jvmargs` |
|---|---|---|
| 8 GB | `-Xmx4G` | `-Xmx2G` |
| 16 GB | `-Xmx6G` ← текущее | `-Xmx4G` ← текущее |
| 32 GB+ | `-Xmx8G` | `-Xmx6G` |
