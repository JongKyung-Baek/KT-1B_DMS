[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$SecretsPath
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2.0

function Read-DeploymentSecrets {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $values = @{}
    foreach ($line in [IO.File]::ReadAllLines($Path, [Text.Encoding]::ASCII)) {
        if ([string]::IsNullOrWhiteSpace($line) `
                -or $line.TrimStart().StartsWith('#')) {
            continue
        }

        $separator = $line.IndexOf('=')
        if ($separator -le 0) {
            throw 'The deployment credential file has an invalid entry.'
        }

        $name = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1)
        $values[$name] = $value
    }

    foreach ($requiredName in @(
            'KT1B_POSTGRES_PASSWORD',
            'KT1B_DB_PASSWORD')) {
        if (-not $values.ContainsKey($requiredName) `
                -or [string]::IsNullOrWhiteSpace($values[$requiredName])) {
            throw "The deployment credential file is missing $requiredName."
        }
    }
}

function New-RandomSecret {
    $bytes = New-Object byte[] 32
    $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $generator.GetBytes($bytes)
    } finally {
        $generator.Dispose()
    }
    return [Convert]::ToBase64String($bytes)
}

$resolvedPath = [IO.Path]::GetFullPath($SecretsPath)
$parentDirectory = [IO.Path]::GetDirectoryName($resolvedPath)
if ([string]::IsNullOrWhiteSpace($parentDirectory) `
        -or -not (Test-Path -LiteralPath $parentDirectory -PathType Container)) {
    throw 'The deployment credential directory does not exist.'
}

if (Test-Path -LiteralPath $resolvedPath) {
    if (-not (Test-Path -LiteralPath $resolvedPath -PathType Leaf)) {
        throw 'The deployment credential path is not a file.'
    }
    Read-DeploymentSecrets -Path $resolvedPath
    Write-Host '[OK] Existing deployment credentials were preserved.'
    exit 0
}

$temporaryPath = Join-Path $parentDirectory `
    ".env.$([Guid]::NewGuid().ToString('N')).tmp"
try {
    [IO.File]::WriteAllLines(
        $temporaryPath,
        @(
            "KT1B_POSTGRES_PASSWORD=$(New-RandomSecret)",
            "KT1B_DB_PASSWORD=$(New-RandomSecret)"
        ),
        [Text.Encoding]::ASCII
    )
    [IO.File]::Move($temporaryPath, $resolvedPath)
} finally {
    if (Test-Path -LiteralPath $temporaryPath -PathType Leaf) {
        Remove-Item -LiteralPath $temporaryPath -Force
    }
}

Read-DeploymentSecrets -Path $resolvedPath
Write-Host '[OK] New deployment credentials were created.'
