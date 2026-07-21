<#
.SYNOPSIS
    Unified Quality Control Script
#>

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"  # Fail fast on PowerShell-level errors

$projectRoot = Get-Location
$capturedIssues = [System.Collections.Generic.List[string]]::new()
$hasIssue = $false
$scriptTimer = [System.Diagnostics.Stopwatch]::StartNew()

function Exec-Step {
    param (
        [string]$StepName,
        [scriptblock]$Command
    )
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host " ▶ $StepName" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # Reset exit code tracker before running
    $global:LASTEXITCODE = 0
    
    # Execute and capture stdout + stderr cleanly without masking $LASTEXITCODE
    $output = & $Command 2>&1 | Out-String
    Write-Host $output.Trim()

    if ($LASTEXITCODE -ne 0) {
        $script:hasIssue = $true
        $script:capturedIssues.Add("================ [FAILURE: $StepName] ================")
        $script:capturedIssues.Add($output)
        Write-Host "❌ $StepName FAILED! (Exit Code: $LASTEXITCODE)" -ForegroundColor Red
        return $false
    }

    Write-Host "✅ $StepName PASSED!" -ForegroundColor Green
    return $true
}

try {
    # ------------------------------------------------------------------
    # 1. FRONTEND: Lint, Build, and Vitest
    # ------------------------------------------------------------------
    $frontendDir = Join-Path $projectRoot "Lottery-App\frontend"

    if (Test-Path $frontendDir) {
        Push-Location $frontendDir
        try {
            if (-not (Test-Path "node_modules")) {
                Exec-Step "Frontend: Install Dependencies" { npm install }
            }

            # Split into discrete steps so failures don't hide each other
            $lintOk = Exec-Step "Frontend: Lint" { npm run lint }
            if ($lintOk) {
                Exec-Step "Frontend: Build" { npm run build }
            }
            Exec-Step "Frontend: Unit Tests" { npm test }
        }
        finally {
            Pop-Location
        }
    } else {
        Write-Warning "Frontend path not found: $frontendDir"
    }

    # ------------------------------------------------------------------
    # 2. BACKEND: SpotBugs & Unit Tests
    # ------------------------------------------------------------------
    $backendDir = Join-Path $projectRoot "Lottery-App\backend\checker"

    if (Test-Path $backendDir) {
        Push-Location $backendDir
        try {
            # Use cross-platform executable path (works on Windows/macOS/Linux)
            $mvnCmd = if ($IsWindows -or ($env:OS -like "*Windows*")) { ".\mvnw.cmd" } else { "./mvnw" }

            Exec-Step "Backend: Static Analysis & Unit Tests" { 
                # Removed hardcoded test files so ALL suite tests run automatically
                & $mvnCmd clean verify "-Dspotbugs.xmlOutput=false"
            }
        }
        finally {
            Pop-Location
        }
    } else {
        Write-Warning "Backend path not found: $backendDir"
    }

}
finally {
    $scriptTimer.Stop()
    
    # ------------------------------------------------------------------
    # 3. SUMMARY & CLIPBOARD
    # ------------------------------------------------------------------
    Write-Host "`n========================================" -ForegroundColor Yellow
    Write-Host " Elapsed Time: $($scriptTimer.Elapsed.ToString('mm\:ss'))" -ForegroundColor Yellow

    if ($hasIssue) {
        $fullReport = $capturedIssues -join "`r`n`r`n"
        
        # Guard clipboard access (avoids crashes in headless CI/CD environments)
        try { $fullReport | Set-Clipboard } catch {}

        Write-Host "❌ CHECKS FAILED!" -ForegroundColor Red
        Write-Host "📋 Error log copied to clipboard." -ForegroundColor Red
        Exit 1
    } else {
        try { Set-Clipboard -Value $null } catch {}
        Write-Host "🎉 ALL CHECKS PASSED!" -ForegroundColor Green
        Exit 0
    }
}