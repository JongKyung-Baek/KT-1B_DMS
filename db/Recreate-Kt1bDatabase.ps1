[CmdletBinding()]
param(
    [string]$Container = 'kt1b-postgres',
    [string]$Database = 'kt1b',
    [string]$DatabaseUser = 'myuser',
    [string]$MaintenanceDatabase = 'postgres',
    [string]$BackupPath,
    [switch]$DiscardExistingDatabase,
    [switch]$SkipSampleData,
    [switch]$VerifyRepeatableMigrations
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

function Assert-SafeIdentifier {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Value,

        [Parameter(Mandatory = $true)]
        [string]$Label
    )

    if ($Value -notmatch '^[A-Za-z_][A-Za-z0-9_.-]{0,62}$') {
        throw "$Label contains unsupported characters: $Value"
    }
}

function Copy-FileToContainer {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Source,

        [Parameter(Mandatory = $true)]
        [string]$Destination
    )

    # Stream through docker exec instead of docker cp. This also works when an
    # old container has a stale host bind mount from an earlier project layout.
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = 'docker'
    $startInfo.Arguments =
        "exec -i $Container dd of=$Destination status=none"
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardInput = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $sourceStream = $null
    try {
        if (-not $process.Start()) {
            throw 'Could not start docker for the database file transfer.'
        }
        $sourceStream = [IO.File]::OpenRead($Source)
        $sourceStream.CopyTo($process.StandardInput.BaseStream)
        $process.StandardInput.BaseStream.Flush()
        $process.StandardInput.Close()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            throw "docker file transfer failed with exit code $($process.ExitCode)."
        }
    } finally {
        if ($null -ne $sourceStream) {
            $sourceStream.Dispose()
        }
        $process.Dispose()
    }
}

$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$sqlDirectory = Join-Path $projectRoot 'src\main\resources\sql'
$migrationManifest = Join-Path $sqlDirectory 'fresh_database_migration.psql'

if ([string]::IsNullOrWhiteSpace($BackupPath)) {
    $resolvedBackup = Join-Path $PSScriptRoot '260701dumpdb3.backup'
} else {
    $resolvedBackup = [IO.Path]::GetFullPath($BackupPath)
}

Assert-SafeIdentifier -Value $Container -Label 'Container'
Assert-SafeIdentifier -Value $Database -Label 'Database'
Assert-SafeIdentifier -Value $DatabaseUser -Label 'DatabaseUser'
Assert-SafeIdentifier -Value $MaintenanceDatabase -Label 'MaintenanceDatabase'

if ($Database -in @('postgres', 'template0', 'template1')) {
    throw "Refusing to replace PostgreSQL system database '$Database'."
}
if (-not $DiscardExistingDatabase) {
    throw @'
This command permanently replaces the selected database. Re-run it with
-DiscardExistingDatabase after confirming the Container and Database values.
'@
}
if (-not (Test-Path -LiteralPath $resolvedBackup -PathType Leaf)) {
    throw "Base database backup is missing: $resolvedBackup"
}
if (-not (Test-Path -LiteralPath $migrationManifest -PathType Leaf)) {
    throw "Migration manifest is missing: $migrationManifest"
}

$expectedDdl = Get-ChildItem -LiteralPath $sqlDirectory `
    -Filter '*_ddl.sql' -File | Sort-Object Name
$manifestText = [IO.File]::ReadAllText(
    $migrationManifest,
    (New-Object Text.UTF8Encoding($false))
)
foreach ($ddl in $expectedDdl) {
    $referencePattern = '(?m)^\\ir\s+' + [Regex]::Escape($ddl.Name) + '\s*$'
    $referenceCount = [Regex]::Matches(
        $manifestText,
        $referencePattern
    ).Count
    if ($referenceCount -ne 1) {
        throw "Migration manifest must include $($ddl.Name) exactly once."
    }
}

$containerRunning = (& docker inspect --format '{{.State.Running}}' `
    $Container 2>$null).Trim()
if ($LASTEXITCODE -ne 0 -or $containerRunning -ne 'true') {
    throw "PostgreSQL container is not running: $Container"
}

