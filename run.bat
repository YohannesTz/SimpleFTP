@echo off
REM Simple FTP Server Run Script for Windows
REM This script builds and runs the FTP server application

echo Starting Simple FTP Server...
echo.

REM Build the project
echo Building project...
call gradlew.bat build --quiet

if %ERRORLEVEL% NEQ 0 (
    echo Build failed. Please check the error messages above.
    exit /b 1
)

echo Build successful!
echo.

REM Run the application
echo Launching FTP Server UI...
call gradlew.bat run --quiet --console=plain

