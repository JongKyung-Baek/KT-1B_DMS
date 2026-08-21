@echo off
setlocal EnableExtensions DisableDelayedExpansion
title KT-1B DMS Server Start

set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"

set "LOCAL_ENV_FILE=%APP_HOME%\.env.local"
if exist "%LOCAL_ENV_FILE%" (
    echo [INFO] Loading local integration settings from ".env.local"...
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%LOCAL_ENV_FILE%") do (
        if /i "%%A"=="TDMS_VIEWER_ENABLED" set "TDMS_VIEWER_ENABLED=%%B"
        if /i "%%A"=="TDMS_VIEWER_BASE_URL" set "TDMS_VIEWER_BASE_URL=%%B"
        if /i "%%A"=="TDMS_VIEWER_CLIENT_ID" set "TDMS_VIEWER_CLIENT_ID=%%B"
        if /i "%%A"=="TDMS_VIEWER_CALLBACK_CLIENT_ID" set "TDMS_VIEWER_CALLBACK_CLIENT_ID=%%B"
        if /i "%%A"=="TDMS_VIEWER_SHARED_SECRET" set "TDMS_VIEWER_SHARED_SECRET=%%B"
        if /i "%%A"=="TDMS_VIEWER_WORK_DIR" set "TDMS_VIEWER_WORK_DIR=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_ENABLED" set "TDMS_PDF_CONVERSION_ENABLED=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_BASE_URL" set "TDMS_PDF_CONVERSION_BASE_URL=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_CLIENT_ID" set "TDMS_PDF_CONVERSION_CLIENT_ID=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_SHARED_SECRET" set "TDMS_PDF_CONVERSION_SHARED_SECRET=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_WORK_DIR" set "TDMS_PDF_CONVERSION_WORK_DIR=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_OUTPUT_FOLDER" set "TDMS_PDF_CONVERSION_OUTPUT_FOLDER=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_CONNECT_TIMEOUT_MS" set "TDMS_PDF_CONVERSION_CONNECT_TIMEOUT_MS=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_READ_TIMEOUT_MS" set "TDMS_PDF_CONVERSION_READ_TIMEOUT_MS=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_POLL_INTERVAL_MS" set "TDMS_PDF_CONVERSION_POLL_INTERVAL_MS=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_BATCH_SIZE" set "TDMS_PDF_CONVERSION_BATCH_SIZE=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_WORKER_THREADS" set "TDMS_PDF_CONVERSION_WORKER_THREADS=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_MAX_ATTEMPTS" set "TDMS_PDF_CONVERSION_MAX_ATTEMPTS=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_RETRY_DELAY_SECONDS" set "TDMS_PDF_CONVERSION_RETRY_DELAY_SECONDS=%%B"
        if /i "%%A"=="TDMS_PDF_CONVERSION_STALE_MINUTES" set "TDMS_PDF_CONVERSION_STALE_MINUTES=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_ENABLED" set "TDMS_STEP_VIEWER_ENABLED=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_BASE_URL" set "TDMS_STEP_VIEWER_BASE_URL=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_CLIENT_ID" set "TDMS_STEP_VIEWER_CLIENT_ID=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_CALLBACK_CLIENT_ID" set "TDMS_STEP_VIEWER_CALLBACK_CLIENT_ID=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_SHARED_SECRET" set "TDMS_STEP_VIEWER_SHARED_SECRET=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_WORK_DIR" set "TDMS_STEP_VIEWER_WORK_DIR=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_CONNECT_TIMEOUT_MS" set "TDMS_STEP_VIEWER_CONNECT_TIMEOUT_MS=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_READ_TIMEOUT_MS" set "TDMS_STEP_VIEWER_READ_TIMEOUT_MS=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_SIGNATURE_CLOCK_SKEW_SECONDS" set "TDMS_STEP_VIEWER_SIGNATURE_CLOCK_SKEW_SECONDS=%%B"
        if /i "%%A"=="TDMS_STEP_VIEWER_STATE_RETENTION_DAYS" set "TDMS_STEP_VIEWER_STATE_RETENTION_DAYS=%%B"
        if /i "%%A"=="TDMS_DISTRIBUTION_INTEGRATION_ENABLED" set "TDMS_DISTRIBUTION_INTEGRATION_ENABLED=%%B"
        if /i "%%A"=="TDMS_DISTRIBUTION_INTEGRATION_CLIENT_ID" set "TDMS_DISTRIBUTION_INTEGRATION_CLIENT_ID=%%B"
        if /i "%%A"=="TDMS_DISTRIBUTION_INTEGRATION_SOURCE_SYSTEM_ID" set "TDMS_DISTRIBUTION_INTEGRATION_SOURCE_SYSTEM_ID=%%B"
        if /i "%%A"=="TDMS_DISTRIBUTION_INTEGRATION_SHARED_SECRET" set "TDMS_DISTRIBUTION_INTEGRATION_SHARED_SECRET=%%B"
        if /i "%%A"=="TDMS_DISTRIBUTION_INTEGRATION_ADDITIONAL_CLIENTS" set "TDMS_DISTRIBUTION_INTEGRATION_ADDITIONAL_CLIENTS=%%B"
    )
)

