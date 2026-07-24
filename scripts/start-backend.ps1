param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Write-Host "== Meetory backend start ==" -ForegroundColor Cyan

$listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($listeners) {
    $pids = $listeners | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($pid in $pids) {
        $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
        Write-Host "Stopping PID $pid ($($proc.ProcessName)) on port $Port..."
        Stop-Process -Id $pid -Force
    }
    Start-Sleep -Seconds 2
}

Push-Location $root
try {
    $env:SPRING_DEVTOOLS_RESTART_ENABLED = "false"
    Write-Host "Starting Spring Boot on port $Port..."
    .\gradlew.bat bootRun --no-daemon
} finally {
    Pop-Location
}
