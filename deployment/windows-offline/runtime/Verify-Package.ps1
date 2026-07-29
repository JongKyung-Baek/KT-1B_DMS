[CmdletBinding()]
param(
    [switch]$Quiet
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2.0

$packageRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$manifestPath = Join-Path $packageRoot 'checksums.sha256'

if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    Write-Error "Checksum manifest is missing: $manifestPath"
    exit 1
}

$packagePrefix = $packageRoot.TrimEnd('\') + '\'
$checked = 0

foreach ($line in [IO.File]::ReadAllLines(
        $manifestPath,
        [Text.Encoding]::ASCII)) {
    if ([string]::IsNullOrWhiteSpace($line)) {
        continue
    }

    if ($line -notmatch '^([0-9A-Fa-f]{64})  (.+)$') {
        Write-Error "Invalid checksum line: $line"
        exit 1
    }

    $expectedHash = $Matches[1].ToUpperInvariant()
    $relativePath = $Matches[2].Replace('/', '\')

    if ([IO.Path]::IsPathRooted($relativePath)) {
        Write-Error "Absolute paths are not allowed in checksums: $relativePath"
        exit 1
    }

    $fullPath = [IO.Path]::GetFullPath(
        (Join-Path $packageRoot $relativePath))
    if (-not $fullPath.StartsWith(
            $packagePrefix,
            [StringComparison]::OrdinalIgnoreCase)) {
        Write-Error "Checksum path escapes the package: $relativePath"
        exit 1
    }
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        Write-Error "Package file is missing: $relativePath"
        exit 1
    }

    $actualHash = (Get-FileHash -LiteralPath $fullPath `
        -Algorithm SHA256).Hash.ToUpperInvariant()
    if ($actualHash -ne $expectedHash) {
        Write-Error "Checksum mismatch: $relativePath"
        exit 1
    }

    $checked++
    if (-not $Quiet) {
        Write-Host "[OK] $relativePath"
    }
}

if ($checked -eq 0) {
    Write-Error 'The checksum manifest does not contain any files.'
    exit 1
}

Write-Host "[OK] Package verification completed: $checked files."
exit 0
