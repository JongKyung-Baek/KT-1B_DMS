[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('Apply', 'Restore')]
    [string]$Mode,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[a-z0-9][a-z0-9.-]{7,79}$')]
    [string]$ReleaseId,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedSelfSha256,

    [ValidatePattern('^[0-9A-Fa-f]{64}$')]
    [string]$ExpectedBackupSha256
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$selfPath = [IO.Path]::GetFullPath($PSCommandPath)
$selfLock = [IO.File]::Open($selfPath, [IO.FileMode]::Open,
    [IO.FileAccess]::Read, [IO.FileShare]::Read)
$hasher = [Security.Cryptography.SHA256]::Create()
try {
    $selfHash = (($hasher.ComputeHash($selfLock) |
            ForEach-Object { $_.ToString('X2') }) -join '')
    $selfLock.Position = 0
} finally {
    $hasher.Dispose()
}
if ($selfHash -cne $ExpectedSelfSha256.ToUpperInvariant()) {
    $selfLock.Dispose()
    throw 'Bind ACL helper SHA-256 differs from the pinned command value.'
}

$root = 'D:\KT1B-DMS'
$staging = Join-Path $root 'staging'
$backupRoot = "D:\KT1B-DMS-bind-acl-backup-$ReleaseId"
$backupJson = Join-Path $backupRoot 'acl-before.json'
$backupHashFile = Join-Path $backupRoot 'acl-before.sha256'
$backupComplete = Join-Path $backupRoot 'BACKUP_COMPLETE'
$adminSid = [Security.Principal.SecurityIdentifier]::new('S-1-5-32-544')
$systemSid = [Security.Principal.SecurityIdentifier]::new('S-1-5-18')
$currentSid = [Security.Principal.WindowsIdentity]::GetCurrent().User
$sections = [Security.AccessControl.AccessControlSections]::Access -bor
    [Security.AccessControl.AccessControlSections]::Owner -bor
    [Security.AccessControl.AccessControlSections]::Group

function Assert-Elevated {
    $principal = [Security.Principal.WindowsPrincipal]::new(
        [Security.Principal.WindowsIdentity]::GetCurrent())
    if (-not $principal.IsInRole(
            [Security.Principal.WindowsBuiltInRole]::Administrator)) {
        throw 'Bind ACL preparation requires an elevated Administrator token.'
    }
}

function Assert-PhysicalItem {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][ValidateSet('Container', 'Leaf')]
        [string]$Type
    )
    $expected = [IO.Path]::GetFullPath($Path).TrimEnd('\')
    if (-not (Test-Path -LiteralPath $expected -PathType $Type)) {
        throw "Required bind ACL target is missing: $expected"
    }
    $item = Get-Item -LiteralPath $expected -Force
    if ([IO.Path]::GetFullPath($item.FullName).TrimEnd('\') -cne $expected) {
        throw "Bind ACL target canonical path differs: $expected"
    }
    if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw "Bind ACL target is a reparse point: $expected"
    }
    return $item
}

