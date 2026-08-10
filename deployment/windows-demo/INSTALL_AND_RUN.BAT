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
set "SECRETS_INITIALIZER=%CD%\runtime\Initialize-DeploymentSecrets.ps1"
set "DB_VOLUME=kt1b-dms-demo-db-data"
set "PORT_FILE=%CD%\runtime\.demo-port"
set "DEMO_PORT=3508"
set "OPEN_BROWSER=1"
set "READY_MESSAGE=Demo is ready."

if "%~1"=="" if exist "%PORT_FILE%" set /p DEMO_PORT=<"%PORT_FILE%"
if not "%~1"=="" (
    if /i "%~1"=="--no-open" (
        set "OPEN_BROWSER=0"
        if exist "%PORT_FILE%" set /p DEMO_PORT=<"%PORT_FILE%"
    ) else (
        set "DEMO_PORT=%~1"
    )
)
if /i "%~2"=="--no-open" set "OPEN_BROWSER=0"

set "KT1B_DEMO_PORT=%DEMO_PORT%"
set "DEMO_URL=http://127.0.0.1:%DEMO_PORT%/login/loginPage"

echo.
echo ============================================================
echo  KT-1B DMS Demo - Install and Run
echo ============================================================
echo.

powershell.exe -NoLogo -NoProfile -NonInteractive -Command "$portNumber = 0; if (-not [int]::TryParse($env:KT1B_DEMO_PORT, [ref]$portNumber) -or $portNumber -lt 1 -or $portNumber -gt 65535) { exit 1 }"
if errorlevel 1 (
    echo [ERROR] Invalid port: %DEMO_PORT%
    echo [INFO]  Example: INSTALL_AND_RUN.BAT 3510
    goto :failed
)

if not exist "%COMPOSE_FILE%" (
    echo [ERROR] Missing file: runtime\compose.yaml
    goto :failed
)
if not exist "%SECRETS_INITIALIZER%" (
    echo [ERROR] Missing credential initializer: runtime\Initialize-DeploymentSecrets.ps1
    goto :failed
)
if not exist "%CD%\Dockerfile" (
    echo [ERROR] Missing file: Dockerfile
    goto :failed
)
if not exist "%CD%\app\TDMS-KT-1B.war" (
    echo [ERROR] Missing application file: app\TDMS-KT-1B.war
    goto :failed
)
if not exist "%CD%\database\kt1b-demo.backup" (
    echo [ERROR] Missing database backup: database\kt1b-demo.backup
    goto :failed
)
if not exist "%CD%\database\10-restore-demo.sh" (
    echo [ERROR] Missing database initializer: database\10-restore-demo.sh
    goto :failed
)

if not exist "%CD%\storage" mkdir "%CD%\storage" >nul 2>&1
if not exist "%CD%\logs" mkdir "%CD%\logs" >nul 2>&1

where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker was not found.
    echo [INFO]  Install Docker, then run this file again.
    goto :failed
)

echo [1/5] Checking Docker...
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running.
    echo [INFO]  Start Docker and wait until it is ready.
    goto :failed
)

docker compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose V2 is not available.
    echo [INFO]  Update Docker and run this file again.
    goto :failed
)

set "DOCKER_OS="
for /f "usebackq delims=" %%O in (`docker info --format "{{.OSType}}" 2^>nul`) do set "DOCKER_OS=%%O"
if /i not "%DOCKER_OS%"=="linux" (
    echo [ERROR] Docker is not using Linux containers.
    echo [INFO]  Switch Docker to Linux container mode and try again.
    goto :failed
)

call :ensure_secrets
if errorlevel 1 goto :failed

echo [2/5] Checking local port %DEMO_PORT%...
set "APP_CONTAINER="
set "APP_PUBLISHED="
set "PORT_OWNED_BY_PROJECT="
for /f "delims=" %%C in ('docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps -q app 2^>nul') do set "APP_CONTAINER=%%C"
if defined APP_CONTAINER call :inspect_existing_app
if not defined PORT_OWNED_BY_PROJECT call :check_port_available
if errorlevel 1 (
    echo [ERROR] Port %DEMO_PORT% is already in use by another program.
    echo [INFO]  Close that program or use another port:
    echo [INFO]  INSTALL_AND_RUN.BAT 3510
    goto :failed
)

