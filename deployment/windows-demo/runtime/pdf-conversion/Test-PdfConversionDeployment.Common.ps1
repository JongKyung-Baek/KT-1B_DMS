[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Import-Module (Join-Path $PSScriptRoot 'PdfConversionDeployment.Common.psm1') `
    -Force

$script:Passed = 0

function Assert-Test {
    param([bool]$Condition, [string]$Name)
    if (-not $Condition) {
        throw "FAILED: $Name"
    }
    $script:Passed++
    Write-Output "PASS: $Name"
}

$powerShell32 = Join-Path $env:SystemRoot `
    'SysWOW64\WindowsPowerShell\v1.0\powershell.exe'
Assert-Test (Test-Path -LiteralPath $powerShell32 -PathType Leaf) `
    '32-bit Windows PowerShell fixture is available'
$runnerPath = Join-Path $PSScriptRoot 'Invoke-PdfConversionRelease.ps1'
$previousErrorAction = $ErrorActionPreference
try {
    $ErrorActionPreference = 'Continue'
    $bitnessOutput = @(& $powerShell32 -NoLogo -NoProfile `
        -ExecutionPolicy Bypass -File $runnerPath -Mode Preflight `
        -ReleaseId 'pdfconv-bitness-test' -ExpectedRunnerSha256 ('0' * 64) `
        -ExpectedCommonSha256 ('0' * 64) `
        -ExpectedRequestSha256 ('0' * 64) 2>&1)
    $bitnessExit = $LASTEXITCODE
} finally {
    $ErrorActionPreference = $previousErrorAction
}
Assert-Test ($bitnessExit -ne 0 -and
    (($bitnessOutput | ForEach-Object { [string]$_ }) -join "`n") -match
        'requires 64-bit Windows PowerShell 5\.1') `
    '32-bit Windows PowerShell is rejected before release path access'

$catalog = @'
;
; Archive created at 2026-08-21 18:20:00 KST
4102; 0 24896 TABLE DATA public docs_sw postgres
4103; 0 24901 TABLE DATA "public" "docs_sw_file" postgres
4104; 0 24905 TABLE DATA public docs_sw_sub_file postgres
4105; 0 24910 TABLE DATA public docs_history postgres
'@
Assert-Test (Test-PgRestoreTableDataEntry -CatalogText $catalog `
    -TableName 'docs_sw') 'pg_restore shell identifier is recognized'
Assert-Test (Test-PgRestoreTableDataEntry -CatalogText $catalog `
    -TableName 'docs_sw_file') 'pg_restore quoted identifier is recognized'
Assert-Test (-not (Test-PgRestoreTableDataEntry -CatalogText $catalog `
    -TableName 'docs_pdf_conversion')) 'missing table is rejected'

$oneCopyDockerfile = @'
FROM eclipse-temurin:17-jre-jammy
COPY app/TDMS-KT-1B.war /opt/kt1b/TDMS-KT-1B.war
ENTRYPOINT ["java", "-jar", "/opt/kt1b/TDMS-KT-1B.war"]
'@
Assert-DockerfileCopyAllowlist -DockerfileText $oneCopyDockerfile
Assert-Test $true 'single approved WAR COPY is accepted'

$twoCopyDockerfile = @'
FROM eclipse-temurin:17-jre-jammy
COPY ["app/SDMS-KT-1B.war", "/opt/kt1b/SDMS-KT-1B.war"]
COPY docker/entrypoint.sh /opt/kt1b/entrypoint.sh
ENTRYPOINT ["/opt/kt1b/entrypoint.sh"]
'@
Assert-DockerfileCopyAllowlist -DockerfileText $twoCopyDockerfile
Assert-Test $true 'approved JSON WAR and entrypoint COPY are accepted'

