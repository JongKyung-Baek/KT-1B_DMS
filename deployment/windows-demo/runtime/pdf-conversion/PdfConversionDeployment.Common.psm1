Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Assert-DeploymentCondition {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [bool]$Condition,

        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Get-CanonicalWindowsPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$Path
    )

    # Resolve-Path fails for a valid destination that has not been created yet.
    # GetFullPath is available in Windows PowerShell 5.1 and is deliberately
    # used for both existing and future deployment paths.
    $fullPath = [IO.Path]::GetFullPath($Path)
    $root = [IO.Path]::GetPathRoot($fullPath)
    if ($fullPath.Length -gt $root.Length) {
        return $fullPath.TrimEnd('\')
    }
    return $fullPath
}

function Get-DeploymentStringSha256 {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [AllowEmptyString()]
        [string]$Text
    )

    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.UTF8Encoding]::new($false).GetBytes($Text)
        return (($algorithm.ComputeHash($bytes) |
                ForEach-Object { $_.ToString('X2') }) -join '')
    } finally {
        $algorithm.Dispose()
    }
}

function ConvertTo-CanonicalGatewayBindSource {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$Source
    )

    $candidate = $Source
    $desktop = [regex]::Match($Source,
        '^/(?:run/desktop/mnt/host|host_mnt)/(?<drive>[A-Za-z])/(?<tail>.+)$')
    if ($desktop.Success) {
        $candidate = $desktop.Groups['drive'].Value.ToUpperInvariant() + ':\' +
            $desktop.Groups['tail'].Value.Replace('/', '\')
    } elseif ($Source -notmatch '^[A-Za-z]:[\\/]') {
        throw 'Gateway bind source is not a canonical Windows drive path.'
    }
    return (Get-CanonicalWindowsPath -Path $candidate.Replace('/', '\')).
        ToUpperInvariant()
}

function Get-CanonicalGatewayBindContract {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$InspectMountsJson,

        [Parameter(Mandatory = $true)]
        [string]$ComposeConfigJson,

        [Parameter(Mandatory = $true)]
        [string]$DeploymentRoot,

        [Parameter(Mandatory = $true)]
        [string]$RuntimeRoot
    )

    $root = Get-CanonicalWindowsPath -Path $DeploymentRoot
    $runtime = Get-CanonicalWindowsPath -Path $RuntimeRoot
    Assert-DeploymentCondition -Condition ($runtime.Equals(
            (Get-CanonicalWindowsPath -Path (
                [IO.Path]::Combine($root, 'runtime'))),
            [StringComparison]::OrdinalIgnoreCase)) `
        -Message 'Gateway runtime root differs from the fixed deployment layout.'

    $expected = @(
        [pscustomobject]@{
            Source = Get-CanonicalWindowsPath -Path (
                [IO.Path]::Combine($runtime, 'nginx.conf'))
            Destination = '/etc/nginx/nginx.conf'
        },
        [pscustomobject]@{
            Source = Get-CanonicalWindowsPath -Path 'D:\CollabView\certs\key.pem'
            Destination = '/etc/nginx/collabview-certs/key.pem'
        },
        [pscustomobject]@{
            Source = Get-CanonicalWindowsPath -Path (
                [IO.Path]::Combine($root, 'certs\key.pass'))
            Destination = '/run/secrets/tls_key_passphrase'
        },
        [pscustomobject]@{
            Source = Get-CanonicalWindowsPath -Path (
                [IO.Path]::Combine($root, 'certs'))
            Destination = '/etc/nginx/kt1b-certs'
        }
    )

    try {
        [object[]]$inspectMounts = ($InspectMountsJson | ConvertFrom-Json)
        $compose = $ComposeConfigJson | ConvertFrom-Json
    } catch {
        throw 'Gateway bind evidence is not valid JSON.'
    }
    $gatewayProperty = $compose.services.PSObject.Properties['gateway']
    Assert-DeploymentCondition -Condition ($null -ne $gatewayProperty) `
        -Message 'Compose config does not contain the gateway service.'
    $composeMounts = @($gatewayProperty.Value.volumes)
    Assert-DeploymentCondition -Condition (
        $inspectMounts.Count -eq $expected.Count -and
        $composeMounts.Count -eq $expected.Count) `
        -Message 'Gateway bind count differs from the exact four-mount allowlist.'

    $canonical = [Collections.Generic.List[string]]::new()
    foreach ($entry in $expected) {
        $source = (Get-CanonicalWindowsPath -Path ([string]$entry.Source)).
            ToUpperInvariant()
        $destination = [string]$entry.Destination
        $inspectMatches = @($inspectMounts | Where-Object {
                [string]$_.Type -ceq 'bind' -and
                -not [bool]$_.RW -and
                [string]$_.Destination -ceq $destination -and
                (ConvertTo-CanonicalGatewayBindSource `
                    -Source ([string]$_.Source)) -ceq $source
            })
        $composeMatches = @($composeMounts | Where-Object {
                [string]$_.type -ceq 'bind' -and
                [bool]$_.read_only -and
                [string]$_.target -ceq $destination -and
                (ConvertTo-CanonicalGatewayBindSource `
                    -Source ([string]$_.source)) -ceq $source
            })
        Assert-DeploymentCondition -Condition (
            $inspectMatches.Count -eq 1 -and $composeMatches.Count -eq 1) `
            -Message "Gateway bind contract differs at $destination."
        [void]$canonical.Add("$destination|$source|RO")
    }
    $lines = @($canonical.ToArray() | Sort-Object)
    $text = ($lines -join "`n") + "`n"
    return [pscustomobject]@{
        Fingerprint = Get-DeploymentStringSha256 -Text $text
        Lines = $lines
        Mounts = $expected
    }
}

function Get-GatewayCanaryDockerArguments {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidatePattern('^[a-z0-9][a-z0-9_.-]{7,127}$')]
        [string]$Name,

        [Parameter(Mandatory = $true)]
        [ValidatePattern('^sha256:[0-9a-f]{64}$')]
        [string]$ImageId,

        [Parameter(Mandatory = $true)]
        [ValidatePattern('^[0-9a-f]{32}$')]
        [string]$OwnershipToken,

        [Parameter(Mandatory = $true)]
        [ValidateNotNullOrEmpty()]
        [string]$CidFile,

        [Parameter(Mandatory = $true)]
        [object]$BindContract
    )

    Assert-DeploymentCondition -Condition (@($BindContract.Mounts).Count -eq 4) `
        -Message 'Gateway canary requires the exact four-mount bind contract.'
    $arguments = [Collections.Generic.List[string]]::new()
    foreach ($argument in @('create', '--name', $Name, '--pull', 'never',
            '--cidfile', $CidFile,
            '--label', "com.esob.tdms.pdfconv.canary=$OwnershipToken",
            '--network', 'none', '--read-only', '--cap-drop', 'ALL',
            '--security-opt', 'no-new-privileges', '--add-host',
            'app:127.0.0.1', '--entrypoint', 'nginx')) {
        [void]$arguments.Add([string]$argument)
    }
    foreach ($mount in @($BindContract.Mounts)) {
        [void]$arguments.Add('--mount')
        [void]$arguments.Add(('type=bind,source={0},target={1},readonly' -f
                [string]$mount.Source, [string]$mount.Destination))
    }
    [void]$arguments.Add($ImageId)
    [void]$arguments.Add('-t')
    [void]$arguments.Add('-c')
    [void]$arguments.Add('/etc/nginx/nginx.conf')
    return $arguments.ToArray()
}

function Assert-DeploymentChildPath {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Candidate,

        [Parameter(Mandatory = $true)]
        [string]$Parent,

        [Parameter(Mandatory = $true)]
        [string]$Description
    )

    $candidateFull = Get-CanonicalWindowsPath -Path $Candidate
    $parentFull = (Get-CanonicalWindowsPath -Path $Parent).TrimEnd('\') + '\'
    Assert-DeploymentCondition `
        -Condition $candidateFull.StartsWith(
            $parentFull, [StringComparison]::OrdinalIgnoreCase) `
        -Message "$Description escaped $parentFull"
    return $candidateFull
}

function Test-ContainerNameInDockerList {
    [CmdletBinding()]
    param(
        [AllowEmptyCollection()]
        [string[]]$Names,

        [Parameter(Mandatory = $true)]
        [string]$ExpectedName
    )

    return @($Names | ForEach-Object { ([string]$_).Trim() } |
        Where-Object { $_ -ceq $ExpectedName }).Count -eq 1
}

function Test-PgRestoreTableDataEntry {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$CatalogText,

        [Parameter(Mandatory = $true)]
        [ValidatePattern('^[a-z_][a-z0-9_]*$')]
        [string]$TableName,

        [ValidatePattern('^[a-z_][a-z0-9_]*$')]
        [string]$SchemaName = 'public'
    )

    # A custom-format pg_restore list row is, for example:
    # 4102; 0 24896 TABLE DATA public docs_sw postgres
    # Quoted identifiers are also accepted. The regex contains one regex
    # escape layer only; do not double the backslashes in single-quoted PS.
    $schema = [regex]::Escape($SchemaName)
    $table = [regex]::Escape($TableName)
    $pattern = '(?im)^\s*\d+;\s+\d+\s+\d+\s+TABLE DATA\s+' +
        '"?' + $schema + '"?\s+"?' + $table + '"?(?:\s+|$)'
    return [regex]::IsMatch($CatalogText, $pattern)
}

function Get-DockerfileCopyPair {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Instruction
    )

    if ($Instruction -notmatch '(?i)^\s*COPY\s+(?<arguments>.+?)\s*$') {
        throw "Unsupported Dockerfile instruction: $Instruction"
    }
    $arguments = $matches['arguments'].Trim()
    if ($arguments.StartsWith('[')) {
        try {
            # Windows PowerShell 5.1 can emit a top-level JSON array as one
            # pipeline object. An explicit cast prevents a nested one-item
            # array from making a valid two-token COPY look ambiguous.
            [object[]]$tokens = ($arguments | ConvertFrom-Json)
        } catch {
            throw "Invalid JSON COPY instruction: $Instruction"
        }
        Assert-DeploymentCondition -Condition ($tokens.Count -eq 2) `
            -Message "COPY must have exactly one source: $Instruction"
        return [pscustomobject]@{
            Source = ([string]$tokens[0]).Replace('\', '/')
            Destination = ([string]$tokens[1]).Replace('\', '/')
        }
    }

    # Only the unambiguous two-token shell form is accepted. Build flags,
    # wildcards, environment expansion and multiple sources widen context and
    # are intentionally rejected.
    $match = [regex]::Match($arguments,
        '^"?(?<source>[^"\s]+)"?\s+"?(?<destination>[^"\s]+)"?$')
    Assert-DeploymentCondition -Condition $match.Success `
        -Message "Unsafe or ambiguous COPY instruction: $Instruction"
    return [pscustomobject]@{
        Source = $match.Groups['source'].Value.Replace('\', '/')
        Destination = $match.Groups['destination'].Value.Replace('\', '/')
    }
}

function Assert-DockerfileCopyAllowlist {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$DockerfileText
    )

    $instructions = @([regex]::Split($DockerfileText, '\r?\n') |
        Where-Object { $_ -match '(?i)^\s*(?:COPY|ADD)\s+' })
    Assert-DeploymentCondition -Condition ($instructions.Count -ge 1) `
        -Message 'The Dockerfile does not copy the TDMS WAR.'
    Assert-DeploymentCondition -Condition ($instructions.Count -le 2) `
        -Message 'The Dockerfile copies more than the approved artifacts.'

    $warCount = 0
    $entrypointCount = 0
    foreach ($instruction in $instructions) {
        Assert-DeploymentCondition `
            -Condition ($instruction -notmatch '(?i)^\s*ADD\s+') `
            -Message 'Dockerfile ADD is not permitted.'
        $pair = Get-DockerfileCopyPair -Instruction $instruction
        if ($pair.Source -match '^app/(?:SDMS|TDMS)-KT-1B\.war$' -and
                $pair.Destination -match '^/opt/kt1b/(?:SDMS|TDMS)-KT-1B\.war$') {
            $warCount++
            continue
        }
        if ($pair.Source -match '^(?:docker/)?entrypoint\.sh$' -and
                $pair.Destination -ceq '/opt/kt1b/entrypoint.sh') {
            $entrypointCount++
            continue
        }
        throw "Dockerfile COPY is outside the deployment allowlist: $instruction"
    }

    Assert-DeploymentCondition -Condition ($warCount -eq 1) `
        -Message 'The Dockerfile must copy exactly one approved TDMS WAR.'
    Assert-DeploymentCondition -Condition ($entrypointCount -le 1) `
        -Message 'The Dockerfile copies the entrypoint more than once.'
}

function Assert-RollbackImageIdentity {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [ValidatePattern('^sha256:[0-9a-f]{64}$')]
        [string]$OriginalImageId,

        [Parameter(Mandatory = $true)]
        [ValidatePattern('^sha256:[0-9a-f]{64}$')]
        [string]$RollbackTagImageId
    )

    # The stopped app may already reference a newly prepared image. It is not
    # rollback evidence. Only the immutable image ID captured before quiesce
    # and the rollback tag created from that ID are compared.
    Assert-DeploymentCondition `
        -Condition ($OriginalImageId -ceq $RollbackTagImageId) `
        -Message 'The rollback tag no longer resolves to the original app image.'
}

function Test-PublishedBindingForPort {
    [CmdletBinding()]
    param(
        [AllowNull()]
        [object]$Bindings,

        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    if ($null -eq $Bindings) { return $false }
    $property = $Bindings.PSObject.Properties["$Port/tcp"]
    if ($null -eq $property -or $null -eq $property.Value) { return $false }
    return @($property.Value).Count -gt 0
}

function Assert-SharedNetworkRuntimeContract {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$AppInspectJson,

        [Parameter(Mandatory = $true)]
        [string]$FileApiInspectJson,

        [Parameter(Mandatory = $true)]
        [string]$ConverterInspectJson
    )

    $app = $AppInspectJson | ConvertFrom-Json
    $fileApi = $FileApiInspectJson | ConvertFrom-Json
    $converter = $ConverterInspectJson | ConvertFrom-Json
    Assert-DeploymentCondition -Condition (
        [string]$app.Id -match '^[0-9a-f]{64}$') `
        -Message 'The app inspect payload has an invalid container ID.'

    $expectedNetwork = 'container:' + [string]$app.Id
    foreach ($entry in @(
            @{ Value = $fileApi; Name = 'File API' },
            @{ Value = $converter; Name = 'PDF converter' })) {
        # Docker records service:app as container:<resolved app ID>. This is
        # the authoritative join target. NetworkSettings.SandboxKey describes
        # a libnetwork-owned sandbox; container-mode joiners do not own that
        # sandbox and may expose an empty or different key in docker inspect.
        $networkMode = [string]$entry.Value.HostConfig.NetworkMode
        Assert-DeploymentCondition `
            -Condition ($networkMode -ceq $expectedNetwork) `
            -Message "$($entry.Name) does not share the exact app network namespace."
        foreach ($port in @(9001, 18080)) {
            Assert-DeploymentCondition -Condition (-not
                (Test-PublishedBindingForPort `
                    -Bindings $entry.Value.HostConfig.PortBindings -Port $port)) `
                -Message "$($entry.Name) unexpectedly publishes host port $port."
        }
    }
    foreach ($port in @(9001, 18080)) {
        Assert-DeploymentCondition -Condition (-not
            (Test-PublishedBindingForPort `
                -Bindings $app.HostConfig.PortBindings -Port $port)) `
            -Message "The shared app namespace publishes private port $port."
    }
}

function Install-DeploymentFileSafely {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Source,

        [Parameter(Mandatory = $true)]
        [string]$Target,

        [Parameter(Mandatory = $true)]
        [string]$QuarantineDirectory
    )

    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $Source -PathType Leaf) `
        -Message "Deployment source file is missing: $Source"
    $sourceFull = Get-CanonicalWindowsPath -Path $Source
    $targetFull = Get-CanonicalWindowsPath -Path $Target
    $parent = Split-Path -Parent $targetFull
    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $parent -PathType Container) `
        -Message "Deployment target parent is missing: $parent"
    $quarantine = Get-CanonicalWindowsPath -Path $QuarantineDirectory
    if (-not (Test-Path -LiteralPath $quarantine -PathType Container)) {
        New-Item -ItemType Directory -Path $quarantine | Out-Null
    }

    $suffix = [Guid]::NewGuid().ToString('N')
    $staged = Join-Path $parent (
        '.tdms-next-' + [IO.Path]::GetFileName($targetFull) + '-' + $suffix)
    $replaced = Join-Path $quarantine (
        'replaced-' + [IO.Path]::GetFileName($targetFull) + '-' + $suffix)
    $targetMoved = $false
    $newTargetInstalled = $false
    try {
        Copy-Item -LiteralPath $sourceFull -Destination $staged
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $sourceFull -Algorithm SHA256).Hash -ceq
            (Get-FileHash -LiteralPath $staged -Algorithm SHA256).Hash) `
            -Message 'Deployment file staging hash mismatch.'
        if (Test-Path -LiteralPath $targetFull -PathType Leaf) {
            Move-Item -LiteralPath $targetFull -Destination $replaced
            $targetMoved = $true
        }
        Move-Item -LiteralPath $staged -Destination $targetFull
        $newTargetInstalled = $true
        Assert-DeploymentCondition -Condition (
            (Get-FileHash -LiteralPath $sourceFull -Algorithm SHA256).Hash -ceq
            (Get-FileHash -LiteralPath $targetFull -Algorithm SHA256).Hash) `
            -Message 'Installed deployment file hash mismatch.'
        return $replaced
    } catch {
        if ($targetMoved -and
                (Test-Path -LiteralPath $replaced -PathType Leaf)) {
            if (Test-Path -LiteralPath $targetFull -PathType Leaf) {
                Move-Item -LiteralPath $targetFull -Destination `
                    (Join-Path $quarantine (
                        'failed-' + [IO.Path]::GetFileName($targetFull) + '-' +
                        [Guid]::NewGuid().ToString('N')))
            }
            Move-Item -LiteralPath $replaced -Destination $targetFull
        } elseif ($newTargetInstalled -and
                (Test-Path -LiteralPath $targetFull -PathType Leaf)) {
            Move-Item -LiteralPath $targetFull -Destination `
                (Join-Path $quarantine (
                    'failed-' + [IO.Path]::GetFileName($targetFull) + '-' +
                    [Guid]::NewGuid().ToString('N')))
        }
        throw
    } finally {
        if (Test-Path -LiteralPath $staged -PathType Leaf) {
            Remove-Item -LiteralPath $staged -Force
        }
    }
}

function Read-StrictEnvironmentFile {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [string[]]$ExactKeys
    )

    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $Path -PathType Leaf) `
        -Message "Environment file is missing: $Path"
    $values = [ordered]@{}
    foreach ($line in [IO.File]::ReadAllLines($Path)) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0 -or $trimmed.StartsWith('#')) { continue }
        Assert-DeploymentCondition `
            -Condition ($line -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') `
            -Message 'Environment file contains an invalid line.'
        $key = $matches[1]
        Assert-DeploymentCondition -Condition (-not $values.Contains($key)) `
            -Message "Environment file contains duplicate key $key."
        $value = $matches[2]
        if ($value.Length -ge 2 -and
                (($value.StartsWith("'") -and $value.EndsWith("'")) -or
                 ($value.StartsWith('"') -and $value.EndsWith('"')))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $values[$key] = $value
    }
    if ($null -ne $ExactKeys) {
        Assert-DeploymentCondition -Condition (
            $values.Count -eq $ExactKeys.Count) `
            -Message 'Environment file has an unexpected number of keys.'
        foreach ($key in $ExactKeys) {
            Assert-DeploymentCondition -Condition $values.Contains($key) `
                -Message "Environment file is missing required key $key."
        }
    }
    return $values
}

function Protect-DeploymentSecretFile {
    [CmdletBinding()]
    param([Parameter(Mandatory = $true)][string]$Path)

    $identity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    $output = @(& icacls.exe $Path /inheritance:r `
        /grant:r "${identity}:(F)" 'SYSTEM:(F)' `
        'BUILTIN\Administrators:(F)' 2>&1)
    if ($LASTEXITCODE -ne 0) {
        throw ('Secret ACL update failed: ' +
            (($output | ForEach-Object { [string]$_ }) -join '; '))
    }
}

function Assert-ProtectedDeploymentSecretFile {
    [CmdletBinding()]
    param([Parameter(Mandatory = $true)][string]$Path)

    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $Path -PathType Leaf) `
        -Message 'PDF conversion runtime secret file is missing.'
    $acl = Get-Acl -LiteralPath $Path
    Assert-DeploymentCondition -Condition $acl.AreAccessRulesProtected `
        -Message 'PDF conversion runtime secrets still inherit permissions.'
    $currentSid = [Security.Principal.WindowsIdentity]::GetCurrent().User.Value
    $allowedSids = @($currentSid, 'S-1-5-18', 'S-1-5-32-544')
    foreach ($rule in $acl.Access) {
        try {
            $sid = $rule.IdentityReference.Translate(
                [Security.Principal.SecurityIdentifier]).Value
        } catch {
            $sid = [string]$rule.IdentityReference
        }
        if ($rule.AccessControlType -eq
                [Security.AccessControl.AccessControlType]::Allow -and
                ($rule.FileSystemRights -band
                    [Security.AccessControl.FileSystemRights]::ReadData) -and
                $allowedSids -notcontains $sid) {
            throw 'PDF conversion runtime secrets are readable by an unapproved identity.'
        }
    }
}

function Get-DeploymentDirectoryFingerprint {
    [CmdletBinding()]
    param([Parameter(Mandatory = $true)][string]$Root)

    $fullRoot = Get-CanonicalWindowsPath -Path $Root
    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $fullRoot -PathType Container) `
        -Message "Fingerprint directory is missing: $fullRoot"
    $prefix = $fullRoot.TrimEnd('\') + '\'
    $lines = foreach ($file in @(Get-ChildItem -LiteralPath $fullRoot `
            -Recurse -File -Force | Sort-Object FullName)) {
        $full = Get-CanonicalWindowsPath -Path $file.FullName
        Assert-DeploymentCondition -Condition $full.StartsWith(
            $prefix, [StringComparison]::OrdinalIgnoreCase) `
            -Message 'Fingerprint path escaped its root.'
        '{0}|{1}|{2}' -f
            $full.Substring($prefix.Length).Replace('\', '/'),
            [long]$file.Length,
            (Get-FileHash -LiteralPath $full -Algorithm SHA256).Hash
    }
    $bytes = [Text.UTF8Encoding]::new($false).GetBytes(
        ($lines -join "`n"))
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        $hash = $algorithm.ComputeHash($bytes)
    } finally {
        $algorithm.Dispose()
    }
    return (($hash | ForEach-Object { $_.ToString('X2') }) -join '')
}

function Write-DeploymentJsonAtomically {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [object]$Value,

        [Parameter(Mandatory = $true)]
        [string]$Path,

        [ValidateRange(2, 20)]
        [int]$Depth = 8
    )

    $target = Get-CanonicalWindowsPath -Path $Path
    $parent = Split-Path -Parent $target
    Assert-DeploymentCondition `
        -Condition (Test-Path -LiteralPath $parent -PathType Container) `
        -Message "JSON target directory is missing: $parent"
    Assert-DeploymentCondition -Condition (-not
        (Test-Path -LiteralPath $target)) `
        -Message 'Atomic JSON publication target already exists.'
    $temporary = Join-Path $parent (
        '.json-next-' + [IO.Path]::GetFileName($target) + '-' +
        [Guid]::NewGuid().ToString('N'))
    try {
        [IO.File]::WriteAllText($temporary,
            (($Value | ConvertTo-Json -Depth $Depth) + "`r`n"),
            [Text.UTF8Encoding]::new($false))
        # State and result files are immutable. Moving a completed temporary
        # file into a previously absent name is an atomic same-volume publish
        # on NTFS and avoids File.Replace overload differences in PS 5.1.
        Move-Item -LiteralPath $temporary -Destination $target
    } finally {
        if (Test-Path -LiteralPath $temporary -PathType Leaf) {
            Remove-Item -LiteralPath $temporary -Force
        }
    }
}

function Test-DeploymentByteSequenceEqual {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [byte[]]$Left,

        [Parameter(Mandatory = $true)]
        [byte[]]$Right
    )

    if ($Left.Length -ne $Right.Length) { return $false }
    [int]$difference = 0
    for ($index = 0; $index -lt $Left.Length; $index++) {
        $difference = $difference -bor
            ([int]$Left[$index] -bxor [int]$Right[$index])
    }
    return $difference -eq 0
}

function Invoke-WithIsolatedDeploymentEnvironment {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Keys,

        [Parameter(Mandatory = $true)]
        [scriptblock]$Operation
    )

    Assert-DeploymentCondition -Condition ($Keys.Count -gt 0) `
        -Message 'Deployment environment isolation requires at least one key.'
    $uniqueKeys = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase)
    foreach ($key in $Keys) {
        Assert-DeploymentCondition -Condition (
            -not [string]::IsNullOrWhiteSpace($key) -and
            $key -cmatch '^[A-Z][A-Z0-9_]*$' -and
            $uniqueKeys.Add($key)) `
            -Message 'Deployment environment isolation keys are invalid.'
    }

    $saved = [ordered]@{}
    $snapshotErrors = [Collections.Generic.List[Exception]]::new()
    foreach ($key in $Keys) {
        try {
            $present = Test-Path -LiteralPath "Env:$key"
            $value = if ($present) {
                [string](Get-Item -LiteralPath "Env:$key" `
                    -ErrorAction Stop).Value
            } else { $null }
            $saved[$key] = [pscustomobject]@{
                Present = $present
                Value = $value
            }
        } catch {
            [void]$snapshotErrors.Add(
                [InvalidOperationException]::new(
                    "Deployment environment snapshot failed for $key."))
        }
    }
    if ($snapshotErrors.Count -gt 0) {
        throw [AggregateException]::new(
            'Deployment environment snapshot failed.',
            [Exception[]]$snapshotErrors.ToArray())
    }

    $primaryException = $null
    $operationOutput = @()
    $isolationErrors = [Collections.Generic.List[Exception]]::new()
    foreach ($key in $Keys) {
        try {
            if (Test-Path -LiteralPath "Env:$key") {
                Remove-Item -LiteralPath "Env:$key" -ErrorAction Stop
            }
            if (Test-Path -LiteralPath "Env:$key") {
                throw [InvalidOperationException]::new(
                    "Deployment environment key remains present: $key.")
            }
        } catch {
            [void]$isolationErrors.Add(
                [InvalidOperationException]::new(
                    "Deployment environment removal failed for $key."))
        }
    }

    if ($isolationErrors.Count -gt 0) {
        $primaryException = [AggregateException]::new(
            'Deployment environment isolation failed.',
            [Exception[]]$isolationErrors.ToArray())
    } else {
        try {
            $operationOutput = @(& $Operation)
        } catch {
            $primaryException = $_.Exception
        }
    }

    $restorationErrors = [Collections.Generic.List[Exception]]::new()
    foreach ($key in $Keys) {
        try {
            $record = $saved[$key]
            if ([bool]$record.Present) {
                Set-Item -LiteralPath "Env:$key" `
                    -Value ([string]$record.Value) -ErrorAction Stop
                $restored = Get-Item -LiteralPath "Env:$key" `
                    -ErrorAction Stop
                if ([string]$restored.Value -cne [string]$record.Value) {
                    throw [InvalidOperationException]::new(
                        "Deployment environment key was not restored: $key.")
                }
            } else {
                if (Test-Path -LiteralPath "Env:$key") {
                    Remove-Item -LiteralPath "Env:$key" -ErrorAction Stop
                }
                if (Test-Path -LiteralPath "Env:$key") {
                    throw [InvalidOperationException]::new(
                        "Deployment environment key was introduced: $key.")
                }
            }
        } catch {
            [void]$restorationErrors.Add(
                [InvalidOperationException]::new(
                    "Deployment environment restoration failed for $key."))
        }
    }

    if ($null -ne $primaryException -and $restorationErrors.Count -eq 0) {
        throw $primaryException
    }
    if ($null -ne $primaryException -or $restorationErrors.Count -gt 0) {
        $failures = [Collections.Generic.List[Exception]]::new()
        if ($null -ne $primaryException) {
            [void]$failures.Add($primaryException)
        }
        foreach ($failure in $restorationErrors) {
            [void]$failures.Add($failure)
        }
        throw [AggregateException]::new(
            'Deployment operation or environment restoration failed.',
            [Exception[]]$failures.ToArray())
    }

    return $operationOutput
}

