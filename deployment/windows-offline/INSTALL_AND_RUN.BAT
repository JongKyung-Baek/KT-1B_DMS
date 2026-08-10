@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Cannot access the offline package folder.
    exit /b 1
)

set "PROJECT_NAME=kt1b-dms-offline"
set "COMPOSE_FILE=%CD%\runtime\compose.yaml"
set "ENV_FILE=%CD%\runtime\offline.env"
set "SECRETS_FILE=%CD%\.env"
set "SECRETS_INITIALIZER=%CD%\runtime\Initialize-DeploymentSecrets.ps1"
set "DB_VOLUME=kt1b-dms-offline-db-data"
set "PORT_FILE=%CD%\runtime\.demo-port"
set "VERIFY_MARKER=%CD%\runtime\.package-verified"
set "IMAGES_ARCHIVE=%CD%\images\kt1b-dms-offline-images.tar"
set "CHECKSUM_FILE=%CD%\checksums.sha256"
set "DEMO_PORT=3508"
set "PORT_ARGUMENT_SET=0"
set "OPEN_BROWSER=1"
set "PAUSE_ON_FAILURE=1"
set "READY_MESSAGE=Offline demo is ready."

:parse_args
if "%~1"=="" goto :args_parsed
if /i "%~1"=="--no-open" (
    set "OPEN_BROWSER=0"
) else if /i "%~1"=="--no-pause" (
    set "PAUSE_ON_FAILURE=0"
) else (
    set "DEMO_PORT=%~1"
    set "PORT_ARGUMENT_SET=1"
)
shift
goto :parse_args

:args_parsed
if exist "%PORT_FILE%" if "%PORT_ARGUMENT_SET%"=="0" set /p DEMO_PORT=<"%PORT_FILE%"
set "KT1B_DEMO_PORT=%DEMO_PORT%"
set "DEMO_URL=http://127.0.0.1:%DEMO_PORT%/login/loginPage"

echo.
echo ============================================================
echo  KT-1B DMS - Windows Offline Install and Run
echo ============================================================
echo.

powershell.exe -NoLogo -NoProfile -NonInteractive -Command ^
    "$portNumber = 0; if (-not [int]::TryParse($env:KT1B_DEMO_PORT, [ref]$portNumber) -or $portNumber -lt 1 -or $portNumber -gt 65535) { exit 1 }"
if errorlevel 1 (
    echo [ERROR] Invalid port: %DEMO_PORT%
    echo [INFO]  Example: INSTALL_AND_RUN.BAT 3510
    goto :failed
)

if not exist "%COMPOSE_FILE%" (
    echo [ERROR] Missing file: runtime\compose.yaml
    goto :failed
)
if not exist "%ENV_FILE%" (
    echo [ERROR] Missing file: runtime\offline.env
    goto :failed
)
if not exist "%SECRETS_INITIALIZER%" (
    echo [ERROR] Missing credential initializer: runtime\Initialize-DeploymentSecrets.ps1
    goto :failed
)
if not exist "%IMAGES_ARCHIVE%" (
    echo [ERROR] Missing Docker images: images\kt1b-dms-offline-images.tar
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
if not exist "%CD%\checksums.sha256" (
    echo [ERROR] Missing checksum manifest: checksums.sha256
    goto :failed
)

set "APP_IMAGE="
set "DB_IMAGE="
set "EXPECTED_APP_IMAGE_ID="
set "EXPECTED_DB_IMAGE_ID="
for /f "usebackq tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
    if /i "%%A"=="KT1B_APP_IMAGE" set "APP_IMAGE=%%B"
    if /i "%%A"=="KT1B_DB_IMAGE" set "DB_IMAGE=%%B"
    if /i "%%A"=="KT1B_APP_IMAGE_ID" set "EXPECTED_APP_IMAGE_ID=%%B"
    if /i "%%A"=="KT1B_DB_IMAGE_ID" set "EXPECTED_DB_IMAGE_ID=%%B"
)
if not defined APP_IMAGE (
    echo [ERROR] KT1B_APP_IMAGE is missing from runtime\offline.env.
    goto :failed
)
if not defined DB_IMAGE (
    echo [ERROR] KT1B_DB_IMAGE is missing from runtime\offline.env.
    goto :failed
)
if not defined EXPECTED_APP_IMAGE_ID (
    echo [ERROR] KT1B_APP_IMAGE_ID is missing from runtime\offline.env.
    goto :failed
)
if not defined EXPECTED_DB_IMAGE_ID (
    echo [ERROR] KT1B_DB_IMAGE_ID is missing from runtime\offline.env.
    goto :failed
)
if not exist "%CD%\storage" mkdir "%CD%\storage"
if errorlevel 1 (
    echo [ERROR] Cannot create the storage folder.
    goto :failed
)
if not exist "%CD%\logs" mkdir "%CD%\logs"
if errorlevel 1 (
    echo [ERROR] Cannot create the logs folder.
    goto :failed
)

