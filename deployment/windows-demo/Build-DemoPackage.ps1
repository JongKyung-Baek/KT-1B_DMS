[CmdletBinding()]
param(
    [string]$Version = (Get-Date -Format 'yyyyMMdd-HHmmss'),
    [string]$SourceDbContainer = 'kt1b-postgres',
    [string]$SourceDbName = 'kt1b',
    [string]$SourceDbUser = 'myuser',
    [string]$OutputRoot,
    [string]$PackagePrefix = 'KT1B-DMS-DEMO-WINDOWS',
    [switch]$SkipBuild,
    [switch]$RunTests,
    [switch]$SkipArchive
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2.0

function Invoke-NativeTool {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,

        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Executable failed with exit code $LASTEXITCODE."
    }
}

function Assert-File {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [string]$Description
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Description is missing: $Path"
    }
}

$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
if ([string]::IsNullOrWhiteSpace($OutputRoot)) {
    $releaseRoot = Join-Path $projectRoot 'release'
} else {
    $releaseRoot = [IO.Path]::GetFullPath($OutputRoot)
}
$packageName = "$PackagePrefix-$Version"
$packageDirectory = Join-Path $releaseRoot $packageName
$zipPath = Join-Path $releaseRoot "$packageName.zip"
$warSource = Join-Path $projectRoot 'target\TDMS-KT-1B.war'
$samplePdfSource = Join-Path $projectRoot 'deployment\windows-demo\assets\demo-document.pdf'
$sqlDirectorySource = Join-Path $projectRoot 'src\main\resources\sql'
$migrationManifestSource = Join-Path $sqlDirectorySource `
    'fresh_database_migration.psql'
$portabilitySqlSource = Join-Path $PSScriptRoot 'database\30-demo-portability.sql'

if (-not $SkipBuild) {
    Write-Host '[1/6] Building the application WAR...'
    Push-Location $projectRoot
    try {
        if ($RunTests) {
            Invoke-NativeTool -Executable (Join-Path $projectRoot 'mvnw.cmd') `
                -Arguments @('clean', 'package')
        } else {
            Invoke-NativeTool -Executable (Join-Path $projectRoot 'mvnw.cmd') `
                -Arguments @('-DskipTests', 'clean', 'package')
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host '[1/6] Reusing the existing application WAR.'
}

Assert-File -Path $warSource -Description 'Application WAR'
Assert-File -Path $samplePdfSource -Description 'Demo PDF'
Assert-File -Path $migrationManifestSource `
    -Description 'Fresh database migration manifest'
Assert-File -Path $portabilitySqlSource -Description 'Demo sanitization SQL'

$pdfBytes = [IO.File]::ReadAllBytes($samplePdfSource)
if ($pdfBytes.Length -lt 5 `
        -or $pdfBytes[0] -ne 0x25 `
        -or $pdfBytes[1] -ne 0x50 `
        -or $pdfBytes[2] -ne 0x44 `
        -or $pdfBytes[3] -ne 0x46 `
        -or $pdfBytes[4] -ne 0x2D) {
    throw "Demo file is not a PDF: $samplePdfSource"
}

Write-Host '[2/6] Preparing the release folder...'
if (Test-Path -LiteralPath $packageDirectory) {
    throw "Release folder already exists: $packageDirectory"
}
if (-not $SkipArchive -and (Test-Path -LiteralPath $zipPath)) {
    throw "Release ZIP already exists: $zipPath"
}

New-Item -ItemType Directory -Force -Path $releaseRoot | Out-Null
New-Item -ItemType Directory -Path $packageDirectory | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageDirectory 'app') | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageDirectory 'storage\demo') | Out-Null
New-Item -ItemType Directory -Path (Join-Path $packageDirectory 'logs') | Out-Null

$rootFiles = @(
    'Dockerfile',
    '.dockerignore',
    'INSTALL_AND_RUN.BAT',
    'START_DEMO.BAT',
    'STOP_DEMO.BAT',
    'STATUS_DEMO.BAT',
    'VIEW_LOGS.BAT',
    'RESET_DEMO_DATA.BAT',
    'README-FIRST.txt'
)

foreach ($fileName in $rootFiles) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot $fileName) `
        -Destination (Join-Path $packageDirectory $fileName)
}

# Keep command files compatible with older Windows Server cmd.exe versions.
Get-ChildItem -LiteralPath $packageDirectory -Filter '*.BAT' -File |
    ForEach-Object {
        $batchText = [IO.File]::ReadAllText(
            $_.FullName,
            [Text.Encoding]::ASCII
        )
        $batchText = $batchText -replace "`r?`n", "`r`n"
        [IO.File]::WriteAllText(
            $_.FullName,
            $batchText,
            [Text.Encoding]::ASCII
        )
    }

Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'runtime') `
    -Destination $packageDirectory -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'database') `
    -Destination $packageDirectory -Recurse
