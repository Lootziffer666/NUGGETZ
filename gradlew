#!/usr/bin/env sh
#
# Self-bootstrapping Gradle wrapper.
# Downloads gradle-wrapper.jar on first run if missing (Google AI Studio repos never commit it).
#
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$JAR" ]; then
  mkdir -p "$SCRIPT_DIR/gradle/wrapper"
  echo "Downloading gradle-wrapper.jar (first run)..." >&2
  URL="https://raw.githubusercontent.com/gradle/gradle/v8.14.1/gradle/wrapper/gradle-wrapper.jar"
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$JAR" "$URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$JAR" "$URL"
  else
    echo "ERROR: curl or wget required to bootstrap the Gradle wrapper." >&2
    exit 1
  fi
fi

exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
