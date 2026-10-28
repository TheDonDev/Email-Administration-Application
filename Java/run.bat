@echo off
cd /d "%~dp0"

set SRC=src/emailapp
set CLASSES=bin
set LIB=lib/sqlite-jdbc.jar

echo === Email Administration Application ===
echo Building...

if not exist "%CLASSES%" mkdir "%CLASSES%"

javac -cp "%LIB%" -d "%CLASSES%" %SRC%\*.java

echo.
echo Build complete. Running application.
echo.

java -cp "%CLASSES%;%LIB%" emailapp.Main
pause
