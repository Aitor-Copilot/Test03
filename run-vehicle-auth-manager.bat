@echo off
echo ===============================================================
echo    VEHICLE AUTHORIZATION DATABASE MANAGER
echo ===============================================================
echo Starting Java application...
echo Note: Database backup will be created automatically at startup
echo.

REM Change to the directory where this batch file is located
cd /d "%~dp0"

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or later and ensure it's in your PATH
    pause
    exit /b 1
)

REM Check if compiled classes exist
if not exist "target\classes\com\vehicleauth\VehicleAuthDatabaseManagerSimple.class" (
    echo ERROR: Java classes not found. Compiling...
    javac -d target\classes src\main\java\com\vehicleauth\VehicleAuthDatabaseManagerSimple.java
    if errorlevel 1 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
    echo.
)

REM Run the Java application
echo Running Vehicle Authorization Database Manager...
echo.
java -cp target\classes com.vehicleauth.VehicleAuthDatabaseManagerSimple

echo.
echo Application finished.
pause
