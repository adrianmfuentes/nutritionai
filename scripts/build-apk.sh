#!/usr/bin/env bash
# Builds the Android APK and copies it to dist/android with a stamped filename.
#
# Usage: scripts/build-apk.sh [debug|release]
#   debug   (default) - always works, self-signed with the Android debug key.
#   release           - requires KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS,
#                        KEY_PASSWORD env vars pointing at a real keystore,
#                        otherwise Gradle produces an unsigned release APK.
set -euo pipefail

BUILD_TYPE="${1:-debug}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ANDROID_DIR="$REPO_ROOT/android"

case "$BUILD_TYPE" in
  debug)
    TASK="assembleDebug"
    APK_PATH="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
    ;;
  release)
    TASK="assembleRelease"
    APK_PATH="$ANDROID_DIR/app/build/outputs/apk/release/app-release.apk"
    ;;
  *)
    echo "Usage: $0 [debug|release]" >&2
    exit 1
    ;;
esac

cd "$ANDROID_DIR"
chmod +x ./gradlew
echo "Building $BUILD_TYPE APK..."
./gradlew "$TASK" --console=plain

if [ ! -f "$APK_PATH" ]; then
  echo "Expected APK not found at $APK_PATH" >&2
  exit 1
fi

VERSION_NAME=$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' app/build.gradle.kts | head -1)
TIMESTAMP=$(date +%Y%m%d%H%M%S)
OUT_DIR="$REPO_ROOT/dist/android"
mkdir -p "$OUT_DIR"
DEST_APK="$OUT_DIR/nutritionai-${VERSION_NAME:-unknown}-${BUILD_TYPE}-${TIMESTAMP}.apk"
cp "$APK_PATH" "$DEST_APK"

echo "APK ready: $DEST_APK"
