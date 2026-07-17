Write-Output "--- CHECKING BACKEND (JAVA / SPRING BOOT) ---"
Set-Location -Path "Lottery-App\backend\checker"
.\mvnw.cmd clean compile

Write-Output "`n--- CHECKING FRONTEND (REACT / TYPESCRIPT) ---"
Set-Location -Path "..\..\frontend"
# Installing dependencies if missing (like antd, recharts, react-router-dom)
npm install antd @ant-design/icons axios react-router-dom recharts dayjs
npm run build
cd ../../
