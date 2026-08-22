[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('Preflight', 'Apply', 'Rollback')]
    [string]$Mode,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9.-]{7,79}$')]
    [string]$ReleaseId,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedRunnerSha256,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedCommonSha256,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedRequestSha256,

    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedStateSha256,

    [switch]$RestoreData,

    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedEvidenceSha256,

    [switch]$OutageSupervisor,

    [ValidateRange(1, 2147483647)]
    [int]$MainProcessId,

    [ValidatePattern('^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,7})?Z$')]
    [string]$MainProcessStartUtc,

    [ValidatePattern('^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,7})?Z$')]
    [string]$SupervisorTriggerUtc,

    [ValidatePattern('^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,7})?Z$')]
    [string]$SupervisorDeadlineUtc,

    [ValidatePattern('^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,7})?Z$')]
    [string]$RecoveryDeadlineUtc,

    [switch]$Rehearsal,

    [ValidatePattern('^D:\\KT1B-DMS-rehearsal-[0-9a-f]{32}$')]
    [string]$RehearsalRoot
)

# Single server-side owner for PDF conversion deployment protocol v2.
# Invoke this uploaded file with powershell.exe -File and a strict ReleaseId.
# Never send the script body or request-supplied paths through SSH -Command.

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

if (-not [Environment]::Is64BitProcess) {
    throw 'The PDF conversion release runner requires 64-bit Windows PowerShell 5.1.'
}

$script:Root = if ($Rehearsal) {
    if ($Mode -cne 'Rollback' -or
            [string]::IsNullOrWhiteSpace($RehearsalRoot) -or
            $env:TDMS_PDF_REHEARSAL_APPROVED -cne $ReleaseId) {
        throw 'The isolated rollback rehearsal contract is invalid.'
    }
    [IO.Path]::GetFullPath($RehearsalRoot).TrimEnd('\')
} else {
    if (-not [string]::IsNullOrWhiteSpace($RehearsalRoot)) {
        throw 'RehearsalRoot is forbidden for production execution.'
    }
    [IO.Path]::GetFullPath('D:\KT1B-DMS').TrimEnd('\')
}
$script:StageParent = Join-Path $script:Root 'staging'
$script:StageRoot = Join-Path $script:StageParent $ReleaseId
$runnerDirectory = [IO.Path]::GetFullPath($PSScriptRoot).TrimEnd('\')
if (-not $runnerDirectory.Equals(
        $script:StageRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'The pinned release runner must execute from its immutable stage.'
}
$script:RequestPath = Join-Path $script:StageRoot 'deployment-request.json'
$script:CommonPath = Join-Path $PSScriptRoot `
    'PdfConversionDeployment.Common.psm1'
$script:RunLogs = Join-Path $script:Root 'run-logs'
$script:Runtime = Join-Path $script:Root 'runtime'
$script:Secrets = Join-Path $script:Runtime 'secrets'
$script:SecretEnvironment = Join-Path $script:Secrets `
    'pdf-conversion.env'
$script:LiveOverlayDirectory = Join-Path $script:Runtime 'pdf-conversion'
$script:LiveOverlay = Join-Path $script:LiveOverlayDirectory `
    'compose.pdf-conversion.yaml'
$script:BaseCompose = Join-Path $script:Runtime 'compose.remote.yaml'
$script:BaseEnvironment = Join-Path $script:Root '.env'
$script:LiveWar = Join-Path $script:Root 'app\SDMS-KT-1B.war'
$script:Checksums = Join-Path $script:Root 'checksums.sha256'
$script:Dockerfile = Join-Path $script:Root 'Dockerfile'
$script:Storage = Join-Path $script:Root 'storage'
$script:StatePath = Join-Path $script:RunLogs `
    "pdf-conversion-state-$ReleaseId.json"
$script:ResultPath = Join-Path $script:RunLogs `
    "pdf-conversion-result-$ReleaseId-$($Mode.ToLowerInvariant()).json"
$script:EvidencePath = Join-Path $script:RunLogs `
    "pdf-conversion-fingerprint-evidence-$ReleaseId.json"
$script:QuiesceEvidencePath = Join-Path $script:RunLogs `
    "pdf-conversion-quiesce-evidence-$ReleaseId.json"
$script:OutageCompletionPath = Join-Path $script:RunLogs `
    "pdf-conversion-outage-complete-$ReleaseId.json"
$script:SupervisorReadyPath = Join-Path $script:RunLogs `
    "pdf-conversion-outage-supervisor-ready-$ReleaseId.json"
$script:LockPath = Join-Path $script:RunLogs `
    'tdms-pdf-conversion-deployment.lock'
$script:DbContainer = 'kt1b-dms-db-1'
$script:AppContainer = 'kt1b-dms-app-1'
$script:GatewayContainer = 'kt1b-dms-gateway-1'
$script:FileApiContainer = 'kt1b-dms-file-api-1'
$script:ConverterContainer = 'kt1b-dms-pdf-converter-1'
$script:CurlExecutable = Join-Path ([Environment]::GetFolderPath(
        [Environment+SpecialFolder]::System)) 'curl.exe'
$script:OutageStarted = $false
$script:OutageStartedAt = $null
$script:OutageDeadline = $null
$script:ApplyDeadline = $null
$script:RollbackStartedAt = $null
$script:GatewayHealthyAt = $null
$script:RuntimeMutationStarted = $false
$script:Lock = $null
$script:State = $null
$script:Request = $null
$script:Artifacts = @{}
$script:ArtifactLocks = [Collections.Generic.List[IO.FileStream]]::new()
$script:LockedReleaseFiles = @{}
$script:ReleaseAncestryInvariant = $null

function Get-BootstrapLockedSha256 {
    param([Parameter(Mandatory = $true)][IO.FileStream]$Stream)
    $Stream.Position = 0
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { $hash = $algorithm.ComputeHash($Stream) } finally {
        $algorithm.Dispose()
        $Stream.Position = 0
    }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Assert-BootstrapReleaseAcl {
    param(
        [string]$Path,
        [switch]$RequireProtected,
        [switch]$PassThru
    )
    $acl = Get-Acl -LiteralPath $Path
    if ($RequireProtected -and -not $acl.AreAccessRulesProtected) {
        throw 'Immutable release staging ACL must disable inheritance.'
    }
    $trustedWriters = @('S-1-5-18', 'S-1-5-32-544')
    $owner = $acl.GetOwner(
        [Security.Principal.SecurityIdentifier]).Value
    if ($trustedWriters -cnotcontains $owner) {
        throw "Immutable release owner is not trusted: $Path"
    }
    $writeMask = [Security.AccessControl.FileSystemRights]::Write -bor
        [Security.AccessControl.FileSystemRights]::Modify -bor
        [Security.AccessControl.FileSystemRights]::FullControl -bor
        [Security.AccessControl.FileSystemRights]::Delete -bor
        [Security.AccessControl.FileSystemRights]::DeleteSubdirectoriesAndFiles -bor
        [Security.AccessControl.FileSystemRights]::ChangePermissions -bor
        [Security.AccessControl.FileSystemRights]::TakeOwnership
    foreach ($rule in @($acl.GetAccessRules($true, $true,
                [Security.Principal.SecurityIdentifier]))) {
        if ($rule.AccessControlType -eq
                [Security.AccessControl.AccessControlType]::Allow -and
                (([int64]$rule.FileSystemRights -band
                    [int64]$writeMask) -ne 0) -and
                $trustedWriters -cnotcontains $rule.IdentityReference.Value) {
            throw "Immutable release ACL grants untrusted write access: $Path"
        }
    }
    if ($PassThru) {
        $sections = [Security.AccessControl.AccessControlSections]::Owner -bor
            [Security.AccessControl.AccessControlSections]::Group -bor
            [Security.AccessControl.AccessControlSections]::Access
        return $acl.GetSecurityDescriptorSddlForm($sections)
    }
}

function Assert-BootstrapReleaseAncestry {
    $expectedStageParent = [IO.Path]::GetFullPath(
        (Join-Path $script:Root 'staging')).TrimEnd('\')
    $expectedStageRoot = [IO.Path]::GetFullPath(
        (Join-Path $expectedStageParent $ReleaseId)).TrimEnd('\')
    if (-not $script:StageParent.Equals($expectedStageParent,
            [StringComparison]::OrdinalIgnoreCase) -or
            -not $script:StageRoot.Equals($expectedStageRoot,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Immutable release ancestry differs from the fixed root layout.'
    }

    $observed = [ordered]@{}
    foreach ($entry in @(
            @{ Name = 'root'; Path = $script:Root },
            @{ Name = 'staging'; Path = $script:StageParent },
            @{ Name = 'release'; Path = $script:StageRoot })) {
        $path = [IO.Path]::GetFullPath([string]$entry.Path).TrimEnd('\')
        if (-not (Test-Path -LiteralPath $path -PathType Container)) {
            throw "Immutable release directory is missing: $path"
        }
        $item = Get-Item -LiteralPath $path -Force
        $itemPath = [IO.Path]::GetFullPath($item.FullName).TrimEnd('\')
        if (-not $itemPath.Equals($path,
                [StringComparison]::OrdinalIgnoreCase)) {
            throw "Immutable release directory canonical path changed: $path"
        }
        if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw "Immutable release directory cannot be a reparse point: $path"
        }
        $sddl = Assert-BootstrapReleaseAcl -Path $path `
            -RequireProtected -PassThru
        $observed[[string]$entry.Name] = '{0}|{1}|{2}' -f
            $itemPath.ToUpperInvariant(),
            $item.CreationTimeUtc.Ticks,
            $sddl
    }

    if ($null -eq $script:ReleaseAncestryInvariant) {
        $script:ReleaseAncestryInvariant = $observed
        return
    }
    foreach ($name in @('root', 'staging', 'release')) {
        if (-not $script:ReleaseAncestryInvariant.Contains($name) -or
                [string]$script:ReleaseAncestryInvariant[$name] -cne
                [string]$observed[$name]) {
            throw "Immutable release ancestry changed after bootstrap: $name"
        }
    }
}

function Assert-BootstrapReleasePath {
    param([string]$Path, [string]$Name)
    $full = [IO.Path]::GetFullPath($Path)
    $parent = [IO.Path]::GetFullPath(
        (Split-Path -Parent $full)).TrimEnd('\')
    if (-not $parent.Equals($script:StageRoot,
            [StringComparison]::OrdinalIgnoreCase)) {
        throw "Deployment $Name is not a direct stage child."
    }
    if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
        throw "Deployment $Name artifact is missing."
    }
    $item = Get-Item -LiteralPath $full -Force
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "Deployment $Name cannot be a reparse point."
    }
    Assert-BootstrapReleaseAcl -Path $full
    return $full
}

function Lock-BootstrapPinnedReleaseFile {
    param([string]$Name, [string]$Path, [string]$ExpectedSha256)
    if ($script:LockedReleaseFiles.ContainsKey($Name)) {
        throw "Deployment file lock is duplicated: $Name"
    }
    $full = Assert-BootstrapReleasePath -Path $Path -Name $Name
    $stream = $null
    try {
        # FileShare.Read denies write, delete, rename, and replacement while
        # later import/parse operations consume the already pinned path.
        $stream = [IO.File]::Open($full, [IO.FileMode]::Open,
            [IO.FileAccess]::Read, [IO.FileShare]::Read)
        $actual = Get-BootstrapLockedSha256 -Stream $stream
        if ($actual -cne $ExpectedSha256.ToUpperInvariant()) {
            if (@('runner', 'common', 'request') -ccontains $Name) {
                throw "Deployment $Name artifact hash mismatch."
            }
            throw "Release artifact hash mismatch: $([IO.Path]::GetFileName($full))"
        }
        [void]$script:ArtifactLocks.Add($stream)
        $script:LockedReleaseFiles[$Name] = [pscustomobject]@{
            Name = $Name
            Path = $full
            ExpectedSha256 = $ExpectedSha256.ToUpperInvariant()
            Length = $stream.Length
            Stream = $stream
        }
        $stream = $null
    } finally {
        if ($stream) { $stream.Dispose() }
    }
}

function Read-BootstrapLockedUtf8Text {
    param([string]$Name)
    if (-not $script:LockedReleaseFiles.ContainsKey($Name)) {
        throw "Deployment file is not locked: $Name"
    }
    $record = $script:LockedReleaseFiles[$Name]
    if ($record.Length -gt [int]::MaxValue) {
        throw "Deployment text artifact is too large: $Name"
    }
    $stream = [IO.FileStream]$record.Stream
    $stream.Position = 0
    $bytes = New-Object byte[] ([int]$record.Length)
    $offset = 0
    while ($offset -lt $bytes.Length) {
        $read = $stream.Read($bytes, $offset, $bytes.Length - $offset)
        if ($read -le 0) { throw "Deployment text artifact was truncated: $Name" }
        $offset += $read
    }
    $stream.Position = 0
    return [Text.UTF8Encoding]::new($false, $true).GetString($bytes)
}

Assert-BootstrapReleaseAncestry
foreach ($artifact in @(
        @{ Path = $PSCommandPath; Hash = $ExpectedRunnerSha256; Name = 'runner' },
        @{ Path = $script:CommonPath; Hash = $ExpectedCommonSha256; Name = 'common' },
        @{ Path = $script:RequestPath; Hash = $ExpectedRequestSha256; Name = 'request' })) {
    Lock-BootstrapPinnedReleaseFile -Name $artifact.Name `
        -Path $artifact.Path -ExpectedSha256 $artifact.Hash
}
Assert-BootstrapReleaseAncestry
$lockedCommon = $script:LockedReleaseFiles['common']
Import-Module ([string]$lockedCommon.Path) -Force
foreach ($name in @('runner', 'common', 'request')) {
    $record = $script:LockedReleaseFiles[$name]
    if ((Get-BootstrapLockedSha256 -Stream $record.Stream) -cne
            $record.ExpectedSha256) {
        throw "Locked deployment $name changed at the bootstrap trust boundary."
    }
}
Assert-BootstrapReleaseAncestry

if ($RestoreData -and $Mode -cne 'Rollback') {
    throw 'RestoreData is permitted only in explicit Rollback mode.'
}

function Invoke-NativeChecked {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock]$Command,

        [Parameter(Mandatory = $true)]
        [string]$Operation,

        [string]$LogPath
    )
    $previous = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = @(& $Command 2>&1)
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previous
    }
    if ($LogPath) {
        [IO.File]::AppendAllText($LogPath,
            ("`r`n===== {0} =====`r`n{1}`r`n" -f $Operation,
                (($output | ForEach-Object { [string]$_ }) -join "`r`n")),
            [Text.UTF8Encoding]::new($false))
    }
    if ($exitCode -ne 0) {
        throw "$Operation failed with exit code $exitCode."
    }
    return $output
}

function Test-ContainerExists {
    param([string]$Name)
    $names = @(Invoke-NativeChecked -Operation "Find container $Name" `
        -Command {
            & docker ps --all --filter "name=^/$Name$" `
                --format '{{.Names}}'
        })
    return Test-ContainerNameInDockerList -Names $names -ExpectedName $Name
}

function Get-ContainerState {
    param([string]$Name)
    Assert-DeploymentCondition -Condition (Test-ContainerExists -Name $Name) `
        -Message "Required container is missing: $Name"
    $result = @(Invoke-NativeChecked -Operation "Inspect container $Name" `
        -Command {
            & docker inspect --format `
                '{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' `
                $Name
        })
    Assert-DeploymentCondition -Condition ($result.Count -eq 1) `
        -Message "Container state is invalid: $Name"
    return ([string]$result[0]).Trim()
}

function Get-ContainerId {
    param([string]$Name)
    $result = @(Invoke-NativeChecked -Operation "Inspect container id $Name" `
        -Command { & docker inspect --format '{{.Id}}' $Name })
    Assert-DeploymentCondition -Condition ($result.Count -eq 1 -and
        ([string]$result[0]).Trim() -match '^[0-9a-f]{64}$') `
        -Message "Container id is invalid: $Name"
    return ([string]$result[0]).Trim()
}

function Get-ImageId {
    param([string]$Image)
    $result = @(Invoke-NativeChecked -Operation "Inspect image $Image" `
        -Command { & docker image inspect --format '{{.Id}}' $Image })
    Assert-DeploymentCondition -Condition ($result.Count -eq 1 -and
        ([string]$result[0]).Trim() -match '^sha256:[0-9a-f]{64}$') `
        -Message "Image id is invalid: $Image"
    return ([string]$result[0]).Trim()
}

function Stop-ContainerIfPresent {
    param([string]$Name, [int]$TimeoutSeconds = 15)
    if (Test-ContainerExists -Name $Name) {
        $state = Get-ContainerState -Name $Name
        if ($state -match '^running\|') {
            [void](Invoke-NativeChecked -Operation "Stop container $Name" `
                -Command { & docker stop --time $TimeoutSeconds $Name })
        }
    }
}

