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

echo [INFO] Stopping the KT-1B DMS demo...
docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" down --remove-orphans
if errorlevel 1 goto :failed

echo [OK] Demo stopped.
echo [OK] Database data was preserved.
popd
endlocal
exit /b 0

:failed
echo [FAILED] The demo could not be stopped cleanly.
popd
endlocal
exit /b 1
