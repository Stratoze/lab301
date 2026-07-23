param(
    [ValidateSet('key','full')]
    [string]$Mode = 'key',
    [string]$Root = (Get-Location).Path,
    [int]$MaxFileBytes = 2MB,
    [int]$MaxBundleBytes = 8MB
)

$ErrorActionPreference = 'SilentlyContinue'
$Root = (Resolve-Path $Root).Path
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$summaryPath = Join-Path $Root "project_summary_$stamp.txt"
$bundleBase = Join-Path $Root "project_bundle_$Mode_$stamp"

$excludeDirNames = @(
    '.git',
    '.idea',
    '.vscode',
    'node_modules',
    'target',
    'build',
    'dist',
    'coverage',
    '.next',
    '.vite',
    '.mvn',
    'logs'
)

$includeExtensions = @(
    '.java',
    '.ts',
    '.tsx',
    '.js',
    '.jsx',
    '.json',
    '.xml',
    '.properties',
    '.yml',
    '.yaml',
    '.sql',
    '.md',
    '.html',
    '.css',
    '.txt'
)

$excludeFileNames = @(
    'package-lock.json',
    'yarn.lock',
    'pnpm-lock.yaml',
    'npm-debug.log'
)

function ShouldSkipDirPath([string]$path) {
    $normalized = $path.Replace('\', '/')

    foreach ($dir in $excludeDirNames) {
        if ($normalized -like "*/$dir/*" -or $normalized -like "*/$dir") {
            return $true
        }
    }

    return $false
}

function RedactLine([string]$line) {
    if ($line -match '(?i)(password|passwd|pwd|secret|token|api[_-]?key|client[_-]?secret|authorization|smtp|mail\.password|spring\.datasource\.password|jwt[_-]?secret)\s*[=:]') {
        return ($line -replace '(?i)(password|passwd|pwd|secret|token|api[_-]?key|client[_-]?secret|authorization|smtp|mail\.password|spring\.datasource\.password|jwt[_-]?secret)\s*[=:]\s*.*', '$1=***REDACTED***')
    }

    return $line
}

$allFiles = Get-ChildItem -Path $Root -Recurse -File | Where-Object {
    -not (ShouldSkipDirPath $_.FullName) -and
    $_.Name -notin $excludeFileNames -and
    ($_.Extension -in $includeExtensions -or $_.Name -in @('mvnw', 'mvnw.cmd', 'Dockerfile', '.env.example')) -and
    $_.Length -le $MaxFileBytes
}

if ($Mode -eq 'key') {
    $allFiles = $allFiles | Where-Object {
        $rel = $_.FullName.Substring($Root.Length).TrimStart('\','/').Replace('\','/')

        $rel -match '(^|/)(pom\.xml|package\.json|vite\.config\.ts|tsconfig\.json|application.*\.(properties|yml|yaml)|Dockerfile)$' -or
        $rel -match '(^|/)(src|test|tests)/.*\.(java|ts|tsx|js|jsx|sql|html|css)$' -or
        $rel -match '(Security|Jwt|Auth|Controller|Service|Repository|Entity|Dto|Request|Response|Config|Exception|Mapper|Test|Tests|IT)\.(java|ts|tsx)$'
    }
}

$summary = New-Object System.Text.StringBuilder
[void]$summary.AppendLine("Project collection summary")
[void]$summary.AppendLine("Generated: $(Get-Date)")
[void]$summary.AppendLine("Root: $Root")
[void]$summary.AppendLine("Mode: $Mode")
[void]$summary.AppendLine("File count: $($allFiles.Count)")
[void]$summary.AppendLine("Total bytes: $(($allFiles | Measure-Object Length -Sum).Sum)")
[void]$summary.AppendLine("")
[void]$summary.AppendLine("Files:")

foreach ($file in $allFiles) {
    $rel = $file.FullName.Substring($Root.Length).TrimStart('\','/').Replace('\','/')
    [void]$summary.AppendLine("$rel`t$($file.Length)")
}

Set-Content -LiteralPath $summaryPath -Value $summary.ToString()

$part = 1
$current = New-Object System.Text.StringBuilder
[void]$current.AppendLine("Project bundle mode=$Mode generated=$(Get-Date)")

foreach ($file in $allFiles) {
    $rel = $file.FullName.Substring($Root.Length).TrimStart('\','/').Replace('\','/')
    $content = Get-Content -LiteralPath $file.FullName -Raw

    $redacted = ($content -split "`r?`n" | ForEach-Object { RedactLine $_ }) -join "`r`n"

    $block = "===== FILE: $rel =====`r`n$redacted`r`n===== END FILE =====`r`n`r`n"

    if ($current.Length + $block.Length -gt $MaxBundleBytes -and $current.Length -gt 0) {
        $out = "${bundleBase}_part_$part.txt"
        Set-Content -LiteralPath $out -Value $current.ToString()
        Write-Host "Created $out"

        $part++
        $current = New-Object System.Text.StringBuilder
        [void]$current.AppendLine("Project bundle mode=$Mode generated=$(Get-Date) part=$part")
    }

    [void]$current.Append($block)
}

if ($current.Length -gt 0) {
    $out = "${bundleBase}_part_$part.txt"
    Set-Content -LiteralPath $out -Value $current.ToString()
    Write-Host "Created $out"
}

Write-Host ""
Write-Host "Done."
Write-Host "Upload or paste these files:"
Write-Host $summaryPath

Get-ChildItem "${bundleBase}_part_*.txt" | ForEach-Object {
    Write-Host $_.FullName
}