function Get-OptionalSidecarRollbackRecord {
    param(
        [string]$ContainerName,
        [string]$ServiceName,
        [string]$RollbackTag
    )
    if (-not (Test-ContainerExists -Name $ContainerName)) {
        return [ordered]@{
            existed = $false
            containerName = $ContainerName
            serviceName = $ServiceName
            containerId = $null
            imageId = $null
            rollbackImageTag = $RollbackTag
        }
    }
    Assert-DeploymentCondition -Condition (
        (Get-ContainerState -Name $ContainerName) -ceq 'running|healthy') `
        -Message "Existing sidecar is not healthy: $ContainerName"
    $imageId = (@(Invoke-NativeChecked `
        -Operation "Inspect original sidecar image $ContainerName" -Command {
            & docker inspect --format '{{.Image}}' $ContainerName
        }))[0].ToString().Trim()
    Assert-DeploymentCondition -Condition (
        $imageId -match '^sha256:[0-9a-f]{64}$') `
        -Message "Existing sidecar image is invalid: $ContainerName"
    [void](Invoke-NativeChecked `
        -Operation "Create sidecar rollback tag $ContainerName" `
        -Command { & docker image tag $imageId $RollbackTag })
    return [ordered]@{
        existed = $true
        containerName = $ContainerName
        serviceName = $ServiceName
        containerId = Get-ContainerId -Name $ContainerName
        imageId = $imageId
        rollbackImageTag = $RollbackTag
    }
}

function Wait-Healthy {
    param([string]$Name, [DateTime]$Deadline)
    $last = 'unavailable'
    do {
        try {
            $last = Get-ContainerState -Name $Name
            if ($last -ceq 'running|healthy') { return }
        } catch {
            $last = 'unavailable'
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $Deadline)
    throw "Container $Name did not become healthy; last=$last"
}

function Assert-OutageDeadline {
    param([string]$Operation)
    if ($script:OutageStarted -and (Get-Date) -ge $script:OutageDeadline) {
        throw "The 180-second outage budget expired during $Operation."
    }
}

function Assert-BeforeDeadline {
    param([DateTime]$Deadline, [string]$Operation)
    if ((Get-Date) -ge $Deadline) {
        throw "The deployment deadline expired during $Operation."
    }
}

function Assert-DatabaseContainerInvariant {
    param([object]$State, [string]$Operation)
    Assert-BeforeDeadline -Deadline $script:OutageDeadline `
        -Operation "database invariant $Operation"
    Assert-DeploymentCondition -Condition (
        (Get-ContainerId -Name $script:DbContainer) -ceq
            [string]$State.databaseContainerId) `
        -Message "Database container identity changed $Operation."
    Assert-DeploymentCondition -Condition (
        (Get-ContainerState -Name $script:DbContainer) -ceq
            'running|healthy') `
        -Message "Database container is not healthy $Operation."
}

function Assert-ContainerFullyStopped {
    param([string]$Name)
    Assert-DeploymentCondition -Condition (
        (Get-ContainerState -Name $Name) -match '^exited\|') `
        -Message "Container did not fully stop: $Name"
}

function Write-OutageCompletion {
    param([string]$Outcome)
    if (Test-Path -LiteralPath $script:OutageCompletionPath -PathType Leaf) {
        return
    }
    $value = [ordered]@{
        protocolVersion = 1
        releaseId = $ReleaseId
        outcome = $Outcome
        outageStartedAt = if ($script:OutageStartedAt) {
            ([DateTime]$script:OutageStartedAt).ToUniversalTime().ToString('o')
        } else { $null }
        applyDeadline = if ($script:ApplyDeadline) {
            ([DateTime]$script:ApplyDeadline).ToUniversalTime().ToString('o')
        } else { $null }
        rollbackStartedAt = if ($script:RollbackStartedAt) {
            ([DateTime]$script:RollbackStartedAt).ToUniversalTime().ToString('o')
        } else { $null }
        gatewayHealthyAt = if ($script:GatewayHealthyAt) {
            ([DateTime]$script:GatewayHealthyAt).ToUniversalTime().ToString('o')
        } else { $null }
        completedAt = (Get-Date).ToUniversalTime().ToString('o')
    }
    Write-DeploymentJsonAtomically -Value $value `
        -Path $script:OutageCompletionPath
}

