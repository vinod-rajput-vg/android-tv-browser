#!/usr/bin/env sh

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
APP_BASE_NAME="$(basename "$0")"

JAVA_EXE=java
if command -v java &> /dev/null; then
    JAVA_EXE=$(command -v java)
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_EXE" -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