Copy-Item -LiteralPath $warSource `
    -Destination (Join-Path $packageDirectory 'app\TDMS-KT-1B.war')
Copy-Item -LiteralPath $samplePdfSource `
    -Destination (Join-Path $packageDirectory 'storage\demo\file.pdf')

# A UTF-8 BOM keeps Korean instructions readable in older Windows Notepad.
$readmePath = Join-Path $packageDirectory 'README-FIRST.txt'
$readmeText = [IO.File]::ReadAllText(
    $readmePath,
    (New-Object Text.UTF8Encoding($false))
)
[IO.File]::WriteAllText(
    $readmePath,
    $readmeText,
    (New-Object Text.UTF8Encoding($true))
)

Write-Host '[3/6] Creating a clean sample database backup...'
Invoke-NativeTool -Executable 'docker' -Arguments @(
    'inspect',
    '--format',
    '{{.State.Running}}',
    $SourceDbContainer
)

$temporaryDatabase = "kt1b_demo_package_$PID"
$sourceBackupInContainer = "/tmp/kt1b-demo-source-$PID.backup"
$migrationSqlDirectory = "/tmp/kt1b-demo-migration-$PID"
$portabilitySqlInContainer = "/tmp/demo-portability-$PID.sql"
$demoBackupInContainer = "/tmp/kt1b-demo-$PID.backup"
$demoBackupDestination = Join-Path $packageDirectory 'database\kt1b-demo.backup'

try {
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer,
        'pg_dump',
        '-U', $SourceDbUser,
        '-d', $SourceDbName,
        '-Fc',
        '--no-owner',
        '--no-privileges',
        '-f', $sourceBackupInContainer
    )

    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer,
        'createdb',
        '-U', $SourceDbUser,
        $temporaryDatabase
    )

    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer,
        'pg_restore',
        '-U', $SourceDbUser,
        '-d', $temporaryDatabase,
        '--no-owner',
        '--no-privileges',
        '--exit-on-error',
        '--single-transaction',
        $sourceBackupInContainer
    )

    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer, 'mkdir', '-p', $migrationSqlDirectory
    )
    foreach ($sqlFile in Get-ChildItem -LiteralPath $sqlDirectorySource -File) {
        Invoke-NativeTool -Executable 'docker' -Arguments @(
            'cp', $sqlFile.FullName,
            "${SourceDbContainer}:$migrationSqlDirectory/$($sqlFile.Name)"
        )
    }
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'cp', $portabilitySqlSource,
        "${SourceDbContainer}:$portabilitySqlInContainer"
    )

    # Apply the same complete, ordered schema migration used for a fresh local
    # database, including the single-portal cleanup and sample reset.
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer,
        'psql',
        '-U', $SourceDbUser,
        '-d', $temporaryDatabase,
        '-v', 'ON_ERROR_STOP=1',
        '-v', 'include_sample_data=true',
        '-f', "$migrationSqlDirectory/fresh_database_migration.psql"
    )
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer,
        'psql',
        '-U', $SourceDbUser,
        '-d', $temporaryDatabase,
        '-v', 'ON_ERROR_STOP=1',
        '-f', $portabilitySqlInContainer
    )

    $validationSql = @'
