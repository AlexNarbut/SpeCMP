#!/bin/sh
# Universal (arm64 + x86_64) libspecmp_notifications.dylib:
#   build.sh <SpecMpNotifications.swift> <выходной каталог> [подпись — mac.signIdentity]
# С подписью — codesign той же идентичностью, что и приложение (hardened runtime + timestamp — требования нотаризации):
# иначе подписанное приложение не загрузит библиотеку, а нотаризация отклонит пакет.
set -eu
SRC="$1"
OUT="$2"
IDENTITY="${3:-}"
TMP="$OUT/tmp"
LIB="$OUT/libspecmp_notifications.dylib"
mkdir -p "$TMP"
for ARCH in arm64 x86_64; do
  xcrun swiftc -emit-library -O -module-name SpecMpNotifications \
    -target "$ARCH-apple-macos12.0" -o "$TMP/libspecmp_notifications-$ARCH.dylib" "$SRC"
done
xcrun lipo -create -output "$LIB" \
  "$TMP/libspecmp_notifications-arm64.dylib" "$TMP/libspecmp_notifications-x86_64.dylib"
rm -rf "$TMP"
if [ -n "$IDENTITY" ]; then
  xcrun codesign --force --timestamp --options runtime --sign "$IDENTITY" "$LIB"
  xcrun codesign --verify --strict "$LIB"
fi
