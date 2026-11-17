#!/bin/sh
# Версия одна на все платформы — gradle/libs.versions.toml (app-versionName / app-versionCode): Xcode берёт её отсюда
# и пишет в собранный Info.plist (до подписи), в исходном Info.plist и Config.xcconfig версии нет.
set -eu
TOML="$SRCROOT/../gradle/libs.versions.toml"
NAME=$(sed -n 's/^app-versionName = "\(.*\)"$/\1/p' "$TOML")
CODE=$(sed -n 's/^app-versionCode = "\(.*\)"$/\1/p' "$TOML")
if [ -z "$NAME" ] || [ -z "$CODE" ]; then
  echo "error: app-versionName / app-versionCode not found in $TOML"
  exit 1
fi
PLIST="$TARGET_BUILD_DIR/$INFOPLIST_PATH"
set_key() {
  /usr/libexec/PlistBuddy -c "Set :$1 $2" "$PLIST" 2>/dev/null || /usr/libexec/PlistBuddy -c "Add :$1 string $2" "$PLIST"
}
set_key CFBundleShortVersionString "$NAME"
set_key CFBundleVersion "$CODE"
echo "App version $NAME ($CODE) -> $PLIST"
touch "$DERIVED_FILE_DIR/app-version.stamp"
