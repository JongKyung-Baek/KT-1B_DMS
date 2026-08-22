[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot `
    'PdfConversionDeployment.Common.psm1') -Force

$script:Passed = 0
$script:ModelRoot = $null

function Assert-Rehearsal {
    param([bool]$Condition, [string]$Name)
    if (-not $Condition) { throw "FAILED: $Name" }
    $script:Passed++
    Write-Output "PASS: $Name"
}

function Write-Ascii {
    param([string]$Path, [string]$Value)
    Set-Content -LiteralPath $Path -Value $Value -Encoding ASCII -NoNewline
}

function Get-Sha256 {
    param([string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

$rehearsalId = [Guid]::NewGuid().ToString('N')
try {
    # This is the in-process rollback model. The separate exact-path Docker
    # rehearsal owns D:\KT1B-DMS, so use a unique temp root and avoid
    # drive-map races.
    $script:ModelRoot = Join-Path ([IO.Path]::GetTempPath()) `
        ('tdms-runtime-rollback-model-' + $rehearsalId)

    Assert-Rehearsal (-not (Test-Path -LiteralPath $script:ModelRoot)) `
        'rehearsal target is new and isolated'
    $runtime = Join-Path $script:ModelRoot 'runtime\pdf-conversion'
    $secrets = Join-Path $script:ModelRoot 'runtime\secrets'
    $app = Join-Path $script:ModelRoot 'app'
    $storage = Join-Path $script:ModelRoot 'storage'
    $database = Join-Path $script:ModelRoot 'database'
    $state = Join-Path $script:ModelRoot 'state'
    $backup = Join-Path $script:ModelRoot 'backup\runtime-files'
    $quarantine = Join-Path $script:ModelRoot 'backup\failed-runtime'
    New-Item -ItemType Directory -Path $runtime, $secrets, $app, $storage,
        $database, $state, $backup | Out-Null

    $liveOverlay = Join-Path $runtime 'compose.pdf-conversion.yaml'
    $liveSecret = Join-Path $secrets 'pdf-conversion.env'
    $liveWar = Join-Path $app 'SDMS-KT-1B.war'
    $gatewayState = Join-Path $state 'gateway.txt'
    $appState = Join-Path $state 'app.txt'
    $sidecarState = Join-Path $state 'sidecars.txt'
    $databaseSentinel = Join-Path $database 'protected-data.bin'
    $storageSentinel = Join-Path $storage 'document.bin'

    Write-Ascii $liveOverlay 'old-overlay'
    Write-Ascii $liveSecret 'old-secret'
    Write-Ascii $liveWar 'old-war'
    Write-Ascii $gatewayState 'gateway-original|running'
    Write-Ascii $appState 'app-original|running|old-image'
    Write-Ascii $sidecarState 'old-sidecars|stopped'
    Write-Ascii $databaseSentinel 'protected-db-before-apply'
    Write-Ascii $storageSentinel 'protected-storage-before-apply'
    Copy-Item -LiteralPath $liveOverlay `
        -Destination (Join-Path $backup 'compose.pdf-conversion.yaml')
    Copy-Item -LiteralPath $liveSecret `
        -Destination (Join-Path $backup 'pdf-conversion.env')
    Copy-Item -LiteralPath $liveWar `
        -Destination (Join-Path $backup 'SDMS-KT-1B.war')

    $databaseFingerprint = Get-Sha256 -Path $databaseSentinel
    $storageFingerprint = Get-Sha256 -Path $storageSentinel
    $trace = [Collections.Generic.List[string]]::new()
    $forcedFailureObserved = $false

    try {
        # Model a failure after the release app and runtime have replaced the
        # old version. The database and document storage remain untouched.
        Write-Ascii $liveOverlay 'release-overlay'
        Write-Ascii $liveSecret 'release-secret'
        Write-Ascii $liveWar 'release-war'
        Write-Ascii $appState 'app-replacement|running|new-image'
        Write-Ascii $sidecarState 'release-sidecars|running'
        throw 'FORCED_POST_APP_FAILURE'
    } catch {
        Assert-Rehearsal ($_.Exception.Message -ceq
            'FORCED_POST_APP_FAILURE') 'forced post-app failure is observed'
        $forcedFailureObserved = $true
        $operations = [ordered]@{
            AssertDatabaseInvariant = {
                param([string]$Phase, [string]$Position)
                Assert-DeploymentCondition -Condition (
                    (Get-Sha256 -Path $databaseSentinel) -ceq
                        $databaseFingerprint) `
                    -Message "Database changed $Position rollback phase $Phase."
            }
            StopGateway = {
                $trace.Add('StopGateway')
                Assert-DeploymentCondition -Condition (
                    (Get-Content -LiteralPath $gatewayState -Raw).StartsWith(
                        'gateway-original|')) `
                    -Message 'Gateway identity changed before rollback.'
                Write-Ascii $gatewayState 'gateway-original|stopped'
            }
            StopReleaseServices = {
                $trace.Add('StopReleaseServices')
                Write-Ascii $appState 'app-replacement|stopped|new-image'
                Write-Ascii $sidecarState 'release-sidecars|stopped'
            }
            RestoreRuntimeFiles = {
                $trace.Add('RestoreRuntimeFiles')
                foreach ($entry in @(
                        @{ Source = 'compose.pdf-conversion.yaml'; Target = $liveOverlay },
                        @{ Source = 'pdf-conversion.env'; Target = $liveSecret },
                        @{ Source = 'SDMS-KT-1B.war'; Target = $liveWar })) {
                    [void](Install-DeploymentFileSafely `
                        -Source (Join-Path $backup $entry.Source) `
                        -Target $entry.Target `
                        -QuarantineDirectory $quarantine)
                }
            }
            RestoreOriginalImage = {
                $trace.Add('RestoreOriginalImage')
                Write-Ascii (Join-Path $state 'image-tag.txt') 'old-image'
            }
            RecreateOriginalApp = {
                $trace.Add('RecreateOriginalApp')
                Write-Ascii $appState 'app-original|running|old-image'
            }
            RestoreOriginalSidecars = {
                $trace.Add('RestoreOriginalSidecars')
                Write-Ascii $sidecarState 'old-sidecars|absent'
            }
            VerifyOriginalApp = {
                $trace.Add('VerifyOriginalApp')
                Assert-DeploymentCondition -Condition (
                    (Get-Content -LiteralPath $appState -Raw) -ceq
                        'app-original|running|old-image') `
                    -Message 'Original app was not recreated.'
                Assert-DeploymentCondition -Condition (
                    (Get-Content -LiteralPath $liveWar -Raw) -ceq 'old-war') `
                    -Message 'Original WAR was not restored.'
            }
            VerifyPreservedDataFingerprints = {
                $trace.Add('VerifyPreservedDataFingerprints')
                Assert-DeploymentCondition -Condition (
                    (Get-Sha256 -Path $databaseSentinel) -ceq
                        $databaseFingerprint) `
                    -Message 'Runtime rollback changed protected DB data.'
                Assert-DeploymentCondition -Condition (
                    (Get-Sha256 -Path $storageSentinel) -ceq
                        $storageFingerprint) `
                    -Message 'Runtime rollback changed document storage.'
            }
            StartExistingGateway = {
                $trace.Add('StartExistingGateway')
                Assert-DeploymentCondition -Condition (
                    (Get-Content -LiteralPath $gatewayState -Raw) -ceq
                        'gateway-original|stopped') `
                    -Message 'Rollback would recreate the gateway.'
                Write-Ascii $gatewayState 'gateway-original|running'
            }
        }
        Invoke-PdfRuntimeOnlyRollback -Operations $operations `
            -Deadline (Get-Date).AddMinutes(1)
    }

    Assert-Rehearsal $forcedFailureObserved `
        'forced failure enters runtime-only rollback'
    Assert-Rehearsal (($trace -join ',') -ceq
        'StopGateway,StopReleaseServices,RestoreRuntimeFiles,' +
        'RestoreOriginalImage,RecreateOriginalApp,RestoreOriginalSidecars,' +
        'VerifyOriginalApp,' +
        'VerifyPreservedDataFingerprints,StartExistingGateway') `
        'runtime rollback operation order is fixed'
    Assert-Rehearsal ((Get-Content -LiteralPath $liveOverlay -Raw) -ceq
        'old-overlay') 'old runtime overlay is restored'
    Assert-Rehearsal ((Get-Content -LiteralPath $liveSecret -Raw) -ceq
        'old-secret') 'old runtime secret is restored'
    Assert-Rehearsal ((Get-Content -LiteralPath $appState -Raw) -ceq
        'app-original|running|old-image') 'old app identity is restored'
    Assert-Rehearsal ((Get-Content -LiteralPath $sidecarState -Raw) -ceq
        'old-sidecars|absent') 'pre-release sidecar state is restored'
    Assert-Rehearsal ((Get-Content -LiteralPath $gatewayState -Raw) -ceq
        'gateway-original|running') 'same gateway identity is restarted'
    Assert-Rehearsal ((Get-Sha256 -Path $databaseSentinel) -ceq
        $databaseFingerprint) 'database is unchanged by automatic rollback'
    Assert-Rehearsal ((Get-Sha256 -Path $storageSentinel) -ceq
        $storageFingerprint) 'storage is unchanged by automatic rollback'
    Assert-Rehearsal (-not (($trace -join ',') -match
        '(?i)restore.*(?:database|storage)|dropdb|pg_restore')) `
        'automatic rollback has no data restoration operation'

    Write-Output "RESULT=PASS;TESTS=$script:Passed;MODEL=$script:ModelRoot"
} finally {
    if ($script:ModelRoot -and
            (Test-Path -LiteralPath $script:ModelRoot -PathType Container)) {
        $modelFull = [IO.Path]::GetFullPath($script:ModelRoot).TrimEnd('\')
        $tempFull = [IO.Path]::GetFullPath(
            [IO.Path]::GetTempPath()).TrimEnd('\') + '\'
        $safe = $modelFull.StartsWith($tempFull,
                [StringComparison]::OrdinalIgnoreCase) -and
            [IO.Path]::GetFileName($modelFull) -match
                '^tdms-runtime-rollback-model-[0-9a-f]{32}$'
        if (-not $safe) {
            throw "Refusing rehearsal cleanup outside isolated model: $modelFull"
        }
        Remove-Item -LiteralPath $script:ModelRoot -Recurse -Force
    }
}
