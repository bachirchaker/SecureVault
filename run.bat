@echo off
setlocal enabledelayedexpansion
title SecureVault - Enterprise Password Manager
cd /d "%~dp0"

echo =====================================================================
echo    SecureVault -- Enterprise Cybersecurity Password Manager
echo =====================================================================
echo.

:: 1. Locate Java 21+ specifically (avoid older Java 8 installations)
set "JAVA_CMD="

if exist "C:\Program Files\Java\jdk-22\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-22\bin\java.exe"
    goto :JAVA_FOUND
)

if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-21\bin\java.exe"
    goto :JAVA_FOUND
)

if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    goto :JAVA_FOUND
)

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    goto :JAVA_FOUND
)

:: Fallback to PATH java
set "JAVA_CMD=java"

:JAVA_FOUND
echo [*] Using Java runtime: "%JAVA_CMD%"
"%JAVA_CMD%" -version
echo.

:: 2. Check MySQL connection / port 3306
echo [*] Checking MySQL service on port 3306...
netstat -ano | findstr /R ":3306 " >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] MySQL does not appear to be running on port 3306!
    echo [WARNING] Please open XAMPP Control Panel and click "Start" next to MySQL.
    echo.
) else (
    echo [OK] MySQL is active and listening on port 3306.
    echo.
)

:: 3. Launch SecureVault JAR
if not exist "target\securevault-1.0.0.jar" (
    echo [*] Compiling application first...
    call mvnw.cmd package -DskipTests
)

echo [*] Starting SecureVault Application Server...
echo [*] Once you see "Started SecureVaultApplication", open:
echo.
echo     ==^>  http://localhost:8080
echo.
echo [*] Press Ctrl+C at any time to stop the server.
echo =====================================================================
echo.

start "" cmd /c "timeout /t 5 >nul && start http://localhost:8080"
"%JAVA_CMD%" -jar target\securevault-1.0.0.jar

pause
