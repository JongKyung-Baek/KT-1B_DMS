@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Cannot access the demo folder.
    exit /b 1
)

set "PROJECT_NAME=kt1b-dms-demo"
set "COMPOSE_FILE=%CD%\runtime\compose.yaml"
set "DB_VOLUME=kt1b-dms-demo-db-data"

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

set "DOCKER_OS="
for /f "usebackq delims=" %%O in (`docker info --format "{{.OSType}}" 2^>nul`) do set "DOCKER_OS=%%O"
if /i not "%DOCKER_OS%"=="linux" (
    echo [ERROR] Docker is not using Linux containers.
    goto :failed
)

echo.
echo ============================================================
echo  KT-1B DMS Demo - Reset Database
echo ============================================================
echo.
echo [WARNING] All changes made to the demo database will be lost.
echo [INFO]    Only this volume will be removed:
echo [INFO]    %DB_VOLUME%
echo.
set "CONFIRM="
set /p "CONFIRM=Type RESET and press Enter to continue: "
if not "%CONFIRM%"=="RESET" (
    echo [CANCELLED] Nothing was changed.
    popd
    endlocal
    exit /b 0
)

echo [1/3] Stopping demo containers...
docker compose -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" down --remove-orphans
if errorlevel 1 goto :failed

echo [2/3] Removing the demo database volume...
docker volume inspect "%DB_VOLUME%" >nul 2>&1
if errorlevel 1 goto :volume_not_found
docker volume rm "%DB_VOLUME%"
if errorlevel 1 (
    echo [ERROR] The demo database volume could not be removed.
    goto :failed
)
goto :start_demo

:volume_not_found
echo [INFO] The demo database volume does not exist yet.

:start_demo
echo [3/3] Recreating the demo database and starting the application...
call "%CD%\INSTALL_AND_RUN.BAT"
set "START_EXIT=%errorlevel%"
popd
endlocal & exit /b %START_EXIT%

:failed
echo [FAILED] Demo data was not reset.
popd
endlocal
exit /b 1