$unsafeDockerfile = @'
FROM eclipse-temurin:17-jre-jammy
COPY . /opt/kt1b
'@
$unsafeRejected = $false
try {
    Assert-DockerfileCopyAllowlist -DockerfileText $unsafeDockerfile
} catch {
    $unsafeRejected = $true
}
Assert-Test $unsafeRejected 'broad Docker build-context COPY is rejected'

Assert-Test (-not (Test-ContainerNameInDockerList -Names @() `
    -ExpectedName 'kt1b-dms-file-api-1')) `
    'missing new File API container is a normal false result'

$originalImage = 'sha256:' + ('a' * 64)
Assert-RollbackImageIdentity -OriginalImageId $originalImage `
    -RollbackTagImageId $originalImage
Assert-Test $true 'rollback identity uses immutable backup tag identity'

$appId = 'b' * 64
$appInspect = @{
    Id = $appId
    HostConfig = @{ PortBindings = @{ '3508/tcp' = $null } }
    NetworkSettings = @{ SandboxKey = '/var/run/docker/netns/app-owner' }
} | ConvertTo-Json -Depth 6 -Compress
$fileInspect = @{
    Id = ('c' * 64)
    HostConfig = @{
        NetworkMode = "container:$appId"
        PortBindings = $null
    }
    NetworkSettings = @{ SandboxKey = '' }
} | ConvertTo-Json -Depth 6 -Compress
$converterInspect = @{
    Id = ('d' * 64)
    HostConfig = @{
        NetworkMode = "container:$appId"
        PortBindings = @{}
    }
    NetworkSettings = @{ SandboxKey = '/var/run/docker/netns/joiner-view' }
} | ConvertTo-Json -Depth 6 -Compress
Assert-SharedNetworkRuntimeContract -AppInspectJson $appInspect `
    -FileApiInspectJson $fileInspect -ConverterInspectJson $converterInspect
Assert-Test $true `
    'joiner SandboxKey is not namespace identity when exact app ID matches'

$staleFileInspect = @{
    Id = ('e' * 64)
    HostConfig = @{
        NetworkMode = ('container:' + ('f' * 64))
        PortBindings = $null
    }
    NetworkSettings = @{ SandboxKey = '/var/run/docker/netns/app-owner' }
} | ConvertTo-Json -Depth 6 -Compress
$staleAppRejected = $false
try {
    Assert-SharedNetworkRuntimeContract -AppInspectJson $appInspect `
        -FileApiInspectJson $staleFileInspect `
        -ConverterInspectJson $converterInspect
} catch {
    $staleAppRejected = $true
}
Assert-Test $staleAppRejected `
    'sidecar targeting a pre-recreate app container ID is rejected'

$publishedAppInspect = @{
    Id = $appId
    HostConfig = @{
        PortBindings = @{
            '9001/tcp' = @(@{ HostIp = '0.0.0.0'; HostPort = '9001' })
        }
    }
} | ConvertTo-Json -Depth 6 -Compress
$publishedRejected = $false
try {
    Assert-SharedNetworkRuntimeContract -AppInspectJson $publishedAppInspect `
        -FileApiInspectJson $fileInspect `
        -ConverterInspectJson $converterInspect
} catch {
    $publishedRejected = $true
}
Assert-Test $publishedRejected 'shared namespace host port publication is rejected'

$gatewayInspectMounts = @(
    @{ Type = 'bind'; Source = '/run/desktop/mnt/host/d/KT1B-DMS/runtime/nginx.conf';
        Destination = '/etc/nginx/nginx.conf'; RW = $false },
    @{ Type = 'bind'; Source = 'D:\CollabView\certs\key.pem';
        Destination = '/etc/nginx/collabview-certs/key.pem'; RW = $false },
    @{ Type = 'bind'; Source = 'D:\KT1B-DMS\certs\key.pass';
        Destination = '/run/secrets/tls_key_passphrase'; RW = $false },
    @{ Type = 'bind'; Source = 'D:\KT1B-DMS\certs';
        Destination = '/etc/nginx/kt1b-certs'; RW = $false }
)
$gatewayComposeConfig = @{
    services = @{
        gateway = @{
            volumes = @(
                @{ type = 'bind'; source = 'D:\KT1B-DMS\runtime\nginx.conf';
                    target = '/etc/nginx/nginx.conf'; read_only = $true },
                @{ type = 'bind'; source = 'D:\CollabView\certs\key.pem';
                    target = '/etc/nginx/collabview-certs/key.pem'; read_only = $true },
                @{ type = 'bind'; source = 'D:\KT1B-DMS\certs\key.pass';
                    target = '/run/secrets/tls_key_passphrase'; read_only = $true },
                @{ type = 'bind'; source = 'D:\KT1B-DMS\certs';
                    target = '/etc/nginx/kt1b-certs'; read_only = $true }
            )
        }
    }
} | ConvertTo-Json -Depth 8 -Compress
$gatewayContract = Get-CanonicalGatewayBindContract `
    -InspectMountsJson (ConvertTo-Json -InputObject $gatewayInspectMounts `
        -Depth 6 -Compress) `
    -ComposeConfigJson $gatewayComposeConfig `
    -DeploymentRoot 'D:\KT1B-DMS' -RuntimeRoot 'D:\KT1B-DMS\runtime'
Assert-Test ($gatewayContract.Fingerprint -match '^[0-9A-F]{64}$' -and
    @($gatewayContract.Lines).Count -eq 4 -and
    @($gatewayContract.Mounts).Count -eq 4) `
    'gateway inspect and compose config share the exact four-bind allowlist'

$gatewayImageId = 'sha256:' + ('e' * 64)
$canaryOwnershipToken = '1' * 32
$canaryCidFile = 'D:\KT1B-DMS\run-logs\canary.cid'
$canaryArguments = @(Get-GatewayCanaryDockerArguments `
    -Name 'kt1b-pdfconv-canary-preflight' -ImageId $gatewayImageId `
    -OwnershipToken $canaryOwnershipToken -CidFile $canaryCidFile `
    -BindContract $gatewayContract)