function Stop-ProcessTreePreservingCurrent {
    param([int]$RootProcessId)
    if ($RootProcessId -eq $PID) {
        throw 'The supervisor cannot terminate itself.'
    }
    $processes = @(Get-CimInstance Win32_Process |
        Select-Object ProcessId, ParentProcessId)
    $descendants = [Collections.Generic.HashSet[int]]::new()
    [void]$descendants.Add($RootProcessId)
    do {
        $added = $false
        foreach ($process in $processes) {
            if ($descendants.Contains([int]$process.ParentProcessId) -and
                    [int]$process.ProcessId -ne $PID -and
                    -not $descendants.Contains([int]$process.ProcessId)) {
                [void]$descendants.Add([int]$process.ProcessId)
                $added = $true
            }
        }
    } while ($added)
    foreach ($processId in @($descendants | Sort-Object -Descending)) {
        if ($processId -eq $PID) { continue }
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
}

function Start-OutageSupervisor {
    param([DateTime]$Trigger, [DateTime]$Deadline, [string]$StateSha256)
    foreach ($path in @($script:SupervisorReadyPath,
            $script:OutageCompletionPath)) {
        Assert-DeploymentCondition -Condition (-not
            (Test-Path -LiteralPath $path)) `
            -Message "Outage coordination file already exists: $path"
    }
    $arguments = @(
        '-NoLogo', '-NoProfile', '-NonInteractive',
        '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath,
        '-Mode', 'Rollback', '-ReleaseId', $ReleaseId,
        '-ExpectedRunnerSha256', $ExpectedRunnerSha256,
        '-ExpectedCommonSha256', $ExpectedCommonSha256,
        '-ExpectedRequestSha256', $ExpectedRequestSha256,
        '-ExpectedStateSha256', $StateSha256,
        '-OutageSupervisor', '-MainProcessId', [string]$PID,
        '-MainProcessStartUtc',
            ((Get-Process -Id $PID).StartTime.ToUniversalTime().ToString('o')),
        '-SupervisorTriggerUtc', $Trigger.ToUniversalTime().ToString('o'),
        '-SupervisorDeadlineUtc', $Deadline.ToUniversalTime().ToString('o'))
    $process = Start-Process -FilePath (Join-Path $PSHOME 'powershell.exe') `
        -ArgumentList $arguments -WindowStyle Hidden -PassThru
    $readyDeadline = (Get-Date).AddSeconds(5)
    do {
        if ($process.HasExited) {
            throw 'The outage supervisor exited before becoming ready.'
        }
        if (Test-Path -LiteralPath $script:SupervisorReadyPath -PathType Leaf) {
            return $process
        }
        Start-Sleep -Milliseconds 100
    } while ((Get-Date) -lt $readyDeadline)
    Stop-ProcessTreePreservingCurrent -RootProcessId $process.Id
    throw 'The outage supervisor did not become ready within five seconds.'
}

function Invoke-OutageSupervisorMode {
    Assert-DeploymentCondition -Condition (
        $Mode -ceq 'Rollback' -and
        -not $Rehearsal -and
        $MainProcessId -gt 0 -and
        -not [string]::IsNullOrWhiteSpace($MainProcessStartUtc) -and
        -not [string]::IsNullOrWhiteSpace($ExpectedStateSha256) -and
        -not [string]::IsNullOrWhiteSpace($SupervisorTriggerUtc) -and
        -not [string]::IsNullOrWhiteSpace($SupervisorDeadlineUtc)) `
        -Message 'The outage supervisor arguments are incomplete.'
    $trigger = [DateTime]::Parse($SupervisorTriggerUtc,
        [Globalization.CultureInfo]::InvariantCulture,
        [Globalization.DateTimeStyles]::AssumeUniversal).ToUniversalTime()
    $deadline = [DateTime]::Parse($SupervisorDeadlineUtc,
        [Globalization.CultureInfo]::InvariantCulture,
        [Globalization.DateTimeStyles]::AssumeUniversal).ToUniversalTime()
    Assert-DeploymentCondition -Condition ($trigger -lt $deadline) `
        -Message 'The outage supervisor deadline ordering is invalid.'
    $expectedMainStart = [DateTime]::Parse($MainProcessStartUtc,
        [Globalization.CultureInfo]::InvariantCulture,
        [Globalization.DateTimeStyles]::AssumeUniversal).ToUniversalTime()
    $main = Get-Process -Id $MainProcessId -ErrorAction SilentlyContinue
    Assert-DeploymentCondition -Condition ($null -ne $main -and
        [Math]::Abs(($main.StartTime.ToUniversalTime() -
            $expectedMainStart).TotalSeconds) -lt 0.01) `
        -Message 'The supervised Apply process identity is invalid.'
    Write-DeploymentJsonAtomically -Value ([ordered]@{
            protocolVersion = 1
            releaseId = $ReleaseId
            supervisorProcessId = $PID
            mainProcessId = $MainProcessId
            triggerAt = $trigger.ToString('o')
            deadlineAt = $deadline.ToString('o')
        }) -Path $script:SupervisorReadyPath
    while ((Get-Date).ToUniversalTime() -lt $trigger) {
        if (Test-Path -LiteralPath $script:OutageCompletionPath -PathType Leaf) {
            return
        }
        Start-Sleep -Milliseconds 250
    }
    if (Test-Path -LiteralPath $script:OutageCompletionPath -PathType Leaf) {
        return
    }
    Stop-ProcessTreePreservingCurrent -RootProcessId $MainProcessId
    Start-Sleep -Milliseconds 500
    Assert-DeploymentCondition -Condition (
        -not (Get-Process -Id $MainProcessId -ErrorAction SilentlyContinue)) `
        -Message 'Timed-out Apply process could not be terminated.'
    $outputPath = Join-Path $script:RunLogs `
        "pdf-conversion-supervised-rollback-$ReleaseId.stdout.log"
    $errorPath = Join-Path $script:RunLogs `
        "pdf-conversion-supervised-rollback-$ReleaseId.stderr.log"
    $arguments = @(
        '-NoLogo', '-NoProfile', '-NonInteractive',
        '-ExecutionPolicy', 'Bypass', '-File', $PSCommandPath,
        '-Mode', 'Rollback', '-ReleaseId', $ReleaseId,
        '-ExpectedRunnerSha256', $ExpectedRunnerSha256,
        '-ExpectedCommonSha256', $ExpectedCommonSha256,
        '-ExpectedRequestSha256', $ExpectedRequestSha256,
        '-ExpectedStateSha256', $ExpectedStateSha256,
        '-RecoveryDeadlineUtc', $deadline.ToString('o'))
    $rollback = Start-Process -FilePath (Join-Path $PSHOME 'powershell.exe') `
        -ArgumentList $arguments -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput $outputPath `
        -RedirectStandardError $errorPath
    $remaining = [int][Math]::Max(0, [Math]::Min([int]::MaxValue,
            ($deadline - (Get-Date).ToUniversalTime()).TotalMilliseconds))
    if (-not $rollback.WaitForExit($remaining)) {
        Stop-ProcessTreePreservingCurrent -RootProcessId $rollback.Id
        throw 'MANUAL_RECOVERY_REQUIRED: supervised rollback exceeded the global outage deadline.'
    }
    Assert-DeploymentCondition -Condition ($rollback.ExitCode -eq 0) `
        -Message 'MANUAL_RECOVERY_REQUIRED: supervised rollback failed.'
}

function Get-Artifact {
    param([string]$Name)
    $property = $script:Request.artifacts.PSObject.Properties[$Name]
    Assert-DeploymentCondition -Condition ($null -ne $property) `
        -Message "Release artifact declaration is missing: $Name"
    $record = $property.Value
    $fileName = [string]$record.file
    Assert-DeploymentCondition -Condition (
        $fileName -match '^[A-Za-z0-9][A-Za-z0-9._-]*$') `
        -Message "Unsafe release artifact file name: $fileName"
    $path = Join-Path $script:StageRoot $fileName
    [void](Assert-DeploymentChildPath -Candidate $path `
        -Parent $script:StageRoot -Description "Artifact $Name")
    Lock-BootstrapPinnedReleaseFile -Name $Name -Path $path `
        -ExpectedSha256 ([string]$record.sha256)
    $script:Artifacts[$Name] = $path
    return $path
}

function Assert-OverlayContract {
    param([string]$Path)
    $text = [IO.File]::ReadAllText($Path)
    foreach ($service in @('app', 'file-api', 'pdf-converter')) {
        Assert-DeploymentCondition -Condition ($text -match
            ("(?m)^  " + [regex]::Escape($service) + ':\s*$')) `
            -Message "Overlay service is missing: $service"
    }
    Assert-DeploymentCondition -Condition ($text -match
        '(?m)^    image:\s*\$\{KT1B_APP_IMAGE:\?') `
        -Message 'App image must come from the immutable release env.'
    foreach ($service in @('file-api', 'pdf-converter')) {
        $match = [regex]::Match($text,
            '(?ms)^  ' + [regex]::Escape($service) +
            ':\s*\r?\n(?<body>.*?)(?=^  [A-Za-z0-9_-]+:\s*\r?\n|^volumes:\s*$|\z)')
        Assert-DeploymentCondition -Condition $match.Success `
            -Message "Cannot isolate overlay service $service."
        Assert-DeploymentCondition -Condition ($match.Groups['body'].Value `
            -match '(?m)^    network_mode:\s*["'']?service:app["'']?\s*$') `
            -Message "$service must share the app network namespace."
        Assert-DeploymentCondition -Condition ($match.Groups['body'].Value `
            -notmatch '(?m)^    ports:\s*$') `
            -Message "$service publishes a host port."
    }
    foreach ($port in @('9001:9001', '18080:18080')) {
        Assert-DeploymentCondition -Condition (-not $text.Contains($port)) `
            -Message "Overlay publishes private port mapping $port."
    }
    Assert-DeploymentCondition -Condition ($text -notmatch
        '(?im)(?:^|[\s"''])[A-Z]:[\\/]') `
        -Message 'Overlay cannot reference an absolute Windows host path.'
    foreach ($forbidden in @(
            'internal_only_cleanup', 'reset_demo_data', 'sample_demo_data')) {
        Assert-DeploymentCondition -Condition (-not $text.Contains($forbidden)) `
            -Message "Overlay contains forbidden reference: $forbidden"
    }
}

function Lock-ImmutableReleaseFiles {
    $expectedCount = if ($script:Artifacts.ContainsKey('publicProbeCa')) {
        10
    } else { 9 }
    Assert-DeploymentCondition -Condition (
        $script:LockedReleaseFiles.Count -eq $expectedCount -and
        $script:ArtifactLocks.Count -eq $expectedCount) `
        -Message 'The immutable release lock set is incomplete.'
    Assert-BootstrapReleaseAncestry
    $topLevelEntries = @(Get-ChildItem -LiteralPath $script:StageRoot -Force)
    Assert-DeploymentCondition -Condition (
        @($topLevelEntries | Where-Object { $_.PSIsContainer }).Count -eq 0) `
        -Message 'Release staging cannot contain subdirectories.'
    $stageEntries = @(Get-ChildItem -LiteralPath $script:StageRoot `
        -Recurse -Force)
    Assert-DeploymentCondition -Condition (
        @($stageEntries | Where-Object { $_.PSIsContainer }).Count -eq 0) `
        -Message 'Release staging cannot contain subdirectories.'
    Assert-DeploymentCondition -Condition (
        @($stageEntries | Where-Object {
                $_.Attributes -band [IO.FileAttributes]::ReparsePoint
            }).Count -eq 0) `
        -Message 'Release staging cannot contain reparse points.'
    $stageFiles = @($stageEntries | Where-Object { -not $_.PSIsContainer })
    $inventoryPaths = @($stageFiles | ForEach-Object {
            [IO.Path]::GetFullPath($_.FullName).ToUpperInvariant()
        } | Sort-Object -Unique)
    $lockedPaths = @($script:LockedReleaseFiles.Values | ForEach-Object {
            [IO.Path]::GetFullPath($_.Path).ToUpperInvariant()
        } | Sort-Object -Unique)
    Assert-DeploymentCondition -Condition (
        $stageFiles.Count -eq $expectedCount -and
        $inventoryPaths.Count -eq $expectedCount -and
        $lockedPaths.Count -eq $expectedCount -and
        (($inventoryPaths -join "`n") -ceq ($lockedPaths -join "`n"))) `
        -Message 'Release staging contains undeclared or missing files.'
    foreach ($record in $script:LockedReleaseFiles.Values) {
        Assert-BootstrapReleasePath -Path $record.Path -Name $record.Name |
            Out-Null
        $item = Get-Item -LiteralPath $record.Path -Force
        Assert-DeploymentCondition -Condition (
            $item.Length -eq $record.Length -and
            (Get-BootstrapLockedSha256 -Stream $record.Stream) -ceq
                $record.ExpectedSha256) `
            -Message "Immutable release file changed: $($record.Name)"
    }
}

function Read-ReleaseContract {
    Assert-BootstrapReleaseAncestry
    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $script:StageRoot -PathType Container) `
        -Message 'Immutable release staging directory is missing.'
    Assert-DeploymentCondition -Condition (-not
        $script:StageRoot.EndsWith('.partial',
            [StringComparison]::OrdinalIgnoreCase)) `
        -Message 'A .partial upload cannot be executed.'
    $stageItem = Get-Item -LiteralPath $script:StageRoot -Force
    Assert-DeploymentCondition -Condition (-not
        ($stageItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) `
        -Message 'Release staging root cannot be a reparse point.'
    $lockedRequest = $script:LockedReleaseFiles['request']
    Assert-DeploymentCondition -Condition (
        (Get-BootstrapLockedSha256 -Stream $lockedRequest.Stream) -ceq
            $lockedRequest.ExpectedSha256) `
        -Message 'Locked deployment request changed before parsing.'
    $requestText = Read-BootstrapLockedUtf8Text -Name 'request'
    $script:Request = $requestText | ConvertFrom-Json
    Assert-DeploymentCondition -Condition (
        (Get-BootstrapLockedSha256 -Stream $lockedRequest.Stream) -ceq
            $lockedRequest.ExpectedSha256) `
        -Message 'Locked deployment request changed during parsing.'
    Assert-DeploymentCondition -Condition (
        [int]$script:Request.protocolVersion -eq 2) `
        -Message 'Unsupported PDF conversion release protocol.'
    Assert-DeploymentCondition -Condition (
        [string]$script:Request.releaseId -ceq $ReleaseId) `
        -Message 'Release request id differs from the command release id.'
    $example = $script:Request.PSObject.Properties['exampleOnly']
    Assert-DeploymentCondition -Condition (-not
        ($null -ne $example -and [bool]$example.Value)) `
        -Message 'The example request cannot be executed.'
    foreach ($file in @(Get-ChildItem -LiteralPath $script:StageRoot `
            -Recurse -Force)) {
        Assert-DeploymentCondition -Condition (-not
            ($file.Attributes -band [IO.FileAttributes]::ReparsePoint)) `
            -Message 'Release staging cannot contain reparse points.'
    }

    $requiredArtifactNames = @('war', 'overlay', 'releaseEnv', 'viewerDdl',
        'pdfDdl', 'fileApiImageArchive')
    $allowedArtifactNames = @($requiredArtifactNames + 'publicProbeCa')
    $artifactProperty = $script:Request.PSObject.Properties['artifacts']
    Assert-DeploymentCondition -Condition ($null -ne $artifactProperty -and
        $null -ne $artifactProperty.Value) `
        -Message 'Release artifact declarations are missing.'
    $declaredArtifactNames = @(
        $artifactProperty.Value.PSObject.Properties | ForEach-Object {
            $_.Name
        })
    Assert-DeploymentCondition -Condition (
        @($declaredArtifactNames | Where-Object {
                $allowedArtifactNames -cnotcontains $_
            }).Count -eq 0 -and
        @($requiredArtifactNames | Where-Object {
                $declaredArtifactNames -cnotcontains $_
            }).Count -eq 0 -and
        $declaredArtifactNames.Count -in 6, 7) `
        -Message 'Release artifacts violate the exact allowlist.'
    foreach ($name in $declaredArtifactNames) {
        $record = $artifactProperty.Value.PSObject.Properties[$name].Value
        $fields = @($record.PSObject.Properties | ForEach-Object { $_.Name })
        Assert-DeploymentCondition -Condition (
            $fields.Count -eq 2 -and
            $fields -ccontains 'file' -and
            $fields -ccontains 'sha256' -and
            [string]$record.sha256 -match '^[0-9A-Fa-f]{64}$') `
            -Message "Release artifact declaration is invalid: $name"
    }
    foreach ($name in $requiredArtifactNames) {
        [void](Get-Artifact -Name $name)
    }
    if ($declaredArtifactNames -ccontains 'publicProbeCa') {
        $publicProbeCa = Get-Artifact -Name 'publicProbeCa'
        Assert-PublicProbeCaCertificate -Path $publicProbeCa
    }
    Assert-OverlayContract -Path $script:Artifacts['overlay']
    $releaseEnvironment = Read-StrictEnvironmentFile `
        -Path $script:Artifacts['releaseEnv'] -ExactKeys @(
            'KT1B_APP_IMAGE', 'KT1B_FILE_API_IMAGE',
            'KT1B_PDF_CONVERTER_IMAGE')
    foreach ($entry in @(
            @{ Key = 'KT1B_APP_IMAGE'; Value = [string]$script:Request.images.app },
            @{ Key = 'KT1B_FILE_API_IMAGE'; Value = [string]$script:Request.images.fileApi },
            @{ Key = 'KT1B_PDF_CONVERTER_IMAGE'; Value = [string]$script:Request.images.converter })) {
        Assert-DeploymentCondition -Condition (
            [string]$releaseEnvironment[$entry.Key] -ceq $entry.Value) `
            -Message "Release environment image mismatch: $($entry.Key)"
        Assert-DeploymentCondition -Condition (
            [string]$entry.Value -match '^[A-Za-z0-9][A-Za-z0-9._/:@-]+$') `
            -Message "Unsafe image reference: $($entry.Key)"
    }

    Assert-ProtectedDeploymentSecretFile -Path $script:SecretEnvironment
    $secretEnvironment = Read-StrictEnvironmentFile `
        -Path $script:SecretEnvironment -ExactKeys @(
            'KT1B_FILE_API_KEY',
            'TDMS_PDF_CONVERSION_CLIENT_ID',
            'TDMS_PDF_CONVERSION_SHARED_SECRET')
    Assert-DeploymentCondition -Condition (
        [string]$secretEnvironment['KT1B_FILE_API_KEY'] -match
            '^[A-Za-z0-9_-]{32,}$') -Message 'File API key is invalid.'
    Assert-DeploymentCondition -Condition (
        [string]$secretEnvironment['TDMS_PDF_CONVERSION_CLIENT_ID'] -match
            '^[A-Za-z0-9._-]{3,100}$') -Message 'Converter client id is invalid.'
    Assert-DeploymentCondition -Condition (
        [string]$secretEnvironment['TDMS_PDF_CONVERSION_SHARED_SECRET'] -match
            '^[A-Za-z0-9_-]{32,}$') -Message 'Converter shared secret is invalid.'

    foreach ($path in @($script:BaseCompose, $script:BaseEnvironment,
            $script:LiveWar, $script:Checksums, $script:Dockerfile)) {
        Assert-DeploymentCondition `
            -Condition (Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Live deployment file is missing: $path"
    }
    foreach ($path in @($script:RunLogs, $script:Runtime, $script:Secrets,
            $script:Storage)) {
        Assert-DeploymentCondition `
            -Condition (Test-Path -LiteralPath $path -PathType Container) `
            -Message "Live deployment directory is missing: $path"
    }
    Assert-DockerfileCopyAllowlist `
        -DockerfileText ([IO.File]::ReadAllText($script:Dockerfile))
    $ddlForbidden = '(?is)\b(?:' +
        'drop\s+(?:database|schema|table|view|materialized\s+view|type|' +
            'function|extension|role|owned)|' +
        'truncate|' +
        'alter\s+table\b.*?\bdrop\s+column' +
        ')\b'
    foreach ($ddl in @($script:Artifacts['viewerDdl'],
            $script:Artifacts['pdfDdl'])) {
        $text = [IO.File]::ReadAllText($ddl)
        Assert-DeploymentCondition -Condition ($text -notmatch $ddlForbidden) `
            -Message 'Release DDL contains a destructive statement.'
    }
    Lock-ImmutableReleaseFiles
}

function Invoke-Compose {
    param([string[]]$Arguments, [string]$Operation,
        [string]$Overlay = $script:Artifacts['overlay'],
        [string]$ReleaseEnvironment = $script:Artifacts['releaseEnv'])
    foreach ($forbidden in @('down', '--volumes', '-v', 'rm', 'pull')) {
        Assert-DeploymentCondition -Condition ($Arguments -notcontains $forbidden) `
            -Message "Forbidden compose argument: $forbidden"
    }
    return Invoke-NativeChecked -Operation $Operation -Command {
        & docker compose -p kt1b-dms `
            --env-file $script:BaseEnvironment `
            --env-file $ReleaseEnvironment `
            --env-file $script:SecretEnvironment `
            -f $script:BaseCompose -f $Overlay @Arguments
    }
}

function Invoke-BaseCompose {
    param([string[]]$Arguments, [string]$Operation)
    foreach ($forbidden in @('down', '--volumes', '-v', 'rm', 'pull')) {
        Assert-DeploymentCondition -Condition ($Arguments -notcontains $forbidden) `
            -Message "Forbidden compose argument: $forbidden"
    }
    return Invoke-NativeChecked -Operation $Operation -Command {
        & docker compose -p kt1b-dms `
            --env-file $script:BaseEnvironment `
            -f $script:BaseCompose @Arguments
    }
}

function Assert-LivePreflight {
    Assert-DeploymentCondition -Condition (
        Test-Path -LiteralPath $script:CurlExecutable -PathType Leaf) `
        -Message 'The fixed Windows curl executable is missing.'
    foreach ($container in @($script:DbContainer, $script:AppContainer,
            $script:GatewayContainer)) {
        Assert-DeploymentCondition -Condition (
            (Get-ContainerState -Name $container) -ceq 'running|healthy') `
            -Message "Live container is not healthy: $container"
    }
    [void](Invoke-NativeChecked -Operation 'Docker engine preflight' `
        -Command { & docker version --format '{{.Server.Version}}' })
    $postgresVersion = @(Invoke-NativeChecked `
        -Operation 'PostgreSQL 17 preflight' -Command {
            & docker exec $script:DbContainer psql --version
        })
    Assert-DeploymentCondition -Condition ($postgresVersion.Count -eq 1 -and
        ([string]$postgresVersion[0]).Trim() -match
            '^psql \(PostgreSQL\) 17(?:\.|\s|$)') `
        -Message 'The release requires the PostgreSQL 17 runtime.'
    [void](Invoke-Compose -Arguments @('config', '--quiet') `
        -Operation 'Validate release compose configuration')
    Assert-DeploymentCondition -Condition (
        (Get-ImageId -Image ([string]$script:Request.images.converter)) -ceq
        [string]$script:Request.images.converterId) `
        -Message 'Loaded converter image differs from the approved artifact.'
    $publicProbeCaPath = if (
        $script:Artifacts.ContainsKey('publicProbeCa')) {
        [string]$script:Artifacts['publicProbeCa']
    } else { $null }
    $curlArguments = @(Get-PublicProbeCurlArguments `
        -PublicProbeCaPath $publicProbeCaPath)
    Assert-DeploymentCondition -Condition (
        $curlArguments.Count -gt 4 -and
        $curlArguments[0] -ceq '--disable' -and
        $curlArguments -ccontains '--noproxy' -and
        $curlArguments[([Array]::IndexOf($curlArguments, '--noproxy') + 1)] `
            -ceq '*') `
        -Message 'Public HTTPS probe curl arguments are unsafe.'
    $https = @(Invoke-WithIsolatedDeploymentEnvironment -Keys @(
            'CURL_CA_BUNDLE', 'SSL_CERT_FILE', 'SSL_CERT_DIR', 'CURL_HOME',
            'HOME', 'HTTPS_PROXY', 'ALL_PROXY', 'HTTP_PROXY', 'NO_PROXY') `
        -Operation {
            Invoke-NativeChecked `
            -Operation 'Validate live HTTPS login' -Command {
                & $script:CurlExecutable @curlArguments
            }
        })
    Assert-DeploymentCondition -Condition ($https.Count -eq 1 -and
        ([string]$https[0]).Trim() -ceq '200') `
        -Message 'Live HTTPS login smoke failed.'
}

function Read-BaseEnvironment {
    $environment = Read-StrictEnvironmentFile -Path $script:BaseEnvironment
    foreach ($key in @('KT1B_DB_NAME', 'KT1B_DB_ADMIN_USER',
            'KT1B_DB_APP_USER')) {
        Assert-DeploymentCondition -Condition ($environment.Contains($key) -and
            [string]$environment[$key] -match
                '^[A-Za-z_][A-Za-z0-9_]*$') `
            -Message "Unsafe or missing DB setting: $key"
    }
    return $environment
}

