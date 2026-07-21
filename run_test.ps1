$ErrorActionPreference = "Continue"
$projectRoot = Get-Location
$errorLog = @()
$hasFailed = $false

# Helper function to execute and catch output
function Exec-Step {
    param (
        [string]$StepName,
        [scriptblock]$Command
    )
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host " ▶ $StepName" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    $output = & $Command 2>&1 | Out-String
    Write-Host $output

    if ($LASTEXITCODE -ne 0) {
        $script:hasFailed = $true
        $script:errorLog += "================ FAILURE AT: $StepName ================"
        $script:errorLog += $output
        Write-Host "❌ $StepName FAILED!" -ForegroundColor Red
        return $false
    }
    Write-Host "✅ $StepName PASSED!" -ForegroundColor Green
    return $true
}

# ------------------------------------------------------------------
# 1. FRONTEND: Static Analysis & Unit Tests
# ------------------------------------------------------------------
$frontendDir = Join-Path $projectRoot "Lottery-App\frontend"

if (Test-Path $frontendDir) {
    Push-Location $frontendDir

    # Fast-check dependencies: only run npm install if node_modules is missing
    if (-not (Test-Path "node_modules")) {
        Exec-Step "Frontend: Install Dependencies" { npm install }
    }

    # Run ESLint & Build
    Exec-Step "Frontend: Static Analysis (ESLint & Build)" { npm run lint; if ($LASTEXITCODE -eq 0) { npm run build } }

    # Run Frontend Unit Tests (CI non-interactive mode)
    Exec-Step "Frontend: Unit Tests" { npm test -- --watchAll=false }

    Pop-Location
} else {
    Write-Warning "Frontend directory not found at $frontendDir"
}

# ------------------------------------------------------------------
# 2. BACKEND: Static Analysis & Unit Tests
# ------------------------------------------------------------------
$backendDir = Join-Path $projectRoot "Lottery-App\backend\checker"

if (Test-Path $backendDir) {
    Push-Location $backendDir

    # Run Maven test + static analysis in ONE unified pass
    # Runs the specified unit tests AND SpotBugs/PMD during verify phase
    Exec-Step "Backend: Tests & Static Analysis" { 
        .\mvnw.cmd clean verify -Dtest="CheckerServiceImplTest,TicketServiceImplTest,UserServiceImplTest,AuthControllerTest" -DfailIfNoTests=false 
    }

    Pop-Location
} else {
    Write-Warning "Backend directory not found at $backendDir"
}

# ------------------------------------------------------------------
# 3. SUMMARY & CLIPBOARD FEEDBACK
# ------------------------------------------------------------------
Write-Host "`n========================================" -ForegroundColor Yellow
if ($hasFailed) {
    $clipboardText = $errorLog -join "`r`n`r`n"
    $clipboardText | Set-Clipboard
    Write-Host "❌ SUITE FAILED. Error details copied to clipboard!" -ForegroundColor Red
    Write-Host "👉 Press Ctrl+V anywhere to paste the exact error stack trace." -ForegroundColor Red
    Exit 1
} else {
    Set-Clipboard -Value $null
    Write-Host "🎉 ALL FRONTEND & BACKEND CHECKS PASSED PERFECTLY!" -ForegroundColor Green
    Exit 0
}