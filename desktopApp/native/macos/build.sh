#!/bin/sh
# Universal (arm64 + x86_64) libspecmp_notifications.dylib из SwiftPM-пакета (Firebase Messaging — статически внутри):
#   build.sh <каталог пакета> <рабочий каталог SwiftPM> <выходной каталог> [подпись — mac.signIdentity]
# С подписью — codesign той же идентичностью, что и приложение (hardened runtime + timestamp — требования нотаризации):
# иначе подписанное приложение не загрузит библиотеку, а нотаризация отклонит пакет.
set -eu
PACKAGE="$1"
SCRATCH="$2"
OUT="$3"
IDENTITY="${4:-}"
LIB="$OUT/libspecmp_notifications.dylib"
xcrun swift build -c release --arch arm64 --arch x86_64 \
  --package-path "$PACKAGE" --scratch-path "$SCRATCH" --product specmp_notifications
mkdir -p "$OUT"
cp -f "$(xcrun swift build -c release --arch arm64 --arch x86_64 \
  --package-path "$PACKAGE" --scratch-path "$SCRATCH" --show-bin-path)/libspecmp_notifications.dylib" "$LIB"
if [ -n "$IDENTITY" ]; then
  xcrun codesign --force --timestamp --options runtime --sign "$IDENTITY" "$LIB"
  xcrun codesign --verify --strict "$LIB"
fi
