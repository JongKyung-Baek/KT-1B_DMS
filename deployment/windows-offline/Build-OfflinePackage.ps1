[CmdletBinding()]
param(
    [string]$Version = (Get-Date -Format 'yyyyMMdd-HHmmss'),
    [string]$SourceDbContainer = 'kt1b-postgres',
    [string]$SourceDbName = 'kt1b',
    [string]$SourceDbUser = 'myuser',
    [switch]$SkipBuild,
    [switch]$RunTests,
    [switch]$SkipImagePull
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

if ($Version -notmatch '^[A-Za-z0-9][A-Za-z0-9_.-]{0,79}$') {
    throw 'Version may contain only letters, numbers, dot, underscore and hyphen.'
}

$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$releaseRoot = Join-Path $projectRoot 'release'
$packageName = "KT1B-DMS-OFFLINE-WINDOWS-$Version"
$packageDirectory = Join-Path $releaseRoot $packageName
$zipPath = Join-Path $releaseRoot "$packageName.zip"
$zipHashPath = "$zipPath.sha256"
$baseBuilder = Join-Path $projectRoot `
    'deployment\windows-demo\Build-DemoPackage.ps1'
$stagingRoot = Join-Path ([IO.Path]::GetTempPath()) `
    "kt1b-dms-offline-$PID-$([Guid]::NewGuid().ToString('N'))"
$stagingPrefix = 'KT1B-DMS-OFFLINE-STAGING'
$stagingVersion = 'base'
$stagingPackage = Join-Path $stagingRoot `
    "$stagingPrefix-$stagingVersion"
$appImage = "kt1b-dms-demo-app:$($Version.ToLowerInvariant())"
$dbImage = 'postgres:17.10-bookworm'
$baseImage = 'eclipse-temurin:17-jre-jammy'
$imageArchiveName = 'kt1b-dms-offline-images.tar'

if (Test-Path -LiteralPath $packageDirectory) {
    throw "Release folder already exists: $packageDirectory"
}
if (Test-Path -LiteralPath $zipPath) {
    throw "Release ZIP already exists: $zipPath"
}
if (Test-Path -LiteralPath $zipHashPath) {
    throw "Release ZIP checksum already exists: $zipHashPath"
}

Assert-File -Path $baseBuilder -Description 'Connected build script'
Assert-File -Path (Join-Path $PSScriptRoot 'INSTALL_AND_RUN.BAT') `
    -Description 'Offline installer'
Assert-File -Path (Join-Path $PSScriptRoot 'runtime\compose.yaml') `
    -Description 'Offline Compose file'

try {
    Write-Host '[1/9] Building the WAR and sanitized sample database...'
    $baseParameters = @{
        Version = $stagingVersion
        SourceDbContainer = $SourceDbContainer
        SourceDbName = $SourceDbName
        SourceDbUser = $SourceDbUser
        OutputRoot = $stagingRoot
        PackagePrefix = $stagingPrefix
        SkipArchive = $true
    }
    if ($SkipBuild) {
        $baseParameters['SkipBuild'] = $true
    }
    if ($RunTests) {
        $baseParameters['RunTests'] = $true
    }
    & $baseBuilder @baseParameters

    $warPath = Join-Path $stagingPackage 'app\TDMS-KT-1B.war'
    $backupPath = Join-Path $stagingPackage 'database\kt1b-demo.backup'
    $secretsInitializerPath = Join-Path $stagingPackage `
        'runtime\Initialize-DeploymentSecrets.ps1'
    Assert-File -Path $warPath -Description 'Application WAR'
    Assert-File -Path $backupPath -Description 'Sanitized demo backup'
    Assert-File -Path $secretsInitializerPath `
        -Description 'Deployment credential initializer'

    Write-Host '[2/9] Preparing pinned Linux/AMD64 images...'
    if (-not $SkipImagePull) {
        Invoke-NativeTool -Executable 'docker' -Arguments @(
            'pull', '--platform', 'linux/amd64', $baseImage
        )
        Invoke-NativeTool -Executable 'docker' -Arguments @(
            'pull', '--platform', 'linux/amd64', $dbImage
        )
    }

    $buildArguments = @(
        'build',
        '--platform', 'linux/amd64',
        '--tag', $appImage,
        '--file', (Join-Path $stagingPackage 'Dockerfile')
    )
    if (-not $SkipImagePull) {
        $buildArguments += '--pull'
    }
    $buildArguments += $stagingPackage
    Invoke-NativeTool -Executable 'docker' -Arguments $buildArguments

    Write-Host '[3/9] Verifying image platform and embedded WAR...'
    $warHash = (Get-FileHash -LiteralPath $warPath `
        -Algorithm SHA256).Hash.ToLowerInvariant()
    $imageWarOutput = & docker run --rm --entrypoint sha256sum `
        $appImage /opt/kt1b/TDMS-KT-1B.war
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not hash the WAR embedded in the application image.'
    }
    $imageWarHash = (($imageWarOutput -split '\s+')[0]).ToLowerInvariant()
    if ($imageWarHash -ne $warHash) {
        throw "WAR/image mismatch. WAR=$warHash image=$imageWarHash"
    }

    $appPlatform = (& docker image inspect --format `
        '{{.Os}}/{{.Architecture}}' $appImage).Trim()
    $dbPlatform = (& docker image inspect --format `
        '{{.Os}}/{{.Architecture}}' $dbImage).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not inspect the packaged image platform.'
    }
    if ($appPlatform -ne 'linux/amd64' -or $dbPlatform -ne 'linux/amd64') {
        throw "Unexpected image platform: app=$appPlatform db=$dbPlatform"
    }

    $appImageId = (& docker image inspect --format '{{.Id}}' `
        $appImage).Trim()
    $dbImageId = (& docker image inspect --format '{{.Id}}' `
        $dbImage).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not inspect packaged image IDs.'
    }

    Write-Host '[4/9] Assembling the offline folder...'
    New-Item -ItemType Directory -Force -Path $releaseRoot | Out-Null
    Copy-Item -LiteralPath $stagingPackage `
        -Destination $packageDirectory -Recurse

    $rootFiles = @(
        'INSTALL_AND_RUN.BAT',
        'START_DEMO.BAT',
        'STOP_DEMO.BAT',
        'STATUS_DEMO.BAT',
        'VIEW_LOGS.BAT',
        'RESET_DEMO_DATA.BAT',
        'VERIFY_PACKAGE.BAT',
        'README-FIRST.txt'
    )
    foreach ($fileName in $rootFiles) {
        Copy-Item -LiteralPath (Join-Path $PSScriptRoot $fileName) `
            -Destination (Join-Path $packageDirectory $fileName) -Force
    }

    Copy-Item -LiteralPath (Join-Path $PSScriptRoot `
        'runtime\compose.yaml') -Destination (Join-Path `
        $packageDirectory 'runtime\compose.yaml') -Force
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot `
        'runtime\Verify-Package.ps1') -Destination (Join-Path `
        $packageDirectory 'runtime\Verify-Package.ps1') -Force

    $unusedRuntimeFiles = @(
        (Join-Path $packageDirectory 'Dockerfile'),
        (Join-Path $packageDirectory '.dockerignore'),
        (Join-Path $packageDirectory '.env'),
        (Join-Path $packageDirectory 'app')
    )
    foreach ($unusedPath in $unusedRuntimeFiles) {
        if (Test-Path -LiteralPath $unusedPath) {
            Remove-Item -LiteralPath $unusedPath -Recurse -Force
        }
    }

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

    Write-Host '[5/9] Exporting all Docker images into one archive...'
    $imagesDirectory = Join-Path $packageDirectory 'images'
    New-Item -ItemType Directory -Path $imagesDirectory | Out-Null
    $imageArchivePath = Join-Path $imagesDirectory $imageArchiveName
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'image', 'save',
        '--output', $imageArchivePath,
        $appImage,
        $dbImage
    )
    Assert-File -Path $imageArchivePath `
        -Description 'Offline Docker image archive'

    Write-Host '[6/9] Writing the image and release manifests...'
    $offlineEnvironment = @(
        "KT1B_APP_IMAGE=$appImage",
        "KT1B_DB_IMAGE=$dbImage",
        "KT1B_APP_IMAGE_ID=$appImageId",
        "KT1B_DB_IMAGE_ID=$dbImageId"
    )
    [IO.File]::WriteAllLines(
        (Join-Path $packageDirectory 'runtime\offline.env'),
        $offlineEnvironment,
        [Text.Encoding]::ASCII
    )

    $archiveHash = (Get-FileHash -LiteralPath $imageArchivePath `
        -Algorithm SHA256).Hash
    $backupHash = (Get-FileHash -LiteralPath (Join-Path `
        $packageDirectory 'database\kt1b-demo.backup') `
        -Algorithm SHA256).Hash
    $imageManifest = @(
        "Application image: $appImage",
        "Application image ID: $appImageId",
        "Application platform: $appPlatform",
        "Application WAR SHA256: $($warHash.ToUpperInvariant())",
        "Database image: $dbImage",
        "Database image ID: $dbImageId",
        "Database platform: $dbPlatform",
        "Image archive: images/$imageArchiveName",
        "Image archive SHA256: $archiveHash",
        "Sanitized database SHA256: $backupHash"
    )
    [IO.File]::WriteAllLines(
        (Join-Path $packageDirectory 'IMAGE-MANIFEST.txt'),
        $imageManifest,
        [Text.Encoding]::ASCII
    )

    $versionLines = @(
        "Package: $packageName",
        "Created: $([DateTimeOffset]::Now.ToString('yyyy-MM-dd HH:mm:ss zzz'))",
        'Target: Windows Server with Linux/AMD64 Docker and Compose V2',
        'Runtime: Java 17 / PostgreSQL 17.10',
        'Mode: Fully offline after Docker is installed',
        'Sample data: 6 users / 6 departments / 16 technical documents',
        'Default URL: http://127.0.0.1:3508/login/loginPage',
        'Default login: admin / esob!'
    )
    [IO.File]::WriteAllLines(
        (Join-Path $packageDirectory 'VERSION.txt'),
        $versionLines,
        [Text.Encoding]::ASCII
    )

    Write-Host '[7/9] Calculating SHA-256 checksums...'
    $checksumPath = Join-Path $packageDirectory 'checksums.sha256'
    $checksumLines = Get-ChildItem -LiteralPath $packageDirectory `
        -File -Recurse |
        Where-Object { $_.FullName -ne $checksumPath } |
        Sort-Object FullName |
        ForEach-Object {
            $relativePath = $_.FullName.Substring(
                $packageDirectory.Length + 1)
            $relativePath = $relativePath.Replace('\', '/')
            $hash = (Get-FileHash -LiteralPath $_.FullName `
                -Algorithm SHA256).Hash
            "$hash  $relativePath"
        }
    [IO.File]::WriteAllLines(
        $checksumPath,
        $checksumLines,
        [Text.Encoding]::ASCII
    )

    Write-Host '[8/9] Creating the offline ZIP archive...'
    Compress-Archive -LiteralPath $packageDirectory `
        -DestinationPath $zipPath `
        -CompressionLevel Optimal

    $zipHash = (Get-FileHash -LiteralPath $zipPath `
        -Algorithm SHA256).Hash
    [IO.File]::WriteAllText(
        $zipHashPath,
        "$zipHash  $([IO.Path]::GetFileName($zipPath))`r`n",
        [Text.Encoding]::ASCII
    )

    Write-Host '[9/9] Offline package completed.'
    Write-Host "Folder: $packageDirectory"
    Write-Host "ZIP:    $zipPath"
    Write-Host "SHA256: $zipHash"
    Write-Host "Hash:   $zipHashPath"
} finally {
    $resolvedTempRoot = [IO.Path]::GetFullPath(
        [IO.Path]::GetTempPath()).TrimEnd('\') + '\'
    $resolvedStagingRoot = [IO.Path]::GetFullPath($stagingRoot)
    if ($resolvedStagingRoot.StartsWith(
            $resolvedTempRoot,
            [StringComparison]::OrdinalIgnoreCase) `
            -and (Test-Path -LiteralPath $resolvedStagingRoot)) {
        Remove-Item -LiteralPath $resolvedStagingRoot -Recurse -Force
    }
}
