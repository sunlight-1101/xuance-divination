$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"
$mysqlConfig = "C:\Users\m1913\mysql-data\xuance-my.ini"

function Ensure-Path {
  $machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
  $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
  $env:Path = "$machinePath;$userPath"
}

function Test-Port($port) {
  return [bool](netstat -ano | Select-String ":$port\s")
}

Ensure-Path

if (-not (Test-Port 3306)) {
  Start-Process -FilePath "$mysqlBin\mysqld.exe" -ArgumentList "--defaults-file=$mysqlConfig" -WindowStyle Hidden
  Start-Sleep -Seconds 5
}

if (-not (Test-Port 8080)) {
  Start-Process -FilePath "mvn.cmd" `
    -ArgumentList "spring-boot:run" `
    -WorkingDirectory "$root\backend" `
    -RedirectStandardOutput "$root\backend\backend.log" `
    -RedirectStandardError "$root\backend\backend.err.log" `
    -WindowStyle Hidden
  Start-Sleep -Seconds 10
}

if (-not (Test-Port 5173)) {
  Start-Process -FilePath "npm.cmd" `
    -ArgumentList "run", "dev" `
    -WorkingDirectory "$root\frontend" `
    -RedirectStandardOutput "$root\frontend\frontend.log" `
    -RedirectStandardError "$root\frontend\frontend.err.log" `
    -WindowStyle Hidden
  Start-Sleep -Seconds 3
}

Write-Host "MySQL:   http://localhost:3306"
Write-Host "Backend: http://localhost:8080"
Write-Host "Frontend http://localhost:5173"