SELECT concat_ws(
    ',',
    (SELECT COUNT(*) FROM docs_user),
    (SELECT COUNT(*) FROM docs_dept),
    (SELECT COUNT(*) FROM docs_sw),
    (SELECT COUNT(*) FROM docs_sw_file),
    (SELECT COUNT(*) FROM docs_sw_sub_file),
    (SELECT COUNT(*) FROM docs_file_security_label),
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = 'public'
        AND table_name IN ('docs_menu', 'docs_user')
        AND column_name = 'auth_site')
);
'@
    $validationOutput = & docker exec $SourceDbContainer `
        psql -U $SourceDbUser -d $temporaryDatabase -Atqc $validationSql
    if ($LASTEXITCODE -ne 0) {
        throw 'Demo database validation query failed.'
    }
    if ($validationOutput.Trim() -ne '6,6,16,16,16,16,0') {
        throw "Unexpected demo database counts: $validationOutput"
    }

    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $SourceDbContainer,
        'pg_dump',
        '-U', $SourceDbUser,
        '-d', $temporaryDatabase,
        '-Fc',
        '--no-owner',
        '--no-privileges',
        '-f', $demoBackupInContainer
    )
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'cp',
        "${SourceDbContainer}:$demoBackupInContainer",
        $demoBackupDestination
    )
} finally {
    & docker exec $SourceDbContainer `
        dropdb -U $SourceDbUser --if-exists $temporaryDatabase | Out-Null
    & docker exec $SourceDbContainer `
        rm -rf `
        $sourceBackupInContainer `
        $migrationSqlDirectory `
        $portabilitySqlInContainer `
        $demoBackupInContainer | Out-Null
}

Assert-File -Path $demoBackupDestination -Description 'Sanitized demo backup'

Write-Host '[4/6] Writing version and checksum information...'
$versionLines = @(
    "Package: $packageName",
    "Created: $([DateTimeOffset]::Now.ToString('yyyy-MM-dd HH:mm:ss zzz'))",
    'Runtime: Java 17 / PostgreSQL 17.10',
    'Sample data: 6 users / 6 departments / 16 technical documents',
    'Default URL: http://127.0.0.1:3508/login/loginPage',
    'Default login: admin / esob!'
)
[IO.File]::WriteAllLines(
    (Join-Path $packageDirectory 'VERSION.txt'),
    $versionLines,
    [Text.Encoding]::ASCII
)

$checksumPath = Join-Path $packageDirectory 'checksums.sha256'
$checksumLines = Get-ChildItem -LiteralPath $packageDirectory -File -Recurse |
    Where-Object { $_.FullName -ne $checksumPath } |
    Sort-Object FullName |
    ForEach-Object {
        $relativePath = $_.FullName.Substring($packageDirectory.Length + 1)
        $relativePath = $relativePath.Replace('\', '/')
        $hash = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash
        "$hash  $relativePath"
    }
[IO.File]::WriteAllLines(
    $checksumPath,
    $checksumLines,
    [Text.Encoding]::ASCII
)

if ($SkipArchive) {
    Write-Host '[5/6] Skipping the ZIP archive for staging use.'
} else {
    Write-Host '[5/6] Creating the ZIP archive...'
    Compress-Archive -LiteralPath $packageDirectory `
        -DestinationPath $zipPath `
        -CompressionLevel Optimal
}

Write-Host '[6/6] Package completed.'
Write-Host "Folder: $packageDirectory"
if (-not $SkipArchive) {
    Write-Host "ZIP:    $zipPath"
    Write-Host "SHA256: $((Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash)"
}
