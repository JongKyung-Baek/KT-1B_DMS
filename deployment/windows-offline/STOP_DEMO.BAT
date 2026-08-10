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

echo [INFO] Stopping the KT-1B DMS offline demo...
docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" down --remove-orphans
if errorlevel 1 goto :failed

echo [OK] Demo stopped. Database data was preserved.
popd
endlocal
exit /b 0

:failed
echo [FAILED] The offline demo could not be stopped cleanly.
popd
endlocal
exit /b 1