> "%CD%\logs\.write-test" echo writable
if not exist "%CD%\logs\.write-test" (
    echo [ERROR] The package folder is not writable.
    echo [INFO]  Extract the ZIP to a local writable folder.
    goto :failed
)
del /q "%CD%\logs\.write-test" >nul 2>&1

where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker CLI was not found.
    echo [INFO]  Install Docker and Docker Compose V2 before entering the closed network.
    goto :failed
)

echo [1/7] Checking the Docker daemon...
docker info >nul 2>"%CD%\logs\docker-info-error.log"
if errorlevel 1 (
    echo [ERROR] Docker is not running or this account cannot access it.
    echo [INFO]  Start Docker. For Access Denied, run as Administrator or add
    echo [INFO]  this account to docker-users and sign in again.
    type "%CD%\logs\docker-info-error.log"
    goto :failed
)

docker compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose V2 is not available.
    goto :failed
)

set "DOCKER_OS="
for /f "usebackq delims=" %%O in (`docker info --format "{{.OSType}}" 2^>nul`) do set "DOCKER_OS=%%O"
if /i not "%DOCKER_OS%"=="linux" (
    echo [ERROR] Docker is not using Linux containers.
    echo [INFO]  Switch the Docker daemon to Linux container mode.
    goto :failed
)

echo [2/7] Checking package integrity...
set "MANIFEST_HASH="
for /f "usebackq delims=" %%H in (`powershell.exe -NoLogo -NoProfile -NonInteractive -Command "(Get-FileHash -LiteralPath $env:CHECKSUM_FILE -Algorithm SHA256).Hash"`) do set "MANIFEST_HASH=%%H"
set "VERIFIED_HASH="
if exist "%VERIFY_MARKER%" set /p VERIFIED_HASH=<"%VERIFY_MARKER%"
if /i "%VERIFIED_HASH%"=="%MANIFEST_HASH%" (
    echo [OK] The package was already verified. Run VERIFY_PACKAGE.BAT to verify again.
) else (
    call "%CD%\VERIFY_PACKAGE.BAT"
    if errorlevel 1 goto :failed
    > "%VERIFY_MARKER%" echo %MANIFEST_HASH%
    if errorlevel 1 (
        echo [ERROR] Cannot write the package verification marker.
        goto :failed
    )
)

echo [3/7] Loading bundled Docker images...
call :inspect_images
if not errorlevel 1 goto :images_ready
docker load --input "%IMAGES_ARCHIVE%"
if errorlevel 1 (
    echo [ERROR] Bundled Docker images could not be loaded.
    goto :failed
)
call :inspect_images
if errorlevel 1 (
    echo [ERROR] Loaded image IDs do not match runtime\offline.env.
    goto :failed
)

:images_ready
echo [OK] Application image: %APP_IMAGE%
echo [OK] Database image:    %DB_IMAGE%

call :ensure_secrets
if errorlevel 1 goto :failed

echo [4/7] Checking local port %DEMO_PORT%...
set "APP_CONTAINER="
set "APP_PUBLISHED="
set "PORT_OWNED_BY_PROJECT="
for /f "delims=" %%C in ('docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps -q app 2^>nul') do set "APP_CONTAINER=%%C"
if defined APP_CONTAINER call :inspect_existing_app
if not defined PORT_OWNED_BY_PROJECT call :check_port_available
if errorlevel 1 (
    echo [ERROR] Port %DEMO_PORT% is already in use by another program.
    echo [INFO]  Close it or run: INSTALL_AND_RUN.BAT 3510
    goto :failed
)

if defined PORT_OWNED_BY_PROJECT call :check_ready
if defined PORT_OWNED_BY_PROJECT if not errorlevel 1 (
    set "READY_MESSAGE=Offline demo is already running."
    goto :ready
)

echo [5/7] Starting the database and application without network access...
docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" up -d --no-build --pull never --remove-orphans
if errorlevel 1 (
    echo [ERROR] Docker Compose could not start the offline demo.
    goto :show_failure_logs
)

docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" exec -T db bash /docker-entrypoint-initdb.d/40-configure-demo-port.sh >nul 2>&1
if errorlevel 1 (
    echo [ERROR] The demo URL configuration could not be synchronized.
    goto :show_failure_logs
)

echo [6/7] Waiting for the web application...
for /l %%W in (1,1,90) do (
    call :check_ready
    if not errorlevel 1 goto :ready
    call :check_container_failure
    if errorlevel 1 goto :show_failure_logs
    if %%W EQU 1 echo [INFO]  Waiting up to 180 seconds...
    powershell.exe -NoLogo -NoProfile -NonInteractive -Command "Start-Sleep -Seconds 2" >nul 2>&1
)

echo [ERROR] The application did not become ready within 180 seconds.
goto :show_failure_logs

