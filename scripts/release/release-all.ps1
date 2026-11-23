# Релиз на Windows: Android google/huawei и Windows-пакеты (MSI, EXE) версии из gradle/libs.versions.toml.
# macOS DMG и iOS (TestFlight) собираются только на Mac — release-all.sh. Артефакты — build/release/<версия>/.
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path "$PSScriptRoot\..\..")

function Toml([string]$key) {
    $line = Select-String -Path 'gradle/libs.versions.toml' -Pattern "^$key = " | Select-Object -First 1
    return ($line.Line -replace '.*= "(.*)"', '$1')
}

function Gradle([string[]]$tasks) {
    & .\gradlew.bat @tasks
    if ($LASTEXITCODE -ne 0) { throw "gradlew $($tasks -join ' ') завершился с кодом $LASTEXITCODE" }
}

$versionName = Toml 'app-versionName'
$versionCode = Toml 'app-versionCode'
$out = "build/release/$versionName-$versionCode"
$skip = " $env:RELEASE_SKIP "

if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Force -Path $out | Out-Null
Write-Host "== Релиз $versionName ($versionCode) -> $out"

if ($skip -notlike '* android *') {
    Write-Host '== Android'
    Gradle @(':androidApp:assembleGoogleRelease', ':androidApp:assembleHuaweiRelease', ':androidApp:bundleGoogleRelease')
    Copy-Item androidApp/build/outputs/apk/google/release/*.apk, androidApp/build/outputs/apk/huawei/release/*.apk, `
        androidApp/build/outputs/bundle/googleRelease/*.aab -Destination $out
}

if ($skip -notlike '* desktop *') {
    Write-Host '== Windows'
    Gradle @(':desktopApp:packageMsi', ':desktopApp:packageExe')
    Copy-Item desktopApp/build/compose/binaries/main/msi/*.msi, desktopApp/build/compose/binaries/main/exe/*.exe `
        -Destination $out
}

Write-Host '== iOS и macOS собираются только на Mac (release-all.sh)'
Write-Host "== Готово: $out"
Get-ChildItem $out