if defined PORT_OWNED_BY_PROJECT call :check_ready
if defined PORT_OWNED_BY_PROJECT if not errorlevel 1 (
    set "READY_MESSAGE=Demo is already running."
    goto :ready
)

echo [3/5] Preparing the database and application images...
echo [INFO]  The first run can take several minutes while images are downloaded.
docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" up -d --build --remove-orphans
if errorlevel 1 (
    echo [ERROR] Docker Compose could not start the demo.
    goto :show_failure_logs
)

docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" exec -T db bash /docker-entrypoint-initdb.d/40-configure-demo-port.sh >nul 2>&1
if errorlevel 1 (
    echo [ERROR] The demo URL configuration could not be synchronized.
    goto :show_failure_logs
)

echo [4/5] Waiting for the web application...
for /l %%W in (1,1,90) do (
    call :check_ready
    if not errorlevel 1 goto :ready
    if %%W EQU 1 echo [INFO]  Waiting up to 180 seconds...
    powershell.exe -NoLogo -NoProfile -NonInteractive -Command "Start-Sleep -Seconds 2" >nul 2>&1
)

echo [ERROR] The application did not become ready within 180 seconds.
goto :show_failure_logs

:ready
> "%PORT_FILE%" echo %DEMO_PORT%
echo [5/5] %READY_MESSAGE%
echo.
echo [OK] URL: %DEMO_URL%
echo [OK] Data and logs are stored in this demo folder.
echo.
if "%OPEN_BROWSER%"=="1" (
    start "" "%DEMO_URL%" >nul 2>&1
    if errorlevel 1 echo [WARN] Open the URL above in a browser.
)
popd
endlocal
exit /b 0

:show_failure_logs
echo.
docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps
echo.
echo [INFO] Recent container logs:
docker compose --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" logs --tail 120 app db
goto :failed

:failed
echo.
echo [FAILED] The demo was not started.
echo [INFO]   Run VIEW_LOGS.BAT for more details after Docker starts.
popd
endlocal
exit /b 1

:ensure_secrets
if exist "%SECRETS_FILE%" goto :validate_secrets
docker volume inspect "%DB_VOLUME%" >nul 2>&1
if not errorlevel 1 (
    echo [ERROR] Existing demo database data was found, but .env is missing.
    echo [INFO]  Copy the .env file from the previous installation into this folder.
    echo [INFO]  New credentials were not generated, so the database was not changed.
    exit /b 1
)

:validate_secrets
powershell.exe -NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass ^
    -File "%SECRETS_INITIALIZER%" -SecretsPath "%SECRETS_FILE%"
exit /b %errorlevel%

:inspect_existing_app
for /f "delims=" %%M in ('docker port "%APP_CONTAINER%" 3508/tcp 2^>nul') do set "APP_PUBLISHED=%%M"
if not defined APP_PUBLISHED exit /b 0
echo(%APP_PUBLISHED%| %SystemRoot%\System32\findstr.exe /e /l /c:":%DEMO_PORT%" >nul
if errorlevel 1 exit /b 0
set "PORT_OWNED_BY_PROJECT=1"
exit /b 0

:check_port_available
powershell.exe -NoLogo -NoProfile -NonInteractive -Command "$listener = Get-NetTCPConnection -State Listen -LocalPort ([int]$env:KT1B_DEMO_PORT) -ErrorAction SilentlyContinue; if ($null -ne $listener) { exit 1 }; exit 0"
exit /b %errorlevel%

:check_ready
where curl.exe >nul 2>&1
if errorlevel 1 goto :check_ready_powershell
curl.exe --fail --silent --show-error --max-time 3 "%DEMO_URL%" >nul 2>&1
exit /b %errorlevel%

:check_ready_powershell
powershell.exe -NoLogo -NoProfile -NonInteractive -Command "try { $response = Invoke-WebRequest -UseBasicParsing -Uri $env:DEMO_URL -TimeoutSec 3; if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) { exit 0 } } catch { }; exit 1"
exit /b %errorlevel%
