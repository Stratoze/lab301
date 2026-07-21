Write-Output "--- CHECKING BACKEND (JAVA / SPRING BOOT) ---"
Set-Location -Path "Lottery-App\backend\checker"

# Run Maven compile + SpotBugs (or use 'pmd:check')
.\mvnw.cmd clean verify spotbugs:check

if ($LASTEXITCODE -ne 0) {
    Write-Error "Backend static analysis failed!"
    Exit 1
}

Write-Output "`n--- CHECKING FRONTEND (REACT / TYPESCRIPT) ---"
Set-Location -Path "..\..\frontend"

# Install dependencies if missing
npm install antd @ant-design/icons axios react-router-dom recharts dayjs

# Run ESLint to check for unused variables in React/TS before building
npm run lint

# Build production bundle
npm run build

Set-Location -Path "..\..\"
