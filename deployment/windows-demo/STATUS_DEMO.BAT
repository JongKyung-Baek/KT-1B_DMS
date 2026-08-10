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
set "PORT_FILE=%CD%\runtime\.demo-port"
set "DEMO_PORT=3508"
if exist "%PORT_FILE%" set /p DEMO_PORT=<"%PORT_FILE%"
if not "%~1"=="" set "DEMO_PORT=%~1"
set "KT1B_DEMO_PORT=%DEMO_PORT%"
set "DEMO_URL=http://127.0.0.1:%DEMO_PORT%/login/loginPage"

where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker was not found.
    goto :not_ready
)
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running.
    goto :not_ready
)
docker compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose V2 is not available.
    goto :not_ready
)

echo.
echo ============================================================
echo  KT-1B DMS Demo - Status
echo ============================================================
echo.
docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps
echo.

call :check_ready
if errorlevel 1 goto :not_ready
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

:check_ready
where curl.exe >nul 2>&1
if errorlevel 1 goto :check_ready_powershell
curl.exe --fail --silent --show-error --max-time 3 "%DEMO_URL%" >nul 2>&1
exit /b %errorlevel%

:check_ready_powershell
powershell.exe -NoLogo -NoProfile -NonInteractive -Command "try { $response = Invoke-WebRequest -UseBasicParsing -Uri $env:DEMO_URL -TimeoutSec 3; if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) { exit 0 } } catch { }; exit 1"
exit /b %errorlevel%
