$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $root "pet-runs"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$zipPath = Join-Path $outputDir "xuance-divination-deploy-$timestamp.zip"
$staging = Join-Path $outputDir "deploy-staging-$timestamp"
$rootFull = (Resolve-Path -LiteralPath $root).Path.TrimEnd('\')

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
New-Item -ItemType Directory -Force -Path $staging | Out-Null

$excludeSegments = @(
  ".git",
  ".idea",
  ".vscode",
  "node_modules",
  "dist",
  "target",
  "pet-runs",
  "mysql-data"
)

function Is-ExcludedPath($fullPath) {
  $relative = $fullPath.Substring($rootFull.Length).TrimStart('\')
  $segments = $relative -split '[\\/]'
  foreach ($segment in $segments) {
    if ($excludeSegments -contains $segment) {
      return $true
    }
  }
  if ($relative.EndsWith(".log") -or $relative.EndsWith(".err.log")) {
    return $true
  }
  return $false
}

Get-ChildItem -Path $root -Recurse -File -Force | ForEach-Object {
  if (Is-ExcludedPath $_.FullName) {
    return
  }
  $relative = $_.FullName.Substring($rootFull.Length).TrimStart('\')
  $target = Join-Path $staging $relative
  $targetDir = Split-Path -Parent $target
  New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
  Copy-Item -LiteralPath $_.FullName -Destination $target -Force
}

Compress-Archive -Path (Join-Path $staging "*") -DestinationPath $zipPath -Force
Remove-Item -LiteralPath $staging -Recurse -Force

Write-Host "Deploy zip created:"
Write-Host $zipPath
