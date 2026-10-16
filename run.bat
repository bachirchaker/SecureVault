@echo off
title SecureVault - Enterprise Password Manager
cd /d "%~dp0"

echo =====================================================================
echo  Starting SecureVault (AES-256-GCM Enterprise Password Manager)
echo =====================================================================
echo.

:: Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Java\jdk-22\bin\java.exe" (
        set "PATH=C:\Program Files\Java\jdk-22\bin;%PATH%"
    ) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
        set "PATH=C:\Program Files\Java\jdk-21\bin;%PATH%"
    ) else (
        echo [ERROR] Java 21+ not found on PATH. Please install Java 21.
        pause
        exit /b 1
    )
)

:: Run compiled JAR if exists, otherwise run via maven wrapper
if exist "target\securevault-1.0.0.jar" (
    echo [INFO] Running SecureVault production JAR...
    echo [INFO] Open your browser at: http://localhost:8080
    echo [INFO] Press Ctrl+C in this window to stop the server.
    echo.
    java -jar target\securevault-1.0.0.jar
) else (
    echo [INFO] Building and starting SecureVault via Maven Wrapper...
    call mvnw.cmd spring-boot:run
)

pause