function Assert-TrustedPathChain {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][ValidateSet('Container', 'Leaf')]
        [string]$Type
    )
    $expected = [IO.Path]::GetFullPath($Path).TrimEnd('\')
    $rootFull = [IO.Path]::GetFullPath($root).TrimEnd('\')
    if ($expected -cne $rootFull -and
            -not $expected.StartsWith($rootFull + '\',
                [StringComparison]::OrdinalIgnoreCase)) {
        throw "Bind ACL target escapes the deployment root: $expected"
    }
    $relative = $expected.Substring($rootFull.Length).TrimStart('\')
    $components = [Collections.Generic.List[string]]::new()
    $components.Add($rootFull)
    $cursor = $rootFull
    foreach ($segment in @($relative.Split('\') |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) })) {
        $cursor = Join-Path $cursor $segment
        $components.Add($cursor)
    }
    $trustedOwners = @($adminSid.Value, $systemSid.Value, $currentSid.Value)
    foreach ($component in $components) {
        $item = Get-Item -LiteralPath $component -Force -ErrorAction Stop
        if ([IO.Path]::GetFullPath($item.FullName).TrimEnd('\') -cne
                [IO.Path]::GetFullPath($component).TrimEnd('\')) {
            throw "Bind ACL path component canonical path differs: $component"
        }
        if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw "Bind ACL path component is a reparse point: $component"
        }
        $owner = (Get-Acl -LiteralPath $component).
            GetOwner([Security.Principal.SecurityIdentifier]).Value
        if ($trustedOwners -cnotcontains $owner) {
            throw "Bind ACL path component owner is not trusted: $component"
        }
    }
    return Assert-PhysicalItem -Path $expected -Type $Type
}

function Assert-TrustedDeploymentDirectory {
    param([Parameter(Mandatory = $true)][string]$Path)
    [void](Assert-PhysicalItem -Path $Path -Type Container)
    $acl = Get-Acl -LiteralPath $Path
    if (-not $acl.AreAccessRulesProtected) {
        throw "Deployment directory inheritance is enabled: $Path"
    }
    if ($acl.GetOwner([Security.Principal.SecurityIdentifier]).Value -cne
            $adminSid.Value) {
        throw "Deployment directory owner is not Administrators: $Path"
    }
    $rules = @($acl.GetAccessRules(
            $true, $true, [Security.Principal.SecurityIdentifier]))
    if ($rules.Count -ne 2) {
        throw "Deployment directory ACL rule count differs: $Path"
    }
    foreach ($sid in @($systemSid.Value, $adminSid.Value)) {
        $matching = @($rules | Where-Object {
                $_.IdentityReference.Value -ceq $sid -and
                $_.AccessControlType -eq
                    [Security.AccessControl.AccessControlType]::Allow -and
                $_.FileSystemRights -eq
                    [Security.AccessControl.FileSystemRights]::FullControl -and
                $_.InheritanceFlags -eq (
                    [Security.AccessControl.InheritanceFlags]::ContainerInherit -bor
                    [Security.AccessControl.InheritanceFlags]::ObjectInherit) -and
                $_.PropagationFlags -eq
                    [Security.AccessControl.PropagationFlags]::None -and
                -not $_.IsInherited
            })
        if ($matching.Count -ne 1) {
            throw "Deployment directory trusted ACE differs: $Path"
        }
    }
}

function New-TrustedDirectoryAcl {
    $acl = [Security.AccessControl.DirectorySecurity]::new()
    $acl.SetAccessRuleProtection($true, $false)
    $acl.SetOwner($adminSid)
    $flags = [Security.AccessControl.InheritanceFlags]::ContainerInherit -bor
        [Security.AccessControl.InheritanceFlags]::ObjectInherit
    foreach ($sid in @($systemSid, $adminSid)) {
        [void]$acl.AddAccessRule(
            [Security.AccessControl.FileSystemAccessRule]::new(
                $sid,
                [Security.AccessControl.FileSystemRights]::FullControl,
                $flags,
                [Security.AccessControl.PropagationFlags]::None,
                [Security.AccessControl.AccessControlType]::Allow))
    }
    return $acl
}

function Get-State {
    param($Target)
    $item = Assert-TrustedPathChain -Path ([string]$Target.Path) `
        -Type ([string]$Target.Type)
    $acl = Get-Acl -LiteralPath $item.FullName
    return [ordered]@{
        path = [IO.Path]::GetFullPath($item.FullName).TrimEnd('\')
        type = [string]$Target.Type
        rights = [string]$Target.Rights
        sddl = $acl.GetSecurityDescriptorSddlForm($sections)
    }
}

function Get-TargetAclFingerprint {
    $lines = [Collections.Generic.List[string]]::new()
    foreach ($target in $targets) {
        $state = Get-State -Target $target
        [void]$lines.Add(([string]$state.path).ToUpperInvariant() + '|' +
            [string]$state.sddl)
    }
    $payload = ((@($lines.ToArray() | Sort-Object) -join "`n") + "`n")
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.UTF8Encoding]::new($false).GetBytes($payload)
        return (($algorithm.ComputeHash($bytes) |
                ForEach-Object { $_.ToString('X2') }) -join '')
    } finally {
        $algorithm.Dispose()
    }
}

function Set-ExactDeploymentAccountRule {
    param($Target)
    $path = [string]$Target.Path
    $type = [string]$Target.Type
    $rights = [Security.AccessControl.FileSystemRights]$Target.Rights
    [void](Assert-TrustedPathChain -Path $path -Type $type)
    $acl = Get-Acl -LiteralPath $path
    foreach ($rule in @($acl.GetAccessRules(
                $true, $false, [Security.Principal.SecurityIdentifier]) |
            Where-Object {
                $_.IdentityReference.Value -ceq $currentSid.Value -and
                $_.AccessControlType -eq
                    [Security.AccessControl.AccessControlType]::Allow
            })) {
        [void]$acl.RemoveAccessRuleSpecific($rule)
    }
    $inheritance = [Security.AccessControl.InheritanceFlags]::None
    $inheritChildren = ($type -eq 'Container')
    if ($Target.Contains('ThisFolderOnly') -and
            [bool]$Target.ThisFolderOnly) {
        $inheritChildren = $false
    }
    if ($inheritChildren) {
        $inheritance = [Security.AccessControl.InheritanceFlags]::ContainerInherit -bor
            [Security.AccessControl.InheritanceFlags]::ObjectInherit
    }
    [void]$acl.AddAccessRule(
        [Security.AccessControl.FileSystemAccessRule]::new(
            $currentSid, $rights, $inheritance,
            [Security.AccessControl.PropagationFlags]::None,
            [Security.AccessControl.AccessControlType]::Allow))
    Set-Acl -LiteralPath $path -AclObject $acl

    $readback = Get-Acl -LiteralPath $path
    $accountRules = @($readback.GetAccessRules(
            $true, $false, [Security.Principal.SecurityIdentifier]) |
        Where-Object {
            $_.IdentityReference.Value -ceq $currentSid.Value -and
            -not $_.IsInherited
        })
    $allowedMasks = @(
        [int64]$rights,
        ([int64]$rights -bor
            [int64][Security.AccessControl.FileSystemRights]::Synchronize)
    ) | Select-Object -Unique
    if ($accountRules.Count -ne 1 -or
            $accountRules[0].AccessControlType -ne
                [Security.AccessControl.AccessControlType]::Allow -or
            $accountRules[0].InheritanceFlags -ne $inheritance -or
            $accountRules[0].PropagationFlags -ne
                [Security.AccessControl.PropagationFlags]::None -or
            $allowedMasks -cnotcontains
                [int64]$accountRules[0].FileSystemRights) {
        throw "Bind ACL readback differs from the minimum contract: $path"
    }
}

function Restore-Record {
    param($Record)
    [void](Assert-TrustedPathChain -Path ([string]$Record.path) `
        -Type ([string]$Record.type))
    $security = if ([string]$Record.type -eq 'Container') {
        [Security.AccessControl.DirectorySecurity]::new()
    } else {
        [Security.AccessControl.FileSecurity]::new()
    }
    $security.SetSecurityDescriptorSddlForm([string]$Record.sddl, $sections)
    Set-Acl -LiteralPath ([string]$Record.path) -AclObject $security
    $readback = (Get-Acl -LiteralPath ([string]$Record.path)).
        GetSecurityDescriptorSddlForm($sections)
    if ($readback -cne [string]$Record.sddl) {
        throw "Bind ACL restore readback differs: $($Record.path)"
    }
}

$targets = @(
    [ordered]@{ Path = Join-Path $root 'database'; Type = 'Container';
        Rights = 'ReadAndExecute' },
    [ordered]@{ Path = Join-Path $root 'certs'; Type = 'Container';
        Rights = 'ReadAndExecute' },
    [ordered]@{ Path = Join-Path $root 'runtime'; Type = 'Container';
        Rights = 'ReadAndExecute'; ThisFolderOnly = $true },
    [ordered]@{ Path = Join-Path $root 'runtime\nginx.conf'; Type = 'Leaf';
        Rights = 'Read' },
    [ordered]@{ Path = Join-Path $root 'truststore'; Type = 'Container';
        Rights = 'Modify' },
    [ordered]@{ Path = Join-Path $root 'storage'; Type = 'Container';
        Rights = 'Modify' },
    [ordered]@{ Path = Join-Path $root 'logs'; Type = 'Container';
        Rights = 'Modify' },
    [ordered]@{ Path = Join-Path $root 'viewer-work'; Type = 'Container';
        Rights = 'Modify' }
)

Assert-Elevated
Assert-TrustedDeploymentDirectory -Path $root
Assert-TrustedDeploymentDirectory -Path $staging

try {
    if ($Mode -eq 'Apply') {
        if (Test-Path -LiteralPath $backupRoot) {
            throw 'Bind ACL backup already exists; choose a new release id.'
        }
        $records = [Collections.Generic.List[object]]::new()
        foreach ($target in $targets) {
            $records.Add((Get-State -Target $target))
        }

        [void][IO.Directory]::CreateDirectory(
            $backupRoot, (New-TrustedDirectoryAcl))
        Assert-TrustedDeploymentDirectory -Path $backupRoot
        $jsonPathPartial = $backupJson + '.partial'
        $payload = [ordered]@{
            protocolVersion = 1
            releaseId = $ReleaseId
            deploymentAccountSid = $currentSid.Value
            capturedAt = (Get-Date).ToUniversalTime().ToString('o')
            targets = $records.ToArray()
        } | ConvertTo-Json -Depth 8
        [IO.File]::WriteAllText($jsonPathPartial, $payload,
            [Text.UTF8Encoding]::new($false))
        Move-Item -LiteralPath $jsonPathPartial -Destination $backupJson
        $backupHash = (Get-FileHash -LiteralPath $backupJson `
            -Algorithm SHA256).Hash
        [IO.File]::WriteAllText($backupHashFile + '.partial',
            $backupHash + "`r`n", [Text.Encoding]::ASCII)
        Move-Item -LiteralPath ($backupHashFile + '.partial') `
            -Destination $backupHashFile
        [IO.File]::WriteAllText($backupComplete + '.partial',
            "COMPLETE`r`n", [Text.Encoding]::ASCII)
        Move-Item -LiteralPath ($backupComplete + '.partial') `
            -Destination $backupComplete

        $attempted = [Collections.Generic.List[object]]::new()
        try {
            foreach ($target in $targets) {
                $attempted.Add($target)
                Set-ExactDeploymentAccountRule -Target $target
            }
            Assert-TrustedDeploymentDirectory -Path $root
            Assert-TrustedDeploymentDirectory -Path $staging
            Write-Output 'BIND_ACL_RESULT=APPLIED'
            Write-Output "BIND_ACL_BACKUP_ROOT=$backupRoot"
            Write-Output "BIND_ACL_BACKUP_SHA256=$backupHash"
            Write-Output "BIND_ACL_TARGET_COUNT=$($targets.Count)"
            Write-Output ("BIND_ACL_APPLIED_FINGERPRINT_SHA256=" +
                (Get-TargetAclFingerprint))
        } catch {
            $rollbackOk = $true
            $restoreRecords = @($records.ToArray())
            [array]::Reverse($restoreRecords)
            foreach ($record in $restoreRecords) {
                try { Restore-Record -Record $record } catch { $rollbackOk = $false }
            }
            if ($rollbackOk) {
                Write-Output 'BIND_ACL_RESULT=ROLLED_BACK'
            } else {
                Write-Output 'BIND_ACL_RESULT=ROLLBACK_FAILED'
            }
            throw
        }
    } else {
        if ([string]::IsNullOrWhiteSpace($ExpectedBackupSha256)) {
            throw 'Restore requires ExpectedBackupSha256.'
        }
        foreach ($path in @($backupJson, $backupHashFile, $backupComplete)) {
            [void](Assert-PhysicalItem -Path $path -Type Leaf)
        }
        Assert-TrustedDeploymentDirectory -Path $backupRoot
        $actualHash = (Get-FileHash -LiteralPath $backupJson `
            -Algorithm SHA256).Hash
        if ($actualHash -cne $ExpectedBackupSha256.ToUpperInvariant() -or
                $actualHash -cne (Get-Content -LiteralPath $backupHashFile `
                    -Raw).Trim().ToUpperInvariant()) {
            throw 'Bind ACL backup SHA-256 differs from the pinned value.'
        }
        $payload = Get-Content -LiteralPath $backupJson -Raw | ConvertFrom-Json
        if ([string]$payload.releaseId -cne $ReleaseId -or
                [string]$payload.deploymentAccountSid -cne $currentSid.Value -or
                @($payload.targets).Count -ne $targets.Count) {
            throw 'Bind ACL backup identity differs from the restore contract.'
        }
        $records = @($payload.targets)
        foreach ($target in $targets) {
            $matches = @($records | Where-Object {
                    [string]$_.path -ceq
                        [IO.Path]::GetFullPath([string]$target.Path).TrimEnd('\') -and
                    [string]$_.type -ceq [string]$target.Type
                })
            if ($matches.Count -ne 1) {
                throw 'Bind ACL backup target set differs from the restore contract.'
            }
        }
        $appliedStates = [Collections.Generic.List[object]]::new()
        foreach ($target in $targets) {
            $appliedStates.Add((Get-State -Target $target))
        }
        $restoreRecords = @($records)
        [array]::Reverse($restoreRecords)
        $restoreFailed = $false
        try {
            foreach ($record in $restoreRecords) {
                Restore-Record -Record $record
            }
        } catch {
            $restoreFailed = $true
            $primaryError = $_
        }
        if ($restoreFailed) {
            $rollbackOk = $true
            foreach ($state in @($appliedStates.ToArray())) {
                try { Restore-Record -Record $state } catch { $rollbackOk = $false }
            }
            if ($rollbackOk) {
                Write-Output 'BIND_ACL_RESTORE_RESULT=ROLLED_BACK_TO_APPLIED'
            } else {
                Write-Output 'BIND_ACL_RESTORE_RESULT=ROLLBACK_FAILED'
            }
            throw $primaryError
        }
        Assert-TrustedDeploymentDirectory -Path $root
        Assert-TrustedDeploymentDirectory -Path $staging
        Write-Output 'BIND_ACL_RESULT=RESTORED'
        Write-Output "BIND_ACL_TARGET_COUNT=$($targets.Count)"
        Write-Output ("BIND_ACL_RESTORED_FINGERPRINT_SHA256=" +
            (Get-TargetAclFingerprint))
    }
} finally {
    $selfLock.Dispose()
}