set "DB_CONTAINER=kt1b-postgres"
set "SERVER_PORT=3508"
set "APP_URL=http://127.0.0.1:%SERVER_PORT%/login/loginPage"
set "WAR_FILE=%APP_HOME%\target\TDMS-KT-1B.war"
set "LOG_DIR=%APP_HOME%\run-logs"
set "STATE_FILE=%LOG_DIR%\kt1b-server.state.json"
set "STDOUT_LOG=%LOG_DIR%\server.out.log"
set "STDERR_LOG=%LOG_DIR%\server.err.log"
set "EXIT_CODE=0"

set "KT1B_APP_HOME=%APP_HOME%"
set "KT1B_WAR_FILE=%WAR_FILE%"
set "KT1B_STATE_FILE=%STATE_FILE%"
set "KT1B_STDOUT_LOG=%STDOUT_LOG%"
set "KT1B_STDERR_LOG=%STDERR_LOG%"

echo.
echo ============================================================
echo   KT-1B DMS Server Start
echo ============================================================
echo.

where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker CLI was not found.
    set "EXIT_CODE=1"
    goto :finish
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Desktop is not running or is not accessible.
    set "EXIT_CODE=1"
    goto :finish
)

docker inspect "%DB_CONTAINER%" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] PostgreSQL container "%DB_CONTAINER%" was not found.
    echo         This script uses the current PostgreSQL 17 baseline on port 5432.
    set "EXIT_CODE=1"
    goto :finish
)

set "DB_RUNNING="
for /f "delims=" %%R in ('docker inspect -f "{{.State.Running}}" "%DB_CONTAINER%" 2^>nul') do set "DB_RUNNING=%%R"
if /i not "%DB_RUNNING%"=="true" (
    echo [INFO] Starting PostgreSQL container "%DB_CONTAINER%"...
    docker start "%DB_CONTAINER%" >nul
    if errorlevel 1 (
        echo [ERROR] PostgreSQL container failed to start.
        set "EXIT_CODE=1"
        goto :finish
    )
) else (
    echo [OK] PostgreSQL container is already running.
)

echo [INFO] Waiting for PostgreSQL...
for /l %%I in (1,1,45) do (
    docker exec "%DB_CONTAINER%" pg_isready -q >nul 2>&1 && goto :database_ready
    ping.exe -n 2 127.0.0.1 >nul
)

echo [ERROR] PostgreSQL did not become ready within 45 seconds.
set "EXIT_CODE=1"
goto :finish

:database_ready
echo [OK] PostgreSQL is ready.

set "KT1B_DB_PASSWORD="
for /f "delims=" %%P in ('docker exec "%DB_CONTAINER%" printenv POSTGRES_PASSWORD 2^>nul') do if not defined KT1B_DB_PASSWORD set "KT1B_DB_PASSWORD=%%P"
if not defined KT1B_DB_PASSWORD (
    echo [ERROR] POSTGRES_PASSWORD is not available in "%DB_CONTAINER%".
    set "EXIT_CODE=1"
    goto :finish
)

