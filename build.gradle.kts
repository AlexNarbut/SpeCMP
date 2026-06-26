import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.android.kmp.library).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.buildConfig).apply(false)
}

var giteaUsername = System.getenv("GITEA_USERNAME")
var giteaPassword = System.getenv("GITEA_PASSWORD")
val localPropFile = file("local.properties")

if (localPropFile.exists()) {
    val properties = Properties().apply {
        load(localPropFile.reader())
    }
    if (properties["signing.GITEA_USERNAME"] != null)
        giteaUsername = properties["signing.GITEA_USERNAME"].toString()
    if (properties["signing.GITEA_PASSWORD"] != null)
        giteaPassword = properties["signing.GITEA_PASSWORD"].toString()
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
        maven {
            url = uri("http://git.pandora.pri/api/packages/mobile_guild/maven")
            isAllowInsecureProtocol = true
            credentials {
                username = giteaUsername
                password = giteaPassword
            }
        }
    }
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()
    }
}