$canaryText = $canaryArguments -join "`n"
Assert-Test ($canaryArguments[0] -ceq 'create' -and
    $canaryArguments -cnotcontains '--rm' -and
    $canaryArguments -ccontains '--network' -and
    $canaryArguments[([Array]::IndexOf($canaryArguments, '--network') + 1)] `
        -ceq 'none' -and
    $canaryArguments -ccontains '--read-only' -and
    $canaryArguments -ccontains '--cidfile' -and
    $canaryArguments[([Array]::IndexOf($canaryArguments, '--cidfile') + 1)] `
        -ceq $canaryCidFile -and
    $canaryArguments -ccontains
        "com.esob.tdms.pdfconv.canary=$canaryOwnershipToken" -and
    $canaryArguments -ccontains '--cap-drop' -and
    $canaryArguments[([Array]::IndexOf($canaryArguments, '--cap-drop') + 1)] `
        -ceq 'ALL' -and
    $canaryArguments -ccontains 'no-new-privileges' -and
    (@($canaryArguments | Where-Object { $_ -ceq '--mount' })).Count -eq 4 -and
    $canaryText -notmatch '(?i)publish|--privileged' -and
    $canaryArguments[$canaryArguments.Count - 4] -ceq $gatewayImageId -and
    $canaryArguments[$canaryArguments.Count - 3] -ceq '-t' -and
    $canaryArguments[$canaryArguments.Count - 2] -ceq '-c' -and
    $canaryArguments[$canaryArguments.Count - 1] -ceq
        '/etc/nginx/nginx.conf') `
    'gateway nginx canary is disposable, isolated, read-only and least-privilege'

$driftedCompose = $gatewayComposeConfig.Replace(
    'D:\\KT1B-DMS\\runtime\\nginx.conf',
    'D:\\KT1B-DMS\\runtime\\alternate.conf')
$gatewayDriftRejected = $false
try {
    [void](Get-CanonicalGatewayBindContract `
        -InspectMountsJson (ConvertTo-Json -InputObject $gatewayInspectMounts `
            -Depth 6 -Compress) -ComposeConfigJson $driftedCompose `
        -DeploymentRoot 'D:\KT1B-DMS' -RuntimeRoot 'D:\KT1B-DMS\runtime')
} catch {
    $gatewayDriftRejected = $true
}
Assert-Test $gatewayDriftRejected `
    'gateway compose bind-source drift is rejected before outage'

$testRoot = Join-Path ([IO.Path]::GetTempPath()) (
    'tdms-pdf-deploy-test-' + [Guid]::NewGuid().ToString('N'))
try {
    $sourceDirectory = Join-Path $testRoot 'source'
    $targetDirectory = Join-Path $testRoot 'target'
    $quarantineDirectory = Join-Path $testRoot 'quarantine'
    New-Item -ItemType Directory -Path $sourceDirectory, $targetDirectory |
        Out-Null
    $source = Join-Path $sourceDirectory 'runtime.env'
    $target = Join-Path $targetDirectory 'runtime.env'
    [IO.File]::WriteAllText($source, 'new', [Text.Encoding]::ASCII)
    [IO.File]::WriteAllText($target, 'old', [Text.Encoding]::ASCII)
    $replaced = Install-DeploymentFileSafely -Source $source `
        -Target $target -QuarantineDirectory $quarantineDirectory
    Assert-Test ((Get-Content -LiteralPath $target -Raw) -ceq 'new') `
        'PowerShell 5.1-safe file install replaces target'
    Assert-Test ((Get-Content -LiteralPath $replaced -Raw) -ceq 'old') `
        'PowerShell 5.1-safe file install quarantines old target'

    $secret = Join-Path $targetDirectory 'pdf-conversion.env'
    [IO.File]::WriteAllLines($secret, @(
            ('KT1B_FILE_API_KEY=' + ('a' * 32)),
            'TDMS_PDF_CONVERSION_CLIENT_ID=tdms',
            ('TDMS_PDF_CONVERSION_SHARED_SECRET=' + ('b' * 32))),
        [Text.Encoding]::ASCII)
    $secretValues = Read-StrictEnvironmentFile -Path $secret -ExactKeys @(
        'KT1B_FILE_API_KEY', 'TDMS_PDF_CONVERSION_CLIENT_ID',
        'TDMS_PDF_CONVERSION_SHARED_SECRET')
    Assert-Test ($secretValues.Count -eq 3) `
        'runtime secret file accepts exactly three required keys'
    Protect-DeploymentSecretFile -Path $secret
    Assert-ProtectedDeploymentSecretFile -Path $secret
    Assert-Test $true 'runtime secret ACL is restricted to approved identities'

    $jsonPath = Join-Path $targetDirectory 'state.json'
    Write-DeploymentJsonAtomically -Value ([ordered]@{
            protocolVersion = 2; state = 'INITIAL' }) -Path $jsonPath
    Assert-Test ((Get-Content -LiteralPath $jsonPath -Raw) -match
        '"state"\s*:\s*"INITIAL"') 'immutable JSON is atomically published'
    $jsonReplayRejected = $false
    try {
        Write-DeploymentJsonAtomically -Value @{ state = 'REPLAY' } `
            -Path $jsonPath
    } catch {
        $jsonReplayRejected = $true
    }
    Assert-Test $jsonReplayRejected 'atomic JSON publication rejects replay'

    $curlWithoutCa = @(Get-PublicProbeCurlArguments)
    Assert-Test ($curlWithoutCa[0] -ceq '--disable' -and
        $curlWithoutCa -ccontains '--noproxy' -and
        $curlWithoutCa[([Array]::IndexOf($curlWithoutCa, '--noproxy') + 1)] `
            -ceq '*' -and
        $curlWithoutCa -cnotcontains '--cacert') `
        'public probe disables curlrc and bypasses every proxy by default'
    $pinnedCaName = Join-Path $targetDirectory 'probe-ca.pem'
    $curlWithCa = @(Get-PublicProbeCurlArguments `
        -PublicProbeCaPath $pinnedCaName)
    $caIndex = [Array]::IndexOf($curlWithCa, '--cacert')
    Assert-Test ($caIndex -gt 0 -and
        $curlWithCa[$caIndex + 1] -ceq $pinnedCaName) `
        'public probe uses the exact pinned CA path when declared'

    $curlHome = Join-Path $testRoot 'malicious-curl-home'
    New-Item -ItemType Directory -Path $curlHome | Out-Null
    $maliciousOutput = Join-Path $testRoot 'curlrc-controlled-output.txt'
    [IO.File]::WriteAllLines((Join-Path $curlHome '_curlrc'), @(
            '--insecure',
            '--proxy "http://127.0.0.1:1"',
            "--output `"$maliciousOutput`"",
            '--write-out "MALICIOUS"'), [Text.Encoding]::ASCII)
    $curlFixture = Join-Path $targetDirectory 'curl-fixture.txt'
    [IO.File]::WriteAllText($curlFixture, 'SAFE', [Text.Encoding]::ASCII)
    $probeKeys = @('CURL_CA_BUNDLE', 'SSL_CERT_FILE', 'SSL_CERT_DIR',
        'CURL_HOME', 'HOME', 'HTTPS_PROXY', 'ALL_PROXY', 'HTTP_PROXY',
        'NO_PROXY', 'TDMS_DEPLOYMENT_TEST_ABSENT')
    $originalEnvironment = [ordered]@{}
    foreach ($key in $probeKeys) {
        $value = [Environment]::GetEnvironmentVariable(
            $key, [EnvironmentVariableTarget]::Process)
        $originalEnvironment[$key] = [pscustomobject]@{
            Present = $null -ne $value
            Value = $value
        }
    }
    try {
        $adversarialEnvironment = [ordered]@{}
        foreach ($key in $probeKeys) {
            if ($key -ceq 'TDMS_DEPLOYMENT_TEST_ABSENT') {
                [Environment]::SetEnvironmentVariable($key, $null,
                    [EnvironmentVariableTarget]::Process)
                continue
            }
            $adversarialValue = switch ($key) {
                'CURL_HOME' { $curlHome }
                'HOME' { $curlHome }
                'HTTPS_PROXY' { 'http://127.0.0.1:1' }
                'ALL_PROXY' { 'http://127.0.0.1:1' }
                'HTTP_PROXY' { 'http://127.0.0.1:1' }
                default { "adversarial-$key" }
            }
            $adversarialEnvironment[$key] = $adversarialValue
            [Environment]::SetEnvironmentVariable($key, $adversarialValue,
                [EnvironmentVariableTarget]::Process)
        }
        $curlExecutable = Join-Path ([Environment]::GetFolderPath(
                [Environment+SpecialFolder]::System)) 'curl.exe'
        $probeResult = @(Invoke-WithIsolatedDeploymentEnvironment `
            -Keys $probeKeys -Operation {
                foreach ($key in $probeKeys) {
                    if (Test-Path -LiteralPath "Env:$key") {
                        throw "Probe environment was not isolated: $key"
                    }
                }
                $output = @(& $curlExecutable --disable --silent --show-error `
                    --noproxy '*' --output - --write-out '|SAFE' `
                    ([Uri]::new($curlFixture).AbsoluteUri) 2>&1)
                [pscustomobject]@{
                    ExitCode = $LASTEXITCODE
                    Text = (($output | ForEach-Object { [string]$_ }) -join '')
                }
            })
        Assert-Test ($probeResult.Count -eq 1 -and
            $probeResult[0].ExitCode -eq 0 -and
            $probeResult[0].Text -ceq 'SAFE|SAFE' -and
            -not (Test-Path -LiteralPath $maliciousOutput)) `
            'malicious curlrc and proxy environment cannot alter probe behavior'

        $restoredExactly = $true
        foreach ($key in $probeKeys | Where-Object {
                $_ -cne 'TDMS_DEPLOYMENT_TEST_ABSENT' }) {
            if (-not (Test-Path -LiteralPath "Env:$key") -or
                    [string](Get-Item -LiteralPath "Env:$key" `
                        -ErrorAction Stop).Value -cne
                    [string]$adversarialEnvironment[$key]) {
                $restoredExactly = $false
            }
        }
        $restoredExactly = $restoredExactly -and -not
            (Test-Path -LiteralPath 'Env:TDMS_DEPLOYMENT_TEST_ABSENT')
        Assert-Test $restoredExactly `
            'probe isolation restores present keys and preserves absent keys'

        $originalFailurePreserved = $false
        try {
            Invoke-WithIsolatedDeploymentEnvironment -Keys $probeKeys `
                -Operation {
                    $env:CURL_HOME = 'operation-mutated-value'
                    throw 'ORIGINAL_PROBE_FAILURE'
                }
        } catch {
            $originalFailurePreserved = $_.Exception.Message -ceq
                'ORIGINAL_PROBE_FAILURE'
        }
        Assert-Test ($originalFailurePreserved -and
            [string]$env:CURL_HOME -ceq
                [string]$adversarialEnvironment['CURL_HOME']) `
            'probe isolation preserves the original operation failure'
    } finally {
        foreach ($key in $probeKeys) {
            $record = $originalEnvironment[$key]
            $value = if ([bool]$record.Present) {
                [string]$record.Value
            } else { $null }
            [Environment]::SetEnvironmentVariable($key, $value,
                [EnvironmentVariableTarget]::Process)
        }
    }

    $sourceCa = $null
    foreach ($candidate in @(Get-ChildItem Cert:\CurrentUser\Root,
            Cert:\LocalMachine\Root)) {
        $rawConstraint = @($candidate.Extensions | Where-Object {
                $_.Oid.Value -ceq '2.5.29.19'
            })
        if ($rawConstraint.Count -ne 1) { continue }
        $constraint = [Security.Cryptography.X509Certificates.X509BasicConstraintsExtension]::new(
            $rawConstraint[0], $rawConstraint[0].Critical)
        if ($constraint.CertificateAuthority) {
            $sourceCa = $candidate
            break
        }
    }
    Assert-Test ($null -ne $sourceCa) `
        'a Windows CA fixture is available for runtime validation'
    $pemHeader = '-----BEGIN CERTIFICATE-----'
    $pemFooter = '-----END CERTIFICATE-----'
    $validPem = $pemHeader + [Environment]::NewLine +
        [Convert]::ToBase64String($sourceCa.RawData,
            [Base64FormattingOptions]::InsertLineBreaks) +
        [Environment]::NewLine + $pemFooter + [Environment]::NewLine
    [IO.File]::WriteAllText($pinnedCaName, $validPem,
        [Text.Encoding]::ASCII)
    Assert-PublicProbeCaCertificate -Path $pinnedCaName
    Assert-Test $true 'single canonical PEM X509 CA is accepted'

    $concatenatedDer = New-Object byte[] ($sourceCa.RawData.Length * 2)
    [Array]::Copy($sourceCa.RawData, 0, $concatenatedDer, 0,
        $sourceCa.RawData.Length)
    [Array]::Copy($sourceCa.RawData, 0, $concatenatedDer,
        $sourceCa.RawData.Length, $sourceCa.RawData.Length)
    $trailingPem = $pemHeader + [Environment]::NewLine +
        [Convert]::ToBase64String($concatenatedDer,
            [Base64FormattingOptions]::InsertLineBreaks) +
        [Environment]::NewLine + $pemFooter + [Environment]::NewLine
    $trailingPath = Join-Path $targetDirectory 'trailing-ca.pem'
    [IO.File]::WriteAllText($trailingPath, $trailingPem,
        [Text.Encoding]::ASCII)
    $trailingRejected = $false
    try {
        Assert-PublicProbeCaCertificate -Path $trailingPath
    } catch {
        $trailingRejected = $_.Exception.Message -match
            'trailing or non-canonical DER bytes'
    }
    Assert-Test $trailingRejected `
        'concatenated DER objects inside one PEM block are rejected'

    $lockedFixture = Join-Path $targetDirectory 'locked-release.bin'
    [IO.File]::WriteAllText($lockedFixture, 'PINNED', [Text.Encoding]::ASCII)
    $lockedStream = [IO.File]::Open($lockedFixture, [IO.FileMode]::Open,
        [IO.FileAccess]::Read, [IO.FileShare]::Read)
    try {
        $writeBlocked = $false
        try {
            $writer = [IO.File]::Open($lockedFixture, [IO.FileMode]::Open,
                [IO.FileAccess]::Write, [IO.FileShare]::None)
            $writer.Dispose()
        } catch [IO.IOException] {
            $writeBlocked = $true
        }
        Assert-Test $writeBlocked `
            'read-shared release handle blocks concurrent replacement writes'
    } finally {
        $lockedStream.Dispose()
    }

    $ancestryRoot = Join-Path $testRoot 'ancestry-root'
    $ancestryParent = Join-Path $ancestryRoot 'staging'
    $ancestryRelease = Join-Path $ancestryParent 'pdfconv-test'
    New-Item -ItemType Directory -Path $ancestryRelease -Force | Out-Null
    $commonFixture = Join-Path $ancestryRelease `
        'PdfConversionDeployment.Common.psm1'
    [IO.File]::WriteAllLines($commonFixture, @(
            "function Get-TdmsPinnedModuleProof { 'PINNED' }",
            "Export-ModuleMember -Function 'Get-TdmsPinnedModuleProof'"),
        [Text.Encoding]::ASCII)
    $commonLock = [IO.File]::Open($commonFixture, [IO.FileMode]::Open,
        [IO.FileAccess]::Read, [IO.FileShare]::Read)
    try {
        $releaseRenameBlocked = $false
        try {
            Move-Item -LiteralPath $ancestryRelease `
                -Destination ($ancestryRelease + '-swapped') -ErrorAction Stop
        } catch [IO.IOException] {
            $releaseRenameBlocked = $true
        }
        Assert-Test $releaseRenameBlocked `
            'locked common module blocks release-directory pathname swap'

        $parentRenameBlocked = $false
        try {
            Move-Item -LiteralPath $ancestryParent `
                -Destination ($ancestryParent + '-swapped') -ErrorAction Stop
        } catch [IO.IOException] {
            $parentRenameBlocked = $true
        }
        Assert-Test $parentRenameBlocked `
            'locked common module blocks staging-ancestor pathname swap'

        $rootRenameBlocked = $false
        try {
            Move-Item -LiteralPath $ancestryRoot `
                -Destination ($ancestryRoot + '-swapped') -ErrorAction Stop
        } catch [IO.IOException] {
            $rootRenameBlocked = $true
        }
        Assert-Test $rootRenameBlocked `
            'locked common module blocks root-ancestor pathname swap'

        $pinnedModule = Import-Module $commonFixture -Force -PassThru
        try {
            Assert-Test ((Get-TdmsPinnedModuleProof) -ceq 'PINNED') `
                'Import-Module consumes the pinned common-module pathname'
        } finally {
            Remove-Module $pinnedModule -Force
        }
    } finally {
        $commonLock.Dispose()
    }
} finally {
    if (Test-Path -LiteralPath $testRoot -PathType Container) {
        Remove-Item -LiteralPath $testRoot -Recurse -Force
    }
}

Write-Output "RESULT=PASS;TESTS=$script:Passed"