$serverVersion = (& docker exec $Container psql `
    -U $DatabaseUser -d $MaintenanceDatabase -Atqc `
    'SHOW server_version_num').Trim()
if ($LASTEXITCODE -ne 0 -or $serverVersion -notmatch '^17[0-9]{4}$') {
    throw "PostgreSQL 17 is required. Server version number: $serverVersion"
}

$runId = "$PID-$([Guid]::NewGuid().ToString('N'))"
$containerWorkDirectory = "/tmp/kt1b-fresh-$runId"
$containerBackup = "$containerWorkDirectory/base.backup"
$containerSqlDirectory = "$containerWorkDirectory/sql"
$includeSampleData = if ($SkipSampleData) { 'false' } else { 'true' }

Write-Host "PostgreSQL: $serverVersion"
Write-Host "Target:     $Container/$Database"
Write-Host "Backup:     $resolvedBackup"
Write-Host "Sample:     $includeSampleData"

try {
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $Container, 'mkdir', '-p', $containerSqlDirectory
    )
    Copy-FileToContainer -Source $resolvedBackup `
        -Destination $containerBackup

    foreach ($sqlFile in Get-ChildItem -LiteralPath $sqlDirectory -File) {
        Copy-FileToContainer -Source $sqlFile.FullName `
            -Destination "$containerSqlDirectory/$($sqlFile.Name)"
    }

    Write-Host '[1/4] Dropping and recreating the target database...'
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $Container,
        'dropdb', '-U', $DatabaseUser,
        '--if-exists', '--force', $Database
    )
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $Container,
        'createdb', '-U', $DatabaseUser,
        '--encoding=UTF8', '--template=template0', $Database
    )

    Write-Host '[2/4] Restoring the tracked base backup...'
    Invoke-NativeTool -Executable 'docker' -Arguments @(
        'exec', $Container,
        'pg_restore', '-U', $DatabaseUser, '-d', $Database,
        '--no-owner', '--no-privileges', '--exit-on-error',
        '--single-transaction', $containerBackup
    )

    Write-Host '[3/4] Applying the complete current migration manifest...'
    $migrationArguments = @(
        'exec', $Container,
        'psql', '-U', $DatabaseUser, '-d', $Database,
        '-v', 'ON_ERROR_STOP=1',
        '-v', "include_sample_data=$includeSampleData",
        '-f', "$containerSqlDirectory/fresh_database_migration.psql"
    )
    Invoke-NativeTool -Executable 'docker' -Arguments $migrationArguments

    if ($VerifyRepeatableMigrations) {
        Write-Host '      Reapplying the manifest to verify repeatability...'
        Invoke-NativeTool -Executable 'docker' -Arguments $migrationArguments
    }

    Write-Host '[4/4] Verifying schema and internal menu/ACL state...'
    $validationQuery = @'