function Get-DatabaseFingerprint {
    param([string]$Database, [string]$AdminUser)
    $tableOutput = @(Invoke-NativeChecked -Operation `
        "List protected tables in $Database" -Command {
            & docker exec $script:DbContainer psql -X -At `
                -U $AdminUser -d $Database -c `
                "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = 'public' ORDER BY tablename;"
        })
    $excludedTables = @(
        'docs_system_config',
        'docs_pdf_conversion',
        'docs_viewer_launch',
        'docs_viewer_event',
        'docs_viewer_callback_nonce',
        'docs_viewer_key')
    $ignoredColumns = @{
        docs_sw_file = @(
            'processing_status', 'processing_error', 'processed_at')
        docs_sw_sub_file = @(
            'processing_status', 'processing_error', 'processed_at')
        docs_history = @(
            'file_no', 'source_system_cd', 'source_event_id',
            'source_correlation_id')
    }
    $canonicalLines = [Collections.Generic.List[string]]::new()
    foreach ($rawTable in $tableOutput) {
        $table = ([string]$rawTable).Trim()
        Assert-DeploymentCondition -Condition (
            $table -match '^[a-z_][a-z0-9_]*$') `
            -Message "Unsafe protected table identifier: $table"
        if ($excludedTables -contains $table) { continue }
        $projection = 'to_jsonb(t)'
        if ($ignoredColumns.ContainsKey($table)) {
            $array = ($ignoredColumns[$table] | ForEach-Object {
                    "'$_'"
                }) -join ','
            $projection = "(to_jsonb(t) - ARRAY[$array])"
        }
        $sql = "SELECT ($projection)::text FROM public.`"$table`" AS t " +
            "ORDER BY ($projection)::text;"
        $rows = @(Invoke-NativeChecked -Operation `
            "Fingerprint protected table $table" -Command {
                # Stream SQL over stdin so Windows native argument parsing
                # cannot strip the quotes required by legacy mixed-case table
                # identifiers such as "CV_VIEW_MARKUP".
                $sql | & docker exec -i $script:DbContainer psql -X -At `
                    -v ON_ERROR_STOP=1 -U $AdminUser -d $Database
            })
        [void]$canonicalLines.Add("TABLE|$table|$($rows.Count)")
        foreach ($row in $rows) {
            [void]$canonicalLines.Add([string]$row)
        }
    }
    $canonical = $canonicalLines.ToArray() -join "`n"
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { $hash = $algorithm.ComputeHash($bytes) } finally { $algorithm.Dispose() }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Get-FullDatabaseFingerprint {
    param([string]$Database, [string]$AdminUser)
    $lines = @(Invoke-NativeChecked -Operation `
        "Fingerprint complete data in $Database" -Command {
            & docker exec $script:DbContainer pg_dump -U $AdminUser `
                -d $Database --data-only --no-owner --no-privileges
        })
    $canonical = @($lines | ForEach-Object { [string]$_ } |
        Where-Object {
            $_ -notmatch '^\\(?:un)?restrict\s+' -and
            $_ -notmatch '^-- Dumped (?:from database|by pg_dump) version '
        }) -join "`n"
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { $hash = $algorithm.ComputeHash($bytes) } finally { $algorithm.Dispose() }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Get-SchemaFingerprint {
    param([string]$Database, [string]$AdminUser)
    $lines = @(Invoke-NativeChecked -Operation `
        "Fingerprint schema in $Database" -Command {
            & docker exec $script:DbContainer pg_dump -U $AdminUser `
                -d $Database --schema-only --no-owner --no-privileges
        })
    $canonical = @($lines | ForEach-Object { [string]$_ } |
        Where-Object {
            $_ -notmatch '^\\(?:un)?restrict\s+' -and
            $_ -notmatch '^-- Dumped (?:from database|by pg_dump) version '
        }) -join "`n"
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { $hash = $algorithm.ComputeHash($bytes) } finally { $algorithm.Dispose() }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Get-ArchiveSchemaFingerprint {
    param([string]$ContainerPath)
    $lines = @(Invoke-NativeChecked -Operation `
        'Fingerprint schema stored in recovery archive' -Command {
            & docker exec $script:DbContainer pg_restore --schema-only `
                --no-owner --no-privileges --file=- $ContainerPath
        })
    $canonical = @($lines | ForEach-Object { [string]$_ } |
        Where-Object {
            $_ -notmatch '^\\(?:un)?restrict\s+' -and
            $_ -notmatch '^-- Dumped (?:from database|by pg_dump) version '
        }) -join "`n"
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($canonical)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { $hash = $algorithm.ComputeHash($bytes) } finally { $algorithm.Dispose() }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Get-ProtectedRowCounts {
    param([string]$Database, [string]$AdminUser)
    $sql = @'
SELECT
    (SELECT COUNT(*) FROM public.docs_sw)::text || '|' ||
    (SELECT COUNT(*) FROM public.docs_sw_file)::text || '|' ||
    (SELECT COUNT(*) FROM public.docs_sw_sub_file)::text || '|' ||
    (SELECT COUNT(*) FROM public.docs_history)::text;
'@
    $result = @(Invoke-NativeChecked -Operation `
        "Count protected rows in $Database" -Command {
            & docker exec $script:DbContainer psql -X -At `
                -U $AdminUser -d $Database -c $sql
        })
    Assert-DeploymentCondition -Condition ($result.Count -eq 1 -and
        ([string]$result[0]).Trim() -match '^\d+\|\d+\|\d+\|\d+$') `
        -Message "Protected row counts are invalid in $Database."
    return ([string]$result[0]).Trim()
}

function Get-DatabaseWalPosition {
    param([string]$Database, [string]$AdminUser)
    $result = @(Invoke-NativeChecked -Operation `
        "Read WAL position in $Database" -Command {
            & docker exec $script:DbContainer psql -X -At `
                -U $AdminUser -d $Database `
                -c 'SELECT pg_current_wal_lsn();'
        })
    Assert-DeploymentCondition -Condition ($result.Count -eq 1 -and
        ([string]$result[0]).Trim() -match '^[0-9A-F]+/[0-9A-F]+$') `
        -Message "Database WAL position is invalid in $Database."
    return ([string]$result[0]).Trim()
}

function Get-StorageMetadataFingerprint {
    $root = [IO.Path]::GetFullPath($script:Storage).TrimEnd('\')
    $prefix = $root + '\'
    $lines = foreach ($file in @(Get-ChildItem -LiteralPath $root `
            -Recurse -File -Force | Sort-Object FullName)) {
        $full = [IO.Path]::GetFullPath($file.FullName)
        Assert-DeploymentCondition -Condition $full.StartsWith(
            $prefix, [StringComparison]::OrdinalIgnoreCase) `
            -Message 'Storage metadata path escaped its root.'
        '{0}|{1}|{2}' -f
            $full.Substring($prefix.Length).Replace('\', '/'),
            [long]$file.Length,
            $file.LastWriteTimeUtc.Ticks
    }
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes($lines -join "`n")
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { $hash = $algorithm.ComputeHash($bytes) } finally { $algorithm.Dispose() }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Invoke-PsqlFile {
    param([string]$Database, [string]$AdminUser,
        [string]$ContainerPath, [string]$Operation)
    [void](Invoke-NativeChecked -Operation $Operation -Command {
        & docker exec $script:DbContainer psql -X -v ON_ERROR_STOP=1 `
            -U $AdminUser -d $Database -f $ContainerPath
    })
}

function Grant-AppRole {
    param([string]$Database, [string]$AdminUser, [string]$AppUser)
    $quotedDb = '"' + $Database.Replace('"', '""') + '"'
    $quotedApp = '"' + $AppUser.Replace('"', '""') + '"'
    $sql = @"
GRANT CONNECT ON DATABASE $quotedDb TO $quotedApp;
GRANT USAGE ON SCHEMA public TO $quotedApp;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO $quotedApp;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO $quotedApp;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO $quotedApp;
"@
    [void](Invoke-NativeChecked -Operation "Grant app role in $Database" `
        -Command {
            & docker exec $script:DbContainer psql -X -v ON_ERROR_STOP=1 `
                -U $AdminUser -d $Database -c $sql
        })
}

function Copy-VerifiedFile {
    param([string]$Source, [string]$Destination)
    $parent = Split-Path -Parent $Destination
    if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
        New-Item -ItemType Directory -Path $parent | Out-Null
    }
    Copy-Item -LiteralPath $Source -Destination $Destination
    Assert-DeploymentCondition -Condition (
        (Get-FileHash -LiteralPath $Source -Algorithm SHA256).Hash -ceq
        (Get-FileHash -LiteralPath $Destination -Algorithm SHA256).Hash) `
        -Message "Backup hash mismatch: $Destination"
}

function Protect-BackupDirectory {
    param([string]$Path)
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    $output = @(& icacls.exe $Path /inheritance:r `
        /grant:r "${identity}:(OI)(CI)F" 'SYSTEM:(OI)(CI)F' `
        'BUILTIN\Administrators:(OI)(CI)F' 2>&1)
    if ($LASTEXITCODE -ne 0) {
        throw ('Backup ACL update failed: ' +
            (($output | ForEach-Object { [string]$_ }) -join '; '))
    }
}

function New-DatabaseBackupAndTestMigrations {
    param([string]$BackupRoot, [Collections.IDictionary]$Environment)
    $dbName = [string]$Environment['KT1B_DB_NAME']
    $admin = [string]$Environment['KT1B_DB_ADMIN_USER']
    $appUser = [string]$Environment['KT1B_DB_APP_USER']
    $containerBackup = "/tmp/pdfconv-$ReleaseId.backup"
    $hostBackup = Join-Path $BackupRoot 'database.backup'
    $sqlRoot = "/tmp/pdfconv-$ReleaseId-sql"
    $tempDb = ('pdfconv_' + [Guid]::NewGuid().ToString('N')).Substring(0, 32)
    try {
        [void](Invoke-NativeChecked -Operation 'Create live custom DB backup' `
            -Command {
                & docker exec $script:DbContainer pg_dump -U $admin -d $dbName `
                    --format=custom --no-owner --file=$containerBackup
            })
        $catalog = @(Invoke-NativeChecked -Operation 'Inspect DB backup catalog' `
            -Command {
                & docker exec $script:DbContainer pg_restore --list $containerBackup
            })
        $catalogText = $catalog -join "`n"
        foreach ($table in @('docs_sw', 'docs_sw_file',
                'docs_sw_sub_file', 'docs_history')) {
            Assert-DeploymentCondition -Condition (
                Test-PgRestoreTableDataEntry -CatalogText $catalogText `
                    -TableName $table) `
                -Message "DB backup omits table data: $table"
        }
        [void](Invoke-NativeChecked -Operation 'Dry-run DB archive' -Command {
            & docker exec $script:DbContainer pg_restore --exit-on-error `
                --no-owner --no-privileges --file=/dev/null $containerBackup
        })
        $archiveSchemaFingerprint = Get-ArchiveSchemaFingerprint `
            -ContainerPath $containerBackup
        [void](Invoke-NativeChecked -Operation 'Copy DB backup to host' `
            -Command {
                & docker cp "${script:DbContainer}:$containerBackup" $hostBackup
            })
        $backupHash = (Get-FileHash -LiteralPath $hostBackup `
            -Algorithm SHA256).Hash
        [void](Invoke-NativeChecked -Operation 'Create SQL staging directory' `
            -Command { & docker exec $script:DbContainer mkdir -p $sqlRoot })
        [void](Invoke-NativeChecked -Operation 'Stage viewer DDL' -Command {
            & docker cp $script:Artifacts['viewerDdl'] `
                "${script:DbContainer}:$sqlRoot/viewer.sql"
        })
        [void](Invoke-NativeChecked -Operation 'Stage PDF DDL' -Command {
            & docker cp $script:Artifacts['pdfDdl'] `
                "${script:DbContainer}:$sqlRoot/pdf.sql"
        })
        [void](Invoke-NativeChecked -Operation 'Create migration test DB' `
            -Command {
                & docker exec $script:DbContainer createdb -U $admin -O $admin `
                    -E UTF8 $tempDb
            })
        [void](Invoke-NativeChecked -Operation 'Restore migration test DB' `
            -Command {
                & docker exec $script:DbContainer pg_restore --exit-on-error `
                    --no-owner --no-privileges -U $admin -d $tempDb `
                    $containerBackup
            })
        $originalDataFingerprint = Get-DatabaseFingerprint `
            -Database $tempDb -AdminUser $admin
        $originalFullDataFingerprint = Get-FullDatabaseFingerprint `
            -Database $tempDb -AdminUser $admin
        $restoredSchemaFingerprint = Get-SchemaFingerprint `
            -Database $tempDb -AdminUser $admin
        $protectedRowCounts = Get-ProtectedRowCounts -Database $tempDb `
            -AdminUser $admin
        foreach ($pass in @(1, 2)) {
            Invoke-PsqlFile -Database $tempDb -AdminUser $admin `
                -ContainerPath "$sqlRoot/viewer.sql" `
                -Operation "Migration test viewer pass $pass"
            Invoke-PsqlFile -Database $tempDb -AdminUser $admin `
                -ContainerPath "$sqlRoot/pdf.sql" `
                -Operation "Migration test PDF pass $pass"
            Grant-AppRole -Database $tempDb -AdminUser $admin -AppUser $appUser
            $schema = Get-SchemaFingerprint -Database $tempDb -AdminUser $admin
            if ($pass -eq 1) { $schemaFirst = $schema }
            Assert-DeploymentCondition -Condition (
                (Get-ProtectedRowCounts -Database $tempDb -AdminUser $admin) `
                    -ceq $protectedRowCounts) `
                -Message 'Migrations changed protected table row counts.'
        }
        Assert-DeploymentCondition -Condition ($schema -ceq $schemaFirst) `
            -Message 'Migration scripts are not repeatable in isolated PG17.'
        Assert-DeploymentCondition -Condition (
            (Get-DatabaseFingerprint -Database $tempDb -AdminUser $admin) -ceq
                $originalDataFingerprint) `
            -Message 'Migrations changed protected logical data.'
        return [ordered]@{
            Path = $hostBackup
            Sha256 = $backupHash
            ProtectedRowCounts = $protectedRowCounts
            MigratedSchemaFingerprint = $schemaFirst
            OriginalDataFingerprint = $originalDataFingerprint
            OriginalFullDataFingerprint = $originalFullDataFingerprint
            # Archive SQL is compared with the live schema. PostgreSQL may
            # normalize equivalent CHECK/index expressions after restore, so
            # the restored schema is retained separately for audit evidence.
            OriginalSchemaFingerprint = $archiveSchemaFingerprint
            RestoredSchemaFingerprint = $restoredSchemaFingerprint
            SqlRoot = $sqlRoot
        }
    } finally {
        try {
            [void](Invoke-NativeChecked -Operation 'Drop migration test DB' `
                -Command {
                    & docker exec $script:DbContainer dropdb --force `
                        --if-exists -U $admin $tempDb
                })
        } catch { }
        try {
            [void](Invoke-NativeChecked -Operation 'Remove DB temp artifacts' `
                -Command {
                    & docker exec $script:DbContainer rm -rf `
                        $containerBackup $sqlRoot
                })
        } catch { }
    }
}

function New-RestrictedBuildContext {
    param([string]$BackupRoot)
    $context = Join-Path $BackupRoot 'build-context'
    New-Item -ItemType Directory -Path $context | Out-Null
    Copy-VerifiedFile -Source $script:Dockerfile `
        -Destination (Join-Path $context 'Dockerfile')
    $dockerfileText = [IO.File]::ReadAllText($script:Dockerfile)
    $sources = [Collections.Generic.List[string]]::new()
    foreach ($line in @([regex]::Split($dockerfileText, '\r?\n') |
            Where-Object { $_ -match '(?i)^\s*COPY\s+' })) {
        $pair = Get-DockerfileCopyPair -Instruction $line
        $source = [string]$pair.Source
        $target = Join-Path $context $source.Replace('/', '\')
        if ($source -match '^app/(?:SDMS|TDMS)-KT-1B\.war$') {
            Copy-VerifiedFile -Source $script:Artifacts['war'] `
                -Destination $target
        } else {
            $liveSource = Join-Path $script:Root $source.Replace('/', '\')
            Copy-VerifiedFile -Source $liveSource -Destination $target
        }
        $sources.Add($source)
    }
    $ignore = [Collections.Generic.List[string]]::new()
    $ignore.Add('*')
    $ignore.Add('!Dockerfile')
    foreach ($source in $sources) {
        $segments = $source.Split('/')
        if ($segments.Count -gt 1) { $ignore.Add('!' + $segments[0] + '/') }
        $ignore.Add('!' + $source)
    }
    [IO.File]::WriteAllLines((Join-Path $context '.dockerignore'),
        $ignore.ToArray(), [Text.Encoding]::ASCII)
    return $context
}

function Update-ChecksumManifest {
    param([string]$Source, [string]$Destination, [string]$WarHash)
    $lines = [Collections.Generic.List[string]]::new()
    $count = 0
    foreach ($line in [IO.File]::ReadAllLines($Source)) {
        if ($line -match '(?i)\s+(?:\*|\./)?app[/\\](?:SDMS|TDMS)-KT-1B\.war\s*$') {
            $lines.Add($WarHash.ToLowerInvariant() +
                '  app/SDMS-KT-1B.war')
            $count++
        } else {
            $lines.Add($line)
        }
    }
    Assert-DeploymentCondition -Condition ($count -eq 1) `
        -Message 'Checksum manifest must contain one TDMS WAR entry.'
    [IO.File]::WriteAllLines($Destination, $lines.ToArray(),
        [Text.Encoding]::ASCII)
}

function New-PreOutageState {
    $backupRoot = Join-Path $script:RunLogs `
        "pdf-conversion-backup-$ReleaseId"
    Assert-DeploymentCondition -Condition (-not
        (Test-Path -LiteralPath $backupRoot)) `
        -Message 'Release backup already exists; use Rollback or a new release id.'
    New-Item -ItemType Directory -Path $backupRoot | Out-Null
    Protect-BackupDirectory -Path $backupRoot
    $files = Join-Path $backupRoot 'runtime-files'
    New-Item -ItemType Directory -Path $files | Out-Null
    Copy-VerifiedFile -Source $script:BaseEnvironment `
        -Destination (Join-Path $files 'tdms.env')
    Copy-VerifiedFile -Source $script:BaseCompose `
        -Destination (Join-Path $files 'compose.remote.yaml')
    Copy-VerifiedFile -Source $script:SecretEnvironment `
        -Destination (Join-Path $files 'pdf-conversion.env')
    Copy-VerifiedFile -Source $script:LiveWar `
        -Destination (Join-Path $files 'SDMS-KT-1B.war')
    Copy-VerifiedFile -Source $script:Checksums `
        -Destination (Join-Path $files 'checksums.sha256')
    $overlayExisted = Test-Path -LiteralPath $script:LiveOverlay -PathType Leaf
    if ($overlayExisted) {
        Copy-VerifiedFile -Source $script:LiveOverlay `
            -Destination (Join-Path $files 'compose.pdf-conversion.yaml')
    }
    $storageBackup = Join-Path $backupRoot 'storage'
    Copy-Item -LiteralPath $script:Storage -Destination $storageBackup -Recurse
    $storageFingerprint = Get-DeploymentDirectoryFingerprint `
        -Root $script:Storage
    Assert-DeploymentCondition -Condition (
        (Get-DeploymentDirectoryFingerprint -Root $storageBackup) -ceq
        $storageFingerprint) -Message 'Storage backup fingerprint mismatch.'

    $environment = Read-BaseEnvironment
    $migrationTestBackup = New-DatabaseBackupAndTestMigrations `
        -BackupRoot $backupRoot -Environment $environment

    [void](Invoke-NativeChecked -Operation 'Load File API image archive' `
        -Command {
            & docker image load --input `
                $script:Artifacts['fileApiImageArchive']
        })
    Assert-DeploymentCondition -Condition (
        (Get-ImageId -Image ([string]$script:Request.images.fileApi)) -ceq
        [string]$script:Request.images.fileApiId) `
        -Message 'Loaded File API image differs from the approved archive.'

    $context = New-RestrictedBuildContext -BackupRoot $backupRoot
    [void](Invoke-NativeChecked -Operation 'Build immutable TDMS app image' `
        -Command {
            & docker build --tag ([string]$script:Request.images.app) `
                --file (Join-Path $context 'Dockerfile') $context
        })
    $preparedAppImageId = Get-ImageId `
        -Image ([string]$script:Request.images.app)

    # Production DDL is additive and is applied while the old application is
    # still healthy. It intentionally remains after runtime-only rollback.
    $dbName = [string]$environment['KT1B_DB_NAME']
    $admin = [string]$environment['KT1B_DB_ADMIN_USER']
    $appUser = [string]$environment['KT1B_DB_APP_USER']
    Assert-DeploymentCondition -Condition (
        (Get-DatabaseFingerprint -Database $dbName -AdminUser $admin) -ceq
            $migrationTestBackup.OriginalDataFingerprint) `
        -Message 'Protected DB data changed after the approved backup.'
    Assert-DeploymentCondition -Condition (
        (Get-SchemaFingerprint -Database $dbName -AdminUser $admin) -ceq
            $migrationTestBackup.OriginalSchemaFingerprint) `
        -Message 'Database schema changed after the approved backup.'
    $sqlRoot = "/tmp/pdfconv-$ReleaseId-production"
    try {
        [void](Invoke-NativeChecked -Operation 'Create production SQL stage' `
            -Command { & docker exec $script:DbContainer mkdir -p $sqlRoot })
        [void](Invoke-NativeChecked -Operation 'Stage production viewer DDL' `
            -Command {
                & docker cp $script:Artifacts['viewerDdl'] `
                    "${script:DbContainer}:$sqlRoot/viewer.sql"
            })
        [void](Invoke-NativeChecked -Operation 'Stage production PDF DDL' `
            -Command {
                & docker cp $script:Artifacts['pdfDdl'] `
                    "${script:DbContainer}:$sqlRoot/pdf.sql"
            })
        Invoke-PsqlFile -Database $dbName -AdminUser $admin `
            -ContainerPath "$sqlRoot/viewer.sql" `
            -Operation 'Apply production viewer DDL'
        Invoke-PsqlFile -Database $dbName -AdminUser $admin `
            -ContainerPath "$sqlRoot/pdf.sql" `
            -Operation 'Apply production PDF DDL'
        Grant-AppRole -Database $dbName -AdminUser $admin -AppUser $appUser
    } finally {
        try {
            [void](Invoke-NativeChecked -Operation 'Remove production SQL stage' `
                -Command {
                    & docker exec $script:DbContainer rm -rf $sqlRoot
                })
        } catch { }
    }
    Assert-DeploymentCondition -Condition (
        (Get-ProtectedRowCounts -Database $dbName -AdminUser $admin) -ceq
            $migrationTestBackup.ProtectedRowCounts) `
        -Message 'Production migrations changed protected table row counts.'
    Assert-DeploymentCondition -Condition (
        (Get-SchemaFingerprint -Database $dbName -AdminUser $admin) -ceq
            $migrationTestBackup.MigratedSchemaFingerprint) `
        -Message 'Production schema differs from the isolated PG17 result.'
    Assert-DeploymentCondition -Condition (
        (Get-DatabaseFingerprint -Database $dbName -AdminUser $admin) -ceq
            $migrationTestBackup.OriginalDataFingerprint) `
        -Message 'Production migrations changed protected logical data.'
    $preMigrationArchive = Join-Path $backupRoot `
        'database.pre-migration.backup'
    Move-Item -LiteralPath ([string]$migrationTestBackup.Path) `
        -Destination $preMigrationArchive
    $databaseBackup = New-DatabaseBackupAndTestMigrations `
        -BackupRoot $backupRoot -Environment $environment
    $databaseWalPosition = Get-DatabaseWalPosition -Database $dbName `
        -AdminUser $admin
    $databaseFingerprint = Get-DatabaseFingerprint -Database $dbName `
        -AdminUser $admin
    $databaseSchemaFingerprint = Get-SchemaFingerprint -Database $dbName `
        -AdminUser $admin
    Assert-DeploymentCondition -Condition (
        $databaseFingerprint -ceq $databaseBackup.OriginalDataFingerprint -and
        $databaseSchemaFingerprint -ceq
            $databaseBackup.OriginalSchemaFingerprint) `
        -Message 'Live database changed after the final recovery backup.'
    Assert-DeploymentCondition -Condition (
        (Get-DeploymentDirectoryFingerprint -Root $script:Storage) -ceq
        $storageFingerprint) `
        -Message 'Storage changed between backup and outage gate.'
    $storageMetadataFingerprint = Get-StorageMetadataFingerprint

    $oldImageId = (@(Invoke-NativeChecked `
        -Operation 'Inspect original app image id' -Command {
            & docker inspect --format '{{.Image}}' $script:AppContainer
        }))[0].ToString().Trim()
    $oldImageReference = (@(Invoke-NativeChecked `
        -Operation 'Inspect original app image reference' -Command {
            & docker inspect --format '{{.Config.Image}}' $script:AppContainer
        }))[0].ToString().Trim()
    Assert-DeploymentCondition -Condition (
        $oldImageId -match '^sha256:[0-9a-f]{64}$') `
        -Message 'Original app image id is invalid.'
    Assert-DeploymentCondition -Condition (
        $oldImageReference -match
            '^[A-Za-z0-9][A-Za-z0-9._/-]*:[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$') `
        -Message 'Original app image reference is invalid.'
    $rollbackTag = "kt1b-dms-app:rollback-$ReleaseId"
    [void](Invoke-NativeChecked -Operation 'Create original app rollback tag' `
        -Command { & docker image tag $oldImageId $rollbackTag })
    $fileApiOriginal = Get-OptionalSidecarRollbackRecord `
        -ContainerName $script:FileApiContainer -ServiceName 'file-api' `
        -RollbackTag "kt1b-tdds-ft:rollback-$ReleaseId"
    $converterOriginal = Get-OptionalSidecarRollbackRecord `
        -ContainerName $script:ConverterContainer -ServiceName 'pdf-converter' `
        -RollbackTag "kt1b-e-pdf-converter:rollback-$ReleaseId"
    Assert-DeploymentCondition -Condition (
        $overlayExisted -or
        (-not [bool]$fileApiOriginal.existed -and
         -not [bool]$converterOriginal.existed)) `
        -Message 'Existing sidecars require a restorable live overlay.'
    $rollbackEnvironment = Join-Path $backupRoot 'rollback-images.env'
    [IO.File]::WriteAllLines($rollbackEnvironment, @(
            "KT1B_APP_IMAGE=$oldImageReference",
            ('KT1B_FILE_API_IMAGE=' + $(if ([bool]$fileApiOriginal.existed) {
                        [string]$fileApiOriginal.rollbackImageTag
                    } else { [string]$script:Request.images.fileApi })),
            ('KT1B_PDF_CONVERTER_IMAGE=' + $(if ([bool]$converterOriginal.existed) {
                        [string]$converterOriginal.rollbackImageTag
                    } else { [string]$script:Request.images.converter }))),
        [Text.Encoding]::ASCII)

    $nextChecksums = Join-Path $backupRoot 'checksums.next'
    Update-ChecksumManifest -Source $script:Checksums `
        -Destination $nextChecksums `
        -WarHash (Get-FileHash -LiteralPath $script:Artifacts['war'] `
            -Algorithm SHA256).Hash
    $state = [ordered]@{
        protocolVersion = 2
        releaseId = $ReleaseId
        preparedAt = (Get-Date).ToUniversalTime().ToString('o')
        backupRoot = $backupRoot
        baseEnvironmentSha256 = (Get-FileHash `
            -LiteralPath $script:BaseEnvironment -Algorithm SHA256).Hash
        baseComposeSha256 = (Get-FileHash `
            -LiteralPath $script:BaseCompose -Algorithm SHA256).Hash
        secretEnvironmentSha256 = (Get-FileHash `
            -LiteralPath $script:SecretEnvironment -Algorithm SHA256).Hash
        overlayExisted = $overlayExisted
        originalWarSha256 = (Get-FileHash `
            -LiteralPath $script:LiveWar -Algorithm SHA256).Hash
        releaseWarSha256 = (Get-FileHash `
            -LiteralPath $script:Artifacts['war'] -Algorithm SHA256).Hash
        originalAppImageId = $oldImageId
        originalAppImageReference = $oldImageReference
        rollbackImageTag = $rollbackTag
        preparedAppImageId = $preparedAppImageId
        fileApiImageId = [string]$script:Request.images.fileApiId
        converterImageId = [string]$script:Request.images.converterId
        originalFileApi = $fileApiOriginal
        originalConverter = $converterOriginal
        rollbackEnvironment = $rollbackEnvironment
        rollbackEnvironmentSha256 = (Get-FileHash `
            -LiteralPath $rollbackEnvironment -Algorithm SHA256).Hash
        appContainerId = Get-ContainerId -Name $script:AppContainer
        gatewayContainerId = Get-ContainerId -Name $script:GatewayContainer
        databaseContainerId = Get-ContainerId -Name $script:DbContainer
        databaseName = $dbName
        databaseAdminUser = $admin
        databaseAppUser = $appUser
        databaseBackup = [string]$databaseBackup.Path
        databaseBackupSha256 = [string]$databaseBackup.Sha256
        originalDatabaseFingerprint = [string]$databaseBackup.OriginalDataFingerprint
        originalDatabaseFullFingerprint = [string]$databaseBackup.OriginalFullDataFingerprint
        restoredDatabaseFullFingerprint = [string]$databaseBackup.OriginalFullDataFingerprint
        originalDatabaseSchemaFingerprint = [string]$databaseBackup.OriginalSchemaFingerprint
        archiveDatabaseSchemaFingerprint = [string]$databaseBackup.OriginalSchemaFingerprint
        restoredDatabaseSchemaFingerprint = [string]$databaseBackup.RestoredSchemaFingerprint
        databaseFingerprint = $databaseFingerprint
        databaseSchemaFingerprint = $databaseSchemaFingerprint
        databaseWalPosition = $databaseWalPosition
        storageBackup = $storageBackup
        storageFingerprint = $storageFingerprint
        storageMetadataFingerprint = $storageMetadataFingerprint
        runtimeBackupHashes = [ordered]@{
            baseEnvironment = (Get-FileHash -LiteralPath `
                (Join-Path $files 'tdms.env') -Algorithm SHA256).Hash
            baseCompose = (Get-FileHash -LiteralPath `
                (Join-Path $files 'compose.remote.yaml') -Algorithm SHA256).Hash
            secretEnvironment = (Get-FileHash -LiteralPath `
                (Join-Path $files 'pdf-conversion.env') -Algorithm SHA256).Hash
            war = (Get-FileHash -LiteralPath `
                (Join-Path $files 'SDMS-KT-1B.war') -Algorithm SHA256).Hash
            checksums = (Get-FileHash -LiteralPath `
                (Join-Path $files 'checksums.sha256') -Algorithm SHA256).Hash
            overlay = if ($overlayExisted) {
                (Get-FileHash -LiteralPath (Join-Path $files `
                    'compose.pdf-conversion.yaml') -Algorithm SHA256).Hash
            } else { $null }
        }
        checksumsNext = $nextChecksums
        checksumsNextSha256 = (Get-FileHash -LiteralPath $nextChecksums `
            -Algorithm SHA256).Hash
        outageTimeoutSeconds = [int]$script:Request.outageTimeoutSeconds
    }
    Write-DeploymentJsonAtomically -Value $state -Path $script:StatePath
    $state['stateSha256'] = (Get-FileHash -LiteralPath $script:StatePath `
        -Algorithm SHA256).Hash
    return $state
}

function Restore-RuntimeFileSet {
    param([object]$State)
    $files = Join-Path ([string]$State.backupRoot) 'runtime-files'
    $quarantine = Join-Path ([string]$State.backupRoot) 'failed-runtime'
    foreach ($entry in @(
            @{ Source = 'tdms.env'; Target = $script:BaseEnvironment;
                Hash = [string]$State.runtimeBackupHashes.baseEnvironment },
            @{ Source = 'compose.remote.yaml'; Target = $script:BaseCompose;
                Hash = [string]$State.runtimeBackupHashes.baseCompose },
            @{ Source = 'pdf-conversion.env'; Target = $script:SecretEnvironment;
                Hash = [string]$State.runtimeBackupHashes.secretEnvironment },
            @{ Source = 'SDMS-KT-1B.war'; Target = $script:LiveWar;
                Hash = [string]$State.runtimeBackupHashes.war },
            @{ Source = 'checksums.sha256'; Target = $script:Checksums;
                Hash = [string]$State.runtimeBackupHashes.checksums })) {
        $backupFile = Join-Path $files $entry.Source
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $backupFile -Algorithm SHA256).Hash -ceq
            $entry.Hash) -Message "Runtime backup was altered: $($entry.Source)"
        [void](Install-DeploymentFileSafely `
            -Source $backupFile `
            -Target $entry.Target -QuarantineDirectory $quarantine)
    }
    Protect-DeploymentSecretFile -Path $script:SecretEnvironment
    Assert-ProtectedDeploymentSecretFile -Path $script:SecretEnvironment
    if ([bool]$State.overlayExisted) {
        $overlayBackup = Join-Path $files 'compose.pdf-conversion.yaml'
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $overlayBackup -Algorithm SHA256).Hash -ceq
            [string]$State.runtimeBackupHashes.overlay) `
            -Message 'Runtime overlay backup was altered.'
        [void](Install-DeploymentFileSafely `
            -Source $overlayBackup `
            -Target $script:LiveOverlay -QuarantineDirectory $quarantine)
    } elseif (Test-Path -LiteralPath $script:LiveOverlay -PathType Leaf) {
        if (-not (Test-Path -LiteralPath $quarantine -PathType Container)) {
            New-Item -ItemType Directory -Path $quarantine | Out-Null
        }
        Move-Item -LiteralPath $script:LiveOverlay -Destination `
            (Join-Path $quarantine ('release-overlay-' +
                [Guid]::NewGuid().ToString('N') + '.yaml'))
    }
}

function Assert-RuntimeRollbackAssets {
    param([object]$State)
    $files = Join-Path ([string]$State.backupRoot) 'runtime-files'
    foreach ($entry in @(
            @{ File = 'tdms.env'; Hash = [string]$State.runtimeBackupHashes.baseEnvironment },
            @{ File = 'compose.remote.yaml'; Hash = [string]$State.runtimeBackupHashes.baseCompose },
            @{ File = 'pdf-conversion.env'; Hash = [string]$State.runtimeBackupHashes.secretEnvironment },
            @{ File = 'SDMS-KT-1B.war'; Hash = [string]$State.runtimeBackupHashes.war },
            @{ File = 'checksums.sha256'; Hash = [string]$State.runtimeBackupHashes.checksums })) {
        $path = Join-Path $files $entry.File
        Assert-DeploymentCondition -Condition (
            Test-Path -LiteralPath $path -PathType Leaf) `
            -Message "Runtime rollback file is missing: $($entry.File)"
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ceq
                $entry.Hash) `
            -Message "Runtime rollback file was altered: $($entry.File)"
    }
    if ([bool]$State.overlayExisted) {
        $overlay = Join-Path $files 'compose.pdf-conversion.yaml'
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $overlay -Algorithm SHA256).Hash -ceq
                [string]$State.runtimeBackupHashes.overlay) `
            -Message 'Runtime rollback overlay was altered.'
    }
    Assert-DeploymentCondition -Condition (
        (Get-FileHash -LiteralPath ([string]$State.rollbackEnvironment) `
            -Algorithm SHA256).Hash -ceq
            [string]$State.rollbackEnvironmentSha256) `
        -Message 'Runtime rollback image environment was altered.'
    Assert-RollbackImageIdentity `
        -OriginalImageId ([string]$State.originalAppImageId) `
        -RollbackTagImageId (Get-ImageId `
            -Image ([string]$State.rollbackImageTag))
    foreach ($sidecar in @($State.originalFileApi,
            $State.originalConverter)) {
        if ([bool]$sidecar.existed) {
            Assert-RollbackImageIdentity `
                -OriginalImageId ([string]$sidecar.imageId) `
                -RollbackTagImageId (Get-ImageId `
                    -Image ([string]$sidecar.rollbackImageTag))
        }
    }
}

function Resume-OriginalBeforeMutation {
    param([object]$State, [DateTime]$Deadline)
    Assert-DeploymentCondition -Condition (-not
        $script:RuntimeMutationStarted) `
        -Message 'Original containers can be resumed only before runtime mutation.'
    Assert-DatabaseContainerInvariant -State $State `
        -Operation 'before unchanged runtime resume'
    Assert-DeploymentCondition -Condition (
        (Get-ContainerId -Name $script:AppContainer) -ceq
            [string]$State.appContainerId) `
        -Message 'Original app container identity changed before resume.'
    [void](Invoke-NativeChecked -Operation 'Resume unchanged original app' `
        -Command { & docker start $script:AppContainer })
    Wait-Healthy -Name $script:AppContainer -Deadline $Deadline
    foreach ($sidecar in @($State.originalFileApi,
            $State.originalConverter)) {
        if (-not [bool]$sidecar.existed) {
            Assert-DeploymentCondition -Condition (-not
                (Test-ContainerExists -Name ([string]$sidecar.containerName))) `
                -Message 'A release-only sidecar appeared before mutation.'
            continue
        }
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name ([string]$sidecar.containerName)) -ceq
                [string]$sidecar.containerId) `
            -Message "Original sidecar identity changed: $($sidecar.containerName)"
        [void](Invoke-NativeChecked `
            -Operation "Resume unchanged sidecar $($sidecar.containerName)" `
            -Command { & docker start ([string]$sidecar.containerName) })
        Wait-Healthy -Name ([string]$sidecar.containerName) `
            -Deadline $Deadline
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name ([string]$sidecar.containerName)) -ceq
                [string]$sidecar.containerId) `
            -Message "Original sidecar was replaced: $($sidecar.containerName)"
    }
    Assert-DeploymentCondition -Condition (
        (Get-ContainerId -Name $script:GatewayContainer) -ceq
            [string]$State.gatewayContainerId) `
        -Message 'Original gateway identity changed before resume.'
    [void](Invoke-NativeChecked -Operation 'Resume unchanged gateway' `
        -Command { & docker start $script:GatewayContainer })
    Wait-Healthy -Name $script:GatewayContainer -Deadline $Deadline
    Assert-DeploymentCondition -Condition (
        (Get-ContainerId -Name $script:GatewayContainer) -ceq
            [string]$State.gatewayContainerId) `
        -Message 'Original gateway was replaced during resume.'
    Assert-DatabaseContainerInvariant -State $State `
        -Operation 'after unchanged runtime resume'
    $script:GatewayHealthyAt = Get-Date
    Write-OutageCompletion -Outcome 'RESUMED_BEFORE_MUTATION'
}

function Invoke-RuntimeRollback {
    param([object]$State, [DateTime]$Deadline)
    $script:RollbackStartedAt = Get-Date
    $script:OutageDeadline = $Deadline
    $operations = [ordered]@{
        AssertDatabaseInvariant = {
            param([string]$Phase, [string]$Position)
            Assert-DatabaseContainerInvariant -State $State `
                -Operation "$Position rollback phase $Phase"
        }
        StopGateway = {
            Stop-ContainerIfPresent -Name $script:GatewayContainer
        }
        StopReleaseServices = {
            foreach ($name in @($script:FileApiContainer,
                    $script:ConverterContainer, $script:AppContainer)) {
                Stop-ContainerIfPresent -Name $name
            }
        }
        RestoreRuntimeFiles = {
            Restore-RuntimeFileSet -State $State
        }
        RestoreOriginalImage = {
            $tagId = Get-ImageId -Image ([string]$State.rollbackImageTag)
            Assert-RollbackImageIdentity `
                -OriginalImageId ([string]$State.originalAppImageId) `
                -RollbackTagImageId $tagId
            [void](Invoke-NativeChecked -Operation 'Restore original app image tag' `
                -Command {
                    & docker image tag ([string]$State.rollbackImageTag) `
                        ([string]$State.originalAppImageReference)
                })
            foreach ($sidecar in @($State.originalFileApi,
                    $State.originalConverter)) {
                if ([bool]$sidecar.existed) {
                    $sidecarTagId = Get-ImageId `
                        -Image ([string]$sidecar.rollbackImageTag)
                    Assert-RollbackImageIdentity `
                        -OriginalImageId ([string]$sidecar.imageId) `
                        -RollbackTagImageId $sidecarTagId
                }
            }
        }
        RecreateOriginalApp = {
            [void](Invoke-BaseCompose -Arguments @(
                'up', '-d', '--no-deps', '--force-recreate',
                '--no-build', 'app') -Operation 'Recreate original app')
            Wait-Healthy -Name $script:AppContainer `
                -Deadline $Deadline
        }
        RestoreOriginalSidecars = {
            Assert-DeploymentCondition -Condition (
                (Get-FileHash -LiteralPath ([string]$State.rollbackEnvironment) `
                    -Algorithm SHA256).Hash -ceq
                    [string]$State.rollbackEnvironmentSha256) `
                -Message 'Rollback image environment was altered.'
            $services = [Collections.Generic.List[string]]::new()
            foreach ($sidecar in @($State.originalFileApi,
                    $State.originalConverter)) {
                if ([bool]$sidecar.existed) {
                    [void]$services.Add([string]$sidecar.serviceName)
                } elseif (Test-ContainerExists `
                        -Name ([string]$sidecar.containerName)) {
                    [void](Invoke-NativeChecked `
                        -Operation "Remove release-only sidecar $($sidecar.containerName)" `
                        -Command {
                            & docker rm --force ([string]$sidecar.containerName)
                        })
                }
            }
            if ($services.Count -gt 0) {
                [void](Invoke-Compose -Overlay $script:LiveOverlay `
                    -ReleaseEnvironment ([string]$State.rollbackEnvironment) `
                    -Arguments (@('up', '-d', '--no-build', '--pull', 'never',
                        '--force-recreate', '--no-deps') + $services.ToArray()) `
                    -Operation 'Restore original private sidecars')
                foreach ($sidecar in @($State.originalFileApi,
                        $State.originalConverter)) {
                    if (-not [bool]$sidecar.existed) { continue }
                    Wait-Healthy -Name ([string]$sidecar.containerName) `
                        -Deadline $Deadline
                    $restoredImage = (@(Invoke-NativeChecked `
                        -Operation "Verify restored sidecar $($sidecar.containerName)" `
                        -Command {
                            & docker inspect --format '{{.Image}}' `
                                ([string]$sidecar.containerName)
                        }))[0].ToString().Trim()
                    Assert-DeploymentCondition -Condition (
                        $restoredImage -ceq [string]$sidecar.imageId) `
                        -Message "Wrong rollback sidecar image: $($sidecar.containerName)"
                }
            }
        }
        VerifyOriginalApp = {
            $image = (@(Invoke-NativeChecked `
                -Operation 'Verify restored app image' -Command {
                    & docker inspect --format '{{.Image}}' $script:AppContainer
                }))[0].ToString().Trim()
            Assert-DeploymentCondition -Condition (
                $image -ceq [string]$State.originalAppImageId) `
                -Message 'Runtime rollback restored the wrong app image.'
            $war = (@(Invoke-NativeChecked -Operation 'Verify restored app WAR' `
                -Command {
                    & docker exec $script:AppContainer sha256sum `
                        /opt/kt1b/SDMS-KT-1B.war
                }))[0].ToString().Split(' ')[0].ToUpperInvariant()
            Assert-DeploymentCondition -Condition (
                $war -ceq [string]$State.originalWarSha256) `
                -Message 'Runtime rollback restored the wrong WAR.'
            $restoredAppId = Get-ContainerId -Name $script:AppContainer
            $restoredAppSandbox = (@(Invoke-NativeChecked `
                -Operation 'Inspect restored app sandbox' -Command {
                    & docker inspect --format '{{.NetworkSettings.SandboxKey}}' `
                        $script:AppContainer
                }))[0].ToString().Trim()
            foreach ($sidecar in @($State.originalFileApi,
                    $State.originalConverter)) {
                if (-not [bool]$sidecar.existed) {
                    Assert-DeploymentCondition -Condition (-not
                        (Test-ContainerExists -Name ([string]$sidecar.containerName))) `
                        -Message 'Release-only sidecar remains after rollback.'
                    continue
                }
                $inspect = (@(Invoke-NativeChecked `
                    -Operation "Inspect restored sidecar topology $($sidecar.containerName)" `
                    -Command {
                        & docker inspect --format '{{json .}}' `
                            ([string]$sidecar.containerName)
                    }))[0].ToString() | ConvertFrom-Json
                Assert-DeploymentCondition -Condition (
                    [string]$inspect.HostConfig.NetworkMode -ceq
                        "container:$restoredAppId" -and
                    [string]$inspect.NetworkSettings.SandboxKey -ceq
                        $restoredAppSandbox -and
                    -not (Test-PublishedBindingForPort `
                        -Bindings $inspect.HostConfig.PortBindings -Port 9001) -and
                    -not (Test-PublishedBindingForPort `
                        -Bindings $inspect.HostConfig.PortBindings -Port 18080)) `
                    -Message 'Restored sidecar topology is unsafe.'
            }
        }
        VerifyPreservedDataFingerprints = {
            $database = Get-DatabaseFingerprint `
                -Database ([string]$State.databaseName) `
                -AdminUser ([string]$State.databaseAdminUser)
            $databaseSchema = Get-SchemaFingerprint `
                -Database ([string]$State.databaseName) `
                -AdminUser ([string]$State.databaseAdminUser)
            $storage = Get-DeploymentDirectoryFingerprint -Root $script:Storage
            $storageMetadata = Get-StorageMetadataFingerprint
            Assert-DeploymentCondition -Condition (
                $database -ceq [string]$State.databaseFingerprint) `
                -Message 'Database fingerprint changed; gateway remains closed.'
            Assert-DeploymentCondition -Condition (
                $databaseSchema -ceq
                    [string]$State.databaseSchemaFingerprint) `
                -Message 'Database schema fingerprint changed; gateway remains closed.'
            Assert-DeploymentCondition -Condition (
                $storage -ceq [string]$State.storageFingerprint) `
                -Message 'Storage fingerprint changed; gateway remains closed.'
            Assert-DeploymentCondition -Condition (
                $storageMetadata -ceq
                    [string]$State.storageMetadataFingerprint) `
                -Message 'Storage metadata changed; gateway remains closed.'
        }
        StartExistingGateway = {
            Assert-DeploymentCondition -Condition (
                (Get-ContainerId -Name $script:GatewayContainer) -ceq
                [string]$State.gatewayContainerId) `
                -Message 'Gateway container was replaced during rollback.'
            [void](Invoke-NativeChecked -Operation 'Start existing gateway' `
                -Command { & docker start $script:GatewayContainer })
            Wait-Healthy -Name $script:GatewayContainer `
                -Deadline $Deadline
            Assert-DeploymentCondition -Condition (
                (Get-ContainerId -Name $script:GatewayContainer) -ceq
                [string]$State.gatewayContainerId) `
                -Message 'Gateway container identity changed during start.'
            $script:GatewayHealthyAt = Get-Date
        }
    }
    Invoke-PdfRuntimeOnlyRollback -Operations $operations -Deadline $Deadline
}

function Assert-AndRestoreDamagedData {
    param([object]$State, [switch]$ValidateOnly)
    Assert-DeploymentCondition -Condition $RestoreData.IsPresent `
        -Message 'Data restore requires the explicit RestoreData switch.'
    Assert-DeploymentCondition -Condition (-not
        [string]::IsNullOrWhiteSpace($ExpectedEvidenceSha256)) `
        -Message 'Data restore requires an approved evidence hash.'
    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $script:EvidencePath -PathType Leaf) `
        -Message 'Fingerprint damage evidence file is missing.'
    Assert-DeploymentCondition -Condition (
        (Get-FileHash -LiteralPath $script:EvidencePath -Algorithm SHA256).Hash -ceq
        $ExpectedEvidenceSha256.ToUpperInvariant()) `
        -Message 'Fingerprint damage evidence hash is invalid.'
    $evidence = Get-Content -LiteralPath $script:EvidencePath -Raw |
        ConvertFrom-Json
    Assert-DeploymentCondition -Condition (
        [int]$evidence.protocolVersion -eq 1 -and
        [string]$evidence.releaseId -ceq $ReleaseId -and
        [string]$evidence.stateSha256 -ceq $ExpectedStateSha256.ToUpperInvariant()) `
        -Message 'Fingerprint evidence does not identify this release state.'
    Assert-DeploymentCondition -Condition (
        [string]$evidence.expectedDatabaseFingerprint -ceq
            [string]$State.databaseFingerprint -and
        [string]$evidence.expectedDatabaseFullFingerprint -ceq
            [string]$State.originalDatabaseFullFingerprint -and
        [string]$evidence.expectedDatabaseSchemaFingerprint -ceq
            [string]$State.databaseSchemaFingerprint -and
        [string]$evidence.expectedStorageFingerprint -ceq
            [string]$State.storageFingerprint) `
        -Message 'Fingerprint evidence has the wrong protected baseline.'
    $actualDb = Get-DatabaseFingerprint `
        -Database ([string]$State.databaseName) `
        -AdminUser ([string]$State.databaseAdminUser)
    $actualDbSchema = Get-SchemaFingerprint `
        -Database ([string]$State.databaseName) `
        -AdminUser ([string]$State.databaseAdminUser)
    $actualDbFull = Get-FullDatabaseFingerprint `
        -Database ([string]$State.databaseName) `
        -AdminUser ([string]$State.databaseAdminUser)
    $actualStorage = Get-DeploymentDirectoryFingerprint -Root $script:Storage
    Assert-DeploymentCondition -Condition (
        [string]$evidence.observedDatabaseFingerprint -ceq $actualDb -and
        [string]$evidence.observedDatabaseFullFingerprint -ceq $actualDbFull -and
        [string]$evidence.observedDatabaseSchemaFingerprint -ceq
            $actualDbSchema -and
        [string]$evidence.observedStorageFingerprint -ceq $actualStorage) `
        -Message 'Fingerprint evidence does not match current live data.'
    $dbDamaged = $actualDb -cne [string]$State.databaseFingerprint -or
        $actualDbSchema -cne [string]$State.databaseSchemaFingerprint -or
        $actualDbFull -cne [string]$State.originalDatabaseFullFingerprint
    $storageDamaged = $actualStorage -cne [string]$State.storageFingerprint
    Assert-DeploymentCondition -Condition ($dbDamaged -or $storageDamaged) `
        -Message 'No actual fingerprint damage exists; data restore is forbidden.'

    if ($dbDamaged) {
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath ([string]$State.databaseBackup) `
                -Algorithm SHA256).Hash -ceq
            [string]$State.databaseBackupSha256) `
            -Message 'Database recovery archive hash is invalid.'
    }
    if ($storageDamaged) {
        Assert-DeploymentCondition -Condition (
            (Get-DeploymentDirectoryFingerprint `
                -Root ([string]$State.storageBackup)) -ceq
            [string]$State.storageFingerprint) `
            -Message 'Storage recovery snapshot fingerprint is invalid.'
    }
    if ($ValidateOnly) {
        return [pscustomobject]@{
            DatabaseRestoreRequired = $dbDamaged
            StorageRestoreRequired = $storageDamaged
        }
    }

    if ($dbDamaged) {
        $backup = [string]$State.databaseBackup
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash -ceq
            [string]$State.databaseBackupSha256) `
            -Message 'Database recovery archive hash is invalid.'
        $containerBackup = "/tmp/pdfconv-$ReleaseId-explicit-restore.backup"
        try {
            [void](Invoke-NativeChecked -Operation 'Copy explicit DB recovery archive' `
                -Command {
                    & docker cp $backup "${script:DbContainer}:$containerBackup"
                })
            [void](Invoke-NativeChecked -Operation 'Drop damaged database' `
                -Command {
                    & docker exec $script:DbContainer dropdb --force --if-exists `
                        -U ([string]$State.databaseAdminUser) `
                        ([string]$State.databaseName)
                })
            [void](Invoke-NativeChecked -Operation 'Recreate recovery database' `
                -Command {
                    & docker exec $script:DbContainer createdb `
                        -U ([string]$State.databaseAdminUser) `
                        -O ([string]$State.databaseAdminUser) -E UTF8 `
                        ([string]$State.databaseName)
                })
            [void](Invoke-NativeChecked -Operation 'Restore damaged database' `
                -Command {
                    & docker exec $script:DbContainer pg_restore --exit-on-error `
                        --no-owner --no-privileges `
                        -U ([string]$State.databaseAdminUser) `
                        -d ([string]$State.databaseName) $containerBackup
                })
            Grant-AppRole -Database ([string]$State.databaseName) `
                -AdminUser ([string]$State.databaseAdminUser) `
                -AppUser ([string]$State.databaseAppUser)
            Assert-DeploymentCondition -Condition (
                (Get-DatabaseFingerprint `
                    -Database ([string]$State.databaseName) `
                    -AdminUser ([string]$State.databaseAdminUser)) -ceq
                    [string]$State.originalDatabaseFingerprint) `
                -Message 'Explicit database restore data fingerprint mismatch.'
            Assert-DeploymentCondition -Condition (
                (Get-FullDatabaseFingerprint `
                    -Database ([string]$State.databaseName) `
                    -AdminUser ([string]$State.databaseAdminUser)) -ceq
                    [string]$State.originalDatabaseFullFingerprint) `
                -Message 'Explicit database restore full fingerprint mismatch.'
            Assert-DeploymentCondition -Condition (
                (Get-SchemaFingerprint `
                    -Database ([string]$State.databaseName) `
                    -AdminUser ([string]$State.databaseAdminUser)) -ceq
                    [string]$State.restoredDatabaseSchemaFingerprint) `
                -Message 'Explicit database restore schema fingerprint mismatch.'
        } finally {
            try {
                [void](Invoke-NativeChecked -Operation 'Remove explicit DB archive' `
                    -Command {
                        & docker exec $script:DbContainer rm -f $containerBackup
                    })
            } catch { }
        }
    }
    if ($storageDamaged) {
        $backupStorage = [string]$State.storageBackup
        Assert-DeploymentCondition -Condition (
            (Get-DeploymentDirectoryFingerprint -Root $backupStorage) -ceq
            [string]$State.storageFingerprint) `
            -Message 'Storage recovery snapshot fingerprint is invalid.'
        $quarantine = Join-Path ([string]$State.backupRoot) `
            ('damaged-storage-' + [Guid]::NewGuid().ToString('N'))
        Move-Item -LiteralPath $script:Storage -Destination $quarantine
        Copy-Item -LiteralPath $backupStorage -Destination $script:Storage -Recurse
        Assert-DeploymentCondition -Condition (
            (Get-DeploymentDirectoryFingerprint -Root $script:Storage) -ceq
            [string]$State.storageFingerprint) `
            -Message 'Explicit storage restore fingerprint mismatch.'
    }
    return [pscustomobject]@{
        DatabaseRestored = $dbDamaged
        StorageRestored = $storageDamaged
    }
}

function Read-ApprovedState {
    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $script:StatePath -PathType Leaf) `
        -Message 'Release state file is missing.'
    Assert-DeploymentCondition -Condition (-not
        [string]::IsNullOrWhiteSpace($ExpectedStateSha256)) `
        -Message 'Rollback requires the approved state hash.'
    Assert-DeploymentCondition -Condition (
        (Get-FileHash -LiteralPath $script:StatePath -Algorithm SHA256).Hash -ceq
        $ExpectedStateSha256.ToUpperInvariant()) `
        -Message 'Release state hash is invalid.'
    $stateObject = Get-Content -LiteralPath $script:StatePath -Raw |
        ConvertFrom-Json
    Assert-DeploymentCondition -Condition (
        [int]$stateObject.protocolVersion -eq 2 -and
        [string]$stateObject.releaseId -ceq $ReleaseId) `
        -Message 'Release state identity is invalid.'
    $expectedBackupRoot = Join-Path $script:RunLogs `
        "pdf-conversion-backup-$ReleaseId"
    Assert-DeploymentCondition -Condition (
        [IO.Path]::GetFullPath([string]$stateObject.backupRoot).TrimEnd('\') `
            -ceq [IO.Path]::GetFullPath($expectedBackupRoot).TrimEnd('\')) `
        -Message 'Release state backup root is invalid.'
    Assert-DeploymentCondition -Condition (
        Test-Path -LiteralPath $expectedBackupRoot -PathType Container) `
        -Message 'Release backup directory is missing.'
    Assert-DeploymentCondition -Condition (-not
        ((Get-Item -LiteralPath $expectedBackupRoot -Force).Attributes -band
            [IO.FileAttributes]::ReparsePoint)) `
        -Message 'Release backup directory cannot be a reparse point.'
    foreach ($entry in @(
            @{ Path = [string]$stateObject.databaseBackup; Type = 'Leaf';
                Name = 'database backup' },
            @{ Path = [string]$stateObject.storageBackup; Type = 'Container';
                Name = 'storage backup' },
            @{ Path = [string]$stateObject.checksumsNext; Type = 'Leaf';
                Name = 'checksum manifest' },
            @{ Path = [string]$stateObject.rollbackEnvironment; Type = 'Leaf';
                Name = 'rollback image environment' })) {
        [void](Assert-DeploymentChildPath -Candidate $entry.Path `
            -Parent $expectedBackupRoot -Description $entry.Name)
        Assert-DeploymentCondition -Condition (
            Test-Path -LiteralPath $entry.Path -PathType $entry.Type) `
            -Message "Release state $($entry.Name) is missing."
    }
    foreach ($id in @($stateObject.originalAppImageId,
            $stateObject.preparedAppImageId, $stateObject.fileApiImageId,
            $stateObject.converterImageId)) {
        Assert-DeploymentCondition -Condition (
            [string]$id -match '^sha256:[0-9a-f]{64}$') `
            -Message 'Release state contains an invalid image id.'
    }
    foreach ($id in @($stateObject.appContainerId,
            $stateObject.gatewayContainerId, $stateObject.databaseContainerId)) {
        Assert-DeploymentCondition -Condition (
            [string]$id -match '^[0-9a-f]{64}$') `
            -Message 'Release state contains an invalid container id.'
    }
    foreach ($identifier in @($stateObject.databaseName,
            $stateObject.databaseAdminUser, $stateObject.databaseAppUser)) {
        Assert-DeploymentCondition -Condition (
            [string]$identifier -match '^[A-Za-z_][A-Za-z0-9_]*$') `
            -Message 'Release state contains an unsafe database identifier.'
    }
    Assert-DeploymentCondition -Condition (
        [string]$stateObject.originalAppImageReference -match
            '^[A-Za-z0-9][A-Za-z0-9._/-]*:[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$' -and
        [string]$stateObject.rollbackImageTag -match
            '^kt1b-dms-app:rollback-[a-z0-9][a-z0-9.-]{7,79}$') `
        -Message 'Release state contains an unsafe image reference.'
    foreach ($hash in @($stateObject.databaseBackupSha256,
            $stateObject.originalDatabaseFingerprint,
            $stateObject.originalDatabaseFullFingerprint,
            $stateObject.restoredDatabaseFullFingerprint,
            $stateObject.originalDatabaseSchemaFingerprint,
            $stateObject.archiveDatabaseSchemaFingerprint,
            $stateObject.restoredDatabaseSchemaFingerprint,
            $stateObject.databaseFingerprint,
            $stateObject.databaseSchemaFingerprint,
            $stateObject.storageFingerprint,
            $stateObject.storageMetadataFingerprint,
            $stateObject.rollbackEnvironmentSha256,
            $stateObject.checksumsNextSha256,
            $stateObject.runtimeBackupHashes.baseEnvironment,
            $stateObject.runtimeBackupHashes.baseCompose,
            $stateObject.runtimeBackupHashes.secretEnvironment,
            $stateObject.runtimeBackupHashes.war,
            $stateObject.runtimeBackupHashes.checksums)) {
        Assert-DeploymentCondition -Condition (
            [string]$hash -match '^[0-9A-F]{64}$') `
            -Message 'Release state contains an invalid integrity hash.'
    }
    Assert-DeploymentCondition -Condition (
        [string]$stateObject.databaseWalPosition -match
            '^[0-9A-F]+/[0-9A-F]+$') `
        -Message 'Release state contains an invalid database WAL position.'
    if ([bool]$stateObject.overlayExisted) {
        Assert-DeploymentCondition -Condition (
            [string]$stateObject.runtimeBackupHashes.overlay -match
                '^[0-9A-F]{64}$') `
            -Message 'Release state contains an invalid overlay backup hash.'
    }
    foreach ($entry in @(
            @{ Value = $stateObject.originalFileApi;
                Container = $script:FileApiContainer; Service = 'file-api';
                TagPrefix = 'kt1b-tdds-ft:rollback-' },
            @{ Value = $stateObject.originalConverter;
                Container = $script:ConverterContainer; Service = 'pdf-converter';
                TagPrefix = 'kt1b-e-pdf-converter:rollback-' })) {
        Assert-DeploymentCondition -Condition (
            [string]$entry.Value.containerName -ceq $entry.Container -and
            [string]$entry.Value.serviceName -ceq $entry.Service -and
            [string]$entry.Value.rollbackImageTag -ceq
                ($entry.TagPrefix + $ReleaseId)) `
            -Message 'Release state contains an invalid sidecar record.'
        if ([bool]$entry.Value.existed) {
            Assert-DeploymentCondition -Condition (
                [bool]$stateObject.overlayExisted -and
                [string]$entry.Value.containerId -match
                    '^[0-9a-f]{64}$' -and
                [string]$entry.Value.imageId -match
                    '^sha256:[0-9a-f]{64}$') `
                -Message 'Release state contains an invalid original sidecar.'
        } else {
            Assert-DeploymentCondition -Condition (
                $null -eq $entry.Value.containerId -and
                $null -eq $entry.Value.imageId) `
                -Message 'Absent original sidecar state is invalid.'
        }
    }
    Assert-DeploymentCondition -Condition (
        [int]$stateObject.outageTimeoutSeconds -eq 180) `
        -Message 'Release state has an invalid outage budget.'
    return $stateObject
}

function Invoke-PreflightMode {
    Read-ReleaseContract
    Assert-LivePreflight
    Assert-DeploymentCondition -Condition (
        [int]$script:Request.outageTimeoutSeconds -eq 180) `
        -Message 'Production outage budget must be exactly 180 seconds.'
    Write-Output 'PDF_RELEASE_RESULT=PREFLIGHT_OK'
    Write-Output 'LIVE_MUTATIONS_APPLIED=False'
}

function Invoke-ApplyMode {
    Read-ReleaseContract
    Assert-LivePreflight
    Assert-DeploymentCondition -Condition (
        [int]$script:Request.outageTimeoutSeconds -eq 180) `
        -Message 'Production outage budget must be exactly 180 seconds.'
    Assert-DeploymentCondition -Condition (-not
        (Test-Path -LiteralPath $script:StatePath)) `
        -Message 'Release state already exists; Apply is not replayable.'
    $script:Lock = [IO.File]::Open($script:LockPath,
        [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite,
        [IO.FileShare]::None)
    try {
        # All expensive or failure-prone preparation completes while the old
        # app and gateway remain healthy and user traffic remains available.
        $script:State = New-PreOutageState
        Assert-RuntimeRollbackAssets -State $script:State
        Assert-LivePreflight
        Write-Output "PRE_OUTAGE_STATE_SHA256=$($script:State.stateSha256)"

        $script:OutageStartedAt = Get-Date
        $script:ApplyDeadline = $script:OutageStartedAt.AddSeconds(90)
        $script:OutageDeadline = $script:OutageStartedAt.AddSeconds(180)
        [void](Start-OutageSupervisor -Trigger $script:ApplyDeadline `
            -Deadline $script:OutageDeadline `
            -StateSha256 ([string]$script:State.stateSha256))
        $script:OutageStarted = $true
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name $script:DbContainer) -ceq
                [string]$script:State.databaseContainerId -and
            (Get-ContainerId -Name $script:GatewayContainer) -ceq
                [string]$script:State.gatewayContainerId) `
            -Message 'A protected container changed before quiesce.'
        [void](Invoke-NativeChecked -Operation 'Stop existing gateway' `
            -Command { & docker stop --time 15 $script:GatewayContainer })
        Stop-ContainerIfPresent -Name $script:AppContainer
        foreach ($name in @($script:FileApiContainer,
                $script:ConverterContainer)) {
            Stop-ContainerIfPresent -Name $name
        }
        Assert-ContainerFullyStopped -Name $script:GatewayContainer
        Assert-ContainerFullyStopped -Name $script:AppContainer
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name $script:AppContainer) -ceq
                [string]$script:State.appContainerId -and
            (Get-ContainerId -Name $script:GatewayContainer) -ceq
                [string]$script:State.gatewayContainerId) `
            -Message 'A protected runtime container changed during quiesce.'
        foreach ($sidecar in @($script:State.originalFileApi,
                $script:State.originalConverter)) {
            if ([bool]$sidecar.existed) {
                Assert-ContainerFullyStopped `
                    -Name ([string]$sidecar.containerName)
                Assert-DeploymentCondition -Condition (
                    (Get-ContainerId -Name ([string]$sidecar.containerName)) `
                        -ceq [string]$sidecar.containerId) `
                    -Message "Sidecar identity changed during quiesce: $($sidecar.containerName)"
            } else {
                Assert-DeploymentCondition -Condition (-not
                    (Test-ContainerExists -Name ([string]$sidecar.containerName))) `
                    -Message 'An unexpected sidecar appeared during quiesce.'
            }
        }
        Assert-DatabaseContainerInvariant -State $script:State `
            -Operation 'after all writer-capable services stopped'
        Assert-BeforeDeadline -Deadline $script:ApplyDeadline `
            -Operation 'stable fingerprint gate'
        $quiescedWal = Get-DatabaseWalPosition `
            -Database ([string]$script:State.databaseName) `
            -AdminUser ([string]$script:State.databaseAdminUser)
        $quiescedDatabase = Get-DatabaseFingerprint `
            -Database ([string]$script:State.databaseName) `
            -AdminUser ([string]$script:State.databaseAdminUser)
        $quiescedDatabaseSchema = Get-SchemaFingerprint `
            -Database ([string]$script:State.databaseName) `
            -AdminUser ([string]$script:State.databaseAdminUser)
        $quiescedStorage = Get-DeploymentDirectoryFingerprint `
            -Root $script:Storage
        $quiescedStorageMetadata = Get-StorageMetadataFingerprint
        Write-Output ('QUIESCED_WAL_MATCH=' +
            ($quiescedWal -ceq [string]$script:State.databaseWalPosition))
        $quiesceEvidence = [ordered]@{
            protocolVersion = 1
            releaseId = $ReleaseId
            capturedAt = (Get-Date).ToUniversalTime().ToString('o')
            stateSha256 = [string]$script:State.stateSha256
            databaseContainerId = [string]$script:State.databaseContainerId
            databaseWalPosition = $quiescedWal
            databaseFingerprint = $quiescedDatabase
            databaseSchemaFingerprint = $quiescedDatabaseSchema
            storageFingerprint = $quiescedStorage
            storageMetadataFingerprint = $quiescedStorageMetadata
        }
        Write-DeploymentJsonAtomically -Value $quiesceEvidence `
            -Path $script:QuiesceEvidencePath
        $gateIsStable =
            $quiescedDatabase -ceq [string]$script:State.databaseFingerprint -and
            $quiescedDatabaseSchema -ceq
                [string]$script:State.databaseSchemaFingerprint -and
            $quiescedStorage -ceq [string]$script:State.storageFingerprint -and
            $quiescedStorageMetadata -ceq
                [string]$script:State.storageMetadataFingerprint
        if (-not $gateIsStable) {
            Resume-OriginalBeforeMutation -State $script:State `
                -Deadline $script:OutageDeadline
            $script:OutageStarted = $false
            throw 'Protected data changed after backup; Apply cancelled before runtime mutation.'
        }
        Assert-BeforeDeadline -Deadline $script:ApplyDeadline `
            -Operation 'stable quiesce completion'
        $script:RuntimeMutationStarted = $true

        if (-not (Test-Path -LiteralPath $script:LiveOverlayDirectory `
                -PathType Container)) {
            New-Item -ItemType Directory `
                -Path $script:LiveOverlayDirectory | Out-Null
        }
        $quarantine = Join-Path ([string]$script:State.backupRoot) `
            'release-replaced-files'
        [void](Install-DeploymentFileSafely `
            -Source $script:Artifacts['war'] -Target $script:LiveWar `
            -QuarantineDirectory $quarantine)
        [void](Install-DeploymentFileSafely `
            -Source $script:Artifacts['overlay'] -Target $script:LiveOverlay `
            -QuarantineDirectory $quarantine)
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath ([string]$script:State.checksumsNext) `
                -Algorithm SHA256).Hash -ceq
            [string]$script:State.checksumsNextSha256) `
            -Message 'Prepared checksum manifest was altered.'
        [void](Install-DeploymentFileSafely `
            -Source ([string]$script:State.checksumsNext) `
            -Target $script:Checksums -QuarantineDirectory $quarantine)
        Assert-BeforeDeadline -Deadline $script:ApplyDeadline `
            -Operation 'runtime file installation'

        [void](Invoke-Compose -Overlay $script:LiveOverlay -Arguments @(
            'up', '-d', '--no-build', '--pull', 'never',
            '--force-recreate', '--no-deps',
            'app', 'file-api', 'pdf-converter') `
            -Operation 'Jointly start app and private sidecars')
        foreach ($name in @($script:AppContainer, $script:FileApiContainer,
                $script:ConverterContainer)) {
            Wait-Healthy -Name $name -Deadline $script:ApplyDeadline
        }
        Assert-BeforeDeadline -Deadline $script:ApplyDeadline `
            -Operation 'joint runtime health'

        Assert-DeploymentCondition -Condition (
            (Get-ImageId -Image ([string]$script:Request.images.app)) -ceq
            [string]$script:State.preparedAppImageId) `
            -Message 'Prepared app image tag changed before start.'
        foreach ($entry in @(
                @{ Name = $script:AppContainer; Id = [string]$script:State.preparedAppImageId },
                @{ Name = $script:FileApiContainer; Id = [string]$script:State.fileApiImageId },
                @{ Name = $script:ConverterContainer; Id = [string]$script:State.converterImageId })) {
            $id = (@(Invoke-NativeChecked -Operation "Inspect image for $($entry.Name)" `
                -Command { & docker inspect --format '{{.Image}}' $entry.Name }))[0].ToString().Trim()
            Assert-DeploymentCondition -Condition ($id -ceq $entry.Id) `
                -Message "Container runs an unexpected image: $($entry.Name)"
        }
        $appInspect = (@(Invoke-NativeChecked -Operation 'Inspect app runtime' `
            -Command { & docker inspect --format '{{json .}}' $script:AppContainer }))[0]
        $fileInspect = (@(Invoke-NativeChecked -Operation 'Inspect File API runtime' `
            -Command { & docker inspect --format '{{json .}}' $script:FileApiContainer }))[0]
        $converterInspect = (@(Invoke-NativeChecked `
            -Operation 'Inspect converter runtime' -Command {
                & docker inspect --format '{{json .}}' $script:ConverterContainer
            }))[0]
        Assert-SharedNetworkRuntimeContract `
            -AppInspectJson ([string]$appInspect) `
            -FileApiInspectJson ([string]$fileInspect) `
            -ConverterInspectJson ([string]$converterInspect)
        $appObject = ([string]$appInspect | ConvertFrom-Json)
        $fileObject = ([string]$fileInspect | ConvertFrom-Json)
        $converterObject = ([string]$converterInspect | ConvertFrom-Json)
        Assert-DeploymentCondition -Condition (
            [string]$appObject.NetworkSettings.SandboxKey -ceq
                [string]$fileObject.NetworkSettings.SandboxKey -and
            [string]$appObject.NetworkSettings.SandboxKey -ceq
                [string]$converterObject.NetworkSettings.SandboxKey) `
            -Message 'Private sidecars do not share the app sandbox.'
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name $script:DbContainer) -ceq
            [string]$script:State.databaseContainerId) `
            -Message 'Database container was replaced.'
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name $script:GatewayContainer) -ceq
            [string]$script:State.gatewayContainerId) `
            -Message 'Gateway container was replaced.'

        foreach ($probe in @(
                @{ Url = 'http://127.0.0.1:18080/api/v1/health'; Status = '200'; Name = 'File API' },
                @{ Url = 'http://127.0.0.1:9001/api/integrations/tdms/v1/convert'; Status = '405'; Name = 'converter' })) {
            $status = @(Invoke-NativeChecked -Operation "Probe private $($probe.Name)" `
                -Command {
                    & docker exec $script:AppContainer curl --silent `
                        --show-error --connect-timeout 5 --max-time 15 `
                        --output /dev/null --write-out '%{http_code}' `
                        $probe.Url
                })
            Assert-DeploymentCondition -Condition ($status.Count -eq 1 -and
                ([string]$status[0]).Trim() -ceq $probe.Status) `
                -Message "Private $($probe.Name) probe failed."
        }
        Assert-BeforeDeadline -Deadline $script:ApplyDeadline `
            -Operation 'private integration probes'

        # The gateway is never recreated. Its original container is started.
        [void](Invoke-NativeChecked -Operation 'Start existing gateway' `
            -Command { & docker start $script:GatewayContainer })
        Wait-Healthy -Name $script:GatewayContainer `
            -Deadline $script:ApplyDeadline
        Assert-DeploymentCondition -Condition (
            (Get-ContainerId -Name $script:GatewayContainer) -ceq
            [string]$script:State.gatewayContainerId) `
            -Message 'Gateway identity changed during Apply.'
        Assert-DatabaseContainerInvariant -State $script:State `
            -Operation 'after successful Apply'
        Assert-BeforeDeadline -Deadline $script:ApplyDeadline `
            -Operation 'existing gateway start'
        $script:GatewayHealthyAt = Get-Date
        Write-OutageCompletion -Outcome 'APPLIED'
        $script:OutageStarted = $false

        $result = [ordered]@{
            protocolVersion = 2
            state = 'APPLIED'
            releaseId = $ReleaseId
            completedAt = (Get-Date).ToUniversalTime().ToString('o')
            statePath = $script:StatePath
            stateSha256 = [string]$script:State.stateSha256
            outageStartedAt = $script:OutageStartedAt.ToUniversalTime().ToString('o')
            applyDeadline = $script:ApplyDeadline.ToUniversalTime().ToString('o')
            gatewayHealthyAt = $script:GatewayHealthyAt.ToUniversalTime().ToString('o')
            outageSeconds = [int]((Get-Date) - $script:OutageStartedAt).TotalSeconds
            gatewayContainerPreserved = $true
            databaseContainerPreserved = $true
            publishedPrivatePorts = 0
            databaseRestored = $false
            storageRestored = $false
        }
        Write-DeploymentJsonAtomically -Value $result `
            -Path $script:ResultPath
        Write-Output 'PDF_RELEASE_RESULT=APPLIED'
        Write-Output "STATE_SHA256=$($script:State.stateSha256)"
    } catch {
        Write-Output ("PDF_RELEASE_ERROR={0}" -f $_.Exception.Message)
        if ($script:OutageStarted -and $null -ne $script:State) {
            try {
                if ($script:RuntimeMutationStarted) {
                    Invoke-RuntimeRollback -State $script:State `
                        -Deadline $script:OutageDeadline
                    Write-OutageCompletion -Outcome `
                        'AUTOMATIC_RUNTIME_ROLLBACK'
                    Write-Output 'AUTOMATIC_ROLLBACK=RUNTIME_ONLY_SUCCESS'
                } else {
                    Resume-OriginalBeforeMutation -State $script:State `
                        -Deadline $script:OutageDeadline
                    Write-Output 'AUTOMATIC_ROLLBACK=UNCHANGED_RUNTIME_RESUMED'
                }
                $script:OutageStarted = $false
                Write-Output 'DATABASE_RESTORED=False'
                Write-Output 'STORAGE_RESTORED=False'
            } catch {
                Write-Output ("AUTOMATIC_ROLLBACK_ERROR={0}" -f
                    $_.Exception.Message)
                Write-Output 'AUTOMATIC_ROLLBACK=FAILED_GATEWAY_REMAINS_CLOSED'
            }
        } else {
            Write-Output 'AUTOMATIC_ROLLBACK=NOT_REQUIRED_PRE_OUTAGE'
        }
        throw
    } finally {
        if ($script:Lock) {
            $script:Lock.Dispose()
            $script:Lock = $null
        }
    }
}

function Invoke-RollbackMode {
    Read-ReleaseContract
    $state = Read-ApprovedState
    $deadline = if ([string]::IsNullOrWhiteSpace($RecoveryDeadlineUtc)) {
        (Get-Date).AddSeconds(180)
    } else {
        [DateTime]::Parse($RecoveryDeadlineUtc,
            [Globalization.CultureInfo]::InvariantCulture,
            [Globalization.DateTimeStyles]::AssumeUniversal).ToUniversalTime()
    }
    Assert-BeforeDeadline -Deadline $deadline -Operation 'rollback start'
    $script:OutageStarted = $true
    $script:OutageStartedAt = Get-Date
    $script:OutageDeadline = $deadline
    Assert-DeploymentCondition -Condition (-not
        (Test-Path -LiteralPath $script:ResultPath)) `
        -Message 'This release rollback result already exists.'
    $script:Lock = [IO.File]::Open($script:LockPath,
        [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite,
        [IO.FileShare]::None)
    try {
        Assert-RuntimeRollbackAssets -State $state
        Assert-DatabaseContainerInvariant -State $state `
            -Operation 'before explicit rollback'
        if ($RestoreData) {
            [void](Assert-AndRestoreDamagedData -State $state -ValidateOnly)
        }
        $dataRestore = [pscustomobject]@{
            DatabaseRestored = $false
            StorageRestored = $false
        }
        if ($RestoreData) {
            $dataRestore = Assert-AndRestoreDamagedData -State $state
            Assert-DatabaseContainerInvariant -State $state `
                -Operation 'after explicit data restore'
        }
        Invoke-RuntimeRollback -State $state -Deadline $deadline
        $result = [ordered]@{
            protocolVersion = 2
            state = if ($RestoreData) {
                'ROLLED_BACK_WITH_VERIFIED_DATA_RESTORE'
            } else {
                'ROLLED_BACK_RUNTIME_ONLY'
            }
            releaseId = $ReleaseId
            completedAt = (Get-Date).ToUniversalTime().ToString('o')
            databaseRestored = [bool]$dataRestore.DatabaseRestored
            storageRestored = [bool]$dataRestore.StorageRestored
            dataRestoreAuthorized = [bool]$RestoreData
            gatewayContainerPreserved = $true
            databaseContainerPreserved = $true
            outageStartedAt = $script:OutageStartedAt.ToUniversalTime().ToString('o')
            rollbackStartedAt = $script:RollbackStartedAt.ToUniversalTime().ToString('o')
            gatewayHealthyAt = $script:GatewayHealthyAt.ToUniversalTime().ToString('o')
        }
        Write-DeploymentJsonAtomically -Value $result `
            -Path $script:ResultPath
        Write-Output (if ($RestoreData) {
            'PDF_RELEASE_RESULT=ROLLBACK_WITH_VERIFIED_DATA_RESTORE'
        } else {
            'PDF_RELEASE_RESULT=ROLLBACK_RUNTIME_ONLY'
        })
        Write-OutageCompletion -Outcome ([string]$result.state)
        $script:OutageStarted = $false
    } finally {
        if ($script:Lock) {
            $script:Lock.Dispose()
            $script:Lock = $null
        }
    }
}

try {
    if ($OutageSupervisor) {
        Invoke-OutageSupervisorMode
    } else {
        switch ($Mode) {
            'Preflight' { Invoke-PreflightMode }
            'Apply' { Invoke-ApplyMode }
            'Rollback' { Invoke-RollbackMode }
        }
    }
} finally {
    foreach ($stream in $script:ArtifactLocks) { $stream.Dispose() }
    $script:ArtifactLocks.Clear()
}