:ready
> "%PORT_FILE%" echo %DEMO_PORT%
if errorlevel 1 (
    echo [ERROR] Cannot save the selected port.
    goto :failed
)
echo [7/7] %READY_MESSAGE%
echo.
echo [OK] URL: %DEMO_URL%
echo [OK] Login: admin / esob!
echo [OK] No registry download or image build was used.
echo.
if "%OPEN_BROWSER%"=="1" (
    start "" "%DEMO_URL%" >nul 2>&1
    if errorlevel 1 echo [WARN] Open the URL above in a browser.
)
popd
endlocal
exit /b 0

:inspect_images
set "LOCAL_APP_IMAGE_ID="
set "LOCAL_DB_IMAGE_ID="
set "APP_PLATFORM="
set "DB_PLATFORM="
for /f "delims=" %%I in ('docker image inspect --format "{{.Id}}" "%APP_IMAGE%" 2^>nul') do set "LOCAL_APP_IMAGE_ID=%%I"
for /f "delims=" %%I in ('docker image inspect --format "{{.Id}}" "%DB_IMAGE%" 2^>nul') do set "LOCAL_DB_IMAGE_ID=%%I"
if /i not "%LOCAL_APP_IMAGE_ID%"=="%EXPECTED_APP_IMAGE_ID%" exit /b 1
if /i not "%LOCAL_DB_IMAGE_ID%"=="%EXPECTED_DB_IMAGE_ID%" exit /b 1
for /f "delims=" %%P in ('docker image inspect --format "{{.Os}}/{{.Architecture}}" "%APP_IMAGE%" 2^>nul') do set "APP_PLATFORM=%%P"
for /f "delims=" %%P in ('docker image inspect --format "{{.Os}}/{{.Architecture}}" "%DB_IMAGE%" 2^>nul') do set "DB_PLATFORM=%%P"
if /i not "%APP_PLATFORM%"=="linux/amd64" exit /b 1
if /i not "%DB_PLATFORM%"=="linux/amd64" exit /b 1
exit /b 0

:ensure_secrets
if exist "%SECRETS_FILE%" goto :validate_secrets
docker volume inspect "%DB_VOLUME%" >nul 2>&1
if not errorlevel 1 (
    echo [ERROR] Existing offline database data was found, but .env is missing.
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
powershell.exe -NoLogo -NoProfile -NonInteractive -Command ^
    "$listener = Get-NetTCPConnection -State Listen -LocalPort ([int]$env:KT1B_DEMO_PORT) -ErrorAction SilentlyContinue; if ($null -ne $listener) { exit 1 }; exit 0"
exit /b %errorlevel%

:check_ready
where curl.exe >nul 2>&1
if errorlevel 1 goto :check_ready_powershell
curl.exe --fail --silent --show-error --max-time 3 "%DEMO_URL%" >nul 2>&1
exit /b %errorlevel%

:check_ready_powershell
powershell.exe -NoLogo -NoProfile -NonInteractive -Command ^
    "try { $response = Invoke-WebRequest -UseBasicParsing -Uri $env:DEMO_URL -TimeoutSec 3; if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) { exit 0 } } catch { }; exit 1"
exit /b %errorlevel%

:check_container_failure
call :inspect_service app
if errorlevel 1 exit /b 1
call :inspect_service db
if errorlevel 1 exit /b 1
exit /b 0

:inspect_service
set "SERVICE_CONTAINER="
for /f "delims=" %%C in ('docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps -q %1 2^>nul') do set "SERVICE_CONTAINER=%%C"
if not defined SERVICE_CONTAINER exit /b 0
call :inspect_container "%SERVICE_CONTAINER%"
exit /b %errorlevel%

:inspect_container
set "CONTAINER_STATE="
set "CONTAINER_HEALTH="
for /f "delims=" %%S in ('docker inspect --format "{{.State.Status}}" %1 2^>nul') do set "CONTAINER_STATE=%%S"
for /f "delims=" %%H in ('docker inspect --format "{{if .State.Health}}{{.State.Health.Status}}{{end}}" %1 2^>nul') do set "CONTAINER_HEALTH=%%H"
if /i "%CONTAINER_STATE%"=="exited" exit /b 1
if /i "%CONTAINER_STATE%"=="dead" exit /b 1
if /i "%CONTAINER_HEALTH%"=="unhealthy" exit /b 1
exit /b 0

:show_failure_logs
echo.
docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" ps
echo.
echo [INFO] Recent container logs:
docker compose --env-file "%ENV_FILE%" --env-file "%SECRETS_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" logs --tail 120 app db
echo [INFO] Application file logs: %CD%\logs

:failed
echo.
echo [FAILED] The offline demo was not started.
echo [INFO]   Run VIEW_LOGS.BAT after Docker starts for more details.
if "%PAUSE_ON_FAILURE%"=="1" pause
popd
endlocal
exit /b 1