if not defined KT1B_DB_USERNAME set "KT1B_DB_USERNAME=myuser"
if not defined KT1B_DB_URL set "KT1B_DB_URL=jdbc:postgresql://127.0.0.1:5432/kt1b"

where java.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java was not found in PATH. JDK 17 or later is required.
    set "EXIT_CODE=1"
    goto :finish
)

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

curl.exe -fsS --connect-timeout 2 -o NUL "%APP_URL%" >nul 2>&1
if not errorlevel 1 (
    echo [OK] KT-1B DMS is already running.
    echo      %APP_URL%
    goto :finish
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$war = Get-Item -LiteralPath $env:KT1B_WAR_FILE -ErrorAction SilentlyContinue;" ^
  "if ($null -eq $war) { exit 1 };" ^
  "$sourceRoot = Join-Path $env:KT1B_APP_HOME 'src';" ^
  "$newerSource = Get-ChildItem -LiteralPath $sourceRoot -Recurse -File | Where-Object { $_.LastWriteTimeUtc -gt $war.LastWriteTimeUtc } | Select-Object -First 1;" ^
  "$pom = Get-Item -LiteralPath (Join-Path $env:KT1B_APP_HOME 'pom.xml');" ^
  "if ($newerSource -or $pom.LastWriteTimeUtc -gt $war.LastWriteTimeUtc) { exit 1 }; exit 0"

if errorlevel 1 (
    echo [INFO] Building the current source because the WAR is missing or outdated...
    call "%APP_HOME%\mvnw.cmd" -DskipTests clean package
    if errorlevel 1 (
        echo [ERROR] Maven build failed.
        set "EXIT_CODE=1"
        goto :finish
    )
) else (
    echo [OK] Existing WAR is up to date.
)

if not exist "%WAR_FILE%" (
    echo [ERROR] WAR file was not created: "%WAR_FILE%"
    set "EXIT_CODE=1"
    goto :finish
)

if exist "%STATE_FILE%" del /q "%STATE_FILE%" >nul 2>&1

echo [INFO] Starting the web server in the background...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$arguments = '-jar ' + [char]34 + $env:KT1B_WAR_FILE + [char]34;" ^
  "$process = Start-Process -FilePath 'java.exe' -ArgumentList $arguments -WorkingDirectory $env:KT1B_APP_HOME -RedirectStandardOutput $env:KT1B_STDOUT_LOG -RedirectStandardError $env:KT1B_STDERR_LOG -WindowStyle Hidden -PassThru;" ^
  "$state = [ordered]@{ pid = $process.Id; startedAtUtc = $process.StartTime.ToUniversalTime().ToString('o'); war = $env:KT1B_WAR_FILE; port = %SERVER_PORT% };" ^
  "$state | ConvertTo-Json -Compress | Set-Content -LiteralPath $env:KT1B_STATE_FILE -Encoding ASCII;" ^
  "Write-Output ('[OK] Web server process started. PID=' + $process.Id)"

if errorlevel 1 (
    echo [ERROR] The web server process could not be started.
    set "EXIT_CODE=1"
    goto :finish
)

echo [INFO] Waiting for the web server...
for /l %%I in (1,1,90) do (
    curl.exe -fsS --connect-timeout 2 -o NUL "%APP_URL%" >nul 2>&1 && goto :server_ready
    ping.exe -n 2 127.0.0.1 >nul
)

echo [ERROR] The web server did not become ready within 90 seconds.
echo         Standard log: "%STDOUT_LOG%"
echo         Error log   : "%STDERR_LOG%"
powershell.exe -NoProfile -Command "if (Test-Path -LiteralPath $env:KT1B_STDOUT_LOG) { Get-Content -LiteralPath $env:KT1B_STDOUT_LOG -Tail 25 }"
set "EXIT_CODE=1"
goto :finish

:server_ready
echo [OK] KT-1B DMS is ready.
echo      URL : %APP_URL%
echo      LOG : %STDOUT_LOG%

:finish
echo.
if /i not "%~1"=="--no-pause" pause
endlocal & exit /b %EXIT_CODE%