function Assert-PublicProbeCaCertificate {
    [CmdletBinding()]
    param([Parameter(Mandatory = $true)][string]$Path)

    $pem = [IO.File]::ReadAllText($Path, [Text.Encoding]::ASCII)
    $match = [regex]::Match($pem,
        '\A\s*-----BEGIN CERTIFICATE-----\s*' +
        '(?<body>[A-Za-z0-9+/=\s]+?)\s*' +
        '-----END CERTIFICATE-----\s*\z')
    Assert-DeploymentCondition -Condition $match.Success `
        -Message 'Public probe CA must contain exactly one PEM X509 certificate.'
    $certificate = $null
    try {
        $raw = [Convert]::FromBase64String(
            [regex]::Replace($match.Groups['body'].Value, '\s', ''))
        $certificate = [Security.Cryptography.X509Certificates.X509Certificate2]::new(
            $raw)
        $canonicalRaw = $certificate.Export(
            [Security.Cryptography.X509Certificates.X509ContentType]::Cert)
        Assert-DeploymentCondition -Condition (
            $raw.Length -eq $canonicalRaw.Length -and
            (Test-DeploymentByteSequenceEqual -Left $raw `
                -Right $canonicalRaw)) `
            -Message 'Public probe CA contains trailing or non-canonical DER bytes.'
        $rawConstraint = @($certificate.Extensions | Where-Object {
                $_.Oid.Value -ceq '2.5.29.19'
            })
        Assert-DeploymentCondition -Condition ($rawConstraint.Count -eq 1) `
            -Message 'Public probe certificate has no unique basic constraints.'
        $constraint = [Security.Cryptography.X509Certificates.X509BasicConstraintsExtension]::new(
            $rawConstraint[0], $rawConstraint[0].Critical)
        Assert-DeploymentCondition -Condition (
            $constraint.CertificateAuthority -and
            -not $certificate.HasPrivateKey) `
            -Message 'Public probe certificate is not a public CA certificate.'
    } catch {
        throw "Public probe CA certificate validation failed: $($_.Exception.Message)"
    } finally {
        if ($certificate) { $certificate.Dispose() }
    }
}

function Get-PublicProbeCurlArguments {
    [CmdletBinding()]
    param([string]$PublicProbeCaPath)

    $arguments = [Collections.Generic.List[string]]::new()
    foreach ($argument in @('--disable', '--silent', '--show-error',
            '--connect-timeout', '10', '--max-time', '30', '--noproxy', '*',
            '--resolve', 'demo.esob.kr:444:127.0.0.1', '--output', 'NUL',
            '--write-out', '%{http_code}')) {
        [void]$arguments.Add($argument)
    }
    if (-not [string]::IsNullOrWhiteSpace($PublicProbeCaPath)) {
        [void]$arguments.Add('--cacert')
        [void]$arguments.Add($PublicProbeCaPath)
    }
    [void]$arguments.Add('https://demo.esob.kr:444/login/loginPage')
    return $arguments.ToArray()
}

function Invoke-PdfRuntimeOnlyRollback {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [Collections.IDictionary]$Operations,

        [Parameter(Mandatory = $true)]
        [DateTime]$Deadline
    )

    $required = @(
        'AssertDatabaseInvariant',
        'StopGateway',
        'StopReleaseServices',
        'RestoreRuntimeFiles',
        'RestoreOriginalImage',
        'RecreateOriginalApp',
        'RestoreOriginalSidecars',
        'VerifyOriginalApp',
        'VerifyPreservedDataFingerprints',
        'StartExistingGateway')
    foreach ($name in $required) {
        Assert-DeploymentCondition -Condition (
            $Operations.Contains($name) -and
            $Operations[$name] -is [scriptblock]) `
            -Message "Runtime rollback operation is missing: $name"
    }

    # Database and storage restoration are intentionally absent. This is the
    # only rollback path used automatically after an Apply failure.
    foreach ($name in @(
            'StopGateway',
            'StopReleaseServices',
            'RestoreRuntimeFiles',
            'RestoreOriginalImage',
            'RecreateOriginalApp',
            'RestoreOriginalSidecars',
            'VerifyOriginalApp',
            'VerifyPreservedDataFingerprints',
            'StartExistingGateway')) {
        Assert-DeploymentCondition -Condition ((Get-Date) -lt $Deadline) `
            -Message "Rollback deadline expired before $name."
        & $Operations['AssertDatabaseInvariant'] $name 'before'
        & $Operations[$name]
        & $Operations['AssertDatabaseInvariant'] $name 'after'
        Assert-DeploymentCondition -Condition ((Get-Date) -lt $Deadline) `
            -Message "Rollback deadline expired after $name."
    }
}

Export-ModuleMember -Function @(
    'Assert-DeploymentCondition',
    'Get-CanonicalWindowsPath',
    'Get-DeploymentStringSha256',
    'ConvertTo-CanonicalGatewayBindSource',
    'Get-CanonicalGatewayBindContract',
    'Get-GatewayCanaryDockerArguments',
    'Assert-DeploymentChildPath',
    'Test-ContainerNameInDockerList',
    'Test-PgRestoreTableDataEntry',
    'Get-DockerfileCopyPair',
    'Assert-DockerfileCopyAllowlist',
    'Assert-RollbackImageIdentity',
    'Test-PublishedBindingForPort',
    'Assert-SharedNetworkRuntimeContract',
    'Install-DeploymentFileSafely',
    'Read-StrictEnvironmentFile',
    'Protect-DeploymentSecretFile',
    'Assert-ProtectedDeploymentSecretFile',
    'Get-DeploymentDirectoryFingerprint',
    'Write-DeploymentJsonAtomically',
    'Test-DeploymentByteSequenceEqual',
    'Invoke-WithIsolatedDeploymentEnvironment',
    'Assert-PublicProbeCaCertificate',
    'Get-PublicProbeCurlArguments',
    'Invoke-PdfRuntimeOnlyRollback')
