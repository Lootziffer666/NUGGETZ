@rem Self-bootstrapping Gradle wrapper for Windows.
@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar

if not exist "%JAR%" (
    echo Downloading gradle-wrapper.jar (first run)... 1>&2
    if not exist "%SCRIPT_DIR%gradle\wrapper" mkdir "%SCRIPT_DIR%gradle\wrapper"
    set "URL=https://raw.githubusercontent.com/gradle/gradle/v8.14.1/gradle/wrapper/gradle-wrapper.jar"
    powershell -Command "Invoke-WebRequest -Uri '!URL!' -OutFile '%JAR%'" 2>nul
    if not exist "%JAR%" (
        echo ERROR: Failed to download gradle-wrapper.jar. 1>&2
        exit /b 1
    )
)

java -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
