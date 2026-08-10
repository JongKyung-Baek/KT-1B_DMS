@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Cannot access the demo folder.
    exit /b 1
)

set "PROJECT_NAME=kt1b-dms-demo"
set "COMPOSE_FILE=%CD%\runtime\compose.yaml"
set "SECRETS_FILE=%CD%\.env"

where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker was not found.
    goto :failed
)
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running.
    goto :failed
)
docker compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose V2 is not available.
    goto :failed
)

echo [INFO] Showing application and database logs.
echo [INFO] Press Ctrl+C to stop viewing logs.
echo.
docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" logs --tail 200 --follow app db
set "LOG_EXIT=%errorlevel%"
popd
endlocal & exit /b %LOG_EXIT%

:failed
popd
endlocal
exit /b 1
