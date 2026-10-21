# SpeCMP

### Android
To run the application on android device/emulator:  
 - open project in Android Studio and run imported android run configuration  

To build the application bundle:  
 - run `./gradlew :androidApp:assembleDebug`  
 - find `.apk` file in `androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### iOS
To run the application on iPhone device/simulator:  
 - Open `iosApp/iosApp.xcproject` in Xcode and run standard configuration  
 - Or use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) for Android Studio

### Desktop
To run the desktop application:
 - run `./gradlew :desktopApp:run`

To build a native package for the current OS:
 - run `./gradlew :desktopApp:packageDistributionForCurrentOS`
 - find the package in `desktopApp/build/compose/binaries/main/`
