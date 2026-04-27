# Navio setup script (English-only to avoid Korean Windows encoding issues)
# Usage: cd C:\Navio\NAVIO ; ./setup.ps1

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }

function Section($msg) {
    Write-Host ""
    Write-Host "=========================================================" -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Cyan
    Write-Host "=========================================================" -ForegroundColor Cyan
}

# 1. Check tools
Section "1/4  Check tools"
$ok = $true
foreach ($t in @("java", "node", "docker")) {
    try {
        & $t --version 2>&1 | Out-Null
        Write-Host "  [OK] $t" -ForegroundColor Green
    } catch {
        Write-Host "  [FAIL] $t not found" -ForegroundColor Red
        $ok = $false
    }
}
if (-not $ok) { Write-Host "Install missing tools first."; exit 1 }

# 2. Docker compose up
Section "2/4  Start MySQL + Redis (Docker)"
Push-Location $root
try { docker compose up -d } catch { Write-Host "  [FAIL] Docker not running?" -ForegroundColor Red; Pop-Location; exit 1 }
Pop-Location
Write-Host "  [OK] Containers started (MySQL:3306, Redis:6379)" -ForegroundColor Green

# 3. Gradle wrapper JAR
Section "3/4  Gradle wrapper JAR"
$wrapperJar = Join-Path $root "backend\gradle\wrapper\gradle-wrapper.jar"
if (Test-Path $wrapperJar) {
    Write-Host "  [OK] Already exists" -ForegroundColor Green
} else {
    Write-Host "  Downloading from jsdelivr CDN..." -ForegroundColor Gray
    $urls = @(
        "https://cdn.jsdelivr.net/gh/spring-projects/spring-boot@v3.2.5/gradle/wrapper/gradle-wrapper.jar",
        "https://cdn.jsdelivr.net/gh/gradle/gradle@v8.7.0/gradle/wrapper/gradle-wrapper.jar"
    )
    $downloaded = $false
    foreach ($u in $urls) {
        try {
            Invoke-WebRequest -Uri $u -OutFile $wrapperJar -UseBasicParsing -ErrorAction Stop
            Write-Host "  [OK] Downloaded from $u" -ForegroundColor Green
            $downloaded = $true
            break
        } catch {
            Write-Host "  [SKIP] $u failed" -ForegroundColor Yellow
        }
    }
    if (-not $downloaded) {
        Write-Host "  [WARN] Could not download. Open backend/ in IntelliJ to auto-generate." -ForegroundColor Yellow
    }
}

# 4. Frontend
Section "4/4  Frontend npm install"
$feDir = Join-Path $root "frontend"
$nodeModules = Join-Path $feDir "node_modules"
if (Test-Path $nodeModules) {
    Write-Host "  Cleaning broken node_modules..." -ForegroundColor Gray
    Remove-Item -Recurse -Force $nodeModules -ErrorAction SilentlyContinue
}
$envFile = Join-Path $feDir ".env.local"
if (-not (Test-Path $envFile)) {
    Copy-Item (Join-Path $feDir ".env.example") $envFile
    Write-Host "  [OK] Created .env.local" -ForegroundColor Green
}
Push-Location $feDir
try {
    npm install --no-audit --no-fund
    Write-Host "  [OK] npm install done" -ForegroundColor Green
} catch {
    Write-Host "  [FAIL] npm install failed" -ForegroundColor Red
}
Pop-Location

# Done
Section "Done!"
Write-Host @"

Next:
  Terminal 1: cd backend  ; ./gradlew.bat bootRun
  Terminal 2: cd frontend ; npm run dev

Open browser: http://localhost:5173
"@ -ForegroundColor Green
