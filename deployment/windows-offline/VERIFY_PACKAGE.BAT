@echo off
setlocal EnableExtensions DisableDelayedExpansion

pushd "%~dp0" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Cannot access the offline package folder.
    exit /b 1
)

if not exist "%CD%\runtime\Verify-Package.ps1" (
    echo [ERROR] Missing verifier: runtime\Verify-Package.ps1
    popd
    endlocal
    exit /b 1
)

echo [INFO] Verifying every packaged file with SHA-256...
powershell.exe -NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass ^
    -File "%CD%\runtime\Verify-Package.ps1"
set "VERIFY_EXIT=%errorlevel%"

if not "%VERIFY_EXIT%"=="0" (
    echo [FAILED] Package verification failed.
    echo [INFO] Do not install this copy. Transfer the original ZIP again.
) else (
    echo [OK] Package files are intact.
)

popd
endlocal & exit /b %VERIFY_EXIT%
