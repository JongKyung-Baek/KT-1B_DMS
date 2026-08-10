@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 exit /b 1

set "PROJECT_NAME=kt1b-dms-offline"
set "COMPOSE_FILE=%CD%\runtime\compose.yaml"
set "ENV_FILE=%CD%\runtime\offline.env"
set "SECRETS_FILE=%CD%\.env"

where docker.exe >nul 2>&1
if errorlevel 1 goto :failed
docker info >nul 2>&1
if errorlevel 1 goto :failed
docker compose version >nul 2>&1
if errorlevel 1 goto :failed

echo [INFO] Container logs follow below. Press Ctrl+C to stop.
echo [INFO] Application file logs are also stored in:
echo [INFO] %CD%\logs
echo.
docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" logs --tail 200 --follow app db
set "LOG_EXIT=%errorlevel%"
popd
endlocal & exit /b %LOG_EXIT%

:failed
echo [FAILED] Docker is not available.
popd
endlocal
exit /b 1
