@echo off
REM Build Portable JAR Script for Windows
REM Creates a single executable JAR with all dependencies included

echo =========================================
echo Building Portable FTP Server JAR
echo =========================================
echo.

REM Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean --quiet

if %ERRORLEVEL% NEQ 0 (
    echo X Build failed. Please check the error messages above.
    exit /b 1
)

echo [OK] Clean successful
echo.

REM Build the shadow JAR (fat JAR with all dependencies)
echo Building portable JAR with all dependencies...
call gradlew.bat shadowJar

if %ERRORLEVEL% NEQ 0 (
    echo X Build failed. Please check the error messages above.
    exit /b 1
)

echo.
echo =========================================
echo [OK] Build Successful!
echo =========================================
echo.

REM Find the generated JAR
for %%f in (build\libs\*-portable.jar) do (
    echo [JAR] Portable JAR created:
    echo    Location: %%f
    echo    Size: %%~zf bytes
    echo.
    echo To run the portable JAR:
    echo    java -jar %%f
    echo.
    echo Or copy the JAR to any system with Java installed:
    echo    copy %%f C:\path\to\destination\
    echo    java -jar SimpleFTPServer-portable-1.0.jar
)

echo.
echo =========================================

