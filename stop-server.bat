@echo off
setlocal EnableExtensions DisableDelayedExpansion
title KT-1B DMS Server Stop

set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"

set "DB_CONTAINER=kt1b-postgres"
set "SERVER_PORT=3508"
set "LOG_DIR=%APP_HOME%\run-logs"
set "STATE_FILE=%LOG_DIR%\kt1b-server.state.json"
set "WAR_FILE=%APP_HOME%\target\SDMS-KT-1B.war"
set "EXIT_CODE=0"

set "KT1B_STATE_FILE=%STATE_FILE%"
set "KT1B_WAR_FILE=%WAR_FILE%"
set "KT1B_SERVER_PORT=%SERVER_PORT%"

echo.
echo ============================================================
echo   KT-1B DMS Server Stop
echo ============================================================
echo.

echo [INFO] Stopping the KT-1B web server...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$stopped = $false;" ^
  "$stateFile = $env:KT1B_STATE_FILE;" ^
  "$expectedWar = [IO.Path]::GetFullPath($env:KT1B_WAR_FILE);" ^
  "if (Test-Path -LiteralPath $stateFile) {" ^
  "  try {" ^
  "    $state = Get-Content -LiteralPath $stateFile -Raw | ConvertFrom-Json;" ^
  "    $process = Get-Process -Id ([int]$state.pid) -ErrorAction SilentlyContinue;" ^
  "    $sameWar = $state.war -and ([IO.Path]::GetFullPath([string]$state.war) -eq $expectedWar);" ^
  "    $sameStart = $process -and $state.startedAtUtc -and ([Math]::Abs(($process.StartTime.ToUniversalTime() - [datetime]::Parse([string]$state.startedAtUtc).ToUniversalTime()).TotalSeconds) -lt 5);" ^
  "    if ($process -and $process.ProcessName -match '^javaw?$' -and $sameWar -and $sameStart) {" ^
  "      Stop-Process -Id $process.Id -Force;" ^
  "      Wait-Process -Id $process.Id -Timeout 10 -ErrorAction SilentlyContinue;" ^
  "      Write-Output ('[OK] Web server stopped. PID=' + $process.Id);" ^
  "      $stopped = $true;" ^
  "    }" ^
  "  } catch { Write-Warning $_.Exception.Message }" ^
  "  Remove-Item -LiteralPath $stateFile -Force -ErrorAction SilentlyContinue;" ^
  "}" ^
  "if (-not $stopped) {" ^
  "  $listener = Get-NetTCPConnection -LocalPort ([int]$env:KT1B_SERVER_PORT) -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1;" ^
  "  if ($listener) {" ^
  "    $process = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue;" ^
  "    if ($process -and $process.ProcessName -match '^javaw?$') {" ^
  "      Stop-Process -Id $process.Id -Force;" ^
  "      Wait-Process -Id $process.Id -Timeout 10 -ErrorAction SilentlyContinue;" ^
  "      Write-Output ('[OK] Web server stopped by port ownership. PID=' + $process.Id);" ^
  "      $stopped = $true;" ^
  "    }" ^
  "  }" ^
  "}" ^
  "if (-not $stopped) { Write-Output '[OK] Web server is already stopped.' }"

if errorlevel 1 (
    echo [WARN] Web server shutdown verification reported an error.
    set "EXIT_CODE=1"
)

where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [WARN] Docker CLI was not found. PostgreSQL could not be checked.
    set "EXIT_CODE=1"
    goto :verify_web
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [WARN] Docker Desktop is not running. PostgreSQL could not be checked.
    set "EXIT_CODE=1"
    goto :verify_web
)

docker inspect "%DB_CONTAINER%" >nul 2>&1
if errorlevel 1 (
    echo [OK] PostgreSQL container "%DB_CONTAINER%" does not exist.
    goto :verify_web
)

set "DB_RUNNING="
for /f "delims=" %%R in ('docker inspect -f "{{.State.Running}}" "%DB_CONTAINER%" 2^>nul') do set "DB_RUNNING=%%R"
if /i "%DB_RUNNING%"=="true" (
    echo [INFO] Stopping PostgreSQL container "%DB_CONTAINER%"...
    docker stop "%DB_CONTAINER%"
    if errorlevel 1 (
        echo [WARN] PostgreSQL container failed to stop.
        set "EXIT_CODE=1"
    ) else (
        echo [OK] PostgreSQL stopped. The container and data volume were preserved.
    )
) else (
    echo [OK] PostgreSQL is already stopped.
)

:verify_web
curl.exe -fsS --connect-timeout 2 -o NUL "http://127.0.0.1:%SERVER_PORT%/login/loginPage" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] Port %SERVER_PORT% is still responding.
    set "EXIT_CODE=1"
) else (
    echo [OK] Port %SERVER_PORT% is closed.
)

echo.
if /i not "%~1"=="--no-pause" pause
endlocal & exit /b %EXIT_CODE%
