@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 exit /b 1

set "PROJECT_NAME=kt1b-dms-offline"
set "COMPOSE_FILE=%CD%\runtime\compose.yaml"
set "ENV_FILE=%CD%\runtime\offline.env"
set "PORT_FILE=%CD%\runtime\.demo-port"
set "DEMO_PORT=3508"
if exist "%PORT_FILE%" set /p DEMO_PORT=<"%PORT_FILE%"
if not "%~1"=="" set "DEMO_PORT=%~1"
set "DEMO_URL=http://127.0.0.1:%DEMO_PORT%/login/loginPage"

where docker.exe >nul 2>&1
if errorlevel 1 goto :not_ready
docker info >nul 2>&1
if errorlevel 1 goto :not_ready
docker compose version >nul 2>&1
if errorlevel 1 goto :not_ready

echo.
echo ============================================================
echo  KT-1B DMS Offline Demo - Status
echo ============================================================
echo.
docker compose --env-file "%ENV_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps
echo.

where curl.exe >nul 2>&1
if errorlevel 1 goto :check_powershell
curl.exe --fail --silent --show-error --max-time 3 "%DEMO_URL%" >nul 2>&1
if errorlevel 1 goto :not_ready
goto :ready

:check_powershell
powershell.exe -NoLogo -NoProfile -NonInteractive -Command ^
    "try { $response = Invoke-WebRequest -UseBasicParsing -Uri $env:DEMO_URL -TimeoutSec 3; if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) { exit 0 } } catch { }; exit 1"
if errorlevel 1 goto :not_ready

:ready
echo [OK] Web application is ready.
echo [OK] URL: %DEMO_URL%
popd
endlocal
exit /b 0

:not_ready
echo [WARN] Web application is not ready at %DEMO_URL%
popd
endlocal
exit /b 1
