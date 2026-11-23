#!/usr/bin/env bash
# Релиз версии из gradle/libs.versions.toml: Android google/huawei; на macOS ещё DMG с нотаризацией и iOS в App Store
# Connect (TestFlight), на Linux — DEB. Windows — release-all.ps1. Артефакты — build/release/<версия>/.
# См. docs/build.md → «Релиз».
set -euo pipefail
cd "$(dirname "$0")/../.."

prop() { grep -E "^$1=" local.properties 2>/dev/null | head -1 | cut -d= -f2- || true; }
toml() { grep -E "^$1 = " gradle/libs.versions.toml | head -1 | sed -E 's/.*= "(.*)"/\1/'; }

VERSION_NAME="$(toml app-versionName)"
VERSION_CODE="$(toml app-versionCode)"
OUT="build/release/${VERSION_NAME}-${VERSION_CODE}"
IOS_BUILD="build/release/ios"
SKIP=" ${RELEASE_SKIP:-} "
case "$(uname -s)" in
  Darwin) OS=mac ;;
  Linux) OS=linux; SKIP+=" ios " ;;
  *) echo "Неподдерживаемая ОС $(uname -s): на Windows — scripts/release/release-all.ps1" >&2; exit 1 ;;
esac

rm -rf "$OUT" "$IOS_BUILD"
mkdir -p "$OUT"
echo "== Релиз ${VERSION_NAME} (${VERSION_CODE}) → ${OUT}"

if [[ "$SKIP" != *" android "* ]]; then
  echo "== Android"
  ./gradlew :androidApp:assembleGoogleRelease :androidApp:assembleHuaweiRelease :androidApp:bundleGoogleRelease
  cp androidApp/build/outputs/apk/google/release/*.apk androidApp/build/outputs/apk/huawei/release/*.apk \
    androidApp/build/outputs/bundle/googleRelease/*.aab "$OUT"/
fi

if [[ "$SKIP" != *" desktop "* && "$OS" == linux ]]; then
  echo "== Linux"
  ./gradlew :desktopApp:packageDeb
  cp desktopApp/build/compose/binaries/main/deb/*.deb "$OUT"/
fi

if [[ "$SKIP" != *" desktop "* && "$OS" == mac ]]; then
  echo "== macOS"
  [[ -n "$(prop mac.signIdentity)" ]] || { echo "Нет mac.signIdentity в local.properties — DMG не будет подписан и нотаризован" >&2; exit 1; }
  # Нотаризация Compose несовместима с configuration cache.
  ./gradlew :desktopApp:packageDmg :desktopApp:notarizeDmg --no-configuration-cache
  cp desktopApp/build/compose/binaries/main/dmg/*.dmg "$OUT"/
fi

if [[ "$SKIP" != *" ios "* ]]; then
  echo "== iOS → TestFlight"
  TEAM_ID="$(grep -m1 'DEVELOPMENT_TEAM = ' iosApp/iosApp.xcodeproj/project.pbxproj | sed -E 's/.*= ([A-Z0-9]+);/\1/')"
  AUTH=()
  if [[ -n "$(prop asc.keyId)" ]]; then
    AUTH=(-authenticationKeyID "$(prop asc.keyId)" -authenticationKeyIssuerID "$(prop asc.issuerId)"
      -authenticationKeyPath "$(prop asc.keyPath)")
  fi
  mkdir -p "$IOS_BUILD"
  cat > "$IOS_BUILD/ExportOptions.plist" <<PLIST
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>method</key><string>app-store-connect</string>
  <key>destination</key><string>upload</string>
  <key>teamID</key><string>${TEAM_ID}</string>
  <key>signingStyle</key><string>automatic</string>
  <key>uploadSymbols</key><true/>
  <key>manageAppVersionAndBuildNumber</key><false/>
</dict>
</plist>
PLIST
  xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release \
    -destination 'generic/platform=iOS' -archivePath "$IOS_BUILD/SpeCMP.xcarchive" \
    -allowProvisioningUpdates ${AUTH[@]+"${AUTH[@]}"} archive
  xcodebuild -exportArchive -archivePath "$IOS_BUILD/SpeCMP.xcarchive" \
    -exportOptionsPlist "$IOS_BUILD/ExportOptions.plist" -exportPath "$IOS_BUILD/export" \
    -allowProvisioningUpdates ${AUTH[@]+"${AUTH[@]}"}
  echo "iOS ${VERSION_NAME} (${VERSION_CODE}) выгружен в App Store Connect — в TestFlight появится после обработки" > "$OUT/ios-testflight.txt"
fi

echo "== Готово: ${OUT}"
ls -la "$OUT"
