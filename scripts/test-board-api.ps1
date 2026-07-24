# Meetory board API manual test
# Usage: powershell -NoProfile -File scripts/test-board-api.ps1

$ErrorActionPreference = "Stop"
$BaseUrl = "http://localhost:8080"
$Email = "boardtest@meetory.com"
$Password = "password12"
$Nickname = "boardtester"

function Write-Step($msg) { Write-Host ""; Write-Host "== $msg ==" -ForegroundColor Cyan }

Write-Step "0. health check"
try {
    Invoke-WebRequest -Uri "$BaseUrl/api/boards" -Method GET -UseBasicParsing | Out-Null
} catch {
    Write-Host "Backend is not running on $BaseUrl. Start with: .\gradlew bootRun" -ForegroundColor Red
    exit 1
}
Write-Host "backend ok"

Write-Step "1. signup (skip if exists)"
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/auth/signup" -Method POST -ContentType "application/json" `
        -Body (@{ email = $Email; password = $Password; nickname = $Nickname } | ConvertTo-Json) | Out-Null
    Write-Host "signup ok"
} catch {
    Write-Host "signup skipped"
}

Write-Step "2. login"
$login = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -ContentType "application/json" `
    -Body (@{ email = $Email; password = $Password } | ConvertTo-Json)
$token = $login.data.accessToken
$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
Write-Host "login ok nickname=$($login.data.nickname)"

Write-Step "3. create board"
$create = Invoke-RestMethod -Uri "$BaseUrl/api/boards" -Method POST -Headers $headers `
    -Body (@{ title = "API test post"; content = "Created by test-board-api.ps1" } | ConvertTo-Json)
$boardId = $create.data.id
Write-Host "created id=$boardId"

Write-Step "4. list boards (no auth)"
$list = Invoke-RestMethod -Uri "$BaseUrl/api/boards" -Method GET
Write-Host "list count=$($list.data.Count)"

Write-Step "5. get detail"
$detail = Invoke-RestMethod -Uri "$BaseUrl/api/boards/$boardId" -Method GET
Write-Host "detail title=$($detail.data.title)"

Write-Step "6. update board"
$updated = Invoke-RestMethod -Uri "$BaseUrl/api/boards/$boardId" -Method PUT -Headers $headers `
    -Body (@{ title = "API test post (updated)"; content = "Updated content." } | ConvertTo-Json)
Write-Host "updated title=$($updated.data.title)"

Write-Step "7. delete board"
Invoke-RestMethod -Uri "$BaseUrl/api/boards/$boardId" -Method DELETE -Headers $headers | Out-Null
Write-Host "deleted id=$boardId"

Write-Step "DONE"
Write-Host "All board API checks passed" -ForegroundColor Green
