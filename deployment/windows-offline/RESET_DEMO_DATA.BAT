@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 exit /b 1

set "PROJECT_NAME=kt1b-dms-offline"
set "COMPOSE_FILE=%CD%\runtime\compose.yaml"
set "ENV_FILE=%CD%\runtime\offline.env"
set "DB_VOLUME=kt1b-dms-offline-db-data"

where docker.exe >nul 2>&1
if errorlevel 1 goto :failed
docker info >nul 2>&1
if errorlevel 1 goto :failed
docker compose version >nul 2>&1
if errorlevel 1 goto :failed

echo.
echo ============================================================
echo  KT-1B DMS Offline Demo - Reset Database
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

echo [1/3] Stopping offline demo containers...
docker compose --env-file "%ENV_FILE%" -p "%PROJECT_NAME%" -f "%COMPOSE_FILE%" down --remove-orphans
if errorlevel 1 goto :failed

echo [2/3] Removing only the offline demo database volume...
docker volume inspect "%DB_VOLUME%" >nul 2>&1
if errorlevel 1 goto :start_demo
docker volume rm "%DB_VOLUME%"
if errorlevel 1 goto :failed

:start_demo
echo [3/3] Restoring the bundled sample database...
call "%CD%\INSTALL_AND_RUN.BAT"
set "START_EXIT=%errorlevel%"
popd
endlocal & exit /b %START_EXIT%

:failed
echo [FAILED] Offline demo data was not reset.
popd
endlocal
exit /b 1