SELECT concat_ws('|',
    current_setting('server_version_num'),
    (SELECT COUNT(*) FROM information_schema.tables
      WHERE table_schema = 'public'),
    (SELECT COUNT(*) FROM docs_menu
      WHERE COALESCE(use_yn, 'N') = 'Y'
        AND COALESCE(del_yn, 'Y') = 'N'),
    (SELECT COUNT(*) FROM docs_menu
      WHERE COALESCE(use_yn, 'N') = 'Y'
        AND COALESCE(del_yn, 'Y') = 'N'
        AND tree_type = 'root'
        AND menu_type IN ('T', 'M', 'P')),
    (SELECT string_agg(menu_cd, ',' ORDER BY menu_cd) FROM docs_menu
      WHERE COALESCE(use_yn, 'N') = 'Y'
        AND COALESCE(del_yn, 'Y') = 'N'
        AND tree_type = 'root'
        AND menu_type IN ('T', 'M', 'P')),
    (SELECT COUNT(*) FROM docs_menu
      WHERE COALESCE(use_yn, 'N') = 'Y'
        AND COALESCE(del_yn, 'Y') = 'N'
        AND role_cd IS NOT NULL
        AND menu_type IN ('T', 'M', 'P')),
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema = 'public'
        AND table_name IN ('docs_menu', 'docs_user')
        AND column_name = 'auth_site'),
    ((SELECT COUNT(*) FROM docs_menu
       WHERE COALESCE(menu_url, '') ~* '(^|/)outside/')
     +
     (SELECT COUNT(*) FROM docs_role_mapping
       WHERE COALESCE(menu_url, '') ~* '(^|/)outside/')),
    ((SELECT COUNT(*) FROM docs_menu
       WHERE menu_url = '/inside' OR menu_url LIKE '/inside/%')
     +
     (SELECT COUNT(*) FROM docs_role_mapping
       WHERE menu_url = '/inside' OR menu_url LIKE '/inside/%')
     +
     (SELECT COUNT(*) FROM docs_form_info
       WHERE search_url = '/inside' OR search_url LIKE '/inside/%')),
    ((SELECT COUNT(*) FROM docs_menu
       WHERE menu_type = 'H'
          OR COALESCE(menu_url, '') IN ('/inside/**', '/general/**', '/outside/**'))
     +
     (SELECT COUNT(*) FROM docs_role_mapping
       WHERE COALESCE(menu_url, '') IN ('/inside/**', '/general/**', '/outside/**'))),
    (SELECT COUNT(*) FROM docs_menu menu
      WHERE COALESCE(menu.use_yn, 'N') = 'Y'
        AND COALESCE(menu.del_yn, 'Y') = 'N'
        AND menu.role_cd IS NOT NULL
        AND menu.menu_type IN ('T', 'M', 'P')
        AND NOT EXISTS (
            SELECT 1 FROM docs_rel_role_group assignment
             WHERE assignment.group_cd = 'RG_001'
               AND assignment.role_cd = menu.role_cd
        )),
    (SELECT COUNT(*) FROM docs_rel_role_group assignment
      WHERE NOT EXISTS (
            SELECT 1 FROM docs_menu menu
             WHERE menu.role_cd = assignment.role_cd
               AND COALESCE(menu.use_yn, 'N') = 'Y'
               AND COALESCE(menu.del_yn, 'Y') = 'N'
        )),
    (SELECT COUNT(*) FROM docs_security_grade),
    (SELECT COUNT(*) FROM docs_user),
    (SELECT COUNT(*) FROM docs_dept),
    (SELECT COUNT(*) FROM docs_sw),
    (SELECT COUNT(*) FROM docs_sw_file),
    (SELECT COUNT(*) FROM docs_sw_sub_file)
);
'@
    $validationResult = (& docker exec $Container psql `
        -U $DatabaseUser -d $Database -Atqc $validationQuery).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw 'Fresh database validation query failed.'
    }

    $values = $validationResult -split '\|'
    if ($values.Count -ne 18) {
        throw "Unexpected validation result: $validationResult"
    }
    if ($values[0] -notmatch '^17[0-9]{4}$') {
        throw "Unexpected PostgreSQL version: $($values[0])"
    }
    if ($values[2] -ne '26' -or
        $values[3] -ne '4' -or
        $values[4] -ne 'MENU_013,MENU_071,MENU_214,MENU_223' -or
        $values[5] -ne '25') {
        throw "Unexpected current menu catalog: $validationResult"
    }
    if ($values[6] -ne '0' -or
        $values[7] -ne '0' -or
        $values[8] -ne '0' -or
        $values[9] -ne '0') {
        throw "Portal-specific schema or menu state remains: $validationResult"
    }
    if ($values[10] -ne '0' -or $values[11] -ne '0') {
        throw "Menu role assignment integrity check failed: $validationResult"
    }
    if ($values[12] -ne '4') {
        throw "Unexpected security grade catalog: $validationResult"
    }
    if (-not $SkipSampleData -and
        (($values[13..17] -join ',') -ne '6,6,16,16,16')) {
        throw "Unexpected sample data counts: $validationResult"
    }

    Write-Host 'Fresh KT-1B database is ready.'
    Write-Host ('server|tables|activeMenus|visibleRoots|rootMenuCodes|' +
        'assignableMenus|portalSelectorColumns|outsideUrls|retiredRoutes|' +
        'broadPortalAcls|' +
        'missingAdminRoles|orphanRoles|grades|users|departments|documents|' +
        'mainFiles|subFiles')
    Write-Host $validationResult
} finally {
    & docker exec $Container rm -rf $containerWorkDirectory 2>$null
}
