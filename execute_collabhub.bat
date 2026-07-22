@echo off
setlocal enabledelayedexpansion

call mvnw clean package

IF ERRORLEVEL 1 (
    echo Build failed! Exiting...
    exit /b
)

echo Checking for processes running on port 80...

FOR /F "tokens=5" %%T IN ('netstat -aon ^| findstr :80 ^| findstr LISTENING') DO (
    SET /A ProcessId=%%T
    echo Found process using port 80: !ProcessId!
    taskkill /F /PID !ProcessId! /T
    echo Process !ProcessId! has been terminated.
)

echo Starting the WAR file...
java -jar target/StartupHub-0.0.1-SNAPSHOT.war

pause
