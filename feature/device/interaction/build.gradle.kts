plugins {
    id("specmp.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:specialist"))

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)

            implementation(libs.alarmtrade.specialist)
            // ConnectingStatus / ConnectionParameters / ConnectToBluetoothDeviceRequest /
            // BluetoothManufacturerData — пакеты ru.alarmtrade.bluetoothControl.* из bluetooth-common.
            implementation(libs.alarmtrade.bluetooth)

            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
